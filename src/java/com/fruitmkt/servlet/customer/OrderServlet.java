package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.dto.OrderDetailViewDTO;
import com.fruitmkt.model.dto.OrderListViewDTO;
import com.fruitmkt.model.dto.ReorderResultDTO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.service.OrderViewService;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller cho lich su don hang, chi tiet va invoice.
 */
@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final OrderViewService orderViewService = new OrderViewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())
                && !AppConfig.ROLE_SHOP_OWNER.equals(user.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");
        if ("detail".equals(action)) {
            handleDetailView(req, resp, user);
            return;
        }
        if ("invoice".equals(action)) {
            handleInvoiceView(req, resp, user);
            return;
        }

        String status = req.getParameter("status");
        int page = parsePage(req.getParameter("page"));
        try {
            OrderListViewDTO view = orderViewService.getOrderListView(user, status, page);
            req.setAttribute("orders", view.getOrders());
            req.setAttribute("deliveryMap", view.getDeliveryMap());
            req.setAttribute("paymentTxMap", view.getPaymentTransactionMap());
            req.setAttribute("currentPage", view.getCurrentPage());
            req.setAttribute("totalPages", view.getTotalPages());
            req.setAttribute("selectedStatus", view.getSelectedStatus());
            req.getRequestDispatcher("/WEB-INF/jsp/customer/orders.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || (!AppConfig.ROLE_CUSTOMER.equals(user.getRole())
                && !AppConfig.ROLE_SHOP_OWNER.equals(user.getRole()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(req.getParameter("orderId"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("confirmDelivery".equals(action)) {
                orderService.customerConfirmDelivery(orderId, user.getUserId());
                SessionUtil.setFlashMessage(req.getSession(),
                        "Cam on ban da xac nhan nhan hang thanh cong!", "success");
            } else if ("reportNotReceived".equals(action)) {
                orderService.reportNotReceived(orderId, user.getUserId());
                SessionUtil.setFlashMessage(req.getSession(),
                        "Ban da bao cao chua nhan duoc hang. Ban quan tri se tien hanh xac minh don hang.",
                        "warning");
            } else if ("cancel".equals(action)) {
                orderService.cancelOrder(orderId, user.getUserId(), req.getParameter("reason"));
                SessionUtil.setFlashMessage(req.getSession(), "Ban da huy don hang thanh cong!", "success");
            } else if ("reorder".equals(action)) {
                ReorderResultDTO result = orderService.reorder(orderId, user.getUserId());
                if (result.getSkippedCount() > 0) {
                    SessionUtil.setFlashMessage(req.getSession(),
                            "Da them " + result.getAddedCount() + " san pham vao gio hang. "
                                    + result.getSkippedCount() + " san pham khong con kha dung da bi bo qua.",
                            "warning");
                } else {
                    SessionUtil.setFlashMessage(req.getSession(),
                            "Da them " + result.getAddedCount() + " san pham vao gio hang thanh cong!",
                            "success");
                }
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        } catch (Exception e) {
            SessionUtil.setFlashMessage(req.getSession(), "Loi: " + e.getMessage(), "error");
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    private void handleDetailView(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        Integer orderId = parseOrderId(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }
        try {
            OrderDetailViewDTO view = orderViewService.getOrderDetailView(user, orderId);
            if (view != null && view.getOrder() != null) {
                req.setAttribute("order", view.getOrder());
                req.setAttribute("orderItems", view.getOrderItems());
                req.setAttribute("paymentTx", view.getPaymentTransaction());
                req.setAttribute("delivery", view.getDelivery());
                req.getRequestDispatcher("/WEB-INF/jsp/customer/order-detail.jsp").forward(req, resp);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    private void handleInvoiceView(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        Integer orderId = parseOrderId(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }
        try {
            OrderDetailViewDTO view = orderViewService.getInvoiceView(user, orderId);
            if (view != null && view.getOrder() != null) {
                req.setAttribute("order", view.getOrder());
                req.setAttribute("orderItems", view.getOrderItems());
                req.getRequestDispatcher("/WEB-INF/jsp/customer/invoice.jsp").forward(req, resp);
                return;
            }
            SessionUtil.setFlashMessage(req.getSession(),
                    "Hoa don dien tu chi kha dung khi don hang da giao thanh cong.", "warning");
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.sendRedirect(req.getContextPath() + "/orders");
    }

    private int parsePage(String pageStr) {
        if (pageStr == null || pageStr.trim().isEmpty()) {
            return 1;
        }
        try {
            return Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Integer parseOrderId(String orderIdStr) {
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
