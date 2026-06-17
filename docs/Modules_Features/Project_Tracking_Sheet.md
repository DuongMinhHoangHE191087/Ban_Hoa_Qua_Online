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
| 1 | Authentication - Sign-up | User Registration Local Form | Guest | Local sign-up form with input validation, BCrypt password hashing, and DB insertion. |  | I.1 | Completed | 2 | 90 | Implemented local registration with constraints. |
| 2 | Authentication - Sign-up | Google OAuth Sign-up | Guest | Google OAuth sign-up with Callback URL handling and new user creation. |  | I.2 | Completed | 3 | 120 | Google sign-up flow fully integrated. |
| 3 | Authentication - Sign-up | Role Selection | Guest | Role selection during sign-up with pending status for Shop Owners. |  | I.3 | Completed | 1 | 60 | Handles customer, delivery, and shop owner status. |
| 4 | Authentication - Login/Logout | Local Login | Guest / Customer / Shop Owner / Delivery / Admin | Local login with email/phone, BCrypt verification, and session storage. |  | I.4 | Completed | 2 | 90 | Password hashed local login. |
| 5 | Authentication - Login/Logout | Google OAuth Sign-in | Guest / Customer / Shop Owner / Delivery / Admin | Google OAuth login with automatic matching and session creation. |  | I.5 | Completed | 3 | 120 | Google oauth callback handling. |
| 6 | Authentication - Login/Logout | User Logout | Customer / Shop Owner / Delivery / Admin | Logout handling by clearing session data and redirecting to homepage. |  | I.6 | Completed | 1 | 60 | Secure logout. |
| 7 | Authentication - Login/Logout | Login Lockout | Guest | Account lockout for 15 minutes after 5 consecutive failed login attempts. |  | I.7 | Completed | 3 | 120 | Implemented security lockout. |
| 8 | User Profile Management | View Profile | Customer / Shop Owner / Delivery / Admin | Profile page displaying user details fetched from session and DB. |  | I.8 | Completed | 2 | 90 | User profile details view. |
| 9 | User Profile Management | Edit Profile | Customer / Shop Owner / Delivery / Admin | Profile editing form with data validation and database update. |  | I.9 | Completed | 3 | 120 | Updates user name, phone, address, and avatar. |
| 10 | User Profile Management | Manage Addresses | Customer | Multiple shipping address management with default address toggle. |  | I.10 | Completed | 3 | 120 | Shipping address list management. |
| 11 | Security - Password | Change Password | Customer / Shop Owner / Delivery / Admin | Change password form with old password verification and BCrypt hashing. |  | I.11 | Completed | 2 | 90 | BCrypt hashed change password. |
| 12 | Security - Access Control | Role Management | System | Role-Based Access Control (RBAC) filter protecting admin/shop/customer paths. |  | I.12 | Completed | 4 | 150 | RBAC authorization filter. |
| 13 | Product Discovery | View Product List | Guest / Customer | Paginated active product list displaying name, image, and price. |  | I.13 | Completed | 3 | 120 | Browse catalog lists with search and pagination. |
| 14 | Product Discovery | View Product Details (Static) | Guest / Customer | Product details page displaying static info, origin, and expiry. |  | I.14 | Completed | 2 | 90 | Standard static details display. |
| 15 | Product Discovery | Browse by Category | Guest / Customer | Product filtering by category using sidebar or dropdown. |  | I.15 | Completed | 1 | 60 | Filter catalog by category. |
| 16 | Product Discovery | Browse Featured Products | Guest / Customer | Featured products section on homepage displaying top 8 items. |  | I.16 | Completed | 2 | 90 | Home page featured items. |
| 17 | Product Discovery | Search by Product Name | Guest / Customer | Product search bar with name wildcard matching and injection prevention. |  | I.17 | Completed | 2 | 90 | wildcard name queries. |
| 18 | Admin User Management | Manage Customers | Admin | Admin panel: Paginated customer list display and search. |  | I.18 | Completed | 3 | 120 | Customer list view & block/unblock. |
| 19 | Admin User Management | Manage Shop Owners | Admin | Admin panel: Paginated shop owner list and approval tracking. |  | I.19 | Completed | 3 | 120 | Shop owner approvals and detail inspect. |
| 20 | Admin User Management | Block/Unblock Accounts | Admin | Admin control: Block or unblock user accounts instantly. |  | I.20 | Completed | 2 | 90 | Suspend/restore accounts. |
| 21 | Admin User Management | Approve Shop Owner Accounts | Admin | Admin control: Approve pending Shop Owner accounts to active status. |  | I.21 | Completed | 2 | 90 | Approve shop owner profile and sync role. |
| **B** | **PRODUCT & ORDER MANAGEMENT** |  |  | **Goal: CRUD products, cart, order placement, inventory, and basic shipping.** |  |  |  |  |  |  |
| 22 | Product Catalog Management | Add New Product | Shop Owner | Shop Owner: Add new product with image upload and DB insertion. |  | II.1 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 23 | Product Catalog Management | Edit Product Info | Shop Owner | Shop Owner: Edit product information and update in database. |  | II.2 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 24 | Product Catalog Management | Delete Listing | Shop Owner | Shop Owner: Soft delete product by updating status to DELETED. |  | II.3 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 25 | Product Catalog Management | Hide Listing | Shop Owner | Shop Owner: Toggle product visibility (ACTIVE or INACTIVE). |  | II.4 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 26 | Product Catalog Management | Product Images CRUD | Shop Owner | Shop Owner: CRUD operations for uploading multiple product images. |  | II.5 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 27 | Product Discovery (Advanced) | View Product Details (Advanced) | Guest / Customer | Dynamic product details page showing variants and slide show. |  | II.6 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 28 | Product Customization | Weight Variants | Shop Owner | Shop Owner: Manage weight variants with distinct price and stock. |  | II.7 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 29 | Product Customization | Packaging Options | Shop Owner | Customer: Select packaging options (gift box, foam) with dynamic pricing. |  | II.8 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 30 | Product Customization | Organic/Imported Labels | Shop Owner | Display Organic or Imported labels on products dynamically. |  | II.9 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 31 | Product Customization | Seasonal Availability | Shop Owner | Restrict product ordering to its configured seasonal availability. |  | II.10 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 32 | Stock & Inventory Control | Stock Quantity Tracking | System | System: Automatic stock deduction on purchase and return on cancellation. |  | II.11 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 33 | Stock & Inventory Control | Low Stock Alerts | Shop Owner | Shop Owner: Dashboard alerts for items with low stock quantity. |  | II.12 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 34 | Stock & Inventory Control | Restock Management | Shop Owner | Shop Owner: Log replenishment events and update inventory counts. |  | II.13 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 35 | Pricing Strategy | Base Pricing | Shop Owner | Shop Owner: Set base pricing for each product variant in VND. |  | II.14 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 36 | Pricing Strategy | Discount Pricing | Shop Owner | Shop Owner: Configure discount prices with slashed original prices. |  | II.15 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 37 | Advanced Search & Filters | Filter Category | Guest / Customer | Advanced product list filtering by selecting multiple categories. |  | II.16 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 38 | Advanced Search & Filters | Filter Price | Guest / Customer | Advanced product list filtering using a price range slider. |  | II.17 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 39 | Advanced Search & Filters | Filter Rating | Guest / Customer | Advanced product list filtering by minimum star rating. |  | II.18 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 40 | Advanced Search & Filters | Filter Availability | Guest / Customer | Advanced product list filtering to only show in-stock products. |  | II.19 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 41 | Advanced Search & Filters | Sort Products | Guest / Customer | Sort products by price, newest arrival, or top rating. |  | II.20 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 42 | Smart Product Recommendations | Recommended Fruits | Guest / Customer | Recommend similar products from the same category on detail pages. |  | II.21 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 43 | Smart Product Recommendations | Best Sellers | Guest / Customer | Homepage section displaying the top 5 best-selling products. |  | II.22 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 44 | Smart Product Recommendations | Recently Viewed | Guest / Customer | Display recently viewed products fetched from browser cookies. |  | II.23 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 45 | Shopping Cart System | Guest Cart LocalStorage | Guest | Guest shopping cart system stored locally in browser LocalStorage. |  | II.24 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 46 | Shopping Cart System | Customer Cart DB | Customer | Database-backed shopping cart system for registered customers. |  | II.25 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 47 | Shopping Cart System | Guest-Customer Cart Sync | Customer | Synchronize guest LocalStorage cart with DB cart upon login. |  | II.26 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 48 | Shopping Cart System | Update Quantity | Guest / Customer | Update cart item quantities with live stock checks and price recalculation. |  | II.27 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 49 | Shopping Cart System | Remove Product | Guest / Customer | Remove individual items or clear all products from the cart. |  | II.28 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 50 | Shopping Cart System | Cart Summary | Guest / Customer | Display cart totals including items, weight, and subtotal. |  | II.29 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 51 | Wishlist System | Add to Wishlist | Customer | Customer: Add favorite products to personal wishlist. |  | II.30 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 52 | Wishlist System | Remove from Wishlist | Customer | Customer: Remove items from wishlist. |  | II.31 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 53 | Wishlist System | Move Wishlist to Cart | Customer | Customer: Move items from wishlist to shopping cart directly. |  | II.32 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 54 | Checkout Process | Select Delivery Address | Customer | Customer: Select shipping address from profile list during checkout. |  | II.33 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 55 | Checkout Process | Order Confirmation | Customer | Order confirmation page with order ID and payment instructions. |  | II.34 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 56 | Order Placement | Create Order | Customer | Customer checkout: Transactional order and order items creation. |  | II.35 | Completed | 5 | 180 | 2026-06-07: Multi-shop checkout creates parent order + per-shop child orders, supports COD/CK parent payment reference, and passes regression tests. |
| 57 | Order Placement | Reservate Inventory | System | System: Hold stock during checkout and rollback if stock is insufficient. |  | II.36 | Completed | 4 | 150 | 2026-06-07: Inventory reservation is executed transactionally per child order item; rollback preserves stock/cart on failure. |
| 58 | Customer Order Tracking | Reorder Previous Purchase | Customer | Customer: Quick reordering of items from historical purchases. |  | II.37 | Completed | 3 | 120 |  |
| 59 | Customer Order Tracking | Track Order Status | Customer | Customer: Track live order delivery status using visual timeline. |  | II.38 | Completed | 3 | 120 |  |
| 60 | Customer Order Tracking | Estimated Delivery Time | Customer / Shop Owner / Delivery | Display estimated delivery time set by the Shop Owner. |  | II.39 | Completed | 1 | 60 |  |
| 61 | Customer Order Tracking | Order Delivery Confirmation | Customer | Customer: Confirm receipt of order to update status to DELIVERED. |  | II.40 | Completed | 2 | 90 |  |
| 62 | Delivery Staff Flow | View Orders for Delivery | Delivery | Delivery Staff: Dashboard with pending delivery jobs and location filters. |  | II.41 | Completed | 3 | 120 |  |
| 63 | Delivery Staff Flow | Update Shipping Status | Delivery | Delivery Staff: Update delivery status to Shipped, Delivered, or Failed. |  | II.42 | Completed | 2 | 90 |  |
| 64 | Shop Owner Order Processing | Approve Order Shop Owner | Shop Owner | Shop Owner: Approve orders and trigger packing process. |  | II.43 | Completed | 3 | 120 |  |
| 65 | Shop Owner Order Processing | Reject Order Shop Owner | Shop Owner | Shop Owner: Cancel orders with a mandatory reason and restock items. |  | II.44 | Completed | 3 | 120 |  |
| 66 | Shop Owner Order Processing | Mark Order as Delivered | Shop Owner / Delivery | Shop Owner/Delivery: Manually mark orders as delivered to close sale. |  | II.45 | Completed | 2 | 90 |  |
| 67 | Content Moderation & Categories | Approve Products | Admin | Admin: Review and approve new product listings to active status. |  | II.46 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 68 | Content Moderation & Categories | Remove Inappropriate Products | Admin | Admin: Block or hide violating product listings from the public. |  | II.47 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 69 | Content Moderation & Categories | Manage Categories CRUD | Admin | Admin: CRUD operations for managing product categories. |  | II.48 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| **C** | **PAYMENT, REPORTS, REVIEWS, NOTIFICATIONS & ADMIN** |  |  | **Goal: Automatic SePay payment, reports, reviews, notifications, and settlement.** |  |  |  |  |  |  |
| 70 | Online Payment Integration | Cash on Delivery COD | Customer | Customer checkout: Support Cash on Delivery (COD) payment option. |  | III.1 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 71 | Online Payment Integration | E-wallet VietQR Dynamic QR | Customer | Customer checkout: Dynamic VietQR code generation for instant payments. |  | III.2 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 72 | Online Payment Integration | Process Payment | System | System: Handle incoming payment webhook requests with signature validation. |  | III.3 | Completed | 5 | 180 | Verified completed and covered by unit/regression tests. |
| 73 | Online Payment Integration | Payment Confirmation SePay Webhook | System | System: SePay webhook receiver to auto-approve orders on payment. |  | III.4 | Completed | 6 | 210 | Verified completed and covered by unit/regression tests. |
| 74 | Online Payment Integration | Real-time Status Polling | Customer | Customer checkout: Real-time API polling for payment success. |  | III.5 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 75 | Online Payment Integration | Payment Refund Logic | Admin | Admin control: Process refunds for cancelled or returned orders. |  | III.6 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 76 | Discount Promotion Engine | Apply Coupon Promotion | Customer | Customer checkout: Apply coupon discount codes for price deduction. |  | III.7 | Completed | 4 | 150 | 2026-06-07: Direct sale, voucher shop per matching shop, and voucher sàn on parent order total are separated in checkout and regression-tested for multi-shop carts. |
| 77 | Invoice & Billing | Generate Digital Invoice | System | System: Auto-generate digital invoice after successful payment. |  | III.8 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 78 | Invoice & Billing | Download Receipt PDF/HTML | Customer | Customer: Download purchase receipt as PDF or clean HTML. |  | III.9 | Completed | 5 | 180 | Verified completed and covered by unit/regression tests. |
| 79 | Order Returns & Refund Requests | Create Return/Exchange Request | Customer | Customer: Submit return/exchange requests with photo evidence. |  | III.10 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 80 | Customer Order History | View Order History Detail | Customer | Customer: View order history details, timeline, and invoice. |  | III.11 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 81 | Marketing Campaigns | Discount Coupons Creation | Shop Owner / Admin | Shop Owner: Create and manage promotional discount coupons. |  | III.12 | Completed | 4 | 150 | PromotionServlet CRUD/toggle/delete and admin negative validation tests are included in the main JUnit runner. |
| 82 | Marketing Campaigns | Seasonal Campaigns | Shop Owner / Admin | Shop Owner: Create automated seasonal discount campaigns. |  | III.13 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 83 | Marketing Campaigns | Flash Sales Configuration | Shop Owner / Admin | Shop Owner: Create timed Flash Sale events with limited stock. |  | III.14 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 84 | Product Review & Rating System | Write Product Review | Customer | Customer: Submit product reviews with ratings and image attachments. |  | III.15 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 85 | Product Review & Rating System | Edit / Delete Product Review | Customer | Customer: Edit or delete personal product reviews and recalculate average. |  | III.16 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 86 | Product Review & Rating System | Rate Products Stars | Customer | Customer: Interactive star-rating widget (1-5 stars) for reviews. |  | III.17 | Completed | 1 | 60 | Verified completed and covered by unit/regression tests. |
| 87 | Product Review & Rating System | Average Rating Calculation | System | System: Automatically update average rating for products. |  | III.18 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 88 | Product Review & Rating System | Approve/Reject Reviews | Admin | Admin: Review moderation panel to approve or block user reviews. |  | III.19 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 89 | Notification & Alert Center | Order Confirmation | System | System: Send email and in-app notifications upon order creation. |  | III.20 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 90 | Notification & Alert Center | Payment Notification | System | System: Send automated payment receipt notifications to customers. |  | III.21 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 91 | Notification & Alert Center | Delivery Status Updates | System | System: Notify customers of shipping updates (Shipped/Delivered). |  | III.22 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 92 | Notification & Alert Center | Promotion Notifications | Admin | Admin: Send promotional notifications to all customers in background. |  | III.23 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 93 | Notification & Alert Center | Low Stock Warning | System | System: Warn Shop Owners when variant inventory falls below 5 items. |  | III.24 | Completed | 2 | 90 | Verified completed and covered by unit/regression tests. |
| 94 | Notification & Alert Center | New Order Alert | System | System: Instant notification to Shop Owners on new order placement. |  | III.25 | Completed | 3 | 120 | 2026-06-07: Multi-shop checkout sends preparation notifications to each affected shop owner after successful commit. |
| 95 | Notification & Alert Center | Email Notifications | System | System: General email sender service utilizing JavaMail API. |  | III.26 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 96 | Notification & Alert Center | In-App Notifications bell | Customer | Customer: Real-time notification badge and dropdown on header. |  | III.27 | Completed | 3 | 120 | Verified completed and covered by unit/regression tests. |
| 97 | Global Admin Monitoring | Monitor Orders Global | Admin | Admin panel: Global order monitoring dashboard with multi-filters. |  | III.28 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 98 | Global Admin Monitoring | Monitor Payments Global | Admin | Admin panel: Monitor all transaction logs and VietQR bank transfers. |  | III.29 | Completed | 4 | 150 | Admin payment dashboard route/JSP exists; filter and pagination regression is included in the main JUnit runner. |
| 99 | Global Admin Monitoring | Handle Refund Requests | Admin | Admin panel: Review and approve customer refund requests. |  | III.30 | Completed | 4 | 150 | Verified completed and covered by unit/regression tests. |
| 100 | Analytics & Reporting | Revenue Report Chart.js | Shop Owner / Admin | Shop/Admin: Revenue visualization charts powered by Chart.js. |  | III.31 | Completed | 5 | 180 | Chart.js dynamic dashboard showing revenue trends over date range. |
| 101 | Analytics & Reporting | Sales/Fruit Usage Report | Shop Owner / Admin | Shop/Admin: Advanced sales and fruit usage reports generation. |  | III.32 | Completed | 6 | 210 | Advanced itemized sales and fruit usage table with CSV export. |
| 102 | Analytics & Reporting | Order Statistics | Shop Owner / Admin | Shop/Admin: Order success and cancellation statistics charts. |  | III.33 | Completed | 4 | 150 | Doughnut chart of order statuses and horizontal bar chart of cancellation reasons. |
| 103 | Shop Settlement Batch Job | Shop Settlement Batch Job | System | System: Daily background job for auto-settlement of completed orders. |  | III.34 | Completed | 7 | 240 | Verified completed and covered by unit/regression tests. |
| **D** | **TOTAL ESTIMATION** |  |  | **Estimated total lines of code: 11,940 LOC (excluding CSS/JSP).** |  |  |  |  | **11,940** |  |
