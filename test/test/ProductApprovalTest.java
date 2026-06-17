package test;

import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.auth.UserDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.auth.User;
import service.catalog.CategoryService;
import service.catalog.ProductService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ProductApprovalTest — Bộ kiểm thử JUnit 4 cho luồng duyệt sản phẩm thông qua Service Layer.
 */
public class ProductApprovalTest {

    private ProductService productService;
    private CategoryService categoryService;
    
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private UserDAO userDAO;

    private int testOwnerId = -1;
    private int testCategoryId = -1;
    private int testProductId = -1;
    private int overriddenCategoryId = -1;

    @Before
    public void setUp() throws SQLException {
        productService = new ProductService();
        categoryService = new CategoryService();
        
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        userDAO = new UserDAO();

        // 1. Tạo một tài khoản Shop Owner giả lập để tạo sản phẩm
        testOwnerId = userDAO.saveNewCustomer(
            "Test Shop Owner", 
            "test_shop_owner_junit_" + System.currentTimeMillis() + "@test.com", 
            "hashed_pwd", 
            "0987654321", 
            "SHOP_OWNER", 
            "ACTIVE", 
            true
        );

        // 2. Tạo một danh mục giả lập ban đầu
        Category cat = new Category();
        cat.setName("Test Cat JUnit " + System.currentTimeMillis());
        cat.setSlug("test-cat-junit-" + System.currentTimeMillis());
        cat.setDisplayOrder(10);
        cat.setIsActive(true);
        testCategoryId = categoryService.createCategory(cat);
    }

    @After
    public void tearDown() {
        // Thực hiện hard delete dữ liệu test để giữ sạch sẽ cơ sở dữ liệu
        try {
            if (testProductId != -1) {
                deleteProductHard(testProductId);
                testProductId = -1;
            }
            if (testCategoryId != -1) {
                categoryDAO.delete(testCategoryId);
                testCategoryId = -1;
            }
            if (overriddenCategoryId != -1) {
                categoryDAO.delete(overriddenCategoryId);
                overriddenCategoryId = -1;
            }
            if (testOwnerId != -1) {
                userDAO.deleteUser(testOwnerId);
                testOwnerId = -1;
            }
        } catch (SQLException e) {
            System.err.println("Clean up failed: " + e.getMessage());
        }
    }

    /** Helper để hard delete sản phẩm test và các biến thể của nó */
    private void deleteProductHard(int productId) throws SQLException {
        try (Connection conn = productDAO.getConnection()) {
            // Xóa biến thể trước do ràng buộc khóa ngoại
            String sqlVar = "DELETE FROM product_variants WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVar)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            // Xóa sản phẩm
            String sqlProd = "DELETE FROM products WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlProd)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
        }
    }

    @Test
    public void testProductApprovalFlow() throws SQLException {
        // 1. Tạo sản phẩm mới dưới danh nghĩa Shop Owner (mặc định trạng thái PENDING)
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Junit Mango");
        p.setDescription("Tươi ngon từ vườn");
        p.setOriginCountry("Việt Nam");
        p.setOriginRegion("Hòa Lộc");
        p.setIsOrganic(false);
        p.setIsImported(false);
        p.setApprovalStatus("PENDING");
        p.setVerificationDocPath("uploads/shop-docs/1/certificate.pdf");

        // Gọi qua Service
        testProductId = productService.createProduct(p);
        assertTrue("Mã sản phẩm tự sinh ra phải lớn hơn 0", testProductId > 0);

        // Kiểm tra xem sản phẩm đã lưu đúng trạng thái PENDING và đường dẫn tài liệu xác minh
        Product fetched = productService.getProductById(testProductId);
        assertNotNull(fetched);
        assertEquals("PENDING", fetched.getApprovalStatus());
        assertEquals("uploads/shop-docs/1/certificate.pdf", fetched.getVerificationDocPath());

        // 2. Admin phê duyệt sản phẩm, đổi mác hữu cơ/nhập khẩu và chuyển danh mục
        Category newCat = new Category();
        newCat.setName("Test Overridden Cat " + System.currentTimeMillis());
        newCat.setSlug("test-overridden-cat-" + System.currentTimeMillis());
        newCat.setDisplayOrder(20);
        newCat.setIsActive(true);
        overriddenCategoryId = categoryService.createCategory(newCat);

        // Phê duyệt qua ProductService
        boolean approved = productService.approveProduct(
            testProductId, 
            true, // Organic = true
            true, // Imported = true
            overriddenCategoryId
        );
        assertTrue("Trạng thái phê duyệt sản phẩm cập nhật thành công", approved);

        // Đọc lại và đối chiếu kết quả phê duyệt
        Product approvedProduct = productService.getProductById(testProductId);
        assertNotNull(approvedProduct);
        assertEquals("APPROVED", approvedProduct.getApprovalStatus());
        assertNull(approvedProduct.getRejectionReason());
        assertTrue("Sản phẩm phải được gắn mác hữu cơ", approvedProduct.getIsOrganic());
        assertTrue("Sản phẩm phải được gắn mác nhập khẩu", approvedProduct.getIsImported());
        assertEquals(overriddenCategoryId, approvedProduct.getCategoryId());

        // 3. Admin gỡ bỏ/bấm ban sản phẩm vi phạm
        boolean banned = productService.banProduct(testProductId);
        assertTrue("Hành động ban sản phẩm vi phạm thành công", banned);

        Product bannedProduct = productService.getProductById(testProductId);
        assertNotNull(bannedProduct);
        assertEquals("DELETED", bannedProduct.getStatus());
    }

    @Test
    public void testProductRejectionFlow() throws SQLException {
        // Tạo sản phẩm
        Product p = new Product();
        p.setOwnerId(testOwnerId);
        p.setCategoryId(testCategoryId);
        p.setName("Junit Starfruit");
        p.setDescription("Khế ngọt");
        p.setOriginCountry("Việt Nam");
        p.setOriginRegion("Bến Tre");
        p.setIsOrganic(false);
        p.setIsImported(false);
        p.setApprovalStatus("PENDING");
        p.setVerificationDocPath("uploads/shop-docs/1/cert.docx");

        testProductId = productService.createProduct(p);

        // Admin từ chối phê duyệt kèm lý do qua Service
        String reason = "Giấy chứng nhận không hợp lệ.";
        boolean rejected = productService.rejectProduct(testProductId, reason);
        assertTrue("Từ chối phê duyệt sản phẩm thành công", rejected);

        // Đọc lại để xác nhận trạng thái REJECTED và lý do từ chối
        Product rejectedProduct = productService.getProductById(testProductId);
        assertNotNull(rejectedProduct);
        assertEquals("REJECTED", rejectedProduct.getApprovalStatus());
        assertEquals(reason, rejectedProduct.getRejectionReason());
    }
}
