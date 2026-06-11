# Task Tracking: DUONGNT

Danh sách toàn bộ các task được giao cho Duong (cập nhật theo file Project Tracking CSV mới nhất). Đã gạch bỏ các phần không liên quan và bổ sung đầy đủ các task bị thiếu.

## Phase 1: User Profile & Admin User Management
- [x] **View Profile** (Customer, Shop Owner, Delivery, Admin) - *Đã hoàn thành*
- [x] **Edit Profile** (Customer, Shop Owner, Delivery, Admin) - *Đã hoàn thành*
- [x] **Manage Addresses** (Customer) - *Đã hoàn thành (1 địa chỉ mặc định)*
- [x] **Manage Customers** (Admin) - *Đã hoàn thành*
- [x] **Manage Shop Owners** (Admin) - *Đã hoàn thành*
- [x] **Block/Unblock Accounts** (Admin) - *Đã hoàn thành*
- [x] **Approve Shop Owner Accounts** (Admin) - *Đã hoàn thành*

## Phase 2: Global Admin Monitoring
- [x] **Monitor Orders Global** (Admin) - *Đã hoàn thành (Trang Giám sát Đơn hàng)*
- [x] **Monitor Payments Global** (Admin) - *Đã hoàn thành (Trang Đối soát Thanh toán)*
- [x] **Handle Refund Requests** (Admin) - *Đã hoàn thành (Trang Quản lý Đổi trả)*
- [x] **Promotion Notifications** (Admin) - *Đã hoàn thành (Trang Gửi thông báo)*
- [x] **Admin Dashboard** (Admin) - *Đã hoàn thành (Tổng quan hệ thống)*

## Phase 3: Order & Delivery (Shop Owner / Customer)
- [x] **Estimated Delivery Time** (Delivery, Customer, Shop Owner) - *Đã hoàn thành*
- [x] **Order Delivery Confirmation** (Customer) - *Đã hoàn thành*
- [x] **Approve Order Shop Owner** (Shop Owner) - *Đã hoàn thành*
- [x] **Reject Order Shop Owner** (Shop Owner) - *Đã hoàn thành*
- [x] **Mark Order as Delivered** (Shop Owner, Delivery) - *Đã hoàn thành*

## Phase 4: Product Review & Rating System
- [x] **Write Product Review** (Customer) - *Đã hoàn thành*
- [x] **Edit / Delete Product Review** (Customer) - *Đã hoàn thành*
- [x] **Rate Products Stars** (Customer) - *Đã hoàn thành*
- [x] **Average Rating Calculation** (System) - *Đã hoàn thành*
- [x] **Approve/Reject Reviews** (Admin) - *Đã hoàn thành (workflow explicit approve/reject)*
- [x] **Promotion Management** (Shop Owner) - *Đã hoàn thành (create/update/toggle/delete voucher)*

## Phase 5: Advanced Admin & Shop Management
- [ ] **Category Management** (Admin)
- [x] **System Config (Fee, Logo)** (Admin) - *Đã hoàn thành (validate fee/logo/config update qua service)*
- [ ] **Detailed Order & Refund Status** (System/Admin)
- [ ] **Chat with Customer** (Customer, Shop Owner)
- [ ] **Refund Approval** (Admin)
- [ ] **Global Shop Management** (Admin)
- [ ] **Admin Profile** (Admin)

## Priority Next

Thứ tự ưu tiên đề xuất cho các màn còn `To Do`:

1. **Detailed Order & Refund Status** - chốt trạng thái đơn/đổi trả để không lệch nghiệp vụ.
2. **Refund Approval** - đi cùng dữ liệu đối soát, giảm rủi ro tài chính.
3. **Global Shop Management** - quản trị shop là lõi admin, ảnh hưởng nhiều màn khác.
4. **Category Management** - nền tảng cho tạo/sửa sản phẩm và điều hướng UI.
5. **Admin Profile** - tác vụ cá nhân, mức ảnh hưởng thấp hơn.
6. **Chat with Customer** - có giá trị vận hành nhưng không chặn luồng core.

## Requested Feature Audit

Audit nhanh cho nhóm chức năng bạn yêu cầu, dựa trên code hiện tại và các test đã có/đã bổ sung:

| Feature | Status | Ghi chú |
| --- | --- | --- |
| Estimated Delivery Time | Completed | `DeliveryDAO.updateEstimatedTime()` và `DeliveryService.updateEstimatedTime()` đã có validate quyền. |
| Order Delivery Confirmation | Completed | Khách xác nhận giao hàng qua `OrderService.customerConfirmDelivery()`. |
| Approve Order Shop Owner | Completed | Duyệt đơn đã được chuẩn hóa về trạng thái `CONFIRMED`. |
| Reject Order Shop Owner | Completed | `OrderService.cancelOrder()` xử lý hủy đơn, hoàn tồn kho, kiểm tra quyền. |
| Mark Order as Delivered | Completed | `DeliveryService.markAsDelivered()` đồng bộ `deliveries` và `orders`. |
| Write Product Review | Completed | `ReviewServlet` + `ReviewService.submitReview()` đã có validate và lưu review. |
| Edit / Delete Product Review | Completed | Đã nối lại UI `order-reviews.jsp` với `ReviewServlet` và `ReviewService.updateReview()/deleteReview()`. |
| Rate Products Stars | Completed | UI rating 1-5 sao đã có ở review form. |
| Average Rating Calculation | Completed | Rating sản phẩm được recalc sau create/update/delete/visibility change. |
| Approve/Reject Reviews | Completed | `AdminReviewAPI` + `review-management.jsp` dùng workflow approve/reject rõ ràng thay cho toggle ẩn/hiện. |
| Promotion Notifications | Completed | Admin broadcast thông báo qua `NotificationService.sendBroadcast()`. |
| Monitor Orders Global | Completed | `AdminOrderServlet` có bộ lọc toàn cục cho đơn hàng + payment status. |
| Monitor Payments Global | Completed | `AdminPaymentServlet` + `payment-dashboard.jsp` cung cấp dashboard riêng cho payment toàn sàn. |
| System Config (Fee, Logo) | Completed | `AdminConfigServlet` + `SystemConfigService` validate fee/logo, chuẩn hóa input, và đồng bộ hiển thị fee theo %. |
| Promotion Management | Completed | `PromotionServlet` + `promotion.jsp` hỗ trợ tạo/sửa/bật-tắt/xóa mềm voucher với validate ngày, phạm vi và quy tắc giá. |
| Handle Refund Requests | Completed | `ReturnRequestService` + `AdminRefundServlet` đã có luồng xử lý. |

## Test Coverage Added / Updated

- `test/com/fruitmkt/test/CartOrderFlowTest.java`
- Kiểm tra lại chuỗi trạng thái đơn hàng `PENDING_PAYMENT -> CONFIRMED -> DISPATCHED -> DELIVERED`
- Thêm test review lifecycle để xác minh rating trung bình cập nhật sau create/edit/delete review
- `test/com/fruitmkt/test/ReviewModerationValidationTest.java`
- `test/com/fruitmkt/test/PaymentDashboardSmokeTest.java`
- `test/com/fruitmkt/test/PromotionServletCrudToggleTest.java`
