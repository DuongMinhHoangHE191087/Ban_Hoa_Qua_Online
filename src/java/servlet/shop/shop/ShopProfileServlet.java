package servlet.shop.shop;
import service.shop.ShopService;

import config.AppConfig;
import util.SessionUtil;
import service.auth.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ShopProfileServlet — Controller cho chức năng: Xem và sửa hồ sơ shop
 *
 * URL: /shop/profile
 * GET : Xem và sửa hồ sơ shop
 * POST: Cập nhật thông tin shop
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
@WebServlet("/shop/profile")
public class ShopProfileServlet extends HttpServlet {

    private final service.shop.ShopService shopService = new service.shop.ShopService();
    private final service.catalog.CategoryService categoryService = new service.catalog.CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            model.entity.shop.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            req.setAttribute("shopProfile", profile);
            req.setAttribute("categories", categoryService.getActiveCategories());
            req.getRequestDispatcher("/WEB-INF/jsp/shop/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            getServletContext().log("Lỗi tải thông tin shop: " + e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải thông tin shop");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        model.entity.auth.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            model.entity.shop.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            if (profile != null) {
                profile.setShopDescription(req.getParameter("shopDescription"));
                profile.setDeliveryAddress(req.getParameter("deliveryAddress"));
                
                // Parse categoryIds checkbox array to JSON string
                String[] categoryIds = req.getParameterValues("categoryIds");
                String preferredCategoriesJson = null;
                if (categoryIds != null && categoryIds.length > 0) {
                    StringBuilder sb = new StringBuilder("[");
                    boolean first = true;
                    for (String id : categoryIds) {
                        try {
                            int catId = Integer.parseInt(id);
                            if (!first) sb.append(",");
                            sb.append(catId);
                            first = false;
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                    sb.append("]");
                    preferredCategoriesJson = sb.toString();
                }
                profile.setPreferredCategories(preferredCategoriesJson);
                
                profile.setBusinessEmail(req.getParameter("businessEmail"));
                
                String expiryDaysRaw = req.getParameter("expiryWarningDays");
                if (expiryDaysRaw != null && !expiryDaysRaw.trim().isEmpty()) {
                    try {
                        profile.setExpiryWarningDays(Integer.parseInt(expiryDaysRaw.trim()));
                    } catch (NumberFormatException e) {
                        profile.setExpiryWarningDays(3);
                    }
                } else {
                    profile.setExpiryWarningDays(3);
                }

                String lowStockRaw = req.getParameter("lowStockThreshold");
                if (lowStockRaw != null && !lowStockRaw.trim().isEmpty()) {
                    try {
                        profile.setLowStockThreshold(Integer.parseInt(lowStockRaw.trim()));
                    } catch (NumberFormatException e) {
                        profile.setLowStockThreshold(5);
                    }
                } else {
                    profile.setLowStockThreshold(5);
                }

                // Note: shopName update might be restricted, but let's allow it for now
                String newName = req.getParameter("shopName");
                if (newName != null && !newName.trim().isEmpty()) {
                    profile.setShopName(newName.trim());
                }

                shopService.updateShopProfile(profile);
                SessionUtil.flashSuccess(req.getSession(), "Cập nhật hồ sơ cửa hàng thành công!");
            } else {
                SessionUtil.flashError(req.getSession(), "Không tìm thấy hồ sơ cửa hàng!");
            }
        } catch (Exception e) {
            getServletContext().log("Lỗi cập nhật shop: " + e.getMessage(), e);
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/shop/profile");
    }

}
