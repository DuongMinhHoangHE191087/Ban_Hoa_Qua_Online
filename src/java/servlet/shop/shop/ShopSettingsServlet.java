package servlet.shop.shop;

import config.AppConfig;
import util.SessionUtil;
import service.shop.ShopService;
import util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ShopSettingsServlet — Controller cho trang Cài đặt cửa hàng.
 *
 * URL: /shop/settings
 * GET : Xem cài đặt cảnh báo tồn kho & hết hạn lô hàng
 * POST: Cập nhật expiryWarningDays, lowStockThreshold
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/shop/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/settings")
public class ShopSettingsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopSettingsServlet.class.getName());
    private final ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendError(403);
            return;
        }

        try {
            model.entity.shop.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            req.setAttribute("shopProfile", profile);
        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi tải cài đặt shop", e);
            req.setAttribute("shopProfile", null);
        }

        req.getRequestDispatcher("/WEB-INF/jsp/shop/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_SHOP_OWNER)) {
            resp.sendError(403);
            return;
        }

        try {
            model.entity.shop.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            if (profile == null) {
                SessionUtil.flashError(req.getSession(), "Không tìm thấy hồ sơ cửa hàng!");
                resp.sendRedirect(req.getContextPath() + "/shop/settings");
                return;
            }

            // Parse expiryWarningDays
            String expiryDaysRaw = req.getParameter("expiryWarningDays");
            if (expiryDaysRaw != null && !expiryDaysRaw.trim().isEmpty()) {
                try {
                    int days = Integer.parseInt(expiryDaysRaw.trim());
                    if (days < 1) days = 1;
                    if (days > 90) days = 90;
                    profile.setExpiryWarningDays(days);
                } catch (NumberFormatException e) {
                    profile.setExpiryWarningDays(3);
                }
            }

            // Parse lowStockThreshold
            String lowStockRaw = req.getParameter("lowStockThreshold");
            if (lowStockRaw != null && !lowStockRaw.trim().isEmpty()) {
                try {
                    int threshold = Integer.parseInt(lowStockRaw.trim());
                    if (threshold < 1) threshold = 1;
                    if (threshold > 1000) threshold = 1000;
                    profile.setLowStockThreshold(threshold);
                } catch (NumberFormatException e) {
                    profile.setLowStockThreshold(5);
                }
            }

            shopService.updateShopProfile(profile);
            SessionUtil.flashSuccess(req.getSession(), "Đã lưu cài đặt cửa hàng thành công!");
            LoggerUtil.info(log, "[ShopSettings] Owner=%d cập nhật: expiryDays=%d, lowStockThreshold=%d",
                    user.getUserId(), profile.getExpiryWarningDays(), profile.getLowStockThreshold());

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi cập nhật cài đặt shop", e);
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/shop/settings");
    }
}
