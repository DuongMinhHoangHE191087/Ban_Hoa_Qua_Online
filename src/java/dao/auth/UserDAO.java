package dao.auth;

import dao.system.BaseDAO;
import config.AppConfig;
import model.entity.auth.User;
import util.HashUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Set;
import util.LoggerUtil;

/**
 * UserDAO — DAO cho entity User.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class UserDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(UserDAO.class.getName());

    public List<User> findById(int id) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * TODO: Implement — findByEmail(String email)
     */
    public User findByEmail(String email) throws SQLException {
       // TODO: Viết SQL và xử lý ResultSet ở đây
        String sql = "SELECT * FROM users WHERE email = ?";
        try(Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return mapRow(rs);
                } else {
                    return null; // Không tìm thấy user nào với email này
                }
            }
        }
   }

   public User findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
   }

   public User findByLoginIdentifier(String identifier) throws SQLException {
        User user = findByEmail(identifier);
        if (user != null) {
            return user;
        }
        return findByPhone(identifier);
   }

    public User registerExternalUser(String email, String fullName) throws SQLException {
     String sql = "INSERT INTO users (full_name, email, password_hash, role, status, is_email_verified, avatar_url) VALUES (?, ?, ?, 'CUSTOMER', 'ACTIVE', 1, 'assets/images/default-avatar.svg')";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         
        ps.setString(1, fullName);
        ps.setString(2, email);
        
        // Random mật khẩu phức tạp để bảo báo người dùng không tự login được nếu ko dùng oauth (trừ khi dùng chức năng quên pass)
        String randomComplexPass = java.util.UUID.randomUUID().toString();
        ps.setString(3, HashUtil.hashPassword(randomComplexPass)); 
        
        int rows = ps.executeUpdate();
        if (rows > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                // Return user mới tạo
                User newUser = new User();
                newUser.setUserId(newId);
                newUser.setFullName(fullName);
                newUser.setEmail(email);
                newUser.setRole("CUSTOMER");
                newUser.setStatus("ACTIVE");
                newUser.setAvatarUrl("assets/images/default-avatar.svg");
                return newUser;
            }
        }
    }
    return null;
}

    public List<User> searchUsers(String role, String keyword, int offset, int limit) throws SQLException {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)");
            String likeKw = "%" + keyword.trim() + "%";
            params.add(likeKw); params.add(likeKw); params.add(likeKw);
        }
        sql.append(" ORDER BY user_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countUsers(String role, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)");
            String likeKw = "%" + keyword.trim() + "%";
            params.add(likeKw); params.add(likeKw); params.add(likeKw);
        }
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { return rs.getInt(1); }
            }
        }
        return 0;
    }

    /**
     * Retrieve all users.
     */
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * TODO: Implement — save(User user)
     */
    public int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role) throws SQLException {
        return saveNewCustomer(fullName, email, passwordHash, phone, role, AppConfig.ACCOUNT_STATUS_INACTIVE, false, "assets/images/default-avatar.svg");
    }

    public int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role, String status, boolean emailVerified) throws SQLException {
        return saveNewCustomer(fullName, email, passwordHash, phone, role, status, emailVerified, "assets/images/default-avatar.svg");
    }

    public int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role, String status, boolean emailVerified, String avatarUrl) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role, status, is_email_verified, avatar_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setString(4, phone);
            stmt.setString(5, role);
            stmt.setString(6, status);
            stmt.setBoolean(7, emailVerified);
            stmt.setString(8, avatarUrl != null ? avatarUrl : "assets/images/default-avatar.svg");
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Trả về ID của user mới tạo
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Cập nhật thông tin cơ bản của User
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ?, user_address = ?, avatar_url = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getUserAddress());
            stmt.setString(4, user.getAvatarUrl());
            stmt.setInt(5, user.getUserId());
            stmt.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái của User (Khóa/Mở khóa)
     */
    public boolean updateUserStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật vai trò (Role) của User (ví dụ: nâng cấp CUSTOMER thành SHOP_OWNER)
     */
    public void updateRole(int userId, String role) throws SQLException {
        String sql = "UPDATE users SET role = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * TODO: Implement — updatePassword(int userId, String newHash)
     */
    public void updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa OTP forgot-password sau khi đã dùng thành công.
     * Tái dùng cột email_verification — không cần ALTER TABLE.
     */
    public void clearForgotPasswordCode(int userId) throws SQLException {
        String sql = "UPDATE users SET email_verification_code_hash = NULL, "
                + "email_verification_expires_at = NULL, "
                + "email_verification_resend_at = NULL, "
                + "email_verification_sent_at = NULL, "
                + "updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void saveEmailVerificationCode(int userId, String codeHash, Timestamp expiresAt, Timestamp resendAt) throws SQLException {
        String sql = "UPDATE users SET email_verification_code_hash = ?, email_verification_expires_at = ?, email_verification_resend_at = ?, email_verification_sent_at = GETDATE(), updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codeHash);
            stmt.setTimestamp(2, expiresAt);
            stmt.setTimestamp(3, resendAt);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }

    public void activateVerifiedEmail(int userId) throws SQLException {
        String sql = "UPDATE users SET status = ?, is_email_verified = 1, email_verification_code_hash = NULL, email_verification_expires_at = NULL, email_verification_resend_at = NULL, email_verification_sent_at = NULL, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, AppConfig.ACCOUNT_STATUS_ACTIVE);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Tăng số lần đăng nhập sai của người dùng
     */
    public void incrementFailedLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_count = failed_login_count + 1, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Reset số lần đăng nhập sai và xóa khóa tài khoản
     */
    public void resetFailedLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_count = 0, locked_until = NULL, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Khóa tài khoản cho đến thời điểm chỉ định
     */
    public void lockAccount(int userId, java.time.LocalDateTime until) throws SQLException {
        String sql = "UPDATE users SET locked_until = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(until));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /** Ánh xạ ResultSet -> User — gọi trong mọi query SELECT */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setUserAddress(rs.getString("user_address"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setEmailVerified(rs.getBoolean("is_email_verified"));
        user.setFailedLoginCount(rs.getInt("failed_login_count"));

        String verificationCodeHash = rs.getString("email_verification_code_hash");
        user.setEmailVerificationCodeHash(verificationCodeHash);

        Timestamp emailVerificationExpiresAt = rs.getTimestamp("email_verification_expires_at");
        if (emailVerificationExpiresAt != null) {
            user.setEmailVerificationExpiresAt(emailVerificationExpiresAt.toLocalDateTime());
        }

        Timestamp emailVerificationResendAt = rs.getTimestamp("email_verification_resend_at");
        if (emailVerificationResendAt != null) {
            user.setEmailVerificationResendAt(emailVerificationResendAt.toLocalDateTime());
        }

        Timestamp emailVerificationSentAt = rs.getTimestamp("email_verification_sent_at");
        if (emailVerificationSentAt != null) {
            user.setEmailVerificationSentAt(emailVerificationSentAt.toLocalDateTime());
        }
        
        Timestamp lockedUntilTs = rs.getTimestamp("locked_until");
        if (lockedUntilTs != null) {
            user.setLockedUntil(lockedUntilTs.toLocalDateTime());
        }
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            user.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            user.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }
        
        return user;
    }

    /**
     * Tìm kiếm người dùng bằng ID
     */
    public User findUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findActiveAdminById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ? AND role = 'ADMIN' AND status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findFirstActiveAdmin() throws SQLException {
        String sql = "SELECT TOP 1 * FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE' ORDER BY user_id ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Batch load users theo danh sách ID.
     */
    public Map<Integer, User> findByIds(Collection<Integer> ids) throws SQLException {
        Map<Integer, User> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(ids);
        StringBuilder placeholders = new StringBuilder();
        int index = 0;
        for (Integer ignored : distinctIds) {
            if (index++ > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT * FROM users WHERE user_id IN (" + placeholders + ")";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer id : distinctIds) {
                stmt.setInt(paramIndex++, id);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = mapRow(rs);
                    map.put(user.getUserId(), user);
                }
            }
        }
        return map;
    }

    /**
     * Xóa người dùng bằng ID (sử dụng khi đăng ký lỗi để đồng bộ).
     */
    public void deleteUser(int userId) throws SQLException {
        // Kiểm tra xem user có đơn hàng nào không
        String sqlCheck = "SELECT COUNT(*) FROM orders WHERE customer_id = ? OR owner_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Nếu có đơn hàng, không xóa cứng mà chuyển trạng thái thành SUSPENDED
                    String sqlUpdate = "UPDATE users SET status = 'SUSPENDED', updated_at = GETDATE() WHERE user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                        updateStmt.setInt(1, userId);
                        updateStmt.executeUpdate();
                    }
                    throw new SQLException("Không thể xóa cứng người dùng vì đã có đơn hàng trong hệ thống. Trạng thái tài khoản đã được chuyển sang SUSPENDED.");
                }
            }
        }

        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public List<User> findActiveShopOwners() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'SHOP_OWNER' AND status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Map<String, Object>> getUserRegistrationTrend(String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT CAST(created_at AS DATE) AS reg_date, COUNT(*) AS user_count " +
            "FROM users " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND CAST(created_at AS DATE) >= ? ");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND CAST(created_at AS DATE) <= ? ");
            params.add(java.sql.Date.valueOf(endDate));
        }
        sql.append("GROUP BY CAST(created_at AS DATE) ORDER BY reg_date");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", rs.getDate("reg_date").toString());
                    map.put("count", rs.getInt("user_count"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    public void updateAvatar(int userId, String avatarUrl) throws SQLException {
        String sql = "UPDATE users SET avatar_url = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, avatarUrl);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public boolean isPhoneExists(String phone, int excludeUserId) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ? AND user_id != ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone.trim());
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}

