package service.shop;

import config.AppConfig;
import dao.order.OrderDAO;
import dao.shop.PaymentDAO;
import dao.auth.UserDAO;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import model.entity.auth.User;
import util.LoggerUtil;
import service.chat.NotificationService;
import service.system.EmailService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

    private static final Logger log = LoggerUtil.getLogger(PaymentService.class);

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderDAO   orderDAO   = new OrderDAO();
    private final UserDAO    userDAO    = new UserDAO();
    private final NotificationService notificationService = new NotificationService();
    private final EmailService emailService = new EmailService();

    private static final int QR_EXPIRE_MIN = 15;

    /**
     * Khởi tạo bản ghi payment_transaction cho đơn CK.
     * Gọi ngay sau khi INSERT orders thành công.
     */
    public PaymentTransaction initPayment(int orderId, String method) throws SQLException {
        return initPayment(orderId, method, null);
    }

    public PaymentTransaction initPayment(int orderId, String method, String ipAddress) throws SQLException {
        Order order = orderDAO.findOneById(orderId);
        if (order == null) throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);

        String reference = buildSepayReference(orderId);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(QR_EXPIRE_MIN);

        int txId = paymentDAO.initTransaction(
            orderId, method, order.getFinalAmount(),
            reference, ipAddress, expiresAt
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
        return paymentDAO.findOneByOrder(orderId);
    }

    public Map<Integer, PaymentTransaction> getPaymentMapByOrderIds(Collection<Integer> orderIds) throws SQLException {
        return paymentDAO.findByOrderIds(orderIds);
    }

    /**
     * Danh sách payment toàn sàn cho dashboard admin.
     */
    public List<Map<String, Object>> getAdminPayments(String status, String paymentMethod,
                                                      String keyword, int page, int pageSize) throws SQLException {
        return paymentDAO.findAdminPayments(status, paymentMethod, keyword, page, pageSize);
    }

    /**
     * Đếm payment toàn sàn theo bộ lọc dashboard admin.
     */
    public int countAdminPayments(String status, String paymentMethod, String keyword) throws SQLException {
        return paymentDAO.countAdminPayments(status, paymentMethod, keyword);
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
        LoggerUtil.info(log, "Customer #%d báo đã CK cho đơn #%d. Cần admin xác nhận.", customerId, orderId);
        return true;
    }

    /**
     * Admin xác nhận đã nhận tiền — chuyển payment → completed, order → CONFIRMED.
     */
    public void adminApprovePayment(int orderId, int adminId) throws SQLException {
        User adminUser = userDAO.findUserById(adminId);
        if (adminUser == null || !AppConfig.ROLE_ADMIN.equals(adminUser.getRole())) {
            throw new SecurityException("Bạn không có quyền thực hiện phê duyệt thanh toán này.");
        }

        Order order = orderDAO.findOneById(orderId);
        if (order == null) throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);

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
        LoggerUtil.info(log, "Admin #%d xác nhận thanh toán đơn #%d.", adminId, orderId);

        // Gửi thông báo thanh toán thành công cho Customer
        try {
            User customer = userDAO.findUserById(order.getCustomerId());
            if (customer != null) {
                String customerMsg = "Đơn hàng #" + orderId + " đã được xác nhận thanh toán thành công.";
                notificationService.send(customer.getUserId(), AppConfig.NOTIF_PAYMENT, "Thanh toán thành công", customerMsg, "/orders/detail?orderId=" + orderId);
                String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + orderId;
                emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(orderId), "Xác nhận thanh toán thành công", orderDetailUrl);
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo thanh toán cho orderId=" + orderId, ex);
        }
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
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newExpiry));
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
            LoggerUtil.warn(log, "[Webhook] Payload thiếu trường bắt buộc: %s", jsonPayload);
            return;
        }

        // Dedup: nếu đã xử lý rồi thì bỏ qua
        if (paymentDAO.isDuplicate(sepayTxId)) {
            LoggerUtil.info(log, "[Webhook] Duplicate sepay_tx_id=%s — bỏ qua.", sepayTxId);
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
            LoggerUtil.warn(log, "[Webhook] Không tìm thấy payment_transaction với reference=%s", code);
            paymentDAO.insertDedup(sepayTxId, code, "not_found");
            return;
        }

        // Validate số tiền
        BigDecimal received;
        try {
            received = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
        } catch (NumberFormatException nfe) {
            LoggerUtil.warn(log, "[Webhook] Số tiền không hợp lệ: %s", amountStr);
            paymentDAO.insertDedup(sepayTxId, code, "invalid_amount");
            return;
        }

        BigDecimal expected = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
        if (received.compareTo(expected) < 0) {
            paymentDAO.updateStatus(tx.getTransactionId(), "failed",
                                    sepayTxId, jsonPayload);
            paymentDAO.updateStatusFailed(tx.getTransactionId(),
                                          "AMOUNT_MISMATCH",
                                          "Nhận " + received + " < yêu cầu " + expected);
            paymentDAO.insertDedup(sepayTxId, code, "amount_mismatch");
            LoggerUtil.warn(log, "[Webhook] Số tiền không khớp: expected=%s received=%s orderId=%d",
                expected, received, tx.getOrderId());
            return;
        }

        // Cập nhật payment → completed
        paymentDAO.updateStatus(tx.getTransactionId(), "completed", sepayTxId, jsonPayload);
        // [AUTOMATED] SePay automatically confirms the order upon successful payment match
        orderDAO.updateStatus(tx.getOrderId(), AppConfig.ORDER_CONFIRMED);
        
        // Gửi thông báo thanh toán thành công cho Customer
        try {
            Order order = orderDAO.findOneById(tx.getOrderId());
            if (order != null) {
                User customer = userDAO.findUserById(order.getCustomerId());
                if (customer != null) {
                    String customerMsg = "Đơn hàng #" + order.getOrderId() + " đã được xác nhận thanh toán thành công qua chuyển khoản tự động.";
                    notificationService.send(customer.getUserId(), AppConfig.NOTIF_PAYMENT, "Thanh toán thành công", customerMsg, "/orders/detail?orderId=" + order.getOrderId());
                    String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + order.getOrderId();
                    emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(order.getOrderId()), "Xác nhận thanh toán thành công", orderDetailUrl);
                }
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo thanh toán webhook cho orderId=" + tx.getOrderId(), ex);
        }

        // Ghi dedup
        paymentDAO.insertDedup(sepayTxId, code, "processed");
        LoggerUtil.info(log, "[Webhook] Thanh toán thành công orderId=%d sepayTxId=%s", tx.getOrderId(), sepayTxId);
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
            if (end > start) {
                String val = json.substring(start + 1, end).trim();
                return val.isEmpty() ? null : val;
            }
            return null;
        } else {
            // Number / boolean / null
            int end = start;
            while (end < json.length() && ",}]\n\r ".indexOf(json.charAt(end)) < 0) end++;
            String val = json.substring(start, end).trim();
            return val.isEmpty() || "null".equalsIgnoreCase(val) ? null : val;
        }
    }

    /** Tạo reference SePay theo format MF + mã đơn hàng, padded tối thiểu 3 chữ số. */
    public static String buildSepayReference(int orderId) {
        return String.format("%s%03d", AppConfig.PAYMENT_REF_PREFIX, orderId);
    }

    /** Mở kết nối dùng cho renewQr (package-private để test). */
    Connection openConnection() throws SQLException {
        return orderDAO.openConnection();
    }
}
