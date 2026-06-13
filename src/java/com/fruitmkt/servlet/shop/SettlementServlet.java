package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.SettlementService;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.ShopSettlement;

import com.fruitmkt.util.LoggerUtil;
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
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc không có quyền.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/auth/login");
            }
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
                    List<com.fruitmkt.model.entity.ShopSettlementOrder> orders = settlementService.getOrdersBySettlementId(settlementId);
                    com.fruitmkt.util.JsonUtil.writeJson(resp, orders);
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không tìm thấy hoặc không có quyền truy cập đối soát này.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống: " + e.getMessage());
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
}
