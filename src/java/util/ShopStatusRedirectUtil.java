package util;

import config.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import service.shop.ShopService;
import java.io.IOException;
import java.sql.SQLException;

/**
 * ShopStatusRedirectUtil — Gom logic điều hướng sang /shop/status theo hồ sơ shop.
 */
public final class ShopStatusRedirectUtil {

    private static final ShopService SHOP_SERVICE = new ShopService();

    private ShopStatusRedirectUtil() {}

    public static boolean redirectToShopStatusIfProfileExists(HttpServletRequest req, HttpServletResponse resp, User user, HttpSession session) throws IOException {
        if (user == null) {
            return false;
        }
        if ("true".equals(req.getParameter("edit"))) {
            return false;
        }
        try {
            ShopProfile profile = SHOP_SERVICE.getShopByUserId(user.getUserId());
            if (profile != null) {
                if (session != null) {
                    session.setAttribute("_shopProfile", profile);
                    SessionUtil.flashSuccess(session, "Hồ sơ shop của bạn đã sẵn sàng để xem trạng thái.");
                }
                resp.sendRedirect(req.getContextPath() + "/shop/status");
                return true;
            }
        } catch (SQLException e) {
            throw new IOException("Không thể kiểm tra trạng thái shop.", e);
        }
        return false;
    }

    public static void redirectToRoleHome(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        String role = user.getRole();
        if (AppConfig.ROLE_ADMIN.equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (AppConfig.ROLE_DELIVERY.equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/delivery/dashboard");
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
