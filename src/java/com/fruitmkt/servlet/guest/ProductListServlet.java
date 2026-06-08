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

        int page       = parseIntParam(req, "page", 1);
        String keyword = req.getParameter("keyword");
        if (keyword != null) {
            keyword = keyword.replaceAll("<[^>]*>", "").trim();
        }
        Integer categoryId = parseIntegerParam(req, "categoryId");
        BigDecimal minPrice = parseDecimalParam(req, "minPrice");
        BigDecimal maxPrice = parseDecimalParam(req, "maxPrice");

        List<Category> categories = Collections.emptyList();
        PagedResultDTO pagedResult = null;

        try {
            categories  = categoryDAO.findAllActive();
            String suggestedIdsParam = req.getParameter("suggestedIds");
            if (suggestedIdsParam != null && !suggestedIdsParam.trim().isEmpty()) {
                String[] idsStr = suggestedIdsParam.split(",");
                List<Product> productsList = new ArrayList<>();
                for (String idStr : idsStr) {
                    try {
                        int id = Integer.parseInt(idStr.trim());
                        Product p = productService.getProductById(id);
                        if (p != null && "ACTIVE".equals(p.getStatus()) && "APPROVED".equals(p.getApprovalStatus())) {
                            productsList.add(p);
                        }
                    } catch (NumberFormatException ignored) {}
                }
                pagedResult = new PagedResultDTO(productsList, 1, 1, productsList.size(), AppConfig.PAGE_SIZE_PRODUCTS);
            } else {
                pagedResult = productService.getProductList(page, keyword, categoryId, minPrice, maxPrice);
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

                    item.put("categoryId", p.getCategoryId());

                    // Lấy biến thể rẻ nhất làm giá đại diện và đơn vị
                    List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                    BigDecimal basePrice = new BigDecimal("45000");
                    String unit = "kg";
                    int totalStock = 0;
                    int defaultVariantId = 0;
                    int cheapestStock = 0;
                    if (variants != null && !variants.isEmpty()) {
                        ProductVariant cheapestVariant = variants.get(0);
                        basePrice = cheapestVariant.getPrice();
                        unit = cheapestVariant.getVariantLabel();
                        defaultVariantId = cheapestVariant.getVariantId();
                        cheapestStock = cheapestVariant.getStockQuantity();
                        for (ProductVariant v : variants) {
                            totalStock += v.getStockQuantity();
                        }
                    }
                    item.put("variantId", defaultVariantId);
                    item.put("stockQuantity", cheapestStock);
                    item.put("unit", unit);
                    item.put("inStock", totalStock > 0);

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

        } catch (SQLException e) {
            req.getServletContext().log("ProductListServlet DB error: " + e.getMessage(), e);
            req.setAttribute("errorMsg", "Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.");
        }

        req.setAttribute("categories",   categories);
        req.setAttribute("pagedResult",  pagedResult);
        req.setAttribute("keyword",      keyword);
        req.setAttribute("categoryId",   categoryId);
        req.setAttribute("minPrice",     minPrice);
        req.setAttribute("maxPrice",     maxPrice);

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
