# 📁 MetaFruit — Nền Tảng Thương Mại Điện Tử Hoa Quả Online Toàn Diện

[![Tech Stack](https://img.shields.io/badge/Tech%20Stack-Java%2017%2F25%20%7C%20Tomcat%2010%20%7C%20SQL%20Server-orange.svg?style=for-the-badge)](file:///README.md)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10%20(Servlet%206%2C%20JSP%203)-blue.svg?style=for-the-badge)](file:///README.md)
[![Security](https://img.shields.io/badge/Security-BCrypt%20%7C%20CSRF%20%7C%20RBAC%20%7C%20PRG-green.svg?style=for-the-badge)](file:///README.md)

Chào mừng đến với **MetaFruit** — Hệ thống thương mại điện tử hoa quả trực tuyến đa đối tượng chuẩn doanh nghiệp. Ứng dụng được xây dựng theo kiến trúc MVC phân lớp sâu (Layered Servlet-Service-DAO), tối ưu hóa trải nghiệm mượt mà, tích hợp thanh toán tự động VietQR (SePay Webhook) và bảo mật nhiều lớp.

---

## 🗺️ MỤC LỤC TÀI LIỆU
1. [🏗️ Kiến Trúc Hệ Thống & Luồng Request](#-kiến-trúc-hệ-thống--luồng-request)
2. [🛡️ Các Giải Pháp Bảo Mật & Tiêu Chuẩn Lập Trình](#%EF%B8%8F-các-giải-pháp-bảo-mật--tiêu-chuẩn-lập-trình)
3. [✨ Chi Tiết Tính Năng Nổi Bật & Hoạt Động Nghiệp Vụ](#-chi-tiết-tính-năng-nổi-bật--hoạt-động-nghiệp-vụ)
4. [⚙️ Hướng Dẫn Cài Đặt & Khởi Chạy](#%EF%B8%8F-hướng-dẫn-cài-đặt--khởi-chạy)

---

## 🏗️ KIẾN TRÚC HỆ THỐNG & LUỒNG REQUEST

Dự án áp dụng mô hình **MVC (Model-View-Controller)** với 4 lớp độc lập:

```text
[Client / Browser] 
       │
       ▼ (HTTP Request)
[Filter Chain] ────────► EncodingFilter ──► SecurityAuthFilter ──► CsrfFilter
       │
       ▼ (Request Validated)
[Servlet Controller] ──► Tiếp nhận Params & Validate DTO
       │
       ▼ (Business Logic)
[Service Layer] ───────► Logic tính toán tiền, kho hàng, thanh toán VietQR, OAuth
       │
       ▼ (SQL Executions)
[DAO Layer] ───────────► JDBC Connection Pooling, PreparedStatement (Zero SQLi)
       │
       ▼ (Query / Mutate)
[SQL Server CSDL]
```

### Phân Lớp Chi Tiết:
*   **View (JSP/JSTL & Tailwind CSS)**: Đặt bảo mật trong `web/WEB-INF/jsp` (chặn truy cập trực tiếp URL). Sử dụng Custom Tags (`ft:currency`, `ft:stars`, `ft:orderStatus`, `ft:allow`) triệt tiêu mã Java scriptlets.
*   **Controller (Jakarta Servlet 6.0)**: Áp dụng nghiêm ngặt mô hình **PRG (Post-Redirect-Get)** chống gửi lặp Form (F5 lặp đơn hàng).
*   **Service Layer**: Chứa 100% logic nghiệp vụ: tính giá biến thể, kiểm tra số lượng tồn kho thực tế, thanh toán VietQR, gửi mail thông báo.
*   **DAO Layer**: Chứa duy nhất các câu lệnh Parameterized SQL. Sử dụng `PreparedStatement` và `try-with-resources` tự động đóng kết nối.

---

## 🛡️ CÁC GIẢI PHÁP BẢO MẬT & TIÊU CHUẨN LẬP TRÌNH

1. **Mã Hóa Mật Khẩu (BCrypt Hashing)**:
   - Sử dụng thư viện `jbcrypt` băm mật khẩu kèm Salt ngẫu nhiên trước khi lưu CSDL. Không bao giờ lưu Plain-text Password.
2. **Bảo Chống SQL Injection (100% Parameterized SQL)**:
   - Toàn bộ truy vấn CSDL trong DAO Layer sử dụng `PreparedStatement`. Tuyệt đối không nối chuỗi SQL.
3. **Phân Quyền Chi Tiết (Role-Based Access Control - RBAC)**:
   - Bộ lọc `RoleFilter` và `SecurityAuthFilter` kiểm tra Session và phân quyền đúng 5 vai trò (Guest, Customer, Shop Owner, Delivery, Admin).
4. **Phòng Chống CSRF (Cross-Site Request Forgery)**:
   - Tự động sinh `CSRF_TOKEN` ngẫu nhiên lưu trong Session, bắt buộc xác thực ở tất cả các HTTP POST Request thao tác dữ liệu.
5. **Chống XSS (Cross-Site Scripting)**:
   - Làm sạch toàn bộ dữ liệu người dùng nhập bằng `ValidationUtil` và encode HTML output bằng JSTL `<c:out>`.
6. **Bảo Vệ Luồng Giao Dịch (PRG Pattern & Transactional Rollback)**:
   - Xử lý đơn hàng đa cửa hàng (Multi-Shop Checkout) trong cùng 1 Database Transaction (`connection.setAutoCommit(false)`), tự động `rollback()` nếu có bất kỳ sản phẩm nào hết hàng.

---

## ✨ CHI TIẾT TÍNH NĂNG NỔI BẬT & HOẠT ĐỘNG NGHIỆP VỤ

### 1. Đăng Nhập & Đăng Ký (Auth & Account Management)
*   **Đăng Ký Tài Khoản**: Khách hàng nhập thông tin -> Hệ thống validate email/sđt -> Băm mật khẩu BCrypt -> Gửi Email xác thực tài khoản qua OTP / Link kích hoạt.
*   **Đăng Nhập**: Xác thực BCrypt Hash. Hỗ trợ **Google OAuth 2.0 Đăng nhập 1-touch**.
*   **Quên / Đổi Mật Khẩu**: Sinh Token xác thực dùng 1 lần (Single-use Token) có thời hạn 15 phút gửi qua Email.

### 2. Đăng Ký & Quản Lý Cửa Hàng (Shop Registration & Merchant Approval)
*   **Đăng Ký Mở Shop**: Người dùng gửi yêu cầu đăng ký Gian hàng hoa quả (Tên Shop, Giấy phép kinh doanh, Địa chỉ, Mã ngân hàng chốt ví).
*   **Quy Trình Xét Duyệt**: Admin kiểm tra hồ sơ -> Nhấn **Duyệt Shop** -> Hệ thống nâng cấp quyền người dùng thành `Shop Owner` và tạo Ví chốt tiền.

### 3. Thêm, Sửa & Tồn Kho Sản Phẩm (Inventory & Product Management)
*   **Thêm / Sửa Sản Phẩm**:
    - Chủ Shop tải ảnh hoa quả lên server (`FileUploadUtil`), thiết lập danh mục, mô tả sản phẩm.
    - Cấu hình **Biến thể trọng lượng** linh hoạt (ví dụ: Hộp 500g, Khay 1kg, Thùng 5kg) kèm giá tiền và tồn kho riêng cho từng biến thể.
*   **Cảnh Báo & Tự Động Quản Lý Tồn Kho**:
    - Hệ thống cảnh báo đỏ In-App cho Chủ shop khi tồn kho biến thể `< 5`.
    - Khi Khách mua hàng thành công -> Tồn kho tự động trừ đi theo Real-time Transaction.
    - Nếu Chủ shop / Khách hủy đơn -> Tồn kho tự động hoàn lại ngay lập tức.

### 4. Luồng Nghiệp Vụ Mua Hàng & Thanh Toán (Cart, Checkout & VietQR)
*   **Giỏ Hàng Động (Dynamic Cart)**: Khách vãng lai tích trữ hàng qua `localStorage`. Ngay sau khi đăng nhập -> Giỏ hàng tự động đồng bộ lên CSDL SQL Server.
*   **Guest Checkout (Đặt hàng không cần tài khoản)**: Tự động sinh tài khoản ảo và gửi thông tin truy cập qua Email cho khách.
*   **Thanh Toán VietQR Tự Động (SePay Integration)**:
    - Sinh mã **Dynamic VietQR** chứa số tiền chính xác và Mã đơn hàng.
    - Ngân hàng báo Webhook về Server -> Hệ thống tự động chuyển trạng thái đơn hàng sang **Đã Thanh Toán** trong 3 giây.
*   **Theo Dõi Đơn Hàng & Đổi Trả**:
    - Khách hàng xem lộ trình giao vận theo real-time.
    - Hỗ trợ gửi yêu cầu Đổi Trả / Hoàn Tiền trong vòng 7 ngày kèm hình ảnh sản phẩm dập nát.

### 5. Giao Hàng & Kết Toán Doanh Thu (Delivery & Daily Settlement)
*   **Phân Hệ Delivery Staff**: Shipper xem danh sách đơn hàng được duyệt trong khu vực -> Bấm Nhận đơn -> Cập nhật trạng thái (Đang giao / Đã giao / Thu tiền COD).
*   **Chốt Ví Tự Động (Daily Settlement Batch Job)**:
    - Hệ thống tự động chạy vào `00:00` mỗi ngày -> Quét các đơn hoàn thành trên 7 ngày -> Tự động chuyển tiền về Ví doanh thu của Chủ shop.

---

## ⚙️ HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY

### 1. Yêu Cầu Môi Trường
*   **JDK**: Java 17 trở lên.
*   **Web Server**: Apache Tomcat 10.1.x (Jakarta Servlet 6.0).
*   **Database**: Microsoft SQL Server 2019 / 2022.
*   **IDE Khuyên Dùng**: NetBeans 21+ hoặc IntelliJ IDEA / VS Code.

### 2. Các Bước Khởi Chạy Dự Án
1. **Khởi Tạo CSDL**: Run tệp SQL `database/Setup_OnlineFruitShopping.sql` trong SQL Server Management Studio (SSMS).
2. **Cấu Hình DB Connection**: Đặt biến môi trường hoặc cập nhật file `src/java/config/DBConfig.java` với `DB_USER` và `DB_PASSWORD`.
3. **Build & Deploy**: Mở dự án trong NetBeans -> Nhấn `Clean and Build` -> Nhấn `Run` để deploy lên Tomcat Server tại `http://localhost:8080/Ban_Hoa_Qua_Online`.

---
*MetaFruit — Nền Tảng Mua Bán Hoa Quả Trực Tuyến An Toàn - Tiện Lợi - Hiện Đại.*
