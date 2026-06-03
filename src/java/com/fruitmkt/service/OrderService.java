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

    public int countAllOrders(String status) throws SQLException {
        return orderDAO.countAll(status);
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
     * Cập nhật đơn hàng thành APPROVED (Duyệt đơn)
     */
    public void confirmOrder(int orderId, int ownerId) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null || order.getOwnerId() != ownerId) {
            throw new RuntimeException("Đơn hàng không hợp lệ hoặc bạn không có quyền duyệt!");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể duyệt đơn hàng ở trạng thái PENDING hoặc CONFIRMED");
        }
        orderDAO.updateStatus(orderId, "APPROVED");
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
        
        // Cập nhật DB trạng thái CANCELLED
        orderDAO.cancel(orderId, cancelledBy, reason);
        // Hoàn trả tồn kho
        orderDAO.restoreInventoryStock(orderId);
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
        if (!"APPROVED".equals(order.getStatus()) && !"PREPARING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể giao đơn đang được chuẩn bị hoặc đã duyệt!");
        }
        orderDAO.updateStatus(orderId, "DISPATCHED");
    }

    /**
     * Khách hàng xác nhận đã nhận hàng
     */
    public void customerConfirmDelivery(int orderId, int customerId) throws SQLException {
        com.fruitmkt.model.entity.Order order = getOrderDetail(orderId);
        if (order == null || order.getCustomerId() != customerId) {
            throw new RuntimeException("Đơn hàng không hợp lệ!");
        }
        if (!"DISPATCHED".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể xác nhận nhận hàng đối với đơn đang giao!");
        }
        orderDAO.updateStatus(orderId, "DELIVERED");
    }
}
