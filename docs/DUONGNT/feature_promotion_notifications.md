# Feature Analysis: Promotion Notifications (System Broadcast)

Chức năng Promotion Notifications cho phép Admin tạo và gửi đi các thông báo dạng Broadcast (thông báo toàn hệ thống) đến tất cả người dùng hoặc một nhóm người dùng cụ thể (Customer, Shop Owner), thường dùng cho các dịp sự kiện, khuyến mãi.

## 1. Thành phần Code

- **Controller (Servlet):** `src/java/servlet/admin/system/AdminNotificationServlet.java` (hoặc tương tự)
- **Service Layer:** `service/NotificationService.java` (hoặc Service tương đương chịu trách nhiệm logic Notification)
- **DAO Layer:** DAO thao tác với bảng `Notifications` hoặc gửi trực tiếp qua Websocket/Email.
- **View (JSP):** `web/WEB-INF/jsp/admin/admin-notifications.jsp` (hoặc giao diện gửi thông báo tương tự).

## 2. Phân tích Luồng Hoạt Động (Workflow)

### GET `/admin/notifications`
1. Hiển thị UI Form cho phép Admin nhập Tiêu đề, Nội dung thông báo, và chọn Đối tượng nhận (All, Customers Only, Shop Owners Only).
2. Liệt kê lịch sử các thông báo Broadcast đã từng gửi trước đó.

### POST `/admin/notifications`
1. **Tiếp nhận Dữ liệu:** Lấy các tham số `title`, `content`, `targetAudience` từ form submit.
2. **Xử lý Service (`NotificationService.sendBroadcast`)**:
   - Lưu trữ bản record Notification dạng "GLOBAL" vào DB (để ai login vào cũng có thể nhìn thấy, hoặc gen ra các records nhỏ cho từng user tùy kiến trúc table).
   - Nếu hệ thống tích hợp **WebSocket** (như `ChatEndpoint`), Service có thể kích hoạt Websocket session để đẩy push-notification ngay lập tức tới các thiết bị đang online.
3. Trả về kết quả cho View thông qua redirect.

## 3. Điểm lưu ý (Key Takeaways)

- **Hiệu suất (Performance):** Nếu hệ thống có nhiều user, việc `INSERT` từng dòng notification vào Database khi bấm gửi là không khả thi. Giải pháp là thiết kế bảng dạng Broadcast (Lưu 1 dòng kèm cờ `isGlobal=true`) để tối ưu hóa.
- Tính năng này đặc biệt hữu dụng cho các chiến dịch Sale, yêu cầu bảo mật cao ở Servlet để tránh lỗ hổng người lạ gửi thông báo rác (chỉ Admin được truy cập).
