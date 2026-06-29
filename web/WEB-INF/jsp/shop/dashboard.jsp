<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>MetaFruit | Tổng Quan Cửa Hàng</title>

                <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

                <!-- Google Fonts & Icons -->
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link
                        href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

                <!-- Core Tailwind and SweetAlert -->
                <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
                <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

                <script>
                    tailwind.config = {
                        theme: {
                            extend: {
                                colors: {
                                    primary: '#4d661c',
                                    'primary-hover': '#364e03',
                                    'primary-dk': '#364e03',
                                    'primary-lt': '#f0f7e6',
                                    surface: '#ffffff',
                                    'surface-2': '#f8fafc',
                                    border: '#e2ece7',
                                    'txt': '#0f172a',
                                    'txt-2': '#475569',
                                    'txt-3': '#94a3b8',
                                },
                                fontFamily: {
                                    sans: ['Lexend', 'Segoe UI', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
                                },
                                boxShadow: {
                                    card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                                }
                            }
                        }
                    }
                </script>

            </head>

            <body class="antialiased text-[#0f172a]" data-user-id="${sessionScope.currentUser.userId}">
                <div class="flex min-h-screen">
                    <!-- Shared Sidebar -->
                    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
                        <jsp:param name="activePage" value="dashboard" />
                    </jsp:include>

                    <!-- Main Content Area -->
                    <main class="flex-grow p-6 md:p-8 overflow-y-auto">
                        <!-- Header Section -->
                        <div
                            class="flex items-center justify-between bg-gradient-to-r from-[#eef9f1] via-[#e2f5e8] to-[#d4f0dd] border border-[#bbf7d0]/80 p-6 rounded-3xl shadow-sm mb-8">
                            <div>
                                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Tổng Quan
                                    Vận Hành</h1>
                                <p class="text-[#475569] text-xs md:text-sm mt-1">Xem nhanh các chỉ số vận hành quan
                                    trọng và các thao tác quản lý nhanh.</p>
                            </div>
                            <div
                                class="hidden md:flex items-center gap-2 bg-[#ffffff]/90 border border-[#bbf7d0]/80 px-4 py-2.5 rounded-2xl text-[#364e03] shadow-sm">
                                <i class="fa-solid fa-leaf text-[#84cc16]"></i>
                                <span class="text-xs font-bold uppercase tracking-wider pulse-live pr-4">Vận hành
                                    Live</span>
                            </div>
                        </div>

                        <!-- Stats Grid Dashboard -->
                        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                            <!-- Realized Revenue Card -->
                            <div
                                class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#f0fbf6]">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-[#E8F5E9] text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-wallet"></i>
                                    </div>
                                    <div>
                                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Doanh thu
                                            thực tế</span>
                                        <h3 class="text-xl font-black text-txt mt-0.5">
                                            <ft:currency value="${revenue}" />
                                        </h3>
                                        <p class="text-[10px] text-emerald-600 font-semibold mt-0.5"><i
                                                class="fa-solid fa-circle-check"></i> Đã hoàn thành đối soát</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Estimated Revenue Card -->
                            <div
                                class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#fffbeb]">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-sack-dollar"></i>
                                    </div>
                                    <div>
                                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Doanh thu
                                            tạm tính</span>
                                        <h3 class="text-xl font-black text-txt mt-0.5">
                                            <ft:currency value="${estimatedRevenue}" />
                                        </h3>
                                        <p class="text-[10px] text-amber-600 font-semibold mt-0.5"><i
                                                class="fa-solid fa-clock"></i> Đơn hàng đang xử lý</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Orders Card -->
                            <div
                                class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-[#E3F2FD] text-blue-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-clipboard-list"></i>
                                    </div>
                                    <div>
                                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng đơn
                                            hàng</span>
                                        <h3 class="text-xl font-black text-txt mt-0.5">${orderCount} đơn</h3>
                                        <p class="text-[10px] text-blue-600 font-semibold mt-0.5"><i
                                                class="fa-solid fa-chart-line"></i> Chỉ số tích lũy</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Low Stock Alert Card -->
                            <div
                                class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#fef2f2]">
                                <div class="flex items-center gap-4">
                                    <div
                                        class="w-12 h-12 rounded-2xl ${lowStock > 0 ? 'bg-red-50 text-red-600 animate-pulse' : 'bg-gray-50 text-gray-500'} flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-triangle-exclamation"></i>
                                    </div>
                                    <div>
                                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Sắp hết
                                            hàng</span>
                                        <h3
                                            class="text-xl font-black ${lowStock > 0 ? 'text-red-600' : 'text-txt'} mt-0.5">
                                            ${lowStock} phân loại</h3>
                                        <p
                                            class="text-[10px] ${lowStock > 0 ? 'text-red-500 font-bold' : 'text-gray-400'} mt-0.5">
                                            <c:choose>
                                                <c:when test="${lowStock > 0}"><i
                                                        class="fa-solid fa-circle-exclamation"></i> Cần nhập kho gấp
                                                </c:when>
                                                <c:otherwise><i class="fa-solid fa-circle-check"></i> Kho hàng an toàn
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <c:if test="${lowStock > 0}">
                            <!-- Low Stock Alert Box — dismissible per-variant via localStorage -->
                            <div id="low-stock-alert-block"
                                class="bg-red-50/85 backdrop-blur-sm border border-red-200/60 p-6 rounded-2xl shadow-sm mb-8">
                                <div class="flex items-start gap-4">
                                    <div
                                        class="w-10 h-10 rounded-xl bg-red-100 text-red-600 flex items-center justify-center text-lg flex-shrink-0">
                                        <i class="fa-solid fa-triangle-exclamation"></i>
                                    </div>
                                    <div class="flex-grow">
                                        <div class="flex items-center justify-between">
                                            <h3 class="text-sm font-bold text-red-800">Cảnh báo tồn kho: Phát hiện phân
                                                loại sắp hết hàng!</h3>
                                            <button onclick="dismissAllLowStockAlerts()" title="Ẩn tất cả thông báo này"
                                                class="text-red-400 hover:text-red-700 transition-colors ml-4 flex-shrink-0 p-1.5 rounded-lg hover:bg-red-100">
                                                <i class="fa-solid fa-xmark text-base"></i>
                                            </button>
                                        </div>
                                        <p class="text-xs text-red-700/80 mt-1">Các mặt hàng dưới đây có số lượng tồn
                                            kho ≤ ngưỡng cảnh báo (<strong>${lowStockThreshold}</strong> đơn vị). Hãy
                                            nhập thêm hàng để tránh gián đoạn kinh doanh.
                                            <a href="${pageContext.request.contextPath}/shop/settings"
                                                class="underline font-bold ml-1 text-red-850">Đổi ngưỡng →</a>
                                        </p>
                                        <div
                                            class="mt-4 overflow-x-auto rounded-xl border border-red-200/40 bg-white/70">
                                            <table class="w-full text-left border-collapse text-xs">
                                                <thead>
                                                    <tr
                                                        class="bg-red-100/30 text-red-800 font-bold uppercase tracking-wider border-b border-red-200/30">
                                                        <th class="py-2.5 px-4">Sản phẩm</th>
                                                        <th class="py-2.5 px-4">Phân loại</th>
                                                        <th class="py-2.5 px-4">SKU</th>
                                                        <th class="py-2.5 px-4">Tồn kho</th>
                                                        <th class="py-2.5 px-4 text-right">Thao tác</th>
                                                    </tr>
                                                </thead>
                                                <tbody class="divide-y divide-red-100/40 text-red-900"
                                                    id="low-stock-table-body">
                                                    <c:forEach var="item" items="${lowStockVariants}">
                                                        <tr class="hover:bg-red-100/10"
                                                            data-variant-id="${item.variantId}"
                                                            id="ls-row-${item.variantId}">
                                                            <td class="py-2.5 px-4 font-semibold">${item.productName}
                                                            </td>
                                                            <td class="py-2.5 px-4">${item.variantLabel}</td>
                                                            <td class="py-2.5 px-4 font-mono">${item.sku}</td>
                                                            <td class="py-2.5 px-4">
                                                                <span
                                                                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                                                                    ${item.stockQuantity}
                                                                </span>
                                                            </td>
                                                            <td class="py-2.5 px-4 text-right">
                                                                <div class="flex items-center justify-end gap-2">
                                                                    <a href="${pageContext.request.contextPath}/shop/inventory"
                                                                        class="inline-flex items-center gap-1 font-bold text-red-700 hover:text-red-950 hover:underline">
                                                                        <i class="fa-solid fa-truck-ramp-box"></i> Nhập
                                                                        hàng
                                                                    </a>
                                                                    <button data-variant-id="${item.variantId}"
                                                                        title="Ẩn cảnh báo cho phân loại này"
                                                                        class="dismiss-variant-btn text-red-300 hover:text-red-600 transition-colors p-1 rounded hover:bg-red-100 ml-1">
                                                                        <i class="fa-solid fa-xmark text-xs"></i>
                                                                    </button>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <script>
                            var LS_KEY = 'dismissedLowStock_' + document.body.getAttribute('data-user-id');

                            function getDismissed() {
                                try { return JSON.parse(localStorage.getItem(LS_KEY) || '[]'); } catch (e) { return []; }
                            }
                            function saveDismissed(ids) { localStorage.setItem(LS_KEY, JSON.stringify(ids)); }

                            function dismissLowStockAlert(variantId) {
                                var row = document.getElementById('ls-row-' + variantId);
                                if (!row) return;
                                row.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                                row.style.opacity = '0';
                                row.style.transform = 'translateX(20px)';
                                setTimeout(function () {
                                    row.remove();
                                    var dismissed = getDismissed();
                                    if (dismissed.indexOf(variantId) === -1) dismissed.push(variantId);
                                    saveDismissed(dismissed);
                                    checkAllDismissed();
                                }, 300);
                            }

                            function dismissAllLowStockAlerts() {
                                var block = document.getElementById('low-stock-alert-block');
                                if (!block) return;
                                var rows = block.querySelectorAll('#low-stock-table-body tr[data-variant-id]');
                                var dismissed = getDismissed();
                                rows.forEach(function (r) {
                                    var vid = parseInt(r.getAttribute('data-variant-id'));
                                    if (dismissed.indexOf(vid) === -1) dismissed.push(vid);
                                });
                                saveDismissed(dismissed);
                                block.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                                block.style.opacity = '0';
                                block.style.transform = 'translateY(-10px)';
                                setTimeout(function () { block.remove(); }, 300);
                            }

                            function checkAllDismissed() {
                                var body = document.getElementById('low-stock-table-body');
                                if (body && body.querySelectorAll('tr').length === 0) dismissAllLowStockAlerts();
                            }

                            window.addEventListener('DOMContentLoaded', function () {
                                // Attach event listeners to dismiss variant buttons to avoid JSTL syntax errors in JS
                                document.querySelectorAll('.dismiss-variant-btn').forEach(function (btn) {
                                    btn.addEventListener('click', function () {
                                        var vid = parseInt(this.getAttribute('data-variant-id'));
                                        dismissLowStockAlert(vid);
                                    });
                                });

                                var dismissed = getDismissed();
                                if (dismissed.length === 0) return;
                                // Làm sạch dismiss list: chỉ giữ những variant vẫn có trên server
                                var currentRows = document.querySelectorAll('#low-stock-table-body tr[data-variant-id]');
                                var currentIds = Array.from(currentRows).map(function (r) { return parseInt(r.getAttribute('data-variant-id')); });
                                var stillActive = dismissed.filter(function (id) { return currentIds.indexOf(id) !== -1; });
                                saveDismissed(stillActive);
                                stillActive.forEach(function (id) {
                                    var row = document.getElementById('ls-row-' + id);
                                    if (row) row.remove();
                                });
                                checkAllDismissed();
                            });
                        </script>

                        <!-- Main Layout Split Grid -->
                        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <!-- Left Column (Recent Orders & Recent Products) -->
                            <div class="lg:col-span-2 space-y-6">
                                <!-- Recent Orders Table -->
                                <div class="bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm">
                                    <div class="flex items-center justify-between mb-5">
                                        <h2 class="text-base font-bold text-txt flex items-center gap-2">
                                            <i class="fa-solid fa-clock-rotate-left text-primary"></i> Đơn hàng gần đây
                                        </h2>
                                        <a href="${pageContext.request.contextPath}/shop/orders"
                                            class="text-xs text-primary hover:underline font-bold transition-all">Xem
                                            tất cả →</a>
                                    </div>
                                    <div class="overflow-x-auto">
                                        <table class="w-full text-left border-collapse">
                                            <thead>
                                                <tr
                                                    class="bg-gray-50/60 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
                                                    <th class="py-3 px-4">Mã ĐH</th>
                                                    <th class="py-3 px-4">Ngày tạo</th>
                                                    <th class="py-3 px-4">Tổng tiền</th>
                                                    <th class="py-3 px-4">Trạng thái</th>
                                                    <th class="py-3 px-4 text-right">Hành động</th>
                                                </tr>
                                            </thead>
                                            <tbody class="divide-y divide-gray-100 text-sm">
                                                <c:forEach var="order" items="${recentOrders}">
                                                    <tr class="hover:bg-gray-50/20 transition-all duration-150">
                                                        <td class="py-3 px-4 font-bold text-gray-700">#${order.orderId}
                                                        </td>
                                                        <td class="py-3 px-4 text-xs text-gray-500">${order.createdAt}
                                                        </td>
                                                        <td class="py-3 px-4 font-bold text-primary">
                                                            <ft:currency value="${order.finalAmount}" />
                                                        </td>
                                                        <td class="py-3 px-4">
                                                            <c:choose>
                                                                <c:when test="${order.status == 'PENDING_PAYMENT'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200">Chờ
                                                                        thanh toán</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'CONFIRMED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">Chờ
                                                                        duyệt</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'PREPARING'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-50 text-purple-700 border border-purple-200">Chuẩn
                                                                        bị</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'APPROVED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">Đã
                                                                        Duyệt</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'DISPATCHED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-pink-50 text-pink-700 border border-pink-200">Đang
                                                                        giao</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'DELIVERED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">Đã
                                                                        giao</span>
                                                                </c:when>
                                                                <c:when test="${order.status == 'CANCELLED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-50 text-red-700 border border-red-200">Đã
                                                                        hủy</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-50 text-gray-600 border border-gray-200">${order.status}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="py-3 px-4 text-right">
                                                            <a href="${pageContext.request.contextPath}/shop/orders?status=${order.status}"
                                                                class="text-xs font-bold text-primary hover:underline">Xử
                                                                lý</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty recentOrders}">
                                                    <tr>
                                                        <td colspan="5"
                                                            class="py-12 text-center text-txt-3 font-light text-xs">
                                                            <i
                                                                class="fa-solid fa-box-open text-3xl mb-3 text-gray-300 block"></i>
                                                            Chưa có đơn hàng nào cần hiển thị!
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                                <!-- Recently Added Products -->
                                <div class="bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm">
                                    <div class="flex items-center justify-between mb-5">
                                        <h2 class="text-base font-bold text-txt flex items-center gap-2">
                                            <i class="fa-solid fa-box text-primary"></i> Sản phẩm mới đăng gần đây
                                        </h2>
                                        <a href="${pageContext.request.contextPath}/shop/products"
                                            class="text-xs text-primary hover:underline font-bold transition-all">Quản
                                            lý sản phẩm →</a>
                                    </div>
                                    <div class="overflow-x-auto">
                                        <table class="w-full text-left border-collapse">
                                            <thead>
                                                <tr
                                                    class="bg-gray-50/60 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
                                                    <th class="py-3 px-4">Ảnh/Tên</th>
                                                    <th class="py-3 px-4 text-center">Quốc gia</th>
                                                    <th class="py-3 px-4 text-center">Đánh giá</th>
                                                    <th class="py-3 px-4">Duyệt</th>
                                                    <th class="py-3 px-4 text-right">Thao tác</th>
                                                </tr>
                                            </thead>
                                            <tbody class="divide-y divide-gray-100 text-sm">
                                                <c:forEach var="prod" items="${recentProducts}">
                                                    <tr class="hover:bg-gray-50/20 transition-all duration-150">
                                                        <td class="py-3 px-4">
                                                            <div class="flex items-center gap-3">
                                                                <div
                                                                    class="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center text-xs overflow-hidden flex-shrink-0 border border-gray-250/30">
                                                                    <c:choose>
                                                                        <c:when
                                                                            test="${not empty prod.verificationDocPath}">
                                                                            <img src="${pageContext.request.contextPath}/uploads/${prod.verificationDocPath}"
                                                                                class="w-full h-full object-cover">
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <i
                                                                                class="fa-solid fa-carrot text-amber-500 text-lg"></i>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </div>
                                                                <div>
                                                                    <span
                                                                        class="font-bold text-gray-800 block text-xs md:text-sm">
                                                                        <c:out value="${prod.name}" />
                                                                    </span>
                                                                    <span class="text-[10px] text-gray-400">
                                                                        <c:out value="${prod.originRegion}" />
                                                                    </span>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td
                                                            class="py-3 px-4 text-center text-xs text-gray-650 font-semibold">
                                                            <c:out value="${prod.originCountry}" />
                                                        </td>
                                                        <td class="py-3 px-4 text-center">
                                                            <span
                                                                class="inline-flex items-center text-xs text-amber-600 font-bold bg-amber-50 px-2 py-0.5 rounded-lg">
                                                                <i class="fa-solid fa-star text-[10px] mr-1"></i>
                                                                ${prod.rating}
                                                            </span>
                                                        </td>
                                                        <td class="py-3 px-4">
                                                            <c:choose>
                                                                <c:when test="${prod.approvalStatus == 'APPROVED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200"><i
                                                                            class="fa-solid fa-circle-check mr-1"></i>
                                                                        Đã duyệt</span>
                                                                </c:when>
                                                                <c:when test="${prod.approvalStatus == 'PENDING'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200"><i
                                                                            class="fa-solid fa-hourglass-start mr-1"></i>
                                                                        Chờ duyệt</span>
                                                                </c:when>
                                                                <c:when test="${prod.approvalStatus == 'REJECTED'}">
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-50 text-red-700 border border-red-200"
                                                                        title="${prod.rejectionReason}"><i
                                                                            class="fa-solid fa-circle-xmark mr-1"></i>
                                                                        Từ chối</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span
                                                                        class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-50 text-gray-600 border border-gray-200">${prod.approvalStatus}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="py-3 px-4 text-right">
                                                            <a href="${pageContext.request.contextPath}/shop/products?action=edit&id=${prod.productId}"
                                                                class="text-xs font-bold text-primary hover:underline">Sửa</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty recentProducts}">
                                                    <tr>
                                                        <td colspan="5"
                                                            class="py-12 text-center text-txt-3 font-light text-xs">
                                                            <i
                                                                class="fa-solid fa-carrot text-3xl mb-3 text-gray-300 block"></i>
                                                            Chưa có sản phẩm nào được đăng tải!
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>

                            <!-- Right Column (Unread Messages & Quick Actions) -->
                            <div class="space-y-6">
                                <!-- Customer Chat Messages Alert Card -->
                                <div
                                    class="bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm bg-gradient-to-br from-white to-[#f0faf5]">
                                    <h2 class="text-base font-bold text-txt flex items-center gap-2 mb-3">
                                        <i class="fa-solid fa-comments text-emerald-600"></i> Hỗ trợ khách hàng
                                    </h2>

                                    <c:choose>
                                        <c:when test="${unreadMessagesCount > 0}">
                                            <div
                                                class="bg-emerald-50 border border-emerald-200 rounded-xl p-4 mb-4 flex items-start gap-3">
                                                <div
                                                    class="w-8 h-8 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center flex-shrink-0 text-sm animate-bounce">
                                                    <i class="fa-solid fa-comment-dots"></i>
                                                </div>
                                                <div>
                                                    <h4 class="text-xs font-bold text-emerald-900">Tin nhắn mới cần trả
                                                        lời!</h4>
                                                    <p class="text-[10px] text-emerald-700/80 mt-0.5">Bạn có <strong
                                                            class="text-emerald-800 text-xs font-black">${unreadMessagesCount}</strong>
                                                        tin nhắn từ khách hàng chưa được phản hồi.</p>
                                                </div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div
                                                class="bg-gray-50 border border-gray-150 rounded-xl p-4 mb-4 flex items-start gap-3">
                                                <div
                                                    class="w-8 h-8 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center flex-shrink-0 text-sm">
                                                    <i class="fa-solid fa-comment-slash"></i>
                                                </div>
                                                <div>
                                                    <h4 class="text-xs font-bold text-gray-700">Đã trả lời hết tin nhắn
                                                    </h4>
                                                    <p class="text-[10px] text-gray-500 mt-0.5">Không có tin nhắn khách
                                                        hàng nào đang chờ xử lý.</p>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <a href="${pageContext.request.contextPath}/shop/chat"
                                        class="w-full inline-flex items-center justify-center gap-2 bg-primary hover:bg-[#364e03] text-white text-xs font-bold py-2.5 px-4 rounded-xl shadow-sm transition-all text-center">
                                        <i class="fa-solid fa-comment-dots"></i> Vào Kênh Trò Chuyện
                                    </a>
                                </div>

                                <!-- Quick Actions Widget -->
                                <div class="bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm">
                                    <h2 class="text-base font-bold text-txt flex items-center gap-2 mb-5">
                                        <i class="fa-solid fa-bolt text-amber-500"></i> Thao tác nhanh
                                    </h2>
                                    <div class="flex flex-col gap-3">
                                        <a href="${pageContext.request.contextPath}/shop/products"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-box"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Quản lý Sản phẩm</h4>
                                                <p class="text-[9px] text-gray-400">Thêm, sửa đổi hoặc ẩn sản phẩm</p>
                                            </div>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/shop/orders"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-file-invoice"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Quản lý Đơn hàng</h4>
                                                <p class="text-[9px] text-gray-400">Duyệt đơn và giao vận chuyển</p>
                                            </div>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/shop/inventory"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-warehouse"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Quản lý Nhập kho</h4>
                                                <p class="text-[9px] text-gray-400">Theo dõi & cập nhật lượng tồn kho
                                                </p>
                                            </div>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/shop/settlement"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-wallet"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Đối soát &amp; Doanh thu</h4>
                                                <p class="text-[9px] text-gray-400">Xem doanh thu &amp; lịch sử đối soát
                                                </p>
                                            </div>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/shop/reports"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-chart-column"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Báo cáo &amp; Thống kê</h4>
                                                <p class="text-[9px] text-gray-400">Xu hướng doanh số &amp; sản lượng
                                                    bán</p>
                                            </div>
                                        </a>

                                        <a href="${pageContext.request.contextPath}/home"
                                            class="group flex items-center gap-3.5 p-3 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                            <div
                                                class="w-9 h-9 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center text-sm transition-colors group-hover:bg-primary group-hover:text-white">
                                                <i class="fa-solid fa-house"></i>
                                            </div>
                                            <div class="flex-grow">
                                                <h4
                                                    class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">
                                                    Về Trang chủ</h4>
                                                <p class="text-[9px] text-gray-400">Xem gian hàng theo góc nhìn khách
                                                </p>
                                            </div>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </main>
                </div>
            </body>

            </html>
