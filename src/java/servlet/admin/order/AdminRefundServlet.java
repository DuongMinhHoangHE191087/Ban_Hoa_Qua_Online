package servlet.admin.order;

import config.AppConfig;
import model.entity.auth.User;
import model.entity.order.ReturnRequest;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import service.order.ReturnRequestService;
import util.LoggerUtil;
import util.PaginationUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/refunds")
public class AdminRefundServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminRefundServlet.class.getName());

    private final ReturnRequestService returnRequestService = new ReturnRequestService();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            String status = req.getParameter("status");
            int page = PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.PAGE_SIZE_ADMIN;

            java.util.List<ReturnRequest> requests = returnRequestService.getAllRequests(status, page, pageSize);
            viewEnrichmentService.enrichReturnRequests(requests);
            int totalRecords = returnRequestService.countAllRequests(status);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / pageSize));

            req.setAttribute("requestList", requests);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalItems", totalRecords);
            req.setAttribute("paramStatus", status);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-refunds.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminRefundServlet.class.getName()),
                    "AdminRefundServlet#doGet",
                    "Lỗi khi tải danh sách yêu cầu hoàn trả",
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User admin = SessionUtil.getCurrentUser(req.getSession());
            if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
                return;
            }

            String action = req.getParameter("action"); // "approve" or "reject"
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            String reason = req.getParameter("decisionReason");

            returnRequestService.processRequest(requestId, action, reason, admin.getUserId(), orderId);

            if ("approve".equals(action)) {
                SessionUtil.flashSuccess(req.getSession(), "Đã duyệt yêu cầu hoàn tiền #" + requestId);
            } else if ("process".equals(action)) {
                SessionUtil.flashSuccess(req.getSession(), "Đang xử lý yêu cầu #" + requestId);
            } else if ("complete".equals(action)) {
                SessionUtil.flashSuccess(req.getSession(), "Đã hoàn tiền thành công cho yêu cầu #" + requestId);
            } else {
                SessionUtil.flashSuccess(req.getSession(), "Đã từ chối yêu cầu hoàn tiền #" + requestId);
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi xử lý hoàn tiền requestId=" + req.getParameter("requestId"), e);
            SessionUtil.flashError(req.getSession(), util.ErrorMessageUtil.getUserMessage(e));
        }
        resp.sendRedirect(req.getContextPath() + "/admin/refunds");
    }
}
