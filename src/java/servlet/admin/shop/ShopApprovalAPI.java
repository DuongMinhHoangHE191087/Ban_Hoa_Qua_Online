package servlet.admin.shop;

import config.AppConfig;
import model.response.ApiResponse;
import service.shop.ShopService;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/admin/shops/approve")
public class ShopApprovalAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopApprovalAPI.class.getName());

    private final ShopService shopService = new ShopService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Auth check
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || !SessionUtil.isLoggedIn(httpSession)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập."));
            return;
        }
        if (!SessionUtil.hasRole(httpSession, AppConfig.ROLE_ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này."));
            return;
        }

        // CSRF check
        String sessionCsrf = (String) httpSession.getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = request.getParameter("_csrf");
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ."));
            return;
        }

        try {
            int profileId = Integer.parseInt(request.getParameter("profileId"));
            String status = request.getParameter("status");
            String rejectionReason = request.getParameter("rejectionReason");

            shopService.updateShopStatus(profileId, status, rejectionReason);

            String message = "APPROVED".equals(status) ? "Đã duyệt cửa hàng" : "Đã từ chối cửa hàng";
            response.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(response, ApiResponse.ok(message));

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi phê duyệt shop profileId=" + request.getParameter("profileId"), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(response, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server: " + e.getMessage()));
        }
    }
}
