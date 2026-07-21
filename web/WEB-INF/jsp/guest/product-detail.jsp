<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tld/fruitmkt.tld" %>

<%-- 
    product-detail.jsp — Giao diện xem chi tiết sản phẩm cao cấp (Premium Organic Glassmorphism).
    Cấu trúc trang:
    [1] 2-column grid: Gallery | Thông tin sản phẩm + Giá + Phân loại + Nút mua
    [2] Panel: Thông tin cửa hàng (tên, mô tả, rating) + Xem thêm từ shop + Vận chuyển + Voucher
    [3] Panel: Thông số kỹ thuật sản phẩm
    [4] Panel: Đánh giá (rating summary, filter tabs, phân trang)
    [5] Section: Sản phẩm tương tự (bên dưới review)
--%>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="${product.name}"/>
</jsp:include>

<!-- Load Tailwind CSS dynamically to sync container layout with Home page -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
<script id="tailwind-config">
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    primary: '#4D661C',
                    'primary-dark': '#364E03',
                    'primary-light': '#D9F99D',
                }
            }
        }
    }
</script>

<!-- Custom Premium CSS loaded from external asset -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/product-detail.css">

<div class="detail-container">
    <div class="container">
        
        <!-- ① Breadcrumbs Navigation -->
        <div class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
            <span class="separator"><i class="fa-solid fa-chevron-right"></i></span>
            <a href="${pageContext.request.contextPath}/products">Trái cây sạch</a>
            <span class="separator"><i class="fa-solid fa-chevron-right"></i></span>
            <span class="text-muted"><c:out value="${product.name}"/></span>
        </div>

        <!-- ② 2-Column Product Layout -->
        <div class="detail-grid">
            
            <!-- LEFT: Interactive Image Gallery -->
            <div class="gallery-wrapper">
                <div class="main-image-box premium-panel">
                    <c:choose>
                        <c:when test="${not empty images}">
                            <c:set var="firstImg" value="${images[0].filePath}"/>
                            <c:choose>
                                <c:when test="${fn:startsWith(firstImg, 'http://') || fn:startsWith(firstImg, 'https://')}">
                                    <img id="main-product-img" src="${firstImg}" alt="<c:out value='${product.name}'/>">
                                </c:when>
                                <c:otherwise>
                                    <c:set var="resolvedPath" value="${firstImg}"/>
                                    <c:if test="${!fn:startsWith(resolvedPath, '/')}">
                                        <c:set var="resolvedPath" value="/${resolvedPath}"/>
                                    </c:if>
                                    <img id="main-product-img" src="${pageContext.request.contextPath}${resolvedPath}" alt="<c:out value='${product.name}'/>" onerror="handleImageError(this)">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <img id="main-product-img" src="${pageContext.request.contextPath}/assets/img/placeholder.png" alt="Placeholder">
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- Thumbnails -->
                <c:if test="${fn:length(images) > 1}">
                    <div class="thumbnail-list">
                        <c:forEach var="img" items="${images}" varStatus="status">
                            <c:set var="thumbImg" value="${img.filePath}"/>
                            <c:choose>
                                <c:when test="${fn:startsWith(thumbImg, 'http://') || fn:startsWith(thumbImg, 'https://')}">
                                    <div class="thumbnail-item ${status.index == 0 ? 'active' : ''}" onclick="switchProductImage(this, '${thumbImg}')">
                                        <img src="${thumbImg}" alt="Thumbnail">
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="resolvedThumb" value="${thumbImg}"/>
                                    <c:if test="${!fn:startsWith(resolvedThumb, '/')}">
                                        <c:set var="resolvedThumb" value="/${resolvedThumb}"/>
                                    </c:if>
                                    <div class="thumbnail-item ${status.index == 0 ? 'active' : ''}" onclick="switchProductImage(this, '${pageContext.request.contextPath}${resolvedThumb}')">
                                        <img src="${pageContext.request.contextPath}${resolvedThumb}" alt="Thumbnail">
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <!-- RIGHT: Product Specifications & Cart -->
            <div class="premium-panel flex-grow">
                <!-- Badges Row -->
                <div class="flex items-center space-x-2 mb-3 gap-2">
                    <c:choose>
                        <c:when test="${isExpiredProduct}">
                            <span id="variant-status-badge" class="badge-stock badge-outstock !bg-red-200 !text-red-800 !border-red-300">
                                <i class="fa-solid fa-clock-rotate-left mr-1"></i> Hết vụ thu hoạch
                            </span>
                        </c:when>
                        <c:when test="${product.status == 'ACTIVE'}">
                            <span id="variant-status-badge" class="badge-stock ${not empty variants && variants[0].stockQuantity > 0 ? 'badge-instock' : 'badge-outstock'}">
                                <c:choose>
                                    <c:when test="${not empty variants && variants[0].stockQuantity > 0}">
                                        <i class="fa-solid fa-circle-check mr-1"></i> Còn hàng
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fa-solid fa-circle-xmark mr-1"></i> Hết hàng
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span id="variant-status-badge" class="badge-stock badge-outstock"><i class="fa-solid fa-circle-xmark mr-1"></i> Hết hàng</span>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${product.isOrganic}">
                        <span class="badge-stock !bg-emerald-100 !text-emerald-900 !border-emerald-300">
                            <i class="fa-solid fa-leaf mr-1"></i> Hữu Cơ
                        </span>
                    </c:if>
                    <c:if test="${product.isImported}">
                        <span class="badge-stock !bg-blue-100 !text-blue-900 !border-blue-300">
                            <i class="fa-solid fa-globe mr-1"></i> Nhập Khẩu
                        </span>
                    </c:if>
                    <c:choose>
                        <c:when test="${hasReviews}">
                            <span class="badge-rating-top">
                                <i class="fa-solid fa-star text-[#F59E0B]"></i>
                                <c:out value="${product.rating}"/> (<c:out value="${totalReviewsCount}"/> Đánh giá)
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge-rating-top !bg-slate-100 !text-slate-600 !border-slate-300 !shadow-sm">
                                <i class="fa-solid fa-star !text-slate-300"></i>
                                Chưa có đánh giá
                            </span>
                        </c:otherwise>
                    </c:choose>
                    <span class="text-xs text-muted"><i class="fa-solid fa-eye mr-1"></i> <c:out value="${product.viewCount}"/> lượt xem</span>
                </div>

                <!-- Product Name -->
                <h1 class="font-bold text-3xl mb-2 text-[#00210D] font-headline-lg tracking-[-0.03em]">
                    <c:out value="${product.name}"/>
                </h1>

                <c:if test="${isOutOfSeason && !isExpiredProduct}">
                    <div class="out-of-season-banner bg-amber-50 border border-amber-200 rounded-xl px-4 py-4 my-4 flex items-center justify-between gap-4">
                        <div class="flex items-center gap-3">
                            <div class="bg-amber-500 text-white min-w-10 h-10 rounded-full flex items-center justify-center text-lg shrink-0">
                                <i class="fa-solid fa-cloud-sun-rain"></i>
                            </div>
                            <div>
                                <h4 class="m-0 text-amber-700 font-bold text-sm">Sản phẩm đã hết mùa vụ gieo trồng</h4>
                                <p class="mt-1 text-amber-900/90 text-xs">Mùa vụ của sản phẩm này từ tháng ${product.seasonStartMonth} đến tháng ${product.seasonEndMonth}. Vui lòng quay lại khi đến mùa vụ hoặc liên hệ shop.</p>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${hasRequestedToday}">
                                <button type="button" id="btn-request-restock" disabled class="bg-slate-300 text-slate-500 border-0 px-4 py-2 rounded-lg font-bold text-xs cursor-not-allowed inline-flex items-center gap-2 whitespace-nowrap">
                                    <i class="fa-solid fa-check"></i> Đã Yêu Cầu Hôm Nay
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" id="btn-request-restock" onclick="requestRestock()" class="bg-primary text-white border-0 px-4 py-2 rounded-lg font-bold text-xs cursor-pointer inline-flex items-center gap-2 transition-transform duration-200 hover:-translate-y-0.5 whitespace-nowrap">
                                    <i class="fa-solid fa-paper-plane"></i> Yêu Cầu Nhập Kho
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${isExpiredProduct}">
                    <div class="expired-product-banner bg-red-50 border border-red-100 rounded-xl px-4 py-4 my-4 flex items-center justify-between gap-4">
                        <div class="flex items-center gap-3">
                            <div class="bg-red-500 text-white min-w-10 h-10 rounded-full flex items-center justify-center text-lg">
                                <i class="fa-solid fa-triangle-exclamation"></i>
                            </div>
                            <div>
                                <h4 class="m-0 text-red-800 font-bold text-sm">Sản phẩm đã hết hạn vụ thu hoạch</h4>
                                <p class="mt-1 text-red-950/85 text-xs">Sản phẩm hiện đang tạm dừng bán. Bạn có thể gửi yêu cầu nhập kho vụ mới để thông báo cho chủ cửa hàng.</p>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${hasRequestedToday}">
                                <button type="button" id="btn-request-restock" disabled class="bg-slate-300 text-slate-500 border-0 px-4 py-2 rounded-lg font-bold text-xs cursor-not-allowed inline-flex items-center gap-2 whitespace-nowrap">
                                    <i class="fa-solid fa-check"></i> Đã Yêu Cầu Hôm Nay
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" id="btn-request-restock" onclick="requestRestock()" class="bg-primary text-white border-0 px-4 py-2 rounded-lg font-bold text-xs cursor-pointer inline-flex items-center gap-2 transition-transform duration-200 hover:-translate-y-0.5 whitespace-nowrap">
                                    <i class="fa-solid fa-paper-plane"></i> Yêu Cầu Nhập Kho
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${isAdminPreview and (product.status == 'INACTIVE' or product.status == 'DELETED')}">
                    <div class="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 my-4 text-xs text-amber-900">
                        <strong class="font-bold">Admin preview:</strong>
                        Sản phẩm đang ở trạng thái <c:out value="${product.status}"/> nhưng vẫn hiển thị đầy đủ để kiểm tra nội bộ.
                    </div>
                </c:if>

                <!-- Origin -->
                <p class="text-sm text-[#44483B] mb-4 flex items-center">
                    <i class="fa-solid fa-location-dot mr-2 text-primary"></i>
                    Xuất xứ: <strong><c:out value="${product.originRegion}"/>, <c:out value="${product.originCountry}"/></strong>
                </p>

                <!-- Flash Sale badge if product promotion exists -->
                <c:if test="${not empty productPromotions}">
                    <c:set var="fp" value="${productPromotions[0]}"/>
                    <div class="flash-sale-badge">
                        <i class="fa-solid fa-bolt"></i>
                        Giảm giá sản phẩm:
                        <c:choose>
                            <c:when test="${fp.discountType == 'PERCENT'}">Giảm <c:out value="${fp.discountValue}"/>%</c:when>
                            <c:otherwise>Giảm <ft:currency value="${fp.discountValue}"/></c:otherwise>
                        </c:choose>
                        <span class="opacity-50">|</span> Còn lại <c:out value="${fp.maxUses - fp.usedCount}"/> lượt
                    </div>
                </c:if>

                <!-- Price Area -->
                <div class="price-area">
                    <span id="original-price-container" class="${not empty variants && variants[0].isDiscounted ? '' : 'hidden'}">
                        <span id="original-price" class="text-red-500 line-through text-base md:text-lg mr-2 font-semibold">
                            <c:if test="${not empty variants}">
                                <ft:currency value="${variants[0].price}"/>
                            </c:if>
                        </span>
                    </span>
                    <span class="price-main" id="displayed-price">
                        <c:choose>
                            <c:when test="${not empty variants}"><ft:currency value="${variants[0].activePrice}"/></c:when>
                            <c:otherwise>0 ₫</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="price-unit" id="displayed-unit">/ <c:out value="${not empty variants ? variants[0].variantLabel : 'đơn vị'}"/></span>
                </div>

                <!-- Product Description -->
                <div class="product-desc-box">
                    <p class="product-desc-text"><c:out value="${product.description}"/></p>
                </div>

                <hr class="border-t border-[#E2ECE7] my-4">

                <!-- Variant Selection -->
                <c:choose>
                    <c:when test="${not empty variants}">
                        <div class="variant-section">
                            <div class="section-sub-title">Chọn phân loại / đóng gói:</div>
                            <div class="variant-chips">
                                <c:forEach var="v" items="${variants}" varStatus="status">
                                    <input type="radio" name="product_variant"
                                           id="v_${v.variantId}"
                                           class="variant-chip-input"
                                           value="${v.variantId}"
                                           data-price="${v.price}"
                                           data-activeprice="${v.activePrice}"
                                           data-isdiscounted="${v.isDiscounted}"
                                           data-label="<c:out value='${v.variantLabel}'/>"
                                           data-stock="${v.stockQuantity}"
                                           ${status.index == 0 ? 'checked' : ''}
                                           onchange="onVariantChange(this)">
                                    <label for="v_${v.variantId}" class="variant-chip-label">
                                        <c:out value="${v.variantLabel}"/>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>

                        <!-- Packaging Options Selection -->
                        <c:if test="${not empty packagingOptions}">
                            <div class="variant-section my-5">
                                <div class="section-sub-title">Chọn quy cách đóng gói (Tùy chọn):</div>
                                <div class="mt-2">
                                    <select id="packaging-selector" onchange="calculateSubtotal()" class="w-full md:max-w-xs px-4 py-2.5 rounded-full border-2 border-gray-200 focus:border-primary focus:ring-0 text-sm font-semibold text-gray-700 bg-white transition-all shadow-sm">
                                        <option value="" data-price-add="0">Mặc định (Không thêm phụ phí)</option>
                                        <c:forEach var="po" items="${packagingOptions}">
                                            <option value="${po.packagingId}" data-price-add="${po.priceAdd}">
                                                <c:out value="${po.label}"/> (+<ft:currency value="${po.priceAdd}"/>)
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="text-sm text-danger font-semibold my-4">Không có phân loại khả dụng cho sản phẩm này.</div>
                    </c:otherwise>
                </c:choose>

                <!-- Stock Indicator -->
                <div class="stock-indicator" id="variant-stock-hint">
                    <c:choose>
                        <c:when test="${isExpiredProduct}">
                            <div class="flex items-center gap-2">
                                <i class="fa-solid fa-calendar-xmark text-red-500"></i>
                                Trạng thái: <strong class="text-red-700">Hết vụ thu hoạch</strong>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${not empty variants}">
                                <div class="flex items-center gap-2">
                                    <i class="fa-solid fa-boxes-stacked text-emerald-500"></i>
                                    Tồn kho: <strong id="stock-qty-val" class="text-emerald-800"><c:out value="${variants[0].stockQuantity}"/></strong> sản phẩm
                                </div>
                                <div class="stock-bar-bg">
                                    <div class="stock-bar-fill" id="stock-bar-fill-indicator" data-initial-stock="${variants[0].stockQuantity}"></div>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Subtotal display -->
                <c:if test="${not empty variants && product.status == 'ACTIVE' && !isOutOfSeason}">
                    <div class="flex items-center justify-between bg-emerald-50 border border-emerald-200 rounded-2xl px-5 py-3.5 my-4">
                        <span class="text-xs font-bold text-[#14532d] uppercase tracking-wider">Tạm tính (chưa gồm ship)</span>
                        <strong id="purchase-subtotal" class="text-xl font-extrabold text-[#166534]">0 ₫</strong>
                    </div>
                </c:if>

                <!-- Cart Action Row -->
                <c:if test="${not empty variants && product.status == 'ACTIVE'}">
                    <c:choose>
                        <c:when test="${isOutOfSeason}">
                            <div class="cart-action-row">
                                <button type="button" disabled class="btn-add-to-cart-large !bg-slate-300 !text-slate-500 !cursor-not-allowed !shadow-none">
                                    <i class="fa-solid fa-calendar-xmark"></i> Hết mùa vụ gieo trồng
                                </button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="cart-action-row" id="cart-action-controls">
                                <div class="qty-selector" id="purchase-qty-selector">
                                    <button type="button" class="qty-btn" onclick="adjustQuantity(-1)"><i class="fa-solid fa-minus"></i></button>
                                    <input type="number" id="purchase-qty" class="qty-input" value="1" min="1" readonly>
                                    <button type="button" class="qty-btn" onclick="adjustQuantity(1)"><i class="fa-solid fa-plus"></i></button>
                                </div>
                                <button type="button" id="btn-add-to-cart" class="btn-add-to-cart-large" onclick="handleAddToCart()">
                                    <i class="fa-solid fa-cart-arrow-down"></i> Thêm Vào Giỏ Hàng
                                </button>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
        </div>

        <!-- ③ SHOP PROFILE PANEL (below 2-column grid) -->
        <c:if test="${not empty shopProfile}">
            <div class="shop-profile-panel mb-6">
                
                <!-- Shop Header Band (green) -->
                <div class="shop-header-band">
                    <div class="shop-header-left">
                        <div class="shop-avatar-lg">
                            <i class="fa-solid fa-store"></i>
                        </div>
                        <div>
                            <div class="shop-name-hero"><c:out value="${shopProfile.shopName}"/></div>
                            <div class="shop-hero-sub">
                                <span class="shop-rating-stars-sm">
                                    <i class="fa-solid fa-star"></i>
                                    <c:out value="${shopProfile.rating}"/>/5
                                </span>
                                <span>•</span>
                                <span>Cửa hàng Đảm Bảo</span>
                                <c:if test="${not empty shopProfile.shopDescription}">
                                    <span>•</span>
                                    <span><c:out value="${shopProfile.shopDescription}"/></span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                    <div class="flex items-center gap-2">
                        <a href="${pageContext.request.contextPath}/shop-view?id=${shopProfile.userId}&idType=owner" class="btn-visit-shop-hero">
                            <i class="fa-solid fa-store"></i> Ghé Thăm Shop
                        </a>
                        <button type="button" id="btnChatWithShop" data-owner-id="${shopProfile.userId}" class="btn-visit-shop-hero !bg-white !text-primary !border-2 !border-primary !px-5 !py-2.5 !gap-1.5 hover:!bg-emerald-50">
                            <i class="fa-solid fa-comments"></i> Chat Với Shop
                        </button>
                    </div>
                </div>

                <!-- Shipping Info Section -->
                <div class="shop-body-section">
                    <div class="shop-section-label">
                        <i class="fa-solid fa-truck-fast"></i> Thông tin vận chuyển
                    </div>
                    <div class="shipping-info-grid">
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-bolt"></i></div>
                            <div>
                                <div class="shipping-pill-title">Giao Siêu Tốc</div>
                                <div class="shipping-pill-sub">Nhận hàng trong 2–3 tiếng</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-box-open"></i></div>
                            <div>
                                <div class="shipping-pill-title">Đóng Gói Cẩn Thận</div>
                                <div class="shipping-pill-sub">Hộp lạnh bảo quản tươi ngon</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-money-bill-wave"></i></div>
                            <div>
                                <div class="shipping-pill-title">Thanh Toán Khi Nhận</div>
                                <div class="shipping-pill-sub">Hỗ trợ COD toàn quốc</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-shield-halved"></i></div>
                            <div>
                                <div class="shipping-pill-title">Đảm Bảo Hoàn Tiền</div>
                                <div class="shipping-pill-sub">Đổi trả trong 24h nếu lỗi</div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Voucher Section — Horizontal Slider --%>
                <c:if test="${not empty productPromotions || not empty shopVouchers || not empty systemVouchers}">
                    <div class="shop-body-section">
                        <div class="shop-section-label">
                        <i class="fa-solid fa-ticket"></i> Ưu đãi: giảm giá sản phẩm, voucher shop, voucher sàn
                        </div>
                        <div class="voucher-slider-wrapper">
                            <div class="voucher-slider-nav">
                                <button class="voucher-nav-btn" id="voucher-prev" onclick="slideVoucher(-1)" aria-label="Trước">
                                    <i class="fa-solid fa-chevron-left"></i>
                                </button>
                                <div class="voucher-dots" id="voucher-dots"></div>
                                <button class="voucher-nav-btn" id="voucher-next" onclick="slideVoucher(1)" aria-label="Tiếp">
                                    <i class="fa-solid fa-chevron-right"></i>
                                </button>
                            </div>
                            <div class="voucher-track-container">
                                <div class="voucher-track" id="voucher-track">

                                    <%-- Giảm giá sản phẩm cho sản phẩm --%>
                                    <c:forEach var="pv" items="${productPromotions}">
                                        <div class="voucher-item type-product">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-bolt"></i></span>
                                                SALE
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${pv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${pv.discountType == 'PERCENT'}">Giảm <c:out value="${pv.discountValue}"/>% sản phẩm này</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${pv.discountValue}"/> cho sản phẩm</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${pv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${pv.minOrderValue}"/></c:if>
                                                </div>
                                                <div class="voucher-expire">Còn <c:out value="${pv.maxUses - pv.usedCount}"/> lượt dùng</div>
                                            </div>
                                            <button class="voucher-copy-btn" data-code="${pv.code}" onclick="copyVoucher(this)">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                    <%-- Voucher shop --%>
                                    <c:forEach var="sv" items="${shopVouchers}">
                                        <div class="voucher-item type-shop">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-store"></i></span>
                                                SHOP
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${sv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${sv.discountType == 'PERCENT'}">Giảm <c:out value="${sv.discountValue}"/>% đơn hàng</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${sv.discountValue}"/> cho đơn hàng</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${sv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${sv.minOrderValue}"/></c:if>
                                                </div>
                                            </div>
                                            <button class="voucher-copy-btn" data-code="${sv.code}" onclick="copyVoucher(this)">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                    <%-- Voucher sàn --%>
                                    <c:forEach var="syv" items="${systemVouchers}">
                                        <div class="voucher-item type-system">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-leaf"></i></span>
                                                SÀN
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${syv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${syv.discountType == 'PERCENT'}">Giảm <c:out value="${syv.discountValue}"/>% toàn sàn</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${syv.discountValue}"/> đơn hàng</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${syv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${syv.minOrderValue}"/></c:if>
                                                </div>
                                            </div>
                                            <button class="voucher-copy-btn" data-code="${syv.code}" onclick="copyVoucher(this)">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- More from this shop -->
                <c:if test="${not empty shopOtherProducts}">
                    <div class="shop-body-section">
                        <div class="shop-section-label justify-between w-full">
                            <span><i class="fa-solid fa-layer-group"></i> Xem thêm sản phẩm từ cửa hàng này</span>
                            <a href="${pageContext.request.contextPath}/shop-view?id=${shopProfile.profileId}&idType=profile"
                               class="text-[10px] text-primary font-bold normal-case tracking-normal no-underline">
                                Xem tất cả <i class="fa-solid fa-arrow-right ml-1"></i>
                            </a>
                        </div>
                        <div class="shop-products-slider" id="shop-slider">
                            <c:forEach var="sp" items="${shopOtherProducts}">
                                <a href="${pageContext.request.contextPath}/products/detail?id=${sp.productId}"
                                   class="shop-product-mini no-underline text-current">
                                    <img src="${sp.image}" class="shop-product-mini-img" alt="${sp.name}" onerror="handleImageError(this)">
                                    <div class="shop-product-mini-info">
                                        <div class="shop-product-mini-name"><c:out value="${sp.name}"/></div>
                                        <div class="shop-product-mini-price"><ft:currency value="${sp.price}"/></div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

            </div>
        </c:if>

        <!-- ④ Product Specifications Panel -->
        <div class="premium-panel mb-6">
            <h2 class="font-headline-lg text-[#00210D] font-bold text-xl mb-2 flex items-center">
                <i class="fa-solid fa-circle-info mr-2 text-primary"></i>
                Thông số kỹ thuật &amp; Bảo quản
            </h2>
            <table class="spec-table">
                <tbody>
                    <tr>
                        <th>Hạn dùng (Kể từ ngày thu hoạch)</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty product.shelfLifeDays}"><c:out value="${product.shelfLifeDays}"/> ngày</c:when>
                                <c:otherwise>Xem trên bao bì</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Ngày thu hoạch</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty product.formattedHarvestDate}"><c:out value="${product.formattedHarvestDate}"/></c:when>
                                <c:otherwise>Xem trên bao bì</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Quốc gia xuất khẩu</th>
                        <td><c:out value="${product.originCountry}"/></td>
                    </tr>
                    <tr>
                        <th>Hướng dẫn bảo quản</th>
                        <td><c:out value="${product.storageInstruction}"/></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- ⑤ Reviews Section -->
        <div class="premium-panel mb-6" id="reviews">
            <h2 class="font-headline-lg text-[#00210D] font-bold text-2xl mb-6">
                Đánh giá từ khách hàng
            </h2>

            <div class="review-grid mb-8">
                <!-- Rating Score Summary -->
                <div class="rating-summary-box">
                    <c:choose>
                        <c:when test="${hasReviews}">
                            <div class="big-score"><c:out value="${product.rating}"/></div>
                            <div class="my-2"><ft:stars rating="${product.rating}"/></div>
                            <div class="text-sm text-muted"><c:out value="${totalReviewsCount}"/> đánh giá thực tế</div>
                        </c:when>
                        <c:otherwise>
                            <div class="big-score !text-slate-400">--</div>
                            <div class="my-2"><ft:stars rating="${product.rating}"/></div>
                            <div class="text-sm text-muted">Chưa có đánh giá</div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Distribution Bars -->
                <div class="flex flex-col justify-center">
                    <c:forEach var="star" begin="1" end="5">
                        <c:set var="starIndex" value="${6 - star}"/>
                        <c:set var="starCount" value="${ratingCounts[starIndex]}"/>
                        <c:set var="starPercent" value="${totalReviewsCount > 0 ? (starCount * 100 / totalReviewsCount) : 0}"/>
                        <div class="rating-bar-row">
                            <div class="bar-stars">${starIndex} <i class="fa-solid fa-star text-[#F59E0B]"></i></div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" data-percent="${starPercent}"></div>
                            </div>
                            <div class="bar-count-percent">
                                <fmt:formatNumber value="${starPercent}" maxFractionDigits="0"/>% (${starCount})
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- Star Filter Tabs -->
            <div class="review-filters-row">
                <a href="${pageContext.request.contextPath}/products/detail?id=${product.productId}#reviews"
                   class="filter-tab-btn ${ratingFilter == null ? 'active' : ''}">
                    Tất cả (${totalReviewsCount})
                </a>
                <c:forEach var="starIndex" begin="1" end="5">
                    <c:set var="sVal" value="${6 - starIndex}"/>
                    <c:set var="sCount" value="${ratingCounts[sVal]}"/>
                    <a href="${pageContext.request.contextPath}/products/detail?id=${product.productId}&amp;rating=${sVal}#reviews"
                       class="filter-tab-btn ${ratingFilter == sVal ? 'active' : ''}">
                        ${sVal} Sao (${sCount})
                    </a>
                </c:forEach>
            </div>

            <!-- Paginated Review List -->
            <div class="reviews-list-wrapper">
                <c:choose>
                    <c:when test="${not empty reviewPagedResult.items}">
                        <c:forEach var="r" items="${reviewPagedResult.items}">
                            <div class="review-card-item">
                                <div class="review-card-header">
                                    <div class="reviewer-meta">
                                        <div class="reviewer-avatar">
                                            <c:out value="${fn:substring(r.customerName, 0, 1)}"/>
                                        </div>
                                        <div>
                                            <div class="reviewer-name"><c:out value="${r.customerName}"/></div>
                                            <div class="flex items-center gap-2">
                                                <ft:stars rating="${r.rating}"/>
                                                <span class="review-date"><c:out value="${r.createdAt}"/></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="review-body-text">
                                    <c:out value="${r.reviewText}"/>
                                </div>
                                <c:if test="${not empty r.reviewImageUrl}">
                                    <div class="review-attachment-box">
                                        <div class="review-thumb-image" onclick="openPhotoModal(this)">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(r.reviewImageUrl, 'http://') || fn:startsWith(r.reviewImageUrl, 'https://') || fn:startsWith(r.reviewImageUrl, 'data:')}">
                                                    <img src="<c:out value='${r.reviewImageUrl}'/>" alt="Ảnh review khách hàng">
                                                </c:when>
                                                <c:when test="${fn:startsWith(r.reviewImageUrl, pageContext.request.contextPath)}">
                                                    <img src="<c:out value='${r.reviewImageUrl}'/>" alt="Ảnh review khách hàng">
                                                </c:when>
                                                <c:when test="${fn:startsWith(r.reviewImageUrl, '/')}">
                                                    <img src="${pageContext.request.contextPath}<c:out value='${r.reviewImageUrl}'/>" alt="Ảnh review khách hàng">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/<c:out value='${r.reviewImageUrl}'/>" alt="Ảnh review khách hàng">
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>

                        <!-- Pagination -->
                        <div class="mt-6">
                            <c:set var="ratingQueryParam" value="${ratingFilter != null ? '&amp;rating=' : ''}${ratingFilter != null ? ratingFilter : ''}"/>
                            <ft:pagination current="${reviewPagedResult.currentPage}"
                                           total="${reviewPagedResult.totalPages}"
                                           baseUrl="${pageContext.request.contextPath}/products/detail?id=${product.productId}${ratingQueryParam}"/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center text-[#8E9285] py-8 italic font-semibold">
                            Chưa có lượt đánh giá nào phù hợp với bộ lọc đã chọn.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- ⑥ Similar Products Slider (below reviews) -->
        <c:if test="${not empty similarProducts}">
            <div class="similar-section">
                <div class="carousel-header-row">
                    <h2 class="font-headline-lg text-[#00210D] font-bold text-2xl">
                        Sản Phẩm Tương Tự
                    </h2>
                    <div class="carousel-arrows">
                        <button class="arrow-btn" onclick="slideCarousel(-1)"><i class="fa-solid fa-chevron-left"></i></button>
                        <button class="arrow-btn" onclick="slideCarousel(1)"><i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                </div>
                
                <div class="slider-track" id="similar-slider">
                    <c:forEach var="p" items="${similarProducts}">
                        <div class="slider-item">
                            <div class="product-card mb-1">
                                <a href="${pageContext.request.contextPath}/products/detail?id=${p.productId}" class="product-card-link no-underline text-current">
                                    <img src="${p.image}"
                                         id="similar_img_${p.productId}" onerror="handleImageError(this)"
                                         class="object-cover w-full h-[180px] rounded-t-xl" alt="<c:out value='${p.name}'/>">
                                    <h3 class="font-bold text-sm px-3 pt-3 line-clamp-1"><c:out value="${p.name}"/></h3>
                                    <div class="product-card__price px-3 py-1 font-bold text-primary">
                                        Giá chỉ từ <ft:currency value="${p.price}"/>
                                    </div>
                                    <div class="product-card__rating px-3 pb-1">
                                        <ft:stars rating="${p.rating}"/>
                                    </div>
                                    <div class="product-card__shop px-3 pb-3 text-xs text-muted flex items-center justify-between">
                                        <span>Nguồn: <c:out value="${p.originRegion}"/></span>
                                        <span class="bg-emerald-100 text-primary text-[9px] font-semibold px-2 py-0.5 rounded-full">/ <c:out value="${p.unit}"/></span>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

    </div>
</div>

<!-- Photo Zoom Modal -->
<div id="photo-viewer-modal" class="premium-modal" onclick="closePhotoModal()">
    <div class="modal-content-box" onclick="event.stopPropagation()">
        <button class="modal-close-btn" onclick="closePhotoModal()"><i class="fa-solid fa-xmark"></i></button>
        <img id="modal-expanded-img" src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7" alt="Expanded review photo">
    </div>
</div>

<!-- Cart Success Toast -->
<div id="cart-added-toast" class="premium-toast">
    <span class="premium-toast-icon"><i class="fa-solid fa-circle-check"></i></span>
    <div>
        <strong class="block">Thành công!</strong>
        <span class="text-xs">Sản phẩm đã được thêm vào giỏ hàng.</span>
    </div>
</div>

<!-- Voucher Copy Toast -->
<div id="copy-toast" class="copy-toast">
    <i class="fa-solid fa-copy mr-2"></i> Đã sao chép mã: <strong id="copy-toast-code"></strong>
</div>

<c:set var="firstVariantPrice" value="${not empty variants ? variants[0].price : 0}" />
<c:set var="escapedProductName" value="${fn:escapeXml(product.name)}" />
<script>
    // Define server-side variables safely in JS scope
    const currentProductId = parseInt('${product.productId}') || 0;
    const csrfToken = '${sessionScope._csrfToken}';

    // Add to recently viewed in localStorage
    try {
        const productInfo = {
            id: currentProductId,
            name: '${escapedProductName}',
            price: '${firstVariantPrice}',
            image: document.getElementById('main-product-img')?.src || ''
        };
        if (productInfo.id > 0) {
            let recentlyViewed = JSON.parse(localStorage.getItem('recentlyViewed') || '[]');
            recentlyViewed = recentlyViewed.filter(item => item.id !== productInfo.id);
            recentlyViewed.unshift(productInfo);
            recentlyViewed = recentlyViewed.slice(0, 5);
            localStorage.setItem('recentlyViewed', JSON.stringify(recentlyViewed));
        }
    } catch(e) {
        console.error("Error storing recently viewed", e);
    }

    // Image fallback
    window.handleImageError = function(img) {
        if (!img.dataset.errorStage) {
            img.dataset.errorStage = "1";
            img.src = "https://images.unsplash.com/photo-1610832958506-ee5633619144?w=600&auto=format&fit=crop&q=80";
        } else if (img.dataset.errorStage === "1") {
            img.dataset.errorStage = "2";
            img.src = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MDAgMzAwIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjRmYmY3IiByeD0iMTYiLz48dGV4dCB4PSIyMDAiIHk9IjE2MCIgZm9udC1mYW1pbHk9InN5c3RlbS11aSxzYW5zLXNlcmlmIiBmb250LXNpemU9IjE2IiBmaWxsPSIjNGQ2NjFjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj5IxINuaCDhuqNuaDwvdGV4dD48L3N2Zz4=";
        }
    };

    // 1. Switch main product image from thumbnail
    function switchProductImage(element, src) {
        document.querySelectorAll('.thumbnail-item').forEach(el => el.classList.remove('active'));
        const mainImg = document.getElementById('main-product-img');
        if (mainImg) mainImg.src = src;
        element.classList.add('active');
    }

    // 2. Helper to update Out of Stock UI elements
    function updateStockUI(stock) {
        const badge = document.getElementById('variant-status-badge');
        const btnAdd = document.getElementById('btn-add-to-cart');
        const qtySelector = document.getElementById('purchase-qty-selector');

        const isExpired = '${isExpiredProduct}' === 'true';
        if (isExpired) {
            if (badge) {
                badge.className = 'badge-stock badge-outstock';
                badge.style.background = '#fecaca';
                badge.style.color = '#b91c1c';
                badge.innerHTML = '<i class="fa-solid fa-clock-rotate-left mr-1"></i> Hết vụ thu hoạch';
            }
            if (btnAdd) {
                btnAdd.classList.add('opacity-50', 'pointer-events-none', 'cursor-not-allowed');
                btnAdd.setAttribute('disabled', 'true');
                btnAdd.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> Hết hạn vụ';
            }
            if (qtySelector) {
                qtySelector.classList.add('opacity-50', 'pointer-events-none');
            }
            return;
        }

        if (stock <= 0) {
            // Update badge to Out of Stock
            if (badge) {
                badge.className = 'badge-stock badge-outstock';
                badge.innerHTML = '<i class="fa-solid fa-circle-xmark mr-1"></i> Hết hàng';
            }
            // Disable and dim the Add to Cart button
            if (btnAdd) {
                btnAdd.classList.add('opacity-50', 'pointer-events-none', 'cursor-not-allowed');
                btnAdd.setAttribute('disabled', 'true');
                btnAdd.innerHTML = '<i class="fa-solid fa-circle-xmark"></i> Hết Hàng';
            }
            // Dim quantity selector controls
            if (qtySelector) {
                qtySelector.classList.add('opacity-50', 'pointer-events-none');
            }
        } else {
            // Update badge to In Stock
            if (badge) {
                badge.className = 'badge-stock badge-instock';
                badge.innerHTML = '<i class="fa-solid fa-circle-check mr-1"></i> Còn hàng';
            }
            // Enable and restore the Add to Cart button
            if (btnAdd) {
                btnAdd.classList.remove('opacity-50', 'pointer-events-none', 'cursor-not-allowed');
                btnAdd.removeAttribute('disabled');
                btnAdd.innerHTML = '<i class="fa-solid fa-cart-arrow-down"></i> Thêm Vào Giỏ Hàng';
            }
            // Restore quantity selector controls
            if (qtySelector) {
                qtySelector.classList.remove('opacity-50', 'pointer-events-none');
            }
        }
    }

    // 2a. Dynamic Stock indicator progress updater
    function updateStockIndicator(stock) {
        const isExpired = '${isExpiredProduct}' === 'true';
        if (isExpired) return;

        const qtyVal = document.getElementById('stock-qty-val');
        if (qtyVal) qtyVal.textContent = stock;
        const fillBar = document.getElementById('stock-bar-fill-indicator');
        if (fillBar) {
            const fillPercent = (stock * 100) / 200;
            fillBar.style.transform = 'scaleX(' + (Math.min(100, fillPercent) / 100) + ')';
        }
    }

    // 2b. Variant change — update price, unit, stock, and check stock-out states
    function onVariantChange(radioElement) {
        const price = parseFloat(radioElement.getAttribute('data-price'));
        const activePrice = parseFloat(radioElement.getAttribute('data-activeprice')) || price;
        const isDiscounted = radioElement.getAttribute('data-isdiscounted') === 'true';
        const label = radioElement.getAttribute('data-label');
        const stock = parseInt(radioElement.getAttribute('data-stock'));

        const priceDisplay = document.getElementById('displayed-price');
        if (priceDisplay) {
            priceDisplay.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(activePrice);
        }

        const origPriceContainer = document.getElementById('original-price-container');
        const origPriceDisplay = document.getElementById('original-price');
        if (origPriceContainer && origPriceDisplay) {
            if (isDiscounted) {
                origPriceDisplay.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
                origPriceContainer.classList.remove('hidden');
            } else {
                origPriceContainer.classList.add('hidden');
            }
        }

        const unitDisplay = document.getElementById('displayed-unit');
        if (unitDisplay) unitDisplay.textContent = ' / ' + label;

        // Dynamic stock indicator update
        updateStockIndicator(stock);

        const qtyInput = document.getElementById('purchase-qty');
        if (qtyInput) qtyInput.value = 1;

        // Apply Out of Stock UI check
        updateStockUI(stock);

        // Recalculate subtotal
        calculateSubtotal();
    }

    // 3. Quantity adjustment
    function adjustQuantity(delta) {
        const qtyInput = document.getElementById('purchase-qty');
        if (!qtyInput) return;
        let currentQty = parseInt(qtyInput.value) || 1;
        currentQty += delta;
        let maxStock = 99;
        const checkedVariant = document.querySelector('input[name="product_variant"]:checked');
        if (checkedVariant) maxStock = parseInt(checkedVariant.getAttribute('data-stock')) || 0;
        if (currentQty < 1) currentQty = 1;
        if (currentQty > maxStock) {
            currentQty = maxStock;
            alert('Số lượng yêu cầu vượt quá tồn kho khả dụng (' + maxStock + ')!');
        }
        qtyInput.value = currentQty;
        calculateSubtotal();
    }

    // 3b. Calculate and display subtotal (active price + packaging)
    function calculateSubtotal() {
        const checkedVariant = document.querySelector('input[name="product_variant"]:checked');
        if (!checkedVariant) return;

        const activePrice = parseFloat(checkedVariant.getAttribute('data-activeprice')) || 0;
        
        const packagingSelect = document.getElementById('packaging-selector');
        let packagingPriceAdd = 0;
        if (packagingSelect) {
            const selectedOpt = packagingSelect.options[packagingSelect.selectedIndex];
            packagingPriceAdd = parseFloat(selectedOpt.getAttribute('data-price-add')) || 0;
        }

        const qtyInput = document.getElementById('purchase-qty');
        const quantity = parseInt(qtyInput ? qtyInput.value : 1) || 1;

        const subtotal = (activePrice + packagingPriceAdd) * quantity;
        
        const subtotalDisplay = document.getElementById('purchase-subtotal');
        if (subtotalDisplay) {
            subtotalDisplay.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(subtotal);
        }
    }

    // 4. Add to cart (Ajax Premium Unified Helper)
    async function handleAddToCart() {
        const checkedVariant = document.querySelector('input[name="product_variant"]:checked');
        if (!checkedVariant) { alert('Vui lòng chọn một phân loại sản phẩm.'); return; }

        const variantId = parseInt(checkedVariant.value);
        const qtyInput = document.getElementById('purchase-qty');
        const quantity = parseInt(qtyInput ? qtyInput.value : 1);
        
        const packagingSelect = document.getElementById('packaging-selector');
        let packagingId = null;
        let packagingPriceAdd = 0;
        let packagingLabel = '';
        if (packagingSelect && packagingSelect.value) {
            packagingId = parseInt(packagingSelect.value);
            const selectedOpt = packagingSelect.options[packagingSelect.selectedIndex];
            packagingPriceAdd = parseFloat(selectedOpt.getAttribute('data-price-add')) || 0;
            packagingLabel = selectedOpt.textContent.trim().split(' (+')[0];
        }

        let name = "<c:out value='${product.name}'/> - " + checkedVariant.getAttribute('data-label');
        if (packagingLabel) {
            name += " (" + packagingLabel + ")";
        }

        const activePrice = parseFloat(checkedVariant.getAttribute('data-activeprice')) || parseFloat(checkedVariant.getAttribute('data-price'));
        const totalPricePerItem = activePrice + packagingPriceAdd;

        const stockQuantity = parseInt(checkedVariant.getAttribute('data-stock')) || 0;

        // CRITICAL: Block out-of-stock actions and display Red Alert
        if (stockQuantity <= 0) {
            Swal.fire({
                icon: 'error',
                title: 'Hết hàng!',
                text: 'Sản phẩm này hiện tại đã hết hàng, không thể thêm vào giỏ hàng.',
                confirmButtonText: 'Đồng ý',
                confirmButtonColor: '#B71C1C',
                background: '#ffffff'
            });
            return;
        }

        let imagePath = 'assets/img/placeholder.png';
        const mainImg = document.getElementById('main-product-img');
        if (mainImg) {
            const srcUrl = mainImg.src;
            const contextIdx = srcUrl.indexOf('/Ban_Hoa_Qua_Online/');
            if (contextIdx >= 0) imagePath = srcUrl.substring(contextIdx + '/Ban_Hoa_Qua_Online/'.length);
        }

        if (window.addCartItem) {
            await window.addCartItem(variantId, quantity, name, totalPricePerItem, imagePath, stockQuantity, currentProductId, packagingId);
        } else {
            console.warn('window.addCartItem not defined globally, falling back to Local Storage only.');
            if (typeof GuestCart !== 'undefined') {
                GuestCart.add({ variantId, name, price: totalPricePerItem, quantity, imagePath, packagingId });
                showSuccessToast();
            } else {
                alert('Thêm vào giỏ hàng thành công!');
            }
        }
    }

    // 5. Show cart success toast
    function showSuccessToast() {
        const toast = document.getElementById('cart-added-toast');
        if (toast) {
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 3000);
        }
    }

    // 5b. Request restock AJAX helper
    async function requestRestock() {
        const isUserLoggedIn = '${not empty sessionScope.currentUser}' === 'true';
        if (!isUserLoggedIn) {
            Swal.fire({
                icon: 'warning',
                title: 'Yêu cầu đăng nhập',
                text: 'Bạn cần đăng nhập để thực hiện gửi yêu cầu nhập kho.',
                confirmButtonText: 'Đăng nhập ngay',
                showCancelButton: true,
                cancelButtonText: 'Đóng',
                confirmButtonColor: 'var(--color-primary)',
                cancelButtonColor: '#ef4444'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '${pageContext.request.contextPath}/login';
                }
            });
            return;
        }

        const btn = document.getElementById('btn-request-restock');
        if (!btn) return;
        
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang gửi...';
        
        try {
            const resp = await fetch('${pageContext.request.contextPath}/products/detail?id=' + currentProductId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-Token': csrfToken
                }
            });
            const data = await resp.json();
            if (data.success) {
                const msg = (data.data && data.data.message) || 'Yêu cầu nhập kho vụ mới đã được gửi tới chủ cửa hàng.';
                Swal.fire({
                    icon: 'success',
                    title: 'Gửi yêu cầu thành công!',
                    text: msg,
                    confirmButtonColor: 'var(--color-primary)'
                });
                btn.innerHTML = '<i class="fa-solid fa-check"></i> Đã Yêu Cầu Hôm Nay';
                btn.style.background = '#cbd5e1'; // gray out
                btn.style.color = '#64748b';
                btn.style.cursor = 'not-allowed';
                btn.onclick = null;
            } else {
                const errMsg = data.error || 'Không thể gửi yêu cầu nhập kho.';
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi',
                    text: errMsg,
                    confirmButtonColor: '#ef4444'
                });
                if (errMsg && errMsg.includes("đăng nhập")) {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Yêu Cầu Nhập Kho';
                } else if (errMsg && errMsg.includes("đã gửi")) {
                    btn.innerHTML = '<i class="fa-solid fa-check"></i> Đã Yêu Cầu Hôm Nay';
                    btn.style.background = '#cbd5e1'; // gray out
                    btn.style.color = '#64748b';
                    btn.style.cursor = 'not-allowed';
                    btn.onclick = null;
                } else {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Yêu Cầu Nhập Kho';
                }
            }
        } catch (err) {
            console.error(err);
            Swal.fire({
                icon: 'error',
                title: 'Lỗi kết nối',
                text: 'Không thể kết nối máy chủ để gửi yêu cầu.',
                confirmButtonColor: '#ef4444'
            });
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Yêu Cầu Nhập Kho';
        }
    }

    // 6. Similar products carousel scroll
    function slideCarousel(direction) {
        const track = document.getElementById('similar-slider');
        if (track) track.scrollLeft += direction * 280;
    }

    // 6b. Voucher horizontal slider
    (function() {
        let voucherPage = 0;
        const ITEMS_PER_PAGE = 2;

        function initVoucherSlider() {
            const track = document.getElementById('voucher-track');
            const dotsEl = document.getElementById('voucher-dots');
            if (!track || !dotsEl) return;

            const items = track.querySelectorAll('.voucher-item');
            if (items.length === 0) return;

            const totalPages = Math.ceil(items.length / ITEMS_PER_PAGE);

            // Build dots
            dotsEl.innerHTML = '';
            for (let i = 0; i < totalPages; i++) {
                const dot = document.createElement('span');
                dot.className = 'voucher-dot' + (i === 0 ? ' active' : '');
                dot.addEventListener('click', () => goToVoucherPage(i));
                dotsEl.appendChild(dot);
            }

            updateVoucherSlider(totalPages);
        }

        function updateVoucherSlider(totalPages) {
            const track = document.getElementById('voucher-track');
            const dotsEl = document.getElementById('voucher-dots');
            const prevBtn = document.getElementById('voucher-prev');
            const nextBtn = document.getElementById('voucher-next');
            if (!track) return;

            const items = track.querySelectorAll('.voucher-item');
            const tp = totalPages || Math.ceil(items.length / ITEMS_PER_PAGE);

            // Calculate item width dynamically
            const containerWidth = track.parentElement.offsetWidth;
            const gap = 12;
            const itemW = (containerWidth - gap) / ITEMS_PER_PAGE;
            const offset = voucherPage * (itemW + gap) * ITEMS_PER_PAGE;
            track.style.transform = 'translateX(-' + offset + 'px)';

            // Update dots
            if (dotsEl) {
                dotsEl.querySelectorAll('.voucher-dot').forEach((d, i) => {
                    d.classList.toggle('active', i === voucherPage);
                });
            }

            if (prevBtn) prevBtn.disabled = voucherPage === 0;
            if (nextBtn) nextBtn.disabled = voucherPage >= tp - 1;
        }

        function goToVoucherPage(page) {
            const track = document.getElementById('voucher-track');
            if (!track) return;
            const totalPages = Math.ceil(track.querySelectorAll('.voucher-item').length / ITEMS_PER_PAGE);
            voucherPage = Math.max(0, Math.min(page, totalPages - 1));
            updateVoucherSlider(totalPages);
        }

        window.slideVoucher = function(direction) {
            const track = document.getElementById('voucher-track');
            if (!track) return;
            const totalPages = Math.ceil(track.querySelectorAll('.voucher-item').length / ITEMS_PER_PAGE);
            goToVoucherPage(voucherPage + direction);
        };

        document.addEventListener('DOMContentLoaded', initVoucherSlider);
    })();

    // 7. Open photo zoom modal
    function openPhotoModal(thumbElement) {
        const img = thumbElement.querySelector('img');
        if (!img) return;
        const modal = document.getElementById('photo-viewer-modal');
        const modalImg = document.getElementById('modal-expanded-img');
        if (modal && modalImg) {
            modalImg.src = img.src;
            modal.classList.add('show');
        }
    }

    // 8. Close photo zoom modal
    function closePhotoModal() {
        const modal = document.getElementById('photo-viewer-modal');
        if (modal) modal.classList.remove('show');
    }

    // 9. Copy voucher code
    function copyVoucher(btn) {
        const code = btn.getAttribute('data-code');
        if (navigator.clipboard) {
            navigator.clipboard.writeText(code).catch(() => {});
        } else {
            const tmp = document.createElement('input');
            tmp.value = code;
            document.body.appendChild(tmp);
            tmp.select();
            document.execCommand('copy');
            document.body.removeChild(tmp);
        }
        const toast = document.getElementById('copy-toast');
        const codeEl = document.getElementById('copy-toast-code');
        if (toast && codeEl) {
            codeEl.textContent = code;
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 2500);
        }
        const origText = btn.textContent;
        btn.textContent = '✓ ĐÃ SAO CHÉP';
        setTimeout(() => btn.textContent = origText, 2000);
    }

    window.openPhotoModal = openPhotoModal;
    window.slideCarousel = slideCarousel;
    window.copyVoucher = copyVoucher;

    // 10. Auto-update similar product images from seeded Unsplash fallback map
    document.addEventListener('DOMContentLoaded', () => {
        // Initialize star rating progress bars style
        document.querySelectorAll('.progress-bar-fill').forEach(el => {
            const pct = el.getAttribute('data-percent');
            if (pct) el.style.transform = 'scaleX(' + (parseFloat(pct) / 100) + ')';
        });

        const fruitImages = {
            1: 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600&auto=format&fit=crop&q=80',
            2: 'https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&auto=format&fit=crop&q=80',
            3: 'https://images.unsplash.com/photo-1571772996211-2f02c9727629?w=600&auto=format&fit=crop&q=80',
            4: 'https://images.unsplash.com/photo-1553279768-865429fa0078?w=600&auto=format&fit=crop&q=80',
            5: 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&auto=format&fit=crop&q=80',
            6: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80',
            7: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80',
            8: 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600&auto=format&fit=crop&q=80',
            9: 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600&auto=format&fit=crop&q=80',
            10: 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=600&auto=format&fit=crop&q=80'
        };
        Object.keys(fruitImages).forEach(id => {
            const imgEl = document.getElementById('similar_img_' + id);
            if (imgEl) imgEl.src = fruitImages[id];
        });

        // Initialize displayed-unit from first checked variant
        const firstVariant = document.querySelector('input[name="product_variant"]:checked');
        if (firstVariant) {
            const label = firstVariant.getAttribute('data-label');
            const unitEl = document.getElementById('displayed-unit');
            if (unitEl && label) unitEl.textContent = ' / ' + label;
            
            // Apply initial stock-out UI check
            const initialStock = parseInt(firstVariant.getAttribute('data-stock')) || 0;
            updateStockUI(initialStock);
            updateStockIndicator(initialStock);
            calculateSubtotal();
        } else {
            const fillIndicator = document.getElementById('stock-bar-fill-indicator');
            if (fillIndicator) {
                const initialStock = parseInt(fillIndicator.getAttribute('data-initial-stock')) || 0;
                updateStockIndicator(initialStock);
            }
        }

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
                        alert(data.error || data.message || 'Vui lòng đăng nhập tài khoản khách hàng để nhắn tin với cửa hàng.');
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('Lỗi kết nối máy chủ.');
                });
            });
        }

        // Lắng nghe sự kiện click các tab lọc rating
        const filterButtons = document.querySelectorAll('.review-filters-row a');
        filterButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                filterButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                
                const url = new URL(btn.href);
                const ratingVal = url.searchParams.get('rating');
                currentRatingFilter = ratingVal ? parseInt(ratingVal) : null;
                currentReviewPage = 1;
                
                loadReviewsRealtime();
            });
        });
    });

    function escapeHtml(value) {
        if (value === null || value === undefined) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    const reviewContextPath = '${pageContext.request.contextPath}';
    const reviewProductId = '${product.productId}';

    let currentReviewPage = 1;
    let currentRatingFilter = null;

    function resolveReviewImageSrc(rawSrc) {
        if (!rawSrc) return '';
        const src = String(rawSrc).trim();
        if (/^(https?:\/\/|data:)/i.test(src)) return src;
        if (src.startsWith(reviewContextPath + '/')) return src;
        if (src.startsWith('/')) return reviewContextPath + src;
        return reviewContextPath + '/' + src;
    }

    async function loadReviewsRealtime() {
        const wrapper = document.querySelector('.reviews-list-wrapper');
        if (!wrapper) return;
        
        wrapper.innerHTML =
            '<div class="text-center py-12">' +
                '<i class="fa-solid fa-spinner fa-spin text-3xl text-[#4d661c] mb-3"></i>' +
                '<p class="text-sm text-muted">Đang tải đánh giá thực tế...</p>' +
            '</div>';
        
        try {
            let url = reviewContextPath + '/products/detail?id=' + reviewProductId + '&format=json&action=getReviews&page=' + currentReviewPage;
            if (currentRatingFilter !== null) {
                url += '&rating=' + currentRatingFilter;
            }
            
            const response = await fetch(url);
            const result = await response.json();
            
            if (result.success && result.data) {
                renderReviewsList(result.data);
            } else {
                wrapper.innerHTML = '<div class="text-center text-red-500 py-8">Không thể tải dữ liệu đánh giá.</div>';
            }
        } catch (err) {
            console.error("Lỗi tải review realtime:", err);
            wrapper.innerHTML = '<div class="text-center text-red-500 py-8">Lỗi kết nối khi tải đánh giá.</div>';
        }
    }

    function renderReviewsList(pagedResult) {
        const wrapper = document.querySelector('.reviews-list-wrapper');
        if (!wrapper) return;
        
        if (!pagedResult.items || pagedResult.items.length === 0) {
            wrapper.innerHTML = '<div class="text-center text-[#8E9285] py-8 italic font-semibold">Chưa có lượt đánh giá nào phù hợp với bộ lọc đã chọn.</div>';
            return;
        }
        
        let html = '';
        pagedResult.items.forEach(r => {
            let starsHtml = '';
            for (let i = 1; i <= 5; i++) {
                if (i <= r.rating) {
                    starsHtml += '<i class="fa-solid fa-star text-amber-400 mr-0.5"></i>';
                } else {
                    starsHtml += '<i class="fa-solid fa-star text-gray-200 mr-0.5"></i>';
                }
            }
            
            let imgHtml = '';
            if (r.reviewImageUrl) {
                const src = resolveReviewImageSrc(r.reviewImageUrl);
                imgHtml =
                    '<div class="review-attachment-box">' +
                        '<div class="review-thumb-image" onclick="openPhotoModal(this)">' +
                            '<img src="' + escapeHtml(src || '') + '" alt="Ảnh review khách hàng">' +
                        '</div>' +
                    '</div>';
            }
            
            const safeCustomerName = escapeHtml(r.customerName || 'Người dùng');
            const safeReviewText = escapeHtml(r.reviewText || '').replace(/\r?\n/g, '<br>');
            const safeCreatedAt = escapeHtml(r.createdAt || '');
            const avatarChar = escapeHtml(r.customerName ? r.customerName.substring(0, 1).toUpperCase() : 'U');
            
            html +=
                '<div class="review-card-item">' +
                    '<div class="review-card-header">' +
                        '<div class="reviewer-meta">' +
                            '<div class="reviewer-avatar">' +
                                avatarChar +
                            '</div>' +
                            '<div>' +
                                '<div class="reviewer-name">' + safeCustomerName + '</div>' +
                                '<div class="flex items-center gap-2">' +
                                    '<div class="flex items-center text-xs">' + starsHtml + '</div>' +
                                    '<span class="review-date">' + safeCreatedAt + '</span>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="review-body-text">' +
                        safeReviewText +
                    '</div>' +
                    imgHtml +
                '</div>';
        });
        
        if (pagedResult.totalPages > 1) {
            let paginationHtml = '<div class="flex items-center justify-center gap-2 mt-6">';
            const navButtonClass = 'px-3 py-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 text-xs font-bold transition-all cursor-pointer bg-white';
            if (pagedResult.currentPage > 1) {
                paginationHtml += '<button onclick="changeReviewPage(' + (pagedResult.currentPage - 1) + ')" class="' + navButtonClass + '">Trước</button>';
            }
            
            let pagesToShow = new Set();
            pagesToShow.add(1);
            if (pagedResult.totalPages > 1) {
                pagesToShow.add(pagedResult.totalPages);
                pagesToShow.add(pagedResult.totalPages - 1);
            }
            pagesToShow.add(pagedResult.currentPage);
            if (pagedResult.currentPage > 1) {
                pagesToShow.add(pagedResult.currentPage - 1);
            }
            if (pagedResult.currentPage < pagedResult.totalPages) {
                pagesToShow.add(pagedResult.currentPage + 1);
            }

            let pagesList = Array.from(pagesToShow).sort((a, b) => a - b);
            for (let idx = 0; idx < pagesList.length; idx++) {
                let pageNum = pagesList[idx];
                if (idx > 0) {
                    let prevPage = pagesList[idx - 1];
                    if (pageNum - prevPage > 1) {
                        paginationHtml += '<button onclick="promptReviewPageJump(' + pagedResult.totalPages + ')" class="w-8 h-8 rounded-lg text-xs font-bold transition-all cursor-pointer border border-gray-200 hover:bg-gray-50 bg-white" title="Nhảy đến trang...">...</button>';
                    }
                }
                
                const isActive = pageNum === pagedResult.currentPage;
                const pageButtonClass = isActive
                    ? 'w-8 h-8 rounded-lg text-xs font-bold transition-all cursor-pointer bg-primary text-white border-none'
                    : 'w-8 h-8 rounded-lg text-xs font-bold transition-all cursor-pointer border border-gray-200 hover:bg-gray-50 bg-white';
                paginationHtml += '<button onclick="changeReviewPage(' + pageNum + ')" class="' + pageButtonClass + '">' + pageNum + '</button>';
            }
            
            if (pagedResult.currentPage < pagedResult.totalPages) {
                paginationHtml += '<button onclick="changeReviewPage(' + (pagedResult.currentPage + 1) + ')" class="' + navButtonClass + '">Tiếp</button>';
            }
            paginationHtml += '</div>';
            html += paginationHtml;
        }
        
        wrapper.innerHTML = html;
    }

    window.promptReviewPageJump = function(totalPages) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: 'Chuyển đến trang',
                text: 'Nhập số trang muốn đến (1 - ' + totalPages + '):',
                input: 'number',
                inputAttributes: { min: 1, max: totalPages, step: 1 },
                showCancelButton: true,
                confirmButtonText: 'Đến',
                cancelButtonText: 'Hủy',
                confirmButtonColor: '#4d661c',
                inputValidator: (value) => {
                    const page = parseInt(value);
                    if (isNaN(page) || page < 1 || page > totalPages) {
                        return 'Số trang phải từ 1 đến ' + totalPages + '!';
                    }
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    changeReviewPage(parseInt(result.value));
                }
            });
        } else {
            const targetPageStr = prompt('Nhập số trang bạn muốn chuyển đến (1 - ' + totalPages + '):');
            if (targetPageStr) {
                const targetPage = parseInt(targetPageStr);
                if (!isNaN(targetPage) && targetPage >= 1 && targetPage <= totalPages) {
                    changeReviewPage(targetPage);
                }
            }
        }
    };

    window.changeReviewPage = function(page) {
        currentReviewPage = page;
        loadReviewsRealtime();
        const reviewsEl = document.getElementById('reviews');
        if (reviewsEl) {
            reviewsEl.scrollIntoView({ behavior: 'smooth' });
        }
    };
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
