# Chức năng 14: Biểu đồ thống kê tỷ lệ đơn hàng thành công và lý do hủy đơn hàng

## 1. Thông tin chung
*   **Tên chức năng:** Thống kê tỷ lệ thành công và lý do hủy đơn hàng.
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner), Quản trị viên (Admin).
*   **Mục tiêu:** Giúp quản trị viên và chủ shop nắm rõ tỷ lệ hoàn thành đơn hàng, theo dõi chi tiết số lượng đơn hàng bị hủy và lý do hủy chính (ví dụ: Khách đổi ý, hết hàng đột xuất, không liên hệ được người nhận), từ đó cải thiện dịch vụ chăm sóc khách hàng.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Request:** Người dùng mở tab thống kê đơn hàng trong trang báo cáo.
2.  **Controller (`ShopReportServlet` / `AdminReportServlet`):**
    *   Xác thực phiên đăng nhập và quyền hạn.
    *   Gọi Service để truy xuất dữ liệu thống kê trạng thái đơn hàng.
3.  **DAO Layer (`OrderDAO`):**
    *   *Thống kê trạng thái:* Đếm số lượng đơn hàng theo từng trạng thái như `DELIVERED` (Thành công), `CANCELLED` (Đã hủy), `RETURNED` (Đổi trả).
    *   *Thống kê lý do hủy:* Đếm số lượng đơn hàng bị hủy phân nhóm theo cột lý do hủy (`cancellation_reason` hoặc `note`).
4.  **JSP Render:**
    *   Đưa dữ liệu thống kê vào thuộc tính request và chuyển đổi thành chuỗi JSON nhúng trong các thẻ HTML ẩn.
5.  **Client JS (Chart.js vẽ biểu đồ Pie/Doughnut):**
    *   JavaScript đọc dữ liệu và vẽ 2 biểu đồ:
        *   **Biểu đồ 1 (Tròn - Pie):** Thống kê tỷ lệ phần trăm trạng thái đơn hàng (Đơn hàng thành công chiếm bao nhiêu %, Đơn hủy chiếm bao nhiêu %).
        *   **Biểu đồ 2 (Cột ngang - Horizontal Bar):** Thống kê các lý do hủy đơn hàng phổ biến nhất để tìm ra nguyên nhân chủ yếu làm mất doanh số.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `orders`:** Chứa trường trạng thái `status` và trường ghi nhận lý do hủy đơn `cancel_reason` / `cancellation_reason`.

---

## 4. Các câu lệnh SQL chính
```sql
-- 1. Thống kê số lượng đơn hàng theo các trạng thái của một shop
SELECT status, COUNT(*) AS count 
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN product_variants pv ON oi.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ?
GROUP BY o.status;

-- 2. Thống kê số lượng đơn hàng bị hủy phân loại theo lý do hủy
SELECT cancellation_reason, COUNT(*) AS count
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN product_variants pv ON oi.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ? AND o.status = 'CANCELLED'
GROUP BY cancellation_reason
ORDER BY count DESC;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lý do hủy đơn bị để trống hoặc NULL:** Khi người dùng hủy đơn nhưng không chọn lý do rõ ràng, SQL Group By sẽ gom nhóm vào danh mục "Không có lý do chi tiết". Giao diện Chart.js sẽ hiển thị nhãn này là *"Chủ động hủy / Khác"* để đảm bảo biểu đồ không bị hiển thị nhãn trống.
2.  **Lỗi tương thích kích thước màn hình:** Cấu hình Chart.js với thuộc tính `responsive: true` và `maintainAspectRatio: false` để biểu đồ tự động co giãn đẹp mắt trên các thiết bị di động của chủ cửa hàng.
