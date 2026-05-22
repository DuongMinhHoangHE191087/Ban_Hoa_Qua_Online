# SOFTWARE REQUIREMENT SPECIFICATION
## Hệ thống bán hoa quả online
### Core Requirements Draft - Version 0.1

**Mục đích tài liệu**  
Tài liệu này mô tả bộ yêu cầu chức năng và nghiệp vụ cốt lõi cho hệ thống bán hoa quả online theo mô hình marketplace. Đây là nền tảng để phát triển tiếp các phần chi tiết của SRS như use case specification, màn hình chức năng, rule nghiệp vụ, báo cáo, settlement và mở rộng AI.

**Phạm vi hệ thống**  
Hệ thống hỗ trợ các vai trò: Guest, Customer, Shop Owner, Delivery Staff và Admin. Hệ thống cho phép duyệt sản phẩm, tìm kiếm/lọc, đăng ký và đăng nhập, quản lý giỏ hàng, đặt hàng, thanh toán, quản lý shop, quản lý sản phẩm, giao hàng, khiếu nại/hoàn trả, đối soát settlement và trao đổi chat giữa khách hàng với shop.

---

# 1. Overall Requirements

## 1.1 System Overview
Hệ thống là sàn bán hoa quả online cho nhiều shop khác nhau. Mỗi shop quản lý sản phẩm, tồn kho, đơn hàng và doanh thu riêng. Khách hàng có thể duyệt sản phẩm, đặt hàng theo từng shop, thanh toán, theo dõi giao hàng và gửi yêu cầu hoàn trả nếu có vấn đề. Admin quản trị người dùng, shop, sản phẩm, đơn hàng và settlement.

## 1.2 Main Business Processes

### 1.2.1 Product Discovery
Guest hoặc Customer truy cập trang chủ, xem danh mục, tìm kiếm, lọc sản phẩm, xem chi tiết và thông tin shop.

### 1.2.2 Order Checkout and Payment
Customer chọn sản phẩm, thêm vào giỏ hàng, nhập địa chỉ, chọn phương thức thanh toán, xác nhận đơn và theo dõi trạng thái thanh toán.

### 1.2.3 Shop Onboarding
Shop Owner đăng ký mở shop, gửi hồ sơ và giấy tờ, Admin duyệt hoặc từ chối theo điều kiện kinh doanh.

### 1.2.4 Fulfillment and Delivery
Shop xác nhận đơn, chuẩn bị hàng, bàn giao cho Delivery Staff, theo dõi trạng thái giao và hoàn tất đơn.

### 1.2.5 After-Sales Handling
Customer tạo yêu cầu hủy, hoàn tiền, đổi trả hoặc khiếu nại; Shop/Admin xử lý theo chính sách.

### 1.2.6 Settlement
Sau khi đơn hoàn thành và qua thời gian khiếu nại, hệ thống tổng hợp doanh thu, phí nền tảng, hoàn tiền và tạo settlement cho shop.

## 1.3 User Requirements

### 1.3.1 Actors

| # | Actor | Description |
| --- | --- | --- |
| 1 | Guest | Unregistered visitor who can browse products, manage a temporary cart, and place orders. A Customer account is automatically created for them upon successful checkout. |
| 2 | Customer | Registered buyer who can manage their cart, place orders, make payments, track orders, chat with shops, submit product reviews, and request cancellations/returns/exchanges. |
| 3 | Shop Owner | Shop seller who manages their storefront, products, inventory, orders, promotions, chat, and settlements. |
| 4 | Delivery Staff | Courier who accepts delivery tasks and updates shipment statuses. |
| 5 | Admin | System administrator who manages users, approves shop registrations, moderates products, and processes shop settlements. |
| 6 | Payment Gateway / Webhook | External system that processes payments and updates transaction statuses. |

### 1.3.2 Core Use Case List

| ID | Use Case | Actor | Priority | Description |
| --- | --- | --- | --- | --- |
| UC-01 | Register Account | Guest | P0 | Register a Customer or Shop Owner account. |
| UC-02 | Login / Logout | Guest / Customer / Shop Owner / Delivery / Admin | P0 | Authenticate users (login/logout) in the system. |
| UC-03 | Browse Products | Guest | P0 | Browse product lists, categories, and details. |
| UC-04 | Search and Filter Products | Guest | P0 | Search and filter products by keyword, price, rating, location, and fruit type. |
| UC-05 | Manage Guest Cart | Guest | P0 | Store temporary shopping cart in localStorage/sessionStorage. |
| UC-06 | Manage Customer Cart | Customer | P0 | Add, edit, or delete items in the customer's shopping cart. |
| UC-07 | Place Order | Guest / Customer | P0 | Place an order that belongs to exactly one shop. For Guest, checkout automatically creates a Customer account. |
| UC-08 | Make Payment | Guest / Customer | P0 | Make a payment via bank transfer or Cash on Delivery (COD). |
| UC-09 | Track Order | Customer | P0 | Track real-time order status and view purchase history. |
| UC-10 | Shop Registration | Customer / Shop Owner | P0 | Submit a shop registration request for Admin approval. |
| UC-11 | Manage Products | Shop Owner | P0 | Create, update, toggle visibility of products, variants, and images. |
| UC-12 | Manage Inventory | Shop Owner | P0 | Update stock levels and track inventory changes. |
| UC-13 | Confirm and Process Orders | Shop Owner | P0 | Confirm orders, prepare packaging, and assign/handover to delivery. |
| UC-14 | Delivery Assignment and Update | Delivery Staff | P0 | Accept delivery tasks and update transit status. |
| UC-15 | Chat Support | Customer / Shop Owner | P1 | Chat directly between customers and shop owners for order/product support. |
| UC-16 | Review Product | Customer | P1 | Submit product reviews and ratings after order completion. |
| UC-17 | Cancel / Return / Exchange Request | Customer | P1 | Submit order cancellation, return, or exchange requests based on policies. |
| UC-18 | Manage Promotions | Shop Owner / Admin | P1 | Create, edit, or disable shop-specific or platform-wide promotional codes. |
| UC-19 | Settlement Management | Admin / Shop Owner | P1 | Aggregate revenues, platform fees, refunds, and process shop settlements. |
| UC-20 | Recommendation | System | P2 | Recommend products based on season, purchase history, and user preferences. |

### 1.3.3 Use Case Grouping by Actor
- Guest: UC-01, UC-03, UC-04, UC-05, UC-07, UC-08.
- Customer: UC-02, UC-06, UC-07, UC-08, UC-09, UC-10, UC-15, UC-16, UC-17.
- Shop Owner: UC-02, UC-11, UC-12, UC-13, UC-15, UC-18, UC-19.
- Delivery Staff: UC-02, UC-14.
- Admin: UC-02, UC-10, UC-18, UC-19.

## 1.4 System Functionalities

### 1.4.1 Screen Flow Summary
Home Page -> Product Listing -> Product Detail -> Cart -> Checkout -> Payment -> Order Tracking -> Review / Return Request.

Admin flow: Login -> Dashboard -> User Management -> Shop Approval -> Product Moderation -> Settlement -> Reports.

Shop flow: Login -> Shop Dashboard -> Product Management -> Inventory -> Order Management -> Chat -> Promotions -> Settlement.

### 1.4.2 Screen Authorization

| Screen / Function | Guest | Customer | Shop Owner | Delivery Staff | Admin |
| --- | --- | --- | --- | --- | --- |
| Home / Product List / Product Detail | X | X | X | X | X |
| Guest Cart | X |  |  |  |  |
| Customer Cart |  | X |  |  |  |
| Checkout / Payment | X | X |  |  |  |
| Order Tracking |  | X | X | X | X |
| Shop Registration |  | X | X |  | X |
| Shop Dashboard |  |  | X |  | X |
| Product Management |  |  | X |  | X |
| Inventory Management |  |  | X |  | X |
| Delivery Dashboard |  |  |  | X | X |
| User Management |  |  |  |  | X |
| Shop Approval |  |  |  |  | X |
| Settlement Dashboard |  |  | X |  | X |
| Chat |  | X | X |  | X |

### 1.4.3 Non-UI Functions

| # | Feature | System Function | Description |
| --- | --- | --- | --- |
| 1 | Authentication | Token / session handling | Quản lý đăng nhập, xác thực, khóa tài khoản khi sai nhiều lần. |
| 2 | Cart Sync | Guest-to-customer sync | Đồng bộ giỏ hàng từ localStorage lên server khi khách đăng nhập. |
| 3 | Payment | Payment webhook processing | Nhận webhook từ cổng thanh toán, cập nhật trạng thái giao dịch. |
| 4 | Inventory | Stock reservation and release | Giữ tồn kho khi đặt hàng, trừ tồn khi xác nhận, hoàn tồn khi hủy. |
| 5 | Notification | System notifications | Gửi thông báo đơn hàng, khuyến mãi, tồn kho và thanh toán. |
| 6 | Settlement | Shop settlement batch job | Tính doanh thu shop theo kỳ và tổng hợp phí/hoàn tiền. |
| 7 | Moderation | Product / shop review workflow | Hỗ trợ admin duyệt shop và kiểm duyệt sản phẩm. |
| 8 | Recommendation | Seasonal recommendation service | Gợi ý sản phẩm theo mùa và lịch sử mua hàng. |

## 1.5 Entities Description

In a conceptual Entity-Relationship Diagram (ERD), we focus on the **core business domain entities** that represent the main business flow: **selling, buying, payment, fulfillment, and feedback**. Pure junction tables (e.g., `order_items`, `order_promotions`) and remaining deferred entities are modeled in the physical schema.

Below are the **11 Core Domain Entities** of the Online Fruit Shopping Platform (5 Strong + 6 Weak):

| # | Entity | Type | Concept | Description |
| --- | --- | --- | --- | --- |
| 01 | `users` | **Strong** | **User** | All actors on the platform (Customers, Shop Owners, Delivery Staff, Admins). Central entity — referenced directly by products, orders, deliveries, reviews, and chat. |
| 02 | `shop_owner_profiles` | **Weak** (of User) | **Shop** | Registered vendor storefront. Depends on `users` (1:1). Carries business state: shop_name, approval_status (PENDING→APPROVED→REJECTED→SUSPENDED), rating. |
| 03 | `categories` | **Strong** | **Category** | Classifications for fresh fruits and agricultural goods. Referenced by `products.category_id`. |
| 04 | `products` | **Strong** | **Product** | Agricultural listings owned by a Shop Owner User, classified by Category. Has variants and images as sub-entities. |
| 05 | `product_variants` | **Weak** (of Product) | **Variant** | Packaging configurations (e.g., Box 1kg, Crate 10kg) with price and stock. `order_items.variant_id` references this — not `products`. |
| 06 | `promotions` | **Strong** | **Promotion** | Voucher campaigns and coupon codes with lifecycle (valid_from → valid_until). Linked to orders via hidden junction `order_promotions`. |
| 07 | `orders` | **Strong** | **Order** | Core purchase transactions between a Customer and a Shop Owner. Lifecycle: PENDING_PAYMENT → CONFIRMED → PREPARING → DISPATCHED → DELIVERED / CANCELLED. |
| 08 | `payment_transactions` | **Weak** (of Order) | **Payment** | Digital payment record (SePay QR or COD). 1:1 with Order. State: pending → completed / failed / refunded / expired. |
| 09 | `deliveries` | **Weak** (of Order) | **Delivery** | Shipping dispatch assigned to a Delivery Staff User. 1:1 with Order. State: ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED / FAILED. |
| 10 | `reviews` | **Weak** (of Order + User) | **Review** | Star rating and text feedback written by a Customer after order completion. Constrained to 1 review per customer per order item. |
| 11 | `chat_sessions` | **Weak** (of User × 2) | **Chat** | Support conversation thread between a Customer and a Shop Owner. Encapsulates `chat_messages` as a sub-entity. State: ACTIVE → CLOSED. |

**Deferred entities** (physical schema phase only):

| Entity | Role | Depends On |
| --- | --- | --- |
| `shop_settlements` | Periodic financial settlement payouts | `users` (owner) |
| `return_requests` | Cancellation / refund / exchange requests | `orders` |
| `cart` / `cart_items` | Shopping cart state (guest uses localStorage) | `users` / `product_variants` |
# 3. Functional Requirements

## 3.0 Section Guideline

This section follows the template structure Feature -> SubFeature -> Screen/Function. Each screen or function lists its purpose, related use cases, and the key fields or components required for the system.

## 3.1 User Authentication

### 3.1.1 Account Access

#### 3.1.1.1 Register Screen

UI Layout: Registration form with account type selector, verification area, and submit button.

Description: Allows a Guest to create a Customer or Shop Owner account. Related UC-01 and UC-10.

| Field Name | Description |
| --- | --- |
| Full Name | Required; 3-100 characters. |
| Email | Required; unique; valid email format. |
| Phone | Optional; max 15 characters. |
| Password | Required; 8-64 characters; stored as hash. |

#### 3.1.1.2 Login Screen

UI Layout: Login form with credential fields and login button.

Description: Authenticates Guest, Customer, Shop Owner, Delivery Staff, and Admin. Related UC-02.

| Field Name | Description |
| --- | --- |
| Email or Phone | Required login identifier. |
| Password | Required; must match stored hash. |
| Remember Me | Optional; keeps the session active longer. |
| Captcha / Anti-bot | Optional security control if enabled. |

#### 3.1.1.3 Forgot Password Screen

UI Layout: Password reset form with verification and new password inputs.

Description: Supports password reset after identity verification. Related UC-02.

| Field Name | Description |
| --- | --- |
| Email or Phone | Required for account lookup. |
| Reset Code | Required; one-time verification code. |
| New Password | Required; must satisfy password policy. |
| Confirm Password | Must match the new password. |

### 3.1.2 Session Security

#### 3.1.2.1 Login Lockout Function

UI Layout: Non-UI security function.

Description: Tracks failed logins, increments failed_login_count, and sets locked_until when the threshold is exceeded. Related UC-02.

| Parameter | Description |
| --- | --- |
| failed_login_count | Counter of consecutive failed attempts. |
| locked_until | Time until the account remains locked. |
| is_email_verified | Determines whether the account can proceed with sensitive actions. |

## 3.2 Product Discovery

### 3.2.1 Catalog Browsing

#### 3.2.1.1 Home / Product Listing Screen

UI Layout: Search bar, category tabs, filter panel, product grid, and pagination.

Description: Shows active products for Guest and authenticated users. Related UC-03 and UC-04.

| Field Name | Description |
| --- | --- |
| Keyword Search | Search by product name or description. |
| Category | Filter by fruit category. |
| Price Range | Filter by min and max price. |
| Rating Filter | Filter by star rating. |
| Shop Region | Filter by shop location or area. |
| Sort Order | Sort by popularity, newest, or price. |

#### 3.2.1.2 Product Detail Screen

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

### 3.3.2 Customer Cart and Checkout

#### 3.3.2.1 Cart Screen

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

#### 3.3.2.2 Checkout Screen

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

#### 3.3.2.3 Order Confirmation Screen

UI Layout: Confirmation message, order code, and next-step guidance.

Description: Shows the generated order code and payment status immediately after checkout. Related UC-07 and UC-08.

| Field Name | Description |
| --- | --- |
| Order Code | Unique code for tracking the order. |
| Payment Status | Pending, completed, failed, or expired. |
| Next Action | Pay now, wait for confirmation, or track order. |

## 3.4 Order, Payment and Tracking

### 3.4.1 Payment Processing

#### 3.4.1.1 Payment Screen

UI Layout: Payment details, QR code or bank reference area, and status banner.

Description: Displays payment instructions and transaction status. Related UC-08.

| Field Name | Description |
| --- | --- |
| Amount | Final payable amount. |
| Payment Method | Bank transfer or COD. |
| QR Code | QR image or payment link for transfer. |
| Reference Code | Payment reference to match the order. |
| Payment Expiry | Time limit for completing the transfer. |

#### 3.4.1.2 Payment Webhook Function

UI Layout: Non-UI integration function.

Description: Receives webhook events from the payment gateway and updates the payment transaction status. Related UC-08.

| Parameter | Description |
| --- | --- |
| sepay_transaction_id | Unique bank transaction identifier. |
| order_code | Order reference code from the payment content. |
| provider_response | Raw webhook payload for audit and reconciliation. |
| process_result | Result of deduplication or processing. |

### 3.4.2 Order Tracking

#### 3.4.2.1 Order Detail / Tracking Screen

UI Layout: Order summary, status timeline, delivery info, and after-sales action buttons.

Description: Lets the Customer, Shop Owner, Delivery Staff, and Admin track the order lifecycle. Related UC-09.

| Field Name | Description |
| --- | --- |
| Order Code | Identifier for the tracked order. |
| Status Timeline | PENDING_PAYMENT, CONFIRMED, PREPARING, DISPATCHED, DELIVERED, CANCELLED. |
| Order Items | Snapshot of purchased products and variants. |
| Delivery Info | Assigned staff and delivery timestamps. |
| After-Sales Action | Cancel, return, or exchange request entry point. |

### 3.4.3 Delivery Management

#### 3.4.3.1 Delivery Dashboard Screen

UI Layout: Assigned delivery list, status cards, and failure handling panel.

Description: Used by Delivery Staff to process assigned orders. Related UC-14.

| Field Name | Description |
| --- | --- |
| Assigned Orders | Orders currently assigned to the staff member. |
| Pickup Status | Indicates whether the parcel has been picked up. |
| In Transit Status | Indicates whether the order is on the way. |
| Failure Reason | Required when delivery fails. |

#### 3.4.3.2 Delivery Status Update Function

UI Layout: Non-UI status transition function.

Description: Updates the delivery status in the allowed order and delivery workflow. Related UC-14.

| Parameter | Description |
| --- | --- |
| status | ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, or FAILED. |
| picked_up_at | Time when the order is collected. |
| delivered_at | Time when the order is delivered. |
| failure_reason | Reason for unsuccessful delivery. |

## 3.5 Shop Management

### 3.5.1 Shop Onboarding

#### 3.5.1.1 Shop Registration Screen

UI Layout: Shop application form and document upload area.

Description: Allows a Customer or Shop Owner to submit a shop registration request. Related UC-10.

| Field Name | Description |
| --- | --- |
| Shop Name | Display name of the shop. |
| Shop Description | Introduction and selling profile. |
| Delivery Address | Pickup or business address. |
| Supporting Documents | Uploaded evidence required for approval. |
| Contact Information | Email, phone, or other contact details. |

#### 3.5.1.2 Shop Approval Screen

UI Layout: Request list, document preview, approval actions, and reason box.

Description: Used by Admin to approve, reject, or suspend a shop application. Related UC-10.

| Field Name | Description |
| --- | --- |
| Application List | Pending shop applications. |
| Approval Status | Pending, approved, rejected, or suspended. |
| Rejection Reason | Required when the application is rejected. |
| Approved At | Timestamp of approval. |

### 3.5.2 Product Management

#### 3.5.2.1 Product List Screen

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

#### 3.5.2.2 Product Form Screen

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

#### 3.5.2.3 Variant / Image Management Function

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

### 3.5.3 Inventory Management

#### 3.5.3.1 Inventory Adjustment Screen

UI Layout: Inventory adjustment form and history table.

Description: Allows the Shop Owner to update stock quantities and record inventory logs. Related UC-12.

| Field Name | Description |
| --- | --- |
| Variant | Target product variant. |
| Change Type | Manual adjust, order reserve, order release, order confirm, or return. |
| Quantity Delta | Positive or negative stock change. |
| Quantity After | Stock after the change. |
| Note | Optional reason for adjustment. |

### 3.5.4 Promotion Management

#### 3.5.4.1 Promotion Management Screen

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

### 3.5.5 Shop Dashboard

#### 3.5.5.1 Shop Summary Screen

UI Layout: KPI cards, sales chart, top products, and inventory alerts.

Description: Gives the Shop Owner a quick operational summary of the shop. Related UC-19.

| Field Name | Description |
| --- | --- |
| Revenue | Sales total for the selected period. |
| Order Count | Number of orders in the period. |
| Best Sellers | Top-selling products. |
| Inventory Alerts | Low-stock or out-of-stock warnings. |
| Settlement Status | Current settlement state for the shop. |

## 3.6 After-Sales, Review and Settlement

### 3.6.1 Return / Exchange Request

#### 3.6.1.1 Request Screen

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

#### 3.6.1.2 Request Review Screen

UI Layout: Request detail view with approve/reject actions and decision notes.

Description: Used by Shop Owner or Admin to decide the request resolution. Related UC-17.

| Field Name | Description |
| --- | --- |
| Request Status | Requested, approved, rejected, processing, completed, or cancelled. |
| Decision Reason | Reason for approval or rejection. |
| Resolution Type | Refund, replacement, discount, or reject. |
| Refund Amount | Amount to refund if applicable. |
| Resolved At | Time when the request is finalized. |

### 3.6.2 Review

#### 3.6.2.1 Product Review Screen

UI Layout: Star rating input, comment box, and submit button.

Description: Lets the Customer review a product after the order is completed. Related UC-16.

| Field Name | Description |
| --- | --- |
| Order Item | Item being reviewed. |
| Rating | Rating from 1 to 5. |
| Review Text | Optional comment text. |
| Visibility | Hidden or visible status managed by Admin. |

### 3.6.3 Settlement

#### 3.6.3.1 Settlement Dashboard Screen

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

#### 3.6.3.2 Payout Confirmation Function

UI Layout: Non-UI financial confirmation function.

Description: Allows Admin to confirm payout and close a settlement after all conditions are met. Related UC-19.

| Parameter | Description |
| --- | --- |
| confirmed_at | Time of settlement confirmation. |
| paid_at | Time the payout was completed. |
| created_by | Admin or authorized finance user. |
| note | Optional payout note. |

## 3.7 Administration

### 3.7.1 User Management

#### 3.7.1.1 User List Screen

UI Layout: User table with filters by role and status.

Description: Allows Admin to review and manage all system users. Related UC-02 and UC-19.

| Field Name | Description |
| --- | --- |
| Full Name | User full name. |
| Email | Login email address. |
| Role | Customer, Shop Owner, Delivery Staff, or Admin. |
| Status | Active or inactive. |
| Locked Until | Lock expiration time if applicable. |

#### 3.7.1.2 User Details Screen

UI Layout: User profile, role controls, and security information panel.

Description: Shows full account information and administrative actions. Related UC-02.

| Field Name | Description |
| --- | --- |
| Profile Data | Name, phone, address, and other personal info. |
| Failed Login Count | Number of consecutive failed login attempts. |
| Email Verified | Verification status of the email address. |
| Account Status | Active or inactive. |

### 3.7.2 Category Management

#### 3.7.2.1 Category List Screen

UI Layout: Category table with search, sort, and active toggle.

Description: Allows Admin to manage the fruit category tree. Related UC-03 and UC-04.

| Field Name | Description |
| --- | --- |
| Name | Category name. |
| Slug | Unique URL-friendly identifier. |
| Display Order | Sorting order in the UI. |
| Is Active | Whether the category is visible to users. |

#### 3.7.2.2 Category Details Screen

UI Layout: Category edit form and active toggle.

Description: Maintains category master data and visibility settings. Related UC-03 and UC-04.

| Field Name | Description |
| --- | --- |
| Name | Category label. |
| Slug | URL-safe category code. |
| Display Order | Visible ordering. |
| Is Active | Active or inactive state. |

### 3.7.3 System Monitoring

#### 3.7.3.1 Order and Payment Monitoring Screen

UI Layout: Monitoring table with order, payment, and delivery status columns.

Description: Lets Admin monitor the system-wide order flow and payment lifecycle. Related UC-19.

| Field Name | Description |
| --- | --- |
| Order Code | Order identifier. |
| Payment Status | Pending, processing, completed, failed, cancelled, refunded, or expired. |
| Delivery Status | Delivery workflow status. |
| Exception Flag | Marks abnormal orders that need attention. |

#### 3.7.3.2 Report Dashboard Screen

UI Layout: Dashboard cards, charts, and export actions.

Description: Shows revenue, user growth, delivery performance, and return rate. Related UC-19.

| Field Name | Description |
| --- | --- |
| Revenue | Total platform or shop revenue. |
| User Growth | Growth trend of registered users. |
| Delivery Performance | Delivery success and failure metrics. |
| Return Rate | Number of after-sales requests and outcomes. |

## 3.8 Non-UI Functions

### 3.8.1 Notification Service

Description: Generates system notifications for orders, promotions, inventory alerts, payment events, and system messages.

| Parameter | Description |
| --- | --- |
| type | ORDER_UPDATE, PROMOTION, SYSTEM, INVENTORY_ALERT, or PAYMENT. |
| title | Notification title. |
| message | Notification content. |
| action_url | Destination when the user clicks the notification. |
| is_read | Read/unread flag. |

### 3.8.2 Cart Synchronization Service

Description: Synchronizes the guest cart from local storage to the authenticated customer cart.

| Parameter | Description |
| --- | --- |
| guest_cart_payload | Cart data stored in the browser. |
| customer_id | Logged-in user identifier. |
| sync_time | Synchronization time. |

### 3.8.3 Payment Webhook Service

Description: Processes webhook callbacks from the payment provider and prevents duplicate processing.

| Parameter | Description |
| --- | --- |
| sepay_transaction_id | Unique payment transaction identifier. |
| order_code | Order reference code. |
| provider_response | Raw response received from the provider. |
| process_result | Result of the deduplication and processing step. |

### 3.8.4 Inventory Reservation Service

Description: Reserves stock during checkout, releases stock on failure or cancellation, and updates inventory logs.

| Parameter | Description |
| --- | --- |
| order_id | Order being processed. |
| variant_id | Product variant affected. |
| reserved_quantity | Quantity held for the order. |
| released_quantity | Quantity returned to stock. |

### 3.8.5 Recommendation Service

Description: Suggests products based on purchase history, seasonal availability, and user preferences.

| Parameter | Description |
| --- | --- |
| user_history | Historical purchase or browsing data. |
| seasonality_signal | Seasonal availability signal. |
| recommended_items | Suggested products returned to the UI. |

# 4. Non-Functional Requirements

## 4.1 Security
- Hệ thống phải phân quyền theo vai trò.
- Mật khẩu phải được mã hóa trước khi lưu.
- Các thao tác nhạy cảm như duyệt shop, chốt settlement, hủy đơn phải có nhật ký audit.
- Webhook thanh toán phải có cơ chế dedup và kiểm tra tính hợp lệ.

## 4.2 Performance
- Trang danh sách sản phẩm nên phản hồi trong vòng 2 giây ở điều kiện tải bình thường.
- Thao tác checkout và tạo đơn không nên vượt quá 5 giây trong điều kiện hệ thống bình thường.
- Hệ thống phải hỗ trợ tìm kiếm và lọc theo từ khóa, giá và danh mục mà không làm treo giao diện.

## 4.3 Availability and Reliability
- Hệ thống nên đạt mức sẵn sàng tối thiểu 99.5% trong giờ vận hành.
- Dữ liệu đơn hàng, thanh toán và settlement phải có khả năng phục hồi khi có lỗi xử lý.

## 4.4 Usability
- Giao diện phải phù hợp mobile first vì phần lớn khách hàng mua trên điện thoại.
- Luồng mua hàng phải ngắn, rõ ràng và ít bước nhất có thể.
- Trạng thái đơn hàng và thanh toán phải hiển thị rõ ràng, dễ hiểu.

## 4.5 Maintainability and Extensibility
- Thiết kế phải cho phép bổ sung flash sale, wishlist, loyalty, AI recommendation và đa kho hàng mà không phá vỡ nghiệp vụ lõi.
- Cấu trúc dữ liệu phải tách biệt giữa nghiệp vụ lõi và mở rộng.

---

# 5. Requirement Appendix

## 5.1 Core Business Rules
- Mỗi đơn hàng chỉ thuộc một shop.
- Một sản phẩm chỉ thuộc một shop owner.
- Guest cart lưu tạm ở local storage, không bắt buộc tạo tài khoản.
- Review chỉ được tạo sau khi đơn hoàn thành.
- Settlement chỉ được chốt sau khi qua thời gian khiếu nại.
- Delivery Staff chỉ cập nhật trạng thái giao nhận.

## 5.2 State Flows to Standardize
- Order: PENDING_PAYMENT -> CONFIRMED -> PREPARING -> DISPATCHED -> DELIVERED
- Order exception: PENDING_PAYMENT -> PAYMENT_FAILED / EXPIRED / CANCELLED
- Delivery: ASSIGNED -> PICKED_UP -> IN_TRANSIT -> DELIVERED / FAILED
- Shop approval: PENDING -> APPROVED / REJECTED / SUSPENDED
- Return request: REQUESTED -> APPROVED / REJECTED -> PROCESSING -> COMPLETED
- Settlement: PENDING -> CONFIRMED -> PAID / CANCELLED

## 5.3 Expansion Scope
- AI recommendation.
- Seasonal product suggestion.
- Loyalty points and coupons.
- Flash sale and countdown promotion.
- Advanced reporting and export.
- Return handling automation and multi-step dispute resolution.

---

# 6. Notes for Further SRS Expansion
File này đóng vai trò là bản yêu cầu chuẩn và chính trước. Khi phát triển tiếp, có thể tách thành:
1. Use Case Specification chi tiết cho các luồng khó như checkout, settlement, return.
2. Screen/Function specification cho từng màn hình.
3. Data model và mapping từ SRS sang Schema.sql.
4. Test scenarios và acceptance criteria cho từng requirement.
