package com.fruitmkt.servlet.delivery;

import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.DeliveryService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/delivery/dashboard")
public class DeliveryDashboardServlet extends HttpServlet {
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession(false));
        if (currentUser == null || !"DELIVERY".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            req.setAttribute("deliveryList", deliveryService.getDeliveriesForStaff(currentUser.getUserId()));
            req.getRequestDispatcher("/WEB-INF/jsp/delivery/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách đơn hàng cần giao.");
        }
    }
}
