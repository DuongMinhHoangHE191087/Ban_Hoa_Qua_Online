package servlet.api.cart;
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
                JsonUtil.writeJson(resp, ApiResponse.error("Mã không hợp lệ, đã hết hạn, hoặc không đủ điều kiện đơn tối thiểu."));
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

    /** Format số tiền VNĐ dạng 10.000 đ */
    private String formatVnd(BigDecimal amount) {
        long val = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        return String.format("%,d đ", val).replace(',', '.');
    }
}
