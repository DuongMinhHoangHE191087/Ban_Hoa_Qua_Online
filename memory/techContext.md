# Technical Context - MeteFruit Nông Sản Sạch

## 🛠 Kiến trúc & Công nghệ
- **Backend:** Java 17, Jakarta Servlet 6 (Tomcat 10), JDBC kết nối SQL Server.
- **Frontend:** JSP, JSTL 3.0, Vanilla CSS, Tailwind CSS (dynamic CDN).
- **Build tool:** Ant (NetBeans build system).

## ⚠️ Các quyết định kỹ thuật & Giải pháp quan trọng (Important Decisions)
1. **Lỗi Jackson Java 8 Date/Time:** Jackson ObjectMapper mặc định không hỗ trợ LocalDate/LocalDateTime của Java 8. Quyết định: Map các entity sang Map đơn giản (String, Integer, BigDecimal) trước khi trả về JSON qua `JsonUtil.writeJson` trong servlet. Đây là giải pháp an toàn 100%, tránh crash runtime 500 do định nghĩa kiểu.
2. **ES6 Template Literals trong JSP:** Các biểu thức `${}` trong Javascript bọc bởi dấu backtick sẽ bị Apache Jasper parse nhầm thành JSP EL và ném ra lỗi ELException. Quyết định: Escape tất cả thành `\${}` để Jasper bỏ qua ở server-side.
3. **Tính toán VND tránh sai số Float:** Quy đổi đơn vị cân nặng thành Grams và thực hiện tính toán số nguyên bằng `Math.round(Number(v.price))` trước khi nhân/chia để tránh Float rounding error.
