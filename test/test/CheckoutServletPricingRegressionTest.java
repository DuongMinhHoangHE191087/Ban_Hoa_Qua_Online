package test;

import config.AppConfig;
import dao.cart.CartDAO;
import dao.catalog.CategoryDAO;
import dao.order.DeliveryDAO;
import dao.order.DeliveryTripDAO;
import dao.order.OrderDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.system.SystemConfigDAO;
import dao.auth.UserDAO;
import model.dto.product.CartSummaryDTO;
import model.entity.order.Delivery;
import model.entity.order.DeliveryTrip;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.auth.User;
import model.entity.Promotion;
import servlet.customer.cart.CheckoutServlet;
import service.order.DeliveryService;
import service.cart.CartService;
import service.shop.PaymentService;
import service.shop.PromotionService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Regression coverage for checkout pricing and multi-shop isolation.
 */
public class CheckoutServletPricingRegressionTest {

    private static final BigDecimal SHOP_ITEM_PRICE = new BigDecimal("100000");
    private static final String CSRF_TOKEN = "csrf-checkout-regression";

    private final CheckoutServletHarness servlet = new CheckoutServletHarness();
    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO variantDAO = new ProductVariantDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final DeliveryTripDAO deliveryTripDAO = new DeliveryTripDAO();
    private final DeliveryService deliveryService = new DeliveryService();
    private final CartService cartService = new CartService();
    private final PaymentService paymentService = new PaymentService();
    private final PromotionService promotionService = new PromotionService();

    private MockHttpEnvironment env;

    private int ownerAId = -1;
    private int ownerBId = -1;
    private int customerId = -1;
    private int categoryId = -1;
    private int productAId = -1;
    private int productBId = -1;
    private int variantAId = -1;
    private int variantBId = -1;
    private int cartId = -1;
    private int createdOrderId = -1;
    private String customerPhone;
    private String shopCouponCode;

    @Before
    public void setUp() throws SQLException {
        env = new MockHttpEnvironment();
        ownerAId = createUser("Checkout Owner A", "checkout_owner_a_" + System.currentTimeMillis() + "@test.com", "SHOP_OWNER", buildUniquePhone(1));
        ownerBId = createUser("Checkout Owner B", "checkout_owner_b_" + System.currentTimeMillis() + "@test.com", "SHOP_OWNER", buildUniquePhone(2));

        createShopProfile(ownerAId, "Checkout Shop A");
        createShopProfile(ownerBId, "Checkout Shop B");

        customerPhone = buildUniquePhone(3);
        customerId = createUser("Checkout Customer", "checkout_customer_" + System.currentTimeMillis() + "@test.com", "CUSTOMER", customerPhone);
        env.setCurrentUser(buildCustomer(customerId));
        env.sessionAttributes.put("_csrfToken", CSRF_TOKEN);

        categoryId = createCategory("Checkout Regression " + System.currentTimeMillis());
        productAId = createProduct(ownerAId, "Checkout Product A");
        productBId = createProduct(ownerBId, "Checkout Product B");
        variantAId = createVariant(productAId, "A-1", SHOP_ITEM_PRICE);
        variantBId = createVariant(productBId, "B-1", new BigDecimal("80000"));

        cartId = cartDAO.createForCustomer(customerId);
        assertTrue(cartId > 0);
        cartDAO.addItem(cartId, variantAId, 1);
        cartDAO.addItem(cartId, variantBId, 1);

        shopCouponCode = "SHOP10-" + System.currentTimeMillis();
        Promotion shopPromo = new Promotion();
        shopPromo.setCode(shopCouponCode);
        shopPromo.setDiscountType("PERCENT");
        shopPromo.setDiscountScope("SHOP");
        shopPromo.setDiscountMax(new BigDecimal("50000"));
        shopPromo.setDiscountValue(new BigDecimal("10"));
        shopPromo.setMinOrderValue(new BigDecimal("50000"));
        shopPromo.setScope("ORDER");
        shopPromo.setProductId(null);
        shopPromo.setMaxUses(1000);
        shopPromo.setUsedCount(0);
        shopPromo.setCanStack(true);
        shopPromo.setValidFrom(LocalDateTime.now().minusDays(1));
        shopPromo.setValidUntil(LocalDateTime.now().plusDays(30));
        shopPromo.setCreatedBy(ownerAId);
        shopPromo.setIsActive(true);
        promotionService.createShopPromotion(shopPromo, ownerAId);
    }

    @After
    public void tearDown() {
        try {
            if (createdOrderId > 0) {
                hardDeleteOrder(createdOrderId);
            }
            if (cartId > 0) {
                hardDeleteCart(cartId);
            }
            if (productAId > 0) {
                hardDeleteProduct(productAId);
            }
            if (productBId > 0) {
                hardDeleteProduct(productBId);
            }
            if (categoryId > 0) {
                categoryDAO.delete(categoryId);
            }
            if (shopCouponCode != null) {
                try (Connection conn = orderDAO.openConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM promotions WHERE code = ?")) {
                    ps.setString(1, shopCouponCode);
                    ps.executeUpdate();
                }
            }
            if (ownerAId > 0) {
                new dao.shop.ShopProfileDAO().deleteByUserId(ownerAId);
            }
            if (ownerBId > 0) {
                new dao.shop.ShopProfileDAO().deleteByUserId(ownerBId);
            }
            if (ownerAId > 0) {
                userDAO.deleteUser(ownerAId);
            }
            if (ownerBId > 0) {
                userDAO.deleteUser(ownerBId);
            }
            if (customerId > 0) {
                userDAO.deleteUser(customerId);
            }
        } catch (SQLException ignored) {
            // Best-effort cleanup only.
        } finally {
            createdOrderId = -1;
            cartId = -1;
            variantAId = -1;
            variantBId = -1;
            productAId = -1;
            productBId = -1;
            categoryId = -1;
            ownerAId = -1;
            ownerBId = -1;
            customerId = -1;
            customerPhone = null;
            shopCouponCode = null;
        }
    }

    @Test
    public void allowMixedShopCheckoutOnGetWithPerShopShipping() throws Exception {
        env.clearRequestState();
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doGetPublic(env.request, env.response);

        assertNull(env.redirectLocation);
        assertEquals("/WEB-INF/jsp/customer/checkout.jsp", env.forwardedPath);
        assertEquals(2, ((Integer) env.requestAttributes.get("shopCount")).intValue());
        CartSummaryDTO summary = (CartSummaryDTO) env.requestAttributes.get("cartSummary");
        assertNotNull(summary);
        assertEquals(0, new BigDecimal("30000").compareTo(summary.getDeliveryFee()));
        assertEquals(0, new BigDecimal("210000").compareTo(summary.getTotal()));
    }

    @Test
    public void checkoutMultipleShopsWithoutShopVoucherCreatesParentAndChildOrders() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doPostPublic(env.request, env.response);

        assertNotNull(env.redirectLocation);
        assertTrue(env.redirectLocation.contains("/checkout?action=success&orderId="));
        createdOrderId = parseOrderId(env.redirectLocation);

        Order parent = orderDAO.findById(createdOrderId).get(0);
        assertEquals(AppConfig.ORDER_TYPE_PARENT, parent.getOrderType());
        assertNull(parent.getOwnerIdObject());
        assertNull(parent.getParentOrderId());
        assertEquals(0, new BigDecimal("180000").compareTo(parent.getTotalAmount()));
        assertEquals(0, new BigDecimal("30000").compareTo(parent.getDeliveryFee()));
        assertEquals(0, BigDecimal.ZERO.compareTo(parent.getDiscountAmount()));
        assertEquals(0, new BigDecimal("210000").compareTo(parent.getFinalAmount()));

        List<Order> children = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, children.size());
        Order childA = findChildByOwner(children, ownerAId);
        Order childB = findChildByOwner(children, ownerBId);
        assertChildOrder(childA, ownerAId, new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("115000"));
        assertChildOrder(childB, ownerBId, new BigDecimal("80000"), BigDecimal.ZERO, new BigDecimal("95000"));

        deliveryService.assignShipper(childA.getOrderId(), 0, LocalDateTime.now().plusDays(1));
        Delivery delivery = deliveryDAO.findByOrderId(childA.getOrderId());
        assertNotNull(delivery);
        assertNotNull(delivery.getDeliveryTripId());
        assertEquals(Integer.valueOf(1), delivery.getTripStopSeq());
        DeliveryTrip trip = deliveryTripDAO.findById(delivery.getDeliveryTripId());
        assertNotNull(trip);
        assertEquals(createdOrderId, trip.getParentOrderId());
        assertEquals(AppConfig.DELIVERY_TRIP_PLANNED, trip.getStatus());
    }

    @Test
    public void shopVoucherInMultiShopCheckoutDiscountsOnlyMatchingChildOrder() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", variantAId + "," + variantBId);
        env.putParam("shopCouponCode", shopCouponCode);

        servlet.doPostPublic(env.request, env.response);

        assertNotNull(env.redirectLocation);
        assertTrue(env.redirectLocation.contains("/checkout?action=success&orderId="));
        createdOrderId = parseOrderId(env.redirectLocation);

        Order parent = orderDAO.findById(createdOrderId).get(0);
        assertEquals(AppConfig.ORDER_TYPE_PARENT, parent.getOrderType());
        assertEquals(0, new BigDecimal("10000").compareTo(parent.getShopDiscountAmount()));
        assertEquals(0, new BigDecimal("10000").compareTo(parent.getDiscountAmount()));
        assertEquals(0, new BigDecimal("200000").compareTo(parent.getFinalAmount()));

        List<Order> children = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, children.size());
        assertChildOrder(findChildByOwner(children, ownerAId), ownerAId, new BigDecimal("100000"), new BigDecimal("10000"), new BigDecimal("105000"));
        assertChildOrder(findChildByOwner(children, ownerBId), ownerBId, new BigDecimal("80000"), BigDecimal.ZERO, new BigDecimal("95000"));
    }

    @Test
    public void calculatePlatformFeeAfterVoucherStackOnPost() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", String.valueOf(variantAId));
        env.putParam("shopCouponCode", shopCouponCode);
        env.putParam("systemCouponCode", "SAAN5");

        servlet.doPostPublic(env.request, env.response);

        assertNotNull(env.redirectLocation);
        assertTrue(env.redirectLocation.contains("/checkout?action=success&orderId="));
        assertEquals("success", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));

        createdOrderId = parseOrderId(env.redirectLocation);
        List<Order> orders = orderDAO.findById(createdOrderId);
        assertFalse(orders.isEmpty());
        Order order = orders.get(0);

        BigDecimal subtotal = SHOP_ITEM_PRICE;
        BigDecimal shopDiscount = new BigDecimal("10000");
        BigDecimal systemDiscount = new BigDecimal("5000");
        BigDecimal totalDiscount = shopDiscount.add(systemDiscount);
        BigDecimal expectedNet = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);
        BigDecimal platformRate = BigDecimal.valueOf(configDAO.getDouble(
                AppConfig.CONFIG_PLATFORM_FEE_RATE, AppConfig.PLATFORM_FEE_RATE_DEFAULT / 100.0));
        BigDecimal expectedPlatformFee = expectedNet.multiply(platformRate).setScale(0, java.math.RoundingMode.HALF_UP);
        BigDecimal expectedFinalAmount = subtotal.subtract(totalDiscount).add(order.getDeliveryFee()).max(BigDecimal.ZERO);

        assertEquals(0, order.getTotalAmount().compareTo(subtotal));
        assertEquals(0, order.getDiscountAmount().compareTo(totalDiscount));
        assertEquals(0, order.getShopDiscountAmount().compareTo(shopDiscount));
        assertEquals(0, order.getSystemDiscountAmount().compareTo(systemDiscount));
        assertEquals(0, order.getPlatformFee().compareTo(expectedPlatformFee));
        assertEquals(0, order.getFinalAmount().compareTo(expectedFinalAmount));
        assertEquals(ownerAId, order.getOwnerId());
        assertEquals(AppConfig.PAYMENT_COD, order.getPaymentMethod());
    }

    @Test
    public void rejectCheckoutForShopOwnerRole() throws Exception {
        env.clearRequestState();
        env.setCurrentUser(buildShopOwner(ownerAId));

        servlet.doGetPublic(env.request, env.response);

        assertEquals(Integer.valueOf(HttpServletResponse.SC_FORBIDDEN), env.errorStatus);
        assertNull(env.forwardedPath);
        assertNull(env.redirectLocation);
    }

    @Test
    public void rejectCheckoutWhenCsrfTokenMissing() throws Exception {
        env.clearRequestState();
        env.sessionAttributes.remove("_csrfToken");
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", String.valueOf(variantAId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/cart", env.redirectLocation);
        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
    }

    @Test
    public void rejectUnknownPaymentMethodOnCheckout() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", "BANKING");
        env.putParam("variantIds", String.valueOf(variantAId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/checkout?variantIds=" + variantAId, env.redirectLocation);
        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
    }

    @Test
    public void rejectCheckoutWhenDeliveryTimeSlotMissing() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", String.valueOf(variantAId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/checkout?variantIds=" + variantAId, env.redirectLocation);
        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
    }

    @Test
    public void rejectCheckoutWhenDeliveryTimeSlotBlank() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "   ");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", String.valueOf(variantAId));

        servlet.doPostPublic(env.request, env.response);

        assertEquals("/ctx/checkout?variantIds=" + variantAId, env.redirectLocation);
        assertEquals("error", env.sessionAttributes.get(AppConfig.SESSION_FLASH_TYPE));
        assertNotNull(env.sessionAttributes.get(AppConfig.SESSION_FLASH_MSG));
    }

    @Test
    public void checkoutMultipleShopsWithBankTransferCreatesParentPaymentFlow() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "12:00-16:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_CK);
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doPostPublic(env.request, env.response);

        assertNotNull(env.redirectLocation);
        assertTrue(env.redirectLocation.contains("/checkout?action=payment&orderId="));
        createdOrderId = parseOrderId(env.redirectLocation);

        Order parent = orderDAO.findById(createdOrderId).get(0);
        assertEquals(AppConfig.ORDER_TYPE_PARENT, parent.getOrderType());
        assertEquals(AppConfig.ORDER_PENDING_PAYMENT, parent.getStatus());
        assertEquals(AppConfig.PAYMENT_CK, parent.getPaymentMethod());

        List<Order> children = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, children.size());
        for (Order child : children) {
            assertEquals(AppConfig.ORDER_PENDING_PAYMENT, child.getStatus());
            assertEquals(AppConfig.PAYMENT_CK, child.getPaymentMethod());
        }

        PaymentTransaction paymentTransaction = paymentService.getPaymentByOrder(createdOrderId);
        assertNotNull(paymentTransaction);
        assertEquals("SEPAY", paymentTransaction.getPaymentMethod());
        assertEquals(0, parent.getFinalAmount().compareTo(paymentTransaction.getAmount()));
    }

    @Test
    public void sepayWebhookConfirmsOrderWithGeneratedReferenceAndPaymentViewMatchesQr() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "12:00-16:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_CK);
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doPostPublic(env.request, env.response);
        assertNotNull(env.redirectLocation);
        createdOrderId = parseOrderId(env.redirectLocation);

        PaymentTransaction paymentTransaction = paymentService.getPaymentByOrder(createdOrderId);
        assertNotNull(paymentTransaction);
        assertNotNull(paymentTransaction.getSepayReference());
        assertTrue(paymentTransaction.getSepayReference().matches("MF\\d{3,10}"));

        env.clearRequestState();
        env.putParam("action", "payment");
        env.putParam("orderId", String.valueOf(createdOrderId));
        servlet.doGetPublic(env.request, env.response);

        assertNull(env.redirectLocation);
        assertEquals("/WEB-INF/jsp/customer/order-payment.jsp", env.forwardedPath);
        assertEquals(paymentTransaction.getSepayReference(), env.requestAttributes.get("reference"));
        String qrUrl = (String) env.requestAttributes.get("qrUrl");
        assertNotNull(qrUrl);
        assertTrue(qrUrl.contains("qr.sepay.vn/img"));
        assertTrue(qrUrl.contains("des="));

        String webhookPayload = "{"
                + "\"id\":\"sepay-" + System.currentTimeMillis() + "\","
                + "\"code\":\"" + paymentTransaction.getSepayReference() + "\","
                + "\"transferType\":\"in\","
                + "\"transferAmount\":\"" + paymentTransaction.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + "\""
                + "}";
        paymentService.processWebhook(webhookPayload);

        Order confirmedOrder = orderDAO.findById(createdOrderId).get(0);
        assertEquals(AppConfig.ORDER_CONFIRMED, confirmedOrder.getStatus());
        List<Order> confirmedChildren = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, confirmedChildren.size());
        for (Order child : confirmedChildren) {
            assertEquals(AppConfig.ORDER_CONFIRMED, child.getStatus());
        }
        PaymentTransaction updatedTransaction = paymentService.getPaymentByOrder(createdOrderId);
        assertNotNull(updatedTransaction);
        assertEquals("completed", updatedTransaction.getStatus());

        env.clearRequestState();
        env.putParam("action", "payment");
        env.putParam("orderId", String.valueOf(createdOrderId));
        servlet.doGetPublic(env.request, env.response);
        assertEquals("/ctx/checkout?action=success&orderId=" + createdOrderId, env.redirectLocation);
        assertNull(env.forwardedPath);

        env.clearRequestState();
        env.putParam("action", "success");
        env.putParam("orderId", String.valueOf(createdOrderId));
        servlet.doGetPublic(env.request, env.response);
        assertNull(env.redirectLocation);
        assertEquals("/WEB-INF/jsp/customer/order-success.jsp", env.forwardedPath);
        assertNotNull(env.requestAttributes.get("paymentTx"));
        assertEquals("completed", ((PaymentTransaction) env.requestAttributes.get("paymentTx")).getStatus());
    }

    @Test
    public void adminApprovalCascadesParentAndChildrenToConfirmed() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "12:00-16:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_CK);
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doPostPublic(env.request, env.response);
        assertNotNull(env.redirectLocation);
        createdOrderId = parseOrderId(env.redirectLocation);

        int adminId = createUser("Checkout Admin", "checkout_admin_" + System.currentTimeMillis() + "@test.com", "ADMIN", buildUniquePhone(4));
        try {
            paymentService.adminApprovePayment(createdOrderId, adminId);

            Order confirmedOrder = orderDAO.findById(createdOrderId).get(0);
            assertEquals(AppConfig.ORDER_CONFIRMED, confirmedOrder.getStatus());

            List<Order> confirmedChildren = orderDAO.findChildrenByParentId(createdOrderId);
            assertEquals(2, confirmedChildren.size());
            for (Order child : confirmedChildren) {
                assertEquals(AppConfig.ORDER_CONFIRMED, child.getStatus());
            }

            PaymentTransaction updatedTransaction = paymentService.getPaymentByOrder(createdOrderId);
            assertNotNull(updatedTransaction);
            assertEquals("completed", updatedTransaction.getStatus());
        } finally {
            userDAO.deleteUser(adminId);
        }
    }

    @Test
    public void reconcileCompletedPaymentsRepairsPendingChildOrders() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "12:00-16:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_CK);
        env.putParam("variantIds", variantAId + "," + variantBId);

        servlet.doPostPublic(env.request, env.response);
        assertNotNull(env.redirectLocation);
        createdOrderId = parseOrderId(env.redirectLocation);

        PaymentTransaction paymentTransaction = paymentService.getPaymentByOrder(createdOrderId);
        assertNotNull(paymentTransaction);

        String webhookPayload = "{"
                + "\"id\":\"sepay-reconcile-" + System.currentTimeMillis() + "\","
                + "\"code\":\"" + paymentTransaction.getSepayReference() + "\","
                + "\"transferType\":\"in\","
                + "\"transferAmount\":\"" + paymentTransaction.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + "\""
                + "}";
        paymentService.processWebhook(webhookPayload);

        List<Order> confirmedChildren = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, confirmedChildren.size());
        for (Order child : confirmedChildren) {
            assertEquals(AppConfig.ORDER_CONFIRMED, child.getStatus());
        }

        for (Order child : confirmedChildren) {
            forceOrderStatus(child.getOrderId(), AppConfig.ORDER_PENDING_PAYMENT);
        }

        int repaired = paymentService.reconcileCompletedPayments();
        assertEquals(1, repaired);

        Order confirmedOrder = orderDAO.findById(createdOrderId).get(0);
        assertEquals(AppConfig.ORDER_CONFIRMED, confirmedOrder.getStatus());

        List<Order> repairedChildren = orderDAO.findChildrenByParentId(createdOrderId);
        assertEquals(2, repairedChildren.size());
        for (Order child : repairedChildren) {
            assertEquals(AppConfig.ORDER_CONFIRMED, child.getStatus());
        }

        PaymentTransaction updatedTransaction = paymentService.getPaymentByOrder(createdOrderId);
        assertNotNull(updatedTransaction);
        assertEquals("completed", updatedTransaction.getStatus());
        assertEquals(0, paymentService.reconcileCompletedPayments());
    }

    @Test
    public void checkoutStatusEndpointReturnsApiEnvelopeAndUnknownFallback() throws Exception {
        env.clearRequestState();
        env.putParam("_csrf", CSRF_TOKEN);
        env.putParam("fullName", "Checkout Customer");
        env.putParam("phone", customerPhone);
        env.putParam("deliveryAddress", "123 Test Street, District 1");
        env.putParam("deliveryTimeSlot", "08:00-12:00");
        env.putParam("paymentMethod", AppConfig.PAYMENT_COD);
        env.putParam("variantIds", String.valueOf(variantAId));

        servlet.doPostPublic(env.request, env.response);

        assertNotNull(env.redirectLocation);
        createdOrderId = parseOrderId(env.redirectLocation);

        env.clearRequestState();
        env.putParam("action", "status");
        env.putParam("orderId", String.valueOf(createdOrderId));
        servlet.doGetPublic(env.request, env.response);

        String existingBody = env.getResponseBody();
        assertNull(env.redirectLocation);
        assertNull(env.forwardedPath);
        assertNull(env.errorStatus);
        assertTrue(existingBody.contains("\"success\":true"));
        assertTrue(existingBody.contains("\"data\":"));
        assertTrue(existingBody.contains("\"status\":\"" + AppConfig.ORDER_CONFIRMED + "\""));

        env.clearRequestState();
        env.putParam("action", "status");
        env.putParam("orderId", String.valueOf(createdOrderId + 999999));
        servlet.doGetPublic(env.request, env.response);

        String missingBody = env.getResponseBody();
        assertNull(env.redirectLocation);
        assertNull(env.forwardedPath);
        assertNull(env.errorStatus);
        assertTrue(missingBody.contains("\"success\":true"));
        assertTrue(missingBody.contains("\"status\":\"UNKNOWN\""));
    }

    @Test
    public void stockCheckUsesOnlySelectedVariants() throws Exception {
        ProductVariant inactiveVariant = variantDAO.findById(variantBId);
        assertNotNull(inactiveVariant);
        inactiveVariant.setIsActive(false);
        variantDAO.update(inactiveVariant);

        List<String> selectedErrors = cartService.checkCartStockBeforeCheckout(customerId, List.of(variantAId));
        assertTrue(selectedErrors.isEmpty());

        List<String> fullCartErrors = cartService.checkCartStockBeforeCheckout(customerId);
        assertFalse(fullCartErrors.isEmpty());
    }

    private void assertChildOrder(Order child, int expectedOwnerId, BigDecimal expectedSubtotal,
                                  BigDecimal expectedDiscount, BigDecimal expectedFinalAmount) {
        assertNotNull(child);
        assertEquals(AppConfig.ORDER_TYPE_CHILD, child.getOrderType());
        assertEquals(createdOrderId, child.getParentOrderId().intValue());
        assertEquals(expectedOwnerId, child.getOwnerId());
        assertEquals(0, expectedSubtotal.compareTo(child.getTotalAmount()));
        assertEquals(0, new BigDecimal("15000").compareTo(child.getDeliveryFee()));
        assertEquals(0, expectedDiscount.compareTo(child.getDiscountAmount()));
        assertEquals(0, expectedFinalAmount.compareTo(child.getFinalAmount()));
    }

    private Order findChildByOwner(List<Order> children, int ownerId) {
        for (Order child : children) {
            if (child.getOwnerId() == ownerId) {
                return child;
            }
        }
        return null;
    }

    private int createUser(String fullName, String email, String role, String phone) throws SQLException {
        return userDAO.saveNewCustomer(fullName, email, "hashed_pwd", phone, role, "ACTIVE", true);
    }

    private String buildUniquePhone(int salt) {
        long raw = Math.abs((System.currentTimeMillis() + salt) % 100000000L);
        return String.format("09%08d", raw);
    }

    private int createCategory(String name) throws SQLException {
        model.entity.catalog.Category cat = new model.entity.catalog.Category();
        cat.setName(name);
        cat.setSlug(name.toLowerCase().replace(' ', '-'));
        cat.setDisplayOrder(88);
        cat.setIsActive(true);
        return categoryDAO.save(cat);
    }

    private int createProduct(int ownerId, String name) throws SQLException {
        Product p = new Product();
        p.setOwnerId(ownerId);
        p.setCategoryId(categoryId);
        p.setName(name);
        p.setDescription("Checkout regression product");
        p.setOriginCountry("Vietnam");
        p.setOriginRegion("North");
        p.setHarvestDate(LocalDate.now().plusDays(2));
        p.setShelfLifeDays(30);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setIsOrganic(false);
        p.setIsImported(false);
        return productDAO.save(p);
    }

    private int createVariant(int productId, String skuSuffix, BigDecimal price) throws SQLException {
        ProductVariant v = new ProductVariant();
        v.setProductId(productId);
        v.setSku("CHK-" + skuSuffix + "-" + System.currentTimeMillis());
        v.setVariantLabel("1kg");
        v.setPrice(price);
        v.setStockQuantity(50);
        v.setIsActive(true);
        return variantDAO.save(v);
    }

    private void hardDeleteCart(int cartId) throws SQLException {
        try (Connection conn = cartDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?")) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }
        }
    }

    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE user_id IN (?, ?) AND action_url = ?")) {
                ps.setInt(1, ownerAId);
                ps.setInt(2, ownerBId);
                ps.setString(3, "/shop/orders");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM payment_transactions WHERE order_id = ? OR order_id IN (SELECT order_id FROM orders WHERE parent_order_id = ?)")) {
                ps.setInt(1, orderId);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM deliveries WHERE order_id = ? OR order_id IN (SELECT order_id FROM orders WHERE parent_order_id = ?)")) {
                ps.setInt(1, orderId);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM delivery_trips WHERE parent_order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM order_promotions WHERE order_id = ? OR order_id IN (SELECT order_id FROM orders WHERE parent_order_id = ?)")) {
                ps.setInt(1, orderId);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM order_items WHERE order_id = ? OR order_id IN (SELECT order_id FROM orders WHERE parent_order_id = ?)")) {
                ps.setInt(1, orderId);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE parent_order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
        }
    }

    private void forceOrderStatus(int orderId, String status) throws SQLException {
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ?, updated_at = GETDATE() WHERE order_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM inventory_logs WHERE variant_id IN (SELECT variant_id FROM product_variants WHERE product_id = ?)")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
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

    private int parseOrderId(String redirectLocation) {
        int idx = redirectLocation.lastIndexOf("orderId=");
        if (idx < 0) {
            throw new IllegalArgumentException("Cannot parse orderId from redirect: " + redirectLocation);
        }
        return Integer.parseInt(redirectLocation.substring(idx + "orderId=".length()));
    }

    private User buildCustomer(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName("Checkout Regression Customer");
        user.setRole(AppConfig.ROLE_CUSTOMER);
        user.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        return user;
    }

    private User buildShopOwner(int userId) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName("Checkout Regression Shop Owner");
        user.setRole(AppConfig.ROLE_SHOP_OWNER);
        user.setStatus(AppConfig.ACCOUNT_STATUS_ACTIVE);
        return user;
    }

    private final class CheckoutServletHarness extends CheckoutServlet {
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
        private String errorMessage;
        private String forwardedPath;
        private String servletPath = "/checkout";
        private final StringWriter responseBody = new StringWriter();
        private final PrintWriter responseWriter = new PrintWriter(responseBody, true);

        private final HttpSession session;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        private MockHttpEnvironment() {
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
            errorMessage = null;
            forwardedPath = null;
            responseBody.getBuffer().setLength(0);
            responseWriter.flush();
        }

        private String getResponseBody() {
            responseWriter.flush();
            return responseBody.toString();
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
                    case "invalidate":
                        sessionAttributes.clear();
                        return null;
                    case "toString":
                        return "MockHttpSession";
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
                    case "getParameterMap":
                        return new HashMap<>(params);
                    case "getParameterNames":
                        return java.util.Collections.enumeration(params.keySet());
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
                    case "removeAttribute":
                        requestAttributes.remove(args[0]);
                        return null;
                    case "getRequestDispatcher":
                        return createDispatcher((String) args[0]);
                    case "toString":
                        return "MockHttpServletRequest";
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
                    case "getWriter":
                        return responseWriter;
                    case "sendRedirect":
                        redirectLocation = (String) args[0];
                        return null;
                    case "sendError":
                        errorStatus = (Integer) args[0];
                        errorMessage = args != null && args.length > 1 ? (String) args[1] : null;
                        return null;
                    case "setStatus":
                        errorStatus = (Integer) args[0];
                        return null;
                    case "toString":
                        return "MockHttpServletResponse";
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

    private void createShopProfile(int userId, String name) throws SQLException {
        dao.shop.ShopProfileDAO shopProfileDAO = new dao.shop.ShopProfileDAO();
        model.entity.shop.ShopProfile p = new model.entity.shop.ShopProfile();
        p.setUserId(userId);
        p.setShopName(name);
        p.setShopDescription("Regression Test Shop Description");
        p.setApprovalStatus("APPROVED");
        p.setDeliveryAddress("123 Test Street");
        p.setRating(java.math.BigDecimal.ZERO);
        p.setBusinessEmail("reg_biz_" + userId + "_" + System.currentTimeMillis() + "@company.com");
        shopProfileDAO.save(p);
    }
}
