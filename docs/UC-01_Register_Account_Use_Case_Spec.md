# Use Case Specification
## Hệ thống bán hoa quả online (Online Fruit Shop System)
### UC-01 Đăng ký tài khoản (Register Account)

## 1. Thông tin Quản lý
**ID và Tên (ID and Name):** UC-01 Đăng ký tài khoản
**Người tạo (Created By):** Dương Minh Hoàng
**Ngày tạo (Date Created):** 19/05/2026

## 2. Định nghĩa Tác nhân & Mục đích
**Tác nhân chính (Primary Actor):** Khách vãng lai (Guest)
**Tác nhân phụ (Secondary Actors):** Admin (chỉ khi cần phê duyệt Shop Owner), Hệ thống Google OAuth (Google API), Dịch vụ Gửi Email / OTP (nếu bật).
**Mô tả (Description):** Cho phép Khách vãng lai tạo mới một tài khoản Customer hoặc Shop Owner để có thể sử dụng các chức năng yêu cầu xác thực của hệ thống Nền tảng Hoa Quả. Hỗ trợ đăng ký thủ công hoặc nhanh chóng thông qua Google OAuth.

## 3. Điều kiện Thực thi
**Sự kiện kích hoạt (Trigger):** Khách vãng lai nhấn vào nút "Đăng ký" (Register) từ trang chủ, trang đăng nhập, hoặc khi bị điều hướng sau một thao tác yêu cầu đăng nhập (ví dụ: tạo giỏ hàng sang bước thanh toán).
**Điều kiện tiên quyết (Preconditions):**
- PRE-1: Người dùng hiện chưa đăng nhập vào hệ thống.
- PRE-2: Trang đăng ký hoặc hệ thống Google OAuth API đang hoạt động bình thường.
**Điều kiện hậu quyết (Postconditions):**
- POST-1: Hệ thống lưu thành công thông tin tài khoản mới vào cơ sở dữ liệu.
- POST-2: Trạng thái tài khoản được thiết lập thành "Active" (nếu là Customer hoặc đăng ký qua Google) hoặc "Pending" (nếu là Shop Owner chờ Admin duyệt).
- POST-3: Hệ thống lưu sự kiện đăng ký vào Audit Log (nếu có yêu cầu).

## 4. Kịch bản (Luồng sự kiện)
### Luồng cơ bản (Normal Flow - Đăng ký bằng Email/Mật khẩu):
1. Khách vãng lai mở trang Đăng ký (Register Page).
2. Hệ thống hiển thị biểu mẫu đăng ký với các trường: Họ và tên, Email, Số điện thoại (tùy chọn), Mật khẩu, Xác nhận Mật khẩu, Loại tài khoản (Customer / Shop Owner) và nút đăng ký qua Google.
3. Khách vãng lai nhập đầy đủ thông tin vào biểu mẫu.
4. Khách vãng lai chọn Loại tài khoản (Mặc định là Customer).
5. Khách vãng lai nhấn nút "Đăng ký".
6. Hệ thống kiểm tra tính hợp lệ của định dạng dữ liệu, mật khẩu (độ mạnh, khớp nhau) và tính duy nhất của Email.
7. Hệ thống tạo tài khoản mới trong cơ sở dữ liệu và phân quyền theo Loại tài khoản đã chọn.
8. Hệ thống mã hóa mật khẩu trước khi lưu.
9. Hệ thống gửi email chào mừng/xác thực (nếu cấu hình yêu cầu).
10. Hệ thống hiển thị thông báo Đăng ký thành công và điều hướng Khách vãng lai tới trang Đăng nhập (Login Page).

### Luồng thay thế (Alternative Flows):
**4.1. Đăng ký qua Google OAuth**
1. Ở bước 3 của Luồng cơ bản, Khách vãng lai nhấn nút "Đăng ký bằng Google".
2. Hệ thống chuyển hướng Khách vãng lai đến trang xác thực của Google.
3. Khách vãng lai đăng nhập và cấp quyền cho hệ thống truy cập thông tin cơ bản (Email, Họ tên, Ảnh đại diện).
4. Google trả về Token Profile cho hệ thống.
5. Hệ thống trích xuất Email và Họ tên từ Token.
6. Hệ thống nhận diện đây là Email mới (chưa tồn tại trong Database), tự động tạo tài khoản mới với loại mặc định là Customer và trạng thái Active. (Không cần mật khẩu).
7. Hệ thống tự động thiết lập phiên đăng nhập (session).
8. Hệ thống điều hướng Khách vãng lai thẳng tới Trang chủ (Home Page) hoặc giao diện được chỉ định.

**4.2. Đăng ký tài khoản Shop Owner cần Admin phê duyệt**
1. Ở bước 4 của Luồng cơ bản, Khách vãng lai chọn Loại tài khoản là "Shop Owner".
2. Khách vãng lai thực hiện tiếp tục đến bước 7.
3. Hệ thống tạo tài khoản nhưng gán trạng thái là "Pending Approval" thay vì Active (dựa theo quy tắc USR-01).
4. Hệ thống gửi thông báo cho Admin về yêu cầu mở Shop mới.
5. Hệ thống thông báo cho Khách vãng lai: "Tạo tài khoản thành công. Tài khoản Shop đang chờ Admin phê duyệt." và kết thúc Use Case.

### Ngoại lệ / Xử lý lỗi (Exceptions):
**6.E1 Dữ liệu không hợp lệ hoặc thiếu**
1. Ở bước 6 của Luồng cơ bản, Hệ thống phát hiện định dạng Email sai, mật khẩu quá ngắn hoặc hai mật khẩu không khớp.
2. Hệ thống bôi đỏ các trường bị lỗi và hiển thị thông báo lỗi tương ứng.
3. Khách vãng lai chỉnh sửa lại dữ liệu và nhấn "Đăng ký" để thử lại. Hệ thống quay lại bước 6.

**7.E1 Email đã tồn tại trong hệ thống**
1. Ở bước 6 của Luồng cơ bản, hoặc bước 6 của Luồng 4.1 (Google), Hệ thống kiểm tra thấy Email đã được đăng ký trước đó.
2. Hệ thống hiển thị thông báo: "Email này đã được sử dụng. Vui lòng đăng nhập hoặc sử dụng Email khác."
3. Khách vãng lai chọn quay lại trang Đăng nhập hoặc nhập Email mới để tiếp tục.

**4.1.E1 Lỗi xác thực từ Google OAuth**
1. Ở bước 4 của Luồng 4.1, Google trả về lỗi (do Khách từ chối cấp quyền, hết hạn token, lỗi mạng).
2. Hệ thống hiển thị thông báo: "Đăng ký bằng Google thất bại. Vui lòng thử lại hoặc đăng ký thủ công."
3. Khách vãng lai quay lại trang đăng ký bình thường.

**10.E1 Lỗi Hệ thống / Database**
1. Ở bước 7 của Luồng cơ bản, hệ thống không thể lưu trữ dữ liệu do mất kết nối CSDL (Database Error).
2. Hệ thống ghi log lỗi và thông báo: "Hệ thống đang gặp sự cố, vui lòng thử lại sau."
3. Hệ thống kết thúc Use Case mà không lưu dữ liệu.

## 5. Thông tin Bổ sung
**Độ ưu tiên (Priority):** Cao (P0 - Chức năng cốt lõi)
**Tần suất sử dụng (Frequency of Use):** Thường xuyên (High). Hàng ngày, mỗi khi có khách hàng hoặc shop mới tiếp cận nền tảng.
**Quy tắc nghiệp vụ (Business Rules):** 
- USR-01: Tài khoản Shop Owner đăng ký cần được phê duyệt để Active.
- USR-05: Guest được phép lướt giỏ hàng nhưng bắt buộc qua Use case Đăng ký/Đăng nhập mới được Checkout.
**Thông tin khác (Other Information):** Mật khẩu phải được mã hóa một chiều (ví dụ: BCrypt, PBKDF2) không được lưu bản rõ. Giao diện trang đăng ký cần responsive tốt trên thiết bị di động.
**Giả định (Assumptions):** 
- Kết nối tới Google OAuth API ở môi trường production luôn ổn định.
- Database có hoạt động để kiểm tra tính duy nhất của Email ngay lập tức.
