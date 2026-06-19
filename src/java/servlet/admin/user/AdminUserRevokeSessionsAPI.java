package servlet.admin.user;

import model.response.ApiResponse;
import service.auth.UserService;
import util.JsonUtil;
import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/users/revoke-sessions")
public class AdminUserRevokeSessionsAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserRevokeSessionsAPI.class.getName());

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));

            userService.deleteSessionsByUserId(userId);

            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok("Đã thu hồi tất cả phiên đăng nhập (Refresh Tokens) của người dùng thành công."));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    request,
                    response,
                    log,
                    "AdminUserRevokeSessionsAPI#doPost",
                    "Lỗi server: " + e.getMessage(),
                    e);
        }
    }
}
