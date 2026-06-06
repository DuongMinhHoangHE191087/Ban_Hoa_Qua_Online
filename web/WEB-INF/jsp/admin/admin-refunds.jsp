<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu cầu Đổi trả – Admin MetaFruit</title>
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
        select {
            appearance: none;
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%234d661c' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            background-size: 12px 8px;
            padding-right: 2.25rem;
        }
        .page-btn {
            display:inline-flex; align-items:center; gap:.3rem;
            padding:.4rem .875rem; border-radius:.625rem;
            font-size:.78rem; font-weight:600;
            border:1px solid #d1d5db; background:#fff;
            color:#374151; cursor:pointer; transition:all .15s;
            text-decoration:none;
        }
        .page-btn:hover { background:#f1f5f9; border-color:#9ca3af; text-decoration:none; }
        .page-btn-active { background:#4d661c; border-color:#4d661c; color:#fff; }
        .page-btn-active:hover { background:#364e03; border-color:#364e03; color:#fff; }
    </style>
</head>
<body>
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="returns"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Quản Lý Đổi Trả / Hoàn Tiền</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Xử lý yêu cầu trả hàng, hoàn tiền từ khách hàng đối với các đơn lỗi.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-rotate-left text-red-500"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Đổi trả</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Filter and Lists Card --%>
        <div class="glass-card">
            <%-- Filter bar --%>
            <form action="" method="GET" class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-wrap gap-4 items-center justify-between">
                <div class="flex items-center gap-2">
                    <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-list-check text-primary mr-1"></i> Danh Sách Yêu Cầu</h3>
                </div>
                <div class="flex items-center gap-2">
                    <select name="status" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white transition-all cursor-pointer">
                        <option value="" ${empty paramStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ duyệt (PENDING)</option>
                        <option value="PROCESSING" ${paramStatus == 'PROCESSING' ? 'selected' : ''}>Đang xử lý (PROCESSING)</option>
                        <option value="APPROVED" ${paramStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt (APPROVED)</option>
                        <option value="COMPLETED" ${paramStatus == 'COMPLETED' ? 'selected' : ''}>Hoàn tất (COMPLETED)</option>
                        <option value="REJECTED" ${paramStatus == 'REJECTED' ? 'selected' : ''}>Đã từ chối (REJECTED)</option>
                    </select>
                    <button type="submit" class="bg-primary hover:bg-primary-dk text-white font-bold px-4 py-2 rounded-xl text-xs shadow active:scale-95 cursor-pointer">
                        <i class="fa-solid fa-filter mr-0.5"></i> Lọc
                    </button>
                </div>
            </form>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">Mã YC</th>
                            <th class="px-6 py-3.5 font-bold">Mã Đơn</th>
                            <th class="px-6 py-3.5 font-bold">Khách Hàng</th>
                            <th class="px-6 py-3.5 font-bold">Lý Do</th>
                            <th class="px-6 py-3.5 font-bold">Mô tả chi tiết</th>
                            <th class="px-6 py-3.5 font-bold">Số tiền hoàn</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty requestList}">
                                <tr>
                                    <td colspan="8" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Không tìm thấy yêu cầu đổi trả nào.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="r" items="${requestList}">
                                    <tr>
                                        <td class="px-6 py-4 font-mono font-bold text-primary">#${r.returnRequestId}</td>
                                        <td class="px-6 py-4 font-bold text-txt">Đơn #${r.orderId}</td>
                                        <td class="px-6 py-4 text-txt-2 text-xs">UID #${r.customerId}</td>
                                        <td class="px-6 py-4 font-medium text-txt-2">${r.reasonCode}</td>
                                        <td class="px-6 py-4 text-xs text-txt-2 max-w-[200px] truncate" title="${r.description}">
                                            ${r.description}
                                        </td>
                                        <td class="px-6 py-4 font-extrabold text-red-600">
                                            <fmt:formatNumber value="${r.refundAmount}" type="number"/> đ
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${r.status == 'PENDING' || r.status == 'REQUESTED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold">
                                                        <i class="fa-solid fa-clock text-[10px]"></i> Chưa Duyệt
                                                    </span>
                                                </c:when>
                                                <c:when test="${r.status == 'PROCESSING'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-blue-50 border border-blue-100 text-blue-800 text-xs font-bold animate-pulse">
                                                        <i class="fa-solid fa-spinner fa-spin text-[10px]"></i> Đang Xử Lý
                                                    </span>
                                                </c:when>
                                                <c:when test="${r.status == 'APPROVED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-teal-50 border border-teal-100 text-teal-800 text-xs font-bold">
                                                        <i class="fa-solid fa-check text-[10px]"></i> Đã Duyệt Hoàn Tiền
                                                    </span>
                                                </c:when>
                                                <c:when test="${r.status == 'COMPLETED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                        <i class="fa-solid fa-check-circle text-[10px]"></i> Đã Hoàn Tiền
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-xs font-bold">
                                                        <i class="fa-solid fa-times-circle text-[10px]"></i> Từ Chối
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${r.status == 'PENDING' || r.status == 'REQUESTED'}">
                                                    <div class="flex flex-col gap-2 w-full max-w-[120px] mx-auto">
                                                        <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" class="w-full">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="process">
                                                            <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                            <input type="hidden" name="orderId" value="${r.orderId}">
                                                            <input type="hidden" name="decisionReason" value="Đang xem xét">
                                                            <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-1.5 rounded-lg text-xs transition-all shadow-sm">
                                                                Đang xử lý
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" onsubmit="return confirmReject(event, '${r.returnRequestId}')" class="w-full">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="reject">
                                                            <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                            <input type="hidden" name="orderId" value="${r.orderId}">
                                                            <input type="hidden" name="decisionReason" value="Từ chối">
                                                            <button type="submit" class="w-full bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 font-bold py-1.5 rounded-lg text-xs transition-all">
                                                                Từ chối
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:when test="${r.status == 'PROCESSING'}">
                                                    <div class="flex flex-col gap-2 w-full max-w-[120px] mx-auto">
                                                        <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" onsubmit="return confirmApprove(event, '${r.returnRequestId}')" class="w-full">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="approve">
                                                            <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                            <input type="hidden" name="orderId" value="${r.orderId}">
                                                            <input type="hidden" name="decisionReason" value="Hợp lệ">
                                                            <button type="submit" class="w-full bg-teal-600 hover:bg-teal-700 text-white font-bold py-1.5 rounded-lg text-xs transition-all shadow-sm">
                                                                Duyệt hoàn
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:when test="${r.status == 'APPROVED'}">
                                                    <div class="flex flex-col gap-2 w-full max-w-[120px] mx-auto">
                                                        <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" class="w-full" onsubmit="return confirm('Xác nhận đã chuyển khoản / hoàn tiền thành công?');">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="complete">
                                                            <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                            <input type="hidden" name="orderId" value="${r.orderId}">
                                                            <input type="hidden" name="decisionReason" value="Hoàn tiền thành công">
                                                            <button type="submit" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-1.5 rounded-lg text-xs transition-all shadow-sm">
                                                                Đã hoàn tiền
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-txt-3 text-xs italic block">Đã xử lý: ${r.decisionReason}</span>
                                                </c:otherwise>
                                            </c:choose>
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
                    <div class="flex gap-1.5">
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <a href="?page=${i}&status=${paramStatus}" 
                               class="page-btn ${i == currentPage ? 'page-btn-active' : ''}">${i}</a>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </div>

    </main>
</div>

<script>
    function confirmApprove(event, requestId) {
        event.preventDefault();
        Swal.fire({
            title: 'Chấp nhận hoàn tiền?',
            html: 'Đồng ý hoàn tiền cho yêu cầu <b>#' + requestId + '</b>.<br><small class="text-emerald-600 font-semibold">Thao tác này sẽ tự động hoàn trả số dư ví/ngân hàng nếu kết nối.</small>',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đồng ý duyệt',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }

    function confirmReject(event, requestId) {
        event.preventDefault();
        Swal.fire({
            title: 'Từ chối đổi trả?',
            html: 'Bạn sẽ từ chối hoàn tiền cho yêu cầu <b>#' + requestId + '</b>.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Xác nhận từ chối',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }
</script>
</body>
</html>
