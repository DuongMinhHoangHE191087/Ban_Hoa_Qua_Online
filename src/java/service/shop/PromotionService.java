package service.shop;
import dao.shop.PromotionDAO;

import dao.catalog.ProductDAO;

import exception.BusinessException;
import model.entity.catalog.Product;
import model.entity.Promotion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * PromotionService - business logic for promotions / vouchers.
 */
public class PromotionService {

    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private static final boolean ENABLE_MOCK_COUPONS = false;

    public Promotion validateShopCoupon(String code, int ownerId, BigDecimal subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        code = code.trim().toUpperCase();

        if (ENABLE_MOCK_COUPONS) {
            Promotion mock = getMockShopCoupon(code, subtotal);
            if (mock != null) {
                return mock;
            }
        }

        Promotion promo = promotionDAO.findValidShopCoupon(code, ownerId, subtotal);
        if (promo == null) {
            return null;
        }

        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            return null;
        }
        return promo;
    }

    public Promotion validateSystemCoupon(String code, BigDecimal subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        code = code.trim().toUpperCase();

        if (ENABLE_MOCK_COUPONS) {
            Promotion mock = getMockSystemCoupon(code, subtotal);
            if (mock != null) {
                return mock;
            }
        }

        Promotion promo = promotionDAO.findValidSystemCoupon(code, subtotal);
        if (promo == null) {
            return null;
        }

        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            return null;
        }
        return promo;
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal base) {
        if (promo == null || base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("PERCENT".equalsIgnoreCase(promo.getDiscountType())) {
            discount = base.multiply(promo.getDiscountValue())
                           .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            if (promo.getDiscountMax() != null && promo.getDiscountMax().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(promo.getDiscountMax());
            }
        } else {
            discount = promo.getDiscountValue().min(base);
        }
        return discount.max(BigDecimal.ZERO);
    }

    public BigDecimal[] calculateAllDiscounts(Promotion shopPromo, Promotion systemPromo,
                                              BigDecimal subtotal, BigDecimal deliveryFee) {
        BigDecimal shopDiscount = calculateDiscount(shopPromo, subtotal);
        BigDecimal afterShop = subtotal.subtract(shopDiscount).max(BigDecimal.ZERO);
        BigDecimal systemDiscount = calculateDiscount(systemPromo, afterShop);
        BigDecimal totalDiscount = shopDiscount.add(systemDiscount);
        BigDecimal finalAmount = subtotal.subtract(totalDiscount).add(deliveryFee).max(BigDecimal.ZERO);
        return new BigDecimal[]{shopDiscount, systemDiscount, totalDiscount, finalAmount};
    }

    /**
     * PRO-01: A customer may NOT use two discount-type coupons together.
     * Allowed combo: 1 discount coupon + 1 free-shipping coupon (SHIPPING scope — currently
     * free-shipping vouchers are not in this schema, so any two ORDER-discount coupons are rejected).
     * Call this before saving the order to enforce server-side stacking rules.
     *
     * Returns the DB record; never trusts a client-supplied discount value.
     *
     * @throws BusinessException with code COUPON-SCOPE when scope mismatch is detected
     */
    public void validateCouponStack(java.util.List<Promotion> shopPromos, java.util.List<Promotion> systemPromos) {
        int totalCoupons = (shopPromos != null ? shopPromos.size() : 0) + (systemPromos != null ? systemPromos.size() : 0);
        if (totalCoupons <= 1) {
            return; // Single coupon is always allowed
        }
        
        int systemCount = systemPromos != null ? systemPromos.size() : 0;
        if (systemCount > 2) {
            throw new BusinessException("PRO-01", "Tối đa chỉ được dùng 2 voucher sàn.");
        }
        
        java.util.Map<Integer, Integer> shopCounts = new java.util.HashMap<>();
        if (shopPromos != null) {
            for (Promotion p : shopPromos) {
                shopCounts.put(p.getCreatedBy(), shopCounts.getOrDefault(p.getCreatedBy(), 0) + 1);
            }
        }
        for (java.util.Map.Entry<Integer, Integer> entry : shopCounts.entrySet()) {
            if (entry.getValue() > 2) {
                throw new BusinessException("PRO-01", "Mỗi shop tối đa chỉ được dùng 2 voucher shop.");
            }
        }

        if (shopPromos != null) {
            for (Promotion p : shopPromos) {
                if (!p.getCanStack()) {
                    throw new BusinessException("PRO-01", "Mã giảm giá [" + p.getCode() + "] không hỗ trợ cộng dồn.");
                }
            }
        }
        if (systemPromos != null) {
            for (Promotion p : systemPromos) {
                if (!p.getCanStack()) {
                    throw new BusinessException("PRO-01", "Mã giảm giá [" + p.getCode() + "] không hỗ trợ cộng dồn.");
                }
            }
        }
    }

    public void validateCouponStack(Promotion shopPromo, Promotion systemPromo) {
        java.util.List<Promotion> shops = new java.util.ArrayList<>();
        if (shopPromo != null) shops.add(shopPromo);
        java.util.List<Promotion> systems = new java.util.ArrayList<>();
        if (systemPromo != null) systems.add(systemPromo);
        validateCouponStack(shops, systems);
    }

    /**
     * Re-resolves a shop coupon from DB by code and validates scope ownership.
     * A SHOP coupon's createdBy must match the target shop's ownerId.
     * Returns the DB record; never trusts a client-supplied discount value.
     *
     * @throws BusinessException with code COUPON-SCOPE when scope mismatch is detected
     */
    public Promotion resolveAndValidateShopCouponScope(String code, int expectedOwnerId,
                                                       BigDecimal subtotal) throws SQLException {
        if (code == null || code.trim().isEmpty()) return null;
        code = code.trim().toUpperCase();
        Promotion promo = promotionDAO.findValidShopCoupon(code, expectedOwnerId, subtotal);
        if (promo == null) {
            throw new BusinessException("COUPON-SCOPE",
                    "Mã giảm giá shop không hợp lệ, không thuộc shop này, hoặc chưa đạt giá trị đơn tối thiểu.");
        }
        if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
            throw new BusinessException("COUPON-SCOPE", "Mã giảm giá [" + code + "] đã hết lượt sử dụng.");
        }
        return promo;
    }

    public int createPromotion(Promotion promo) throws SQLException {
        validatePromotion(promo);
        return promotionDAO.save(promo);
    }

    public void updatePromotion(Promotion promo) throws SQLException {
        validatePromotion(promo);
        promotionDAO.update(promo);
    }

    public List<Promotion> getShopPromos(int ownerId) throws SQLException {
        return promotionDAO.findByOwner(ownerId);
    }

    public List<Promotion> getGlobalPromotions() throws SQLException {
        return promotionDAO.findGlobalPromotions();
    }

    public int createShopPromotion(Promotion promo, int ownerId) throws SQLException {
        validateShopPromotion(promo, ownerId);
        promo.setCreatedBy(ownerId);
        return promotionDAO.save(promo);
    }

    public void updateShopPromotion(Promotion promo, int ownerId) throws SQLException {
        validateShopPromotion(promo, ownerId);
        promotionDAO.update(promo);
    }

    public int createGlobalPromotion(Promotion promo, int adminId) throws SQLException {
        validateGlobalPromotion(promo, adminId);
        promo.setCreatedBy(adminId);
        return promotionDAO.save(promo);
    }

    public void updateGlobalPromotion(Promotion promo, int adminId) throws SQLException {
        validateGlobalPromotion(promo, adminId);
        promotionDAO.update(promo);
    }

    public Promotion getPromotionById(int promoId) throws SQLException {
        if (promoId <= 0) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ.");
        }
        return promotionDAO.findById(promoId);
    }

    public List<Product> getPromotionProductsForAdmin() throws SQLException {
        return productDAO.findAllAdminProducts(1, 1000, "APPROVED");
    }

    public void deactivate(int promoId) throws SQLException {
        promotionDAO.deactivate(promoId);
    }

    public void softDelete(int promoId) throws SQLException {
        promotionDAO.softDelete(promoId);
    }

    private void validatePromotion(Promotion promo) {
        if (promo == null) {
            throw new IllegalArgumentException("Dữ liệu khuyến mãi không hợp lệ.");
        }
        if (promo.getCode() == null || promo.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống.");
        }
        if (promo.getDiscountType() == null
                || (!"PERCENT".equalsIgnoreCase(promo.getDiscountType())
                && !"FIXED".equalsIgnoreCase(promo.getDiscountType()))) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        }
        if (promo.getDiscountScope() == null
                || (!"SHOP".equalsIgnoreCase(promo.getDiscountScope())
                && !"ALL".equalsIgnoreCase(promo.getDiscountScope()))) {
            throw new IllegalArgumentException("Phạm vi giảm giá không hợp lệ.");
        }
        if (promo.getScope() == null
                || (!"ORDER".equalsIgnoreCase(promo.getScope())
                && !"PRODUCT".equalsIgnoreCase(promo.getScope()))) {
            throw new IllegalArgumentException("Quy tắc áp dụng không hợp lệ.");
        }
        if (promo.getDiscountValue() == null
                || promo.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải lớn hơn 0.");
        }
        if ("PERCENT".equalsIgnoreCase(promo.getDiscountType())
                && promo.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100%.");
        }
        if (promo.getDiscountMax() != null && promo.getDiscountMax().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị giảm tối đa không hợp lệ.");
        }
        if (promo.getMinOrderValue() != null && promo.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không hợp lệ.");
        }
        if (promo.getValidFrom() == null || promo.getValidUntil() == null
                || promo.getValidFrom().isAfter(promo.getValidUntil())) {
            throw new IllegalArgumentException("Thời gian hiệu lực không hợp lệ.");
        }
        if ("PRODUCT".equalsIgnoreCase(promo.getScope()) && promo.getProductId() == null) {
            throw new IllegalArgumentException("Khuyến mãi theo sản phẩm cần chọn sản phẩm áp dụng.");
        }
    }

    private void validateShopPromotion(Promotion promo, int ownerId) throws SQLException {
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Chủ shop không hợp lệ.");
        }
        validatePromotion(promo);
        // Task 5: force coupon's owner_id to the authenticated shop owner — never trust client-supplied id
        promo.setCreatedBy(ownerId);
        enforceDiscountScope(promo, "SHOP");
        // Task 5: expiry must be in the future
        if (promo.getValidUntil() != null && !promo.getValidUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày kết thúc phải ở tương lai.");
        }
        // Task 5: max-usages > 0 when set
        if (promo.getMaxUses() != null && promo.getMaxUses() <= 0) {
            throw new IllegalArgumentException("Số lượt sử dụng tối đa phải lớn hơn 0.");
        }
        validatePromotionProductOwnership(promo, ownerId, false);
        ensureUniqueCode(promo);
    }

    private void validateGlobalPromotion(Promotion promo, int adminId) throws SQLException {
        if (adminId <= 0) {
            throw new IllegalArgumentException("Quản trị viên không hợp lệ.");
        }
        validatePromotion(promo);
        // Task 6: system-scope coupons MUST be ALL; shops cannot self-issue system scope
        enforceDiscountScope(promo, "ALL");
        promo.setCreatedBy(adminId);
        // Task 5/6: expiry in future, maxUses > 0 when set
        if (promo.getValidUntil() != null && !promo.getValidUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày kết thúc phải ở tương lai.");
        }
        if (promo.getMaxUses() != null && promo.getMaxUses() <= 0) {
            throw new IllegalArgumentException("Số lượt sử dụng tối đa phải lớn hơn 0.");
        }
        validatePromotionProductOwnership(promo, adminId, true);
        ensureUniqueCode(promo);
    }

    private void enforceDiscountScope(Promotion promo, String expectedScope) {
        if (promo.getDiscountScope() == null || !expectedScope.equalsIgnoreCase(promo.getDiscountScope())) {
            throw new IllegalArgumentException("Phạm vi voucher không hợp lệ cho ngữ cảnh này.");
        }
        promo.setDiscountScope(expectedScope);
    }

    private void validatePromotionProductOwnership(Promotion promo, int ownerId, boolean globalMode)
            throws SQLException {
        if (!"PRODUCT".equalsIgnoreCase(promo.getScope())) {
            return;
        }
        if (promo.getProductId() == null) {
            throw new IllegalArgumentException("Khuyến mãi theo sản phẩm cần chọn sản phẩm áp dụng.");
        }

        List<Product> products = productDAO.findById(promo.getProductId());
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm áp dụng không tồn tại.");
        }
        Product product = products.get(0);
        if (!globalMode && product.getOwnerId() != ownerId) {
            throw new IllegalArgumentException("Sản phẩm áp dụng phải thuộc shop của bạn.");
        }
    }

    private void ensureUniqueCode(Promotion promo) throws SQLException {
        String normalized = promo.getCode().trim().toUpperCase();
        Promotion existing = promotionDAO.findAnyByCode(normalized);
        if (existing != null && existing.getPromoId() != promo.getPromoId()) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại.");
        }
        promo.setCode(normalized);
    }

    private Promotion getMockShopCoupon(String code, BigDecimal subtotal) {
        if ("SHOP10".equals(code)) {
            if (subtotal.compareTo(new BigDecimal("100000")) < 0) return null;
            return buildMockPromo(0, "SHOP10", "PERCENT", "SHOP", "ORDER",
                    new BigDecimal("10"), new BigDecimal("50000"), new BigDecimal("100000"));
        }
        return null;
    }

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
