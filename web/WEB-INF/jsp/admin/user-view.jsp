<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết người dùng – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<input type="hidden" id="js-csrf" value="${sessionScope._csrfToken}">
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="users"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 animate-fade-in-up opacity-0">
        
        <div class="mb-6 flex items-center justify-between">
            <a href="${pageContext.request.contextPath}/admin/users" class="text-primary hover:text-primary-dk font-bold flex items-center gap-2 transition-colors text-decoration-none">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
            </a>
            <h1 class="text-2xl font-extrabold text-txt">Chi Tiết Người Dùng</h1>
        </div>

        <div class="glass-card max-w-3xl mx-auto overflow-hidden">
            <div class="bg-gradient-to-r from-primary to-primary-dk p-6 text-white flex items-center gap-4">
                <div class="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center text-2xl font-bold border-2 border-white/30 shadow-inner">
                    <c:choose>
                        <c:when test="${not empty user.avatarUrl}">
                            <img src="${user.avatarUrl}" alt="Avatar" class="w-full h-full rounded-full object-cover">
                        </c:when>
                        <c:otherwise>
                            <i class="fa-solid fa-user"></i>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <h2 class="text-xl font-bold">${user.fullName}</h2>
                    <p class="text-white/80 text-sm mt-1">ID: #${user.userId} • Đăng ký: <fmt:parseDate value="${user.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDate}"/></p>
                </div>
                <div class="ml-auto">
                    <c:choose>
                        <c:when test="${user.status == 'ACTIVE'}">
                            <span id="user-status-badge" class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-emerald-400/20 text-emerald-100 border border-emerald-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-solid fa-check"></i> ACTIVE
                            </span>
                        </c:when>
                        <c:when test="${user.status == 'SUSPENDED'}">
                            <span id="user-status-badge" class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-amber-400/20 text-amber-100 border border-amber-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-solid fa-ban"></i> SUSPENDED
                            </span>
                        </c:when>
                        <c:when test="${user.status == 'LOCKED'}">
                            <span id="user-status-badge" class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-red-400/20 text-red-100 border border-red-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-solid fa-lock"></i> LOCKED
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span id="user-status-badge" class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-slate-400/20 text-slate-100 border border-slate-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-regular fa-hourglass-half"></i> ${user.status}
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="p-8 grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <div class="detail-label"><i class="fa-regular fa-id-card mr-1"></i> Họ và tên</div>
                    <div class="detail-value">${user.fullName}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-regular fa-envelope mr-1"></i> Địa chỉ Email</div>
                    <div class="detail-value">${user.email} <c:if test="${user.emailVerified}"><i class="fa-solid fa-circle-check text-emerald-500 ml-1 text-sm" title="Đã xác thực"></i></c:if></div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-phone mr-1"></i> Số điện thoại</div>
                    <div class="detail-value">${empty user.phone ? '<span class="text-txt-3 italic">Chưa cập nhật</span>' : user.phone}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-user-shield mr-1"></i> Vai trò (Role)</div>
                    <div class="detail-value">
                        <span class="inline-block px-2.5 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-bold uppercase border border-indigo-100">
                            ${user.role}
                        </span>
                    </div>
                </div>
                <div class="md:col-span-2">
                    <div class="detail-label"><i class="fa-solid fa-map-location-dot mr-1"></i> Địa chỉ mặc định</div>
                    <div class="detail-value">${empty user.userAddress ? '<span class="text-txt-3 italic">Chưa có địa chỉ</span>' : user.userAddress}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-clock-rotate-left mr-1"></i> Cập nhật lần cuối</div>
                    <div class="detail-value text-sm">
                        <c:choose>
                            <c:when test="${not empty user.updatedAt}">
                                <fmt:parseDate value="${user.updatedAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedUpdDate" type="both"/>
                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedUpdDate}"/>
                            </c:when>
                            <c:otherwise><span class="text-txt-3">Chưa có</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-triangle-exclamation mr-1"></i> Đăng nhập sai</div>
                    <div class="detail-value text-sm ${user.failedLoginCount > 0 ? 'text-amber-600 font-bold' : ''}">
                        ${user.failedLoginCount} lần
                    </div>
                </div>
            </div>
            
            <div class="px-8 py-4 bg-slate-50 border-t border-border flex justify-end gap-2 flex-wrap">
                <c:if test="${user.role ne 'ADMIN'}">
                    <button class="toggle-status-btn font-bold px-4 py-2.5 rounded-xl text-sm transition-all cursor-pointer ${user.status == 'ACTIVE' ? 'bg-red-50 border border-red-200 text-red-600 hover:bg-red-100' : user.status == 'SUSPENDED' ? 'bg-emerald-50 border border-emerald-200 text-emerald-600 hover:bg-emerald-100' : user.status == 'LOCKED' ? 'bg-amber-50 border border-amber-200 text-amber-600 hover:bg-amber-100' : 'bg-sky-50 border border-sky-200 text-sky-600 hover:bg-sky-100'}"
                            data-id="${user.userId}"
                            data-current="${user.status}"
                            id="btn-toggle-${user.userId}">
                        <c:choose>
                            <c:when test="${user.status == 'ACTIVE'}">
                                <i class="fa-solid fa-ban mr-1"></i> Đình chỉ
                            </c:when>
                            <c:when test="${user.status == 'SUSPENDED'}">
                                <i class="fa-solid fa-rotate-left mr-1"></i> Khôi phục
                            </c:when>
                            <c:when test="${user.status == 'LOCKED'}">
                                <i class="fa-solid fa-unlock mr-1"></i> Mở khóa
                            </c:when>
                            <c:otherwise>
                                <i class="fa-solid fa-user-check mr-1"></i> Kích hoạt
                            </c:otherwise>
                        </c:choose>
                    </button>

                    <button class="revoke-sessions-btn bg-amber-50 border border-amber-200 text-amber-600 hover:bg-amber-100 font-bold px-4 py-2.5 rounded-xl text-sm transition-all cursor-pointer"
                            data-id="${user.userId}"
                            data-name="<c:out value="${user.fullName}"/>"
                            id="btn-revoke-${user.userId}">
                        <i class="fa-solid fa-user-slash mr-1"></i> Thu hồi phiên
                    </button>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/users" class="px-6 py-2.5 bg-white border border-slate-300 hover:bg-slate-100 text-txt-2 font-bold rounded-xl transition-colors cursor-pointer text-sm flex items-center">
                    Đóng
                </a>
            </div>
        </div>
    </main>
</div>

<script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
<script>
    const Toast = Swal.mixin({
        toast: true,
        position: 'bottom-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true
    });

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

    function getStatusBadgeMeta(status) {
        switch (status) {
            case 'ACTIVE':
                return {
                    className: 'inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-emerald-400/20 text-emerald-100 border border-emerald-400/30 text-xs font-bold shadow-sm',
                    html: '<i class="fa-solid fa-check"></i> ACTIVE'
                };
            case 'SUSPENDED':
                return {
                    className: 'inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-amber-400/20 text-amber-100 border border-amber-400/30 text-xs font-bold shadow-sm',
                    html: '<i class="fa-solid fa-ban"></i> SUSPENDED'
                };
            case 'LOCKED':
                return {
                    className: 'inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-red-400/20 text-red-100 border border-red-400/30 text-xs font-bold shadow-sm',
                    html: '<i class="fa-solid fa-lock"></i> LOCKED'
                };
            case 'INACTIVE':
                return {
                    className: 'inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-slate-400/20 text-slate-100 border border-slate-400/30 text-xs font-bold shadow-sm',
                    html: '<i class="fa-regular fa-hourglass-half"></i> INACTIVE'
                };
            default:
                return {
                    className: 'inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-slate-400/20 text-slate-100 border border-slate-400/30 text-xs font-bold shadow-sm',
                    html: status
                };
        }
    }

    function getStatusActionMeta(status) {
        const baseClass = 'toggle-status-btn font-bold px-4 py-2.5 rounded-xl text-sm transition-all cursor-pointer';
        switch (status) {
            case 'ACTIVE':
                return {
                    nextStatus: 'SUSPENDED',
                    className: baseClass + ' bg-red-50 border border-red-200 text-red-600 hover:bg-red-100',
                    html: '<i class="fa-solid fa-ban mr-1"></i> Đình chỉ'
                };
            case 'SUSPENDED':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' bg-emerald-50 border border-emerald-200 text-emerald-600 hover:bg-emerald-100',
                    html: '<i class="fa-solid fa-rotate-left mr-1"></i> Khôi phục'
                };
            case 'LOCKED':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' bg-amber-50 border border-amber-200 text-amber-600 hover:bg-amber-100',
                    html: '<i class="fa-solid fa-unlock mr-1"></i> Mở khóa'
                };
            case 'INACTIVE':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' bg-sky-50 border border-sky-200 text-sky-600 hover:bg-sky-100',
                    html: '<i class="fa-solid fa-user-check mr-1"></i> Kích hoạt'
                };
            default:
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' bg-sky-50 border border-sky-200 text-sky-600 hover:bg-sky-100',
                    html: '<i class="fa-solid fa-user-check mr-1"></i> Kích hoạt'
                };
        }
    }

    const toggleBtn = document.querySelector('.toggle-status-btn');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', function() {
            const userId = this.getAttribute('data-id');
            const currentStatus = this.getAttribute('data-current');
            const actionMeta = getStatusActionMeta(currentStatus);
            const newStatus = actionMeta.nextStatus;

            this.disabled = true;
            const originalHtml = this.innerHTML;
            this.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Đang xử lý...';

            const CSRF = document.getElementById('js-csrf').value;
            fetch('${pageContext.request.contextPath}/admin/users/status', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': CSRF
                },
                body: 'userId=' + encodeURIComponent(userId) + '&status=' + encodeURIComponent(newStatus) + '&_csrf=' + encodeURIComponent(CSRF)
            })
            .then(handleJSONResponse)
            .then(data => {
                this.disabled = false;
                if (data.success) {
                    Toast.fire({ icon: 'success', title: data.data || data.message || 'Cập nhật thành công' });
                    this.setAttribute('data-current', newStatus);
                    const badge = document.getElementById('user-status-badge');
                    const badgeMeta = getStatusBadgeMeta(newStatus);
                    const nextActionMeta = getStatusActionMeta(newStatus);

                    if (badge) {
                        badge.className = badgeMeta.className;
                        badge.innerHTML = badgeMeta.html;
                    }
                    this.className = nextActionMeta.className;
                    this.innerHTML = nextActionMeta.html;
                } else {
                    Toast.fire({ icon: 'error', title: data.error || data.message || 'Có lỗi xảy ra' });
                    this.innerHTML = originalHtml;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Toast.fire({ icon: 'error', title: error.message || 'Lỗi kết nối mạng' });
                this.disabled = false;
                this.innerHTML = originalHtml;
            });
        });
    }

    const revokeBtn = document.querySelector('.revoke-sessions-btn');
    if (revokeBtn) {
        revokeBtn.addEventListener('click', function() {
            const userId = this.getAttribute('data-id');
            const userName = this.getAttribute('data-name');

            Swal.fire({
                title: 'Xác nhận thu hồi phiên đăng nhập?',
                text: 'Hành động này sẽ xóa toàn bộ refresh tokens của ' + userName + '. Người dùng này sẽ phải đăng nhập lại.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: 'Đồng ý thu hồi',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    this.disabled = true;
                    const originalHtml = this.innerHTML;
                    this.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Đang xử lý...';

                    const CSRF = document.getElementById('js-csrf').value;
                    fetch('${pageContext.request.contextPath}/admin/users/revoke-sessions', {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-Requested-With': 'XMLHttpRequest',
                            'X-CSRF-Token': CSRF
                        },
                        body: 'userId=' + encodeURIComponent(userId) + '&_csrf=' + encodeURIComponent(CSRF)
                    })
                    .then(handleJSONResponse)
                    .then(data => {
                        this.disabled = false;
                        this.innerHTML = originalHtml;
                        if (data.success) {
                            Swal.fire(
                                'Thành công!',
                                data.data || data.message || 'Thao tác thành công',
                                'success'
                            );
                        } else {
                            Toast.fire({ icon: 'error', title: data.error || data.message || 'Có lỗi xảy ra' });
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        Toast.fire({ icon: 'error', title: error.message || 'Lỗi kết nối mạng' });
                        this.disabled = false;
                        this.innerHTML = originalHtml;
                    });
                }
            });
        });
    }
</script>
</body>
</html>
