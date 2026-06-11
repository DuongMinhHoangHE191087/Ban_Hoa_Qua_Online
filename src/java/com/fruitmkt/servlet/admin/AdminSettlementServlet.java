package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.SettlementService;

import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(AdminSettlementServlet.class.getName());

    private final SettlementService settlementService = new SettlementService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String status = req.getParameter("status");
            int page = 1;
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try { page = Integer.parseInt(pageStr); } catch (NumberFormatException e) {
                    LoggerUtil.warn(log, "Tham số page không hợp lệ: " + pageStr, e);
                }
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
            LoggerUtil.error(log, "Lỗi khi tải danh sách đối soát", e);
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
