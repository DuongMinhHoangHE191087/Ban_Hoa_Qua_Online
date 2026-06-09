package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ChatDAO;
import com.fruitmkt.model.entity.Notification;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.NotificationService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NotificationServlet - danh sách thông báo của user và các API AJAX liên quan.
 */
@WebServlet({"/notifications", "/api/notifications/unread", "/api/notifications/recent", "/api/notifications/markAllRead"})
public class NotificationServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(NotificationServlet.class.getName());
    private final NotificationService notificationService = new NotificationService();
    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            String requestType = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestType) || req.getServletPath().contains("/api/")) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"success\":false,\"message\":\"Chưa đăng nhập.\"}");
            } else {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
            }
            return;
        }

        String action = req.getParameter("action");
        String servletPath = req.getServletPath();

        if ("getRecent".equals(action) || servletPath.contains("/api/notifications/recent")) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            Map<String, Object> result = new HashMap<>();
            try {
                List<Notification> list = notificationService.getAllNotifications(currentUser.getUserId());
                int limit = Math.min(5, list.size());
                result.put("success", true);
                result.put("notifications", list.subList(0, limit));
                out.print(JsonUtil.toJson(result));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Lỗi lấy danh sách thông báo gần đây", e);
                result.put("success", false);
                result.put("message", e.getMessage());
                try {
                    out.print(JsonUtil.toJson(result));
                } catch (Exception ignored) {}
            } finally {
                out.flush();
            }
            return;
        }

        if ("getUnreadCounts".equals(action) || servletPath.contains("/api/notifications/unread")) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            Map<String, Object> result = new HashMap<>();
            try {
                int unreadNotifs = notificationService.getUnread(currentUser.getUserId()).size();
                int unreadChats = chatDAO.countTotalUnread(currentUser.getUserId());
                result.put("success", true);
                result.put("unreadNotifications", unreadNotifs);
                result.put("unreadChats", unreadChats);
                out.print(JsonUtil.toJson(result));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Lỗi lấy số lượng chưa đọc", e);
                result.put("success", false);
                result.put("message", e.getMessage());
                try {
                    out.print(JsonUtil.toJson(result));
                } catch (Exception ignored) {}
            } finally {
                out.flush();
            }
            return;
        }

        try {
            List<Notification> notifications = notificationService.getAllNotifications(currentUser.getUserId());
            req.setAttribute("notifications", notifications);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/notification.jsp").forward(req, resp);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi tải danh sách thông báo", e);
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Không thể tải danh sách thông báo.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.contains("/api/") || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));

        try {
            if ("markRead".equals(action) || servletPath.contains("/markRead")) {
                String notifIdStr = req.getParameter("notifId");
                if (notifIdStr == null || notifIdStr.trim().isEmpty()) {
                    notifIdStr = req.getParameter("notificationId");
                }
                if (notifIdStr != null && !notifIdStr.trim().isEmpty()) {
                    int notifId = Integer.parseInt(notifIdStr);
                    notificationService.markRead(notifId);
                }
                if (isApi) {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"success\":true,\"message\":\"Đã đánh dấu đọc.\"}");
                    return;
                }
            } else if ("markAllRead".equals(action) || servletPath.contains("/markAllRead")) {
                notificationService.markAllRead(currentUser.getUserId());
                if (isApi) {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"success\":true,\"message\":\"Đã đánh dấu đọc tất cả.\"}");
                    return;
                }
                SessionUtil.flashSuccess(req.getSession(), "Đã đánh dấu đọc tất cả thông báo.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Lỗi xử lý POST thông báo", e);
            if (isApi) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\":false,\"message\":\"Có lỗi xảy ra: " + e.getMessage() + "\"}");
                return;
            }
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra: " + e.getMessage());
        }

        String redirectUrl = req.getParameter("redirectUrl");
        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + redirectUrl);
        } else {
            resp.sendRedirect(req.getContextPath() + "/notifications");
        }
    }
}
