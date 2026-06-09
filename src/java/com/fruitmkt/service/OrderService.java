package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CartDAO;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.PaymentDAO;
import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.dto.CheckoutDTO;
import com.fruitmkt.model.dto.PagedResultDTO;
import com.fruitmkt.model.dto.ReorderResultDTO;
import com.fruitmkt.model.entity.Cart;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.User;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderService - Tang business logic cho nghiep vu don hang.
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final NotificationService notificationService = new NotificationService();
    private final InventoryService inventoryService = new InventoryService();
    private final DeliveryService deliveryService = new DeliveryService();
    private final CartDAO cartDAO = new CartDAO();
    private final UserDAO userDAO = new UserDAO();

    public Order placeOrder(int customerId, CheckoutDTO dto) throws SQLException {
        throw new UnsupportedOperationException("Not implemented: placeOrder(int customerId, CheckoutDTO dto)");
    }

    public List<Order> getAllOrders(String status, int page, int pageSize) throws SQLException {
        return orderDAO.findAll(status, page, pageSize);
    }

    public List<Order> getAllOrders(String status, String paymentMethod, String paymentStatus, int page, int pageSize)
            throws SQLException {
        return orderDAO.findAll(status, paymentMethod, paymentStatus, page, pageSize);
    }

    public int countAllOrders(String status) throws SQLException {
        return orderDAO.countAll(status);
    }

    public int countAllOrders(String status, String paymentMethod, String paymentStatus) throws SQLException {
        return orderDAO.countAll(status, paymentMethod, paymentStatus);
    }

    public Order getOrderDetail(int orderId) throws SQLException {
        List<Order> list = orderDAO.findById(orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    public PagedResultDTO getOrderHistory(int customerId, int page) throws SQLException {
        throw new UnsupportedOperationException("Not implemented: getOrderHistory(int customerId, int page)");
    }

    public void confirmOrder(int orderId, int ownerId) throws SQLException {
        Order order = getOrderDetail(orderId);
        if (order == null || order.getOwnerId() != ownerId) {
            throw new RuntimeException("Don hang khong hop le hoac ban khong co quyen duyet!");
        }
        if (!AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())
                && !AppConfig.ORDER_CONFIRMED.equals(order.getStatus())) {
            throw new RuntimeException("Chi co the duyet don hang o trang thai PENDING_PAYMENT hoac CONFIRMED.");
        }
        orderDAO.updateStatus(orderId, AppConfig.ORDER_CONFIRMED);
    }

    public void cancelOrder(int orderId, int cancelledBy, String reason) throws SQLException {
        Order order = getOrderDetail(orderId);
        if (order == null) {
            throw new RuntimeException("Don hang khong ton tai!");
        }
        if (AppConfig.ORDER_DELIVERED.equals(order.getStatus()) || AppConfig.ORDER_CANCELLED.equals(order.getStatus())) {
            throw new RuntimeException("Don hang da giao hoac da huy, khong the huy them!");
        }

        User user = userDAO.findUserById(cancelledBy);
        if (user != null) {
            if (AppConfig.ROLE_CUSTOMER.equals(user.getRole())) {
                if (order.getCustomerId() != cancelledBy) {
                    throw new RuntimeException("Ban khong co quyen huy don hang nay!");
                }
                if (!AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())
                        && !AppConfig.ORDER_CONFIRMED.equals(order.getStatus())) {
                    throw new RuntimeException("Cua hang da duyet hoac dang giao don, khong the tu y huy!");
                }
            } else if (AppConfig.ROLE_SHOP_OWNER.equals(user.getRole()) && order.getOwnerId() != cancelledBy) {
                throw new RuntimeException("Don hang nay khong thuoc cua hang cua ban!");
            }
        }

        orderDAO.cancel(orderId, cancelledBy, reason);
        List<OrderItem> items = orderDAO.findItemsByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getVariantId() != null) {
                inventoryService.release(item.getVariantId(), item.getQuantity(), orderId);
            }
        }
    }

    public PagedResultDTO shopOrders(int ownerId, String status, int page) throws SQLException {
        int pageSize = 10;
        List<Order> list = orderDAO.findByOwner(ownerId, status, page, pageSize);
        PagedResultDTO dto = new PagedResultDTO();
        dto.setItems(list);
        dto.setCurrentPage(page);
        return dto;
    }

    public void dispatchOrder(int orderId, int ownerId) throws SQLException {
        dispatchOrder(orderId, ownerId, null);
    }

    public void dispatchOrder(int orderId, int ownerId, LocalDateTime estimatedTime) throws SQLException {
        Order order = getOrderDetail(orderId);
        if (order == null || order.getOwnerId() != ownerId) {
            throw new RuntimeException("Don hang khong hop le!");
        }
        if (!AppConfig.ORDER_CONFIRMED.equals(order.getStatus()) && !"PREPARING".equals(order.getStatus())) {
            throw new RuntimeException("Chi co the giao don dang duoc chuan bi hoac da duyet!");
        }
        orderDAO.updateStatus(orderId, AppConfig.ORDER_DISPATCHED);
        deliveryService.assignShipper(orderId, 0, estimatedTime);
    }

    public void customerConfirmDelivery(int orderId, int customerId) throws SQLException {
        Order order = orderDAO.findByIdForCustomer(orderId, customerId);
        if (order == null) {
            throw new RuntimeException("Don hang khong hop le!");
        }
        if (!AppConfig.ORDER_DISPATCHED.equals(order.getStatus())
                && !AppConfig.ORDER_DELIVERED.equals(order.getStatus())) {
            throw new RuntimeException("Chi co the xac nhan nhan hang doi voi don dang giao hoac da giao!");
        }
        orderDAO.updateStatus(orderId, AppConfig.ORDER_DELIVERED);
        orderDAO.updateReceivedStatus(orderId, "RECEIVED");
    }

    public void reportNotReceived(int orderId, int customerId) throws SQLException {
        Order order = orderDAO.findByIdForCustomer(orderId, customerId);
        if (order == null) {
            throw new SecurityException("Ban khong co quyen thuc hien hanh dong nay.");
        }
        if (!AppConfig.ORDER_DISPATCHED.equals(order.getStatus())
                && !AppConfig.ORDER_DELIVERED.equals(order.getStatus())) {
            throw new IllegalStateException("Chi co the bao chua nhan hang voi don dang giao hoac da giao.");
        }
        orderDAO.updateReceivedStatus(orderId, "NOT_RECEIVED");
    }

    public ReorderResultDTO reorder(int orderId, int customerId) throws SQLException {
        Order order = orderDAO.findByIdForCustomer(orderId, customerId);
        if (order == null) {
            throw new SecurityException("Ban khong co quyen thuc hien hanh dong nay.");
        }

        List<OrderItem> items = orderDAO.findItemsByOrderId(orderId);
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        int cartId = carts.isEmpty() ? cartDAO.createForCustomer(customerId) : carts.get(0).getCartId();

        int addedCount = 0;
        int skippedCount = 0;
        for (OrderItem item : items) {
            if (item.getVariantId() == null) {
                skippedCount++;
                continue;
            }
            try {
                cartDAO.addItem(cartId, item.getVariantId(), item.getQuantity());
                addedCount++;
            } catch (Exception ex) {
                skippedCount++;
            }
        }

        ReorderResultDTO result = new ReorderResultDTO();
        result.setAddedCount(addedCount);
        result.setSkippedCount(skippedCount);
        return result;
    }

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

                    cancelOrder(orderId, 1, "Qua 30 phut cua hang khong nhan don. He thong tu dong huy.");

                    if (AppConfig.PAYMENT_CK.equals(paymentMethod)) {
                        orderDAO.updateRefundStatus(orderId, "REFUNDED");
                        var txList = paymentDAO.findByOrder(orderId);
                        if (!txList.isEmpty()) {
                            paymentDAO.updateStatus(txList.get(0).getTransactionId(),
                                    "refunded", "SYSTEM_TIMEOUT_REFUND", "Cua hang khong nhan don");
                        }
                    }

                    try {
                        notificationService.send(customerId, "ORDER_UPDATE", "Đơn hàng tự động hủy",
                                "Rất tiếc, cửa hàng đã không nhận chuẩn bị đơn hàng #" + orderId + " của bạn trong vòng 30 phút. "
                                        + (AppConfig.PAYMENT_CK.equals(paymentMethod)
                                        ? "Tiền đã được hoàn trả tự động vào tài khoản của bạn. " : "")
                                        + "Vui lòng đặt lại đơn hàng khác.",
                                "/customer/orders");
                    } catch (Exception e) {
                        System.err.println("Failed to notify customer of auto cancellation: " + e.getMessage());
                    }

                    try {
                        notificationService.send(ownerId, "ORDER_UPDATE", "Hủy đơn hàng do quá hạn nhận",
                                "Đơn hàng #" + orderId + " đã bị hệ thống tự động hủy và hoàn tiền vì bạn không bấm nhận đơn trong vòng 30 phút.",
                                "/shop/orders");
                    } catch (Exception e) {
                        System.err.println("Failed to notify shop owner of auto cancellation: " + e.getMessage());
                    }
                }
            }
        }
    }

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
                    System.out.println("[OrderService] Auto-confirming settlement eligibility for order #" + orderId
                            + " after " + freezeDays + " freeze days.");
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

    public List<Order> getRecentOrdersByOwner(int ownerId, int limit) throws SQLException {
        return orderDAO.findByOwner(ownerId, null, 1, limit);
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        return orderDAO.findItemsByOrderId(orderId);
    }
}
