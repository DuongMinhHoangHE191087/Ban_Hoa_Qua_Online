# USE CASE SPECIFICATION MASTER DOCUMENT
## Online Fruit Shopping System (Marketplace Model)

**Project Name:** Online Fruit Shopping System  
**Author:** Duong Minh Hoang  
**Date Created:** May 27, 2026  
**Status:** Approved & Implemented  
**Version:** 2.1.0 (Enterprise Specification - Sorted)  

---

## 1. Document Overview & Purpose

This document serves as the **Single Source of Truth** for the Use Case Specifications of the **Online Fruit Shopping System**. It is designed to bridge the gap between business requirements (SRS / Business Rules Catalog) and the technical implementations of the Java 17, Servlet 6, JSP, and Tomcat 10 backend.

It details **exactly 50 Use Cases** mapping 100% to the **103 leaf features** in the system's Feature Tree and strictly enforcing all enterprise-level **Business Rules** (e.g., real-time stock reservation, 20km delivery radius check, 30kg express limit, VietQR SePay webhook idempotency, multi-shop order splitting, refund windows, and automated settlements).

Per the system design outline:
* **UC-01 to UC-20** represent the **20 Core Use Cases** mapped to primary user panels and front-end screens, keeping their IDs and sequence preserved.
* **UC-21 to UC-50** represent the **30 Advanced Use Cases**, capturing third-party integrations, background schedulers, administrative controllers, and edge-case exceptions.

All use cases follow the **Single Markdown Table** template from [use_case_example.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/docs/HoangDM/use_case_example.md) for professional display, easy copy-pasting, and compatibility with standard office suites.

---

## 2. Actor Definitions

The system utilizes six distinct human actors and two system-triggered external actors:

1. **Guest (Unauthenticated User):** An anonymous browser who can view products, search, filter, manage a local cart, and initiate a checkout.
2. **Customer (Authenticated User):** A registered buyer who manages addresses, syncs carts, performs payments, tracks orders, writes reviews, and requests refunds.
3. **Shop Owner (Merchant):** A registered seller who lists fruits, manages weight variants and batch-based inventory, processes orders, manages wallets, and withdraws funds.
4. **Delivery Staff (Shipper):** A registered logistics driver who accepts delivery trips, updates transit states, and uploads Proof of Delivery (POD).
5. **Admin (Platform Operator):** The administrator who verifies shops and products, moderates reviews, handles high-value disputes, checks audit logs, and monitors reports.
6. **System (Background Automation):** A proxy actor representing automated tasks, batch jobs, and internal services (such as the SePay payment gateway webhook, the 15-minute stock hold timer, and the 12-hour settlement holding scheduler).

---

## 3. Use Case Summary Matrix

| ID | Use Case | Actor | Priority | Description |
| :--- | :--- | :--- | :--- | :--- |
| UC-01 | Register Account | Guest | P0 | Register a Customer or Shop Owner account. |
| UC-02 | Login / Logout | Customer / Shop Owner / Delivery / Admin | P0 | Authenticate users (login/logout) in the system. |
| UC-03 | Browse Products | Guest | P0 | Browse product lists, categories, and details. |
| UC-04 | Search and Filter Products | Guest | P0 | Search and filter products by keyword, price, rating, location, and fruit type. |
| UC-05 | Manage Guest Cart | Guest | P0 | Store temporary shopping cart in localStorage/sessionStorage. |
| UC-06 | Manage Customer Cart | Customer | P0 | Add, edit, or delete items in the customer's shopping cart. |
| UC-07 | Place Order | Customer | P0 | Checkout and place fruit orders with stock reservation and multi-shop split. |
| UC-08 | Make Payment | Customer | P0 | Choose payment route (VietQR/COD) and execute the financial transaction. |
| UC-09 | Track Order | Customer | P0 | Check dynamic transit steps, coordinates, and delivery updates. |
| UC-10 | Shop Registration | Shop Owner | P0 | Submit shop details, warehouse address, and safety permits for verification. |
| UC-11 | Manage Products | Shop Owner | P0 | Perform product CRUD operations, variant setup, and Organic/Imported tags. |
| UC-12 | Manage Inventory | Shop Owner | P0 | Update stock counts, track logs, and handle low stock threshold warnings. |
| UC-13 | Confirm and Process Orders | Shop Owner | P0 | Accept or reject orders, and pack fresh fruits using FEFO warehouse picking. |
| UC-14 | Delivery Assignment and Update | Delivery Staff | P0 | Accept delivery trips and update order transit states to Delivered or Failed. |
| UC-15 | Chat Support | Customer / Shop Owner | P1 | Message directly to discuss produce freshness and coordinate delivery. |
| UC-16 | Review Product | Customer | P0 | Write star ratings and reviews for successfully delivered fruit items. |
| UC-17 | Cancel / Return / Exchange Request | Customer | P0 | Cancel pending unpaid orders or request refunds for damaged produce. |
| UC-18 | Manage Promotions | Shop Owner | P1 | Create discount vouchers, campaign budgets, and flash sales stock quotas. |
| UC-19 | Settlement Management | Shop Owner / Admin | P0 | Withdraw settled balances after the 12-hour holding window. |
| UC-20 | Recommendation | Guest / Customer | P1 | Browse Best Sellers and seasonal product recommendations to boost sales. |
| UC-21 | Google OAuth Sign-in & Sign-up | Guest | P0 | Onboard or log into Customer accounts instantly using Google credentials. |
| UC-22 | View and Edit Profile | User (All Roles) | P1 | Update profile information, phone numbers, and upload personal avatar images. |
| UC-23 | Manage Delivery Addresses | Customer | P0 | Save up to 5 shipping addresses, setting exactly 1 default address. |
| UC-24 | Change Password & Account Recovery | User (All Roles) | P0 | Alter passwords using BCrypt, or recover access using email tokens. |
| UC-25 | Sync Guest Cart to Customer Account | Customer | P0 | Automatically consolidate local guest carts upon successful user login. |
| UC-26 | Manage Product Wishlist | Customer | P2 | Add target fruits to wishlists and easily transfer items into carts. |
| UC-27 | Validate Delivery Address & Shipping Radius | Customer / System | P0 | Automatically verify shipping coordinates are within a 20km shop radius. |
| UC-28 | Select Delivery & Shipping Method | Customer | P0 | Select shipping methods, enforcing 30kg load limits and travel ETA. |
| UC-29 | Reorder Previous Purchase | Customer | P1 | Fast-checkout by cloning items directly from a past completed order. |
| UC-30 | Prepare Order & Manage Freshness Batches | Shop Owner | P0 | Pick warehouse batches using FEFO and check ±3% weight variance tolerance. |
| UC-31 | Shipper Pickup & Delivery Lifecycle | Delivery Staff | P0 | Accept dispatch runs and navigate routes using mobile map systems. |
| UC-32 | Order Delivery Confirmation & Proof | Delivery Staff | P0 | Submit photo handover proof, GPS coordinates, and confirm COD collections. |
| UC-33 | Handle Delivery Failures & SLA Exceptions | Delivery Staff / System | P0 | Mark delivery failures after 2 failed calls 5 mins apart, or weather stops. |
| UC-34 | Process Payment via Cash on Delivery (COD) | Customer | P0 | Checkout using COD, checking the 2,000,000 VND cap and failed strikes. |
| UC-35 | Process Payment via VietQR Dynamic QR | Customer | P0 | Scan dynamic VietQR code to trigger the 10-minute online payment timer. |
| UC-36 | Confirm Payment via SePay Webhook | System | P0 | Idempotently confirm payments by matching unique bank transfer references. |
| UC-37 | Handle Payment & Stock Exceptions | System | P0 | Resolve payment orphans and issue refunds for stock reservation failures. |
| UC-38 | Apply Promo Coupon Code & Budget Control | Customer / System | P0 | Apply discount coupons, enforcing stacking rules and atomic usage caps. |
| UC-39 | Moderate Reviews & Sentiment Control | Admin / System | P1 | Moderate customer comments and block fake self-reviews by device/IP. |
| UC-40 | Request Refund for Spoilage/Damaged Fruits | Customer | P0 | Request partial or full refunds within 6 hours with video/photo evidence. |
| UC-41 | Process Refund Request | Shop Owner / Admin | P0 | Review and resolve refund requests within the 12-hour SLA window. |
| UC-42 | Generate Digital Invoice & Download PDF | Customer / System | P1 | Generate and download printable invoices listing harvested batches. |
| UC-43 | Send Real-Time Push & In-App Notifications | System | P0 | Deliver WebSocket dynamic push alerts on critical order status changes. |
| UC-44 | Send Asynchronous Email & SMS Alerts | System | P0 | Send transactional security emails and temporary credentials asynchronously. |
| UC-45 | View Shop Wallet Settlement | Shop Owner / System | P0 | Audit daily pending/available balances and platform commission fees. |
| UC-46 | Manage Customer Accounts | Admin | P1 | Audit active customers, track COD blocks, and apply 90-day data masking. |
| UC-47 | Manage Shop Owner Verification | Admin | P0 | Review and approve/reject pending merchant registrations and documents. |
| UC-48 | Block & Unblock User Accounts | Admin | P0 | Block violating users, auto-hiding listings and cancelling pending orders. |
| UC-49 | Moderate Product Listings | Admin | P0 | Moderate pending product queues and verify imported organic permits. |
| UC-50 | View Global Operations & Revenue Dashboards | Admin | P1 | Monitor global platform GMV, commissions, and voucher cost allocations. |

---

## 4. Detailed Use Case Specifications (UC-01 to UC-50)

---

### PART A: 20 CORE USE CASES (PRESERVED SEQUENCE)

#### **UC-01: Register Account**

| ID and Name | UC-01 Register Account |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Authentication -> Account Access |
| **Primary Actors** | Guest |
| **Secondary Actors** | Admin (only when Shop Owner approval is required), Google OAuth System, Email Delivery Service |
| **Description** | Allows a Guest to create a new Customer or Shop Owner account to securely access authenticated features. It supports both manual registration and quick registration via Google OAuth. |
| **Preconditions** | - PRE-1: The user is not currently logged into the system.<br>- PRE-2: The registration page and Google OAuth API are functioning normally. |
| **Trigger** | The Guest clicks the "Register" button from the navigation bar or login page. |
| **Postconditions** | - POST-1: The system successfully saves the new account information into the database.<br>- POST-2: The account status is set to "Active" (Customers or Google registrations) or "Pending Verification" (Shop Owners awaiting Admin verification under rule USR-01). |
| **Normal Flow** | 1. The Guest opens the Register Page.<br>2. The system displays the registration form containing fields: Full Name, Email, Password, Confirm Password, Account Type (Customer / Shop Owner), and a "Register with Google" button.<br>3. The Guest enters valid information into the required fields.<br>4. The Guest selects the Account Type (Default is Customer).<br>5. The Guest clicks the "Register" button.<br>6. The system verifies data format, password strength (minimum 8 characters, 1 capital, 1 number), and Email uniqueness.<br>7. The system creates a new account, securely hashes the password using BCrypt, and persists it in the `users` table.<br>8. The system displays a success message and redirects the user to the Login Page. |
| **Alternative Flows** | **4.1. Registration via Google OAuth**<br>1. At step 3 of the Normal Flow, the Guest clicks the "Register with Google" button.<br>2. The system redirects the Guest to Google's authentication page.<br>3. The Guest logs in and grants basic profile access.<br>4. Google returns a secure Profile Token.<br>5. The system extracts the Email and Full Name. If the Email is not in the database, the system automatically creates a new Customer account with status "Active", generating a random password hash. It establishes a login session.<br>6. The system redirects the user directly to the Home Page.<br><br>**4.2. Shop Owner Account Registration**<br>1. At step 4 of the Normal Flow, the Guest selects the Account Type as "Shop Owner".<br>2. The Guest completes the registration form and submits.<br>3. The system creates the account but assigns the status as "Pending Verification" under Business Rule USR-01.<br>4. The system alerts the Admin regarding a new pending shop verification request and notifies the Guest: "Account created. Awaiting Admin approval of legal credentials." |
| **Exceptions** | **6.E1 Invalid or Missing Data**<br>1. At step 6 of the Normal Flow, validation fails (e.g., mismatched passwords, weak password, invalid email format).<br>2. The system highlights the erroneous fields and displays specific error messages. The Guest corrects the data and clicks "Register" again.<br><br>**7.E1 Email Already Exists**<br>1. At step 6 of the Normal Flow, the system finds the Email is already registered.<br>2. The system displays: "This email is already in use. Please log in or use a different email address." |
| **Priority** | High (P0 - Core functionality) |
| **Frequency of Use** | High. Daily usage, whenever new customers or shops join the platform. |
| **Business Rules** | - USR-01: Shop Owner accounts must be approved by an Admin before they can sell or withdraw.<br>- USR-05: Guest users must register or log in before they can proceed to checkout. |
| **Other Information** | Guests can register only as Customer or Shop Owner. Shippers and Admins are manually provisioned. |
| **Assumptions** | Email SMTP server is configured and functional for sending verification emails. |

---

#### **UC-02: Login / Logout**

| ID and Name | UC-02 Login / Logout |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Authentication -> Session |
| **Primary Actors** | Guest / Authenticated User (Customer, Shop Owner, Shipper, Admin) |
| **Secondary Actors** | None |
| **Description** | Handles the entire session cycle: allows a user to authenticate via local Email/Password to gain role-based secure panel access, and allows an authenticated user to completely invalidate their session to safely exit. |
| **Preconditions** | - PRE-1: For login, the user must have an active registered account.<br>- PRE-2: For logout, the user must currently possess a valid system session. |
| **Trigger** | The user clicks "Login" or "Logout" from the platform header. |
| **Postconditions** | - POST-1 (Login): Creates a secure HTTP session context containing User ID, Role, and metadata.<br>- POST-2 (Logout): Invalidates the session, destroys security cookies (JSESSIONID), and sets the role to Guest. |
| **Normal Flow** | **Part A - Login Flow:**<br>1. The Guest navigates to the Login Page.<br>2. The Guest enters their Email and Password and clicks "Login".<br>3. The system validates fields, checks password hash matches the database using BCrypt, and verifies account status is "Active".<br>4. The system creates a secure session and redirects the user based on role (Customer -> Home, Shop Owner -> Shop Dashboard, Admin -> Admin Panel, Shipper -> Delivery Panel).<br><br>**Part B - Logout Flow:**<br>1. The logged-in user clicks "Logout".<br>2. The system invalidates the session context on the server side.<br>3. The system deletes the browser session cookie.<br>4. The system redirects the user to the homepage, displaying a success toast. |
| **Alternative Flows** | **4.1. Lockout Check**<br>1. If a user fails to enter the correct password 5 consecutive times, the system enforces Business Rule SEC-01 (Lockout).<br>2. The system sets the account status to "Locked Out" for 15 minutes, blocking further login attempts. |
| **Exceptions** | **3.E1 Invalid Credentials**<br>1. At step 3 of Login, credentials do not match.<br>2. The system displays: "Invalid email or password" (protecting against account enumeration). |
| **Priority** | High (P0 - Core security) |
| **Frequency of Use** | Extremely High. Executed multiple times daily. |
| **Business Rules** | - USR-01: Shop Owners with Pending status are blocked from logging in.<br>- SEC-01: Auto lockout after 5 consecutive failed password attempts. |
| **Other Information** | Sessions automatically expire after 30 minutes of inactivity to protect shared terminals. |
| **Assumptions** | None |

---

#### **UC-03: Browse Products**

| ID and Name | UC-03 Browse Products |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Discovery -> Browsing |
| **Primary Actors** | Guest / Customer |
| **Secondary Actors** | None |
| **Description** | Allows Guest and Customer users to discover fruit products by opening the catalog page, browsing active seasonal items, and exploring categories (Citrus, Berries, Imported, Organic). |
| **Preconditions** | - PRE-1: Active fruit product listings exist in the database. |
| **Trigger** | The user opens the home page or clicks "Shop" in the navigation bar. |
| **Postconditions** | - POST-1: Renders a responsive grid of approved, active, and in-season fruit product cards. |
| **Normal Flow** | 1. The user navigates to the Shop Catalog page.<br>2. The system queries the `products` and `product_variants` tables to retrieve approved, active products.<br>3. The system checks seasonal availability: only items whose seasonal months match the current calendar date are shown (PRD-03).<br>4. The system renders the products as grid cards, showing name, image, average rating star, base price, discount price, and a quick "Add to Cart" CTA.<br>5. The user selects a category (e.g. "Citrus") from the navigation sidebar.<br>6. The system dynamically filters and displays only citrus fruit products. |
| **Alternative Flows** | **4.1. Empty Category display**<br>1. The selected category has no active fruits.<br>2. The system renders an elegant empty state: "No fresh items in this category currently. Browse our popular best sellers instead!" |
| **Exceptions** | None |
| **Priority** | High (P0 - Core discovery) |
| **Frequency of Use** | Extremely High. The primary entry page for conversions. |
| **Business Rules** | - PRD-03: Auto-filters out products whose Seasonal Availability is inactive for the current date.<br>- INV-03: Out-of-stock items (all weight variants = 0) are hidden from featured discovery. |
| **Other Information** | Catalog uses lazy-loading for images to ensure fast rendering. |
| **Assumptions** | None |

---

#### **UC-04: Search and Filter Products**

| ID and Name | UC-04 Search and Filter Products |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Discovery -> Filtering |
| **Primary Actors** | Guest / Customer |
| **Secondary Actors** | None |
| **Description** | Allows users to find specific fruits by entering search keywords, and refine massive catalog results using a side filter panel (Price range slider, star rating, availability, and tags). |
| **Preconditions** | - PRE-1: The user is on the Shop Catalog or search page. |
| **Trigger** | The user types a query in the Search bar or toggles sidebar filter criteria. |
| **Postconditions** | - POST-1: The product listing updates dynamically to match exact search/filter queries. |
| **Normal Flow** | 1. The user enters a search term (e.g., "Apple") in the top search bar and hits Enter.<br>2. The system executes a wildcard parameterized query: `LIKE %Apple%` on active listings.<br>3. The search results grid renders. The user toggles the sidebar filters:<br>   - Selects Category checkbox: "Imported Fruits" (PRD-02).<br>   - Drags the Price slider to set range: 50,000 VND to 150,000 VND.<br>   - Checks Star Rating: "4 Stars and Above".<br>4. The system executes a combined SQL query incorporating these parameters.<br>5. The system dynamically returns the matched fruits via AJAX, refreshing the catalog grid. |
| **Alternative Flows** | **4.1. Search yielded no matches**<br>1. The system finds 0 results for the query.<br>2. The system displays: "No fruits matched your criteria." and offers a "Reset Filters" CTA. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - PRD-02: Imported Fruits filter checks batch documents status.<br>- REV-02: Rating filter calculates average stars using only valid, unrejected reviews. |
| **Other Information** | Optimizes search speed by maintaining database indexes on `products.name`. |
| **Assumptions** | None |

---

#### **UC-05: Manage Guest Cart**

| ID and Name | UC-05 Manage Guest Cart |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Cart & Wishlist -> Guest Storage |
| **Primary Actors** | Guest |
| **Secondary Actors** | None |
| **Description** | Allows unauthenticated guests to add products, select specific weight variants, increment quantities, and manage their shopping cart using client-side LocalStorage. |
| **Preconditions** | - PRE-1: User is not authenticated.<br>- PRE-2: User browser has enabled LocalStorage permissions. |
| **Trigger** | The Guest clicks "Add to Cart" or modifies cart item quantities in the header. |
| **Postconditions** | - POST-1: Cart items array is successfully saved/updated inside LocalStorage (`guest_cart`). |
| **Normal Flow** | 1. The Guest views a product detail page, selects a variant (e.g., "Kiwi - 500g Box") and clicks "Add to Cart".<br>2. The client-side JS checks for existing LocalStorage keys under `guest_cart`. If empty, it initializes a JSON array.<br>3. The system inserts: `variant_id`, `product_id`, `quantity`, `price`, `title`, and `image_url` into the array.<br>4. If the item exists, the system increments the quantity.<br>5. The Guest opens the Cart page. The system parses LocalStorage and renders a checkout summary table (ORD-02 calculations).<br>6. The Guest clicks "+" or "-" to modify quantities, or clicks "Remove" to splice the item from the array. |
| **Alternative Flows** | None |
| **Exceptions** | **6.E1 LocalStorage Quota Exceeded**<br>1. If the browser's storage is full, the system catches the exception and falls back to saving session data in memory (SessionStorage). |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. Core onboarding feature. |
| **Business Rules** | - USR-05: Guest cart works for drafts, but checking out requires login/registration.<br>- CHK-01: Cart entries are drafts. Values are re-validated dynamically at checkout. |
| **Other Information** | Keeps server databases clean from junk traffic sessions. |
| **Assumptions** | None |

---

#### **UC-06: Manage Customer Cart**

| ID and Name | UC-06 Manage Customer Cart |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Cart & Wishlist -> Persistent Storage |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Governs persistent shopping cart management for authenticated Customers. Cart items are saved directly in the database, ensuring persistence across multiple customer devices. |
| **Preconditions** | - PRE-1: Customer is authenticated. |
| **Trigger** | The Customer adds a product variant or views their shopping cart. |
| **Postconditions** | - POST-1: Cart entries are written and modified in the database `cart_items` table. |
| **Normal Flow** | 1. The Customer clicks "Add to Cart" on a product detail page.<br>2. The system sends an AJAX POST request containing `variant_id` and `quantity` to the Cart Servlet.<br>3. The Servlet queries the `cart_items` table for the matching Customer ID and variant ID.<br>4. If found, it updates the quantity field. If not, it inserts a new record.<br>5. The Customer opens their Cart Page. The system queries the database, validates active stock, and renders the cart list.<br>6. The Customer modifies quantities. The server recasts calculations, updating the DB and returning a JSON response to update the UI totals. |
| **Alternative Flows** | **4.1. Out of Stock Adjustment**<br>1. When rendering the cart, the system detects a variant's stock has decreased below the cart's quantity.<br>2. Under rule CHK-06, the system automatically drops the cart item quantity to match available stock and renders a notification: "Quantity adjusted to match available stock." |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - CHK-01: Cart items are drafts and subject to real-time verification before checkout.<br>- CHK-06: Blocks checkout if stock falls below requested cart quantity. |
| **Other Information** | Database cart persists indefinitely until checkout or manual deletion. |
| **Assumptions** | None |

---

#### **UC-07: Place Order**

| ID and Name | UC-07 Place Order |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Checkout -> Inventory holding |
| **Primary Actors** | Customer |
| **Secondary Actors** | System |
| **Description** | The critical checkout step. Re-validates prices, allocates vouchers, reserves stock for exactly 15 minutes, splits multi-shop items into individual Child Orders, and generates payment targets. |
| **Preconditions** | - PRE-1: Delivery address is selected and validated (UC-27/28).<br>- PRE-2: Cart is not empty. |
| **Trigger** | The Customer clicks the "Place Order" button. |
| **Postconditions** | - POST-1: Parent and Child orders are persisted in `PENDING_PAYMENT` state.<br>- POST-2: Stock is reserved for exactly 15 minutes (INV-01). |
| **Normal Flow** | 1. The Customer clicks "Place Order" on the Checkout Page.<br>2. The system opens a **Database Transaction**:<br>   - Checks stock availability (CHK-01/06).<br>   - Locks variant stock in the `product_variants` table, timestamping the lock (INV-01).<br>   - If multi-shop items exist, splits the checkout into 1 Parent Order and multiple Child Orders grouped by Shop (CHK-02).<br>   - Allocates platform coupon discounts proportionally to each Child Order (CHK-04).<br>   - Computes total invoice (ORD-02 - minimum 0 VND).<br>3. The system saves the orders, triggers a background 15-minute expiration timer (INV-01), and redirects the Customer to the Payment Page. |
| **Alternative Flows** | **4.1. Cart Stock Shortage (Transactional Rollback)**<br>1. If stock is insufficient, the system cancels order creation under rule CHK-06.<br>2. The system rolls back the transaction, updates cart items, and displays: "Stock shortage occurred. Please reduce quantity to checkout." |
| **Exceptions** | **2.E1 Stock Lock Expiry**<br>1. The Customer fails to pay within 15 minutes.<br>2. The background timer fires, releases the stock (INV-01), cancels the transaction, and sets order status to `CANCELLED`. |
| **Priority** | High (P0 - Key Transactional Path) |
| **Frequency of Use** | Extremely High. Synced on every checkout. |
| **Business Rules** | - INV-01: Dynamic stock reservation for exactly 15 minutes.<br>- ORD-02: Total calculations must align exactly and never become negative.<br>- CHK-02: Multi-shop cart must split into Parent and Child orders.<br>- CHK-04: platform vouchers must allocate proportionally to Child Orders.<br>- CHK-06: Blocks order creation if available stock is lower than requested. |
| **Other Information** | Implemented using strict transaction isolation to prevent race conditions. |
| **Assumptions** | None |

---

#### **UC-08: Make Payment**

| ID and Name | UC-08 Make Payment |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Payment Management -> Online/COD |
| **Primary Actors** | Customer |
| **Secondary Actors** | Payment Gateway API, System |
| **Description** | Initiates the selected payment route (COD or Bank Transfer via VietQR). Sets up transactional sessions (10-minute timeout for online payment) and updates the payment states. |
| **Preconditions** | - PRE-1: An order has been successfully placed in `PENDING_PAYMENT` state (UC-07). |
| **Trigger** | The Customer selects a payment method and clicks "Pay Now" or "Confirm Order". |
| **Postconditions** | - POST-1: For VietQR, initiates status polling and renders the dynamic QR. For COD, updates status to `COD_CONFIRMED`. |
| **Normal Flow** | 1. The Customer selects the payment method on the Payment Panel.<br>2. **Route A: VietQR Bank Transfer** (UC-35/36/37):<br>   - Renders a dynamic QR code with integrated transfer reference (e.g. "FRUIT123").<br>   - Renders a 10-minute transfer expiration countdown (PAY-02).<br>   - Starts a 3-second AJAX status polling loop checking database transaction status.<br>3. **Route B: Cash on Delivery** (UC-34/36):<br>   - Checks COD limit (< 2M VND under PAY-01) and customer block status (SEC-01).<br>   - Updates order status to `COD_CONFIRMED`. |
| **Alternative Flows** | None |
| **Exceptions** | **2.E1 COD Blocked**<br>1. If customer failed delivery count is > 3 in 30 days (SEC-01), the system blocks the COD option and forces online payment. |
| **Priority** | High (P0 - Key Revenue Gate) |
| **Frequency of Use** | Extremely High. Executed on every checkout. |
| **Business Rules** | - PAY-01: COD limits transactions to below 2,000,000 VND.<br>- PAY-02: Online payment sessions must complete within 10 minutes.<br>- SEC-01: Disables COD option if customer abuse threshold is exceeded. |
| **Other Information** | Mapped securely to gateway webhooks for automatic status reconciliation. |
| **Assumptions** | None |

---

#### **UC-09: Track Order**

| ID and Name | UC-09 Track Order |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Delivery Management -> Transit |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Allows Customers to monitor their order fulfillment process in real-time, checking status transitions, estimated delivery travel times (ETA), and shipper delivery proofs. |
| **Preconditions** | - PRE-1: The Customer is authenticated.<br>- PRE-2: An order has been successfully paid or COD-confirmed. |
| **Trigger** | The Customer clicks "Track Order" on their Order History dashboard. |
| **Postconditions** | - POST-1: Renders the order tracking timeline showing step-by-step progress. |
| **Normal Flow** | 1. The Customer opens their Order History page and clicks "Track Order" next to an active order.<br>2. The system queries order, payment, and shipment status records from the database.<br>3. The system renders the tracking panel containing:<br>   - Stepper Timeline: Pending -> Paid -> Processing -> Preparing -> Ready -> Shipped -> Delivered.<br>   - Shipper profile widget (Name, Phone number, vehicle photo) once a driver is assigned.<br>   - Dynamic ETA: calculated preparation plus maps travel time (DEL-03).<br>4. The Customer views the real-time status of their package. |
| **Alternative Flows** | **4.1. Order Completed (Proof Display)**<br>1. Once the order reaches `DELIVERED` status, the tracking timeline unlocks the Proof of Delivery (POD) display.<br>2. The Customer can view the uploaded handover picture and delivery timestamp. |
| **Exceptions** | None |
| **Priority** | High (P0 - Customer Satisfaction) |
| **Frequency of Use** | Extremely High. Checked repeatedly by customers awaiting express fruits. |
| **Business Rules** | - DLV-04: Mapped directly to the shipper delivery lifecycle state machine.<br>- DEL-03: ETA is dynamically calculated from preparation and coordinates check. |
| **Other Information** | Tracking details are private and require secure customer authorization. |
| **Assumptions** | None |

---

#### **UC-10: Shop Registration**

| ID and Name | UC-10 Shop Registration |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Shop Onboarding -> Profile |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | Admin |
| **Description** | Governs merchant onboarding. Allows a Guest or Customer to apply for a Shop Owner account by uploading business identity documents, warehouse coordinates, and food safety permits. |
| **Preconditions** | - PRE-1: The applicant possesses valid credentials and is not currently blocked. |
| **Trigger** | The user clicks "Register a Shop" from the dashboard footer or profile menu. |
| **Postconditions** | - POST-1: Creates a new Shop record in the database in a "Pending Verification" state (USR-01). |
| **Normal Flow** | 1. The applicant opens the Shop Registration page.<br>2. The system renders a registration form requesting: Shop Name, Phone, Email, Warehouse coordinate address, ID Card upload, Tax Code, and Food Safety Certificate upload.<br>3. The applicant fills the fields, autocomplete resolves their warehouse address via Maps API (USR-03), and uploads citizen/safety documents.<br>4. The applicant clicks "Submit Registration".<br>5. The system performs input validation, hashes credentials, and inserts the Shop record with `status = 'PENDING_VERIFICATION'`.<br>6. The system notifies the applicant that their shop is pending Admin verification, and alerts the Admin queue. |
| **Alternative Flows** | None |
| **Exceptions** | **5.E1 Missing Legal Documents**<br>1. The applicant submits without uploading required certificates.<br>2. The system blocks the request, highlighting missing fields, and displays: "Uploading safety certificates is legally required." |
| **Priority** | High (P0 - Merchant Onboarding) |
| **Frequency of Use** | Low. Executed once per merchant. |
| **Business Rules** | - USR-01: Shop Owner accounts are created as "Pending Verification" and require Admin approval to activate.<br>- USR-03: Warehouse address must be validated using Maps API to extract precise coordinates. |
| **Other Information** | Once approved by Admin (UC-47), the merchant is permitted to upload products. |
| **Assumptions** | None |

---

#### **UC-11: Manage Products**

| ID and Name | UC-11 Manage Products |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Product Management -> Listings |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | Admin (for approval moderation) |
| **Description** | Allows a verified Shop Owner to perform CRUD operations on their listings: adding new fruits with weight variants, editing descriptions/images, and deleting or hiding products. |
| **Preconditions** | - PRE-1: The merchant's shop is verified and Active (SHP-01). |
| **Trigger** | The Shop Owner clicks "Product Management" on their dashboard. |
| **Postconditions** | - POST-1: Product and variant records are inserted, modified, or soft-deleted in the database. |
| **Normal Flow** | 1. The Shop Owner enters the Product Management dashboard.<br>2. To **Add Product**, the merchant selects "Add New", fills the description, category, weight variants (PRD-01), uploads images, and clicks submit. The product goes to `PENDING_APPROVAL` status.<br>3. To **Edit Product**, the merchant clicks edit, alters fields (e.g. updating description), and saves. Listings remain Active.<br>4. To **Hide Product**, the merchant toggles the visibility switch. The product is set to `HIDDEN`, hiding it from storefront search.<br>5. To **Delete Product**, the merchant clicks delete. The system executes a soft delete by setting `status = 'DELETED'` to preserve historical data references (DAT-02). |
| **Alternative Flows** | **4.1. Sensitive Edits (Category/Name Modification)**<br>1. If a merchant changes core fields like Name or Category to Organic/Imported, the system triggers re-verification (PRD-02).<br>2. The listing `status` reverts to `PENDING_APPROVAL` and is hidden from public sale until Admin approved. |
| **Exceptions** | **2.E1 Compliance Hold Block**<br>1. If the shop is placed on `ComplianceHold` (SHP-02) due to expired credentials, the system blocks product addition/modification actions. |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. Active as inventory catalogs change. |
| **Business Rules** | - PRD-01: Fruit products must have weight-based variants. selling by count alone is blocked.<br>- PRD-02: Organic/Imported tags require batch document uploads.<br>- SHP-02: Suspended/Hold shops are blocked from managing products.<br>- DAT-02: Soft deletions are enforced to maintain database reference integrity. |
| **Other Information** | Soft deleted items are excluded from catalog indexing but kept in SQL tables. |
| **Assumptions** | None |

---

#### **UC-12: Manage Inventory**

| ID and Name | UC-12 Manage Inventory |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Product Management -> Stock Control |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | System |
| **Description** | Enables merchants to track their warehouse fruit stock in real-time, record restocking batch logs, configure low-stock thresholds, and receive automated system warning alerts. |
| **Preconditions** | - PRE-1: Product variants are mapped to a specific warehouse location (SHP-05). |
| **Trigger** | The Shop Owner clicks "Inventory Control", or a purchase decreases stock below limits. |
| **Postconditions** | - POST-1: Stock quantities are modified in the inventory tables. Logs are generated. |
| **Normal Flow** | 1. The Shop Owner enters the "Inventory Dashboard".<br>2. The system renders the current available stock for each variant, showing current weight (kg) and alert thresholds.<br>3. The Shop Owner inputs a restock weight of 100kg for a Fuji Apple variant, selecting the inbound batch number.<br>4. The system validates the input, updates the available stock, and registers the transaction in `inventory_logs`. |
| **Alternative Flows** | **4.1. Automated Low Stock Alert (System triggered)**<br>1. A Customer checkout decreases stock of Kiwi to 4kg.<br>2. The system checks the Kiwi threshold (5kg). Because current stock is below threshold, rule INV-02 triggers.<br>3. The system pushes a push notification to the merchant: "Low stock alert: Kiwi is below 5kg. Please restock."<br><br>**4.2. Out of Stock Hiding (System triggered)**<br>1. If stock drops to 0, the system triggers INV-03.<br>2. The system automatically hides the product from recommendations and homepage best sellers. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. Automatically run on every order. |
| **Business Rules** | - INV-02: Triggers alerts to merchants when inventory falls below minimum threshold.<br>- INV-03: Hides out-of-stock products from recommendations and best seller grids.<br>- SHP-05: Products must map to a warehouse to support proximity checking. |
| **Other Information** | Prevents overselling of fresh produce. |
| **Assumptions** | None |

---

#### **UC-13: Confirm and Process Orders**

| ID and Name | UC-13 Confirm and Process Orders |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Order Processing -> Acceptance |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | System, Delivery Staff |
| **Description** | Governs the merchant's first order response. The Shop Owner must accept/reject orders within a 10-minute SLA window, prepare packages using FEFO batch picking, and manage weight variances. |
| **Preconditions** | - PRE-1: Order is in status `PAID` or `COD_CONFIRMED` and belongs to this shop. |
| **Trigger** | The Shop Owner is alerted of a new order on their dashboard. |
| **Postconditions** | - POST-1: Order status is updated to `PROCESSING` (accepted) or `READY_FOR_PICKUP` (packed). |
| **Normal Flow** | 1. The Shop Owner opens their dashboard and clicks "Accept Order". The order moves to `PROCESSING` (ORD-01).<br>2. The staff opens the picking sheet. The system suggests batches using **FEFO** (First Expired, First Out) under rule FRH-02.<br>3. The staff weighs and packs the fruits, entering the actual weight in the system.<br>4. The system validates the weight variance is within ±3% tolerance under rule FRH-07.<br>5. The Shop Owner clicks "Mark Ready for Pickup".<br>6. The order status updates to `READY_FOR_PICKUP`. |
| **Alternative Flows** | **4.1. SLA Auto-Cancel (System triggered)**<br>1. If the merchant does not accept an express order within 10 minutes (OLC-03), the system auto-cancels it, releases the stock, and refunds the customer.<br><br>**4.2. Shortage & Substitution Proposal**<br>1. If a stock shortage occurs, the merchant proposes a substitute fruit under rule OLC-05.<br>2. The Customer receives a notification. If approved, prices adjust under OLC-07; if not responded in 10 minutes, the item is cancelled and refunded (OLC-06). |
| **Exceptions** | **5.E1 Variance Over 3%**<br>1. If packed weight is 5% below target, the system calculates a shortage refund under FRH-07, updates the invoice, and lets the order proceed. |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. Run on every purchase. |
| **Business Rules** | - ORD-01: Customer cannot cancel once shop is processing.<br>- OLC-03: Auto-cancels express orders not accepted within 10 minutes.<br>- FRH-02: Enforces FEFO batch picking rules.<br>- FRH-07: Standard weight variance allowance is ±3%. Shortages beyond this are refunded.<br>- OLC-05 / OLC-06 / OLC-07: Governs product substitution pricing and responses. |
| **Other Information** | Critical for maintaining express delivery timelines. |
| **Assumptions** | None |

---

#### **UC-14: Delivery Assignment and Update**

| ID and Name | UC-14 Delivery Assignment and Update |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Delivery Management -> Transit |
| **Primary Actors** | Delivery Staff (Shipper) |
| **Secondary Actors** | Customer, System |
| **Description** | Governs the driver's delivery cycle: accepting assigned orders, picking up items at the shop, navigating routes, and updating transit states (AwaitingPickup -> InTransit -> Arrived). |
| **Preconditions** | - PRE-1: Driver is logged into their delivery application.<br>- PRE-2: Order is in status `READY_FOR_PICKUP`. |
| **Trigger** | The driver accepts an available delivery trip in the queue. |
| **Postconditions** | - POST-1: Transit states are updated sequentially in the database (DLV-04). |
| **Normal Flow** | 1. The Shipper views the delivery queue and clicks "Accept Trip". The status updates to `AWAITING_PICKUP`.<br>2. The Shipper arrives at the warehouse, verifies order ID, picks up the package, and clicks "Pick Up". The status updates to `IN_TRANSIT`.<br>3. The system renders the map routing to the customer's coordinates.<br>4. The Shipper arrives at the customer's home and clicks "Arrived". The status updates to `ARRIVED`. |
| **Alternative Flows** | **4.1. Driver Reassignment**<br>1. The assigned driver does not pick up the package within the configured limit.<br>2. The system triggers rule DLV-03, automatically reassigning another driver (up to 3 times). |
| **Exceptions** | **2.E1 Weight Load Exceeded**<br>1. If cart weight exceeds 30kg, express delivery is blocked at checkout (DEL-02). |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - DEL-02: Express motorbike load weight limit is capped at 30kg.<br>- DLV-03: System reassigns shippers up to 3 times if they fail to pick up.<br>- DLV-04: Delivery states must flow sequentially: AwaitingPickup -> InTransit -> Arrived -> Delivered. |
| **Other Information** | GPS coordinates are synced to the customer map tracking page. |
| **Assumptions** | Driver mobile GPS is functional. |

---

#### **UC-15: Chat Support**

| ID and Name | UC-15 Chat Support |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Support -> Communications |
| **Primary Actors** | Customer / Shop Owner |
| **Secondary Actors** | None |
| **Description** | Renders a real-time messaging interface between Customer and Shop Owner to discuss product quality, ripeness details, or coordinate specific delivery directions. |
| **Preconditions** | - PRE-1: Both users have registered accounts in the system. |
| **Trigger** | The Customer clicks "Chat with Shop" on a product detail or order page. |
| **Postconditions** | - POST-1: Establishes a WebSocket chat session, saving message logs. |
| **Normal Flow** | 1. The Customer clicks "Chat with Shop".<br>2. The system opens a chat panel, pre-populating a reference card of the active fruit product or order.<br>3. The Customer types: "How ripe are the avocados currently?" and clicks Send.<br>4. The system validates text, saves the message in `chat_messages` table, and sends a WebSocket message.<br>5. The Shop Owner receives a desktop notification, opens their chat dashboard, and replies. |
| **Alternative Flows** | **4.1. Offline Messaging**<br>1. The Shop Owner is currently offline.<br>2. The system saves the message and flags the shop dashboard with an unread indicator, queuing an in-app alert. |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | High. Frequently used before placing custom fruit orders. |
| **Business Rules** | - USR-04: Chat logs can be audited by Admins during dispute or manipulation investigations. |
| **Other Information** | Promotes merchant-buyer relations and reduces cancellation rates. |
| **Assumptions** | None |

---

#### **UC-16: Review Product**

| ID and Name | UC-16 Review Product |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Review & Rating -> Feedback |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Allows Customers to write text reviews and rate fruits with stars (1-5). Review eligibility is strictly restricted to verified purchases of that product in successfully delivered orders. |
| **Preconditions** | - PRE-1: Customer is authenticated.<br>- PRE-2: Order is marked as `DELIVERED` or `COMPLETED`. |
| **Trigger** | The Customer clicks "Write Review" next to a delivered item. |
| **Postconditions** | - POST-1: Review is stored in database in a pending moderation state. |
| **Normal Flow** | 1. The Customer opens their delivered order history.<br>2. The system displays a "Review Product" button next to the purchased fruits.<br>3. The Customer clicks the button.<br>4. The system opens a modal prompting for: Star Rating (1-5) and comment text.<br>5. The Customer enters 5 stars, writes a review, and submits.<br>6. The system verifies review eligibility under rule REV-01 and saves the review. |
| **Alternative Flows** | None |
| **Exceptions** | **6.E1 Verification Failure**<br>1. An unverified customer attempts to post a review for an unpurchased fruit.<br>2. Under rule REV-01, the system blocks the database insert and returns a security warning. |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - REV-01 / RVR-04: Reviews are strictly locked until the customer has purchased and received the product. |
| **Other Information** | Prevents competitive review bombing or fake merchant rating boosts. |
| **Assumptions** | None |

---

#### **UC-17: Cancel / Return / Exchange Request**

| ID and Name | UC-17 Cancel / Return / Exchange Request |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | After-sales -> Claim |
| **Primary Actors** | Customer |
| **Secondary Actors** | Shop Owner, Admin |
| **Description** | Enables a Customer to cancel an unpaid order, or submit a refund request for spoiled/damaged fresh fruits within exactly 6 hours from delivery, uploading photo or video proof. |
| **Preconditions** | - PRE-1: For refunds, the order status must be `DELIVERED` and within the 6-hour limit (REF-01).<br>- PRE-2: For cancellation, the order must be `PENDING` (ORD-01). |
| **Trigger** | The Customer clicks "Cancel Order" or "Request Refund" in their dashboard. |
| **Postconditions** | - POST-1: The order status is set to `CANCELLED`, or a refund case is registered. |
| **Normal Flow** | **Part A - Cancellation:**<br>1. The Customer clicks "Cancel Order" on an unpaid or pending order.<br>2. The system checks order status is `PENDING` (ORD-01). It sets status to `CANCELLED`, releasing the reserved stock.<br><br>**Part B - Refund Claim:**<br>1. The Customer clicks "Request Refund" within 6 hours of delivery (REF-01).<br>2. The system renders a form asking for: refund type (partial/full), reason, and proof upload.<br>3. The Customer uploads a video of bruised fruit and submits.<br>4. The system verifies that photo/video proof exists under rule REF-02, and saves the claim. |
| **Alternative Flows** | **4.1. Cancellation Blocked**<br>1. The Customer tries to cancel but the shop has updated status to `PROCESSING`.<br>2. Under rule ORD-01, the system disables the cancellation action and displays: "Merchant has started processing. Cancellation blocked." |
| **Exceptions** | **2.E1 6-Hour Refund Expired**<br>1. The Customer tries to request a refund 8 hours post-delivery.<br>2. Under rule REF-01, the system has disabled the button, and blocks further submissions. |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - ORD-01: Customers can cancel only in Pending status. Processing onwards blocks cancellations.<br>- REF-01: Fresh produce refund claims are locked exactly 6 hours post-delivery.<br>- REF-02: Refund requests require photo or video proof. Physical return is bypassed by default to save logistics fees. |
| **Other Information** | Direct refund without physical return protects margins and maintains buyer trust. |
| **Assumptions** | None |

---

#### **UC-18: Manage Promotions**

| ID and Name | UC-18 Manage Promotions |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Promotion System -> Setup |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | Admin |
| **Description** | Allows merchants to create discount vouchers, set up minimum order value thresholds, allocate flash sales quotas, and manage promotional campaign budgets. |
| **Preconditions** | - PRE-1: Shop account is Active (SHP-01). |
| **Trigger** | The Shop Owner clicks "Promotion Panel" on their Merchant Dashboard. |
| **Postconditions** | - POST-1: New coupons or flash sale schedules are persisted in the database. |
| **Normal Flow** | 1. The Shop Owner clicks "Create Coupon".<br>2. The system renders a configuration form: code, discount amount, expiry date, maximum usages, and total budget limit (PRO-03).<br>3. The Shop Owner enters data and clicks "Publish".<br>4. The system validates fields, maps the voucher funding source directly to the Shop Owner's account (COU-07), and saves. |
| **Alternative Flows** | **4.1. Flash Sale Allocation**<br>1. The Shop Owner clicks "Setup Flash Sale".<br>2. The merchant selects a product, sets the promotional price, and allocates a campaign stock quota (e.g. 15kg) under rule COU-06.<br>3. The system isolates the campaign stock from regular warehouse stock. |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Low. Managed during shop promotional cycles. |
| **Business Rules** | - PRO-03 / COU-02: Vouchers automatically deactivate on expiry or when budget caps are reached.<br>- COU-06: Flash sale campaign stock must be separately allocated and capped.<br>- COU-07: Shop-funded vouchers are deducted from merchant sales; platform vouchers map to marketing cost. |
| **Other Information** | Helps merchants boost sales dynamically while avoiding budget overflows. |
| **Assumptions** | None |

---

#### **UC-19: Settlement Management**

| ID and Name | UC-19 Settlement Management |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Financial Management -> Wallet |
| **Primary Actors** | Shop Owner / Admin |
| **Secondary Actors** | System (Automated Scheduler) |
| **Description** | Manages merchant revenue settlements. Funds are held for exactly 12 hours from delivery, platform commissions are deducted, and verified merchants can request bank withdrawals. |
| **Preconditions** | - PRE-1: Shop profile is verified and not under compliance hold (SHP-02). |
| **Trigger** | The Shop Owner requests a withdrawal, or the automated 12-hour scheduler fires. |
| **Postconditions** | - POST-1: Merchant wallet ledgers are updated. Bank payout transfers are queued. |
| **Normal Flow** | **Part A - Automated Settlement:**<br>1. The system scheduler runs every hour, identifying orders delivered > 12 hours ago with no open disputes.<br>2. Under rule PAY-03 and FIN-04, the system calculates net revenue (Invoice - 5% Platform commission).<br>3. The system transfers the funds from `PendingBalance` to `AvailableBalance`, setting order state to `COMPLETED` (OLC-08).<br><br>**Part B - Wallet Withdrawal:**<br>1. The Shop Owner clicks "Withdraw", enters 1,000,000 VND, and submits.<br>2. The system checks validation (FIN-08): balance is >= 1,000,000 VND, shop is not under compliance hold.<br>3. The system executes the payout and logs the transaction. |
| **Alternative Flows** | None |
| **Exceptions** | **3.E1 Compliance Hold Block**<br>1. The shop has expired safety documents, placing it under ComplianceHold (SHP-02).<br>2. Under rule FIN-08, the system blocks the withdrawal, forcing certificate renewal. |
| **Priority** | High (P0 - Key Revenue Integrity) |
| **Frequency of Use** | High. Automatically run hourly; checked weekly by merchants. |
| **Business Rules** | - PAY-03: Sales proceeds are locked in pending balance for exactly 12 hours post-delivery.<br>- SHP-02: Compliance holds block merchants from withdrawing funds.<br>- FIN-03 / FIN-04: Funds settle only after order completion. Commission isolation is enforced.<br>- FIN-08: Withdrawals require active credentials, positive balance, and no open holds. |
| **Other Information** | Prevents platform capital loss by maintaining a dispute buffer window. |
| **Assumptions** | None |

---

#### **UC-20: Recommendation**

| ID and Name | UC-20 Recommendation |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Discovery -> Merchandising |
| **Primary Actors** | Guest / Customer |
| **Secondary Actors** | None |
| **Description** | Renders personalized product recommendation carousels (Best Sellers, Related Fruits, and Recently Viewed) on the homepage and details pages, filtering out unavailable or out-of-stock items. |
| **Preconditions** | - PRE-1: Active fruit product listings exist in the database. |
| **Trigger** | The user loads the homepage or opens a product detail page. |
| **Postconditions** | - POST-1: Renders customized recommendation carousels showing high-conversion fruits. |
| **Normal Flow** | 1. The user opens the Fruit Shop homepage.<br>2. The system fetches the "Best Sellers" list by joining products with completed sales volume metrics (`orders` and `order_items` tables).<br>3. The system filters the results to ensure that all variants have available stock (INV-03).<br>4. The system fetches "Recommended Fruits" matching seasonal periods and active tags.<br>5. The system renders the dynamic carousels. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - INV-03: Completely out of stock products (all weight variants = 0) must be hidden from recommendations.<br>- REV-02: Star ratings displayed in carousels must exclude reviews rejected or flagged by Admin. |
| **Other Information** | Uses session cookies to track "Recently Viewed" lists to avoid database read latency. |
| **Assumptions** | None |

---

### PART B: 30 ADVANCED & EXCEPTION USE CASES

#### **UC-21: Google OAuth Sign-in & Sign-up**

| ID and Name | UC-21 Google OAuth Sign-in & Sign-up |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Authentication -> Third-Party Access |
| **Primary Actors** | Guest |
| **Secondary Actors** | Google OAuth System (Google API) |
| **Description** | Enables a Guest to quickly register a new Customer account or log into an existing account using their Google credentials, automatically extracting profile details. |
| **Preconditions** | - PRE-1: The user is unauthenticated.<br>- PRE-2: Google OAuth credentials are active on the server. |
| **Trigger** | The Guest clicks the "Login with Google" button on login/register pages. |
| **Postconditions** | - POST-1: Establishes a logged-in session, creating a Customer profile if it is their first visit. |
| **Normal Flow** | 1. The user clicks "Login with Google".<br>2. The system redirects to the Google login page.<br>3. The user authenticates and Google returns a secure ID profile token.<br>4. The system decodes the token, retrieving `email` and `sub` (Google ID).<br>5. The system checks database: if account exists, establishes login session and redirects.<br>6. If account does not exist, system auto-creates a Customer profile with status "Active", hashes a random password, logs them in, and redirects to home page. |
| **Alternative Flows** | None |
| **Exceptions** | **5.E1 Email Conflict**<br>1. The email exists but is tied to a local Admin or Shop Owner account.<br>2. The system blocks login, displaying: "Please login using your standard password credentials." |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - USR-05: Google registered users are active and can proceed to checkout.<br>- COU-04: Enforces IP/Device risk controls during quick social registration. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-22: View and Edit Profile**

| ID and Name | UC-22 View and Edit Profile |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | User Profile -> Information |
| **Primary Actors** | User (Customer, Shop Owner, Shipper, Admin) |
| **Secondary Actors** | None |
| **Description** | Allows authenticated users to view their account details (name, email, phone, avatar) and modify updateable personal information (excluding email). |
| **Preconditions** | - PRE-1: User is logged into the system. |
| **Trigger** | The user clicks "My Account" in their dashboard. |
| **Postconditions** | - POST-1: User profile details are modified in the database. |
| **Normal Flow** | 1. The user navigates to their profile page.<br>2. The system renders their profile fields. Email is kept read-only.<br>3. The user edits their Full Name and Phone number, and uploads a new profile avatar.<br>4. The user clicks "Save Changes".<br>5. The system validates the formats (Phone must be a valid 10-digit Vietnamese number).<br>6. The system updates the database and displays a success toast. |
| **Alternative Flows** | None |
| **Exceptions** | **5.E1 Phone Invalid**<br>1. At step 5, the phone format is incorrect.<br>2. The system rejects the update, prompting correct input formats. |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Low. |
| **Business Rules** | - USR-02: Shipping addresses are isolated from core profile information. |
| **Other Information** | Keeps historical audits of user name changes. |
| **Assumptions** | None |

---

#### **UC-23: Manage Delivery Addresses**

| ID and Name | UC-23 Manage Delivery Addresses |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | User Profile -> Addresses |
| **Primary Actors** | Customer |
| **Secondary Actors** | Google Maps API |
| **Description** | Allows Customers to save a maximum of 5 delivery addresses. Addresses are geocoded via Google Maps API to register precise coordinates for proximity checks. |
| **Preconditions** | - PRE-1: Customer is authenticated.<br>- PRE-2: Map Service API is online. |
| **Trigger** | The Customer navigates to "Manage Addresses" in their Profile settings. |
| **Postconditions** | - POST-1: Stores the address with geocoded coordinates in the database. |
| **Normal Flow** | 1. The Customer enters the Address page, displaying saved locations.<br>2. The Customer clicks "Add Address".<br>3. The Customer types their street address. Autocomplete suggestions render.<br>4. The Customer selects their location. The system geocodes the address, retrieving latitude and longitude (USR-03).<br>5. The Customer sets the address as default, and clicks "Save".<br>6. The system marks this address default (unchecking previous default) and saves. |
| **Alternative Flows** | None |
| **Exceptions** | **2.E1 Address Limit Reached**<br>1. Customer already has 5 addresses.<br>2. Under rule USR-02, the system blocks form access: "Please delete an existing address to add a new one." |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - USR-02: Maximum 5 delivery addresses per Customer account. Exactly 1 must be Default.<br>- USR-03: Address must be validated via map API to retrieve exact coordinates. |
| **Other Information** | Coordinates are cached in session to prevent repeated API calls. |
| **Assumptions** | None |

---

#### **UC-24: Change Password & Account Recovery**

| ID and Name | UC-24 Change Password & Account Recovery |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | User Profile -> Security |
| **Primary Actors** | User |
| **Secondary Actors** | Email Delivery Service |
| **Description** | Allows logged-in users to update their password using BCrypt hashing, and unauthenticated users to request a password recovery link via verified email tokens. |
| **Preconditions** | - PRE-1: Email server SMTP is active. |
| **Trigger** | The user clicks "Change Password" in Profile or "Forgot Password" on Login page. |
| **Postconditions** | - POST-1: Password is changed. All other sessions are invalidated. |
| **Normal Flow** | **Part A - Password Change:**<br>1. User fills current, new, and confirm password fields.<br>2. The system verifies current password match, hashes the new password with BCrypt, updates the DB, and clears active sessions.<br><br>**Part B - Account Recovery:**<br>1. User enters their email on the Forgot Password page.<br>2. The system verifies email, creates a single-use token valid for 60 minutes, and emails a reset link.<br>3. User clicks link, enters new password, and saves. |
| **Alternative Flows** | None |
| **Exceptions** | **2.E1 Token Expired**<br>1. User clicks the recovery link after 70 minutes.<br>2. The system rejects validation and prompts a new request. |
| **Priority** | High (P0) |
| **Frequency of Use** | Low. |
| **Business Rules** | - SEC-01: Ensures cryptographic password protection. |
| **Other Information** | Prevents brute-forcing recovery tokens by hashing them in the DB. |
| **Assumptions** | None |

---

#### **UC-25: Sync Guest Cart to Customer Account**

| ID and Name | UC-25 Sync Guest Cart to Customer Account |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Cart & Wishlist -> Synchronization |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Automatically consolidates the guest's browser LocalStorage cart items into the Customer's database cart immediately after a successful login or registration event. |
| **Preconditions** | - PRE-1: The Customer has just logged in successfully.<br>- PRE-2: A guest cart array exists in browser LocalStorage. |
| **Trigger** | Successful authentication callback. |
| **Postconditions** | - POST-1: Guest items are merged into the database `cart_items` table.<br>- POST-2: Browser LocalStorage is cleared. |
| **Normal Flow** | 1. Following login, client JS reads `guest_cart` from LocalStorage.<br>2. The script transmits the JSON array to the Cart Sync Servlet.<br>3. The Servlet processes a database transaction: loops guest items, checks database records, adds quantities together (capped at stock limits), and saves.<br>4. The Servlet returns a success response.<br>5. Client JS deletes the LocalStorage key, reloading the refreshed persistent cart page. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | High. Triggered on every login. |
| **Business Rules** | - CHK-01: Merged cart items are subjected to standard server re-validation. |
| **Other Information** | Ensures a smooth shopping transition from Guest to Customer. |
| **Assumptions** | None |

---

#### **UC-26: Manage Product Wishlist**

| ID and Name | UC-26 Manage Product Wishlist |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Cart & Wishlist -> Favorites |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Allows Customers to favor products to save them for future purchase. Enables moving products directly from their Wishlist into the active shopping Cart. |
| **Preconditions** | - PRE-1: Customer is authenticated. |
| **Trigger** | The Customer clicks the "Heart" icon on a listing card or opens their Wishlist panel. |
| **Postconditions** | - POST-1: Updates the customer favorited record in the `wishlists` table. |
| **Normal Flow** | 1. Customer browses and clicks the "Heart" icon on "Fuji Apple".<br>2. System executes database insert, saving their favorite.<br>3. The icon changes to a solid red heart, displaying: "Added to Wishlist".<br>4. Customer opens their Wishlist dashboard and clicks "Move to Cart" on an item.<br>5. The system inserts the item's default variant into the Cart and splices it from the Wishlist. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | Low (P2) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - INV-03: Wishlist displays "Out of Stock" and blocks cart transfer if the item's variants have 0 stock. |
| **Other Information** | Helps track popular catalog interests. |
| **Assumptions** | None |

---

#### **UC-27: Validate Delivery Address & Shipping Radius**

| ID and Name | UC-27 Validate Delivery Address & Shipping Radius |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Checkout -> Geolocation Check |
| **Primary Actors** | Customer |
| **Secondary Actors** | Google Maps Distance Matrix API (System proxy) |
| **Description** | Automated geolocation validation step during checkout. Ensures that the Customer's shipping coordinates are within a strict 20km road radius from the warehouse coordinates. |
| **Preconditions** | - PRE-1: Shipping address is geocoded.<br>- PRE-2: Shop warehouse coordinates are registered (SHP-05). |
| **Trigger** | The Customer proceeds to the Shipping section during Checkout. |
| **Postconditions** | - POST-1: Confirms proximity compliance. Shipping routes are initialized. |
| **Normal Flow** | 1. During checkout, the Customer selects their address.<br>2. The system queries maps distance matrix API using the Customer's and Shop Owner's coordinates.<br>3. If the calculated road travel distance is below 20km, the validation is approved.<br>4. The system calculates and displays the exact shipping fee and distance. |
| **Alternative Flows** | None |
| **Exceptions** | **3.E1 Outside Proximity Radius**<br>1. If calculated distance is 22.4km (violating the 20km cap under DEL-01).<br>2. The system blocks checkout, disables CTAs, and displays: "Delivery is outside the shop's 20km freshness zone." |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - DEL-01: Shipping coordinates must be within 20km from shop coordinates.<br>- USR-03: Geocoding validation required to verify checkout coordinates.<br>- DLV-01: Checkout is blocked if customer address is outside operational service coordinates. |
| **Other Information** | Prevents quality decay during transit of fresh produce. |
| **Assumptions** | None |

---

#### **UC-28: Select Delivery & Shipping Method**

| ID and Name | UC-28 Select Delivery & Shipping Method |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Checkout -> Logistics |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Enables the Customer to select their delivery method. Evaluates weight load limits (30kg express limit) and calculates prep plus travel time to render the ETA. |
| **Preconditions** | - PRE-1: Shipping address is validated within the 20km radius (UC-27). |
| **Trigger** | The Customer selects a shipping option on the Checkout Page. |
| **Postconditions** | - POST-1: Shipping fee and travel time ETA are saved to the active checkout session. |
| **Normal Flow** | 1. The Customer selects "Express Motorbike Delivery".<br>2. The system sums the weight of all items in the checkout cart (e.g. 12kg).<br>3. The system verifies weight is below the 30kg load limit (DEL-02).<br>4. The system calculates the ETA (Prep time + Maps travel time) = 45 minutes (DEL-03).<br>5. Renders Shipping Fee (25,000 VND) and ETA (45 minutes) on the checkout card. |
| **Alternative Flows** | **4.1. Cart Over Weight Cap**<br>1. The checkout weight is 32kg.<br>2. Under DEL-02, the system disables the express motorbike shipping method and prompts: "Weight exceeds 30kg motorbike limit. Select standard cargo transport." |
| **Exceptions** | **4.E1 Freshness Time-out Trigger**<br>1. Heavy traffic routes calculate a travel time of 2 hours, exceeding the fresh safety SLA for cut fruits (e.g. 90 minutes).<br>2. Under rule FRH-05, the system blocks the checkout, forcing shop reassignment. |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - DEL-02: Caps express motorbike delivery weight at 30kg.<br>- DEL-03 / DLV-02: ETA = Prep Time + Travel Time + Buffer.<br>- FRH-05: Blocks checkout if ETA travel time exceeds freshness safe limits. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-29: Reorder Previous Purchase**

| ID and Name | UC-29 Reorder Previous Purchase |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Checkout -> Fast Reorder |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Provides Customers with a fast checkout shortcut. Clones items and weight variants from a completed old order and loads them directly into their active cart, bypassing catalog search. |
| **Preconditions** | - PRE-1: Customer possesses a completed order history. |
| **Trigger** | The Customer clicks the "Reorder" button on their Order History list. |
| **Postconditions** | - POST-1: Selected old items are mapped and added into the active shopping Cart. |
| **Normal Flow** | 1. Customer opens their Order History dashboard.<br>2. Customer identifies a past favorite order and clicks "Reorder".<br>3. System queries database, matches item variants, checks active stock, and pushes all available items into the Cart.<br>4. The system redirects the Customer directly to the Cart page. |
| **Alternative Flows** | **4.1. Out of Season Warning**<br>1. Some items in the target old order are currently out of stock or out of season.<br>2. The system adds only available items to the Cart and displays: "Unavailable items were bypassed." |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - CHK-01 / CHK-06: Added items are subjected to dynamic checkout re-validation. |
| **Other Information** | Improves returning customer conversions. |
| **Assumptions** | None |

---

#### **UC-30: Prepare Order & Manage Freshness Batches**

| ID and Name | UC-30 Prepare Order & Manage Freshness Batches |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Order Processing -> FEFO Picking |
| **Primary Actors** | Shop Owner |
| **Secondary Actors** | None |
| **Description** | Outlines order packaging. Enforces FEFO (First Expired, First Out) warehouse batch picking, manages physical weight variances (±3% rule), and triggers shortages substitution rules. |
| **Preconditions** | - PRE-1: Order status is `PROCESSING`. |
| **Trigger** | Packing staff retrieves the order fulfillment slip. |
| **Postconditions** | - POST-1: Mapped batches are locked. Order status moves to `READY_FOR_PICKUP`. |
| **Normal Flow** | 1. Staff opens the picking detail. System suggests the specific batch using **FEFO** (First Expired, First Out) rules (FRH-02).<br>2. Staff picks the fruit, weighs it, and enters the actual weight in the dashboard.<br>3. System checks weight variance is within ±3% tolerance under rule FRH-07.<br>4. The Shop Owner clicks "Ready for Pickup".<br>5. Order status is updated to `READY_FOR_PICKUP`, and driver dispatch alerts trigger. |
| **Alternative Flows** | **4.1. Freshness Shortage (Substitution)**<br>1. Selected batch has spoiled. Merchant proposes a substitute fruit under rule OLC-05.<br>2. Customer receives notification. If confirmed, pricing adjusts (OLC-07); if not confirmed in 10 minutes, the item is cancelled and refunded (OLC-06). |
| **Exceptions** | **4.E1 Weight Variance > 3% Shortage**<br>1. If actual weight is 5% below target, system calculates a shortage refund under FRH-07, adjusts invoice, and allows transit. |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - FRH-02: Enforces FEFO batch picking rules.<br>- FRH-07: Standard weight variance allowance is ±3%. Shortages beyond this are refunded.<br>- OLC-05 / OLC-06 / OLC-07: Governs product substitution pricing and responses. |
| **Other Information** | Critical for maintaining fresh warehouse stock compliance. |
| **Assumptions** | None |

---

#### **UC-31: Shipper Pickup & Delivery Lifecycle**

| ID and Name | UC-31 Shipper Pickup & Delivery Lifecycle |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Delivery Management -> Transit |
| **Primary Actors** | Delivery Staff (Shipper) |
| **Secondary Actors** | System |
| **Description** | Outlines active shipper transitions: driver pickup, package code verification, maps routing navigation, and sequential status updates (AwaitingPickup -> InTransit -> Arrived). |
| **Preconditions** | - PRE-1: Driver is marked active.<br>- PRE-2: Order is `READY_FOR_PICKUP`. |
| **Trigger** | Driver accepts or receives the delivery assignment. |
| **Postconditions** | - POST-1: Order transit status updates dynamically. |
| **Normal Flow** | 1. Driver clicks "Accept Trip". Status updates to `AWAITING_PICKUP` (DLV-04).<br>2. Driver arrives at warehouse, scans pickup, and clicks "Pick Up". Status updates to `IN_TRANSIT` (DLV-04).<br>3. System provides map coordinates. Driver arrives at home and clicks "Arrived". Status updates to `ARRIVED` (DLV-04). |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - DLV-04: Delivery states must flow sequentially: AwaitingPickup -> InTransit -> Arrived -> Delivered. |
| **Other Information** | Pushes active SMS alerts to customers during transit. |
| **Assumptions** | None |

---

#### **UC-32: Order Delivery Confirmation & Proof**

| ID and Name | UC-32 Order Delivery Confirmation & Proof |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Delivery Management -> Confirmation |
| **Primary Actors** | Delivery Staff (Shipper) |
| **Secondary Actors** | Customer, System |
| **Description** | Governs the successful delivery handover. Enforces uploading Proof of Delivery (POD) including physical photo evidence, GPS coordinates, and payment capture for COD orders. |
| **Preconditions** | - PRE-1: Order status is `ARRIVED`. |
| **Trigger** | Driver completes package handover and clicks "Complete Delivery". |
| **Postconditions** | - POST-1: Order status is updated to `DELIVERED` (DLV-04).<br>- POST-2: Launches the 6-hour refund dispute window. |
| **Normal Flow** | 1. Driver hands package to Customer and clicks "Complete Delivery".<br>2. System requires POD (DLV-05): physical photo upload, captures driver GPS coordinates, and requires COD cash input (FIN-05).<br>3. Driver uploads photo and submits.<br>4. System verifies details match, sets order to `DELIVERED`, starts the 6-hour dispute window (REF-01), and notifies Customer. |
| **Alternative Flows** | **4.1. COD Cash Discrepancy**<br>1. Cash input does not match invoice total.<br>2. Under FIN-05, the system flags `COD_RECONCILIATION_EXCEPTION` and blocks order completion until corrected. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - DLV-05: Successful delivery requires photo proof, coordinates, and COD verification.<br>- FIN-05: COD cash collected must match system invoice total. Mismatches trigger exceptions.<br>- REF-01: Successful delivery initiates the 6-hour refund complaint window. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-33: Handle Delivery Failures & SLA Exceptions**

| ID and Name | UC-33 Handle Delivery Failures & SLA Exceptions |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Delivery Management -> Exceptions |
| **Primary Actors** | Delivery Staff (Shipper) / System |
| **Secondary Actors** | Customer |
| **Description** | Governs unsuccessful delivery attempts, enforcing contact rules (2 attempts 5 minutes apart), registering failure reasons, and managing force majeure weather suspensions. |
| **Preconditions** | - PRE-1: Order status is `IN_TRANSIT` or `ARRIVED`. |
| **Trigger** | Customer is unreachable, or severe weather blocks logistics. |
| **Postconditions** | - POST-1: Order status is updated to `FAILED_DELIVERY`, and return trip is queued. |
| **Normal Flow** | 1. Driver arrives at the address, but Customer is unreachable.<br>2. Driver calls Customer, waits 5 minutes, and makes a second call.<br>3. System verifies call logs, satisfying the "2 contact attempts, 5 minutes apart" rule (DLV-06).<br>4. Driver clicks "Mark Delivery Failed", selects reason: "Customer unreachable", and uploads house photo.<br>5. Order status is updated to `FAILED_DELIVERY`. |
| **Alternative Flows** | **4.1. Weather / Force Majeure Exception (System triggered)**<br>1. Severe storm triggers platform suspension.<br>2. System triggers DLV-07: disables express checkout for the affected coordinates and pushes alerts. |
| **Exceptions** | **4.E1 Premature Failure Attempt**<br>1. Driver attempts to mark failed without making 2 call attempts.<br>2. System blocks the action: "SLA Violation: You must attempt to contact the customer twice, 5 minutes apart." |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - DLV-06: Drivers must make at least 2 contact attempts, 5 minutes apart, before failing delivery.<br>- DLV-07: Disables express checkout in affected coordinates during weather suspensions. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-34: Process Payment via Cash on Delivery (COD)**

| ID and Name | UC-34 Process Payment via Cash on Delivery (COD) |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Payment Management -> COD |
| **Primary Actors** | Customer |
| **Secondary Actors** | System |
| **Description** | Governs COD payment checks: verifies transaction amount is below 2,000,000 VND, and ensures the Customer does not have a "COD disabled" risk status due to 3 failed deliveries. |
| **Preconditions** | - PRE-1: An order has been placed in `PENDING_PAYMENT` state. |
| **Trigger** | Customer selects "Cash on Delivery" during checkout. |
| **Postconditions** | - POST-1: Order payment method is approved and set to `COD_CONFIRMED`. |
| **Normal Flow** | 1. Customer selects "Cash on Delivery" payment method.<br>2. System checks invoice total is under 2,000,000 VND (PAY-01) and customer has < 3 failed deliveries in 30 days (SEC-01).<br>3. System approves COD method.<br>4. Customer clicks Place Order. Order is marked `COD_CONFIRMED`. |
| **Alternative Flows** | **4.1. Order Value Exceeds COD Limit**<br>1. Invoice is 2,200,000 VND.<br>2. Under PAY-01, the system blocks COD, prompting online card payment. |
| **Exceptions** | **3.E1 COD Blocked due to Abuse**<br>1. Customer has 4 failed deliveries in 30 days.<br>2. Under SEC-01, the system blocks COD: "Cash on Delivery is disabled due to failed delivery history. Pay online to proceed." |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - PAY-01: COD limits transactions to below 2,000,000 VND.<br>- SEC-01: Disables COD option if customer abuse threshold is exceeded.<br>- FIN-07: COD refunds are not paid in physical cash by the shipper; they must go to the wallet. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-35: Process Payment via VietQR Dynamic QR**

| ID and Name | UC-35 Process Payment via VietQR Dynamic QR |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Payment Management -> Online |
| **Primary Actors** | Customer |
| **Secondary Actors** | VietQR API / Bank Gateway |
| **Description** | Generates a dynamic VietQR code using order amount and unique transaction ID. Initiates a 10-minute payment countdown session with real-time status polling. |
| **Preconditions** | - PRE-1: Order is in status `PENDING_PAYMENT`. |
| **Trigger** | Customer selects "VietQR Bank Transfer" and clicks "Pay Now". |
| **Postconditions** | - POST-1: Dynamically renders the QR code. Renders the 10-minute session timer. |
| **Normal Flow** | 1. Customer selects "VietQR Bank Transfer" and clicks "Pay Now".<br>2. System calls VietQR generator, encoding shop bank account, invoice total, and reference description (e.g. "FRUIT123").<br>3. System renders Payment Screen showing the QR code, exact bank fields, and 10-minute countdown (PAY-02).<br>4. System launches AJAX polling checking transaction status every 3 seconds.<br>5. Customer scans QR and transfers funds. |
| **Alternative Flows** | **4.1. 10-Minute Timeout Triggered (System)**<br>1. Countdown reaches 0 without transaction confirmation.<br>2. Under PAY-02, the system marks the transaction `EXPIRED`, releases the reserved stock, and cancels the order. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - PAY-02: E-wallet / Card / QR payment sessions expire after exactly 10 minutes, releasing stock. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-36: Confirm Payment via SePay Webhook**

| ID and Name | UC-36 Confirm Payment via SePay Webhook |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Payment Management -> Callback |
| **Primary Actors** | System (Automated Webhook Proxy) |
| **Secondary Actors** | SePay Payment Gateway API |
| **Description** | Processes automated payment notifications from the SePay gateway webhook. Enforces signature verification and transaction deduplication to ensure idempotent confirmation. |
| **Preconditions** | - PRE-1: SePay server has sent a payment callback payload.<br>- PRE-2: Secure API signature verification is active. |
| **Trigger** | SePay gateway executes an HTTP POST webhook request containing transaction details. |
| **Postconditions** | - POST-1: Payment transaction and order status are updated to `PAID` / `SUCCESS`. |
| **Normal Flow** | 1. SePay sends POST callback request to system.<br>2. System verifies payload signature and logs raw metadata (DAT-04).<br>3. System extracts description (e.g. "FRUIT123") and unique transaction ID.<br>4. System queries `sepay_webhook_dedup` table (FIN-01 check).<br>5. Finding no duplicate, system records transaction ID to the dedup table and updates order status to `PAID`. |
| **Alternative Flows** | **4.1. Duplicate Webhook Detected (System)**<br>1. Transaction ID is found in the dedup table.<br>2. Under FIN-01, the system ignores duplicate event, returning HTTP 200 OK. |
| **Exceptions** | **2.E1 Signature Verification Failed**<br>1. Signature does not match. System rejects request as unauthorized, returning HTTP 401. |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - FIN-01: Requires idempotent payment webhook processing using transaction deduplication.<br>- DAT-04: webhooks must store raw payload, timestamps, signature verification, and outcomes. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-37: Handle Payment & Stock Exceptions**

| ID and Name | UC-37 Handle Payment & Stock Exceptions |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Payment Management -> Reconciliation |
| **Primary Actors** | System (Automated Job) |
| **Secondary Actors** | Admin |
| **Description** | Resolves anomalies: handles "Payment Orphans" (webhook succeeds but order creation failed) and "Inventory Exceptions" (payment collected but stock lock failed). |
| **Preconditions** | - PRE-1: A payment anomaly is flagged by system logs. |
| **Trigger** | Webhook confirms a transaction but order integrity checks fail. |
| **Postconditions** | - POST-1: System initiates safety rollbacks, refunds, and alerts. |
| **Normal Flow** | **Part A - Payment Orphan:**<br>1. Webhook confirms payment but no matching order exists (e.g. customer closed browser).<br>2. Under rule OLC-09, the system creates a Payment Orphan record, blocking automatic dispatch, and assigns an Admin ticket.<br>3. Admin reviews ticket and pushes order creation or refund.<br><br>**Part B - Inventory Exception:**<br>1. Payment succeeds but stock locking failed due to race condition.<br>2. Under OLC-10, the system flags `InventoryException` and automatically refunds the Customer's wallet. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Low. |
| **Business Rules** | - OLC-09: Payment orphans trigger manual/reconciliation workflows.<br>- OLC-10: Stock reservation failures after payment require immediate automated refunds.<br>- DAT-05: Data discrepancies between payment, order, and shipments block settlement. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-38: Apply Promo Coupon Code & Budget Control**

| ID and Name | UC-38 Apply Promo Coupon Code & Budget Control |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Promotion System -> Voucher |
| **Primary Actors** | Customer |
| **Secondary Actors** | System |
| **Description** | Enables applying discount codes at checkout. Enforces coupon stacking rules (no double discount vouchers) and executes atomic budget deductions to prevent budget leak. |
| **Preconditions** | - PRE-1: Active coupon campaigns exist. |
| **Trigger** | Customer enters coupon code and clicks "Apply". |
| **Postconditions** | - POST-1: Calculated coupon discount is applied to the order subtotal. |
| **Normal Flow** | 1. Customer enters code "FRESH10".<br>2. System checks validity period, user usage, minimum order value (COU-01), and remaining budget limit (PRO-03).<br>3. System checks for applied discount coupons. Under rule PRO-01, it blocks stacking of two order-level discount vouchers.<br>4. System validates the code, deducts it from the subtotal, and updates the invoice. |
| **Alternative Flows** | **4.1. Stacking Shipping Voucher**<br>1. Customer has "FRESH10" applied and attempts to add "FREESHIP50".<br>2. System checks PRO-01: since "FREESHIP50" is a shipping voucher, stacking is permitted. |
| **Exceptions** | **4.E1 Coupon Budget Exceeded**<br>1. Two users attempt to use the last remaining coupon usage simultaneously.<br>2. Under COU-03, system locks the row during check. The first succeeds. The second is rejected: "Maximum coupon usage reached." |
| **Priority** | High (P0) |
| **Frequency of Use** | Very High. |
| **Business Rules** | - PRO-01: Order-level discount coupons cannot be stacked. Shipping coupons can be stacked.<br>- PRO-03 / COU-02: Vouchers automatically expire once budget or usage caps are reached.<br>- COU-01: Min order value checks are calculated based on item value after shop discounts.<br>- COU-03: Coupon budget deductions are handled atomically. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-39: Moderate Reviews & Sentiment Control**

| ID and Name | UC-39 Moderate Reviews & Sentiment Control |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Review & Rating -> Moderation |
| **Primary Actors** | Admin / System |
| **Secondary Actors** | None |
| **Description** | Renders moderation interfaces for Admins to filter reviews, and enforces rating calculation rules (excluding rejected reviews or fake merchant self-reviews). |
| **Preconditions** | - PRE-1: Review is submitted and pending moderation. |
| **Trigger** | Admin opens the Review Queue, or a newly approved review triggers recalculation. |
| **Postconditions** | - POST-1: Review status is updated, and product average stars are recalculated. |
| **Normal Flow** | 1. Admin reviews queue, checks comment content, and clicks "Approve Review".<br>2. System sets status to `APPROVED`. review becomes visible.<br>3. System recalculates average star rating using rule REV-02: Average Rating = Total Valid Stars / Total Valid Reviews.<br>4. Recalculated stars display on the listing card. |
| **Alternative Flows** | **4.1. Fraud Review Block (System triggered)**<br>1. A Shop Owner uses a Customer account logged from the same Device ID/IP to rate their own shop's fruit (USR-04 / RVR-03).<br>2. System triggers rule RVR-03: flags review as `FRAUD_REJECTED`, hides it from storefront, and reduces shop trust score. |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | High. |
| **Business Rules** | - REV-02 / RVR-02: Average Rating calculations must exclude rejected, spam, or fraudulent reviews.<br>- RVR-01: Reviews can only be edited by the user within 7 days and cannot be modified after Admin violation processing.<br>- RVR-03: System automatically flags and rejects fake ratings from matching IPs/Devices. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-40: Request Refund for Spoilage/Damaged Fruits**

| ID and Name | UC-40 Request Refund for Spoilage/Damaged Fruits |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Refund System -> Claim |
| **Primary Actors** | Customer |
| **Secondary Actors** | None |
| **Description** | Governs fresh fruit claims. Customers can request a full/partial refund for rotten or bruised fruits within exactly 6 hours post-delivery, uploading photo/video evidence. |
| **Preconditions** | - PRE-1: Order is `DELIVERED`.<br>- PRE-2: The 6-hour complaint window has not expired (REF-01). |
| **Trigger** | Customer clicks "Request Refund" next to a delivered item. |
| **Postconditions** | - POST-1: A refund case is created in the database. |
| **Normal Flow** | 1. Customer opens delivered order details within the 6-hour limit (REF-01).<br>2. Customer clicks "Request Refund".<br>3. Form requires: refund type, reason, damage ratio, and video/photo upload (REF-02).<br>4. Customer uploads video of rotten peaches and submits.<br>5. Under RFD-01, system creates the claim and sets status to `PENDING_MERCHANT_RESPONSE`. |
| **Alternative Flows** | **4.1. Weight Shortage Claim**<br>1. Customer selects "Weight Shortage" and inputs actual weight (e.g. 800g instead of 1000g).<br>2. Under RFD-02, system calculates shortage refund = Unit Price * Missing Weight (200g). |
| **Exceptions** | **2.E1 Refund Window Expired**<br>1. Attempting refund 8 hours after delivery.<br>2. Under REF-01, system blocks the request: "Refund window expired." |
| **Priority** | High (P0) |
| **Frequency of Use** | Low. |
| **Business Rules** | - REF-01: Fresh produce refund window is capped at exactly 6 hours post-delivery.<br>- REF-02: Requires clear photo or video evidence. Focuses on refund without physical return.<br>- RFD-01: Customers are restricted to exactly 1 refund claim per order line item.<br>- RFD-02: Shortage refunds = Unit Price * Missing Weight. |
| **Other Information** | No physical return by default reduces reverse transport costs. |
| **Assumptions** | None |

---

#### **UC-41: Process Refund Request**

| ID and Name | UC-41 Process Refund Request |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Refund System -> Verification |
| **Primary Actors** | Shop Owner / Admin |
| **Secondary Actors** | Payment Gateway API |
| **Description** | Handles the merchant/admin response to claims. Shop must respond in 12 hours, otherwise Admin reviews. Approved refunds are electronically routed to original payment methods. |
| **Preconditions** | - PRE-1: A refund claim is pending merchant response. |
| **Trigger** | Shop Owner opens their dashboard, or the 12-hour response timer expires. |
| **Postconditions** | - POST-1: Claim is resolved. Refund payout is initiated. |
| **Normal Flow** | 1. Shop Owner reviews video evidence, and clicks "Approve Refund".<br>2. System triggers the Electronic Refund Service (REF-03 / FIN-06):<br>   - Paid online: refund goes to original bank method.<br>   - COD: refund goes to platform wallet (FIN-07).<br>3. System deducts amount from merchant revenue ledger (RFD-06), and moves order to `COMPLETED` (OLC-08). |
| **Alternative Flows** | **4.1. Merchant Rejects / Dispute**<br>1. Shop Owner rejects refund, entering a dispute statement.<br>2. Claim moves to Admin Dispute queue for final resolution.<br><br>**4.2. 12-Hour SLA Auto-Approval (System)**<br>1. Merchant fails to respond in 12 hours.<br>2. Under RFD-05, the case moves to Admin and auto-approves if value is < 200,000 VND. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Low. |
| **Business Rules** | - REF-03 / FIN-06: Online refunds prioritize the original payment method. COD refunds go to platform wallet.<br>- OLC-08: Order is marked complete only after refund cases are closed.<br>- RFD-05: Merchant must respond within 12 hours, otherwise case escalates/auto-approves.<br>- RFD-06: Approved refunds must update revenue, platform fee deductions, and settlement.<br>- RFD-08: Payment gateway payout failures redirect to manual queues. |
| **Other Information** | Protects buyers from uncooperative sellers. |
| **Assumptions** | None |

---

#### **UC-42: Generate Digital Invoice & Download PDF**

| ID and Name | UC-42 Generate Digital Invoice & Download PDF |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Order Management -> Documentation |
| **Primary Actors** | Customer / System |
| **Secondary Actors** | None |
| **Description** | Automatically generates a digital receipt and invoice for completed orders, displaying picked batch numbers, tax data, and offers PDF download. |
| **Preconditions** | - PRE-1: Order is in status `DELIVERED` or `COMPLETED`. |
| **Trigger** | The Customer clicks "Download PDF Receipt" on their Order Details page. |
| **Postconditions** | - POST-1: Compiles order metrics and streams a downloadable PDF file. |
| **Normal Flow** | 1. Customer opens Order Details and clicks "Download PDF Receipt".<br>2. System queries order items, pricing, vouchers, and picked batch numbers (FRH-02).<br>3. System compiles details into an HTML5 template, converting it to PDF via OpenPDF/ReportLab.<br>4. Streams the PDF file to the browser. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Low. |
| **Business Rules** | - ORD-02: Total calculations must align exactly with invoicing totals (cannot be negative). |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-43: Send Real-Time Push & In-App Notifications**

| ID and Name | UC-43 Send Real-Time Push & In-App Notifications |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Notification System -> Delivery |
| **Primary Actors** | System |
| **Secondary Actors** | Customer, Shop Owner |
| **Description** | Dispatches real-time in-app alerts (via WebSocket indicators) on state transitions (Order Paid, Accepted, Shipped, Delivered) using idempotent delivery NTF-01. |
| **Preconditions** | - PRE-1: A database state change occurs (e.g. order marked delivered). |
| **Trigger** | Order transitions (e.g. shipper clicks delivered). |
| **Postconditions** | - POST-1: Alert record saved. WebSocket pushes alert to client browser. |
| **Normal Flow** | 1. Order is marked `DELIVERED`. System triggers Notification Service.<br>2. System checks Event UUID against processed logs (NTF-01 check).<br>3. Finding no duplicate, system saves alert record.<br>4. WebSocket pushes alert to Customer's active session, incrementing their red bell counter. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Extremely High. |
| **Business Rules** | - NTF-01: Critical notifications must utilize an idempotent delivery mechanism.<br>- NTF-02: Generates automatic alerts to all parties if express delivery is delayed beyond SLA. |
| **Other Information** | WebSocket has lightweight long-polling backup support. |
| **Assumptions** | None |

---

#### **UC-44: Send Asynchronous Email & SMS Alerts**

| ID and Name | UC-44 Send Asynchronous Email & SMS Alerts |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Notification System -> External |
| **Primary Actors** | System |
| **Secondary Actors** | SMTP Email Gateway |
| **Description** | Asynchronously dispatches external emails: registration verifications, OTP alerts, and login credentials/passwords for Guest checkout accounts. |
| **Preconditions** | - PRE-1: A critical event requires external notifications.<br>- PRE-2: SMTP server credentials are functional. |
| **Trigger** | System events (e.g. Guest checkout payment confirmed via SePay webhook). |
| **Postconditions** | - POST-1: Communication task is queued in an asynchronous thread and dispatched. |
| **Normal Flow** | 1. Guest payment confirmed. System auto-provisions account and password.<br>2. System pulls HTML email template, binds credentials, and queues task in a separate thread pool (NTF-03).<br>3. Asynchronous thread calls SMTP gateway API, successfully dispatching email. |
| **Alternative Flows** | **4.1. SMTP Gateway Timeout**<br>1. Gateway is offline.<br>2. System catches error, marks status `RETRY_PENDING`, and retries in 5 minutes (max 3 retries). |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | High. |
| **Business Rules** | - NTF-01 / NTF-03: System notifications are processed asynchronously to protect main thread performance. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-45: View Shop Wallet Settlement**

| ID and Name | UC-45 View Shop Wallet Settlement |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Financial Management -> Wallet |
| **Primary Actors** | Shop Owner / System |
| **Secondary Actors** | None |
| **Description** | Allows Shop Owners to check wallet balances (Pending Balance, Available Balance). Implements platform commission deductions post-dispute window. |
| **Preconditions** | - PRE-1: Shop account is Active (SHP-01). |
| **Trigger** | Shop Owner enters the Wallet dashboard. |
| **Postconditions** | - POST-1: Wallet available balance displays settled commission-deducted funds. |
| **Normal Flow** | 1. Shop Owner opens their financial dashboard.<br>2. System displays Pending Balance (funds in 12-hour hold) and settled commission-deducted Available Balance.<br>3. Shop Owner checks transaction ledger: an order delivered 13 hours ago has moved to Available Balance, minus the 5% platform commission (FIN-04). |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | High. Checked daily. |
| **Business Rules** | - PAY-03: Sales proceeds are held for exactly 12 hours from successful delivery to allow disputes.<br>- FIN-03 / FIN-04: Platform commission is deducted, and funds are settled to merchants only after the order is Completed. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-46: Manage Customer Accounts**

| ID and Name | UC-46 Manage Customer Accounts |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Administration -> User Control |
| **Primary Actors** | Admin |
| **Secondary Actors** | None |
| **Description** | Renders a Customer directory dashboard for Admins. Displays customer registration logs, COD abuse strikes, and manages profile data masking (after 90 days). |
| **Preconditions** | - PRE-1: Admin is logged in under ADM-01. |
| **Trigger** | Admin navigates to the Customer Management panel. |
| **Postconditions** | - POST-1: Directory table displays registered profiles. Drawer renders details. |
| **Normal Flow** | 1. Admin enters Customer panel.<br>2. System queries database and renders table: name, email, date registered, COD failed delivery strikes.<br>3. Admin filters by registration date.<br>4. Admin clicks Customer row, displaying details. |
| **Alternative Flows** | **4.1. Manual COD Unlock**<br>1. Customer COD is locked due to 3 failed deliveries.<br>2. Admin reviews profile, accepts statement, and clicks "Restore COD". Status is reset. |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | Low. |
| **Business Rules** | - SEC-01: Failed delivery counters are monitored to enforce COD lockout rules.<br>- DAT-06: Customer address and phone numbers are masked after 90 days from purchase to protect data privacy. |
| **Other Information** | Audit trail logs all profile adjustments. |
| **Assumptions** | None |

---

#### **UC-47: Manage Shop Owner Verification**

| ID and Name | UC-47 Manage Shop Owner Verification |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Administration -> Shop Control |
| **Primary Actors** | Admin |
| **Secondary Actors** | Email Delivery Service |
| **Description** | Governs the shop approval workflow. Admin reviews uploaded business licenses, warehouse details, tax IDs, and food safety permits to approve/reject shops. |
| **Preconditions** | - PRE-1: A merchant has registered a shop which is in "Pending Verification" status. |
| **Trigger** | Admin enters the Shop Verification Queue. |
| **Postconditions** | - POST-1: Shop and associated Owner accounts are changed to Active. |
| **Normal Flow** | 1. Admin reviews queue, clicks "Review Application" next to a shop.<br>2. System displays tax ID, payout bank, ID Card scans, and Safety Certificate uploads.<br>3. Admin downloads documents, verifies credentials, and clicks "Approve Shop".<br>4. System updates Shop and associated Owner statuses to "Active" (SHP-01).<br>5. Success email is automatically sent to the merchant. |
| **Alternative Flows** | **4.1. Reject Application**<br>1. Credentials are invalid. Admin clicks "Reject Application" and types reason: "Safety certificate expired."<br>2. System sets Shop to "Rejected", sending notification email. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - USR-01: Shop Owner accounts are created as "Pending Verification" and require Admin approval to activate.<br>- SHP-01: Active status requires valid ID, tax, and food safety credentials.<br>- DAT-07: All approval or rejection decisions are written to the Admin Audit Trail. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-48: Block & Unblock User Accounts**

| ID and Name | UC-48 Block & Unblock User Accounts |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Administration -> Account Risk |
| **Primary Actors** | Admin |
| **Secondary Actors** | Email Delivery Service |
| **Description** | Enables Admin to block violating accounts. Blocking a Shop Owner triggers a database transaction cascade to automatically hide active listings and cancel pending orders. |
| **Preconditions** | - PRE-1: Admin is logged in. |
| **Trigger** | Admin opens a user profile drawer and clicks "Block Account". |
| **Postconditions** | - POST-1: User status is set to Blocked. Sessions are terminated.<br>- POST-2: If Shop Owner, listings are Hidden and pending orders are Cancelled. |
| **Normal Flow** | 1. Admin opens profile, clicks "Block Account", and types reason.<br>2. System sets `status = 'BLOCKED'` in the `users` table.<br>3. Session tokens are invalidated immediately, logging out the user.<br>4. Action and reason are recorded in the security audit logs (DAT-07). |
| **Alternative Flows** | **4.1. Blocking a Shop Owner (Cascade)**<br>1. Target is a Shop Owner. System updates status to Blocked and Shop to Suspended (SHP-02).<br>2. System executes database transaction cascade (SEC-02): sets `status = 'HIDDEN'` for all products owned by this shop, cancels all pending orders, and starts online payout refunds. |
| **Exceptions** | **2.E1 Self-Blocking Prevent**<br>1. Admin tries to block their own profile. System rejects the action. |
| **Priority** | High (P0) |
| **Frequency of Use** | Low. |
| **Business Rules** | - SEC-02: Blocking a Shop Owner must automatically hide their products and cancel/refund pending orders.<br>- SHP-02: Suspended status blocks creation of new listings, flash sales, or wallet withdrawals.<br>- DAT-07: Action must be captured in the Admin Audit logs. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-49: Moderate Product Listings**

| ID and Name | UC-49 Moderate Product Listings |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Administration -> Product Quality |
| **Primary Actors** | Admin |
| **Secondary Actors** | Email Delivery Service |
| **Description** | Allows the Admin to moderate products in the verification queue, review organic/imported certificates, approve listings for public sale, or hide violating items. |
| **Preconditions** | - PRE-1: A product has been submitted and is in "Pending Approval" state. |
| **Trigger** | Admin selects the "Product Moderation" tab in the Admin Panel. |
| **Postconditions** | - POST-1: Product status is set to Active or Rejected in the database. |
| **Normal Flow** | 1. Admin reviews queue, clicks details of a product.<br>2. System displays description, variants, and certificate attachments.<br>3. Admin downloads documents (e.g. batch document for Organic tag under PRD-02).<br>4. Admin clicks "Approve Product".<br>5. System sets product `status = 'ACTIVE'`, and emails merchant. |
| **Alternative Flows** | **4.1. Rejecting Listing**<br>1. Content violates policies. Admin clicks "Reject Listing", typing reason.<br>2. System sets product status to Rejected, and emails merchant. |
| **Exceptions** | None |
| **Priority** | High (P0) |
| **Frequency of Use** | Medium. |
| **Business Rules** | - PRD-02: Enforces verification of imported or organic certifications.<br>- DAT-07: All product moderation actions are saved in the Admin Audit logs. |
| **Other Information** | None |
| **Assumptions** | None |

---

#### **UC-50: View Global Operations & Revenue Dashboards**

| ID and Name | UC-50 View Global Operations & Revenue Dashboards |
| :---- | :---- |
| **Created By** | Duong Minh Hoang |
| **Date Created** | 2026-05-27 |
| **Feature** | Financial Management -> Reports |
| **Primary Actors** | Admin |
| **Secondary Actors** | Chart.js System |
| **Description** | Aggregates database metrics to display platform analytics: GMV, Net Revenue, platform fees, shipping fees, and platform-funded voucher costs, using Chart.js trends. |
| **Preconditions** | - PRE-1: Admin is logged in. |
| **Trigger** | Admin opens the Dashboard home. |
| **Postconditions** | - POST-1: Renders sales charts, transaction volumes, and operational metrics. |
| **Normal Flow** | 1. Admin opens the panel.<br>2. System executes aggregated database queries: GMV, isolations (ADM-03), filters out cancelled/failed payment orders (ADM-04), and aggregates parent/child order behavior (ADM-05).<br>3. Maps arrays to Chart.js scripts.<br>4. Renders responsive revenue trend lines, pie charts, and alert widgets. |
| **Alternative Flows** | None |
| **Exceptions** | None |
| **Priority** | Medium (P1) |
| **Frequency of Use** | High. Checked daily. |
| **Business Rules** | - ADM-03: Revenue reports must strictly isolate GMV, merchant revenue, platform fees, shipping fees, vouchers, and refunds.<br>- ADM-04: Excludes cancelled or fraudulent transactions from active net revenue totals.<br>- ADM-05: Isolates Parent Order vs Child Order counting to prevent dashboard report inflation. |
| **Other Information** | Reads use optimized read-replicas or analytical tables to prevent slowing checkout traffic. |
| **Assumptions** | None |
