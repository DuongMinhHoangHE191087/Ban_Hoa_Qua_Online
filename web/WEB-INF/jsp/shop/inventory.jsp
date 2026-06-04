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
        /* Sticky scrollable table body */
        .scroll-table-wrapper { max-height: 260px; overflow-y: auto; }
        .scroll-table-wrapper thead th { position: sticky; top: 0; z-index: 5; }
        /* Custom scrollbar */
        .scroll-table-wrapper::-webkit-scrollbar { width: 4px; }
        .scroll-table-wrapper::-webkit-scrollbar-track { background: #f1f5f9; }
        .scroll-table-wrapper::-webkit-scrollbar-thumb { background: #bbf7d0; border-radius: 2px; }
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
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="replenishmentDate">
                                    Ngày nhập kho <span class="text-red-500">*</span>
                                </label>
                                <input type="date" name="replenishmentDate" id="replenishmentDate" required
                                       class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            </div>

                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2" for="supplierDetails">
                                    Nhà cung cấp <span class="text-txt-3 font-normal">(tùy chọn)</span>
                                </label>
                                <input type="text" name="supplierDetails" id="supplierDetails"
                                       placeholder="Tên nhà cung cấp..."
                                       class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            </div>

                            <button type="submit"
                                    class="w-full py-3 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-xl transition-all duration-200 shadow-sm flex items-center justify-center gap-2">
                                <i class="fa-solid fa-circle-arrow-down"></i>
                                Nhập kho sản phẩm
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Right: Stock Table + History (3 cols) -->
            <div class="lg:col-span-3 flex flex-col gap-6">

                <!-- Current Stock Levels -->
                <div class="glass-card rounded-2xl overflow-hidden">
                    <div class="flex items-center gap-3 p-5 border-b border-[#e2ece7] bg-[#f9fdf9]">
                        <div class="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
                            <i class="fa-solid fa-boxes-stacked"></i>
                        </div>
                        <h2 class="text-sm font-bold text-txt">Số lượng tồn kho hiện tại</h2>
                    </div>
                    <div class="scroll-table-wrapper">
                        <table class="w-full text-left border-collapse text-sm">
                            <thead>
                                <tr class="bg-[#f0faf3] border-b border-[#e2ece7] text-xs font-bold text-[#4d661c] uppercase tracking-wider">
                                    <th class="py-3 px-4">Sản phẩm &amp; Biến thể</th>
                                    <th class="py-3 px-4">SKU</th>
                                    <th class="py-3 px-4">Tồn kho</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-[#f0f4f0]">
                                <c:forEach var="v" items="${variants}">
                                    <tr class="hover:bg-[#f9fdf9] transition-colors">
                                        <td class="py-3 px-4">
                                            <div class="font-semibold text-txt text-xs">${v.productName}</div>
                                            <div class="text-[10px] text-txt-3 mt-0.5">${v.variantLabel}</div>
                                        </td>
                                        <td class="py-3 px-4">
                                            <code class="text-[11px] bg-gray-100 text-gray-600 px-2 py-0.5 rounded font-mono">${v.sku}</code>
                                        </td>
                                        <td class="py-3 px-4">
                                            <c:choose>
                                                <c:when test="${v.stockQuantity <= 0}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-red-50 text-red-700 border border-red-200 animate-pulse">
                                                        <i class="fa-solid fa-triangle-exclamation text-[8px]"></i>Hết hàng
                                                    </span>
                                                </c:when>
                                                <c:when test="${v.stockQuantity < 10}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200">
                                                        <i class="fa-solid fa-exclamation text-[8px]"></i>Còn ít (${v.stockQuantity})
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                                                        <i class="fa-solid fa-check text-[8px]"></i>Còn hàng (${v.stockQuantity})
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty variants}">
                                    <tr>
                                        <td colspan="3" class="py-10 text-center text-txt-3 text-xs">
                                            <i class="fa-solid fa-box-open text-3xl mb-2 text-gray-200 block"></i>
                                            Chưa có sản phẩm nào trong kho!
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Replenishment History -->
                <div class="glass-card rounded-2xl overflow-hidden">
                    <div class="flex items-center gap-3 p-5 border-b border-[#e2ece7] bg-[#f9fdf9]">
                        <div class="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                            <i class="fa-solid fa-clock-rotate-left"></i>
                        </div>
                        <h2 class="text-sm font-bold text-txt">Lịch sử nhập kho</h2>
                    </div>
                    <div class="scroll-table-wrapper">
                        <table class="w-full text-left border-collapse text-sm">
                            <thead>
                                <tr class="bg-[#f0faf3] border-b border-[#e2ece7] text-xs font-bold text-[#4d661c] uppercase tracking-wider">
                                    <th class="py-3 px-4">Mã Phiếu</th>
                                    <th class="py-3 px-4">Sản phẩm</th>
                                    <th class="py-3 px-4">SL Nhập</th>
                                    <th class="py-3 px-4">Nhà cung cấp</th>
                                    <th class="py-3 px-4">Ngày nhập</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-[#f0f4f0]">
                                <c:forEach var="log" items="${replenishmentLogs}">
                                    <tr class="hover:bg-[#f9fdf9] transition-colors">
                                        <td class="py-3 px-4 text-xs font-bold text-txt-2">#${log.logId}</td>
                                        <td class="py-3 px-4">
                                            <div class="font-semibold text-txt text-xs">${log.productName}</div>
                                            <div class="text-[10px] text-txt-3 mt-0.5">${log.variantLabel}</div>
                                        </td>
                                        <td class="py-3 px-4">
                                            <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                                                <i class="fa-solid fa-arrow-up text-[8px]"></i>+${log.quantity}
                                            </span>
                                        </td>
                                        <td class="py-3 px-4 text-xs text-txt-2">
                                            <c:choose>
                                                <c:when test="${not empty log.supplierDetails}"><c:out value="${log.supplierDetails}"/></c:when>
                                                <c:otherwise><span class="text-txt-3 italic">Không có</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="py-3 px-4 text-xs text-txt-3">${log.replenishmentDate}</td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty replenishmentLogs}">
                                    <tr>
                                        <td colspan="5" class="py-10 text-center text-txt-3 text-xs">
                                            <i class="fa-solid fa-history text-3xl mb-2 text-gray-200 block"></i>
                                            Chưa có lịch sử nhập kho nào!
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </main>
</div>

<script>
    // Set default replenishment date to today
    document.getElementById('replenishmentDate').valueAsDate = new Date();
</script>
</body>
</html>
