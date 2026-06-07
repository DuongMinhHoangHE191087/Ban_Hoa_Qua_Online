package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.NotificationService;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Notification;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * NotificationServlet — Controller cho chức năng: Danh sách thông báo của user
 *
 * URL: /notifications
 * GET : Danh sách thông báo của user
 * POST: Đánh dấu đã đọc / đã đọc tất cả
 *
 * @author fruitmkt-team
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        try {
            List<Notification> notifications = notificationService.getAllNotifications(user.getUserId());
            req.setAttribute("notifications", notifications);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/notification.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải trang thông báo");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("markAllRead".equals(action)) {
                notificationService.markAllRead(user.getUserId());
                SessionUtil.flashSuccess(req.getSession(), "Đã đánh dấu tất cả thông báo là đã đọc.");
            } else if ("markRead".equals(action)) {
                String notifIdStr = req.getParameter("notificationId");
                if (notifIdStr != null && !notifIdStr.trim().isEmpty()) {
                    int notifId = Integer.parseInt(notifIdStr);
                    notificationService.markRead(notifId);
                }
                String redirectUrl = req.getParameter("redirectUrl");
                if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + redirectUrl);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Lỗi xử lý: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/notifications");
    }
}
