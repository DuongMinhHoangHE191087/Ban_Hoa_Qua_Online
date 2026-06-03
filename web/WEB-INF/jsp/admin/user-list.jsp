<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý người dùng - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    
    <style>
        .user-status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
        }
        .status-ACTIVE { background: #dcfce7; color: #166534; }
        .status-INACTIVE { background: #f3f4f6; color: #4b5563; }
        .status-LOCKED { background: #fee2e2; color: #991b1b; }
        .status-SUSPENDED { background: #fef3c7; color: #92400e; }
        
        .role-badge {
            background: #e0e7ff; color: #3730a3;
            padding: 4px 8px; border-radius: var(--radius-md);
            font-size: 0.75rem; font-weight: 600;
        }
        
        .table-actions {
            display: flex; gap: var(--space-2);
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="users"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <h1>Quản lý người dùng</h1>
            </header>
            
            <div class="admin-content">
                <div class="admin-panel">
                    
                    <form action="${pageContext.request.contextPath}/admin/users" method="get" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;">Danh sách tài khoản</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <select name="role" style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                                <option value="">Tất cả vai trò</option>
                                <option value="CUSTOMER" ${paramRole == 'CUSTOMER' ? 'selected' : ''}>Khách hàng</option>
                                <option value="SHOP_OWNER" ${paramRole == 'SHOP_OWNER' ? 'selected' : ''}>Cửa hàng</option>
                                <option value="DELIVERY" ${paramRole == 'DELIVERY' ? 'selected' : ''}>Tài xế</option>
                                <option value="ADMIN" ${paramRole == 'ADMIN' ? 'selected' : ''}>Admin</option>
                            </select>
                            <input type="text" name="search" value="<c:out value="${paramSearch}"/>" placeholder="Tên, Email, SĐT..." style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                            <button type="submit" class="btn btn-secondary btn-sm"><i class="fa-solid fa-search"></i> Lọc</button>
                        </div>
                    </form>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Họ Tên</th>
                                    <th>Email</th>
                                    <th>SĐT</th>
                                    <th>Vai trò</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty userList}">
                                        <tr><td colspan="7" class="text-center text-muted">Không có người dùng nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="u" items="${userList}">
                                            <tr>
                                                <td>#${u.userId}</td>
                                                <td style="font-weight: 500;">${u.fullName}</td>
                                                <td>${u.email}</td>
                                                <td>${empty u.phone ? '-' : u.phone}</td>
                                                <td><span class="role-badge">${u.role}</span></td>
                                                <td>
                                                    <span class="user-status-badge status-${u.status}" id="status-badge-${u.userId}">
                                                        ${u.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="table-actions">
                                                        <a href="${pageContext.request.contextPath}/admin/users/edit?id=${u.userId}" class="btn btn-sm btn-primary">
                                                            <i class="fa-solid fa-pen"></i> Sửa
                                                        </a>
                                                        <c:if test="${u.role ne 'ADMIN'}">
                                                            <button class="btn btn-sm ${u.status == 'ACTIVE' ? 'btn-danger' : 'btn-success'} toggle-status-btn" 
                                                                    data-id="${u.userId}" 
                                                                    data-current="${u.status}"
                                                                    id="btn-toggle-${u.userId}">
                                                                <c:choose>
                                                                    <c:when test="${u.status == 'ACTIVE'}"><i class="fa-solid fa-lock"></i> Khóa</c:when>
                                                                    <c:otherwise><i class="fa-solid fa-unlock"></i> Mở Khóa</c:otherwise>
                                                                </c:choose>
                                                            </button>
                                                        </c:if>
                                                        <c:if test="${u.role eq 'ADMIN'}">
                                                            <span class="text-muted" style="font-size: 0.8rem; font-style: italic;">(Khoá 🔒)</span>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div style="display: flex; justify-content: center; margin-top: var(--space-4); gap: 5px;">
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <a href="?page=${i}&role=${paramRole}&search=${paramSearch}" 
                                   class="btn btn-sm ${i == currentPage ? 'btn-primary' : 'btn-secondary'}">${i}</a>
                            </c:forEach>
                        </div>
                    </c:if>
                    
                </div>
            </div>
        </main>
    </div>

    <!-- Toast Notification (Simple Implementation) -->
    <div id="toast" style="visibility: hidden; min-width: 250px; background-color: #333; color: #fff; text-align: center; border-radius: 4px; padding: 12px; position: fixed; z-index: 1000; left: 50%; bottom: 30px; transform: translateX(-50%); transition: visibility 0.5s, bottom 0.5s;">
        Messege here
    </div>

    <script>
        function handleJSONResponse(response) {
            const contentType = response.headers.get("content-type");
            if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
                if (contentType && contentType.indexOf("application/json") !== -1) {
                    return response.json().then(errData => {
                        throw new Error(errData.message || errData.error || 'Lỗi hệ thống (Mã: ' + response.status + ')');
                    });
                }
                throw new Error('Lỗi hệ thống (Mã: ' + response.status + ')');
            }
            return response.json();
        }

        function showToast(msg, isSuccess) {
            var toast = document.getElementById("toast");
            toast.innerText = msg;
            toast.style.backgroundColor = isSuccess ? "var(--color-success)" : "var(--color-danger)";
            toast.style.visibility = "visible";
            toast.style.bottom = "50px";
            setTimeout(function(){ 
                toast.style.visibility = "hidden"; 
                toast.style.bottom = "30px";
            }, 3000);
        }

        document.querySelectorAll('.toggle-status-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = this.getAttribute('data-id');
                const currentStatus = this.getAttribute('data-current');
                // Nếu đang ACTIVE thì chuyển thành LOCKED, ngược lại chuyển thành ACTIVE
                const newStatus = (currentStatus === 'ACTIVE') ? 'LOCKED' : 'ACTIVE';
                
                // Vô hiệu hóa nút trong khi chờ
                this.disabled = true;
                const originalHtml = this.innerHTML;
                this.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';

                // Call AJAX API
                fetch('${pageContext.request.contextPath}/admin/users/status', {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: 'userId=' + userId + '&status=' + newStatus + '&_csrf=${sessionScope._csrfToken}'
                })
                .then(handleJSONResponse)
                .then(data => {
                    this.disabled = false;
                    if (data.success) {
                        showToast(data.message, true);
                        // Cập nhật giao diện
                        this.setAttribute('data-current', newStatus);
                        const badge = document.getElementById('status-badge-' + userId);
                        badge.className = 'user-status-badge status-' + newStatus;
                        badge.innerText = newStatus;
                        
                        if (newStatus === 'ACTIVE') {
                            this.className = 'btn btn-sm btn-danger toggle-status-btn';
                            this.innerHTML = '<i class="fa-solid fa-lock"></i> Khóa';
                        } else {
                            this.className = 'btn btn-sm btn-success toggle-status-btn';
                            this.innerHTML = '<i class="fa-solid fa-unlock"></i> Mở Khóa';
                        }
                    } else {
                        showToast(data.message || data.error || 'Có lỗi xảy ra', false);
                        this.innerHTML = originalHtml;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast(error.message || 'Lỗi kết nối mạng', false);
                    this.disabled = false;
                    this.innerHTML = originalHtml;
                });
            });
        });
    </script>
</body>
</html>
