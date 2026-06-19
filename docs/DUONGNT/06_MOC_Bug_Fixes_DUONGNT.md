---
title: "MOC - Tổng Hợp Sửa Lỗi (Bug Fixes) - DUONGNT"
date: "2026-06-18"
tags:
  - MOC
  - BugFix
  - DUONGNT
  - Admin
  - ShopApproval
  - Authentication
---

# MOC: TỔNG HỢP SỬA LỖI VÀ TỐI ƯU HÓA (DUONGNT)

Tài liệu này tổng hợp các lỗi phát sinh trong quá trình kiểm thử phần việc của **DUONGNT** (Tài liệu tham chiếu từ file `Lỗi .md`) và các giải pháp đã được áp dụng vào mã nguồn.

## 1. Lỗi Xóa Phiên Đăng Nhập (Logout Error)
### Mô tả Lỗi
- **Triệu chứng:** Khi người dùng nhấn Đăng xuất, hệ thống trả về lỗi **Server Error 500** (`IllegalStateException: Cannot create a session after the response has been committed`).
- **Nguyên nhân gốc rễ:** Servlet thực hiện `session.invalidate()` (xóa session hiện tại), sau đó lại cố gắng tạo một session mới `req.getSession(true)` để lưu thông báo (Flash Message) thành công và redirect. Việc tạo session sau khi response chuẩn bị commit gây ra ngoại lệ.

### Giải pháp Kỹ thuật Đã Triển Khai
1. **[LogoutServlet.java](../../src/java/servlet/auth/LogoutServlet.java):**
   - Loại bỏ hoàn toàn việc tạo session mới trong `LogoutServlet`.
   - Thay đổi cơ chế thông báo bằng cách truyền thêm Query Parameter vào URL khi điều hướng: `resp.sendRedirect(req.getContextPath() + "/auth/login?logout=success");`.
2. **[LoginServlet.java](../../src/java/servlet/auth/LoginServlet.java):**
   - Bổ sung logic kiểm tra tham số: Nếu tồn tại tham số `logout=success`, hệ thống sẽ thiết lập câu thông báo "Đăng xuất tài khoản thành công!" truyền vào `req.setAttribute` để trang JSP hiển thị.

---

## 2. Lỗi Chức năng Phê duyệt Cửa hàng (Shop Approval)
### Mô tả Lỗi và Yêu cầu Bổ sung
Dựa trên phản hồi từ nhóm kiểm thử và thiết kế:
- **Trường Email bị thiếu:** Form nộp đơn không lưu lại "Email liên hệ kinh doanh" như trong thiết kế, gây khó khăn cho Admin khi duyệt đơn.
- **Trải nghiệm duyệt đơn (UI/UX):** Bảng danh sách yêu cầu cần có tính năng "Xem chi tiết" qua cửa sổ Pop-up (Modal) trước khi Admin đưa ra quyết định duyệt/từ chối. Cần bao gồm Tên, Email, Địa chỉ, Danh mục kinh doanh và Tệp đính kèm.

### Giải pháp Kỹ thuật Đã Triển Khai
1. **Bổ sung Trường Business Email:**
   - **Giao diện ([shop-apply.jsp](../../web/WEB-INF/jsp/customer/shop-apply.jsp)):** Bổ sung trường input cho `businessEmail` ngang hàng với "Tên cửa hàng".
   - **Backend ([ShopApplyServlet.java](../../src/java/servlet/customer/shop/ShopApplyServlet.java)):** Lấy `req.getParameter("businessEmail")` và cập nhật vào `ShopProfile` (hỗ trợ cả hai trường hợp Nộp mới và Nộp lại).
2. **Tạo API Cung cấp Dữ liệu Chi tiết:**
   - **[AdminShopDetailAPI.java](../../src/java/servlet/admin/shop/AdminShopDetailAPI.java):** Xây dựng API mới dùng phương thức `GET` (`/api/admin/shops/detail`). Trả về cấu trúc JSON đầy đủ về thông tin của shop bao gồm chuỗi JSON chứa các `categoryId` và `docPaths`.
3. **Cập nhật Giao diện Quản trị ([shop-approvals.jsp](../../web/WEB-INF/jsp/admin/shop-approvals.jsp)):**
   - **Controller ([ShopApprovalServlet.java](../../src/java/servlet/admin/shop/ShopApprovalServlet.java)):** Truyền danh sách `categories` hoạt động xuống request để JSP có thể ánh xạ tên danh mục từ ID.
   - **View:** Thêm nút "Xem chi tiết" (icon kính lúp/mắt) vào cột Hành động.
   - **Pop-up Modal:** Thêm mã HTML và JavaScript thực hiện tính năng gọi API qua `fetch()`, phân tích cú pháp JSON mảng danh mục / tài liệu đính kèm và đổ dữ liệu động lên Modal. Bao gồm cả các liên kết có thể click trực tiếp để xem/tải tài liệu kinh doanh.

## 3. Liên kết liên quan
- [[00_Session_History_MOC]] - Nhật ký các phiên thảo luận.
- [[05_MOC_Iteration_2]] - MOC quản lý chức năng duyệt đơn.
- [[task_tracking]] - Danh sách phân công công việc.
