package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ReviewDAO;
import com.fruitmkt.model.entity.Review;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.ReviewService;
import com.fruitmkt.service.OrderService;
import com.fruitmkt.dao.OrderDAO;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * ReviewServlet — Controller cho chức năng: Form viết review (sau khi order completed)
 *
 * URL: /reviews
 * GET : Form viết review (sau khi order completed)
 * POST: Lưu review
 *
 * @author fruitmkt-team
 */
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/reviews")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();
    private final OrderService orderService = new OrderService();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            SessionUtil.setFlashMessage(session, "Vui lòng đăng nhập để đánh giá sản phẩm.", "danger");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
     

        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            SessionUtil.flashError(session, "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            Order order = orderDAO.findByIdForCustomer(orderId, user.getUserId());
            if (order == null) {
                SessionUtil.flashError(session, "Không tìm thấy đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }

            // Đơn hàng phải ở trạng thái DELIVERED mới được review
            if (!"DELIVERED".equals(order.getStatus())) {
                SessionUtil.flashError(session, "Chỉ có đơn hàng đã giao thành công mới có thể đánh giá sản phẩm.");
                resp.sendRedirect(req.getContextPath() + "/orders?action=detail&orderId=" + orderId);
                return;
            }

            List<OrderItem> items = orderService.getOrderItems(orderId);
            
            // Đánh dấu sản phẩm nào đã được review
            List<OrderItem> unreviewedItems = new ArrayList<>();
            List<OrderItem> reviewedItems = new ArrayList<>();
            for (OrderItem item : items) {
                if (reviewService.canReview(user.getUserId(), item.getOrderItemId())) {
                    unreviewedItems.add(item);
                } else {
                    reviewedItems.add(item);
                }
            }

            req.setAttribute("order", order);
            req.setAttribute("unreviewedItems", unreviewedItems);
            req.setAttribute("reviewedItems", reviewedItems);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/review.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Đã xảy ra lỗi: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User user = SessionUtil.getCurrentUser(session);

        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // CSRF Verification
        String csrfParam = req.getParameter("_csrf");
        String csrfSession = (String) session.getAttribute("_csrfToken");
        if (csrfSession != null && !csrfSession.equals(csrfParam)) {
            SessionUtil.flashError(session, "Yêu cầu không hợp lệ (CSRF). Vui lòng thử lại.");
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        String orderItemIdStr = req.getParameter("orderItemId");
        String ratingStr = req.getParameter("rating");
        String reviewText = req.getParameter("reviewText");
        String reviewImageUrl = req.getParameter("reviewImageUrl");

        if (orderIdStr == null || orderItemIdStr == null || ratingStr == null) {
            SessionUtil.flashError(session, "Vui lòng nhập đầy đủ thông tin đánh giá.");
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        int orderId = Integer.parseInt(orderIdStr);
        try {
            int orderItemId = Integer.parseInt(orderItemIdStr);
            int rating = Integer.parseInt(ratingStr);

            // Double check order ownership & status
            Order order = orderDAO.findByIdForCustomer(orderId, user.getUserId());
            if (order == null || !"DELIVERED".equals(order.getStatus())) {
                SessionUtil.flashError(session, "Đơn hàng không hợp lệ để đánh giá.");
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }

            Review review = new Review();
            review.setOrderItemId(orderItemId);
            review.setCustomerId(user.getUserId());
            review.setRating(rating);
            review.setReviewText(reviewText != null ? reviewText.trim() : "");
            review.setReviewImageUrl(reviewImageUrl != null ? reviewImageUrl.trim() : null);

            reviewService.submitReview(review);
            SessionUtil.flashSuccess(session, "Cảm ơn bạn đã gửi đánh giá sản phẩm!");

        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi lưu đánh giá: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/orders?action=detail&orderId=" + orderId);
    }
}
