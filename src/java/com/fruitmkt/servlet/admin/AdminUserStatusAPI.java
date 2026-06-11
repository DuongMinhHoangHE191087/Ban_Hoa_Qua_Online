package com.fruitmkt.servlet.admin;

import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.service.UserService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/users/status")
public class AdminUserStatusAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserStatusAPI.class.getName());

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String status = request.getParameter("status");

            boolean updated = userService.updateUserStatus(userId, status);

            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(response, ApiResponse.ok("Cập nhật trạng thái thành công"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy user"));
            }
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi cập nhật trạng thái người dùng userId=" + request.getParameter("userId"), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
        }
    }
}
