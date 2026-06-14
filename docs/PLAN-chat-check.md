# Kế hoạch kiểm tra và hoàn thiện tính năng Chat (PLAN-chat-check.md)

Tài liệu này đề xuất kế hoạch kiểm tra toàn diện hệ thống chat real-time bằng WebSockets, xác định các điểm còn thiếu (missing features), các lỗi bảo mật tiềm ẩn và lộ trình tối ưu hóa.

---

## 1. Các điểm cần kiểm tra (Diagnostics Checklist)

### A. Bảo mật (Security & CSRF)
- `[ ]` **CSRF Token Validation**: Các endpoint API POST tại [ChatAPI.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/servlet/api/chat/ChatAPI.java) (`sendMessage`, `createAdminSession`) và [ChatMediaUploadServlet.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/src/java/servlet/api/chat/ChatMediaUploadServlet.java) hiện tại có bỏ qua kiểm tra CSRF token không?
- `[ ]` **Quyền truy cập Session (Auth/Role Checks)**: Xác minh việc phân quyền trong `ChatServlet`, `ShopChatServlet` và `AdminChatServlet` có ngăn chặn người dùng thường xem trộm tin nhắn của Shop khác hoặc Admin không.
- `[ ]` **Media Upload Validation**: Xác minh validation phần mở rộng file (extension) và mime type trong `ChatMediaUploadServlet` có an toàn chống tải file độc hại (RCE shell) không.

### B. Kiểm thử hiệu năng & Rò rỉ bộ nhớ (Memory Leaks & Reliability)
- `[ ]` **Kiểm tra rò rỉ session WebSocket**: Xác thực class [ChatEndpointSessionLeakTest.java](file:///d:/DMHoang/Project_GitHub/Ban_Hoa_Qua_Online/test/test/ChatEndpointSessionLeakTest.java) có chạy và pass để đảm bảo cơ chế dọn dẹp `ROOM_MAP` khi đóng kết nối/có lỗi.
- `[ ]` **Tự động kết nối lại (Auto-reconnect)**: Kiểm tra khả năng tự động reconnect trên Client khi mất mạng.
- `[ ]` **HTTP Fallback**: Kiểm tra xem cơ chế gửi tin nhắn bằng HTTP POST có hoạt động trơn tru khi WebSocket bị lỗi hoặc bị chặn không.

### C. Giao diện & Trải nghiệm (UI/UX)
- `[ ]` **Đồng bộ hóa tên hiển thị Admin (Masking)**: Đảm bảo tên thật của Admin không bị lộ trên giao diện Chat của Customer và Shop Owner (luôn hiển thị "Hỗ trợ Admin").
- `[ ]` **Preview file tải lên & Tiến trình upload**: Kiểm tra hiển thị thanh tiến trình (%) và preview ảnh/video trực quan trên UI trước khi gửi.

---

## 2. Các chức năng còn thiếu & Đề xuất bổ sung

### A. Trạng thái tin nhắn đã đọc thời gian thực (Real-time Read Receipt)
- **Hiện tại**: Cờ `is_read` trong DB chỉ được cập nhật khi người dùng load lại lịch sử chat qua `GET /api/chat?action=getMessages`.
- **Đề xuất**: Gửi gói tin nhắn kiểu `{"type":"READ","messageId":X}` qua WebSocket để cập nhật trạng thái "Đã xem" thời gian thực trên màn hình người gửi.

### B. Chỉ báo đang soạn tin (Typing Indicator)
- **Hiện tại**: Chưa có phản hồi khi đối phương đang gõ tin nhắn.
- **Đề xuất**: Gửi gói tin `{"type":"TYPING","isTyping":true/false}` qua WebSocket khi người dùng tương tác với trường nhập liệu (`textarea`).

### C. Giới hạn dung lượng tải lên phía Client (Client-side Upload Restrictions)
- **Hiện tại**: `ChatMediaUploadServlet` có cấu hình giới hạn tối đa 50MB. Tuy nhiên phía Client chưa kiểm tra dung lượng file trước khi gửi XHR, gây tốn băng thông và trải nghiệm kém nếu người dùng chọn file quá lớn.
- **Đề xuất**: Thêm kiểm tra kích thước file phía Client (Ví dụ: tối đa 5MB cho ảnh, 30MB cho video) trong JS trước khi gửi request tải lên.

---

## 3. Kế hoạch thực hiện (Implementation Roadmap)

### Pha 1: Chạy toàn bộ Unit Tests & Kiểm tra an toàn bảo mật
1. Chạy tất cả test suite `test.ChatServiceTest`, `test.ChatEndpointSessionLeakTest`.
2. Kiểm tra log lỗi compile hoặc test fail nếu có.
3. Rà soát CSRF Filter đối với các API Endpoint của Chat.

### Pha 2: Thiết kế và tích hợp CSRF Protection cho HTTP Fallback
- Tích hợp gửi kèm CSRF Token (`X-CSRF-Token` header hoặc tham số body) khi gọi POST đến `/api/chat` và `/api/chat/upload`.

### Pha 3: Nâng cấp trải nghiệm Client-side
- Thêm kiểm tra kích thước file và định dạng file ngay trong JavaScript (`web/assets/js/components/chat.js` hoặc tag `<script>` trực tiếp trong JSP).
- Tích hợp thêm Micro-animations hoặc thay đổi nhỏ để tăng trải nghiệm mượt mà.
