package com.fruitmkt.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TokenUtil — Tiện ích xử lý Access Token và Refresh Token bảo mật.
 * Sử dụng HmacSHA256 tự ký theo chuẩn gọn nhẹ, không cần thư viện ngoài.
 *
 * @author fruitmkt-team
 */
public final class TokenUtil {
    private static final String SECRET_KEY = "fruitmkt-super-secret-key-2026-secure-sha256";
    private static final long ACCESS_TOKEN_EXPIRY_MS = 15L * 60 * 1000; // 15 phút
    public static final int REFRESH_TOKEN_EXPIRY_SECS = 7 * 24 * 60 * 60; // 7 ngày

    /**
     * Tạo Access Token dạng: userId.expiresAt.signature
     */
    public static String generateAccessToken(int userId) {
        long expiresAt = System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS;
        String payload = userId + "." + expiresAt;
        String signature = calculateHmac(payload, SECRET_KEY);
        return payload + "." + signature;
    }

    /**
     * Giải mã và xác thực Access Token. Trả về userId nếu hợp lệ, ngược lại trả về null.
     */
    public static Integer verifyAccessToken(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;

        try {
            int userId = Integer.parseInt(parts[0]);
            long expiresAt = Long.parseLong(parts[1]);
            String signature = parts[2];

            if (System.currentTimeMillis() > expiresAt) {
                return null; // Token hết hạn
            }

            String payload = userId + "." + expiresAt;
            String expectedSignature = calculateHmac(payload, SECRET_KEY);

            // Chống timing attack
            if (MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8), 
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return userId;
            }
        } catch (Exception e) {
            // Lỗi parse
        }
        return null;
    }

    /**
     * Sinh Refresh Token ngẫu nhiên (UUID) để lưu Database
     */
    public static String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gắn Access Token Cookie (HttpOnly)
     */
    public static void addAccessTokenCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        addCookie(response, request, "accessToken", token, (int) (ACCESS_TOKEN_EXPIRY_MS / 1000));
    }

    /**
     * Gắn Refresh Token Cookie (HttpOnly)
     */
    public static void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        addCookie(response, request, "refreshToken", token, REFRESH_TOKEN_EXPIRY_SECS);
    }

    /**
     * Xóa sạch Token Cookies
     */
    public static void clearTokens(HttpServletRequest request, HttpServletResponse response) {
        addCookie(response, request, "accessToken", "", 0);
        addCookie(response, request, "refreshToken", "", 0);
    }

    private static void addCookie(HttpServletResponse response, HttpServletRequest request,
                                  String name, String value, int maxAgeSeconds) {
        boolean secure = request.isSecure()
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append('=').append(value == null ? "" : value);
        cookie.append("; Path=").append(request.getContextPath()).append('/');
        cookie.append("; Max-Age=").append(maxAgeSeconds);
        cookie.append("; HttpOnly");
        if (secure) {
            cookie.append("; Secure");
        }
        cookie.append("; SameSite=Lax");

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Đọc giá trị Cookie theo tên
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private static String calculateHmac(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC-SHA256", e);
        }
    }

    private TokenUtil() {}
}
