package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
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

    private final com.fruitmkt.service.ShopService shopService = new com.fruitmkt.service.ShopService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            com.fruitmkt.model.entity.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            req.setAttribute("shopProfile", profile);
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
        com.fruitmkt.model.entity.User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            com.fruitmkt.model.entity.ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            if (profile != null) {
                profile.setShopDescription(req.getParameter("shopDescription"));
                profile.setDeliveryAddress(req.getParameter("deliveryAddress"));
                profile.setPreferredCategories(req.getParameter("preferredCategories"));
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
