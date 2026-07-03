package servlet.shop.product;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.catalog.ProductDAO;
import dao.catalog.ProductImageDAO;
import dao.catalog.ProductVariantDAO;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.catalog.ProductImage;
import model.entity.catalog.ProductVariant;
import model.entity.auth.User;
import model.dto.common.PagedResultDTO;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ProductManageServlet — Controller hiển thị danh sách sản phẩm của shop
 *
 * URL: /shop/products
 * GET : Danh sách sản phẩm của shop
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/products")
public class ProductManageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ProductManageServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final service.catalog.ProductService productService = new service.catalog.ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra session/quyền chủ cửa hàng
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String keyword = req.getParameter("keyword");
        String categoryIdStr = req.getParameter("categoryId");
        String stockStatus = req.getParameter("stockStatus");
        String filterApprovalStatus = req.getParameter("approvalStatus");
        String sellStatus = req.getParameter("sellStatus");

        try {
            int page = util.PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.DEFAULT_PAGE_SIZE;

            Integer categoryId = null;
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                try {
                    categoryId = Integer.parseInt(categoryIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            PagedResultDTO pagedResult = productService.getProductsByOwner(
                    currentUser.getUserId(), page, pageSize, keyword, categoryId, filterApprovalStatus, sellStatus, stockStatus);

            @SuppressWarnings("unchecked")
            List<Product> rawProducts = (List<Product>) pagedResult.getItems();
            List<Map<String, Object>> products = new ArrayList<>();

            // 3. Tải danh mục một lần rồi tách ra map hiển thị + danh sách active cho modal
            List<Category> categories = categoryDAO.findAll();
            Map<Integer, String> categoryMap = new HashMap<>();
            List<Category> activeCategories = new ArrayList<>();
            for (Category c : categories) {
                categoryMap.put(c.getCategoryId(), c.getName());
                if (c.getIsActive()) {
                    activeCategories.add(c);
                }
            }
            req.setAttribute("categories", activeCategories);

            List<Integer> productIds = new ArrayList<>();
            for (Product p : rawProducts) {
                productIds.add(p.getProductId());
            }
            Map<Integer, ProductImage> primaryImages = productImageDAO.findPrimaryByProductIds(productIds);
            Map<Integer, List<ProductVariant>> variantsByProduct = productVariantDAO.findByProductIds(productIds);

            for (Product p : rawProducts) {
                List<ProductVariant> variants = variantsByProduct.get(p.getProductId());
                products.add(buildManageProductCard(req, p, categoryMap,
                        primaryImages.get(p.getProductId()),
                        variants));
            }

            // 4. Gán thuộc tính vào request
            req.setAttribute("keyword", keyword);
            req.setAttribute("categoryId", categoryIdStr);
            req.setAttribute("stockStatus", stockStatus);
            req.setAttribute("approvalStatus", filterApprovalStatus);
            req.setAttribute("sellStatus", sellStatus);
            req.setAttribute("products", products);
            req.setAttribute("currentPage", pagedResult.getCurrentPage());
            req.setAttribute("totalPages", pagedResult.getTotalPages());
            req.setAttribute("totalItems", pagedResult.getTotalItems());

            // 5. Forward đến trang danh sách JSP
            req.getRequestDispatcher("/WEB-INF/jsp/shop/product-list.jsp").forward(req, resp);

        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi tải danh sách sản phẩm", e);
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Có lỗi xảy ra khi tải danh sách sản phẩm.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        }
    }

    private Map<String, Object> buildManageProductCard(HttpServletRequest req, Product product,
                                                       Map<Integer, String> categoryMap,
                                                       ProductImage primaryImage,
                                                       List<ProductVariant> variants) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", product.getProductId());
        map.put("name", product.getName());
        map.put("originCountry", product.getOriginCountry());
        map.put("originRegion", product.getOriginRegion());
        map.put("harvestDate", product.getFormattedHarvestDate());
        map.put("shelfLifeDays", product.getShelfLifeDays());
        map.put("status", product.getStatus());
        map.put("viewCount", product.getViewCount());
        map.put("rating", product.getRating());
        map.put("soldQuantity", product.getSoldQuantity());
        map.put("approvalStatus", product.getApprovalStatus());
        map.put("rejectionReason", product.getRejectionReason());
        map.put("categoryName", categoryMap.getOrDefault(product.getCategoryId(), "Không xác định"));
        map.put("image", resolveImagePath(req, primaryImage));

        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;
        int totalStock = 0;
        String unitDisplay = "Chưa có";
        if (variants != null && !variants.isEmpty()) {
            minPrice = variants.get(0).getPrice();
            maxPrice = variants.get(variants.size() - 1).getPrice();
            for (ProductVariant variant : variants) {
                totalStock += variant.getStockQuantity();
            }
            if (variants.size() == 1) {
                unitDisplay = variants.get(0).getVariantLabel();
            } else {
                unitDisplay = variants.size() + " phân loại";
            }
        }
        map.put("price", minPrice);
        map.put("minPrice", minPrice);
        map.put("maxPrice", maxPrice);
        map.put("hasMultipleVariants", variants != null && variants.size() > 1);
        map.put("stock", totalStock);
        map.put("unit", unitDisplay);
        return map;
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Mọi hành động ghi nhận qua servlet cụ thể khác (Create/Edit/Status)
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
