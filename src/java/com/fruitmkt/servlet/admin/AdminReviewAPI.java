package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.ReviewService;
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

@WebServlet("/admin/reviews/visibility")
public class AdminReviewAPI extends HttpServlet {
    private final ReviewService reviewService = new ReviewService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            int reviewId = Integer.parseInt(request.getParameter("reviewId"));
            boolean isHidden = Boolean.parseBoolean(request.getParameter("isHidden"));
            
            reviewService.updateReviewVisibility(reviewId, isHidden);
            
            result.put("success", true);
            result.put("message", isHidden ? "Đã ẩn đánh giá khỏi cửa hàng" : "Đã hiển thị đánh giá");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}
