<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tồn kho | MetaFruit</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

</head>
<body class="antialiased text-txt bg-background">
<div class="flex min-h-screen">

    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="inventory"/>
    </jsp:include>

    <!-- Main Content -->
    <main class="flex-1 p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Nhập hàng &amp; Tồn kho</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Nhập thêm hàng vào kho, xem lịch sử và số lượng phân loại hiện tại.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-surface/80 border border-primary-fixed/80 px-4 py-2 rounded-xl text-primary-dark shadow-sm">
                <i class="fa-solid fa-warehouse text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Tồn kho</span>
            </div>
        </div>



        <!-- Database Schema Error -->
        <c:if test="${not empty inventoryError}">
            <div class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 border-amber-500 bg-amber-50 text-amber-800 shadow-sm text-sm font-semibold">
                <i class="fa-solid fa-triangle-exclamation text-amber-600"></i>
                <span class="flex-1"><c:out value="${inventoryError}"/></span>
            </div>
        </c:if>

        <!-- Main Grid: Form + Tables -->
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6">

            <!-- Left: Restock Form (2 cols) -->
            <div class="lg:col-span-2">
                <div class="glass-card rounded-2xl overflow-hidden">
                    <div class="flex items-center gap-3 p-5 border-b border-[#e2ece7] bg-[#f9fdf9]">
                        <div class="w-9 h-9 rounded-xl bg-[#edf7f2] text-primary flex items-center justify-center">
                            <i class="fa-solid fa-plus"></i>
                        </div>
                        <h2 class="text-sm font-bold text-txt">Nhập kho sản phẩm</h2>
                    </div>
                    <div class="p-5">
                        <form action="${pageContext.request.contextPath}/shop/inventory" method="POST" id="restockForm">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="actionType">
                                    Loại tác vụ <span class="text-red-500">*</span>
                                </label>
                                <select name="actionType" id="actionType" required
                                        class="w-full px-4 py-2.5 border border-border rounded-xl text-sm bg-white focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                    <option value="RESTOCK" selected>Tăng kho (Nhập hàng)</option>
                                    <option value="REDUCE">Giảm kho (Hao hụt/Hỏng/Điều chỉnh)</option>
                                </select>
                            </div>

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="variantId">
                                    Sản phẩm &amp; Phân loại <span class="text-red-500">*</span>
                                </label>
                                <select name="variantId" id="variantId" required
                                        class="w-full px-4 py-2.5 border border-border rounded-xl text-sm bg-white focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                    <option value="" disabled selected>-- Chọn phân loại sản phẩm --</option>
                                    <c:forEach var="v" items="${variants}">
                                        <option value="${v.variantId}">
                                            ${v.productName} - ${v.variantLabel} (Hiện tại: ${v.stockQuantity})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="quantity">
                                    Số lượng <span class="text-red-500">*</span>
                                </label>
                                <input type="number" name="quantity" id="quantity" min="1" required
                                       placeholder="Ví dụ: 10, 50, 100"
                                       class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            </div>

                            <div class="mb-4" id="expiryGroup">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="expiresAt">
                                    Ngày hết hạn <span class="text-txt-3 font-normal">(Tùy chọn)</span>
                                </label>
                                <input type="date" name="expiresAt" id="expiresAt"
                                       class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            </div>

                            <%-- Ngày nhập kho ẩn, tự động lấy ngày hôm nay qua JS --%>
                            <input type="hidden" name="changedAt" id="changedAt">

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" id="noteLabel" for="note">Ghi chú</label>
                                <input type="text" name="note" id="note" class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all" placeholder="Ghi chú (ví dụ: Nhập hàng từ nhà cung cấp A)">
                            </div>

                            <button type="submit" class="w-full mt-6 py-3 px-4 bg-gradient-to-r from-primary to-[#5b7a22] hover:from-[#435919] hover:to-primary-hover text-white font-bold text-sm rounded-xl shadow-md hover:shadow-lg shadow-primary/20 hover:shadow-primary/30 transform hover:-translate-y-0.5 active:translate-y-0 transition-all duration-150 flex items-center justify-center gap-2 cursor-pointer border-0">
                                <i id="submitBtnIcon" class="fa-solid fa-circle-arrow-down text-base"></i>
                                <span id="submitBtnText">Nhập kho sản phẩm</span>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Right Columns (Tables Column) -->
            <div class="lg:col-span-3 flex flex-col gap-6">
                    
                    <!-- Stock Levels Card -->
                    <div class="bg-white border border-border rounded-2xl shadow-sm overflow-hidden h-fit">
                        <div class="p-5 border-b border-border bg-[#f9fdf9] flex items-center justify-between gap-3">
                            <h2 class="text-sm font-bold text-txt whitespace-nowrap"><i class="fa-solid fa-boxes-stacked mr-2"></i>Số lượng tồn kho hiện tại</h2>
                            <div class="relative flex-1 max-w-[220px]">
                                <i class="fa-solid fa-magnifying-glass absolute left-3 top-1/2 -translate-y-1/2 text-txt-3 text-xs pointer-events-none"></i>
                                <input id="stockSearch" type="text" placeholder="Tìm sản phẩm, SKU..."
                                       class="w-full pl-8 pr-3 py-1.5 border border-border rounded-lg text-xs focus:outline-none focus:border-primary transition-all bg-white">
                            </div>
                        </div>
                        <div class="p-0">
                            <div class="w-full overflow-auto max-h-[260px] scrollbar-thin">
                                <table id="stockTable" class="w-full border-collapse text-left">
                                    <thead class="bg-[#f8fcf9] text-xs font-bold uppercase tracking-wider text-txt-2 sticky top-0 z-10 border-b border-border">
                                        <tr>
                                            <th class="px-5 py-3.5">Sản phẩm &amp; Phân loại</th>
                                            <th class="px-5 py-3.5">SKU</th>
                                            <th class="px-5 py-3.5">Tồn kho hiện tại</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="v" items="${variants}">
                                            <tr data-row class="hover:bg-primary/5 transition-colors">
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <strong class="text-txt font-bold">${v.productName}</strong>
                                                    <div class="text-[#94a3b8] text-xs">${v.variantLabel}</div>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm"><code>${v.sku}</code></td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${v.stockQuantity <= 0}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-red-50 text-red-700 border border-red-200 shadow-sm whitespace-nowrap">
                                                                <i class="fa-solid fa-circle-xmark mr-1 text-[10px]"></i> Hết hàng (0)
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${v.stockQuantity < 10}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200 shadow-sm whitespace-nowrap">
                                                                <i class="fa-solid fa-triangle-exclamation mr-1 text-[10px]"></i> Sắp hết (${v.stockQuantity})
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 shadow-sm whitespace-nowrap">
                                                                <i class="fa-solid fa-circle-check mr-1 text-[10px]"></i> Còn hàng (${v.stockQuantity})
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty variants}">
                                            <tr>
                                                <td colspan="3" class="text-center py-8 text-[#94a3b8] italic">
                                                    Chưa có sản phẩm nào!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- ── Lô hàng & Ngày hết hạn ─────────────────────────────── -->
                    <div class="bg-white border border-border rounded-2xl shadow-sm overflow-hidden h-fit">
                        <div class="p-5 border-b border-border bg-[#f9fdf9] flex items-center justify-between gap-3">
                            <h2 class="text-sm font-bold text-txt whitespace-nowrap">
                                <i class="fa-solid fa-calendar-days mr-2 text-amber-500"></i>Lô hàng & Ngày hết hạn
                            </h2>
                            <span class="text-xs text-txt-3">Chỉ hiển thị lô còn hiệu lực (chưa hết hạn / chưa xử lý)</span>
                        </div>
                        <div class="p-0">
                            <div class="w-full overflow-auto max-h-[260px] scrollbar-thin">
                                <table id="batchTable" class="w-full border-collapse text-left">
                                    <thead class="bg-[#f8fcf9] text-xs font-bold uppercase tracking-wider text-txt-2 sticky top-0 z-10 border-b border-border">
                                        <tr>
                                            <th class="px-5 py-3.5">Lô #</th>
                                            <th class="px-5 py-3.5">Sản phẩm & Phân loại</th>
                                            <th class="px-5 py-3.5">SL nhập</th>
                                            <th class="px-5 py-3.5">Ngày nhập</th>
                                            <th class="px-5 py-3.5">Ngày hết hạn</th>
                                            <th class="px-5 py-3.5">Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="batch" items="${activeBatches}">
                                            <tr data-row class="hover:bg-primary/5 transition-colors">
                                                <td class="px-5 py-3.5 border-b border-border text-sm font-mono text-txt-2">#${batch.logId}</td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <strong class="text-txt font-bold">${batch.productName}</strong>
                                                    <div class="text-[#94a3b8] text-xs">${batch.variantLabel}</div>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm font-bold text-emerald-700">+${batch.quantityDelta}</td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm text-txt-2">${batch.formattedChangedAt}</td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${empty batch.expiresAt}">
                                                            <span class="text-txt-3 italic">Không có</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="font-semibold">${batch.formattedExpiresAt}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${batch.expiringSoon}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200 whitespace-nowrap">
                                                                <i class="fa-solid fa-triangle-exclamation mr-1 text-[10px]"></i>Sắp hết hạn
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${not empty batch.expiresAt}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 whitespace-nowrap">
                                                                <i class="fa-solid fa-circle-check mr-1 text-[10px]"></i>Còn hạn
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-slate-50 text-slate-500 border border-slate-200 whitespace-nowrap">
                                                                <i class="fa-solid fa-minus mr-1 text-[10px]"></i>Không rõ
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty activeBatches}">
                                            <tr>
                                                <td colspan="6" class="text-center py-8 text-[#94a3b8] italic">
                                                    Chưa có lô hàng nào còn hiệu lực!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- History Column -->
                    <div class="bg-white border border-border rounded-2xl shadow-sm overflow-hidden h-fit">
                        <div class="p-5 border-b border-border bg-[#f9fdf9] flex items-center justify-between gap-3">
                            <h2 class="text-sm font-bold text-txt whitespace-nowrap"><i class="fa-solid fa-clock-rotate-left mr-2"></i>Lịch sử biến động kho</h2>
                            <div class="relative flex-1 max-w-[220px]">
                                <i class="fa-solid fa-magnifying-glass absolute left-3 top-1/2 -translate-y-1/2 text-txt-3 text-xs pointer-events-none"></i>
                                <input id="historySearch" type="text" placeholder="Tìm sản phẩm, loại, ghi chú..."
                                       class="w-full pl-8 pr-3 py-1.5 border border-border rounded-lg text-xs focus:outline-none focus:border-primary transition-all bg-white">
                            </div>
                        </div>
                        <div class="p-0">
                            <div class="w-full overflow-auto max-h-[260px] scrollbar-thin">
                                <table id="historyTable" class="w-full border-collapse text-left">
                                    <thead class="bg-[#f8fcf9] text-xs font-bold uppercase tracking-wider text-txt-2 sticky top-0 z-10 border-b border-border">
                                        <tr>
                                            <th class="px-5 py-3.5">Mã</th>
                                            <th class="px-5 py-3.5">Sản phẩm &amp; Phân loại</th>
                                            <th class="px-5 py-3.5">Thay đổi</th>
                                            <th class="px-5 py-3.5">Loại</th>
                                            <th class="px-5 py-3.5">Ghi chú</th>
                                            <th class="px-5 py-3.5">Thời gian</th>
                                            <th class="px-5 py-3.5">Người thực hiện</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${restockLogs}">
                                            <tr data-row class="hover:bg-primary/5 transition-colors">
                                                <td class="px-5 py-3.5 border-b border-border text-sm">#${log.logId}</td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <strong class="text-txt font-bold">${log.productName}</strong>
                                                    <div class="text-[#94a3b8] text-xs">${log.variantLabel}</div>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <span class="font-bold px-2 py-0.5 rounded text-xs ${log.quantityDelta >= 0 ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'}">
                                                        ${log.quantityDelta >= 0 ? '+' : ''}${log.quantityDelta}
                                                    </span>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${log.changeType == 'ORDER_RESERVE'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-blue-50 text-blue-700 border border-blue-200 whitespace-nowrap">📦 Giữ hàng</span>
                                                        </c:when>
                                                        <c:when test="${log.changeType == 'ORDER_CONFIRM'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-violet-50 text-violet-700 border border-violet-200 whitespace-nowrap">✅ Đã bán</span>
                                                        </c:when>
                                                        <c:when test="${log.changeType == 'ORDER_RELEASE'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 whitespace-nowrap">↩ Hoàn kho</span>
                                                        </c:when>
                                                        <c:when test="${log.changeType == 'EXPIRED'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-orange-50 text-orange-700 border border-orange-200 whitespace-nowrap">⏰ Hết hạn</span>
                                                        </c:when>
                                                        <c:when test="${log.changeType == 'SPOILED'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-red-50 text-red-700 border border-red-200 whitespace-nowrap">🗑 Thối hỏng</span>
                                                        </c:when>
                                                        <c:when test="${log.changeType == 'RETURN'}">
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-yellow-50 text-yellow-700 border border-yellow-200 whitespace-nowrap">↩ Trả hàng</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-gray-100 text-gray-700 border border-gray-200 whitespace-nowrap">⚙ Điều chỉnh</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${not empty log.note}">
                                                            <span class="text-txt">${log.note}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-[#94a3b8]">-</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">${log.formattedChangedAt}</td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">${log.changedByName}</td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty restockLogs}">
                                            <tr>
                                                <td colspan="7" class="text-center py-8 text-[#94a3b8] italic">
                                                    Chưa có lịch sử biến động kho nào!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

            </div>
        </div>

    <script>
        window.csrfToken = '${sessionScope._csrfToken}';

        document.addEventListener('DOMContentLoaded', function() {
            const todayStr = new Date().toISOString().split('T')[0];
            
            // 1. Tự động set ngày nhập kho = hôm nay (hidden input)
            const changedAtInput = document.getElementById('changedAt');
            if (changedAtInput) {
                changedAtInput.value = todayStr;
            }

            // 2. Dynamic button text và layout theo loại tác vụ
            const actionTypeSelect = document.getElementById('actionType');
            const submitBtnText  = document.getElementById('submitBtnText');
            const submitBtnIcon  = document.getElementById('submitBtnIcon');
            const expiryGroup    = document.getElementById('expiryGroup');
            const expiresInput   = document.getElementById('expiresAt');
            const noteInput      = document.getElementById('note');
            const noteLabel      = document.getElementById('noteLabel');

            if (expiresInput) {
                expiresInput.min = todayStr;
            }

            function updateFormLayout() {
                if (!actionTypeSelect || !submitBtnText || !submitBtnIcon) return;
                const v = actionTypeSelect.value;
                if (v === 'RESTOCK') {
                    submitBtnText.textContent = 'Nhập kho sản phẩm';
                    submitBtnIcon.className   = 'fa-solid fa-circle-arrow-down text-base';
                    if (expiryGroup) expiryGroup.style.display = 'block';
                    if (expiresInput) expiresInput.disabled = false;
                    if (noteInput) {
                        noteInput.placeholder = 'Ghi chú (ví dụ: Nhập hàng từ nhà cung cấp A)';
                        noteInput.required = false;
                    }
                    if (noteLabel) noteLabel.innerHTML = 'Ghi chú';
                } else {
                    submitBtnText.textContent = 'Giảm kho sản phẩm';
                    submitBtnIcon.className   = 'fa-solid fa-circle-minus text-base';
                    if (expiryGroup) expiryGroup.style.display = 'none';
                    if (expiresInput) {
                        expiresInput.disabled = true;
                        expiresInput.value = '';
                    }
                    if (noteInput) {
                        noteInput.placeholder = 'Nhập lý do giảm kho (thối hỏng, hết hạn, hao hụt...) *';
                        noteInput.required = true;
                    }
                    if (noteLabel) noteLabel.innerHTML = 'Ghi chú <span class="text-red-500">*</span>';
                }
            }

            if (actionTypeSelect) {
                actionTypeSelect.addEventListener('change', updateFormLayout);
                updateFormLayout();
            }

            // 3. CSRF attach on submit & validation
            const form = document.getElementById('restockForm');
            if (form) {
                form.addEventListener('submit', function(e) {
                    const csrfInput = this.querySelector('input[name="_csrf"]');
                    if (csrfInput && (!csrfInput.value || csrfInput.value.trim() === '')) {
                        csrfInput.value = window.csrfToken || '';
                    }
                    // Luôn cập nhật changedAt = ngày hôm nay ngay trước khi submit
                    if (changedAtInput) {
                        changedAtInput.value = todayStr;
                    }

                    // Chặn ngày hết hạn trong quá khứ khi nhập kho
                    if (actionTypeSelect && actionTypeSelect.value === 'RESTOCK' && expiresInput && expiresInput.value) {
                        if (expiresInput.value < todayStr) {
                            e.preventDefault();
                            if (window.Swal) {
                                Swal.fire({
                                    icon: 'error',
                                    title: 'Lỗi nhập liệu',
                                    text: 'Ngày hết hạn không được là ngày trong quá khứ.',
                                    confirmButtonColor: '#4d661c'
                                });
                            } else {
                                alert('Ngày hết hạn không được là ngày trong quá khứ.');
                            }
                            return false;
                        }
                    }
                });
            }

            // ── 4. Tìm kiếm bảng Tồn kho hiện tại ────────────────────────────
            const stockSearch = document.getElementById('stockSearch');
            if (stockSearch) {
                stockSearch.addEventListener('input', function() {
                    const q = this.value.trim().toLowerCase();
                    document.querySelectorAll('#stockTable tbody tr[data-row]').forEach(function(tr) {
                        tr.style.display = (!q || tr.textContent.toLowerCase().includes(q)) ? '' : 'none';
                    });
                });
            }

            // ── 5. Tìm kiếm bảng Lịch sử biến động kho ──────────────────────
            const historySearch = document.getElementById('historySearch');
            if (historySearch) {
                historySearch.addEventListener('input', function() {
                    const q = this.value.trim().toLowerCase();
                    document.querySelectorAll('#historyTable tbody tr[data-row]').forEach(function(tr) {
                        tr.style.display = (!q || tr.textContent.toLowerCase().includes(q)) ? '' : 'none';
                    });
                });
            }
        });
    </script>
</body>
</html>
