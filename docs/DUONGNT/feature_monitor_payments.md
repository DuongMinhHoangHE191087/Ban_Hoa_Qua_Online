# Feature Analysis: Monitor Payments Global

Chức năng đối soát thanh toán (Payments/Settlement Dashboard) cho phép Admin theo dõi dòng tiền thanh toán từ khách hàng tới các shop, kiểm soát việc tính phí nền tảng (platform fee) và thực hiện quá trình chuyển tiền cho Shop (Settlement).

## 1. Thành phần Code

- **Controller (Servlet):** `src/java/servlet/admin/settlement/AdminPaymentServlet.java` (và `AdminSettlementServlet.java`)
- **Service Layer:** `service/shop/PaymentService.java`, `service/shop/SettlementService.java`
- **DAO Layer:** Các DAO truy vấn bảng `PaymentTransaction` hoặc `Settlement`.
- **View (JSP):** `web/WEB-INF/jsp/admin/payment-dashboard.jsp`

## 2. Phân tích Luồng Hoạt Động (Workflow)

### GET `/admin/payments`
1. Khởi tạo đối tượng `PaymentService` hoặc `SettlementService`.
2. Truy vấn danh sách các giao dịch thanh toán hoặc yêu cầu rút tiền (Settlement Request) của các shop.
3. Tổng hợp số liệu:
   - Tổng tiền đang giữ (Hold).
   - Tổng tiền đã chuyển cho shop (Settled).
   - Doanh thu từ phí nền tảng (Platform fee revenue).
4. Các dữ liệu này được set vào request và render lên bảng thống kê tài chính.

### POST `/admin/payments` (Thực thi lệnh Settlement)
1. Khi Admin bấm nút "Duyệt lệnh rút tiền" (Approve Settlement), Servlet nhận request (có chứa `settlementId`).
2. Chuyển cho `SettlementService.approveSettlement(id)` thực thi:
   - Thay đổi trạng thái Settlement từ `PENDING` sang `COMPLETED`.
   - Cập nhật số dư/trạng thái trong `PaymentTransaction` hoặc bảng Shop balance tương ứng.
3. Redirect (PRG Pattern) về lại trang `/admin/payments` với thông báo thành công.

## 3. Điểm lưu ý (Key Takeaways)

- Đây là chức năng cốt lõi về **tài chính**, do đó mọi thao tác update trong DAO phải chạy trong Transaction (`conn.setAutoCommit(false)`), nếu có bất kỳ lỗi nào xảy ra phải `rollback()` để đảm bảo không bị thất thoát tiền.
- Cần chú ý tính chính xác của thuật toán tính phí nền tảng (Fee percentage được lấy từ `SystemConfig`).
