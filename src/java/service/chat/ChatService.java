package service.chat;

import dao.chat.ChatDAO;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

    private final ChatDAO chatDAO = new ChatDAO();

    /**
     * Lấy hoặc tạo phiên chat mới giữa customer và shop owner. Không yêu cầu đơn hàng phải tồn tại.
     */
    public ChatSession getOrCreateSession(int customerId, int ownerId) throws SQLException {
        if (customerId <= 0 || ownerId <= 0) {
            throw new IllegalArgumentException("ID khách hàng hoặc ID chủ shop không hợp lệ.");
        }
        if (customerId == ownerId) {
            throw new IllegalArgumentException("Không thể tự trò chuyện với chính mình.");
        }

        List<ChatSession> existing = chatDAO.findSessionByParticipants(customerId, ownerId);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0);
        }

        int newId = chatDAO.createSession(customerId, ownerId);
        ChatSession session = new ChatSession();
        session.setSessionId(newId);
        session.setCustomerId(customerId);
        session.setOwnerId(ownerId);
        session.setStatus("ACTIVE");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
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

        // Kiểm tra chống XSS và HTML injection
        if (content.matches("(?s).*<[^>]*>.*")) {
            throw new IllegalArgumentException("Nội dung tin nhắn không hợp lệ.");
        }

        String sanitizedContent = content.trim();

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(sanitizedContent);
        msg.setIsRead(false);

        chatDAO.saveMessage(msg);
    }

    /**
     * Lấy toàn bộ danh sách tin nhắn của 1 phiên.
     */
    public List<ChatMessage> getMessages(int sessionId) throws SQLException {
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

    /**
     * Đếm tổng số tin nhắn chưa đọc của một người dùng trong tất cả các session.
     */
    public int countTotalUnread(int userId) throws SQLException {
        if (userId <= 0) {
            return 0;
        }
        return chatDAO.countTotalUnread(userId);
    }

}
