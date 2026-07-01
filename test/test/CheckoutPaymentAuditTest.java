package test;

import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.shop.PaymentDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import service.shop.PaymentService;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * CheckoutPaymentAuditTest — Smoke test end-to-end luồng thanh toán CK.
 *
 * Kiểm tra:
 *   1. Tạo order CK → status = PENDING_PAYMENT
 *   2. Payment transaction được khởi tạo đúng (amount, reference, status=pending)
 *   3. Webhook hợp lệ → order → CONFIRMED, payment → completed
 *   4. Webhook duplicate → không lỗi, dedup idempotent
 *   5. Webhook khi order đã CONFIRMED → bị bỏ qua (skipped_wrong_status)
 *   6. Webhook transferType="out" → bị bỏ qua (skipped_not_in)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CheckoutPaymentAuditTest {

    private static final BigDecimal ORDER_AMOUNT = new BigDecimal("150000.00");

    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private PaymentService paymentService;

    private int testOwnerId = -1;
    private int testCustomerId = -1;
    private int testCategoryId = -1;
    private int testProductId = -1;
    private int testVariantId = -1;
    private int testOrderId = -1;
    private String testReference;
    private String testSepayTxId;

    @Before
    public void setUp() throws Exception {
        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        orderDAO = new OrderDAO();
        paymentDAO = new PaymentDAO();
        paymentService = new PaymentService();

        long ts = System.currentTimeMillis();
        testSepayTxId = "AUDIT_TX_" + ts;

        // Owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("Audit Owner", "audit_owner_" + ts + "@test.com",
            "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);

        // Customer
        String custPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer("Audit Customer", "audit_cust_" + ts + "@test.com",
            "pwd", custPhone, "CUSTOMER", "ACTIVE", true);

        // Category
        Category cat = new Category();
        cat.setName("Audit Cat " + ts);
        cat.setSlug("audit-cat-" + ts);
        cat.setDisplayOrder(300);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // Product
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Audit Product " + ts);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setHarvestDate(LocalDate.now());
        p.setShelfLifeDays(7);
        testProductId = productDAO.save(p);

        // Variant
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("AUDIT-SKU-" + ts);
        v.setVariantLabel("Túi 1kg");
        v.setPrice(ORDER_AMOUNT);
        v.setStockQuantity(50);
        v.setIsActive(true);
        testVariantId = variantDAO.save(v);

        // Order PENDING_PAYMENT
        Order o = new Order();
        o.setCustomerId(testCustomerId);
        o.setOwnerId(testOwnerId);
        o.setDeliveryAddress("456 Audit Lane");
        o.setStatus("PENDING_PAYMENT");
        o.setOrderType("CHILD");
        o.setTotalAmount(ORDER_AMOUNT);
        o.setDeliveryFee(BigDecimal.ZERO);
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setSystemDiscountAmount(BigDecimal.ZERO);
        o.setShopDiscountAmount(BigDecimal.ZERO);
        o.setPlatformFee(BigDecimal.ZERO);
        o.setFinalAmount(ORDER_AMOUNT);
        o.setPaymentMethod("CK");
        o.setRefundStatus("NONE");
        testOrderId = orderDAO.save(o);

        // Payment transaction
        testReference = "MFAUDIT" + testOrderId;
        paymentDAO.initTransaction(testOrderId, "CK", ORDER_AMOUNT, testReference,
            "127.0.0.1", LocalDateTime.now().plusMinutes(15));
    }

    @After
    public void tearDown() {
        try {
            cleanupDedup(testSepayTxId);
            if (testOrderId != -1) {
                cleanupPayments(testOrderId);
                cleanupOrder(testOrderId);
            }
            if (testVariantId != -1) cleanupVariant(testVariantId);
            if (testProductId != -1) cleanupProduct(testProductId);
            if (testCategoryId != -1) categoryDAO.delete(testCategoryId);
            if (testOwnerId != -1) userDAO.deleteUser(testOwnerId);
            if (testCustomerId != -1) userDAO.deleteUser(testCustomerId);
        } catch (Exception e) {
            System.err.println("[CheckoutPaymentAuditTest] Cleanup: " + e.getMessage());
        }
    }

    // TC-AUDIT-01
    @Test
    public void test01_orderCreated_shouldBePendingPayment() throws Exception {
        List<Order> orders = orderDAO.findById(testOrderId);
        assertFalse("Order phải tồn tại", orders.isEmpty());
        assertEquals("Order status phải là PENDING_PAYMENT", "PENDING_PAYMENT", orders.get(0).getStatus());
        assertEquals("Payment method phải là CK", "CK", orders.get(0).getPaymentMethod());
    }

    // TC-AUDIT-02
    @Test
    public void test02_paymentTransaction_shouldBeInitializedCorrectly() throws Exception {
        List<PaymentTransaction> txList = paymentDAO.findByOrder(testOrderId);
        assertFalse("Payment transaction phải tồn tại", txList.isEmpty());
        PaymentTransaction tx = txList.get(0);
        assertEquals("Status phải = pending", "pending", tx.getStatus());
        assertEquals("Reference phải đúng format", testReference, tx.getSepayReference());
        assertEquals("Amount phải khớp với finalAmount",
            ORDER_AMOUNT.stripTrailingZeros(), tx.getAmount().stripTrailingZeros());
        assertTrue("QR chưa hết hạn", tx.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // TC-AUDIT-03
    @Test
    public void test03_validWebhook_shouldConfirmOrder() throws Exception {
        String payload = buildPayload(testSepayTxId, testReference, "in", ORDER_AMOUNT.toPlainString());
        paymentService.processWebhook(payload);

        List<Order> orders = orderDAO.findById(testOrderId);
        assertEquals("Order phải chuyển sang CONFIRMED", "CONFIRMED", orders.get(0).getStatus());

        List<PaymentTransaction> txList = paymentDAO.findByOrder(testOrderId);
        assertEquals("Payment phải chuyển sang completed", "completed", txList.get(0).getStatus());
        assertEquals("sepayTransactionId phải được lưu", testSepayTxId, txList.get(0).getSepayTransactionId());

        assertTrue("Dedup record phải tồn tại", paymentDAO.isDuplicate(testSepayTxId));
    }

    // TC-AUDIT-04
    @Test
    public void test04_duplicateWebhook_shouldBeIdempotent() throws Exception {
        // Lần 1
        String payload = buildPayload(testSepayTxId + "_DUP", testReference, "in", ORDER_AMOUNT.toPlainString());
        paymentService.processWebhook(payload);
        // Lần 2 (duplicate)
        paymentService.processWebhook(payload);

        // Chỉ có 1 dedup record
        int dedupCount = countDedupForTxId(testSepayTxId + "_DUP");
        assertEquals("Duplicate webhook: phải có đúng 1 dedup record", 1, dedupCount);
    }

    // TC-AUDIT-05: Order status guard — webhook sau khi order đã CONFIRMED bị bỏ qua
    @Test
    public void test05_webhookAfterConfirmed_shouldBeSkipped() throws Exception {
        // Confirm order trước
        orderDAO.updateStatus(testOrderId, "CONFIRMED");

        // Gửi webhook (khác txId để tránh dedup trùng)
        String laterTxId = testSepayTxId + "_LATE";
        String payload = buildPayload(laterTxId, testReference, "in", ORDER_AMOUNT.toPlainString());
        paymentService.processWebhook(payload); // không throw

        // Dedup phải có record cho late webhook
        assertTrue("Phải có dedup record cho late webhook", paymentDAO.isDuplicate(laterTxId));

        // Order vẫn CONFIRMED (không bị thay đổi)
        List<Order> orders = orderDAO.findById(testOrderId);
        assertEquals("Order phải vẫn CONFIRMED", "CONFIRMED", orders.get(0).getStatus());
    }

    // TC-AUDIT-06: transferType="out" bị bỏ qua
    @Test
    public void test06_outboundTransfer_shouldBeSkipped() throws Exception {
        String outTxId = testSepayTxId + "_OUT";
        String payload = buildPayload(outTxId, testReference, "out", ORDER_AMOUNT.toPlainString());
        paymentService.processWebhook(payload);

        // Order vẫn PENDING_PAYMENT
        List<Order> orders = orderDAO.findById(testOrderId);
        assertEquals("Order phải vẫn PENDING_PAYMENT với transferType=out",
            "PENDING_PAYMENT", orders.get(0).getStatus());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String buildPayload(String txId, String code, String transferType, String amount) {
        return "{\"id\": \"" + txId + "\","
            + "\"code\": \"" + code + "\","
            + "\"transferType\": \"" + transferType + "\","
            + "\"transferAmount\": \"" + amount + "\"}";
    }

    private int countDedupForTxId(String sepayTxId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM sepay_webhook_dedup WHERE sepay_transaction_id = ?")) {
            ps.setString(1, sepayTxId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("countDedupForTxId error: " + e.getMessage());
        }
        return -1;
    }

    private void cleanupDedup(String txIdPrefix) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM sepay_webhook_dedup WHERE sepay_transaction_id LIKE ?")) {
            ps.setString(1, txIdPrefix + "%");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupDedup error: " + e.getMessage());
        }
    }

    private void cleanupPayments(int orderId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM payment_transactions WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupPayments error: " + e.getMessage());
        }
    }

    private void cleanupOrder(int orderId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM orders WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupOrder error: " + e.getMessage());
        }
    }

    private void cleanupVariant(int variantId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE variant_id = ?")) {
            ps.setInt(1, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupVariant error: " + e.getMessage());
        }
    }

    private void cleanupProduct(int productId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE products SET status = 'DELETED', updated_at = GETDATE() WHERE product_id = ?")) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupProduct error: " + e.getMessage());
        }
    }
}
