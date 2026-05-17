package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * ChatService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class ChatService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public com.fruitmkt.model.entity.ChatSession getOrCreateSession(int customerId, int ownerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getOrCreateSession(int customerId, int ownerId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void sendMessage(int sessionId, int senderId, String content) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: sendMessage(int sessionId, int senderId, String content)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getMessages(int sessionId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getMessages(int sessionId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void markRead(int sessionId, int readerId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: markRead(int sessionId, int readerId)");
    }

}
