package com.fruitmkt.servlet.guest;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.service.ReviewService;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.model.dto.PagedResultDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
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
            if (product == null) {
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Sản phẩm yêu cầu không tồn tại hoặc đã bị ẩn.");
                req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "warning");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            // 3. Đọc danh sách các biến thể đang hoạt động của sản phẩm
            List<ProductVariant> variants = productVariantDAO.findByProduct(productId);

            // 4. Đọc album ảnh của sản phẩm
            List<ProductImage> images = productImageDAO.findByProduct(productId);

            // 5. Đọc hồ sơ cửa hàng của chủ sở hữu sản phẩm (Shop Owner Profile)
            ShopProfile shopProfile = null;
            List<ShopProfile> shopProfiles = shopProfileDAO.findByUserId(product.getOwnerId());
            if (shopProfiles != null && !shopProfiles.isEmpty()) {
                shopProfile = shopProfiles.get(0);
            }

            // 6. Đọc danh sách sản phẩm tương tự cùng danh mục (loại trừ sản phẩm hiện tại, giới hạn 8 sản phẩm)
            List<Product> similarProductsRaw = productDAO.findSimilarProducts(productId, product.getCategoryId(), 8);
            List<Map<String, Object>> similarProducts = new java.util.ArrayList<>();
            for (Product p : similarProductsRaw) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("rating", p.getRating() != null ? p.getRating() : new java.math.BigDecimal("4.8"));
                item.put("originRegion", p.getOriginRegion());

                // Lấy ảnh chính thực tế
                ProductImage pi = productImageDAO.findPrimary(p.getProductId());
                String imagePath = null;
                if (pi != null && pi.getFilePath() != null && !pi.getFilePath().trim().isEmpty()) {
                    imagePath = pi.getFilePath().trim().replace('\\', '/');
                }
                if (imagePath == null) {
                    imagePath = req.getContextPath() + "/assets/img/placeholder.png";
                } else if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
                    if (!imagePath.startsWith("/")) {
                        imagePath = "/" + imagePath;
                    }
                    imagePath = req.getContextPath() + imagePath;
                }
                item.put("image", imagePath);

                // Lấy biến thể rẻ nhất làm giá đại diện và đơn vị
                List<ProductVariant> pVariants = productVariantDAO.findByProduct(p.getProductId());
                java.math.BigDecimal basePrice = new java.math.BigDecimal("45000");
                String unit = "kg";
                if (pVariants != null && !pVariants.isEmpty()) {
                    ProductVariant cheapestVariant = pVariants.get(0);
                    basePrice = cheapestVariant.getPrice();
                    unit = cheapestVariant.getVariantLabel();
                }
                item.put("price", basePrice);
                item.put("unit", unit);

                similarProducts.add(item);
            }

            // 7. Lấy các sản phẩm khác của shop này để hiển thị "Xem thêm từ cửa hàng"
            List<Map<String, Object>> shopOtherProducts = new java.util.ArrayList<>();
            List<Product> shopProductsRaw = productDAO.findByOwnerAndActiveStatus(product.getOwnerId(), productId, 8);
            for (Product sp : shopProductsRaw) {
                Map<String, Object> spItem = new java.util.HashMap<>();
                spItem.put("productId", sp.getProductId());
                spItem.put("name", sp.getName());
                spItem.put("rating", sp.getRating() != null ? sp.getRating() : new java.math.BigDecimal("4.5"));
                ProductImage spPi = productImageDAO.findPrimary(sp.getProductId());
                String spImg = null;
                if (spPi != null && spPi.getFilePath() != null && !spPi.getFilePath().trim().isEmpty()) {
                    spImg = spPi.getFilePath().trim().replace('\\', '/');
                }
                if (spImg == null) {
                    spImg = req.getContextPath() + "/assets/img/placeholder.png";
                } else if (!spImg.startsWith("http://") && !spImg.startsWith("https://")) {
                    if (!spImg.startsWith("/")) spImg = "/" + spImg;
                    spImg = req.getContextPath() + spImg;
                }
                spItem.put("image", spImg);
                List<ProductVariant> spVars = productVariantDAO.findByProduct(sp.getProductId());
                java.math.BigDecimal spPrice = new java.math.BigDecimal("45000");
                if (spVars != null && !spVars.isEmpty()) {
                    spPrice = spVars.get(0).getPrice();
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

            // 13. Tính tổng số review thực tế
            int totalReviewsCount = 0;
            for (int count : ratingDistribution.values()) {
                totalReviewsCount += count;
            }

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
            req.setAttribute("ratingFilter", ratingFilter);
            req.setAttribute("totalReviewsCount", totalReviewsCount);

            // 15. Forward tới trang JSP hiển thị
            req.getRequestDispatcher("/WEB-INF/jsp/guest/product-detail.jsp").forward(req, resp);

        } catch (SQLException e) {
            // Log lỗi SQL và chuyển hướng về trang lỗi hệ thống
            e.printStackTrace();
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
