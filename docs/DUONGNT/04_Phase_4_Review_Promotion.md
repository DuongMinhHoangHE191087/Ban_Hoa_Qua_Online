# Phase 4: Product Review & Rating System

## Tổng quan
Hệ thống Đánh giá (Review), Xếp hạng sao (Rating) và Quản lý Khuyến mãi của Shop.

## Phân tích Code chi tiết

### 1. Write / Edit / Delete Review
- **Servlet:** src/java/servlet/customer/order/ReviewServlet.java
- **Service:** src/java/service/product/ReviewService.java
- **DAO:** src/java/dao/product/ReviewDAO.java
- **JSP:** web/WEB-INF/jsp/customer/order-reviews.jsp
- **Luồng:** Chỉ những đơn hàng có trạng thái DELIVERED mới hiển thị nút Đánh giá. Khách có thể Sửa/Xóa đánh giá của chính mình.

### 2. Average Rating Calculation
- **Logic:** Trigger mỗi khi có Review mới thông qua Service, SQL tính AVG(rating) group by ProductID để cập nhật số sao hiển thị ở danh sách sản phẩm.

### 3. Approve/Reject Reviews (Admin Moderation)
- **Servlet:** src/java/servlet/admin/product/AdminReviewAPI.java, src/java/servlet/admin/product/AdminReviewServlet.java
- **JSP:** web/WEB-INF/jsp/admin/review-management.jsp
- **Logic:** Admin có thể khóa các đánh giá vi phạm tiêu chuẩn cộng đồng (chuyển cờ isHidden=true).

### 4. Promotion Management
- **Servlet:** src/java/servlet/shop/promotion/PromotionServlet.java
- **DAO:** src/java/dao/shop/VoucherDAO.java (hoặc PromotionDAO)
- **JSP:** web/WEB-INF/jsp/shop/promotion.jsp
- **Logic:** Quản lý Mã giảm giá (Vouchers). Check logic: Ngày hết hạn, Số lượng mã, Loại giảm (%, Cố định).
