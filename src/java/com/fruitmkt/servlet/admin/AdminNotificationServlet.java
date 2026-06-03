package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.NotificationService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/notifications")
public class AdminNotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            java.util.List<com.fruitmkt.model.entity.Notification> notifications = notificationService.getAllSystemNotifications();
            req.setAttribute("notificationList", notifications);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-notifications.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải trang thông báo");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String title = req.getParameter("title");
            String message = req.getParameter("message");
            String target = req.getParameter("target");
            
            if (title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
                SessionUtil.flashError(req.getSession(), "Vui lòng nhập đầy đủ Tiêu đề và Nội dung.");
            } else {
                notificationService.sendBroadcast(title, message, target);
                SessionUtil.flashSuccess(req.getSession(), "Đã gửi thông báo thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Lỗi gửi thông báo: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/notifications");
    }
}
