package com.fruitmkt.servlet.shop;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.model.entity.User;
import com.fruitmkt.model.entity.ShopProfile;
import com.fruitmkt.dao.ShopProfileDAO;
import com.fruitmkt.dao.CategoryDAO;
import com.fruitmkt.model.entity.Category;
import com.fruitmkt.util.SessionUtil;

import com.fruitmkt.util.LoggerUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ShopStatusServlet — Hiển thị trạng thái duyệt của shop đối tác, và cho phép nộp lại nếu bị từ chối.
 *
 * URL: /shop/status
 *
 * @author fruitmkt-team
 */
@WebServlet("/shop/status")
@MultipartConfig(
    maxFileSize    = 26_214_400L,   // 25MB mỗi file
    maxRequestSize = 264_241_152L,  // ~252MB tổng
    fileSizeThreshold = 1_048_576   // 1MB buffer
)
public class ShopStatusServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopStatusServlet.class.getName());

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        if (!"SHOP_OWNER".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        try {
            List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
            ShopProfile profile = null;
            if (!profiles.isEmpty()) {
                profile = profiles.get(0);
            }

            req.setAttribute("profile", profile);
            
            // Load active categories for the resubmit form if profile is rejected
            if (profile != null && "REJECTED".equals(profile.getApprovalStatus())) {
                try {
                    List<Category> categories = categoryDAO.findAllActive();
                    req.setAttribute("categories", categories);
                } catch (SQLException ex) {
                    req.setAttribute("categories", new ArrayList<>());
                }
            }

            req.getRequestDispatcher("/WEB-INF/jsp/shop/status.jsp").forward(req, resp);

        } catch (SQLException e) {
            getServletContext().log("ShopStatusServlet doGet: Không tải được shop profile", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi tải thông tin cửa hàng.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        if (!"SHOP_OWNER".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        // Check CSRF
        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            SessionUtil.flashError(req.getSession(), "CSRF token không hợp lệ. Vui lòng thử lại.");
            resp.sendRedirect(req.getContextPath() + "/shop/status");
            return;
        }

        try {
            List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
            if (profiles.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            ShopProfile profile = profiles.get(0);
            if (!"REJECTED".equals(profile.getApprovalStatus())) {
                resp.sendRedirect(req.getContextPath() + "/shop/status");
                return;
            }

            String shopName = req.getParameter("storeName");
            String address = req.getParameter("address");
            String businessEmail = req.getParameter("businessEmail");
            String description = req.getParameter("shopDescription");

            shopName = sanitizeInput(shopName);
            address = sanitizeInput(address);
            businessEmail = sanitizeInput(businessEmail);
            description = sanitizeInput(description);

            shopName = com.fruitmkt.util.ValidationUtil.requireValidShopName(shopName, "Tên cửa hàng");
            address = com.fruitmkt.util.ValidationUtil.requireValidAddress(address, "Địa chỉ kinh doanh");
            businessEmail = com.fruitmkt.util.ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");

            // Check business email uniqueness, excluding this shop's own current email
            if (!businessEmail.equalsIgnoreCase(profile.getBusinessEmail())) {
                if (shopProfileDAO.isBusinessEmailExists(businessEmail)) {
                    throw new Exception("Mỗi doanh nghiệp chỉ được đăng ký tối đa 1 gian hàng! Email kinh doanh này đã được sử dụng.");
                }
            }

            // Process categories selection
            String preferredCategoriesJson = null;
            String[] catIds = req.getParameterValues("categoryIds");
            if (catIds != null && catIds.length > 0) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < catIds.length; i++) {
                    try {
                        int catId = Integer.parseInt(catIds[i]);
                        if (i > 0) sb.append(",");
                        sb.append(catId);
                    } catch (NumberFormatException e) {
                        LoggerUtil.warn(log, "ID danh mục không hợp lệ: " + catIds[i], e);
                    }
                }
                sb.append("]");
                preferredCategoriesJson = sb.toString();
            }

            // Documents upload
            String uploadDir = getServletContext().getRealPath("");
            List<String> docPathList = new ArrayList<>();
            int docCount = 0;
            
            java.util.Collection<jakarta.servlet.http.Part> parts = req.getParts();
            for (jakarta.servlet.http.Part part : parts) {
                if (!"businessDocs".equals(part.getName())) continue;
                if (part.getSize() == 0) continue;

                String docError = com.fruitmkt.util.ValidationUtil.validateShopDoc(part.getSubmittedFileName(), part.getSize());
                if (docError != null) {
                    throw new Exception(docError);
                }
                docCount++;
            }

            if (docCount > AppConfig.MAX_SHOP_DOC_COUNT) {
                throw new Exception("Chỉ được upload tối đa " + AppConfig.MAX_SHOP_DOC_COUNT + " tài liệu.");
            }

            for (jakarta.servlet.http.Part part : parts) {
                if (!"businessDocs".equals(part.getName())) continue;
                if (part.getSize() == 0) continue;

                String savedPath = com.fruitmkt.util.FileUploadUtil.saveShopDoc(part, uploadDir, currentUser.getUserId());
                if (savedPath != null) {
                    docPathList.add("\"" + savedPath + "\"");
                }
            }

            // If no files uploaded, retain previous ones
            String docPathsJson = docPathList.isEmpty() ? profile.getDocPaths() : "[" + String.join(",", docPathList) + "]";

            // Update shop profile to PENDING
            profile.setShopName(shopName);
            profile.setDeliveryAddress(address);
            profile.setBusinessEmail(businessEmail);
            profile.setShopDescription(description != null ? description : "");
            profile.setPreferredCategories(preferredCategoriesJson);
            profile.setDocPaths(docPathsJson);
            profile.setApprovalStatus("PENDING");
            profile.setRejectionReason(null);
            profile.setApprovedAt(null);

            shopProfileDAO.update(profile);

            // Send confirmation email async
            final String finalStoreName = shopName;
            new Thread(() -> {
                try {
                    com.fruitmkt.service.EmailService emailService = new com.fruitmkt.service.EmailService();
                    emailService.sendShopApplicationReceivedEmail(currentUser.getEmail(), currentUser.getFullName(), finalStoreName);
                } catch (Exception ex) {
                    getServletContext().log("Không thể gửi email nhận đơn đăng ký shop nộp lại cho " + currentUser.getEmail(), ex);
                }
            }).start();

            SessionUtil.flashSuccess(req.getSession(), "Nộp lại hồ sơ thành công! Đang chờ Admin phê duyệt lại.");
            resp.sendRedirect(req.getContextPath() + "/shop/status");

        } catch (Exception e) {
            getServletContext().log("ShopStatusServlet POST error: " + e.getMessage(), e);
            req.setAttribute("errorMsg", e.getMessage());
            try {
                List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
                req.setAttribute("profile", profiles.isEmpty() ? null : profiles.get(0));
                req.setAttribute("categories", categoryDAO.findAllActive());
            } catch (SQLException ex) {
                LoggerUtil.warn(log, "Không thể tải lại thông tin shop khi xử lý lỗi", ex);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/shop/status.jsp").forward(req, resp);
        }
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
