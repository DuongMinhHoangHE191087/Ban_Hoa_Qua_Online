# Phase 3: Order & Delivery (Shop Owner / Customer)

## Tổng quan
Quản lý vòng đời đơn hàng từ lúc đặt, xác nhận, giao hàng đến khi hoàn tất.

## Phân tích Code chi tiết

### 1. Estimated Delivery Time
- **Service:** src/java/service/delivery/DeliveryService.java
- **DAO:** src/java/dao/delivery/DeliveryDAO.java
- **Logic:** DeliveryService.updateEstimatedTime(). Tính toán thời gian giao dự kiến dựa trên vị trí địa lý hoặc cấu hình cứng.

### 2. Shop Approve/Reject Order
- **Servlet:** src/java/servlet/shop/order/ShopOrderServlet.java
- **Service:** src/java/service/order/OrderService.java
- **JSP:** web/WEB-INF/jsp/shop/order/shop-orders.jsp
- **Luồng Approve:** Cập nhật trạng thái PENDING -> CONFIRMED.
- **Luồng Reject:** Chuyển CANCELLED, gọi ProductDAO để *hoàn lại số lượng tồn kho* (Stock Refund) ngay lập tức.

### 3. Delivery Confirmation & Mark as Delivered
- **Servlet:** 
  - src/java/servlet/delivery/DeliveryUpdateAPI.java (Shipper update)
  - src/java/servlet/customer/order/OrderServlet.java (Customer confirm)
- **JSP:** web/WEB-INF/jsp/customer/order-detail.jsp
- **Logic:** Khi shipper nhấn giao xong -> DISPATCHED sang DELIVERED. Giao diện cập nhật Real-time.
