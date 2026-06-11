package com.fruitmkt.test;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AiSearchTest — Bộ kiểm thử JUnit 4 cho chức năng AI Search & Gợi ý sản phẩm.
 */
public class AiSearchTest {

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private SystemConfigDAO systemConfigDAO;

    @Before
    public void setUp() {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        systemConfigDAO = new SystemConfigDAO();
    }

    @Test
    public void testApiKeyRetrieval() throws SQLException {
        String dbKey = systemConfigDAO.getValue(AppConfig.CONFIG_GEMINI_API_KEY);
        String envKey = System.getenv("GEMINI_API_KEY");
        System.out.println("=== DIAGNOSING GEMINI API KEY ===");
        System.out
                .println(
                        "Key from Database (system_config): ["
                                + (dbKey == null ? "NULL"
                                        : (dbKey.isEmpty() ? "EMPTY"
                                                : (dbKey.length() > 4 ? dbKey.substring(0, 4) + "..." : "SHORT")))
                                + "]");
        System.out
                .println(
                        "Key from Environment Variable: ["
                                + (envKey == null ? "NULL"
                                        : (envKey.isEmpty() ? "EMPTY"
                                                : (envKey.length() > 4 ? envKey.substring(0, 4) + "..." : "SHORT")))
                                + "]");
        System.out.println("=================================");

        String apiKey = dbKey;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = envKey;
        }

        // Test không lỗi crash khi đọc key
        assertNotNull(
                "Hệ thống phải có cơ chế lấy API Key (có thể null/trống nếu chưa cấu hình nhưng hàm đọc không được lỗi)",
                systemConfigDAO);
    }

    @Test
    public void testProductContextRetrieval() throws SQLException {
        // Kiểm tra xem DAO có thể truy xuất danh sách sản phẩm làm context cho AI không
        List<Product> aiProducts = productDAO.findAllActiveForAI();
        assertNotNull("Danh sách sản phẩm làm Context cho AI không được null", aiProducts);

        // Nếu có sản phẩm trong DB, kiểm tra xem các thông tin cơ bản có được nạp đầy
        // đủ không
        if (!aiProducts.isEmpty()) {
            Product p = aiProducts.get(0);
            assertTrue("ID sản phẩm phải lớn hơn 0", p.getProductId() > 0);
            assertNotNull("Tên sản phẩm không được null", p.getName());
            assertNotNull("Mô tả sản phẩm không được null", p.getDescription());
        }
    }

    @Test
    public void testBriefProductsByIds() throws SQLException {
        // Lấy danh sách sản phẩm active
        List<Product> aiProducts = productDAO.findAllActiveForAI();
        if (!aiProducts.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            // Lấy tối đa 2 ID để test
            ids.add(aiProducts.get(0).getProductId());
            if (aiProducts.size() > 1) {
                ids.add(aiProducts.get(1).getProductId());
            }

            // Gọi DAO lấy thông tin ngắn gọn
            List<Map<String, Object>> briefProducts = productDAO.findBriefProductsByIds(ids);
            assertNotNull("Danh sách chi tiết gợi ý không được null", briefProducts);
            assertFalse("Danh sách chi tiết gợi ý không được rỗng khi truyền ID hợp lệ", briefProducts.isEmpty());

            Map<String, Object> map = briefProducts.get(0);
            assertTrue("Phải chứa productId", map.containsKey("productId"));
            assertTrue("Phải chứa name", map.containsKey("name"));
            assertTrue("Phải chứa price", map.containsKey("price"));
            assertTrue("Phải chứa unit", map.containsKey("unit"));
            assertTrue("Phải chứa image", map.containsKey("image"));
        }
    }

    @Test
    public void testCategoryRetrieval() throws SQLException {
        // Kiểm tra xem danh mục hoạt động có được tải thành công
        List<Category> activeCategories = categoryDAO.findAllActive();
        assertNotNull("Danh sách danh mục không được null", activeCategories);
    }
}
