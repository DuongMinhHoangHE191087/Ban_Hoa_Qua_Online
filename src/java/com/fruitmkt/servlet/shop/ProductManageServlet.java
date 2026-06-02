package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

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

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

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

        try {
            // 2. Lấy danh sách sản phẩm chưa bị xóa mềm của shop
            List<Product> rawProducts = productDAO.findByOwner(currentUser.getUserId());
            List<Map<String, Object>> products = new ArrayList<>();

            // 3. Tải danh mục phục vụ hiển thị tên danh mục
            List<Category> categories = categoryDAO.findAll();
            Map<Integer, String> categoryMap = new HashMap<>();
            for (Category c : categories) {
                categoryMap.put(c.getCategoryId(), c.getName());
            }

            for (Product p : rawProducts) {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", p.getProductId());
                map.put("name", p.getName());
                map.put("originCountry", p.getOriginCountry());
                map.put("originRegion", p.getOriginRegion());
                map.put("harvestDate", p.getFormattedHarvestDate());
                map.put("shelfLifeDays", p.getShelfLifeDays());
                map.put("status", p.getStatus());
                map.put("viewCount", p.getViewCount());
                map.put("rating", p.getRating());
                map.put("soldQuantity", p.getSoldQuantity());
                map.put("categoryName", categoryMap.getOrDefault(p.getCategoryId(), "Không xác định"));

                // Lấy ảnh chính thực tế
                ProductImage primaryImg = productImageDAO.findPrimary(p.getProductId());
                String imagePath = null;
                if (primaryImg != null && primaryImg.getFilePath() != null && !primaryImg.getFilePath().trim().isEmpty()) {
                    imagePath = primaryImg.getFilePath().trim().replace('\\', '/');
                }
                
                if (imagePath == null) {
                    imagePath = req.getContextPath() + "/assets/img/placeholder.png";
                } else if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
                    if (!imagePath.startsWith("/")) {
                        imagePath = "/" + imagePath;
                    }
                    imagePath = req.getContextPath() + imagePath;
                }
                map.put("image", imagePath);

                // Lấy thông tin biến thể đại diện để lấy giá và tồn kho
                List<ProductVariant> variants = productVariantDAO.findByProduct(p.getProductId());
                BigDecimal price = BigDecimal.ZERO;
                int stock = 0;
                String unit = "Chưa có";
                if (variants != null && !variants.isEmpty()) {
                    ProductVariant v = variants.get(0);
                    price = v.getPrice();
                    stock = v.getStockQuantity();
                    unit = v.getVariantLabel();
                }
                map.put("price", price);
                map.put("stock", stock);
                map.put("unit", unit);

                products.add(map);
            }

            // 4. Gán thuộc tính vào request
            req.setAttribute("products", products);

            // 5. Forward đến trang danh sách JSP
            req.getRequestDispatcher("/WEB-INF/jsp/shop/product-list.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Có lỗi xảy ra khi tải danh sách sản phẩm.");
            req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "error");
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Mọi hành động ghi nhận qua servlet cụ thể khác (Create/Edit/Status)
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
