package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.ReviewService;
import com.fruitmkt.model.entity.Review;

import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminReviewServlet.class.getName());

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Review> reviews = reviewService.getAllReviewsForAdmin();
            req.setAttribute("reviewList", reviews);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/review-management.jsp").forward(req, resp);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải danh sách đánh giá", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách đánh giá");
        }
    }
}
