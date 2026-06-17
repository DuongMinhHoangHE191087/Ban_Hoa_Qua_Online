package servlet.auth;

import config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Xây dựng Google OAuth URL
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(AppConfig.GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("email profile openid", StandardCharsets.UTF_8)
                + "&prompt=select_account";
        
        // Redirect sang Google
        resp.sendRedirect(googleAuthUrl);
    }
}
