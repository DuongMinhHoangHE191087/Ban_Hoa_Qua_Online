# 🚀 Báo Cáo Kết Quả Thực Hiện (Walkthrough)

Báo cáo này tóm tắt các thay đổi đã thực hiện liên quan đến validation ngày thu hoạch, tự động hủy active sản phẩm hết hạn vụ, tự động cập nhật ngày thu hoạch khi nhập kho và xử lý triệt để lỗi CSRF 403 khi thêm hàng vào kho.

---

## 🛠 Thay đổi đã thực hiện

### 1. Sửa lỗi CSRF 403 & Validation Ngày Nhập Kho tại trang Tồn kho
- **Sửa lỗi 403**: Tích hợp lấy `window.csrfToken = '${sessionScope._csrfToken}';` động từ session khi render trang [inventory.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/shop/inventory.jsp). Bổ sung Javascript submit interceptor vào `#restockForm` để tự động kiểm tra và điền token vào input hidden `_csrf` nếu bị rỗng.
- **Validate ngày nhập kho**: Thiết lập thuộc tính `max` cho input date ngày nhập kho `#changedAt` là ngày hiện tại ngay khi tải trang bằng Javascript. Phía máy chủ cũng đã được kiểm tra nghiêm ngặt trong [InventoryServlet.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/servlet/shop/InventoryServlet.java) để chặn tất cả ngày tương lai.
- **Log debug tốt hơn**: Chuyển đổi `System.out.println` trong [CsrfFilter.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/filter/CsrfFilter.java) thành `req.getServletContext().log` để đảm bảo thông tin kiểm tra token được ghi rõ ràng vào các file log của Tomcat (`localhost.log` hoặc `catalina.log`).

### 2. Tự động Cập Nhật Ngày Thu Hoạch và Active Sản Phẩm khi Nhập Kho
- **DAO**: Thêm phương thức `updateHarvestDateAndStatus` vào [ProductDAO.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/dao/ProductDAO.java) để cập nhật ngày thu hoạch (`harvest_date`) và trạng thái sản phẩm (`status`) trong cùng một Transaction Connection.
- **Service**: Thêm phương thức `getProductId` trong [ProductVariantDAO.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/dao/ProductVariantDAO.java). Đồng thời, cập nhật logic phương thức `restock()` trong [InventoryService.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/service/InventoryService.java) để trong quá trình giao dịch nhập kho, tự động lấy `productId` và gọi cập nhật ngày thu hoạch của sản phẩm thành ngày nhập kho (`changedAt`), đồng thời kích hoạt lại trạng thái sản phẩm thành `ACTIVE`.

### 3. Tự động Hủy ACTIVE (Deactive) Sản Phẩm khi Hết Hạn Vụ
- **Logic Hết Vụ**: Bổ sung phương thức `autoDeactivateExpiredProducts` vào [ProductDAO.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/dao/ProductDAO.java). Phương thức này thực hiện câu lệnh SQL tự động cập nhật status sản phẩm thành `INACTIVE` nếu quá ngày hết hạn (`harvest_date + shelf_life_days <= ngày hiện tại`). **Đặc biệt, nếu người dùng không điền hạn sử dụng hoặc điền bằng 0 (shelf_life_days <= 0), sản phẩm sẽ không bao giờ bị tự động hủy active.**
- **Tích Hợp Tự Động**: Gọi quét hết hạn `autoDeactivateExpiredProducts()` ở đầu các hàm truy vấn danh sách hoặc chi tiết sản phẩm của `ProductDAO.java` (`findById`, `findAll`, `findByOwner`, `findByCategory`, `findFlashSaleProducts`, `search`, `countSearch`).
- **Sửa Lỗi Đồng Bộ Flash Sale**: Bổ sung điều kiện lọc `p.status = 'ACTIVE'` vào hàm `findFlashSaleProducts()` để đảm bảo các sản phẩm đã bị hủy active (do hết hạn vụ) không còn hiển thị sai tại danh sách Flash Sale.

### 4. Validation Ngày Thu Hoạch khi Thêm/Sửa Sản Phẩm
- **Giao diện (JSP)**: Thiết lập giới hạn `max` cho input date Ngày thu hoạch (`#modal-harvestDate`) trong [product-list.jsp](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/web/WEB-INF/jsp/shop/product-list.jsp) là ngày hiện tại thông qua Javascript khi tải trang.
- **Máy chủ (Servlets)**: Thêm validation chặt chẽ trong cả [ProductCreateServlet.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/servlet/shop/ProductCreateServlet.java) và [ProductEditServlet.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/com/fruitmkt/servlet/shop/ProductEditServlet.java): Nếu ngày thu hoạch gửi lên lớn hơn ngày hiện tại, trả về lỗi "Ngày thu hoạch không được vượt quá ngày hiện tại".

### 5. Sửa lỗi cú pháp trong product-list.jsp & product-detail.jsp
- **product-list.jsp**: Thay thế Template Literals chứa ký tự escape backslash (`\${...}`) trong JS code bằng nối chuỗi JavaScript thông thường (`+`) để giải quyết triệt để lỗi phân tích cú pháp của IDE.
- **product-detail.jsp**:
  - Loại bỏ hoàn toàn thuộc tính style chứa EL `style="width: ${...}"` hoặc `style="${...}"` để tránh IDE CSS parser báo lỗi.
  - Chuyển sang sử dụng các thuộc tính dữ liệu HTML5 `data-percent` và `data-initial-stock`.
  - Dùng JavaScript lắng nghe sự kiện `DOMContentLoaded` để tự động tính toán và áp dụng chiều rộng `element.style.width` cho các thanh tiến trình một cách động.
  - Đồng thời cập nhật tính năng: vẽ lại thanh tiến trình tồn kho một cách trực quan khi người dùng thay đổi phân loại biến thể sản phẩm (Variant Change).
  - Chuyển `onclick="copyVoucher(this, '<c:out value="${...}"/>')"` thành `data-code="${...}" onclick="copyVoucher(this)"` để tránh xung đột dấu nháy và lỗi cú pháp.
  - Sửa `alt="<c:out value='${sp.name}'/>"` thành `alt="${sp.name}"` để tránh lồng thẻ JSTL không hợp lệ trong thuộc tính HTML.
  - Định nghĩa biến JS an toàn ở đầu block `<script>`: `const currentProductId = parseInt('${product.productId}') || 0;` thay vì nhúng trực tiếp biểu thức JSP EL vào tham số gọi hàm trong Javascript.

---

## 🧪 Kết Quả Kiểm Tra và Biên Dịch (Build)
- Đã chạy biên dịch thành công 152 lớp và atomic sync hot-reload:
  - Lệnh chạy: `powershell -ExecutionPolicy Bypass -File build-tools.ps1 reload`
  - Kết quả: Hoàn thành thành công, Tomcat reload lại ứng dụng và lưu giữ phiên làm việc ổn định, IDE không còn báo lỗi đỏ/cảnh báo vàng đối với `product-list.jsp` và `product-detail.jsp`.
