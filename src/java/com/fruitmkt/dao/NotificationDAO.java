package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.Notification;
import java.sql.*;
import java.util.*;

public class NotificationDAO extends BaseDAO {

    public List<Notification> findByUser(int userId, boolean unreadOnly) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? " + (unreadOnly ? "AND is_read = 0 " : "") + "ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Notification> findAllSystemNotifications() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE notification_type = 'SYSTEM' ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void insertForRole(String title, String message, String role) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) " +
                     "SELECT user_id, ?, ?, 'SYSTEM', 0, GETDATE() FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public void insertForAll(String title, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) " +
                     "SELECT user_id, ?, ?, 'SYSTEM', 0, GETDATE() FROM users";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("notification_type"));
        
        n.setIsRead(rs.getBoolean("is_read"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) n.setCreatedAt(createdAt.toLocalDateTime());
        
        return n;
    }
}
