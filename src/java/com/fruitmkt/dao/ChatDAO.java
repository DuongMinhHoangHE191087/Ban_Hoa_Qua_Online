package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
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
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findSessionByParticipants(int customerId, int ownerId)");
    }

    /**
     * TODO: Implement — findSessionsByCustomer(int customerId)
     */
    public List<ChatSession> findSessionsByCustomer(int customerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findSessionsByCustomer(int customerId)");
    }

    /**
     * TODO: Implement — findSessionsByOwner(int ownerId)
     */
    public List<ChatSession> findSessionsByOwner(int ownerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findSessionsByOwner(int ownerId)");
    }

    /**
     * TODO: Implement — createSession(int customerId, int ownerId)
     */
    public int createSession(int customerId, int ownerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: createSession(int customerId, int ownerId)");
    }

    /**
     * TODO: Implement — saveMessage(ChatMessage msg)
     */
    public int saveMessage(ChatMessage msg) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: saveMessage(ChatMessage msg)");
    }

    /**
     * TODO: Implement — findMessages(int sessionId)
     */
    public List<ChatSession> findMessages(int sessionId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: findMessages(int sessionId)");
    }

    /**
     * TODO: Implement — markRead(int sessionId, int readerId)
     */
    public void markRead(int sessionId, int readerId) throws SQLException {
        // TODO: Viết SQL và xử lý ResultSet ở đây
        throw new UnsupportedOperationException("Not implemented yet: markRead(int sessionId, int readerId)");
    }

    /** Ánh xạ ResultSet -> ChatSession — gọi trong mọi query SELECT */
    private ChatSession mapRow(ResultSet rs) throws SQLException {
        // TODO: rs.getInt(), rs.getString()... theo Schema.sql
        throw new UnsupportedOperationException("mapRow not implemented");
    }
}
