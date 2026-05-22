package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EmailTemplateService — Xây dựng HTML template cho từng loại email gửi đi.
 *
 * SRP: Class này CHỈ build HTML string, không gửi email.
 * Mỗi public method tương ứng 1 loại email nghiệp vụ.
 *
 * @author fruitmkt-team
 */
public class EmailTemplateService {

    // ── Style constants ────────────────────────────────────────────────────
    private static final String EMAIL_STYLE_BASE =
            "font-family:Arial,Helvetica,sans-serif;background:#f5f8f6;margin:0;padding:0;color:#1f2937;";
    private static final String CARD_STYLE =
            "max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #dbe7df;"
            + "border-radius:18px;overflow:hidden;box-shadow:0 18px 48px rgba(20,83,45,0.10);";
    private static final String HEADER_STYLE =
            "padding:28px 28px 20px;background:linear-gradient(135deg,#14532d 0%,#1f6d3b 100%);color:#ffffff;";
    private static final String BODY_STYLE = "padding:28px;line-height:1.7;font-size:15px;";
    private static final String FOOTER_STYLE =
            "padding:20px 28px 28px;border-top:1px solid #e5efe8;background:#fbfdfb;"
            + "color:#607166;font-size:12px;line-height:1.6;";

    // ── Public template builders ───────────────────────────────────────────

    /** Template email xác minh OTP 6 số. */
    public String buildVerificationEmail(String fullName, String verificationCode) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("Mã có hiệu lực", "5 phút");
        facts.put("Gửi lại mã", "Sau 1 phút");
        facts.put("Bảo mật", "Không chia sẻ cho bất kỳ ai");

        String factsHtml = buildFactsTable(facts);
        String mainHtml = buildOtpBox(verificationCode, factsHtml);
        String footerHtml = "Nếu bạn không tạo tài khoản này, chỉ cần bỏ qua email. "
                + "Không ai có thể kích hoạt tài khoản nếu không có mã này."
                + "<br><br>Trân trọng,<br><strong>Đội ngũ " + escapeHtml(AppConfig.APP_NAME) + "</strong>";

        return buildBrandedEmail(
                "Xác minh tài khoản",
                "<p style='margin:0 0 10px 0;'>Xin chào <strong>" + escapeHtml(fullName) + "</strong>,</p>"
                + "<p style='margin:0;'>Cảm ơn bạn đã đăng ký. Để hoàn tất việc tạo tài khoản, "
                + "vui lòng nhập mã xác minh bên dưới.</p>",
                mainHtml,
                "Xem trang xác minh",
                AppConfig.APP_BASE_URL + "/auth/verify",
                footerHtml);
    }

    /** Template email đặt lại mật khẩu. */
    public String buildPasswordResetEmail(String fullName, String resetLink) {
        String mainHtml = "<div style='text-align:center;margin:22px 0;'>"
                + "<a href='" + escapeHtml(resetLink) + "' "
                + "style='display:inline-block;padding:14px 28px;border-radius:14px;"
                + "background:#14532d;color:#fff;font-weight:700;text-decoration:none;font-size:15px;'>"
                + "Đặt lại mật khẩu</a></div>"
                + "<p style='font-size:13px;color:#607166;text-align:center;margin:0;'>"
                + "Liên kết có hiệu lực trong 15 phút.</p>";

        String footerHtml = "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. "
                + "Tài khoản vẫn an toàn.<br><br>Trân trọng,<br><strong>Đội ngũ "
                + escapeHtml(AppConfig.APP_NAME) + "</strong>";

        return buildBrandedEmail(
                "Đặt lại mật khẩu",
                "<p style='margin:0 0 10px 0;'>Xin chào <strong>" + escapeHtml(fullName) + "</strong>,</p>"
                + "<p style='margin:0;'>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>",
                mainHtml,
                null, null,
                footerHtml);
    }

    /** Template email thông báo đơn hàng. */
    public String buildOrderNotificationEmail(String fullName, String orderId, String status, String orderDetailUrl) {
        String mainHtml = "<div style='background:#f0f8f3;border:1px solid #c8e2d0;border-radius:14px;"
                + "padding:16px 20px;margin:16px 0;'>"
                + "<p style='margin:0 0 8px;font-size:13px;color:#607166;'>Mã đơn hàng</p>"
                + "<p style='margin:0;font-size:18px;font-weight:800;color:#14532d;letter-spacing:2px;'>"
                + escapeHtml(orderId) + "</p>"
                + "<p style='margin:8px 0 0;font-size:14px;color:#1f2937;'>Trạng thái: <strong>"
                + escapeHtml(status) + "</strong></p>"
                + "</div>";

        String footerHtml = "Cảm ơn bạn đã tin tưởng " + escapeHtml(AppConfig.APP_NAME)
                + ".<br><br>Trân trọng,<br><strong>Đội ngũ " + escapeHtml(AppConfig.APP_NAME) + "</strong>";

        return buildBrandedEmail(
                "Cập nhật đơn hàng",
                "<p style='margin:0;'>Xin chào <strong>" + escapeHtml(fullName)
                + "</strong>, đơn hàng của bạn vừa được cập nhật.</p>",
                mainHtml,
                "Xem chi tiết đơn hàng",
                orderDetailUrl,
                footerHtml);
    }

    // ── Core layout builder (dùng nội bộ) ─────────────────────────────────

    /**
     * Tổng hợp HTML hoàn chỉnh cho 1 email branded.
     * primaryCtaText/Url nullable — bỏ qua nếu null hoặc rỗng.
     */
    public String buildBrandedEmail(
            String headline,
            String introHtml,
            String mainHtml,
            String primaryCtaText,
            String primaryCtaUrl,
            String footerHtml) {

        return "<html><body style='" + EMAIL_STYLE_BASE + "'>"
                + "<div style='padding:32px 16px;'>"
                + "<div style='" + CARD_STYLE + "'>"
                + buildHeader(headline)
                + "<div style='" + BODY_STYLE + "'>"
                + "<div style='font-size:16px;color:#385143;margin-bottom:16px;'>" + introHtml + "</div>"
                + mainHtml
                + buildCta(primaryCtaText, primaryCtaUrl)
                + "</div>"
                + buildFooter(footerHtml)
                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private String buildHeader(String headline) {
        return "<div style='" + HEADER_STYLE + "'>"
                + "<div style='display:flex;align-items:center;gap:12px;margin-bottom:18px;'>"
                + "<div style='width:44px;height:44px;border-radius:999px;background:rgba(255,255,255,0.18);"
                + "display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:700;'>M</div>"
                + "<div>"
                + "<div style='font-size:13px;letter-spacing:1.2px;text-transform:uppercase;opacity:0.88;'>"
                + escapeHtml(AppConfig.APP_NAME) + "</div>"
                + "<div style='font-size:24px;font-weight:800;margin-top:4px;'>" + escapeHtml(headline) + "</div>"
                + "</div>"
                + "</div>"
                + "<div style='font-size:14px;opacity:0.92;max-width:520px;'>"
                + "Sàn nông sản sạch, hiện đại và an toàn cho người dùng.</div>"
                + "</div>";
    }

    private String buildCta(String text, String url) {
        if (text == null || text.trim().isEmpty() || url == null || url.trim().isEmpty()) {
            return "";
        }
        return "<div style='margin-top:24px;text-align:center;'>"
                + "<a href='" + escapeHtml(url) + "' style='display:inline-block;background:"
                + AppConfig.APP_BRAND_COLOR + ";color:#fff;text-decoration:none;font-weight:700;"
                + "padding:12px 20px;border-radius:12px;'>"
                + escapeHtml(text)
                + "</a></div>";
    }

    private String buildFooter(String footerHtml) {
        return "<div style='" + FOOTER_STYLE + "'>"
                + "<div style='margin-bottom:10px;'>" + footerHtml + "</div>"
                + "<div>Hỗ trợ: <a href='mailto:" + escapeHtml(AppConfig.APP_SUPPORT_EMAIL) + "' "
                + "style='color:" + AppConfig.APP_BRAND_COLOR + ";text-decoration:none;'>"
                + escapeHtml(AppConfig.APP_SUPPORT_EMAIL) + "</a></div>"
                + "</div>";
    }

    private String buildOtpBox(String verificationCode, String factsHtml) {
        return "<div style='text-align:center;margin:22px 0 18px;'>"
                + "<div style='display:inline-block;padding:14px 22px;border-radius:16px;"
                + "background:#eef8f1;border:1px solid #c8e2d0;'>"
                + "<div style='font-size:12px;letter-spacing:1.4px;text-transform:uppercase;"
                + "color:#5e7162;margin-bottom:6px;'>Mã xác minh của bạn</div>"
                + "<div style='font-size:34px;font-weight:800;letter-spacing:8px;color:"
                + AppConfig.APP_BRAND_COLOR + ";line-height:1.2;'>"
                + escapeHtml(verificationCode) + "</div>"
                + "</div></div>"
                + "<div style='background:#fbfdfb;border:1px solid #e2ede5;border-radius:14px;"
                + "padding:16px 18px;'>"
                + factsHtml
                + "</div>";
    }

    private String buildFactsTable(Map<String, String> facts) {
        StringBuilder sb = new StringBuilder(
                "<table role='presentation' style='width:100%;border-collapse:separate;"
                + "border-spacing:0 10px;margin-top:18px;'>");
        for (Map.Entry<String, String> entry : facts.entrySet()) {
            sb.append("<tr>")
              .append("<td style='padding:0;width:38%;font-size:13px;color:#607166;'>")
              .append(escapeHtml(entry.getKey()))
              .append("</td>")
              .append("<td style='padding:0;font-size:13px;color:#1f2937;font-weight:700;'>")
              .append(escapeHtml(entry.getValue()))
              .append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
