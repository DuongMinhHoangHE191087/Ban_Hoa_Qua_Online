package servlet.shop.product;

import config.AppConfig;
import dao.catalog.ProductDAO;
import dao.catalog.ProductImageDAO;
import model.entity.catalog.Product;
import model.entity.catalog.ProductImage;
import model.entity.auth.User;
import model.response.ApiResponse;
import util.SessionUtil;
import util.FileUploadUtil;
import util.JsonUtil;

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
 * ProductStatusServlet — API Servlet phục vụ AJAX Toggle, Soft Delete, và Xóa ảnh nhanh
 URL: /shop/product-status
 */
@WebServlet("/shop/product-status")
public class ProductStatusServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ProductStatusServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra đăng nhập
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"));
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ"));
            return;
        }

        try {
            if ("toggle".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("productId"));
                String status = req.getParameter("status");

                if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Trạng thái không hợp lệ"));
                    return;
                }

                List<Product> products = productDAO.findById(productId);
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền chỉnh sửa sản phẩm này"));
                    return;
                }

                Product p = products.get(0);
                String targetStatus = status;
                if ("ACTIVE".equals(status) && p.isExpired()) {
                    targetStatus = "OUT_OF_SEASON";
                }
                productDAO.updateStatus(productId, targetStatus);
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));

            } else if ("delete".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("productId"));

                List<Product> products = productDAO.findById(productId);
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền chỉnh sửa sản phẩm này"));
                    return;
                }

                productDAO.deleteProduct(productId);
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));

            } else if ("delete-image".equals(action)) {
                int imageId = Integer.parseInt(req.getParameter("imageId"));

                ProductImage img = productImageDAO.findById(imageId);
                if (img == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy ảnh"));
                    return;
                }

                List<Product> products = productDAO.findById(img.getProductId());
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền xóa ảnh của sản phẩm này"));
                    return;
                }

                java.io.File imgFile = new java.io.File(getServletContext().getRealPath(""), img.getFilePath());
                FileUploadUtil.delete(imgFile.getAbsolutePath());

                productImageDAO.delete(imageId);
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));

            } else if ("set-primary".equals(action)) {
                int imageId = Integer.parseInt(req.getParameter("imageId"));
                int productId = Integer.parseInt(req.getParameter("productId"));

                List<Product> products = productDAO.findById(productId);
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "Không có quyền chỉnh sửa ảnh này"));
                    return;
                }

                productImageDAO.setPrimary(imageId, productId);
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));

            } else if ("reorder-images".equals(action)) {
                String imageIdsStr = req.getParameter("imageIds");
                if (imageIdsStr == null || imageIdsStr.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Thiếu danh sách imageIds"));
                    return;
                }
                String[] idParts = imageIdsStr.split(",");
                for (int i = 0; i < idParts.length; i++) {
                    try {
                        int imgId = Integer.parseInt(idParts[i].trim());
                        ProductImage img = productImageDAO.findById(imgId);
                        if (img != null) {
                            List<Product> products = productDAO.findById(img.getProductId());
                            if (products != null && !products.isEmpty() && products.get(0).getOwnerId() == currentUser.getUserId()) {
                                productImageDAO.updateDisplayOrder(imgId, i);
                            }
                        }
                    } catch (NumberFormatException e) {
                        LoggerUtil.warn(log, "ID ảnh không hợp lệ khi sắp xếp: " + idParts[i], e);
                    }
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                JsonUtil.writeJson(resp, ApiResponse.ok(null));

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "Hành động không xác định"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJson(resp, ApiResponse.fail(HttpServletResponse.SC_BAD_REQUEST, "ID sai định dạng số"));
        } catch (SQLException e) {
            LoggerUtil.error(log, "Lỗi cơ sở dữ liệu khi cập nhật trạng thái sản phẩm", e);
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "ProductStatusServlet#doPost",
                    "Lỗi hệ thống khi cập nhật trạng thái sản phẩm.",
                    e);
        }
    }
}
