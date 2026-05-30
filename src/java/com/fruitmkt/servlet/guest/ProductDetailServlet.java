package com.fruitmkt.servlet.guest;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.dao.ProductVariantDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * ProductDetailServlet — Controller cho chức năng: Chi tiết sản phẩm và variant
 *
 * URL: /products/detail
 * GET : Chi tiết sản phẩm và variant
 * POST: -
 *
 * @author fruitmkt-team
 */
@WebServlet("/products/detail")
public class ProductDetailServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        
        String rawId = req.getParameter("id");
        if (rawId == null || rawId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        try {
            int productId = Integer.parseInt(rawId.trim());
            Product product = productService.getProductDetail(productId);
            List<ProductVariant> variants = productVariantDAO.findByProduct(productId);
            List<Product> recommendations = productService.getRecommendations(productId, product.getCategoryId(), product.getOwnerId());

            req.setAttribute("product", product);
            req.setAttribute("variants", variants);
            req.setAttribute("recommendations", recommendations);
            
            req.getRequestDispatcher("/WEB-INF/jsp/guest/product-detail.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
        } catch (SQLException e) {
            req.getServletContext().log("ProductDetailServlet DB error: " + e.getMessage(), e);
            req.setAttribute("errorMsg", "Không thể tải chi tiết sản phẩm. Vui lòng thử lại sau.");
            req.getRequestDispatcher("/WEB-INF/jsp/guest/product-list.jsp").forward(req, resp);
        }
    }
}
