package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.InventoryLog;
import java.sql.*;
import java.util.*;

/**
 * InventoryDAO — DAO cho entity InventoryLog.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class InventoryDAO extends BaseDAO {

    /**
     * Lấy toàn bộ lịch sử biến động kho của chủ cửa hàng.
     */
    public List<InventoryLog> findLogsByOwner(int ownerId) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT il.* FROM inventory_logs il "
                   + "JOIN product_variants pv ON il.variant_id = pv.variant_id "
                   + "JOIN products p ON pv.product_id = p.product_id "
                   + "WHERE p.owner_id = ? "
                   + "ORDER BY il.changed_at DESC";
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
     * Tìm lịch sử thay đổi theo variant_id.
     */
    public List<InventoryLog> findByVariant(int variantId) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_logs WHERE variant_id = ? ORDER BY changed_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lưu một bản ghi lịch sử thay đổi kho mới.
     */
    public int save(InventoryLog log) throws SQLException {
        String sql = "INSERT INTO inventory_logs (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, changed_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getVariantId());
            ps.setInt(2, log.getChangedBy());
            ps.setString(3, log.getChangeType());
            ps.setInt(4, log.getQuantityDelta());
            ps.setInt(5, log.getQuantityAfter());
            ps.setString(6, log.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Ánh xạ ResultSet -> InventoryLog — gọi trong mọi query SELECT */
    private InventoryLog mapRow(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setLogId(rs.getInt("log_id"));
        log.setVariantId(rs.getInt("variant_id"));
        log.setChangedBy(rs.getInt("changed_by"));
        log.setChangeType(rs.getString("change_type"));
        log.setQuantityDelta(rs.getInt("quantity_delta"));
        log.setQuantityAfter(rs.getInt("quantity_after"));
        log.setNote(rs.getString("note"));
        
        Timestamp changedAtVal = rs.getTimestamp("changed_at");
        if (changedAtVal != null) {
            log.setChangedAt(changedAtVal.toLocalDateTime());
        }
        return log;
    }
}
