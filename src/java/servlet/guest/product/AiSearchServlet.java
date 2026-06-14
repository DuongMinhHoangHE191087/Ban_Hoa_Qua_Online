package servlet.guest.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.system.SystemConfigDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.response.ApiResponse;
import util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import util.LoggerUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/api/ai/search")
public class AiSearchServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AiSearchServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Lấy API Key từ DB, biến môi trường, hoặc file .env trực tiếp
            String apiKey = systemConfigDAO.getValue(AppConfig.CONFIG_GEMINI_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                apiKey = System.getenv("GEMINI_API_KEY");
            }
            if (apiKey == null || apiKey.trim().isEmpty()) {
                apiKey = getApiKeyFromDotEnv();
            }

            if (apiKey == null || apiKey.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST,
                    "API Key chưa được cấu hình. Vui lòng liên hệ Admin thiết lập gemini_api_key trong Cấu hình Hệ thống hoặc đặt biến môi trường GEMINI_API_KEY."));
                return;
            }

            // 2. Đọc câu hỏi/yêu cầu tìm kiếm của người dùng
            byte[] bodyBytes = req.getInputStream().readAllBytes();
            String jsonInput = new String(bodyBytes, StandardCharsets.UTF_8);
            Map<String, Object> requestData;
            try {
                requestData = mapper.readValue(jsonInput, Map.class);
            } catch (Exception parseError) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST,
                    "Nội dung tìm kiếm không hợp lệ."));
                return;
            }
            String userMessage = normalizeText(requestData.get("message"));

            if (userMessage == null || userMessage.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Nội dung tìm kiếm không được để trống."));
                return;
            }

            // 3. Tải danh mục và sản phẩm đang có sẵn làm Context cho AI
            List<Category> activeCategories = categoryDAO.findAllActive();
            List<Map<String, Object>> activeProductsBrief = productDAO.findAllActiveBriefForAI();

            // Xây dựng chuỗi catalog thông tin cho AI
            StringBuilder catalogBuilder = new StringBuilder();
            catalogBuilder.append("=== DANH MỤC SẢN PHẨM ===\n");
            for (Category cat : activeCategories) {
                catalogBuilder.append(String.format("- Tên danh mục: %s\n", cat.getName()));
            }

            catalogBuilder.append("\n=== DANH SÁCH HOA QUẢ ĐANG CÓ TRONG KHO ===\n");
            for (Map<String, Object> p : activeProductsBrief) {
                catalogBuilder.append(String.format("- ID: %d, Tên: %s, Tồn kho: %d, Trạng thái: %s\n",
                        p.get("productId"),
                        p.get("name"),
                        p.get("stock"),
                        p.get("status")
                ));
            }

            // 4. Xây dựng Prompt Hệ thống & Định nghĩa cấu trúc JSON đầu ra cho Gemini 2.5 Flash
            String systemInstruction = "Bạn là Trợ lý AI chuyên nghiệp tư vấn mua hàng và tìm kiếm nông sản sạch tại website MetaFruit.\n" +
                    "Nhiệm vụ của bạn là lắng nghe nhu cầu của khách hàng, tư vấn chọn hoa quả chín cây, tươi ngon phù hợp và gợi ý các sản phẩm phù hợp.\n" +
                    "=== QUY TẮC BẢO MẬT & ĐẦU RA (QUAN TRỌNG) ===\n" +
                    "1. Tuyệt đối KHÔNG ĐƯỢC nhắc đến hoặc hiển thị bất kỳ ID sản phẩm nào (ví dụ: 'ID: 1', 'mã số 5') trong nội dung câu trả lời văn bản ('reply') gửi cho khách hàng. Hãy trả lời bằng văn phong tự nhiên, chỉ dùng tên sản phẩm.\n" +
                    "2. Chỉ trả về ID sản phẩm trong trường 'suggestedProductIds' dưới dạng mảng số nguyên. Mảng này được dùng ngầm để thêm sản phẩm vào giỏ hàng và lọc danh mục hiển thị trên giao diện.\n" +
                    "3. Gợi ý sản phẩm PHẢI lấy từ danh sách thực tế có sẵn dưới đây. Tuyệt đối không tự bịa ra sản phẩm hoặc ID sản phẩm không có trong danh sách.\n" +
                    "4. Chỉ tư vấn về hoa quả, thực phẩm sạch, cách bảo quản và chế biến. Lịch sự từ chối các câu hỏi ngoài lề (lập trình, toán học, v.v.).\n" +
                    "5. Khi giới thiệu các sản phẩm gợi ý trong phần trả lời ('reply'), hãy trình bày thật bắt mắt: viết TÊN SẢN PHẨM IN ĐẬM ở một dòng riêng biệt, xuống dòng và viết giá tiền định dạng VNĐ sinh động (Ví dụ: '**Cam Sành Hàm Yên**\\n*Giá bán: 35.000đ / kg*') để tăng tính thuyết phục và tối ưu hiệu quả bán hàng.\n\n" +
                    catalogBuilder.toString();

            // Payload gọi Gemini API
            Map<String, Object> payload = new HashMap<>();
            
            // System instruction
            Map<String, Object> systemInstructionMap = new HashMap<>();
            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("text", systemInstruction);
            systemInstructionMap.put("parts", Collections.singletonList(partsMap));
            payload.put("systemInstruction", systemInstructionMap);

            // Contents
            Map<String, Object> userPart = new HashMap<>();
            userPart.put("text", userMessage);
            Map<String, Object> userContent = new HashMap<>();
            userContent.put("role", "user");
            userContent.put("parts", Collections.singletonList(userPart));
            payload.put("contents", Collections.singletonList(userContent));

            // Generation Config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");

            // Response Schema definition
            Map<String, Object> responseSchema = new HashMap<>();
            responseSchema.put("type", "OBJECT");

            Map<String, Object> properties = new HashMap<>();
            
            Map<String, Object> replyProp = new HashMap<>();
            replyProp.put("type", "STRING");
            replyProp.put("description", "Lời tư vấn, giải đáp thân thiện bằng tiếng Việt.");
            properties.put("reply", replyProp);

            Map<String, Object> suggestedProductIdsProp = new HashMap<>();
            suggestedProductIdsProp.put("type", "ARRAY");
            Map<String, Object> itemsType = new HashMap<>();
            itemsType.put("type", "INTEGER");
            suggestedProductIdsProp.put("items", itemsType);
            suggestedProductIdsProp.put("description", "Mảng các ID sản phẩm được gợi ý phù hợp với nhu cầu.");
            properties.put("suggestedProductIds", suggestedProductIdsProp);

            responseSchema.put("properties", properties);
            responseSchema.put("required", Arrays.asList("reply", "suggestedProductIds"));
            generationConfig.put("responseSchema", responseSchema);
            payload.put("generationConfig", generationConfig);

            String requestBody = mapper.writeValueAsString(payload);

            // 5. Gọi API Gemini 2.5 Flash
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi khi kết nối với dịch vụ AI: HTTP " + httpResponse.statusCode()));
                return;
            }

            // 6. Phân tích kết quả trả về từ Gemini
            Map<String, Object> geminiResponse = mapper.readValue(httpResponse.body(), Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) geminiResponse.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không nhận được phản hồi hợp lệ từ AI."));
                return;
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
            if (contentMap == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Phản hồi AI không đúng định dạng."));
                return;
            }

            List<Map<String, Object>> partsList = (List<Map<String, Object>>) contentMap.get("parts");
            if (partsList == null || partsList.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Phản hồi AI trống."));
                return;
            }

            String jsonText = normalizeText(partsList.get(0).get("text"));
            if (jsonText == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Phản hồi AI không có nội dung."));
                return;
            }
            Map<String, Object> aiResult = mapper.readValue(jsonText, Map.class);

            String reply = normalizeReply(aiResult.get("reply"));
            List<Integer> suggestedIds = extractSuggestedProductIds(aiResult.get("suggestedProductIds"));
            List<Map<String, Object>> productsDetails = new ArrayList<>();
            if (suggestedIds != null && !suggestedIds.isEmpty()) {
                List<Map<String, Object>> briefProducts = productDAO.findBriefProductsByIds(suggestedIds);
                if (briefProducts != null) {
                    productsDetails = new ArrayList<>(briefProducts);
                }
            }

            // Fix: chỉ giữ lại ID của những sản phẩm thực sự ACTIVE+APPROVED
            // Tránh trường hợp Gemini gợi ý sản phẩm không có thực hoặc không hợp lệ
            List<Integer> validIds = new ArrayList<>();
            for (Map<String, Object> pd : productsDetails) {
                Object pid = pd.get("productId");
                if (pid instanceof Number number) {
                    validIds.add(number.intValue());
                }
            }
            // Giới hạn tối đa 6 gợi ý hiển thị trong widget
            if (validIds.size() > 6) {
                validIds = new ArrayList<>(validIds.subList(0, 6));
                productsDetails = new ArrayList<>(productsDetails.subList(0, 6));
            }

            // Gửi kết quả về cho frontend (đảm bảo cấu trúc reply + suggestedProductIds + products không đổi)
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("reply", reply);
            responseData.put("suggestedProductIds", validIds);
            responseData.put("products", productsDetails);
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(responseData));

        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi kết nối cơ sở dữ liệu khi xử lý AI search", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage()));
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi hệ thống khi xử lý AI search", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Lỗi hệ thống khi xử lý AI: " + e.getMessage()));
        }
    }

    private String getApiKeyFromDotEnv() {
        String[] paths = {
            ".env",
            "../.env",
            "../../.env",
            System.getProperty("user.dir") + "/.env",
            System.getProperty("user.dir") + "/../.env"
        };
        for (String p : paths) {
            java.io.File file = new java.io.File(p);
            if (file.exists() && file.isFile()) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2 && "GEMINI_API_KEY".equals(parts[0].trim())) {
                            String val = parts[1].trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            return val;
                        }
                    }
                } catch (Exception e) {
                    // Tiếp tục thử đường dẫn tiếp theo
                }
            }
        }
        // Thử tìm thêm bằng Real Path từ ServletContext nếu có
        try {
            String webappPath = getServletContext().getRealPath("/");
            if (webappPath != null) {
                String[] contextPaths = {
                    webappPath + "/.env",
                    webappPath + "/../.env",
                    webappPath + "/../../.env",
                    webappPath + "/../../../.env"
                };
                for (String p : contextPaths) {
                    java.io.File file = new java.io.File(p);
                    if (file.exists() && file.isFile()) {
                        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.isEmpty() || line.startsWith("#")) continue;
                                String[] parts = line.split("=", 2);
                                if (parts.length == 2 && "GEMINI_API_KEY".equals(parts[0].trim())) {
                                    String val = parts[1].trim();
                                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                        val = val.substring(1, val.length() - 1);
                                    }
                                    return val;
                                }
                            }
                        } catch (Exception e) {
                            // Bỏ qua
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Bỏ qua nếu có lỗi ServletContext
        }
        return null;
    }

    private String normalizeText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeReply(Object value) {
        String reply = normalizeText(value);
        if (reply != null) {
            return reply;
        }
        return "Mình chưa nhận được câu trả lời hợp lệ từ AI. Vui lòng thử lại.";
    }

    private List<Integer> extractSuggestedProductIds(Object rawIds) {
        List<Integer> ids = new ArrayList<>();
        if (!(rawIds instanceof List<?> rawList)) {
            return ids;
        }
        for (Object rawId : rawList) {
            if (rawId instanceof Number number) {
                ids.add(number.intValue());
                continue;
            }
            if (rawId instanceof String text) {
                try {
                    ids.add(Integer.parseInt(text.trim()));
                } catch (NumberFormatException ignored) {
                    // Bỏ qua giá trị không hợp lệ.
                }
            }
        }
        return ids;
    }
}
