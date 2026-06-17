package test;

import service.auth.AuthService;
import service.catalog.InventoryService;
import service.system.EmailService;
import service.shop.ShopService;
import service.chat.ChatService;
import service.system.EmailTemplateService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.sql.SQLException;

public class BoundaryAndExceptionTests {

    private AuthService authService;
    private InventoryService inventoryService;
    private EmailService emailService;
    private ShopService shopService;
    private ChatService chatService;

    @Before
    public void setUp() {
        authService = new AuthService();
        inventoryService = new InventoryService();
        emailService = new EmailService();
        shopService = new ShopService();
        chatService = new ChatService();
    }

    // ─── AuthService Boundary Tests ───
    @Test(expected = IllegalArgumentException.class)
    public void registerUser_nullUser_throws() throws Exception {
        authService.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changePassword_negativeUserId_throws() throws Exception {
        authService.changePassword(-5, "oldPass", "newPass");
    }

    @Test(expected = IllegalArgumentException.class)
    public void changePassword_nullOldPass_throws() throws Exception {
        authService.changePassword(1, null, "newPass");
    }

    @Test(expected = IllegalArgumentException.class)
    public void changePassword_emptyNewPass_throws() throws Exception {
        authService.changePassword(1, "oldPass", "   ");
    }

    // ─── InventoryService Boundary Tests ───
    @Test(expected = IllegalArgumentException.class)
    public void getRestockHistory_negativeOwnerId_throws() throws SQLException {
        inventoryService.getRestockHistory(-99);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLogs_negativeVariantId_throws() throws SQLException {
        inventoryService.getLogs(-1);
    }

    // ─── EmailService Boundary Tests ───
    @Test(expected = IllegalArgumentException.class)
    public void sendVerificationCodeEmail_nullEmail_throws() throws SQLException {
        emailService.sendVerificationCodeEmail(null, "John", "123456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendVerificationCodeEmail_emptyCode_throws() throws SQLException {
        emailService.sendVerificationCodeEmail("test@example.com", "John", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendPasswordResetEmail_emptyLink_throws() throws SQLException {
        emailService.sendPasswordResetEmail("test@example.com", "John", "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendOrderNotificationEmail_emptyOrderId_throws() throws SQLException {
        emailService.sendOrderNotificationEmail("test@example.com", "John", "", "PENDING", "http://link");
    }

    // ─── ShopService Boundary Tests ───
    @Test(expected = IllegalArgumentException.class)
    public void getShopsByStatus_nullStatus_throws() throws SQLException {
        shopService.getShopsByStatus(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShopStatus_negativeProfileId_throws() throws SQLException {
        shopService.updateShopStatus(-5, "APPROVED", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateShopStatus_rejectedWithoutReason_throws() throws SQLException {
        shopService.updateShopStatus(1, "REJECTED", "   ");
    }

    // ─── ChatService Boundary Tests ───
    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_nullContent_throws() throws SQLException {
        chatService.sendMessage(1, 2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_scriptTag_throws() throws SQLException {
        chatService.sendMessage(1, 2, "Hello <script>alert(1)</script>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessage_imgOnErrorTag_throws() throws SQLException {
        chatService.sendMessage(1, 2, "<img src=x onerror=alert(1)>");
    }

    // ─── EmailTemplateService Extreme Escape Tests ───
    @Test
    public void escapeHtml_extremeCases() {
        String result = EmailTemplateService.escapeHtml("\"'&=<>");
        assertEquals("&quot;&#39;&amp;&#61;&lt;&gt;", result);
        
        assertEquals("", EmailTemplateService.escapeHtml(null));
        assertEquals("normalText123", EmailTemplateService.escapeHtml("normalText123"));
    }
}
