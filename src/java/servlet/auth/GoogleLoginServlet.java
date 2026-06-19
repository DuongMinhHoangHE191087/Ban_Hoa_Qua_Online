package servlet.auth;

import config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * GoogleLoginServlet — Điều hướng người dùng sang màn hình xin cấp quyền (OAuth Consent) của Google.
 *
 * URL: /auth/google-login
 * GET: Tạo URL xin quyền và redirect.
 *
 * @author fruitmkt-team
 */
@WebServlet("/auth/google-login")
public class GoogleLoginServlet extends HttpServlet {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /** Session attribute key for the OAuth state token (anti-CSRF / login-CSRF). */
    static final String SESSION_OAUTH_STATE = "_oauthState";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Tạo state token ngẫu nhiên để chống login-CSRF (OAuth state parameter)
        byte[] stateBytes = new byte[24];
        SECURE_RANDOM.nextBytes(stateBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);

        // Lưu state vào session trước khi redirect sang Google
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_OAUTH_STATE, state);

        // Xây dựng Google OAuth URL kèm state
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(AppConfig.GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("email profile openid", StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&prompt=select_account";

        // Redirect sang Google
        resp.sendRedirect(googleAuthUrl);
    }
}
