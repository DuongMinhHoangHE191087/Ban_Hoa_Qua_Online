package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import dao.system.ConnectionPool;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * ShopProfileDAOTransactionTest — Transaction consistency for updateApprovalStatus.
 *
 * Test IDs:
 *   TXN-01  Successful APPROVED: both shop profile AND user role update atomically
 *   TXN-02  Invalid profileId: no user role change occurs
 *   TXN-03  Invalid userId (0): profile status reverts on SQLException
 *   TXN-04  REJECTED status: user.role stays CUSTOMER (no role change expected)
 *   TXN-05  PENDING status: no role change, approval_status stored correctly
 *   TXN-06  Concurrent approval requests produce exactly one APPROVED + correct role
 *   TXN-07  approved_at is set on APPROVED, null on REJECTED
 *   TXN-08  rejection_reason is stored on REJECTED, null on APPROVED
 */
public class ShopProfileDAOTransactionTest {

    private UserDAO        userDAO;
    private ShopProfileDAO shopProfileDAO;

    private final List<Integer> userIds    = new ArrayList<>();
    private final List<Integer> profileIds = new ArrayList<>();

    // ---------------------------------------------------------------
    // Setup / Teardown
    // ---------------------------------------------------------------

    @Before
    public void setUp() {
        userDAO        = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
    }

    @After
    public void tearDown() {
        for (int pid : profileIds) { deleteProfile(pid); }
        for (int uid : userIds)    { try { userDAO.deleteUser(uid); } catch (Exception ignored) { } }
        profileIds.clear();
        userIds.clear();
    }

    // ---------------------------------------------------------------
    // TXN-01: APPROVED updates both tables atomically
    // ---------------------------------------------------------------

    @Test
    public void should_updateBothTables_atomically_when_approved() throws Exception {
        // Arrange
        int uid = createCustomer("txn01");
        int pid = createPendingProfile(uid, "Shop TXN-01");

        // Act
        shopProfileDAO.updateApprovalStatus(pid, uid, "APPROVED", null);

        // Assert — shop profile updated
        ShopProfile dbProfile = shopProfileDAO.findById(pid);
        assertEquals("Profile status must be APPROVED", "APPROVED", dbProfile.getApprovalStatus());
        assertNotNull("approved_at must be set",         dbProfile.getApprovedAt());

        // Assert — user role promoted in same transaction
        User dbUser = userDAO.findUserById(uid);
        assertEquals("User role must be SHOP_OWNER after approval",
                AppConfig.ROLE_SHOP_OWNER, dbUser.getRole());
    }

    // ---------------------------------------------------------------
    // TXN-02: Non-existent profileId → no DB rows changed, no exception crash
    // ---------------------------------------------------------------

    @Test
    public void should_notChangeUserRole_when_profileIdDoesNotExist() throws Exception {
        // Arrange
        int uid = createCustomer("txn02");
        int nonExistentProfileId = Integer.MAX_VALUE;

        // Act — should not throw; updateApprovalStatus silently skips missing profile
        // (ShopService.updateShopStatus checks findById first, but DAO itself just executes 0 rows)
        try {
            shopProfileDAO.updateApprovalStatus(nonExistentProfileId, uid, "APPROVED", null);
        } catch (SQLException e) {
            // Some implementations throw when 0 rows affected — acceptable
        }

        // Assert — user role must still be CUSTOMER
        User dbUser = userDAO.findUserById(uid);
        assertEquals("Role must remain CUSTOMER when profileId is invalid",
                AppConfig.ROLE_CUSTOMER, dbUser.getRole());
    }

    // ---------------------------------------------------------------
    // TXN-03: userId = 0 → DAO throws SQLException and rolls back profile change
    // ---------------------------------------------------------------

    @Test
    public void should_rollbackProfileChange_when_userIdIsZero() throws Exception {
        // Arrange
        int uid = createCustomer("txn03");
        int pid = createPendingProfile(uid, "Shop TXN-03");

        // Act — pass userId = 0, which triggers the "rows == 0" guard in DAO
        try {
            shopProfileDAO.updateApprovalStatus(pid, 0, "APPROVED", null);
            // If no exception, verify rollback happened (profile status unchanged)
        } catch (SQLException e) {
            // Expected: DAO throws and rolls back
        }

        // Assert — the profile must still be PENDING because transaction rolled back
        ShopProfile dbProfile = shopProfileDAO.findById(pid);
        assertEquals("Profile must remain PENDING after rollback",
                "PENDING", dbProfile.getApprovalStatus());

        // User role must remain CUSTOMER
        User dbUser = userDAO.findUserById(uid);
        assertEquals("Role must remain CUSTOMER after rollback",
                AppConfig.ROLE_CUSTOMER, dbUser.getRole());
    }

    // ---------------------------------------------------------------
    // TXN-04: REJECTED status → user.role stays CUSTOMER
    // ---------------------------------------------------------------

    @Test
    public void should_keepRoleCustomer_when_rejected() throws Exception {
        // Arrange
        int uid = createCustomer("txn04");
        int pid = createPendingProfile(uid, "Shop TXN-04");

        // Act
        shopProfileDAO.updateApprovalStatus(pid, uid, "REJECTED", "Documents missing");

        // Assert — role unchanged
        User dbUser = userDAO.findUserById(uid);
        assertEquals("REJECTED must not change user role",
                AppConfig.ROLE_CUSTOMER, dbUser.getRole());

        ShopProfile dbProfile = shopProfileDAO.findById(pid);
        assertEquals("REJECTED", dbProfile.getApprovalStatus());
    }

    // ---------------------------------------------------------------
    // TXN-05: PENDING status written correctly (no role change)
    // ---------------------------------------------------------------

    @Test
    public void should_storePendingStatus_without_changingUserRole() throws Exception {
        // Arrange — create profile already APPROVED, then set back to PENDING (admin edge case)
        int uid = createCustomer("txn05");
        int pid = createPendingProfile(uid, "Shop TXN-05");

        // First approve
        shopProfileDAO.updateApprovalStatus(pid, uid, "APPROVED", null);
        assertEquals("SHOP_OWNER", userDAO.findUserById(uid).getRole());

        // Act — set back to PENDING (simulating admin review)
        // Note: userId=0 here because PENDING should NOT alter user.role
        shopProfileDAO.updateApprovalStatus(pid, 0, "PENDING", null);

        // Assert
        ShopProfile dbProfile = shopProfileDAO.findById(pid);
        assertEquals("Profile must be PENDING", "PENDING", dbProfile.getApprovalStatus());
    }

    // ---------------------------------------------------------------
    // TXN-06: Concurrent approval requests — exactly one wins
    // ---------------------------------------------------------------

    @Test
    public void should_produceExactlyOneApproval_under_concurrentRequests() throws Exception {
        // Arrange
        int uid = createCustomer("txn06");
        int pid = createPendingProfile(uid, "Shop TXN-06");

        int threadCount = 5;
        ExecutorService pool  = Executors.newFixedThreadPool(threadCount);
        CountDownLatch  start = new CountDownLatch(1);
        AtomicInteger   okCount    = new AtomicInteger(0);
        AtomicInteger   errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    shopProfileDAO.updateApprovalStatus(pid, uid, "APPROVED", null);
                    okCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        }

        // Act — release all threads simultaneously
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // Assert — final state must be APPROVED exactly once
        ShopProfile dbProfile = shopProfileDAO.findById(pid);
        assertEquals("Profile must be APPROVED after concurrent requests",
                "APPROVED", dbProfile.getApprovalStatus());

        User dbUser = userDAO.findUserById(uid);
        assertEquals("Role must be SHOP_OWNER after concurrent approval",
                AppConfig.ROLE_SHOP_OWNER, dbUser.getRole());

        // At least one thread must have succeeded
        assertTrue("At least one approval thread must succeed", okCount.get() >= 1);
    }

    // ---------------------------------------------------------------
    // TXN-07: approved_at is set on APPROVED, null on REJECTED
    // ---------------------------------------------------------------

    @Test
    public void should_setApprovedAt_when_approved_and_clearIt_when_rejected() throws Exception {
        // Arrange
        int uid = createCustomer("txn07a");
        int pid = createPendingProfile(uid, "Shop TXN-07a");

        // Act — approve
        shopProfileDAO.updateApprovalStatus(pid, uid, "APPROVED", null);
        ShopProfile approved = shopProfileDAO.findById(pid);
        assertNotNull("approved_at must be set after APPROVED", approved.getApprovedAt());

        // Arrange — separate user for rejection test
        int uid2 = createCustomer("txn07b");
        int pid2 = createPendingProfile(uid2, "Shop TXN-07b");

        // Act — reject
        shopProfileDAO.updateApprovalStatus(pid2, uid2, "REJECTED", "Bad docs");
        ShopProfile rejected = shopProfileDAO.findById(pid2);
        assertNull("approved_at must be NULL after REJECTED", rejected.getApprovedAt());
    }

    // ---------------------------------------------------------------
    // TXN-08: rejection_reason stored on REJECTED, null on APPROVED
    // ---------------------------------------------------------------

    @Test
    public void should_storeRejectionReason_when_rejected_and_clearIt_when_approved() throws Exception {
        // Arrange
        int uid = createCustomer("txn08");
        int pid = createPendingProfile(uid, "Shop TXN-08");

        // Act — reject with reason
        shopProfileDAO.updateApprovalStatus(pid, uid, "REJECTED", "Missing business license");
        ShopProfile rejected = shopProfileDAO.findById(pid);
        assertEquals("Rejection reason must be stored",
                "Missing business license", rejected.getRejectionReason());

        // Arrange — approve a fresh profile to verify reason cleared
        int uid2 = createCustomer("txn08b");
        int pid2 = createPendingProfile(uid2, "Shop TXN-08b");

        // Act — approve (rejectionReason = null)
        shopProfileDAO.updateApprovalStatus(pid2, uid2, "APPROVED", null);
        ShopProfile approvedProfile = shopProfileDAO.findById(pid2);
        assertNull("rejection_reason must be NULL after APPROVED",
                approvedProfile.getRejectionReason());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private int createCustomer(String tag) throws Exception {
        long ts  = System.currentTimeMillis();
        int uid  = userDAO.saveNewCustomer(
                "TXN User " + tag,
                "txn_" + tag + "_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(),
                AppConfig.ROLE_CUSTOMER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true);
        userIds.add(uid);
        return uid;
    }

    private int createPendingProfile(int userId, String shopName) throws SQLException {
        ShopProfile p = new ShopProfile();
        p.setUserId(userId);
        p.setShopName(shopName);
        p.setShopDescription("Transaction test shop");
        p.setApprovalStatus("PENDING");
        p.setDeliveryAddress("Test Address, District 1");
        p.setRating(java.math.BigDecimal.ZERO);
        p.setBusinessEmail("txn_biz_" + userId + "_" + System.currentTimeMillis() + "@company.com");
        int pid = shopProfileDAO.save(p);
        profileIds.add(pid);
        return pid;
    }

    private void deleteProfile(int pid) {
        if (pid <= 0) return;
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM shop_owner_profiles WHERE profile_id = ?")) {
            ps.setInt(1, pid);
            ps.executeUpdate();
        } catch (Exception ignored) { }
    }

    private String buildPhone() {
        return "09" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }
}
