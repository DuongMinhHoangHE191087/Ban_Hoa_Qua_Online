package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.PaymentService;
import com.fruitmkt.util.LoggerUtil;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/admin/payments")
public class AdminPaymentServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminPaymentServlet.class.getName());

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
        String keyword = req.getParameter("keyword");
        String pageStr = req.getParameter("page");
        int page = 1;
        try {
            if (pageStr != null) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            LoggerUtil.warn(log, "Tham số page không hợp lệ: " + pageStr, e);
        }

        int pageSize = 20;

        try {
            List<Map<String, Object>> payments = paymentService.getAdminPayments(
                    statusFilter, paymentMethod, keyword, page, pageSize);
            int totalCount = paymentService.countAdminPayments(statusFilter, paymentMethod, keyword);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPages < 1) {
                totalPages = 1;
            }

            req.setAttribute("payments", payments);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("paymentMethod", paymentMethod);
            req.setAttribute("keyword", keyword);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/payment-dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
