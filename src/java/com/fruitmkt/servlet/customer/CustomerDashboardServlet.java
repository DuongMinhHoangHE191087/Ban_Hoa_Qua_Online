package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * CustomerDashboardServlet — Controller cho Bảng điều khiển của Khách hàng.
 * URL: /customer/dashboard
 */
@WebServlet("/customer/dashboard")
public class CustomerDashboardServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_CUSTOMER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // Lấy danh sách đơn hàng gần đây (5 đơn hàng gần nhất)
            List<Order> recentOrders = orderDAO.findByCustomer(currentUser.getUserId(), null, 1, 5);
            
            // Lấy danh sách voucher hệ thống khả dụng
            List<Promotion> activeVouchers = promotionDAO.findActiveSystemPromotions();

            req.setAttribute("recentOrders", recentOrders);
            req.setAttribute("activeVouchers", activeVouchers);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Không thể tải bảng điều khiển: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
