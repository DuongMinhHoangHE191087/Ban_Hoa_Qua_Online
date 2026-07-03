package servlet.admin.system;

import service.auth.UserService;
import service.order.OrderService;
import service.order.ReturnRequestService;
import service.shop.SettlementService;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminDashboardServlet.class.getName());

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
            int confirmedSettlements = settlementService.countAllSettlements("CONFIRMED");
            int openSettlementIssues = settlementService.countOpenPaymentIssues();

            req.setAttribute("totalUsers", totalUsers);
            req.setAttribute("totalOrders", totalOrders);
            req.setAttribute("pendingRefunds", pendingRefunds);
            req.setAttribute("confirmedSettlements", confirmedSettlements);
            req.setAttribute("openSettlementIssues", openSettlementIssues);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminDashboardServlet.class.getName()),
                    "AdminDashboardServlet#doGet",
                    "Lỗi tải dashboard",
                    e);
        }
    }
}
