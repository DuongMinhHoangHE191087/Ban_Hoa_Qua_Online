package servlet.admin.product;

import config.AppConfig;
import model.entity.auth.User;
import model.entity.catalog.Product;
import model.response.ApiResponse;
import service.admin.AdminViewEnrichmentService;
import service.catalog.ProductService;
import util.JsonUtil;
import util.LoggerUtil;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/api/admin/products/detail")
public class AdminProductDetailAPI extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminProductDetailAPI.class.getName());
    private final ProductService productService = new ProductService();
    private final AdminViewEnrichmentService viewEnrichmentService = new AdminViewEnrichmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        if (currentUser == null || !AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập."));
            return;
        }

        try {
            int productId = Integer.parseInt(req.getParameter("id"));
            Product product = productService.getProductDetail(productId);
            if (product == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm."));
                return;
            }
            viewEnrichmentService.enrichProducts(java.util.Collections.singletonList(product));

            Map<String, Object> data = new HashMap<>();
            data.put("productId", product.getProductId());
            data.put("ownerId", product.getOwnerId());
            data.put("ownerName", textOrNull(product.getOwnerName()));
            data.put("shopName", textOrNull(product.getShopName()));
            data.put("categoryId", product.getCategoryId());
            data.put("categoryName", textOrNull(product.getCategoryName()));
            data.put("name", textOrNull(product.getName()));
            data.put("description", textOrNull(product.getDescription()));
            data.put("originCountry", textOrNull(product.getOriginCountry()));
            data.put("originRegion", textOrNull(product.getOriginRegion()));
            data.put("harvestDate", product.getHarvestDate() == null ? null : product.getHarvestDate().toString());
            data.put("shelfLifeDays", product.getShelfLifeDays());
            data.put("storageInstruction", textOrNull(product.getStorageInstruction()));
            data.put("status", textOrNull(product.getStatus()));
            data.put("viewCount", product.getViewCount());
            data.put("rating", product.getRating() == null ? null : product.getRating().toPlainString());
            data.put("soldQuantity", product.getSoldQuantity());
            data.put("createdAt", product.getCreatedAt() == null ? null : product.getCreatedAt().toString());
            data.put("updatedAt", product.getUpdatedAt() == null ? null : product.getUpdatedAt().toString());
            data.put("isOrganic", product.getIsOrganic());
            data.put("isImported", product.getIsImported());
            data.put("seasonStartMonth", product.getSeasonStartMonth());
            data.put("seasonEndMonth", product.getSeasonEndMonth());
            data.put("seasonLabel", textOrNull(product.getSeasonLabel()));
            data.put("inSeason", product.isInSeason());
            data.put("approvalStatus", textOrNull(product.getApprovalStatus()));
            data.put("verificationDocPath", textOrNull(product.getVerificationDocPath()));
            data.put("rejectionReason", textOrNull(product.getRejectionReason()));

            resp.setStatus(HttpServletResponse.SC_OK);
            JsonUtil.writeJson(resp, ApiResponse.ok(data, "Lấy thông tin sản phẩm thành công"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ."));
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi khi lấy chi tiết sản phẩm", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi server."));
        }
    }

    private String textOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
