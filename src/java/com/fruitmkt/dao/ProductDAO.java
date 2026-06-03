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

    public ProductDAO() {
        super();
        ensureDeletedStatusAllowed();
    }

    private void ensureDeletedStatusAllowed() {
        String sqlCheck = "SELECT COUNT(*) FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('products') AND definition LIKE '%DELETED%'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            boolean hasDeleted = false;
            try (ResultSet rs = stmt.executeQuery(sqlCheck)) {
                if (rs.next()) {
                    hasDeleted = rs.getInt(1) > 0;
                }
            }
            if (!hasDeleted) {
                String sqlFindName = "SELECT name FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('products') AND definition LIKE '%status%'";
                String constraintName = null;
                try (ResultSet rs = stmt.executeQuery(sqlFindName)) {
                    if (rs.next()) {
                        constraintName = rs.getString(1);
                    }
                }
                if (constraintName != null) {
                    stmt.execute("ALTER TABLE products DROP CONSTRAINT " + constraintName);
                }
                stmt.execute("ALTER TABLE products ADD CONSTRAINT CK_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))");
            }
        } catch (SQLException e) {
            System.err.println("Warning: Cannot alter products.status check constraint: " + e.getMessage());
        }
    }

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
        String sql = "SELECT * FROM products WHERE status = 'ACTIVE' ORDER BY product_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
        String sql = "SELECT * FROM products WHERE owner_id = ? AND status != 'DELETED' ORDER BY product_id DESC";
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
        String sql = "SELECT * FROM products WHERE category_id = ? AND status = 'ACTIVE' ORDER BY product_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
        sql.append("WHERE p.status = 'ACTIVE' ");

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
     * Đếm tổng số sản phẩm ACTIVE khớp với bộ lọc tìm kiếm/danh mục để hỗ trợ phân trang.
     */
    public int countSearch(String keyword, Integer categoryId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT p.product_id) FROM products p ");
        if (minPrice != null || maxPrice != null) {
            sql.append("JOIN product_variants pv ON p.product_id = pv.product_id ");
        }
        sql.append("WHERE p.status = 'ACTIVE' ");

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
        String sql = "INSERT INTO products (owner_id, category_id, name, description, origin_country, origin_region, harvest_date, shelf_life_days, storage_instruction, status, view_count, rating, sold_quantity, label_type, season_start, season_end, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
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
            ps.setString(14, product.getLabelType());

            if (product.getSeasonStart() != null) {
                ps.setInt(15, product.getSeasonStart());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            if (product.getSeasonEnd() != null) {
                ps.setInt(16, product.getSeasonEnd());
            } else {
                ps.setNull(16, Types.INTEGER);
            }

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
        String sql = "UPDATE products SET owner_id = ?, category_id = ?, name = ?, description = ?, origin_country = ?, origin_region = ?, harvest_date = ?, shelf_life_days = ?, storage_instruction = ?, status = ?, view_count = ?, rating = ?, sold_quantity = ?, label_type = ?, season_start = ?, season_end = ?, updated_at = GETDATE() WHERE product_id = ?";
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
            ps.setString(14, product.getLabelType());

            if (product.getSeasonStart() != null) {
                ps.setInt(15, product.getSeasonStart());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            if (product.getSeasonEnd() != null) {
                ps.setInt(16, product.getSeasonEnd());
            } else {
                ps.setNull(16, Types.INTEGER);
            }

            ps.setInt(17, product.getProductId());
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

    /**
     * Lấy danh sách 5 sản phẩm bán chạy nhất đã giao hàng.
     *
     * @param limit số lượng sản phẩm tối đa cần lấy
     * @return danh sách sản phẩm bán chạy nhất
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public List<Product> findBestSellers(int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT TOP (?) p.*, SUM(oi.quantity) as sold "
                   + "FROM products p "
                   + "JOIN order_items oi ON p.product_id = oi.product_id "
                   + "JOIN orders o ON oi.order_id = o.order_id "
                   + "WHERE o.status = 'DELIVERED' "
                   + "GROUP BY p.product_id, p.owner_id, p.category_id, p.name, p.description, p.origin_country, p.origin_region, p.harvest_date, p.shelf_life_days, p.storage_instruction, p.status, p.view_count, p.rating, p.sold_quantity, p.created_at, p.updated_at, p.label_type, p.season_start, p.season_end "
                   + "ORDER BY sold DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy chi tiết sản phẩm theo danh sách các mã ID (hỗ trợ Recently Viewed cookie).
     *
     * @param productIds danh sách ID sản phẩm cần lấy
     * @return danh sách sản phẩm theo đúng thứ tự ID đầu vào
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public List<Product> findRecentlyViewed(List<Integer> productIds) throws SQLException {
        List<Product> list = new ArrayList<>();
        if (productIds == null || productIds.isEmpty()) {
            return list;
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE status = 'ACTIVE' AND product_id IN (");
        for (int i = 0; i < productIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setInt(i + 1, productIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, Product> map = new HashMap<>();
                while (rs.next()) {
                    Product p = mapRow(rs);
                    map.put(p.getProductId(), p);
                }
                // Keep the exact input order of IDs
                for (Integer id : productIds) {
                    if (map.containsKey(id)) {
                        list.add(map.get(id));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Bộ lọc tìm kiếm nâng cao với hỗ trợ đa danh mục, khoảng giá, đánh giá,
     * tình trạng tồn kho và sắp xếp linh hoạt.
     *
     * @param keyword      từ khóa tìm kiếm (tên hoặc mô tả)
     * @param categoryIds  danh sách ID danh mục lọc
     * @param minPrice     giá tối thiểu
     * @param maxPrice     giá tối đa
     * @param rating       đánh giá tối thiểu
     * @param inStockOnly  chỉ lấy sản phẩm còn hàng
     * @param sortBy       tiêu chí sắp xếp: price_asc, price_desc, rating, newest
     * @param page         trang hiện tại
     * @param pageSize     số sản phẩm mỗi trang
     * @return danh sách sản phẩm phù hợp
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public List<Product> searchAdvanced(String keyword, List<Integer> categoryIds, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Double rating, Boolean inStockOnly, String sortBy, int page, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.* FROM products p ");

        // Always join variants if we need variant properties for filters or order
        sql.append("LEFT JOIN product_variants pv ON p.product_id = pv.product_id AND pv.is_active = 1 ");
        sql.append("WHERE p.status = 'ACTIVE' ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            sql.append("AND p.category_id IN (");
            for (int i = 0; i < categoryIds.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
                params.add(categoryIds.get(i));
            }
            sql.append(") ");
        }

        if (minPrice != null) {
            sql.append("AND COALESCE(pv.discount_price, pv.price) >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND COALESCE(pv.discount_price, pv.price) <= ? ");
            params.add(maxPrice);
        }

        if (rating != null) {
            sql.append("AND p.rating >= ? ");
            params.add(rating);
        }

        if (inStockOnly != null && inStockOnly) {
            sql.append("AND EXISTS (SELECT 1 FROM product_variants WHERE product_id = p.product_id AND stock_quantity > 0 AND is_active = 1) ");
        }

        // Sorting using scalar dependent subqueries to avoid SELECT DISTINCT issues in SQL Server
        if ("price_asc".equals(sortBy)) {
            sql.append("ORDER BY (SELECT MIN(COALESCE(pv2.discount_price, pv2.price)) FROM product_variants pv2 WHERE pv2.product_id = p.product_id AND pv2.is_active = 1) ASC ");
        } else if ("price_desc".equals(sortBy)) {
            sql.append("ORDER BY (SELECT MIN(COALESCE(pv2.discount_price, pv2.price)) FROM product_variants pv2 WHERE pv2.product_id = p.product_id AND pv2.is_active = 1) DESC ");
        } else if ("rating".equals(sortBy)) {
            sql.append("ORDER BY p.rating DESC ");
        } else {
            // default or "newest"
            sql.append("ORDER BY p.product_id DESC ");
        }

        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
     * Đếm tổng số sản phẩm khớp bộ lọc nâng cao để phân trang.
     *
     * @param keyword     từ khóa tìm kiếm
     * @param categoryIds danh sách ID danh mục lọc
     * @param minPrice    giá tối thiểu
     * @param maxPrice    giá tối đa
     * @param rating      đánh giá tối thiểu
     * @param inStockOnly chỉ đếm sản phẩm còn hàng
     * @return tổng số sản phẩm khớp
     * @throws SQLException nếu xảy ra lỗi truy vấn
     */
    public int countSearchAdvanced(String keyword, List<Integer> categoryIds, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Double rating, Boolean inStockOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT p.product_id) FROM products p ");
        sql.append("LEFT JOIN product_variants pv ON p.product_id = pv.product_id AND pv.is_active = 1 ");
        sql.append("WHERE p.status = 'ACTIVE' ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            sql.append("AND p.category_id IN (");
            for (int i = 0; i < categoryIds.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
                params.add(categoryIds.get(i));
            }
            sql.append(") ");
        }

        if (minPrice != null) {
            sql.append("AND COALESCE(pv.discount_price, pv.price) >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND COALESCE(pv.discount_price, pv.price) <= ? ");
            params.add(maxPrice);
        }

        if (rating != null) {
            sql.append("AND p.rating >= ? ");
            params.add(rating);
        }

        if (inStockOnly != null && inStockOnly) {
            sql.append("AND EXISTS (SELECT 1 FROM product_variants WHERE product_id = p.product_id AND stock_quantity > 0 AND is_active = 1) ");
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

        p.setLabelType(rs.getString("label_type"));

        int sStart = rs.getInt("season_start");
        p.setSeasonStart(rs.wasNull() ? null : sStart);

        int sEnd = rs.getInt("season_end");
        p.setSeasonEnd(rs.wasNull() ? null : sEnd);

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