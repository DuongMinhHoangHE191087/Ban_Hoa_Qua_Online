package servlet.shop.order;

import config.AppConfig;
import util.SessionUtil;
import util.LoggerUtil;
import service.order.OrderService;

import model.dto.common.PagedResultDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(ShopOrderServlet.class.getName());
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
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
            } catch (NumberFormatException ignored) {
                // Keep default page=1 for invalid input — no logging needed here
            }
        }

        try {
            PagedResultDTO dto = orderService.shopOrders(user.getUserId(), status, page);
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
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
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
                LocalDateTime estimatedTime = null;
                if (estimateStr != null && !estimateStr.trim().isEmpty()) {
                    try {
                        // Try standard ISO parsing first (supports seconds)
                        estimatedTime = LocalDateTime.parse(estimateStr.trim());
                    } catch (DateTimeParseException ex) {
                        try {
                            // Fallback to pattern without seconds (commonly sent by HTML5 inputs)
                            estimatedTime = LocalDateTime.parse(estimateStr.trim(), DT_FMT);
                        } catch (DateTimeParseException ex2) {
                            LoggerUtil.warn(log, "estimatedDeliveryTime format không hợp lệ: " + estimateStr, ex2);
                            // Bỏ qua thời gian, vẫn dispatch bình thường
                        }
                    }
                }
                orderService.dispatchOrder(orderId, user.getUserId(), estimatedTime);
                SessionUtil.setFlashMessage(req.getSession(), "Đã bàn giao đơn hàng cho vận chuyển thành công!", "success");
            } else {
                SessionUtil.setFlashMessage(req.getSession(), "Hành động không hợp lệ!", "error");
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "[ShopOrder] POST action=" + action + " orderId=" + orderIdStr, e);
            SessionUtil.setFlashMessage(req.getSession(),
                    "Lỗi: " + (e.getMessage() != null ? e.getMessage() : "Có lỗi không xác định."), "error");
        }

        // PRG: redirect về trang đơn hàng, giữ lại bộ lọc status nếu có
        String status = req.getParameter("currentStatus");
        String redirect = req.getContextPath() + "/shop/orders";
        if (status != null && !status.trim().isEmpty()) {
            redirect += "?status=" + status;
        }
        resp.sendRedirect(redirect);
    }

}
