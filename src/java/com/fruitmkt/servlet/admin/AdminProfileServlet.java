package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.HashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/profile")
public class AdminProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        
        // Fetch fresh data
        try {
            User freshAdmin = userDAO.findUserById(admin.getUserId());
            req.setAttribute("adminUser", freshAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");

        try {
            if ("updateInfo".equals(action)) {
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");

                User updatedAdmin = userDAO.findUserById(admin.getUserId());
                updatedAdmin.setFullName(fullName);
                updatedAdmin.setPhone(phone);
                
                userDAO.update(updatedAdmin);
                
                // Update session
                req.getSession().setAttribute(AppConfig.SESSION_USER, updatedAdmin);
                SessionUtil.flashSuccess(req.getSession(), "Đã cập nhật thông tin thành công.");

            } else if ("updatePassword".equals(action)) {
                String oldPass = req.getParameter("oldPassword");
                String newPass = req.getParameter("newPassword");
                String confirmPass = req.getParameter("confirmPassword");

                if (!newPass.equals(confirmPass)) {
                    throw new Exception("Mật khẩu xác nhận không khớp.");
                }

                User currentAdmin = userDAO.findUserById(admin.getUserId());
                if (!HashUtil.verify(oldPass, currentAdmin.getPasswordHash())) {
                    throw new Exception("Mật khẩu cũ không chính xác.");
                }

                String newHash = HashUtil.hashPassword(newPass);
                userDAO.updatePassword(admin.getUserId(), newHash);
                
                SessionUtil.flashSuccess(req.getSession(), "Đã thay đổi mật khẩu thành công.");
            } else {
                throw new Exception("Hành động không hợp lệ: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Lỗi: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/profile");
    }
}
