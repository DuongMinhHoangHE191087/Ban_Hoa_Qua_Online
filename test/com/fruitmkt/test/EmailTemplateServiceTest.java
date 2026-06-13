package com.fruitmkt.test;

import com.fruitmkt.service.EmailTemplateService;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

public class EmailTemplateServiceTest {

    private EmailTemplateService templateService;

    @Before
    public void setUp() {
        templateService = new EmailTemplateService();
    }

    @Test
    public void escapeHtml_htmlMetaCharacters_escaped() {
        String result = EmailTemplateService.escapeHtml("<script>alert('xss')</script>");
        assertFalse("Should escape < characters", result.contains("<script>"));
        assertTrue("Should contain &lt; escape", result.contains("&lt;"));
        assertTrue("Should contain &gt; escape", result.contains("&gt;"));
    }

    @Test
    public void escapeHtml_ampersand_escaped() {
        String result = EmailTemplateService.escapeHtml("Tom & Jerry");
        assertTrue("Should escape ampersand", result.contains("&amp;"));
        assertFalse("Should not contain raw ampersand", result.equals("Tom & Jerry"));
    }

    @Test
    public void escapeHtml_quotes_escaped() {
        String result = EmailTemplateService.escapeHtml("He said \"hello\"");
        assertTrue("Should escape double quotes", result.contains("&quot;"));
    }

    @Test
    public void escapeHtml_singleQuotes_escaped() {
        String result = EmailTemplateService.escapeHtml("It's dangerous");
        assertTrue("Should escape single quotes", result.contains("&#39;"));
    }

    @Test
    public void escapeHtml_null_returnsEmpty() {
        String result = EmailTemplateService.escapeHtml(null);
        assertEquals("", result);
    }

    @Test
    public void escapeHtml_normalText_unchanged() {
        String result = EmailTemplateService.escapeHtml("Hello World");
        assertEquals("Hello World", result);
    }

    @Test
    public void buildVerificationEmail_containsCodePlaceholder() {
        String result = templateService.buildVerificationEmail("John Doe", "123456");
        assertTrue("Should contain full name", result.contains("John Doe"));
        assertTrue("Should contain verification code", result.contains("123456"));
        assertTrue("Should be HTML", result.contains("<html>"));
        assertTrue("Should contain body close", result.contains("</body>"));
    }

    @Test
    public void buildVerificationEmail_xssProtected() {
        String result = templateService.buildVerificationEmail("<script>alert('xss')</script>", "123456");
        assertFalse("Should escape HTML in name", result.contains("<script>"));
        assertTrue("Should escape as &lt;", result.contains("&lt;"));
    }

    @Test
    public void buildPasswordResetEmail_containsLink() {
        String result = templateService.buildPasswordResetEmail("Jane Doe", "https://example.com/reset?token=abc");
        assertTrue("Should contain full name", result.contains("Jane Doe"));
        assertTrue("Should contain reset link", result.contains("https://example.com/reset"));
        assertTrue("Should be HTML", result.contains("<html>"));
    }

    @Test
    public void buildPasswordResetEmail_xssProtected() {
        String result = templateService.buildPasswordResetEmail("Jane", "https://example.com?q=<script>");
        assertFalse("Should escape HTML in URL", result.contains("<script>"));
        assertTrue("Should escape as &lt;", result.contains("&lt;"));
    }

    @Test
    public void buildOrderNotificationEmail_containsOrderInfo() {
        String result = templateService.buildOrderNotificationEmail("Bob", "ORD-12345", "SHIPPED", "https://example.com/order");
        assertTrue("Should contain full name", result.contains("Bob"));
        assertTrue("Should contain order ID", result.contains("ORD-12345"));
        assertTrue("Should contain status", result.contains("SHIPPED"));
    }

    @Test
    public void buildOrderNotificationEmail_xssProtected() {
        String result = templateService.buildOrderNotificationEmail("Bob", "<img src=x onerror=alert('xss')>", "SHIPPED", "https://example.com");
        assertFalse("Should escape HTML injection", result.contains("onerror="));
        assertTrue("Should escape as &lt;", result.contains("&lt;"));
    }

    @Test
    public void buildShopApplicationReceivedEmail_containsShopInfo() {
        String result = templateService.buildShopApplicationReceivedEmail("Owner Name", "Fruit Shop");
        assertTrue("Should contain owner name", result.contains("Owner Name"));
        assertTrue("Should contain shop name", result.contains("Fruit Shop"));
        assertTrue("Should be HTML", result.contains("<html>"));
    }

    @Test
    public void buildShopApprovedEmail_containsCelebration() {
        String result = templateService.buildShopApprovedEmail("Owner Name", "Fruit Shop");
        assertTrue("Should contain owner name", result.contains("Owner Name"));
        assertTrue("Should contain shop name", result.contains("Fruit Shop"));
        assertTrue("Should contain approved message", result.contains("PHÊ DUYỆT"));
    }

    @Test
    public void buildShopRejectedEmail_containsRejectionReason() {
        String result = templateService.buildShopRejectedEmail("Owner Name", "Fruit Shop", "Insufficient documents");
        assertTrue("Should contain rejection reason", result.contains("Insufficient documents"));
        assertTrue("Should contain owner name", result.contains("Owner Name"));
        assertTrue("Should contain shop name", result.contains("Fruit Shop"));
    }

    @Test
    public void buildShopRejectedEmail_xssProtected() {
        String result = templateService.buildShopRejectedEmail("Owner", "Shop", "<b onmouseover=alert('xss')>Reason</b>");
        assertFalse("Should escape HTML in reason", result.contains("onmouseover="));
        assertTrue("Should escape as &lt;", result.contains("&lt;"));
    }

    @Test
    public void buildBrandedEmail_withNullCta_omitsCta() {
        String result = templateService.buildBrandedEmail(
                "Test Headline",
                "Intro text",
                "Main content",
                null,
                null,
                "Footer text"
        );
        assertTrue("Should contain headline", result.contains("Test Headline"));
        assertTrue("Should contain intro", result.contains("Intro text"));
        assertTrue("Should contain main content", result.contains("Main content"));
    }

    @Test
    public void buildBrandedEmail_withValidCta_includesCta() {
        String result = templateService.buildBrandedEmail(
                "Test Headline",
                "Intro text",
                "Main content",
                "Click Here",
                "https://example.com",
                "Footer text"
        );
        assertTrue("Should include CTA link", result.contains("https://example.com"));
        assertTrue("Should include CTA text", result.contains("Click Here"));
    }
}
