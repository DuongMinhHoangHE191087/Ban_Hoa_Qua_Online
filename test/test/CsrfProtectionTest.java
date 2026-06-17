package test;

import config.AppConfig;
import filter.CsrfFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * CsrfProtectionTest — Kiểm tra CsrfFilter trực tiếp không cần Tomcat.
 * Dùng Proxy-based mock HttpServletRequest/Response (pattern từ CheckoutServletPricingRegressionTest).
 */
public class CsrfProtectionTest {

    private static final String VALID_TOKEN = "test-csrf-token-valid-12345";

    private MockEnv env;

    @Before
    public void setUp() {
        env = new MockEnv();
    }

    // =========================================================
    // TC-CSRF-01: POST với token đúng qua parameter _csrf
    // =========================================================

    @Test
    public void should_allowPost_when_tokenMatchesSession_viaParameter() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.paramToken = VALID_TOKEN;

        runFilter();
        assertTrue("chain.doFilter phải được gọi khi token đúng", env.chainCalled.get());
        assertFalse("sendError không được gọi", env.errorSent.get());
    }

    // =========================================================
    // TC-CSRF-02: POST với token đúng qua header X-CSRF-Token
    // =========================================================

    @Test
    public void should_allowPost_when_tokenMatchesSession_viaXCsrfHeader() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.headerToken = VALID_TOKEN;

        runFilter();
        assertTrue("chain.doFilter phải được gọi", env.chainCalled.get());
    }

    // =========================================================
    // TC-CSRF-03: POST không có token → 403
    // =========================================================

    @Test
    public void should_blockPost_when_noTokenProvided() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";

        runFilter();
        assertFalse("chain.doFilter không được gọi khi thiếu token", env.chainCalled.get());
        assertEquals("sendError phải 403", 403, env.errorCode.get());
    }

    // =========================================================
    // TC-CSRF-04: POST với token sai → 403
    // =========================================================

    @Test
    public void should_blockPost_when_tokenMismatch() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.paramToken = "wrong-token-xyz";

        runFilter();
        assertFalse("chain.doFilter không được gọi", env.chainCalled.get());
        assertEquals("sendError phải 403", 403, env.errorCode.get());
    }

    // =========================================================
    // TC-CSRF-05: POST đến /auth/* — bỏ qua (auth tự check)
    // =========================================================

    @Test
    public void should_allowPost_to_authPaths_without_token() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/auth/login";

        runFilter();
        assertTrue("auth/* không cần token ở filter level", env.chainCalled.get());
    }

    // =========================================================
    // TC-CSRF-06: POST đến /api/* — bỏ qua (webhook)
    // =========================================================

    @Test
    public void should_allowPost_to_apiPaths_without_token() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/api/payment/webhook";

        runFilter();
        assertTrue("api/* không cần token", env.chainCalled.get());
    }

    // =========================================================
    // TC-CSRF-07: POST đến /cart — bỏ qua (Beacon API)
    // =========================================================

    @Test
    public void should_allowPost_to_cartPath_without_token() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/cart";

        runFilter();
        assertTrue("cart không cần token", env.chainCalled.get());
    }

    // =========================================================
    // TC-CSRF-08: GET luôn được phép dù không có token
    // =========================================================

    @Test
    public void should_allowGet_always() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "GET";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";

        runFilter();
        assertTrue("GET không cần CSRF check", env.chainCalled.get());
    }

    // =========================================================
    // TC-CSRF-09: Session mới không có token — filter tạo token mới
    // =========================================================

    @Test
    public void should_createNewToken_when_sessionHasNone() throws Exception {
        env.sessionToken = null;
        env.method = "GET";
        env.uri = "/Ban_Hoa_Qua_Online/home";

        runFilter();
        assertNotNull("Session phải có CSRF token sau khi GET",
                env.sessionAttributes.get(AppConfig.SESSION_CSRF_TOKEN));
    }

    // =========================================================
    // TC-CSRF-10: Token rỗng bị từ chối
    // =========================================================

    @Test
    public void should_blockPost_when_tokenIsEmpty() throws Exception {
        env.sessionToken = VALID_TOKEN;
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/customer/orders";
        env.paramToken = "";

        runFilter();
        assertFalse("Token rỗng phải bị chặn", env.chainCalled.get());
        assertEquals(403, env.errorCode.get());
    }

    // =========================================================
    // Helper: chạy CsrfFilter với mock environment
    // =========================================================

    private void runFilter() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        HttpServletRequest req = env.buildRequest();
        HttpServletResponse resp = env.buildResponse();
        FilterChain chain = (request, response) -> env.chainCalled.set(true);
        filter.doFilter(req, resp, chain);
    }

    // =========================================================
    // Mock Environment
    // =========================================================

    static class MockEnv {
        String method = "GET";
        String uri = "/Ban_Hoa_Qua_Online/home";
        String paramToken = null;
        String headerToken = null;
        String sessionToken = null;
        boolean isAjax = false;

        Map<String, Object> sessionAttributes = new HashMap<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        AtomicBoolean errorSent = new AtomicBoolean(false);
        AtomicInteger errorCode = new AtomicInteger(0);
        AtomicReference<String> redirectLocation = new AtomicReference<>();

        HttpSession buildSession() {
            if (sessionToken != null) {
                sessionAttributes.put(AppConfig.SESSION_CSRF_TOKEN, sessionToken);
            }
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class[]{HttpSession.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getAttribute": return sessionAttributes.get(args[0]);
                            case "setAttribute": sessionAttributes.put((String) args[0], args[1]); return null;
                            case "removeAttribute": sessionAttributes.remove(args[0]); return null;
                            case "getId": return "mock-session-id";
                            case "isNew": return false;
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
                            case "getParameter":
                                if ("_csrf".equals(args[0])) return paramToken;
                                if ("format".equals(args[0])) return null;
                                return null;
                            case "getHeader":
                                if ("X-CSRF-Token".equals(args[0]) || "X-XSRF-TOKEN".equals(args[0]))
                                    return headerToken;
                                if ("X-Requested-With".equals(args[0]))
                                    return isAjax ? "XMLHttpRequest" : null;
                                if ("Accept".equals(args[0])) return "text/html";
                                return null;
                            case "getCharacterEncoding": return "UTF-8";
                            case "setCharacterEncoding": return null;
                            case "getServletContext": return null;
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
                                errorSent.set(true);
                                errorCode.set((int) args[0]);
                                return null;
                            case "sendRedirect":
                                redirectLocation.set((String) args[0]);
                                return null;
                            case "setStatus": return null;
                            case "setContentType": return null;
                            case "setCharacterEncoding": return null;
                            case "getWriter":
                                return new PrintWriter(new StringWriter());
                            default: return null;
                        }
                    });
        }
    }
}
