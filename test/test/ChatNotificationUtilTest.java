package test;

import config.AppConfig;
import org.junit.Test;
import util.ChatNotificationUtil;

import static org.junit.Assert.assertEquals;

public class ChatNotificationUtilTest {

    @Test
    public void sanitizePreview_stripsHtmlAndNormalizesWhitespace() {
        String preview = ChatNotificationUtil.sanitizePreview(
                "<b>Hello</b> <script>alert(1)</script>\n   world",
                null
        );

        assertEquals("Hello alert(1) world", preview);
    }

    @Test
    public void sanitizePreview_mediaOnlyUsesPlaceholder() {
        assertEquals("[Hình ảnh]", ChatNotificationUtil.sanitizePreview(null, "image"));
        assertEquals("[Video]", ChatNotificationUtil.sanitizePreview("   ", "VIDEO"));
        assertEquals("[Tin nhắn mới]", ChatNotificationUtil.sanitizePreview("", null));
    }

    @Test
    public void buildActionUrl_usesRecipientRole() {
        assertEquals("/admin/chat?sessionId=10", ChatNotificationUtil.buildActionUrl(AppConfig.ROLE_ADMIN, 10));
        assertEquals("/shop/chat?sessionId=10", ChatNotificationUtil.buildActionUrl(AppConfig.ROLE_SHOP_OWNER, 10));
        assertEquals("/chat?sessionId=10", ChatNotificationUtil.buildActionUrl(AppConfig.ROLE_CUSTOMER, 10));
        assertEquals("/chat?sessionId=10", ChatNotificationUtil.buildActionUrl(null, 10));
    }
}
