package com.fruitmkt.service;

import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * SystemConfigService — Quản lý cấu hình hệ thống (Platform fee, freeze days, etc.)
 */
public class SystemConfigService {

    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    public String getValue(String key) throws SQLException {
        return configDAO.getValue(key);
    }

    public double getDouble(String key, double defaultVal) {
        return configDAO.getDouble(key, defaultVal);
    }

    public int getInt(String key, int defaultVal) {
        return configDAO.getInt(key, defaultVal);
    }

    public List<Map<String, Object>> findAll() throws SQLException {
        return configDAO.findAll();
    }

    public List<Map<String, Object>> getHistory(String key, int limit) throws SQLException {
        return configDAO.getHistory(key, limit);
    }

    /**
     * Cập nhật cấu hình hệ thống và gửi email thông báo cho shop nếu có tăng phí / thay đổi quan trọng.
     */
    public void updateConfig(String key, String newValue, LocalDateTime effectiveDate, int changedBy, String reason) throws SQLException {
        if (effectiveDate == null) {
            effectiveDate = LocalDateTime.now();
        }
        if (effectiveDate.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Effective date cannot be in the past");
        }

        try (Connection conn = configDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                String oldValue = configDAO.getValue(key);
                configDAO.updateConfigWithHistory(conn, key, newValue, effectiveDate, changedBy, reason);
                conn.commit();

                // Nếu đổi platform_fee_rate hoặc freeze_days, gửi thông báo cho các Shop
                if ("platform_fee_rate".equalsIgnoreCase(key) || "freeze_days".equalsIgnoreCase(key)) {
                    notifyShopsOfFeeChange(key, oldValue, newValue, effectiveDate, reason);
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void notifyShopsOfFeeChange(String key, String oldValue, String newValue, LocalDateTime effectiveDate, String reason) {
        try {
            List<User> activeShops = userDAO.findActiveShopOwners();
            String configName = "platform_fee_rate".equalsIgnoreCase(key) ? "Tỷ lệ phí nền tảng (Platform Fee Rate)" : "Thời gian đóng băng tiền quyết toán (Freeze Days)";
            String unit = "platform_fee_rate".equalsIgnoreCase(key) ? "%" : " ngày";
            
            // Format values for display
            String oldValStr = oldValue != null ? oldValue : "N/A";
            String newValStr = newValue != null ? newValue : "N/A";
            if ("platform_fee_rate".equalsIgnoreCase(key)) {
                try {
                    oldValStr = String.valueOf(Double.parseDouble(oldValStr) * 100);
                    newValStr = String.valueOf(Double.parseDouble(newValStr) * 100);
                } catch (Exception ignored) {}
            }

            String dateStr = effectiveDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String subject = "[Thông báo Quan trọng] Thay đổi chính sách phí gian hàng " + configName;
            
            for (User shop : activeShops) {
                String emailBody = buildFeeChangeEmail(shop.getFullName(), configName, oldValStr, newValStr, unit, dateStr, reason);
                try {
                    emailService.sendHtml(shop.getEmail(), subject, emailBody);
                } catch (Exception e) {
                    // Log error and continue to other shops
                    System.err.println("Failed to send fee change email to shop " + shop.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to notify shops of fee change: " + e.getMessage());
        }
    }

    private String buildFeeChangeEmail(String shopName, String configName, String oldVal, String newVal, String unit, String effectiveDate, String reason) {
        return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>"
             + "  <h2 style='color: #d32f2f; border-bottom: 2px solid #d32f2f; padding-bottom: 10px;'>Thông Báo Thay Đổi Chính Sách Phí</h2>"
             + "  <p>Kính gửi quý chủ gian hàng <strong>" + shopName + "</strong>,</p>"
             + "  <p>Ban quản trị sàn <strong>FruitMarket</strong> xin thông báo về việc điều chỉnh cấu hình hệ thống liên quan đến phí và quyết toán cụ thể như sau:</p>"
             + "  <table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>"
             + "    <tr style='background-color: #f9f9f9;'>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Hạng mục thay đổi</td>"
             + "      <td style='padding: 10px; border: 1px solid #ddd;'>" + configName + "</td>"
             + "    </tr>"
             + "    <tr>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Giá trị cũ</td>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; color: #777; text-decoration: line-through;'>" + oldVal + unit + "</td>"
             + "    </tr>"
             + "    <tr style='background-color: #fffde7;'>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Giá trị mới áp dụng</td>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; color: #d32f2f; font-weight: bold;'>" + newVal + unit + "</td>"
             + "    </tr>"
             + "    <tr>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Thời gian có hiệu lực</td>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold; color: #388e3c;'>" + effectiveDate + "</td>"
             + "    </tr>"
             + "    <tr style='background-color: #f9f9f9;'>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>Lý do thay đổi</td>"
             + "      <td style='padding: 10px; border: 1px solid #ddd; font-style: italic;'>" + reason + "</td>"
             + "    </tr>"
             + "  </table>"
             + "  <p style='background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; border-radius: 4px;'>"
             + "    <strong>Lưu ý:</strong> Mọi đơn hàng được tạo trước thời điểm hiệu lực nêu trên sẽ áp dụng mức phí cũ. Các đơn hàng tạo từ thời điểm hiệu lực trở đi sẽ tự động áp dụng mức phí mới."
             + "  </p>"
             + "  <p>Nếu quý chủ gian hàng có bất kỳ thắc mắc nào, vui lòng liên hệ bộ phận hỗ trợ đối tác của FruitMarket để được giải đáp.</p>"
             + "  <p style='margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; font-size: 0.9em; color: #666;'>"
             + "    Trân trọng,<br/>"
             + "    <strong>Ban Quản Trị FruitMarket</strong>"
             + "  </p>"
             + "</div>";
    }
}
