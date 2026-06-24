# Chức năng 12: Biểu đồ trực quan hóa doanh thu sử dụng thư viện Chart.js

## 1. Thông tin chung
*   **Tên chức năng:** Thống kê và vẽ biểu đồ doanh thu cửa hàng/toàn sàn.
*   **Đối tượng sử dụng (Actor):** Chủ cửa hàng (Shop Owner), Quản trị viên (Admin).
*   **Mục tiêu:** Cung cấp cái nhìn trực quan, nhanh chóng về xu hướng phát triển doanh số bán hoa quả theo thời gian bằng biểu đồ đường hoặc cột tương tác cao.

---

## 2. Luồng hoạt động chi tiết (Workflow Flow)
1.  **Request:** Người dùng truy cập trang dashboard quản lý (`/shop/dashboard` hoặc `/admin/dashboard`).
2.  **Controller (`ShopDashboardServlet` / `AdminDashboardServlet`):**
    *   Xác thực quyền của người truy cập (Admin hoặc Shop Owner).
    *   Gọi Service tương ứng để lấy dữ liệu doanh thu tích lũy và xu hướng doanh thu (Ví dụ: Doanh thu thực tế đã quyết toán, doanh thu ước tính của các đơn đang xử lý).
3.  **DAO Layer (`OrderDAO`):**
    *   Thực hiện các truy vấn SQL tổng hợp doanh thu theo tuần, tháng hoặc năm (sử dụng hàm `SUM(total_amount)` và lọc theo trạng thái đơn hàng).
4.  **JSP Render (Truyền dữ liệu ngầm):**
    *   Servlet nhận kết quả dữ liệu từ DAO, chuyển thành định dạng JSON và đính kèm vào thẻ HTML ẩn trong trang JSP.
    *   Ví dụ: `<div id="chart-data" class="hidden" data-revenue-trend='[{"date":"2026-06-20","revenue":1500000}, ...]'>`.
5.  **Client JS (Chart.js vẽ biểu đồ):**
    *   Đoạn mã JavaScript trên trang JSP lấy dữ liệu từ thẻ ẩn bằng `getAttribute('data-revenue-trend')` và `JSON.parse()`.
    *   Khởi tạo đối tượng biểu đồ Chart.js:
        ```javascript
        const ctx = document.getElementById('revenueTrendChart').getContext('2d');
        new Chart(ctx, {
            type: 'line', // Biểu đồ dạng đường
            data: {
                labels: labelsArray, // Các mốc ngày
                datasets: [{
                    label: 'Doanh thu (VND)',
                    data: dataArray, // Số tiền tương ứng
                    borderColor: '#4d661c',
                    tension: 0.3
                }]
            }
        });
        ```

---

## 3. Cấu trúc Database liên quan
*   **Bảng `orders`:** Lưu trữ thông tin số tiền thanh toán (`total_amount`), trạng thái đơn hàng (`status`), và ngày đặt hàng (`created_at`).
*   **Bảng `order_items`:** Chi tiết các sản phẩm trong đơn để tính doanh thu riêng lẻ của từng shop.

---

## 4. Các câu lệnh SQL chính
```sql
-- Thống kê doanh thu theo ngày của một Shop Owner cụ thể (đơn hàng thành công)
SELECT CAST(o.created_at AS DATE) AS order_date, SUM(oi.price * oi.quantity) AS daily_revenue
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN product_variants pv ON oi.variant_id = pv.variant_id
JOIN products p ON pv.product_id = p.product_id
WHERE p.owner_id = ? AND o.status = 'DELIVERED'
GROUP BY CAST(o.created_at AS DATE)
ORDER BY order_date;
```

---

## 5. Các trường hợp lỗi & Cách xử lý (Error Handling)
1.  **Dữ liệu rỗng:** Khi shop mới được tạo và chưa có doanh số, DAO trả về danh sách rỗng. JS trên giao diện kiểm tra điều kiện này và hiển thị thông báo *"Chưa có dữ liệu thống kê doanh thu"* thay vì vẽ một biểu đồ lỗi hoặc biểu đồ trống.
2.  **Lỗi nạp thư viện Chart.js CDN:** Sử dụng đường dẫn thư viện Chart.js an toàn thông qua HTTPS CDN chất lượng cao. Nếu CDN lỗi, trang web vẫn hiển thị bảng số liệu thống kê chi tiết bằng bảng HTML thông thường để thay thế.
