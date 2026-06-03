<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giám sát đơn hàng - Admin MetaFruit</title>
    <!-- Google Fonts & Tailwind CSS -->
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    
    <script id="tailwind-config">
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: "#4d661c",
                        "primary-dark": "#364e03",
                        "primary-light": "#d9f99d",
                        bg: "#f4fbf7",
                        surface: "#ffffff",
                        border: "#e2ece7",
                        "text-primary": "#00210d",
                        "text-secondary": "#44483b",
                        "text-muted": "#8e9285",
                    }
                }
            }
        }
    </script>
    
    <style>
        .premium-glass-card {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(77, 102, 28, 0.08);
            box-shadow: 0 10px 30px -10px rgba(20, 83, 45, 0.03);
        }
    </style>
</head>
<body class="bg-bg text-text-primary">
    <div class="admin-layout flex min-h-screen">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="orders"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="flex-1 p-6 md:p-8 overflow-y-auto">
            <header class="mb-8 pb-4 border-b border-border flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-text-primary">Giám Sát Đơn Hàng</h1>
                    <p class="text-text-secondary text-sm mt-1">Xem và kiểm duyệt toàn bộ luồng giao dịch, thanh toán đơn hàng trên toàn hệ thống.</p>
                </div>
            </header>

            <!-- Flash Message (PRG pattern support) -->
            <c:if test="${not empty sessionScope.flashMsg}">
                <script>
                    Swal.fire({
                        icon: '${sessionScope.flashType == "success" ? "success" : "error"}',
                        title: '${sessionScope.flashType == "success" ? "Thành công" : "Lỗi"}',
                        text: '${sessionScope.flashMsg}',
                        timer: 3000,
                        showConfirmButton: false
                    });
                </script>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <!-- Filter Suggestions & Developer Guide Panel -->
            <div class="bg-emerald-50/60 border border-emerald-200/50 rounded-2xl p-5 mb-8 text-sm">
                <h4 class="font-bold text-primary flex items-center gap-2 mb-2">
                    <span class="material-symbols-outlined text-lg">tips_and_updates</span>
                    Hướng dẫn mở rộng bộ lọc đơn hàng (Developer Filter Expansion Guide)
                </h4>
                <p class="text-text-secondary text-xs mb-3 leading-relaxed">
                    Bạn có thể tự thêm các bộ lọc mới (ví dụ: <code>minAmount</code>, <code>dateRange</code>) bằng cách mở rộng 3 tầng dữ liệu dưới đây:
                </p>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-3 font-mono text-[11px] leading-relaxed">
                    <div class="bg-white/80 p-3.5 rounded-xl border border-emerald-100 shadow-sm">
                        <span class="font-bold text-primary block mb-1">1. DAO (OrderDAO.java)</span>
                        <span class="text-text-secondary text-[10px]">Thêm điều kiện vào câu SQL:</span>
                        <pre class="bg-slate-900 text-slate-100 p-2 rounded text-[10px] overflow-x-auto mt-1">
if (minAmount != null) {
    sql.append("AND o.final_amount >= ? ");
    params.add(minAmount);
}</pre>
                    </div>
                    <div class="bg-white/80 p-3.5 rounded-xl border border-emerald-100 shadow-sm">
                        <span class="font-bold text-blue-700 block mb-1">2. Servlet Layer</span>
                        <span class="text-text-secondary text-[10px]">Đọc tham số gửi từ request:</span>
                        <pre class="bg-slate-900 text-slate-100 p-2 rounded text-[10px] overflow-x-auto mt-1">
String minAmtStr = req.getParameter("minAmount");
BigDecimal minAmount = minAmtStr != null ? 
    new BigDecimal(minAmtStr) : null;</pre>
                    </div>
                    <div class="bg-white/80 p-3.5 rounded-xl border border-emerald-100 shadow-sm">
                        <span class="font-bold text-purple-700 block mb-1">3. New JSP Input Field</span>
                        <span class="text-text-secondary text-[10px]">Thêm thẻ input vào form lọc:</span>
                        <pre class="bg-slate-900 text-slate-100 p-2 rounded text-[10px] overflow-x-auto mt-1">
&lt;input type="number" 
    name="minAmount" 
    value="${param.minAmount}" 
    placeholder="Nhập giá bán..." /&gt;</pre>
                    </div>
                </div>
            </div>

            <!-- Section 1: Đơn hàng chuyển khoản cần xác nhận thanh toán -->
            <div class="premium-glass-card rounded-[1.5rem] p-6 mb-8 border-l-4 border-l-amber-500">
                <h3 class="text-lg font-bold text-amber-700 flex items-center gap-2 mb-4">
                    <span class="material-symbols-outlined">payments</span> Đơn Chuyển Khoản Chờ Phê Duyệt (${pendingPayments.size()})
                </h3>
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse text-sm">
                        <thead>
                            <tr class="bg-slate-50/50">
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Mã Đơn</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Khách Hàng</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Gian Hàng</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Mã Tham Chiếu QR</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Trạng Thái Giao Dịch</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Số Tiền</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary text-center">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-border/60">
                            <c:choose>
                                <c:when test="${empty pendingPayments}">
                                    <tr>
                                        <td colspan="7" class="p-8 text-center text-text-muted italic">Không có đơn hàng nào chờ duyệt thanh toán.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="order" items="${pendingPayments}">
                                        <c:set var="pt" value="${pendingTxMap[order.orderId]}" />
                                        <tr class="hover:bg-primary-light/10 transition-colors">
                                            <td class="p-3 font-bold text-primary">#${order.orderId}</td>
                                            <td class="p-3">User ID: ${order.customerId}</td>
                                            <td class="p-3">Shop ID: ${order.ownerId}</td>
                                            <td class="p-3 font-mono text-xs font-bold text-slate-600">${pt != null ? pt.sepayReference : 'N/A'}</td>
                                            <td class="p-3">
                                                <c:choose>
                                                    <c:when test="${empty pt}">
                                                        <span class="px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-bold border border-gray-200">Chưa tạo QR</span>
                                                    </c:when>
                                                    <c:when test="${pt.status == 'pending'}">
                                                        <span class="px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-bold border border-gray-200">Chưa thanh toán</span>
                                                    </c:when>
                                                    <c:when test="${pt.status == 'processing'}">
                                                        <span class="px-2.5 py-1 bg-amber-100 text-amber-800 rounded-full text-xs font-bold border border-amber-200 animate-pulse">Khách báo đã CK</span>
                                                    </c:when>
                                                    <c:when test="${pt.status == 'completed'}">
                                                        <span class="px-2.5 py-1 bg-emerald-100 text-emerald-800 rounded-full text-xs font-bold border border-emerald-200">Đã khớp SePay (Chờ duyệt)</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="px-2.5 py-1 bg-red-100 text-red-800 rounded-full text-xs font-bold border border-red-200">${pt.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="p-3 text-[#ba1a1a] font-extrabold"><ft:currency value="${order.finalAmount}" /></td>
                                            <td class="p-3 text-center">
                                                <div class="flex gap-2 justify-center">
                                                    <form action="${pageContext.request.contextPath}/admin/orders" method="POST" class="inline" onsubmit="return confirmApprove(event, '${order.orderId}')">
                                                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                        <input type="hidden" name="action" value="confirmPayment">
                                                        <input type="hidden" name="orderId" value="${order.orderId}">
                                                        <button type="submit" class="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-3 py-1.5 rounded-lg flex items-center gap-1 shadow-sm transition-all hover:scale-105 active:scale-95 cursor-pointer">
                                                            <span class="material-symbols-outlined text-sm">check</span> Xác nhận nhận tiền
                                                        </button>
                                                    </form>
                                                    <button class="bg-red-50 text-red-600 hover:bg-red-600 hover:text-white border border-red-200 text-xs font-bold px-3 py-1.5 rounded-lg flex items-center gap-1 shadow-sm transition-all active:scale-95 cursor-pointer" onclick="showCancelModal('${order.orderId}')">
                                                        <span class="material-symbols-outlined text-sm">close</span> Hủy đơn
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Section 2: Bộ lọc và danh sách tất cả các đơn hàng -->
            <div class="premium-glass-card rounded-[1.5rem] p-6">
                <h3 class="text-lg font-bold text-primary-dark flex items-center gap-2 mb-6 border-b border-border pb-3">
                    <span class="material-symbols-outlined">list_alt</span> Lịch Sử Giao Dịch Toàn Sàn
                </h3>
                
                <!-- Dynamic Filters Form -->
                <form action="" method="GET" class="flex flex-wrap gap-4 items-end bg-slate-50/50 p-4 rounded-2xl border border-border mb-6">
                    <div class="flex flex-col gap-1 flex-1 min-w-[200px]">
                        <span class="text-[10px] font-bold text-text-secondary uppercase tracking-wider">Trạng thái Đơn hàng</span>
                        <select name="status" class="w-full rounded-xl border border-border/80 p-2 bg-white text-xs focus:ring-2 focus:ring-primary focus:outline-none">
                            <option value="" ${empty statusFilter ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="PENDING_PAYMENT" ${statusFilter == 'PENDING_PAYMENT' ? 'selected' : ''}>Chờ thanh toán</option>
                            <option value="CONFIRMED" ${statusFilter == 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                            <option value="PREPARING" ${statusFilter == 'PREPARING' ? 'selected' : ''}>Shop đang chuẩn bị</option>
                            <option value="APPROVED" ${statusFilter == 'APPROVED' ? 'selected' : ''}>Đã duyệt (Chờ giao)</option>
                            <option value="DISPATCHED" ${statusFilter == 'DISPATCHED' ? 'selected' : ''}>Đang giao</option>
                            <option value="DELIVERED" ${statusFilter == 'DELIVERED' ? 'selected' : ''}>Đã nhận</option>
                            <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </div>
                    
                    <div class="flex flex-col gap-1 flex-1 min-w-[150px]">
                        <span class="text-[10px] font-bold text-text-secondary uppercase tracking-wider">Hình thức thanh toán</span>
                        <select name="paymentMethod" class="w-full rounded-xl border border-border/80 p-2 bg-white text-xs focus:ring-2 focus:ring-primary focus:outline-none">
                            <option value="" ${empty paymentMethod ? 'selected' : ''}>Tất cả hình thức</option>
                            <option value="CK" ${paymentMethod == 'CK' ? 'selected' : ''}>Chuyển khoản (CK)</option>
                            <option value="COD" ${paymentMethod == 'COD' ? 'selected' : ''}>Thanh toán khi nhận (COD)</option>
                        </select>
                    </div>

                    <div class="flex flex-col gap-1 flex-1 min-w-[180px]">
                        <span class="text-[10px] font-bold text-text-secondary uppercase tracking-wider">Trạng thái Giao dịch</span>
                        <select name="paymentStatus" class="w-full rounded-xl border border-border/80 p-2 bg-white text-xs focus:ring-2 focus:ring-primary focus:outline-none">
                            <option value="" ${empty paymentStatus ? 'selected' : ''}>Tất cả trạng thái GD</option>
                            <option value="pending" ${paymentStatus == 'pending' ? 'selected' : ''}>Chưa thanh toán (pending)</option>
                            <option value="processing" ${paymentStatus == 'processing' ? 'selected' : ''}>Khách báo đã chuyển (processing)</option>
                            <option value="completed" ${paymentStatus == 'completed' ? 'selected' : ''}>Thành công (completed)</option>
                            <option value="failed" ${paymentStatus == 'failed' ? 'selected' : ''}>Thất bại (failed)</option>
                            <option value="refunded" ${paymentStatus == 'refunded' ? 'selected' : ''}>Đã hoàn tiền (refunded)</option>
                        </select>
                    </div>

                    <div class="flex gap-2">
                        <button type="submit" class="bg-primary text-white hover:bg-primary-dark font-bold px-5 py-2 rounded-xl text-xs transition-all shadow-sm flex items-center gap-1 active:scale-95 cursor-pointer">
                            <span class="material-symbols-outlined text-sm">search</span> Lọc đơn
                        </button>
                        <a href="?" class="bg-white border border-border hover:bg-slate-100 text-text-secondary font-bold px-4 py-2 rounded-xl text-xs transition-all flex items-center justify-center cursor-pointer">
                            Reset
                        </a>
                    </div>
                </form>

                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse text-sm">
                        <thead>
                            <tr class="bg-slate-50/50">
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Mã Đơn</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Khách Hàng</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Chủ Shop</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Hình Thức</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Chi Tiết Giao Dịch</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Tổng Tiền</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Trạng Thái Đơn</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary">Thời Gian Đặt</th>
                                <th class="p-3 border-b border-border font-semibold text-text-secondary text-center">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-border/60">
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="9" class="p-8 text-center text-text-muted italic">Không tìm thấy dữ liệu đơn hàng nào.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="order" items="${orders}">
                                        <c:set var="pt" value="${txMap[order.orderId]}" />
                                        <tr class="hover:bg-primary-light/10 transition-colors">
                                            <td class="p-3 font-bold text-primary">#${order.orderId}</td>
                                            <td class="p-3">Khách ID: ${order.customerId}</td>
                                            <td class="p-3">Shop ID: ${order.ownerId}</td>
                                            <td class="p-3">
                                                <span class="px-2 py-0.5 rounded text-[11px] font-bold ${order.paymentMethod == 'CK' ? 'bg-blue-50 text-blue-700 border border-blue-100' : 'bg-amber-50 text-amber-700 border border-amber-100'}">
                                                    ${order.paymentMethod}
                                                </span>
                                            </td>
                                            <td class="p-3">
                                                <c:choose>
                                                    <c:when test="${order.paymentMethod == 'COD'}">
                                                        <span class="text-text-muted text-xs italic">COD (Thu tiền khi nhận)</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:choose>
                                                            <c:when test="${empty pt}">
                                                                <span class="text-text-muted text-xs">Chưa tạo QR</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="flex flex-col text-xs">
                                                                    <span class="font-bold text-slate-700">Ref: ${pt.sepayReference}</span>
                                                                    <span class="text-text-muted text-[10px]">Trạng thái: 
                                                                        <strong class="${pt.status == 'completed' ? 'text-emerald-600' : (pt.status == 'processing' ? 'text-amber-600' : 'text-slate-600')}">${pt.status}</strong>
                                                                    </span>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="p-3 text-[#ba1a1a] font-extrabold"><ft:currency value="${order.finalAmount}" /></td>
                                            <td class="p-3">
                                                <span class="px-2.5 py-1 rounded-full text-xs font-bold inline-flex items-center gap-1
                                                    ${order.status == 'PENDING_PAYMENT' ? 'bg-amber-50 text-amber-800 border border-amber-200' : ''}
                                                    ${order.status == 'CONFIRMED' ? 'bg-blue-50 text-blue-800 border border-blue-200' : ''}
                                                    ${order.status == 'PREPARING' ? 'bg-indigo-50 text-indigo-800 border border-indigo-200' : ''}
                                                    ${order.status == 'APPROVED' ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : ''}
                                                    ${order.status == 'DISPATCHED' ? 'bg-sky-50 text-sky-800 border border-sky-200' : ''}
                                                    ${order.status == 'DELIVERED' ? 'bg-teal-50 text-teal-800 border border-teal-200' : ''}
                                                    ${order.status == 'CANCELLED' ? 'bg-red-50 text-red-800 border border-red-200' : ''}
                                                ">
                                                    <c:choose>
                                                        <c:when test="${order.status == 'PENDING_PAYMENT'}">Chờ thanh toán</c:when>
                                                        <c:when test="${order.status == 'CONFIRMED'}">Đã xác nhận</c:when>
                                                        <c:when test="${order.status == 'PREPARING'}">Shop đang chuẩn bị</c:when>
                                                        <c:when test="${order.status == 'APPROVED'}">Đã duyệt (Chờ giao)</c:when>
                                                        <c:when test="${order.status == 'DISPATCHED'}">Đang giao</c:when>
                                                        <c:when test="${order.status == 'DELIVERED'}">Đã nhận</c:when>
                                                        <c:when test="${order.status == 'CANCELLED'}">Đã hủy</c:when>
                                                        <c:otherwise>${order.status}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>
                                            <td class="p-3 text-text-muted text-xs">${order.createdAt}</td>
                                            <td class="p-3 text-center">
                                                <div class="flex gap-2 justify-center">
                                                    <!-- Nút Hủy đơn cho các đơn chưa kết thúc -->
                                                    <c:if test="${order.status != 'DELIVERED' && order.status != 'CANCELLED'}">
                                                        <button class="bg-red-50 text-red-600 hover:bg-red-600 hover:text-white border border-red-100 text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm transition-all active:scale-95 cursor-pointer" onclick="showCancelModal('${order.orderId}')">
                                                            <i class="fa-solid fa-trash-can"></i> Hủy đơn
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${order.status == 'DELIVERED' || order.status == 'CANCELLED'}">
                                                        <span class="text-text-muted text-xs italic">Không thể thao tác</span>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination UI with advanced parameters preserved -->
                <div class="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-border gap-4">
                    <span class="text-xs text-text-secondary font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <div class="flex gap-1.5">
                        <c:if test="${currentPage > 1}">
                            <a href="?status=${statusFilter}&paymentMethod=${paymentMethod}&paymentStatus=${paymentStatus}&page=${currentPage - 1}" class="bg-white border border-border hover:bg-slate-50 text-text-secondary font-bold px-4 py-2 rounded-xl text-xs transition-all flex items-center gap-1 active:scale-95 cursor-pointer">
                                <span class="material-symbols-outlined text-sm">chevron_left</span> Trước
                            </a>
                        </c:if>
                        <c:if test="${currentPage < totalPages}">
                            <a href="?status=${statusFilter}&paymentMethod=${paymentMethod}&paymentStatus=${paymentStatus}&page=${currentPage + 1}" class="bg-white border border-border hover:bg-slate-50 text-text-secondary font-bold px-4 py-2 rounded-xl text-xs transition-all flex items-center gap-1 active:scale-95 cursor-pointer">
                                Sau <span class="material-symbols-outlined text-sm">chevron_right</span>
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal Hủy Đơn -->
    <div id="cancelModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
        <div class="bg-white rounded-2xl p-6 w-full max-w-md shadow-2xl border border-border animate-fade-in">
            <h3 class="text-lg font-bold text-text-primary mb-2 flex items-center gap-2">
                <span class="material-symbols-outlined text-red-600">warning</span> Hủy đơn hàng bởi Admin
            </h3>
            <p class="text-text-secondary text-xs mb-4 leading-relaxed">Vui lòng nhập lý do hủy đơn hàng này để cập nhật hệ thống và tự động hoàn trả tồn kho sản phẩm.</p>
            <form action="${pageContext.request.contextPath}/admin/orders" method="POST" onsubmit="return submitCancel(event)">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="action" value="cancelOrder">
                <input type="hidden" name="orderId" id="cancelOrderId">
                <textarea name="reason" id="cancelReason" rows="4" class="w-full rounded-xl border border-border/80 p-3 text-sm focus:ring-2 focus:ring-primary focus:outline-none mb-4" placeholder="Nhập lý do chi tiết..." required></textarea>
                <div class="flex justify-end gap-2">
                    <button type="button" class="bg-white border border-border text-text-secondary font-bold px-4 py-2 rounded-xl text-xs transition-all active:scale-95 cursor-pointer" onclick="closeCancelModal()">Hủy bỏ</button>
                    <button type="submit" class="bg-red-600 hover:bg-red-700 text-white font-bold px-4 py-2 rounded-xl text-xs transition-all shadow-md active:scale-95 cursor-pointer">Xác nhận Hủy Đơn</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function confirmApprove(event, orderId) {
            event.preventDefault();
            Swal.fire({
                title: 'Duyệt thanh toán?',
                text: "Bạn xác nhận đã nhận đủ tiền cho đơn hàng #" + orderId + "?",
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#10b981',
                cancelButtonColor: '#d1d5db',
                confirmButtonText: 'Đúng, xác nhận',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    event.target.submit();
                }
            });
            return false;
        }

        function showCancelModal(orderId) {
            document.getElementById('cancelOrderId').value = orderId;
            document.getElementById('cancelReason').value = '';
            document.getElementById('cancelModal').classList.remove('hidden');
        }

        function closeCancelModal() {
            document.getElementById('cancelModal').classList.add('hidden');
        }

        function submitCancel(event) {
            event.preventDefault();
            const reason = document.getElementById('cancelReason').value.trim();
            if(!reason) {
                Swal.fire('Lỗi', 'Vui lòng nhập lý do hủy đơn hàng', 'error');
                return false;
            }
            event.target.submit();
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const modal = document.getElementById('cancelModal');
            if (event.target === modal) {
                closeCancelModal();
            }
        }
    </script>
</body>
</html>
