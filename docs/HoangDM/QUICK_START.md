# QUICK START GUIDE - Build & Deploy

## 🚀 5 Phút Setup

### Bước 1: Cấu hình lần đầu (1 lần duy nhất)
```bash
clean_build_run.bat --config
```
- Nhập đường dẫn **Java Home**: `C:\Program Files\Java\jdk-26.0.1`
- Nhập đường dẫn **Tomcat Home**: `C:\apache-tomcat-10.1.55`
- Nhập đường dẫn **Tomcat Instance**: (IDE directory)
- ✅ Lưu cấu hình

### Bước 2: Build & Deploy
```bash
clean_build_run.bat
```
**Quá trình tự động:**
- ✓ Kill Tomcat (xóa port 8080)
- ✓ Xóa cache toàn bộ
- ✓ Compile Java files
- ✓ Đồng bộ web files
- ✓ Khởi động Tomcat
- ✓ **Mở app tự động trong browser** 🎉
- ✓ Bắt đầu chế độ Watch

**Thời gian:** ~15-20 giây (🚀 **3x nhanh hơn NetBeans**)

### Bước 3: App tự động mở

Browser sẽ tự động mở:
```
http://localhost:8080/Ban_Hoa_Qua_Online/
```

Nếu muốn mở lại sau này:
```bash
build-tools.bat open
```

## 📝 Chế độ Watch (Theo dõi file)

Sau khi build xong, script tự động watch:

```
[14:30:15] Watch mode started
[14:30:20] Java file detected: UserService.java (Changed)
[14:30:22] Processing batch changes: Java files
[14:30:23] Java recompilation successful!
```

**Edit file → Tự động recompile → Refresh browser** ✨

**Dừng:** Nhấn `Ctrl+C`

## 🔧 Các lệnh hữu ích

| Lệnh | Tác dụng |
|------|---------|
| `clean_build_run.bat` | Build & Deploy lần đầu |
| `clean_build_run.bat --logs` | Xem log lỗi |
| `build-tools.bat open` | Mở app trong browser |
| `build-tools.bat status` | Kiểm tra trang thái Tomcat |
| `build-tools.bat kill-tomcat` | Kill Tomcat process |
| `build-tools.bat clean-all` | Xóa hết cache |
| `build-tools.bat reset` | Reset toàn bộ |

## ⚡ Performance vs NetBeans

| Tác vụ | NetBeans | v2.0 Script | Cải thiện |
|--------|----------|------------|----------|
| Full rebuild | 40-50s | 15-20s | **✅ 60% nhanh** |
| Compile Java | 8-12s | 2-4s | **✅ 70% nhanh** |
| Watch recompile | 5-8s | 1-2s | **✅ 75% nhanh** |
| Browser open | Manual | Auto ✨ | **✅ Tự động** |
| Cache cleanup | Manual | Auto ✨ | **✅ Tự động** |

**💡 Xem chi tiết:** [PERFORMANCE_COMPARISON.md](PERFORMANCE_COMPARISON.md)

## 🐛 Gặp lỗi?

### Lỗi 404
```bash
clean_build_run.bat --logs
```
→ Xem file `build_errors.log` → Sửa lỗi → Chạy lại

### Tomcat không khởi động
```bash
clean_build_run.bat --config
```
→ Cấu hình lại đường dẫn

### Port 8080 đang sử dụng
```bash
build-tools.bat kill-tomcat
```
→ Kill process đang chiếm port

### Cần reset toàn bộ
```bash
build-tools.bat reset
```
→ Xóa cache, lock, logs → Chạy `clean_build_run.bat` lại

## 📊 Cải tiến so với cũ

| Vấn đề | Cũ | Mới |
|-------|-----|-----|
| Lỗi 404 | Reload nhiều lần | ✅ Một lần ok |
| Cache cũ | Phải xóa thủ công | ✅ Tự động xóa |
| Edit file | Không update | ✅ Tự động recompile |
| Debug lỗi | Chỉ console | ✅ Lưu file log |
| Tomcat crash | Phải kill thủ công | ✅ Auto lock mechanism |

## 📚 Tài liệu đầy đủ

Xem file: `BUILD_AND_DEPLOY_GUIDE.md`

---

**Version**: 2.0 Quick Start
**Updated**: 2024-06-03
