package servlet.auth;

import config.AppConfig;
import service.auth.AuthService;
import util.SessionUtil;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ResetPasswordServlet — Bước 3: Đặt lại mật khẩu sau khi đã xác minh OTP.
 *
 * URL: /auth/reset-password
 * GET : Kiểm tra session guards → hiển thị form đặt lại mật khẩu
 * POST: Validate → gọi authService.resetPassword() → xóa session forgot → redirect /auth/login
 *
 * Guard kép:
 *   1. SESSION_FORGOT_EMAIL phải có
 *   2. SESSION_FORGOT_VERIFIED phải là Boolean.TRUE
 *   → Nếu thiếu một trong hai → redirect /auth/forgot
 */
@WebServlet("/auth/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAuthorized(req.getSession(false))) {
            SessionUtil.flashError(req.getSession(true), "Vui lòng xác minh email trước khi đặt lại mật khẩu.");
            resp.sendRedirect(req.getContextPath() + "/auth/forgot");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (!isAuthorized(session)) {
            SessionUtil.flashError(req.getSession(true), "Phiên làm việc không hợp lệ. Vui lòng bắt đầu lại.");
            resp.sendRedirect(req.getContextPath() + "/auth/forgot");
            return;
        }

        String email = (String) session.getAttribute(AppConfig.SESSION_FORGOT_EMAIL);
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            if (!ValidationUtil.notBlank(newPassword)) {
                throw new Exception("Mật khẩu mới không được để trống.");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new Exception("Mật khẩu xác nhận không khớp.");
            }

            authService.resetPassword(email, newPassword);

            // Xóa toàn bộ session forgot sau khi reset thành công
            session.removeAttribute(AppConfig.SESSION_FORGOT_EMAIL);
            session.removeAttribute(AppConfig.SESSION_FORGOT_VERIFIED);

            SessionUtil.flashSuccess(req.getSession(), "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            resp.sendRedirect(req.getContextPath() + "/auth/login");

        } catch (Exception e) {
            req.setAttribute("errorMsg", util.ErrorMessageUtil.getUserMessage(e));
            req.getRequestDispatcher("/WEB-INF/jsp/auth/reset-password.jsp").forward(req, resp);
        }
    }

    /** Kiểm tra đủ cả 2 session guards trước khi cho phép vào trang reset */
    private boolean isAuthorized(HttpSession session) {
        if (session == null) return false;
        String email = (String) session.getAttribute(AppConfig.SESSION_FORGOT_EMAIL);
        Boolean verified = (Boolean) session.getAttribute(AppConfig.SESSION_FORGOT_VERIFIED);
        return ValidationUtil.notBlank(email) && Boolean.TRUE.equals(verified);
    }
}
