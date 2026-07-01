# Chức năng 4: Bật/Tắt ẩn hiện sản phẩm (Trạng thái ACTIVE hoặc INACTIVE)

## 1. Thông tin chung
*   **Tên chức năng:** Thay đổi trạng thái hiển thị của sản phẩm (Hiển thị/Ẩn).
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Cho phép chủ cửa hàng tạm thời ẩn sản phẩm khi hết hàng đột xuất hoặc cho phép hiển thị lại sản phẩm trên giao diện người dùng mà không cần xóa sản phẩm.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Client (Giao diện):** Tại danh sách quản lý sản phẩm của shop, chủ shop click vào công tắc bật/tắt (Toggle Switch) hiển thị. Trình duyệt gửi một request AJAX (POST) đến `/shop/product-status?action=toggle&productId=X&status=INACTIVE` (hoặc `ACTIVE`).
2.  **Controller (`ProductStatusServlet`):**
    *   Xác thực phiên đăng nhập và phân quyền Shop Owner.
    *   Kiểm tra quyền sở hữu sản phẩm của Shop Owner.
    *   Kiểm tra tính hợp lệ của tham số trạng thái yêu cầu (`ACTIVE`, `INACTIVE`).
    *   Kiểm tra logic phụ: Nếu muốn chuyển sang `ACTIVE` nhưng sản phẩm đã quá hạn thời gian mùa vụ (`isExpired` dựa trên tháng mùa vụ bắt đầu/kết thúc), tự động chuyển đổi mục tiêu trạng thái thành `OUT_OF_SEASON`.
    *   Gọi `ProductDAO.updateStatus(productId, targetStatus)`.
3.  **DAO Layer (`ProductDAO`):**
    *   Thực hiện cập nhật cột `status` trong cơ sở dữ liệu dựa trên `productId`.
4.  **Database:** Lưu dữ liệu cập nhật trạng thái mới.
5.  **Response (Client):** Gửi phản hồi JSON thành công về Client. Giao diện thay đổi trạng thái của Switch (màu xanh/màu xám) và hiển thị thông báo góc màn hình (toast message).

---

## 3. Cấu trúc Database liên quan
*   **Bảng `products`:** Trường cập nhật chính là `status` (kiểu dữ liệu VARCHAR, nhận các giá trị: `'ACTIVE'`, `'INACTIVE'`, `'OUT_OF_SEASON'`, `'DELETED'`).

---

## 4. Các câu lệnh SQL chính
```sql
-- Cập nhật trạng thái hiển thị của sản phẩm
UPDATE products 
SET status = ?, updated_at = GETDATE() 
WHERE product_id = ?;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Tham số trạng thái gửi lên không đúng định dạng:** Servlet kiểm tra giá trị của tham số `status`. Nếu giá trị không nằm trong danh sách cho phép, trả về mã lỗi HTTP 400 Bad Request kèm thông báo: *"Trạng thái không hợp lệ"*.
2.  **Sản phẩm đã bị xóa trước đó:** Nếu sản phẩm đã ở trạng thái `DELETED` hoặc không tồn tại, trả về thông báo lỗi thích hợp để tránh khôi phục các sản phẩm đã bị hủy.
