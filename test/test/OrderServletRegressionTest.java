package test;

import config.AppConfig;
import dao.cart.CartDAO;
import dao.catalog.CategoryDAO;
import dao.order.DeliveryDAO;
import dao.order.OrderDAO;
import dao.order.OrderItemDAO;
import dao.shop.PaymentDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.auth.UserDAO;
import model.entity.cart.Cart;
import model.entity.cart.CartItem;
import model.entity.catalog.Category;
import model.entity.order.Delivery;
import model.entity.order.Order;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.auth.User;
import servlet.customer.order.OrderServlet;
import service.order.DeliveryService;
import service.shop.PaymentService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OrderServletRegressionTest {

    private static final BigDecimal PRICE = new BigDecimal("120000");

    private final OrderServletHarness servlet = new OrderServletHarness();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final PaymentService paymentService = new PaymentService();
    private final DeliveryService deliveryService = new DeliveryService();

    private MockHttpEnvironment env;

    private int ownerId = -1;
    private int customerId = -1;
    private int categoryId = -1;
    private int productId = -1;
    private int variantId = -1;
    private int ckOrderId = -1;
    private int dispatchedOrderId = -1;
    private String customerPhone;

    @Before
    public void setUp() throws Exception {
        env = new MockHttpEnvironment("/orders");

        ownerId = createUser("Order Owner", "order_owner_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_SHOP_OWNER, buildUniquePhone(1));
        customerPhone = buildUniquePhone(2);
        customerId = createUser("Order Customer", "order_customer_" + System.currentTimeMillis() + "@test.com",
                AppConfig.ROLE_CUSTOMER, customerPhone);

        categoryId = createCategory("Order Servlet " + System.currentTimeMillis());
        productId = createProduct(ownerId, "Order Servlet Product");
        variantId = createVariant(productId, "ORDER-1", PRICE);

        ckOrderId = createOrder(customerId, ownerId, AppConfig.ORDER_PENDING_PAYMENT, AppConfig.PAYMENT_CK, PRICE);
        addOrderItem(ckOrderId);
        paymentService.initPayment(ckOrderId, "SEPAY");

        dispatchedOrderId = createOrder(customerId, ownerId, AppConfig.ORDER_DISPATCHED, AppConfig.PAYMENT_COD, PRICE);
        addOrderItem(dispatchedOrderId);
        deliveryService.assignShipper(dispatchedOrderId, 0, LocalDateTime.now().plusHours(2));

        env.setCurrentUser(buildUser(customerId, AppConfig.ROLE_CUSTOMER));
    }

    @After
    public void tearDown() {
        try {
            if (ckOrderId > 0) {
                hardDeleteOrder(ckOrderId);
            }
            if (dispatchedOrderId > 0) {
                hardDeleteOrder(dispatchedOrderId);
            }
            hardDeleteCustomerCart();
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
    public void detailViewLoadsOrderItemsPaymentAndDeliveryData() throws Exception {
        env.clearRequestState();
        env.putParam("action", "detail");
        env.putParam("orderId", String.valueOf(ckOrderId));

        servlet.doGetPublic(env.request, env.response);

        assertEquals("/WEB-INF/jsp/customer/order-detail.jsp", env.forwardedPath);
        assertNotNull(env.requestAttributes.get("order"));
        assertNotNull(env.requestAttributes.get("orderItems"));
        assertNotNull(env.requestAttributes.get("paymentTx"));
        assertNull(env.redirectLocation);
    }

    @Test
    public void listViewLoadsDeliveryAndPaymentMapsFromViewService() throws Exception {
        env.clearRequestState();
        env.setCurrentUser(buildUser(ownerId, AppConfig.ROLE_SHOP_OWNER));

        servlet.doGetPublic(env.request, env.response);

        assertEquals("/WEB-INF/jsp/customer/orders.jsp", env.forwardedPath);
        Map<?, ?> paymentTxMap = (Map<?, ?>) env.requestAttributes.get("paymentTxMap");
        Map<?, ?> deliveryMap = (Map<?, ?>) env.requestAttributes.get("deliveryMap");
        assertNotNull(paymentTxMap);
        assertNotNull(deliveryMap);
        assertTrue(paymentTxMap.containsKey(ckOrderId));
        assertTrue(deliveryMap.containsKey(dispatchedOrderId));
    }

    @Test
    public void reorderActionAddsItemsBackToCart() throws Exception {
        env.clearRequestState();
        env.putParam("action", "reorder");
        env.putParam("orderId", String.valueOf(ckOrderId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/cart", env.redirectLocation);
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        assertFalse(carts.isEmpty());
        List<CartItem> items = cartDAO.findItems(carts.get(0).getCartId());
        assertEquals(1, items.size());
        assertEquals(variantId, items.get(0).getVariantId());
    }

    @Test
    public void reportNotReceivedUpdatesReceivedStatus() throws Exception {
        env.clearRequestState();
        env.putParam("action", "reportNotReceived");
        env.putParam("orderId", String.valueOf(dispatchedOrderId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/profile/order-detail?orderId=" + dispatchedOrderId, env.redirectLocation);
        assertEquals("warning", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        Order updated = orderDAO.findById(dispatchedOrderId).get(0);
        assertEquals("NOT_RECEIVED", updated.getReceivedStatus());
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
        category.setDisplayOrder(55);
        category.setIsActive(true);
        return categoryDAO.save(category);
    }

    private int createProduct(int ownerId, String name) throws SQLException {
        Product product = new Product();
        product.setOwnerId(ownerId);
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription("Order servlet regression product");
        product.setOriginCountry("Vietnam");
        product.setOriginRegion("Da Lat");
        product.setHarvestDate(LocalDate.now().plusDays(2));
        product.setShelfLifeDays(10);
        product.setStatus("ACTIVE");
        product.setApprovalStatus("APPROVED");
        product.setIsOrganic(false);
        product.setIsImported(false);
        return productDAO.save(product);
    }

    private int createVariant(int productId, String skuSuffix, BigDecimal price) throws SQLException {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku("ORD-" + skuSuffix + "-" + System.currentTimeMillis());
        variant.setVariantLabel("1kg");
        variant.setPrice(price);
        variant.setStockQuantity(20);
        variant.setIsActive(true);
        return variantDAO.save(variant);
    }

    private int createOrder(int customerId, int ownerId, String status, String paymentMethod, BigDecimal amount)
            throws SQLException {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOwnerId(ownerId);
        order.setOrderType(AppConfig.ORDER_TYPE_CHILD);
        order.setDeliveryAddress("123 Test Address");
        order.setDeliveryTimeSlot("08:00-12:00");
        order.setStatus(status);
        order.setTotalAmount(amount);
        order.setDeliveryFee(new BigDecimal("15000"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setSystemDiscountAmount(BigDecimal.ZERO);
        order.setShopDiscountAmount(BigDecimal.ZERO);
        order.setPlatformFee(BigDecimal.ZERO);
        order.setFinalAmount(amount.add(new BigDecimal("15000")));
        order.setPaymentMethod(paymentMethod);
        order.setRefundStatus("NONE");
        return orderDAO.save(order);
    }

    private void addOrderItem(int orderId) throws SQLException {
        CartItem item = new CartItem();
        item.setVariantId(variantId);
        item.setProductId(productId);
        item.setProductName("Order Servlet Product");
        item.setVariantLabel("1kg");
        item.setPrice(PRICE);
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

    private void hardDeleteCustomerCart() throws SQLException {
        List<Cart> carts = cartDAO.findByCustomer(customerId);
        for (Cart cart : carts) {
            try (Connection conn = cartDAO.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                    ps.setInt(1, cart.getCartId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?")) {
                    ps.setInt(1, cart.getCartId());
                    ps.executeUpdate();
                }
            }
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

    private final class OrderServletHarness extends OrderServlet {
        void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doGet(req, resp);
        }

        void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            super.doPost(req, resp);
        }
    }

    private static final class MockHttpEnvironment {
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, Object> requestAttributes = new HashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();

        private String redirectLocation;
        private Integer errorStatus;
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

        private void setCurrentUser(User user) {
            sessionAttributes.put(AppConfig.SESSION_USER, user);
        }

        private void putParam(String name, String value) {
            params.put(name, value);
        }

        private void clearRequestState() {
            params.clear();
            requestAttributes.clear();
            redirectLocation = null;
            errorStatus = null;
            forwardedPath = null;
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
                    handler);
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
