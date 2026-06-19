package util;

import config.AppConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * LoggerUtil — Centralized logging utility.
 *
 * Wraps java.util.logging to provide consistent logging across the application.
 * Can be easily migrated to SLF4J in the future by changing the implementation.
 *
 * @author fruitmkt-team
 */
public class LoggerUtil {

    private static final Object FILE_LOG_LOCK = new Object();
    private static volatile boolean fileLoggingConfigured = false;
    private static volatile Path configuredLogFile;

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
     * Enable file logging for the application.
     *
     * The log file is created at <basePath>/logs/log.txt when basePath is
     * available, otherwise at <working-dir>/logs/log.txt.
     *
     * @param basePath webapp real path or another writable base directory
     * @return the resolved log file path when configuration succeeds, otherwise null
     */
    public static Path configureFileLogging(String basePath) {
        synchronized (FILE_LOG_LOCK) {
            if (fileLoggingConfigured && configuredLogFile != null) {
                return configuredLogFile;
            }

            Path logFile = resolveLogFilePath(basePath);
            Logger rootLogger = Logger.getLogger("");

            try {
                Path logDir = logFile.getParent();
                if (logDir != null) {
                    Files.createDirectories(logDir);
                }

                FileHandler fileHandler = new FileHandler(logFile.toString(), true);
                fileHandler.setEncoding(StandardCharsets.UTF_8.name());
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(new SimpleFormatter());

                rootLogger.addHandler(fileHandler);
                rootLogger.setLevel(Level.INFO);

                configuredLogFile = logFile;
                fileLoggingConfigured = true;
                rootLogger.log(Level.INFO, "DAO SQL file logging enabled at {0}", logFile.toAbsolutePath());
                return logFile;
            } catch (IOException ex) {
                Logger.getLogger(LoggerUtil.class.getName())
                        .log(Level.WARNING, "Không thể khởi tạo file log: " + logFile.toAbsolutePath(), ex);
                return null;
            }
        }
    }

    /**
     * Returns whether the file logger has already been configured.
     */
    public static boolean isFileLoggingConfigured() {
        return fileLoggingConfigured;
    }

    /**
     * Return the log file resolved during the last successful configuration.
     */
    public static Path getConfiguredLogFile() {
        return configuredLogFile;
    }

    private static Path resolveLogFilePath(String basePath) {
        String root = (basePath == null || basePath.trim().isEmpty())
                ? System.getProperty("user.dir")
                : basePath.trim();
        return Paths.get(root, AppConfig.LOG_DIR, AppConfig.LOG_FILE_NAME);
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
