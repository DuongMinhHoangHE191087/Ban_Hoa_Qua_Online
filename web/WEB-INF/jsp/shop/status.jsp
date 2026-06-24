<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Tr?ng thái gian hàng - MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Material Symbols Outlined -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">
    <!-- Tailwind CSS -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
    <script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#14532D",
                        "primary-hover": "#166534",
                        "primary-light": "#d1ffd8",
                        "surface": "#eaffea",
                        "on-surface": "#00210d",
                        "on-surface-variant": "#44483b",
                        "outline": "#75796a",
                        "outline-variant": "#c5c8b7",
                        "error": "#ba1a1a"
                    },
                    borderRadius: {
                        "lg": "0.75rem",
                        "xl": "1.25rem",
                        "2xl": "1.5rem"
                    },
                    fontFamily: {
                        sans: ["Lexend", "sans-serif"]
                    }
                }
            }
        }
    </script>
    <style>
        body {
            font-family: 'Lexend', sans-serif;
        }
        .glass-card {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.5);
            box-shadow: 0 20px 50px rgba(20, 83, 45, 0.12);
        }
        ::-webkit-scrollbar {
            width: 6px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: #14532D;
            border-radius: 9999px;
        }
    </style>
</head>
<body class="bg-emerald-50 text-on-surface min-h-screen flex flex-col antialiased relative">

    <!-- Decorative Organic Background -->
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
        <div class="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-multiply" 
             style="background-image: url('https://lh3.googleusercontent.com/aida-public/AB6AXuDbzTRH5MPfxXQnED9OhayiGIhydHTVZL2CgybXiVn-iGcwBhA-qLCSGyekLQAVcm_RUpEDJtEv1_dACfRuWo4Utwsq8I5P2LdCjSPImoyUi9-ZwkMLix_Tor9bQei6zL2uFzVk6hMIf55qGhWqNDePckWeNBL3FpIcPmUalFvXnu98oImfdEpYZ05NsZqqwDPlzhQWXpUx0A0uTgqMNLhwXCQa8vYL5qKzl33ZDymr54KIJvNsO7tkF4BM8QHEctyj4Mzaizwus24');">
        </div>
        <div class="absolute inset-0 bg-gradient-to-br from-white/90 via-emerald-50/70 to-emerald-100/90 backdrop-blur-[4px]"></div>
    </div>

    <!-- Top AppBar Navigation Header -->
    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/40 backdrop-blur-md shadow-[0_2px_15px_rgba(20,83,45,0.03)]">
        <div class="flex items-center gap-2">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" class="h-8 w-8 rounded-lg object-cover">
            <div class="text-2xl font-bold text-primary tracking-wide">
                MetaFruit
            </div>
        </div>
        <div class="flex items-center gap-4">
            <span class="text-xs font-medium bg-emerald-100 text-primary px-3 py-1 rounded-full border border-primary/20">
                <c:out value="${sessionScope.user.fullName}"/>
            </span>
            <a href="${pageContext.request.contextPath}/auth/logout" class="text-xs font-semibold text-red-600 hover:underline flex items-center gap-0.5">
                <span class="material-symbols-outlined text-[16px]">logout</span>
                Ðang xu?t
            </a>
        </div>
    </header>

    <!-- Main Content Container -->
    <main class="flex-1 flex items-center justify-center pt-28 pb-16 px-4 md:px-8 relative z-10 w-full">
        
        <div class="w-full max-w-5xl glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">
            
            <c:if test="${not empty requestScope.errorMsg}">
                <div class="mb-6 p-4 bg-red-50 border-l-4 border-error text-red-800 rounded-r-lg flex items-center gap-3 shadow-sm animate-pulse">
                    <span class="material-symbols-outlined text-error">error</span>
                    <span class="text-sm font-medium"><c:out value="${requestScope.errorMsg}"/></span>
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.flashMsg}">
                <c:set var="isError" value="${sessionScope.flashType == 'error'}"/>
                <div class="mb-6 p-4 ${isError ? 'bg-red-50 border-error text-red-800' : 'bg-green-50 border-primary text-green-800'} border-l-4 rounded-r-lg flex items-center gap-3 shadow-sm">
                    <span class="material-symbols-outlined ${isError ? 'text-error' : 'text-primary'}">
                        ${isError ? 'error' : 'check_circle'}
                    </span>
                    <span class="text-sm font-medium"><c:out value="${sessionScope.flashMsg}"/></span>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <c:set var="displayDocPaths" value="${not empty requestScope.shopStatusDraftDocPaths ? requestScope.shopStatusDraftDocPaths : requestScope.profileDocPaths}"/>

            <c:if test="${not empty profile}">
                <div class="mb-6 grid gap-4 lg:grid-cols-[minmax(0,1.35fr)_minmax(320px,0.65fr)]">
                    <section class="rounded-3xl border border-white/70 bg-white/80 p-6 md:p-8 shadow-[0_18px_45px_rgba(20,83,45,0.10)]">
                        <div class="flex flex-wrap items-center gap-3">
                            <c:set var="shopStatusKey" value="${profile.approvalStatus}"/>
                            <jsp:include page="/WEB-INF/jsp/common/shop-status-copy.jspf"/>
                        </div>

                        <c:choose>
                            <c:when test="${profile.approvalStatus == 'APPROVED'}">
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Gian hàng đã hoạt động</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Hồ sơ đã được phê duyệt. Bạn có thể truy cập dashboard, xem lại tài liệu đã nộp và quản lý cửa hàng ngay từ đây.
                                </p>
                            </c:when>
                            <c:otherwise>
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Trạng thái hồ sơ của bạn</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Chúng tôi đã tải được hồ sơ shop của bạn. Bạn có thể xem chi tiết, tài liệu đính kèm và trạng thái xử lý ở bên dưới.
                                </p>
                            </c:otherwise>
                        </c:choose>

                        <div class="mt-6 flex flex-wrap gap-3">
                            <c:choose>
                                <c:when test="${profile.approvalStatus == 'REJECTED'}">
                                    <a href="#resubmitForm" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">edit</span>
                                        Sửa và nộp lại
                                    </a>
                                    <a href="#shop-status-docs" class="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-xs font-bold text-rose-700 transition-all hover:border-rose-300 hover:bg-rose-50">
                                        <span class="material-symbols-outlined text-[18px]">description</span>
                                        Xem tài liệu
                                    </a>
                                </c:when>
                                <c:when test="${profile.approvalStatus == 'APPROVED'}">
                                    <a href="${pageContext.request.contextPath}/shop/dashboard" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">dashboard</span>
                                        Vào dashboard
                                    </a>
                                    <a href="#shop-status-docs" class="inline-flex items-center gap-2 rounded-xl border border-emerald-200 bg-white px-4 py-2.5 text-xs font-bold text-emerald-700 transition-all hover:border-emerald-300 hover:bg-emerald-50">
                                        <span class="material-symbols-outlined text-[18px]">visibility</span>
                                        Xem lại hồ sơ
                                    </a>
                                </c:when>
                                <c:when test="${profile.approvalStatus == 'SUSPENDED'}">
                                    <a href="mailto:support@metafruit.com" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">support_agent</span>
                                        Liên hệ hỗ trợ
                                    </a>
                                    <a href="${pageContext.request.contextPath}/" class="inline-flex items-center gap-2 rounded-xl border border-red-200 bg-white px-4 py-2.5 text-xs font-bold text-red-700 transition-all hover:border-red-300 hover:bg-red-50">
                                        <span class="material-symbols-outlined text-[18px]">home</span>
                                        Về trang chủ
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="#shop-status-docs" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">description</span>
                                        Xem tài liệu
                                    </a>
                                    <a href="${pageContext.request.contextPath}/shop/dashboard" class="inline-flex items-center gap-2 rounded-xl border border-primary/20 bg-white px-4 py-2.5 text-xs font-bold text-primary transition-all hover:border-primary/30 hover:bg-emerald-50">
                                        <span class="material-symbols-outlined text-[18px]">dashboard</span>
                                        Vào dashboard
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <aside class="rounded-3xl border border-primary/10 bg-primary/5 p-5 md:p-6 shadow-[0_18px_45px_rgba(20,83,45,0.06)]">
                        <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-primary/70">Tóm tắt hồ sơ</p>
                        <div class="mt-4 space-y-3">
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Tên cửa hàng</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${profile.shopName}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Email kinh doanh</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${profile.businessEmail}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Địa chỉ kinh doanh</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${profile.deliveryAddress}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Tài liệu đã lưu</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:out value="${not empty displayDocPaths ? fn:length(displayDocPaths) : 0}"/> file
                                </p>
                            </div>
                        </div>
                    </aside>
                </div>
            </c:if>

            <c:if test="${not empty displayDocPaths}">
                <div id="shop-status-docs" class="mb-6 rounded-3xl border border-white/70 bg-white/75 p-6 md:p-8 shadow-[0_18px_45px_rgba(20,83,45,0.08)]">
                    <div class="flex items-center gap-3">
                        <div class="w-11 h-11 rounded-2xl bg-primary/10 text-primary flex items-center justify-center shrink-0">
                            <span class="material-symbols-outlined text-[24px]">description</span>
                        </div>
                        <div>
                            <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-primary/70">Tài liệu hồ sơ</p>
                            <h3 class="text-base md:text-lg font-bold text-on-surface">
                                <c:choose>
                                    <c:when test="${not empty requestScope.shopStatusDraftDocPaths}">Tài liệu nháp đang lưu trên hệ thống</c:when>
                                    <c:otherwise>Tài liệu đã nộp kèm hồ sơ</c:otherwise>
                                </c:choose>
                            </h3>
                            <p class="text-xs md:text-sm text-on-surface-variant mt-1">
                                <c:choose>
                                    <c:when test="${not empty requestScope.shopStatusDraftDocPaths}">Bạn có thể sửa các trường khác mà không phải tải lại file nếu chỉ gặp lỗi validation text.</c:when>
                                    <c:otherwise>Đây là danh sách file hiện đang gắn với đơn shop của bạn.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </div>

                    <div class="mt-5 grid gap-2">
                        <c:forEach var="docPath" items="${displayDocPaths}">
                            <c:set var="docPathParts" value="${fn:split(docPath, '/')}"/>
                            <c:set var="docFileName" value="${docPathParts[fn:length(docPathParts) - 1]}"/>
                            <div class="flex items-center gap-3 rounded-2xl border border-primary/10 bg-white/80 px-4 py-3 text-sm shadow-sm">
                                <span class="material-symbols-outlined text-primary text-[20px]">folder_open</span>
                                <div class="min-w-0 flex-1">
                                    <p class="font-semibold text-on-surface break-all"><c:out value="${docFileName}"/></p>
                                    <p class="text-[11px] text-on-surface-variant break-all"><c:out value="${docPath}"/></p>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:choose>
                <%-- Tr?ng thái CH? DUY?T (PENDING) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'PENDING'}">
                    <div class="text-center mb-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-amber-50 border border-amber-200 text-amber-500 mb-4 animate-pulse">
                            <span class="material-symbols-outlined text-[36px]">hourglass_empty</span>
                        </div>
                        <h1 class="text-2xl font-bold text-amber-700 mb-2">H? so dang du?c xét duy?t</h1>
                        <p class="text-sm text-on-surface-variant font-light">
                            H? th?ng dã nh?n don dang ký c?a b?n. Ban qu?n tr? dang ti?n hành xác minh thông tin.
                        </p>
                    </div>

                    <!-- Timeline Stepper -->
                    <div class="my-10 bg-white/40 p-6 rounded-xl border border-white/60">
                        <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-6 flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">timeline</span>
                            Ti?n trình x? lý h? so
                        </h3>
                        
                        <div class="relative pl-6 space-y-8 border-l-2 border-primary/20">
                            <!-- Step 1 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-primary border-4 border-emerald-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-primary">N?p don dang ký m? gian hàng</h4>
                                <p class="text-xs text-on-surface-variant mt-1">Ðã hoàn thành g?i thông tin c?a hàng lên h? th?ng.</p>
                            </div>
                            
                            <!-- Step 2 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-amber-500 border-4 border-amber-100 flex items-center justify-center animate-ping"></div>
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-amber-500 border-4 border-amber-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-amber-600">Ban qu?n tr? th?m d?nh h? so</h4>
                                <p class="text-xs text-on-surface-variant mt-1">Ðang d?i chi?u gi?y t? pháp lý và di?u ki?n kinh doanh. Th?i gian duy?t t? 1-3 ngày làm vi?c.</p>
                            </div>
                            
                            <!-- Step 3 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-gray-300 border-4 border-gray-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-gray-400">Kích ho?t gian hàng chính th?c</h4>
                                <p class="text-xs text-on-surface-variant mt-1">C?a hàng di vào ho?t d?ng và b?t d?u dang bán s?n ph?m.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Shop Details Card -->
                    <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-3 text-sm">
                        <h4 class="text-xs font-bold text-primary uppercase tracking-wider mb-2">Thông tin gian hàng dang ký</h4>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Tên c?a hàng:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.shopName}"/></span>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Email kinh doanh:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.businessEmail}"/></span>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Ð?a di?m gom hàng:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.deliveryAddress}"/></span>
                        </div>
                    </div>
                </c:when>

                <%-- Tr?ng thái B? T? CH?I (REJECTED) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'REJECTED'}">
                    <div class="text-center mb-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 border border-red-200 text-red-500 mb-4">
                            <span class="material-symbols-outlined text-[36px]">cancel</span>
                        </div>
                        <h1 class="text-2xl font-bold text-red-700 mb-2">Ðon dang ký b? t? ch?i</h1>
                        <p class="text-sm text-on-surface-variant font-light">
                            H? so c?a b?n không d? di?u ki?n phê duy?t ho?c thông tin không chính xác.
                        </p>
                    </div>

                    <!-- Rejection Reason Card -->
                    <div class="mb-8 p-5 bg-red-50/70 border border-red-200 rounded-xl space-y-2">
                        <h4 class="text-xs font-bold text-red-700 uppercase tracking-wider flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">info</span>
                            Lý do t? ch?i phê duy?t
                        </h4>
                        <p class="text-sm text-red-900 font-medium">
                            <c:out value="${profile.rejectionReason != null ? profile.rejectionReason : 'Tài li?u cung c?p không rõ ràng ho?c không h?p l?. Vui lòng ki?m tra l?i gi?y t? dang ký kinh doanh.'}"/>
                        </p>
                    </div>

                    <!-- Resubmit Form -->
                    <form action="${pageContext.request.contextPath}/shop/status" method="post" enctype="multipart/form-data" class="space-y-6" id="resubmitForm">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        
                        <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                            <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-2 flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">edit_note</span>
                                Ði?u ch?nh & n?p l?i thông tin h? so
                            </h3>
                            
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <!-- Store Name -->
                                <div class="flex flex-col gap-1">
                                    <label class="text-xs font-semibold text-primary" for="storeName">Tên c?a hàng *</label>
                                    <div class="relative">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">storefront</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                               id="storeName" name="storeName" value="<c:out value="${not empty param.storeName ? param.storeName : profile.shopName}"/>" type="text" required minlength="3">
                                    </div>
                                </div>
                                
                                <!-- Business Email -->
                                <div class="flex flex-col gap-1">
                                    <label class="text-xs font-semibold text-primary" for="businessEmail">Email liên h? kinh doanh *</label>
                                    <div class="relative">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">contact_mail</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                               id="businessEmail" name="businessEmail" value="<c:out value="${not empty param.businessEmail ? param.businessEmail : profile.businessEmail}"/>" type="email" required>
                                    </div>
                                </div>
                            </div>

                            <!-- Business Address -->
                            <div class="flex flex-col gap-1">
                                <label class="text-xs font-semibold text-primary" for="address">Ð?a ch? kinh doanh *</label>
                                <div class="relative">
                                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">pin_drop</span>
                                    <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                           id="address" name="address" value="<c:out value="${not empty param.address ? param.address : profile.deliveryAddress}"/>" type="text" required minlength="5">
                                </div>
                            </div>

                            <!-- Shop Description -->
                            <div class="flex flex-col gap-1">
                                <label class="text-xs font-semibold text-primary" for="shopDescription">Mô t? ng?n v? c?a hàng</label>
                                <textarea class="w-full px-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                          id="shopDescription" name="shopDescription" rows="3" placeholder="Gi?i thi?u v? s?n ph?m d?c trung ho?c ngu?n g?c nông s?n c?a b?n..."><c:out value="${not empty param.shopDescription ? param.shopDescription : profile.shopDescription}"/></textarea>
                            </div>

                            <!-- Categories Selection -->
                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-semibold text-primary">Danh m?c s?n ph?m kinh doanh *</label>
                                <div class="grid grid-cols-2 gap-2 mt-1" id="categoryGrid">
                                    <c:choose>
                                        <c:when test="${not empty categories}">
                                            <c:forEach var="cat" items="${categories}">
                                                <c:set var="isChecked" value="false"/>
                                                <c:choose>
                                                    <c:when test="${not empty paramValues.categoryIds}">
                                                        <c:forEach var="selectedId" items="${paramValues.categoryIds}">
                                                            <c:if test="${selectedId == cat.categoryId}">
                                                                <c:set var="isChecked" value="true"/>
                                                            </c:if>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:when test="${not empty profile and not empty profile.preferredCategories}">
                                                        <c:set var="prefCatContains" value="[${cat.categoryId},"/>
                                                        <c:set var="prefCatContainsMiddle" value=",${cat.categoryId},"/>
                                                        <c:set var="prefCatContainsEnd" value=",${cat.categoryId}]"/>
                                                        <c:set var="prefCatSingle" value="[${cat.categoryId}]"/>
                                                        <c:if test="${profile.preferredCategories == prefCatSingle ||
                                                                      fn:contains(profile.preferredCategories, prefCatContains) ||
                                                                      fn:contains(profile.preferredCategories, prefCatContainsMiddle) ||
                                                                      fn:contains(profile.preferredCategories, prefCatContainsEnd)}">
                                                            <c:set var="isChecked" value="true"/>
                                                        </c:if>
                                                    </c:when>
                                                </c:choose>
                                                <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                                    <input class="rounded text-primary focus:ring-primary border-outline/30 bg-white" 
                                                           name="categoryIds" 
                                                           value="<c:out value="${cat.categoryId}"/>" 
                                                           type="checkbox"
                                                           <c:if test="${isChecked}">checked</c:if>>
                                                    <span class="ml-2.5 text-xs font-medium text-on-surface"><c:out value="${cat.name}"/></span>
                                                </label>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="col-span-2 text-xs text-outline italic">Không t?i du?c danh m?c.</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Document Upload -->
                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-semibold text-primary">T?i l?i tài li?u xác minh (GPKD, ch?ng nh?n...) *</label>
                                <p class="text-[10px] text-outline">T?i lên file m?i s? ghi dè tài li?u cu c?a b?n</p>
                                <div class="border-2 border-dashed border-primary/20 rounded-lg p-5 text-center bg-white/20 hover:bg-emerald-50/50 transition-colors cursor-pointer group relative" id="dropzone">
                                    <input class="absolute inset-0 w-full h-full opacity-0 cursor-pointer" 
                                           id="businessDocs" name="businessDocs" type="file" 
                                           multiple 
                                           accept=".pdf,.jpg,.jpeg,.png,.docx"
                                           onchange="handleFileSelect(this)"
                                           <c:if test="${empty requestScope.shopStatusDraftDocPaths}">required</c:if>>
                                    <div class="flex flex-col items-center gap-2 pointer-events-none">
                                        <span class="material-symbols-outlined text-[36px] text-primary/60 group-hover:text-primary transition-colors">cloud_upload</span>
                                        <p class="text-xs font-medium text-on-surface-variant" id="uploadLabel">
                                            Kéo th? tài li?u vào dây ho?c <span class="text-primary font-bold">ch?n t?p t? thi?t b?</span>
                                        </p>
                                    </div>
                                </div>
                                <ul id="fileList" class="mt-2 space-y-1 hidden"></ul>
                                <p id="fileError" class="text-xs text-red-600 hidden"></p>
                                <c:if test="${not empty requestScope.shopStatusDraftDocPaths}">
                                    <div class="rounded-xl border border-emerald-200 bg-emerald-50/70 p-4 space-y-3">
                                        <div class="flex items-center gap-2">
                                            <span class="material-symbols-outlined text-primary text-[20px]">draft</span>
                                            <div>
                                                <p class="text-sm font-semibold text-primary">Tài li?u nháp dã luu trên h? th?ng</p>
                                                <p class="text-xs text-on-surface-variant">B?n có th? s?a n?i dung khác mà không ph?i t?i l?i file.</p>
                                            </div>
                                        </div>
                                        <div class="space-y-2">
                                            <c:forEach var="draftDocPath" items="${requestScope.shopStatusDraftDocPaths}">
                                                <c:set var="draftPathSegments" value="${fn:split(draftDocPath, '/')}"/>
                                                <c:set var="draftFileName" value="${draftPathSegments[fn:length(draftPathSegments) - 1]}"/>
                                                <div class="flex items-center gap-2 rounded-lg border border-emerald-200 bg-white/80 px-3 py-2 text-xs">
                                                    <span class="material-symbols-outlined text-primary text-[16px]">description</span>
                                                    <span class="font-medium text-on-surface break-all"><c:out value="${draftFileName}"/></span>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <div class="flex items-start gap-3">
                            <div class="flex items-center h-5 mt-0.5">
                                <input class="w-4 h-4 rounded border-outline/30 text-primary focus:ring-primary bg-white cursor-pointer"
                                       id="agreeTerms" name="agreeTerms" type="checkbox" <c:if test="${not empty param.agreeTerms}">checked</c:if> required>
                            </div>
                            <label class="text-xs text-on-surface-variant leading-relaxed cursor-pointer" for="agreeTerms">
                                Tôi cam k?t thông tin cung c?p là chính xác và d?ng ý v?i
                                <a class="text-primary font-bold hover:underline" href="#">Ði?u kho?n dành cho d?i tác bán hàng</a>
                                c?a MetaFruit.
                            </label>
                        </div>

                        <!-- Resubmit Button -->
                        <button type="submit" class="w-full mt-4 bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                            <span>G?i l?i h? so xét duy?t</span>
                            <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">send</span>
                        </button>
                    </form>
                </c:when>

                <%-- Tr?ng thái ÐÃ PHÊ DUY?T (APPROVED) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'SUSPENDED'}">
                    <div class="text-center mb-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 border border-red-200 text-red-500 mb-4">
                            <span class="material-symbols-outlined text-[36px]">lock</span>
                        </div>
                        <h1 class="text-2xl font-bold text-red-700 mb-2">Gian hàng đang bị đình chỉ</h1>
                        <p class="text-sm text-on-surface-variant font-light">
                            Cửa hàng của bạn đang bị tạm ngưng hoạt động. Bạn vẫn có thể xem hồ sơ, nhưng cần liên hệ hỗ trợ để được xem xét mở lại.
                        </p>
                    </div>

                    <div class="mb-8 p-5 bg-red-50/70 border border-red-200 rounded-xl space-y-2">
                        <h4 class="text-xs font-bold text-red-700 uppercase tracking-wider flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">info</span>
                            Lý do đình chỉ
                        </h4>
                        <p class="text-sm text-red-900 font-medium">
                            <c:out value="${profile.rejectionReason != null ? profile.rejectionReason : 'Gian hàng đang bị đình chỉ. Vui lòng liên hệ hỗ trợ để biết thêm chi tiết và hướng xử lý.'}"/>
                        </p>
                    </div>

                    <div class="flex flex-col sm:flex-row gap-3 justify-center">
                        <a href="mailto:support@metafruit.com" class="inline-flex items-center justify-center gap-2 bg-primary text-white text-sm font-semibold py-3 px-6 rounded-lg shadow-md hover:bg-primary-hover transition-all">
                            <span class="material-symbols-outlined text-[18px]">support_agent</span>
                            Liên hệ hỗ trợ
                        </a>
                        <a href="${pageContext.request.contextPath}/" class="inline-flex items-center justify-center gap-2 border border-red-200 bg-white text-red-700 text-sm font-semibold py-3 px-6 rounded-lg hover:bg-red-50 transition-all">
                            <span class="material-symbols-outlined text-[18px]">home</span>
                            Về trang chủ
                        </a>
                    </div>
                </c:when>

                <c:when test="${not empty profile and profile.approvalStatus == 'APPROVED'}">
                    <div class="text-center py-6">
                        <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-50 border border-green-200 text-primary mb-6">
                            <span class="material-symbols-outlined text-[48px]">check_circle</span>
                        </div>
                        <h1 class="text-2xl font-bold text-primary mb-2">Gian hàng dã kích ho?t ho?t d?ng</h1>
                        <p class="text-sm text-on-surface-variant font-light mb-8 max-w-md mx-auto">
                            Chúc m?ng! Ðon dang ký c?a b?n dã du?c phê duy?t thành công. B?n dã có toàn quy?n truy c?p trang qu?n tr? c?a hàng.
                        </p>
                        
                        <div class="flex flex-col gap-3 max-w-sm mx-auto">
                            <a href="${pageContext.request.contextPath}/shop/dashboard" 
                               class="w-full bg-primary text-white text-sm font-semibold py-3 px-6 rounded-lg shadow-md hover:bg-primary-hover transition-all flex items-center justify-center gap-2">
                                <span class="material-symbols-outlined text-[18px]">dashboard</span>
                                Vào trang qu?n tr? c?a tôi
                            </a>
                            <a href="${pageContext.request.contextPath}/" class="text-xs text-primary font-bold hover:underline">
                                Tr? v? trang ch?
                            </a>
                        </div>
                    </div>
                </c:when>

                <%-- Chua có thông tin don dang ký (Chua luu) --%>
                <c:otherwise>
                    <div class="text-center py-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-50 border border-gray-200 text-outline mb-4">
                            <span class="material-symbols-outlined text-[36px]">contact_support</span>
                        </div>
                        <h1 class="text-xl font-bold text-on-surface mb-2">Chua tìm th?y don dang ký</h1>
                        <p class="text-sm text-on-surface-variant font-light mb-6">
                            Tài kho?n c?a b?n dã du?c nâng c?p lên Ch? c?a hàng, nhung chua có thông tin h? so du?c luu.
                        </p>
                        <a href="${pageContext.request.contextPath}/auth/register?accountType=SHOP_OWNER" 
                           class="inline-flex items-center gap-2 bg-primary text-white text-sm font-semibold py-2.5 px-5 rounded-lg shadow hover:bg-primary-hover transition-colors">
                            <span class="material-symbols-outlined text-[18px]">app_registration</span>
                            B? sung thông tin h? so ngay
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
            
        </div>
    </main>

    <!-- Site Footer -->
    <footer class="w-full py-4 text-center border-t border-white/20 bg-white/40 backdrop-blur-md relative z-10">
        <p class="text-xs text-on-surface-variant/80 font-light">&copy; 2026 MetaFruit. Gi?i pháp nông s?n Vi?t s?ch và b?n v?ng.</p>
    </footer>

    <script>
        const MAX_DOC_COUNT = 10;
        const MAX_DOC_SIZE_MB = 25;
        const MAX_DOC_SIZE_BYTES = MAX_DOC_SIZE_MB * 1024 * 1024;
        const ALLOWED_EXTS = ['pdf', 'jpg', 'jpeg', 'png', 'docx'];

        function handleFileSelect(input) {
            const fileListEl = document.getElementById('fileList');
            const fileErrorEl = document.getElementById('fileError');
            const uploadLabel = document.getElementById('uploadLabel');
            const files = Array.from(input.files);

            fileListEl.innerHTML = '';
            fileListEl.classList.add('hidden');
            fileErrorEl.classList.add('hidden');
            fileErrorEl.textContent = '';

            let errors = [];
            if (files.length > MAX_DOC_COUNT) {
                errors.push('Ch? du?c ch?n t?i da ' + MAX_DOC_COUNT + ' tài li?u.');
            }

            const validFiles = files.slice(0, MAX_DOC_COUNT);
            validFiles.forEach((file) => {
                const ext = file.name.split('.').pop().toLowerCase();
                const li = document.createElement('li');
                li.className = 'flex items-center gap-2 text-xs py-1 px-2 bg-white/50 rounded-lg border border-outline/20';

                if (!ALLOWED_EXTS.includes(ext)) {
                    errors.push('File "' + file.name + '" không du?c h? tr?. Ch? ch?p nh?n PDF, JPG, PNG, DOCX.');
                    li.classList.add('border-red-300', 'bg-red-50/50');
                    li.innerHTML = '<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">' + file.name + ' — Sai d?nh d?ng</span>';
                } else if (file.size > MAX_DOC_SIZE_BYTES) {
                    errors.push('File "' + file.name + '" vu?t quá 25MB.');
                    li.classList.add('border-red-300', 'bg-red-50/50');
                    li.innerHTML = '<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">' + file.name + ' — Quá 25MB</span>';
                } else {
                    li.innerHTML = '<span class="material-symbols-outlined text-primary text-[16px]">description</span><span class="text-on-surface">' + file.name + '</span><span class="ml-auto text-outline">' + (file.size/1024/1024).toFixed(1) + 'MB</span>';
                }
                fileListEl.appendChild(li);
            });

            if (validFiles.length > 0) {
                fileListEl.classList.remove('hidden');
                uploadLabel.innerHTML = 'Ðã ch?n <span class="text-primary font-bold">' + validFiles.length + ' t?p</span> tài li?u';
            } else {
                uploadLabel.innerHTML = 'Kéo th? tài li?u vào dây ho?c <span class="text-primary font-bold">ch?n t?p t? thi?t b?</span>';
            }

            if (errors.length > 0) {
                fileErrorEl.textContent = errors[0];
                fileErrorEl.classList.remove('hidden');
            }
        }

        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('resubmitForm');
            if (form) {
                form.addEventListener('submit', function(event) {
                    const existingErrors = form.querySelectorAll('.client-error');
                    existingErrors.forEach(el => el.remove());

                    let hasError = false;

                    function showError(inputEl, message) {
                        hasError = true;
                        const errorDiv = document.createElement('p');
                        errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                        errorDiv.textContent = message;
                        inputEl.closest('.flex-col').appendChild(errorDiv);
                    }

                    const storeNameInput = document.getElementById('storeName');
                    if (storeNameInput.value.trim().length < 3) {
                        showError(storeNameInput, 'Tên c?a hàng ph?i t? 3 ký t? tr? lên.');
                    }

                    const businessEmailInput = document.getElementById('businessEmail');
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(businessEmailInput.value.trim())) {
                        showError(businessEmailInput, 'Email liên h? kinh doanh không dúng d?nh d?ng.');
                    }

                    const addressInput = document.getElementById('address');
                    if (addressInput.value.trim().length < 5) {
                        showError(addressInput, 'Ð?a ch? kinh doanh ph?i t? 5 ký t? tr? lên.');
                    }

                    const categoryChecked = form.querySelectorAll('input[name="categoryIds"]:checked');
                    if (categoryChecked.length === 0) {
                        hasError = true;
                        const errorDiv = document.createElement('p');
                        errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                        errorDiv.textContent = 'Vui lòng ch?n ít nh?t m?t danh m?c s?n ph?m.';
                        document.getElementById('categoryGrid').parentNode.appendChild(errorDiv);
                    }

                    const fileInput = document.getElementById('businessDocs');
                    if (fileInput.files.length > 0) {
                        const files = Array.from(fileInput.files);
                        if (files.length > MAX_DOC_COUNT) {
                            hasError = true;
                            const errorDiv = document.createElement('p');
                            errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                            errorDiv.textContent = 'Ch? du?c ch?n t?i da ' + MAX_DOC_COUNT + ' tài li?u.';
                            document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                        }

                        for (let file of files) {
                            const ext = file.name.split('.').pop().toLowerCase();
                            if (!ALLOWED_EXTS.includes(ext)) {
                                hasError = true;
                                const errorDiv = document.createElement('p');
                                errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                                errorDiv.textContent = 'T?p "' + file.name + '" không h?p l?. Ch? ch?p nh?n các d?nh d?ng PDF, JPG, PNG, DOCX.';
                                document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                                break;
                            }
                            if (file.size > MAX_DOC_SIZE_BYTES) {
                                hasError = true;
                                const errorDiv = document.createElement('p');
                                errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                                errorDiv.textContent = 'T?p "' + file.name + '" vu?t quá gi?i h?n 25MB.';
                                document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                                break;
                            }
                        }
                    }

                    if (hasError) {
                        event.preventDefault();
                        const firstError = form.querySelector('.client-error');
                        if (firstError) {
                            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }
                    }
                });
            }
        });
    </script>
</body>
</html>
