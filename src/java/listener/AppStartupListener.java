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

        // Warm up pool và log trạng thái ngay khi startup
        ConnectionPool.logPoolStats();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionPool.shutdown();
    }
}
