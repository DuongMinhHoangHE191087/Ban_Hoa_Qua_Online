<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
                <%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>

                    <!-- Load header and inject Page Title -->
                    <jsp:include page="/WEB-INF/jsp/common/header.jsp">
                        <jsp:param name="pageTitle" value="Trang chủ - MetaFruit" />
                    </jsp:include>

                    <!-- Google Fonts Lexend & Material Icons for rich premium look -->
                    <link href="https://fonts.googleapis.com" rel="preconnect">
                    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
                    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap"
                        rel="stylesheet">
                    <link rel="stylesheet"
                        href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">

                    <!-- Isolated Tailwind CSS Engine for dynamic layout -->
                    <script
                        src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>

                    <!-- Overriding tailwind configurations to match HomeUI brand precisely -->
                    <script>
                        tailwind.config = {
                            theme: {
                                extend: {
                                    colors: {
                                        "primary": "#4d661c",          // Emerald Green
                                        "primary-hover": "#364e03",
                                        "primary-light": "#d9f99d",
                                        "secondary": "#31694b",        // Deep Forest Green
                                        "surface-bright": "#eaffea",   // Warm Mint Light
                                        "surface-container-low": "#d1ffd8",
                                        "on-surface": "#00210d",
                                        "on-surface-variant": "#44483b",
                                        "tertiary": "#486554",
                                        "tertiary-container": "#d5f5e0"
                                    },
                                    fontFamily: {
                                        sans: ["Lexend", "sans-serif"]
                                    }
                                }
                            }
                        }
                    </script>

                    <script>
                        /**
                         * Handle image loading error by resolving context path casing mismatch or falling back.
                         * Declared globally early in <head> so it's defined before <img> tags render.
                         */
                        window.handleImageError = function (img) {
                            if (!img.dataset.errorStage) {
                                img.dataset.errorStage = "1";
                                // Tier 1: Try a gorgeous fresh fruit Unsplash CDN image
                                img.src = "https://images.unsplash.com/photo-1610832958506-ee5633619144?w=600&auto=format&fit=crop&q=80";
                            } else if (img.dataset.errorStage === "1") {
                                img.dataset.errorStage = "2";
                                // Tier 2: Inline offline-proof highly premium SVG fallback representing MeteFruit brand
                                img.src = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MDAgMzAwIiB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIj48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImIiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiNmNGZiZjciLz48c3RvcCBvZmZzZXQ9IjEwMCUiIHN0b3AtY29sb3I9IiNlMmY1ZWEiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0icCIgeDE9IjAlIiB5MT0iMCUiIHgyPSIxMDAlIiB5Mj0iMTAwJSI+PHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iIzRkNjYxYyIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzMxNjk0YiIvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9InVybCgjYikiIHJ4PSIxNiIvPjxjaXJjbGUgY3g9IjIwMCIgY3k9IjEyMCIgcj0iNDUiIGZpbGw9IiNkOWY5OWQiIG9wYWNpdHk9IjAuNiIvPjxwYXRoIGQ9Ik0yMDAsODVjMjUsMCA0MCwyNSAxNSw1MGMtMjUsMC00MC0yNS0xNS01MHoiIGZpbGw9InVybCgjcCkiLz48cGF0aCBkPSJNMjAwLDEwNWMtMTUsMC0yNSwxNS0xMCwzMGMxNSwwIDI1LTE1IDEwLTMweiIgZmlsbD0iIzg0Y2MxNiIvPjx0ZXh0IHg9IjIwMCIgeT0iMjAwIiBmb250LWZhbWlseT0ic3lzdGVtLXVpLHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtd2VpZ2h0PSI3MDAiIGZpbGw9IiMwMDIxMGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiPlZlcmRhbnQgTWFya2V0PC90ZXh0Pjx0ZXh0IHg9IjIwMCIgeT0iMjI1IiBmb250LWZhbWlseT0ic3lzdGVtLXVpLHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTIiIGZvbnQtd2VpZ2h0PSI0MDAiIGZpbGw9IiM0NDQ4M2IiIHRleHQtYW5jaG9yPSJtaWRkbGUiPk7DtG5nIFPhuqNuIFPhuqFjaCBDYW8gQ2FwPC90ZXh0Pjwvc3ZnPg==";
                            }
                        };
                    </script>

                    <!-- Embedded Premium Style overrides for glassy micro-interactions -->
                    <style>
                        /* Clean layout and glass effects overrides */
                        .glass-panel {
                            background: rgba(255, 255, 255, 0.75);
                            backdrop-filter: blur(12px);
                            -webkit-backdrop-filter: blur(12px);
                            border: 1px solid rgba(255, 255, 255, 0.4);
                        }

                        .ambient-shadow {
                            box-shadow: 0 10px 40px rgba(20, 83, 45, 0.06);
                        }

                        .flash-glow {
                            box-shadow: 0 0 25px rgba(239, 68, 68, 0.15);
                        }

                        .hide-scrollbar::-webkit-scrollbar {
                            display: none;
                        }

                        .hide-scrollbar {
                            -ms-overflow-style: none;
                            scrollbar-width: none;
                        }

                        /* Override navbar to have custom premium glassy layout */
                        .navbar {
                            background: rgba(255, 255, 255, 0.8) !important;
                            backdrop-filter: blur(16px) !important;
                            -webkit-backdrop-filter: blur(16px) !important;
                            border-bottom: 1px solid rgba(255, 255, 255, 0.5) !important;
                            box-shadow: 0 4px 20px rgba(20, 83, 45, 0.03) !important;
                            font-family: 'Lexend', sans-serif !important;
                        }

                        .navbar__logo {
                            color: #4d661c !important;
                            font-weight: 700 !important;
                        }

                        .btn-primary {
                            background: #4d661c !important;
                        }

                        .btn-primary:hover {
                            background: #364e03 !important;
                        }

                        .btn-secondary {
                            border-color: #4d661c !important;
                            color: #4d661c !important;
                        }

                        .navbar__search {
                            display: none !important;
                            /* Hide original small search as we have a massive hero search */
                        }

                        /* Active categories dynamic border */
                        .cat-active {
                            background-color: #4d661c !important;
                            color: #ffffff !important;
                        }
                    </style>

                    <!-- Main Page Background Wrapping -->
                    <div
                        class="bg-gradient-to-br from-surface-bright via-white to-surface-container-low min-h-screen text-on-surface antialiased font-sans">

                        <!-- Hero Section with AI Search Container & Interactive Slideshow (2-Column Layout) -->
                        <section class="relative px-6 md:px-12 pt-28 pb-16 max-w-7xl mx-auto z-10">
                            <!-- Floating organic abstract blobs -->
                            <div
                                class="absolute top-10 left-10 w-72 h-72 bg-primary-light/30 rounded-full blur-3xl pointer-events-none -z-10">
                            </div>
                            <div
                                class="absolute bottom-10 right-10 w-96 h-96 bg-emerald-200/20 rounded-full blur-3xl pointer-events-none -z-10">
                            </div>

                            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
                                <!-- Left: Search & Brand Headline -->
                                <div class="lg:col-span-7 flex flex-col items-start text-left">
                                    <div
                                        class="inline-flex items-center gap-2 bg-emerald-100/80 border border-emerald-200/50 px-4 py-1.5 rounded-full mb-6 text-xs md:text-sm font-semibold text-primary shadow-sm animate-pulse">
                                        <span class="material-symbols-outlined text-[18px]">verified</span>
                                        <span>MetaFrui - Nông sản 100% sạch</span>
                                    </div>

                                    <h1 class="text-3xl md:text-5xl font-extrabold text-on-surface mb-6 leading-tight">
                                        Nông Sản Sạch <br>
                                        <span
                                            class="text-primary bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Hữu
                                            Cơ Cao Cấp</span>
                                    </h1>

                                    <p
                                        class="text-sm md:text-base text-on-surface-variant mb-8 max-w-xl font-light leading-relaxed">
                                        Chào mừng bạn đến <strong>MetaFruit</strong>. Hãy xem và đặt mua những đặc
                                        sản tươi ngon, trực tiếp từ các nhà vườn danh tiếng.
                                    </p>

                                    <!-- Dynamic Search Form -->
                                    <div
                                        class="w-full glass-panel p-2 rounded-full shadow-lg hover:shadow-xl transition-all duration-300 group focus-within:ring-2 focus-within:ring-primary/20 relative z-20">
                                        <form action="${pageContext.request.contextPath}/home" method="get"
                                            class="flex items-center w-full" id="heroSearchForm" onsubmit="handleHeroSearch(event)">
                                            <c:if test="${not empty selectedCategoryId}">
                                                <input type="hidden" name="categoryId" value="${selectedCategoryId}">
                                            </c:if>
                                            <div class="flex items-center flex-1 pl-4 md:pl-6">
                                                <span
                                                    class="material-symbols-outlined text-primary text-[24px] group-focus-within:scale-110 transition-transform">search</span>
                                                <input
                                                    class="w-full bg-transparent border-none text-on-surface placeholder:text-on-surface-variant/70 focus:ring-0 text-sm md:text-base ml-3 outline-none"
                                                    id="searchInput" name="keyword" value="${fn:escapeXml(keyword)}"
                                                    placeholder="Tìm đặc sản sầu riêng Ri6, vải thiều, dâu tây..."
                                                    type="text">
                                            </div>
                                            <button type="submit"
                                                class="bg-primary hover:bg-primary-hover text-white font-semibold text-xs md:text-sm px-6 md:px-8 py-3.5 rounded-full transition-colors flex items-center gap-2 shadow-md">
                                                <span>Tìm kiếm</span>
                                                <span
                                                    class="material-symbols-outlined text-[18px] hidden md:inline">arrow_forward</span>
                                            </button>
                                        </form>
                                    </div>

                                    <!-- AI Suggested Keywords -->
                                    <div class="flex flex-wrap justify-start items-center gap-2 mt-5 max-w-2xl">
                                        <span
                                            class="text-xs text-on-surface-variant flex items-center gap-2 font-semibold">
                                            <img src="${pageContext.request.contextPath}/assets/images/logo_light.png"
                                                alt="MetaFruit"
                                                style="height: 18px; width: 18px; border-radius: 4px; object-fit: cover;">
                                            AI gợi ý:
                                        </span>
                                        <button onclick="applyAiPrompt('sầu riêng chín ngọt béo ngậy')"
                                            class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                            "Sầu riêng chín béo"
                                        </button>
                                        <button onclick="applyAiPrompt('trái cây nhập khẩu giàu vitamin c')"
                                            class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                            "Quả giàu Vitamin C"
                                        </button>
                                        <button onclick="applyAiPrompt('cam vàng ngọt mát')"
                                            class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                            "Cam mọng giải nhiệt"
                                        </button>
                                    </div>
                                </div>

                                <!-- Right: Slideshow Banner -->
                                <div
                                    class="lg:col-span-5 relative w-full aspect-[4/3] rounded-3xl overflow-hidden shadow-2xl border border-white/60 glass-panel">
                                    <div class="absolute inset-0 transition-opacity duration-1000 opacity-100 slide-item"
                                        id="slide-0">
                                        <img src="${pageContext.request.contextPath}/assets/images/banner_fruit_1.png"
                                            alt="MetaFruit Banner 1" class="w-full h-full object-cover">
                                        <div
                                            class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent flex items-end p-6">
                                            <span class="text-white font-medium text-lg">Trái Cây Hữu Cơ Việt
                                                Nam</span>
                                        </div>
                                    </div>
                                    <div class="absolute inset-0 transition-opacity duration-1000 opacity-0 slide-item"
                                        id="slide-1">
                                        <img src="${pageContext.request.contextPath}/assets/images/banner_fruit_2.png"
                                            alt="MetaFruit Banner 2" class="w-full h-full object-cover">
                                        <div
                                            class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent flex items-end p-6">
                                            <span class="text-white font-medium text-lg">Dinh Dưỡng Từ Thiên
                                                Nhiên</span>
                                        </div>
                                    </div>
                                    <div class="absolute inset-0 transition-opacity duration-1000 opacity-0 slide-item"
                                        id="slide-2">
                                        <img src="${pageContext.request.contextPath}/assets/images/banner_fruit_3.png"
                                            alt="MetaFruit Banner 3" class="w-full h-full object-cover">
                                        <div
                                            class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent flex items-end p-6">
                                            <span class="text-white font-medium text-lg">Đạt Chuẩn
                                                VietGAP</span>
                                        </div>
                                    </div>
                                    <div class="absolute inset-0 transition-opacity duration-1000 opacity-0 slide-item"
                                        id="slide-3">
                                        <img src="${pageContext.request.contextPath}/assets/images/banner_fruit_4.png"
                                            alt="MetaFruit Banner 4" class="w-full h-full object-cover">
                                        <div
                                            class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent flex items-end p-6">
                                            <span class="text-white font-medium text-lg">Giao Hàng Tận
                                                Nơi</span>
                                        </div>
                                    </div>

                                    <!-- Indicator Dots -->
                                    <div class="absolute bottom-4 right-6 flex gap-2 z-30">
                                        <span class="w-2.5 h-2.5 rounded-full bg-white/80 cursor-pointer transition-all"
                                            onclick="setSlide(0)" id="dot-0"></span>
                                        <span class="w-2.5 h-2.5 rounded-full bg-white/40 cursor-pointer transition-all"
                                            onclick="setSlide(1)" id="dot-1"></span>
                                        <span class="w-2.5 h-2.5 rounded-full bg-white/40 cursor-pointer transition-all"
                                            onclick="setSlide(2)" id="dot-2"></span>
                                        <span class="w-2.5 h-2.5 rounded-full bg-white/40 cursor-pointer transition-all"
                                            onclick="setSlide(3)" id="dot-3"></span>
                                    </div>
                                </div>
                            </div>
                        </section>


                        <!-- FLASH SALE SECTION (Golden hour countdown) -->
                        <c:if test="${not empty flashSaleProducts}">
                            <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                                <div
                                    class="glass-panel flash-glow rounded-3xl p-6 md:p-8 border-red-200 bg-gradient-to-br from-red-50/95 via-rose-50/90 to-amber-50/90 shadow-[0_20px_50px_rgba(239,68,68,0.15)]">

                                    <!-- Section Header with Real-Time Clock & Slider Nav -->
                                    <div
                                        class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-red-100/50">
                                        <div class="flex items-center gap-2">
                                            <div>
                                                <h2 class="text-xl md:text-2xl font-bold tracking-tight text-red-600">
                                                    SIÊU
                                                    DEAL GIỜ VÀNG - FLASH SALE</h2>
                                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Đặc sản
                                                    nông
                                                    sản giảm sâu siêu tốc. Nhanh tay kẻo lỡ!</p>
                                            </div>
                                        </div>

                                        <div
                                            class="flex flex-wrap items-center gap-4 w-full sm:w-auto justify-between sm:justify-end">
                                            <!-- Countdown Display -->
                                            <div
                                                class="flex items-center gap-2 bg-red-50 border border-red-200/50 px-4 py-2 rounded-2xl shadow-inner">
                                                <span
                                                    class="text-xs font-bold text-red-600 uppercase tracking-wider hidden sm:inline mr-1">Kết
                                                    thúc sau:</span>
                                                <div
                                                    class="flex items-center gap-1 font-mono text-sm font-bold text-red-700">
                                                    <span class="bg-red-600 text-white px-2.5 py-1 rounded-lg"
                                                        id="hourBox">02</span>
                                                    <span>:</span>
                                                    <span class="bg-red-600 text-white px-2.5 py-1 rounded-lg"
                                                        id="minuteBox">45</span>
                                                    <span>:</span>
                                                    <span
                                                        class="bg-red-600 text-white px-2.5 py-1 rounded-lg text-red-100 animate-pulse"
                                                        id="secondBox">12</span>
                                                </div>
                                            </div>

                                            <!-- Slider Navigation Buttons -->
                                            <div class="flex items-center gap-2 border-l border-red-100/80 pl-4">
                                                <button onclick="scrollFlashSale(-1)"
                                                    class="w-9 h-9 rounded-xl border border-red-200 bg-white text-red-600 hover:bg-red-600 hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                    <span
                                                        class="material-symbols-outlined text-[18px] font-bold">chevron_left</span>
                                                </button>
                                                <button onclick="scrollFlashSale(1)"
                                                    class="w-9 h-9 rounded-xl border border-red-200 bg-white text-red-600 hover:bg-red-600 hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                    <span
                                                        class="material-symbols-outlined text-[18px] font-bold">chevron_right</span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Flash Sale Products Horizontal Slider Container -->
                                    <div id="flashSaleContainer"
                                        class="flex gap-6 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
                                        <c:forEach var="item" items="${flashSaleProducts}">
                                            <article data-product-id="${item.productId}"
                                                class="w-[280px] sm:w-[320px] shrink-0 bg-white/90 border border-white/50 rounded-2xl p-3 flex flex-col group hover:-translate-y-1 hover:shadow-md transition-all duration-300 relative overflow-hidden snap-start">
                                                <!-- Discount Tag Badge -->
                                                <div
                                                    class="absolute top-4 left-4 z-10 bg-red-600 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm">
                                                    -
                                                    <c:out value="${item.discountPercent}" />%
                                                </div>

                                                <!-- Clickable Product Area -->
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                    class="block group/link flex-grow flex flex-col justify-between"
                                                    style="text-decoration: none; color: inherit;">
                                                    <!-- Image Section -->
                                                    <div class="relative aspect-[4/3] rounded-xl overflow-hidden mb-4 bg-emerald-50"
                                                        style="aspect-ratio: 4/3;">
                                                        <img src="${item.image}" alt="${item.name}"
                                                            onerror="handleImageError(this)"
                                                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                        <!-- Floating added qty badge -->
                                                        <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                            id="badge-prod-${item.productId}">
                                                            Đã thêm 0
                                                        </div>
                                                    </div>

                                                    <!-- Content Section -->
                                                    <div class="flex-grow flex flex-col justify-between px-1">
                                                        <div>
                                                            <div class="flex justify-between items-start gap-2 mb-1">
                                                                <h3
                                                                    class="font-bold text-sm text-on-surface line-clamp-1 group-hover:text-primary transition-colors">
                                                                    <c:out value="${item.name}" />
                                                                </h3>
                                                            </div>

                                                            <!-- Stars and Unit Info -->
                                                            <div class="flex items-center gap-2 mb-3">
                                                                <div class="text-amber-500 scale-90 -ml-1">
                                                                    <ft:stars rating="${item.rating}"
                                                                        showValue="false" />
                                                                </div>
                                                                <span
                                                                    class="text-[10px] bg-emerald-100 text-primary font-semibold px-2 py-0.5 rounded-full">
                                                                    Đơn vị:
                                                                    <c:out value="${item.unit}" />
                                                                </span>
                                                            </div>

                                                            <!-- Price Tag -->
                                                            <div class="flex items-baseline gap-2 mb-3">
                                                                <span class="text-base font-bold text-red-600">
                                                                    <ft:currency value="${item.price}" />
                                                                </span>
                                                                <span
                                                                    class="text-xs text-on-surface-variant/60 line-through">
                                                                    <ft:currency value="${item.originalPrice}" />
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </a>

                                                <!-- Progress and Buy Actions (outside of standard card link to allow nested form clicks) -->
                                                <div class="space-y-3 px-1 mt-3">
                                                    <!-- Custom Progress Bar -->
                                                    <c:set var="percentRemaining"
                                                        value="${(item.stockRemaining / item.stockTotal) * 100}" />
                                                    <div class="space-y-1">
                                                        <div class="flex justify-between text-[10px] font-semibold">
                                                            <span class="text-red-600">Chỉ còn ${item.stockRemaining}
                                                                ${item.unit}</span>
                                                            <span class="text-on-surface-variant/60">Đã bán
                                                                ${item.stockTotal -
                                                                item.stockRemaining}</span>
                                                        </div>
                                                        <div
                                                            class="w-full bg-gray-100 h-2 rounded-full overflow-hidden border border-gray-200/50">
                                                            <div class="flash-sale-progress bg-gradient-to-r from-red-500 to-orange-500 h-full rounded-full transition-all duration-500"
                                                                data-width="${percentRemaining}"></div>
                                                        </div>
                                                    </div>

                                                    <button type="button"
                                                        onclick="quickAddProduct(event, '${item.productId}')"
                                                        class="w-full bg-red-50 border border-red-200 hover:bg-red-600 hover:text-white text-red-600 font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                        <span
                                                            class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                        Mua ngay Deal sốc
                                                    </button>
                                                </div>
                                            </article>
                                        </c:forEach>

                                        <!-- View All Card -->
                                        <div
                                            class="w-[220px] shrink-0 bg-red-50/50 border border-red-200/50 rounded-2xl p-6 flex flex-col items-center justify-center text-center group hover:bg-red-600 transition-all duration-300 snap-start cursor-pointer">
                                            <a href="${pageContext.request.contextPath}/products"
                                                class="flex flex-col items-center justify-center gap-3 w-full h-full text-red-600 group-hover:text-white"
                                                style="text-decoration: none;">
                                                <span
                                                    class="material-symbols-outlined text-[40px] font-bold">arrow_forward_ios</span>
                                                <span class="font-bold text-sm">Xem tất cả deal sốc</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <!-- BEST SELLERS SECTION -->
                        <c:if test="${not empty bestSellersProducts}">
                            <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                                <div
                                    class="glass-panel rounded-3xl p-6 md:p-8 border-emerald-200 bg-gradient-to-br from-emerald-50/95 via-teal-50/90 to-cyan-50/90 shadow-[0_20px_50px_rgba(16,185,129,0.15)] transition-all duration-300">

                                    <!-- Section Header & Slider Nav -->
                                    <div
                                        class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-emerald-100/50">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-[32px] font-bold">trending_up</span>
                                            <div>
                                                <h2 class="text-xl md:text-2xl font-bold tracking-tight text-primary">
                                                    SẢN PHẨM BÁN CHẠY NHẤT</h2>
                                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Top 10 sản
                                                    phẩm bán chạy nhất, được khách hàng tin tưởng khuyên dùng.</p>
                                            </div>
                                        </div>

                                        <div class="flex items-center gap-2 border-emerald-100/80">
                                            <button onclick="scrollBestSellers(-1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_left</span>
                                            </button>
                                            <button onclick="scrollBestSellers(1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_right</span>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Best Sellers Products Horizontal Slider Container -->
                                    <div id="bestSellersContainer"
                                        class="flex gap-6 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
                                        <c:forEach var="item" items="${bestSellersProducts}">
                                            <article data-product-id="${item.productId}"
                                                class="w-[280px] sm:w-[320px] shrink-0 bg-white/90 border border-white/50 rounded-2xl p-3 flex flex-col group hover:-translate-y-1 hover:shadow-md transition-all duration-300 relative overflow-hidden snap-start">
                                                <c:if test="${item.discountPercent > 0}">
                                                    <!-- Discount Tag Badge -->
                                                    <div
                                                        class="absolute top-4 left-4 z-10 bg-red-600 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm">
                                                        -
                                                        <c:out value="${item.discountPercent}" />%
                                                    </div>
                                                </c:if>

                                                <!-- Clickable Product Area -->
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                    class="block group/link flex-grow flex flex-col justify-between"
                                                    style="text-decoration: none; color: inherit;">
                                                    <!-- Image Section -->
                                                    <div class="relative aspect-[4/3] rounded-xl overflow-hidden mb-4 bg-emerald-50"
                                                        style="aspect-ratio: 4/3;">
                                                        <img src="${item.image}" alt="${item.name}"
                                                            onerror="handleImageError(this)"
                                                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                        <!-- Floating added qty badge -->
                                                        <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                            id="badge-prod-${item.productId}">
                                                            Đã thêm 0
                                                        </div>
                                                    </div>

                                                    <!-- Content Section -->
                                                    <div class="flex-grow flex flex-col justify-between px-1">
                                                        <div>
                                                            <div class="flex justify-between items-start gap-2 mb-1">
                                                                <h3
                                                                    class="font-bold text-sm text-on-surface line-clamp-1 group-hover:text-primary transition-colors">
                                                                    <c:out value="${item.name}" />
                                                                </h3>
                                                            </div>

                                                            <!-- Stars and Unit Info -->
                                                            <div class="flex items-center gap-2 mb-3">
                                                                <div class="text-amber-500 scale-90 -ml-1">
                                                                    <ft:stars rating="${item.rating}"
                                                                        showValue="false" />
                                                                </div>
                                                                <span
                                                                    class="text-[10px] bg-emerald-100 text-primary font-semibold px-2 py-0.5 rounded-full">
                                                                    Đơn vị:
                                                                    <c:out value="${item.unit}" />
                                                                </span>
                                                            </div>

                                                            <!-- Price Tag -->
                                                            <div class="flex items-baseline gap-2 mb-3">
                                                                <span class="text-base font-bold text-primary">
                                                                    <ft:currency value="${item.price}" />
                                                                </span>
                                                                <c:if test="${item.discountPercent > 0}">
                                                                    <span
                                                                        class="text-xs text-on-surface-variant/60 line-through">
                                                                        <ft:currency value="${item.originalPrice}" />
                                                                    </span>
                                                                </c:if>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </a>

                                                <!-- Buy Actions -->
                                                <div class="space-y-3 px-1 mt-3">
                                                    <div class="flex justify-between text-[10px] font-semibold">
                                                        <span class="text-primary-hover">Còn lại: ${item.stockRemaining}
                                                            ${item.unit}</span>
                                                        <span class="text-on-surface-variant/60">Đã bán
                                                            ${item.soldQuantity}</span>
                                                    </div>
                                                    <button type="button"
                                                        onclick="quickAddProduct(event, '${item.productId}')"
                                                        class="w-full bg-primary-light hover:bg-primary hover:text-white text-primary font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                        <span
                                                            class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                        Mua Ngay
                                                    </button>
                                                </div>
                                            </article>
                                        </c:forEach>

                                        <!-- View All Card -->
                                        <div
                                            class="w-[220px] shrink-0 bg-emerald-50/50 border border-primary/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center group hover:bg-primary transition-all duration-300 snap-start cursor-pointer">
                                            <a href="${pageContext.request.contextPath}/products"
                                                class="flex flex-col items-center justify-center gap-3 w-full h-full text-primary group-hover:text-white"
                                                style="text-decoration: none;">
                                                <span
                                                    class="material-symbols-outlined text-[40px] font-bold">arrow_forward_ios</span>
                                                <span class="font-bold text-sm">Xem tất cả bán chạy</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <!-- SEASONAL PRODUCTS SECTION -->
                        <c:if test="${not empty seasonalProducts}">
                            <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                                <div
                                    class="glass-panel rounded-3xl p-6 md:p-8 border-amber-200 bg-gradient-to-br from-amber-50/95 via-orange-50/90 to-yellow-50/90 shadow-[0_20px_50px_rgba(245,158,11,0.15)] transition-all duration-300">

                                    <!-- Section Header & Slider Nav -->
                                    <div
                                        class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-emerald-100/50">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-[32px] font-bold">eco</span>
                                            <div>
                                                <h2 class="text-xl md:text-2xl font-bold tracking-tight text-primary">
                                                    SẢN PHẨM THEO MÙA TƯƠI NGON</h2>
                                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Nông sản
                                                    thu hoạch đúng mùa vụ chính gốc vườn, ngọt lành tự nhiên.</p>
                                            </div>
                                        </div>

                                        <div class="flex items-center gap-2 border-emerald-100/80">
                                            <button onclick="scrollSeasonal(-1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_left</span>
                                            </button>
                                            <button onclick="scrollSeasonal(1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_right</span>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Seasonal Products Horizontal Slider Container -->
                                    <div id="seasonalContainer"
                                        class="flex gap-6 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
                                        <c:forEach var="item" items="${seasonalProducts}">
                                            <article data-product-id="${item.productId}"
                                                class="w-[280px] sm:w-[320px] shrink-0 bg-white/90 border border-white/50 rounded-2xl p-3 flex flex-col group hover:-translate-y-1 hover:shadow-md transition-all duration-300 relative overflow-hidden snap-start">
                                                <c:if test="${item.discountPercent > 0}">
                                                    <!-- Discount Tag Badge -->
                                                    <div
                                                        class="absolute top-4 left-4 z-10 bg-red-600 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm">
                                                        -
                                                        <c:out value="${item.discountPercent}" />%
                                                    </div>
                                                </c:if>

                                                <!-- Clickable Product Area -->
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                    class="block group/link flex-grow flex flex-col justify-between"
                                                    style="text-decoration: none; color: inherit;">
                                                    <!-- Image Section -->
                                                    <div class="relative aspect-[4/3] rounded-xl overflow-hidden mb-4 bg-emerald-50"
                                                        style="aspect-ratio: 4/3;">
                                                        <img src="${item.image}" alt="${item.name}"
                                                            onerror="handleImageError(this)"
                                                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                        <!-- Floating added qty badge -->
                                                        <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                            id="badge-prod-${item.productId}">
                                                            Đã thêm 0
                                                        </div>
                                                    </div>

                                                    <!-- Content Section -->
                                                    <div class="flex-grow flex flex-col justify-between px-1">
                                                        <div>
                                                            <div class="flex justify-between items-start gap-2 mb-1">
                                                                <h3
                                                                    class="font-bold text-sm text-on-surface line-clamp-1 group-hover:text-primary transition-colors">
                                                                    <c:out value="${item.name}" />
                                                                </h3>
                                                            </div>

                                                            <!-- Stars and Unit Info -->
                                                            <div class="flex items-center gap-2 mb-3">
                                                                <div class="text-amber-500 scale-90 -ml-1">
                                                                    <ft:stars rating="${item.rating}"
                                                                        showValue="false" />
                                                                </div>
                                                                <span
                                                                    class="text-[10px] bg-emerald-100 text-primary font-semibold px-2 py-0.5 rounded-full">
                                                                    Đơn vị:
                                                                    <c:out value="${item.unit}" />
                                                                </span>
                                                            </div>

                                                            <!-- Price Tag -->
                                                            <div class="flex items-baseline gap-2 mb-3">
                                                                <span class="text-base font-bold text-primary">
                                                                    <ft:currency value="${item.price}" />
                                                                </span>
                                                                <c:if test="${item.discountPercent > 0}">
                                                                    <span
                                                                        class="text-xs text-on-surface-variant/60 line-through">
                                                                        <ft:currency value="${item.originalPrice}" />
                                                                    </span>
                                                                </c:if>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </a>

                                                <!-- Buy Actions -->
                                                <div class="space-y-3 px-1 mt-3">
                                                    <div class="flex justify-between text-[10px] font-semibold">
                                                        <span class="text-primary-hover">Còn lại: ${item.stockRemaining}
                                                            ${item.unit}</span>
                                                        <span class="text-on-surface-variant/60">Đã bán
                                                            ${item.soldQuantity}</span>
                                                    </div>
                                                    <button type="button"
                                                        onclick="quickAddProduct(event, '${item.productId}')"
                                                        class="w-full bg-primary-light hover:bg-primary hover:text-white text-primary font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                        <span
                                                            class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                        Mua Ngay
                                                    </button>
                                                </div>
                                            </article>
                                        </c:forEach>

                                        <!-- View All Card -->
                                        <div
                                            class="w-[220px] shrink-0 bg-emerald-50/50 border border-primary/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center group hover:bg-primary transition-all duration-300 snap-start cursor-pointer">
                                            <a href="${pageContext.request.contextPath}/products"
                                                class="flex flex-col items-center justify-center gap-3 w-full h-full text-primary group-hover:text-white"
                                                style="text-decoration: none;">
                                                <span
                                                    class="material-symbols-outlined text-[40px] font-bold">arrow_forward_ios</span>
                                                <span class="font-bold text-sm">Xem tất cả theo mùa</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <!-- ORGANIC PRODUCTS SECTION -->
                        <c:if test="${not empty organicProducts}">
                            <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                                <div
                                    class="glass-panel rounded-3xl p-6 md:p-8 border-lime-200 bg-gradient-to-br from-lime-50/95 via-emerald-50/90 to-green-50/90 shadow-[0_20px_50px_rgba(132,204,22,0.15)] transition-all duration-300">
                                    <div
                                        class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-emerald-100/50">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-[32px] font-bold">energy_savings_leaf</span>
                                            <div>
                                                <h2 class="text-xl md:text-2xl font-bold tracking-tight text-primary">
                                                    SẢN PHẨM HỮU CƠ (ORGANIC)</h2>
                                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Nông sản
                                                    chuẩn hữu cơ, không hóa chất độc hại, an toàn tuyệt đối cho gia đình
                                                    bạn.</p>
                                            </div>
                                        </div>
                                        <div class="flex items-center gap-2 border-emerald-100/80">
                                            <button onclick="scrollOrganic(-1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_left</span>
                                            </button>
                                            <button onclick="scrollOrganic(1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_right</span>
                                            </button>
                                        </div>
                                    </div>
                                    <div id="organicContainer"
                                        class="flex gap-6 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
                                        <c:forEach var="item" items="${organicProducts}">
                                            <article data-product-id="${item.productId}"
                                                class="w-[280px] sm:w-[320px] shrink-0 bg-white/90 border border-white/50 rounded-2xl p-3 flex flex-col group hover:-translate-y-1 hover:shadow-md transition-all duration-300 relative overflow-hidden snap-start">
                                                <c:if test="${item.discountPercent > 0}">
                                                    <div
                                                        class="absolute top-4 left-4 z-10 bg-red-600 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm">
                                                        -
                                                        <c:out value="${item.discountPercent}" />%
                                                    </div>
                                                </c:if>
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                    class="block group/link flex-grow flex flex-col justify-between"
                                                    style="text-decoration: none; color: inherit;">
                                                    <div class="relative aspect-[4/3] rounded-xl overflow-hidden mb-4 bg-emerald-50"
                                                        style="aspect-ratio: 4/3;">
                                                        <img src="${item.image}" alt="${item.name}"
                                                            onerror="handleImageError(this)"
                                                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                        <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                            id="badge-prod-${item.productId}">
                                                            Đã thêm 0
                                                        </div>
                                                    </div>
                                                    <div class="flex-grow flex flex-col justify-between px-1">
                                                        <div>
                                                            <div class="flex justify-between items-start gap-2 mb-1">
                                                                <h3
                                                                    class="font-bold text-sm text-on-surface line-clamp-1 group-hover:text-primary transition-colors">
                                                                    <c:out value="${item.name}" />
                                                                </h3>
                                                            </div>
                                                            <div class="flex items-center gap-2 mb-3">
                                                                <div class="text-amber-500 scale-90 -ml-1">
                                                                    <ft:stars rating="${item.rating}"
                                                                        showValue="false" />
                                                                </div>
                                                                <span
                                                                    class="text-[10px] bg-emerald-100 text-primary font-semibold px-2 py-0.5 rounded-full">
                                                                    Đơn vị:
                                                                    <c:out value="${item.unit}" />
                                                                </span>
                                                            </div>
                                                            <div class="flex items-baseline gap-2 mb-3">
                                                                <span class="text-base font-bold text-primary">
                                                                    <ft:currency value="${item.price}" />
                                                                </span>
                                                                <c:if test="${item.discountPercent > 0}">
                                                                    <span
                                                                        class="text-xs text-on-surface-variant/60 line-through">
                                                                        <ft:currency value="${item.originalPrice}" />
                                                                    </span>
                                                                </c:if>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </a>
                                                <div class="space-y-3 px-1 mt-3">
                                                    <div class="flex justify-between text-[10px] font-semibold">
                                                        <span class="text-primary-hover">Còn lại: ${item.stockRemaining}
                                                            ${item.unit}</span>
                                                        <span class="text-on-surface-variant/60">Đã bán
                                                            ${item.soldQuantity}</span>
                                                    </div>
                                                    <button type="button"
                                                        onclick="quickAddProduct(event, '${item.productId}')"
                                                        class="w-full bg-primary-light hover:bg-primary hover:text-white text-primary font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                        <span
                                                            class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                        Mua Ngay
                                                    </button>
                                                </div>
                                            </article>
                                        </c:forEach>

                                        <!-- View All Card -->
                                        <div
                                            class="w-[220px] shrink-0 bg-emerald-50/50 border border-primary/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center group hover:bg-primary transition-all duration-300 snap-start cursor-pointer">
                                            <a href="${pageContext.request.contextPath}/products"
                                                class="flex flex-col items-center justify-center gap-3 w-full h-full text-primary group-hover:text-white"
                                                style="text-decoration: none;">
                                                <span
                                                    class="material-symbols-outlined text-[40px] font-bold">arrow_forward_ios</span>
                                                <span class="font-bold text-sm">Xem tất cả hữu cơ</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <!-- IMPORTED PRODUCTS SECTION -->
                        <c:if test="${not empty importedProducts}">
                            <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                                <div
                                    class="glass-panel rounded-3xl p-6 md:p-8 border-sky-200 bg-gradient-to-br from-sky-50/95 via-blue-50/90 to-indigo-50/90 shadow-[0_20px_50px_rgba(14,165,233,0.15)] transition-all duration-300">
                                    <div
                                        class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-emerald-100/50">
                                        <div class="flex items-center gap-2">
                                            <span
                                                class="material-symbols-outlined text-primary text-[32px] font-bold">public</span>
                                            <div>
                                                <h2 class="text-xl md:text-2xl font-bold tracking-tight text-primary">
                                                    SẢN PHẨM NHẬP KHẨU CAO CẤP</h2>
                                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Trái cây
                                                    nhập khẩu chính ngạch tươi ngon từ các quốc gia danh tiếng.</p>
                                            </div>
                                        </div>
                                        <div class="flex items-center gap-2 border-emerald-100/80">
                                            <button onclick="scrollImported(-1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_left</span>
                                            </button>
                                            <button onclick="scrollImported(1)"
                                                class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer">
                                                <span
                                                    class="material-symbols-outlined text-[18px] font-bold">chevron_right</span>
                                            </button>
                                        </div>
                                    </div>
                                    <div id="importedContainer"
                                        class="flex gap-6 overflow-x-auto pb-4 hide-scrollbar snap-x snap-mandatory">
                                        <c:forEach var="item" items="${importedProducts}">
                                            <article data-product-id="${item.productId}"
                                                class="w-[280px] sm:w-[320px] shrink-0 bg-white/90 border border-white/50 rounded-2xl p-3 flex flex-col group hover:-translate-y-1 hover:shadow-md transition-all duration-300 relative overflow-hidden snap-start">
                                                <c:if test="${item.discountPercent > 0}">
                                                    <div
                                                        class="absolute top-4 left-4 z-10 bg-red-600 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm">
                                                        -
                                                        <c:out value="${item.discountPercent}" />%
                                                    </div>
                                                </c:if>
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                    class="block group/link flex-grow flex flex-col justify-between"
                                                    style="text-decoration: none; color: inherit;">
                                                    <div class="relative aspect-[4/3] rounded-xl overflow-hidden mb-4 bg-emerald-50"
                                                        style="aspect-ratio: 4/3;">
                                                        <img src="${item.image}" alt="${item.name}"
                                                            onerror="handleImageError(this)"
                                                            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                        <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                            id="badge-prod-${item.productId}">
                                                            Đã thêm 0
                                                        </div>
                                                    </div>
                                                    <div class="flex-grow flex flex-col justify-between px-1">
                                                        <div>
                                                            <div class="flex justify-between items-start gap-2 mb-1">
                                                                <h3
                                                                    class="font-bold text-sm text-on-surface line-clamp-1 group-hover:text-primary transition-colors">
                                                                    <c:out value="${item.name}" />
                                                                </h3>
                                                            </div>
                                                            <div class="flex items-center gap-2 mb-3">
                                                                <div class="text-amber-500 scale-90 -ml-1">
                                                                    <ft:stars rating="${item.rating}"
                                                                        showValue="false" />
                                                                </div>
                                                                <span
                                                                    class="text-[10px] bg-emerald-100 text-primary font-semibold px-2 py-0.5 rounded-full">
                                                                    Đơn vị:
                                                                    <c:out value="${item.unit}" />
                                                                </span>
                                                            </div>
                                                            <div class="flex items-baseline gap-2 mb-3">
                                                                <span class="text-base font-bold text-primary">
                                                                    <ft:currency value="${item.price}" />
                                                                </span>
                                                                <c:if test="${item.discountPercent > 0}">
                                                                    <span
                                                                        class="text-xs text-on-surface-variant/60 line-through">
                                                                        <ft:currency value="${item.originalPrice}" />
                                                                    </span>
                                                                </c:if>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </a>
                                                <div class="space-y-3 px-1 mt-3">
                                                    <div class="flex justify-between text-[10px] font-semibold">
                                                        <span class="text-primary-hover">Còn lại: ${item.stockRemaining}
                                                            ${item.unit}</span>
                                                        <span class="text-on-surface-variant/60">Đã bán
                                                            ${item.soldQuantity}</span>
                                                    </div>
                                                    <button type="button"
                                                        onclick="quickAddProduct(event, '${item.productId}')"
                                                        class="w-full bg-primary-light hover:bg-primary hover:text-white text-primary font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                        <span
                                                            class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                        Mua Ngay
                                                    </button>
                                                </div>
                                            </article>
                                        </c:forEach>

                                        <!-- View All Card -->
                                        <div
                                            class="w-[220px] shrink-0 bg-emerald-50/50 border border-primary/20 rounded-2xl p-6 flex flex-col items-center justify-center text-center group hover:bg-primary transition-all duration-300 snap-start cursor-pointer">
                                            <a href="${pageContext.request.contextPath}/products"
                                                class="flex flex-col items-center justify-center gap-3 w-full h-full text-primary group-hover:text-white"
                                                style="text-decoration: none;">
                                                <span
                                                    class="material-symbols-outlined text-[40px] font-bold">arrow_forward_ios</span>
                                                <span class="font-bold text-sm">Xem tất cả nhập khẩu</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <!-- CATEGORY PILLS FILTER SECTION -->
                        <section class="px-6 md:px-12 max-w-7xl mx-auto mb-10 relative z-10">
                            <div class="flex flex-col gap-3 pb-3 border-b border-primary/10">
                                <h2 class="text-lg font-bold text-on-surface flex items-center gap-1.5">
                                    <span class="material-symbols-outlined text-primary text-[22px]">category</span>
                                    Khám phá Danh mục Đặc sản Nông sản
                                </h2>

                                <div class="flex items-center gap-2.5 overflow-x-auto pb-2.5 hide-scrollbar">
                                    <!-- All category option -->
                                    <a href="${pageContext.request.contextPath}/home?keyword=${keyword}" class="category-pill px-5 py-2 rounded-full text-xs font-semibold whitespace-nowrap shadow-sm border
                                transition-all hover:scale-105 duration-200 ${empty selectedCategoryId ? 'bg-primary
                                text-white border-primary shadow-emerald-950/10' : 'bg-white border-white/60
                                text-on-surface-variant hover:bg-emerald-50'}" onclick="filterCategoryAjax(event, null)">
                                        Tất cả sản phẩm
                                    </a>

                                    <!-- Database Driven Categories -->
                                    <c:forEach var="cat" items="${categories}">
                                        <a href="${pageContext.request.contextPath}/home?categoryId=${cat.categoryId}&keyword=${keyword}"
                                            class="category-pill px-5 py-2 rounded-full text-xs font-semibold whitespace-nowrap shadow-sm
                                    border
                                    transition-all hover:scale-105 duration-200 ${selectedCategoryId == cat.categoryId ?
                                    'bg-primary text-white border-primary shadow-emerald-950/10' : 'bg-white
                                    border-white/60
                                    text-on-surface-variant hover:bg-emerald-50'}" onclick="filterCategoryAjax(event, '${cat.categoryId}')">
                                            <c:out value="${cat.name}" />
                                        </a>
                                    </c:forEach>
                                </div>
                            </div>
                        </section>

                        <!-- SEASONAL HARVEST CATALOG GRID -->
                        <section id="catalog-grid" class="px-6 md:px-12 max-w-7xl mx-auto pb-32 relative z-10">

                            <!-- Section Header -->
                            <div class="flex justify-between items-center mb-8">
                                <div>
                                    <h2 class="text-xl md:text-2xl font-bold text-on-surface"> THU HOẠCH CHÍNH VỤ -
                                        SẢN
                                        PHẨM
                                        MỚI</h2>
                                    <p class="text-xs text-on-surface-variant font-light mt-0.5">Nông sản sạch chín tự
                                        nhiên
                                        vừa
                                        được vận chuyển về kho đóng gói.</p>
                                </div>

                                <c:if test="${not empty keyword or not empty selectedCategoryId}">
                                    <a href="${pageContext.request.contextPath}/home"
                                        class="text-xs font-bold text-primary flex items-center gap-1 hover:underline">
                                        <span class="material-symbols-outlined text-[16px]">refresh</span>
                                        Xóa bộ lọc
                                    </a>
                                </c:if>
                            </div>

                             <div id="catalog-products-container" class="w-full" data-selected-category="${selectedCategoryId}" data-keyword="${fn:escapeXml(keyword)}">
                            <!-- Empty Products Fallback State -->
                            <c:if test="${empty normalProducts}">
                                <div
                                    class="glass-panel rounded-3xl p-16 text-center max-w-2xl mx-auto ambient-shadow flex flex-col items-center gap-4">
                                    <span
                                        class="material-symbols-outlined text-[64px] text-primary/40 animate-pulse">eco</span>
                                    <div>
                                        <h3 class="font-bold text-lg text-on-surface">Không tìm thấy sản phẩm phù hợp
                                        </h3>
                                        <p class="text-xs text-on-surface-variant font-light mt-1">Xin lỗi, hệ thống
                                            không
                                            tìm
                                            thấy trái cây khớp với bộ lọc tìm kiếm của bạn. Hãy thử đổi từ khóa khác
                                            nhé!
                                        </p>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/home"
                                        class="btn btn-primary btn-sm px-6 py-2.5 rounded-full mt-2">
                                        Quay lại Trang chủ
                                    </a>
                                </div>
                            </c:if>

                            <!-- Standard Products Grid layout -->
                            <c:if test="${not empty normalProducts}">
                                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                                    <c:forEach var="item" items="${normalProducts}">
                                        <article data-product-id="${item.productId}"
                                            class="bg-white/70 glass-panel rounded-3xl p-3 ambient-shadow flex flex-col group hover:-translate-y-1.5 hover:shadow-lg hover:border-emerald-300/40 transition-all duration-300">

                                            <!-- Clickable Product Area -->
                                            <a href="${pageContext.request.contextPath}/products/detail?id=${item.productId}"
                                                class="block group/link" style="text-decoration: none; color: inherit;">
                                                <!-- High Resolution Image with Zoom Scale on Hover -->
                                                <div class="relative aspect-[4/3] rounded-2xl overflow-hidden mb-4 bg-emerald-50"
                                                    style="aspect-ratio: 4/3;">
                                                    <img src="${item.image}" alt="${item.name}"
                                                        onerror="handleImageError(this)"
                                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                    <!-- Floating added qty badge -->
                                                    <div class="cart-qty-badge absolute top-3 left-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden"
                                                        id="badge-prod-${item.productId}">
                                                        Đã thêm 0
                                                    </div>
                                                    <!-- Organic Badge Badge -->
                                                    <div
                                                        class="absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm">
                                                        Nông sản sạch
                                                    </div>
                                                </div>

                                                <!-- Card Body Elements -->
                                                <div class="px-1 mb-3">
                                                    <h3
                                                        class="font-bold text-sm text-on-surface line-clamp-1 mb-1 group-hover:text-primary transition-colors">
                                                        <c:out value="${item.name}" />
                                                    </h3>
                                                    <p
                                                        class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mb-2 h-8 leading-relaxed">
                                                        <c:out value="${item.description}" />
                                                    </p>

                                                    <!-- Ratings and Sold Volume metadata -->
                                                    <div class="flex justify-between items-center">
                                                        <div
                                                            class="flex items-center gap-1 text-amber-500 scale-90 -ml-1">
                                                            <ft:stars rating="${item.rating}" showValue="true" />
                                                        </div>
                                                        <span class="text-[10px] text-on-surface-variant font-medium">
                                                            Đã bán ${item.soldQuantity}
                                                        </span>
                                                    </div>
                                                </div>
                                            </a>

                                            <!-- Lower Action Block (Price & Add to Cart) -->
                                            <div
                                                class="flex justify-between items-center gap-3 pt-3 border-t border-gray-100 mt-auto px-1">
                                                <div class="flex flex-col">
                                                    <span class="text-base font-bold text-primary">
                                                        <ft:currency value="${item.price}" />
                                                    </span>
                                                    <span class="text-[10px] text-on-surface-variant font-light">
                                                        /
                                                        <c:out value="${item.unit}" />
                                                    </span>
                                                </div>

                                                <button type="button"
                                                    onclick="quickAddProduct(event, '${item.productId}')"
                                                    class="bg-primary hover:bg-primary-hover text-white p-2.5 rounded-xl flex items-center justify-center hover:scale-105 active:scale-95 transition-all shadow-sm cursor-pointer"
                                                    title="Thêm vào giỏ">
                                                    <span
                                                        class="material-symbols-outlined text-[20px]">add_shopping_cart</span>
                                                </button>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </div>

                                <!-- Beautiful Pagination Controls -->
                                <c:if test="${totalPages > 1}">
                                    <div class="flex justify-center items-center mt-12 gap-2">
                                        <!-- Prev Button -->
                                        <c:choose>
                                            <c:when test="${currentPage > 1}">
                                                <c:url var="prevUrl" value="/home">
                                                    <c:param name="page" value="${currentPage - 1}" />
                                                    <c:if test="${not empty keyword}">
                                                        <c:param name="keyword" value="${keyword}" />
                                                    </c:if>
                                                    <c:if test="${not empty selectedCategoryId}">
                                                        <c:param name="categoryId" value="${selectedCategoryId}" />
                                                    </c:if>
                                                </c:url>
                                                <a href="${prevUrl}"
                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                                    <span
                                                        class="material-symbols-outlined text-[20px]">chevron_left</span>
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span
                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                                    <span
                                                        class="material-symbols-outlined text-[20px]">chevron_left</span>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>

                                        <!-- Page Numbers with Ellipsis -->
                                        <c:forEach var="p" begin="1" end="${totalPages}">
                                            <c:choose>
                                                <%-- Display conditions: first page, last page, and neighbor pages --%>
                                                    <c:when
                                                        test="${p == 1 || p == totalPages || (p >= currentPage - 1 && p <= currentPage + 1)}">
                                                        <c:url var="pageUrl" value="/home">
                                                            <c:param name="page" value="${p}" />
                                                            <c:if test="${not empty keyword}">
                                                                <c:param name="keyword" value="${keyword}" />
                                                            </c:if>
                                                            <c:if test="${not empty selectedCategoryId}">
                                                                <c:param name="categoryId"
                                                                    value="${selectedCategoryId}" />
                                                            </c:if>
                                                        </c:url>
                                                        <c:choose>
                                                            <c:when test="${currentPage == p}">
                                                                <span
                                                                    class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white font-bold shadow-md shadow-primary/20">
                                                                    ${p}
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="${pageUrl}"
                                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-on-surface-variant font-medium hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                                                    ${p}
                                                                </a>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <%-- Ellipsis before active range --%>
                                                        <c:when test="${p == 2 && currentPage > 3}">
                                                            <button onclick="promptPageJump('${totalPages}')"
                                                                class="w-10 h-10 flex items-center justify-center text-on-surface-variant/50 font-bold hover:text-primary transition-colors cursor-pointer"
                                                                title="Nhảy đến trang...">...</button>
                                                        </c:when>
                                                        <%-- Ellipsis after active range --%>
                                                            <c:when
                                                                test="${p == totalPages - 1 && currentPage < totalPages - 2}">
                                                                <button onclick="promptPageJump('${totalPages}')"
                                                                    class="w-10 h-10 flex items-center justify-center text-on-surface-variant/50 font-bold hover:text-primary transition-colors cursor-pointer"
                                                                    title="Nhảy đến trang...">...</button>
                                                            </c:when>
                                            </c:choose>
                                        </c:forEach>

                                        <!-- Next Button -->
                                        <c:choose>
                                            <c:when test="${currentPage < totalPages}">
                                                <c:url var="nextUrl" value="/home">
                                                    <c:param name="page" value="${currentPage + 1}" />
                                                    <c:if test="${not empty keyword}">
                                                        <c:param name="keyword" value="${keyword}" />
                                                    </c:if>
                                                    <c:if test="${not empty selectedCategoryId}">
                                                        <c:param name="categoryId" value="${selectedCategoryId}" />
                                                    </c:if>
                                                </c:url>
                                                <a href="${nextUrl}"
                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                                    <span
                                                        class="material-symbols-outlined text-[20px]">chevron_right</span>
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span
                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                                    <span
                                                        class="material-symbols-outlined text-[20px]">chevron_right</span>
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>

                                <!-- View All Products Button -->
                                <div class="flex justify-center mt-8">
                                    <a href="${pageContext.request.contextPath}/products"
                                        class="inline-flex items-center gap-2 bg-gradient-to-r from-primary to-secondary hover:from-primary-hover hover:to-secondary text-white font-bold text-sm px-8 py-3.5 rounded-full transition-all duration-300 shadow-md hover:shadow-lg hover:-translate-y-0.5 active:scale-95 cursor-pointer"
                                        style="text-decoration: none;">
                                        <span class="material-symbols-outlined text-[18px]">grid_view</span>
                                        <span>Xem tất cả sản phẩm</span>
                                    </a>
                                </div>
                            </c:if>
                            </div>
                        </section>

                    </div><!-- end main wrapper -->

                    <!-- QUICK ADD MODAL -->
                    <div id="quick-add-modal"
                        class="hidden fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/40 backdrop-blur-sm opacity-0 transition-opacity duration-300"
                        onclick="if(event.target===this)closeQuickAddModal()">
                        <div
                            class="bg-white w-full sm:max-w-md rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl transform scale-95 transition-transform duration-300 max-h-[90vh] overflow-y-auto">
                            <!-- Modal Header -->
                            <div class="flex items-center justify-between mb-5">
                                <h2 class="font-bold text-base text-on-surface">Chọn phân loại &amp; số lượng</h2>
                                <button onclick="closeQuickAddModal()"
                                    class="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 transition-colors cursor-pointer">
                                    <span
                                        class="material-symbols-outlined text-[18px] text-on-surface-variant">close</span>
                                </button>
                            </div>

                            <!-- Product Info Row -->
                            <div class="flex items-center gap-4 mb-5 bg-emerald-50/60 rounded-2xl p-3">
                                <img id="qa-product-img" src="" alt=""
                                    class="w-20 h-20 rounded-xl object-cover shrink-0 border border-emerald-100/50"
                                    onerror="handleImageError(this)">
                                <div class="flex-grow min-w-0">
                                    <h3 id="qa-product-name" class="font-bold text-sm text-on-surface line-clamp-2">Tên
                                        sản
                                        phẩm</h3>
                                    <p id="qa-product-desc"
                                        class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mt-1"></p>
                                </div>
                            </div>

                            <!-- Variant Options -->
                            <div class="mb-5">
                                <label class="block text-xs font-bold text-on-surface-variant mb-2">Chọn phân
                                    loại:</label>
                                <div id="qa-variants-container" class="flex flex-wrap gap-2">
                                    <!-- Variant chips rendered dynamically via JS -->
                                </div>
                            </div>

                            <!-- Price and Quantity Adjustment -->
                            <div class="flex items-center justify-between border-t border-gray-100 pt-4 mb-5">
                                <div>
                                    <span
                                        class="block text-[10px] text-on-surface-variant/60 font-semibold uppercase tracking-wider">Đơn
                                        giá:</span>
                                    <span id="qa-product-price" class="text-base font-bold text-primary">0đ</span>
                                </div>
                                <div class="flex items-center gap-3">
                                    <button type="button" onclick="adjustQuickAddQty(-1)"
                                        class="w-8 h-8 rounded-lg bg-gray-100 text-on-surface hover:bg-gray-200 active:scale-95 transition-all flex items-center justify-center font-bold cursor-pointer">-</button>
                                    <span id="qa-qty-value" class="w-6 text-center font-bold text-sm">1</span>
                                    <button type="button" onclick="adjustQuickAddQty(1)"
                                        class="w-8 h-8 rounded-lg bg-gray-100 text-on-surface hover:bg-gray-200 active:scale-95 transition-all flex items-center justify-center font-bold cursor-pointer">+</button>
                                </div>
                            </div>

                            <!-- Total Price & Call to Action -->
                            <div class="flex items-center justify-between border-t border-gray-100 pt-4 mb-5">
                                <div>
                                    <span
                                        class="block text-[10px] text-on-surface-variant/60 font-semibold uppercase tracking-wider">Tạm
                                        tính:</span>
                                    <span id="qa-total-price" class="text-lg font-bold text-red-600">0đ</span>
                                </div>
                            </div>

                            <!-- Submit Button -->
                            <button type="button" onclick="confirmQuickAdd()"
                                class="w-full bg-primary hover:bg-primary-hover text-white font-bold py-3 rounded-2xl flex items-center justify-center gap-2 transition-all active:scale-95 shadow-md cursor-pointer mb-2">
                                <span class="material-symbols-outlined text-[18px]">shopping_cart_checkout</span>
                                Xác nhận thêm vào giỏ hàng
                            </button>
                        </div>
                    </div>

                    <!-- Countdown Timer JavaScript for Flash Sale & AJAX Cart Operations -->

                    <script>
                        // ============================================================
                        // HERO INTERACTIVE SLIDESHOW LOGIC
                        // ============================================================
                        let currentSlide = 0;
                        const totalSlides = 4;
                        let slideInterval;

                        function showSlide(index) {
                            currentSlide = index;
                            document.querySelectorAll('.slide-item').forEach((slide, idx) => {
                                if (idx === index) {
                                    slide.classList.remove('opacity-0');
                                    slide.classList.add('opacity-100');
                                } else {
                                    slide.classList.remove('opacity-100');
                                    slide.classList.add('opacity-0');
                                }
                            });
                            // Update dots
                            for (let i = 0; i < totalSlides; i++) {
                                const dot = document.getElementById(`dot-${i}`);
                                if (dot) {
                                    if (i === index) {
                                        dot.classList.remove('bg-white/40');
                                        dot.classList.add('bg-white/80', 'w-4');
                                    } else {
                                        dot.classList.remove('bg-white/80', 'w-4');
                                        dot.classList.add('bg-white/40');
                                    }
                                }
                            }
                        }

                        function setSlide(index) {
                            showSlide(index);
                            resetSlideTimer();
                        }

                        function nextSlide() {
                            const next = (currentSlide + 1) % totalSlides;
                            showSlide(next);
                        }

                        function resetSlideTimer() {
                            clearInterval(slideInterval);
                            slideInterval = setInterval(nextSlide, 4000);
                        }

                        /**
                         * Set high-end Flash Sale timer target.
                         */
                        function startFlashSaleTimer() {
                            const now = new Date();
                            const threeHours = 3 * 60 * 60 * 1000;
                            const msPassedSinceMidnight = now.getTime() - new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
                            const intervalsPassed = Math.floor(msPassedSinceMidnight / threeHours);
                            const nextIntervalTarget = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime() + (intervalsPassed + 1) * threeHours;

                            const hourBox = document.getElementById('hourBox');
                            const minuteBox = document.getElementById('minuteBox');
                            const secondBox = document.getElementById('secondBox');

                            function updateClock() {
                                const timeDiff = nextIntervalTarget - new Date().getTime();
                                if (timeDiff <= 0) {
                                    clearInterval(clockInterval);
                                    startFlashSaleTimer();
                                    return;
                                }
                                const hours = Math.floor(timeDiff / (1000 * 60 * 60));
                                const minutes = Math.floor((timeDiff % (1000 * 60 * 60)) / (1000 * 60));
                                const seconds = Math.floor((timeDiff % (1000 * 60)) / 1000);

                                if (hourBox) hourBox.textContent = String(hours).padStart(2, '0');
                                if (minuteBox) minuteBox.textContent = String(minutes).padStart(2, '0');
                                if (secondBox) secondBox.textContent = String(seconds).padStart(2, '0');
                            }
                            updateClock();
                            const clockInterval = setInterval(updateClock, 1000);
                        }

                        function applyAiPrompt(prompt) {
                            const searchInput = document.getElementById('searchInput');
                            if (searchInput) {
                                searchInput.value = prompt;
                                const wrapper = searchInput.closest('.glass-panel');
                                if (wrapper) {
                                    wrapper.classList.add('ring-4', 'ring-primary/40');
                                    setTimeout(() => {
                                        wrapper.classList.remove('ring-4', 'ring-primary/40');
                                        searchInput.closest('form').submit();
                                    }, 300);
                                }
                            }
                        }

                        function scrollFlashSale(direction) {
                            const container = document.getElementById('flashSaleContainer');
                            if (container) {
                                const cardWidth = container.firstElementChild ? container.firstElementChild.offsetWidth + 24 : 300;
                                container.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
                            }
                        }

                        // ============================================================
                        // PREMIUM AJAX QUICK ADD CART OPERATIONS
                        // ============================================================
                        window.currentQuickAddData = null;

                        /**
                         * Intercept and trigger dynamic quick-add flow.
                         */
                        async function quickAddProduct(event, productId) {
                            if (event) {
                                event.preventDefault();
                                event.stopPropagation();
                            }

                            try {
                                console.log(`[FruitMkt] Loading product \${productId} details for Quick Add...`);
                                const contextPath = window.contextPath || '';

                                // Fetch product and variants as JSON
                                const response = await fetch(`\${contextPath}/products/detail?id=\${productId}&format=json`, {
                                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                                });
                                const contentType = response.headers.get("content-type");
                                if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
                                    if (contentType && contentType.indexOf("application/json") !== -1) {
                                        const errData = await response.json();
                                        throw new Error(errData.error || 'Lỗi hệ thống (Mã: ' + response.status + ')');
                                    }
                                    throw new Error('Lỗi hệ thống (Mã: ' + response.status + ')');
                                }
                                const envelope = await response.json();
                                if (envelope.success === false) {
                                    alert(envelope.error || 'Sản phẩm hiện không khả dụng.');
                                    return;
                                }
                                const data = envelope.data;
                                if (!data) {
                                    alert('Sản phẩm hiện không khả dụng.');
                                    return;
                                }

                                // ── Kiểm tra trạng thái từ server ──
                                if (data.success === false) {
                                    const reason = data.reason || '';
                                    const msg = data.message || 'Sản phẩm hiện không khả dụng.';
                                    if (reason === 'OUT_OF_SEASON') {
                                        if (confirm(msg + '\n\nBạn có muốn gửi yêu cầu nhập kho vụ mới tới cửa hàng không?')) {
                                            window.location.href = `${contextPath}/products/detail?id=${productId}`;
                                        }
                                    } else if (reason === 'OUT_OF_STOCK') {
                                        alert('Sản phẩm đã hết hàng. Vui lòng quay lại sau.');
                                    } else {
                                        alert(msg);
                                    }
                                    return;
                                }
                                if (!data.variants || data.variants.length === 0) {
                                    alert('Sản phẩm đã hết hàng. Vui lòng quay lại sau.');
                                    return;
                                }
                                // Cảnh báo nhẹ khi sản phẩm ngoài mùa vụ nhưng vẫn còn hàng
                                if (data.product && data.product.isOutOfSeason) {
                                    console.info('[FruitMkt] Sản phẩm ngoài mùa vụ nhưng vẫn còn hàng, cho phép mua.');
                                }

                                window.currentQuickAddData = {
                                    product: data.product,
                                    variants: data.variants,
                                    selectedVariant: data.variants[0],
                                    quantity: 1
                                };

                                // IF PRODUCT HAS ONLY 1 VARIANT -> Directly add to cart! 0-click addition
                                if (data.variants.length === 1) {
                                    const v = data.variants[0];
                                    const imagePath = data.product.imagePath || 'assets/img/placeholder.png';
                                    await window.addCartItem(v.variantId, 1, `\${data.product.name} - \${v.variantLabel}`, v.price, imagePath, v.stockQuantity, data.product.productId);
                                    return;
                                }

                                // IF PRODUCT HAS MULTIPLE VARIANTS -> Show the quick variant selection modal!
                                openQuickAddModal(data.product, data.variants);
                            } catch (err) {
                                console.error('Lỗi tải thông tin sản phẩm:', err);
                                alert('Không thể kết nối đến máy chủ.');
                            }
                        }

                        function openQuickAddModal(product, variants) {
                            const modal = document.getElementById('quick-add-modal');
                            if (!modal) return;

                            // Image resolving
                            let imgUrl = product.imagePath || 'assets/img/placeholder.png';
                            if (imgUrl && !imgUrl.startsWith('http://') && !imgUrl.startsWith('https://')) {
                                if (!imgUrl.startsWith('/')) imgUrl = '/' + imgUrl;
                                imgUrl = (window.contextPath || '') + imgUrl;
                            }

                            document.getElementById('qa-product-img').src = imgUrl;
                            document.getElementById('qa-product-img').alt = product.name;
                            document.getElementById('qa-product-name').textContent = product.name;
                            document.getElementById('qa-product-desc').textContent = product.description || 'Nông sản sạch chín tự nhiên tốt cho sức khỏe.';

                            // Render variants chips
                            const container = document.getElementById('qa-variants-container');
                            container.innerHTML = '';
                            variants.forEach((v, idx) => {
                                const btn = document.createElement('button');
                                btn.type = 'button';
                                btn.className = `px-4 py-2 border rounded-full text-xs font-semibold transition-all cursor-pointer \${idx === 0
                                ? 'border-primary bg-emerald-50 text-primary font-bold shadow-sm ring-2 ring-primary/10'
                                : 'border-gray-200 bg-white text-on-surface-variant hover:border-primary hover:bg-emerald-50/30'
                                }`;
                                btn.textContent = v.variantLabel;
                                btn.onclick = () => selectQuickAddVariant(v.variantId, btn);
                                btn.setAttribute('data-variant-id', v.variantId);
                                container.appendChild(btn);
                            });

                            // Set default variant values
                            const v = variants[0];
                            window.currentQuickAddData.selectedVariant = v;
                            window.currentQuickAddData.quantity = 1;

                            updateQuickAddValues();

                            // Animate open modal
                            modal.classList.remove('hidden');
                            setTimeout(() => {
                                modal.classList.remove('opacity-0');
                                modal.firstElementChild.classList.remove('scale-95');
                            }, 30);
                        }

                        function closeQuickAddModal() {
                            const modal = document.getElementById('quick-add-modal');
                            if (!modal) return;

                            modal.classList.add('opacity-0');
                            modal.firstElementChild.classList.add('scale-95');
                            setTimeout(() => {
                                modal.classList.add('hidden');
                                window.currentQuickAddData = null;
                            }, 300);
                        }

                        function selectQuickAddVariant(variantId, clickedBtn) {
                            if (!window.currentQuickAddData) return;
                            const v = window.currentQuickAddData.variants.find(i => i.variantId === variantId);
                            if (!v) return;

                            window.currentQuickAddData.selectedVariant = v;
                            window.currentQuickAddData.quantity = 1; // Reset quantity to 1 on variant change

                            // Update active chip style
                            const container = document.getElementById('qa-variants-container');
                            container.querySelectorAll('button').forEach(btn => {
                                btn.className = 'px-4 py-2 border rounded-full text-xs font-semibold transition-all cursor-pointer border-gray-200 bg-white text-on-surface-variant hover:border-primary hover:bg-emerald-50/30';
                            });
                            clickedBtn.className = 'px-4 py-2 border rounded-full text-xs font-semibold transition-all cursor-pointer border-primary bg-emerald-50 text-primary font-bold shadow-sm ring-2 ring-primary/10';

                            updateQuickAddValues();
                        }

                        function adjustQuickAddQty(delta) {
                            if (!window.currentQuickAddData) return;
                            const v = window.currentQuickAddData.selectedVariant;
                            let qty = window.currentQuickAddData.quantity + delta;

                            if (qty < 1) qty = 1;
                            if (qty > v.stockQuantity) {
                                qty = v.stockQuantity;
                                alert(`Kho chỉ còn \${v.stockQuantity} sản phẩm khả dụng cho phân loại này!`);
                            }

                            window.currentQuickAddData.quantity = qty;
                            updateQuickAddValues();
                        }

                        function updateQuickAddValues() {
                            if (!window.currentQuickAddData) return;
                            const v = window.currentQuickAddData.selectedVariant;
                            const qty = window.currentQuickAddData.quantity;

                            // [FIX] v.price từ server JSON (fresh) — VND luôn là số nguyên
                            // Dùng Math.round để chuyển về int rồi nhân, tránh sai số float
                            const unitPrice = Math.round(Number(v.price));
                            const totalPrice = unitPrice * qty; // int * int = exact

                            const fmt = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
                            document.getElementById('qa-product-price').textContent = fmt.format(unitPrice);
                            document.getElementById('qa-qty-value').textContent = qty;
                            document.getElementById('qa-total-price').textContent = fmt.format(totalPrice);
                        }


                        async function confirmQuickAdd() {
                            if (!window.currentQuickAddData) return;
                            const v = window.currentQuickAddData.selectedVariant;
                            const qty = window.currentQuickAddData.quantity;
                            const p = window.currentQuickAddData.product;

                            const imagePath = p.imagePath || 'assets/img/placeholder.png';
                            await window.addCartItem(v.variantId, qty, `\${p.name} - \${v.variantLabel}`, v.price, imagePath, v.stockQuantity, p.productId);
                            closeQuickAddModal();
                        }

                        // ============================================================
                        // DYNAMIC CARD QUANTITY DISPLAY LOGIC
                        // ============================================================
                        /**
                         * Scan Local Storage and update floating green quantity badges on all product cards.
                         */
                        window.updateCardAddedQuantities = function () {
                            const isLoggedIn = window.isLoggedIn === true;
                            const key = isLoggedIn ? 'userCart' : 'guestCart';

                            let items = [];
                            try {
                                items = JSON.parse(localStorage.getItem(key)) || [];
                            } catch (e) {
                                items = [];
                            }

                            // 1. Group quantities added by variantId and productId
                            const variantQtys = {};
                            items.forEach(i => {
                                variantQtys[i.variantId] = (variantQtys[i.variantId] || 0) + (parseInt(i.quantity) || 0);
                            });

                            // Let's query all cards in grid
                            const cards = document.querySelectorAll('article[data-product-id]');

                            cards.forEach(card => {
                                const prodId = parseInt(card.getAttribute('data-product-id'));
                                if (!prodId) return;

                                let productQty = 0;
                                items.forEach(item => {
                                    const cardName = card.querySelector('h3')?.textContent?.trim() || "";

                                    const itemProdName = item.productName || item.name || "";
                                    if (itemProdName.toLowerCase().includes(cardName.toLowerCase()) || cardName.toLowerCase().includes(itemProdName.split(' - ')[0].toLowerCase())) {
                                        productQty += (parseInt(item.quantity) || 0);
                                    }
                                });

                                // Update badge
                                const badge = document.getElementById(`badge-prod-\${prodId}`);
                                if (badge) {
                                    if (productQty > 0) {
                                        badge.textContent = `Đã thêm \${productQty}`;
                                        badge.classList.remove('hidden');
                                    } else {
                                        badge.classList.add('hidden');
                                    }
                                }
                            });
                        };

                        // ============================================================
                        // UNIFIED ADD CART AJAX METHOD (COMMONS)
                        // ============================================================
                        window.addCartItem = async function (variantId, quantity, name, price, imagePath, stockQuantity, productId) {
                            const isLoggedIn = window.isLoggedIn === true;
                            const contextPath = window.contextPath || '';

                            // 1. Optimistic Update of Local Storage & Badge
                            let rollbackItems = null;
                            try {
                                if (isLoggedIn) {
                                    let items = JSON.parse(localStorage.getItem('userCart')) || [];
                                    rollbackItems = JSON.stringify(items);
                                    const idx = items.findIndex(i => i.variantId === parseInt(variantId));
                                    if (idx >= 0) {
                                        items[idx].quantity += parseInt(quantity);
                                    } else {
                                        items.push({
                                            cartItemId: -1,
                                            variantId: parseInt(variantId),
                                            productName: name.split(' - ')[0],
                                            variantLabel: name.split(' - ')[1] || 'Mặc định',
                                            price: parseFloat(price),
                                            weightKg: 1.0,
                                            quantity: parseInt(quantity),
                                            imagePath: imagePath || 'assets/img/placeholder.png',
                                            stockQuantity: parseInt(stockQuantity) || 99,
                                            productId: parseInt(productId) || null
                                        });
                                    }
                                    localStorage.setItem('userCart', JSON.stringify(items));
                                } else {
                                    let items = JSON.parse(localStorage.getItem('guestCart')) || [];
                                    rollbackItems = JSON.stringify(items);
                                    const idx = items.findIndex(i => i.variantId === parseInt(variantId));
                                    if (idx >= 0) {
                                        items[idx].quantity += parseInt(quantity);
                                    } else {
                                        items.push({
                                            variantId: parseInt(variantId),
                                            name: name,
                                            price: parseFloat(price),
                                            quantity: parseInt(quantity),
                                            imagePath: imagePath || 'assets/img/placeholder.png',
                                            stockQuantity: parseInt(stockQuantity) || 99,
                                            productId: parseInt(productId) || null
                                        });
                                    }
                                    localStorage.setItem('guestCart', JSON.stringify(items));
                                }

                                // Update badge and card quantities immediately
                                if (typeof GuestCart !== 'undefined') {
                                    GuestCart.updateBadge();
                                } else {
                                    const badge = document.getElementById('cart-badge');
                                    if (badge) {
                                        const key = isLoggedIn ? 'userCart' : 'guestCart';
                                        const items = JSON.parse(localStorage.getItem(key)) || [];
                                        badge.textContent = items.reduce((sum, i) => sum + i.quantity, 0);
                                    }
                                }
                                if (window.updateCardAddedQuantities) {
                                    window.updateCardAddedQuantities();
                                }

                                // Show toast immediately
                                showCartSuccessToast(name, quantity);

                            } catch (e) {
                                console.warn('[Cart] Error performing optimistic UI update:', e);
                            }

                            // 2. Perform Backend Request Asynchronously
                            fetch(`\${contextPath}/cart?action=add`, {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                    'X-Requested-With': 'XMLHttpRequest'
                                },
                                body: `variantId=\${variantId}&quantity=\${quantity}&_csrf=\${window.csrfToken || ''}`
                            })
                                .then(response => {
                                    const contentType = response.headers.get("content-type");
                                    if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
                                        if (contentType && contentType.indexOf("application/json") !== -1) {
                                            return response.json().then(errData => {
                                                throw new Error(errData.error || `Lỗi hệ thống (Mã: \${response.status})`);
                                            });
                                        }
                                        throw new Error(`Lỗi hệ thống (Mã: \${response.status})`);
                                    }
                                    return response.json();
                                })
                                .then(data => {
                                    if (!data.success) {
                                        throw new Error(data.error || 'Có lỗi xảy ra khi thêm vào giỏ hàng.');
                                    }

                                    // Sync with final DB mapping
                                    if (isLoggedIn && data.cartSummary && data.cartSummary.items) {
                                        const mappedItems = data.cartSummary.items.map(item => ({
                                            cartItemId: item.cartItemId,
                                            variantId: item.variantId,
                                            productName: item.productName,
                                            variantLabel: item.variantLabel,
                                            price: item.price,
                                            weightKg: item.weightKg || 1.0,
                                            quantity: item.quantity,
                                            imagePath: item.imagePath,
                                            stockQuantity: item.stockQuantity,
                                            productId: item.productId
                                        }));
                                        localStorage.setItem('userCart', JSON.stringify(mappedItems));
                                        if (window.updateCardAddedQuantities) {
                                            window.updateCardAddedQuantities();
                                        }
                                    }
                                })
                                .catch(err => {
                                    console.error('Error syncing add cart with server:', err);
                                    // Rollback local storage on error
                                    if (rollbackItems) {
                                        if (isLoggedIn) {
                                            localStorage.setItem('userCart', rollbackItems);
                                        } else {
                                            localStorage.setItem('guestCart', rollbackItems);
                                        }
                                        if (typeof GuestCart !== 'undefined') {
                                            GuestCart.updateBadge();
                                        } else {
                                            const badge = document.getElementById('cart-badge');
                                            if (badge) {
                                                const key = isLoggedIn ? 'userCart' : 'guestCart';
                                                const items = JSON.parse(localStorage.getItem(key)) || [];
                                                badge.textContent = items.reduce((sum, i) => sum + i.quantity, 0);
                                            }
                                        }
                                        if (window.updateCardAddedQuantities) {
                                            window.updateCardAddedQuantities();
                                        }
                                    }
                                    alert(err.message || 'Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.');
                                });
                        };

                        function showCartSuccessToast(productName, quantity) {
                            let toast = document.getElementById('cart-added-toast');
                            if (!toast) {
                                const toastHtml = `
                <div id="cart-added-toast" class="premium-toast font-sans">
                    <span class="premium-toast-icon"><i class="fa-solid fa-circle-check"></i></span>
                    <div>
                        <strong style="display: block; font-weight: 700;">Thành công!</strong>
                        <span class="text-xs" id="toast-message">Đã thêm sản phẩm vào giỏ hàng.</span>
                    </div>
                </div>
            `;
                                document.body.insertAdjacentHTML('beforeend', toastHtml);
                                toast = document.getElementById('cart-added-toast');
                            }

                            const msgEl = document.getElementById('toast-message');
                            if (msgEl) {
                                msgEl.innerHTML = `Đã thêm <strong>\${quantity}</strong> x <strong>\${productName}</strong> vào giỏ hàng.`;
                            }

                            toast.classList.add('show');
                            setTimeout(() => toast.classList.remove('show'), 3500);
                        }

                        function promptPageJump(totalPages) {
                            if (typeof Swal !== 'undefined') {
                                Swal.fire({
                                    title: 'Nhảy đến trang',
                                    text: 'Nhập số trang muốn đến (1 - ' + totalPages + '):',
                                    input: 'number',
                                    inputAttributes: {
                                        min: 1,
                                        max: totalPages,
                                        step: 1
                                    },
                                    inputValue: 1,
                                    showCancelButton: true,
                                    confirmButtonText: 'Đồng ý',
                                    cancelButtonText: 'Hủy',
                                    confirmButtonColor: '#4d661c',
                                    cancelButtonColor: '#6b7280',
                                    background: '#ffffff',
                                    inputValidator: (value) => {
                                        if (!value) {
                                            return 'Vui lòng nhập số trang!';
                                        }
                                        const page = parseInt(value);
                                        if (isNaN(page) || page < 1 || page > totalPages) {
                                            return 'Số trang phải từ 1 đến ' + totalPages + '!';
                                        }
                                    }
                                }).then((result) => {
                                    if (result.isConfirmed && result.value) {
                                        const targetPage = parseInt(result.value);
                                        const urlParams = new URLSearchParams(window.location.search);
                                        urlParams.set('page', targetPage);
                                        window.location.search = urlParams.toString();
                                    }
                                });
                            } else {
                                const targetPageStr = prompt("Nhập số trang bạn muốn chuyển đến (1 - " + totalPages + "):");
                                if (targetPageStr !== null) {
                                    const targetPage = parseInt(targetPageStr.trim());
                                    if (!isNaN(targetPage) && targetPage >= 1 && targetPage <= totalPages) {
                                        const urlParams = new URLSearchParams(window.location.search);
                                        urlParams.set('page', targetPage);
                                        window.location.search = urlParams.toString();
                                    } else {
                                        alert("Số trang không hợp lệ!");
                                    }
                                }
                            }
                        }

                        function scrollBestSellers(direction) {
                            const container = document.getElementById('bestSellersContainer');
                            if (container) {
                                const cardWidth = container.firstElementChild ? container.firstElementChild.offsetWidth + 24 : 300;
                                container.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
                            }
                        }

                        function scrollSeasonal(direction) {
                            const container = document.getElementById('seasonalContainer');
                            if (container) {
                                const cardWidth = container.firstElementChild ? container.firstElementChild.offsetWidth + 24 : 300;
                                container.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
                            }
                        }

                        function scrollOrganic(direction) {
                            const container = document.getElementById('organicContainer');
                            if (container) {
                                const cardWidth = container.firstElementChild ? container.firstElementChild.offsetWidth + 24 : 300;
                                container.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
                            }
                        }

                        function scrollImported(direction) {
                            const container = document.getElementById('importedContainer');
                            if (container) {
                                const cardWidth = container.firstElementChild ? container.firstElementChild.offsetWidth + 24 : 300;
                                container.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
                            }
                        }

                        // Launch initializers
                        const catalogProdContainer = document.getElementById('catalog-products-container');
                        let currentCategoryId = catalogProdContainer && catalogProdContainer.getAttribute('data-selected-category') ? catalogProdContainer.getAttribute('data-selected-category') : null;
                        let currentKeyword = catalogProdContainer && catalogProdContainer.getAttribute('data-keyword') ? catalogProdContainer.getAttribute('data-keyword') : '';

                        function handleHeroSearch(event) {
                            if (event) event.preventDefault();
                            const keywordInput = document.getElementById('searchInput');
                            currentKeyword = keywordInput ? keywordInput.value.trim() : '';
                            searchProductsAjax(currentKeyword, currentCategoryId, 1);
                        }

                        function filterCategoryAjax(event, categoryId) {
                            if (event) event.preventDefault();
                            currentCategoryId = categoryId;
                            
                            // Update active style of pills
                            document.querySelectorAll('.category-pill').forEach(el => {
                                el.classList.remove('bg-primary', 'text-white', 'border-primary', 'shadow-emerald-950/10');
                                el.classList.add('bg-white', 'border-white/60', 'text-on-surface-variant', 'hover:bg-emerald-50');
                            });
                            
                            const activeEl = event.currentTarget;
                            activeEl.classList.remove('bg-white', 'border-white/60', 'text-on-surface-variant', 'hover:bg-emerald-50');
                            activeEl.classList.add('bg-primary', 'text-white', 'border-primary', 'shadow-emerald-950/10');
                            
                            searchProductsAjax(currentKeyword, currentCategoryId, 1);
                        }

                        async function searchProductsAjax(keyword, categoryId, page) {
                            const container = document.getElementById('catalog-products-container');
                            if (!container) return;
                            
                            // Add opacity and disable interactions
                            container.style.opacity = '0.5';
                            container.style.pointerEvents = 'none';
                            
                            try {
                                let url = '${pageContext.request.contextPath}/home?format=json&page=' + page;
                                if (keyword) url += '&keyword=' + encodeURIComponent(keyword);
                                if (categoryId) url += '&categoryId=' + categoryId;
                                
                                const response = await fetch(url);
                                const result = await response.json();
                                
                                container.style.opacity = '1';
                                container.style.pointerEvents = 'auto';
                                
                                if (result.success && result.data) {
                                    renderCatalogProducts(result.data);
                                    // Smooth scroll to catalog-grid
                                    const catalogGrid = document.getElementById('catalog-grid');
                                    if (catalogGrid) {
                                        catalogGrid.scrollIntoView({ behavior: 'smooth' });
                                    }
                                    if (window.updateCardAddedQuantities) {
                                        window.updateCardAddedQuantities();
                                    }
                                }
                            } catch (err) {
                                console.error("Lỗi AJAX search:", err);
                                container.style.opacity = '1';
                                container.style.pointerEvents = 'auto';
                            }
                        }

                        function renderCatalogProducts(data) {
                            const container = document.getElementById('catalog-products-container');
                            if (!container) return;
                            
                            const products = data.normalProducts || [];
                            const currentPage = data.currentPage || 1;
                            const totalPages = data.totalPages || 1;
                            const ctx = '${pageContext.request.contextPath}';
                            
                            if (products.length === 0) {
                                container.innerHTML = `
                                    <div class="glass-panel rounded-3xl p-16 text-center max-w-2xl mx-auto ambient-shadow flex flex-col items-center gap-4">
                                        <span class="material-symbols-outlined text-[64px] text-primary/40 animate-pulse">eco</span>
                                        <div>
                                            <h3 class="font-bold text-lg text-on-surface">Không tìm thấy sản phẩm phù hợp</h3>
                                            <p class="text-xs text-on-surface-variant font-light mt-1">Xin lỗi, hệ thống không tìm thấy trái cây khớp với bộ lọc tìm kiếm của bạn. Hãy thử đổi từ khóa khác nhé!</p>
                                        </div>
                                        <a href="${ctx}/home" class="btn btn-primary btn-sm px-6 py-2.5 rounded-full mt-2">Quay lại Trang chủ</a>
                                    </div>
                                `;
                                return;
                            }
                            
                            let html = '<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">';
                            
                            products.forEach(item => {
                                let starsHtml = '';
                                const ratingVal = parseFloat(item.rating) || 0;
                                const ratingLabel = ratingVal > 0 ? ratingVal.toFixed(1) : 'Chưa có đánh giá';
                                const ratingLabelClass = ratingVal > 0 ? 'text-xs text-on-surface-variant ml-1 font-semibold' : 'text-xs text-gray-400 ml-1 font-semibold';
                                const fullStars = Math.floor(ratingVal);
                                const halfStar = (ratingVal - fullStars) >= 0.5 ? 1 : 0;
                                const emptyStars = 5 - fullStars - halfStar;
                                
                                for (let i = 0; i < fullStars; i++) starsHtml += '<i class="fa-solid fa-star text-amber-500 mr-0.5"></i>';
                                if (halfStar) starsHtml += '<i class="fa-solid fa-star-half-stroke text-amber-500 mr-0.5"></i>';
                                for (let i = 0; i < emptyStars; i++) starsHtml += '<i class="fa-regular fa-star text-amber-500 mr-0.5"></i>';
                                
                                const formattedPrice = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.price);
                                
                                html += `
                                    <article data-product-id="${item.productId}"
                                        class="bg-white/70 glass-panel rounded-3xl p-3 ambient-shadow flex flex-col group hover:-translate-y-1.5 hover:shadow-lg hover:border-emerald-300/40 transition-all duration-300">
                                        <a href="${ctx}/products/detail?id=${item.productId}" class="block group/link" style="text-decoration: none; color: inherit;">
                                            <div class="relative aspect-[4/3] rounded-2xl overflow-hidden mb-4 bg-emerald-50" style="aspect-ratio: 4/3;">
                                                <img src="${item.image}" alt="${fn:escapeXml(item.name)}" onerror="handleImageError(this)" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                                <div class="cart-qty-badge absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm hidden" id="badge-prod-${item.productId}">Đã thêm 0</div>
                                                <div class="absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm">Nông sản sạch</div>
                                            </div>
                                            <div class="px-1 mb-3">
                                                <h3 class="font-bold text-sm text-on-surface line-clamp-1 mb-1 group-hover:text-primary transition-colors">${fn:escapeXml(item.name)}</h3>
                                                <p class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mb-2 h-8 leading-relaxed">
                                                    <c:out value="${empty item.description ? '' : item.description}" />
                                                </p>
                                                <div class="flex justify-between items-center">
                                                    <div class="flex items-center gap-1 text-amber-500 scale-90 -ml-1">${starsHtml}<span class="${ratingLabelClass}">${ratingLabel}</span></div>
                                                    <span class="text-[10px] text-on-surface-variant font-medium">Đã bán ${empty item.soldQuantity ? 0 : item.soldQuantity}</span>
                                                </div>
                                            </div>
                                        </a>
                                        <div class="flex justify-between items-center gap-3 pt-3 border-t border-gray-100 mt-auto px-1">
                                            <div class="flex flex-col">
                                                <span class="text-base font-bold text-primary">${formattedPrice}</span>
                                                <span class="text-[10px] text-on-surface-variant font-light">/
                                                    <c:out value="${empty item.unit ? 'kg' : item.unit}" />
                                                </span>
                                            </div>
                                            <button type="button" onclick="quickAddProduct(event, '${item.productId}')" class="bg-primary hover:bg-primary-hover text-white p-2.5 rounded-xl flex items-center justify-center hover:scale-105 active:scale-95 transition-all shadow-sm cursor-pointer" title="Thêm vào giỏ">
                                                <span class="material-symbols-outlined text-[20px]">add_shopping_cart</span>
                                            </button>
                                        </div>
                                    </article>
                                `;
                            });
                            
                            html += '</div>';
                            
                            // Pagination
                            if (totalPages > 1) {
                                html += '<div class="flex justify-center items-center mt-12 gap-2">';
                                
                                // Prev
                                if (currentPage > 1) {
                                    html += `
                                        <button onclick="searchProductsAjax(currentKeyword, currentCategoryId, ${currentPage - 1})"
                                            class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                            <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                        </button>
                                    `;
                                } else {
                                    html += `
                                        <span class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                        </span>
                                    `;
                                }
                                
                                // Pages
                                for (let p = 1; p <= totalPages; p++) {
                                    if (p === 1 || p === totalPages || (p >= currentPage - 1 && p <= currentPage + 1)) {
                                        if (currentPage === p) {
                                            html += `<span class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white font-bold shadow-md shadow-primary/20">${p}</span>`;
                                        } else {
                                            html += `
                                                <button onclick="searchProductsAjax(currentKeyword, currentCategoryId, ${p})"
                                                    class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-on-surface-variant font-medium hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                                    ${p}
                                                </button>
                                            `;
                                        }
                                    } else if ((p === 2 && currentPage > 3) || (p === totalPages - 1 && currentPage < totalPages - 2)) {
                                        html += `<span class="w-10 h-10 flex items-center justify-center text-on-surface-variant/50 font-bold">...</span>`;
                                    }
                                }
                                
                                // Next
                                if (currentPage < totalPages) {
                                    html += `
                                        <button onclick="searchProductsAjax(currentKeyword, currentCategoryId, ${currentPage + 1})"
                                            class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                            <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                        </button>
                                    `;
                                } else {
                                    html += `
                                        <span class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                        </span>
                                    `;
                                }
                                
                                html += '</div>';
                            }
                            
                            // View All button
                            html += `
                                <div class="flex justify-center mt-8">
                                    <a href="${ctx}/products" class="inline-flex items-center gap-2 bg-gradient-to-r from-primary to-secondary hover:from-primary-hover hover:to-secondary text-white font-bold text-sm px-8 py-3.5 rounded-full transition-all duration-300 shadow-md hover:shadow-lg hover:-translate-y-0.5 active:scale-95 cursor-pointer" style="text-decoration: none;">
                                        <span class="material-symbols-outlined text-[18px]">grid_view</span>
                                        <span>Xem tất cả sản phẩm</span>
                                    </a>
                                </div>
                            `;
                            
                            container.innerHTML = html;
                        }

                        function escapeHtml(str) {
                            if (!str) return '';
                            return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
                        }

                        document.addEventListener('DOMContentLoaded', () => {
                            // Initialize Hero Slideshow
                            showSlide(0);
                            resetSlideTimer();

                            startFlashSaleTimer();
                            if (window.updateCardAddedQuantities) {
                                window.updateCardAddedQuantities();
                            }
                            document.querySelectorAll('.flash-sale-progress').forEach(el => {
                                const w = el.getAttribute('data-width');
                                if (w) {
                                    el.style.width = w + '%';
                                }
                            });

                            // Auto scroll to catalog grid if search query or category is applied
                            const urlParams = new URLSearchParams(window.location.search);
                            if (urlParams.has('keyword') || urlParams.has('categoryId')) {
                                const catalogGrid = document.getElementById('catalog-grid');
                                if (catalogGrid) {
                                    setTimeout(() => {
                                        catalogGrid.scrollIntoView({ behavior: 'smooth' });
                                    }, 300);
                                }
                            }
                        });
                    </script>

                    <!-- Load site footer -->
                    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
