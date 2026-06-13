package com.fruitmkt.dao;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.LoggerUtil;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ConnectionPool {

    private static final Logger log = Logger.getLogger(ConnectionPool.class.getName());
    private static DataSource dataSource;

    static {
        // 1. Try org.apache.tomcat.jdbc.pool.DataSource (Tomcat JDBC)
        try {
            Class<?> dsClass = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
            Object ds = dsClass.getDeclaredConstructor().newInstance();
            
            dsClass.getMethod("setDriverClassName", String.class).invoke(ds, AppConfig.DB_DRIVER_CLASS);
            dsClass.getMethod("setUrl", String.class).invoke(ds, AppConfig.DB_JDBC_URL);
            dsClass.getMethod("setUsername", String.class).invoke(ds, AppConfig.DB_USER);
            dsClass.getMethod("setPassword", String.class).invoke(ds, AppConfig.DB_PASSWORD);
            
            dsClass.getMethod("setInitialSize", int.class).invoke(ds, 10);
            dsClass.getMethod("setMaxActive", int.class).invoke(ds, 100);
            dsClass.getMethod("setMaxIdle", int.class).invoke(ds, 30);
            dsClass.getMethod("setMinIdle", int.class).invoke(ds, 10);
            dsClass.getMethod("setMaxWait", int.class).invoke(ds, 10000);
            dsClass.getMethod("setValidationQuery", String.class).invoke(ds, "SELECT 1");
            dsClass.getMethod("setTestOnBorrow", boolean.class).invoke(ds, true);
            
            dataSource = (DataSource) ds;
            LoggerUtil.info(log, "[ConnectionPool] Initialized Tomcat JDBC Connection Pool successfully.");
        } catch (Throwable e) {
            LoggerUtil.warn(log, "[ConnectionPool] Could not load Tomcat JDBC Connection Pool, trying fallback: " + e.getMessage());
            
            // 2. Try org.apache.tomcat.dbcp.dbcp2.BasicDataSource (Tomcat DBCP)
            try {
                Class<?> dsClass = Class.forName("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
                Object ds = dsClass.getDeclaredConstructor().newInstance();
                
                dsClass.getMethod("setDriverClassName", String.class).invoke(ds, AppConfig.DB_DRIVER_CLASS);
                dsClass.getMethod("setUrl", String.class).invoke(ds, AppConfig.DB_JDBC_URL);
                dsClass.getMethod("setUsername", String.class).invoke(ds, AppConfig.DB_USER);
                dsClass.getMethod("setPassword", String.class).invoke(ds, AppConfig.DB_PASSWORD);
                
                dsClass.getMethod("setInitialSize", int.class).invoke(ds, 10);
                dsClass.getMethod("setMaxTotal", int.class).invoke(ds, 100);
                dsClass.getMethod("setMaxIdle", int.class).invoke(ds, 30);
                dsClass.getMethod("setMinIdle", int.class).invoke(ds, 10);
                dsClass.getMethod("setMaxWaitMillis", long.class).invoke(ds, 10000L);
                
                dataSource = (DataSource) ds;
                LoggerUtil.info(log, "[ConnectionPool] Initialized Tomcat DBCP BasicDataSource successfully.");
            } catch (Throwable ex) {
                LoggerUtil.warn(log, "[ConnectionPool] Could not load Tomcat DBCP, connection pooling disabled: " + ex.getMessage());
                dataSource = null;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        return java.sql.DriverManager.getConnection(AppConfig.DB_JDBC_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }
}
