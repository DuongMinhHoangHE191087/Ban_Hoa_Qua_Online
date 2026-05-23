<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Duyệt gian hàng - Admin</title>
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
        
        /* CSS cho các nút chức năng nhỏ trong bảng */
        .btn-sm { padding: 6px 12px; font-size: 13px; border-radius: 4px; cursor: pointer; display: inline-block; }
        .btn-success { background: #10b981; color: white; border: none; transition: 0.2s; }
        .btn-success:hover { background: #059669; }
        .btn-danger { background: #ef4444; color: white; border: none; transition: 0.2s; }
        .btn-danger:hover { background: #dc2626; }
        
        /* CSS cho thông báo Alert */
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
            <!-- Menu đang active sẽ có highlight -->
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/shops" class="menu-link" style="width: 100%; background: rgba(77,102,28,0.1); color: var(--color-primary);"><i class="fa-solid fa-store"></i> Duyệt gian hàng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/users" class="menu-link" style="width: 100%;"><i class="fa-solid fa-users"></i> Người dùng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/settlements" class="menu-link" style="width: 100%;"><i class="fa-solid fa-file-invoice"></i> Đối soát</a></li>
        </ul>
    </aside>

    <main class="admin-main">
        <h1 style="margin-bottom: var(--space-4);">Duyệt gian hàng đăng ký mới</h1>
        
        <!-- Khối hiển thị thông báo trả về từ Controller qua param URL -->
        <c:if test="${param.msg == 'approve_success'}">
            <div class="msg-alert msg-success"><i class="fa-solid fa-check-circle"></i> Đã phê duyệt gian hàng thành công! Tài khoản đã được tự động cấp quyền Shop Owner.</div>
        </c:if>
        <c:if test="${param.msg == 'reject_success'}">
            <div class="msg-alert msg-success"><i class="fa-solid fa-check-circle"></i> Đã từ chối gian hàng thành công!</div>
        </c:if>
        <c:if test="${param.msg == 'error'}">
            <div class="msg-alert msg-error"><i class="fa-solid fa-circle-exclamation"></i> Có lỗi xảy ra trong quá trình xử lý với Cơ sở dữ liệu.</div>
        </c:if>

        <div class="table-container">
            <!-- Hiển thị khi không có danh sách -->
            <c:if test="${empty pendingShops}">
                <div style="text-align: center; padding: 40px; color: var(--color-text-secondary);">
                    <i class="fa-solid fa-inbox fa-3x" style="margin-bottom: 10px; opacity: 0.5;"></i>
                    <p>Hiện không có gian hàng nào đang chờ duyệt.</p>
                </div>
            </c:if>

            <!-- Hiển thị khi có dữ liệu -->
            <c:if test="${not empty pendingShops}">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Ngày ĐK</th>
                            <th>Tên Shop</th>
                            <th>Mô tả kinh doanh</th>
                            <th>Thông tin chủ shop</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="shop" items="${pendingShops}">
                            <tr>
                                <td><fmt:formatDate value="${shop.created_at}" pattern="dd/MM/yyyy HH:mm" /></td>
                                <td><strong>${shop.shop_name}</strong></td>
                                <!-- Nội dung mô tả cắt ngắn nếu quá dài -->
                                <td style="max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${shop.shop_description}">
                                    ${shop.shop_description}
                                </td>
                                <td>
                                    <div><i class="fa-solid fa-user"></i> ${shop.full_name}</div>
                                    <div><i class="fa-solid fa-envelope"></i> ${shop.email}</div>
                                    <div><i class="fa-solid fa-phone"></i> ${shop.phone}</div>
                                </td>
                                <td>
                                    <!-- Nút Duyệt: Bọc trong thẻ Form gọi phương thức POST để bảo mật thay vì thẻ A -->
                                    <form action="${pageContext.request.contextPath}/admin/shops/approve" method="POST" style="display:inline;" onsubmit="return confirm('Bạn chắc chắn muốn DUYỆT shop này chứ?');">
                                        <input type="hidden" name="profileId" value="${shop.profile_id}" />
                                        <button type="submit" class="btn-sm btn-success" title="Phê duyệt"><i class="fa-solid fa-check"></i> Duyệt</button>
                                    </form>
                                    
                                    <!-- Nút Từ chối: Gọi hàm JS để mở Popup nhập lý do -->
                                    <button type="button" class="btn-sm btn-danger" onclick="rejectShop(${shop.profile_id})" title="Từ chối"><i class="fa-solid fa-xmark"></i> Từ chối</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </main>
</div>

<!-- Form ngầm để gửi yêu cầu Reject. Sẽ được Javascript kích hoạt -->
<form id="rejectForm" action="${pageContext.request.contextPath}/admin/shops/reject" method="POST" style="display: none;">
    <input type="hidden" name="profileId" id="rejectProfileId" value="" />
    <input type="hidden" name="reason" id="rejectReason" value="" />
</form>

<script>
    /**
     * Hàm xử lý từ chối shop: 
     * Mở popup nhập lý do, nếu Admin nhập hợp lệ thì chèn vào form ngầm và submit gửi đi.
     */
    function rejectShop(profileId) {
        let reason = prompt("Vui lòng nhập lý do từ chối gian hàng này (Ví dụ: Thông tin không đầy đủ, Tên sai quy định...):");
        if (reason != null && reason.trim() !== "") {
            document.getElementById('rejectProfileId').value = profileId;
            document.getElementById('rejectReason').value = reason;
            document.getElementById('rejectForm').submit();
        } else if (reason != null) {
            alert("Bạn bắt buộc phải nhập lý do từ chối để thông báo cho người đăng ký!");
        }
    }
</script>
</body>
</html>
