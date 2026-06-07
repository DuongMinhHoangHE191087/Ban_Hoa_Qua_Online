package com.fruitmkt.servlet.customer;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.util.FileUploadUtil;
import com.fruitmkt.util.SessionUtil;
import com.fruitmkt.service.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ShopApplyServlet — Cho phép CUSTOMER đã đăng nhập nộp đơn đăng ký mở shop.
 *
 * URL: /customer/shop-apply
 * GET : Hiển thị form nộp đơn (kiểm tra trạng thái hiện tại)
 * POST: Xử lý đơn đăng ký + upload tài liệu
 *
 * Nghiệp vụ:
 * - Chỉ CUSTOMER đã verify email mới được nộp đơn
 * - Nếu đã có đơn PENDING hoặc APPROVED → báo lỗi
 * - Sau nộp đơn: user vẫn là CUSTOMER cho đến khi Admin APPROVE
 * - Khi Admin APPROVE: ShopProfileDAO.updateApprovalStatus() sẽ đổi role → SHOP_OWNER
 *
 * @author fruitmkt-team
 */
@WebServlet("/customer/shop-apply")
@MultipartConfig(
    maxFileSize    = 26_214_400L,   // 25MB mỗi file
    maxRequestSize = 264_241_152L,  // ~252MB tổng
    fileSizeThreshold = 1_048_576   // 1MB buffer
)
public class ShopApplyServlet extends HttpServlet {

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User currentUser = (User) req.getSession().getAttribute(AppConfig.SESSION_USER);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            // Kiểm tra xem user đã có shop profile chưa
            List<ShopProfile> existingProfiles = shopProfileDAO.findByUserId(currentUser.getUserId());
            if (!existingProfiles.isEmpty()) {
                ShopProfile existing = existingProfiles.get(0);
                req.setAttribute("existingProfile", existing);
            }

            // Load danh mục active
            List<Category> categories = categoryDAO.findAllActive();
            req.setAttribute("categories", categories);

        } catch (SQLException e) {
            getServletContext().log("ShopApplyServlet GET error", e);
            req.setAttribute("categories", new ArrayList<>());
        }

        req.getRequestDispatcher("/WEB-INF/jsp/customer/shop-apply.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        User currentUser = (User) req.getSession().getAttribute(AppConfig.SESSION_USER);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        // Kiểm tra CSRF
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            forwardWithError(req, resp, currentUser, "CSRF token không hợp lệ. Vui lòng thử lại.");
            return;
        }

        String shopName = req.getParameter("shopName");
        String shopAddress = req.getParameter("shopAddress");
        String shopDescription = req.getParameter("shopDescription");

        try {
            // Check and update user phone if missing
            if (currentUser.getPhone() == null || currentUser.getPhone().trim().isEmpty()) {
                String userPhone = req.getParameter("userPhone");
                userPhone = com.fruitmkt.util.ValidationUtil.requireValidPhone(userPhone, "Số điện thoại");
                currentUser.setPhone(userPhone);
                new com.fruitmkt.dao.UserDAO().update(currentUser);
                req.getSession().setAttribute(AppConfig.SESSION_USER, currentUser);
            }

            // 1. Validate input cơ bản bằng ValidationUtil
            shopName = com.fruitmkt.util.ValidationUtil.requireValidShopName(shopName, "Tên cửa hàng");
            shopAddress = com.fruitmkt.util.ValidationUtil.requireValidAddress(shopAddress, "Địa chỉ kinh doanh");

            // 2. Kiểm tra trùng: user đã có profile PENDING hoặc APPROVED chưa
            List<ShopProfile> existing = shopProfileDAO.findByUserId(currentUser.getUserId());
            if (!existing.isEmpty()) {
                ShopProfile profile = existing.get(0);
                if ("PENDING".equals(profile.getApprovalStatus())) {
                    throw new Exception("Bạn đã có đơn đăng ký shop đang chờ duyệt. Vui lòng chờ Admin xem xét.");
                }
                if ("APPROVED".equals(profile.getApprovalStatus())) {
                    throw new Exception("Tài khoản của bạn đã được phê duyệt là Shop Owner rồi.");
                }
                // Nếu REJECTED → cho nộp lại bằng cách cập nhật profile cũ
                if ("REJECTED".equals(profile.getApprovalStatus())) {
                    resubmitApplication(req, resp, currentUser, profile, shopName, shopAddress, shopDescription);
                    return;
                }
            }

            // 3. Xử lý danh mục kinh doanh → JSON
            String preferredCategoriesJson = buildCategoryJson(req.getParameterValues("categoryIds"));

            // 4. Xử lý upload tài liệu (tối đa 10 file, 25MB mỗi file)
            String docPathsJson = uploadDocs(req, currentUser.getUserId());

            // 5. Tạo ShopProfile mới với PENDING
            ShopProfile newProfile = new ShopProfile();
            newProfile.setUserId(currentUser.getUserId());
            newProfile.setShopName(shopName.trim());
            newProfile.setShopDescription(shopDescription != null ? shopDescription.trim() : "");
            newProfile.setApprovalStatus("PENDING");
            newProfile.setDeliveryAddress(shopAddress.trim());
            newProfile.setRating(java.math.BigDecimal.ZERO);
            newProfile.setPreferredCategories(preferredCategoriesJson);
            newProfile.setDocPaths(docPathsJson);

            shopProfileDAO.save(newProfile);

            // Gửi email nhận đơn async
            new Thread(() -> {
                try {
                    emailService.sendShopApplicationReceivedEmail(currentUser.getEmail(), currentUser.getFullName(), newProfile.getShopName());
                } catch (Exception ex) {
                    getServletContext().log("Không thể gửi email nhận đơn đăng ký shop cho " + currentUser.getEmail(), ex);
                }
            }).start();

            // 6. PRG pattern
            SessionUtil.flashSuccess(req.getSession(),
                    "Đơn đăng ký mở shop của bạn đã được gửi thành công! Chúng tôi sẽ liên hệ trong 1-3 ngày làm việc.");
            resp.sendRedirect(req.getContextPath() + "/customer/shop-apply");

        } catch (Exception e) {
            getServletContext().log("ShopApplyServlet POST error: " + e.getMessage(), e);
            forwardWithError(req, resp, currentUser, e.getMessage());
        }
    }

    /**
     * Nộp lại đơn sau khi bị REJECTED — cập nhật profile cũ thay vì tạo mới.
     */
    private void resubmitApplication(HttpServletRequest req, HttpServletResponse resp,
            User currentUser, ShopProfile profile,
            String shopName, String shopAddress, String shopDescription)
            throws Exception, ServletException, IOException {
        String preferredCategoriesJson = buildCategoryJson(req.getParameterValues("categoryIds"));
        String docPathsJson = uploadDocs(req, currentUser.getUserId());

        profile.setShopName(shopName.trim());
        profile.setShopDescription(shopDescription != null ? shopDescription.trim() : "");
        profile.setApprovalStatus("PENDING");
        profile.setRejectionReason(null);
        profile.setApprovedAt(null);
        profile.setDeliveryAddress(shopAddress.trim());
        profile.setPreferredCategories(preferredCategoriesJson);
        profile.setDocPaths(docPathsJson);

        shopProfileDAO.update(profile);

        // Gửi email nhận đơn async
        new Thread(() -> {
            try {
                emailService.sendShopApplicationReceivedEmail(currentUser.getEmail(), currentUser.getFullName(), profile.getShopName());
            } catch (Exception ex) {
                getServletContext().log("Không thể gửi email nhận đơn đăng ký shop nộp lại cho " + currentUser.getEmail(), ex);
            }
        }).start();

        SessionUtil.flashSuccess(req.getSession(),
                "Đơn đăng ký mở shop đã được nộp lại thành công! Chúng tôi sẽ xem xét trong 1-3 ngày làm việc.");
        resp.sendRedirect(req.getContextPath() + "/customer/shop-apply");
    }

    /** Serialize categoryIds[] thành JSON array "[1,3,5]" */
    private String buildCategoryJson(String[] catIds) {
        if (catIds == null || catIds.length == 0) return null;
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String id : catIds) {
            try {
                int catId = Integer.parseInt(id);
                if (!first) sb.append(",");
                sb.append(catId);
                first = false;
            } catch (NumberFormatException ignored) {}
        }
        sb.append("]");
        return sb.toString();
    }

    /** Xử lý upload tài liệu, trả về JSON array đường dẫn hoặc null */
    private String uploadDocs(HttpServletRequest req, int userId) throws Exception, IOException {
        String uploadDir = getServletContext().getRealPath("");
        List<String> docPathList = new ArrayList<>();
        int docCount = 0;

        for (Part part : req.getParts()) {
            if (!"shopDocs".equals(part.getName())) continue;
            if (part.getSize() == 0) continue;

            // Sử dụng ValidationUtil để check kích thước, extension
            String docError = com.fruitmkt.util.ValidationUtil.validateShopDoc(part.getSubmittedFileName(), part.getSize());
            if (docError != null) {
                throw new Exception(docError);
            }

            if (docCount >= AppConfig.MAX_SHOP_DOC_COUNT) {
                throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
            }

            String savedPath = FileUploadUtil.saveShopDoc(part, uploadDir, userId);
            if (savedPath != null) {
                docPathList.add("\"" + savedPath + "\"");
                docCount++;
            }
        }

        return docPathList.isEmpty() ? null : "[" + String.join(",", docPathList) + "]";
    }

    /** Forward về form với thông báo lỗi và load lại data */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
            User currentUser, String errorMsg) throws ServletException, IOException {
        try {
            List<ShopProfile> existingProfiles = shopProfileDAO.findByUserId(currentUser.getUserId());
            if (!existingProfiles.isEmpty()) {
                req.setAttribute("existingProfile", existingProfiles.get(0));
            }
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException ex) {
            req.setAttribute("categories", new ArrayList<>());
        }
        req.setAttribute("errorMsg", errorMsg);
        req.getRequestDispatcher("/WEB-INF/jsp/customer/shop-apply.jsp").forward(req, resp);
    }
}
