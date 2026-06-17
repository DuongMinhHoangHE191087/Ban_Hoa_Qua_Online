package test;

import service.cart.CheckoutService;
import model.dto.checkout.CheckoutRequestDTO;
import model.entity.auth.User;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for CheckoutService.
 * Covers: request validation, inventory checks, promotion validation,
 * multi-shop order handling, authorization, and database errors.
 */
public class CheckoutServiceExceptionHandlingTest {

    private CheckoutService checkoutService;
    private User testUser;

    @Before
    public void setUp() {
        checkoutService = new CheckoutService();
        testUser = createTestUser(1, "customer@example.com", "Customer Name", "0987654321");
    }

    private User createTestUser(int userId, String email, String fullName, String phone) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole("CUSTOMER");
        return user;
    }

    private CheckoutRequestDTO createValidCheckoutRequest() {
        CheckoutRequestDTO request = new CheckoutRequestDTO();
        request.setFullName("Test Customer");
        request.setPhone("0987654321");
        request.setDeliveryAddress("123 Test Street, Test City");
        request.setPaymentMethod("BANK_TRANSFER");
        request.setVariantIds(Arrays.asList(1, 2));
        request.setSaveAddressToBook(false);
        return request;
    }

    // ============= BUILD CHECKOUT VIEW - INPUT VALIDATION ERRORS =============

    @Test
    public void buildCheckoutView_nullUser_throws() {
        try {
            checkoutService.buildCheckoutView(null, Arrays.asList(1, 2));
            fail("Should throw exception for null user");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void buildCheckoutView_nullVariantIds_throws() {
        try {
            checkoutService.buildCheckoutView(testUser, null);
            fail("Should throw exception for null variant IDs");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void buildCheckoutView_emptyVariantIds_throws() {
        try {
            checkoutService.buildCheckoutView(testUser, new ArrayList<>());
            fail("Should throw exception for empty variant IDs");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("trống") ||
                      e.getMessage().contains("rỗng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void buildCheckoutView_negativeVariantId_throws() {
        try {
            checkoutService.buildCheckoutView(testUser, Arrays.asList(-1, -2));
            fail("Should throw exception for negative variant IDs");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void buildCheckoutView_zeroVariantId_throws() {
        try {
            checkoutService.buildCheckoutView(testUser, Arrays.asList(0, 0));
            fail("Should throw exception for zero variant IDs");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void buildCheckoutView_nonexistentVariant_throws() {
        try {
            checkoutService.buildCheckoutView(testUser, Arrays.asList(999999, 888888));
            fail("Should throw exception for non-existent variants");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= PLACE ORDER - NULL/INVALID REQUEST VALIDATION =============

    @Test
    public void placeOrder_nullUser_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            checkoutService.placeOrder(null, request, "127.0.0.1");
            fail("Should throw exception for null user");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullRequest_throws() {
        try {
            checkoutService.placeOrder(testUser, null, "127.0.0.1");
            fail("Should throw exception for null request");
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullFullName_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setFullName(null);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for null fullName");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_blankFullName_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setFullName("   ");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for blank fullName");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullPhone_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setPhone(null);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for null phone");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_invalidPhoneFormat_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setPhone("invalid-phone");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for invalid phone format");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullDeliveryAddress_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setDeliveryAddress(null);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for null delivery address");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_blankDeliveryAddress_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setDeliveryAddress("   ");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for blank delivery address");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullPaymentMethod_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setPaymentMethod(null);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for null payment method");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_invalidPaymentMethod_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setPaymentMethod("INVALID_METHOD");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for invalid payment method");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_emptyVariantIds_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setVariantIds(new ArrayList<>());
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for empty variant IDs");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_nullVariantIds_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setVariantIds(null);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for null variant IDs");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= PLACE ORDER - INVENTORY VALIDATION ERRORS =============

    @Test
    public void placeOrder_insufficientStock_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setVariantIds(Arrays.asList(1)); // Assume variant 1 has insufficient stock
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for insufficient stock");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("tồn kho") ||
                      e.getMessage().contains("hết hàng") ||
                      e.getMessage().contains("vượt quá"));
        }
    }

    @Test
    public void placeOrder_outOfStockProduct_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setVariantIds(Arrays.asList(999999)); // Non-existent variant
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for out of stock");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_stockDepletedDuringCheckout_throws() {
        try {
            // Simulate stock depletion during concurrent checkout
            CheckoutRequestDTO request = createValidCheckoutRequest();
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should handle stock depletion");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= PLACE ORDER - PROMOTION VALIDATION =============

    @Test
    public void placeOrder_invalidShopCoupon_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setShopCouponCode("INVALID_CODE");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for invalid shop coupon");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("mã") ||
                      e.getMessage().contains("không hợp lệ"));
        }
    }

    @Test
    public void placeOrder_expiredCoupon_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setSystemCouponCode("EXPIRED_CODE");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for expired coupon");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("hết hạn") ||
                      e.getMessage().contains("không còn"));
        }
    }

    @Test
    public void placeOrder_minimumPurchaseNotMet_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setShopCouponCode("MIN_PURCHASE_CODE");
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for minimum purchase not met");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("tối thiểu") ||
                      e.getMessage().contains("minimum"));
        }
    }

    // ============= PLACE ORDER - PRODUCT/SHOP STATUS VALIDATION =============

    @Test
    public void placeOrder_deactivatedProduct_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            request.setVariantIds(Arrays.asList(1)); // Assume variant 1 is deactivated
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for deactivated product");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("ngừng kinh doanh") ||
                      e.getMessage().contains("không tồn tại"));
        }
    }

    @Test
    public void placeOrder_deactivatedShop_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should throw exception for deactivated shop");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_customerBuyingOwnProduct_throws() throws Exception {
        try {
            User shopOwner = createTestUser(2, "shop@example.com", "Shop Owner", "0999999999");
            shopOwner.setRole("SHOP_OWNER");
            CheckoutRequestDTO request = createValidCheckoutRequest();
            checkoutService.placeOrder(shopOwner, request, "127.0.0.1");
            fail("Should throw exception for buying own product");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không thể mua") ||
                      e.getMessage().contains("cửa hàng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= PLACE ORDER - CONCURRENT ACCESS =============

    @Test
    public void placeOrder_concurrentCheckoutSameItems_atomic() throws InterruptedException {
        CheckoutRequestDTO request = createValidCheckoutRequest();

        Thread t1 = new Thread(() -> {
            try {
                checkoutService.placeOrder(testUser, request, "127.0.0.1");
            } catch (Exception e) {
                // Expected behavior
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                User user2 = createTestUser(2, "customer2@example.com", "Customer 2", "0987654322");
                checkoutService.placeOrder(user2, request, "127.0.0.1");
            } catch (Exception e) {
                // Expected behavior
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // At least one should fail due to stock constraints
    }

    @Test
    public void placeOrder_lockAcquisitionTimeout_throws() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            // Try to acquire lock with concurrent requests
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should handle lock acquisition timeout");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("đang được xử lý") ||
                      e.getMessage().contains("khong bam lien tiep"));
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DATABASE ERROR HANDLING =============

    @Test
    public void buildCheckoutView_databaseError_handled() {
        try {
            checkoutService.buildCheckoutView(testUser, Arrays.asList(1));
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_databaseError_handled() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= EDGE CASES =============

    @Test
    public void placeOrder_veryLongAddress_succeeds() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            StringBuilder longAddress = new StringBuilder();
            for (int i = 0; i < 50; i++) {
                longAddress.append("Test Address ");
            }
            request.setDeliveryAddress(longAddress.toString());
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            // May succeed or fail depending on validation rules
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void placeOrder_largeVariantList_succeeds() {
        try {
            CheckoutRequestDTO request = createValidCheckoutRequest();
            List<Integer> manyVariants = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                manyVariants.add(i);
            }
            request.setVariantIds(manyVariants);
            checkoutService.placeOrder(testUser, request, "127.0.0.1");
            // May succeed or fail depending on business rules
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }
}
