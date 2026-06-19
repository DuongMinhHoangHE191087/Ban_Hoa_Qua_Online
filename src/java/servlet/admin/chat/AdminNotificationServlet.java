package servlet.admin.chat;

import service.chat.NotificationService;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/notifications")
public class AdminNotificationServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminNotificationServlet.class.getName());

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            java.util.List<model.entity.chat.Notification> notifications = notificationService.getAllSystemNotifications();
            req.setAttribute("notificationList", notifications);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-notifications.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminNotificationServlet.class.getName()),
                    "AdminNotificationServlet#doGet",
                    "Lỗi tải trang thông báo",
                    e);
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
            LoggerUtil.error(log, "Lỗi gửi thông báo", e);
            SessionUtil.flashError(req.getSession(), "Lỗi gửi thông báo: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/notifications");
    }
}
