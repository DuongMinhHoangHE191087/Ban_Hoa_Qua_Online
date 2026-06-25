# Chức năng 10: Thông báo tức thời cho Shop Owner khi có đơn hàng mới được đặt

## 1. Thông tin chung
*   **Tên chức năng:** Cảnh báo đơn hàng mới cho chủ shop.
*   **Đối tượng sử dụng (Actor):** Hệ thống (System) gửi cho Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Giúp chủ shop phát hiện đơn hàng mới ngay lập tức để thực hiện quy trình xác nhận, đóng gói trái cây tươi và bàn giao cho đơn vị vận chuyển sớm nhất, đảm bảo độ tươi ngon của sản phẩm.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Giao dịch đặt hàng:** Khách hàng hoàn tất thanh toán giỏ hàng.
2.  **Phân tách đơn hàng theo shop (`CheckoutService`):**
    *   Giỏ hàng của khách hàng có thể mua sản phẩm từ nhiều shop khác nhau. Hệ thống tiến hành tách giỏ hàng lớn thành các đơn hàng nhỏ (Child Orders) thuộc về từng Shop Owner tương ứng dựa trên `owner_id` của sản phẩm.
3.  **Tạo thông báo tức thời:**
    *   Với mỗi đơn hàng con được tạo, hệ thống lập tức gọi `NotificationService.send()` gửi thông báo in-app đến tài khoản của chủ shop đó.
    *   **Nội dung thông báo:** *"Có đơn hàng mới cần chuẩn bị (Mã đơn hàng: #Y). Tổng tiền: Z VND."*.
    *   **URL hành động:** `/shop/orders` (đưa chủ shop trực tiếp tới giao diện quản lý đơn hàng của cửa hàng).
4.  **Tự động gửi email đi kèm:** Đồng thời gửi một email thông báo có đơn hàng mới đến email đăng ký của chủ shop qua `EmailService.sendOrderNotificationEmail`.
5.  **Client UI:** Khi chủ shop đang mở giao diện web quản lý, chuông thông báo ở góc màn hình sẽ tự động cập nhật số lượng tăng lên nhờ cơ chế AJAX Polling tải lại số lượng thông báo chưa đọc.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `orders`:** Đơn hàng con liên kết với `owner_id`.
*   **Bảng `notifications`:** Lưu vết thông báo in-app loại `'ORDER_UPDATE'` gửi cho chủ shop.

---

## 4. Các câu lệnh SQL chính
```sql
-- Lưu thông báo in-app cho Chủ shop
INSERT INTO notifications (user_id, type, title, message, action_url, is_read, created_at)
VALUES (?, 'ORDER_UPDATE', 'Có đơn hàng mới cần chuẩn bị', ?, ?, 0, GETDATE());
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lỗi gửi email cho chủ shop:** Nếu Mail Server của chủ shop bị đầy hoặc email sai, hệ thống bắt lỗi trong block `try-catch` gửi email, ghi log hệ thống và đảm bảo thông báo in-app vẫn được tạo thành công để chủ shop thấy thông tin khi đăng nhập vào web quản trị.
2.  **Thông báo đơn hàng không thuộc về chủ shop:** Hệ thống bảo mật chặt chẽ bằng cách so khớp ID tài khoản người nhận trong bảng `notifications` với Session hiện tại của chủ shop khi họ truy cập vào đường dẫn `action_url`.
