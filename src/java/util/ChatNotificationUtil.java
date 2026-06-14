package util;

import config.AppConfig;
import java.util.Locale;

/**
 * ChatNotificationUtil - Helper cho preview và liên kết notification của chat.
 */
public final class ChatNotificationUtil {

    private ChatNotificationUtil() {
    }

    /**
     * Tạo preview an toàn cho notification chat.
     * - Loại bỏ tag HTML
     * - Gom whitespace
     * - Giới hạn độ dài để tránh spam UI
     */
    public static String sanitizePreview(String content, String mediaType) {
        String text = content != null ? content : "";

        // Remove tags first, then normalize whitespace/control chars.
        text = text.replaceAll("(?s)<[^>]*>", " ");
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ");
        text = text.replace('\r', ' ');
        text = text.replace('\n', ' ');
        text = text.replace('\t', ' ');
        text = text.replaceAll("\\s+", " ").trim();

        if (text.isEmpty()) {
            String normalizedMediaType = mediaType != null
                    ? mediaType.trim().toUpperCase(Locale.ROOT)
                    : "";
            if ("IMAGE".equals(normalizedMediaType)) {
                return "[Hình ảnh]";
            }
            if ("VIDEO".equals(normalizedMediaType)) {
                return "[Video]";
            }
            return "[Tin nhắn mới]";
        }

        if (text.length() > 50) {
            return text.substring(0, 50) + "…";
        }
        return text;
    }

    /**
     * Build action URL cho notification chat theo vai trò người nhận.
     */
    public static String buildActionUrl(String recipientRole, int sessionId) {
        if (AppConfig.ROLE_ADMIN.equals(recipientRole)) {
            return "/admin/chat?sessionId=" + sessionId;
        }
        if (AppConfig.ROLE_SHOP_OWNER.equals(recipientRole)) {
            return "/shop/chat?sessionId=" + sessionId;
        }
        return "/chat?sessionId=" + sessionId;
    }
}
