package listener;

import config.AppConfig;
import dao.system.ConnectionPool;
import util.LoggerUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * AppStartupListener — Validates critical configuration at application startup.
 *
 * Runs automatically when the application starts and fails fast if required
 * secrets are not properly configured via environment variables in production.
 *
 * @author fruitmkt-team
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Load .env TRƯỚC KHI AppConfig được class-loaded lần đầu.
        // DotEnvLoader ghi vào System.setProperty() để AppConfig.getEnvOrDefault() đọc được.
        String realPath = sce.getServletContext().getRealPath("");
        util.DotEnvLoader.load(realPath);
        Path logFile = LoggerUtil.configureFileLogging(realPath);
        String appEnv = System.getenv("APP_ENV");
        if (appEnv == null || appEnv.isBlank()) {
            appEnv = System.getProperty("APP_ENV");
        }
        if (appEnv == null || appEnv.isBlank()) {
            appEnv = "development";
        }
        sce.getServletContext().setAttribute("appEnv", appEnv);

        Path effectiveLogFile = logFile != null ? logFile : LoggerUtil.getConfiguredLogFile();
        if (effectiveLogFile == null) {
            effectiveLogFile = Path.of(System.getProperty("user.dir"), AppConfig.LOG_DIR, AppConfig.LOG_FILE_NAME);
        }
        sce.getServletContext().setAttribute("appLogFilePath", effectiveLogFile.toString());
        sce.getServletContext().setAttribute("logFilePath", effectiveLogFile.toString());

        try {
            AppConfig.validateSecretsForProduction();
            LoggerUtil.info(log, "[AppStartup] Configuration validation passed");
            if (logFile != null) {
                LoggerUtil.info(log, "[AppStartup] DAO SQL logs: %s", logFile.toAbsolutePath());
            }
        } catch (IllegalStateException ex) {
            LoggerUtil.error(log, "[AppStartup] FATAL: " + ex.getMessage(), ex);
            throw ex;
        }

        // Automatic DB migration check for expiry_warning_days and low_stock_threshold columns
        try (java.sql.Connection conn = ConnectionPool.getConnection()) {
            boolean hasExpiryCol = false;
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "shop_owner_profiles", "expiry_warning_days")) {
                if (rs.next()) {
                    hasExpiryCol = true;
                }
            }
            if (!hasExpiryCol) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE shop_owner_profiles ADD expiry_warning_days INT NOT NULL CONSTRAINT DF_shop_owner_profiles_expiry_warning_days DEFAULT 3");
                    LoggerUtil.info(log, "[AppStartup] Successfully migrated shop_owner_profiles table to add expiry_warning_days column.");
                }
            }

            boolean hasLowStockCol = false;
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "shop_owner_profiles", "low_stock_threshold")) {
                if (rs.next()) {
                    hasLowStockCol = true;
                }
            }
            if (!hasLowStockCol) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE shop_owner_profiles ADD low_stock_threshold INT NOT NULL CONSTRAINT DF_shop_owner_profiles_low_stock_threshold DEFAULT 5");
                    LoggerUtil.info(log, "[AppStartup] Successfully migrated shop_owner_profiles table to add low_stock_threshold column.");
                }
            }

            boolean hasAutoApproveKey = false;
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM system_config WHERE config_key = 'product_auto_approve'")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasAutoApproveKey = true;
                    }
                }
            }
            if (!hasAutoApproveKey) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO system_config (config_key, config_value, description, data_type) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, "product_auto_approve");
                    ps.setString(2, "false");
                    ps.setString(3, "Tự động duyệt sản phẩm khi tạo mới hoặc cập nhật (true/false). Mặc định false.");
                    ps.setString(4, "BOOLEAN");
                    ps.executeUpdate();
                    LoggerUtil.info(log, "[AppStartup] Successfully seeded 'product_auto_approve' configuration key.");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "[AppStartup] Warning during automatic DB migration", e);
        }

        // Warm up pool và log trạng thái ngay khi startup
        ConnectionPool.logPoolStats();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerUtil.info(log, "[AppStartup] contextDestroyed: Closing ConnectionPool to prevent Timer leaks...");
        ConnectionPool.closePool();

        LoggerUtil.info(log, "[AppStartup] contextDestroyed: Cleaning up JDBC drivers to prevent Timer already cancelled error...");
        java.util.Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            // Lọc chỉ gỡ Driver thuộc WebApp ClassLoader hiện tại (Tránh xóa nhầm Driver của Tomcat toàn cục nếu có)
            if (driver.getClass().getClassLoader() == this.getClass().getClassLoader()) {
                try {
                    java.sql.DriverManager.deregisterDriver(driver);
                    LoggerUtil.info(log, "[AppStartup] Deregistered JDBC driver: " + driver);
                } catch (java.sql.SQLException e) {
                    LoggerUtil.error(log, "[AppStartup] Error deregistering driver: " + driver, e);
                }
            }
        }
    }
}
