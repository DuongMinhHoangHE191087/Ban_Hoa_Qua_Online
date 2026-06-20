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
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * PaymentWebhookRaceTest — Kiểm tra idempotency của webhook khi nhiều request đến đồng thời.
 *
 * Kịch bản: 10 thread cùng gọi processWebhook() với cùng sepayTxId.
 * Kết quả đúng: chỉ 1 bản ghi trong dedup table, order status = CONFIRMED đúng 1 lần.
 */
public class PaymentWebhookRaceTest {

    private static final int CONCURRENT_WEBHOOKS = 10;
    private static final BigDecimal ORDER_AMOUNT = new BigDecimal("120000.00");

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

    private String uniqueSepayTxId;
    private String sepayReference;

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
        uniqueSepayTxId = "RACE_WH_" + ts;

        // Tạo owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("WH Race Owner", "wh_race_owner_" + ts + "@test.com",
            "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);

        // Tạo customer
        String custPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer("WH Race Cust", "wh_race_cust_" + ts + "@test.com",
            "pwd", custPhone, "CUSTOMER", "ACTIVE", true);

        // Tạo category
        Category cat = new Category();
        cat.setName("WH Race Cat " + ts);
        cat.setSlug("wh-race-cat-" + ts);
        cat.setDisplayOrder(200);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // Tạo product
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("WH Race Product " + ts);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setHarvestDate(LocalDate.now());
        p.setShelfLifeDays(10);
        testProductId = productDAO.save(p);

        // Tạo variant
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("WH-RACE-SKU-" + ts);
        v.setVariantLabel("Box");
        v.setPrice(ORDER_AMOUNT);
        v.setStockQuantity(100);
        v.setIsActive(true);
        testVariantId = variantDAO.save(v);

        // Tạo order PENDING_PAYMENT
        Order o = new Order();
        o.setCustomerId(testCustomerId);
        o.setOwnerId(testOwnerId);
        o.setDeliveryAddress("123 Test Street");
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

        // Tạo payment_transaction
        sepayReference = "MFRACE" + testOrderId;
        paymentDAO.initTransaction(testOrderId, "CK", ORDER_AMOUNT, sepayReference, "127.0.0.1",
            LocalDateTime.now().plusMinutes(15));
    }

    @After
    public void tearDown() {
        try {
            if (testOrderId != -1) {
                cleanupDedup(uniqueSepayTxId);
                cleanupPayments(testOrderId);
                cleanupOrder(testOrderId);
            }
            if (testVariantId != -1) cleanupVariant(testVariantId);
            if (testProductId != -1) cleanupProduct(testProductId);
            if (testCategoryId != -1) categoryDAO.delete(testCategoryId);
            if (testOwnerId != -1) userDAO.deleteUser(testOwnerId);
            if (testCustomerId != -1) userDAO.deleteUser(testCustomerId);
        } catch (Exception e) {
            System.err.println("[PaymentWebhookRaceTest] Cleanup: " + e.getMessage());
        }
    }

    /**
     * TC-WH-RACE-01: 10 webhook đồng thời với cùng sepayTxId — chỉ 1 record dedup được tạo,
     * order chỉ được confirm đúng 1 lần.
     */
    @Test
    public void should_processExactlyOnce_whenDuplicateWebhooksArrive() throws InterruptedException, SQLException {
        final String payload = "{"
            + "\"id\": \"" + uniqueSepayTxId + "\","
            + "\"code\": \"" + sepayReference + "\","
            + "\"transferType\": \"in\","
            + "\"transferAmount\": \"" + ORDER_AMOUNT.toPlainString() + "\""
            + "}";

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        CountDownLatch ready = new CountDownLatch(CONCURRENT_WEBHOOKS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENT_WEBHOOKS);

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_WEBHOOKS);

        for (int i = 0; i < CONCURRENT_WEBHOOKS; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    done.countDown();
                    return;
                }
                try {
                    paymentService.processWebhook(payload);
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("[WH Race] Error: " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        assertTrue("Tất cả webhook phải kết thúc trong 30s", done.await(30, TimeUnit.SECONDS));
        pool.shutdown();

        // Tất cả call phải hoàn thành không lỗi
        assertEquals("Tất cả " + CONCURRENT_WEBHOOKS + " webhook phải xử lý xong không lỗi",
            CONCURRENT_WEBHOOKS, processedCount.get() + errorCount.get());
        assertEquals("Không được có exception: errorCount phải = 0", 0, errorCount.get());

        // Chỉ đúng 1 dedup record
        int dedupCount = countDedup(uniqueSepayTxId);
        assertEquals("Phải có đúng 1 dedup record cho sepayTxId=" + uniqueSepayTxId, 1, dedupCount);

        // Order phải ở trạng thái CONFIRMED
        List<Order> orders = orderDAO.findById(testOrderId);
        assertFalse("Order phải tìm thấy được", orders.isEmpty());
        assertEquals("Order status phải = CONFIRMED", "CONFIRMED", orders.get(0).getStatus());

        // Payment phải ở trạng thái completed
        List<PaymentTransaction> txList = paymentDAO.findByOrder(testOrderId);
        assertFalse("Payment transaction phải tồn tại", txList.isEmpty());
        assertEquals("Payment status phải = completed", "completed", txList.get(0).getStatus());
    }

    /**
     * TC-WH-RACE-02: Webhook với số tiền không đủ — order không bị confirm.
     */
    @Test
    public void should_notConfirmOrder_whenAmountInsufficient() throws Exception {
        String lowAmountPayload = "{"
            + "\"id\": \"RACE_LOW_AMT_" + System.currentTimeMillis() + "\","
            + "\"code\": \"" + sepayReference + "\","
            + "\"transferType\": \"in\","
            + "\"transferAmount\": \"1000.00\""
            + "}";
        paymentService.processWebhook(lowAmountPayload);

        List<Order> orders = orderDAO.findById(testOrderId);
        assertFalse(orders.isEmpty());
        assertEquals("Order phải vẫn PENDING_PAYMENT khi số tiền không đủ",
            "PENDING_PAYMENT", orders.get(0).getStatus());
    }

    /**
     * TC-WH-RACE-03: Webhook với sai reference — không ảnh hưởng order.
     */
    @Test
    public void should_notConfirmOrder_whenReferenceNotFound() throws Exception {
        String wrongRefPayload = "{"
            + "\"id\": \"RACE_WRONG_REF_" + System.currentTimeMillis() + "\","
            + "\"code\": \"MFNONEXISTENT99999\","
            + "\"transferType\": \"in\","
            + "\"transferAmount\": \"" + ORDER_AMOUNT.toPlainString() + "\""
            + "}";
        paymentService.processWebhook(wrongRefPayload); // không throw

        List<Order> orders = orderDAO.findById(testOrderId);
        assertFalse(orders.isEmpty());
        assertEquals("Order phải vẫn PENDING_PAYMENT khi reference sai",
            "PENDING_PAYMENT", orders.get(0).getStatus());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private int countDedup(String sepayTxId) throws SQLException {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM sepay_webhook_dedup WHERE sepay_transaction_id = ?")) {
            ps.setString(1, sepayTxId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private void cleanupDedup(String sepayTxId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM sepay_webhook_dedup WHERE sepay_transaction_id LIKE ?")) {
            ps.setString(1, "RACE_WH_%");
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
                 "DELETE FROM product_variants WHERE variant_id = ?")) {
            ps.setInt(1, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupVariant error: " + e.getMessage());
        }
    }

    private void cleanupProduct(int productId) {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM products WHERE product_id = ?")) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("cleanupProduct error: " + e.getMessage());
        }
    }
}
