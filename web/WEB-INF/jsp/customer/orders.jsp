<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Lịch sử đơn hàng" />
</jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet">

<script id="tailwind-config">
    tailwind.config = {
        darkMode: "class",
        theme: {
            extend: {
                "colors": {
                    "secondary-fixed": "#b4f0c9",
                    "primary-fixed": "#ceee93",
                    "primary": "#4d661c",
                    "primary-container": "#d9f99d",
                    "surface": "#eaffea",
                    "on-secondary-container": "#386f50",
                    "on-error-container": "#93000a",
                    "outline-variant": "#c5c8b7",
                    "inverse-surface": "#00391a",
                    "outline": "#75796a",
                    "on-primary": "#ffffff",
                    "surface-variant": "#b1f2be",
                    "surface-container": "#bcfdc9",
                    "on-surface-variant": "#44483b",
                    "on-secondary-fixed": "#002111",
                    "on-error": "#ffffff",
                    "primary-fixed-dim": "#b3d17a",
                    "inverse-primary": "#b3d17a",
                    "tertiary-container": "#d5f5e0",
                    "on-primary-fixed-variant": "#364e03",
                    "secondary-container": "#b4f0c9",
                    "on-background": "#00210d",
                    "inverse-on-surface": "#c3ffce",
                    "tertiary": "#486554",
                    "surface-container-high": "#b7f7c3",
                    "on-tertiary-fixed": "#042014",
                    "error-container": "#ffdad6",
                    "error": "#ba1a1a",
                    "tertiary-fixed": "#caead6",
                    "secondary-fixed-dim": "#99d4ae",
                    "on-primary-container": "#597428",
                    "on-surface": "#00210d",
                    "on-secondary-fixed-variant": "#175034",
                    "on-tertiary-fixed-variant": "#314d3e",
                    "surface-tint": "#4d661c",
                    "surface-container-highest": "#b1f2be",
                    "on-tertiary-container": "#557161",
                    "surface-dim": "#a9e9b6",
                    "surface-container-lowest": "#ffffff",
                    "surface-container-low": "#d1ffd8",
                    "on-secondary": "#ffffff",
                    "background": "#eaffea",
                    "on-tertiary": "#ffffff",
                    "tertiary-fixed-dim": "#afceba",
                    "secondary": "#31694b",
                    "surface-bright": "#eaffea",
                    "on-primary-fixed": "#131f00"
                },
                "fontFamily": {
                    "label-md": ["Lexend"],
                    "headline-lg-mobile": ["Lexend"],
                    "headline-md": ["Lexend"],
                    "label-sm": ["Lexend"],
                    "display-lg": ["Lexend"],
                    "body-lg": ["Lexend"],
                    "headline-lg": ["Lexend"],
                    "body-md": ["Lexend"]
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
        border: 1px solid rgba(255, 255, 255, 0.5);
        box-shadow: 0 10px 30px -10px rgba(20, 83, 45, 0.05);
    }
</style>

<main class="max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-on-background">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-baseline md:justify-between mb-lg border-b border-surface-container-high pb-4 gap-4">
        <h1 class="font-display-lg text-3xl md:text-4xl text-inverse-surface font-bold tracking-tight">Lịch sử đơn hàng của bạn</h1>
        <p class="text-on-surface-variant font-medium">Theo dõi và quản lý các đơn đặt hàng trái cây sạch</p>
    </div>

    <!-- Alert Flash message -->
    <c:if test="${not empty sessionScope.flashMsg}">
        <div class="mb-6 p-4 rounded-xl flex items-center justify-between shadow-sm border ${sessionScope.flashType == 'success' ? 'bg-[#dcfce7] border-[#bbf7d0] text-emerald-800' : 'bg-error-container border-[#ffdad6] text-[#93000a]'}">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined">${sessionScope.flashType == 'success' ? 'check_circle' : 'error'}</span>
                <span class="font-semibold">${sessionScope.flashMsg}</span>
            </div>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <!-- Filter Tabs (DEL-03 và timeline status) -->
    <div class="flex flex-wrap gap-2 mb-8 bg-white/40 p-2 rounded-2xl border border-white/30 backdrop-blur-[8px]">
        <a href="${pageContext.request.contextPath}/orders" class="px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 ${empty selectedStatus ? 'bg-primary text-on-primary shadow-sm' : 'text-on-surface-variant hover:bg-surface-variant/30'}">
            <span class="material-symbols-outlined text-lg">all_inbox</span> Tất cả
        </a>
        <a href="${pageContext.request.contextPath}/orders?status=PENDING_PAYMENT" class="px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 ${selectedStatus == 'PENDING_PAYMENT' ? 'bg-primary text-on-primary shadow-sm' : 'text-on-surface-variant hover:bg-surface-variant/30'}">
            <span class="material-symbols-outlined text-lg">payments</span> Chờ thanh toán
        </a>
        <a href="${pageContext.request.contextPath}/orders?status=DISPATCHED" class="px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 ${selectedStatus == 'DISPATCHED' ? 'bg-primary text-on-primary shadow-sm' : 'text-on-surface-variant hover:bg-surface-variant/30'}">
            <span class="material-symbols-outlined text-lg">local_shipping</span> Đang giao hàng
        </a>
        <a href="${pageContext.request.contextPath}/orders?status=DELIVERED" class="px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 ${selectedStatus == 'DELIVERED' ? 'bg-primary text-on-primary shadow-sm' : 'text-on-surface-variant hover:bg-surface-variant/30'}">
            <span class="material-symbols-outlined text-lg">task_alt</span> Đã giao thành công
        </a>
        <a href="${pageContext.request.contextPath}/orders?status=CANCELLED" class="px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 ${selectedStatus == 'CANCELLED' ? 'bg-primary text-on-primary shadow-sm' : 'text-on-surface-variant hover:bg-surface-variant/30'}">
            <span class="material-symbols-outlined text-lg">cancel</span> Đã hủy
        </a>
    </div>

    <!-- Orders List -->
    <div class="flex flex-col gap-6">
        <c:forEach var="order" items="${orders}">
            <div class="premium-glass-card rounded-[1.5rem] p-6 flex flex-col gap-6">
                
                <!-- Order Card Top Bar -->
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-outline-variant/30 pb-4 gap-3">
                    <div class="flex items-center gap-3">
                        <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" class="font-headline-md text-xl text-primary font-bold hover:underline">
                            Đơn hàng #${order.orderId}
                        </a>
                        <span class="text-on-surface-variant text-sm font-medium">Đặt ngày: ${order.createdAt}</span>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${order.status == 'PENDING_PAYMENT'}">
                                <span class="bg-amber-100 text-amber-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Chờ thanh toán</span>
                            </c:when>
                            <c:when test="${order.status == 'CONFIRMED'}">
                                <span class="bg-blue-100 text-blue-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Đã xác nhận</span>
                            </c:when>
                            <c:when test="${order.status == 'PREPARING'}">
                                <span class="bg-indigo-100 text-indigo-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Shop chuẩn bị hàng</span>
                            </c:when>
                            <c:when test="${order.status == 'APPROVED'}">
                                <span class="bg-teal-100 text-teal-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Đã duyệt (Chờ giao)</span>
                            </c:when>
                            <c:when test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
                                <span class="bg-sky-100 text-sky-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Đang giao hàng</span>
                            </c:when>
                            <c:when test="${order.status == 'DELIVERED'}">
                                <span class="bg-emerald-100 text-emerald-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Giao thành công</span>
                            </c:when>
                            <c:when test="${order.status == 'CANCELLED'}">
                                <span class="bg-rose-100 text-rose-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">Đã hủy</span>
                            </c:when>
                            <c:otherwise>
                                <span class="bg-slate-100 text-slate-800 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider">${order.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Order Content Summary -->
                <div class="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
                    <div class="md:col-span-8 flex flex-col gap-2 border-r border-outline-variant/30 pr-0 md:pr-6">
                        <div class="flex items-start gap-2">
                            <span class="material-symbols-outlined text-on-surface-variant mt-0.5">location_on</span>
                            <p class="text-on-surface-variant"><strong class="text-inverse-surface">Địa chỉ giao hàng:</strong> ${order.deliveryAddress}</p>
                        </div>
                        <div class="flex items-start gap-2">
                            <span class="material-symbols-outlined text-on-surface-variant mt-0.5">notes</span>
                            <p class="text-on-surface-variant"><strong class="text-inverse-surface">Ghi chú:</strong> ${order.notes != null ? order.notes : 'Không có'}</p>
                        </div>
                        <div class="flex items-start gap-2">
                            <span class="material-symbols-outlined text-on-surface-variant mt-0.5">shopping_bag</span>
                            <p class="text-on-surface-variant"><strong class="text-inverse-surface">Tổng cộng thanh toán:</strong> <span class="text-[#d32f2f] font-bold text-lg"><ft:currency value="${order.finalAmount}" /></span></p>
                        </div>
                    </div>
                    
                    <!-- Action buttons -->
                    <div class="md:col-span-4 flex flex-col gap-3 items-stretch justify-center px-2">
                        <!-- Nút hủy đơn hàng -->
                        <c:if test="${order.status == 'PENDING_PAYMENT' || order.status == 'CONFIRMED'}">
                            <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-full">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                <input type="hidden" name="action" value="cancel">
                                <input type="hidden" name="orderId" value="${order.orderId}">
                                <input type="hidden" name="reason" value="Khách hàng tự hủy đơn từ lịch sử đơn hàng">
                                <button type="submit" class="w-full bg-[#fef2f2] text-error hover:bg-[#ba1a1a] hover:text-white py-3 rounded-xl transition-all font-semibold border border-error/20 flex items-center justify-center gap-2 active:scale-95 transform" onclick="return confirm('Bạn có chắc muốn hủy đơn hàng #${order.orderId} không?');">
                                    <span class="material-symbols-outlined text-lg">cancel</span> Hủy đơn hàng
                                </button>
                            </form>
                        </c:if>

                        <!-- Nút đã nhận được hàng -->
                        <c:if test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
                            <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-full">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                <input type="hidden" name="action" value="confirmDelivery">
                                <input type="hidden" name="orderId" value="${order.orderId}">
                                <button type="submit" class="w-full bg-primary text-on-primary hover:bg-inverse-surface py-3 rounded-xl transition-all font-semibold flex items-center justify-center gap-2 shadow-md active:scale-95 transform">
                                    <span class="material-symbols-outlined text-lg">check_circle</span> Đã nhận được hàng
                                </button>
                            </form>
                        </c:if>

                        <!-- Đã nhận hàng thành công -->
                        <c:if test="${order.status == 'DELIVERED'}">
                            <div class="flex flex-col gap-2 w-full">
                                <a href="${pageContext.request.contextPath}/reviews?orderId=${order.orderId}" class="w-full bg-[#f5fdf9] text-primary hover:bg-primary hover:text-on-primary py-2.5 rounded-xl transition-all font-semibold border border-primary/20 text-center flex items-center justify-center gap-2 text-sm">
                                    <span class="material-symbols-outlined text-base">star</span> Viết đánh giá chất lượng
                                </a>
                                <a href="${pageContext.request.contextPath}/returns?orderId=${order.orderId}" class="w-full bg-[#fff5f5] text-error hover:bg-[#ba1a1a] hover:text-white py-2.5 rounded-xl transition-all font-semibold border border-error/20 text-center flex items-center justify-center gap-2 text-sm">
                                    <span class="material-symbols-outlined text-base">rotate_left</span> Yêu cầu Đổi / Trả hàng
                                </a>
                                <a href="${pageContext.request.contextPath}/orders?action=invoice&orderId=${order.orderId}" class="w-full bg-tertiary-container text-on-tertiary-container hover:bg-tertiary hover:text-on-primary py-2.5 rounded-xl transition-all font-semibold text-center flex items-center justify-center gap-2 text-sm">
                                    <span class="material-symbols-outlined text-base">description</span> Xem hóa đơn điện tử
                                </a>
                            </div>
                        </c:if>

                        <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" class="w-full bg-white hover:bg-surface-variant/30 text-on-surface-variant py-2.5 rounded-xl transition-all font-semibold border border-outline-variant/30 text-center flex items-center justify-center gap-2 text-sm">
                            <span class="material-symbols-outlined text-base">info</span> Xem chi tiết tracking
                        </a>
                    </div>
                </div>

            </div>
        </c:forEach>

        <c:if test="${empty orders}">
            <div class="text-center py-16 bg-white/50 premium-glass-card rounded-[2rem] border border-white/30">
                <span class="material-symbols-outlined text-6xl text-on-surface-variant opacity-60 mb-4">box_open</span>
                <h4 class="text-lg font-bold text-inverse-surface mb-2">Không tìm thấy đơn hàng nào</h4>
                <p class="text-on-surface-variant text-sm mb-6">Bạn chưa có đơn hàng nào hoặc không có đơn hàng nào khớp với trạng thái đã chọn.</p>
                <a href="${pageContext.request.contextPath}/products" class="inline-flex items-center gap-2 bg-primary text-on-primary px-6 py-3 rounded-xl font-semibold hover:bg-inverse-surface transition-all shadow-md active:scale-95 transform">
                    Mua sắm trái cây ngay <span class="material-symbols-outlined">arrow_forward</span>
                </a>
            </div>
        </c:if>
    </div>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
