# Use Case Specification
## Online Fruit Shop System
### UC-08 View and Track Order History

## 1. Management Information
**ID and Name:** UC-08 View and Track Order History
**Created By:** Duong Minh Hoang
**Date Created:** May 20, 2026
**Feature:** Order, Payment and Tracking -> Order History Page & Order Detail / Tracking Page

## 2. Actor Definitions & Purpose
**Primary Actor:** Customer
**Secondary Actors:** None
**Description:** Allows an authenticated Customer to view a comprehensive list of all their past and current orders. The Customer can drill down into specific order details, track the real-time delivery status, and voluntarily cancel an order if permitted by system business constraints.

## 3. Execution Conditions
**Trigger:** The Customer clicks on "My Orders", "Order History", or "Track Order" from their user account menu or navigation bar.
**Preconditions:**
- PRE-1: The user is authenticated as a Customer.
**Postconditions:**
- POST-1: The Customer views accurate, up-to-date statuses of their previous and ongoing orders.
- POST-2: If an order is successfully cancelled, its status is updated to "Cancelled" in the database, and any held inventory is released back to the general pool.

## 4. Scenarios (Flow of Events)
### Normal Flow (View Order History and Detail):
1. The Customer navigates to the "Order History" page.
2. The system retrieves all orders associated with the Customer's ID from the database, sorting them chronologically (newest first).
3. The system displays a paginated list of orders. Each entry shows the Order ID, Creation Date, Total Price, Shop Name, and the Current Status (e.g., Pending, Processing, Delivering, Completed, Cancelled).
4. The Customer clicks on a specific Order ID to view its full details.
5. The system fetches detailed information, including individual products, prices, applied promotional vouchers, selected payment method, and delivery address.
6. The system displays the Order Detail / Tracking Page, presenting a visual progress timeline of the order's state (Pending $\rightarrow$ Processing $\rightarrow$ Delivering $\rightarrow$ Completed).

### Alternative Flows:
**4.1. Cancel a Pending Order (Enforcing DEL-03)**
1. On the Order Detail page for an order whose status is strictly **"Pending"** (Waiting for Shop confirmation) or **"Awaiting Payment"**.
2. The Customer clicks the "Cancel Order" button.
3. The system prompts the Customer to confirm the action and optionally provide a reason for cancellation.
4. The Customer confirms the cancellation request.
5. The system performs a backend validation to ensure the order has not progressed beyond the allowed states.
6. The system legally updates the Order Status to **"Cancelled"**.
7. The system executes a stock release mechanism, returning any "Held" product quantities back to the Shop's available inventory pool.
8. The system automatically triggers a background notification to the relevant Shop Owner.
9. The system reloads the page, reflecting the new "Cancelled" status and removing the cancellation button.

**4.2. Filter and Sort Order History**
1. On the Order History page, the Customer selects a filter tab (e.g., "To Pay", "To Receive", "Completed", "Cancelled").
2. The system queries the database with the specific status filter applied.
3. The system updates the UI to show only the orders matching the selected status.

### Exceptions / Error Handling:
**4.1.E1 Cancellation Attempt on Processing Order (DEL-03 Violation)**
1. At Step 5 of Alternative Flow 4.1, the system detects that the Shop Owner has recently transitioned the order status to **"Processing"** (meaning the shop is actively packaging or cutting the fruit) or **"Delivering"**.
2. The system strictly blocks the cancellation request.
3. The system refreshes the UI state to hide the "Cancel Order" button and displays a warning alert: "This order is already being processed or prepared by the Shop and can no longer be cancelled. Please contact the Shop directly for further assistance."

**4.E1 Delivery Tracking API / Network Failure**
1. At Step 6 of the Normal Flow, the system attempts to fetch live geolocation tracking data from the Delivery Staff's app or a third-party logistics API but the service is unreachable.
2. The system prevents the page from crashing, degrading gracefully by hiding the live-map widget.
3. The system falls back to displaying the last-known text-based status update (e.g., "Delivering to customer - Updated 5 mins ago").

## 5. Additional Information
**Priority:** High (P1)
**Frequency of Use:** High. Customers frequently check back to track their fast-moving fruit deliveries.
**Business Rules:**
- **DEL-03 (Cancellation Constraint):** The Customer is only permitted to voluntarily self-cancel an order while it remains in "Pending" or "Awaiting Payment" statuses. Once the order progresses to "Processing" (in preparation/packaging), self-cancellation is strictly disabled to prevent loss of perishable goods for the Shop.
**Other Information:**
- The Order History page must clearly distinguish between logic flows of Guest tracking (via email/phone link) and Customer tracking (via authenticated account), though this Use Case focuses purely on the Authenticated Customer.
- If an order was paid via an Online Payment Gateway and is successfully cancelled (before Processing), the system must queue a refund workflow.
**Assumptions:** Shop Owners and Delivery Staff update order statuses via their respective portals in near real-time, ensuring the Customer's tracking view accurately reflects reality.