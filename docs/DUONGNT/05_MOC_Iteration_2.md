# Map of Content (MOC) - Iteration 2: Global Admin Monitoring

Tài liệu này tổng hợp phân tích luồng code chi tiết cho các chức năng thuộc Iteration 2 (Phase 2) dành cho quyền **Admin**, tập trung vào việc quản trị, giám sát và đối soát toàn hệ thống.

## 🗂️ Danh sách tính năng (Phase 2)

1. [Tổng quan Bảng điều khiển (Admin Dashboard)](feature_admin_dashboard.md)
   - *Phân tích chi tiết `AdminDashboardServlet`, `UserService`, `OrderService` và `admin-dashboard.jsp`.*
2. [Giám sát Đơn hàng Toàn cục (Monitor Orders Global)](feature_monitor_orders.md)
   - *Phân tích chi tiết `AdminOrderServlet`, `OrderService`, `OrderDAO` và `admin-orders.jsp`.*
3. [Đối soát Thanh toán (Monitor Payments Global)](feature_monitor_payments.md)
   - *Phân tích chi tiết `AdminPaymentServlet`, `PaymentService`, `PaymentDAO` và `payment-dashboard.jsp`.*
4. [Xử lý Yêu cầu Đổi trả (Handle Refund Requests)](feature_refund_requests.md)
   - *Phân tích chi tiết `AdminRefundServlet`, `ReturnRequestService` và `admin-refunds.jsp`.*
5. [Hệ thống Thông báo Khuyến mãi (Promotion Notifications)](feature_promotion_notifications.md)
   - *Phân tích chi tiết `AdminNotificationServlet`, `NotificationService` và `admin-notifications.jsp`.*

## 📐 Kiến trúc MVC chung của Iteration 2

Tất cả các chức năng trong Iteration 2 đều tuân thủ chặt chẽ mô hình **MVC + Layered Architecture**:
- **View (JSP):** Đặt tại `web/WEB-INF/jsp/admin/`. Chịu trách nhiệm hiển thị UI (dùng JSTL/EL) và lấy dữ liệu từ request attributes.
- **Controller (Servlet):** Nằm ở `src/java/servlet/admin/`. Xử lý HTTP request, kiểm tra quyền Admin qua session (hoặc dùng Filter), parse tham số lọc/phân trang, gọi Service layer và forward sang JSP.
- **Service Layer:** Nằm ở `src/java/service/`. Chứa các business logic (tính toán doanh thu tổng, xác thực logic kinh doanh trước khi đối soát hoặc duyệt hoàn trả).
- **Data Access Object (DAO):** Nằm ở `src/java/dao/`. Thực hiện thao tác CRUD trực tiếp với Database SQL Server (dùng `PreparedStatement`).
