# Tổng quan nhanh
Đây là một web app bán hoa quả online bằng Java 17, theo kiểu truyền thống của Java Web: Servlet xử lý request, Service xử lý nghiệp vụ, DAO truy vấn database, và JSP render giao diện. Ứng dụng dùng Jakarta Servlet 6, JSP/JSTL, Tomcat 10, Ant/NetBeans, và database chính là SQL Server.

Luồng dễ hình dung nhất là:

Browser
  ↓ HTTP request
Filter: encoding / logging / session / CSRF / auth / role
  ↓
Servlet / Controller
  ↓
Service
  ↓
DAO
  ↓
SQL Server
  ↓
DAO trả entity / DTO
  ↓
Service xử lý logic
  ↓
Servlet setAttribute + forward JSP hoặc redirect
  ↓
JSP + CSS + JS render HTML

## 1. Cấu trúc thư mục chung
`src/java`
Đây là nơi chứa code Java chính. Các package được chia theo vai trò khá rõ:

- `config`: Cấu hình toàn cục như DB, email, session keys, role, order status
- `dao`: Truy cập database, chứa SQL
- `service`: Business logic, validate dữ liệu, gọi DAO
- `servlet`: Controller nhận request, gọi service/DAO, forward JSP hoặc redirect
- `filter`: Middleware trước khi request vào servlet: auth, role, CSRF, logging
- `model/entity`: Object đại diện bảng dữ liệu
- `model/dto`: Object gom dữ liệu để hiển thị hoặc truyền giữa lớp
- `util`: Hàm tiện ích: session, token, validation, upload, pagination
- `tag`: Custom JSP tag như định dạng tiền, rating, trạng thái đơn
- `websocket`: Chức năng real-time nếu có, ví dụ chat

Cấu trúc này được thể hiện qua danh sách package như dao/auth, dao/catalog, dao/order, service/catalog, servlet/customer, servlet/shop, servlet/admin, v.v.

## 2. Cấu trúc web frontend
`web/`
Đây là phần web root:

- `web/index.jsp`: Trang vào đầu tiên, forward sang `/home`
- `web/WEB-INF/jsp`: Chứa JSP thật sự; đặt trong WEB-INF để người dùng không truy cập trực tiếp
- `web/WEB-INF/jsp/common`: Header, navbar, footer, layout dùng chung
- `web/WEB-INF/jsp/guest`: Trang dành cho khách
- `web/WEB-INF/jsp/customer`: Trang khách hàng đã đăng nhập
- `web/WEB-INF/jsp/shop`: Trang chủ shop
- `web/WEB-INF/jsp/admin`: Trang admin
- `web/WEB-INF/jsp/delivery`: Trang nhân viên giao hàng
- `web/assets`: CSS, JS, ảnh, font
- `web/uploads`: File upload

`index.jsp` rất đơn giản: nó forward request đầu tiên đến `/home`.

## 3. Điểm vào ứng dụng: web.xml, filter, servlet
File `web/WEB-INF/web.xml` khai báo app là Jakarta Servlet 6.0 và đặt tên ứng dụng là Ban Hoa Qua Online.

Nó cũng cấu hình:
- Trang lỗi 403, 404, 500 trỏ tới JSP trong WEB-INF.
- Session timeout là 60 phút.
- Welcome file là index.jsp.
- Các filter quan trọng như EncodingFilter, LoggingFilter, SessionRestoreFilter, CsrfFilter, AuthFilter, RoleFilter.

Điểm rất quan trọng: thứ tự filter mapping có ý nghĩa. File này ghi rõ filter được map theo thứ tự thực thi.
Ví dụ, AuthFilter được áp dụng cho các khu vực cần đăng nhập như `/customer/*`, `/shop/*`, `/delivery/*`, `/admin/*`, `/checkout`, `/orders`, `/returns`, `/chat`.
RoleFilter tiếp tục kiểm tra quyền theo vai trò cho các khu vực tương tự.

## 4. Kiến trúc tầng: Servlet → Service → DAO → Database
### 4.1 Servlet / Controller
Servlet là nơi nhận HTTP request. Ví dụ `HomeServlet` được map với URL `/home`.
Trong `doGet`, servlet:
- Đọc parameter như keyword, categoryId.
- Parse và validate nhẹ dữ liệu request.
- Gọi DAO để lấy categories, sản phẩm flash sale, best seller, seasonal, organic, imported.
- Set dữ liệu vào request bằng `req.setAttribute`.
- Forward sang JSP trong WEB-INF.

Ví dụ `HomeServlet` đọc keyword và categoryId từ request. Nó lấy danh mục và sản phẩm từ database. Sau đó gán dữ liệu vào request scope. Cuối cùng forward tới `/WEB-INF/jsp/guest/home.jsp`, tức là JSP không được truy cập trực tiếp từ URL ngoài.

*Ghi chú cho người mới:* trong code lý tưởng, servlet nên gọi Service, không nên gọi DAO trực tiếp. Một số phần trong project đã theo pattern Service, nhưng một số servlet như `HomeServlet` vẫn gọi DAO trực tiếp. Khi sửa code mới, nên ưu tiên pattern Servlet → Service → DAO.

### 4.2 Service
Service chứa nghiệp vụ và validate. Ví dụ `ProductService` ghi rõ trách nhiệm là validate input, áp dụng business rule, rồi delegate xuống DAO; không viết SQL và không tương tác trực tiếp với HttpRequest / HttpResponse.

Ví dụ khi lấy danh sách sản phẩm, service:
- Đảm bảo page không nhỏ hơn 1.
- Lấy page size từ config.
- Tính tổng số trang.
- Gọi DAO để search.
- Trả về `PagedResultDTO`.

Khi tạo sản phẩm, service validate object, set status mặc định là ACTIVE, rồi gọi DAO save.
Khi duyệt sản phẩm, service kiểm tra productId, categoryId, cập nhật approval status, rồi đảm bảo sản phẩm được bật ACTIVE.

### 4.3 DAO
DAO là nơi chứa SQL. `BaseDAO` nói rất rõ quy tắc của team:
- Chỉ viết SQL trong DAO.
- Luôn dùng PreparedStatement.
- Luôn dùng try-with-resources.
- Ném SQLException lên Service.
- Đặt tên method theo pattern như `findById`, `findAll`, `save`, `update`, `delete`.

`BaseDAO` cũng load SQL Server JDBC driver và cung cấp getConnection() từ ConnectionPool.
`ProductDAO` cũng nhắc lại quy tắc: chỉ chứa SQL, không chứa business logic, dùng PreparedStatement, ném SQLException, dùng try-with-resources.

Ví dụ method `findById` dùng SQL có parameter `?`, set bằng `ps.setInt`, rồi map `ResultSet` thành Product.
Method search dùng `StringBuilder` để build query động, nhưng vẫn truyền giá trị bằng parameter list và `PreparedStatement`, tránh nối trực tiếp input người dùng vào SQL.

## 5. JSP render giao diện
Các JSP nằm trong `web/WEB-INF/jsp`. Ví dụ trang home khai báo content type UTF-8 và dùng JSTL taglib `c`, `fmt`, `fn`, cùng custom tag `ft`.
Trang home include header dùng chung bằng `<jsp:include>`.
Nó cũng load CSS/JS static qua `${pageContext.request.contextPath}` để path đúng dù app deploy dưới context nào.

Điểm nên nhớ với JSP:
- JSP chỉ nên hiển thị dữ liệu.
- Không nên viết SQL trong JSP.
- Không nên nhồi business logic phức tạp vào JSP.
- Servlet/Service chuẩn bị dữ liệu bằng `request.setAttribute`, JSP dùng JSTL để render.

## 6. Database và domain chính
Database nằm trong `database/Schema.sql` và file này nói rõ nó là tài liệu tham khảo schema; file setup/seed chính là `Setup_OnlineFruitShopping.sql`.

Một số bảng quan trọng:
- `users`: Bảng users lưu tài khoản, email, password hash, phone, role, status, avatar, thông tin xác minh email, lock account. Role gồm CUSTOMER, SHOP_OWNER, DELIVERY, ADMIN.
- `products`: Bảng products thuộc về một owner_id, có category, name, description, origin, status, rating, sold quantity, organic/imported flags, mùa vụ, approval status.
- `product_images` và `product_variants`: Ảnh sản phẩm nằm ở `product_images`. Biến thể sản phẩm nằm ở `product_variants`, có SKU, label, price, stock quantity, weight, discount info.
- `orders` và `order_items`: Bảng orders lưu đơn hàng, customer, owner shop, parent/child order, địa chỉ giao hàng, trạng thái, total amount, payment method, refund status.
- Bảng `order_items` lưu snapshot tên sản phẩm, label biến thể, số lượng, giá tại thời điểm đặt hàng, subtotal, packaging snapshot. Snapshot rất quan trọng: nếu sau này shop đổi tên sản phẩm hoặc giá, đơn hàng cũ vẫn giữ đúng thông tin lúc mua.
- `return_requests`: Bảng return_requests lưu yêu cầu hủy/trả/đổi, lý do, bằng chứng, số lượng, hướng xử lý, refund amount, status, người duyệt.
- `shop_settlements`: Bảng shop_settlements lưu đối soát shop: gross amount, platform fee, refund amount, adjustment amount, net amount, status.
- `deliveries`: Bảng deliveries lưu giao hàng, order, trip, staff, status, timestamps, failure reason, proof image.

## 7. Config quan trọng
`AppConfig` là nơi chứa hằng số toàn cục. File này nói đây là nơi duy nhất chứa magic number và string literals dùng chung.

Một số nhóm config chính:
- Database host, port, name, user, password, JDBC URL.
- Google OAuth config.
- Email SMTP và secret/token config.
- App name, support email, base URL.
- Pagination constants.
- Upload limits và allowed extensions.
- Session keys như current user, flash message, CSRF token.
- Role constants như CUSTOMER, SHOP_OWNER, DELIVERY, ADMIN.
- Order status constants.
- Delivery status constants.
- Return request constants.

Lưu ý bảo mật cho người mới: một số default secret/password đang nằm trong code. Khi deploy thật, nên dùng biến môi trường và không commit credential thật.

## 8. Authentication và Authorization
`AuthFilter` chặn các URL yêu cầu đăng nhập như `/customer/*`, `/shop/*`, `/delivery/*`, `/admin/*`.

Luồng filter:
- Nếu session hiện tại đã login thì cho qua.
- Nếu chưa login, thử kiểm tra accessToken trong cookie.
- Nếu access token hết hạn/không có, thử refreshToken.
- Nếu vẫn không xác thực được:
  - AJAX/JSON thì trả JSON lỗi 401.
  - Request thường thì redirect về `/auth/login?redirect=...`.

Điểm cần học kỹ ở đây:
- Session-based auth.
- Token-based remember/restore session.
- Cookie security.
- Role-based access control.
- CSRF protection.

## 9. Các actor chính trong hệ thống
Dựa vào schema và cấu trúc servlet/JSP, app có các nhóm người dùng chính:

- **Guest**: Xem trang chủ. Xem danh sách/chị tiết sản phẩm. Có thể dùng cart guest theo session/local storage tùy luồng.
- **Customer**: Đăng ký, đăng nhập. Quản lý giỏ hàng. Checkout. Xem đơn hàng. Đánh giá. Tạo yêu cầu trả/hủy/đổi.
- **Shop Owner**: Quản lý shop. Quản lý sản phẩm. Quản lý tồn kho. Xử lý đơn hàng. Khuyến mãi. Báo cáo. Đối soát.
- **Delivery**: Xem dashboard giao hàng. Xem chi tiết delivery. Cập nhật trạng thái giao hàng.
- **Admin**: Quản lý user. Duyệt shop. Duyệt/quản lý sản phẩm. Giám sát đơn. Quản lý refund. Quản lý settlement. Cấu hình hệ thống.

Role trong database khớp với các actor này: CUSTOMER, SHOP_OWNER, DELIVERY, ADMIN.

## 10. Những điều quan trọng nhất cần biết khi đọc/sửa code
### 10.1 Luôn tìm servlet theo URL trước
Nếu bạn muốn biết `/shop/products` làm gì, hãy tìm Servlet thường là điểm bắt đầu tốt nhất.
### 10.2 JSP nằm sau WEB-INF, không gọi trực tiếp bằng URL
Bạn không vào trực tiếp: `/WEB-INF/jsp/guest/home.jsp`
Mà request đi qua servlet `/home`, servlet forward vào JSP. `HomeServlet` forward đến JSP sau khi chuẩn bị dữ liệu.
### 10.3 SQL chỉ nên ở DAO
Đây là rule quan trọng nhất khi sửa persistence logic. `BaseDAO` ghi rõ SQL chỉ nằm trong DAO, không ở Service hay Servlet.
### 10.4 Dùng PreparedStatement
DAO phải dùng parameterized query để tránh SQL injection. `ProductDAO.findById` là ví dụ đơn giản nhất với `WHERE product_id = ?` và `ps.setInt(1, id)`.
### 10.5 Service là nơi validate business logic
Ví dụ `ProductService.getProductById` reject productId <= 0.
`ProductService.toggleStatus` chỉ cho phép ACTIVE hoặc INACTIVE.
### 10.6 Status phải khớp schema
Ví dụ bảng orders có CHECK constraint cho status như PENDING_PAYMENT, APPROVED, CONFIRMED, PREPARING, DISPATCHED, DELIVERED, v.v.
Nếu thêm status mới trong Java mà không sửa schema, database có thể reject insert/update.
### 10.7 Cẩn thận với domain rule “một order thuộc một shop owner”
Trong bảng orders, có owner_id và parent_order_id, cho thấy hệ thống có thể tách đơn cha/con theo shop.
Khi sửa checkout/order, phải giữ rule: mỗi order con thuộc một shop owner.
### 10.8 PRG sau POST
Project có rule nên giữ Post/Redirect/Get sau POST. Nghĩa là sau khi submit form thành công, servlet nên sendRedirect thay vì forward trực tiếp, để tránh refresh browser gửi lại form.

## 11. Nên học tiếp theo theo thứ tự nào?
**Giai đoạn 1: Nền tảng Java Web**
HTTP cơ bản, GET vs POST, request parameter, response redirect, status code, cookie/session, Servlet, HttpServlet, doGet, doPost, @WebServlet, RequestDispatcher.forward, sendRedirect, JSP/JSTL, ${...} Expression Language, <c:forEach>, <c:if>, <fmt:formatNumber>, include header/footer, form binding đơn giản.

**Giai đoạn 2: Kiến trúc project**
MVC trong Java Web, Servlet = Controller, Service = Business logic, DAO = Data access, JSP = View, Layered architecture.
Không gọi DB trong JSP. Không viết SQL trong Service. Không nhồi quá nhiều logic vào Servlet.
DTO vs Entity.

**Giai đoạn 3: Database và SQL Server**
SELECT, JOIN, WHERE, ORDER BY, INSERT, UPDATE, transaction, Foreign key, check constraint, Pagination bằng OFFSET FETCH, PreparedStatement và SQL injection, Mapping ResultSet sang object Java.

**Giai đoạn 4: Authentication / Authorization**
Session login, Cookie, Access token / refresh token, CSRF token, Role filter, Password hashing.

**Giai đoạn 5: Domain bán hàng**
Nên đọc kỹ các flow: Product listing / product detail, Cart, Checkout, Order parent/child theo shop, Payment COD/CK, Delivery, Return/refund, Settlement cho shop. Đặc biệt nên học kỹ các bảng orders, order_items, return_requests, shop_settlements, deliveries, vì đây là phần dễ lỗi nghiệp vụ nhất.

## 12. Cách đọc một chức năng cụ thể
Ví dụ bạn muốn hiểu chức năng “trang chủ”:
- Bắt đầu từ `web/index.jsp`: thấy forward sang `/home`.
- Tìm servlet `@WebServlet("/home")`: đó là `HomeServlet`.
- Đọc `doGet`: thấy lấy keyword, category, page, gọi DAO lấy sản phẩm.
- Xem servlet set attribute nào: categories, flashSaleProducts, normalProducts, currentPage, v.v.
- Xem JSP nhận dữ liệu: `web/WEB-INF/jsp/guest/home.jsp`.
- Nếu muốn biết SQL, đi vào `ProductDAO` hoặc `CategoryDAO`.
- Nếu muốn biết bảng, mở `database/Schema.sql`.

## 13. Mental model ngắn gọn cho người mới
Hãy nhớ 5 câu này:
1. URL vào servlet, không vào JSP trực tiếp.
2. Servlet nhận request và chuẩn bị dữ liệu.
3. Service xử lý luật nghiệp vụ và validate.
4. DAO là nơi duy nhất nên viết SQL.
5. JSP chỉ render dữ liệu thành HTML.
Nếu sửa code mà giữ được 5 điều này, bạn sẽ ít làm hỏng kiến trúc hơn.
