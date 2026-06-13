package com.fruitmkt.test;

import com.fruitmkt.service.EmailService;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;

public class EmailServiceTest {

    private EmailService emailService;

    @Before
    public void setUp() {
        emailService = new EmailService();
    }

    @Test(expected = SQLException.class)
    public void sendVerificationCodeEmail_nullEmail_throws() throws Exception {
        emailService.sendVerificationCodeEmail(null, "John", "123456");
    }

    @Test(expected = SQLException.class)
    public void sendVerificationCodeEmail_blankEmail_throws() throws Exception {
        emailService.sendVerificationCodeEmail("", "John", "123456");
    }

    @Test(expected = SQLException.class)
    public void sendVerificationCodeEmail_nullFullName_throws() throws Exception {
        emailService.sendVerificationCodeEmail("test@example.com", null, "123456");
    }

    @Test(expected = SQLException.class)
    public void sendVerificationCodeEmail_nullCode_throws() throws Exception {
        emailService.sendVerificationCodeEmail("test@example.com", "John", null);
    }

    @Test(expected = SQLException.class)
    public void sendPasswordResetEmail_nullEmail_throws() throws Exception {
        emailService.sendPasswordResetEmail(null, "John", "https://example.com/reset");
    }

    @Test(expected = SQLException.class)
    public void sendPasswordResetEmail_nullFullName_throws() throws Exception {
        emailService.sendPasswordResetEmail("test@example.com", null, "https://example.com/reset");
    }

    @Test(expected = SQLException.class)
    public void sendPasswordResetEmail_nullLink_throws() throws Exception {
        emailService.sendPasswordResetEmail("test@example.com", "John", null);
    }

    @Test(expected = SQLException.class)
    public void sendOrderNotificationEmail_nullEmail_throws() throws Exception {
        emailService.sendOrderNotificationEmail(null, "John", "ORD-123", "SHIPPED", "https://example.com");
    }

    @Test(expected = SQLException.class)
    public void sendOrderNotificationEmail_nullFullName_throws() throws Exception {
        emailService.sendOrderNotificationEmail("test@example.com", null, "ORD-123", "SHIPPED", "https://example.com");
    }

    @Test(expected = SQLException.class)
    public void sendOrderNotificationEmail_nullOrderId_throws() throws Exception {
        emailService.sendOrderNotificationEmail("test@example.com", "John", null, "SHIPPED", "https://example.com");
    }

    @Test(expected = SQLException.class)
    public void sendOrderNotificationEmail_nullStatus_throws() throws Exception {
        emailService.sendOrderNotificationEmail("test@example.com", "John", "ORD-123", null, "https://example.com");
    }

    @Test(expected = SQLException.class)
    public void sendOrderNotificationEmail_nullOrderDetailUrl_throws() throws Exception {
        emailService.sendOrderNotificationEmail("test@example.com", "John", "ORD-123", "SHIPPED", null);
    }

    @Test(expected = SQLException.class)
    public void sendShopApplicationReceivedEmail_nullEmail_throws() throws Exception {
        emailService.sendShopApplicationReceivedEmail(null, "Owner", "My Shop");
    }

    @Test(expected = SQLException.class)
    public void sendShopApplicationReceivedEmail_nullOwnerName_throws() throws Exception {
        emailService.sendShopApplicationReceivedEmail("test@example.com", null, "My Shop");
    }

    @Test(expected = SQLException.class)
    public void sendShopApplicationReceivedEmail_nullShopName_throws() throws Exception {
        emailService.sendShopApplicationReceivedEmail("test@example.com", "Owner", null);
    }

    @Test(expected = SQLException.class)
    public void sendShopApprovedEmail_nullEmail_throws() throws Exception {
        emailService.sendShopApprovedEmail(null, "Owner", "My Shop");
    }

    @Test(expected = SQLException.class)
    public void sendShopRejectedEmail_nullEmail_throws() throws Exception {
        emailService.sendShopRejectedEmail(null, "Owner", "My Shop", "Not enough docs");
    }

    @Test(expected = SQLException.class)
    public void sendShopRejectedEmail_nullReason_throws() throws Exception {
        emailService.sendShopRejectedEmail("test@example.com", "Owner", "My Shop", null);
    }

    @Test(expected = SQLException.class)
    public void sendHtml_nullEmail_throws() throws Exception {
        emailService.sendHtml(null, "Subject", "<p>Body</p>");
    }

    @Test(expected = SQLException.class)
    public void sendHtml_nullSubject_throws() throws Exception {
        emailService.sendHtml("test@example.com", null, "<p>Body</p>");
    }

    @Test(expected = SQLException.class)
    public void sendHtml_nullBody_throws() throws Exception {
        emailService.sendHtml("test@example.com", "Subject", null);
    }
}
