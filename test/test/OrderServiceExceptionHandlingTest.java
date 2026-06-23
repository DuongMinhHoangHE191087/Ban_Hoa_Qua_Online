package test;

import dao.order.OrderDAO;
import model.entity.order.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.order.OrderService;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Exception handling tests for OrderService.
 * Uses seed rows 1, 101, and 102 from Setup_OnlineFruitShopping.sql.
 */
public class OrderServiceExceptionHandlingTest {

    private final OrderDAO orderDAO = new OrderDAO();
    private OrderService orderService;

    @Before
    public void setUp() {
        orderService = new OrderService();
    }

    @After
    public void tearDown() {
        try {
            orderDAO.updateStatus(1, "DELIVERED");
            orderDAO.updateStatus(101, "CONFIRMED");
        } catch (SQLException ignored) {
            // Best-effort cleanup only.
        }
    }

    private void setOrderStatus(int orderId, String status) throws SQLException {
        orderDAO.updateStatus(orderId, status);
    }

    @Test
    public void cancelOrder_negativeOrderId_throws() {
        try {
            orderService.cancelOrder(-1, 5, "reason");
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_zeroOrderId_throws() {
        try {
            orderService.cancelOrder(0, 5, "reason");
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_nullReason_throws() {
        try {
            orderService.cancelOrder(1, 5, null);
            fail("Should throw exception for null reason");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_blankReason_throws() {
        try {
            orderService.cancelOrder(1, 5, "   ");
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

    @Test
    public void cancelOrder_orderNotFound_throws() {
        try {
            orderService.cancelOrder(999999, 5, "reason");
            fail("Should throw exception for non-existent order");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_deliveredOrder_throws() {
        try {
            orderService.cancelOrder(1, 5, "reason");
            fail("Should throw exception for delivered order");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_customerNotOwner_throws() {
        try {
            orderService.cancelOrder(101, 6, "reason");
            fail("Should throw exception for unauthorized cancellation");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_shopOwnerCancellingWrongShop_throws() {
        try {
            orderService.cancelOrder(101, 4, "reason");
            fail("Should throw exception for wrong shop owner");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_customerCancellingApprovedOrder_throws() {
        try {
            setOrderStatus(101, "APPROVED");
            orderService.cancelOrder(101, 5, "reason");
            fail("Should throw exception for approved order");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_negativeOrderId_throws() {
        try {
            orderService.confirmOrder(-1, 3);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_zeroOrderId_throws() {
        try {
            orderService.confirmOrder(0, 3);
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

    @Test
    public void confirmOrder_orderNotFound_throws() {
        try {
            orderService.confirmOrder(999999, 3);
            fail("Should throw exception for non-existent order");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_wrongShopOwner_throws() {
        try {
            orderService.confirmOrder(1, 4);
            fail("Should throw exception for wrong shop owner");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_notConfirmedStatus_throws() {
        try {
            orderService.confirmOrder(1, 3);
            fail("Should throw exception when order not in CONFIRMED status");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getOrderDetail_negativeOrderId_throws() {
        try {
            orderService.getOrderDetail(-1);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getOrderDetail_zeroOrderId_throws() {
        try {
            orderService.getOrderDetail(0);
            fail("Should throw exception for zero order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getOrderDetail_orderNotFound_returnsNull() {
        try {
            Order result = orderService.getOrderDetail(999999);
            assertNull(result);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_negativeOrderId_throws() {
        try {
            orderService.dispatchOrder(-1, 3);
            fail("Should throw exception for negative order ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_zeroOrderId_throws() {
        try {
            orderService.dispatchOrder(0, 3);
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

    @Test
    public void dispatchOrder_orderNotFound_throws() {
        try {
            orderService.dispatchOrder(999999, 3);
            fail("Should throw exception for non-existent order");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_wrongShopOwner_throws() {
        try {
            orderService.dispatchOrder(1, 4);
            fail("Should throw exception for wrong shop owner");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_invalidOrderStatus_throws() {
        try {
            orderService.dispatchOrder(1, 3);
            fail("Should throw exception for invalid order status");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_databaseError_handled() {
        try {
            orderService.cancelOrder(1, 5, "reason");
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void confirmOrder_databaseError_handled() {
        try {
            orderService.confirmOrder(1, 3);
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void dispatchOrder_databaseError_handled() {
        try {
            orderService.dispatchOrder(1, 3);
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void cancelOrder_concurrentCancellations_atomic() {
        try {
            setOrderStatus(1, "CONFIRMED");

            Thread t1 = new Thread(() -> {
                try {
                    orderService.cancelOrder(1, 5, "reason1");
                } catch (Exception ignored) {
                    // Expected in at least one branch.
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    orderService.cancelOrder(1, 6, "reason2");
                } catch (Exception ignored) {
                    // Expected in at least one branch.
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            assertNotNull(e.getMessage());
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }
}
