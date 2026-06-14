package test;

import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.order.DeliveryDAO;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.order.Order;
import model.entity.order.Delivery;
import service.order.DeliveryService;
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

public class DeliveryServiceTest {

    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private OrderDAO orderDAO;
    private DeliveryDAO deliveryDAO;
    private DeliveryService deliveryService;

    private int testOwnerId = -1;
    private int testCustomerId = -1;
    private int testShipperId = -1;
    private int testCategoryId = -1;
    private int testProductId = -1;
    private int testVariantId = -1;
    private int testOrderId = -1;
    private int testDeliveryId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        orderDAO = new OrderDAO();
        deliveryDAO = new DeliveryDAO();
        deliveryService = new DeliveryService();

        // 1. Create owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("Del Test Owner", "del_owner_" + System.currentTimeMillis() + "@test.com", "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);

        // 2. Create customer
        String customerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer("Del Test Cust", "del_cust_" + System.currentTimeMillis() + "@test.com", "pwd", customerPhone, "CUSTOMER", "ACTIVE", true);

        // 3. Create shipper
        String shipperPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 2) % 100000000L));
        testShipperId = userDAO.saveNewCustomer("Del Test Shipper", "del_ship_" + System.currentTimeMillis() + "@test.com", "pwd", shipperPhone, "DELIVERY", "ACTIVE", true);

        // 4. Create category
        Category cat = new Category();
        cat.setName("Del Test Cat " + System.currentTimeMillis());
        cat.setSlug("del-test-cat-" + System.currentTimeMillis());
        cat.setDisplayOrder(100);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // 5. Create product
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Product Del Test");
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setHarvestDate(LocalDate.now());
        p.setShelfLifeDays(10);
        testProductId = productDAO.save(p);

        // 6. Create variant
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("DEL-SKU-" + System.currentTimeMillis());
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
        o.setStatus("APPROVED");
        o.setTotalAmount(new BigDecimal("100000.00"));
        o.setDeliveryFee(new BigDecimal("20000.00"));
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setSystemDiscountAmount(BigDecimal.ZERO);
        o.setShopDiscountAmount(BigDecimal.ZERO);
        o.setPlatformFee(BigDecimal.ZERO);
        o.setFinalAmount(new BigDecimal("120000.00"));
        o.setPaymentMethod("COD");
        o.setRefundStatus("NONE");
        testOrderId = orderDAO.save(o);
    }

    @After
    public void tearDown() {
        try {
            if (testOrderId != -1) {
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
            if (testShipperId != -1) {
                userDAO.deleteUser(testShipperId);
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    @Test
    public void testDeliveryAssignmentAndCompletion() throws Exception {
        // Assign shipper
        deliveryService.assignShipper(testOrderId, testShipperId, LocalDateTime.now().plusDays(1));

        Delivery d = deliveryDAO.findByOrderId(testOrderId);
        assertNotNull(d);
        testDeliveryId = d.getDeliveryId();

        assertEquals("ASSIGNED", d.getStatus());
        assertEquals(testShipperId, d.getStaffId().intValue());

        // Update status to DELIVERED with proof
        deliveryService.markAsDelivered(testShipperId, testDeliveryId, "http://proof.image.url");

        d = deliveryDAO.findById(testDeliveryId);
        assertEquals("DELIVERED", d.getStatus());
        assertEquals("http://proof.image.url", d.getProofImageUrl());

        // Check if order status is updated to DELIVERED too
        Order order = orderDAO.findById(testOrderId).get(0);
        assertEquals("DELIVERED", order.getStatus());
    }

    @Test
    public void testGetDashboardDeliveries() throws Exception {
        // Create delivery
        deliveryService.assignShipper(testOrderId, testShipperId, LocalDateTime.now().plusDays(1));
        
        List<Delivery> merged = deliveryService.getDashboardDeliveries(testShipperId);
        assertNotNull(merged);
        assertTrue(merged.size() > 0);
    }

    private void hardDeleteDelivery(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps1 = conn.prepareStatement("DELETE FROM deliveries WHERE order_id = ?");
             PreparedStatement ps2 = conn.prepareStatement("DELETE FROM delivery_trips WHERE parent_order_id = ?")) {
            ps1.setInt(1, orderId);
            ps1.executeUpdate();
            ps2.setInt(1, orderId);
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
