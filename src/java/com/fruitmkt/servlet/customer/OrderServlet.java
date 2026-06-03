package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * OrderServlet — Controller cho chức năng: Lịch sử đơn hàng và chi tiết tracking
 *
 * URL: /orders
 * GET : Lịch sử đơn hàng và chi tiết tracking
 * POST: Hủy đơn hàng
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/customer/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final com.fruitmkt.dao.OrderDAO orderDAO = new com.fruitmkt.dao.OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || (!"CUSTOMER".equals(user.getRole()) && !"SHOP_OWNER".equals(user.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            int pageSize = 10;
            java.util.List<com.fruitmkt.model.entity.Order> list = orderDAO.findByCustomer(user.getUserId(), page, pageSize);
            req.setAttribute("orders", list);
            req.setAttribute("currentPage", page);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/orders.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || (!"CUSTOMER".equals(user.getRole()) && !"SHOP_OWNER".equals(user.getRole()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        String orderIdStr = req.getParameter("orderId");
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        try {
            if ("confirmDelivery".equals(action)) {
                orderService.customerConfirmDelivery(orderId, user.getUserId());
                SessionUtil.setFlashMessage(req.getSession(), "Cảm ơn bạn đã xác nhận nhận hàng thành công!", "success");
            } else if ("cancel".equals(action)) {
                String reason = req.getParameter("reason");
                orderService.cancelOrder(orderId, user.getUserId(), reason);
                SessionUtil.setFlashMessage(req.getSession(), "Bạn đã hủy đơn hàng thành công!", "success");
            }
        } catch (Exception e) {
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi: " + e.getMessage(), "error");
        }
        
        resp.sendRedirect(req.getContextPath() + "/orders");
    }

}
