package servlet.admin.shop;

import config.AppConfig;
import util.SessionUtil;
import model.entity.shop.ShopProfile;
import service.shop.ShopService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/shops/manage")
public class AdminShopManageServlet extends HttpServlet {

    private final ShopService shopService = new ShopService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        try {
            List<ShopProfile> profiles = shopService.getAllShops();
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
                shopService.updateShopStatus(profileId, AppConfig.SHOP_SUSPENDED, null);
                SessionUtil.flashSuccess(req.getSession(), "Đã đình chỉ cửa hàng.");
            } else if ("reject".equals(action)) {
                String rejectionReason = req.getParameter("rejectionReason");
                shopService.updateShopStatus(profileId, AppConfig.SHOP_REJECTED, rejectionReason);
                SessionUtil.flashSuccess(req.getSession(), "Đã từ chối cửa hàng.");
            } else if ("activate".equals(action)) {
                shopService.updateShopStatus(profileId, AppConfig.SHOP_APPROVED, null);
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
