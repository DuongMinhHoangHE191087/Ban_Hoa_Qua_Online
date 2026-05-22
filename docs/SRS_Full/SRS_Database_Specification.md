# SYSTEM REQUIREMENTS SPECIFICATION & DATABASE ARCHITECTURE
## ONLINE FRUIT SHOPPING PLATFORM

> **Document Type:** High-Level Design & Physical Database Schema Specification  
> **Version:** 1.0  
> **Target Format:** Fully compatible and optimized for direct Microsoft Word (.docx) export.  

---

## 1. High Level Design

### 1.1 Software Architecture

Below is the software architectural diagram representing the system's structural layout, sub-systems, components, and communication flows. It adheres to the classic layered (multi-tier) architectural pattern for enterprise Java web applications, ensuring a clean separation of concerns.

![Software Architecture Diagram](file:///C:/Users/Admin/.gemini/antigravity-ide/brain/78e07daf-ef98-45c1-8c11-41094e6f254a/system_architecture_diagram_1779294951731.png)

#### Explanation of Diagram Components

1. **Web Browser (Client Layer)**:
   * **Role**: The frontend client where users (Customers, Shop Owners, Delivery Staff, Admins) interact with the application.
   * **Communication**: Initiates operations by dispatching **HTTP Requests** (GET, POST) and renders the corresponding **HTTP Responses** containing processed HTML/CSS/JS.

2. **System Architecture (Application Server Layer)**:
   * **Presentation Logic Layer (Servlet: Controller)**:
     * **Role**: Acts as the traffic controller (MVC Controller). It intercepts incoming HTTP Requests, parses parameters, performs basic input validation, and delegates business requests to the Service Layer.
     * **Example**: `ProductController`, `OrderController`, `UserController`.
   * **User Interface (UI: JSP + JSTL)**:
     * **Role**: The view engine (MVC View) that dynamically renders graphical interfaces using HTML, CSS, JavaScript, and Java Standard Tag Library (JSTL) based on data models supplied by the Controller.
     * **Example**: `/web/WEB-INF/jsp/product/list.jsp`, `/web/WEB-INF/jsp/order/checkout.jsp`.
   * **Service Layer (Java: Business Services)**:
     * **Role**: The core brain of the application (Business Logic Layer). It implements strict business rules, handles transactions, manages calculations, and coordinates multiple Data Access Objects (DAOs) under unified operations.
     * **Example**: `ProductService`, `OrderService`, `UserService`.
   * **Data Access Layer (JDBC: DAO)**:
     * **Role**: The persistence coordinator. It communicates directly with the database using JDBC (Java Database Connectivity) via SQL queries, preparing statements, executing commands, and parsing SQL result sets into Java Objects.
     * **Example**: `ProductDAO`, `OrderDAO`, `UserDAO`.
   * **Model Classes (Java Beans/POJOs)**:
     * **Role**: Domain Models representing physical database entities in Java memory. They transfer data between layers.
     * **Example**: `Product`, `Order`, `User`.
   * **Common Classes (Java Utilities & Helpers)**:
     * **Role**: General reusable utilities shared across all layers. Includes database connection pool managers (`DBContext`), custom filters (`AuthFilter`), cryptographic helpers (`HashUtility`), and constant enums.
     * **Example**: `DBContext`, `BaseService`, `SecurityFilter`.

3. **SQL Server (Database Layer)**:
   * **Role**: The persistent relational database system. It stores the application state, guarantees ACID transactions, and enforces data integrity constraints (Primary Keys, Foreign Keys, Unique constraints, Check conditions).

---

## 2. Detailed Database Schema Specification

### 2.1 Database Table Matrix

Below is a quick reference matrix of all 24 physical tables defined and normalized within the database schema:

| No | Table Name | Functional Description |
|---|---|---|
| 01 | [users](#221-users) | Manages all system user accounts (Customers, Shop Owners, Delivery Staff, Admins). |
| 02 | [user_sessions](#222-user_sessions) | Stores user login sessions and authentication tokens. |
| 03 | [shop_owner_profiles](#223-shop_owner_profiles) | Detailed profiles and approval statuses for Shop Owners' storefronts. |
| 04 | [categories](#224-categories) | Product categories for classifying agricultural goods and fresh fruits. |
| 05 | [products](#225-products) | General information for registered agricultural items and fresh fruits. |
| 06 | [product_images](#226-product_images) | Image gallery storage supporting multiple images per fruit product. |
| 07 | [product_variants](#227-product_variants) | Specific product packaging options, pricing tiers, and stock amounts. |
| 08 | [inventory_logs](#228-inventory_logs) | Complete audit log for stock fluctuations (adjustments, reservations, releases). |
| 09 | [promotions](#229-promotions) | Coupon/Voucher management supporting shop-level or platform-wide scopes. |
| 10 | [cart](#2210-cart) | Shopping cart header associated with each registered customer. |
| 11 | [cart_items](#2211-cart_items) | Individual product variants and quantities stored inside customers' carts. |
| 12 | [orders](#2212-orders) | Main order header details, including pricing breakdown, payment, and status. |
| 13 | [order_items](#2213-order_items) | Transactional snapshots of purchased variants, pricing, and quantities. |
| 14 | [order_promotions](#2214-order_promotions) | Logs the history of applied promo codes and actual discounts per order. |
| 15 | [return_requests](#2215-return_requests) | Tracks cancellations, returns, and refunds requested by customers. |
| 16 | [shop_settlements](#2216-shop_settlements) | Periodic financial settlement sessions between the platform and shop owners. |
| 17 | [shop_settlement_orders](#2217-shop_settlement_orders) | Detailed mapping of orders processed within a specific financial settlement. |
| 18 | [payment_transactions](#2218-payment_transactions) | Automated payment transaction records via SePay QR or COD. |
| 19 | [sepay_webhook_dedup](#2219-sepay_webhook_dedup) | Idempotent ledger to prevent duplicate processing of bank transfer webhooks. |
| 20 | [deliveries](#2220-deliveries) | Dispatching logs, shipper assignments, and delivery tracking. |
| 21 | [reviews](#2221-reviews) | Customer reviews, star ratings, and feedback comments on purchased products. |
| 22 | [chat_sessions](#2222-chat_sessions) | Active chat rooms connecting customers directly with shop owners. |
| 23 | [chat_messages](#2223-chat_messages) | Individual message threads stored chronologically inside active chat sessions. |
| 24 | [notifications](#2224-notifications) | Automated alerts, order updates, and marketing notifications pushed to users. |

---

### 2.2 Detailed Table Field Descriptions

*Legend for Table Constraints:*
* `PK` ~ Primary Key
* `FK` ~ Foreign Key
* `UN` ~ Unique
* `NN` ~ Not Null

---

#### 2.2.1 users
Stores general authentication and profile information for all users (Customers, Shop Owners, Delivery Staff, and Administrators).

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `user_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Unique identifier for each user account, auto-incrementing. |
| 02 | `full_name` | | | | ✓ | **NVARCHAR(100)**: Display name of the user. |
| 03 | `email` | | | ✓ | ✓ | **NVARCHAR(255)**: Email address used for authentication; must be unique across the platform. |
| 04 | `password_hash` | | | | | **NVARCHAR(255)**: Secure BCrypt hash of the user password. Can be NULL for Google OAuth users. |
| 05 | `phone` | | | | | **NVARCHAR(15)**: User's contact number, primarily utilized for delivery operations. |
| 06 | `role` | | | | ✓ | **NVARCHAR(20)**: Role classification. CHECK constraints: `CUSTOMER`, `SHOP_OWNER`, `DELIVERY`, `ADMIN`. |
| 07 | `status` | | | | ✓ | **NVARCHAR(20)**: Account status. CHECK constraints: `ACTIVE` (enabled), `INACTIVE` (locked/disabled). |
| 08 | `user_address` | | | | | **NVARCHAR(500)**: Default shipping address for the user account. |
| 09 | `is_email_verified` | | | | ✓ | **BIT (DEFAULT 0)**: Cờ indicating if the email has been verified via OTP or registration email link. |
| 10 | `failed_login_count` | | | | ✓ | **INT (DEFAULT 0)**: Consecutively failed login attempts to prevent brute-force attacks. |
| 11 | `locked_until` | | | | | **DATETIME**: Timestamp indicating until when the account is temporarily locked out. |
| 12 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp when the user account was created. |
| 13 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of the latest account information modification. |

---

#### 2.2.2 user_sessions
Stores secure tokens to manage persistent user login states and API authentication.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `session_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing session identifier. |
| 02 | `user_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)`. Automatically cascaded on user deletion. |
| 03 | `token` | | | ✓ | ✓ | **NVARCHAR(100)**: Unique session token (UUID or JWT) generated upon login. |
| 04 | `expires_at` | | | | ✓ | **DATETIME**: Session expiration timestamp. |

---

#### 2.2.3 shop_owner_profiles
Maintains legal registration profiles, store descriptions, and approval statuses for online storefronts owned by Shop Owners.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `profile_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing shop profile identifier. |
| 02 | `user_id` | | ✓ | ✓ | ✓ | **INT**: Foreign Key referencing `users(user_id)`. Enforces a strict 1-to-1 relationship. |
| 03 | `shop_name` | | | | ✓ | **NVARCHAR(150)**: Registered brand name of the fruit shop. |
| 04 | `shop_description` | | | | | **NVARCHAR(MAX)**: Detailed story, introduction, or promotional pitch for the shop. |
| 05 | `approval_status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'PENDING')**: Verification lifecycle status. CHECK: `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`. |
| 06 | `rejection_reason` | | | | | **NVARCHAR(500)**: Explanation logged by Admin if storefront registration is rejected. |
| 07 | `approved_at` | | | | | **DATETIME**: Timestamp indicating when the storefront was approved. |
| 08 | `delivery_address` | | | | | **NVARCHAR(500)**: Physical address of the farm/warehouse where drivers pick up goods. |
| 09 | `rating` | | | | ✓ | **DECIMAL(3,2) (DEFAULT 0)**: Cumulative average shop rating from customer orders (1.00 to 5.00). |
| 10 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of shop profile registration submission. |
| 11 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of the latest shop profile modification. |

---

#### 2.2.4 categories
Stores product categories to classify various fresh fruits and agricultural goods.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `category_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing category identifier. |
| 02 | `name` | | | ✓ | ✓ | **NVARCHAR(100)**: Display name of the category; must be unique to prevent duplicates. |
| 03 | `slug` | | | ✓ | ✓ | **NVARCHAR(100)**: URL-friendly format of the name used for SEO indexing; unique constraint. |
| 04 | `display_order` | | | | ✓ | **INT (DEFAULT 0)**: Index used for sorting the layout sequence of categories on UI. |
| 05 | `is_active` | | | | ✓ | **BIT (DEFAULT 1)**: Status toggle (1: Available; 0: Deactivated/Hidden). |

---

#### 2.2.5 products
Stores general catalog data for fresh fruit listings.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `product_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing product identifier. |
| 02 | `owner_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` indicating the Shop Owner who sells this product. |
| 03 | `category_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `categories(category_id)` to classify the product. |
| 04 | `name` | | | | ✓ | **NVARCHAR(200)**: Display title of the fruit item. |
| 05 | `description` | | | | | **NVARCHAR(MAX)**: Informative product details, nutritional value, and storage instructions. |
| 06 | `origin_country` | | | | | **NVARCHAR(100)**: Country of origin (e.g., Vietnam, Australia, USA). |
| 07 | `origin_region` | | | | | **NVARCHAR(150)**: Regional cultivation zone (e.g., Luc Ngan, Da Lat). |
| 08 | `harvest_date` | | | | | **DATE**: Date when the fruit was harvested from the farm. |
| 09 | `shelf_life_days` | | | | | **INT**: Estimated shelf life in days under standard conditions. |
| 10 | `storage_instruction` | | | | | **NVARCHAR(300)**: Directions for optimal storage (e.g., "Keep cold at 4-8°C"). |
| 11 | `status` | | | | ✓ | **NVARCHAR(20)**: Catalog availability. CHECK constraints: `ACTIVE` (visible), `INACTIVE` (hidden/discontinued). |
| 12 | `view_count` | | | | ✓ | **INT (DEFAULT 0)**: Cumulative customer view impressions for popular metrics. |
| 13 | `rating` | | | | ✓ | **DECIMAL(3,2) (DEFAULT 0)**: Average customer rating (1.00 to 5.00) based on verified purchase reviews. |
| 14 | `sold_quantity` | | | | ✓ | **INT (DEFAULT 0)**: Total volume of variants purchased successfully. |
| 15 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Creation timestamp. |
| 16 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last update timestamp. |

---

#### 2.2.6 product_images
Stores URLs or physical paths of images associated with each catalog product.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `image_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing image identifier. |
| 02 | `product_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `products(product_id)`. Automatically deleted on cascade when product is removed. |
| 03 | `file_path` | | | | ✓ | **NVARCHAR(500)**: CDN URL or path of the uploaded image file. |
| 04 | `display_order` | | | | ✓ | **INT (DEFAULT 0)**: Sorting index (lower values are prioritized on the UI slide). |
| 05 | `is_primary` | | | | ✓ | **BIT (DEFAULT 0)**: Flag identifying the main thumbnail image (1: Main; 0: Gallery). |
| 06 | `uploaded_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp indicating when the file was uploaded. |

---

#### 2.2.7 product_variants
Stores packaging configurations, prices, and stock amounts. A product can have multiple variants (e.g., "Box 1kg", "Carton 10kg").

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `variant_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing variant identifier. |
| 02 | `product_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `products(product_id)`. Cascaded on product removal. |
| 03 | `sku` | | | ✓ | ✓ | **NVARCHAR(50)**: Unique Stock Keeping Unit serving as the warehousing barcode. |
| 04 | `variant_label` | | | | ✓ | **NVARCHAR(100)**: Packaging label (e.g., "Box 1kg", "Carton 10kg"). |
| 05 | `price` | | | | ✓ | **DECIMAL(12,2)**: Retail listing price of this variant (VND). |
| 06 | `stock_quantity` | | | | ✓ | **INT (DEFAULT 0)**: Real-time physical inventory amount remaining in the shop warehouse. |
| 07 | `is_active` | | | | ✓ | **BIT (DEFAULT 1)**: Status toggle (1: Purchasable; 0: Out of stock/Deactivated). |
| 08 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Registration timestamp. |
| 09 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last update timestamp. |

---

#### 2.2.8 inventory_logs
Logs all stock fluctuations for inventory audit purposes.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `log_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing inventory log identifier. |
| 02 | `variant_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `product_variants(variant_id)`. |
| 03 | `changed_by` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the user who triggered the stock update. |
| 04 | `change_type` | | | | ✓ | **NVARCHAR(20)**: Event classifier. CHECK constraints: `MANUAL_ADJUST`, `ORDER_RESERVE` (checkout holds), `ORDER_RELEASE` (canceled holds), `ORDER_CONFIRM` (deduction), `RETURN`. |
| 05 | `quantity_delta` | | | | ✓ | **INT**: Net stock variation amount (positive for additions, negative for deductions). |
| 06 | `quantity_after` | | | | ✓ | **INT**: Resultant stock amount in inventory post-event. |
| 07 | `note` | | | | | **NVARCHAR(300)**: Descriptive explanation (e.g., "Weekly restock", "Held for Order DH101"). |
| 08 | `changed_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of the inventory change event. |

---

#### 2.2.9 promotions
Stores discount codes and promotional campaigns.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `promo_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing promotion identifier. |
| 02 | `code` | | | ✓ | ✓ | **NVARCHAR(50)**: Unique voucher code entered at checkout (e.g., "GIAM20K", "FREESHIP"). |
| 03 | `discount_type` | | | | ✓ | **NVARCHAR(10)**: Calculation type. CHECK: `PERCENT` (% off), `FIXED` (flat discount). |
| 04 | `discount_scope` | | | | ✓ | **NVARCHAR(50)**: Campaign owner. CHECK: `SHOP` (vendor-funded), `ALL` (platform-funded). |
| 05 | `discount_max` | | | | ✓ | **DECIMAL(10,2) (DEFAULT 0)**: Cap amount of the discount (especially useful for percentage discounts). |
| 06 | `discount_value` | | | | ✓ | **DECIMAL(10,2)**: Actual discount magnitude (e.g., 10.00 for PERCENT, 20000.00 for FIXED). |
| 07 | `min_order_value` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Minimum subtotal required in cart to qualify for this voucher. |
| 08 | `scope` | | | | ✓ | **NVARCHAR(15)**: Scope target. CHECK constraints: `ORDER` (whole cart), `PRODUCT` (specific catalog items). |
| 09 | `product_id` | | ✓ | | | **INT**: Foreign Key referencing `products(product_id)`. NULL if targeted to the entire store. |
| 10 | `max_uses` | | | | | **INT**: Cumulative usage limit cap. NULL if unlimited. |
| 11 | `used_count` | | | | ✓ | **INT (DEFAULT 0)**: Real-time cumulative times this code has been successfully redeemed. |
| 12 | `can_stack` | | | | ✓ | **BIT (DEFAULT 0)**: Flag indicating if this voucher can be combined with other coupons (1: Yes; 0: No). |
| 13 | `valid_from` | | | | ✓ | **DATETIME**: Effective starting timestamp. |
| 14 | `valid_until` | | | | ✓ | **DATETIME**: Effective expiration timestamp. |
| 15 | `created_by` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the Admin or Shop Owner who created it. |
| 16 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Creation timestamp. |
| 17 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last update timestamp. |
| 18 | `is_deleted` | | | | ✓ | **BIT (DEFAULT 0)**: Soft-deletion indicator flag (1: Deleted; 0: Active). |
| 19 | `is_active` | | | | ✓ | **BIT (DEFAULT 1)**: Immediate availability toggle switch. |

---

#### 2.2.10 cart
Stores shopping cart headers for registered customers.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `cart_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing cart identifier. |
| 02 | `customer_id` | | ✓ | ✓ | ✓ | **INT**: Foreign Key referencing `users(user_id)`. Enforces a strict 1-to-1 relationship. |
| 03 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Creation timestamp. |
| 04 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last modification timestamp. |

---

#### 2.2.11 cart_items
Stores individual product variants and quantities stored inside customers' carts.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `cart_item_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing item identifier. |
| 02 | `cart_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `cart(cart_id)`. Cascaded automatically on cart header release. |
| 03 | `variant_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `product_variants(variant_id)`. |
| 04 | `quantity` | | | | ✓ | **INT**: Selected volume. Checked to guarantee value `>= 1`. |
| 05 | `added_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of item addition. |

---

#### 2.2.12 orders
Stores transactional headers, total sums, and lifecycles of customer purchases.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `order_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing order identifier. |
| 02 | `customer_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the purchasing Customer. |
| 03 | `owner_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the recipient Shop Owner. |
| 04 | `delivery_address` | | | | ✓ | **NVARCHAR(500)**: Explicit shipping destination address provided by the user. |
| 05 | `user_address` | | | | ✓ | **NVARCHAR(500)**: Customer registration address archived at the moment of order placement. |
| 06 | `delivery_time_slot` | | | | | **NVARCHAR(100)**: Customer's preferred delivery time window (e.g., "Office hours", "After 6 PM"). |
| 07 | `notes` | | | | | **NVARCHAR(300)**: Custom shipping instructions (e.g., "Fragile, handle fruit with care"). |
| 08 | `cancelled_at` | | | | | **DATETIME**: Order cancellation timestamp. |
| 09 | `cancelled_by` | | ✓ | | | **INT**: Foreign Key referencing `users(user_id)` identifying who canceled the order. |
| 10 | `cancellation_reason` | | | | | **NVARCHAR(500)**: Reason logged for canceling the order. |
| 11 | `status` | | | | ✓ | **NVARCHAR(25) (DEFAULT 'PENDING_PAYMENT')**: Order lifecycle status. CHECK constraints: `PENDING_PAYMENT`, `CONFIRMED`, `PREPARING`, `DISPATCHED` (in transit), `DELIVERED`, `CANCELLED`, `PAYMENT_FAILED`, `EXPIRED`. |
| 12 | `total_amount` | | | | ✓ | **DECIMAL(14,2)**: Total cost of order items before promotions (VND). |
| 13 | `delivery_fee` | | | | ✓ | **DECIMAL(10,2) (DEFAULT 0)**: Logistics shipping fee (VND). |
| 14 | `discount_amount` | | | | ✓ | **DECIMAL(12,2) (DEFAULT 0)**: Total applied discount sum (shop and system coupons combined). |
| 15 | `system_discount_amount` | | | | ✓ | **DECIMAL(12,2) (DEFAULT 0)**: Discount amount funded by the platform coupon. |
| 16 | `shop_discount_amount` | | | | ✓ | **DECIMAL(12,2) (DEFAULT 0)**: Discount amount funded by the vendor shop coupon. |
| 17 | `platform_fee` | | | | ✓ | **DECIMAL(12,2) (DEFAULT 0)**: Service commission calculated and cut by the platform (VND). |
| 18 | `final_amount` | | | | ✓ | **DECIMAL(14,2)**: Net payable sum: `total_amount` + `delivery_fee` - `discount_amount`. |
| 19 | `payment_method` | | | | ✓ | **NVARCHAR(20)**: Payment channel. CHECK constraints: `CK` (Bank transfer QR), `COD` (Cash on delivery). |
| 20 | `refund_status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'NONE')**: Financial refund status. CHECK constraints: `NONE`, `PENDING`, `APPROVED`, `REJECTED`, `PROCESSING`, `REFUNDED`, `FAILED`. |
| 21 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of order placement. |
| 22 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last update timestamp. |

---

#### 2.2.13 order_items
Stores product purchase logs. Preserves historical pricing snapshot data to safeguard invoice legality when catalogs change.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `order_item_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing item identifier. |
| 02 | `order_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Cascaded on order header deletion. |
| 03 | `variant_id` | | ✓ | | | **INT**: Foreign Key referencing `product_variants(variant_id)`. Set NULL on deletion to protect logs. |
| 04 | `product_name_snapshot` | | | | ✓ | **NVARCHAR(200)**: Archived name snapshot of the fruit product at purchase. |
| 05 | `variant_label_snapshot` | | | | ✓ | **NVARCHAR(100)**: Archived label snapshot of packaging (e.g., "Box 1kg") at purchase. |
| 06 | `quantity` | | | | ✓ | **INT**: Quantity purchased. Checked to guarantee value `>= 1`. |
| 07 | `unit_price` | | | | ✓ | **DECIMAL(12,2)**: Price charged per unit at checkout (VND). |
| 08 | `subtotal` | | | | ✓ | **DECIMAL(14,2)**: Line subtotal: `unit_price` * `quantity` (VND). |

---

#### 2.2.14 order_promotions
Tracks promotion voucher usage logs associated with orders.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `usage_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing identifier. |
| 02 | `order_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Cascaded on order deletion. |
| 03 | `promo_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `promotions(promo_id)`. |
| 04 | `customer_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` to evaluate per-user usage limits. |
| 05 | `discount_applied` | | | | ✓ | **DECIMAL(12,2)**: Net financial discount deducted from the order via this coupon. |
| 06 | `used_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of redemption. |

---

#### 2.2.15 return_requests
Stores cancellation and refund requests filed by customers for broken, rotten, or incorrect fruit deliveries.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `return_request_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing return identifier. |
| 02 | `order_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Cascaded on order deletion. |
| 03 | `order_item_id` | | ✓ | | | **INT**: Foreign Key referencing `order_items(order_item_id)`. NULL if targeted to the whole order. |
| 04 | `customer_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the filing Customer. |
| 05 | `request_type` | | | | ✓ | **NVARCHAR(20)**: Request type. CHECK constraints: `CANCEL` (before delivery), `RETURN` (refund return), `EXCHANGE`. |
| 06 | `reason_code` | | | | ✓ | **NVARCHAR(50)**: Reason classification. CHECK: `WRONG_ITEM`, `DAMAGED` (rotten/crushed), `MISSING_ITEM`, `LATE_DELIVERY`, `NOT_AS_DESCRIBED`, `OTHER`. |
| 07 | `description` | | | | | **NVARCHAR(1000)**: Elaborate customer feedback explanation. |
| 08 | `evidence_url` | | | | | **NVARCHAR(500)**: URL of image or video proof showing rotten/draped fruits. |
| 09 | `requested_quantity` | | | | ✓ | **INT (DEFAULT 1)**: Quantity target. Checked to guarantee value `>= 1`. |
| 10 | `resolution_type` | | | | | **NVARCHAR(20)**: Resolution mechanism. CHECK constraints: `REFUND`, `REPLACE`, `DISCOUNT` (partial discount), `REJECT`. |
| 11 | `replacement_variant_id` | | ✓ | | | **INT**: Foreign Key referencing `product_variants(variant_id)` for exchange units. |
| 12 | `refund_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Resolved refund sum (VND). |
| 13 | `status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'REQUESTED')**: Execution status. CHECK: `REQUESTED`, `APPROVED`, `REJECTED`, `PROCESSING`, `COMPLETED`, `CANCELLED`. |
| 14 | `decided_by` | | ✓ | | | **INT**: Foreign Key referencing `users(user_id)` identifying the reviewer (Admin or Shop Owner). |
| 15 | `decision_reason` | | | | | **NVARCHAR(500)**: Official remarks logged by the reviewer explaining the decision. |
| 16 | `resolved_at` | | | | | **DATETIME**: Settlement completion timestamp. |
| 17 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Filing timestamp. |
| 18 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Latest status change timestamp. |

---

#### 2.2.16 shop_settlements
Manages periodic financial settlements between the platform and Shop Owners.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `settlement_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing settlement session identifier. |
| 02 | `owner_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the Shop Owner. |
| 03 | `period_start` | | | | ✓ | **DATE**: Starting date of the accounting cycle. |
| 04 | `period_end` | | | | ✓ | **DATE**: Ending date of the accounting cycle. |
| 05 | `gross_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Cumulative order sales subtotal from successful orders (VND). |
| 06 | `platform_fee_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Commission cut retained by the platform (VND). |
| 07 | `refund_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Total customer refund deductions logged in this cycle (VND). |
| 08 | `adjustment_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Manual accounting adjustments (e.g., driver dispute compensations). |
| 09 | `net_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Net payable sum: `gross_amount` - `platform_fee_amount` - `refund_amount` +/- `adjustment_amount` (VND). |
| 10 | `status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'PENDING')**: Settlement status. CHECK constraints: `PENDING`, `CONFIRMED` (by shop owner), `PAID` (wire completed), `CANCELLED`. |
| 11 | `calculated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Automatic ledger run timestamp. |
| 12 | `confirmed_at` | | | | | **DATETIME**: Shop owner confirmation timestamp. |
| 13 | `paid_at` | | | | | **DATETIME**: Wire transaction execution timestamp. |
| 14 | `created_by` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the accountant Admin who initiated payout. |
| 15 | `note` | | | | | **NVARCHAR(500)**: Internal accounting notes. |

---

#### 2.2.17 shop_settlement_orders
Stores order mapping logs to guarantee that an order is settled once.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `settlement_order_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing identifier. |
| 02 | `settlement_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `shop_settlements(settlement_id)`. Cascaded on session removal. |
| 03 | `order_id` | | ✓ | ✓ | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Unique constraint enforces a single payout process. |
| 04 | `order_amount` | | | | ✓ | **DECIMAL(14,2)**: Total order value calculated at settlement. |
| 05 | `platform_fee_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Commission platform fee cut on this order. |
| 06 | `discount_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Voucher discount adjustments evaluated. |
| 07 | `refund_amount` | | | | ✓ | **DECIMAL(14,2) (DEFAULT 0)**: Refund deductions charged against this order. |
| 08 | `net_amount` | | | | ✓ | **DECIMAL(14,2)**: Net payout sum generated on this order (VND). |

---

#### 2.2.18 payment_transactions
Stores digital payment records (bank transfer automated integrations via SePay or COD).

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `transaction_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing payment identifier. |
| 02 | `order_id` | | ✓ | ✓ | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Enforces a strict 1-to-1 relationship per active order. |
| 03 | `payment_method` | | | | ✓ | **NVARCHAR(30) (DEFAULT 'SEPAY')**: Selected method (e.g., 'SEPAY' or 'COD'). |
| 04 | `sepay_transaction_id` | | | ✓ | | **NVARCHAR(100)**: Unique bank transfer ID returned by SePay webhook. |
| 05 | `sepay_reference` | | | | | **NVARCHAR(100)**: Autogenerated payment description text (e.g., "DH101") serving as the matching key. |
| 06 | `sepay_qr_code` | | | | | **NVARCHAR(500)**: CDN URL of the dynamically generated payment QR code shown to the client. |
| 07 | `amount` | | | | ✓ | **DECIMAL(14,2)**: Paid value. Must match `orders.final_amount` exactly. |
| 08 | `currency` | | | | ✓ | **NVARCHAR(3) (DEFAULT 'VND')**: ISO currency tag (e.g., 'VND'). |
| 09 | `status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'pending')**: Transaction state. CHECK constraints: `pending`, `processing`, `completed` (funds received), `failed`, `cancelled`, `refunded`, `expired`. |
| 10 | `initiated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Timestamp of transaction creation and QR generation. |
| 11 | `completed_at` | | | | | **DATETIME**: Webhook callback matching confirmation timestamp. |
| 12 | `expires_at` | | | | | **DATETIME**: QR expiration timeout (typically 15 minutes post-initiation). |
| 13 | `provider_response` | | | | | **NVARCHAR(MAX)**: Raw JSON response callback payload received from SePay for audit purposes. |
| 14 | `error_code` | | | | | **NVARCHAR(50)**: Error code string returned by the gateway. |
| 15 | `error_message` | | | | | **NVARCHAR(500)**: Human-readable error message details for client support. |
| 16 | `ip_address` | | | | | **NVARCHAR(45)**: Client device IP address logged during transaction initiation. |

---

#### 2.2.19 sepay_webhook_dedup
Maintains bank transaction callbacks to prevent duplicate processing of webhook payloads (Idempotency ledger).

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `dedup_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing ledger identifier. |
| 02 | `sepay_transaction_id` | | | ✓ | ✓ | **NVARCHAR(100)**: Bank transaction ID used to perform duplicates verification. |
| 03 | `order_code` | | | | ✓ | **NVARCHAR(100)**: Matched reference memo text parsed (e.g., "DH101"). |
| 04 | `process_result` | | | | ✓ | **NVARCHAR(30) (DEFAULT 'processed')**: Result of processing (e.g., 'processed'). |
| 05 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Logging timestamp. |

---

#### 2.2.20 deliveries
Tracks shipper dispatches and delivery tracking progress.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `delivery_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing delivery identifier. |
| 02 | `order_id` | | ✓ | ✓ | ✓ | **INT**: Foreign Key referencing `orders(order_id)`. Enforces a strict 1-to-1 relationship per order. |
| 03 | `staff_id` | | ✓ | | | **INT**: Foreign Key referencing `users(user_id)` indicating the assigned Shipper. |
| 04 | `status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'ASSIGNED')**: Shipping state. CHECK constraints: `ASSIGNED`, `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, `FAILED`. |
| 05 | `picked_up_at` | | | | | **DATETIME**: Shipper pick-up confirmation timestamp at the vendor warehouse. |
| 06 | `delivered_at` | | | | | **DATETIME**: Client delivery confirmation timestamp. |
| 07 | `failure_reason` | | | | | **NVARCHAR(300)**: Notes documenting delivery failure (e.g., "Unresponsive phone"). |
| 08 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Dispatch logging timestamp. |
| 09 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Latest state modification timestamp. |

---

#### 2.2.21 reviews
Stores customer feedback and ratings on purchased items.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `review_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing review identifier. |
| 02 | `order_item_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `order_items(order_item_id)`. |
| 03 | `customer_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the writing Customer. |
| 04 | `rating` | | | | ✓ | **TINYINT**: Rating scale. CHECK constraints enforce a range between `1` and `5` stars. |
| 05 | `review_text` | | | | | **NVARCHAR(1000)**: Written testimonial comments of customer experience. |
| 06 | `is_hidden` | | | | ✓ | **BIT (DEFAULT 0)**: Moderation flag (1: Hidden/Inappropriate; 0: Visible). |
| 07 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Submission timestamp. |

*Business Rule Constraint*: The composite unique key constraint `UQ_review_customer_item` is applied to columns `(customer_id, order_item_id)` to guarantee a customer can review an individual purchased item only once.

---

#### 2.2.22 chat_sessions
Manages active chat sessions connecting customers directly with shop owners.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `session_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing chat room identifier. |
| 02 | `customer_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the participating Customer. |
| 03 | `owner_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the participating Shop Owner. |
| 04 | `status` | | | | ✓ | **NVARCHAR(20) (DEFAULT 'ACTIVE')**: Room state. CHECK constraints: `ACTIVE` (open), `CLOSED`. |
| 05 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Chat initiation timestamp. |
| 06 | `updated_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Last message timestamp (utilized to sort active rooms on UI). |
| 07 | `closed_at` | | | | | **DATETIME**: Room closure timestamp. |

---

#### 2.2.23 chat_messages
Stores message history inside active chat sessions.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `message_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing message identifier. |
| 02 | `session_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `chat_sessions(session_id)`. Cascaded on chat room removal. |
| 03 | `sender_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)` identifying the writing author. |
| 04 | `content` | | | | ✓ | **NVARCHAR(MAX)**: Text message payload sent by the author. |
| 05 | `is_read` | | | | ✓ | **BIT (DEFAULT 0)**: Unread state indicator flag (1: Read; 0: Unread) to trigger UI alert dots. |
| 06 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Message sent timestamp. |

---

#### 2.2.24 notifications
Stores automated user notifications pushed in-app.

\* PK~Primary Key; FK~Foreign Key; UN~Unique; NN ~ not null

| No | Field | PK | FK | UN | NN | Description |
|---|---|:---:|:---:|:---:|:---:|---|
| 01 | `notification_id` | ✓ | | | ✓ | **INT IDENTITY(1,1)**: Primary Key. Auto-incrementing notification identifier. |
| 02 | `user_id` | | ✓ | | ✓ | **INT**: Foreign Key referencing `users(user_id)`. Cascaded automatically on user account removal. |
| 03 | `type` | | | | ✓ | **NVARCHAR(50)**: UI classification. CHECK: `ORDER_UPDATE`, `PROMOTION`, `SYSTEM`, `INVENTORY_ALERT`, `PAYMENT`. |
| 04 | `title` | | | | ✓ | **NVARCHAR(200)**: Heading summary text of the alert (e.g., "Order Dispatched!"). |
| 05 | `message` | | | | ✓ | **NVARCHAR(MAX)**: Detailed text description of the notification. |
| 06 | `action_url` | | | | | **NVARCHAR(300)**: In-app route link matching this event (e.g., "/order/detail?id=101"). |
| 07 | `is_read` | | | | ✓ | **BIT (DEFAULT 0)**: Unread indicator state flag (1: Read; 0: Unread). |
| 08 | `created_at` | | | | ✓ | **DATETIME (DEFAULT GETDATE())**: Dispatch timestamp. |

---

## 3. Recommended Word Conversion (Markdown to .docx)

To export this Markdown document directly into a premium Word Document (.docx) mirroring the pastel palette structure in your design guidelines:

### Method 1: Using Pandoc (Developer Grade)
Run the following terminal command to convert the markdown file using a default professional stylesheet template:
```bash
pandoc -s docs/SRS_Database_Specification.md -o docs/SRS_Database_Specification.docx --reference-doc=template.docx
```

### Method 2: Web-Based Exporters (Instant Setup)
1. Open **Dillinger.io** or **Markdown-to-DOCX Converter** in your browser.
2. Paste the full content of this `SRS_Database_Specification.md` file.
3. Click **Export** or **Download as DOCX**.

### Stylistic Adjustments Inside Microsoft Word:
1. Open the generated `.docx` file in MS Word.
2. Click on any of the database tables to open the **Table Design** ribbon.
3. Apply a neat black border grid style.
4. Select the table header row (containing *No, Field, PK, FK, UN, NN, Description*).
5. Open **Shading** (the bucket tool) and select a soft pastel orange color (HEX Code: `#FCE4D6` or `#F7D6C8` matching your exact screenshot).
6. Format the *No* and *Field* columns with **blue italic font** (`#0563C1`) to replicate the exact visual scheme of the software requirements specification.
