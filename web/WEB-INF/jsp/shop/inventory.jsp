<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tồn kho | Kênh Người Bán</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Tailwind & SweetAlert -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:         '#4d661c',
                        'primary-hover': '#364e03',
                        'primary-lt':    '#f0f7e6',
                        border:          '#e2ece7',
                        'txt':           '#0f172a',
                        'txt-2':         '#475569',
                        'txt-3':         '#94a3b8',
                    },
                    fontFamily: { sans: ['Lexend', 'sans-serif'] }
                }
            }
        }
    </script>

    <style>
        body { background-color: #f4fbf7; font-family: 'Lexend', sans-serif; }
        .glass-card {
            background: #ffffff;
            border: 1px solid #e2ece7;
            box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 4px 16px -4px rgba(20,83,45,.06);
        }
        /* Custom scrollbar for table container */
        .scrollbar-thin::-webkit-scrollbar { width: 4px; }
        .scrollbar-thin::-webkit-scrollbar-track { background: #f1f5f9; }
        .scrollbar-thin::-webkit-scrollbar-thumb { background: #bbf7d0; border-radius: 2px; }
    </style>
</head>
<body class="antialiased text-[#0f172a]">
<div class="flex min-h-screen">

    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="inventory"/>
    </jsp:include>

    <!-- Main Content -->
    <main class="flex-1 p-6 md:p-8 overflow-y-auto">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Nhập hàng &amp; Tồn kho</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Nhập thêm hàng vào kho, xem lịch sử và số lượng biến thể hiện tại.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-white/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-warehouse text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Tồn kho</span>
            </div>
        </div>

        <!-- Flash Message -->
        <c:if test="${not empty sessionScope.flashMsg}">
            <div id="flash-alert" class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 shadow-sm text-sm font-semibold
                 ${sessionScope.flashType == 'error' ? 'bg-red-50 border-red-500 text-red-800' : 'bg-emerald-50 border-emerald-500 text-emerald-800'}">
                <i class="fa-solid ${sessionScope.flashType == 'error' ? 'fa-circle-exclamation' : 'fa-circle-check'}"></i>
                <span class="flex-1"><c:out value="${sessionScope.flashMsg}"/></span>
                <button onclick="document.getElementById('flash-alert').remove()" class="opacity-60 hover:opacity-100 transition-opacity">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
            <c:remove var="flashMsg" scope="session"/>
            <c:remove var="flashType" scope="session"/>
        </c:if>

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
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="variantId">
                                    Sản phẩm &amp; Biến thể <span class="text-red-500">*</span>
                                </label>
                                <select name="variantId" id="variantId" required
                                        class="w-full px-4 py-2.5 border border-border rounded-xl text-sm bg-white focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                    <option value="" disabled selected>-- Chọn biến thể sản phẩm --</option>
                                    <c:forEach var="v" items="${variants}">
                                        <option value="${v.variantId}">
                                            ${v.productName} - ${v.variantLabel} (Hiện tại: ${v.stockQuantity})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="quantity">
                                    Số lượng nhập thêm <span class="text-red-500">*</span>
                                </label>
                                <input type="number" name="quantity" id="quantity" min="1" required
                                       placeholder="Ví dụ: 10, 50, 100"
                                       class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            </div>

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="changedAt">Ngày nhập kho <span class="text-red-500">*</span></label>
                                <input type="date" name="changedAt" id="changedAt" class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all" required>
                            </div>

                            <div class="mb-4">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="note">Ghi chú</label>
                                <input type="text" name="note" id="note" class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all" placeholder="Ghi chú nhập kho (ví dụ: Nhập hàng từ nhà cung cấp A)">
                            </div>

                            <button type="submit" class="w-full mt-6 py-3 px-4 bg-gradient-to-r from-primary to-[#5b7a22] hover:from-[#435919] hover:to-primary-hover text-white font-bold text-sm rounded-xl shadow-md hover:shadow-lg shadow-primary/20 hover:shadow-primary/30 transform hover:-translate-y-0.5 active:translate-y-0 transition-all duration-150 flex items-center justify-center gap-2 cursor-pointer border-0">
                                <i class="fa-solid fa-circle-arrow-down text-base"></i>
                                <span>Nhập kho sản phẩm</span>
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Right Columns (Tables Column) -->
            <div class="lg:col-span-3 flex flex-col gap-6">
                    
                    <!-- Stock Levels Card -->
                    <div class="bg-white border border-border rounded-2xl shadow-sm overflow-hidden h-fit">
                        <div class="p-5 border-b border-border bg-[#f9fdf9]">
                            <h2 class="text-sm font-bold text-txt"><i class="fa-solid fa-boxes-stacked mr-2"></i>Số lượng tồn kho hiện tại</h2>
                        </div>
                        <div class="p-0">
                            <div class="w-full overflow-auto max-h-[260px] scrollbar-thin">
                                <table class="w-full border-collapse text-left">
                                    <thead class="bg-[#f8fcf9] text-xs font-bold uppercase tracking-wider text-txt-2 sticky top-0 z-10 border-b border-border">
                                        <tr>
                                            <th class="px-5 py-3.5">Sản phẩm & Biến thể</th>
                                            <th class="px-5 py-3.5">SKU</th>
                                            <th class="px-5 py-3.5">Tồn kho hiện tại</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="v" items="${variants}">
                                            <tr class="hover:bg-primary/5 transition-colors">
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <strong class="text-txt font-bold">${v.productName}</strong>
                                                    <div class="text-[#94a3b8] text-xs">${v.variantLabel}</div>
                                                </td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm"><code>${v.sku}</code></td>
                                                <td class="px-5 py-3.5 border-b border-border text-sm">
                                                    <c:choose>
                                                        <c:when test="${v.stockQuantity <= 0}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-red-50 text-red-700 border border-red-200 shadow-sm">
                                                                <i class="fa-solid fa-circle-xmark mr-1 text-[10px]"></i> Hết hàng (0)
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${v.stockQuantity < 10}">
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200 shadow-sm">
                                                                <i class="fa-solid fa-triangle-exclamation mr-1 text-[10px]"></i> Sắp hết (${v.stockQuantity})
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 shadow-sm">
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

                    <!-- History Column -->
                    <div class="bg-white border border-border rounded-2xl shadow-sm overflow-hidden h-fit">
                        <div class="p-5 border-b border-border bg-[#f9fdf9]">
                            <h2 class="text-sm font-bold text-txt"><i class="fa-solid fa-clock-rotate-left mr-2"></i>Lịch sử biến động kho</h2>
                        </div>
                        <div class="p-0">
                            <div class="w-full overflow-auto max-h-[260px] scrollbar-thin">
                                <table class="w-full border-collapse text-left">
                                    <thead class="bg-[#f8fcf9] text-xs font-bold uppercase tracking-wider text-txt-2 sticky top-0 z-10 border-b border-border">
                                        <tr>
                                            <th class="px-5 py-3.5">Mã</th>
                                            <th class="px-5 py-3.5">Sản phẩm & Biến thể</th>
                                            <th class="px-5 py-3.5">Thay đổi</th>
                                            <th class="px-5 py-3.5">Loại</th>
                                            <th class="px-5 py-3.5">Ghi chú</th>
                                            <th class="px-5 py-3.5">Thời gian</th>
                                            <th class="px-5 py-3.5">Người thực hiện</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${restockLogs}">
                                            <tr class="hover:bg-primary/5 transition-colors">
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
                                                    <span class="px-2 py-0.5 rounded text-[11px] font-semibold bg-gray-100 text-gray-700 border border-gray-200">${log.changeType}</span>
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
        // Set dynamic csrfToken for JS context
        window.csrfToken = '${sessionScope._csrfToken}';

        document.addEventListener('DOMContentLoaded', function() {
            // Set max restock date to today and default to today
            const changedAtInput = document.getElementById('changedAt');
            if (changedAtInput) {
                const todayStr = new Date().toISOString().split('T')[0];
                changedAtInput.setAttribute('max', todayStr);
                changedAtInput.value = todayStr;
            }

            // Intercept form submit to ensure CSRF token is correctly attached
            const form = document.getElementById('restockForm');
            if (form) {
                form.addEventListener('submit', function(e) {
                    const csrfInput = this.querySelector('input[name="_csrf"]');
                    if (csrfInput && (!csrfInput.value || csrfInput.value.trim() === '')) {
                        csrfInput.value = window.csrfToken || '';
                    }
                });
            }
        });
    </script>
</body>
</html>
