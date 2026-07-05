package servlet.shop.settlement;

import config.AppConfig;
import util.SessionUtil;
import service.shop.SettlementService;
import model.entity.auth.User;
import model.entity.shop.ShopSettlement;

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
 * SettlementServlet — Controller cho chức năng: Xem settlement summary và chi tiết kỳ
 *
 * URL: /shop/settlement
 * GET : Xem settlement summary và chi tiết kỳ
 * POST: -
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/settlement")
public class SettlementServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(SettlementServlet.class.getName());

    private final SettlementService settlementService = new SettlementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = requireShopOwner(req, resp, session);
        if (currentUser == null) {
            return;
        }

        String action = req.getParameter("action");
        if ("getOrders".equals(action)) {
            try {
                String idStr = req.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã đối soát.");
                    return;
                }
                int settlementId = Integer.parseInt(idStr);
                ShopSettlement settlement = settlementService.getSettlementById(settlementId);
                
                // Security check: ensure the settlement belongs to the current shop owner
                if (settlement != null && settlement.getOwnerId() == currentUser.getUserId()) {
                    List<model.entity.shop.ShopSettlementOrder> orders = settlementService.getOrdersBySettlementId(settlementId);
                    util.JsonUtil.writeJson(resp, orders);
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không tìm thấy hoặc không có quyền truy cập đối soát này.");
                }
            } catch (Exception e) {
                util.ServletUtil.sendPageInternalServerError(
                        req,
                        resp,
                        java.util.logging.Logger.getLogger(SettlementServlet.class.getName()),
                        "SettlementServlet#doGet",
                        "Lỗi hệ thống: " + e.getMessage(),
                        e);
            }
            return;
        }

        // Default: List all settlements
        resp.setContentType("text/html;charset=UTF-8");
        try {
            List<ShopSettlement> settlements = settlementService.getSettlementsByOwner(currentUser.getUserId());
            req.setAttribute("settlements", settlements);

            // Calculate summaries in Java to avoid EL arithmetic and type coercion issues in JSP
            java.math.BigDecimal totalGross = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalFee = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalRefund = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalNet = java.math.BigDecimal.ZERO;
            if (settlements != null) {
                for (ShopSettlement s : settlements) {
                    if (s.getGrossAmount() != null) totalGross = totalGross.add(s.getGrossAmount());
                    if (s.getPlatformFeeAmount() != null) totalFee = totalFee.add(s.getPlatformFeeAmount());
                    if (s.getRefundAmount() != null) totalRefund = totalRefund.add(s.getRefundAmount());
                    if (s.getNetAmount() != null) totalNet = totalNet.add(s.getNetAmount());
                }
            }
            req.setAttribute("totalGross", totalGross);
            req.setAttribute("totalFee", totalFee);
            req.setAttribute("totalRefund", totalRefund);
            req.setAttribute("totalNet", totalNet);

            req.getRequestDispatcher("/WEB-INF/jsp/shop/settlement.jsp").forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi tải lịch sử đối soát", e);
            SessionUtil.flashError(session, "Lỗi khi tải lịch sử đối soát: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = requireShopOwner(req, resp, session);
        if (currentUser == null) {
            return;
        }

        String action = req.getParameter("action");
        String settlementIdStr = req.getParameter("settlementId");
        if (settlementIdStr == null || settlementIdStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Thiếu mã settlement.");
            resp.sendRedirect(req.getContextPath() + "/shop/settlement");
            return;
        }

        try {
            int settlementId = Integer.parseInt(settlementIdStr);
            if ("confirm".equals(action)) {
                settlementService.confirmSettlement(settlementId, currentUser.getUserId(), req.getParameter("confirmNote"));
                SessionUtil.flashSuccess(session, "Đã xác nhận settlement #" + settlementId + ". Vui lòng chờ admin chuyển khoản.");
            } else if ("dispute".equals(action)) {
                settlementService.disputeSettlement(settlementId, currentUser.getUserId(), req.getParameter("cancelReason"));
                SessionUtil.flashSuccess(session, "Đã ghi nhận settlement #" + settlementId + " là có tranh chấp / hủy.");
            } else if ("reportUnreceived".equals(action)) {
                settlementService.reportPaymentIssue(settlementId, currentUser.getUserId(), req.getParameter("issueNote"));
                SessionUtil.flashSuccess(session, "Đã báo settlement #" + settlementId + " là chưa nhận được tiền. Admin sẽ kiểm tra đối soát.");
            } else {
                SessionUtil.flashError(session, "Hành động không hợp lệ.");
            }
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Mã settlement không hợp lệ.");
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi xử lý settlement của shop", e);
            SessionUtil.flashError(session, e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/shop/settlement");
    }

    private User requireShopOwner(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws IOException {
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc không có quyền.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
            }
            return null;
        }
        return currentUser;
    }
}
