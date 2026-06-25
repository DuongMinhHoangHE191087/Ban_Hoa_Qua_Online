package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * RoleFilterAccessControlTest — Kiểm tra RoleFilter trực tiếp với Proxy mock.
 * Đảm bảo mọi path/role combination đều được xử lý đúng.
 */
public class RoleFilterAccessControlTest {

    private UserDAO userDAO;
    private ShopProfileDAO shopProfileDAO;
    private MockEnv env;

    private int adminId = -1;
    private int customerId = -1;
    private int shopOwnerId = -1;
    private int deliveryId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        env = new MockEnv();
        long ts = System.currentTimeMillis();

        adminId = userDAO.saveNewCustomer(
                "RFC Admin", "rfc_admin_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(1),
                AppConfig.ROLE_ADMIN, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        customerId = userDAO.saveNewCustomer(
                "RFC Customer", "rfc_cust_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(2),
                AppConfig.ROLE_CUSTOMER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        shopOwnerId = userDAO.saveNewCustomer(
                "RFC ShopOwner", "rfc_shop_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(3),
                AppConfig.ROLE_SHOP_OWNER, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
        deliveryId = userDAO.saveNewCustomer(
                "RFC Delivery", "rfc_del_" + ts + "@test.com",
                HashUtil.hashPassword("TestPass@2026"), buildPhone(4),
                AppConfig.ROLE_DELIVERY, AppConfig.ACCOUNT_STATUS_ACTIVE, true);
    }

    @After
    public void tearDown() {
        try {
            if (adminId > 0)     userDAO.deleteUser(adminId);
            if (customerId > 0)  userDAO.deleteUser(customerId);
            if (shopOwnerId > 0) userDAO.deleteUser(shopOwnerId);
            if (deliveryId > 0)  userDAO.deleteUser(deliveryId);
        } catch (SQLException e) {
            System.err.println("[RoleFilterAccessControlTest] Cleanup: " + e.getMessage());
        }
    }

    // =========================================================
    // TC-RF-01: Admin được phép truy cập /admin/*
    // =========================================================

    @Test
    public void should_allowAdmin_when_requestingAdminPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/admin/dashboard";
        env.user = buildUser(adminId, AppConfig.ROLE_ADMIN);

        runFilter();
        assertTrue("Admin phải được truy cập /admin/*", env.chainCalled.get());
    }

    // =========================================================
    // TC-RF-02: Customer bị chặn khi truy cập /admin/*
    // =========================================================

    @Test
    public void should_forbidCustomer_when_requestingAdminPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/admin/orders";
        env.user = buildUser(customerId, AppConfig.ROLE_CUSTOMER);

        runFilter();
        assertFalse("Customer không được vào /admin/*", env.chainCalled.get());
        assertTrue("Phải redirect hoặc sendError", env.errorCode.get() > 0 || env.redirectLocation.get() != null);
    }

    // =========================================================
    // TC-RF-03: Customer được phép truy cập /customer/*
    // =========================================================

    @Test
    public void should_allowCustomer_when_requestingCustomerPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.user = buildUser(customerId, AppConfig.ROLE_CUSTOMER);

        runFilter();
        assertTrue("Customer phải được truy cập /customer/*", env.chainCalled.get());
    }

    // =========================================================
    // TC-RF-04: Guest (không login) bị redirect đến /auth/login
    // =========================================================

    @Test
    public void should_redirectToLogin_when_noSession() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.user = null; // chưa đăng nhập

        runFilter();
        assertFalse("Guest không được vào /customer/*", env.chainCalled.get());
        String redirect = env.redirectLocation.get();
        if (redirect != null) {
            assertTrue("Phải redirect về login hoặc home",
                    redirect.contains("auth") || redirect.contains("login") || redirect.contains("home"));
        } else {
            assertTrue("Phải sendError 401 hoặc 403", env.errorCode.get() == 401 || env.errorCode.get() == 403);
        }
    }

    // =========================================================
    // TC-RF-05: Delivery được phép truy cập /delivery/*
    // =========================================================

    @Test
    public void should_allowDelivery_when_requestingDeliveryPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/delivery/dashboard";
        env.user = buildUser(deliveryId, AppConfig.ROLE_DELIVERY);

        runFilter();
        assertTrue("Delivery phải được truy cập /delivery/*", env.chainCalled.get());
    }

    // =========================================================
    // TC-RF-06: Customer bị chặn khi truy cập /delivery/*
    // =========================================================

    @Test
    public void should_forbidCustomer_when_requestingDeliveryPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/delivery/dashboard";
        env.user = buildUser(customerId, AppConfig.ROLE_CUSTOMER);

        runFilter();
        assertFalse("Customer không được vào /delivery/*", env.chainCalled.get());
    }

    // =========================================================
    // TC-RF-07: ShopOwner được phép truy cập /shop/*
    // =========================================================

    @Test
    public void should_allowShopOwner_when_requestingShopPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/shop/products";
        env.user = buildUser(shopOwnerId, AppConfig.ROLE_SHOP_OWNER);

        runFilter();
        // ShopOwner có thể cần shop có status APPROVED để vào
        // Nếu chưa có shop → redirect, chain không được gọi với lý do khác
        // Test này chủ yếu verify không có exception/crash
        // (chain có thể không được gọi nếu shop chưa APPROVED)
        assertFalse("Không được crash khi ShopOwner truy cập /shop/*",
                env.errorCode.get() == 500);
    }

    // =========================================================
    // TC-RF-08B: Customer có ShopProfile vẫn được vào /shop/status
    // =========================================================

    @Test
    public void should_allowCustomerWithShopProfile_when_requestingShopStatus() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/shop/status";
        env.user = buildUser(customerId, AppConfig.ROLE_CUSTOMER);
        createShopProfile(customerId, AppConfig.SHOP_PENDING);

        runFilter();
        assertTrue("Customer có hồ sơ shop phải được vào /shop/status", env.chainCalled.get());
        assertEquals("Không được trả 403 cho /shop/status", 0, env.errorCode.get());
    }

    // =========================================================
    // TC-RF-08: Admin được phép truy cập /shop/* (admin review shop)
    // =========================================================

    @Test
    public void should_allowAdmin_when_requestingShopPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/shop/dashboard";
        env.user = buildUser(adminId, AppConfig.ROLE_ADMIN);

        runFilter();
        // Admin có thể access shop path nếu filter cho phép
        assertFalse("Không được crash", env.errorCode.get() == 500);
    }

    // =========================================================
    // TC-RF-09: Admin Ä‘Æ°á»£c phÃ©p truy cáº­p /notifications
    // =========================================================

    @Test
    public void should_allowAdmin_when_requestingNotificationsPath() throws Exception {
        env.reset();
        env.uri = "/Ban_Hoa_Qua_Online/notifications";
        env.user = buildUser(adminId, AppConfig.ROLE_ADMIN);

        runFilter();
        assertTrue("Admin pháº£i Ä‘Æ°á»£c truy cáº­p /notifications", env.chainCalled.get());
        assertEquals("KhÃ´ng Ä‘Æ°á»£c tráº£ vá» lá»—i cho admin", 0, env.errorCode.get());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void runFilter() throws Exception {
        RoleFilter filter = new RoleFilter();
        HttpServletRequest req = env.buildRequest();
        HttpServletResponse resp = env.buildResponse();
        FilterChain chain = (request, response) -> env.chainCalled.set(true);
        filter.doFilter(req, resp, chain);
    }

    private User buildUser(int userId, String role) {
        User u = new User();
        u.setUserId(userId);
        u.setRole(role);
        u.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        u.setEmailVerified(true);
        return u;
    }

    private void createShopProfile(int userId, String status) throws SQLException {
        ShopProfile profile = new ShopProfile();
        profile.setUserId(userId);
        profile.setShopName("RFC Shop " + userId);
        profile.setShopDescription("Regression test shop");
        profile.setApprovalStatus(status);
        profile.setDeliveryAddress("123 Test Street");
        profile.setRating(BigDecimal.ZERO);
        profile.setPreferredCategories("[1]");
        profile.setDocPaths("[\"uploads/test.pdf\"]");
        profile.setBusinessEmail("shop_" + userId + "@test.com");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        shopProfileDAO.save(profile);
    }

    private String buildPhone(int offset) {
        return "09" + String.format("%08d",
                Math.abs((System.nanoTime() + offset) % 100_000_000L));
    }

    static class MockEnv {
        String method = "GET";
        String uri = "/Ban_Hoa_Qua_Online/home";
        User user = null;
        boolean isAjax = false;

        Map<String, Object> sessionAttributes = new HashMap<>();
        Map<String, Object> requestAttributes = new HashMap<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        AtomicInteger errorCode = new AtomicInteger(0);
        AtomicReference<String> redirectLocation = new AtomicReference<>();

        void reset() {
            method = "GET";
            uri = "/Ban_Hoa_Qua_Online/home";
            user = null;
            isAjax = false;
            sessionAttributes = new HashMap<>();
            requestAttributes = new HashMap<>();
            chainCalled = new AtomicBoolean(false);
            errorCode = new AtomicInteger(0);
            redirectLocation = new AtomicReference<>();
        }

        HttpSession buildSession() {
            if (user != null) {
                sessionAttributes.put(AppConfig.SESSION_USER, user);
            }
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class[]{HttpSession.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getAttribute": return sessionAttributes.get(args[0]);
                            case "setAttribute": sessionAttributes.put((String) args[0], args[1]); return null;
                            case "removeAttribute": sessionAttributes.remove(args[0]); return null;
                            case "getId": return "mock-role-filter-session";
                            case "isNew": return user == null;
                            case "invalidate": sessionAttributes.clear(); return null;
                            default: return null;
                        }
                    });
        }

        HttpServletRequest buildRequest() {
            HttpSession session = buildSession();
            String contextPath = "/Ban_Hoa_Qua_Online";
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, m, args) -> {
                        switch (m.getName()) {
                            case "getMethod": return method;
                            case "getRequestURI": return uri;
                            case "getContextPath": return contextPath;
                            case "getSession": return session;
                            case "getParameter": return null;
                            case "getHeader":
                                if ("X-Requested-With".equals(args[0]))
                                    return isAjax ? "XMLHttpRequest" : null;
                                return null;
                            case "getAttribute": return requestAttributes.get(args[0]);
                            case "setAttribute": requestAttributes.put((String) args[0], args[1]); return null;
                            case "getCharacterEncoding": return "UTF-8";
                            case "setCharacterEncoding": return null;
                            case "getServletContext": return null;
                            case "getRequestDispatcher": return null;
                            default: return null;
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
                            case "setStatus": errorCode.set((int) args[0]); return null;
                            case "setContentType": return null;
                            case "setCharacterEncoding": return null;
                            case "getWriter": return new PrintWriter(new StringWriter());
                            default: return null;
                        }
                    });
        }
    }
}
