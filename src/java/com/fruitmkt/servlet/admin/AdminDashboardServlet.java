package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.UserService;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.service.ReturnRequestService;
import com.fruitmkt.service.SettlementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final ReturnRequestService returnRequestService = new ReturnRequestService();
    private final SettlementService settlementService = new SettlementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int totalUsers = userService.countUsers(null, null);
            int totalOrders = orderService.countAllOrders(null);
            int pendingRefunds = returnRequestService.countAllRequests("REQUESTED"); // Assuming "REQUESTED" is pending status
            int unpaidSettlements = settlementService.countAllSettlements("PENDING");

            req.setAttribute("totalUsers", totalUsers);
            req.setAttribute("totalOrders", totalOrders);
            req.setAttribute("pendingRefunds", pendingRefunds);
            req.setAttribute("unpaidSettlements", unpaidSettlements);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải dashboard");
        }
    }
}
