package test;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.system.SystemConfigDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.response.ApiResponse;
import service.chat.AiSearchService;
import servlet.guest.product.AiSearchServlet;
import util.JsonUtil;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
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

        assertTrue("Gemini API key phai duoc cau hinh trong AppConfig",
                AppConfig.GEMINI_API_KEY != null && !AppConfig.GEMINI_API_KEY.trim().isEmpty());
        assertTrue("Gemini API key phai duoc seed trong database",
                dbKey != null && !dbKey.trim().isEmpty());

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
    @SuppressWarnings("unchecked")
    public void testUpstreamErrorResponseUsesErrorContract() throws Exception {
        AiSearchService service = new AiSearchService();
        Method method = AiSearchService.class.getDeclaredMethod(
                "buildUpstreamErrorResponse",
                long.class,
                String.class,
                String.class,
                Integer.class,
                String.class);
        method.setAccessible(true);

        Map<String, Object> response = (Map<String, Object>) method.invoke(
                service,
                System.currentTimeMillis() - 25L,
                "http_403",
                "Gemini từ chối yêu cầu với mã HTTP 403: API key bị thu hồi.",
                403,
                "{\"error\":{\"message\":\"API key bị thu hồi.\"}}");

        assertNotNull("Payload loi upstream phai ton tai", response);
        assertEquals("upstream_error", response.get("source"));
        assertFalse("Payload loi upstream khong duoc danh la fallback", Boolean.TRUE.equals(response.get("fallback")));
        assertEquals("http_403", response.get("errorCode"));
        assertTrue("Payload loi phai giu nguyen thong diep loi", String.valueOf(response.get("errorMessage")).contains("API key bị thu hồi"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyGeminiPayloadDoesNotFallback() throws Exception {
        AiSearchService service = new AiSearchService();
        Method method = AiSearchService.class.getDeclaredMethod(
                "buildGeminiResponse",
                String.class,
                long.class);
        method.setAccessible(true);

        Map<String, Object> response = (Map<String, Object>) method.invoke(
                service,
                "{\"candidates\":[]}",
                System.currentTimeMillis() - 25L);

        assertNotNull("Payload loai nay phai tra ve response", response);
        assertEquals("upstream_error", response.get("source"));
        assertFalse("Payload loai nay khong duoc fallback", Boolean.TRUE.equals(response.get("fallback")));
        assertEquals("empty_candidates", response.get("errorCode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFallbackReplyIsNaturalAndProductFocused() throws Exception {
        AiSearchServlet servlet = new AiSearchServlet();
        Method method = AiSearchServlet.class.getDeclaredMethod("buildFallbackReply", String.class, List.class);
        method.setAccessible(true);

        Product first = new Product();
        first.setName("Táo Envy Mỹ Nhập Khẩu Premium");
        Product second = new Product();
        second.setName("Kiwi Vàng New Zealand Zespri");

        String reply = (String) method.invoke(servlet, "Hoa quả nào nhập khẩu giàu vitamin C", List.of(first, second));

        assertFalse("Fallback reply khong duoc nhac den su co ky thuat", reply.contains("sự cố"));
        assertTrue("Fallback reply phai goi y theo nhom nhu cau", reply.contains("trái cây nhập khẩu giàu vitamin C") || reply.contains("trái cây giàu vitamin C"));
        assertTrue("Fallback reply phai nhan manh san pham cu the", reply.contains("Táo Envy") || reply.contains("Kiwi Vàng"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRelevantSelectionPrioritizesImportedVitaminCFruits() throws Exception {
        AiSearchServlet servlet = new AiSearchServlet();
        Method method = AiSearchServlet.class.getDeclaredMethod("selectRelevantProducts", String.class, List.class, Map.class, int.class);
        method.setAccessible(true);

        Product domestic = new Product();
        domestic.setProductId(1);
        domestic.setCategoryId(1);
        domestic.setName("Cam Sành Cao Phong Hòa Bình");
        domestic.setOriginCountry("Việt Nam");
        domestic.setDescription("Cam tươi ngọt thanh");
        domestic.setSoldQuantity(100);

        Product imported = new Product();
        imported.setProductId(2);
        imported.setCategoryId(2);
        imported.setName("Táo Envy Mỹ Nhập Khẩu Premium");
        imported.setOriginCountry("Mỹ");
        imported.setDescription("Táo nhập khẩu giàu vitamin C");
        imported.setSoldQuantity(20);

        Product imported2 = new Product();
        imported2.setProductId(3);
        imported2.setCategoryId(2);
        imported2.setName("Kiwi Vàng New Zealand Zespri");
        imported2.setOriginCountry("New Zealand");
        imported2.setDescription("Kiwi nhập khẩu giàu vitamin C");
        imported2.setSoldQuantity(10);

        Map<Integer, String> categoryNames = new LinkedHashMap<>();
        categoryNames.put(1, "Trái cây trong nước");
        categoryNames.put(2, "Trái cây nhập khẩu");

        List<Product> selected = (List<Product>) method.invoke(
                servlet,
                "Hoa quả nào nhập khẩu giàu vitamin C",
                List.of(domestic, imported, imported2),
                categoryNames,
                2);

        assertFalse("Danh sach goi y fallback khong duoc rong", selected.isEmpty());
        assertTrue("San pham dau tien phai la san pham nhap khau phu hop", !selected.get(0).getOriginCountry().toLowerCase().contains("viet"));
    }

    @Test
    public void testCategoryRetrieval() throws SQLException {
        List<Category> activeCategories = categoryDAO.findAllActive();
        assertNotNull("Danh sach danh muc khong duoc null", activeCategories);
    }
}
