package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ProductManageServlet — Controller cho chức năng: Danh sách sản phẩm của shop, CRUD
 *
 * URL: /shop/products
 * GET : Danh sách sản phẩm của shop, CRUD
 * POST: Tạo/sửa/ẩn sản phẩm
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/products")
public class ProductManageServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User user = SessionUtil.getCurrentUser(session);
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                List<Category> categories = categoryDAO.findAllActive();
                req.setAttribute("categories", categories);
                req.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(req, resp);
            } else if ("edit".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("id"));
                Product product = productService.getProductDetail(productId);
                if (product.getOwnerId() != user.getUserId()) {
                    resp.sendError(403, "Bạn không có quyền thao tác trên sản phẩm này.");
                    return;
                }
                List<ProductVariant> variants = productVariantDAO.findByProduct(productId);
                List<Category> categories = categoryDAO.findAllActive();

                req.setAttribute("product", product);
                req.setAttribute("variants", variants);
                req.setAttribute("categories", categories);
                req.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(req, resp);
            } else if ("toggle".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("id"));
                Product product = productService.getProductDetail(productId);
                if (product.getOwnerId() != user.getUserId()) {
                    resp.sendError(403, "Bạn không có quyền thao tác trên sản phẩm này.");
                    return;
                }
                String nextStatus = "ACTIVE".equals(product.getStatus()) ? "INACTIVE" : "ACTIVE";
                productService.toggleStatus(productId, nextStatus);
                SessionUtil.flashSuccess(session, "Cập nhật trạng thái sản phẩm thành công!");
                resp.sendRedirect(req.getContextPath() + "/shop/products");
            } else {
                // List view
                List<Product> products = productDAO.findByOwner(user.getUserId());
                req.setAttribute("products", products);
                req.getRequestDispatcher("/WEB-INF/jsp/shop/product-list.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            req.getServletContext().log("ProductManageServlet GET error: " + e.getMessage(), e);
            SessionUtil.flashError(session, "Có lỗi xảy ra: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User user = SessionUtil.getCurrentUser(session);
        String rawId = req.getParameter("id");

        try {
            int categoryId = Integer.parseInt(req.getParameter("categoryId"));
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String originCountry = req.getParameter("originCountry");
            String originRegion = req.getParameter("originRegion");
            String rawHarvestDate = req.getParameter("harvestDate");
            String rawShelfLife = req.getParameter("shelfLifeDays");
            String storageInstruction = req.getParameter("storageInstruction");
            String labelType = req.getParameter("labelType");
            String rawSeasonStart = req.getParameter("seasonStart");
            String rawSeasonEnd = req.getParameter("seasonEnd");

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên sản phẩm không được trống.");
            }

            LocalDate harvestDate = (rawHarvestDate != null && !rawHarvestDate.trim().isEmpty()) ? LocalDate.parse(rawHarvestDate.trim()) : null;
            Integer shelfLifeDays = (rawShelfLife != null && !rawShelfLife.trim().isEmpty()) ? Integer.parseInt(rawShelfLife.trim()) : null;
            
            Integer seasonStart = (rawSeasonStart != null && !rawSeasonStart.trim().isEmpty()) ? Integer.parseInt(rawSeasonStart.trim()) : null;
            Integer seasonEnd = (rawSeasonEnd != null && !rawSeasonEnd.trim().isEmpty()) ? Integer.parseInt(rawSeasonEnd.trim()) : null;

            if (seasonStart != null && (seasonStart < 1 || seasonStart > 12)) {
                throw new IllegalArgumentException("Tháng bắt đầu mùa vụ phải từ 1 đến 12.");
            }
            if (seasonEnd != null && (seasonEnd < 1 || seasonEnd > 12)) {
                throw new IllegalArgumentException("Tháng kết thúc mùa vụ phải từ 1 đến 12.");
            }

            // Parse weight variants
            String[] weights = req.getParameterValues("weights[]");
            String[] prices = req.getParameterValues("prices[]");
            String[] discountPrices = req.getParameterValues("discountPrices[]");
            String[] stocks = req.getParameterValues("stocks[]");
            String[] packagingOptions = req.getParameterValues("packagingOptions[]");

            if (weights == null || weights.length == 0) {
                throw new IllegalArgumentException("Phải khai báo ít nhất một biến thể khối lượng cho sản phẩm.");
            }

            List<ProductVariant> variants = new ArrayList<>();
            Set<Integer> weightSet = new HashSet<>();

            for (int i = 0; i < weights.length; i++) {
                int weightGrams = Integer.parseInt(weights[i].trim());
                if (weightGrams <= 0) {
                    throw new IllegalArgumentException("Khối lượng sản phẩm phải lớn hơn 0.");
                }
                
                // II.7 Test case: Inserting duplicate weight values must be rejected
                if (!weightSet.add(weightGrams)) {
                    throw new IllegalArgumentException("Không được thêm các biến thể khối lượng trùng lặp (" + weightGrams + "g).");
                }

                BigDecimal price = new BigDecimal(prices[i].trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Đơn giá biến thể phải lớn hơn 0 VNĐ.");
                }

                BigDecimal discountPrice = null;
                if (discountPrices != null && discountPrices.length > i && discountPrices[i] != null && !discountPrices[i].trim().isEmpty()) {
                    discountPrice = new BigDecimal(discountPrices[i].trim());
                    // II.15 Test case: discount price must be less than base price
                    if (discountPrice.compareTo(price) >= 0) {
                        throw new IllegalArgumentException("Giá khuyến mãi (" + discountPrice + " VNĐ) phải nhỏ hơn giá gốc (" + price + " VNĐ).");
                    }
                    if (discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Giá khuyến mãi phải lớn hơn 0 VNĐ.");
                    }
                }

                int stock = Integer.parseInt(stocks[i].trim());
                if (stock < 0) {
                    throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
                }

                String packaging = (packagingOptions != null && packagingOptions.length > i && packagingOptions[i] != null && !packagingOptions[i].trim().isEmpty()) ? packagingOptions[i].trim() : null;
                if (packaging != null && !"Gift Box".equals(packaging) && !"Foam Tray".equals(packaging)) {
                    throw new IllegalArgumentException("Tùy chọn đóng gói không hợp lệ.");
                }

                ProductVariant v = new ProductVariant();
                v.setVariantLabel(weightGrams + "g");
                v.setSku("SKU-" + System.currentTimeMillis() + "-" + weightGrams + "-" + i);
                v.setPrice(price);
                v.setDiscountPrice(discountPrice);
                v.setStockQuantity(stock);
                v.setWeightGrams(weightGrams);
                v.setPackagingOption(packaging);
                v.setIsActive(true);

                variants.add(v);
            }

            Product product;
            boolean isEdit = (rawId != null && !rawId.trim().isEmpty());

            if (isEdit) {
                int productId = Integer.parseInt(rawId.trim());
                product = productService.getProductDetail(productId);
                if (product.getOwnerId() != user.getUserId()) {
                    resp.sendError(403, "Bạn không có quyền thao tác trên sản phẩm này.");
                    return;
                }
            } else {
                product = new Product();
                product.setOwnerId(user.getUserId());
            }

            product.setCategoryId(categoryId);
            product.setName(name);
            product.setDescription(description);
            product.setOriginCountry(originCountry);
            product.setOriginRegion(originRegion);
            product.setHarvestDate(harvestDate);
            product.setShelfLifeDays(shelfLifeDays);
            product.setStorageInstruction(storageInstruction);
            product.setLabelType(labelType);
            product.setSeasonStart(seasonStart);
            product.setSeasonEnd(seasonEnd);

            if (isEdit) {
                productService.updateProduct(product);
                // Save variants
                productVariantDAO.deactivateAllByProduct(product.getProductId());
                for (ProductVariant v : variants) {
                    v.setProductId(product.getProductId());
                    productVariantDAO.save(v);
                }
                SessionUtil.flashSuccess(session, "Cập nhật sản phẩm '" + name + "' thành công!");
            } else {
                int productId = productService.createProduct(product);
                for (ProductVariant v : variants) {
                    v.setProductId(productId);
                    productVariantDAO.save(v);
                }
                SessionUtil.flashSuccess(session, "Thêm sản phẩm mới '" + name + "' thành công!");
            }
            resp.sendRedirect(req.getContextPath() + "/shop/products");

        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
            resp.sendRedirect(req.getHeader("referer") != null ? req.getHeader("referer") : req.getContextPath() + "/shop/products");
        } catch (Exception e) {
            req.getServletContext().log("ProductManageServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(session, "Thao tác thất bại. Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }
}
