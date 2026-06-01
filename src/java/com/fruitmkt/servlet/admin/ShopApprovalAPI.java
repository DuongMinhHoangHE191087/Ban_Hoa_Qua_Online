package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.ShopService;
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

@WebServlet("/admin/shops/approve")
public class ShopApprovalAPI extends HttpServlet {
    private final ShopService shopService = new ShopService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            int profileId = Integer.parseInt(request.getParameter("profileId"));
            String status = request.getParameter("status"); // 'APPROVED' or 'REJECTED'
            String rejectionReason = request.getParameter("rejectionReason"); 
            
            shopService.updateShopStatus(profileId, status, rejectionReason);
            
            result.put("success", true);
            result.put("message", "APPROVED".equals(status) ? "Đã duyệt cửa hàng" : "Đã từ chối cửa hàng");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}
