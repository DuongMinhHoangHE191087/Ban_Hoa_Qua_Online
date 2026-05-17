package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * ProductService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class ProductService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.dto.PagedResultDTO getProductList(int page, String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getProductList(int page, String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.Product getProductDetail(int productId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getProductDetail(int productId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getVariants(int productId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getVariants(int productId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public int createProduct(com.fruitmkt.model.entity.Product product) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: createProduct(com.fruitmkt.model.entity.Product product)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void updateProduct(com.fruitmkt.model.entity.Product product) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: updateProduct(com.fruitmkt.model.entity.Product product)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void toggleStatus(int productId, String status) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: toggleStatus(int productId, String status)");
    }

}
