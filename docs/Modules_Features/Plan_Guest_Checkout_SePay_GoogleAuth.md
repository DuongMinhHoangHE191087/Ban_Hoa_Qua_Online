# Plan thay đổi feature tree, checkout guest, SePay và Google Auth

## 1. Mục tiêu

Tài liệu này mô tả phạm vi thay đổi cần thiết để hỗ trợ các yêu cầu sau:

- Guest chưa đăng nhập vẫn có thể thêm giỏ hàng và thanh toán.
- Sau khi thanh toán thành công, hệ thống tạo tài khoản và cấp mật khẩu qua email.
- Tích hợp đăng nhập / đăng ký bằng Google Auth.
- Thanh toán bằng SePay.
- Khách hàng đã có tài khoản sẽ dùng mật khẩu vừa cấp hoặc Google Auth để xem lịch sử đơn và tracking.
- Tracking và Order History vẫn ưu tiên luồng authenticated, không mở public tracking token trong phạm vi này.

Mục tiêu của plan là giúp đánh giá trước chi phí, độ phức tạp và điểm phải sửa trong tài liệu / database / use case trước khi đụng code.

---

## 2. Kết luận nhanh

### 2.1 Những phần cần đổi mạnh

- Feature tree cần thêm nhánh rõ ràng cho Google Auth và SePay.
- UC-07 Checkout là use case phải sửa nhiều nhất vì đây là nơi chuyển từ "chỉ Customer" sang "Guest + Customer".
- Business rule USR-05 phải đổi vì hiện tại SRS đang chốt Guest phải login/register trước checkout.
- UI Screen Checklist cho Checkout và Payment cần cập nhật để phản ánh Guest Checkout và SePay.

### 2.2 Những phần thay đổi ít

- UC-08 View and Track Order History gần như giữ nguyên logic authenticated Customer.
- UC-01 Register và UC-02 Login chủ yếu đồng bộ mô tả với Google Auth đã có trong SRS hiện tại.
- Database payment schema đã có nền tảng SePay, nên không cần thêm bảng thanh toán mới nếu đi theo hướng tối giản.

### 2.3 Điểm cần cân nhắc kỹ

- Nếu để Guest thanh toán trực tiếp, hệ thống phải tự sinh tài khoản sau thanh toán để Customer có thể xem history và tracking sau này.
- Nếu cấp mật khẩu qua email, cần đảm bảo không lộ dữ liệu nhạy cảm và mật khẩu phải được hash trước khi lưu.
- Nếu muốn Google Auth hoạt động tốt với email đã tồn tại, phải chốt rõ rule link account theo email hay theo Google subject ID.

---

## 3. Phạm vi yêu cầu mới

### 3.1 Guest checkout

Guest chưa đăng nhập vẫn có thể:

- lưu cart ở localStorage / sessionStorage,
- đi đến checkout,
- nhập đầy đủ thông tin nhận hàng,
- chọn phương thức thanh toán,
- thanh toán thành công qua SePay,
- sau đó được hệ thống tạo tài khoản và gửi thông tin đăng nhập qua email.

### 3.2 Google Auth

Hệ thống cần hỗ trợ:

- đăng nhập bằng Google,
- đăng ký bằng Google,
- liên kết tài khoản theo email hoặc Google subject,
- dùng Google Auth làm một đường vào để khách quay lại hệ thống mà không cần nhớ password.

### 3.3 SePay

Hệ thống cần hỗ trợ:

- tạo giao dịch thanh toán,
- hiển thị QR / reference,
- nhận webhook xác nhận,
- cập nhật trạng thái payment / order,
- tránh xử lý trùng webhook.

### 3.4 History và tracking

Theo yêu cầu hiện tại, tracking và order history:

- vẫn là luồng đăng nhập,
- không cần mở public tracking riêng,
- có thể dùng account vừa được cấp password hoặc Google Auth để truy cập.

---

## 4. Feature tree cần thay đổi

### 4.1 Feature tree hiện tại

Trong [SRS Feature Tree](SRS_Feature_Tree_FruitShop.md), nhánh đang liên quan trực tiếp là:

- Authentication
- Cart and Checkout
- Order, Payment and Tracking
- Non-UI Services

### 4.2 Nhánh cần bổ sung hoặc chỉnh lại

#### Authentication

Thêm các mục con sau:

- Google OAuth Sign-in / Sign-up
- Account Linking / Social Login Handling

#### Cart and Checkout

Thêm hoặc đổi tên các mục con sau:

- Guest Checkout Page
- Guest Customer Info Form
- Auto Account Provisioning Function

#### Order, Payment and Tracking

Thêm:

- SePay Payment Integration
- SePay Webhook Function
- Payment Status Synchronization

#### Non-UI Services

Thêm:

- Email Delivery Service
- Password Generation / Hashing Service
- Account Provisioning Service

### 4.3 Gợi ý cách cập nhật feature tree

Nếu muốn giữ tài liệu gọn, có thể cập nhật feature tree theo hướng sau:

```text
Online Fruit Shop System
├─ Authentication [Guest, Customer, Shop Owner, Delivery Staff, Admin]
│  ├─ Account Access
│  │  ├─ Register Page
│  │  ├─ Login Page
│  │  ├─ Forgot Password Page
│  │  └─ Google OAuth Sign-in / Sign-up
│  └─ Session Security
│     └─ Login Lockout Function
├─ Product Discovery [Guest, Customer, Shop Owner, Delivery Staff, Admin]
│  └─ ...
├─ Cart and Checkout [Guest, Customer]
│  ├─ Guest Cart
│  ├─ Guest Checkout Page
│  ├─ Customer Cart Page
│  ├─ Checkout Page
│  └─ Order Confirmation Page
├─ Order, Payment and Tracking [Customer, Shop Owner, Delivery Staff, Admin, Payment Gateway / Bank]
│  ├─ Payment Page
│  ├─ SePay Payment Integration
│  ├─ Payment Webhook Function
│  ├─ Order History Page
│  ├─ Order Detail / Tracking Page
│  └─ Delivery Status Update Function
└─ Non-UI Services [System]
   ├─ Cart Synchronization Service
   ├─ Inventory Reservation Service
   ├─ Notification Service
   ├─ Email Delivery Service
   └─ Account Provisioning Service
```

---

## 5. Use case nào cần sửa

### 5.1 UC-07 Checkout

Đây là use case phải sửa chính.

#### Điểm phải đổi

- PRE-1 phải mở rộng từ Customer sang Guest / Customer.
- Flow phải có nhánh Guest nhập họ tên, email, số điện thoại, địa chỉ đầy đủ.
- Nếu thanh toán thành công, hệ thống tạo account tự động.
- Nếu email đã tồn tại, phải chốt rule xử lý:
  - hoặc báo user đăng nhập,
  - hoặc link vào Google Auth nếu cùng email.
- Payment method cần đổi từ mô tả chung sang SePay / COD / các phương thức tương ứng nếu vẫn giữ COD.

#### Ảnh hưởng nghiệp vụ

- Đây là chỗ thay đổi logic lớn nhất của hệ thống.
- Dễ phát sinh trùng tài khoản nếu guest thanh toán bằng email đã tồn tại.
- Dễ phát sinh lỗi transaction nếu tạo order, tạo account, gửi email, và nhận webhook không đồng bộ.

### 5.2 UC-01 Register Account

Phần này đã có Google OAuth trong bản SRS hiện tại, nhưng nên cập nhật lại để đồng bộ với feature tree mới.

#### Điểm cần kiểm tra

- Google registration có tạo tài khoản Customer mặc định hay không.
- Nếu email Google trùng email local account thì link account thế nào.
- Nếu cho phép guest checkout tạo account tự động, UC-01 cần nói rõ tài khoản được tạo bởi checkout có thể dùng lại Google Auth hay local password.

### 5.3 UC-02 Login

Chủ yếu cần đồng bộ lại mô tả.

#### Điểm cần kiểm tra

- Google login cần xử lý account đã tồn tại.
- Nếu guest được auto provision, account mới tạo phải có thể login bằng email/password sau khi nhận mail.

### 5.4 UC-08 View and Track Order History

Theo yêu cầu mới, UC-08 có thể giữ gần như nguyên.

#### Điểm cần đổi nhẹ

- Mô tả nên nói rõ Customer có thể vào bằng mật khẩu vừa được cấp hoặc Google Auth.
- Nếu tài khoản vừa tạo từ guest checkout, order history vẫn thuộc authenticated Customer.
- Không cần public tracking riêng nếu bạn muốn giữ đơn giản và an toàn.

### 5.5 UC-05 và UC-06

Hai use case cart này về cơ bản không cần thay đổi lớn.

#### Điểm cần rà soát

- Guest cart vẫn là local storage.
- Customer cart vẫn là server cart.
- Guest checkout là luồng mới nằm trên nền guest cart, không làm hỏng mô hình cart hiện có.

---

## 6. Database impact

### 6.1 Schema hiện tại đã có gì

Từ [database/Schema.sql](../database/Schema.sql), hiện đã có:

- `users` có `email`, `password_hash`, `status`, `is_email_verified`
- `orders` có `status`, `payment_method`, `delivery_address`, `user_address`
- `payment_transactions` có các trường liên quan SePay như `sepay_transaction_id`, `sepay_reference`, `sepay_qr_code`, `status`, `provider_response`
- `sepay_webhook_dedup` để tránh xử lý trùng webhook

### 6.2 Kết luận về schema

Không bắt buộc phải thêm bảng thanh toán mới nếu chỉ triển khai SePay theo schema hiện tại.

### 6.3 Các thay đổi schema có thể cần

#### Nhóm bắt buộc nếu làm guest auto account chuẩn hơn

- Cân nhắc thêm `users.auth_provider` để phân biệt `LOCAL` và `GOOGLE`.
- Cân nhắc thêm `users.google_subject` hoặc `google_id` để link tài khoản Google bền vững hơn.

#### Nhóm tùy chọn nhưng hữu ích

- Thêm `orders.order_code` nếu muốn mã đơn công khai riêng, thay vì chỉ dùng `order_id` hoặc mã sinh từ logic service.
- Thêm `orders.guest_email_snapshot` nếu muốn truy vết rõ người đặt ban đầu trước khi auto account hoàn tất.

#### Nhóm không nên làm nếu muốn giữ scope gọn

- Public tracking token riêng cho guest tracking.
- Bảng mới chỉ để phục vụ tracking công khai.

### 6.4 Rủi ro nếu không đổi schema

Nếu giữ nguyên schema hoàn toàn, hệ thống vẫn làm được, nhưng:

- việc link Google Auth có thể phụ thuộc quá nhiều vào email,
- việc audit nguồn tài khoản tạo từ guest checkout sẽ kém rõ ràng hơn,
- order code công khai phải tạo theo quy ước service chứ không lưu cố định.

---

## 7. Thay đổi tài liệu cần làm

### 7.1 Tài liệu bắt buộc cập nhật

- [SRS_Feature_Tree_FruitShop.md](SRS_Feature_Tree_FruitShop.md)
- [SRS_Section_3_Functional_Requirements_FruitShop.docx.md](SRS_Section_3_Functional_Requirements_FruitShop.docx.md)
- [Business Rules Catalog - Nền Tảng Hoa Quả.md](Business%20Rules%20Catalog%20-%20N%E1%BB%81n%20T%E1%BA%A3ng%20Hoa%20Qu%E1%BA%A3.md)
- [UC-07_Checkout_Use_Case_Spec.md](UC-07_Checkout_Use_Case_Spec.md)
- [UC-01_Register_Account_Use_Case_Spec.md](UC-01_Register_Account_Use_Case_Spec.md)
- [UC-02_Login_Use_Case_Spec.md](UC-02_Login_Use_Case_Spec.md)
- [UI_Screen_Function_Checklist_FruitShop.md](UI_Screen_Function_Checklist_FruitShop.md)

### 7.2 Tài liệu nên rà soát thêm

- [UC-08_View_and_Track_Order_History_Use_Case_Spec.md](UC-08_View_and_Track_Order_History_Use_Case_Spec.md)
- [SRS_Core_Requirements_FruitShop.md](SRS_Core_Requirements_FruitShop.md)
- [SRS_Actor_Functional_Analysis_FruitShop.md](SRS_Actor_Functional_Analysis_FruitShop.md)

### 7.3 Nội dung cần cập nhật trong tài liệu

- Mô tả actor của checkout.
- Business rule về guest checkout.
- Mô tả payment method SePay.
- Mô tả auto account creation sau checkout.
- Mô tả Google Auth ở authentication.
- Mô tả order history / tracking vẫn cần đăng nhập.

---

## 8. Luồng nghiệp vụ đề xuất

### 8.1 Guest checkout flow

1. Guest thêm sản phẩm vào cart.
2. Guest vào checkout.
3. Hệ thống yêu cầu nhập thông tin giao hàng đầy đủ.
4. Hệ thống kiểm tra lại tồn kho, phí ship, rule một shop / một đơn.
5. Guest chọn SePay.
6. Hệ thống tạo order ở trạng thái phù hợp.
7. Hệ thống tạo payment transaction và QR / reference.
8. Guest thanh toán qua SePay.
9. Webhook xác nhận payment thành công.
10. Hệ thống tạo account customer tự động.
11. Hệ thống tạo mật khẩu ngẫu nhiên, hash và lưu vào DB.
12. Hệ thống gửi email gồm:
    - thông tin đăng nhập,
    - link/login hướng dẫn,
    - hóa đơn,
    - link đến order history / tracking sau khi login.

### 8.2 Google Auth flow

1. User bấm Login with Google.
2. Google trả email và subject.
3. Hệ thống tìm user theo Google subject hoặc email.
4. Nếu chưa có account, hệ thống tạo account mới.
5. Nếu đã có account, hệ thống login trực tiếp.
6. User vào dashboard / order history như bình thường.

### 8.3 Order history / tracking flow

1. User đăng nhập bằng password vừa được cấp hoặc Google Auth.
2. User vào Order History.
3. Hệ thống truy vấn đơn theo `customer_id`.
4. User xem tracking detail và trạng thái real-time gần đúng.

---

## 9. Các điểm kỹ thuật phải xử lý

### 9.1 Transaction boundary

Phần nhạy nhất là transaction giữa:

- tạo order,
- tạo payment transaction,
- nhận webhook SePay,
- tạo account tự động,
- gửi email thông báo.

Nên thiết kế để:

- order và payment transaction không bị tạo trùng,
- webhook lặp không gây double update,
- email fail không làm rollback order đã thanh toán thành công.

### 9.2 Duplicate handling

- Webhook SePay phải idempotent.
- Nếu user submit checkout lại, hệ thống không được sinh thêm order duplicate.
- Nếu email đã tồn tại, phải có rule rõ để tránh tạo 2 account cùng một email.

### 9.3 Password handling

- Không gửi mật khẩu plaintext trong DB.
- Chỉ gửi mật khẩu tạm thời qua email một lần nếu bắt buộc.
- Nên khuyến nghị user đổi mật khẩu sau khi login lần đầu.

### 9.4 Google account linking

- Nên ưu tiên link theo Google subject + email.
- Không nên chỉ dùng email nếu muốn giảm rủi ro email thay đổi hoặc xung đột account.

---

## 10. Độ phức tạp và chi phí ước lượng

### 10.1 Nếu chỉ sửa tài liệu

- Độ phức tạp: thấp.
- Thời gian: 0.5 đến 1 ngày.
- Chi phí công: thấp.

### 10.2 Nếu làm code nhưng giữ DB gần như nguyên trạng

- Độ phức tạp: cao.
- Thời gian: khoảng 8 đến 14 person-days.
- Chi phí công: trung bình đến cao.

### 10.3 Nếu thêm Google subject / auth_provider / order_code persist riêng

- Độ phức tạp: cao hơn một mức.
- Thời gian: khoảng 12 đến 18 person-days.
- Chi phí công: cao hơn do phải sửa thêm schema, DAO và migration.

### 10.4 Điểm tốn công nhất

1. Tích hợp checkout guest mà vẫn giữ đúng transaction.
2. Xử lý webhook SePay không trùng.
3. Auto-provision account sau payment thành công.
4. Đồng bộ Google Auth với account local đã tồn tại.
5. Gửi email ổn định mà không làm chậm checkout.

---

## 11. Rủi ro chính

### 11.1 Rủi ro nghiệp vụ

- Guest dùng email đã tồn tại nhưng không biết mật khẩu.
- Một user có thể có cả local password và Google Auth nếu không link đúng.
- Thanh toán thành công nhưng email gửi mật khẩu thất bại.

### 11.2 Rủi ro kỹ thuật

- Webhook trả về lặp.
- Order sinh trùng khi user bấm lại.
- Đồng bộ trạng thái payment / order lệch nhau.
- Gửi email sync làm treo response.

### 11.3 Rủi ro vận hành

- Cấu hình SMTP / Google OAuth / SePay callback bị sai môi trường.
- Quy trình support khách hàng phức tạp hơn vì có thêm account tự sinh.

---

## 12. Khuyến nghị triển khai theo pha

### Pha 1: Chốt tài liệu và business rule

- Sửa feature tree.
- Sửa USR-05.
- Sửa UC-07.
- Đồng bộ UC-01, UC-02, UC-08 ở mức mô tả.

### Pha 2: Payment và checkout

- Gắn SePay vào checkout.
- Hoàn thiện payment webhook.
- Chốt mapping giữa order và payment transaction.

### Pha 3: Guest auto account

- Sinh account sau khi payment thành công.
- Gửi email mật khẩu / hướng dẫn login.
- Cho phép user dùng account đó để xem history / tracking.

### Pha 4: Google Auth

- Hoàn thiện Google OAuth flow.
- Link account theo email / Google subject.
- Kiểm tra xung đột với account local.

### Pha 5: Hardening

- Idempotency.
- Audit log.
- Email retry.
- Security review.

---

## 13. Quyết định khuyến nghị

Nếu muốn giảm độ phức tạp, nên chọn phương án sau:

- Guest được checkout và thanh toán qua SePay.
- Sau thanh toán thành công, hệ thống tự tạo tài khoản Customer và gửi email mật khẩu tạm thời.
- Tracking và Order History vẫn require login.
- Google Auth là tùy chọn login / signup thêm, không bắt buộc để hoàn tất purchase.

Phương án này giữ được phạm vi vừa phải, không cần public tracking token, không đụng quá sâu vào mô hình bảo mật hiện tại, và tận dụng được schema payment hiện có.

---

## 14. Kết luận

Với yêu cầu hiện tại, phần cần sửa nhiều nhất là checkout flow và business rule liên quan Guest checkout.

SePay không phải phần nặng nhất vì schema đã có nền tảng.
Google Auth cũng không phải viết mới hoàn toàn vì SRS hiện tại đã có mô tả đăng ký / đăng nhập bằng Google.

Điểm quyết định là bạn có muốn giữ tracking/history login-only hay mở public tracking. Nếu giữ login-only thì phạm vi hợp lý hơn và rủi ro thấp hơn.
