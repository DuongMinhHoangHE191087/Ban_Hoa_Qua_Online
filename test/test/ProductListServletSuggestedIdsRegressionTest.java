package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.system.ConnectionPool;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.dto.common.PagedResultDTO;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import servlet.guest.product.ProductListServlet;

import javax.sql.DataSource;
import java.io.IOException;
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
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression coverage for the suggestedIds branch in ProductListServlet.
 * The goal is to keep request SQL flat even as the suggested product list grows.
 */
public class ProductListServletSuggestedIdsRegressionTest {

    private static final int EXPECTED_REQUEST_QUERY_COUNT = 5;

    private final ProductListServletHarness servlet = new ProductListServletHarness();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();

    private MockHttpEnvironment env;
    private int ownerId = -1;
    private int categoryId = -1;
    private final List<Integer> createdProductIds = new ArrayList<>();

    private DataSource originalDataSource;
    private boolean countingDataSourceInstalled;
    private AtomicInteger queryCount;

    @Before
    public void setUp() throws Exception {
        env = new MockHttpEnvironment("/products");

        ownerId = createUser("Suggested IDs Owner", "suggested_owner_" + System.currentTimeMillis() + "@test.com");
        
        // Tạo shop profile cho ownerId để sản phẩm hiển thị trong catalog
        dao.shop.ShopProfileDAO shopProfileDAO = new dao.shop.ShopProfileDAO();
        model.entity.shop.ShopProfile profile = new model.entity.shop.ShopProfile();
        profile.setUserId(ownerId);
        profile.setShopName("Suggested Shop Test");
        profile.setShopDescription("Suggested test shop");
        profile.setApprovalStatus("APPROVED");
        profile.setDeliveryAddress("123 Suggested Street");
        profile.setRating(java.math.BigDecimal.ZERO);
        profile.setBusinessEmail("sug_biz_" + ownerId + "_" + System.currentTimeMillis() + "@company.com");
        shopProfileDAO.save(profile);

        categoryId = createCategory("Suggested IDs " + System.currentTimeMillis());

        for (int i = 0; i < 4; i++) {
            BigDecimal price = new BigDecimal("45000").add(BigDecimal.valueOf(i * 2500L));
            int productId = createProduct(ownerId, categoryId, "Suggested Product " + (i + 1));
            createVariant(productId, i + 1, price);
            createdProductIds.add(productId);
        }
    }

    @After
    public void tearDown() {
        restoreDataSource();

        try {
            for (Integer productId : createdProductIds) {
                deleteProductHard(productId);
            }
            if (categoryId > 0) {
                categoryDAO.delete(categoryId);
            }
            if (ownerId > 0) {
                new dao.shop.ShopProfileDAO().deleteByUserId(ownerId);
                userDAO.deleteUser(ownerId);
            }
        } catch (SQLException ignored) {
        }
    }

    @Test
    public void suggestedIdsBranchKeepsQueryCountFlatAndPreservesOrder() throws Exception {
        List<Integer> requestedOrder = new ArrayList<>(createdProductIds);
        Collections.reverse(requestedOrder);

        installCountingDataSource();
        AtomicInteger requestCounter = queryCount;
        queryCount.set(0);
        try {
            env.clearRequestState();
            env.putParam("suggestedIds", joinIds(requestedOrder));

            servlet.doGetPublic(env.request, env.response);
        } finally {
            restoreDataSource();
        }

        assertEquals("/WEB-INF/jsp/guest/product-list.jsp", env.forwardedPath);
        assertNotNull(env.requestAttributes.get("pagedResult"));
        assertEquals(EXPECTED_REQUEST_QUERY_COUNT, requestCounter.get());

        PagedResultDTO pagedResult = (PagedResultDTO) env.requestAttributes.get("pagedResult");
        List<?> items = pagedResult.getItems();
        assertEquals(requestedOrder.size(), items.size());

        for (int i = 0; i < requestedOrder.size(); i++) {
            Map<?, ?> card = (Map<?, ?>) items.get(i);
            assertEquals(requestedOrder.get(i).intValue(), ((Number) card.get("productId")).intValue());
        }
    }

    private int createUser(String fullName, String email) throws SQLException {
        return userDAO.saveNewCustomer(
                fullName,
                email,
                "hashed_pwd",
                buildUniquePhone(1),
                AppConfig.ROLE_SHOP_OWNER,
                "ACTIVE",
                true
        );
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
        product.setDescription("Regression product for suggestedIds");
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

    private int createVariant(int productId, int index, BigDecimal price) throws SQLException {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku("SG-" + index + "-" + System.currentTimeMillis());
        variant.setVariantLabel("1kg");
        variant.setPrice(price);
        variant.setStockQuantity(10);
        variant.setIsActive(true);
        return variantDAO.save(variant);
    }

    private void deleteProductHard(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM product_variants WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
        }
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

    private String buildUniquePhone(int salt) {
        long raw = Math.abs((System.currentTimeMillis() + salt) % 100000000L);
        return String.format("09%08d", raw);
    }

    private String joinIds(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    private Connection wrapConnection(Connection realConnection, AtomicInteger counter) {
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

    private Statement wrapStatement(Statement realStatement, AtomicInteger counter) {
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

    private Object invokeReal(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private final class CountingDataSource implements DataSource {
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

    private final class ProductListServletHarness extends ProductListServlet {
        void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doGet(req, resp);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new LinkedHashMap<>();
        private final Map<String, Object> requestAttributes = new HashMap<>();

        private String forwardedPath;
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

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private void clearRequestState() {
            params.clear();
            requestAttributes.clear();
            forwardedPath = null;
        }

        private HttpSession createSessionProxy() {
            InvocationHandler handler = (proxy, method, args) -> defaultValue(method.getReturnType());
            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    handler
            );
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
                    case "getAttribute":
                        return requestAttributes.get(args[0]);
                    case "setAttribute":
                        requestAttributes.put((String) args[0], args[1]);
                        return null;
                    case "getRequestDispatcher":
                        return createDispatcher((String) args[0]);
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler
            );
        }

        private RequestDispatcher createDispatcher(String path) {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("forward".equals(method.getName())) {
                    forwardedPath = path;
                    return null;
                }
                return defaultValue(method.getReturnType());
            };
            return (RequestDispatcher) Proxy.newProxyInstance(
                    RequestDispatcher.class.getClassLoader(),
                    new Class<?>[]{RequestDispatcher.class},
                    handler
            );
        }

        private HttpServletResponse createResponseProxy() {
            InvocationHandler handler = (proxy, method, args) -> defaultValue(method.getReturnType());
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler
            );
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
