package com.fruitmkt.servlet.guest;

import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * HomeServlet — Controller cho chức năng: Trang chủ hiển thị danh mục, sản phẩm
 * nổi bật, và Flash Sale.
 * 
 * URL: /home
 * GET : Trang chủ: sản phẩm nổi bật, danh mục (kết nối DB hoặc tự động nạp Mock
 * Data Việt Nam)
 *
 * @author fruitmkt-team
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Lọc tham số tìm kiếm & danh mục từ Request
        String keyword = req.getParameter("keyword");
        String categoryIdParam = req.getParameter("categoryId");
        Integer categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdParam);
            } catch (NumberFormatException e) {
                // Bỏ qua định dạng không hợp lệ
            }
        }

        List<Category> categoriesList = new ArrayList<>();
        List<Map<String, Object>> flashSaleProducts = new ArrayList<>();
        List<Map<String, Object>> normalProducts = new ArrayList<>();
        boolean isSearchOrFilterActive = (keyword != null && !keyword.trim().isEmpty()) || categoryId != null;

        int page = 1;
        int totalPages = 1;
        int totalProducts = 0;

        try {
            // 2. Lấy danh sách Categories từ Database thực tế
            categoriesList = categoryDAO.findAllActive();

            // 3. Lấy tổng số sản phẩm để tính số trang hỗ trợ phân trang
            totalProducts = productDAO.countSearch(keyword, categoryId, null, null);
            int pageSize = 8; // 8 sản phẩm mỗi trang giúp bố cục gọn gàng, cân đối
            totalPages = (int) Math.ceil((double) totalProducts / pageSize);
            if (totalPages < 1) totalPages = 1;

            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                    if (page > totalPages) page = totalPages;
                } catch (NumberFormatException e) {
                    // Giữ trang mặc định
                }
            }

            // 2a. Lấy sản phẩm Flash Sale thực tế hoạt động độc lập (không phụ thuộc trang hiện tại)
            List<Product> flashProductsRaw = productDAO.findFlashSaleProducts();
            for (Product p : flashProductsRaw) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("description", p.getDescription());
                item.put("rating", p.getRating() != null ? p.getRating() : new BigDecimal("4.8"));
                item.put("soldQuantity", p.getSoldQuantity());

                // Lấy ảnh chính thực tế và bổ sung Context Path tự động để tránh lỗi 404
                ProductImage pi = productImageDAO.findPrimary(p.getProductId());
                String imagePath = null;
                if (pi != null && pi.getFilePath() != null && !pi.getFilePath().trim().isEmpty()) {
                    imagePath = pi.getFilePath().trim().replace('\\', '/');
                }
                
                if (imagePath == null) {
                    imagePath = "/assets/images/placeholder.png";
                }
                
                if (imagePath.startsWith("/assets/")) {
                    imagePath = req.getContextPath() + imagePath;
                }

                item.put("image", imagePath);

                // Lấy biến thể rẻ nhất làm giá đại diện và đơn vị
                List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                BigDecimal basePrice = new BigDecimal("45000");
                String unit = "kg";
                int stockRemaining = 10;

                if (variants != null && !variants.isEmpty()) {
                    ProductVariant cheapestVariant = variants.get(0);
                    basePrice = cheapestVariant.getPrice();
                    unit = cheapestVariant.getVariantLabel();
                    stockRemaining = cheapestVariant.getStockQuantity();
                }
                item.put("unit", unit);

                // Lấy khuyến mãi (Flash Sale) đang hoạt động cho sản phẩm
                List<Promotion> activePromos = promotionDAO.findActivePromotionsByProduct(p.getProductId());
                if (activePromos != null && !activePromos.isEmpty()) {
                    Promotion promo = activePromos.get(0);
                    BigDecimal discountValue = promo.getDiscountValue();
                    BigDecimal finalPrice = basePrice;
                    int discountPercent = 0;

                    if ("PERCENT".equals(promo.getDiscountType())) {
                        discountPercent = discountValue.intValue();
                        BigDecimal discountAmount = basePrice.multiply(discountValue).divide(new BigDecimal("100"), 2,
                                java.math.RoundingMode.HALF_UP);
                        if (promo.getDiscountMax() != null && promo.getDiscountMax().compareTo(BigDecimal.ZERO) > 0) {
                            if (discountAmount.compareTo(promo.getDiscountMax()) > 0) {
                                discountAmount = promo.getDiscountMax();
                            }
                        }
                        finalPrice = basePrice.subtract(discountAmount);
                    } else if ("FIXED".equals(promo.getDiscountType())) {
                        finalPrice = basePrice.subtract(discountValue);
                        if (basePrice.compareTo(BigDecimal.ZERO) > 0) {
                            discountPercent = discountValue.multiply(new BigDecimal("100"))
                                    .divide(basePrice, 0, java.math.RoundingMode.HALF_UP).intValue();
                        }
                    }

                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        finalPrice = BigDecimal.ZERO;
                    }

                    item.put("price", finalPrice);
                    item.put("originalPrice", basePrice);
                    item.put("discountPercent", discountPercent);
                    item.put("stockRemaining", stockRemaining);
                    item.put("stockTotal", stockRemaining + 40);
                    flashSaleProducts.add(item);
                }
            }

            // 4. Lấy sản phẩm từ Database thực tế theo trang hiện tại (lưới catalog chính)
            List<Product> dbProducts = new ArrayList<>();
            if (isSearchOrFilterActive) {
                dbProducts = productDAO.search(keyword, categoryId, null, null, page, pageSize);
            } else {
                dbProducts = productDAO.findAll(page, pageSize);
            }

            // Ánh xạ các sản phẩm trong DB thành dạng Map để dễ đọc ở JSP
            for (Product p : dbProducts) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("description", p.getDescription());
                item.put("rating", p.getRating() != null ? p.getRating() : new BigDecimal("4.8"));
                item.put("soldQuantity", p.getSoldQuantity());

                // 1. Lấy ảnh chính thực tế và bổ sung Context Path tự động để tránh lỗi 404
                ProductImage pi = productImageDAO.findPrimary(p.getProductId());
                String imagePath = null;
                if (pi != null && pi.getFilePath() != null && !pi.getFilePath().trim().isEmpty()) {
                    imagePath = pi.getFilePath().trim().replace('\\', '/');
                }
                
                if (imagePath == null) {
                    imagePath = "/assets/images/placeholder.png";
                }
                
                if (imagePath.startsWith("/assets/")) {
                    imagePath = req.getContextPath() + imagePath;
                }

                item.put("image", imagePath);

                // 2. Lấy biến thể rẻ nhất làm giá đại diện và đơn vị
                List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                BigDecimal basePrice = new BigDecimal("45000");
                String unit = "kg";
                int stockRemaining = 10;

                if (variants != null && !variants.isEmpty()) {
                    ProductVariant cheapestVariant = variants.get(0);
                    basePrice = cheapestVariant.getPrice();
                    unit = cheapestVariant.getVariantLabel();
                    stockRemaining = cheapestVariant.getStockQuantity();
                }
                item.put("unit", unit);

                // 3. Kiểm tra khuyến mãi đang hoạt động để hiển thị giá đã giảm (nếu có) trên lưới chính
                List<Promotion> activePromos = promotionDAO.findActivePromotionsByProduct(p.getProductId());
                if (activePromos != null && !activePromos.isEmpty()) {
                    Promotion promo = activePromos.get(0);
                    BigDecimal discountValue = promo.getDiscountValue();
                    BigDecimal finalPrice = basePrice;

                    if ("PERCENT".equals(promo.getDiscountType())) {
                        BigDecimal discountAmount = basePrice.multiply(discountValue).divide(new BigDecimal("100"), 2,
                                java.math.RoundingMode.HALF_UP);
                        if (promo.getDiscountMax() != null && promo.getDiscountMax().compareTo(BigDecimal.ZERO) > 0) {
                            if (discountAmount.compareTo(promo.getDiscountMax()) > 0) {
                                discountAmount = promo.getDiscountMax();
                            }
                        }
                        finalPrice = basePrice.subtract(discountAmount);
                    } else if ("FIXED".equals(promo.getDiscountType())) {
                        finalPrice = basePrice.subtract(discountValue);
                    }

                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        finalPrice = BigDecimal.ZERO;
                    }

                    item.put("price", finalPrice);
                } else {
                    item.put("price", basePrice);
                }
                
                // Mọi sản phẩm của trang này đều được hiển thị đầy đủ trên lưới chính để tránh còn ô trống
                normalProducts.add(item);
            }

        } catch (SQLException e) {
            req.getServletContext().log("Không kết nối được database hoặc truy vấn lỗi: " + e.getMessage(), e);
        }

        // 4. Gán dữ liệu vào Request scope
        req.setAttribute("categories", categoriesList);
        req.setAttribute("flashSaleProducts", flashSaleProducts);
        req.setAttribute("normalProducts", normalProducts);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedCategoryId", categoryId);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);

        // 5. Forward đến JSP Trang chủ an toàn sau WEB-INF
        req.getServletContext().getRequestDispatcher("/WEB-INF/jsp/guest/home.jsp").forward(req, resp);
    }

    /**
     * Ánh xạ productId sang một URL ảnh Unsplash cao cấp, rực rỡ và ổn định 100%
     * Tránh hoàn toàn lỗi 404 tài nguyên tĩnh trên các máy local của người dùng.
     */

}
