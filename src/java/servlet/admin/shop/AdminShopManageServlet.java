package servlet.admin.shop;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import model.dto.common.PagedResultDTO;
import model.entity.shop.ShopProfile;
import service.admin.AdminViewEnrichmentService; // Helper for data enrichment
import service.shop.ShopService;
import util.PaginationUtil;
import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import util.ErrorMessageUtil;

@WebServlet("/admin/shops/manage")
public class AdminShopManageServlet extends HttpServlet {

    private final ShopService shopService = new ShopService();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        req.setAttribute("currentFilter", "ALL");
        req.setAttribute("categories", java.util.Collections.emptyList());

        try {
            int page = PaginationUtil.parsePage(req.getParameter("page"));
            int pageSize = AppConfig.PAGE_SIZE_ADMIN;
            String currentFilter = normalizeFilter(req.getParameter("filter"));
            String queryStatus = AppConfig.SHOP_APPROVED.equals(currentFilter) ? AppConfig.SHOP_APPROVED
                    : AppConfig.SHOP_PENDING.equals(currentFilter) ? AppConfig.SHOP_PENDING
                    : AppConfig.SHOP_REJECTED.equals(currentFilter) ? AppConfig.SHOP_REJECTED
                    : AppConfig.SHOP_SUSPENDED.equals(currentFilter) ? AppConfig.SHOP_SUSPENDED
                    : null;
            PagedResultDTO shopPage = shopService.getShopsPaged(queryStatus, page, pageSize);
            @SuppressWarnings("unchecked")
            List<ShopProfile> profiles = (List<ShopProfile>) shopPage.getItems();
            viewEnrichmentService.enrichShopProfiles(profiles);
            req.setAttribute("shopList", shopPage.getItems());
            req.setAttribute("currentFilter", currentFilter);
            req.setAttribute("currentPage", shopPage.getCurrentPage());
            req.setAttribute("totalPages", shopPage.getTotalPages());
            req.setAttribute("totalItems", shopPage.getTotalItems());
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException e) {
            getServletContext().log("AdminShopManageServlet GET error", e);
            req.setAttribute("errorMsg", "Không thể tải danh sách cửa hàng.");
        }

        req.getRequestDispatcher("/WEB-INF/jsp/admin/admin-shops.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req.getSession(), AppConfig.ROLE_ADMIN)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền.");
            return;
        }

        String action = req.getParameter("action");
        String profileIdStr = req.getParameter("profileId");

        try {
            if (profileIdStr == null || profileIdStr.isEmpty()) {
                throw new Exception("Thiếu profileId");
            }
            int profileId = Integer.parseInt(profileIdStr);

            if ("suspend".equals(action)) {
                shopService.updateShopStatus(profileId, AppConfig.SHOP_SUSPENDED, null);
                SessionUtil.flashSuccess(req.getSession(), "Đã đình chỉ cửa hàng.");
            } else if ("reject".equals(action)) {
                String rejectionReason = req.getParameter("rejectionReason");
                shopService.updateShopStatus(profileId, AppConfig.SHOP_REJECTED, rejectionReason);
                SessionUtil.flashSuccess(req.getSession(), "Đã từ chối cửa hàng.");
            } else if ("activate".equals(action)) {
                shopService.updateShopStatus(profileId, AppConfig.SHOP_APPROVED, null);
                SessionUtil.flashSuccess(req.getSession(), "Đã khôi phục hoạt động cửa hàng.");
            } else {
                throw new Exception("Hành động không hợp lệ: " + action);
            }
        } catch (Exception e) {
            getServletContext().log("AdminShopManageServlet POST error: " + util.ErrorMessageUtil.getSafeLogMessage(e), e);
            SessionUtil.flashError(req.getSession(), ErrorMessageUtil.getUserMessage(e));
        }

        resp.sendRedirect(buildRedirectUrl(req));
    }

    private String buildRedirectUrl(HttpServletRequest req) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/admin/shops/manage");
        String filter = normalizeFilter(req.getParameter("filter"));
        String page = req.getParameter("page");
        boolean hasQuery = false;
        if (page != null && page.trim().matches("\\d+")) {
            url.append("?page=").append(page.trim());
            hasQuery = true;
        }
        if (filter != null && !filter.isEmpty()) {
            url.append(hasQuery ? "&" : "?").append("filter=").append(filter);
        }
        return url.toString();
    }

    private String normalizeFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return "ALL";
        }
        String normalized = filter.trim().toUpperCase();
        return switch (normalized) {
            case "ALL", AppConfig.SHOP_PENDING, AppConfig.SHOP_APPROVED, AppConfig.SHOP_REJECTED, AppConfig.SHOP_SUSPENDED -> normalized;
            default -> "ALL";
        };
    }
}
