# Chức năng 2: Chỉnh sửa thông tin sản phẩm và cập nhật vào Database

## 1. Thông tin chung
*   **Tên chức năng:** Sửa thông tin sản phẩm và cập nhật dữ liệu.
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Cho phép chủ cửa hàng sửa đổi các thông tin mô tả, giá cả, xuất xứ, hạn sử dụng, thông tin mùa vụ, và quản lý các biến thể/đóng gói hiện có của sản phẩm.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Client (Giao diện):** Chủ shop click nút "Sửa" trên sản phẩm. Hệ thống gửi yêu cầu GET lên `/shop/product-edit?productId=X`. Form hiển thị toàn bộ thông tin sản phẩm cũ được điền sẵn từ DB. Chủ shop sửa đổi thông tin và bấm **"Cập nhật"** (POST request).
2.  **Controller (`ProductEditServlet`):**
    *   Xác thực quyền hạn Shop Owner và kiểm tra quyền sở hữu đối với sản phẩm này (tránh sửa chéo sản phẩm của shop khác).
    *   Đọc các tham số mới gửi lên từ request form (tên, xuất xứ, mùa vụ, các giá trị biến thể cập nhật).
    *   Thực hiện validation thông tin đầu vào.
3.  **DAO Layer (`ProductDAO`, `ProductVariantDAO`):**
    *   Gọi `ProductDAO.update(product)` để cập nhật các thuộc tính cốt lõi trong bảng `products`.
    *   Đối với các biến thể (variants):
        *   Cập nhật các biến thể cũ đã có sự thay đổi về giá, cân nặng, chương trình khuyến mãi bằng `ProductVariantDAO.update()`.
        *   Nếu có thêm biến thể mới trong danh sách cập nhật, tiến hành gọi `ProductVariantDAO.save()`.
4.  **Database:** Lưu các thay đổi thông qua truy vấn `UPDATE` an toàn với SQL Parameterized Queries.
5.  **Redirect (PRG Pattern):** Lưu flash message thông báo thành công vào HTTP Session, sau đó redirect chủ shop về danh sách sản phẩm để tránh lặp lại request POST khi F5 trang.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `products`:** Lưu thông tin chung cập nhật (ngày thu hoạch, xuất xứ, bảo quản, Organic/Imported...).
*   **Bảng `product_variants`:** Cập nhật hoặc lưu thêm phân loại (giá bán, giá khuyến mãi, cân nặng...).

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Cập nhật thông tin chung sản phẩm
UPDATE products 
SET category_id = ?, name = ?, description = ?, origin_country = ?, 
    origin_region = ?, harvest_date = ?, shelf_life_days = ?, 
    storage_instruction = ?, is_organic = ?, is_imported = ?, 
    season_start_month = ?, season_end_month = ?, updated_at = GETDATE()
WHERE product_id = ? AND owner_id = ?;

-- 2. Cập nhật thông tin biến thể
UPDATE product_variants 
SET variant_label = ?, price = ?, weight_kg = ?, 
    discount_price = ?, discount_start = ?, discount_end = ?, 
    updated_at = GETDATE()
WHERE variant_id = ? AND product_id = ?;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lỗi vi phạm bản quyền dữ liệu:** Nếu `owner_id` của sản phẩm trong DB không khớp với `userId` trong Session, Servlet lập tức trả về mã lỗi HTTP 403 Forbidden (hoặc 404 Not Found) kèm thông báo lỗi.
2.  **Lỗi định dạng dữ liệu (ví dụ: nhập giá âm, định dạng số sai):** Servlet sẽ thêm thông báo lỗi vào thuộc tính lỗi, giữ nguyên giá trị người dùng vừa sửa và trả về trang form edit để hiển thị thông báo đỏ.
