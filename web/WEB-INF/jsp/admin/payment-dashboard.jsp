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
    <title>Giám sát thanh toán - Admin Verdant Market</title>
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
                        primary: '#4d661c',
                        'primary-dk': '#364e03',
                        'primary-lt': '#f0f7e6',
                        surface: '#ffffff',
                        'surface-2': '#f8fafc',
                        border: '#e2ece7',
                        txt: '#0f172a',
                        'txt-2': '#475569',
                        'txt-3': '#94a3b8'
                    },
                    fontFamily: {
                        sans: ['Lexend', 'sans-serif']
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
        .pagination-wrapper { padding: 0 !important; }
        .pagination { gap: 0.375rem !important; margin: 0 !important; }
        .pagination .page-link {
            display: inline-flex; align-items: center; justify-content: center;
            min-width: 2rem; height: 2rem; border-radius: 0.5rem;
            font-size: 0.75rem; font-weight: 600;
            border: 1px solid #e2ece7; background: #fff;
            color: #374151; cursor: pointer; transition: all 0.15s;
            text-decoration: none;
        }
        .pagination .page-item.active .page-link {
            background: #4d661c; border-color: #4d661c; color: #fff;
        }
        .pagination .page-item.disabled .page-link {
            color: #94a3b8; border-color: #e2ece7; background: #f8fafc; cursor: not-allowed;
        }
        .pagination .page-item .page-link:hover:not(.disabled) {
            background: #f1f5f9; border-color: #9ca3af;
        }
    </style>
</head>
<body>
<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="payments"/>
    </jsp:include>

    <main class="admin-main p-6 md:p-8 overflow-y-auto">
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Giám Sát Thanh Toán Toàn Sàn</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Theo dõi giao dịch, trạng thái thanh toán và dấu vết đối soát của toàn hệ thống.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-credit-card text-amber-500"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Payments</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <div class="glass-card">
            <form action="" method="GET" class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-wrap gap-4 items-end justify-between">
                <div class="flex flex-wrap gap-3 items-end">
                    <div class="flex flex-col gap-1 min-w-[180px]">
                        <span class="text-[10px] font-bold text-txt-2 uppercase tracking-wider">Trạng thái giao dịch</span>
                        <select name="status" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white">
                            <option value="" ${empty statusFilter ? 'selected' : ''}>Tất cả</option>
                            <option value="pending" ${statusFilter == 'pending' ? 'selected' : ''}>Pending</option>
                            <option value="processing" ${statusFilter == 'processing' ? 'selected' : ''}>Processing</option>
                            <option value="completed" ${statusFilter == 'completed' ? 'selected' : ''}>Completed</option>
                            <option value="failed" ${statusFilter == 'failed' ? 'selected' : ''}>Failed</option>
                            <option value="cancelled" ${statusFilter == 'cancelled' ? 'selected' : ''}>Cancelled</option>
                            <option value="refunded" ${statusFilter == 'refunded' ? 'selected' : ''}>Refunded</option>
                            <option value="expired" ${statusFilter == 'expired' ? 'selected' : ''}>Expired</option>
                        </select>
                    </div>
                    <div class="flex flex-col gap-1 min-w-[160px]">
                        <span class="text-[10px] font-bold text-txt-2 uppercase tracking-wider">Hình thức</span>
                        <select name="paymentMethod" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white">
                            <option value="" ${empty paymentMethod ? 'selected' : ''}>Tất cả</option>
                            <option value="CK" ${paymentMethod == 'CK' ? 'selected' : ''}>Chuyển khoản</option>
                            <option value="COD" ${paymentMethod == 'COD' ? 'selected' : ''}>COD</option>
                            <option value="SEPAY" ${paymentMethod == 'SEPAY' ? 'selected' : ''}>SePay</option>
                        </select>
                    </div>
                    <div class="flex flex-col gap-1 min-w-[260px]">
                        <span class="text-[10px] font-bold text-txt-2 uppercase tracking-wider">Từ khóa</span>
                        <input type="text" name="keyword" value="${param.keyword}" placeholder="Mã giao dịch, order, shop, khách..."
                               class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white">
                    </div>
                    <button type="submit" class="bg-primary text-white hover:bg-primary-dk font-bold px-5 py-2 rounded-xl text-xs transition-all shadow-sm flex items-center gap-1 active:scale-95 cursor-pointer">
                        <i class="fa-solid fa-search"></i> Lọc
                    </button>
                    <a href="?" class="bg-white border border-border hover:bg-slate-100 text-txt-2 font-bold px-4 py-2 rounded-xl text-xs transition-all flex items-center justify-center cursor-pointer">
                        Reset
                    </a>
                </div>
                <div class="text-xs text-txt-2 font-medium">
                    Tổng kết quả: <span class="font-bold text-primary">${totalCount}</span>
                </div>
            </form>

            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse text-sm">
                    <thead>
                        <tr class="bg-slate-50/50">
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Giao dịch</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Đơn hàng</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Khách / Shop</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Hình thức</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Trạng thái</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2 text-right">Số tiền</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Tham chiếu</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Mốc thời gian</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-border/60">
                        <c:choose>
                            <c:when test="${empty payments}">
                                <tr>
                                    <td colspan="8" class="p-8 text-center text-txt-3 italic">Không tìm thấy giao dịch nào.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${payments}">
                                    <tr class="hover:bg-primary-lt/20">
                                        <td class="p-3 font-mono font-bold text-primary">#${p.transactionId}</td>
                                        <td class="p-3">
                                            <a href="${pageContext.request.contextPath}/admin/orders" class="font-bold text-txt hover:text-primary">#${p.orderId}</a>
                                            <div class="text-[11px] text-txt-3">${p.orderStatus}</div>
                                        </td>
                                        <td class="p-3">
                                            <div class="font-semibold text-txt">${p.customerName}</div>
                                            <div class="text-[11px] text-txt-3">${empty p.shopName ? 'Hệ thống' : p.shopName}</div>
                                        </td>
                                        <td class="p-3">
                                            <span class="px-2 py-0.5 rounded text-[11px] font-bold ${p.paymentMethod == 'CK' ? 'bg-blue-50 text-blue-700 border border-blue-100' : (p.paymentMethod == 'COD' ? 'bg-amber-50 text-amber-700 border border-amber-100' : 'bg-violet-50 text-violet-700 border border-violet-100')}">
                                                ${p.paymentMethod}
                                            </span>
                                        </td>
                                        <td class="p-3">
                                            <c:choose>
                                                <c:when test="${p.paymentStatus == 'pending'}">
                                                    <span class="px-2.5 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-bold border border-gray-200">Pending</span>
                                                </c:when>
                                                <c:when test="${p.paymentStatus == 'processing'}">
                                                    <span class="px-2.5 py-1 bg-amber-100 text-amber-800 rounded-full text-xs font-bold border border-amber-200">Processing</span>
                                                </c:when>
                                                <c:when test="${p.paymentStatus == 'completed'}">
                                                    <span class="px-2.5 py-1 bg-emerald-100 text-emerald-800 rounded-full text-xs font-bold border border-emerald-200">Completed</span>
                                                </c:when>
                                                <c:when test="${p.paymentStatus == 'failed'}">
                                                    <span class="px-2.5 py-1 bg-red-100 text-red-800 rounded-full text-xs font-bold border border-red-200">Failed</span>
                                                </c:when>
                                                <c:when test="${p.paymentStatus == 'refunded'}">
                                                    <span class="px-2.5 py-1 bg-sky-100 text-sky-800 rounded-full text-xs font-bold border border-sky-200">Refunded</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="px-2.5 py-1 bg-slate-100 text-slate-700 rounded-full text-xs font-bold border border-slate-200">${p.paymentStatus}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="p-3 text-right font-extrabold text-[#ba1a1a] whitespace-nowrap">
                                            <ft:currency value="${p.amount}" />
                                        </td>
                                        <td class="p-3 text-xs">
                                            <div class="font-mono text-[11px] text-txt-2 break-all">${p.sepayReference}</div>
                                            <div class="font-mono text-[11px] text-txt-3 break-all">${p.sepayTransactionId}</div>
                                        </td>
                                        <td class="p-3 text-xs text-txt-2 whitespace-nowrap">
                                            <div>Khởi tạo: <fmt:formatDate value="${p.initiatedAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                                            <div>Hoàn tất: <c:choose><c:when test="${not empty p.completedAt}"><fmt:formatDate value="${p.completedAt}" pattern="dd/MM/yyyy HH:mm"/></c:when><c:otherwise>--</c:otherwise></c:choose></div>
                                            <div>Hết hạn: <c:choose><c:when test="${not empty p.expiresAt}"><fmt:formatDate value="${p.expiresAt}" pattern="dd/MM/yyyy HH:mm"/></c:when><c:otherwise>--</c:otherwise></c:choose></div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <c:if test="${totalPages > 1}">
                <div class="flex justify-between items-center px-6 py-4 border-t border-border gap-4">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="?status=${fn:escapeXml(statusFilter)}&paymentMethod=${fn:escapeXml(paymentMethod)}&keyword=${fn:escapeXml(param.keyword)}" />
                </div>
            </c:if>
        </div>
    </main>
</div>
</body>
</html>
