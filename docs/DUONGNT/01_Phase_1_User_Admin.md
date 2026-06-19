# Phase 1: User Profile & Admin User Management

## Tổng quan
Phase 1 tập trung vào việc quản lý thông tin người dùng (Profile) và các tác vụ quản trị người dùng cơ bản của Admin.

## Phân tích Code chi tiết

### 1. View & Edit Profile
- **Servlet:** 
  - src/java/servlet/base/UserProfileServlet.java (Customer)
  - src/java/servlet/admin/user/AdminProfileServlet.java (Admin)
  - src/java/servlet/shop/shop/ShopProfileServlet.java (Shop)
- **Service:** src/java/service/auth/UserService.java
- **DAO:** src/java/dao/auth/UserDAO.java
- **JSP:** 
  - web/WEB-INF/jsp/customer/profile/user-profile.jsp
  - web/WEB-INF/jsp/admin/admin-profile.jsp
- **Luồng (Flow):** Lấy UserID từ Session -> UserService.getUserById() -> Trả về View. Khi update: form submit -> UserService.updateProfile() -> UserDAO.update().

### 2. Manage Addresses
- **Servlet:** src/java/servlet/customer/profile/AddressServlet.java
- **DAO:** src/java/dao/auth/UserAddressDAO.java (hoặc method trong UserDAO)
- **JSP:** web/WEB-INF/jsp/customer/profile/addresses.jsp
- **Logic:** Hỗ trợ 1 địa chỉ mặc định cho mỗi Customer để đơn giản hóa quá trình thanh toán (Checkout).

### 3. Admin Manage Users & Shop Owners
- **Servlet:** 
  - src/java/servlet/admin/user/AdminUserServlet.java
  - src/java/servlet/admin/shop/AdminShopManageServlet.java
- **JSP:** 
  - web/WEB-INF/jsp/admin/admin-users.jsp
  - web/WEB-INF/jsp/admin/admin-shops.jsp
- **Logic:** Lấy danh sách toàn bộ User/Shop. Phân trang dùng OFFSET/FETCH.

### 4. Block/Unblock & Approve Shop
- **Servlet (API):** 
  - src/java/servlet/admin/user/AdminUserStatusAPI.java
  - src/java/servlet/admin/shop/ShopApprovalAPI.java
- **Logic:** Dùng AJAX/Fetch API gọi lên Servlet. Service cập nhật cờ isBlocked hoặc status = APPROVED.
