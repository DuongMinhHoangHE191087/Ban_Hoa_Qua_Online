package util;

import exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ErrorMessageUtil — Standardize error handling across all servlets.
 * PRINCIPLE: Log detailed errors server-side, return generic messages to users.
 *
 * Prevents SQL injection info disclosure, stack trace leaks, and internal path exposure.
 * All exceptions caught in servlet doPost/doGet should route through here.
 *
 * @author fruitmkt-team
 */
public final class ErrorMessageUtil {

    // Generic user-facing messages (never expose internals)
    public static final String MSG_INTERNAL_ERROR = "Đã xảy ra lỗi. Vui lòng thử lại sau.";
    public static final String MSG_DB_ERROR = "Không thể kết nối cơ sở dữ liệu. Vui lòng thử lại.";
    public static final String MSG_FILE_ERROR = "Lỗi xử lý tệp. Vui lòng thử lại.";
    public static final String MSG_VALIDATION_ERROR = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
    public static final String MSG_UNAUTHORIZED = "Bạn không có quyền thực hiện hành động này.";
    public static final String MSG_NOT_FOUND = "Không tìm thấy dữ liệu yêu cầu.";
    public static final String MSG_DUPLICATE = "Dữ liệu đã tồn tại. Vui lòng kiểm tra lại.";
    public static final String MSG_TIMEOUT = "Yêu cầu quá thời gian cho phép. Vui lòng thử lại.";
    public static final String MSG_RATE_LIMIT = "Quá nhiều yêu cầu. Vui lòng thử lại sau.";

    /**
     * Return generic user message based on exception type.
     * Safe to display directly in responses.
     */
    public static String getUserMessage(Exception e) {
        if (e == null) {
            return MSG_INTERNAL_ERROR;
        }

        if (e instanceof BusinessException) {
            String message = ((BusinessException) e).getPublicMessage();
            return message == null || message.isBlank() ? MSG_INTERNAL_ERROR : message;
        }
        if (e instanceof java.sql.SQLException) {
            return MSG_DB_ERROR;
        }
        if (e instanceof java.io.IOException) {
            return MSG_FILE_ERROR;
        }
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return MSG_VALIDATION_ERROR;
        }
        if (e instanceof SecurityException) {
            return MSG_UNAUTHORIZED;
        }
        if (e instanceof java.util.concurrent.TimeoutException) {
            return MSG_TIMEOUT;
        }

        return MSG_INTERNAL_ERROR;
    }

    /**
     * Append a safe reference ID to a user-facing message.
     */
    public static String withReference(String message, String requestId) {
        String base = (message == null || message.isBlank()) ? MSG_INTERNAL_ERROR : message.trim();
        if (requestId == null || requestId.isBlank()) {
            return base;
        }
        return base + " (Mã tham chiếu: " + sanitizeForLog(requestId) + ")";
    }

    /**
     * Get safe string for logging (no newlines/tabs to prevent log injection).
     */
    public static String getSafeLogMessage(Exception e) {
        if (e == null) {
            return "(null exception)";
        }

        String msg = e instanceof BusinessException be
                ? "[" + be.getErrorCode() + "] " + be.getPublicMessage()
                : e.toString();
        if (msg == null || msg.isBlank()) {
            return e.getClass().getSimpleName();
        }

        // Sanitize: replace newline, carriage return, tab with underscore
        return msg.replaceAll("[\\r\\n\\t]", "_");
    }

    /**
     * Log exception with context. Replaces scattered try/catch blocks.
     * Logs full details server-side for debugging, never exposed to client.
     *
     * Example usage:
     *   try {
     *       orderDAO.save(order);
     *   } catch (SQLException e) {
     *       ErrorMessageUtil.logException(log, "Failed to save order for customer=" + customerId, e);
     *       throw new Exception(ErrorMessageUtil.MSG_DB_ERROR);
     *   }
     */
    public static void logException(Logger log, String context, Exception e) {
        if (log == null || e == null) {
            return;
        }

        String logMessage = "[" + context + "] " + getSafeLogMessage(e);

        if (e instanceof BusinessException be) {
            log.log(Level.WARNING, logMessage + " | businessCode=" + be.getErrorCode(), e);
        } else if (e instanceof java.sql.SQLException) {
            // Database errors: log with full stack trace for debugging
            log.log(Level.SEVERE, logMessage, e);
        } else if (e instanceof java.io.IOException) {
            // File/IO errors: log warning with stack trace
            log.log(Level.WARNING, logMessage, e);
        } else if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            // Validation errors: log at INFO level (expected/common)
            log.log(Level.INFO, logMessage, e);
        } else if (e instanceof SecurityException) {
            // Security violations: log as WARNING
            log.log(Level.WARNING, logMessage, e);
        } else {
            // Unknown: log as severe
            log.log(Level.SEVERE, logMessage, e);
        }
    }

    /**
     * Convenience method: log exception and return safe user message in one call.
     * Returns the generic message to be flashed to user.
     */
    public static String logAndGetUserMessage(Logger log, String context, Exception e) {
        logException(log, context, e);
        return getUserMessage(e);
    }

    /**
     * Check if exception is a "user error" (validation) vs "system error" (infrastructure).
     * User errors get handled gracefully; system errors get escalated.
     */
    public static boolean isUserError(Exception e) {
        return e instanceof BusinessException
                || e instanceof IllegalArgumentException
                || e instanceof IllegalStateException;
    }

    /**
     * Check if exception is security-critical and should be escalated immediately.
     */
    public static boolean isSecurityError(Exception e) {
        return e instanceof SecurityException;
    }

    /**
     * Sanitize user input before including in logs.
     * Removes characters that could be used for log injection attacks.
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "(null)";
        }
        return input.replaceAll("[\\r\\n\\t]", "_");
    }

    /**
     * Build a compact request summary for error logs.
     */
    public static String buildRequestSummary(HttpServletRequest req, String requestId) {
        if (req == null) {
            return "requestId=" + sanitizeForLog(requestId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("requestId=").append(sanitizeForLog(requestId));
        sb.append(" method=").append(sanitizeForLog(req.getMethod()));
        sb.append(" uri=").append(sanitizeForLog(req.getRequestURI()));

        String query = req.getQueryString();
        if (query != null && !query.isBlank()) {
            sb.append(" query=").append(sanitizeForLog(query));
        }

        String accept = req.getHeader("Accept");
        if (accept != null && !accept.isBlank()) {
            sb.append(" accept=").append(sanitizeForLog(accept));
        }

        String xRequestedWith = req.getHeader("X-Requested-With");
        if (xRequestedWith != null && !xRequestedWith.isBlank()) {
            sb.append(" xrw=").append(sanitizeForLog(xRequestedWith));
        }

        String contentType = req.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            sb.append(" contentType=").append(sanitizeForLog(contentType));
        }

        String remoteAddr = req.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isBlank()) {
            sb.append(" remoteAddr=").append(sanitizeForLog(remoteAddr));
        }

        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser != null) {
            sb.append(" userId=").append(currentUser.getUserId());
            sb.append(" role=").append(sanitizeForLog(currentUser.getRole()));
        }

        String params = summarizeParameters(req);
        if (!params.isEmpty()) {
            sb.append(" params=").append(params);
        }

        return sb.toString();
    }

    /**
     * Log request-scoped exception details with a stable request summary.
     */
    public static void logRequestException(Logger log, HttpServletRequest req, String context, Exception e, String requestId) {
        if (log == null || e == null) {
            return;
        }

        String requestSummary = buildRequestSummary(req, requestId);
        String logMessage = "[" + context + "] " + requestSummary + " | " + getSafeLogMessage(e);

        if (e instanceof BusinessException be) {
            log.log(Level.WARNING, logMessage + " | businessCode=" + be.getErrorCode(), e);
        } else if (e instanceof java.sql.SQLException) {
            log.log(Level.SEVERE, logMessage, e);
        } else if (e instanceof java.io.IOException) {
            log.log(Level.WARNING, logMessage, e);
        } else if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            log.log(Level.WARNING, logMessage, e);
        } else if (e instanceof SecurityException) {
            log.log(Level.WARNING, logMessage, e);
        } else {
            log.log(Level.SEVERE, logMessage, e);
        }
    }

    private static String summarizeParameters(HttpServletRequest req) {
        java.util.Map<String, String[]> params = req.getParameterMap();
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (java.util.Map.Entry<String, String[]> entry : params.entrySet()) {
            if (count >= 6) {
                sb.append("...");
                break;
            }
            if (count > 0) {
                sb.append(", ");
            }

            String key = sanitizeForLog(entry.getKey());
            sb.append(key).append('=');
            sb.append(formatParameterValue(entry.getKey(), entry.getValue()));
            count++;
        }
        sb.append('}');
        return sb.toString();
    }

    private static String formatParameterValue(String key, String[] values) {
        if (values == null || values.length == 0) {
            return "[]";
        }

        if (isSensitiveParameter(key)) {
            return "***";
        }

        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(values.length, 3);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"').append(truncate(sanitizeForLog(values[i]), 120)).append('"');
        }
        if (values.length > limit) {
            sb.append(", ...");
        }
        sb.append(']');
        return sb.toString();
    }

    private static boolean isSensitiveParameter(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("csrf")
                || normalized.contains("otp")
                || normalized.contains("code")
                || normalized.contains("cookie")
                || normalized.contains("key");
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private ErrorMessageUtil() {
        /* Utility class — no instantiation */
    }
}
