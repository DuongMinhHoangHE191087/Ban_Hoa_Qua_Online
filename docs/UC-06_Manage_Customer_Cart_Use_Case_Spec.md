# Use Case Specification
## Online Fruit Shop System
### UC-06 Manage Customer Cart

## 1. Management Information
**ID and Name:** UC-06 Manage Customer Cart
**Created By:** Duong Minh Hoang
**Date Created:** May 20, 2026
**Feature:** Cart and Checkout -> Customer Cart

## 2. Actor Definitions & Purpose
**Primary Actor:** Customer
**Secondary Actors:** None
**Description:** Allows an authenticated Customer to build an order by adding, updating, or removing fruit products in their shopping cart. Unlike the Guest Cart, the Customer Cart is stored securely in the database, allowing synchronization and persistence across multiple devices and sessions.

## 3. Execution Conditions
**Trigger:** The Customer clicks the "Add to Cart" button on a product page, clicks the Cart icon in the navigation bar, or logs into the system with an existing Guest Cart.
**Preconditions:**
- PRE-1: The user is authenticated as a Customer.
- PRE-2: The selected product has an available stock quantity > 0.
**Postconditions:**
- POST-1: The selected products and their quantities are saved or updated directly in the server's database.
- POST-2: The UI reflects the synchronized cart item count and total price instantly.

## 4. Scenarios (Flow of Events)
### Normal Flow (Add Product to Customer Cart):
1. The Customer navigates to a Product Listing Page or Product Detail Page.
2. The Customer selects the desired product variations (e.g., weight option like 500g or 3kg) and inputs the desired quantity.
3. The Customer clicks the "Add to Cart" button.
4. The system validates the requested quantity against the currently available inventory in the database (Stock Quantity).
5. The system sends an asynchronous request to the server to add/update the `cart_items` table with the product ID, variant ID, and quantity. (If the item already exists in the cart, its quantity is updated appropriately).
6. The server confirms the addition and returns the updated global Cart item counter/subtotal.
7. The system updates the UI and displays a brief success toast/notification: "Item successfully added to cart."

### Alternative Flows:
**4.1. Merge Guest Cart upon Login**
1. A Guest with items in their local Temporary Cart successfully logs in (via UC-02 or UC-01).
2. The system detects the presence of local cart data.
3. The system transfers/merges the local cart items securely over to the authenticated Customer's database cart.
4. If a matching item already exists in both carts, the system securely consolidates them (e.g., sums up the quantities without exceeding available total stock).
5. The system clears the local temporary cart.

**4.2. View and Update Cart Quantity**
1. The Customer clicks the Cart icon in the navigation bar.
2. The system retrieves the cart data from the Database, fetching the latest prices, active promotions, and stock information.
3. The system displays the Customer Cart Page, listing all selected items, their individual prices, and the order subtotal.
4. The Customer adjusts the quantity of a specific item using the (+) or (-) buttons.
5. The system validates the new quantity against the available stock in real-time.
6. The system updates the Database and automatically recalculates the subtotal on the UI.

**4.3. Remove Item from Cart**
1. On the Customer Cart Page, the Customer clicks the "Remove" (or trash bin) icon next to a cart item.
2. The system sends a delete request to the server.
3. The server removes the item from the user's cart record in the database.
4. The system removes the item from the UI list, recalculates the order subtotal, and updates the global Cart counter.

**4.4. Proceed to Checkout**
1. On the Customer Cart Page, the Customer clicks the "Proceed to Checkout" button.
2. The system verifies that the cart is not empty and validates all items against current stock limits.
3. The system redirects the Customer directly to the Checkout Page (UC-07).

### Exceptions / Error Handling:
**4.E1 Out of Stock / Insufficient quantity**
1. At step 4 of the Normal Flow, or step 5 of Alternative Flow 4.2, the requested quantity exceeds the currently available stock.
2. The system rejects the addition/update.
3. The system displays an error message: "The requested quantity exceeds available stock."
4. The system automatically adjusts the input field to the maximum available quantity.

**4.2.E1 Product Price or Status Changed**
1. At step 2 of Alternative Flow 4.2, the server indicates that a previously added product is now deactivated, completely out of stock, or its price has changed since it was last added.
2. The system updates the user's cart UI to reflect reality (e.g., highlighting the item in red as "Out of stock" or displaying the new price with a warning).
3. The system displays an alert: "Some items in your cart have been updated based on current availability and pricing."
4. The Customer must review and accept the changes, or remove unavailable items before they can proceed to Checkout.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** Very High. Used multiple times by Customers during shopping sessions.
**Business Rules:**
- Related to INV-01: Items in the Cart are NOT physically reserved yet. Stock quantity is only officially reserved (Hold) during the Checkout phase for 15 minutes. Thus, a product currently in the cart might still go out of stock if another user buys it first.
**Other Information:** 
- The Customer Cart data must persist in the server Database, ensuring that if a Customer logs in on their Mobile Phone, they see the same cart items they added previously on their Desktop.
- Cart items belonging to Shops that have subsequently been banned/suspended must be automatically marked as unavailable.
**Assumptions:** Network connectivity is stable enough for real-time Database updates on cart item manipulations.