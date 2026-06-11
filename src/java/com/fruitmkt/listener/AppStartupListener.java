package com.fruitmkt.listener;

import com.fruitmkt.config.AppConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

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

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            AppConfig.validateSecretsForProduction();
            System.out.println("[AppStartup] ✓ Configuration validation passed");
        } catch (IllegalStateException ex) {
            System.err.println("[AppStartup] ✗ FATAL: " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }
}
