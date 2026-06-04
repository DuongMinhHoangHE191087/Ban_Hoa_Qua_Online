package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.ChatMessage;
import com.fruitmkt.model.entity.ChatSession;
import java.sql.*;
import java.util.*;

/**
 * ChatDAO — DAO cho entity ChatSession.
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

    /**
     * TODO: Implement — findSessionByParticipants(int customerId, int ownerId)
     */
    public List<ChatSession> findSessionByParticipants(int customerId, int ownerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE customer_id = ? AND owner_id = ?";
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
     * TODO: Implement — findSessionsByCustomer(int customerId)
     */
    public List<ChatSession> findSessionsByCustomer(int customerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE customer_id = ? ORDER BY updated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * TODO: Implement — findSessionsByOwner(int ownerId)
     */
    public List<ChatSession> findSessionsByOwner(int ownerId) throws SQLException {
        List<ChatSession> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE owner_id = ? ORDER BY updated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * TODO: Implement — createSession(int customerId, int ownerId)
     */
    public int createSession(int customerId, int ownerId) throws SQLException {
        String sql = "INSERT INTO chat_sessions (customer_id, owner_id, status, created_at, updated_at) VALUES (?, ?, 'ACTIVE', GETDATE(), GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, ownerId);
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
     * TODO: Implement — saveMessage(ChatMessage msg)
     */
    public int saveMessage(ChatMessage msg) throws SQLException {
        String sqlInsert = "INSERT INTO chat_messages (session_id, sender_id, content, is_read, created_at) VALUES (?, ?, ?, ?, GETDATE())";
        String sqlUpdateSession = "UPDATE chat_sessions SET updated_at = GETDATE() WHERE session_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int msgId = 0;
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, msg.getSessionId());
                    ps.setInt(2, msg.getSenderId());
                    ps.setString(3, msg.getContent());
                    ps.setBoolean(4, msg.getIsRead());
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
     * TODO: Implement — findMessages(int sessionId)
     */
    public List<ChatMessage> findMessages(int sessionId) throws SQLException {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC";
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
     * TODO: Implement — markRead(int sessionId, int readerId)
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

    /** Ánh xạ ResultSet -> ChatSession — gọi trong mọi query SELECT */
    private ChatSession mapRow(ResultSet rs) throws SQLException {
        ChatSession cs = new ChatSession();
        cs.setSessionId(rs.getInt("session_id"));
        cs.setCustomerId(rs.getInt("customer_id"));
        cs.setOwnerId(rs.getInt("owner_id"));
        cs.setStatus(rs.getString("status"));
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
        cm.setIsRead(rs.getBoolean("is_read"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) cm.setCreatedAt(ca.toLocalDateTime());
        return cm;
    }
}
