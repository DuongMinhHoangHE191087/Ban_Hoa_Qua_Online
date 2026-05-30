package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * ShopDashboardServlet — Controller cho chức năng: Dashboard tổng quan vận hành shop
 *
 * URL: /shop/dashboard
 * GET : Dashboard tổng quan vận hành shop
 * POST: -
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/dashboard")
public class ShopDashboardServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        if (!SessionUtil.isLoggedIn(session) || !SessionUtil.hasRole(session, AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User user = SessionUtil.getCurrentUser(session);
        List<ProductVariant> lowStock = Collections.emptyList();

        try {
            lowStock = productService.getLowStockAlerts(user.getUserId());
        } catch (SQLException e) {
            req.getServletContext().log("ShopDashboardServlet DB error: " + e.getMessage(), e);
        }

        // Mock additional KPI metrics for beautiful dashboard visualization
        req.setAttribute("revenue", new java.math.BigDecimal("15250000"));
        req.setAttribute("orderCount", 42);
        req.setAttribute("lowStock", lowStock);
        req.setAttribute("settlementStatus", "Chờ đối soát");

        req.getRequestDispatcher("/WEB-INF/jsp/shop/dashboard.jsp").forward(req, resp);
    }
}
