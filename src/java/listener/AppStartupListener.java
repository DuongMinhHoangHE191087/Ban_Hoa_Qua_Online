package listener;

import config.AppConfig;
import dao.system.ConnectionPool;
import util.LoggerUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
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

        try {
            AppConfig.validateSecretsForProduction();
            LoggerUtil.info(log, "[AppStartup] Configuration validation passed");
        } catch (IllegalStateException ex) {
            LoggerUtil.error(log, "[AppStartup] FATAL: " + ex.getMessage(), ex);
            throw ex;
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
