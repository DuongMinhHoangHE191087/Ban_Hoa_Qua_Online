package service.chat;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import dao.chat.NotificationDAO;
import model.entity.auth.User;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import model.entity.chat.Notification;
import util.ChatNotificationUtil;
import websocket.ChatEndpoint;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatDeliveryService - Xử lý lưu tin nhắn, broadcast realtime và fallback notification.
 */
public class ChatDeliveryService {

    private static final Logger LOG = Logger.getLogger(ChatDeliveryService.class.getName());

    private final ChatDAO chatDAO = new ChatDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Lưu tin nhắn chat và phân phối tới WebSocket room nếu có.
     * Nếu người nhận không online trong room, tạo notification DB an toàn.
     */
    public void sendMessage(int sessionId, int senderId, String senderRole, String senderName,
                            String content, String mediaUrl, String mediaType) throws SQLException {
        if (sessionId <= 0 || senderId <= 0) {
            throw new IllegalArgumentException("Session ID hoặc Sender ID không hợp lệ.");
        }

        String normalizedContent = content != null ? content.trim() : null;
        String normalizedMediaUrl = mediaUrl != null ? mediaUrl.trim() : null;
        String normalizedMediaType = mediaType != null ? mediaType.trim().toUpperCase(Locale.ROOT) : null;

        if ((normalizedContent == null || normalizedContent.isEmpty())
                && (normalizedMediaUrl == null || normalizedMediaUrl.isEmpty())) {
            throw new IllegalArgumentException("Nội dung tin nhắn hoặc tệp đính kèm không được rỗng");
        }

        ChatSession session = chatDAO.findSessionById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session không tồn tại");
        }

        boolean isParticipant = session.getCustomerId() == senderId || session.getOwnerId() == senderId;
        boolean isAdmin = AppConfig.ROLE_ADMIN.equals(senderRole);
        if (!isParticipant && !isAdmin) {
            throw new IllegalArgumentException("Không có quyền gửi vào session này");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(normalizedContent);
        msg.setMediaUrl(normalizedMediaUrl);
        msg.setMediaType(normalizedMediaType);
        msg.setIsRead(false);

        int messageId = chatDAO.saveMessage(msg);
        String createdAt = LocalDateTime.now().toString();

        ChatEndpoint.broadcastMessage(
                sessionId,
                senderId,
                senderRole,
                senderName,
                messageId,
                normalizedContent,
                normalizedMediaUrl,
                normalizedMediaType,
                createdAt
        );

        int recipientId = (session.getCustomerId() == senderId) ? session.getOwnerId() : session.getCustomerId();
        if (ChatEndpoint.isUserOnline(sessionId, recipientId)) {
            return;
        }

        try {
            if (notificationDAO.hasUnreadChatNotification(recipientId, sessionId)) {
                LOG.info("ChatDeliveryService: Skip offline notification for user #" + recipientId
                        + " (notification already pending)");
                return;
            }

            User recipient = userDAO.findUserById(recipientId);

            Notification notif = new Notification();
            notif.setUserId(recipientId);
            notif.setType(AppConfig.NOTIF_SYSTEM);
            notif.setTitle("Bạn có tin nhắn mới");
            notif.setMessage(ChatNotificationUtil.sanitizePreview(normalizedContent, normalizedMediaType));
            notif.setActionUrl(ChatNotificationUtil.buildActionUrl(recipient != null ? recipient.getRole() : null, sessionId));
            notif.setIsRead(false);
            notificationDAO.save(notif);

            LOG.info("ChatDeliveryService: Created offline notification for user #" + recipientId
                    + " on session #" + sessionId);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "ChatDeliveryService: lỗi gửi offline notification", e);
        }
    }
}
