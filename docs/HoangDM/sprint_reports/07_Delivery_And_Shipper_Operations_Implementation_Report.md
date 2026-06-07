# Báo Cáo Triển Khai Hệ Thống Giao Hàng (Delivery Flow) - MetaFruit

Báo cáo chi tiết phát triển luồng giao vận hoàn chỉnh, sửa lỗi SQL/NPE và nâng cấp thiết kế UI Sky Blue cao cấp.

---

## 1. Tổng Quan Mã Nguồn Đã Tạo/Sửa Đổi

- **Số lượng thay đổi**: 12 files (7 Java classes & 5 JSP pages)
- **Chức năng hoàn thành**: 9 chức năng thuộc nghiệp vụ Giao hàng & Vận chuyển (khớp 100% tài liệu SRS)
- **Số lượng bug đã fix**: 5 bugs lập trình được phát hiện và khắc phục triệt để.
- **Phong cách thiết kế**: Đồng bộ tông màu Sky Blue chủ đạo, áp dụng hiệu ứng Glassmorphism hiện đại.

---

## 2. Danh Sách Lỗi (Bugs) Lập Trình Đã Khắc Phục

| Bug ID | Vị Trí File | Nguyên Nhân Lỗi | Giải Pháp Khắc Phục |
| :--- | :--- | :--- | :--- |
| **B1** | `DeliveryService.java` | So sánh `!=` trên `Integer staffId` gây NPE khi autounbox nếu staffId là null. | Đổi thành `!staffId.equals(del.getStaffId())` để so sánh đối tượng an toàn. |
| **B2** | `ShopOrderServlet.java` | Giá trị hardcode `-1` khi gán shipper khiến đơn hàng bị ẩn khỏi dashboard của shipper. | Đổi thành `assignShipper(orderId, 0, ...)` và chuyển đổi thành `NULL` trong DAO. |
| **B3** | `DeliveryDAO.java` | Thiếu dấu cách khoảng trước từ khóa `WHERE` khi cộng chuỗi câu lệnh SQL cập nhật. | Thêm dấu cách: `" WHERE delivery_id = ?"` để tránh lỗi cú pháp T-SQL. |
| **B4** | `delivery-detail.jsp` | Tệp JSP trước đó chỉ là dạng stub (TODO) không hiển thị thông tin thực tế. | Triển khai giao diện timeline giao hàng đầy đủ, thông tin người nhận và ảnh bằng chứng. |
| **B5** | `DeliveryUpdateAPI.java` | Khi Shipper giao hàng thành công, hệ thống chỉ cập nhật bảng `deliveries` mà quên bảng `orders`. | Viết mới phương thức `markAsDelivered()` để cập nhật đồng bộ trạng thái cả hai bảng. |

---

## 3. Quy Trình Vận Hành Hệ Thống Giao Hàng

### Luồng Trạng Thái Giao Vận (Delivery State Machine)
1. **ASSIGNED (Mới nhận)**: Đơn hàng được Shop Owner duyệt và gán giao vận (mặc định chưa chỉ định Shipper cụ thể).
2. **PICKED_UP (Đã lấy hàng)**: Shipper nhận đơn giao và xác nhận đã lấy hàng trực tiếp tại kho của Shop Owner.
3. **IN_TRANSIT (Đang giao)**: Shipper bắt đầu quá trình vận chuyển nông sản đến vị trí của khách hàng.
4. **DELIVERED hoặc FAILED**: Shipper tải ảnh bằng chứng giao hàng thành công hoặc điền lý do nếu quá trình giao thất bại.

### Luồng Tự Nhận Đơn Giao Hàng (Shipper Self-Claim Flow)
Để nâng cao tính linh hoạt cho mạng lưới giao hàng tự do, hệ thống hỗ trợ cơ chế tự chọn đơn:
- **Đăng ký tự động**: Shop Owner khởi tạo giao vận không chỉ định shipper (`staff_id = NULL`). Đơn này hiển thị trên dashboard của toàn bộ shippers.
- **Xử lý tranh chấp (Race Condition)**: API nhận đơn sử dụng câu lệnh SQL nguyên tố với điều kiện `WHERE staff_id IS NULL`, đảm bảo chỉ duy nhất một shipper nhận đơn thành công đầu tiên, các shipper click sau sẽ nhận thông báo từ chối thân thiện.

---

## 4. Tuân Thủ Kiến Trúc OOP & Nguyên Tắc SOLID

- **Single Responsibility (SRP)**: Toàn bộ thao tác cơ sở dữ liệu được gói gọn bên trong `DeliveryDAO`. Tầng `DeliveryService` chỉ xử lý nghiệp vụ logic và kiểm tra dữ liệu đầu vào.
- **Don't Repeat Yourself (DRY)**: Tính năng **Mua lại đơn hàng (Reorder)** tái sử dụng hoàn toàn phương thức `CartDAO.addItem()` hiện tại mà không tạo thêm các hàm trùng lặp.
- **Data Transfer Object (DTO)**: Triển khai lớp wrapper `DeliveryWithOrderDTO` để truyền dữ liệu gộp từ hai thực thể `Delivery` và `Order` xuống tầng giao diện (JSP), giúp mã nguồn JSP sạch sẽ, không gọi trực tiếp DAO.

---

## 5. Thiết Kế & Cải Tiến Giao Diện Người Dùng (UI/UX)

- **Delivery Dashboard**: Bộ lọc tab linh hoạt cho phép Shipper quản lý đơn theo trạng thái. Hiển thị gọn gàng tên, SĐT và địa chỉ của khách hàng. Đặc biệt tích hợp nút **Nhận đơn giao hàng này** và huy hiệu nhấp nháy **Chờ Shipper nhận đơn** đối với các đơn hàng chưa có người đảm nhận.
- **Customer Order Detail**: Bổ sung thẻ thông tin vận chuyển hiển thị ảnh bằng chứng giao hàng của Shipper và thời gian giao dự kiến theo thời gian thực.
- **Reorder Button**: Thêm nút "Mua lại đơn hàng này" tại trang lịch sử mua hàng, tự động đưa các sản phẩm hợp lệ quay trở lại giỏ hàng kèm cảnh báo nếu có sản phẩm hết hàng.
