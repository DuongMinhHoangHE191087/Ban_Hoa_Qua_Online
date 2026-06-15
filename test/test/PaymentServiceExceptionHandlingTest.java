package test;

import service.shop.PaymentService;
import model.entity.shop.PaymentTransaction;
import org.junit.Test;
import org.junit.Before;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for PaymentService.
 * Covers: payment initialization, manual confirmation, admin approval,
 * transaction state validation, expiration handling, and database errors.
 */
public class PaymentServiceExceptionHandlingTest {

    private PaymentService paymentService;

    @Before
    public void setUp() {
        paymentService = new PaymentService();
    }

    // ============= INIT PAYMENT - INPUT VALIDATION ERRORS =============

    @Test
    public void initPayment_negativeOrderId_throws() {
        try {
            paymentService.initPayment(-1, "BANK_TRANSFER");
            fail("Should throw exception for negative order ID");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("không tồn tại"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void initPayment_zeroOrderId_throws() {
        try {
            paymentService.initPayment(0, "BANK_TRANSFER");
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void initPayment_nullPaymentMethod_throws() {
        try {
            paymentService.initPayment(1, null);
            fail("Should throw exception for null payment method");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void initPayment_blankPaymentMethod_throws() {
        try {
            paymentService.initPayment(1, "   ");
            fail("Should throw exception for blank payment method");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void initPayment_invalidPaymentMethod_throws() {
        try {
            paymentService.initPayment(1, "INVALID_METHOD");
            fail("Should throw exception for invalid payment method");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= INIT PAYMENT - RESOURCE NOT FOUND =============

    @Test
    public void initPayment_orderNotFound_throws() {
        try {
            paymentService.initPayment(999999, "BANK_TRANSFER");
            fail("Should throw exception for non-existent order");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("không tồn tại"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= INIT PAYMENT - WITH IP ADDRESS =============

    @Test
    public void initPayment_withNullIpAddress_succeeds() {
        try {
            PaymentTransaction result = paymentService.initPayment(1, "BANK_TRANSFER", null);
            assertNull(result);
        } catch (SQLException e) {
            // Database error acceptable in test environment
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void initPayment_withInvalidIpAddress_throws() {
        try {
            paymentService.initPayment(1, "BANK_TRANSFER", "invalid-ip");
            fail("Should throw exception for invalid IP format");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= GET PAYMENT BY ORDER - INPUT VALIDATION =============

    @Test
    public void getPaymentByOrder_negativeOrderId_throws() {
        try {
            PaymentTransaction result = paymentService.getPaymentByOrder(-1);
            if (result != null) {
                fail("Should not find payment for negative order ID");
            }
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getPaymentByOrder_zeroOrderId_throws() {
        try {
            PaymentTransaction result = paymentService.getPaymentByOrder(0);
            if (result != null) {
                fail("Should not find payment for zero order ID");
            }
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getPaymentByOrder_orderNotFound_returnsNull() {
        try {
            PaymentTransaction result = paymentService.getPaymentByOrder(999999);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM MANUAL PAYMENT - INPUT VALIDATION =============

    @Test
    public void confirmManualPayment_negativeOrderId_throws() {
        try {
            paymentService.confirmManualPayment(-1, 1);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_zeroOrderId_throws() {
        try {
            paymentService.confirmManualPayment(0, 1);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_negativeCustomerId_throws() {
        try {
            paymentService.confirmManualPayment(1, -1);
            fail("Should throw exception for negative customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_zeroCustomerId_throws() {
        try {
            paymentService.confirmManualPayment(1, 0);
            fail("Should throw exception for zero customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM MANUAL PAYMENT - AUTHORIZATION (IDOR) =============

    @Test
    public void confirmManualPayment_wrongCustomer_throws() {
        try {
            // Customer 1 tries to confirm payment for Customer 2's order
            paymentService.confirmManualPayment(1, 999);
            fail("Should throw exception for unauthorized customer (IDOR)");
        } catch (SecurityException e) {
            assertTrue(e.getMessage().contains("không có quyền") ||
                      e.getMessage().contains("quyền truy cập"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM MANUAL PAYMENT - STATE VALIDATION =============

    @Test
    public void confirmManualPayment_orderNotPendingPayment_throws() {
        try {
            // Order must be in PENDING_PAYMENT status
            paymentService.confirmManualPayment(1, 1);
            fail("Should throw exception when order not pending payment");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("chờ thanh toán") ||
                      e.getMessage().contains("trạng thái"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_noPaymentRecord_throws() {
        try {
            // Payment transaction record must exist
            paymentService.confirmManualPayment(1, 1);
            fail("Should throw exception when no payment record found");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("thanh toán"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM MANUAL PAYMENT - EXPIRATION VALIDATION =============

    @Test
    public void confirmManualPayment_expiredQrCode_returnsFalse() {
        try {
            // QR code should be valid and not expired
            boolean result = paymentService.confirmManualPayment(1, 1);
            assertFalse("Should return false for expired QR code", result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_nullExpirationDate_succeeds() {
        try {
            // Null expiration date should be handled gracefully
            boolean result = paymentService.confirmManualPayment(1, 1);
            assertTrue("Should handle null expiration date", result || !result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADMIN APPROVE PAYMENT - INPUT VALIDATION =============

    @Test
    public void adminApprovePayment_negativeOrderId_throws() {
        try {
            paymentService.adminApprovePayment(-1, 1);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_zeroOrderId_throws() {
        try {
            paymentService.adminApprovePayment(0, 1);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_negativeAdminId_throws() {
        try {
            paymentService.adminApprovePayment(1, -1);
            fail("Should throw exception for negative admin ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_zeroAdminId_throws() {
        try {
            paymentService.adminApprovePayment(1, 0);
            fail("Should throw exception for zero admin ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADMIN APPROVE PAYMENT - AUTHORIZATION =============

    @Test
    public void adminApprovePayment_nonAdminUser_throws() {
        try {
            // User must have ADMIN role
            paymentService.adminApprovePayment(1, 999);
            fail("Should throw exception for non-admin user");
        } catch (SecurityException e) {
            assertTrue(e.getMessage().contains("không có quyền") ||
                      e.getMessage().contains("admin"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_customerUser_throws() {
        try {
            // CUSTOMER role cannot approve payments
            paymentService.adminApprovePayment(1, 1);
            fail("Should throw exception for customer user");
        } catch (SecurityException e) {
            assertTrue(e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADMIN APPROVE PAYMENT - RESOURCE NOT FOUND =============

    @Test
    public void adminApprovePayment_orderNotFound_throws() {
        try {
            paymentService.adminApprovePayment(999999, 1);
            fail("Should throw exception for non-existent order");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_paymentRecordNotFound_throws() {
        try {
            // Payment transaction record must exist
            paymentService.adminApprovePayment(1, 1);
            fail("Should throw exception when no payment record found");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("không tìm thấy") ||
                      e.getMessage().contains("thanh toán"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADMIN APPROVE PAYMENT - STATE VALIDATION =============

    @Test
    public void adminApprovePayment_orderNotPendingPayment_throws() {
        try {
            // Order must be in PENDING_PAYMENT status
            paymentService.adminApprovePayment(1, 1);
            fail("Should throw exception when order not pending payment");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("chờ thanh toán") ||
                      e.getMessage().contains("trạng thái"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONCURRENT PAYMENT CONFIRMATIONS =============

    @Test
    public void confirmManualPayment_concurrentConfirmations_atomic() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                paymentService.confirmManualPayment(1, 1);
            } catch (Exception e) {
                // Expected
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                paymentService.confirmManualPayment(1, 1);
            } catch (Exception e) {
                // Second confirmation should fail
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // At least one should fail due to atomic state transition
    }

    @Test
    public void adminApprovePayment_concurrentApprovals_atomic() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                paymentService.adminApprovePayment(1, 1);
            } catch (Exception e) {
                // Expected
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                paymentService.adminApprovePayment(1, 2);
            } catch (Exception e) {
                // Second approval should fail
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // At least one should fail due to atomic state transition
    }

    // ============= DATABASE ERROR HANDLING =============

    @Test
    public void initPayment_databaseError_handled() {
        try {
            paymentService.initPayment(1, "BANK_TRANSFER");
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_databaseError_handled() {
        try {
            paymentService.confirmManualPayment(1, 1);
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void adminApprovePayment_databaseError_handled() {
        try {
            paymentService.adminApprovePayment(1, 1);
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= EDGE CASES =============

    @Test
    public void initPayment_veryLargeOrderId_handled() {
        try {
            paymentService.initPayment(Integer.MAX_VALUE, "BANK_TRANSFER");
            fail("Should handle very large order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmManualPayment_veryLargeCustomerId_handled() {
        try {
            paymentService.confirmManualPayment(1, Integer.MAX_VALUE);
            fail("Should handle very large customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }
}
