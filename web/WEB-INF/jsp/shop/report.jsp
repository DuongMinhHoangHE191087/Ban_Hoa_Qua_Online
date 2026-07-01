<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo &amp; Thống kê | MetaFruit</title>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Local Fonts & Icons (offline-safe) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

    <!-- Core Tailwind and SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <!-- Chart.js Secure CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

</head>
<body class="antialiased text-txt bg-background">
    <div class="flex min-h-screen">
        <!-- Shared Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
            <jsp:param name="activePage" value="reports"/>
        </jsp:include>

        <!-- Main Content Area -->
        <main class="flex-grow p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">
            <!-- Header Section -->
            <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-3xl shadow-sm mb-8">
                <div>
                    <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight font-sans">Thống Kê &amp; Báo Cáo Doanh Thu</h1>
                    <p class="text-txt-2 text-xs md:text-sm mt-1">Phân tích chuyên sâu về doanh thu cửa hàng, tình trạng đơn hàng và hành vi mua sắm.</p>
                </div>
                <div class="flex items-center gap-2">
                    <button onclick="exportToCSV()" class="bg-primary hover:bg-primary-hover text-white text-xs font-bold px-4 py-2.5 rounded-xl shadow-sm transition-all flex items-center gap-2 cursor-pointer border-none">
                        <i class="fa-solid fa-file-csv text-sm"></i> Xuất dữ liệu (CSV)
                    </button>
                </div>
            </div>

            <%-- Date Range Filter Form --%>
            <form method="GET" action="${pageContext.request.contextPath}/shop/reports" class="glass-card p-5 mb-8 rounded-2xl">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
                    <div class="w-full">
                        <label class="block text-xs font-bold text-txt-2 uppercase mb-1.5">Từ ngày</label>
                        <div class="relative">
                            <input type="date" name="startDate" value="${startDate}" required
                                   class="w-full bg-surface-2 border border-border px-3 py-2 rounded-xl text-txt text-sm focus:outline-none focus:border-[#4d661c]">
                        </div>
                    </div>
                    <div class="w-full">
                        <label class="block text-xs font-bold text-txt-2 uppercase mb-1.5">Đến ngày</label>
                        <div class="relative">
                            <input type="date" name="endDate" value="${endDate}" required
                                   class="w-full bg-surface-2 border border-border px-3 py-2 rounded-xl text-txt text-sm focus:outline-none focus:border-[#4d661c]">
                        </div>
                    </div>
                    <div class="w-full">
                        <label class="block text-xs font-bold text-txt-2 uppercase mb-1.5">Danh mục</label>
                        <select name="categoryId" class="w-full bg-surface-2 border border-border px-3 py-2.5 rounded-xl text-txt text-sm focus:outline-none focus:border-[#4d661c]">
                            <option value="">Tất cả danh mục</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${selectedCategoryId == cat.categoryId ? 'selected' : ''}>
                                    <c:out value="${cat.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="flex gap-2 w-full lg:col-span-1">
                        <button type="submit" class="bg-[#4d661c] hover:bg-[#364e03] text-white text-sm font-bold px-4 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer flex-1 border-none text-center">
                            <i class="fa-solid fa-filter mr-1"></i> Lọc
                        </button>
                        <button type="button" onclick="setQuickDateRange(30)" class="bg-white border border-border hover:bg-surface-2 text-txt text-xs px-2 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer">
                            30d
                        </button>
                        <button type="button" onclick="setQuickDateRange(7)" class="bg-white border border-border hover:bg-surface-2 text-txt text-xs px-2 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer">
                            7d
                        </button>
                    </div>
                </div>
            </form>

            <%-- KPI Cards --%>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6 mb-8">
                <%-- Total Revenue --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#f0fbf6]">
                    <div class="w-12 h-12 rounded-2xl bg-[#E8F5E9] text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-money-bill-wave"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Doanh thu shop</span>
                        <h3 class="text-base font-black text-[#364e03] mt-0.5"><ft:currency value="${kpiTotalRevenue}" /></h3>
                    </div>
                </div>

                <%-- Estimated Revenue --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#fffbeb]">
                    <div class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-clock"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Doanh thu tạm tính</span>
                        <h3 class="text-base font-black text-txt mt-0.5"><ft:currency value="${kpiEstimatedRevenue}" /></h3>
                    </div>
                </div>

                <%-- Total Orders --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-cart-flatbed"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Tổng đơn hàng</span>
                        <h3 class="text-base font-black text-txt mt-0.5">${kpiTotalOrders} đơn</h3>
                    </div>
                </div>

                <%-- Success Orders --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-circle-check"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Đơn thành công</span>
                        <h3 class="text-base font-black text-txt mt-0.5">${kpiSuccessfulOrders} đơn</h3>
                    </div>
                </div>

                <%-- Cancellation Rate --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default bg-gradient-to-br from-white to-[#fef2f2]">
                    <div class="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Tỷ lệ hủy đơn</span>
                        <h3 class="text-base font-black text-txt mt-0.5">${kpiCancellationRate}%</h3>
                    </div>
                </div>

                <%-- Total Units Sold --%>
                <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="w-12 h-12 rounded-2xl bg-purple-50 text-purple-600 flex items-center justify-center text-xl shadow-inner">
                        <i class="fa-solid fa-basket-shopping"></i>
                    </div>
                    <div>
                        <span class="text-xs font-bold text-txt-3 uppercase tracking-wider block">Sản lượng bán</span>
                        <h3 class="text-base font-black text-txt mt-0.5">${kpiTotalUnitsSold} đv</h3>
                    </div>
                </div>
            </div>

            <%-- Charts Grid --%>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <%-- Revenue Trend Chart --%>
                <div class="glass-card p-6 flex flex-col justify-between rounded-2xl lg:col-span-2">
                    <div class="flex items-center justify-between mb-4 border-b border-border pb-3">
                        <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-chart-line text-primary mr-1.5"></i>Biểu đồ xu hướng doanh thu</h3>
                        <span class="text-xs text-txt-3">Hằng ngày</span>
                    </div>
                    <div class="relative h-[300px]">
                        <canvas id="revenueTrendChart"></canvas>
                    </div>
                </div>

                <%-- Order Status stats chart --%>
                <div class="glass-card p-6 flex flex-col justify-between rounded-2xl">
                    <div class="flex items-center justify-between mb-4 border-b border-border pb-3">
                        <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-chart-pie text-blue-500 mr-1.5"></i>Tỉ lệ đơn hàng theo trạng thái</h3>
                        <span class="text-xs text-txt-3">Tổng quan</span>
                    </div>
                    <div class="relative h-[300px] flex items-center justify-center">
                        <div class="w-full max-w-[280px]">
                            <canvas id="orderStatusChart"></canvas>
                        </div>
                    </div>
                </div>

                <%-- Cancellation Reasons Chart --%>
                <div class="glass-card p-6 flex flex-col justify-between rounded-2xl">
                    <div class="flex items-center justify-between mb-4 border-b border-border pb-3">
                        <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-circle-question text-red-500 mr-1.5"></i>Lý do hủy đơn phổ biến</h3>
                        <span class="text-xs text-txt-3">Số lượng</span>
                    </div>
                    <div class="relative h-[300px]">
                        <canvas id="cancellationReasonChart"></canvas>
                    </div>
                </div>
            </div>

            <%-- Fruit Usage Report Table --%>
            <div class="glass-card p-6 mb-8 rounded-2xl">
                <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-6 border-b border-border pb-4 gap-3">
                    <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-basket-shopping text-amber-500 mr-1.5"></i>Báo cáo bán hàng &amp; mức sử dụng trái cây cửa hàng</h3>
                    <div class="flex items-center gap-3">
                        <div class="relative">
                            <i class="fa-solid fa-search text-txt-3 absolute left-3 top-1/2 -translate-y-1/2 text-xs pointer-events-none"></i>
                            <input type="text" id="fruitSearch" placeholder="Tìm sản phẩm..."
                                   class="pl-8 pr-4 py-1.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary bg-surface-2 w-44 transition-all">
                        </div>
                        <span class="text-xs text-txt-3 font-semibold whitespace-nowrap" id="fruitRowCount"></span>
                    </div>
                </div>
                <div class="overflow-x-auto overflow-y-auto max-h-[520px]">
                    <table class="w-full border-collapse text-left" id="fruitUsageTable">
                        <thead class="sticky top-0 z-10">
                            <tr class="border-b border-border text-xs font-bold text-txt-3 uppercase tracking-wider bg-surface-2">
                                <th class="py-3 px-3">Tên Trái Cây / Sản Phẩm</th>
                                <th class="py-3 px-4">Phân loại</th>
                                <th class="py-3 px-4 text-center">Số lượng đã bán</th>
                                <th class="py-3 px-4 text-right">Tổng doanh số</th>
                                <th class="py-3 px-4 text-center">Số đơn hàng</th>
                            </tr>
                        </thead>
                        <tbody id="fruitTableBody" class="divide-y divide-border text-sm text-txt-2">
                            <c:forEach var="item" items="${fruitUsage}">
                                <tr data-fruit-row class="hover:bg-surface-2 transition-colors">
                                    <td class="py-3.5 px-3 font-bold text-txt"><c:out value="${item.productName}"/></td>
                                    <td class="py-3.5 px-4"><span class="bg-gray-100 text-gray-700 text-xs font-semibold px-2 py-1 rounded-md"><c:out value="${item.variantLabel}"/></span></td>
                                    <td class="py-3.5 px-4 text-center font-semibold text-txt">${item.totalQuantity}</td>
                                    <td class="py-3.5 px-4 text-right font-bold text-primary"><ft:currency value="${item.totalAmount}" /></td>
                                    <td class="py-3.5 px-4 text-center">${item.orderCount} đơn hàng</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty fruitUsage}">
                                <tr>
                                    <td colspan="5" class="py-8 text-center text-txt-3 italic">Không có dữ liệu bán hàng nào trong giai đoạn này.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- Hidden element holding JSON data to avoid JS parsing issues in IDE -->
    <div id="chart-data" class="hidden"
         data-revenue-trend='<c:out value="${empty revenueTrendJson ? '[]' : revenueTrendJson}" escapeXml="true" />'
         data-order-status='<c:out value="${empty orderStatusStatsJson ? '[]' : orderStatusStatsJson}" escapeXml="true" />'
         data-cancellation='<c:out value="${empty cancellationReasonStatsJson ? '[]' : cancellationReasonStatsJson}" escapeXml="true" />'>
    </div>

    <%-- Script vẽ biểu đồ và xuất báo cáo CSV --%>
    <script>
        // 1. Khôi phục dữ liệu từ JSP (đọc gián tiếp qua data attributes)
        const chartDataEl = document.getElementById('chart-data');
        const revenueData = JSON.parse(chartDataEl.getAttribute('data-revenue-trend') || '[]');
        const orderStatsData = JSON.parse(chartDataEl.getAttribute('data-order-status') || '[]');
        const cancellationData = JSON.parse(chartDataEl.getAttribute('data-cancellation') || '[]');

        // Helper: Định dạng tiền tệ VND
        function formatCurrency(val) {
            return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
        }

        // A. Biểu đồ xu hướng doanh thu
        const revCtx = document.getElementById('revenueTrendChart').getContext('2d');
        new Chart(revCtx, {
            type: 'line',
            data: {
                labels: revenueData.map(d => d.date),
                datasets: [{
                    label: 'Doanh thu (VND)',
                    data: revenueData.map(d => d.revenue),
                    borderColor: '#4d661c',
                    backgroundColor: 'rgba(77, 102, 28, 0.05)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#4d661c',
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return 'Doanh thu: ' + formatCurrency(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return value >= 1000000 ? (value / 1000000) + 'M' : formatCurrency(value);
                            }
                        }
                    }
                }
            }
        });

        // B. Biểu đồ tỉ lệ đơn hàng theo trạng thái
        const statusCtx = document.getElementById('orderStatusChart').getContext('2d');
        const statusLabelsVi = {
            'PENDING_PAYMENT': 'Chờ thanh toán',
            'APPROVED': 'Đã duyệt',
            'CONFIRMED': 'Chờ duyệt',
            'PREPARING': 'Chuẩn bị',
            'DISPATCHED': 'Đang giao',
            'DELIVERED': 'Thành công',
            'CANCELLED': 'Đã hủy',
            'PAYMENT_FAILED': 'Thất bại',
            'EXPIRED': 'Hết hạn'
        };
        new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: orderStatsData.map(d => statusLabelsVi[d.status] || d.status),
                datasets: [{
                    data: orderStatsData.map(d => d.count),
                    backgroundColor: ['#eab308', '#3b82f6', '#6366f1', '#a855f7', '#ec4899', '#22c55e', '#ef4444', '#64748b', '#94a3b8']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, font: { size: 11 } }
                    }
                }
            }
        });

        // C. Biểu đồ lý do hủy đơn
        const cancelCtx = document.getElementById('cancellationReasonChart').getContext('2d');
        new Chart(cancelCtx, {
            type: 'bar',
            data: {
                labels: cancellationData.map(d => d.reason.length > 20 ? d.reason.substring(0, 18) + '...' : d.reason),
                datasets: [{
                    label: 'Số đơn hủy',
                    data: cancellationData.map(d => d.count),
                    backgroundColor: '#f87171',
                    borderRadius: 4
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: { beginAtZero: true, ticks: { precision: 0 } }
                }
            }
        });

        // Nút chọn nhanh khoảng thời gian
        function setQuickDateRange(days) {
            const end = new Date();
            const start = new Date();
            start.setDate(end.getDate() - days);

            const formatDate = (date) => {
                let month = '' + (date.getMonth() + 1);
                let day = '' + date.getDate();
                let year = date.getFullYear();
                if (month.length < 2) month = '0' + month;
                if (day.length < 2) day = '0' + day;
                return [year, month, day].join('-');
            };

            document.getElementsByName('startDate')[0].value = formatDate(start);
            document.getElementsByName('endDate')[0].value = formatDate(end);
            document.forms[0].submit();
        }

        // Fruit table search filter
        (function() {
            const searchEl = document.getElementById('fruitSearch');
            const countEl  = document.getElementById('fruitRowCount');
            const tbody    = document.getElementById('fruitTableBody');
            if (!searchEl || !tbody) return;
            const allRows = Array.from(tbody.querySelectorAll('tr[data-fruit-row]'));
            countEl.textContent = allRows.length + ' sản phẩm';
            searchEl.addEventListener('input', function() {
                const term = this.value.toLowerCase().trim();
                let vis = 0;
                allRows.forEach(function(row) {
                    const match = term === '' || row.textContent.toLowerCase().includes(term);
                    row.style.display = match ? '' : 'none';
                    if (match) vis++;
                });
                countEl.textContent = (term === '' ? allRows.length : vis) + ' sản phẩm';
            });
        })();

        // Xuất báo cáo CSV phía client
        function exportToCSV() {
            const rows = [
                ["Báo cáo bán hàng MetaFruit - Cửa hàng"],
                ["Giai doạn", "${startDate} den ${endDate}"],
                [],
                ["Ten San Pham", "Phien Ban", "So Luong Ban", "Doanh Thu (VND)", "So Don Hang"]
            ];

            const jspData = [
                <c:forEach var="item" items="${fruitUsage}">
                    [
                        "<c:out value="${item.productName}"/>",
                        "<c:out value="${item.variantLabel}"/>",
                        "${item.totalQuantity}",
                        "${item.totalAmount}",
                        "${item.orderCount}"
                    ],
                </c:forEach>
            ];

            if (jspData.length === 0) {
                Swal.fire({ icon: 'warning', title: 'Thông báo', text: 'Không có dữ liệu nào để xuất!' });
                return;
            }

            rows.push(...jspData);

            let csvContent = "";
            rows.forEach(function(rowArray) {
                let row = rowArray.map(val => {
                    if (typeof val === 'string') {
                        return '"' + val.replace(/"/g, '""') + '"';
                    }
                    return val;
                }).join(",");
                csvContent += row + "\r\n";
            });

            const BOM = "\uFEFF";
            const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement("a");
            if (link.download !== undefined) {
                const url = URL.createObjectURL(blob);
                link.setAttribute("href", url);
                link.setAttribute("download", "MetaFruit_Shop_BaoCao_" + "${startDate}" + "_to_" + "${endDate}" + ".csv");
                link.style.visibility = 'hidden';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    </script>
</body>
</html>
