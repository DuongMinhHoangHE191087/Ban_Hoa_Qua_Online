<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Đặt hàng thành công - Verdant Market"/></jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
<link href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css" rel="stylesheet">

<script id="tailwind-config">
  tailwind.config = {
    darkMode: "class",
    theme: {
      extend: {
        "colors": {
          "surface-container": "#bcfdc9",
          "on-tertiary": "#ffffff",
          "surface-variant": "#b1f2be",
          "on-error-container": "#93000a",
          "surface-container-highest": "#b1f2be",
          "secondary-fixed": "#b4f0c9",
          "on-background": "#00210d",
          "on-tertiary-fixed-variant": "#314d3e",
          "on-secondary-container": "#386f50",
          "inverse-surface": "#00391a",
          "surface-dim": "#a9e9b6",
          "secondary-container": "#b4f0c9",
          "outline-variant": "#c5c8b7",
          "tertiary-fixed-dim": "#afceba",
          "secondary-fixed-dim": "#99d4ae",
          "primary-fixed-dim": "#b3d17a",
          "primary-fixed": "#ceee93",
          "surface-bright": "#eaffea",
          "on-error": "#ffffff",
          "secondary": "#31694b",
          "on-surface": "#00210d",
          "primary-container": "#d9f99d",
          "error": "#ba1a1a",
          "on-tertiary-fixed": "#042014",
          "surface-container-lowest": "#ffffff",
          "surface-container-high": "#b7f7c3",
          "inverse-primary": "#b3d17a",
          "primary": "#4d661c",
          "on-surface-variant": "#44483b",
          "tertiary-container": "#d5f5e0",
          "on-secondary": "#ffffff",
          "error-container": "#ffdad6",
          "on-primary": "#ffffff",
          "background": "#eaffea",
          "tertiary": "#486554",
          "on-primary-fixed-variant": "#364e03",
          "inverse-on-surface": "#c3ffce",
          "on-secondary-fixed": "#002111",
          "surface-container-low": "#d1ffd8",
          "surface-tint": "#4d661c",
          "surface": "#eaffea",
          "on-primary-fixed": "#131f00",
          "on-primary-container": "#597428",
          "on-tertiary-container": "#557161",
          "on-secondary-fixed-variant": "#175034",
          "outline": "#75796a",
          "tertiary-fixed": "#caead6"
        },
        "borderRadius": {
          "DEFAULT": "0.25rem",
          "lg": "0.5rem",
          "xl": "0.75rem",
          "full": "9999px"
        },
        "spacing": {
          "gutter": "24px",
          "xs": "4px",
          "md": "24px",
          "xl": "64px",
          "margin-mobile": "16px",
          "sm": "12px",
          "lg": "40px",
          "base": "8px",
          "margin-desktop": "48px"
        },
        "fontFamily": {
          "sans": ["Lexend", "sans-serif"]
        }
      }
    }
  }
</script>

<style>
    .glass-card {
        background-color: rgba(255, 255, 255, 0.75);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.5);
        box-shadow: 0 10px 15px -3px rgba(20, 83, 45, 0.05), 0 4px 6px -2px rgba(20, 83, 45, 0.03);
    }
</style>

<div class="pt-24 pb-12 px-margin-mobile md:px-margin-desktop max-w-4xl mx-auto font-sans antialiased text-on-background bg-[#eaffea] min-h-screen">
    
    <c:choose>
        <c:when test="${not empty order}">
            <div class="max-w-2xl mx-auto text-center py-12 glass-card rounded-2xl p-8 flex flex-col items-center gap-6 shadow-xl border border-white/60">
                
                <!-- Icon Checked Hoạt Họa Premium -->
                <div class="w-24 h-24 rounded-full bg-emerald-100/80 flex items-center justify-center text-primary border border-emerald-200 shadow-md animate-bounce">
                    <span class="material-symbols-outlined text-[56px] font-bold text-[#14532D]">verified</span>
                </div>
                
                <div>
                    <h1 class="text-3xl font-extrabold text-[#00210d] tracking-tight">Đặt Hàng Thành Công!</h1>
                    <p class="text-on-surface-variant mt-3 text-sm leading-relaxed max-w-md mx-auto">
                        Cảm ơn bạn đã tin chọn nông sản sạch chất lượng cao tại <strong>MetaFruit Premium</strong>. Đơn hàng của bạn đã được tiếp nhận và xử lý.
                    </p>
                </div>

                <!-- Order Summary Card -->
                <div class="w-full bg-white/60 border border-[#bcfdc9] rounded-xl p-6 text-start text-sm text-on-surface-variant space-y-4 shadow-sm">
                    <h3 class="text-base font-bold text-[#00210d] border-b border-emerald-100/50 pb-2">Thông tin đơn hàng</h3>
                    
                    <div class="flex justify-between items-center py-1">
                        <span class="text-on-surface-variant font-medium">Mã đơn hàng:</span>
                        <span class="text-inverse-surface font-extrabold text-base text-[#14532D]">#<c:out value="${order.orderId}"/></span>
                    </div>
                    
                    <div class="flex justify-between items-center py-1">
                        <span class="text-on-surface-variant font-medium">Tổng tiền thanh toán:</span>
                        <span class="text-[#ba1a1a] font-bold text-lg"><ft:currency value="${order.finalAmount}"/></span>
                    </div>
                    
                    <div class="flex justify-between items-center py-1">
                        <span class="text-on-surface-variant font-medium">Phương thức thanh toán:</span>
                        <span class="text-inverse-surface font-semibold text-sm">
                            <c:choose>
                                <c:when test="${order.paymentMethod eq 'CK'}">
                                    Chuyển khoản QR ngân hàng
                                </c:when>
                                <c:otherwise>
                                    Thanh toán khi nhận hàng (COD)
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    
                    <div class="flex justify-between items-center py-1">
                        <span class="text-on-surface-variant font-medium">Trạng thái đơn hàng:</span>
                        <span class="px-3 py-1 bg-emerald-100 text-emerald-800 rounded-full font-bold text-xs">
                            <c:choose>
                                <c:when test="${order.status eq 'PENDING_PAYMENT'}">
                                    Chờ thanh toán
                                </c:when>
                                <c:otherwise>
                                    Đã xác nhận
                                </c:otherwise>
                            </c:choose>
                            </span>
                    </div>

                    <!-- Destination Address -->
                    <div class="border-t border-emerald-100/50 pt-3 mt-1">
                        <span class="text-xs font-bold text-primary block mb-1">Địa chỉ nhận hàng:</span>
                        <p class="text-xs text-on-surface-variant leading-relaxed font-medium"><c:out value="${order.deliveryAddress}"/></p>
                    </div>
                </div>

                <!-- Hướng dẫn chặng tiếp theo (Next Step Instructions) -->
                <div class="w-full bg-[#d1ffd8]/50 border border-[#bcfdc9] rounded-xl p-5 text-start">
                    <h4 class="text-sm font-bold text-[#14532D] flex items-center gap-1.5 mb-2">
                        <span class="material-symbols-outlined text-[18px]">info</span>
                        Hướng dẫn chặng tiếp theo:
                    </h4>
                    <c:choose>
                        <c:when test="${order.paymentMethod eq 'CK'}">
                            <p class="text-xs text-on-surface-variant leading-relaxed">
                                Bạn đã chọn phương thức <strong>Chuyển khoản QR ngân hàng</strong>. Để đơn hàng được xác nhận ngay lập tức và giữ chỗ tồn kho nông sản, vui lòng hoàn tất giao dịch thanh toán.
                            </p>
                            <div class="mt-4 flex justify-center">
                                <a href="${pageContext.request.contextPath}/checkout?action=payment&orderId=${order.orderId}" class="w-full sm:w-auto bg-[#14532D] text-white font-bold py-3 px-8 rounded-lg hover:bg-opacity-95 transition-all shadow-md active:scale-95 text-sm flex items-center justify-center gap-2 cursor-pointer">
                                    <span class="material-symbols-outlined text-lg">qr_code_scanner</span>
                                    <span>Thanh toán ngay qua mã QR</span>
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-xs text-on-surface-variant leading-relaxed">
                                Đơn hàng của bạn sẽ được chuẩn bị bởi nhà vườn và bàn giao cho đơn vị vận chuyển hỏa tốc. Thời gian dự kiến giao hàng trong vòng <strong>2 - 3 tiếng</strong>.
                                Vui lòng chuẩn bị sẵn số tiền <strong class="text-[#ba1a1a]"><ft:currency value="${order.finalAmount}"/></strong> để thanh toán cho nhân viên giao hàng khi nhận sản phẩm.
                            </p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Action Buttons -->
                <div class="flex flex-col sm:flex-row gap-4 w-full justify-center border-t border-emerald-100/50 pt-6 mt-4">
                    <a href="${pageContext.request.contextPath}/home" class="bg-white text-[#14532D] border border-[#14532D] font-bold py-3.5 px-6 rounded-lg hover:bg-emerald-50 transition-all text-sm flex items-center justify-center gap-2 cursor-pointer">
                        <span class="material-symbols-outlined text-lg">arrow_back</span>
                        <span>Tiếp tục mua sắm</span>
                    </a>
                    
                    <a href="${pageContext.request.contextPath}/orders" class="bg-[#14532D] text-white font-bold py-3.5 px-6 rounded-lg hover:bg-opacity-90 transition-all shadow-md active:scale-95 text-sm flex items-center justify-center gap-2 cursor-pointer">
                        <span class="material-symbols-outlined text-lg">receipt_long</span>
                        <span>Xem lịch sử đơn hàng</span>
                    </a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="max-w-2xl mx-auto text-center py-12 glass-card rounded-2xl p-8 flex flex-col items-center gap-6 shadow-xl border border-white/60">
                <div class="w-20 h-20 rounded-full bg-red-100 flex items-center justify-center text-red-600 border border-red-200 animate-pulse">
                    <span class="material-symbols-outlined text-[48px] font-bold">error</span>
                </div>
                <div>
                    <h1 class="text-2xl font-bold text-[#93000a]">Không tìm thấy đơn hàng</h1>
                    <p class="text-sm text-on-surface-variant mt-2">Thông tin đơn hàng không tồn tại hoặc bạn không có quyền truy cập thông tin này.</p>
                </div>
                <a href="${pageContext.request.contextPath}/home" class="bg-[#14532D] text-white font-bold py-3 px-6 rounded-lg hover:bg-opacity-90 transition-all text-sm flex items-center justify-center gap-2 cursor-pointer">
                    <span class="material-symbols-outlined">home</span>
                    <span>Quay lại Trang chủ</span>
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <%-- CRITICAL SCRIPT: RESET GIỎ HÀNG LOCAL STORAGE CHỌN LỌC --%>
    <c:set var="purgedIds" value="${sessionScope._purgedVariantIds}"/>
    <c:remove var="_purgedVariantIds" scope="session"/>
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const purgedVariantIdsParam = '<c:out value="${purgedIds}" default=""/>';
            if (purgedVariantIdsParam) {
                const purgedIds = purgedVariantIdsParam.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
                console.log('[FruitMkt] Selective local cart purge. Variant IDs:', purgedIds);

                const keys = ['userCart', 'guestCart'];
                keys.forEach(key => {
                    try {
                        let items = JSON.parse(localStorage.getItem(key)) || [];
                        if (items.length > 0) {
                            items = items.filter(item => !purgedIds.includes(item.variantId));
                            localStorage.setItem(key, JSON.stringify(items));
                        }
                    } catch (e) {
                        console.warn('[FruitMkt] Lỗi lọc giỏ local ' + key + ':', e);
                    }
                });

                if (typeof GuestCart !== 'undefined') {
                    GuestCart.updateBadge();
                } else {
                    const badge = document.getElementById('cart-badge');
                    if (badge) {
                        const items = JSON.parse(localStorage.getItem('userCart')) || [];
                        badge.textContent = items.reduce((sum, i) => sum + i.quantity, 0);
                    }
                }
            }
        });
    </script>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
