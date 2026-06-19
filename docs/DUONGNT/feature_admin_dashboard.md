# Feature Analysis: Admin Dashboard

Chức năng **Admin Dashboard** là trung tâm tổng quan của hệ thống, cung cấp cái nhìn toàn cảnh về hoạt động kinh doanh, số lượng người dùng, đơn hàng và các chỉ số tài chính quan trọng.

## 1. Thành phần Code

- **Controller (Servlet):** `src/java/servlet/admin/system/AdminDashboardServlet.java`
- **Service Layer:** `UserService`, `OrderService`, `ReturnRequestService`, `SettlementService`
- **DAO Layer:** Các hàm đếm/tổng hợp (e.g., `countUsers`, `countOrdersByStatus`, `calculateTotalRevenue`)
- **View (JSP):** `web/WEB-INF/jsp/admin/admin-dashboard.jsp`

## 2. Phân tích Luồng Hoạt Động (Workflow)

### GET `/admin/dashboard`
1. **Bảo mật:** `AdminDashboardServlet` kiểm tra session để đảm bảo user hiện tại là `ADMIN`.
2. **Khởi tạo Service:** Servlet khởi tạo các đối tượng Service tương ứng để gọi logic.
3. **Thu thập Dữ liệu (Data Fetching):**
   - Lấy tổng số lượng người dùng (khách hàng, chủ shop) thông qua `UserService`.
   - Lấy tổng số lượng đơn hàng và trạng thái đơn thông qua `OrderService`.
   - Tính toán tổng doanh thu hệ thống.
   - Truy xuất danh sách các yêu cầu đổi trả mới nhất cần xử lý (`ReturnRequestService`).
4. **Đẩy lên View:** Đóng gói tất cả dữ liệu thống kê vào `HttpServletRequest` attribute.
5. **Hiển thị UI:** Forward request tới file `admin-dashboard.jsp`. Trong file JSP này, JSTL và EL được dùng để render các ô "Thẻ thống kê" (Statistic Cards) và biểu đồ (nếu có).

## 3. Điểm lưu ý (Key Takeaways)

- Tốc độ tải trang Dashboard phụ thuộc nhiều vào hiệu suất của các hàm `count()` và `sum()` trong DAO. Hiện tại các hàm này trả về số liệu trực tiếp thay vì lôi toàn bộ object lên.
- Dashboard đóng vai trò như điểm điều hướng (navigation hub) cho phép Admin click trực tiếp vào các mục đang cần xử lý (ví dụ: có 5 yêu cầu đổi trả đang chờ).
