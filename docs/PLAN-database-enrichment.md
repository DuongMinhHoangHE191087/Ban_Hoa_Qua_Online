# Kế hoạch làm giàu và rà soát thiết kế Cơ sở dữ liệu (PLAN-database-enrichment.md)

Tài liệu này trình bày phân tích hiện trạng cơ sở dữ liệu hệ thống Online Fruit Shop Marketplace, rà soát chi tiết tính chất Nullability (cho phép NULL hay NOT NULL) của các bảng cốt lõi và thiết kế các bảng/trường mới để hoàn thiện hệ thống (bao gồm bảng Audit Logs bị thiếu).

---

## 1. Rà soát tính chất Nullability (NULL vs NOT NULL)

Sau khi kiểm duyệt toàn bộ file schema, dưới đây là đánh giá và xác nhận về tính hợp lệ của việc cho phép NULL/NOT NULL trên các trường quan trọng:

### A. Bảng `users` (Thông tin người dùng)
- `password_hash` (`NVARCHAR(255) NULL`): **Hợp lý**. Cho phép NULL đối với các tài khoản đăng nhập trực tiếp qua bên thứ ba như Google (OAuth2) mà không tạo mật khẩu cục bộ ban đầu.
- `phone` (`NVARCHAR(15) NULL UNIQUE`): **Hợp lý**. Cho phép NULL để người dùng đăng ký bằng Email hoặc Google có thể bổ sung số điện thoại sau trong trang cá nhân.
- `avatar_url` (`NVARCHAR(500) NULL`): **Hợp lý**. Người dùng mới chưa có ảnh đại diện sẽ hiển thị avatar mặc định theo ký tự tên đầu tiên.

### B. Bảng `orders` (Đơn hàng)
- `owner_id` (`INT NULL`): **Bắt buộc**. Do hệ thống hỗ trợ mô hình Multi-shop (một giỏ hàng gom từ nhiều shop), đơn hàng gốc (PARENT order) sẽ không thuộc riêng một shop nào (`owner_id = NULL`). Các đơn hàng con (CHILD order) tách ra cho từng shop sẽ có `owner_id NOT NULL`.
- `parent_order_id` (`INT NULL`): **Hợp lý**. Trường này `NULL` đối với đơn hàng gốc (PARENT) và chứa ID trỏ tới đơn gốc đối với đơn hàng con (CHILD).
- `cancelled_at`, `cancelled_by`, `cancellation_reason` (`NULL`): **Hợp lý**. Chỉ điền dữ liệu khi đơn hàng bị chuyển trạng thái sang `CANCELLED`.

### C. Bảng `chat_messages` (Tin nhắn chat)
- `content` (`NVARCHAR(MAX) NULL`): **Hợp lý**. Người dùng có thể chỉ gửi tệp đính kèm (ảnh hoặc video) mà không cần nhập nội dung văn bản.
- `media_url`, `media_type` (`NULL`): **Hợp lý**. Chỉ điền khi tin nhắn có kèm file phương tiện.

### D. Bảng `payment_transactions` (Giao dịch thanh toán)
- `sepay_transaction_id` (`NVARCHAR(100) NULL`): **Bắt buộc**. Mã giao dịch ngân hàng thực tế chỉ được ghi nhận từ Webhook khi người dùng thực hiện chuyển khoản thành công.
- `sepay_reference` (`NVARCHAR(100) NULL`): **Bắt buộc**. Nội dung chuyển khoản/mã đơn hàng chuyển đổi, chỉ được điền khi bắt đầu khởi tạo mã QR thanh toán.

---

## 2. Các phần làm giàu CSDL đề xuất (Enrichment Proposal)

Để nâng cấp hệ thống đạt mức vận hành doanh nghiệp toàn diện, chúng tôi đề xuất bổ sung các cấu trúc dữ liệu sau:

### A. Bổ sung bảng `audit_logs` (Nhật ký kiểm toán - Hoàn toàn thiếu trong mã nguồn)
Bảng này ghi lại toàn bộ lịch sử thao tác quan trọng của Admin và người vận hành (duyệt sản phẩm, khóa tài khoản, thay đổi tham số hệ thống) để đối soát.
```sql
CREATE TABLE audit_logs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL FOREIGN KEY REFERENCES users(user_id) ON DELETE SET NULL, -- Ai thực hiện (NULL nếu do hệ thống tự động)
    action NVARCHAR(100) NOT NULL,                                            -- Loại hành động (VD: 'LOCK_USER', 'APPROVE_SHOP')
    target_type NVARCHAR(50) NOT NULL,                                       -- Đối tượng tác động ('USER', 'SHOP', 'PRODUCT')
    target_id INT NULL,                                                       -- ID của đối tượng bị tác động
    detail NVARCHAR(MAX) NOT NULL,                                            -- Mô tả chi tiết hoặc JSON thay đổi (trước/sau)
    ip_address NVARCHAR(45) NULL,                                             -- Địa chỉ IP của client thực hiện
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);
```

### B. Bổ sung các cột an toàn vào `products` và `product_variants`
- `discount_percentage` (`DECIMAL(5,2) NULL CHECK (discount_percentage BETWEEN 0 AND 100)`): Thêm vào sản phẩm/variant giúp dễ dàng hiển thị nhãn giảm giá (VD: -15%) thay vì phải tính toán động trên UI.

---

## 3. Kế hoạch triển khai & Xác thực

### Pha 1: Tạo kịch bản Sql Migration
- Tạo file SQL migration mới: `database/Migration_20260614_AuditLogsAndSchemaEnrichment.sql` chứa câu lệnh tạo bảng `audit_logs` và bổ sung các cột mới.

### Pha 2: Thực thi nâng cấp Database
- Chạy cập nhật database bằng công cụ `setup-db` hoặc gọi lệnh `sqlcmd.exe` thông qua `build-tools.ps1`.

### Pha 3: Xác minh tự động
- Chạy lại toàn bộ JUnit tests để kiểm tra xem có bất kỳ ảnh hưởng nào tới hoạt động hiện tại của hệ thống hay không:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\build-tools.ps1 test
  ```
