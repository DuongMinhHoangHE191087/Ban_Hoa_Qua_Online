package com.fruitmkt.servlet.admin;

import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String role = request.getParameter("role");
            String keyword = request.getParameter("search");
            
            int page = 1;
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try { page = Integer.parseInt(pageStr); } catch(Exception e) {}
            }
            int pageSize = 10;
            int offset = (page - 1) * pageSize;
            
            List<User> users = userService.searchUsers(role, keyword, offset, pageSize);
            int totalRecords = userService.countUsers(role, keyword);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            
            request.setAttribute("userList", users);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("paramRole", role);
            request.setAttribute("paramSearch", keyword);
            
            request.getRequestDispatcher("/WEB-INF/jsp/admin/user-list.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading users");
        }
    }
}
