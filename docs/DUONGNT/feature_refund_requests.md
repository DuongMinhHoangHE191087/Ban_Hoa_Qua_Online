# Feature Analysis: Handle Refund Requests

Chức năng xử lý yêu cầu đổi/trả hàng (Refund Requests) cho phép Admin xem xét và phê duyệt các yêu cầu trả hàng từ phía người mua, đảm bảo tính công bằng giữa người mua và Shop.

## 1. Thành phần Code

- **Controller (Servlet):** `src/java/servlet/admin/order/AdminRefundServlet.java`
- **Service Layer:** `service/order/ReturnRequestService.java`
- **DAO Layer:** DAO quản lý bảng Return Requests.
- **View (JSP):** `web/WEB-INF/jsp/admin/admin-refunds.jsp`

## 2. Phân tích Luồng Hoạt Động (Workflow)

### GET `/admin/refunds`
1. Lấy danh sách các yêu cầu đổi trả đang chờ xử lý (`PENDING`) thông qua `ReturnRequestService`.
2. Gắn kèm thông tin chi tiết: Lộ trình đơn hàng, hình ảnh bằng chứng (proof images) khách tải lên, lý do đổi trả.
3. Forward danh sách tới `admin-refunds.jsp`.

### POST `/admin/refunds` (Duyệt / Từ chối Đổi trả)
1. Servlet nhận `requestId` và `action` (APPROVE / REJECT) từ nút bấm của Admin.
2. Kiểm tra tính hợp lệ của action, sau đó gọi `ReturnRequestService.approveRequest()` hoặc `rejectRequest()`.
3. Trong Service:
   - Nếu **APPROVE**: 
     - Đổi trạng thái request thành `APPROVED`.
     - (Tùy logic nghiệp vụ) Cập nhật trạng thái Order thành `REFUNDED` hoặc `RETURNED`.
     - Phối hợp với `PaymentService` để tính toán lại số dư, hoàn tiền cho Customer và khấu trừ vào balance của Shop (nếu Shop đã được cộng tiền).
   - Nếu **REJECT**:
     - Đổi trạng thái request thành `REJECTED`.
     - Bắn Notification/Chat cho Customer báo lý do từ chối.
4. Trả về kết quả thông báo và tải lại danh sách bằng PRG (Post-Redirect-Get).

## 3. Điểm lưu ý (Key Takeaways)

- Đây cũng là luồng ảnh hưởng tới tài chính (Hoàn tiền), nên đòi hỏi DAO phải chạy Transaction để rollback nếu xảy ra rủi ro (lỗi network, dữ liệu thay đổi bất thường).
- Có sự giao thoa chặt chẽ với module `Payment` và `Notification`.
