package com.fruitmkt.util;

/**
 * ValidationUtil — /** Tiện ích validate input từ form/request. Dùng trước khi gọi Service. */
 * @author fruitmkt-team
 */
public final class ValidationUtil {

        /** Kiểm tra email hợp lệ */
        public static boolean isValidEmail(String email) { throw new UnsupportedOperationException(); }
        /** Kiểm tra số điện thoại VN hợp lệ (10 số) */
        public static boolean isValidPhone(String phone) { throw new UnsupportedOperationException(); }
        /** Kiểm tra mật khẩu đủ mạnh (8-64 ký tự) */
        public static boolean isValidPassword(String pwd) { throw new UnsupportedOperationException(); }
        /** Chuỗi không null và không rỗng */
        public static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
        /** Số nguyên dương */
        public static boolean isPositiveInt(int n) { return n > 0; }
        /** Giá trị price hợp lệ (> 0) */
        public static boolean isPositiveDecimal(java.math.BigDecimal bd) { return bd != null && bd.compareTo(java.math.BigDecimal.ZERO) > 0; }

    private ValidationUtil() {}
}
