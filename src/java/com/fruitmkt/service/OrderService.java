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

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Order placeOrder(int customerId, com.fruitmkt.model.dto.CheckoutDTO dto) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: placeOrder(int customerId, com.fruitmkt.model.dto.CheckoutDTO dto)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Order getOrderDetail(int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getOrderDetail(int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.dto.PagedResultDTO getOrderHistory(int customerId, int page) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getOrderHistory(int customerId, int page)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void confirmOrder(int orderId, int ownerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: confirmOrder(int orderId, int ownerId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void cancelOrder(int orderId, int cancelledBy, String reason) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: cancelOrder(int orderId, int cancelledBy, String reason)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.dto.PagedResultDTO shopOrders(int ownerId, String status, int page) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: shopOrders(int ownerId, String status, int page)");
    }

}
