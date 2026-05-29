package com.fruitmkt.servlet.customer;

import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.CartService;
import com.fruitmkt.model.dto.CartSummaryDTO;
import com.fruitmkt.model.entity.CartItem;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.ProductVariantDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

        // Kiểm tra phân quyền: Chỉ CUSTOMER mới được quyền checkout
        if (!com.fruitmkt.config.AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
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
                        if (order.getCustomerId() == user.getUserId() && "CK".equals(order.getPaymentMethod())) {
                            req.setAttribute("order", order);
                            String bankId = "MB";
                            String accountNo = "0999999999";
                            String accountName = "CONG TY METAFRUIT PREMIUM";
                            String description = "MF" + orderId;
                            String amountFormatted = order.getFinalAmount().setScale(0, java.math.RoundingMode.HALF_UP).toString();
                            String qrUrl = "https://img.vietqr.io/image/" + bankId + "-" + accountNo + "-compact2.png"
                                    + "?amount=" + amountFormatted
                                    + "&addInfo=" + java.net.URLEncoder.encode(description, "UTF-8")
                                    + "&accountName=" + java.net.URLEncoder.encode(accountName, "UTF-8");
                            req.setAttribute("qrUrl", qrUrl);
                            req.setAttribute("bankId", bankId);
                            req.setAttribute("accountNo", accountNo);
                            req.setAttribute("accountName", accountName);
                            req.setAttribute("description", description);
                            req.setAttribute("amountFormatted", amountFormatted);
                            
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

        // Kiểm tra phân quyền: Chỉ CUSTOMER mới được quyền checkout
        if (!com.fruitmkt.config.AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
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

        String deliveryAddress = req.getParameter("deliveryAddress");
        String paymentMethod = req.getParameter("paymentMethod");
        String notes = req.getParameter("notes");
        String variantIdsParam = req.getParameter("variantIds");

        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            SessionUtil.flashError(session, "Vui lòng nhập địa chỉ nhận hàng.");
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
            return;
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "COD";
        }

        try {
            CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
            List<CartItem> items = cartSummary.getItems();
            if (items.isEmpty()) {
                SessionUtil.flashError(session, "Giỏ hàng trống.");
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
                ProductVariant variant = variantMap.get(item.getVariantId());
                if (variant == null || !variant.getIsActive()) {
                    stockErrors.add(item.getProductName() + " đã ngừng kinh doanh.");
                } else if (item.getQuantity() > variant.getStockQuantity()) {
                    stockErrors.add(item.getProductName() + " (" + item.getVariantLabel() + ") chỉ còn " + variant.getStockQuantity() + " sản phẩm.");
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

            int orderId = 0;
            Connection conn = null;
            try {
                conn = orderDAO.openConnection();
                conn.setAutoCommit(false);

                Order order = new Order();
                order.setCustomerId(user.getUserId());
                order.setOwnerId(ownerId);
                order.setDeliveryAddress(deliveryAddress.trim());
                order.setUserAddress(user.getUserAddress() != null ? user.getUserAddress() : deliveryAddress.trim());
                order.setStatus("CONFIRMED");
                order.setTotalAmount(subtotal);
                order.setDeliveryFee(deliveryFee);
                order.setDiscountAmount(BigDecimal.ZERO);
                order.setSystemDiscountAmount(BigDecimal.ZERO);
                order.setShopDiscountAmount(BigDecimal.ZERO);
                order.setPlatformFee(BigDecimal.ZERO);
                order.setFinalAmount(finalAmount);
                order.setPaymentMethod(paymentMethod);
                order.setNotes(notes);

                orderId = saveOrderWithConn(conn, order);

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

            // [FIX] Lưu purgedVariantIds vào session thay vì URL
            StringBuilder purgedSb = new StringBuilder();
            for (int i = 0; i < checkoutItems.size(); i++) {
                purgedSb.append(checkoutItems.get(i).getVariantId());
                if (i < checkoutItems.size() - 1) purgedSb.append(",");
            }
            session.setAttribute("_purgedVariantIds", purgedSb.toString());

            SessionUtil.flashSuccess(session, "Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
            resp.sendRedirect(req.getContextPath() + "/checkout?action=success&orderId=" + orderId);

        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Đã xảy ra lỗi trong quá trình đặt hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/checkout?variantIds=" + (variantIdsParam != null ? variantIdsParam : ""));
        }
    }

    private int saveOrderWithConn(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, delivery_address, user_address, notes, status, total_amount, delivery_fee, discount_amount, system_discount_amount, shop_discount_amount, platform_fee, final_amount, payment_method, refund_status, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NONE', GETDATE(), GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setInt(2, order.getOwnerId());
            ps.setString(3, order.getDeliveryAddress());
            ps.setString(4, order.getUserAddress());
            ps.setString(5, order.getNotes());
            ps.setString(6, order.getStatus());
            ps.setBigDecimal(7, order.getTotalAmount());
            ps.setBigDecimal(8, order.getDeliveryFee());
            ps.setBigDecimal(9, order.getDiscountAmount());
            ps.setBigDecimal(10, order.getSystemDiscountAmount());
            ps.setBigDecimal(11, order.getShopDiscountAmount());
            ps.setBigDecimal(12, order.getPlatformFee());
            ps.setBigDecimal(13, order.getFinalAmount());
            ps.setString(14, order.getPaymentMethod());
            
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
