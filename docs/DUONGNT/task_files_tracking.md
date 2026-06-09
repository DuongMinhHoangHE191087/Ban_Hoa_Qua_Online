# Task Files Tracking (Phase 5)

Tài liệu này ghi chú lại danh sách các file mã nguồn đã được tạo mới (New) hoặc chỉnh sửa (Modified) trong quá trình thực hiện Phase 5.

## 1. Category Management (Admin)
- *Đã hoàn thiện từ Phase trước, được tái sử dụng:*
  - `src/java/com/fruitmkt/servlet/admin/CategoryServlet.java`
  - `src/java/com/fruitmkt/dao/CategoryDAO.java`
  - `web/WEB-INF/jsp/admin/admin-categories.jsp`

## 2. System Config (Fee, Logo) (Admin)
- **[New]** `src/java/com/fruitmkt/servlet/admin/AdminConfigServlet.java`
- **[New]** `web/WEB-INF/jsp/admin/admin-config.jsp`
- *Tái sử dụng:* `src/java/com/fruitmkt/dao/SystemConfigDAO.java`

## 3. Detailed Order & Refund Status (System/Admin)
- **[Modified]** `src/java/com/fruitmkt/service/ReturnRequestService.java`
- **[Modified]** `src/java/com/fruitmkt/servlet/admin/AdminRefundServlet.java`
- **[Modified]** `web/WEB-INF/jsp/admin/admin-refunds.jsp`
- **[Modified]** `web/WEB-INF/jsp/admin/return-requests.jsp`
- *Tái sử dụng:* `src/java/com/fruitmkt/dao/OrderDAO.java` (đã hỗ trợ sẵn PREPARING, DISPATCHED, DELIVERED)
- *Tái sử dụng:* `web/WEB-INF/jsp/customer/orders.jsp` (đã có UI tương ứng)

## 4. Chat with Customer (Customer, Shop Owner)
- **[Created]** `src/java/com/fruitmkt/servlet/api/ChatAPI.java` (AJAX polling)
- **[Modified]** `src/java/com/fruitmkt/servlet/customer/ChatServlet.java`
- **[Created]** `src/java/com/fruitmkt/servlet/shop/ShopChatServlet.java`
- **[Created]** `web/WEB-INF/jsp/customer/chat.jsp`
- **[Created]** `web/WEB-INF/jsp/shop/chat.jsp`
- *Tái sử dụng:* `src/java/com/fruitmkt/dao/ChatDAO.java`

## 5. Refund Approval (Admin)
- *Đã hoàn thành cùng lúc với Task 3 thông qua các file:*
  - `src/java/com/fruitmkt/service/ReturnRequestService.java`
  - `src/java/com/fruitmkt/servlet/admin/AdminRefundServlet.java`
  - `web/WEB-INF/jsp/admin/admin-refunds.jsp`

## 6. Global Shop Management (Admin)
- **[Created]** `src/java/com/fruitmkt/servlet/admin/AdminShopManageServlet.java`
- **[Created]** `web/WEB-INF/jsp/admin/admin-shops.jsp`
- **[Modified]** `web/WEB-INF/jsp/common/admin-sidebar.jsp`

## 7. Admin Profile (Admin)
- **[Created]** `src/java/com/fruitmkt/servlet/admin/AdminProfileServlet.java`
- **[Created]** `web/WEB-INF/jsp/admin/admin-profile.jsp`
- *Tái sử dụng:* `src/java/com/fruitmkt/dao/UserDAO.java`

## 8. Force Logout All Sessions (Maintenance)
- **[Modified]** `src/java/com/fruitmkt/dao/UserDAO.java` (Thêm hàm `deleteAllSessions()`)
- **[Modified]** `src/java/com/fruitmkt/servlet/admin/AdminConfigServlet.java` (Xử lý `action=clearAllSessions`)
- **[Modified]** `web/WEB-INF/jsp/admin/admin-config.jsp` (Thêm giao diện Tác vụ bảo trì)

## 9. Bug Fixes & Encoding (Bảo trì)
- **[Modified]** `web/WEB-INF/jsp/shop/product-list.jsp` (Thêm dấu escape `\${...}` cho JavaScript template literal để tránh xung đột với JSP EL sau khi gộp code).
- **[Modified]** Khôi phục file `web/WEB-INF/jsp/common/admin-sidebar.jsp` về trạng thái nguyên bản để sửa lỗi mojibake bị double-encode, và cập nhật thêm tính năng Voucher sàn.
- **[Modified]** Thêm `pageEncoding="UTF-8"` thủ công vào các file:
  - `web/WEB-INF/jsp/common/alert.jsp`
  - `web/WEB-INF/jsp/common/error.jsp`
  - `web/WEB-INF/jsp/common/header.jsp`
  - `web/WEB-INF/jsp/common/profile.jsp`
- **[Modified]** `src/java/com/fruitmkt/service/ProductService.java` (Sửa lỗi trùng lặp phương thức gây lỗi Compile khiến Tomcat không nhận diện được các Servlet mới).
- **[Modified]** `src/java/com/fruitmkt/servlet/shop/PromotionServlet.java` (Chuyển sang dùng urlPatterns rõ ràng cho annotation WebServlet).
- **[Modified]** `src/java/com/fruitmkt/servlet/admin/AdminProductServlet.java` (Chuyển sang dùng urlPatterns rõ ràng cho annotation WebServlet).
- **[Modified]** `web/WEB-INF/jsp/common/admin-sidebar.jsp` (Sửa sai đường dẫn Yêu cầu đổi trả từ /admin/returns thành /admin/refunds).
- **[Modified]** `web/WEB-INF/jsp/common/header.jsp` (Xóa dòng khai báo pageEncoding bị trùng lặp gây ra lỗi 500 JasperException toàn trang).
- **[Modified]** `web/WEB-INF/jsp/common/error.jsp` (Xóa dòng khai báo pageEncoding bị trùng lặp gây ra lỗi 500 JasperException toàn trang).
