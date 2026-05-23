package com.fruitmkt.servlet.auth;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ChangePasswordServlet — Đổi mật khẩu cho user đã đăng nhập.
 *
 * URL: /auth/change-password
 * GET : Kiểm tra đăng nhập → hiển thị form đổi mật khẩu
 * POST: Validate → authService.changePassword() → flash success → redirect (PRG)
 *
 * Guard: Phải đang đăng nhập. Tài khoản Google OAuth sẽ báo lỗi ở tầng Service.
 */
@WebServlet("/auth/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionUtil.isLoggedIn(session)) {
            SessionUtil.flashError(req.getSession(true), "Vui lòng đăng nhập để thực hiện chức năng này.");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/change-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (!SessionUtil.isLoggedIn(session)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User currentUser = SessionUtil.getCurrentUser(session);
        String currentPassword = req.getParameter("currentPassword");
        String newPassword     = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            if (!ValidationUtil.notBlank(newPassword)) {
                throw new Exception("Mật khẩu mới không được để trống.");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new Exception("Mật khẩu xác nhận không khớp.");
            }

            authService.changePassword(currentUser.getUserId(), currentPassword, newPassword);

            SessionUtil.flashSuccess(session, "Đổi mật khẩu thành công.");
            resp.sendRedirect(req.getContextPath() + "/auth/change-password");

        } catch (Exception e) {
            req.setAttribute("errorMsg", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/change-password.jsp").forward(req, resp);
        }
    }
}
