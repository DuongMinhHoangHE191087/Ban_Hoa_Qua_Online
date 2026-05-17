package com.fruitmkt.service;

import java.sql.SQLException;

/**
 * NotificationService — Tầng business logic cho nghiệp vụ tương ứng.
 *
 * QUY TẮC:
 *   - Chỉ gọi DAO, không viết SQL ở đây
 *   - Chứa tất cả validation và business rule
 *   - Ném RuntimeException hoặc custom exception cho Servlet xử lý
 *   - Không tương tác trực tiếp với HttpRequest/Response
 *
 * @author fruitmkt-team
 */
public class NotificationService {

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void send(int userId, String type, String title, String message, String actionUrl) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: send(int userId, String type, String title, String message, String actionUrl)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public java.util.List getUnread(int userId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: getUnread(int userId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void markRead(int notifId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: markRead(int notifId)");
    }

    /**
     * TODO: Implement — xem SRS / use case tương ứng
     */
    public void markAllRead(int userId) throws SQLException {
        // TODO: Validate input → gọi DAO → business rule → return result
        throw new UnsupportedOperationException("Not implemented: markAllRead(int userId)");
    }

}
