# Feature Analysis: Monitor Orders Global

Tính năng này cung cấp cho Admin một giao diện để giám sát toàn bộ đơn hàng trên hệ thống, không phân biệt shop nào, giúp theo dõi luồng lưu chuyển hàng hóa và bắt lỗi các đơn hàng bị kẹt trạng thái.

## 1. Thành phần Code

- **Controller (Servlet):** `src/java/servlet/admin/order/AdminOrderServlet.java` (và `AdminOrderMonitorServlet.java` nếu có mở rộng).
- **Service Layer:** `service/order/OrderService.java`
- **DAO Layer:** `dao/order/OrderDAO.java`
- **View (JSP):** `web/WEB-INF/jsp/admin/admin-orders.jsp` (hoặc view hiển thị tương ứng).

## 2. Phân tích Luồng Hoạt Động (Workflow)

### GET `/admin/orders`
1. **Tiếp nhận Parameters:** Servlet nhận các tham số như `status` (để lọc đơn hàng), `page` (để phân trang) và `search` (tìm mã đơn).
2. **Xử lý Logic:**
   - Dùng `OrderService` gọi `OrderDAO.getAllOrders(status, page, keyword)` để lấy danh sách `Order` object.
   - Hàm DAO sử dụng cơ chế `OFFSET ... FETCH NEXT` của SQL Server để phân trang hiệu quả.
   - `OrderService` có thể làm giàu dữ liệu bằng cách gắn thêm thông tin Shop và Khách hàng tương ứng cho từng đơn.
3. **Hiển thị View:** List các đơn hàng được đẩy vào `request.setAttribute("orders", orderList)` và forward sang trang JSP hiển thị. Tại đây có các huy hiệu (badges) màu sắc tương ứng với trạng thái (PENDING, CONFIRMED, DISPATCHED, DELIVERED, CANCELLED).

## 3. Điểm lưu ý (Key Takeaways)

- **Read-only Focus:** Admin chỉ *giám sát* trạng thái chứ không được phép *tự ý sửa* trạng thái đơn (việc đó là của Shop Owner và Customer).
- Cần cẩn thận ở phần SQL phân trang (Pagination) kết hợp tìm kiếm, đòi hỏi phải đếm tổng số dòng (count) để vẽ giao diện phân trang chính xác.
