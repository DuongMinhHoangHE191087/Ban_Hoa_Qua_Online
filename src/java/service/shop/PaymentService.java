package service.shop;

import config.AppConfig;
import dao.order.OrderDAO;
import dao.shop.PaymentDAO;
import dao.auth.UserDAO;
import com.fasterxml.jackson.databind.JsonNode;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import model.entity.auth.User;
import util.JsonUtil;
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
    private static final class SepayWebhookPayload {
        public JsonNode id;
        public JsonNode code;
        public JsonNode transferType;
        public JsonNode transferAmount;
    }

    private static final class WebhookProcessingResult {
        private final boolean duplicate;
        private final boolean notifyCustomer;
        private final int orderId;
        private final int customerId;
        private final String sepayTxId;

        private WebhookProcessingResult(boolean duplicate, boolean notifyCustomer, int orderId,
                                        int customerId, String sepayTxId) {
            this.duplicate = duplicate;
            this.notifyCustomer = notifyCustomer;
            this.orderId = orderId;
            this.customerId = customerId;
            this.sepayTxId = sepayTxId;
        }

        static WebhookProcessingResult duplicate() {
            return new WebhookProcessingResult(true, false, -1, -1, null);
        }

        static WebhookProcessingResult handled(int orderId, int customerId, String sepayTxId) {
            return new WebhookProcessingResult(false, true, orderId, customerId, sepayTxId);
        }

        static WebhookProcessingResult skipped() {
            return new WebhookProcessingResult(false, false, -1, -1, null);
        }

        boolean isDuplicate() {
            return duplicate;
        }

        boolean shouldNotifyCustomer() {
            return notifyCustomer;
        }
    }

    public void processWebhook(String jsonPayload) throws SQLException {
        SepayWebhookPayload payload;
        try {
            payload = JsonUtil.fromJson(jsonPayload, SepayWebhookPayload.class);
        } catch (Exception e) {
            LoggerUtil.warn(log, "[Webhook] Payload JSON không hợp lệ: " + jsonPayload, e);
            return;
        }

        String sepayTxId = normalizeNodeText(payload != null ? payload.id : null);
        String code = normalizeNodeText(payload != null ? payload.code : null);
        String transferType = normalizeNodeText(payload != null ? payload.transferType : null);
        String amountStr = normalizeNodeText(payload != null ? payload.transferAmount : null);

        if (sepayTxId == null || code == null) {
            LoggerUtil.warn(log, "[Webhook] Payload thiếu trường bắt buộc: %s", jsonPayload);
            return;
        }

        WebhookProcessingResult result;
        try (Connection conn = paymentDAO.openConnection()) {
            conn.setAutoCommit(false);
            try {
                result = processWebhook(conn, jsonPayload, sepayTxId, code, transferType, amountStr);
                if (result.isDuplicate()) {
                    conn.rollback();
                    return;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        if (result.shouldNotifyCustomer()) {
            notifyWebhookSuccess(result.orderId, result.customerId, result.sepayTxId);
        }
    }

    private WebhookProcessingResult processWebhook(Connection conn, String jsonPayload, String sepayTxId,
                                                   String code, String transferType, String amountStr) throws SQLException {
        if (!paymentDAO.insertDedup(conn, sepayTxId, code, "processing")) {
            LoggerUtil.info(log, "[Webhook] Duplicate sepay_tx_id=%s — bỏ qua.", sepayTxId);
            return WebhookProcessingResult.duplicate();
        }

        if (!"in".equalsIgnoreCase(transferType)) {
            paymentDAO.updateDedupResult(conn, sepayTxId, "skipped_not_in");
            return WebhookProcessingResult.skipped();
        }

        PaymentTransaction tx = paymentDAO.findByReference(conn, code);
        if (tx == null) {
            LoggerUtil.warn(log, "[Webhook] Không tìm thấy payment_transaction với reference=%s", code);
            paymentDAO.updateDedupResult(conn, sepayTxId, "not_found");
            return WebhookProcessingResult.skipped();
        }

        Order order = orderDAO.findOneById(conn, tx.getOrderId());
        if (order == null || !AppConfig.ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            String currentStatus = order != null ? order.getStatus() : "null";
            LoggerUtil.warn(log, "[Webhook] orderId=%d không ở trạng thái PENDING_PAYMENT (hiện tại: %s) — bỏ qua.",
                tx.getOrderId(), currentStatus);
            paymentDAO.updateDedupResult(conn, sepayTxId, "skipped_wrong_status");
            return WebhookProcessingResult.skipped();
        }

        BigDecimal received;
        try {
            received = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
        } catch (NumberFormatException nfe) {
            LoggerUtil.warn(log, "[Webhook] Số tiền không hợp lệ: %s", amountStr);
            paymentDAO.updateDedupResult(conn, sepayTxId, "invalid_amount");
            return WebhookProcessingResult.skipped();
        }

        BigDecimal expected = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
        if (received.compareTo(expected) < 0) {
            paymentDAO.updateStatus(conn, tx.getTransactionId(), "failed", sepayTxId, jsonPayload);
            paymentDAO.updateDedupResult(conn, sepayTxId, "amount_mismatch");
            LoggerUtil.warn(log, "[Webhook] Số tiền không khớp: expected=%s received=%s orderId=%d",
                expected, received, tx.getOrderId());
            return WebhookProcessingResult.skipped();
        }

        paymentDAO.updateStatus(conn, tx.getTransactionId(), "completed", sepayTxId, jsonPayload);
        orderDAO.updateStatus(conn, tx.getOrderId(), AppConfig.ORDER_CONFIRMED);
        paymentDAO.updateDedupResult(conn, sepayTxId, "processed");
        LoggerUtil.info(log, "[Webhook] Thanh toán thành công orderId=%d sepayTxId=%s", tx.getOrderId(), sepayTxId);
        return WebhookProcessingResult.handled(order.getOrderId(), order.getCustomerId(), sepayTxId);
    }

    private void notifyWebhookSuccess(int orderId, int customerId, String sepayTxId) {
        try {
            LoggerUtil.info(log, "[Webhook] Gửi thông báo khách hàng cho orderId=%d sepayTxId=%s", orderId, sepayTxId);
            User customer = userDAO.findUserById(customerId);
            if (customer != null) {
                String customerMsg = "Đơn hàng #" + orderId + " đã được xác nhận thanh toán thành công qua chuyển khoản tự động.";
                notificationService.send(customer.getUserId(), AppConfig.NOTIF_PAYMENT, "Thanh toán thành công", customerMsg, "/orders/detail?orderId=" + orderId);
                String orderDetailUrl = AppConfig.APP_BASE_URL + "/orders/detail?orderId=" + orderId;
                emailService.sendOrderNotificationEmail(customer.getEmail(), customer.getFullName(), String.valueOf(orderId), "Xác nhận thanh toán thành công", orderDetailUrl);
            }
        } catch (Exception ex) {
            LoggerUtil.warn(log, "Không gửi được thông báo thanh toán webhook cho orderId=" + orderId, ex);
        }
    }

    private String normalizeNodeText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
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
