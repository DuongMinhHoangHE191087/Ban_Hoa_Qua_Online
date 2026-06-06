<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Thông báo của bạn" />
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

<main class="max-w-4xl mx-auto px-4 py-12 font-body-md text-on-background">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 border-b border-outline-variant/30 pb-4 gap-4">
        <div>
            <h1 class="font-display-lg text-3xl text-inverse-surface font-bold tracking-tight">Thông báo của bạn</h1>
            <p class="text-on-surface-variant font-medium text-sm mt-1">Cập nhật những tin tức, đơn hàng và ưu đãi mới nhất</p>
        </div>
        <c:if test="${not empty notifications}">
            <form action="${pageContext.request.contextPath}/notifications" method="POST" class="shrink-0">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="action" value="markAllRead">
                <button type="submit" class="bg-primary text-on-primary hover:bg-inverse-surface px-5 py-2.5 rounded-xl font-semibold transition-all text-sm flex items-center gap-2 active:scale-95 transform shadow-md">
                    <span class="material-symbols-outlined text-lg">done_all</span> Đánh dấu đã đọc tất cả
                </button>
            </form>
        </c:if>
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

    <!-- Notifications List -->
    <div class="flex flex-col gap-4">
        <c:forEach var="n" items="${notifications}">
            <div class="premium-glass-card rounded-2xl p-5 flex items-start gap-4 transition-all duration-200 border-l-4 ${n.isRead ? 'border-transparent' : 'border-primary bg-primary/5'}">
                <!-- Icon theo loại thông báo -->
                <div class="shrink-0">
                    <c:choose>
                        <c:when test="${n.type == 'ORDER_UPDATE'}">
                            <div class="bg-emerald-100 text-emerald-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">box</span>
                            </div>
                        </c:when>
                        <c:when test="${n.type == 'PROMOTION'}">
                            <div class="bg-orange-100 text-orange-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">sell</span>
                            </div>
                        </c:when>
                        <c:when test="${n.type == 'SYSTEM'}">
                            <div class="bg-blue-100 text-blue-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">info</span>
                            </div>
                        </c:when>
                        <c:when test="${n.type == 'INVENTORY_ALERT'}">
                            <div class="bg-red-100 text-red-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">warning</span>
                            </div>
                        </c:when>
                        <c:when test="${n.type == 'PAYMENT'}">
                            <div class="bg-green-100 text-green-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">credit_card</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="bg-gray-100 text-gray-800 p-3 rounded-full flex items-center justify-center">
                                <span class="material-symbols-outlined">notifications</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Nội dung thông báo -->
                <div class="flex-1 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div class="flex flex-col gap-1">
                        <div class="flex items-center gap-2 flex-wrap">
                            <h3 class="font-bold text-inverse-surface text-base"><c:out value="${n.title}"/></h3>
                            <c:if test="${not n.isRead}">
                                <span class="bg-primary-container text-primary text-[10px] font-extrabold px-2 py-0.5 rounded-full uppercase tracking-wider">Mới</span>
                            </c:if>
                        </div>
                        <p class="text-on-surface-variant text-sm leading-relaxed"><c:out value="${n.message}"/></p>
                        <span class="text-on-surface-variant/60 text-xs font-medium mt-1">
                            Thời gian: <fmt:formatDate value="${n.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/>
                        </span>
                    </div>

                    <!-- Hành động -->
                    <div class="shrink-0 flex items-center gap-2">
                        <!-- Nút đánh dấu đã đọc (chỉ hiện khi chưa đọc) -->
                        <c:if test="${not n.isRead}">
                            <form action="${pageContext.request.contextPath}/notifications" method="POST" class="inline">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                <input type="hidden" name="action" value="markRead">
                                <input type="hidden" name="notificationId" value="${n.notificationId}">
                                <button type="submit" class="text-primary hover:bg-primary/10 p-2 rounded-xl transition-all font-semibold text-xs flex items-center gap-1" title="Đánh dấu đã đọc">
                                    <span class="material-symbols-outlined text-base">check</span> Đã đọc
                                </button>
                            </form>
                        </c:if>

                        <!-- Nút xem chi tiết (nếu có actionUrl) -->
                        <c:if test="${not empty n.actionUrl}">
                            <form action="${pageContext.request.contextPath}/notifications" method="POST" class="inline">
                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                <input type="hidden" name="action" value="markRead">
                                <input type="hidden" name="notificationId" value="${n.notificationId}">
                                <input type="hidden" name="redirectUrl" value="${n.actionUrl}">
                                <button type="submit" class="bg-primary text-on-primary hover:bg-inverse-surface px-4 py-2 rounded-xl transition-all font-semibold text-xs flex items-center gap-1 active:scale-95 transform shadow-sm">
                                    Chi tiết <span class="material-symbols-outlined text-base">arrow_forward</span>
                                </button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>

        <!-- Trạng thái trống -->
        <c:if test="${empty notifications}">
            <div class="text-center py-16 bg-white/50 premium-glass-card rounded-[2rem] border border-white/30">
                <span class="material-symbols-outlined text-6xl text-on-surface-variant opacity-60 mb-4">notifications_off</span>
                <h4 class="text-lg font-bold text-inverse-surface mb-2">Không có thông báo nào</h4>
                <p class="text-on-surface-variant text-sm">Hộp thư thông báo của bạn hiện đang trống sạch.</p>
            </div>
        </c:if>
    </div>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
