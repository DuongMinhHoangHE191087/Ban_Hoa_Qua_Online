package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * ProductStatusServlet — API Servlet phục vụ AJAX Toggle, Soft Delete, và Xóa ảnh nhanh
 URL: /shop/product-status
 */
@WebServlet("/shop/product-status")
public class ProductStatusServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        // 1. Kiểm tra đăng nhập
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            out.print("{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Hành động không hợp lệ\"}");
            return;
        }

        try {
            if ("toggle".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("productId"));
                String status = req.getParameter("status");

                if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
                    out.print("{\"success\":false,\"message\":\"Trạng thái không hợp lệ\"}");
                    return;
                }

                // Kiểm tra quyền sở hữu sản phẩm
                List<Product> products = productDAO.findById(productId);
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    out.print("{\"success\":false,\"message\":\"Không có quyền chỉnh sửa sản phẩm này\"}");
                    return;
                }

                productDAO.updateStatus(productId, status);
                out.print("{\"success\":true}");

            } else if ("delete".equals(action)) {
                int productId = Integer.parseInt(req.getParameter("productId"));

                // Kiểm tra quyền sở hữu sản phẩm
                List<Product> products = productDAO.findById(productId);
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    out.print("{\"success\":false,\"message\":\"Không có quyền chỉnh sửa sản phẩm này\"}");
                    return;
                }

                productDAO.deleteProduct(productId);
                out.print("{\"success\":true}");

            } else if ("delete-image".equals(action)) {
                int imageId = Integer.parseInt(req.getParameter("imageId"));

                ProductImage img = productImageDAO.findById(imageId);
                if (img == null) {
                    out.print("{\"success\":false,\"message\":\"Không tìm thấy ảnh\"}");
                    return;
                }

                // Kiểm tra quyền sở hữu sản phẩm của ảnh
                List<Product> products = productDAO.findById(img.getProductId());
                if (products == null || products.isEmpty() || products.get(0).getOwnerId() != currentUser.getUserId()) {
                    out.print("{\"success\":false,\"message\":\"Không có quyền xóa ảnh của sản phẩm này\"}");
                    return;
                }

                // Xóa vật lý tệp ảnh trên đĩa cứng
                String realPath = getServletContext().getRealPath("") + "/" + img.getFilePath();
                FileUploadUtil.delete(realPath);

                // Xóa bản ghi trong DB
                productImageDAO.delete(imageId);
                out.print("{\"success\":true}");

            } else {
                out.print("{\"success\":false,\"message\":\"Hành động không xác định\"}");
            }
        } catch (NumberFormatException e) {
            out.print("{\"success\":false,\"message\":\"ID sai định dạng số\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Lỗi cơ sở dữ liệu: " + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }
}
