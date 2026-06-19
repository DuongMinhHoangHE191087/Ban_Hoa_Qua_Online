package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.auth.UserSessionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import servlet.admin.system.AdminConfigServlet;
import servlet.admin.user.AdminUserRevokeSessionsAPI;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AdminUserSessionManagementRegressionTest {

    private static final String CSRF_TOKEN = "test-csrf-token";

    private final UserDAO userDAO = new UserDAO();
    private final UserSessionDAO userSessionDAO = new UserSessionDAO();
    private final RevokeSessionsHarness revokeSessionsServlet = new RevokeSessionsHarness();
    private final AdminConfigHarness adminConfigServlet = new AdminConfigHarness();
    private final List<Integer> createdUserIds = new ArrayList<>();

    @Before
    public void setUp() {
        createdUserIds.clear();
    }

    @After
    public void tearDown() {
        for (int i = createdUserIds.size() - 1; i >= 0; i--) {
            try {
                userDAO.deleteUser(createdUserIds.get(i));
            } catch (SQLException ignored) {
                // Best-effort cleanup only.
            }
        }
        createdUserIds.clear();
    }

    @Test
    public void revokeSessionsEndpointDeletesOnlyTargetUsersSessions() throws Exception {
        int adminId = createUser("Admin Revoke", AppConfig.ROLE_ADMIN);
        int targetUserId = createUser("Target Revoke", AppConfig.ROLE_CUSTOMER);
        int otherUserId = createUser("Other Revoke", AppConfig.ROLE_CUSTOMER);

        String targetToken1 = seedSession(targetUserId, "target-1");
        String targetToken2 = seedSession(targetUserId, "target-2");
        String otherToken = seedSession(otherUserId, "other-1");

        MockHttpEnvironment env = new MockHttpEnvironment("/admin/users/revoke-sessions");
        env.setCurrentUser(buildSessionUser(adminId, AppConfig.ROLE_ADMIN));
        env.setCsrfToken(CSRF_TOKEN);
        env.putParam("userId", String.valueOf(targetUserId));

        revokeSessionsServlet.doPostPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), env.status);
        assertTrue(env.contentType == null || env.contentType.contains("application/json"));
        assertEquals("UTF-8", env.characterEncoding);

        Map<?, ?> response = JsonUtil.fromJson(env.getResponseBody(), Map.class);
        assertEquals(Boolean.TRUE, response.get("success"));
        assertNotNull(response.get("data"));

        assertNull(userSessionDAO.findUserIdBySessionToken(targetToken1));
        assertNull(userSessionDAO.findUserIdBySessionToken(targetToken2));
        assertEquals(Integer.valueOf(otherUserId), userSessionDAO.findUserIdBySessionToken(otherToken));
    }

    @Test
    public void revokeSessionsEndpoint_blocksNonAdminUser() throws Exception {
        int customerId = createUser("Customer Blocked", AppConfig.ROLE_CUSTOMER);

        MockHttpEnvironment env = new MockHttpEnvironment("/admin/users/revoke-sessions");
        env.setCurrentUser(buildSessionUser(customerId, AppConfig.ROLE_CUSTOMER));
        env.putParam("userId", String.valueOf(customerId));

        revokeSessionsServlet.doPostPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_FORBIDDEN), env.status);
        Map<?, ?> response = JsonUtil.fromJson(env.getResponseBody(), Map.class);
        assertEquals(Boolean.FALSE, response.get("success"));
        assertNotNull(response.get("error"));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, ((Number) ((Map<?, ?>) response.get("meta")).get("statusCode")).intValue());
    }

    @Test
    public void revokeSessionsEndpoint_returns400WhenUserIdMissing() throws Exception {
        int adminId = createUser("Admin Missing UserId", AppConfig.ROLE_ADMIN);

        MockHttpEnvironment env = new MockHttpEnvironment("/admin/users/revoke-sessions");
        env.setCurrentUser(buildSessionUser(adminId, AppConfig.ROLE_ADMIN));

        revokeSessionsServlet.doPostPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_BAD_REQUEST), env.status);
        Map<?, ?> response = JsonUtil.fromJson(env.getResponseBody(), Map.class);
        assertEquals(Boolean.FALSE, response.get("success"));
        assertEquals("Thiếu userId.", response.get("error"));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ((Number) ((Map<?, ?>) response.get("meta")).get("statusCode")).intValue());
    }

    @Test
    public void revokeSessionsEndpoint_returns400WhenUserIdInvalid() throws Exception {
        int adminId = createUser("Admin Invalid UserId", AppConfig.ROLE_ADMIN);

        MockHttpEnvironment env = new MockHttpEnvironment("/admin/users/revoke-sessions");
        env.setCurrentUser(buildSessionUser(adminId, AppConfig.ROLE_ADMIN));
        env.putParam("userId", "abc");

        revokeSessionsServlet.doPostPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_BAD_REQUEST), env.status);
        Map<?, ?> response = JsonUtil.fromJson(env.getResponseBody(), Map.class);
        assertEquals(Boolean.FALSE, response.get("success"));
        assertEquals("userId không hợp lệ.", response.get("error"));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ((Number) ((Map<?, ?>) response.get("meta")).get("statusCode")).intValue());
    }

    @Test
    public void clearAllSessionsActionDeletesEveryStoredToken() throws Exception {
        int adminId = createUser("Admin Clear", AppConfig.ROLE_ADMIN);
        int userOneId = createUser("User One", AppConfig.ROLE_CUSTOMER);
        int userTwoId = createUser("User Two", AppConfig.ROLE_CUSTOMER);

        String token1 = seedSession(userOneId, "clear-1");
        String token2 = seedSession(userOneId, "clear-2");
        String token3 = seedSession(userTwoId, "clear-3");

        MockHttpEnvironment env = new MockHttpEnvironment("/admin/config");
        env.setCurrentUser(buildSessionUser(adminId, AppConfig.ROLE_ADMIN));
        env.setCsrfToken(CSRF_TOKEN);
        env.putParam("action", "clearAllSessions");

        adminConfigServlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/admin/config", env.redirectLocation);
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
        assertNull(env.status);

        assertNull(userSessionDAO.findUserIdBySessionToken(token1));
        assertNull(userSessionDAO.findUserIdBySessionToken(token2));
        assertNull(userSessionDAO.findUserIdBySessionToken(token3));
    }

    private int createUser(String fullNamePrefix, String role) throws SQLException {
        String suffix = String.valueOf(System.currentTimeMillis());
        String fullName = fullNamePrefix + " " + suffix;
        String email = fullNamePrefix.toLowerCase().replace(' ', '.') + "." + suffix + "@test.com";
        String phone = buildPhone();
        int userId = userDAO.saveNewCustomer(
                fullName,
                email,
                "hashed_pwd",
                phone,
                role,
                AppConfig.ACCOUNT_STATUS_ACTIVE,
                true
        );
        createdUserIds.add(userId);
        return userId;
    }

    private String seedSession(int userId, String tokenPrefix) throws SQLException {
        String token = tokenPrefix + "-" + System.nanoTime();
        userSessionDAO.saveUserSession(userId, token, futureExpiry());
        return token;
    }

    private Timestamp futureExpiry() {
        return new Timestamp(System.currentTimeMillis() + 3_600_000L);
    }

    private String buildPhone() {
        long raw = Math.abs(System.nanoTime() % 100_000_000L);
        return "09" + String.format("%08d", raw);
    }

    private User buildSessionUser(int userId, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        user.setEmailVerified(true);
        return user;
    }

    private static final class RevokeSessionsHarness extends AdminUserRevokeSessionsAPI {
        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }
    }

    private static final class AdminConfigHarness extends AdminConfigServlet {
        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();
        private final StringWriter responseBody = new StringWriter();
        private final PrintWriter responseWriter = new PrintWriter(responseBody, true);

        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private Integer status;
        private String contentType;
        private String characterEncoding;
        private String redirectLocation;

        private MockHttpEnvironment(String servletPath) {
            this.session = createSessionProxy();
            this.request = createRequestProxy(servletPath);
            this.response = createResponseProxy();
        }

        private void setCurrentUser(User user) {
            sessionAttributes.put(AppConfig.SESSION_USER, user);
        }

        private void setCsrfToken(String token) {
            sessionAttributes.put(AppConfig.SESSION_CSRF_TOKEN, token);
            params.put("_csrf", token);
        }

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private String getResponseBody() {
            responseWriter.flush();
            return responseBody.toString();
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
                    case "getId":
                        return "mock-admin-session";
                    case "isNew":
                        return false;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    handler
            );
        }

        private HttpServletRequest createRequestProxy(String servletPath) {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getParameter":
                        return params.get(args[0]);
                    case "getSession":
                        return session;
                    case "getContextPath":
                        return "/ctx";
                    case "getServletPath":
                        return servletPath;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler
            );
        }

        private HttpServletResponse createResponseProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setStatus":
                        status = (Integer) args[0];
                        return null;
                    case "setContentType":
                        contentType = (String) args[0];
                        return null;
                    case "setCharacterEncoding":
                        characterEncoding = (String) args[0];
                        return null;
                    case "getWriter":
                        return responseWriter;
                    case "sendRedirect":
                        redirectLocation = (String) args[0];
                        return null;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler
            );
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
