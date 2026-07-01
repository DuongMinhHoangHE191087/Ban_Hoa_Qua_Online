<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Chi tiết đơn hàng #${order.orderId}" />
</jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
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

<main class="max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-on-background">
    


    <!-- Top Navigation and Actions -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-lg pb-4 border-b border-surface-container-high gap-4">
        <div>
            <a href="${pageContext.request.contextPath}/orders" class="text-on-surface-variant hover:text-primary transition-all text-sm font-semibold flex items-center gap-1 mb-2">
                <span class="material-symbols-outlined text-base">arrow_back</span> Quay lại danh sách đơn hàng
            </a>
            <h1 class="font-display-lg text-2xl md:text-3xl text-inverse-surface font-bold">Chi tiết đơn hàng #${order.orderId}</h1>
            <p class="text-on-surface-variant text-sm mt-1">Mã tham chiếu thanh toán & giao vận</p>
        </div>
        <div class="flex flex-wrap gap-2">
            <c:if test="${order.status == 'DELIVERED'}">
                <a href="${pageContext.request.contextPath}/orders?action=invoice&orderId=${order.orderId}" class="bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary px-5 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-sm flex items-center gap-2">
                    <span class="material-symbols-outlined text-lg">description</span> Xem Hóa Đơn Điện Tử (PDF)
                </a>
            </c:if>
        </div>
    </div>

        <!-- Timeline Progress Board -->
    <c:set var="stepNum" value="1" />
    <c:choose>
        <c:when test="${order.status == 'CANCELLED'}">
            <c:set var="stepNum" value="-1" />
        </c:when>
        <c:when test="${order.status == 'PENDING_PAYMENT'}">
            <c:set var="stepNum" value="1" />
        </c:when>
        <c:when test="${order.status == 'CONFIRMED'}">
            <c:set var="stepNum" value="2" />
        </c:when>
        <c:when test="${order.status == 'APPROVED' || order.status == 'PREPARING'}">
            <c:set var="stepNum" value="3" />
        </c:when>
        <c:when test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
            <c:set var="stepNum" value="4" />
        </c:when>
        <c:when test="${order.status == 'DELIVERED'}">
            <c:set var="stepNum" value="5" />
        </c:when>
    </c:choose>
    <c:choose>
        <c:when test="${order.status == 'CANCELLED'}">
            <c:set var="timelineHeadline" value="Đã hủy đơn hàng" />
            <c:set var="timelineTone" value="bg-error-container/50 text-[#93000a] border-error/20" />
        </c:when>
        <c:when test="${order.status == 'PENDING_PAYMENT'}">
            <c:set var="timelineHeadline" value="Chờ thanh toán QR" />
            <c:set var="timelineTone" value="bg-amber-100 text-amber-800 border-amber-200" />
        </c:when>
        <c:when test="${order.status == 'CONFIRMED'}">
            <c:set var="timelineHeadline" value="Đã xác nhận" />
            <c:set var="timelineTone" value="bg-amber-100 text-amber-800 border-amber-200" />
        </c:when>
        <c:when test="${order.status == 'APPROVED' || order.status == 'PREPARING'}">
            <c:set var="timelineHeadline" value="Đang chuẩn bị hàng" />
            <c:set var="timelineTone" value="bg-amber-100 text-amber-800 border-amber-200" />
        </c:when>
        <c:when test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
            <c:set var="timelineHeadline" value="Đang giao hàng" />
            <c:set var="timelineTone" value="bg-sky-100 text-sky-800 border-sky-200" />
        </c:when>
        <c:when test="${order.status == 'DELIVERED'}">
            <c:set var="timelineHeadline" value="Hoàn thành" />
            <c:set var="timelineTone" value="bg-emerald-100 text-emerald-800 border-emerald-200" />
        </c:when>
        <c:otherwise>
            <c:set var="timelineHeadline" value="Trạng thái đơn hàng" />
            <c:set var="timelineTone" value="bg-surface-container-low text-on-surface-variant border-outline-variant/30" />
        </c:otherwise>
    </c:choose>
    <c:set var="timelinePercent" value="${stepNum == -1 ? 100 : (stepNum - 1) * 25}" />

    <div class="premium-glass-card rounded-[1.5rem] p-6 mb-8">
        <div class="flex flex-col gap-2 lg:flex-row lg:items-end lg:justify-between mb-5">
            <div>
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">local_shipping</span> Hành trình vận chuyển
                </h3>
                <p class="text-sm text-on-surface-variant mt-1">Các mốc được trình bày theo cùng ngôn ngữ tile mềm như khối thông tin giao hàng.</p>
            </div>
            <span class="inline-flex items-center gap-2 self-start rounded-full border px-3 py-1.5 text-xs font-bold ${timelineTone}">
                <span class="material-symbols-outlined text-[16px]">
                    <c:choose>
                        <c:when test="${stepNum == -1}">cancel</c:when>
                        <c:otherwise>route</c:otherwise>
                    </c:choose>
                </span>
                <c:out value="${timelineHeadline}" />
            </span>
        </div>

        <c:choose>
            <c:when test="${stepNum == -1}">
                <div class="rounded-2xl border border-error/20 bg-error-container/30 p-5 shadow-sm">
                    <div class="flex items-start gap-3">
                        <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-error text-on-error shadow-sm">
                            <span class="material-symbols-outlined">cancel</span>
                        </span>
                        <div class="min-w-0">
                            <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-error">Hành trình tạm dừng</p>
                            <p class="mt-1 text-base font-semibold text-[#93000a]">Đơn hàng đã hủy vào ${order.updatedAt}</p>
                            <p class="mt-1 text-sm text-[#93000a]/80">
                                <strong>Người thực hiện:</strong> ${order.cancelledBy == order.customerId ? 'Khách hàng' : 'Cửa hàng / Quản trị viên'}
                            </p>
                            <p class="mt-1 text-sm text-[#93000a]/80">
                                <strong>Lý do hủy:</strong> ${order.cancellationReason != null ? order.cancellationReason : 'Không có lý do chi tiết.'}
                            </p>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid gap-3 xl:grid-cols-5">
                    <div class="rounded-2xl border ${stepNum >= 1 ? 'border-primary/20 bg-white/85 shadow-sm' : 'border-white/70 bg-white/70'} p-4 transition-all">
                        <div class="flex items-start gap-3">
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${stepNum >= 1 ? 'bg-primary-container/70 text-primary' : 'bg-surface-container-high/70 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-[20px]">receipt_long</span>
                            </span>
                            <div class="min-w-0">
                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] ${stepNum >= 1 ? 'text-primary' : 'text-on-surface-variant'}">Đặt hàng</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:choose>
                                        <c:when test="${order.status == 'PENDING_PAYMENT'}">Đang chờ thanh toán</c:when>
                                        <c:otherwise>Đơn đã tạo thành công</c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="text-xs text-on-surface-variant mt-1">
                                    <c:choose>
                                        <c:when test="${order.status == 'PENDING_PAYMENT'}">
                                            <c:choose>
                                                <c:when test="${paymentTx != null && paymentTx.status == 'processing'}">Đã chuyển tiền, chờ duyệt</c:when>
                                                <c:when test="${paymentTx != null && paymentTx.status == 'completed'}">Khớp SePay, chờ Admin duyệt</c:when>
                                                <c:otherwise>Chờ thanh toán QR</c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>Thanh toán và ghi nhận đơn thành công</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl border ${stepNum >= 2 ? 'border-primary/20 bg-white/85 shadow-sm' : 'border-white/70 bg-white/70'} p-4 transition-all">
                        <div class="flex items-start gap-3">
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${stepNum >= 2 ? 'bg-primary-container/70 text-primary' : 'bg-surface-container-high/70 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-[20px]">verified</span>
                            </span>
                            <div class="min-w-0">
                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] ${stepNum >= 2 ? 'text-primary' : 'text-on-surface-variant'}">Đã xác nhận</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:choose>
                                        <c:when test="${order.paymentMethod == 'CK'}">Đã thanh toán chuyển khoản</c:when>
                                        <c:otherwise>COD được duyệt</c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="text-xs text-on-surface-variant mt-1">Shop đã chốt đơn và sẵn sàng xử lý.</p>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl border ${stepNum >= 3 ? 'border-primary/20 bg-white/85 shadow-sm' : 'border-white/70 bg-white/70'} p-4 transition-all">
                        <div class="flex items-start gap-3">
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${stepNum >= 3 ? 'bg-primary-container/70 text-primary' : 'bg-surface-container-high/70 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-[20px]">inventory</span>
                            </span>
                            <div class="min-w-0">
                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] ${stepNum >= 3 ? 'text-primary' : 'text-on-surface-variant'}">Chuẩn bị hàng</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:choose>
                                        <c:when test="${stepNum >= 3}">Shop đang đóng gói lạnh</c:when>
                                        <c:otherwise>Chờ shop chuẩn bị</c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="text-xs text-on-surface-variant mt-1">Các mặt hàng đang được kiểm tra và đóng gói.</p>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl border ${stepNum >= 4 ? 'border-primary/20 bg-white/85 shadow-sm' : 'border-white/70 bg-white/70'} p-4 transition-all">
                        <div class="flex items-start gap-3">
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${stepNum >= 4 ? 'bg-primary-container/70 text-primary' : 'bg-surface-container-high/70 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-[20px]">local_shipping</span>
                            </span>
                            <div class="min-w-0">
                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] ${stepNum >= 4 ? 'text-primary' : 'text-on-surface-variant'}">Đang giao hàng</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:choose>
                                        <c:when test="${stepNum >= 4}">Shipper đã nhận hàng</c:when>
                                        <c:otherwise>Chờ bàn giao cho shipper</c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="text-xs text-on-surface-variant mt-1">Theo dõi trạng thái giao vận theo thời gian thực.</p>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-2xl border ${stepNum >= 5 ? 'border-primary/20 bg-white/85 shadow-sm' : 'border-white/70 bg-white/70'} p-4 transition-all">
                        <div class="flex items-start gap-3">
                            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${stepNum >= 5 ? 'bg-primary text-on-primary' : 'bg-surface-container-high/70 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-[20px]">task_alt</span>
                            </span>
                            <div class="min-w-0">
                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] ${stepNum >= 5 ? 'text-primary' : 'text-on-surface-variant'}">Hoàn thành</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:choose>
                                        <c:when test="${stepNum >= 5}">Đã ký nhận thành công</c:when>
                                        <c:otherwise>Chờ hoàn tất giao hàng</c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="text-xs text-on-surface-variant mt-1">Mốc cuối của hành trình giao hàng.</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-3">
                    <div class="h-2 flex-1 overflow-hidden rounded-full bg-surface-container-high/70">
                        <div class="order-timeline-fill h-full rounded-full bg-primary transition-all duration-500" data-progress="${timelinePercent}"></div>
                    </div>
                    <p class="text-xs text-on-surface-variant whitespace-nowrap">Cập nhật gần nhất: ${order.updatedAt}</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Main Grid Content -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
        
        <!-- Left: Items list & pricing -->
        <div class="lg:col-span-8 flex flex-col gap-6">
            <!-- Items Card -->
            <div class="premium-glass-card rounded-[1.5rem] overflow-hidden">
                <div class="px-6 py-4 border-b border-outline-variant/30 flex items-center justify-between">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold">Danh sách sản phẩm đã mua</h3>
                    <c:if test="${order.orderType != 'PARENT'}">
                        <c:choose>
                            <c:when test="${order.ownerId > 0}">
                                <a href="${pageContext.request.contextPath}/shop-view?id=${order.ownerId}" class="text-xs font-bold text-primary bg-primary-container hover:bg-primary hover:text-on-primary px-3 py-1 rounded-full flex items-center gap-1 transition-all">
                                    <span class="material-symbols-outlined text-[14px]">store</span>
                                    ${shopName}
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="text-xs font-bold text-primary bg-primary-container px-3 py-1 rounded-full flex items-center gap-1">
                                    <span class="material-symbols-outlined text-[14px]">store</span>
                                    ${shopName}
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </div>
                
                <c:choose>
                    <c:when test="${order.orderType == 'PARENT'}">
                        <!-- Hiển thị phân chia theo Shop (Sub-orders) -->
                        <div class="divide-y divide-outline-variant/20">
                            <c:forEach var="child" items="${childOrders}">
                                <div class="p-6">
                                    <!-- Shop sub-order header -->
                                    <div class="flex items-center justify-between gap-4 cursor-pointer select-none pb-2" data-order-id="${child.orderId}" onclick="toggleSubOrder(this.dataset.orderId)">
                                        <div class="flex items-center gap-3">
                                            <span class="material-symbols-outlined text-txt-2 transform transition-transform duration-300" id="arrow-${child.orderId}">expand_more</span>
                                            <div>
                                                <h4 class="font-bold text-inverse-surface text-base flex items-center gap-2">
                                                    <c:choose>
                                                        <c:when test="${child.ownerId > 0}">
                                                            <a href="${pageContext.request.contextPath}/shop-view?id=${child.ownerId}" class="hover:underline hover:text-primary transition-all" onclick="event.stopPropagation();">
                                                                ${shopNamesMap[child.orderId]}
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span>${shopNamesMap[child.orderId]}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <span class="text-xs font-normal text-on-surface-variant">(Đơn con #${child.orderId})</span>
                                                </h4>
                                                <div class="text-xs text-on-surface-variant mt-0.5">
                                                    Tổng tiền: <span class="font-bold text-[#ba1a1a]"><ft:currency value="${child.finalAmount}"/></span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="flex items-center gap-2">
                                            <span class="px-3 py-1 rounded-full text-xs font-bold bg-[#bcfdc9] text-on-secondary-container">
                                                ${child.status}
                                            </span>
                                            <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${child.orderId}" class="text-xs font-bold text-primary hover:underline flex items-center gap-0.5 ml-2" onclick="event.stopPropagation();">
                                                Chi tiết <span class="material-symbols-outlined text-[14px]">chevron_right</span>
                                            </a>
                                        </div>
                                    </div>
                                    
                                    <!-- Collapsible items list -->
                                    <div id="suborder-items-${child.orderId}" class="mt-4 pl-8 border-l-2 border-primary/20 space-y-4 overflow-hidden transition-all duration-300">
                                                                         <c:forEach var="item" items="${childOrderItemsMap[child.orderId]}">
                                     <%@ include file="/WEB-INF/jsp/customer/common/order-item-card.jspf" %>
                                 </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- Hiển thị đơn lẻ như cũ -->
                        <div class="divide-y divide-outline-variant/20">
                                                <c:forEach var="item" items="${orderItems}">
                        <%@ include file="/WEB-INF/jsp/customer/common/order-item-card.jspf" %>
                    </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Pricing summary card -->
            <div class="premium-glass-card rounded-[1.5rem] p-6">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4">Chi tiết thanh toán hóa đơn</h3>
                <div class="flex flex-col gap-3 font-body-md">
                    <div class="flex justify-between items-center text-on-surface-variant">
                        <span>Tiền hàng tạm tính</span>
                        <span class="text-inverse-surface font-semibold"><ft:currency value="${order.totalAmount}"/></span>
                    </div>
                    <div class="flex justify-between items-center text-on-surface-variant">
                        <span>Phí giao hàng lạnh</span>
                        <span class="text-inverse-surface font-semibold"><ft:currency value="${order.deliveryFee}"/></span>
                    </div>
                    <c:if test="${order.shopDiscountAmount > 0}">
                        <div class="flex justify-between items-center text-primary font-semibold">
                            <span>Voucher shop</span>
                            <span>-<ft:currency value="${order.shopDiscountAmount}"/></span>
                        </div>
                    </c:if>
                    <c:if test="${order.systemDiscountAmount > 0}">
                        <div class="flex justify-between items-center text-primary font-semibold">
                            <span>Voucher sàn (MetaFruit)</span>
                            <span>-<ft:currency value="${order.systemDiscountAmount}"/></span>
                        </div>
                    </c:if>
                    <hr class="border-outline-variant/30 my-2">
                    <div class="flex justify-between items-center font-bold text-lg">
                        <span class="text-inverse-surface">Khách hàng thực thanh toán</span>
                        <span class="text-[#ba1a1a] text-xl font-extrabold"><ft:currency value="${order.finalAmount}"/></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Shipping info & Action controls -->
        <div class="lg:col-span-4 flex flex-col gap-6">
            
                        <!-- Shipping details card -->
            <div class="premium-glass-card rounded-[1.5rem] p-6">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">local_shipping</span> Thông tin giao hàng
                </h3>
                <div class="grid gap-3 text-sm text-on-surface-variant">
                    <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                        <div class="flex items-center gap-2 mb-2 text-primary">
                            <span class="material-symbols-outlined text-base">store</span>
                            <span class="text-[11px] font-bold uppercase tracking-[0.18em]">Cửa hàng</span>
                        </div>
                        <p class="font-semibold text-on-surface">
                            <c:choose>
                                <c:when test="${not empty shopName}">
                                    <c:choose>
                                        <c:when test="${order.ownerId > 0}">
                                            <a href="${pageContext.request.contextPath}/shop-view?id=${order.ownerId}" class="hover:underline text-primary transition-all">
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
                        <div class="flex items-center gap-2 mb-2 text-primary">
                            <span class="material-symbols-outlined text-base">location_on</span>
                            <span class="text-[11px] font-bold uppercase tracking-[0.18em]">Địa chỉ giao hàng</span>
                        </div>
                        <p class="font-semibold text-on-surface"><c:out value="${order.deliveryAddress}"/></p>
                    </div>
                    <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                        <div class="flex items-center gap-2 mb-2 text-primary">
                            <span class="material-symbols-outlined text-base">payment</span>
                            <span class="text-[11px] font-bold uppercase tracking-[0.18em]">Phương thức thanh toán</span>
                        </div>
                        <p class="font-semibold text-on-surface">
                            <c:choose>
                                <c:when test="${order.paymentMethod == 'COD'}">Thanh toán khi nhận hàng (COD)</c:when>
                                <c:when test="${order.paymentMethod == 'CK'}">Chuyển khoản online / VietQR</c:when>
                                <c:otherwise><c:out value="${order.paymentMethod}"/></c:otherwise>
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

            <%-- Delivery ETA Card (shown when DISPATCHED or DELIVERED) --%>
            <c:if test="${order.status == 'DISPATCHED' || order.status == 'DELIVERED'}">
                <c:choose>
                    <c:when test="${not empty delivery}">
                        <c:choose>
                            <c:when test="${delivery.status == 'DELIVERED'}">
                                <c:set var="deliveryStatusLabel" value="Đã giao hàng thành công" />
                                <c:set var="deliveryStatusTone" value="bg-emerald-100 text-emerald-800 border-emerald-200" />
                                <c:set var="deliveryStatusIcon" value="check_circle" />
                                <c:set var="deliveryStatusNote" value="Đơn hàng đã đến tay người nhận." />
                            </c:when>
                            <c:when test="${delivery.status == 'FAILED'}">
                                <c:set var="deliveryStatusLabel" value="Giao hàng thất bại" />
                                <c:set var="deliveryStatusTone" value="bg-red-100 text-red-800 border-red-200" />
                                <c:set var="deliveryStatusIcon" value="error" />
                                <c:set var="deliveryStatusNote" value="Đơn hàng cần được xử lý lại hoặc đối soát." />
                            </c:when>
                            <c:when test="${delivery.status == 'IN_TRANSIT'}">
                                <c:set var="deliveryStatusLabel" value="Shipper đang giao hàng" />
                                <c:set var="deliveryStatusTone" value="bg-sky-100 text-sky-800 border-sky-200" />
                                <c:set var="deliveryStatusIcon" value="local_shipping" />
                                <c:set var="deliveryStatusNote" value="Đơn hàng đang trên đường đến địa chỉ của bạn." />
                            </c:when>
                            <c:when test="${delivery.status == 'PICKED_UP'}">
                                <c:set var="deliveryStatusLabel" value="Đã lấy hàng từ kho" />
                                <c:set var="deliveryStatusTone" value="bg-amber-100 text-amber-800 border-amber-200" />
                                <c:set var="deliveryStatusIcon" value="inventory_2" />
                                <c:set var="deliveryStatusNote" value="Shipper đã nhận hàng và chuẩn bị di chuyển." />
                            </c:when>
                            <c:otherwise>
                                <c:set var="deliveryStatusLabel" value="Đang tìm shipper" />
                                <c:set var="deliveryStatusTone" value="bg-amber-100 text-amber-800 border-amber-200" />
                                <c:set var="deliveryStatusIcon" value="hourglass_empty" />
                                <c:set var="deliveryStatusNote" value="Hệ thống đang ghép chuyến vận chuyển phù hợp." />
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${order.receivedStatus == 'RECEIVED'}">
                                <c:set var="receivedStatusLabel" value="Bạn đã xác nhận nhận hàng" />
                                <c:set var="receivedStatusTone" value="bg-emerald-100 text-emerald-800 border-emerald-200" />
                                <c:set var="receivedStatusIcon" value="done_all" />
                                <c:set var="receivedStatusNote" value="Đã khớp trạng thái giao nhận với phía khách hàng." />
                            </c:when>
                            <c:when test="${order.receivedStatus == 'NOT_RECEIVED'}">
                                <c:set var="receivedStatusLabel" value="Bạn đã báo chưa nhận được hàng" />
                                <c:set var="receivedStatusTone" value="bg-red-100 text-red-800 border-red-200" />
                                <c:set var="receivedStatusIcon" value="report_problem" />
                                <c:set var="receivedStatusNote" value="Hệ thống đang ghi nhận khiếu nại giao nhận." />
                            </c:when>
                            <c:otherwise>
                                <c:set var="receivedStatusLabel" value="Chờ bạn xác nhận" />
                                <c:set var="receivedStatusTone" value="bg-amber-100 text-amber-800 border-amber-200" />
                                <c:set var="receivedStatusIcon" value="hourglass_empty" />
                                <c:set var="receivedStatusNote" value="Vui lòng xác nhận sau khi bạn nhận đủ hàng." />
                            </c:otherwise>
                        </c:choose>

                        <div class="premium-glass-card rounded-[1.5rem] p-6">
                            <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between mb-5">
                                <div>
                                    <h3 class="font-headline-md text-lg text-inverse-surface font-bold flex items-center gap-2">
                                        <span class="material-symbols-outlined text-primary">schedule</span> Thông tin giao vận
                                    </h3>
                                    <p class="text-sm text-on-surface-variant mt-1">Mốc vận chuyển được gom vào các tile trạng thái để đọc nhanh và đồng nhất với phần trên.</p>
                                </div>
                                <span class="inline-flex items-center gap-2 self-start rounded-full border px-3 py-1.5 text-xs font-bold ${deliveryStatusTone}">
                                    <span class="material-symbols-outlined text-[16px]">${deliveryStatusIcon}</span>
                                    <c:out value="${deliveryStatusLabel}" />
                                </span>
                            </div>

                            <div class="grid gap-3 md:grid-cols-2">
                                <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                                    <div class="mb-2">
                                        <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Xác nhận của bạn</span>
                                    </div>
                                    <span class="inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-bold ${receivedStatusTone}">
                                        <span class="material-symbols-outlined text-[16px]">${receivedStatusIcon}</span>
                                        <c:out value="${receivedStatusLabel}" />
                                    </span>
                                    <p class="mt-3 text-xs text-on-surface-variant"><c:out value="${receivedStatusNote}" /></p>
                                </div>

                                <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                                    <div class="mb-2">
                                        <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Trạng thái vận chuyển</span>
                                    </div>
                                    <span class="inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-bold ${deliveryStatusTone}">
                                        <span class="material-symbols-outlined text-[16px]">${deliveryStatusIcon}</span>
                                        <c:out value="${deliveryStatusLabel}" />
                                    </span>
                                    <p class="mt-3 text-xs text-on-surface-variant"><c:out value="${deliveryStatusNote}" /></p>
                                </div>

                                <c:if test="${not empty delivery.estimatedDeliveryTime}">
                                    <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm md:col-span-2">
                                        <div class="mb-2">
                                            <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Dự kiến giao</span>
                                        </div>
                                        <p class="font-semibold text-on-surface">${delivery.estimatedDeliveryTime}</p>
                                        <p class="mt-1 text-xs text-on-surface-variant">Mốc này được cập nhật từ luồng vận hành giao nhận.</p>
                                    </div>
                                </c:if>

                                <c:if test="${not empty delivery.createdAt}">
                                    <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm md:col-span-2">
                                        <div class="mb-2">
                                            <span class="text-[11px] font-bold uppercase tracking-[0.18em] text-primary">Bàn giao vận chuyển</span>
                                        </div>
                                        <p class="font-semibold text-on-surface">${delivery.createdAt}</p>
                                        <p class="mt-1 text-xs text-on-surface-variant">Thời điểm đơn hàng được đưa vào luồng giao vận.</p>
                                    </div>
                                </c:if>
                            </div>

                            <div class="mt-5 border-t border-outline-variant/20 pt-5">
                                <div class="flex items-center gap-2 mb-3">
                                    <span class="material-symbols-outlined text-primary text-base">timeline</span>
                                    <h4 class="text-sm font-bold text-on-surface">Lịch sử hành trình</h4>
                                </div>

                                <div class="grid gap-3">
                                    <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                                        <div class="min-w-0">
                                            <p class="font-semibold text-on-surface text-sm">Chuẩn bị bàn giao vận chuyển</p>
                                            <p class="mt-1 text-xs text-on-surface-variant">Đơn hàng đã được ghi nhận để chờ shipper đến lấy.</p>
                                            <c:if test="${not empty delivery.createdAt}">
                                                <p class="mt-1 text-[10px] text-on-surface-variant/80">${delivery.createdAt}</p>
                                            </c:if>
                                        </div>
                                    </div>

                                    <c:if test="${delivery.status == 'PICKED_UP' || delivery.status == 'IN_TRANSIT' || delivery.status == 'DELIVERED'}">
                                        <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                                            <div class="min-w-0">
                                                <p class="font-semibold text-on-surface text-sm">Shipper đã lấy hàng</p>
                                                <p class="mt-1 text-xs text-on-surface-variant">Shipper đã nhận hàng từ cửa hàng và bắt đầu di chuyển.</p>
                                                <c:if test="${not empty delivery.pickedUpAt}">
                                                    <p class="mt-1 text-[10px] text-on-surface-variant/80">${delivery.pickedUpAt}</p>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${delivery.status == 'IN_TRANSIT' || delivery.status == 'DELIVERED'}">
                                        <div class="rounded-2xl border border-white/70 bg-white/70 p-4 shadow-sm">
                                            <div class="min-w-0">
                                                <p class="font-semibold text-on-surface text-sm">Đang vận chuyển</p>
                                                <p class="mt-1 text-xs text-on-surface-variant">Shipper đang trên đường giao đến địa chỉ của bạn.</p>
                                                <c:if test="${not empty delivery.estimatedDeliveryTime}">
                                                    <p class="mt-1 text-xs font-semibold text-primary">Dự kiến giao: ${delivery.estimatedDeliveryTime}</p>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${delivery.status == 'DELIVERED'}">
                                        <div class="rounded-2xl border border-emerald-200 bg-emerald-50/80 p-4 shadow-sm">
                                            <div class="min-w-0">
                                                <p class="font-semibold text-emerald-800 text-sm">Giao hàng thành công</p>
                                                <p class="mt-1 text-xs text-emerald-700/90">Đơn hàng đã được giao đến người nhận. Cảm ơn bạn đã mua sắm tại MetaFruit!</p>
                                                <c:if test="${not empty delivery.deliveredAt}">
                                                    <p class="mt-1 text-[10px] text-emerald-700/70">${delivery.deliveredAt}</p>
                                                </c:if>
                                                <c:if test="${not empty delivery.proofImageUrl}">
                                                    <a href="${delivery.proofImageUrl}" target="_blank" class="mt-3 block w-full max-w-[12rem] overflow-hidden rounded-xl border border-emerald-200 bg-white/80">
                                                        <img src="${delivery.proofImageUrl}" alt="Ảnh giao hàng" class="w-full max-h-24 object-cover">
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${delivery.status == 'FAILED'}">
                                        <div class="rounded-2xl border border-red-200 bg-red-50/80 p-4 shadow-sm">
                                            <div class="min-w-0">
                                                <p class="font-semibold text-red-700 text-sm">Giao hàng thất bại</p>
                                                <p class="mt-1 text-xs text-red-700/90">Lý do: ${delivery.failureReason}</p>
                                                <c:if test="${not empty delivery.updatedAt}">
                                                    <p class="mt-1 text-[10px] text-red-700/70">${delivery.updatedAt}</p>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="premium-glass-card rounded-[1.5rem] p-6">
                            <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4 flex items-center gap-2">
                                <span class="material-symbols-outlined text-primary">schedule</span> Thông tin giao vận
                            </h3>
                            <p class="text-on-surface-variant text-sm italic">Thông tin giao vận đang được cập nhật. Shipper sẽ liên hệ bạn sớm.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <!-- Customer actions control card (DEL-03) -->
            <div class="premium-glass-card rounded-[1.5rem] p-6 flex flex-col gap-4">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold border-b border-outline-variant/20 pb-3 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">construction</span> Tương tác nghiệp vụ
                </h3>

                <!-- Nút tự hủy đơn hàng (Chỉ cho phép khi ở trạng thái PENDING hoặc CONFIRMED) -->
                <c:if test="${order.status == 'PENDING_PAYMENT' || order.status == 'CONFIRMED'}">
                    <button type="button" onclick="const f=document.getElementById('cancelFormContainer'); f.classList.toggle('hidden');" class="w-full bg-[#fef2f2] text-error hover:bg-[#ba1a1a] hover:text-white py-3.5 rounded-xl font-bold transition-all border border-error/20 flex items-center justify-center gap-2 text-sm shadow-sm active:scale-95 transform">
                        <span class="material-symbols-outlined">cancel</span> Yêu cầu hủy đơn hàng
                    </button>
                    
                    <div id="cancelFormContainer" class="hidden mt-2 p-4 border border-outline-variant/40 rounded-2xl bg-slate-50 flex flex-col gap-3">
                        <form action="${pageContext.request.contextPath}/orders" method="POST" class="flex flex-col gap-3">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <div>
                                <label class="block text-xs font-bold text-on-surface-variant mb-1">Vui lòng chọn lý do hủy đơn:</label>
                                <select class="w-full rounded-lg border border-outline-variant/40 p-2.5 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm font-medium" name="reason" required>
                                    <option value="Tôi muốn thay đổi mặt hàng / số lượng mua">Tôi muốn thay đổi mặt hàng / số lượng mua</option>
                                    <option value="Tôi điền sai địa chỉ / thông tin nhận hàng">Tôi điền sai địa chỉ / thông tin nhận hàng</option>
                                    <option value="Tôi không muốn mua hàng nữa">Tôi không muốn mua hàng nữa</option>
                                    <option value="Khác">Lý do khác</option>
                                </select>
                            </div>
                            <button type="submit" class="w-full bg-error text-on-error py-2.5 rounded-xl font-bold text-sm shadow-md hover:bg-inverse-surface transition-all active:scale-95 transform" onclick="return confirm('Hành động này sẽ giải phóng toàn bộ số lượng sản phẩm đang giữ trong kho. Bạn chắc chắn muốn hủy?');">
                                Xác nhận hủy đơn
                            </button>
                        </form>
                    </div>
                </c:if>

                <!-- Nút xác nhận nhận hàng hoặc báo chưa nhận được hàng -->
                <c:if test="${(order.status == 'DISPATCHED' || order.status == 'DELIVERED') && (empty order.receivedStatus || order.receivedStatus == 'PENDING')}">
                    <div class="flex flex-col gap-3 w-full">
                        <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-full">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="confirmDelivery">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="w-full bg-primary text-on-primary hover:bg-inverse-surface py-3.5 rounded-xl font-bold transition-all shadow-md flex items-center justify-center gap-2 active:scale-95 transform" onclick="return confirm('Nhấn xác nhận khi bạn đã nhận được gói hàng tươi ngon và kiểm tra đúng số lượng.');">
                                <span class="material-symbols-outlined text-lg">verified</span> Đã nhận được hàng
                            </button>
                        </form>
                        
                        <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-full">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="reportNotReceived">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="w-full bg-red-50 text-red-600 hover:bg-red-600 hover:text-white py-3 rounded-xl font-bold transition-all border border-red-200 flex items-center justify-center gap-2 active:scale-95 transform" onclick="return confirm('Bạn xác nhận chưa nhận được hàng? Hệ thống sẽ báo cáo lên quản trị viên để giải quyết tranh chấp.');">
                                <span class="material-symbols-outlined text-lg">report_problem</span> Chưa nhận được hàng
                            </button>
                        </form>
                        <p class="text-[10px] text-on-surface-variant text-center opacity-85 mt-1">Vui lòng kiểm tra kỹ trước khi bấm xác nhận.</p>
                    </div>
                </c:if>

                <!-- Đã giao hàng thành công (DELIVERED) -> Khách hàng được quyền đánh giá hoặc yêu cầu đổi trả -->
                <c:if test="${order.status == 'DELIVERED'}">
                    <div class="flex flex-col gap-3 w-full">
                        <a href="${pageContext.request.contextPath}/reviews?orderId=${order.orderId}" class="w-full bg-primary text-on-primary hover:bg-inverse-surface py-3.5 rounded-xl font-bold transition-all shadow-md text-center flex items-center justify-center gap-2 text-sm">
                            <span class="material-symbols-outlined">star</span> Viết đánh giá sản phẩm
                        </a>
                        
                        <a href="${pageContext.request.contextPath}/returns?orderId=${order.orderId}" class="w-full bg-[#fff5f5] text-error hover:bg-[#ba1a1a] hover:text-white py-3.5 rounded-xl font-bold border border-error/20 text-center flex items-center justify-center gap-2 text-sm">
                            <span class="material-symbols-outlined">rotate_left</span> Yêu cầu Đổi / Trả hàng
                        </a>
                    </div>
                </c:if>

                <c:if test="${order.status == 'CANCELLED'}">
                    <div class="text-center p-4 border border-outline-variant/20 rounded-xl bg-slate-50 text-on-surface-variant text-xs font-semibold">
                        <span class="material-symbols-outlined text-lg mb-1 block">info</span> Đơn hàng đã dừng hoạt động. Không có thao tác khả dụng.
                    </div>
                </c:if>
            </div>
        </div>

    </div>
</main>

<script>
    function toggleSubOrder(childId) {
        const list = document.getElementById('suborder-items-' + childId);
        const arrow = document.getElementById('arrow-' + childId);
        if (!list) return;
        const isHidden = list.style.display === 'none' || list.style.display === ';
        list.style.display = isHidden ? 'block' : 'none';
        arrow.style.transform = isHidden ? 'rotate(0deg)' : 'rotate(-90deg)';
    }
    // Initialise all sub-order panels collapsed
    document.querySelectorAll('[data-order-id]').forEach(function(el) {
        const id = el.dataset.orderId;
        const list = document.getElementById('suborder-items-' + id);
        if (list) list.style.display = 'none';
    });
</script>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('.order-timeline-fill[data-progress]').forEach(function(el) {
            var value = parseFloat(el.getAttribute('data-progress') || '0');
            if (isNaN(value)) {
                value = 0;
            }
            value = Math.max(0, Math.min(100, value));
            el.style.width = value + '%';
        });
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />



