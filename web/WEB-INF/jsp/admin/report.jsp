<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thống kê & Báo cáo – Admin Verdant Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <!-- Chart.js Secure CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
        .table-container::-webkit-scrollbar {
            height: 6px;
        }
        .table-container::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 4px;
        }
    </style>
</head>
<body>
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="reports"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Thống Kê & Báo Cáo Doanh Thu</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Phân tích chuyên sâu về doanh thu nền tảng, tình trạng đơn hàng và hành vi mua sắm.</p>
            </div>
            <div class="flex items-center gap-2">
                <button onclick="exportToCSV()" class="bg-primary hover:bg-[#3d5216] text-white text-xs font-bold px-4 py-2.5 rounded-xl shadow-sm transition-all flex items-center gap-2 cursor-pointer">
                    <i class="fa-solid fa-file-csv text-sm"></i> Xuất dữ liệu (CSV)
                </button>
            </div>
        </div>

        <%-- Date Range Filter Form --%>
        <form method="GET" action="${pageContext.request.contextPath}/admin/reports" class="glass-card p-5 mb-8">
            <div class="flex flex-wrap items-end gap-4">
                <div class="flex-grow min-w-[200px]">
                    <label class="block text-xs font-bold text-txt-2 uppercase mb-1.5">Từ ngày</label>
                    <div class="relative">
                        <input type="date" name="startDate" value="${startDate}" required
                               class="w-full bg-surface-2 border border-border px-3 py-2 rounded-xl text-txt text-sm focus:outline-none focus:border-primary">
                    </div>
                </div>
                <div class="flex-grow min-w-[200px]">
                    <label class="block text-xs font-bold text-txt-2 uppercase mb-1.5">Đến ngày</label>
                    <div class="relative">
                        <input type="date" name="endDate" value="${endDate}" required
                               class="w-full bg-surface-2 border border-border px-3 py-2 rounded-xl text-txt text-sm focus:outline-none focus:border-primary">
                    </div>
                </div>
                <div class="flex gap-2">
                    <button type="submit" class="bg-primary hover:bg-primary-dk text-white text-sm font-bold px-6 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer">
                        <i class="fa-solid fa-filter mr-1.5"></i> Lọc dữ liệu
                    </button>
                    <button type="button" onclick="setQuickDateRange(30)" class="bg-white border border-border hover:bg-surface-2 text-txt text-sm px-4 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer">
                        30 ngày qua
                    </button>
                    <button type="button" onclick="setQuickDateRange(7)" class="bg-white border border-border hover:bg-surface-2 text-txt text-sm px-4 py-2.5 rounded-xl shadow-sm transition-all cursor-pointer">
                        7 ngày qua
                    </button>
                </div>
            </div>
        </form>

        <%-- KPI Cards --%>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <%-- Total Revenue --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-primary-lt text-primary flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-money-bill-wave"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Doanh thu giai đoạn</span>
                    <h3 class="text-xl font-black text-txt mt-0.5"><ft:currency value="${kpiTotalRevenue}" /></h3>
                </div>
            </div>

            <%-- Total Orders --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-cart-flatbed"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng đơn phát sinh</span>
                    <h3 class="text-xl font-black text-txt mt-0.5">${kpiTotalOrders} đơn</h3>
                </div>
            </div>

            <%-- Success Orders --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-circle-check"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Đơn thành công</span>
                    <h3 class="text-xl font-black text-txt mt-0.5">${kpiSuccessfulOrders} đơn</h3>
                </div>
            </div>

            <%-- Cancellation Rate --%>
            <div class="glass-card p-5 flex items-center gap-4 hover:-translate-y-0.5 transition-all duration-200 cursor-default">
                <div class="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center text-xl shadow-inner">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                </div>
                <div>
                    <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tỷ lệ hủy đơn</span>
                    <h3 class="text-xl font-black text-txt mt-0.5">${kpiCancellationRate}%</h3>
                </div>
            </div>
        </div>

        <%-- Charts Grid --%>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <%-- Revenue Trend Chart --%>
            <div class="glass-card p-6 flex flex-col justify-between">
                <div class="flex items-center justify-between mb-4 border-b border-border pb-3">
                    <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-chart-line text-primary mr-1.5"></i>Biểu đồ xu hướng doanh thu</h3>
                    <span class="text-xs text-txt-3">Hằng ngày</span>
                </div>
                <div class="relative h-[300px]">
                    <canvas id="revenueTrendChart"></canvas>
                </div>
            </div>

            <%-- Order success vs cancellation stats chart --%>
            <div class="glass-card p-6 flex flex-col justify-between">
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

            <%-- User Growth Chart --%>
            <div class="glass-card p-6 flex flex-col justify-between">
                <div class="flex items-center justify-between mb-4 border-b border-border pb-3">
                    <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-user-plus text-purple-500 mr-1.5"></i>Biểu đồ phát triển thành viên</h3>
                    <span class="text-xs text-txt-3">Số lượng đăng ký mới: ${kpiNewUsers}</span>
                </div>
                <div class="relative h-[300px]">
                    <canvas id="userGrowthChart"></canvas>
                </div>
            </div>

            <%-- Cancellation Reasons Chart --%>
            <div class="glass-card p-6 flex flex-col justify-between">
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
        <div class="glass-card p-6 mb-8">
            <div class="flex items-center justify-between mb-6 border-b border-border pb-4">
                <h3 class="text-base font-bold text-txt"><i class="fa-solid fa-basket-shopping text-amber-500 mr-1.5"></i>Báo cáo bán hàng & mức sử dụng trái cây nâng cao</h3>
                <span class="text-xs text-txt-3 font-semibold">Bộ dữ liệu giai đoạn</span>
            </div>
            
            <div class="table-container overflow-x-auto">
                <table class="w-full border-collapse text-left" id="fruitUsageTable">
                    <thead>
                        <tr class="border-b border-border text-xs font-bold text-txt-3 uppercase tracking-wider">
                            <th class="pb-3 pr-4">Tên Trái Cây / Sản Phẩm</th>
                            <th class="pb-3 px-4">Phiên bản / Loại</th>
                            <th class="pb-3 px-4">Cửa hàng cung cấp</th>
                            <th class="pb-3 px-4 text-center">Số lượng đã bán</th>
                            <th class="pb-3 px-4 text-right">Tổng doanh số</th>
                            <th class="pb-3 pl-4 text-center">Số đơn hàng</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-border text-sm text-txt-2">
                        <c:forEach var="item" items="${fruitUsage}">
                            <tr class="hover:bg-surface-2 transition-colors">
                                <td class="py-3.5 pr-4 font-bold text-txt"><c:out value="${item.productName}"/></td>
                                <td class="py-3.5 px-4"><span class="bg-gray-100 text-gray-700 text-xs font-semibold px-2 py-1 rounded-md"><c:out value="${item.variantLabel}"/></span></td>
                                <td class="py-3.5 px-4 text-xs font-medium"><c:out value="${item.shopName}"/></td>
                                <td class="py-3.5 px-4 text-center font-semibold text-txt">${item.totalQuantity}</td>
                                <td class="py-3.5 px-4 text-right font-bold text-primary"><ft:currency value="${item.totalAmount}" /></td>
                                <td class="py-3.5 px-4 text-center">${item.orderCount} đơn hàng</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty fruitUsage}">
                            <tr>
                                <td colspan="6" class="py-8 text-center text-txt-3 italic">Không có dữ liệu bán hàng nào trong giai đoạn này.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </main>
</div>

<%-- Script vẽ biểu đồ và xuất báo cáo CSV --%>
<script>
    // 1. Khôi phục dữ liệu từ JSP (JSON Strings được chuẩn bị ở Servlet)
    const revenueData = ${empty revenueTrendJson ? '[]' : revenueTrendJson};
    const orderStatsData = ${empty orderStatusStatsJson ? '[]' : orderStatusStatsJson};
    const userGrowthData = ${empty userGrowthJson ? '[]' : userGrowthJson};
    const cancellationData = ${empty cancellationReasonStatsJson ? '[]' : cancellationReasonStatsJson};

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
    // Map status tiếng Anh sang tiếng Việt để hiển thị trực quan
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

    // C. Biểu đồ thành viên mới
    const userCtx = document.getElementById('userGrowthChart').getContext('2d');
    new Chart(userCtx, {
        type: 'bar',
        data: {
            labels: userGrowthData.map(d => d.date),
            datasets: [{
                label: 'Thành viên mới',
                data: userGrowthData.map(d => d.count),
                backgroundColor: '#8b5cf6',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, ticks: { precision: 0 } }
            }
        }
    });

    // D. Biểu đồ lý do hủy đơn
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
            indexAxis: 'y', // Biểu đồ cột nằm ngang
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
        }

        document.getElementsByName('startDate')[0].value = formatDate(start);
        document.getElementsByName('endDate')[0].value = formatDate(end);
        document.forms[0].submit();
    }

    // Xuất báo cáo CSV phía client
    function exportToCSV() {
        const rows = [
            ["Báo cáo bán hàng Verdant Market"],
            ["Giai doan", "${startDate} den ${endDate}"],
            [],
            ["Ten San Pham", "Phien Ban", "Cua Hang", "So Luong Ban", "Doanh Thu (VND)", "So Don Hang"]
        ];

        // Duyệt dòng dữ liệu trong bảng để xuất
        const jspData = [
            <c:forEach var="item" items="${fruitUsage}">
                [
                    "<c:out value="${item.productName}"/>",
                    "<c:out value="${item.variantLabel}"/>",
                    "<c:out value="${item.shopName}"/>",
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

        // Chuyển mảng thành định dạng CSV
        let csvContent = "";
        rows.forEach(function(rowArray) {
            let row = rowArray.map(val => {
                if (typeof val === 'string') {
                    // Escape double quotes
                    return '"' + val.replace(/"/g, '""') + '"';
                }
                return val;
            }).join(",");
            csvContent += row + "\r\n";
        });

        // Tạo tệp và tải xuống kèm BOM để hiển thị đúng Unicode Tiếng Việt trong Excel
        const BOM = "\uFEFF";
        const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement("a");
        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", "Verdant_Market_Admin_BaoCao_" + "${startDate}" + "_to_" + "${endDate}" + ".csv");
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
</script>
</body>
</html>
