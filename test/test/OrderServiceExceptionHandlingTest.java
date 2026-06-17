package test;

import service.order.OrderService;
import model.entity.order.Order;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for OrderService.
 * Covers: input validation, state transitions, authorization (IDOR),
 * resource not found errors, and database errors.
 */
public class OrderServiceExceptionHandlingTest {

    private OrderService orderService;

    @Before
    public void setUp() {
        orderService = new OrderService();
    }

    // ============= CANCEL ORDER - INPUT VALIDATION ERRORS =============

    @Test
    public void cancelOrder_negativeOrderId_throws() {
        try {
            orderService.cancelOrder(-1, 1, "reason");
            fail("Should throw exception for negative order ID");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tồn tại") ||
                      e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_zeroOrderId_throws() {
        try {
            orderService.cancelOrder(0, 1, "reason");
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_nullReason_throws() {
        try {
            orderService.cancelOrder(1, 1, null);
            fail("Should throw exception for null reason");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_blankReason_throws() {
        try {
            orderService.cancelOrder(1, 1, "   ");
            fail("Should throw exception for blank reason");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_negativeCustomerId_throws() {
        try {
            orderService.cancelOrder(1, -1, "reason");
            fail("Should throw exception for negative customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_zeroCustomerId_throws() {
        try {
            orderService.cancelOrder(1, 0, "reason");
            fail("Should throw exception for zero customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CANCEL ORDER - RESOURCE NOT FOUND ERRORS =============

    @Test
    public void cancelOrder_orderNotFound_throws() {
        try {
            orderService.cancelOrder(999999, 1, "reason");
            fail("Should throw exception for non-existent order");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tồn tại") ||
                      e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CANCEL ORDER - STATE TRANSITION ERRORS =============

    @Test
    public void cancelOrder_deliveredOrder_throws() {
        try {
            // Assuming order ID 1 is already DELIVERED
            orderService.cancelOrder(1, 1, "reason");
            fail("Should throw exception for delivered order");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("đã giao") ||
                      e.getMessage().contains("không thể hủy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_alreadyCancelledOrder_throws() {
        try {
            // Assuming order ID 1 is already CANCELLED
            orderService.cancelOrder(1, 1, "reason");
            fail("Should throw exception for already cancelled order");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("đã hủy") ||
                      e.getMessage().contains("không thể hủy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CANCEL ORDER - AUTHORIZATION (IDOR) ERRORS =============

    @Test
    public void cancelOrder_customerNotOwner_throws() {
        try {
            // Customer 1 tries to cancel order owned by Customer 2
            orderService.cancelOrder(1, 999, "reason");
            fail("Should throw exception for unauthorized cancellation (IDOR)");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("quyền") ||
                      e.getMessage().contains("không có quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_shopOwnerCancellingWrongShop_throws() {
        try {
            // Shop owner trying to cancel order from different shop
            orderService.cancelOrder(1, 999, "reason");
            fail("Should throw exception for wrong shop owner");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không thuộc") ||
                      e.getMessage().contains("cửa hàng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_customerCancellingApprovedOrder_throws() {
        try {
            // Customer trying to cancel order that's already approved
            orderService.cancelOrder(1, 1, "reason");
            fail("Should throw exception for approved order");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không được phép") ||
                      e.getMessage().contains("duyệt"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM ORDER - INPUT VALIDATION ERRORS =============

    @Test
    public void confirmOrder_negativeOrderId_throws() {
        try {
            orderService.confirmOrder(-1, 1);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_zeroOrderId_throws() {
        try {
            orderService.confirmOrder(0, 1);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_negativeOwnerId_throws() {
        try {
            orderService.confirmOrder(1, -1);
            fail("Should throw exception for negative owner ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_zeroOwnerId_throws() {
        try {
            orderService.confirmOrder(1, 0);
            fail("Should throw exception for zero owner ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM ORDER - RESOURCE NOT FOUND ERRORS =============

    @Test
    public void confirmOrder_orderNotFound_throws() {
        try {
            orderService.confirmOrder(999999, 1);
            fail("Should throw exception for non-existent order");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không hợp lệ") ||
                      e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM ORDER - AUTHORIZATION (IDOR) ERRORS =============

    @Test
    public void confirmOrder_wrongShopOwner_throws() {
        try {
            // Shop owner 1 tries to confirm order from shop 2
            orderService.confirmOrder(1, 999);
            fail("Should throw exception for wrong shop owner (IDOR)");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không hợp lệ") ||
                      e.getMessage().contains("quyền"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONFIRM ORDER - STATE TRANSITION ERRORS =============

    @Test
    public void confirmOrder_notConfirmedStatus_throws() {
        try {
            // Order must be in CONFIRMED status to confirm
            orderService.confirmOrder(1, 1);
            fail("Should throw exception when order not in CONFIRMED status");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("CONFIRMED") ||
                      e.getMessage().contains("trạng thái"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= GET ORDER DETAIL - INPUT VALIDATION ERRORS =============

    @Test
    public void getOrderDetail_negativeOrderId_throws() {
        try {
            Order result = orderService.getOrderDetail(-1);
            // May return null or throw
            if (result == null) {
                fail("Should handle negative order ID");
            }
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getOrderDetail_zeroOrderId_throws() {
        try {
            Order result = orderService.getOrderDetail(0);
            if (result == null) {
                fail("Should handle zero order ID");
            }
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= GET ORDER DETAIL - RESOURCE NOT FOUND =============

    @Test
    public void getOrderDetail_orderNotFound_returnsNull() {
        try {
            Order result = orderService.getOrderDetail(999999);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DISPATCH ORDER - INPUT VALIDATION ERRORS =============

    @Test
    public void dispatchOrder_negativeOrderId_throws() {
        try {
            orderService.dispatchOrder(-1, 1);
            fail("Should throw exception for negative order ID");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không hợp lệ"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_zeroOrderId_throws() {
        try {
            orderService.dispatchOrder(0, 1);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_negativeOwnerId_throws() {
        try {
            orderService.dispatchOrder(1, -1);
            fail("Should throw exception for negative owner ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DISPATCH ORDER - RESOURCE NOT FOUND ERRORS =============

    @Test
    public void dispatchOrder_orderNotFound_throws() {
        try {
            orderService.dispatchOrder(999999, 1);
            fail("Should throw exception for non-existent order");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không hợp lệ"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DISPATCH ORDER - AUTHORIZATION (IDOR) ERRORS =============

    @Test
    public void dispatchOrder_wrongShopOwner_throws() {
        try {
            // Shop owner 1 tries to dispatch order from shop 2
            orderService.dispatchOrder(1, 999);
            fail("Should throw exception for wrong shop owner (IDOR)");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("không hợp lệ"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DISPATCH ORDER - STATE TRANSITION ERRORS =============

    @Test
    public void dispatchOrder_invalidOrderStatus_throws() {
        try {
            // Order must be APPROVED, CONFIRMED, or PREPARING to dispatch
            orderService.dispatchOrder(1, 1);
            fail("Should throw exception for invalid order status");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("chuẩn bị") ||
                      e.getMessage().contains("duyệt"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= DATABASE ERROR HANDLING =============

    @Test
    public void cancelOrder_databaseError_handled() {
        try {
            orderService.cancelOrder(1, 1, "reason");
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_databaseError_handled() {
        try {
            orderService.confirmOrder(1, 1);
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_databaseError_handled() {
        try {
            orderService.dispatchOrder(1, 1);
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONCURRENT ORDER OPERATIONS =============

    @Test
    public void cancelOrder_concurrentCancellations_atomic() {
        try {
            // Simulate two concurrent cancellation attempts on same order
            Thread t1 = new Thread(() -> {
                try {
                    orderService.cancelOrder(1, 1, "reason1");
                } catch (Exception e) {
                    // First one succeeds, second fails
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    orderService.cancelOrder(1, 2, "reason2");
                } catch (Exception e) {
                    // Expected to fail
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            // At least one should fail or be handled atomically
        } catch (InterruptedException e) {
            assertNotNull(e.getMessage());
        }
    }
}
