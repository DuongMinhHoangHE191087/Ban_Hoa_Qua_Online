package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * PromotionService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class PromotionService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public int createPromotion(com.fruitmkt.model.entity.Promotion promo) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: createPromotion(com.fruitmkt.model.entity.Promotion promo)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Promotion validate(String code, int customerId, java.math.BigDecimal orderTotal) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: validate(String code, int customerId, java.math.BigDecimal orderTotal)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getShopPromos(int ownerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getShopPromos(int ownerId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void deactivate(int promoId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: deactivate(int promoId)");
    }

}
