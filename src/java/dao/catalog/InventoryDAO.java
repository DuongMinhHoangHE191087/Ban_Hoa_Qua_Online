package dao.catalog;

import dao.system.BaseDAO;
import model.entity.catalog.InventoryLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryDAO — DAO cho entity InventoryLog.
 *
 * QUY TẮC:
 * - Chỉ chứa SQL, không chứa business logic
 * - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 * - Mỗi method ném SQLException để Service xử lý
 * - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class InventoryDAO extends BaseDAO {

    /**
     * Exposes connection for starting standard JDBC transactions in Service.
     */
    public Connection openConnection() throws SQLException {
        return getConnection();
    }

    /**
     * Saves an inventory log entry using an active transactional connection.
     */
    public int save(Connection conn, InventoryLog log) throws SQLException {
        String sql = "INSERT INTO inventory_logs (variant_id, changed_by, change_type, quantity_delta, quantity_after, note, expires_at, is_expired, changed_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getVariantId());
            ps.setInt(2, log.getChangedBy());
            ps.setString(3, log.getChangeType());
            ps.setInt(4, log.getQuantityDelta());
            ps.setInt(5, log.getQuantityAfter());
            if (log.getNote() != null) {
                ps.setString(6, log.getNote());
            } else {
                ps.setNull(6, Types.NVARCHAR);
            }
            if (log.getExpiresAt() != null) {
                ps.setDate(7, java.sql.Date.valueOf(log.getExpiresAt()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setBoolean(8, log.isExpired());
            if (log.getChangedAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(log.getChangedAt()));
            } else {
                ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Lưu lịch sử thay đổi kho thất bại, không lấy được mã tự tăng.");
    }

    /**
     * Saves an inventory log entry using a fresh connection.
     */
    public int save(InventoryLog log) throws SQLException {
        try (Connection conn = getConnection()) {
            return save(conn, log);
        }
    }

    /**
     * Retrieves all inventory history logs for products owned by a specific Shop
     * Owner.
     */
    public List<InventoryLog> findByOwner(int ownerId) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT il.*, p.name AS product_name, pv.variant_label, u.full_name AS changed_by_name "
                + "FROM inventory_logs il "
                + "JOIN product_variants pv ON il.variant_id = pv.variant_id "
                + "JOIN products p ON pv.product_id = p.product_id "
                + "JOIN users u ON il.changed_by = u.user_id "
                + "WHERE p.owner_id = ? "
                + "ORDER BY il.changed_at DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all inventory history logs for a specific Product Variant.
     */
    public List<InventoryLog> findByVariant(int variantId) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT il.*, p.name AS product_name, pv.variant_label, u.full_name AS changed_by_name "
                + "FROM inventory_logs il "
                + "JOIN product_variants pv ON il.variant_id = pv.variant_id "
                + "JOIN products p ON pv.product_id = p.product_id "
                + "JOIN users u ON il.changed_by = u.user_id "
                + "WHERE il.variant_id = ? "
                + "ORDER BY il.changed_at DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /** Ánh xạ ResultSet -> InventoryLog */
    private InventoryLog mapRow(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog();
        log.setLogId(rs.getInt("log_id"));
        log.setVariantId(rs.getInt("variant_id"));
        log.setChangedBy(rs.getInt("changed_by"));
        log.setChangeType(rs.getString("change_type"));
        log.setQuantityDelta(rs.getInt("quantity_delta"));
        log.setQuantityAfter(rs.getInt("quantity_after"));
        log.setNote(rs.getString("note"));

        java.sql.Date expDate = rs.getDate("expires_at");
        if (expDate != null) {
            log.setExpiresAt(expDate.toLocalDate());
        }
        log.setExpired(rs.getBoolean("is_expired"));

        Timestamp ts = rs.getTimestamp("changed_at");
        if (ts != null) {
            log.setChangedAt(ts.toLocalDateTime());
        }

        return log;
    }

    /** Ánh xạ ResultSet -> InventoryLog kèm thông tin chi tiết liên kết */
    private InventoryLog mapRowWithDetails(ResultSet rs) throws SQLException {
        InventoryLog log = mapRow(rs);
        log.setProductName(rs.getString("product_name"));
        log.setVariantLabel(rs.getString("variant_label"));
        log.setChangedByName(rs.getString("changed_by_name"));
        return log;
    }

    /**
     * Tìm tất cả các dòng giao dịch nhập kho (MANUAL_ADJUST và delta > 0) có ngày
     * hết hạn đã qua
     * nhưng chưa được xử lý hết hạn (is_expired = 0).
     */
    public List<InventoryLog> findExpiredLogs(Connection conn) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_logs WHERE change_type = 'MANUAL_ADJUST' AND quantity_delta > 0 "
                + "AND expires_at IS NOT NULL AND expires_at <= CAST(GETDATE() AS DATE) AND is_expired = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Lấy danh sách lô hàng nhập kho còn hiệu lực (MANUAL_ADJUST, delta > 0, is_expired = 0)
     * kèm thông tin sản phẩm, phân loại và ngày hết hạn — dùng cho bảng Tồn kho của Shop Owner.
     */
    public List<InventoryLog> findActiveBatchesByOwner(int ownerId) throws SQLException {
        List<InventoryLog> list = new ArrayList<>();
        String sql = "SELECT il.log_id, il.variant_id, il.quantity_delta, il.quantity_after, "
                + "       il.expires_at, il.is_expired, il.changed_at, il.note, il.changed_by, "
                + "       p.name AS product_name, pv.variant_label, u.full_name AS changed_by_name "
                + "FROM inventory_logs il "
                + "JOIN product_variants pv ON il.variant_id = pv.variant_id "
                + "JOIN products p ON pv.product_id = p.product_id "
                + "JOIN users u ON il.changed_by = u.user_id "
                + "WHERE p.owner_id = ? "
                + "  AND il.change_type = 'MANUAL_ADJUST' "
                + "  AND il.quantity_delta > 0 "
                + "  AND il.is_expired = 0 "
                + "ORDER BY il.expires_at ASC, il.changed_at DESC";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Đánh dấu log nhập kho đã được xử lý hết hạn.
     */
    public void markLogExpired(Connection conn, int logId) throws SQLException {
        String sql = "UPDATE inventory_logs SET is_expired = 1 WHERE log_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
            ps.executeUpdate();
        }
    }
}
