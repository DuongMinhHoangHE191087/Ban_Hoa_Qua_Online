<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Giỏ hàng"/></jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined giống hệt file mẫu -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet">

<script id="tailwind-config">
    tailwind.config = {
        darkMode: "class",
        theme: {
            extend: {
                "colors": {
                    "secondary-fixed": "#b4f0c9",
                    "primary-fixed": "#ceee93",
                    "primary": "#4d661c",
                    "primary-container": "#d9f99d",
                    "surface": "#eaffea",
                    "on-secondary-container": "#386f50",
                    "on-error-container": "#93000a",
                    "outline-variant": "#c5c8b7",
                    "inverse-surface": "#00391a",
                    "outline": "#75796a",
                    "on-primary": "#ffffff",
                    "surface-variant": "#b1f2be",
                    "surface-container": "#bcfdc9",
                    "on-surface-variant": "#44483b",
                    "on-secondary-fixed": "#002111",
                    "on-error": "#ffffff",
                    "primary-fixed-dim": "#b3d17a",
                    "inverse-primary": "#b3d17a",
                    "tertiary-container": "#d5f5e0",
                    "on-primary-fixed-variant": "#364e03",
                    "secondary-container": "#b4f0c9",
                    "on-background": "#00210d",
                    "inverse-on-surface": "#c3ffce",
                    "tertiary": "#486554",
                    "surface-container-high": "#b7f7c3",
                    "on-tertiary-fixed": "#042014",
                    "error-container": "#ffdad6",
                    "error": "#ba1a1a",
                    "tertiary-fixed": "#caead6",
                    "secondary-fixed-dim": "#99d4ae",
                    "on-primary-container": "#597428",
                    "on-surface": "#00210d",
                    "on-secondary-fixed-variant": "#175034",
                    "on-tertiary-fixed-variant": "#314d3e",
                    "surface-tint": "#4d661c",
                    "surface-container-highest": "#b1f2be",
                    "on-tertiary-container": "#557161",
                    "surface-dim": "#a9e9b6",
                    "surface-container-lowest": "#ffffff",
                    "surface-container-low": "#d1ffd8",
                    "on-secondary": "#ffffff",
                    "background": "#eaffea",
                    "on-tertiary": "#ffffff",
                    "tertiary-fixed-dim": "#afceba",
                    "secondary": "#31694b",
                    "surface-bright": "#eaffea",
                    "on-primary-fixed": "#131f00"
                },
                "borderRadius": {
                    "DEFAULT": "0.25rem",
                    "lg": "0.5rem",
                    "xl": "0.75rem",
                    "full": "9999px"
                },
                "spacing": {
                    "gutter": "24px",
                    "sm": "12px",
                    "margin-desktop": "48px",
                    "lg": "40px",
                    "xl": "64px",
                    "md": "24px",
                    "margin-mobile": "16px",
                    "base": "8px",
                    "xs": "4px"
                },
                "fontFamily": {
                    "label-md": ["Lexend"],
                    "headline-lg-mobile": ["Lexend"],
                    "headline-md": ["Lexend"],
                    "label-sm": ["Lexend"],
                    "display-lg": ["Lexend"],
                    "body-lg": ["Lexend"],
                    "headline-lg": ["Lexend"],
                    "body-md": ["Lexend"]
                },
                "fontSize": {
                    "label-md": ["14px", { "lineHeight": "20px", "fontWeight": "500" }],
                    "headline-lg-mobile": ["28px", { "lineHeight": "36px", "fontWeight": "600" }],
                    "headline-md": ["24px", { "lineHeight": "32px", "fontWeight": "600" }],
                    "label-sm": ["12px", { "lineHeight": "16px", "letterSpacing": "0.05em", "fontWeight": "600" }],
                    "display-lg": ["48px", { "lineHeight": "56px", "letterSpacing": "-0.02em", "fontWeight": "700" }],
                    "body-lg": ["18px", { "lineHeight": "28px", "fontWeight": "400" }],
                    "headline-lg": ["32px", { "lineHeight": "40px", "letterSpacing": "-0.01em", "fontWeight": "600" }],
                    "body-md": ["16px", { "lineHeight": "24px", "fontWeight": "400" }]
                }
            }
        }
    }
</script>

<style>
    /* Continuous Curvature Squircle shadows */
    .premium-glass-card {
        background: rgba(255, 255, 255, 0.7);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.4);
        box-shadow: 0 8px 32px rgba(20, 83, 45, 0.05);
    }
</style>

<main class="max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-body-md text-on-background">
    <div class="flex items-baseline justify-between mb-lg border-b border-surface-container-high pb-4">
        <h1 class="font-display-lg text-display-lg text-inverse-surface font-bold tracking-tight">Giỏ hàng của bạn</h1>
        <span id="summary-count-top" class="font-body-lg text-body-lg text-on-surface-variant fw-semibold">0 mặt hàng</span>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
        <!-- Khu vực sản phẩm giỏ hàng -->
        <section class="lg:col-span-8 flex flex-col gap-4">
            <!-- Row: Select All Checkbox -->
            <div id="cart-header-actions" class="bg-white/70 backdrop-blur-[12px] border border-white/40 shadow-[0_4px_12px_rgba(20,83,45,0.04)] rounded-xl p-4 flex items-center justify-between hidden select-none">
                <label class="flex items-center gap-3 cursor-pointer">
                    <input type="checkbox" id="chk-select-all" class="rounded text-primary focus:ring-primary w-5 h-5 border-[#BBF7D0] bg-[#eaffea] cursor-pointer" checked>
                    <span class="font-label-md text-inverse-surface font-semibold text-sm">Chọn tất cả (<span id="checked-count">0</span>/<span id="total-count">0</span> sản phẩm)</span>
                </label>
            </div>
            
            <div id="cart-items-container" class="flex flex-col gap-md">
                <!-- Loading Spinner ngầm -->
                <div class="text-center py-12 premium-glass-card rounded-3xl">
                    <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary border-t-transparent" role="status"></div>
                    <p class="text-on-surface-variant mt-4">Đang đồng bộ hóa giỏ hàng tươi ngon...</p>
                </div>
            </div>
        </section>
        
        <!-- Khối Summary (Bên phải) -->
        <aside class="lg:col-span-4">
            <div id="cart-checkout-card" class="bg-white/80 backdrop-blur-[16px] border border-white/50 shadow-[0_8px_24px_rgba(20,83,45,0.06)] rounded-[1.5rem] p-lg sticky top-[100px] hidden">
                <h2 class="font-headline-lg text-headline-lg text-inverse-surface mb-md font-bold">Tóm tắt đơn hàng</h2>
                <div class="flex flex-col gap-sm text-on-surface-variant font-body-md text-body-md">
                    <div class="flex justify-between items-center border-b border-surface-container/30 pb-2">
                        <span>Số lượng mặt hàng</span>
                        <span id="summary-count" class="font-label-md text-inverse-surface fw-bold">0 mặt hàng</span>
                    </div>
                    <div class="flex justify-between items-center border-b border-surface-container/30 pb-2">
                        <span>Tổng trọng lượng</span>
                        <span id="summary-weight" class="font-label-md text-inverse-surface fw-bold">0.000 kg</span>
                    </div>
                    <div class="flex justify-between items-center border-b border-surface-container/30 pb-2">
                        <span>Tiền tạm tính</span>
                        <span id="summary-subtotal" class="font-label-md text-inverse-surface fw-bold">0 đ</span>
                    </div>
                    <div class="flex justify-between items-center">
                        <span>Phí vận chuyển</span>
                        <span class="font-label-md text-primary fw-semibold">Tạm tính sau</span>
                    </div>
                </div>
                <hr class="border-surface-container-high my-md">
                <div class="flex justify-between items-center mb-lg">
                    <span class="font-headline-md text-headline-md text-inverse-surface font-bold">Tổng thanh toán</span>
                    <span id="summary-total" class="font-headline-lg text-headline-lg text-primary font-bold">0 đ</span>
                </div>
                
                <button id="btn-cart-checkout" class="w-full bg-primary text-on-primary font-label-md text-label-md py-4 rounded-xl hover:bg-inverse-surface transition-all shadow-md flex items-center justify-center gap-sm group active:scale-95 transform">
                    <span class="checkout-spinner inline-block animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent me-1 hidden"></span>
                    <span>Tiến hành thanh toán</span>
                    <span class="material-symbols-outlined text-lg group-hover:translate-x-1 transition-transform">arrow_forward</span>
                </button>
                
                <p class="font-label-sm text-label-sm text-on-surface-variant text-center mt-sm opacity-80">
                    Thanh toán an toàn bảo mật bởi Verdant Market.
                </p>
            </div>
        </aside>
    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/pages/cart.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const isLoggedIn = ${not empty sessionScope.currentUser ? "true" : "false"};
        const contextPath = '${pageContext.request.contextPath}';
        CartPage.init(isLoggedIn, contextPath);
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
