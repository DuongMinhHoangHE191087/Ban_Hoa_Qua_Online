package servlet.guest.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.entity.catalog.Product;
import model.response.ApiResponse;
import service.chat.AiSearchService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@WebServlet("/api/ai/search")
public class AiSearchServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AiSearchServlet.class.getName());

    private static final int MAX_FALLBACK_PRODUCTS = 6;
    private static final Set<String> AI_SEARCH_STOP_WORDS = Set.of(
            "cho", "voi", "cua", "mot", "nhung", "nay", "ban", "toi", "minh", "tu",
            "den", "la", "va", "the", "thi", "duoc", "khong");
    private static final Set<String> VITAMIN_C_HINTS = Set.of(
            "cam", "quyt", "buoi", "kiwi", "dau tay", "oi", "chanh", "cherry", "dua luoi");

    private final ObjectMapper mapper = new ObjectMapper();
    private final AiSearchService aiSearchService = new AiSearchService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        boolean streamingRequested = isStreamingRequest(req);
        if (streamingRequested) {
            resp.setContentType("text/event-stream;charset=UTF-8");
            resp.setHeader("Cache-Control", "no-cache, no-transform");
            resp.setHeader("Connection", "keep-alive");
            resp.setHeader("X-Accel-Buffering", "no");
        } else {
            resp.setContentType("application/json;charset=UTF-8");
        }

        PrintWriter streamWriter = null;
        try {
            String userMessage = normalizeText(req.getParameter("message"));
            if (userMessage == null) {
                byte[] bodyBytes = req.getInputStream().readAllBytes();
                String jsonInput = new String(bodyBytes, StandardCharsets.UTF_8);
                if (jsonInput != null && !jsonInput.isBlank()) {
                    try {
                        Map<String, Object> requestData = mapper.readValue(jsonInput, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        userMessage = normalizeText(requestData.get("message"));
                    } catch (Exception parseError) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        JsonUtil.writeJson(resp, ApiResponse.fail(
                                HttpServletResponse.SC_BAD_REQUEST,
                                "Nội dung tìm kiếm không hợp lệ."));
                        return;
                    }
                }
            }

            if (userMessage == null || userMessage.isBlank()) {
                if (streamingRequested) {
                    streamWriter = ensureWriter(resp, streamWriter);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    writeSseEvent(streamWriter, "error", Map.of(
                            "message", "Nội dung tìm kiếm không được để trống."));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, ApiResponse.fail(
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Nội dung tìm kiếm không được để trống."));
                }
                return;
            }

            if (streamingRequested) {
                streamWriter = ensureWriter(resp, streamWriter);
                final PrintWriter finalStreamWriter = streamWriter;
                resp.setStatus(HttpServletResponse.SC_OK);
                Map<String, Object> responseData = aiSearchService.streamSearch(
                        userMessage,
                        delta -> writeSseEvent(finalStreamWriter, "delta", eventPayload("delta", delta)));
                req.setAttribute("requestMetricsFallback", Boolean.TRUE.equals(responseData.get("fallback")));
                Object products = responseData.get("products");
                if (products instanceof List<?> list) {
                    req.setAttribute("requestMetricsItemCount", list.size());
                }

                if ("upstream_error".equals(responseData.get("source"))) {
                    writeSseEvent(streamWriter, "error", responseData);
                } else {
                    writeSseEvent(streamWriter, "done", responseData);
                }
                return;
            }

            Map<String, Object> responseData = aiSearchService.search(userMessage);
            req.setAttribute("requestMetricsFallback", Boolean.TRUE.equals(responseData.get("fallback")));
            Object products = responseData.get("products");
            if (products instanceof List<?> list) {
                req.setAttribute("requestMetricsItemCount", list.size());
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(responseData));
        } catch (IllegalArgumentException e) {
            if (streamingRequested) {
                try {
                    streamWriter = ensureWriter(resp, streamWriter);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    writeSseEvent(streamWriter, "error", eventPayload("message", e.getMessage()));
                } catch (IOException ioException) {
                    util.ServletUtil.sendJsonInternalServerError(
                            req,
                            resp,
                            log,
                            "AiSearchServlet#doPost",
                            "Lỗi hệ thống khi xử lý AI: " + ioException.getMessage(),
                            ioException);
                }
                return;
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            if (streamingRequested) {
                try {
                    streamWriter = ensureWriter(resp, streamWriter);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    writeSseEvent(streamWriter, "error", eventPayload(
                            "message",
                            "Lỗi hệ thống khi xử lý AI: " + e.getMessage()));
                } catch (IOException ioException) {
                    util.ServletUtil.sendJsonInternalServerError(
                            req,
                            resp,
                            log,
                            "AiSearchServlet#doPost",
                            "Lỗi hệ thống khi xử lý AI: " + ioException.getMessage(),
                            ioException);
                }
                return;
            }
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "AiSearchServlet#doPost",
                    "Lỗi hệ thống khi xử lý AI: " + e.getMessage(),
                    e);
        }
    }

    private boolean isStreamingRequest(HttpServletRequest req) {
        String streamParam = normalizeText(req.getParameter("stream"));
        if (streamParam != null && ("1".equals(streamParam) || "true".equalsIgnoreCase(streamParam))) {
            return true;
        }

        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            return true;
        }

        String requestedWith = normalizeText(req.getHeader("X-AI-Stream"));
        return requestedWith != null && ("1".equals(requestedWith) || "true".equalsIgnoreCase(requestedWith));
    }

    private PrintWriter ensureWriter(HttpServletResponse resp, PrintWriter currentWriter) throws IOException {
        return currentWriter != null ? currentWriter : resp.getWriter();
    }

    private Map<String, Object> eventPayload(String key, Object value) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put(key, value);
        return payload;
    }

    private void writeSseEvent(PrintWriter writer, String eventType, Map<String, Object> data) throws IOException {
        writer.write("event: " + eventType + "\n");
        writer.write("data: " + mapper.writeValueAsString(data) + "\n\n");
        writer.flush();
    }

    private String normalizeText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
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
            return "Hiện tại các sản phẩm phù hợp đang tạm hết hoặc chưa sẵn sàng. Bạn thử đổi từ khóa để mình gợi ý chính xác hơn nhé.";
        }
        String intentLabel = deriveFallbackIntentLabel(userMessage);
        List<String> highlightNames = extractProductNames(products, 3);
        StringBuilder reply = new StringBuilder();
        reply.append("MetaFruit đã chọn ra các sản phẩm phù hợp nhất");
        if (!intentLabel.isBlank()) {
            reply.append(" cho nhu cầu ").append(intentLabel);
        }
        reply.append(". Nổi bật gồm ").append(joinNaturalLanguage(highlightNames)).append(".");
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
        return "hiện tại";
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
                builder.append(i == values.size() - 1 ? " và " : ", ");
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

    private record ScoredProduct(Product product, int score) {}
}
