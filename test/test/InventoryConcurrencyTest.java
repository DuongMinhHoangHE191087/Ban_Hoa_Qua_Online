package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import model.entity.catalog.ProductVariant;
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
 * InventoryConcurrencyTest — Kiểm tra tính toàn vẹn tồn kho khi
 * nhiều buyer cùng lúc đặt đơn cho sản phẩm có stock hạn chế.
 *
 * Dùng database-level locking (UPDLOCK hint hoặc serializable isolation)
 * — không phải application-level lock.
 */
public class InventoryConcurrencyTest {

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private CategoryDAO categoryDAO;

    private int ownerId = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;

    @Before
    public void setUp() throws Exception {
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        categoryDAO = new CategoryDAO();

        long ts = System.currentTimeMillis();
        ownerId = userDAO.saveNewCustomer(
                "ICT Owner", "ict_owner_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(1),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        categoryId = createCategory("ICT Category " + ts);
        productId = createProduct(ownerId, categoryId, "ICT Product " + ts);
        variantId = createVariant(productId, "ICT-SKU-" + ts, new BigDecimal("50000"), 5);
    }

    @After
    public void tearDown() {
        try {
            if (variantId > 0) hardDeleteVariant(variantId);
            if (productId > 0) hardDeleteProduct(productId);
            if (categoryId > 0) categoryDAO.delete(categoryId);
            if (ownerId > 0) userDAO.deleteUser(ownerId);
        } catch (SQLException e) {
            System.err.println("[InventoryConcurrencyTest] Cleanup: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-INV-01: 10 concurrent reserve(qty=1) với stock=5
    //            Đúng 5 thành công, 5 thất bại, stock = 0
    // =========================================================

    @Test
    public void should_allow_exactly_stockCount_concurrent_reserves() throws InterruptedException {
        int initialStock = 5;
        int concurrentAttempts = 10;

        CountDownLatch ready = new CountDownLatch(concurrentAttempts);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(concurrentAttempts);
        for (int i = 0; i < concurrentAttempts; i++) {
            pool.submit(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                try {
                    reserveStockWithLock(variantId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertTrue("Thread pool phải kết thúc trong 30s", pool.awaitTermination(30, TimeUnit.SECONDS));

        int finalStock = getVariantStock(variantId);
        assertTrue("Stock không được âm (actual: " + finalStock + ")", finalStock >= 0);
        assertEquals("Stock phải về 0 sau khi tất cả success", 0, finalStock);
        assertEquals("Đúng " + initialStock + " thành công", initialStock, successCount.get());
        assertEquals("Đúng " + (concurrentAttempts - initialStock) + " thất bại",
                concurrentAttempts - initialStock, failCount.get());
    }

    // =========================================================
    // TC-INV-02: Reserve thành công khi stock đủ
    // =========================================================

    @Test
    public void should_succeed_when_stockSufficient() throws SQLException {
        int beforeStock = getVariantStock(variantId);
        assertTrue("Ban đầu phải có stock > 0", beforeStock > 0);

        reserveStockWithLock(variantId, 2);

        int afterStock = getVariantStock(variantId);
        assertEquals("Stock phải giảm đúng 2", beforeStock - 2, afterStock);
    }

    // =========================================================
    // TC-INV-03: Reserve thất bại khi stock = 0
    // =========================================================

    @Test
    public void should_fail_when_stockIsZero() throws SQLException {
        setStock(variantId, 0);

        boolean threw = false;
        try {
            reserveStockWithLock(variantId, 1);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Phải ném exception khi stock = 0", threw);
        assertEquals("Stock phải vẫn = 0", 0, getVariantStock(variantId));
    }

    // =========================================================
    // TC-INV-04: Reserve qty lớn hơn stock thất bại
    // =========================================================

    @Test
    public void should_fail_when_requestedQtyExceedsStock() throws SQLException {
        int currentStock = getVariantStock(variantId);
        boolean threw = false;
        try {
            reserveStockWithLock(variantId, currentStock + 1);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Phải ném exception khi qty > stock", threw);
        assertEquals("Stock không được thay đổi", currentStock, getVariantStock(variantId));
    }

    // =========================================================
    // TC-INV-05: Rollback reserve khi exception sau deduct
    // =========================================================

    @Test
    public void should_rollbackStock_when_exceptionAfterDeduct() throws SQLException {
        int beforeStock = getVariantStock(variantId);

        try (Connection conn = variantDAO.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Deduct stock
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE product_variants SET stock_quantity = stock_quantity - 1 " +
                        "WHERE variant_id = ? AND stock_quantity >= 1")) {
                    ps.setInt(1, variantId);
                    int rows = ps.executeUpdate();
                    if (rows == 0) throw new SQLException("Stock không đủ");
                }
                // Giả lập exception sau khi deduct
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
            }
        }

        int afterStock = getVariantStock(variantId);
        assertEquals("Stock phải được rollback về giá trị ban đầu", beforeStock, afterStock);
    }

    // =========================================================
    // Helpers — tất cả dùng UPDLOCK để an toàn concurrent
    // =========================================================

    private void reserveStockWithLock(int variantId, int qty) throws SQLException {
        try (Connection conn = variantDAO.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // UPDLOCK + ROWLOCK ngăn phantom reads trong SQL Server
                String lockSql = "SELECT stock_quantity FROM product_variants WITH (UPDLOCK, ROWLOCK) WHERE variant_id = ?";
                int currentStock;
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, variantId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); throw new SQLException("Variant không tồn tại"); }
                        currentStock = rs.getInt("stock_quantity");
                    }
                }
                if (currentStock < qty) {
                    conn.rollback();
                    throw new SQLException("Stock không đủ: có " + currentStock + ", cần " + qty);
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE variant_id = ?")) {
                    ps.setInt(1, qty);
                    ps.setInt(2, variantId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
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

    private void setStock(int variantId, int stock) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE product_variants SET stock_quantity = ? WHERE variant_id = ?")) {
            ps.setInt(1, stock);
            ps.setInt(2, variantId);
            ps.executeUpdate();
        }
    }

    private int createCategory(String name) throws SQLException {
        try (Connection conn = categoryDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (name, slug, description, is_active) VALUES (?, ?, '', 1)",
                     new String[]{"category_id"})) {
            ps.setString(1, name);
            ps.setString(2, "ict-" + System.currentTimeMillis());
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
                     "INSERT INTO products (owner_id, category_id, name, slug, description, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, '', 'ACTIVE', GETDATE(), GETDATE())",
                     new String[]{"product_id"})) {
            ps.setInt(1, ownerId);
            ps.setInt(2, categoryId);
            ps.setString(3, name);
            ps.setString(4, "ict-p-" + System.currentTimeMillis());
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
                     "INSERT INTO product_variants (product_id, sku, price, stock_quantity, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, 1, GETDATE(), GETDATE())",
                     new String[]{"variant_id"})) {
            ps.setInt(1, productId);
            ps.setString(2, sku);
            ps.setBigDecimal(3, price);
            ps.setInt(4, stock);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được variant");
    }

    private void hardDeleteVariant(int variantId) throws SQLException {
        try (Connection conn = variantDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM product_variants WHERE variant_id = ?")) {
            ps.setInt(1, variantId); ps.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM products WHERE product_id = ?")) {
            ps.setInt(1, productId); ps.executeUpdate();
        }
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
