package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ReplenishmentLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReplenishmentLogDAO — Handles DB operations for the replenishment_logs table.
 * Adheres to rule of keeping SQL statements purely in the DAO layer.
 */
public class ReplenishmentLogDAO extends BaseDAO {

    /**
     * Exposes connection for starting standard JDBC transactions in Service.
     */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }

    /**
     * Saves a replenishment log entry using an active transactional connection.
     */
    public int save(Connection conn, ReplenishmentLog log) throws SQLException {
        String sql = "INSERT INTO replenishment_logs (variant_id, replenished_by, quantity, supplier_details, replenishment_date, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getVariantId());
            ps.setInt(2, log.getReplenishedBy());
            ps.setInt(3, log.getQuantity());
            if (log.getSupplierDetails() != null) {
                ps.setString(4, log.getSupplierDetails());
            } else {
                ps.setNull(4, Types.NVARCHAR);
            }
            ps.setDate(5, Date.valueOf(log.getReplenishmentDate()));
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu lịch sử replenishment thất bại, không lấy được mã tự tăng.");
    }

    /**
     * Retrieves all replenishment history logs of products owned by a specific Shop Owner.
     */
    public List<ReplenishmentLog> findByOwner(int ownerId) throws SQLException {
        List<ReplenishmentLog> list = new ArrayList<>();
        String sql = "SELECT rl.*, p.name AS product_name, pv.variant_label, u.full_name AS user_name "
                   + "FROM replenishment_logs rl "
                   + "JOIN product_variants pv ON rl.variant_id = pv.variant_id "
                   + "JOIN products p ON pv.product_id = p.product_id "
                   + "JOIN users u ON rl.replenished_by = u.user_id "
                   + "WHERE p.owner_id = ? "
                   + "ORDER BY rl.created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Map ResultSet row to ReplenishmentLog entity.
     */
    private ReplenishmentLog mapRowWithDetails(ResultSet rs) throws SQLException {
        ReplenishmentLog rl = new ReplenishmentLog();
        rl.setLogId(rs.getInt("log_id"));
        rl.setVariantId(rs.getInt("variant_id"));
        rl.setReplenishedBy(rs.getInt("replenished_by"));
        rl.setQuantity(rs.getInt("quantity"));
        rl.setSupplierDetails(rs.getString("supplier_details"));
        
        Date dateVal = rs.getDate("replenishment_date");
        if (dateVal != null) {
            rl.setReplenishmentDate(dateVal.toLocalDate());
        }
        
        Timestamp createdAtVal = rs.getTimestamp("created_at");
        if (createdAtVal != null) {
            rl.setCreatedAt(createdAtVal.toLocalDateTime());
        }
        
        // Helper fields
        rl.setProductName(rs.getString("product_name"));
        rl.setVariantLabel(rs.getString("variant_label"));
        rl.setReplenishedByName(rs.getString("user_name"));
        
        return rl;
    }
}
