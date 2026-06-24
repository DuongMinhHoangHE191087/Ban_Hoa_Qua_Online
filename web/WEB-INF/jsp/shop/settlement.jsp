<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
                <!DOCTYPE html>
                <html lang="vi">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Kênh Người Bán | Đối Soát & Doanh Thu</title>

                    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

                    <!-- Google Fonts & Icons -->
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link
                        href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">

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
                                        sans: ['Segoe UI', 'Lexend', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
                                    },
                                    boxShadow: {
                                        card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                                    }
                                }
                            }
                        }
                    </script>

                    <style>
                        body {
                            background-color: #f4fbf7;
                            font-family: 'Segoe UI', 'Lexend', -apple-system, sans-serif;
                        }

                        .glass-card {
                            background: #ffffff;
                            border: 1px solid #e2ece7;
                            box-shadow: 0 1px 3px rgba(0, 0, 0, .05), 0 4px 16px -4px rgba(20, 83, 45, .06);
                        }

                        tbody tr {
                            transition: background .12s;
                        }

                        tbody tr:hover td {
                            background: #f8fafc;
                        }
                    </style>
                </head>

                <body class="antialiased text-[#0f172a]">
                    <div class="flex min-h-screen">
                        <!-- Shared Sidebar -->
                        <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
                            <jsp:param name="activePage" value="settlement" />
                        </jsp:include>

                        <!-- Main Content Area -->
                        <main class="flex-1 p-6 md:p-8 overflow-y-auto">
                            <!-- Header Section -->
                            <div
                                class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
                                <div>
                                    <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Đối
                                        Soát & Doanh Thu</h1>
                                    <p class="text-[#475569] text-xs md:text-sm mt-1">Theo dõi các kỳ đối soát tài
                                        chính, doanh thu thực nhận và phí hệ thống.</p>
                                </div>
                                <div
                                    class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                                    <i class="fa-solid fa-wallet text-[#84cc16]"></i>
                                    <span class="text-xs font-bold uppercase tracking-wider">Tài chính</span>
                                </div>
                            </div>

                            <!-- Cumulative Summaries (Calculated in Controller) -->

                            <!-- Stats Grid Dashboard -->
                            <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                                <!-- Total Gross -->
                                <div class="glass-card p-5 flex items-center gap-4 rounded-2xl">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-money-bill-trend-up"></i>
                                    </div>
                                    <div>
                                        <span
                                            class="text-[10px] font-bold text-txt-3 uppercase tracking-wider block">Tổng
                                            doanh thu gộp</span>
                                        <h3 class="text-lg font-black text-txt mt-0.5">
                                            <fmt:formatNumber value="${totalGross}" type="number" /> đ
                                        </h3>
                                    </div>
                                </div>

                                <!-- Total Platform Fees -->
                                <div class="glass-card p-5 flex items-center gap-4 rounded-2xl">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-percent"></i>
                                    </div>
                                    <div>
                                        <span
                                            class="text-[10px] font-bold text-txt-3 uppercase tracking-wider block">Phí
                                            sàn khấu trừ</span>
                                        <h3 class="text-lg font-black text-red-600 mt-0.5">
                                            -
                                            <fmt:formatNumber value="${totalFee}" type="number" /> đ
                                        </h3>
                                    </div>
                                </div>

                                <!-- Total Refund -->
                                <div class="glass-card p-5 flex items-center gap-4 rounded-2xl">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-reply"></i>
                                    </div>
                                    <div>
                                        <span
                                            class="text-[10px] font-bold text-txt-3 uppercase tracking-wider block">Hoàn
                                            trả khách hàng</span>
                                        <h3 class="text-lg font-black text-amber-600 mt-0.5">
                                            -
                                            <fmt:formatNumber value="${totalRefund}" type="number" /> đ
                                        </h3>
                                    </div>
                                </div>

                                <!-- Total Net -->
                                <div class="glass-card p-5 flex items-center gap-4 rounded-2xl bg-emerald-50/20">
                                    <div
                                        class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                                        <i class="fa-solid fa-wallet"></i>
                                    </div>
                                    <div>
                                        <span
                                            class="text-[10px] font-bold text-[#364e03] uppercase tracking-wider block">Thực
                                            nhận (Đã chốt)</span>
                                        <h3 class="text-lg font-black text-emerald-600 mt-0.5">
                                            <fmt:formatNumber value="${totalNet}" type="number" /> đ
                                        </h3>
                                    </div>
                                </div>
                            </div>

                            <!-- Settlements List Card -->
                            <div class="glass-card rounded-2xl overflow-hidden shadow-sm bg-white">
                                <div
                                    class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                                    <h3 class="font-bold text-txt text-sm">
                                        <i class="fa-solid fa-receipt text-primary mr-1"></i> Lịch Sử Đối Soát Định Kỳ
                                    </h3>
                                </div>

                                <div class="overflow-x-auto">
                                    <table class="w-full text-left text-xs md:text-sm border-collapse">
                                        <thead>
                                            <tr
                                                class="bg-surface-2 border-b border-border text-txt-2 text-xs font-bold uppercase tracking-wider">
                                                <th class="px-6 py-3.5">Mã đối soát</th>
                                                <th class="px-6 py-3.5">Chu kỳ</th>
                                                <th class="px-6 py-3.5 text-right">Doanh thu gộp</th>
                                                <th class="px-6 py-3.5 text-right">Phí sàn</th>
                                                <th class="px-6 py-3.5 text-right">Hoàn trả</th>
                                                <th class="px-6 py-3.5 text-right">Thực nhận</th>
                                                <th class="px-6 py-3.5 text-center">Trạng thái</th>
                                                <th class="px-6 py-3.5 text-center">Thao tác</th>
                                            </tr>
                                        </thead>
                                        <tbody class="divide-y divide-[#f1f5f9] text-txt font-medium">
                                            <c:forEach var="s" items="${settlements}">
                                                <tr>
                                                    <td class="px-6 py-4 font-mono font-bold text-primary">
                                                        #${s.settlementId}</td>
                                                    <td class="px-6 py-4 text-txt-2">
                                                        <fmt:formatDate value="${s.periodStartAsDate}"
                                                            pattern="dd/MM/yyyy" /> -
                                                        <fmt:formatDate value="${s.periodEndAsDate}"
                                                            pattern="dd/MM/yyyy" />
                                                    </td>
                                                    <td class="px-6 py-4 text-right">
                                                        <ft:currency value="${s.grossAmount}" />
                                                    </td>
                                                    <td class="px-6 py-4 text-right text-red-600">
                                                        -
                                                        <ft:currency value="${s.platformFeeAmount}" />
                                                    </td>
                                                    <td class="px-6 py-4 text-right text-amber-600">
                                                        -
                                                        <ft:currency value="${s.refundAmount}" />
                                                    </td>
                                                    <td
                                                        class="px-6 py-4 text-right font-bold text-emerald-600 bg-emerald-50/10">
                                                        <ft:currency value="${s.netAmount}" />
                                                    </td>
                                                    <td class="px-6 py-4 text-center">
                                                        <c:choose>
                                                            <c:when test="${s.status == 'PENDING'}">
                                                                <span
                                                                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-[10px] font-bold">
                                                                    <i class="fa-solid fa-clock text-[9px]"></i> Chờ TT
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${s.status == 'CONFIRMED'}">
                                                                <span
                                                                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-blue-50 border border-blue-100 text-blue-800 text-[10px] font-bold">
                                                                    <i class="fa-solid fa-circle-check text-[9px]"></i>
                                                                    Đã Chốt
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${s.status == 'PAID'}">
                                                                <span
                                                                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-[10px] font-bold">
                                                                    <i class="fa-solid fa-square-check text-[9px]"></i>
                                                                    Đã Thanh Toán
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span
                                                                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-100 text-red-800 text-[10px] font-bold">
                                                                    <i class="fa-solid fa-circle-xmark text-[9px]"></i>
                                                                    Đã Hủy
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="px-6 py-4 text-center">
                                                        <fmt:formatDate var="startStr" value="${s.periodStartAsDate}"
                                                            pattern="dd/MM/yyyy" />
                                                        <fmt:formatDate var="endStr" value="${s.periodEndAsDate}"
                                                            pattern="dd/MM/yyyy" />
                                                        <button type="button"
                                                            onclick="openDetails('${s.settlementId}', '${startStr} - ${endStr}')"
                                                            class="inline-flex items-center gap-1 bg-primary hover:bg-primary-dk text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all active:scale-95 cursor-pointer shadow-sm">
                                                            <i class="fa-solid fa-magnifying-glass-chart"></i> Chi tiết
                                                        </button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty settlements}">
                                                <tr>
                                                    <td colspan="8" class="px-6 py-12 text-center text-txt-3">
                                                        <i
                                                            class="fa-solid fa-folder-open text-3xl mb-2 block text-slate-300"></i>
                                                        Chưa có kỳ đối soát nào được tạo cho shop của bạn.
                                                    </td>
                                                </tr>
                                            </c:if>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </main>
                    </div>

                    <!-- Detail Modal -->
                    <div id="detailModal" class="fixed inset-0 z-50 hidden overflow-y-auto"
                        aria-labelledby="modal-title" role="dialog" aria-modal="true">
                        <div
                            class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
                            <!-- Backdrop -->
                            <div class="fixed inset-0 bg-[#0f172a]/40 backdrop-blur-sm transition-opacity"
                                aria-hidden="true" onclick="closeModal()"></div>

                            <!-- Modal positioning helper -->
                            <span class="hidden sm:inline-block sm:align-middle sm:h-screen"
                                aria-hidden="true">&#8203;</span>

                            <!-- Modal Box -->
                            <div
                                class="inline-block align-middle bg-white rounded-2xl text-left overflow-hidden shadow-2xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full border border-border">
                                <!-- Header -->
                                <div
                                    class="bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border-b border-[#bbf7d0]/40 px-6 py-4 flex items-center justify-between">
                                    <div>
                                        <h3 class="text-base font-extrabold text-[#364e03]" id="modal-title-desc">
                                            Chi tiết đơn hàng trong kỳ đối soát #<span id="modalSettlementId"></span>
                                        </h3>
                                        <p class="text-xs text-txt-2 mt-0.5" id="modalPeriodText"></p>
                                    </div>
                                    <button type="button" onclick="closeModal()"
                                        class="text-txt-2 hover:text-red-600 transition-colors cursor-pointer text-lg">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>
                                </div>

                                <!-- Content -->
                                <div class="p-6">
                                    <!-- Loader -->
                                    <div id="modalLoader" class="flex flex-col items-center justify-center py-12">
                                        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary">
                                        </div>
                                        <span class="text-xs text-txt-2 mt-3 font-medium">Đang tải dữ liệu đơn
                                            hàng...</span>
                                    </div>

                                    <!-- Table Content -->
                                    <div id="modalTableContainer" class="hidden">
                                        <div
                                            class="overflow-x-auto rounded-xl border border-border max-h-[350px] overflow-y-auto">
                                            <table class="w-full text-left text-xs border-collapse">
                                                <thead>
                                                    <tr
                                                        class="bg-surface-2 border-b border-border text-txt-2 font-bold uppercase tracking-wider sticky top-0 bg-slate-50 z-10">
                                                        <th class="px-4 py-3">Mã đơn hàng</th>
                                                        <th class="px-4 py-3 text-right">Doanh thu gộp</th>
                                                        <th class="px-4 py-3 text-right">Phí hệ thống</th>
                                                        <th class="px-4 py-3 text-right">Voucher giảm giá</th>
                                                        <th class="px-4 py-3 text-right">Hoàn trả</th>
                                                        <th class="px-4 py-3 text-right">Thực nhận</th>
                                                    </tr>
                                                </thead>
                                                <tbody id="modalTableBody"
                                                    class="divide-y divide-[#f1f5f9] text-txt font-medium">
                                                    <!-- Dynamic rows go here -->
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>

                                <!-- Footer -->
                                <div class="bg-slate-50 px-6 py-4 border-t border-border flex justify-end">
                                    <button type="button" onclick="closeModal()"
                                        class="px-4 py-2 border border-slate-300 rounded-xl text-xs font-bold text-txt-2 hover:bg-slate-100 transition-all cursor-pointer">
                                        Đóng
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Script to handle AJAX and Modal -->
                    <script>
                        function openDetails(settlementId, periodText) {
                            document.getElementById('modalSettlementId').textContent = settlementId;
                            document.getElementById('modalPeriodText').textContent = 'Kỳ chu kỳ: ' + periodText;

                            // Show modal & loader, hide table container
                            const modal = document.getElementById('detailModal');
                            modal.classList.remove('hidden');
                            document.getElementById('modalLoader').classList.remove('hidden');
                            document.getElementById('modalTableContainer').classList.add('hidden');

                            // AJAX request
                            fetch('${pageContext.request.contextPath}/shop/settlement?action=getOrders&id=' + settlementId, {
                                headers: {
                                    'X-Requested-With': 'XMLHttpRequest'
                                }
                            })
                                .then(response => {
                                    if (!response.ok) {
                                        throw new Error('Lỗi tải chi tiết đơn hàng: ' + response.statusText);
                                    }
                                    return response.json();
                                })
                                .then(data => {
                                    const tbody = document.getElementById('modalTableBody');
                                    tbody.innerHTML = ''; // Clear existing rows

                                    if (data.length === 0) {
                                        tbody.innerHTML = '<tr><td colspan="6" class="px-4 py-8 text-center text-txt-3">Không tìm thấy đơn hàng nào trong kỳ đối soát này.</td></tr>';
                                    } else {
                                        data.forEach(item => {
                                            const tr = document.createElement('tr');
                                            tr.className = 'hover:bg-slate-50/50 transition-all duration-150';

                                            // Format money values helpers
                                            const formatMoney = (val) => Number(val).toLocaleString('vi-VN') + ' đ';

                                            tr.innerHTML = `
                            <td class="px-4 py-3 font-mono font-bold text-txt">#\${item.orderId}</td>
                            <td class="px-4 py-3 text-right">\${formatMoney(item.orderAmount)}</td>
                            <td class="px-4 py-3 text-right text-red-600">-\${formatMoney(item.platformFeeAmount)}</td>
                            <td class="px-4 py-3 text-right text-slate-500">-\${formatMoney(item.discountAmount)}</td>
                            <td class="px-4 py-3 text-right text-amber-600">-\${formatMoney(item.refundAmount)}</td>
                            <td class="px-4 py-3 text-right font-bold text-emerald-600 bg-emerald-50/10">\${formatMoney(item.netAmount)}</td>
                        `;
                                            tbody.appendChild(tr);
                                        });
                                    }

                                    // Hide loader, show table container
                                    document.getElementById('modalLoader').classList.add('hidden');
                                    document.getElementById('modalTableContainer').classList.remove('hidden');
                                })
                                .catch(error => {
                                    console.error(error);
                                    Swal.fire({
                                        title: 'Lỗi!',
                                        text: 'Không thể tải danh sách đơn hàng cho kỳ đối soát này. Vui lòng thử lại sau.',
                                        icon: 'error',
                                        confirmButtonColor: '#4d661c'
                                    });
                                    closeModal();
                                });
                        }

                        function closeModal() {
                            document.getElementById('detailModal').classList.add('hidden');
                        }
                    </script>
                </body>

                </html>