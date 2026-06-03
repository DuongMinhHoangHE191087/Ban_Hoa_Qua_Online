package com.fruitmkt.servlet.common;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.UserService;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {
    private final UserService userService = new UserService();

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

        String action = req.getParameter("action");
        try {
            User dbUser = userService.findById(currentUser.getUserId());

            if ("updateProfile".equals(action)) {
                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");

                if (fullName == null || fullName.trim().isEmpty()) {
                    SessionUtil.flashError(req.getSession(), "Họ tên không được để trống!");
                    resp.sendRedirect(req.getContextPath() + "/profile");
                    return;
                }

                dbUser.setFullName(fullName.trim());
                if (phone != null) dbUser.setPhone(phone.trim());

                userService.updateUser(dbUser);
                
                // Update session
                SessionUtil.setCurrentUser(req.getSession(), dbUser);
                SessionUtil.flashSuccess(req.getSession(), "Cập nhật thông tin thành công!");

            } else if ("updateAddress".equals(action)) {
                String address = req.getParameter("userAddress");
                if (address != null) {
                    dbUser.setUserAddress(address.trim());
                    userService.updateUser(dbUser);
                    
                    // Update session
                    SessionUtil.setCurrentUser(req.getSession(), dbUser);
                    SessionUtil.flashSuccess(req.getSession(), "Cập nhật địa chỉ thành công!");
                }
            } else {
                SessionUtil.flashError(req.getSession(), "Hành động không hợp lệ!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SessionUtil.flashError(req.getSession(), "Có lỗi xảy ra: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}
