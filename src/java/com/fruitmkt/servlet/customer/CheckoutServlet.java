package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.CartService;
import com.fruitmkt.service.PaymentService;
import com.fruitmkt.model.dto.CartSummaryDTO;
import com.fruitmkt.model.entity.CartItem;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.PaymentDAO;
import com.fruitmkt.dao.ProductVariantDAO;

import com.fruitmkt.model.entity.UserAddress;
import com.fruitmkt.dao.UserAddressDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CheckoutServlet — Controller cho chức năng: Thanh toán và đặt hàng
 *
 * URL: /checkout
 * GET : Hiển thị form thanh toán hoặc màn hình đặt hàng thành công
 * POST: Tạo đơn hàng, trừ kho, xoá giỏ hàng DB (Transaction-safe)
 *
 * @author fruitmkt-team
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentService paymentService = new PaymentService();
    private final com.fruitmkt.service.PromotionService promotionService = new com.fruitmkt.service.PromotionService();
    private final com.fruitmkt.dao.PromotionDAO promotionDAO = new com.fruitmkt.dao.PromotionDAO();
    private final com.fruitmkt.dao.SystemConfigDAO configDAO = new com.fruitmkt.dao.SystemConfigDAO();


    // SePay / VietQR config
    private static final String BANK_ID       = AppConfig.SEPAY_BANK_ID;
    private static final String ACCOUNT_NO    = AppConfig.SEPAY_ACCOUNT_NO;
    private static final String ACCOUNT_NAME  = AppConfig.SEPAY_ACCOUNT_NAME;
    private static final String REF_PREFIX    = AppConfig.PAYMENT_REF_PREFIX;    // Nội dung CK = MF + orderId
    private static final int    QR_EXPIRE_MIN = AppConfig.QR_EXPIRE_MINUTES;      // QR hết hạn sau 15 phút


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // Kiểm tra phân quyền: Chỉ CUSTOMER và SHOP_OWNER mới được quyền checkout
        if (!com.fruitmkt.config.AppConfig.ROLE_CUSTOMER.equals(user.getRole())
                && !com.fruitmkt.config.AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thanh toán. Chức năng này chỉ dành cho khách hàng.");
            return;
        }

        String action = req.getParameter("action");
        if ("success".equals(action)) {
            req.setAttribute("isSuccess", true);
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr != null) {
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    List<Order> orders = orderDAO.findById(orderId);
                    if (!orders.isEmpty()) {
                        Order order = orders.get(0);
                        if (order.getCustomerId() == user.getUserId()) {
                            req.setAttribute("order", order);
                        }
                    }
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                }
            }
            req.getRequestDispatcher("/WEB-INF/jsp/customer/order-success.jsp").forward(req, resp);
            return;
        }

        if ("payment".equals(action)) {
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr != null) {
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    List<Order> orders = orderDAO.findById(orderId);
                    if (!orders.isEmpty()) {
                        Order order = orders.get(0);
                        if (order.getCustomerId() == user.getUserId() && AppConfig.PAYMENT_CK.equals(order.getPaymentMethod())) {
                            req.setAttribute("order", order);
                            String reference  = REF_PREFIX + orderId;
                            String amountFmt  = order.getFinalAmount().setScale(0, java.math.RoundingMode.HALF_UP).toString();
                            String qrUrl = buildQrUrl(reference, amountFmt);
                            req.setAttribute("qrUrl",         qrUrl);
                            req.setAttribute("bankId",        BANK_ID);
                            req.setAttribute("accountNo",     ACCOUNT_NO);
                            req.setAttribute("accountName",   ACCOUNT_NAME);
                            req.setAttribute("reference",     reference);
                            req.setAttribute("amountFormatted", amountFmt);
                            req.setAttribute("qrExpireMin",   QR_EXPIRE_MIN);
                            // Lấy trạng thái payment_transaction để biết đã confirmPayment chưa
                            try {
                                var txList = paymentDAO.findByOrder(orderId);
                                if (!txList.isEmpty()) req.setAttribute("paymentTx", txList.get(0));
                            } catch (Exception ignored) {}
                            req.getRequestDispatcher("/WEB-INF/jsp/customer/order-payment.jsp").forward(req, resp);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        if ("status".equals(action)) {
            String orderIdStr = req.getParameter("orderId");
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (orderIdStr != null) {
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    List<Order> orders = orderDAO.findById(orderId);
                    if (!orders.isEmpty()) {
                        Order order = orders.get(0);
                        if (order.getCustomerId() == user.getUserId()) {
                            resp.getWriter().write("{\"status\":\"" + order.getStatus() + "\"}");
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resp.getWriter().write("{\"status\":\"UNKNOWN\"}");
            return;
        }

        try {
            CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
            if (cartSummary.getItems().isEmpty()) {
                SessionUtil.flashError(session, "Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }

            String variantIdsParam = req.getParameter("variantIds");
            if (variantIdsParam != null && !variantIdsParam.trim().isEmpty()) {
                java.util.Set<Integer> selectedIds = new java.util.HashSet<>();
                for (String s : variantIdsParam.split(",")) {
                    try {
                        selectedIds.add(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                if (!selectedIds.isEmpty()) {
                    List<CartItem> filtered = new java.util.ArrayList<>();
                    for (CartItem item : cartSummary.getItems()) {
                        if (selectedIds.contains(item.getVariantId())) {
                            filtered.add(item);
                        }
                    }

                    if (filtered.isEmpty()) {
                        SessionUtil.flashError(session, "Không tìm thấy sản phẩm nào được chọn trong giỏ hàng để thanh toán.");
                        resp.sendRedirect(req.getContextPath() + "/cart");
                        return;
                    }

                    long accumulativeSubtotal = 0;
                    long accumulativeGrams = 0;
                    for (CartItem item : filtered) {
                        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                        BigDecimal weight = item.getWeightKg() != null ? item.getWeightKg() : new BigDecimal("1.000");

                        long unitPrice = price.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
                        accumulativeSubtotal += unitPrice * item.getQuantity();

                        long weightGrams = weight.multiply(new BigDecimal("1000")).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
                        accumulativeGrams += weightGrams * item.getQuantity();
                    }

                    BigDecimal subtotal = new BigDecimal(accumulativeSubtotal);
                    BigDecimal totalWeight = new BigDecimal(accumulativeGrams).divide(new BigDecimal("1000"), 3, java.math.RoundingMode.HALF_UP);
                    BigDecimal deliveryFee = new BigDecimal("15000");
                    BigDecimal finalAmount = subtotal.add(deliveryFee);

                    cartSummary.setItems(filtered);
                    cartSummary.setSubtotal(subtotal);
                    cartSummary.setTotalWeight(totalWeight);
                    cartSummary.setDeliveryFee(deliveryFee);
                    cartSummary.setTotal(finalAmount);
                }
            }

            if (cartSummary != null && cartSummary.getItems() != null && !cartSummary.getItems().isEmpty()) {
                int firstProductId = cartSummary.getItems().get(0).getProductId();
                int ownerId = orderDAO.getOwnerIdByProductId(firstProductId);
                req.setAttribute("shopOwnerId", ownerId);
            }

            List<UserAddress> userAddresses = new UserAddressDAO().findByUser(user.getUserId());
            req.setAttribute("userAddresses", userAddresses);
            req.setAttribute("cartSummary", cartSummary);
            req.setAttribute("userAddress", user.getUserAddress());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/checkout.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi khi tải trang thanh toán. Vui lòng thử lại.");
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // Kiểm tra phân quyền: Chỉ CUSTOMER và SHOP_OWNER mới được quyền checkout
        if (!com.fruitmkt.config.AppConfig.ROLE_CUSTOMER.equals(user.getRole())
                && !com.fruitmkt.config.AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thanh toán. Chức năng này chỉ dành cho khách hàng.");
            return;
        }

        // [FIX] Validate CSRF token
        String csrfParam = req.getParameter("_csrf");
        String csrfSession = (String) session.getAttribute("_csrfToken");
        if (csrfSession != null && !csrfSession.equals(csrfParam)) {
            SessionUtil.flashError(session, "Yêu cầu không hợp lệ (CSRF). Vui lòng thử lại.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        // ─── action=confirmPayment : Khách bấm "Tôi đã thanh toán" ──────────────
        String action = req.getParameter("action");
        if ("confirmPayment".equals(action)) {
            String orderIdStr = req.getParameter("orderId");
            try {
                int orderId2 = Integer.parseInt(orderIdStr);
                boolean ok = paymentService.confirmManualPayment(orderId2, user.getUserId());
                if (ok) {
                    SessionUtil.flashSuccess(session,
                        "Chúng tôi đã nhận thông báo thanh toán. Admin sẽ xác minh và duyệt trong 1–24 giờ làm việc.");
                } else {
                    SessionUtil.flashError(session,
                        "Mã QR đã hết hạn. Vui lòng làm mới mã QR và thanh toán lại.");
                }
            } catch (SecurityException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (Exception e) {
                SessionUtil.flashError(session, "Lỗi: " + e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/checkout?action=payment&orderId=" + req.getParameter("orderId"));
            return;
        }
        // ─────────────────────────────────────────────────────────────────────

        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String deliveryAddress = req.getParameter("deliveryAddress");
        String paymentMethod = req.getParameter("paymentMethod");
        String notes = req.getParameter("notes");
        String variantIdsParam = req.getParameter("variantIds");
        String shopCouponCode = req.getParameter("shopCouponCode");
        String systemCouponCode = req.getParameter("systemCouponCode");

        // Validate recipient details carefully
        if (fullName == null || fullName.trim().length() < 3) {
            SessionUtil.flashError(session, "Họ và tên người nhận phải từ 3 ký tự trở lên.");
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
            return;
        }

        if (phone == null || !phone.trim().matches("^(0|\\+84)[3|5|7|8|9][0-9]{8}$")) {
            SessionUtil.flashError(session, "Số điện thoại không hợp lệ (phải là số điện thoại Việt Nam gồm 10 chữ số).");
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
            return;
        }

        if (deliveryAddress == null || deliveryAddress.trim().length() < 5) {
            SessionUtil.flashError(session, "Địa chỉ giao hàng chi tiết phải từ 5 ký tự trở lên.");
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
            return;
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "COD";
        }

        try {
            synchronized (String.valueOf(user.getUserId()).intern()) {
                CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
                List<CartItem> items = cartSummary.getItems();
                if (items.isEmpty()) {
                    SessionUtil.flashError(session, "Giỏ hàng trống hoặc đơn hàng đang được xử lý.");
                    resp.sendRedirect(req.getContextPath() + "/cart");
                    return;
                }

                // [FIX] Bỏ fallback empty→all: bắt buộc phải có variantIds
                java.util.Set<Integer> selectedVariantIds = new java.util.HashSet<>();
                if (variantIdsParam != null && !variantIdsParam.trim().isEmpty()) {
                    for (String part : variantIdsParam.split(",")) {
                        try { selectedVariantIds.add(Integer.parseInt(part.trim())); } catch (NumberFormatException e) { /* ignore */ }
                    }
                }
                if (selectedVariantIds.isEmpty()) {
                    SessionUtil.flashError(session, "Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
                    resp.sendRedirect(req.getContextPath() + "/cart");
                    return;
                }

                List<CartItem> checkoutItems = new java.util.ArrayList<>();
                for (CartItem item : items) {
                    if (selectedVariantIds.contains(item.getVariantId())) {
                        checkoutItems.add(item);
                    }
                }

                if (checkoutItems.isEmpty()) {
                    SessionUtil.flashError(session, "Không tìm thấy sản phẩm nào để thanh toán.");
                    resp.sendRedirect(req.getContextPath() + "/cart");
                    return;
                }

                // [FIX] Load tất cả variants 1 lần, tái dùng cho stock check + tính tiền + insert
                java.util.Map<Integer, ProductVariant> variantMap = new java.util.HashMap<>();
                for (CartItem item : checkoutItems) {
                    int vid = item.getVariantId();
                    if (!variantMap.containsKey(vid)) {
                        ProductVariant pv = productVariantDAO.findById(vid);
                        if (pv != null) variantMap.put(vid, pv);
                    }
                }

                // Kiểm tra tồn kho bằng cache
                List<String> stockErrors = new java.util.ArrayList<>();
                for (CartItem item : checkoutItems) {
                    if (item.getQuantity() <= 0) {
                        stockErrors.add("Số lượng sản phẩm " + item.getProductName() + " không hợp lệ.");
                    } else {
                        ProductVariant variant = variantMap.get(item.getVariantId());
                        if (variant == null || !variant.getIsActive()) {
                            stockErrors.add(item.getProductName() + " đã ngừng kinh doanh.");
                        } else if (item.getQuantity() > variant.getStockQuantity()) {
                            stockErrors.add(item.getProductName() + " (" + item.getVariantLabel() + ") chỉ còn " + variant.getStockQuantity() + " sản phẩm.");
                        }
                    }
                }
                if (!stockErrors.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Một số sản phẩm không đủ tồn kho: ");
                    for (String err : stockErrors) sb.append(err).append(". ");
                    SessionUtil.flashError(session, sb.toString());
                    resp.sendRedirect(req.getContextPath() + "/cart");
                    return;
                }

                // Tính tiền từ cache (không query thêm)
                long accumulativeSubtotal = 0;
                for (CartItem item : checkoutItems) {
                    ProductVariant variant = variantMap.get(item.getVariantId());
                    BigDecimal price = variant != null ? variant.getPrice() : BigDecimal.ZERO;
                    long unitPrice = price.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
                    accumulativeSubtotal += unitPrice * item.getQuantity();
                }

                BigDecimal subtotal = new BigDecimal(accumulativeSubtotal);
                BigDecimal deliveryFee = new BigDecimal("15000");
                BigDecimal finalAmount = subtotal.add(deliveryFee);

                // [FIX] Dùng DAO method trong package để tránh gọi getConnection() protected từ servlet
                ProductVariant firstVariant = variantMap.get(checkoutItems.get(0).getVariantId());
                if (firstVariant == null) {
                    throw new IllegalStateException("Không thể xác định thông tin sản phẩm để tạo đơn hàng.");
                }
                int ownerId = orderDAO.getOwnerIdByProductId(firstVariant.getProductId());
                if (ownerId == -1) {
                    throw new IllegalStateException("Không tìm thấy owner_id cho product_id=" + firstVariant.getProductId());
                }

                // Chống tự mua hàng (Self-Buying Prevention)
                if (user.getUserId() == ownerId) {
                    SessionUtil.flashError(session, "Bạn không thể mua hàng từ cửa hàng của chính mình.");
                    resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
                    return;
                }

                // Coupon Application
                BigDecimal shopDiscount = BigDecimal.ZERO;
                BigDecimal systemDiscount = BigDecimal.ZERO;
                BigDecimal totalDiscount = BigDecimal.ZERO;
                com.fruitmkt.model.entity.Promotion shopPromo = null;
                com.fruitmkt.model.entity.Promotion systemPromo = null;

                try {
                    if (shopCouponCode != null && !shopCouponCode.trim().isEmpty()) {
                        shopPromo = promotionService.validateShopCoupon(shopCouponCode, ownerId, subtotal);
                        if (shopPromo == null) {
                            SessionUtil.flashError(session, "Mã giảm giá của cửa hàng không hợp lệ, đã hết hạn, hoặc chưa đạt giá trị đơn tối thiểu.");
                            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
                            return;
                        }
                    }
                    if (systemCouponCode != null && !systemCouponCode.trim().isEmpty()) {
                        systemPromo = promotionService.validateSystemCoupon(systemCouponCode, subtotal);
                        if (systemPromo == null) {
                            SessionUtil.flashError(session, "Mã giảm giá của sàn không hợp lệ, đã hết hạn, hoặc chưa đạt giá trị đơn tối thiểu.");
                            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
                            return;
                        }
                    }
                    if (shopPromo != null || systemPromo != null) {
                        BigDecimal[] calcs = promotionService.calculateAllDiscounts(shopPromo, systemPromo, subtotal, deliveryFee);
                        shopDiscount = calcs[0];
                        systemDiscount = calcs[1];
                        totalDiscount = calcs[2];
                        finalAmount = calcs[3];
                    }
                } catch (Exception e) {
                    System.err.println("[FruitMkt] ERROR: Lỗi tính toán coupon: " + e.getMessage());
                    SessionUtil.flashError(session, "Lỗi áp dụng mã giảm giá: " + e.getMessage());
                    resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
                    return;
                }

                // Platform fee calculation
                BigDecimal platformFeeRate = new BigDecimal(String.valueOf(configDAO.getDouble("platform_fee_rate", 0.02)));
                BigDecimal platformFee = subtotal.multiply(platformFeeRate).setScale(0, java.math.RoundingMode.HALF_UP);

                int orderId = 0;
                Connection conn = null;
                try {
                    conn = orderDAO.openConnection();
                    conn.setAutoCommit(false);

                    Order order = new Order();
                    order.setCustomerId(user.getUserId());
                    order.setOwnerId(ownerId);
                    String formattedAddress = fullName.trim() + " | SĐT: " + phone.trim() + " | Địa chỉ: " + deliveryAddress.trim();
                    order.setDeliveryAddress(formattedAddress);
                    order.setRecipientName(fullName.trim());
                    order.setRecipientPhone(phone.trim());
                    // [FIX B1] COD bắt đầu ở CONFIRMED (shop cần duyệt), CK bắt đầu ở PENDING_PAYMENT
                    order.setStatus(AppConfig.PAYMENT_COD.equals(paymentMethod)
                            ? AppConfig.ORDER_CONFIRMED
                            : AppConfig.ORDER_PENDING_PAYMENT);
                    order.setTotalAmount(subtotal);
                    order.setDeliveryFee(deliveryFee);
                    order.setDiscountAmount(totalDiscount);
                    order.setSystemDiscountAmount(systemDiscount);
                    order.setShopDiscountAmount(shopDiscount);
                    order.setPlatformFee(platformFee);
                    order.setFinalAmount(finalAmount);
                    order.setPaymentMethod(paymentMethod);
                    order.setNotes(notes);

                    orderId = saveOrderWithConn(conn, order);

                    // Save coupon association if applicable
                    if (shopPromo != null && shopPromo.getPromoId() > 0) {
                        promotionDAO.saveOrderPromotion(conn, orderId, shopPromo.getPromoId(), user.getUserId(), shopDiscount);
                        String updateUsedCountSql = "UPDATE promotions SET used_count = used_count + 1, updated_at = GETDATE() WHERE promo_id = ?";
                        try (PreparedStatement psPromo = conn.prepareStatement(updateUsedCountSql)) {
                            psPromo.setInt(1, shopPromo.getPromoId());
                            psPromo.executeUpdate();
                        }
                    }
                    if (systemPromo != null && systemPromo.getPromoId() > 0) {
                        promotionDAO.saveOrderPromotion(conn, orderId, systemPromo.getPromoId(), user.getUserId(), systemDiscount);
                        String updateUsedCountSql = "UPDATE promotions SET used_count = used_count + 1, updated_at = GETDATE() WHERE promo_id = ?";
                        try (PreparedStatement psPromo = conn.prepareStatement(updateUsedCountSql)) {
                            psPromo.setInt(1, systemPromo.getPromoId());
                            psPromo.executeUpdate();
                        }
                    }


                    String insertItemSql = "INSERT INTO order_items (order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    String updateStockSql = "UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE variant_id = ?";

                    try (PreparedStatement itemPs = conn.prepareStatement(insertItemSql);
                         PreparedStatement stockPs = conn.prepareStatement(updateStockSql)) {
                        for (CartItem item : checkoutItems) {
                            // [FIX] Dùng cache variantMap thay vì query lần 3
                            ProductVariant variant = variantMap.get(item.getVariantId());
                            BigDecimal latestPrice = variant != null ? variant.getPrice() : item.getPrice();

                            itemPs.setInt(1, orderId);
                            itemPs.setInt(2, item.getVariantId());
                            itemPs.setString(3, item.getProductName());
                            itemPs.setString(4, item.getVariantLabel());
                            itemPs.setInt(5, item.getQuantity());
                            itemPs.setBigDecimal(6, latestPrice);
                            itemPs.setBigDecimal(7, latestPrice.multiply(new BigDecimal(item.getQuantity())));
                            itemPs.addBatch();

                            stockPs.setInt(1, item.getQuantity());
                            stockPs.setInt(2, item.getVariantId());
                            stockPs.addBatch();
                        }
                        itemPs.executeBatch();
                        stockPs.executeBatch();
                    }

                    StringBuilder deleteCartSql = new StringBuilder(
                        "DELETE FROM cart_items WHERE cart_id = (SELECT cart_id FROM cart WHERE customer_id = ?) AND variant_id IN (");
                    for (int i = 0; i < checkoutItems.size(); i++) {
                        deleteCartSql.append("?");
                        if (i < checkoutItems.size() - 1) deleteCartSql.append(",");
                    }
                    deleteCartSql.append(")");

                    try (PreparedStatement deletePs = conn.prepareStatement(deleteCartSql.toString())) {
                        deletePs.setInt(1, user.getUserId());
                        for (int i = 0; i < checkoutItems.size(); i++) {
                            deletePs.setInt(2 + i, checkoutItems.get(i).getVariantId());
                        }
                        deletePs.executeUpdate();
                    }

                    conn.commit();
                    System.out.println("[FruitMkt] Order placed. Order ID: " + orderId);
                } catch (Exception ex) {
                    if (conn != null) { try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } }
                    throw ex;
                } finally {
                    if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
                }

                // [FIX B2] Tạo payment_transactions record cho đơn CK (sau commit để tránh rollback)
                if (AppConfig.PAYMENT_CK.equals(paymentMethod)) {
                    try {
                        paymentDAO.initTransaction(
                            orderId,
                            "SEPAY",
                            finalAmount,
                            REF_PREFIX + orderId,
                            req.getRemoteAddr(),
                            LocalDateTime.now().plusMinutes(QR_EXPIRE_MIN)
                        );
                    } catch (SQLException ex) {
                        // Log nhưng không throw — đơn hàng đã tạo thành công, payment record có thể retry
                        System.err.println("[FruitMkt] WARN: Không tạo được payment_transactions cho orderId=" + orderId + ": " + ex.getMessage());
                    }
                }

                // [FIX] Lưu purgedVariantIds vào session thay vì URL
                StringBuilder purgedSb = new StringBuilder();
                for (int i = 0; i < checkoutItems.size(); i++) {
                    purgedSb.append(checkoutItems.get(i).getVariantId());
                    if (i < checkoutItems.size() - 1) purgedSb.append(",");
                }
                session.setAttribute("_purgedVariantIds", purgedSb.toString());

                SessionUtil.flashSuccess(session, "Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
                if (AppConfig.PAYMENT_CK.equals(paymentMethod)) {
                    resp.sendRedirect(req.getContextPath() + "/checkout?action=payment&orderId=" + orderId);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/checkout?action=success&orderId=" + orderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Đã xảy ra lỗi trong quá trình đặt hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
        }
    }

    /** Xây dựng URL QR động VietQR */
    private String buildQrUrl(String reference, String amount) throws java.io.UnsupportedEncodingException {
        return "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-compact2.png"
                + "?amount=" + amount
                + "&addInfo=" + java.net.URLEncoder.encode(reference, "UTF-8")
                + "&accountName=" + java.net.URLEncoder.encode(ACCOUNT_NAME, "UTF-8");
    }

    private int saveOrderWithConn(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, delivery_address, recipient_name, recipient_phone, notes, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, shop_acceptance_deadline, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NONE', ?, GETDATE(), GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setInt(2, order.getOwnerId());
            ps.setString(3, order.getDeliveryAddress());
            ps.setString(4, order.getRecipientName());
            ps.setString(5, order.getRecipientPhone());
            ps.setString(6, order.getNotes());
            ps.setString(7, order.getStatus());
            ps.setBigDecimal(8, order.getTotalAmount());
            ps.setBigDecimal(9, order.getDeliveryFee());
            ps.setBigDecimal(10, order.getDiscountAmount());
            ps.setBigDecimal(11, order.getSystemDiscountAmount());
            ps.setBigDecimal(12, order.getShopDiscountAmount());
            ps.setBigDecimal(13, order.getPlatformFee());
            ps.setBigDecimal(14, order.getFinalAmount());
            ps.setString(15, order.getPaymentMethod());
            if ("CONFIRMED".equals(order.getStatus())) {
                ps.setTimestamp(16, java.sql.Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)));
            } else {
                ps.setNull(16, java.sql.Types.TIMESTAMP);
            }
            
            ps.executeUpdate();
            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Không lấy được mã Order tự tăng.");
    }
}
