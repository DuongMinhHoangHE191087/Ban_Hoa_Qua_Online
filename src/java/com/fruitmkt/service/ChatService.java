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

    private final com.fruitmkt.dao.ChatDAO chatDAO = new com.fruitmkt.dao.ChatDAO();

    /**
     * Lấy hoặc tạo phiên chat mới giữa customer và shop owner. Không yêu cầu đơn hàng phải tồn tại.
     */
    public com.fruitmkt.model.entity.ChatSession getOrCreateSession(int customerId, int ownerId) throws SQLException {
        if (customerId <= 0 || ownerId <= 0) {
            throw new IllegalArgumentException("ID khách hàng hoặc ID chủ shop không hợp lệ.");
        }
        if (customerId == ownerId) {
            throw new IllegalArgumentException("Không thể tự trò chuyện với chính mình.");
        }

        java.util.List<com.fruitmkt.model.entity.ChatSession> existing = chatDAO.findSessionByParticipants(customerId, ownerId);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0);
        }

        int newId = chatDAO.createSession(customerId, ownerId);
        com.fruitmkt.model.entity.ChatSession session = new com.fruitmkt.model.entity.ChatSession();
        session.setSessionId(newId);
        session.setCustomerId(customerId);
        session.setOwnerId(ownerId);
        session.setStatus("ACTIVE");
        session.setCreatedAt(java.time.LocalDateTime.now());
        session.setUpdatedAt(java.time.LocalDateTime.now());
        return session;
    }

    /**
     * Gửi tin nhắn mới. Tiệt trùng thẻ HTML chống XSS.
     */
    public void sendMessage(int sessionId, int senderId, String content) throws SQLException {
        if (sessionId <= 0 || senderId <= 0) {
            throw new IllegalArgumentException("Session ID hoặc Sender ID không hợp lệ.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống.");
        }

        // Tẩy XSS cơ bản bằng cách loại bỏ các thẻ HTML
        String sanitizedContent = content.replaceAll("<[^>]*>", "").trim();
        if (sanitizedContent.isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không hợp lệ.");
        }

        com.fruitmkt.model.entity.ChatMessage msg = new com.fruitmkt.model.entity.ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(sanitizedContent);
        msg.setIsRead(false);

        chatDAO.saveMessage(msg);
    }

    /**
     * Lấy toàn bộ danh sách tin nhắn của 1 phiên.
     */
    public java.util.List<com.fruitmkt.model.entity.ChatMessage> getMessages(int sessionId) throws SQLException {
        if (sessionId <= 0) {
            throw new IllegalArgumentException("Session ID không hợp lệ.");
        }
        return chatDAO.findMessages(sessionId);
    }

    /**
     * Đánh dấu tin nhắn đã đọc.
     */
    public void markRead(int sessionId, int readerId) throws SQLException {
        if (sessionId <= 0 || readerId <= 0) {
            throw new IllegalArgumentException("Session ID hoặc Reader ID không hợp lệ.");
        }
        chatDAO.markRead(sessionId, readerId);
    }

}
