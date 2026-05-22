package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ProductVariant;
import java.sql.*;
import java.util.*;

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
        String sql = "INSERT INTO product_variants (product_id, sku, variant_label, price, stock_quantity, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, variant.getProductId());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getVariantLabel());
            ps.setBigDecimal(4, variant.getPrice());
            ps.setInt(5, variant.getStockQuantity());
            ps.setBoolean(6, variant.getIsActive());
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
        String sql = "UPDATE product_variants SET sku = ?, variant_label = ?, price = ?, stock_quantity = ?, is_active = ?, updated_at = GETDATE() WHERE variant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, variant.getSku());
            ps.setString(2, variant.getVariantLabel());
            ps.setBigDecimal(3, variant.getPrice());
            ps.setInt(4, variant.getStockQuantity());
            ps.setBoolean(5, variant.getIsActive());
            ps.setInt(6, variant.getVariantId());
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật số lượng tồn kho (tăng hoặc giảm) của biến thể sản phẩm.
     */
    public void updateStock(int variantId, int delta) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = stock_quantity + ?, updated_at = GETDATE() WHERE variant_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, variantId);
            ps.executeUpdate();
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
}
