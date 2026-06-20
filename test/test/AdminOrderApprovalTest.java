package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.shop.PaymentDAO;
import model.entity.auth.User;
import model.entity.order.Order;
import service.shop.PaymentService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * AdminOrderApprovalTest — Kiểm tra PaymentService.adminApprovePayment():
 * admin phê duyệt thanh toán CK, từ chối quyền non-admin.
 *
 * adminApprovePayment(int orderId, int adminId) — nhận userId (int), không phải User object.
 */
public class AdminOrderApprovalTest {

    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private PaymentService paymentService;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;

    private int adminId = -1;
    private int customerId = -1;
    private int shopOwnerId = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;
    private int testOrderId = -1;

    @Before
    public void setUp() throws Exception {
        userDAO = new UserDAO();
        orderDAO = new OrderDAO();
        paymentDAO = new PaymentDAO();
        paymentService = new PaymentService();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();

        long ts = System.currentTimeMillis();
        adminId = userDAO.saveNewCustomer(
                "AOA Admin", "aoa_admin_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(1),
                AppConfig.ROLE_ADMIN, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        customerId = userDAO.saveNewCustomer(
                "AOA Customer", "aoa_cust_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(2),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        shopOwnerId = userDAO.saveNewCustomer(
                "AOA ShopOwner", "aoa_shop_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(3),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        categoryId = createCategory("AOA Category " + ts);
        productId = createProduct(shopOwnerId, categoryId, "AOA Product " + ts);
        variantId = createVariant(productId, "AOA-SKU-" + ts, new BigDecimal("500000"), 10);
        testOrderId = insertPendingBankTransferOrder(customerId, shopOwnerId, new BigDecimal("515000"));
        paymentService.initPayment(testOrderId, AppConfig.PAYMENT_CK);
    }

    @After
    public void tearDown() {
        try {
            if (testOrderId > 0) {
                hardDeletePaymentTransactions(testOrderId);
                hardDeleteOrder(testOrderId);
            }
            if (variantId > 0) hardDeleteVariant(variantId);
            if (productId > 0) hardDeleteProduct(productId);
            if (categoryId > 0) categoryDAO.delete(categoryId);
            if (adminId > 0)     userDAO.deleteUser(adminId);
            if (customerId > 0)  userDAO.deleteUser(customerId);
            if (shopOwnerId > 0) userDAO.deleteUser(shopOwnerId);
        } catch (SQLException e) {
            System.err.println("[AdminOrderApprovalTest] Cleanup: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-AOA-01: Admin phê duyệt thanh toán CK thành công
    // =========================================================

    @Test
    public void should_confirmOrder_when_adminApprovesPayment() throws Exception {
        paymentService.adminApprovePayment(testOrderId, adminId);

        Order order = orderDAO.findOneById(testOrderId);
        assertNotNull("Order phải tồn tại", order);
        assertNotEquals("Status phải thay đổi sau approve",
                AppConfig.ORDER_PENDING_PAYMENT, order.getStatus());
    }

    // =========================================================
    // TC-AOA-02: Customer bị từ chối phê duyệt
    // =========================================================

    @Test
    public void should_throwException_when_customerTries() {
        Exception ex = null;
        try {
            paymentService.adminApprovePayment(testOrderId, customerId);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi customer cố approve", ex);
    }

    // =========================================================
    // TC-AOA-03: Shop owner không được phê duyệt thay admin
    // =========================================================

    @Test
    public void should_throwException_when_shopOwnerTries() {
        Exception ex = null;
        try {
            paymentService.adminApprovePayment(testOrderId, shopOwnerId);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi shop owner cố approve", ex);
    }

    // =========================================================
    // TC-AOA-04: Order không tồn tại → exception rõ ràng
    // =========================================================

    @Test
    public void should_throwException_when_orderNotFound() {
        Exception ex = null;
        try {
            paymentService.adminApprovePayment(999999999, adminId);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi order không tồn tại", ex);
    }

    // =========================================================
    // TC-AOA-05: Admin không tồn tại bị từ chối
    // =========================================================

    @Test
    public void should_throwException_when_adminIdNotFound() {
        Exception ex = null;
        try {
            paymentService.adminApprovePayment(testOrderId, 999999999);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull("Phải ném exception khi adminId không tồn tại", ex);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private int insertPendingBankTransferOrder(int cId, int oId, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, delivery_address, status, payment_method, total_amount, " +
                "delivery_fee, final_amount, order_type, created_at, updated_at) " +
                "VALUES (?, ?, '123 Test Address', '" + AppConfig.ORDER_PENDING_PAYMENT + "', '" + AppConfig.PAYMENT_CK + "', " +
                "?, 15000, ?, 'CHILD', GETDATE(), GETDATE())";
        try (Connection conn = orderDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"order_id"})) {
            ps.setInt(1, cId);
            ps.setInt(2, oId);
            ps.setBigDecimal(3, amount.subtract(new BigDecimal("15000")));
            ps.setBigDecimal(4, amount);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được order test");
    }

    private void hardDeletePaymentTransactions(int orderId) throws SQLException {
        try (Connection conn = paymentDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM payment_transactions WHERE order_id = ?")) {
            ps.setInt(1, orderId); ps.executeUpdate();
        }
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM order_items WHERE order_id = ?")) {
                    ps.setInt(1, orderId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM orders WHERE order_id = ?")) {
                    ps.setInt(1, orderId); ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); throw e;
            }
        }
    }

    private int createCategory(String name) throws SQLException {
        try (Connection conn = categoryDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (name, slug, is_active) VALUES (?, ?, 1)",
                     new String[]{"category_id"})) {
            ps.setString(1, name);
            ps.setString(2, "aoa-" + System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Không tạo được category");
    }

    private int createProduct(int ownerId, int catId, String name) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO products (owner_id, category_id, name, description, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, '', 'ACTIVE', GETDATE(), GETDATE())",
                     new String[]{"product_id"})) {
            ps.setInt(1, ownerId); ps.setInt(2, catId);
            ps.setString(3, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Không tạo được product");
    }

    private int createVariant(int productId, String sku, BigDecimal price, int stock) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO product_variants (product_id, sku, variant_label, price, stock_quantity, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, 1, GETDATE(), GETDATE())",
                    new String[]{"variant_id"})) {
            ps.setInt(1, productId); ps.setString(2, sku); ps.setString(3, "1kg");
            ps.setBigDecimal(4, price); ps.setInt(5, stock);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Không tạo được variant");
    }

    private void hardDeleteVariant(int variantId) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM product_variants WHERE variant_id = ?")) {
            ps.setInt(1, variantId); ps.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
            ps.setInt(1, productId); ps.executeUpdate();
        }
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
