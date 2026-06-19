package dao.system;

import config.AppConfig;
import util.LoggerUtil;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * ConnectionPool — quản lý connection pool đến SQL Server.
 *
 * Thứ tự ưu tiên:
 *   1. Tomcat JDBC Pool (hiệu suất cao nhất, hỗ trợ JMX metrics)
 *   2. Tomcat DBCP2 (fallback an toàn)
 *   3. DriverManager   (không pool — chỉ dùng khi test đơn lẻ, KHÔNG production)
 *
 * Tuning chính:
 *   - validationOnBorrow + validationOnCreate: chặn connection chết trước khi vào DAO
 *   - ResetAbandonedTimer + removeAbandoned: tránh false-positive cho query dài
 *   - idle eviction + maxAge: đóng connection cũ/nhàn rỗi để giảm socket chết ngầm
 */
public final class ConnectionPool {

    private static final Logger log = Logger.getLogger(ConnectionPool.class.getName());

    // ---- Pool sizing — phù hợp cho fruit shop traffic (không phải enterprise) ----
    private static final int  POOL_INITIAL_SIZE           = 5;
    private static final int  POOL_MAX_ACTIVE             = 30;
    private static final int  POOL_MAX_IDLE               = 10;
    private static final int  POOL_MIN_IDLE               = 5;
    private static final int  POOL_MAX_WAIT_MS            = 8_000;   // fail fast sau 8s

    // ---- Idle eviction — xóa connection không dùng sau 5 phút ----
    private static final int  POOL_EVICTION_INTERVAL_MS   = 60_000;  // kiểm tra mỗi 60s
    private static final int  POOL_MIN_EVICTABLE_IDLE_MS  = 300_000; // bay nếu idle > 5 phút
    private static final int  POOL_EVICTION_TESTS_PER_RUN = 3;

    // ---- Validation / lifecycle ----
    private static final long POOL_VALIDATION_INTERVAL_MS = 30_000L; // cache validation 30s
    private static final int  POOL_VALIDATION_TIMEOUT_S   = 5;       // fail fast on dead DB
    private static final long POOL_MAX_AGE_MS             = 60L * 60L * 1000L; // recycle after 1h
    private static final int  POOL_SUSPECT_TIMEOUT_S      = 120;     // log suspect connections after 2m
    private static final String TOMCAT_JDBC_INTERCEPTORS =
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
          + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
          + "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer";

    // ---- Abandoned detection — thu hồi connection bị leak ----
    private static final int  POOL_ABANDONED_TIMEOUT_S    = 300;      // reclaim sau 5 phút giữ liên tục
    private static final int  POOL_ABANDON_WHEN_PERCENT   = 0;        // reclaim ngay khi timeout

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
        // DriverManager fallback — cảnh báo vì không có pooling
        return DaoSqlLogger.wrapConnection(java.sql.DriverManager.getConnection(
                AppConfig.DB_JDBC_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD));
    }

    /** Trả về true nếu pool đã khởi tạo thành công (Tomcat JDBC hoặc DBCP2). */
    public static boolean isPoolActive() {
        return poolActive;
    }

    /**
     * Ghi log trạng thái pool ra server log.
     * Gọi từ AppStartupListener hoặc periodic health check.
     */
    public static void logPoolStats() {
        if (dataSource == null) {
            LoggerUtil.warn(log, "[ConnectionPool] Không có pool — đang dùng DriverManager");
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
                // DBCP2 dùng getNumActive / getNumIdle — giống nhau, không cần xử lý
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "[ConnectionPool] Không đọc được pool stats", e);
        }
    }

    public static void closePool() {
        if (dataSource != null) {
            try {
                dataSource.getClass().getMethod("close").invoke(dataSource);
                LoggerUtil.info(log, "[ConnectionPool] Đã đóng DataSource và giải phóng Timer thành công.");
            } catch (Exception e) {
                LoggerUtil.warn(log, "[ConnectionPool] Không thể đóng DataSource: " + e.getMessage());
            }
            dataSource = null;
            poolActive = false;
        }
    }

    // =========================================================
    // Initialization
    // =========================================================

    private static void initPool() {
        if (tryInitDbcp2Pool())      return;
        if (tryInitTomcatJdbcPool()) return;
        LoggerUtil.warn(log,
            "[ConnectionPool] WARN: không có pool library — dùng DriverManager. " +
            "Không phù hợp cho production!");
    }

    private static boolean tryInitTomcatJdbcPool() {
        try {
            Class<?> cls = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
            Object   ds  = cls.getDeclaredConstructor().newInstance();

            set(cls, ds, "setDriverClassName", String.class,  AppConfig.DB_DRIVER_CLASS);
            set(cls, ds, "setUrl",             String.class,  AppConfig.DB_JDBC_URL);
            set(cls, ds, "setUsername",        String.class,  AppConfig.DB_USER);
            set(cls, ds, "setPassword",        String.class,  AppConfig.DB_PASSWORD);

            set(cls, ds, "setInitialSize", int.class, POOL_INITIAL_SIZE);
            set(cls, ds, "setMaxActive",   int.class, POOL_MAX_ACTIVE);
            set(cls, ds, "setMaxIdle",     int.class, POOL_MAX_IDLE);
            set(cls, ds, "setMinIdle",     int.class, POOL_MIN_IDLE);
            set(cls, ds, "setMaxWait",     int.class, POOL_MAX_WAIT_MS);

            // Validation / lifecycle hardening
            set(cls, ds, "setDefaultAutoCommit",   Boolean.class, Boolean.TRUE);
            set(cls, ds, "setDefaultReadOnly",     Boolean.class, Boolean.FALSE);
            set(cls, ds, "setValidationQuery",     String.class,  VALIDATION_QUERY);
            set(cls, ds, "setValidationQueryTimeout", int.class,  POOL_VALIDATION_TIMEOUT_S);
            set(cls, ds, "setTestOnBorrow",        boolean.class, true);
            setOptional(cls, ds, "setTestOnConnect",       boolean.class, true);
            set(cls, ds, "setTestWhileIdle",       boolean.class, true);
            set(cls, ds, "setValidationInterval",  long.class,    POOL_VALIDATION_INTERVAL_MS);
            setOptional(cls, ds, "setLogValidationErrors", boolean.class,  true);
            setOptional(cls, ds, "setUseDisposableConnectionFacade", boolean.class, true);
            setOptional(cls, ds, "setFairQueue",           boolean.class,  true);
            setOptional(cls, ds, "setJmxEnabled",          boolean.class,  true);
            setOptional(cls, ds, "setJdbcInterceptors",    String.class,   TOMCAT_JDBC_INTERCEPTORS);
            set(cls, ds, "setRollbackOnReturn",    boolean.class,  true);
            setOptional(cls, ds, "setMaxAge",              long.class,     POOL_MAX_AGE_MS);
            setOptional(cls, ds, "setSuspectTimeout",      int.class,      POOL_SUSPECT_TIMEOUT_S);

            // Idle eviction
            set(cls, ds, "setTimeBetweenEvictionRunsMillis", int.class, POOL_EVICTION_INTERVAL_MS);
            set(cls, ds, "setMinEvictableIdleTimeMillis",    int.class, POOL_MIN_EVICTABLE_IDLE_MS);
            set(cls, ds, "setNumTestsPerEvictionRun",        int.class, POOL_EVICTION_TESTS_PER_RUN);

            // Abandoned connection recovery — thu hồi kết nối bị giữ > 60s
            set(cls, ds, "setRemoveAbandoned",        boolean.class, true);
            set(cls, ds, "setRemoveAbandonedTimeout", int.class,     POOL_ABANDONED_TIMEOUT_S);
            setOptional(cls, ds, "setAbandonWhenPercentageFull", int.class, POOL_ABANDON_WHEN_PERCENT);
            set(cls, ds, "setLogAbandoned",           boolean.class, true);

            DataSource tempDs = (DataSource) ds;
            // Test connection to trigger internal initialization & validate connectivity
            try (Connection conn = tempDs.getConnection()) {
                LoggerUtil.info(log, "[ConnectionPool] Tomcat JDBC Pool validation connection successful");
            }

            dataSource = tempDs;
            poolActive = true;
            LoggerUtil.info(log, "[ConnectionPool] Tomcat JDBC Pool OK — max=" + POOL_MAX_ACTIVE);
            return true;
        } catch (Throwable e) {
            LoggerUtil.warn(log, "[ConnectionPool] Tomcat JDBC Pool không khả dụng", e);
            return false;
        }
    }

    private static boolean tryInitDbcp2Pool() {
        try {
            Class<?> cls = Class.forName("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
            Object   ds  = cls.getDeclaredConstructor().newInstance();

            set(cls, ds, "setDriverClassName", String.class,  AppConfig.DB_DRIVER_CLASS);
            set(cls, ds, "setUrl",             String.class,  AppConfig.DB_JDBC_URL);
            set(cls, ds, "setUsername",        String.class,  AppConfig.DB_USER);
            set(cls, ds, "setPassword",        String.class,  AppConfig.DB_PASSWORD);

            set(cls, ds, "setInitialSize",   int.class,  POOL_INITIAL_SIZE);
            set(cls, ds, "setMaxTotal",      int.class,  POOL_MAX_ACTIVE);
            set(cls, ds, "setMaxIdle",       int.class,  POOL_MAX_IDLE);
            set(cls, ds, "setMinIdle",       int.class,  POOL_MIN_IDLE);
            set(cls, ds, "setMaxWaitMillis", long.class, (long) POOL_MAX_WAIT_MS);

            // Validation / lifecycle hardening
            set(cls, ds, "setDefaultAutoCommit",             Boolean.class, Boolean.TRUE);
            set(cls, ds, "setDefaultReadOnly",               Boolean.class, Boolean.FALSE);
            set(cls, ds, "setValidationQuery",               String.class,  VALIDATION_QUERY);
            set(cls, ds, "setValidationQueryTimeout",       int.class,     POOL_VALIDATION_TIMEOUT_S);
            set(cls, ds, "setTestOnBorrow",                  boolean.class, true);
            setOptional(cls, ds, "setTestOnCreate",                  boolean.class, true);
            set(cls, ds, "setTestWhileIdle",                 boolean.class, true);
            setOptional(cls, ds, "setFastFailValidation",            boolean.class, true);
            set(cls, ds, "setTimeBetweenEvictionRunsMillis", long.class, (long) POOL_EVICTION_INTERVAL_MS);
            set(cls, ds, "setMinEvictableIdleTimeMillis",    long.class, (long) POOL_MIN_EVICTABLE_IDLE_MS);
            set(cls, ds, "setNumTestsPerEvictionRun",        int.class,  POOL_EVICTION_TESTS_PER_RUN);
            set(cls, ds, "setRollbackOnReturn",              boolean.class, true);
            setOptional(cls, ds, "setLogExpiredConnections",         boolean.class, true);
            setOptional(cls, ds, "setMaxConnLifetimeMillis",         long.class,  POOL_MAX_AGE_MS);

            // Abandoned connection recovery
            set(cls, ds, "setRemoveAbandonedOnBorrow",      boolean.class, true);
            set(cls, ds, "setRemoveAbandonedOnMaintenance", boolean.class, true);
            set(cls, ds, "setRemoveAbandonedTimeout",       int.class,    POOL_ABANDONED_TIMEOUT_S);
            set(cls, ds, "setLogAbandoned",                 boolean.class, true);

            DataSource tempDs = (DataSource) ds;
            // Test connection to trigger internal initialization & validate connectivity
            try (Connection conn = tempDs.getConnection()) {
                LoggerUtil.info(log, "[ConnectionPool] Tomcat DBCP2 Pool validation connection successful");
            }

            dataSource = tempDs;
            poolActive = true;
            LoggerUtil.info(log, "[ConnectionPool] Tomcat DBCP2 Pool OK — max=" + POOL_MAX_ACTIVE);
            return true;
        } catch (Throwable e) {
            LoggerUtil.warn(log, "[ConnectionPool] Tomcat DBCP2 không khả dụng", e);
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
            LoggerUtil.warn(log, "[ConnectionPool] Không set được " + method, e);
        }
    }

    /**
     * Shut down and close the connection pool to prevent classloader/timer memory leaks.
     */
    public static void shutdown() {
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
