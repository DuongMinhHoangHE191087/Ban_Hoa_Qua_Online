# Chức năng 7: Gửi thông báo xác nhận thanh toán/biên lai tự động cho khách hàng

## 1. Thông tin chung
*   **Tên chức năng:** Gửi biên lai và thông báo xác nhận thanh toán thành công.
*   **Đối tượng sử dụng (Actor):** Hệ thống (System) gửi cho Khách hàng (Customer).
*   **Mục tiêu:** Tự động gửi thông tin xác thực việc nhận tiền của hệ thống cho khách hàng sau khi họ hoàn thành giao dịch chuyển khoản hoặc thanh toán trực tuyến.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Sự kiện thanh toán thành công:**
    *   *Trường hợp 1:* Hệ thống nhận được webhook từ bên thứ ba (ví dụ: cổng thanh toán SePay) khớp mã giao dịch chuyển khoản.
    *   *Trường hợp 2:* Quản trị viên (Admin) kiểm tra tài khoản và click nút "Xác nhận đã nhận tiền" trên giao diện Admin Payment Dashboard (`/admin/payments`).
2.  **Xử lý nghiệp vụ (`PaymentService`):**
    *   Hệ thống gọi `PaymentDAO` cập nhật trạng thái giao dịch thanh toán trong bảng `payment_transactions` thành `'completed'`.
    *   Cập nhật trạng thái đơn hàng trong bảng `orders` thành `CONFIRMED` (Đã xác nhận).
3.  **Tạo thông báo xác nhận thanh toán (`PaymentService.adminApprovePayment`):**
    *   **In-app Notification:** Hệ thống tạo bản ghi thông báo in-app loại `'PAYMENT'` gửi đến `userId` của khách hàng: *"Đơn hàng #X đã được xác nhận thanh toán thành công."*
    *   **Email Receipt:** Gọi `emailService.sendOrderNotificationEmail()` gửi hóa đơn điện tử biên nhận đã thanh toán thành công, đính kèm link chi tiết đơn hàng.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `payment_transactions`:** Lưu thông tin giao dịch (số tiền, mã tham chiếu chuyển khoản, trạng thái giao dịch: `pending`, `processing`, `completed`).
*   **Bảng `notifications`:** Lưu vết thông báo in-app để khách hàng nhận diện.

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Cập nhật trạng thái giao dịch thanh toán thành công
UPDATE payment_transactions 
SET status = 'completed', updated_by = ?, updated_at = GETDATE() 
WHERE transaction_id = ?;

-- 2. Cập nhật trạng thái đơn hàng sang Đã xác nhận (CONFIRMED)
UPDATE orders 
SET status = 'CONFIRMED', updated_at = GETDATE() 
WHERE order_id = ?;

-- 3. Tạo thông báo in-app cho khách hàng
INSERT INTO notifications (user_id, type, title, message, action_url, is_read, created_at)
VALUES (?, 'PAYMENT', 'Thanh toán thành công', ?, ?, 0, GETDATE());
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lỗi trùng lặp webhook giao dịch (Idempotency):** Sử dụng cơ chế ghi nhận khóa duy nhất hoặc kiểm tra trạng thái giao dịch trước khi xử lý. Nếu giao dịch đã ở trạng thái `completed`, hệ thống bỏ qua và không gửi thông báo trùng lặp lần thứ hai cho khách hàng.
2.  **Khách hàng không nhận được email:** Hệ thống có cơ chế kiểm tra lại lịch sử gửi thông báo. Thông báo in-app luôn được ghi nhận trực tiếp vào Database để khách hàng đăng nhập là thấy ngay, đảm bảo thông tin không bị thất lạc hoàn toàn.
