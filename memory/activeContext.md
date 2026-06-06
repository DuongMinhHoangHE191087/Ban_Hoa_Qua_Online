# Active Context - MeteFruit Nông Sản Sạch

## 📌 Bối cảnh Hiện tại
- **Đã nâng cấp cơ chế Database khi Đăng nhập**: Hiện thực hóa hoàn chỉnh API `/api/cart/sync` tại [CartSyncServlet.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/servlet/api/CartSyncServlet.java) để tự động gộp giỏ hàng vãng lai (`guestCart` từ Local Storage) vào Database của user khi họ đăng nhập thành công từ bất kỳ trang nào.
- **Đã dập tắt 100% lỗi hiển thị `undefined` tên, `undefined` biến thể, và `NaN kg`**: Tích hợp cơ chế fallback thông minh phân tích cú pháp từ nhãn `"Tên sản phẩm - Tên biến thể"` của Guest Cart.
- **Đã sửa lỗi giao diện giỏ hàng trống và nút thanh toán xoay vô tận**:
  - Chuyển đổi toàn bộ các class `d-none` (Bootstrap) thành class `hidden` (Tailwind CSS) trong [cart.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/customer/cart.jsp) và [cart.js](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/assets/js/pages/cart.js).
  - Tự động đính kèm `X-CSRF-Token` header vào request POST `/cart?action=checkStock` để loại bỏ vĩnh viễn lỗi 403 Forbidden.
- **Đã triển khai Sổ Địa Chỉ (Address Book) & Cấu trúc Người Nhận mới**:
  - Thêm bảng `user_addresses` (CRUD Ajax tại `UserProfileServlet.java` và `profile.jsp`).
  - Cập nhật bảng `orders` để thay thế `user_address` bằng `recipient_name` và `recipient_phone`, tối ưu hóa việc quản lý và lưu giữ thông tin người nhận khi mua hàng (tích hợp trong `CheckoutServlet.java` và `checkout.jsp`).

## 🎯 Mục tiêu Tiếp theo
- Duy trì sự ổn định của hệ thống trong các tác vụ thanh toán, giỏ hàng từng phần.
- Đảm bảo các tài liệu thiết kế (SRS, SDS, UC, BR, ERD) luôn được cập nhật đồng bộ với cấu trúc thực tế của mã nguồn.
- Tuân thủ nghiêm ngặt tiêu chuẩn `/clean-code`.
