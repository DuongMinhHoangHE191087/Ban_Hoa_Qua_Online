package servlet.guest.product;
import dao.shop.PromotionDAO;
import dao.catalog.ProductPackagingOptionDAO;

import config.AppConfig;
import model.response.ApiResponse;
import service.catalog.ProductService;
import service.order.ReviewService;
import dao.catalog.ProductVariantDAO;
import dao.catalog.ProductImageDAO;
import dao.shop.ShopProfileDAO;
import dao.catalog.ProductDAO;

import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.catalog.ProductImage;
import model.entity.shop.ShopProfile;
import model.entity.Promotion;
import model.dto.common.PagedResultDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * ProductDetailServlet — Controller cho chức năng: Xem chi tiết sản phẩm,
 * hiển thị các biến thể, album ảnh, cửa hàng bán, sản phẩm tương tự dạng slider,
 * và các đánh giá (reviews) phân trang kèm ảnh, có bộ lọc theo số sao.
 *
 * URL: /products/detail
 * GET: Chi tiết sản phẩm và variant
 * POST: -
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service / DAO thích hợp
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Forward đến /WEB-INF/jsp/guest/... (không để truy cập trực tiếp)
 *
 * @author fruitmkt-team
 */
@WebServlet("/products/detail")
public class ProductDetailServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Đọc và kiểm tra mã sản phẩm (product ID)
        String idParam = req.getParameter("id");
        int productId = 0;
        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                productId = Integer.parseInt(idParam.trim());
            }
        } catch (NumberFormatException e) {
            productId = 0;
        }

        if (productId <= 0) {
            // Lưu thông báo lỗi và chuyển hướng về trang chủ
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Yêu cầu không hợp lệ. Không tìm thấy mã sản phẩm.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        try {
            // 2. Đọc thông tin chi tiết sản phẩm (Đồng thời tự động tăng lượt xem)
            Product product = productService.getProductDetail(productId);
            if (product == null || "DELETED".equals(product.getStatus())) {
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Sản phẩm yêu cầu không tồn tại hoặc đã bị ẩn.");
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "warning");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            // Nếu sản phẩm INACTIVE thì là ngừng bán, chặn truy cập hoàn toàn
            if ("INACTIVE".equals(product.getStatus())) {
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Sản phẩm này hiện đã ngừng bán.");
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "warning");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            boolean isExpiredProduct = "OUT_OF_SEASON".equals(product.getStatus());
            req.setAttribute("isExpiredProduct", isExpiredProduct);

            int currentMonth = java.time.LocalDate.now().getMonthValue();
            boolean isOutOfSeason = false;
            if (product.getSeasonStartMonth() != null && product.getSeasonEndMonth() != null) {
                int start = product.getSeasonStartMonth();
                int end = product.getSeasonEndMonth();
                if (start <= end) {
                    isOutOfSeason = (currentMonth < start || currentMonth > end);
                } else {
                    isOutOfSeason = (currentMonth < start && currentMonth > end);
                }
            }
            req.setAttribute("isOutOfSeason", isOutOfSeason);

            boolean hasRequestedToday = false;
            model.entity.auth.User currentUser = (model.entity.auth.User) req.getSession().getAttribute(AppConfig.SESSION_USER);
            if (currentUser != null && isExpiredProduct) {
                hasRequestedToday = productDAO.hasRequestedRestockToday(product.getOwnerId(), currentUser.getUserId(), product.getProductId());
            }
            req.setAttribute("hasRequestedToday", hasRequestedToday);

            // 3. Đọc danh sách các biến thể đang hoạt động của sản phẩm
            List<ProductVariant> variants = productVariantDAO.findByProduct(productId);

            // Đọc danh sách bao bì đóng gói chọn thêm
            dao.catalog.ProductPackagingOptionDAO packagingOptionDAO = new dao.catalog.ProductPackagingOptionDAO();
            List<model.entity.catalog.ProductPackagingOption> packagingOptions = packagingOptionDAO.findByProduct(productId);
            req.setAttribute("packagingOptions", packagingOptions);

            // AJAX format=json support for quick-view and variant selector modals on Home page
            String format = req.getParameter("format");
            boolean isJson = "json".equals(format) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
            
            if (isJson) {
                String actionParam = req.getParameter("action");
                if ("getReviews".equals(actionParam)) {
                    // Xử lý bộ lọc đánh giá theo số sao
                    String ratingParam = req.getParameter("rating");
                    Integer ratingFilter = null;
                    try {
                        if (ratingParam != null && !ratingParam.trim().isEmpty()) {
                            int ratingVal = Integer.parseInt(ratingParam.trim());
                            if (ratingVal >= 1 && ratingVal <= 5) {
                                ratingFilter = ratingVal;
                            }
                        }
                    } catch (NumberFormatException e) {}

                    // Xử lý phân trang
                    String pageParam = req.getParameter("page");
                    int reviewPage = 1;
                    try {
                        if (pageParam != null && !pageParam.trim().isEmpty()) {
                            reviewPage = Integer.parseInt(pageParam.trim());
                            if (reviewPage < 1) reviewPage = 1;
                        }
                    } catch (NumberFormatException e) {}

                    int reviewPageSize = 5;
                    PagedResultDTO reviewPagedResult = reviewService.getReviewsPaginated(productId, ratingFilter, reviewPage, reviewPageSize);

                    util.JsonUtil.writeJson(resp, ApiResponse.ok(reviewPagedResult));
                    return;
                }

                // ── Kiểm tra điều kiện tồn tại trước khi trả JSON ──
                // Sản phẩm hết mùa — khách không thể mua
                if (isExpiredProduct) {
                    Map<String, Object> errData = new java.util.HashMap<>();
                    errData.put("success", false);
                    errData.put("reason", "OUT_OF_SEASON");
                    errData.put("message", "Sản phẩm này đã hết mùa. Bạn có thể yêu cầu nhập kho vụ mới.");
                    util.JsonUtil.writeJson(resp, ApiResponse.ok(errData));
                    return;
                }
                // Sản phẩm đang ngoài mùa vụ — hiển thị cảnh báo nhưng vẫn cho mua
                boolean isOutOfSeasonJson = Boolean.TRUE.equals(req.getAttribute("isOutOfSeason"));

                // Tự động map các trường sang HashMap để tránh NullPointerException của Map.of khi có giá trị null
                Map<Integer, ProductImage> primaryImageMap = productImageDAO.findPrimaryByProductIds(
                        java.util.Collections.singletonList(product.getProductId()));
                ProductImage pi = primaryImageMap.get(product.getProductId());
                String primaryImage = null;
                if (pi != null && pi.getFilePath() != null) {
                    primaryImage = pi.getFilePath().trim().replace('\\', '/');
                }

                Map<String, Object> productMap = new java.util.HashMap<>();
                productMap.put("productId", product.getProductId());
                productMap.put("name", product.getName());
                productMap.put("description", product.getDescription() != null ? product.getDescription() : "");
                productMap.put("imagePath", primaryImage != null ? primaryImage : "");
                productMap.put("isOutOfSeason", isOutOfSeasonJson);

                List<Map<String, Object>> variantsMapList = new java.util.ArrayList<>();
                List<Map<String, Object>> inStockVariants = new java.util.ArrayList<>();
                for (ProductVariant v : variants) {
                    Map<String, Object> vMap = new java.util.HashMap<>();
                    vMap.put("variantId", v.getVariantId());
                    vMap.put("variantLabel", v.getVariantLabel());
                    vMap.put("price", v.getPrice());
                    vMap.put("activePrice", v.getActivePrice());
                    vMap.put("isDiscounted", v.getIsDiscounted());
                    vMap.put("discountPrice", v.getDiscountPrice());
                    vMap.put("weightKg", v.getWeightKg());
                    vMap.put("stockQuantity", v.getStockQuantity());
                    boolean soldOut = v.getStockQuantity() <= 0;
                    vMap.put("soldOut", soldOut);
                    variantsMapList.add(vMap);
                    if (!soldOut) inStockVariants.add(vMap);
                }

                // Nếu không có variant nào còn hàng → báo lỗi rõ ràng
                if (inStockVariants.isEmpty()) {
                    Map<String, Object> errData = new java.util.HashMap<>();
                    errData.put("success", false);
                    errData.put("reason", "OUT_OF_STOCK");
                    errData.put("message", "Sản phẩm tạm hết hàng. Vui lòng quay lại sau.");
                    util.JsonUtil.writeJson(resp, ApiResponse.ok(errData));
                    return;
                }

                List<Map<String, Object>> packagingsMapList = new java.util.ArrayList<>();
                for (model.entity.catalog.ProductPackagingOption po : packagingOptions) {
                    Map<String, Object> poMap = new java.util.HashMap<>();
                    poMap.put("packagingId", po.getPackagingId());
                    poMap.put("label", po.getLabel());
                    poMap.put("priceAdd", po.getPriceAdd());
                    packagingsMapList.add(poMap);
                }

                Map<String, Object> data = new java.util.HashMap<>();
                data.put("product", productMap);
                data.put("variants", inStockVariants);  // Chỉ trả variants còn hàng
                data.put("allVariants", variantsMapList); // Trả tất cả (kể cả hết hàng) để UI disable
                data.put("packagingOptions", packagingsMapList);

                util.JsonUtil.writeJson(resp, ApiResponse.ok(data));
                return;
            }

            // 4. Đọc album ảnh của sản phẩm
            List<ProductImage> images = productImageDAO.findByProduct(productId);

            // 5. Đọc hồ sơ cửa hàng của chủ sở hữu sản phẩm (Shop Owner Profile)
            ShopProfile shopProfile = null;
            shopProfile = shopProfileDAO.findOneByUserId(product.getOwnerId());

            // 6. Đọc danh sách sản phẩm tương tự cùng danh mục (loại trừ sản phẩm hiện tại, giới hạn 8 sản phẩm)
            List<Product> similarProductsRaw = productDAO.findSimilarProducts(productId, product.getCategoryId(), 8);
            List<Integer> similarProductIds = new java.util.ArrayList<>();
            for (Product p : similarProductsRaw) {
                similarProductIds.add(p.getProductId());
            }
            Map<Integer, ProductImage> similarImageMap = productImageDAO.findPrimaryByProductIds(similarProductIds);
            Map<Integer, List<ProductVariant>> similarVariantMap = productVariantDAO.findByProductIds(similarProductIds);
            List<Map<String, Object>> similarProducts = new java.util.ArrayList<>();
            for (Product p : similarProductsRaw) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("rating", p.getRating() != null ? p.getRating() : java.math.BigDecimal.ZERO);
                item.put("originRegion", p.getOriginRegion());

                // Lấy ảnh chính thực tế
                item.put("image", resolveImagePath(req, similarImageMap.get(p.getProductId())));

                // Lấy biến thể rẻ nhất làm giá đại diện và đơn vị
                List<ProductVariant> pVariants = similarVariantMap.get(p.getProductId());
                java.math.BigDecimal basePrice = new java.math.BigDecimal("45000");
                String unit = "kg";
                if (pVariants != null && !pVariants.isEmpty()) {
                    ProductVariant cheapestVariant = pVariants.get(0);
                    basePrice = cheapestVariant.getActivePrice();
                    unit = cheapestVariant.getVariantLabel();
                }
                item.put("price", basePrice);
                item.put("unit", unit);

                similarProducts.add(item);
            }

            // 7. Lấy các sản phẩm khác của shop này để hiển thị "Xem thêm từ cửa hàng"
            List<Map<String, Object>> shopOtherProducts = new java.util.ArrayList<>();
            List<Product> shopProductsRaw = productDAO.findByOwnerAndActiveStatus(product.getOwnerId(), productId, 8);
            List<Integer> shopProductIds = new java.util.ArrayList<>();
            for (Product sp : shopProductsRaw) {
                shopProductIds.add(sp.getProductId());
            }
            Map<Integer, ProductImage> shopImageMap = productImageDAO.findPrimaryByProductIds(shopProductIds);
            Map<Integer, List<ProductVariant>> shopVariantMap = productVariantDAO.findByProductIds(shopProductIds);
            for (Product sp : shopProductsRaw) {
                Map<String, Object> spItem = new java.util.HashMap<>();
                spItem.put("productId", sp.getProductId());
                spItem.put("name", sp.getName());
                spItem.put("rating", sp.getRating() != null ? sp.getRating() : java.math.BigDecimal.ZERO);
                spItem.put("image", resolveImagePath(req, shopImageMap.get(sp.getProductId())));
                List<ProductVariant> spVars = shopVariantMap.get(sp.getProductId());
                java.math.BigDecimal spPrice = new java.math.BigDecimal("45000");
                if (spVars != null && !spVars.isEmpty()) {
                    spPrice = spVars.get(0).getActivePrice();
                }
                spItem.put("price", spPrice);
                shopOtherProducts.add(spItem);
            }

            // 8. Lấy voucher/khuyến mãi của shop và của hệ thống
            List<Promotion> shopVouchers = promotionDAO.findShopActivePromotions(product.getOwnerId());
            List<Promotion> systemVouchers = promotionDAO.findActiveSystemPromotions();

            // 9. Lấy khuyến mãi dành riêng cho sản phẩm này (Flash Sale)
            List<Promotion> productPromotions = promotionDAO.findActivePromotionsByProduct(productId);

            // 10. Xử lý bộ lọc đánh giá theo số sao
            String ratingParam = req.getParameter("rating");
            Integer ratingFilter = null;
            try {
                if (ratingParam != null && !ratingParam.trim().isEmpty()) {
                    int ratingVal = Integer.parseInt(ratingParam.trim());
                    if (ratingVal >= 1 && ratingVal <= 5) {
                        ratingFilter = ratingVal;
                    }
                }
            } catch (NumberFormatException e) {
                ratingFilter = null;
            }

            // 11. Xử lý phân trang cho danh sách đánh giá
            String pageParam = req.getParameter("page");
            int reviewPage = 1;
            try {
                if (pageParam != null && !pageParam.trim().isEmpty()) {
                    reviewPage = Integer.parseInt(pageParam.trim());
                    if (reviewPage < 1) reviewPage = 1;
                }
            } catch (NumberFormatException e) {
                reviewPage = 1;
            }

            // Kích thước mặc định của trang review là 5 bản ghi
            int reviewPageSize = 5;
            PagedResultDTO reviewPagedResult = reviewService.getReviewsPaginated(productId, ratingFilter, reviewPage, reviewPageSize);

            // 12. Thống kê số lượng đánh giá theo từng sao (1-5★)
            Map<Integer, Integer> ratingDistribution = reviewService.getRatingDistribution(productId);
            int[] ratingCounts = new int[6];
            for (Map.Entry<Integer, Integer> entry : ratingDistribution.entrySet()) {
                Integer starKey = entry.getKey();
                if (starKey != null && starKey >= 1 && starKey <= 5) {
                    ratingCounts[starKey] = entry.getValue() != null ? entry.getValue() : 0;
                }
            }

            // 13. Tính tổng số review thực tế
            int totalReviewsCount = 0;
            for (int star = 1; star <= 5; star++) {
                totalReviewsCount += ratingCounts[star];
            }
            boolean hasReviews = totalReviewsCount > 0;
            java.math.BigDecimal displayRating = java.math.BigDecimal.ZERO;
            if (hasReviews) {
                java.math.BigDecimal totalScore = java.math.BigDecimal.ZERO;
                for (int star = 1; star <= 5; star++) {
                    int count = ratingCounts[star];
                    if (count > 0) {
                        totalScore = totalScore.add(java.math.BigDecimal.valueOf((long) star * count));
                    }
                }
                displayRating = totalScore.divide(java.math.BigDecimal.valueOf(totalReviewsCount), 2, java.math.RoundingMode.HALF_UP);
            }
            product.setRating(displayRating);

            // 14. Đổ dữ liệu vào Request Attributes
            req.setAttribute("product", product);
            req.setAttribute("variants", variants);
            req.setAttribute("images", images);
            req.setAttribute("shopProfile", shopProfile);
            req.setAttribute("similarProducts", similarProducts);
            req.setAttribute("shopOtherProducts", shopOtherProducts);
            req.setAttribute("shopVouchers", shopVouchers);
            req.setAttribute("systemVouchers", systemVouchers);
            req.setAttribute("productPromotions", productPromotions);
            req.setAttribute("reviewPagedResult", reviewPagedResult);
            req.setAttribute("ratingDistribution", ratingDistribution);
            req.setAttribute("ratingCounts", ratingCounts);
            req.setAttribute("ratingFilter", ratingFilter);
            req.setAttribute("totalReviewsCount", totalReviewsCount);
            req.setAttribute("hasReviews", hasReviews);

            // 15. Forward tới trang JSP hiển thị
            req.getRequestDispatcher("/WEB-INF/jsp/guest/product-detail.jsp").forward(req, resp);

        } catch (SQLException e) {
            // Log lỗi SQL và chuyển hướng về trang lỗi hệ thống
            getServletContext().log("ProductDetailServlet SQL Error for ID " + productId + ": " + e.getMessage(), e);
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String idParam = req.getParameter("id");
        int productId = 0;
        try {
            if (idParam != null) productId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {}

        // 1. Chỉ user đã đăng nhập mới có thể gửi yêu cầu
        model.entity.auth.User currentUser = (model.entity.auth.User) req.getSession().getAttribute(AppConfig.SESSION_USER);
        if (currentUser == null) {
            util.JsonUtil.writeJson(resp, ApiResponse.error("Bạn cần đăng nhập để thực hiện gửi yêu cầu nhập kho."));
            return;
        }

        if (productId <= 0) {
            util.JsonUtil.writeJson(resp, ApiResponse.error("Mã sản phẩm không hợp lệ."));
            return;
        }

        try {
            Product p = productDAO.findOneById(productId);
            if (p != null) {

                // Nếu sản phẩm INACTIVE thì chặn gửi yêu cầu restock
                if ("INACTIVE".equals(p.getStatus())) {
                    util.JsonUtil.writeJson(resp, ApiResponse.error("Sản phẩm này đã ngừng bán, không thể gửi yêu cầu nhập kho."));
                    return;
                }

                // 2. Mỗi user chỉ gửi được 1 lần/ngày cho mỗi sản phẩm
                boolean hasRequestedToday = productDAO.hasRequestedRestockToday(p.getOwnerId(), currentUser.getUserId(), p.getProductId());
                if (hasRequestedToday) {
                    util.JsonUtil.writeJson(resp, ApiResponse.error("Bạn đã gửi yêu cầu nhập kho cho sản phẩm này hôm nay rồi. Vui lòng quay lại vào ngày mai!"));
                    return;
                }

                // Gọi DAO tạo thông báo cho chủ shop
                productDAO.createRestockNotification(p.getOwnerId(), currentUser.getUserId(), p.getProductId(), p.getName());
                util.JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("message", "Gửi yêu cầu nhập kho vụ mới tới chủ cửa hàng thành công!")));
            } else {
                util.JsonUtil.writeJson(resp, ApiResponse.error("Không tìm thấy thông tin sản phẩm."));
            }
        } catch (Exception e) {
            getServletContext().log("Error in sending restock request: " + e.getMessage(), e);
            util.JsonUtil.writeJson(resp, ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    private String resolveImagePath(HttpServletRequest req, ProductImage image) {
        String imagePath = null;
        if (image != null && image.getFilePath() != null && !image.getFilePath().trim().isEmpty()) {
            imagePath = image.getFilePath().trim().replace('\\', '/');
        }
        if (imagePath == null) {
            return req.getContextPath() + "/assets/img/placeholder.png";
        }
        if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
            if (!imagePath.startsWith("/")) {
                imagePath = "/" + imagePath;
            }
            imagePath = req.getContextPath() + imagePath;
        }
        return imagePath;
    }
}
