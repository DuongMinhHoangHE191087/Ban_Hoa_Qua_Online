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
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            List<ShopSettlement> settlements = settlementService.getSettlementsByOwner(currentUser.getUserId());
            req.setAttribute("settlements", settlements);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/settlement.jsp").forward(req, resp);
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi khi tải lịch sử đối soát", e);
            SessionUtil.flashError(session, "Lỗi khi tải lịch sử đối soát: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        }
    }
}
