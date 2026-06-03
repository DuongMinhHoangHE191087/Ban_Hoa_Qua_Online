package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * CategoryServlet — Controller cho chức năng: Quản lý danh mục trái cây
 *
 * URL: /admin/categories
 * GET : Quản lý danh mục trái cây
 * POST: CRUD category
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/categories")
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        try {
            // Lấy danh sách danh mục
            List<Category> categories = categoryDAO.findAll();
            req.setAttribute("categories", categories);
            
            // Forward tới view
            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-categories.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách danh mục");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String action = req.getParameter("action");
        if (action == null) {
            SessionUtil.setFlashMessage(req.getSession(), "Hành động không hợp lệ", "danger");
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return;
        }

        try {
            switch (action) {
                case "create":
                    createCategory(req, resp);
                    break;
                case "update":
                    updateCategory(req, resp);
                    break;
                case "delete":
                    deleteCategory(req, resp);
                    break;
                case "toggle":
                    toggleStatus(req, resp);
                    break;
                default:
                    SessionUtil.setFlashMessage(req.getSession(), "Hành động không hợp lệ", "danger");
                    resp.sendRedirect(req.getContextPath() + "/admin/categories");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.setFlashMessage(req.getSession(), "Lỗi hệ thống: " + e.getMessage(), "danger");
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
        }
    }

    private void createCategory(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String name = req.getParameter("name");
        String slug = req.getParameter("slug");
        int displayOrder = Integer.parseInt(req.getParameter("displayOrder"));
        boolean isActive = req.getParameter("isActive") != null;

        Category cat = new Category();
        cat.setName(name);
        cat.setSlug(slug);
        cat.setDisplayOrder(displayOrder);
        cat.setIsActive(isActive);

        categoryDAO.save(cat);
        SessionUtil.setFlashMessage(req.getSession(), "Thêm danh mục mới thành công!", "success");
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private void updateCategory(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int categoryId = Integer.parseInt(req.getParameter("categoryId"));
        String name = req.getParameter("name");
        String slug = req.getParameter("slug");
        int displayOrder = Integer.parseInt(req.getParameter("displayOrder"));
        boolean isActive = req.getParameter("isActive") != null;

        Category cat = new Category();
        cat.setCategoryId(categoryId);
        cat.setName(name);
        cat.setSlug(slug);
        cat.setDisplayOrder(displayOrder);
        cat.setIsActive(isActive);

        categoryDAO.update(cat);
        SessionUtil.setFlashMessage(req.getSession(), "Cập nhật danh mục thành công!", "success");
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private void deleteCategory(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int categoryId = Integer.parseInt(req.getParameter("categoryId"));
        
        if (categoryDAO.hasActiveProducts(categoryId)) {
            SessionUtil.setFlashMessage(req.getSession(), "Không thể xóa danh mục đang có sản phẩm!", "danger");
        } else {
            categoryDAO.delete(categoryId);
            SessionUtil.setFlashMessage(req.getSession(), "Xóa danh mục thành công!", "success");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
    
    private void toggleStatus(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int categoryId = Integer.parseInt(req.getParameter("categoryId"));
        List<Category> list = categoryDAO.findById(categoryId);
        if (!list.isEmpty()) {
            Category cat = list.get(0);
            cat.setIsActive(!cat.getIsActive());
            categoryDAO.update(cat);
            SessionUtil.setFlashMessage(req.getSession(), "Đã thay đổi trạng thái hiển thị của danh mục!", "success");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}
