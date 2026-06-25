# Chức năng 3: Xóa mềm sản phẩm (Soft Delete bằng cách cập nhật status sang DELETED)

## 1. Thông tin chung
*   **Tên chức năng:** Xóa mềm sản phẩm (Soft Delete Product).
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Ẩn sản phẩm khỏi cửa hàng và công cụ tìm kiếm của khách hàng mà không làm hỏng tính toàn vẹn dữ liệu trong cơ sở dữ liệu (các đơn hàng cũ vẫn giữ được tham chiếu đến sản phẩm).

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Client (Giao diện):** Từ danh sách sản phẩm, chủ shop bấm nút "Xóa". Trình duyệt gửi một request AJAX (POST) đến `/shop/product-status?action=delete&productId=X`.
2.  **Controller (`ProductStatusServlet`):**
    *   Xác thực phiên làm việc (Session) của chủ shop.
    *   Kiểm tra xem sản phẩm có thuộc quyền quản lý của cửa hàng hiện tại hay không.
    *   Nếu hợp lệ, gọi phương thức nghiệp vụ xóa sản phẩm: `productDAO.deleteProduct(productId)`.
3.  **DAO Layer (`ProductDAO` - Sử dụng Transaction):**
    *   Thực hiện đồng bộ các hành động sau trong một Database Transaction:
        1.  Cập nhật trạng thái của dòng sản phẩm trong bảng `products` thành `'DELETED'`.
        2.  Cập nhật thuộc tính `is_active = 0` (hủy kích hoạt) của tất cả biến thể liên quan trong bảng `product_variants`.
        3.  Xóa tất cả các bản ghi chứa biến thể của sản phẩm này trong bảng giỏ hàng của khách hàng (`cart_items`) để tránh việc khách hàng tiến hành checkout sản phẩm đã bị xóa.
        4.  Hủy kích hoạt các khuyến mãi (`promotions`) riêng của sản phẩm này.
4.  **Database:** Lưu các thay đổi và kết thúc (Commit) Transaction.
5.  **Response (Client):** Trả về phản hồi JSON báo thành công. JavaScript ở Client bắt sự kiện, thực hiện hiệu ứng ẩn dòng sản phẩm trên bảng hiển thị mà không cần tải lại toàn bộ trang.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `products`:** Cập nhật cột `status` thành `'DELETED'`.
*   **Bảng `product_variants`:** Cập nhật `is_active` thành `0`.
*   **Bảng `cart_items`:** Xóa các bản ghi liên quan để giải phóng giỏ hàng của người dùng.

---

## 4. Các câu lệnh SQL chính
```sql
-- Chạy trong một Transaction
-- 1. Cập nhật trạng thái sản phẩm
UPDATE products SET status = 'DELETED', updated_at = GETDATE() WHERE product_id = ?;

-- 2. Hủy kích hoạt tất cả các biến thể
UPDATE product_variants SET is_active = 0, updated_at = GETDATE() WHERE product_id = ?;

-- 3. Xóa sản phẩm khỏi giỏ hàng của khách hàng
DELETE FROM cart_items 
WHERE variant_id IN (SELECT variant_id FROM product_variants WHERE product_id = ?);
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Sản phẩm không thuộc quyền sở hữu:** Hệ thống trả về lỗi HTTP 403 Forbidden dạng JSON: `{"status": 403, "message": "Không có quyền chỉnh sửa..."}`.
2.  **Lỗi khóa ngoại hoặc Database:** Cơ chế Transaction tự động gọi `conn.rollback()` để đảm bảo không xảy ra xung đột dữ liệu (ví dụ: sản phẩm bị đánh dấu xóa nhưng biến thể vẫn hoạt động). Ghi nhận log chi tiết thông qua `LoggerUtil`.
