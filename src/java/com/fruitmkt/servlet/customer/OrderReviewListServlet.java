package com.fruitmkt.servlet.customer;

import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.dao.OrderItemDAO;
import com.fruitmkt.dao.ReviewDAO;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.Review;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/customer/order-reviews")
public class OrderReviewListServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(OrderReviewListServlet.class.getName());

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/customer/orders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            List<Order> orderList = orderDAO.findById(orderId);
            if (orderList.isEmpty() || orderList.get(0).getCustomerId() != user.getUserId()) {
                resp.sendError(403, "Đơn hàng không tồn tại hoặc bạn không có quyền.");
                return;
            }
            Order order = orderList.get(0);
            if (!"DELIVERED".equals(order.getStatus())) {
                SessionUtil.setFlashMessage(req.getSession(), "Chỉ có thể đánh giá đơn hàng đã giao thành công.", "warning");
                resp.sendRedirect(req.getContextPath() + "/customer/orders");
                return;
            }

            List<OrderItem> items = orderItemDAO.findByOrderId(orderId);
            Map<Integer, Review> reviewMap = new HashMap<>();
            
            for (OrderItem item : items) {
                Review r = reviewDAO.findByOrderItemId(item.getOrderItemId());
                if (r != null) {
                    reviewMap.put(item.getOrderItemId(), r);
                }
            }

            req.setAttribute("order", order);
            req.setAttribute("items", items);
            req.setAttribute("reviewMap", reviewMap);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/order-reviews.jsp").forward(req, resp);

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi hệ thống khi tải danh sách đánh giá đơn hàng", e);
            resp.sendError(500, "Lỗi hệ thống");
        }
    }
}
