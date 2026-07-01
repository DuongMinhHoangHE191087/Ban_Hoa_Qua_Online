package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.chat.NotificationDAO;
import model.entity.chat.Notification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.chat.NotificationService;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * NotificationServiceScopedTest — Regression cho thông báo theo user.
 */
public class NotificationServiceScopedTest {

    private UserDAO userDAO;
    private NotificationDAO notificationDAO;
    private NotificationService notificationService;

    private int userAId = -1;
    private int userBId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        notificationDAO = new NotificationDAO();
        notificationService = new NotificationService();

        long ts = System.currentTimeMillis();
        userAId = userDAO.saveNewCustomer(
                "Notification User A",
                "notif_user_a_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(1),
                AppConfig.ROLE_CUSTOMER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true);
        userBId = userDAO.saveNewCustomer(
                "Notification User B",
                "notif_user_b_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(2),
                AppConfig.ROLE_SHOP_OWNER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true);
    }

    @After
    public void tearDown() {
        try (Connection conn = notificationDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE user_id IN (?, ?)")) {
            ps.setInt(1, userAId);
            ps.setInt(2, userBId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }

        try {
            if (userAId > 0) {
                userDAO.deleteUser(userAId);
            }
            if (userBId > 0) {
                userDAO.deleteUser(userBId);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    public void shouldCountUnreadAndLimitRecentPerUser() throws SQLException {
        createNotification(userAId, "Old unread", "Unread #1", false);
        createNotification(userAId, "Middle read", "Read #1", true);
        createNotification(userAId, "Newest unread", "Unread #2", false);
        createNotification(userBId, "Other user", "Unread other user", false);

        assertEquals(2, notificationService.countUnreadByUser(userAId));
        assertEquals(1, notificationService.countUnreadByUser(userBId));

        List<Notification> recent = notificationService.getRecentNotifications(userAId, 2);
        assertEquals(2, recent.size());
        assertEquals("Newest unread", recent.get(0).getTitle());
        assertEquals("Middle read", recent.get(1).getTitle());
    }

    @Test
    public void shouldRejectOtherUsersButStayIdempotentForOwner() throws SQLException {
        int readNotifId = createNotification(userAId, "Owner read", "Read me", false);
        int deleteNotifId = createNotification(userAId, "Owner delete", "Delete me", false);

        notificationService.markRead(readNotifId, userAId);
        notificationService.markRead(readNotifId, userAId);
        assertEquals(1, notificationService.countUnreadByUser(userAId));

        expectSecurityException(() -> notificationService.markRead(readNotifId, userBId));
        expectSecurityException(() -> notificationService.delete(deleteNotifId, userBId));

        notificationService.delete(deleteNotifId, userAId);
        notificationService.delete(deleteNotifId, userAId);
        assertEquals(0, notificationService.countUnreadByUser(userAId));
    }

    private int createNotification(int userId, String title, String message, boolean isRead) throws SQLException {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(AppConfig.NOTIF_SYSTEM);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setActionUrl(null);
        notif.setIsRead(isRead);
        int notifId = notificationDAO.save(notif);
        if (notifId <= 0) {
            fail("Notification id must be created");
        }
        return notifId;
    }

    private void expectSecurityException(SqlRunnable action) throws SQLException {
        try {
            action.run();
            fail("Expected SecurityException");
        } catch (SecurityException expected) {
            // Expected
        }
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws SQLException;
    }
}
