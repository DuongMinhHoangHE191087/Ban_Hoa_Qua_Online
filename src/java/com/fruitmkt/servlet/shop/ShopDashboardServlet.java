package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.service.SettlementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

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

    // TODO: Inject service — thêm service cần dùng ở đây
    // private final XxxService xxxService = new XxxService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendError(403);
            return;
        }

        // Tạm thời chỉ hiển thị giao diện mẫu, chưa tính toán số liệu thật
        req.setAttribute("revenue", 0);
        req.setAttribute("orderCount", 0);
        req.setAttribute("lowStock", 0);

        req.getRequestDispatcher("/WEB-INF/jsp/shop/dashboard.jsp").forward(req, resp);
    }

}
