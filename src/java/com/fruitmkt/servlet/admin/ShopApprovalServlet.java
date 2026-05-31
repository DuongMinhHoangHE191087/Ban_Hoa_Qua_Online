package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.service.ShopService;
import com.fruitmkt.model.entity.ShopProfile;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ShopApprovalServlet — Controller cho chức năng: Queue duyệt shop mới
 *
 * URL: /admin/shops
 * GET : Queue duyệt shop mới
 * POST: Approve/reject shop
 *
 * QUY TẮC SERVLET:
 *   1. Không viết SQL ở đây — gọi Service
 *   2. Sau POST thành công dùng PRG pattern (sendRedirect)
 *   3. Lưu flash message vào session trước redirect
 *   4. Forward đến /WEB-INF/jsp/admin/... (không để truy cập trực tiếp)
 *   5. Kiểm tra quyền bằng SessionUtil trước khi xử lý
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/shops")
public class ShopApprovalServlet extends HttpServlet {

    private final ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Get all shops
            List<ShopProfile> shops = shopService.getAllShops();
            req.setAttribute("shopList", shops);
            req.getRequestDispatcher("/WEB-INF/jsp/admin/shop-approvals.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải danh sách cửa hàng");
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
        throw new UnsupportedOperationException("doPost not implemented: ShopApprovalServlet");
    }

}
