package dao.catalog;

import dao.system.BaseDAO;
import model.entity.catalog.ProductVariant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ProductVariantDAO — DAO cho entity ProductVariant.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ProductVariantDAO extends BaseDAO {

    /**
     * Tìm biến thể sản phẩm theo ID.
     */
    public ProductVariant findById(int id) throws SQLException {
        String sql = "SELECT * FROM product_variants WHERE variant_id = ? AND is_active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm toàn bộ danh sách biến thể đang hoạt động của một sản phẩm.
     */
    public List<ProductVariant> findByProduct(int productId) throws SQLException {
        List<ProductVariant> list = new ArrayList<>();
        String sql = "SELECT * FROM product_variants WHERE product_id = ? AND is_active = 1 ORDER BY price ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Batch load biến thể đang hoạt động theo nhiều productId.
     */
    public Map<Integer, List<ProductVariant>> findByProductIds(Collection<Integer> productIds) throws SQLException {
        Map<Integer, List<ProductVariant>> map = new LinkedHashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(productIds);
        StringBuilder placeholders = new StringBuilder();
        int index = 0;
        for (Integer ignored : distinctIds) {
            if (index++ > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT * FROM product_variants WHERE product_id IN (" + placeholders + ") AND is_active = 1 "
                   + "ORDER BY product_id ASC, price ASC, variant_id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer productId : distinctIds) {
                ps.setInt(paramIndex++, productId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariant variant = mapRow(rs);
                    map.computeIfAbsent(variant.getProductId(), key -> new ArrayList<>()).add(variant);
                }
            }
        }
        return map;
    }

    /**
     * Tìm biến thể sản phẩm theo SKU.
     */
    public ProductVariant findBySku(String sku) throws SQLException {
        String sql = "SELECT * FROM product_variants WHERE sku = ? AND is_active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lưu một biến thể sản phẩm mới vào cơ sở dữ liệu.
     */
    public int save(ProductVariant variant) throws SQLException {
        String sql = "INSERT INTO product_variants (product_id, sku, variant_label, price, stock_quantity, weight_kg, is_active, discount_price, discount_start, discount_end, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, variant.getProductId());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getVariantLabel());
            ps.setBigDecimal(4, variant.getPrice());
            ps.setInt(5, variant.getStockQuantity());
            ps.setBigDecimal(6, variant.getWeightKg() != null ? variant.getWeightKg() : new java.math.BigDecimal("1.0"));
            ps.setBoolean(7, variant.getIsActive());
            ps.setBigDecimal(8, variant.getDiscountPrice());
            ps.setTimestamp(9, variant.getDiscountStart() != null ? Timestamp.valueOf(variant.getDiscountStart()) : null);
            ps.setTimestamp(10, variant.getDiscountEnd() != null ? Timestamp.valueOf(variant.getDiscountEnd()) : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu biến thể sản phẩm thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Cập nhật thông tin của biến thể sản phẩm.
     */
    public void update(ProductVariant variant) throws SQLException {
        String sql = "UPDATE product_variants SET sku = ?, variant_label = ?, price = ?, stock_quantity = ?, weight_kg = ?, is_active = ?, discount_price = ?, discount_start = ?, discount_end = ?, updated_at = GETDATE() WHERE variant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, variant.getSku());
            ps.setString(2, variant.getVariantLabel());
            ps.setBigDecimal(3, variant.getPrice());
            ps.setInt(4, variant.getStockQuantity());
            ps.setBigDecimal(5, variant.getWeightKg() != null ? variant.getWeightKg() : new java.math.BigDecimal("1.0"));
            ps.setBoolean(6, variant.getIsActive());
            ps.setBigDecimal(7, variant.getDiscountPrice());
            ps.setTimestamp(8, variant.getDiscountStart() != null ? Timestamp.valueOf(variant.getDiscountStart()) : null);
            ps.setTimestamp(9, variant.getDiscountEnd() != null ? Timestamp.valueOf(variant.getDiscountEnd()) : null);
            ps.setInt(10, variant.getVariantId());
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật số lượng tồn kho (tăng hoặc giảm) của biến thể sản phẩm.
     */
    public void updateStock(int variantId, int delta) throws SQLException {
        try (Connection conn = getConnection()) {
            updateStock(conn, variantId, delta);
        }
    }

    /**
     * Cập nhật số lượng tồn kho (tăng hoặc giảm) trong một Transaction Connection.
     */
    public void updateStock(Connection conn, int variantId, int delta) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = stock_quantity + ?, updated_at = GETDATE() WHERE variant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, variantId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cập nhật tồn kho thất bại. Biến thể sản phẩm không tồn tại hoặc đã bị xóa.");
            }
        }
    }

    /**
     * Hủy hoạt động (Soft delete) của một biến thể sản phẩm.
     */
    public void deactivate(int variantId) throws SQLException {
        String sql = "UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE variant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại của một biến thể sản phẩm.
     */
    public int getStockQuantity(int variantId) throws SQLException {
        try (Connection conn = getConnection()) {
            return getStockQuantity(conn, variantId);
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại trong một Transaction Connection.
     */
    public int getStockQuantity(Connection conn, int variantId) throws SQLException {
        String sql = "SELECT stock_quantity FROM product_variants WHERE variant_id = ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        throw new SQLException("Biến thể sản phẩm không tồn tại hoặc không hoạt động.");
    }

    /**
     * Trừ kho của biến thể sản phẩm nguyên tử (atomic update check)
     * và trả về số lượng tồn kho mới sau khi trừ.
     */
    public int decrementStockQuantity(Connection conn, int variantId, int qty) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = stock_quantity - ?, updated_at = GETDATE() "
                   + "OUTPUT INSERTED.stock_quantity "
                   + "WHERE variant_id = ? AND stock_quantity >= ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, variantId);
            ps.setInt(3, qty);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        throw new SQLException("Không đủ số lượng hàng tồn kho cho sản phẩm hoặc sản phẩm không hoạt động.");
    }

    public int getProductId(Connection conn, int variantId) throws SQLException {
        String sql = "SELECT product_id FROM product_variants WHERE variant_id = ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }
        }
        throw new SQLException("Không tìm thấy sản phẩm của biến thể này hoặc biến thể không hoạt động.");
    }

    /** Ánh xạ ResultSet -> ProductVariant */
    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        ProductVariant pv = new ProductVariant();
        pv.setVariantId(rs.getInt("variant_id"));
        pv.setProductId(rs.getInt("product_id"));
        pv.setSku(rs.getString("sku"));
        pv.setVariantLabel(rs.getString("variant_label"));
        pv.setPrice(rs.getBigDecimal("price"));
        pv.setStockQuantity(rs.getInt("stock_quantity"));
        pv.setIsActive(rs.getBoolean("is_active"));
        pv.setWeightKg(rs.getBigDecimal("weight_kg"));
        pv.setDiscountPrice(rs.getBigDecimal("discount_price"));
        
        Timestamp discStartVal = rs.getTimestamp("discount_start");
        if (discStartVal != null) {
            pv.setDiscountStart(discStartVal.toLocalDateTime());
        }
        
        Timestamp discEndVal = rs.getTimestamp("discount_end");
        if (discEndVal != null) {
            pv.setDiscountEnd(discEndVal.toLocalDateTime());
        }
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            pv.setCreatedAt(createdAtVal.toLocalDateTime());
        }
        
        Timestamp updatedAtVal = rs.getTimestamp("updated_at");
        if (updatedAtVal != null) {
            pv.setUpdatedAt(updatedAtVal.toLocalDateTime());
        }
        
        return pv;
    }

    public int getProductOwnerId(Connection conn, int variantId) throws SQLException {
        String sql = "SELECT p.owner_id FROM product_variants pv JOIN products p ON pv.product_id = p.product_id WHERE pv.variant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("owner_id");
                }
            }
        }
        throw new SQLException("Không tìm thấy chủ sở hữu của biến thể sản phẩm này.");
    }

    public int getProductOwnerId(int variantId) throws SQLException {
        try (Connection conn = getConnection()) {
            return getProductOwnerId(conn, variantId);
        }
    }

    public List<Map<String, Object>> findVariantsWithOwnerDetails(int ownerId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT pv.variant_id, pv.variant_label, pv.stock_quantity, pv.sku, "
                   + "       p.product_id, p.name AS product_name, p.shelf_life_days, "
                   + "       p.season_start_month, p.season_end_month "
                   + "FROM product_variants pv "
                   + "JOIN products p ON pv.product_id = p.product_id "
                   + "WHERE p.owner_id = ? AND pv.is_active = 1 AND p.status != 'DELETED' "
                   + "ORDER BY p.name ASC, pv.price ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("variantId", rs.getInt("variant_id"));
                    map.put("variantLabel", rs.getString("variant_label"));
                    map.put("stockQuantity", rs.getInt("stock_quantity"));
                    map.put("sku", rs.getString("sku"));
                    
                    int productId = rs.getInt("product_id");
                    map.put("productId", productId);
                    map.put("productName", rs.getString("product_name"));
                    
                    int shelfLife = rs.getInt("shelf_life_days");
                    if (rs.wasNull()) {
                        map.put("shelfLifeDays", null);
                    } else {
                        map.put("shelfLifeDays", shelfLife);
                    }
                    
                    model.entity.catalog.Product dummyProduct = new model.entity.catalog.Product();
                    dummyProduct.setProductId(productId);
                    dummyProduct.setName(rs.getString("product_name"));
                    
                    int startMonth = rs.getInt("season_start_month");
                    boolean startNull = rs.wasNull();
                    int endMonth = rs.getInt("season_end_month");
                    boolean endNull = rs.wasNull();
                    
                    if (!startNull) {
                        dummyProduct.setSeasonStartMonth(startMonth);
                    }
                    if (!endNull) {
                        dummyProduct.setSeasonEndMonth(endMonth);
                    }
                    
                    map.put("seasonLabel", dummyProduct.getSeasonLabel());
                    map.put("inSeason", dummyProduct.isInSeason());
                    
                    list.add(map);
                }
            }
        }
        return list;
    }

    public Map<String, Object> getVariantAndProductName(Connection conn, int variantId) throws SQLException {
        String sql = "SELECT pv.sku, p.name FROM product_variants pv JOIN products p ON pv.product_id = p.product_id WHERE pv.variant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sku", rs.getString("sku"));
                    map.put("name", rs.getString("name"));
                    return map;
                }
            }
        }
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("sku", "Unknown SKU");
        fallback.put("name", "Sản phẩm");
        return fallback;
    }
}

