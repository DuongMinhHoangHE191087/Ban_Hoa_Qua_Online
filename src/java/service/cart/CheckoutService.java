package service.cart;

import config.AppConfig;
import dao.auth.UserAddressDAO;
import dao.auth.UserDAO;
import dao.cart.CartDAO;
import dao.order.OrderDAO;
import dao.order.OrderItemDAO;
import dao.system.SystemConfigDAO;
import exception.BusinessException;
import model.dto.checkout.CheckoutQuoteDTO;
import model.dto.checkout.CheckoutQuoteRequestDTO;
import model.dto.checkout.CheckoutRequestDTO;
import model.dto.checkout.CheckoutResultDTO;
import model.dto.checkout.CheckoutShopSummaryDTO;
import model.dto.checkout.CheckoutViewData;
import model.entity.Promotion;
import model.entity.auth.User;
import model.entity.cart.CartItem;
import model.entity.catalog.ProductVariant;
import model.entity.order.Order;
import service.auth.UserService;
import service.catalog.InventoryService;
import service.chat.NotificationService;
import service.order.OrderService;
import service.shop.PaymentService;
import service.shop.PromotionService;
import service.system.EmailService;
import util.LoggerUtil;
import util.UserLockManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Business logic cho luong checkout.
 */
public class CheckoutService {

    private static final Logger log = LoggerUtil.getLogger(CheckoutService.class);

    private static final String PHONE_REGEX = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$";
    private static final BigDecimal COD_MAX_AMOUNT = new BigDecimal("2000000");
    private static final BigDecimal MAX_CART_WEIGHT_KG = new BigDecimal("30");

    private final OrderService orderService = new OrderService();
    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final PromotionService promotionService = new PromotionService();
    private final dao.shop.PromotionDAO promotionDAO = new dao.shop.PromotionDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final PaymentService paymentService = new PaymentService();
    private final NotificationService notificationService = new NotificationService();
    private final UserService userService = new UserService();
    private final InventoryService inventoryService = new InventoryService();
    private final UserAddressDAO userAddressDAO = new UserAddressDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();
    private final CheckoutPricingEngine pricingEngine = new CheckoutPricingEngine();

    public CheckoutViewData buildCheckoutView(User user, List<Integer> requestedVariantIds) throws SQLException {
        CheckoutPricingEngine.CheckoutPricingSnapshot snapshot = pricingEngine.buildSelectionQuote(user, requestedVariantIds);
        CheckoutQuoteDTO quote = snapshot.getQuote();

        CheckoutViewData viewData = new CheckoutViewData();
        viewData.setCartSummary(snapshot.getCartSummary());
        viewData.setUserAddresses(userAddressDAO.findByUser(user.getUserId()));
        viewData.setShopCount(quote.getShopCount());
        viewData.setDirectSaleAmount(quote.getDirectSaleAmount());
        viewData.setShopSummaries(quote.getShopSummaries());
        viewData.setQuote(quote);
        if (snapshot.getSingleOwnerId() != null) {
            viewData.setShopOwnerId(snapshot.getSingleOwnerId());
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
            CheckoutQuoteRequestDTO quoteRequest = toQuoteRequest(request);
            CheckoutPricingEngine.CheckoutPricingSnapshot snapshot = pricingEngine.buildQuote(user, quoteRequest, true);
            CheckoutQuoteDTO quote = snapshot.getQuote();
            requireValidQuote(quote);
            BigDecimal finalAmount = quote.getFinalAmount() != null ? quote.getFinalAmount() : BigDecimal.ZERO;
            boolean paymentRequired = requiresPayment(request.getPaymentMethod(), finalAmount);

            if (snapshot.getCartSummary().getTotalWeight().compareTo(MAX_CART_WEIGHT_KG) > 0) {
                throw new BusinessException("DEL-02",
                        "Đơn giao hỏa tốc tối đa 30kg. Tổng trọng lượng giỏ hàng của bạn là "
                                + snapshot.getCartSummary().getTotalWeight().setScale(1, RoundingMode.HALF_UP) + " kg.");
            }

            if (AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())) {
                if (finalAmount.compareTo(COD_MAX_AMOUNT) >= 0) {
                    throw new BusinessException("PAY-01",
                            "COD chỉ áp dụng cho đơn dưới 2.000.000đ. Vui lòng chọn chuyển khoản.");
                }
                if (!orderService.isCodEligible(user.getUserId())) {
                    throw new BusinessException("SEC-01",
                            "Tài khoản của bạn tạm thời không được sử dụng COD do có quá nhiều đơn giao thất bại. Vui lòng chọn chuyển khoản.");
                }
            }

            for (Integer ownerId : snapshot.getItemsByOwner().keySet()) {
                if (ownerId != null && ownerId == user.getUserId()) {
                    throw new IllegalArgumentException("Bạn không thể mua hàng từ cửa hàng của chính mình.");
                }
            }

            CheckoutResultDTO result = snapshot.getItemsByOwner().size() > 1
                    ? placeMultiShopOrder(user, request, snapshot, remoteAddress, paymentRequired)
                    : placeSingleShopOrder(user, request, snapshot, remoteAddress, paymentRequired);

            result.setPurgedVariantIds(buildPurgedVariantIds(snapshot.getCheckoutItems()));
            return result;
        } finally {
            lock.unlock();
            UserLockManager.cleanUp(user.getUserId());
        }
    }

    private CheckoutResultDTO placeSingleShopOrder(User user,
                                                   CheckoutRequestDTO request,
                                                   CheckoutPricingEngine.CheckoutPricingSnapshot snapshot,
                                                   String remoteAddress,
                                                   boolean paymentRequired) throws SQLException {
        Integer ownerId = snapshot.getSingleOwnerId();
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalStateException("Không thể xác định shop của đơn hàng.");
        }

        CheckoutShopSummaryDTO summary = snapshot.getQuote().getShopSummaries().get(0);
        BigDecimal platformFeeRate = BigDecimal.valueOf(
                configDAO.getDouble(AppConfig.CONFIG_PLATFORM_FEE_RATE, AppConfig.PLATFORM_FEE_RATE_DEFAULT / 100.0)
        );
        BigDecimal merchandiseDiscount = summary.getShopMerchandiseDiscountAmount()
                .add(summary.getSystemMerchandiseDiscountAmount());
        BigDecimal netMerchandiseAmount = summary.getSubtotal().subtract(merchandiseDiscount).max(BigDecimal.ZERO);
        BigDecimal platformFee = netMerchandiseAmount.multiply(platformFeeRate).setScale(0, RoundingMode.HALF_UP);
 
        int orderId;
        try (Connection conn = orderDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                Order order = buildOrder(user, ownerId, null, AppConfig.ORDER_TYPE_CHILD, request,
                        resolveInitialStatus(paymentRequired),
                        summary.getSubtotal(),
                        summary.getDeliveryFee(),
                        summary.getDiscountAmount(),
                        summary.getSystemMerchandiseDiscountAmount().add(summary.getSystemShippingDiscountAmount()),
                        summary.getShopMerchandiseDiscountAmount().add(summary.getShopShippingDiscountAmount()),
                        platformFee,
                        summary.getFinalAmount());
                orderId = orderDAO.save(conn, order);

                orderItemDAO.saveBatch(conn, orderId, snapshot.getCheckoutItems(), snapshot.getVariantMap());
                reserveInventory(conn, snapshot.getCheckoutItems(), orderId, user.getUserId());

                for (CheckoutPricingEngine.PromotionAllocation allocation : snapshot.getPromotionAllocations()) {
                    savePromotionUsage(conn, orderId, user.getUserId(),
                            allocation.getPromo(), allocation.getDiscountAmount());
                }

                cartDAO.deleteItemsByCustomer(conn, user.getUserId(), request.getVariantIds());
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        initPaymentIfNeeded(orderId, paymentRequired, remoteAddress);
        notifyCustomerOrderCreated(user, orderId, false);
        if (!paymentRequired || AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())) {
            notifySingleShopOrderReady(ownerId, orderId);
        }

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(orderId);
        result.setPaymentRequired(paymentRequired);
        result.setSuccessMessage("Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
        return result;
    }

    private CheckoutResultDTO placeMultiShopOrder(User user,
                                                  CheckoutRequestDTO request,
                                                  CheckoutPricingEngine.CheckoutPricingSnapshot snapshot,
                                                  String remoteAddress,
                                                  boolean paymentRequired) throws SQLException {
        CheckoutQuoteDTO quote = snapshot.getQuote();
        BigDecimal platformFeeRate = BigDecimal.valueOf(
                configDAO.getDouble(AppConfig.CONFIG_PLATFORM_FEE_RATE, AppConfig.PLATFORM_FEE_RATE_DEFAULT / 100.0)
        );
        String status = resolveInitialStatus(paymentRequired);

        int parentOrderId;
        Map<Integer, Integer> childOrderIdByOwner = new java.util.LinkedHashMap<>();
        BigDecimal totalPlatformFee = BigDecimal.ZERO;
        try (Connection conn = orderDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                Order parentOrder = buildOrder(user, null, null, AppConfig.ORDER_TYPE_PARENT, request,
                        status,
                        quote.getSubtotal(),
                        quote.getDeliveryFee(),
                        quote.getDiscountAmount(),
                        quote.getSystemDiscountAmount(),
                        quote.getShopDiscountAmount(),
                        BigDecimal.ZERO,
                        quote.getFinalAmount());
                parentOrderId = orderDAO.save(conn, parentOrder);

                for (CheckoutShopSummaryDTO summary : quote.getShopSummaries()) {
                    BigDecimal merchandiseDiscount = summary.getShopMerchandiseDiscountAmount()
                            .add(summary.getSystemMerchandiseDiscountAmount());
                    BigDecimal netMerchandise = summary.getSubtotal().subtract(merchandiseDiscount).max(BigDecimal.ZERO);
                    BigDecimal ownerPlatformFee = netMerchandise.multiply(platformFeeRate)
                            .setScale(0, RoundingMode.HALF_UP);
                    totalPlatformFee = totalPlatformFee.add(ownerPlatformFee);

                    Order childOrder = buildOrder(user, summary.getOwnerId(), parentOrderId, AppConfig.ORDER_TYPE_CHILD, request,
                            status,
                            summary.getSubtotal(),
                            summary.getDeliveryFee(),
                            summary.getDiscountAmount(),
                            summary.getSystemMerchandiseDiscountAmount()
                                    .add(summary.getSystemShippingDiscountAmount())
                                    .add(summary.getPaymentDiscountAmount()),
                            summary.getShopMerchandiseDiscountAmount().add(summary.getShopShippingDiscountAmount()),
                            ownerPlatformFee,
                            summary.getFinalAmount());
                    int childOrderId = orderDAO.save(conn, childOrder);
                    childOrderIdByOwner.put(summary.getOwnerId(), childOrderId);

                    List<CartItem> ownerItems = snapshot.getItemsByOwner().get(summary.getOwnerId());
                    orderItemDAO.saveBatch(conn, childOrderId, ownerItems, snapshot.getVariantMap());
                    reserveInventory(conn, ownerItems, childOrderId, user.getUserId());

                    for (CheckoutPricingEngine.PromotionAllocation allocation : snapshot.getPromotionAllocations()) {
                        if (summary.getOwnerId() == allocation.getOwnerId()) {
                            savePromotionUsage(conn, childOrderId, user.getUserId(),
                                    allocation.getPromo(), allocation.getDiscountAmount());
                        }
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

        initPaymentIfNeeded(parentOrderId, paymentRequired, remoteAddress);
        notifyCustomerOrderCreated(user, parentOrderId, true);
        if (!paymentRequired || AppConfig.PAYMENT_COD.equals(request.getPaymentMethod())) {
            sendShopPreparationNotifications(childOrderIdByOwner);
        }

        CheckoutResultDTO result = new CheckoutResultDTO();
        result.setOrderId(parentOrderId);
        result.setPaymentRequired(paymentRequired);
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

    private CheckoutQuoteRequestDTO toQuoteRequest(CheckoutRequestDTO request) {
        CheckoutQuoteRequestDTO quoteRequest = new CheckoutQuoteRequestDTO();
        quoteRequest.setVariantIds(request.getVariantIds());
        quoteRequest.setDeliveryAddress(request.getDeliveryAddress());
        quoteRequest.setDeliveryTimeSlot(request.getDeliveryTimeSlot());
        quoteRequest.setPaymentMethod(request.getPaymentMethod());
        quoteRequest.setShopCouponCodes(request.getShopCouponCodes());
        quoteRequest.setSystemCouponCodes(request.getSystemCouponCodes());
        return quoteRequest;
    }

    private void requireValidQuote(CheckoutQuoteDTO quote) {
        if (quote == null) {
            throw new IllegalStateException("Không thể tính toán quote checkout.");
        }
        if (quote.getErrors() != null && !quote.getErrors().isEmpty()) {
            throw new BusinessException("CHECKOUT-QUOTE", quote.getErrors().get(0));
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
        boolean claimed = promotionDAO.claimUsage(conn, promo.getPromoId());
        if (!claimed) {
            throw new IllegalStateException(
                    "Mã giảm giá [" + promo.getCode() + "] đã hết lượt sử dụng. Đặt hàng bị hủy.");
        }
        promotionDAO.saveOrderPromotion(conn, orderId, promo.getPromoId(), customerId, discount,
                promo.getCode(), promo.getDiscountScope(), promotionService.resolveBenefitTarget(promo));
    }

    private void initPaymentIfNeeded(int orderId, boolean paymentRequired, String remoteAddress) throws SQLException {
        if (!paymentRequired) {
            return;
        }
        try {
            paymentService.initPayment(orderId, "SEPAY", remoteAddress);
        } catch (SQLException ex) {
            LoggerUtil.warn(log, "Khong tao duoc payment transaction cho orderId=" + orderId, ex);
            throw ex;
        }
    }

    private void sendShopPreparationNotifications(Map<Integer, Integer> childOrderIdByOwner) {
        for (Map.Entry<Integer, Integer> entry : childOrderIdByOwner.entrySet()) {
            notifySingleShopOrderReady(entry.getKey(), entry.getValue());
        }
    }

    private void notifySingleShopOrderReady(int ownerId, int orderId) {
        try {
            String shopMsg = "Đơn hàng #" + orderId + " đã sẵn sàng để shop chuẩn bị.";
            notificationService.send(ownerId, AppConfig.NOTIF_ORDER_UPDATE,
                    "Có đơn hàng mới cần chuẩn bị",
                    shopMsg,
                    "/shop/orders");
            User shopOwner = userDAO.findUserById(ownerId);
            if (shopOwner != null) {
                emailService.sendOrderNotificationEmail(
                        shopOwner.getEmail(),
                        shopOwner.getFullName(),
                        String.valueOf(orderId),
                        "Có đơn hàng mới cần chuẩn bị",
                        AppConfig.APP_BASE_URL + "/shop/orders"
                );
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Khong gui duoc thong bao chuan bi hang cho ownerId=" + ownerId, ex);
        }
    }

    private void notifyCustomerOrderCreated(User user, int orderId, boolean multiShop) {
        try {
            String orderDetailUrl = AppConfig.APP_BASE_URL + "/profile/order-detail?orderId=" + orderId;
            String customerMsg = multiShop
                    ? "Đơn hàng tổng #" + orderId + " đã đặt thành công và được tách theo từng shop."
                    : "Đơn hàng #" + orderId + " của bạn đã được tạo thành công.";
            notificationService.send(user.getUserId(), AppConfig.NOTIF_ORDER_UPDATE,
                    "Đặt hàng thành công", customerMsg, "/profile/order-detail?orderId=" + orderId);
            emailService.sendOrderNotificationEmail(user.getEmail(), user.getFullName(),
                    String.valueOf(orderId), "Đặt hàng thành công", orderDetailUrl);
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo đặt hàng cho customerId=" + user.getUserId(), ex);
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

    private String resolveInitialStatus(boolean paymentRequired) {
        return paymentRequired ? AppConfig.ORDER_PENDING_PAYMENT : AppConfig.ORDER_CONFIRMED;
    }

    private boolean requiresPayment(String paymentMethod, BigDecimal finalAmount) {
        return AppConfig.PAYMENT_CK.equals(paymentMethod)
                && finalAmount != null
                && finalAmount.compareTo(BigDecimal.ZERO) > 0;
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
}
