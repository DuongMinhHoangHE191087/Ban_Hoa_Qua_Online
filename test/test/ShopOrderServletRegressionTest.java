package test;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.order.DeliveryDAO;
import dao.order.DeliveryTripDAO;
import dao.order.OrderDAO;
import dao.order.OrderItemDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.catalog.ProductVariantDAO;
import dao.auth.UserDAO;
import model.entity.cart.CartItem;
import model.entity.catalog.Category;
import model.entity.order.Delivery;
import model.entity.order.DeliveryTrip;
import model.entity.order.Order;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.auth.User;
import servlet.shop.order.ShopOrderServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ShopOrderServletRegressionTest {

    private final ShopOrderServletHarness servlet = new ShopOrderServletHarness();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final DeliveryTripDAO deliveryTripDAO = new DeliveryTripDAO();

    private MockHttpEnvironment env;

    private int ownerId = -1;
    private int customerId = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;
    private int orderId = -1;

    @Before
    public void setUp() throws Exception {
        env = new MockHttpEnvironment("/shop/orders");

        ownerId = createUser("Shop Order Owner", "shop_order_owner_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_SHOP_OWNER, buildUniquePhone(1));
        customerId = createUser("Shop Order Customer", "shop_order_customer_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_CUSTOMER, buildUniquePhone(2));
        categoryId = createCategory("Shop Order " + System.currentTimeMillis());
        productId = createProduct(ownerId, "Shop Dispatch Product");
        variantId = createVariant(productId, "SHOP-1", new BigDecimal("90000"));
        orderId = createOrder(customerId, ownerId);
        addOrderItem(orderId);

        env.setCurrentUser(buildUser(ownerId, AppConfig.ROLE_SHOP_OWNER));
    }

    @After
    public void tearDown() {
        try {
            if (orderId > 0) {
                hardDeleteOrder(orderId);
            }
            if (productId > 0) {
                hardDeleteProduct(productId);
            }
            if (categoryId > 0) {
                categoryDAO.delete(categoryId);
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
    public void dispatchActionUpdatesOrderAndCreatesDeliveryTrip() throws Exception {
        env.clearRequestState();
        env.putParam("action", "dispatch");
        env.putParam("orderId", String.valueOf(orderId));
        env.putParam("estimatedDeliveryTime", "2030-01-01T09:30:00");

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/shop/orders", env.redirectLocation);
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));

        Order updated = orderDAO.findById(orderId).get(0);
        assertEquals(AppConfig.ORDER_DISPATCHED, updated.getStatus());

        Delivery delivery = deliveryDAO.findByOrderId(orderId);
        assertNotNull(delivery);
        assertNotNull(delivery.getDeliveryTripId());

        DeliveryTrip trip = deliveryTripDAO.findById(delivery.getDeliveryTripId());
        assertNotNull(trip);
        assertEquals(orderId, trip.getParentOrderId());
        assertEquals(AppConfig.DELIVERY_TRIP_PLANNED, trip.getStatus());
    }

    @Test
    public void forbidNonShopOwnerOnPost() throws Exception {
        env.clearRequestState();
        env.setCurrentUser(buildUser(customerId, AppConfig.ROLE_CUSTOMER));
        env.putParam("action", "dispatch");
        env.putParam("orderId", String.valueOf(orderId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_FORBIDDEN), env.errorStatus);
    }

    private int createUser(String fullName, String email, String role, String phone) throws SQLException {
        return userDAO.saveNewCustomer(fullName, email, "hashed_pwd", phone, role, "ACTIVE", true);
    }

    private String buildUniquePhone(int salt) {
        long raw = Math.abs((System.currentTimeMillis() + salt) % 100000000L);
        return String.format("09%08d", raw);
    }

    private int createCategory(String name) throws SQLException {
        Category category = new Category();
        category.setName(name);
        category.setSlug(name.toLowerCase().replace(' ', '-'));
        category.setDisplayOrder(88);
        category.setIsActive(true);
        return categoryDAO.save(category);
    }

    private int createProduct(int ownerId, String name) throws SQLException {
        Product product = new Product();
        product.setOwnerId(ownerId);
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription("Shop order regression product");
        product.setOriginCountry("Vietnam");
        product.setOriginRegion("Can Tho");
        product.setHarvestDate(LocalDate.now().plusDays(2));
        product.setShelfLifeDays(15);
        product.setStatus("ACTIVE");
        product.setApprovalStatus("APPROVED");
        product.setIsOrganic(false);
        product.setIsImported(false);
        return productDAO.save(product);
    }

    private int createVariant(int productId, String skuSuffix, BigDecimal price) throws SQLException {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku("SHOP-" + skuSuffix + "-" + System.currentTimeMillis());
        variant.setVariantLabel("500g");
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
        order.setDeliveryAddress("456 Shop Address");
        order.setDeliveryTimeSlot("12:00-16:00");
        order.setStatus(AppConfig.ORDER_CONFIRMED);
        order.setTotalAmount(new BigDecimal("90000"));
        order.setDeliveryFee(new BigDecimal("15000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setSystemDiscountAmount(BigDecimal.ZERO);
        order.setShopDiscountAmount(BigDecimal.ZERO);
        order.setPlatformFee(BigDecimal.ZERO);
        order.setFinalAmount(new BigDecimal("105000"));
        order.setPaymentMethod(AppConfig.PAYMENT_COD);
        order.setRefundStatus("NONE");
        return orderDAO.save(order);
    }

    private void addOrderItem(int orderId) throws SQLException {
        CartItem item = new CartItem();
        item.setVariantId(variantId);
        item.setProductId(productId);
        item.setProductName("Shop Dispatch Product");
        item.setVariantLabel("500g");
        item.setPrice(new BigDecimal("90000"));
        item.setQuantity(1);
        item.setPackagingPriceAdd(BigDecimal.ZERO);

        Map<Integer, ProductVariant> variantMap = new HashMap<>();
        variantMap.put(variantId, variantDAO.findById(variantId));

        try (Connection conn = orderDAO.openConnection()) {
            orderItemDAO.saveBatch(conn, orderId, java.util.Collections.singletonList(item), variantMap);
        }
    }

    private User buildUser(int userId, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setStatus("ACTIVE");
        return user;
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection()) {
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

    private final class ShopOrderServletHarness extends ShopOrderServlet {
        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doPost(req, resp);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new HashMap<>();
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
                    case "getRequestDispatcher":
                        return createDispatcher((String) args[0]);
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    handler);
        }

        private RequestDispatcher createDispatcher(String path) {
            InvocationHandler handler = (proxy, method, args) -> defaultValue(method.getReturnType());
            return (RequestDispatcher) Proxy.newProxyInstance(
                    RequestDispatcher.class.getClassLoader(),
                    new Class<?>[]{RequestDispatcher.class},
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
