# MOC: Lịch Sử Phiên Trò Chuyện (Session History)

Tài liệu này lưu trữ lại tóm tắt nội dung và các quyết định quan trọng trong phiên trò chuyện hiện tại, giúp bạn dễ dàng theo dõi lại luồng công việc đã thực hiện.

## 1. Tóm tắt vấn đề đã giải quyết
- **Vấn đề ban đầu:** Có sự nhầm lẫn về việc phân chia nhiệm vụ của DUONGNT. Ban đầu, bạn khẳng định vai trò của mình hoàn toàn là **Admin** và thắc mắc tại sao ở Iteration 2 lại xuất hiện các chức năng của **Shop Owner** và **Delivery**.
- **Quá trình điều tra:** Đã tiến hành kiểm tra lại file `task_tracking.md` và đặc biệt là file gốc `ProjectTracking - 20260607.xlsx - MetaFruit.csv` nằm trong thư mục `Task`.
- **Kết luận:** File Tracking CSV ghi nhận rõ ở cột **In Charge** (Duong):
  - **Iteration 1:** Hoàn toàn là các task của Admin (Quản lý User).
  - **Iteration 2:** Là các task của Shop Owner, Delivery và Customer (Duyệt/hủy đơn, Cập nhật giờ giao, Đánh dấu đã giao).
  - **Iteration 3:** Đánh giá sản phẩm (Review) và quay lại làm Admin với các tính năng Giám sát cấp cao (Monitor, Refund).

## 2. Các tài liệu đã được tạo & cập nhật trong phiên
Dưới đây là danh sách các tài liệu đã được chỉnh sửa để phản ánh đúng 100% công việc của bạn:

- [[00_Master_Phan_Tich_Toan_Dien_Du_An]]: File Bách Khoa Toàn Thư được viết lại từ đầu. Liệt kê toàn bộ các chức năng bạn làm từ Phase 1 đến Phase 5, ánh xạ chuẩn xác vào 3 Iteration theo file Excel, kèm theo phân tích chi tiết từng file code (Servlet, Service, DAO) mà bạn chịu trách nhiệm.
- [[00_Huong_Dan_Review_Code_Thuc_Chien]]: Kịch bản hướng dẫn review code 4 bước (Trình diễn app -> Bắt Network JSP -> Servlet -> Service & DAO), lấy tính năng Refund của Admin làm ví dụ mẫu cực kỳ chi tiết.
- [[00_Master_MOC_Ban_Hoa_Qua]]: Cập nhật thêm các liên kết mới nhất để bạn dễ dàng điều hướng.

## 3. Lời khuyên cho buổi Review Đồ Án
- Bạn là thành viên có phổ nhiệm vụ rất rộng (Cầm trịch cả Admin, hỗ trợ cả luồng Đơn hàng của Shop/Shipper ở Iteration 2). 
- Khi giảng viên hỏi về Iteration 2, hãy tự tin trả lời về luồng trạng thái đơn hàng (Approve/Reject).
- Khi giảng viên hỏi về Iteration 1 và 3, hãy thể hiện sức mạnh xử lý luồng tiền và quyền kiểm soát của Admin.
