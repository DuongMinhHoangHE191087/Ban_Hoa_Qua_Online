package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.order.OrderDAO;
import dao.order.OrderItemDAO;
import dao.system.ConnectionPool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.cart.CartItem;
import model.entity.order.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import servlet.admin.order.AdminOrderServlet;
import servlet.shop.product.ProductEditServlet;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdminOrderAndProductEditRegressionTest {

    private static final int EXPECTED_ADMIN_CANCEL_QUERY_COUNT = 7;
    private static final int EXPECTED_PRODUCT_EDIT_QUERY_COUNT = 5;

    private static final BigDecimal PRODUCT_PRICE = new BigDecimal("120000");

    private final AdminOrderServletHarness adminServlet = new AdminOrderServletHarness();
    private final ProductEditServletHarness productEditServlet = new ProductEditServletHarness();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private MockHttpEnvironment env;

    private int adminId = -1;
    private int ownerId = -1;
    private int customerId = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;
    private int orderId = -1;

    private DataSource originalDataSource;
    private boolean countingDataSourceInstalled;
    private AtomicInteger queryCount;

    @Before
    public void setUp() throws Exception {
        env = new MockHttpEnvironment("/test");

        adminId = createUser("Admin Lookup", "admin_lookup_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_ADMIN, buildUniquePhone(1));
        ownerId = createUser("Shop Owner", "shop_owner_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_SHOP_OWNER, buildUniquePhone(2));
        customerId = createUser("Customer", "customer_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_CUSTOMER, buildUniquePhone(3));

        categoryId = createCategory("Edit Regression " + System.currentTimeMillis());
        productId = createProduct(ownerId, categoryId, "Edit Regression Product");
        variantId = createVariant(productId, "EDIT-1", PRODUCT_PRICE);
        orderId = createOrder(customerId, ownerId);
        addOrderItem(orderId);
    }

    @After
    public void tearDown() {
        restoreDataSource();

        try {
            if (variantId > 0) {
                deleteInventoryLogsByVariant(variantId);
            }
            if (orderId > 0) {
                hardDeleteOrder(orderId);
            }
            if (productId > 0) {
                hardDeleteProduct(productId);
            }
            if (categoryId > 0) {
                categoryDAO.delete(categoryId);
            }
            if (adminId > 0) {
                userDAO.deleteUser(adminId);
            }
            if (ownerId > 0) {
                userDAO.deleteUser(ownerId);
            }
            if (customerId > 0) {
                userDAO.deleteUser(customerId);
            }
        } catch (SQLException ignored) {
        }
    }

    @Test
    public void adminCancelBranchKeepsQueryCountFlat() throws Exception {
        env.clearRequestState();
        env.setCurrentUser(buildUser(adminId, AppConfig.ROLE_ADMIN));
        env.putParam("action", "cancelOrder");
        env.putParam("orderId", String.valueOf(orderId));
        env.putParam("reason", "Admin review");

        int requestQueries = measureQueries(() -> adminServlet.doPostPublic(env.request, env.response));

        assertEquals("/ctx/admin/orders", env.redirectLocation);
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertEquals(EXPECTED_ADMIN_CANCEL_QUERY_COUNT, requestQueries);

        Order updated = orderDAO.findOneById(orderId);
        assertNotNull(updated);
        assertEquals(AppConfig.ORDER_CANCELLED, updated.getStatus());
    }

    @Test
    public void productEditLoadKeepsQueryCountFlat() throws Exception {
        env.clearRequestState();
        env.setCurrentUser(buildUser(ownerId, AppConfig.ROLE_SHOP_OWNER));
        env.putParam("id", String.valueOf(productId));

        int requestQueries = measureQueries(() -> productEditServlet.doGetPublic(env.request, env.response));

        assertEquals("/ctx/shop/products", env.redirectLocation);
        assertEquals(EXPECTED_PRODUCT_EDIT_QUERY_COUNT, requestQueries);
    }

    private int measureQueries(ThrowingRunnable action) throws Exception {
        installCountingDataSource();
        AtomicInteger requestCounter = queryCount;
        queryCount.set(0);
        try {
            action.run();
        } finally {
            restoreDataSource();
        }
        return requestCounter.get();
    }

    private void installCountingDataSource() throws Exception {
        Field field = ConnectionPool.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        originalDataSource = (DataSource) field.get(null);
        queryCount = new AtomicInteger();
        field.set(null, new CountingDataSource(queryCount));
        countingDataSourceInstalled = true;
    }

    private void restoreDataSource() {
        if (!countingDataSourceInstalled) {
            return;
        }
        try {
            Field field = ConnectionPool.class.getDeclaredField("dataSource");
            field.setAccessible(true);
            field.set(null, originalDataSource);
        } catch (Exception ignored) {
        } finally {
            countingDataSourceInstalled = false;
            originalDataSource = null;
            queryCount = null;
        }
    }

    private int createUser(String fullName, String email, String role, String phone) throws SQLException {
        return userDAO.saveNewCustomer(fullName, email, "hashed_pwd", phone, role, "ACTIVE", true);
    }

    private int createCategory(String name) throws SQLException {
        Category category = new Category();
        category.setName(name);
        category.setSlug(name.toLowerCase().replace(' ', '-') + "-" + System.currentTimeMillis());
        category.setDisplayOrder(1);
        category.setIsActive(true);
        return categoryDAO.save(category);
    }

    private int createProduct(int ownerId, int categoryId, String name) throws SQLException {
        Product product = new Product();
        product.setOwnerId(ownerId);
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription("Regression product for edit screen");
        product.setOriginCountry("Vietnam");
        product.setOriginRegion("Da Lat");
        product.setHarvestDate(LocalDate.now());
        product.setShelfLifeDays(30);
        product.setStorageInstruction("Keep cool");
        product.setStatus("ACTIVE");
        product.setViewCount(0);
        product.setRating(BigDecimal.ZERO);
        product.setSoldQuantity(0);
        product.setIsOrganic(false);
        product.setIsImported(false);
        product.setApprovalStatus("APPROVED");
        return productDAO.save(product);
    }

    private int createVariant(int productId, String skuSuffix, BigDecimal price) throws SQLException {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku("EDIT-" + skuSuffix + "-" + System.currentTimeMillis());
        variant.setVariantLabel("1kg");
        variant.setPrice(price);
        variant.setStockQuantity(10);
        variant.setIsActive(true);
        return variantDAO.save(variant);
    }

    private int createOrder(int customerId, int ownerId) throws SQLException {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOwnerId(ownerId);
        order.setOrderType(AppConfig.ORDER_TYPE_CHILD);
        order.setDeliveryAddress("123 Test Address");
        order.setDeliveryTimeSlot("08:00-12:00");
        order.setStatus(AppConfig.ORDER_CONFIRMED);
        order.setTotalAmount(PRODUCT_PRICE);
        order.setDeliveryFee(new BigDecimal("15000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setSystemDiscountAmount(BigDecimal.ZERO);
        order.setShopDiscountAmount(BigDecimal.ZERO);
        order.setPlatformFee(BigDecimal.ZERO);
        order.setFinalAmount(PRODUCT_PRICE.add(new BigDecimal("15000")));
        order.setPaymentMethod(AppConfig.PAYMENT_COD);
        order.setRefundStatus("NONE");
        return orderDAO.save(order);
    }

    private void addOrderItem(int orderId) throws SQLException {
        CartItem item = new CartItem();
        item.setVariantId(variantId);
        item.setProductId(productId);
        item.setProductName("Edit Regression Product");
        item.setVariantLabel("1kg");
        item.setPrice(PRODUCT_PRICE);
        item.setQuantity(1);
        item.setPackagingPriceAdd(BigDecimal.ZERO);

        Map<Integer, ProductVariant> variantMap = new HashMap<>();
        variantMap.put(variantId, variantDAO.findById(variantId));

        try (Connection conn = orderDAO.openConnection()) {
            orderItemDAO.saveBatch(conn, orderId, java.util.Collections.singletonList(item), variantMap);
        }
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM payment_transactions WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM deliveries WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM delivery_trips WHERE parent_order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET status = 'DELETED', updated_at = GETDATE() WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
        }
    }

    private void deleteInventoryLogsByVariant(int variantId) throws SQLException {
        try (Connection conn = orderDAO.openConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_logs WHERE variant_id = ?")) {
                ps.setInt(1, variantId);
                ps.executeUpdate();
            }
        }
    }

    private User buildUser(int userId, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setStatus("ACTIVE");
        return user;
    }

    private String buildUniquePhone(int salt) {
        long raw = Math.abs((System.currentTimeMillis() + salt) % 100000000L);
        return String.format("09%08d", raw);
    }

    private final class AdminOrderServletHarness extends AdminOrderServlet {
        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doPost(req, resp);
        }
    }

    private final class ProductEditServletHarness extends ProductEditServlet {
        void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doGet(req, resp);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class CountingDataSource implements DataSource {
        private final AtomicInteger counter;

        private CountingDataSource(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return wrapConnection(DriverManager.getConnection(AppConfig.DB_JDBC_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD), counter);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return wrapConnection(DriverManager.getConnection(AppConfig.DB_JDBC_URL, username, password), counter);
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("Parent logger not supported in test datasource");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("Not a wrapper for " + iface.getName());
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return iface.isInstance(this);
        }
    }

    private static Connection wrapConnection(Connection realConnection, AtomicInteger counter) {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if (name.startsWith("prepareStatement") || name.startsWith("prepareCall") || "createStatement".equals(name)) {
                Object statement = invokeReal(method, realConnection, args);
                return wrapStatement((Statement) statement, counter);
            }
            return invokeReal(method, realConnection, args);
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }

    private static Statement wrapStatement(Statement realStatement, AtomicInteger counter) {
        Class<?>[] interfaces = realStatement instanceof PreparedStatement
                ? new Class<?>[]{PreparedStatement.class}
                : new Class<?>[]{Statement.class};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if (name.startsWith("execute")) {
                counter.incrementAndGet();
            }
            return invokeReal(method, realStatement, args);
        };
        return (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                interfaces,
                handler
        );
    }

    private static Object invokeReal(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new LinkedHashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();

        private String redirectLocation;
        private Integer errorStatus;
        private final String servletPath;

        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private MockHttpEnvironment(String servletPath) {
            this.servletPath = servletPath;
            this.session = createSessionProxy();
            this.request = createRequestProxy();
            this.response = createResponseProxy();
        }

        private void setCurrentUser(User user) {
            sessionAttributes.put(AppConfig.SESSION_USER, user);
        }

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private void clearRequestState() {
            params.clear();
            redirectLocation = null;
            errorStatus = null;
        }

        private HttpSession createSessionProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAttribute":
                        return sessionAttributes.get(args[0]);
                    case "setAttribute":
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    case "removeAttribute":
                        sessionAttributes.remove(args[0]);
                        return null;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    handler);
        }

        private HttpServletRequest createRequestProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getParameter":
                        return params.get(args[0]);
                    case "getSession":
                        return session;
                    case "getContextPath":
                        return "/ctx";
                    case "getServletPath":
                        return servletPath;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler);
        }

        private HttpServletResponse createResponseProxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "sendRedirect":
                        redirectLocation = (String) args[0];
                        return null;
                    case "sendError":
                        errorStatus = (Integer) args[0];
                        return null;
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler);
        }

        private Object defaultValue(Class<?> type) {
            if (type == null || !type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == char.class) return '\0';
            return null;
        }
    }
}
