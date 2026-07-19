<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<%--
  Chi tiết đơn hàng dành riêng cho Customer truy cập từ Profile.
  Đường dẫn: /profile/order-detail?orderId=X
  - Breadcrumb quay về /profile?tab=orders (thay vì /orders)
  - Invoice download link trỏ về /profile/order-detail?action=invoice
  - Tất cả nội dung giống order-detail.jsp nhưng không dùng trang /orders
--%>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Chi tiết đơn hàng #${order.orderId}" />
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
<link href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css" rel="stylesheet">

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
    .step-active { background: #4d661c !important; border-color: #4d661c !important; color: #fff !important; }
    .step-done   { background: #d9f99d !important; border-color: #4d661c !important; color: #4d661c !important; }
    @media print {
        nav, header, footer, .no-print { display: none !important; }
        .print-invoice { page-break-inside: avoid; }
    }
</style>

<main class="max-w-7xl mx-auto px-4 md:px-8 py-10 font-body-md text-on-background">



    <!-- Top Navigation: back to profile orders tab -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-8 pb-4 border-b border-surface-container-high gap-4">
        <div>
            <a href="${pageContext.request.contextPath}/profile?tab=orders"
               class="text-on-surface-variant hover:text-primary transition-all text-sm font-semibold flex items-center gap-1 mb-2">
                <span class="material-symbols-outlined text-base">arrow_back</span> Quay lại đơn hàng của tôi
            </a>
            <h1 class="font-display-lg text-2xl md:text-3xl text-inverse-surface font-bold">Chi tiết đơn hàng #${order.orderId}</h1>
            <p class="text-on-surface-variant text-sm mt-1">Mã tham chiếu thanh toán &amp; giao vận</p>
        </div>
        <div class="flex flex-wrap gap-2">
            <c:if test="${order.status == 'DELIVERED'}">
                <a href="${pageContext.request.contextPath}/profile/order-detail?action=invoice&orderId=${order.orderId}"
                   target="_blank"
                   class="bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary px-5 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-sm flex items-center gap-2">
                    <span class="material-symbols-outlined text-lg">description</span> Tải hóa đơn PDF
                </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/profile?tab=orders"
               class="bg-white border border-outline-variant/30 text-on-surface-variant hover:bg-surface-variant/30 px-5 py-2.5 rounded-xl font-semibold text-sm transition-all flex items-center gap-2">
                <span class="material-symbols-outlined text-lg">list_alt</span> Tất cả đơn hàng
            </a>
        </div>
    </div>

    <!-- Status Stepper -->
    <div class="premium-glass-card rounded-[1.5rem] p-6 mb-8">
        <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">analytics</span> Trạng thái hành trình đơn hàng
        </h3>
        <c:if test="${order.status == 'CANCELLED'}">
            <div class="flex items-center gap-3 p-4 bg-red-50 border border-red-200 rounded-xl">
                <span class="material-symbols-outlined text-red-500 text-2xl">cancel</span>
                <div>
                    <p class="font-bold text-red-700">Đơn hàng đã bị hủy</p>
                    <c:if test="${not empty order.cancellationReason}">
                        <p class="text-sm text-red-600">Lý do: <c:out value="${order.cancellationReason}"/></p>
                    </c:if>
                </div>
            </div>
        </c:if>
        <c:if test="${order.status != 'CANCELLED'}">
            <%-- Compute activeIdx once using c:choose (safe, no EL nested ternary) --%>
            <c:choose>
                <c:when test="${order.status == 'PENDING_PAYMENT'}"><c:set var="activeIdx" value="0"/></c:when>
                <c:when test="${order.status == 'CONFIRMED' or order.status == 'APPROVED'}"><c:set var="activeIdx" value="1"/></c:when>
                <c:when test="${order.status == 'PREPARING'}"><c:set var="activeIdx" value="2"/></c:when>
                <c:when test="${order.status == 'DISPATCHED'}"><c:set var="activeIdx" value="3"/></c:when>
                <c:when test="${order.status == 'DELIVERED'}"><c:set var="activeIdx" value="4"/></c:when>
                <c:otherwise><c:set var="activeIdx" value="-1"/></c:otherwise>
            </c:choose>

            <div class="flex items-start justify-between gap-2 relative">
                <div class="absolute top-5 left-[10%] right-[10%] h-0.5 bg-gray-200 z-0"></div>

                <%-- Step 1: Chờ thanh toán --%>
                <div class="flex flex-col items-center gap-2 z-10 flex-1">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center font-bold text-sm transition-all
                        <c:choose><c:when test="${activeIdx >= 0}">bg-primary border-primary text-white</c:when><c:otherwise>bg-white border-gray-300 text-gray-400</c:otherwise></c:choose>">
                        <c:choose><c:when test="${activeIdx > 0}"><span class="material-symbols-outlined text-base">check</span></c:when><c:otherwise>1</c:otherwise></c:choose>
                    </div>
                    <span class="text-[10px] font-semibold text-center leading-tight <c:choose><c:when test="${activeIdx >= 0}">text-primary</c:when><c:otherwise>text-gray-400</c:otherwise></c:choose>">Chờ thanh toán</span>
                </div>

                <%-- Step 2: Đã xác nhận --%>
                <div class="flex flex-col items-center gap-2 z-10 flex-1">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center font-bold text-sm transition-all
                        <c:choose><c:when test="${activeIdx >= 1}">bg-primary border-primary text-white</c:when><c:otherwise>bg-white border-gray-300 text-gray-400</c:otherwise></c:choose>">
                        <c:choose><c:when test="${activeIdx > 1}"><span class="material-symbols-outlined text-base">check</span></c:when><c:otherwise>2</c:otherwise></c:choose>
                    </div>
                    <span class="text-[10px] font-semibold text-center leading-tight <c:choose><c:when test="${activeIdx >= 1}">text-primary</c:when><c:otherwise>text-gray-400</c:otherwise></c:choose>">Đã xác nhận</span>
                </div>

                <%-- Step 3: Chuẩn bị --%>
                <div class="flex flex-col items-center gap-2 z-10 flex-1">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center font-bold text-sm transition-all
                        <c:choose><c:when test="${activeIdx >= 2}">bg-primary border-primary text-white</c:when><c:otherwise>bg-white border-gray-300 text-gray-400</c:otherwise></c:choose>">
                        <c:choose><c:when test="${activeIdx > 2}"><span class="material-symbols-outlined text-base">check</span></c:when><c:otherwise>3</c:otherwise></c:choose>
                    </div>
                    <span class="text-[10px] font-semibold text-center leading-tight <c:choose><c:when test="${activeIdx >= 2}">text-primary</c:when><c:otherwise>text-gray-400</c:otherwise></c:choose>">Chuẩn bị</span>
                </div>

                <%-- Step 4: Đang giao --%>
                <div class="flex flex-col items-center gap-2 z-10 flex-1">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center font-bold text-sm transition-all
                        <c:choose><c:when test="${activeIdx >= 3}">bg-primary border-primary text-white</c:when><c:otherwise>bg-white border-gray-300 text-gray-400</c:otherwise></c:choose>">
                        <c:choose><c:when test="${activeIdx > 3}"><span class="material-symbols-outlined text-base">check</span></c:when><c:otherwise>4</c:otherwise></c:choose>
                    </div>
                    <span class="text-[10px] font-semibold text-center leading-tight <c:choose><c:when test="${activeIdx >= 3}">text-primary</c:when><c:otherwise>text-gray-400</c:otherwise></c:choose>">Đang giao</span>
                </div>

                <%-- Step 5: Đã giao --%>
                <div class="flex flex-col items-center gap-2 z-10 flex-1">
                    <div class="w-10 h-10 rounded-full border-2 flex items-center justify-center font-bold text-sm transition-all
                        <c:choose><c:when test="${activeIdx >= 4}">bg-primary border-primary text-white</c:when><c:otherwise>bg-white border-gray-300 text-gray-400</c:otherwise></c:choose>">
                        <c:choose><c:when test="${activeIdx >= 4}"><span class="material-symbols-outlined text-base">check</span></c:when><c:otherwise>5</c:otherwise></c:choose>
                    </div>
                    <span class="text-[10px] font-semibold text-center leading-tight <c:choose><c:when test="${activeIdx >= 4}">text-primary</c:when><c:otherwise>text-gray-400</c:otherwise></c:choose>">Đã giao</span>
                </div>
            </div>
        </c:if>
    </div>

    <!-- Order Info Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">

        <!-- Delivery Info -->
        <div class="premium-glass-card rounded-[1.5rem] p-6 lg:col-span-2">
            <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between mb-4">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary text-lg">local_shipping</span> Thông tin giao hàng
                </h3>
                <span class="text-xs font-bold uppercase tracking-[0.18em] text-on-surface-variant">Thông tin nhận hàng</span>
            </div>
            <div class="grid gap-3 text-sm text-on-surface-variant">
                <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                    <div class="mb-2">
                        <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Cửa hàng</span>
                    </div>
                    <p class="font-semibold text-on-surface">
                        <c:choose>
                            <c:when test="${not empty shopName}">
                                <c:choose>
                                    <c:when test="${order.ownerId > 0}">
                                        <a href="${pageContext.request.contextPath}/shop-view?id=${order.ownerId}&idType=owner" class="hover:underline text-primary transition-all">
                                            <c:out value="${shopName}"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${shopName}"/>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>Đơn tổng hợp nhiều cửa hàng</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                    <div class="mb-2">
                        <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Địa chỉ giao hàng</span>
                    </div>
                    <p class="font-semibold text-on-surface"><c:out value="${order.deliveryAddress}"/></p>
                </div>
                <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                    <div class="mb-2">
                        <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Phương thức thanh toán</span>
                    </div>
                    <p class="font-semibold text-on-surface">
                        <c:choose>
                            <c:when test="${order.paymentMethod == 'COD'}">COD (Thanh toán khi nhận hàng)</c:when>
                            <c:otherwise>Chuyển khoản ngân hàng</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <c:if test="${not empty order.notes}">
                    <div class="rounded-2xl border border-secondary-container/70 bg-secondary-container/20 p-4 shadow-sm">
                        <div class="mb-2">
                            <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Ghi chú vận chuyển</span>
                        </div>
                        <p class="font-medium text-on-surface italic"><c:out value="${order.notes}"/></p>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- Invoice Summary -->
        <div class="premium-glass-card rounded-[1.5rem] p-6">
            <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4 flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-lg">receipt_long</span> Hóa đơn
            </h3>
            <div class="space-y-3 text-sm">
                <div class="flex justify-between text-on-surface-variant">
                    <span>Phí vận chuyển</span>
                    <span><fmt:formatNumber value="${order.deliveryFee}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></span>
                </div>
                <c:if test="${order.discountAmount > 0}">
                    <div class="flex justify-between text-emerald-700">
                        <span>Giảm giá</span>
                        <span>- <fmt:formatNumber value="${order.discountAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></span>
                    </div>
                </c:if>
                <div class="flex justify-between font-bold text-on-surface border-t border-gray-200 pt-3 text-base">
                    <span>Tổng thanh toán</span>
                    <span class="text-primary text-lg"><fmt:formatNumber value="${order.finalAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></span>
                </div>

                <!-- Payment Transaction -->
                <c:if test="${not empty paymentTx}">
                    <div class="mt-4 p-3 rounded-xl bg-surface-container-low border border-surface-container-high text-xs space-y-1">
                        <p class="font-bold text-on-surface mb-2 flex items-center gap-1">
                            <span class="material-symbols-outlined text-sm text-primary">verified</span>
                            <c:choose>
                                <c:when test="${order.orderType == 'PARENT'}">Giao dịch thanh toán của đơn cha</c:when>
                                <c:otherwise>Giao dịch thanh toán</c:otherwise>
                            </c:choose>
                        </p>
                        <div class="flex justify-between"><span class="text-outline">Trạng thái</span>
                            <c:choose>
                                <c:when test="${paymentTx.status == 'completed'}">
                                    <span class="font-semibold text-emerald-700">Thành công</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="font-semibold text-amber-700"><c:out value="${paymentTx.status}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${not empty paymentTx.sepayTransactionId}">
                            <div class="flex justify-between"><span class="text-outline">Mã GD</span>
                                <span class="font-mono font-semibold text-on-surface"><c:out value="${paymentTx.sepayTransactionId}"/></span>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Products Ordered -->
    <c:choose>
        <c:when test="${order.orderType == 'PARENT'}">
            <!-- Multi-shop parent order: show children -->
            <c:if test="${not empty childOrders}">
                <div class="space-y-6">
                    <h3 class="font-headline-md text-lg text-inverse-surface font-bold flex items-center gap-2">
                        <span class="material-symbols-outlined text-primary">store</span> Đơn hàng theo cửa hàng
                    </h3>
                    <c:forEach var="child" items="${childOrders}" varStatus="idx">
                        <div class="premium-glass-card rounded-[1.5rem] overflow-hidden">
                            <!-- Child order header -->
                            <div class="px-6 py-4 bg-surface-container-low border-b border-surface-container-high flex flex-wrap items-center justify-between gap-3">
                                <div class="flex items-center gap-3">
                                    <span class="material-symbols-outlined text-primary">storefront</span>
                                    <div>
                                        <p class="font-bold text-on-surface">
                                            <c:choose>
                                                <c:when test="${child.ownerId > 0}">
                                                    <a href="${pageContext.request.contextPath}/shop-view?id=${child.ownerId}&idType=owner" class="hover:underline text-primary transition-all">
                                                        <c:out value="${shopNamesMap[child.orderId]}"/>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${shopNamesMap[child.orderId]}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <p class="text-xs text-on-surface-variant">Đơn con #<c:out value="${child.orderId}"/></p>
                                    </div>
                                </div>
                                    <c:choose>
                                        <c:when test="${child.status == 'DELIVERED'}"><c:set var="badgeCls" value="bg-emerald-100 text-emerald-800"/></c:when>
                                        <c:when test="${child.status == 'DISPATCHED'}"><c:set var="badgeCls" value="bg-sky-100 text-sky-800"/></c:when>
                                        <c:when test="${child.status == 'CONFIRMED'}"><c:set var="badgeCls" value="bg-amber-100 text-amber-800"/></c:when>
                                        <c:when test="${child.status == 'CANCELLED'}"><c:set var="badgeCls" value="bg-red-100 text-red-800"/></c:when>
                                        <c:otherwise><c:set var="badgeCls" value="bg-amber-100 text-amber-800"/></c:otherwise>
                                    </c:choose>
                                <span class="px-3 py-1 rounded-full text-xs font-bold ${badgeCls}">
                                    <c:choose>
                                        <c:when test="${child.status == 'PENDING_PAYMENT'}">Chờ thanh toán</c:when>
                                        <c:when test="${child.status == 'CONFIRMED'}">Đã xác nhận</c:when>
                                        <c:when test="${child.status == 'PREPARING'}">Đang chuẩn bị</c:when>
                                        <c:when test="${child.status == 'DISPATCHED'}">Đang giao</c:when>
                                        <c:when test="${child.status == 'DELIVERED'}">Đã giao</c:when>
                                        <c:when test="${child.status == 'CANCELLED'}">Đã hủy</c:when>
                                        <c:otherwise><c:out value="${child.status}"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <!-- Child items -->
                            <div class="p-6 space-y-4">
                                <c:forEach var="item" items="${childOrderItemsMap[child.orderId]}">
                                    <%@ include file="/WEB-INF/jsp/customer/common/order-item-card.jspf" %>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <!-- Single shop order: show items directly -->
            <div class="premium-glass-card rounded-[1.5rem] overflow-hidden">
                <div class="px-6 py-4 bg-surface-container-low border-b border-surface-container-high">
                    <h3 class="font-headline-md text-base text-inverse-surface font-bold flex items-center gap-2">
                        <span class="material-symbols-outlined text-primary">shopping_basket</span>
                        Sản phẩm đã đặt
                        <c:if test="${not empty shopName}">
                            <span class="text-sm text-on-surface-variant font-normal">—
                                <c:choose>
                                    <c:when test="${order.ownerId > 0}">
                                        <a href="${pageContext.request.contextPath}/shop-view?id=${order.ownerId}&idType=owner" class="hover:underline text-primary transition-all">
                                            <c:out value="${shopName}"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${shopName}"/>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </c:if>
                    </h3>
                </div>
                <div class="p-6 space-y-4">
                    <c:forEach var="item" items="${orderItems}">
                        <%@ include file="/WEB-INF/jsp/customer/common/order-item-card.jspf" %>
                    </c:forEach>
                </div>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- Bottom Actions -->
    <div class="mt-8 flex flex-wrap gap-3 justify-between items-center">
        <a href="${pageContext.request.contextPath}/profile?tab=orders"
           class="flex items-center gap-2 px-5 py-2.5 bg-white border border-outline-variant/30 text-on-surface-variant hover:bg-surface-variant/30 rounded-xl font-semibold text-sm transition-all">
            <span class="material-symbols-outlined text-base">arrow_back</span> Đơn hàng của tôi
        </a>
        <c:if test="${order.status == 'DELIVERED'}">
            <div class="flex gap-3">
                <a href="${pageContext.request.contextPath}/reviews?orderId=${order.orderId}"
                   class="flex items-center gap-2 px-5 py-2.5 bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary rounded-xl font-semibold text-sm transition-all shadow-sm">
                    <span class="material-symbols-outlined text-base">star</span> Viết đánh giá sản phẩm
                </a>
                <a href="${pageContext.request.contextPath}/profile/order-detail?action=invoice&orderId=${order.orderId}"
                   target="_blank"
                   class="flex items-center gap-2 px-5 py-2.5 bg-primary text-on-primary hover:bg-primary/90 rounded-xl font-semibold text-sm transition-all shadow-sm">
                    <span class="material-symbols-outlined text-base">print</span> In / Tải Hóa đơn PDF
                </a>
            </div>
        </c:if>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />

