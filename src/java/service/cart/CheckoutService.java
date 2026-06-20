package service.cart;
import service.shop.PromotionService;
import service.catalog.InventoryService;
import service.order.OrderService;
import dao.shop.PromotionDAO;
import service.shop.PaymentService;
import service.system.EmailService;
import service.chat.NotificationService;
import service.auth.UserService;
import exception.BusinessException;

import config.AppConfig;
import dao.cart.CartDAO;
import dao.order.OrderDAO;
import dao.order.OrderItemDAO;
import dao.catalog.ProductVariantDAO;

import dao.system.SystemConfigDAO;
import dao.auth.UserAddressDAO;
import dao.auth.UserDAO;
import model.dto.product.CartSummaryDTO;
import model.dto.checkout.CheckoutRequestDTO;
import model.dto.checkout.CheckoutResultDTO;
import model.dto.checkout.CheckoutViewData;
import model.entity.cart.CartItem;
import model.entity.order.Order;
import model.entity.catalog.ProductVariant;
import model.entity.Promotion;
import model.entity.auth.User;
import util.LoggerUtil;
import util.UserLockManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Business logic cho luong checkout.
 */
public class CheckoutService {

    private static final Logger log = LoggerUtil.getLogger(CheckoutService.class);

    private static final BigDecimal DELIVERY_FEE_PER_SHOP = new BigDecimal("15000");
    private static final String PHONE_REGEX = "^(0|\\+84)[35789][0-9]{8}$";
    /** PAY-01: COD is blocked for orders >= 2,000,000 VND */
    private static final BigDecimal COD_MAX_AMOUNT = new BigDecimal("2000000");
    /** DEL-02: Maximum cart weight for express delivery in kg */
    private static final BigDecimal MAX_CART_WEIGHT_KG = new BigDecimal("30");

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();
    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final PromotionService promotionService = new PromotionService();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final PaymentService paymentService = new PaymentService();
    private final NotificationService notificationService = new NotificationService();
    private final UserService userService = new UserService();
    private final InventoryService inventoryService = new InventoryService();
    private final UserAddressDAO userAddressDAO = new UserAddressDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    public CheckoutViewData buildCheckoutView(User user, List<Integer> requestedVariantIds) throws SQLException {
        CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
        }
        if (requestedVariantIds == null) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
        }

        List<CartItem> selectedItems = filterCheckoutItems(cartSummary.getItems(), requestedVariantIds, false);
        if (selectedItems != null) {
            cartSummary.setItems(selectedItems);
            cartSummary.setSubtotal(calculateSubtotal(selectedItems, null));
            cartSummary.setTotalWeight(calculateTotalWeight(selectedItems));
        }

        Map<Integer, List<CartItem>> itemsByOwner = groupItemsByOwnerId(cartSummary.getItems(), null);
        BigDecimal deliveryFee = DELIVERY_FEE_PER_SHOP.multiply(new BigDecimal(itemsByOwner.size()));
        cartSummary.setDeliveryFee(deliveryFee);
        cartSummary.setTotal(cartSummary.getSubtotal().add(deliveryFee));

        CheckoutViewData viewData = new CheckoutViewData();
        viewData.setCartSummary(cartSummary);
        viewData.setUserAddresses(userAddressDAO.findByUser(user.getUserId()));
        viewData.setShopCount(itemsByOwner.size());
        viewData.setDirectSaleAmount(calculateDirectSaleAmount(cartSummary.getItems()));
        if (itemsByOwner.size() == 1) {
            viewData.setShopOwnerId(itemsByOwner.keySet().iterator().next());
        }
        return viewData;
    }

    public CheckoutResultDTO placeOrder(User user, CheckoutRequestDTO request, String remoteAddress)
            throws SQLException, InterruptedException {
        validateRequest(user, request);
        userService.saveOrUpdateCheckoutContactInfo(
                user,
                request.getFullName(),
                request.getPhone(),
                request.getDeliveryAddress(),
                request.isSaveAddressToBook()
        );

        ReentrantLock lock = UserLockManager.getLock(user.getUserId());
        if (!lock.tryLock(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Yeu cau dat hang cua ban dang duoc he thong xu ly. Vui long khong bam lien tiep.");
        }

        try {
            CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
            List<CartItem> checkoutItems = filterCheckoutItems(cartSummary.getItems(), request.getVariantIds(), true);
            Map<Integer, ProductVariant> variantMap = loadVariantMap(checkoutItems);

            validateStock(checkoutItems, variantMap);

            BigDecimal subtotal = calculateSubtotal(checkoutItems, variantMap);

            // DEL-02: Block checkout if total cart weight > 30 kg
            BigDecimal totalWeight = calculateTotalWeight(checkoutItems);
            if (totalWeight.compareTo(MAX_CART_WEIGHT_KG) > 0) {
                throw new BusinessException("DEL-02",
                        "Đơn giao hỏa tốc tối đa 30kg. Tổng trọng lượng giỏ hàng của bạn là "
                        + totalWeight.setScale(1, RoundingMode.HALF_UP) + " kg.");
            }

            // PAY-01: COD blocked for orders >= 2,000,000 VND (pre-discount subtotal + delivery)
            if (AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())) {
                Map<Integer, List<CartItem>> ownerGroupForFee = groupItemsByOwnerId(checkoutItems, variantMap);
                BigDecimal estimatedDeliveryFee = DELIVERY_FEE_PER_SHOP.multiply(new BigDecimal(ownerGroupForFee.size()));
                BigDecimal estimatedTotal = subtotal.add(estimatedDeliveryFee);
                if (estimatedTotal.compareTo(COD_MAX_AMOUNT) >= 0) {
                    throw new BusinessException("PAY-01",
                            "COD chỉ áp dụng cho đơn dưới 2.000.000đ. Vui lòng chọn chuyển khoản.");
                }
                // SEC-01: Block COD for customers with > 3 failed deliveries in 30 days
                if (!orderService.isCodEligible(user.getUserId())) {
                    throw new BusinessException("SEC-01",
                            "Tài khoản của bạn tạm thời không được sử dụng COD do có quá nhiều đơn giao thất bại. Vui lòng chọn chuyển khoản.");
                }
            }

            Map<Integer, List<CartItem>> itemsByOwner = groupItemsByOwnerId(checkoutItems, variantMap);

            CheckoutResultDTO result;
            if (itemsByOwner.size() > 1) {
                result = placeMultiShopOrder(user, request, checkoutItems, itemsByOwner, variantMap, subtotal, remoteAddress);
            } else {
                result = placeSingleShopOrder(user, request, checkoutItems, variantMap, subtotal, remoteAddress);
            }

            result.setPurgedVariantIds(buildPurgedVariantIds(checkoutItems));
            return result;
        } finally {
            lock.unlock();
            UserLockManager.cleanUp(user.getUserId());
        }
    }

    private CheckoutResultDTO placeSingleShopOrder(User user,
                                                   CheckoutRequestDTO request,
                                                   List<CartItem> checkoutItems,
                                                   Map<Integer, ProductVariant> variantMap,
                                                   BigDecimal subtotal,
                                                   String remoteAddress) throws SQLException {
        int ownerId = resolveSingleShopOwnerId(checkoutItems, variantMap);
        if (user.getUserId() == ownerId) {
            throw new IllegalArgumentException("Bạn không thể mua hàng từ cửa hàng của chính mình.");
        }

        Promotion shopPromo = null;
        Promotion systemPromo = null;
        BigDecimal deliveryFee = DELIVERY_FEE_PER_SHOP;
        BigDecimal shopDiscount = BigDecimal.ZERO;
        BigDecimal systemDiscount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.add(deliveryFee);

        if (isNotBlank(request.getShopCouponCode())) {
            // coupon-scope: re-resolve from DB, validate scope ownership — never trust client data
            shopPromo = promotionService.resolveAndValidateShopCouponScope(
                    request.getShopCouponCode(), ownerId, subtotal);
        }
        if (isNotBlank(request.getSystemCouponCode())) {
            systemPromo = promotionService.validateSystemCoupon(request.getSystemCouponCode(), subtotal);
            if (systemPromo == null) {
                throw new IllegalArgumentException("Mã giảm giá của sàn không hợp lệ, đã hết hạn, hoặc chưa đạt giá trị đơn tối thiểu.");
            }
        }
        // PRO-01: reject double-discount stacking
        promotionService.validateCouponStack(shopPromo, systemPromo);
        if (shopPromo != null || systemPromo != null) {
            BigDecimal[] calcs = promotionService.calculateAllDiscounts(shopPromo, systemPromo, subtotal, deliveryFee);
            shopDiscount = calcs[0];
            systemDiscount = calcs[1];
            totalDiscount = calcs[2];
            finalAmount = calcs[3];
        }

        BigDecimal platformFeeRate = BigDecimal.valueOf(
                configDAO.getDouble(AppConfig.CONFIG_PLATFORM_FEE_RATE, AppConfig.PLATFORM_FEE_RATE_DEFAULT / 100.0)
        );
        BigDecimal netMerchandiseAmount = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);
        BigDecimal platformFee = netMerchandiseAmount.multiply(platformFeeRate).setScale(0, RoundingMode.HALF_UP);

        int orderId;
        try (Connection conn = orderDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                Order order = buildOrder(user, ownerId, null, AppConfig.ORDER_TYPE_CHILD, request,
                        resolveInitialStatus(request.getPaymentMethod()), subtotal, deliveryFee,
                        totalDiscount, systemDiscount, shopDiscount, platformFee, finalAmount);
                orderId = orderDAO.save(conn, order);

                orderItemDAO.saveBatch(conn, orderId, checkoutItems, variantMap);
                reserveInventory(conn, checkoutItems, orderId, user.getUserId());

                savePromotionUsage(conn, orderId, user.getUserId(), shopPromo, shopDiscount);
                savePromotionUsage(conn, orderId, user.getUserId(), systemPromo, systemDiscount);
                cartDAO.deleteItemsByCustomer(conn, user.getUserId(), request.getVariantIds());
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        initPaymentIfNeeded(orderId, request.getPaymentMethod(), remoteAddress);

        // Gửi thông báo đặt hàng cho Customer và Shop Owner
        try {
            String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + orderId;
            String customerMsg = "Đơn hàng #" + orderId + " của bạn đã được tạo thành công.";
            notificationService.send(user.getUserId(), AppConfig.NOTIF_ORDER_UPDATE, "Đặt hàng thành công", customerMsg, "/orders/detail?orderId=" + orderId);
            emailService.sendOrderNotificationEmail(user.getEmail(), user.getFullName(), String.valueOf(orderId), "Đặt hàng thành công", orderDetailUrl);
            
            User shopOwner = userDAO.findUserById(ownerId);
            if (shopOwner != null) {
                String shopMsg = "Bạn có đơn hàng mới #" + orderId + " cần chuẩn bị.";
                notificationService.send(ownerId, AppConfig.NOTIF_ORDER_UPDATE, "Có đơn hàng mới cần chuẩn bị", shopMsg, "/shop/orders");
                emailService.sendOrderNotificationEmail(shopOwner.getEmail(), shopOwner.getFullName(), String.valueOf(orderId), "Có đơn hàng mới cần chuẩn bị", AppConfig.APP_BASE_URL + "/shop/orders");
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo đặt hàng cho orderId=" + orderId, ex);
        }

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(orderId);
        result.setPaymentRequired(AppConfig.PAYMENT_CK.equals(request.getPaymentMethod()));
        result.setSuccessMessage("Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
        return result;
    }

    private CheckoutResultDTO placeMultiShopOrder(User user,
                                                  CheckoutRequestDTO request,
                                                  List<CartItem> checkoutItems,
                                                  Map<Integer, List<CartItem>> itemsByOwner,
                                                  Map<Integer, ProductVariant> variantMap,
                                                  BigDecimal subtotal,
                                                  String remoteAddress) throws SQLException {
        for (Integer ownerId : itemsByOwner.keySet()) {
            if (ownerId != null && ownerId == user.getUserId()) {
                throw new IllegalArgumentException("Bạn không thể mua hàng từ cửa hàng của chính mình.");
            }
        }

        Map<Integer, BigDecimal> subtotalByOwner = new LinkedHashMap<>();
        Map<Integer, BigDecimal> shopDiscountByOwner = new LinkedHashMap<>();
        Map<Integer, BigDecimal> systemDiscountByOwner = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<CartItem>> entry : itemsByOwner.entrySet()) {
            BigDecimal ownerSubtotal = calculateSubtotal(entry.getValue(), variantMap);
            subtotalByOwner.put(entry.getKey(), ownerSubtotal);
            shopDiscountByOwner.put(entry.getKey(), BigDecimal.ZERO);
            systemDiscountByOwner.put(entry.getKey(), BigDecimal.ZERO);
        }

        Promotion shopPromo = null;
        Integer shopPromoOwnerId = null;
        if (isNotBlank(request.getShopCouponCode())) {
            // coupon-scope: find which owner in the cart owns this coupon by trying each with DB re-resolution.
            // resolveAndValidateShopCouponScope already checks code+ownerId in DB so it's safe against spoofing.
            for (Integer ownerId : itemsByOwner.keySet()) {
                Promotion candidate = promotionService.validateShopCoupon(
                        request.getShopCouponCode(), ownerId, subtotalByOwner.get(ownerId));
                if (candidate != null) {
                    shopPromo = candidate;
                    shopPromoOwnerId = ownerId;
                    break;
                }
            }
            if (shopPromo == null) {
                // Explicit scope enforcement: re-resolve throws COUPON-SCOPE with first owner if all fail
                throw new BusinessException("COUPON-SCOPE",
                        "Mã voucher shop không hợp lệ hoặc không thuộc bất kỳ shop nào trong giỏ hàng.");
            }
            BigDecimal shopDiscount = promotionService.calculateDiscount(shopPromo, subtotalByOwner.get(shopPromoOwnerId));
            shopDiscountByOwner.put(shopPromoOwnerId, shopDiscount);
        }

        BigDecimal totalShopDiscount = sumValues(shopDiscountByOwner);
        BigDecimal afterShopTotal = subtotal.subtract(totalShopDiscount).max(BigDecimal.ZERO);
        Promotion systemPromo = null;
        if (isNotBlank(request.getSystemCouponCode())) {
            systemPromo = promotionService.validateSystemCoupon(request.getSystemCouponCode(), afterShopTotal);
            if (systemPromo == null) {
                throw new IllegalArgumentException("Mã voucher sàn không hợp lệ, đã hết hạn, hoặc chưa đạt giá trị đơn tối thiểu.");
            }
            // PRO-01: reject double-discount stacking
            promotionService.validateCouponStack(shopPromo, systemPromo);
            BigDecimal systemDiscount = promotionService.calculateDiscount(systemPromo, afterShopTotal);
            Map<Integer, BigDecimal> allocationBase = new LinkedHashMap<>();
            for (Integer ownerId : itemsByOwner.keySet()) {
                allocationBase.put(ownerId, subtotalByOwner.get(ownerId)
                        .subtract(shopDiscountByOwner.get(ownerId)).max(BigDecimal.ZERO));
            }
            systemDiscountByOwner.putAll(allocateDiscount(systemDiscount, allocationBase));
        }

        BigDecimal deliveryFee = DELIVERY_FEE_PER_SHOP.multiply(new BigDecimal(itemsByOwner.size()));
        BigDecimal totalSystemDiscount = sumValues(systemDiscountByOwner);
        BigDecimal totalDiscount = totalShopDiscount.add(totalSystemDiscount);
        BigDecimal finalAmount = subtotal.subtract(totalDiscount).add(deliveryFee).max(BigDecimal.ZERO);
        BigDecimal platformFeeRate = BigDecimal.valueOf(
                configDAO.getDouble(AppConfig.CONFIG_PLATFORM_FEE_RATE, AppConfig.PLATFORM_FEE_RATE_DEFAULT / 100.0)
        );
        String status = resolveInitialStatus(request.getPaymentMethod());

        int parentOrderId;
        Map<Integer, Integer> childOrderIdByOwner = new LinkedHashMap<>();
        BigDecimal totalPlatformFee = BigDecimal.ZERO;
        try (Connection conn = orderDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                Order parentOrder = buildOrder(user, null, null, AppConfig.ORDER_TYPE_PARENT, request,
                        status, subtotal, deliveryFee, totalDiscount, totalSystemDiscount, totalShopDiscount,
                        BigDecimal.ZERO, finalAmount);
                parentOrderId = orderDAO.save(conn, parentOrder);

                for (Integer ownerId : itemsByOwner.keySet()) {
                    BigDecimal ownerSubtotal = subtotalByOwner.get(ownerId);
                    BigDecimal ownerShopDiscount = shopDiscountByOwner.get(ownerId);
                    BigDecimal ownerSystemDiscount = systemDiscountByOwner.get(ownerId);
                    BigDecimal ownerDiscount = ownerShopDiscount.add(ownerSystemDiscount);
                    BigDecimal netMerchandise = ownerSubtotal.subtract(ownerDiscount).max(BigDecimal.ZERO);
                    BigDecimal ownerPlatformFee = netMerchandise.multiply(platformFeeRate).setScale(0, RoundingMode.HALF_UP);
                    totalPlatformFee = totalPlatformFee.add(ownerPlatformFee);

                    Order childOrder = buildOrder(user, ownerId, parentOrderId, AppConfig.ORDER_TYPE_CHILD, request,
                            status, ownerSubtotal, DELIVERY_FEE_PER_SHOP, ownerDiscount, ownerSystemDiscount,
                            ownerShopDiscount, ownerPlatformFee, netMerchandise.add(DELIVERY_FEE_PER_SHOP));
                    int childOrderId = orderDAO.save(conn, childOrder);
                    childOrderIdByOwner.put(ownerId, childOrderId);

                    List<CartItem> ownerItems = itemsByOwner.get(ownerId);
                    orderItemDAO.saveBatch(conn, childOrderId, ownerItems, variantMap);
                    reserveInventory(conn, ownerItems, childOrderId, user.getUserId());

                    if (shopPromo != null && shopPromoOwnerId != null && shopPromoOwnerId.equals(ownerId)
                            && ownerShopDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        savePromotionUsage(conn, childOrderId, user.getUserId(), shopPromo, ownerShopDiscount);
                    }
                    if (systemPromo != null && ownerSystemDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        savePromotionUsage(conn, childOrderId, user.getUserId(), systemPromo, ownerSystemDiscount);
                    }
                }

                orderDAO.updatePlatformFee(conn, parentOrderId, totalPlatformFee);
                cartDAO.deleteItemsByCustomer(conn, user.getUserId(), request.getVariantIds());
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        sendShopPreparationNotifications(childOrderIdByOwner);
        initPaymentIfNeeded(parentOrderId, request.getPaymentMethod(), remoteAddress);

        // Gửi thông báo đặt hàng cho Customer
        try {
            String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + parentOrderId;
            String msg = "Đơn hàng tổng #" + parentOrderId + " đã đặt thành công và được tự động tách theo từng shop.";
            notificationService.send(user.getUserId(), AppConfig.NOTIF_ORDER_UPDATE, "Đặt hàng thành công", msg, "/orders/detail?orderId=" + parentOrderId);
            emailService.sendOrderNotificationEmail(user.getEmail(), user.getFullName(), String.valueOf(parentOrderId), "Đặt hàng thành công", orderDetailUrl);
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo đặt hàng cho customerId=" + user.getUserId(), ex);
        }

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(parentOrderId);
        result.setPaymentRequired(AppConfig.PAYMENT_CK.equals(request.getPaymentMethod()));
        result.setSuccessMessage("Đặt hàng thành công! Đơn hàng đã được tách theo từng shop.");
        return result;
    }

    private void validateRequest(User user, CheckoutRequestDTO request) {
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            throw new SecurityException("Bạn không có quyền thực hiện thanh toán.");
        }
        if (request.getFullName() == null || request.getFullName().trim().length() < 3) {
            throw new IllegalArgumentException("Họ và tên người nhận phải từ 3 ký tự trở lên.");
        }
        if (request.getPhone() == null || !request.getPhone().trim().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải là số điện thoại Việt Nam gồm 10 chữ số).");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().length() < 5) {
            throw new IllegalArgumentException("Địa chỉ giao hàng chi tiết phải từ 5 ký tự trở lên.");
        }
        if (request.getDeliveryTimeSlot() == null || request.getDeliveryTimeSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khung giờ giao hàng.");
        }
        if (request.getVariantIds() == null || request.getVariantIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
        }
        if (!AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())
                && !AppConfig.PAYMENT_CK.equals(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
    }

    private List<CartItem> filterCheckoutItems(List<CartItem> items, List<Integer> variantIds, boolean failWhenEmpty) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống hoặc đơn hàng đang được xử lý.");
        }
        if (variantIds == null || variantIds.isEmpty()) {
            if (failWhenEmpty) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
            }
            return null;
        }

        Set<Integer> selectedIds = new HashSet<>(variantIds);
        List<CartItem> checkoutItems = new ArrayList<>();
        for (CartItem item : items) {
            if (selectedIds.contains(item.getVariantId())) {
                checkoutItems.add(item);
            }
        }
        if (checkoutItems.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm nào để thanh toán.");
        }
        return checkoutItems;
    }

    private Map<Integer, ProductVariant> loadVariantMap(List<CartItem> checkoutItems) throws SQLException {
        Map<Integer, ProductVariant> variantMap = new HashMap<>();
        for (CartItem item : checkoutItems) {
            int variantId = item.getVariantId();
            if (!variantMap.containsKey(variantId)) {
                ProductVariant variant = productVariantDAO.findById(variantId);
                if (variant != null) {
                    variantMap.put(variantId, variant);
                }
            }
        }
        return variantMap;
    }

    private void validateStock(List<CartItem> checkoutItems, Map<Integer, ProductVariant> variantMap) {
        List<String> stockErrors = new ArrayList<>();
        for (CartItem item : checkoutItems) {
            if (item.getQuantity() <= 0) {
                stockErrors.add("So luong san pham " + item.getProductName() + " khong hop le.");
                continue;
            }
            ProductVariant variant = variantMap.get(item.getVariantId());
            if (variant == null || !variant.getIsActive()) {
                stockErrors.add(item.getProductName() + " đã ngừng kinh doanh hoặc hết hàng.");
            } else if (item.getQuantity() > variant.getStockQuantity()) {
                stockErrors.add(item.getProductName() + " (" + item.getVariantLabel() + ") vượt quá tồn kho, chỉ còn "
                        + variant.getStockQuantity() + " sản phẩm.");
            }
        }
        if (!stockErrors.isEmpty()) {
            throw new IllegalStateException("Một số sản phẩm không đủ tồn kho: " + String.join(". ", stockErrors));
        }
    }

    private void reserveInventory(Connection conn, List<CartItem> items, int orderId, int userId) throws SQLException {
        for (CartItem item : items) {
            inventoryService.reserve(conn, item.getVariantId(), item.getQuantity(), orderId, userId);
        }
    }

    private void savePromotionUsage(Connection conn, int orderId, int customerId, Promotion promo, BigDecimal discount)
            throws SQLException {
        if (promo == null || promo.getPromoId() <= 0 || discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // Atomic claim: tăng used_count CHỈ KHI còn dưới max_uses — phòng race condition.
        // Gọi trước saveOrderPromotion để rollback sạch nếu mã đã hết lượt.
        boolean claimed = promotionDAO.claimUsage(conn, promo.getPromoId());
        if (!claimed) {
            throw new IllegalStateException(
                "Mã giảm giá [" + promo.getCode() + "] đã hết lượt sử dụng. Đặt hàng bị hủy.");
        }
        promotionDAO.saveOrderPromotion(conn, orderId, promo.getPromoId(), customerId, discount);
    }

    private void initPaymentIfNeeded(int orderId, String paymentMethod, String remoteAddress) throws SQLException {
        if (!AppConfig.PAYMENT_CK.equals(paymentMethod)) {
            return;
        }
        try {
            paymentService.initPayment(orderId, "SEPAY", remoteAddress);
        } catch (SQLException ex) {
            LoggerUtil.warn(log, "Khong tao duoc payment transaction cho orderId=" + orderId, ex);
        }
    }

    private void sendShopPreparationNotifications(Map<Integer, Integer> childOrderIdByOwner) {
        for (Map.Entry<Integer, Integer> entry : childOrderIdByOwner.entrySet()) {
            Integer ownerId = entry.getKey();
            Integer childOrderId = entry.getValue();
            try {
                String shopMsg = "Đơn hàng #" + childOrderId + " đã được tạo từ checkout nhiều shop. Vui lòng kiểm tra và chuẩn bị hàng.";
                notificationService.send(ownerId, AppConfig.NOTIF_ORDER_UPDATE,
                        "Có đơn hàng mới cần chuẩn bị",
                        shopMsg,
                        "/shop/orders");
                User shopOwner = userDAO.findUserById(ownerId);
                if (shopOwner != null) {
                    emailService.sendOrderNotificationEmail(
                        shopOwner.getEmail(),
                        shopOwner.getFullName(),
                        String.valueOf(childOrderId),
                        "Có đơn hàng mới cần chuẩn bị",
                        AppConfig.APP_BASE_URL + "/shop/orders"
                    );
                }
            } catch (Exception ex) {
                LoggerUtil.warn(log, "Khong gui duoc thong bao chuan bi hang cho ownerId=" + ownerId, ex);
            }
        }
    }

    private Order buildOrder(User user,
                             Integer ownerId,
                             Integer parentOrderId,
                             String orderType,
                             CheckoutRequestDTO request,
                             String status,
                             BigDecimal subtotal,
                             BigDecimal deliveryFee,
                             BigDecimal discountAmount,
                             BigDecimal systemDiscountAmount,
                             BigDecimal shopDiscountAmount,
                             BigDecimal platformFee,
                             BigDecimal finalAmount) {
        Order order = new Order();
        order.setCustomerId(user.getUserId());
        order.setOwnerId(ownerId);
        order.setParentOrderId(parentOrderId);
        order.setOrderType(orderType);
        order.setDeliveryAddress(request.getDeliveryAddress().trim());
        order.setRecipientName(request.getFullName().trim());
        order.setRecipientPhone(request.getPhone().trim());
        order.setDeliveryTimeSlot(request.getDeliveryTimeSlot().trim());
        order.setStatus(status);
        order.setTotalAmount(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(discountAmount);
        order.setSystemDiscountAmount(systemDiscountAmount);
        order.setShopDiscountAmount(shopDiscountAmount);
        order.setPlatformFee(platformFee);
        order.setFinalAmount(finalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(normalizeNullable(request.getNotes()));
        order.setRefundStatus("NONE");
        return order;
    }

    private String resolveInitialStatus(String paymentMethod) {
        return AppConfig.PAYMENT_COD.equals(paymentMethod)
                ? AppConfig.ORDER_CONFIRMED
                : AppConfig.ORDER_PENDING_PAYMENT;
    }

    private int resolveSingleShopOwnerId(List<CartItem> checkoutItems, Map<Integer, ProductVariant> variantMap)
            throws SQLException {
        Integer resolvedOwnerId = null;
        for (CartItem item : checkoutItems) {
            int productId = item.getProductId();
            if (productId <= 0 && variantMap != null) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                if (variant != null) {
                    productId = variant.getProductId();
                }
            }
            if (productId <= 0) {
                throw new IllegalStateException("Không thể xác định shop của sản phẩm trong giỏ hàng.");
            }
            int itemOwnerId = orderDAO.getOwnerIdByProductId(productId);
            if (itemOwnerId <= 0) {
                throw new IllegalStateException("Không tìm thấy owner_id cho product_id=" + productId);
            }
            if (resolvedOwnerId == null) {
                resolvedOwnerId = itemOwnerId;
            } else if (resolvedOwnerId.intValue() != itemOwnerId) {
                throw new IllegalArgumentException("Giỏ hàng chỉ hỗ trợ thanh toán cho một shop mỗi lần.");
            }
        }
        return resolvedOwnerId != null ? resolvedOwnerId : -1;
    }

    private Map<Integer, List<CartItem>> groupItemsByOwnerId(List<CartItem> checkoutItems,
                                                             Map<Integer, ProductVariant> variantMap) throws SQLException {
        Map<Integer, List<CartItem>> grouped = new LinkedHashMap<>();
        for (CartItem item : checkoutItems) {
            int productId = item.getProductId();
            if (productId <= 0 && variantMap != null) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                if (variant != null) {
                    productId = variant.getProductId();
                }
            }
            if (productId <= 0) {
                throw new IllegalStateException("Không thể xác định shop của sản phẩm trong giỏ hàng.");
            }
            int ownerId = orderDAO.getOwnerIdByProductId(productId);
            if (ownerId <= 0) {
                throw new IllegalStateException("Không tìm thấy owner_id cho product_id=" + ownerId);
            }
            grouped.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    private BigDecimal calculateSubtotal(List<CartItem> items, Map<Integer, ProductVariant> variantMap) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            ProductVariant variant = variantMap != null ? variantMap.get(item.getVariantId()) : null;
            BigDecimal price = variant != null ? variant.getActivePrice()
                    : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            BigDecimal packagingPriceAdd = item.getPackagingPriceAdd() != null
                    ? item.getPackagingPriceAdd() : BigDecimal.ZERO;
            subtotal = subtotal.add(price.add(packagingPriceAdd).multiply(new BigDecimal(item.getQuantity())));
        }
        return subtotal.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalWeight(List<CartItem> items) {
        long accumulativeGrams = 0;
        for (CartItem item : items) {
            BigDecimal weight = item.getWeightKg() != null ? item.getWeightKg() : new BigDecimal("1.000");
            long weightGrams = weight.multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP).longValue();
            accumulativeGrams += weightGrams * item.getQuantity();
        }
        return new BigDecimal(accumulativeGrams).divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal sumValues(Map<Integer, BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values.values()) {
            if (value != null) {
                sum = sum.add(value);
            }
        }
        return sum;
    }

    private Map<Integer, BigDecimal> allocateDiscount(BigDecimal discount, Map<Integer, BigDecimal> allocationBase) {
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal baseTotal = sumValues(allocationBase);
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0 || baseTotal.compareTo(BigDecimal.ZERO) <= 0) {
            for (Integer ownerId : allocationBase.keySet()) {
                result.put(ownerId, BigDecimal.ZERO);
            }
            return result;
        }

        BigDecimal remaining = discount;
        int index = 0;
        int size = allocationBase.size();
        for (Map.Entry<Integer, BigDecimal> entry : allocationBase.entrySet()) {
            index++;
            BigDecimal allocated;
            if (index == size) {
                allocated = remaining;
            } else {
                allocated = discount.multiply(entry.getValue())
                        .divide(baseTotal, 0, RoundingMode.HALF_UP)
                        .min(entry.getValue())
                        .max(BigDecimal.ZERO);
                remaining = remaining.subtract(allocated);
            }
            result.put(entry.getKey(), allocated.max(BigDecimal.ZERO));
        }
        return result;
    }

    private BigDecimal calculateDirectSaleAmount(List<CartItem> items) {
        long accumulativeDiscount = 0;
        for (CartItem item : items) {
            BigDecimal basePrice = item.getBasePrice() != null ? item.getBasePrice()
                    : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            BigDecimal activePrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal unitDiscount = basePrice.subtract(activePrice).max(BigDecimal.ZERO);
            long unitDiscountValue = unitDiscount.setScale(0, RoundingMode.HALF_UP).longValue();
            accumulativeDiscount += unitDiscountValue * item.getQuantity();
        }
        return new BigDecimal(accumulativeDiscount);
    }

    private String buildPurgedVariantIds(List<CartItem> checkoutItems) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < checkoutItems.size(); i++) {
            builder.append(checkoutItems.get(i).getVariantId());
            if (i < checkoutItems.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
