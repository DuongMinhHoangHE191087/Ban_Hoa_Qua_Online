# Phase 5: Advanced Admin & Shop Management

## Tổng quan
Các tính năng nâng cao, cấu hình hệ thống cốt lõi và tích hợp Real-time.

## Phân tích Code chi tiết

### 1. Category Management & System Config
- **Servlet:** 
  - src/java/servlet/admin/system/CategoryServlet.java
  - src/java/servlet/admin/system/AdminConfigServlet.java
- **JSP:** 
  - web/WEB-INF/jsp/admin/admin-categories.jsp
  - web/WEB-INF/jsp/admin/admin-config.jsp
- **Logic:** Quản lý Menu danh mục, Cấu hình % phí sàn, Logo, Tên App. Lưu trong file config hoặc Table SystemConfig.

### 2. Chat with Customer (Websocket)
- **Endpoint:** src/java/websocket/ChatEndpoint.java (Jakarta Websocket)
- **Servlet:** src/java/servlet/customer/chat/ChatServlet.java, src/java/servlet/shop/chat/ShopChatServlet.java
- **JSP:** web/WEB-INF/jsp/customer/chat.jsp, web/WEB-INF/jsp/shop/chat.jsp
- **Logic:** Quản lý Map các Session đang online. Gửi tin nhắn real-time giữa CustomerID và ShopID. Lưu lịch sử chat xuống DB để không bị mất khi F5.

### 3. Detailed Status & Global Shop
- **Logic:** Tách biệt rõ trạng thái Giao hàng (DeliveryStatus) và Trạng thái Thanh toán (PaymentStatus) thay vì gộp chung vào 1 cột OrderStatus. Cấu trúc cơ sở dữ liệu và DAO mapping được phân loại rõ ràng.
