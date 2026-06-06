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
                            <input type="number" name="minPrice" placeholder="Giá tối thiểu" value="${minPrice}" min="0"
                                   class="w-full px-3 py-2 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-xs outline-none">
                            <input type="number" name="maxPrice" placeholder="Giá tối đa" value="${maxPrice}" min="0"
                                   class="w-full px-3 py-2 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-xs outline-none">
                        </div>
                    </div>

                    <!-- Sắp xếp (Dành cho nâng cấp tính năng tương lai) -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-primary" for="sortSelector">Sắp xếp hiển thị</label>
                        <select id="sortSelector" name="sort"
                                class="w-full px-4 py-2.5 bg-white border border-outline/20 focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none">
                            <option value="newest">Sản phẩm mới nhất</option>
                            <option value="best_seller">Bán chạy nhất</option>
                            <option value="price_asc">Giá tăng dần</option>
                            <option value="price_desc">Giá giảm dần</option>
                        </select>
                    </div>

                    <div class="pt-4 flex flex-col gap-2">
                        <button type="submit"
                                class="w-full bg-primary hover:bg-primary-hover text-white text-xs font-semibold py-3 px-4 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-md active:scale-95 cursor-pointer">
                            <span class="material-symbols-outlined text-[16px]">done</span>
                            Áp dụng bộ lọc
                        </button>
                        <a href="${pageContext.request.contextPath}/products"
                           class="w-full border border-primary/20 bg-white/60 hover:bg-emerald-50 text-primary text-xs font-semibold py-3 px-4 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-sm active:scale-95 text-center">
                            <span class="material-symbols-outlined text-[16px]">refresh</span>
                            Đặt lại bộ lọc
                        </a>
                    </div>
                </form>
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
                        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                            <c:forEach var="p" items="${pagedResult.items}">
                                <article data-product-id="${p.productId}"
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

                                        <button type="button" onclick="quickAddProduct(event, ${p.productId})"
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

                                <!-- Page Numbers -->
                                <c:forEach var="pageNum" begin="1" end="${pagedResult.totalPages}">
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

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
