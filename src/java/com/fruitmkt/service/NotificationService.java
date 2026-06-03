package com.fruitmkt.service;

import com.fruitmkt.dao.NotificationDAO;
import com.fruitmkt.model.entity.Notification;
import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();

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
