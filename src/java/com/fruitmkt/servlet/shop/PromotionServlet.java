package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.PromotionService;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.Promotion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * PromotionServlet — Controller cho chức năng: Danh sách voucher của shop
 *
 * URL: /shop/promotions
 * GET : Danh sách voucher của shop
 * POST: Tạo/sửa/deactivate voucher
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/promotions")
public class PromotionServlet extends HttpServlet {

    private final PromotionService promotionService = new PromotionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            List<Promotion> promotions = promotionService.getShopPromos(currentUser.getUserId());
            req.setAttribute("promotions", promotions);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/promotion.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi khi tải danh sách khuyến mãi: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 1. Đọc params / JSON body
        //        2. Validate input
        //        3. Gọi service
        //        4. Set flash message
        //        5. Redirect (PRG pattern)
        //
        // Ví dụ:
        // req.getSession().setAttribute(AppConfig.SESSION_FLASH_MSG, "Thành công!");
        // req.getSession().setAttribute(AppConfig.SESSION_FLASH_TYPE, "success");
        // resp.sendRedirect(req.getContextPath() + "/..");
        throw new UnsupportedOperationException("doPost not implemented: PromotionServlet");
    }

}
