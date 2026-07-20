package filter;

import dao.auth.UserDAO;
import dao.auth.UserSessionDAO;
import model.entity.auth.User;
import util.SessionUtil;
import util.TokenUtil;
import util.LoggerUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * AuthFilter — Chặn truy cập các URL yêu cầu đăng nhập.
 *
 * CÁC URL BẢO VỆ: /customer/*, /shop/*, /delivery/*, /admin/*, và /api/* qua web.xml
 * Nếu chưa login → Tự động thẩm định qua Access Token & Refresh Token trước khi redirect.
 *
 * THỨ TỰ CHẠY: 4
 * @author fruitmkt-team
 */
public class AuthFilter implements Filter {

    private static final Logger log = Logger.getLogger(AuthFilter.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private final UserSessionDAO userSessionDAO = new UserSessionDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();

        if (isPublicApiRequest(requestUri, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        // Trường hợp đã có session hợp lệ
        if (SessionUtil.isLoggedIn(session)) {
            chain.doFilter(request, response);
            return;
        }

        // Trường hợp chưa có session hợp lệ -> Kiểm tra Access Token trong Cookie
        String accessToken = TokenUtil.getCookieValue(req, "accessToken");
        if (accessToken != null) {
            Integer userId = TokenUtil.verifyAccessToken(accessToken);
            if (userId != null) {
                try {
                    User user = userDAO.findUserById(userId);
                    if (user != null && "ACTIVE".equals(user.getStatus())) {
                        // Tự động khôi phục session đăng nhập
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                        chain.doFilter(request, response);
                        return;
                    }
                } catch (SQLException e) {
                    LoggerUtil.warn(log, "Lỗi cơ sở dữ liệu khi khôi phục session qua Access Token", e);
                }
            }
        }

        // Access Token bị hết hạn hoặc không có -> Kiểm tra Refresh Token trong Cookie
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
                        
                        // Tự động khôi phục session đăng nhập
                        SessionUtil.setCurrentUser(req.getSession(true), user);
                        chain.doFilter(request, response);
                        return;
                    }
                } else {
                    // Refresh token không hợp lệ hoặc đã hết hạn trong database -> Xóa cookie
                    TokenUtil.clearTokens(req, resp);
                }
            } catch (SQLException e) {
                LoggerUtil.warn(log, "Lỗi cơ sở dữ liệu khi khôi phục session qua Refresh Token", e);
            }
        }

        // Không thể xác thực tự động -> Kiểm tra nếu là AJAX/JSON thì trả về JSON lỗi thay vì redirect
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || "json".equals(req.getParameter("format"))
                || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (isAjax || isApiRequest(requestUri, contextPath)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"error\":\"Phiên đăng nhập hết hạn hoặc chưa đăng nhập.\"}");
        } else {
            if (requestUri.startsWith(contextPath + "/checkout")) {
                SessionUtil.flashError(req.getSession(true), "Bạn cần đăng nhập để tiếp tục thanh toán. Hệ thống sẽ đưa bạn trở lại checkout sau khi đăng nhập.");
            }
            String redirectUrl = requestUri;
            if (req.getQueryString() != null) {
                redirectUrl += "?" + req.getQueryString();
            }
            String encodedRedirect = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/auth/login?redirect=" + encodedRedirect);
        }
    }

    private boolean isApiRequest(String requestUri, String contextPath) {
        return requestUri != null && requestUri.startsWith(contextPath + "/api/");
    }

    private boolean isPublicApiRequest(String requestUri, String contextPath) {
        if (requestUri == null) {
            return false;
        }
        return requestUri.equals(contextPath + "/api/coupon/validate")
                || requestUri.equals(contextPath + "/api/ai/search")
                || requestUri.equals(contextPath + "/api/payment/webhook");
    }
}
