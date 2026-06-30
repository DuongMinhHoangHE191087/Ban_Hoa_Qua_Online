package filter;

import exception.BusinessException;
import model.response.ApiResponse;
import util.ErrorMessageUtil;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * GlobalExceptionFilter — lớp chặn lỗi trung tâm cho toàn bộ ứng dụng.
 *
 * Mục tiêu:
 *   - Không để exception thô đi ra ngoài thành lỗi 500 mặc định của container
 *   - Phân biệt lỗi nghiệp vụ và lỗi hệ thống
 *   - Trả JSON cho API/AJAX, trả trang lỗi thân thiện cho HTML
 *   - Gắn requestId để đối chiếu với log file
 */
public class GlobalExceptionFilter implements Filter {

    private static final Logger log = Logger.getLogger(GlobalExceptionFilter.class.getName());

    private static final String REQUEST_ID_ATTR = "requestId";
    private static final String ERROR_ID_ATTR = "errorId";
    private static final String LOG_FILE_ATTR = "logFilePath";
    private static final String SHOW_ERROR_DETAILS_ATTR = "showErrorDetails";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestId = ensureRequestId(req);
        req.setAttribute(REQUEST_ID_ATTR, requestId);
        resp.setHeader("X-Request-ID", requestId);

        try {
            chain.doFilter(request, response);
        } catch (BusinessException e) {
            handleBusinessException(req, resp, e, requestId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            handleValidationException(req, resp, e, requestId);
        } catch (SecurityException e) {
            handleSecurityException(req, resp, e, requestId);
        } catch (Exception e) {
            handleSystemException(req, resp, e, requestId);
        }
    }

    private void handleBusinessException(HttpServletRequest req, HttpServletResponse resp,
            Exception e, String requestId) throws IOException, ServletException {
        ErrorMessageUtil.logRequestException(log, req, "GlobalExceptionFilter", e, requestId);
        String userMessage = ErrorMessageUtil.getUserMessage(e);

        if (expectsJson(req)) {
            writeJsonError(req, resp, HttpServletResponse.SC_BAD_REQUEST, userMessage, requestId, e);
            return;
        }

        redirectBackWithFlash(req, resp, ErrorMessageUtil.withReference(userMessage, requestId));
    }

    private void handleValidationException(HttpServletRequest req, HttpServletResponse resp,
            Exception e, String requestId) throws IOException, ServletException {
        ErrorMessageUtil.logRequestException(log, req, "GlobalExceptionFilter", e, requestId);
        String userMessage = ErrorMessageUtil.getUserMessage(e);

        if (expectsJson(req)) {
            writeJsonError(req, resp, HttpServletResponse.SC_BAD_REQUEST, userMessage, requestId, e);
            return;
        }

        redirectBackWithFlash(req, resp, ErrorMessageUtil.withReference(userMessage, requestId));
    }

    private void handleSecurityException(HttpServletRequest req, HttpServletResponse resp,
            SecurityException e, String requestId) throws IOException, ServletException {
        ErrorMessageUtil.logRequestException(log, req, "GlobalExceptionFilter", e, requestId);

        if (expectsJson(req)) {
            writeJsonError(req, resp, HttpServletResponse.SC_FORBIDDEN,
                    ErrorMessageUtil.MSG_UNAUTHORIZED, requestId, e);
            return;
        }

        if (resp.isCommitted()) {
            return;
        }

        resp.sendError(HttpServletResponse.SC_FORBIDDEN, ErrorMessageUtil.MSG_UNAUTHORIZED);
    }

    private void handleSystemException(HttpServletRequest req, HttpServletResponse resp,
            Exception e, String requestId) throws IOException, ServletException {
        ErrorMessageUtil.logRequestException(log, req, "GlobalExceptionFilter", e, requestId);

        if (expectsJson(req)) {
            writeJsonError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ErrorMessageUtil.withReference(ErrorMessageUtil.MSG_INTERNAL_ERROR, requestId),
                    requestId,
                    e);
            return;
        }

        forwardToErrorPage(req, resp, e, requestId);
    }

    private void writeJsonError(HttpServletRequest req, HttpServletResponse resp, int status,
            String message, String requestId, Exception e) throws IOException {
        if (resp.isCommitted()) {
            return;
        }

        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("requestId", requestId);
        meta.put("errorType", e.getClass().getSimpleName());
        String logFilePath = resolveLogFilePath(req);
        if (!logFilePath.isBlank()) {
            meta.put("logFilePath", logFilePath);
        }
        if (e instanceof BusinessException businessException) {
            meta.put("errorCode", businessException.getErrorCode());
        }

        JsonUtil.writeJson(resp, ApiResponse.fail(status, message, meta));
    }

    private void forwardToErrorPage(HttpServletRequest req, HttpServletResponse resp,
            Exception e, String requestId) throws IOException, ServletException {
        if (resp.isCommitted()) {
            return;
        }

        prepareErrorAttributes(req, requestId, e);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.getRequestDispatcher("/WEB-INF/jsp/error/500.jsp").forward(req, resp);
    }

    private void prepareErrorAttributes(HttpServletRequest req, String requestId, Exception e) {
        String logFilePath = resolveLogFilePath(req);
        boolean showErrorDetails = isDevelopmentMode(req);

        req.setAttribute(ERROR_ID_ATTR, requestId);
        req.setAttribute(LOG_FILE_ATTR, logFilePath);
        req.setAttribute(SHOW_ERROR_DETAILS_ATTR, showErrorDetails);
        req.setAttribute("jakarta.servlet.error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.setAttribute("jakarta.servlet.error.request_uri", req.getRequestURI());
        req.setAttribute("jakarta.servlet.error.servlet_name", req.getServletPath());
        req.setAttribute("jakarta.servlet.error.exception", e);
        req.setAttribute("jakarta.servlet.error.exception_type", e.getClass());
        req.setAttribute("jakarta.servlet.error.message",
                ErrorMessageUtil.withReference(ErrorMessageUtil.MSG_INTERNAL_ERROR, requestId));
    }

    private void redirectBackWithFlash(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        if (resp.isCommitted()) {
            return;
        }

        HttpSession session = req.getSession();
        SessionUtil.setFlashMessage(session, message, "error");
        resp.sendRedirect(resolveRedirectTarget(req));
    }

    private String resolveRedirectTarget(HttpServletRequest req) {
        String referer = req.getHeader("Referer");
        if (isSafeReferer(req, referer)) {
            return referer;
        }

        return req.getContextPath() + "/";
    }

    private boolean isSafeReferer(HttpServletRequest req, String referer) {
        if (referer == null || referer.isBlank()) {
            return false;
        }

        StringBuilder origin = new StringBuilder();
        origin.append(req.getScheme()).append("://").append(req.getServerName());
        int port = req.getServerPort();
        boolean defaultHttp = "http".equalsIgnoreCase(req.getScheme()) && port == 80;
        boolean defaultHttps = "https".equalsIgnoreCase(req.getScheme()) && port == 443;
        if (!defaultHttp && !defaultHttps) {
            origin.append(':').append(port);
        }

        return referer.startsWith(origin + req.getContextPath());
    }

    private boolean expectsJson(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri != null && uri.startsWith(req.getContextPath() + "/api/")) {
            return true;
        }

        String format = req.getParameter("format");
        if ("json".equalsIgnoreCase(format)) {
            return true;
        }

        String xRequestedWith = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
            return true;
        }

        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        String contentType = req.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String ensureRequestId(HttpServletRequest req) {
        Object existing = req.getAttribute(REQUEST_ID_ATTR);
        if (existing instanceof String existingId && !existingId.isBlank()) {
            return existingId;
        }

        String requestId = UUID.randomUUID().toString().substring(0, 12);
        req.setAttribute(REQUEST_ID_ATTR, requestId);
        return requestId;
    }

    private String resolveLogFilePath(HttpServletRequest req) {
        Object attr = req.getServletContext().getAttribute(LOG_FILE_ATTR);
        if (attr instanceof String path && !path.isBlank()) {
            return path;
        }

        Path configuredLogFile = LoggerUtil.getConfiguredLogFile();
        if (configuredLogFile != null) {
            return configuredLogFile.toString();
        }

        return Path.of(System.getProperty("user.dir"), "logs", "log.txt").toString();
    }

    private boolean isDevelopmentMode(HttpServletRequest req) {
        Object appEnv = req.getServletContext().getAttribute("appEnv");
        if (appEnv instanceof String env && !env.isBlank()) {
            return !"production".equalsIgnoreCase(env);
        }

        return !"production".equalsIgnoreCase(config.AppConfig.APP_ENV);
    }
}
