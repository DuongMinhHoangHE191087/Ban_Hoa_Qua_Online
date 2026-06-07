package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ProductDAO;
import com.fruitmkt.dao.ProductImageDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.dao.ProductPackagingOptionDAO;
import com.fruitmkt.dao.ProductVariantDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.Product;
import com.fruitmkt.model.entity.ProductImage;
import com.fruitmkt.model.entity.ProductPackagingOption;
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
import java.util.Set;
import java.util.HashSet;

/**
 * ProductEditServlet — Servlet xử lý chỉnh sửa sản phẩm cho shop
 URL: /shop/product-edit
 */
@WebServlet("/shop/product-edit")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 5, // 25MB
    maxRequestSize = AppConfig.MAX_UPLOAD_SIZE_BYTES * 10 // 50MB
)
public class ProductEditServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        // Content-type set by JsonUtil for AJAX; set HTML only for non-AJAX fallback
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
            com.fruitmkt.dao.ProductPackagingOptionDAO packagingOptionDAO = new com.fruitmkt.dao.ProductPackagingOptionDAO();
            List<com.fruitmkt.model.entity.ProductPackagingOption> packagingOptions = packagingOptionDAO.findByProduct(productId);

            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", true);
                responseData.put("product", p);
                responseData.put("images", images);
                responseData.put("variants", variants);
                responseData.put("packagingOptions", packagingOptions);
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/shop/products");

        } catch (SQLException e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", false);
                responseData.put("message", "Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage());
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }
            SessionUtil.flashError(session, "Lỗi truy vấn cơ sở dữ liệu.");
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        // Content-type set by JsonUtil for AJAX; HTML set only for redirect responses
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
            String status = req.getParameter("status");
            
            boolean isOrganic = req.getParameter("isOrganic") != null;
            boolean isImported = req.getParameter("isImported") != null;
            String seasonStartMonthStr = req.getParameter("seasonStartMonth");
            String seasonEndMonthStr = req.getParameter("seasonEndMonth");
            
            // Đọc danh sách biến thể
            String[] variantIds = req.getParameterValues("variantId");
            String[] variantLabels = req.getParameterValues("variantLabel");
            String[] variantPrices = req.getParameterValues("variantPrice");
            String[] variantStocks = req.getParameterValues("variantStock");
            String[] variantWeights = req.getParameterValues("variantWeight");
            String[] variantDiscountPrices = req.getParameterValues("variantDiscountPrice");
            String[] variantDiscountStarts = req.getParameterValues("variantDiscountStart");
            String[] variantDiscountEnds = req.getParameterValues("variantDiscountEnd");

            // Đọc danh sách đóng gói chọn thêm
            String[] packagingIds = req.getParameterValues("packagingId");
            String[] packagingLabels = req.getParameterValues("packagingLabel");
            String[] packagingPriceAdds = req.getParameterValues("packagingPriceAdd");

            List<String> errors = new ArrayList<>();

            // 2. Validate thông tin
            if (name == null || name.trim().isEmpty()) {
                errors.add("Tên sản phẩm không được để trống.");
            }
            if (status == null || (!"ACTIVE".equals(status) && !"INACTIVE".equals(status) && !"OUT_OF_SEASON".equals(status))) {
                errors.add("Trạng thái hiển thị không hợp lệ.");
            }

            int categoryId = 0;
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                errors.add("Danh mục không hợp lệ.");
            }

            // Validate danh sách biến thể (chỉ validate tên và giá; stock do inventory quản lý)
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

                    String wStr = (variantWeights != null && variantWeights.length > i) ? variantWeights[i] : null;
                    if (wStr != null && !wStr.trim().isEmpty()) {
                        try {
                            BigDecimal weight = new BigDecimal(wStr);
                            if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                                errors.add("Cân nặng của phân loại '" + (label != null ? label : "") + "' phải lớn hơn 0.");
                            }
                        } catch (Exception e) {
                            errors.add("Cân nặng của phân loại '" + (label != null ? label : "") + "' không đúng định dạng số.");
                        }
                    }

                    String dpStr = (variantDiscountPrices != null && variantDiscountPrices.length > i) ? variantDiscountPrices[i] : null;
                    if (dpStr != null && !dpStr.trim().isEmpty()) {
                        try {
                            BigDecimal dp = new BigDecimal(dpStr);
                            if (dp.compareTo(BigDecimal.ZERO) < 0) {
                                errors.add("Giá khuyến mãi của phân loại '" + (label != null ? label : "") + "' không được âm.");
                            }
                            BigDecimal normalPrice = new BigDecimal(pStr);
                            if (dp.compareTo(normalPrice) >= 0) {
                                errors.add("Giá khuyến mãi của phân loại '" + (label != null ? label : "") + "' phải nhỏ hơn giá gốc.");
                            }
                        } catch (Exception e) {
                            errors.add("Giá khuyến mãi của phân loại '" + (label != null ? label : "") + "' không đúng định dạng số.");
                        }
                        
                        String dsStr = (variantDiscountStarts != null && variantDiscountStarts.length > i) ? variantDiscountStarts[i] : null;
                        String deStr = (variantDiscountEnds != null && variantDiscountEnds.length > i) ? variantDiscountEnds[i] : null;
                        if (dsStr == null || dsStr.trim().isEmpty() || deStr == null || deStr.trim().isEmpty()) {
                            errors.add("Phân loại '" + (label != null ? label : "") + "' cấu hình giảm giá phải có cả ngày bắt đầu và kết thúc.");
                        } else {
                            try {
                                java.time.LocalDateTime.parse(dsStr.trim());
                                java.time.LocalDateTime.parse(deStr.trim());
                            } catch (Exception e) {
                                errors.add("Ngày giảm giá của phân loại '" + (label != null ? label : "") + "' không đúng định dạng ISO (YYYY-MM-DDTHH:MM).");
                            }
                        }
                    }
                }
            }

            Integer seasonStartMonth = null;
            if (seasonStartMonthStr != null && !seasonStartMonthStr.trim().isEmpty()) {
                try {
                    seasonStartMonth = Integer.parseInt(seasonStartMonthStr.trim());
                    if (seasonStartMonth < 1 || seasonStartMonth > 12) {
                        errors.add("Tháng bắt đầu mùa vụ phải từ 1 đến 12.");
                    }
                } catch (NumberFormatException e) {
                    errors.add("Tháng bắt đầu mùa vụ phải là số nguyên.");
                }
            }

            Integer seasonEndMonth = null;
            if (seasonEndMonthStr != null && !seasonEndMonthStr.trim().isEmpty()) {
                try {
                    seasonEndMonth = Integer.parseInt(seasonEndMonthStr.trim());
                    if (seasonEndMonth < 1 || seasonEndMonth > 12) {
                        errors.add("Tháng kết thúc mùa vụ phải từ 1 đến 12.");
                    }
                } catch (NumberFormatException e) {
                    errors.add("Tháng kết thúc mùa vụ phải là số nguyên.");
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
                    errors.add("Hạn sử dụng phải là số ngày.");
                }
            }

            // 3. Validate ảnh mới và tài liệu xác minh mới (nếu có)
            List<Part> imageParts = new ArrayList<>();
            Part verificationDocPart = null;
            try {
                verificationDocPart = req.getPart("verificationDoc");
                if (verificationDocPart != null && verificationDocPart.getSize() > 0) {
                    String filename = verificationDocPart.getSubmittedFileName();
                    if (!FileUploadUtil.isAllowedDoc(filename)) {
                        errors.add("Giấy tờ xác nhận nông sản không đúng định dạng (chỉ hỗ trợ: pdf, jpg, jpeg, png, docx).");
                    }
                }
                
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

            // 5. Cập nhật cơ sở dữ liệu
            if (verificationDocPart != null && verificationDocPart.getSize() > 0) {
                String uploadDir = getServletContext().getRealPath("");
                String docPath = FileUploadUtil.saveShopDoc(verificationDocPart, uploadDir, currentUser.getUserId());
                p.setVerificationDocPath(docPath);
            }

            p.setName(name.trim());
            p.setDescription(description != null ? description.trim() : null);
            p.setCategoryId(categoryId);
            p.setOriginCountry(originCountry != null ? originCountry.trim() : null);
            p.setOriginRegion(originRegion != null ? originRegion.trim() : null);
            p.setHarvestDate(harvestDate);
            p.setShelfLifeDays(shelfLifeDays);
            p.setStorageInstruction(storageInstruction != null ? storageInstruction.trim() : null);
            p.setStatus(status);
            p.setIsOrganic(isOrganic);
            p.setIsImported(isImported);
            p.setSeasonStartMonth(seasonStartMonth);
            p.setSeasonEndMonth(seasonEndMonth);
            
            // Mỗi lần cập nhật thông tin sẽ chuyển về trạng thái chờ duyệt PENDING
            p.setApprovalStatus("PENDING");

            // Gọi DAO cập nhật products
            productDAO.update(p);

            // Cập nhật danh sách biến thể
            List<ProductVariant> existingVariants = productVariantDAO.findByProduct(productId);
            Set<Integer> submittedIds = new HashSet<>();
            if (variantIds != null) {
                for (String vidStr : variantIds) {
                    if (vidStr != null && !vidStr.trim().isEmpty()) {
                        try {
                            submittedIds.add(Integer.parseInt(vidStr.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // 1. Soft-delete các biến thể đã bị người dùng xóa trên UI
            if (existingVariants != null) {
                for (ProductVariant ev : existingVariants) {
                    if (!submittedIds.contains(ev.getVariantId())) {
                        productVariantDAO.deactivate(ev.getVariantId());
                    }
                }
            }

            // 2. Insert hoặc Update các biến thể gửi lên
            if (variantLabels != null) {
                for (int i = 0; i < variantLabels.length; i++) {
                    String label = variantLabels[i].trim();
                    
                    BigDecimal vPrice = BigDecimal.ZERO;
                    if (variantPrices != null && variantPrices.length > i && variantPrices[i] != null) {
                        try {
                            vPrice = new BigDecimal(variantPrices[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    Integer currentVid = null;
                    if (variantIds != null && variantIds.length > i && variantIds[i] != null && !variantIds[i].trim().isEmpty()) {
                        try {
                            currentVid = Integer.parseInt(variantIds[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    BigDecimal vWeight = new BigDecimal("1.000");
                    if (variantWeights != null && variantWeights.length > i && variantWeights[i] != null && !variantWeights[i].trim().isEmpty()) {
                        try {
                            vWeight = new BigDecimal(variantWeights[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    BigDecimal vDiscPrice = null;
                    if (variantDiscountPrices != null && variantDiscountPrices.length > i && variantDiscountPrices[i] != null && !variantDiscountPrices[i].trim().isEmpty()) {
                        try {
                            vDiscPrice = new BigDecimal(variantDiscountPrices[i].trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    java.time.LocalDateTime vDiscStart = null;
                    if (variantDiscountStarts != null && variantDiscountStarts.length > i && variantDiscountStarts[i] != null && !variantDiscountStarts[i].trim().isEmpty()) {
                        try {
                            vDiscStart = java.time.LocalDateTime.parse(variantDiscountStarts[i].trim());
                        } catch (Exception ignored) {}
                    }

                    java.time.LocalDateTime vDiscEnd = null;
                    if (variantDiscountEnds != null && variantDiscountEnds.length > i && variantDiscountEnds[i] != null && !variantDiscountEnds[i].trim().isEmpty()) {
                        try {
                            vDiscEnd = java.time.LocalDateTime.parse(variantDiscountEnds[i].trim());
                        } catch (Exception ignored) {}
                    }

                    if (currentVid == null || currentVid == 0) {
                        // Thêm mới biến thể
                        ProductVariant v = new ProductVariant();
                        v.setProductId(productId);
                        v.setSku("SP-" + productId + "-" + System.currentTimeMillis() + "-" + i);
                        v.setVariantLabel(label);
                        v.setPrice(vPrice);
                        v.setStockQuantity(0); // Tồn kho ban đầu = 0; cập nhật qua module Nhập kho
                        v.setWeightKg(vWeight);
                        v.setDiscountPrice(vDiscPrice);
                        v.setDiscountStart(vDiscStart);
                        v.setDiscountEnd(vDiscEnd);
                        v.setIsActive(true);
                        productVariantDAO.save(v);
                    } else {
                        // Cập nhật biến thể cũ
                        ProductVariant v = null;
                        for (ProductVariant ev : existingVariants) {
                            if (ev.getVariantId() == currentVid) {
                                v = ev;
                                break;
                            }
                        }
                        if (v == null) {
                            v = productVariantDAO.findById(currentVid);
                        }
                        if (v != null) {
                            v.setVariantLabel(label);
                            v.setPrice(vPrice);
                            v.setWeightKg(vWeight);
                            v.setDiscountPrice(vDiscPrice);
                            v.setDiscountStart(vDiscStart);
                            v.setDiscountEnd(vDiscEnd);
                            productVariantDAO.update(v);
                        }
                    }
                }
            }

            // 3. Cập nhật danh sách bao bì tùy chọn
            ProductPackagingOptionDAO ppoDAO = new ProductPackagingOptionDAO();
            List<Integer> keepPackagingIds = new ArrayList<>();
            if (packagingLabels != null) {
                for (int i = 0; i < packagingLabels.length; i++) {
                    String pLabel = packagingLabels[i];
                    if (pLabel != null && !pLabel.trim().isEmpty()) {
                        BigDecimal priceAdd = BigDecimal.ZERO;
                        if (packagingPriceAdds != null && packagingPriceAdds.length > i && packagingPriceAdds[i] != null) {
                            try {
                                priceAdd = new BigDecimal(packagingPriceAdds[i].trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        
                        Integer currentPpid = null;
                        if (packagingIds != null && packagingIds.length > i && packagingIds[i] != null && !packagingIds[i].trim().isEmpty()) {
                            try {
                                currentPpid = Integer.parseInt(packagingIds[i].trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        
                        if (currentPpid == null || currentPpid == 0) {
                            // Thêm mới
                            ProductPackagingOption option = new ProductPackagingOption();
                            option.setProductId(productId);
                            option.setLabel(pLabel.trim());
                            option.setPriceAdd(priceAdd);
                            option.setIsActive(true);
                            int newId = ppoDAO.save(option);
                            keepPackagingIds.add(newId);
                        } else {
                            // Cập nhật
                            ProductPackagingOption option = ppoDAO.findById(currentPpid);
                            if (option != null) {
                                option.setLabel(pLabel.trim());
                                option.setPriceAdd(priceAdd);
                                ppoDAO.update(option);
                                keepPackagingIds.add(currentPpid);
                            }
                        }
                    }
                }
            }
            // Xóa các bao bì không nằm trong danh sách giữ lại
            ppoDAO.deleteByProductExcept(productId, keepPackagingIds);

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

            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Cập nhật sản phẩm thành công!");
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }

            SessionUtil.flashSuccess(session, "Cập nhật thông tin sản phẩm thành công!");
            resp.sendRedirect(req.getContextPath() + "/shop/products");

        } catch (SQLException e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("success", false);
                responseData.put("errors", List.of("Lỗi cơ sở dữ liệu khi cập nhật sản phẩm: " + e.getMessage()));
                com.fruitmkt.util.JsonUtil.writeJson(resp, responseData);
                return;
            }
            SessionUtil.flashError(session, "Lỗi cơ sở dữ liệu khi cập nhật sản phẩm: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/shop/products");
        }
    }
}
