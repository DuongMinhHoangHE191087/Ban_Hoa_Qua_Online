<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý Gian Hàng | MetaFruit</title>
                <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

                <!-- Google Fonts & Material Symbols -->
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet"
                    href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

                <style>
                    .shop-profile-shell {
                        width: 100%;
                    }

                    .shop-profile-grid {
                        align-items: start;
                    }

                    .shop-profile-panel,
                    .shop-profile-preview,
                    .shop-profile-checklist {
                        overflow: hidden !important;
                        border-radius: 28px !important;
                        border: 1px solid rgba(255, 255, 255, 0.56) !important;
                        background: rgba(255, 255, 255, 0.78) !important;
                        box-shadow: 0 16px 40px rgba(20, 83, 45, 0.08) !important;
                        backdrop-filter: blur(16px);
                        -webkit-backdrop-filter: blur(16px);
                    }

                    .shop-profile-panel > div:first-child,
                    .shop-profile-preview > div:first-child,
                    .shop-profile-checklist > div:first-child {
                        padding: 1rem 1.25rem !important;
                        border-bottom: 1px solid rgba(193, 204, 182, 0.45) !important;
                        background: linear-gradient(90deg, rgba(237, 253, 242, 0.9) 0%, rgba(255, 255, 255, 0.92) 55%, rgba(255, 255, 255, 0.8) 100%) !important;
                    }

                    .shop-profile-panel .upload-zone {
                        transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
                    }

                    .shop-profile-panel .upload-zone:hover {
                        transform: translateY(-2px);
                        border-color: rgba(20, 83, 45, 0.35) !important;
                        background: rgba(240, 253, 244, 0.9) !important;
                        box-shadow: 0 12px 30px rgba(20, 83, 45, 0.08) !important;
                    }

                    .shop-profile-panel .upload-zone:hover .material-symbols-outlined {
                        color: #14532d !important;
                    }

                    .shop-profile-panel .img-wrap {
                        box-shadow: 0 14px 30px rgba(20, 83, 45, 0.12) !important;
                    }

                    .shop-profile-preview > div:last-child {
                        background: radial-gradient(circle at top, rgba(217, 249, 157, 0.45), transparent 55%), linear-gradient(180deg, #f8fff7 0%, #eef8ef 100%) !important;
                    }
                </style>
                
                <!-- Tailwind & SweetAlert -->
                <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
                <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

            </head>

            <body class="antialiased text-txt bg-background">
                <div class="flex min-h-screen">

                    <!-- Shared Sidebar -->
                    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
                        <jsp:param name="activePage" value="profile" />
                    </jsp:include>

                    <!-- Main Content -->
                    <main class="flex-1 p-4 md:p-6 lg:p-8 overflow-y-auto animate-fade-in-up opacity-0">
                        <div class="shop-profile-shell relative mx-auto flex w-full max-w-7xl flex-col gap-6">

                            <section
                                class="relative overflow-hidden rounded-[32px] border border-primary-fixed/50 bg-[linear-gradient(135deg,rgba(240,253,244,0.98)_0%,rgba(251,255,249,0.92)_45%,rgba(255,255,255,0.88)_100%)] px-6 py-6 shadow-[0_20px_60px_rgba(20,83,45,0.08)] md:px-8 md:py-7">
                                <div class="pointer-events-none absolute inset-0">
                                    <div class="absolute -top-16 right-6 h-40 w-40 rounded-full bg-primary/10 blur-3xl"></div>
                                    <div class="absolute -bottom-16 left-1/4 h-44 w-44 rounded-full bg-emerald-200/60 blur-3xl"></div>
                                    <div class="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-primary/30 to-transparent"></div>
                                </div>

                                <div class="relative flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
                                    <div class="max-w-2xl">
                                        <div
                                            class="inline-flex items-center gap-2 rounded-full border border-primary-fixed/60 bg-white/80 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-primary-dark shadow-sm">
                                            <span class="material-symbols-outlined text-sm">storefront</span>
                                            Shop profile studio
                                        </div>
                                        <h1 class="mt-4 text-2xl font-extrabold tracking-tight text-primary-dark md:text-4xl">
                                            Quản lý Gian Hàng
                                        </h1>
                                        <p class="mt-3 max-w-2xl text-sm leading-6 text-txt-2 md:text-[15px]">
                                            Tạo bộ nhận diện rõ ràng cho shop: ảnh bìa, logo và thông tin vận hành được gom vào
                                            một màn hình có cấu trúc gọn, sang và dễ kiểm soát.
                                        </p>
                                        <div class="mt-5 flex flex-wrap gap-3">
                                            <div
                                                class="flex items-center gap-3 rounded-2xl border border-white/80 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                                                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
                                                    <span class="material-symbols-outlined text-[22px]">verified</span>
                                                </span>
                                                <div>
                                                    <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-txt-2">Trạng thái</p>
                                                    <p class="text-sm font-bold text-primary-dark">Seller profile</p>
                                                </div>
                                            </div>
                                            <div
                                                class="flex items-center gap-3 rounded-2xl border border-white/80 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                                                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-100 text-emerald-600">
                                                    <span class="material-symbols-outlined text-[22px]">photo_library</span>
                                                </span>
                                                <div>
                                                    <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-txt-2">Tài sản</p>
                                                    <p class="text-sm font-bold text-primary-dark">Ảnh bìa + logo</p>
                                                </div>
                                            </div>
                                            <div
                                                class="flex items-center gap-3 rounded-2xl border border-white/80 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                                                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
                                                    <span class="material-symbols-outlined text-[22px]">auto_graph</span>
                                                </span>
                                                <div>
                                                    <p class="text-[10px] font-bold uppercase tracking-[0.18em] text-txt-2">Mục tiêu</p>
                                                    <p class="text-sm font-bold text-primary-dark">Hoàn thiện hồ sơ</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="grid w-full gap-3 sm:grid-cols-3 xl:max-w-xl">
                                        <div class="rounded-[24px] border border-white/70 bg-white/85 p-4 shadow-sm backdrop-blur">
                                            <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-txt-2">Nhận diện</p>
                                            <p class="mt-2 text-sm font-semibold leading-6 text-primary-dark">Logo và ảnh bìa giúp shop trông tin cậy hơn ngay từ cái nhìn đầu tiên.</p>
                                        </div>
                                        <div class="rounded-[24px] border border-white/70 bg-primary/5 p-4 shadow-sm">
                                            <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-primary">Trực tiếp</p>
                                            <p class="mt-2 text-sm font-semibold leading-6 text-primary-dark">Preview ở cột phải phản hồi ngay khi bạn đổi ảnh hoặc nội dung.</p>
                                        </div>
                                        <div class="rounded-[24px] border border-white/70 bg-white/85 p-4 shadow-sm backdrop-blur">
                                            <p class="text-[10px] font-bold uppercase tracking-[0.22em] text-txt-2">Quy trình</p>
                                            <p class="mt-2 text-sm font-semibold leading-6 text-primary-dark">Hoàn tất nội dung rồi bấm lưu để cập nhật hồ sơ chính thức.</p>
                                        </div>
                                    </div>
                                </div>
                            </section>

                        <!-- Page Header -->
                        <div
                            class="hidden items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-3xl shadow-sm mb-6">
                            <div>
                                <h1
                                    class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight flex items-center gap-2">
                                    <span class="material-symbols-outlined text-2xl">storefront</span>
                                    Quản lý Gian Hàng
                                </h1>
                                <p class="text-txt-2 text-xs md:text-sm mt-1">Cập nhật hồ sơ thương hiệu,
                                    logo, ảnh bìa và địa chỉ kho của bạn.</p>
                            </div>
                            <div
                                class="hidden md:flex items-center gap-2 bg-surface/80 border border-primary-fixed/80 px-4 py-2 rounded-2xl text-primary-dark shadow-sm">
                                <span class="material-symbols-outlined text-base">verified</span>
                                <span class="text-xs font-bold uppercase tracking-wider">Hồ sơ Seller</span>
                            </div>
                        </div>



                        <!-- Main Layout: Form columns + Live preview -->
                        <div class="shop-profile-grid grid grid-cols-1 gap-6 xl:grid-cols-5">

                            <!-- LEFT: Edit forms -->
                            <div class="xl:col-span-3 flex flex-col gap-6">

                                <!-- SECTION: Cover banner upload -->
                                <div class="shop-profile-panel glass overflow-hidden rounded-[28px] border border-white/50 p-5 shadow-[0_16px_40px_rgba(20,83,45,0.08)]">
                                    <div
                                        class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-xl">photo_camera</span>
                                            <h2 class="font-bold text-on-surface text-base">Ảnh Bìa Gian Hàng</h2>
                                        </div>
                                        <span
                                            class="text-xs bg-primary/10 text-primary rounded-full px-2 py-0.5 font-semibold">Tỉ
                                            lệ 16:5</span>
                                    </div>

                                    <div class="relative mb-4 img-wrap rounded-[24px] h-[220px] overflow-hidden bg-[#b7f7c3]">
                                        <img id="banner-preview-img"
                                            src="${not empty shopProfile.coverUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.coverUrl) : pageContext.request.contextPath.concat('/assets/images/default-banner.png')}"
                                            alt="Ảnh bìa" class="w-full h-full object-cover">
                                        <div class="img-overlay">
                                            <label for="banner-file-input"
                                                class="bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-bold px-3 py-2 cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-sm align-middle">upload</span>
                                                Thay đổi
                                            </label>
                                        </div>
                                    </div>

                                    <input type="file" id="banner-file-input" accept="image/*" class="hidden"
                                        onchange="uploadImage(this, 'cover')">

                                    <div class="upload-zone p-6 flex flex-col items-center justify-center gap-2"
                                        onclick="document.getElementById('banner-file-input').click()"
                                        ondragover="dragOverHandler(event)" ondragleave="dragLeaveHandler(event)"
                                        ondrop="dropHandler(event, 'cover')">
                                        <span
                                            class="material-symbols-outlined text-3xl text-outline">add_photo_alternate</span>
                                        <p class="text-xs font-semibold text-on-surface-variant">Kéo thả ảnh vào đây
                                            hoặc <span class="text-primary underline cursor-pointer">chọn file</span>
                                        </p>
                                        <p class="text-[10px] text-outline">JPG, PNG, WebP · Tối đa 5MB</p>
                                    </div>
                                </div>

                                <!-- SECTION: Logo upload -->
                                <div class="shop-profile-panel glass rounded-2xl p-5 border border-white/40">
                                    <div
                                        class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-xl">account_circle</span>
                                            <h2 class="font-bold text-on-surface text-base">Ảnh Đại Diện (Logo Shop)
                                            </h2>
                                        </div>
                                        <span
                                            class="text-xs bg-primary/10 text-primary rounded-full px-2 py-0.5 font-semibold">Tỉ
                                            lệ 1:1</span>
                                    </div>

                                    <div class="flex gap-6 items-center">
                                        <div class="img-wrap shrink-0 w-28 h-28 rounded-[24px] overflow-hidden border-[3px] border-white shadow-[0_4px_12px_rgba(0,0,0,0.1)]">
                                            <img id="avatar-preview-img"
                                                src="${not empty shopProfile.logoUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.logoUrl) : pageContext.request.contextPath.concat('/assets/images/default-logo.png')}"
                                                alt="Logo" class="w-full h-full object-cover">
                                            <div class="img-overlay">
                                                <label for="logo-file-input" class="cursor-pointer">
                                                    <span
                                                        class="material-symbols-outlined text-white text-2xl">edit</span>
                                                </label>
                                            </div>
                                        </div>

                                        <div class="flex-1">
                                            <input type="file" id="logo-file-input" accept="image/*" class="hidden"
                                                onchange="uploadImage(this, 'logo')">
                                            <div class="upload-zone p-4 flex flex-col items-center justify-center gap-1"
                                                onclick="document.getElementById('logo-file-input').click()"
                                                ondragover="dragOverHandler(event)"
                                                ondragleave="dragLeaveHandler(event)"
                                                ondrop="dropHandler(event, 'logo')">
                                                <span
                                                    class="material-symbols-outlined text-2xl text-outline">upload_file</span>
                                                <p class="text-xs font-semibold text-on-surface-variant text-center">Kéo
                                                    thả hoặc <span class="text-primary underline">chọn ảnh</span></p>
                                                <p class="text-[10px] text-outline text-center">JPG, PNG · Tối đa 2MB
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- SECTION: Shop Info Fields -->
                                <div class="shop-profile-panel glass rounded-2xl p-5 border border-white/40">
                                    <div
                                        class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-xl">edit_note</span>
                                            <h2 class="font-bold text-on-surface text-base">Thông Tin Gian Hàng</h2>
                                        </div>
                                        <span
                                            class="text-xs bg-orange/10 text-orange rounded-full px-2 py-0.5 font-semibold hidden"
                                            id="dirty-indicator">Chưa lưu</span>
                                    </div>

                                    <form action="${pageContext.request.contextPath}/shop/profile" method="post"
                                        class="space-y-4" id="shop-info-form">
                                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                        <!-- Shop name -->
                                        <div>
                                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5"
                                                for="shopName">Tên Gian Hàng <span class="text-error">*</span></label>
                                            <input type="text" id="shopName" name="shopName"
                                                class="form-input font-medium"
                                                value="<c:out value='${shopProfile.shopName}'/>" required maxlength="50"
                                                oninput="markDirty()">
                                        </div>

                                        <!-- Business Email -->
                                        <div>
                                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5"
                                                for="businessEmail">Email Doanh Nghiệp</label>
                                            <input type="email" id="businessEmail" name="businessEmail"
                                                class="form-input font-medium"
                                                value="<c:out value='${shopProfile.businessEmail}'/>"
                                                placeholder="VD: shop@domain.com" oninput="markDirty()">
                                        </div>

                                        <!-- Description -->
                                        <div>
                                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5"
                                                for="shopDescription">Mô tả gian hàng</label>
                                            <textarea id="shopDescription" name="shopDescription" rows="4"
                                                class="form-input font-medium"
                                                placeholder="Mô tả sản phẩm, phong cách, cam kết chất lượng..."
                                                oninput="markDirty()"><c:out value="${shopProfile.shopDescription}"/></textarea>
                                        </div>

                                        <!-- Category -->
                                        <div>
                                            <label class="block text-xs font-bold text-on-surface-variant mb-2">Danh mục
                                                kinh doanh chính</label>
                                            <div
                                                class="grid gap-2 rounded-[24px] border border-outline-variant/40 bg-gradient-to-br from-white/80 to-primary/5 p-4 sm:grid-cols-2 xl:grid-cols-3">
                                                <c:forEach var="cat" items="${categories}">
                                                    <c:set var="prefJson" value="${shopProfile.preferredCategories}" />
                                                    <c:set var="searchToken1" value="[${cat.categoryId}]" />
                                                    <c:set var="searchToken2" value="[${cat.categoryId}," />
                                                    <c:set var="searchToken3" value=",${cat.categoryId}]" />
                                                    <c:set var="searchToken4" value=",${cat.categoryId}," />
                                                    <c:set var="isChecked"
                                                        value="${fn:contains(prefJson, searchToken1) || fn:contains(prefJson, searchToken2) || fn:contains(prefJson, searchToken3) || fn:contains(prefJson, searchToken4)}" />

                                                    <label
                                                        class="group flex items-center gap-2 rounded-2xl border border-outline-variant/40 bg-white/85 px-3 py-2.5 text-xs font-medium text-on-surface-variant shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:bg-primary/5">
                                                        <input type="checkbox" name="categoryIds"
                                                            value="${cat.categoryId}"
                                                            class="h-4 w-4 cursor-pointer rounded border-[#c5c8b7] text-primary focus:ring-primary"
                                                            ${isChecked ? 'checked' : '' } onchange="markDirty()">
                                                        <span class="leading-snug">
                                                            <c:out value="${cat.name}" />
                                                        </span>
                                                    </label>
                                                </c:forEach>
                                            </div>
                                        </div>

                                        <!-- Delivery Address -->
                                        <div>
                                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5"
                                                for="deliveryAddress">Địa chỉ nhà kho (lấy hàng) <span
                                                    class="text-error">*</span></label>
                                            <textarea id="deliveryAddress" name="deliveryAddress" rows="3"
                                                class="form-input font-medium" required
                                                placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố..."
                                                oninput="markDirty()"><c:out value="${shopProfile.deliveryAddress}"/></textarea>
                                        </div>

                                        <!-- Settings shortcut -->
                                        <div
                                            class="flex items-center gap-3 p-3.5 bg-[#f0faf3] rounded-xl border border-[#b7f7c3]/60">
                                            <i class="fa-solid fa-sliders text-primary text-base"></i>
                                            <div class="flex-1">
                                                <p class="text-xs font-bold text-primary">Cài đặt cảnh báo tồn kho &amp;
                                                    hết hạn lô</p>
                                                <p class="text-[10px] text-on-surface-variant">Ngưỡng tồn kho thấp &amp;
                                                    số ngày cảnh báo trước hết hạn đã được chuyển sang trang Cài đặt
                                                    riêng.</p>
                                            </div>
                                            <a href="${pageContext.request.contextPath}/shop/settings"
                                                class="text-xs font-bold text-primary hover:underline whitespace-nowrap flex items-center gap-1">
                                                Đến Cài đặt <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                            </a>
                                        </div>

                                        <!-- Submit Buttons -->
                                        <div class="flex items-center gap-3 pt-3 border-t border-outline-variant/30">
                                            <button type="submit"
                                                class="flex-1 sm:flex-none inline-flex items-center justify-center gap-2 rounded-2xl bg-primary px-6 py-3 text-sm font-bold text-white shadow-md transition-all hover:bg-primary-hover">
                                                <span class="material-symbols-outlined text-base">save</span> Lưu thông
                                                tin
                                            </button>
                                            <button type="reset"
                                                class="flex-1 sm:flex-none rounded-2xl border border-[#c5c8b7] bg-white px-6 py-3 text-xs font-bold text-on-surface-variant transition-colors hover:bg-gray-50"
                                                onclick="clearDirty()">
                                                Đặt lại
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <!-- RIGHT: Public Live Preview & Completeness Checklist -->
                            <div class="xl:col-span-2">
                                <div class="sticky top-6 flex flex-col gap-6">

                                    <!-- Live Mockup (Shopee/TikTok style) -->
                                    <div class="shop-profile-preview glass rounded-3xl overflow-hidden border border-white/50 shadow-lg">
                                        <div
                                            class="bg-primary/5 px-5 py-3 border-b border-[#b7f7c3]/30 flex justify-between items-center">
                                            <span class="text-xs font-bold text-primary flex items-center gap-1">
                                                <span class="material-symbols-outlined text-sm">visibility</span> Xem
                                                trước trực tiếp
                                            </span>
                                            <a href="${pageContext.request.contextPath}/shop-view?id=${shopProfile.profileId}&idType=profile"
                                                target="_blank"
                                                class="text-xs text-primary font-semibold hover:underline flex items-center gap-0.5">
                                                Xem trang công khai <span
                                                    class="material-symbols-outlined text-xs">open_in_new</span>
                                            </a>
                                        </div>

                                        <div class="p-4 bg-[#f0fdf4]">
                                            <!-- Banner background mockup -->
                                            <div class="relative rounded-[28px] overflow-hidden h-[160px] bg-emerald-200">
                                                <img id="mockup-banner"
                                                    src="${not empty shopProfile.coverUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.coverUrl) : pageContext.request.contextPath.concat('/assets/images/default-banner.png')}"
                                                    alt="Banner Mockup" class="w-full h-full object-cover">
                                                <div
                                                    class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent">
                                                </div>

                                                <!-- Overlapping avatar -->
                                                <div class="absolute -bottom-7 left-4 z-10">
                                                    <div
                                                        class="w-16 h-16 rounded-[22px] overflow-hidden border-4 border-white bg-white shadow-[0_10px_24px_rgba(0,0,0,0.15)]">
                                                        <img id="mockup-logo"
                                                            src="${not empty shopProfile.logoUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.logoUrl) : pageContext.request.contextPath.concat('/assets/images/default-logo.png')}"
                                                            alt="Logo Mockup" class="w-full h-full object-cover">
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Shop stats & details -->
                                            <div class="pt-10 px-2 pb-2">
                                                <h3 class="text-base font-bold text-primary" id="mockup-name">
                                                    <c:out value="${shopProfile.shopName}" />
                                                </h3>
                                                <p class="text-xs text-on-surface-variant line-clamp-2 mt-1 leading-relaxed"
                                                    id="mockup-desc">
                                                    <c:out value="${shopProfile.shopDescription}" />
                                                </p>

                                                <div class="flex flex-wrap gap-2 mt-3 text-[10px]">
                                                    <span
                                                        class="bg-[#d9f99d] text-primary rounded-full px-2.5 py-0.5 font-bold">⭐
                                                        ${shopProfile.rating} Đánh giá</span>
                                                    <span
                                                        class="bg-primary/10 text-primary rounded-full px-2.5 py-0.5 font-semibold">15.2k
                                                        Theo dõi</span>
                                                    <span
                                                        class="bg-[#d5f5e0] text-[#486554] rounded-full px-2.5 py-0.5 font-semibold">128
                                                        Sản phẩm</span>
                                                </div>
                                                <!-- Category tags mockup -->
                                                <div class="flex flex-wrap gap-1.5 mt-3"
                                                    id="mockup-categories-container"></div>

                                                <div class="flex gap-2 mt-4">
                                                    <button
                                                        class="flex-1 rounded-2xl bg-primary py-2.5 text-xs font-bold text-white shadow-md">Theo
                                                        dõi</button>
                                                    <button
                                                        class="flex-1 rounded-2xl border border-primary bg-white py-2.5 text-xs font-bold text-primary">Chat</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Completeness Checklist -->
                                    <div class="shop-profile-checklist glass rounded-3xl p-5 border border-white/50 shadow-md">
                                        <div class="flex items-center justify-between mb-3">
                                            <h3 class="text-sm font-bold text-primary">Độ hoàn thiện hồ sơ</h3>
                                            <span class="text-xs font-extrabold text-primary"
                                                id="completeness-score">80%</span>
                                        </div>
                                        <div class="w-full bg-[#dcfce7] h-2 rounded-full overflow-hidden mb-4">
                                            <div class="bg-gradient-to-r from-primary to-green-500 h-full rounded-full transition-all duration-500 w-4/5"
                                                id="completeness-bar"></div>
                                        </div>

                                        <div class="space-y-2.5 text-xs text-on-surface-variant"
                                            id="checklist-container">
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-green-500 text-base"
                                                    id="chk-logo">check_circle</span>
                                                <span>Đã tải lên Logo Cửa Hàng</span>
                                            </div>
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-green-500 text-base"
                                                    id="chk-banner">check_circle</span>
                                                <span>Đã thiết lập Ảnh bìa gian hàng</span>
                                            </div>
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-green-500 text-base"
                                                    id="chk-name">check_circle</span>
                                                <span>Tên gian hàng hợp lệ</span>
                                            </div>
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-green-500 text-base"
                                                    id="chk-desc">check_circle</span>
                                                <span>Mô tả shop thu hút khách hàng</span>
                                            </div>
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-[#c5c8b7] text-base"
                                                    id="chk-email">radio_button_unchecked</span>
                                                <span>Email doanh nghiệp liên hệ</span>
                                            </div>
                                            <div class="flex items-center gap-3 rounded-2xl border border-white/70 bg-white/80 px-3 py-2.5 shadow-sm">
                                                <span class="material-symbols-outlined text-green-500 text-base"
                                                    id="chk-address">check_circle</span>
                                                <span>Địa chỉ kho lấy hàng</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>
                        </div>
                    </main>
                </div>

                <script>
                    function markDirty() {
                        document.getElementById('dirty-indicator').classList.remove('hidden');
                        // Live updates for mockup text
                        document.getElementById('mockup-name').innerText = document.getElementById('shopName').value || "Tên Gian Hàng";
                        document.getElementById('mockup-desc').innerText = document.getElementById('shopDescription').value || "Chưa có mô tả gian hàng.";
                        updateMockupCategories();
                        calculateScore();
                    }

                    function updateMockupCategories() {
                        const container = document.getElementById('mockup-categories-container');
                        if (!container) return;
                        container.innerHTML = '';
                        const checkedBoxes = document.querySelectorAll('input[name="categoryIds"]:checked');
                        checkedBoxes.forEach(cb => {
                            const label = cb.closest('label');
                            if (label) {
                                const span = label.querySelector('span');
                                if (span) {
                                    const tag = document.createElement('span');
                                    tag.className = 'inline-flex items-center rounded-full border border-primary/20 bg-white/85 px-2.5 py-1 text-[10px] font-semibold text-primary-dark shadow-sm';
                                    tag.textContent = span.textContent;
                                    container.appendChild(tag);
                                }
                            }
                        });
                    }

                    function clearDirty() {
                        document.getElementById('dirty-indicator').classList.add('hidden');
                    }

                    function getUploadCsrfToken() {
                        const csrfInput = document.querySelector('#shop-info-form input[name="_csrf"]');
                        return csrfInput ? csrfInput.value.trim() : '';
                    }

                    // Drag and drop events
                    function dragOverHandler(e) {
                        e.preventDefault();
                        e.currentTarget.classList.add('drag-over');
                    }

                    function dragLeaveHandler(e) {
                        e.currentTarget.classList.remove('drag-over');
                    }

                    function dropHandler(e, type) {
                        e.preventDefault();
                        e.currentTarget.classList.remove('drag-over');

                        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                            const file = e.dataTransfer.files[0];
                            const fileInputId = type === 'logo' ? 'logo-file-input' : 'banner-file-input';
                            const fileInput = document.getElementById(fileInputId);

                            // Assign files to input
                            const dataTransfer = new DataTransfer();
                            dataTransfer.items.add(file);
                            fileInput.files = dataTransfer.files;

                            uploadImage(fileInput, type);
                        }
                    }

                    // Ajax Image Upload Function
                    function uploadImage(input, type) {
                        if (!input.files || !input.files[0]) return;

                        const file = input.files[0];
                        const csrfToken = getUploadCsrfToken();
                        if (!csrfToken) {
                            Swal.fire({
                                icon: 'error',
                                title: 'Thiếu CSRF token',
                                text: 'Phiên làm việc hiện tại không hợp lệ. Vui lòng tải lại trang và thử lại.',
                                confirmButtonColor: '#ba1a1a'
                            });
                            return;
                        }

                        const formData = new FormData();
                        formData.append("file", file);
                        formData.set("_csrf", csrfToken);

                        // Show loading toast
                        Swal.fire({
                            title: 'Đang tải lên...',
                            text: 'Vui lòng chờ trong giây lát',
                            allowOutsideClick: false,
                            didOpen: () => {
                                Swal.showLoading();
                            }
                        });

                        fetch('${pageContext.request.contextPath}/api/shop/upload?type=' + type, {
                            method: 'POST',
                            headers: {
                                'X-Requested-With': 'XMLHttpRequest',
                                'X-CSRF-Token': csrfToken
                            },
                            body: formData
                        })
                            .then(async res => {
                                const text = await res.text();
                                let data = {};
                                if (text) {
                                    try {
                                        data = JSON.parse(text);
                                    } catch (parseError) {
                                        throw new Error('Phản hồi upload không hợp lệ.');
                                    }
                                }
                                if (!res.ok) {
                                    throw data;
                                }
                                return data;
                            })
                            .then(data => {
                                Swal.close();
                                const payload = data && data.data ? data.data : {};
                                const uploadedUrl = payload.url || data.url;

                                if (data.success && uploadedUrl) {
                                    // Update views
                                    if (type === 'logo') {
                                        document.getElementById('avatar-preview-img').src = uploadedUrl;
                                        document.getElementById('mockup-logo').src = uploadedUrl;
                                    } else {
                                        document.getElementById('banner-preview-img').src = uploadedUrl;
                                        document.getElementById('mockup-banner').src = uploadedUrl;
                                    }

                                    Swal.fire({
                                        icon: 'success',
                                        title: 'Tải lên thành công!',
                                        confirmButtonColor: '#14532D',
                                        timer: 1500
                                    });
                                    calculateScore();
                                } else {
                                    Swal.fire({
                                        icon: 'error',
                                        title: 'Lỗi upload',
                                        text: data.error || payload.error || payload.message || 'Có lỗi xảy ra',
                                        confirmButtonColor: '#ba1a1a'
                                    });
                                }
                            })
                            .catch(err => {
                                Swal.close();
                                Swal.fire({
                                    icon: 'error',
                                    title: 'Lỗi upload',
                                    text: (err && (err.error || err.message)) || 'Không thể tải lên ảnh. Vui lòng thử lại.',
                                    confirmButtonColor: '#ba1a1a'
                                });
                            })
                            .finally(() => {
                                input.value = '';
                            });
                    }

                    // Calculate completeness score dynamically
                    function calculateScore() {
                        let score = 0;
                        const totalItems = 6;

                        // 1. Logo
                        const logoSrc = document.getElementById('avatar-preview-img').src;
                        const hasLogo = !logoSrc.includes('default-logo.png');
                        if (hasLogo) {
                            score++;
                            document.getElementById('chk-logo').innerText = 'check_circle';
                            document.getElementById('chk-logo').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-logo').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-logo').style.color = '#c5c8b7';
                        }

                        // 2. Banner
                        const bannerSrc = document.getElementById('banner-preview-img').src;
                        const hasBanner = !bannerSrc.includes('default-banner.png');
                        if (hasBanner) {
                            score++;
                            document.getElementById('chk-banner').innerText = 'check_circle';
                            document.getElementById('chk-banner').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-banner').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-banner').style.color = '#c5c8b7';
                        }

                        // 3. Name
                        const nameVal = document.getElementById('shopName').value.trim();
                        if (nameVal.length > 2) {
                            score++;
                            document.getElementById('chk-name').innerText = 'check_circle';
                            document.getElementById('chk-name').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-name').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-name').style.color = '#c5c8b7';
                        }

                        // 4. Description
                        const descVal = document.getElementById('shopDescription').value.trim();
                        if (descVal.length > 10) {
                            score++;
                            document.getElementById('chk-desc').innerText = 'check_circle';
                            document.getElementById('chk-desc').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-desc').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-desc').style.color = '#c5c8b7';
                        }

                        // 5. Email
                        const emailVal = document.getElementById('businessEmail').value.trim();
                        if (emailVal.length > 5) {
                            score++;
                            document.getElementById('chk-email').innerText = 'check_circle';
                            document.getElementById('chk-email').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-email').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-email').style.color = '#c5c8b7';
                        }

                        // 6. Address
                        const addrVal = document.getElementById('deliveryAddress').value.trim();
                        if (addrVal.length > 5) {
                            score++;
                            document.getElementById('chk-address').innerText = 'check_circle';
                            document.getElementById('chk-address').style.color = '#22c55e';
                        } else {
                            document.getElementById('chk-address').innerText = 'radio_button_unchecked';
                            document.getElementById('chk-address').style.color = '#c5c8b7';
                        }

                        const pct = Math.round((score / totalItems) * 100);
                        document.getElementById('completeness-score').innerText = pct + '%';
                        document.getElementById('completeness-bar').style.width = pct + '%';
                    }

                    // Run score and mockup updates on page load
                    window.addEventListener('DOMContentLoaded', function () {
                        calculateScore();
                        updateMockupCategories();
                    });
                </script>
            </body>

            </html>
