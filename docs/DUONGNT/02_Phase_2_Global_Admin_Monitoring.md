# Phase 2: Global Admin Monitoring (ITERATION 2)

> [!IMPORTANT] GHI CHÚ RIÊNG CHO ITERATION 2
> Đây là Iteration cốt lõi dành cho Admin để kiểm soát rủi ro toàn sàn, bao gồm dòng tiền, đơn hàng ảo và các khiếu nại đổi trả.

## Phân tích Chi Tiết Các Chức Năng (Iter 2)

### 1. Admin Dashboard (Tổng quan hệ thống)
- **Servlet:** src/java/servlet/admin/system/AdminDashboardServlet.java
- **DAO:** Các hàm thống kê (countUsers, countOrdersByStatus, calculateTotalRevenue)
- **JSP:** web/WEB-INF/jsp/admin/admin-dashboard.jsp
- **Luồng:** Gom nhóm dữ liệu từ nhiều DAO -> Push vào equest -> Render biểu đồ & thẻ trạng thái trên dmin-dashboard.jsp.

### 2. Monitor Orders Global (Giám sát đơn hàng)
- **Servlet:** src/java/servlet/admin/order/AdminOrderServlet.java
- **Service:** src/java/service/order/OrderService.java
- **DAO:** src/java/dao/order/OrderDAO.java
- **JSP:** web/WEB-INF/jsp/admin/admin-orders.jsp
- **Logic:** Lọc đơn hàng theo nhiều tiêu chí (Shop, Trạng thái, Khoảng ngày). Quản trị viên chỉ có quyền *Read-Only* để đảm bảo tính khách quan.

### 3. Monitor Payments Global (Đối soát thanh toán)
- **Servlet:** src/java/servlet/admin/settlement/AdminPaymentServlet.java
- **Service:** src/java/service/shop/PaymentService.java, src/java/service/shop/SettlementService.java
- **DAO:** src/java/dao/shop/PaymentTransactionDAO.java
- **JSP:** web/WEB-INF/jsp/admin/payment-dashboard.jsp
- **Logic:** Tính toán Hold Balance (tiền đang giữ), Settled (đã chuyển) và tính Phí Nền Tảng (Platform Fee) cho mỗi đơn hàng thành công. Khi Approve lệnh rút tiền, hệ thống trừ tiền trong Virtual Balance của Shop.

### 4. Handle Refund Requests (Quản lý Đổi/Trả)
- **Servlet:** src/java/servlet/admin/order/AdminRefundServlet.java
- **Service:** src/java/service/order/ReturnRequestService.java
- **JSP:** web/WEB-INF/jsp/admin/admin-refunds.jsp
- **Luồng:** Admin xem xét Proof (hình ảnh/video). Nếu duyệt (Approve) -> Trạng thái đơn chuyển sang REFUNDED -> Kích hoạt logic hoàn tiền cho người mua và thu hồi tiền của Shop qua PaymentService. DAO sử dụng SQL Transaction chặt chẽ.

### 5. Promotion Notifications (Thông báo)
- **Servlet:** src/java/servlet/admin/system/AdminNotificationServlet.java
- **Service:** src/java/service/notification/NotificationService.java
- **JSP:** web/WEB-INF/jsp/admin/admin-notifications.jsp
- **Logic:** Hệ thống Broadcast. Khi có khuyến mãi, Admin gửi thông báo. Insert 1 dòng Global Notification thay vì Insert N dòng cho N user để tối ưu hiệu suất Database.
