# SOFTWARE REQUIREMENT SPECIFICATION
## Online Fruit Shop System
### 3. Functional Requirements

**Document Type:** Standalone Section 3 for DOCX export  
**Version:** 0.1  
**Purpose:** This document isolates the Functional Requirements section of the SRS as screen/function-based content, standardizes the tables for Word editing, and serves as input for business review, test cases, mockups, and implementation.

---

## 3.0 Section Guideline

This section follows the template structure Feature -> SubFeature -> Screen/Function. Each screen or function lists its purpose, related use cases, main UI elements or parameters, and the key validation rules needed for implementation.

### 3.0.1 Screen Coverage Map

| Functional Group | Primary Roles | SRS Coverage | UI Checklist Mapping |
| --- | --- | --- | --- |
| Authentication | Guest, Customer, Shop Owner, Delivery Staff, Admin | Register, Login, Forgot Password, Lockout | Authentication Entry Page, Register Page, Login Page, Forgot Password Page |
| Product Discovery | Guest, Customer, Shop Owner, Delivery Staff, Admin | Home, Listing, Detail, Search / Filter | Home Page, Product Listing Page, Product Detail Page, Search Results Page |
| Cart and Checkout | Guest, Customer | Guest Cart, Cart, Checkout, Confirmation | Guest Cart Page, Customer Cart Page, Checkout Page, Order Confirmation Page |
| Order, Payment and Tracking | Customer, Shop Owner, Delivery Staff, Admin | Payment, History, Tracking, Delivery Update | Order History Page, Order Detail / Tracking Page, Delivery Dashboard, Delivery Detail Page |
| Shop Management | Customer, Shop Owner, Admin | Registration, Product, Inventory, Promotion, Dashboard, Settlement | Shop Registration Page, Shop Dashboard, Shop Profile Page, Product List Page, Product Create/Edit Page, Variant Management Page, Image Management Page, Inventory Management Page, Order Management Page, Promotion Management Page, Settlement Summary Page, Shop Report Page |
| Administration | Admin | User, Category, Approval, Moderation, Monitoring, Master Data, Reports | Admin Dashboard, User Management Page, Shop Approval Queue / Detail Page, Category Management Page, Product Moderation Page, Order / Payment Monitoring Page, Settlement Management Page, Setting List Page, Report Dashboard Page, System Notification Page, Audit Log Page |
| Non-UI Functions | System | Sync, webhook, reservation, notification, recommendation, batch job | Cart Sync Service, Payment Webhook Service, Inventory Reservation Service, Notification Service, Recommendation Service, Settlement Batch Job |

### 3.0.2 Screen State Rules

- Loading: The screen must show a loading or skeleton state while data is not yet available.
- Empty: The screen must provide a clear empty state when no data exists.
- Success: The primary content must be fully visible and consistent.
- Error: Error messages must explain the failure and the next step.
- No Permission: Access must be blocked when the user lacks permission.
- Sensitive Action: Actions such as payment, order cancellation, shop approval, and settlement closure must require confirmation or audit logging.

### 3.0.3 Core Business Rules

| Rule | Description |
| --- | --- |
| One Order per Shop | Each order must belong to exactly one shop owner. If the cart contains items from multiple shops, checkout must split the order or block the action. |
| Guest Cart | The Guest cart is stored temporarily in localStorage or sessionStorage and does not require an account. |
| Review Eligibility | Reviews can only be created after the order is completed. |
| Settlement Timing | Settlement can only be finalized after the complaint window expires. |
| Delivery Role | Delivery Staff can only update delivery statuses. |
| Audit Trail | Sensitive actions must be recorded in the audit trail for traceability. |

---

## 3.1 User Authentication

### 3.1.1 Account Access

#### 3.1.1.1 Register Page

Content #1: UI Layout

Responsive register page with a branded introduction panel on the left and a form card on the right. The form card should include grouped inputs for identity, credentials, and account setup. On desktop, the page should feel like a guided onboarding screen with clear hierarchy, while on mobile it should collapse into a single-column form with the primary action always visible.

Content #2: Brief Description

Allows a Guest to create a new Customer or Shop Owner account for UC-01. The screen supports manual sign-up and Google OAuth sign-up, validates email uniqueness and password rules, and routes shop owner accounts to Pending Approval when the business rule requires admin review. If the user arrives here from a protected action such as checkout, the page must preserve the return intent after successful registration.

Content #3: Component / Field Specification

Field Group: Identity

| Field Name | Description |
| --- | --- |
| Full Name | Required text input; 3-100 characters; initial value blank. |
| Email | Required email input; unique; 5-254 characters; initial value blank. |
| Phone Number | Optional text input; up to 15 characters; initial value blank. |

Field Group: Credentials

| Field Name | Description |
| --- | --- |
| Password | Required password input; 8-64 characters; masked entry; stored only as a hash after submission. |
| Confirm Password | Required password input; must match Password exactly; initial value blank. |

Field Group: Account Setup and Security

| Field Name | Description |
| --- | --- |
| Verification Code | Conditional input shown only when email or OTP verification is enabled. |
| Register with Google | Secondary action button that starts Google OAuth sign-up. |
| Register Button | Primary action button that submits the form. |
| Login Link | Navigation link that returns existing users to the Login Page. |

Behavior Notes:
- Email must be unique before the account is created.
- Password and Confirm Password must match before submission succeeds.
- Shop Owner registration must create a Pending Approval account when admin review is required.
- Google OAuth registration must create the account without a password when the email is new.
- Validation errors must stay on the form and show field-level messages instead of clearing the page.

#### 3.1.1.2 Login Page

UI Layout: Login form with credential fields and login button.

Description: Authenticates Guest, Customer, Shop Owner, Delivery Staff, and Admin. Related UC-02.

| Field Name | Description |
| --- | --- |
| Email or Phone | Required login identifier. |
| Password | Required; must match stored hash. |
| Remember Me | Optional; keeps the session active longer. |
| Captcha / Anti-bot | Optional security control if enabled. |

Checklist:
- Incorrect passwords must display a clear error message.
- Locked accounts must display the lock status.
- The screen must include a link to Forgot Password.
- Excessive failed attempts must trigger lockout.

#### 3.1.1.3 Forgot Password Page

UI Layout: Password reset form with verification and new password inputs.

Description: Supports password reset after identity verification. Related UC-02.

| Field Name | Description |
| --- | --- |
| Email or Phone | Required for account lookup. |
| Reset Code | Required; one-time verification code. |
| New Password | Required; must satisfy password policy. |
| Confirm Password | Must match the new password. |

Checklist:
- The reset code must have an expiration time.
- New Password and Confirm Password must match.
- Invalid or expired codes must show a clear error.

### 3.1.2 Session Security

#### 3.1.2.1 Login Lockout Function

UI Layout: Non-UI security function.

Description: Tracks failed logins, increments failed_login_count, and sets locked_until when the threshold is exceeded. Related UC-02.

| Parameter | Description |
| --- | --- |
| failed_login_count | Counter of consecutive failed attempts. |
| locked_until | Time until the account remains locked. |
| is_email_verified | Determines whether the account can proceed with sensitive actions. |

Behavior:
- Each failed login increments failed_login_count by 1.
- When the threshold is exceeded, the system sets locked_until and rejects further attempts.
- Once the unlock time is reached, the account can log in again.
- Every lock or unlock event must be audited if required by policy.

---

## 3.2 Product Discovery

### 3.2.1 Home Page

UI Layout: Search bar, category blocks, featured products, promotion banners, and seasonal recommendation blocks.

Description: Shows active products, highlights, and quick navigation entry points for Guest and authenticated users. Related UC-03, UC-04, and UC-20.

| Component | Description |
| --- | --- |
| Search Bar | Quick keyword search entry point. |
| Category Blocks | Main fruit categories for navigation. |
| Featured Product Grid | Hot or highlighted products. |
| Promotion Banner | Active promotion or campaign banner. |
| Seasonal Recommendation Block | Recommended products based on season or history. |
| Footer Links | Support, policies, and contact links. |

Checklist:
- Only active products are shown.
- Discontinued items must be hidden.
- The search bar must always be accessible.
- Featured blocks must have clear navigation links.

#### 3.2.1.2 Product Listing Page

UI Layout: Search input, category tabs, filter panel, sorting dropdown, product grid, and pagination.

Description: Shows product lists with filtering and sorting for Guest and authenticated users. Related UC-03 and UC-04.

| Field Name | Description |
| --- | --- |
| Keyword Search | Search by product name or description. |
| Category | Filter by fruit category. |
| Price Range | Filter by min and max price. |
| Rating Filter | Filter by star rating. |
| Shop Region | Filter by shop location or area. |
| Sort Order | Sort by popularity, newest, or price. |

Checklist:
- Filters must work correctly in combination.
- Product cards must show image, name, price, rating, and shop.
- Pagination must preserve filter state if the design supports it.
- Results must contain only active products.

#### 3.2.1.3 Product Detail Page

UI Layout: Image gallery, product summary, variant selector, shop card, and buy/add-to-cart actions.

Description: Presents full product information including origin and storage details. Related UC-03.

| Field Name | Description |
| --- | --- |
| Product Images | Primary and gallery images. |
| Product Name | Product title shown to the user. |
| Origin Country | Country of origin. |
| Origin Region | Production region or farm area. |
| Harvest Date | Harvest date if available. |
| Shelf Life Days | Expected storage duration. |
| Storage Instruction | Guidance for keeping the fruit fresh. |
| Variants | Weight, grade, ripeness, or packaging options. |
| Shop Information | Shop name, rating, and delivery address. |

Checklist:
- Price must update based on the selected variant.
- Out-of-stock variants cannot be selected.
- If a variant is required, it must be selected before adding to cart.
- Stock display must reflect current inventory.

### 3.2.2 Search and Filter Function

#### 3.2.2.1 Search / Filter Panel Function

UI Layout: Search input and multiple filter controls used on listing pages.

Description: Supports combined filtering by keyword, price, rating, region, and fruit type. Related UC-04.

| Field Name | Description |
| --- | --- |
| Keyword | Text query for search. |
| Min Price | Lower bound of the price range. |
| Max Price | Upper bound of the price range. |
| Fruit Type | Fruit type or subcategory filter. |
| Active Only | Restrict results to active products. |

Checklist:
- Keyword must not be empty when a search text submission is required.
- Min Price must be less than or equal to Max Price.
- The filter must return results that match the combined criteria.

---

## 3.3 Cart and Checkout

### 3.3.1 Guest Cart

#### 3.3.1.1 Guest Cart Storage Function

UI Layout: Non-UI cart persistence function.

Description: Stores the guest cart in localStorage or sessionStorage and merges it to the server cart after login. Related UC-05.

| Parameter | Description |
| --- | --- |
| cart_key | Browser storage key for the guest cart. |
| guest_session_id | Identifier for the anonymous cart session. |
| cart_items | Product variants and quantities stored locally. |
| sync_status | Marks whether the cart has been synchronized. |

Behavior:
- The Guest cart must persist after refresh according to design.
- When the user logs in, the system must sync the cart to the server.
- If there is a sync conflict, the merge rule must be clearly defined.

#### 3.3.1.2 Guest Cart Page

UI Layout: Cart item list, quantity controls, login prompt, and checkout prompt.

Description: Lets a Guest review the temporary cart before login or checkout. Related UC-05.

| Field Name | Description |
| --- | --- |
| Product | Product name snapshot. |
| Variant | Variant label selected by the customer. |
| Quantity | Number of units to purchase. |
| Unit Price | Price per unit at checkout time. |
| Subtotal | Line total for each cart item. |
| Login Prompt | CTA to sign in and sync the cart. |

Checklist:
- Refreshing the page must not clear the cart as designed.
- If guest checkout is not supported, a clear login prompt must be shown.
- Quantity must be validated against inventory.

### 3.3.2 Customer Cart and Checkout

#### 3.3.2.1 Customer Cart Page

UI Layout: Cart item list, quantity controls, voucher area, and checkout button.

Description: Lets the Customer review and edit cart items before checkout. Related UC-06 and UC-07.

| Field Name | Description |
| --- | --- |
| Product | Product name snapshot. |
| Variant | Variant label selected by the customer. |
| Quantity | Number of units to purchase. |
| Unit Price | Price per unit at checkout time. |
| Subtotal | Line total for each cart item. |
| Voucher Code | Optional promotion code. |

Checklist:
- Quantity cannot exceed stock.
- If the cart contains multiple shops, checkout must split the order or block the action.
- The total amount must be shown clearly before submission.

#### 3.3.2.2 Checkout Page

UI Layout: Shipping form, payment method selector, order summary, and confirm button.

Description: Creates an order for exactly one shop owner. Related UC-07 and UC-08.

| Field Name | Description |
| --- | --- |
| Delivery Address | Address for receiving the order. |
| Time Slot | Preferred delivery time window. |
| Notes | Special instructions from the customer. |
| Payment Method | Bank transfer or COD. |
| Order Summary | Total, discount, delivery fee, and final amount. |
| Confirm Order | Final action to submit the order. |

Checklist:
- Checkout must allow only one shop.
- If inventory is insufficient, order placement must be blocked.
- Confirm Order must generate a unique order code.
- Total must be calculated correctly: subtotal - discount + delivery fee.

#### 3.3.2.3 Order Confirmation Page

UI Layout: Confirmation message, order code, and next-step guidance.

Description: Shows the generated order code and payment status immediately after checkout. Related UC-07 and UC-08.

| Field Name | Description |
| --- | --- |
| Order Code | Unique code for tracking the order. |
| Payment Status | Pending, completed, failed, or expired. |
| Next Action | Pay now, wait for confirmation, or track order. |

Checklist:
- The order code must be displayed clearly so the user can save it.
- If payment is pending, next-step guidance must be shown.
- If COD is used, appropriate confirmation guidance must be displayed.

---

## 3.4 Order, Payment and Tracking

### 3.4.1 Payment Processing

#### 3.4.1.1 Payment Page

UI Layout: Payment details, QR code or bank reference area, and status banner.

Description: Displays payment instructions and transaction status. Related UC-08.

| Field Name | Description |
| --- | --- |
| Amount | Final payable amount. |
| Payment Method | Bank transfer or COD. |
| QR Code | QR image or payment link for transfer. |
| Reference Code | Payment reference to match the order. |
| Payment Expiry | Time limit for completing the transfer. |
| Payment Status Banner | Current transaction status and user guidance. |

Checklist:
- The reference code must match the order code or payment content.
- The expiry time must be displayed clearly.
- Payment status must update after webhook or polling.

#### 3.4.1.2 Payment Webhook Function

UI Layout: Non-UI integration function.

Description: Receives webhook events from the payment gateway and updates the payment transaction status. Related UC-08.

| Parameter | Description |
| --- | --- |
| sepay_transaction_id | Unique bank transaction identifier. |
| order_code | Order reference code from the payment content. |
| provider_response | Raw webhook payload for audit and reconciliation. |
| process_result | Result of deduplication or processing. |

Behavior:
- The system must prevent duplicate webhook processing.
- If a transaction has already been processed, the system must return an idempotent result.
- The raw payload must be stored for audit and reconciliation.

### 3.4.2 Order Tracking

#### 3.4.2.1 Order History Page

UI Layout: Order list, status filters, date filters, and search by order code.

Description: Lets the Customer review historical orders and open order details. Related UC-09.

| Field Name | Description |
| --- | --- |
| Order Code | Order identifier. |
| Order Date | Date and time when the order was created. |
| Shop Name | Shop associated with the order. |
| Total Amount | Final order amount. |
| Payment Status | Payment state of the order. |
| Order Status | Current order lifecycle status. |

Checklist:
- Only orders belonging to the current account are shown.
- Status filters must work correctly.
- Clicking an order must open the correct detail page.

#### 3.4.2.2 Order Detail / Tracking Page

UI Layout: Order summary, status timeline, delivery info, and after-sales action buttons.

Description: Lets the Customer, Shop Owner, Delivery Staff, and Admin track the order lifecycle. Related UC-09.

| Field Name | Description |
| --- | --- |
| Order Code | Identifier for the tracked order. |
| Status Timeline | PENDING_PAYMENT, CONFIRMED, PREPARING, DISPATCHED, DELIVERED, CANCELLED. |
| Order Items | Snapshot of purchased products and variants. |
| Delivery Info | Assigned staff and delivery timestamps. |
| Payment Info | Payment method, payment status, and payment reference. |
| After-Sales Action | Cancel, return, or exchange request entry point. |

Checklist:
- The timeline must reflect the standard state flow.
- Completed orders must allow reviews.
- Cancelled, expired, or failed orders must clearly display the final status.
- After-sales actions must follow business rules.

### 3.4.3 Delivery Management

#### 3.4.3.1 Delivery Dashboard Page

UI Layout: Assigned delivery list, status cards, and failure handling panel.

Description: Used by Delivery Staff to process assigned orders. Related UC-14.

| Field Name | Description |
| --- | --- |
| Assigned Orders | Orders currently assigned to the staff member. |
| Pickup Status | Indicates whether the parcel has been picked up. |
| In Transit Status | Indicates whether the order is on the way. |
| Failure Reason | Required when delivery fails. |

Checklist:
- Only assigned orders are shown.
- Delivery failures must require a reason.
- Staff can update only valid delivery statuses.

#### 3.4.3.2 Delivery Status Update Function

UI Layout: Non-UI status transition function.

Description: Updates the delivery status in the allowed order and delivery workflow. Related UC-14.

| Parameter | Description |
| --- | --- |
| status | ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, or FAILED. |
| picked_up_at | Time when the order is collected. |
| delivered_at | Time when the order is delivered. |
| failure_reason | Reason for unsuccessful delivery. |

Behavior:
- Only valid workflow statuses are accepted.
- Reverse transitions are blocked when the business rules do not allow them.
- A FAILED status must include failure_reason.

---

## 3.5 Shop Management

### 3.5.1 Shop Onboarding

#### 3.5.1.1 Shop Registration Page

UI Layout: Shop application form and document upload area.

Description: Allows a Customer or Shop Owner to submit a shop registration request. Related UC-10.

| Field Name | Description |
| --- | --- |
| Shop Name | Display name of the shop. |
| Shop Description | Introduction and selling profile. |
| Delivery Address | Pickup or business address. |
| Supporting Documents | Uploaded evidence required for approval. |
| Contact Information | Email, phone, or other contact details. |

Checklist:
- The form must enter pending state after submission.
- Uploaded documents must be stored in an allowed format.
- If rejected, the rejection reason must be shown clearly.

#### 3.5.1.2 Shop Approval Page

UI Layout: Request list, document preview, approval actions, and reason box.

Description: Used by Admin to approve, reject, or suspend a shop application. Related UC-10.

| Field Name | Description |
| --- | --- |
| Application List | Pending shop applications. |
| Approval Status | Pending, approved, rejected, or suspended. |
| Rejection Reason | Required when the application is rejected. |
| Approved At | Timestamp of approval. |

Checklist:
- Rejection requires a reason.
- Approved At must be recorded when the application is approved.
- All approval actions must be audit logged.

#### 3.5.1.3 Shop Profile Page

UI Layout: Shop profile form and business information panel.

Description: Lets the Shop Owner review and update public shop information. Related UC-10 and UC-19.

| Field Name | Description |
| --- | --- |
| Shop Name | Public shop name. |
| Shop Description | Shop introduction text. |
| Delivery Address | Pickup or business address. |
| Contact Information | Email, phone, or other contact details. |
| Business Status | Active, pending, rejected, or suspended. |

Checklist:
- Public information must be consistent across the shop card and order detail.
- If the shop is suspended, selling must be blocked.

### 3.5.2 Product Management

#### 3.5.2.1 Product List Page

UI Layout: Product table with search, filter, and action buttons.

Description: Shows products owned by the shop and supports CRUD operations. Related UC-11.

| Field Name | Description |
| --- | --- |
| Product Name | Product title. |
| Category | Category assignment. |
| Status | Active or inactive. |
| Rating | Average product rating. |
| Sold Quantity | Total sold count. |
| Actions | Edit, hide, or delete. |

Checklist:
- Only products belonging to the current shop are shown.
- Hide/delete actions must respect business rules.
- The table should support filtering by status and category.

#### 3.5.2.2 Product Create / Edit Page

UI Layout: Product create/edit form with metadata and image sections.

Description: Lets the Shop Owner maintain product master data. Related UC-11.

| Field Name | Description |
| --- | --- |
| Product Name | Required text field. |
| Description | Detailed product description. |
| Origin Country | Country of origin. |
| Origin Region | Region or farm area. |
| Harvest Date | Product harvest date. |
| Shelf Life Days | Storage duration estimate. |
| Storage Instruction | Guidance for preserving freshness. |
| Category | Product category selection. |
| Status | Active or inactive. |

Checklist:
- Product name is required.
- If inactive products are allowed, they must not appear in the storefront.
- The description must support SEO or appear clearly on the detail page.

#### 3.5.2.3 Product Variant / Image Management Page

UI Layout: Variant editor and image uploader.

Description: Maintains SKU, price, stock, and display images for product variants. Related UC-11.

| Field Name | Description |
| --- | --- |
| SKU | Unique variant code. |
| Variant Label | Weight, grade, or ripeness label. |
| Price | Selling price of the variant. |
| Stock Quantity | Available inventory for the variant. |
| Primary Image | Main product image. |
| Display Order | Ordering of gallery images. |

Checklist:
- SKU must be unique.
- Images must display in display_order sequence.
- Out-of-stock variants must be clearly shown in the UI.

### 3.5.3 Inventory Management

#### 3.5.3.1 Inventory Management Page

UI Layout: Inventory adjustment form and history table.

Description: Allows the Shop Owner to update stock quantities and record inventory logs. Related UC-12.

| Field Name | Description |
| --- | --- |
| Variant | Target product variant. |
| Change Type | Manual adjust, order reserve, order release, order confirm, or return. |
| Quantity Delta | Positive or negative stock change. |
| Quantity After | Stock after the change. |
| Note | Optional reason for adjustment. |

Checklist:
- Quantity After must be calculated correctly after each change.
- Change Type must match the inventory workflow.
- Every change must be logged.

### 3.5.4 Order and Support Operations

#### 3.5.4.1 Order Management Page

UI Layout: Order list, status filters, action buttons, and quick detail drawer.

Description: Lets the Shop Owner process orders from confirmation to handover. Related UC-13.

| Field Name | Description |
| --- | --- |
| Order Code | Order identifier. |
| Customer Name | Buyer name snapshot. |
| Payment Status | Pending, completed, failed, or expired. |
| Order Status | Current processing status. |
| Assigned Delivery | Delivery staff or partner if available. |
| Actions | Confirm, prepare, handover, or view detail. |

Checklist:
- The Shop Owner can process only orders belonging to their shop.
- Order status must follow the standard workflow.
- If payment is not completed, handover must be blocked.

#### 3.5.4.2 Chat Inbox Page

UI Layout: Conversation list, message panel, and attachment controls.

Description: Supports chat between Customer and Shop Owner regarding products or orders. Related UC-15.

| Field Name | Description |
| --- | --- |
| Conversation List | List of active chat threads. |
| Last Message | Recent message preview. |
| Unread Count | Unread messages. |
| Related Order | Order reference when chat is attached to an order. |
| Message Input | Text entry area. |

Checklist:
- Chat must be linked to the correct shop or related order.
- New messages must update in near real time.
- Users must be authorized for the thread.

### 3.5.5 Promotion Management

#### 3.5.5.1 Promotion Management Page

UI Layout: Promotion list with create/edit form.

Description: Lets the Shop Owner or Admin manage discount codes and promotion rules. Related UC-18.

| Field Name | Description |
| --- | --- |
| Code | Unique promotion code. |
| Discount Type | Percent or fixed amount. |
| Discount Scope | Shop-wide or all-platform scope. |
| Discount Value | Value of the discount. |
| Discount Max | Maximum discount cap. |
| Min Order Value | Minimum order amount to apply. |
| Valid From | Start time of the promotion. |
| Valid Until | End time of the promotion. |
| Max Uses | Optional usage limit. |
| Can Stack | Whether the voucher can stack. |

Checklist:
- Code must be unique.
- Expired promotions must stop applying automatically.
- If Can Stack is false, voucher combinations must be blocked.

### 3.5.6 Shop Dashboard and Reporting

#### 3.5.6.1 Shop Dashboard Page

UI Layout: KPI cards, sales chart, top products, and inventory alerts.

Description: Gives the Shop Owner a quick operational summary of the shop. Related UC-19.

| Field Name | Description |
| --- | --- |
| Revenue | Sales total for the selected period. |
| Order Count | Number of orders in the period. |
| Best Sellers | Top-selling products. |
| Inventory Alerts | Low-stock or out-of-stock warnings. |
| Settlement Status | Current settlement state for the shop. |

Checklist:
- KPI data must be filtered by a clear time period.
- Inventory alerts must reflect current stock.
- Dashboard data must belong to the current shop.

#### 3.5.6.2 Shop Report Page

UI Layout: Reports, charts, and export actions.

Description: Shows operational and sales reports for the shop owner. Related UC-19.

| Field Name | Description |
| --- | --- |
| Revenue Trend | Revenue over time. |
| Order Trend | Order volume trend. |
| Top Products | Best-performing products. |
| Return Rate | After-sales request ratio. |
| Export Action | Export report to file if enabled. |

Checklist:
- Reports must support period filtering.
- Export output must match the currently displayed data.

---

## 3.6 After-Sales, Review and Settlement

### 3.6.1 Return / Exchange Request

#### 3.6.1.1 Return / Exchange Request Page

UI Layout: Request form with order selection, evidence upload, and submit button.

Description: Lets the Customer create a cancel, return, or exchange request. Related UC-17.

| Field Name | Description |
| --- | --- |
| Order Code | Order being requested. |
| Order Item | Specific item if the request applies to one line item. |
| Request Type | Cancel, return, or exchange. |
| Reason Code | Wrong item, damaged, missing item, late delivery, not as described, or other. |
| Description | Detailed explanation of the issue. |
| Evidence URL | Link or uploaded proof image/video. |
| Requested Quantity | Number of items affected. |

Checklist:
- Request type must comply with policy.
- Evidence should be mandatory for dispute cases.
- Requests cannot be submitted outside the allowed time window.

#### 3.6.1.2 Request Review Page

UI Layout: Request detail view with approve/reject actions and decision notes.

Description: Used by Shop Owner or Admin to decide the request resolution. Related UC-17.

| Field Name | Description |
| --- | --- |
| Request Status | Requested, approved, rejected, processing, completed, or cancelled. |
| Decision Reason | Reason for approval or rejection. |
| Resolution Type | Refund, replacement, discount, or reject. |
| Refund Amount | Amount to refund if applicable. |
| Resolved At | Time when the request is finalized. |

Checklist:
- Rejection must include a clear reason.
- Refunds must show the exact amount.
- Processing states must follow the workflow rules.

### 3.6.2 Review

#### 3.6.2.1 Product Review Page

UI Layout: Star rating input, comment box, and submit button.

Description: Lets the Customer review a product after the order is completed. Related UC-16.

| Field Name | Description |
| --- | --- |
| Order Item | Item being reviewed. |
| Rating | Rating from 1 to 5. |
| Review Text | Optional comment text. |
| Visibility | Hidden or visible status managed by Admin. |

Checklist:
- Reviews are allowed only when the order is completed.
- Each order item can be reviewed only once.
- Hidden reviews must be controlled by Admin.

### 3.6.3 Settlement

#### 3.6.3.1 Settlement Dashboard Page

UI Layout: Settlement list, period filter, and settlement summary cards.

Description: Shows settlement calculations for each shop by period. Related UC-19.

| Field Name | Description |
| --- | --- |
| Period Start | Settlement period start date. |
| Period End | Settlement period end date. |
| Gross Amount | Total revenue before fees and refunds. |
| Platform Fee Amount | Platform fee deducted from shop revenue. |
| Refund Amount | Total refunded amount in the period. |
| Adjustment Amount | Manual or operational adjustment. |
| Net Amount | Final amount payable to the shop. |
| Status | Pending, confirmed, paid, or cancelled. |

Checklist:
- Settlement calculations must use valid data only.
- Period filter must be clear and consistent.
- Paid status must include payment confirmation details.

#### 3.6.3.2 Payout Confirmation Function

UI Layout: Non-UI financial confirmation function.

Description: Allows Admin to confirm payout and close a settlement after all conditions are met. Related UC-19.

| Parameter | Description |
| --- | --- |
| confirmed_at | Time of settlement confirmation. |
| paid_at | Time the payout was completed. |
| created_by | Admin or authorized finance user. |
| note | Optional payout note. |

Behavior:
- Confirmation is allowed only when settlement is eligible.
- The confirmation action must be audit logged.
- If payout fails, the system must preserve an appropriate retryable state.

---

## 3.7 Administration

### 3.7.1 Dashboard and User Management

#### 3.7.1.1 Admin Dashboard Page

UI Layout: KPI cards, alerts, recent activities, and shortcut cards.

Description: Gives the Admin a quick overview of users, shops, orders, settlement, and system exceptions.

| Field Name | Description |
| --- | --- |
| Pending Approvals | Number of pending shop or product approvals. |
| Exception Orders | Orders flagged for attention. |
| Settlement Alerts | Settlement items requiring action. |
| Recent Activities | Latest sensitive actions or system events. |
| Revenue Snapshot | High-level platform revenue summary. |

Checklist:
- Dashboard data must reflect system data at the appropriate time.
- Shortcut cards must lead to the correct admin screens.

#### 3.7.1.2 User Management Page

UI Layout: User table with filters by role and status.

Description: Allows Admin to review and manage all system users. Related UC-02 and UC-19.

| Field Name | Description |
| --- | --- |
| Full Name | User full name. |
| Email | Login email address. |
| Role | Customer, Shop Owner, Delivery Staff, or Admin. |
| Status | Active or inactive. |
| Locked Until | Lock expiration time if applicable. |

Checklist:
- Role and status filters must work correctly.
- Lock/unlock actions must be audited.
- The system must not allow permission changes beyond policy.

#### 3.7.1.3 User Detail Page

UI Layout: User profile, role controls, and security information panel.

Description: Shows full account information and administrative actions. Related UC-02.

| Field Name | Description |
| --- | --- |
| Profile Data | Name, phone, address, and other personal info. |
| Failed Login Count | Number of consecutive failed login attempts. |
| Email Verified | Verification status of the email address. |
| Account Status | Active or inactive. |

Checklist:
- Security information must be clearly visible.
- Role/status changes must be permission checked.

### 3.7.2 Approval and Category Management

#### 3.7.2.1 Shop Approval Queue Page

UI Layout: Queue table with filters, status badge, and quick action buttons.

Description: Shows pending shop registration requests for Admin processing. Related UC-10.

| Field Name | Description |
| --- | --- |
| Application List | Pending shop applications. |
| Submission Time | Time the application was created. |
| Requested By | User who submitted the request. |
| Approval Status | Pending, approved, rejected, or suspended. |
| Action | Open detail, approve, reject, or suspend. |

Checklist:
- Pending requests must be prioritized.
- Quick actions can be used only with the proper permission.

#### 3.7.2.2 Shop Approval Detail Page

UI Layout: Request detail, document preview, approval actions, and reason box.

Description: Used by Admin to approve, reject, or suspend a shop application. Related UC-10.

| Field Name | Description |
| --- | --- |
| Application ID | Unique request identifier. |
| Shop Name | Shop name being reviewed. |
| Supporting Documents | Uploaded documents for approval. |
| Approval Status | Pending, approved, rejected, or suspended. |
| Rejection Reason | Required when the application is rejected. |
| Approved At | Timestamp of approval. |

Checklist:
- Rejection reason is mandatory when denying a request.
- Documents must be previewable before the decision.
- Every decision must be audited.

#### 3.7.2.3 Category Management Page

UI Layout: Category table with search, sort, and active toggle.

Description: Allows Admin to manage the fruit category tree. Related UC-03 and UC-04.

| Field Name | Description |
| --- | --- |
| Name | Category name. |
| Slug | Unique URL-friendly identifier. |
| Display Order | Sorting order in the UI. |
| Is Active | Whether the category is visible to users. |

Checklist:
- Slug must be unique.
- Inactive categories must not appear in the storefront.

#### 3.7.2.4 Product Moderation Page

UI Layout: Moderation queue with product preview, approval actions, and reason box.

Description: Lets Admin review product content and apply moderation actions. Related UC-11 and UC-19.

| Field Name | Description |
| --- | --- |
| Product Name | Product title under review. |
| Shop Name | Shop that owns the product. |
| Moderation Status | Pending, approved, rejected, hidden, or restored. |
| Decision Reason | Reason for moderation action. |
| Reviewed At | Timestamp of review. |

Checklist:
- Moderation actions must include a reason when rejecting or hiding content.
- Approved products must satisfy the content and data rules.

### 3.7.3 Monitoring and Reporting

#### 3.7.3.1 Order Monitoring Page

UI Layout: Monitoring table with order, payment, and delivery status columns.

Description: Lets Admin monitor the system-wide order flow and payment lifecycle. Related UC-19.

| Field Name | Description |
| --- | --- |
| Order Code | Order identifier. |
| Payment Status | Pending, processing, completed, failed, cancelled, refunded, or expired. |
| Delivery Status | Delivery workflow status. |
| Exception Flag | Marks abnormal orders that need attention. |

Checklist:
- Exception flags must be easy to filter and prioritize.
- All statuses must reflect the real system data.

#### 3.7.3.2 Payment Monitoring Page

UI Layout: Payment table with transaction status and provider information.

Description: Lets Admin monitor payment processing, webhook results, and reconciliation outcomes.

| Field Name | Description |
| --- | --- |
| Payment ID | Unique payment transaction identifier. |
| Order Code | Related order reference. |
| Amount | Transaction amount. |
| Provider | Payment provider or bank reference. |
| Transaction Status | Pending, success, failed, refunded, or expired. |
| Dedup Status | Whether the webhook was deduplicated successfully. |

Checklist:
- Duplicate webhooks must be detected.
- Failed or expired payments must be clearly visible.

#### 3.7.3.3 Settlement Management Page

UI Layout: Settlement list, period filter, and settlement actions.

Description: Lets Admin review, confirm, and manage shop settlement lifecycle. Related UC-19.

| Field Name | Description |
| --- | --- |
| Period Start | Settlement period start date. |
| Period End | Settlement period end date. |
| Gross Amount | Total revenue before fees and refunds. |
| Platform Fee Amount | Platform fee deducted from shop revenue. |
| Refund Amount | Total refunded amount in the period. |
| Adjustment Amount | Manual or operational adjustment. |
| Net Amount | Final amount payable to the shop. |
| Status | Pending, confirmed, paid, or cancelled. |

Checklist:
- Settlement can only be closed when business conditions are met.
- Paid status must be synchronized with payout confirmation.

#### 3.7.3.4 Report Dashboard Page

UI Layout: Dashboard cards, charts, and export actions.

Description: Shows revenue, user growth, delivery performance, and return rate. Related UC-19.

| Field Name | Description |
| --- | --- |
| Revenue | Total platform or shop revenue. |
| User Growth | Growth trend of registered users. |
| Delivery Performance | Delivery success and failure metrics. |
| Return Rate | Number of after-sales requests and outcomes. |

Checklist:
- Reports must support period filtering.
- Export must match the displayed data.

#### 3.7.3.5 System Notification Page

UI Layout: Notification list, read/unread toggle, and send/action controls if enabled.

Description: Lets Admin monitor or manage system notifications sent to users.

| Field Name | Description |
| --- | --- |
| Type | ORDER_UPDATE, PROMOTION, SYSTEM, INVENTORY_ALERT, or PAYMENT. |
| Title | Notification title. |
| Message | Notification content. |
| Action URL | Destination when the user clicks the notification. |
| Is Read | Read/unread flag. |

Checklist:
- Notifications must display the correct type.
- Clicking a notification must route to the correct destination.

#### 3.7.3.6 Audit Log Page

UI Layout: Audit list with actor, action, target, and time filters.

Description: Lets Admin review sensitive operations across the system.

| Field Name | Description |
| --- | --- |
| Event Time | Time the action occurred. |
| Actor | User or system actor who performed the action. |
| Action | Operation that was executed. |
| Target | Object or record that was affected. |
| Result | Success or failure result. |
| IP Address | Origin IP for the action if tracked. |

Checklist:
- Audit logs must be immutable according to policy.
- Time and actor filters must support quick investigation.

### 3.7.4 Master Data Management

#### 3.7.4.1 Setting List Page

UI Layout: Setting list table with type filter, status filter, keyword search, and quick actions for creating or editing settings.

Description: Lets the Admin view, search, filter, sort, activate, deactivate, and maintain system master data settings.

| Field Name | Description |
| --- | --- |
| Id | Unique identifier of the setting record. |
| Name | Setting name shown in the list. |
| Type | Setting category or master data group. |
| Value | Setting value stored for the record. |
| Priority | Sort order or display priority. |
| Status | Active or inactive setting state. |
| Action | Edit, activate, or deactivate based on the current status. |

UI Components:
- Type filter dropdown
- Status filter dropdown
- Keyword search box
- Search button
- New Setting link
- Sortable table columns
- Pagination controls

Checklist:
- The Type filter must default to active setting types or show all allowed types.
- The Status filter must support All Statuses, Active, and Inactive.
- Keyword search must filter by setting name or value.
- Column sorting must support ascending and descending order.
- The Action column must show Edit plus Activate or Deactivate depending on the current status.
- New Setting and Edit must open the Setting Detail screen.

Field Description:

| Field Name | Description |
| --- | --- |
| (1) | Initial values: all active setting names with null or blank type. Hovering the mouse must show the field name: “Setting Type”. |
| (2) | Initial values: All Statuses, Active, Inactive, with the default value “All Status”. Hovering the mouse must show the field name: “Setting Status”. |
| (3) | The change-status action is Activate or Deactivate depending on the current status of the related setting record. |

#### 3.7.4.2 Setting Detail Page

UI Layout: Setting form with data fields, validation area, and save/cancel actions.

Description: Lets the Admin create a new setting or update an existing setting from the list page.

| Field Name | Description |
| --- | --- |
| Setting Type | Required master data category for the setting. |
| Setting Name | Required display name of the setting. |
| Setting Value | Required stored value. |
| Priority | Optional ordering value used for sorting. |
| Status | Active or inactive state. |

Checklist:
- The form must support both create and update operations.
- Required fields must be validated before save.
- Saving a setting must update the list immediately after success.
- Cancelling the form must return to the Setting List page without saving changes.

---

## 3.8 Non-UI Functions

### 3.8.1 Cart Synchronization Service

Description: Synchronizes the guest cart from local storage to the authenticated customer cart.

| Parameter | Description |
| --- | --- |
| guest_cart_payload | Cart data stored in the browser. |
| customer_id | Logged-in user identifier. |
| sync_time | Synchronization time. |

Checklist:
- The cart must sync after successful login.
- If data conflicts, the merge rule must be clear.

### 3.8.2 Inventory Reservation Service

Description: Reserves stock during checkout, releases stock on failure or cancellation, and updates inventory logs.

| Parameter | Description |
| --- | --- |
| order_id | Order being processed. |
| variant_id | Product variant affected. |
| reserved_quantity | Quantity held for the order. |
| released_quantity | Quantity returned to stock. |

Checklist:
- Stock reservation must be synchronized with checkout.
- Stock release must run when an order is cancelled or payment fails.

### 3.8.3 Notification Service

Description: Generates system notifications for orders, promotions, inventory alerts, payment events, and system messages.

| Parameter | Description |
| --- | --- |
| type | ORDER_UPDATE, PROMOTION, SYSTEM, INVENTORY_ALERT, or PAYMENT. |
| title | Notification title. |
| message | Notification content. |
| action_url | Destination when the user clicks the notification. |
| is_read | Read/unread flag. |

Checklist:
- Notifications must go to the correct recipient and type.
- action_url must be valid.

### 3.8.4 Recommendation Service

Description: Suggests products based on purchase history, seasonal availability, and user preferences.

| Parameter | Description |
| --- | --- |
| user_history | Historical purchase or browsing data. |
| seasonality_signal | Seasonal availability signal. |
| recommended_items | Suggested products returned to the UI. |

Checklist:
- Recommendations must prioritize seasonal and historical relevance.
- If personalization data is missing, fall back to featured products.

### 3.8.5 Settlement Batch Job

Description: Runs periodically to calculate shop settlement totals after the complaint window closes.

| Parameter | Description |
| --- | --- |
| period_start | Start date of the settlement window. |
| period_end | End date of the settlement window. |
| shop_count | Number of shops included in the batch. |
| batch_status | Running, success, or failed. |

Checklist:
- The job must run only when the data is eligible.
- If the job fails, a retry or alert mechanism must exist.
- The result must be synchronized with the settlement dashboard.

---

## 3.9 Appendix Notes for DOCX Conversion

- When exporting to Word, use heading styles for levels 3.1, 3.2, 3.3, etc. to generate the table of contents automatically.
- Keep the Field Name / Description tables intact so they can be pasted into DOCX without structural changes.
- If needed, this Section 3 can be split further into three files: Authentication, Commerce Flow, and Administration.
- This file has been standardized by screen/function to align with the UI checklist and the core SRS.

---

## 3.10 Context Diagram Components for Copy-Paste

### 3.10.1 System process label

| Component type | Copy-paste label |
| --- | --- |
| System process | `0. Online Fruit Shop System` |
| System boundary | `System boundary` |

### 3.10.2 External entities

| External entity | Copy-paste label | Meaning in the diagram |
| --- | --- | --- |
| Guest | `Guest` | Anonymous user who browses and prepares a temporary cart |
| Customer | `Customer` | Authenticated buyer who places orders and uses after-sales features |
| Shop Owner | `Shop Owner` | Merchant who manages shop, products, inventory, orders, promotions, and settlement |
| Delivery Staff | `Delivery Staff` | Fulfillment actor who updates delivery progress |
| Admin | `Admin` | System administrator who manages approval, moderation, monitoring, and master data |
| Payment Gateway / Bank | `Payment Gateway / Bank` | External payment provider that confirms or rejects transactions |
| Scheduler / Timer | `Scheduler / Timer` | Time-based trigger for settlement batch jobs |

### 3.10.3 Data flow labels from external entities to the system

| External entity | Data flow labels into system |
| --- | --- |
| Guest | `search criteria`, `keyword`, `category selection`, `price range`, `rating filter`, `shop region filter`, `sort order`, `registration data`, `login credentials`, `password reset request`, `verification code`, `guest cart data`, `guest cart snapshot` |
| Customer | `cart data`, `cart item quantity`, `shipping address`, `delivery time slot`, `order note`, `voucher code`, `payment method selection`, `payment request`, `cancel request`, `return request`, `exchange request`, `review content`, `star rating`, `chat message` |
| Shop Owner | `shop registration data`, `shop profile data`, `product master data`, `product description`, `origin data`, `harvest date`, `shelf life data`, `variant data`, `SKU data`, `price data`, `stock update`, `inventory adjustment data`, `order action`, `promotion data`, `chat reply`, `settlement note` |
| Delivery Staff | `assignment acceptance`, `pickup confirmation`, `delivery status update`, `failed delivery reason`, `proof of delivery` |
| Admin | `approval decision`, `rejection reason`, `suspension decision`, `moderation action`, `category data`, `user management data`, `setting data`, `monitoring query`, `settlement action`, `audit request` |
| Payment Gateway / Bank | `payment callback`, `transaction result`, `transaction reference`, `signature data`, `reconciliation response` |
| Scheduler / Timer | `batch trigger` |

### 3.10.4 Data flow labels from the system to external entities

| External entity | Data flow labels from system |
| --- | --- |
| Guest | `product catalog`, `product list`, `product detail data`, `search result list`, `featured product list`, `promotion banner data`, `recommendation data`, `authentication result`, `verification result`, `password reset result`, `cart state`, `validation message` |
| Customer | `order confirmation`, `order code`, `order summary`, `payment instruction`, `payment status`, `payment expiry`, `order history data`, `tracking data`, `status timeline`, `delivery information`, `return status`, `exchange status`, `review status`, `notification`, `chat thread` |
| Shop Owner | `order queue`, `order detail`, `product list`, `inventory state`, `sales summary`, `dashboard summary`, `report data`, `settlement summary`, `customer message`, `approval result`, `notification`, `chat thread` |
| Delivery Staff | `delivery assignment`, `order detail`, `route information`, `status timeline`, `delivery instruction` |
| Admin | `approval queue`, `dashboard summary`, `monitoring data`, `report data`, `alert data`, `audit log`, `setting detail`, `settlement list`, `notification` |
| Payment Gateway / Bank | `payment initiation`, `callback reference`, `reconciliation data` |
| Scheduler / Timer | `batch result`, `settlement status`, `error log` |

### 3.10.5 Standard labels to use on the diagram

- Use one system process label only: `0. Online Fruit Shop System`.
- Use one boundary label only: `System boundary`.
- Use external entity labels exactly as listed above.
- Use noun phrases for data flows, not verbs or screen names.
- Do not add internal services, database tables, controllers, or UI pages to the context diagram.

### 3.10.6 Minimal copy-paste set for a clean context diagram

If you want a compact diagram, copy only these items:

| Item | Label |
| --- | --- |
| Process | `0. Online Fruit Shop System` |
| Entities | `Guest`, `Customer`, `Shop Owner`, `Delivery Staff`, `Admin`, `Payment Gateway / Bank`, `Scheduler / Timer` |
| Guest in | `search criteria`, `registration data`, `login credentials`, `guest cart data` |
| Guest out | `product catalog`, `search result list`, `authentication result`, `cart state` |
| Customer in | `cart data`, `shipping address`, `payment request`, `review content`, `return request`, `chat message` |
| Customer out | `order confirmation`, `payment status`, `tracking data`, `notification` |
| Shop Owner in | `product master data`, `inventory adjustment data`, `order action`, `promotion data` |
| Shop Owner out | `order queue`, `dashboard summary`, `report data`, `settlement summary` |
| Delivery Staff in | `delivery status update`, `failed delivery reason` |
| Delivery Staff out | `delivery assignment`, `order detail` |
| Admin in | `approval decision`, `moderation action`, `monitoring query`, `settlement action` |
| Admin out | `approval queue`, `monitoring data`, `audit log`, `settlement list` |
| Payment Gateway in | `payment callback`, `transaction result` |
| Payment Gateway out | `payment initiation`, `reconciliation data` |
| Scheduler in | `batch trigger` |
| Scheduler out | `batch result`, `settlement status` |
