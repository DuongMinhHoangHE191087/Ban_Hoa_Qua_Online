package com.fruitmkt.servlet.admin;

import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.service.ReviewService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/reviews/visibility")
public class AdminReviewAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminReviewAPI.class.getName());

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            int reviewId = Integer.parseInt(request.getParameter("reviewId"));
            String action = request.getParameter("action");
            if (action == null || action.trim().isEmpty()) {
                boolean isHidden = Boolean.parseBoolean(request.getParameter("isHidden"));
                action = isHidden ? "reject" : "approve";
            }

            reviewService.moderateReview(reviewId, action);
            boolean rejected = "reject".equalsIgnoreCase(action) || "hide".equalsIgnoreCase(action);
            String message = rejected ? "Đã từ chối và ẩn đánh giá" : "Đã duyệt đánh giá";
            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok(message));

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi kiểm duyệt review", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
        }
    }
}
