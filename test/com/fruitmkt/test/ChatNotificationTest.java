package com.fruitmkt.test;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.dao.NotificationDAO;
import com.fruitmkt.model.entity.ChatSession;
import com.fruitmkt.model.entity.Notification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ChatNotificationTest — JUnit 4 Test cho cơ chế chống spam thông báo khi chat.
 * Đảm bảo: Chỉ gửi duy nhất 1 thông báo chưa đọc cho mỗi session chat nếu người nhận offline.
 *
 * @author fruitmkt-team
 */
public class ChatNotificationTest {

    private ChatDAO chatDAO;
    private NotificationDAO notificationDAO;
    private int testSessionId = 0;
    
    // Giả lập 2 user test
    private final int senderId = 1;
    private final int recipientId = 2; 

    @Before
    public void setUp() throws SQLException {
        chatDAO = new ChatDAO();
        notificationDAO = new NotificationDAO();

        // Đảm bảo DB đã cập nhật các cột mới phục vụ test
        try (Connection conn = chatDAO.getConnection()) {
            // Cập nhật chat_sessions.session_type
            if (!hasColumn(conn, "chat_sessions", "session_type")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE chat_sessions ADD session_type NVARCHAR(10) NOT NULL DEFAULT 'SHOP'");
                }
            }
            // Cho phép content NULL
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE chat_messages ALTER COLUMN content NVARCHAR(MAX) NULL");
            }
            // Cập nhật chat_messages.media_url
            if (!hasColumn(conn, "chat_messages", "media_url")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE chat_messages ADD media_url NVARCHAR(500) NULL");
                }
            }
            // Cập nhật chat_messages.media_type
            if (!hasColumn(conn, "chat_messages", "media_type")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE chat_messages ADD media_type NVARCHAR(10) NULL");
                }
            }
        }

        // Dọn dẹp trước nếu có rác
        cleanupTestRecords();

        // Tạo 1 session chat test mới
        testSessionId = chatDAO.createSession(senderId, recipientId, "SHOP");
        System.out.println("Created test chat session: " + testSessionId);
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID(?) AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @After
    public void tearDown() throws SQLException {
        // Dọn dẹp dữ liệu test sau khi chạy xong
        cleanupTestRecords();
    }

    /**
     * Hàm dọn dẹp dữ liệu test trong DB
     */
    private void cleanupTestRecords() throws SQLException {
        try (Connection conn = chatDAO.getConnection()) {
            // Xóa notifications test
            String sqlNotif = "DELETE FROM notifications WHERE action_url LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlNotif)) {
                ps.setString(1, "%sessionId=" + testSessionId + "%");
                ps.executeUpdate();
            }

            if (testSessionId > 0) {
                // Xóa tin nhắn test
                String sqlMsgs = "DELETE FROM chat_messages WHERE session_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlMsgs)) {
                    ps.setInt(1, testSessionId);
                    ps.executeUpdate();
                }

                // Xóa session test
                String sqlSession = "DELETE FROM chat_sessions WHERE session_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlSession)) {
                    ps.setInt(1, testSessionId);
                    ps.executeUpdate();
                }
            }
        }
    }

    /**
     * Logic mô phỏng kiểm tra chống spam thông báo khi gửi tin nhắn offline.
     * Trả về true nếu có tạo thông báo mới, false nếu bỏ qua do bị chặn spam.
     */
    private boolean simulateSendOfflineNotification(int sessionId, int fromUid, int toUid) throws SQLException {
        // Kiểm tra xem đã có thông báo chưa đọc liên quan tới session này cho recipient chưa
        boolean hasPending = false;
        String checkSql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0 AND action_url LIKE ?";
        try (Connection conn = chatDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, toUid);
            ps.setString(2, "%sessionId=" + sessionId + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasPending = true;
                }
            }
        }

        if (!hasPending) {
            // Chưa có -> Tạo thông báo mới
            Notification notif = new Notification();
            notif.setUserId(toUid);
            notif.setType(AppConfig.NOTIF_SYSTEM);
            notif.setTitle("Bạn có tin nhắn mới");
            notif.setMessage("Test message preview");
            notif.setActionUrl("/chat?sessionId=" + sessionId);
            notif.setIsRead(false);
            notificationDAO.save(notif);
            return true;
        }
        
        // Đã có -> Bỏ qua
        return false;
    }

    @Test
    public void testOfflineNotificationAntiSpamLogic() throws SQLException {
        System.out.println("--- Testing Anti-Spam Notification Logic ---");

        // 1. Gửi tin nhắn đầu tiên (Recipient offline) -> Phải tạo thông báo
        boolean firstSent = simulateSendOfflineNotification(testSessionId, senderId, recipientId);
        assertTrue("Tin nhắn đầu tiên phải tạo thông báo mới", firstSent);

        // Đếm số lượng thông báo chưa đọc của recipient liên quan tới session này (phải là 1)
        List<Notification> notifsAfterFirst = notificationDAO.findByUser(recipientId, true);
        long count = notifsAfterFirst.stream()
                .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + testSessionId))
                .count();
        assertEquals("Số lượng thông báo chưa đọc trong DB phải là 1", 1, count);

        // 2. Gửi tiếp tin nhắn thứ hai lập tức -> Không được tạo thêm thông báo (chống spam)
        boolean secondSent = simulateSendOfflineNotification(testSessionId, senderId, recipientId);
        assertFalse("Tin nhắn thứ hai không được tạo thêm thông báo (phải bị chặn spam)", secondSent);

        // Kiểm tra lại trong DB: Số lượng thông báo vẫn phải là 1, không tăng lên 2
        List<Notification> notifsAfterSecond = notificationDAO.findByUser(recipientId, true);
        long countAfterSecond = notifsAfterSecond.stream()
                .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + testSessionId))
                .count();
        assertEquals("Số lượng thông báo chưa đọc trong DB vẫn phải duy trì là 1", 1, countAfterSecond);

        // 3. Giả lập recipient đọc thông báo (đánh dấu là đã đọc)
        Notification pendingNotif = notifsAfterSecond.stream()
                .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + testSessionId))
                .findFirst()
                .orElse(null);
        assertNotNull(pendingNotif);
        notificationDAO.markRead(pendingNotif.getNotificationId());

        // Đếm lại số thông báo chưa đọc (lúc này phải là 0)
        List<Notification> notifsAfterRead = notificationDAO.findByUser(recipientId, true);
        long countAfterRead = notifsAfterRead.stream()
                .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + testSessionId))
                .count();
        assertEquals("Sau khi đọc, số lượng thông báo chưa đọc phải là 0", 0, countAfterRead);

        // 4. Gửi tin nhắn thứ ba -> Vì thông báo cũ đã được đọc, lúc này phải tạo một thông báo mới
        boolean thirdSent = simulateSendOfflineNotification(testSessionId, senderId, recipientId);
        assertTrue("Sau khi đã đọc, tin nhắn mới tiếp theo phải tạo lại thông báo", thirdSent);

        // Kiểm tra trong DB: Lại xuất hiện 1 thông báo chưa đọc mới
        List<Notification> notifsAfterThird = notificationDAO.findByUser(recipientId, true);
        long countAfterThird = notifsAfterThird.stream()
                .filter(n -> n.getActionUrl() != null && n.getActionUrl().contains("sessionId=" + testSessionId))
                .count();
        assertEquals("Phải có 1 thông báo chưa đọc mới được tạo", 1, countAfterThird);
        
        System.out.println("Anti-Spam Notification Test Passed successfully!");
    }
}
