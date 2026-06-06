package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/shops/manage")
public class AdminShopManageServlet extends HttpServlet {

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        try {
            List<ShopProfile> profiles = shopProfileDAO.findAll();
            req.setAttribute("shopList", profiles);
        } catch (SQLException e) {
            getServletContext().log("AdminShopManageServlet GET error", e);
            req.setAttribute("errorMsg", "Không thể tải danh sách cửa hàng.");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-shops.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền.");
            return;
        }

        String action = req.getParameter("action");
        String profileIdStr = req.getParameter("profileId");

        try {
            if (profileIdStr == null || profileIdStr.isEmpty()) {
                throw new Exception("Thiếu profileId");
            }
            int profileId = Integer.parseInt(profileIdStr);

            if ("suspend".equals(action)) {
                // Suspends the shop
                shopProfileDAO.updateApprovalStatus(profileId, 0, "SUSPENDED", "Đình chỉ bởi Admin");
                SessionUtil.flashSuccess(req.getSession(), "Đã đình chỉ cửa hàng.");
            } else if ("activate".equals(action)) {
                // Reactivates the shop
                shopProfileDAO.updateApprovalStatus(profileId, 0, "APPROVED", "Khôi phục hoạt động");
                SessionUtil.flashSuccess(req.getSession(), "Đã khôi phục hoạt động cửa hàng.");
            } else {
                throw new Exception("Hành động không hợp lệ: " + action);
            }
        } catch (Exception e) {
            getServletContext().log("AdminShopManageServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(req.getSession(), "Lỗi: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/shops/manage");
    }
}
