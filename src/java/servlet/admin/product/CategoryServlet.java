package servlet.admin.product;

import config.AppConfig;
import model.entity.catalog.Category;
import model.entity.auth.User;
import service.catalog.CategoryService;
import util.SessionUtil;

import util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * CategoryServlet — Controller xử lý luồng nghiệp vụ Quản lý Danh mục phía Admin.
 *
 * URL: /admin/categories
 * GET: Hiển thị giao diện quản lý danh sách danh mục
 * POST: Thực hiện tạo mới, cập nhật, xóa, bật/tắt hiển thị danh mục
 *
 * @author fruitmkt-team
 */
@WebServlet("/admin/categories")
public class CategoryServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CategoryServlet.class.getName());

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // Lấy danh sách danh mục thông qua Service
            List<Category> categories = categoryService.getAllCategories();
            req.setAttribute("categories", categories);
            
            // Forward tới view quản trị
            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-categories.jsp").forward(req, resp);
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(CategoryServlet.class.getName()),
                    "CategoryServlet#doGet",
                    "Lỗi khi lấy danh sách danh mục: " + e.getMessage(),
                    e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            SessionUtil.setFlashMessage(session, "Hành động không hợp lệ", "danger");
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
                    SessionUtil.setFlashMessage(session, "Hành động không hợp lệ", "danger");
                    resp.sendRedirect(req.getContextPath() + "/admin/categories");
            }
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi hệ thống khi xử lý danh mục action=" + action, e);
            SessionUtil.setFlashMessage(session, "Lỗi hệ thống: " + e.getMessage(), "danger");
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

        try {
            categoryService.createCategory(cat);
            SessionUtil.setFlashMessage(req.getSession(), "Thêm danh mục mới thành công!", "success");
        } catch (IllegalArgumentException e) {
            SessionUtil.setFlashMessage(req.getSession(), e.getMessage(), "danger");
        }
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

        try {
            categoryService.updateCategory(cat);
            SessionUtil.setFlashMessage(req.getSession(), "Cập nhật danh mục thành công!", "success");
        } catch (IllegalArgumentException e) {
            SessionUtil.setFlashMessage(req.getSession(), e.getMessage(), "danger");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private void deleteCategory(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int categoryId = Integer.parseInt(req.getParameter("categoryId"));
        
        try {
            // Service sẽ tự kiểm định tính hợp lệ của việc xóa danh mục (ràng buộc sản phẩm)
            categoryService.deleteCategory(categoryId);
            SessionUtil.setFlashMessage(req.getSession(), "Xóa danh mục thành công!", "success");
        } catch (IllegalStateException | IllegalArgumentException e) {
            SessionUtil.setFlashMessage(req.getSession(), e.getMessage(), "danger");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
    
    private void toggleStatus(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        int categoryId = Integer.parseInt(req.getParameter("categoryId"));
        try {
            categoryService.toggleCategoryStatus(categoryId);
            SessionUtil.setFlashMessage(req.getSession(), "Đã thay đổi trạng thái hiển thị của danh mục!", "success");
        } catch (IllegalArgumentException e) {
            SessionUtil.setFlashMessage(req.getSession(), e.getMessage(), "danger");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}
