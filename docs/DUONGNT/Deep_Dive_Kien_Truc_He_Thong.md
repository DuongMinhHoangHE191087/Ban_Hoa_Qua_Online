# Bức Tranh Toàn Cảnh: Deep Dive Kiến Trúc Dự Án "Bán Hoa Quả Online"

Nếu giảng viên đã cho phép dùng AI, thì mục tiêu bây giờ của bạn không phải là "đối phó", mà là **thực sự làm chủ mã nguồn**. Để hiểu thấu đáo một dự án, bạn không nên đọc code theo kiểu nhảy từ file này sang file khác một cách vô định. Hãy bắt đầu từ **Core Business (Nghiệp vụ cốt lõi)**, sau đó đến **Database**, và cuối cùng mới là **Code**.

Tài liệu này sẽ mổ xẻ toàn bộ hệ thống từ gốc rễ.

---

## 1. Bản Chất Của Hệ Thống (Platform vs Single Shop)

Lỗi sai lớn nhất của người mới khi đọc dự án này là nghĩ rằng đây là một cửa hàng bán trái cây bình thường. Không phải! 
Hệ thống này là một **Sàn Thương Mại Điện Tử (Platform)** giống như Shopee hay Lazada thu nhỏ.

**Minh chứng cốt lõi:**
Hệ thống có role `SHOP_OWNER`. Một khách hàng (Customer) có thể thêm vào giỏ hàng 3 trái táo của Shop A và 5 trái cam của Shop B. Khi thanh toán, hệ thống phải tự động **tách đơn hàng**:
- **Parent Order (Đơn cha):** Đại diện cho 1 lần thanh toán duy nhất của Khách hàng.
- **Child Orders (Đơn con):** Mỗi Shop sẽ nhận được 1 đơn con riêng biệt để tự quản lý việc đóng gói và giao hàng.

Bạn phải luôn giữ "Mental Model" (mô hình tư duy) này trong đầu khi đọc code, đặc biệt là phần Order và Settlement.

---

## 2. Giải Phẫu Cơ Sở Dữ Liệu (Database Schema)

Hiểu Database là hiểu 70% dự án. Các bảng được liên kết với nhau theo các cụm nghiệp vụ:

### Cụm 1: Account & Auth (Tài khoản)
- **`users`**: Chứa thông tin đăng nhập của tất cả mọi người. Phân biệt quyền hạn qua cột `role` (`CUSTOMER`, `SHOP_OWNER`, `DELIVERY`, `ADMIN`).

### Cụm 2: Catalog (Sản phẩm)
- **`categories`**: Danh mục (Trái cây nội, Trái cây ngoại...).
- **`products`**: Thông tin chung của sản phẩm (Tên, Nguồn gốc, Mô tả). **Lưu ý:** Bảng này có cột `owner_id` trỏ về user để biết sản phẩm này của Shop nào.
- **`product_variants`**: Các biến thể của sản phẩm (Ví dụ: Táo size lớn 1kg, Táo size nhỏ 500g). Giá tiền và Tồn kho (`stock_quantity`) nằm ở bảng này chứ không nằm ở bảng products.

### Cụm 3: Logistics & Order (Xương sống của hệ thống - Iteration 2)
- **`orders`**: Lưu cả Parent Order và Child Order. Nếu cột `parent_order_id` là NULL thì nó là đơn cha.
- **`order_items`**: Chi tiết món hàng trong đơn. **Cực kỳ quan trọng:** Nó sao chép (snapshot) giá tiền và tên sản phẩm tại thời điểm mua để lỡ sau này Shop đổi giá thì hóa đơn cũ không bị sai lệch.
- **`deliveries`**: Liên kết 1 Đơn hàng (Child Order) với 1 Shipper (User role DELIVERY).

### Cụm 4: Tài chính & Chăm sóc khách hàng (Iteration 3)
- **`shop_settlements`**: Bảng đối soát. Sàn giữ tiền của khách, mỗi tuần/tháng sẽ tính toán lại (trừ phí hoa hồng) rồi thanh toán (settlement) cho Shop Owner.
- **`return_requests`**: Khiếu nại, đổi trả.

---

## 3. Luồng Sống Của Một Request (Request Lifecycle)

Khi khách hàng bấm 1 nút trên web, điều gì xảy ra ở dưới Java?

1. **Tomcat Server:** Tiếp nhận Request từ cổng 8080.
2. **Filters (Trạm gác):** 
   - `EncodingFilter`: Ép kiểu chữ UTF-8 để không lỗi font tiếng Việt.
   - `AuthFilter`: Lấy `session.getAttribute("currentUser")`. Nếu chưa đăng nhập mà dám vào trang `/admin`, nó "đá" văng ra `/login`.
3. **Servlet (Điều phối):** Ví dụ `AdminShopManageServlet.java`. Nó đóng vai trò là "Người điều phối giao thông", tuyệt đối không chứa các logic if-else phức tạp liên quan đến tiền bạc hay dữ liệu.
4. **Service (Nghiệp vụ):** Ví dụ `ShopService.java`. Đây là não bộ. Nó sẽ thực thi các kiểm tra: *Shop này đã bị khóa chưa? Lấy danh sách shop ra sao?*.
5. **DAO (Data Access Object):** Ví dụ `UserDAO.java`. Service gọi DAO: *"Ê, chạy cho tao câu lệnh UPDATE status xuống DB"*. DAO dùng `PreparedStatement` để thực thi và hứng lỗi `SQLException`.
6. **Servlet (Hồi đáp):** DAO trả data về cho Service -> Service trả về cho Servlet -> Servlet gói data vào Request bằng `req.setAttribute()` và đẩy (forward) sang cho file JSP.
7. **JSP (View):** Chạy code JSTL `<c:forEach>` để biến data thành HTML ném trả về cho Trình duyệt.

---

## 4. Vòng Đời Của Một Đơn Hàng (The Order Lifecycle)

Đây là luồng phức tạp và đắt giá nhất của hệ thống, bao phủ toàn bộ **Iteration 2** mà bạn đang học:

1. **Khởi tạo (Pending Payment):** Khách đặt hàng, đơn được tạo nhưng chưa thanh toán.
2. **Chờ Duyệt (Pending / Approved):** Khách thanh toán xong. Đơn nổ về màn hình của Shop Owner. Shop Owner bấm *Duyệt (Approve)* -> Hàng bị trừ khỏi tồn kho.
3. **Đóng Gói (Preparing):** Shop đang chuẩn bị trái cây vào hộp.
4. **Giao Hàng (Dispatched):** Đơn được giao cho Shipper. Bảng `deliveries` ghi nhận trạng thái và cập nhật *Thời gian dự kiến (Estimated Time)*.
5. **Hoàn Thành (Delivered):** Shipper giao xong, Khách bấm *Xác nhận*. Hệ thống khóa đơn, cộng tiền vào doanh thu dự kiến của Shop.
6. *(Ngoại lệ)* **Hủy Đơn (Cancelled):** Shop từ chối nhận đơn, hệ thống tự động hoàn lại số tồn kho (restock).

---

## 5. Cách Đọc Code Như Một Senior

Bạn đừng mở file lên đọc từ dòng 1 đến dòng 1000. Hãy đọc theo chiều dọc (Vertical Slice):

**Cách 1: Trace từ UI xuống DB**
1. Bật web lên, bấm vào tính năng "Danh sách Đơn Hàng Admin". Nhìn URL: `/admin/orders`.
2. Mở NetBeans, Ctrl+F hoặc dùng thanh Search tìm `@WebServlet("/admin/orders")`. Bạn sẽ ra file `AdminOrderServlet.java`.
3. Nhìn vào hàm `doGet()` của file đó, bạn thấy nó gọi `orderDAO.searchGlobalOrders(...)`.
4. Click chuột phải (Go to Declaration) chui vào file DAO xem câu lệnh SQL nó JOIN những bảng nào.

**Cách 2: Trace từ Database lên UI**
1. Nhìn vào bảng `return_requests`. Thấy trạng thái `PENDING`.
2. Suy luận: *"Ai đổi cái trạng thái này thành APPROVED nhỉ?"*. Mở NetBeans, tìm chữ `"APPROVED"` trong package `service.order` hoặc `dao.order`.
3. Bạn sẽ lòi ra file `ReturnRequestService.java` chứa hàm `approveReturnRequest()`.
4. Trace ngược lên xem Servlet nào gọi hàm này. Kết quả: `AdminRefundServlet.java`.

---

> **Lời kết:** Hãy giữ file hướng dẫn này làm cẩm nang. Khi bạn nắm được Bức tranh lớn (Sàn thương mại điện tử) và Bộ khung xương (Database Schema), thì những đoạn code bên trong Java Servlet hay JSP chỉ là lớp thịt đắp lên mà thôi. Bạn sẽ không bao giờ bị "ngợp" code nữa!
