package dao.auth;

import dao.system.BaseDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * UserSessionDAO — DAO cho các tác vụ liên quan đến user_sessions.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class UserSessionDAO extends BaseDAO {

    /**
     * Lưu trữ Refresh Token (Session) mới vào bảng user_sessions
     */
    public void saveUserSession(int userId, String token, Timestamp expiresAt) throws SQLException {
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
    public void deleteSessionToken(String token) throws SQLException {
        String sql = "DELETE FROM user_sessions WHERE token = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }

    public void deleteUserSession(String token) throws SQLException {
        deleteSessionToken(token);
    }

    /**
     * Xóa toàn bộ phiên đăng nhập của TẤT CẢ người dùng (Dùng khi bảo trì hệ thống)
     */
    public void deleteAllSessions() throws SQLException {
        String sql = "DELETE FROM user_sessions";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa toàn bộ phiên đăng nhập (Refresh Tokens) của một người dùng cụ thể
     */
    public void deleteSessionsByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM user_sessions WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
