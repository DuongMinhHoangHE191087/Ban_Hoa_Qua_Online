<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm duyệt đánh giá – Admin Verdant Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:      '#4d661c',
                        'primary-dk': '#364e03',
                        'primary-lt': '#f0f7e6',
                        surface:      '#ffffff',
                        'surface-2':  '#f8fafc',
                        border:       '#e2ece7',
                        'txt':        '#0f172a',
                        'txt-2':      '#475569',
                        'txt-3':      '#94a3b8',
                    },
                    fontFamily: {
                        sans: ['Segoe UI','-apple-system','BlinkMacSystemFont','Helvetica Neue','Arial','sans-serif'],
                    },
                    boxShadow: {
                        card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Segoe UI',-apple-system,sans-serif; }
        .glass-card {
            background:#fff;
            border:1px solid #e2ece7;
            border-radius:1rem;
            box-shadow:0 1px 3px rgba(0,0,0,.05),0 4px 16px -4px rgba(20,83,45,.06);
        }
        tbody tr { transition:background .12s; }
        tbody tr:hover td { background:#f8fafc; }
        
        /* Toggle Switch CSS */
        .switch {
            position: relative; display: inline-block; width: 44px; height: 24px;
        }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider {
            position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
            background-color: #cbd5e1; transition: .4s; border-radius: 24px;
        }
        .slider:before {
            position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px;
            background-color: white; transition: .4s; border-radius: 50%;
        }
        input:checked + .slider { background-color: #10b981; }
        input:checked + .slider:before { transform: translateX(20px); }
    </style>
</head>
<body>
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="reviews"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Kiểm Duyệt Đánh Giá</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Quản lý phản hồi khách hàng, ẩn đánh giá không phù hợp hoặc spam.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-comments text-amber-500"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Reviews</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Review list panel --%>
        <div class="glass-card">
            <%-- Filter/Search bar --%>
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-star text-amber-500 mr-1"></i> Đánh Giá Từ Khách Hàng</h3>
                <div class="relative w-full sm:w-64">
                    <input type="text" id="reviewSearch" placeholder="Tìm tên sản phẩm, khách hàng..." 
                           class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none pl-9 pr-4 py-2 text-xs bg-white transition-all">
                    <i class="fa-solid fa-search text-txt-3 absolute left-3 top-2.5 text-xs"></i>
                </div>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold" style="width: 25%">Sản phẩm / Người dùng</th>
                            <th class="px-6 py-3.5 font-bold text-center">Rating</th>
                            <th class="px-6 py-3.5 font-bold" style="width: 40%">Nội dung đánh giá</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Duyệt / Từ chối</th>
                        </tr>
                    </thead>
                    <tbody id="reviewTableBody" class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty reviewList}">
                                <tr>
                                    <td colspan="5" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Chưa có đánh giá nào trên hệ thống.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="review" items="${reviewList}">
                                    <tr>
                                        <%-- Product / User --%>
                                        <td class="px-6 py-4">
                                            <strong class="searchable-text text-primary text-sm block font-bold">${review.productName}</strong>
                                            <span class="searchable-text text-xs text-txt-2 block mt-1">Bởi: <b class="text-txt">${review.customerName}</b></span>
                                        </td>
                                        <%-- Rating --%>
                                        <td class="px-6 py-4 text-center whitespace-nowrap text-amber-400 text-xs">
                                            <c:forEach begin="1" end="${review.rating}"><i class="fa-solid fa-star"></i></c:forEach>
                                            <c:forEach begin="${review.rating + 1}" end="5"><i class="fa-regular fa-star text-slate-300"></i></c:forEach>
                                        </td>
                                        <%-- Review text & Image --%>
                                        <td class="px-6 py-4 text-xs text-txt-2">
                                            <div class="searchable-text max-w-[320px] truncate font-medium text-txt" title="${fn:escapeXml(review.reviewText)}">
                                                ${review.reviewText}
                                            </div>
                                            <c:if test="${not empty review.reviewImageUrl}">
                                                <a href="${review.reviewImageUrl}" target="_blank" 
                                                   class="inline-flex items-center gap-1 mt-1.5 text-[10px] font-bold text-primary hover:text-primary-dk hover:underline">
                                                    <i class="fa-solid fa-image"></i> Xem ảnh đính kèm
                                                </a>
                                            </c:if>
                                        </td>
                                        <%-- Status Badge --%>
                                        <td class="px-6 py-4 text-center" id="status-col-${review.reviewId}">
                                            <c:choose>
                                                <c:when test="${review.isHidden}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold">
                                                        <i class="fa-solid fa-circle-xmark text-[10px]"></i> Đã từ chối
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                        <i class="fa-solid fa-circle-check text-[10px]"></i> Đã duyệt
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <%-- Action buttons --%>
                                        <td class="px-6 py-4 text-center">
                                            <div class="flex justify-center gap-2">
                                                <c:if test="${review.isHidden}">
                                                    <button type="button" onclick="moderateReview('${review.reviewId}', 'approve')"
                                                            class="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm transition-all">
                                                        <i class="fa-solid fa-check"></i> Duyệt
                                                    </button>
                                                </c:if>
                                                <c:if test="${not review.isHidden}">
                                                    <button type="button" onclick="moderateReview('${review.reviewId}', 'reject')"
                                                            class="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold shadow-sm transition-all">
                                                        <i class="fa-solid fa-xmark"></i> Từ chối
                                                    </button>
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
        </div>

    </main>
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

    document.getElementById('reviewSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        document.querySelectorAll('#reviewTableBody tr').forEach(row => {
            const searchableTexts = row.querySelectorAll('.searchable-text');
            if(searchableTexts.length === 0) return; // empty row
            let match = false;
            searchableTexts.forEach(el => {
                if(el.textContent.toLowerCase().includes(term)) match = true;
            });
            row.style.display = match ? '' : 'none';
        });
    });

    function moderateReview(reviewId, action) {
        fetch('${pageContext.request.contextPath}/admin/reviews/visibility', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: 'reviewId=' + reviewId + '&action=' + encodeURIComponent(action) + '&_csrf=${sessionScope._csrfToken}'
        })
        .then(handleJSONResponse)
        .then(data => {
            if(data.success) {
                const Toast = Swal.mixin({ toast: true, position: 'bottom-end', showConfirmButton: false, timer: 2000, timerProgressBar: true });
                Toast.fire({ icon: 'success', title: data.message });
                setTimeout(() => window.location.reload(), 800);
            } else {
                Swal.fire('Lỗi', data.message, 'error');
                setTimeout(() => window.location.reload(), 1500);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire('Lỗi', error.message || 'Lỗi kết nối mạng.', 'error');
        });
    }
</script>
</body>
</html>
