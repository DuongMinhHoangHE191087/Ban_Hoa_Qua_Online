<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý toàn bộ cửa hàng – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="manage-shops"/>
    </jsp:include>

    <%-- Main --%>
    <main class="flex-1 overflow-y-auto p-6 md:p-8 animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-surface border border-border p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Tổng Quản Lý Cửa Hàng</h1>
                <p class="text-txt-2 text-sm mt-1">Giám sát và kiểm soát hoạt động của tất cả các cửa hàng trên hệ thống.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-primary-lt text-primary px-4 py-2 rounded-xl border border-primary-fixed font-bold">
                <i class="fa-solid fa-store"></i> ${totalItems} Gian Hàng
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <c:url var="manageAllShopsUrl" value="/admin/shops/manage" />
        <c:url var="managePendingShopsUrl" value="/admin/shops/manage">
            <c:param name="filter" value="PENDING" />
        </c:url>
        <c:url var="manageApprovedShopsUrl" value="/admin/shops/manage">
            <c:param name="filter" value="APPROVED" />
        </c:url>
        <c:url var="manageRejectedShopsUrl" value="/admin/shops/manage">
            <c:param name="filter" value="REJECTED" />
        </c:url>
        <c:url var="manageSuspendedShopsUrl" value="/admin/shops/manage">
            <c:param name="filter" value="SUSPENDED" />
        </c:url>

        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3 mb-6">
            <div class="flex flex-wrap items-center gap-2">
                <span class="text-xs font-bold text-txt-2 uppercase tracking-wider">Lọc nhanh</span>
                <a href="${manageAllShopsUrl}"
                   class="px-3.5 py-2 rounded-xl text-xs font-bold transition-all border ${currentFilter == 'ALL' ? 'bg-primary text-white shadow-sm border-primary' : 'bg-white text-txt-2 border-slate-200 hover:bg-slate-50'}">
                    Tất cả
                </a>
                <a href="${managePendingShopsUrl}"
                   class="px-3.5 py-2 rounded-xl text-xs font-bold transition-all border ${currentFilter == 'PENDING' ? 'bg-[#f59e0b] text-white shadow-sm border-[#f59e0b]' : 'bg-white text-txt-2 border-slate-200 hover:bg-slate-50'}">
                    Chờ duyệt
                </a>
                <a href="${manageApprovedShopsUrl}"
                   class="px-3.5 py-2 rounded-xl text-xs font-bold transition-all border ${currentFilter == 'APPROVED' ? 'bg-[#22c55e] text-white shadow-sm border-[#22c55e]' : 'bg-white text-txt-2 border-slate-200 hover:bg-slate-50'}">
                    Đã duyệt
                </a>
                <a href="${manageRejectedShopsUrl}"
                   class="px-3.5 py-2 rounded-xl text-xs font-bold transition-all border ${currentFilter == 'REJECTED' ? 'bg-[#ef4444] text-white shadow-sm border-[#ef4444]' : 'bg-white text-txt-2 border-slate-200 hover:bg-slate-50'}">
                    Từ chối
                </a>
                <a href="${manageSuspendedShopsUrl}"
                   class="px-3.5 py-2 rounded-xl text-xs font-bold transition-all border ${currentFilter == 'SUSPENDED' ? 'bg-slate-700 text-white shadow-sm border-slate-700' : 'bg-white text-txt-2 border-slate-200 hover:bg-slate-50'}">
                    Đình chỉ
                </a>
            </div>
            <div class="text-xs text-txt-2 bg-white border border-slate-200 rounded-xl px-4 py-2 shadow-sm">
                Đang xem: <span class="font-bold text-txt">${currentFilter}</span>
            </div>
        </div>

        <%-- Shops list panel --%>
        <div class="glass-card overflow-hidden">
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-list-check text-primary mr-1"></i> Danh Sách Cửa Hàng</h3>
                <div class="relative w-full sm:w-64">
                    <input type="text" id="shopSearch" placeholder="Tìm tên cửa hàng..."
                           class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none pl-9 pr-4 py-2 text-xs bg-white transition-all">
                    <i class="fa-solid fa-search text-txt-3 absolute left-3 top-2.5 text-xs"></i>
                </div>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">Gian Hàng</th>
                            <th class="px-6 py-3.5 font-bold">Chủ Shop</th>
                            <th class="px-6 py-3.5 font-bold">Địa Chỉ</th>
                            <th class="px-6 py-3.5 font-bold text-center">Đánh Giá</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng Thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody id="shopTableBody" class="divide-y divide-slate-100">
                        <c:choose>
                            <c:when test="${empty shopList}">
                                <tr><td colspan="6" class="px-6 py-12 text-center text-slate-400">Không có cửa hàng nào trên hệ thống.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="shop" items="${shopList}">
                                    <tr class="hover:bg-slate-50 transition-colors">
                                        <td class="px-6 py-4">
                                            <strong class="shop-name-col block text-slate-800">
                                                <a href="${pageContext.request.contextPath}/shop-view?id=${shop.profileId}&idType=profile"
                                                   class="hover:text-primary transition-colors">
                                                    <c:out value="${shop.shopName}"/>
                                                </a>
                                            </strong>
                                            <span class="text-xs text-slate-500 line-clamp-1 max-w-[200px]" title="${fn:escapeXml(shop.shopDescription)}">${shop.shopDescription}</span>
                                        </td>
                                        <td class="px-6 py-4">
                                            <div class="font-semibold text-txt">
                                                <a href="${pageContext.request.contextPath}/shop-view?id=${shop.profileId}&idType=profile"
                                                   class="hover:text-primary transition-colors">
                                                    <c:out value="${shop.ownerName}"/>
                                                </a>
                                            </div>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600 line-clamp-2 max-w-[250px]">${shop.deliveryAddress}</td>
                                        <td class="px-6 py-4 text-center">
                                            <span class="inline-flex items-center gap-1 bg-amber-50 text-amber-600 px-2 py-0.5 rounded font-bold text-xs border border-amber-200">
                                                ${shop.rating} <i class="fa-solid fa-star text-[10px]"></i>
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${shop.approvalStatus == 'APPROVED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-bold">
                                                        <i class="fa-solid fa-check-circle"></i> Đang Hoạt Động
                                                    </span>
                                                </c:when>
                                                <c:when test="${shop.approvalStatus == 'SUSPENDED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-200 text-red-700 text-xs font-bold">
                                                        <i class="fa-solid fa-ban"></i> Đã Đình Chỉ
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-600 text-xs font-bold">
                                                        ${shop.approvalStatus}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <button type="button"
                                                    class="bg-blue-600 hover:bg-blue-700 text-white font-bold p-2 rounded-lg text-xs transition-all active:scale-95 cursor-pointer mr-1"
                                                    onclick="showDetailModal('${shop.profileId}')" title="Xem chi tiết">
                                                <i class="fa-solid fa-eye"></i>
                                            </button>
                                            <c:if test="${shop.approvalStatus == 'APPROVED'}">
                                                <form action="${pageContext.request.contextPath}/admin/shops/manage" method="POST" class="inline" onsubmit="return confirmSuspend(event, '${fn:escapeXml(shop.shopName)}')">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="suspend">
                                                    <input type="hidden" name="profileId" value="${shop.profileId}">
                                                    <input type="hidden" name="filter" value="${currentFilter}">
                                                    <input type="hidden" name="page" value="${currentPage}">
                                                    <button type="submit" class="bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm">
                                                        Đình Chỉ
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${shop.approvalStatus == 'SUSPENDED'}">
                                                <form action="${pageContext.request.contextPath}/admin/shops/manage" method="POST" class="inline" onsubmit="return confirmActivate(event, '${fn:escapeXml(shop.shopName)}')">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="activate">
                                                    <input type="hidden" name="profileId" value="${shop.profileId}">
                                                    <input type="hidden" name="filter" value="${currentFilter}">
                                                    <input type="hidden" name="page" value="${currentPage}">
                                                    <button type="submit" class="bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm">
                                                        Mở Khóa
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <c:if test="${totalPages > 1}">
                <div class="px-6 py-4 border-t border-border flex items-center justify-between bg-slate-50/50">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="${pageContext.request.contextPath}/admin/shops?filter=${fn:escapeXml(currentFilter)}" />
                </div>
            </c:if>
        </div>

    </main>
</div>

<div id="detailModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-3xl shadow-2xl border border-border flex flex-col max-h-[90vh]">
        <div class="flex items-center justify-between gap-4 px-6 py-4 border-b border-border">
            <div>
                <h3 class="font-black text-txt text-base flex items-center gap-2"><i class="fa-solid fa-store text-primary"></i> Chi tiết cửa hàng</h3>
                <p class="text-xs text-txt-2 mt-1">Xem đầy đủ thông tin đăng ký kinh doanh, giấy tờ và trạng thái kiểm duyệt.</p>
            </div>
            <div class="flex items-center gap-2">
                <a id="detailPublicLink" href="#" target="_blank"
                   class="inline-flex items-center gap-1 bg-primary-lt text-primary hover:text-primary-dk border border-[#bbf7d0] px-3 py-2 rounded-xl text-xs font-bold transition-all">
                    <i class="fa-solid fa-arrow-up-right-from-square"></i> Mở shop công khai
                </a>
                <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeDetailModal()">&times;</button>
            </div>
        </div>
        <div class="p-6 overflow-y-auto flex-1 text-sm space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Thông tin cơ bản</h4>
                    <p><span class="font-semibold text-txt-2">Tên shop:</span> <span id="detailName" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Chủ shop:</span> <span id="detailOwnerName" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Email liên hệ:</span> <span id="detailEmail" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Email chủ shop:</span> <span id="detailOwnerEmail" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Địa chỉ:</span> <span id="detailAddress" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Trạng thái:</span> <span id="detailStatus" class="font-bold"></span></p>
                </div>
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Mô tả</h4>
                    <p id="detailDescription" class="text-txt-2 text-xs leading-relaxed whitespace-pre-line"></p>
                </div>
            </div>

            <div class="bg-slate-50 p-4 rounded-xl border border-border">
                <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Danh mục kinh doanh</h4>
                <div id="detailCategories" class="flex flex-wrap gap-2"></div>
            </div>

            <div class="bg-slate-50 p-4 rounded-xl border border-border">
                <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Tệp đính kèm (Tài liệu xác minh)</h4>
                <ul id="detailDocs" class="space-y-2"></ul>
            </div>
        </div>
        <div class="px-6 py-4 border-t border-border flex justify-end">
            <button class="bg-white border border-slate-200 text-txt-2 hover:bg-slate-50 font-bold px-4 py-2 rounded-xl text-xs transition-all cursor-pointer"
                    onclick="closeDetailModal()">
                Đóng
            </button>
        </div>
    </div>
</div>

<script id="categoriesData" type="application/json">
    {
        <c:forEach var="cat" items="${categories}" varStatus="status">
            "${cat.categoryId}": "${fn:escapeXml(cat.name)}"<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    }
</script>
<script>
    const categoryMap = JSON.parse(document.getElementById('categoriesData').textContent);
</script>

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

    function statusBadge(status) {
        const normalized = (status || '').toString().toUpperCase();
        if (normalized === 'PENDING') return '<span class="text-amber-600">⏳ Đang chờ duyệt</span>';
        if (normalized === 'APPROVED') return '<span class="text-emerald-600">✅ Đã duyệt</span>';
        if (normalized === 'REJECTED') return '<span class="text-red-600">❌ Từ chối</span>';
        if (normalized === 'SUSPENDED') return '<span class="text-slate-700">⛔ Đình chỉ</span>';
        return '<span class="text-slate-600">' + (status || 'Không rõ') + '</span>';
    }

    function closeDetailModal() {
        document.getElementById('detailModal').classList.add('hidden');
    }

    function showDetailModal(profileId) {
        Swal.fire({ title: 'Đang tải...', allowOutsideClick: false, didOpen: () => { Swal.showLoading() } });
        fetch('${pageContext.request.contextPath}/api/admin/shops/detail?id=' + profileId)
            .then(handleJSONResponse)
            .then(data => {
                Swal.close();
                if (data.success && data.data) {
                    const shop = data.data;
                    document.getElementById('detailPublicLink').href = '${pageContext.request.contextPath}/shop-view?id=' + shop.shopId + '&idType=profile';
                    document.getElementById('detailName').textContent = shop.shopName || '';
                    document.getElementById('detailOwnerName').textContent = shop.ownerName || '';
                    document.getElementById('detailEmail').textContent = shop.businessEmail || '';
                    document.getElementById('detailOwnerEmail').textContent = shop.ownerEmail || '';
                    document.getElementById('detailAddress').textContent = shop.deliveryAddress || '';
                    document.getElementById('detailDescription').textContent = shop.shopDescription || 'Không có mô tả.';
                    document.getElementById('detailStatus').innerHTML = statusBadge(shop.approvalStatus);

                    const catContainer = document.getElementById('detailCategories');
                    catContainer.innerHTML = '';
                    if (shop.preferredCategories) {
                        try {
                            const catIds = JSON.parse(shop.preferredCategories);
                            if (catIds && catIds.length > 0) {
                                catIds.forEach(id => {
                                    const catName = categoryMap[id] || ('Danh mục ' + id);
                                    catContainer.innerHTML += '<span class="bg-primary/10 text-primary px-3 py-1 rounded-full text-xs font-semibold">' + escapeHtml(catName) + '</span>';
                                });
                            } else {
                                catContainer.innerHTML = '<span class="text-xs text-txt-3">Không có</span>';
                            }
                        } catch (e) {
                            catContainer.innerHTML = '<span class="text-xs text-txt-3">Lỗi dữ liệu</span>';
                        }
                    } else {
                        catContainer.innerHTML = '<span class="text-xs text-txt-3">Không có</span>';
                    }

                    const docContainer = document.getElementById('detailDocs');
                    docContainer.innerHTML = '';
                    if (shop.docPaths) {
                        try {
                            const paths = JSON.parse(shop.docPaths);
                            if (paths && paths.length > 0) {
                                paths.forEach(path => {
                                    const fileName = path.split('/').pop().split('\\').pop();
                                    const fileUrl = '${pageContext.request.contextPath}/shop/docs?path=' + encodeURIComponent(path);
                                    docContainer.innerHTML += '<li class="flex items-center justify-between p-3 border border-border rounded-lg bg-white">' +
                                        '<div class="flex items-center gap-2 overflow-hidden">' +
                                        '<i class="fa-solid fa-file-alt text-primary text-lg"></i>' +
                                        '<span class="text-xs text-txt truncate">' + escapeHtml(fileName) + '</span>' +
                                        '</div>' +
                                        '<div class="flex gap-2">' +
                                        '<a href="' + fileUrl + '" class="text-emerald-600 hover:text-emerald-800" title="Tải xuống"><i class="fa-solid fa-download"></i></a>' +
                                        '</div>' +
                                        '</li>';
                                });
                            } else {
                                docContainer.innerHTML = '<span class="text-xs text-txt-3">Không có tài liệu</span>';
                            }
                        } catch (e) {
                            docContainer.innerHTML = '<span class="text-xs text-txt-3">Lỗi dữ liệu tài liệu</span>';
                        }
                    } else {
                        docContainer.innerHTML = '<span class="text-xs text-txt-3">Không có tài liệu</span>';
                    }

                    document.getElementById('detailModal').classList.remove('hidden');
                } else {
                    Swal.fire('Lỗi', data.message || 'Không thể lấy thông tin.', 'error');
                }
            })
            .catch(error => {
                Swal.close();
                Swal.fire('Lỗi', error.message || 'Lỗi kết nối mạng.', 'error');
            });
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    document.getElementById('shopSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        document.querySelectorAll('#shopTableBody tr').forEach(row => {
            const nameEl = row.querySelector('.shop-name-col');
            if(!nameEl) return;
            const name = nameEl.textContent.toLowerCase();
            row.style.display = name.includes(term) ? '' : 'none';
        });
    });

    window.onclick = e => {
        if (e.target === document.getElementById('detailModal')) closeDetailModal();
    };

    function confirmSuspend(event, shopName) {
        event.preventDefault();
        Swal.fire({
            title: 'Đình chỉ hoạt động cửa hàng?',
            html: 'Bạn có chắc chắn muốn đình chỉ cửa hàng <b>' + shopName + '</b>? Shop này sẽ không thể tiếp tục kinh doanh bán hàng.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đình chỉ',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }

    function confirmActivate(event, shopName) {
        event.preventDefault();
        Swal.fire({
            title: 'Mở khóa cửa hàng?',
            html: 'Mở lại hoạt động kinh doanh cho cửa hàng <b>' + shopName + '</b>?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Mở khóa',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }
</script>
</body>
</html>
