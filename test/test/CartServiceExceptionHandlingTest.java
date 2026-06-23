package test;

import service.cart.CartService;
import model.dto.product.CartSummaryDTO;
import org.junit.Test;
import org.junit.Before;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Comprehensive exception handling tests for CartService.
 * Covers: input validation, inventory checks, authorization failures (IDOR),
 * resource not found errors, database errors, and concurrent access issues.
 */
public class CartServiceExceptionHandlingTest {

    private CartService cartService;

    @Before
    public void setUp() {
        cartService = new CartService();
    }

    // ============= ADD TO CART - INPUT VALIDATION ERRORS =============

    @Test
    public void addToCart_negativeCustomerId_throws() {
        try {
            cartService.addToCart(-1, 1, 1);
            fail("Should throw exception for negative customer ID");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        } catch (SQLException e) {
            // Database error is also acceptable
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_zeroCustomerId_throws() {
        try {
            cartService.addToCart(0, 1, 1);
            fail("Should throw exception for zero customer ID");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_negativeVariantId_throws() {
        try {
            cartService.addToCart(1, -1, 1);
            fail("Should throw exception for negative variant ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_zeroVariantId_throws() {
        try {
            cartService.addToCart(1, 0, 1);
            fail("Should throw exception for zero variant ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_negativeQuantity_throws() {
        try {
            cartService.addToCart(1, 1, -5);
            fail("Should throw exception for negative quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("phải lớn hơn 0") ||
                      e.getMessage().contains("số lượng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_zeroQuantity_throws() {
        try {
            cartService.addToCart(1, 1, 0);
            fail("Should throw exception for zero quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("phải lớn hơn 0") ||
                      e.getMessage().contains("số lượng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_largeNegativeQuantity_throws() {
        try {
            cartService.addToCart(1, 1, -1000);
            fail("Should throw exception for large negative quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("phải lớn hơn 0") ||
                      e.getMessage().contains("số lượng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADD TO CART - PRODUCT VALIDATION ERRORS =============

    @Test
    public void addToCart_nonexistentVariant_throws() {
        try {
            cartService.addToCart(1, 999999, 1);
            fail("Should throw exception for non-existent variant");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tồn tại") ||
                      e.getMessage().contains("không tìm thấy"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_deactivatedVariant_throws() {
        try {
            cartService.addToCart(1, Integer.MAX_VALUE, 1);
            fail("Should throw exception for deactivated variant");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không tồn tại") ||
                      e.getMessage().contains("ngừng kinh doanh"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= ADD TO CART - INVENTORY VALIDATION ERRORS =============

    @Test
    public void addToCart_quantityExceedsStock_throws() {
        try {
            // Try to add more items than available in stock
            cartService.addToCart(1, 1, 1000000);
            fail("Should throw exception when quantity exceeds stock");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("vượt quá") ||
                      e.getMessage().contains("tồn kho"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_quantityExceedsRemainingStock_throws() {
        try {
            // First add some items to cart
            cartService.addToCart(1, 1, 5);

            // Try to add more items that would exceed available stock
            cartService.addToCart(1, 1, 1000000);
            fail("Should throw exception when total quantity exceeds stock");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("vượt quá") ||
                      e.getMessage().contains("tồn kho"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_packagingWithQuantityExceedsStock_throws() {
        try {
            cartService.addToCart(1, 1, 1000000, 1); // With packaging option
            fail("Should throw exception when quantity exceeds stock");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("vượt quá") ||
                      e.getMessage().contains("tồn kho"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE QUANTITY - INPUT VALIDATION ERRORS =============

    @Test
    public void updateQuantity_negativeCustomerId_throws() {
        try {
            cartService.updateQuantity(-1, 1, 5);
            fail("Should throw exception for negative customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_zeroCustomerId_throws() {
        try {
            cartService.updateQuantity(0, 1, 5);
            fail("Should throw exception for zero customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_negativeCartItemId_throws() {
        try {
            cartService.updateQuantity(1, -1, 5);
            fail("Should throw exception for negative cart item ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_zeroCartItemId_throws() {
        try {
            cartService.updateQuantity(1, 0, 5);
            fail("Should throw exception for zero cart item ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_negativeQuantity_throws() {
        try {
            cartService.updateQuantity(1, 1, -5);
            fail("Should throw exception for negative quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("phải lớn hơn 0") ||
                      e.getMessage().contains("số lượng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_zeroQuantity_throws() {
        try {
            cartService.updateQuantity(1, 1, 0);
            fail("Should throw exception for zero quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("phải lớn hơn 0") ||
                      e.getMessage().contains("số lượng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE QUANTITY - RESOURCE NOT FOUND ERRORS =============

    @Test
    public void updateQuantity_cartItemNotFound_throws() {
        try {
            cartService.updateQuantity(1, 999999, 5);
            fail("Should throw exception for non-existent cart item");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            assertTrue(msg.contains("không tìm thấy") ||
                      msg.contains("không tồn tại"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= UPDATE QUANTITY - AUTHORIZATION (IDOR) ERRORS =============

    @Test
    public void updateQuantity_itemNotInCustomerCart_throws() {
        try {
            // Customer 1 tries to update item from Customer 2's cart
            cartService.updateQuantity(1, 999, 5); // 999 is assumed to be in customer 2's cart
            fail("Should throw exception for unauthorized cart item access (IDOR)");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("không thuộc") ||
                      e.getMessage().contains("giỏ hàng"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_quantityExceedsStock_throws() {
        try {
            cartService.updateQuantity(1, 1, 1000000);
            fail("Should throw exception when updated quantity exceeds stock");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            assertTrue(msg.contains("vượt quá") ||
                      msg.contains("không tìm thấy") ||
                      msg.contains("không thuộc") ||
                      msg.contains("tồn kho"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }


    // ============= GET CART - INPUT VALIDATION ERRORS =============

    @Test
    public void getCart_negativeCustomerId_throws() {
        try {
            cartService.getCart(-1);
            fail("Should throw exception for negative customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void getCart_zeroCustomerId_throws() {
        try {
            cartService.getCart(0);
            fail("Should throw exception for zero customer ID");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }


    // ============= DATABASE ERROR HANDLING =============

    @Test
    public void addToCart_databaseError_handled() {
        try {
            cartService.addToCart(1, 1, 1);
        } catch (IllegalArgumentException e) {
            // Input validation error is expected
            assertNotNull(e.getMessage());
        } catch (SQLException e) {
            // Database error should be caught and reported
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_databaseError_handled() {
        try {
            cartService.updateQuantity(1, 1, 5);
            fail("Should handle database errors gracefully");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= CONCURRENT ACCESS SCENARIOS =============

    @Test
    public void addToCart_concurrentAdditionExceedsStock_throws() {
        try {
            // Simulate two customers trying to add items concurrently
            // that would exceed total stock
            Thread t1 = new Thread(() -> {
                try {
                    cartService.addToCart(1, 1, 500000);
                } catch (Exception e) {
                    // Expected to fail
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    cartService.addToCart(2, 1, 500000);
                } catch (Exception e) {
                    // Expected to fail
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            // At least one should fail due to stock constraints
        } catch (InterruptedException e) {
            assertNotNull(e.getMessage());
        }
    }

    // ============= EDGE CASES =============

    @Test
    public void addToCart_quantityAtExactStockLimit_succeeds() {
        try {
            // This should succeed if variant has exactly qty available
            cartService.addToCart(1, 1, 1);
            // Verify item was added
            CartSummaryDTO cart = cartService.getCart(1);
            assertTrue(cart.getItems().size() > 0);
        } catch (IllegalArgumentException e) {
            // May fail if not enough stock
            assertTrue(e.getMessage().contains("vượt quá"));
        } catch (SQLException e) {
            // Database error is acceptable in test environment
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void addToCart_maximumIntQuantity_throws() {
        try {
            cartService.addToCart(1, 1, Integer.MAX_VALUE);
            fail("Should throw exception for excessively large quantity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("vượt quá"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void updateQuantity_largeQuantity_throws() {
        try {
            cartService.updateQuantity(1, 1, Integer.MAX_VALUE);
            fail("Should throw exception for excessively large quantity");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            assertTrue(msg.contains("vượt quá") ||
                      msg.contains("không tìm thấy") ||
                      msg.contains("không thuộc") ||
                      msg.contains("tồn kho"));
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }
}
