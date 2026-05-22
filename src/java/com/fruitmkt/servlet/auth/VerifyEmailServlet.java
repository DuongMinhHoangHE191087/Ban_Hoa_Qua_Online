package com.fruitmkt.servlet.auth;

import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/auth/verify")
public class VerifyEmailServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String email = session != null ? (String) session.getAttribute(AppConfig.SESSION_VERIFY_EMAIL) : null;
        if (email == null || email.trim().isEmpty()) {
            SessionUtil.flashError(req.getSession(true), "Vui lòng đăng ký hoặc đăng nhập để xác minh email.");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        req.setAttribute("email", email);
        req.getRequestDispatcher("/WEB-INF/jsp/auth/verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        String email = session != null ? (String) session.getAttribute(AppConfig.SESSION_VERIFY_EMAIL) : null;

        try {
            if (!ValidationUtil.notBlank(email)) {
                throw new Exception("Vui lòng đăng nhập lại để xác minh email.");
            }

            if ("resend".equalsIgnoreCase(action)) {
                User user = authService.resendVerificationCode(email);
                SessionUtil.flashSuccess(req.getSession(), "Đã gửi lại mã xác minh. Mã mới có hiệu lực trong 5 phút.");
                req.getSession().setAttribute(AppConfig.SESSION_VERIFY_EMAIL, user.getEmail());
                resp.sendRedirect(req.getContextPath() + "/auth/verify");
                return;
            }

            String code = req.getParameter("code");
            User activatedUser = authService.verifyEmailCode(email, code);
            SessionUtil.flashSuccess(req.getSession(), "Xác minh email thành công. Tài khoản đã được kích hoạt.");
            if (session != null) {
                session.removeAttribute(AppConfig.SESSION_VERIFY_EMAIL);
            }
            resp.sendRedirect(req.getContextPath() + "/auth/login");

        } catch (Throwable e) {
            req.setAttribute("errorMsg", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/jsp/auth/verify.jsp").forward(req, resp);
        }
    }
}