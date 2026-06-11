<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đối soát Thanh toán – Admin Verdant Market</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
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
                        sans: ['Lexend', 'sans-serif'],
                    },
                    boxShadow: {
                        card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Lexend', sans-serif; }
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
        <jsp:param name="activeMenu" value="settlements"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8 gap-4">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Đối Soát &amp; Thanh Toán Shop</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Tính toán phí nền tảng, hoàn tiền, và chuyển khoản doanh thu cho chủ shop.</p>
            </div>
            <div class="flex items-center gap-3">
                <form action="${pageContext.request.contextPath}/admin/settlements" method="POST" class="inline">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                    <input type="hidden" name="action" value="triggerSettlement">
                    <button type="submit" class="bg-primary hover:bg-[#364e03] text-white font-bold px-4 py-2.5 rounded-xl text-xs shadow-md active:scale-95 transition-all flex items-center gap-1.5 cursor-pointer">
                        <i class="fa-solid fa-arrows-rotate"></i>
                        <span>Chạy Auto-Settlement</span>
                    </button>
                </form>
                <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2.5 rounded-xl text-[#364e03] shadow-sm">
                    <i class="fa-solid fa-file-invoice-dollar text-[#84cc16]"></i>
                    <span class="text-xs font-bold uppercase tracking-wider">Settlements</span>
                </div>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Filter and Lists Card --%>
        <div class="glass-card">
            <%-- Filter bar --%>
            <form action="" method="GET" class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-wrap gap-4 items-center justify-between">
                <div class="flex items-center gap-2">
                    <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-calculator text-primary mr-1"></i> Báo Cáo Kỳ Đối Soát</h3>
                </div>
                <div class="flex items-center gap-2">
                    <select name="status" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white transition-all cursor-pointer">
                        <option value="" ${empty paramStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán (PENDING)</option>
                        <option value="PAID" ${paramStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán (PAID)</option>
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
                            <th class="px-6 py-3.5 font-bold">Mã ĐS</th>
                            <th class="px-6 py-3.5 font-bold">Cửa Hàng</th>
                            <th class="px-6 py-3.5 font-bold">Kỳ Đối Soát</th>
                            <th class="px-6 py-3.5 font-bold text-right">Doanh thu gộp</th>
                            <th class="px-6 py-3.5 font-bold text-right">Phí nền tảng</th>
                            <th class="px-6 py-3.5 font-bold text-right">Hoàn tiền</th>
                            <th class="px-6 py-3.5 font-bold text-right">Số tiền thực nhận</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty settlementList}">
                                <tr>
                                    <td colspan="9" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Không tìm thấy kỳ đối soát nào.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="s" items="${settlementList}">
                                    <tr>
                                        <td class="px-6 py-4 font-mono font-bold text-primary">#${s.settlementId}</td>
                                        <td class="px-6 py-4 font-semibold text-txt">Shop #${s.ownerId}</td>
                                        <td class="px-6 py-4 text-xs text-txt-2 whitespace-nowrap">
                                            <fmt:formatDate value="${s.periodStartAsDate}" pattern="dd/MM/yyyy"/> - 
                                            <fmt:formatDate value="${s.periodEndAsDate}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td class="px-6 py-4 text-right font-semibold text-txt">
                                            <fmt:formatNumber value="${s.grossAmount}" type="number"/> đ
                                        </td>
                                        <td class="px-6 py-4 text-right font-medium text-red-600 whitespace-nowrap">
                                            -<fmt:formatNumber value="${s.platformFeeAmount}" type="number"/> đ
                                        </td>
                                        <td class="px-6 py-4 text-right font-medium text-red-500 whitespace-nowrap">
                                            -<fmt:formatNumber value="${s.refundAmount}" type="number"/> đ
                                        </td>
                                        <td class="px-6 py-4 text-right font-black text-emerald-600 whitespace-nowrap bg-emerald-50/20">
                                            <fmt:formatNumber value="${s.netAmount}" type="number"/> đ
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${s.status == 'PENDING'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold">
                                                        <i class="fa-solid fa-clock text-[10px]"></i> Chờ TT
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                        <i class="fa-solid fa-check-circle text-[10px]"></i> Đã Thanh Toán
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${s.status == 'PENDING'}">
                                                    <form action="${pageContext.request.contextPath}/admin/settlements" method="POST" 
                                                          onsubmit="return confirmPayment(event, '${s.settlementId}', '${s.netAmount}')" class="inline-block">
                                                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                        <input type="hidden" name="action" value="markPaid">
                                                        <input type="hidden" name="settlementId" value="${s.settlementId}">
                                                        <button type="submit" 
                                                                class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all active:scale-95 cursor-pointer shadow-sm">
                                                            <i class="fa-solid fa-credit-card mr-1"></i> Thanh toán
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-txt-3 text-xs italic block">—</span>
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
    function confirmPayment(event, settlementId, amount) {
        event.preventDefault();
        
        // Format amount using JS locale formatting for display
        const formattedAmount = Number(amount).toLocaleString('vi-VN') + ' đ';
        
        Swal.fire({
            title: 'Xác nhận thanh toán?',
            html: 'Đánh dấu đã chuyển khoản thành công doanh thu thực nhận <b>' + formattedAmount + '</b> cho Shop ở kỳ đối soát <b>#' + settlementId + '</b>?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đúng, xác nhận',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }
</script>
</body>
</html>
