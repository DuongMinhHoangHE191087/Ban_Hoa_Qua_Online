package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.ReturnRequest;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.service.ReturnService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * ReturnRequestServlet — Quản lý yêu cầu Hủy/Đổi/Trả của khách hàng và Phán quyết của Admin.
 * 
 * URL: /returns
 */
@WebServlet("/returns")
public class ReturnRequestServlet extends HttpServlet {

    private final ReturnService returnService = new ReturnService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");

        // --- Admin view list of return requests ---
        if ("list".equals(action)) {
            if (!AppConfig.ROLE_ADMIN.equals(user.getRole()) && !AppConfig.ROLE_SHOP_OWNER.equals(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem trang này.");
                return;
            }
            try {
                List<ReturnRequest> list;
                if (AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                    list = returnService.getAllRequests();
                } else {
                    list = returnService.getRequestsByOwner(user.getUserId());
                }
                req.setAttribute("returnRequests", list);
                
                if (AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                    req.getRequestDispatcher("/WEB-INF/jsp/admin/return-requests.jsp").forward(req, resp);
                } else {
                    req.getRequestDispatcher("/WEB-INF/jsp/shop/return-requests.jsp").forward(req, resp);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi truy vấn dữ liệu.");
            }
            return;
        }

        // --- Customer creates return request form ---
        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            Order order = orderService.getOrderDetail(orderId);
            if (order == null) {
                SessionUtil.flashError(session, "Không tìm thấy đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            // Guard: chỉ customer sở hữu đơn mới được gửi yêu cầu đổi trả
            if (order.getCustomerId() != user.getUserId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
                return;
            }

            // Guard: chỉ đơn hàng đã giao thành công (DELIVERED) mới được gửi yêu cầu đổi trả
            if (!"DELIVERED".equals(order.getStatus())) {
                SessionUtil.flashError(session, "Chỉ có đơn hàng giao thành công mới được yêu cầu đổi trả / hoàn tiền.");
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }

            List<OrderItem> items = orderService.getOrderItems(orderId);
            req.setAttribute("order", order);
            req.setAttribute("orderItems", items);

            req.getRequestDispatcher("/WEB-INF/jsp/customer/return-request.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");

        // --- Admin phán quyết duyệt/từ chối ---
        if ("decide".equals(action)) {
            if (!AppConfig.ROLE_ADMIN.equals(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin mới có quyền phê duyệt đổi trả.");
                return;
            }

            String reqIdStr = req.getParameter("requestId");
            String status = req.getParameter("status"); // APPROVED hoặc REJECTED
            String reason = req.getParameter("reason");

            if (reqIdStr == null || status == null || reason == null || reason.trim().isEmpty()) {
                SessionUtil.flashError(session, "Vui lòng nhập đầy đủ thông tin quyết định.");
                resp.sendRedirect(req.getContextPath() + "/returns?action=list");
                return;
            }

            try {
                int requestId = Integer.parseInt(reqIdStr);
                returnService.decide(requestId, status, reason, user.getUserId());
                SessionUtil.flashSuccess(session, "Đã cập nhật phán quyết cho yêu cầu #" + requestId);
            } catch (Exception e) {
                SessionUtil.flashError(session, "Lỗi xử lý: " + e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/returns?action=list");
            return;
        }

        // --- Customer tạo mới yêu cầu đổi trả ---
        String orderIdStr = req.getParameter("orderId");
        String orderItemIdStr = req.getParameter("orderItemId");
        String requestType = req.getParameter("requestType"); // RETURN hoặc EXCHANGE
        String reasonCode = req.getParameter("reasonCode");
        String description = req.getParameter("description");
        String evidenceUrl = req.getParameter("evidenceUrl");
        String qtyStr = req.getParameter("requestedQuantity");

        if (orderIdStr == null || orderItemIdStr == null || requestType == null || reasonCode == null || description == null || description.trim().isEmpty()) {
            SessionUtil.flashError(session, "Vui lòng điền đầy đủ các thông tin bắt buộc.");
            resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            int orderItemId = Integer.parseInt(orderItemIdStr);
            int requestedQuantity = qtyStr != null ? Integer.parseInt(qtyStr) : 1;

            ReturnRequest rr = new ReturnRequest();
            rr.setOrderId(orderId);
            rr.setOrderItemId(orderItemId);
            rr.setCustomerId(user.getUserId());
            rr.setRequestType(requestType);
            rr.setReasonCode(reasonCode);
            rr.setDescription(description);
            rr.setEvidenceUrl(evidenceUrl);
            rr.setRequestedQuantity(requestedQuantity);

            int reqId = returnService.createRequest(rr);
            if (reqId > 0) {
                SessionUtil.flashSuccess(session, "Gửi yêu cầu đổi trả hàng thành công. Vui lòng chờ Admin xử lý.");
            } else {
                SessionUtil.flashError(session, "Tạo yêu cầu thất bại.");
            }
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, "Lỗi nghiệp vụ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi hệ thống: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/returns?orderId=" + orderIdStr);
    }
}
