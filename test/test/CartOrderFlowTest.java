package test;

import dao.cart.CartDAO;
import dao.catalog.CategoryDAO;
import dao.order.OrderDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import service.order.OrderService;
import dao.auth.UserDAO;
import service.order.ReviewService;
import model.entity.cart.Cart;
import model.entity.cart.CartItem;
import model.entity.catalog.Category;
import model.entity.order.Order;
import model.entity.order.OrderItem;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.order.Review;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * CartOrderFlowTest — Bộ kiểm thử JUnit 4 cho toàn bộ luồng nghiệp vụ:
 *   Giỏ hàng (Cart) → Đặt hàng (Order) → Hủy đơn → Khôi phục tồn kho.
 *
 * CÁC LUỒNG KIỂM TRA:
 *   1. Thêm sản phẩm vào giỏ hàng và tính tổng tiền chính xác
 *   2. Cập nhật số lượng item trong giỏ hàng
 *   3. Xóa item khỏi giỏ hàng
 *   4. Tạo đơn hàng từ giỏ hàng và kiểm tra trạng thái ban đầu
 *   5. Luồng xử lý trạng thái đơn hàng (PENDING → CONFIRMED → DELIVERED)
 *   6. Hủy đơn hàng và kiểm tra hoàn trả kho tự động
 *
 * QUY TẮC KIỂM THỬ:
 *   - Test đúng theo nghiệp vụ, KHÔNG test thuần code
 *   - Mỗi test phải tự dọn dẹp dữ liệu sau khi chạy xong
 *   - setUp/tearDown phải đảm bảo isolation giữa các test case
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CartOrderFlowTest {

    // ---- DAOs ----
    private CartDAO    cartDAO;
    private OrderDAO   orderDAO;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private CategoryDAO categoryDAO;
    private UserDAO    userDAO;
    private OrderService orderService;
    private ReviewService reviewService;

    // ---- IDs của dữ liệu tạm tạo trong test ----
    private int testOwnerId    = -1;
    private int testCustomerId = -1;
    private int testCategoryId = -1;
    private int testProductId  = -1;
    private int testVariantId  = -1;
    private int testCartId     = -1;
    private int testOrderId    = -1;

    // ---- Giá ban đầu của biến thể ----
    private static final BigDecimal VARIANT_PRICE = new BigDecimal("95000.00");
    private static final BigDecimal DELIVERY_FEE  = new BigDecimal("20000.00");

    @Before
    public void setUp() throws SQLException {
        cartDAO    = new CartDAO();
        orderDAO   = new OrderDAO();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        categoryDAO = new CategoryDAO();
        userDAO    = new UserDAO();
        orderService = new OrderService();
        reviewService = new ReviewService();

        // 1. Tạo shop owner test
        String ownerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime()) % 100000000L));
        testOwnerId = userDAO.saveNewCustomer(
            "Cart Test Owner",
            "cart_test_owner_" + System.currentTimeMillis() + "@test.com",
            "hashed_pwd",
            ownerPhone,
            "SHOP_OWNER",
            "ACTIVE",
            true
        );
        assertTrue("testOwnerId phải > 0", testOwnerId > 0);

        // 2. Tạo customer test
        String customerPhone = "09" + String.format("%08d", Math.abs((System.nanoTime() + 1) % 100000000L));
        testCustomerId = userDAO.saveNewCustomer(
            "Cart Test Customer",
            "cart_test_cust_" + System.currentTimeMillis() + "@test.com",
            "hashed_pwd",
            customerPhone,
            "CUSTOMER",
            "ACTIVE",
            true
        );
        assertTrue("testCustomerId phải > 0", testCustomerId > 0);

        // 3. Tạo category test
        Category cat = new Category();
        cat.setName("Cart Test Cat " + System.currentTimeMillis());
        cat.setSlug("cart-test-cat-" + System.currentTimeMillis());
        cat.setDisplayOrder(98);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);
        assertTrue("testCategoryId phải > 0", testCategoryId > 0);

        // 4. Tạo sản phẩm test (APPROVED, còn hạn)
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Cam Test JUnit Cart");
        p.setDescription("Sản phẩm test cho CartOrderFlowTest");
        p.setOriginCountry("Việt Nam");
        p.setOriginRegion("Hòa Bình");
        p.setHarvestDate(LocalDate.now().plusDays(2));
        p.setShelfLifeDays(30);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        p.setIsOrganic(false);
        p.setIsImported(false);
        testProductId = productDAO.save(p);
        assertTrue("testProductId phải > 0", testProductId > 0);

        // 5. Tạo biến thể test có tồn kho = 50
        ProductVariant v = new ProductVariant();
        v.setProductId(testProductId);
        v.setSku("TEST-CAM-CART-SKU-" + System.currentTimeMillis());
        v.setVariantLabel("Hộp 1kg test");
        v.setPrice(VARIANT_PRICE);
        v.setStockQuantity(50);
        v.setIsActive(true);
        testVariantId = variantDAO.save(v);
        assertTrue("testVariantId phải > 0", testVariantId > 0);

        // 6. Tạo giỏ hàng cho customer
        testCartId = cartDAO.createForCustomer(testCustomerId);
        assertTrue("testCartId phải > 0", testCartId > 0);
    }

    @After
    public void tearDown() {
        try {
            // Xóa đơn hàng test (order_items sẽ bị cascade)
            if (testOrderId != -1) {
                hardDeleteOrder(testOrderId);
                testOrderId = -1;
            }
            // Xóa giỏ hàng
            if (testCartId != -1) {
                hardDeleteCart(testCartId);
                testCartId = -1;
            }
            // Xóa sản phẩm (variants + images bị cascade)
            if (testProductId != -1) {
                hardDeleteProduct(testProductId);
                testProductId = -1;
            }
            // Xóa category
            if (testCategoryId != -1) {
                categoryDAO.delete(testCategoryId);
                testCategoryId = -1;
            }
            // Xóa users
            if (testOwnerId != -1) {
                userDAO.deleteUser(testOwnerId);
                testOwnerId = -1;
            }
            if (testCustomerId != -1) {
                userDAO.deleteUser(testCustomerId);
                testCustomerId = -1;
            }
        } catch (SQLException e) {
            System.err.println("[CartOrderFlowTest] Cleanup failed: " + e.getMessage());
        }
    }

    // =========================================================
    // NGHIỆP VỤ 1: Giỏ hàng — Thêm, cập nhật, xóa sản phẩm
    // =========================================================

    /**
     * TC-CART-01: Thêm sản phẩm vào giỏ hàng và kiểm tra tính tổng tiền chính xác.
     * Nghiệp vụ: Khách hàng thêm 2 đơn vị vào giỏ hàng → tổng = price × qty
     */
    @Test
    public void test01_AddItemToCartAndVerifySubtotal() throws SQLException {
        // Thêm 2 hộp vào giỏ
        cartDAO.addItem(testCartId, testVariantId, 2);

        List<CartItem> items = cartDAO.findItems(testCartId);
        assertEquals("Giỏ hàng phải có đúng 1 dòng item", 1, items.size());

        CartItem item = items.get(0);
        assertEquals("Số lượng phải là 2", 2, item.getQuantity());
        assertEquals("Giá đơn vị phải khớp", VARIANT_PRICE, item.getPrice());

        // Kiểm tra tính toán subtotal: 95,000 × 2 = 190,000
        BigDecimal expectedSubtotal = VARIANT_PRICE.multiply(new BigDecimal(2));
        BigDecimal actualSubtotal   = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
        assertEquals("Tổng tiền item phải được tính chính xác", expectedSubtotal, actualSubtotal);
    }

    /**
     * TC-CART-02: Thêm cùng sản phẩm lần 2 → số lượng phải cộng dồn (idempotent merge).
     * Nghiệp vụ: Không tạo dòng trùng, mà cộng quantity vào dòng đã có.
     */
    @Test
    public void test02_AddSameItemTwiceMergesQuantity() throws SQLException {
        cartDAO.addItem(testCartId, testVariantId, 1);
        cartDAO.addItem(testCartId, testVariantId, 3); // thêm lần 2

        List<CartItem> items = cartDAO.findItems(testCartId);
        assertEquals("Vẫn phải là 1 dòng duy nhất (không tạo dòng mới)", 1, items.size());
        assertEquals("Số lượng phải được gộp thành 4", 4, items.get(0).getQuantity());
    }

    /**
     * TC-CART-03: Cập nhật số lượng item trong giỏ hàng.
     * Nghiệp vụ: Khách hàng thay đổi số lượng từ trang giỏ hàng.
     */
    @Test
    public void test03_UpdateItemQuantity() throws SQLException {
        cartDAO.addItem(testCartId, testVariantId, 1);
        List<CartItem> items = cartDAO.findItems(testCartId);
        int cartItemId = items.get(0).getCartItemId();

        // Cập nhật lên 5
        cartDAO.updateItemQuantity(cartItemId, 5);

        List<CartItem> updated = cartDAO.findItems(testCartId);
        assertEquals("Số lượng sau cập nhật phải là 5", 5, updated.get(0).getQuantity());
    }

    /**
     * TC-CART-04: Xóa item khỏi giỏ hàng → giỏ phải trống.
     * Nghiệp vụ: Khách hàng bỏ sản phẩm khỏi giỏ.
     */
    @Test
    public void test04_RemoveItemFromCart() throws SQLException {
        cartDAO.addItem(testCartId, testVariantId, 2);
        List<CartItem> items = cartDAO.findItems(testCartId);
        int cartItemId = items.get(0).getCartItemId();

        cartDAO.removeItem(cartItemId);

        List<CartItem> afterRemove = cartDAO.findItems(testCartId);
        assertTrue("Giỏ hàng phải trống sau khi xóa item duy nhất", afterRemove.isEmpty());
    }

    /**
     * TC-CART-05: Xóa sạch toàn bộ giỏ hàng (clearCart).
     * Nghiệp vụ: Sau khi đặt hàng thành công, giỏ hàng phải được reset.
     */
    @Test
    public void test05_ClearCartAfterCheckout() throws SQLException {
        cartDAO.addItem(testCartId, testVariantId, 3);
        assertEquals("Phải có 1 item trước khi clear", 1, cartDAO.findItems(testCartId).size());

        cartDAO.clearCart(testCartId);

        assertTrue("Giỏ hàng phải trống sau clearCart", cartDAO.findItems(testCartId).isEmpty());
    }

    // =========================================================
    // NGHIỆP VỤ 2: Đặt hàng — Tạo đơn, chuyển trạng thái
    // =========================================================

    /**
     * TC-ORDER-01: Tạo đơn hàng mới và kiểm tra trạng thái ban đầu là PENDING_PAYMENT.
     * Nghiệp vụ: Sau khi checkout, đơn hàng mặc định là PENDING_PAYMENT (chưa thanh toán).
     */
    @Test
    public void test06_CreateOrderWithCorrectInitialStatus() throws SQLException {
        cartDAO.addItem(testCartId, testVariantId, 2);

        // Tạo đơn hàng
        Order order = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE.multiply(new BigDecimal(2)));
        testOrderId = orderDAO.save(order);
        assertTrue("order_id phải > 0", testOrderId > 0);

        // Kiểm tra trạng thái
        List<Order> orders = orderDAO.findById(testOrderId);
        assertFalse("Phải tìm thấy đơn hàng", orders.isEmpty());
        Order saved = orders.get(0);
        assertEquals("Trạng thái ban đầu phải là PENDING_PAYMENT", "PENDING_PAYMENT", saved.getStatus());
        assertEquals("customer_id phải khớp", testCustomerId, saved.getCustomerId());
        assertEquals("owner_id phải khớp", testOwnerId, saved.getOwnerId());
    }

    /**
     * TC-ORDER-02: Luồng chuyển trạng thái đơn hàng hợp lệ theo SRS.
     * Nghiệp vụ: PENDING_PAYMENT -> CONFIRMED -> DISPATCHED -> DELIVERED.
     */
    @Test
    public void test07_OrderStatusTransitionFlow() throws SQLException {
        Order order = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE);
        testOrderId = orderDAO.save(order);

        orderService.confirmOrder(testOrderId, testOwnerId);
        assertEquals("Sau APPROVED", "APPROVED", getOrderStatus(testOrderId));

        orderService.dispatchOrder(testOrderId, testOwnerId);
        assertEquals("Sau DISPATCHED", "DISPATCHED", getOrderStatus(testOrderId));

        orderService.customerConfirmDelivery(testOrderId, testCustomerId);
        assertEquals("Sau DELIVERED", "DELIVERED", getOrderStatus(testOrderId));
        assertEquals("received_status phải được cập nhật", "RECEIVED", orderDAO.findById(testOrderId).get(0).getReceivedStatus());
    }

    /**
     * TC-ORDER-03: Hủy đơn hàng — kiểm tra trạng thái CANCELLED và lý do hủy được lưu.
     * Nghiệp vụ: Khách hàng hoặc shop có thể hủy đơn kèm lý do.
     */
    @Test
    public void test08_CancelOrderWithReason() throws SQLException {
        Order order = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE);
        order.setStatus("CONFIRMED");
        testOrderId = orderDAO.save(order);

        String cancelReason = "Khách hàng đổi ý không muốn mua nữa";
        orderDAO.cancel(testOrderId, testCustomerId, cancelReason);

        List<Order> orders = orderDAO.findById(testOrderId);
        Order cancelled = orders.get(0);
        assertEquals("Trạng thái phải là CANCELLED", "CANCELLED", cancelled.getStatus());
        assertEquals("Lý do hủy phải được lưu chính xác", cancelReason, cancelled.getCancellationReason());
        assertEquals("Người hủy phải là customer", testCustomerId, (int) cancelled.getCancelledBy());
        assertNotNull("cancelled_at phải có giá trị", cancelled.getCancelledAt());
    }

    /**
     * TC-ORDER-04: Hủy đơn hàng → hoàn trả tồn kho sản phẩm.
     * Nghiệp vụ: Sau khi hủy, số lượng tồn kho phải được cộng trả lại.
     */
    @Test
    public void test09_CancelOrderRestoresInventory() throws SQLException {
        // Kiểm tra tồn kho ban đầu
        ProductVariant varBefore = variantDAO.findById(testVariantId);
        int stockBefore = varBefore.getStockQuantity();

        // Tạo đơn hàng và trừ tồn kho thủ công (simulate checkout)
        Order order = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE.multiply(new BigDecimal(3)));
        order.setStatus("CONFIRMED");
        testOrderId = orderDAO.save(order);

        // Chèn order_item để hoàn trả kho đúng
        insertOrderItem(testOrderId, testVariantId, 3, VARIANT_PRICE);

        // Trừ kho (simulate checkout)
        deductStock(testVariantId, 3);

        ProductVariant varAfterCheckout = variantDAO.findById(testVariantId);
        assertEquals("Tồn kho phải giảm 3 sau checkout", stockBefore - 3, varAfterCheckout.getStockQuantity());

        // Hủy đơn và hoàn trả kho
        orderDAO.cancel(testOrderId, testCustomerId, "Hủy để test hoàn kho");
        orderDAO.restoreInventoryStock(testOrderId);

        // Kiểm tra kho được hoàn lại
        ProductVariant varAfterCancel = variantDAO.findById(testVariantId);
        assertEquals("Tồn kho phải được hoàn trả về mức ban đầu sau hủy đơn",
                     stockBefore, varAfterCancel.getStockQuantity());
    }

    /**
     * TC-ORDER-05: Phân trang đơn hàng theo customer — đếm đúng số đơn.
     * Nghiệp vụ: Trang lịch sử đơn hàng của khách phải hiển thị đủ các đơn.
     */
    @Test
    public void test10_PaginationOrdersByCustomer() throws SQLException {
        // Đếm số đơn ban đầu của testCustomer
        int countBefore = orderDAO.countByCustomer(testCustomerId);

        // Tạo thêm 2 đơn hàng
        Order o1 = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE);
        int id1 = orderDAO.save(o1);

        Order o2 = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE.multiply(new BigDecimal(2)));
        int id2 = orderDAO.save(o2);

        int countAfter = orderDAO.countByCustomer(testCustomerId);
        assertEquals("Số đơn hàng phải tăng đúng 2", countBefore + 2, countAfter);

        // Lấy trang 1, page size 10 → phải chứa cả 2 đơn mới
        List<Order> page1 = orderDAO.findByCustomer(testCustomerId, 1, 10);
        assertTrue("Danh sách đơn hàng phải chứa đơn mới nhất", page1.size() >= 2);

        // Cleanup thêm
        hardDeleteOrder(id1);
        hardDeleteOrder(id2);
    }

    /**
     * TC-REVIEW-01: Gửi, sửa và xóa đánh giá phải tự động cập nhật rating trung bình của sản phẩm.
     * Nghiệp vụ: Rating của product phải phản ánh tập review hợp lệ hiện tại.
     */
    @Test
    public void test11_ReviewLifecycleRecalculatesProductRating() throws SQLException {
        Order order = buildTestOrder(testCustomerId, testOwnerId, VARIANT_PRICE);
        testOrderId = orderDAO.save(order);
        insertOrderItem(testOrderId, testVariantId, 1, VARIANT_PRICE);
        orderDAO.updateStatus(testOrderId, "DELIVERED");

        int orderItemId = orderDAO.findItemsByOrderId(testOrderId).get(0).getOrderItemId();

        Review review = new Review();
        review.setOrderItemId(orderItemId);
        review.setCustomerId(testCustomerId);
        review.setRating(5);
        review.setReviewText("Sản phẩm tươi, đóng gói tốt.");
        review.setReviewImageUrl(null);

        reviewService.submitReview(review);
        BigDecimal ratingAfterCreate = productDAO.findById(testProductId).get(0).getRating();
        assertNotNull("Rating phải được cập nhật sau khi tạo review", ratingAfterCreate);
        assertEquals("Rating trung bình phải là 5 sau review đầu tiên", 0, ratingAfterCreate.compareTo(new BigDecimal("5.00")));

        review.setRating(3);
        review.setReviewText("Sản phẩm vẫn tốt nhưng giao hơi chậm.");
        reviewService.updateReview(review);
        BigDecimal ratingAfterEdit = productDAO.findById(testProductId).get(0).getRating();
        assertEquals("Rating trung bình phải được cập nhật sau khi sửa review", 0, ratingAfterEdit.compareTo(new BigDecimal("3.00")));

        reviewService.deleteReview(review.getReviewId());
        BigDecimal ratingAfterDelete = productDAO.findById(testProductId).get(0).getRating();
        assertEquals("Rating phải quay về 0 sau khi xóa review cuối cùng", 0, ratingAfterDelete.compareTo(BigDecimal.ZERO));
    }

    // =========================================================
    // Các helper methods
    // =========================================================

    /** Tạo Order object chuẩn để test */
    private Order buildTestOrder(int customerId, int ownerId, BigDecimal totalAmount) {
        Order o = new Order();
        o.setCustomerId(customerId);
        o.setOwnerId(ownerId);
        o.setDeliveryAddress("123 Test Street, JUnit District");
        o.setDeliveryTimeSlot("08:00-12:00");
        o.setNotes("Test order from CartOrderFlowTest");
        o.setStatus("PENDING_PAYMENT");
        o.setTotalAmount(totalAmount);
        o.setDeliveryFee(DELIVERY_FEE);
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setSystemDiscountAmount(BigDecimal.ZERO);
        o.setShopDiscountAmount(BigDecimal.ZERO);
        o.setPlatformFee(BigDecimal.ZERO);
        o.setFinalAmount(totalAmount.add(DELIVERY_FEE));
        o.setPaymentMethod("COD");
        o.setRefundStatus("NONE");
        return o;
    }

    /** Lấy trạng thái đơn hàng theo ID */
    private String getOrderStatus(int orderId) throws SQLException {
        List<Order> orders = orderDAO.findById(orderId);
        return orders.isEmpty() ? null : orders.get(0).getStatus();
    }

    /** Chèn order_item để test hoàn trả kho */
    private void insertOrderItem(int orderId, int variantId, int qty, BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, variant_id, product_name_snapshot, variant_label_snapshot, quantity, unit_price, subtotal) "
                   + "VALUES (?, ?, N'Cam Test JUnit', N'Hộp 1kg', ?, ?, ?)";
        try (Connection conn = orderDAO.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, variantId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            ps.setBigDecimal(5, unitPrice.multiply(new BigDecimal(qty)));
            ps.executeUpdate();
        }
    }

    /** Trừ tồn kho thủ công để simulate checkout */
    private void deductStock(int variantId, int qty) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE variant_id = ?";
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, variantId);
            ps.executeUpdate();
        }
    }

    /** Xóa cứng giỏ hàng và cart_items */
    private void hardDeleteCart(int cartId) throws SQLException {
        try (Connection conn = cartDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                ps.setInt(1, cartId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?")) {
                ps.setInt(1, cartId); ps.executeUpdate();
            }
        }
    }

    /** Xóa cứng đơn hàng và order_items */
    private void hardDeleteOrder(int orderId) throws SQLException {
        try (Connection conn = orderDAO.openConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM deliveries WHERE order_id = ?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM delivery_trips WHERE parent_order_id = ?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
        }
    }

    /** Xóa cứng sản phẩm cùng biến thể */
    private void hardDeleteProduct(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM product_variants WHERE product_id = ?")) {
                ps.setInt(1, productId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                ps.setInt(1, productId); ps.executeUpdate();
            }
        }
    }
}
