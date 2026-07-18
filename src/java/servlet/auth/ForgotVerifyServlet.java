package servlet.auth;

import config.AppConfig;
import model.entity.auth.User;
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
 * ForgotVerifyServlet — Bước 2: Xác minh OTP đã gửi đến email.
 *
 * URL: /auth/forgot-verify
 * GET : Hiển thị form nhập OTP (tái dùng verify.jsp)
 * POST action=resend : Gửi lại OTP (silently skip nếu email không tồn tại)
 * POST action=verify : Xác minh OTP → set session cờ → redirect /auth/reset-password
 *
 * Guard: Phải có SESSION_FORGOT_EMAIL trong session, nếu không → redirect /auth/forgot
 */
@WebServlet("/auth/forgot-verify")
public class ForgotVerifyServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String email = session != null ? (String) session.getAttribute(AppConfig.SESSION_FORGOT_EMAIL) : null;

        if (!ValidationUtil.notBlank(email)) {
            SessionUtil.flashError(req.getSession(true), "Vui lòng bắt đầu lại quy trình quên mật khẩu.");
            resp.sendRedirect(req.getContextPath() + "/auth/forgot");
            return;
        }

        req.setAttribute("email", email);
        req.setAttribute("forgotMode", true); // JSP dùng để điều chỉnh text nếu cần
        req.getRequestDispatcher("/WEB-INF/jsp/auth/verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        String email = session != null ? (String) session.getAttribute(AppConfig.SESSION_FORGOT_EMAIL) : null;

        try {
            if (!ValidationUtil.notBlank(email)) {
                throw new Exception("Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại.");
            }

            if ("resend".equalsIgnoreCase(action)) {
                // sendForgotPasswordCode silently skip nếu email không tồn tại — cooldown vẫn được check
                authService.sendForgotPasswordCode(email);
                SessionUtil.flashSuccess(req.getSession(), "Đã gửi lại mã xác minh. Mã mới có hiệu lực trong 5 phút.");
                resp.sendRedirect(req.getContextPath() + "/auth/forgot-verify");
                return;
            }

            // action=verify (default)
            String code = req.getParameter("code");
            User user = authService.verifyForgotCode(email, code);

            // OTP hợp lệ → set cờ cho phép vào trang reset-password
            session.setAttribute(AppConfig.SESSION_FORGOT_VERIFIED, Boolean.TRUE);
            resp.sendRedirect(req.getContextPath() + "/auth/reset-password");

        } catch (Exception e) {
            req.setAttribute("errorMsg", util.ErrorMessageUtil.getUserMessage(e));
            req.setAttribute("email", email);
            req.setAttribute("forgotMode", true);
            req.getRequestDispatcher("/WEB-INF/jsp/auth/verify.jsp").forward(req, resp);
        }
    }
}
