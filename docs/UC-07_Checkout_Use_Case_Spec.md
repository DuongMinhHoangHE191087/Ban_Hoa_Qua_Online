# Use Case Specification
## Online Fruit Shop System
### UC-07 Checkout

## 1. Management Information
**ID and Name:** UC-07 Checkout
**Created By:** Duong Minh Hoang
**Date Created:** May 20, 2026
**Feature:** Cart and Checkout -> Checkout Page

## 2. Actor Definitions & Purpose
**Primary Actor:** Customer
**Secondary Actors:** Payment Gateway (External API), Logistics/Mapping API
**Description:** Allows an authenticated Customer to formalize their purchase by providing delivery details, choosing a payment method, and officially placing the order. This use case enforces critical logistics rules (weight, distance) and inventory locks to prevent overselling.

## 3. Execution Conditions
**Trigger:** The Customer clicks the "Proceed to Checkout" button from the Customer Cart Page (UC-06), or the system automatically directs the user here after converting a Guest Cart upon login (USR-05).
**Preconditions:**
- PRE-1: The user is authenticated as a Customer.
- PRE-2: The Customer's Cart contains at least one product.
- PRE-3: The user's account is active and not globally suspended.
**Postconditions:**
- POST-1: A new Order record is created in the database (e.g., status "Pending" or "Awaiting Payment").
- POST-2: The respective product inventory quantities are transitioned to a "Hold" state.
- POST-3: The Customer Cart is cleared for the successfully processed items.

## 4. Scenarios (Flow of Events)
### Normal Flow (Checkout using Cash on Delivery - COD):
1. The system displays the Checkout Page, summarizing all cart items, individual weights, total order price, and an estimated shipping fee based on the default address.
2. The Customer selects an existing Delivery Address or enters a new one.
3. The system calls a Logistics/Mapping API to calculate the exact distance between the Shop's inventory location and the Customer's Delivery Address.
4. The system validates the Delivery Distance (must be $\le$ 20km) and Total Cargo Weight (must be $\le$ 30kg).
5. The system dynamically computes the exact shipping fee based on distance and weight, then updates the Grand Total.
6. The Customer selects the "Cash on Delivery (COD)" payment method.
7. The system evaluates the business rules for COD applicability (Invoice Total must be < 2,000,000 VND, and Customer must not have a history of "bombing" orders).
8. The Customer clicks the "Place Order" button.
9. The system validates the current real-time stock availability for all items one final time.
10. The system creates the Order record in the database with the status **"Pending"** (Waiting for Shop confirmation/preparation).
11. The system immediately locks the required stock quantities, changing them to a "Hold" status (preventing concurrent purchases by other users).
12. The system clears the successfully processed items from the Customer Cart.
13. The system redirects the Customer to the Order Confirmation/Success Page, displaying the Order ID and expected delivery time.

### Alternative Flows:
**4.1. Checkout using Online Payment (Credit Card / E-wallet)**
1. In Step 6 of the Normal Flow, the Customer selects "Online Payment" (e.g., VNPAY, Momo, or Credit Card).
2. The system allows this payment method regardless of the 2,000,000 VND limit.
3. The Customer clicks "Place Order".
4. The system creates the Order record in the database with the status **"Awaiting Payment"**.
5. The system holds the inventory quantities for a maximum of 15 minutes (INV-01).
6. The system redirects the Customer to the external Payment Gateway.
7. The Customer completes the payment on the payment provider's page within the allowable 10-minute transaction window (PAY-02).
8. The system receives a success webhook from the Payment Gateway.
9. The system updates the Order status to **"Pending"** or **"Paid"**, officially confirming the inventory deduction, and clears the cart.
10. The Customer is redirected back to the fruit shop's Order Success Page.

**4.2. Apply Promotional Voucher**
1. At Step 5 of the Normal Flow, the Customer enters a voucher code or selects an available coupon logic.
2. The system verifies the voucher validity (expiration, minimum order value, max usage limits).
3. The system applies the discount to the subtotal and recalculates the Grand Total.
4. The flow resumes at Step 6.

### Exceptions / Error Handling:
**4.E1 Delivery Distance Exceeds Limit (DEL-01)**
1. At Step 4 of the Normal Flow, the system determines the delivery radius exceeds 20km from the shop.
2. The system disables the "Place Order" button.
3. The system displays a clear error message: "Delivery address is outside the 20km maximum radius for instant delivery. Please select a closer address."

**4.E2 Cart Weight Exceeds Capacity Limit (DEL-02)**
1. At Step 4 of the Normal Flow, the system calculates that the total cart weight exceeds 30kg.
2. The system prevents checkout and displays a warning: "Total order weight exceeds the 30kg limit for motorcycle instant delivery. Please reduce the quantity of heavy items."

**4.E3 COD Constraints Violated (PAY-01 & SEC-01)**
1. At Step 7 of the Normal Flow, the system blocks COD selection if:
   - The Invoice Total $\ge$ 2,000,000 VND.
   - Or, the Customer has a history of $\ge$ 3 failed deliveries caused by the buyer (Anti-Bombing Rule SEC-01).
2. The system disables (grays out) the COD radio button and displays an inline localized exception text (e.g., "COD is not available for orders above 2,000,000 VND or due to account history. Please use Online Payment").
3. The Customer must select Online Payment to proceed.

**4.E4 Online Payment Timeout or Failure (INV-01 & PAY-02)**
1. At Step 7 of Alternative Flow 4.1, the 10-minute payment session expires, or the Customer clicks "Cancel Payment", or the card is rejected.
2. The system updates the local Order status to **"Payment Failed / Cancelled"**.
3. Crucially, the 15-minute inventory hold expires, and the system **Releases** the held stock back to the general available pool (INV-01).
4. The system retains the items in the Customer Cart (instead of clearing them) and suggests the user to try checking out again.

**4.E5 Shop Owner Attempting to Buy Own Products (USR-04)**
1. At any point during checkout, if the system detects the authenticated user is attempting to buy products belonging to their own Shop (checked via User ID, IP, or Device ID logic).
2. The system blocks the finalization and shows an error: "Shop Owners are not permitted to purchase their own products to manipulate sales or ratings."

**4.E6 Real-time Out of Stock**
1. At Step 9 of the Normal Flow, concurrent purchases have reduced the available inventory below the Customer's requested quantity.
2. The system halts the checkout.
3. The system redirects the user back to the Cart Page (UC-06), highlights the unavailable items, and displays: "Sorry, some items in your cart just went out of stock. Please review your cart."

## 5. Additional Information
**Priority:** Highest (P0 - Mission Critical)
**Frequency of Use:** High
**Business Rules:**
- **USR-04:** Anti-manipulation. Shop Owners cannot buy their own products.
- **INV-01:** "Stock Quantity" must be kept in a "Hold" state for 15 minutes when proceeding with an online payment. If the payment process surpasses 15 minutes, the Hold is automatically Release.
- **DEL-01:** Instant delivery is restricted strictly to a 20km radius between the Shop and the Delivery Address.
- **DEL-02:** Max load constraint for motorcycle transport is 30kg.
- **PAY-01:** COD limit is < 2,000,000 VND to prevent high-value fraudulent orders.
- **SEC-01 (Anti-Bombing):** Accounts with a history of $\ge$ 3 failed deliveries lose COD privileges.
**Assumptions:** The integration with the Third-party Mapping/Distance API is responsive. Payment webhook endpoints are exposed securely and receive timely callbacks from the Gateway.