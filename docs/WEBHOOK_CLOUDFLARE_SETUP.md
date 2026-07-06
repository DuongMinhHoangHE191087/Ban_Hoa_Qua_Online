# 🔔 Hướng Dẫn Setup Webhook SePay + Cloudflare Tunnel
> **Dự án:** MetaFruit — Ban Hoa Qua Online  
> **Phiên bản:** 1.0  
> **Mục tiêu:** Nhận webhook thanh toán tự động từ SePay về localhost để xác nhận đơn hàng

---

## 📋 Mục Lục

1. [Tổng quan luồng thanh toán](#1-tổng-quan-luồng-thanh-toán)
2. [Thông tin endpoint webhook](#2-thông-tin-endpoint-webhook)
3. [Bước 1 — Chạy Cloudflare Tunnel](#3-bước-1--chạy-cloudflare-tunnel)
4. [Bước 2 — Cấu hình SePay Webhook](#4-bước-2--cấu-hình-sepay-webhook)
5. [Bước 3 — Test Webhook thủ công](#5-bước-3--test-webhook-thủ-công)
6. [Kiểm tra log xác nhận](#6-kiểm-tra-log-xác-nhận)
7. [Troubleshooting](#7-troubleshooting)
8. [Cấu hình môi trường Production](#8-cấu-hình-môi-trường-production)

---

## 1. Tổng Quan Luồng Thanh Toán

```
[Khách chọn CK] → [App tạo QR SePay] → [Khách chuyển khoản]
                                                   ↓
                           [SePay nhận tiền vào tài khoản]
                                                   ↓
                    [SePay POST webhook → URL của bạn]
                                                   ↓
              [PaymentWebhookServlet xử lý + xác nhận đơn]
                                                   ↓
                    [Đơn hàng → trạng thái CONFIRMED]
```

### Mã Reference chuẩn

```
Format:  MF + orderId (padded 3 chữ số)
Ví dụ:   MF101  →  Đơn hàng #101
         MF007  →  Đơn hàng #7
```

> ⚠️ **QUAN TRỌNG:** Khách phải điền **chính xác mã reference** vào nội dung CK.  
> Ví dụ: `MF101` — Không thêm chữ khác.

---

## 2. Thông Tin Endpoint Webhook

| Thông số | Giá trị |
|---|---|
| **Servlet class** | `PaymentWebhookServlet` |
| **URL path** | `/api/payment/webhook` |
| **HTTP Method** | `POST` |
| **Content-Type** | `application/json` |
| **Local URL** | `http://localhost:8080/Ban_Hoa_Qua_Online/api/payment/webhook` |
| **Tunnel URL** | `https://<tunnel-domain>/Ban_Hoa_Qua_Online/api/payment/webhook` |

### Payload SePay gửi về (ví dụ thực tế)

```json
{
  "id": "12345678",
  "gateway": "MBBank",
  "transactionDate": "2026-06-13 01:00:00",
  "accountNumber": "SBSEPAY3NHWA061W5V2",
  "subAccount": null,
  "code": "MF101",
  "content": "MF101 chuyen khoan tien hang",
  "transferType": "in",
  "description": "",
  "transferAmount": 150000,
  "referenceCode": "FT26164...",
  "accumulated": 150000,
  "id": "sepay-1781287419670"
}
```

### Logic xử lý trong `PaymentService.processWebhook()`

| Điều kiện | Xử lý |
|---|---|
| `transferType != "in"` | Bỏ qua (chỉ xử lý tiền vào) |
| `sepay_transaction_id` đã tồn tại | Dedup — bỏ qua (idempotent) |
| Không tìm thấy `code` trong DB | Log warn, ghi `not_found` |
| `transferAmount < expected` | Đánh dấu `failed`, log `AMOUNT_MISMATCH` |
| Hợp lệ | `payment_transactions → completed`, `orders → CONFIRMED` |

---

## 3. Bước 1 — Chạy Cloudflare Tunnel

### Phát hiện: Tunnel `metafruit` đã được đăng ký sẵn!

```
ID:   b873150f-1854-45a0-b0b2-8eade3f62f1b
Name: metafruit
```

### Cách 1: Dùng build-tools.bat (Khuyến nghị)

```
1. Mở build-tools.bat
2. Chọn [0] → Chạy toàn bộ quy trình (Build + Deploy + Mở CF Tunnel)
   HOẶC
3. Chọn [18] → Chỉ chạy Cloudflare Tunnel đã đăng ký
```

> Menu [18] sẽ chạy: `cloudflared tunnel run` (dùng tunnel mặc định đã đăng ký)

### Cách 2: Chạy thủ công

```powershell
# Trong PowerShell hoặc CMD
cloudflared tunnel run metafruit
```

### Cách 3: Chạy với tên cụ thể nếu có nhiều tunnel

```powershell
cloudflared tunnel run b873150f-1854-45a0-b0b2-8eade3f62f1b
```

### Xác nhận tunnel đang chạy

Khi tunnel chạy thành công, bạn sẽ thấy log:

```
INF  Connection b873150f... registered at ...
INF  Connection ...registered at region1.argotunnel.com
```

### Lấy domain tunnel

```powershell
cloudflared tunnel info metafruit
```

Hoặc vào https://dash.cloudflare.com → Zero Trust → Tunnels → `metafruit` → xem Public Hostname.

---

## 4. Bước 2 — Cấu Hình SePay Webhook

### 4.1 Đăng nhập SePay

1. Truy cập: **https://my.sepay.vn**
2. Đăng nhập tài khoản SePay của bạn

### 4.2 Vào cài đặt Webhook

```
Menu bên trái → Cài đặt → Webhook / Tích hợp API
```

### 4.3 Điền thông tin Webhook

| Trường | Giá trị |
|---|---|
| **Webhook URL** | `https://<tunnel-domain>.trycloudflare.com/Ban_Hoa_Qua_Online/api/payment/webhook` |
| **Method** | `POST` |
| **Content-Type** | `application/json` |
| **Status** | `Bật (Enable)` |

> **Ví dụ URL hoàn chỉnh:**  
> `https://metafruit.example.workers.dev/Ban_Hoa_Qua_Online/api/payment/webhook`

### 4.4 Lưu và kiểm tra kết nối

SePay sẽ gửi một request test — endpoint phải trả `HTTP 200`.

---

## 5. Bước 3 — Test Webhook Thủ Công

### Test với `curl` (trong PowerShell)

```powershell
# Thay <tunnel-domain> bằng domain thật của bạn
$tunnelUrl = "https://<tunnel-domain>/Ban_Hoa_Qua_Online/api/payment/webhook"

$payload = @{
    id = "test-sepay-$(Get-Date -Format 'yyyyMMddHHmmss')"
    gateway = "MBBank"
    accountNumber = "SBSEPAY3NHWA061W5V2"
    code = "MF001"
    transferType = "in"
    transferAmount = 150000
    content = "MF001 test webhook"
} | ConvertTo-Json

Invoke-RestMethod -Uri $tunnelUrl -Method POST -Body $payload -ContentType "application/json"
```

### Test với localhost (không cần tunnel)

```powershell
$localUrl = "http://localhost:8080/Ban_Hoa_Qua_Online/api/payment/webhook"

$payload = @{
    id = "test-local-$(Get-Date -Format 'yyyyMMddHHmmss')"
    code = "MF001"
    transferType = "in"
    transferAmount = 150000
} | ConvertTo-Json

Invoke-RestMethod -Uri $localUrl -Method POST -Body $payload -ContentType "application/json"
```

### Kết quả mong đợi

```json
{"success": true, "data": null}
```

---

## 6. Kiểm Tra Log Xác Nhận

### Xem Tomcat log realtime

```powershell
# Trong build-tools.bat → chọn [5] Xem log gần đây
# Hoặc chạy trực tiếp:
powershell -File build-tools.ps1 logs
```

### Tìm dòng log webhook

```
[SePay Webhook] Received webhook payload from SePay
[Webhook] Thanh toán thành công orderId=1 sepayTxId=test-sepay-...
```

### Kiểm tra trong Database

```sql
-- Xem payment transactions
SELECT * FROM payment_transactions ORDER BY created_at DESC;

-- Xem dedup log
SELECT * FROM sepay_webhook_dedup ORDER BY created_at DESC;

-- Xem trạng thái đơn hàng
SELECT order_id, status FROM orders WHERE order_id = 1;
```

---

## 7. Troubleshooting

### ❌ Lỗi: Tunnel không chạy — "No tunnel credentials found"

```powershell
# Đăng nhập lại vào Cloudflare
cloudflared tunnel login

# Sau đó chạy lại
cloudflared tunnel run metafruit
```

### ❌ Lỗi: 404 Not Found tại webhook URL

**Nguyên nhân:** Tomcat chưa chạy hoặc app chưa deploy.

```
1. Mở build-tools.bat → chọn [0] Chạy toàn bộ quy trình
2. Đợi "BUILD SUCCESSFUL" và Tomcat khởi động
3. Thử lại URL
```

### ❌ Lỗi: "reference not found" trong log

**Nguyên nhân:** Mã MF trong nội dung CK không khớp với `payment_transactions`.

```sql
-- Kiểm tra xem đơn hàng đã có payment chưa
SELECT pt.*, o.status 
FROM payment_transactions pt
JOIN orders o ON pt.order_id = o.order_id
ORDER BY pt.created_at DESC;
```

### ❌ Lỗi: "AMOUNT_MISMATCH" — Số tiền không khớp

**Nguyên nhân:** Khách chuyển ít hơn tổng tiền đơn hàng.

**Cách xử lý:**
- Thông báo khách chuyển thêm
- Hoặc admin xác nhận thủ công trong trang Admin → Thanh toán

### ❌ Lỗi: CSRF token rejected cho webhook

**Đã được xử lý:** CsrfFilter đã bỏ qua `/api/*` endpoints.  
Xem: `CsrfFilter.java` dòng 45.

---

## 8. Cấu Hình Môi Trường Production

### 8.1 Set Environment Variables

Tạo file `.env` hoặc set trực tiếp trong hệ điều hành:

```bash
# Tomcat → catalina.bat / catalina.sh → thêm vào JAVA_OPTS
set JAVA_OPTS=-DAPP_DEPLOY_URL=https://your-real-domain.com

# Hoặc set env var
set APP_DEPLOY_URL=https://your-real-domain.com
set SEPAY_BANK_ID=MBBank
set SEPAY_ACCOUNT_NO=YOUR_REAL_ACCOUNT_NUMBER
set SEPAY_ACCOUNT_NAME=TEN_CONG_TY_BAN
```

### 8.2 Danh sách biến môi trường quan trọng

| Biến | Mô tả | Giá trị mặc định |
|---|---|---|
| `APP_DEPLOY_URL` | Domain public của app | `http://localhost:8080/Ban_Hoa_Qua_Online` |
| `SEPAY_BANK_ID` | Mã ngân hàng SePay | `MBBank` |
| `SEPAY_ACCOUNT_NO` | Số tài khoản SePay | (cần set) |
| `SEPAY_ACCOUNT_NAME` | Tên chủ tài khoản | `CONG TY TNHH METAFRUIT` |
| `DB_HOST` | SQL Server host | `localhost` |
| `DB_PASSWORD` | Mật khẩu DB | (cần set qua env) |

### 8.3 URL Webhook cho từng môi trường

```
[Development - Cloudflare Tunnel]
https://metafruit.xxx.workers.dev/Ban_Hoa_Qua_Online/api/payment/webhook

[Production - VPS/Server]
https://yourdomain.com/Ban_Hoa_Qua_Online/api/payment/webhook
```

### 8.4 Checklist trước khi go-live

- [ ] `APP_DEPLOY_URL` đã set đúng domain
- [ ] `SEPAY_ACCOUNT_NO` và `SEPAY_ACCOUNT_NAME` khớp với tài khoản thật
- [ ] Webhook URL đã được cấu hình trong SePay dashboard
- [ ] Test giao dịch thật 1.000đ → kiểm tra log
- [ ] `DB_PASSWORD` đã set qua env
- [ ] `SECRET_KEY` đã set để sign JWT

---

## 📌 Tóm Tắt Nhanh (Quick Reference)

```
1. Chạy Tomcat:     build-tools.bat → [0]
2. Chạy CF Tunnel:  build-tools.bat → [18]  hoặc: cloudflared tunnel run metafruit
3. Lấy tunnel URL:  cloudflared tunnel info metafruit
4. Điền SePay:      https://my.sepay.vn → Cài đặt → Webhook → điền URL ở bước 3
5. Test:            POST https://<tunnel>/Ban_Hoa_Qua_Online/api/payment/webhook
6. Xem log:         build-tools.bat → [5]
```

---

*📝 Tài liệu này được tạo tự động bởi Antigravity AI — Cập nhật: 2026-06-13*
