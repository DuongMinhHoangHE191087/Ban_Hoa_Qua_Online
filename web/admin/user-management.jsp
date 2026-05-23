<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý người dùng - Admin</title>
    <!-- Import CSS gốc của dự án -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-sidebar { width: 260px; background: var(--color-surface); border-right: 1px solid var(--color-border); padding: var(--space-4); }
        .admin-main { flex: 1; padding: var(--space-6); background: var(--color-bg); }
        .table-container { background: var(--color-surface); padding: var(--space-5); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
        .data-table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { padding: var(--space-3); text-align: left; border-bottom: 1px solid var(--color-border); }
        .data-table th { background: var(--color-bg); color: var(--color-text-secondary); }
        
        .btn-sm { padding: 6px 12px; font-size: 13px; border-radius: 4px; cursor: pointer; display: inline-block; border: none; transition: 0.2s;}
        .btn-warning { background: #f59e0b; color: white; }
        .btn-warning:hover { background: #d97706; }
        .btn-success { background: #10b981; color: white; }
        .btn-success:hover { background: #059669; }
        
        /* Nhãn trạng thái tài khoản hiển thị kiểu Badge */
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
        .badge-active { background: #d1fae5; color: #065f46; }
        .badge-suspended { background: #fee2e2; color: #991b1b; }
        .badge-inactive { background: #f3f4f6; color: #374151; }

        .msg-alert { padding: 12px 15px; border-radius: 4px; margin-bottom: 20px; font-weight: 500;}
        .msg-success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;}
        .msg-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca;}
    </style>
</head>
<body>
<div class="admin-layout">
    <!-- Sidebar -->
    <aside class="admin-sidebar">
        <div class="navbar__logo mb-4">
            <div class="logo-icon"><i class="fa-solid fa-leaf"></i></div>
            <div class="logo-text">Fruit<span class="text-highlight">Mkt</span> Admin</div>
        </div>
        <ul class="navbar__menu" style="flex-direction: column; align-items: flex-start; padding: 0;">
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/dashboard" class="menu-link" style="width: 100%;"><i class="fa-solid fa-chart-pie"></i> Dashboard</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/products" class="menu-link" style="width: 100%;"><i class="fa-solid fa-box"></i> Sản phẩm vi phạm</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/shops" class="menu-link" style="width: 100%;"><i class="fa-solid fa-store"></i> Duyệt gian hàng</a></li>
            <!-- Menu đang active sẽ có highlight -->
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/users" class="menu-link" style="width: 100%; background: rgba(77,102,28,0.1); color: var(--color-primary);"><i class="fa-solid fa-users"></i> Người dùng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/settlements" class="menu-link" style="width: 100%;"><i class="fa-solid fa-file-invoice"></i> Đối soát</a></li>
        </ul>
    </aside>

    <main class="admin-main">
        <h1 style="margin-bottom: var(--space-4);">Quản lý người dùng hệ thống</h1>
        
        <!-- Khối hiển thị thông báo thao tác -->
        <c:if test="${param.msg == 'success'}">
            <div class="msg-alert msg-success"><i class="fa-solid fa-check-circle"></i> Cập nhật trạng thái tài khoản thành công!</div>
        </c:if>
        <c:if test="${param.msg == 'error'}">
            <div class="msg-alert msg-error"><i class="fa-solid fa-circle-exclamation"></i> Có lỗi xảy ra trong quá trình cập nhật cơ sở dữ liệu.</div>
        </c:if>

        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Liên hệ</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Lặp qua danh sách lấy từ Servlet -->
                    <c:forEach var="user" items="${usersList}">
                        <tr>
                            <td>#${user.user_id}</td>
                            <td><strong>${user.full_name}</strong></td>
                            <td>
                                <div><i class="fa-solid fa-envelope" style="font-size: 12px; width: 14px;"></i> ${user.email}</div>
                                <div><i class="fa-solid fa-phone" style="font-size: 12px; width: 14px;"></i> ${user.phone}</div>
                            </td>
                            <td>
                                <!-- Chuyển đổi tên Vai trò hiển thị ra tiếng Việt -->
                                <c:choose>
                                    <c:when test="${user.role == 'CUSTOMER'}">Khách hàng</c:when>
                                    <c:when test="${user.role == 'SHOP_OWNER'}">Chủ gian hàng</c:when>
                                    <c:when test="${user.role == 'DELIVERY_STAFF'}">Shipper</c:when>
                                    <c:otherwise>${user.role}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <!-- Hiển thị Badge trạng thái tương ứng với màu -->
                                <c:choose>
                                    <c:when test="${user.status == 'ACTIVE'}"><span class="badge badge-active">Hoạt động</span></c:when>
                                    <c:when test="${user.status == 'SUSPENDED'}"><span class="badge badge-suspended">Đã bị khóa</span></c:when>
                                    <c:otherwise><span class="badge badge-inactive">${user.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <!-- Tùy theo trạng thái hiện tại mà hiển thị nút bấm tương ứng (Khóa / Mở Khóa) -->
                                <c:if test="${user.status == 'ACTIVE'}">
                                    <form action="${pageContext.request.contextPath}/admin/users/toggle-status" method="POST" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn KHÓA tài khoản này? (User sẽ không thể đăng nhập nữa)');">
                                        <input type="hidden" name="userId" value="${user.user_id}" />
                                        <input type="hidden" name="newStatus" value="SUSPENDED" />
                                        <button type="submit" class="btn-sm btn-warning" title="Khóa tài khoản"><i class="fa-solid fa-ban"></i> Khóa</button>
                                    </form>
                                </c:if>
                                
                                <c:if test="${user.status == 'SUSPENDED'}">
                                    <form action="${pageContext.request.contextPath}/admin/users/toggle-status" method="POST" style="display:inline;" onsubmit="return confirm('Cho phép tài khoản này hoạt động trở lại?');">
                                        <input type="hidden" name="userId" value="${user.user_id}" />
                                        <input type="hidden" name="newStatus" value="ACTIVE" />
                                        <button type="submit" class="btn-sm btn-success" title="Mở khóa"><i class="fa-solid fa-unlock"></i> Mở khóa</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
