package servlet.admin.system;
import dao.auth.UserDAO;

import config.AppConfig;
import model.entity.auth.User;
import service.system.SystemConfigService;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/admin/config")
public class AdminConfigServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminConfigServlet.class.getName());

    private final SystemConfigService systemConfigService = new SystemConfigService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(request.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            List<Map<String, Object>> configs = systemConfigService.findAll();
            request.setAttribute("configs", configs);
            request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-config.jsp").forward(request, response);
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi khi tải cấu hình hệ thống", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải cấu hình hệ thống.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User admin = SessionUtil.getCurrentUser(session);
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");
        if ("update".equals(action)) {
            String configKey = request.getParameter("configKey");
            String configValue = request.getParameter("configValue");
            String reason = request.getParameter("reason");

            if (reason == null || reason.trim().isEmpty()) {
                reason = "Cập nhật bởi Admin " + admin.getFullName();
            }

            try {
                systemConfigService.updateConfig(configKey, configValue, null, admin.getUserId(), reason);
                SessionUtil.setFlashMessage(session, "Cập nhật cấu hình [" + configKey + "] thành công!", "success");
            } catch (SQLException e) {
                LoggerUtil.error(log, "Lỗi khi cập nhật cấu hình: " + configKey, e);
                SessionUtil.setFlashMessage(session, "Lỗi khi cập nhật cấu hình: " + e.getMessage(), "danger");
            } catch (IllegalArgumentException e) {
                SessionUtil.setFlashMessage(session, e.getMessage(), "danger");
            }
        } else if ("clearAllSessions".equals(action)) {
            try {
                dao.auth.UserDAO udao = new dao.auth.UserDAO();
                udao.deleteAllSessions();
                SessionUtil.flashSuccess(session, "Đã xóa toàn bộ phiên đăng nhập của người dùng. Họ sẽ phải đăng nhập lại khi phiên hiện tại hết hạn.");
            } catch (SQLException e) {
                LoggerUtil.error(log, "Lỗi khi xóa phiên đăng nhập", e);
                SessionUtil.flashError(session, "Lỗi khi xóa phiên đăng nhập: " + e.getMessage());
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/config");
    }
}
