package websocket;

import config.AppConfig;
import dao.chat.ChatDAO;
import model.entity.chat.ChatSession;
import model.entity.auth.User;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import service.chat.ChatDeliveryService;
import util.ErrorMessageUtil;

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
    private final ChatDeliveryService chatDeliveryService = new ChatDeliveryService();

    public static boolean isUserOnline(int sessionId, int userId) {
        Set<Session> room = ROOM_MAP.get(sessionId);
        if (room == null) {
            return false;
        }
        for (Session peer : room) {
            if (peer.isOpen()) {
                Object uid = peer.getUserProperties().get("userId");
                if (uid != null && uid.equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void broadcastMessage(int sessionId, int senderId, String senderRole, String senderName,
                                        int messageId, String content, String mediaUrl, String mediaType,
                                        String createdAtStr) {
        Set<Session> room = ROOM_MAP.get(sessionId);
        if (room == null) {
            return;
        }

        for (Session peer : room) {
            if (!peer.isOpen()) {
                continue;
            }
            try {
                String peerRole = (String) peer.getUserProperties().get("userRole");
                boolean peerIsAdmin = AppConfig.ROLE_ADMIN.equals(peerRole);
                String displayName = senderName != null ? senderName : "";
                if (!peerIsAdmin && AppConfig.ROLE_ADMIN.equals(senderRole)) {
                    displayName = "Hỗ trợ Admin";
                }

                String responseJson = buildMessageJson(
                        messageId,
                        senderId,
                        displayName,
                        senderRole,
                        content,
                        mediaUrl,
                        mediaType,
                        createdAtStr
                );
                peer.getBasicRemote().sendText(responseJson);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "ChatEndpoint: không gửi được tới peer", e);
            }
        }
    }

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

        try {
            chatDeliveryService.sendMessage(
                    sessionId,
                    senderId,
                    senderRole,
                    senderName,
                    content,
                    mediaUrl,
                    mediaType
            );
            return;
        } catch (IllegalArgumentException e) {
            sendError(wsSession, ErrorMessageUtil.getUserMessage(e));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "ChatEndpoint.onMessage: lỗi lưu tin nhắn", e);
            sendError(wsSession, "Lỗi lưu tin nhắn.");
        }
    }

    @OnClose
    public void onClose(Session wsSession, CloseReason reason) {
        removeFromRoom(wsSession);
    }

    @OnError
    public void onError(Session wsSession, Throwable throwable) {
        LOG.log(Level.WARNING, "ChatEndpoint.onError", throwable);
        removeFromRoom(wsSession);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Xóa wsSession khỏi ROOM_MAP và dọn room rỗng.
     * Được gọi từ cả @OnClose lẫn @OnError để đảm bảo mọi con đường thoát
     * đều giải phóng session — tránh rò rỉ bộ nhớ.
     *
     * Thread-safe: ConcurrentHashMap.computeIfPresent + CopyOnWriteArraySet.remove
     * đảm bảo an toàn khi nhiều thread gọi đồng thời.
     */
    private void removeFromRoom(Session wsSession) {
        Integer sessionId = (Integer) wsSession.getUserProperties().get("sessionId");
        if (sessionId == null) return;
        ROOM_MAP.computeIfPresent(sessionId, (id, room) -> {
            room.remove(wsSession);
            return room.isEmpty() ? null : room;  // trả về null → ConcurrentHashMap tự xóa entry
        });
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
            LOG.log(Level.WARNING, "ChatEndpoint.sendError", e);
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
    private static String buildMessageJson(int messageId, int senderId, String senderName, String senderRole, String content, String mediaUrl, String mediaType, String createdAt) {
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
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
