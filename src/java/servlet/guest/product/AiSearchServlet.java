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
import java.text.Normalizer;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

@WebServlet("/api/ai/search")
public class AiSearchServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AiSearchServlet.class.getName());
    private static final int MAX_RETRIES = 2;
    private static final int INITIAL_RETRY_DELAY_MS = 750;
    private static final int MAX_FALLBACK_PRODUCTS = 6;
    private static final int MAX_AI_CONTEXT_PRODUCTS = 12;
    private static final Set<Integer> TRANSIENT_AI_STATUS_CODES = Set.of(429, 500, 502, 503, 504);

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
            // 1. Nạp catalog trước để có thể fallback ngay cả khi AI upstream lỗi.
            List<Category> activeCategories = categoryDAO.findAllActive();
            Map<Integer, String> categoryNames = new HashMap<>();
            for (Category category : activeCategories) {
                categoryNames.put(category.getCategoryId(), category.getName());
            }
            List<Product> activeProductsForAI = productDAO.findAllActiveForAI();

            // 2. Đọc câu hỏi/yêu cầu tìm kiếm sớm để có thể fallback ngay khi cần.
            String userMessage = normalizeText(req.getParameter("message"));
            if (userMessage == null) {
                byte[] bodyBytes = req.getInputStream().readAllBytes();
                String jsonInput = new String(bodyBytes, StandardCharsets.UTF_8);
                try {
                    Map<String, Object> requestData = mapper.readValue(jsonInput, Map.class);
                    userMessage = normalizeText(requestData.get("message"));
                } catch (Exception parseError) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST,
                            "Nội dung tìm kiếm không hợp lệ."));
                    return;
                }
            }

            if (userMessage == null || userMessage.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Nội dung tìm kiếm không được để trống."));
                return;
            }

            // 3. Lấy API Key từ DB, biến môi trường, hoặc AppConfig.GEMINI_API_KEY trực tiếp
            String apiKey = systemConfigDAO.getValue(AppConfig.CONFIG_GEMINI_API_KEY);
            if (apiKey == null || apiKey.trim().isEmpty() || "AIzaSyDOb1pEhCxsWfeJa1Zn5-a9TM6z-OxiqnE".equals(apiKey.trim())) {
                apiKey = System.getenv("GEMINI_API_KEY");
            }
            if (apiKey == null || apiKey.trim().isEmpty() || "AIzaSyDOb1pEhCxsWfeJa1Zn5-a9TM6z-OxiqnE".equals(apiKey.trim())) {
                apiKey = AppConfig.GEMINI_API_KEY;
            }

            if (apiKey == null || apiKey.trim().isEmpty() || "AIzaSyDOb1pEhCxsWfeJa1Zn5-a9TM6z-OxiqnE".equals(apiKey.trim())) {
                LoggerUtil.warn(log, "Gemini API key chưa được cấu hình, chuyển sang fallback nội bộ cho AI search.");
                writeFallbackResponse(req, resp, userMessage, activeCategories, activeProductsForAI);
                return;
            }

            List<Product> promptProducts = selectRelevantProducts(userMessage, activeProductsForAI, categoryNames, MAX_AI_CONTEXT_PRODUCTS);

            // Xây dựng chuỗi catalog thông tin cho AI
            StringBuilder catalogBuilder = new StringBuilder();
            catalogBuilder.append("=== DANH MỤC SẢN PHẨM PHÙ HỢP NHẤT ===\n");
            for (Category cat : activeCategories) {
                catalogBuilder.append(String.format("- Tên danh mục: %s\n", cat.getName()));
            }

            catalogBuilder.append("\n=== DANH SÁCH SẢN PHẨM KHỚP NHU CẦU ===\n");
            for (Product product : promptProducts) {
                catalogBuilder.append(String.format("- ID: %d, Tên: %s, Nguồn gốc: %s, Đã bán: %d, Đánh giá: %s, Mô tả ngắn: %s\n",
                        product.getProductId(),
                        normalizeText(product.getName()),
                        normalizeText(product.getOriginCountry()),
                        product.getSoldQuantity(),
                        product.getRating() == null ? "N/A" : product.getRating().toPlainString(),
                        truncateForPrompt(product.getDescription(), 120)
                ));
            }

            // 4. Xây dựng Prompt Hệ thống & Định nghĩa cấu trúc JSON đầu ra cho Gemini 2.5 Flash
            String systemInstruction = "Bạn là Trợ lý AI chuyên nghiệp tư vấn mua hàng và tìm kiếm nông sản sạch tại website MetaFruit.\n" +
                    "Nhiệm vụ của bạn là lắng nghe nhu cầu của khách hàng, tư vấn chọn hoa quả chín cây, tươi ngon phù hợp và gợi ý các sản phẩm phù hợp.\n" +
                    "Danh sách sản phẩm bên dưới đã được lọc theo nhu cầu hiện tại để phản hồi nhanh và chính xác hơn.\n" +
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

            // 5. Gọi API Gemini với cơ chế tự động thử lại (Retry & Fallback) phòng lỗi 503/429
            HttpResponse<String> httpResponse = null;
            String model = "gemini-2.5-flash";
            String lastErrorBody = null;

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                        .build();

                try {
                    httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LoggerUtil.warn(log, String.format(
                            "Gemini API bị gián đoạn ở lần thử %d/%d.",
                            attempt, MAX_RETRIES), ie);
                    break;
                } catch (IOException ioe) {
                    LoggerUtil.warn(log, String.format(
                            "Không gọi được Gemini API ở lần thử %d/%d.",
                            attempt, MAX_RETRIES), ioe);
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep((long) INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1)));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }
                    break;
                }

                int status = httpResponse.statusCode();
                lastErrorBody = httpResponse.body();

                if (status == 200) {
                    break;
                }

                // Nếu bị 503 Service Unavailable hoặc 429 Rate Limit thì tiến hành thử lại
                if (isTransientAiStatus(status)) {
                    LoggerUtil.warn(log, String.format(
                            "Gemini API trả về HTTP %d ở lần thử %d/%d. Đang thử lại...",
                            status, attempt, MAX_RETRIES));
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep((long) INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1)));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                }
                break;
            }

            if (httpResponse == null) {
                LoggerUtil.warn(log, String.format(
                        "Gemini API không phản hồi sau %d lần thử. Chuyển sang fallback nội bộ.",
                        MAX_RETRIES));
                writeFallbackResponse(req, resp, userMessage, activeCategories, activeProductsForAI);
                return;
            }

            int finalStatus = httpResponse.statusCode();
            if (finalStatus != 200) {
                LoggerUtil.warn(log, String.format(
                        "Gemini API trả về HTTP %d sau %d lần thử. Chuyển sang fallback nội bộ. Body=%s",
                        finalStatus, MAX_RETRIES, truncateForLog(lastErrorBody)));
                writeFallbackResponse(req, resp, userMessage, activeCategories, activeProductsForAI);
                return;
            }

            try {
                // 6. Phân tích kết quả trả về từ Gemini
                Map<String, Object> geminiResponse = mapper.readValue(httpResponse.body(), Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) geminiResponse.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    throw new IllegalStateException("AI candidates missing");
                }

                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                if (contentMap == null) {
                    throw new IllegalStateException("AI content missing");
                }

                List<Map<String, Object>> partsList = (List<Map<String, Object>>) contentMap.get("parts");
                if (partsList == null || partsList.isEmpty()) {
                    throw new IllegalStateException("AI parts missing");
                }

                String jsonText = normalizeText(partsList.get(0).get("text"));
                if (jsonText == null) {
                    throw new IllegalStateException("AI text missing");
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
            } catch (Exception aiResponseError) {
                LoggerUtil.warn(log, "Không xử lý được phản hồi Gemini, chuyển sang fallback nội bộ.", aiResponseError);
                writeFallbackResponse(req, resp, userMessage, activeCategories, activeProductsForAI);
            }

        } catch (SQLException e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "AiSearchServlet#doPost",
                    "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(),
                    e);
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "AiSearchServlet#doPost",
                    "Lỗi hệ thống khi xử lý AI: " + e.getMessage(),
                    e);
        }
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

    private boolean isTransientAiStatus(int status) {
        return TRANSIENT_AI_STATUS_CODES.contains(status);
    }

    private void writeFallbackResponse(HttpServletRequest req, HttpServletResponse resp, String userMessage,
            List<Category> activeCategories, List<Product> activeProducts) throws SQLException, IOException {
        if (activeProducts == null) {
            activeProducts = productDAO.findAllActiveForAI();
        }
        Map<Integer, String> categoryNames = new HashMap<>();
        for (Category category : activeCategories) {
            categoryNames.put(category.getCategoryId(), category.getName());
        }

        List<Product> fallbackProducts = selectFallbackProducts(userMessage, activeProducts, categoryNames);
        List<Integer> suggestedIds = new ArrayList<>();
        for (Product product : fallbackProducts) {
            suggestedIds.add(product.getProductId());
        }

        List<Map<String, Object>> productsDetails = Collections.emptyList();
        if (!suggestedIds.isEmpty()) {
            List<Map<String, Object>> briefProducts = productDAO.findBriefProductsByIds(suggestedIds);
            if (briefProducts != null && !briefProducts.isEmpty()) {
                Map<Integer, Map<String, Object>> briefById = new HashMap<>();
                for (Map<String, Object> product : briefProducts) {
                    Object idValue = product.get("productId");
                    if (idValue instanceof Number number) {
                        briefById.put(number.intValue(), product);
                    }
                }

                productsDetails = new ArrayList<>();
                for (Integer productId : suggestedIds) {
                    Map<String, Object> product = briefById.get(productId);
                    if (product != null) {
                        productsDetails.add(product);
                    }
                }
            }
        }

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("reply", buildFallbackReply(userMessage, fallbackProducts));
        responseData.put("suggestedProductIds", suggestedIds);
        responseData.put("products", productsDetails);
        responseData.put("fallback", true);
        resp.setStatus(HttpServletResponse.SC_OK);
        JsonUtil.writeJson(resp, ApiResponse.ok(responseData));
    }

    private List<Product> selectFallbackProducts(String userMessage, List<Product> activeProducts,
            Map<Integer, String> categoryNames) {
        return selectRelevantProducts(userMessage, activeProducts, categoryNames, MAX_FALLBACK_PRODUCTS);
    }

    private List<Product> selectRelevantProducts(String userMessage, List<Product> activeProducts,
            Map<Integer, String> categoryNames, int limit) {
        if (activeProducts == null || activeProducts.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        String normalizedMessage = normalizeSearchText(userMessage);
        List<String> keywords = extractSearchKeywords(normalizedMessage);
        List<ScoredProduct> scoredProducts = new ArrayList<>();

        for (Product product : activeProducts) {
            String categoryName = categoryNames.get(product.getCategoryId());
            int score = scoreFallbackProduct(product, normalizedMessage, keywords, categoryName);
            if (score > 0) {
                scoredProducts.add(new ScoredProduct(product, score));
            }
        }

        if (scoredProducts.isEmpty()) {
            List<Product> sorted = new ArrayList<>(activeProducts);
            sorted.sort((left, right) -> {
                int soldCompare = Integer.compare(right.getSoldQuantity(), left.getSoldQuantity());
                if (soldCompare != 0) {
                    return soldCompare;
                }
                return Integer.compare(right.getProductId(), left.getProductId());
            });
            return new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size())));
        }

        scoredProducts.sort((left, right) -> {
            int scoreCompare = Integer.compare(right.score(), left.score());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            int soldCompare = Integer.compare(right.product().getSoldQuantity(), left.product().getSoldQuantity());
            if (soldCompare != 0) {
                return soldCompare;
            }
            return Integer.compare(right.product().getProductId(), left.product().getProductId());
        });

        List<Product> selected = new ArrayList<>();
        for (ScoredProduct scored : scoredProducts) {
            if (selected.size() >= limit) {
                break;
            }
            selected.add(scored.product());
        }
        return selected;
    }

    private int scoreFallbackProduct(Product product, String normalizedMessage, List<String> keywords,
            String categoryName) {
        String normalizedName = normalizeSearchText(product.getName());
        String normalizedDescription = normalizeSearchText(product.getDescription());
        String normalizedOriginCountry = normalizeSearchText(product.getOriginCountry());
        String normalizedOriginRegion = normalizeSearchText(product.getOriginRegion());
        String normalizedStorageInstruction = normalizeSearchText(product.getStorageInstruction());
        String normalizedCategory = normalizeSearchText(categoryName);
        boolean importedProduct = product.getIsImported();

        String haystack = String.join(" ",
                normalizedName,
                normalizedDescription,
                normalizedOriginCountry,
                normalizedOriginRegion,
                normalizedStorageInstruction,
                normalizedCategory).trim();

        int score = 0;
        if (!normalizedMessage.isEmpty() && haystack.contains(normalizedMessage)) {
            score += 6;
        }
        if (!normalizedName.isEmpty() && !normalizedMessage.isEmpty() && normalizedName.contains(normalizedMessage)) {
            score += 4;
        }
        if (!normalizedCategory.isEmpty() && !normalizedMessage.isEmpty() && normalizedCategory.contains(normalizedMessage)) {
            score += 2;
        }

        boolean wantsImported = normalizedMessage.contains("nhap khau") || normalizedMessage.contains("import");
        if (wantsImported) {
            if (importedProduct || normalizedName.contains("nhap khau") || isImportedOrigin(normalizedOriginCountry)) {
                score += 5;
            } else if (!normalizedOriginCountry.isBlank() || importedProduct) {
                score += 1;
            }
        } else if (importedProduct) {
            score += 1;
        }

        boolean wantsVitaminC = normalizedMessage.contains("vitamin c");
        if (wantsVitaminC && containsAny(normalizedName, VITAMIN_C_HINTS)) {
            score += 4;
        } else if (wantsVitaminC && containsAny(normalizedDescription, VITAMIN_C_HINTS)) {
            score += 2;
        }

        for (String keyword : keywords) {
            if (keyword.length() < 3) {
                continue;
            }
            if (normalizedName.contains(keyword)) {
                score += 4;
            } else if (haystack.contains(keyword)) {
                score += 2;
            }
        }

        return score;
    }

    private List<String> extractSearchKeywords(String normalizedText) {
        List<String> keywords = new ArrayList<>();
        if (normalizedText == null || normalizedText.isBlank()) {
            return keywords;
        }

        for (String token : normalizedText.split(" ")) {
            if (token == null || token.isBlank() || token.length() < 3) {
                continue;
            }
            if (AI_SEARCH_STOP_WORDS.contains(token)) {
                continue;
            }
            keywords.add(token);
        }
        return keywords;
    }

    private String normalizeSearchText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buildFallbackReply(String userMessage, List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "Hiện tại mình chưa tìm thấy sản phẩm phù hợp ngay. Bạn có thể thử lại sau ít phút hoặc đổi từ khóa tìm kiếm.";
        }

        String intentLabel = deriveFallbackIntentLabel(userMessage);
        List<String> highlightNames = extractProductNames(products, 3);
        StringBuilder reply = new StringBuilder();
        reply.append("Mình đã chọn ra các sản phẩm phù hợp nhất");
        if (!intentLabel.isBlank()) {
            reply.append(" cho ").append(intentLabel);
        }
        reply.append(" trong kho hiện có.");

        if (!highlightNames.isEmpty()) {
            reply.append(" Nổi bật gồm ").append(joinNaturalLanguage(highlightNames));
            if (products.size() > highlightNames.size()) {
                reply.append(" và một số lựa chọn khác.");
            } else {
                reply.append('.');
            }
        }

        return reply.toString();
    }

    private String deriveFallbackIntentLabel(String userMessage) {
        String normalizedMessage = normalizeSearchText(userMessage);
        if (normalizedMessage.isBlank()) {
            return "";
        }

        boolean wantsImported = normalizedMessage.contains("nhap khau") || normalizedMessage.contains("import");
        boolean wantsVitaminC = normalizedMessage.contains("vitamin c");

        if (wantsImported && wantsVitaminC) {
            return "trái cây nhập khẩu giàu vitamin C";
        }
        if (wantsVitaminC) {
            return "trái cây giàu vitamin C";
        }
        if (wantsImported) {
            return "trái cây nhập khẩu";
        }
        if (normalizedMessage.contains("nguoi om")) {
            return "quà cho người ốm";
        }
        if (normalizedMessage.contains("bieu") || normalizedMessage.contains("qua tang") || normalizedMessage.contains("hop qua")) {
            return "quà biếu";
        }
        if (normalizedMessage.contains("sau rieng")) {
            return "sầu riêng";
        }
        if (normalizedMessage.contains("cam")) {
            return "cam";
        }
        if (normalizedMessage.contains("kiwi")) {
            return "kiwi";
        }
        return "";
    }

    private List<String> extractProductNames(List<Product> products, int limit) {
        List<String> names = new ArrayList<>();
        if (products == null || products.isEmpty() || limit <= 0) {
            return names;
        }

        for (Product product : products) {
            if (names.size() >= limit) {
                break;
            }
            String name = normalizeText(product.getName());
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    private String joinNaturalLanguage(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        if (values.size() == 2) {
            return values.get(0) + " và " + values.get(1);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                if (i == values.size() - 1) {
                    builder.append(" và ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private boolean containsAny(String haystack, Set<String> needles) {
        if (haystack == null || haystack.isBlank() || needles == null || needles.isEmpty()) {
            return false;
        }
        for (String needle : needles) {
            if (needle != null && !needle.isBlank() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private boolean isImportedOrigin(String normalizedOriginCountry) {
        if (normalizedOriginCountry == null || normalizedOriginCountry.isBlank()) {
            return false;
        }
        return !normalizedOriginCountry.contains("viet nam")
                && !normalizedOriginCountry.contains("vietnam");
    }

    private String truncateForPrompt(String text, int maxLength) {
        String value = normalizeText(text);
        if (value == null) {
            return "";
        }
        if (maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength).trim() + "...";
    }

    private String truncateForLog(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = text.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (sanitized.length() <= 300) {
            return sanitized;
        }
        return sanitized.substring(0, 300) + "...";
    }

    private record ScoredProduct(Product product, int score) {}

    private static final Set<String> AI_SEARCH_STOP_WORDS = Set.of(
            "cho",
            "voi",
            "cua",
            "mot",
            "nhung",
            "nay",
            "ban",
            "toi",
            "minh",
            "tu",
            "den",
            "la",
            "va",
            "the",
            "thi",
            "duoc",
            "khong");

    private static final Set<String> VITAMIN_C_HINTS = Set.of(
            "cam",
            "quyt",
            "buoi",
            "kiwi",
            "dau tay",
            "oi",
            "chanh",
            "cherry",
            "dua luoi");
}
