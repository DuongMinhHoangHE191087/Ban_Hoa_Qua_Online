package com.fruitmkt.servlet.admin;

import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/config")
public class AdminConfigServlet extends HttpServlet {

    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Map<String, Object>> configs = systemConfigDAO.findAll();
            request.setAttribute("configs", configs);
            request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-config.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải cấu hình hệ thống.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
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

            try (Connection conn = systemConfigDAO.openConnection()) {
                conn.setAutoCommit(false);
                try {
                    systemConfigDAO.updateConfigWithHistory(conn, configKey, configValue, LocalDateTime.now(), admin.getUserId(), reason);
                    conn.commit();
                    SessionUtil.setFlashMessage(session, "Cập nhật cấu hình [" + configKey + "] thành công!", "success");
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                SessionUtil.setFlashMessage(session, "Lỗi khi cập nhật cấu hình: " + e.getMessage(), "danger");
            }
        } else if ("clearAllSessions".equals(action)) {
            try {
                com.fruitmkt.dao.UserDAO udao = new com.fruitmkt.dao.UserDAO();
                udao.deleteAllSessions();
                SessionUtil.flashSuccess(session, "Đã xóa toàn bộ phiên đăng nhập của người dùng. Họ sẽ phải đăng nhập lại khi phiên hiện tại hết hạn.");
            } catch (SQLException e) {
                e.printStackTrace();
                SessionUtil.flashError(session, "Lỗi khi xóa phiên đăng nhập: " + e.getMessage());
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/config");
    }
}
