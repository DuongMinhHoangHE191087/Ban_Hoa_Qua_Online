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

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");
        if ("detail".equals(action)) {
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr != null) {
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    com.fruitmkt.model.entity.Order order = orderDAO.findByIdForCustomer(orderId, user.getUserId());
                    if (order == null && "SHOP_OWNER".equals(user.getRole())) {
                        order = orderDAO.findByIdForOwner(orderId, user.getUserId());
                    }
                    if (order != null) {
                        req.setAttribute("order", order);
                        req.setAttribute("orderItems", orderService.getOrderItems(orderId));
                        // Get delivery tracking info if any
                        try {
                            com.fruitmkt.dao.DeliveryDAO deliveryDAO = new com.fruitmkt.dao.DeliveryDAO();
                            req.setAttribute("delivery", deliveryDAO.findByOrderId(orderId));
                        } catch (Exception ignored) {}
                        
                        req.getRequestDispatcher("/WEB-INF/jsp/customer/order-detail.jsp").forward(req, resp);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        if ("invoice".equals(action)) {
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr != null) {
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    com.fruitmkt.model.entity.Order order = orderDAO.findByIdForCustomer(orderId, user.getUserId());
                    if (order == null && "SHOP_OWNER".equals(user.getRole())) {
                        order = orderDAO.findByIdForOwner(orderId, user.getUserId());
                    }
                    if (order != null) {
                        // Enforce DELIVERED (Completed) status check for invoice
                        if ("DELIVERED".equals(order.getStatus())) {
                            req.setAttribute("order", order);
                            req.setAttribute("orderItems", orderService.getOrderItems(orderId));
                            req.getRequestDispatcher("/WEB-INF/jsp/customer/invoice.jsp").forward(req, resp);
                            return;
                        } else {
                            SessionUtil.setFlashMessage(req.getSession(), "Hóa đơn điện tử chỉ khả dụng khi đơn hàng đã giao thành công.", "warning");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        // Default list view with status filter
        String status = req.getParameter("status");
        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            int pageSize = 10;
            java.util.List<com.fruitmkt.model.entity.Order> list = orderDAO.findByCustomer(user.getUserId(), status, page, pageSize);
            int totalCount = orderDAO.countByCustomer(user.getUserId(), status);
            req.setAttribute("orders", list);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", (int) Math.ceil((double) totalCount / pageSize));
            req.setAttribute("selectedStatus", status);
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
                // [FIX B6] RBAC: chỉ chủ đơn mới được confirm
                com.fruitmkt.model.entity.Order ord = orderDAO.findByIdForCustomer(orderId, user.getUserId());
                if (ord == null) {
                    resp.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
                    return;
                }
                orderService.customerConfirmDelivery(orderId, user.getUserId());
                SessionUtil.setFlashMessage(req.getSession(), "Cảm ơn bạn đã xác nhận nhận hàng thành công!", "success");
            } else if ("cancel".equals(action)) {
                // [FIX B6] RBAC: chỉ chủ đơn mới được hủy
                com.fruitmkt.model.entity.Order ord = orderDAO.findByIdForCustomer(orderId, user.getUserId());
                if (ord == null) {
                    resp.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền hủy đơn hàng này.");
                    return;
                }
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
