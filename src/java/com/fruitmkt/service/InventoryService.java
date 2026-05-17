package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * InventoryService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class InventoryService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void reserve(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: reserve(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void release(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: release(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void confirm(int variantId, int qty, int orderId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: confirm(int variantId, int qty, int orderId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void manualAdjust(int variantId, int delta, String note, int userId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: manualAdjust(int variantId, int delta, String note, int userId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getLogs(int variantId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getLogs(int variantId)");
    }

}
