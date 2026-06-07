<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>

<!-- Load header and inject Page Title -->
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Danh sách sản phẩm - MetaFruit" />
</jsp:include>

<!-- Google Fonts Lexend & Material Icons -->
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">

<!-- Tailwind CSS Engine for consistent modern rich aesthetics -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>

<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    "primary": "#14532D", // Match MetaFruit theme precisely
                    "primary-hover": "#166534",
                    "primary-light": "#d1ffd8",
                    "surface": "#eaffea",
                    "on-surface": "#00210d",
                    "on-surface-variant": "#44483b",
                    "outline": "#75796a",
                    "outline-variant": "#c5c8b7"
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
    .glass-panel {
        background: rgba(255, 255, 255, 0.75);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.4);
    }
    .ambient-shadow {
        box-shadow: 0 10px 40px rgba(20, 83, 45, 0.06);
    }
    .hide-scrollbar::-webkit-scrollbar {
        display: none;
    }
    .hide-scrollbar {
        -ms-overflow-style: none;
        scrollbar-width: none;
    }
</style>

<script>
    window.handleImageError = function (img) {
        if (!img.dataset.errorStage) {
            img.dataset.errorStage = "1";
            img.src = "https://images.unsplash.com/photo-1610832958506-ee5633619144?w=600&auto=format&fit=crop&q=80";
        } else if (img.dataset.errorStage === "1") {
            img.dataset.errorStage = "2";
            img.src = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MDAgMzAwIiB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIj48ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9ImIiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiNmNGZiZjciLz48c3RvcCBvZmZzZXQ9IjEwMCUiIHN0b3AtY29sb3I9IiNlMmY1ZWEiLz48L2xpbmVhckdyYWRpZW50PjxsaW5lYXJHcmFkaWVudCBpZD0icCIgeDE9IjAlIiB5MT0iMCUiIHgyPSIxMDAlIiB5Mj0iMTAwJSI+PHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iIzRkNjYxYyIvPjxzdG9wIG9mZnNldD0iMTAwJSIgc3RvcC1jb2xvcj0iIzMxNjk0YiIvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9InVybCgjYikiIHJ4PSIxNiIvPjxjaXJjbGUgY3g9IjIwMCIgY3k9IjEyMCIgcj0iNDUiIGZpbGw9IiNkOWY5OWQiIG9wYWNpdHk9IjAuNiIvPjxwYXRoIGQ9Ik0yMDAsODVjMjUsMCA0MCwyNSAxNSw1MGMtMjUsMC00MC0yNS0xNS01MHoiIGZpbGw9InVybCgjcCkiLz48cGF0aCBkPSJNMjAwLDEwNWMtMTUsMC0yNSwxNS0xMCwzMGMxNSwwIDI1LTE1IDEwLTMweiIgZmlsbD0iIzg0Y2MxNiIvPjx0ZXh0IHg9IjIwMCIgeT0iMjAwIiBmb250LWZhbWlseT0ic3lzdGVtLXVpLHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTgiIGZvbnQtd2VpZ2h0PSI3MDAiIGZpbGw9IiMwMDIxMGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiPlZlcmRhbnQgTWFya2V0PC90ZXh0Pjx0ZXh0IHg9IjIwMCIgeT0iMjI1IiBmb250LWZhbWlseT0ic3lzdGVtLXVpLHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTIiIGZvbnQtd2VpZ2h0PSI0MDAiIGZpbGw9IiM0NDQ4M2IiIHRleHQtYW5jaG9yPSJtaWRkbGUiPk7DtG5nIFPhuqNuIFPhuqFjaCBDYW8gQ2FwPC90ZXh0Pjwvc3ZnPg==";
        }
    };

    function quickAddProduct(event, productId) {
        event.preventDefault();
        event.stopPropagation();
        if (window.quickAddProductGlobal) {
            window.quickAddProductGlobal(productId);
        } else {
            // Fallback: Redirect to detail page
            window.location.href = "${pageContext.request.contextPath}/products/detail?id=" + productId;
        }
    }
</script>

<div class="bg-gradient-to-br from-emerald-50/50 via-white to-emerald-100/40 min-h-screen pt-28 pb-20">
    <div class="max-w-7xl mx-auto px-4 md:px-8">
        
        <!-- Page Title & Navigation Path -->
        <div class="mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
                <h1 class="text-2xl md:text-3xl font-bold text-primary flex items-center gap-2">
                    <span class="material-symbols-outlined text-[32px]">storefront</span>
                    Sản Phẩm Của Chúng Tôi
                </h1>
                <p class="text-xs md:text-sm text-on-surface-variant font-light mt-1">
                    Khám phá nguồn đặc sản sạch hữu cơ VietGAP chất lượng cao bảo vệ sức khỏe gia đình bạn.
                </p>
            </div>
            <nav class="flex items-center gap-2 text-xs font-semibold bg-white/70 px-4 py-2 rounded-full border border-white/50 shadow-sm w-fit">
                <a href="${pageContext.request.contextPath}/" class="text-on-surface-variant hover:text-primary transition-colors">Trang chủ</a>
                <span class="material-symbols-outlined text-[14px] text-outline">chevron_right</span>
                <span class="text-primary">Sản phẩm</span>
            </nav>
        </div>

        <div class="flex flex-col lg:flex-row gap-8 items-start">
            
            <!-- Left Sidebar Filter (Glassmorphism design) -->
            <aside class="w-full lg:w-[280px] shrink-0 glass-panel rounded-3xl p-6 ambient-shadow bg-white/60">
                <h2 class="font-bold text-base text-primary mb-6 flex items-center gap-2 pb-3 border-b border-primary/10">
                    <span class="material-symbols-outlined text-[20px]">filter_alt</span>
                    Bộ Lọc Tìm Kiếm
                </h2>
                
                <form action="${pageContext.request.contextPath}/products" method="get" class="space-y-6">
                    <!-- Sắp xếp -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary" for="sortSelector">Sắp xếp hiển thị</label>
                        <select id="sortSelector" name="sort"
                                class="w-full px-4 py-2.5 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none">
                            <option value="newest" selected>Mới nhất</option>
                            <option value="price_asc">Giá: Thấp đến Cao</option>
                            <option value="price_desc">Giá: Cao đến Thấp</option>
                            <option value="best_seller">Số lượt bán</option>
                        </select>
                    </div>

                    <!-- Keyword search field -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary" for="searchKeyword">Tìm kiếm từ khóa</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">search</span>
                            <input type="text" id="searchKeyword" name="keyword" value="${fn:escapeXml(keyword)}"
                                   placeholder="Tên sản phẩm..."
                                   class="w-full pl-9 pr-4 py-2.5 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none">
                        </div>
                    </div>

                    <!-- Category selector field -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary" for="catSelector">Danh mục sản phẩm</label>
                        <select id="catSelector" name="categoryId"
                                class="w-full px-4 py-2.5 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none">
                            <option value="">Tất cả danh mục</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${cat.categoryId == categoryId ? 'selected' : ''}>
                                    <c:out value="${cat.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Range of price field -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary">Khoảng giá (VNĐ)</label>
                        <div class="grid grid-cols-2 gap-2">
                            <input type="number" id="minPriceInput" name="minPrice" placeholder="Giá tối thiểu" value="${minPrice}" min="0"
                                   class="w-full px-3 py-2 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-xs outline-none">
                            <input type="number" id="maxPriceInput" name="maxPrice" placeholder="Giá tối đa" value="${maxPrice}" min="0"
                                   class="w-full px-3 py-2 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-xs outline-none">
                        </div>
                    </div>

                    <!-- Filter Rating -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary">Đánh giá sản phẩm</label>
                        <div class="space-y-1.5">
                            <label class="flex items-center gap-2 text-xs text-on-surface-variant cursor-pointer">
                                <input type="radio" name="ratingFilter" value="0" checked class="text-primary focus:ring-primary rounded">
                                <span>Tất cả</span>
                            </label>
                            <label class="flex items-center gap-2 text-xs text-on-surface-variant cursor-pointer">
                                <input type="radio" name="ratingFilter" value="5" class="text-primary focus:ring-primary rounded">
                                <span class="flex items-center gap-0.5">5 sao <span class="material-symbols-outlined text-[13px] text-amber-500">star</span></span>
                            </label>
                            <label class="flex items-center gap-2 text-xs text-on-surface-variant cursor-pointer">
                                <input type="radio" name="ratingFilter" value="4" class="text-primary focus:ring-primary rounded">
                                <span class="flex items-center gap-0.5">4 sao trở lên <span class="material-symbols-outlined text-[13px] text-amber-500">star</span></span>
                            </label>
                            <label class="flex items-center gap-2 text-xs text-on-surface-variant cursor-pointer">
                                <input type="radio" name="ratingFilter" value="3" class="text-primary focus:ring-primary rounded">
                                <span class="flex items-center gap-0.5">3 sao trở lên <span class="material-symbols-outlined text-[13px] text-amber-500">star</span></span>
                            </label>
                        </div>
                    </div>

                    <!-- Filter Availability -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary">Tình trạng kho hàng</label>
                        <label class="flex items-center gap-2 text-xs text-on-surface-variant cursor-pointer">
                            <input type="checkbox" id="inStockFilter" class="text-primary focus:ring-primary rounded">
                            <span>Chỉ hiển thị còn hàng</span>
                        </label>
                    </div>

                    <div class="pt-4 flex flex-col gap-2">
                        <button type="submit"
                                class="w-full bg-primary hover:bg-primary-hover text-white text-xs font-semibold py-3 px-4 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-md active:scale-95 cursor-pointer">
                            <span class="material-symbols-outlined text-[16px]">done</span>
                            Áp dụng bộ lọc
                        </button>
                        <a href="${pageContext.request.contextPath}/products" onclick="sessionStorage.removeItem('aiFilteredProductIds')"
                           class="w-full border border-primary/20 bg-white/60 hover:bg-emerald-50 text-primary text-xs font-semibold py-3 px-4 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 text-center">
                            <span class="material-symbols-outlined text-[16px]">refresh</span>
                            Đặt lại bộ lọc
                        </a>
                    </div>
                </form>

                <!-- Recently Viewed Widget -->
                <div class="mt-8 pt-6 border-t border-primary/10">
                    <h3 class="font-bold text-sm text-primary mb-4 flex items-center gap-2">
                        <span class="material-symbols-outlined text-[18px]">history</span>
                        Đã Xem Gần Đây (Recently Viewed)
                    </h3>
                    <div id="recentlyViewedContainer"></div>
                </div>
            </aside>

            <!-- Right Results Area -->
            <main class="flex-1 w-full">
                <!-- Search Result Header Status -->
                <div class="flex justify-between items-center mb-6 bg-white/60 border border-white/50 px-5 py-3 rounded-2xl shadow-sm glass-panel">
                    <span class="text-xs md:text-sm font-medium text-on-surface-variant">
                        Tìm thấy <strong class="text-primary font-bold">${not empty pagedResult ? pagedResult.totalItems : 0}</strong> sản phẩm
                    </span>
                    <c:if test="${not empty keyword or not empty categoryId or not empty minPrice or not empty maxPrice}">
                        <span class="text-xs bg-emerald-100 text-primary px-3 py-1 rounded-full font-semibold">Đang lọc</span>
                    </c:if>
                </div>

                <!-- AI Filter alert banner -->
                <div id="aiFilterBanner" class="hidden flex items-center justify-between gap-3 mb-6 bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-200/80 p-4 rounded-2xl shadow-sm">
                    <div class="flex items-center gap-2 text-xs md:text-sm text-emerald-800 font-semibold">
                        <span class="material-symbols-outlined text-[20px] text-emerald-600 animate-pulse">psychology</span>
                        <span>Đang lọc danh sách sản phẩm theo gợi ý của Trợ lý AI.</span>
                    </div>
                    <button onclick="clearAiFilter()" class="bg-white hover:bg-emerald-100 border border-emerald-200/80 text-emerald-800 font-bold px-3 py-1.5 rounded-lg text-xs transition-all cursor-pointer shadow-sm flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">refresh</span> Đặt lại
                    </button>
                </div>

                <!-- Products Grid -->
                <c:choose>
                    <c:when test="${empty pagedResult or empty pagedResult.items}">
                        <!-- Empty Fallback View -->
                        <div class="glass-panel rounded-3xl p-16 text-center max-w-xl mx-auto ambient-shadow flex flex-col items-center gap-4 bg-white/60">
                            <span class="material-symbols-outlined text-[64px] text-primary/30 animate-bounce">sentiment_dissatisfied</span>
                            <div>
                                <h3 class="font-bold text-lg text-on-surface">Không tìm thấy sản phẩm phù hợp</h3>
                                <p class="text-xs text-on-surface-variant font-light mt-1.5 leading-relaxed">
                                    Rất tiếc! Hệ thống không tìm thấy nông sản nào khớp với yêu cầu bộ lọc hiện tại của bạn. Vui lòng thử lại với từ khóa khác hoặc xóa bớt tiêu chí lọc nhé.
                                </p>
                            </div>
                            <a href="${pageContext.request.contextPath}/products"
                               class="btn bg-primary hover:bg-primary-hover text-white text-xs font-semibold px-6 py-3 rounded-full mt-2 shadow-md">
                                Xem tất cả sản phẩm
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- Grid layout -->
                        <div id="productsGrid" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                            <c:forEach var="p" items="${pagedResult.items}">
                                <article data-product-id="${p.productId}"
                                         data-price="${p.price}"
                                         data-rating="${p.rating}"
                                         data-sold="${p.soldQuantity}"
                                         data-category-id="${p.categoryId}"
                                         data-in-stock="${p.inStock}"
                                         class="bg-white/80 glass-panel rounded-3xl p-3 ambient-shadow flex flex-col group hover:-translate-y-1.5 hover:shadow-lg hover:border-emerald-300/40 transition-all duration-300">
                                    
                                    <a href="${pageContext.request.contextPath}/products/detail?id=${p.productId}"
                                       class="block group/link flex-grow" style="text-decoration: none; color: inherit;">
                                        <!-- Image Aspect Ratio block -->
                                        <div class="relative aspect-[4/3] rounded-2xl overflow-hidden mb-4 bg-emerald-50" style="aspect-ratio: 4/3;">
                                            <img src="${p.image}" alt="${fn:escapeXml(p.name)}"
                                                 onerror="handleImageError(this)"
                                                 class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                                            
                                            <!-- Category tag standard badge -->
                                            <div class="absolute top-3 right-3 bg-primary text-white text-[10px] font-bold px-2 py-0.5 rounded-md shadow-sm">
                                                Nông sản sạch
                                            </div>
                                        </div>

                                        <!-- Content Block -->
                                        <div class="px-1 mb-3">
                                            <h3 class="font-bold text-sm text-on-surface line-clamp-1 mb-1 group-hover:text-primary transition-colors">
                                                <c:out value="${p.name}"/>
                                            </h3>
                                            <p class="text-xs text-on-surface-variant/80 font-light line-clamp-2 mb-2 h-8 leading-relaxed">
                                                <c:out value="${p.description}"/>
                                            </p>

                                            <!-- Ratings & Sold volume -->
                                            <div class="flex justify-between items-center">
                                                <div class="flex items-center gap-1 text-amber-500 scale-90 -ml-1">
                                                    <ft:stars rating="${p.rating}" showValue="true"/>
                                                </div>
                                                <span class="text-[10px] text-on-surface-variant font-medium">
                                                    Đã bán ${p.soldQuantity}
                                                </span>
                                            </div>
                                        </div>
                                    </a>

                                    <!-- Bottom Action (Price & Add to cart) -->
                                    <div class="flex justify-between items-center gap-3 pt-3 border-t border-gray-100 mt-auto px-1">
                                        <div class="flex flex-col">
                                            <span class="text-base font-bold text-primary">
                                                <ft:currency value="${p.price}"/>
                                            </span>
                                            <span class="text-[10px] text-on-surface-variant font-light">
                                                / <c:out value="${p.unit}"/>
                                            </span>
                                        </div>

                                        <button type="button" onclick="quickAddProduct(event, '${p.productId}')"
                                                class="bg-primary hover:bg-primary-hover text-white p-2.5 rounded-xl flex items-center justify-center hover:scale-105 active:scale-95 transition-all shadow-sm cursor-pointer"
                                                title="Thêm vào giỏ hàng">
                                            <span class="material-symbols-outlined text-[20px]">add_shopping_cart</span>
                                        </button>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>

                        <!-- Beautiful Pagination Controls -->
                        <c:if test="${pagedResult.totalPages > 1}">
                            <div class="flex justify-center items-center mt-12 gap-2">
                                <!-- Prev Button -->
                                <c:choose>
                                    <c:when test="${pagedResult.currentPage > 1}">
                                        <c:url var="prevUrl" value="/products">
                                            <c:param name="page" value="${pagedResult.currentPage - 1}" />
                                            <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                            <c:if test="${not empty categoryId}"><c:param name="categoryId" value="${categoryId}" /></c:if>
                                            <c:if test="${not empty minPrice}"><c:param name="minPrice" value="${minPrice}" /></c:if>
                                            <c:if test="${not empty maxPrice}"><c:param name="maxPrice" value="${maxPrice}" /></c:if>
                                        </c:url>
                                        <a href="${prevUrl}"
                                           class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                            <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-[20px]">chevron_left</span>
                                        </span>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Page Numbers with Ellipsis -->
                                <c:forEach var="pageNum" begin="1" end="${pagedResult.totalPages}">
                                    <c:choose>
                                        <%-- Display conditions: first page, last page, and neighbor pages --%>
                                        <c:when test="${pageNum == 1 || pageNum == pagedResult.totalPages || (pageNum >= pagedResult.currentPage - 1 && pageNum <= pagedResult.currentPage + 1)}">
                                            <c:url var="pageUrl" value="/products">
                                                <c:param name="page" value="${pageNum}" />
                                                <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                                <c:if test="${not empty categoryId}"><c:param name="categoryId" value="${categoryId}" /></c:if>
                                                <c:if test="${not empty minPrice}"><c:param name="minPrice" value="${minPrice}" /></c:if>
                                                <c:if test="${not empty maxPrice}"><c:param name="maxPrice" value="${maxPrice}" /></c:if>
                                            </c:url>
                                            <c:choose>
                                                <c:when test="${pagedResult.currentPage == pageNum}">
                                                    <span class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white font-bold shadow-md shadow-primary/20">
                                                        ${pageNum}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageUrl}"
                                                       class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-on-surface-variant font-medium hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                                        ${pageNum}
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <%-- Ellipsis before active range --%>
                                        <c:when test="${pageNum == 2 && pagedResult.currentPage > 3}">
                                            <span class="w-10 h-10 flex items-center justify-center text-on-surface-variant/50 font-bold">...</span>
                                        </c:when>
                                        <%-- Ellipsis after active range --%>
                                        <c:when test="${pageNum == pagedResult.totalPages - 1 && pagedResult.currentPage < pagedResult.totalPages - 2}">
                                            <span class="w-10 h-10 flex items-center justify-center text-on-surface-variant/50 font-bold">...</span>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>

                                <!-- Next Button -->
                                <c:choose>
                                    <c:when test="${pagedResult.currentPage < pagedResult.totalPages}">
                                        <c:url var="nextUrl" value="/products">
                                            <c:param name="page" value="${pagedResult.currentPage + 1}" />
                                            <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                            <c:if test="${not empty categoryId}"><c:param name="categoryId" value="${categoryId}" /></c:if>
                                            <c:if test="${not empty minPrice}"><c:param name="minPrice" value="${minPrice}" /></c:if>
                                            <c:if test="${not empty maxPrice}"><c:param name="maxPrice" value="${maxPrice}" /></c:if>
                                        </c:url>
                                        <a href="${nextUrl}"
                                           class="flex items-center justify-center w-10 h-10 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-95 duration-200">
                                            <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="flex items-center justify-center w-10 h-10 rounded-xl border border-gray-100 bg-gray-50/50 text-gray-400 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-[20px]">chevron_right</span>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>
    </div>
</div>

<!-- Floating AI Support Assistant Button -->
<div class="fixed bottom-6 right-6 z-50 group">
    <button type="button" aria-label="AI Assistant"
            class="relative w-14 h-14 bg-gradient-to-tr from-emerald-600 to-teal-500 text-white rounded-full flex items-center justify-center shadow-lg hover:shadow-emerald-500/20 active:scale-95 hover:scale-105 transition-all duration-300 cursor-pointer overflow-hidden border border-emerald-400/30">
        <!-- Pulse effect -->
        <span class="absolute inset-0 rounded-full bg-emerald-400/20 animate-ping opacity-75"></span>
        <span class="material-symbols-outlined text-[28px] animate-pulse relative z-10">smart_toy</span>
    </button>
    <!-- Tooltip -->
    <span class="absolute right-16 top-1/2 -translate-y-1/2 bg-on-surface text-white text-xs font-semibold px-3 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none whitespace-nowrap shadow-md">
        Trợ lý AI hỗ trợ 24/7
    </span>
</div>

<script>
    // Helper to clear AI filter
    function clearAiFilter() {
        sessionStorage.removeItem('aiFilteredProductIds');
        applyClientFilters();
    }

    // Client-side Filter & Sort logic
    function applyClientFilters() {
        const minPriceInput = document.getElementById('minPriceInput');
        const maxPriceInput = document.getElementById('maxPriceInput');
        const minPrice = minPriceInput && minPriceInput.value.trim() !== '' ? parseFloat(minPriceInput.value) : 0;
        const maxPrice = maxPriceInput && maxPriceInput.value.trim() !== '' ? parseFloat(maxPriceInput.value) : Infinity;
        
        const checkedRating = document.querySelector('input[name="ratingFilter"]:checked');
        const minRating = checkedRating && checkedRating.value.trim() !== '' ? parseFloat(checkedRating.value) : 0;
        
        const inStockOnly = document.getElementById('inStockFilter')?.checked || false;
        const sortVal = document.getElementById('sortSelector')?.value || 'newest';

        // Check for AI filter
        const aiFilteredIdsRaw = sessionStorage.getItem('aiFilteredProductIds');
        const aiFilteredIds = aiFilteredIdsRaw ? JSON.parse(aiFilteredIdsRaw) : null;

        const banner = document.getElementById('aiFilterBanner');
        if (aiFilteredIds && aiFilteredIds.length > 0) {
            if (banner) banner.classList.remove('hidden');
        } else {
            if (banner) banner.classList.add('hidden');
        }

        const grid = document.getElementById('productsGrid');
        if (!grid) return;
        const items = Array.from(grid.querySelectorAll('article'));

        let visibleCount = 0;
        items.forEach(item => {
            const price = parseFloat(item.getAttribute('data-price')) || 0;
            const rating = parseFloat(item.getAttribute('data-rating')) || 0;
            const inStock = item.getAttribute('data-in-stock') === 'true';
            const productId = parseInt(item.getAttribute('data-product-id')) || 0;

            let show = true;
            if (price < minPrice || price > maxPrice) show = false;
            if (rating < minRating) show = false;
            if (inStockOnly && !inStock) show = false;
            if (aiFilteredIds && aiFilteredIds.length > 0 && !aiFilteredIds.includes(productId)) show = false;

            if (show) {
                item.style.setProperty('display', 'flex', 'important');
                visibleCount++;
            } else {
                item.style.setProperty('display', 'none', 'important');
            }
        });

        // Dynamic results count text update
        const resultsCountEl = document.querySelector('strong.text-primary');
        if (resultsCountEl) {
            resultsCountEl.textContent = visibleCount;
        }

        // Hide old fallback view if present
        let noProductsEl = document.getElementById('noProductsFallback');
        if (noProductsEl) {
            noProductsEl.style.display = 'none';
        }

        // B4: Chỉ hiện SweetAlert khi AI filter đang hoạt động VÀ không tìm thấy sp
        // Tránh toast lỗi xuất hiện ngay khi trang load bình thường không có sp
        if (visibleCount === 0 && aiFilteredIds && aiFilteredIds.length > 0) {
            Swal.fire({
                toast: true,
                position: 'top-end',
                icon: 'warning',
                title: 'Không tìm thấy sản phẩm',
                text: 'Rất tiếc! Hệ thống không tìm thấy nông sản nào khớp với yêu cầu bộ lọc hiện tại của bạn. Vui lòng đặt lại bộ lọc.',
                showConfirmButton: false,
                timer: 4500,
                timerProgressBar: true
            });
        }

        // Sorting visible items
        const visibleItems = items.filter(item => item.style.display !== 'none');
        visibleItems.sort((a, b) => {
            if (sortVal === 'price_asc') {
                return (parseFloat(a.getAttribute('data-price')) || 0) - (parseFloat(b.getAttribute('data-price')) || 0);
            } else if (sortVal === 'price_desc') {
                return (parseFloat(b.getAttribute('data-price')) || 0) - (parseFloat(a.getAttribute('data-price')) || 0);
            } else if (sortVal === 'best_seller') {
                return (parseInt(b.getAttribute('data-sold')) || 0) - (parseInt(a.getAttribute('data-sold')) || 0);
            } else {
                // newest - sort by product ID descending
                return (parseInt(b.getAttribute('data-product-id')) || 0) - (parseInt(a.getAttribute('data-product-id')) || 0);
            }
        });

        // Re-append sorted items
        visibleItems.forEach(item => grid.appendChild(item));
    }

    // Recently Viewed loader
    function loadRecentlyViewed() {
        const container = document.getElementById('recentlyViewedContainer');
        if (!container) return;
        const recentlyViewed = JSON.parse(localStorage.getItem('recentlyViewed') || '[]');
        if (recentlyViewed.length === 0) {
            container.innerHTML = '<p class="text-xs text-outline italic">Chưa có sản phẩm nào đã xem</p>';
            return;
        }
        let html = '<div class="space-y-3.5">';
        recentlyViewed.slice(0, 5).forEach(item => {
            const priceVal = parseFloat(item.price) || 0;
            const detailUrl = '${pageContext.request.contextPath}/products/detail?id=' + item.id;
            html += '<a href="' + detailUrl + '" class="flex items-center gap-3 p-2 bg-white/40 hover:bg-emerald-50/50 border border-transparent hover:border-primary/10 rounded-xl transition-all">' +
                    '<img src="' + item.image + '" class="w-10 h-10 object-cover rounded-lg" onerror="this.src=\'https://images.unsplash.com/photo-1610832958506-ee5633619144?w=100&auto=format&fit=crop&q=80\'">' +
                    '<div class="min-w-0 flex-1">' +
                        '<div class="text-xs font-bold text-on-surface truncate">' + item.name + '</div>' +
                        '<div class="text-[10px] text-primary font-bold">' + priceVal.toLocaleString() + ' ₫</div>' +
                    '</div>' +
                   '</a>';
        });
        html += '</div>';
        container.innerHTML = html;
    }

    document.addEventListener('DOMContentLoaded', () => {
        // Submit handler for the filter form
        const form = document.querySelector('aside form');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                applyClientFilters();
            });
        }

        // Automatic sort on change
        const sortSelector = document.getElementById('sortSelector');
        if (sortSelector) {
            sortSelector.addEventListener('change', () => {
                applyClientFilters();
            });
        }

        loadRecentlyViewed();
        applyClientFilters();
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
