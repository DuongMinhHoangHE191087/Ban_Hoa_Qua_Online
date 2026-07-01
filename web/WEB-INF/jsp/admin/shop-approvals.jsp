<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phê duyệt cửa hàng – Admin MetaFruit</title>
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
        <jsp:param name="activeMenu" value="shops"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-surface border border-border p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Phê Duyệt Cửa Hàng</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Duyệt thông tin đăng ký gian hàng và cấp quyền kinh doanh cho người dùng.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-primary-lt text-primary px-4 py-2 rounded-xl border border-primary-fixed font-bold">
                <i class="fa-solid fa-store text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Gian Hàng</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Shops list panel --%>
        <div class="glass-card">
            <%-- Filter/Search bar --%>
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-store text-primary mr-1"></i> Yêu Cầu Mở Cửa Hàng</h3>
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
                            <th class="px-6 py-3.5 font-bold">Tên Cửa Hàng</th>
                            <th class="px-6 py-3.5 font-bold">Mô tả</th>
                            <th class="px-6 py-3.5 font-bold">Địa chỉ hoạt động</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody id="shopTableBody" class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty shopList}">
                                <tr>
                                    <td colspan="5" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Không có yêu cầu mở shop nào đang chờ xử lý.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="shop" items="${shopList}">
                                    <tr>
                                        <%-- Shop Name & UID --%>
                                        <td class="px-6 py-4">
                                            <strong class="shop-name-col text-sm text-txt font-bold">${shop.shopName}</strong>
                                            <span class="text-xs text-txt-2 block mt-1">Chủ sở hữu UID: <b class="font-mono text-primary">${shop.userId}</b></span>
                                        </td>
                                        <%-- Description --%>
                                        <td class="px-6 py-4 text-xs text-txt-2">
                                            <div class="max-w-[200px] truncate" title="${fn:escapeXml(shop.shopDescription)}">
                                                ${shop.shopDescription}
                                            </div>
                                        </td>
                                        <%-- Address --%>
                                        <td class="px-6 py-4 text-xs text-txt-2">
                                            <div class="max-w-[200px] truncate" title="${fn:escapeXml(shop.deliveryAddress)}">
                                                ${shop.deliveryAddress}
                                            </div>
                                        </td>
                                        <%-- Status Badge --%>
                                        <td class="px-6 py-4 text-center">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold" id="status-badge-${shop.profileId}">
                                                <c:choose>
                                                    <c:when test="${shop.approvalStatus == 'PENDING'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold">
                                                            <i class="fa-solid fa-clock text-[10px]"></i> Chờ Duyệt
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${shop.approvalStatus == 'APPROVED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                            <i class="fa-solid fa-check-circle text-[10px]"></i> Đã Duyệt
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${shop.approvalStatus == 'REJECTED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-xs font-bold">
                                                            <i class="fa-solid fa-times-circle text-[10px]"></i> Từ Chối
                                                        </span>
                                                    </c:when>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <%-- Action Buttons --%>
                                        <td class="px-6 py-4 text-center">
                                            <div class="flex items-center justify-center gap-2" id="action-btns-${shop.profileId}">
                                                <button class="bg-blue-600 hover:bg-blue-700 text-white font-bold p-2 rounded-lg text-xs transition-all active:scale-95 cursor-pointer mr-1" 
                                                        onclick="showDetailModal('${shop.profileId}')" title="Xem chi tiết">
                                                    <i class="fa-solid fa-eye"></i>
                                                </button>
                                                <c:choose>
                                                    <c:when test="${shop.approvalStatus == 'PENDING'}">
                                                        <button class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold p-2 rounded-lg text-xs transition-all active:scale-95 cursor-pointer" 
                                                                onclick="approveShop('${shop.profileId}')" title="Duyệt cửa hàng">
                                                            <i class="fa-solid fa-check"></i>
                                                        </button>
                                                        <button class="bg-red-600 hover:bg-red-700 text-white font-bold p-2 rounded-lg text-xs transition-all active:scale-95 cursor-pointer" 
                                                                onclick="showRejectModal('${shop.profileId}')" title="Từ chối yêu cầu">
                                                            <i class="fa-solid fa-xmark"></i>
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-txt-3 text-xs italic block">Đã xử lý</span>
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
        </div>

    </main>
</div>

<%-- Detail Modal --%>
<div id="detailModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-3xl shadow-2xl border border-border flex flex-col max-h-[90vh]">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base flex items-center gap-2"><i class="fa-solid fa-store text-primary"></i> Chi tiết cửa hàng</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeDetailModal()">&times;</button>
        </div>
        <div class="p-6 overflow-y-auto flex-1 text-sm space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Thông tin cơ bản</h4>
                    <p><span class="font-semibold text-txt-2">Tên:</span> <span id="detailName" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Email liên hệ:</span> <span id="detailEmail" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Địa chỉ:</span> <span id="detailAddress" class="text-txt font-medium"></span></p>
                    <p class="mt-1"><span class="font-semibold text-txt-2">Trạng thái:</span> <span id="detailStatus" class="font-bold"></span></p>
                </div>
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Mô tả</h4>
                    <p id="detailDescription" class="text-txt-2 text-xs leading-relaxed italic"></p>
                </div>
            </div>
            
            <div class="bg-slate-50 p-4 rounded-xl border border-border">
                <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Danh mục kinh doanh</h4>
                <div id="detailCategories" class="flex flex-wrap gap-2">
                    <!-- Categories go here -->
                </div>
            </div>

            <div class="bg-slate-50 p-4 rounded-xl border border-border">
                <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Tệp đính kèm (Tài liệu xác minh)</h4>
                <ul id="detailDocs" class="space-y-2">
                    <!-- Docs go here -->
                </ul>
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

<%-- Reject Modal --%>
<div id="rejectModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Từ chối mở cửa hàng</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeRejectModal()">&times;</button>
        </div>
        <div class="p-6">
            <p class="text-xs text-txt-2 mb-3 leading-relaxed">Vui lòng nhập lý do từ chối cụ thể để gửi thông báo hướng dẫn lại cho người dùng.</p>
            <input type="hidden" id="rejectProfileId">
            <textarea id="rejectionReason" rows="4" 
                      class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm resize-none mb-4 transition-all" 
                      placeholder="Lý do (ví dụ: Giấy phép kinh doanh không hợp lệ hoặc địa chỉ không rõ ràng)..."></textarea>
            <div class="flex justify-end gap-2">
                <button class="bg-white border border-slate-200 text-txt-2 hover:bg-slate-50 font-bold px-4 py-2 rounded-xl text-xs transition-all cursor-pointer" 
                        onclick="closeRejectModal()">
                    Hủy bỏ
                </button>
                <button class="bg-red-600 hover:bg-red-700 text-white font-bold px-4 py-2 rounded-xl text-xs transition-all shadow-md active:scale-95 cursor-pointer" 
                        onclick="submitReject()">
                    Xác nhận Từ chối
                </button>
            </div>
        </div>
    </div>
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

    document.getElementById('shopSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        document.querySelectorAll('#shopTableBody tr').forEach(row => {
            const nameEl = row.querySelector('.shop-name-col');
            if(!nameEl) return;
            const name = nameEl.textContent.toLowerCase();
            row.style.display = name.includes(term) ? '' : 'none';
        });
    });

    function approveShop(profileId) {
        Swal.fire({
            title: 'Duyệt cửa hàng này?',
            text: "Cửa hàng sẽ được cấp quyền hoạt động ngay lập tức.",
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#d1d5db',
            confirmButtonText: 'Có, Duyệt ngay',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                updateShopStatus(profileId, 'APPROVED', '');
            }
        });
    }

    function showRejectModal(profileId) {
        document.getElementById('rejectProfileId').value = profileId;
        document.getElementById('rejectionReason').value = '';
        document.getElementById('rejectModal').classList.remove('hidden');
    }

    function closeRejectModal() {
        document.getElementById('rejectModal').classList.add('hidden');
    }

    function submitReject() {
        const profileId = document.getElementById('rejectProfileId').value;
        const reason = document.getElementById('rejectionReason').value.trim();
        if(!reason) {
            Swal.fire('Lỗi', 'Vui lòng nhập lý do từ chối', 'warning');
            return;
        }
        closeRejectModal();
        updateShopStatus(profileId, 'REJECTED', reason);
    }

    function updateShopStatus(profileId, status, reason) {
        fetch('${pageContext.request.contextPath}/admin/shops/approve', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: 'profileId=' + profileId + '&status=' + status + '&rejectionReason=' + encodeURIComponent(reason) + '&_csrf=${sessionScope._csrfToken}'
        })
        .then(handleJSONResponse)
        .then(data => {
            if(data.success) {
                Swal.fire({ icon: 'success', title: 'Thành công', text: data.message, timer: 1500, showConfirmButton: false });
                const badgeContainer = document.getElementById('status-badge-' + profileId);
                const actionContainer = document.getElementById('action-btns-' + profileId);
                if(status === 'APPROVED') {
                    badgeContainer.className = 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold';
                    badgeContainer.innerHTML = '<i class="fa-solid fa-check-circle text-[10px]"></i> Đã Duyệt';
                } else if(status === 'REJECTED') {
                    badgeContainer.className = 'inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-xs font-bold';
                    badgeContainer.innerHTML = '<i class="fa-solid fa-times-circle text-[10px]"></i> Từ Chối';
                    badgeContainer.title = reason;
                }
                actionContainer.innerHTML = '<span class="text-txt-3 text-xs italic block">Đã xử lý</span>';
            } else {
                Swal.fire('Lỗi', data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire('Lỗi', error.message || 'Lỗi kết nối mạng.', 'error');
        });
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
                if(data.success && data.data) {
                    const shop = data.data;
                    document.getElementById('detailName').textContent = shop.shopName || '';
                    document.getElementById('detailEmail').textContent = shop.businessEmail || '';
                    document.getElementById('detailAddress').textContent = shop.deliveryAddress || '';
                    document.getElementById('detailDescription').textContent = shop.shopDescription || 'Không có mô tả.';
                    
                    let statusHtml = '';
                    if (shop.approvalStatus === 'PENDING') statusHtml = '<span class="text-amber-600">⏳ Đang chờ duyệt</span>';
                    else if (shop.approvalStatus === 'APPROVED') statusHtml = '<span class="text-emerald-600">✅ Đã duyệt</span>';
                    else if (shop.approvalStatus === 'REJECTED') statusHtml = '<span class="text-red-600">❌ Từ chối</span>';
                    document.getElementById('detailStatus').innerHTML = statusHtml;

                    // Categories
                    const catContainer = document.getElementById('detailCategories');
                    catContainer.innerHTML = '';
                    if (shop.preferredCategories) {
                        try {
                            const catIds = JSON.parse(shop.preferredCategories);
                            if (catIds && catIds.length > 0) {
                                catIds.forEach(id => {
                                    const catName = categoryMap[id] || ('Danh mục ' + id);
                                    catContainer.innerHTML += `<span class="bg-primary/10 text-primary px-3 py-1 rounded-full text-xs font-semibold">` + catName + `</span>`;
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

                    // Docs
                    const docContainer = document.getElementById('detailDocs');
                    docContainer.innerHTML = '';
                    if (shop.docPaths) {
                        try {
                            const paths = JSON.parse(shop.docPaths);
                            if (paths && paths.length > 0) {
                                paths.forEach(path => {
                                    const fileName = path.split('/').pop().split('\\').pop();
                                    const fileUrl = '${pageContext.request.contextPath}/shop/docs?path=' + encodeURIComponent(path);
                                    docContainer.innerHTML += `<li class="flex items-center justify-between p-3 border border-border rounded-lg bg-white">
                                        <div class="flex items-center gap-2 overflow-hidden">
                                            <i class="fa-solid fa-file-alt text-primary text-lg"></i>
                                            <span class="text-xs text-txt truncate">` + fileName + `</span>
                                        </div>
                                        <div class="flex gap-2">
                                            <a href="` + fileUrl + `" class="text-emerald-600 hover:text-emerald-800" title="Tải xuống"><i class="fa-solid fa-download"></i></a>
                                        </div>
                                    </li>`;
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
                console.error('Error:', error);
                Swal.fire('Lỗi', error.message || 'Lỗi kết nối mạng.', 'error');
            });
    }

    window.onclick = e => {
        if (e.target === document.getElementById('rejectModal')) closeRejectModal();
        if (e.target === document.getElementById('detailModal')) closeDetailModal();
    };
</script>
</body>
</html>
