package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Promotion;
import java.sql.*;
import java.util.*;

/**
 * PromotionDAO — DAO cho entity Promotion.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class PromotionDAO extends BaseDAO {

    /**
     * Tìm khuyến mãi theo ID.
     */
    public Promotion findById(int id) throws SQLException {
        String sql = "SELECT * FROM promotions WHERE promo_id = ? AND is_deleted = 0";
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
     * Tìm khuyến mãi theo Code.
     */
    public Promotion findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM promotions WHERE code = ? AND is_deleted = 0 AND is_active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm khuyến mãi do chủ cửa hàng cụ thể tạo ra.
     */
    public List<Promotion> findByOwner(int ownerId) throws SQLException {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions WHERE created_by = ? AND is_deleted = 0 ORDER BY promo_id DESC";
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
     * Lấy danh sách khuyến mãi đang hoạt động áp dụng cho một sản phẩm cụ thể (để xác định Flash Sale).
     */
    public List<Promotion> findActivePromotionsByProduct(int productId) throws SQLException {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions WHERE product_id = ? AND scope = 'PRODUCT' "
                   + "AND is_active = 1 AND is_deleted = 0 AND valid_from <= GETDATE() AND valid_until >= GETDATE()";
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
     * Lưu một khuyến mãi mới vào cơ sở dữ liệu.
     */
    public int save(Promotion promo) throws SQLException {
        String sql = "INSERT INTO promotions (code, discount_type, discount_scope, discount_max, discount_value, min_order_value, scope, product_id, max_uses, used_count, can_stack, valid_from, valid_until, created_by, created_at, updated_at, is_deleted, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 0, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, promo.getCode());
            ps.setString(2, promo.getDiscountType());
            ps.setString(3, promo.getDiscountScope());
            ps.setBigDecimal(4, promo.getDiscountMax());
            ps.setBigDecimal(5, promo.getDiscountValue());
            ps.setBigDecimal(6, promo.getMinOrderValue());
            ps.setString(7, promo.getScope());
            
            if (promo.getProductId() != null) {
                ps.setInt(8, promo.getProductId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            if (promo.getMaxUses() != null) {
                ps.setInt(9, promo.getMaxUses());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            
            ps.setInt(10, promo.getUsedCount());
            ps.setBoolean(11, promo.getCanStack());
            
            ps.setTimestamp(12, Timestamp.valueOf(promo.getValidFrom()));
            ps.setTimestamp(13, Timestamp.valueOf(promo.getValidUntil()));
            ps.setInt(14, promo.getCreatedBy());
            ps.setBoolean(15, promo.getIsActive());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu khuyến mãi thất bại, không lấy được mã khóa tự tăng.");
    }

    /**
     * Cập nhật thông tin của khuyến mãi.
     */
    public void update(Promotion promo) throws SQLException {
        String sql = "UPDATE promotions SET code = ?, discount_type = ?, discount_scope = ?, discount_max = ?, discount_value = ?, min_order_value = ?, scope = ?, product_id = ?, max_uses = ?, used_count = ?, can_stack = ?, valid_from = ?, valid_until = ?, is_active = ?, updated_at = GETDATE() WHERE promo_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, promo.getCode());
            ps.setString(2, promo.getDiscountType());
            ps.setString(3, promo.getDiscountScope());
            ps.setBigDecimal(4, promo.getDiscountMax());
            ps.setBigDecimal(5, promo.getDiscountValue());
            ps.setBigDecimal(6, promo.getMinOrderValue());
            ps.setString(7, promo.getScope());
            
            if (promo.getProductId() != null) {
                ps.setInt(8, promo.getProductId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            if (promo.getMaxUses() != null) {
                ps.setInt(9, promo.getMaxUses());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            
            ps.setInt(10, promo.getUsedCount());
            ps.setBoolean(11, promo.getCanStack());
            ps.setTimestamp(12, Timestamp.valueOf(promo.getValidFrom()));
            ps.setTimestamp(13, Timestamp.valueOf(promo.getValidUntil()));
            ps.setBoolean(14, promo.getIsActive());
            ps.setInt(15, promo.getPromoId());
            ps.executeUpdate();
        }
    }

    /**
     * Tăng số lượng đã sử dụng của mã khuyến mãi lên 1 đơn vị.
     */
    public void incrementUsedCount(int promoId) throws SQLException {
        String sql = "UPDATE promotions SET used_count = used_count + 1, updated_at = GETDATE() WHERE promo_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ps.executeUpdate();
        }
    }

    /**
     * Hủy kích hoạt một chương trình khuyến mãi.
     */
    public void deactivate(int promoId) throws SQLException {
        String sql = "UPDATE promotions SET is_active = 0, updated_at = GETDATE() WHERE promo_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ps.executeUpdate();
        }
    }

    /** Ánh xạ ResultSet -> Promotion */
    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setPromoId(rs.getInt("promo_id"));
        p.setCode(rs.getString("code"));
        p.setDiscountType(rs.getString("discount_type"));
        p.setDiscountScope(rs.getString("discount_scope"));
        p.setDiscountMax(rs.getBigDecimal("discount_max"));
        p.setDiscountValue(rs.getBigDecimal("discount_value"));
        p.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        p.setScope(rs.getString("scope"));
        
        int productId = rs.getInt("product_id");
        p.setProductId(rs.wasNull() ? null : productId);
        
        int maxUses = rs.getInt("max_uses");
        p.setMaxUses(rs.wasNull() ? null : maxUses);
        
        p.setUsedCount(rs.getInt("used_count"));
        p.setCanStack(rs.getBoolean("can_stack"));
        
        Timestamp validFromVal = rs.getTimestamp("valid_from");
        if (validFromVal != null) {
            p.setValidFrom(validFromVal.toLocalDateTime());
        }
        
        Timestamp validUntilVal = rs.getTimestamp("valid_until");
        if (validUntilVal != null) {
            p.setValidUntil(validUntilVal.toLocalDateTime());
        }
        
        p.setCreatedBy(rs.getInt("created_by"));
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            p.setCreatedAt(createdAtVal.toLocalDateTime());
        }
        
        Timestamp updatedAtVal = rs.getTimestamp("updated_at");
        if (updatedAtVal != null) {
            p.setUpdatedAt(updatedAtVal.toLocalDateTime());
        }
        
        p.setIsDeleted(rs.getBoolean("is_deleted"));
        p.setIsActive(rs.getBoolean("is_active"));
        
        return p;
    }
}
