package dao.system;

import config.AppConfig;
import util.LoggerUtil;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * ConnectionPool - manages SQL Server connections.
 *
 * Priority:
 *   1. Tomcat JDBC Pool
 *   2. Tomcat DBCP2
 *   3. DriverManager fallback
 */
public final class ConnectionPool {

    private static final Logger log = Logger.getLogger(ConnectionPool.class.getName());

    // Pool sizing
    private static final int  POOL_INITIAL_SIZE           = 5;
    private static final int  POOL_MAX_ACTIVE             = 30;
    private static final int  POOL_MAX_IDLE               = 10;
    private static final int  POOL_MIN_IDLE               = 5;
    private static final int  POOL_MAX_WAIT_MS            = 8_000;

    // Idle eviction
    private static final int  POOL_EVICTION_INTERVAL_MS   = 60_000;
    private static final int  POOL_MIN_EVICTABLE_IDLE_MS  = 300_000;
    private static final int  POOL_EVICTION_TESTS_PER_RUN = 3;

    // Validation / lifecycle
    private static final long POOL_VALIDATION_INTERVAL_MS = 30_000L;
    private static final int  POOL_VALIDATION_TIMEOUT_S   = 5;
    private static final long POOL_MAX_AGE_MS             = 60L * 60L * 1000L;
    private static final int  POOL_SUSPECT_TIMEOUT_S      = 120;
    private static final String TOMCAT_JDBC_INTERCEPTORS =
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
          + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
          + "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer";

    // Abandoned detection
    private static final int  POOL_ABANDONED_TIMEOUT_S    = 300;
    private static final int  POOL_ABANDON_WHEN_PERCENT   = 0;

    private static final String VALIDATION_QUERY = "SELECT 1";

    private static volatile DataSource dataSource;
    private static volatile boolean    poolActive = false;

    static {
        initPool();
    }

    private ConnectionPool() {}

    // =========================================================
    // Public API
    // =========================================================

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return DaoSqlLogger.wrapConnection(dataSource.getConnection());
        }
        return DaoSqlLogger.wrapConnection(java.sql.DriverManager.getConnection(
                AppConfig.DB_JDBC_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD));
    }

    public static boolean isPoolActive() {
        return poolActive;
    }

    public static void logPoolStats() {
        if (dataSource == null) {
            LoggerUtil.warn(log, "[ConnectionPool] No pool available - using DriverManager");
            return;
        }
        try {
            Class<?> cls = dataSource.getClass();
            try {
                Object active = cls.getMethod("getNumActive").invoke(dataSource);
                Object idle   = cls.getMethod("getNumIdle").invoke(dataSource);
                LoggerUtil.info(log, "[ConnectionPool] active=" + active
                        + " idle=" + idle + " maxActive=" + POOL_MAX_ACTIVE);
            } catch (NoSuchMethodException e) {
                // DBCP2 and Tomcat JDBC Pool expose the same methods here.
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "[ConnectionPool] Unable to read pool stats", e);
        }
    }

    // =========================================================
    // Initialization
    // =========================================================

    private static void initPool() {
        boolean tomcatLoggingAvailable = isClassAvailable("org.apache.juli.logging.LogFactory");
        boolean dbcp2Available = tomcatLoggingAvailable
                && isClassAvailable("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
        boolean tomcatJdbcAvailable = tomcatLoggingAvailable
                && isClassAvailable("org.apache.tomcat.jdbc.pool.DataSource");

        if (dbcp2Available && tryInitDbcp2Pool()) {
            return;
        }
        if (tomcatJdbcAvailable && tryInitTomcatJdbcPool()) {
            return;
        }

        if (tomcatLoggingAvailable) {
            LoggerUtil.info(log,
                    "[ConnectionPool] No usable Tomcat pool implementation found, using DriverManager fallback.");
        } else {
            LoggerUtil.info(log,
                    "[ConnectionPool] Tomcat pool libraries are not on the classpath; using DriverManager fallback.");
        }
    }

    private static boolean tryInitTomcatJdbcPool() {
        try {
            Class<?> cls = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
            Object ds = cls.getDeclaredConstructor().newInstance();

            set(cls, ds, "setDriverClassName", String.class, AppConfig.DB_DRIVER_CLASS);
            set(cls, ds, "setUrl", String.class, AppConfig.DB_JDBC_URL);
            set(cls, ds, "setUsername", String.class, AppConfig.DB_USER);
            set(cls, ds, "setPassword", String.class, AppConfig.DB_PASSWORD);

            set(cls, ds, "setInitialSize", int.class, POOL_INITIAL_SIZE);
            set(cls, ds, "setMaxActive", int.class, POOL_MAX_ACTIVE);
            set(cls, ds, "setMaxIdle", int.class, POOL_MAX_IDLE);
            set(cls, ds, "setMinIdle", int.class, POOL_MIN_IDLE);
            set(cls, ds, "setMaxWait", int.class, POOL_MAX_WAIT_MS);

            set(cls, ds, "setDefaultAutoCommit", Boolean.class, Boolean.TRUE);
            set(cls, ds, "setDefaultReadOnly", Boolean.class, Boolean.FALSE);
            set(cls, ds, "setValidationQuery", String.class, VALIDATION_QUERY);
            set(cls, ds, "setValidationQueryTimeout", int.class, POOL_VALIDATION_TIMEOUT_S);
            set(cls, ds, "setTestOnBorrow", boolean.class, true);
            setOptional(cls, ds, "setTestOnConnect", boolean.class, true);
            set(cls, ds, "setTestWhileIdle", boolean.class, true);
            set(cls, ds, "setValidationInterval", long.class, POOL_VALIDATION_INTERVAL_MS);
            setOptional(cls, ds, "setLogValidationErrors", boolean.class, true);
            setOptional(cls, ds, "setUseDisposableConnectionFacade", boolean.class, true);
            setOptional(cls, ds, "setFairQueue", boolean.class, true);
            setOptional(cls, ds, "setJmxEnabled", boolean.class, true);
            setOptional(cls, ds, "setJdbcInterceptors", String.class, TOMCAT_JDBC_INTERCEPTORS);
            set(cls, ds, "setRollbackOnReturn", boolean.class, true);
            setOptional(cls, ds, "setMaxAge", long.class, POOL_MAX_AGE_MS);
            setOptional(cls, ds, "setSuspectTimeout", int.class, POOL_SUSPECT_TIMEOUT_S);

            set(cls, ds, "setTimeBetweenEvictionRunsMillis", int.class, POOL_EVICTION_INTERVAL_MS);
            set(cls, ds, "setMinEvictableIdleTimeMillis", int.class, POOL_MIN_EVICTABLE_IDLE_MS);
            set(cls, ds, "setNumTestsPerEvictionRun", int.class, POOL_EVICTION_TESTS_PER_RUN);

            set(cls, ds, "setRemoveAbandoned", boolean.class, true);
            set(cls, ds, "setRemoveAbandonedTimeout", int.class, POOL_ABANDONED_TIMEOUT_S);
            setOptional(cls, ds, "setAbandonWhenPercentageFull", int.class, POOL_ABANDON_WHEN_PERCENT);
            set(cls, ds, "setLogAbandoned", boolean.class, true);

            DataSource tempDs = (DataSource) ds;
            try (Connection conn = tempDs.getConnection()) {
                LoggerUtil.info(log, "[ConnectionPool] Tomcat JDBC Pool validation connection successful");
            }

            dataSource = tempDs;
            poolActive = true;
            LoggerUtil.info(log, "[ConnectionPool] Tomcat JDBC Pool OK - max=" + POOL_MAX_ACTIVE);
            return true;
        } catch (Throwable e) {
            LoggerUtil.warn(log, "[ConnectionPool] Tomcat JDBC Pool unavailable", e);
            return false;
        }
    }

    private static boolean tryInitDbcp2Pool() {
        try {
            Class<?> cls = Class.forName("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
            Object ds = cls.getDeclaredConstructor().newInstance();

            set(cls, ds, "setDriverClassName", String.class, AppConfig.DB_DRIVER_CLASS);
            set(cls, ds, "setUrl", String.class, AppConfig.DB_JDBC_URL);
            set(cls, ds, "setUsername", String.class, AppConfig.DB_USER);
            set(cls, ds, "setPassword", String.class, AppConfig.DB_PASSWORD);

            set(cls, ds, "setInitialSize", int.class, POOL_INITIAL_SIZE);
            set(cls, ds, "setMaxTotal", int.class, POOL_MAX_ACTIVE);
            set(cls, ds, "setMaxIdle", int.class, POOL_MAX_IDLE);
            set(cls, ds, "setMinIdle", int.class, POOL_MIN_IDLE);
            set(cls, ds, "setMaxWaitMillis", long.class, (long) POOL_MAX_WAIT_MS);

            set(cls, ds, "setDefaultAutoCommit", Boolean.class, Boolean.TRUE);
            set(cls, ds, "setDefaultReadOnly", Boolean.class, Boolean.FALSE);
            set(cls, ds, "setValidationQuery", String.class, VALIDATION_QUERY);
            set(cls, ds, "setValidationQueryTimeout", int.class, POOL_VALIDATION_TIMEOUT_S);
            set(cls, ds, "setTestOnBorrow", boolean.class, true);
            setOptional(cls, ds, "setTestOnCreate", boolean.class, true);
            set(cls, ds, "setTestWhileIdle", boolean.class, true);
            setOptional(cls, ds, "setFastFailValidation", boolean.class, true);
            set(cls, ds, "setTimeBetweenEvictionRunsMillis", long.class, POOL_EVICTION_INTERVAL_MS);
            set(cls, ds, "setMinEvictableIdleTimeMillis", long.class, POOL_MIN_EVICTABLE_IDLE_MS);
            set(cls, ds, "setNumTestsPerEvictionRun", int.class, POOL_EVICTION_TESTS_PER_RUN);
            set(cls, ds, "setRollbackOnReturn", boolean.class, true);
            setOptional(cls, ds, "setLogExpiredConnections", boolean.class, true);
            setOptional(cls, ds, "setMaxConnLifetimeMillis", long.class, POOL_MAX_AGE_MS);

            set(cls, ds, "setRemoveAbandonedOnBorrow", boolean.class, true);
            set(cls, ds, "setRemoveAbandonedOnMaintenance", boolean.class, true);
            set(cls, ds, "setRemoveAbandonedTimeout", int.class, POOL_ABANDONED_TIMEOUT_S);
            set(cls, ds, "setLogAbandoned", boolean.class, true);

            DataSource tempDs = (DataSource) ds;
            try (Connection conn = tempDs.getConnection()) {
                LoggerUtil.info(log, "[ConnectionPool] Tomcat DBCP2 Pool validation connection successful");
            }

            dataSource = tempDs;
            poolActive = true;
            LoggerUtil.info(log, "[ConnectionPool] Tomcat DBCP2 Pool OK - max=" + POOL_MAX_ACTIVE);
            return true;
        } catch (Throwable e) {
            LoggerUtil.warn(log, "[ConnectionPool] Tomcat DBCP2 unavailable", e);
            return false;
        }
    }

    private static void set(Class<?> cls, Object obj, String method, Class<?> paramType, Object value)
            throws Exception {
        cls.getMethod(method, paramType).invoke(obj, value);
    }

    private static void setOptional(Class<?> cls, Object obj, String method, Class<?> paramType, Object value) {
        try {
            set(cls, obj, method, paramType, value);
        } catch (NoSuchMethodException ignored) {
            // Optional setter not available on this pool implementation.
        } catch (Exception e) {
            LoggerUtil.warn(log, "[ConnectionPool] Could not set " + method, e);
        }
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, ConnectionPool.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ex) {
            return false;
        }
    }

    /**
     * Shut down and close the connection pool to prevent classloader/timer memory leaks.
     */
    public static void closePool() {
        if (dataSource != null) {
            try {
                dataSource.getClass().getMethod("close").invoke(dataSource);
                LoggerUtil.info(log, "[ConnectionPool] Connection pool closed successfully on context destroy.");
            } catch (Exception e) {
                LoggerUtil.warn(log, "[ConnectionPool] Error closing pool on context destroy", e);
            } finally {
                dataSource = null;
                poolActive = false;
            }
        }
    }
}
