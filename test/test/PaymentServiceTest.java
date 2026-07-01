package test;

import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.shop.PaymentDAO;
import model.entity.auth.User;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class PaymentServiceTest {

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
    private int testTransactionId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        orderDAO = new OrderDAO();
        paymentDAO = new PaymentDAO();
        paymentService = new PaymentService();

        // 1. Create owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("Pay Test Owner", "pay_owner_" + System.currentTimeMillis() + "@test.com", "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);

        // 2. Create customer
        String customerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer("Pay Test Cust", "pay_cust_" + System.currentTimeMillis() + "@test.com", "pwd", customerPhone, "CUSTOMER", "ACTIVE", true);

        // 3. Create category
        Category cat = new Category();
        cat.setName("Pay Test Cat " + System.currentTimeMillis());
        cat.setSlug("pay-test-cat-" + System.currentTimeMillis());
        cat.setDisplayOrder(100);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // 4. Create product
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Product Pay Test");
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setHarvestDate(LocalDate.now());
        p.setShelfLifeDays(10);
        testProductId = productDAO.save(p);

        // 5. Create variant
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("PAY-SKU-" + System.currentTimeMillis());
        v.setVariantLabel("Box");
        v.setPrice(new BigDecimal("100000.00"));
        v.setStockQuantity(10);
        v.setIsActive(true);
        testVariantId = variantDAO.save(v);

        // 6. Create order
        Order o = new Order();
        o.setCustomerId(testCustomerId);
        o.setOwnerId(testOwnerId);
        o.setDeliveryAddress("Test Address");
        o.setStatus("PENDING_PAYMENT");
        o.setTotalAmount(new BigDecimal("100000.00"));
        o.setDeliveryFee(new BigDecimal("20000.00"));
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setSystemDiscountAmount(BigDecimal.ZERO);
        o.setShopDiscountAmount(BigDecimal.ZERO);
        o.setPlatformFee(BigDecimal.ZERO);
        o.setFinalAmount(new BigDecimal("120000.00"));
        o.setPaymentMethod("CK");
        o.setRefundStatus("NONE");
        testOrderId = orderDAO.save(o);

        // 7. Create payment transaction
        testTransactionId = paymentDAO.initTransaction(testOrderId, "CK", new BigDecimal("120000.00"), "TX" + testOrderId, "127.0.0.1", LocalDateTime.now().plusHours(1));
    }

    @After
    public void tearDown() {
        try {
            if (testOrderId != -1) {
                hardDeletePayments(testOrderId);
                hardDeleteOrder(testOrderId);
            }
            if (testProductId != -1) {
                hardDeleteProduct(testProductId);
            }
            if (testCategoryId != -1) {
                categoryDAO.delete(testCategoryId);
            }
            if (testOwnerId != -1) {
                userDAO.deleteUser(testOwnerId);
            }
            if (testCustomerId != -1) {
                userDAO.deleteUser(testCustomerId);
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    @Test
    public void testRenewQr() throws Exception {
        paymentService.renewQr(testOrderId, testCustomerId);
        PaymentTransaction tx = paymentDAO.findByOrder(testOrderId).get(0);
        assertNotNull(tx);
        assertTrue(tx.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    public void testConfirmManualPaymentMovesTransactionToProcessing() throws Exception {
        boolean confirmed = paymentService.confirmManualPayment(testOrderId, testCustomerId);
        assertTrue(confirmed);

        PaymentTransaction tx = paymentDAO.findByOrder(testOrderId).get(0);
        assertNotNull(tx);
        assertEquals("processing", tx.getStatus());
        assertNotNull(tx.getSepayReference());

        Order o = orderDAO.findById(testOrderId).get(0);
        assertEquals("PENDING_PAYMENT", o.getStatus());
    }

    @Test
    public void testAdminApprovePayment() throws Exception {
        // Create an admin
        String adminPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        int adminId = userDAO.saveNewCustomer("Pay Admin", "pay_admin_" + System.currentTimeMillis() + "@test.com", "pwd", adminPhone, "ADMIN", "ACTIVE", true);
        try {
            paymentService.adminApprovePayment(testOrderId, adminId);
            Order o = orderDAO.findById(testOrderId).get(0);
            assertEquals("CONFIRMED", o.getStatus());
        } finally {
            userDAO.deleteUser(adminId);
        }
    }

    @Test
    public void testProcessWebhookNormal() throws Exception {
        String payload = "{"
                + "\"id\": \"SEPAY_TX_123\","
                + "\"code\": \"TX" + testOrderId + "\","
                + "\"transferType\": \"in\","
                + "\"transferAmount\": \"120000.00\""
                + "}";
        PaymentService.WebhookProcessingResult result = paymentService.processWebhook(payload);
        assertEquals("processed", result.getOutcome());
        assertFalse(result.isDuplicate());

        PaymentTransaction tx = paymentDAO.findByOrder(testOrderId).get(0);
        assertEquals("completed", tx.getStatus());
        assertEquals("SEPAY_TX_123", tx.getSepayTransactionId());

        Order o = orderDAO.findById(testOrderId).get(0);
        assertEquals("CONFIRMED", o.getStatus());

        // Deduplication check
        assertTrue(paymentDAO.isDuplicate("SEPAY_TX_123"));
    }

    @Test
    public void testProcessWebhookDuplicate() throws Exception {
        String payload = "{"
                + "\"id\": \"SEPAY_TX_DUPLICATE\","
                + "\"code\": \"TX" + testOrderId + "\","
                + "\"transferType\": \"in\","
                + "\"transferAmount\": \"120000.00\""
                + "}";
        PaymentService.WebhookProcessingResult firstResult = paymentService.processWebhook(payload);
        assertEquals("processed", firstResult.getOutcome());

        // Send again
        PaymentService.WebhookProcessingResult duplicateResult = paymentService.processWebhook(payload);
        assertEquals("duplicate", duplicateResult.getOutcome());
        assertTrue(duplicateResult.isDuplicate());
    }

    @Test
    public void testProcessWebhookMismatchedAmount() throws Exception {
        String payload = "{"
                + "\"id\": \"SEPAY_TX_MISMATCH\","
                + "\"code\": \"TX" + testOrderId + "\","
                + "\"transferType\": \"in\","
                + "\"transferAmount\": \"1000.00\""
                + "}";
        PaymentService.WebhookProcessingResult result = paymentService.processWebhook(payload);
        assertEquals("amount_mismatch", result.getOutcome());

        PaymentTransaction tx = paymentDAO.findByOrder(testOrderId).get(0);
        assertEquals("failed", tx.getStatus());
        assertEquals("AMOUNT_MISMATCH", tx.getErrorCode());
    }

    private void hardDeletePayments(int orderId) throws SQLException {
        try (Connection conn = paymentDAO.openConnection();
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM payment_transactions WHERE order_id = ?");
             PreparedStatement ps2 = conn.prepareStatement("DELETE FROM sepay_webhook_dedup WHERE order_code = ?")) {
            ps1.setInt(1, orderId);
            ps1.executeUpdate();
            ps2.setString(1, "TX" + orderId);
            ps2.executeUpdate();
        }
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM product_variants WHERE product_id = ?");
             PreparedStatement ps2 = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
            ps1.setInt(1, productId);
            ps1.executeUpdate();
            ps2.setInt(1, productId);
            ps2.executeUpdate();
        }
    }
}
