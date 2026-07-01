package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.ChatDAO;
import model.entity.auth.User;
import model.entity.chat.ChatMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.chat.ChatReadService;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatReadServiceTest {

    private final UserDAO userDAO = new UserDAO();
    private final ChatDAO chatDAO = new ChatDAO();
    private final ChatReadService chatReadService = new ChatReadService();

    private final List<Integer> createdUserIds = new ArrayList<>();
    private final List<Integer> createdSessionIds = new ArrayList<>();

    @Before
    public void setUp() {
        createdUserIds.clear();
        createdSessionIds.clear();
    }

    @After
    public void tearDown() throws Exception {
        try (Connection conn = chatDAO.getConnection()) {
            for (Integer sessionId : createdSessionIds) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_messages WHERE session_id = ?")) {
                    ps.setInt(1, sessionId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM chat_sessions WHERE session_id = ?")) {
                    ps.setInt(1, sessionId);
                    ps.executeUpdate();
                }
            }
            for (Integer userId : createdUserIds) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }
        }
    }

    @Test
    public void loadBootstrap_limitsMessagesAndReturnsPagingCursor() throws Exception {
        int customerId = createUser("Bootstrap Customer", "bootstrap-customer-" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_CUSTOMER);
        int ownerId = createUser("Bootstrap Shop", "bootstrap-shop-" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_SHOP_OWNER);
        int sessionId = chatDAO.createSession(customerId, ownerId, "SHOP");
        createdSessionIds.add(sessionId);

        saveMessage(sessionId, customerId, "message-1");
        saveMessage(sessionId, ownerId, "message-2");
        saveMessage(sessionId, customerId, "message-3");

        User customer = userDAO.findUserById(customerId);
        Map<String, Object> payload = chatReadService.loadBootstrap(customer, sessionId, 2);

        @SuppressWarnings("unchecked")
        List<ChatMessage> messages = (List<ChatMessage>) payload.get("messages");
        assertEquals(2, messages.size());
        assertEquals("message-2", messages.get(0).getContent());
        assertEquals("message-3", messages.get(1).getContent());
        assertTrue(Boolean.TRUE.equals(payload.get("hasMoreMessages")));
        assertEquals(messages.get(0).getMessageId(), payload.get("nextBeforeMessageId"));
    }

    @Test
    public void loadMessagePage_masksAdminSenderNameForCustomer() throws Exception {
        int customerId = createUser("Support Customer", "support-customer-" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_CUSTOMER);
        int adminId = createUser("Support Admin", "support-admin-" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_ADMIN);
        int sessionId = chatDAO.createSession(customerId, adminId, "ADMIN");
        createdSessionIds.add(sessionId);

        saveMessage(sessionId, adminId, "Xin chào từ admin");

        User customer = userDAO.findUserById(customerId);
        Map<String, Object> payload = chatReadService.loadMessagePage(customer, sessionId, null, 10);

        @SuppressWarnings("unchecked")
        List<ChatMessage> messages = (List<ChatMessage>) payload.get("messages");
        assertFalse(messages.isEmpty());
        assertEquals("Hỗ trợ Admin", messages.get(0).getSenderName());
        assertEquals(AppConfig.ROLE_ADMIN, messages.get(0).getSenderRole());
    }

    private int createUser(String fullName, String email, String role) throws Exception {
        int userId = userDAO.saveNewCustomer(
                fullName,
                email,
                HashUtil.hashPassword("chat-read-test-" + System.nanoTime()),
                String.format("09%08d", Math.abs(email.hashCode()) % 100000000),
                role,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
        createdUserIds.add(userId);
        return userId;
    }

    private void saveMessage(int sessionId, int senderId, String content) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setIsRead(false);
        chatDAO.saveMessage(message);
    }
}
