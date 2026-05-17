package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * SettlementService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class SettlementService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.ShopSettlement calculate(int ownerId, java.time.LocalDate start, java.time.LocalDate end) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: calculate(int ownerId, java.time.LocalDate start, java.time.LocalDate end)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void confirm(int settlementId, int adminId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: confirm(int settlementId, int adminId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void markPaid(int settlementId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: markPaid(int settlementId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getSettlements(int ownerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getSettlements(int ownerId)");
    }

}
