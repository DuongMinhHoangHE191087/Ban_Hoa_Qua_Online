package com.fruitmkt.servlet.shop;

import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.ShopService;
import com.fruitmkt.util.FileUploadUtil;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

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
        PrintWriter out = resp.getWriter();

        // 1. Kiểm tra quyền chủ shop
        User user = SessionUtil.getCurrentUser(req.getSession());
        if (user == null || !"SHOP_OWNER".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"success\":false,\"message\":\"Chưa đăng nhập hoặc không có quyền!\"}");
            return;
        }

        try {
            ShopProfile profile = shopService.getShopByUserId(user.getUserId());
            if (profile == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"success\":false,\"message\":\"Không tìm thấy hồ sơ gian hàng!\"}");
                return;
            }

            // 2. Xác định kiểu upload (logo hoặc cover)
            String type = req.getParameter("type");
            if (type == null || (!type.equals("logo") && !type.equals("cover"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\":false,\"message\":\"Tham số 'type' không hợp lệ!\"}");
                return;
            }

            Part part = req.getPart("file");
            if (part == null || part.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\":false,\"message\":\"Không có file được chọn!\"}");
                return;
            }

            // 3. Tiến hành lưu file
            String uploadDir = getServletContext().getRealPath("");
            String relativePath = FileUploadUtil.save(part, uploadDir);

            if (relativePath == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"success\":false,\"message\":\"Không thể lưu file!\"}");
                return;
            }

            // Xóa file cũ nếu có để giải phóng bộ nhớ
            String contextPath = req.getContextPath();
            if (type.equals("logo")) {
                if (profile.getLogoUrl() != null) {
                    FileUploadUtil.delete(uploadDir + "/" + profile.getLogoUrl());
                }
                profile.setLogoUrl(relativePath);
            } else {
                if (profile.getCoverUrl() != null) {
                    FileUploadUtil.delete(uploadDir + "/" + profile.getCoverUrl());
                }
                profile.setCoverUrl(relativePath);
            }

            // 4. Lưu vào Database
            shopService.updateShopProfile(profile);

            // Trả về JSON thành công kèm URL để frontend cập nhật preview tức thì
            String fullUrl = contextPath + "/" + relativePath;
            out.write("{\"success\":true,\"url\":\"" + fullUrl + "\",\"relativePath\":\"" + relativePath + "\"}");

        } catch (Exception e) {
            getServletContext().log("Lỗi upload ảnh shop: " + e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\":false,\"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
        }
    }
}
