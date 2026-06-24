package servlet.shop.shop;

import config.AppConfig;
import dao.catalog.CategoryDAO;
import dao.shop.ShopProfileDAO;
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
import util.FileUploadUtil;
import util.LoggerUtil;
import util.SessionUtil;
import util.ShopDocDraftUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ShopStatusServlet — Hiển thị trạng thái shop và cho phép nộp lại hồ sơ bị từ chối.
 */
@WebServlet("/shop/status")
@MultipartConfig(
    maxFileSize = 26_214_400L,
    maxRequestSize = 264_241_152L,
    fileSizeThreshold = 1_048_576
)
public class ShopStatusServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShopStatusServlet.class.getName());

    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ShopService shopService = new ShopService();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = SessionUtil.getCurrentUser(session);

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        if (!AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        try {
            List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
            ShopProfile profile = profiles.isEmpty() ? null : profiles.get(0);
            req.setAttribute("profile", profile);

            if (profile != null && AppConfig.SHOP_REJECTED.equals(profile.getApprovalStatus())) {
                try {
                    req.setAttribute("categories", categoryDAO.findAllActive());
                } catch (SQLException ex) {
                    req.setAttribute("categories", new ArrayList<>());
                }
            }

            ShopDocDraftUtil.exposeDraftDocs(session, req, ShopDocDraftUtil.STATUS_SCOPE, "shopStatusDraftDocPaths");
            req.getRequestDispatcher("/WEB-INF/jsp/shop/status.jsp").forward(req, resp);
        } catch (SQLException e) {
            util.ServletUtil.sendPageInternalServerError(
                    req,
                    resp,
                    java.util.logging.Logger.getLogger(ShopStatusServlet.class.getName()),
                    "ShopStatusServlet#doGet",
                    "Lỗi hệ thống khi tải thông tin cửa hàng.",
                    e);
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

        if (!AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

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
            if (!AppConfig.SHOP_REJECTED.equals(profile.getApprovalStatus())) {
                resp.sendRedirect(req.getContextPath() + "/shop/status");
                return;
            }

            handleRejectedResubmit(req, resp, session, currentUser, profile);
        } catch (Exception e) {
            getServletContext().log("ShopStatusServlet POST error: " + e.getMessage(), e);
            forwardWithError(req, resp, session, currentUser, e.getMessage());
        }
    }

    private void handleRejectedResubmit(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, User currentUser, ShopProfile profile) throws Exception {
        String shopName = sanitizeInput(req.getParameter("storeName"));
        String address = sanitizeInput(req.getParameter("address"));
        String businessEmail = sanitizeInput(req.getParameter("businessEmail"));
        String description = sanitizeInput(req.getParameter("shopDescription"));

        shopName = ValidationUtil.requireValidShopName(shopName, "Tên cửa hàng");
        address = ValidationUtil.requireValidAddress(address, "Địa chỉ kinh doanh");
        businessEmail = ValidationUtil.requireValidEmail(businessEmail, "Email liên hệ kinh doanh");

        if (req.getParameter("agreeTerms") == null) {
            throw new Exception("Bạn phải đồng ý với điều khoản sử dụng trước khi gửi lại hồ sơ.");
        }

        String preferredCategoriesJson = buildCategoryJson(req.getParameterValues("categoryIds"));
        if (preferredCategoriesJson == null) {
            throw new Exception("Vui lòng chọn ít nhất một danh mục sản phẩm.");
        }

        List<String> uploadedDraftPaths = ShopDocDraftUtil.uploadDraftDocs(
                req, "businessDocs", ShopDocDraftUtil.STATUS_SCOPE);
        if (!uploadedDraftPaths.isEmpty()) {
            ShopDocDraftUtil.replaceDraftDocs(session, ShopDocDraftUtil.STATUS_SCOPE, uploadedDraftPaths);
        }

        List<String> draftPaths = ShopDocDraftUtil.getDraftPaths(session, ShopDocDraftUtil.STATUS_SCOPE);
        if (draftPaths.isEmpty()) {
            throw new Exception("Vui lòng tải lên ít nhất một tài liệu xác minh.");
        }

        List<String> promotedDocPaths = new ArrayList<>();
        try {
            promotedDocPaths = ShopDocDraftUtil.promoteDraftDocs(session, ShopDocDraftUtil.STATUS_SCOPE, currentUser.getUserId());
            String docPathsJson = ShopDocDraftUtil.toJsonArray(promotedDocPaths);

            profile.setShopName(shopName);
            profile.setBusinessEmail(businessEmail);
            profile.setDeliveryAddress(address);
            profile.setShopDescription(description != null ? description : "");
            profile.setPreferredCategories(preferredCategoriesJson);
            profile.setDocPaths(docPathsJson);
            profile.setApprovalStatus(AppConfig.SHOP_PENDING);
            profile.setRejectionReason(null);
            profile.setApprovedAt(null);

            shopService.resubmitRejectedShopApplication(profile, null);
            ShopDocDraftUtil.clearDraftDocs(session, ShopDocDraftUtil.STATUS_SCOPE);
            sendApplicationReceivedEmail(currentUser, shopName);
            SessionUtil.flashSuccess(session, "Nộp lại hồ sơ thành công! Đang chờ Admin phê duyệt lại.");
            resp.sendRedirect(req.getContextPath() + "/shop/status");
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
                getServletContext().log("Không thể gửi email nhận đơn đăng ký shop nộp lại cho " + email, ex);
            }
        }).start();
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
                List<ShopProfile> profiles = shopProfileDAO.findByUserId(currentUser.getUserId());
                req.setAttribute("profile", profiles.isEmpty() ? null : profiles.get(0));
            }
            req.setAttribute("categories", categoryDAO.findAllActive());
        } catch (SQLException ex) {
            req.setAttribute("categories", new ArrayList<>());
        }
        ShopDocDraftUtil.exposeDraftDocs(session, req, ShopDocDraftUtil.STATUS_SCOPE, "shopStatusDraftDocPaths");
        req.setAttribute("errorMsg", errorMsg);
        req.getRequestDispatcher("/WEB-INF/jsp/shop/status.jsp").forward(req, resp);
    }
}
