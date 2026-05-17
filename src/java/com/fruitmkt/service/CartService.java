package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * CartService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class CartService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.dto.CartSummaryDTO getCart(int customerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getCart(int customerId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void addToCart(int customerId, int variantId, int qty) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: addToCart(int customerId, int variantId, int qty)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void updateQuantity(int cartItemId, int qty) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: updateQuantity(int cartItemId, int qty)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void removeItem(int cartItemId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: removeItem(int cartItemId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void syncGuestCart(int customerId, String guestCartJson) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: syncGuestCart(int customerId, String guestCartJson)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.math.BigDecimal applyVoucher(int cartId, String code) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: applyVoucher(int cartId, String code)");
    }

}
