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
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setMaxAge((int)(ACCESS_TOKEN_EXPIRY_MS / 1000));
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Gắn Refresh Token Cookie (HttpOnly)
     */
    public static void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setMaxAge(REFRESH_TOKEN_EXPIRY_SECS);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Xóa sạch Token Cookies
     */
    public static void clearTokens(HttpServletRequest request, HttpServletResponse response) {
        Cookie ac = new Cookie("accessToken", "");
        ac.setPath(request.getContextPath() + "/");
        ac.setMaxAge(0);
        ac.setHttpOnly(true);
        response.addCookie(ac);

        Cookie rc = new Cookie("refreshToken", "");
        rc.setPath(request.getContextPath() + "/");
        rc.setMaxAge(0);
        rc.setHttpOnly(true);
        response.addCookie(rc);
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
