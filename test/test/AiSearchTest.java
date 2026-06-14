package test;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.system.SystemConfigDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.response.ApiResponse;
import util.JsonUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * AiSearchTest - JUnit coverage for AI search data and response contracts.
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
        System.out.println(
                "Key from Database (system_config): ["
                        + (dbKey == null ? "NULL"
                        : (dbKey.isEmpty() ? "EMPTY"
                        : (dbKey.length() > 4 ? dbKey.substring(0, 4) + "..." : "SHORT")))
                        + "]");
        System.out.println(
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

        // The getter path should not crash even when the key is missing.
        assertNotNull(
                "He thong phai co co che lay API Key (co the null/trong neu chua cau hinh nhung ham doc khong duoc loi)",
                systemConfigDAO);
    }

    @Test
    public void testProductContextRetrieval() throws SQLException {
        // Verify the DAO can load active products for AI context.
        List<Product> aiProducts = productDAO.findAllActiveForAI();
        assertNotNull("Danh sach san pham lam Context cho AI khong duoc null", aiProducts);

        if (!aiProducts.isEmpty()) {
            Product p = aiProducts.get(0);
            assertTrue("ID san pham phai lon hon 0", p.getProductId() > 0);
            assertNotNull("Ten san pham khong duoc null", p.getName());
            assertNotNull("Mo ta san pham khong duoc null", p.getDescription());
        }
    }

    @Test
    public void testBriefProductsByIds() throws SQLException {
        List<Product> aiProducts = productDAO.findAllActiveForAI();
        if (!aiProducts.isEmpty()) {
            List<Integer> ids = new ArrayList<>();
            ids.add(aiProducts.get(0).getProductId());
            if (aiProducts.size() > 1) {
                ids.add(aiProducts.get(1).getProductId());
            }

            List<Map<String, Object>> briefProducts = productDAO.findBriefProductsByIds(ids);
            assertNotNull("Danh sach chi tiet goi y khong duoc null", briefProducts);
            assertFalse("Danh sach chi tiet goi y khong duoc rong khi truyen ID hop le", briefProducts.isEmpty());

            Map<String, Object> map = briefProducts.get(0);
            assertTrue("Phai chua productId", map.containsKey("productId"));
            assertTrue("Phai chua name", map.containsKey("name"));
            assertTrue("Phai chua price", map.containsKey("price"));
            assertTrue("Phai chua unit", map.containsKey("unit"));
            assertTrue("Phai chua image", map.containsKey("image"));
        }
    }

    @Test
    public void testApiResponseFailUsesErrorField() throws Exception {
        String json = JsonUtil.toJson(ApiResponse.fail(400, "Nội dung tìm kiếm không hợp lệ."));
        assertTrue("ApiResponse that bai phai co success=false", json.contains("\"success\":false"));
        assertTrue("ApiResponse that bai phai dung field error", json.contains("\"error\":\"Nội dung tìm kiếm không hợp lệ.\""));
        assertFalse("ApiResponse that bai khong duoc dung field message", json.contains("\"message\""));
    }

    @Test
    public void testApiResponseOkWrapsAiPayloadInDataEnvelope() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reply", "Cam sành ngọt thanh");
        payload.put("suggestedProductIds", List.of(1, 2));
        payload.put("products", List.of(Map.of("productId", 1)));

        String json = JsonUtil.toJson(ApiResponse.ok(payload));
        assertTrue("ApiResponse thanh cong phai co success=true", json.contains("\"success\":true"));
        assertTrue("ApiResponse thanh cong phai boc payload trong data", json.contains("\"data\":"));
        assertTrue("Payload reply phai con nguyen trong data", json.contains("\"reply\":\"Cam sành ngọt thanh\""));
        assertTrue("Payload suggestedProductIds phai con nguyen trong data", json.contains("\"suggestedProductIds\":[1,2]"));
    }

    @Test
    public void testCategoryRetrieval() throws SQLException {
        List<Category> activeCategories = categoryDAO.findAllActive();
        assertNotNull("Danh sach danh muc khong duoc null", activeCategories);
    }
}
