package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AdminOrderServlet — Controller cho chức năng: Monitoring đơn hàng toàn sàn
 *
 * URL: /admin/orders
 * GET : Monitoring đơn hàng toàn sàn
 * POST: Can thiệp đơn bất thường
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/admin/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String status = req.getParameter("status");
            int page = 1;
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try { page = Integer.parseInt(pageStr); } catch (Exception e) {}
            }
            int pageSize = 20;

            java.util.List<com.fruitmkt.model.entity.Order> orders = orderService.getAllOrders(status, page, pageSize);
            int totalRecords = orderService.countAllOrders(status);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            req.setAttribute("orderList", orders);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("paramStatus", status);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải danh sách đơn hàng toàn sàn");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("cancel".equals(action)) {
                int orderId = Integer.parseInt(req.getParameter("orderId"));
                String reason = req.getParameter("reason");
                com.fruitmkt.model.entity.User admin = SessionUtil.getCurrentUser(req.getSession());
                
                orderService.cancelOrder(orderId, admin.getUserId(), "ADMIN CANCEL: " + reason);
                SessionUtil.flashSuccess(req.getSession(), "Đã hủy đơn hàng #" + orderId + " thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Lỗi khi xử lý đơn hàng: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }

}
