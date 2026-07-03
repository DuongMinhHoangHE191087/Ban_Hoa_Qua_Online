<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Hồ sơ cá nhân" />
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
                Tài khoản cá nhân
            </h1>
            <p class="text-xs text-txt-2 mt-1">Quản lý thông tin tài khoản, địa chỉ nhận hàng, lịch sử đơn hàng và bảo mật.</p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/" class="inline-flex items-center gap-2 px-4 py-2 border border-gray-200 hover:border-primary text-xs font-bold text-txt-2 hover:text-primary rounded-xl bg-white shadow-sm hover:shadow transition-all duration-200 cursor-pointer">
                <i class="fa-solid fa-house"></i>
                Quay về trang chủ
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

    <c:if test="${user.status ne 'ACTIVE' or not empty user.lockedUntil}">
        <div class="mb-6 rounded-3xl border border-red-200 bg-gradient-to-br from-red-50 via-white to-rose-50 p-5 md:p-6 shadow-sm">
            <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                <div class="flex items-start gap-4">
                    <div class="w-12 h-12 rounded-2xl bg-red-100 text-red-700 flex items-center justify-center shrink-0">
                        <i class="fa-solid fa-lock text-lg"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-red-500">Trạng thái tài khoản</p>
                        <c:choose>
                            <c:when test="${not empty user.lockedUntil}">
                                <h2 class="text-lg md:text-xl font-bold text-red-800 mt-1">Tài khoản đang bị khóa tạm thời</h2>
                                <p class="text-sm text-red-700 mt-2 leading-6">
                                    Hệ thống đã khóa tạm thời tài khoản của bạn do thao tác đăng nhập sai nhiều lần. Bạn có thể thử lại sau hoặc dùng các tùy chọn hỗ trợ bên dưới.
                                </p>
                            </c:when>
                            <c:when test="${user.status == 'SUSPENDED'}">
                                <h2 class="text-lg md:text-xl font-bold text-red-800 mt-1">Tài khoản đang bị đình chỉ</h2>
                                <p class="text-sm text-red-700 mt-2 leading-6">
                                    Tài khoản hiện đang bị tạm ngưng bởi quản trị viên. Một số chức năng sẽ bị hạn chế cho đến khi trạng thái được khôi phục.
                                </p>
                            </c:when>
                            <c:otherwise>
                                <h2 class="text-lg md:text-xl font-bold text-red-800 mt-1">Tài khoản đang bị khóa</h2>
                                <p class="text-sm text-red-700 mt-2 leading-6">
                                    Tài khoản của bạn hiện không ở trạng thái hoạt động. Vui lòng liên hệ hỗ trợ để được xem xét mở lại.
                                </p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="flex flex-wrap gap-2">
                    <span class="inline-flex items-center gap-1.5 rounded-full bg-white px-3 py-1 text-[10px] font-bold text-red-700 border border-red-200">
                        <i class="fa-solid fa-shield-halved"></i>
                        <c:out value="${user.status}"/>
                    </span>
                    <span class="inline-flex items-center gap-1.5 rounded-full bg-white px-3 py-1 text-[10px] font-bold text-slate-600 border border-slate-200">
                        <i class="fa-solid fa-user-tag"></i>
                        <c:out value="${user.role}"/>
                    </span>
                </div>
            </div>
            <div class="mt-4 flex flex-wrap gap-3">
                <a href="mailto:support@metafruit.com" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover">
                    <i class="fa-solid fa-headset"></i>
                    Liên hệ hỗ trợ
                </a>
                <a href="${pageContext.request.contextPath}/auth/forgot" class="inline-flex items-center gap-2 rounded-xl border border-red-200 bg-white px-4 py-2.5 text-xs font-bold text-red-700 transition-all hover:border-red-300 hover:bg-red-50">
                    <i class="fa-solid fa-key"></i>
                    Khôi phục mật khẩu
                </a>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty shopProfile and shopProfile.approvalStatus == 'SUSPENDED'}">
        <div class="mb-6 rounded-3xl border border-red-200 bg-gradient-to-br from-red-50 via-white to-rose-50 p-5 md:p-6 shadow-sm">
            <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                <div class="flex items-start gap-4">
                    <div class="w-12 h-12 rounded-2xl bg-red-100 text-red-700 flex items-center justify-center shrink-0">
                        <i class="fa-solid fa-store text-lg"></i>
                    </div>
                    <div class="min-w-0">
                        <div class="flex flex-wrap items-center gap-3">
                            <span class="inline-flex items-center gap-1.5 rounded-full bg-white px-3 py-1 text-[10px] font-bold text-red-700 border border-red-200">
                                Trạng thái gian hàng
                            </span>
                            <span class="text-[10px] font-bold uppercase tracking-[0.22em] text-red-500">Shop owner status</span>
                        </div>
                        <h2 class="text-lg md:text-xl font-bold text-red-800 mt-1">Gian hàng đang bị đình chỉ</h2>
                        <p class="text-sm text-red-700 mt-2 leading-6">
                            <c:out value="${not empty shopProfile.shopName ? shopProfile.shopName : 'Gian hàng của bạn'}"/> đang tạm dừng hoạt động. Bạn có thể xem chi tiết trạng thái và liên hệ hỗ trợ để được kiểm tra lại.
                        </p>
                    </div>
                </div>
                <div class="flex flex-wrap gap-2">
                    <span class="inline-flex items-center gap-1.5 rounded-full bg-white px-3 py-1 text-[10px] font-bold text-red-700 border border-red-200">
                        <i class="fa-solid fa-lock"></i>
                        <c:out value="${shopProfile.approvalStatus}"/>
                    </span>
                    <span class="inline-flex items-center gap-1.5 rounded-full bg-white px-3 py-1 text-[10px] font-bold text-slate-600 border border-slate-200">
                        <i class="fa-solid fa-store"></i>
                        <c:out value="${shopProfile.shopName}"/>
                    </span>
                </div>
            </div>
            <div class="mt-4 flex flex-wrap gap-3">
                <a href="${pageContext.request.contextPath}/shop/status" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover">
                    <i class="fa-solid fa-clipboard-list"></i>
                    Xem chi tiết trạng thái
                </a>
                <a href="mailto:support@metafruit.com" class="inline-flex items-center gap-2 rounded-xl border border-red-200 bg-white px-4 py-2.5 text-xs font-bold text-red-700 transition-all hover:border-red-300 hover:bg-red-50">
                    <i class="fa-solid fa-headset"></i>
                    Liên hệ hỗ trợ
                </a>
            </div>
            <c:if test="${not empty shopProfile.rejectionReason}">
                <div class="mt-4 rounded-2xl border border-red-200 bg-white/80 p-4">
                    <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-red-700">Lý do trạng thái</p>
                    <p class="mt-2 text-xs text-red-700 leading-6"><c:out value="${shopProfile.rejectionReason}"/></p>
                </div>
            </c:if>
        </div>
    </c:if>

    <div class="flex flex-col lg:flex-row gap-8">
        
        <!-- Sidebar Navigation -->
        <aside class="w-full lg:w-64 shrink-0">
            <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm sticky top-24">
                <div class="flex items-center gap-3 pb-5 mb-5 border-b border-gray-100">
                    <div class="w-12 h-12 rounded-full overflow-hidden border border-primary/20 shrink-0 flex items-center justify-center">
                        <c:choose>
                            <c:when test="${not empty user.avatarUrl}">
                                <img class="w-full h-full object-cover" 
                                     src="${fn:startsWith(user.avatarUrl, 'http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)}" 
                                     alt="Avatar"
                                     onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                <div class="avatar-fallback is-hidden bg-gradient-to-br from-[#14532d] to-[#166534] text-white font-bold text-[18px] uppercase rounded-full">
                                    <c:out value="${fn:substring(user.fullName, 0, 1)}"/>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="avatar-fallback bg-gradient-to-br from-[#14532d] to-[#166534] text-white font-bold text-[18px] uppercase rounded-full">
                                    <c:out value="${fn:substring(user.fullName, 0, 1)}"/>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="overflow-hidden">
                        <h3 class="text-sm font-bold text-txt truncate"><c:out value="${user.fullName}"/></h3>
                        <span class="text-[10px] text-txt-3"><c:out value="${user.email}"/></span>
                    </div>
                </div>

                <h2 class="text-[10px] font-bold uppercase tracking-wider text-txt-3 mb-3 px-2">Menu quản lý</h2>
                <nav class="flex flex-col gap-1" id="profile-tabs">
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left bg-primary text-white" 
                            data-tab="profile-tab">
                        <i class="fa-solid fa-user text-sm w-4 text-center"></i>
                        <span class="flex-1">Thông tin cá nhân</span>
                    </button>
                    <c:if test="${user.role != 'ADMIN'}">
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="address-tab">
                            <i class="fa-solid fa-map-location-dot text-sm w-4 text-center"></i>
                            <span class="flex-1">Sổ địa chỉ</span>
                            <c:if test="${not empty addresses}"><span class="ml-auto px-1.5 py-0.5 bg-gray-200 text-txt-3 rounded text-[9px] font-bold">${fn:length(addresses)}</span></c:if>
                        </button>
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="orders-tab">
                            <i class="fa-solid fa-box text-sm w-4 text-center"></i>
                            <span class="flex-1">Đơn hàng của tôi</span>
                            <c:if test="${not empty orders}"><span class="ml-auto px-1.5 py-0.5 bg-primary/10 text-primary rounded text-[9px] font-bold">${fn:length(orders)}</span></c:if>
                        </button>
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="payments-tab">
                            <i class="fa-solid fa-credit-card text-sm w-4 text-center"></i>
                            <span class="flex-1">Lịch sử thanh toán</span>
                            <c:if test="${not empty payments}"><span class="ml-auto px-1.5 py-0.5 bg-blue-100 text-blue-700 rounded text-[9px] font-bold">${fn:length(payments)}</span></c:if>
                        </button>
                    </c:if>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="security-tab">
                        <i class="fa-solid fa-shield-halved text-sm w-4 text-center"></i>
                        <span class="flex-1">Đổi mật khẩu</span>
                    </button>
                </nav>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="flex-1">
            
            <!-- 1. Profile Info Tab -->
            <div id="profile-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-user text-primary"></i> Thông tin tài khoản
                </h2>
                
                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data" class="space-y-6">
                    <input type="hidden" name="action" value="updateProfile">
                    <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">

                    <!-- Avatar Upload -->
                    <div class="flex flex-col sm:flex-row items-center gap-6 pb-6 border-b border-gray-100">
                        <div class="relative w-24 h-24 rounded-full overflow-hidden border border-primary/20 shadow-sm cursor-pointer group shrink-0" 
                             onclick="document.getElementById('avatarInput').click();" 
                             title="Click để đổi ảnh đại diện">
                            <img id="avatarPreview" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" 
                                 src="${not empty user.avatarUrl ? (fn:startsWith(user.avatarUrl, 'http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)) : pageContext.request.contextPath.concat('/assets/images/default-avatar.svg')}" 
                                 alt="Avatar"
                                 onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/assets/images/default-avatar.svg';">
                            <div class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                <i class="fa-solid fa-camera text-base"></i>
                            </div>
                        </div>
                        <div class="text-center sm:text-left space-y-1.5">
                            <h3 class="text-sm font-bold text-txt"><c:out value="${user.fullName}"/></h3>
                            <div class="flex items-center justify-center sm:justify-start gap-2">
                                <span class="text-[10px] text-txt-3">Vai trò:</span>
                                <span class="px-2 py-0.5 bg-primary-lt text-primary border border-primary/10 rounded-full text-[9px] font-bold uppercase"><c:out value="${user.role}"/></span>
                            </div>
                            <input type="file" name="avatar" id="avatarInput" accept="image/jpeg,image/png,image/webp" class="hidden">
                            <button type="button" class="px-3 py-1.5 border border-border hover:border-primary text-[10px] font-bold text-txt-2 hover:text-primary rounded-lg transition-colors cursor-pointer" 
                                    onclick="document.getElementById('avatarInput').click();">Chọn ảnh mới</button>
                        </div>
                    </div>

                    <!-- Personal Information Form fields -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="fullName">Họ và tên <span class="text-red-500">*</span></label>
                            <input type="text" id="fullName" name="fullName" value="<c:out value="${user.fullName}"/>" required
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="phone">Số điện thoại</label>
                            <input type="text" id="phone" name="phone" value="<c:out value="${user.phone}"/>" placeholder="Ví dụ: 0987654321"
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                    </div>

                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-txt-3" for="email">Địa chỉ Email (Tên đăng nhập)</label>
                        <input type="email" id="email" name="email" value="<c:out value="${user.email}"/>" disabled
                               class="w-full px-4 py-2 bg-gray-50 border border-border rounded-xl text-xs text-txt-3 cursor-not-allowed">
                    </div>

                    <div class="flex justify-end pt-4">
                        <button type="submit" class="flex items-center gap-2 px-5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>

                <h2 class="text-[10px] font-bold uppercase tracking-wider text-txt-3 mb-3 px-2">Menu quản lý</h2>
                <nav class="flex flex-col gap-1" id="profile-tabs">
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left bg-primary text-white" 
                            data-tab="profile-tab">
                        <i class="fa-solid fa-user text-sm w-4 text-center"></i>
                        <span class="flex-1">Thông tin cá nhân</span>
                    </button>
                    <c:if test="${user.role != 'ADMIN'}">
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="address-tab">
                            <i class="fa-solid fa-map-location-dot text-sm w-4 text-center"></i>
                            <span class="flex-1">Sổ địa chỉ</span>
                            <c:if test="${not empty addresses}"><span class="ml-auto px-1.5 py-0.5 bg-gray-200 text-txt-3 rounded text-[9px] font-bold">${fn:length(addresses)}</span></c:if>
                        </button>
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="orders-tab">
                            <i class="fa-solid fa-box text-sm w-4 text-center"></i>
                            <span class="flex-1">Đơn hàng của tôi</span>
                            <c:if test="${not empty orders}"><span class="ml-auto px-1.5 py-0.5 bg-primary/10 text-primary rounded text-[9px] font-bold">${fn:length(orders)}</span></c:if>
                        </button>
                        <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                                data-tab="payments-tab">
                            <i class="fa-solid fa-credit-card text-sm w-4 text-center"></i>
                            <span class="flex-1">Lịch sử thanh toán</span>
                            <c:if test="${not empty payments}"><span class="ml-auto px-1.5 py-0.5 bg-blue-100 text-blue-700 rounded text-[9px] font-bold">${fn:length(payments)}</span></c:if>
                        </button>
                    </c:if>
                    <button class="tab-btn w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-xs font-bold transition-all duration-200 text-left text-txt-2 hover:bg-primary-lt hover:text-primary" 
                            data-tab="security-tab">
                        <i class="fa-solid fa-shield-halved text-sm w-4 text-center"></i>
                        <span class="flex-1">Đổi mật khẩu</span>
                    </button>
                </nav>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="flex-1">
            
            <!-- 1. Profile Info Tab -->
            <div id="profile-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-user text-primary"></i> Thông tin tài khoản
                </h2>
                
                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data" class="space-y-6">
                    <input type="hidden" name="action" value="updateProfile">
                    <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">

                    <!-- Avatar Upload -->
                    <div class="flex flex-col sm:flex-row items-center gap-6 pb-6 border-b border-gray-100">
                        <div class="relative w-24 h-24 rounded-full overflow-hidden border border-primary/20 shadow-sm cursor-pointer group shrink-0" 
                             onclick="document.getElementById('avatarInput').click();" 
                             title="Click để đổi ảnh đại diện">
                            <img id="avatarPreview" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" 
                                 src="${not empty user.avatarUrl ? (fn:startsWith(user.avatarUrl, 'http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)) : pageContext.request.contextPath.concat('/assets/images/default-avatar.svg')}" 
                                 alt="Avatar"
                                 onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/assets/images/default-avatar.svg';">
                            <div class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                <i class="fa-solid fa-camera text-base"></i>
                            </div>
                        </div>
                        <div class="text-center sm:text-left space-y-1.5">
                            <h3 class="text-sm font-bold text-txt"><c:out value="${user.fullName}"/></h3>
                            <div class="flex items-center justify-center sm:justify-start gap-2">
                                <span class="text-[10px] text-txt-3">Vai trò:</span>
                                <span class="px-2 py-0.5 bg-primary-lt text-primary border border-primary/10 rounded-full text-[9px] font-bold uppercase"><c:out value="${user.role}"/></span>
                            </div>
                            <input type="file" name="avatar" id="avatarInput" accept="image/jpeg,image/png,image/webp" class="hidden">
                            <button type="button" class="px-3 py-1.5 border border-border hover:border-primary text-[10px] font-bold text-txt-2 hover:text-primary rounded-lg transition-colors cursor-pointer" 
                                    onclick="document.getElementById('avatarInput').click();">Chọn ảnh mới</button>
                        </div>
                    </div>

                    <!-- Personal Information Form fields -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="fullName">Họ và tên <span class="text-red-500">*</span></label>
                            <input type="text" id="fullName" name="fullName" value="<c:out value="${user.fullName}"/>" required
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="phone">Số điện thoại</label>
                            <input type="text" id="phone" name="phone" value="<c:out value="${user.phone}"/>" placeholder="Ví dụ: 0987654321"
                                   class="w-full px-4 py-2 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                        </div>
                    </div>

                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-txt-3" for="email">Địa chỉ Email (Tên đăng nhập)</label>
                        <input type="email" id="email" name="email" value="<c:out value="${user.email}"/>" disabled
                               class="w-full px-4 py-2 bg-gray-50 border border-border rounded-xl text-xs text-txt-3 cursor-not-allowed">
                    </div>

                    <div class="flex justify-end pt-4">
                        <button type="submit" class="flex items-center gap-2 px-5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>

            <c:if test="${user.role != 'ADMIN'}">
            <!-- 2. Address Book Tab -->
            <div id="address-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <div class="flex justify-between items-center mb-6 pb-3 border-b border-gray-100">
                    <h2 class="text-base font-bold text-txt flex items-center gap-2">
                        <i class="fa-solid fa-map-location-dot text-primary"></i> Sổ địa chỉ giao hàng
                    </h2>
                    <button onclick="openAddressModal()" class="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">
                        <i class="fa-solid fa-plus"></i> Thêm địa chỉ mới
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
                                                <span class="px-2 py-0.5 bg-primary/10 text-primary border border-primary/20 rounded text-[9px] font-bold">Mặc định</span>
                                            </c:if>
                                        </div>
                                        <p class="text-xs text-txt-2 leading-relaxed"><c:out value="${addr.addressDetail}"/></p>
                                    </div>
                                    <div class="flex items-center gap-2 shrink-0">
                                        <c:if test="${not addr['default']}">
                                            <form action="${pageContext.request.contextPath}/profile" method="post" class="inline">
                                                <input type="hidden" name="action" value="setDefaultAddress">
                                                <input type="hidden" name="addressId" value="${addr.addressId}">
                                                <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">
                                                <button type="submit" class="px-2 py-1 text-[9px] border border-gray-200 text-txt-2 hover:border-primary hover:text-primary rounded transition-colors cursor-pointer bg-white">Đặt mặc định</button>
                                            </form>
                                        </c:if>
                                        <button data-address-id="${addr.addressId}"
                                                data-recipient-name="<c:out value='${addr.recipientName}'/>"
                                                data-recipient-phone="<c:out value='${addr.recipientPhone}'/>"
                                                data-address-detail="<c:out value='${addr.addressDetail}'/>"
                                                data-is-default="${addr['default']}"
                                                onclick="openAddressModalFromBtn(this)" 
                                                class="p-1.5 border border-gray-200 text-txt-2 hover:border-blue-500 hover:text-blue-500 rounded bg-white transition-colors cursor-pointer" title="Sửa">
                                            <i class="fa-solid fa-pen text-xs"></i>
                                        </button>
                                        <c:if test="${not addr['default']}">
                                            <form action="${pageContext.request.contextPath}/profile" method="post" class="inline" onsubmit="return confirmDeleteAddress(event)">
                                                <input type="hidden" name="action" value="deleteAddress">
                                                <input type="hidden" name="addressId" value="${addr.addressId}">
                                                <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">
                                                <button type="submit" class="p-1.5 border border-gray-200 text-txt-2 hover:border-red-500 hover:text-red-500 rounded bg-white transition-colors cursor-pointer" title="Xóa">
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
                                <p class="text-xs text-txt-3">Chưa lưu địa chỉ giao hàng nào. Vui lòng thêm địa chỉ nhận hàng!</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>


            <!-- 3. Orders List and Tracking Tab -->
            <div id="orders-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <div class="flex flex-wrap items-center justify-between gap-4 mb-6 pb-3 border-b border-gray-100">
                    <h2 class="text-base font-bold text-txt flex items-center gap-2">
                        <i class="fa-solid fa-box text-primary"></i> Đơn hàng của tôi
                    </h2>
                    <!-- Status Filter Tabs -->
                    <div class="flex flex-wrap gap-1" id="order-filter-tabs">
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-primary bg-primary text-white transition-all" data-status="ALL">Tất cả</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-amber-400 hover:text-amber-600 transition-all" data-status="PENDING_PAYMENT">Chờ TT</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-blue-400 hover:text-blue-600 transition-all" data-status="CONFIRMED">Đã nhận</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-purple-400 hover:text-purple-600 transition-all" data-status="DISPATCHED">Đang giao</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-green-400 hover:text-green-600 transition-all" data-status="DELIVERED">Đã giao</button>
                        <button class="order-filter-btn px-2.5 py-1 text-[9px] font-bold rounded-lg border border-gray-200 text-txt-2 hover:border-red-400 hover:text-red-600 transition-all" data-status="CANCELLED">Đã hủy</button>
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
                                            <span class="text-xs font-bold text-primary">Đơn hàng #<c:out value="${ord.orderId}"/></span>
                                            <span class="text-[10px] text-txt-3">Đặt ngày: <fmt:parseDate value="${ord.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" /><fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm"/></span>
                                        </div>
                                        <div class="flex items-center gap-2.5">
                                            <!-- Vietnamese status labels with icons -->
                                            <c:choose>
                                                <c:when test="${ord.status == 'PENDING_PAYMENT'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-amber-50 text-amber-700 border-amber-200">
                                                        <i class="fa-solid fa-clock mr-0.5"></i> Chờ thanh toán
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'CONFIRMED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-blue-50 text-blue-700 border-blue-200">
                                                        <i class="fa-solid fa-clipboard-check mr-0.5"></i> Đã xác nhận
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'PREPARING'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-indigo-50 text-indigo-700 border-indigo-200">
                                                        <i class="fa-solid fa-box-open mr-0.5"></i> Đang chuẩn bị
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'DISPATCHED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-purple-50 text-purple-700 border-purple-200">
                                                        <i class="fa-solid fa-truck-fast mr-0.5"></i> Đang giao hàng
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'DELIVERED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-emerald-50 text-emerald-700 border-emerald-200">
                                                        <i class="fa-solid fa-circle-check mr-0.5"></i> Đã giao thành công
                                                    </span>
                                                </c:when>
                                                <c:when test="${ord.status == 'CANCELLED'}">
                                                    <span class="px-2.5 py-0.5 border rounded-full text-[9px] font-bold bg-red-50 text-red-700 border-red-200">
                                                        <i class="fa-solid fa-xmark mr-0.5"></i> Đã hủy
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

                                        <!-- Content -->
                                        <div class="p-4">
                                            <!-- Shop name -->
                                            <div class="text-[10px] text-txt-3 flex items-center gap-1.5 mb-3">
                                                <i class="fa-solid fa-store"></i>
                                                <span>Cửa hàng: <span class="font-semibold text-txt-2"><c:out value="${shopNamesMap[ord.orderId]}"/></span></span>
                                            </div>

                                            <!-- All items -->
                                            <div class="space-y-1.5 mb-4">
                                                <c:forEach var="item" items="${orderItemsMap[ord.orderId]}">
                                                    <div class="flex items-center justify-between text-xs text-txt-2">
                                                        <span class="flex items-center gap-1.5">
                                                            <span class="w-1.5 h-1.5 rounded-full bg-primary/40 shrink-0"></span>
                                                            <c:out value="${item.productNameSnapshot}"/>
                                                            <c:if test="${not empty item.variantLabelSnapshot}">
                                                                <span class="text-txt-3">(${item.variantLabelSnapshot})</span>
                                                            </c:if>
                                                            <span class="text-txt-3">x${item.quantity}</span>
                                                        </span>
                                                        <span class="font-medium text-txt shrink-0 ml-4">
                                                            <fmt:formatNumber value="${item.subtotal}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                                        </span>
                                                    </div>
                                                </c:forEach>
                                            </div>

                                            <!-- Footer: total + actions -->
                                            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pt-3 border-t border-gray-100">
                                                <div class="text-xs">
                                                    <span class="text-txt-3">Tổng số tiền:</span>
                                                    <span class="ml-1 font-bold text-primary text-sm">
                                                        <fmt:formatNumber value="${ord.finalAmount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                                    </span>
                                                </div>
                                                <div class="flex items-center gap-2 flex-wrap">
                                                    <!-- View detail page -->
                                                    <a href="${pageContext.request.contextPath}/profile/order-detail?orderId=${ord.orderId}"
                                                       class="px-3 py-1.5 bg-white border border-gray-200 text-txt-2 hover:border-primary hover:text-primary rounded-lg text-[9px] font-bold transition-all flex items-center gap-1">
                                                        <i class="fa-solid fa-file-lines"></i> Chi tiết &amp; Vận đơn
                                                    </a>
                                                    <!-- Invoice PDF - DELIVERED only -->
                                                    <c:if test="${ord.status == 'DELIVERED'}">
                                                        <a href="${pageContext.request.contextPath}/reviews?orderId=${ord.orderId}"
                                                           class="px-3 py-1.5 bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary rounded-lg text-[9px] font-bold transition-all flex items-center gap-1 shadow-sm">
                                                            <i class="fa-solid fa-star"></i> Viết đánh giá
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/orders?action=invoice&orderId=${ord.orderId}"
                                                           target="_blank"
                                                           class="px-3 py-1.5 bg-emerald-50 border border-emerald-200 text-emerald-700 hover:bg-emerald-100 rounded-lg text-[9px] font-bold transition-all flex items-center gap-1">
                                                            <i class="fa-solid fa-file-invoice"></i> Tải hoá đơn
                                                        </a>
                                                    </c:if>
                                                    <!-- Cancel - PENDING_PAYMENT only -->
                                                    <c:if test="${ord.status == 'PENDING_PAYMENT'}">
                                                        <form action="${pageContext.request.contextPath}/profile" method="post" class="inline"
                                                              onsubmit="return confirmCancelOrder(event, '${ord.orderId}')">
                                                            <input type="hidden" name="action" value="cancelOrder">
                                                            <input type="hidden" name="orderId" value="${ord.orderId}">
                                                            <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">
                                                            <button type="submit" class="px-3 py-1.5 bg-white border border-red-200 text-red-500 hover:bg-red-50 hover:border-red-400 rounded-lg text-[9px] font-bold transition-all cursor-pointer">
                                                                <i class="fa-solid fa-ban mr-0.5"></i> Hủy đơn hàng
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-10 border-2 border-dashed border-gray-100 rounded-2xl">
                                <i class="fa-solid fa-bag-shopping text-gray-300 text-3xl mb-3"></i>
                                <p class="text-xs text-txt-3">Bạn chưa đặt đơn hàng nào trên MetaFruit.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Empty filter result placeholder -->
                    <div id="orders-empty-filter" class="hidden text-center py-10 border-2 border-dashed border-gray-100 rounded-2xl">
                        <i class="fa-solid fa-filter-circle-xmark text-gray-300 text-3xl mb-3"></i>
                        <p class="text-xs text-txt-3">Không có đơn hàng nào theo bộ lọc này.</p>
                    </div>
                </div>
            </div>

            <div id="payments-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-credit-card text-primary"></i> Lịch sử giao dịch & Thanh toán
                </h2>

                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse text-xs">
                        <thead>
                            <tr class="bg-gray-50 border-b border-gray-100 text-txt-2 font-bold">
                                <th class="p-3">Giao dịch</th>
                                <th class="p-3">Đơn hàng</th>
                                <th class="p-3">Hình thức</th>
                                <th class="p-3">Mã GD</th>
                                <th class="p-3">Số tiền</th>
                                <th class="p-3">Thời gian</th>
                                <th class="p-3">Trạng thái</th>
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
                                            <td class="p-3 font-bold text-txt"><fmt:formatNumber value="${pay.amount}" type="currency" currencySymbol="đ" maxFractionDigits="0"/></td>
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
                                        <td colspan="7" class="text-center py-6 text-txt-3">Chưa phát sinh giao dịch thanh toán nào.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
            </c:if>

            <!-- 5. Password tab -->
            <div id="security-tab" class="tab-content bg-white border border-gray-100 rounded-2xl p-6 md:p-8 shadow-sm hidden">
                <h2 class="text-base font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-shield-halved text-primary"></i> Đổi mật khẩu bảo mật
                </h2>

                <c:choose>
                    <c:when test="${empty user.passwordHash}">
                        <div class="p-4 bg-emerald-50/50 border border-primary/20 rounded-xl text-primary text-xs font-medium flex items-center gap-2.5">
                            <i class="fa-solid fa-circle-info text-base"></i> 
                            <span>Tài khoản này được đăng nhập trực tiếp qua <strong>Google OAuth</strong>. Mật khẩu không áp dụng.</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/profile" method="post" id="passwordForm" class="space-y-6">
                            <input type="hidden" name="action" value="changePassword">
                            <input type="hidden" name="_csrf" value="${sessionScope['_csrfToken']}">

                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-bold text-txt-2" for="currentPassword">Mật khẩu hiện tại <span class="text-red-500">*</span></label>
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
                                    <label class="text-xs font-bold text-txt-2" for="newPassword">Mật khẩu mới <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="newPassword" name="newPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('newPassword', this)">
                                            <i class="fa-solid fa-eye text-xs"></i>
                                        </button>
                                    </div>
                                    <div class="text-[10px] text-txt-3">Độ dài từ 8 đến 64 ký tự.</div>
                                    <div class="mt-2 space-y-1.5">
                                        <div class="flex items-center justify-between text-[9px] font-semibold">
                                            <span class="text-txt-3">Độ mạnh mật khẩu:</span>
                                            <span id="strengthText" class="text-txt-3 font-bold">Trống</span>
                                        </div>
                                        <div class="w-full bg-gray-200 h-1.5 rounded-full overflow-hidden flex gap-1">
                                            <div id="strengthSegment1" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment2" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment3" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                        </div>
                                    </div>
                                </div>

                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-txt-2" for="confirmPassword">Xác nhận mật khẩu mới <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="confirmPassword" name="confirmPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-xs focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('confirmPassword', this)">
                                            <i class="fa-solid fa-eye text-xs"></i>
                                        </button>
                                    </div>
                                    <span id="matchText" class="text-[9px] text-red-600 hidden">Mật khẩu xác nhận không khớp!</span>
                                </div>
                            </div>

                            <div class="flex justify-end pt-4">
                                <button type="submit" id="submitPasswordBtn" class="flex items-center gap-2 px-5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                                    <i class="fa-solid fa-key"></i> Đặt lại mật khẩu
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
            <h3 id="modalTitle" class="text-sm font-bold text-txt">Thêm địa chỉ nhận hàng mới</h3>
            <button onclick="closeAddressModal()" class="text-txt-3 hover:text-txt border-0 bg-transparent cursor-pointer"><i class="fa-solid fa-xmark text-base"></i></button>
        </div>

        <form action="${pageContext.request.contextPath}/profile" method="post" class="space-y-4">
            <input type="hidden" name="action" id="modalAction" value="addAddress">
            <input type="hidden" name="addressId" id="modalAddressId" value="">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="recipientName">Họ và tên người nhận *</label>
                <input type="text" id="recipientName" name="recipientName" required
                       class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary">
            </div>

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="recipientPhone">Số điện thoại nhận hàng *</label>
                <input type="text" id="recipientPhone" name="recipientPhone" required
                       class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary">
            </div>

            <div class="flex flex-col gap-1.5">
                <label class="text-[10px] font-bold text-txt-2" for="addressDetail">Địa chỉ chi tiết *</label>
                <textarea id="addressDetail" name="addressDetail" rows="3" required placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố..."
                          class="w-full px-3 py-2 border border-border rounded-lg text-xs focus:outline-none focus:border-primary resize-none"></textarea>
            </div>

            <div class="flex items-center gap-2">
                <input type="checkbox" id="isDefault" name="isDefault" value="true" class="rounded text-primary focus:ring-primary">
                <label for="isDefault" class="text-[10px] text-txt-2 font-medium cursor-pointer">Đặt làm địa chỉ nhận hàng mặc định</label>
            </div>

            <div class="flex justify-end gap-2.5 pt-3 border-t border-gray-100">
                <button type="button" onclick="closeAddressModal()" class="px-4 py-2 border border-gray-200 hover:bg-gray-50 text-[10px] font-bold text-txt-2 rounded-lg transition-colors cursor-pointer bg-white">Hủy bỏ</button>
                <button type="submit" class="px-4 py-2 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">Lưu địa chỉ</button>
            </div>
        </form>
    </div>
</div>

<div id="orderDetailModal" class="fixed inset-0 bg-black/50 backdrop-blur-[2px] flex items-center justify-center z-50 hidden opacity-0 transition-opacity duration-300">
    <div class="bg-white rounded-2xl p-6 w-full max-w-lg border border-gray-100 shadow-xl transform scale-95 transition-transform duration-300">
        <div class="flex justify-between items-center pb-3 border-b border-gray-100 mb-4">
            <h3 class="text-sm font-bold text-txt">Chi tiết đơn hàng</h3>
            <button onclick="closeOrderDetailModal()" class="text-txt-3 hover:text-txt border-0 bg-transparent cursor-pointer"><i class="fa-solid fa-xmark text-base"></i></button>
        </div>
        <div id="orderDetailContent" class="space-y-4 max-h-[60vh] overflow-y-auto pr-2 text-xs">
            <!-- Dynamic content loaded via JS -->
            <div class="flex justify-between items-center pt-3 border-t border-gray-100">
                <span class="text-txt-2">Tổng thanh toán:</span>
                <span class="text-primary" id="detailFinalAmount">0đ</span>
            </div>
            
            <!-- Close action -->
            <div class="flex justify-end pt-3 border-t border-gray-100">
                <button type="button" onclick="closeOrderDetailModal()" class="px-5 py-2 bg-primary hover:bg-primary-hover text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer">
                    Đóng cửa sổ
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

    // Auto-select tab from URL query parameter
    (function() {
        const urlParams = new URLSearchParams(window.location.search);
        let tabParam = urlParams.get('tab');
        if (tabParam) {
            if (!tabParam.endsWith('-tab')) {
                tabParam = tabParam + '-tab';
            }
            const targetBtn = document.querySelector(`#profile-tabs button[data-tab="\${tabParam}"]`);
            if (targetBtn) {
                targetBtn.click();
            }
        }
    })();

    // Order filter buttons — client-side filter for order cards
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

    // Preview avatar image before uploading — enhanced with animation
    const avatarInput = document.getElementById('avatarInput');
    if (avatarInput) {
        avatarInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                if (file.size > 5 * 1024 * 1024) {
                    alert('File ảnh không được vượt quá 5MB!');
                    event.target.value = '';
                    return;
                }
                const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
                if (!allowedTypes.includes(file.type)) {
                    alert('Chỉ chấp nhận các định dạng ảnh: JPG, PNG, WEBP');
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
                strengthText.textContent = 'Trống';
                strengthText.className = 'text-txt-3 font-bold';
                return;
            }
            if (pwd.length < 8) {
                strengthText.textContent = 'Rất ngắn (Yếu)';
                strengthText.className = 'text-red-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-red-500 rounded-full transition-all';
                return;
            }
            if (score <= 1) {
                strengthText.textContent = 'Yếu';
                strengthText.className = 'text-red-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-red-500 rounded-full transition-all';
            } else if (score === 2 || score === 3) {
                strengthText.textContent = 'Trung bình';
                strengthText.className = 'text-orange-500 font-bold';
                seg1.className = 'h-full w-1/3 bg-orange-500 rounded-full transition-all';
                seg2.className = 'h-full w-1/3 bg-orange-500 rounded-full transition-all';
            } else if (score >= 4) {
                strengthText.textContent = 'Mạnh';
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
                alert('Mật khẩu xác nhận không khớp!');
                return;
            }
            if (newPasswordInput.value.length < 8) {
                e.preventDefault();
                alert('Mật khẩu mới phải từ 8 ký tự trở lên!');
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
            document.getElementById('modalTitle').textContent = 'Chỉnh sửa địa chỉ giao hàng';
            document.getElementById('modalAction').value = 'updateAddress';
        } else {
            document.getElementById('modalTitle').textContent = 'Thêm địa chỉ nhận hàng mới';
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

    function confirmDeleteAddress(event) {
        event.preventDefault();
        Swal.fire({
            title: 'Xóa địa chỉ giao hàng?',
            text: 'Bạn có chắc chắn muốn xóa địa chỉ giao hàng này không?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ba1a1a',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đồng ý xóa',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }

    function confirmCancelOrder(event, orderId) {
        event.preventDefault();
        Swal.fire({
            title: 'Hủy đơn hàng?',
            html: 'Bạn chắc chắn muốn hủy đơn hàng <b>#' + orderId + '</b>? Hành động này không thể hoàn tác!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ba1a1a',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đồng ý hủy',
            cancelButtonText: 'Không'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />

</content>
