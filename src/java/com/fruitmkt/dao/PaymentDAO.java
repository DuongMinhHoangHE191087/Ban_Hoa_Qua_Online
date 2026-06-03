package com.fruitmkt.dao;

import com.fruitmkt.dao.base.BaseDAO;
import com.fruitmkt.model.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

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
}
