package com.fruitmkt.servlet.api;

import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Notification;
import com.fruitmkt.service.NotificationService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.config.AppConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationAPIServlet — Endpoint API cho thông báo.
 * URL: /api/notifications
 */
@WebServlet("/api/notifications")
public class NotificationAPIServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);
        
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Người dùng chưa đăng nhập."));
            return;
        }

        try {
            int limit = 5;
            String limitStr = req.getParameter("limit");
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr);
                } catch (NumberFormatException ignored) {}
            }

            List<Notification> unreadNotifs = notificationService.getUnread(user.getUserId());
            int unreadCount = unreadNotifs.size();

            List<Notification> allNotifs = notificationService.getAllNotifications(user.getUserId());
            if (allNotifs.size() > limit) {
                allNotifs = allNotifs.subList(0, limit);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("unreadCount", unreadCount);
            responseData.put("notifications", allNotifs);

            JsonUtil.writeJson(resp, responseData);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Người dùng chưa đăng nhập."));
            return;
        }

        // Xác thực CSRF Token
        String sessionToken = session != null ? (String) session.getAttribute(AppConfig.SESSION_CSRF_TOKEN) : null;
        String requestToken = req.getHeader("X-CSRF-Token");
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = req.getParameter("_csrf");
        }
        if (sessionToken == null || !sessionToken.equals(requestToken)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "CSRF token không hợp lệ."));
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("markRead".equals(action)) {
                String notifIdStr = req.getParameter("notificationId");
                if (notifIdStr == null || notifIdStr.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, Map.of("success", false, "error", "Thiếu ID thông báo."));
                    return;
                }
                int notifId = Integer.parseInt(notifIdStr);
                notificationService.markRead(notifId);
                JsonUtil.writeJson(resp, Map.of("success", true));
            } else if ("markAllRead".equals(action)) {
                notificationService.markAllRead(user.getUserId());
                JsonUtil.writeJson(resp, Map.of("success", true));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, Map.of("success", false, "error", "Hành động không hợp lệ."));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, Map.of("success", false, "error", "Lỗi máy chủ: " + e.getMessage()));
        }
    }
}
