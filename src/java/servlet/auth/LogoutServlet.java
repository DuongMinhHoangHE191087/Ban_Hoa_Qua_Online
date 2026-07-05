package servlet.auth;

import service.auth.AuthService;
import util.SessionUtil;
import util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * LogoutServlet — Controller cho chức năng: Xóa session và redirect về home
 *
 * URL: /auth/logout
 * GET : Xóa session, xóa Refresh Token khỏi DB, xóa cookies và redirect về /auth/login
 *
 * @author fruitmkt-team
 */
@WebServlet(urlPatterns = {"/auth/logout", "/logout"})
public class LogoutServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // 1. Lấy và xóa Refresh Token khỏi Database để vô hiệu hóa hoàn toàn phiên hoạt động này
        String refreshToken = TokenUtil.getCookieValue(req, "refreshToken");
        if (refreshToken != null) {
            try {
                authService.deleteUserSession(refreshToken);
            } catch (SQLException e) {
                req.getServletContext().log("Lỗi cơ sở dữ liệu khi xóa session token trong LogoutServlet: " + util.ErrorMessageUtil.getSafeLogMessage(e), e);
            }
        }
        
        // 2. Xóa sạch Cookie Access Token & Refresh Token trên trình duyệt
        TokenUtil.clearTokens(req, resp);
        
        // 3. Hủy bỏ session hiện tại (nếu có)
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 4. Chuyển hướng về trang Đăng nhập với tham số logout=success
        resp.sendRedirect(req.getContextPath() + "/auth/login?logout=success");
    }

}
