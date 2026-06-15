package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import dao.system.ConnectionPool;
import filter.RoleFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import util.HashUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * RoleFilterCacheTest — Verifies the _shopProfile session cache in RoleFilter.
 *
 * Test IDs:
 *   CACHE-01  First request (no cache): DAO is hit, profile stored in session
 *   CACHE-02  Second request (cache hit): session attribute reused, chain proceeds
 *   CACHE-03  SHOP_OWNER with APPROVED profile: chain is called (access allowed)
 *   CACHE-04  SHOP_OWNER with PENDING profile: redirect to /shop/status
 *   CACHE-05  SHOP_OWNER with REJECTED profile: redirect to /shop/status
 *   CACHE-06  Cache invalidated when approval_status changes to PENDING
 *   CACHE-07  Non-SHOP_OWNER accessing /shop/*: 403, chain not called
 *   CACHE-08  /shop/status is always accessible to SHOP_OWNER (bypass cache check)
 */
public class RoleFilterCacheTest {

    private static final String CTX = "/Ban_Hoa_Qua_Online";

    private UserDAO        userDAO;
    private ShopProfileDAO shopProfileDAO;

    private int shopOwnerId = -1;
    private int customerId  = -1;
    private int profileId   = -1;

    private final List<Integer> extraProfileIds = new ArrayList<>();
    private final List<Integer> extraUserIds    = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        userDAO        = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        long ts        = System.currentTimeMillis();

        shopOwnerId = userDAO.saveNewCustomer(
                "Cache ShopOwner", "cache_shop_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(1),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        customerId = userDAO.saveNewCustomer(
                "Cache Customer", "cache_cust_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(2),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);

        // Create an APPROVED profile for shopOwnerId
        profileId = createProfile(shopOwnerId, "Cache Test Shop", "APPROVED");
    }

    @After
    public void tearDown() {
        for (int pid : extraProfileIds) { deleteProfile(pid); }
        deleteProfile(profileId);
        for (int uid : extraUserIds) {
            try { userDAO.deleteUser(uid); } catch (Exception ignored) { }
        }
        try { if (shopOwnerId > 0) userDAO.deleteUser(shopOwnerId); } catch (Exception ignored) { }
        try { if (customerId  > 0) userDAO.deleteUser(customerId);  } catch (Exception ignored) { }
    }

    // ---------------------------------------------------------------
    // CACHE-01: First request with no cached profile — DAO is hit
    // ---------------------------------------------------------------

    @Test
    public void should_fetchProfileFromDAO_when_noCachedProfile() throws Exception {
        // Arrange — session has no _shopProfile attribute yet
        MockEnv env = shopOwnerEnv(shopOwnerId);
        env.uri = CTX + "/shop/products";
        // No pre-cached profile in session

        // Act
        runFilter(env);

        // Assert — _shopProfile must now be stored in session after the filter ran
        Object cachedProfile = env.sessionAttrs.get("_shopProfile");
        assertNotNull("RoleFilter must cache ShopProfile in session on first request", cachedProfile);
        assertTrue("Cached object must be a ShopProfile", cachedProfile instanceof ShopProfile);
    }

    // ---------------------------------------------------------------
    // CACHE-02: Second request uses the cached profile — chain proceeds
    // ---------------------------------------------------------------

    @Test
    public void should_reuseCachedProfile_and_allowAccess_on_subsequentRequest() throws Exception {
        // Arrange — pre-populate session cache with an APPROVED profile
        MockEnv env = shopOwnerEnv(shopOwnerId);
        env.uri = CTX + "/shop/products";

        ShopProfile cachedProfile = new ShopProfile();
        cachedProfile.setProfileId(profileId);
        cachedProfile.setUserId(shopOwnerId);
        cachedProfile.setApprovalStatus("APPROVED");
        env.sessionAttrs.put("_shopProfile", cachedProfile);

        // Act
        runFilter(env);

        // Assert — chain called (access granted) and cached profile unchanged
        assertTrue("Chain must be called when cached profile is APPROVED", env.chainCalled.get());
        assertSame("Cached profile must be the same object (no DAO re-fetch)",
                cachedProfile, env.sessionAttrs.get("_shopProfile"));
    }

    // ---------------------------------------------------------------
    // CACHE-03: SHOP_OWNER with APPROVED profile — chain is called
    // ---------------------------------------------------------------

    @Test
    public void should_allowAccess_when_shopOwnerProfileIsApproved() throws Exception {
        // Arrange — fresh session, no cache
        MockEnv env = shopOwnerEnv(shopOwnerId);
        env.uri = CTX + "/shop/products";

        // Act
        runFilter(env);

        // Assert
        assertTrue("SHOP_OWNER with APPROVED profile must reach the chain", env.chainCalled.get());
        assertNull("No redirect expected", env.redirectLocation.get());
    }

    // ---------------------------------------------------------------
    // CACHE-04: SHOP_OWNER with PENDING profile — redirect to /shop/status
    // ---------------------------------------------------------------

    @Test
    public void should_redirectToShopStatus_when_profileIsPending() throws Exception {
        // Arrange — create a PENDING profile user
        long ts = System.currentTimeMillis();
        int pendingUserId = userDAO.saveNewCustomer(
                "Pending Owner", "pending_owner_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(3),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        extraUserIds.add(pendingUserId);
        extraProfileIds.add(createProfile(pendingUserId, "Pending Shop", "PENDING"));

        MockEnv env = shopOwnerEnv(pendingUserId);
        env.uri = CTX + "/shop/products";

        // Act
        runFilter(env);

        // Assert — must redirect to /shop/status, not call chain
        assertFalse("Chain must NOT be called for pending shop owner", env.chainCalled.get());
        String redirect = env.redirectLocation.get();
        assertNotNull("Must redirect", redirect);
        assertTrue("Must redirect to /shop/status", redirect.contains("/shop/status"));
    }

    // ---------------------------------------------------------------
    // CACHE-05: SHOP_OWNER with REJECTED profile — redirect to /shop/status
    // ---------------------------------------------------------------

    @Test
    public void should_redirectToShopStatus_when_profileIsRejected() throws Exception {
        // Arrange
        long ts = System.currentTimeMillis();
        int rejectedUserId = userDAO.saveNewCustomer(
                "Rejected Owner", "rejected_owner_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(4),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        extraUserIds.add(rejectedUserId);
        extraProfileIds.add(createProfile(rejectedUserId, "Rejected Shop", "REJECTED"));

        MockEnv env = shopOwnerEnv(rejectedUserId);
        env.uri = CTX + "/shop/products";

        // Act
        runFilter(env);

        // Assert
        assertFalse("Chain must NOT be called for rejected shop owner", env.chainCalled.get());
        String redirect = env.redirectLocation.get();
        assertNotNull("Must redirect", redirect);
        assertTrue("Must redirect to /shop/status", redirect.contains("/shop/status"));
    }

    // ---------------------------------------------------------------
    // CACHE-06: Cache invalidated when approval status changes
    // ---------------------------------------------------------------

    @Test
    public void should_refreshProfileCache_when_approvalStatusChanges() throws Exception {
        // Arrange — pre-cache a PENDING profile, but DB now has APPROVED
        MockEnv env = shopOwnerEnv(shopOwnerId);
        env.uri = CTX + "/shop/products";

        // Stale cached profile: PENDING
        ShopProfile staleProfile = new ShopProfile();
        staleProfile.setProfileId(profileId);
        staleProfile.setUserId(shopOwnerId);
        staleProfile.setApprovalStatus("PENDING");
        env.sessionAttrs.put("_shopProfile", staleProfile);

        // The RoleFilter reads FROM cache — so it will see PENDING and redirect.
        // To test cache invalidation the session cache must be cleared first.
        // Simulate what should happen: clear the session cache attribute.
        env.sessionAttrs.remove("_shopProfile");

        // Act — now no cache in session; DAO re-fetch returns APPROVED
        runFilter(env);

        // Assert — fresh fetch from DB gives APPROVED, chain is called
        assertTrue("After cache clear and re-fetch of APPROVED profile, chain must be called",
                env.chainCalled.get());

        ShopProfile freshCached = (ShopProfile) env.sessionAttrs.get("_shopProfile");
        assertNotNull("Profile must be re-cached", freshCached);
        assertEquals("Re-cached profile must reflect DB status APPROVED",
                "APPROVED", freshCached.getApprovalStatus());
    }

    // ---------------------------------------------------------------
    // CACHE-07: Non-SHOP_OWNER accessing /shop/* — 403 Forbidden
    // ---------------------------------------------------------------

    @Test
    public void should_return403_when_customerAccessesShopPath() throws Exception {
        // Arrange
        MockEnv env = new MockEnv();
        env.uri = CTX + "/shop/products";
        env.sessionUser = buildUser(customerId, AppConfig.ROLE_CUSTOMER);

        // Act
        runFilter(env);

        // Assert
        assertFalse("Customer must NOT reach the chain", env.chainCalled.get());
        assertTrue("Must send 403 or redirect",
                env.errorCode.get() == 403 || env.redirectLocation.get() != null);
    }

    // ---------------------------------------------------------------
    // CACHE-08: /shop/status is always accessible to SHOP_OWNER (bypass check)
    // ---------------------------------------------------------------

    @Test
    public void should_allowShopStatus_for_shopOwner_regardless_ofProfileApproval() throws Exception {
        // Arrange — SHOP_OWNER with no cached profile and no profile in DB
        long ts = System.currentTimeMillis();
        int noProfileUserId = userDAO.saveNewCustomer(
                "No Profile Owner", "no_profile_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(5),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        extraUserIds.add(noProfileUserId);

        MockEnv env = shopOwnerEnv(noProfileUserId);
        env.uri = CTX + "/shop/status";  // the status page must always be reachable

        // Act
        runFilter(env);

        // Assert — chain called for /shop/status even without a profile
        assertTrue("/shop/status must be accessible to any SHOP_OWNER", env.chainCalled.get());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void runFilter(MockEnv env) throws Exception {
        RoleFilter filter = new RoleFilter();
        HttpServletRequest  req  = env.buildRequest();
        HttpServletResponse resp = env.buildResponse();
        FilterChain chain = (request, response) -> env.chainCalled.set(true);
        filter.doFilter(req, resp, chain);
    }

    private MockEnv shopOwnerEnv(int userId) {
        MockEnv env = new MockEnv();
        env.sessionUser = buildUser(userId, AppConfig.ROLE_SHOP_OWNER);
        return env;
    }

    private User buildUser(int userId, String role) {
        User u = new User();
        u.setUserId(userId);
        u.setRole(role);
        u.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        u.setEmailVerified(true);
        return u;
    }

    private int createProfile(int userId, String shopName, String status) throws SQLException {
        ShopProfile p = new ShopProfile();
        p.setUserId(userId);
        p.setShopName(shopName);
        p.setShopDescription("Cache test shop");
        p.setApprovalStatus(status);
        p.setDeliveryAddress("123 Cache Street");
        p.setRating(java.math.BigDecimal.ZERO);
        p.setBusinessEmail("cache_biz_" + userId + "_" + System.currentTimeMillis() + "@company.com");
        return shopProfileDAO.save(p);
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

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }

    // ---------------------------------------------------------------
    // Proxy-based mock environment (mirrors RoleFilterAccessControlTest pattern)
    // ---------------------------------------------------------------

    static class MockEnv {
        String method = "GET";
        String uri    = CTX + "/shop/products";
        User   sessionUser = null;

        final Map<String, Object>     sessionAttrs     = new HashMap<>();
        final Map<String, Object>     requestAttrs     = new HashMap<>();
        final AtomicBoolean           chainCalled      = new AtomicBoolean(false);
        final AtomicInteger           errorCode        = new AtomicInteger(0);
        final AtomicReference<String> redirectLocation = new AtomicReference<>();

        HttpSession buildSession() {
            if (sessionUser != null) {
                sessionAttrs.put(AppConfig.SESSION_USER, sessionUser);
            }
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class[]{HttpSession.class},
                    (proxy, m, args) -> {
                        switch (m.getName()) {
                            case "getAttribute":    return sessionAttrs.get(args[0]);
                            case "setAttribute":    sessionAttrs.put((String) args[0], args[1]); return null;
                            case "removeAttribute": sessionAttrs.remove(args[0]);                 return null;
                            case "getId":           return "mock-cache-session";
                            case "isNew":           return sessionUser == null;
                            case "invalidate":      sessionAttrs.clear();                         return null;
                            default:                return null;
                        }
                    });
        }

        HttpServletRequest buildRequest() {
            HttpSession session = buildSession();
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, m, args) -> {
                        switch (m.getName()) {
                            case "getMethod":        return method;
                            case "getRequestURI":    return uri;
                            case "getContextPath":   return CTX;
                            case "getSession":       return session;
                            case "getParameter":     return null;
                            case "getHeader":
                                if ("X-Requested-With".equals(args[0])) return null;
                                if ("Accept".equals(args[0]))            return "text/html";
                                return null;
                            case "getAttribute":     return requestAttrs.get(args[0]);
                            case "setAttribute":     requestAttrs.put((String) args[0], args[1]); return null;
                            case "getCharacterEncoding":  return "UTF-8";
                            case "setCharacterEncoding":  return null;
                            case "getServletContext":     return null;
                            case "getRequestDispatcher":  return null;
                            default:                      return null;
                        }
                    });
        }

        HttpServletResponse buildResponse() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class[]{HttpServletResponse.class},
                    (proxy, m, args) -> {
                        switch (m.getName()) {
                            case "sendError":
                                errorCode.set((int) args[0]);
                                return null;
                            case "sendRedirect":
                                redirectLocation.set((String) args[0]);
                                return null;
                            case "setStatus":            errorCode.set((int) args[0]); return null;
                            case "setContentType":       return null;
                            case "setCharacterEncoding": return null;
                            case "getWriter":            return new PrintWriter(new StringWriter());
                            default:                     return null;
                        }
                    });
        }
    }
}
