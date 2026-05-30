package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.ReviewService;
import com.fruitmkt.model.entity.Review;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Review> reviews = reviewService.getAllReviewsForAdmin();
            req.setAttribute("reviewList", reviews);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/review-management.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách đánh giá");
        }
    }
}
