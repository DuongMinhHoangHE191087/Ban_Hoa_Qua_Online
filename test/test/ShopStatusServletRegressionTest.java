package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import servlet.shop.shop.ShopStatusServlet;
import util.HashUtil;
import util.SessionUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ShopStatusServletRegressionTest {

    private UserDAO userDAO;
    private ShopProfileDAO shopProfileDAO;
    private int customerId = -1;

    @Before
    public void setUp() throws SQLException {
        userDAO = new UserDAO();
        shopProfileDAO = new ShopProfileDAO();
        customerId = userDAO.saveNewCustomer(
                "Status Regression Customer",
                "status_reg_" + System.currentTimeMillis() + "@test.com",
                HashUtil.hashPassword("TestPass@2026"),
                buildPhone(),
                AppConfig.ROLE_CUSTOMER,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
        assertTrue(customerId > 0);

        ShopProfile profile = new ShopProfile();
        profile.setUserId(customerId);
        profile.setShopName("Regression Shop");
        profile.setShopDescription("Regression");
        profile.setApprovalStatus(AppConfig.SHOP_PENDING);
        profile.setDeliveryAddress("123 Test Street");
        profile.setRating(BigDecimal.ZERO);
        profile.setPreferredCategories("[1]");
        profile.setDocPaths("[\"uploads/test.pdf\"]");
        profile.setBusinessEmail("shop_" + customerId + "@test.com");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        shopProfileDAO.save(profile);
    }

    @After
    public void tearDown() {
        try {
            if (customerId > 0) {
                shopProfileDAO.deleteByUserId(customerId);
                userDAO.deleteUser(customerId);
            }
        } catch (SQLException e) {
            System.err.println("[ShopStatusServletRegressionTest] Cleanup failed: " + e.getMessage());
        }
    }

    @Test
    public void should_forwardStatusPage_when_customerHasShopProfile() throws Exception {
        ShopStatusServlet servlet = new ShopStatusServlet();
        MockEnv env = new MockEnv(buildUser(customerId, AppConfig.ROLE_CUSTOMER));

        Method doGet = ShopStatusServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, env.request, env.response);

        assertEquals("Không được redirect khi đã có hồ sơ shop", null, env.redirectTarget.get());
        assertEquals("/WEB-INF/jsp/shop/status.jsp", env.forwardPath.get());
        assertSame("profile phải được gán vào request", true, env.requestAttributes.containsKey("profile"));
    }

    private User buildUser(int userId, String role) {
        User u = new User();
        u.setUserId(userId);
        u.setRole(role);
        u.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        u.setEmailVerified(true);
        return u;
    }

    private String buildPhone() {
        return "09" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }

    private static final class MockEnv {
        final Map<String, Object> sessionAttrs = new HashMap<>();
        final Map<String, Object> requestAttributes = new HashMap<>();
        final AtomicReference<String> redirectTarget = new AtomicReference<>();
        final AtomicReference<String> forwardPath = new AtomicReference<>();
        final HttpServletRequest request;
        final HttpServletResponse response;

        MockEnv(User user) {
            if (user != null) {
                sessionAttrs.put(AppConfig.SESSION_USER, user);
            }
            HttpSession session = (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class[]{HttpSession.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getAttribute":
                                return sessionAttrs.get(args[0]);
                            case "setAttribute":
                                sessionAttrs.put((String) args[0], args[1]);
                                return null;
                            case "invalidate":
                                sessionAttrs.clear();
                                return null;
                            default:
                                return null;
                        }
                    });

            request = (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getSession":
                                return session;
                            case "getContextPath":
                                return "/Ban_Hoa_Qua_Online";
                            case "getRequestURI":
                                return "/Ban_Hoa_Qua_Online/shop/status";
                            case "setAttribute":
                                requestAttributes.put((String) args[0], args[1]);
                                return null;
                            case "getAttribute":
                                return requestAttributes.get(args[0]);
                            case "getRequestDispatcher":
                                String path = (String) args[0];
                                return (RequestDispatcher) Proxy.newProxyInstance(
                                        RequestDispatcher.class.getClassLoader(),
                                        new Class[]{RequestDispatcher.class},
                                        (dp, dm, da) -> {
                                            if ("forward".equals(dm.getName())) {
                                                forwardPath.set(path);
                                            }
                                            return null;
                                        });
                            default:
                                return null;
                        }
                    });

            response = (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class[]{HttpServletResponse.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "sendRedirect":
                                redirectTarget.set((String) args[0]);
                                return null;
                            case "setStatus":
                            case "setContentType":
                            case "setCharacterEncoding":
                                return null;
                            default:
                                return null;
                        }
                    });
        }
    }
}
