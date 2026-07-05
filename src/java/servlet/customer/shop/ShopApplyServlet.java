package servlet.customer.shop;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.entity.auth.User;
import model.entity.catalog.Category;
import model.entity.shop.ShopProfile;
import service.shop.ShopService;
import service.system.EmailService;
import util.ActorAccessPolicy;
import util.FileUploadUtil;
import util.LoggerUtil;
import util.SessionUtil;
import util.ShopStatusRedirectUtil;
import util.ShopDocDraftUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ShopApplyServlet — Nộp đơn đăng ký mở shop và nộp lại hồ sơ bị từ chối.
 */
@WebServlet("/customer/shop-apply")
@MultipartConfig(
    maxFileSize = 26_214_400L,
    maxRequestSize = 264_241_152L,
    fileSizeThreshold = 1_048_576
)
public class ShopApplyServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopApplyServlet.class.getName());

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ShopService shopService = new ShopService();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = SessionUtil.getCurrentUser(session);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        if (!ActorAccessPolicy.canAccessCustomerArea(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        try {
            if (!"true".equals(req.getParameter("edit"))
                    && ShopStatusRedirectUtil.redirectToShopStatusIfProfileExists(req, resp, currentUser, session)) {
                return;
            }
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException e) {
            getServletContext().log("ShopApplyServlet GET error", e);
            req.setAttribute("categories", new ArrayList<>());
        }

        ShopDocDraftUtil.exposeDraftDocs(session, req, ShopDocDraftUtil.APPLY_SCOPE, "shopApplyDraftDocPaths");
        req.getRequestDispatcher("/WEB-INF/jsp/customer/shop-apply.jsp").forward(req, resp);
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
        if (!ActorAccessPolicy.canAccessCustomerArea(currentUser)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        String sessionCsrf = (String) req.getSession().getAttribute(AppConfig.SESSION_CSRF_TOKEN);
        String reqCsrf = req.getParameter("_csrf");
        if (sessionCsrf == null || !sessionCsrf.equals(reqCsrf)) {
            forwardWithError(req, resp, session, currentUser, "CSRF token không hợp lệ. Vui lòng thử lại.");
            return;
        }

        try {
            ShopProfile existing = shopService.getShopByUserId(currentUser.getUserId());
            if (existing != null) {
                String status = existing.getApprovalStatus();
                if (AppConfig.SHOP_PENDING.equals(status)) {
                    throw new Exception("Bạn đã có đơn đăng ký shop đang chờ duyệt. Vui lòng chờ Admin xem xét.");
                }
                if (AppConfig.SHOP_APPROVED.equals(status)) {
                    throw new Exception("Tài khoản của bạn đã được phê duyệt là Shop Owner rồi.");
                }
                if (AppConfig.SHOP_REJECTED.equals(status)) {
                    handleRejectedResubmit(req, resp, session, currentUser, existing);
                    return;
                }
            }

            handleNewApplication(req, resp, session, currentUser);
        } catch (Exception e) {
            getServletContext().log("ShopApplyServlet POST error: " + util.ErrorMessageUtil.getSafeLogMessage(e), e);
            forwardWithError(req, resp, session, currentUser,
                    util.ErrorMessageUtil.logAndGetUserMessage(log, "ShopApplyServlet#doPost", e));
        }
    }

    private void handleNewApplication(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User currentUser) throws Exception {
        String shopName = sanitizeInput(req.getParameter("shopName"));
        String businessEmail = sanitizeInput(req.getParameter("businessEmail"));
        String shopAddress = sanitizeInput(req.getParameter("shopAddress"));
        String shopDescription = sanitizeInput(req.getParameter("shopDescription"));

        shopName = ValidationUtil.requireValidShopName(shopName, "Tên cửa hàng");
        businessEmail = ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");
        shopAddress = ValidationUtil.requireValidAddress(shopAddress, "Địa chỉ kinh doanh");

        String preferredCategoriesJson = buildCategoryJson(req.getParameterValues("categoryIds"));
        if (preferredCategoriesJson == null) {
            throw new Exception("Vui lòng chọn ít nhất một danh mục sản phẩm.");
        }

        if (req.getParameter("agreeTerms") == null) {
            throw new Exception("Bạn phải đồng ý với điều khoản sử dụng trước khi gửi đơn.");
        }

        String phoneToUpdate = resolvePhoneToUpdate(currentUser, req);

        List<String> uploadedDraftPaths = ShopDocDraftUtil.uploadDraftDocs(
                req, "businessDocs", ShopDocDraftUtil.APPLY_SCOPE);
        if (!uploadedDraftPaths.isEmpty()) {
            ShopDocDraftUtil.replaceDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE, uploadedDraftPaths);
        }

        List<String> draftPaths = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.APPLY_SCOPE);
        if (draftPaths.isEmpty()) {
            throw new Exception("Vui lòng tải lên ít nhất một tài liệu xác minh.");
        }

        List<String> promotedDocPaths = new ArrayList<>();
        try {
            promotedDocPaths = ShopDocDraftUtil.promoteDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE, currentUser.getUserId());
            String docPathsJson = ShopDocDraftUtil.toJsonArray(promotedDocPaths);

            ShopProfile profile = new ShopProfile();
            profile.setUserId(currentUser.getUserId());
            profile.setShopName(shopName);
            profile.setBusinessEmail(businessEmail);
            profile.setShopDescription(shopDescription != null ? shopDescription : "");
            profile.setApprovalStatus(AppConfig.SHOP_PENDING);
            profile.setDeliveryAddress(shopAddress);
            profile.setRating(BigDecimal.ZERO);
            profile.setPreferredCategories(preferredCategoriesJson);
            profile.setDocPaths(docPathsJson);

            shopService.submitShopApplication(profile, phoneToUpdate);
            if (phoneToUpdate != null) {
                currentUser.setPhone(phoneToUpdate);
                session.setAttribute(AppConfig.SESSION_USER, currentUser);
            }

            ShopDocDraftUtil.clearDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE);
            sendApplicationReceivedEmail(currentUser, shopName);
            SessionUtil.flashSuccess(session,
                    "Đơn đăng ký mở shop của bạn đã được gửi thành công! Chúng tôi sẽ liên hệ trong 1-3 ngày làm việc.");
            resp.sendRedirect(req.getContextPath() + "/customer/shop-apply");
        } catch (Exception e) {
            rollbackPromotedDocs(promotedDocPaths);
            throw e;
        }
    }

    private void handleRejectedResubmit(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User currentUser, ShopProfile profile) throws Exception {
        String shopName = sanitizeInput(req.getParameter("shopName"));
        String businessEmail = sanitizeInput(req.getParameter("businessEmail"));
        String shopAddress = sanitizeInput(req.getParameter("shopAddress"));
        String shopDescription = sanitizeInput(req.getParameter("shopDescription"));

        shopName = ValidationUtil.requireValidShopName(shopName, "Tên cửa hàng");
        businessEmail = ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");
        shopAddress = ValidationUtil.requireValidAddress(shopAddress, "Địa chỉ kinh doanh");

        String preferredCategoriesJson = buildCategoryJson(req.getParameterValues("categoryIds"));
        if (preferredCategoriesJson == null) {
            throw new Exception("Vui lòng chọn ít nhất một danh mục sản phẩm.");
        }

        if (req.getParameter("agreeTerms") == null) {
            throw new Exception("Bạn phải đồng ý với điều khoản sử dụng trước khi gửi lại hồ sơ.");
        }

        List<String> uploadedDraftPaths = ShopDocDraftUtil.uploadDraftDocs(
                req, "businessDocs", ShopDocDraftUtil.APPLY_SCOPE);
        if (!uploadedDraftPaths.isEmpty()) {
            ShopDocDraftUtil.replaceDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE, uploadedDraftPaths);
        }

        List<String> draftPaths = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.APPLY_SCOPE);
        if (draftPaths.isEmpty()) {
            throw new Exception("Vui lòng tải lên ít nhất một tài liệu xác minh.");
        }

        List<String> promotedDocPaths = new ArrayList<>();
        try {
            promotedDocPaths = ShopDocDraftUtil.promoteDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE, currentUser.getUserId());
            String docPathsJson = ShopDocDraftUtil.toJsonArray(promotedDocPaths);

            profile.setShopName(shopName);
            profile.setBusinessEmail(businessEmail);
            profile.setShopDescription(shopDescription != null ? shopDescription : "");
            profile.setApprovalStatus(AppConfig.SHOP_PENDING);
            profile.setRejectionReason(null);
            profile.setApprovedAt(null);
            profile.setDeliveryAddress(shopAddress);
            profile.setPreferredCategories(preferredCategoriesJson);
            profile.setDocPaths(docPathsJson);

            shopService.resubmitRejectedShopApplication(profile, null);
            ShopDocDraftUtil.clearDraftDocs(session, ShopDocDraftUtil.APPLY_SCOPE);
            sendApplicationReceivedEmail(currentUser, shopName);
            SessionUtil.flashSuccess(session,
                    "Đơn đăng ký mở shop đã được nộp lại thành công! Chúng tôi sẽ xem xét trong 1-3 ngày làm việc.");
            resp.sendRedirect(req.getContextPath() + "/customer/shop-apply");
        } catch (Exception e) {
            rollbackPromotedDocs(promotedDocPaths);
            throw e;
        }
    }

    private void sendApplicationReceivedEmail(User currentUser, String shopName) {
        final String email = currentUser.getEmail();
        final String fullName = currentUser.getFullName();
        final String finalShopName = shopName;
        new Thread(() -> {
            try {
                emailService.sendShopApplicationReceivedEmail(email, fullName, finalShopName);
            } catch (Exception ex) {
                getServletContext().log("Không thể gửi email nhận đơn đăng ký shop cho " + email, ex);
            }
        }).start();
    }

    private String resolvePhoneToUpdate(User currentUser, HttpServletRequest req) throws Exception {
        if (currentUser.getPhone() != null && !currentUser.getPhone().trim().isEmpty()) {
            return null;
        }
        return ValidationUtil.requireValidPhone(sanitizeInput(req.getParameter("userPhone")), "Số điện thoại");
    }

    private String buildCategoryJson(String[] catIds) {
        if (catIds == null || catIds.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String id : catIds) {
            try {
                int catId = Integer.parseInt(id);
                if (!first) {
                    sb.append(",");
                }
                sb.append(catId);
                first = false;
            } catch (NumberFormatException e) {
                LoggerUtil.warn(log, "ID danh mục không hợp lệ: " + id, e);
            }
        }
        if (first) {
            return null;
        }
        sb.append("]");
        return sb.toString();
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "").trim();
    }

    private void rollbackPromotedDocs(List<String> promotedDocPaths) {
        if (promotedDocPaths == null || promotedDocPaths.isEmpty()) {
            return;
        }
        for (String path : promotedDocPaths) {
            FileUploadUtil.delete(path);
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User currentUser, String errorMsg) throws ServletException, IOException {
        try {
            if (currentUser != null) {
                ShopProfile existingProfile = shopService.getShopByUserId(currentUser.getUserId());
                if (existingProfile != null) {
                    req.setAttribute("existingProfile", existingProfile);
                    req.setAttribute("existingProfileDocPaths", parseJsonArray(existingProfile.getDocPaths()));
                }
            }
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException ex) {
            req.setAttribute("categories", new ArrayList<>());
        }
        ShopDocDraftUtil.exposeDraftDocs(session, req, ShopDocDraftUtil.APPLY_SCOPE, "shopApplyDraftDocPaths");
        req.setAttribute("errorMsg", errorMsg);
        req.getRequestDispatcher("/WEB-INF/jsp/customer/shop-apply.jsp").forward(req, resp);
    }

    private List<String> parseJsonArray(String json) {
        List<String> values = new ArrayList<>();
        if (json == null) {
            return values;
        }

        String trimmed = json.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) {
            return values;
        }

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        if (trimmed.isEmpty()) {
            return values;
        }

        for (String token : trimmed.split(",")) {
            String item = token.trim();
            if (item.startsWith("\"") && item.endsWith("\"") && item.length() >= 2) {
                item = item.substring(1, item.length() - 1);
            }
            if (item.startsWith("'") && item.endsWith("'") && item.length() >= 2) {
                item = item.substring(1, item.length() - 1);
            }
            if (!item.isEmpty() && !"null".equalsIgnoreCase(item)) {
                values.add(item);
            }
        }

        return values;
    }
}
