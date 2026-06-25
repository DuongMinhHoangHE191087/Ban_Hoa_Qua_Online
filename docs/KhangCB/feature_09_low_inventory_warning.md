# Chức năng 9: Cảnh báo tồn kho biến thể xuống dưới 5 sản phẩm cho chủ cửa hàng

## 1. Thông tin chung
*   **Tên chức năng:** Cảnh báo tự động khi tồn kho của sản phẩm/biến thể xuống thấp.
*   **Đối tượng sử dụng (Actor):** Hệ thống (System) gửi cho Chủ cửa hàng (Shop Owner).
*   **Mục tiêu:** Giúp chủ cửa hàng nắm bắt kịp thời các mặt hàng sắp hết để chủ động nhập thêm hàng hoặc ẩn sản phẩm, tránh trường hợp khách đặt mua nhưng không có hàng để giao.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Sự kiện cập nhật kho hàng (`InventoryService`):**
    *   Sự kiện trừ tồn kho xảy ra khi khách hàng hoàn tất đặt đơn mua trái cây.
    *   Hoặc khi chủ cửa hàng điều chỉnh giảm kho hàng thủ công.
2.  **Kích hoạt luồng kiểm tra (`InventoryService.checkAndSendLowStockAlert`):**
    *   Hệ thống thực hiện lấy ra thông tin ID của chủ cửa hàng từ `product_variants` và `products`.
    *   Truy vấn ngưỡng tồn kho cảnh báo tối thiểu được cấu hình riêng trong hồ sơ của shop: `shop_owner_profiles.low_stock_threshold` (Nếu không cấu hình, mặc định hệ thống sẽ dùng giá trị là 5).
3.  **So sánh số lượng tồn kho:**
    *   So sánh lượng tồn kho thực tế sau biến động (`stockAfter`) với ngưỡng cảnh báo (`threshold`).
    *   Nếu `stockAfter` $\le$ `threshold`, hệ thống tiến hành tạo cảnh báo.
4.  **Tạo thông báo in-app gửi chủ shop:**
    *   Hệ thống tự động tra cứu tên sản phẩm, tên biến thể (ví dụ: "Cam sành - Hộp 5kg") và mã SKU tương ứng.
    *   Tạo bản ghi thông báo in-app loại `'INVENTORY_ALERT'` gửi đến chủ cửa hàng: *"Sản phẩm 'Cam sành' (SKU: SP-1-1) sắp hết hàng. Chỉ còn 3 sản phẩm trong kho."*.
    *   Đính kèm URL hành động dẫn đến trang quản lý kho sản phẩm `/shop/products`.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `shop_owner_profiles`:** Lưu cấu hình ngưỡng cảnh báo `low_stock_threshold`.
*   **Bảng `product_variants`:** Cập nhật cột số lượng tồn kho `stock_quantity`.
*   **Bảng `notifications`:** Nhận thông báo cảnh báo kho hàng thấp.

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Truy vấn ngưỡng cảnh báo của chủ cửa hàng
SELECT low_stock_threshold FROM shop_owner_profiles WHERE user_id = ?;

-- 2. Truy vấn thông tin tên sản phẩm và SKU của biến thể để hiển thị thông báo
SELECT pv.sku, p.name 
FROM product_variants pv
JOIN products p ON pv.product_id = p.product_id
WHERE pv.variant_id = ?;

-- 3. Tạo thông báo in-app loại cảnh báo kho hàng
INSERT INTO notifications (user_id, type, title, message, action_url, is_read, created_at)
VALUES (?, 'INVENTORY_ALERT', 'Cảnh báo tồn kho thấp', ?, ?, 0, GETDATE());
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Chủ shop không cài đặt cấu hình:** Hệ thống sẽ tự động dùng giá trị mặc định tối thiểu là 5 sản phẩm để tránh trường hợp cảnh báo bị lỗi khi không tìm thấy cấu hình shop.
2.  **Spam thông báo:** Để tránh việc hệ thống liên tục gửi thông báo mỗi khi kho hàng giảm từ 4 xuống 3, rồi xuống 2, hệ thống có thể kết hợp kiểm tra xem đã có thông báo tồn kho thấp chưa được đọc cho sản phẩm này hay chưa, nếu có rồi sẽ không tạo thêm thông báo mới.
