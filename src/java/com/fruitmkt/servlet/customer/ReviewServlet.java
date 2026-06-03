package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ReviewDAO;
import com.fruitmkt.model.entity.Review;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
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

    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            SessionUtil.setFlashMessage(req.getSession(), "Vui lòng đăng nhập để đánh giá.", "danger");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String orderItemIdStr = req.getParameter("orderItemId");
        String action = req.getParameter("action");
        if (orderItemIdStr == null || orderItemIdStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu Order Item ID");
            return;
        }
        
        try {
            int orderItemId = Integer.parseInt(orderItemIdStr);
            Review existingReview = reviewDAO.findByOrderItemId(orderItemId);

            if ("edit".equals(action)) {
                if (existingReview == null || existingReview.getCustomerId() != user.getUserId()) {
                    SessionUtil.setFlashMessage(req.getSession(), "Đánh giá không tồn tại hoặc bạn không có quyền sửa.", "danger");
                    resp.sendRedirect(req.getContextPath() + "/customer/orders");
                    return;
                }
                req.setAttribute("review", existingReview);
            } else {
                if (existingReview != null) {
                    SessionUtil.setFlashMessage(req.getSession(), "Bạn đã đánh giá sản phẩm này rồi.", "warning");
                    resp.sendRedirect(req.getContextPath() + "/customer/orders");
                    return;
                }
            }

            req.setAttribute("orderItemId", orderItemId);
            req.setAttribute("action", action);
            req.getRequestDispatcher("/WEB-INF/jsp/customer/review-submit.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            String action = req.getParameter("action");
            int orderItemId = Integer.parseInt(req.getParameter("orderItemId"));

            if ("delete".equals(action)) {
                Review existingReview = reviewDAO.findByOrderItemId(orderItemId);
                if (existingReview != null && existingReview.getCustomerId() == user.getUserId()) {
                    reviewDAO.delete(existingReview.getReviewId());
                    recalculateRating(orderItemId);
                    SessionUtil.setFlashMessage(req.getSession(), "Đã xóa đánh giá thành công.", "success");
                }
                resp.sendRedirect(req.getContextPath() + "/customer/orders");
                return;
            }

            int rating = Integer.parseInt(req.getParameter("rating"));
            String reviewText = req.getParameter("reviewText");

            if (rating < 1 || rating > 5) {
                SessionUtil.setFlashMessage(req.getSession(), "Số sao không hợp lệ.", "danger");
                resp.sendRedirect(req.getContextPath() + "/reviews?orderItemId=" + orderItemId + ("edit".equals(action) ? "&action=edit" : ""));
                return;
            }

            String imageUrl = null;
            Part filePart = req.getPart("reviewImage");
            if (filePart != null && filePart.getSize() > 0) {
                String uploadPath = req.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "reviews";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String fileName = UUID.randomUUID().toString() + "_" + Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                filePart.write(uploadPath + File.separator + fileName);
                imageUrl = "uploads/reviews/" + fileName;
            }

            Review existingReview = reviewDAO.findByOrderItemId(orderItemId);

            if ("edit".equals(action)) {
                if (existingReview != null && existingReview.getCustomerId() == user.getUserId()) {
                    existingReview.setRating(rating);
                    existingReview.setReviewText(reviewText);
                    if (imageUrl != null) {
                        existingReview.setReviewImageUrl(imageUrl);
                    }
                    reviewDAO.update(existingReview);
                    recalculateRating(orderItemId);
                    SessionUtil.setFlashMessage(req.getSession(), "Cập nhật đánh giá thành công!", "success");
                }
            } else {
                if (existingReview == null) {
                    Review review = new Review();
                    review.setOrderItemId(orderItemId);
                    review.setCustomerId(user.getUserId());
                    review.setRating(rating);
                    review.setReviewText(reviewText);
                    review.setReviewImageUrl(imageUrl);
                    review.setIsHidden(false);
                    reviewDAO.save(review);
                    recalculateRating(orderItemId);
                    SessionUtil.setFlashMessage(req.getSession(), "Cảm ơn bạn đã gửi đánh giá!", "success");
                }
            }

            resp.sendRedirect(req.getContextPath() + "/customer/orders");

        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi hệ thống: " + e.getMessage(), "danger");
            resp.sendRedirect(req.getContextPath() + "/customer/orders");
        }
    }

    private void recalculateRating(int orderItemId) throws SQLException {
        int productId = productDAO.getProductIdByOrderItem(orderItemId);
        if (productId != -1) {
            productDAO.recalculateRating(productId);
        }
    }
}
