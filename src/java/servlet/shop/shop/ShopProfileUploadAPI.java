package servlet.shop.shop;

import model.entity.shop.ShopProfile;
import model.entity.auth.User;
import model.response.ApiResponse;
import service.shop.ShopService;
import util.FileUploadUtil;
import util.JsonUtil;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;

/**
 * ShopProfileUploadAPI — API xử lý upload ảnh Logo và Ảnh bìa của Shop
 * URL: /api/shop/upload
 */
@WebServlet("/api/shop/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,  // 1MB
    maxFileSize = 1024 * 1024 * 5,       // 5MB
    maxRequestSize = 1024 * 1024 * 10     // 10MB
)
public class ShopProfileUploadAPI extends HttpServlet {

    private final ShopService shopService = new ShopService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        // 1. Kiểm tra quyền chủ shop
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập hoặc không có quyền!"));
            return;
        }

        try {
            ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            if (profile == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ gian hàng!"));
                return;
            }

            // 2. Xác định kiểu upload (logo hoặc cover)
            String type = req.getParameter("type");
            if (type == null || (!type.equals("logo") && !type.equals("cover"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Tham số 'type' không hợp lệ!"));
                return;
            }

            Part part = req.getPart("file");
            if (part == null || part.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Không có file được chọn!"));
                return;
            }

            // 3. Tiến hành lưu file
            String relativePath = FileUploadUtil.save(part, getServletContext().getRealPath(""));

            if (relativePath == null) {
                util.ServletUtil.sendJsonInternalServerError(
                        req,
                        resp,
                        java.util.logging.Logger.getLogger(ShopProfileUploadAPI.class.getName()),
                        "ShopProfileUploadAPI#doPost",
                        "Không thể lưu file!",
                        new IllegalStateException("Không thể lưu file!"));
                return;
            }

            // Xóa file cũ nếu có để giải phóng bộ nhớ
            String contextPath = req.getContextPath();
            if (type.equals("logo")) {
                if (profile.getLogoUrl() != null) {
                    FileUploadUtil.delete(profile.getLogoUrl());
                }
                profile.setLogoUrl(relativePath);
            } else {
                if (profile.getCoverUrl() != null) {
                    FileUploadUtil.delete(profile.getCoverUrl());
                }
                profile.setCoverUrl(relativePath);
            }

            // 4. Lưu vào Database
            shopService.updateShopProfile(profile);

            // Trả về JSON thành công kèm URL để frontend cập nhật preview tức thì
            String fullUrl = contextPath + "/" + relativePath;
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(Map.of("url", fullUrl, "relativePath", relativePath)));

        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ShopProfileUploadAPI.class.getName()),
                    "ShopProfileUploadAPI#doPost",
                    "Lỗi hệ thống: " + e.getMessage(),
                    e);
        }
    }
}
