# Chức năng 5: CRUD hình ảnh sản phẩm (Quản lý tải lên nhiều ảnh)

## 1. Thông tin chung
*   **Tên chức năng:** Thêm, sửa, xóa và sắp xếp thứ tự hiển thị của nhiều hình ảnh sản phẩm.
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Quản lý kho hình ảnh của sản phẩm, giúp sản phẩm hiển thị bắt mắt, hỗ trợ thiết lập ảnh chính đại diện (Primary) và sắp xếp thứ tự hiển thị của ảnh phụ.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
Quản lý ảnh được tích hợp trong quá trình **Thêm/Sửa sản phẩm** và qua các **API AJAX** xử lý ảnh nhanh:

### A. Tải lên nhiều ảnh (Thêm/Sửa sản phẩm)
*   Chủ shop chọn nhiều file ảnh mô tả trong form. Request gửi dưới dạng `multipart/form-data`.
*   Servlet tiếp nhận, dùng `FileUploadUtil` lưu file vật lý vào thư mục máy chủ.
*   Lưu các bản ghi tương ứng vào bảng `product_images` với thứ tự tự động tăng (`display_order`).

### B. Thiết lập ảnh đại diện chính (Set Primary - AJAX)
*   **Client:** Bấm nút "Đặt làm ảnh chính" trên giao diện quản lý ảnh sản phẩm. Request AJAX gửi lên `/shop/product-status?action=set-primary&imageId=X&productId=Y`.
*   **Controller:** Kiểm tra bảo mật và quyền sở hữu sản phẩm. Gọi `ProductImageDAO.setPrimary(imageId, productId)`.
*   **DAO (Transaction):**
    1.  Chuyển `is_primary = 0` (False) đối với tất cả ảnh thuộc về `productId`.
    2.  Chuyển `is_primary = 1` (True) riêng đối với `imageId`.

### C. Sắp xếp thứ tự ảnh (Reorder Images - AJAX)
*   **Client:** Người dùng thực hiện kéo thả hoặc sắp xếp ảnh trên giao diện. Trình duyệt gửi request AJAX chứa chuỗi danh sách ID ảnh đã sắp xếp (ví dụ: `imageIds=12,15,13`).
*   **Controller & DAO:** Tách chuỗi ID và cập nhật lại thứ tự hiển thị (`display_order` từ 0 đến n) trong bảng `product_images` cho từng ID ảnh.

### D. Xóa ảnh sản phẩm (Delete Image - AJAX)
*   **Client:** Chủ shop bấm nút "Xóa ảnh" (biểu tượng thùng rác). Gửi request AJAX chứa `action=delete-image&imageId=X`.
*   **Controller & Util:**
    1.  Đọc đường dẫn file ảnh (`file_path`) trong DB.
    2.  Gọi `FileUploadUtil.delete(absolutePath)` xóa file vật lý trên server.
    3.  Gọi `ProductImageDAO.delete(imageId)` xóa dòng thông tin trong Database.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `product_images`:**
    *   `image_id` (INT, Primary Key, Auto Increment)
    *   `product_id` (INT, Foreign Key)
    *   `file_path` (VARCHAR - Lưu đường dẫn tương đối của file ảnh trên máy chủ)
    *   `display_order` (INT - Thứ tự hiển thị)
    *   `is_primary` (BIT / BOOLEAN - Đánh dấu ảnh đại diện chính)

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Đặt ảnh chính (Đưa toàn bộ ảnh khác về ảnh phụ)
UPDATE product_images SET is_primary = 0 WHERE product_id = ?;
-- 2. Đặt ảnh được chọn làm ảnh chính
UPDATE product_images SET is_primary = 1 WHERE image_id = ?;

-- 3. Cập nhật thứ tự hiển thị của hình ảnh
UPDATE product_images SET display_order = ? WHERE image_id = ?;

-- 4. Xóa bản ghi hình ảnh khỏi database
DELETE FROM product_images WHERE image_id = ?;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lỗi không tìm thấy file vật lý khi xóa:** Nếu file vật lý trên máy chủ đã bị mất trước đó, hàm xóa file vật lý sẽ bỏ qua an toàn và tiếp tục thực hiện xóa dòng trong cơ sở dữ liệu để tránh treo ứng dụng.
2.  **Lỗi xóa mất tất cả ảnh đại diện:** Khi một ảnh chính bị xóa, hệ thống sẽ tự động quét ảnh tiếp theo có thứ tự hiển thị nhỏ nhất để làm ảnh đại diện mới, đảm bảo sản phẩm luôn có hình ảnh hiển thị trên trang chủ.
