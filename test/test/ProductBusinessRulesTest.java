package test;

import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.auth.UserDAO;
import dao.cart.CartDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.cart.Cart;
import model.entity.cart.CartItem;
import service.catalog.ProductService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductBusinessRulesTest — Bộ kiểm thử cho các quy tắc nghiệp vụ cốt lõi:
 *   1. Quản lý hết hạn & Mùa vụ (Seasonal Availability / Auto Deactivation)
 *   2. Đóng gói tùy chọn (Packaging Options & Price Additions)
 *   3. Biến thể trọng lượng & Giá giảm (Weight Variants, Base & Discount Pricing)
 *   4. Cảnh báo tồn kho & Yêu cầu nhập kho (Low Stock Alerts & Restock Management)
 */
public class ProductBusinessRulesTest {

    private ProductService productService;
    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private CategoryDAO categoryDAO;
    private UserDAO userDAO;
    private CartDAO cartDAO;

    private int testOwnerId = -1;
    private int testCustomerId = -1;
    private int testCategoryId = -1;
    private int testProductId1 = -1; // Sản phẩm hết hạn
    private int testProductId2 = -1; // Sản phẩm thường
    private int testVariantId1 = -1; // Biến thể có giảm giá hoạt động
    private int testVariantId2 = -1; // Biến thể có giảm giá hết hạn
    private int testPackagingId = -1; // Lựa chọn đóng gói
    private int testCartId = -1;

    @Before
    public void setUp() throws SQLException {
        productService = new ProductService();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        categoryDAO = new CategoryDAO();
        userDAO = new UserDAO();
        cartDAO = new CartDAO();

        // 1. Tạo tài khoản shop owner & customer giả lập
        testOwnerId = userDAO.saveNewCustomer(
            "Shop Rules Owner", 
            "shop_rules_" + System.currentTimeMillis() + "@test.com", 
            "hashed_pwd", 
            "0971112222", 
            "SHOP_OWNER", 
            "ACTIVE", 
            true
        );
        testCustomerId = userDAO.saveNewCustomer(
            "Customer Junit Rules", 
            "cust_rules_" + System.currentTimeMillis() + "@test.com", 
            "hashed_pwd", 
            "0972223333", 
            "CUSTOMER", 
            "ACTIVE", 
            true
        );

        // 2. Tạo danh mục test
        Category cat = new Category();
        cat.setName("Cat Rules JUnit " + System.currentTimeMillis());
        cat.setSlug("cat-rules-junit-" + System.currentTimeMillis());
        cat.setDisplayOrder(15);
        cat.setIsActive(true);
        testCategoryId = categoryDAO.save(cat);

        // 3. Tạo giỏ hàng cho khách hàng
        testCartId = cartDAO.createForCustomer(testCustomerId);
    }

    @After
    public void tearDown() {
        try {
            // Xóa sạch dữ liệu test trong database
            if (testCartId != -1) {
                try (Connection conn = cartDAO.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                        ps.setInt(1, testCartId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?")) {
                        ps.setInt(1, testCartId);
                        ps.executeUpdate();
                    }
                }
            }
            if (testPackagingId != -1) {
                try (Connection conn = productDAO.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM product_packaging_options WHERE packaging_id = ?")) {
                    ps.setInt(1, testPackagingId);
                    ps.executeUpdate();
                }
            }
            if (testProductId1 != -1) {
                deleteProductHard(testProductId1);
            }
            if (testProductId2 != -1) {
                deleteProductHard(testProductId2);
            }
            if (testCategoryId != -1) {
                categoryDAO.delete(testCategoryId);
            }
            if (testOwnerId != -1) {
                userDAO.deleteUser(testOwnerId);
            }
            if (testCustomerId != -1) {
                userDAO.deleteUser(testCustomerId);
            }
        } catch (SQLException e) {
            System.err.println("Clean up failed in ProductBusinessRulesTest: " + e.getMessage());
        }
    }

    private void deleteProductHard(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM product_variants WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM product_packaging_options WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Nghiệp vụ 1: Sản phẩm tự động đổi trạng thái thành OUT_OF_SEASON sau khi hết hạn bảo quản
     * (harvest_date + shelf_life_days <= now)
     */
    @Test
    public void testSeasonalAutoDeactivation() throws SQLException {
        // Tạo sản phẩm có ngày thu hoạch cách đây 10 ngày, hạn bảo quản 5 ngày -> đã hết hạn
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Sầu Riêng Hết Hạn");
        p.setDescription("Sầu riêng đã quá hạn sử dụng");
        p.setHarvestDate(LocalDate.now().minusDays(10));
        p.setShelfLifeDays(5);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");

        testProductId1 = productDAO.save(p);

        // Gọi getProductById thông qua ProductService để kích hoạt quét tự động
        Product fetched = productService.getProductById(testProductId1);
        assertNotNull(fetched);
        assertEquals("Trạng thái sản phẩm quá hạn phải tự động chuyển thành OUT_OF_SEASON", "OUT_OF_SEASON", fetched.getStatus());
    }

    /**
     * Nghiệp vụ 2: Biến thể trọng lượng & Giá giảm hoạt động/hết hạn
     * - Khi thời gian giảm giá nằm trong khoảng hiện tại -> activePrice = discountPrice
     * - Khi đã hết hạn giảm giá -> activePrice tự động quay về basePrice ban đầu
     */
    @Test
    public void testVariantDiscountPricing() throws SQLException {
        // Tạo sản phẩm cơ bản hoạt động bình thường
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Nho Hữu Cơ Giảm Giá");
        p.setHarvestDate(LocalDate.now().plusDays(5));
        p.setShelfLifeDays(20);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        testProductId2 = productDAO.save(p);

        // 1. Biến thể có chương trình giảm giá đang hoạt động
        ProductVariant varActive = new ProductVariant();
        varActive.setProductId(testProductId2);
        varActive.setSku("NHO-ACTIVE-DISC");
        varActive.setVariantLabel("Hộp 500g");
        varActive.setPrice(new BigDecimal("200000.00"));
        varActive.setStockQuantity(50);
        varActive.setIsActive(true);
        varActive.setDiscountPrice(new BigDecimal("150000.00"));
        varActive.setDiscountStart(LocalDateTime.now().minusDays(2));
        varActive.setDiscountEnd(LocalDateTime.now().plusDays(2));
        testVariantId1 = variantDAO.save(varActive);

        // 2. Biến thể có chương trình giảm giá đã hết hạn
        ProductVariant varExpired = new ProductVariant();
        varExpired.setProductId(testProductId2);
        varExpired.setSku("NHO-EXPIRED-DISC");
        varExpired.setVariantLabel("Hộp 1kg");
        varExpired.setPrice(new BigDecimal("380000.00"));
        varExpired.setStockQuantity(30);
        varExpired.setIsActive(true);
        varExpired.setDiscountPrice(new BigDecimal("299000.00"));
        varExpired.setDiscountStart(LocalDateTime.now().minusDays(10));
        varExpired.setDiscountEnd(LocalDateTime.now().minusDays(1)); // Đã kết thúc hôm qua
        testVariantId2 = variantDAO.save(varExpired);

        // Kiểm tra biến thể 1 (Giảm giá đang chạy)
        ProductVariant fetchedActive = variantDAO.findById(testVariantId1);
        assertNotNull(fetchedActive);
        assertTrue(fetchedActive.getIsDiscounted());
        assertEquals("Giá hoạt động phải bằng giá giảm giá", new BigDecimal("150000.00"), fetchedActive.getActivePrice());

        // Kiểm tra biến thể 2 (Giảm giá đã hết hạn)
        ProductVariant fetchedExpired = variantDAO.findById(testVariantId2);
        assertNotNull(fetchedExpired);
        assertFalse(fetchedExpired.getIsDiscounted());
        assertEquals("Giá hoạt động phải tự động quay về giá cũ (base price)", new BigDecimal("380000.00"), fetchedExpired.getActivePrice());
    }

    /**
     * Nghiệp vụ 3: Đóng gói tùy chọn (Packaging Options)
     * Khách hàng chọn đóng gói kèm theo sản phẩm (Ví dụ: hộp gỗ +35k), giỏ hàng tính đúng giá tổng hợp.
     */
    @Test
    public void testPackagingOptionsAndCartSubtotal() throws SQLException {
        // Tạo sản phẩm & biến thể mẫu
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Cam Sành Hộp Quà");
        p.setHarvestDate(LocalDate.now().plusDays(5));
        p.setShelfLifeDays(15);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        testProductId2 = productDAO.save(p);

        ProductVariant variant = new ProductVariant();
        variant.setProductId(testProductId2);
        variant.setSku("CAM-HOP-QUA");
        variant.setVariantLabel("Hộp tiêu chuẩn");
        variant.setPrice(new BigDecimal("50000.00"));
        variant.setStockQuantity(100);
        variant.setIsActive(true);
        int variantId = variantDAO.save(variant);

        // Tạo đóng gói tùy chọn (Hộp gỗ cao cấp +35,000 VND)
        try (Connection conn = productDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO product_packaging_options (product_id, label, price_add, is_active) VALUES (?, ?, ?, 1)",
                 PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, testProductId2);
            ps.setString(2, "Hộp gỗ thông cao cấp");
            ps.setBigDecimal(3, new BigDecimal("35000.00"));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    testPackagingId = rs.getInt(1);
                }
            }
        }
        assertTrue(testPackagingId > 0);

        // Thêm vào giỏ hàng với lựa chọn đóng gói này (Số lượng: 2 quả/hộp)
        cartDAO.addItem(testCartId, variantId, 2, testPackagingId);

        // Lấy giỏ hàng và kiểm tra tính toán giá tiền
        List<CartItem> cartItems = cartDAO.findItems(testCartId);
        assertEquals(1, cartItems.size());
        CartItem item = cartItems.get(0);
        assertEquals("Hộp gỗ thông cao cấp", item.getPackagingLabel());
        assertEquals(new BigDecimal("35000.00"), item.getPackagingPriceAdd());
        
        // Giá gốc là 50,000 + 35,000 (đóng gói) = 85,000đ/sản phẩm. Số lượng 2 -> Tổng cộng phải là 170,000đ
        BigDecimal expectedSubtotal = new BigDecimal("85000.00").multiply(new BigDecimal(2));
        // Tính tổng tiền item bao gồm đóng gói
        BigDecimal calculatedSubtotal = item.getPrice().add(item.getPackagingPriceAdd()).multiply(new BigDecimal(item.getQuantity()));
        assertEquals("Tổng giá trị giỏ hàng của sản phẩm kèm đóng gói phải được tính chính xác", expectedSubtotal, calculatedSubtotal);
    }

    /**
     * Nghiệp vụ 4: Kiểm soát tồn kho & gửi yêu cầu nhập kho (Low Stock Alerts & Restock Request)
     */
    @Test
    public void testLowStockAlertAndRestockNotification() throws SQLException {
        // Tạo sản phẩm & biến thể có lượng tồn kho thấp (1 quả)
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Na Hoàng Đế Hết Hàng");
        p.setHarvestDate(LocalDate.now().plusDays(5));
        p.setShelfLifeDays(10);
        p.setStatus("ACTIVE");
        p.setApprovalStatus("APPROVED");
        testProductId2 = productDAO.save(p);

        ProductVariant variant = new ProductVariant();
        variant.setProductId(testProductId2);
        variant.setSku("NA-HOANG-DE");
        variant.setVariantLabel("Hộp 1 quả");
        variant.setPrice(new BigDecimal("120000.00"));
        variant.setStockQuantity(1); // Tồn kho thấp cực hạn
        variant.setIsActive(true);
        int variantId = variantDAO.save(variant);

        // 1. Kiểm tra cảnh báo tồn kho thấp (ngưỡng <= 5)
        int lowStockCount = productDAO.getLowStockCountByOwner(testOwnerId, 5);
        assertEquals("Shop phải nhận diện được 1 biến thể sắp hết hàng", 1, lowStockCount);

        List<java.util.Map<String, Object>> lowStockList = productDAO.getLowStockVariantsByOwner(testOwnerId, 5);
        assertEquals(1, lowStockList.size());
        assertEquals("NA-HOANG-DE", lowStockList.get(0).get("sku"));

        // 2. Khách hàng gửi yêu cầu nhập kho cho sản phẩm này
        assertFalse(productDAO.hasRequestedRestockToday(testOwnerId, testCustomerId, testProductId2));
        productDAO.createRestockNotification(testOwnerId, testCustomerId, testProductId2, p.getName());
        
        // Kiểm tra xem yêu cầu hôm nay đã được ghi nhận hay chưa
        assertTrue("Yêu cầu nhập kho trong ngày của khách phải được lưu vết", 
            productDAO.hasRequestedRestockToday(testOwnerId, testCustomerId, testProductId2));
    }
}
