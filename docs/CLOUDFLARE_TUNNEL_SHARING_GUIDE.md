# 🌐 Hướng Dẫn Setup Cloudflare Tunnel Để Chia Sẻ Dự Án Cho Nhóm Test Đồng Thời
> **Dự án:** Ban Hoa Qua Online (MetaFruit)  
> **Mục tiêu:** Public ứng dụng chạy ở localhost ra Internet qua HTTPS bảo mật giúp các thành viên trong nhóm có thể truy cập, sử dụng và test đồng thời mà không cần cấu hình NAT/Port Forwarding trên Router.

---

## 📋 Mục Lục
1. [Tổng quan về Cloudflare Tunnel](#1-tổng-quan-về-cloudflare-tunnel)
2. [Cách 1: Sử dụng Tunnel Miễn Phí Không Cần Tên Miền (TryCloudflare) - NHANH NHẤT](#2-cách-1-sử-dụng-tunnel-miễn-phí-không-cần-tên-miền-trycloudflare---nhanh-nhất)
3. [Cách 2: Sử dụng Tunnel Đã Cấu Hình Sẵn (metafruit)](#3-cách-2-sử-dụng-tunnel-đã-cấu-hình-sẵn-metafruit)
4. [Cách 3: Tạo Tunnel Riêng Với Tên Miền Tùy Chỉnh (Custom Domain)](#4-cách-3-tạo-tunnel-riêng-với-tên-miền-tùy-chỉnh-custom-domain)
5. [Cấu hình tối ưu hóa để nhóm test đồng thời (Concurrency Testing)](#5-cấu-hình-tối-ưu-hóa-để-nhóm-test-đồng-thời-concurrency-testing)
6. [Xử lý lỗi thường gặp (Troubleshooting)](#6-xử-lý-lỗi-thường-gặp-troubleshooting)

---

## 1. Tổng Quan Về Cloudflare Tunnel

Cloudflare Tunnel giúp tạo một kết nối mã hóa an toàn giữa máy local của bạn (nơi đang chạy Tomcat) và máy chủ Cloudflare. 

```
[Máy của thành viên nhóm] ──(HTTPS)──> [Cloudflare Edge] ──(Tunnel)──> [cloudflared client] ──> [Tomcat Local (Port 8080)]
```

### Lợi ích:
- **Không cần mở port** trên Router (Port Forwarding).
- **Không cần IP tĩnh** hay các dịch vụ DNS động (DDNS).
- Cung cấp sẵn **HTTPS miễn phí** có chứng chỉ hợp lệ.
- Bảo mật thông tin máy chủ gốc (máy dev của bạn).

---

## 2. Cách 1: Sử dụng Tunnel Miễn Phí Không Cần Tên Miền (TryCloudflare) - NHANH NHẤT

Đây là phương án tốt nhất nếu bạn muốn chia sẻ nhanh dự án cho nhóm test trong vài giờ mà không muốn đăng ký tài khoản Cloudflare hoặc mua tên miền riêng.

### Bước 1: Tải và cài đặt `cloudflared`
- **Windows:**
  1. Tải file thực thi trực tiếp từ Cloudflare: [cloudflared-windows-amd64.msi](https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.msi) hoặc bản exe: [cloudflared-windows-amd64.exe](https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe).
  2. Đổi tên thành `cloudflared.exe` và thêm vào thư mục đã được cấu hình trong biến môi trường `PATH` (hoặc copy trực tiếp vào thư mục dự án).
  3. Hoặc dùng PowerShell với quyền Admin để cài qua Winget:
     ```powershell
     winget install --id Cloudflare.cloudflared
     ```
- **macOS (Homebrew):**
  ```bash
  brew install cloudflare/cloudflare/cloudflared
  ```

### Bước 2: Khởi động Tomcat Local
Đảm bảo dự án đã được build và chạy thành công trên Tomcat local.
- Mở `build-tools.bat` -> chọn **[0]** (Build + Deploy + Run).
- Hoặc kiểm tra xem link local này có hoạt động không: `http://localhost:8080/Ban_Hoa_Qua_Online/`

### Bước 3: Tạo Tunnel tức thì (Ephemeral Tunnel)
Mở terminal (PowerShell hoặc CMD) và chạy lệnh sau:
```powershell
cloudflared tunnel --url http://localhost:8080
```

### Bước 4: Lấy URL và chia sẻ
Trong log hiển thị trên terminal, tìm dòng tương tự như sau:
```
+------------------------------------------------------------+
|  Your quick Tunnel has been created!                       |
|  Url: https://some-random-words.trycloudflare.com         |
+------------------------------------------------------------+
```
Sao chép URL đó và gửi cho nhóm của bạn. Đường dẫn đầy đủ để truy cập ứng dụng sẽ là:
> **`https://some-random-words.trycloudflare.com/Ban_Hoa_Qua_Online/`**

*(Lưu ý: URL này sẽ thay đổi mỗi khi bạn khởi động lại lệnh).*

---

## 3. Cách 2: Sử dụng Tunnel Đã Cấu Hình Sẵn (metafruit)

Dự án đã có cấu hình Tunnel mẫu tại tệp [config.yml](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/cloudflare/config.yml). 

### Bước 1: Xác nhận file credentials
Phương pháp này yêu cầu máy của bạn phải có file thông tin xác thực (credentials) của tunnel `metafruit` nằm tại đường dẫn:
`C:\Users\Admin\.cloudflared\b873150f-1854-45a0-b0b2-8eade3f62f1b.json`

### Bước 2: Chạy qua bộ công cụ tích hợp
Bạn chỉ cần mở `build-tools.bat` ở thư mục gốc dự án:
1. Nhập số **`18`** để chạy riêng Cloudflare Tunnel.
2. Hoặc nhập số **`0`** để vừa build dự án, khởi động Tomcat và chạy tunnel song song.

### Bước 3: Chạy thủ công bằng dòng lệnh
Nếu không dùng file script, mở terminal tại thư mục gốc dự án và chạy:
```powershell
cloudflared tunnel --config cloudflare/config.yml run
```

---

## 4. Cách 3: Tạo Tunnel Riêng Với Tên Miền Tùy Chỉnh (Custom Domain)

Nếu bạn muốn có một đường dẫn **cố định và chuyên nghiệp** (ví dụ: `https://test.domaincuaban.com/Ban_Hoa_Qua_Online/`) để nhóm test tiện lưu trữ, hãy làm theo các bước sau:

### Yêu cầu:
- Có một tài khoản Cloudflare (Miễn phí).
- Một tên miền đã trỏ Name Servers về Cloudflare quản lý.

### Bước 1: Đăng nhập Cloudflare trên máy tính của bạn
Chạy lệnh sau trên Terminal:
```powershell
cloudflared tunnel login
```
Một trình duyệt sẽ tự động mở ra. Đăng nhập vào tài khoản Cloudflare của bạn, chọn tên miền muốn sử dụng và nhấn **Authorize**.

### Bước 2: Tạo một Tunnel mới
Đặt tên cho tunnel của bạn (ví dụ: `fruitshop-test`):
```powershell
cloudflared tunnel create fruitshop-test
```
Lệnh này sẽ trả về một mã **Tunnel ID** (ví dụ: `a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d`) và tạo một file JSON chứa thông tin xác thực tại thư mục cá nhân của bạn (`~/.cloudflared/`).

### Bước 3: Cấu hình tệp `config.yml`
Tạo hoặc chỉnh sửa file cấu hình (ví dụ đặt tại `cloudflare/config.yml`):
```yaml
tunnel: a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d
credentials-file: C:\Users\Tên_User\.cloudflared\a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d.json

ingress:
  # Ánh xạ tên miền phụ vào tomcat local của bạn
  - hostname: test.domaincuaban.com
    service: http://localhost:8080
  
  # Cấu hình mặc định trả về lỗi 404 cho các yêu cầu khác
  - service: http_status:404
```

### Bước 4: Tạo bản ghi DNS (Route traffic)
Liên kết tên miền phụ với tunnel của bạn bằng cách chạy:
```powershell
cloudflared tunnel route dns fruitshop-test test.domaincuaban.com
```
Bản ghi CNAME trỏ từ `test.domaincuaban.com` về mã định danh của tunnel sẽ tự động được thêm vào cấu hình DNS của bạn trên Cloudflare.

### Bước 5: Chạy Tunnel
Chạy lệnh sau để kích hoạt tunnel:
```powershell
cloudflared tunnel --config cloudflare/config.yml run
```
Bây giờ, cả nhóm có thể truy cập dự án qua địa chỉ cố định:
> **`https://test.domaincuaban.com/Ban_Hoa_Qua_Online/`**

---

## 5. Cấu Hình Tối Ưu Hóa Để Nhóm Test Đồng Thời (Concurrency Testing)

Khi nhiều người cùng truy cập vào máy tính cá nhân của bạn để test, tài nguyên máy và kết nối cơ sở dữ liệu sẽ bị chia sẻ. Cần tối ưu các cấu hình sau:

### 5.1 Tăng kết nối tối đa của Database (SQL Server)
Mặc định SQL Server Express giới hạn tài nguyên và kết nối. Nếu sử dụng SQL Server Local, hãy đảm bảo Pool Size trong cấu hình kết nối database của ứng dụng đủ dùng:
- Mở file cấu hình kết nối DB (ví dụ: `src/java/context/DBContext.java` hoặc file properties tương ứng).
- Đảm bảo trong hàm kết nối có sử dụng cấu hình đóng kết nối (`Connection.close()`) ngay sau khi thực hiện xong câu lệnh bằng cú pháp **Try-with-resources** để tránh nghẽn/tràn Connection Pool (đây là quy tắc bắt buộc trong [AGENTS.md](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/AGENTS.md)).

### 5.2 Cấu hình Tomcat để tải được nhiều Request cùng lúc
Mở file cấu hình `conf/server.xml` của bộ cài Tomcat bạn đang chạy, tìm thẻ `<Connector port="8080" ...>` và tối ưu hóa các tham số sau:
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           maxThreads="200"
           minSpareThreads="10"
           acceptCount="100" />
```
*Giải thích:*
- `maxThreads="200"`: Cho phép xử lý tối đa 200 luồng đồng thời (thay vì mặc định thấp hơn).
- `acceptCount="100"`: Hàng đợi tối đa cho các request chờ khi toàn bộ các luồng xử lý đều bận.

### 5.3 Cấu hình Session Persistence (Giữ phiên đăng nhập khi restart app)
Khi bạn sửa code và Tomcat tự động reload/redeploy, các thành viên đang test sẽ bị văng phiên đăng nhập (mất session). Để tránh điều này, hãy bật tính năng Session Persistence trong `context.xml` của Tomcat:
```xml
<Manager pathname="SESSIONS.ser" />
```
*(Bỏ comment dòng này trong file `web/META-INF/context.xml` hoặc file cấu hình Tomcat tương ứng).*

---

## 6. Cách Dự Phòng: Sử Dụng SSH Tunnel Qua Localhost.run (Khi Cổng 7844 Bị Chặn)

Nếu mạng của bạn (ví dụ mạng wifi trường học, ký túc xá, hoặc doanh nghiệp) chặn cổng **outbound 7844 (TCP/UDP)**, Cloudflare Tunnel sẽ không kết nối được và báo lỗi `i/o timeout` (Error 1033). 

**Giải pháp thay thế cực nhanh là sử dụng SSH Tunnel của localhost.run qua cổng 22 tiêu chuẩn (hầu như không bị chặn):**

### Bước 1: Khởi động Tomcat Local
Đảm bảo Tomcat đang chạy ở cổng 8080 (ví dụ: `http://localhost:8080/Ban_Hoa_Qua_Online/`).

### Bước 2: Chạy lệnh SSH Tunnel
Mở PowerShell hoặc CMD và chạy lệnh sau:
```powershell
ssh -o StrictHostKeyChecking=no -R 80:localhost:8080 nokey@localhost.run
```
*(Nếu Windows hỏi bạn có muốn tin cậy host key không, hãy gõ `yes` và nhấn Enter).*

### Bước 3: Lấy URL và truy cập
Trong terminal sẽ hiển thị dòng thông tin kết nối thành công:
```
authenticated as anonymous user
xxxxxx.lhr.life tunneled with tls termination, https://xxxxxx.lhr.life
```
URL công khai của bạn sẽ là: `https://xxxxxx.lhr.life/Ban_Hoa_Qua_Online/`.

### Bước 4: Cấu hình SePay Webhook (nếu cần)
Copy địa chỉ `https://xxxxxx.lhr.life/Ban_Hoa_Qua_Online/api/payment/webhook` và dán vào phần cấu hình Webhook trên trang quản trị SePay.

---

## 7. Xử Lý Lỗi Thường Gặp (Troubleshooting)

### Lỗi 1: `502 Bad Gateway`
- **Triệu chứng:** Người dùng truy cập URL Cloudflare Tunnel nhưng trình duyệt báo lỗi `502 Bad Gateway`.
- **Nguyên nhân:** Cloudflare Tunnel đang hoạt động nhưng không thể kết nối tới Tomcat local (cổng 8080).
- **Cách khắc phục:**
  1. Kiểm tra Tomcat đã khởi động chưa: Truy cập thử `http://localhost:8080/Ban_Hoa_Qua_Online/` trên máy chủ local.
  2. Kiểm tra xem cấu hình cổng trong tệp `config.yml` có khớp với cổng chạy thực tế của Tomcat (thông thường là `8080`).

### Lỗi 2: `404 Not Found` sau khi truy cập URL chính
- **Triệu chứng:** Truy cập `https://xxx.trycloudflare.com` hiển thị lỗi `404 Not Found` của Tomcat hoặc Cloudflare.
- **Nguyên nhân:** Thiếu đường dẫn ngữ cảnh (Context Path). 
- **Cách khắc phục:** Đảm bảo thêm hậu tố `/Ban_Hoa_Qua_Online/` vào cuối URL (ví dụ: `https://xxx.trycloudflare.com/Ban_Hoa_Qua_Online/`).

### Lỗi 3: `Forbidden` hoặc lỗi bảo mật liên quan đến CORS / CSRF
- **Triệu chứng:** Thành viên không thể thực hiện gửi form thanh toán, đăng nhập, hoặc gửi các yêu cầu POST.
- **Cách khắc phục:** 
  - Ứng dụng đã được thiết lập tự động loại trừ kiểm tra CSRF đối với các endpoint thanh toán thông qua cấu hình trong `CsrfFilter.java` (bỏ qua `/api/*`).
  - Nếu gặp lỗi CORS đối với các tài nguyên tĩnh hoặc API khác, bạn có thể bật CORS trên Cloudflare Tunnel bằng CLI:
    ```powershell
    hyh cors <domain-cua-ban>
    ```
    hoặc thông qua cấu hình `ingress` nâng cao.

### Lỗi 4: Mạng chậm khi test chung
- **Nguyên nhân:** Băng thông upload của mạng gia đình (nơi đặt máy local chạy Tomcat) bị giới hạn.
- **Cách khắc phục:** 
  - Khuyên các thành viên trong nhóm tắt các tác vụ download nặng khi test.
  - Tối ưu dung lượng hình ảnh trên ứng dụng Web bằng cách sử dụng ảnh kích thước nhỏ.

---

*Tài liệu hướng dẫn được biên soạn nhằm phục vụ quá trình phát triển nhóm của dự án Ban Hoa Qua Online.*
