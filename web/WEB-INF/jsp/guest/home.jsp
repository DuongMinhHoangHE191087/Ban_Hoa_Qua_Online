<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>

                <!-- Load header and inject Page Title -->
                <jsp:include page="/WEB-INF/jsp/common/header.jsp">
                    <jsp:param name="pageTitle" value="Trang chủ - MeteFruit" />
                </jsp:include>

                <!-- Google Fonts Lexend & Material Icons for rich premium look -->
                <link href="https://fonts.googleapis.com" rel="preconnect">
                <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
                <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap"
                    rel="stylesheet">
                <link
                    href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
                    rel="stylesheet">

                <!-- Isolated Tailwind CSS Engine for dynamic layout -->
                <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>

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

                    <!-- Hero Section with AI Search Container -->
                    <section
                        class="relative px-6 md:px-12 pt-28 pb-16 max-w-7xl mx-auto flex flex-col items-center justify-center text-center">
                        <!-- Floating organic abstract blobs -->
                        <div
                            class="absolute top-10 left-10 w-72 h-72 bg-primary-light/30 rounded-full blur-3xl pointer-events-none -z-10">
                        </div>
                        <div
                            class="absolute bottom-10 right-10 w-96 h-96 bg-emerald-200/20 rounded-full blur-3xl pointer-events-none -z-10">
                        </div>

                        <!-- Banner Announcement -->
                        <div
                            class="inline-flex items-center gap-2 bg-emerald-100/80 border border-emerald-200/50 px-4 py-1.5 rounded-full mb-6 text-xs md:text-sm font-semibold text-primary shadow-sm animate-bounce">
                            <span class="material-symbols-outlined text-[18px]">verified</span>
                            <span>Cam kết 100% nông sản chuẩn VietGAP & Hữu Cơ</span>
                        </div>

                        <h1
                            class="text-3xl md:text-5xl lg:text-6xl font-bold text-on-surface mb-6 max-w-4xl leading-tight">
                            Nông Sản Sạch Cao Cấp <br>
                            <span
                                class="text-primary bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Giao
                                Nhanh Tận Cửa Nhà Bạn</span>
                        </h1>

                        <p class="text-sm md:text-lg text-on-surface-variant mb-10 max-w-2xl font-light">
                            Trải nghiệm tinh hoa trái cây và nông sản hữu cơ chín tự nhiên từ các nhà vườn trứ danh Việt
                            Nam, bảo đảm độ ngọt lành và an toàn vệ sinh thực phẩm tối đa.
                        </p>

                        <!-- Dynamic Search Form (Massive AI Ready Search Box) -->
                        <div
                            class="w-full max-w-3xl glass-panel p-2 rounded-full shadow-lg hover:shadow-xl transition-all duration-300 group focus-within:ring-2 focus-within:ring-primary/20 relative z-20">
                            <form action="${pageContext.request.contextPath}/home" method="get"
                                class="flex items-center w-full">
                                <!-- If category filter is active, retain it on search -->
                                <c:if test="${not empty selectedCategoryId}">
                                    <input type="hidden" name="categoryId" value="${selectedCategoryId}">
                                </c:if>
                                <div class="flex items-center flex-1 pl-4 md:pl-6">
                                    <span
                                        class="material-symbols-outlined text-primary text-[24px] group-focus-within:scale-110 transition-transform">search</span>
                                    <input
                                        class="w-full bg-transparent border-none text-on-surface placeholder:text-on-surface-variant/70 focus:ring-0 text-sm md:text-base ml-3 outline-none"
                                        id="searchInput" name="keyword" value="${fn:escapeXml(keyword)}"
                                    placeholder="Tìm đặc sản sầu riêng Ri6, vải thiều Lục Ngạn, dâu tây Đà Lạt..."
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

                        <!-- AI Suggested Keywords & Quick Prompts -->
                        <div class="flex flex-wrap justify-center items-center gap-2 mt-4 max-w-2xl">
                            <span class="text-xs text-on-surface-variant flex items-center gap-1 font-semibold">
                                <span class="material-symbols-outlined text-[16px] text-primary">psychology</span>
                                Tìm gợi ý AI:
                            </span>
                            <button onclick="applyAiPrompt('sầu riêng chín ngọt béo ngậy')"
                                class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                "Sầu riêng chín cây ngọt béo" 👑
                            </button>
                            <button onclick="applyAiPrompt('trái cây nhập khẩu giàu vitamin c')"
                                class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                "Quả mọng giàu Vitamin C" 🍓
                            </button>
                            <button onclick="applyAiPrompt('cam vàng ngọt mát')"
                                class="text-xs bg-white/60 border border-white/50 px-3 py-1 rounded-full text-on-surface-variant hover:bg-primary-light hover:text-primary transition-all shadow-sm">
                                "Cam mọng nước giải nhiệt" 🍊
                            </button>
                        </div>
                    </section>


                    <!-- FLASH SALE SECTION (Golden hour countdown) -->
                    <c:if test="${not empty flashSaleProducts}">
                        <section class="px-6 md:px-12 max-w-7xl mx-auto mb-16">
                            <div class="glass-panel flash-glow rounded-3xl p-6 md:p-8 border-red-100/50 bg-white/65">

                                <!-- Section Header with Real-Time Clock & Slider Nav -->
                                <div
                                    class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 pb-4 border-b border-red-100/50">
                                    <div class="flex items-center gap-2">
                                        <span
                                            class="material-symbols-outlined text-red-500 text-[32px] animate-pulse font-bold">bolt</span>
                                        <div>
                                            <h2 class="text-xl md:text-2xl font-bold tracking-tight text-red-600">⚡ SIÊU
                                                DEAL GIỜ VÀNG - FLASH SALE</h2>
                                            <p class="text-xs text-on-surface-variant font-light mt-0.5">Đặc sản nông
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

                                                        <!-- Short Description for Flash Sale items -->
                                                        <p
                                                            class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mb-2 h-8 leading-relaxed">
                                                            <c:out value="${item.description}" />
                                                        </p>

                                                        <!-- Stars and Unit Info -->
                                                        <div class="flex items-center gap-2 mb-3">
                                                            <div class="text-amber-500 scale-90 -ml-1">
                                                                <ft:stars rating="${item.rating}" showValue="false" />
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
                                                        <div class="bg-gradient-to-r from-red-500 to-orange-500 h-full rounded-full transition-all duration-500"
                                                            style="width: ${percentRemaining}%"></div>
                                                    </div>
                                                </div>

                                                <button type="button"
                                                    onclick="quickAddProduct(event, ${item.productId})"
                                                    class="w-full bg-red-50 border border-red-200 hover:bg-red-600 hover:text-white text-red-600 font-bold text-xs py-2.5 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 cursor-pointer">
                                                    <span
                                                        class="material-symbols-outlined text-[16px]">shopping_cart</span>
                                                    Mua ngay Deal sốc
                                                </button>
                                            </div>
                                        </article>
                                    </c:forEach>
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
                                <a href="${pageContext.request.contextPath}/home?keyword=${keyword}"
                                class="px-5 py-2 rounded-full text-xs font-semibold whitespace-nowrap shadow-sm border
                                transition-all hover:scale-105 duration-200 ${empty selectedCategoryId ? 'bg-primary
                                text-white border-primary shadow-emerald-950/10' : 'bg-white border-white/60
                                text-on-surface-variant hover:bg-emerald-50'}">
                                Tất cả sản phẩm
                                </a>

                                <!-- Database Driven Categories -->
                                <c:forEach var="cat" items="${categories}">
                                    <a href="${pageContext.request.contextPath}/home?categoryId=${cat.categoryId}&keyword=${keyword}"
                                    class="px-5 py-2 rounded-full text-xs font-semibold whitespace-nowrap shadow-sm
                                    border
                                    transition-all hover:scale-105 duration-200 ${selectedCategoryId == cat.categoryId ?
                                    'bg-primary text-white border-primary shadow-emerald-950/10' : 'bg-white
                                    border-white/60
                                    text-on-surface-variant hover:bg-emerald-50'}">
                                    <c:out value="${cat.name}" />
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </section>

                    <!-- SEASONAL HARVEST CATALOG GRID -->
                    <section class="px-6 md:px-12 max-w-7xl mx-auto pb-32 relative z-10">

                        <!-- Section Header -->
                        <div class="flex justify-between items-center mb-8">
                            <div>
                                <h2 class="text-xl md:text-2xl font-bold text-on-surface">🍎 THU HOẠCH CHÍNH VỤ - SẢN
                                    PHẨM
                                    MỚI</h2>
                                <p class="text-xs text-on-surface-variant font-light mt-0.5">Nông sản sạch chín tự nhiên
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

                        <!-- Empty Products Fallback State -->
                        <c:if test="${empty normalProducts}">
                            <div
                                class="glass-panel rounded-3xl p-16 text-center max-w-2xl mx-auto ambient-shadow flex flex-col items-center gap-4">
                                <span
                                    class="material-symbols-outlined text-[64px] text-primary/40 animate-pulse">eco</span>
                                <div>
                                    <h3 class="font-bold text-lg text-on-surface">Không tìm thấy sản phẩm phù hợp</h3>
                                    <p class="text-xs text-on-surface-variant font-light mt-1">Xin lỗi, hệ thống không
                                        tìm
                                        thấy trái cây khớp với bộ lọc tìm kiếm của bạn. Hãy thử đổi từ khóa khác nhé!
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
                                                    <div class="flex items-center gap-1 text-amber-500 scale-90 -ml-1">
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

                                            <button type="button" onclick="quickAddProduct(event, ${item.productId})"
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
                                                <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span
                                                class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                                <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>

                                    <!-- Page Numbers -->
                                    <c:forEach var="p" begin="1" end="${totalPages}">
                                        <c:url var="pageUrl" value="/home">
                                            <c:param name="page" value="${p}" />
                                            <c:if test="${not empty keyword}">
                                                <c:param name="keyword" value="${keyword}" />
                                            </c:if>
                                            <c:if test="${not empty selectedCategoryId}">
                                                <c:param name="categoryId" value="${selectedCategoryId}" />
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
                                                <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span
                                                class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                                <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                        </c:if>
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
                                <span class="material-symbols-outlined text-[18px] text-on-surface-variant">close</span>
                            </button>
                        </div>

                        <!-- Product Info Row -->
                        <div class="flex items-center gap-4 mb-5 bg-emerald-50/60 rounded-2xl p-3">
                            <img id="qa-product-img" src="" alt=""
                                class="w-20 h-20 rounded-xl object-cover shrink-0 border border-emerald-100/50"
                                onerror="handleImageError(this)">
                            <div class="flex-grow min-w-0">
                                <h3 id="qa-product-name" class="font-bold text-sm text-on-surface line-clamp-2">Tên sản
                                    phẩm</h3>
                                <p id="qa-product-desc"
                                    class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mt-1"></p>
                            </div>
                        </div>

                        <!-- Variant Options -->
                        <div class="mb-5">
                            <label class="block text-xs font-bold text-on-surface-variant mb-2">Chọn phân loại:</label>
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
                            const data = await response.json();

                            if (!data.success || !data.variants || data.variants.length === 0) {
                                alert('Sản phẩm hiện không khả dụng.');
                                return;
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
                        const success = await window.addCartItem(v.variantId, qty, `\${p.name} - \${v.variantLabel}`, v.price, imagePath, v.stockQuantity, p.productId);
                        if (success) {
                            closeQuickAddModal();
                        }
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
                        try {
                            const isLoggedIn = window.isLoggedIn === true;
                            const contextPath = window.contextPath || '';

                            const response = await fetch(`\${contextPath}/cart?action=add`, {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                    'X-Requested-With': 'XMLHttpRequest'
                                },
                                body: `variantId=\${variantId}&quantity=\${quantity}&_csrf=\${window.csrfToken || ''}`
                            });
                            const data = await response.json();

                            if (data.success) {
                                if (isLoggedIn) {
                                    if (data.cartSummary && data.cartSummary.items) {
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
                                    }
                                } else {
                                    const item = {
                                        variantId: parseInt(variantId),
                                        name: name,
                                        price: parseFloat(price),
                                        quantity: parseInt(quantity),
                                        imagePath: imagePath || 'assets/img/placeholder.png',
                                        stockQuantity: parseInt(stockQuantity) || 99,
                                        productId: parseInt(productId) || null
                                    };
                                    if (typeof GuestCart !== 'undefined') {
                                        GuestCart.add(item);
                                    } else {
                                        let items = JSON.parse(localStorage.getItem('guestCart')) || [];
                                        const idx = items.findIndex(i => i.variantId === item.variantId);
                                        if (idx >= 0) items[idx].quantity += item.quantity;
                                        else items.push(item);
                                        localStorage.setItem('guestCart', JSON.stringify(items));
                                    }
                                }

                                // Update header badges
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

                                // Update home card displays
                                if (window.updateCardAddedQuantities) {
                                    window.updateCardAddedQuantities();
                                }

                                // Toast
                                showCartSuccessToast(name, quantity);
                                return true;
                            } else {
                                alert(data.error || 'Có lỗi xảy ra khi thêm vào giỏ hàng.');
                                return false;
                            }
                        } catch (err) {
                            console.error('Lỗi thêm giỏ hàng:', err);
                            alert('Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.');
                            return false;
                        }
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

                    // Launch initializers
                    document.addEventListener('DOMContentLoaded', () => {
                        startFlashSaleTimer();
                        if (window.updateCardAddedQuantities) {
                            window.updateCardAddedQuantities();
                        }
                    });
                </script>

                <!-- Load site footer -->
                <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />