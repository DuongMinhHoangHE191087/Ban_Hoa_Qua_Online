package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.model.dto.PagedResultDTO;
import com.fruitmkt.model.entity.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * ProductService — Business logic cho Product.
 *
 * SRP: Validate input, áp dụng business rule, delegate xuống DAO.
 * Không viết SQL, không tương tác HttpRequest/Response.
 *
 * @author fruitmkt-team
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Lấy danh sách sản phẩm có filter + phân trang.
     *
     * @param page       Trang hiện tại (1-based). Nếu <= 0 sẽ được reset về 1.
     * @param keyword    Từ khóa tìm kiếm (nullable).
     * @param categoryId ID danh mục (nullable = tất cả danh mục).
     * @param minPrice   Giá tối thiểu (nullable).
     * @param maxPrice   Giá tối đa (nullable).
     */
    public PagedResultDTO getProductList(int page, String keyword, Integer categoryId,
                                          BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        if (page < 1) page = 1;

        int pageSize = AppConfig.PAGE_SIZE_PRODUCTS;
        int total = productDAO.countSearch(keyword, categoryId, minPrice, maxPrice);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page > totalPages) page = totalPages;

        List<Product> items = productDAO.search(keyword, categoryId, minPrice, maxPrice, page, pageSize);
        return new PagedResultDTO(items, page, totalPages, total, pageSize);
    }

    /**
     * Lấy chi tiết 1 sản phẩm theo ID.
     *
     * @throws IllegalArgumentException nếu productId <= 0.
     * @throws SQLException             nếu không tìm thấy sản phẩm.
     */
    public Product getProductDetail(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId không hợp lệ.");
        }
        List<Product> results = productDAO.findById(productId);
        if (results == null || results.isEmpty()) {
            throw new SQLException("Không tìm thấy sản phẩm với ID: " + productId);
        }
        productDAO.incrementViewCount(productId);
        return results.get(0);
    }

    /**
     * Tạo sản phẩm mới — chỉ dành cho SHOP_OWNER.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public int createProduct(Product product) throws SQLException {
        validateProduct(product);
        product.setStatus("ACTIVE");
        return productDAO.save(product);
    }

    /**
     * Cập nhật sản phẩm — chỉ chủ sở hữu mới được phép.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public void updateProduct(Product product) throws SQLException {
        validateProduct(product);
        productDAO.update(product);
    }

    /**
     * Bật/tắt trạng thái sản phẩm.
     *
     * @param status Phải là "ACTIVE" hoặc "INACTIVE".
     */
    public void toggleStatus(int productId, String status) throws SQLException {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
        productDAO.updateStatus(productId, status);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void validateProduct(Product product) {
        if (product == null) throw new IllegalArgumentException("Dữ liệu sản phẩm không được null.");
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (product.getOwnerId() <= 0) {
            throw new IllegalArgumentException("Owner ID không hợp lệ.");
        }
    }
}
