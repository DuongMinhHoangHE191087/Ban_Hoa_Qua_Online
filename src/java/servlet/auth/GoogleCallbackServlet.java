package servlet.auth;

import config.AppConfig;
import model.entity.auth.User;
import service.auth.AuthService;
import util.JsonUtil;
import util.ServletUtil;
import util.SessionUtil;
import util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * GoogleCallbackServlet — Xử lý kết quả trả về từ Google OAuth.
 *
 * URL: /GoogleCallback (khớp GOOGLE_REDIRECT_URI)
 * GET: Trao đổi code lấy Token, lấy UserInfo, đồng bộ DB và tạo phiên đăng nhập.
 *
 * @author fruitmkt-team
 */
@WebServlet("/GoogleCallback")
public class GoogleCallbackServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String code = req.getParameter("code");
        String error = req.getParameter("error");
        String returnedState = req.getParameter("state");

        // 1. Kiểm tra lỗi nếu người dùng từ chối cấp quyền
        if (error != null || code == null || code.trim().isEmpty()) {
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Đăng nhập bằng Google đã bị hủy hoặc thất bại.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // 1b. Xác minh OAuth state để chống login-CSRF
        HttpSession preSession = req.getSession(false);
        String expectedState = (preSession != null)
                ? (String) preSession.getAttribute(GoogleLoginServlet.SESSION_OAUTH_STATE)
                : null;
        // Xóa state khỏi session ngay sau khi đọc (dùng một lần)
        if (preSession != null) {
            preSession.removeAttribute(GoogleLoginServlet.SESSION_OAUTH_STATE);
        }
        if (expectedState == null || !expectedState.equals(returnedState)) {
            HttpSession s = req.getSession(true);
            s.setAttribute(AppConfig.SESSION_FLASH_MSG, "Yêu cầu đăng nhập không hợp lệ (state mismatch). Vui lòng thử lại.");
            s.setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // 2. Giao dịch mã authorization_code lấy access_token
            Map<String, Object> tokenMap = exchangeCodeForToken(req, code);
            String accessToken = (String) tokenMap.get("access_token");
            if (accessToken == null) {
                throw new Exception("Không tìm thấy access_token trong phản hồi của Google.");
            }

            // 3. Sử dụng access_token để lấy thông tin hồ sơ của người dùng
            Map<String, Object> profileMap = fetchGoogleUserProfile(accessToken);
            String email = (String) profileMap.get("email");
            String fullName = (String) profileMap.get("name");
            String pictureUrl = (String) profileMap.get("picture");

            if (email == null || email.trim().isEmpty()) {
                throw new Exception("Không thể lấy địa chỉ Email từ tài khoản Google của bạn.");
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = "Google User";
            }

            // 4. Xử lý logic đăng nhập hoặc tự động đăng ký mới ở DB thông qua AuthService
            User user = authService.processGoogleLogin(email, fullName, pictureUrl);

            // 5. Chống tấn công Session Fixation: Hủy session cũ, tạo session mới sạch sẽ
            req.getSession().invalidate();
            HttpSession newSession = req.getSession(true);
            SessionUtil.setCurrentUser(newSession, user);

            // 6. Thiết lập bộ đôi Access Token (15 phút) và Refresh Token (7 ngày) dạng Cookie HttpOnly
            String jwtAccessToken = TokenUtil.generateAccessToken(user.getUserId());
            TokenUtil.addAccessTokenCookie(req, resp, jwtAccessToken);

            String refreshToken = TokenUtil.generateRefreshToken();
            java.sql.Timestamp expiresAt = new java.sql.Timestamp(
                    System.currentTimeMillis() + (long) TokenUtil.REFRESH_TOKEN_EXPIRY_SECS * 1000
            );

            // Lưu trữ Refresh Token vào Database để quản lý phiên
            authService.saveUserSession(user.getUserId(), refreshToken, expiresAt);
            TokenUtil.addRefreshTokenCookie(req, resp, refreshToken);

            // 7. Lưu flash message thành công và redirect
            SessionUtil.flashSuccess(newSession, "Đăng nhập bằng tài khoản Google thành công! Chào mừng, " + user.getFullName() + ".");
            redirectToRoleDashboard(req, resp, user);

        } catch (Exception e) {
            req.getServletContext().log("Lỗi tích hợp Google OAuth: " + e.getMessage(), e);
            HttpSession session = req.getSession(true);
            // UC-21: nếu đây là lỗi chặn đặc quyền, hiển thị thông báo đó; còn lại dùng thông báo chung
            String userMsg = (e.getMessage() != null && e.getMessage().startsWith("Vui lòng đăng nhập bằng mật khẩu"))
                    ? e.getMessage()
                    : "Đăng nhập bằng Google thất bại. Vui lòng thử lại.";
            SessionUtil.flashError(session, userMsg);
            resp.sendRedirect(req.getContextPath() + "/auth/login");
        }
    }

    /**
     * Trao đổi authorization_code lấy access_token bằng HttpClient (Java 25 Native)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeCodeForToken(HttpServletRequest req, String code) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String formBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(ServletUtil.getGoogleRedirectUri(req), StandardCharsets.UTF_8)
                + "&grant_type=" + URLEncoder.encode(AppConfig.GOOGLE_GRANT_TYPE, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.GOOGLE_LINK_GET_TOKEN))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Mã phản hồi trao đổi token không hợp lệ: " + response.statusCode() + " - " + response.body());
        }

        return JsonUtil.fromJson(response.body(), Map.class);
    }

    /**
     * Lấy thông tin người dùng từ UserInfo Endpoint của Google
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchGoogleUserProfile(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.GOOGLE_LINK_GET_USER_INFO))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Mã phản hồi lấy user info không hợp lệ: " + response.statusCode() + " - " + response.body());
        }

        return JsonUtil.fromJson(response.body(), Map.class);
    }

    /**
     * Điều hướng thông minh theo Role của người dùng
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
