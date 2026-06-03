package com.fruitmkt.service;

import com.fruitmkt.dao.NotificationDAO;
import com.fruitmkt.model.entity.Notification;
import java.sql.SQLException;
import java.util.List;

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

    public void markRead(int notifId) throws SQLException {
        notificationDAO.markRead(notifId);
    }

    public void markAllRead(int userId) throws SQLException {
        notificationDAO.markAllRead(userId);
    }
    public List<Notification> getAllSystemNotifications() throws SQLException {
        return notificationDAO.findAllSystemNotifications();
    }

    public void sendBroadcast(String title, String message, String target) throws SQLException {
        if ("ALL".equalsIgnoreCase(target)) {
            notificationDAO.insertForAll(title, message);
        } else {
            notificationDAO.insertForRole(title, message, target);
        }
    }
}
