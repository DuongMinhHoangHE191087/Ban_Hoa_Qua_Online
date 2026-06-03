package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.PaymentDAO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PaymentService — Business logic cho thanh toán.
 *
 * Luồng chính:
 *   1. CK: initPayment() → tạo payment_transactions
 *   2. Customer bấm "Tôi đã thanh toán" → confirmManualPayment()
 *   3. Admin xác nhận → adminApprovePayment()
 *   4. Tương lai: SePay webhook → processWebhook()
 *
 * @author fruitmkt-team
 */
public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderDAO   orderDAO   = new OrderDAO();

    private static final int QR_EXPIRE_MIN = 15;

    /**
     * Khởi tạo bản ghi payment_transaction cho đơn CK.
     * Gọi ngay sau khi INSERT orders thành công.
     */
    public PaymentTransaction initPayment(int orderId, String method) throws SQLException {
        List<Order> orders = orderDAO.findById(orderId);
        if (orders.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        Order order = orders.get(0);

        String reference = "MF" + orderId;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(QR_EXPIRE_MIN);

        int txId = paymentDAO.initTransaction(
            orderId, method, order.getFinalAmount(),
            reference, null, expiresAt
        );

        PaymentTransaction tx = new PaymentTransaction();
        tx.setTransactionId(txId);
        tx.setOrderId(orderId);
        tx.setPaymentMethod(method);
        tx.setAmount(order.getFinalAmount());
        tx.setSepayReference(reference);
        tx.setStatus("pending");
        tx.setExpiresAt(expiresAt);
        return tx;
    }

    /**
     * Lấy thông tin payment transaction của đơn hàng.
     */
    public PaymentTransaction getPaymentByOrder(int orderId) throws SQLException {
        List<PaymentTransaction> list = paymentDAO.findByOrder(orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Khách bấm "Tôi đã thanh toán" — chuyển status → processing.
     * Thông báo admin cần xác minh thủ công.
     * @return false nếu QR đã hết hạn
     */
    public boolean confirmManualPayment(int orderId, int customerId) throws SQLException {
        // Guard: đơn phải thuộc customer này
        Order order = orderDAO.findByIdForCustomer(orderId, customerId);
        if (order == null) throw new SecurityException("Không có quyền truy cập đơn hàng #" + orderId);
        if (!AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán.");
        }

        PaymentTransaction tx = getPaymentByOrder(orderId);
        if (tx == null) {
            throw new IllegalStateException("Không tìm thấy bản ghi thanh toán cho đơn #" + orderId);
        }
        // Kiểm tra QR còn hạn không
        if (tx.getExpiresAt() != null && LocalDateTime.now().isAfter(tx.getExpiresAt())) {
            return false; // QR hết hạn
        }
        paymentDAO.updateStatus(tx.getTransactionId(), "processing");
        // Thông báo admin (NotificationService sẽ được mở rộng sau)
        System.out.println("[PaymentService] Customer #" + customerId
            + " báo đã CK cho đơn #" + orderId + ". Cần admin xác nhận.");
        return true;
    }

    /**
     * Admin xác nhận đã nhận tiền — chuyển payment → completed, order → CONFIRMED.
     */
    public void adminApprovePayment(int orderId, int adminId) throws SQLException {
        com.fruitmkt.dao.UserDAO userDAO = new com.fruitmkt.dao.UserDAO();
        com.fruitmkt.model.entity.User adminUser = userDAO.findUserById(adminId);
        if (adminUser == null || !AppConfig.ROLE_ADMIN.equals(adminUser.getRole())) {
            throw new SecurityException("Bạn không có quyền thực hiện phê duyệt thanh toán này.");
        }

        List<Order> orders = orderDAO.findById(orderId);
        if (orders.isEmpty()) throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        Order order = orders.get(0);

        if (!AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán.");
        }

        PaymentTransaction tx = getPaymentByOrder(orderId);
        if (tx == null) throw new IllegalStateException("Không tìm thấy bản ghi thanh toán.");

        // Cập nhật payment → completed
        paymentDAO.updateStatus(tx.getTransactionId(), "completed",
                                "ADMIN_MANUAL_" + adminId, null);
        // Cập nhật order → CONFIRMED
        orderDAO.updateStatus(orderId, AppConfig.ORDER_CONFIRMED);
        System.out.println("[PaymentService] Admin #" + adminId
            + " xác nhận thanh toán đơn #" + orderId);
    }

    /**
     * Làm mới QR (gia hạn thêm 15 phút).
     * Chỉ cho phép nếu đơn vẫn ở PENDING_PAYMENT và QR thực sự đã hết hạn.
     */
    public PaymentTransaction renewQr(int orderId, int customerId) throws SQLException {
        Order order = orderDAO.findByIdForCustomer(orderId, customerId);
        if (order == null) throw new SecurityException("Không có quyền truy cập đơn hàng #" + orderId);
        if (!AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new IllegalStateException("Không thể làm mới QR cho đơn không ở trạng thái chờ thanh toán.");
        }
        PaymentTransaction tx = getPaymentByOrder(orderId);
        if (tx == null) {
            // Chưa có → tạo mới
            return initPayment(orderId, "SEPAY");
        }
        // Gia hạn expires_at
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(QR_EXPIRE_MIN);
        String updateSql = "UPDATE payment_transactions SET expires_at = ?, status = 'pending' "
                         + "WHERE transaction_id = ?";
        try (java.sql.Connection conn = paymentDAO.openConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(newExpiry));
            ps.setInt(2, tx.getTransactionId());
            ps.executeUpdate();
        }
        tx.setExpiresAt(newExpiry);
        tx.setStatus("pending");
        return tx;
    }

    /**
     * Xử lý SePay Webhook — implement đầy đủ cho production.
     *
     * Payload SePay (relevant fields):
     *   id            → sepay_transaction_id
     *   code          → sepay_reference (VD: "MF101")
     *   transferAmount → số tiền thực nhận
     *   transferType  → "in" = tiền vào, "out" = tiền ra
     *
     * @param jsonPayload raw JSON body từ SePay
     */
    public void processWebhook(String jsonPayload) throws SQLException {
        // Parse JSON thủ công (không dùng lib phụ để tránh dependency)
        String sepayTxId    = extractJsonString(jsonPayload, "id");
        String code         = extractJsonString(jsonPayload, "code");
        String transferType = extractJsonString(jsonPayload, "transferType");
        String amountStr    = extractJsonString(jsonPayload, "transferAmount");

        if (sepayTxId == null || code == null) {
            System.err.println("[Webhook] Payload thiếu trường bắt buộc: " + jsonPayload);
            return;
        }

        // Dedup: nếu đã xử lý rồi thì bỏ qua
        if (paymentDAO.isDuplicate(sepayTxId)) {
            System.out.println("[Webhook] Duplicate sepay_tx_id=" + sepayTxId + " — bỏ qua.");
            return;
        }

        // Chỉ xử lý tiền vào
        if (!"in".equalsIgnoreCase(transferType)) {
            paymentDAO.insertDedup(sepayTxId, code, "skipped_not_in");
            return;
        }

        // Tìm payment_transaction theo reference
        PaymentTransaction tx = paymentDAO.findByReference(code);
        if (tx == null) {
            System.err.println("[Webhook] Không tìm thấy payment_transaction với reference=" + code);
            paymentDAO.insertDedup(sepayTxId, code, "not_found");
            return;
        }

        // Validate số tiền
        BigDecimal received = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
        if (received.compareTo(tx.getAmount()) < 0) {
            paymentDAO.updateStatus(tx.getTransactionId(), "failed",
                                    sepayTxId, jsonPayload);
            paymentDAO.updateStatusFailed(tx.getTransactionId(),
                                          "AMOUNT_MISMATCH",
                                          "Nhận " + received + " < yêu cầu " + tx.getAmount());
            paymentDAO.insertDedup(sepayTxId, code, "amount_mismatch");
            System.err.println("[Webhook] Số tiền không khớp: expected=" + tx.getAmount()
                + " received=" + received + " orderId=" + tx.getOrderId());
            return;
        }

        // Cập nhật payment → completed
        paymentDAO.updateStatus(tx.getTransactionId(), "completed", sepayTxId, jsonPayload);
        // Cập nhật order → CONFIRMED
        orderDAO.updateStatus(tx.getOrderId(), AppConfig.ORDER_CONFIRMED);
        // Ghi dedup
        paymentDAO.insertDedup(sepayTxId, code, "processed");
        System.out.println("[Webhook] Thanh toán thành công orderId=" + tx.getOrderId()
            + " sepayTxId=" + sepayTxId);
    }

    // ─── Helper: parse giá trị từ JSON string đơn giản ─────────────────────
    // Dùng regex-free manual parse để tránh phụ thuộc library
    private String extractJsonString(String json, String key) {
        if (json == null) return null;
        // Tìm "key": value hoặc "key":"value"
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        // Bỏ qua khoảng trắng
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            // String value
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        } else {
            // Number / boolean / null
            int end = start;
            while (end < json.length() && ",}]\n\r ".indexOf(json.charAt(end)) < 0) end++;
            String val = json.substring(start, end).trim();
            return "null".equals(val) ? null : val;
        }
    }

    /** Mở kết nối dùng cho renewQr (package-private để test). */
    java.sql.Connection openConnection() throws SQLException {
        return orderDAO.openConnection();
    }
}
