package com.fruitmkt.servlet.common;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.UserService;
import com.fruitmkt.service.AuthService;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.FileUploadUtil;
import com.fruitmkt.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;

@WebServlet("/profile")
@MultipartConfig(
    maxFileSize = 5242880L,      // 5MB
    maxRequestSize = 20971520L,  // 20MB
    fileSizeThreshold = 1048576  // 1MB
)
public class UserProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // Lấy lại user mới nhất từ DB
            User dbUser = userService.findById(currentUser.getUserId());
            req.setAttribute("user", dbUser);
            req.getRequestDispatcher("/WEB-INF/jsp/common/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải thông tin cá nhân");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        try {
            User dbUser = userService.findById(currentUser.getUserId());

            if ("updateProfile".equals(action)) {
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");

                // 1. Khử XSS cho dữ liệu đầu vào
                fullName = fullName != null ? fullName.replaceAll("<[^>]*>", "").trim() : "";
                phone = phone != null ? phone.replaceAll("<[^>]*>", "").trim() : "";

                // 2. Validate bằng ValidationUtil
                ValidationUtil.requireNotBlank(fullName, "Họ và tên");
                phone = ValidationUtil.normalizePhone(phone);
                if (phone != null && !phone.isEmpty()) {
                    ValidationUtil.requireValidPhone(phone, "Số điện thoại");
                    // Check trùng số điện thoại
                    if (userService.isPhoneTakenByAnother(phone, dbUser.getUserId())) {
                        throw new Exception("Số điện thoại này đã được đăng ký bởi tài khoản khác!");
                    }
                }

                // 3. Xử lý tải lên avatar
                Part avatarPart = req.getPart("avatar");
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadDir = getServletContext().getRealPath("");
                    String savedPath = FileUploadUtil.save(avatarPart, uploadDir);
                    if (savedPath != null) {
                        // Xóa ảnh cũ nếu là file upload custom trước đó để tiết kiệm tài nguyên
                        String oldAvatar = dbUser.getAvatarUrl();
                        if (oldAvatar != null && !oldAvatar.equals("assets/images/default-avatar.svg") && !oldAvatar.startsWith("http")) {
                            FileUploadUtil.delete(uploadDir + File.separator + oldAvatar);
                        }
                        dbUser.setAvatarUrl(savedPath);
                    }
                }

                dbUser.setFullName(fullName);
                dbUser.setPhone(phone);

                userService.updateUser(dbUser);
                
                // Đồng bộ lại session
                SessionUtil.setCurrentUser(req.getSession(), dbUser);
                SessionUtil.flashSuccess(req.getSession(), "Cập nhật thông tin cá nhân thành công!");

            } else if ("updateAddress".equals(action)) {
                String address = req.getParameter("userAddress");
                address = address != null ? address.replaceAll("<[^>]*>", "").trim() : "";

                ValidationUtil.requireValidAddress(address, "Địa chỉ giao hàng");
                
                dbUser.setUserAddress(address);
                userService.updateUser(dbUser);
                
                // Đồng bộ session
                SessionUtil.setCurrentUser(req.getSession(), dbUser);
                SessionUtil.flashSuccess(req.getSession(), "Cập nhật địa chỉ thành công!");

            } else if ("changePassword".equals(action)) {
                String currentPassword = req.getParameter("currentPassword");
                String newPassword = req.getParameter("newPassword");
                String confirmPassword = req.getParameter("confirmPassword");

                if (currentPassword == null || currentPassword.isEmpty() ||
                    newPassword == null || newPassword.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty()) {
                    throw new Exception("Vui lòng điền đầy đủ thông tin mật khẩu!");
                }

                if (!newPassword.equals(confirmPassword)) {
                    throw new Exception("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                }

                // Đổi mật khẩu qua AuthService (kiểm tra password cũ, độ dài)
                authService.changePassword(dbUser.getUserId(), currentPassword, newPassword);
                SessionUtil.flashSuccess(req.getSession(), "Đổi mật khẩu tài khoản thành công!");
            } else {
                SessionUtil.flashError(req.getSession(), "Hành động không hợp lệ!");
            }
        } catch (Exception e) {
            req.getServletContext().log("UserProfileServlet POST error: " + e.getMessage(), e);
            SessionUtil.flashError(req.getSession(), e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}
