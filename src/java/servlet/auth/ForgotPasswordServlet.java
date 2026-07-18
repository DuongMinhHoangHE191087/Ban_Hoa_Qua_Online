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
 * ForgotPasswordServlet — Bước 1: User nhập email để nhận OTP đặt lại mật khẩu.
 *
 * URL: /auth/forgot
 * GET : Hiển thị form nhập email
 * POST: Gửi OTP về email (nếu email tồn tại); luôn redirect sang trang verify
 *       dù email có tồn tại hay không (anti-enumeration)
 *
 * Luồng tiếp theo: → /auth/forgot-verify → /auth/reset-password
 */
@WebServlet("/auth/forgot")
public class ForgotPasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Nếu đã đăng nhập thì chuyển về trang chủ
        HttpSession session = req.getSession(false);
        if (SessionUtil.isLoggedIn(session)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/auth/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // CSRF check — ngăn kẻ tấn công trigger gửi email reset cho nạn nhân
        HttpSession csrfSession = req.getSession(false);
        String sessionToken = (csrfSession != null)
                ? (String) csrfSession.getAttribute(AppConfig.SESSION_CSRF_TOKEN) : null;
        String requestToken = req.getParameter("_csrf");
        if (sessionToken == null || !sessionToken.equals(requestToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ");
            return;
        }

        String email = req.getParameter("email");

        try {
            if (!ValidationUtil.notBlank(email) || !ValidationUtil.isValidEmail(email)) {
                throw new Exception("Địa chỉ email không hợp lệ.");
            }

            // sendForgotPasswordCode trả false nếu email không tồn tại — vẫn tiếp tục (anti-enumeration)
            authService.sendForgotPasswordCode(email.trim().toLowerCase());

            // Lưu email vào session dùng cho bước verify và reset
            HttpSession session = req.getSession(true);
            session.setAttribute(AppConfig.SESSION_FORGOT_EMAIL, email.trim().toLowerCase());
            // Xóa verified flag cũ nếu có
            session.removeAttribute(AppConfig.SESSION_FORGOT_VERIFIED);

            SessionUtil.flashSuccess(session,
                    "Nếu email của bạn đã đăng ký, mã xác minh đã được gửi. Vui lòng kiểm tra hộp thư.");
            resp.sendRedirect(req.getContextPath() + "/auth/forgot-verify");

        } catch (Exception e) {
            req.setAttribute("errorMsg", util.ErrorMessageUtil.getUserMessage(e));
            req.getRequestDispatcher("/WEB-INF/jsp/auth/forgot-password.jsp").forward(req, resp);
        }
    }
}
