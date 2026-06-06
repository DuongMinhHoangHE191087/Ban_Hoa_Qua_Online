# BẢNG THEO DÕI TIẾN ĐỘ DỰ ÁN TOÀN DIỆN (PROJECT TRACKING SHEET)
## Dự án: Hệ Thống Bán Hoa Quả Online (Online Fruit Shop System)
### Tài Liệu Đặc Tả Kỹ Thuật Lập Trình Chi Tiết Từng Đầu Việc (Khớp 100% 103 Chức Năng Tiếng Anh)

Tài liệu này được thiết kế để các bạn sinh viên có thể **copy trực tiếp bảng Markdown dưới đây và dán vào Microsoft Excel hoặc Google Sheets**. Bảng dữ liệu đã được bóc tách chi tiết kỹ thuật cho **từng chức năng trong danh sách 103 chức năng tiếng Anh không gộp**, chỉ rõ file cần tạo, tham số truyền nhận, câu lệnh SQL chi tiết và kịch bản kiểm thử.

---

## 👥 Vai Trò Hệ Thống
* **`Guest`**: Khách vãng lai chưa đăng nhập.
* **`Customer`**: Khách hàng mua quả đã đăng nhập.
* **`Shop Owner`**: Chủ cửa hàng hoa quả.
* **`Delivery`**: Nhân viên giao hàng (Shipper).
* **`Admin`**: Quản trị viên hệ thống.
* **`System`**: Hệ thống xử lý tự động (Trigger, Batch Job).

---

## 📊 Bảng Theo Dõi Tiến Độ Master (Master Project Tracking Sheet)

| No. | Feature | Function | Main actors | Description | In Charge | RDS | Status | Level | LOC | Notes / Updates |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| **A** | **AUTHENTICATION, PROFILE & DISCOVERY** |  |  | **Goal: Sign-up, login, session security, and basic product discovery.** |  |  |  |  |  |  |
| 1 | Authentication - Sign-up | User Registration Local Form | Guest | Local sign-up form with input validation, BCrypt password hashing, and DB insertion. |  | I.1 | Doing | 2 | 90 |  |
| 2 | Authentication - Sign-up | Google OAuth Sign-up | Guest | Google OAuth sign-up with Callback URL handling and new user creation. |  | I.2 | Doing | 3 | 120 |  |
| 3 | Authentication - Sign-up | Role Selection | Guest | Role selection during sign-up with pending status for Shop Owners. |  | I.3 | Doing | 1 | 60 |  |
| 4 | Authentication - Login/Logout | Local Login | Guest / Customer / Shop Owner / Delivery / Admin | Local login with email/phone, BCrypt verification, and session storage. |  | I.4 | Doing | 2 | 90 |  |
| 5 | Authentication - Login/Logout | Google OAuth Sign-in | Guest / Customer / Shop Owner / Delivery / Admin | Google OAuth login with automatic matching and session creation. |  | I.5 | Doing | 3 | 120 |  |
| 6 | Authentication - Login/Logout | User Logout | Customer / Shop Owner / Delivery / Admin | Logout handling by clearing session data and redirecting to homepage. |  | I.6 | Doing | 1 | 60 |  |
| 7 | Authentication - Login/Logout | Login Lockout | Guest | Account lockout for 15 minutes after 5 consecutive failed login attempts. |  | I.7 | Doing | 3 | 120 |  |
| 8 | User Profile Management | View Profile | Customer / Shop Owner / Delivery / Admin | Profile page displaying user details fetched from session and DB. |  | I.8 | Doing | 2 | 90 |  |
| 9 | User Profile Management | Edit Profile | Customer / Shop Owner / Delivery / Admin | Profile editing form with data validation and database update. |  | I.9 | Doing | 3 | 120 |  |
| 10 | User Profile Management | Manage Addresses | Customer | Multiple shipping address management with default address toggle. |  | I.10 | Doing | 3 | 120 |  |
| 11 | Security - Password | Change Password | Customer / Shop Owner / Delivery / Admin | Change password form with old password verification and BCrypt hashing. |  | I.11 | Doing | 2 | 90 |  |
| 12 | Security - Access Control | Role Management | System | Role-Based Access Control (RBAC) filter protecting admin/shop/customer paths. |  | I.12 | Doing | 4 | 150 |  |
| 13 | Product Discovery | View Product List | Guest / Customer | Paginated active product list displaying name, image, and price. |  | I.13 | To Do | 3 | 120 |  |
| 14 | Product Discovery | View Product Details (Static) | Guest / Customer | Product details page displaying static info, origin, and expiry. |  | I.14 | To Do | 2 | 90 |  |
| 15 | Product Discovery | Browse by Category | Guest / Customer | Product filtering by category using sidebar or dropdown. |  | I.15 | To Do | 1 | 60 |  |
| 16 | Product Discovery | Browse Featured Products | Guest / Customer | Featured products section on homepage displaying top 8 items. |  | I.16 | To Do | 2 | 90 |  |
| 17 | Product Discovery | Search by Product Name | Guest / Customer | Product search bar with name wildcard matching and injection prevention. |  | I.17 | To Do | 2 | 90 |  |
| 18 | Admin User Management | Manage Customers | Admin | Admin panel: Paginated customer list display and search. |  | I.18 | To Do | 3 | 120 |  |
| 19 | Admin User Management | Manage Shop Owners | Admin | Admin panel: Paginated shop owner list and approval tracking. |  | I.19 | To Do | 3 | 120 |  |
| 20 | Admin User Management | Block/Unblock Accounts | Admin | Admin control: Block or unblock user accounts instantly. |  | I.20 | To Do | 2 | 90 |  |
| 21 | Admin User Management | Approve Shop Owner Accounts | Admin | Admin control: Approve pending Shop Owner accounts to active status. |  | I.21 | To Do | 2 | 90 |  |
| **B** | **PRODUCT & ORDER MANAGEMENT** |  |  | **Goal: CRUD products, cart, order placement, inventory, and basic shipping.** |  |  |  |  |  |  |
| 22 | Product Catalog Management | Add New Product | Shop Owner | Shop Owner: Add new product with image upload and DB insertion. |  | II.1 | To Do | 4 | 150 |  |
| 23 | Product Catalog Management | Edit Product Info | Shop Owner | Shop Owner: Edit product information and update in database. |  | II.2 | To Do | 4 | 150 |  |
| 24 | Product Catalog Management | Delete Listing | Shop Owner | Shop Owner: Soft delete product by updating status to DELETED. |  | II.3 | To Do | 1 | 60 |  |
| 25 | Product Catalog Management | Hide Listing | Shop Owner | Shop Owner: Toggle product visibility (ACTIVE or INACTIVE). |  | II.4 | To Do | 1 | 60 |  |
| 26 | Product Catalog Management | Product Images CRUD | Shop Owner | Shop Owner: CRUD operations for uploading multiple product images. |  | II.5 | To Do | 4 | 150 |  |
| 27 | Product Discovery (Advanced) | View Product Details (Advanced) | Guest / Customer | Dynamic product details page showing variants and slide show. |  | II.6 | To Do | 4 | 150 |  |
| 28 | Product Customization | Weight Variants | Shop Owner | Shop Owner: Manage weight variants with distinct price and stock. |  | II.7 | To Do | 3 | 120 |  |
| 29 | Product Customization | Packaging Options | Shop Owner | Customer: Select packaging options (gift box, foam) with dynamic pricing. |  | II.8 | To Do | 2 | 90 |  |
| 30 | Product Customization | Organic/Imported Labels | Shop Owner | Display Organic or Imported labels on products dynamically. |  | II.9 | To Do | 1 | 60 |  |
| 31 | Product Customization | Seasonal Availability | Shop Owner | Restrict product ordering to its configured seasonal availability. |  | II.10 | To Do | 3 | 120 |  |
| 32 | Stock & Inventory Control | Stock Quantity Tracking | System | System: Automatic stock deduction on purchase and return on cancellation. |  | II.11 | To Do | 3 | 120 |  |
| 33 | Stock & Inventory Control | Low Stock Alerts | Shop Owner | Shop Owner: Dashboard alerts for items with low stock quantity. |  | II.12 | To Do | 2 | 90 |  |
| 34 | Stock & Inventory Control | Restock Management | Shop Owner | Shop Owner: Log replenishment events and update inventory counts. |  | II.13 | To Do | 3 | 120 |  |
| 35 | Pricing Strategy | Base Pricing | Shop Owner | Shop Owner: Set base pricing for each product variant in VND. |  | II.14 | To Do | 2 | 90 |  |
| 36 | Pricing Strategy | Discount Pricing | Shop Owner | Shop Owner: Configure discount prices with slashed original prices. |  | II.15 | To Do | 2 | 90 |  |
| 37 | Advanced Search & Filters | Filter Category | Guest / Customer | Advanced product list filtering by selecting multiple categories. |  | II.16 | To Do | 3 | 120 |  |
| 38 | Advanced Search & Filters | Filter Price | Guest / Customer | Advanced product list filtering using a price range slider. |  | II.17 | To Do | 3 | 120 |  |
| 39 | Advanced Search & Filters | Filter Rating | Guest / Customer | Advanced product list filtering by minimum star rating. |  | II.18 | To Do | 2 | 90 |  |
| 40 | Advanced Search & Filters | Filter Availability | Guest / Customer | Advanced product list filtering to only show in-stock products. |  | II.19 | To Do | 2 | 90 |  |
| 41 | Advanced Search & Filters | Sort Products | Guest / Customer | Sort products by price, newest arrival, or top rating. |  | II.20 | To Do | 2 | 90 |  |
| 42 | Smart Product Recommendations | Recommended Fruits | Guest / Customer | Recommend similar products from the same category on detail pages. |  | II.21 | To Do | 2 | 90 |  |
| 43 | Smart Product Recommendations | Best Sellers | Guest / Customer | Homepage section displaying the top 5 best-selling products. |  | II.22 | To Do | 3 | 120 |  |
| 44 | Smart Product Recommendations | Recently Viewed | Guest / Customer | Display recently viewed products fetched from browser cookies. |  | II.23 | To Do | 3 | 120 |  |
| 45 | Shopping Cart System | Guest Cart LocalStorage | Guest | Guest shopping cart system stored locally in browser LocalStorage. |  | II.24 | To Do | 1 | 60 |  |
| 46 | Shopping Cart System | Customer Cart DB | Customer | Database-backed shopping cart system for registered customers. |  | II.25 | To Do | 3 | 120 |  |
| 47 | Shopping Cart System | Guest-Customer Cart Sync | Customer | Synchronize guest LocalStorage cart with DB cart upon login. |  | II.26 | To Do | 4 | 150 |  |
| 48 | Shopping Cart System | Update Quantity | Guest / Customer | Update cart item quantities with live stock checks and price recalculation. |  | II.27 | To Do | 2 | 90 |  |
| 49 | Shopping Cart System | Remove Product | Guest / Customer | Remove individual items or clear all products from the cart. |  | II.28 | To Do | 2 | 90 |  |
| 50 | Shopping Cart System | Cart Summary | Guest / Customer | Display cart totals including items, weight, and subtotal. |  | II.29 | To Do | 2 | 90 |  |
| 51 | Wishlist System | Add to Wishlist | Customer | Customer: Add favorite products to personal wishlist. |  | II.30 | To Do | 2 | 90 |  |
| 52 | Wishlist System | Remove from Wishlist | Customer | Customer: Remove items from wishlist. |  | II.31 | To Do | 1 | 60 |  |
| 53 | Wishlist System | Move Wishlist to Cart | Customer | Customer: Move items from wishlist to shopping cart directly. |  | II.32 | To Do | 3 | 120 |  |
| 54 | Checkout Process | Select Delivery Address | Customer | Customer: Select shipping address from profile list during checkout. |  | II.33 | To Do | 2 | 90 |  |
| 55 | Checkout Process | Order Confirmation | Customer | Order confirmation page with order ID and payment instructions. |  | II.34 | To Do | 2 | 90 |  |
| 56 | Order Placement | Create Order | Customer | Customer checkout: Transactional order and order items creation. |  | II.35 | To Do | 5 | 180 |  |
| 57 | Order Placement | Reservate Inventory | System | System: Hold stock during checkout and rollback if stock is insufficient. |  | II.36 | To Do | 4 | 150 |  |
| 58 | Customer Order Tracking | Reorder Previous Purchase | Customer | Customer: Quick reordering of items from historical purchases. |  | II.37 | Completed | 3 | 120 |  |
| 59 | Customer Order Tracking | Track Order Status | Customer | Customer: Track live order delivery status using visual timeline. |  | II.38 | Completed | 3 | 120 |  |
| 60 | Customer Order Tracking | Estimated Delivery Time | Customer / Shop Owner / Delivery | Display estimated delivery time set by the Shop Owner. |  | II.39 | Completed | 1 | 60 |  |
| 61 | Customer Order Tracking | Order Delivery Confirmation | Customer | Customer: Confirm receipt of order to update status to DELIVERED. |  | II.40 | Completed | 2 | 90 |  |
| 62 | Delivery Staff Flow | View Orders for Delivery | Delivery | Delivery Staff: Dashboard with pending delivery jobs and location filters. |  | II.41 | Completed | 3 | 120 |  |
| 63 | Delivery Staff Flow | Update Shipping Status | Delivery | Delivery Staff: Update delivery status to Shipped, Delivered, or Failed. |  | II.42 | Completed | 2 | 90 |  |
| 64 | Shop Owner Order Processing | Approve Order Shop Owner | Shop Owner | Shop Owner: Approve orders and trigger packing process. |  | II.43 | Completed | 3 | 120 |  |
| 65 | Shop Owner Order Processing | Reject Order Shop Owner | Shop Owner | Shop Owner: Cancel orders with a mandatory reason and restock items. |  | II.44 | Completed | 3 | 120 |  |
| 66 | Shop Owner Order Processing | Mark Order as Delivered | Shop Owner / Delivery | Shop Owner/Delivery: Manually mark orders as delivered to close sale. |  | II.45 | Completed | 2 | 90 |  |
| 67 | Content Moderation & Categories | Approve Products | Admin | Admin: Review and approve new product listings to active status. |  | II.46 | To Do | 3 | 120 |  |
| 68 | Content Moderation & Categories | Remove Inappropriate Products | Admin | Admin: Block or hide violating product listings from the public. |  | II.47 | To Do | 2 | 90 |  |
| 69 | Content Moderation & Categories | Manage Categories CRUD | Admin | Admin: CRUD operations for managing product categories. |  | II.48 | To Do | 4 | 150 |  |
| **C** | **PAYMENT, REPORTS, REVIEWS, NOTIFICATIONS & ADMIN** |  |  | **Goal: Automatic SePay payment, reports, reviews, notifications, and settlement.** |  |  |  |  |  |  |
| 70 | Online Payment Integration | Cash on Delivery COD | Customer | Customer checkout: Support Cash on Delivery (COD) payment option. |  | III.1 | To Do | 2 | 90 |  |
| 71 | Online Payment Integration | E-wallet VietQR Dynamic QR | Customer | Customer checkout: Dynamic VietQR code generation for instant payments. |  | III.2 | To Do | 4 | 150 |  |
| 72 | Online Payment Integration | Process Payment | System | System: Handle incoming payment webhook requests with signature validation. |  | III.3 | To Do | 5 | 180 |  |
| 73 | Online Payment Integration | Payment Confirmation SePay Webhook | System | System: SePay webhook receiver to auto-approve orders on payment. |  | III.4 | To Do | 6 | 210 |  |
| 74 | Online Payment Integration | Real-time Status Polling | Customer | Customer checkout: Real-time API polling for payment success. |  | III.5 | To Do | 2 | 90 |  |
| 75 | Online Payment Integration | Payment Refund Logic | Admin | Admin control: Process refunds for cancelled or returned orders. |  | III.6 | To Do | 4 | 150 |  |
| 76 | Discount Promotion Engine | Apply Coupon Promotion | Customer | Customer checkout: Apply coupon discount codes for price deduction. |  | III.7 | To Do | 4 | 150 |  |
| 77 | Invoice & Billing | Generate Digital Invoice | System | System: Auto-generate digital invoice after successful payment. |  | III.8 | To Do | 3 | 120 |  |
| 78 | Invoice & Billing | Download Receipt PDF/HTML | Customer | Customer: Download purchase receipt as PDF or clean HTML. |  | III.9 | To Do | 5 | 180 |  |
| 79 | Order Returns & Refund Requests | Create Return/Exchange Request | Customer | Customer: Submit return/exchange requests with photo evidence. |  | III.10 | To Do | 4 | 150 |  |
| 80 | Customer Order History | View Order History Detail | Customer | Customer: View order history details, timeline, and invoice. |  | III.11 | To Do | 3 | 120 |  |
| 81 | Marketing Campaigns | Discount Coupons Creation | Shop Owner / Admin | Shop Owner: Create and manage promotional discount coupons. |  | III.12 | To Do | 4 | 150 |  |
| 82 | Marketing Campaigns | Seasonal Campaigns | Shop Owner / Admin | Shop Owner: Create automated seasonal discount campaigns. |  | III.13 | To Do | 4 | 150 |  |
| 83 | Marketing Campaigns | Flash Sales Configuration | Shop Owner / Admin | Shop Owner: Create timed Flash Sale events with limited stock. |  | III.14 | To Do | 4 | 150 |  |
| 84 | Product Review & Rating System | Write Product Review | Customer | Customer: Submit product reviews with ratings and image attachments. |  | III.15 | To Do | 4 | 150 |  |
| 85 | Product Review & Rating System | Edit / Delete Product Review | Customer | Customer: Edit or delete personal product reviews and recalculate average. |  | III.16 | To Do | 3 | 120 |  |
| 86 | Product Review & Rating System | Rate Products Stars | Customer | Customer: Interactive star-rating widget (1-5 stars) for reviews. |  | III.17 | To Do | 1 | 60 |  |
| 87 | Product Review & Rating System | Average Rating Calculation | System | System: Automatically update average rating for products. |  | III.18 | To Do | 2 | 90 |  |
| 88 | Product Review & Rating System | Approve/Reject Reviews | Admin | Admin: Review moderation panel to approve or block user reviews. |  | III.19 | To Do | 3 | 120 |  |
| 89 | Notification & Alert Center | Order Confirmation | System | System: Send email and in-app notifications upon order creation. |  | III.20 | To Do | 2 | 90 |  |
| 90 | Notification & Alert Center | Payment Notification | System | System: Send automated payment receipt notifications to customers. |  | III.21 | To Do | 2 | 90 |  |
| 91 | Notification & Alert Center | Delivery Status Updates | System | System: Notify customers of shipping updates (Shipped/Delivered). |  | III.22 | To Do | 2 | 90 |  |
| 92 | Notification & Alert Center | Promotion Notifications | Admin | Admin: Send promotional notifications to all customers in background. |  | III.23 | To Do | 4 | 150 |  |
| 93 | Notification & Alert Center | Low Stock Warning | System | System: Warn Shop Owners when variant inventory falls below 5 items. |  | III.24 | To Do | 2 | 90 |  |
| 94 | Notification & Alert Center | New Order Alert | System | System: Instant notification to Shop Owners on new order placement. |  | III.25 | To Do | 3 | 120 |  |
| 95 | Notification & Alert Center | Email Notifications | System | System: General email sender service utilizing JavaMail API. |  | III.26 | To Do | 4 | 150 |  |
| 96 | Notification & Alert Center | In-App Notifications bell | Customer | Customer: Real-time notification badge and dropdown on header. |  | III.27 | To Do | 3 | 120 |  |
| 97 | Global Admin Monitoring | Monitor Orders Global | Admin | Admin panel: Global order monitoring dashboard with multi-filters. |  | III.28 | To Do | 4 | 150 |  |
| 98 | Global Admin Monitoring | Monitor Payments Global | Admin | Admin panel: Monitor all transaction logs and VietQR bank transfers. |  | III.29 | To Do | 4 | 150 |  |
| 99 | Global Admin Monitoring | Handle Refund Requests | Admin | Admin panel: Review and approve customer refund requests. |  | III.30 | To Do | 4 | 150 |  |
| 100 | Analytics & Reporting | Revenue Report Chart.js | Shop Owner / Admin | Shop/Admin: Revenue visualization charts powered by Chart.js. |  | III.31 | To Do | 5 | 180 |  |
| 101 | Analytics & Reporting | Sales/Fruit Usage Report | Shop Owner / Admin | Shop/Admin: Advanced sales and fruit usage reports generation. |  | III.32 | To Do | 6 | 210 |  |
| 102 | Analytics & Reporting | Order Statistics | Shop Owner / Admin | Shop/Admin: Order success and cancellation statistics charts. |  | III.33 | To Do | 4 | 150 |  |
| 103 | Shop Settlement Batch Job | Shop Settlement Batch Job | System | System: Daily background job for auto-settlement of completed orders. |  | III.34 | To Do | 7 | 240 |  |
| **D** | **TOTAL ESTIMATION** |  |  | **Estimated total lines of code: 11,940 LOC (excluding CSS/JSP).** |  |  |  |  | **11,940** |  |
