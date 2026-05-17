package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * ReturnService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class ReturnService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public int createRequest(com.fruitmkt.model.entity.ReturnRequest req) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: createRequest(com.fruitmkt.model.entity.ReturnRequest req)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.ReturnRequest getRequest(int requestId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getRequest(int requestId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void decide(int requestId, String status, String reason, int decidedBy) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: decide(int requestId, String status, String reason, int decidedBy)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getMyRequests(int customerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getMyRequests(int customerId)");
    }

}
