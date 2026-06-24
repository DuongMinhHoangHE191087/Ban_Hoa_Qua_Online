package util;

import config.AppConfig;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * ValidationUtil — Trung tâm kiểm tra dữ liệu đầu vào cho toàn bộ ứng dụng.
 *
 * NGUYÊN TẮC:
 *   - Mọi validation tái sử dụng phải nằm ở đây, KHÔNG inline trong Servlet/Service.
 *   - Boolean methods (isValid*) chỉ trả về true/false, không throw.
 *   - require* methods throw Exception ngay nếu invalid — dùng trong Service layer.
 *   - Không phụ thuộc vào framework hay HTTP context.
 *
 * @author fruitmkt-team
 */
public final class ValidationUtil {

    // ── Compiled Patterns ──────────────────────────────────────────────────
    /** RFC 5322 simplified — chặn local part bắt đầu/kết thúc bằng dấu chấm */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9+_.\\-]*@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");

    /** Số điện thoại VN: đầu 03x,05x,07x,08x,09x, 10 chữ số */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0|\\+84)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-9])[0-9]{7}$");

    /** Ký tự log injection: newline, carriage return */
    private static final Pattern LOG_INJECTION_CHARS = Pattern.compile("[\\r\\n\\t]");

    // ── Giới hạn độ dài ────────────────────────────────────────────────────
    public static final int PASSWORD_MIN_LEN = 8;
    public static final int PASSWORD_MAX_LEN = 64;
    public static final int FULL_NAME_MAX_LEN = 100;
    public static final int SHOP_NAME_MIN_LEN = 2;
    public static final int SHOP_NAME_MAX_LEN = 150;
    public static final int ADDRESS_MAX_LEN = 500;
    public static final int REJECTION_REASON_MAX_LEN = 500;
    public static final int SHOP_DESC_MAX_LEN = 2000;
    public static final int REVIEW_TEXT_MAX_LEN = 1000;
    public static final int REVIEW_MIN_RATING = 1;
    public static final int REVIEW_MAX_RATING = 5;

    // ── Boolean validators (không throw) ──────────────────────────────────

    /** Kiểm tra email hợp lệ (RFC 5322 simplified) */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Kiểm tra số điện thoại VN hợp lệ (10 số, đầu số đúng) */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Chuẩn hóa số điện thoại về định dạng bắt đầu bằng '0' duy nhất, loại bỏ ký tự không phải số.
     * Ví dụ: "+84 987 654 321" -> "0987654321", "0987-654-321" -> "0987654321"
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return null;
        String clean = phone.trim().replaceAll("[^0-9+]", "");
        if (clean.startsWith("+84")) {
            clean = "0" + clean.substring(3);
        } else if (clean.startsWith("84") && clean.length() > 9) {
            clean = "0" + clean.substring(2);
        }
        return clean;
    }

    /** Kiểm tra mật khẩu đủ mạnh (8–64 ký tự) */
    public static boolean isValidPassword(String pwd) {
        return isValidPassword(pwd, PASSWORD_MIN_LEN, PASSWORD_MAX_LEN);
    }

    /** Kiểm tra mật khẩu với giới hạn tùy chỉnh */
    public static boolean isValidPassword(String pwd, int minLen, int maxLen) {
        if (pwd == null) return false;
        int len = pwd.length();
        return len >= minLen && len <= maxLen;
    }

    /** Kiểm tra tên cửa hàng hợp lệ (2–150 ký tự, không rỗng) */
    public static boolean isValidShopName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= SHOP_NAME_MIN_LEN && trimmed.length() <= SHOP_NAME_MAX_LEN;
    }

    /** Kiểm tra địa chỉ hợp lệ (không rỗng, ≤500 ký tự) */
    public static boolean isValidAddress(String addr) {
        if (addr == null) return false;
        String trimmed = addr.trim();
        return !trimmed.isEmpty() && trimmed.length() <= ADDRESS_MAX_LEN;
    }

    /** Kiểm tra lý do từ chối (không rỗng, ≤500 ký tự) */
    public static boolean isValidRejectionReason(String reason) {
        if (reason == null) return false;
        String trimmed = reason.trim();
        return !trimmed.isEmpty() && trimmed.length() <= REJECTION_REASON_MAX_LEN;
    }

    /**
     * Kiểm tra Part upload tài liệu shop hợp lệ:
     *   - Không rỗng
     *   - Kích thước ≤ AppConfig.MAX_SHOP_DOC_SIZE_BYTES (25MB)
     *   - Extension nằm trong whitelist: PDF, JPG, JPEG, PNG, DOCX
     *
     * @return null nếu hợp lệ, hoặc chuỗi mô tả lỗi cụ thể
     */
    public static String validateShopDoc(String filename, long size) {
        if (filename == null || filename.trim().isEmpty()) {
            return "Vui lòng chọn một tệp tài liệu hợp lệ.";
        }
        if (size <= 0) {
            return "Tệp '" + sanitizeForLog(filename) + "' đang trống. Vui lòng chọn lại file khác.";
        }
        if (size > AppConfig.MAX_SHOP_DOC_SIZE_BYTES) {
            return "Tệp '" + sanitizeForLog(filename) + "' vượt quá giới hạn "
                    + (AppConfig.MAX_SHOP_DOC_SIZE_BYTES / 1024 / 1024) + "MB.";
        }
        if (!isAllowedDocExtension(filename)) {
            return "Tệp '" + sanitizeForLog(filename) + "' không được hỗ trợ. "
                    + "Chỉ chấp nhận: " + String.join(", ", AppConfig.ALLOWED_DOC_EXTS).toUpperCase() + ".";
        }
        return null; // hợp lệ
    }

    /**
     * Kiểm tra extension file tài liệu có nằm trong whitelist không.
     * Whitelist: PDF, JPG, JPEG, PNG, DOCX (từ AppConfig.ALLOWED_DOC_EXTS)
     */
    public static boolean isAllowedDocExtension(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename).toLowerCase();
        for (String allowed : AppConfig.ALLOWED_DOC_EXTS) {
            if (allowed.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    /** Kiểm tra extension ảnh sản phẩm có nằm trong whitelist không */
    public static boolean isAllowedImageExtension(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename).toLowerCase();
        for (String allowed : AppConfig.ALLOWED_IMAGE_EXTS) {
            if (allowed.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    /** Chuỗi không null và không rỗng sau trim */
    public static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Số nguyên dương */
    public static boolean isPositiveInt(int n) {
        return n > 0;
    }

    /** Giá trị price/decimal hợp lệ (> 0) */
    public static boolean isPositiveDecimal(BigDecimal bd) {
        return bd != null && bd.compareTo(BigDecimal.ZERO) > 0;
    }

    /** Kiểm tra đánh giá hợp lệ (1-5) */
    public static boolean isValidRating(int rating) {
        return rating >= REVIEW_MIN_RATING && rating <= REVIEW_MAX_RATING;
    }

    /** Kiểm tra text đánh giá hợp lệ (không rỗng, ≤1000 ký tự) */
    public static boolean isValidReviewText(String text) {
        if (text == null) return false;
        String trimmed = text.trim();
        return !trimmed.isEmpty() && trimmed.length() <= REVIEW_TEXT_MAX_LEN;
    }

    /** Kiểm tra loại yêu cầu hoàn trả hợp lệ (RETURN, EXCHANGE, REFUND) */
    public static boolean isValidReturnType(String type) {
        if (type == null) return false;
        return type.equals("RETURN") || type.equals("EXCHANGE") || type.equals("REFUND");
    }

    /** Kiểm tra trạng thái yêu cầu hoàn trả hợp lệ */
    public static boolean isValidReturnStatus(String status) {
        if (status == null) return false;
        return status.equals("PENDING") || status.equals("APPROVED") ||
               status.equals("REJECTED") || status.equals("COMPLETED");
    }

    // ── Require methods (throw Exception nếu invalid) ──────────────────────

    /**
     * Yêu cầu chuỗi không rỗng — dùng trong Service layer để validate nhanh.
     * @throws Exception với message rõ ràng nếu rỗng
     */
    public static String requireNotBlank(String value, String fieldLabel) throws Exception {
        if (!notBlank(value)) {
            throw new Exception(fieldLabel + " không được để trống!");
        }
        return value.trim();
    }

    /** Yêu cầu email hợp lệ */
    public static String requireValidEmail(String email, String fieldLabel) throws Exception {
        String v = requireNotBlank(email, fieldLabel);
        if (!isValidEmail(v)) {
            throw new Exception(fieldLabel + " không đúng định dạng (ví dụ: ten@gmail.com)!");
        }
        return v;
    }

    /** Yêu cầu mật khẩu hợp lệ */
    public static String requireValidPassword(String pwd, String fieldLabel) throws Exception {
        requireNotBlank(pwd, fieldLabel);
        if (!isValidPassword(pwd)) {
            throw new Exception(fieldLabel + " phải từ " + PASSWORD_MIN_LEN
                    + " đến " + PASSWORD_MAX_LEN + " ký tự!");
        }
        return pwd;
    }

    /** Yêu cầu số điện thoại VN hợp lệ */
    public static String requireValidPhone(String phone, String fieldLabel) throws Exception {
        String normalized = normalizePhone(phone);
        String v = requireNotBlank(normalized, fieldLabel);
        if (!isValidPhone(v)) {
            throw new Exception(fieldLabel + " không đúng định dạng (10 số, đầu 03x/05x/07x/08x/09x)!");
        }
        return v;
    }

    /** Yêu cầu tên cửa hàng hợp lệ */
    public static String requireValidShopName(String name, String fieldLabel) throws Exception {
        String v = requireNotBlank(name, fieldLabel);
        if (!isValidShopName(v)) {
            throw new Exception(fieldLabel + " phải từ " + SHOP_NAME_MIN_LEN
                    + " đến " + SHOP_NAME_MAX_LEN + " ký tự!");
        }
        return v;
    }

    /** Yêu cầu địa chỉ hợp lệ */
    public static String requireValidAddress(String addr, String fieldLabel) throws Exception {
        String v = requireNotBlank(addr, fieldLabel);
        if (!isValidAddress(v)) {
            throw new Exception(fieldLabel + " không được vượt quá " + ADDRESS_MAX_LEN + " ký tự!");
        }
        return v;
    }

    /** Yêu cầu lý do từ chối hợp lệ */
    public static String requireValidRejectionReason(String reason, String fieldLabel) throws Exception {
        String v = requireNotBlank(reason, fieldLabel);
        if (!isValidRejectionReason(v)) {
            throw new Exception(fieldLabel + " không được vượt quá " + REJECTION_REASON_MAX_LEN + " ký tự!");
        }
        return v;
    }

    /** Yêu cầu đánh giá hợp lệ */
    public static int requireValidRating(int rating, String fieldLabel) throws Exception {
        if (!isValidRating(rating)) {
            throw new Exception(fieldLabel + " phải từ " + REVIEW_MIN_RATING + " đến " + REVIEW_MAX_RATING + " sao!");
        }
        return rating;
    }

    /** Yêu cầu text đánh giá hợp lệ */
    public static String requireValidReviewText(String text, String fieldLabel) throws Exception {
        String v = requireNotBlank(text, fieldLabel);
        if (!isValidReviewText(v)) {
            throw new Exception(fieldLabel + " không được vượt quá " + REVIEW_TEXT_MAX_LEN + " ký tự!");
        }
        return v;
    }

    /** Yêu cầu loại yêu cầu hoàn trả hợp lệ */
    public static String requireValidReturnType(String type, String fieldLabel) throws Exception {
        String v = requireNotBlank(type, fieldLabel);
        if (!isValidReturnType(v)) {
            throw new Exception(fieldLabel + " phải là: RETURN, EXCHANGE, hoặc REFUND!");
        }
        return v;
    }

    /** Yêu cầu trạng thái yêu cầu hoàn trả hợp lệ */
    public static String requireValidReturnStatus(String status, String fieldLabel) throws Exception {
        String v = requireNotBlank(status, fieldLabel);
        if (!isValidReturnStatus(v)) {
            throw new Exception(fieldLabel + " không hợp lệ!");
        }
        return v;
    }

    // ── Sanitize helpers ───────────────────────────────────────────────────

    /**
     * Làm sạch chuỗi trước khi đưa vào log — loại bỏ \n \r \t.
     * Dùng để tránh Log Injection attacks.
     */
    public static String sanitizeForLog(String value) {
        if (value == null) return "(null)";
        return LOG_INJECTION_CHARS.matcher(value).replaceAll("_");
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private static String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    private ValidationUtil() { /* Utility class — không khởi tạo */ }
}
