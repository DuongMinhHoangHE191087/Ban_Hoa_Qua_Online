package com.fruitmkt.servlet.admin;

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

@WebServlet("/admin/users/edit")
public class AdminUserEditServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request.getSession()) || !SessionUtil.hasRole(request.getSession(), AppConfig.ROLE_ADMIN)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String userIdStr = request.getParameter("id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            User user = userService.findById(userId);
            if (user == null) {
                SessionUtil.flashError(request.getSession(), "Không tìm thấy người dùng!");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/jsp/admin/user-edit.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("AdminUserEditServlet doGet error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!SessionUtil.isLoggedIn(request.getSession()) || !SessionUtil.hasRole(request.getSession(), AppConfig.ROLE_ADMIN)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);
            User user = userService.findById(userId);
            if (user != null) {
                user.setFullName(request.getParameter("fullName"));
                user.setPhone(request.getParameter("phone"));
                user.setRole(request.getParameter("role"));
                user.setUserAddress(request.getParameter("userAddress"));
                
                userService.updateUser(user);
                SessionUtil.flashSuccess(request.getSession(), "Đã cập nhật thông tin người dùng thành công!");
            }
        } catch (Exception e) {
            getServletContext().log("AdminUserEditServlet doPost error", e);
            SessionUtil.flashError(request.getSession(), "Lỗi khi cập nhật người dùng!");
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
