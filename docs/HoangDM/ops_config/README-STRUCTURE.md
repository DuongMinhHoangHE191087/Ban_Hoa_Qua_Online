# 📁 FruitMkt — Cấu trúc dự án & Hướng dẫn Code

> **Java JSP/Servlet + SQL Server | Tomcat 10 | Jakarta EE 10**
> Hỗ trợ JDK 17+ (tương thích JDK 25). Source level: Java 11.

---

## 📦 Thư viện cần tải về (đặt vào `web/WEB-INF/lib/`)

| JAR | Link | Dùng cho |
|-----|------|----------|
| `mssql-jdbc-12.x.x.jre11.jar` | [Microsoft JDBC](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server) | Kết nối SQL Server |
| `jstl-3.0.x.jar` + `jakarta.servlet.jsp.jstl-api-3.0.x.jar` | [JSTL on Maven Central](https://mvnrepository.com/artifact/jakarta.servlet.jsp.jstl/jakarta.servlet.jsp.jstl-api) | JSTL tags trong JSP |
| `jackson-databind-2.x.x.jar` + deps | [Jackson](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind) | JSON (webhook, AJAX) |
| `jbcrypt-0.4.jar` | [jBCrypt](https://www.mindrot.org/projects/jBCrypt/) | Hash mật khẩu |

> **Lưu ý**: Dự án dùng `jakarta.*` (Tomcat 10+), KHÔNG phải `javax.*`.

---

## 🗂️ Sơ đồ cây thư mục

```
src/
  conf/
    context.xml                  ← JNDI DataSource config (tuỳ chọn)
  java/com/fruitmkt/
    config/
      DBConfig.java              ← ⚡ JDBC DriverManager kết nối SQL Server
      AppConfig.java             ← 🔧 Hằng số toàn hệ thống (roles, status, config)
    model/
      entity/                    ← 🗄️ 24 Java Beans ánh xạ 1:1 với bảng DB
        User.java, Order.java, Product.java, ...
      dto/                       ← 📦 5 DTOs truyền data giữa Service ↔ JSP
        PagedResultDTO.java, ProductListDTO.java, ...
    dao/
      base/BaseDAO.java          ← 🔌 Abstract class cung cấp getConnection()
      UserDAO.java               ← 📋 1 DAO per entity, chỉ chứa SQL
      ProductDAO.java, OrderDAO.java, ...
    service/                     ← 🧠 13 Services chứa business logic
      AuthService.java, ProductService.java, ...
    servlet/
      auth/                      ← LoginServlet, LogoutServlet, RegisterServlet
      guest/                     ← HomeServlet, ProductListServlet, ProductDetailServlet
      customer/                  ← CartServlet, CheckoutServlet, OrderServlet, ...
      shop/                      ← ShopDashboardServlet, ProductManageServlet, ...
      delivery/                  ← DeliveryServlet
      admin/                     ← AdminDashboardServlet, UserManageServlet, ...
      api/                       ← PaymentWebhookServlet, CartSyncServlet
    filter/
      EncodingFilter.java        ← (1) UTF-8 cho mọi request
      LoggingFilter.java         ← (2) Log method + URI + thời gian
      CsrfFilter.java            ← (3) CSRF token validation
      AuthFilter.java            ← (4) Chặn /customer/*, /shop/*, ...
      RoleFilter.java            ← (5) Kiểm tra role sau khi auth
    tag/
      CurrencyFormatTag.java     ← <ft:currency value="${price}"/>
      StarRatingTag.java         ← <ft:stars rating="${product.rating}"/>
      OrderStatusTag.java        ← <ft:orderStatus code="${order.status}"/>
      PaginationTag.java         ← <ft:pagination .../>
      PermissionTag.java         ← <ft:allow role="ADMIN">...</ft:allow>
    util/
      HashUtil.java              ← BCrypt hash & verify
      SessionUtil.java           ← Session read/write + flash messages
      ValidationUtil.java        ← Input validation (email, phone, password)
      DateUtil.java              ← LocalDateTime format/parse
      FileUploadUtil.java        ← Upload ảnh lên server filesystem
      JsonUtil.java              ← Jackson JSON serialize/deserialize
      PaginationUtil.java        ← Offset & total pages calculation

web/
  WEB-INF/
    web.xml                      ← Session config, error pages
    lib/                         ← ← ← ĐẶT JAR VÀO ĐÂY
    tld/
      fruitmkt.tld               ← Tag Library Descriptor
    jsp/
      common/                    ← header.jsp, footer.jsp, navbar.jsp, alert.jsp, error.jsp
      auth/                      ← login.jsp, register.jsp, forgot-password.jsp
      guest/                     ← home.jsp, product-list.jsp, product-detail.jsp
      customer/                  ← 10 JSPs cho khách hàng
      shop/                      ← 10 JSPs cho chủ shop
      delivery/                  ← 3 JSPs cho nhân viên giao hàng
      admin/                     ← 6 JSPs cho admin
  assets/
    css/
      main.css                   ← ✨ Design system đầy đủ (tokens, components)
      components/                ← Component-specific CSS
      pages/                     ← Page-specific CSS (load riêng trong JSP)
    js/
      main.js                    ← GuestCart, CartSync, ApiClient
      components/
        chat.js                  ← WebSocket chat client
      pages/                     ← Page-specific JS
  uploads/                       ← Upload thư mục (cần 755 permission trên Linux)
```

---

## 🏗️ Kiến trúc MVC — Luồng request

```
Browser
  ↓ HTTP Request
Filter Chain (Encoding → Logging → CSRF → Auth → Role)
  ↓
Servlet (doGet/doPost)
  ├── Đọc request params
  ├── Gọi Service
  │     └── Gọi DAO (SQL → DB)
  ├── Set request.setAttribute(data)
  └── forward → JSP (render HTML)
         └── Custom Tags (ft:currency, ft:stars...)
```

### PRG Pattern (bắt buộc cho mọi POST):
```
POST /shop/products → Servlet → Service → DAO
  ↓ (sau khi xử lý xong)
session.flashSuccess("Đã thêm sản phẩm!")
resp.sendRedirect("/shop/products")   ← Redirect (GET)
  ↓
Servlet doGet → forward → JSP
JSP → alert.jsp hiển thị flash
```

---

## ⚙️ Cấu hình DB (bước đầu tiên)

Mở `src/java/com/fruitmkt/config/DBConfig.java` và thay:

```java
private static final String DB_HOST     = "localhost";    // ← IP máy chủ SQL
private static final String DB_PORT     = "1433";         // ← Port SQL Server
private static final String DB_NAME     = "OnlineFruitShopping"; // ← Tên DB
private static final String DB_USER     = "sa";           // ← Username
private static final String DB_PASSWORD = "your_password_here";  // ← Password
```

> ⚠️ **KHÔNG commit DBConfig.java có password thật lên git!**
> Dùng `.gitignore` hoặc environment variables.

---

## 🏷️ Custom Tag Library — Cách dùng trong JSP

Khai báo đầu file JSP:
```jsp
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
```

| Tag | Cú pháp | Output |
|-----|---------|--------|
| `ft:currency` | `<ft:currency value="${price}"/>` | `150.000 đ` |
| `ft:stars` | `<ft:stars rating="${product.rating}" showValue="true"/>` | `★★★★☆ (4.2)` |
| `ft:orderStatus` | `<ft:orderStatus code="${order.status}"/>` | Badge màu |
| `ft:pagination` | `<ft:pagination current="${page}" total="${total}" baseUrl="/products"/>` | Thanh phân trang |
| `ft:allow` | `<ft:allow role="ADMIN,SHOP_OWNER">...</ft:allow>` | Ẩn/hiện theo role |

---

## 🔐 Security Checklist (bắt buộc)

- [x] **CSRF**: Form POST phải có `<input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">`
- [x] **SQL Injection**: Luôn dùng `PreparedStatement` trong DAO
- [x] **XSS**: Luôn dùng `<c:out value="..."/>` hoặc `${fn:escapeXml(...)}` khi render user data
- [x] **Password**: Luôn hash bằng `HashUtil.hash()`, không lưu plain text
- [x] **Auth Check**: Servlet check `SessionUtil.isLoggedIn()` trước khi xử lý
- [x] **Role Check**: `SessionUtil.hasRole()` hoặc dùng `RoleFilter`

---

## 📡 Session Keys — Dùng thống nhất

```java
// AppConfig.SESSION_* — không dùng magic string
session.getAttribute(AppConfig.SESSION_USER)          // → User object
session.getAttribute(AppConfig.SESSION_CSRF_TOKEN)    // → CSRF token string
session.getAttribute(AppConfig.SESSION_FLASH_MSG)     // → Flash message
session.getAttribute(AppConfig.SESSION_FLASH_TYPE)    // → "success"|"error"|"warning"
```

---

## 🛒 Guest Cart (localStorage → Server sync)

1. **Guest thêm vào giỏ**: JS gọi `GuestCart.add({ variantId, name, price, quantity })`
2. **Guest đăng nhập**: `LoginServlet` xác thực → tạo session
3. **Client sync**: JS gọi `CartSync.syncToServer()` → POST `/api/cart/sync`
4. **Server nhận**: `CartSyncServlet` → `CartService.syncGuestCart()`
5. **Xóa localStorage**: `GuestCart.clear()` sau khi sync thành công

---

## 💬 WebSocket Chat

- **Client**: `web/assets/js/components/chat.js` — `FruitChat.init({ sessionId, userId, contextPath })`
- **Server**: Cần tạo `ChatWebSocketEndpoint.java` với `@ServerEndpoint("/chat-ws")`
- **Endpoint URL**: `ws://host/ctx/chat-ws?sessionId=xxx&userId=yyy`

---

## 🗺️ Sprint Roadmap

| Sprint | Mục tiêu |
|--------|---------|
| **Sprint 0** | Cấu hình DB, implement `BaseDAO`, `DBConfig`, `User` entity + DAO + Service, Login/Register |
| **Sprint 1** | Product listing, Product detail, Category filter, Search |
| **Sprint 2** | Cart (localStorage + DB), Checkout flow, Order placement |
| **Sprint 3** | Payment (CK/COD), SePay webhook, Order status machine |
| **Sprint 4** | Shop management (product, inventory, order, promotion) |
| **Sprint 5** | Delivery management, Review system, Return request |
| **Sprint 6** | Admin panel (users, shop approval, settlement) |
| **Sprint 7** | Chat (WebSocket), Notification, Reporting |

---

## 📏 Coding Rules (tóm tắt)

### DAO
```java
// ✅ ĐÚNG — PreparedStatement
String sql = "SELECT * FROM users WHERE email = ?";
try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, email);
    try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapRow(rs) : null; }
}

// ❌ SAI — String concatenation
String sql = "SELECT * FROM users WHERE email = '" + email + "'"; // SQL INJECTION!
```

### Servlet
```java
// ✅ Sau POST thành công → PRG
SessionUtil.flashSuccess(req.getSession(), "Đặt hàng thành công!");
resp.sendRedirect(req.getContextPath() + "/orders");

// ❌ SAI — Forward sau POST (F5 sẽ submit lại)
req.getRequestDispatcher("/WEB-INF/jsp/customer/order-history.jsp").forward(req, resp);
```

### JSP
```jsp
<%-- ✅ ĐÚNG — escapeXml chống XSS --%>
<c:out value="${user.fullName}"/>

<%-- ❌ SAI — raw output --%>
${user.fullName}
```

---

*Cập nhật lần cuối: 2025-05-17 | fruitmkt-team*
