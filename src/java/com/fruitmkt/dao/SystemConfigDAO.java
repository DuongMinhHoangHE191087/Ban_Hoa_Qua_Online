package com.fruitmkt.dao;

import com.fruitmkt.dao.BaseDAO;
import com.fruitmkt.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SystemConfigDAO — Đọc/ghi bảng system_config.
 *
 * Bảng system_config dùng pattern key-value để lưu cấu hình toàn hệ thống.
 * Khi admin thay đổi: ghi history, cập nhật effective_date, gửi email cho shop.
 *
 * @author fruitmkt-team
 */
public class SystemConfigDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(SystemConfigDAO.class.getName());

    /**
     * Lấy giá trị config theo key. Trả null nếu không tồn tại.
     */
    public String getValue(String key) throws SQLException {
        String sql = "SELECT config_value FROM system_config WHERE config_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("config_value");
            }
        }
        return null;
    }

    /**
     * Lấy giá trị DECIMAL config, fallback về defaultVal nếu không có.
     */
    public double getDouble(String key, double defaultVal) {
        try {
            String v = getValue(key);
            return v != null ? Double.parseDouble(v) : defaultVal;
        } catch (Exception e) { return defaultVal; }
    }

    /**
     * Lấy giá trị INT config, fallback về defaultVal nếu không có.
     */
    public int getInt(String key, int defaultVal) {
        try {
            String v = getValue(key);
            return v != null ? Integer.parseInt(v) : defaultVal;
        } catch (Exception e) { return defaultVal; }
    }

    /**
     * Cập nhật config value + ghi history + đặt effective_date.
     * Gọi trong một transaction — caller truyền conn.
     *
     * @param conn        Connection đang dùng (caller quản lý)
     * @param key         config_key
     * @param newValue    giá trị mới
     * @param effectiveDate  ngày hiệu lực (không được là quá khứ)
     * @param changedBy   admin user_id
     * @param reason      lý do thay đổi
     */
    public void updateConfigWithHistory(Connection conn, String key, String newValue,
                                        LocalDateTime effectiveDate, int changedBy,
                                        String reason) throws SQLException {
        // Lấy giá trị cũ
        String oldValue = null;
        String selectSql = "SELECT config_value FROM system_config WHERE config_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) oldValue = rs.getString("config_value");
            }
        }

        // Cập nhật system_config
        String updateSql = "UPDATE system_config "
                         + "SET config_value = ?, previous_value = ?, effective_date = ?, "
                         + "    changed_by = ?, changed_at = GETDATE(), updated_at = GETDATE() "
                         + "WHERE config_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, newValue);
            ps.setString(2, oldValue);
            ps.setTimestamp(3, Timestamp.valueOf(effectiveDate));
            ps.setInt(4, changedBy);
            ps.setString(5, key);
            ps.executeUpdate();
        }

        // Log configuration change instead of system_config_history table
        LoggerUtil.info(log, String.format("[SystemConfig] CONFIG CHANGE: Key: %s, Old Value: %s, New Value: %s, Effective Date: %s, Changed By: %d, Reason: %s",
                key, oldValue, newValue, effectiveDate, changedBy, reason));
    }

    /** Lấy lịch sử thay đổi của một config key (trả về danh sách rỗng vì không dùng bảng history). */
    public List<Map<String, Object>> getHistory(String key, int limit) throws SQLException {
        return new ArrayList<>();
    }

    /** Lấy tất cả config để hiển thị admin UI. */
    public List<Map<String, Object>> findAll() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name AS admin_name "
                   + "FROM system_config c "
                   + "LEFT JOIN users u ON u.user_id = c.changed_by "
                   + "ORDER BY c.config_key";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
        }
        return list;
    }

    /** Mở connection public cho Service dùng transaction thủ công. */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }
}
