package com.fruitmkt.servlet.api;

import com.fruitmkt.model.entity.Promotion;
import com.fruitmkt.service.PromotionService;

import com.fruitmkt.util.LoggerUtil;
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
 *   ownerId  - ID chủ shop (để validate mã shop)
 *   scope    - "SHOP" hoặc "SYSTEM"
 *
 * Response JSON:
 *   {"valid": true/false, "discountAmount": 10000, "message": "..."}
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
        String scope    = req.getParameter("scope"); // SHOP hoặc SYSTEM

        // Validate params cơ bản
        if (code == null || code.trim().isEmpty()) {
            resp.getWriter().write("{\"valid\":false,\"discountAmount\":0,\"message\":\"Vui lòng nhập mã giảm giá.\"}");
            return;
        }

        BigDecimal subtotal;
        try {
            subtotal = new BigDecimal(subtotalStr != null ? subtotalStr.trim() : "0");
        } catch (NumberFormatException e) {
            resp.getWriter().write("{\"valid\":false,\"discountAmount\":0,\"message\":\"Tổng tiền không hợp lệ.\"}");
            return;
        }

        try {
            Promotion promo = null;

            if ("SHOP".equalsIgnoreCase(scope)) {
                int ownerId;
                try { ownerId = Integer.parseInt(ownerIdStr); }
                catch (NumberFormatException e) {
                    resp.getWriter().write("{\"valid\":false,\"discountAmount\":0,\"message\":\"Thiếu thông tin shop.\"}");
                    return;
                }
                promo = promotionService.validateShopCoupon(code, ownerId, subtotal);
            } else {
                // SYSTEM hoặc không rõ
                promo = promotionService.validateSystemCoupon(code, subtotal);
            }

            if (promo == null) {
                resp.getWriter().write("{\"valid\":false,\"discountAmount\":0,"
                    + "\"message\":\"Mã không hợp lệ, đã hết hạn, hoặc không đủ điều kiện đơn tối thiểu.\"}");
                return;
            }

            BigDecimal discountAmt = promotionService.calculateDiscount(promo, subtotal);
            String fmtAmt = discountAmt.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
            String displayAmt = formatVnd(discountAmt);

            resp.getWriter().write("{\"valid\":true,\"discountAmount\":" + fmtAmt
                + ",\"promoId\":" + promo.getPromoId()
                + ",\"discountType\":\"" + promo.getDiscountType() + "\""
                + ",\"message\":\"Áp dụng thành công! Giảm " + displayAmt + "\"}");

        } catch (Exception e) {
            LoggerUtil.error(log, "Lỗi khi validate coupon code=" + req.getParameter("code"), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"valid\":false,\"discountAmount\":0,\"message\":\"Lỗi hệ thống. Vui lòng thử lại.\"}");
        }
    }

    /** Format số tiền VNĐ dạng 10.000 đ */
    private String formatVnd(BigDecimal amount) {
        long val = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        return String.format("%,d đ", val).replace(',', '.');
    }
}
