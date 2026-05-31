package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.ProductVariant;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductEditController — Servlet xử lý chỉnh sửa sản phẩm cho shop
 * URL: /shop/product-edit
 */
@WebServlet("/shop/product-edit")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 5, // 25MB
    maxRequestSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 10 // 50MB
)
public class ProductEditController extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String idParam = req.getParameter("id");
        int productId = 0;
        try {
            if (idParam != null) {
                productId = Integer.parseInt(idParam.trim());
            }
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Mã sản phẩm không đúng định dạng.");
            resp.sendRedirect(req.getContextPath() + "/shop/products");
            return;
        }

        try {
            // 1. Tải thông tin sản phẩm và kiểm tra chủ sở hữu
            List<Product> products = productDAO.findById(productId);
            if (products == null || products.isEmpty()) {
                SessionUtil.flashError(session, "Không tìm thấy sản phẩm.");
                resp.sendRedirect(req.getContextPath() + "/shop/products");
                return;
            }

            Product p = products.get(0);
            if (p.getOwnerId() != currentUser.getUserId() || "DELETED".equals(p.getStatus())) {
                SessionUtil.flashError(session, "Bạn không có quyền chỉnh sửa sản phẩm này.");
                resp.sendRedirect(req.getContextPath() + "/shop/products");
                return;
            }

            // 2. Tải các thông tin liên quan
            List<Category> categories = categoryDAO.findAllActive();
            List<ProductImage> images = productImageDAO.findByProduct(productId);
            List<ProductVariant> variants = productVariantDAO.findByProduct(productId);

            ProductVariant v = null;
            if (variants != null && !variants.isEmpty()) {
                v = variants.get(0);
            }

            req.setAttribute("product", p);
            req.setAttribute("categories", categories);
            req.setAttribute("images", images);
            req.setAttribute("variant", v);

            // 3. Forward tới JSP chỉnh sửa
            req.getRequestDispatcher("/WEB-INF/jsp/shop/shop-product-edit.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi truy vấn cơ sở dữ liệu.");
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null || !AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String idParam = req.getParameter("id");
        int productId = 0;
        try {
            if (idParam != null) {
                productId = Integer.parseInt(idParam.trim());
            }
        } catch (NumberFormatException e) {
            SessionUtil.flashError(session, "Mã sản phẩm không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/shop/products");
            return;
        }

        try {
            // Kiểm tra sở hữu sản phẩm
            List<Product> products = productDAO.findById(productId);
            if (products == null || products.isEmpty()) {
                SessionUtil.flashError(session, "Sản phẩm không tồn tại.");
                resp.sendRedirect(req.getContextPath() + "/shop/products");
                return;
            }

            Product p = products.get(0);
            if (p.getOwnerId() != currentUser.getUserId() || "DELETED".equals(p.getStatus())) {
                SessionUtil.flashError(session, "Bạn không có quyền chỉnh sửa sản phẩm này.");
                resp.sendRedirect(req.getContextPath() + "/shop/products");
                return;
            }

            // 1. Đọc các trường thông tin cập nhật
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            String originCountry = req.getParameter("originCountry");
            String originRegion = req.getParameter("originRegion");
            String harvestDateStr = req.getParameter("harvestDate");
            String shelfLifeStr = req.getParameter("shelfLifeDays");
            String storageInstruction = req.getParameter("storageInstruction");
            String categoryIdStr = req.getParameter("categoryId");
            String priceStr = req.getParameter("price");
            String stockStr = req.getParameter("stock");
            String unit = req.getParameter("unit");
            String status = req.getParameter("status");

            List<String> errors = new ArrayList<>();

            // 2. Validate thông tin
            if (name == null || name.trim().isEmpty()) {
                errors.add("Tên sản phẩm không được để trống.");
            }
            if (unit == null || unit.trim().isEmpty()) {
                errors.add("Đơn vị tính không được để trống.");
            }
            if (status == null || (!"ACTIVE".equals(status) && !"INACTIVE".equals(status))) {
                errors.add("Trạng thái hiển thị không hợp lệ.");
            }

            int categoryId = 0;
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                errors.add("Danh mục không hợp lệ.");
            }

            BigDecimal price = BigDecimal.ZERO;
            try {
                price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Giá bán phải lớn hơn 0.");
                }
            } catch (Exception e) {
                errors.add("Giá bán không đúng định dạng số.");
            }

            int stock = 0;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    errors.add("Số lượng tồn kho không được âm.");
                }
            } catch (NumberFormatException e) {
                errors.add("Số lượng tồn kho phải là số nguyên.");
            }

            LocalDate harvestDate = null;
            if (harvestDateStr != null && !harvestDateStr.trim().isEmpty()) {
                try {
                    harvestDate = LocalDate.parse(harvestDateStr);
                } catch (DateTimeParseException e) {
                    errors.add("Ngày thu hoạch không đúng định dạng YYYY-MM-DD.");
                }
            }

            Integer shelfLifeDays = null;
            if (shelfLifeStr != null && !shelfLifeStr.trim().isEmpty()) {
                try {
                    shelfLifeDays = Integer.parseInt(shelfLifeStr);
                    if (shelfLifeDays < 0) {
                        errors.add("Hạn sử dụng không được âm.");
                    }
                } catch (NumberFormatException e) {
                    errors.add("Hạn sử dụng phải là số ngày.");
                }
            }

            // 3. Validate ảnh mới
            List<Part> imageParts = new ArrayList<>();
            try {
                for (Part part : req.getParts()) {
                    if ("images".equals(part.getName()) && part.getSize() > 0) {
                        String filename = part.getSubmittedFileName();
                        if (filename != null && !filename.trim().isEmpty()) {
                            if (!FileUploadUtil.isAllowedImage(filename)) {
                                errors.add("Tệp tin '" + filename + "' không phải là định dạng ảnh được phép (chỉ hỗ trợ: jpg, jpeg, png, webp).");
                            } else {
                                imageParts.add(part);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                errors.add("Lỗi đọc tệp tải lên: " + e.getMessage());
            }

            // 4. Nếu có lỗi, trả lại form chỉnh sửa
            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("product", p);
                req.setAttribute("categories", categoryDAO.findAllActive());
                req.setAttribute("images", productImageDAO.findByProduct(productId));
                List<ProductVariant> variants = productVariantDAO.findByProduct(productId);
                req.setAttribute("variant", (variants != null && !variants.isEmpty()) ? variants.get(0) : null);

                req.getRequestDispatcher("/WEB-INF/jsp/shop/shop-product-edit.jsp").forward(req, resp);
                return;
            }

            // 5. Cập nhật cơ sở dữ liệu
            p.setName(name.trim());
            p.setDescription(description != null ? description.trim() : null);
            p.setCategoryId(categoryId);
            p.setOriginCountry(originCountry != null ? originCountry.trim() : null);
            p.setOriginRegion(originRegion != null ? originRegion.trim() : null);
            p.setHarvestDate(harvestDate);
            p.setShelfLifeDays(shelfLifeDays);
            p.setStorageInstruction(storageInstruction != null ? storageInstruction.trim() : null);
            p.setStatus(status);

            // Gọi DAO cập nhật products
            productDAO.update(p);

            // Cập nhật hoặc lưu mới biến thể sản phẩm đại diện
            List<ProductVariant> variants = productVariantDAO.findByProduct(productId);
            if (variants == null || variants.isEmpty()) {
                ProductVariant v = new ProductVariant();
                v.setProductId(productId);
                v.setSku("SP-" + productId + "-DF");
                v.setVariantLabel(unit.trim());
                v.setPrice(price);
                v.setStockQuantity(stock);
                v.setIsActive(true);
                productVariantDAO.save(v);
            } else {
                ProductVariant v = variants.get(0);
                v.setVariantLabel(unit.trim());
                v.setPrice(price);
                v.setStockQuantity(stock);
                productVariantDAO.update(v);
            }

            // Lưu hình ảnh bổ sung
            String uploadDir = getServletContext().getRealPath("");
            List<ProductImage> existingImages = productImageDAO.findByProduct(productId);
            int nextDisplayOrder = existingImages != null ? existingImages.size() : 0;

            for (Part part : imageParts) {
                String relativePath = FileUploadUtil.save(part, uploadDir);
                if (relativePath != null) {
                    ProductImage img = new ProductImage();
                    img.setProductId(productId);
                    img.setFilePath(relativePath);
                    img.setDisplayOrder(nextDisplayOrder);
                    // Nếu chưa có ảnh nào thì đặt làm ảnh chính, ngược lại là ảnh phụ
                    img.setIsPrimary(nextDisplayOrder == 0);
                    productImageDAO.save(img);
                    nextDisplayOrder++;
                }
            }

            SessionUtil.flashSuccess(session, "Cập nhật thông tin sản phẩm thành công!");
            resp.sendRedirect(req.getContextPath() + "/shop/products");

        } catch (SQLException e) {
            e.printStackTrace();
            SessionUtil.flashError(session, "Lỗi cơ sở dữ liệu khi cập nhật sản phẩm: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }
}
