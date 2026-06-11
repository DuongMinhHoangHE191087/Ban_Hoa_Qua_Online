package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.ReviewService;
import com.fruitmkt.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/admin/reviews/visibility")
public class AdminReviewAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminReviewAPI.class.getName());

    private final ReviewService reviewService = new ReviewService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            int reviewId = Integer.parseInt(request.getParameter("reviewId"));
            String action = request.getParameter("action");
            if (action == null || action.trim().isEmpty()) {
                boolean isHidden = Boolean.parseBoolean(request.getParameter("isHidden"));
                action = isHidden ? "reject" : "approve";
            }

            reviewService.moderateReview(reviewId, action);
            boolean rejected = "reject".equalsIgnoreCase(action) || "hide".equalsIgnoreCase(action);
            result.put("success", true);
            result.put("message", rejected ? "Đã từ chối và ẩn đánh giá" : "Đã duyệt đánh giá");
            
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi kiểm duyệt review", e);
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}
