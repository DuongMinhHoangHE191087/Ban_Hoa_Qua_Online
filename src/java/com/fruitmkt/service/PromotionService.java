package com.fruitmkt.service;

import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.model.entity.Promotion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * PromotionService — Business logic cho mã giảm giá.
 *
 * Công thức tính tiền chuẩn TMĐT:
 *   shop_discount   = min(subtotal × shopRate, shopMax) [PERCENT]
 *                   | min(shopFixed, subtotal)           [FIXED]
 *   system_discount = min((subtotal - shopDisc) × sysRate, sysMax) [PERCENT]
 *                   | min(sysFixed, subtotal - shopDisc)            [FIXED]
 *   final_amount    = subtotal - shop_discount - system_discount + delivery_fee
 *
 * @author fruitmkt-team
 */
public class PromotionService {

    private final PromotionDAO promotionDAO = new PromotionDAO();

    // ─── Mock coupon data (dùng khi chưa có dữ liệu thật trong DB) ─────────
    // Để tắt mock: set ENABLE_MOCK_COUPONS = false
    private static final boolean ENABLE_MOCK_COUPONS = true;

    /**
     * Validate mã giảm giá SHOP — trả về Promotion nếu hợp lệ, null nếu không.
     *
     * @param code       mã giảm giá do khách nhập
     * @param ownerId    ID chủ shop (để đảm bảo mã thuộc đúng shop)
     * @param subtotal   tổng tiền sản phẩm (trước phí giao)
     */
    public Promotion validateShopCoupon(String code, int ownerId,
                                        BigDecimal subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) return null;
        code = code.trim().toUpperCase();

        // Mock coupon check (DEV mode)
        if (ENABLE_MOCK_COUPONS) {
            Promotion mock = getMockShopCoupon(code, subtotal);
            if (mock != null) return mock;
        }

        Promotion promo = promotionDAO.findValidShopCoupon(code, ownerId, subtotal);
        if (promo == null) return null;

        // Kiểm tra lượt dùng
        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            return null; // Mã đã hết lượt dùng
        }
        return promo;
    }

    /**
     * Validate mã giảm giá SÀN — trả về Promotion nếu hợp lệ, null nếu không.
     *
     * @param code     mã giảm giá do khách nhập
     * @param subtotal tổng tiền sản phẩm (trước khi áp mã shop)
     */
    public Promotion validateSystemCoupon(String code, BigDecimal subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) return null;
        code = code.trim().toUpperCase();

        // Mock coupon check (DEV mode)
        if (ENABLE_MOCK_COUPONS) {
            Promotion mock = getMockSystemCoupon(code, subtotal);
            if (mock != null) return mock;
        }

        Promotion promo = promotionDAO.findValidSystemCoupon(code, subtotal);
        if (promo == null) return null;

        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            return null;
        }
        return promo;
    }

    /**
     * Tính số tiền giảm thực tế từ một mã giảm giá.
     *
     * @param promo   Promotion đã validate
     * @param base    Số tiền áp dụng giảm (subtotal hoặc subtotal sau shop)
     * @return        Số tiền giảm (không âm, không vượt quá base)
     */
    public BigDecimal calculateDiscount(Promotion promo, BigDecimal base) {
        if (promo == null || base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("PERCENT".equals(promo.getDiscountType())) {
            // Phần trăm: discount = base × (discountValue / 100), capped by discountMax
            discount = base.multiply(promo.getDiscountValue())
                          .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            if (promo.getDiscountMax() != null
                    && promo.getDiscountMax().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(promo.getDiscountMax());
            }
        } else {
            // FIXED: trừ cố định, tối đa bằng base
            discount = promo.getDiscountValue().min(base);
        }
        return discount.max(BigDecimal.ZERO);
    }

    /**
     * Tính toán đầy đủ discount khi áp cả shop + sàn.
     * Áp shop trước, sàn sau trên phần còn lại.
     *
     * @return mảng [shopDiscount, systemDiscount, totalDiscount, finalAmount]
     */
    public BigDecimal[] calculateAllDiscounts(Promotion shopPromo, Promotion systemPromo,
                                              BigDecimal subtotal, BigDecimal deliveryFee) {
        BigDecimal shopDiscount   = calculateDiscount(shopPromo, subtotal);
        BigDecimal afterShop      = subtotal.subtract(shopDiscount).max(BigDecimal.ZERO);
        BigDecimal systemDiscount = calculateDiscount(systemPromo, afterShop);
        BigDecimal totalDiscount  = shopDiscount.add(systemDiscount);
        BigDecimal finalAmount    = subtotal.subtract(totalDiscount)
                                           .add(deliveryFee)
                                           .max(BigDecimal.ZERO);
        return new BigDecimal[]{shopDiscount, systemDiscount, totalDiscount, finalAmount};
    }

    /**
     * Tạo mã giảm giá mới.
     */
    public int createPromotion(Promotion promo) throws SQLException {
        if (promo.getCode() == null || promo.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống.");
        }
        if (promo.getDiscountValue() == null
                || promo.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0.");
        }
        if (promo.getValidFrom() == null || promo.getValidUntil() == null
                || promo.getValidFrom().isAfter(promo.getValidUntil())) {
            throw new IllegalArgumentException("Thời gian hiệu lực không hợp lệ.");
        }
        return promotionDAO.save(promo);
    }

    /**
     * Lấy danh sách mã giảm giá active của shop.
     */
    @SuppressWarnings("unchecked")
    public List<Promotion> getShopPromos(int ownerId) throws SQLException {
        return promotionDAO.findByOwner(ownerId);
    }

    /**
     * Hủy kích hoạt mã giảm giá.
     */
    public void deactivate(int promoId) throws SQLException {
        promotionDAO.deactivate(promoId);
    }

    /**
     * Xóa mềm mã giảm giá.
     */
    public void softDelete(int promoId) throws SQLException {
        promotionDAO.softDelete(promoId);
    }

    // ─── Mock coupons ────────────────────────────────────────────────────────

    /**
     * Mock coupon SHOP — dùng cho DEV khi chưa có dữ liệu DB.
     * SHOP10: Giảm 10%, max 50,000, min_order 100,000, scope SHOP
     */
    private Promotion getMockShopCoupon(String code, BigDecimal subtotal) {
        if ("SHOP10".equals(code)) {
            if (subtotal.compareTo(new BigDecimal("100000")) < 0) return null;
            return buildMockPromo(0, "SHOP10", "PERCENT", "SHOP", "ORDER",
                    new BigDecimal("10"), new BigDecimal("50000"), new BigDecimal("100000"));
        }
        return null;
    }

    /**
     * Mock coupon SÀN — dùng cho DEV khi chưa có dữ liệu DB.
     * SAAN5: Giảm cố định 5,000, min_order 50,000, scope ALL
     * SALE20: Giảm 20%, max 100,000, min_order 200,000, scope ALL
     */
    private Promotion getMockSystemCoupon(String code, BigDecimal subtotal) {
        if ("SAAN5".equals(code)) {
            if (subtotal.compareTo(new BigDecimal("50000")) < 0) return null;
            return buildMockPromo(0, "SAAN5", "FIXED", "ALL", "ORDER",
                    new BigDecimal("5000"), new BigDecimal("5000"), new BigDecimal("50000"));
        }
        if ("SALE20".equals(code)) {
            if (subtotal.compareTo(new BigDecimal("200000")) < 0) return null;
            return buildMockPromo(0, "SALE20", "PERCENT", "ALL", "ORDER",
                    new BigDecimal("20"), new BigDecimal("100000"), new BigDecimal("200000"));
        }
        return null;
    }

    private Promotion buildMockPromo(int id, String code, String discountType, String discountScope,
                                     String scope, BigDecimal discountValue, BigDecimal discountMax,
                                     BigDecimal minOrderValue) {
        Promotion p = new Promotion();
        p.setPromoId(id);
        p.setCode(code);
        p.setDiscountType(discountType);
        p.setDiscountScope(discountScope);
        p.setScope(scope);
        p.setDiscountValue(discountValue);
        p.setDiscountMax(discountMax);
        p.setMinOrderValue(minOrderValue);
        p.setUsedCount(0);
        p.setMaxUses(null);
        p.setCanStack(true);
        p.setIsActive(true);
        p.setIsDeleted(false);
        return p;
    }
}
