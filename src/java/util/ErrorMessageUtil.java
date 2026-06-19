package util;

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
     * Get safe string for logging (no newlines/tabs to prevent log injection).
     */
    public static String getSafeLogMessage(Exception e) {
        if (e == null) {
            return "(null exception)";
        }

        String msg = e.getMessage();
        if (msg == null) {
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

        if (e instanceof java.sql.SQLException) {
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
        return e instanceof IllegalArgumentException || e instanceof IllegalStateException;
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

    private ErrorMessageUtil() {
        /* Utility class — no instantiation */
    }
}
