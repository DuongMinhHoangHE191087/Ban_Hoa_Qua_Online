package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import java.sql.SQLException;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.service.EmailService;

/**
 * OrderService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * OrderService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class OrderService {

    private final com.fruitmkt.dao.OrderDAO orderDAO = new com.fruitmkt.dao.OrderDAO();
    private final com.fruitmkt.dao.SystemConfigDAO configDAO = new com.fruitmkt.dao.SystemConfigDAO();
    private final com.fruitmkt.dao.PaymentDAO paymentDAO = new com.fruitmkt.dao.PaymentDAO();
    private final com.fruitmkt.service.NotificationService notificationService = new com.fruitmkt.service.NotificationService();
    private final com.fruitmkt.service.InventoryService inventoryService = new com.fruitmkt.service.InventoryService();

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Order placeOrder(int customerId, com.fruitmkt.model.dto.CheckoutDTO dto) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: placeOrder(int customerId, com.fruitmkt.model.dto.CheckoutDTO dto)");
    }

    public java.util.List<com.fruitmkt.model.entity.Order> getAllOrders(String status, int page, int pageSize) throws SQLException {
        return orderDAO.findAll(status, page, pageSize);
    }

    public java.util.List<com.fruitmkt.model.entity.Order> getAllOrders(String status, String paymentMethod, String paymentStatus, int page, int pageSize) throws SQLException {
        return orderDAO.findAll(status, paymentMethod, paymentStatus, page, pageSize);
    }

    public int countAllOrders(String status) throws SQLException {
        return orderDAO.countAll(status);
    }

    public int countAllOrders(String status, String paymentMethod, String paymentStatus) throws SQLException {
        return orderDAO.countAll(status, paymentMethod, paymentStatus);
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Order getOrderDetail(int orderId) throws SQLException {
        java.util.List<com.fruitmkt.model.entity.Order> list = orderDAO.findById(orderId);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.dto.PagedResultDTO getOrderHistory(int customerId, int page) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getOrderHistory(int customerId, int page)");
    }

    /**
     * Cập nhật đơn hàng sang CONFIRMED sau khi shop owner duyệt.
     */
    public void confirmOrder(int orderId, int ownerId) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null || order.getOwnerId() != ownerId) {
            throw new RuntimeException("Đơn hàng không hợp lệ hoặc bạn không có quyền duyệt!");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể duyệt đơn hàng ở trạng thái PENDING hoặc CONFIRMED");
        }
        orderDAO.updateStatus(orderId, "CONFIRMED");
    }

    /**
     * Hủy đơn hàng và hoàn trả tồn kho.
     */
    public void cancelOrder(int orderId, int cancelledBy, String reason) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null) {
            throw new RuntimeException("Đơn hàng không tồn tại!");
        }
        if ("DELIVERED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã giao hoặc đã hủy, không thể hủy thêm!");
        }

        // Kiểm tra quyền hủy đơn hàng (Privilege Escalation Prevention)
        com.fruitmkt.dao.UserDAO userDAO = new com.fruitmkt.dao.UserDAO();
        com.fruitmkt.model.entity.User user = userDAO.findUserById(cancelledBy);
        if (user != null) {
            if (com.fruitmkt.config.AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
                if (order.getCustomerId() != cancelledBy) {
                    throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
                }
                if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
                    throw new RuntimeException("Cửa hàng đã duyệt hoặc đang giao đơn, không thể tự ý hủy!");
                }
            } else if (com.fruitmkt.config.AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
                if (order.getOwnerId() != cancelledBy) {
                    throw new RuntimeException("Đơn hàng này không thuộc cửa hàng của bạn!");
                }
            }
        }
        
        // Cập nhật DB trạng thái CANCELLED
        orderDAO.cancel(orderId, cancelledBy, reason);
        // Hoàn trả tồn kho thông qua InventoryService để ghi nhận nhật ký tồn kho
        java.util.List<com.fruitmkt.model.entity.OrderItem> items = orderDAO.findItemsByOrderId(orderId);
        for (com.fruitmkt.model.entity.OrderItem item : items) {
            if (item.getVariantId() != null) {
                inventoryService.release(item.getVariantId(), item.getQuantity(), orderId);
            }
        }
    }

    /**
     * Lấy danh sách đơn hàng cho Shop
     */
    public com.fruitmkt.model.dto.PagedResultDTO shopOrders(int ownerId, String status, int page) throws SQLException {
        int pageSize = 10;
        java.util.List<com.fruitmkt.model.entity.Order> list = orderDAO.findByOwner(ownerId, status, page, pageSize);
        // Chưa đếm tổng số trang, tạm trả về list
        com.fruitmkt.model.dto.PagedResultDTO dto = new com.fruitmkt.model.dto.PagedResultDTO();
        dto.setItems(list);
        dto.setCurrentPage(page);
        return dto;
    }

    /**
     * Chuyển trạng thái sang DISPATCHED và có thể gọi DeliveryService để tạo bản ghi phân công.
     */
    public void dispatchOrder(int orderId, int ownerId) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null || order.getOwnerId() != ownerId) {
            throw new RuntimeException("Đơn hàng không hợp lệ!");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"PREPARING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể giao đơn đang được chuẩn bị hoặc đã duyệt!");
        }
        orderDAO.updateStatus(orderId, "DISPATCHED");

        try {
            UserDAO userDAO = new UserDAO();
            User customer = userDAO.findUserById(order.getCustomerId());
            if (customer != null) {
                String customerMsg = "Đơn hàng #" + orderId + " của bạn đang được giao.";
                notificationService.send(customer.getUserId(), AppConfig.NOTIF_ORDER_UPDATE, "Đơn hàng đang giao", customerMsg, "/orders/detail?orderId=" + orderId);
                
                EmailService emailService = new EmailService();
                String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + orderId;
                emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(orderId), "Đơn hàng đang giao (DISPATCHED)", orderDetailUrl);
            }
        } catch (Exception ex) {
            System.err.println("[OrderService] WARN: Không gửi được thông báo dispatch cho orderId=" + orderId + ": " + ex.getMessage());
        }
    }

    /**
     * Khách hàng xác nhận đã nhận hàng
     */
    public void customerConfirmDelivery(int orderId, int customerId) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null || order.getCustomerId() != customerId) {
            throw new RuntimeException("Đơn hàng không hợp lệ!");
        }
        if (!"DISPATCHED".equals(order.getStatus()) && !"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể xác nhận nhận hàng đối với đơn đang giao (DISPATCHED) hoặc đã giao (DELIVERED)!");
        }
        orderDAO.updateStatus(orderId, "DELIVERED");
        orderDAO.updateReceivedStatus(orderId, "RECEIVED");

        try {
            UserDAO userDAO = new UserDAO();
            User customer = userDAO.findUserById(customerId);
            if (customer != null) {
                String customerMsg = "Đơn hàng #" + orderId + " đã giao thành công và được bạn xác nhận.";
                notificationService.send(customerId, AppConfig.NOTIF_ORDER_UPDATE, "Giao hàng thành công", customerMsg, "/orders/detail?orderId=" + orderId);
                
                EmailService emailService = new EmailService();
                String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + orderId;
                emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(orderId), "Giao hàng thành công (DELIVERED)", orderDetailUrl);
            }
        } catch (Exception ex) {
            System.err.println("[OrderService] WARN: Không gửi được thông báo delivery confirm cho orderId=" + orderId + ": " + ex.getMessage());
        }
    }

    /**
     * Tự động hủy các đơn hàng quá 30 phút mà cửa hàng không nhận (chấp nhận giao).
     * Hoàn tiền cho khách nếu là đơn CK.
     */
    public void autoCancelUnacceptedOrders() throws SQLException {
        String sql = "SELECT * FROM orders WHERE status = 'CONFIRMED' AND shop_acceptance_deadline IS NOT NULL AND shop_acceptance_deadline < GETDATE()";
        try (java.sql.Connection conn = orderDAO.openConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int customerId = rs.getInt("customer_id");
                    int ownerId = rs.getInt("owner_id");
                    String paymentMethod = rs.getString("payment_method");
                    
                    // 1. Hủy đơn hàng và hoàn trả tồn kho
                    cancelOrder(orderId, 1, "Quá 30 phút cửa hàng không nhận đơn. Hệ thống tự động hủy.");
                    
                    // 2. Nếu là đơn CK, hoàn tiền tự động (giả lập)
                    if ("CK".equals(paymentMethod)) {
                        orderDAO.updateRefundStatus(orderId, "REFUNDED");
                        // Cập nhật trạng thái payment transaction tương ứng
                        var txList = paymentDAO.findByOrder(orderId);
                        if (!txList.isEmpty()) {
                            paymentDAO.updateStatus(txList.get(0).getTransactionId(), "refunded", "SYSTEM_TIMEOUT_REFUND", "Cửa hàng không nhận đơn");
                        }
                    }
                    
                    // 3. Gửi thông báo cho khách hàng
                    try {
                        notificationService.send(customerId, "ORDER_UPDATE", "Đơn hàng tự động hủy", 
                            "Rất tiếc, cửa hàng đã không nhận chuẩn bị đơn hàng #" + orderId + " của bạn trong vòng 30 phút. " +
                            ("CK".equals(paymentMethod) ? "Tiền đã được hoàn trả tự động vào tài khoản của bạn. " : "") +
                            "Vui lòng đặt lại đơn hàng khác.", "/customer/orders");
                    } catch (Exception e) {
                        System.err.println("Failed to notify customer of auto cancellation: " + e.getMessage());
                    }
                    
                    // 4. Gửi thông báo cho chủ shop
                    try {
                        notificationService.send(ownerId, "ORDER_UPDATE", "Hủy đơn hàng do quá hạn nhận", 
                            "Đơn hàng #" + orderId + " đã bị hệ thống tự động hủy và hoàn tiền vì bạn không bấm nhận đơn trong vòng 30 phút.", "/shop/orders");
                    } catch (Exception e) {
                        System.err.println("Failed to notify shop owner of auto cancellation: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Tự động xác nhận hoàn thành đơn hàng đã giao (DELIVERED) sau freeze_days ngày nếu khách không khiếu nại.
     */
    public void autoConfirmDeliveredOrders() throws SQLException {
        int freezeDays = configDAO.getInt(AppConfig.CONFIG_FREEZE_DAYS, AppConfig.FREEZE_DAYS_DEFAULT);
        String sql = "SELECT o.order_id, o.owner_id, o.final_amount FROM orders o "
                   + "LEFT JOIN deliveries d ON d.order_id = o.order_id "
                   + "WHERE o.status = 'DELIVERED' "
                   + "AND NOT EXISTS (SELECT 1 FROM return_requests r WHERE r.order_id = o.order_id AND r.status IN ('REQUESTED', 'PROCESSING', 'APPROVED')) "
                   + "AND COALESCE(d.delivered_at, o.updated_at) < DATEADD(day, ?, GETDATE())";
        try (java.sql.Connection conn = orderDAO.openConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, -freezeDays);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    System.out.println("[OrderService] Auto-confirming settlement eligibility for order #" + orderId + " after " + freezeDays + " freeze days.");
                    String updateSql = "UPDATE orders SET updated_at = GETDATE() WHERE order_id = ?";
                    try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                        psUpdate.setInt(1, orderId);
                        psUpdate.executeUpdate();
                    }
                }
            }
        }
    }

    public java.math.BigDecimal getRevenueByOwner(int ownerId) throws SQLException {
        return orderDAO.getRevenueByOwner(ownerId);
    }

    public int getOrderCountByOwner(int ownerId) throws SQLException {
        return orderDAO.countByOwner(ownerId, null);
    }

    public java.util.List<com.fruitmkt.model.entity.Order> getRecentOrdersByOwner(int ownerId, int limit) throws SQLException {
        return orderDAO.findByOwner(ownerId, null, 1, limit);
    }

    public java.util.List<com.fruitmkt.model.entity.OrderItem> getOrderItems(int orderId) throws SQLException {
        return orderDAO.findItemsByOrderId(orderId);
    }
}

