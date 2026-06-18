package dao.system;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.LoggerUtil;
import util.ValidationUtil;

/**
 * DaoSqlLogger — tạo proxy JDBC để trace SQL đọc/ghi của toàn bộ DAO.
 *
 * Mục tiêu:
 *   - Không sửa từng DAO một cách thủ công
 *   - Log SQL template, params và số dòng / số bản ghi bị ảnh hưởng
 *   - Giữ log an toàn bằng cách sanitize/mask dữ liệu nhạy cảm
 */
final class DaoSqlLogger {

    private static final Logger LOG = Logger.getLogger("dao.sql");
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final int MAX_SQL_LENGTH = 1000;
    private static final int MAX_PARAM_LENGTH = 160;
    private static final String QUERY_LABEL = "QUERY";
    private static final String UPDATE_LABEL = "UPDATE";
    private static final String BATCH_LABEL = "BATCH";
    private static final String TX_LABEL = "TX";
    private static final String RESULT_LABEL = "RESULT";
    private static final String GENERATED_KEYS_LABEL = "GENERATED_KEYS";
    private static final String[] SENSITIVE_SQL_KEYWORDS = {
        "password", "secret", "token", "refresh", "access_token",
        "provider_response", "signature", "otp", "verification_code",
        "csrf", "cookie", "session", "api_key", "private_key"
    };

    private DaoSqlLogger() {
        /* Utility class — không khởi tạo */
    }

    static Connection wrapConnection(Connection connection) {
        if (connection == null) {
            return null;
        }
        if (Proxy.isProxyClass(connection.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(connection);
            if (handler instanceof ConnectionHandler) {
                return connection;
            }
        }
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionHandler(connection));
    }

    private static PreparedStatement wrapPreparedStatement(PreparedStatement statement, String sqlTemplate) {
        if (statement == null) {
            return null;
        }
        if (Proxy.isProxyClass(statement.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(statement);
            if (handler instanceof StatementHandler) {
                return statement;
            }
        }
        Class<?>[] interfaces = (statement instanceof CallableStatement)
                ? new Class<?>[]{CallableStatement.class}
                : new Class<?>[]{PreparedStatement.class};
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                interfaces,
                new StatementHandler(statement, sqlTemplate));
    }

    private static ResultSet wrapResultSet(ResultSet resultSet, String label, String sql, String caller, long startedAtNanos) {
        if (resultSet == null) {
            return null;
        }
        if (Proxy.isProxyClass(resultSet.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(resultSet);
            if (handler instanceof ResultSetHandler) {
                return resultSet;
            }
        }
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new ResultSetHandler(resultSet, label, sql, caller, startedAtNanos));
    }

    private static Object invokeTarget(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            throw cause != null ? cause : ex;
        }
    }

    private static String caller() {
        return STACK_WALKER.walk(stream -> stream
                .filter(frame -> {
                    String className = frame.getClassName();
                    return !className.startsWith(DaoSqlLogger.class.getName())
                            && !className.startsWith("java.lang.reflect.")
                            && !className.startsWith("jdk.proxy")
                            && !className.startsWith("java.util.logging.");
                })
                .findFirst()
                .map(frame -> simpleClassName(frame.getClassName())
                        + "." + frame.getMethodName()
                        + ":" + frame.getLineNumber())
                .orElse("unknown"));
    }

    private static String simpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    private static boolean isSensitiveSql(String sql) {
        String normalized = sql == null ? "" : sql.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_SQL_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeSql(String sql) {
        if (sql == null) {
            return "(null)";
        }
        String normalized = ValidationUtil.sanitizeForLog(sql)
                .replaceAll("\\s+", " ")
                .trim();
        return truncate(normalized, MAX_SQL_LENGTH);
    }

    private static Map<Integer, Object> snapshotParams(Map<Integer, Object> params) {
        return new LinkedHashMap<>(params);
    }

    private static String formatParams(Map<Integer, Object> params, boolean sensitiveSql) {
        if (params == null || params.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<Integer, Object> entry : new TreeMap<>(params).entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey()).append('=').append(formatValue(entry.getValue(), sensitiveSql));
        }
        sb.append(']');
        return truncate(sb.toString(), MAX_SQL_LENGTH);
    }

    private static String formatBatchParams(List<Map<Integer, Object>> batches, boolean sensitiveSql) {
        if (batches == null || batches.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < batches.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatParams(batches.get(i), sensitiveSql));
            if (sb.length() > MAX_SQL_LENGTH) {
                sb.append(" ...");
                break;
            }
        }
        sb.append(']');
        return truncate(sb.toString(), MAX_SQL_LENGTH);
    }

    private static String formatValue(Object value, boolean sensitiveSql) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof CharSequence || value instanceof Character) {
            if (sensitiveSql) {
                return "***";
            }
            String text = ValidationUtil.sanitizeForLog(String.valueOf(value));
            return "'" + truncate(text, MAX_PARAM_LENGTH) + "'";
        }

        if (value instanceof byte[] bytes) {
            return "[bytes:" + bytes.length + "]";
        }

        if (value.getClass().isArray()) {
            return truncate(String.valueOf(value), MAX_PARAM_LENGTH);
        }

        return truncate(ValidationUtil.sanitizeForLog(String.valueOf(value)), MAX_PARAM_LENGTH);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static void logSqlStart(String label, String sql, Map<Integer, Object> params) {
        if (!LOG.isLoggable(Level.INFO)) {
            return;
        }
        String caller = caller();
        boolean sensitiveSql = isSensitiveSql(sql);
        LoggerUtil.info(LOG,
                "[DAO-%s] caller=%s | sql=%s | params=%s",
                label,
                caller,
                normalizeSql(sql),
                formatParams(params, sensitiveSql));
    }

    private static void logSqlEnd(String label, String sql, String caller, long startedAtNanos, int affectedRows, Map<Integer, Object> params) {
        if (!LOG.isLoggable(Level.INFO)) {
            return;
        }
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos);
        boolean sensitiveSql = isSensitiveSql(sql);
        LoggerUtil.info(LOG,
                "[DAO-%s-END] caller=%s | rows=%d | durationMs=%d | sql=%s | params=%s",
                label,
                caller,
                affectedRows,
                durationMs,
                normalizeSql(sql),
                formatParams(params, sensitiveSql));
    }

    private static void logFailure(String label, String sql, Map<Integer, Object> params, Throwable thrown) {
        String caller = caller();
        boolean sensitiveSql = isSensitiveSql(sql);
        LoggerUtil.error(LOG,
                String.format("[DAO-%s-FAIL] caller=%s | sql=%s | params=%s",
                        label,
                        caller,
                        normalizeSql(sql),
                        formatParams(params, sensitiveSql)),
                thrown);
    }

    private static void logTransaction(String action) {
        if (LOG.isLoggable(Level.INFO)) {
            LoggerUtil.info(LOG, "[DAO-%s] action=%s | caller=%s", TX_LABEL, action, caller());
        }
    }

    private static final class ConnectionHandler implements InvocationHandler {

        private final Connection delegate;

        private ConnectionHandler(Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("prepareStatement".equals(methodName) || "prepareCall".equals(methodName)) {
                String sql = (args != null && args.length > 0 && args[0] instanceof String)
                        ? (String) args[0]
                        : null;
                Object result = invokeTarget(method, delegate, args);
                if (result instanceof PreparedStatement ps) {
                    return wrapPreparedStatement(ps, sql);
                }
                return result;
            }

            Object result = invokeTarget(method, delegate, args);
            if ("setAutoCommit".equals(methodName)
                    && args != null
                    && args.length == 1
                    && args[0] instanceof Boolean
                    && !((Boolean) args[0])) {
                logTransaction("BEGIN");
            } else if ("commit".equals(methodName)) {
                logTransaction("COMMIT");
            } else if ("rollback".equals(methodName)) {
                logTransaction("ROLLBACK");
            }
            return result;
        }
    }

    private static final class StatementHandler implements InvocationHandler {

        private final PreparedStatement delegate;
        private final String sqlTemplate;
        private final Map<Integer, Object> currentParams = new LinkedHashMap<>();
        private final List<Map<Integer, Object>> batchSnapshots = new ArrayList<>();
        private long lastExecutionStartedAt = 0L;
        private String lastExecutionLabel = QUERY_LABEL;
        private boolean closed = false;

        private StatementHandler(PreparedStatement delegate, String sqlTemplate) {
            this.delegate = delegate;
            this.sqlTemplate = sqlTemplate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            if (methodName.startsWith("set")
                    && args != null
                    && args.length >= 1
                    && args[0] instanceof Integer) {
                captureParameter(methodName, args);
                return invokeTarget(method, delegate, args);
            }

            if ("clearParameters".equals(methodName)) {
                currentParams.clear();
                return invokeTarget(method, delegate, args);
            }

            if ("addBatch".equals(methodName)) {
                batchSnapshots.add(snapshotParams(currentParams));
                return invokeTarget(method, delegate, args);
            }

            if ("clearBatch".equals(methodName)) {
                batchSnapshots.clear();
                return invokeTarget(method, delegate, args);
            }

            if ("executeQuery".equals(methodName)) {
                long startedAt = System.nanoTime();
                String sql = currentSql();
                logSqlStart(QUERY_LABEL, sql, currentParams);
                try {
                    ResultSet rs = (ResultSet) invokeTarget(method, delegate, args);
                    lastExecutionStartedAt = startedAt;
                    lastExecutionLabel = QUERY_LABEL;
                    String caller = caller();
                    return wrapResultSet(rs, QUERY_LABEL, sql, caller, startedAt);
                } catch (Throwable thrown) {
                    logFailure(QUERY_LABEL, sql, currentParams, thrown);
                    throw thrown;
                }
            }

            if ("executeUpdate".equals(methodName) || "executeLargeUpdate".equals(methodName)) {
                long startedAt = System.nanoTime();
                String sql = currentSql();
                logSqlStart(UPDATE_LABEL, sql, currentParams);
                try {
                    Object result = invokeTarget(method, delegate, args);
                    lastExecutionStartedAt = startedAt;
                    lastExecutionLabel = UPDATE_LABEL;
                    int affectedRows = result instanceof Number ? ((Number) result).intValue() : 0;
                    logSqlEnd(UPDATE_LABEL, sql, caller(), startedAt, affectedRows, currentParams);
                    return result;
                } catch (Throwable thrown) {
                    logFailure(UPDATE_LABEL, sql, currentParams, thrown);
                    throw thrown;
                }
            }

            if ("executeBatch".equals(methodName)) {
                long startedAt = System.nanoTime();
                String sql = currentSql();
                Map<Integer, Object> paramsForLog = currentParams.isEmpty() ? new LinkedHashMap<>() : snapshotParams(currentParams);
                logSqlStart(BATCH_LABEL, sql, paramsForLog);
                try {
                    int[] counts = (int[]) invokeTarget(method, delegate, args);
                    lastExecutionStartedAt = startedAt;
                    lastExecutionLabel = BATCH_LABEL;
                    LoggerUtil.info(LOG,
                            "[DAO-%s-END] caller=%s | batchSize=%d | durationMs=%d | sql=%s | params=%s",
                            BATCH_LABEL,
                            caller(),
                            batchSnapshots.size(),
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                            normalizeSql(sql),
                            formatBatchParams(batchSnapshots.isEmpty() ? List.of(paramsForLog) : batchSnapshots, isSensitiveSql(sql)));
                    batchSnapshots.clear();
                    return counts;
                } catch (Throwable thrown) {
                    logFailure(BATCH_LABEL, sql, currentParams, thrown);
                    throw thrown;
                }
            }

            if ("execute".equals(methodName)) {
                long startedAt = System.nanoTime();
                String sql = currentSql();
                logSqlStart(QUERY_LABEL, sql, currentParams);
                try {
                    Object result = invokeTarget(method, delegate, args);
                    lastExecutionStartedAt = startedAt;
                    lastExecutionLabel = QUERY_LABEL;
                    LoggerUtil.info(LOG,
                            "[DAO-%s-END] caller=%s | result=%s | durationMs=%d | sql=%s | params=%s",
                            QUERY_LABEL,
                            caller(),
                            result,
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                            normalizeSql(sql),
                            formatParams(currentParams, isSensitiveSql(sql)));
                    return result;
                } catch (Throwable thrown) {
                    logFailure(QUERY_LABEL, sql, currentParams, thrown);
                    throw thrown;
                }
            }

            if ("getResultSet".equals(methodName) || "getGeneratedKeys".equals(methodName)) {
                Object result = invokeTarget(method, delegate, args);
                if (result instanceof ResultSet rs) {
                    String label = "getGeneratedKeys".equals(methodName) ? GENERATED_KEYS_LABEL : lastExecutionLabel;
                    String sql = currentSql();
                    long startedAt = lastExecutionStartedAt != 0L ? lastExecutionStartedAt : System.nanoTime();
                    return wrapResultSet(rs, label, sql, caller(), startedAt);
                }
                return result;
            }

            if ("close".equals(methodName)) {
                try {
                    return invokeTarget(method, delegate, args);
                } finally {
                    currentParams.clear();
                    batchSnapshots.clear();
                    closed = true;
                }
            }

            return invokeTarget(method, delegate, args);
        }

        private void captureParameter(String methodName, Object[] args) {
            Integer index = (Integer) args[0];
            if ("setNull".equals(methodName)) {
                currentParams.put(index, null);
                return;
            }
            if (args.length > 1) {
                currentParams.put(index, args[1]);
            } else {
                currentParams.put(index, null);
            }
        }

        private String currentSql() {
            return sqlTemplate != null ? sqlTemplate : "(unknown sql)";
        }
    }

    private static final class ResultSetHandler implements InvocationHandler {

        private final ResultSet delegate;
        private final String label;
        private final String sql;
        private final String caller;
        private final long startedAtNanos;
        private int rows = 0;
        private boolean closed = false;

        private ResultSetHandler(ResultSet delegate, String label, String sql, String caller, long startedAtNanos) {
            this.delegate = delegate;
            this.label = label;
            this.sql = sql;
            this.caller = caller;
            this.startedAtNanos = startedAtNanos;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("next".equals(methodName)) {
                boolean hasNext = (Boolean) invokeTarget(method, delegate, args);
                if (hasNext) {
                    rows++;
                }
                return hasNext;
            }
            if ("close".equals(methodName)) {
                try {
                    return invokeTarget(method, delegate, args);
                } finally {
                    if (!closed) {
                        closed = true;
                        LoggerUtil.info(LOG,
                                "[DAO-%s-END] caller=%s | rows=%d | durationMs=%d | sql=%s",
                                label,
                                caller,
                                rows,
                                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos),
                                normalizeSql(sql));
                    }
                }
            }
            return invokeTarget(method, delegate, args);
        }
    }
}
