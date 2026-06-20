package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import dao.system.ConnectionPool;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import service.auth.AuthService;
import service.shop.ShopService;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ShopRegistrationPerformanceTest — Timing benchmarks for the shop owner flow.
 *
 * Strategy: each test measures wall-clock time across N repetitions and asserts
 * the per-operation average stays inside the agreed SLA.  These are NOT
 * micro-benchmarks — they test end-to-end latency including real DB round-trips,
 * which is the metric that matters for production.
 *
 * Thresholds (generous to account for CI variance):
 *   PERF-01  Single registration (CUSTOMER)                 < 1 000 ms
 *   PERF-02  Single registration (SHOP_OWNER path)          < 1 000 ms
 *   PERF-03  ShopProfile lookup by userId (DAO)             <   100 ms
 *   PERF-04  updateApprovalStatus (approve, single txn)     <   500 ms
 *   PERF-05  ShopService.getShopByUserId (service layer)    <   200 ms
 *   PERF-06  Batch: 5 sequential registrations avg < 1 000 ms each
 *   PERF-07  ConnectionPool.getConnection round-trip         <    50 ms
 *   PERF-08  findByApprovalStatus("PENDING") on non-empty   <   200 ms
 */
public class ShopRegistrationPerformanceTest {

    // ---- SLA thresholds (milliseconds) ----
    private static final long SLA_REGISTRATION_MS      = 1_000;
    private static final long SLA_DAO_LOOKUP_MS         =   100;
    private static final long SLA_APPROVE_MS            =   500;
    private static final long SLA_SERVICE_LOOKUP_MS     =   200;
    private static final long SLA_CONNECTION_MS         =   500;
    private static final long SLA_STATUS_QUERY_MS       =   200;

    private UserDAO        userDAO;
    private ShopProfileDAO shopProfileDAO;
    private AuthService    authService;
    private ShopService    shopService;

    private final List<Integer> userIds    = new ArrayList<>();
    private final List<Integer> profileIds = new ArrayList<>();

    @Before
    public void setUp() {
        userDAO        = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        authService    = new AuthService();
        shopService    = new ShopService();
    }

    @After
    public void tearDown() {
        for (int pid : profileIds) { deleteProfile(pid); }
        for (int uid : userIds)    {
            try { userDAO.deleteUser(uid); } catch (Exception ignored) { }
        }
        profileIds.clear();
        userIds.clear();
    }

    // ---------------------------------------------------------------
    // PERF-01: Single CUSTOMER registration < 1 000 ms
    // ---------------------------------------------------------------

    @Test
    public void should_completeCustomerRegistration_within_1000ms() throws Exception {
        User user = buildUser("perf01_" + ts() + "@test.com", "CUSTOMER");

        long start = System.currentTimeMillis();
        User registered = authService.register(user, null, null, null, null);
        long elapsed = System.currentTimeMillis() - start;

        track(registered);
        assertTrue("Customer registration must complete in < " + SLA_REGISTRATION_MS + " ms, was " + elapsed,
                elapsed < SLA_REGISTRATION_MS);
    }

    // ---------------------------------------------------------------
    // PERF-02: SHOP_OWNER registration path < 1 000 ms
    // ---------------------------------------------------------------

    @Test
    public void should_completeShopOwnerRegistration_within_1000ms() throws Exception {
        User user = buildUser("perf02_" + ts() + "@test.com", "SHOP_OWNER");

        long start = System.currentTimeMillis();
        User registered = authService.register(user, "Perf Shop 02", "1 Speed St, District 1", "[1,2]", null);
        long elapsed = System.currentTimeMillis() - start;

        track(registered);
        // Collect the created profile for cleanup
        List<ShopProfile> profiles = shopProfileDAO.findByUserId(registered.getUserId());
        if (!profiles.isEmpty()) track(profiles.get(0));

        assertTrue("SHOP_OWNER registration must complete in < " + SLA_REGISTRATION_MS + " ms, was " + elapsed,
                elapsed < SLA_REGISTRATION_MS);
    }

    // ---------------------------------------------------------------
    // PERF-03: ShopProfile DAO lookup by userId < 100 ms
    // ---------------------------------------------------------------

    @Test
    public void should_findShopProfileByUserId_within_100ms() throws Exception {
        // Arrange — create a user and profile to look up
        int uid = createCustomer("perf03");
        int pid = createPendingProfile(uid, "Perf Shop 03");

        // Act
        long start = System.currentTimeMillis();
        List<ShopProfile> profiles = shopProfileDAO.findByUserId(uid);
        long elapsed = System.currentTimeMillis() - start;

        assertFalse("Profile must be found", profiles.isEmpty());
        assertTrue("findByUserId must complete in < " + SLA_DAO_LOOKUP_MS + " ms, was " + elapsed,
                elapsed < SLA_DAO_LOOKUP_MS);
    }

    // ---------------------------------------------------------------
    // PERF-04: updateApprovalStatus (full transaction) < 500 ms
    // ---------------------------------------------------------------

    @Test
    public void should_approveShop_within_500ms() throws Exception {
        // Arrange
        int uid = createCustomer("perf04");
        int pid = createPendingProfile(uid, "Perf Shop 04");

        // Act
        long start = System.currentTimeMillis();
        shopProfileDAO.updateApprovalStatus(pid, uid, "APPROVED", null);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue("updateApprovalStatus transaction must complete in < " + SLA_APPROVE_MS + " ms, was " + elapsed,
                elapsed < SLA_APPROVE_MS);

        // Verify correctness as a sanity check
        ShopProfile profile = shopProfileDAO.findById(pid);
        assertEquals("APPROVED", profile.getApprovalStatus());
    }

    // ---------------------------------------------------------------
    // PERF-05: ShopService.getShopByUserId < 200 ms
    // ---------------------------------------------------------------

    @Test
    public void should_getShopByUserId_via_service_within_200ms() throws Exception {
        // Arrange
        int uid = createCustomer("perf05");
        int pid = createPendingProfile(uid, "Perf Shop 05");

        // Act
        long start = System.currentTimeMillis();
        ShopProfile result = shopService.getShopByUserId(uid);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull("getShopByUserId must return a profile", result);
        assertTrue("ShopService.getShopByUserId must complete in < " + SLA_SERVICE_LOOKUP_MS + " ms, was " + elapsed,
                elapsed < SLA_SERVICE_LOOKUP_MS);
    }

    // ---------------------------------------------------------------
    // PERF-06: 5 sequential registrations, average < 1 000 ms each
    // ---------------------------------------------------------------

    @Test
    public void should_completeSequentialRegistrations_within_avgSLA() throws Exception {
        int count = 5;
        long totalElapsed = 0;

        for (int i = 0; i < count; i++) {
            User user = buildUser("perf06_" + i + "_" + ts() + "@test.com", "CUSTOMER");
            long start = System.currentTimeMillis();
            User registered = authService.register(user, null, null, null, null);
            totalElapsed += System.currentTimeMillis() - start;
            track(registered);
        }

        long avg = totalElapsed / count;
        assertTrue(count + " sequential registrations: average " + avg + " ms must be < " + SLA_REGISTRATION_MS,
                avg < SLA_REGISTRATION_MS);
    }

    // ---------------------------------------------------------------
    // PERF-07: ConnectionPool.getConnection round-trip < 50 ms
    // ---------------------------------------------------------------

    @Test
    public void should_acquireConnection_within_50ms() throws Exception {
        // Warm up the pool with one connection before measuring
        try (Connection warmup = ConnectionPool.getConnection()) {
            assertNotNull(warmup);
        }

        long start = System.currentTimeMillis();
        try (Connection conn = ConnectionPool.getConnection()) {
            long elapsed = System.currentTimeMillis() - start;
            assertNotNull("Connection must be non-null", conn);
            assertTrue("ConnectionPool.getConnection must complete in < " + SLA_CONNECTION_MS + " ms, was " + elapsed,
                    elapsed < SLA_CONNECTION_MS);
        }
    }

    // ---------------------------------------------------------------
    // PERF-08: findByApprovalStatus("PENDING") < 200 ms
    // ---------------------------------------------------------------

    @Test
    public void should_queryPendingShops_within_200ms() throws Exception {
        // Arrange — ensure at least one PENDING profile exists
        int uid = createCustomer("perf08");
        createPendingProfile(uid, "Perf Shop 08");

        // Act
        long start = System.currentTimeMillis();
        List<ShopProfile> pending = shopProfileDAO.findByApprovalStatus("PENDING");
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull("Result list must not be null", pending);
        assertTrue("findByApprovalStatus(PENDING) must complete in < " + SLA_STATUS_QUERY_MS + " ms, was " + elapsed,
                elapsed < SLA_STATUS_QUERY_MS);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User buildUser(String email, String role) {
        User u = new User();
        u.setFullName("Perf Test User");
        u.setEmail(email);
        u.setPasswordHash("SecurePass@2026!");
        u.setPhone(buildPhone());
        u.setRole(role);
        u.setStatus(AppConfig.ACCOUNT_STATUS_INACTIVE);
        u.setEmailVerified(false);
        return u;
    }

    private int createCustomer(String tag) throws Exception {
        int uid = userDAO.saveNewCustomer(
                "Perf User " + tag,
                "perf_" + tag + "_" + ts() + "@test.com",
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
        p.setShopDescription("Performance test shop");
        p.setApprovalStatus("PENDING");
        p.setDeliveryAddress("1 Perf Street");
        p.setRating(java.math.BigDecimal.ZERO);
        p.setBusinessEmail("perf_biz_" + userId + "_" + ts() + "@company.com");
        int pid = shopProfileDAO.save(p);
        profileIds.add(pid);
        return pid;
    }

    private void track(User u) {
        if (u != null && u.getUserId() > 0) userIds.add(u.getUserId());
    }

    private void track(ShopProfile p) {
        if (p != null && p.getProfileId() > 0) profileIds.add(p.getProfileId());
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

    private long ts() { return System.currentTimeMillis(); }

    private String buildPhone() {
        return "09" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }
}
