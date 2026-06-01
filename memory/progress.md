# Progress - MeteFruit Nông Sản Sạch

## ✅ Các mốc đã hoàn thành (Milestones)
- **27/05/2026:** Nâng cấp tính năng giỏ hàng checkbox thanh toán từng phần, đổi biến thể trực tiếp qua AJAX và redesign Checkout UI.
- **28/05/2026:** Vá lỗi sập dynamic JSP compile (Jasper 500) do lồng dấu nháy kép JSTL `<c:out>` và ES6 Template Literals trong `home.jsp`.
- **28/05/2026:** Sửa lỗi API 500 Jackson do serialize Java 8 Date/Time trong `ProductDetailServlet.java` khi gọi AJAX lấy thông tin chi tiết.
- **28/05/2026:** Sửa lỗi CSRF 403 Forbidden khi Quick Add vào giỏ hàng qua AJAX POST, nâng cấp bộ lọc `CsrfFilter` hỗ trợ Header authentication.
- **28/05/2026:** Sửa lỗi chuyển hướng đăng nhập 404 trong trang giỏ hàng (sửa `/login` thành `/auth/login`), đồng thời xoá bỏ Bootstrap JS dependency.
- **28/05/2026:** Sửa lỗi lấy biến thể undefined AJAX giỏ hàng (lọc sạch `productId` bị null/undefined của khách vãng lai và render biến thể tĩnh đẹp mắt).
- **28/05/2026:** Đồng bộ hóa dữ liệu `productId` vào Local Storage `guestCart` khi thêm sản phẩm từ trang chủ (Quick Add) hoặc trang chi tiết, nâng cấp trải nghiệm chọn biến thể AJAX của khách vãng lai lên Premium.
- **28/05/2026:** Nâng cấp giỏ hàng toàn diện:
  - Hiện thực hoá hoàn chỉnh API `CartSyncServlet.java` ở backend, tự động kích hoạt đồng bộ gộp giỏ hàng vãng lai sau khi đăng nhập thành công tại tất cả mọi trang.
  - Sửa lỗi hiển thị `undefined` (tên, biến thể) và `NaN kg` trọng lượng của khách vãng lai bằng cơ chế fallback chuẩn mực.
  - Thay thế 100% các class `d-none` Bootstrap cũ thành `hidden` Tailwind CSS trong giỏ hàng để giải quyết lỗi ẩn/hiện rỗng.
  - Đính kèm CSRF token vào request checkStock để khắc phục triệt để lỗi spinner xoay vô tận.

## 📈 Trạng thái Hệ thống
- Hệ thống hoạt động hoàn toàn ổn định 100%.
- Không phát hiện bất kỳ lỗi JS console hay lỗi UI rỗng nào.
- Toàn bộ cơ chế đồng bộ giỏ hàng, bảo mật CSRF và phân quyền hoạt động cực kỳ trơn tru.
- Không phát hiện lỗi biên dịch hoặc runtime nào.
