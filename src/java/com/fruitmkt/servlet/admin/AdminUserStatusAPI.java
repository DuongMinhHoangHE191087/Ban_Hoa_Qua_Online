package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.UserService;
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

@WebServlet("/admin/users/status")
public class AdminUserStatusAPI extends HttpServlet {
    private final UserService userService = new UserService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String status = request.getParameter("status"); // 'ACTIVE' or 'LOCKED' or 'INACTIVE'
            
            boolean updated = userService.updateUserStatus(userId, status);
            
            if (updated) {
                result.put("success", true);
                result.put("message", "Cập nhật trạng thái thành công");
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}
