package test;

import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.order.ReturnRequestDAO;
import dao.order.DeliveryDAO;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.order.ReturnRequest;
import service.order.ReturnService;
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

import static org.junit.Assert.*;

public class ReturnServiceTest {

    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private OrderDAO orderDAO;
    private ReturnRequestDAO returnDAO;
    private DeliveryDAO deliveryDAO;
    private ReturnService returnService;

    private int testOwnerId = -1;
    private int testCustomerId = -1;
    private int testAdminId = -1;
    private int testCategoryId = -1;
    private int testProductId = -1;
    private int testVariantId = -1;
    private int testOrderId = -1;
    private int testOrderItemId = -1;
    private int testRequestId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        orderDAO = new OrderDAO();
        returnDAO = new ReturnRequestDAO();
        deliveryDAO = new DeliveryDAO();
        returnService = new ReturnService();

        // 1. Create owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("Ret Test Owner", "ret_owner_" + System.currentTimeMillis() + "@test.com", "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);

        // 2. Create customer
        String customerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer("Ret Test Cust", "ret_cust_" + System.currentTimeMillis() + "@test.com", "pwd", customerPhone, "CUSTOMER", "ACTIVE", true);

        // 3. Create admin
        String adminPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 2) % 100000000L));
        testAdminId = userDAO.saveNewCustomer("Ret Test Admin", "ret_admin_" + System.currentTimeMillis() + "@test.com", "pwd", adminPhone, "ADMIN", "ACTIVE", true);

        // 4. Create category
        Category cat = new Category();
        cat.setName("Ret Test Cat " + System.currentTimeMillis());
        cat.setSlug("ret-test-cat-" + System.currentTimeMillis());
        cat.setDisplayOrder(100);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // 5. Create product
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Product Ret Test");
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setHarvestDate(LocalDate.now());
        p.setShelfLifeDays(10);
        testProductId = productDAO.save(p);

        // 6. Create variant
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("RET-SKU-" + System.currentTimeMillis());
        v.setVariantLabel("Box");
        v.setPrice(new BigDecimal("100000.00"));
        v.setStockQuantity(10);
        v.setIsActive(true);
        testVariantId = variantDAO.save(v);

        // 7. Create order
        Order o = new Order();
        o.setCustomerId(testCustomerId);
        o.setOwnerId(testOwnerId);
        o.setDeliveryAddress("Test Address");
        o.setStatus("DELIVERED");
        o.setTotalAmount(new BigDecimal("100000.00"));
        o.setDeliveryFee(new BigDecimal("20000.00"));
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setSystemDiscountAmount(BigDecimal.ZERO);
        o.setShopDiscountAmount(BigDecimal.ZERO);
        o.setPlatformFee(BigDecimal.ZERO);
        o.setFinalAmount(new BigDecimal("120000.00"));
        o.setPaymentMethod("COD");
        o.setRefundStatus("NONE");
        o.setUpdatedAt(LocalDateTime.now());
        testOrderId = orderDAO.save(o);

        // 8. Create order item
        testOrderItemId = insertOrderItem(testOrderId, testVariantId, 2, new BigDecimal("100000.00"));

        // 9. Create delivery log
        insertDeliveryLog(testOrderId, testCustomerId);
    }

    @After
    public void tearDown() {
        try {
            if (testOrderId != -1) {
                hardDeleteReturnRequests(testOrderId);
                hardDeleteDelivery(testOrderId);
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
            if (testAdminId != -1) {
                userDAO.deleteUser(testAdminId);
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    @Test
    public void testCreateReturnRequestSuccess() throws Exception {
        ReturnRequest req = new ReturnRequest();
        req.setOrderId(testOrderId);
        req.setCustomerId(testCustomerId);
        req.setOrderItemId(testOrderItemId);
        req.setRequestType("RETURN");
        req.setRequestedQuantity(1);
        req.setReasonCode("OTHER");
        req.setDescription("Product got damaged during transit");
        req.setEvidenceUrl("http://evidence.url");

        testRequestId = returnService.createRequest(req);
        assertTrue(testRequestId > 0);

        ReturnRequest saved = returnDAO.findById(testRequestId);
        assertNotNull(saved);
        assertEquals("REQUESTED", saved.getStatus());
        assertEquals(new BigDecimal("100000.00"), saved.getRefundAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateReturnRequestNonDeliveredOrder() throws Exception {
        // Change order status back to CONFIRMED
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = 'CONFIRMED' WHERE order_id = ?")) {
            ps.setInt(1, testOrderId);
            ps.executeUpdate();
        }

        ReturnRequest req = new ReturnRequest();
        req.setOrderId(testOrderId);
        req.setCustomerId(testCustomerId);
        req.setOrderItemId(testOrderItemId);
        req.setRequestType("RETURN");
        req.setRequestedQuantity(1);
        req.setReasonCode("OTHER");
        req.setDescription("Test");

        returnService.createRequest(req); // should throw IllegalArgumentException
    }

    @Test
    public void testAdminApproveReturnRequest() throws Exception {
        ReturnRequest req = new ReturnRequest();
        req.setOrderId(testOrderId);
        req.setCustomerId(testCustomerId);
        req.setOrderItemId(testOrderItemId);
        req.setRequestType("RETURN");
        req.setRequestedQuantity(2);
        req.setReasonCode("OTHER");
        req.setDescription("Approved Test");
        
        testRequestId = returnService.createRequest(req);

        // Approve it
        returnService.decide(testRequestId, "APPROVED", "Approved by admin", testAdminId);

        ReturnRequest saved = returnDAO.findById(testRequestId);
        assertEquals("APPROVED", saved.getStatus());

        // Check order refund status is REFUNDED
        Order o = orderDAO.findById(testOrderId).get(0);
        assertEquals("REFUNDED", o.getRefundStatus());

        // Check variant stock is restored (original stock is 10)
        ProductVariant variant = variantDAO.findById(testVariantId);
        assertEquals(12, variant.getStockQuantity());
    }

    private int insertOrderItem(int orderId, int variantId, int qty, BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal) "
                   + "VALUES (?, ?, N'Snapshot Name', N'Snapshot Label', ?, ?, ?)";
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setInt(2, variantId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            ps.setBigDecimal(5, unitPrice.multiply(new BigDecimal(qty)));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create order item");
    }

    private void insertDeliveryLog(int orderId, int shipperId) throws SQLException {
        String sql = "INSERT INTO deliveries (order_id, staff_id, status, delivered_at, created_at, updated_at) "
                   + "VALUES (?, ?, 'DELIVERED', GETDATE(), GETDATE(), GETDATE())";
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, shipperId);
            ps.executeUpdate();
        }
    }

    private void hardDeleteReturnRequests(int orderId) throws SQLException {
        try (Connection conn = returnDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM return_requests WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private void hardDeleteDelivery(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM deliveries WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?");
             PreparedStatement ps2 = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
            ps1.setInt(1, orderId);
            ps1.executeUpdate();
            ps2.setInt(1, orderId);
            ps2.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps1 = conn.prepareStatement("UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE product_id = ?");
             PreparedStatement ps2 = conn.prepareStatement("UPDATE products SET status = 'DELETED', updated_at = GETDATE() WHERE product_id = ?")) {
            ps1.setInt(1, productId);
            ps1.executeUpdate();
            ps2.setInt(1, productId);
            ps2.executeUpdate();
        }
    }
}
