package com.fruitmkt.servlet.guest;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.model.dto.PagedResultDTO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        int page       = parseIntParam(req, "page", 1);
        String keyword = req.getParameter("keyword");
        Integer categoryId = parseIntegerParam(req, "categoryId");
        BigDecimal minPrice = parseDecimalParam(req, "minPrice");
        BigDecimal maxPrice = parseDecimalParam(req, "maxPrice");

        List<Category> categories = Collections.emptyList();
        PagedResultDTO pagedResult = null;

        try {
            categories  = categoryDAO.findAllActive();
            pagedResult = productService.getProductList(page, keyword, categoryId, minPrice, maxPrice);
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
