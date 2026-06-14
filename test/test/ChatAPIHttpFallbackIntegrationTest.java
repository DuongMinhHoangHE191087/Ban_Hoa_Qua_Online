package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import dao.chat.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.chat.ChatMessage;
import model.entity.chat.Notification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import servlet.api.chat.ChatAPI;
import util.HashUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChatAPIHttpFallbackIntegrationTest {

    private final ChatAPIHarness servlet = new ChatAPIHarness();
    private final ChatDAO chatDAO = new ChatDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();

    private MockHttpEnvironment env;
    private int sessionId = -1;
    private int senderUserId = -1;
    private int recipientUserId = -1;
    private String csrfToken;
    private String sentMessage;

    @Before
    public void setUp() throws Exception {
        env = new MockHttpEnvironment();
        csrfToken = "csrf-chat-api-" + System.currentTimeMillis();
        sentMessage = "HTTP fallback message " + System.currentTimeMillis();
        long now = System.currentTimeMillis();

        senderUserId = createUser("Chat API Sender", "chat-api-sender-" + now + "@test.com",
                String.format("090%07d", now % 10_000_000L), AppConfig.ROLE_CUSTOMER);
        recipientUserId = createUser("Chat API Shop", "chat-api-shop-" + now + "@test.com",
                String.format("091%07d", now % 10_000_000L), AppConfig.ROLE_SHOP_OWNER);
        sessionId = chatDAO.createSession(senderUserId, recipientUserId, "SHOP");

        User sender = userDAO.findUserById(senderUserId);
        assertNotNull(sender);
        env.setCurrentUser(sender);
        env.putSessionAttribute(AppConfig.SESSION_CSRF_TOKEN, csrfToken);
        clearRoomMap();
    }

    @After
    public void tearDown() {
        cleanupTestData();
    }

    @Test
    public void sendMessage_viaHttpFallback_persistsAndNotifiesOfflineRecipient() throws Exception {
        env.clearRequestState();
        env.putParam("action", "sendMessage");
        env.putParam("sessionId", String.valueOf(sessionId));
        env.putParam("content", sentMessage);
        env.putParam("mediaUrl", "");
        env.putParam("mediaType", "");
        env.putParam("_csrf", csrfToken);

        servlet.doPostPublic(env.request, env.response);

        assertEquals(HttpServletResponse.SC_OK, env.status);
        assertTrue(env.getResponseBody().contains("\"success\":true"));
        assertTrue(env.getResponseBody().contains("Đã gửi tin nhắn"));

        List<ChatMessage> messages = chatDAO.findMessages(sessionId);
        assertEquals(1, messages.size());
        assertEquals(sentMessage, messages.get(0).getContent());
        assertEquals(senderUserId, messages.get(0).getSenderId());

        List<Notification> notifications = notificationDAO.findByUser(recipientUserId, true);
        assertEquals(1, notifications.size());
        Notification notification = notifications.get(0);
        assertEquals("Bạn có tin nhắn mới", notification.getTitle());
        assertEquals(sentMessage, notification.getMessage());
        assertEquals("/shop/chat?sessionId=" + sessionId, notification.getActionUrl());
    }

    @Test
    public void sendMessage_asAdminReply_persistsAndLinksCustomerNotificationToChat() throws Exception {
        int adminUserId = -1;
        int adminSessionId = -1;
        String adminMessage = "Admin reply " + System.currentTimeMillis();

        try {
            adminUserId = createUser("Chat API Admin", "chat-api-admin-" + System.currentTimeMillis() + "@test.com",
                    String.format("092%07d", System.currentTimeMillis() % 10_000_000L), AppConfig.ROLE_ADMIN);
            adminSessionId = chatDAO.createSession(senderUserId, adminUserId, "ADMIN");

            User admin = userDAO.findUserById(adminUserId);
            assertNotNull(admin);
            env.setCurrentUser(admin);

            clearRoomMap();
            env.clearRequestState();
            env.putParam("action", "sendMessage");
            env.putParam("sessionId", String.valueOf(adminSessionId));
            env.putParam("content", adminMessage);
            env.putParam("mediaUrl", "");
            env.putParam("mediaType", "");
            env.putParam("_csrf", csrfToken);

            servlet.doPostPublic(env.request, env.response);

            assertEquals(HttpServletResponse.SC_OK, env.status);
            assertTrue(env.getResponseBody().contains("\"success\":true"));
            assertTrue(env.getResponseBody().contains("Đã gửi tin nhắn"));

            List<ChatMessage> messages = chatDAO.findMessages(adminSessionId);
            assertEquals(1, messages.size());
            assertEquals(adminMessage, messages.get(0).getContent());
            assertEquals(adminUserId, messages.get(0).getSenderId());
            assertEquals("ADMIN", messages.get(0).getSenderRole());

            List<Notification> notifications = notificationDAO.findByUser(senderUserId, true);
            Notification notification = notifications.stream()
                    .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + adminSessionId))
                    .findFirst()
                    .orElse(null);
            assertNotNull(notification);
            assertEquals("Bạn có tin nhắn mới", notification.getTitle());
            assertEquals(adminMessage, notification.getMessage());
            assertEquals("/chat?sessionId=" + adminSessionId, notification.getActionUrl());
        } finally {
            try {
                if (adminSessionId > 0) {
                    cleanupChatArtifacts(adminSessionId);
                }
            } finally {
                deleteUser(adminUserId);
            }
        }
    }

    private int createUser(String fullName, String email, String phone, String role) throws SQLException {
        String passwordHash = HashUtil.hashPassword("chat-api-test-" + System.currentTimeMillis());
        return userDAO.saveNewCustomer(
                fullName,
                email,
                passwordHash,
                phone,
                role,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
    }

    private void cleanupTestData() {
        try {
            clearRoomMap();
            cleanupChatArtifacts(sessionId);
            deleteUser(senderUserId);
            deleteUser(recipientUserId);
        } catch (Exception ignored) {
            // Best-effort cleanup only.
        } finally {
            sessionId = -1;
            senderUserId = -1;
            recipientUserId = -1;
            csrfToken = null;
            sentMessage = null;
        }
    }

    private void cleanupChatArtifacts(int targetSessionId) throws SQLException {
        if (targetSessionId <= 0) {
            return;
        }
        try (Connection conn = chatDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE action_url LIKE ?")) {
                ps.setString(1, "%sessionId=" + targetSessionId + "%");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_messages WHERE session_id = ?")) {
                ps.setInt(1, targetSessionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_sessions WHERE session_id = ?")) {
                ps.setInt(1, targetSessionId);
                ps.executeUpdate();
            }
        }
    }

    private void deleteUser(int userId) throws SQLException {
        if (userId <= 0) {
            return;
        }
        try (Connection conn = chatDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    @SuppressWarnings("unchecked")
    private void clearRoomMap() {
        try {
            java.lang.reflect.Field roomMapField = websocket.ChatEndpoint.class.getDeclaredField("ROOM_MAP");
            roomMapField.setAccessible(true);
            Map<Integer, java.util.Set<jakarta.websocket.Session>> roomMap =
                    (Map<Integer, java.util.Set<jakarta.websocket.Session>>) roomMapField.get(null);
            roomMap.clear();
        } catch (Exception ignored) {
            // Best-effort only.
        }
    }

    private static final class ChatAPIHarness extends ChatAPI {
        void doPostPublic(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
            super.doPost(request, response);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();
        private final StringWriter responseBody = new StringWriter();
        private final PrintWriter responseWriter = new PrintWriter(responseBody, true);

        private Integer status;
        private String contentType;
        private String characterEncoding;

        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private MockHttpEnvironment() {
            this.session = createSessionProxy();
            this.request = createRequestProxy();
            this.response = createResponseProxy();
        }

        private void setCurrentUser(User user) {
            sessionAttributes.put(AppConfig.SESSION_USER, user);
        }

        private void putSessionAttribute(String name, Object value) {
            sessionAttributes.put(name, value);
        }

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private void putHeader(String name, String value) {
            headers.put(name, value);
        }

        private void clearRequestState() {
            params.clear();
            headers.clear();
            status = null;
            contentType = null;
            characterEncoding = null;
            responseBody.getBuffer().setLength(0);
            responseWriter.flush();
        }

        private String getResponseBody() {
            responseWriter.flush();
            return responseBody.toString();
        }

        private HttpSession createSessionProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAttribute":
                        return sessionAttributes.get(args[0]);
                    case "setAttribute":
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    case "removeAttribute":
                        sessionAttributes.remove(args[0]);
                        return null;
                    case "invalidate":
                        sessionAttributes.clear();
                        return null;
                    case "toString":
                        return "MockHttpSession";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    handler
            );
        }

        private HttpServletRequest createRequestProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getParameter":
                        return params.get(args[0]);
                    case "getHeader":
                        return headers.get(args[0]);
                    case "getSession":
                        return session;
                    case "toString":
                        return "MockHttpServletRequest";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler
            );
        }

        private HttpServletResponse createResponseProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getWriter":
                        return responseWriter;
                    case "setStatus":
                        status = (Integer) args[0];
                        return null;
                    case "setContentType":
                        contentType = (String) args[0];
                        return null;
                    case "setCharacterEncoding":
                        characterEncoding = (String) args[0];
                        return null;
                    case "sendError":
                        status = (Integer) args[0];
                        return null;
                    case "sendRedirect":
                        return null;
                    case "toString":
                        return "MockHttpServletResponse";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler
            );
        }

        private Object defaultValue(Class<?> type) {
            if (type == null || !type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == char.class) return '\0';
            return null;
        }
    }
}
