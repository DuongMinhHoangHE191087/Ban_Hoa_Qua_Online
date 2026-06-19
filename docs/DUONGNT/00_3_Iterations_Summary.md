# Tổng hợp luồng chức năng theo 3 Iterations

Tài liệu này gom nhóm toàn bộ phân tích chức năng từ 5 Phase thành **3 Iterations** đúng theo file Project Tracking. Mỗi Iteration đều liệt kê chi tiết luồng nghiệp vụ và đường dẫn source code tương ứng để bạn dễ dàng trace code.

---

## 🟢 ITERATION 1: User Profile & Admin User Management
*(Tương đương Phase 1)*
Tập trung vào quản lý người dùng, tài khoản và hồ sơ cá nhân.

### 1. View & Edit Profile (Tất cả Role)
- **Nghiệp vụ:** Xem và cập nhật thông tin cá nhân (tên, số điện thoại, avatar). Riêng Admin bị ẩn đi lịch sử đơn hàng và sổ địa chỉ.
- **Source Code:**
  - **Servlet:** `src/java/servlet/base/UserProfileServlet.java`, `src/java/servlet/base/UpdateProfileServlet.java`
  - **Service:** `src/java/service/user/UserService.java`
  - **DAO:** `src/java/dao/auth/UserDAO.java`
  - **JSP:** `web/WEB-INF/jsp/common/profile.jsp`

### 2. Manage Customers & Shop Owners (Admin)
- **Nghiệp vụ:** Admin xem danh sách khách hàng và chủ shop, phân trang, lọc theo role. Khả năng xem chi tiết thông tin (Read-only view).
- **Source Code:**
  - **Servlet:** `src/java/servlet/admin/AdminUserManageServlet.java`, `src/java/servlet/admin/AdminUserViewServlet.java`
  - **DAO:** `src/java/dao/auth/UserDAO.java`
  - **JSP:** `web/WEB-INF/jsp/admin/user-list.jsp`, `web/WEB-INF/jsp/admin/user-view.jsp`

### 3. Block/Unblock & Approve Accounts (Admin)
- **Nghiệp vụ:** Khóa/Mở khóa tài khoản ngay lập tức. Duyệt tài khoản Shop Owner đang ở trạng thái pending sang active.
- **Source Code:**
  - **Servlet:** `src/java/servlet/admin/AdminUserActionServlet.java` (xử lý logic toggle block/unblock, approve)
  - **Service:** `src/java/service/user/UserService.java` (validate rule)
  - **DAO:** `src/java/dao/auth/UserDAO.java` (gọi hàm update status)

---

## 🟡 ITERATION 2: Order & Delivery (Shop Owner / Customer)
*(Tương đương Phase 3)*
Đây là luồng cốt lõi vận hành đơn hàng, từ lúc shop duyệt đơn cho đến khi shipper giao hàng và khách xác nhận.

### 1. Approve & Reject Order (Shop Owner)
- **Nghiệp vụ:** Shop Owner tiếp nhận đơn con (`CHILD` order). Có thể duyệt (chuyển sang `CONFIRMED`) để tiến hành đóng gói, hoặc từ chối (bắt buộc nhập lý do hủy) và hệ thống sẽ tự động hoàn lại tồn kho.
- **Source Code:**
  - **Servlet:** `src/java/servlet/shop/ShopOrderServlet.java`
  - **Service:** `src/java/service/order/OrderService.java` (Hàm `approveOrder`, `cancelOrder`)
  - **DAO:** `src/java/dao/order/OrderDAO.java` (Hàm `updateStatus`, `cancel`), `src/java/dao/catalog/ProductVariantDAO.java` (hoàn stock)
  - **JSP:** `web/WEB-INF/jsp/shop/orders.jsp`, `web/WEB-INF/jsp/shop/order-detail.jsp`

### 2. Estimated Delivery Time & Mark as Delivered (Shop Owner / Delivery)
- **Nghiệp vụ:** Shop/Shipper cập nhật thời gian giao hàng dự kiến (Estimated Time). Khi giao xong thì chuyển trạng thái đơn hàng sang `DELIVERED` và cập nhật thông tin chuyến giao.
- **Source Code:**
  - **Servlet:** `src/java/servlet/delivery/DeliveryDashboardServlet.java`, `src/java/servlet/delivery/DeliveryDetailServlet.java`
  - **Service:** `src/java/service/delivery/DeliveryService.java` (Hàm `updateEstimatedTime`, `markAsDelivered`)
  - **DAO:** `src/java/dao/order/DeliveryDAO.java` (Cập nhật bảng `deliveries`), `src/java/dao/order/OrderDAO.java`
  - **JSP:** `web/WEB-INF/jsp/delivery/dashboard.jsp`, `web/WEB-INF/jsp/delivery/detail.jsp`

### 3. Order Delivery Confirmation (Customer)
- **Nghiệp vụ:** Khách hàng bấm xác nhận "Đã nhận được hàng". Hành động này khóa luồng đơn hàng, chuyển trạng thái hoàn tất để ghi nhận doanh thu (hoặc mở điều kiện cho phép review).
- **Source Code:**
  - **Servlet:** `src/java/servlet/customer/CustomerOrderServlet.java` (Action `confirmDelivery`)
  - **Service:** `src/java/service/order/OrderService.java` (Hàm `customerConfirmDelivery`)
  - **JSP:** `web/WEB-INF/jsp/customer/orders.jsp`, `web/WEB-INF/jsp/customer/order-detail.jsp`

---

## 🔴 ITERATION 3: Global Monitoring, Reviews & Refunds
*(Tương đương Phase 2, Phase 4, Phase 5 gom lại)*
Đây là phần mở rộng quyền lực cho Admin và sự tương tác nâng cao của người dùng (Đánh giá, Khiếu nại).

### 1. Product Review & Rating System (Customer & Admin)
- **Nghiệp vụ:** Khách hàng viết đánh giá (1-5 sao, đính kèm ảnh). Khách có thể Edit/Delete review của mình. Hệ thống tự tính lại điểm trung bình của sản phẩm. Admin có quyền duyệt (Approve) hoặc ẩn (Reject) các đánh giá rác.
- **Source Code:**
  - **Servlet:** `src/java/servlet/customer/ReviewServlet.java`, `src/java/servlet/admin/AdminReviewAPI.java`
  - **Service:** `src/java/service/catalog/ReviewService.java` (Hàm tính trung bình rating, validate)
  - **DAO:** `src/java/dao/catalog/ReviewDAO.java`, `src/java/dao/catalog/ProductDAO.java` (cập nhật rating)
  - **JSP:** `web/WEB-INF/jsp/customer/order-reviews.jsp`, `web/WEB-INF/jsp/admin/review-management.jsp`

### 2. Monitor Orders & Payments Global (Admin)
- **Nghiệp vụ:** Admin giám sát toàn bộ đơn hàng trên sàn (Parent/Child orders). Cung cấp bộ lọc phức tạp. Theo dõi dashboard thanh toán, đối soát biến động số dư qua các cổng thanh toán.
- **Source Code:**
  - **Servlet:** `src/java/servlet/admin/AdminOrderServlet.java`, `src/java/servlet/admin/AdminPaymentServlet.java`
  - **DAO:** `src/java/dao/order/OrderDAO.java` (Hàm search global)
  - **JSP:** `web/WEB-INF/jsp/admin/admin-orders.jsp`, `web/WEB-INF/jsp/admin/payment-dashboard.jsp`

### 3. Handle Refund Requests (Admin)
- **Nghiệp vụ:** Quản lý và phê duyệt các yêu cầu trả hàng/hoàn tiền. Khi Admin duyệt, hệ thống tự động tính toán hoàn tiền cho khách và trừ đi phần đối soát của Shop.
- **Source Code:**
  - **Servlet:** `src/java/servlet/admin/AdminRefundServlet.java`
  - **Service:** `src/java/service/order/ReturnRequestService.java` (Hàm `approveReturnRequest`)
  - **DAO:** `src/java/dao/order/ReturnRequestDAO.java`, `src/java/dao/order/OrderDAO.java`
  - **JSP:** `web/WEB-INF/jsp/admin/admin-refunds.jsp`, `web/WEB-INF/jsp/admin/return-requests.jsp`

### 4. Promotion Notifications (Admin)
- **Nghiệp vụ:** Gửi thông báo hệ thống hoặc khuyến mãi đồng loạt cho toàn bộ người dùng (Broadcast).
- **Source Code:**
  - **Servlet:** `src/java/servlet/admin/AdminNotificationServlet.java`
  - **Service:** `src/java/service/notification/NotificationService.java` (Hàm `sendBroadcast`)
  - **DAO:** `src/java/dao/notification/NotificationDAO.java`
  - **JSP:** `web/WEB-INF/jsp/admin/notifications.jsp`
