package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserManagementDAO chuyên xử lý các thao tác quản lý người dùng.
 * Bao gồm lấy danh sách, khóa/mở khóa tài khoản (Cập nhật status).
 */
public class UserManagementDAO extends BaseDAO {

    /**
     * Lấy danh sách toàn bộ người dùng trong hệ thống (ngoại trừ ADMIN).
     * Sắp xếp theo thứ tự đăng ký mới nhất lên đầu.
     */
    public List<Map<String, Object>> getAllUsers() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        // Lấy tất cả user, ngoại trừ các tài khoản Admin để tránh tình trạng Admin tự khóa chính mình
        String sql = "SELECT user_id, full_name, email, phone, role, status, created_at " +
                     "FROM users WHERE role != 'ADMIN' ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("user_id", rs.getInt("user_id"));
                user.put("full_name", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                user.put("phone", rs.getString("phone"));
                user.put("role", rs.getString("role"));
                user.put("status", rs.getString("status"));
                user.put("created_at", rs.getTimestamp("created_at"));
                list.add(user);
            }
        }
        return list;
    }

    /**
     * Thay đổi trạng thái của tài khoản (Khóa hoặc Mở khóa).
     * @param userId ID của người dùng
     * @param newStatus Trạng thái mới (VD: 'ACTIVE' để mở khóa, hoặc 'SUSPENDED' để khóa)
     * @return true nếu CSDL update thành công ít nhất 1 dòng
     */
    public boolean updateUserStatus(int userId, String newStatus) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
