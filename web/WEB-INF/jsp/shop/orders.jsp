<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý Đơn hàng | Kênh Người Bán</title>
                <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
                <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
                <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
                <script>
                    tailwind.config = {
                        theme: {
                            extend: {
                                colors: {
                                    primary: '#4d661c', 'primary-hover': '#364e03',
                                    'primary-lt': '#f0f7e6', border: '#e2ece7',
                                    txt: '#0f172a', 'txt-2': '#475569', 'txt-3': '#94a3b8',
                                },
                                fontFamily: { sans: ['Lexend', 'sans-serif'] }
                            }
                        }
                    }
                </script>
                <style>
                    body {
                        background-color: #f4fbf7;
                        font-family: 'Lexend', sans-serif;
                    }

                    .glass-card {
                        background: #ffffff;
                        border: 1px solid #e2ece7;
                        box-shadow: 0 1px 3px rgba(0, 0, 0, .05), 0 4px 16px -4px rgba(20, 83, 45, .06);
                    }

                    .modal-overlay {
                        display: none;
                        position: fixed;
                        inset: 0;
                        z-index: 1000;
                        background: rgba(0, 0, 0, 0.45);
                        backdrop-filter: blur(4px);
                        align-items: center;
                        justify-content: center;
                    }

                    .modal-overlay.active {
                        display: flex;
                    }
                    .pagination-wrapper { padding: 0 !important; }
                    .pagination { gap: 0.375rem !important; margin: 0 !important; display: flex; list-style: none; }
                    .pagination .page-link {
                        display: inline-flex; align-items: center; justify-content: center;
                        min-width: 2.25rem; height: 2.25rem; border-radius: 0.5rem;
                        font-size: 0.75rem; font-weight: 600;
                        border: 1px solid #e2ece7; background: #fff;
                        color: #374151; cursor: pointer; transition: all 0.15s;
                        text-decoration: none;
                    }
                    .pagination .page-item.active .page-link {
                        background: #4d661c; border-color: #4d661c; color: #fff;
                    }
                    .pagination .page-item.disabled .page-link {
                        color: #94a3b8; border-color: #e2ece7; background: #f8fafc; cursor: not-allowed;
                    }
                    .pagination .page-item .page-link:hover:not(.disabled) {
                        background: #f1f5f9; border-color: #9ca3af;
                    }
                </style>
            </head>

            <body class="antialiased text-[#0f172a]">
                <div class="flex min-h-screen">

                    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
                        <jsp:param name="activePage" value="orders" />
                    </jsp:include>

                    <main class="flex-1 p-6 md:p-8 overflow-y-auto">

                        <!-- Page Header -->
                        <div
                            class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
                            <div>
                                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Quản lý Đơn
                                    hàng</h1>
                                <p class="text-[#475569] text-xs md:text-sm mt-1">Duyệt đơn, bàn giao vận chuyển, theo
                                    dõi trạng thái đơn hàng.</p>
                            </div>
                            <div
                                class="hidden md:flex items-center gap-2 bg-white/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                                <i class="fa-solid fa-clipboard-list text-primary"></i>
                                <span class="text-xs font-bold uppercase tracking-wider">Đơn hàng</span>
                            </div>
                        </div>

                        <!-- Flash Message -->
                        <c:if test="${not empty sessionScope.flashMsg}">
                            <div id="flash-alert"
                                class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 shadow-sm text-sm font-semibold
                 ${sessionScope.flashType == 'success' ? 'bg-emerald-50 border-emerald-500 text-emerald-800' : 'bg-red-50 border-red-500 text-red-800'}">
                                <i
                                    class="fa-solid ${sessionScope.flashType == 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
                                <span class="flex-1">
                                    <c:out value="${sessionScope.flashMsg}" />
                                </span>
                                <button onclick="document.getElementById('flash-alert').remove()"
                                    class="opacity-60 hover:opacity-100 transition-opacity">
                                    <i class="fa-solid fa-xmark"></i>
                                </button>
                            </div>
                            <c:remove var="flashMsg" scope="session" />
                            <c:remove var="flashType" scope="session" />
                        </c:if>

                        <!-- Status Filter Pills -->
                        <div class="flex flex-wrap gap-2 mb-6">
                            <a href="?status="
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${empty status ? 'bg-primary text-white border-primary shadow-sm' : 'bg-white text-txt-2 border-border hover:border-primary hover:text-primary'}">
                                <i class="fa-solid fa-list-ul mr-1"></i>Tất cả
                            </a>
                            <a href="?status=PENDING_PAYMENT"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'PENDING_PAYMENT' ? 'bg-amber-500 text-white border-amber-500 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-amber-400 hover:text-amber-600'}">
                                <i class="fa-solid fa-clock mr-1"></i>Chờ thanh toán
                            </a>
                            <a href="?status=CONFIRMED"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'CONFIRMED' ? 'bg-blue-600 text-white border-blue-600 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-blue-400 hover:text-blue-600'}">
                                <i class="fa-solid fa-bell mr-1"></i>Chờ duyệt
                            </a>
                            <a href="?status=APPROVED"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'APPROVED' ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-indigo-400 hover:text-indigo-600'}">
                                <i class="fa-solid fa-check-double mr-1"></i>Đã duyệt
                            </a>
                            <a href="?status=DISPATCHED"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'DISPATCHED' ? 'bg-pink-600 text-white border-pink-600 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-pink-400 hover:text-pink-600'}">
                                <i class="fa-solid fa-truck mr-1"></i>Đang giao
                            </a>
                            <a href="?status=DELIVERED"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'DELIVERED' ? 'bg-emerald-600 text-white border-emerald-600 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-emerald-400 hover:text-emerald-600'}">
                                <i class="fa-solid fa-circle-check mr-1"></i>Hoàn thành
                            </a>
                            <a href="?status=CANCELLED"
                                class="px-4 py-2 rounded-full text-xs font-bold border transition-all duration-200
               ${status == 'CANCELLED' ? 'bg-red-600 text-white border-red-600 shadow-sm' : 'bg-white text-txt-2 border-border hover:border-red-400 hover:text-red-600'}">
                                <i class="fa-solid fa-ban mr-1"></i>Đã hủy
                            </a>
                        </div>

                        <!-- Orders Table -->
                        <div class="glass-card rounded-2xl overflow-hidden">
                            <div class="overflow-x-auto">
                                <table class="w-full text-left border-collapse">
                                    <thead>
                                        <tr
                                            class="bg-[#f0faf3] border-b border-[#e2ece7] text-xs font-bold text-[#4d661c] uppercase tracking-wider">
                                            <th class="py-3.5 px-5">Mã ĐH</th>
                                            <th class="py-3.5 px-4">Khách hàng</th>
                                            <th class="py-3.5 px-4">Ngày tạo</th>
                                            <th class="py-3.5 px-4">Tổng tiền</th>
                                            <th class="py-3.5 px-4">Thanh toán</th>
                                            <th class="py-3.5 px-4">Trạng thái</th>
                                            <th class="py-3.5 px-5 text-right">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody class="divide-y divide-[#f0f4f0] text-sm">
                                        <c:forEach var="order" items="${orders}">
                                            <tr class="hover:bg-[#f9fdf9] transition-colors duration-150">
                                                <td class="py-3.5 px-5 font-bold text-[#364e03]">#${order.orderId}</td>
                                                <td class="py-3.5 px-4 text-txt-2 text-xs">
                                                    <div class="flex items-start gap-2">
                                                        <div
                                                            class="w-7 h-7 rounded-full bg-[#edf7f2] text-[#4d661c] flex items-center justify-center text-[10px] font-bold shrink-0 mt-0.5">
                                                            <i class="fa-solid fa-user"></i>
                                                        </div>
                                                        <div>
                                                            <c:choose>
                                                                <c:when test="${not empty order.recipientName}">
                                                                    <span
                                                                        class="font-semibold text-txt block">${order.recipientName}</span>
                                                                    <c:if test="${not empty order.recipientPhone}">
                                                                        <span
                                                                            class="text-txt-3 text-[10px]">${order.recipientPhone}</span>
                                                                    </c:if>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span>KH #${order.customerId}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="py-3.5 px-4 text-xs text-txt-3">${order.createdAt}</td>
                                                <td class="py-3.5 px-4 font-bold text-primary">
                                                    <ft:currency value="${order.finalAmount}" />
                                                </td>
                                                <td class="py-3.5 px-4 text-xs text-txt-2">
                                                    <span
                                                        class="px-2 py-1 rounded-lg bg-gray-100 text-gray-600 font-medium">${order.paymentMethod}</span>
                                                </td>
                                                <td class="py-3.5 px-4">
                                                    <c:choose>
                                                        <c:when test="${order.status == 'PENDING_PAYMENT'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-200">
                                                                <i class="fa-solid fa-clock text-[8px]"></i>Chờ TT
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'CONFIRMED'}">
                                                            <div class="flex flex-col gap-1">
                                                                <span
                                                                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">
                                                                    <i class="fa-solid fa-bell text-[8px]"></i>Chờ Duyệt
                                                                </span>
                                                                <c:if test="${not empty order.shopAcceptanceDeadline}">
                                                                    <span
                                                                        class="countdown-timer inline-flex items-center gap-1 text-[10px] font-bold text-orange-500"
                                                                        data-deadline="${order.shopAcceptanceDeadline}">
                                                                        <i
                                                                            class="fa-solid fa-hourglass-half text-[8px]"></i>
                                                                        <span class="countdown-display">--:--</span>
                                                                    </span>
                                                                </c:if>
                                                            </div>
                                                        </c:when>
                                                        <c:when test="${order.status == 'PREPARING'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-purple-50 text-purple-700 border border-purple-200">
                                                                <i class="fa-solid fa-box text-[8px]"></i>Chuẩn bị
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'APPROVED'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
                                                                <i class="fa-solid fa-check-double text-[8px]"></i>Đã
                                                                Duyệt
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'DISPATCHED'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-pink-50 text-pink-700 border border-pink-200">
                                                                <i class="fa-solid fa-truck text-[8px]"></i>Đang giao
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'DELIVERED'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                                                                <i class="fa-solid fa-circle-check text-[8px]"></i>Đã
                                                                giao
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'CANCELLED'}">
                                                            <span
                                                                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-bold bg-red-50 text-red-700 border border-red-200">
                                                                <i class="fa-solid fa-ban text-[8px]"></i>Đã hủy
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span
                                                                class="inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-bold bg-gray-50 text-gray-600 border border-gray-200">${order.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="py-3.5 px-5 text-right">
                                                    <div class="inline-flex items-center gap-2">
                                                        <!-- Duyệt đơn: chỉ khi CONFIRMED -->
                                                        <c:if test="${order.status == 'CONFIRMED'}">
                                                            <form
                                                                action="${pageContext.request.contextPath}/shop/orders"
                                                                method="POST" class="inline">
                                                                <input type="hidden" name="_csrf"
                                                                    value="${sessionScope._csrfToken}">
                                                                <input type="hidden" name="action" value="approve">
                                                                <input type="hidden" name="orderId"
                                                                    value="${order.orderId}">
                                                                <input type="hidden" name="currentStatus"
                                                                    value="${status}">
                                                                <button type="submit"
                                                                    class="px-3 py-1.5 rounded-lg bg-primary text-white text-[11px] font-bold hover:bg-primary-hover transition-colors shadow-sm">
                                                                    <i class="fa-solid fa-check mr-1"></i>Duyệt
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                        <!-- Bàn giao: khi APPROVED hoặc PREPARING -->
                                                        <c:if
                                                            test="${order.status == 'APPROVED' || order.status == 'PREPARING'}">
                                                            <button type="button"
                                                                onclick="openDispatchModal('${order.orderId}')"
                                                                class="px-3 py-1.5 rounded-lg bg-emerald-600 text-white text-[11px] font-bold hover:bg-emerald-700 transition-colors shadow-sm">
                                                                <i class="fa-solid fa-truck mr-1"></i>Giao hàng
                                                            </button>
                                                        </c:if>
                                                        <!-- Hủy đơn: khi chưa DELIVERED/CANCELLED/DISPATCHED -->
                                                        <c:if
                                                            test="${order.status != 'DELIVERED' && order.status != 'CANCELLED' && order.status != 'DISPATCHED'}">
                                                            <button type="button"
                                                                onclick="openRejectModal('${order.orderId}')"
                                                                class="px-3 py-1.5 rounded-lg bg-red-50 text-red-600 border border-red-200 text-[11px] font-bold hover:bg-red-600 hover:text-white transition-colors">
                                                                <i class="fa-solid fa-ban mr-1"></i>Hủy
                                                            </button>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty orders}">
                                            <tr>
                                                <td colspan="7" class="py-16 text-center text-txt-3">
                                                    <i
                                                        class="fa-solid fa-box-open text-4xl mb-3 text-gray-200 block"></i>
                                                    <p class="text-sm font-medium">Không có đơn hàng nào!</p>
                                                    <p class="text-xs mt-1">Chưa có đơn hàng phù hợp với bộ lọc đang
                                                        chọn.</p>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                            <c:if test="${totalPages > 1}">
                                <div class="px-6 py-4 border-t border-[#e2ece7] flex items-center justify-between bg-slate-50/50">
                                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="${pageContext.request.contextPath}/shop/orders?status=${fn:escapeXml(status)}" />
                                </div>
                            </c:if>
                        </div>
                    </main>
                </div>

                <!-- Modal: Hủy Đơn -->
                <div id="rejectModal" class="modal-overlay">
                    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 p-6">
                        <div class="flex items-center justify-between mb-5">
                            <h2 class="text-base font-bold text-txt flex items-center gap-2">
                                <span
                                    class="w-8 h-8 rounded-xl bg-red-50 text-red-600 flex items-center justify-center">
                                    <i class="fa-solid fa-ban text-sm"></i>
                                </span>
                                Lý do hủy đơn hàng
                            </h2>
                            <button onclick="closeModal('rejectModal')"
                                class="w-8 h-8 rounded-xl bg-gray-100 text-gray-500 hover:bg-gray-200 flex items-center justify-center transition-colors">
                                <i class="fa-solid fa-xmark"></i>
                            </button>
                        </div>
                        <form action="${pageContext.request.contextPath}/shop/orders" method="POST">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="reject">
                            <input type="hidden" name="orderId" id="rejectOrderId">
                            <input type="hidden" name="currentStatus" id="rejectCurrentStatus" value="${status}">
                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2">Vui lòng cho biết lý do hủy đơn
                                    này:</label>
                                <textarea name="reason" rows="4" required placeholder="Hết hàng, khách đổi ý..."
                                    class="w-full px-4 py-3 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 resize-none"></textarea>
                            </div>
                            <div class="flex gap-3 justify-end">
                                <button type="button" onclick="closeModal('rejectModal')"
                                    class="px-5 py-2.5 rounded-xl border border-border text-xs font-bold text-txt-2 hover:bg-gray-50 transition-colors">
                                    Đóng
                                </button>
                                <button type="submit"
                                    class="px-5 py-2.5 rounded-xl bg-red-600 text-white text-xs font-bold hover:bg-red-700 transition-colors shadow-sm">
                                    <i class="fa-solid fa-ban mr-1"></i>Xác nhận Hủy
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Modal: Bàn giao vận chuyển -->
                <div id="dispatchModal" class="modal-overlay">
                    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 p-6">
                        <div class="flex items-center justify-between mb-5">
                            <h2 class="text-base font-bold text-txt flex items-center gap-2">
                                <span
                                    class="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                                    <i class="fa-solid fa-truck text-sm"></i>
                                </span>
                                Bàn giao vận chuyển
                            </h2>
                            <button onclick="closeModal('dispatchModal')"
                                class="w-8 h-8 rounded-xl bg-gray-100 text-gray-500 hover:bg-gray-200 flex items-center justify-center transition-colors">
                                <i class="fa-solid fa-xmark"></i>
                            </button>
                        </div>
                        <form action="${pageContext.request.contextPath}/shop/orders" method="POST">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="dispatch">
                            <input type="hidden" name="orderId" id="dispatchOrderId">
                            <input type="hidden" name="currentStatus" id="dispatchCurrentStatus" value="${status}">
                            <div class="mb-5">
                                <label class="block text-xs font-bold text-txt-2 mb-2">Dự kiến thời gian giao đến khách
                                    <span class="font-normal text-txt-3">(Tùy chọn)</span></label>
                                <input type="datetime-local" name="estimatedDeliveryTime"
                                    id="dispatchEstimatedDeliveryTime"
                                    class="w-full px-4 py-3 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10">
                                <p class="text-[10px] text-txt-3 mt-1">Để trống nếu chưa xác định thời gian giao hàng cụ
                                    thể.</p>
                            </div>
                            <div class="flex gap-3 justify-end">
                                <button type="button" onclick="closeModal('dispatchModal')"
                                    class="px-5 py-2.5 rounded-xl border border-border text-xs font-bold text-txt-2 hover:bg-gray-50 transition-colors">
                                    Đóng
                                </button>
                                <button type="submit" id="dispatchSubmitBtn"
                                    class="px-5 py-2.5 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-hover transition-colors shadow-sm">
                                    <i class="fa-solid fa-truck mr-1"></i>Bàn giao
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <script>
                    function formatDateTimeLocal(date) {
                        const pad = (v) => String(v).padStart(2, '0');
                        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
                            + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
                    }

                    function openRejectModal(orderId) {
                        document.getElementById('rejectOrderId').value = orderId;
                        document.getElementById('rejectModal').classList.add('active');
                    }

                    function openDispatchModal(orderId) {
                        const estimatedInput = document.getElementById('dispatchEstimatedDeliveryTime');
                        if (estimatedInput) {
                            const now = new Date();
                            estimatedInput.min = formatDateTimeLocal(now);
                            // Gợi ý mặc định: 2 giờ sau
                            const suggested = new Date(now.getTime() + 2 * 60 * 60 * 1000);
                            estimatedInput.value = formatDateTimeLocal(suggested);
                        }
                        document.getElementById('dispatchOrderId').value = orderId;
                        document.getElementById('dispatchModal').classList.add('active');
                    }

                    function closeModal(id) {
                        document.getElementById(id).classList.remove('active');
                    }

                    // Đóng modal khi click backdrop
                    document.querySelectorAll('.modal-overlay').forEach(el => {
                        el.addEventListener('click', function (e) {
                            if (e.target === this) closeModal(this.id);
                        });
                    });

                    // Disable submit button sau khi click để tránh double-submit
                    document.getElementById('dispatchModal').querySelector('form').addEventListener('submit', function () {
                        const btn = document.getElementById('dispatchSubmitBtn');
                        btn.disabled = true;
                        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i>Đang xử lý...';
                    });

                    // Countdown timers for CONFIRMED orders with acceptance deadline
                    (function initCountdowns() {
                        document.querySelectorAll('.countdown-timer').forEach(function (el) {
                            var raw = el.dataset.deadline;
                            if (!raw) return;
                            // Parse LocalDateTime.toString() as local time (format: "yyyy-MM-ddTHH:mm:ss...")
                            var parts = raw.split(/[-T:.]/);
                            var deadline = new Date(
                                parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]),
                                parseInt(parts[3] || 0), parseInt(parts[4] || 0), parseInt(parts[5] || 0)
                            );
                            if (isNaN(deadline.getTime())) return;
                            var display = el.querySelector('.countdown-display');

                            function tick() {
                                var remaining = deadline.getTime() - Date.now();
                                if (remaining <= 0) {
                                    display.textContent = 'QUÁ HẠN';
                                    el.className = 'countdown-timer inline-flex items-center gap-1 text-[10px] font-bold text-red-600 animate-pulse';
                                    return;
                                }
                                var h = Math.floor(remaining / 3600000);
                                var m = Math.floor((remaining % 3600000) / 60000);
                                var s = Math.floor((remaining % 60000) / 1000);
                                display.textContent = h > 0
                                    ? h + 'g ' + String(m).padStart(2, '0') + 'p'
                                    : String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
                                if (remaining < 10 * 60 * 1000) {
                                    el.className = 'countdown-timer inline-flex items-center gap-1 text-[10px] font-bold text-red-600 animate-pulse';
                                }
                                setTimeout(tick, 1000);
                            }
                            tick();
                        });
                    })();
                </script>
            </body>

            </html>