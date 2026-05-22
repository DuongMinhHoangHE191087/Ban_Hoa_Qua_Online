package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

/**
 * EmailService — Gửi email qua SMTP. CHỈ lo transport, không build HTML.
 *
 * SRP: Class này CHỈ gửi email. Mọi HTML template được delegate sang EmailTemplateService.
 * Caller build nội dung HTML trước rồi gọi sendHtml() hoặc các method tiện ích.
 *
 * @author fruitmkt-team
 */
public class EmailService {

    private final EmailTemplateService templateService = new EmailTemplateService();

    // ── Convenience methods (nghiệp vụ cụ thể) ────────────────────────────

    /**
     * Gửi email xác minh OTP sau khi đăng ký.
     */
    public boolean sendVerificationCodeEmail(String toEmail, String fullName, String verificationCode)
            throws SQLException {
        String subject = "[" + AppConfig.APP_NAME + "] Mã xác minh tài khoản của bạn";
        String html = templateService.buildVerificationEmail(fullName, verificationCode);
        return sendHtml(toEmail, subject, html);
    }

    /**
     * Gửi email đặt lại mật khẩu.
     */
    public boolean sendPasswordResetEmail(String toEmail, String fullName, String resetLink)
            throws SQLException {
        String subject = "[" + AppConfig.APP_NAME + "] Yêu cầu đặt lại mật khẩu";
        String html = templateService.buildPasswordResetEmail(fullName, resetLink);
        return sendHtml(toEmail, subject, html);
    }

    /**
     * Gửi email thông báo cập nhật đơn hàng.
     */
    public boolean sendOrderNotificationEmail(String toEmail, String fullName,
                                               String orderId, String status, String orderDetailUrl)
            throws SQLException {
        String subject = "[" + AppConfig.APP_NAME + "] Đơn hàng " + orderId + " - " + status;
        String html = templateService.buildOrderNotificationEmail(fullName, orderId, status, orderDetailUrl);
        return sendHtml(toEmail, subject, html);
    }

    // ── Core transport ────────────────────────────────────────────────────

    /**
     * Gửi email HTML thuần — low-level, dùng khi cần tùy biến hoàn toàn.
     */
    public boolean sendHtml(String toEmail, String subject, String htmlBody) throws SQLException {
        try {
            Session session = buildSession();
            MimeMessage message = buildMessage(session, toEmail, subject, htmlBody);
            Transport transport = session.getTransport("smtp");
            try {
                transport.connect(AppConfig.EMAIL_SMTP_HOST, AppConfig.EMAIL_FROM, AppConfig.EMAIL_PASSWORD);
                transport.sendMessage(message, message.getAllRecipients());
                return true;
            } finally {
                if (transport.isConnected()) transport.close();
            }
        } catch (MessagingException e) {
            throw new SQLException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Session buildSession() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.starttls.required", "true");
        props.setProperty("mail.smtp.host", AppConfig.EMAIL_SMTP_HOST);
        props.setProperty("mail.smtp.port", AppConfig.EMAIL_SMTP_PORT);
        props.setProperty("mail.smtp.ssl.trust", AppConfig.EMAIL_SMTP_HOST);
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.smtp.connectiontimeout", "10000");
        props.setProperty("mail.smtp.timeout", "10000");
        props.setProperty("mail.smtp.writetimeout", "10000");

        String from = AppConfig.EMAIL_FROM;
        String pass = AppConfig.EMAIL_PASSWORD;
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });
    }

    private MimeMessage buildMessage(Session session, String toEmail, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(AppConfig.EMAIL_FROM));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        msg.setSubject(subject, "UTF-8");
        msg.setSentDate(new Date());
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        msg.saveChanges();
        return msg;
    }
}
