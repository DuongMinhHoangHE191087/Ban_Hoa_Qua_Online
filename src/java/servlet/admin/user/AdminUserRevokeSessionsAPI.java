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

@WebServlet("/admin/users/revoke-sessions")
public class AdminUserRevokeSessionsAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserRevokeSessionsAPI.class.getName());

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!SessionUtil.hasRole(request.getSession(false), AppConfig.ROLE_ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện thao tác này."));
            return;
        }

        try {
            String userIdParam = request.getParameter("userId");
            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Thiếu userId."));
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdParam.trim());
            } catch (NumberFormatException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "userId không hợp lệ."));
                return;
            }

            if (userId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "userId không hợp lệ."));
                return;
            }

            userService.deleteSessionsByUserId(userId);

            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok("Đã thu hồi tất cả phiên đăng nhập (Refresh Tokens) của người dùng thành công."));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    request,
                    response,
                    log,
                    "AdminUserRevokeSessionsAPI#doPost",
                        "Lỗi hệ thống khi thu hồi phiên đăng nhập.",
                        e);
        }
    }
}
