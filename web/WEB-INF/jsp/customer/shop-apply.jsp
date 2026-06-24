<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>ÄÄƒng kÃ½ má»Ÿ gian hÃ ng - MetaFruit</title>
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        "primary": "#14532D",
                        "primary-hover": "#166534",
                        "surface": "#eaffea",
                        "on-surface": "#00210d",
                        "on-surface-variant": "#44483b",
                        "outline": "#75796a",
                        "outline-variant": "#c5c8b7",
                        "error": "#ba1a1a"
                    },
                    fontFamily: { sans: ["Lexend", "sans-serif"] }
                }
            }
        }
    </script>
    <style>
        body { font-family: 'Lexend', sans-serif; }
        .glass-card {
            background: rgba(255,255,255,0.88);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255,255,255,0.5);
            box-shadow: 0 20px 50px rgba(20,83,45,0.12);
        }
        .status-badge-pending  { background: #fef3c7; color: #92400e; }
        .status-badge-approved { background: #d1fae5; color: #065f46; }
        .status-badge-rejected { background: #fee2e2; color: #991b1b; }
    </style>
</head>
<body class="bg-emerald-50 text-on-surface min-h-screen antialiased">

    <!-- Decorative Background -->
    <div class="fixed inset-0 z-0 pointer-events-none">
        <div class="absolute inset-0 bg-gradient-to-br from-emerald-50 via-white to-green-100"></div>
        <div class="absolute top-0 right-0 w-96 h-96 bg-emerald-200/30 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl"></div>
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-green-200/20 rounded-full translate-y-1/2 -translate-x-1/2 blur-2xl"></div>
    </div>

    <!-- Header -->
    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/60 backdrop-blur-md shadow-sm">
        <a href="${pageContext.request.contextPath}/" class="flex items-center gap-2">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" class="h-8 w-8 rounded-lg object-cover">
            <span class="text-2xl font-bold text-primary tracking-wide">MetaFruit</span>
        </a>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/customer/profile" class="text-sm font-medium text-on-surface-variant hover:text-primary transition-colors flex items-center gap-1">
                <span class="material-symbols-outlined text-[18px]">account_circle</span>
                <c:out value="${sessionScope.currentUser.fullName}"/>
            </a>
            <a href="${pageContext.request.contextPath}/" class="text-sm font-medium text-primary hover:text-primary-hover flex items-center gap-1 px-3 py-1.5 rounded-full hover:bg-emerald-100 transition-colors">
                <span class="material-symbols-outlined text-[18px]">home</span>
                Trang chá»§
            </a>
        </div>
    </header>

    <!-- Main -->
    <main class="relative z-10 pt-28 pb-16 px-4 md:px-8 flex justify-center">
        <div class="w-full max-w-3xl">

            <!-- Page Title -->
            <div class="text-center mb-8">
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-emerald-100 mb-4">
                    <span class="material-symbols-outlined text-primary text-3xl">storefront</span>
                </div>
                <h1 class="text-2xl md:text-3xl font-bold text-primary mb-2">ÄÄƒng kÃ½ má»Ÿ gian hÃ ng</h1>
                <p class="text-sm text-on-surface-variant">Äiá»n thÃ´ng tin Ä‘á»ƒ gá»­i Ä‘Æ¡n Ä‘Äƒng kÃ½. Admin sáº½ xÃ©t duyá»‡t trong 1-3 ngÃ y lÃ m viá»‡c.</p>
            </div>

            <!-- Flash Message -->
            <c:if test="${not empty sessionScope.flashMsg}">
                <c:set var="isError" value="${sessionScope.flashType == 'error'}"/>
                <div class="mb-6 p-4 ${isError ? 'bg-red-50 border-error text-red-800' : 'bg-green-50 border-primary text-green-800'} border-l-4 rounded-r-xl flex items-center gap-3 shadow-sm glass-card">
                    <span class="material-symbols-outlined ${isError ? 'text-error' : 'text-primary'}">${isError ? 'error' : 'check_circle'}</span>
                    <span class="text-sm font-medium"><c:out value="${sessionScope.flashMsg}"/></span>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <!-- TRáº NG THÃI ÄÃƒ Ná»˜P ÄÆ N (náº¿u cÃ³) -->
                        <c:if test="${not empty requestScope.existingProfile}">
                <div class="mb-6 grid gap-4 lg:grid-cols-[minmax(0,1.35fr)_minmax(320px,0.65fr)]">
                    <section class="rounded-3xl border border-white/70 bg-white/80 p-6 md:p-8 shadow-[0_18px_45px_rgba(20,83,45,0.10)]">
                        <div class="flex flex-wrap items-center gap-3">
                            <c:set var="shopStatusKey" value="${existingProfile.approvalStatus}"/>
                            <jsp:include page="/WEB-INF/jsp/common/shop-status-copy.jspf"/>
                        </div>

                        <c:choose>
                            <c:when test="${existingProfile.approvalStatus == 'APPROVED'}">
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Gian hàng đã hoạt động</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Hồ sơ đã được phê duyệt. Bạn có thể mở dashboard, xem lại tài liệu đã lưu và quản lý gian hàng ngay từ đây.
                                </p>
                            </c:when>
                            <c:when test="${existingProfile.approvalStatus == 'REJECTED'}">
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Hồ sơ cần chỉnh sửa và nộp lại</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Đơn đã bị từ chối, nhưng bạn vẫn có thể sửa lại thông tin, thay tài liệu và gửi lại ngay trên màn hình này mà không phải tạo hồ sơ mới.
                                </p>
                            </c:when>
                            <c:when test="${existingProfile.approvalStatus == 'SUSPENDED'}">
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Gian hàng đang bị đình chỉ</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Gian hàng hiện không thể hoạt động bình thường. Hãy xem lý do bên dưới và liên hệ hỗ trợ để được mở lại.
                                </p>
                            </c:when>
                            <c:otherwise>
                                <h2 class="mt-4 text-2xl md:text-3xl font-extrabold text-on-surface">Trạng thái hồ sơ của bạn</h2>
                                <p class="mt-3 text-sm md:text-base text-on-surface-variant leading-6">
                                    Chúng tôi đã lưu hồ sơ shop hiện tại của bạn. Bạn có thể xem chi tiết thông tin, tài liệu đính kèm và trạng thái xử lý ở bên dưới.
                                </p>
                            </c:otherwise>
                        </c:choose>

                        <div class="mt-6 flex flex-wrap gap-3">
                            <c:choose>
                                <c:when test="${existingProfile.approvalStatus == 'REJECTED'}">
                                    <a href="#shopApplyForm" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">edit</span>
                                        Sửa hồ sơ ngay
                                    </a>
                                    <a href="#shopDropzone" class="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-xs font-bold text-rose-700 transition-all hover:border-rose-300 hover:bg-rose-50">
                                        <span class="material-symbols-outlined text-[18px]">description</span>
                                        Xem tài liệu đã lưu
                                    </a>
                                </c:when>
                                <c:when test="${existingProfile.approvalStatus == 'APPROVED'}">
                                    <a href="${pageContext.request.contextPath}/shop/dashboard" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">dashboard</span>
                                        Vào dashboard
                                    </a>
                                    <a href="${pageContext.request.contextPath}/shop/status" class="inline-flex items-center gap-2 rounded-xl border border-emerald-200 bg-white px-4 py-2.5 text-xs font-bold text-emerald-700 transition-all hover:border-emerald-300 hover:bg-emerald-50">
                                        <span class="material-symbols-outlined text-[18px]">visibility</span>
                                        Xem trạng thái
                                    </a>
                                </c:when>
                                <c:when test="${existingProfile.approvalStatus == 'SUSPENDED'}">
                                    <a href="mailto:support@metafruit.com" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">support_agent</span>
                                        Liên hệ hỗ trợ
                                    </a>
                                    <a href="${pageContext.request.contextPath}/shop/status" class="inline-flex items-center gap-2 rounded-xl border border-red-200 bg-white px-4 py-2.5 text-xs font-bold text-red-700 transition-all hover:border-red-300 hover:bg-red-50">
                                        <span class="material-symbols-outlined text-[18px]">assignment</span>
                                        Xem chi tiết trạng thái
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/shop/status" class="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-bold text-white shadow-sm transition-all hover:bg-primary-hover hover:-translate-y-0.5">
                                        <span class="material-symbols-outlined text-[18px]">assignment</span>
                                        Mở trang trạng thái
                                    </a>
                                    <a href="mailto:support@metafruit.com" class="inline-flex items-center gap-2 rounded-xl border border-primary/20 bg-white px-4 py-2.5 text-xs font-bold text-primary transition-all hover:border-primary/30 hover:bg-emerald-50">
                                        <span class="material-symbols-outlined text-[18px]">support_agent</span>
                                        Liên hệ hỗ trợ
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
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${existingProfile.shopName}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Email kinh doanh</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${existingProfile.businessEmail}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Địa chỉ kinh doanh</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface break-words"><c:out value="${existingProfile.deliveryAddress}"/></p>
                            </div>
                            <div class="rounded-2xl bg-white/80 p-4 border border-white/60">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">Tài liệu đã lưu</p>
                                <p class="mt-1 text-sm font-semibold text-on-surface">
                                    <c:out value="${not empty requestScope.existingProfileDocPaths ? fn:length(requestScope.existingProfileDocPaths) : 0}"/> file
                                </p>
                            </div>
                        </div>

                        <c:if test="${existingProfile.approvalStatus == 'REJECTED' and not empty existingProfile.rejectionReason}">
                            <div class="mt-4 rounded-2xl border border-rose-200 bg-rose-50 p-4">
                                <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-rose-700">Lý do từ chối</p>
                                <p class="mt-2 text-xs text-rose-700 leading-6"><c:out value="${existingProfile.rejectionReason}"/></p>
                            </div>
                        </c:if>
                    </aside>
                </div>
            </c:if>

            <c:set var="canApply" value="${empty existingProfile or existingProfile.approvalStatus == 'REJECTED'}"/>
            <c:if test="${canApply}">
                <div class="glass-card rounded-2xl p-6 md:p-10">
                    <h2 class="text-base font-bold text-primary mb-6 flex items-center gap-2">
                        <span class="material-symbols-outlined text-[20px]">edit_document</span>
                        <c:choose>
                            <c:when test="${not empty existingProfile and existingProfile.approvalStatus == 'REJECTED'}">Ná»™p láº¡i Ä‘Æ¡n Ä‘Äƒng kÃ½</c:when>
                            <c:otherwise>ThÃ´ng tin Ä‘Äƒng kÃ½ gian hÃ ng</c:otherwise>
                        </c:choose>
                    </h2>

                    <!-- Error Message -->
                    <c:if test="${not empty requestScope.errorMsg}">
                        <div class="mb-6 p-4 bg-red-50 border-l-4 border-error text-red-800 rounded-r-lg flex items-center gap-3 shadow-sm">
                            <span class="material-symbols-outlined text-error">error</span>
                            <span class="text-sm font-medium"><c:out value="${requestScope.errorMsg}"/></span>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/customer/shop-apply" method="post" enctype="multipart/form-data" class="space-y-6" id="shopApplyForm">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                        <!-- ThÃ´ng tin tÃ i khoáº£n (readonly) -->
                        <div class="bg-emerald-50/60 p-4 rounded-xl border border-emerald-200/50">
                            <p class="text-xs font-semibold text-primary uppercase tracking-wider mb-3 flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">account_circle</span>
                                ThÃ´ng tin tÃ i khoáº£n liÃªn káº¿t
                            </p>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                                <div>
                                    <span class="text-xs text-on-surface-variant">Há» vÃ  tÃªn:</span>
                                    <p class="font-medium text-on-surface"><c:out value="${sessionScope.currentUser.fullName}"/></p>
                                </div>
                                <div>
                                    <span class="text-xs text-on-surface-variant">Email:</span>
                                    <p class="font-medium text-on-surface"><c:out value="${sessionScope.currentUser.email}"/></p>
                                </div>
                            </div>
                        </div>

                        <!-- YÃªu cáº§u bá»• sung Sá»‘ Ä‘iá»‡n thoáº¡i liÃªn há»‡ -->
                        <c:if test="${empty sessionScope.currentUser.phone}">
                            <div class="bg-amber-50/80 p-5 rounded-xl border border-amber-200/60 space-y-3 shadow-sm">
                                <h3 class="text-xs font-bold text-amber-800 uppercase tracking-wider flex items-center gap-1.5">
                                    <span class="material-symbols-outlined text-[16px] text-amber-700">phone</span>
                                    YÃªu cáº§u bá»• sung Sá»‘ Ä‘iá»‡n thoáº¡i liÃªn há»‡ *
                                </h3>
                                <p class="text-[11px] text-amber-700 font-medium">TÃ i khoáº£n liÃªn káº¿t cá»§a báº¡n chÆ°a cÃ³ sá»‘ Ä‘iá»‡n thoáº¡i. Vui lÃ²ng cáº­p nháº­t sá»‘ Ä‘iá»‡n thoáº¡i Ä‘á»ƒ thá»±c hiá»‡n Ä‘Äƒng kÃ½ gian hÃ ng.</p>
                                <div>
                                    <label class="text-xs font-semibold text-primary" for="userPhone">Sá»‘ Ä‘iá»‡n thoáº¡i di Ä‘á»™ng *</label>
                                    <div class="relative mt-1">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">phone</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm outline-none transition-all"
                                               id="userPhone" name="userPhone"
                                               value="<c:out value="${param.userPhone}"/>"
                                               placeholder="VD: 0987654321" type="tel" required pattern="^(0|\+84)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-9])[0-9]{7}$">
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <!-- ThÃ´ng tin cá»­a hÃ ng -->
                        <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                            <h3 class="text-xs font-bold text-primary uppercase tracking-wider flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">domain</span>
                                ThÃ´ng tin cá»­a hÃ ng
                            </h3>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label class="text-xs font-semibold text-primary" for="shopName">TÃªn cá»­a hÃ ng / Doanh nghiá»‡p *</label>
                                    <div class="relative mt-1">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">storefront</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm outline-none transition-all"
                                               id="shopName" name="shopName"
                                               value="<c:out value="${not empty param.shopName ? param.shopName : existingProfile.shopName}"/>"
                                               placeholder="VD: NÃ´ng tráº¡i há»¯u cÆ¡ xanh" type="text" required maxlength="150">
                                    </div>
                                </div>
                                
                                <div>
                                    <label class="text-xs font-semibold text-primary" for="businessEmail">Email liÃªn há»‡ kinh doanh *</label>
                                    <div class="relative mt-1">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">mail</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm outline-none transition-all"
                                               id="businessEmail" name="businessEmail"
                                               value="<c:out value="${not empty param.businessEmail ? param.businessEmail : existingProfile.businessEmail}"/>"
                                               placeholder="VD: email@doanhnghiep.com" type="email" required maxlength="150">
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label class="text-xs font-semibold text-primary" for="shopAddress">Äá»‹a chá»‰ kinh doanh / Äiá»ƒm gom hÃ ng *</label>
                                <div class="relative mt-1">
                                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">pin_drop</span>
                                    <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm outline-none transition-all"
                                           id="shopAddress" name="shopAddress"
                                           value="<c:out value="${not empty param.shopAddress ? param.shopAddress : existingProfile.deliveryAddress}"/>"
                                           placeholder="Sá»‘ nhÃ , tÃªn Ä‘Æ°á»ng, phÆ°á»ng/xÃ£, quáº­n/huyá»‡n, tá»‰nh/thÃ nh" type="text" required maxlength="500">
                                </div>
                            </div>

                            <div>
                                <label class="text-xs font-semibold text-primary" for="shopDescription">MÃ´ táº£ cá»­a hÃ ng</label>
                                <textarea class="w-full mt-1 px-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm outline-none transition-all resize-none"
                                          id="shopDescription" name="shopDescription" rows="3"
                                          placeholder="Giá»›i thiá»‡u ngáº¯n vá» cá»­a hÃ ng, sáº£n pháº©m chuyÃªn bÃ¡n, kinh nghiá»‡m..."><c:out value="${not empty param.shopDescription ? param.shopDescription : existingProfile.shopDescription}"/></textarea>
                            </div>
                        </div>

                        <!-- Danh má»¥c kinh doanh -->
                        <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-3">
                            <h3 class="text-xs font-bold text-primary uppercase tracking-wider flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">category</span>
                                Danh má»¥c sáº£n pháº©m dá»± kiáº¿n kinh doanh
                            </h3>
                            <p class="text-[10px] text-outline">CÃ³ thá»ƒ chá»n nhiá»u danh má»¥c</p>
                            <div class="grid grid-cols-2 md:grid-cols-3 gap-2">
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
                                                <c:when test="${not empty existingProfile and not empty existingProfile.preferredCategories}">
                                                    <c:set var="prefCatContains" value="[${cat.categoryId},"/>
                                                    <c:set var="prefCatContainsMiddle" value=",${cat.categoryId},"/>
                                                    <c:set var="prefCatContainsEnd" value=",${cat.categoryId}]"/>
                                                    <c:set var="prefCatSingle" value="[${cat.categoryId}]"/>
                                                    <c:if test="${existingProfile.preferredCategories == prefCatSingle || 
                                                                  fn:contains(existingProfile.preferredCategories, prefCatContains) || 
                                                                  fn:contains(existingProfile.preferredCategories, prefCatContainsMiddle) || 
                                                                  fn:contains(existingProfile.preferredCategories, prefCatContainsEnd)}">
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
                                        <p class="col-span-3 text-xs text-outline italic">KhÃ´ng táº£i Ä‘Æ°á»£c danh má»¥c.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Upload tÃ i liá»‡u xÃ¡c minh -->
                        <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-3">
                            <h3 class="text-xs font-bold text-primary uppercase tracking-wider flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">upload_file</span>
                                TÃ i liá»‡u xÃ¡c minh kinh doanh
                            </h3>
                            <p class="text-[10px] text-outline">Tá»‘i Ä‘a 10 tÃ i liá»‡u â€¢ Má»—i file â‰¤ 25MB â€¢ PDF, JPG, PNG, DOCX</p>
                            <p class="text-xs text-on-surface-variant">VÃ­ dá»¥: Giáº¥y phÃ©p kinh doanh (GPKD), Chá»©ng nháº­n ATTP, Giáº¥y chá»©ng nháº­n VietGAP...</p>

                            <div class="border-2 border-dashed border-primary/20 rounded-xl p-6 text-center bg-white/20 hover:bg-emerald-50/50 transition-colors cursor-pointer group relative" id="shopDropzone">
                                <input class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                                       id="shopDocs" name="shopDocs" type="file"
                                       multiple accept=".pdf,.jpg,.jpeg,.png,.docx"
                                       onchange="handleDocSelect(this)"
                                       <c:if test="${empty requestScope.shopApplyDraftDocPaths}">required</c:if>>
                                <div class="flex flex-col items-center gap-2 pointer-events-none">
                                    <span class="material-symbols-outlined text-[40px] text-primary/50 group-hover:text-primary transition-colors">cloud_upload</span>
                                    <p class="text-sm font-medium text-on-surface-variant" id="shopUploadLabel">
                                        KÃ©o tháº£ tÃ i liá»‡u hoáº·c <span class="text-primary font-bold">chá»n tá»« thiáº¿t bá»‹</span>
                                    </p>
                                </div>
                            </div>
                            <ul id="shopFileList" class="mt-2 space-y-1 hidden"></ul>
                            <p id="shopFileError" class="text-xs text-red-600 hidden"></p>
                            <c:if test="${not empty requestScope.shopApplyDraftDocPaths}">
                                <div class="rounded-xl border border-emerald-200 bg-emerald-50/70 p-4 space-y-3">
                                    <div class="flex items-center gap-2">
                                        <span class="material-symbols-outlined text-primary text-[20px]">draft</span>
                                        <div>
                                            <p class="text-sm font-semibold text-primary">TÃ i liá»‡u nhÃ¡p Ä‘Ã£ lÆ°u trÃªn há»‡ thá»‘ng</p>
                                            <p class="text-xs text-on-surface-variant">Báº¡n cÃ³ thá»ƒ sá»­a cÃ¡c trÆ°á»ng khÃ¡c mÃ  khÃ´ng pháº£i chá»n láº¡i file.</p>
                                        </div>
                                    </div>
                                    <div class="space-y-2">
                                        <c:forEach var="draftDocPath" items="${requestScope.shopApplyDraftDocPaths}">
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

                        <!-- Terms -->
                        <div class="flex items-start gap-3">
                            <div class="flex items-center h-5 mt-0.5">
                                <input class="w-4 h-4 rounded border-outline/30 text-primary focus:ring-primary bg-white cursor-pointer"
                                       id="agreeTerms" name="agreeTerms" type="checkbox" <c:if test="${not empty param.agreeTerms}">checked</c:if> required>
                            </div>
                            <label class="text-xs text-on-surface-variant leading-relaxed cursor-pointer" for="agreeTerms">
                                TÃ´i cam káº¿t thÃ´ng tin cung cáº¥p lÃ  chÃ­nh xÃ¡c vÃ  Ä‘á»“ng Ã½ vá»›i
                                <a class="text-primary font-bold hover:underline" href="#">Äiá»u khoáº£n dÃ nh cho Ä‘á»‘i tÃ¡c bÃ¡n hÃ ng</a>
                                cá»§a MetaFruit.
                            </label>
                        </div>

                        <!-- Submit -->
                        <button type="submit" id="submitBtn"
                                class="w-full bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                            <span class="material-symbols-outlined text-[20px]">send</span>
                            <span>Gá»­i Ä‘Æ¡n Ä‘Äƒng kÃ½</span>
                        </button>
                    </form>
                </div>
            </c:if>

        </div>
    </main>

    <script>
        const MAX_DOC = 10;
        const MAX_SIZE = 25 * 1024 * 1024;
        const ALLOWED = ['pdf', 'jpg', 'jpeg', 'png', 'docx'];

        function handleDocSelect(input) {
            const listEl = document.getElementById('shopFileList');
            const errorEl = document.getElementById('shopFileError');
            const label = document.getElementById('shopUploadLabel');
            const files = Array.from(input.files);

            listEl.innerHTML = '';
            listEl.classList.add('hidden');
            errorEl.classList.add('hidden');
            let errors = [];

            if (files.length > MAX_DOC) {
                errors.push(`Chá»‰ Ä‘Æ°á»£c chá»n tá»‘i Ä‘a ${'${'}MAX_DOC} tÃ i liá»‡u.`);
            }

            files.slice(0, MAX_DOC).forEach(file => {
                const ext = file.name.split('.').pop().toLowerCase();
                const li = document.createElement('li');
                li.className = 'flex items-center gap-2 text-xs py-1.5 px-3 bg-white/60 rounded-lg border border-outline/20';
                if (!ALLOWED.includes(ext)) {
                    errors.push(`"${'${'}file.name}" â€” sai Ä‘á»‹nh dáº¡ng.`);
                    li.innerHTML = `<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">${'${'}file.name} â€” KhÃ´ng há»— trá»£</span>`;
                } else if (file.size > MAX_SIZE) {
                    errors.push(`"${'${'}file.name}" â€” vÆ°á»£t quÃ¡ 25MB.`);
                    li.innerHTML = `<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">${'${'}file.name} â€” ${'${'}(file.size/1024/1024).toFixed(1)}MB (quÃ¡ 25MB)</span>`;
                } else {
                    li.innerHTML = `<span class="material-symbols-outlined text-primary text-[16px]">description</span><span>${'${'}file.name}</span><span class="ml-auto text-outline">${'${'}(file.size/1024/1024).toFixed(1)}MB</span>`;
                }
                listEl.appendChild(li);
            });

            if (files.length > 0) {
                listEl.classList.remove('hidden');
                label.innerHTML = `ÄÃ£ chá»n <span class="text-primary font-bold">${'${'}Math.min(files.length, MAX_DOC)} tá»‡p</span>`;
            }
            if (errors.length > 0) {
                errorEl.textContent = errors[0];
                errorEl.classList.remove('hidden');
                // Block submit náº¿u cÃ³ lá»—i
                document.getElementById('submitBtn').disabled = true;
            } else {
                document.getElementById('submitBtn').disabled = false;
            }
        }
    </script>
</body>
</html>
