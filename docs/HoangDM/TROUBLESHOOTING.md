# TROUBLESHOOTING GUIDE - Build & Deploy

## 🔴 Vấn đề & Giải pháp

### 1. Lỗi 404 sau deploy

**Triệu chứng:**
```
HTTP 404 - Page not found
/Ban_Hoa_Qua_Online/
```

**Nguyên nhân:**
- Cache JSP cũ của Tomcat
- Classes không được compile
- Context XML không được tạo

**Giải pháp:**
```bash
# Option 1: Clean & rebuild
clean_build_run.bat

# Option 2: Chỉ xóa cache
build-tools.bat clean-all
clean_build_run.bat

# Option 3: Reset toàn bộ
build-tools.bat reset
clean_build_run.bat
```

---

### 2. Tomcat không khởi động

**Triệu chứng:**
```
Waiting for Tomcat to start (max 30 seconds)...
Tomcat port 8080 is not ready
```

**Nguyên nhân:**
- Đường dẫn Tomcat sai
- Port 8080 đang bị chiếm
- JAVA_HOME không hợp lệ

**Giải pháp:**

**Bước 1:** Kiểm tra port
```bash
build-tools.bat status
```
Nếu `Port 8080: IN USE` → Kill process:
```bash
build-tools.bat kill-tomcat
```

**Bước 2:** Cấu hình lại Tomcat
```bash
clean_build_run.bat --config
```
Nhập đúng đường dẫn:
- Tomcat Home (nơi cài đặt Tomcat)
- Tomcat Instance (nơi IDE chạy Tomcat)

**Bước 3:** Chạy lại
```bash
clean_build_run.bat
```

---

### 3. Lỗi compile Java

**Triệu chứng:**
```
=================== COMPILATION ERRORS ===================
[compile errors...]
========================================================
Java compilation failed!
```

**Giải pháp:**

**Bước 1:** Xem lỗi chi tiết
```bash
clean_build_run.bat --logs
```
Mở file `build_errors.log` → Xem lỗi

**Bước 2:** Phổ biến nhất:
- Import sai: `cannot find symbol`
- Syntax error: `] expected`
- Type mismatch: `incompatible types`

**Bước 3:** Fix lỗi
- Sửa code trong IDE
- Lưu file
- Watch mode sẽ tự động recompile

---

### 4. Port 8080/8005 đang bị chiếm

**Triệu chứng:**
```
[WARN] Found process on port 8080. Force killing...
```

**Nguyên nhân:**
- Tomcat đang chạy từ phiên trước
- Process khác chiếm port
- IDE debug process

**Giải pháp:**

```bash
# Option 1: Tự động kill
build-tools.bat kill-tomcat

# Option 2: Manual kill (PowerShell Admin)
Stop-Process -Name java -Force
Stop-Process -Name javaw -Force

# Option 3: Tìm process chiếm port
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

### 5. Build lock stuck

**Triệu chứng:**
```
Build is in progress. Waiting...
(waits forever)
```

**Nguyên nhân:**
- Process previous build crash
- File `build.lock` không được release
- Script bị interrupt (Ctrl+C)

**Giải pháp:**

```bash
# Xóa lock file
del build.lock

# Hoặc dùng build-tools
build-tools.bat reset
```

---

### 6. Watch mode không detect file thay đổi

**Triệu chứng:**
```
Edit file → Không tự động compile
```

**Nguyên nhân:**
- File watcher bị crash
- Debounce interval quá dài
- File path không hợp lệ

**Giải pháp:**

```bash
# Dừng (Ctrl+C) và chạy lại
clean_build_run.bat

# Nếu vẫn không được:
build-tools.bat reset
clean_build_run.bat
```

---

### 7. Lỗi Robocopy sync

**Triệu chứng:**
```
Robocopy sync failed with exit code 16
```

**Nguyên nhân:**
- File bị lock bởi process khác
- Permission denied
- Disk full

**Giải pháp:**

```bash
# Kill Tomcat để release lock
build-tools.bat kill-tomcat

# Clean & rebuild
build-tools.bat clean-all
clean_build_run.bat
```

---

### 8. JSP không update

**Triệu chứng:**
```
Edit JSP file → Browser vẫn hiển thị cũ
```

**Nguyên nhân:**
- JSP cache của Tomcat
- Browser cache
- Tomcat reloadable=false

**Giải pháp:**

```bash
# Xóa JSP cache
build-tools.bat clean-all

# Rebuild
clean_build_run.bat

# Browser: Ctrl+Shift+Delete → Xóa cache
```

---

### 9. Database connection error

**Triệu chứng:**
```
java.sql.SQLException: Cannot get JDBC Connection
```

**Nguyên nhân:**
- Database driver missing
- Connection string sai
- Database không chạy

**Giải pháp:**

```bash
# 1. Kiểm tra database driver trong web/WEB-INF/lib/
ls build/web/WEB-INF/lib/ | grep -i mysql

# 2. Xem log chi tiết
build-tools.bat logs

# 3. Kiểm tra connection string (ở web.xml hoặc Java class)

# 4. Rebuild
clean_build_run.bat
```

---

### 10. Lỗi OutOfMemoryException

**Triệu chứng:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Nguyên nhân:**
- Tomcat memory quá nhỏ
- Memory leak trong code
- Cache không được clear

**Giải pháp:**

```bash
# Xóa cache
build-tools.bat clean-all

# Tăng memory cho Tomcat
# Sửa $CATALINA_HOME/bin/catalina.bat hoặc setenv.bat
# Thêm: set JAVA_OPTS=-Xmx1024m -Xms512m

# Rebuild
clean_build_run.bat
```

---

### 11. Servlet mapping không hoạt động

**Triệu chứng:**
```
404 Not Found
/Ban_Hoa_Qua_Online/ServletName
```

**Nguyên nhân:**
- web.xml config sai
- Servlet path sai
- Classes không được compile

**Giải pháp:**

```bash
# 1. Kiểm tra web.xml
cat build/web/WEB-INF/web.xml | grep -A5 servlet

# 2. Rebuild classes
build-tools.bat clean-all
clean_build_run.bat

# 3. Kiểm tra servlet class compiled
ls build/web/WEB-INF/classes/com/fruitmkt/*/
```

---

### 12. IDE không nhận changes

**Triệu chứng:**
```
Edit file in IDE → Console hiện recompile → IDE vẫn error
```

**Nguyên nhân:**
- IDE project structure sai
- IDE cache
- Output directory khác

**Giải pháp:**

```bash
# 1. Reload project trong IDE
# NetBeans: Right-click project → Clean and Build

# 2. Hoặc từ command line
clean_build_run.bat

# 3. Refresh IDE: F5 (NetBeans), Ctrl+Shift+R (Eclipse)
```

---

## 📋 Checklist Khi Gặp Vấn đề

```
□ Chạy: clean_build_run.bat --logs
□ Xem file: build_errors.log
□ Xem file: build_debug.log
□ Kiểm tra trang thái: build-tools.bat status
□ Kill Tomcat: build-tools.bat kill-tomcat
□ Xóa cache: build-tools.bat clean-all
□ Reset: build-tools.bat reset
□ Chạy lại: clean_build_run.bat
```

---

## 📝 Log Files để Share với AI

Khi gặp lỗi, copy nội dung từ:
1. `build_errors.log` - Lỗi chính
2. `build_debug.log` - Debug trace
3. Console output

**Cách share:**
```bash
# 1. Xem lỗi
clean_build_run.bat --logs

# 2. Copy nội dung build_errors.log
# 3. Gửi cho AI cùng đặc tả vấn đề
```

---

## 🔍 Advanced Debugging

### Bật Tomcat debug mode
```bash
# Sửa $CATALINA_HOME/bin/setenv.bat
set JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```
Sau đó IDE có thể attach debugger.

### Xem compile log chi tiết
```bash
# PowerShell
cat build_debug.log | Select-String "compile" -A5

# CMD
findstr "compile" build_debug.log
```

### Monitor file changes
```bash
# Xem files bị thay đổi
Get-ChildItem -Path "src/java", "web" -Recurse | 
  Where-Object { $_.LastWriteTime -gt (Get-Date).AddMinutes(-5) }
```

---

## 💡 Best Practices

1. **Trước khi report lỗi:**
   - Chạy `build-tools.bat reset`
   - Chạy `clean_build_run.bat` lại
   - Nếu vẫn lỗi → collect logs

2. **Khi edit code:**
   - Chỉ edit file trong IDE (không trực tiếp edit build/web)
   - Để watch mode tự động rebuild
   - Refresh browser sau edit

3. **Khi gặp build error:**
   - Đừng kill script ngay
   - Xem log để hiểu lỗi
   - Fix code → Tự động recompile
   - Hoặc Ctrl+C → Edit → Chạy lại

4. **Dọn dẹp định kỳ:**
   - Mỗi tuần: `build-tools.bat clean-logs`
   - Mỗi tháng: `build-tools.bat clean-all`
   - Khi máy slow: `build-tools.bat reset`

---

**Version**: 2.0
**Last Updated**: 2024-06-03
**Support**: Vietnamese
