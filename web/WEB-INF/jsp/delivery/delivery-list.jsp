<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="MetaFruit | Lịch sử giao hàng"/>
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms"></script>
<script>
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#16A34A', 'primary-hover': '#15803D', 'primary-light': '#DCFCE7',
                'txt': '#0F172A', 'txt-2': '#475569', 'txt-3': '#94A3B8',
                'border-c': '#BBF7D0', 'bg-page': '#F0FDF4'
            },
            fontFamily: { sans: ['Lexend', 'sans-serif'] }
        }
    }
}
</script>

<main class="max-w-7xl mx-auto px-4 md:px-8 py-10 font-sans text-txt">

    <%-- Header --%>
    <div class="mb-7 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-2xl bg-primary flex items-center justify-center shadow-md">
                <i class="fa-solid fa-list-check text-white text-lg"></i>
            </div>
            <div>
                <h1 class="text-2xl font-extrabold text-[#14532D] tracking-tight">Lịch sử giao hàng</h1>
                <p class="text-txt-3 text-xs">Tổng hợp tất cả đơn giao nhận của bạn</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/delivery/dashboard"
           class="text-primary hover:text-primary-hover text-sm font-bold flex items-center gap-1.5">
            <i class="fa-solid fa-gauge-high"></i> Tổng quan
        </a>
    </div>

    <%-- Flash Message --%>
    <c:if test="${not empty sessionScope.flashMsg}">
        <div id="flash-alert" class="flex items-center gap-3 p-4 mb-6 rounded-2xl border-l-4 text-sm font-semibold shadow-sm
             ${sessionScope.flashType == 'success' ? 'bg-emerald-50 border-emerald-500 text-emerald-800' : 'bg-red-50 border-red-400 text-red-800'}">
            <i class="fa-solid ${sessionScope.flashType == 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
            <span class="flex-1"><c:out value="${sessionScope.flashMsg}"/></span>
            <button onclick="document.getElementById('flash-alert').remove()" class="opacity-60 hover:opacity-100"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <%-- Status Filter Tabs --%>
    <div class="flex flex-wrap gap-2 mb-6 bg-white/60 border border-border-c p-2 rounded-2xl backdrop-blur">
        <a href="${pageContext.request.contextPath}/delivery/list"
           class="tab-pill ${empty filterStatus ? 'bg-primary text-white shadow-sm' : 'text-txt-2 hover:bg-primary-light hover:text-primary'}">
            <i class="fa-solid fa-list"></i> Tất cả
        </a>
        <a href="${pageContext.request.contextPath}/delivery/list?status=ASSIGNED"
           class="tab-pill ${filterStatus == 'ASSIGNED' ? 'bg-blue-600 text-white shadow-sm' : 'text-txt-2 hover:bg-blue-50 hover:text-blue-600'}">
            <i class="fa-solid fa-clipboard-list"></i> Được phân công
        </a>
        <a href="${pageContext.request.contextPath}/delivery/list?status=PICKED_UP"
           class="tab-pill ${filterStatus == 'PICKED_UP' ? 'bg-yellow-500 text-white shadow-sm' : 'text-txt-2 hover:bg-yellow-50 hover:text-yellow-600'}">
            <i class="fa-solid fa-box-open"></i> Đã lấy hàng
        </a>
        <a href="${pageContext.request.contextPath}/delivery/list?status=IN_TRANSIT"
           class="tab-pill ${filterStatus == 'IN_TRANSIT' ? 'bg-orange-500 text-white shadow-sm' : 'text-txt-2 hover:bg-orange-50 hover:text-orange-500'}">
            <i class="fa-solid fa-truck-fast"></i> Đang giao
        </a>
        <a href="${pageContext.request.contextPath}/delivery/list?status=DELIVERED"
           class="tab-pill ${filterStatus == 'DELIVERED' ? 'bg-emerald-600 text-white shadow-sm' : 'text-txt-2 hover:bg-emerald-50 hover:text-emerald-600'}">
            <i class="fa-solid fa-circle-check"></i> Đã giao
        </a>
        <a href="${pageContext.request.contextPath}/delivery/list?status=FAILED"
           class="tab-pill ${filterStatus == 'FAILED' ? 'bg-red-600 text-white shadow-sm' : 'text-txt-2 hover:bg-red-50 hover:text-red-500'}">
            <i class="fa-solid fa-circle-xmark"></i> Giao thất bại
        </a>
    </div>

    <%-- Delivery Table --%>
    <div class="glass-card rounded-3xl overflow-hidden">
        <c:choose>
            <c:when test="${empty deliveryList}">
                <div class="py-20 text-center px-6">
                    <div class="w-16 h-16 rounded-full bg-primary-light flex items-center justify-center mx-auto mb-4">
                        <i class="fa-solid fa-truck-ramp-box text-2xl text-primary"></i>
                    </div>
                    <h3 class="text-base font-bold text-txt mb-1">Không có đơn hàng nào</h3>
                    <p class="text-txt-3 text-sm">
                        <c:choose>
                            <c:when test="${not empty filterStatus}">Không có đơn nào ở trạng thái này.</c:when>
                            <c:otherwise>Bạn chưa được phân công đơn giao hàng nào.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <%-- Table Header --%>
                <div class="hidden md:grid grid-cols-12 gap-2 px-6 py-3 bg-primary-light text-[11px] font-bold text-[#14532D] uppercase tracking-wider">
                    <div class="col-span-1">#</div>
                    <div class="col-span-2">Mã đơn</div>
                    <div class="col-span-3">Người nhận</div>
                    <div class="col-span-4">Địa chỉ</div>
                    <div class="col-span-1">Trạng thái</div>
                    <div class="col-span-1 text-right">Chi tiết</div>
                </div>

                <%-- Table Rows --%>
                <c:forEach var="item" items="${deliveryList}" varStatus="vs">
                    <div class="grid md:grid-cols-12 gap-2 px-6 py-4 items-center
                                ${vs.last ? '' : 'border-b border-border-c/40'}
                                hover:bg-primary-light/30 transition-colors">

                        <%-- Index --%>
                        <div class="col-span-1 text-txt-3 text-xs font-bold">${vs.count}</div>

                        <%-- Order ID --%>
                        <div class="col-span-2">
                            <span class="text-xs font-mono font-bold text-txt bg-primary-light px-2 py-0.5 rounded-lg">
                                #<c:out value="${item.orderId}"/>
                            </span>
                        </div>

                        <%-- Recipient --%>
                        <div class="col-span-3">
                            <p class="text-sm font-bold text-txt truncate"><c:out value="${item.recipientName}"/></p>
                            <p class="text-xs text-txt-3 mt-0.5"><i class="fa-solid fa-phone text-[9px] mr-1"></i><c:out value="${item.recipientPhone}"/></p>
                        </div>

                        <%-- Address --%>
                        <div class="col-span-4">
                            <p class="text-xs text-txt-2 line-clamp-2"><c:out value="${item.deliveryAddress}"/></p>
                            <c:if test="${item.estimatedDeliveryTime != null}">
                                <p class="text-[10px] text-txt-3 mt-1">
                                    <i class="fa-regular fa-clock mr-1"></i>Dự kiến:
                                    <fmt:formatDate value="${item.estimatedDeliveryTime}" pattern="dd/MM HH:mm" type="both"/>
                                </p>
                            </c:if>
                        </div>

                        <%-- Status Badge --%>
                        <div class="col-span-1">
                            <c:choose>
                                <c:when test="${item.deliveryStatus == 'ASSIGNED'}">
                                    <span class="status-badge bg-blue-50 text-blue-700 border border-blue-200">
                                        <i class="fa-solid fa-clipboard-list text-[8px]"></i> Phân công
                                    </span>
                                </c:when>
                                <c:when test="${item.deliveryStatus == 'PICKED_UP'}">
                                    <span class="status-badge bg-yellow-50 text-yellow-700 border border-yellow-200">
                                        <i class="fa-solid fa-box-open text-[8px]"></i> Đã lấy
                                    </span>
                                </c:when>
                                <c:when test="${item.deliveryStatus == 'IN_TRANSIT'}">
                                    <span class="status-badge bg-orange-50 text-orange-700 border border-orange-200">
                                        <i class="fa-solid fa-truck-fast text-[8px]"></i> Đang giao
                                    </span>
                                </c:when>
                                <c:when test="${item.deliveryStatus == 'DELIVERED'}">
                                    <span class="status-badge bg-emerald-50 text-emerald-700 border border-emerald-200">
                                        <i class="fa-solid fa-circle-check text-[8px]"></i> Đã giao
                                    </span>
                                </c:when>
                                <c:when test="${item.deliveryStatus == 'FAILED'}">
                                    <span class="status-badge bg-red-50 text-red-700 border border-red-200">
                                        <i class="fa-solid fa-circle-xmark text-[8px]"></i> Thất bại
                                    </span>
                                    <c:if test="${not empty item.failureReason}">
                                        <p class="text-[10px] text-red-500 mt-1 line-clamp-1" title="${item.failureReason}">
                                            <c:out value="${item.failureReason}"/>
                                        </p>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge bg-gray-100 text-gray-600"><c:out value="${item.deliveryStatus}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <%-- Action --%>
                        <div class="col-span-1 text-right">
                            <a href="${pageContext.request.contextPath}/delivery/detail?deliveryId=${item.deliveryId}"
                               class="inline-flex items-center gap-1.5 text-xs font-bold text-primary hover:text-primary-hover hover:underline">
                                Xem <i class="fa-solid fa-arrow-right text-[10px]"></i>
                            </a>
                        </div>
                    </div>
                </c:forEach>

                <%-- Footer count --%>
                <div class="px-6 py-3 bg-primary-light/40 border-t border-border-c/40 text-xs text-txt-3 font-semibold">
                    Tổng: ${fn:length(deliveryList)} đơn hàng
                    <c:if test="${not empty filterStatus}"> — lọc theo: <c:out value="${filterStatus}"/></c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
