package servlet.admin.order;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.order.OrderDAO;
import model.entity.auth.User;
import model.entity.order.Order;
import model.entity.shop.PaymentTransaction;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import service.order.DeliveryService;
import service.order.OrderService;
import service.shop.PaymentService;
import util.LoggerUtil;
import util.PaginationUtil;
import util.SessionUtil;
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
 * AdminOrderServlet - Controller cho Admin quản lý và xác nhận đơn hàng.
 *
 * URL: /admin/orders
 * GET : Danh sách đơn hàng toàn sàn, filter theo status/payment
 * POST: Xác nhận thanh toán thủ công (action=confirmPayment)
 */
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminOrderServlet.class.getName());

    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderService orderService = new OrderService();
    private final DeliveryService deliveryService = new DeliveryService();
    private final PaymentService paymentService = new PaymentService();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

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

        try {
            int page = PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.PAGE_SIZE_ORDERS;

            List<Order> orders = orderDAO.findAll(statusFilter, paymentMethod, paymentStatus, page, pageSize);
            viewEnrichmentService.enrichOrders(orders);
            int totalCount = orderDAO.countAll(statusFilter, paymentMethod, paymentStatus);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));

            req.setAttribute("orders", orders);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("paymentMethod", paymentMethod);
            req.setAttribute("paymentStatus", paymentStatus);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalItems", totalCount);

            List<Integer> orderIds = new java.util.ArrayList<>();
            for (Order order : orders) {
                orderIds.add(order.getOrderId());
            }
            java.util.Map<Integer, PaymentTransaction> txMap = paymentService.getPaymentMapByOrderIds(orderIds);
            req.setAttribute("txMap", txMap);

            List<Order> pendingPayments = orderDAO.findAll(AppConfig.ORDER_PENDING_PAYMENT, "CK", null, 1, 50);
            viewEnrichmentService.enrichOrders(pendingPayments);
            req.setAttribute("pendingPayments", pendingPayments);

            List<Integer> pendingOrderIds = new java.util.ArrayList<>();
            for (Order order : pendingPayments) {
                pendingOrderIds.add(order.getOrderId());
            }
            java.util.Map<Integer, PaymentTransaction> pendingTxMap = paymentService.getPaymentMapByOrderIds(pendingOrderIds);
            req.setAttribute("pendingTxMap", pendingTxMap);

            List<User> deliveryStaff = userDAO.searchUsers(AppConfig.ROLE_DELIVERY, null, 0, 100);
            req.setAttribute("deliveryStaff", deliveryStaff);

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

        String action = req.getParameter("action");
        String orderIdStr = req.getParameter("orderId");
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (Exception e) {
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
                orderService.cancelOrder(orderId, admin.getUserId(), reason);
                SessionUtil.setFlashMessage(req.getSession(),
                        "Đã hủy đơn hàng #" + orderId + ".", "success");

            } else if ("assignDelivery".equals(action)) {
                String shipperIdStr = req.getParameter("shipperId");
                int shipperId;
                try {
                    shipperId = Integer.parseInt(shipperIdStr);
                } catch (Exception e) {
                    SessionUtil.setFlashMessage(req.getSession(), "Shipper ID không hợp lệ.", "error");
                    resp.sendRedirect(req.getContextPath() + "/admin/orders");
                    return;
                }
                deliveryService.assignShipper(orderId, shipperId, null);
                SessionUtil.setFlashMessage(req.getSession(),
                        "Đã chỉ định shipper cho đơn hàng #" + orderId + ".", "success");

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
