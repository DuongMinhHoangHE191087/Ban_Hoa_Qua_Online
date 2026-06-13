package com.fruitmkt.servlet.admin;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.service.CategoryService;
import com.fruitmkt.service.ProductService;
import com.fruitmkt.util.SessionUtil;

import com.fruitmkt.util.LoggerUtil;
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
 * AdminProductServlet — Servlet xử lý phê duyệt, từ chối và gỡ bỏ sản phẩm vi phạm của Admin.
 *
 * URL: /admin/products
 * GET: Hiển thị danh sách sản phẩm quản trị (chờ duyệt, đã duyệt, bị từ chối...)
 * POST: Thực hiện hành động Duyệt (Approve), Từ chối (Reject), hoặc Gỡ bỏ (Ban) sản phẩm.
 *
 * @author fruitmkt-team
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {"/admin/products"})
public class AdminProductServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminProductServlet.class.getName());

    private final ProductService productService = new ProductService();
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
            int page = 1;
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr.trim());
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    LoggerUtil.warn(log, "Tham số page không hợp lệ: " + pageStr, e);
                }
            }

            String approvalStatus = req.getParameter("approvalStatus");
            if (approvalStatus != null && approvalStatus.trim().isEmpty()) {
                approvalStatus = null;
            }

            int pageSize = AppConfig.PAGE_SIZE_ADMIN;
            // Gọi qua Service Layer thay vì trực tiếp DAO
            List<Product> products = productService.getAllAdminProducts(page, pageSize, approvalStatus);
            List<Category> categories = categoryService.getAllCategories();

            req.setAttribute("products", products);
            req.setAttribute("categories", categories);
            req.setAttribute("currentPage", page);
            req.setAttribute("paramApprovalStatus", approvalStatus);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-products.jsp").forward(req, resp);

        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi cơ sở dữ liệu khi tải danh sách sản phẩm admin", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            SessionUtil.flashError(session, "Hành động không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }

        try {
            switch (action) {
                case "approve":
                    approveProduct(req, resp, session);
                    break;
                case "reject":
                    rejectProduct(req, resp, session);
                    break;
                case "ban":
                    banProduct(req, resp, session);
                    break;
                default:
                    SessionUtil.flashError(session, "Hành động không hợp lệ.");
                    resp.sendRedirect(req.getContextPath() + "/admin/products");
            }
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi hệ thống khi xử lý hành động sản phẩm: " + action, e);
            SessionUtil.flashError(session, "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/products");
        }
    }

    private void approveProduct(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws SQLException, IOException {
        // Bug fix: bắt buộc validate trước khi parse — tránh NumberFormatException khi tham số rỗng hoặc giả mạo
        int productId;
        int categoryId;
        try {
            productId = Integer.parseInt(req.getParameter("productId"));
            categoryId = Integer.parseInt(req.getParameter("categoryId"));
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Dữ liệu gửi lên không hợp lệ (productId / categoryId).");
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }
        boolean isOrganic  = req.getParameter("isOrganic")  != null;
        boolean isImported = req.getParameter("isImported") != null;

        try {
            boolean success = productService.approveProduct(productId, isOrganic, isImported, categoryId);
            if (success) {
                SessionUtil.flashSuccess(session, "Đã phê duyệt sản phẩm thành công và đưa lên sàn.");
            } else {
                SessionUtil.flashError(session, "Phê duyệt sản phẩm thất bại.");
            }
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private void rejectProduct(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws SQLException, IOException {
        // Bug fix: bắt buộc validate trước khi parse — tránh NumberFormatException khi tham số rỗng hoặc giả mạo
        int productId;
        try {
            productId = Integer.parseInt(req.getParameter("productId"));
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Dữ liệu gửi lên không hợp lệ (productId).");
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }
        String reason = req.getParameter("reason");

        try {
            boolean success = productService.rejectProduct(productId, reason);
            if (success) {
                SessionUtil.flashSuccess(session, "Đã từ chối phê duyệt sản phẩm.");
            } else {
                SessionUtil.flashError(session, "Từ chối phê duyệt sản phẩm thất bại.");
            }
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private void banProduct(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws SQLException, IOException {
        // Bug fix: bắt buộc validate trước khi parse — tránh NumberFormatException khi tham số rỗng hoặc giả mạo
        int productId;
        try {
            productId = Integer.parseInt(req.getParameter("productId"));
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Dữ liệu gửi lên không hợp lệ (productId).");
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }

        try {
            boolean success = productService.banProduct(productId);
            if (success) {
                SessionUtil.flashSuccess(session, "Đã gỡ bỏ sản phẩm vi phạm khỏi website.");
            } else {
                SessionUtil.flashError(session, "Gỡ bỏ sản phẩm thất bại.");
            }
        } catch (IllegalArgumentException e) {
            SessionUtil.flashError(session, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }
}
