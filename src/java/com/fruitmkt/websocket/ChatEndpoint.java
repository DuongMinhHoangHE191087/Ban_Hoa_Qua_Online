package com.fruitmkt.websocket;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.dao.NotificationDAO;
import com.fruitmkt.model.entity.ChatMessage;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.Notification;
import com.fruitmkt.model.entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatEndpoint — WebSocket endpoint cho hệ thống chat real-time.
 *
 * URL: ws://host/ctx/ws/chat/{sessionId}
 *
 * LUỒNG XỬ LÝ:
 *   1. Client kết nối → @OnOpen: xác thực user từ HttpSession, thêm vào roomMap
 *   2. Client gửi text JSON → @OnMessage: lưu DB, broadcast tới tất cả WS trong room
 *   3. Người nhận offline → ghi notification vào DB để hiển thị khi họ login lại
 *   4. Client ngắt kết nối → @OnClose: xóa khỏi roomMap
 *
 * THREAD SAFETY: ConcurrentHashMap + CopyOnWriteArraySet
 *
 * ĐỊNH DẠNG TIN NHẮN (JSON đơn giản, không dùng thư viện):
 *   Client → Server: {"content":"nội dung tin nhắn"}
 *   Server → Client: {"senderId":1,"content":"nội dung","createdAt":"2026-06-07T10:00:00","messageId":42}
 *
 * @author fruitmkt-team
 */
@ServerEndpoint(
    value = "/ws/chat/{sessionId}",
    configurator = HttpSessionConfigurator.class
)
public class ChatEndpoint {

    private static final Logger LOG = Logger.getLogger(ChatEndpoint.class.getName());

    /** Map: sessionId (chat session) → tập hợp WS sessions đang kết nối */
    private static final Map<Integer, Set<Session>> ROOM_MAP = new ConcurrentHashMap<>();

    private final ChatDAO chatDAO = new ChatDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // ----------------------------------------------------------------
    // Lifecycle callbacks
    // ----------------------------------------------------------------

    @OnOpen
    public void onOpen(Session wsSession, EndpointConfig config,
                       @PathParam("sessionId") int sessionId) {
        // Lấy user từ HttpSession (được đặt vào bởi HttpSessionConfigurator)
        HttpSession httpSession = (HttpSession) config.getUserProperties()
                                                      .get(HttpSessionConfigurator.HTTP_SESSION_KEY);
        if (httpSession == null) {
            closeUnauthorized(wsSession, "Không có HTTP session.");
            return;
        }
        User currentUser = (User) httpSession.getAttribute(AppConfig.SESSION_USER);
        if (currentUser == null) {
            closeUnauthorized(wsSession, "Chưa đăng nhập.");
            return;
        }

        // Xác thực quyền truy cập session
        try {
            ChatSession chatSession = chatDAO.findSessionById(sessionId);
            if (chatSession == null) {
                closeUnauthorized(wsSession, "Chat session không tồn tại.");
                return;
            }
            int uid = currentUser.getUserId();
            boolean isParticipant = (chatSession.getCustomerId() == uid || chatSession.getOwnerId() == uid);
            boolean isAdmin = AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
            if (!isParticipant && !isAdmin) {
                closeUnauthorized(wsSession, "Không có quyền truy cập session này.");
                return;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "ChatEndpoint.onOpen: lỗi xác thực sessionId=" + sessionId, e);
            closeUnauthorized(wsSession, "Lỗi server.");
            return;
        }

        // Lưu userId vào WS session userProperties để dùng ở @OnMessage
        wsSession.getUserProperties().put("userId", currentUser.getUserId());
        wsSession.getUserProperties().put("userRole", currentUser.getRole());
        wsSession.getUserProperties().put("fullName", currentUser.getFullName());
        wsSession.getUserProperties().put("sessionId", sessionId);

        // Thêm WS session vào room
        ROOM_MAP.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(wsSession);
        LOG.info("ChatEndpoint: user #" + currentUser.getUserId() + " joined room #" + sessionId
                 + " (total in room: " + ROOM_MAP.get(sessionId).size() + ")");
    }

    @OnMessage
    public void onMessage(Session wsSession, String rawMessage) {
        Integer sessionId = (Integer) wsSession.getUserProperties().get("sessionId");
        Integer senderId  = (Integer) wsSession.getUserProperties().get("userId");
        String senderRole = (String) wsSession.getUserProperties().get("userRole");
        String senderName = (String) wsSession.getUserProperties().get("fullName");
        
        if (sessionId == null || senderId == null) return;

        // Parse JSON đơn giản: {"content":"...", "mediaUrl":"...", "mediaType":"..."}
        String content = extractField(rawMessage, "content");
        String mediaUrl = extractField(rawMessage, "mediaUrl");
        String mediaType = extractField(rawMessage, "mediaType");

        // Tin nhắn hợp lệ phải có content hoặc mediaUrl
        if ((content == null || content.trim().isEmpty()) && (mediaUrl == null || mediaUrl.trim().isEmpty())) {
            return;
        }

        if (content != null) {
            content = content.trim();
        }

        // Lưu vào DB
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setMediaUrl(mediaUrl);
        msg.setMediaType(mediaType);
        msg.setIsRead(false);

        int messageId = 0;
        try {
            messageId = chatDAO.saveMessage(msg);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "ChatEndpoint.onMessage: lỗi lưu tin nhắn", e);
            sendError(wsSession, "Lỗi lưu tin nhắn.");
            return;
        }

        // Build JSON response per peer to support name masking
        String createdAtStr = LocalDateTime.now().toString();

        // Broadcast tới tất cả WS sessions trong room
        Set<Session> room = ROOM_MAP.get(sessionId);
        if (room != null) {
            for (Session peer : room) {
                if (peer.isOpen()) {
                    try {
                        String peerRole = (String) peer.getUserProperties().get("userRole");
                        boolean peerIsAdmin = AppConfig.ROLE_ADMIN.equals(peerRole);
                        String displayName = senderName;
                        if (!peerIsAdmin && "ADMIN".equals(senderRole)) {
                            displayName = "Hỗ trợ Admin";
                        }
                        String responseJson = buildMessageJson(messageId, senderId, displayName, senderRole, content, mediaUrl, mediaType, createdAtStr);
                        peer.getBasicRemote().sendText(responseJson);
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "ChatEndpoint: không gửi được tới peer", e);
                    }
                }
            }
        }

        // Offline fallback: gửi notification cho người nhận nếu họ không có trong room
        sendOfflineNotification(sessionId, senderId, content, mediaUrl, mediaType, room);
    }

    @OnClose
    public void onClose(Session wsSession, CloseReason reason) {
        Integer sessionId = (Integer) wsSession.getUserProperties().get("sessionId");
        if (sessionId != null) {
            Set<Session> room = ROOM_MAP.get(sessionId);
            if (room != null) {
                room.remove(wsSession);
                if (room.isEmpty()) {
                    ROOM_MAP.remove(sessionId);
                }
            }
        }
    }

    @OnError
    public void onError(Session wsSession, Throwable throwable) {
        LOG.log(Level.WARNING, "ChatEndpoint.onError: " + throwable.getMessage(), throwable);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Gửi notification DB khi người nhận không online trong room.
     * Tích hợp cơ chế CHỐNG SPAM: Chỉ gửi 1 thông báo chưa đọc duy nhất cho mỗi session.
     */
    private void sendOfflineNotification(int sessionId, int senderId, String content, String mediaUrl, String mediaType, Set<Session> room) {
        try {
            ChatSession cs = chatDAO.findSessionById(sessionId);
            if (cs == null) return;

            // Xác định người nhận
            int recipientId = (cs.getCustomerId() == senderId) ? cs.getOwnerId() : cs.getCustomerId();

            // Kiểm tra recipient có đang trong room không
            boolean recipientOnline = false;
            if (room != null) {
                for (Session peer : room) {
                    if (peer.isOpen()) {
                        Object uid = peer.getUserProperties().get("userId");
                        if (uid != null && uid.equals(recipientId)) {
                            recipientOnline = true;
                            break;
                        }
                    }
                }
            }

            if (!recipientOnline) {
                // CHỐNG SPAM: Kiểm tra xem đã có thông báo chưa đọc của session này cho recipient chưa
                boolean hasPending = false;
                String checkSql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0 AND action_url LIKE ?";
                try (Connection conn = chatDAO.getConnection();
                     PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, recipientId);
                    ps.setString(2, "%sessionId=" + sessionId + "%");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasPending = true;
                        }
                    }
                }

                if (!hasPending) {
                    // Người nhận offline và chưa có thông báo chưa đọc cho session này -> Ghi notification mới
                    Notification notif = new Notification();
                    notif.setUserId(recipientId);
                    notif.setType(AppConfig.NOTIF_SYSTEM);
                    notif.setTitle("Bạn có tin nhắn mới");

                    String text = content;
                    if (text == null || text.trim().isEmpty()) {
                        if ("IMAGE".equals(mediaType)) {
                            text = "[Hình ảnh]";
                        } else if ("VIDEO".equals(mediaType)) {
                            text = "[Video]";
                        } else {
                            text = "[Tin nhắn mới]";
                        }
                    }
                    String preview = text.length() > 50 ? text.substring(0, 50) + "…" : text;
                    notif.setMessage(preview);

                    // Phân biệt action url cho từng vai trò
                    String actionUrl = "/chat?sessionId=" + sessionId;
                    // Nếu là ADMIN session và người nhận là Admin hoặc Shop
                    if ("ADMIN".equals(cs.getSessionType())) {
                        actionUrl = "/admin/chat?sessionId=" + sessionId;
                    } else if (recipientId == cs.getOwnerId()) {
                        actionUrl = "/shop/chat?sessionId=" + sessionId;
                    }
                    
                    notif.setActionUrl(actionUrl);
                    notif.setIsRead(false);
                    notificationDAO.save(notif);
                    LOG.info("ChatEndpoint: Created offline notification for user #" + recipientId + " on session #" + sessionId);
                } else {
                    LOG.info("ChatEndpoint: Skip offline notification for user #" + recipientId + " (notification already pending)");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "ChatEndpoint: lỗi gửi offline notification", e);
        }
    }

    /** Đóng WS session với mã 1008 (Policy Violation) khi không có quyền */
    private void closeUnauthorized(Session wsSession, String reason) {
        try {
            wsSession.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "ChatEndpoint: không thể đóng session", e);
        }
    }

    /** Gửi JSON lỗi tới client */
    private void sendError(Session wsSession, String msg) {
        try {
            wsSession.getBasicRemote().sendText("{\"error\":\"" + escapeJson(msg) + "\"}");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "ChatEndpoint.sendError: " + e.getMessage());
        }
    }

    /**
     * Parse trường bất kỳ từ JSON đơn giản.
     * Dùng string manipulation tránh dependency thêm thư viện.
     */
    private String extractField(String json, String fieldName) {
        if (json == null) return null;
        int idx = json.indexOf("\"" + fieldName + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        
        int startQuote = json.indexOf('"', colon + 1);
        if (startQuote < 0) {
            // Check null
            String sub = json.substring(colon + 1).trim();
            if (sub.startsWith("null")) return null;
            return null;
        }
        
        int endQuote = -1;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && json.charAt(i - 1) != '\\') {
                endQuote = i;
                break;
            }
        }
        if (endQuote <= startQuote) return null;
        return json.substring(startQuote + 1, endQuote)
                   .replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\\\", "\\");
     }

    /** Build JSON message response */
    private String buildMessageJson(int messageId, int senderId, String senderName, String senderRole, String content, String mediaUrl, String mediaType, String createdAt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"messageId\":").append(messageId);
        sb.append(",\"senderId\":").append(senderId);
        sb.append(",\"senderName\":").append(senderName != null ? "\"" + escapeJson(senderName) + "\"" : "null");
        sb.append(",\"senderRole\":").append(senderRole != null ? "\"" + escapeJson(senderRole) + "\"" : "null");
        sb.append(",\"content\":").append(content != null ? "\"" + escapeJson(content) + "\"" : "null");
        sb.append(",\"mediaUrl\":").append(mediaUrl != null ? "\"" + escapeJson(mediaUrl) + "\"" : "null");
        sb.append(",\"mediaType\":").append(mediaType != null ? "\"" + escapeJson(mediaType) + "\"" : "null");
        sb.append(",\"createdAt\":\"").append(createdAt).append("\"");
        sb.append("}");
        return sb.toString();
    }

    /** Escape JSON string — tránh XSS và JSON injection */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
