<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Kiểm Duyệt Đánh Giá - Admin Dashboard" />
</jsp:include>

<div class="bg-gray-50 min-h-screen text-on-surface antialiased flex font-sans pt-[72px]">
    <!-- SIDEBAR -->
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp" />

    <!-- MAIN CONTENT -->
    <main class="flex-grow p-6 lg:p-8 ml-0 lg:ml-64 transition-all duration-300">
        <!-- Page Header -->
        <header class="mb-8 flex flex-col md:flex-row justify-between md:items-end gap-4">
            <div>
                <h1 class="text-2xl md:text-3xl font-bold text-gray-900 tracking-tight">Kiểm Duyệt Đánh Giá</h1>
                <p class="text-sm text-gray-500 mt-1 font-light">Quản lý nội dung phản hồi từ người dùng, ẩn các đánh giá spam, không phù hợp.</p>
            </div>
            
            <div class="flex items-center gap-3">
                <span class="text-sm font-medium bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary text-[18px]">forum</span>
                    Tổng số Đánh giá: 
                    <span class="font-bold text-gray-900">${fn:length(reviewList)}</span>
                </span>
            </div>
        </header>

        <!-- DATA TABLE -->
        <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden relative z-10">
            <!-- Table Header Filters -->
            <div class="p-5 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                <h2 class="text-base font-semibold text-gray-800 flex items-center gap-2">
                    <span class="material-symbols-outlined text-gray-500">rate_review</span>
                    Danh sách Đánh giá
                </h2>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-[18px]">search</span>
                    <input type="text" id="reviewSearch" placeholder="Tìm theo nội dung, sản phẩm..." 
                           class="pl-9 pr-4 py-2 bg-white border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none w-64 transition-all shadow-sm">
                </div>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse whitespace-nowrap">
                    <thead>
                        <tr class="bg-gray-50 text-gray-500 text-xs uppercase tracking-wider font-semibold border-b border-gray-100">
                            <th class="px-6 py-4 font-semibold w-1/4">Sản phẩm / Người dùng</th>
                            <th class="px-6 py-4 font-semibold w-1/12 text-center">Đánh giá</th>
                            <th class="px-6 py-4 font-semibold w-1/2">Nội dung</th>
                            <th class="px-6 py-4 font-semibold text-center w-1/12">Trạng thái</th>
                            <th class="px-6 py-4 font-semibold text-center w-1/12">Ẩn / Hiện</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100 text-sm" id="reviewTableBody">
                        <c:if test="${empty reviewList}">
                            <tr>
                                <td colspan="5" class="px-6 py-12 text-center text-gray-400">
                                    <span class="material-symbols-outlined text-4xl mb-2 opacity-50">speaker_notes_off</span>
                                    <p>Chưa có đánh giá nào trên hệ thống.</p>
                                </td>
                            </tr>
                        </c:if>
                        <c:forEach var="review" items="${reviewList}">
                            <tr class="hover:bg-gray-50/80 transition-colors">
                                <td class="px-6 py-4">
                                    <div class="flex items-start gap-3">
                                        <div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-primary flex-shrink-0">
                                            <span class="material-symbols-outlined text-[20px]">person</span>
                                        </div>
                                        <div class="overflow-hidden">
                                            <p class="font-bold text-gray-900 truncate" title="${fn:escapeXml(review.productName)}">
                                                <c:out value="${review.productName}"/>
                                            </p>
                                            <p class="text-xs text-gray-500 mt-0.5 truncate">Bởi: <span class="font-medium text-gray-700"><c:out value="${review.customerName}"/></span></p>
                                        </div>
                                    </div>
                                </td>
                                <td class="px-6 py-4 text-center">
                                    <div class="flex items-center justify-center text-amber-500">
                                        <c:forEach begin="1" end="${review.rating}">
                                            <span class="material-symbols-outlined text-[16px]" style="font-variation-settings: 'FILL' 1;">star</span>
                                        </c:forEach>
                                        <c:forEach begin="${review.rating + 1}" end="5">
                                            <span class="material-symbols-outlined text-[16px]">star</span>
                                        </c:forEach>
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-normal">
                                    <p class="text-gray-600 line-clamp-2 text-sm leading-relaxed" title="${fn:escapeXml(review.reviewText)}">
                                        <c:out value="${review.reviewText}"/>
                                    </p>
                                    <c:if test="${not empty review.reviewImageUrl}">
                                        <a href="${review.reviewImageUrl}" target="_blank" class="text-primary text-xs flex items-center gap-1 hover:underline mt-1">
                                            <span class="material-symbols-outlined text-[14px]">image</span> Xem ảnh đính kèm
                                        </a>
                                    </c:if>
                                </td>
                                <td class="px-6 py-4 text-center" id="status-col-${review.reviewId}">
                                    <c:choose>
                                        <c:when test="${review.isHidden}">
                                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-600 border border-gray-200">
                                                <span class="material-symbols-outlined text-[14px]">visibility_off</span> Đã Ẩn
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600 border border-emerald-200">
                                                <span class="material-symbols-outlined text-[14px]">visibility</span> Đang Hiện
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="px-6 py-4 text-center">
                                    <!-- Toggle Switch Component -->
                                    <label class="relative inline-flex items-center cursor-pointer">
                                        <input type="checkbox" class="sr-only peer" 
                                               onchange="toggleReviewVisibility(${review.reviewId}, !this.checked)"
                                               ${!review.isHidden ? 'checked' : ''}>
                                        <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-500 shadow-inner"></div>
                                    </label>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    // Search filter
    document.getElementById('reviewSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        const rows = document.querySelectorAll('#reviewTableBody tr');
        rows.forEach(row => {
            if(row.children.length === 1) return; // empty state row
            const textContent = row.textContent.toLowerCase();
            if(textContent.includes(term)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    function toggleReviewVisibility(reviewId, isHidden) {
        fetch('${pageContext.request.contextPath}/admin/reviews/visibility', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'reviewId=' + reviewId + '&isHidden=' + isHidden + '&_csrf=${sessionScope._csrfToken}'
        })
        .then(response => response.json())
        .then(data => {
            if(data.success) {
                // Update Badge UI Without Refreshing
                const statusCol = document.getElementById('status-col-' + reviewId);
                if(isHidden) {
                    statusCol.innerHTML = '<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-600 border border-gray-200"><span class="material-symbols-outlined text-[14px]">visibility_off</span> Đã Ẩn</span>';
                } else {
                    statusCol.innerHTML = '<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600 border border-emerald-200"><span class="material-symbols-outlined text-[14px]">visibility</span> Đang Hiện</span>';
                }

                // Show toast notification
                const Toast = Swal.mixin({
                    toast: true,
                    position: 'bottom-end',
                    showConfirmButton: false,
                    timer: 2000,
                    timerProgressBar: true
                });
                Toast.fire({
                    icon: 'success',
                    title: data.message
                });
            } else {
                Swal.fire('Lỗi', data.message, 'error');
                // Revert toggle if failed (requires reloading page for simplicity if edge case)
                setTimeout(() => window.location.reload(), 1500);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire('Lỗi', 'Lỗi kết nối mạng.', 'error');
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
