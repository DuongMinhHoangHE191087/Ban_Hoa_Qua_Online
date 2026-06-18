<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Gian Hàng | Kênh Người Bán</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Material Symbols -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">

    <!-- Tailwind & SweetAlert -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#14532D',
                        'primary-hover': '#166534',
                        'primary-light': '#4d661c',
                        'primary-container': '#d9f99d',
                        'on-primary-container': '#597428',
                        secondary: '#31694b',
                        'secondary-container': '#b4f0c9',
                        tertiary: '#486554',
                        'tertiary-container': '#d5f5e0',
                        surface: '#f0fdf4',
                        'on-surface': '#00210d',
                        'on-surface-variant': '#44483b',
                        outline: '#75796a',
                        'outline-variant': '#c5c8b7',
                        error: '#ba1a1a',
                        'error-container': '#ffdad6',
                        amber: '#f59e0b',
                        orange: '#ea580c',
                    },
                    fontFamily: { sans: ['Lexend', 'sans-serif'] }
                }
            }
        }
    </script>

    <style>
        body { background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 40%, #f0fdf4 80%); font-family: 'Lexend', sans-serif; }
        .glass { background: rgba(255, 255, 255, 0.78); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.55); box-shadow: 0 4px 24px -4px rgba(20, 83, 45, 0.07); }
        .form-input {
            width: 100%;
            padding: 10px 14px;
            border: 1.5px solid #c5c8b7;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Lexend', sans-serif;
            background: rgba(255, 255, 255, 0.8);
            color: #00210d;
            outline: none;
            transition: all 0.2s;
        }
        .form-input:focus {
            border-color: #14532D;
            box-shadow: 0 0 0 3px rgba(20, 83, 45, 0.12);
            background: white;
        }
        .upload-zone {
            border: 2px dashed #c5c8b7;
            border-radius: 12px;
            background: rgba(255, 255, 255, 0.5);
            cursor: pointer;
            transition: all 0.25s;
        }
        .upload-zone:hover, .upload-zone.drag-over {
            border-color: #14532D;
            background: rgba(20, 83, 45, 0.05);
        }
        .img-overlay {
            position: absolute;
            inset: 0;
            background: rgba(0,0,0,0.35);
            opacity: 0;
            transition: opacity 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            border-radius: inherit;
        }
        .img-wrap:hover .img-overlay { opacity: 1; }
        .img-wrap { position: relative; border-radius: inherit; cursor: pointer; }
    </style>
</head>
<body class="antialiased text-on-surface">
<div class="flex min-h-screen">

    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="profile"/>
    </jsp:include>

    <!-- Main Content -->
    <main class="flex-1 p-4 md:p-6 lg:p-8 overflow-y-auto">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-[#e8fbe8] to-[#cbf7cb] border border-[#b7f7c3]/60 p-6 rounded-3xl shadow-sm mb-6">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary tracking-tight flex items-center gap-2">
                    <span class="material-symbols-outlined text-2xl">storefront</span>
                    Quản lý Gian Hàng
                </h1>
                <p class="text-on-surface-variant text-xs md:text-sm mt-1">Cập nhật hồ sơ thương hiệu, logo, ảnh bìa và địa chỉ kho của bạn.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-white/80 border border-[#b7f7c3]/80 px-4 py-2 rounded-2xl text-primary shadow-sm">
                <span class="material-symbols-outlined text-base">verified</span>
                <span class="text-xs font-bold uppercase tracking-wider">Hồ sơ Seller</span>
            </div>
        </div>

        <!-- Flash / Alert Message -->
        <c:if test="${not empty sessionScope.flashMsg}">
            <script>
                Swal.fire({
                    icon: '${sessionScope.flashType == "error" ? "error" : "success"}',
                    title: '${sessionScope.flashType == "error" ? "Thất bại" : "Thành công"}',
                    text: '${sessionScope.flashMsg}',
                    confirmButtonColor: '#14532D',
                    timer: 3000
                });
            </script>
            <c:remove var="flashMsg" scope="session"/>
            <c:remove var="flashType" scope="session"/>
        </c:if>

        <!-- Main Layout: Form columns + Live preview -->
        <div class="grid grid-cols-1 xl:grid-cols-5 gap-6">

            <!-- LEFT: Edit forms -->
            <div class="xl:col-span-3 flex flex-col gap-6">

                <!-- SECTION: Cover banner upload -->
                <div class="glass rounded-2xl p-5 border border-white/40">
                    <div class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-primary text-xl">photo_camera</span>
                            <h2 class="font-bold text-on-surface text-base">Ảnh Bìa Gian Hàng</h2>
                        </div>
                        <span class="text-xs bg-primary/10 text-primary rounded-full px-2 py-0.5 font-semibold">Tỉ lệ 16:5</span>
                    </div>

                    <div class="relative mb-4 img-wrap" style="border-radius:12px; height:180px; overflow:hidden; background:#b7f7c3;">
                        <img id="banner-preview-img" 
                             src="${not empty shopProfile.coverUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.coverUrl) : pageContext.request.contextPath.concat('/assets/images/default-banner.png')}" 
                             alt="Ảnh bìa" style="width:100%;height:100%;object-fit:cover;">
                        <div class="img-overlay">
                            <label for="banner-file-input" class="bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-bold px-3 py-2 cursor-pointer">
                                <span class="material-symbols-outlined text-sm align-middle">upload</span> Thay đổi
                            </label>
                        </div>
                    </div>

                    <input type="file" id="banner-file-input" accept="image/*" class="hidden" onchange="uploadImage(this, 'cover')">

                    <div class="upload-zone p-6 flex flex-col items-center justify-center gap-2" 
                         onclick="document.getElementById('banner-file-input').click()"
                         ondragover="dragOverHandler(event)" ondragleave="dragLeaveHandler(event)" ondrop="dropHandler(event, 'cover')">
                        <span class="material-symbols-outlined text-3xl text-outline">add_photo_alternate</span>
                        <p class="text-xs font-semibold text-on-surface-variant">Kéo thả ảnh vào đây hoặc <span class="text-primary underline cursor-pointer">chọn file</span></p>
                        <p class="text-[10px] text-outline">JPG, PNG, WebP · Tối đa 5MB</p>
                    </div>
                </div>

                <!-- SECTION: Logo upload -->
                <div class="glass rounded-2xl p-5 border border-white/40">
                    <div class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-primary text-xl">account_circle</span>
                            <h2 class="font-bold text-on-surface text-base">Ảnh Đại Diện (Logo Shop)</h2>
                        </div>
                        <span class="text-xs bg-primary/10 text-primary rounded-full px-2 py-0.5 font-semibold">Tỉ lệ 1:1</span>
                    </div>

                    <div class="flex gap-6 items-center">
                        <div class="img-wrap shrink-0" style="width:96px;height:96px;border-radius:16px;overflow:hidden;border:3px solid white;box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                            <img id="avatar-preview-img" 
                                 src="${not empty shopProfile.logoUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.logoUrl) : pageContext.request.contextPath.concat('/assets/images/default-logo.png')}" 
                                 alt="Logo" style="width:100%;height:100%;object-fit:cover;">
                            <div class="img-overlay">
                                <label for="logo-file-input" class="cursor-pointer">
                                    <span class="material-symbols-outlined text-white text-2xl">edit</span>
                                </label>
                            </div>
                        </div>

                        <div class="flex-1">
                            <input type="file" id="logo-file-input" accept="image/*" class="hidden" onchange="uploadImage(this, 'logo')">
                            <div class="upload-zone p-4 flex flex-col items-center justify-center gap-1"
                                 onclick="document.getElementById('logo-file-input').click()"
                                 ondragover="dragOverHandler(event)" ondragleave="dragLeaveHandler(event)" ondrop="dropHandler(event, 'logo')">
                                <span class="material-symbols-outlined text-2xl text-outline">upload_file</span>
                                <p class="text-xs font-semibold text-on-surface-variant text-center">Kéo thả hoặc <span class="text-primary underline">chọn ảnh</span></p>
                                <p class="text-[10px] text-outline text-center">JPG, PNG · Tối đa 2MB</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- SECTION: Shop Info Fields -->
                <div class="glass rounded-2xl p-5 border border-white/40">
                    <div class="flex items-center justify-between mb-4 pb-2 border-b border-outline-variant/30">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-primary text-xl">edit_note</span>
                            <h2 class="font-bold text-on-surface text-base">Thông Tin Gian Hàng</h2>
                        </div>
                        <span class="text-xs bg-orange/10 text-orange rounded-full px-2 py-0.5 font-semibold hidden" id="dirty-indicator">Chưa lưu</span>
                    </div>

                    <form action="${pageContext.request.contextPath}/shop/profile" method="post" class="space-y-4" id="shop-info-form">
                        <!-- Shop name -->
                        <div>
                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5" for="shopName">Tên Gian Hàng <span class="text-error">*</span></label>
                            <input type="text" id="shopName" name="shopName" class="form-input font-medium" 
                                   value="<c:out value='${shopProfile.shopName}'/>" required maxlength="50" oninput="markDirty()">
                        </div>

                        <!-- Business Email -->
                        <div>
                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5" for="businessEmail">Email Doanh Nghiệp</label>
                            <input type="email" id="businessEmail" name="businessEmail" class="form-input font-medium" 
                                   value="<c:out value='${shopProfile.businessEmail}'/>" placeholder="VD: shop@domain.com" oninput="markDirty()">
                        </div>

                        <!-- Description -->
                        <div>
                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5" for="shopDescription">Mô tả gian hàng</label>
                            <textarea id="shopDescription" name="shopDescription" rows="4" class="form-input font-medium" 
                                      placeholder="Mô tả sản phẩm, phong cách, cam kết chất lượng..." oninput="markDirty()"><c:out value="${shopProfile.shopDescription}"/></textarea>
                        </div>

                        <!-- Category -->
                        <div>
                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5" for="preferredCategories">Danh mục kinh doanh chính</label>
                            <input type="text" id="preferredCategories" name="preferredCategories" class="form-input font-medium" 
                                   value="<c:out value='${shopProfile.preferredCategories}'/>" placeholder="VD: Trái cây organic, Trái cây sấy khô" oninput="markDirty()">
                        </div>

                        <!-- Delivery Address -->
                        <div>
                            <label class="block text-xs font-bold text-on-surface-variant mb-1.5" for="deliveryAddress">Địa chỉ nhà kho (lấy hàng) <span class="text-error">*</span></label>
                            <textarea id="deliveryAddress" name="deliveryAddress" rows="3" class="form-input font-medium" required
                                      placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố..." oninput="markDirty()"><c:out value="${shopProfile.deliveryAddress}"/></textarea>
                        </div>

                        <!-- Settings shortcut -->
                        <div class="flex items-center gap-3 p-3.5 bg-[#f0faf3] rounded-xl border border-[#b7f7c3]/60">
                            <i class="fa-solid fa-sliders text-primary text-base"></i>
                            <div class="flex-1">
                                <p class="text-xs font-bold text-primary">Cài đặt cảnh báo tồn kho &amp; hết hạn lô</p>
                                <p class="text-[10px] text-on-surface-variant">Ngưỡng tồn kho thấp &amp; số ngày cảnh báo trước hết hạn đã được chuyển sang trang Cài đặt riêng.</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/shop/settings"
                               class="text-xs font-bold text-primary hover:underline whitespace-nowrap flex items-center gap-1">
                                Đến Cài đặt <i class="fa-solid fa-arrow-right text-[10px]"></i>
                            </a>
                        </div>

                        <!-- Submit Buttons -->
                        <div class="flex items-center gap-3 pt-3 border-t border-outline-variant/30">
                            <button type="submit" class="flex-1 sm:flex-none inline-flex items-center justify-center gap-2 bg-primary hover:bg-primary-hover text-white text-sm font-bold px-6 py-2.5 rounded-xl shadow-md transition-all">
                                <span class="material-symbols-outlined text-base">save</span> Lưu thông tin
                            </button>
                            <button type="reset" class="flex-1 sm:flex-none text-xs font-bold text-on-surface-variant bg-white border border-[#c5c8b7] px-6 py-2.5 rounded-xl hover:bg-gray-50 transition-colors" onclick="clearDirty()">
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
                    <div class="glass rounded-3xl overflow-hidden border border-white/50 shadow-lg">
                        <div class="bg-primary/5 px-5 py-3 border-b border-[#b7f7c3]/30 flex justify-between items-center">
                            <span class="text-xs font-bold text-primary flex items-center gap-1">
                                <span class="material-symbols-outlined text-sm">visibility</span> Xem trước trực tiếp
                            </span>
                            <a href="${pageContext.request.contextPath}/shop-view?id=${shopProfile.profileId}" target="_blank" class="text-xs text-primary font-semibold hover:underline flex items-center gap-0.5">
                                Xem trang công khai <span class="material-symbols-outlined text-xs">open_in_new</span>
                            </a>
                        </div>

                        <div class="p-4 bg-[#f0fdf4]">
                            <!-- Banner background mockup -->
                            <div class="relative rounded-2xl overflow-hidden h-[120px] bg-emerald-200">
                                <img id="mockup-banner" 
                                     src="${not empty shopProfile.coverUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.coverUrl) : pageContext.request.contextPath.concat('/assets/images/default-banner.png')}" 
                                     alt="Banner Mockup" class="w-full h-full object-cover">
                                <div class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent"></div>
                                
                                <!-- Overlapping avatar -->
                                <div class="absolute -bottom-6 left-4 z-10">
                                    <div class="w-14 h-14 rounded-xl overflow-hidden border-2 border-white bg-white shadow-md">
                                        <img id="mockup-logo" 
                                             src="${not empty shopProfile.logoUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.logoUrl) : pageContext.request.contextPath.concat('/assets/images/default-logo.png')}" 
                                             alt="Logo Mockup" class="w-full h-full object-cover">
                                    </div>
                                </div>
                            </div>

                            <!-- Shop stats & details -->
                            <div class="pt-8 px-2 pb-2">
                                <h3 class="text-base font-bold text-primary" id="mockup-name"><c:out value="${shopProfile.shopName}"/></h3>
                                <p class="text-xs text-on-surface-variant line-clamp-2 mt-1 leading-relaxed" id="mockup-desc"><c:out value="${shopProfile.shopDescription}"/></p>

                                <div class="flex flex-wrap gap-2 mt-3 text-[10px]">
                                    <span class="bg-[#d9f99d] text-primary rounded-full px-2.5 py-0.5 font-bold">⭐ ${shopProfile.rating} Đánh giá</span>
                                    <span class="bg-primary/10 text-primary rounded-full px-2.5 py-0.5 font-semibold">15.2k Theo dõi</span>
                                    <span class="bg-[#d5f5e0] text-[#486554] rounded-full px-2.5 py-0.5 font-semibold">128 Sản phẩm</span>
                                </div>

                                <div class="flex gap-2 mt-4">
                                    <button class="flex-1 bg-primary text-white text-xs font-bold py-2 rounded-lg shadow-sm">Theo dõi</button>
                                    <button class="flex-1 bg-white border border-primary text-primary text-xs font-bold py-2 rounded-lg">Chat</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Completeness Checklist -->
                    <div class="glass rounded-3xl p-5 border border-white/50 shadow-md">
                        <div class="flex items-center justify-between mb-3">
                            <h3 class="text-sm font-bold text-primary">Độ hoàn thiện hồ sơ</h3>
                            <span class="text-xs font-extrabold text-primary" id="completeness-score">80%</span>
                        </div>
                        <div class="w-full bg-[#dcfce7] h-2 rounded-full overflow-hidden mb-4">
                            <div class="bg-gradient-to-r from-primary to-green-500 h-full rounded-full transition-all duration-500" style="width: 80%;" id="completeness-bar"></div>
                        </div>

                        <div class="space-y-2.5 text-xs text-on-surface-variant" id="checklist-container">
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-green-500 text-base" id="chk-logo">check_circle</span>
                                <span>Đã tải lên Logo Cửa Hàng</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-green-500 text-base" id="chk-banner">check_circle</span>
                                <span>Đã thiết lập Ảnh bìa gian hàng</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-green-500 text-base" id="chk-name">check_circle</span>
                                <span>Tên gian hàng hợp lệ</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-green-500 text-base" id="chk-desc">check_circle</span>
                                <span>Mô tả shop thu hút khách hàng</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-[#c5c8b7] text-base" id="chk-email">radio_button_unchecked</span>
                                <span>Email doanh nghiệp liên hệ</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-green-500 text-base" id="chk-address">check_circle</span>
                                <span>Địa chỉ kho lấy hàng</span>
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
        calculateScore();
    }

    function clearDirty() {
        document.getElementById('dirty-indicator').classList.add('hidden');
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
        const formData = new FormData();
        formData.append("file", file);

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
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            Swal.close();
            if (data.success) {
                // Update views
                if (type === 'logo') {
                    document.getElementById('avatar-preview-img').src = data.url;
                    document.getElementById('mockup-logo').src = data.url;
                } else {
                    document.getElementById('banner-preview-img').src = data.url;
                    document.getElementById('mockup-banner').src = data.url;
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
                    text: data.message || 'Có lỗi xảy ra',
                    confirmButtonColor: '#ba1a1a'
                });
            }
        })
        .catch(err => {
            Swal.close();
            Swal.fire({
                icon: 'error',
                title: 'Lỗi mạng',
                text: 'Không thể kết nối đến máy chủ',
                confirmButtonColor: '#ba1a1a'
            });
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

    // Run score calculations on page load
    window.addEventListener('DOMContentLoaded', calculateScore);
</script>
</body>
</html>
