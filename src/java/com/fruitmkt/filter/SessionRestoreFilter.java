package com.fruitmkt.filter;

import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.TokenUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * SessionRestoreFilter — Tự động phục hồi phiên đăng nhập từ Cookie Access/Refresh Token cho tất cả các trang.
 * Giúp hiển thị thông tin chào mừng, giỏ hàng của thành viên tại trang chủ (/home, /)
 * ngay cả khi họ khởi động lại trình duyệt mà không cần điều hướng bắt buộc.
 *
 * THỨ TỰ CHẠY: 2 (sau EncodingFilter và trước CsrfFilter / AuthFilter)
 * @author fruitmkt-team
 */
public class SessionRestoreFilter implements Filter {

    private static final Logger log = Logger.getLogger(SessionRestoreFilter.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String uri = req.getRequestURI();
        // Bỏ qua tài nguyên tĩnh để tối ưu hóa hiệu năng truy vấn DB
        if (uri.contains("/assets/") || uri.contains("/uploads/") 
                || uri.endsWith(".css") || uri.endsWith(".js") 
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") 
                || uri.endsWith(".gif") || uri.endsWith(".svg") || uri.endsWith(".ico") 
                || uri.endsWith(".webp") || uri.endsWith(".woff") || uri.endsWith(".woff2")) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Nếu đã có session đăng nhập hợp lệ -> Cho đi tiếp luôn
        if (SessionUtil.isLoggedIn(session)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Chưa có session -> Thử phục hồi bằng Access Token Cookie
        String accessToken = TokenUtil.getCookieValue(req, "accessToken");
        if (accessToken != null) {
            Integer userId = TokenUtil.verifyAccessToken(accessToken);
            if (userId != null) {
                try {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Khôi phục session và tiếp tục
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (SQLException e) {
                    LoggerUtil.warn(log, "SessionRestoreFilter: Lỗi khôi phục session bằng Access Token", e);
                }
            }
        }

        // 3. Access Token hết hạn/không có -> Thử phục hồi bằng Refresh Token Cookie
        String refreshToken = TokenUtil.getCookieValue(req, "refreshToken");
        if (refreshToken != null) {
            try {
                Integer userId = userDAO.findUserIdBySessionToken(refreshToken);
                if (userId != null) {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Tái cấp phát Access Token Cookie mới
                        String newAccessToken = TokenUtil.generateAccessToken(userId);
                        TokenUtil.addAccessTokenCookie(req, resp, newAccessToken);
                        
                        // Khôi phục session đăng nhập
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                    }
                } else {
                    // Token trong DB đã hết hạn hoặc không hợp lệ -> Xóa cookies khỏi client
                    TokenUtil.clearTokens(req, resp);
                }
            } catch (SQLException e) {
                LoggerUtil.warn(log, "SessionRestoreFilter: Lỗi khôi phục session bằng Refresh Token", e);
            }
        }

        // Đi tiếp sang các filter tiếp theo (CsrfFilter, AuthFilter...)
        chain.doFilter(request, response);
    }
}
