package service.chat;

import dao.chat.NotificationDAO;
import model.dto.common.PagedResultDTO;
import model.entity.chat.Notification;
import java.sql.SQLException;
import java.util.List;
import util.PaginationUtil;

/**
 * NotificationService — Quản lý gửi và hiển thị thông báo trong hệ thống.
 */
public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();



    public void send(int userId, String type, String title, String message, String actionUrl) throws SQLException {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type); // 'ORDER_UPDATE', 'PROMOTION', 'SYSTEM', 'INVENTORY_ALERT', 'PAYMENT'
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setActionUrl(actionUrl);
        notif.setIsRead(false);
        notificationDAO.save(notif);
    }

    public List<Notification> getUnread(int userId) throws SQLException {
        return notificationDAO.findByUser(userId, true);
    }

    public List<Notification> getAllNotifications(int userId) throws SQLException {
        return notificationDAO.findByUser(userId, false);
    }

    public int countUnreadByUser(int userId) throws SQLException {
        return notificationDAO.countUnreadByUser(userId);
    }

    public List<Notification> getRecentNotifications(int userId, int limit) throws SQLException {
        return notificationDAO.findRecentByUser(userId, limit);
    }

    public void markRead(int notifId) throws SQLException {
        notificationDAO.markRead(notifId);
    }

    public void markRead(int notifId, int userId) throws SQLException {
        int affected = notificationDAO.markRead(notifId, userId);
        if (affected == 0) {
            ensureNotificationOwnershipIfExists(notifId, userId);
        }
    }

    public void markAllRead(int userId) throws SQLException {
        notificationDAO.markAllRead(userId);
    }
    public List<Notification> getAllSystemNotifications() throws SQLException {
        return notificationDAO.findAllSystemNotifications();
    }

    public PagedResultDTO getAllSystemNotificationsPaged(int page, int pageSize) throws SQLException {
        int validatedPage = PaginationUtil.validatePage(page);
        int validatedPageSize = PaginationUtil.validatePageSize(pageSize);
        List<Notification> notifications = notificationDAO.findAllSystemNotifications(validatedPage, validatedPageSize);
        int totalCount = notificationDAO.countAllSystemNotifications();
        return PaginationUtil.buildPagedResult(notifications, validatedPage, validatedPageSize, totalCount);
    }

    public void sendBroadcast(String title, String message, String target) throws SQLException {
        if ("ALL".equalsIgnoreCase(target)) {
            notificationDAO.insertForAll(title, message);
        } else {
            notificationDAO.insertForRole(title, message, target);
        }
    }

    public void delete(int notifId) throws SQLException {
        notificationDAO.delete(notifId);
    }

    public void delete(int notifId, int userId) throws SQLException {
        int affected = notificationDAO.delete(notifId, userId);
        if (affected == 0) {
            ensureNotificationOwnershipIfExists(notifId, userId);
        }
    }

    private void ensureNotificationOwnershipIfExists(int notifId, int userId) throws SQLException {
        Integer ownerId = notificationDAO.findUserIdByNotificationId(notifId);
        if (ownerId != null && ownerId != userId) {
            throw new SecurityException("Thông báo không tồn tại hoặc không thuộc quyền của bạn.");
        }
    }
}
