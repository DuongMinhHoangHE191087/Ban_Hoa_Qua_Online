package test;

import exception.BusinessException;
import filter.GlobalExceptionFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GlobalExceptionFilterTest {

    @Test
    public void shouldReturnJsonErrorForApiBusinessException() throws Exception {
        MockEnv env = new MockEnv();
        env.method = "POST";
        env.uri = "/Ban_Hoa_Qua_Online/api/orders";
        env.contentType = "application/json";
        env.contextAttributes.put("appEnv", "development");
        env.contextAttributes.put("logFilePath", "D:/DMHoang/Project_GitHub/Ban_hoa_qua_online/logs/log.txt");

        FilterChain chain = (request, response) -> {
            throw new BusinessException("INVALID_STATUS", "Trạng thái không hợp lệ");
        };

        new GlobalExceptionFilter().doFilter(env.buildRequest(), env.buildResponse(), chain);

        assertEquals(400, env.status);
        assertTrue(env.responseContentType != null && env.responseContentType.startsWith("application/json"));
        assertTrue(env.body.toString().contains("\"success\":false"));
        assertTrue(env.body.toString().contains("\"requestId\""));
        assertTrue(env.body.toString().contains("INVALID_STATUS"));
    }

    @Test
    public void shouldForwardUnexpectedHtmlErrorToCustom500Page() throws Exception {
        MockEnv env = new MockEnv();
        env.method = "GET";
        env.uri = "/Ban_Hoa_Qua_Online/home";
        env.accept = "text/html";
        env.contextAttributes.put("appEnv", "development");
        env.contextAttributes.put("logFilePath", "D:/DMHoang/Project_GitHub/Ban_hoa_qua_online/logs/log.txt");

        FilterChain chain = (request, response) -> {
            throw new ServletException("boom");
        };

        new GlobalExceptionFilter().doFilter(env.buildRequest(), env.buildResponse(), chain);

        assertEquals(500, env.status);
        assertEquals("/WEB-INF/jsp/error/500.jsp", env.forwardedPath.get());
        assertNotNull(env.requestAttributes.get("errorId"));
        assertNotNull(env.requestAttributes.get("logFilePath"));
        assertNotNull(env.requestAttributes.get("jakarta.servlet.error.exception"));
    }

    private static final class MockEnv {
        String method = "GET";
        String uri = "/Ban_Hoa_Qua_Online/home";
        String contextPath = "/Ban_Hoa_Qua_Online";
        String accept;
        String contentType;
        String xRequestedWith;
        final Map<String, Object> requestAttributes = new HashMap<>();
        final Map<String, Object> contextAttributes = new HashMap<>();
        final Map<String, String> headers = new HashMap<>();
        final StringWriter body = new StringWriter();
        final AtomicReference<String> forwardedPath = new AtomicReference<>();
        int status = 0;
        String responseContentType;

        HttpServletRequest buildRequest() {
            ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(
                    ServletContext.class.getClassLoader(),
                    new Class[]{ServletContext.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getAttribute":
                                return contextAttributes.get(args[0]);
                            case "setAttribute":
                                contextAttributes.put((String) args[0], args[1]);
                                return null;
                            default:
                                return null;
                        }
                    });

            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getMethod":
                                return MockEnv.this.method;
                            case "getRequestURI":
                                return uri;
                            case "getContextPath":
                                return contextPath;
                            case "getHeader":
                                if (args == null || args.length == 0) {
                                    return null;
                                }
                                if ("Accept".equals(args[0])) {
                                    return accept;
                                }
                                if ("X-Requested-With".equals(args[0])) {
                                    return xRequestedWith;
                                }
                                if ("Referer".equals(args[0])) {
                                    return null;
                                }
                                return headers.get(String.valueOf(args[0]));
                            case "getContentType":
                                return contentType;
                            case "getScheme":
                                return "http";
                            case "getServerName":
                                return "localhost";
                            case "getServerPort":
                                return 8080;
                            case "getParameter":
                                return null;
                            case "getParameterMap":
                                return java.util.Collections.emptyMap();
                            case "getAttribute":
                                return requestAttributes.get(args[0]);
                            case "setAttribute":
                                requestAttributes.put((String) args[0], args[1]);
                                return null;
                            case "getSession":
                                return null;
                            case "getSession" + "(boolean)":
                                return null;
                            case "getServletContext":
                                return servletContext;
                            case "getRequestDispatcher":
                                String target = (String) args[0];
                                return (RequestDispatcher) Proxy.newProxyInstance(
                                        RequestDispatcher.class.getClassLoader(),
                                        new Class[]{RequestDispatcher.class},
                                        (dp, dm, da) -> {
                                            if ("forward".equals(dm.getName())) {
                                                forwardedPath.set(target);
                                            }
                                            return null;
                                        });
                            case "getCharacterEncoding":
                                return "UTF-8";
                            case "getRemoteAddr":
                                return "127.0.0.1";
                            case "getQueryString":
                                return null;
                            default:
                                return null;
                        }
                    });
        }

        HttpServletResponse buildResponse() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class[]{HttpServletResponse.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setStatus":
                                status = (Integer) args[0];
                                return null;
                            case "getStatus":
                                return status;
                            case "setContentType":
                                responseContentType = (String) args[0];
                                return null;
                            case "setCharacterEncoding":
                                return null;
                            case "setHeader":
                                headers.put((String) args[0], String.valueOf(args[1]));
                                return null;
                            case "getWriter":
                                return new PrintWriter(body);
                            case "sendRedirect":
                                headers.put("Location", (String) args[0]);
                                status = 302;
                                return null;
                            case "sendError":
                                status = (Integer) args[0];
                                return null;
                            case "isCommitted":
                                return false;
                            case "getContentType":
                                return responseContentType;
                            default:
                                return null;
                        }
                    });
        }
    }
}
