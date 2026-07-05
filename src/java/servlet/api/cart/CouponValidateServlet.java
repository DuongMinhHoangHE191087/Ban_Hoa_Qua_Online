package servlet.api.cart;
import dao.shop.PromotionDAO;
import service.shop.PromotionService;

import model.entity.Promotion;

import model.response.ApiResponse;
import util.LoggerUtil;
import util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * CouponValidateServlet — AJAX endpoint xác thực mã giảm giá.
 *
 * URL: /api/coupon/validate
 * GET : Validate mã giảm giá và trả về JSON kết quả
 *
 * Params:
 *   code    - Mã giảm giá
 *   subtotal - Tổng tiền sản phẩm (BigDecimal string)
 *   ownerId  - ID chủ shop (hoặc danh sách ID cách nhau bằng dấu phẩy)
 *   scope    - "SHOP" hoặc "SYSTEM"
 *
 * Response JSON:
 *   {"success": true, "data": {"discountAmount": 10000, "message": "..."}}
 *   {"success": false, "error": "..."}
 *
 * @author fruitmkt-team
 */
@WebServlet("/api/coupon/validate")
public class CouponValidateServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CouponValidateServlet.class.getName());

    private final PromotionService promotionService = new PromotionService();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String code     = req.getParameter("code");
        String subtotalStr = req.getParameter("subtotal");
        String ownerIdStr  = req.getParameter("ownerId");
        String scope    = req.getParameter("scope");

        if (code == null || code.trim().isEmpty()) {
            JsonUtil.writeJson(resp, ApiResponse.error("Vui lòng nhập mã giảm giá."));
            return;
        }

        BigDecimal subtotal;
        try {
            subtotal = new BigDecimal(subtotalStr != null ? subtotalStr.trim() : "0");
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(resp, ApiResponse.error("Tổng tiền không hợp lệ."));
            return;
        }

        try {
            Promotion promo = null;

            if ("SHOP".equalsIgnoreCase(scope)) {
                if (ownerIdStr == null || ownerIdStr.trim().isEmpty()) {
                    JsonUtil.writeJson(resp, ApiResponse.error("Thiếu thông tin shop."));
                    return;
                }
                String[] parts = ownerIdStr.split(",");
                for (String part : parts) {
                    try {
                        int ownerId = Integer.parseInt(part.trim());
                        if (ownerId > 0) {
                            Promotion candidate = promotionService.validateShopCoupon(code, ownerId, subtotal);
                            if (candidate != null) {
                                promo = candidate;
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                promo = promotionService.validateSystemCoupon(code, subtotal);
            }

            if (promo == null) {
                JsonUtil.writeJson(resp, ApiResponse.error(resolveFailureMessage(code, scope, ownerIdStr, subtotal)));
                return;
            }

            BigDecimal discountAmt = promotionService.calculateDiscount(promo, subtotal);
            String fmtAmt = discountAmt.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String displayAmt = formatVnd(discountAmt);

            java.util.Map<String, Object> data = java.util.Map.of(
                "discountAmount", Long.parseLong(fmtAmt),
                "promoId", promo.getPromoId(),
                "discountType", promo.getDiscountType(),
                "discountScope", promo.getDiscountScope(),
                "benefitTarget", promo.getBenefitTarget(),
                "canStack", promo.getCanStack(),
                "ownerId", promo.getCreatedBy(), // Return ownerId of coupon creator
                "message", "Áp dụng thành công! Giảm " + displayAmt
            );
            JsonUtil.writeJson(resp, ApiResponse.ok(data));

        } catch (Exception e) {
            util.ServletUtil.sendJsonInternalServerError(
                    req,
                    resp,
                    log,
                    "CouponValidateServlet#doGet",
                    "Lỗi hệ thống. Vui lòng thử lại.",
                    e);
        }
    }

    private String resolveFailureMessage(String code, String scope, String ownerIdStr, BigDecimal subtotal) throws SQLException {
        Promotion promo = promotionDAO.findAnyByCode(code != null ? code.trim().toUpperCase() : null);
        if (promo == null) {
            return "Mã voucher không tồn tại.";
        }

        if (promo.getIsDeleted()) {
            return "Mã voucher này đã bị xóa.";
        }
        if (!promo.getIsActive()) {
            return "Mã voucher này hiện đang tạm dừng.";
        }

        LocalDateTime now = LocalDateTime.now();
        if (promo.getValidFrom() != null && promo.getValidFrom().isAfter(now)) {
            return "Mã voucher chưa đến thời gian áp dụng.";
        }
        if (promo.getValidUntil() != null && promo.getValidUntil().isBefore(now)) {
            return "Mã voucher đã hết hạn.";
        }

        if (promo.getMaxUses() != null && promo.getMaxUses() > 0 && promo.getUsedCount() >= promo.getMaxUses()) {
            return "Mã voucher này đã hết lượt sử dụng.";
        }

        String benefitTarget = promo.getBenefitTarget() != null ? promo.getBenefitTarget().trim().toUpperCase() : "";

        if ("PRODUCT".equalsIgnoreCase(promo.getScope())) {
            return "Mã này là voucher theo sản phẩm và không thể áp dụng ở bước thanh toán.";
        }

        String requestedScope = scope != null ? scope.trim().toUpperCase() : "";
        String couponScope = promo.getDiscountScope() != null ? promo.getDiscountScope().trim().toUpperCase() : "";

        if (PromotionService.BENEFIT_TARGET_PAYMENT_METHOD.equalsIgnoreCase(benefitTarget)) {
            if ("SHOP".equals(requestedScope)) {
                return "Mã này là voucher phương thức thanh toán. Hãy áp dụng ở phần voucher thanh toán.";
            }
            if ("SYSTEM".equals(requestedScope)) {
                if (promo.getMinOrderValue() != null && subtotal.compareTo(promo.getMinOrderValue()) < 0) {
                    return "Mã voucher phương thức thanh toán này chỉ áp dụng cho đơn từ "
                            + formatVnd(promo.getMinOrderValue()) + " trở lên.";
                }
                return "Mã voucher phương thức thanh toán không hợp lệ, đã hết hạn hoặc chưa đủ điều kiện áp dụng.";
            }
        }

        if ("SHOP".equals(requestedScope)) {
            if ("ALL".equals(couponScope)) {
                return "Mã này là voucher sàn. Hãy áp dụng ở phần voucher sàn.";
            }
            if (ownerIdStr != null && !ownerIdMatches(ownerIdStr, promo.getCreatedBy())) {
                return "Mã voucher này thuộc shop khác và không áp dụng cho giỏ hàng hiện tại.";
            }
            if (promo.getMinOrderValue() != null && subtotal.compareTo(promo.getMinOrderValue()) < 0) {
                return "Mã voucher này chỉ áp dụng cho đơn từ " + formatVnd(promo.getMinOrderValue()) + " trở lên.";
            }
            return "Mã voucher shop không hợp lệ, chưa đạt giá trị đơn tối thiểu, hoặc không thuộc shop nào trong giỏ hàng.";
        }

        if ("SYSTEM".equals(requestedScope)) {
            if ("SHOP".equals(couponScope)) {
                return "Mã này là voucher shop. Hãy áp dụng ở phần voucher shop của đúng shop.";
            }
            if (promo.getMinOrderValue() != null && subtotal.compareTo(promo.getMinOrderValue()) < 0) {
                return "Mã voucher này chỉ áp dụng cho đơn từ " + formatVnd(promo.getMinOrderValue()) + " trở lên.";
            }
            return "Mã voucher sàn không hợp lệ, đã hết hạn hoặc chưa đủ điều kiện áp dụng.";
        }

        return "Mã không hợp lệ, đã hết hạn, hoặc không đủ điều kiện đơn tối thiểu.";
    }

    private boolean ownerIdMatches(String ownerIdStr, int createdBy) {
        if (ownerIdStr == null || ownerIdStr.trim().isEmpty()) {
            return false;
        }
        Set<Integer> ownerIds = new HashSet<>();
        for (String part : ownerIdStr.split(",")) {
            try {
                int ownerId = Integer.parseInt(part.trim());
                if (ownerId > 0) {
                    ownerIds.add(ownerId);
                }
            } catch (NumberFormatException ignored) {
                // ignore invalid owner ids
            }
        }
        return ownerIds.contains(createdBy);
    }

    /** Format số tiền VNĐ dạng 10.000 đ */
    private String formatVnd(BigDecimal amount) {
        long val = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        return String.format("%,d đ", val).replace(',', '.');
    }
}
