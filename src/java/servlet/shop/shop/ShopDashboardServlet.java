package servlet.shop.shop;
import service.catalog.ProductService;

import config.AppConfig;
import util.SessionUtil;
import service.order.OrderService;
import service.shop.ShopService;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ShopDashboardServlet — Controller cho chức năng: Dashboard tổng quan vận hành shop
 *
 * URL: /shop/dashboard
 * GET : Dashboard tổng quan vận hành shop
 * POST: -
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/shop/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/dashboard")
public class ShopDashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopDashboardServlet.class.getName());

    private final OrderService orderService = new OrderService();
    private final ProductService productService = new ProductService();
    private final ShopService shopService = new ShopService();
    private final service.chat.ChatService chatService = new service.chat.ChatService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendError(403);
            return;
        }

        int ownerId = user.getUserId();

        // Load shop profile để lấy ngưỡng cảnh báo cá nhân
        int lowStockThreshold = 10; // fallback default
        try {
            model.entity.shop.ShopProfile profile = shopService.getShopByUserId(ownerId);
            if (profile != null && profile.getLowStockThreshold() > 0) {
                lowStockThreshold = profile.getLowStockThreshold();
            }
        } catch (Exception e) {
            LoggerUtil.warn(log, "[Dashboard] Không load được shopProfile cho ownerId=%d, dùng threshold mặc định=%d", ownerId, lowStockThreshold);
        }

        try {
            java.math.BigDecimal revenue = orderService.getRevenueByOwner(ownerId);
            java.math.BigDecimal estimatedRevenue = orderService.getEstimatedRevenueByOwner(ownerId);
            int orderCount = orderService.getOrderCountByOwner(ownerId);
            int lowStock = productService.getLowStockCountByOwner(ownerId, lowStockThreshold);
            java.util.List<java.util.Map<String, Object>> lowStockVariants =
                    productService.getLowStockVariantsByOwner(ownerId, lowStockThreshold);
            java.util.List<model.entity.order.Order> recentOrders = orderService.getRecentOrdersByOwner(ownerId, 5);

            int unreadMessagesCount = 0;
            try {
                unreadMessagesCount = chatService.countTotalUnread(ownerId);
            } catch (Exception e) {
                // Ignore chat query issues
            }

            java.util.List<model.entity.catalog.Product> recentProducts = new java.util.ArrayList<>();
            try {
                java.util.List<model.entity.catalog.Product> allProducts = productService.getProductsByOwner(ownerId);
                if (allProducts != null) {
                    if (allProducts.size() > 5) {
                        recentProducts = allProducts.subList(0, 5);
                    } else {
                        recentProducts = allProducts;
                    }
                }
            } catch (Exception e) {
                // Ignore product query issues
            }

            req.setAttribute("revenue", revenue);
            req.setAttribute("estimatedRevenue", estimatedRevenue);
            req.setAttribute("orderCount", orderCount);
            req.setAttribute("lowStock", lowStock);
            req.setAttribute("lowStockVariants", lowStockVariants);
            req.setAttribute("lowStockThreshold", lowStockThreshold);
            req.setAttribute("recentOrders", recentOrders);
            req.setAttribute("unreadMessagesCount", unreadMessagesCount);
            req.setAttribute("recentProducts", recentProducts);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải dữ liệu dashboard shop", e);
            req.setAttribute("revenue", java.math.BigDecimal.ZERO);
            req.setAttribute("estimatedRevenue", java.math.BigDecimal.ZERO);
            req.setAttribute("orderCount", 0);
            req.setAttribute("lowStock", 0);
            req.setAttribute("lowStockVariants", new java.util.ArrayList<>());
            req.setAttribute("lowStockThreshold", lowStockThreshold);
            req.setAttribute("recentOrders", new java.util.ArrayList<>());
            req.setAttribute("unreadMessagesCount", 0);
            req.setAttribute("recentProducts", new java.util.ArrayList<>());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/shop/dashboard.jsp").forward(req, resp);
    }
}
