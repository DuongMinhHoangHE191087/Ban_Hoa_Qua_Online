package servlet.shop.promotion;
import service.shop.PromotionService;
import model.dto.common.PagedResultDTO;

import config.AppConfig;
import model.entity.Promotion;
import model.entity.auth.User;
import service.catalog.ProductService;

import util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * PromotionServlet - controller cho trang quản lý khuyến mãi.
 *
 * Supports both:
 * - /shop/promotions  -> shop-owned promotions (discount_scope = SHOP)
 * - /admin/promotions -> global promotions (discount_scope = ALL)
 */
@WebServlet(name = "PromotionServlet", urlPatterns = {"/shop/promotions", "/admin/promotions"})
public class PromotionServlet extends HttpServlet {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final PromotionService promotionService = new PromotionService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        PromotionMode mode = resolveMode(req, currentUser);
        if (mode == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        try {
            populatePageContext(req, currentUser, mode);
            req.getRequestDispatcher("/WEB-INF/jsp/shop/promotion.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Không tải được trang khuyến mãi.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req.getSession());
        PromotionMode mode = resolveMode(req, currentUser);
        if (mode == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thao tác.");
            return;
        }

        String action = normalize(req.getParameter("action"));
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "SAVE":
                    createPromotion(req, currentUser, mode);
                    SessionUtil.flashSuccess(req.getSession(), "Đã tạo khuyến mãi thành công.");
                    break;
                case "UPDATE":
                    updatePromotion(req, currentUser, mode);
                    SessionUtil.flashSuccess(req.getSession(), "Đã cập nhật khuyến mãi thành công.");
                    break;
                case "TOGGLE":
                    togglePromotion(req, currentUser, mode);
                    SessionUtil.flashSuccess(req.getSession(), "Đã cập nhật trạng thái khuyến mãi.");
                    break;
                case "DELETE":
                    deletePromotion(req, currentUser, mode);
                    SessionUtil.flashSuccess(req.getSession(), "Đã xóa mềm khuyến mãi.");
                    break;
                default:
                    throw new IllegalArgumentException("Hành động không hợp lệ.");
            }
        } catch (Exception e) {
            SessionUtil.flashError(req.getSession(), util.ErrorMessageUtil.getUserMessage(e));
        }

        resp.sendRedirect(req.getContextPath() + getBasePath(mode));
    }

    private void populatePageContext(HttpServletRequest req, User currentUser, PromotionMode mode)
            throws SQLException {
        req.setAttribute("promotionMode", mode.name());
        req.setAttribute("promotionBasePath", getBasePath(mode));
        req.setAttribute("promotionTitle", mode == PromotionMode.SHOP
                ? "Quản Lý Voucher Shop"
                : "Quản Lý Voucher Sàn");
        req.setAttribute("promotionDescription", mode == PromotionMode.SHOP
                ? "Tạo, sửa, bật/tắt và xóa mềm voucher shop của riêng cửa hàng."
                : "Tạo, sửa, bật/tắt và xóa mềm voucher sàn cho toàn hệ thống.");
        req.setAttribute("promotionBadge", mode == PromotionMode.SHOP ? "VOUCHER SHOP" : "VOUCHER SÀN");
        req.setAttribute("promotionFixedScope", mode == PromotionMode.SHOP ? "SHOP" : "ALL");
        int page = util.PaginationUtil.parsePage(req.getParameter("page"));
        int pageSize = mode == PromotionMode.SHOP ? AppConfig.DEFAULT_PAGE_SIZE : AppConfig.PAGE_SIZE_ADMIN;
        PagedResultDTO pagedResult = mode == PromotionMode.SHOP
                ? promotionService.getShopPromos(currentUser.getUserId(), page, pageSize)
                : promotionService.getGlobalPromotions(page, pageSize);

        req.setAttribute("promotions", pagedResult.getItems());
        req.setAttribute("currentPage", pagedResult.getCurrentPage());
        req.setAttribute("totalPages", pagedResult.getTotalPages());
        req.setAttribute("totalItems", pagedResult.getTotalItems());

        req.setAttribute("products", mode == PromotionMode.SHOP
                ? productService.getProductsByOwner(currentUser.getUserId())
                : promotionService.getPromotionProductsForAdmin());

        String editIdParam = req.getParameter("editId");
        if (editIdParam != null && !editIdParam.trim().isEmpty()) {
            try {
                int editId = parseRequiredInt(editIdParam, "Mã khuyến mãi không hợp lệ.");
                Promotion editPromotion = loadPromotionForMode(editId, currentUser, mode);
                if (editPromotion != null) {
                    req.setAttribute("editPromotion", editPromotion);
                    req.setAttribute("editValidFrom", formatDateTime(editPromotion.getValidFrom()));
                    req.setAttribute("editValidUntil", formatDateTime(editPromotion.getValidUntil()));
                }
            } catch (IllegalArgumentException ex) {
                SessionUtil.flashError(req.getSession(), util.ErrorMessageUtil.getUserMessage(ex));
            }
        }
    }

    private void createPromotion(HttpServletRequest req, User currentUser, PromotionMode mode) throws SQLException {
        Promotion promo = buildPromotionFromRequest(req);
        if (mode == PromotionMode.SHOP) {
            // Task 5: owner_id is forced to authenticated user inside service — not trusted from request
            promotionService.createShopPromotion(promo, currentUser.getUserId());
        } else {
            // Task 6: re-check ADMIN role at service boundary before issuing a system-scope coupon
            if (!AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
                throw new SecurityException("Chỉ quản trị viên mới có thể tạo voucher sàn.");
            }
            promotionService.createGlobalPromotion(promo, currentUser.getUserId());
        }
    }

    private void updatePromotion(HttpServletRequest req, User currentUser, PromotionMode mode) throws SQLException {
        int promoId = parseRequiredInt(req.getParameter("promoId"), "Mã khuyến mãi không hợp lệ.");
        Promotion promo = loadPromotionForMode(promoId, currentUser, mode);
        if (promo == null) {
            throw new IllegalArgumentException("Khuyến mãi không tồn tại hoặc không thuộc ngữ cảnh hiện tại.");
        }

        applyRequestToPromotion(req, promo);
        if (mode == PromotionMode.SHOP) {
            promotionService.updateShopPromotion(promo, currentUser.getUserId());
        } else {
            // Task 6: re-check ADMIN role before modifying a system-scope coupon
            if (!AppConfig.ROLE_ADMIN.equals(currentUser.getRole())) {
                throw new SecurityException("Chỉ quản trị viên mới có thể sửa voucher sàn.");
            }
            promotionService.updateGlobalPromotion(promo, currentUser.getUserId());
        }
    }

    private void togglePromotion(HttpServletRequest req, User currentUser, PromotionMode mode) throws SQLException {
        int promoId = parseRequiredInt(req.getParameter("promoId"), "Mã khuyến mãi không hợp lệ.");
        Promotion promo = loadPromotionForMode(promoId, currentUser, mode);
        if (promo == null) {
            throw new IllegalArgumentException("Khuyến mãi không tồn tại hoặc không thuộc ngữ cảnh hiện tại.");
        }

        promo.setIsActive(!promo.getIsActive());
        if (mode == PromotionMode.SHOP) {
            promotionService.updateShopPromotion(promo, currentUser.getUserId());
        } else {
            promotionService.updateGlobalPromotion(promo, currentUser.getUserId());
        }
    }

    private void deletePromotion(HttpServletRequest req, User currentUser, PromotionMode mode) throws SQLException {
        int promoId = parseRequiredInt(req.getParameter("promoId"), "Mã khuyến mãi không hợp lệ.");
        Promotion promo = loadPromotionForMode(promoId, currentUser, mode);
        if (promo == null) {
            throw new IllegalArgumentException("Khuyến mãi không tồn tại hoặc không thuộc ngữ cảnh hiện tại.");
        }

        promotionService.softDelete(promoId);
    }

    private Promotion loadPromotionForMode(int promoId, User currentUser, PromotionMode mode) throws SQLException {
        Promotion promo = promotionService.getPromotionById(promoId);
        if (promo == null) {
            return null;
        }
        if (mode == PromotionMode.SHOP) {
            return promo.getCreatedBy() == currentUser.getUserId() && "SHOP".equalsIgnoreCase(promo.getDiscountScope())
                    ? promo : null;
        }
        return "ALL".equalsIgnoreCase(promo.getDiscountScope()) ? promo : null;
    }

    private Promotion buildPromotionFromRequest(HttpServletRequest req) {
        Promotion promo = new Promotion();
        applyRequestToPromotion(req, promo);
        return promo;
    }

    private void applyRequestToPromotion(HttpServletRequest req, Promotion promo) {
        promo.setCode(normalize(req.getParameter("code")));
        promo.setDiscountType(normalize(req.getParameter("discountType")));
        promo.setDiscountScope(normalize(req.getParameter("discountScope")));
        promo.setScope(normalize(req.getParameter("scope")));
        promo.setBenefitTarget(normalize(req.getParameter("benefitTarget")));
        promo.setProductId(parseOptionalInt(req.getParameter("productId")));
        promo.setDiscountValue(parseBigDecimal(req.getParameter("discountValue"), "Giá trị giảm giá không hợp lệ."));
        promo.setDiscountMax(parseOptionalBigDecimal(req.getParameter("discountMax")));
        promo.setMinOrderValue(parseOptionalBigDecimal(req.getParameter("minOrderValue")));
        promo.setMaxUses(parseOptionalInt(req.getParameter("maxUses")));
        promo.setCanStack(req.getParameter("canStack") != null);
        promo.setIsActive(req.getParameter("isActive") != null);
        promo.setValidFrom(parseDateTime(req.getParameter("validFrom"), "Ngày bắt đầu không hợp lệ."));
        promo.setValidUntil(parseDateTime(req.getParameter("validUntil"), "Ngày kết thúc không hợp lệ."));
    }

    private PromotionMode resolveMode(HttpServletRequest req, User currentUser) {
        String servletPath = req.getServletPath();
        if ("/admin/promotions".equals(servletPath)) {
            return currentUser != null && AppConfig.ROLE_ADMIN.equals(currentUser.getRole())
                    ? PromotionMode.GLOBAL : null;
        }
        if ("/shop/promotions".equals(servletPath)) {
            return currentUser != null && AppConfig.ROLE_SHOP_OWNER.equals(currentUser.getRole())
                    ? PromotionMode.SHOP : null;
        }
        return null;
    }

    private String getBasePath(PromotionMode mode) {
        return mode == PromotionMode.GLOBAL ? "/admin/promotions" : "/shop/promotions";
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(INPUT_FORMATTER);
    }

    private int parseRequiredInt(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message, ex);
        }
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị số không hợp lệ.", ex);
        }
    }

    private BigDecimal parseBigDecimal(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message, ex);
        }
    }

    private BigDecimal parseOptionalBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị số không hợp lệ.", ex);
        }
    }

    private LocalDateTime parseDateTime(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(message, ex);
        }
    }

    private enum PromotionMode {
        SHOP,
        GLOBAL
    }
}
