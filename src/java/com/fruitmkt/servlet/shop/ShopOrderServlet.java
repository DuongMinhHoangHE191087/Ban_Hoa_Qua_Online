package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.service.DeliveryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * ShopOrderServlet — Controller cho chức năng: Đơn hàng của shop, filter theo status
 *
 * URL: /shop/orders
 * GET : Đơn hàng của shop, filter theo status
 * POST: Xác nhận/chuẩn bị/bàn giao đơn hàng
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/shop/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/orders")
public class ShopOrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ chủ shop mới được truy cập!");
            return;
        }

        String status = req.getParameter("status");
        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }

        try {
            com.fruitmkt.model.dto.PagedResultDTO dto = orderService.shopOrders(user.getUserId(), status, page);
            req.setAttribute("orders", dto.getItems());
            req.setAttribute("currentPage", page);
            req.setAttribute("status", status);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/orders.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        String orderIdStr = req.getParameter("orderId");
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/shop/orders");
            return;
        }

        try {
            if ("approve".equals(action)) {
                orderService.confirmOrder(orderId, user.getUserId());
                SessionUtil.setFlashMessage(req.getSession(), "Đã duyệt đơn hàng thành công!", "success");
            } else if ("reject".equals(action)) {
                String reason = req.getParameter("reason");
                orderService.cancelOrder(orderId, user.getUserId(), reason);
                SessionUtil.setFlashMessage(req.getSession(), "Đã hủy đơn hàng và hoàn lại tồn kho!", "success");
            } else if ("dispatch".equals(action)) {
                String estimateStr = req.getParameter("estimatedDeliveryTime");
                orderService.dispatchOrder(orderId, user.getUserId());
                
                // B2 Fix: staffId=0 maps to NULL in DAO (unassigned) — delivery staff can self-pick from dashboard
                java.time.LocalDateTime estimatedTime = null;
                if (estimateStr != null && !estimateStr.trim().isEmpty()) {
                    estimatedTime = java.time.LocalDateTime.parse(estimateStr);
                }
                deliveryService.assignShipper(orderId, 0, estimatedTime);
                
                SessionUtil.setFlashMessage(req.getSession(), "Đã giao đơn hàng cho vận chuyển!", "success");
            }
        } catch (Exception e) {
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi: " + e.getMessage(), "error");
        }
        
        resp.sendRedirect(req.getContextPath() + "/shop/orders");
    }

}
