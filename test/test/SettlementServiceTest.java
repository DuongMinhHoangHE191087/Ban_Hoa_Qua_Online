package test;

import dao.auth.UserDAO;
import dao.shop.SettlementDAO;
import model.entity.shop.ShopSettlement;
import service.shop.SettlementService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class SettlementServiceTest {

    private UserDAO userDAO;
    private SettlementDAO settlementDAO;
    private SettlementService settlementService;

    private int testOwnerId = -1;
    private int testSettlementId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        settlementDAO = new SettlementDAO();
        settlementService = new SettlementService();

        // 1. Create owner
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer("Set Test Owner", "set_owner_" + System.currentTimeMillis() + "@test.com", "pwd", ownerPhone, "SHOP_OWNER", "ACTIVE", true);
    }

    @After
    public void tearDown() {
        try {
            if (testSettlementId != -1) {
                hardDeleteSettlement(testSettlementId);
            }
            if (testOwnerId != -1) {
                userDAO.deleteUser(testOwnerId);
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    @Test
    public void testSettlementFlow() throws Exception {
        // Manually insert a settlement request for the owner
        testSettlementId = insertSettlement(testOwnerId, new BigDecimal("500000.00"), "PENDING");
        assertTrue(testSettlementId > 0);

        // Retrieve settlements
        List<ShopSettlement> settlements = settlementService.getSettlementsByOwner(testOwnerId);
        assertFalse(settlements.isEmpty());
        assertEquals(testSettlementId, settlements.get(0).getSettlementId());

        // Count all settlements
        int count = settlementService.countAllSettlements("PENDING");
        assertTrue(count > 0);

        // Mark paid
        settlementService.markPaid(testSettlementId);

        List<ShopSettlement> paidSettlements = settlementService.getAllSettlements("PAID", 1, 10);
        boolean found = false;
        for (ShopSettlement s : paidSettlements) {
            if (s.getSettlementId() == testSettlementId) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testRunAutoSettlementDoesNotThrow() throws Exception {
        // Trigger auto settlement logic (should run cleanly)
        int processed = settlementService.runAutoSettlement();
        assertTrue(processed >= 0);
    }

    private int insertSettlement(int ownerId, BigDecimal amount, String status) throws SQLException {
        String sql = "INSERT INTO shop_settlements (owner_id, period_start, period_end, gross_amount, net_amount, status, created_by) "
                   + "VALUES (?, GETDATE(), GETDATE(), ?, ?, ?, ?)";
        try (Connection conn = settlementDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ownerId);
            ps.setBigDecimal(2, amount);
            ps.setBigDecimal(3, amount);
            ps.setString(4, status);
            ps.setInt(5, ownerId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to insert settlement");
    }

    private void hardDeleteSettlement(int settlementId) throws SQLException {
        try (Connection conn = settlementDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM shop_settlements WHERE settlement_id = ?")) {
            ps.setInt(1, settlementId);
            ps.executeUpdate();
        }
    }
}
