package com.fruitmkt.servlet.admin;

import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ReturnRequestService;
import com.fruitmkt.util.SessionUtil;

import com.fruitmkt.util.LoggerUtil;
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

            java.util.List<com.fruitmkt.model.entity.ReturnRequest> requests = returnRequestService.getAllRequests(status, page, pageSize);
            int totalRecords = returnRequestService.countAllRequests(status);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            req.setAttribute("requestList", requests);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("paramStatus", status);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-refunds.jsp").forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi tải danh sách yêu cầu hoàn trả", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải danh sách yêu cầu hoàn trả");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action"); // "approve" or "reject"
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            String reason = req.getParameter("decisionReason");
            
            User admin = SessionUtil.getCurrentUser(req.getSession());
            
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
            SessionUtil.flashError(req.getSession(), "Lỗi xử lý hoàn tiền: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/refunds");
    }
}
