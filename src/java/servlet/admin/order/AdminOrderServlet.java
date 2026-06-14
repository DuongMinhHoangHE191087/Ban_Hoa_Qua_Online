package servlet.admin.order;
import service.order.OrderService;

import config.AppConfig;
import dao.order.OrderDAO;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import model.entity.auth.User;
import service.shop.PaymentService;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(AdminOrderServlet.class.getName());

    private final OrderDAO       orderDAO       = new OrderDAO();
    private final service.order.OrderService orderService = new service.order.OrderService();
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
        String paymentMethod = req.getParameter("paymentMethod");
        String paymentStatus = req.getParameter("paymentStatus");
        String pageStr      = req.getParameter("page");
        int page = 1;
        try { if (pageStr != null) page = Integer.parseInt(pageStr); }
        catch (NumberFormatException e) {
            LoggerUtil.warn(log, "Tham số page không hợp lệ: " + pageStr, e);
        }

        try {
            int pageSize = AppConfig.PAGE_SIZE_ORDERS;
            List<Order> orders = orderDAO.findAll(statusFilter, paymentMethod, paymentStatus, page, pageSize);
            int totalCount = orderDAO.countAll(statusFilter, paymentMethod, paymentStatus);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPages < 1) totalPages = 1;
            
            req.setAttribute("orders",        orders);
            req.setAttribute("statusFilter",  statusFilter);
            req.setAttribute("paymentMethod", paymentMethod);
            req.setAttribute("paymentStatus", paymentStatus);
            req.setAttribute("currentPage",   page);
            req.setAttribute("totalPages",    totalPages);

            // Fetch payment transactions mapping for main orders
            java.util.Map<Integer, PaymentTransaction> txMap = new java.util.HashMap<>();
            for (Order order : orders) {
                PaymentTransaction tx = paymentService.getPaymentByOrder(order.getOrderId());
                if (tx != null) {
                    txMap.put(order.getOrderId(), tx);
                }
            }
            req.setAttribute("txMap", txMap);

            // Danh sách đơn CK đang chờ xác nhận (cho tab "Chờ duyệt thanh toán")
            List<Order> pendingPayments = orderDAO.findAll(AppConfig.ORDER_PENDING_PAYMENT, "CK", null, 1, 50);
            req.setAttribute("pendingPayments", pendingPayments);

            // Fetch payment transactions mapping for pending payments
            java.util.Map<Integer, PaymentTransaction> pendingTxMap = new java.util.HashMap<>();
            for (Order order : pendingPayments) {
                PaymentTransaction tx = paymentService.getPaymentByOrder(order.getOrderId());
                if (tx != null) {
                    pendingTxMap.put(order.getOrderId(), tx);
                }
            }
            req.setAttribute("pendingTxMap", pendingTxMap);

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
            LoggerUtil.error(log, "Lỗi hệ thống khi xử lý đơn hàng #" + req.getParameter("orderId"), e);
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi hệ thống: " + e.getMessage(), "error");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }
}
