package servlet.admin.user;

import config.AppConfig;
import model.response.ApiResponse;
import service.auth.UserService;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;
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

        // Defense-in-depth: kiểm tra quyền ADMIN trực tiếp, không chỉ dựa vào RoleFilter
        if (!SessionUtil.hasRole(request.getSession(false), AppConfig.ROLE_ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện thao tác này."));
            return;
        }

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
            // Log chi tiết server-side, trả về thông báo chung (không lộ e.getMessage())
            LoggerUtil.error(log, "Lỗi khi cập nhật trạng thái người dùng userId=" + request.getParameter("userId"), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống."));
        }
    }
}
