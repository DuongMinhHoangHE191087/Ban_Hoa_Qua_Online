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
 * ProductCreateServlet — Servlet xử lý thêm mới sản phẩm và upload ảnh cho shop
 URL: /shop/product-create
 */
@WebServlet("/shop/product-create")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 5, // 25MB
    maxRequestSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 10 // 50MB
)//upload file
public class ProductCreateServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/shop/products");
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

        // 1. Đọc các trường thông tin
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String originCountry = req.getParameter("originCountry");
        String originRegion = req.getParameter("originRegion");
        String harvestDateStr = req.getParameter("harvestDate");
        String shelfLifeStr = req.getParameter("shelfLifeDays");
        String storageInstruction = req.getParameter("storageInstruction");
        String categoryIdStr = req.getParameter("categoryId");
        
        // Đọc danh sách biến thể
        String[] variantLabels = req.getParameterValues("variantLabel");
        String[] variantPrices = req.getParameterValues("variantPrice");
        String[] variantStocks = req.getParameterValues("variantStock");

        // Giữ lại giá trị cũ phòng khi validate thất bại
        req.setAttribute("oldName", name);
        req.setAttribute("oldDescription", description);
        req.setAttribute("oldOriginCountry", originCountry);
        req.setAttribute("oldOriginRegion", originRegion);
        req.setAttribute("oldHarvestDate", harvestDateStr);
        req.setAttribute("oldShelfLife", shelfLifeStr);
        req.setAttribute("oldStorageInstruction", storageInstruction);
        req.setAttribute("oldCategoryId", categoryIdStr);

        List<String> errors = new ArrayList<>();

        // 2. Validate dữ liệu cơ bản
        if (name == null || name.trim().isEmpty()) {
            errors.add("Tên sản phẩm không được để trống.");
        }

        int categoryId = 0;
        try {
            categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException e) {
            errors.add("Vui lòng chọn danh mục hợp lệ.");
        }

        // Validate danh sách biến thể
        if (variantLabels == null || variantLabels.length == 0) {
            errors.add("Sản phẩm phải có ít nhất một phân loại/biến thể.");
        } else {
            for (int i = 0; i < variantLabels.length; i++) {
                String label = variantLabels[i];
                if (label == null || label.trim().isEmpty()) {
                    errors.add("Tên phân loại tại vị trí thứ " + (i + 1) + " không được để trống.");
                }
                
                String pStr = (variantPrices != null && variantPrices.length > i) ? variantPrices[i] : null;
                BigDecimal price = BigDecimal.ZERO;
                try {
                    price = new BigDecimal(pStr);
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Giá bán phân loại '" + (label != null ? label : "") + "' phải lớn hơn 0.");
                    }
                } catch (Exception e) {
                    errors.add("Giá bán phân loại '" + (label != null ? label : "") + "' không đúng định dạng số.");
                }
                
                String sStr = (variantStocks != null && variantStocks.length > i) ? variantStocks[i] : null;
                try {
                    int stock = Integer.parseInt(sStr);
                    if (stock < 0) {
                        errors.add("Số lượng tồn kho phân loại '" + (label != null ? label : "") + "' không được âm.");
                    }
                } catch (NumberFormatException e) {
                    errors.add("Số lượng tồn kho phân loại '" + (label != null ? label : "") + "' phải là số nguyên.");
                }
            }
        }

        LocalDate harvestDate = null;
        if (harvestDateStr != null && !harvestDateStr.trim().isEmpty()) {
            try {
                harvestDate = LocalDate.parse(harvestDateStr);
                if (harvestDate.isAfter(LocalDate.now())) {
                    errors.add("Ngày thu hoạch không được vượt quá ngày hiện tại.");
                }
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
                errors.add("Hạn sử dụng phải là số ngày (nguyên dương).");
            }
        }

        // 3. Validate file ảnh trước khi ghi xuống đĩa
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

        // 4. Nếu có lỗi, chuyển ngược lại form
        if (!errors.isEmpty()) {
            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", false);
                responseData.put("errors", errors);
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }
            SessionUtil.flashError(session, String.join("<br>", errors));
            resp.sendRedirect(req.getContextPath() + "/shop/products");
            return;
        }

        // 5. Lưu thông tin vào Database
        try {
            Product p = new Product();
            p.setOwnerId(currentUser.getUserId());
            p.setCategoryId(categoryId);
            p.setName(name.trim());
            p.setDescription(description != null ? description.trim() : null);
            p.setOriginCountry(originCountry != null ? originCountry.trim() : null);
            p.setOriginRegion(originRegion != null ? originRegion.trim() : null);
            p.setHarvestDate(harvestDate);
            p.setShelfLifeDays(shelfLifeDays);
            p.setStorageInstruction(storageInstruction != null ? storageInstruction.trim() : null);
            p.setStatus("ACTIVE");

            // Lưu sản phẩm và nhận ID tự sinh
            int productId = productDAO.save(p);

            // Lưu hình ảnh sản phẩm
            String uploadDir = getServletContext().getRealPath("");
            int imageIndex = 0;
            for (Part part : imageParts) {
                String relativePath = FileUploadUtil.save(part, uploadDir);
                if (relativePath != null) {
                    ProductImage img = new ProductImage();
                    img.setProductId(productId);
                    img.setFilePath(relativePath);
                    img.setDisplayOrder(imageIndex);
                    img.setIsPrimary(imageIndex == 0);
                    productImageDAO.save(img);
                    imageIndex++;
                }
            }

            // Lưu danh sách các biến thể sản phẩm
            if (variantLabels != null) {
                for (int i = 0; i < variantLabels.length; i++) {
                    ProductVariant variant = new ProductVariant();
                    variant.setProductId(productId);
                    variant.setSku("SP-" + productId + "-" + (i + 1));
                    variant.setVariantLabel(variantLabels[i].trim());
                    
                    BigDecimal vPrice = BigDecimal.ZERO;
                    if (variantPrices != null && variantPrices.length > i && variantPrices[i] != null) {
                        try {
                            vPrice = new BigDecimal(variantPrices[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    variant.setPrice(vPrice);

                    int vStock = 0;
                    if (variantStocks != null && variantStocks.length > i && variantStocks[i] != null) {
                        try {
                            vStock = Integer.parseInt(variantStocks[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    variant.setStockQuantity(vStock);

                    variant.setIsActive(true);
                    productVariantDAO.save(variant);
                }
            }

            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Thêm sản phẩm mới thành công!");
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }

            SessionUtil.flashSuccess(session, "Thêm sản phẩm mới thành công!");
            resp.sendRedirect(req.getContextPath() + "/shop/products");

        } catch (SQLException e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", false);
                responseData.put("errors", List.of("Lỗi cơ sở dữ liệu khi lưu sản phẩm: " + e.getMessage()));
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }
            SessionUtil.flashError(session, "Lỗi cơ sở dữ liệu khi lưu sản phẩm: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }
}
