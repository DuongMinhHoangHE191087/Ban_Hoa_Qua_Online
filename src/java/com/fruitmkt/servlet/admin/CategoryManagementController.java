package com.fruitmkt.servlet.admin;

import com.fruitmkt.dao.CategoryManagementDAO;
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
 * Controller xử lý luồng Quản trị Danh mục.
 * Lắng nghe các url: danh sách (/admin/categories), thêm mới (/add), bật tắt hiển thị (/toggle)
 */
@WebServlet(name = "CategoryManagementController", urlPatterns = {"/admin/categories", "/admin/categories/add", "/admin/categories/toggle"})
public class CategoryManagementController extends HttpServlet {

    /**
     * Hàm doGet: Dùng để lấy danh sách danh mục và load lên JSP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        CategoryManagementDAO dao = new CategoryManagementDAO();

        try {
            if ("/admin/categories".equals(path)) {
                // Liệt kê danh sách
                List<Map<String, Object>> categories = dao.getAllCategories();
                request.setAttribute("categories", categories);
                request.getRequestDispatcher("/admin/category-management.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi lấy dữ liệu danh mục");
        }
    }

    /**
     * Hàm doPost: Xử lý Form thêm mới danh mục và yêu cầu chuyển đổi trạng thái (Ẩn/Hiện).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đặt encoding utf-8 để đọc tiếng Việt chuẩn (Tên danh mục)
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        CategoryManagementDAO dao = new CategoryManagementDAO();

        try {
            if ("/admin/categories/add".equals(path)) {
                // Lấy data từ Form HTML
                String name = request.getParameter("name");
                String slug = request.getParameter("slug");
                int displayOrder = Integer.parseInt(request.getParameter("displayOrder"));
                boolean isActive = "1".equals(request.getParameter("isActive")); // Kiểm tra xem option là 1(true) hay 0(false)
                
                // Gọi DAO ghi vào cơ sở dữ liệu
                dao.addCategory(name, slug, displayOrder, isActive);
                response.sendRedirect(request.getContextPath() + "/admin/categories?msg=add_success");
                
            } else if ("/admin/categories/toggle".equals(path)) {
                // Xử lý đổi trạng thái
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));
                boolean isActive = "1".equals(request.getParameter("isActive"));
                
                dao.toggleCategoryStatus(categoryId, isActive);
                response.sendRedirect(request.getContextPath() + "/admin/categories?msg=toggle_success");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Lỗi xảy ra (thường là do trùng Tên hoặc trùng URL tĩnh UNIQUE trong DB)
            response.sendRedirect(request.getContextPath() + "/admin/categories?msg=error");
        }
    }
}
