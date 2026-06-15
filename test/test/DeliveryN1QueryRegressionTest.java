package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.order.OrderDAO;
import model.entity.order.Order;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * DeliveryN1QueryRegressionTest — Kiểm tra OrderDAO.findByIds() mới thêm:
 * Batch load orders bằng IN clause thay vì N+1 queries.
 */
public class DeliveryN1QueryRegressionTest {

    private UserDAO userDAO;
    private OrderDAO orderDAO;

    private int ownerId = -1;
    private int customerId = -1;
    private final List<Integer> testOrderIds = new ArrayList<>();

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        orderDAO = new OrderDAO();

        long ts = System.currentTimeMillis();
        ownerId = userDAO.saveNewCustomer(
                "DN1Owner_" + ts, "dn1_owner_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(1), AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        customerId = userDAO.saveNewCustomer(
                "DN1Cust_" + ts, "dn1_cust_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(2), AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        for (int i = 0; i < 5; i++) {
            testOrderIds.add(insertOrder(customerId, ownerId));
        }
    }

    @After
    public void tearDown() {
        try {
            for (int orderId : testOrderIds) {
                hardDeleteOrder(orderId);
            }
            if (ownerId > 0)    userDAO.deleteUser(ownerId);
            if (customerId > 0) userDAO.deleteUser(customerId);
        } catch (SQLException e) {
            System.err.println("[DeliveryN1QueryRegressionTest] Cleanup: " + e.getMessage());
        }
        testOrderIds.clear();
    }

    // =========================================================
    // TC-DN1-01: Batch load đúng số lượng orders
    // =========================================================

    @Test
    public void should_returnAllOrders_when_batchLoaded() throws SQLException {
        Map<Integer, Order> result = orderDAO.findByIds(testOrderIds);
        assertEquals("Phải trả về đúng " + testOrderIds.size() + " orders",
                testOrderIds.size(), result.size());

        for (int orderId : testOrderIds) {
            assertTrue("Map phải có orderId " + orderId, result.containsKey(orderId));
            assertNotNull("Order phải không null", result.get(orderId));
            assertEquals("customerId phải đúng", customerId, result.get(orderId).getCustomerId());
        }
    }

    // =========================================================
    // TC-DN1-02: List rỗng trả về map rỗng (không NPE)
    // =========================================================

    @Test
    public void should_returnEmptyMap_when_orderIdListIsEmpty() throws SQLException {
        Map<Integer, Order> result = orderDAO.findByIds(Collections.emptyList());
        assertNotNull("Phải trả về map không null", result);
        assertTrue("Map phải rỗng", result.isEmpty());
    }

    // =========================================================
    // TC-DN1-03: null input trả về map rỗng (không NPE)
    // =========================================================

    @Test
    public void should_returnEmptyMap_when_orderIdListIsNull() throws SQLException {
        Map<Integer, Order> result = orderDAO.findByIds(null);
        assertNotNull("Phải trả về map không null cho null input", result);
        assertTrue("Map phải rỗng", result.isEmpty());
    }

    // =========================================================
    // TC-DN1-04: Duplicate IDs trong input — map dedup tự động
    // =========================================================

    @Test
    public void should_handleDuplicateOrderIds() throws SQLException {
        if (testOrderIds.size() < 2) return;
        List<Integer> withDuplicates = new ArrayList<>(testOrderIds);
        withDuplicates.add(testOrderIds.get(0));
        withDuplicates.add(testOrderIds.get(1));

        Map<Integer, Order> result = orderDAO.findByIds(withDuplicates);
        assertEquals("Map phải deduplicate — kích thước bằng số ID unique",
                testOrderIds.size(), result.size());
    }

    // =========================================================
    // TC-DN1-05: ID không tồn tại — không có trong map
    // =========================================================

    @Test
    public void should_notReturnOrders_for_nonExistentIds() throws SQLException {
        List<Integer> mixed = new ArrayList<>(testOrderIds.subList(0, 2));
        mixed.add(9999991);
        mixed.add(9999992);

        Map<Integer, Order> result = orderDAO.findByIds(mixed);
        assertEquals("Chỉ trả về 2 ID hợp lệ", 2, result.size());
        assertFalse("ID không tồn tại không có trong map", result.containsKey(9999991));
        assertFalse("ID không tồn tại không có trong map", result.containsKey(9999992));
    }

    // =========================================================
    // TC-DN1-06: Batch load 1 ID — cũng hoạt động đúng
    // =========================================================

    @Test
    public void should_returnSingleOrder_when_onlyOneIdProvided() throws SQLException {
        int singleId = testOrderIds.get(0);
        Map<Integer, Order> result = orderDAO.findByIds(Collections.singletonList(singleId));
        assertEquals("Phải có đúng 1 entry", 1, result.size());
        assertNotNull("Order phải không null", result.get(singleId));
    }

    // =========================================================
    // Helpers
    // =========================================================

    private int insertOrder(int cId, int oId) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, owner_id, status, payment_method, subtotal_amount, " +
                "delivery_fee, final_amount, order_type, created_at, updated_at) " +
                "VALUES (?, ?, 'CONFIRMED', 'COD', 100000, 15000, 115000, 'CHILD', GETDATE(), GETDATE())";
        try (Connection conn = orderDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"order_id"})) {
            ps.setInt(1, cId);
            ps.setInt(2, oId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được order test");
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

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }
}
