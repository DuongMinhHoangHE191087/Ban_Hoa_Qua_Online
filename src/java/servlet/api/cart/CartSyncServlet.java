package servlet.api.cart;

import config.AppConfig;
import model.response.ApiResponse;
import util.SessionUtil;
import service.cart.CartService;
import util.JsonUtil;
import util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CartSyncServlet — Controller cho chức năng: Đồng bộ giỏ hàng vãng lai lên Database
 *
 * URL: /api/cart/sync
 * GET : -
 * POST: Đồng bộ guest cart localStorage lên server
 *
 * @author fruitmkt-team
 */
@WebServlet("/api/cart/sync")
public class CartSyncServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CartSyncServlet.class.getName());

    /** Payload trả về của sync. */
    public record CartSyncResponse(String message) {}

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        JsonUtil.writeJson(resp, ApiResponse.fail(405, "Phương thức GET không được hỗ trợ cho endpoint này."));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        model.entity.auth.User user = SessionUtil.getCurrentUser(session);

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(401, "Người dùng chưa đăng nhập."));
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String bodyJson = sb.toString();

            Map<String, Object> parsedBody = JsonUtil.fromJson(bodyJson, Map.class);
            if (parsedBody != null && parsedBody.containsKey("items")) {
                String itemsJson = JsonUtil.toJson(parsedBody.get("items"));
                cartService.syncGuestCart(user.getUserId(), itemsJson);
            }

            JsonUtil.writeJson(resp, ApiResponse.ok(
                    new CartSyncResponse("Đồng bộ gộp giỏ hàng thành công.")));
        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "CartSyncServlet#doPost",
                    "Lỗi máy chủ. Vui lòng thử lại.",
                    e);
        }
    }
}
