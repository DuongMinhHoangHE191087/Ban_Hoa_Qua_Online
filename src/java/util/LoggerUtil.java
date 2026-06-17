package util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoggerUtil — Centralized logging utility.
 *
 * Wraps java.util.logging to provide consistent logging across the application.
 * Can be easily migrated to SLF4J in the future by changing the implementation.
 *
 * @author fruitmkt-team
 */
public class LoggerUtil {

    private LoggerUtil() {
        /* Utility class — không khởi tạo */
    }

    /**
     * Get a logger for the specified class.
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Log a message at INFO level.
     */
    public static void info(Logger logger, String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Log a formatted message at INFO level.
     */
    public static void info(Logger logger, String message, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, String.format(message, args));
        }
    }

    /**
     * Log a message at WARNING level.
     */
    public static void warn(Logger logger, String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Log a formatted message at WARNING level.
     */
    public static void warn(Logger logger, String message, Object... args) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, String.format(message, args));
        }
    }

    /**
     * Log a warning with exception.
     */
    public static void warn(Logger logger, String message, Throwable thrown) {
        logger.log(Level.WARNING, message, thrown);
    }

    /**
     * Log a message at ERROR (SEVERE) level.
     */
    public static void error(Logger logger, String message) {
        logger.log(Level.SEVERE, message);
    }

    /**
     * Log a formatted message at ERROR (SEVERE) level.
     */
    public static void error(Logger logger, String message, Object... args) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, String.format(message, args));
        }
    }

    /**
     * Log an error with exception.
     */
    public static void error(Logger logger, String message, Throwable thrown) {
        logger.log(Level.SEVERE, message, thrown);
    }

    /**
     * Log at DEBUG (FINE) level.
     */
    public static void debug(Logger logger, String message) {
        logger.log(Level.FINE, message);
    }

    /**
     * Log a formatted message at DEBUG (FINE) level.
     */
    public static void debug(Logger logger, String message, Object... args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format(message, args));
        }
    }

    /**
     * Log a debug message with exception.
     */
    public static void debug(Logger logger, String message, Throwable thrown) {
        logger.log(Level.FINE, message, thrown);
    }
}
