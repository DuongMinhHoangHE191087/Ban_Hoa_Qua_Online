package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.PaymentTransaction;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.PaymentService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminOrderServlet — Controller cho Admin quản lý và xác nhận đơn hàng.
 *
 * URL: /admin/orders
 * GET : Danh sách đơn hàng (toàn sàn, filter theo status/payment)
 * POST: Xác nhận thanh toán thủ công (action=confirmPayment)
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private final OrderDAO       orderDAO       = new OrderDAO();
    private final com.fruitmkt.service.OrderService orderService = new com.fruitmkt.service.OrderService();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String statusFilter = req.getParameter("status");
        String pageStr      = req.getParameter("page");
        int page = 1;
        try { if (pageStr != null) page = Integer.parseInt(pageStr); }
        catch (NumberFormatException ignored) {}

        try {
            int pageSize = AppConfig.PAGE_SIZE_ORDERS;
            List<Order> orders = orderDAO.findAll(statusFilter, page, pageSize);
            req.setAttribute("orders",       orders);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("currentPage",  page);

            // Danh sách đơn CK đang chờ xác nhận (cho tab "Chờ duyệt thanh toán")
            List<Order> pendingPayments = orderDAO.findAll(AppConfig.ORDER_PENDING_PAYMENT, 1, 50);
            req.setAttribute("pendingPayments", pendingPayments);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/orders.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action     = req.getParameter("action");
        String orderIdStr = req.getParameter("orderId");
        int orderId;
        try { orderId = Integer.parseInt(orderIdStr); }
        catch (Exception e) {
            SessionUtil.setFlashMessage(req.getSession(), "Order ID không hợp lệ.", "error");
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        try {
            if ("confirmPayment".equals(action)) {
                paymentService.adminApprovePayment(orderId, admin.getUserId());
                SessionUtil.setFlashMessage(req.getSession(),
                    "Đã xác nhận thanh toán cho đơn hàng #" + orderId + ". Đơn chuyển sang CONFIRMED.", "success");

            } else if ("cancelOrder".equals(action)) {
                String reason = req.getParameter("reason");
                List<Order> orders = orderDAO.findById(orderId);
                if (!orders.isEmpty()) {
                    orderService.cancelOrder(orderId, admin.getUserId(), reason);
                    SessionUtil.setFlashMessage(req.getSession(),
                        "Đã hủy đơn hàng #" + orderId + ".", "success");
                }
            } else {
                SessionUtil.setFlashMessage(req.getSession(), "Hành động không hợp lệ.", "error");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi: " + e.getMessage(), "error");
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi hệ thống: " + e.getMessage(), "error");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }
}
