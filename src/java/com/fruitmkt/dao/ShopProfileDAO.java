package com.fruitmkt.dao;

import com.fruitmkt.dao.BaseDAO;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ShopProfileDAO — DAO cho entity ShopProfile.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ShopProfileDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(ShopProfileDAO.class.getName());
    private static boolean schemaChecked = false;

    public ShopProfileDAO() {
        checkSchemaOnce();
    }

    private synchronized void checkSchemaOnce() {
        if (schemaChecked) return;
        try (Connection conn = getConnection()) {
            // Check business_email
            boolean emailColExists = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT TOP 0 business_email FROM shop_owner_profiles")) {
                emailColExists = true;
            } catch (SQLException e) {
                LoggerUtil.warn(log, "business_email column not yet present, will attempt to add", e);
            }
            if (!emailColExists) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE shop_owner_profiles ADD business_email NVARCHAR(255) NULL");
                    LoggerUtil.info(log, "[DB Migrator] Success: Added business_email column.");
                }
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("SET QUOTED_IDENTIFIER ON; CREATE UNIQUE NONCLUSTERED INDEX UX_shop_owner_profiles_business_email ON shop_owner_profiles(business_email) WHERE business_email IS NOT NULL");
                    LoggerUtil.info(log, "[DB Migrator] Success: Created filtered unique index on business_email.");
                } catch (SQLException ex) {
                    LoggerUtil.warn(log, "[DB Migrator] Warning creating index on business_email", ex);
                }
            }

            // Check logo_url
            boolean logoColExists = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT TOP 0 logo_url FROM shop_owner_profiles")) {
                logoColExists = true;
            } catch (SQLException e) {
                LoggerUtil.warn(log, "logo_url column not yet present, will attempt to add", e);
            }
            if (!logoColExists) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE shop_owner_profiles ADD logo_url NVARCHAR(500) NULL");
                    LoggerUtil.info(log, "[DB Migrator] Success: Added logo_url column.");
                }
            }

            // Check cover_url
            boolean coverColExists = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT TOP 0 cover_url FROM shop_owner_profiles")) {
                coverColExists = true;
            } catch (SQLException e) {
                LoggerUtil.warn(log, "cover_url column not yet present, will attempt to add", e);
            }
            if (!coverColExists) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE shop_owner_profiles ADD cover_url NVARCHAR(500) NULL");
                    LoggerUtil.info(log, "[DB Migrator] Success: Added cover_url column.");
                }
            }

            schemaChecked = true;
        } catch (SQLException e) {
            LoggerUtil.error(log, "[DB Migrator] Error checking/adding shop_owner_profiles columns", e);
            schemaChecked = true;
        }
    }

    /**
     * Tìm shop profile theo ID người dùng.
     */
    public List<ShopProfile> findByUserId(int userId) throws SQLException {
        List<ShopProfile> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_owner_profiles WHERE user_id = ?";
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

    /**
     * Tìm shop profile theo ID profile.
     */
    public ShopProfile findById(int profileId) throws SQLException {
        String sql = "SELECT * FROM shop_owner_profiles WHERE profile_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy tất cả shop profile theo trạng thái duyệt.
     */
    public List<ShopProfile> findAll(String approvalStatus) throws SQLException {
        List<ShopProfile> list = new ArrayList<>();
        String sql = (approvalStatus == null)
            ? "SELECT * FROM shop_owner_profiles ORDER BY created_at DESC"
            : "SELECT * FROM shop_owner_profiles WHERE approval_status = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (approvalStatus != null) {
                ps.setString(1, approvalStatus);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Lấy toàn bộ shop profiles (không lọc) */
    public List<ShopProfile> findAll() throws SQLException {
        return findAll(null);
    }

    /** Lọc theo approvalStatus — alias ngắn hơn cho admin code */
    public List<ShopProfile> findByApprovalStatus(String status) throws SQLException {
        return findAll(status);
    }

    /**
     * Lưu mới một shop profile và trả về ID tự sinh.
     */
    public int save(ShopProfile profile) throws SQLException {
        String sql = "INSERT INTO shop_owner_profiles "
                   + "(user_id, shop_name, shop_description, approval_status, rejection_reason, "
                   + "approved_at, delivery_address, rating, preferred_categories, doc_paths, business_email, logo_url, cover_url, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getShopName());
            ps.setString(3, profile.getShopDescription());
            ps.setString(4, profile.getApprovalStatus() != null ? profile.getApprovalStatus() : "PENDING");
            ps.setString(5, profile.getRejectionReason());
            ps.setTimestamp(6, profile.getApprovedAt() != null ? Timestamp.valueOf(profile.getApprovedAt()) : null);
            ps.setString(7, profile.getDeliveryAddress());
            ps.setBigDecimal(8, profile.getRating() != null ? profile.getRating() : java.math.BigDecimal.ZERO);
            ps.setString(9, profile.getPreferredCategories());
            ps.setString(10, profile.getDocPaths());
            ps.setString(11, profile.getBusinessEmail());
            ps.setString(12, profile.getLogoUrl());
            ps.setString(13, profile.getCoverUrl());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    profile.setProfileId(generatedId);
                    return generatedId;
                }
            }
        }
        throw new SQLException("Lỗi khi thêm mới shop profile, không lấy được khóa tự tăng.");
    }

    /**
     * Cập nhật thông tin shop profile.
     */
    public void update(ShopProfile profile) throws SQLException {
        String sql = "UPDATE shop_owner_profiles SET shop_name = ?, shop_description = ?, approval_status = ?, "
                   + "rejection_reason = ?, approved_at = ?, delivery_address = ?, rating = ?, "
                   + "preferred_categories = ?, doc_paths = ?, business_email = ?, logo_url = ?, cover_url = ?, updated_at = GETDATE() WHERE profile_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getShopName());
            ps.setString(2, profile.getShopDescription());
            ps.setString(3, profile.getApprovalStatus());
            ps.setString(4, profile.getRejectionReason());
            ps.setTimestamp(5, profile.getApprovedAt() != null ? Timestamp.valueOf(profile.getApprovedAt()) : null);
            ps.setString(6, profile.getDeliveryAddress());
            ps.setBigDecimal(7, profile.getRating());
            ps.setString(8, profile.getPreferredCategories());
            ps.setString(9, profile.getDocPaths());
            ps.setString(10, profile.getBusinessEmail());
            ps.setString(11, profile.getLogoUrl());
            ps.setString(12, profile.getCoverUrl());
            ps.setInt(13, profile.getProfileId());
            ps.executeUpdate();
        }
    }

    /**
     * Duyệt hoặc từ chối phê duyệt shop.
     * [BUGFIX] Khi APPROVED: cập nhật đồng thời users.role = 'SHOP_OWNER'.
     * Dùng hai UPDATE riêng vì MSSQL không hỗ trợ UPDATE multi-table.
     * Nếu update thứ 2 thất bại sẽ throw lên để Service rollback logic.
     */
    public void updateApprovalStatus(int profileId, int userId, String status, String rejectionReason) throws SQLException {
        // Bước 1: Cập nhật trạng thái duyệt trên shop_owner_profiles
        String sqlProfile = "UPDATE shop_owner_profiles "
                + "SET approval_status = ?, rejection_reason = ?, approved_at = ?, updated_at = GETDATE() "
                + "WHERE profile_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
            ps.setString(1, status);
            ps.setString(2, rejectionReason);
            ps.setTimestamp(3, "APPROVED".equals(status) ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(4, profileId);
            ps.executeUpdate();
        }

        // Bước 2: Nếu APPROVED → đổi role user thành SHOP_OWNER
        if ("APPROVED".equals(status) && userId > 0) {
            String sqlUser = "UPDATE users SET role = 'SHOP_OWNER', updated_at = GETDATE() WHERE user_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("[CRITICAL] Cập nhật role SHOP_OWNER thất bại cho user_id=" + userId);
                }
            }
        }
    }

    /**
     * Cập nhật đường dẫn tài liệu upload sau khi đăng ký.
     */
    public void updateDocPaths(int profileId, String jsonDocPaths) throws SQLException {
        String sql = "UPDATE shop_owner_profiles SET doc_paths = ?, updated_at = GETDATE() WHERE profile_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jsonDocPaths);
            ps.setInt(2, profileId);
            ps.executeUpdate();
        }
    }

    /** Ánh xạ ResultSet -> ShopProfile — gọi trong mọi query SELECT */
    private ShopProfile mapRow(ResultSet rs) throws SQLException {
        ShopProfile p = new ShopProfile();
        p.setProfileId(rs.getInt("profile_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setShopName(rs.getString("shop_name"));
        p.setShopDescription(rs.getString("shop_description"));
        p.setApprovalStatus(rs.getString("approval_status"));
        p.setRejectionReason(rs.getString("rejection_reason"));
        
        Timestamp approvedAtTs = rs.getTimestamp("approved_at");
        if (approvedAtTs != null) {
            p.setApprovedAt(approvedAtTs.toLocalDateTime());
        }
        p.setDeliveryAddress(rs.getString("delivery_address"));
        p.setRating(rs.getBigDecimal("rating"));
        p.setPreferredCategories(rs.getString("preferred_categories"));
        p.setDocPaths(rs.getString("doc_paths"));
        p.setBusinessEmail(rs.getString("business_email"));
        p.setLogoUrl(rs.getString("logo_url"));
        p.setCoverUrl(rs.getString("cover_url"));
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            p.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            p.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }
        return p;
    }

    /**
     * Kiểm tra xem email doanh nghiệp đã được sử dụng hay chưa.
     */
    public boolean isBusinessEmailExists(String businessEmail) throws SQLException {
        if (businessEmail == null || businessEmail.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM shop_owner_profiles WHERE business_email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, businessEmail.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
