<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>${shopProfile.shopName} - Trang Cửa Hàng | Verdant Market</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Fonts & Icons -->
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
                        surface: '#eaffea',
                        'on-surface': '#00210d',
                        'on-surface-variant': '#44483b',
                        outline: '#75796a',
                        'outline-variant': '#c5c8b7',
                        background: '#f0fdf4',
                        error: '#ba1a1a',
                        amber: '#f59e0b',
                        orange: '#ea580c',
                    },
                    fontFamily: { sans: ['Lexend', 'sans-serif'] }
                }
            }
        }
    </script>

    <style>
        body { background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 30%, #f0fdf4 60%, #ecfdf5 100%); min-height: 100vh; }
        .glass { background: rgba(255,255,255,0.72); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 4px 24px -4px rgba(20,83,45,0.08); }
        .glass-subtle { background: rgba(255,255,255,0.5); backdrop-filter: blur(8px); }
        .shop-banner { position: relative; height: 260px; overflow: hidden; border-radius: 1.5rem; }
        @media (min-width: 768px) { .shop-banner { height: 320px; } }
        .shop-banner img { width: 100%; height: 100%; object-fit: cover; }
        .shop-banner::after { content: ''; position: absolute; inset: 0; background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0.1) 60%, transparent 100%); }
        .avatar-ring { border: 4px solid white; box-shadow: 0 0 0 3px #14532D, 0 8px 24px rgba(20,83,45,0.2); }
        .tab-item { transition: all 0.2s; border-bottom: 3px solid transparent; padding-bottom: 10px; cursor: pointer; color: #44483b; font-size: 14px; white-space: nowrap; }
        .tab-item:hover, .tab-item.active { color: #14532D; font-weight: 600; border-bottom-color: #14532D; }
        .product-card { overflow: hidden; border-radius: 1rem; background: rgba(255,255,255,0.8); border: 1px solid rgba(255,255,255,0.6); transition: all 0.25s cubic-bezier(0.4,0,0.2,1); }
        .product-card:hover { transform: translateY(-3px); box-shadow: 0 16px 40px -8px rgba(20,83,45,0.18); border-color: #d9f99d; }
        .product-card img { transition: transform 0.4s ease; }
        .product-card:hover img { transform: scale(1.06); }
    </style>
</head>
<body class="text-on-surface">

<!-- Include Header / Navbar -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="max-w-[1280px] mx-auto px-4 md:px-6 pt-24 pb-20">

    <!-- ===== SHOP PROFILE BAR & BANNER ===== -->
    <div class="relative mb-6">
        <!-- Banner -->
        <div class="shop-banner">
            <img src="${not empty shopProfile.coverUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.coverUrl) : pageContext.request.contextPath.concat('/assets/images/shop_banner.png')}" alt="Shop Banner">
        </div>

        <!-- Overlapping Profile Details -->
        <div class="glass rounded-3xl -mt-12 md:-mt-16 mx-2 md:mx-6 relative z-10 p-5 md:p-6 shadow-xl">
            <div class="flex flex-col md:flex-row items-start md:items-center gap-5">
                <!-- Logo -->
                <div class="relative -mt-16 md:-mt-20 shrink-0 mx-auto md:mx-0">
                    <div class="w-24 h-24 md:w-28 md:h-28 rounded-2xl overflow-hidden avatar-ring bg-white flex items-center justify-center">
                        <img src="${not empty shopProfile.logoUrl ? pageContext.request.contextPath.concat('/').concat(shopProfile.logoUrl) : pageContext.request.contextPath.concat('/assets/images/shop_avatar.png')}" alt="Logo" class="w-full h-full object-cover">
                    </div>
                    <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-green-400 border-2 border-white rounded-full"></div>
                </div>

                <!-- Profile text info -->
                <div class="flex-1 text-center md:text-left w-full">
                    <div class="flex flex-col md:flex-row md:items-center gap-2 mb-2 justify-center md:justify-start">
                        <h1 class="font-extrabold text-primary text-xl md:text-2xl">${shopProfile.shopName}</h1>
                        <span class="inline-flex self-center items-center gap-1 bg-primary/10 text-primary rounded-full px-2.5 py-0.5 text-xs font-bold">
                            <span class="material-symbols-outlined text-xs">verified</span> Chính hãng
                        </span>
                    </div>
                    <p class="text-on-surface-variant text-xs md:text-sm max-w-xl">${shopProfile.shopDescription}</p>

                    <!-- Stats Row -->
                    <div class="flex flex-wrap items-center justify-center md:justify-start gap-4 mt-4 text-xs font-medium text-on-surface-variant">
                        <div class="flex items-center gap-1">
                            <span class="material-symbols-outlined text-amber text-base">star</span>
                            <span class="font-bold text-on-surface">${shopProfile.rating}</span> (2.4k đánh giá)
                        </div>
                        <span class="text-outline-variant">|</span>
                        <div>
                            <span class="font-bold text-on-surface">15.2k</span> Người theo dõi
                        </div>
                        <span class="text-outline-variant">|</span>
                        <div>
                            Danh mục: <span class="font-bold text-primary">${shopProfile.preferredCategories}</span>
                        </div>
                    </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-2 w-full md:w-auto shrink-0 justify-center">
                    <button class="flex-1 md:flex-none inline-flex items-center gap-1.5 bg-primary hover:bg-primary-hover text-white text-xs font-bold px-5 py-2.5 rounded-xl transition-all shadow-md">
                        <span class="material-symbols-outlined text-sm">person_add</span> Theo dõi
                    </button>
                    <button id="btnChatWithShop" data-owner-id="${shopProfile.userId}" class="flex-1 md:flex-none inline-flex items-center gap-1.5 bg-white border border-[#c5c8b7] text-on-surface-variant text-xs font-bold px-5 py-2.5 rounded-xl hover:bg-gray-50 transition-colors">
                        <span class="material-symbols-outlined text-sm">chat</span> Chat
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== TABS NAV ===== -->
    <div class="glass rounded-2xl px-5 py-3.5 mb-6">
        <div class="flex gap-8 overflow-x-auto" id="shop-tabs">
            <button class="tab-item active flex items-center gap-1.5 shrink-0" onclick="switchTab('products')">
                <span class="material-symbols-outlined text-base">inventory_2</span> Sản phẩm
            </button>
            <button class="tab-item flex items-center gap-1.5 shrink-0" onclick="switchTab('promotions')">
                <span class="material-symbols-outlined text-base">loyalty</span> Voucher shop
            </button>
            <button class="tab-item flex items-center gap-1.5 shrink-0" onclick="switchTab('reviews')">
                <span class="material-symbols-outlined text-base">reviews</span> Đánh giá & Phản hồi
            </button>
            <button class="tab-item flex items-center gap-1.5 shrink-0" onclick="switchTab('about')">
                <span class="material-symbols-outlined text-base">info</span> Thông tin chi tiết
            </button>
        </div>
    </div>

    <!-- ===== TAB CONTENT CONTENT ===== -->
    <div>
        <!-- TAB: PRODUCTS -->
        <div id="tab-products" class="tab-content animate-fade-in">
            <!-- Search & Filters bar -->
            <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6 glass rounded-2xl p-4">
                <div class="text-sm font-semibold text-on-surface-variant">
                    Tổng số <span class="text-primary font-bold">${products.size()}</span> sản phẩm tươi ngon
                </div>
                <div class="relative w-full sm:max-w-xs">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">search</span>
                    <input type="text" id="shop-product-search" placeholder="Tìm trong shop này..." 
                           class="w-full pl-9 pr-4 py-2 text-xs rounded-xl bg-white/70 border border-[#c5c8b7] focus:border-primary focus:outline-none focus:bg-white transition-all">
                </div>
            </div>

            <!-- Product Grid -->
            <c:choose>
                <c:when test="${empty products}">
                    <div class="text-center py-12 glass rounded-3xl">
                        <span class="material-symbols-outlined text-5xl text-outline mb-2">sentiment_dissatisfied</span>
                        <p class="text-on-surface-variant text-sm font-semibold">Cửa hàng hiện chưa đăng bán sản phẩm nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4" id="product-grid">
                        <c:forEach var="p" items="${products}">
                            <div class="product-card flex flex-col group relative overflow-hidden rounded-2xl bg-white border border-[#c5c8b7]/30 shadow-sm" data-name="${p.name.toLowerCase()}">
                                <!-- Image area -->
                                <div class="relative aspect-[4/3] overflow-hidden bg-white p-1">
                                    <img src="${p.image}" alt="${p.name}" class="w-full h-full object-cover rounded-xl transition-transform duration-300 group-hover:scale-105">
                                    
                                    <!-- Badges -->
                                    <div class="absolute top-2 left-2 flex flex-col gap-1 z-10">
                                        <span class="bg-primary text-white text-[9px] font-bold px-1.5 py-0.5 rounded shadow-sm">HỮU CƠ</span>
                                        <c:if test="${not empty p.discountPercent}">
                                            <span class="bg-red-500 text-white text-[9px] font-bold px-1.5 py-0.5 rounded shadow-sm">-${p.discountPercent}%</span>
                                        </c:if>
                                    </div>
                                    <div class="absolute bottom-2 right-2 bg-black/40 text-white text-[9px] px-1.5 py-0.5 rounded-md backdrop-blur-sm">
                                        Đã bán ${p.soldQuantity}
                                    </div>
                                </div>

                                <!-- Info area -->
                                <div class="p-2.5 flex flex-col flex-1">
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${p.productId}" class="text-xs md:text-sm font-bold text-on-surface hover:text-primary leading-snug line-clamp-2 mb-1 transition-colors">
                                        <c:out value="${p.name}"/>
                                    </a>

                                    <div class="flex items-center gap-1 mb-1.5">
                                        <div class="flex text-amber">
                                            <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                            <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                            <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                            <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                            <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                        </div>
                                        <span class="text-[9px] text-on-surface-variant font-semibold">(${p.rating})</span>
                                    </div>

                                    <div class="flex items-baseline gap-1 mt-auto">
                                        <span class="text-primary font-extrabold text-xs md:text-sm">
                                            <fmt:formatNumber value="${p.price}" pattern="#,##0" /> ₫
                                        </span>
                                        <c:if test="${not empty p.oldPrice}">
                                            <span class="text-outline text-[10px] line-through">
                                                <fmt:formatNumber value="${p.oldPrice}" pattern="#,##0" /> ₫
                                            </span>
                                        </c:if>
                                        <span class="text-outline text-[9px] ml-auto">/${p.unit}</span>
                                    </div>

                                    <!-- Add to cart -->
                                    <button onclick="addToCart('${p.productId}')" class="mt-2 w-full bg-primary hover:bg-primary-hover text-white text-[11px] font-bold py-1.5 rounded-lg transition-all shadow-sm flex items-center justify-center gap-1">
                                        <span class="material-symbols-outlined text-xs">add_shopping_cart</span> Thêm vào giỏ
                                    </button>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <!-- Product Pagination Controls -->
                    <div class="flex justify-center items-center gap-2 mt-8" id="product-pagination">
                        <!-- Populated by JS -->
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- TAB: PROMOTIONS -->
        <div id="tab-promotions" class="tab-content hidden animate-fade-in">
            <c:choose>
                <c:when test="${empty promotions}">
                    <div class="text-center py-12 glass rounded-3xl">
                        <span class="material-symbols-outlined text-5xl text-outline mb-2">loyalty</span>
                        <p class="text-on-surface-variant text-sm font-semibold">Cửa hàng hiện chưa có chương trình ưu đãi nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4" id="promo-grid">
                        <c:forEach var="promo" items="${promotions}">
                            <div class="promo-card glass rounded-2xl p-5 border border-white/40 flex gap-4 items-center" data-code="${promo.code}">
                                <div class="w-12 h-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center shrink-0">
                                    <span class="material-symbols-outlined text-2xl">
                                        <c:choose>
                                            <c:when test="${promo.discountType == 'PERCENT'}">percent</c:when>
                                            <c:otherwise>payments</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="flex-1">
                                    <h4 class="font-bold text-sm text-on-surface">
                                        Giảm <c:choose>
                                            <c:when test="${promo.discountType == 'PERCENT'}">
                                                <fmt:formatNumber value="${promo.discountValue}" pattern="#,##0"/>%
                                            </c:when>
                                            <c:otherwise>
                                                <fmt:formatNumber value="${promo.discountValue}" pattern="#,##0"/>đ
                                            </c:otherwise>
                                        </c:choose>
                                        cho đơn từ <fmt:formatNumber value="${promo.minOrderValue}" pattern="#,##0"/>đ
                                    </h4>
                                    <p class="text-xs text-on-surface-variant mt-0.5">
                                        Mã: <strong class="text-primary">${promo.code}</strong>
                                    </p>
                                </div>
                                <button onclick="copyPromoCode('${promo.code}')" class="bg-primary hover:bg-primary-hover text-white text-xs font-bold px-4 py-2 rounded-xl shadow transition-all">Lưu mã</button>
                            </div>
                        </c:forEach>
                    </div>
                    <!-- Promotions Pagination Controls -->
                    <div class="flex justify-center items-center gap-2 mt-8" id="promo-pagination">
                        <!-- Populated by JS -->
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- TAB: REVIEWS -->
        <div id="tab-reviews" class="tab-content hidden animate-fade-in">
            <div class="glass rounded-3xl p-6 border border-white/40 space-y-6">
                <div class="flex flex-col md:flex-row items-center gap-8 bg-primary/5 rounded-2xl p-6">
                    <div class="text-center">
                        <div class="text-4xl md:text-5xl font-extrabold text-primary">${shopProfile.rating}</div>
                        <div class="flex text-amber my-2 justify-center">
                            <span class="material-symbols-outlined text-base fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            <span class="material-symbols-outlined text-base fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            <span class="material-symbols-outlined text-base fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            <span class="material-symbols-outlined text-base fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            <span class="material-symbols-outlined text-base fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                        </div>
                        <div class="text-xs text-on-surface-variant font-medium">Đánh giá trung bình</div>
                    </div>
                    
                    <div class="flex-1 space-y-2 w-full">
                        <div class="flex items-center gap-2 text-xs">
                            <span class="w-12 text-on-surface-variant font-medium">5 Sao</span>
                            <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                                <div class="bg-primary h-full rounded-full" style="width: 85%"></div>
                            </div>
                            <span class="w-8 text-right font-semibold">85%</span>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span class="w-12 text-on-surface-variant font-medium">4 Sao</span>
                            <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                                <div class="bg-primary h-full rounded-full" style="width: 10%"></div>
                            </div>
                            <span class="w-8 text-right font-semibold">10%</span>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span class="w-12 text-on-surface-variant font-medium">3 Sao</span>
                            <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                                <div class="bg-primary h-full rounded-full" style="width: 4%"></div>
                            </div>
                            <span class="w-8 text-right font-semibold">4%</span>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span class="w-12 text-on-surface-variant font-medium">2 Sao</span>
                            <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                                <div class="bg-primary h-full rounded-full" style="width: 1%"></div>
                            </div>
                            <span class="w-8 text-right font-semibold">1%</span>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span class="w-12 text-on-surface-variant font-medium">1 Sao</span>
                            <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                                <div class="bg-red-400 h-full rounded-full" style="width: 0%"></div>
                            </div>
                            <span class="w-8 text-right font-semibold">0%</span>
                        </div>
                    </div>
                </div>

                <!-- Feedbacks List -->
                <div class="space-y-4">
                    <!-- Feedback Item 1 -->
                    <div class="p-4 rounded-2xl bg-white/40 border border-outline-variant/10 flex gap-3">
                        <img src="https://api.dicebear.com/7.x/adventurer/svg?seed=Felix" class="w-10 h-10 rounded-full shrink-0 object-cover" />
                        <div class="flex-1 space-y-1.5">
                            <div class="flex items-center justify-between">
                                <h4 class="text-xs font-bold text-on-surface">Nguyễn Văn Nam</h4>
                                <span class="text-[10px] text-on-surface-variant">04-06-2026</span>
                            </div>
                            <div class="flex text-amber">
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            </div>
                            <p class="text-xs text-on-surface-variant leading-relaxed font-medium">Trái cây cực kỳ ngon ngọt và tươi mới, đóng gói rất cẩn thận, an tâm khi giao hàng xa.</p>
                        </div>
                    </div>

                    <!-- Feedback Item 2 -->
                    <div class="p-4 rounded-2xl bg-white/40 border border-outline-variant/10 flex gap-3">
                        <img src="https://api.dicebear.com/7.x/adventurer/svg?seed=Aria" class="w-10 h-10 rounded-full shrink-0 object-cover" />
                        <div class="flex-1 space-y-1.5">
                            <div class="flex items-center justify-between">
                                <h4 class="text-xs font-bold text-on-surface">Trần Thị Mai</h4>
                                <span class="text-[10px] text-on-surface-variant">02-06-2026</span>
                            </div>
                            <div class="flex text-amber">
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                                <span class="material-symbols-outlined text-xs fill-1" style="font-variation-settings:'FILL' 1;">star</span>
                            </div>
                            <p class="text-xs text-on-surface-variant leading-relaxed font-medium">Dưa hấu chín mọng nước, ngọt lịm không hạt, đúng chuẩn VietGAP hữu cơ ngon sạch.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- TAB: ABOUT -->
        <div id="tab-about" class="tab-content hidden animate-fade-in">
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Info cards -->
                <div class="lg:col-span-2 glass rounded-3xl p-6 border border-white/40 space-y-4">
                    <h3 class="font-bold text-primary text-base flex items-center gap-1">
                        <span class="material-symbols-outlined text-lg">info</span> Giới thiệu về cửa hàng
                    </h3>
                    <p class="text-xs md:text-sm text-on-surface-variant leading-relaxed">${shopProfile.shopDescription}</p>
                    
                    <div class="h-px bg-outline-variant/30 my-4"></div>
                    
                    <h3 class="font-bold text-primary text-base flex items-center gap-1">
                        <span class="material-symbols-outlined text-lg">location_on</span> Địa chỉ kho & lấy hàng
                    </h3>
                    <p class="text-xs md:text-sm text-on-surface-variant leading-relaxed">${shopProfile.deliveryAddress}</p>
                </div>

                <div class="glass rounded-3xl p-6 border border-white/40 space-y-4">
                    <h3 class="font-bold text-primary text-base">Thông tin vận hành</h3>
                    <div class="space-y-3 text-xs">
                        <div class="flex justify-between py-1.5 border-b border-outline-variant/20">
                            <span class="text-on-surface-variant">Hoạt động từ:</span>
                            <span class="font-semibold text-on-surface">2026</span>
                        </div>
                        <div class="flex justify-between py-1.5 border-b border-outline-variant/20">
                            <span class="text-on-surface-variant">Tỉ lệ phản hồi chat:</span>
                            <span class="font-semibold text-primary">99% (trong vài phút)</span>
                        </div>
                        <div class="flex justify-between py-1.5 border-b border-outline-variant/20">
                            <span class="text-on-surface-variant">Thời gian chuẩn bị hàng:</span>
                            <span class="font-semibold text-on-surface">~1.2 giờ</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<!-- Include Footer -->
<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />

<script>
    function switchTab(tabId) {
        // Hide all tab contents
        document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
        // Show selected tab content
        document.getElementById('tab-' + tabId).classList.remove('hidden');
        
        // Deactivate all tab nav items
        document.querySelectorAll('.tab-item').forEach(el => el.classList.remove('active'));
        // Activate current tab nav item
        event.currentTarget.classList.add('active');
    }

    // Configuration
    const ITEMS_PER_PAGE_PRODUCTS = 8;
    const ITEMS_PER_PAGE_PROMOS = 4;

    let currentProductPage = 1;
    let currentPromoPage = 1;

    function paginateProducts() {
        const query = (document.getElementById('shop-product-search')?.value || '').toLowerCase().trim();
        const cards = Array.from(document.querySelectorAll('#product-grid > .product-card'));
        if (cards.length === 0) return;
        
        // Filter cards first
        const filteredCards = cards.filter(card => {
            const name = card.getAttribute('data-name') || '';
            return name.includes(query);
        });

        // Hide all cards first
        cards.forEach(card => card.style.display = 'none');

        // Calculate pages
        const totalItems = filteredCards.length;
        const totalPages = Math.max(1, Math.ceil(totalItems / ITEMS_PER_PAGE_PRODUCTS));
        
        if (currentProductPage > totalPages) {
            currentProductPage = totalPages;
        }

        // Show cards for current page
        const start = (currentProductPage - 1) * ITEMS_PER_PAGE_PRODUCTS;
        const end = start + ITEMS_PER_PAGE_PRODUCTS;
        const pageCards = filteredCards.slice(start, end);
        pageCards.forEach(card => card.style.setProperty('display', 'flex', 'important'));

        // Render pagination controls
        renderPaginationControls('product-pagination', totalPages, currentProductPage, (page) => {
            currentProductPage = page;
            paginateProducts();
        });
    }

    function paginatePromos() {
        const cards = Array.from(document.querySelectorAll('#promo-grid > .promo-card'));
        if (cards.length === 0) return;
        
        // Hide all cards first
        cards.forEach(card => card.style.display = 'none');

        // Calculate pages
        const totalItems = cards.length;
        const totalPages = Math.max(1, Math.ceil(totalItems / ITEMS_PER_PAGE_PROMOS));
        
        if (currentPromoPage > totalPages) {
            currentPromoPage = totalPages;
        }

        // Show cards for current page
        const start = (currentPromoPage - 1) * ITEMS_PER_PAGE_PROMOS;
        const end = start + ITEMS_PER_PAGE_PROMOS;
        const pageCards = cards.slice(start, end);
        pageCards.forEach(card => card.style.setProperty('display', 'flex', 'important'));

        // Render pagination controls
        renderPaginationControls('promo-pagination', totalPages, currentPromoPage, (page) => {
            currentPromoPage = page;
            paginatePromos();
        });
    }

    function renderPaginationControls(containerId, totalPages, currentPage, onPageChange) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        let html = '';
        
        // Prev button
        if (currentPage > 1) {
            html += '<button onclick="window[\'' + containerId + 'Change\'](' + (currentPage - 1) + ')" class="flex items-center justify-center w-8 h-8 rounded-lg border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">';
            html += '    <span class="material-symbols-outlined text-sm">chevron_left</span>';
            html += '</button>';
        } else {
            html += `<span class="flex items-center justify-center w-8 h-8 rounded-lg border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                <span class="material-symbols-outlined text-sm">chevron_left</span>
            </span>`;
        }

        // Page numbers
        for (let i = 1; i <= totalPages; i++) {
            if (i === currentPage) {
                html += '<span class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary/20 text-primary font-extrabold border border-primary/40 shadow-sm">' + i + '</span>';
            } else {
                html += '<button onclick="window[\'' + containerId + 'Change\'](' + i + ')" class="flex items-center justify-center w-8 h-8 rounded-lg border border-primary/20 bg-white text-on-surface-variant hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">' + i + '</button>';
            }
        }

        // Next button
        if (currentPage < totalPages) {
            html += '<button onclick="window[\'' + containerId + 'Change\'](' + (currentPage + 1) + ')" class="flex items-center justify-center w-8 h-8 rounded-lg border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">';
            html += '    <span class="material-symbols-outlined text-sm">chevron_right</span>';
            html += '</button>';
        } else {
            html += `<span class="flex items-center justify-center w-8 h-8 rounded-lg border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                <span class="material-symbols-outlined text-sm">chevron_right</span>
            </span>`;
        }

        container.innerHTML = html;
        
        // Expose callback globally
        window[containerId + 'Change'] = onPageChange;
    }

    // Client-side search filters inside shop
    const searchInput = document.getElementById('shop-product-search');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            currentProductPage = 1;
            paginateProducts();
        });
    }

    // Copy Voucher Code
    function copyPromoCode(code) {
        navigator.clipboard.writeText(code).then(() => {
            Swal.fire({
                icon: 'success',
                title: 'Đã sao chép!',
                text: 'Đã lưu mã giảm giá: ' + code,
                confirmButtonColor: '#14532D',
                timer: 1500
            });
        }).catch(err => {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi',
                text: 'Không thể sao chép mã giảm giá.',
                confirmButtonColor: '#14532D'
            });
        });
    }

    // Fake Add to Cart
    function addToCart(productId) {
        Swal.fire({
            icon: 'success',
            title: 'Đã thêm vào giỏ!',
            text: 'Sản phẩm đã được thêm vào giỏ hàng thành công.',
            confirmButtonColor: '#14532D',
            timer: 1500
        });
    }

    // Initialize pagination on load
    document.addEventListener('DOMContentLoaded', () => {
        paginateProducts();
        paginatePromos();
    });

    const btnChatWithShop = document.getElementById('btnChatWithShop');
    if (btnChatWithShop) {
        btnChatWithShop.addEventListener('click', function() {
            const ownerId = this.getAttribute('data-owner-id');
            const csrfToken = '${sessionScope._csrfToken}';
            const ctx = '${pageContext.request.contextPath}';
            
            fetch(ctx + '/api/chat', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: new URLSearchParams({
                    action: 'createShopSession',
                    ownerId: ownerId,
                    _csrf: csrfToken
                }).toString()
            })
            .then(r => r.json())
            .then(data => {
                if (data.success && data.data && data.data.sessionId) {
                    window.location.href = ctx + '/chat?sessionId=' + data.data.sessionId;
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Chưa đăng nhập hoặc lỗi',
                        text: data.error || data.message || 'Vui lòng đăng nhập tài khoản khách hàng để nhắn tin với cửa hàng.',
                        confirmButtonColor: '#14532D'
                    });
                }
            })
            .catch(err => {
                console.error(err);
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi kết nối',
                    text: 'Không thể kết nối tới máy chủ.',
                    confirmButtonColor: '#14532D'
                });
            });
        });
    }
</script>
</body>
</html>
