package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.HashUtil;
import java.sql.*;
import java.util.*;

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

    /**
     * TODO: Implement — findById(int id)
     */
    public List<User> findById(int id) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findById(int id)");
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
     String sql = "INSERT INTO users (full_name, email, password_hash, role, status, is_email_verified) VALUES (?, ?, ?, 'CUSTOMER', 'ACTIVE', 1)";
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
                return newUser;
            }
        }
    }
    return null;
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
        return saveNewCustomer(fullName, email, passwordHash, phone, role, AppConfig.ACCOUNT_STATUS_INACTIVE, false);
    }

    public int saveNewCustomer(String fullName, String email, String passwordHash, String phone, String role, String status, boolean emailVerified) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role, status, is_email_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setString(4, phone);
            stmt.setString(5, role);
            stmt.setString(6, status);
            stmt.setBoolean(7, emailVerified);
            
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
        String sql = "UPDATE users SET full_name = ?, phone = ?, user_address = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getUserAddress());
            stmt.setInt(4, user.getUserId());
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
     * TODO: Implement — incrementFailedLogin(int userId)
     */
    public void incrementFailedLogin(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: incrementFailedLogin(int userId)");
    }

    /**
     * TODO: Implement — resetFailedLogin(int userId)
     */
    public void resetFailedLogin(int userId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: resetFailedLogin(int userId)");
    }

    /**
     * TODO: Implement — lockAccount(int userId, java.time.LocalDateTime until)
     */
    public void lockAccount(int userId, java.time.LocalDateTime until) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: lockAccount(int userId, java.time.LocalDateTime until)");
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

    /**
     * Lưu trữ Refresh Token (Session) mới vào bảng user_sessions
     */
    public void saveUserSession(int userId, String token, java.sql.Timestamp expiresAt) throws SQLException {
        String sql = "INSERT INTO user_sessions (user_id, token, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, token);
            stmt.setTimestamp(3, expiresAt);
            stmt.executeUpdate();
        }
    }

    /**
     * Tìm user_id dựa trên Refresh Token hợp lệ (chưa hết hạn)
     */
    public Integer findUserIdBySessionToken(String token) throws SQLException {
        String sql = "SELECT user_id FROM user_sessions WHERE token = ? AND expires_at > GETDATE()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return null;
    }

    /**
     * Xóa Refresh Token khi logout hoặc thu hồi
     */
    public void deleteUserSession(String token) throws SQLException {
        String sql = "DELETE FROM user_sessions WHERE token = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }
}
