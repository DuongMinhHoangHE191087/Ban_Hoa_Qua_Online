package servlet.admin.product;

import config.AppConfig;
import model.entity.catalog.Category;
import model.entity.catalog.Product;
import model.entity.auth.User;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import service.catalog.CategoryService;
import service.catalog.ProductService;
import util.PaginationUtil;
import util.SessionUtil;
import util.ErrorMessageUtil;

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
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

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
            int page = PaginationUtil.parsePage(req.getParameter("page"));

            String approvalStatus = req.getParameter("approvalStatus");
            if (approvalStatus == null) {
                approvalStatus = "PENDING";
            } else if (approvalStatus.trim().isEmpty() || "ALL".equalsIgnoreCase(approvalStatus.trim())) {
                approvalStatus = null;
            } else {
                approvalStatus = approvalStatus.trim();
            }

            Integer categoryId = null;
            String categoryIdParam = req.getParameter("categoryId");
            if (categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
                try {
                    int parsedCategoryId = Integer.parseInt(categoryIdParam.trim());
                    if (parsedCategoryId > 0) {
                        categoryId = parsedCategoryId;
                    }
                } catch (NumberFormatException ignored) {
                    categoryId = null;
                }
            }

            int pageSize = AppConfig.PAGE_SIZE_ADMIN;
            // Gọi qua Service Layer thay vì trực tiếp DAO
            List<Product> products = productService.getAllAdminProducts(page, pageSize, approvalStatus, categoryId);
            List<Category> categories = categoryService.getAllCategories();
            int totalCount = productService.countAllAdminProducts(approvalStatus, categoryId);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
            viewEnrichmentService.enrichProducts(products);

            req.setAttribute("products", products);
            req.setAttribute("categories", categories);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("paramApprovalStatus", req.getParameter("approvalStatus") == null ? "PENDING" : req.getParameter("approvalStatus").trim());
            req.setAttribute("paramCategoryId", categoryId);
            req.setAttribute("totalItems", totalCount);

            req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-products.jsp").forward(req, resp);

        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(AdminProductServlet.class.getName()),
                    "AdminProductServlet#doGet",
                    ErrorMessageUtil.MSG_DB_ERROR,
                    e);
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
            String userMsg = ErrorMessageUtil.logAndGetUserMessage(log, "Failed to process product action: " + action, e);
            SessionUtil.flashError(session, userMsg);
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
        resp.sendRedirect(buildRedirectUrl(req));
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
        resp.sendRedirect(buildRedirectUrl(req));
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
        resp.sendRedirect(buildRedirectUrl(req));
    }

    private String buildRedirectUrl(HttpServletRequest req) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/admin/products");
        boolean hasQuery = false;

        String page = req.getParameter("page");
        if (page != null && page.trim().matches("\\d+")) {
            url.append("?page=").append(page.trim());
            hasQuery = true;
        }

        String approvalStatus = trim(req.getParameter("approvalStatus"));
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            url.append(hasQuery ? "&" : "?").append("approvalStatus=").append(approvalStatus);
            hasQuery = true;
        }

        String categoryId = trim(req.getParameter("categoryId"));
        if (categoryId != null && categoryId.matches("\\d+")) {
            url.append(hasQuery ? "&" : "?").append("categoryId=").append(categoryId);
        }

        return url.toString();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
