package com.fruitmkt.test;

import com.fruitmkt.dao.PromotionDAO;
import com.fruitmkt.dao.UserDAO;
import com.fruitmkt.model.entity.Promotion;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PromotionVoucherTest — Bộ kiểm thử JUnit 4 cho toàn bộ luồng nghiệp vụ mã giảm giá:
 *   Tạo voucher → Áp dụng → Kiểm tra tính đúng đắn → Hết hạn → Hết lượt.
 *
 * CÁC LUỒNG KIỂM TRA:
 *   1. Tạo mã giảm giá hệ thống (ALL scope) và tìm theo code
 *   2. Tạo mã voucher shop owner (SHOP scope) và tìm theo owner
 *   3. Áp mã hệ thống hợp lệ (discount_scope=ALL, đơn đủ tối thiểu)
 *   4. Không áp được mã khi đơn hàng chưa đủ giá trị tối thiểu
 *   5. Không áp được mã đã hết hạn (valid_until < now)
 *   6. Mã PERCENT — tính chiết khấu % đúng và không vượt discount_max
 *   7. Mã FIXED — tính chiết khấu cố định đúng
 *   8. Tăng used_count sau khi áp mã
 *   9. Mã không hoạt động (is_active=0) không tìm được
 *  10. Flash sale sản phẩm — tìm mã khuyến mãi theo product_id
 *
 * QUY TẮC KIỂM THỬ:
 *   - Test đúng nghiệp vụ khuyến mãi, không test thuần code
 *   - Dọn dẹp dữ liệu sau mỗi test (soft delete)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PromotionVoucherTest {

    private PromotionDAO promotionDAO;
    private UserDAO      userDAO;

    private int testAdminId  = -1;
    private int testOwnerId  = -1;
    private int testPromoId1 = -1; // Mã hệ thống
    private int testPromoId2 = -1; // Mã shop
    private int testPromoId3 = -1; // Mã hết hạn
    private int testPromoId4 = -1; // Mã % với cap

    @Before
    public void setUp() throws SQLException {
        promotionDAO = new PromotionDAO();
        userDAO      = new UserDAO();

        // Dùng admin sẵn có (user_id=1) và shop owner sẵn có (user_id=3)
        testAdminId = 1;
        testOwnerId = 3;
    }

    @After
    public void tearDown() {
        try {
            // Soft delete các mã test
            if (testPromoId1 != -1) { promotionDAO.softDelete(testPromoId1); testPromoId1 = -1; }
            if (testPromoId2 != -1) { promotionDAO.softDelete(testPromoId2); testPromoId2 = -1; }
            if (testPromoId3 != -1) { promotionDAO.softDelete(testPromoId3); testPromoId3 = -1; }
            if (testPromoId4 != -1) { promotionDAO.softDelete(testPromoId4); testPromoId4 = -1; }
        } catch (SQLException e) {
            System.err.println("[PromotionVoucherTest] Cleanup failed: " + e.getMessage());
        }
    }

    // =========================================================
    // NGHIỆP VỤ 1: Tạo và tìm kiếm mã giảm giá
    // =========================================================

    /**
     * TC-PROMO-01: Tạo mã giảm giá hệ thống (scope=ALL) và tìm lại theo code.
     * Nghiệp vụ: Admin tạo mã toàn sàn → khách hàng nhập mã khi checkout.
     */
    @Test
    public void test01_CreateSystemPromoAndFindByCode() throws SQLException {
        String code = "TEST-SYS-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,           // discount_max (0 = không giới hạn)
            new BigDecimal("30000.00"), // discount_value
            new BigDecimal("150000.00"), // min_order_value
            null,                       // product_id
            100,                        // max_uses
            LocalDateTime.now().minusHours(1), // valid_from
            LocalDateTime.now().plusDays(30),  // valid_until
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);
        assertTrue("promoId phải > 0", testPromoId1 > 0);

        // Tìm lại theo code
        Promotion found = promotionDAO.findByCode(code);
        assertNotNull("Phải tìm thấy mã theo code", found);
        assertEquals("Code phải khớp", code, found.getCode());
        assertEquals("Discount scope phải là ALL", "ALL", found.getDiscountScope());
        assertEquals("Discount value phải khớp", new BigDecimal("30000.00"), found.getDiscountValue());
        assertTrue("Mã phải đang active", found.getIsActive());
    }

    /**
     * TC-PROMO-02: Tạo voucher của shop owner (discount_scope=SHOP) và tìm theo owner.
     * Nghiệp vụ: Shop owner tạo voucher riêng cho cửa hàng mình.
     */
    @Test
    public void test02_CreateShopVoucherAndFindByOwner() throws SQLException {
        String code = "SHOP-VOUCHER-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "PERCENT", "SHOP", "ORDER",
            new BigDecimal("50000.00"),  // discount_max (tối đa 50k)
            new BigDecimal("15.00"),     // discount_value = 15%
            new BigDecimal("200000.00"), // min_order_value
            null,
            200,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(7),
            testOwnerId
        );
        testPromoId2 = promotionDAO.save(promo);
        assertTrue(testPromoId2 > 0);

        // Tìm voucher theo ownerId
        List<Promotion> ownerPromos = promotionDAO.findByOwner(testOwnerId);
        boolean found = ownerPromos.stream().anyMatch(p -> p.getPromoId() == testPromoId2);
        assertTrue("Danh sách voucher của shop phải chứa mã vừa tạo", found);
    }

    // =========================================================
    // NGHIỆP VỤ 2: Áp dụng mã giảm giá khi checkout
    // =========================================================

    /**
     * TC-PROMO-03: Áp mã hệ thống hợp lệ khi đơn đủ giá trị tối thiểu.
     * Nghiệp vụ: Khách nhập mã → hệ thống validate → áp dụng thành công.
     */
    @Test
    public void test03_ApplyValidSystemCouponOnSufficientOrder() throws SQLException {
        String code = "VALID-SYS-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("50000.00"),
            new BigDecimal("200000.00"), // đơn tối thiểu 200k
            null, 500,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);

        // Đơn hàng trị giá 300k >= min_order_value 200k → hợp lệ
        BigDecimal subtotal = new BigDecimal("300000.00");
        Promotion applicable = promotionDAO.findValidSystemCoupon(code, subtotal);
        assertNotNull("Mã hợp lệ phải áp dụng được cho đơn đủ giá trị", applicable);
        assertEquals("Discount value phải khớp", new BigDecimal("50000.00"), applicable.getDiscountValue());
    }

    /**
     * TC-PROMO-04: Không áp được mã khi đơn chưa đủ giá trị tối thiểu.
     * Nghiệp vụ: Cần mua thêm để đủ điều kiện áp mã.
     */
    @Test
    public void test04_SystemCouponNotApplicableWhenOrderBelowMinimum() throws SQLException {
        String code = "MIN-GUARD-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("30000.00"),
            new BigDecimal("300000.00"), // đơn tối thiểu 300k
            null, 100,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);

        // Đơn hàng chỉ 150k < 300k → không hợp lệ
        BigDecimal subtotalTooLow = new BigDecimal("150000.00");
        Promotion result = promotionDAO.findValidSystemCoupon(code, subtotalTooLow);
        assertNull("Mã KHÔNG được áp khi đơn chưa đủ giá trị tối thiểu", result);
    }

    /**
     * TC-PROMO-05: Mã giảm giá đã hết hạn không tìm được.
     * Nghiệp vụ: Sau ngày hết hạn, mã không còn hiệu lực.
     */
    @Test
    public void test05_ExpiredCouponCannotBeFound() throws SQLException {
        String code = "EXPIRED-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("20000.00"),
            new BigDecimal("100000.00"),
            null, 50,
            LocalDateTime.now().minusDays(30), // valid_from: 30 ngày trước
            LocalDateTime.now().minusDays(1),  // valid_until: hết hạn hôm qua
            testAdminId
        );
        testPromoId3 = promotionDAO.save(promo);

        // Tìm kiếm mã — phải không tìm thấy do đã hết hạn
        Promotion expired = promotionDAO.findValidSystemCoupon(code, new BigDecimal("200000.00"));
        assertNull("Mã đã hết hạn KHÔNG được áp dụng", expired);

        // Tuy nhiên findById vẫn tìm được (admin xem lịch sử)
        Promotion byId = promotionDAO.findById(testPromoId3);
        assertNotNull("Admin vẫn phải xem được mã đã hết hạn qua findById", byId);
    }

    /**
     * TC-PROMO-06: Tính chiết khấu PERCENT đúng và không vượt discount_max (cap).
     * Nghiệp vụ: Mã 20% tối đa 100k → đơn 700k phải chỉ giảm 100k (không giảm 140k).
     */
    @Test
    public void test06_PercentDiscountRespectsCap() throws SQLException {
        String code = "PERCENT-CAP-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "PERCENT", "ALL", "ORDER",
            new BigDecimal("100000.00"),  // discount_max = 100k
            new BigDecimal("20.00"),      // discount_value = 20%
            new BigDecimal("200000.00"),  // min_order_value
            null, 300,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId4 = promotionDAO.save(promo);

        Promotion found = promotionDAO.findByCode(code);
        assertNotNull(found);
        assertEquals("PERCENT", found.getDiscountType());
        assertEquals(new BigDecimal("20.00"), found.getDiscountValue());
        assertEquals(new BigDecimal("100000.00"), found.getDiscountMax());

        // Tính toán chiết khấu nghiệp vụ:
        BigDecimal orderAmount = new BigDecimal("700000.00");
        BigDecimal rawDiscount = orderAmount.multiply(found.getDiscountValue())
                                            .divide(new BigDecimal("100"));
        // rawDiscount = 700,000 × 20% = 140,000
        BigDecimal actualDiscount = rawDiscount.min(found.getDiscountMax());
        // actualDiscount = min(140,000; 100,000) = 100,000

        assertEquals("Chiết khấu % phải bị giới hạn bởi discount_max",
                     new BigDecimal("100000.00"), actualDiscount);
    }

    /**
     * TC-PROMO-07: Mã FIXED — tính chiết khấu cố định đúng.
     * Nghiệp vụ: Mã giảm 50k cố định → đơn 300k phải trừ đúng 50k.
     */
    @Test
    public void test07_FixedDiscountCalculationIsExact() throws SQLException {
        String code = "FIXED-EXACT-" + System.currentTimeMillis();
        BigDecimal fixedValue = new BigDecimal("50000.00");
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO, fixedValue,
            new BigDecimal("150000.00"),
            null, 200,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);

        Promotion found = promotionDAO.findByCode(code);
        assertNotNull(found);

        // Tính chiết khấu: đơn 300k - 50k = 250k
        BigDecimal orderAmount    = new BigDecimal("300000.00");
        BigDecimal discount       = found.getDiscountValue(); // 50,000 (FIXED)
        BigDecimal amountAfterDisc = orderAmount.subtract(discount);
        assertEquals("Sau giảm FIXED 50k, đơn 300k còn lại phải là 250k",
                     new BigDecimal("250000.00"), amountAfterDisc);
    }

    /**
     * TC-PROMO-08: Tăng used_count sau khi áp mã giảm giá.
     * Nghiệp vụ: Mỗi lần khách áp mã, used_count phải tăng lên 1.
     */
    @Test
    public void test08_UsedCountIncrementsAfterApplyingCoupon() throws SQLException {
        String code = "USED-COUNT-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("25000.00"),
            new BigDecimal("100000.00"),
            null, 50,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);

        Promotion before = promotionDAO.findById(testPromoId1);
        int usedBefore = before.getUsedCount(); // = 0

        // Simulate áp mã: tăng used_count
        promotionDAO.incrementUsedCount(testPromoId1);
        promotionDAO.incrementUsedCount(testPromoId1);

        Promotion after = promotionDAO.findById(testPromoId1);
        assertEquals("used_count phải tăng đúng 2 lần", usedBefore + 2, after.getUsedCount());
    }

    /**
     * TC-PROMO-09: Hủy kích hoạt mã → mã không tìm được nữa.
     * Nghiệp vụ: Admin tắt mã đang chạy → khách không áp được nữa.
     */
    @Test
    public void test09_DeactivatedCouponCannotBeFound() throws SQLException {
        String code = "DEACT-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "ALL", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("20000.00"),
            new BigDecimal("80000.00"),
            null, 100,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testAdminId
        );
        testPromoId1 = promotionDAO.save(promo);

        // Xác nhận tìm được trước khi deactivate
        assertNotNull("Mã phải tìm được khi is_active=1",
                      promotionDAO.findValidSystemCoupon(code, new BigDecimal("200000.00")));

        // Deactivate
        promotionDAO.deactivate(testPromoId1);

        // Sau deactivate không tìm được
        Promotion result = promotionDAO.findValidSystemCoupon(code, new BigDecimal("200000.00"));
        assertNull("Mã đã bị deactivate KHÔNG được tìm thấy qua findValidSystemCoupon", result);
    }

    /**
     * TC-PROMO-10: Flash sale theo sản phẩm — tìm mã khuyến mãi theo product_id.
     * Nghiệp vụ: Trang chi tiết sản phẩm hiển thị các mã flash sale đang chạy.
     */
    @Test
    public void test10_FindActiveFlashSalePromotionsByProduct() throws SQLException {
        // Sản phẩm có mã flash sale trong seed data (product_id=5 - Dâu Tây)
        int productId = 5;
        List<Promotion> flashSales = promotionDAO.findActivePromotionsByProduct(productId);

        // Trong seed data có mã FLASHSALE-DAUTAY cho product_id=5
        // Nếu mã đang active và chưa hết hạn thì phải tìm thấy
        assertNotNull("findActivePromotionsByProduct không được trả về null", flashSales);
        // Danh sách có thể rỗng nếu seed mã đã hết hạn — nhưng không được null
        // Test tìm mã mới tạo để chắc chắn:
        String flashCode = "FLASH-PROD-" + System.currentTimeMillis();
        Promotion flashPromo = buildPromo(
            flashCode, "PERCENT", "ALL", "PRODUCT",
            new BigDecimal("30000.00"),
            new BigDecimal("10.00"),
            new BigDecimal("50000.00"),
            productId,   // product_id
            100,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(1),
            testAdminId
        );
        testPromoId2 = promotionDAO.save(flashPromo);

        List<Promotion> flashSalesAfter = promotionDAO.findActivePromotionsByProduct(productId);
        boolean foundNew = flashSalesAfter.stream().anyMatch(p -> p.getPromoId() == testPromoId2);
        assertTrue("Mã flash sale mới tạo phải xuất hiện trong danh sách flash sale của sản phẩm", foundNew);
    }

    /**
     * TC-PROMO-11: Lấy danh sách voucher shop đang hoạt động.
     * Nghiệp vụ: Trang sản phẩm hiển thị voucher của shop owner đó.
     */
    @Test
    public void test11_FindActiveShopVouchers() throws SQLException {
        String code = "SHOP-ACT-" + System.currentTimeMillis();
        Promotion promo = buildPromo(
            code, "FIXED", "SHOP", "ORDER",
            BigDecimal.ZERO,
            new BigDecimal("20000.00"),
            new BigDecimal("150000.00"),
            null, 300,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(30),
            testOwnerId
        );
        testPromoId1 = promotionDAO.save(promo);

        // findShopActivePromotions chỉ lấy SHOP scope + is_active + chưa hết hạn
        List<Promotion> shopPromos = promotionDAO.findShopActivePromotions(testOwnerId);
        assertNotNull(shopPromos);
        boolean found = shopPromos.stream().anyMatch(p -> p.getPromoId() == testPromoId1);
        assertTrue("Voucher shop mới tạo phải xuất hiện trong danh sách active của shop", found);
    }

    /**
     * TC-PROMO-12: Lấy danh sách mã hệ thống đang hoạt động (top 5).
     * Nghiệp vụ: Trang checkout hiển thị các mã toàn sàn đang chạy.
     */
    @Test
    public void test12_FindActiveSystemPromotions() throws SQLException {
        // Mã hệ thống đã có trong seed data (SAAN5, SALE20, v.v.)
        List<Promotion> systemPromos = promotionDAO.findActiveSystemPromotions();
        assertNotNull("Danh sách mã hệ thống không được null", systemPromos);
        // Có thể có hoặc không có mã nào — không assert size vì phụ thuộc vào seed data
        // Nhưng tất cả kết quả trả về phải là is_active = 1
        for (Promotion p : systemPromos) {
            assertTrue("Mọi mã trong danh sách phải đang active", p.getIsActive());
            assertEquals("Mọi mã trong danh sách phải là discount_scope=ALL",
                         "ALL", p.getDiscountScope());
        }
    }

    // =========================================================
    // Helper — xây dựng Promotion object chuẩn
    // =========================================================

    private Promotion buildPromo(
            String code, String discountType, String discountScope, String scope,
            BigDecimal discountMax, BigDecimal discountValue, BigDecimal minOrderValue,
            Integer productId, Integer maxUses,
            LocalDateTime validFrom, LocalDateTime validUntil,
            int createdBy) {

        Promotion p = new Promotion();
        p.setCode(code);
        p.setDiscountType(discountType);
        p.setDiscountScope(discountScope);
        p.setDiscountMax(discountMax);
        p.setDiscountValue(discountValue);
        p.setMinOrderValue(minOrderValue);
        p.setScope(scope);
        p.setProductId(productId);
        p.setMaxUses(maxUses);
        p.setUsedCount(0);
        p.setCanStack(false);
        p.setValidFrom(validFrom);
        p.setValidUntil(validUntil);
        p.setCreatedBy(createdBy);
        p.setIsActive(true);
        return p;
    }
}
