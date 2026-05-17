package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * ReviewService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class ReviewService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void submitReview(com.fruitmkt.model.entity.Review review) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: submitReview(com.fruitmkt.model.entity.Review review)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getReviews(int productId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getReviews(int productId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public boolean canReview(int customerId, int orderItemId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: canReview(int customerId, int orderItemId)");
    }

}
