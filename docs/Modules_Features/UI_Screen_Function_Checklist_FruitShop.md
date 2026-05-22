# UI Screen and Functional Checklist
## Online Fruit Shop Marketplace
### Version 0.1

## 1. Document Purpose
Tài liệu này mô tả toàn bộ màn hình UI, checklist chức năng và các xử lý chuẩn cho hệ thống bán hoa quả online theo mô hình marketplace. Đây là tài liệu hỗ trợ đặc tả giao diện, phân rã chức năng theo màn hình, và làm đầu vào cho SRS, mockup, test case, và phát triển hệ thống.

## 2. Scope
Tài liệu bao phủ các vai trò: Guest, Customer, Shop Owner, Delivery Staff, Admin và các chức năng hệ thống nền như notification, payment webhook, inventory reservation, recommendation.

## 3. Screen Design Principles
- Mỗi màn hình phải có mục tiêu rõ ràng và chỉ phục vụ một nhóm nhiệm vụ chính.
- Mỗi màn hình phải có trạng thái chuẩn: loading, empty, success, error, no permission.
- Các dữ liệu quan trọng như giá, tồn kho, trạng thái đơn, trạng thái thanh toán phải hiển thị nhất quán.
- Các thao tác nhạy cảm như thanh toán, hủy đơn, duyệt shop, chốt settlement phải có xác nhận.
- UI mobile-first nhưng vẫn tối ưu cho desktop.

## 4. Screen Inventory by Role

### 4.1 Guest Screens
- Home Page
- Product Listing Page
- Product Detail Page
- Search Results Page
- Authentication Entry Page
- Register Page
- Login Page
- Forgot Password Page
- Guest Cart Page

### 4.2 Customer Screens
- Customer Dashboard
- Profile Page
- Customer Cart Page
- Checkout Page
- Order Confirmation Page
- Order History Page
- Order Detail / Tracking Page
- Chat Page
- Review Submission Page
- Return / Exchange Request Page
- Notification Center
- Shop Registration Page

### 4.3 Shop Owner Screens
- Shop Dashboard
- Shop Profile Page
- Product List Page
- Product Create / Edit Page
- Product Variant Management Page
- Product Image Management Page
- Inventory Management Page
- Order Management Page
- Order Detail Page
- Chat Inbox Page
- Promotion Management Page
- Settlement Summary Page
- Shop Report Page

### 4.4 Delivery Staff Screens
- Delivery Dashboard
- Assigned Delivery List Page
- Delivery Detail Page
- Delivery Status Update Page
- Delivery Failure Report Page

### 4.5 Admin Screens
- Admin Dashboard
- User Management Page
- User Detail Page
- Shop Approval Queue Page
- Shop Approval Detail Page
- Category Management Page
- Product Moderation Page
- Order Monitoring Page
- Payment Monitoring Page
- Settlement Management Page
- Report Dashboard Page
- System Notification Page
- Audit Log Page

### 4.6 System / Non-UI Functions
- Cart Sync Service
- Payment Webhook Service
- Inventory Reservation Service
- Notification Service
- Recommendation Service
- Settlement Batch Job

---

## 5. Screen Specifications

## 5.1 Guest Screens

### 5.1.1 Home Page
Purpose: Hiển thị sản phẩm nổi bật, danh mục chính, shop nổi bật, khuyến mãi và gợi ý theo mùa.

Related Use Cases: UC-03, UC-04, UC-20

Primary Actions:
- Xem danh mục
- Tìm kiếm nhanh
- Đi tới chi tiết sản phẩm
- Xem khuyến mãi nổi bật

UI Components:
- Header navigation
- Search bar
- Category carousel
- Featured product grid
- Promotion banner
- Seasonal recommendation block
- Footer links

Checklist:
- Hiển thị đúng sản phẩm active
- Không hiển thị hàng ngưng bán
- Có trạng thái loading và empty
- Search bar luôn truy cập được
- Danh mục click được

Validation:
- Keyword search không rỗng khi submit
- Link sản phẩm phải trỏ đúng product detail

### 5.1.2 Product Listing Page
Purpose: Liệt kê sản phẩm theo danh mục, shop, giá, rating, khu vực.

Related Use Cases: UC-03, UC-04

Primary Actions:
- Lọc sản phẩm
- Sắp xếp kết quả
- Chuyển trang
- Mở chi tiết sản phẩm

UI Components:
- Filter sidebar
- Search input
- Sorting dropdown
- Product cards
- Pagination
- Count summary

Fields:
| Field | Description |
| --- | --- |
| Keyword | Từ khóa tìm kiếm |
| Category | Danh mục trái cây |
| Min Price | Giá nhỏ nhất |
| Max Price | Giá lớn nhất |
| Rating | Số sao tối thiểu |
| Shop Region | Khu vực shop |
| Sort By | Mới nhất, bán chạy, giá tăng, giá giảm |

Checklist:
- Kết quả lọc đúng tổ hợp điều kiện
- Không bị mất filter khi chuyển trang nếu có thiết kế lưu trạng thái
- Card sản phẩm phải có ảnh, tên, giá, rating, shop

### 5.1.3 Product Detail Page
Purpose: Cung cấp thông tin đầy đủ của sản phẩm trước khi mua.

Related Use Cases: UC-03

Primary Actions:
- Chọn variant
- Thêm vào giỏ hàng
- Mua ngay
- Xem shop
- Xem review

UI Components:
- Image gallery
- Product summary
- Variant selector
- Price panel
- Shop card
- Description tab
- Review tab
- Related products block

Fields:
| Field | Description |
| --- | --- |
| Product Name | Tên sản phẩm |
| Origin Country | Nguồn gốc quốc gia |
| Origin Region | Vùng sản xuất |
| Harvest Date | Ngày thu hoạch |
| Shelf Life Days | Số ngày bảo quản |
| Storage Instruction | Hướng dẫn bảo quản |
| Variant Label | Phân loại hàng |
| Price | Giá theo variant |
| Stock Quantity | Tồn kho còn lại |
| Shop Name | Tên shop |
| Shop Rating | Đánh giá shop |

Checklist:
- Variant phải chọn trước khi thêm giỏ nếu sản phẩm yêu cầu variant
- Giá cập nhật theo variant
- Stock hiển thị đúng
- Không cho chọn variant hết hàng

Validation:
- Quantity >= 1
- Cannot add if stock unavailable

### 5.1.4 Authentication Entry Page
Purpose: Điều hướng giữa đăng nhập, đăng ký, quên mật khẩu.

Related Use Cases: UC-01, UC-02

UI Components:
- Login button
- Register button
- Forgot password link
- Social login buttons if enabled

Checklist:
- Dễ truy cập từ header/footer
- Không lộ màn hình riêng tư nếu chưa đăng nhập

### 5.1.5 Register Page
Purpose: Tạo tài khoản Customer hoặc Shop Owner.

Related Use Cases: UC-01

UI Components:
- Full name
- Email
- Phone
- Password
- Confirm password
- Account type selector
- Verification area

Checklist:
- Email unique
- Phone unique if required
- Password policy enforced
- Account type selected

### 5.1.6 Login Page
Purpose: Xác thực người dùng.

Related Use Cases: UC-02

UI Components:
- Email or phone
- Password
- Remember me
- Login button
- Error message area

Checklist:
- Sai mật khẩu báo lỗi rõ ràng
- Tài khoản bị khóa phải hiển thị thông báo
- Có liên kết quên mật khẩu

### 5.1.7 Forgot Password Page
Purpose: Tạo yêu cầu đặt lại mật khẩu.

Related Use Cases: UC-02

UI Components:
- Email or phone
- Reset code
- New password
- Confirm password
- Submit button

Checklist:
- Mã xác thực hết hạn phải báo lỗi
- Password mới phải đạt policy

### 5.1.8 Guest Cart Page
Purpose: Lưu tạm và cho phép xem giỏ hàng chưa đăng nhập.

Related Use Cases: UC-05

UI Components:
- Cart item list
- Quantity controls
- Remove button
- Login prompt
- Checkout prompt

Checklist:
- Dữ liệu lưu ở localStorage/sessionStorage
- Refresh trang không mất cart theo thiết kế
- Khi đăng nhập thì đồng bộ cart lên server

---

## 5.2 Customer Screens

### 5.2.1 Customer Dashboard
Purpose: Tổng quan nhanh các đơn gần đây, ưu đãi, gợi ý và thông báo.

Related Use Cases: UC-09, UC-20

UI Components:
- Order summary cards
- Recent orders
- Recommendation block
- Notification shortcut

Checklist:
- Hiển thị đúng đơn thuộc tài khoản hiện tại
- Có shortcut tới tracking và chat

### 5.2.2 Profile Page
Purpose: Chỉnh sửa thông tin cá nhân và địa chỉ mặc định.

Related Use Cases: UC-09

Fields:
| Field | Description |
| --- | --- |
| Full Name | Họ tên |
| Email | Email đăng nhập |
| Phone | Số điện thoại |
| Address | Địa chỉ mặc định |
| Password Change | Đổi mật khẩu |

Checklist:
- Không cho sửa email nếu chính sách khóa
- Kiểm tra format phone và address

### 5.2.3 Customer Cart Page
Purpose: Quản lý giỏ hàng chính thức của customer.

Related Use Cases: UC-06, UC-07

UI Components:
- Item list
- Quantity editor
- Voucher input
- Order summary
- Checkout button

Checklist:
- Số lượng không vượt tồn
- Không trộn sản phẩm nhiều shop khi checkout
- Hiển thị tổng tiền minh bạch

### 5.2.4 Checkout Page
Purpose: Tạo đơn hàng thuộc đúng một shop.

Related Use Cases: UC-07, UC-08

UI Components:
- Address form
- Delivery time slot selector
- Notes field
- Payment method selector
- Voucher area
- Final summary
- Confirm order button

Fields:
| Field | Description |
| --- | --- |
| Delivery Address | Địa chỉ nhận hàng |
| Time Slot | Khung giờ giao |
| Notes | Ghi chú giao hàng |
| Payment Method | CK hoặc COD |
| Voucher Code | Mã giảm giá |

Checklist:
- Chỉ 1 shop/đơn
- Không đủ tồn thì chặn
- Total = subtotal - discount + delivery fee + platform fee
- Confirm phải có modal xác nhận nếu cần

### 5.2.5 Order Confirmation Page
Purpose: Hiển thị mã đơn, trạng thái thanh toán, bước tiếp theo.

Related Use Cases: UC-07, UC-08

UI Components:
- Success banner
- Order code
- Payment status
- CTA to track order

Checklist:
- Mã đơn hiển thị rõ
- Nếu thanh toán pending phải có hướng dẫn tiếp theo

### 5.2.6 Order History Page
Purpose: Xem danh sách đơn cũ và lọc theo trạng thái.

Related Use Cases: UC-09

UI Components:
- Status filter
- Date filter
- Order cards
- Search by order code

Checklist:
- Đơn hiển thị đúng user
- Click vào card mở detail

### 5.2.7 Order Detail / Tracking Page
Purpose: Theo dõi trạng thái đơn và các sự kiện liên quan.

Related Use Cases: UC-09, UC-14

UI Components:
- Order header
- Timeline status
- Delivery info
- Payment info
- Action buttons: chat, review, return request

Checklist:
- Timeline theo đúng trạng thái nghiệp vụ
- Hiển thị cancelled / failed / expired rõ ràng
- Nếu đơn hoàn thành thì cho review

### 5.2.8 Chat Page
Purpose: Nhắn tin với shop về sản phẩm hoặc đơn hàng.

Related Use Cases: UC-15

UI Components:
- Conversation list
- Chat panel
- Message input
- Attachment button if enabled

Checklist:
- Chỉ chat với shop của đơn hoặc shop đã liên hệ
- Tin nhắn mới phải cập nhật real time hoặc near real time

### 5.2.9 Review Submission Page
Purpose: Gửi đánh giá sản phẩm sau khi đơn hoàn thành.

Related Use Cases: UC-16

UI Components:
- Rating stars
- Comment box
- Submit button

Checklist:
- Chỉ mở khi order completed
- Mỗi order item chỉ review một lần

### 5.2.10 Return / Exchange Request Page
Purpose: Tạo yêu cầu hủy, hoàn, đổi trả.

Related Use Cases: UC-17

UI Components:
- Order item selector
- Request type selector
- Reason code selector
- Evidence upload
- Description field
- Submit button

Checklist:
- Lý do phải hợp lệ
- Evidence nên có nếu yêu cầu bồi hoàn
- Không cho gửi nếu ngoài thời gian cho phép

### 5.2.11 Notification Center
Purpose: Hiển thị thông báo đơn hàng, thanh toán, khuyến mãi và hệ thống.

Related Use Cases: UC-09, UC-18

UI Components:
- Notification list
- Read/unread toggle
- Filter by type

Checklist:
- Chưa đọc phải nổi bật
- Click notification mở đúng action_url

### 5.2.12 Shop Registration Page
Purpose: Đăng ký mở shop.

Related Use Cases: UC-10

UI Components:
- Shop info form
- Document upload
- Submit button
- Status panel

Checklist:
- Hiển thị trạng thái pending/approved/rejected
- Nếu rejected thì có lý do

---

## 5.3 Shop Owner Screens

### 5.3.1 Shop Dashboard
Purpose: Tổng quan vận hành shop.

Related Use Cases: UC-19

UI Components:
- Revenue cards
- Order count
- Best sellers
- Low stock alerts
- Settlement snapshot

Checklist:
- Số liệu lọc theo thời gian
- Chỉ hiển thị dữ liệu shop hiện tại

### 5.3.2 Shop Profile Page
Purpose: Cập nhật hồ sơ shop.

Related Use Cases: UC-10

Fields:
| Field | Description |
| --- | --- |
| Shop Name | Tên shop |
| Shop Description | Mô tả shop |
| Logo | Ảnh logo |
| Banner | Ảnh banner |
| Delivery Address | Địa chỉ shop |
| Contact Info | Thông tin liên hệ |

Checklist:
- Ảnh phải đúng định dạng
- Lưu thay đổi thành công phải có phản hồi

### 5.3.3 Product List Page
Purpose: Quản lý danh sách sản phẩm của shop.

Related Use Cases: UC-11

UI Components:
- Search
- Filter by status/category
- Add product button
- Table/list of products

Checklist:
- CRUD đầy đủ
- Active/inactive rõ ràng
- Không cho xóa nếu ảnh hưởng dữ liệu giao dịch nếu chính sách không cho

### 5.3.4 Product Create / Edit Page
Purpose: Tạo mới hoặc sửa sản phẩm.

Related Use Cases: UC-11

Fields:
| Field | Description |
| --- | --- |
| Product Name | Tên sản phẩm |
| Description | Mô tả |
| Category | Danh mục |
| Origin Country | Nguồn gốc quốc gia |
| Origin Region | Vùng trồng |
| Harvest Date | Ngày thu hoạch |
| Shelf Life Days | Số ngày bảo quản |
| Storage Instruction | Hướng dẫn bảo quản |
| Status | Active/Inactive |

Checklist:
- Bắt buộc các trường chính
- Không cho lưu nếu thiếu tên hoặc category
- Có preview dữ liệu trước khi lưu

### 5.3.5 Product Variant Management Page
Purpose: Quản lý biến thể, giá và tồn kho.

Related Use Cases: UC-11, UC-12

Fields:
| Field | Description |
| --- | --- |
| SKU | Mã biến thể |
| Variant Label | Khối lượng, loại, độ chín |
| Price | Giá bán |
| Stock Quantity | Tồn kho |
| Is Active | Trạng thái hoạt động |

Checklist:
- SKU unique
- Giá > 0
- Stock không âm

### 5.3.6 Product Image Management Page
Purpose: Quản lý hình ảnh sản phẩm.

Related Use Cases: UC-11

UI Components:
- Upload area
- Image list
- Primary image selector
- Reorder control

Checklist:
- Có ảnh chính
- File format hợp lệ
- Thứ tự hiển thị đúng

### 5.3.7 Inventory Management Page
Purpose: Cập nhật và theo dõi tồn kho.

Related Use Cases: UC-12

UI Components:
- Inventory table
- Adjustment form
- Inventory log history

Checklist:
- Mỗi điều chỉnh phải có note nếu cần
- Stock không âm
- Log phải ghi ai thay đổi và khi nào

### 5.3.8 Order Management Page
Purpose: Xử lý đơn mới của shop.

Related Use Cases: UC-13

UI Components:
- Order queue
- Status filter
- Action buttons: confirm, prepare, hand over, cancel

Checklist:
- Chỉ xử lý đơn thuộc shop
- Không được nhảy trạng thái
- Hủy phải có lý do

### 5.3.9 Order Detail Page
Purpose: Xem chi tiết từng đơn.

Related Use Cases: UC-13, UC-17

UI Components:
- Order summary
- Customer info
- Item list
- Delivery info
- Payment info
- After-sales history

Checklist:
- Snapshot đơn không bị thay đổi theo sản phẩm gốc
- Thấy được payment và delivery states

### 5.3.10 Chat Inbox Page
Purpose: Trả lời khách hàng.

Related Use Cases: UC-15

Checklist:
- Chỉ chat của shop mình
- Có unread indicator
- Có tìm kiếm hội thoại nếu cần

### 5.3.11 Promotion Management Page
Purpose: Tạo và quản lý voucher của shop.

Related Use Cases: UC-18

Fields:
| Field | Description |
| --- | --- |
| Code | Mã giảm giá |
| Discount Type | PERCENT/FIXED |
| Discount Value | Giá trị giảm |
| Max Discount | Mức giảm tối đa |
| Min Order Value | Đơn tối thiểu |
| Valid From/Until | Thời gian hiệu lực |
| Usage Limit | Số lượt dùng tối đa |
| Stackable | Có cho phép stack không |

Checklist:
- Mã unique
- Hết hiệu lực thì không áp dụng
- Quy tắc scope rõ ràng

### 5.3.12 Settlement Summary Page
Purpose: Xem doanh thu và đối soát cho shop.

Related Use Cases: UC-19

UI Components:
- Settlement cards
- Period filter
- Order drill-down
- Payment status summary

Checklist:
- Gross, fee, refund, net hiển thị rõ
- Mỗi order có trace trong settlement

### 5.3.13 Shop Report Page
Purpose: Báo cáo doanh thu và vận hành.

Related Use Cases: UC-19

UI Components:
- Sales chart
- Best seller chart
- Inventory chart
- Export button

Checklist:
- Có filter theo ngày/tháng
- Có thể export nếu yêu cầu

---

## 5.4 Delivery Staff Screens

### 5.4.1 Delivery Dashboard
Purpose: Tổng quan các đơn giao được phân công.

Related Use Cases: UC-14

UI Components:
- Assigned orders summary
- Status cards
- Task queue

Checklist:
- Chỉ dữ liệu được phân công
- Có trạng thái từng đơn

### 5.4.2 Assigned Delivery List Page
Purpose: Xem danh sách đơn giao.

Related Use Cases: UC-14

Checklist:
- Filter theo trạng thái
- Mở được chi tiết từng đơn

### 5.4.3 Delivery Detail Page
Purpose: Xem chi tiết giao hàng.

Related Use Cases: UC-14

Fields:
| Field | Description |
| --- | --- |
| Order Code | Mã đơn |
| Customer Address | Địa chỉ giao |
| Pickup Time | Thời gian lấy hàng |
| Delivery Time | Thời gian giao |
| Failure Reason | Lý do thất bại nếu có |

Checklist:
- Hiển thị rõ tuyến giao
- Không cho sửa ngoài phạm vi

### 5.4.4 Delivery Status Update Page
Purpose: Cập nhật trạng thái giao.

Related Use Cases: UC-14

Checklist:
- Trạng thái theo workflow
- Có thời gian cập nhật
- Không cho chuyển trạng thái không hợp lệ

---

## 5.5 Admin Screens

### 5.5.1 Admin Dashboard
Purpose: Tổng quan toàn hệ thống.

Related Use Cases: UC-19

UI Components:
- KPI cards
- Order overview
- Revenue overview
- Shop approval count
- Delivery metrics

Checklist:
- Số liệu toàn sàn
- Có bộ lọc thời gian

### 5.5.2 User Management Page
Purpose: Quản lý người dùng và vai trò.

Related Use Cases: UC-02, UC-19

Fields:
| Field | Description |
| --- | --- |
| Full Name | Tên người dùng |
| Email | Email |
| Role | Vai trò |
| Status | Trạng thái |
| Locked Until | Thời gian khóa |

Checklist:
- Admin đổi role được
- Khóa/mở khóa có log

### 5.5.3 Shop Approval Queue Page
Purpose: Duyệt yêu cầu mở shop.

Related Use Cases: UC-10

Checklist:
- Có trạng thái pending
- Có tài liệu đính kèm
- Có nút approve/reject/suspend

### 5.5.4 Shop Approval Detail Page
Purpose: Xem chi tiết hồ sơ shop.

Related Use Cases: UC-10

Checklist:
- Thấy đủ giấy tờ và lý do
- Reject phải có reason

### 5.5.5 Category Management Page
Purpose: Quản lý danh mục trái cây.

Related Use Cases: UC-03, UC-04

Fields:
| Field | Description |
| --- | --- |
| Name | Tên danh mục |
| Slug | Định danh URL |
| Display Order | Thứ tự hiển thị |
| Is Active | Hoạt động hay không |

Checklist:
- Slug unique
- Category inactive không hiển thị ngoài public

### 5.5.6 Product Moderation Page
Purpose: Kiểm duyệt sản phẩm.

Related Use Cases: UC-11

Checklist:
- Có danh sách chờ duyệt nếu áp dụng
- Có lý do từ chối
- Có audit trail

### 5.5.7 Order Monitoring Page
Purpose: Theo dõi đơn toàn hệ thống.

Related Use Cases: UC-19

Checklist:
- Xem được trạng thái mọi đơn
- Có filter theo shop, customer, trạng thái

### 5.5.8 Payment Monitoring Page
Purpose: Theo dõi giao dịch thanh toán.

Related Use Cases: UC-08, UC-19

Checklist:
- Pending/processing/completed/failed rõ ràng
- Có QR, reference, provider response nếu cần

### 5.5.9 Settlement Management Page
Purpose: Quản lý settlement cho shop.

Related Use Cases: UC-19

Checklist:
- Period lọc được
- Có confirm payout
- Không chốt trước thời gian khiếu nại

### 5.5.10 Report Dashboard Page
Purpose: Báo cáo toàn sàn.

Related Use Cases: UC-19

Checklist:
- Doanh thu
- User growth
- Delivery performance
- Return rate

### 5.5.11 System Notification Page
Purpose: Quản lý thông báo hệ thống.

Related Use Cases: UC-09, UC-18

Checklist:
- Có loại thông báo
- Có trạng thái đọc/chưa đọc

### 5.5.12 Audit Log Page
Purpose: Kiểm tra lịch sử thao tác.

Related Use Cases: UC-19

Checklist:
- Ghi ai, làm gì, lúc nào, trước/sau ra sao
- Dùng cho truy vết sự cố

---

## 6. Functional Checklist Summary

### 6.1 Core Checklist by Process
- Guest can browse products, search, filter, and store cart locally.
- Customer can register, login, manage cart, checkout, pay, track order, chat, review, and create after-sales requests.
- Shop Owner can register shop, manage shop profile, products, variants, inventory, orders, chat, promotions, and settlement.
- Delivery Staff can view assigned orders and update delivery progress.
- Admin can manage users, shops, categories, products, payments, settlement, reports, and audit logs.

### 6.2 Critical Business Rules Checklist
- One order belongs to one shop only.
- Each product belongs to one shop owner only.
- Guest cart uses localStorage/sessionStorage.
- Order snapshot must be immutable after checkout.
- Payment webhook must not be processed twice.
- After-sales request must follow policy and time window.
- Settlement must only run after complaint window expires.

### 6.3 UI State Checklist
- Loading state
- Empty state
- Success state
- Error state
- No permission state
- Not found state
- Disabled / unavailable action state

### 6.4 Validation Checklist
- Required fields are enforced
- Unique fields are checked
- Numeric ranges are validated
- Status transitions are controlled
- Permission checks exist for every restricted action

## 7. Notes for SRS Mapping
Tài liệu này có thể dùng để map trực tiếp sang:
- Section 1.4 System Functionalities
- Section 2 Use Case Specifications
- Section 3 Functional Requirements
- Test cases và acceptance tests
- Mockup/UI wireframe
