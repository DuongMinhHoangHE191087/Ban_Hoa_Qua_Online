package com.fruitmkt.servlet.guest;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.dto.PagedResultDTO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductListServlet — Danh sách sản phẩm với filter và phân trang.
 *
 * URL: /products
 * GET : keyword, categoryId, minPrice, maxPrice, page → forward đến product-list.jsp
 *
 * @author fruitmkt-team
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        int page = parseIntParam(req, "page", 1);
        String keyword = req.getParameter("keyword");
        
        // Parse multi categories
        List<Integer> categoryIds = new java.util.ArrayList<>();
        String[] rawCatIds = req.getParameterValues("categoryIds");
        if (rawCatIds != null) {
            for (String val : rawCatIds) {
                try {
                    categoryIds.add(Integer.parseInt(val.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        // Support fallback single categoryId
        String rawCatId = req.getParameter("categoryId");
        if (rawCatId != null && !rawCatId.trim().isEmpty() && categoryIds.isEmpty()) {
            try {
                categoryIds.add(Integer.parseInt(rawCatId.trim()));
            } catch (NumberFormatException ignored) {}
        }

        BigDecimal minPrice = parseDecimalParam(req, "minPrice");
        BigDecimal maxPrice = parseDecimalParam(req, "maxPrice");
        
        // Parse rating filter
        Double rating = null;
        String rawRating = req.getParameter("rating");
        if (rawRating != null && !rawRating.trim().isEmpty()) {
            try {
                rating = Double.parseDouble(rawRating.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Parse stock filter
        Boolean inStockOnly = null;
        String rawStock = req.getParameter("inStockOnly");
        if (rawStock != null && !rawStock.trim().isEmpty()) {
            inStockOnly = Boolean.parseBoolean(rawStock.trim());
        }

        String sortBy = req.getParameter("sortBy");

        List<Category> categories = Collections.emptyList();
        PagedResultDTO pagedResult = null;
        List<Product> bestSellers = Collections.emptyList();
        List<Product> recentlyViewed = Collections.emptyList();

        Integer categoryId = (!categoryIds.isEmpty()) ? categoryIds.get(0) : null;

        try {
            categories  = categoryDAO.findAllActive();
            pagedResult = productService.getProductListAdvanced(page, keyword, categoryIds, minPrice, maxPrice, rating, inStockOnly, sortBy);
            bestSellers = productService.getBestSellers();
            
            // II.23: Load recently viewed products from cookies
            Cookie[] cookies = req.getCookies();
            List<Integer> recentlyViewedIds = new java.util.ArrayList<>();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("recently_viewed_ids".equals(c.getName())) {
                        String val = java.net.URLDecoder.decode(c.getValue(), "UTF-8");
                        if (val != null && !val.trim().isEmpty()) {
                            String[] split = val.split(",");
                            // load up to 10 products
                            for (int i = split.length - 1; i >= 0 && recentlyViewedIds.size() < 10; i--) {
                                try {
                                    int id = Integer.parseInt(split[i].trim());
                                    if (!recentlyViewedIds.contains(id)) {
                                        recentlyViewedIds.add(id);
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                }
            }
            if (!recentlyViewedIds.isEmpty()) {
                recentlyViewed = productService.getRecentlyViewed(recentlyViewedIds);
            }

            // Chuyển đổi danh sách Product sang List<Map<String, Object>> để cung cấp đầy đủ thông tin (ảnh, giá, đơn vị) cho JSP
            if (pagedResult != null && pagedResult.getItems() != null) {
                @SuppressWarnings("unchecked")
                List<Product> rawProducts = (List<Product>) pagedResult.getItems();
                List<Map<String, Object>> mappedProducts = new ArrayList<>();
                for (Product p : rawProducts) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", p.getProductId());
                    item.put("name", p.getName());
                    item.put("description", p.getDescription());
                    item.put("rating", p.getRating() != null ? p.getRating() : new BigDecimal("4.8"));
                    item.put("soldQuantity", p.getSoldQuantity());

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
                    List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                    BigDecimal basePrice = new BigDecimal("45000");
                    String unit = "kg";
                    if (variants != null && !variants.isEmpty()) {
                        ProductVariant cheapestVariant = variants.get(0);
                        basePrice = cheapestVariant.getPrice();
                        unit = cheapestVariant.getVariantLabel();
                    }
                    item.put("unit", unit);

                    // Kiểm tra khuyến mãi đang hoạt động
                    List<Promotion> activePromos = promotionDAO.findActivePromotionsByProduct(p.getProductId());
                    if (activePromos != null && !activePromos.isEmpty()) {
                        Promotion promo = activePromos.get(0);
                        BigDecimal discountValue = promo.getDiscountValue();
                        BigDecimal finalPrice = basePrice;
                        if ("PERCENT".equals(promo.getDiscountType())) {
                            BigDecimal discountAmount = basePrice.multiply(discountValue).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
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
                    mappedProducts.add(item);
                }
                pagedResult.setItems(mappedProducts);
            }

        } catch (Exception e) {
            req.getServletContext().log("ProductListServlet error: " + e.getMessage(), e);
            req.setAttribute("errorMsg", "Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.");
        }

        // Check if Ajax request
        boolean isAjax = "true".equals(req.getParameter("ajax")) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
        if (isAjax) {
            com.fruitmkt.util.JsonUtil.writeJson(resp, pagedResult);
            return;
        }

        req.setAttribute("categories",   categories);
        req.setAttribute("pagedResult",  pagedResult);
        req.setAttribute("bestSellers",  bestSellers);
        req.setAttribute("recentlyViewed", recentlyViewed);
        req.setAttribute("keyword",      keyword);
        req.setAttribute("categoryId",    categoryId);
        req.setAttribute("categoryIds",   categoryIds);
        req.setAttribute("minPrice",     minPrice);
        req.setAttribute("maxPrice",     maxPrice);
        req.setAttribute("rating",       rating);
        req.setAttribute("inStockOnly",  inStockOnly);
        req.setAttribute("sortBy",       sortBy);

        req.getRequestDispatcher("/WEB-INF/jsp/guest/product-list.jsp").forward(req, resp);
    }

    // ── Parse helpers ──────────────────────────────────────────────────────

    private int parseIntParam(HttpServletRequest req, String name, int defaultValue) {
        String raw = req.getParameter(name);
        if (raw == null || raw.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseIntegerParam(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimalParam(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            BigDecimal val = new BigDecimal(raw.trim());
            return val.compareTo(BigDecimal.ZERO) > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
