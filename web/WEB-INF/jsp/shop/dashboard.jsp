<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kênh Người Bán | Tổng Quan Cửa Hàng</title>
    
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">
    
    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    
    <!-- Core Tailwind and SweetAlert -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:      '#4d661c',
                        'primary-hover': '#364e03',
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
            box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 4px 16px -4px rgba(20,83,45,.06);
        }
    </style>
</head>
<body class="antialiased text-[#0f172a]">
    <div class="flex min-h-screen">
        <!-- Shared Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
            <jsp:param name="activePage" value="dashboard"/>
        </jsp:include>

        <!-- Main Content Area -->
        <main class="flex-1 p-6 md:p-8 overflow-y-auto">
            <!-- Header Section -->
            <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
                <div>
                    <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Tổng Quan Vận Hành</h1>
                    <p class="text-[#475569] text-xs md:text-sm mt-1">Xem nhanh các chỉ số vận hành quan trọng và các thao tác quản lý nhanh.</p>
                </div>
                <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                    <i class="fa-solid fa-leaf text-[#84cc16]"></i>
                    <span class="text-xs font-bold uppercase tracking-wider">MetaFruit Live</span>
                </div>
            </div>



            <!-- Stats Grid Dashboard -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <!-- Revenue Card -->
                <div class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="flex items-center gap-4">
                        <div class="w-12 h-12 rounded-2xl bg-[#E8F5E9] text-emerald-600 flex items-center justify-center text-xl shadow-inner">
                            <i class="fa-solid fa-money-bill-wave"></i>
                        </div>
                        <div>
                            <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng doanh thu</span>
                            <h3 class="text-2xl font-black text-txt mt-0.5">
                                <ft:currency value="${revenue}" />
                            </h3>
                        </div>
                    </div>
                </div>

                <!-- Orders Card -->
                <div class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="flex items-center gap-4">
                        <div class="w-12 h-12 rounded-2xl bg-[#E3F2FD] text-blue-600 flex items-center justify-center text-xl shadow-inner">
                            <i class="fa-solid fa-clipboard-list"></i>
                        </div>
                        <div>
                            <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Tổng đơn hàng</span>
                            <h3 class="text-2xl font-black text-txt mt-0.5">${orderCount}</h3>
                        </div>
                    </div>
                </div>

                <!-- Low Stock Alert Card -->
                <div class="glass-card p-5 flex items-center justify-between hover:-translate-y-1 transition-all duration-200 rounded-2xl cursor-default">
                    <div class="flex items-center gap-4">
                        <div class="w-12 h-12 rounded-2xl ${lowStock > 0 ? 'bg-red-50 text-red-600 animate-pulse' : 'bg-orange-50 text-amber-600'} flex items-center justify-center text-xl shadow-inner">
                            <i class="fa-solid fa-triangle-exclamation"></i>
                        </div>
                        <div>
                            <span class="text-xs font-bold text-txt-3 uppercase tracking-wider">Sắp hết hàng</span>
                            <h3 class="text-2xl font-black ${lowStock > 0 ? 'text-red-600' : 'text-txt'} mt-0.5">${lowStock}</h3>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Main Layout Split Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Recent Orders Table (Col span 2) -->
                <div class="lg:col-span-2 bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-5">
                            <h2 class="text-base font-bold text-txt flex items-center gap-2">
                                <i class="fa-solid fa-clock-rotate-left text-primary"></i> Đơn hàng gần đây
                            </h2>
                            <a href="${pageContext.request.contextPath}/shop/orders" class="text-xs text-primary hover:text-[#364e03] font-bold transition-all">Xem tất cả →</a>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left border-collapse">
                                <thead>
                                    <tr class="bg-gray-50/60 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
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
                                            <td class="py-3 px-4 font-bold text-gray-700">#${order.orderId}</td>
                                            <td class="py-3 px-4 text-xs text-gray-500">${order.createdAt}</td>
                                            <td class="py-3 px-4 font-bold text-primary"><ft:currency value="${order.finalAmount}" /></td>
                                            <td class="py-3 px-4">
                                                <c:choose>
                                                    <c:when test="${order.status == 'PENDING_PAYMENT'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200">Chờ thanh toán</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'CONFIRMED'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">Chờ duyệt</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'PREPARING'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-50 text-purple-700 border border-purple-200">Chuẩn bị</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'APPROVED'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">Đã Duyệt</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'DISPATCHED'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-pink-50 text-pink-700 border border-pink-200">Đang giao</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'DELIVERED'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">Đã giao</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'CANCELLED'}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-50 text-red-700 border border-red-200">Đã hủy</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-50 text-gray-600 border border-gray-200">${order.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="py-3 px-4 text-right">
                                                <a href="${pageContext.request.contextPath}/shop/orders?status=${order.status}" class="text-xs font-bold text-primary hover:underline">Xem & Xử lý</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty recentOrders}">
                                        <tr>
                                            <td colspan="5" class="py-12 text-center text-txt-3 font-light text-xs">
                                                <i class="fa-solid fa-box-open text-3xl mb-3 text-gray-300 block"></i>
                                                Chưa có đơn hàng nào của shop được ghi nhận!
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Quick Actions Widget -->
                <div class="bg-white border border-[#e2ece7] rounded-2xl p-6 shadow-sm flex flex-col justify-between">
                    <div>
                        <h2 class="text-base font-bold text-txt flex items-center gap-2 mb-5">
                            <i class="fa-solid fa-bolt text-amber-500"></i> Thao tác nhanh
                        </h2>
                        <div class="flex flex-col gap-3">
                            <a href="${pageContext.request.contextPath}/shop/products" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-box"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Quản lý Sản phẩm</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Thêm, sửa đổi hoặc ẩn sản phẩm</p>
                                </div>
                            </a>

                            <a href="${pageContext.request.contextPath}/shop/orders" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-file-invoice"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Quản lý Đơn hàng</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Duyệt đơn và giao vận chuyển</p>
                                </div>
                            </a>

                            <a href="${pageContext.request.contextPath}/shop/inventory" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-warehouse"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Quản lý Nhập kho</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Theo dõi & cập nhật lượng tồn kho</p>
                                </div>
                            </a>

                            <a href="${pageContext.request.contextPath}/shop/settlement" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-wallet"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Đối soát &amp; Doanh thu</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Xem doanh thu &amp; lịch sử đối soát</p>
                                </div>
                            </a>

                            <a href="${pageContext.request.contextPath}/shop/reports" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-chart-column"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Báo cáo &amp; Thống kê</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Xu hướng doanh số &amp; sản lượng bán</p>
                                </div>
                            </a>

                            <a href="${pageContext.request.contextPath}/home" class="group flex items-center gap-3.5 p-3.5 rounded-xl border border-gray-150/70 hover:border-primary/20 hover:bg-[#f4fbf7]/40 transition-all duration-200">
                                <div class="w-10 h-10 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center text-base transition-colors group-hover:bg-primary group-hover:text-white">
                                    <i class="fa-solid fa-house"></i>
                                </div>
                                <div class="flex-grow">
                                    <h4 class="text-xs font-bold text-gray-700 group-hover:text-primary transition-colors">Về Trang chủ</h4>
                                    <p class="text-[10px] text-gray-400 mt-0.5">Xem gian hàng theo góc nhìn khách</p>
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
