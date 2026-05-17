package com.fruitmkt.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * HashUtil — Tiện ích mã hóa và xác minh mật khẩu bằng BCrypt.
 *
 * THƯ VIỆN CẦN:
 *   jbcrypt-0.4.jar (hoặc bcrypt-*.jar) trong WEB-INF/lib/
 *   Tải tại: https://www.mindrot.org/projects/jBCrypt/
 *
 * CÁCH DÙNG:
 * <pre>
 *   // Khi đăng ký:
 *   String hash = HashUtil.hash(plainPassword);
 *   user.setPasswordHash(hash);
 *
 *   // Khi đăng nhập:
 *   boolean ok = HashUtil.verify(inputPassword, user.getPasswordHash());
 * </pre>
 *
 * @author fruitmkt-team
 */
public final class HashUtil {

    /** Cost factor cho BCrypt — tăng lên 12 nếu cần bảo mật cao hơn */
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Tạo BCrypt hash từ plain text password.
     * @param plainPassword mật khẩu chưa mã hóa
     * @return chuỗi BCrypt hash để lưu vào DB
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Kiểm tra plain password có khớp với hash không.
     * @param plainPassword mật khẩu người dùng nhập
     * @param storedHash    hash lấy từ DB
     * @return true nếu khớp
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        return BCrypt.checkpw(plainPassword, storedHash);
    }

    private HashUtil() {}
}
