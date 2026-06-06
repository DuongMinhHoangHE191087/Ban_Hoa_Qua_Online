# Danh sách các file đã tạo/sửa đổi (Phase 1 - Phase 4)

Tài liệu này tổng hợp toàn bộ các file mà nhánh DUONGNTHE191504 đã thực hiện để hoàn thành từ Phase 1 đến Phase 4 theo yêu cầu của dự án.

## 🛡️ PHASE 1: Quản lý Người dùng (User Profile & Admin)

**Tầng Giao diện (JSP):**
- `web/WEB-INF/jsp/auth/login.jsp`
- `web/WEB-INF/jsp/admin/user-list.jsp`
- `web/WEB-INF/jsp/admin/user-edit.jsp`

**Tầng Điều khiển & Xử lý (Servlet / API):**
- `src/java/com/fruitmkt/servlet/admin/AdminUserStatusAPI.java`
- `src/java/com/fruitmkt/servlet/admin/ShopApprovalServlet.java`
- `src/java/com/fruitmkt/servlet/admin/ShopApprovalAPI.java`

**Tầng Database (DAO):**
- `src/java/com/fruitmkt/dao/UserDAO.java`
- `src/java/com/fruitmkt/dao/ShopProfileDAO.java`

---

## 📊 PHASE 2: Giám sát Hệ thống (Global Admin Monitoring)

**Tầng Giao diện (JSP):**
- `web/WEB-INF/jsp/admin/dashboard.jsp`
- `web/WEB-INF/jsp/admin/admin-orders.jsp`
- `web/WEB-INF/jsp/admin/admin-settlements.jsp`
- `web/WEB-INF/jsp/admin/admin-refunds.jsp`
- `web/WEB-INF/jsp/admin/admin-notifications.jsp`
- `web/WEB-INF/jsp/admin/coming-soon.jsp`

**Tầng Điều khiển & Xử lý (Servlet / API):**
- `src/java/com/fruitmkt/servlet/admin/AdminDashboardServlet.java`
- `src/java/com/fruitmkt/servlet/admin/AdminOrderServlet.java`
- `src/java/com/fruitmkt/servlet/admin/AdminSettlementServlet.java`
- `src/java/com/fruitmkt/servlet/admin/AdminRefundServlet.java`

---

## 🚚 PHASE 3: Luồng Giao hàng (Order & Delivery Flow)

**Tầng Giao diện (JSP):**
- `web/WEB-INF/jsp/shop/orders.jsp`
- `web/WEB-INF/jsp/delivery/dashboard.jsp`
- `web/WEB-INF/jsp/customer/orders.jsp`

**Tầng Điều khiển & Xử lý (Servlet / API / Service):**
- `src/java/com/fruitmkt/servlet/shop/ShopOrderServlet.java`
- `src/java/com/fruitmkt/servlet/delivery/DeliveryDashboardServlet.java`
- `src/java/com/fruitmkt/servlet/delivery/DeliveryUpdateAPI.java`
- `src/java/com/fruitmkt/servlet/customer/OrderServlet.java`
- `src/java/com/fruitmkt/service/OrderService.java`
- `src/java/com/fruitmkt/service/DeliveryService.java`

**Tầng Database (DAO):**
- `src/java/com/fruitmkt/dao/OrderDAO.java`
- `src/java/com/fruitmkt/dao/DeliveryDAO.java`

---

## ⭐ PHASE 4: Đánh giá Sản phẩm (Product Review & Rating)

**Tầng Giao diện (JSP):**
- `web/WEB-INF/jsp/customer/order-reviews.jsp`
- `web/WEB-INF/jsp/customer/review-submit.jsp`

**Tầng Điều khiển & Xử lý (Servlet / API):**
- `src/java/com/fruitmkt/servlet/customer/OrderReviewListServlet.java`
- `src/java/com/fruitmkt/servlet/customer/ReviewServlet.java`
- `src/java/com/fruitmkt/servlet/admin/AdminReviewAPI.java`

**Tầng Database (DAO):**
- `src/java/com/fruitmkt/dao/OrderItemDAO.java`
- `src/java/com/fruitmkt/dao/ReviewDAO.java`
- `src/java/com/fruitmkt/dao/ProductDAO.java`

---

## 📝 Tài liệu & Báo cáo
- `docs/DUONGNT/task_tracking.md`
- `docs/DUONGNT/03_Phase_1_to_3_Summary.html`
- `docs/DUONGNT/04_FileList_Phase_1_to_4.md`
- `docs/DUONGNT/05_Phase_4_Summary.html`
- `docs/DUONGNT/06_Phase_1_to_4_Full_Summary.html`
