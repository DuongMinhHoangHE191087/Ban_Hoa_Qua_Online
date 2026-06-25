# Chức năng 8: Thông báo tiến trình vận chuyển cho khách hàng (Đang giao / Đã giao)

## 1. Thông tin chung
*   **Tên chức năng:** Gửi thông báo cập nhật tiến độ vận chuyển đơn hàng.
*   **Đối tượng sử dụng (Actor):** Hệ thống (System) / Nhân viên giao hàng (Delivery Staff).
*   **Mục tiêu:** Cung cấp thông tin trực quan cho khách hàng khi đơn hàng được thay đổi trạng thái giao vận sang "Đang giao hàng" hoặc "Đã giao hàng thành công", giúp khách hàng chủ động thời gian nhận hoa quả.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Sự kiện chuyển đổi trạng thái giao vận:**
    *   *Sự kiện 1 (Đang giao):* Nhân viên giao hàng (Delivery Staff) hoặc Shop Owner đổi trạng thái đơn hàng sang Đang giao (`DISPATCHED`).
    *   *Sự kiện 2 (Đã giao):* Nhân viên giao hàng xác nhận giao thành công, tải lên ảnh minh chứng hoặc cập nhật trạng thái đơn hàng sang Đã giao (`DELIVERED`).
2.  **Xử lý nghiệp vụ (`DeliveryService` / `OrderService`):**
    *   Cập nhật trạng thái đơn hàng tương ứng trong bảng `orders`.
3.  **Tạo thông báo vận chuyển gửi khách hàng:**
    *   **Khi đơn đang giao (`DISPATCHED`):**
        *   Tạo thông báo in-app: *"Đơn hàng #X đang được giao. Vui lòng chú ý điện thoại để nhận hàng."*
        *   Gửi email tự động thông báo đơn hàng đang được chuyển đi.
    *   **Khi đơn giao thành công (`DELIVERED`):**
        *   Tạo thông báo in-app: *"Đơn hàng #X đã giao thành công. Cảm ơn bạn đã ủng hộ cửa hàng!"*
        *   Gửi email hóa đơn cuối cùng báo hoàn tất đơn hàng.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `orders`:** Cập nhật cột `status` thành `'DISPATCHED'` hoặc `'DELIVERED'`.
*   **Bảng `notifications`:** Ghi nhận thông báo in-app loại `'ORDER_UPDATE'`.

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Cập nhật trạng thái vận chuyển trong đơn hàng
UPDATE orders 
SET status = ?, updated_at = GETDATE() 
WHERE order_id = ?;

-- 2. Lưu thông báo giao vận cho khách hàng vào database
INSERT INTO notifications (user_id, type, title, message, action_url, is_read, created_at)
VALUES (?, 'ORDER_UPDATE', ?, ?, ?, 0, GETDATE());
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Giao hàng thất bại (Khách không nghe máy, không có người nhận):** Đơn vị vận chuyển cập nhật trạng thái thành giao thất bại/hoàn hàng. Hệ thống tự động gửi thông báo in-app báo đơn hàng bị tạm ngưng hoặc đổi trạng thái để khách hàng liên hệ lại cửa hàng giải quyết.
2.  **Thông báo bị trễ:** Hệ thống sử dụng Logger để ghi vết thời gian đổi trạng thái, giúp admin kiểm tra được thời gian thực tế đơn được giao đi và thời gian gửi thông báo để tối ưu hệ thống.
