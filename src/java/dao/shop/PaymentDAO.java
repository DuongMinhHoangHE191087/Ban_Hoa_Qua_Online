package dao.shop;

import dao.system.BaseDAO;
import model.entity.shop.PaymentTransaction;
import util.PaginationHelper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Set;
import util.LoggerUtil;

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
     * Tìm payment transaction duy nhất theo orderId.
     */
    public PaymentTransaction findOneByOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm payment transaction duy nhất theo orderId trên connection hiện tại.
     */
    public PaymentTransaction findOneByOrder(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT * FROM payment_transactions WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Batch load payment transaction theo danh sách orderId.
     */
    public Map<Integer, PaymentTransaction> findByOrderIds(Collection<Integer> orderIds) throws SQLException {
        Map<Integer, PaymentTransaction> map = new LinkedHashMap<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return map;
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(orderIds);
        StringBuilder placeholders = new StringBuilder();
        int size = distinctIds.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "SELECT * FROM payment_transactions WHERE order_id IN (" + placeholders + ")";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Integer orderId : distinctIds) {
                ps.setInt(paramIndex++, orderId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentTransaction tx = mapRow(rs);
                    map.put(tx.getOrderId(), tx);
                }
            }
        }
        return map;
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
     * Lấy các orderId có payment completed nhưng tree đơn vẫn còn lệch để reconcile nền.
     */
    public List<Integer> findCompletedOrderIdsForReconciliation() throws SQLException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT o.order_id "
                   + "FROM orders o "
                   + "JOIN payment_transactions pt ON pt.order_id = o.order_id "
                   + "WHERE pt.status = 'completed' "
                   + "  AND (o.status = 'PENDING_PAYMENT' "
                   + "       OR EXISTS (SELECT 1 FROM orders c "
                   + "                  WHERE c.parent_order_id = o.order_id "
                   + "                    AND c.status <> 'CONFIRMED')) "
                   + "ORDER BY o.order_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái transaction. providerResponse có thể null.
     */
    public void updateStatus(int transactionId, String status,
                             String sepayTransactionId, String providerResponse) throws SQLException {
        try (Connection conn = getConnection()) {
            updateStatus(conn, transactionId, status, sepayTransactionId, providerResponse);
        }
    }

    /**
     * Cập nhật trạng thái transaction trên connection hiện tại.
     */
    public void updateStatus(Connection conn, int transactionId, String status,
                             String sepayTransactionId, String providerResponse) throws SQLException {
        String normalizedStatus = status != null ? status.trim() : null;
        String sql2;
        if ("processing".equalsIgnoreCase(normalizedStatus)) {
            sql2 = "UPDATE payment_transactions "
                 + "SET status = ?, completed_at = NULL "
                 + "WHERE transaction_id = ? AND status = 'pending'";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, normalizedStatus);
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }
            return;
        }

        if ("completed".equalsIgnoreCase(normalizedStatus)) {
            sql2 = "UPDATE payment_transactions "
                 + "SET status = ?, sepay_transaction_id = ?, provider_response = ?, "
                 + "    completed_at = CASE WHEN status <> 'completed' THEN GETDATE() ELSE completed_at END "
                 + "WHERE transaction_id = ? AND status IN ('pending', 'processing')";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, normalizedStatus);
                ps.setString(2, sepayTransactionId);
                ps.setString(3, providerResponse);
                ps.setInt(4, transactionId);
                ps.executeUpdate();
            }
            return;
        }

        sql2 = "UPDATE payment_transactions "
             + "SET status = ? "
             + "WHERE transaction_id = ? AND status <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setString(1, normalizedStatus);
            ps.setInt(2, transactionId);
            ps.setString(3, normalizedStatus);
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
        try (Connection conn = getConnection()) {
            updateStatusFailed(conn, transactionId, errorCode, errorMessage);
        }
    }

    /**
     * Cập nhật trạng thái failed trên connection hiện tại.
     */
    public void updateStatusFailed(Connection conn, int transactionId, String errorCode, String errorMessage) throws SQLException {
        String sql = "UPDATE payment_transactions "
                   + "SET status = 'failed', error_code = ?, error_message = ? "
                   + "WHERE transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
     * Nếu race condition xảy ra (hai webhook cùng lúc vượt qua isDuplicate()),
     * UNIQUE constraint sẽ chặn INSERT thứ hai — bắt error 2627 và bỏ qua an toàn.
     */
    public void insertDedup(String sepayTxId, String orderCode, String processResult) throws SQLException {
        try (Connection conn = getConnection()) {
            insertDedup(conn, sepayTxId, orderCode, processResult);
        }
    }

    /**
     * Ghi vào bảng dedup trên connection hiện tại để đảm bảo idempotency.
     * Trả về false nếu sepayTxId đã tồn tại.
     */
    public boolean insertDedup(Connection conn, String sepayTxId, String orderCode, String processResult) throws SQLException {
        String sql = "INSERT INTO sepay_webhook_dedup (sepay_transaction_id, order_code, process_result) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sepayTxId);
            ps.setString(2, orderCode);
            ps.setString(3, processResult);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // SQL Server error 2627 = UNIQUE KEY constraint violation
            // Xảy ra khi hai webhook đồng thời pass isDuplicate() trước khi cái nào insert xong.
            // Coi như "đã xử lý" — bỏ qua an toàn, không throw.
            if (e.getErrorCode() == 2627) {
                LoggerUtil.info(log, "[Dedup] Race condition caught: sepayTxId=%s đã tồn tại — bỏ qua.", sepayTxId);
                return false;
            }
            throw e;
        }
    }

    /**
     * Cập nhật kết quả xử lý dedup trên connection hiện tại.
     */
    public void updateDedupResult(Connection conn, String sepayTxId, String processResult) throws SQLException {
        String sql = "UPDATE sepay_webhook_dedup SET process_result = ? WHERE sepay_transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, processResult);
            ps.setString(2, sepayTxId);
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
        try (Connection conn = getConnection()) {
            return findByReference(conn, sepayReference);
        }
    }

    /**
     * Lấy transaction theo orderId và sepay_reference trên connection hiện tại.
     */
    public PaymentTransaction findByReference(Connection conn, String sepayReference) throws SQLException {
        String sql = "SELECT * FROM payment_transactions WHERE sepay_reference = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

    /**
     * Tạo bản ghi payment_transaction cho đơn COD khi shipper xác nhận giao thành công.
     * Đây là thời điểm tiền mặt được thu — ghi nhận là completed ngay lập tức.
     *
     * @param conn   Connection trong transaction của DeliveryService
     * @param orderId  ID đơn hàng (đơn con CHILD)
     * @param amount   Số tiền thu được (final_amount của đơn)
     * @param note     Ghi chú (VD: "Shipper xác nhận thu tiền COD đơn #123")
     */
    public void initCodTransaction(Connection conn, int orderId, BigDecimal amount, String note) throws SQLException {
        String sql = "INSERT INTO payment_transactions "
                   + "(order_id, payment_method, amount, currency, status, initiated_at, completed_at, provider_response) "
                   + "VALUES (?, 'COD', ?, 'VND', 'completed', GETDATE(), GETDATE(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount != null ? amount : BigDecimal.ZERO);
            ps.setString(3, note);
            ps.executeUpdate();
        }
    }
}
