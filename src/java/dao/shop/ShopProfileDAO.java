package dao.shop;

import dao.system.BaseDAO;
import model.entity.shop.ShopProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.PaginationHelper;

/**
 * ShopProfileDAO — DAO cho entity ShopProfile.
 *
 * QUY TẮC:
 * - Chỉ chứa SQL, không chứa business logic
 * - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 * - Mỗi method ném SQLException để Service xử lý
 * - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ShopProfileDAO extends BaseDAO {

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
     * Tìm shop profile duy nhất theo ID người dùng.
     */
    public ShopProfile findOneByUserId(int userId) throws SQLException {
        try (Connection conn = getConnection()) {
            return findOneByUserId(conn, userId);
        }
    }

    public ShopProfile findOneByUserId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM shop_owner_profiles WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
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

    public List<ShopProfile> findAll(String approvalStatus, int page, int pageSize) throws SQLException {
        List<ShopProfile> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM shop_owner_profiles ");
        if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
            sql.append("WHERE approval_status = ? ");
        }
        sql.append("ORDER BY created_at DESC ")
                .append(PaginationHelper.OFFSET_FETCH_SQL);

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
                ps.setString(paramIndex++, approvalStatus);
            }
            paramIndex = PaginationHelper.bindOffsetFetch(ps, paramIndex, page, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countAll(String approvalStatus) throws SQLException {
        String sql = (approvalStatus == null || approvalStatus.trim().isEmpty())
                ? "SELECT COUNT(*) FROM shop_owner_profiles"
                : "SELECT COUNT(*) FROM shop_owner_profiles WHERE approval_status = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
                ps.setString(1, approvalStatus);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
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
     * Batch load shop profiles theo danh sách user_id.
     */
    public Map<Integer, ShopProfile> findByUserIds(Collection<Integer> userIds) throws SQLException {
        Map<Integer, ShopProfile> map = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(userIds);
        StringBuilder placeholders = new StringBuilder();
        int size = distinctIds.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT * FROM shop_owner_profiles WHERE user_id IN (" + placeholders + ")";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer userId : distinctIds) {
                ps.setInt(paramIndex++, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShopProfile profile = mapRow(rs);
                    map.put(profile.getUserId(), profile);
                }
            }
        }
        return map;
    }

    /**
     * Xóa shop profile theo user_id.
     */
    public void deleteByUserId(int userId) throws SQLException {
        try (Connection conn = getConnection()) {
            deleteByUserId(conn, userId);
        }
    }

    public void deleteByUserId(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM shop_owner_profiles WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Lưu mới một shop profile và trả về ID tự sinh.
     */
    public int save(ShopProfile profile) throws SQLException {
        try (Connection conn = getConnection()) {
            return save(conn, profile);
        }
    }

    public int save(Connection conn, ShopProfile profile) throws SQLException {
        ShopProfile existing = findOneByUserId(conn, profile.getUserId());
        if (existing != null) {
            if ("REJECTED".equals(existing.getApprovalStatus())) {
                deleteByUserId(conn, profile.getUserId());
            } else {
                throw new SQLException("User " + profile.getUserId() + " đã có shop profile ở trạng thái "
                        + existing.getApprovalStatus());
            }
        }

        String sql = "INSERT INTO shop_owner_profiles "
                + "(user_id, shop_name, shop_description, approval_status, rejection_reason, "
                + "approved_at, delivery_address, rating, preferred_categories, doc_paths, business_email, logo_url, cover_url, expiry_warning_days, low_stock_threshold, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            ps.setInt(14, profile.getExpiryWarningDays() > 0 ? profile.getExpiryWarningDays() : 3);
            ps.setInt(15, profile.getLowStockThreshold() > 0 ? profile.getLowStockThreshold() : 5);

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
        try (Connection conn = getConnection()) {
            update(conn, profile);
        }
    }

    public void update(Connection conn, ShopProfile profile) throws SQLException {
        if (profile.getProfileId() <= 0) {
            ShopProfile existing = findOneByUserId(conn, profile.getUserId());
            if (existing == null) {
                throw new SQLException("Không tìm thấy shop profile để cập nhật.");
            }
            profile.setProfileId(existing.getProfileId());
        }

        String sql = "UPDATE shop_owner_profiles SET shop_name = ?, shop_description = ?, approval_status = ?, "
                + "rejection_reason = ?, approved_at = ?, delivery_address = ?, rating = ?, "
                + "preferred_categories = ?, doc_paths = ?, business_email = ?, logo_url = ?, cover_url = ?, expiry_warning_days = ?, low_stock_threshold = ?, updated_at = GETDATE() WHERE profile_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            ps.setInt(13, profile.getExpiryWarningDays() > 0 ? profile.getExpiryWarningDays() : 3);
            ps.setInt(14, profile.getLowStockThreshold() > 0 ? profile.getLowStockThreshold() : 5);
            ps.setInt(15, profile.getProfileId());
            ps.executeUpdate();
        }
    }

    /**
     * Duyệt hoặc từ chối phê duyệt shop.
     * [BUGFIX] Khi APPROVED: cập nhật đồng thời users.role = 'SHOP_OWNER' trong
     * cùng 1 transaction.
     * TRANSACTION: Nếu bất kỳ UPDATE nào thất bại, cả hai sẽ rollback → dữ liệu
     * nhất quán.
     */
    public void updateApprovalStatus(int profileId, int userId, String status, String rejectionReason)
            throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Bước 1: Cập nhật trạng thái duyệt trên shop_owner_profiles
                String sqlProfile = "UPDATE shop_owner_profiles "
                        + "SET approval_status = ?, rejection_reason = ?, approved_at = ?, updated_at = GETDATE() "
                        + "WHERE profile_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
                    ps.setString(1, status);
                    ps.setString(2, rejectionReason);
                    ps.setTimestamp(3, "APPROVED".equals(status) ? new Timestamp(System.currentTimeMillis()) : null);
                    ps.setInt(4, profileId);
                    int profileRows = ps.executeUpdate();
                    if (profileRows == 0) {
                        throw new SQLException("Không tìm thấy shop profile #" + profileId);
                    }
                }

                // Bước 2: Nếu APPROVED → đổi role user thành SHOP_OWNER
                if ("APPROVED".equals(status) && userId > 0) {
                    String sqlUser = "UPDATE users SET role = 'SHOP_OWNER', updated_at = GETDATE() WHERE user_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                        ps.setInt(1, userId);
                        int rows = ps.executeUpdate();
                        if (rows == 0) {
                            throw new SQLException(
                                    "[CRITICAL] Cập nhật role SHOP_OWNER thất bại cho user_id=" + userId);
                        }
                    }
                } else if ("APPROVED".equals(status)) {
                    throw new SQLException("userId không hợp lệ cho thao tác APPROVED.");
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
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
        p.setExpiryWarningDays(rs.getInt("expiry_warning_days"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));

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
        try (Connection conn = getConnection()) {
            return isBusinessEmailExists(conn, businessEmail, 0);
        }
    }

    public boolean isBusinessEmailExists(Connection conn, String businessEmail, int excludeUserId) throws SQLException {
        if (businessEmail == null || businessEmail.trim().isEmpty()) {
            return false;
        }
        String sql = excludeUserId > 0
                ? "SELECT COUNT(*) FROM shop_owner_profiles WHERE business_email = ? AND user_id <> ?"
                : "SELECT COUNT(*) FROM shop_owner_profiles WHERE business_email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, businessEmail.trim());
            if (excludeUserId > 0) {
                ps.setInt(2, excludeUserId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
