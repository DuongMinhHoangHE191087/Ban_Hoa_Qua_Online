package com.fruitmkt.servlet.api;

import com.fruitmkt.dto.ApiResponse;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Notification;
import com.fruitmkt.service.NotificationService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.config.AppConfig;

import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * NotificationAPIServlet — Endpoint API cho thông báo.
 * URL: /api/notifications
 */
@WebServlet("/api/notifications")
public class NotificationAPIServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(NotificationAPIServlet.class.getName());

    public record NotificationListResponse(int unreadCount, List<Notification> notifications) {}

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(401, "Người dùng chưa đăng nhập."));
            return;
        }

        try {
            int limit = 5;
            String limitStr = req.getParameter("limit");
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr);
                } catch (NumberFormatException e) {
                    LoggerUtil.warn(log, "Tham số limit không hợp lệ: " + limitStr, e);
                }
            }

            List<Notification> unreadNotifs = notificationService.getUnread(user.getUserId());
            int unreadCount = unreadNotifs.size();

            List<Notification> allNotifs = notificationService.getAllNotifications(user.getUserId());
            if (allNotifs.size() > limit) {
                allNotifs = allNotifs.subList(0, limit);
            }

            JsonUtil.writeJson(resp, ApiResponse.ok(
                    new NotificationListResponse(unreadCount, List.copyOf(allNotifs))));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LoggerUtil.error(log, "Error fetching notifications", e);
            JsonUtil.writeJson(resp, ApiResponse.fail("Lỗi máy chủ. Vui lòng thử lại."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(401, "Người dùng chưa đăng nhập."));
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
            JsonUtil.writeJson(resp, ApiResponse.fail(403, "CSRF token không hợp lệ."));
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("markRead".equals(action)) {
                String notifIdStr = req.getParameter("notificationId");
                if (notifIdStr == null || notifIdStr.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, ApiResponse.fail(400, "Thiếu ID thông báo."));
                    return;
                }
                int notifId = Integer.parseInt(notifIdStr);
                notificationService.markRead(notifId);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));
            } else if ("markAllRead".equals(action)) {
                notificationService.markAllRead(user.getUserId());
                JsonUtil.writeJson(resp, ApiResponse.ok(null));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(400, "Hành động không hợp lệ."));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LoggerUtil.error(log, "Error processing notification action", e);
            JsonUtil.writeJson(resp, ApiResponse.fail("Lỗi máy chủ. Vui lòng thử lại."));
        }
    }
}
