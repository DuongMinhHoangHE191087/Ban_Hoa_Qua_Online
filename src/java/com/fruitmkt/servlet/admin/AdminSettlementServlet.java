package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.SettlementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AdminSettlementServlet — Controller cho chức năng: Confirm và chốt settlement cho shop
 *
 * URL: /admin/settlement
 * GET : Confirm và chốt settlement cho shop
 * POST: Confirm/mark paid settlement
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/admin/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/settlements")
public class AdminSettlementServlet extends HttpServlet {

    private final SettlementService settlementService = new SettlementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String status = req.getParameter("status");
            int page = 1;
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try { page = Integer.parseInt(pageStr); } catch (Exception e) {}
            }
            int pageSize = 20;

            java.util.List<com.fruitmkt.model.entity.ShopSettlement> settlements = settlementService.getAllSettlements(status, page, pageSize);
            int totalRecords = settlementService.countAllSettlements(status);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            req.setAttribute("settlementList", settlements);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("paramStatus", status);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-settlements.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải danh sách đối soát");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("markPaid".equals(action)) {
                int settlementId = Integer.parseInt(req.getParameter("settlementId"));
                settlementService.markPaid(settlementId);
                SessionUtil.flashSuccess(req.getSession(), "Đã đánh dấu Đã Thanh Toán cho đối soát #" + settlementId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Lỗi cập nhật đối soát: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/settlements");
    }

}
