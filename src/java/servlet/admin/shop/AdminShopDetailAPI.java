package servlet.admin.shop;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.ShopProfileDAO;
import model.entity.auth.User;
import model.entity.shop.ShopProfile;
import model.response.ApiResponse;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AdminShopDetailAPI - Trả về thông tin chi tiết Shop (kèm Email liên hệ) dưới dạng JSON.
 */
@WebServlet("/api/admin/shops/detail")
public class AdminShopDetailAPI extends HttpServlet {
    private static final Logger log = Logger.getLogger(AdminShopDetailAPI.class.getName());
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập."));
            return;
        }

        try {
            int shopId = Integer.parseInt(req.getParameter("id"));
            ShopProfile profile = shopProfileDAO.findById(shopId);

            if (profile == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy cửa hàng."));
                return;
            }

            User user = userDAO.findUserById(profile.getUserId());
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy người dùng."));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("shopId", profile.getProfileId());
            data.put("shopName", profile.getShopName());
            data.put("ownerId", user.getUserId());
            data.put("ownerName", user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : user.getEmail());
            data.put("ownerEmail", user.getEmail());
            data.put("businessEmail", profile.getBusinessEmail() != null ? profile.getBusinessEmail() : user.getEmail());
            data.put("deliveryAddress", profile.getDeliveryAddress());
            data.put("shopDescription", profile.getShopDescription());
            data.put("approvalStatus", profile.getApprovalStatus());
            data.put("preferredCategories", profile.getPreferredCategories());
            data.put("docPaths", profile.getDocPaths());

            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(data, "Lấy thông tin thành công"));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "ID cửa hàng không hợp lệ."));
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi khi lấy chi tiết cửa hàng", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server."));
        }
    }
}
