package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CartDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.OrderItemDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.dao.UserAddressDAO;
import com.fruitmkt.model.dto.CartSummaryDTO;
import com.fruitmkt.model.dto.CheckoutRequestDTO;
import com.fruitmkt.model.dto.CheckoutResultDTO;
import com.fruitmkt.model.dto.CheckoutViewData;
import com.fruitmkt.model.entity.CartItem;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.UserLockManager;
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

    private final CartService cartService = new CartService();
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

    public CheckoutViewData buildCheckoutView(User user, List<Integer> requestedVariantIds) throws SQLException {
        CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
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
            throw new IllegalArgumentException("Ban khong the mua hang tu cua hang cua chinh minh.");
        }

        Promotion shopPromo = null;
        Promotion systemPromo = null;
        BigDecimal deliveryFee = DELIVERY_FEE_PER_SHOP;
        BigDecimal shopDiscount = BigDecimal.ZERO;
        BigDecimal systemDiscount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.add(deliveryFee);

        if (isNotBlank(request.getShopCouponCode())) {
            shopPromo = promotionService.validateShopCoupon(request.getShopCouponCode(), ownerId, subtotal);
            if (shopPromo == null) {
                throw new IllegalArgumentException("Ma giam gia cua cua hang khong hop le, da het han, hoac chua dat gia tri don toi thieu.");
            }
        }
        if (isNotBlank(request.getSystemCouponCode())) {
            systemPromo = promotionService.validateSystemCoupon(request.getSystemCouponCode(), subtotal);
            if (systemPromo == null) {
                throw new IllegalArgumentException("Ma giam gia cua san khong hop le, da het han, hoac chua dat gia tri don toi thieu.");
            }
        }
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
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        initPaymentIfNeeded(orderId, request.getPaymentMethod(), remoteAddress);

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(orderId);
        result.setPaymentRequired(AppConfig.PAYMENT_CK.equals(request.getPaymentMethod()));
        result.setSuccessMessage("Dat hang thanh cong! Cam on ban da mua hang.");
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
                throw new IllegalArgumentException("Ban khong the mua hang tu cua hang cua chinh minh.");
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
                throw new IllegalArgumentException("Ma voucher shop khong hop le cho cac shop trong gio hang.");
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
                throw new IllegalArgumentException("Ma voucher san khong hop le, da het han, hoac chua dat gia tri don toi thieu.");
            }
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
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        sendShopPreparationNotifications(childOrderIdByOwner);
        initPaymentIfNeeded(parentOrderId, request.getPaymentMethod(), remoteAddress);

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(parentOrderId);
        result.setPaymentRequired(AppConfig.PAYMENT_CK.equals(request.getPaymentMethod()));
        result.setSuccessMessage("Dat hang thanh cong! Don hang da duoc tach theo tung shop.");
        return result;
    }

    private void validateRequest(User user, CheckoutRequestDTO request) {
        if (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
            throw new SecurityException("Ban khong co quyen thuc hien thanh toan.");
        }
        if (request.getFullName() == null || request.getFullName().trim().length() < 3) {
            throw new IllegalArgumentException("Ho va ten nguoi nhan phai tu 3 ky tu tro len.");
        }
        if (request.getPhone() == null || !request.getPhone().trim().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("So dien thoai khong hop le (phai la so dien thoai Viet Nam gom 10 chu so).");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().length() < 5) {
            throw new IllegalArgumentException("Dia chi giao hang chi tiet phai tu 5 ky tu tro len.");
        }
        if (request.getDeliveryTimeSlot() == null || request.getDeliveryTimeSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui long chon khung gio giao hang.");
        }
        if (request.getVariantIds() == null || request.getVariantIds().isEmpty()) {
            throw new IllegalArgumentException("Vui long chon it nhat mot san pham de thanh toan.");
        }
        if (!AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())
                && !AppConfig.PAYMENT_CK.equals(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Phuong thuc thanh toan khong hop le.");
        }
    }

    private List<CartItem> filterCheckoutItems(List<CartItem> items, List<Integer> variantIds, boolean failWhenEmpty) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Gio hang trong hoac don hang dang duoc xu ly.");
        }
        if (variantIds == null || variantIds.isEmpty()) {
            if (failWhenEmpty) {
                throw new IllegalArgumentException("Vui long chon it nhat mot san pham de thanh toan.");
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
            throw new IllegalArgumentException("Khong tim thay san pham nao de thanh toan.");
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
                stockErrors.add(item.getProductName() + " da ngung kinh doanh.");
            } else if (item.getQuantity() > variant.getStockQuantity()) {
                stockErrors.add(item.getProductName() + " (" + item.getVariantLabel() + ") chi con "
                        + variant.getStockQuantity() + " san pham.");
            }
        }
        if (!stockErrors.isEmpty()) {
            throw new IllegalStateException("Mot so san pham khong du ton kho: " + String.join(". ", stockErrors));
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
        promotionDAO.saveOrderPromotion(conn, orderId, promo.getPromoId(), customerId, discount);
        promotionDAO.incrementUsedCount(conn, promo.getPromoId());
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
            try {
                notificationService.send(entry.getKey(), AppConfig.NOTIF_ORDER_UPDATE,
                        "Có đơn hàng cần chuẩn bị",
                        "Đơn hàng #" + entry.getValue() + " đã được tạo từ checkout nhiều shop. Vui lòng kiểm tra và chuẩn bị hàng.",
                        "/shop/orders");
            } catch (Exception ex) {
                LoggerUtil.warn(log, "Khong gui duoc thong bao chuan bi hang cho ownerId=" + entry.getKey(), ex);
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
                throw new IllegalStateException("Khong the xac dinh shop cua san pham trong gio hang.");
            }
            int itemOwnerId = orderDAO.getOwnerIdByProductId(productId);
            if (itemOwnerId <= 0) {
                throw new IllegalStateException("Khong tim thay owner_id cho product_id=" + productId);
            }
            if (resolvedOwnerId == null) {
                resolvedOwnerId = itemOwnerId;
            } else if (resolvedOwnerId.intValue() != itemOwnerId) {
                throw new IllegalArgumentException("Gio hang chi ho tro thanh toan cho mot shop moi lan.");
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
                throw new IllegalStateException("Khong the xac dinh shop cua san pham trong gio hang.");
            }
            int ownerId = orderDAO.getOwnerIdByProductId(productId);
            if (ownerId <= 0) {
                throw new IllegalStateException("Khong tim thay owner_id cho product_id=" + productId);
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
