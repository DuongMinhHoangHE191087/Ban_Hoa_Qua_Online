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
    <title>Đối soát Thanh toán – Admin MetaFruit</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="settlements"/>
    </jsp:include>

    <main class="admin-main p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8 gap-4">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Đối Soát &amp; Thanh Toán Shop</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Tính toán phí nền tảng, hoàn tiền, và chuyển khoản doanh thu cho chủ shop.</p>
            </div>
            <div class="flex items-center gap-3">
                <form action="${pageContext.request.contextPath}/admin/settlements" method="POST" class="inline">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                    <input type="hidden" name="action" value="triggerSettlement">
                    <button type="submit" class="bg-primary hover:bg-primary-dk text-white font-bold px-4 py-2.5 rounded-xl text-xs shadow-md active:scale-95 transition-all flex items-center gap-1.5 cursor-pointer">
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

        <div class="glass-card">
            <form action="" method="GET" class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-wrap gap-4 items-center justify-between">
                <div class="flex items-center gap-2">
                    <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-calculator text-primary mr-1"></i> Báo Cáo Kỳ Đối Soát</h3>
                </div>
                <div class="flex items-center gap-2">
                    <select name="status" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white transition-all cursor-pointer">
                        <option value="" ${empty paramStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ shop xác nhận (PENDING)</option>
                        <option value="CONFIRMED" ${paramStatus == 'CONFIRMED' ? 'selected' : ''}>Chờ thanh toán (CONFIRMED)</option>
                        <option value="PAID" ${paramStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán (PAID)</option>
                        <option value="CANCELLED" ${paramStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy (CANCELLED)</option>
                    </select>
                    <select name="issueFilter" class="rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-xs bg-white transition-all cursor-pointer">
                        <option value="" ${empty paramIssueFilter ? 'selected' : ''}>Tất cả báo cáo</option>
                        <option value="OPEN" ${paramIssueFilter == 'OPEN' ? 'selected' : ''}>Đang báo chưa nhận tiền</option>
                        <option value="REPORTED" ${paramIssueFilter == 'REPORTED' ? 'selected' : ''}>Đã báo</option>
                        <option value="UNDER_REVIEW" ${paramIssueFilter == 'UNDER_REVIEW' ? 'selected' : ''}>Đang đối soát lại</option>
                        <option value="RESOLVED" ${paramIssueFilter == 'RESOLVED' ? 'selected' : ''}>Đã xử lý xong</option>
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
                                        <td class="px-6 py-4">
                                            <div class="font-semibold text-txt">${s.shopName}</div>
                                            <div class="text-[11px] text-txt-3 mt-0.5">Chủ shop: ${s.ownerName}</div>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-txt-2 whitespace-nowrap">
                                            <fmt:formatDate value="${s.periodStartAsDate}" pattern="dd/MM/yyyy"/>
                                            -
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
                                                        <i class="fa-solid fa-clock text-[10px]"></i> Chờ shop xác nhận
                                                    </span>
                                                </c:when>
                                                <c:when test="${s.status == 'CONFIRMED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-blue-50 border border-blue-100 text-blue-800 text-xs font-bold">
                                                        <i class="fa-solid fa-circle-check text-[10px]"></i> Chờ thanh toán
                                                    </span>
                                                </c:when>
                                                <c:when test="${s.status == 'PAID'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                        <i class="fa-solid fa-check-circle text-[10px]"></i> Đã Thanh Toán
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-rose-50 border border-rose-100 text-rose-800 text-xs font-bold">
                                                        <i class="fa-solid fa-ban text-[10px]"></i> Đã hủy
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${s.status == 'PAID' || s.paymentIssueStatus == 'REPORTED' || s.paymentIssueStatus == 'UNDER_REVIEW' || s.paymentIssueStatus == 'RESOLVED'}">
                                                <div class="mt-2 space-y-1 text-[10px] text-txt-3 leading-relaxed">
                                                    <c:if test="${not empty s.paidReference}">
                                                        <div>GD: <span class="font-semibold text-txt">${s.paidReference}</span></div>
                                                    </c:if>
                                                    <c:if test="${not empty s.paidNote}">
                                                        <div>Ghi chú: <span class="font-semibold text-txt">${s.paidNote}</span></div>
                                                    </c:if>
                                                    <c:if test="${s.paymentIssueStatus == 'REPORTED'}">
                                                        <div class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-rose-50 border border-rose-100 text-rose-700 font-bold">
                                                            <i class="fa-solid fa-triangle-exclamation text-[9px]"></i> Có tranh chấp / báo lỗi
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${s.paymentIssueStatus == 'UNDER_REVIEW'}">
                                                        <div class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 font-bold">
                                                            <i class="fa-solid fa-magnifying-glass text-[9px]"></i> Đang đối soát lại
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${s.paymentIssueStatus == 'RESOLVED'}">
                                                        <div class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-700 font-bold">
                                                            <i class="fa-solid fa-circle-check text-[9px]"></i> Đã xử lý xong
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${s.status == 'PENDING'}">
                                                    <span class="text-txt-3 text-xs">Chờ shop xác nhận</span>
                                                </c:when>
                                                <c:when test="${s.status == 'CONFIRMED'}">
                                                    <form action="${pageContext.request.contextPath}/admin/settlements" method="POST"
                                                          onsubmit="return confirmPayment(event, this, '${s.settlementId}', '${s.netAmount}')" class="inline-block">
                                                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                        <input type="hidden" name="action" value="markPaid">
                                                        <input type="hidden" name="settlementId" value="${s.settlementId}">
                                                        <input type="hidden" name="paidReference" value="">
                                                        <input type="hidden" name="paidNote" value="">
                                                        <button type="submit" class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all active:scale-95 cursor-pointer shadow-sm">
                                                            <i class="fa-solid fa-credit-card mr-1"></i> Xác nhận TT
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:when test="${s.status == 'PAID' && (s.paymentIssueStatus == 'REPORTED' || s.paymentIssueStatus == 'UNDER_REVIEW')}">
                                                    <div class="flex flex-col items-center gap-2">
                                                        <form action="${pageContext.request.contextPath}/admin/settlements" method="POST"
                                                              onsubmit="return reviewPaymentIssue(event, this, '${s.settlementId}', 'resolve')" class="inline-block">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="resolveIssue">
                                                            <input type="hidden" name="settlementId" value="${s.settlementId}">
                                                            <input type="hidden" name="resolutionNote" value="">
                                                            <button type="submit" class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all active:scale-95 cursor-pointer shadow-sm">
                                                                <i class="fa-solid fa-circle-check mr-1"></i> Chốt đối soát
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/admin/settlements" method="POST"
                                                              onsubmit="return reviewPaymentIssue(event, this, '${s.settlementId}', 'reopen')" class="inline-block">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="reopenPayment">
                                                            <input type="hidden" name="settlementId" value="${s.settlementId}">
                                                            <input type="hidden" name="resolutionNote" value="">
                                                            <button type="submit" class="bg-amber-600 hover:bg-amber-700 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all active:scale-95 cursor-pointer shadow-sm">
                                                                <i class="fa-solid fa-rotate-right mr-1"></i> Mở lại TT
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:when test="${s.status == 'PAID'}">
                                                    <span class="text-txt-3 text-xs">Đã thanh toán</span>
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

            <c:if test="${totalPages > 1}">
                <div class="flex justify-between items-center px-6 py-4 border-t border-border gap-4">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="?status=${fn:escapeXml(paramStatus)}&issueFilter=${fn:escapeXml(paramIssueFilter)}" />
                </div>
            </c:if>
        </div>
    </main>
</div>

<script>
    function confirmPayment(event, form, settlementId, amount) {
        event.preventDefault();
        const formattedAmount = Number(amount).toLocaleString('vi-VN') + ' đ';
        Swal.fire({
            title: 'Xác nhận thanh toán?',
            html: 'Nhập mã giao dịch ngân hàng cho kỳ đối soát <b>#' + settlementId + '</b> với số tiền <b>' + formattedAmount + '</b>.',
            input: 'text',
            inputPlaceholder: 'VD: BIDV-20260702-001',
            inputAttributes: {
                autocapitalize: 'off',
                autocorrect: 'off'
            },
            inputValidator: (value) => {
                if (!value || !value.trim()) {
                    return 'Mã giao dịch là bắt buộc.';
                }
                return null;
            },
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đã chuyển khoản',
            cancelButtonText: 'Hủy'
        }).then(r => {
            if (r.isConfirmed) {
                form.querySelector('[name="paidReference"]').value = (r.value || '').trim();
                form.querySelector('[name="paidNote"]').value = 'Xác nhận thanh toán từ admin dashboard';
                form.submit();
            }
        });
        return false;
    }

    function reviewPaymentIssue(event, form, settlementId, mode) {
        event.preventDefault();
        const isResolve = mode === 'resolve';
        Swal.fire({
            title: isResolve ? 'Chốt đối soát?' : 'Mở lại thanh toán?',
            text: isResolve
                ? 'Nhập ghi chú xác nhận đã xử lý báo cáo cho settlement #' + settlementId + '.'
                : 'Nhập ghi chú để mở lại settlement #' + settlementId + ' cho bước thanh toán lại.',
            input: 'textarea',
            inputPlaceholder: isResolve
                ? 'Nhập kết quả kiểm tra / đối soát'
                : 'Nhập lý do mở lại thanh toán',
            inputAttributes: {
                'aria-label': isResolve ? 'Ghi chú chốt đối soát' : 'Ghi chú mở lại thanh toán'
            },
            inputValidator: (value) => {
                if (!value || !value.trim()) {
                    return 'Vui lòng nhập ghi chú.';
                }
                return null;
            },
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: isResolve ? '#10b981' : '#f59e0b',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: isResolve ? 'Chốt đối soát' : 'Mở lại',
            cancelButtonText: 'Hủy'
        }).then(r => {
            if (r.isConfirmed) {
                form.querySelector('[name="resolutionNote"]').value = (r.value || '').trim();
                form.submit();
            }
        });
        return false;
    }
</script>
</body>
</html>
