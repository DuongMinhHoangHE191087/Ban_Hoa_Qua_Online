package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Product;
import java.sql.*;
import java.util.*;

/**
 * ProductDAO — DAO cho entity Product.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ProductDAO extends BaseDAO {

    /**
     * Tìm sản phẩm theo ID.
     */
    public List<Product> findById(int id) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách toàn bộ sản phẩm có phân trang.
     */
    public List<Product> findAll(int page, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM products ORDER BY product_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm theo ID của chủ cửa hàng.
     */
    public List<Product> findByOwner(int ownerId) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE owner_id = ? ORDER BY product_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm theo Category ID có phân trang.
     */
    public List<Product> findByCategory(int categoryId, int page, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY product_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách tất cả sản phẩm đang có chương trình khuyến mãi hoạt động (Flash Sale).
     */
    public List<Product> findFlashSaleProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM products p "
                   + "JOIN promotions pr ON p.product_id = pr.product_id "
                   + "WHERE pr.scope = 'PRODUCT' AND pr.is_active = 1 AND pr.is_deleted = 0 "
                   + "AND pr.valid_from <= GETDATE() AND pr.valid_until >= GETDATE() "
                   + "ORDER BY p.product_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa, danh mục, khoảng giá và phân trang.
     */
    public List<Product> search(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, int page, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.* FROM products p ");
        if (minPrice != null || maxPrice != null) {
            sql.append("JOIN product_variants pv ON p.product_id = pv.product_id ");
        }
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append("AND pv.price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND pv.price <= ? ");
            params.add(maxPrice);
        }
        
        sql.append("ORDER BY p.product_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số sản phẩm khớp với bộ lọc tìm kiếm/danh mục để hỗ trợ phân trang.
     */
    public int countSearch(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT p.product_id) FROM products p ");
        if (minPrice != null || maxPrice != null) {
            sql.append("JOIN product_variants pv ON p.product_id = pv.product_id ");
        }
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append("AND pv.price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND pv.price <= ? ");
            params.add(maxPrice);
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Lưu sản phẩm mới vào DB.
     */
    public int save(Product product) throws SQLException {
        String sql = "INSERT INTO products (owner_id, category_id, name, description, origin_country, origin_region, harvest_date, shelf_life_days, storage_instruction, status, view_count, rating, sold_quantity, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, product.getOwnerId());
            ps.setInt(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getOriginCountry());
            ps.setString(6, product.getOriginRegion());
            
            if (product.getHarvestDate() != null) {
                ps.setDate(7, java.sql.Date.valueOf(product.getHarvestDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            
            if (product.getShelfLifeDays() != null) {
                ps.setInt(8, product.getShelfLifeDays());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.setString(9, product.getStorageInstruction());
            ps.setString(10, product.getStatus() != null ? product.getStatus() : "ACTIVE");
            ps.setInt(11, product.getViewCount());
            ps.setBigDecimal(12, product.getRating() != null ? product.getRating() : java.math.BigDecimal.ZERO);
            ps.setInt(13, product.getSoldQuantity());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu sản phẩm thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Cập nhật thông tin sản phẩm.
     */
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET owner_id = ?, category_id = ?, name = ?, description = ?, origin_country = ?, origin_region = ?, harvest_date = ?, shelf_life_days = ?, storage_instruction = ?, status = ?, view_count = ?, rating = ?, sold_quantity = ?, updated_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getOwnerId());
            ps.setInt(2, product.getCategoryId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setString(5, product.getOriginCountry());
            ps.setString(6, product.getOriginRegion());
            
            if (product.getHarvestDate() != null) {
                ps.setDate(7, java.sql.Date.valueOf(product.getHarvestDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            
            if (product.getShelfLifeDays() != null) {
                ps.setInt(8, product.getShelfLifeDays());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            ps.setString(9, product.getStorageInstruction());
            ps.setString(10, product.getStatus());
            ps.setInt(11, product.getViewCount());
            ps.setBigDecimal(12, product.getRating());
            ps.setInt(13, product.getSoldQuantity());
            ps.setInt(14, product.getProductId());
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái sản phẩm.
     */
    public void updateStatus(int productId, String status) throws SQLException {
        String sql = "UPDATE products SET status = ?, updated_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    /**
     * Tăng số lượng lượt xem của sản phẩm.
     */
    public void incrementViewCount(int productId) throws SQLException {
        String sql = "UPDATE products SET view_count = view_count + 1, updated_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    /**
     * Tìm kiếm các sản phẩm tương tự cùng danh mục (Category) phục vụ hiển thị
     * danh sách gợi ý ở trang chi tiết sản phẩm. Loại trừ sản phẩm hiện tại.
     * Sắp xếp theo số lượng bán (sold_quantity DESC) và lượt xem (view_count DESC).
     *
     * @param productId   ID sản phẩm hiện tại cần loại trừ
     * @param categoryId  ID danh mục của sản phẩm hiện tại
     * @param limit       Số lượng sản phẩm tương tự tối đa cần lấy
     * @return danh sách các sản phẩm tương tự
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu
     */
    public List<Product> findSimilarProducts(int productId, int categoryId, int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products "
                   + "WHERE category_id = ? AND product_id != ? AND status = 'ACTIVE' "
                   + "ORDER BY sold_quantity DESC, view_count DESC, product_id DESC "
                   + "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, productId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm ACTIVE của một shop owner cụ thể,
     * loại trừ sản phẩm hiện tại đang xem, giới hạn số lượng.
     * Phục vụ phần "Xem thêm từ cửa hàng này" trên trang chi tiết sản phẩm.
     *
     * @param ownerId          ID của chủ cửa hàng
     * @param excludeProductId ID sản phẩm đang xem (loại trừ khỏi danh sách)
     * @param limit            Số lượng sản phẩm tối đa cần lấy
     * @return danh sách sản phẩm khác của shop
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public List<Product> findByOwnerAndActiveStatus(int ownerId, int excludeProductId, int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products "
                   + "WHERE owner_id = ? AND product_id != ? AND status = 'ACTIVE' "
                   + "ORDER BY sold_quantity DESC, view_count DESC, product_id DESC "
                   + "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setInt(2, excludeProductId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }


    /** Ánh xạ ResultSet -> Product — gọi trong mọi query SELECT */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setOwnerId(rs.getInt("owner_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setOriginCountry(rs.getString("origin_country"));
        p.setOriginRegion(rs.getString("origin_region"));
        
        java.sql.Date harvestDateVal = rs.getDate("harvest_date");
        if (harvestDateVal != null) {
            p.setHarvestDate(harvestDateVal.toLocalDate());
        }
        
        int shelfLife = rs.getInt("shelf_life_days");
        p.setShelfLifeDays(rs.wasNull() ? null : shelfLife);
        
        p.setStorageInstruction(rs.getString("storage_instruction"));
        p.setStatus(rs.getString("status"));
        p.setViewCount(rs.getInt("view_count"));
        p.setRating(rs.getBigDecimal("rating"));
        p.setSoldQuantity(rs.getInt("sold_quantity"));
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            p.setCreatedAt(createdAtVal.toLocalDateTime());
        }
        
        Timestamp updatedAtVal = rs.getTimestamp("updated_at");
        if (updatedAtVal != null) {
            p.setUpdatedAt(updatedAtVal.toLocalDateTime());
        }
        
        return p;
    }
}
