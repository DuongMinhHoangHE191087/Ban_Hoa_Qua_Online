package servlet.base;

import model.entity.auth.User;
import model.response.ApiResponse;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * BaseHttpServlet — Base class for all servlets.
 *
 * Provides:
 * - Common character encoding and content type setup
 * - Centralized authentication checks
 * - Role-based access control
 * - Consistent error handling
 * - Request/response logging
 * - CSRF token management
 *
 * @author fruitmkt-team
 */
public abstract class BaseHttpServlet extends HttpServlet {

    protected static final Logger log = Logger.getLogger(BaseHttpServlet.class.getName());

    /**
     * Setup character encoding and content type for all requests.
     * Call this at the start of doGet/doPost.
     */
    protected void setupResponse(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            LoggerUtil.warn(log, "Failed to set request encoding", e);
        }
        resp.setContentType("text/html;charset=UTF-8");
    }

    /**
     * Setup JSON response content type.
     */
    protected void setupJsonResponse(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
    }

    /**
     * Get the current authenticated user from session.
     * @return User if logged in, null otherwise
     */
    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return SessionUtil.getCurrentUser(session);
    }

    /**
     * Require user to be authenticated. Redirect to login if not.
     * @return true if authenticated, false if redirected to login
     */
    protected boolean requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            LoggerUtil.debug(log, "User not authenticated, redirecting to login");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return false;
        }
        return true;
    }

    /**
     * Require user to have specific role. Send 403 if not authorized.
     * @return true if authorized, false if redirected
     */
    protected boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String... allowedRoles) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return false;
        }

        for (String role : allowedRoles) {
            if (role.equals(user.getRole())) {
                return true;
            }
        }

        LoggerUtil.warn(log, "User %s (role=%s) attempted to access restricted resource: %s",
            user.getUserId(), user.getRole(), req.getRequestURI());
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        return false;
    }

    /**
     * Send JSON error response.
     */
    protected void sendJsonError(HttpServletResponse resp, String message) throws IOException {
        setupJsonResponse(resp);
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonUtil.writeJson(resp, ApiResponse.error(message));
    }

    /**
     * Send JSON success response with data.
     */
    protected void sendJsonSuccess(HttpServletResponse resp, Object data) throws IOException {
        setupJsonResponse(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        JsonUtil.writeJson(resp, ApiResponse.ok(data));
    }

    /**
     * Send JSON error response with status code.
     */
    protected void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        setupJsonResponse(resp);
        resp.setStatus(statusCode);
        JsonUtil.writeJson(resp, ApiResponse.fail(statusCode, message));
    }

    /**
     * Get and validate CSRF token from session.
     */
    protected boolean validateCsrfToken(HttpSession session, String csrfParam) {
        if (session == null || csrfParam == null) {
            return false;
        }
        String csrfSession = (String) session.getAttribute("_csrfToken");
        if (csrfSession == null) {
            return false;
        }
        // Use constant-time comparison to prevent timing attacks
        return constantTimeEquals(csrfSession, csrfParam);
    }

    /**
     * Constant-time string comparison to prevent timing side-channel attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        try {
            return java.security.MessageDigest.isEqual(aBytes, bBytes);
        } catch (Exception e) {
            LoggerUtil.warn(log, "CSRF token comparison failed", e);
            return false;
        }
    }

    /**
     * Handle exceptions with consistent logging and error response.
     */
    protected void handleException(HttpServletRequest req, HttpServletResponse resp, Exception ex) throws IOException {
        if (isJsonRequest(req)) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "BaseHttpServlet#handleException",
                    "Internal server error",
                    ex);
        } else {
            try {
                util.ServletUtil.sendPageInternalServerError(
                        req,
                        resp,
                        log,
                        "BaseHttpServlet#handleException",
                        "An error occurred.",
                        ex);
            } catch (ServletException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Check if this is a JSON API request.
     */
    protected boolean isJsonRequest(HttpServletRequest req) {
        String contentType = req.getContentType();
        String accept = req.getHeader("Accept");
        return (contentType != null && contentType.contains("application/json")) ||
               (accept != null && accept.contains("application/json"));
    }

    /**
     * Get integer parameter with default value.
     */
    protected int getIntParameter(HttpServletRequest req, String paramName, int defaultValue) {
        try {
            String value = req.getParameter(paramName);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            LoggerUtil.debug(log, "Invalid integer parameter %s: %s", paramName, req.getParameter(paramName));
            return defaultValue;
        }
    }

    /**
     * Get long parameter with default value.
     */
    protected long getLongParameter(HttpServletRequest req, String paramName, long defaultValue) {
        try {
            String value = req.getParameter(paramName);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            LoggerUtil.debug(log, "Invalid long parameter %s: %s", paramName, req.getParameter(paramName));
            return defaultValue;
        }
    }
}
