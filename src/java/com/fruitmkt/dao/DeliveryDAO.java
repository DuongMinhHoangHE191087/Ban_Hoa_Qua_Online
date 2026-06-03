package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Delivery;
import java.sql.*;
import java.util.*;

/**
 * DeliveryDAO — DAO cho entity Delivery.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class DeliveryDAO extends BaseDAO {

    public Delivery findById(int deliveryId) throws SQLException {
        String sql = "SELECT * FROM deliveries WHERE delivery_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deliveryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Delivery> findByStaffId(int staffId) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT * FROM deliveries WHERE staff_id = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void updateStatusAndProof(int deliveryId, String status, String failureReason, String proofImageUrl) throws SQLException {
        String sql = "UPDATE deliveries SET status = ?, failure_reason = ?, proof_image_url = ?, updated_at = GETDATE() ";
        if ("PICKED_UP".equals(status)) {
            sql += ", picked_up_at = GETDATE() ";
        } else if ("DELIVERED".equals(status)) {
            sql += ", delivered_at = GETDATE() ";
        }
        sql += "WHERE delivery_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, failureReason);
            ps.setString(3, proofImageUrl);
            ps.setInt(4, deliveryId);
            ps.executeUpdate();
        }
    }

    public void updateEstimatedTime(int deliveryId, java.time.LocalDateTime estimatedTime) throws SQLException {
        String sql = "UPDATE deliveries SET estimated_delivery_time = ?, updated_at = GETDATE() WHERE delivery_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, estimatedTime != null ? Timestamp.valueOf(estimatedTime) : null);
            ps.setInt(2, deliveryId);
            ps.executeUpdate();
        }
    }

    public void assignShipper(int orderId, int staffId, java.time.LocalDateTime estimatedTime) throws SQLException {
        String sql = "INSERT INTO deliveries (order_id, staff_id, status, estimated_delivery_time, created_at, updated_at) "
                   + "VALUES (?, ?, 'ASSIGNED', ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            if (staffId > 0) {
                ps.setInt(2, staffId);
            } else {
                ps.setNull(2, Types.INTEGER); // Chờ phân công
            }
            ps.setTimestamp(3, estimatedTime != null ? Timestamp.valueOf(estimatedTime) : null);
            ps.executeUpdate();
        }
    }

    public Delivery findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT TOP 1 * FROM deliveries WHERE order_id = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Delivery mapRow(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setDeliveryId(rs.getInt("delivery_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setStaffId(rs.getObject("staff_id") != null ? rs.getInt("staff_id") : null);
        d.setStatus(rs.getString("status"));
        
        Timestamp pickedUpAt = rs.getTimestamp("picked_up_at");
        if (pickedUpAt != null) d.setPickedUpAt(pickedUpAt.toLocalDateTime());
        
        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) d.setDeliveredAt(deliveredAt.toLocalDateTime());
        
        d.setFailureReason(rs.getString("failure_reason"));
        d.setProofImageUrl(rs.getString("proof_image_url"));
        
        Timestamp estTime = rs.getTimestamp("estimated_delivery_time");
        if (estTime != null) d.setEstimatedDeliveryTime(estTime.toLocalDateTime());
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) d.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) d.setUpdatedAt(updatedAt.toLocalDateTime());
        
        return d;
    }

    public Delivery findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM deliveries WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
}

