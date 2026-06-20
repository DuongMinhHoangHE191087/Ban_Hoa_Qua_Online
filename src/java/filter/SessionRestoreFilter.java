package filter;

import dao.auth.UserDAO;
import dao.auth.UserSessionDAO;
import model.entity.auth.User;
import util.LoggerUtil;
import util.SessionUtil;
import util.TokenUtil;
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
    private final UserSessionDAO userSessionDAO = new UserSessionDAO();

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

        // 1. Kiểm tra Access Token trước (Rất nhanh, không cần gọi DB)
        String accessToken = TokenUtil.getCookieValue(req, "accessToken");
        Integer userIdFromAccess = TokenUtil.verifyAccessToken(accessToken);

        if (userIdFromAccess != null) {
            // Access Token còn hiệu lực
            if (!SessionUtil.isLoggedIn(session)) {
                // Khôi phục session nếu bị mất (ví dụ: Tomcat restart)
                try {
                    User user = userDAO.findUserById(userIdFromAccess);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                    }
                } catch (SQLException e) {
                    LoggerUtil.warn(log, "SessionRestoreFilter: Lỗi DB khi khôi phục qua Access Token", e);
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // 2. Access Token hết hạn hoặc không có -> Kiểm tra Refresh Token (Cần gọi DB)
        String refreshToken = TokenUtil.getCookieValue(req, "refreshToken");
        if (refreshToken != null) {
            try {
                Integer userId = userSessionDAO.findUserIdBySessionToken(refreshToken);
                if (userId != null) {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Tái cấp phát Access Token mới
                        String newAccessToken = TokenUtil.generateAccessToken(userId);
                        TokenUtil.addAccessTokenCookie(req, resp, newAccessToken);
                        
                        // Đảm bảo session vẫn tồn tại
                        if (!SessionUtil.isLoggedIn(session)) {
                            SessionUtil.setCurrentUser(req.getSession(true), user);
                        }
                        chain.doFilter(request, response);
                        return;
                    }
                }
                // Nhánh else: Refresh Token không hợp lệ hoặc đã bị Thu hồi (Revoke) từ DB!
                // Phải bắt buộc đăng xuất người dùng (xóa sạch session & cookie)
                TokenUtil.clearTokens(req, resp);
                if (session != null) {
                    session.invalidate();
                }
            } catch (SQLException e) {
                LoggerUtil.warn(log, "SessionRestoreFilter: Lỗi DB khi kiểm tra Refresh Token", e);
            }
        } else {
            // Không có cả Access Token và Refresh Token, nhưng có thể có session cũ rác
            if (SessionUtil.isLoggedIn(session)) {
                TokenUtil.clearTokens(req, resp);
                session.invalidate();
            }
        }

        // Đi tiếp sang các filter tiếp theo (CsrfFilter, AuthFilter...)
        chain.doFilter(request, response);
    }
}
