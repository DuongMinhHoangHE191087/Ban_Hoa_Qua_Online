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
 * ShopApprovalDAO chuyên xử lý các thao tác liên quan đến xét duyệt gian hàng.
 * Bao gồm lấy danh sách chờ duyệt, thao tác duyệt (Approve) và từ chối (Reject).
 */
public class ShopApprovalDAO extends BaseDAO {

    /**
     * Lấy danh sách các cửa hàng đang chờ duyệt (approval_status = 'PENDING').
     * JOIN với bảng users để lấy thêm thông tin người đăng ký (tên, email, phone) cho Admin dễ nhìn.
     */
    public List<Map<String, Object>> getPendingShops() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        // Câu lệnh SQL Join để móc nối thông tin giữa hồ sơ shop và người dùng
        String sql = "SELECT s.profile_id, s.shop_name, s.shop_description, s.created_at, " +
                     "u.full_name, u.email, u.phone " +
                     "FROM shop_owner_profiles s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "WHERE s.approval_status = 'PENDING' " +
                     "ORDER BY s.created_at ASC"; // Ưu tiên xử lý những người đăng ký trước
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> shop = new HashMap<>();
                shop.put("profile_id", rs.getInt("profile_id"));
                shop.put("shop_name", rs.getString("shop_name"));
                shop.put("shop_description", rs.getString("shop_description"));
                shop.put("created_at", rs.getTimestamp("created_at"));
                shop.put("full_name", rs.getString("full_name"));
                shop.put("email", rs.getString("email"));
                shop.put("phone", rs.getString("phone"));
                list.add(shop);
            }
        }
        return list;
    }

    /**
     * Phê duyệt gian hàng.
     * Cập nhật trạng thái thành APPROVED, gán thời gian duyệt, 
     * ĐỒNG THỜI nâng cấp quyền của user lên thành SHOP_OWNER.
     * Sử dụng Transaction để bảo toàn dữ liệu.
     */
    public boolean approveShop(int profileId) throws SQLException {
        String getUserIdSql = "SELECT user_id FROM shop_owner_profiles WHERE profile_id = ?";
        String updateShopSql = "UPDATE shop_owner_profiles SET approval_status = 'APPROVED', approved_at = GETDATE() WHERE profile_id = ?";
        String updateUserSql = "UPDATE users SET role = 'SHOP_OWNER' WHERE user_id = ?";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lấy user_id
            int userId = -1;
            try (PreparedStatement psGet = conn.prepareStatement(getUserIdSql)) {
                psGet.setInt(1, profileId);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                    }
                }
            }

            if (userId == -1) return false;

            // 2. Duyệt Shop
            try (PreparedStatement psShop = conn.prepareStatement(updateShopSql)) {
                psShop.setInt(1, profileId);
                psShop.executeUpdate();
            }

            // 3. Nâng cấp quyền User
            try (PreparedStatement psUser = conn.prepareStatement(updateUserSql)) {
                psUser.setInt(1, userId);
                psUser.executeUpdate();
            }

            conn.commit(); // Hoàn tất Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); 
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Từ chối gian hàng với lý do cụ thể.
     */
    public boolean rejectShop(int profileId, String reason) throws SQLException {
        String sql = "UPDATE shop_owner_profiles SET approval_status = 'REJECTED', rejection_reason = ? WHERE profile_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, profileId);
            return ps.executeUpdate() > 0;
        }
    }
}
