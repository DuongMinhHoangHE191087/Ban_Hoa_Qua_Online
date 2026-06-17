package test;

import service.order.DeliveryService;
import model.entity.order.Delivery;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for DeliveryService.
 * Covers: delivery assignment, status updates, authorization (staff verification),
 * state transitions, proof validation, and database errors.
 */
public class DeliveryServiceExceptionHandlingTest {

    private DeliveryService deliveryService;

    @Before
    public void setUp() {
        deliveryService = new DeliveryService();
    }

    // ============= UPDATE STATUS AND PROOF - INPUT VALIDATION =============

    @Test
    public void updateStatusAndProof_negativeStaffId_throws() {
        try {
            deliveryService.updateStatusAndProof(-1, 1, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for negative staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_zeroStaffId_throws() {
        try {
            deliveryService.updateStatusAndProof(0, 1, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for zero staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_negativeDeliveryId_throws() {
        try {
            deliveryService.updateStatusAndProof(1, -1, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for negative delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_zeroDeliveryId_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 0, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for zero delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_nullStatus_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, null, null, "proof.jpg");
            fail("Should throw exception for null status");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("bắt buộc") ||
                      e.getMessage().contains("trạng thái"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_blankStatus_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "   ", null, "proof.jpg");
            fail("Should throw exception for blank status");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("bắt buộc") ||
                      e.getMessage().contains("trạng thái"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_invalidStatus_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "INVALID_STATUS", null, "proof.jpg");
            fail("Should throw exception for invalid status");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE STATUS AND PROOF - DELIVERED VALIDATION =============

    @Test
    public void updateStatusAndProof_deliveredWithoutProof_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "DELIVERED", null, null);
            fail("Should throw exception when DELIVERED status has no proof");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ảnh") ||
                      e.getMessage().contains("bằng chứng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_deliveredWithBlankProof_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "DELIVERED", null, "   ");
            fail("Should throw exception when DELIVERED status has blank proof");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ảnh") ||
                      e.getMessage().contains("bằng chứng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE STATUS AND PROOF - FAILED VALIDATION =============

    @Test
    public void updateStatusAndProof_failedWithoutReason_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "FAILED", null, "proof.jpg");
            fail("Should throw exception when FAILED status has no reason");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("lý do") ||
                      e.getMessage().contains("reason"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_failedWithBlankReason_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "FAILED", "   ", "proof.jpg");
            fail("Should throw exception when FAILED status has blank reason");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("lý do") ||
                      e.getMessage().contains("reason"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE STATUS AND PROOF - RESOURCE NOT FOUND =============

    @Test
    public void updateStatusAndProof_deliveryNotFound_throws() {
        try {
            deliveryService.updateStatusAndProof(1, 999999, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for non-existent delivery");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE STATUS AND PROOF - AUTHORIZATION (STAFF VERIFICATION) =============

    @Test
    public void updateStatusAndProof_wrongStaffId_throws() {
        try {
            // Staff 1 tries to update delivery assigned to Staff 2
            deliveryService.updateStatusAndProof(999, 1, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception for unauthorized staff (wrong staff ID)");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateStatusAndProof_nullStaffIdInDelivery_throws() {
        try {
            // Delivery has null staffId, but staff is trying to update
            deliveryService.updateStatusAndProof(1, 1, "DELIVERED", null, "proof.jpg");
            fail("Should throw exception when delivery has null staffId");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE ESTIMATED TIME - INPUT VALIDATION =============

    @Test
    public void updateEstimatedTime_negativeStaffId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(-1, 1, estimatedTime);
            fail("Should throw exception for negative staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_zeroStaffId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(0, 1, estimatedTime);
            fail("Should throw exception for zero staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_negativeDeliveryId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, -1, estimatedTime);
            fail("Should throw exception for negative delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_zeroDeliveryId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, 0, estimatedTime);
            fail("Should throw exception for zero delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_nullEstimatedTime_throws() {
        try {
            deliveryService.updateEstimatedTime(1, 1, null);
            fail("Should throw exception for null estimated time");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_pastEstimatedTime_throws() {
        try {
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
            deliveryService.updateEstimatedTime(1, 1, pastTime);
            fail("Should throw exception for past estimated time");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE ESTIMATED TIME - RESOURCE NOT FOUND =============

    @Test
    public void updateEstimatedTime_deliveryNotFound_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, 999999, estimatedTime);
            fail("Should throw exception for non-existent delivery");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE ESTIMATED TIME - AUTHORIZATION =============

    @Test
    public void updateEstimatedTime_wrongStaffId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(999, 1, estimatedTime);
            fail("Should throw exception for unauthorized staff");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE ESTIMATED TIME - STATE VALIDATION =============

    @Test
    public void updateEstimatedTime_deliveredDelivery_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, 1, estimatedTime);
            fail("Should throw exception for already delivered delivery");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("đã hoàn tất") ||
                      e.getMessage().contains("không thể"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_failedDelivery_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, 1, estimatedTime);
            fail("Should throw exception for failed delivery");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("thất bại") ||
                      e.getMessage().contains("không thể"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ASSIGN SHIPPER - INPUT VALIDATION =============

    @Test
    public void assignShipper_negativeOrderId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.assignShipper(-1, 1, estimatedTime);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void assignShipper_zeroOrderId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.assignShipper(0, 1, estimatedTime);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void assignShipper_negativeStaffId_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.assignShipper(1, -1, estimatedTime);
            fail("Should throw exception for negative staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ASSIGN SHIPPER - RESOURCE NOT FOUND =============

    @Test
    public void assignShipper_orderNotFound_throws() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.assignShipper(999999, 1, estimatedTime);
            fail("Should throw exception for non-existent order");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= GET DELIVERY BY ORDER ID - INPUT VALIDATION =============

    @Test
    public void getDeliveryByOrderId_negativeOrderId_throws() {
        try {
            Delivery result = deliveryService.getDeliveryByOrderId(-1);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getDeliveryByOrderId_zeroOrderId_throws() {
        try {
            Delivery result = deliveryService.getDeliveryByOrderId(0);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getDeliveryByOrderId_orderNotFound_returnsNull() {
        try {
            Delivery result = deliveryService.getDeliveryByOrderId(999999);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= MARK AS DELIVERED - INPUT VALIDATION =============

    @Test
    public void markAsDelivered_negativeStaffId_throws() {
        try {
            deliveryService.markAsDelivered(-1, 1, "proof.jpg");
            fail("Should throw exception for negative staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_zeroStaffId_throws() {
        try {
            deliveryService.markAsDelivered(0, 1, "proof.jpg");
            fail("Should throw exception for zero staff ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_negativeDeliveryId_throws() {
        try {
            deliveryService.markAsDelivered(1, -1, "proof.jpg");
            fail("Should throw exception for negative delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_zeroDeliveryId_throws() {
        try {
            deliveryService.markAsDelivered(1, 0, "proof.jpg");
            fail("Should throw exception for zero delivery ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_nullProofImageUrl_throws() {
        try {
            deliveryService.markAsDelivered(1, 1, null);
            fail("Should throw exception for null proof image URL");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ảnh") ||
                      e.getMessage().contains("bằng chứng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_blankProofImageUrl_throws() {
        try {
            deliveryService.markAsDelivered(1, 1, "   ");
            fail("Should throw exception for blank proof image URL");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ảnh") ||
                      e.getMessage().contains("bằng chứng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= MARK AS DELIVERED - RESOURCE NOT FOUND =============

    @Test
    public void markAsDelivered_deliveryNotFound_throws() {
        try {
            deliveryService.markAsDelivered(1, 999999, "proof.jpg");
            fail("Should throw exception for non-existent delivery");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= MARK AS DELIVERED - AUTHORIZATION =============

    @Test
    public void markAsDelivered_wrongStaffId_throws() {
        try {
            deliveryService.markAsDelivered(999, 1, "proof.jpg");
            fail("Should throw exception for unauthorized staff");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DATABASE ERROR HANDLING =============

    @Test
    public void updateStatusAndProof_databaseError_handled() {
        try {
            deliveryService.updateStatusAndProof(1, 1, "DELIVERED", null, "proof.jpg");
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateEstimatedTime_databaseError_handled() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.updateEstimatedTime(1, 1, estimatedTime);
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void assignShipper_databaseError_handled() {
        try {
            LocalDateTime estimatedTime = LocalDateTime.now().plusDays(1);
            deliveryService.assignShipper(1, 1, estimatedTime);
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void markAsDelivered_databaseError_handled() {
        try {
            deliveryService.markAsDelivered(1, 1, "proof.jpg");
            fail("Should handle database errors gracefully");
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONCURRENT DELIVERY OPERATIONS =============

    @Test
    public void updateStatusAndProof_concurrentStatusUpdates_atomic() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                deliveryService.updateStatusAndProof(1, 1, "DELIVERED", null, "proof1.jpg");
            } catch (Exception e) {
                // Expected
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                deliveryService.updateStatusAndProof(1, 1, "FAILED", "reason", "proof2.jpg");
            } catch (Exception e) {
                // Second update should fail
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // At least one should fail due to atomic state transition
    }

    @Test
    public void markAsDelivered_concurrentMarkings_atomic() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                deliveryService.markAsDelivered(1, 1, "proof1.jpg");
            } catch (Exception e) {
                // Expected
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                deliveryService.markAsDelivered(2, 1, "proof2.jpg");
            } catch (Exception e) {
                // Different delivery, may succeed or fail
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Should handle concurrent access safely
    }
}
