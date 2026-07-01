# Chức năng 13: Báo cáo bán hàng nâng cao và sản lượng tiêu thụ hoa quả

## 1. Thông tin chung
*   **Tên chức năng:** Báo cáo chi tiết bán hàng và sản lượng hoa quả tiêu thụ.
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner), Quản trị viên (Admin).
*   **Mục tiêu:** Cung cấp báo cáo số liệu dạng bảng chi tiết về doanh số bán ra của từng loại trái cây, phân tích sản phẩm bán chạy nhất, sản lượng hoa quả hao hụt và doanh thu thực nhận sau chiết khấu.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Client (Giao diện):** Người dùng truy cập trang Báo cáo (`/shop/report` hoặc `/admin/report`), chọn bộ lọc:
    *   Khoảng thời gian: Từ ngày - Đến ngày.
    *   Danh mục hoa quả (ví dụ: Trái cây nhập khẩu, Trái cây Organic).
    *   Bấm nút **"Lọc báo cáo"**.
2.  **Controller (`ShopReportServlet` / `AdminReportServlet`):**
    *   Nhận các tham số lọc từ yêu cầu gửi lên.
    *   Kiểm tra tính hợp lệ của tham số ngày (tránh ngày bắt đầu lớn hơn ngày kết thúc).
    *   Gọi Service thực hiện phân tích báo cáo: `orderService.getSalesReport(ownerId, startDate, endDate)`.
3.  **DAO Layer (`OrderDAO`, `ProductDAO`):**
    *   Thực hiện truy vấn SQL phức tạp kết nối (JOIN) nhiều bảng để tổng hợp dữ liệu.
    *   Tính toán số tiền chiết khấu hệ thống và doanh thu thực tế shop nhận được.
    *   Thống kê số lượng từng mặt hàng đã được giao thành công.
4.  **JSP Render (Kết quả hiển thị):**
    *   Servlet trả về kết quả là danh sách đối tượng báo cáo.
    *   Đổ dữ liệu vào bảng HTML trong trang JSP với các cột: Tên sản phẩm, SKU, Đơn vị tính, Số lượng bán ra, Tổng doanh thu, Chiết khấu sàn, Doanh thu thực nhận.
    *   Hỗ trợ xuất bảng báo cáo này thành định dạng Excel hoặc PDF cho người dùng tải về máy.

---

## 3. Cấu trúc Database liên quan
*   **Bảng `orders`:** Cột `status`, `created_at`.
*   **Bảng `order_items`:** Cột `quantity`, `price`, `packaging_price_add` (chi tiết từng loại trái cây bán ra).
*   **Bảng `products` / `product_variants`:** Cột `name`, `sku`, `weight_kg`.

---

## 4. Các câu lệnh SQL chính
```sql
-- Thống kê sản lượng tiêu thụ hoa quả của cửa hàng trong khoảng thời gian cụ thể
SELECT 
    p.name AS product_name,
    pv.sku AS variant_sku,
    SUM(oi.quantity) AS total_quantity_sold,
    SUM(oi.price * oi.quantity) AS gross_revenue,
    SUM(oi.price * oi.quantity * 0.05) AS platform_fee, -- Ví dụ phí sàn 5%
    SUM(oi.price * oi.quantity * 0.95) AS net_revenue
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN product_variants pv ON oi.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ? 
  AND o.status = 'DELIVERED'
  AND o.created_at BETWEEN ? AND ?
GROUP BY p.name, pv.sku
ORDER BY total_quantity_sold DESC;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Lỗi truy vấn quá tải bộ nhớ (Out of Memory):** Khi khoảng thời gian lọc quá dài và hệ thống có hàng triệu đơn hàng, việc nạp toàn bộ bản ghi sẽ gây đơ server. Hệ thống xử lý bằng cách giới hạn khoảng thời gian lọc tối đa là 1 năm hoặc áp dụng phân trang cho báo cáo.
2.  **Định dạng ngày nhập sai:** Nếu người dùng nhập ngày bắt đầu sau ngày kết thúc hoặc nhập sai định dạng chuỗi, Servlet bắt lỗi hiển thị cảnh báo đỏ và tự động gán ngày bắt đầu là đầu tháng hiện tại và ngày kết thúc là hôm nay.
