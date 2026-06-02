# Build và Deploy Guide - Ban Hoa Qua Online

## 📋 Mục đích

Scripts này được nâng cấp để giải quyết các vấn đề:
- ✅ Tránh lỗi 404 do cache cũ của Tomcat
- ✅ Phát hiện batch file thay đổi (múc file cùng lúc)
- ✅ Tự động rebuild toàn bộ dự án khi cần
- ✅ Logging chi tiết cho debugging
- ✅ Lock mechanism tránh rebuild trùng lặp
- ✅ Auto-detect đường dẫn Tomcat
- ✅ Kiểm tra tính sẵn sàng của server

## 🚀 Cách sử dụng

### 1. Lần đầu tiên - Cấu hình Tomcat

```bash
clean_build_run.bat --config
```

Nhập các thông tin:
- **Java Home**: Đường dẫn JDK (VD: `C:\Program Files\Java\jdk-26.0.1`)
- **Tomcat Home**: Đường dẫn Tomcat installation (VD: `C:\apache-tomcat-10.1.55`)
- **Tomcat Instance**: Đường dẫn instance của IDE (VD: `C:\Users\Admin\AppData\Local\JetBrains\...`)

### 2. Chạy Build & Deploy thông thường

```bash
clean_build_run.bat
```

hoặc double-click file `clean_build_run.bat`

**Quy trình thực hiện:**
1. **Kiểm tra & dừng Tomcat** - Kill toàn bộ process chiếm port 8080, 8005
2. **Xóa cache toàn diện** - work/, temp/, logs/, JSP cache
3. **Đồng bộ web files** - Robocopy từ web/ → build/web/
4. **Compile Java** - Biên dịch tất cả .java files
5. **Chuẩn bị Tomcat context** - Tạo Context XML
6. **Khởi động Tomcat** - Start Tomcat và chờ sẵn sàng
7. **Chế độ Watch** - Theo dõi thay đổi file để rebuild tự động

### 3. Xem log gần đây

```bash
clean_build_run.bat --logs
```

Hiển thị 3 loại log:
- **build_deploy.log** - Log chính
- **build_errors.log** - Lỗi chi tiết
- **build_debug.log** - Debug chi tiết

### 4. Debug mode

```bash
clean_build_run.bat --debug
```

Chế độ này sẽ in ra chi tiết toàn bộ quá trình.

## 📊 Các tính năng nâng cấp

### 🔄 Batch File Change Detection
- Script kiểm tra checksum của tất cả file trong `src/java` và `web/`
- Nếu phát hiện thay đổi → trigger rebuild
- Debounce 2 giây để tránh rebuild spam

### 🔒 Build Lock Mechanism
- File `build.lock` ngăn chặn rebuild trùng lặp
- Timeout 10 phút tự động xóa lock nếu process crash
- Chờ xong build trước rebuild lại

### 🗑️ Comprehensive Cache Cleanup
Xóa toàn bộ:
- `$CATALINA_BASE/work/` - Compiled JSP
- `$CATALINA_BASE/temp/` - Temporary files
- `$CATALINA_BASE/logs/` - Old logs
- `$CATALINA_BASE/webapps/Ban_Hoa_Qua_Online/` - Deployed app

### 📝 Detailed Logging
3 log files:
1. **build_deploy.log** - Thông tin chính, warnings, errors
2. **build_errors.log** - Chỉ lỗi (gửi AI dễ hơn)
3. **build_debug.log** - Debug trace (phát hiện vấn đề)

### 🔍 Smart File Watchers
- Giám sát `src/java/` → Trigger Java recompile
- Giám sát `web/` → Trigger web sync
- Debounce 2 giây → Tránh rebuild spam
- Batch changes → Compile cùng lúc nếu đổi 2 thư mục

### ⚙️ Auto-Detect Paths
Tự động tìm:
- Tomcat installation từ các đường dẫn phổ biến
- Tomcat instance từ IDE directories
- Fallback: Yêu cầu user nhập nếu không tìm thấy

### 🏥 Server Health Check
- Chờ port 8080 sẵn sàng (max 30s)
- Kiểm tra HTTP 200 response
- Xác minh ứng dụng accessible

## 📋 Files được tạo/sửa

### Trong project:
- ✏️ `clean_build_run.bat` - Batch wrapper (nâng cấp)
- ✏️ `clean_build_run.ps1` - PowerShell script chính (nâng cấp)
- 📄 `BUILD_AND_DEPLOY_GUIDE.md` - Hướng dẫn này (NEW)

### Khi chạy:
- 📝 `build_deploy.log` - Main log
- 🔴 `build_errors.log` - Error log (gửi AI)
- 🔵 `build_debug.log` - Debug log
- 🔒 `build.lock` - Lock file (tạm thời)
- 📄 `sources.txt` - List Java files
- 📄 `tomcat_config.ini` - Cấu hình Tomcat (nếu --config)

## ⚡ Watch Mode (Chế độ giám sát)

Sau khi build xong, script tự động chuyển sang **Watch Mode**:

```
[2024-06-03 14:30:15] [INFO] Watch mode started
[Watcher] Java file detected: UserService.java (Changed) at 14:30:20
[2024-06-03 14:30:22] [INFO] Processing batch changes: Java files at 14:30:22
[2024-06-03 14:30:23] [INFO] Java recompilation successful!
```

**Cách hoạt động:**
1. **Phát hiện thay đổi** → Java file thay đổi
2. **Debounce 2 giây** → Chờ không có file nào thay đổi
3. **Batch processing** → Compile tất cả files
4. **Auto-reload** → Tomcat tự động reload classes

**Dừng Watch Mode:** Nhấn `Ctrl+C`

## 🐛 Debugging

### Vấn đề: Lỗi 404 sau deploy
**Giải pháp:**
1. Xem log: `clean_build_run.bat --logs`
2. Kiểm tra `build_errors.log` cho lỗi compile
3. Xóa cache thủ công: Xóa thư mục `build/web/WEB-INF/classes`
4. Chạy lại: `clean_build_run.bat`

### Vấn đề: Build lock không release
**Giải pháp:**
1. Xóa file `build.lock` thủ công
2. Kill Tomcat: `clean_build_run.bat`
3. Chạy lại

### Vấn đề: Tomcat không khởi động
**Giải pháp:**
1. Cấu hình lại: `clean_build_run.bat --config`
2. Kiểm tra port 8080 có process khác chiếm: `netstat -ano | find "8080"`
3. Xem debug log: `clean_build_run.bat --logs`

### Vấn đề: Compile error không được show
**Giải pháp:**
1. Xem `build_errors.log` để xem lỗi chi tiết
2. Hoặc xem `build_deploy.log` để xem quy trình toàn bộ

## 📊 Performance Improvements

| Yếu tố | Cũ | Mới | Cải tiến |
|--------|-----|-----|----------|
| Cache cleanup | Chỉ work, temp | work, temp, logs, webapps | ✅ Toàn diện hơn |
| File detection | Từng file | Batch detect | ✅ Nhanh hơn |
| Rebuild spam | Có | Debounce 2s | ✅ Tránh spam |
| Logging | Chỉ console | 3 log files | ✅ Chi tiết hơn |
| Error capture | Giới hạn | Lưu file | ✅ Dễ debug |
| Lock mechanism | Không | 10min timeout | ✅ Tránh crash |
| Tomcat auto-detect | Hardcoded | Auto-detect | ✅ Linh hoạt |

## 🔧 Advanced Usage

### Chỉ compile Java (không sync web)
Sửa trong `clean_build_run.ps1`:
```powershell
$pendingChanges.Web = $false  # Bỏ qua web sync
```

### Thay đổi debounce interval
Tìm và sửa:
```powershell
$DebounceInterval = 2000  # milliseconds (mặc định: 2000 = 2s)
```

### Thay đổi build timeout
Tìm và sửa:
```powershell
$BuildTimeout = 600  # seconds (mặc định: 600 = 10 phút)
```

## 📞 Support

Nếu gặp lỗi:
1. Chạy: `clean_build_run.bat --logs`
2. Copy nội dung `build_errors.log` hoặc `build_debug.log`
3. Gửi cho developer hoặc AI để fix

---

**Version**: 2.0 (nâng cấp)
**Last Updated**: 2024-06-03
**Support**: Vi tiếng + English
