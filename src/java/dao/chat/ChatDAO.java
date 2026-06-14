package dao.chat;

import dao.system.BaseDAO;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import util.LoggerUtil;
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
 * ChatDAO — DAO cho entity ChatSession và ChatMessage.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class ChatDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(ChatDAO.class.getName());

    // ----------------------------------------------------------------
    // Session queries
    // ----------------------------------------------------------------

    /**
     * Tìm session theo participants. Chỉ lấy session ACTIVE để tránh mở lại session đã đóng.
     * [FIX bug#1]: thêm AND status='ACTIVE' filter.
     */
    public List<ChatSession> findSessionByParticipants(int customerId, int ownerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE customer_id = ? AND owner_id = ? AND status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm session theo sessionId đơn lẻ — dùng để xác thực quyền trong WebSocket và API.
     * [NEW] — hỗ trợ IDOR check: caller so sánh customerId/ownerId với user hiện tại.
     */
    public ChatSession findSessionById(int sessionId) throws SQLException {
        String sql = "SELECT * FROM chat_sessions WHERE session_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách session của customer kèm tên shop/admin — dùng cho sidebar.
     * [NEW] — JOIN users + shop_owner_profiles để lấy tên hiển thị.
     */
    public List<ChatSession> findSessionsByCustomer(int customerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT cs.*, " +
                     "  COALESCE(sop.shop_name, u.full_name) AS partner_name, " +
                     "  u.avatar_url AS partner_avatar " +
                     "FROM chat_sessions cs " +
                     "JOIN users u ON u.user_id = cs.owner_id " +
                     "LEFT JOIN shop_owner_profiles sop ON sop.user_id = cs.owner_id " +
                     "WHERE cs.customer_id = ? " +
                     "ORDER BY cs.updated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatSession cs = mapRow(rs);
                    cs.setPartnerName(rs.getString("partner_name"));
                    cs.setPartnerAvatar(rs.getString("partner_avatar"));
                    list.add(cs);
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách session của shop owner kèm tên customer — dùng cho sidebar shop.
     * [NEW] — JOIN users để lấy full_name + avatar.
     */
    public List<ChatSession> findSessionsByOwner(int ownerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT cs.*, u.full_name AS partner_name, u.avatar_url AS partner_avatar " +
                     "FROM chat_sessions cs " +
                     "JOIN users u ON u.user_id = cs.customer_id " +
                     "WHERE cs.owner_id = ? " +
                     "ORDER BY cs.updated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatSession cs = mapRow(rs);
                    cs.setPartnerName(rs.getString("partner_name"));
                    cs.setPartnerAvatar(rs.getString("partner_avatar"));
                    list.add(cs);
                }
            }
        }
        return list;
    }

    /**
     * Lấy tất cả session — dành cho Admin xem toàn bộ cuộc trò chuyện.
     * [NEW]
     */
    public List<ChatSession> findAllSessions() throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT cs.*, " +
                     "  cu.full_name AS customer_name, " +
                     "  COALESCE(sop.shop_name, ou.full_name) AS partner_name " +
                     "FROM chat_sessions cs " +
                     "JOIN users cu ON cu.user_id = cs.customer_id " +
                     "JOIN users ou ON ou.user_id = cs.owner_id " +
                     "LEFT JOIN shop_owner_profiles sop ON sop.user_id = cs.owner_id " +
                     "ORDER BY cs.updated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatSession cs = mapRow(rs);
                    cs.setPartnerName(rs.getString("customer_name") + " ↔ " + rs.getString("partner_name"));
                    list.add(cs);
                }
            }
        }
        return list;
    }

    /**
     * Tạo session mới với session_type (SHOP hoặc ADMIN).
     */
    public int createSession(int customerId, int ownerId) throws SQLException {
        return createSession(customerId, ownerId, "SHOP");
    }

    /**
     * Tạo session với session_type tường minh.
     * [NEW] — hỗ trợ phân biệt SHOP vs ADMIN session.
     */
    public int createSession(int customerId, int ownerId, String sessionType) throws SQLException {
        String sql = "INSERT INTO chat_sessions (customer_id, owner_id, status, session_type, created_at, updated_at) " +
                     "VALUES (?, ?, 'ACTIVE', ?, GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, ownerId);
            ps.setString(3, sessionType != null ? sessionType : "SHOP");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Creating chat session failed, no ID obtained.");
    }

    /**
     * Đếm số tin nhắn chưa đọc trong session cho user cụ thể.
     * [NEW] — dùng cho badge unread trên sidebar.
     */
    public int countUnread(int sessionId, int readerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM chat_messages WHERE session_id = ? AND sender_id != ? AND is_read = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Đếm tổng số tin nhắn chưa đọc trong tất cả các session của một user.
     * [NEW] — dùng cho badge unread trên header navbar.
     */
    public int countTotalUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM chat_messages cm " +
                     "JOIN chat_sessions cs ON cs.session_id = cm.session_id " +
                     "WHERE (cs.customer_id = ? OR cs.owner_id = ?) " +
                     "  AND cm.sender_id != ? " +
                     "  AND cm.is_read = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // Message queries
    // ----------------------------------------------------------------

    /**
     * Lưu tin nhắn mới và cập nhật updated_at của session trong cùng transaction.
     */
    public int saveMessage(ChatMessage msg) throws SQLException {
        String sqlInsert = "INSERT INTO chat_messages (session_id, sender_id, content, media_url, media_type, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        String sqlUpdateSession = "UPDATE chat_sessions SET updated_at = GETDATE() WHERE session_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int msgId = 0;
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, msg.getSessionId());
                    ps.setInt(2, msg.getSenderId());
                    ps.setString(3, msg.getContent());
                    ps.setString(4, msg.getMediaUrl());
                    ps.setString(5, msg.getMediaType());
                    ps.setBoolean(6, msg.getIsRead());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            msgId = rs.getInt(1);
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSession)) {
                    ps.setInt(1, msg.getSessionId());
                    ps.executeUpdate();
                }
                conn.commit();
                return msgId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /**
     * Lấy tất cả tin nhắn trong session, sắp xếp theo thời gian tăng dần.
     */
    public List<ChatMessage> findMessages(int sessionId) throws SQLException {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT cm.*, u.full_name AS sender_name, u.role AS sender_role " +
                     "FROM chat_messages cm " +
                     "LEFT JOIN users u ON cm.sender_id = u.user_id " +
                     "WHERE cm.session_id = ? " +
                     "ORDER BY cm.created_at ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMessageRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đánh dấu tất cả tin nhắn chưa đọc trong session là đã đọc (không đánh dấu tin của chính readerId).
     */
    public void markRead(int sessionId, int readerId) throws SQLException {
        String sql = "UPDATE chat_messages SET is_read = 1 WHERE session_id = ? AND sender_id != ? AND is_read = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, readerId);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // Mapping helpers
    // ----------------------------------------------------------------

    /** Ánh xạ ResultSet → ChatSession */
    private ChatSession mapRow(ResultSet rs) throws SQLException {
        ChatSession cs = new ChatSession();
        cs.setSessionId(rs.getInt("session_id"));
        cs.setCustomerId(rs.getInt("customer_id"));
        cs.setOwnerId(rs.getInt("owner_id"));
        cs.setStatus(rs.getString("status"));
        // session_type — cột mới; trả về chuỗi rỗng nếu cột chưa tồn tại (backward compat)
        try { cs.setSessionType(rs.getString("session_type")); } catch (SQLException e) {
            LoggerUtil.warn(log, "session_type column not yet present, skipping", e);
        }
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) cs.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) cs.setUpdatedAt(ua.toLocalDateTime());
        Timestamp cla = rs.getTimestamp("closed_at");
        if (cla != null) cs.setClosedAt(cla.toLocalDateTime());
        return cs;
    }

    private ChatMessage mapMessageRow(ResultSet rs) throws SQLException {
        ChatMessage cm = new ChatMessage();
        cm.setMessageId(rs.getInt("message_id"));
        cm.setSessionId(rs.getInt("session_id"));
        cm.setSenderId(rs.getInt("sender_id"));
        cm.setContent(rs.getString("content"));
        cm.setMediaUrl(rs.getString("media_url"));
        cm.setMediaType(rs.getString("media_type"));
        cm.setIsRead(rs.getBoolean("is_read"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) cm.setCreatedAt(ca.toLocalDateTime());
        
        try { cm.setSenderName(rs.getString("sender_name")); } catch (SQLException e) {
            LoggerUtil.warn(log, "sender_name column not present in this query, skipping", e);
        }
        try { cm.setSenderRole(rs.getString("sender_role")); } catch (SQLException e) {
            LoggerUtil.warn(log, "sender_role column not present in this query, skipping", e);
        }
        
        return cm;
    }
}
