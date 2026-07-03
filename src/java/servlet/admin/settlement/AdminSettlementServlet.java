package servlet.admin.settlement;

import config.AppConfig;
import model.entity.auth.User;
import model.entity.shop.ShopSettlement;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import service.shop.SettlementService;
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

/**
 * AdminSettlementServlet - Controller cho chức năng đối soát / chốt settlement cho shop.
 */
@WebServlet("/admin/settlements")
public class AdminSettlementServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminSettlementServlet.class.getName());

    private final SettlementService settlementService = new SettlementService();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            String status = req.getParameter("status");
            String issueFilter = req.getParameter("issueFilter");
            int page = PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.PAGE_SIZE_ADMIN;

            java.util.List<ShopSettlement> settlements = settlementService.getAllSettlements(status, issueFilter, page, pageSize);
            viewEnrichmentService.enrichSettlements(settlements);
            int totalRecords = settlementService.countAllSettlements(status, issueFilter);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / pageSize));

            req.setAttribute("settlementList", settlements);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalItems", totalRecords);
            req.setAttribute("paramStatus", status);
            req.setAttribute("paramIssueFilter", issueFilter);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-settlements.jsp").forward(req, resp);
        } catch (Exception e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminSettlementServlet.class.getName()),
                    "AdminSettlementServlet#doGet",
                    "Lỗi khi tải danh sách đối soát",
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req.getSession());
        if (admin == null || !AppConfig.ROLE_ADMIN.equals(admin.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            String action = req.getParameter("action");
            if ("markPaid".equals(action)) {
                int settlementId = Integer.parseInt(req.getParameter("settlementId"));
                String paidReference = req.getParameter("paidReference");
                String paidNote = req.getParameter("paidNote");
                settlementService.markPaid(settlementId, admin.getUserId(), paidReference, paidNote);
                SessionUtil.flashSuccess(req.getSession(), "Đã xác nhận thanh toán cho đối soát #" + settlementId);
            } else if ("resolveIssue".equals(action)) {
                int settlementId = Integer.parseInt(req.getParameter("settlementId"));
                String resolutionNote = req.getParameter("resolutionNote");
                settlementService.resolvePaymentIssue(settlementId, admin.getUserId(), resolutionNote);
                SessionUtil.flashSuccess(req.getSession(), "Đã chốt đối soát lại cho settlement #" + settlementId);
            } else if ("reopenPayment".equals(action)) {
                int settlementId = Integer.parseInt(req.getParameter("settlementId"));
                String resolutionNote = req.getParameter("resolutionNote");
                settlementService.reopenPaymentRetry(settlementId, admin.getUserId(), resolutionNote);
                SessionUtil.flashSuccess(req.getSession(), "Đã mở lại settlement #" + settlementId + " để kiểm tra thanh toán.");
            } else if ("triggerSettlement".equals(action)) {
                int processed = settlementService.runAutoSettlement();
                if (processed > 0) {
                    SessionUtil.flashSuccess(req.getSession(), "Kích hoạt đối soát tự động thành công! Đã chốt thêm " + processed + " kỳ đối soát mới.");
                } else {
                    SessionUtil.flashSuccess(req.getSession(), "Kích hoạt đối soát thành công! Không tìm thấy đơn hàng mới nào đủ điều kiện đóng băng/quyết toán.");
                }
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi cập nhật đối soát", e);
            SessionUtil.flashError(req.getSession(), "Lỗi cập nhật đối soát: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/settlements");
    }

}
