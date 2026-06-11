package com.fruitmkt.servlet.api;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.response.ApiResponse;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.CartService;
import com.fruitmkt.util.JsonUtil;
import com.fruitmkt.util.LoggerUtil;

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
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(session);

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
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LoggerUtil.error(log, "Error syncing cart for user", e);
            JsonUtil.writeJson(resp, ApiResponse.fail("Lỗi máy chủ. Vui lòng thử lại."));
        }
    }
}
