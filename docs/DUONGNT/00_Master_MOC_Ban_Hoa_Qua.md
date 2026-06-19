# 00_Master_MOC_Ban_Hoa_Qua

Đây là Map of Content (MOC) tổng hợp phân tích toàn bộ source code của dự án **Bán Hoa Quả Online**. Tài liệu được chia theo **5 Phase** (phân bổ kiến trúc) và gom nhóm thành **3 Iterations** (phân bổ tiến độ thực tế).

## Tài liệu Kiến Trúc & Tổng Quan
- [[00_Master_Phan_Tich_Toan_Dien_Du_An|BÁCH KHOA TOÀN THƯ: Tổng Hợp Toàn Diện (Phase, Iteration, Source Code)]]
- [[00_Session_History_MOC|Nhật ký Phiên Trò Chuyện & Chốt Task DUONGNT]]
- [[00_Architecture_Overview|Tổng quan Kiến trúc Web App (Phân bố thư mục, luồng chạy)]]
- [[Deep_Dive_Kien_Truc_He_Thong|Bức Tranh Toàn Cảnh: Deep Dive Kiến Trúc & Vòng đời dữ liệu (NÊN ĐỌC KỸ)]]
- [[00_MVC_Flow_Explanation|Giải thích chi tiết luồng MVC cho người mới]]
- [[Kich_Ban_Review_Code|Kịch bản Review Code Admin (Dùng để trả lời Giảng viên)]]
- [[00_Huong_Dan_Review_Code_Thuc_Chien|MOC MỚI: Hướng dẫn 4 Bước Review Code Thực chiến cho 1 tính năng]]

## 1. Phân bổ theo 5 Phases (Kiến trúc gốc)
- [[01_Phase_1_User_Admin|Phase 1: User Profile & Admin User Management]]
- [[02_Phase_2_Global_Admin_Monitoring|Phase 2: Global Admin Monitoring]]
- [[03_Phase_3_Order_Delivery|Phase 3: Order & Delivery (Shop Owner / Customer)]]
- [[04_Phase_4_Review_Promotion|Phase 4: Product Review & Rating System]]
- [[05_Phase_5_Advanced_Admin|Phase 5: Advanced Admin & Shop Management]]

## 2. Phân bổ theo 3 Iterations (Tiến độ Project Tracking)
- [[00_3_Iterations_Summary|MOC: Tổng hợp nhanh 3 Iterations và liên kết Source Code (ĐẶC BIỆT CHÚ Ý ITERATION 2)]]

## Troubleshooting / Cẩm nang sửa lỗi thường gặp

### 1. Lỗi "java.lang.IllegalStateException: Timer already cancelled" và "Stop Tomcat Failed"
- **Triệu chứng:** Khi dev trên NetBeans và lưu file liên tục (tính năng Hot Deploy tự động kích hoạt), web bị văng lỗi 500 với thông báo "Timer already cancelled". Đồng thời khi nhấn nút Stop server Tomcat thì luôn báo Failed.
- **Nguyên nhân:** Lỗi do thư viện SQL Server JDBC Driver (`mssql-jdbc`) xung đột với cơ chế dọn dẹp các luồng (Thread/Timer) của Tomcat mỗi khi reload giữa chừng. Khi lỗi này xảy ra, tiến trình Java chạy ngầm bị treo (deadlock/zombie), khiến Tomcat mất khả năng tiếp nhận lệnh Stop từ NetBeans. Hơn nữa, cơ chế của Windows đã khóa (lock) các file liên quan khiến quá trình tắt gặp thất bại.
- **Cách khắc phục nhanh nhất (không cần restart máy):**
  1. Mở **Task Manager** (bấm tổ hợp phím `Ctrl + Shift + Esc`).
  2. Chuyển sang tab **Details** (Chi tiết), tìm đến tiến trình `java.exe`.
  3. Nhấn chuột phải và chọn **End task** để bắt buộc tắt tiến trình đang bị treo.
  4. Mở lại NetBeans (hoặc NetBeans vẫn đang mở thì nhấn chuột phải vào Project), chọn **Clean and Build** sau đó **Run** lại từ đầu. Lỗi sẽ lập tức biến mất.
