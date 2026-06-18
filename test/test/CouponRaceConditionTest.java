package test;

import config.AppConfig;
import dao.auth.UserDAO;
import dao.shop.PromotionDAO;
import model.entity.Promotion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * CouponRaceConditionTest — Kiểm tra atomic claimUsage() phòng race condition.
 *
 * Kịch bản: 20 thread đồng thời cố gọi claimUsage() cho coupon có max_uses=5.
 * Kết quả đúng: chỉ đúng 5 thread thành công, used_count == 5.
 */
public class CouponRaceConditionTest {

    private static final int MAX_USES = 5;
    private static final int THREAD_COUNT = 20;

    private PromotionDAO promotionDAO;
    private UserDAO userDAO;

    private int testAdminId = -1;
    private int testPromoId = -1;

    @Before
    public void setUp() throws Exception {
        promotionDAO = new PromotionDAO();
        userDAO = new UserDAO();

        long ts = System.currentTimeMillis();
        // Dùng admin id=1 sẵn có (hoặc tạo tạm)
        testAdminId = 1;

        // Tạo coupon với max_uses = 5
        Promotion promo = new Promotion();
        promo.setCode("RACE_TEST_" + ts);
        promo.setDiscountType("FIXED");
        promo.setDiscountScope("ALL");
        promo.setDiscountMax(new BigDecimal("50000"));
        promo.setDiscountValue(new BigDecimal("50000"));
        promo.setMinOrderValue(new BigDecimal("0"));
        promo.setScope("ORDER");
        promo.setProductId(null);
        promo.setMaxUses(MAX_USES);
        promo.setUsedCount(0);
        promo.setCanStack(false);
        promo.setValidFrom(LocalDateTime.now().minusDays(1));
        promo.setValidUntil(LocalDateTime.now().plusDays(30));
        promo.setCreatedBy(testAdminId);
        promo.setIsActive(true);
        testPromoId = promotionDAO.save(promo);
    }

    @After
    public void tearDown() {
        try {
            if (testPromoId != -1) {
                promotionDAO.softDelete(testPromoId);
                testPromoId = -1;
            }
        } catch (SQLException e) {
            System.err.println("[CouponRaceConditionTest] Cleanup failed: " + e.getMessage());
        }
    }

    /**
     * TC-RACE-01: 20 thread đồng thời claimUsage() — chỉ đúng MAX_USES=5 được chấp nhận.
     */
    @Test
    public void should_allowExactlyMaxUses_whenConcurrentClaimsExceedLimit() throws InterruptedException {
        final int promoId = testPromoId;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    done.countDown();
                    return;
                }

                Connection conn = null;
                try {
                    conn = promotionDAO.getConnection();
                    conn.setAutoCommit(false);
                    boolean claimed = promotionDAO.claimUsage(conn, promoId);
                    conn.commit();
                    if (claimed) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    if (conn != null) {
                        try { conn.rollback(); } catch (Exception ignored) {}
                    }
                } finally {
                    if (conn != null) {
                        try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
                    }
                    done.countDown();
                }
            });
        }

        ready.await(10, TimeUnit.SECONDS);
        start.countDown(); // Tất cả thread bắt đầu cùng lúc
        assertTrue("Tất cả thread phải kết thúc trong 30s", done.await(30, TimeUnit.SECONDS));
        pool.shutdown();

        // Tổng request = THREAD_COUNT
        assertEquals("Tổng success + fail phải = " + THREAD_COUNT,
            THREAD_COUNT, successCount.get() + failCount.get());

        // Chỉ đúng MAX_USES thread thành công
        assertEquals("Phải đúng " + MAX_USES + " lần claim thành công",
            MAX_USES, successCount.get());

        // Kiểm tra used_count trong DB
        Promotion reloaded = promotionDAO.findById(testPromoId);
        assertNotNull("Coupon phải tồn tại sau test", reloaded);
        assertEquals("used_count trong DB phải đúng bằng MAX_USES=" + MAX_USES,
            MAX_USES, reloaded.getUsedCount());

        // Đảm bảo không vượt max_uses
        assertTrue("used_count KHÔNG được vượt max_uses",
            reloaded.getUsedCount() <= MAX_USES);
    }

    /**
     * TC-RACE-02: Khi max_uses=NULL (không giới hạn), claimUsage() luôn trả về true.
     */
    @Test
    public void should_alwaysSucceed_whenMaxUsesIsNull() throws Exception {
        // Tạo coupon không giới hạn lượt
        Promotion unlimitedPromo = new Promotion();
        unlimitedPromo.setCode("RACE_UNLIMITED_" + System.currentTimeMillis());
        unlimitedPromo.setDiscountType("FIXED");
        unlimitedPromo.setDiscountScope("ALL");
        unlimitedPromo.setDiscountMax(new BigDecimal("10000"));
        unlimitedPromo.setDiscountValue(new BigDecimal("10000"));
        unlimitedPromo.setMinOrderValue(BigDecimal.ZERO);
        unlimitedPromo.setScope("ORDER");
        unlimitedPromo.setProductId(null);
        unlimitedPromo.setMaxUses(null); // NULL = không giới hạn
        unlimitedPromo.setUsedCount(0);
        unlimitedPromo.setCanStack(false);
        unlimitedPromo.setValidFrom(LocalDateTime.now().minusDays(1));
        unlimitedPromo.setValidUntil(LocalDateTime.now().plusDays(30));
        unlimitedPromo.setCreatedBy(testAdminId);
        unlimitedPromo.setIsActive(true);
        int unlimitedId = promotionDAO.save(unlimitedPromo);

        try {
            try (Connection conn = promotionDAO.getConnection()) {
                conn.setAutoCommit(false);
                boolean claimed = promotionDAO.claimUsage(conn, unlimitedId);
                conn.commit();
                assertTrue("max_uses=NULL phải luôn trả về true", claimed);
            }
            Promotion reloaded = promotionDAO.findById(unlimitedId);
            assertEquals("used_count phải = 1", 1, reloaded.getUsedCount());
        } finally {
            promotionDAO.softDelete(unlimitedId);
        }
    }

    /**
     * TC-RACE-03: Coupon đã dùng hết (used_count = max_uses), claimUsage() trả về false.
     */
    @Test
    public void should_returnFalse_whenCouponAlreadyExhausted() throws Exception {
        // Tạo coupon đã hết (used_count = max_uses = 1)
        Promotion exhaustedPromo = new Promotion();
        exhaustedPromo.setCode("RACE_EXHAUSTED_" + System.currentTimeMillis());
        exhaustedPromo.setDiscountType("FIXED");
        exhaustedPromo.setDiscountScope("ALL");
        exhaustedPromo.setDiscountMax(new BigDecimal("10000"));
        exhaustedPromo.setDiscountValue(new BigDecimal("10000"));
        exhaustedPromo.setMinOrderValue(BigDecimal.ZERO);
        exhaustedPromo.setScope("ORDER");
        exhaustedPromo.setProductId(null);
        exhaustedPromo.setMaxUses(1);
        exhaustedPromo.setUsedCount(1); // Đã dùng hết
        exhaustedPromo.setCanStack(false);
        exhaustedPromo.setValidFrom(LocalDateTime.now().minusDays(1));
        exhaustedPromo.setValidUntil(LocalDateTime.now().plusDays(30));
        exhaustedPromo.setCreatedBy(testAdminId);
        exhaustedPromo.setIsActive(true);
        int exhaustedId = promotionDAO.save(exhaustedPromo);

        try {
            try (Connection conn = promotionDAO.getConnection()) {
                conn.setAutoCommit(false);
                boolean claimed = promotionDAO.claimUsage(conn, exhaustedId);
                conn.commit();
                assertFalse("Coupon đã hết lượt phải trả về false", claimed);
            }
            Promotion reloaded = promotionDAO.findById(exhaustedId);
            assertEquals("used_count phải vẫn = 1 (không tăng thêm)", 1, reloaded.getUsedCount());
        } finally {
            promotionDAO.softDelete(exhaustedId);
        }
    }
}
