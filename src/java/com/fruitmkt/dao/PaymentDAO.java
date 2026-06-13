package com.fruitmkt.dao;

import com.fruitmkt.dao.BaseDAO;
import com.fruitmkt.model.entity.PaymentTransaction;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.PaginationHelper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * PaymentDAO — DAO cho entity PaymentTransaction.
 *
 * QUY TẮC:
 *   - Chỉ chứa SQL, không chứa business logic
 *   - Dùng PreparedStatement, KHÔNG nối chuỗi SQL
 *   - Mỗi method ném SQLException để Service xử lý
 *   - Dùng try-with-resources cho Connection + PreparedStatement
 *
 * @author fruitmkt-team
 */
public class PaymentDAO extends BaseDAO {

    private static final Logger log = Logger.getLogger(PaymentDAO.class.getName());

    /**
     * Tạo mới bản ghi payment_transactions khi đơn CK được đặt.
     * Trả về transaction_id tự tăng.
     */
    public int initTransaction(int orderId, String paymentMethod,
                               BigDecimal amount, String sepayReference,
                               String ipAddress, LocalDateTime expiresAt) throws SQLException {
        String sql = "INSERT INTO payment_transactions "
                   + "(order_id, payment_method, sepay_reference, amount, currency, status, initiated_at, expires_at, ip_address) "
                   + "VALUES (?, ?, ?, ?, 'VND', 'pending', GETDATE(), ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setString(2, paymentMethod);
            ps.setString(3, sepayReference);
            ps.setBigDecimal(4, amount);
            ps.setTimestamp(5, expiresAt != null ? Timestamp.valueOf(expiresAt) : null);
            ps.setString(6, ipAddress);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("initTransaction: Không lấy được generated key.");
    }

    /**
     * Tìm payment transaction theo orderId.
     */
    public List<PaymentTransaction> findByOrder(int orderId) throws SQLException {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Tìm payment transaction theo SePay transaction ID (để dedup webhook).
     */
    public List<PaymentTransaction> findBySepayTxId(String txId) throws SQLException {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_transactions WHERE sepay_transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái transaction. providerResponse có thể null.
     */
    public void updateStatus(int transactionId, String status,
                             String sepayTransactionId, String providerResponse) throws SQLException {
        String sql = "UPDATE payment_transactions "
                   + "SET status = ?, sepay_transaction_id = ?, provider_response = ?, "
                   + "    completed_at = CASE WHEN ? = 'completed' THEN GETDATE() ELSE completed_at END, "
                   + "    updated_at = GETDATE() "
                   + "WHERE transaction_id = ?";
        // payment_transactions không có updated_at theo schema — dùng completed_at để log
        // Schema thực tế: (transaction_id, order_id, payment_method, sepay_transaction_id,
        //                   sepay_reference, sepay_qr_code, amount, currency, status,
        //                   initiated_at, completed_at, expires_at, provider_response,
        //                   error_code, error_message, ip_address)
        String sql2 = "UPDATE payment_transactions "
                    + "SET status = ?, sepay_transaction_id = ?, provider_response = ?, "
                    + "    completed_at = CASE WHEN ? = 'completed' THEN GETDATE() ELSE NULL END "
                    + "WHERE transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setString(1, status);
            ps.setString(2, sepayTransactionId);
            ps.setString(3, providerResponse);
            ps.setString(4, status);
            ps.setInt(5, transactionId);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái đơn giản (không cần sepay_transaction_id).
     */
    public void updateStatus(int transactionId, String status) throws SQLException {
        updateStatus(transactionId, status, null, null);
    }

    /**
     * Cập nhật status + ghi lỗi khi payment fail.
     */
    public void updateStatusFailed(int transactionId, String errorCode, String errorMessage) throws SQLException {
        String sql = "UPDATE payment_transactions "
                   + "SET status = 'failed', error_code = ?, error_message = ? "
                   + "WHERE transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, errorCode);
            ps.setString(2, errorMessage);
            ps.setInt(3, transactionId);
            ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra sepay_transaction_id đã được xử lý chưa (dedup webhook).
     * Dùng bảng sepay_webhook_dedup.
     */
    public boolean isDuplicate(String sepayTxId) throws SQLException {
        String sql = "SELECT 1 FROM sepay_webhook_dedup WHERE sepay_transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sepayTxId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Ghi vào bảng dedup để đảm bảo idempotency.
     * Nếu đã tồn tại (UNIQUE constraint) sẽ throw SQLException — caller bắt để bỏ qua.
     */
    public void insertDedup(String sepayTxId, String orderCode, String processResult) throws SQLException {
        String sql = "INSERT INTO sepay_webhook_dedup (sepay_transaction_id, order_code, process_result) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sepayTxId);
            ps.setString(2, orderCode);
            ps.setString(3, processResult);
            ps.executeUpdate();
        }
    }

    /**
     * Mở kết nối public — dùng khi Service cần transaction thủ công (VD: renewQr).
     */
    public java.sql.Connection openConnection() throws SQLException {
        return getConnection();
    }

    /**
     * Lấy transaction theo orderId và sepay_reference (để match webhook).
     */
    public PaymentTransaction findByReference(String sepayReference) throws SQLException {
        String sql = "SELECT * FROM payment_transactions WHERE sepay_reference = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sepayReference);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Ánh xạ ResultSet → PaymentTransaction */
    private PaymentTransaction mapRow(ResultSet rs) throws SQLException {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setOrderId(rs.getInt("order_id"));
        tx.setPaymentMethod(rs.getString("payment_method"));
        tx.setSepayTransactionId(rs.getString("sepay_transaction_id"));
        tx.setSepayReference(rs.getString("sepay_reference"));
        tx.setSepayQrCode(rs.getString("sepay_qr_code"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setCurrency(rs.getString("currency"));
        tx.setStatus(rs.getString("status"));

        Timestamp initiated = rs.getTimestamp("initiated_at");
        if (initiated != null) tx.setInitiatedAt(initiated.toLocalDateTime());

        Timestamp completed = rs.getTimestamp("completed_at");
        if (completed != null) tx.setCompletedAt(completed.toLocalDateTime());

        Timestamp expires = rs.getTimestamp("expires_at");
        if (expires != null) tx.setExpiresAt(expires.toLocalDateTime());

        tx.setProviderResponse(rs.getString("provider_response"));
        tx.setErrorCode(rs.getString("error_code"));
        tx.setErrorMessage(rs.getString("error_message"));
        tx.setIpAddress(rs.getString("ip_address"));
        return tx;
    }

    /**
     * Tìm tất cả giao dịch thanh toán của khách hàng.
     */
    public List<PaymentTransaction> findByCustomer(int customerId) throws SQLException {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT pt.* FROM payment_transactions pt "
                   + "JOIN orders o ON pt.order_id = o.order_id "
                   + "WHERE o.customer_id = ? "
                   + "ORDER BY pt.initiated_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> findAdminPayments(String status, String paymentMethod, String keyword,
                                                       int page, int pageSize) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT pt.transaction_id, pt.order_id, pt.payment_method, pt.sepay_transaction_id, pt.sepay_reference, " +
                "       pt.sepay_qr_code, pt.amount, pt.currency, pt.status AS payment_status, " +
                "       pt.initiated_at, pt.completed_at, pt.expires_at, pt.provider_response, pt.error_code, pt.error_message, " +
                "       pt.ip_address, o.status AS order_status, o.payment_method AS order_payment_method, " +
                "       c.full_name AS customer_name, COALESCE(sop.shop_name, N'') AS shop_name " +
                "FROM payment_transactions pt " +
                "JOIN orders o ON pt.order_id = o.order_id " +
                "JOIN users c ON o.customer_id = c.user_id " +
                "LEFT JOIN shop_owner_profiles sop ON o.owner_id = sop.user_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND pt.status = ? ");
            params.add(status.trim());
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append("AND pt.payment_method = ? ");
            params.add(paymentMethod.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (CAST(pt.transaction_id AS NVARCHAR(20)) LIKE ? "
                    + "OR CAST(pt.order_id AS NVARCHAR(20)) LIKE ? "
                    + "OR pt.sepay_reference LIKE ? "
                    + "OR ISNULL(pt.sepay_transaction_id, '') LIKE ? "
                    + "OR c.full_name LIKE ? "
                    + "OR ISNULL(sop.shop_name, '') LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("ORDER BY pt.initiated_at DESC ").append(PaginationHelper.OFFSET_FETCH_SQL);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }
            paramIndex = PaginationHelper.bindOffsetFetch(ps, paramIndex, page, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("transactionId", rs.getInt("transaction_id"));
                    row.put("orderId", rs.getInt("order_id"));
                    row.put("paymentMethod", rs.getString("payment_method"));
                    row.put("sepayTransactionId", rs.getString("sepay_transaction_id"));
                    row.put("sepayReference", rs.getString("sepay_reference"));
                    row.put("sepayQrCode", rs.getString("sepay_qr_code"));
                    row.put("amount", rs.getBigDecimal("amount"));
                    row.put("currency", rs.getString("currency"));
                    row.put("paymentStatus", rs.getString("payment_status"));
                    row.put("orderStatus", rs.getString("order_status"));
                    row.put("orderPaymentMethod", rs.getString("order_payment_method"));
                    row.put("customerName", rs.getString("customer_name"));
                    row.put("shopName", rs.getString("shop_name"));
                    row.put("initiatedAt", rs.getTimestamp("initiated_at"));
                    row.put("completedAt", rs.getTimestamp("completed_at"));
                    row.put("expiresAt", rs.getTimestamp("expires_at"));
                    row.put("providerResponse", rs.getString("provider_response"));
                    row.put("errorCode", rs.getString("error_code"));
                    row.put("errorMessage", rs.getString("error_message"));
                    row.put("ipAddress", rs.getString("ip_address"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public int countAdminPayments(String status, String paymentMethod, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM payment_transactions pt " +
                "JOIN orders o ON pt.order_id = o.order_id " +
                "JOIN users c ON o.customer_id = c.user_id " +
                "LEFT JOIN shop_owner_profiles sop ON o.owner_id = sop.user_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND pt.status = ? ");
            params.add(status.trim());
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append("AND pt.payment_method = ? ");
            params.add(paymentMethod.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (CAST(pt.transaction_id AS NVARCHAR(20)) LIKE ? "
                    + "OR CAST(pt.order_id AS NVARCHAR(20)) LIKE ? "
                    + "OR pt.sepay_reference LIKE ? "
                    + "OR ISNULL(pt.sepay_transaction_id, '') LIKE ? "
                    + "OR c.full_name LIKE ? "
                    + "OR ISNULL(sop.shop_name, '') LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
