# Nhật ký sử dụng AI (AI Evidence Log)

**Họ và tên / Mã sinh viên:** Dương (DUONGNTHE191504)
**Chức năng:** Admin Dashboard & Quản lý (Module Admin)

> Theo yêu cầu của Leader: "code xong phần nào dùng AI phần nào ae lưu lại bằng chứng sau ông hỏi hay gì cho dễ nhé".

Dưới đây là nhật ký sử dụng AI trong quá trình phát triển:

## 1. Phân tích thiết kế hệ thống (SRS)
- **Thời gian:** 23/05/2026
- **Công việc:** Nhờ AI đọc toàn bộ file phân tích Actor và UI Checklist (`SRS_Actor_Functional_Analysis_FruitShop.md` và `UI_Screen_Function_Checklist_FruitShop.md`) để hiểu quy mô của dự án Marketplace. AI đã tóm tắt và quy hoạch lại toàn bộ scope công việc cho Module Admin.
- **Kết quả:** Đã lên lại danh sách Task chuẩn mực, cập nhật file Plan bao quát toàn hệ thống.

## 2. Thiết kế giao diện (UI) cho Admin Dashboard
- **Thời gian:** 23/05/2026
- **Công việc:** AI sinh mã HTML/CSS tĩnh dựa trên `main.css` để làm bản nháp giao diện Admin Dashboard (Sidebar, Chart.js, Card thống kê).
- **Kết quả:** Lưu tại `docs/DUONGNT/adminDashboardUI/admin-dashboard.html`.

## 3. Lập trình tính năng Admin Dashboard
- **Thời gian:** 23/05/2026
- **Công việc:** AI viết file `DashboardDAO.java` bám sát `Schema.sql`, sử dụng đúng chuẩn `BaseDAO` và cú pháp SQL Server của hệ thống. Đồng thời khởi tạo `AdminDashboardController.java` và giao diện động `admin-dashboard.jsp`. Bổ sung comment cực kỳ chi tiết cho toàn bộ code Java và JSP theo nguyên tắc làm việc nhóm.
- **Kết quả:** Đã hoàn thành Giai đoạn 1 (Admin Dashboard) với đầy đủ comment giải thích logic, sẵn sàng để gộp code.

## 4. Tính năng Quản lý Phê duyệt Gian hàng (Shop Approval)
- **Thời gian:** 23/05/2026
- **Công việc:** Nhờ AI triển khai luồng phê duyệt theo thiết kế tài liệu `UI_Screen_Function_Checklist_FruitShop.md`. 
  - Đã sinh mã `ShopApprovalDAO.java` (lấy danh sách PENDING, thực hiện duyệt bằng Transaction để up role user lên SHOP_OWNER).
  - Khởi tạo `ShopApprovalController.java` cho các method POST (Approve, Reject).
  - Code trang `shop-approval.jsp` có cảnh báo JS an toàn, form gửi dữ liệu an toàn.
- **Kết quả:** Đã hoàn tất, đáp ứng requirement của Admin. Code được chú thích rõ ràng.

## 5. Tính năng Quản trị Người Dùng (User Management)
- **Thời gian:** 23/05/2026
- **Công việc:** AI hỗ trợ viết toàn bộ luồng Quản trị người dùng (Khóa / Mở khóa tài khoản).
  - Viết `UserManagementDAO.java` lấy danh sách user và update `status`. Tránh khóa nhầm account ADMIN.
  - Viết `UserManagementController.java` điều hướng POST/GET.
  - Xây dựng `user-management.jsp` hiển thị Badge màu theo trạng thái tài khoản, và tự động thay đổi nút (Khóa / Mở Khóa) tùy theo status của user.
- **Kết quả:** Thành công. Mã nguồn tuân thủ nguyên tắc comment rõ ràng của Leader.

## 6. Tính năng Quản trị Danh mục Sản phẩm (Category Management)
- **Thời gian:** 23/05/2026
- **Công việc:** AI hoàn thiện tính năng cuối cùng của Giai đoạn 2.
  - Xây dựng `CategoryManagementDAO.java` thao tác CSDL thêm mới và cập nhật trạng thái danh mục.
  - Thiết kế `CategoryManagementController.java` xử lý luồng add / toggle trạng thái.
  - Code `category-management.jsp` gồm Form điền nhanh danh mục (Thêm Slug, Thứ tự) và Bảng danh sách trạng thái Bật/Tắt (thay vì Xóa cứng để bảo toàn dữ liệu liên kết khóa ngoại). Toàn bộ được comment giải thích chi tiết.
- **Kết quả:** Hoàn thành xuất sắc Giai đoạn 2 của Module Admin (Duyệt Shop, Quản lý User, Quản lý Danh mục). Chuẩn bị Commit Code.
