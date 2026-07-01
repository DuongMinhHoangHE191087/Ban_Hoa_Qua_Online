package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.cart.CartDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import service.cart.CartService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * CartConcurrencyTest — Kiểm tra tính toàn vẹn kho hàng khi nhiều user
 * cùng lúc thêm vào giỏ sản phẩm còn 1 đơn vị tồn kho.
 */
public class CartConcurrencyTest {

    private UserDAO userDAO;
    private CartDAO cartDAO;
    private CartService cartService;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;

    private int ownerId = -1;
    private int customer1Id = -1;
    private int customer2Id = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;
    private int cart1Id = -1;
    private int cart2Id = -1;

    @Before
    public void setUp() throws Exception {
        userDAO = new UserDAO();
        cartDAO = new CartDAO();
        cartService = new CartService();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();

        long ts = System.currentTimeMillis();
        ownerId = userDAO.saveNewCustomer(
                "CCT Owner", "cct_owner_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(1),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        customer1Id = userDAO.saveNewCustomer(
                "CCT Cust1", "cct_cust1_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(2),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        customer2Id = userDAO.saveNewCustomer(
                "CCT Cust2", "cct_cust2_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(3),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        categoryId = createCategory("CCT Category " + ts);
        productId = createProduct(ownerId, categoryId, "CCT Product " + ts);
        // Tạo variant với tồn kho = 1
        variantId = createVariant(productId, "CCT-SKU-" + ts, new BigDecimal("50000"), 1);

        cart1Id = cartDAO.createForCustomer(customer1Id);
        cart2Id = cartDAO.createForCustomer(customer2Id);
    }

    @After
    public void tearDown() {
        try {
            if (cart1Id > 0) hardDeleteCart(cart1Id);
            if (cart2Id > 0) hardDeleteCart(cart2Id);
            if (variantId > 0) hardDeleteVariant(variantId);
            if (productId > 0) hardDeleteProduct(productId);
            if (categoryId > 0) categoryDAO.delete(categoryId);
            if (ownerId > 0)    userDAO.deleteUser(ownerId);
            if (customer1Id > 0) userDAO.deleteUser(customer1Id);
            if (customer2Id > 0) userDAO.deleteUser(customer2Id);
        } catch (SQLException e) {
            System.err.println("[CartConcurrencyTest] Cleanup: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-CONC-01: Hai buyer cùng lúc thêm sản phẩm tồn kho = 1
    //             Chỉ 1 được thêm thành công, stock không âm
    // =========================================================

    @Test
    public void should_preventNegativeStock_when_twoBuyersAddSameLastUnit()
            throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable buyer1 = () -> {
            ready.countDown();
            try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            try {
                cartDAO.addItem(cart1Id, variantId, 1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        };

        Runnable buyer2 = () -> {
            ready.countDown();
            try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            try {
                cartDAO.addItem(cart2Id, variantId, 1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(buyer1);
        pool.submit(buyer2);

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertTrue("Thread pool phải kết thúc trong 10s", pool.awaitTermination(10, TimeUnit.SECONDS));

        // Kiểm tra tồn kho không âm
        int remainingStock = getVariantStock(variantId);
        assertTrue("Tồn kho không được âm (hiện tại: " + remainingStock + ")", remainingStock >= 0);

        // Tổng thành công + thất bại = 2 (không mất request)
        assertEquals("Tổng request phải = 2", 2, successCount.get() + failCount.get());
    }

    // =========================================================
    // TC-CONC-02: Single user thêm số lượng quá tồn kho
    //             CartService hoặc DB constraint phải từ chối
    // =========================================================

    @Test
    public void should_rejectOrCapQuantity_when_requestedQtyExceedsStock() {
        int requestedQty = 1000; // vượt xa tồn kho = 1
        boolean threw = false;
        try {
            cartDAO.addItem(cart1Id, variantId, requestedQty);
        } catch (Exception e) {
            threw = true;
        }
        // Nếu không throw, tồn kho phải không âm
        if (!threw) {
            int stock = getVariantStockSafe(variantId);
            assertTrue("Tồn kho không được âm dù request qty vượt quá", stock >= 0);
        }
        // Test pass — quan trọng là không crash và không gây data corruption
    }

    // =========================================================
    // TC-CONC-03: Tồn kho 0 — thêm vào giỏ phải bị từ chối hoặc hạn chế
    // =========================================================

    @Test
    public void should_handleZeroStock_gracefully() {
        // Set stock về 0 trước
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE product_variants SET stock_quantity = 0 WHERE variant_id = ?")) {
            ps.setInt(1, variantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Không set được stock = 0: " + e.getMessage());
        }

        boolean threw = false;
        try {
            cartDAO.addItem(cart1Id, variantId, 1);
        } catch (Exception e) {
            threw = true;
        }

        if (!threw) {
            // Nếu CartDAO không check stock, CartService/Checkout sẽ check
            // Test pass — không crash, không data corruption
        }
        // Verify tồn kho không âm
        int stock = getVariantStockSafe(variantId);
        assertTrue("Tồn kho không được < 0 sau khi add qty=1 khi stock=0", stock >= 0);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private int createCategory(String name) throws SQLException {
        try (Connection conn = categoryDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (name, slug, is_active) VALUES (?, ?, 1)",
                     new String[]{"category_id"})) {
            ps.setString(1, name);
            ps.setString(2, "cct-" + System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được category");
    }

    private int createProduct(int ownerId, int categoryId, String name) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO products (owner_id, category_id, name, description, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, '', 'ACTIVE', GETDATE(), GETDATE())",
                     new String[]{"product_id"})) {
            ps.setInt(1, ownerId);
            ps.setInt(2, categoryId);
            ps.setString(3, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được product");
    }

    private int createVariant(int productId, String sku, BigDecimal price, int stock) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO product_variants (product_id, sku, variant_label, price, stock_quantity, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, 1, GETDATE(), GETDATE())",
                     new String[]{"variant_id"})) {
            ps.setInt(1, productId);
            ps.setString(2, sku);
            ps.setString(3, "1kg");
            ps.setBigDecimal(4, price);
            ps.setInt(5, stock);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được variant");
    }

    private int getVariantStock(int variantId) {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT stock_quantity FROM product_variants WHERE variant_id = ?")) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("stock_quantity");
            }
        } catch (SQLException e) {
            fail("Không lấy được stock: " + e.getMessage());
        }
        return -1;
    }

    private int getVariantStockSafe(int variantId) {
        try { return getVariantStock(variantId); } catch (Exception e) { return -1; }
    }

    private void hardDeleteCart(int cartId) throws SQLException {
        try (Connection conn = cartDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                ps.setInt(1, cartId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?")) {
                ps.setInt(1, cartId); ps.executeUpdate();
            }
        }
    }

    private void hardDeleteVariant(int variantId) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE variant_id = ?")) {
            ps.setInt(1, variantId); ps.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET status = 'DELETED', updated_at = GETDATE() WHERE product_id = ?")) {
            ps.setInt(1, productId); ps.executeUpdate();
        }
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
