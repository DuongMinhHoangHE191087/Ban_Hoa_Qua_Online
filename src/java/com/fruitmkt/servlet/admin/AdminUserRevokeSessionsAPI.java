package com.fruitmkt.servlet.admin;

import com.fruitmkt.service.UserService;
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

@WebServlet("/admin/users/revoke-sessions")
public class AdminUserRevokeSessionsAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserRevokeSessionsAPI.class.getName());

    private final UserService userService = new UserService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            
            userService.deleteSessionsByUserId(userId);
            
            result.put("success", true);
            result.put("message", "Đã thu hồi tất cả phiên đăng nhập (Refresh Tokens) của người dùng thành công.");
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi thu hồi phiên đăng nhập userId=" + request.getParameter("userId"), e);
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}
