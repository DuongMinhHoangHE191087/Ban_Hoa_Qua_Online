package service.chat;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import model.entity.auth.User;
import model.entity.chat.ChatMessage;
import model.entity.chat.ChatSession;
import websocket.ChatEndpoint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-path service cho chat bootstrap và phân trang lịch sử.
 */
// Touched for IDE re-indexing
public class ChatReadService {

    private static final int DEFAULT_MESSAGE_LIMIT = 30;
    private static final int MAX_MESSAGE_LIMIT = 100;

    private final ChatDAO chatDAO = new ChatDAO();
    private final UserDAO userDAO = new UserDAO();

    public Map<String, Object> loadBootstrap(User currentUser, Integer requestedSessionId, Integer requestedLimit)
            throws SQLException {
        List<ChatSession> sessions = loadSessions(currentUser);
        maskAdminSessionNames(currentUser, sessions);

        ChatSession activeSession = resolveActiveSession(currentUser, sessions, requestedSessionId);
        Map<String, Object> messagePage = Collections.emptyMap();
        if (activeSession != null) {
            ensurePartnerPresentation(activeSession, currentUser);
            messagePage = loadMessagePage(currentUser, activeSession.getSessionId(), null, requestedLimit);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessions", sessions);
        response.put("activeSession", activeSession);
        response.put("messages", messagePage.getOrDefault("messages", Collections.emptyList()));
        response.put("hasMoreMessages", messagePage.getOrDefault("hasMore", false));
        response.put("nextBeforeMessageId", messagePage.get("nextBeforeMessageId"));
        response.put("presence", buildPresence(activeSession, currentUser));
        response.put("currentUser", buildCurrentUserMap(currentUser));
        response.put("currentUserId", currentUser.getUserId());
        return response;
    }

    public Map<String, Object> loadMessagePage(User currentUser, int sessionId, Integer beforeMessageId, Integer requestedLimit)
            throws SQLException {
        ChatSession session = requireAccessibleSession(currentUser, sessionId);
        int limit = normalizeMessageLimit(requestedLimit);

        chatDAO.markRead(sessionId, currentUser.getUserId());
        List<ChatMessage> messages = chatDAO.findMessages(sessionId, beforeMessageId, limit + 1);
        maskAdminMessages(currentUser, messages);

        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = new ArrayList<>(messages.subList(1, messages.size()));
        }

        Integer nextBeforeMessageId = null;
        if (!messages.isEmpty()) {
            nextBeforeMessageId = messages.get(0).getMessageId();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("messages", messages);
        response.put("currentUserId", currentUser.getUserId());
        response.put("hasMore", hasMore);
        response.put("nextBeforeMessageId", nextBeforeMessageId);
        response.put("activeSession", session);
        return response;
    }

    private List<ChatSession> loadSessions(User currentUser) throws SQLException {
        String role = currentUser.getRole();
        if (AppConfig.ROLE_SHOP_OWNER.equals(role)) {
            return chatDAO.findSessionsByOwner(currentUser.getUserId());
        }
        if (AppConfig.ROLE_CUSTOMER.equals(role)) {
            return chatDAO.findSessionsByCustomer(currentUser.getUserId());
        }
        if (AppConfig.ROLE_ADMIN.equals(role)) {
            return chatDAO.findAllSessions();
        }
        return new ArrayList<>();
    }

    private ChatSession resolveActiveSession(User currentUser, List<ChatSession> sessions, Integer requestedSessionId)
            throws SQLException {
        if (requestedSessionId != null && requestedSessionId > 0) {
            for (ChatSession session : sessions) {
                if (session.getSessionId() == requestedSessionId) {
                    return session;
                }
            }
            ChatSession direct = requireAccessibleSession(currentUser, requestedSessionId);
            ensurePartnerPresentation(direct, currentUser);
            return direct;
        }
        if (sessions == null || sessions.isEmpty()) {
            return null;
        }
        ChatSession active = sessions.get(0);
        ensurePartnerPresentation(active, currentUser);
        return active;
    }

    private ChatSession requireAccessibleSession(User currentUser, int sessionId) throws SQLException {
        if (sessionId <= 0) {
            throw new ChatApiException(HttpStatus.BAD_REQUEST, "Session không hợp lệ");
        }
        ChatSession session = chatDAO.findSessionById(sessionId);
        if (session == null) {
            throw new ChatApiException(HttpStatus.NOT_FOUND, "Session không tồn tại");
        }
        if (!isAuthorized(currentUser, session)) {
            throw new ChatApiException(HttpStatus.FORBIDDEN, "Không có quyền truy cập");
        }
        return session;
    }

    private boolean isAuthorized(User currentUser, ChatSession session) {
        if (currentUser == null || session == null) {
            return false;
        }
        if (AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            return true;
        }
        int userId = currentUser.getUserId();
        return session.getCustomerId() == userId || session.getOwnerId() == userId;
    }

    private int normalizeMessageLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_MESSAGE_LIMIT;
        }
        return Math.min(requestedLimit, MAX_MESSAGE_LIMIT);
    }

    private void maskAdminSessionNames(User currentUser, List<ChatSession> sessions) {
        if (currentUser == null || AppConfig.ROLE_ADMIN.equals(currentUser.getRole()) || sessions == null) {
            return;
        }
        for (ChatSession session : sessions) {
            if ("ADMIN".equalsIgnoreCase(session.getSessionType())) {
                session.setPartnerName("Hỗ trợ Admin");
            }
        }
    }

    private void maskAdminMessages(User currentUser, List<ChatMessage> messages) {
        if (currentUser == null || AppConfig.ROLE_ADMIN.equals(currentUser.getRole()) || messages == null) {
            return;
        }
        for (ChatMessage message : messages) {
            if (AppConfig.ROLE_ADMIN.equals(message.getSenderRole())) {
                message.setSenderName("Hỗ trợ Admin");
            }
        }
    }

    private void ensurePartnerPresentation(ChatSession session, User currentUser) throws SQLException {
        if (session == null) {
            return;
        }
        if (session.getPartnerName() != null && !session.getPartnerName().isBlank()) {
            if (!AppConfig.ROLE_ADMIN.equals(currentUser.getRole()) && "ADMIN".equalsIgnoreCase(session.getSessionType())) {
                session.setPartnerName("Hỗ trợ Admin");
            }
            return;
        }

        int partnerId = resolvePartnerId(session, currentUser);
        if (partnerId <= 0) {
            return;
        }

        User partner = userDAO.findUserById(partnerId);
        if (partner == null) {
            return;
        }
        if (!AppConfig.ROLE_ADMIN.equals(currentUser.getRole()) && AppConfig.ROLE_ADMIN.equals(partner.getRole())) {
            session.setPartnerName("Hỗ trợ Admin");
        } else {
            session.setPartnerName(partner.getFullName());
        }
        session.setPartnerAvatar(partner.getAvatarUrl());
    }

    private int resolvePartnerId(ChatSession session, User currentUser) {
        if (session == null || currentUser == null) {
            return -1;
        }
        if (AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            return session.getCustomerId();
        }
        return session.getCustomerId() == currentUser.getUserId() ? session.getOwnerId() : session.getCustomerId();
    }

    private Map<String, Object> buildPresence(ChatSession activeSession, User currentUser) {
        Map<String, Object> presence = new LinkedHashMap<>();
        if (activeSession == null || currentUser == null) {
            return presence;
        }

        int currentUserId = currentUser.getUserId();
        int partnerId = resolvePartnerId(activeSession, currentUser);
        presence.put(String.valueOf(currentUserId), true);
        if (partnerId > 0) {
            presence.put(String.valueOf(partnerId), ChatEndpoint.isUserOnline(activeSession.getSessionId(), partnerId));
        }
        return presence;
    }

    private Map<String, Object> buildCurrentUserMap(User currentUser) {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("userId", currentUser.getUserId());
        userMap.put("role", currentUser.getRole());
        userMap.put("fullName", currentUser.getFullName());
        return userMap;
    }

    public static class ChatApiException extends RuntimeException {
        private final int statusCode;

        public ChatApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getPublicMessage() {
            return getMessage();
        }
    }

    public static final class HttpStatus {
        public static final int BAD_REQUEST = 400;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;

        private HttpStatus() {
        }
    }
}
