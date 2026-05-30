<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Phê Duyệt Cửa Hàng - Admin Dashboard" />
</jsp:include>

<div class="bg-gray-50 min-h-screen text-on-surface antialiased flex font-sans pt-[72px]">
    <!-- SIDEBAR -->
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp" />

    <!-- MAIN CONTENT -->
    <main class="flex-grow p-6 lg:p-8 ml-0 lg:ml-64 transition-all duration-300">
        <!-- Page Header -->
        <header class="mb-8 flex flex-col md:flex-row justify-between md:items-end gap-4">
            <div>
                <h1 class="text-2xl md:text-3xl font-bold text-gray-900 tracking-tight">Phê Duyệt Cửa Hàng</h1>
                <p class="text-sm text-gray-500 mt-1 font-light">Quản lý và duyệt các yêu cầu mở cửa hàng mới của User.</p>
            </div>
            
            <div class="flex items-center gap-3">
                <span class="text-sm font-medium bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 flex items-center gap-2">
                    <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span> 
                    Chờ duyệt: 
                    <span class="font-bold text-gray-900">
                        <c:set var="pendingCount" value="0"/>
                        <c:forEach var="shop" items="${shopList}">
                            <c:if test="${shop.approvalStatus == 'PENDING'}">
                                <c:set var="pendingCount" value="${pendingCount + 1}"/>
                            </c:if>
                        </c:forEach>
                        ${pendingCount}
                    </span>
                </span>
            </div>
        </header>

        <!-- DATA TABLE -->
        <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden relative z-10">
            <!-- Table Header Filters (Optional) -->
            <div class="p-5 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                <h2 class="text-base font-semibold text-gray-800 flex items-center gap-2">
                    <span class="material-symbols-outlined text-gray-500">store</span>
                    Danh sách yêu cầu
                </h2>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-[18px]">search</span>
                    <input type="text" id="shopSearch" placeholder="Tìm tên shop..." 
                           class="pl-9 pr-4 py-2 bg-white border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none w-64 transition-all shadow-sm">
                </div>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse whitespace-nowrap">
                    <thead>
                        <tr class="bg-gray-50 text-gray-500 text-xs uppercase tracking-wider font-semibold border-b border-gray-100">
                            <th class="px-6 py-4 font-semibold">Tên Cửa Hàng</th>
                            <th class="px-6 py-4 font-semibold">Mô tả</th>
                            <th class="px-6 py-4 font-semibold">Địa chỉ giao/nhận</th>
                            <th class="px-6 py-4 font-semibold text-center">Trạng thái</th>
                            <th class="px-6 py-4 font-semibold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100 text-sm" id="shopTableBody">
                        <c:if test="${empty shopList}">
                            <tr>
                                <td colspan="5" class="px-6 py-12 text-center text-gray-400">
                                    <span class="material-symbols-outlined text-4xl mb-2 opacity-50">inbox</span>
                                    <p>Không có yêu cầu mở shop nào.</p>
                                </td>
                            </tr>
                        </c:if>
                        <c:forEach var="shop" items="${shopList}">
                            <tr class="hover:bg-gray-50/80 transition-colors">
                                <td class="px-6 py-4">
                                    <div class="flex items-center gap-3">
                                        <div class="w-10 h-10 rounded-xl bg-orange-100 flex items-center justify-center text-orange-600 shadow-sm border border-orange-200">
                                            <span class="material-symbols-outlined text-[20px]">storefront</span>
                                        </div>
                                        <div>
                                            <p class="font-bold text-gray-900"><c:out value="${shop.shopName}"/></p>
                                            <p class="text-[11px] text-gray-500 font-mono mt-0.5">User ID: ${shop.userId}</p>
                                        </div>
                                    </div>
                                </td>
                                <td class="px-6 py-4">
                                    <p class="text-gray-600 truncate max-w-[200px]" title="${fn:escapeXml(shop.shopDescription)}">
                                        <c:out value="${shop.shopDescription}"/>
                                    </p>
                                </td>
                                <td class="px-6 py-4">
                                    <p class="text-gray-600 truncate max-w-[200px]" title="${fn:escapeXml(shop.deliveryAddress)}">
                                        <c:out value="${shop.deliveryAddress}"/>
                                    </p>
                                </td>
                                <td class="px-6 py-4 text-center">
                                    <c:choose>
                                        <c:when test="${shop.approvalStatus == 'PENDING'}">
                                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-600 border border-amber-200" id="status-badge-${shop.profileId}">
                                                <span class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span> Chờ Duyệt
                                            </span>
                                        </c:when>
                                        <c:when test="${shop.approvalStatus == 'APPROVED'}">
                                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600 border border-emerald-200" id="status-badge-${shop.profileId}">
                                                <span class="material-symbols-outlined text-[14px]">check_circle</span> Đã Duyệt
                                            </span>
                                        </c:when>
                                        <c:when test="${shop.approvalStatus == 'REJECTED'}">
                                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-50 text-red-600 border border-red-200" id="status-badge-${shop.profileId}" title="${fn:escapeXml(shop.rejectionReason)}">
                                                <span class="material-symbols-outlined text-[14px]">cancel</span> Từ chối
                                            </span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td class="px-6 py-4 text-center">
                                    <div class="flex justify-center items-center gap-2" id="action-btns-${shop.profileId}">
                                        <c:if test="${shop.approvalStatus == 'PENDING'}">
                                            <button onclick="approveShop(${shop.profileId})"
                                                    class="p-2 bg-emerald-50 text-emerald-600 hover:bg-emerald-600 hover:text-white rounded-lg transition-colors border border-emerald-200 shadow-sm"
                                                    title="Duyệt cửa hàng">
                                                <span class="material-symbols-outlined text-[18px]">check</span>
                                            </button>
                                            <button onclick="showRejectModal(${shop.profileId})"
                                                    class="p-2 bg-red-50 text-red-600 hover:bg-red-600 hover:text-white rounded-lg transition-colors border border-red-200 shadow-sm"
                                                    title="Từ chối">
                                                <span class="material-symbols-outlined text-[18px]">close</span>
                                            </button>
                                        </c:if>
                                        <c:if test="${shop.approvalStatus != 'PENDING'}">
                                            <span class="text-xs text-gray-400 italic">Đã xử lý</span>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<!-- Reject Modal -->
<div id="rejectModal" class="fixed inset-0 bg-gray-900/50 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-white rounded-3xl shadow-xl w-full max-w-md p-6 transform scale-95 opacity-0 transition-all duration-300" id="rejectModalContent">
        <h3 class="text-xl font-bold text-gray-900 mb-2">Từ chối mở cửa hàng</h3>
        <p class="text-sm text-gray-500 mb-6">Vui lòng nhập lý do từ chối để người dùng biết và khắc phục.</p>
        
        <input type="hidden" id="rejectProfileId">
        <textarea id="rejectionReason" rows="3" class="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all resize-none" placeholder="Ví dụ: Giấy tờ không hợp lệ, mô tả thiếu chi tiết..."></textarea>
        
        <div class="flex justify-end gap-3 mt-6">
            <button onclick="closeRejectModal()" class="px-5 py-2.5 rounded-xl font-semibold text-gray-600 hover:bg-gray-100 transition-colors text-sm">Hủy</button>
            <button onclick="submitReject()" class="px-5 py-2.5 rounded-xl font-semibold text-white bg-red-500 hover:bg-red-600 shadow-md shadow-red-500/20 transition-colors text-sm">Xác nhận Từ chối</button>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    // Search filter
    document.getElementById('shopSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        const rows = document.querySelectorAll('#shopTableBody tr');
        rows.forEach(row => {
            if(row.children.length === 1) return; // empty state row
            const name = row.querySelector('.font-bold').textContent.toLowerCase();
            if(name.includes(term)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
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
        const modal = document.getElementById('rejectModal');
        const content = document.getElementById('rejectModalContent');
        modal.classList.remove('hidden');
        setTimeout(() => {
            content.classList.remove('scale-95', 'opacity-0');
            content.classList.add('scale-100', 'opacity-100');
        }, 10);
    }

    function closeRejectModal() {
        const modal = document.getElementById('rejectModal');
        const content = document.getElementById('rejectModalContent');
        content.classList.remove('scale-100', 'opacity-100');
        content.classList.add('scale-95', 'opacity-0');
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300);
    }

    function submitReject() {
        const profileId = document.getElementById('rejectProfileId').value;
        const reason = document.getElementById('rejectionReason').value.trim();
        if(!reason) {
            Swal.fire('Lỗi', 'Vui lòng nhập lý do từ chối', 'error');
            return;
        }
        closeRejectModal();
        updateShopStatus(profileId, 'REJECTED', reason);
    }

    function updateShopStatus(profileId, status, reason) {
        fetch('${pageContext.request.contextPath}/admin/shops/approve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'profileId=' + profileId + '&status=' + status + '&rejectionReason=' + encodeURIComponent(reason) + '&_csrf=${sessionScope._csrfToken}'
        })
        .then(response => response.json())
        .then(data => {
            if(data.success) {
                Swal.fire({
                    icon: 'success',
                    title: 'Thành công',
                    text: data.message,
                    timer: 1500,
                    showConfirmButton: false
                });
                
                // Update UI dynamically
                const badgeContainer = document.getElementById('status-badge-' + profileId);
                const actionContainer = document.getElementById('action-btns-' + profileId);
                
                if(status === 'APPROVED') {
                    badgeContainer.outerHTML = '<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600 border border-emerald-200" id="status-badge-'+profileId+'"><span class="material-symbols-outlined text-[14px]">check_circle</span> Đã Duyệt</span>';
                } else if(status === 'REJECTED') {
                    badgeContainer.outerHTML = '<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-50 text-red-600 border border-red-200" id="status-badge-'+profileId+'" title="'+reason+'"><span class="material-symbols-outlined text-[14px]">cancel</span> Từ chối</span>';
                }
                
                actionContainer.innerHTML = '<span class="text-xs text-gray-400 italic">Đã xử lý</span>';
            } else {
                Swal.fire('Lỗi', data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire('Lỗi', 'Lỗi kết nối mạng.', 'error');
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
