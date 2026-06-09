<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Há»“ sÆ¡ cÃ¡ nhÃ¢n" />
</jsp:include>

<!-- Load Tailwind CSS Play Script -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    primary:         '#14532d',
                    'primary-hover': '#166534',
                    'primary-lt':    '#d1ffd8',
                    border:          '#e2ece7',
                    'txt':           '#0f172a',
                    'txt-2':         '#475569',
                    'txt-3':         '#94a3b8',
                },
                fontFamily: { sans: ['Lexend', 'sans-serif'] }
            }
        }
    }
</script>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 min-h-screen">
    
    <!-- Title Section -->
    <div class="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold text-txt flex items-center gap-2">
                <i class="fa-solid fa-circle-user text-primary text-3xl"></i>
                TÃ i khoáº£n cÃ¡ nhÃ¢n
            </h1>
            <p class="text-xs text-txt-2 mt-1">Quáº£n lÃ½ thÃ´ng tin tÃ i khoáº£n, Ä‘á»‹a chá»‰ nháº­n hÃ ng, lá»‹ch sá»­ Ä‘Æ¡n hÃ ng vÃ  báº£o máº­t.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/" class="inline-flex items-center gap-2 px-4 py-2 border border-gray-200 hover:border-primary text-xs font-bold text-txt-2 hover:text-primary rounded-xl bg-white shadow-sm hover:shadow transition-all duration-200 cursor-pointer">
                <i class="fa-solid fa-house"></i>
                Quay vá» trang chá»§
            </a>
        </div>
    </div>

    <!-- Alert / Flash Message -->
    <c:if test="${not empty sessionScope.flashMsg}">
        <c:set var="isError" value="${sessionScope.flashType == 'error'}"/>
        <div class="mb-6 p-4 rounded-xl border flex items-center gap-3 shadow-sm transition-all duration-300 ${isError ? 'bg-red-50 text-red-800 border-red-200' : 'bg-green-50 text-green-800 border-green-200'}">
            <i class="fa-solid ${isError ? 'fa-circle-exclamation text-red-500' : 'fa-circle-check text-green-500'} text-lg"></i>
            <span class="text-sm font-semibold"><c:out value="${sessionScope.flashMsg}"/></span>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <div class="flex flex-col lg:flex-row gap-8">
        
        <!-- Sidebar Navigation -->
        <aside class="w-full lg:w-64 shrink-0">
            <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm sticky top-24">
                <div class="flex items-center gap-3 pb-5 mb-5 border-b border-gray-100">
                    <img class="w-12 h-12 rounded-full object-cover border border-primary/20" 
                         src="${not empty user.avatarUrl ? (fn:startsWith(user.avatarUrl, 'http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)) : pageContext.request.contextPath.concat('/assets/images/default-avatar.svg')}" alt="Avatar">
                    <div class="overflow-hidden">
                        <h3 class="text-sm font-bold text-txt truncate"><c:out value="${user.fullName}"/></h3>
                        <span class="text-[10px] text-txt-3"><c:out value="${user.email}"/></span>
                    </div>
                </div>

                <h2 class="text-[10px] font-bold uppercase tracking-wider text-txt-3 mb-3 px-2">Menu quáº£n lÃ½</h2>
                <nav class="flex flex-col gap-1" id="profile-tabs">
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left bg-primary text-white" 
                            data-tab="profile-tab">
                        <i class="fa-solid fa-user text-sm w-4 text-center"></i>
                        <span class="flex-1">ThÃ´ng tin cÃ¡ nhÃ¢n</span>
                    </button>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="address-tab">
                        <i class="fa-solid fa-map-location-dot text-sm w-4 text-center"></i>
                        <span class="flex-1">Sá»• Ä‘á»‹a chá»‰</span>
                        <c:if test="${not empty addresses}"><span class="ml-auto px-1.5 py-0.5 bg-gray-200 text-txt-3 rounded text-[9px] font-bold">${fn:length(addresses)}</span></c:if>
                    </button>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="orders-tab">
                        <i class="fa-solid fa-box text-sm w-4 text-center"></i>
                        <span class="flex-1">ÄÆ¡n hÃ ng cá»§a tÃ´i</span>
                        <c:if test="${not empty orders}"><span class="ml-auto px-1.5 py-0.5 bg-primary/10 text-primary rounded text-[9px] font-bold">${fn:length(orders)}</span></c:if>
                    </button>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="payments-tab">
                        <i class="fa-solid fa-credit-card text-sm w-4 text-center"></i>
                        <span class="flex-1">Lá»‹ch sá»­ thanh toÃ¡n</span>
                        <c:if test="${not empty payments}"><span class="ml-auto px-1.5 py-0.5 bg-blue-100 text-blue-700 rounded text-[9px] font-bold">${fn:length(payments)}</span></c:if>
                    </button>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="security-tab">
                        <i class="fa-solid fa-shield-halved text-sm w-4 text-center"></i>
                        <span class="flex-1">Äá»•i máº­t kháº©u</span>
                    </button>
                </nav>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="flex-1">
            
            <!-- 1. Profile Info Tab -->
            <div id="profile-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-user text-primary"></i> ThÃ´ng tin tÃ i khoáº£n
                </h2>
                
                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data" class="space-y-6">
                    <input type="hidden" name="action" value="updateProfile">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                    <!-- Avatar Upload -->
                    <div class="flex flex-col sm:flex-row items-center gap-6 pb-6 border-b border-gray-100">
                        <div class="relative w-24 h-24 rounded-full overflow-hidden border border-primary/20 shadow-sm cursor-pointer group shrink-0" 
                             onclick="document.getElementById('avatarInput').click();" 
                             title="Click Ä‘á»ƒ Ä‘á»•i áº£nh Ä‘áº¡i diá»‡n">
                            <img id="avatarPreview" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" 
                                 src="${not empty user.avatarUrl ? (fn:startsWith(user.avatarUrl, 'http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)) : pageContext.request.contextPath.concat('/assets/images/default-avatar.svg')}" alt="Avatar">
                            <div class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                <i class="fa-solid fa-camera text-base"></i>
                            </div>
                        </div>
                        <div class="text-center sm:text-left space-y-1.5">
                            <h3 class="text-sm font-bold text-txt"><c:out value="${user.fullName}"/></h3>
                            <div class="flex items-center justify-center sm:justify-start gap-2">
                                <span class="text-[10px] text-txt-3">Vai trÃ²:</span>
                                <span class="px-2 py-0.5 bg-primary-lt text-primary border border-primary/10 rounded-full text-[9px] font-bold uppercase"><c:out value="${user.role}"/></span>
                            </div>
                            <input type="file" name="avatar" id="avatarInput" accept="image/jpeg,image/png,image/webp" class="hidden">
                            <button type="button" class="px-3 py-1.5 border border-border hover:border-primary text-[10px] font-bold text-txt-2 hover:text-primary rounded-lg transition-colors cursor-pointer" 
                                    onclick="document.getElementById('avatarInput').click();">Chá»n áº£nh má»›i</button>
                        </div>
                    </div>

                    <!-- Personal Information Form fields -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="fullName">Há» vÃ  tÃªn <span class="text-red-500">*</span></label>
                            <input type="text" id="fullName" name="fullName" value="<c:out value="${user.fullName}"/>" required
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="phone">Sá»‘ Ä‘iá»‡n thoáº¡i</label>
                            <input type="text" id="phone" name="phone" value="<c:out value="${user.phone}"/>" placeholder="VÃ­ dá»¥: 0987654321"
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                    </div>

                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-txt-3" for="email">Äá»‹a chá»‰ Email (TÃªn Ä‘Äƒng nháº­p)</label>
                        <input type="email" id="email" name="email" value="<c:out value="${user.email}"/>" disabled
                               class="w-full px-4 py-2 bg-gray-50 border border-border rounded-xl text-xs text-txt-3 cursor-not-allowed">
                    </div>

                    <div class="flex justify-end pt-4">
                        <button type="submit" class="flex items-center gap-2 px-5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                            <i class="fa-solid fa-floppy-disk"></i> LÆ°u thay Ä‘á»•i
                        </button>
                    </div>
                </form>
            </div>

            <!-- 2. Address Book Tab -->
            <div id="address-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <div class="flex justify-between items-center mb-6 pb-3 border-b border-gray-100">
                    <h2 class="text-base font-bold text-txt flex items-center gap-2">
                        <i class="fa-solid fa-map-location-dot text-primary"></i> Sá»• Ä‘á»‹a chá»‰ giao hÃ ng
                    </h2>
                    <button onclick="openAddressModal()" class="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">
                        <i class="fa-solid fa-plus"></i> ThÃªm Ä‘á»‹a chá»‰ má»›i
                    </button>
                </div>

                <!-- Address list container -->
                <div class="space-y-4">
                    <c:choose>
                        <c:when test="${not empty addresses}">
                            <c:forEach var="addr" items="${addresses}">
                                <div class="p-4 rounded-xl border border-gray-100 bg-gray-50/50 hover:bg-gray-50 transition-colors flex justify-between items-start gap-4">
                                    <div class="space-y-1">
                                        <div class="flex items-center gap-2.5">
                                            <span class="text-xs font-bold text-txt"><c:out value="${addr.recipientName}"/></span>
                                            <span class="text-gray-300">|</span>
                                            <span class="text-xs text-txt-2"><c:out value="${addr.recipientPhone}"/></span>
                                            <c:if test="${addr['default']}">
                                                <span class="px-2 py-0.5 bg-primary/10 text-primary border border-primary/20 rounded text-[9px] font-bold">Máº·c Ä‘á»‹nh</span>
                                            </c:if>
                                        </div>
                                        <p class="text-xs text-txt-2 leading-relaxed"><c:out value="${addr.addressDetail}"/></p>
                                    </div>
                                    <div class="flex items-center gap-2 shrink-0">
                                        <c:if test="${not addr['default']}">
                                            <form action="${pageContext.request.contextPath}/profile" method="post" class="inline">
                                                <input type="hidden" name="action" value="setDefaultAddress">
                                                <input type="hidden" name="addressId" value="${addr.addressId}">
                                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                <button type="submit" class="px-2 py-1 text-[9px] border border-gray-200 text-txt-2 hover:border-primary hover:text-primary rounded transition-colors cursor-pointer bg-white">Äáº·t máº·c Ä‘á»‹nh</button>
                                            </form>
                                        </c:if>
                                        <button data-address-id="${addr.addressId}"
                                                data-recipient-name="<c:out value='${addr.recipientName}'/>"
                                                data-recipient-phone="<c:out value='${addr.recipientPhone}'/>"
                                                data-address-detail="<c:out value='${addr.addressDetail}'/>"
                                                data-is-default="${addr['default']}"
                                                onclick="openAddressModalFromBtn(this)" 
                                                class="p-1.5 border border-gray-200 text-txt-2 hover:border-blue-500 hover:text-blue-500 rounded bg-white transition-colors cursor-pointer" title="Sá»­a">
                                            <i class="fa-solid fa-pen text-xs"></i>
                                        </button>
                                        <c:if test="${not addr['default']}">
                                            <form action="${pageContext.request.contextPath}/profile" method="post" class="inline" onsubmit="return confirm('Báº¡n cháº¯c cháº¯n muá»‘n xÃ³a Ä‘á»‹a chá»‰ nÃ y?');">
                                                <input type="hidden" name="action" value="deleteAddress">
                                                <input type="hidden" name="addressId" value="${addr.addressId}">
                                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                <button type="submit" class="p-1.5 border border-gray-200 text-txt-2 hover:border-red-500 hover:text-red-500 rounded bg-white transition-colors cursor-pointer" title="XÃ³a">
                                                    <i class="fa-solid fa-trash text-xs"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-8 border-2 border-dashed border-gray-100 rounded-2xl">
                                <i class="fa-solid fa-map-location text-gray-300 text-3xl mb-2.5"></i>
                                <p class="text-xs text-txt-3">ChÆ°a lÆ°u Ä‘á»‹a chá»‰ giao hÃ ng nÃ o. Vui lÃ²ng thÃªm Ä‘á»‹a chá»‰ nháº­n hÃ ng!</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>


            <!-- 3. Orders List and Tracking Tab -->
            <div id="orders-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <div class="flex flex-wrap items-center justify-between gap-4 mb-6 pb-3 border-b border-gray-100">
                    <h2 class="text-base font-bold text-txt flex items-center gap-2">
                        <i class="fa-solid fa-box text-primary"></i> ÄÆ¡n hÃ ng cá»§a tÃ´i
                    </h2>
                    <!-- Status Filter Tabs -->
                    <div class="flex flex-wrap gap-1" id="order-filter-tabs">
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-primary bg-primary text-white transition-all" data-status="ALL">Táº¥t cáº£</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-amber-400 hover:text-amber-600 transition-all" data-status="PENDING_PAYMENT">Chá» TT</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-blue-400 hover:text-blue-600 transition-all" data-status="CONFIRMED">ÄÃ£ nháº­n</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-purple-400 hover:text-purple-600 transition-all" data-status="DISPATCHED">Äang giao</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-green-400 hover:text-green-600 transition-all" data-status="DELIVERED">ÄÃ£ giao</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-red-400 hover:text-red-600 transition-all" data-status="CANCELLED">ÄÃ£ há»§y</button>
                    </div>
                </div>

                <div class="space-y-5" id="orders-list">
                    <c:choose>
                        <c:when test="${not empty orders}">
                            <c:forEach var="ord" items="${orders}">
                                <div class="order-card border border-gray-100 rounded-xl overflow-hidden hover:shadow-md transition-shadow" data-status="${ord.status}">
                                    <!-- Header -->
                                    <div class="px-4 py-3 bg-gray-50 border-b border-gray-100 flex flex-wrap justify-between items-center gap-3">
                                        <div class="flex items-center gap-3">
                                            <span class="text-xs font-bold text-primary">ÄÆ¡n hÃ ng #<c:out value="${ord.orderId}"/></span>
                                            <span class="text-[10px] text-txt-3">Äáº·t ngÃ y: <fmt:parseDate value="${ord.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" /><fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm"/></span>
                                        </div>
                                        <div class="flex items-center gap-2.5">
                                            <!-- Vietnamese status labels with icons -->
                                            <c:choose>
                                                <c:when test="${ord.status == 'PENDING_PAYMENT'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-amber-50 text-amber-700 border-amber-200">
                                                        <i class="fa-solid fa-clock mr-0.5"></i> Chá» thanh toÃ¡n
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'CONFIRMED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-blue-50 text-blue-700 border-blue-200">
                                                        <i class="fa-solid fa-clipboard-check mr-0.5"></i> ÄÃ£ xÃ¡c nháº­n
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'PREPARING'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-indigo-50 text-indigo-700 border-indigo-200">
                                                        <i class="fa-solid fa-box-open mr-0.5"></i> Äang chuáº©n bá»‹
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'DISPATCHED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-purple-50 text-purple-700 border-purple-200">
                                                        <i class="fa-solid fa-truck-fast mr-0.5"></i> Äang giao hÃ ng
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'DELIVERED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-emerald-50 text-emerald-700 border-emerald-200">
                                                        <i class="fa-solid fa-circle-check mr-0.5"></i> ÄÃ£ giao thÃ nh cÃ´ng
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'CANCELLED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-red-50 text-red-700 border-red-200">
                                                        <i class="fa-solid fa-xmark mr-0.5"></i> ÄÃ£ há»§y
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-gray-100 text-gray-700 border-gray-200">
                                                        <c:out value="${ord.status}"/>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <!-- Content snippet -->
                                    <div class="p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
                                        <div class="space-y-1.5 flex-1">
                                            <div class="text-[10px] text-txt-3 flex items-center gap-1.5">
                                                <i class="fa-solid fa-store"></i>
                                                <span>Cá»­a hÃ ng: <span class="font-semibold text-txt-2"><c:out value="${shopNamesMap[ord.orderId]}"/></span></span>
                                            </div>
                                            <!-- Items list -->
                                            <div class="space-y-1">
                                                <c:forEach var="item" items="${orderItemsMap[ord.orderId]}" varStatus="st">
                                                    <c:if test="${st.index < 2}">
                                                        <p class="text-xs text-txt-2 flex justify-between">
                                                            <span>â€¢ <c:out value="${item.productNameSnapshot}"/> (<c:out value="${item.variantLabelSnapshot}"/>) <span class="text-txt-3">x${item.quantity}</span></span>
                                                            <span class="font-medium text-txt"><fmt:formatNumber value="${item.subtotal}" type="currency" currencySymbol="Ä‘" maxFractionDigits="0"/></span>
                                                        </p>
                                                    </c:if>
                                                </c:forEach>
                                                <c:if test="${st.count > 2}">
                                                    <p class="text-[10px] text-txt-3 italic pl-3">... vÃ  ${st.count - 2} sáº£n pháº©m khÃ¡c</p>
                                                </c:if>
                                            </div>
                                        </div>
                                        
                                        <!-- Actions and Total -->
                                        <div class="flex flex-col items-end gap-2 md:border-l md:border-gray-100 md:pl-4 shrink-0 min-w-[140px]">
                                            <span class="text-[10px] text-txt-3">Tá»•ng sá»‘ tiá»n:</span>
                                            <span class="text-xs font-bold text-primary"><fmt:formatNumber value="${ord.finalAmount}" type="currency" currencySymbol="Ä‘" maxFractionDigits="0"/></span>
                                            
                                            <!-- Button detail trigger -->
                                            <button data-order-id="${ord.orderId}"
                                                    data-status="${ord.status}"
                                                    data-payment-method="${ord.paymentMethod}"
                                                    data-delivery-address="<c:out value='${ord.deliveryAddress}'/>"
                                                    data-delivery-fee="${ord.deliveryFee}"
                                                    data-discount-amount="${ord.discountAmount}"
                                                    data-final-amount="${ord.finalAmount}"
                                                    data-notes="<c:out value='${ord.notes}'/>"
                                                    data-shop-name="<c:out value='${shopNamesMap[ord.orderId]}'/>"
                                                    onclick="openOrderDetailModalFromBtn(this)"
                                                    class="w-full px-2.5 py-1 bg-white border border-gray-200 text-txt-2 hover:border-primary hover:text-primary rounded text-[9px] font-bold transition-all cursor-pointer">
                                                <i class="fa-solid fa-file-lines mr-0.5"></i> Chi tiáº¿t & Váº­n Ä‘Æ¡n
                                            </button>

                                            <!-- Cancel button: only for PENDING_PAYMENT -->
                                            <c:if test="${ord.status == 'PENDING_PAYMENT'}">
                                                <form action="${pageContext.request.contextPath}/profile" method="post" class="w-full"
                                                      onsubmit="return confirm('Báº¡n cháº¯c cháº¯n muá»‘n há»§y Ä‘Æ¡n hÃ ng #${ord.orderId}?\nHÃ nh Ä‘á»™ng nÃ y khÃ´ng thá»ƒ hoÃ n tÃ¡c!');">
                                                    <input type="hidden" name="action" value="cancelOrder">
                                                    <input type="hidden" name="orderId" value="${ord.orderId}">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <button type="submit" class="w-full px-2.5 py-1 bg-white border border-red-200 text-red-500 hover:bg-red-50 hover:border-red-400 rounded text-[9px] font-bold transition-all cursor-pointer">
                                                        <i class="fa-solid fa-ban mr-0.5"></i> Há»§y Ä‘Æ¡n hÃ ng
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-10 border-2 border-dashed border-gray-100 rounded-2xl">
                                <i class="fa-solid fa-bag-shopping text-gray-300 text-3xl mb-3"></i>
                                <p class="text-xs text-txt-3">Báº¡n chÆ°a Ä‘áº·t Ä‘Æ¡n hÃ ng nÃ o trÃªn MetaFruit.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Empty filter result placeholder -->
                    <div id="orders-empty-filter" class="hidden text-center py-10 border-2 border-dashed border-gray-100 rounded-2xl">
                        <i class="fa-solid fa-filter-circle-xmark text-gray-300 text-3xl mb-3"></i>
                        <p class="text-xs text-txt-3">KhÃ´ng cÃ³ Ä‘Æ¡n hÃ ng nÃ o theo bá»™ lá»c nÃ y.</p>
                    </div>
                </div>
            </div>

            <div id="payments-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-credit-card text-primary"></i> Lá»‹ch sá»­ giao dá»‹ch & Thanh toÃ¡n
                </h2>

                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse text-xs">
                        <thead>
                            <tr class="bg-gray-50 border-b border-gray-100 text-txt-2 font-bold">
                                <th class="p-3">Giao dá»‹ch</th>
                                <th class="p-3">ÄÆ¡n hÃ ng</th>
                                <th class="p-3">HÃ¬nh thá»©c</th>
                                <th class="p-3">MÃ£ GD</th>
                                <th class="p-3">Sá»‘ tiá»n</th>
                                <th class="p-3">Thá»i gian</th>
                                <th class="p-3">Tráº¡ng thÃ¡i</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-gray-100">
                            <c:choose>
                                <c:when test="${not empty payments}">
                                    <c:forEach var="pay" items="${payments}">
                                        <tr class="hover:bg-gray-50/50">
                                            <td class="p-3 font-semibold text-txt">#<c:out value="${pay.transactionId}"/></td>
                                            <td class="p-3 text-primary font-bold">#<c:out value="${pay.orderId}"/></td>
                                            <td class="p-3"><c:out value="${pay.paymentMethod}"/></td>
                                            <td class="p-3 text-txt-2 font-mono"><c:out value="${pay.sepayTransactionId != null ? pay.sepayTransactionId : '--'}"/></td>
                                            <td class="p-3 font-bold text-txt"><fmt:formatNumber value="${pay.amount}" type="currency" currencySymbol="Ä‘" maxFractionDigits="0"/></td>
                                            <td class="p-3 text-txt-3">
                                                <fmt:parseDate value="${pay.initiatedAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedInit" type="both" />
                                                <fmt:formatDate value="${parsedInit}" pattern="dd/MM/yyyy HH:mm"/>
                                            </td>
                                            <td class="p-3">
                                                <c:set var="pClass" value="bg-gray-100 text-gray-700"/>
                                                <c:if test="${pay.status == 'pending'}"><c:set var="pClass" value="bg-amber-100 text-amber-800"/></c:if>
                                                <c:if test="${pay.status == 'completed'}"><c:set var="pClass" value="bg-green-100 text-green-800"/></c:if>
                                                <c:if test="${pay.status == 'failed'}"><c:set var="pClass" value="bg-red-100 text-red-800"/></c:if>
                                                <span class="px-2 py-0.5 rounded text-[9px] font-bold capitalize <c:out value="${pClass}"/>"><c:out value="${pay.status}"/></span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="text-center py-6 text-txt-3">ChÆ°a phÃ¡t sinh giao dá»‹ch thanh toÃ¡n nÃ o.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- 5. Password tab -->
            <div id="security-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-shield-halved text-primary"></i> Äá»•i máº­t kháº©u báº£o máº­t
                </h2>

                <c:choose>
                    <c:when test="${empty user.passwordHash}">
                        <div class="p-4 bg-emerald-50/50 border border-primary/20 rounded-xl text-primary text-xs font-medium flex items-center gap-2.5">
                            <i class="fa-solid fa-circle-info text-base"></i> 
                            <span>TÃ i khoáº£n nÃ y Ä‘Æ°á»£c Ä‘Äƒng nháº­p trá»±c tiáº¿p qua <strong>Google OAuth</strong>. Máº­t kháº©u khÃ´ng Ã¡p dá»¥ng.</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/profile" method="post" id="passwordForm" class="space-y-6">
                            <input type="hidden" name="action" value="changePassword">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-bold text-txt-2" for="currentPassword">Máº­t kháº©u hiá»‡n táº¡i <span class="text-red-500">*</span></label>
                                <div class="relative w-full">
                                    <input type="password" id="currentPassword" name="currentPassword" required
                                           class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                                    <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                            onclick="togglePasswordVisibility('currentPassword', this)">
                                        <i class="fa-solid fa-eye text-xs"></i>
                                    </button>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-txt-2" for="newPassword">Máº­t kháº©u má»›i <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="newPassword" name="newPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('newPassword', this)">
                                            <i class="fa-solid fa-eye text-xs"></i>
                                        </button>
                                    </div>
                                    <div class="text-[10px] text-txt-3">Äá»™ dÃ i tá»« 8 Ä‘áº¿n 64 kÃ½ tá»±.</div>
                                    <div class="mt-2 space-y-1.5">
                                        <div class="flex items-center justify-between text-[9px] font-semibold">
                                            <span class="text-txt-3">Äá»™ máº¡nh máº­t kháº©u:</span>
                                            <span id="strengthText" class="text-txt-3 font-bold">Trá»‘ng</span>
                                        </div>
                                        <div class="w-full bg-gray-200 h-1.5 rounded-full overflow-hidden flex gap-1">
                                            <div id="strengthSegment1" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment2" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment3" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                        </div>
                                    </div>
                                </div>

                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-txt-2" for="confirmPassword">XÃ¡c nháº­n máº­t kháº©u má»›i <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="confirmPassword" name="confirmPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('confirmPassword', this)">
                                            <i class="fa-solid fa-eye text-xs"></i>
                                        </button>
                                    </div>
                                    <span id="matchText" class="text-[9px] text-red-600 hidden">Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p!</span>
                                </div>
                            </div>

                            <div class="flex justify-end pt-4">
                                <button type="submit" id="submitPasswordBtn" class="flex items-center gap-2 px-5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                                    <i class="fa-solid fa-key"></i> Äáº·t láº¡i máº­t kháº©u
                                </button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<!-- ==================== ADDRESS BOOK MODAL ==================== -->
<div id="addressModal" class="fixed inset-0 bg-black/50 backdrop-blur-[2px] flex items-center justify-center z-50 hidden opacity-0 transition-opacity duration-300">
    <div class="bg-white rounded-2xl p-6 w-full max-w-md border border-gray-100 shadow-xl transform scale-95 transition-transform duration-300">
        <div class="flex justify-between items-center pb-3 border-b border-gray-100 mb-4">
            <h3 id="modalTitle" class="text-sm font-bold text-txt">ThÃªm Ä‘á»‹a chá»‰ giao hÃ ng má»›i</h3>
            <button onclick="closeAddressModal()" class="text-txt-3 hover:text-txt border-0 bg-transparent cursor-pointer"><i class="fa-solid fa-xmark text-base"></i></button>
        </div>

        <form action="${pageContext.request.contextPath}/profile" method="post" class="space-y-4">
            <input type="hidden" name="action" id="modalAction" value="addAddress">
            <input type="hidden" name="addressId" id="modalAddressId" value="">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="recipientName">Há» vÃ  tÃªn ngÆ°á»i nháº­n *</label>
                <input type="text" id="recipientName" name="recipientName" required
                       class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary">
            </div>

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="recipientPhone">Sá»‘ Ä‘iá»‡n thoáº¡i nháº­n hÃ ng *</label>
                <input type="text" id="recipientPhone" name="recipientPhone" required
                       class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary">
            </div>

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="addressDetail">Äá»‹a chá»‰ chi tiáº¿t *</label>
                <textarea id="addressDetail" name="addressDetail" rows="3" required placeholder="Sá»‘ nhÃ , tÃªn Ä‘Æ°á»ng, phÆ°á»ng/xÃ£, quáº­n/huyá»‡n, tá»‰nh/thÃ nh phá»‘..."
                          class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary resize-none"></textarea>
            </div>

            <div class="flex items-center gap-2">
                <input type="checkbox" id="isDefault" name="isDefault" value="true" class="rounded text-primary focus:ring-primary">
                <label for="isDefault" class="text-[10px] text-txt-2 font-medium cursor-pointer">Äáº·t lÃ m Ä‘á»‹a chá»‰ nháº­n hÃ ng máº·c Ä‘á»‹nh</label>
            </div>

            <div class="flex justify-end gap-2.5 pt-3 border-t border-gray-100">
                <button type="button" onclick="closeAddressModal()" class="px-4 py-2 border border-gray-200 hover:bg-gray-50 text-[10px] font-bold text-txt-2 rounded-lg transition-colors cursor-pointer bg-white">Há»§y bá»</button>
                <button type="submit" class="px-4 py-2 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">LÆ°u Ä‘á»‹a chá»‰</button>
            </div>
        </form>
    </div>
</div>

<!-- ==================== ORDER DETAILS MODAL ==================== -->
<div id="orderDetailModal" class="fixed inset-0 bg-black/50 backdrop-blur-[2px] flex items-center justify-center z-50 hidden opacity-0 transition-opacity duration-300">
    <div class="bg-white rounded-2xl p-6 w-full max-w-2xl border border-gray-100 shadow-xl transform scale-95 transition-transform duration-300 max-h-[85vh] overflow-y-auto">
        <div class="flex justify-between items-center pb-3 border-b border-gray-100 mb-5">
            <h3 class="text-sm font-bold text-txt">Chi tiáº¿t Ä‘Æ¡n hÃ ng & Váº­n Ä‘Æ¡n</h3>
            <button onclick="closeOrderDetailModal()" class="text-txt-3 hover:text-txt border-0 bg-transparent cursor-pointer"><i class="fa-solid fa-xmark text-base"></i></button>
        </div>

        <div class="space-y-6">
            
            <!-- Delivery Stepper Tracking Indicator -->
            <div>
                <h4 class="text-[10px] font-bold uppercase tracking-wider text-txt-3 mb-4">Tráº¡ng thÃ¡i váº­n chuyá»ƒn</h4>
                <div class="flex items-center justify-between text-xs font-semibold relative px-6">
                    <!-- Line connector -->
                    <div class="absolute top-3.5 left-[15%] right-[15%] h-0.5 bg-gray-200 -z-10" id="stepperLine"></div>
                    
                    <!-- Steps -->
                    <div class="flex flex-col items-center gap-1.5 step-node" id="step-PENDING_PAYMENT">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors" id="step-icon-1">
                            <i class="fa-solid fa-wallet text-gray-300 text-xs"></i>
                        </div>
                        <span class="text-[9px] text-txt-3">Chá» T.ToÃ¡n</span>
                    </div>
                    <div class="flex flex-col items-center gap-1.5 step-node" id="step-CONFIRMED">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors" id="step-icon-2">
                            <i class="fa-solid fa-clipboard-check text-gray-300 text-xs"></i>
                        </div>
                        <span class="text-[9px] text-txt-3">ÄÃ£ Nháº­n</span>
                    </div>
                    <div class="flex flex-col items-center gap-1.5 step-node" id="step-PREPARING">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors" id="step-icon-3">
                            <i class="fa-solid fa-box-open text-gray-300 text-xs"></i>
                        </div>
                        <span class="text-[9px] text-txt-3">Chuáº©n Bá»‹</span>
                    </div>
                    <div class="flex flex-col items-center gap-1.5 step-node" id="step-DISPATCHED">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors" id="step-icon-4">
                            <i class="fa-solid fa-truck-fast text-gray-300 text-xs"></i>
                        </div>
                        <span class="text-[9px] text-txt-3">Äang Giao</span>
                    </div>
                    <div class="flex flex-col items-center gap-1.5 step-node" id="step-DELIVERED">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors" id="step-icon-5">
                            <i class="fa-solid fa-circle-check text-gray-300 text-xs"></i>
                        </div>
                        <span class="text-[9px] text-txt-3">ÄÃ£ Giao</span>
                    </div>
                </div>
            </div>

            <!-- Details grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-gray-100">
                <div class="space-y-3">
                    <h4 class="text-[10px] font-bold uppercase tracking-wider text-txt-3">ThÃ´ng tin nháº­n hÃ ng</h4>
                    <div class="space-y-1.5 text-xs text-txt-2">
                        <p>Cá»­a hÃ ng: <strong class="text-txt" id="detailShopName">Cá»­a hÃ ng A</strong></p>
                        <p>Äá»‹a chá»‰ giao hÃ ng: <span id="detailAddress">Sá»‘ nhÃ  x, Ä‘Æ°á»ng y...</span></p>
                        <p>PhÆ°Æ¡ng thá»©c thanh toÃ¡n: <strong id="detailPaymentMethod">Chuyá»ƒn khoáº£n</strong></p>
                        <p>Ghi chÃº Ä‘Æ¡n hÃ ng: <span id="detailNotes" class="italic text-txt-3">KhÃ´ng cÃ³ ghi chÃº</span></p>
                    </div>
                </div>

                <div class="space-y-3">
                    <h4 class="text-[10px] font-bold uppercase tracking-wider text-txt-3">HÃ³a Ä‘Æ¡n thanh toÃ¡n</h4>
                    <div class="space-y-1.5 text-xs text-txt-2">
                        <div class="flex justify-between">
                            <span>PhÃ­ váº­n chuyá»ƒn:</span>
                            <span id="detailFee">0Ä‘</span>
                        </div>
                        <div class="flex justify-between">
                            <span>Giáº£m giÃ¡ khuyáº¿n mÃ£i:</span>
                            <span id="detailDiscount">0Ä‘</span>
                        </div>
                        <div class="flex justify-between border-t border-gray-100 pt-1.5 font-bold text-txt text-sm">
                            <span>Tá»•ng tiá»n thá»±c nháº­n:</span>
                            <span class="text-primary" id="detailFinalAmount">0Ä‘</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Close action -->
            <div class="flex justify-end pt-3 border-t border-gray-100">
                <button type="button" onclick="closeOrderDetailModal()" class="px-5 py-2 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">
                    ÄÃ³ng cá»­a sá»•
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    // JS Tab navigation system
    const tabs = document.querySelectorAll('#profile-tabs button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabs.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.getAttribute('data-tab');

            // Reset buttons
            tabs.forEach(t => {
                t.className = "tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary";
            });

            // Set active button
            btn.className = "tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left bg-primary text-white";

            // Hide all contents
            tabContents.forEach(c => c.classList.add('hidden'));

            // Show active content
            document.getElementById(target).classList.remove('hidden');
        });
    });

    // Order filter buttons â€” client-side filter for order cards
    const orderFilterBtns = document.querySelectorAll('.order-filter-btn');
    const orderCards = document.querySelectorAll('.order-card');
    const emptyFilterMsg = document.getElementById('orders-empty-filter');

    orderFilterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const status = btn.getAttribute('data-status');

            // Update active filter button style
            orderFilterBtns.forEach(b => {
                b.className = 'order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 transition-all';
            });
            btn.className = 'order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-primary bg-primary text-white transition-all';

            // Filter cards
            let visibleCount = 0;
            orderCards.forEach(card => {
                if (status === 'ALL' || card.getAttribute('data-status') === status) {
                    card.style.display = '';
                    visibleCount++;
                } else {
                    card.style.display = 'none';
                }
            });

            // Show/hide empty placeholder
            if (emptyFilterMsg) {
                if (visibleCount === 0 && orderCards.length > 0) {
                    emptyFilterMsg.classList.remove('hidden');
                } else {
                    emptyFilterMsg.classList.add('hidden');
                }
            }
        });
    });

    // Preview avatar image before uploading â€” enhanced with animation
    const avatarInput = document.getElementById('avatarInput');
    if (avatarInput) {
        avatarInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                if (file.size > 5 * 1024 * 1024) {
                    alert('File áº£nh khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 5MB!');
                    event.target.value = '';
                    return;
                }
                const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
                if (!allowedTypes.includes(file.type)) {
                    alert('Chá»‰ cháº¥p nháº­n cÃ¡c Ä‘á»‹nh dáº¡ng áº£nh: JPG, PNG, WEBP');
                    event.target.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = function(e) {
                    const preview = document.getElementById('avatarPreview');
                    preview.style.opacity = '0.5';
                    preview.style.transform = 'scale(0.95)';
                    preview.style.transition = 'all 0.2s ease';
                    setTimeout(() => {
                        preview.src = e.target.result;
                        preview.style.opacity = '1';
                        preview.style.transform = 'scale(1)';
                    }, 150);
                    // Update sidebar avatar too
                    const sidebarAvatar = document.querySelector('aside img');
                    if (sidebarAvatar) setTimeout(() => { sidebarAvatar.src = e.target.result; }, 200);
                };
                reader.readAsDataURL(file);

                // Show file name indicator below button
                const shortName = file.name.length > 22 ? file.name.substring(0, 19) + '...' : file.name;
                const chooseBtn = avatarInput.nextElementSibling;
                let indicator = document.getElementById('avatarFileIndicator');
                if (!indicator) {
                    indicator = document.createElement('div');
                    indicator.id = 'avatarFileIndicator';
                    indicator.className = 'flex items-center gap-1 text-[10px] text-green-600 font-medium mt-1';
                    chooseBtn && chooseBtn.parentNode && chooseBtn.parentNode.insertBefore(indicator, chooseBtn.nextSibling);
                }
                indicator.innerHTML = '<i class="fa-solid fa-check-circle"></i> ' + shortName;
            }
        });
    }

    // Toggle password visibility
    function togglePasswordVisibility(inputId, button) {
        const input = document.getElementById(inputId);
        const icon = button.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }

    // Password strength logic
    const newPasswordInput = document.getElementById('newPassword');
    const strengthText = document.getElementById('strengthText');
    const seg1 = document.getElementById('strengthSegment1');
    const seg2 = document.getElementById('strengthSegment2');
    const seg3 = document.getElementById('strengthSegment3');

    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            const pwd = this.value;
            let score = 0;
            
            if (pwd.length >= 8) score++;
            if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
            if (/\d/.test(pwd)) score++;
            if (/[^a-zA-Z\d]/.test(pwd)) score++;
            
            seg1.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            seg2.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            seg3.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            
            if (pwd.length === 0) {
                strengthText.textContent = 'Trá»‘ng';
                strengthText.className = 'text-txt-3 font-bold';
                return;
            }
            if (pwd.length < 8) {
                strengthText.textContent = 'Ráº¥t ngáº¯n (Yáº¿u)';
                strengthText.className = 'text-red-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-red-500 rounded-full transition-all';
                return;
            }
            if (score <= 1) {
                strengthText.textContent = 'Yáº¿u';
                strengthText.className = 'text-red-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-red-500 rounded-full transition-all';
            } else if (score === 2 || score === 3) {
                strengthText.textContent = 'Trung bÃ¬nh';
                strengthText.className = 'text-orange-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-orange-500 rounded-full transition-all';
                seg2.className = 'h-full w-1/3 bg-orange-500 rounded-full transition-all';
            } else if (score >= 4) {
                strengthText.textContent = 'Máº¡nh';
                strengthText.className = 'text-green-600 font-bold';
                seg1.className = 'h-full w-1/3 bg-green-600 rounded-full transition-all';
                seg2.className = 'h-full w-1/3 bg-green-600 rounded-full transition-all';
                seg3.className = 'h-full w-1/3 bg-green-600 rounded-full transition-all';
            }
        });
    }

    const confirmPasswordInput = document.getElementById('confirmPassword');
    const matchText = document.getElementById('matchText');
    const passwordForm = document.getElementById('passwordForm');

    function checkPasswordMatch() {
        if (!confirmPasswordInput || !newPasswordInput) return true;
        if (confirmPasswordInput.value !== newPasswordInput.value && confirmPasswordInput.value.length > 0) {
            matchText.classList.remove('hidden');
            return false;
        } else {
            matchText.classList.add('hidden');
            return true;
        }
    }

    if (confirmPasswordInput) confirmPasswordInput.addEventListener('input', checkPasswordMatch);
    if (newPasswordInput) newPasswordInput.addEventListener('input', checkPasswordMatch);
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            if (!checkPasswordMatch()) {
                e.preventDefault();
                alert('Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p!');
                return;
            }
            if (newPasswordInput.value.length < 8) {
                e.preventDefault();
                alert('Máº­t kháº©u má»›i pháº£i tá»« 8 kÃ½ tá»± trá»Ÿ lÃªn!');
                return;
            }
        });
    }

    // Modal Address helpers
    const addressModal = document.getElementById('addressModal');
    function openAddressModalFromBtn(btn) {
        const id = btn.getAttribute('data-address-id') || '';
        const name = btn.getAttribute('data-recipient-name') || '';
        const phone = btn.getAttribute('data-recipient-phone') || '';
        const detail = btn.getAttribute('data-address-detail') || '';
        const isDefault = btn.getAttribute('data-is-default') === 'true';
        openAddressModal(id, name, phone, detail, isDefault);
    }
    function openAddressModal(id = '', name = '', phone = '', detail = '', isDefault = false) {
        document.getElementById('modalAddressId').value = id;
        document.getElementById('recipientName').value = name;
        document.getElementById('recipientPhone').value = phone;
        document.getElementById('addressDetail').value = detail;
        document.getElementById('isDefault').checked = isDefault;

        if (id) {
            document.getElementById('modalTitle').textContent = 'Chá»‰nh sá»­a Ä‘á»‹a chá»‰ giao hÃ ng';
            document.getElementById('modalAction').value = 'updateAddress';
        } else {
            document.getElementById('modalTitle').textContent = 'ThÃªm Ä‘á»‹a chá»‰ giao hÃ ng má»›i';
            document.getElementById('modalAction').value = 'addAddress';
        }

        addressModal.classList.remove('hidden');
        setTimeout(() => {
            addressModal.classList.remove('opacity-0');
            addressModal.querySelector('div').classList.remove('scale-95');
        }, 50);
    }

    function closeAddressModal() {
        addressModal.classList.add('opacity-0');
        addressModal.querySelector('div').classList.add('scale-95');
        setTimeout(() => {
            addressModal.classList.add('hidden');
        }, 300);
    }

    // Modal Order details & stepper tracking helpers
    const orderDetailModal = document.getElementById('orderDetailModal');
    function openOrderDetailModalFromBtn(btn) {
        const orderId = btn.getAttribute('data-order-id') || '';
        const status = btn.getAttribute('data-status') || '';
        const paymentMethod = btn.getAttribute('data-payment-method') || '';
        const deliveryAddress = btn.getAttribute('data-delivery-address') || '';
        const deliveryFee = btn.getAttribute('data-delivery-fee') || '0';
        const discountAmount = btn.getAttribute('data-discount-amount') || '0';
        const finalAmount = btn.getAttribute('data-final-amount') || '0';
        const notes = btn.getAttribute('data-notes') || '';
        const shopName = btn.getAttribute('data-shop-name') || '';
        openOrderDetailModal(orderId, status, paymentMethod, deliveryAddress, deliveryFee, discountAmount, finalAmount, notes, shopName);
    }
    function openOrderDetailModal(orderId, status, paymentMethod, deliveryAddress, deliveryFee, discountAmount, finalAmount, notes, shopName) {
        document.getElementById('detailShopName').textContent = shopName;
        document.getElementById('detailAddress').textContent = deliveryAddress;
        document.getElementById('detailPaymentMethod').textContent = paymentMethod === 'COD' ? 'COD (Nháº­n hÃ ng thanh toÃ¡n)' : 'Chuyá»ƒn khoáº£n (CK)';
        document.getElementById('detailNotes').textContent = notes ? notes : 'KhÃ´ng cÃ³ ghi chÃº';
        document.getElementById('detailFee').textContent = deliveryFee + 'Ä‘';
        document.getElementById('detailDiscount').textContent = discountAmount + 'Ä‘';
        document.getElementById('detailFinalAmount').textContent = finalAmount + 'Ä‘';

        // Clear stepper highlight
        const statuses = ['PENDING_PAYMENT', 'CONFIRMED', 'PREPARING', 'DISPATCHED', 'DELIVERED'];
        let activeIdx = statuses.indexOf(status);
        if (status === 'APPROVED') {
            activeIdx = 1; // Map APPROVED as CONFIRMED on stepper
        }
        if (status === 'CANCELLED' || status === 'PAYMENT_FAILED' || status === 'EXPIRED') {
            activeIdx = -1; // No highlight
        }

        // Stepper coloring
        statuses.forEach((st, idx) => {
            const node = document.getElementById('step-' + st);
            const iconBox = document.getElementById('step-icon-' + (idx + 1));
            const icon = iconBox.querySelector('i');
            
            if (idx <= activeIdx) {
                iconBox.className = "w-8 h-8 rounded-full flex items-center justify-center border-2 border-primary bg-primary-lt text-primary transition-colors";
                icon.className = icon.className.replace('text-gray-300', 'text-primary');
            } else {
                iconBox.className = "w-8 h-8 rounded-full flex items-center justify-center border-2 border-gray-200 bg-white transition-colors";
                icon.className = icon.className.replace('text-primary', 'text-gray-300');
            }
        });

        // Set stepper connector line color
        const line = document.getElementById('stepperLine');
        if (activeIdx >= 4) {
            line.className = "absolute top-3.5 left-[15%] right-[15%] h-0.5 bg-primary -z-10";
        } else {
            line.className = "absolute top-3.5 left-[15%] right-[15%] h-0.5 bg-gray-200 -z-10";
        }

        orderDetailModal.classList.remove('hidden');
        setTimeout(() => {
            orderDetailModal.classList.remove('opacity-0');
            orderDetailModal.querySelector('div').classList.remove('scale-95');
        }, 50);
    }

    function closeOrderDetailModal() {
        orderDetailModal.classList.add('opacity-0');
        orderDetailModal.querySelector('div').classList.add('scale-95');
        setTimeout(() => {
            orderDetailModal.classList.add('hidden');
        }, 300);
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
