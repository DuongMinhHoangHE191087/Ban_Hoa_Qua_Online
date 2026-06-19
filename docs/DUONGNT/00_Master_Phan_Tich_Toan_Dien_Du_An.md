# Tổng Hợp Phân Tích Toàn Diện Dự Án (Tài liệu của DUONGNT)

Tài liệu này tổng hợp toàn bộ các chức năng bạn đã thực hiện dựa theo chính xác file `ProjectTracking - 20260607.xlsx - MetaFruit.csv`. Các chức năng được chia theo 3 Iteration (dựa trên cột RDS: I, II, III) và phân tích chi tiết các file source code mà bạn đã code.

---

## Phần 1: Phân Tích Các Chức Năng Của DUONGNT (Theo 3 Iterations)

Dưới đây là danh sách **100% các task có tên "Duong" ở cột In Charge**, trích xuất trực tiếp từ file CSV của nhóm bạn.

### 🟢 ITERATION 1 (Mã RDS: I.18 đến I.21)
*Trong Iteration này, bạn đóng vai trò hoàn toàn là **Admin** để quản lý người dùng.*

1. **Manage Customers (Admin - I.18):** Xem danh sách khách hàng, phân trang và tìm kiếm.
2. **Manage Shop Owners (Admin - I.19):** Xem danh sách chủ shop và theo dõi trạng thái phê duyệt.
3. **Block/Unblock Accounts (Admin - I.20):** Khóa hoặc mở khóa tài khoản người dùng ngay lập tức.
4. **Approve Shop Owner Accounts (Admin - I.21):** Phê duyệt các tài khoản Shop Owner đang chờ duyệt để họ được hoạt động.
   - *Source Code liên quan:* `AdminUserManageServlet.java`, `AdminUserActionServlet.java`, `UserService.java`, `UserDAO.java`.

### 🟡 ITERATION 2 (Mã RDS: II.39 đến II.45)
*Trong Iteration này, cột "Main actors" trong file CSV ghi rõ bạn code cho **Shop Owner, Delivery và Customer** thuộc chức năng Tracking và Order Processing.*

1. **Estimated Delivery Time (Delivery, Customer, Shop Owner - II.39):** Hiển thị thời gian giao hàng dự kiến.
2. **Order Delivery Confirmation (Customer - II.40):** Khách hàng xác nhận đã nhận được đơn hàng để chuyển trạng thái thành DELIVERED.
3. **Approve Order Shop Owner (Shop Owner - II.43):** Shop Owner duyệt đơn hàng và bắt đầu quá trình đóng gói.
4. **Reject Order Shop Owner (Shop Owner - II.44):** Shop Owner hủy đơn hàng (bắt buộc nhập lý do) và hoàn lại tồn kho.
5. **Mark Order as Delivered (Shop Owner, Delivery - II.45):** Chủ shop hoặc Shipper đánh dấu đơn hàng đã được giao thành công.
   - *Source Code liên quan:* `ShopOrderServlet.java`, `CustomerOrderServlet.java`, `OrderService.java`, `DeliveryService.java`, `OrderDAO.java`.

### 🔴 ITERATION 3 (Mã RDS: III.15 - III.19, III.23, III.28 - III.30)
*Trong Iteration này, bạn code hệ thống Đánh giá (Review) và quay lại làm **Admin** với các tính năng Giám sát cấp cao.*

1. **Product Review & Rating System (Customer, Admin, System - III.15 đến III.19):** Khách hàng viết/sửa/xóa đánh giá và chọn số sao. Hệ thống tự động tính lại điểm trung bình. Admin có panel để phê duyệt hoặc chặn đánh giá.
2. **Promotion Notifications (Admin - III.23):** Admin gửi thông báo khuyến mãi chạy ngầm cho tất cả khách hàng.
3. **Monitor Orders Global (Admin - III.28):** Admin giám sát toàn bộ đơn hàng trên hệ thống với nhiều bộ lọc.
4. **Monitor Payments Global (Admin - III.29):** Admin giám sát mọi giao dịch thanh toán và chuyển khoản VietQR.
5. **Handle Refund Requests (Admin - III.30):** Admin xem xét và phê duyệt các yêu cầu hoàn tiền của khách hàng.
   - *Source Code liên quan:* `ReviewServlet.java`, `AdminReviewAPI.java`, `AdminRefundServlet.java`, `AdminOrderServlet.java`, `AdminPaymentServlet.java`, `ReturnRequestService.java`, `ReviewService.java`.

---

## Phần 2: Phân Tích Chi Tiết Từng File Source Code Trọng Điểm Của Bạn

Dưới đây là phân tích logic của các file code xương sống do bạn chịu trách nhiệm:

### 1. Tầng Controller (Servlet)
- **`AdminUserActionServlet.java` (Iter 1):** Xử lý lệnh của Admin. Nhận `userId` và `action` (như "block", "approve") từ giao diện. Gọi xuống `UserService.updateUserStatus()` để đổi trạng thái.
- **`ShopOrderServlet.java` (Iter 2):** Xử lý đơn hàng của Shop. Tại hàm `doPost()`, nếu `action="approve"`, gọi `OrderService.confirmOrder()`. Nếu `action="reject"`, bắt buộc lấy tham số `reason` (lý do) rồi gọi `OrderService.cancelOrder()`.
- **`AdminRefundServlet.java` (Iter 3):** Phê duyệt hoàn trả. Lấy `requestId` và `action`. Chuyển dữ liệu xuống `ReturnRequestService.processRequest()` để xử lý nghiệp vụ tài chính.
- **`AdminReviewAPI.java` (Iter 3):** Xử lý kiểm duyệt đánh giá bằng JSON/AJAX. Trả về mã status 200 OK sau khi Admin duyệt hoặc ẩn đánh giá.

### 2. Tầng Business Logic (Service) - Nơi chứa thuật toán
- **`UserService.java`:** Hàm `updateUserStatus()` kiểm tra ID hợp lệ và đảm bảo Admin không tự khóa tài khoản của chính mình trước khi gọi xuống DAO.
- **`OrderService.java`:** Hàm `cancelOrder()` kiểm tra điều kiện khắt khe: Đơn hàng đã giao (DELIVERED) thì cấm hủy. Nếu hủy hợp lệ, nó sẽ chuyển trạng thái đơn, đồng thời gọi `ProductVariantDAO.restock()` để tự động cộng lại lượng hoa quả vào kho cho Shop.
- **`ReturnRequestService.java`:** Hàm `processRequest()` xử lý luồng tiền phức tạp. Khi duyệt hoàn tiền, nó đổi trạng thái yêu cầu, tính toán trừ đi tiền đối soát của Shop trong bảng `shop_settlements` và xử lý hoàn hàng.
- **`ReviewService.java`:** Hàm `calculateAverageRating()` được kích hoạt mỗi khi có đánh giá mới hoặc đánh giá bị xóa. Nó sẽ lặp qua tất cả số sao hợp lệ của sản phẩm để tính trung bình cộng, sau đó lưu lại vào database.

### 3. Tầng Database (DAO)
- **`UserDAO.java`:** Chạy lệnh `UPDATE users SET status = ? WHERE user_id = ?` dùng PreparedStatement để chống SQL Injection.
- **`OrderDAO.java`:** Cập nhật trạng thái đơn, đồng thời kết nối với các bảng liên quan (như chi tiết đơn hàng) để Shop và Admin có thể lấy ra theo dõi.
- **`ReturnRequestDAO.java` / `ReviewDAO.java`:** Thực thi các câu lệnh `JOIN` nhiều bảng (users, orders, return_requests) để lấy đầy đủ thông tin (tên người mua, tiền, lý do) đổ ra màn hình quản lý.
