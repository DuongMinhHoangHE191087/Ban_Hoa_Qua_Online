# Use Case Specification
## Online Fruit Shop System
### UC-05 Manage Guest Cart

## 1. Management Information
**ID and Name:** UC-05 Manage Guest Cart
**Created By:** Duong Minh Hoang
**Date Created:** May 19, 2026
**Feature:** Cart and Checkout -> Guest Cart

## 2. Actor Definitions & Purpose
**Primary Actor:** Guest / Unauthenticated User
**Secondary Actors:** None
**Description:** Allows a Guest to temporarily build an order by adding, updating, or removing fruit products in a temporary shopping cart. The cart data is stored locally on the user's device (e.g., LocalStorage or SessionStorage) to ensure a seamless browsing experience before requiring authentication.

## 3. Execution Conditions
**Trigger:** The Guest clicks the "Add to Cart" button on a product page, or clicks the Cart icon in the navigation bar to view/edit their current selections.
**Preconditions:**
- PRE-1: The user is browsing the system as a Guest (unauthenticated).
- PRE-2: The browser allows LocalStorage/SessionStorage operations.
- PRE-3: The selected product has an available stock quantity > 0.
**Postconditions:**
- POST-1: The selected products and their quantities are saved or updated in the browser's temporary storage.
- POST-2: The UI reflects the updated cart item count and total price instantly.

## 4. Scenarios (Flow of Events)
### Normal Flow (Add Product to Guest Cart):
1. The Guest navigates to the Product Listing Page or Product Detail Page.
2. The Guest selects the desired product variations (e.g., weight option like 500g or 3kg) and input the desired quantity.
3. The Guest clicks the "Add to Cart" button.
4. The system validates the requested quantity against the currently available inventory (Stock Quantity).
5. The system saves the product ID, variant ID, and quantity into the browser's LocalStorage/SessionStorage. (If the item already exists, its quantity is incremented).
6. The system updates the global Cart item counter on the navigation bar.
7. The system displays a brief success toast/notification: "Item successfully added to cart."

### Alternative Flows:
**4.1. View and Update Cart Quantity**
1. The Guest clicks the Cart icon in the navigation bar.
2. The system retrieves the cart data from LocalStorage and fetches the latest prices and stock information from the server.
3. The system displays the Guest Cart Page, listing all selected items, their individual prices, and the order subtotal.
4. The Guest adjusts the quantity of a specific item using the (+) or (-) buttons.
5. The system validates the new quantity against the available stock.
6. The system updates LocalStorage and automatically recalculates the subtotal.

**4.2. Remove Item from Cart**
1. On the Guest Cart Page, the Guest clicks the "Remove" (or trash bin) icon next to a cart item.
2. The system asks for a quick confirmation (optional, based on UX design) or immediately removes the item from LocalStorage.
3. The system removes the item from the UI list and recalculates the order subtotal.
4. The system updates the global Cart item counter.

**4.3. Proceed to Checkout (Authentication Required)**
1. On the Guest Cart Page, the Guest clicks the "Proceed to Checkout" button.
2. The system identifies that the user is currently a Guest.
3. The system intercepts the checkout navigation and enforces business rule USR-05.
4. The system redirects the Guest to the Login/Register Page.
5. The system passes a `returnUrl` or state parameter to ensure the user is taken back to the checkout process once authentication (and cart merging) succeeds.

### Exceptions / Error Handling:
**4.E1 Out of Stock / Insufficient quantity**
1. At step 4 of the Normal Flow, or step 5 of Alternative Flow 4.1, the requested quantity exceeds the currently available stock.
2. The system rejects the addition/update.
3. The system displays an error message: "The requested quantity exceeds available stock."
4. The system automatically adjusts the input field to the maximum available quantity.

**4.1.E1 Product Price or Status Changed**
1. At step 2 of Alternative Flow 4.1, the server indicates that a previously added product is now deactivated, out of stock, or its price has changed.
2. The system updates the local cart to reflect reality (e.g., marking the item as "Out of stock" or adjusting the displayed price).
3. The system displays an alert: "Some items in your cart have been updated based on current availability and pricing."
4. The Guest must review and accept the changes or remove unavailable items.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** Very High. Many users will browse and add items before deciding to log in.
**Business Rules:**
- USR-05: Guests are allowed to browse products and add them to the cart, but they are strictly required to log in or register before they are permitted to proceed to Checkout.
**Other Information:** 
- The guest cart data is strictly client-side (LocalStorage/SessionStorage) and does not persist across different devices/browsers.
- Upon successful login (UC-02) or registration (UC-01), any items existing in the Guest Cart will be automatically merged into the user's permanent Server Cart (Customer Cart). 
**Assumptions:** The Guest's web browser is configured to allow LocalStorage or cookies. If in strict Incognito/Private mode where storage is disabled, the cart might reset upon page reload; the system should handle this gracefully (e.g., displaying a warning if storage writes fail).