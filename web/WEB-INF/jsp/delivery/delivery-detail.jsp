<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Chi tiết giao hàng #${delivery.deliveryId}"/>
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms"></script>
<script>
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#0284C7', 'primary-hover': '#0369A1', 'primary-light': '#E0F2FE',
                'txt': '#0F172A', 'txt-2': '#475569', 'txt-3': '#94A3B8',
                'border-c': '#BAE6FD', 'bg-page': '#F0F9FF'
            },
            fontFamily: { sans: ['Lexend', 'sans-serif'] }
        }
    }
}
</script>

<style>
body { background: #F0F9FF; }
.glass-card { background: rgba(255,255,255,0.88); backdrop-filter: blur(14px); border: 1px solid rgba(186,230,253,0.6); box-shadow: 0 4px 24px -6px rgba(2,132,199,0.08); }
.info-row { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 10px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: #94A3B8; }
.info-value { font-size: 14px; font-weight: 600; color: #0F172A; }
</style>

<main class="max-w-4xl mx-auto px-4 md:px-8 py-10 font-sans text-txt">

    <%-- Back Button + Title --%>
    <div class="mb-7">
        <a href="${pageContext.request.contextPath}/delivery/dashboard"
           class="text-primary hover:text-primary-hover text-sm font-bold flex items-center gap-1.5 mb-3 w-fit">
            <i class="fa-solid fa-arrow-left"></i> Quay lại Dashboard
        </a>
        <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-2xl bg-primary flex items-center justify-center shadow-md">
                <i class="fa-solid fa-truck-fast text-white text-lg"></i>
            </div>
            <div>
                <h1 class="text-2xl font-extrabold text-[#0C4A6E] tracking-tight">Chi tiết giao hàng #${delivery.deliveryId}</h1>
                <p class="text-txt-3 text-xs">Mã đơn hàng: #${delivery.orderId}</p>
            </div>
        </div>
    </div>

    <%-- Flash --%>
    <c:if test="${not empty sessionScope.flashMsg}">
        <div id="flash-alert" class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 text-sm font-semibold shadow-sm
             ${sessionScope.flashType == 'success' ? 'bg-emerald-50 border-emerald-500 text-emerald-800' : 'bg-red-50 border-red-400 text-red-800'}">
            <i class="fa-solid ${sessionScope.flashType == 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
            <span class="flex-1"><c:out value="${sessionScope.flashMsg}"/></span>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <%-- Delivery not found --%>
    <c:if test="${empty delivery}">
        <div class="glass-card rounded-3xl text-center py-20 px-6">
            <i class="fa-solid fa-circle-exclamation text-4xl text-red-400 mb-4"></i>
            <h2 class="text-lg font-bold text-red-700 mb-2">Không tìm thấy đơn giao hàng</h2>
            <p class="text-txt-3 text-sm">Đơn giao hàng này không tồn tại hoặc bạn không có quyền xem.</p>
        </div>
    </c:if>

    <c:if test="${not empty delivery}">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

            <%-- Left: Delivery Status + Timeline --%>
            <div class="flex flex-col gap-6">

                <%-- Status Card --%>
                <div class="glass-card rounded-3xl p-6">
                    <h2 class="font-bold text-[#0C4A6E] text-base mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-route text-primary"></i> Hành trình giao hàng
                    </h2>
                    <%-- Status Steps --%>
                    <div class="flex flex-col gap-0">
                        <c:set var="s" value="${delivery.status}"/>
                        <%-- Step helper macro --%>
                        <c:forEach var="step" items="ASSIGNED,PICKED_UP,IN_TRANSIT,DELIVERED" varStatus="vs">
                            <c:set var="done" value="${s == 'ASSIGNED' && vs.index == 0
                                || (s == 'PICKED_UP' && vs.index <= 1)
                                || (s == 'IN_TRANSIT' && vs.index <= 2)
                                || (s == 'DELIVERED' && vs.index <= 3)}"/>
                            <c:set var="current" value="${s == step}"/>
                            <div class="flex items-start gap-3 pb-5 relative">
                                <%-- Connector --%>
                                <c:if test="${!vs.last}">
                                    <div class="absolute left-[13px] top-7 bottom-0 w-[2px] ${done ? 'bg-primary' : 'bg-slate-200'}"></div>
                                </c:if>
                                <%-- Circle --%>
                                <div class="w-7 h-7 rounded-full shrink-0 flex items-center justify-center border-2 z-10
                                    ${done ? 'bg-primary border-primary' : (current ? 'bg-primary-light border-primary' : 'bg-slate-100 border-slate-200')}">
                                    <i class="fa-solid ${done ? 'fa-check' : 'fa-circle'} text-[10px] ${done ? 'text-white' : (current ? 'text-primary' : 'text-slate-300')}"></i>
                                </div>
                                <div>
                                    <span class="font-bold text-sm ${done ? 'text-primary' : 'text-txt-3'}">
                                        <c:choose>
                                            <c:when test="${step == 'ASSIGNED'}">Đã nhận đơn</c:when>
                                            <c:when test="${step == 'PICKED_UP'}">Đã lấy hàng từ Shop</c:when>
                                            <c:when test="${step == 'IN_TRANSIT'}">Đang trên đường giao</c:when>
                                            <c:when test="${step == 'DELIVERED'}">Giao thành công</c:when>
                                        </c:choose>
                                    </span>
                                    <c:if test="${step == 'DELIVERED' && not empty delivery.deliveredAt}">
                                        <span class="block text-xs text-txt-3">${delivery.deliveredAt}</span>
                                    </c:if>
                                    <c:if test="${step == 'PICKED_UP' && not empty delivery.pickedUpAt}">
                                        <span class="block text-xs text-txt-3">${delivery.pickedUpAt}</span>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>

                        <%-- Failed special --%>
                        <c:if test="${delivery.status == 'FAILED'}">
                            <div class="flex items-start gap-3">
                                <div class="w-7 h-7 rounded-full bg-red-600 border-2 border-red-600 flex items-center justify-center shrink-0">
                                    <i class="fa-solid fa-xmark text-white text-[10px]"></i>
                                </div>
                                <div>
                                    <span class="font-bold text-sm text-red-600">Giao hàng thất bại</span>
                                    <c:if test="${not empty delivery.failureReason}">
                                        <p class="text-xs text-red-500 mt-0.5">${delivery.failureReason}</p>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <%-- Estimated Time Card --%>
                <div class="glass-card rounded-3xl p-6">
                    <h2 class="font-bold text-[#0C4A6E] text-base mb-3 flex items-center gap-2">
                        <i class="fa-regular fa-clock text-primary"></i> Thời gian giao dự kiến
                    </h2>
                    <c:choose>
                        <c:when test="${not empty delivery.estimatedDeliveryTime}">
                            <p class="text-2xl font-extrabold text-primary">${delivery.estimatedDeliveryTime}</p>
                        </c:when>
                        <c:otherwise>
                            <p class="text-txt-3 italic text-sm">Chưa thiết lập</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <%-- Right: Order / Recipient Info --%>
            <div class="flex flex-col gap-6">

                <%-- Recipient Info Card --%>
                <c:if test="${not empty order}">
                    <div class="glass-card rounded-3xl p-6">
                        <h2 class="font-bold text-[#0C4A6E] text-base mb-4 flex items-center gap-2">
                            <i class="fa-solid fa-user text-primary"></i> Thông tin người nhận
                        </h2>
                        <div class="flex flex-col gap-4">
                            <div class="info-row">
                                <span class="info-label">Tên người nhận</span>
                                <span class="info-value">${order.recipientName}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Số điện thoại</span>
                                <span class="info-value">${order.recipientPhone}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Địa chỉ giao hàng</span>
                                <span class="info-value">${order.deliveryAddress}</span>
                            </div>
                            <c:if test="${not empty order.notes}">
                                <div class="info-row">
                                    <span class="info-label">Ghi chú</span>
                                    <span class="info-value italic text-txt-2">${order.notes}</span>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:if>

                <%-- Proof Image Card (if delivered) --%>
                <c:if test="${not empty delivery.proofImageUrl}">
                    <div class="glass-card rounded-3xl p-6">
                        <h2 class="font-bold text-[#0C4A6E] text-base mb-3 flex items-center gap-2">
                            <i class="fa-solid fa-image text-emerald-600"></i> Ảnh bằng chứng giao hàng
                        </h2>
                        <a href="${delivery.proofImageUrl}" target="_blank" class="block hover:opacity-90 transition-opacity rounded-2xl overflow-hidden border border-border-c">
                            <img src="${delivery.proofImageUrl}" alt="Ảnh giao hàng" class="w-full max-h-56 object-cover">
                        </a>
                    </div>
                </c:if>
            </div>
        </div>
    </c:if>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
