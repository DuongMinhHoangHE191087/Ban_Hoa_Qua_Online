package com.fruitmkt.servlet.auth;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.service.AuthService.VerificationRequiredException;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * LoginServlet — Controller cho chức năng: Hiển thị form đăng nhập và xử lý xác thực
 *
 * URL: /auth/login
 * GET : Hiển thị form đăng nhập (tự động điều hướng nếu đã đăng nhập)
 * POST: Xác thực thông tin, cấp phát bộ đôi cookie HttpOnly Access & Refresh Token, và chuyển hướng theo role.
 *
 * @author fruitmkt-team
 */
@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Nếu người dùng đã đăng nhập từ trước, tự động chuyển hướng về trang tương ứng theo phân quyền
        HttpSession session = req.getSession(false);
        if (SessionUtil.isLoggedIn(session)) {
            User user = SessionUtil.getCurrentUser(session);
            redirectToRoleDashboard(req, resp, user);
            return;
        }
        
        // Forward trực tiếp đến trang JSP đăng nhập an toàn
        req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String identifier = req.getParameter("identifier");
        String password = req.getParameter("password");
        String redirectTarget = req.getParameter("redirect");
        
        // 1. Kiểm tra CSRF token thủ công tại Servlet để tăng tính bảo mật
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf != null && !sessionCsrf.equals(reqCsrf)) {
            req.setAttribute("errorMsg", "CSRF token không hợp lệ hoặc phiên làm việc đã hết hạn.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
            return;
        }

        try {
            // 2. Xác thực credentials từ AuthService
            User user = authService.login(identifier, password);
            
            // 3. Chống tấn công Session Fixation bằng cách hủy session cũ và tạo session mới
            req.getSession().invalidate();
            HttpSession newSession = req.getSession(true);
            SessionUtil.setCurrentUser(newSession, user);
            
            // 4. Tạo bộ đôi Access Token (15 phút) & Refresh Token (7 ngày)
            String accessToken = TokenUtil.generateAccessToken(user.getUserId());
            TokenUtil.addAccessTokenCookie(req, resp, accessToken);
            
            String refreshToken = TokenUtil.generateRefreshToken();
            java.sql.Timestamp expiresAt = new java.sql.Timestamp(System.currentTimeMillis() + (long) TokenUtil.REFRESH_TOKEN_EXPIRY_SECS * 1000);
            
            // Lưu Refresh Token vào Database thông qua AuthService
            authService.saveUserSession(user.getUserId(), refreshToken, expiresAt);
            TokenUtil.addRefreshTokenCookie(req, resp, refreshToken);
            
            // 5. Thiết lập flash message chào mừng thành công
            SessionUtil.flashSuccess(newSession, "Chào mừng quay trở lại, " + user.getFullName() + "!");
            
            // 6. Xử lý chuyển hướng (Redirect)
            // Ngăn chặn lỗ hổng Open Redirect bằng cách kiểm tra target path
            if (redirectTarget != null && !redirectTarget.trim().isEmpty() && (redirectTarget.startsWith("/") || redirectTarget.startsWith(req.getContextPath()))) {
                resp.sendRedirect(redirectTarget);
            } else {
                redirectToRoleDashboard(req, resp, user);
            }
            
        } catch (VerificationRequiredException e) {
            HttpSession session = req.getSession(true);
            SessionUtil.flashError(session, "Tài khoản chưa được xác minh. Vui lòng nhập mã code để kích hoạt tài khoản.");
            session.setAttribute(AppConfig.SESSION_VERIFY_EMAIL, e.getEmail());
            resp.sendRedirect(req.getContextPath() + "/auth/verify");

        } catch (Exception e) {
            // Đăng nhập thất bại -> Hiển thị lỗi thân thiện
            req.setAttribute("errorMsg", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(req, resp);
        }
    }
    
    /**
     * Phương thức hỗ trợ điều hướng người dùng về đúng trang quản trị theo vai trò (Role-based redirection)
     */
    private void redirectToRoleDashboard(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String role = user.getRole();
        if (AppConfig.ROLE_ADMIN.equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (AppConfig.ROLE_SHOP_OWNER.equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        } else if (AppConfig.ROLE_DELIVERY.equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/delivery/");
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
