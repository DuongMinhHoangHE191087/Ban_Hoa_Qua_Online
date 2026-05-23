package com.fruitmkt.servlet.admin;

import com.fruitmkt.dao.UserManagementDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller quản lý tài khoản người dùng trong hệ thống.
 * Hỗ trợ đường dẫn xem danh sách (/admin/users) và thay đổi trạng thái (/admin/users/toggle-status).
 */
@WebServlet(name = "UserManagementController", urlPatterns = {"/admin/users", "/admin/users/toggle-status"})
public class UserManagementController extends HttpServlet {

    /**
     * Hàm doGet lấy danh sách toàn bộ User đẩy sang trang JSP để Admin xem.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        UserManagementDAO dao = new UserManagementDAO();

        try {
            if ("/admin/users".equals(path)) {
                // Lấy danh sách tất cả các user (trừ Admin)
                List<Map<String, Object>> usersList = dao.getAllUsers();
                // Nạp list vào request
                request.setAttribute("usersList", usersList);
                // Đẩy sang file giao diện hiển thị bảng
                request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi lấy dữ liệu người dùng");
        }
    }

    /**
     * Hàm doPost để nhận yêu cầu Khóa/Mở khóa người dùng từ các nút bấm trên giao diện.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        UserManagementDAO dao = new UserManagementDAO();

        try {
            if ("/admin/users/toggle-status".equals(path)) {
                // Đọc userId và status mong muốn từ Form (dạng input hidden)
                int userId = Integer.parseInt(request.getParameter("userId"));
                String newStatus = request.getParameter("newStatus");
                
                // Thực hiện đổi trạng thái trong Cơ sở dữ liệu
                dao.updateUserStatus(userId, newStatus);
                
                // Load lại trang danh sách, truyền thêm URL parameter msg=success để bật cảnh báo xanh
                response.sendRedirect(request.getContextPath() + "/admin/users?msg=success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/users?msg=error");
        }
    }
}
