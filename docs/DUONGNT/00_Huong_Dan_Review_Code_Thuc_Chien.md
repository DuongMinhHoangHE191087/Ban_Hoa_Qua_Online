# Hướng dẫn Review Code Thực Chiến (Dành riêng cho Actor Admin)

Đây là tài liệu hướng dẫn **phương pháp review code chuẩn nhất** để bạn có thể tự tin biểu diễn trước giảng viên. Thay vì học vẹt từng dòng code, bạn chỉ cần nắm đúng **Quy trình 4 Bước** dưới đây. Nó áp dụng cho mọi chức năng Admin trong dự án!

---

## Phần 1: Quy trình 4 Bước Review 1 Chức năng Bất Kỳ

Để giảng viên thấy bạn thực sự hiểu nghiệp vụ (Business Logic), hãy làm theo đúng thứ tự này khi thuyết trình:

1. **Bước 1: Trình diễn App & Test Use Case (Blackbox Testing)**
   - Mở trình duyệt, đăng nhập bằng tài khoản Admin.
   - Bấm vào tính năng cần review.
   - Cố tình thao tác sai để hiển thị thông báo lỗi (ví dụ: Không nhập lý do từ chối).
   - Thao tác đúng để hệ thống báo thành công.
   - *Mục đích: Chứng minh sản phẩm chạy thực tế và bạn có test các "Edge cases" (trường hợp ngoại lệ).*

2. **Bước 2: Chỉ ra sự kiện trên Giao diện (JSP)**
   - Bật F12 (Developer Tools) -> Chuyển sang tab **Network**.
   - Bấm nút trên giao diện, chỉ cho giảng viên thấy: *"Thưa thầy, khi em bấm nút này, nó sẽ gửi một request POST đến đường dẫn của Admin kèm theo các tham số (action, id...)"*.
   - Mở file `.jsp` tương ứng ra chỉ đoạn mã HTML chứa cái form/nút đó.

3. **Bước 3: Bắt luồng tại Controller (Servlet)**
   - Vào NetBeans, dùng tổ hợp phím **Ctrl + O**, tìm file Servlet đang "hứng" cái URL đó.
   - Chỉ ra hàm `doPost()` tiếp nhận dữ liệu.

4. **Bước 4: Mổ xẻ Nghiệp vụ (Service) và Cơ sở dữ liệu (DAO)**
   - Từ Servlet, Ctrl + Click để nhảy thẳng vào file **Service**. Giải thích các dòng code `if-else` (Luật nghiệp vụ).
   - Từ Service, nhảy tiếp vào **DAO**. Giải thích câu lệnh SQL.

---

## Phần 2: Kịch bản Mẫu - Chức năng "Quản lý Đổi Trả / Hoàn Tiền" (Admin Core Feature)

Dưới đây là kịch bản ráp nối đúng 4 bước trên cho chức năng **Handle Refund Requests** (thuộc Phase Quản trị hệ thống của Admin). Bạn hãy vừa đọc file này, vừa mở NetBeans để thực hành theo nhé:

### Bước 1: Trình diễn Use Case
- Đăng nhập tài khoản Admin, vào màn hình **Quản lý Đổi Trả (Refunds)**.
- Nhấn xem 1 yêu cầu hoàn tiền đang ở trạng thái `REQUESTED` (Khách hàng vừa tạo khiếu nại).
- Bấm nút **Phê Duyệt (Approve)**.
- Hệ thống báo: *"Đã duyệt yêu cầu hoàn tiền!"*. Trạng thái chuyển thành `APPROVED`.

### Bước 2: Code Giao diện
- Mở file: `web/WEB-INF/jsp/admin/admin-refunds.jsp`.
- Nói: *"Trên giao diện, em có các nút xử lý (Duyệt/Từ chối). Khi bấm, form sẽ gửi `action=approve` hoặc `action=reject` cùng với `requestId` và `orderId` về đường dẫn `/admin/refunds`."*

### Bước 3: Code Controller
- Mở file: `src/java/servlet/admin/order/AdminRefundServlet.java`.
- Nói: *"Tại hàm `doPost()`, em lấy các tham số từ request (action, requestId, reason). Sau đó em gọi tầng nghiệp vụ: `returnRequestService.processRequest(requestId, action, reason, admin.getUserId(), orderId)`. Nếu thành công, em flash message báo 'Đã duyệt yêu cầu hoàn tiền'."*

### Bước 4: Code Nghiệp vụ & Database (Business Logic)
- **Service:** Mở file `src/java/service/order/ReturnRequestService.java`, tìm hàm `processRequest()`.
  - Nói: *"Thưa thầy, đây là nghiệp vụ phức tạp nhất. Khi Admin duyệt Refund, tầng Service của em không chỉ đổi trạng thái Request thành APPROVED. Em còn gọi hàm `orderService.cancelOrder()` (hoặc cập nhật đơn) để hệ thống tự động tính toán lại số tiền đối soát của Shop Owner, và trả lại hàng tồn kho. Em gộp nhiều xử lý logic ở đây để Controller được sạch sẽ (Clean Code)."*
- **DAO:** Mở file `src/java/dao/order/ReturnRequestDAO.java`.
  - Nói: *"Tại DAO, em gọi câu lệnh SQL `UPDATE return_requests SET status = ?, admin_comment = ? WHERE request_id = ?`. Tất cả đều sử dụng `PreparedStatement` để an toàn trước tấn công SQL Injection."*

---

## Phần 3: Cách Áp dụng cho Toàn Bộ Dự án

Bây giờ bạn đã hiểu quy tắc 4 Bước. Giả sử giảng viên không bắt kiểm tra phần Refund mà hỏi phần **Giám sát Đơn hàng (Monitor Orders Global)** hay **Cấu hình phí hệ thống (System Config)**, bạn cũng cứ làm y xì đúc:

- **Bước 1:** Lên giao diện Admin, thử tìm kiếm đơn hàng hoặc đổi phí hoa hồng hệ thống.
- **Bước 2:** F12 bật Network xem gọi URL nào (`/admin/orders` hoặc `/admin/config`).
- **Bước 3:** Tìm Servlet tương ứng (`AdminOrderServlet` hoặc `AdminConfigServlet`).
- **Bước 4:** Nhảy vào Service/DAO để xem luồng lọc SQL.

Quy trình **View -> Servlet -> Service -> DAO** luôn luôn đúng. Chúc bạn làm chủ hoàn toàn Role Admin của mình!

---

## Phần 3: Cách Áp dụng cho Toàn Bộ Dự án

Bây giờ bạn đã hiểu quy tắc 4 Bước. Nếu giảng viên yêu cầu bạn review phần **Xác nhận giao hàng (Delivery Confirmation)** hay **Nhập thời gian dự kiến (Estimated Time)** của Iteration 2, bạn cứ tuân thủ đúng 4 bước đó:

- **Bước 1:** Test giao diện app (Ví dụ: Shipper bấm cập nhật giờ).
- **Bước 2:** Bật tab Network xem URL (VD: `/delivery/detail`).
- **Bước 3:** Tìm `@WebServlet("/delivery/detail")` và đọc hàm `doPost()`.
- **Bước 4:** Bấm nhảy code chui vào `DeliveryService` và `DeliveryDAO` để xem SQL UPDATE cột thời gian.

Bạn cứ tự tin làm chủ "Đường đi của Request" từ **View -> Servlet -> Service -> DAO**. Bạn sẽ không bao giờ bị quên bài hay sợ bị hỏi vặn vẹo!
