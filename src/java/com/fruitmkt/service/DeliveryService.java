package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * DeliveryService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class DeliveryService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void assignDelivery(int orderId, int staffId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: assignDelivery(int orderId, int staffId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void updateStatus(int deliveryId, String status, String failureReason) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: updateStatus(int deliveryId, String status, String failureReason)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getMyDeliveries(int staffId, String status) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getMyDeliveries(int staffId, String status)");
    }

}
