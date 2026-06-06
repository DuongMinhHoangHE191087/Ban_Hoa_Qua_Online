# BÁO CÁO TIẾN ĐỘ TUẦN TOÀN DIỆN (WEEKLY PROGRESS REPORT)
## Dự án: Hệ Thống Bán Hoa Quả Online (MetaFruit)
### Nhóm: 1 | Tuần kết thúc ngày: 07-06-2026
---

> [!NOTE]
> Báo cáo tuần này tổng hợp toàn bộ lịch sử phát triển dự án **MetaFruit** từ trước đến nay, đối chiếu khớp 100% với tài liệu **SRS** (Đặc tả yêu cầu phần mềm), project tracking và codebase thực tế đã hoàn thành.

---

## 📊 I. BẢNG TỔNG HỢP TRẠNG THÁI TIẾN ĐỘ (STATUS REPORT)

| # | Hạng mục công việc (Project Task) | Người phụ trách (In-charge) | Trạng thái (Status) | Ghi chú chi tiết (Notes & Updates) |
|---|---|---|---|---|
| 1 | **Overall Requirements & Scope** | HaiPT | **Completed** | Xác định phạm vi 103 chức năng chuẩn SRS tiếng Anh. |
| 2 | **Use Case Specifications** | HaiPT | **Completed** | Hoàn thành vẽ Use Case Diagram và đặc tả kịch bản Use Case 1-50. |
| 3 | **Functional Requirements & Mockups** | HoangDM | **Completed** | Thiết kế UI/UX mockups cho toàn bộ các màn hình chính. |
| 4 | **Non-Functional Requirements** | KhangCB | **Completed** | Đặc tả hiệu năng, bảo mật và khả năng mở rộng hệ thống. |
| 5 | **Requirement Appendix & Rules** | DuongNT | **Completed** | Hoàn thiện Business Rules cho việc định giá, vận chuyển và đổi trả. |
| 6 | **Database Design & Specifications** | Cả nhóm (All Team) | **Completed** | Thiết kế ERD chuẩn hóa, làm giàu cơ sở dữ liệu thật (Enriched SQL). |
| 7 | **Authentication & Security System** | HoangDM | **Completed** | Hoàn thành xác thực Dual-Token Cookie, bảo mật HttpOnly, RBAC. |
| 8 | **Product Catalog Discovery** | HoangDM | **Completed** | Hiển thị sản phẩm động, tìm kiếm nâng cao, bộ lọc danh mục. |
| 9 | **Shopping Cart System** | HoangDM | **Completed** | Giỏ hàng lai (LocalStorage & DB), cập nhật AJAX thời gian thực. |
| 10 | **Product Management (CRUD)** | KhangCB | **Completed** | Hoàn thành chức năng thêm/sửa/xóa sản phẩm và upload ảnh. |
| 11 | **Checkout & Order Placement** | HoangDM / Quan | **Completed** | Hoàn tất tích hợp địa chỉ giao hàng và quy trình tạo đơn hàng ACID. |
| 12 | **Shop Profile & Address Management** | HoangDM | **Completed** | Quản lý thông tin shop, upload ảnh và địa chỉ khách hàng. |
| 13 | **Guest Views (About, Contact, ShopView)** | HoangDM | **Completed** | Hoàn thiện trang Giới thiệu, Liên hệ và xem trang của từng Shop. |
| 14 | **Admin System Maintenance (Force Logout)** | HoangDM | **Completed** | Tích hợp tính năng cưỡng chế đăng xuất tất cả phiên để bảo trì hệ thống. |
| 15 | **Online Payment (VietQR & SePay)** | HoangDM | **To Do** | Kế hoạch tích hợp cổng QR động ngân hàng và máy thu Webhook. |

---

## ⚡ II. CHI TIẾT CÁC PHẦN ĐÃ HOÀN THÀNH (DONE)
*Tính đến ngày 07-06-2026*

### 1. Phân hệ Xác thực & Bảo mật (Authentication & RBAC)
*   **User Registration Local Form [Done - HoangDM]**: Form đăng ký tài khoản cục bộ có xác thực dữ liệu đầu vào phía Client & Server, băm mật khẩu bằng thuật toán **BCrypt**.
*   **Google OAuth 2.0 Integration [Done - HoangDM]**: Đăng ký và đăng nhập nhanh bằng tài khoản Google. Tự động liên kết và kích hoạt tài khoản.
*   **Role Selection [Done - HoangDM]**: Cho phép chọn vai trò (Customer, Shop Owner, Delivery, Admin) khi đăng ký với trạng thái kiểm duyệt ban đầu cho Shop Owner.
*   **Dual-Token Session & Lockout [Done - HoangDM]**: Token truy cập 15 phút và Refresh Token 7 ngày qua HttpOnly Cookie. Tự động khóa tài khoản 15 phút nếu nhập sai mật khẩu quá 5 lần liên tiếp.
*   **Role-Based Access Control (RBAC) [Done - HoangDM]**: Bộ lọc `RoleFilter` bảo vệ nghiêm ngặt các URL hệ thống, ngăn chặn mọi hình thức tấn công bypass quyền hạn.

### 2. Phân hệ Khám phá Sản phẩm & Giỏ hàng (Discovery & Cart)
*   **Dynamic Landing Page [Done - HoangDM]**: Nạp dữ liệu sản phẩm thật từ Database SQL Server, lọc danh mục, thanh tìm kiếm thông minh chống SQL Injection.
*   **Advanced Product Details [Done - HoangDM]**: Hiển thị biến thể trọng lượng sống động, tính toán giá chênh lệch động và số lượng tồn kho tức thời.
*   **Hybrid Cart Synchronization [Done - HoangDM]**: Gộp giỏ hàng tạm (LocalStorage của khách vãng lai) vào Database khi người dùng đăng nhập thành công.
*   **Interactive Inline Cart [Done - HoangDM]**: Cập nhật số lượng mặt hàng, tính tổng tiền, tổng trọng lượng và xóa sản phẩm hoàn toàn qua **AJAX** không load lại trang.

### 3. Phân hệ Đặt hàng & Địa chỉ (Checkout & Address Management)
*   **Customer Checkout UI [Done - HoangDM]**: Thiết kế giao diện thanh toán Glassmorphism cực kỳ cao cấp, hiển thị tóm tắt giỏ hàng, thông tin phí ship tính toán tự động.
*   **Order Placement Transaction [Done - HoangDM]**: Xây dựng thủ tục tạo hóa đơn giao dịch ACID trong JDBC. Khóa dòng kiểm tra kho trước khi trừ số lượng sản phẩm để chống lỗi bán quá tải (Over-selling).
*   **Address Management [Done - HoangDM]**: Tích hợp danh sách địa chỉ giao hàng (`UserAddress`) của Customer vào luồng thanh toán.
*   **Order Success Screen [Done - HoangDM]**: Trang xác nhận đơn hàng thành công, hiển thị tóm tắt thông tin đơn hàng và lời cảm ơn.

### 4. Phân hệ Cửa hàng & Thông tin Shop (Shop Profile & ShopView)
*   **Shop Profile Management [Done - HoangDM]**: Hỗ trợ Shop Owner chỉnh sửa thông tin cửa hàng, tích hợp `ShopProfileUploadAPI` cho phép tải ảnh đại diện/ảnh bìa cửa hàng.
*   **Shop Storefront (ShopView) [Done - HoangDM]**: Trang hiển thị danh sách sản phẩm riêng biệt của từng cửa hàng với giao diện hiện đại, tối ưu cho trải nghiệm người dùng.

### 5. Thông tin & Trải nghiệm Guest (Guest Experience & Shared Layouts)
*   **Guest Pages [Done - HoangDM]**: Triển khai trang Giới thiệu (About Us) với thiết kế lộng lẫy và trang Liên hệ (Contact) hoàn hảo.
*   **Shared Layout Upgrades [Done - HoangDM]**: Cập nhật Footer, Header, Navbar, và Profile UI nâng cấp đồng bộ giao diện cho toàn bộ website.

### 6. Quản trị nâng cao (Admin System Maintenance)
*   **Force Logout All Sessions [Done - HoangDM]**: Cho phép Admin cưỡng chế đăng xuất toàn bộ người dùng khỏi hệ thống thông qua việc xóa phiên và cập nhật database để phục vụ bảo trì hệ thống định kỳ.

### 7. Phân hệ Giao hàng & Vận chuyển (Delivery & Shipper Flow)
*   **Duyệt/Hủy đơn phía Shop Owner [Done - HoangDM]**: Tích hợp giao diện duyệt, hủy đơn kèm lý do và hoàn kho tương ứng.
*   **Dashboard Shipper & Tự nhận đơn (Self-Claim) [Done - HoangDM]**: Thiết kế màn hình Shipper tự do nhận đơn hàng chưa phân công. Ngăn ngừa Race Condition bằng điều kiện SQL nguyên tố.
*   **Theo dõi hành trình Shopee/TikTok Shop Style [Done - HoangDM]**: Hiển thị nhật ký sự kiện chi tiết với thời gian thực và ảnh đối chứng cho khách hàng.

---

## ⚙️ III. CÁ C PHẦN ĐANG TRIỂN KHAI (DOING)
*Dự kiến hoàn thành trước ngày 12-06-2026*

1.  **Product CRUD & Multi-Image Upload [Doing - KhangCB]**:
    *   Xây dựng trang Dashboard quản trị cho Shop Owner quản lý danh sách sản phẩm.
    *   Triển khai Upload nhiều ảnh sản phẩm đồng thời lên thư mục Assets `/assets/images/`.
2.  **Content Moderation & Categories [Doing - Quan]**:
    *   Trang Admin duyệt sản phẩm mới của các Shop Owner trước khi cho hiển thị công khai.
    *   Quản trị danh mục sản phẩm (Thêm/Sửa/Xóa danh mục).

---

## 📅 IV. KẾ HOẠCH CHI TIẾT TUẦN TIẾP THEO (NEXT WEEK PLAN)
*Từ ngày 08-06-2026 đến ngày 14-06-2026*

*   **Tích hợp Payment Gateway VietQR & SePay Webhook [In-charge: HoangDM]**:
    *   Sinh mã QR động ngân hàng chứa chính xác số tiền và mã nội dung đơn hàng.
    *   Xây dựng API bảo mật xác thực payload webhook bằng thuật toán chữ ký băm SHA-256 để tự động cập nhật trạng thái đơn hàng sang `PAID` trong vòng 1 giây.
*   **Hệ thống Đánh giá & Phản hồi (Product Reviews) [In-charge: Quan]**:
    *   Viết mã chức năng viết đánh giá, chấm điểm sao (1-5 sao) và đính kèm hình ảnh thực tế cho các sản phẩm đã mua thành công.

---

## 🚨 V. CÁC VẤN ĐỀ VÀ ĐỀ XUẤT (ISSUES & SUGGESTIONS)

1.  **Vấn đề hiệu năng Flash Sale (Database Row Locking)**:
    *   *Mô tả*: Khi có Flash Sale với lượng truy cập cao, nhiều giao dịch thanh toán đồng thời tác động vào bảng `product_variants` dễ gây nghẽn hàng chờ SQL.
    *   *Đề xuất*: Thiết kế cơ chế trừ kho ảo trên bộ nhớ đệm hoặc tối ưu hóa Transaction Isolation Level về mức phù hợp nhất.
2.  **Vấn đề phụ thuộc mạng thanh toán bên thứ ba (SePay Webhook)**:
    *   *Mô tả*: Webhook SePay phụ thuộc vào kết nối mạng và độ trễ phản hồi của hệ thống ngân hàng.
    *   *Đề xuất*: Luôn duy trì phương án thanh toán thủ công (COD) và cơ chế tải ảnh hóa đơn giao dịch dự phòng làm phương án fallback.

---
*Báo cáo được chuẩn hóa và đối chiếu trực tiếp từ mã nguồn thực tế của dự án.*
**Đại diện nhóm phát triển: Dương Minh Hoàng (HE191087)**iết đánh giá, chấm điểm sao (1-5 sao) và đính kèm hình ảnh thực tế cho các sản phẩm đã mua thành công.

---

## 🚨 V. CÁC VẤN ĐỀ VÀ ĐỀ XUẤT (ISSUES & SUGGESTIONS)

1.  **Vấn đề hiệu năng Flash Sale (Database Row Locking)**:
    *   *Mô tả*: Khi có Flash Sale với lượng truy cập cao, nhiều giao dịch thanh toán đồng thời tác động vào bảng `product_variants` dễ gây nghẽn hàng chờ SQL.
    *   *Đề xuất*: Thiết kế cơ chế trừ kho ảo trên bộ nhớ đệm hoặc tối ưu hóa Transaction Isolation Level về mức phù hợp nhất.
2.  **Vấn đề phụ thuộc mạng thanh toán bên thứ ba (SePay Webhook)**:
    *   *Mô tả*: Webhook SePay phụ thuộc vào kết nối mạng và độ trễ phản hồi của hệ thống ngân hàng.
    *   *Đề xuất*: Luôn duy trì phương án thanh toán thủ công (COD) và cơ chế tải ảnh hóa đơn giao dịch dự phòng làm phương án fallback.

---
*Báo cáo được chuẩn hóa và đối chiếu trực tiếp từ mã nguồn thực tế của dự án.*
**Đại diện nhóm phát triển: Dương Minh Hoàng (HE191087)**
