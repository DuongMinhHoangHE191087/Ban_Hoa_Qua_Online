package com.fruitmkt.test;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.servlet.shop.PromotionServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test cho PromotionServlet: create, update, toggle, delete.
 */
public class PromotionServletCrudToggleTest {

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final PromotionServletHarness servlet = new PromotionServletHarness();

    private MockHttpEnvironment env;
    private int createdPromoId = -1;

    @Before
    public void setUp() {
        env = new MockHttpEnvironment();
        env.setCurrentUser(buildShopOwner(3, "Shop Owner Seed"));
    }

    @After
    public void tearDown() {
        try {
            if (createdPromoId > 0) {
                promotionDAO.softDelete(createdPromoId);
            }
        } catch (SQLException ignored) {
            // Best-effort cleanup only.
        } finally {
            createdPromoId = -1;
        }
    }

    @Test
    public void loadPromotionPageForShopOwner() throws Exception {
        servlet.doGetPublic(env.request, env.response);

        assertEquals("/WEB-INF/jsp/shop/promotion.jsp", env.forwardedPath);
        assertNotNull(env.requestAttributes.get("promotions"));
        assertNotNull(env.requestAttributes.get("products"));
        assertNull(env.errorStatus);
    }

    @Test
    public void createUpdateToggleAndDeletePromotionThroughServlet() throws Exception {
        String baseCode = "SV-CRUD-" + System.currentTimeMillis();
        String updatedCode = baseCode + "-UPD";

        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, baseCode, "FIXED", "SHOP", "ORDER", null,
                new BigDecimal("15000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);
        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/shop/promotions", env.redirectLocation);
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));

        Promotion created = promotionDAO.findByCode(baseCode);
        assertNotNull(created);
        createdPromoId = created.getPromoId();
        assertTrue(created.getIsActive());
        assertEquals(baseCode, created.getCode());

        env.clearRequestState();
        env.putParam("action", "update");
        env.putParam("promoId", String.valueOf(createdPromoId));
        applyPromotionForm(env, updatedCode, "FIXED", "SHOP", "ORDER", null,
                new BigDecimal("20000"), new BigDecimal("60000"), new BigDecimal("130000"), "8", false, true);
        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/shop/promotions", env.redirectLocation);
        Promotion updated = promotionDAO.findById(createdPromoId);
        assertNotNull(updated);
        assertEquals(updatedCode, updated.getCode());
        assertTrue(updated.getDiscountValue().compareTo(new BigDecimal("20000")) == 0);
        assertFalse(updated.getCanStack());
        assertTrue(updated.getIsActive());

        env.clearRequestState();
        env.putParam("action", "toggle");
        env.putParam("promoId", String.valueOf(createdPromoId));
        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/shop/promotions", env.redirectLocation);
        Promotion toggled = promotionDAO.findById(createdPromoId);
        assertNotNull(toggled);
        assertFalse(toggled.getIsActive());

        env.clearRequestState();
        env.putParam("action", "delete");
        env.putParam("promoId", String.valueOf(createdPromoId));
        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/shop/promotions", env.redirectLocation);
        assertNull(promotionDAO.findById(createdPromoId));
    }

    @Test
    public void rejectInvalidDateRange() throws Exception {
        String code = "SV-BAD-DATE-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "SHOP", "ORDER", null,
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);
        env.putParam("validFrom", INPUT_FORMAT.format(LocalDateTime.now().plusDays(2)));
        env.putParam("validUntil", INPUT_FORMAT.format(LocalDateTime.now().plusDays(1)));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    @Test
    public void rejectInvalidDiscountScope() throws Exception {
        String code = "SV-BAD-SCOPE-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "GLOBAL", "ORDER", null,
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    @Test
    public void rejectProductScopeWithoutProductId() throws Exception {
        String code = "SV-BAD-PRODUCT-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "SHOP", "PRODUCT", "",
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    @Test
    public void rejectInvalidDateRangeOnAdminRoute() throws Exception {
        useAdminPromotionRoute();
        String code = "AD-BAD-DATE-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "ALL", "ORDER", null,
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);
        env.putParam("validFrom", INPUT_FORMAT.format(LocalDateTime.now().plusDays(2)));
        env.putParam("validUntil", INPUT_FORMAT.format(LocalDateTime.now().plusDays(1)));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    @Test
    public void rejectShopDiscountScopeOnAdminRoute() throws Exception {
        useAdminPromotionRoute();
        String code = "AD-BAD-SCOPE-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "SHOP", "ORDER", null,
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    @Test
    public void rejectProductScopeWithoutProductIdOnAdminRoute() throws Exception {
        useAdminPromotionRoute();
        String code = "AD-BAD-PRODUCT-" + System.currentTimeMillis();
        env.clearRequestState();
        env.putParam("action", "save");
        applyPromotionForm(env, code, "FIXED", "ALL", "PRODUCT", "",
                new BigDecimal("10000"), new BigDecimal("50000"), new BigDecimal("120000"), "5", true, true);

        servlet.doPostPublic(env.request, env.response);

        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(promotionDAO.findByCode(code));
    }

    private void applyPromotionForm(MockHttpEnvironment environment, String code, String discountType,
                                    String discountScope, String scope, String productId,
                                    BigDecimal discountValue, BigDecimal discountMax, BigDecimal minOrderValue,
                                    String maxUses, boolean canStack, boolean isActive) {
        environment.putParam("code", code);
        environment.putParam("discountType", discountType);
        environment.putParam("discountScope", discountScope);
        environment.putParam("scope", scope);
        if (productId != null) {
            environment.putParam("productId", productId);
        }
        environment.putParam("discountValue", discountValue.toPlainString());
        environment.putParam("discountMax", discountMax.toPlainString());
        environment.putParam("minOrderValue", minOrderValue.toPlainString());
        environment.putParam("maxUses", maxUses);
        environment.putParam("validFrom", INPUT_FORMAT.format(LocalDateTime.now().minusDays(1)));
        environment.putParam("validUntil", INPUT_FORMAT.format(LocalDateTime.now().plusDays(7)));
        if (canStack) {
            environment.putParam("canStack", "on");
        }
        if (isActive) {
            environment.putParam("isActive", "on");
        }
    }

    private User buildShopOwner(int userId, String fullName) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setRole(AppConfig.ROLE_SHOP_OWNER);
        user.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        return user;
    }

    private User buildAdmin(int userId, String fullName) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setRole(AppConfig.ROLE_ADMIN);
        user.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        return user;
    }

    private void useAdminPromotionRoute() {
        env.setCurrentUser(buildAdmin(1, "Admin Seed"));
        env.setServletPath("/admin/promotions");
    }

    private final class PromotionServletHarness extends PromotionServlet {
        void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doGet(req, resp);
        }

        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doPost(req, resp);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, Object> requestAttributes = new HashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();

        private String redirectLocation;
        private Integer errorStatus;
        private String errorMessage;
        private String forwardedPath;
        private String servletPath = "/shop/promotions";

        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private MockHttpEnvironment() {
            this.session = createSessionProxy();
            this.request = createRequestProxy();
            this.response = createResponseProxy();
        }

        private void setCurrentUser(User user) {
            sessionAttributes.put(AppConfig.SESSION_USER, user);
        }

        private void setServletPath(String servletPath) {
            this.servletPath = servletPath;
        }

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private void clearRequestState() {
            params.clear();
            requestAttributes.clear();
            redirectLocation = null;
            errorStatus = null;
            errorMessage = null;
            forwardedPath = null;
        }

        private HttpSession createSessionProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAttribute":
                        return sessionAttributes.get(args[0]);
                    case "setAttribute":
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    case "removeAttribute":
                        sessionAttributes.remove(args[0]);
                        return null;
                    case "invalidate":
                        sessionAttributes.clear();
                        return null;
                    case "toString":
                        return "MockHttpSession";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    handler);
        }

        private HttpServletRequest createRequestProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getParameter":
                        return params.get(args[0]);
                    case "getParameterMap":
                        return new HashMap<>(params);
                    case "getParameterNames":
                        return java.util.Collections.enumeration(params.keySet());
                    case "getSession":
                        return session;
                    case "getContextPath":
                        return "/ctx";
                    case "getServletPath":
                        return servletPath;
                    case "getAttribute":
                        return requestAttributes.get(args[0]);
                    case "setAttribute":
                        requestAttributes.put((String) args[0], args[1]);
                        return null;
                    case "removeAttribute":
                        requestAttributes.remove(args[0]);
                        return null;
                    case "getRequestDispatcher":
                        return createDispatcher((String) args[0]);
                    case "toString":
                        return "MockHttpServletRequest";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler);
        }

        private RequestDispatcher createDispatcher(String path) {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("forward".equals(method.getName())) {
                    forwardedPath = path;
                    return null;
                }
                return defaultValue(method.getReturnType());
            };
            return (RequestDispatcher) Proxy.newProxyInstance(
                    RequestDispatcher.class.getClassLoader(),
                    new Class<?>[]{RequestDispatcher.class},
                    handler);
        }

        private HttpServletResponse createResponseProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "sendRedirect":
                        redirectLocation = (String) args[0];
                        return null;
                    case "sendError":
                        errorStatus = (Integer) args[0];
                        errorMessage = args != null && args.length > 1 ? (String) args[1] : null;
                        return null;
                    case "setStatus":
                        errorStatus = (Integer) args[0];
                        return null;
                    case "toString":
                        return "MockHttpServletResponse";
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler);
        }

        private Object defaultValue(Class<?> type) {
            if (type == null || !type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == char.class) return '\0';
            return null;
        }

    }
}
