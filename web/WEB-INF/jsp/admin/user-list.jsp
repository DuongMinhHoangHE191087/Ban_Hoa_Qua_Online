<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý người dùng – Admin MetaFruit</title>
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
    <main class="admin-main p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Quản Lý Người Dùng</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Tìm kiếm thành viên, phân cấp vai trò và kiểm soát trạng thái hoạt động tài khoản.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-surface/80 border border-primary-fixed/80 px-4 py-2 rounded-xl text-primary-dark shadow-sm">
                <i class="fa-solid fa-users text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Thành Viên</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
        <%-- Filter and Lists Card --%>
        <div class="glass-card">
            <%-- Filter bar --%>
            <form action="" method="GET" class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-wrap gap-4 items-center justify-between">
                <div class="flex items-center gap-2">
                    <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-user-gear text-primary mr-1"></i> Danh Sách Tài Khoản</h3>
                </div>
                
                <div class="flex flex-wrap items-center gap-3">
                    <select name="role" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white transition-all cursor-pointer">
                        <option value="">Tất cả vai trò</option>
                        <option value="CUSTOMER" ${paramRole == 'CUSTOMER' ? 'selected' : ''}>Khách hàng</option>
                        <option value="SHOP_OWNER" ${paramRole == 'SHOP_OWNER' ? 'selected' : ''}>Cửa hàng</option>
                        <option value="DELIVERY" ${paramRole == 'DELIVERY' ? 'selected' : ''}>Tài xế</option>
                        <option value="ADMIN" ${paramRole == 'ADMIN' ? 'selected' : ''}>Admin</option>
                    </select>
                    
                    <div class="relative">
                        <input type="text" name="search" value="<c:out value="${paramSearch}"/>" placeholder="Tên, Email, SĐT..." 
                               class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none pl-8 pr-4 py-2 text-xs bg-white transition-all">
                        <i class="fa-solid fa-search text-txt-3 absolute left-3 top-3 text-[10px]"></i>
                    </div>
                    
                    <button type="submit" class="bg-primary hover:bg-primary-dk text-white font-bold px-4 py-2 rounded-xl text-xs shadow active:scale-95 cursor-pointer">
                        <i class="fa-solid fa-filter mr-0.5"></i> Lọc
                    </button>
                    <a href="?" class="bg-white border border-slate-300 hover:bg-slate-50 text-txt-2 font-bold px-3 py-2 rounded-xl text-xs transition-all cursor-pointer">
                        <i class="fa-solid fa-rotate-right"></i>
                    </a>
                </div>
            </form>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">ID</th>
                            <th class="px-6 py-3.5 font-bold">Họ và Tên</th>
                            <th class="px-6 py-3.5 font-bold">Email (Tài khoản)</th>
                            <th class="px-6 py-3.5 font-bold">Số điện thoại</th>
                            <th class="px-6 py-3.5 font-bold text-center">Vai trò</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty userList}">
                                <tr>
                                    <td colspan="7" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Không tìm thấy tài khoản nào phù hợp.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="u" items="${userList}">
                                    <tr>
                                        <td class="px-6 py-4 font-mono font-bold text-primary">#${u.userId}</td>
                                        <td class="px-6 py-4 font-bold text-txt"><c:out value="${u.fullName}"/></td>
                                        <td class="px-6 py-4 text-txt-2 text-xs"><c:out value="${u.email}"/></td>
                                        <td class="px-6 py-4 text-xs font-mono text-txt-2">${empty u.phone ? '—' : fn:escapeXml(u.phone)}</td>
                                        <td class="px-6 py-4 text-center">
                                            <span class="inline-block px-2.5 py-1 rounded-lg bg-indigo-50 border border-indigo-100 text-indigo-700 text-xs font-semibold uppercase tracking-wider">
                                                ${u.role}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold" id="status-badge-${u.userId}">
                                                <c:choose>
                                                    <c:when test="${u.status == 'ACTIVE'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                            <i class="fa-solid fa-check text-[10px]"></i> ACTIVE
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${u.status == 'SUSPENDED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold">
                                                            <i class="fa-solid fa-ban text-[10px]"></i> SUSPENDED
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${u.status == 'LOCKED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-xs font-bold">
                                                            <i class="fa-solid fa-lock text-[10px]"></i> LOCKED
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${u.status == 'INACTIVE'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold">
                                                            <i class="fa-regular fa-hourglass-half text-[10px]"></i> INACTIVE
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold">
                                                            <c:out value="${u.status}"/>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <div class="flex items-center justify-center gap-2">

                                                <a href="${pageContext.request.contextPath}/admin/users/view?id=${u.userId}" 
                                                   class="bg-white hover:bg-slate-50 border border-slate-200 text-txt-2 hover:text-blue-600 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
                                                    <i class="fa-solid fa-eye mr-0.5"></i> Xem
                                                </a>
                                                <c:choose>
                                                    <c:when test="${u.role ne 'ADMIN'}">
                                                        <button class="toggle-status-btn bg-white border border-slate-200 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer ${u.status == 'ACTIVE' ? 'text-red-600 hover:bg-red-50' : u.status == 'SUSPENDED' ? 'text-emerald-600 hover:bg-emerald-50' : u.status == 'LOCKED' ? 'text-amber-600 hover:bg-amber-50' : 'text-sky-600 hover:bg-sky-50'}"
                                                                data-id="${u.userId}"
                                                                data-current="${u.status}"
                                                                id="btn-toggle-${u.userId}">
                                                            <c:choose>
                                                                <c:when test="${u.status == 'ACTIVE'}">
                                                                    <i class="fa-solid fa-ban mr-0.5"></i> Đình chỉ
                                                                </c:when>
                                                                <c:when test="${u.status == 'SUSPENDED'}">
                                                                    <i class="fa-solid fa-rotate-left mr-0.5"></i> Khôi phục
                                                                </c:when>
                                                                <c:when test="${u.status == 'LOCKED'}">
                                                                    <i class="fa-solid fa-unlock mr-0.5"></i> Mở khóa
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <i class="fa-solid fa-user-check mr-0.5"></i> Kích hoạt
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </button>
                                                        
                                                        <button class="revoke-sessions-btn bg-white border border-slate-200 text-amber-600 hover:bg-amber-50 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer"
                                                                data-id="${u.userId}"
                                                                data-name="<c:out value="${u.fullName}"/>"
                                                                id="btn-revoke-${u.userId}">
                                                            <i class="fa-solid fa-user-slash mr-0.5"></i> Thu hồi phiên
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-txt-3 text-xs italic block select-none">—</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <%-- Pagination --%>
            <c:if test="${totalPages > 1}">
                <div class="flex justify-between items-center px-6 py-4 border-t border-border gap-4">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="?role=${fn:escapeXml(paramRole)}&search=${fn:escapeXml(paramSearch)}" />
                </div>
            </c:if>
        </div>

    </main>
</div>

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
                    className: 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold',
                    html: '<i class="fa-solid fa-check text-[10px]"></i> ACTIVE'
                };
            case 'SUSPENDED':
                return {
                    className: 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold',
                    html: '<i class="fa-solid fa-ban text-[10px]"></i> SUSPENDED'
                };
            case 'LOCKED':
                return {
                    className: 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-xs font-bold',
                    html: '<i class="fa-solid fa-lock text-[10px]"></i> LOCKED'
                };
            case 'INACTIVE':
                return {
                    className: 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold',
                    html: '<i class="fa-regular fa-hourglass-half text-[10px]"></i> INACTIVE'
                };
            default:
                return {
                    className: 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold',
                    html: status
                };
        }
    }

    function getStatusActionMeta(status) {
        const baseClass = 'toggle-status-btn bg-white border border-slate-200 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer';
        switch (status) {
            case 'ACTIVE':
                return {
                    nextStatus: 'SUSPENDED',
                    className: baseClass + ' text-red-600 hover:bg-red-50',
                    html: '<i class="fa-solid fa-ban mr-0.5"></i> Đình chỉ'
                };
            case 'SUSPENDED':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' text-emerald-600 hover:bg-emerald-50',
                    html: '<i class="fa-solid fa-rotate-left mr-0.5"></i> Khôi phục'
                };
            case 'LOCKED':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' text-amber-600 hover:bg-amber-50',
                    html: '<i class="fa-solid fa-unlock mr-0.5"></i> Mở khóa'
                };
            case 'INACTIVE':
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' text-sky-600 hover:bg-sky-50',
                    html: '<i class="fa-solid fa-user-check mr-0.5"></i> Kích hoạt'
                };
            default:
                return {
                    nextStatus: 'ACTIVE',
                    className: baseClass + ' text-sky-600 hover:bg-sky-50',
                    html: '<i class="fa-solid fa-user-check mr-0.5"></i> Kích hoạt'
                };
        }
    }

    document.querySelectorAll('.toggle-status-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const userId = this.getAttribute('data-id');
            const currentStatus = this.getAttribute('data-current');
            const actionMeta = getStatusActionMeta(currentStatus);
            const newStatus = actionMeta.nextStatus;
            
            this.disabled = true;
            const originalHtml = this.innerHTML;
            this.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-0.5"></i>';

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
                    const badge = document.getElementById('status-badge-' + userId);
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
    });

    document.querySelectorAll('.revoke-sessions-btn').forEach(btn => {
        btn.addEventListener('click', function() {
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
                    this.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-0.5"></i>';

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
    });
</script>
</body>
</html>
