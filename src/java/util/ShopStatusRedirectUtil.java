package util;

import config.AppConfig;
import dao.shop.ShopProfileDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * ShopStatusRedirectUtil — Gom logic điều hướng sang /shop/status theo hồ sơ shop.
 */
public final class ShopStatusRedirectUtil {

    private static final ShopProfileDAO SHOP_PROFILE_DAO = new ShopProfileDAO();

    private ShopStatusRedirectUtil() {}

    public static boolean redirectToShopStatusIfProfileExists(HttpServletRequest req, HttpServletResponse resp, User user, HttpSession session) throws IOException {
        if (user == null) {
            return false;
        }
        try {
            List<ShopProfile> profiles = SHOP_PROFILE_DAO.findByUserId(user.getUserId());
            if (!profiles.isEmpty()) {
                if (session != null) {
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
