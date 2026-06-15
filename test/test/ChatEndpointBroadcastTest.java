package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import dao.chat.NotificationDAO;
import org.junit.Test;
import websocket.ChatEndpoint;
import util.HashUtil;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChatEndpointBroadcastTest {

    private final UserDAO userDAO = new UserDAO();
    private final ChatDAO chatDAO = new ChatDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Test
    public void broadcastMessage_sendsExactlyOnePayloadPerOpenSession() throws Exception {
        Map<Integer, Set<Session>> roomMap = getRoomMap();
        roomMap.clear();

        List<String> senderPayloads = new ArrayList<>();
        List<String> recipientPayloads = new ArrayList<>();

        Session sender = createSessionMock(1, AppConfig.ROLE_CUSTOMER, senderPayloads);
        Session recipient = createSessionMock(2, AppConfig.ROLE_ADMIN, recipientPayloads);

        roomMap.put(77, new java.util.concurrent.CopyOnWriteArraySet<>(List.of(sender, recipient)));

        try {
            ChatEndpoint.broadcastMessage(
                    77,
                    1,
                    AppConfig.ROLE_CUSTOMER,
                    "Alice",
                    9001,
                    "Xin chào",
                    null,
                    null,
                    "2026-06-14T12:00:00"
            );

            assertEquals(1, senderPayloads.size());
            assertEquals(1, recipientPayloads.size());
            assertTrue(senderPayloads.get(0).contains("\"messageId\":9001"));
            assertTrue(recipientPayloads.get(0).contains("\"messageId\":9001"));
            assertTrue(senderPayloads.get(0).contains("\"senderName\":\"Alice\""));
        } finally {
            roomMap.clear();
        }
    }

    @Test
    public void broadcastMessage_masksAdminNameForCustomerPeer() throws Exception {
        Map<Integer, Set<Session>> roomMap = getRoomMap();
        roomMap.clear();

        List<String> customerPayloads = new ArrayList<>();
        List<String> adminPayloads = new ArrayList<>();

        Session customer = createSessionMock(10, AppConfig.ROLE_CUSTOMER, customerPayloads);
        Session admin = createSessionMock(11, AppConfig.ROLE_ADMIN, adminPayloads);

        roomMap.put(78, new java.util.concurrent.CopyOnWriteArraySet<>(List.of(customer, admin)));

        try {
            ChatEndpoint.broadcastMessage(
                    78,
                    11,
                    AppConfig.ROLE_ADMIN,
                    "Trần Admin",
                    9002,
                    "Hỗ trợ",
                    null,
                    null,
                    "2026-06-14T12:30:00"
            );

            assertEquals(1, customerPayloads.size());
            assertEquals(1, adminPayloads.size());
            assertTrue(customerPayloads.get(0).contains("\"senderName\":\"Hỗ trợ Admin\""));
            assertTrue(adminPayloads.get(0).contains("\"senderName\":\"Trần Admin\""));
        } finally {
            roomMap.clear();
        }
    }

    @Test
    public void onMessage_adminReply_pushesToRoomAndCreatesCustomerNotification() throws Exception {
        Map<Integer, Set<Session>> roomMap = getRoomMap();
        roomMap.clear();

        int sessionId = -1;
        int customerId = -1;
        int adminId = -1;
        String adminMessage = "Realtime admin reply " + System.currentTimeMillis();

        List<String> customerPayloads = new ArrayList<>();
        List<String> adminPayloads = new ArrayList<>();

        ChatEndpoint endpoint = new ChatEndpoint();

        try {
            customerId = createUser("WS Customer", "ws-customer-" + System.currentTimeMillis() + "@test.com", AppConfig.ROLE_CUSTOMER);
            adminId = createUser("WS Admin", "ws-admin-" + System.currentTimeMillis() + "@test.com", AppConfig.ROLE_ADMIN);
            sessionId = createChatSession(customerId, adminId, "ADMIN");
            Session customer = createSessionMock(customerId, AppConfig.ROLE_CUSTOMER, "Customer A", sessionId, customerPayloads);
            Session admin = createSessionMock(adminId, AppConfig.ROLE_ADMIN, "Admin A", sessionId, adminPayloads);
            roomMap.put(sessionId, new java.util.concurrent.CopyOnWriteArraySet<>(List.of(customer, admin)));
            customer.getUserProperties().put("sessionId", sessionId);
            admin.getUserProperties().put("sessionId", sessionId);

            String rawMessage = "{\"content\":\"" + adminMessage.replace("\"", "\\\"") + "\"}";
            endpoint.onMessage(admin, rawMessage);

            assertEquals(1, customerPayloads.size());
            assertEquals(1, adminPayloads.size());
            assertTrue(customerPayloads.get(0).contains("\"messageId\":"));
            assertTrue(customerPayloads.get(0).contains("\"senderRole\":\"ADMIN\""));
            assertTrue(customerPayloads.get(0).contains("\"senderName\":\"Admin A\""));
            assertTrue(adminPayloads.get(0).contains("\"senderName\":\"Admin A\""));

            List<model.entity.chat.ChatMessage> messages = chatDAO.findMessages(sessionId);
            assertEquals(1, messages.size());
            assertEquals(adminMessage, messages.get(0).getContent());
            assertEquals(adminId, messages.get(0).getSenderId());

            List<model.entity.chat.Notification> notifications = notificationDAO.findByUser(customerId, true);
            final int targetSessionId = sessionId;
            model.entity.chat.Notification notification = notifications.stream()
                    .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + targetSessionId))
                    .findFirst()
                    .orElse(null);
            assertNotNull(notification);
            assertEquals("/chat?sessionId=" + targetSessionId, notification.getActionUrl());
            assertEquals(adminMessage, notification.getMessage());
        } finally {
            roomMap.clear();
            if (sessionId > 0) {
                cleanupChatArtifacts(sessionId);
            }
            deleteUser(customerId);
            deleteUser(adminId);
        }
    }

    private int createUser(String fullName, String email, String role) throws Exception {
        return userDAO.saveNewCustomer(
                fullName,
                email,
                HashUtil.hashPassword("ws-chat-" + System.currentTimeMillis()),
                "09" + String.format("%08d", System.currentTimeMillis() % 100_000_000L),
                role,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Set<Session>> getRoomMap() throws Exception {
        Field roomMapField = ChatEndpoint.class.getDeclaredField("ROOM_MAP");
        roomMapField.setAccessible(true);
        return (Map<Integer, Set<Session>>) roomMapField.get(null);
    }

    private int createChatSession(int customerId, int ownerId, String sessionType) throws Exception {
        return chatDAO.createSession(customerId, ownerId, sessionType);
    }

    private Session createSessionMock(int userId, String role, List<String> payloadSink) {
        return createSessionMock(userId, role, "Mock User " + userId, null, payloadSink);
    }

    private Session createSessionMock(int userId, String role, String fullName, Integer sessionId, List<String> payloadSink) {
        Map<String, Object> userProps = new HashMap<>();
        userProps.put("userId", userId);
        userProps.put("userRole", role);
        userProps.put("fullName", fullName);
        if (sessionId != null) {
            userProps.put("sessionId", sessionId);
        }

        InvocationHandler basicHandler = (proxy, method, args) -> {
            if ("sendText".equals(method.getName()) && args != null && args.length > 0) {
                payloadSink.add((String) args[0]);
                return null;
            }
            return defaultValue(method.getReturnType());
        };

        RemoteEndpoint.Basic basicRemote = (RemoteEndpoint.Basic) Proxy.newProxyInstance(
                RemoteEndpoint.Basic.class.getClassLoader(),
                new Class<?>[]{RemoteEndpoint.Basic.class},
                basicHandler
        );

        InvocationHandler sessionHandler = (proxy, method, args) -> {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("getUserProperties".equals(name)) {
                return userProps;
            }
            if ("getBasicRemote".equals(name)) {
                return basicRemote;
            }
            if ("isOpen".equals(name)) {
                return true;
            }
            if ("toString".equals(name)) {
                return "MockSession#" + userId;
            }
            return defaultValue(method.getReturnType());
        };

        return (Session) Proxy.newProxyInstance(
                Session.class.getClassLoader(),
                new Class<?>[]{Session.class},
                sessionHandler
        );
    }

    private void cleanupChatArtifacts(int targetSessionId) throws Exception {
        if (targetSessionId <= 0) {
            return;
        }
        try (java.sql.Connection conn = chatDAO.getConnection()) {
            try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE action_url LIKE ?")) {
                ps.setString(1, "%sessionId=" + targetSessionId + "%");
                ps.executeUpdate();
            }
            try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_messages WHERE session_id = ?")) {
                ps.setInt(1, targetSessionId);
                ps.executeUpdate();
            }
            try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_sessions WHERE session_id = ?")) {
                ps.setInt(1, targetSessionId);
                ps.executeUpdate();
            }
        }
    }

    private void deleteUser(int userId) {
        if (userId <= 0) {
            return;
        }
        try (java.sql.Connection conn = chatDAO.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // Best-effort cleanup only.
        }
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == null || !returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }
}
