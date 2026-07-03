package servlet.admin.order;

import config.AppConfig;
import dao.order.OrderDAO;
import model.dto.common.PagedResultDTO;
import model.entity.auth.User;
import model.entity.order.Order;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import util.PaginationUtil;
import util.SessionUtil;
import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * AdminOrderMonitorServlet - Monitor và giám sát tất cả đơn hàng trên sàn.
 * URL: /admin/order-monitor
 */
@WebServlet("/admin/order-monitor")
public class AdminOrderMonitorServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminOrderMonitorServlet.class.getName());

    private final OrderDAO orderDAO = new OrderDAO();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
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
            PagedResultDTO pagedResult = PaginationUtil.buildPagedResult(orders, page, pageSize, totalCount);

            req.setAttribute("pagedResult", pagedResult);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("paymentMethod", paymentMethod);
            req.setAttribute("paymentStatus", paymentStatus);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/order-monitor.jsp").forward(req, resp);
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminOrderMonitorServlet.class.getName()),
                    "AdminOrderMonitorServlet#doGet",
                    "Lỗi hệ thống: " + e.getMessage(),
                    e);
        }
    }
}
