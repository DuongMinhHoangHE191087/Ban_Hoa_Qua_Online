<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Thanh toÃ¡n - Verdant Market"/></jsp:include>

<!-- TÃ­ch há»£p Tailwind CSS CDN, Lexend Font vÃ  Material Symbols Outlined cho phong cÃ¡ch Verdant Clarity -->
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
            background-color: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.4);
            box-shadow: 0 4px 6px -1px rgba(20, 83, 45, 0.05), 0 2px 4px -1px rgba(20, 83, 45, 0.03);
        }
        .form-input {
            background-color: #DCFCE7;
            border: 1px solid #14532D;
            color: #00210d;
        }
        .form-input:focus {
            border-width: 2px;
            outline: none;
            box-shadow: none;
        }
        .custom-radio:checked {
            background-color: #14532D;
            border-color: #14532D;
        }
</style>

<div class="pt-24 pb-12 px-margin-mobile md:px-margin-desktop max-w-7xl mx-auto font-sans antialiased text-on-background bg-[#eaffea] min-h-screen">
    
    <c:choose>
        <%-- MÃ€N HÃŒNH Äáº¶T HÃ€NG THÃ€NH CÃ”NG --%>
        <c:when test="${not empty isSuccess}">
            <div class="max-w-2xl mx-auto text-center py-16 glass-card rounded-xl border border-white/50 p-8 flex flex-col items-center gap-6 mt-12 shadow-xl">
                <!-- Checkmark Icon Animated -->
                <div class="w-20 h-20 rounded-full bg-emerald-100 flex items-center justify-center text-primary border border-emerald-200 shadow-md">
                    <span class="material-symbols-outlined text-[48px] font-bold">verified</span>
                </div>
                
                <div>
                    <h1 class="text-3xl font-bold text-inverse-surface tracking-tight">Äáº·t HÃ ng ThÃ nh CÃ´ng!</h1>
                    <p class="text-on-surface-variant mt-3 text-sm leading-relaxed max-w-md mx-auto">
                        Cáº£m Æ¡n báº¡n Ä‘Ã£ mua nÃ´ng sáº£n sáº¡ch táº¡i <strong>Verdant Market</strong>. ÄÆ¡n hÃ ng cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn tá»›i nhÃ  vÆ°á»n Ä‘Ã³ng gÃ³i láº¡nh giao há»a tá»‘c!
                    </p>
                </div>

                <div class="w-full bg-[#d1ffd8]/60 border border-[#bcfdc9] rounded-2xl p-5 text-start text-xs text-on-surface-variant space-y-2">
                    <div class="flex justify-between border-b border-emerald-100/30 pb-2">
                        <span>Tráº¡ng thÃ¡i thanh toÃ¡n</span>
                        <span class="text-primary font-bold">ThÃ nh cÃ´ng (COD / Chá» xá»­ lÃ½)</span>
                    </div>
                    <div class="flex justify-between border-b border-emerald-100/30 pb-2">
                        <span>MÃ£ Ä‘Æ¡n hÃ ng</span>
                        <span class="text-inverse-surface font-bold">#<c:out value="${param.orderId}"/></span>
                    </div>
                    <div class="flex justify-between">
                        <span>Dá»± kiáº¿n giao hÃ ng</span>
                        <span class="text-inverse-surface font-bold">Trong 2 - 3 tiáº¿ng (Giao Há»a Tá»‘c)</span>
                    </div>
                </div>

                <div class="flex flex-col sm:flex-row gap-4 w-full justify-center">
                    <a href="${pageContext.request.contextPath}/home" class="bg-[#14532D] text-white font-bold py-3.5 px-8 rounded-lg hover:bg-opacity-90 transition-all shadow-md active:scale-95 text-sm flex items-center justify-center gap-2 cursor-pointer">
                        <span class="material-symbols-outlined text-lg">shopping_basket</span>
                        <span>Tiáº¿p tá»¥c mua sáº¯m</span>
                    </a>
                </div>
            </div>

            <%-- CRITICAL SCRIPT: RESET GIá»Ž HÃ€NG LOCAL STORAGE CHá»ŒN Lá»ŒC --%>
            <%-- [FIX] Äá»c purgedVariantIds tá»« session attribute (khÃ´ng lá»™ trÃªn URL) --%>
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
                                console.warn('[FruitMkt] Lá»—i lá»c giá» local ' + key + ':', e);
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
                    } else {
                        // Fallback: xÃ³a toÃ n bá»™
                        localStorage.removeItem('userCart');
                        localStorage.removeItem('guestCart');
                        if (typeof GuestCart !== 'undefined') {
                            GuestCart.clear();
                            GuestCart.updateBadge();
                        }
                    }
                });
            </script>
        </c:when>

        <%-- FORM THANH TOÃN chuáº©n máº«u Check_Out_UI.html --%>
        <c:otherwise>
            <!-- Dynamic Flash Message Alert -->
            <c:if test="${not empty sessionScope.flashMsg}">
                <div id="flash-alert-container" class="mb-6 p-4 rounded-2xl bg-red-50 flex items-center gap-3 border-l-4 border-red-500 text-red-700 shadow-md">
                    <span class="material-symbols-outlined">error</span>
                    <div class="flex-grow font-bold text-sm"><c:out value="${sessionScope.flashMsg}"/></div>
                    <button type="button" onclick="document.getElementById('flash-alert-container').remove();" class="opacity-60 hover:opacity-100 focus:outline-none">
                        <span class="material-symbols-outlined text-[18px]">close</span>
                    </button>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <div class="mb-8 flex items-baseline justify-between border-b border-[#b1f2be] pb-4">
                <div>
                    <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-primary mb-2">HoÃ n táº¥t Ä‘Æ¡n hÃ ng</h1>
                    <p class="font-body-md text-body-md text-on-surface-variant">Vui lÃ²ng kiá»ƒm tra láº¡i thÃ´ng tin vÃ  xÃ¡c nháº­n thanh toÃ¡n.</p>
                </div>
                <a href="${pageContext.request.contextPath}/cart" class="text-xs font-bold text-primary flex items-center gap-1 hover:underline">
                    <span class="material-symbols-outlined text-[16px]">arrow_back</span>
                    Quay láº¡i giá» hÃ ng
                </a>
            </div>

            <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="post" onsubmit="return validateCheckoutForm()" class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="variantIds" value="<c:out value="${param.variantIds}"/>">

                <!-- LEFT COLUMN: Forms -->
                <div class="lg:col-span-8 flex flex-col gap-8">
                    <!-- Section 1: Delivery Info -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center gap-3 mb-6 text-primary border-b border-[#b1f2be] pb-3">
                            <span class="material-symbols-outlined text-2xl">local_shipping</span>
                            <h2 class="font-headline-md text-headline-md font-bold">ThÃ´ng tin giao hÃ ng</h2>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <%-- Shopee/TikTok Style Address Selector --%>
                            <div class="flex flex-col gap-2 md:col-span-2 mb-2">
                                <label class="font-label-md text-label-md text-[#14532D] font-bold">Äá»‹a chá»‰ nháº­n hÃ ng</label>
                                
                                <!-- 1. Selected Address Display Card -->
                                <div id="selectedAddressCard" class="bg-white border border-[#bcfdc9] rounded-xl p-4 shadow-sm flex items-start justify-between gap-4 transition-all duration-200">
                                    <div class="flex-grow space-y-1">
                                        <div class="flex items-center gap-2 flex-wrap">
                                            <span id="displayRecipientName" class="font-bold text-sm text-slate-800"></span>
                                            <span class="text-gray-300">|</span>
                                            <span id="displayRecipientPhone" class="text-xs text-slate-600 font-semibold"></span>
                                            <span id="defaultBadge" class="hidden px-2 py-0.5 bg-red-100 text-red-700 text-[9px] font-bold rounded-full border border-red-200">Máº·c Ä‘á»‹nh</span>
                                        </div>
                                        <p id="displayAddressDetail" class="text-xs text-slate-600 leading-relaxed"></p>
                                    </div>
                                    <button type="button" onclick="toggleAddressSelectionList()" class="text-primary hover:underline font-bold text-xs flex items-center gap-0.5 shrink-0 cursor-pointer">
                                        Thay Ä‘á»•i <span class="material-symbols-outlined text-[16px] transition-transform duration-200" id="expandIcon">expand_more</span>
                                    </button>
                                </div>

                                <!-- 2. Collapsible Address List -->
                                <div id="addressSelectionList" class="hidden mt-3 space-y-3 bg-[#d1ffd8]/20 border border-[#bcfdc9]/40 rounded-xl p-4 transition-all duration-300">
                                    <div class="text-[10px] font-bold uppercase tracking-wider text-[#31694b] mb-2">Chá»n Ä‘á»‹a chá»‰ nháº­n hÃ ng khÃ¡c</div>
                                    <div id="addressOptionsContainer" class="space-y-2 max-h-60 overflow-y-auto pr-1">
                                        <!-- Rendered dynamically by JS -->
                                    </div>
                                    <div class="flex justify-between items-center border-t border-emerald-100/50 pt-3 mt-3">
                                        <button type="button" onclick="showInlineAddressForm('add')" class="text-primary hover:underline text-xs font-bold flex items-center gap-0.5 cursor-pointer">
                                            <span class="material-symbols-outlined text-[16px]">add</span> ThÃªm Ä‘á»‹a chá»‰ má»›i
                                        </button>
                                        <button type="button" onclick="toggleAddressSelectionList()" class="text-slate-500 hover:text-slate-700 text-xs font-bold cursor-pointer">
                                            ÄÃ³ng
                                        </button>
                                    </div>
                                </div>

                                <!-- 3. Collapsible Inline Form -->
                                <div id="inlineAddressForm" class="hidden overflow-hidden transition-all duration-300 border-t border-emerald-100 pt-4 mt-4 space-y-4">
                                    <div class="text-xs font-bold text-[#14532D]" id="inlineFormTitle">ThÃªm Ä‘á»‹a chá»‰ giao hÃ ng má»›i</div>
                                    <input type="hidden" id="inlineAddressId" value="">
                                    <input type="hidden" id="inlineAction" value="add">
                                    
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[10px] font-bold text-slate-700" for="mRecipientName">Há» vÃ  tÃªn ngÆ°á»i nháº­n *</label>
                                            <input type="text" id="mRecipientName" class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-[#14532D] bg-white">
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[10px] font-bold text-slate-700" for="mRecipientPhone">Sá»‘ Ä‘iá»‡n thoáº¡i nháº­n hÃ ng *</label>
                                            <input type="text" id="mRecipientPhone" class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-[#14532D] bg-white">
                                        </div>
                                        <div class="flex flex-col gap-1.5 md:col-span-2">
                                            <label class="text-[10px] font-bold text-slate-700" for="mAddressDetail">Äá»‹a chá»‰ chi tiáº¿t *</label>
                                            <textarea id="mAddressDetail" rows="2" placeholder="Sá»‘ nhÃ , tÃªn Ä‘Æ°á»ng, phÆ°á»ng/xÃ£, quáº­n/huyá»‡n, tá»‰nh/thÃ nh phá»‘..."
                                                      class="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-[#14532D] resize-none bg-white"></textarea>
                                        </div>
                                    </div>
                                    
                                    <div class="flex items-center gap-2">
                                        <input type="checkbox" id="mIsDefault" class="rounded text-[#14532D] focus:ring-[#14532D] h-4 w-4 cursor-pointer">
                                        <label for="mIsDefault" class="text-[10px] text-slate-700 font-bold cursor-pointer select-none">Äáº·t lÃ m Ä‘á»‹a chá»‰ nháº­n hÃ ng máº·c Ä‘á»‹nh</label>
                                    </div>
                                    
                                    <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
                                        <button type="button" id="btnCancelInlineAddress" onclick="hideInlineAddressForm()" class="px-4 py-2 border border-slate-200 hover:bg-slate-50 text-[10px] font-bold text-slate-600 rounded-lg transition-colors bg-white cursor-pointer">Há»§y bá»</button>
                                        <button type="button" id="btnSaveCheckoutAddress" onclick="handleInlineAddressSubmit()" class="px-4 py-2 bg-[#14532D] text-white text-[10px] font-bold rounded-lg transition-colors border-0 cursor-pointer hover:bg-opacity-95">LÆ°u Ä‘á»‹a chá»‰</button>
                                    </div>
                                </div>
                            </div>

                            <!-- Hidden inputs to be posted to the servlet -->
                            <input type="hidden" id="fullName" name="fullName">
                            <input type="hidden" id="phone" name="phone">
                            <input type="hidden" id="deliveryAddress" name="deliveryAddress">
                            <input type="hidden" id="saveAddressToBook" name="saveAddressToBook" value="false">
                            <div class="flex flex-col gap-2 md:col-span-2">
                                <label class="font-label-md text-label-md text-[#14532D]" for="notes">Ghi chÃº (Tuá»³ chá»n)</label>
                                <textarea class="form-input rounded-lg px-4 py-3 font-body-md text-body-md w-full" id="notes" name="notes" placeholder="Ghi chÃº thÃªm cho ngÆ°á»i giao hÃ ng..." rows="3"></textarea>
                            </div>
                        </div>
                    </section>

                    <!-- Section 2: Payment Methods -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center gap-3 mb-6 text-primary border-b border-[#b1f2be] pb-3">
                            <span class="material-symbols-outlined text-2xl">payments</span>
                            <h2 class="font-headline-md text-headline-md font-bold">PhÆ°Æ¡ng thá»©c thanh toÃ¡n</h2>
                        </div>
                        <div class="flex flex-col gap-4">
                            <!-- Option 1: COD -->
                            <label class="flex items-start gap-4 p-4 rounded-lg border border-outline-variant hover:border-primary bg-white/40 cursor-pointer transition-colors">
                                <div class="flex items-center h-6">
                                    <input checked="" class="custom-radio w-5 h-5 text-primary focus:ring-primary border-[#14532D]" name="paymentMethod" type="radio" value="COD">
                                </div>
                                <div class="flex flex-col">
                                    <span class="font-label-md text-label-md text-on-surface font-bold">Thanh toÃ¡n khi nháº­n hÃ ng (COD)</span>
                                    <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">Thanh toÃ¡n báº±ng tiá»n máº·t khi giao hÃ ng táº­n nÆ¡i.</span>
                                </div>
                                <span class="material-symbols-outlined ml-auto text-primary opacity-70">local_atm</span>
                            </label>
                            
                            <!-- Option 2: Bank QR -->
                            <label class="flex items-start gap-4 p-4 rounded-lg border border-outline-variant hover:border-primary bg-white/40 cursor-pointer transition-colors">
                                <div class="flex items-center h-6">
                                    <input class="custom-radio w-5 h-5 text-primary focus:ring-primary border-[#14532D]" name="paymentMethod" type="radio" value="CK">
                                </div>
                                <div class="flex flex-col">
                                    <span class="font-label-md text-label-md text-on-surface font-bold">Chuyá»ƒn khoáº£n QR ngÃ¢n hÃ ng</span>
                                    <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">ThÃ´ng tin vÃ  mÃ£ QR chuyá»ƒn khoáº£n sáº½ hiá»ƒn thá»‹ sau khi Ä‘áº·t hÃ ng.</span>
                                </div>
                                <span class="material-symbols-outlined ml-auto text-primary opacity-70">account_balance</span>
                            </label>
                        </div>
                    </section>

                    <!-- Section 3: MÃ£ Giáº£m GiÃ¡ -->
                    <section class="glass-card rounded-xl p-6 md:p-8" id="coupon-section">
                        <div class="flex items-center gap-3 mb-4 text-primary border-b border-[#b1f2be] pb-3">
                            <span class="material-symbols-outlined text-2xl">loyalty</span>
                            <h2 class="font-headline-md text-headline-md font-bold">Voucher shop / Voucher sÃ n</h2>
                        </div>
                        <!-- Má»™t Ã´ nháº­p mÃ£ giáº£m giÃ¡ duy nháº¥t -->
                        <div>
                            <label class="block text-sm font-bold text-[#14532D] mb-1" for="couponInput">Nháº­p mÃ£ voucher shop hoáº·c voucher sÃ n</label>
                            <div class="flex gap-2">
                                <input type="text" id="couponInput" placeholder="Nháº­p mÃ£ voucher (VD: SHOP10, SAAN5, SALE20)"
                                    class="form-input rounded-lg px-3 py-2.5 text-sm flex-1 uppercase font-semibold tracking-wider"
                                    style="text-transform:uppercase"/>
                                <button type="button" onclick="applyCoupon()"
                                    class="bg-[#14532D] text-white font-bold px-5 py-2.5 rounded-lg text-sm hover:bg-opacity-90 transition-all cursor-pointer flex-shrink-0 active:scale-95 duration-150 shadow-md">
                                    Ãp dá»¥ng
                                </button>
                            </div>
                            <p id="couponMsg" class="text-xs mt-2 hidden"></p>
                        </div>
                        
                        <!-- Container hiá»ƒn thá»‹ cÃ¡c mÃ£ Ä‘Ã£ Ã¡p dá»¥ng -->
                        <div id="appliedCouponsContainer" class="mt-4 hidden border-t border-dashed border-emerald-200 pt-3">
                            <span class="text-xs font-bold text-on-surface-variant block mb-2">MÃ£ Ä‘Ã£ Ã¡p dá»¥ng:</span>
                            <div id="appliedCouponsList" class="flex flex-col gap-2">
                                <!-- Rendered dynamically via JS -->
                            </div>
                        </div>

                        <!-- Hidden inputs Ä‘á»ƒ gá»­i lÃªn Servlet khi Submit -->
                        <input type="hidden" name="shopCouponCode" id="shopCouponCode"/>
                        <input type="hidden" name="systemCouponCode" id="systemCouponCode"/>

                    </section>

                    <!-- Section 4: Order Items List -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center justify-between mb-6 border-b border-[#b1f2be] pb-3">
                            <div class="flex items-center gap-3 text-primary">
                                <span class="material-symbols-outlined text-2xl">shopping_basket</span>
                                <h2 class="font-headline-md text-headline-md font-bold">Sáº£n pháº©m trong Ä‘Æ¡n</h2>
                            </div>
                            <a class="font-label-md text-label-md text-primary hover:underline flex items-center gap-1 font-semibold text-sm" href="${pageContext.request.contextPath}/cart">
                                Sá»­a giá» hÃ ng <span class="material-symbols-outlined text-sm">edit</span>
                            </a>
                        </div>
                        <div class="flex flex-col gap-4">
                            <c:forEach var="item" items="${cartSummary.items}">
                                <div class="flex items-center gap-4 py-4 border-b border-[#BBF7D0] last:border-0">
                                    <div class="w-20 h-20 rounded-lg overflow-hidden shrink-0 border border-emerald-100/50">
                                        <c:choose>
                                            <c:when test="${not empty item.imagePath}">
                                                <c:set var="itemImg" value="${item.imagePath}"/>
                                                <c:choose>
                                                    <c:when test="${fn:startsWith(itemImg, 'http://') || fn:startsWith(itemImg, 'https://')}">
                                                        <img src="${itemImg}" alt="${item.productName}" class="w-full h-full object-cover">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="resolvedItemImg" value="${itemImg}"/>
                                                        <c:if test="${!fn:startsWith(resolvedItemImg, '/')}">
                                                            <c:set var="resolvedItemImg" value="/${resolvedItemImg}"/>
                                                        </c:if>
                                                        <img src="${pageContext.request.contextPath}${resolvedItemImg}" alt="${item.productName}" class="w-full h-full object-cover">
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/assets/img/placeholder.png" alt="Placeholder" class="w-full h-full object-cover">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="flex flex-col flex-grow">
                                        <span class="font-label-md text-label-md text-on-surface text-lg font-bold"><c:out value="${item.productName}"/></span>
                                        <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">Biáº¿n thá»ƒ: <strong class="text-primary"><c:out value="${item.variantLabel}"/></strong></span>
                                        <span class="font-body-md text-body-md text-on-surface-variant text-xs mt-0.5">Trá»ng lÆ°á»£ng: <c:out value="${item.weightKg}"/> kg</span>
                                        <c:if test="${not empty item.packagingLabel}">
                                            <span class="font-body-md text-body-md text-on-surface-variant text-xs mt-1 block">
                                                ÄÃ³ng gÃ³i: <span class="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded text-xs font-semibold border border-[#BBF7D0]/40"><c:out value="${item.packagingLabel}"/> (+<ft:currency value="${item.packagingPriceAdd}"/>)</span>
                                            </span>
                                        </c:if>
                                    </div>
                                    <div class="text-right">
                                        <span class="font-label-md text-label-md text-primary font-bold text-lg block">
                                            <ft:currency value="${item.price + item.packagingPriceAdd}"/>
                                        </span>
                                        <c:if test="${item.packagingPriceAdd > 0}">
                                            <span class="text-[10px] text-gray-400 block">Gá»“m Ä‘Ã³ng gÃ³i +<ft:currency value="${item.packagingPriceAdd}"/></span>
                                        </c:if>
                                        <span class="font-body-md text-body-md text-on-surface-variant text-sm font-semibold">SL: <c:out value="${item.quantity}"/></span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </div>

                <!-- RIGHT COLUMN: Sidebar Summary -->
                <div class="lg:col-span-4">
                    <div class="glass-card rounded-xl p-6 sticky top-24 border border-white/50 shadow-xl">
                        <h2 class="font-headline-md text-headline-md text-primary mb-6 font-bold border-b border-[#b1f2be] pb-3">Tá»•ng káº¿t Ä‘Æ¡n hÃ ng</h2>
                        <div class="flex flex-col gap-4 font-body-md text-body-md text-on-surface mb-6">
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Táº¡m tÃ­nh sau sale trá»±c tiáº¿p (<c:out value="${fn:length(cartSummary.items)}"/> sáº£n pháº©m)</span>
                                <span class="font-bold text-inverse-surface" id="summary-subtotal"><ft:currency value="${cartSummary.subtotal}"/></span>
                            </div>
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Sale trá»±c tiáº¿p</span>
                                <span class="font-bold text-red-600" id="summary-direct-sale">- <ft:currency value="${directSaleAmount}"/></span>
                            </div>
                            <div class="flex justify-between items-center" id="shop-discount-row" style="display:none!important">
                                <span class="text-on-surface-variant">Voucher shop</span>
                                <span class="font-bold text-red-600" id="summary-shop-discount">- 0 Ä‘</span>
                            </div>
                            <div class="flex justify-between items-center" id="system-discount-row" style="display:none!important">
                                <span class="text-on-surface-variant">Voucher sÃ n</span>
                                <span class="font-bold text-red-600" id="summary-system-discount">- 0 Ä‘</span>
                            </div>
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Tá»•ng trá»ng lÆ°á»£ng</span>
                                <span class="font-bold text-inverse-surface"><c:out value="${cartSummary.totalWeight}"/> kg</span>
                            </div>
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">PhÃ­ váº­n chuyá»ƒn (Giao há»a tá»‘c)</span>
                                <span class="font-bold text-inverse-surface" id="summary-delivery"><ft:currency value="${cartSummary.deliveryFee}"/></span>
                            </div>
                        </div>
                        <div class="border-t border-[#BBF7D0] pt-4 mb-8">
                            <div class="flex justify-between items-end">
                                <span class="font-label-md text-label-md text-on-surface font-bold">Tá»•ng cá»™ng</span>
                                <span class="font-headline-md text-headline-md text-primary font-black text-2xl" id="summary-total"><ft:currency value="${cartSummary.total}"/></span>
                            </div>
                            <span class="block text-right text-xs text-on-surface-variant mt-1">(ÄÃ£ bao gá»“m VAT & CÆ°á»›c báº£o Ã´n cold-chain)</span>
                        </div>
                        <button id="submitBtn" type="submit" class="w-full py-4 rounded-lg bg-[#14532D] text-white font-label-md text-label-md flex justify-center items-center gap-2 hover:bg-opacity-90 transition-all font-bold cursor-pointer active:scale-95 shadow-md">
                            Äáº·t hÃ ng ngay
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </button>
                        <p class="text-center text-xs text-on-surface-variant mt-4 font-body-md flex items-center justify-center gap-1 opacity-80">
                            <span class="material-symbols-outlined text-sm">shield</span>
                            ThÃ´ng tin cá»§a báº¡n Ä‘Æ°á»£c báº£o máº­t tuyá»‡t Ä‘á»‘i
                        </p>
                    </div>
                </div>
            </form>
        </c:otherwise>
    </c:choose>

</div>

<!-- Hidden inputs to safely pass server variables to JS without IDE syntax errors -->
<input type="hidden" id="js-subtotal" value="${cartSummary.subtotal}">
<input type="hidden" id="js-delivery" value="${cartSummary.deliveryFee}">
<input type="hidden" id="js-ctx" value="${pageContext.request.contextPath}">
<input type="hidden" id="js-owner-id" value="<c:out value='${shopOwnerId}' default='0'/>">

<!-- Hidden address elements for JS -->
<c:forEach var="addr" items="${userAddresses}">
    <span class="js-user-address-data" style="display:none;"
          data-address-id="${addr.addressId}"
          data-recipient-name="<c:out value="${addr.recipientName}"/>"
          data-recipient-phone="<c:out value="${addr.recipientPhone}"/>"
          data-address-detail="<c:out value="${addr.addressDetail}"/>"
          data-is-default="${addr['default'] ? 'true' : 'false'}"></span>
</c:forEach>

<script>
// â”€â”€â”€ Coupon AJAX Logic (Merged Input) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
const SUBTOTAL    = parseFloat(document.getElementById('js-subtotal').value || '0');
const DELIVERY    = parseFloat(document.getElementById('js-delivery').value || '0');
const CTX         = document.getElementById('js-ctx').value;
const OWNER_ID    = document.getElementById('js-owner-id').value;

let shopCouponCode   = '';
let shopDiscount     = 0;
let systemCouponCode = '';
let systemDiscount   = 0;

function applyCoupon() {
    const inputEl = document.getElementById('couponInput');
    const code = inputEl.value.trim().toUpperCase();
    if (!code) return;

    // Check if coupon is already applied
    if (code === shopCouponCode || code === systemCouponCode) {
        showCouponMsg('MÃ£ voucher nÃ y Ä‘Ã£ Ä‘Æ°á»£c Ã¡p dá»¥ng rá»“i.', 'text-red-600 font-bold');
        return;
    }

    showCouponMsg('Äang kiá»ƒm tra...', 'text-on-surface-variant');

    // Step 1: Validate as SHOP coupon first
    validateCouponAPI(code, 'SHOP')
        .then(data => {
            if (data.valid) {
                // Applied successfully as SHOP coupon!
                shopCouponCode = code;
                shopDiscount = data.discountAmount || 0;
                document.getElementById('shopCouponCode').value = code;
                showCouponMsg('âœ” ' + data.message + ' (MÃ£ cá»§a Shop)', 'text-emerald-700 font-bold');
                inputEl.value = '';

                // Stacking adjustment: if we already have a system coupon, we must revalidate it 
                // because the subtotal after shop coupon discount has changed!
                if (systemCouponCode) {
                    revalidateSystemCoupon().then(() => {
                        updateSummary();
                        renderAppliedCoupons();
                    });
                } else {
                    updateSummary();
                    renderAppliedCoupons();
                }
            } else {
                // Step 2: If shop validation fails, try validating as SYSTEM coupon
                return validateCouponAPI(code, 'SYSTEM')
                    .then(sysData => {
                        if (sysData.valid) {
                            systemCouponCode = code;
                            systemDiscount = sysData.discountAmount || 0;
                            document.getElementById('systemCouponCode').value = code;
                            showCouponMsg('âœ” ' + sysData.message + ' (MÃ£ cá»§a SÃ n)', 'text-emerald-700 font-bold');
                            inputEl.value = '';
                            updateSummary();
                            renderAppliedCoupons();
                        } else {
                            // If both failed, show error message
                            showCouponMsg('âœ˜ MÃ£ voucher khÃ´ng há»£p lá»‡, Ä‘Ã£ háº¿t háº¡n, hoáº·c khÃ´ng Ä‘á»§ Ä‘iá»u kiá»‡n tá»‘i thiá»ƒu.', 'text-red-600 font-bold');
                            console.log('[Coupon Log] Shop check error:', data.message);
                            console.log('[Coupon Log] System check error:', sysData.message);
                        }
                    });
            }
        })
        .catch(err => {
            console.error('[Coupon Log] Error validating coupon:', err);
            showCouponMsg('âœ˜ Lá»—i káº¿t ná»‘i. Vui lÃ²ng thá»­ láº¡i.', 'text-red-600 font-bold');
        });
}

function validateCouponAPI(code, scope) {
    const currentSubtotal = scope === 'SYSTEM' ? (SUBTOTAL - shopDiscount) : SUBTOTAL;
    const url = CTX + '/api/coupon/validate?code=' + encodeURIComponent(code) +
                '&subtotal=' + currentSubtotal +
                '&ownerId=' + OWNER_ID +
                '&scope=' + scope;
    return fetch(url).then(r => r.json());
}

function revalidateSystemCoupon() {
    if (!systemCouponCode) return Promise.resolve();
    
    return validateCouponAPI(systemCouponCode, 'SYSTEM')
        .then(data => {
            if (data.valid) {
                systemDiscount = data.discountAmount || 0;
            } else {
                console.warn('[Coupon Log] System coupon ' + systemCouponCode + ' became invalid after shop discount applied: ' + data.message);
                systemCouponCode = '';
                systemDiscount = 0;
                document.getElementById('systemCouponCode').value = '';
            }
        })
        .catch(err => {
            console.error('[Coupon Log] Error revalidating system coupon:', err);
        });
}

function removeCoupon(scope) {
    if (scope === 'SHOP') {
        shopCouponCode = '';
        shopDiscount = 0;
        document.getElementById('shopCouponCode').value = '';
        showCouponMsg('ÄÃ£ xÃ³a mÃ£ cá»§a Shop.', 'text-on-surface-variant');
        
        if (systemCouponCode) {
            revalidateSystemCoupon().then(() => {
                updateSummary();
                renderAppliedCoupons();
            });
        } else {
            updateSummary();
            renderAppliedCoupons();
        }
    } else if (scope === 'SYSTEM') {
        systemCouponCode = '';
        systemDiscount = 0;
        document.getElementById('systemCouponCode').value = '';
        showCouponMsg('ÄÃ£ xÃ³a mÃ£ cá»§a SÃ n.', 'text-on-surface-variant');
        updateSummary();
        renderAppliedCoupons();
    }
}

function showCouponMsg(text, className) {
    const msgEl = document.getElementById('couponMsg');
    msgEl.textContent = text;
    msgEl.className = 'text-xs mt-2 ' + className;
    msgEl.classList.remove('hidden');
}

function renderAppliedCoupons() {
    const container = document.getElementById('appliedCouponsContainer');
    const listEl = document.getElementById('appliedCouponsList');
    listEl.innerHTML = '';
    
    let hasCoupon = false;
    const fmt = (n) => new Intl.NumberFormat('vi-VN', {style:'currency', currency:'VND'}).format(n);
    
    if (shopCouponCode) {
        hasCoupon = true;
        const item = document.createElement('div');
        item.className = 'flex justify-between items-center bg-emerald-50 border border-emerald-200 rounded-lg px-3 py-2 text-xs';
        item.innerHTML = '<div>' +
            '<span class="font-bold text-emerald-800 bg-emerald-200 px-1.5 py-0.5 rounded mr-1">Voucher shop</span>' +
            '<span class="font-bold text-on-surface">' + shopCouponCode + '</span>' +
            '<span class="text-on-surface-variant ml-1">(Giáº£m ' + fmt(shopDiscount) + ')</span>' +
            '</div>' +
            '<button type="button" onclick="removeCoupon(\'SHOP\')" class="text-red-600 hover:text-red-800 font-bold ml-2 focus:outline-none">XÃ³a</button>';
        listEl.appendChild(item);
    }
    
    if (systemCouponCode) {
        hasCoupon = true;
        const item = document.createElement('div');
        item.className = 'flex justify-between items-center bg-teal-50 border border-teal-200 rounded-lg px-3 py-2 text-xs';
        item.innerHTML = '<div>' +
            '<span class="font-bold text-teal-800 bg-teal-200 px-1.5 py-0.5 rounded mr-1">Voucher sÃ n</span>' +
            '<span class="font-bold text-on-surface">' + systemCouponCode + '</span>' +
            '<span class="text-on-surface-variant ml-1">(Giáº£m ' + fmt(systemDiscount) + ')</span>' +
            '</div>' +
            '<button type="button" onclick="removeCoupon(\'SYSTEM\')" class="text-red-600 hover:text-red-800 font-bold ml-2 focus:outline-none">XÃ³a</button>';
        listEl.appendChild(item);
    }
    
    if (hasCoupon) {
        container.classList.remove('hidden');
    } else {
        container.classList.add('hidden');
    }
}

function updateSummary() {
    const total = Math.max(0, SUBTOTAL - shopDiscount - systemDiscount + DELIVERY);
    const fmt = (n) => new Intl.NumberFormat('vi-VN', {style:'currency', currency:'VND'}).format(n);

    const shopRow   = document.getElementById('shop-discount-row');
    const systemRow = document.getElementById('system-discount-row');

    if (shopDiscount > 0) {
        shopRow.style.removeProperty('display');
        document.getElementById('summary-shop-discount').textContent = '- ' + fmt(shopDiscount);
    } else {
        shopRow.style.setProperty('display', 'none', 'important');
    }

    if (systemDiscount > 0) {
        systemRow.style.removeProperty('display');
        document.getElementById('summary-system-discount').textContent = '- ' + fmt(systemDiscount);
    } else {
        systemRow.style.setProperty('display', 'none', 'important');
    }

    document.getElementById('summary-total').textContent = fmt(total);
}

// Enter key apply coupon
document.getElementById('couponInput').addEventListener('keydown', e => { 
    if (e.key === 'Enter') {
        e.preventDefault();
        applyCoupon(); 
    } 
});

// â”€â”€â”€ Shopee/TikTok Style Address Widget Logic â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
let userAddresses = Array.from(document.querySelectorAll('.js-user-address-data')).map(el => ({
    addressId: parseInt(el.getAttribute('data-address-id')),
    recipientName: el.getAttribute('data-recipient-name'),
    recipientPhone: el.getAttribute('data-recipient-phone'),
    addressDetail: el.getAttribute('data-address-detail'),
    isDefault: el.getAttribute('data-is-default') === 'true'
}));

let selectedAddressId = null;

// Populate initial address
function initAddressWidget() {
    // If no addresses, show the inline form immediately (in 'add' mode, permanently expanded)
    if (userAddresses.length === 0) {
        document.getElementById('selectedAddressCard').style.display = 'none';
        showInlineAddressForm('add');
        document.getElementById('btnCancelInlineAddress').style.display = 'none'; // No cancel since they must add an address
        document.getElementById('mIsDefault').checked = true;
        document.getElementById('mIsDefault').disabled = true; // Must be default since it's the first one
        return;
    }

    // Find default or first address
    let activeAddr = userAddresses.find(a => a.isDefault);
    if (!activeAddr) activeAddr = userAddresses[0];

    selectAddress(activeAddr.addressId);
    renderAddressList();
}

function selectAddress(addressId) {
    selectedAddressId = addressId;
    const addr = userAddresses.find(a => a.addressId === addressId);
    if (!addr) return;

    // Update display card
    document.getElementById('displayRecipientName').textContent = addr.recipientName;
    document.getElementById('displayRecipientPhone').textContent = addr.recipientPhone;
    document.getElementById('displayAddressDetail').textContent = addr.addressDetail;
    
    const defaultBadge = document.getElementById('defaultBadge');
    if (addr.isDefault) {
        defaultBadge.classList.remove('hidden');
    } else {
        defaultBadge.classList.add('hidden');
    }

    // Set form hidden inputs for submit
    document.getElementById('fullName').value = addr.recipientName;
    document.getElementById('phone').value = addr.recipientPhone;
    document.getElementById('deliveryAddress').value = addr.addressDetail;

    // Re-render list to show checked radio
    renderAddressList();
}

function renderAddressList() {
    const container = document.getElementById('addressOptionsContainer');
    if (!container) return;
    container.innerHTML = '';

    userAddresses.forEach(addr => {
        const isSelected = (addr.addressId === selectedAddressId);
        
        const card = document.createElement('div');
        card.className = `p-3 rounded-lg border text-xs flex items-start justify-between gap-3 cursor-pointer transition-all duration-150 ${isSelected ? 'bg-emerald-50/75 border-[#14532D]/40 shadow-sm' : 'bg-white border-slate-100 hover:border-slate-300'}`;
        card.onclick = () => selectAddress(addr.addressId);

        card.innerHTML = `
            <div class="flex items-start gap-2.5 flex-grow">
                <input type="radio" name="addrSelect" value="${addr.addressId}" ${isSelected ? 'checked' : ''} class="mt-0.5 text-primary focus:ring-primary h-3.5 w-3.5 cursor-pointer">
                <div class="space-y-0.5 flex-grow">
                    <div class="flex items-center gap-1.5 flex-wrap">
                        <span class="font-bold text-slate-800">${addr.recipientName}</span>
                        <span class="text-slate-300">|</span>
                        <span class="text-slate-600 font-semibold">${addr.recipientPhone}</span>
                        \${addr.isDefault ? '<span class="px-1.5 py-0.2 bg-red-100 text-red-700 text-[8px] font-bold rounded border border-red-200">Máº·c Ä‘á»‹nh</span>' : ''}
                    </div>
                    <p class="text-[11px] text-slate-500 leading-relaxed">\${addr.addressDetail}</p>
                </div>
            </div>
            <button type="button" onclick="event.stopPropagation(); showInlineAddressForm('edit', \${addr.addressId})" class="text-[#14532D] hover:underline font-bold shrink-0 text-[11px] cursor-pointer">Sá»­a</button>
        `;
        
        container.appendChild(card);
    });
}

function toggleAddressSelectionList() {
    const list = document.getElementById('addressSelectionList');
    const icon = document.getElementById('expandIcon');
    if (list.classList.contains('hidden')) {
        list.classList.remove('hidden');
        if (icon) icon.classList.add('rotate-180');
    } else {
        list.classList.add('hidden');
        if (icon) icon.classList.remove('rotate-180');
    }
}

function showInlineAddressForm(mode, addressId = null) {
    const form = document.getElementById('inlineAddressForm');
    const title = document.getElementById('inlineFormTitle');
    const actionInput = document.getElementById('inlineAction');
    const addressIdInput = document.getElementById('inlineAddressId');
    
    const nameInput = document.getElementById('mRecipientName');
    const phoneInput = document.getElementById('mRecipientPhone');
    const detailInput = document.getElementById('mAddressDetail');
    const defaultInput = document.getElementById('mIsDefault');

    actionInput.value = mode;

    if (mode === 'add') {
        title.textContent = 'ThÃªm Ä‘á»‹a chá»‰ giao hÃ ng má»›i';
        addressIdInput.value = '';
        nameInput.value = '';
        phoneInput.value = '';
        detailInput.value = '';
        defaultInput.checked = (userAddresses.length === 0);
        defaultInput.disabled = (userAddresses.length === 0);
    } else {
        title.textContent = 'Cáº­p nháº­t Ä‘á»‹a chá»‰ giao hÃ ng';
        const addr = userAddresses.find(a => a.addressId === addressId);
        if (!addr) return;
        
        addressIdInput.value = addr.addressId;
        nameInput.value = addr.recipientName;
        phoneInput.value = addr.recipientPhone;
        detailInput.value = addr.addressDetail;
        defaultInput.checked = addr.isDefault;
        defaultInput.disabled = addr.isDefault; // If it's already default, cannot uncheck it
    }

    form.classList.remove('hidden');
    form.style.maxHeight = '500px';
    nameInput.focus();
}

function hideInlineAddressForm() {
    const form = document.getElementById('inlineAddressForm');
    form.style.maxHeight = '0px';
    setTimeout(() => {
        form.classList.add('hidden');
    }, 300);
}

function handleInlineAddressSubmit() {
    const mode = document.getElementById('inlineAction').value;
    const addressId = document.getElementById('inlineAddressId').value;
    const name = document.getElementById('mRecipientName').value.trim();
    const phone = document.getElementById('mRecipientPhone').value.trim();
    const detail = document.getElementById('mAddressDetail').value.trim();
    const isDefault = document.getElementById('mIsDefault').checked;

    if (name.length < 3) {
        alert('Há» vÃ  tÃªn ngÆ°á»i nháº­n pháº£i tá»« 3 kÃ½ tá»± trá»Ÿ lÃªn.');
        document.getElementById('mRecipientName').focus();
        return;
    }
    const phoneRegex = /^(0|\+84)[3|5|7|8|9][0-9]{8}$/;
    if (!phoneRegex.test(phone)) {
        alert('Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡ (pháº£i lÃ  sá»‘ Ä‘iá»‡n thoáº¡i Viá»‡t Nam gá»“m 10 chá»¯ sá»‘).');
        document.getElementById('mRecipientPhone').focus();
        return;
    }
    if (detail.length < 5) {
        alert('Äá»‹a chá»‰ chi tiáº¿t pháº£i tá»« 5 kÃ½ tá»± trá»Ÿ lÃªn.');
        document.getElementById('mAddressDetail').focus();
        return;
    }

    const btn = document.getElementById('btnSaveCheckoutAddress');
    btn.disabled = true;
    btn.classList.add('opacity-50');
    btn.textContent = 'Äang lÆ°u...';

    const params = new URLSearchParams({
        action: mode === 'edit' ? 'update' : mode,
        addressId: addressId,
        recipientName: name,
        recipientPhone: phone,
        addressDetail: detail,
        isDefault: isDefault ? 'true' : 'false',
        _csrf: '${sessionScope._csrfToken}'
    });

    fetch('${pageContext.request.contextPath}/api/address', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(r => r.json())
    .then(data => {
        btn.disabled = false;
        btn.classList.remove('opacity-50');
        btn.textContent = 'LÆ°u Ä‘á»‹a chá»‰';

        if (data.success) {
            const updatedAddr = {
                addressId: data.address.addressId,
                recipientName: data.address.recipientName,
                recipientPhone: data.address.recipientPhone,
                addressDetail: data.address.addressDetail,
                isDefault: data.address.isDefault
            };

            if (updatedAddr.isDefault) {
                // Set all other addresses isDefault to false
                userAddresses.forEach(a => a.isDefault = false);
            }

            if (mode === 'add') {
                userAddresses.push(updatedAddr);
            } else {
                const idx = userAddresses.findIndex(a => a.addressId === updatedAddr.addressId);
                if (idx !== -1) {
                    userAddresses[idx] = updatedAddr;
                }
            }

            // Restore elements in case it was the first address
            document.getElementById('selectedAddressCard').style.display = 'flex';
            document.getElementById('btnCancelInlineAddress').style.display = 'inline-block';
            document.getElementById('mIsDefault').disabled = false;

            selectAddress(updatedAddr.addressId);
            hideInlineAddressForm();
        } else {
            alert('Lá»—i: ' + data.error);
        }
    })
    .catch(err => {
        btn.disabled = false;
        btn.classList.remove('opacity-50');
        btn.textContent = 'LÆ°u Ä‘á»‹a chá»‰';
        console.error(err);
        alert('ÄÃ£ xáº£y ra lá»—i káº¿t ná»‘i.');
    });
}

function validateCheckoutForm() {
    const fullName = document.getElementById('fullName').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const deliveryAddress = document.getElementById('deliveryAddress').value.trim();

    if (userAddresses.length === 0) {
        alert('Vui lÃ²ng thÃªm Ä‘á»‹a chá»‰ nháº­n hÃ ng trÆ°á»›c khi thanh toÃ¡n.');
        showInlineAddressForm('add');
        return false;
    }

    if (!fullName || !phone || !deliveryAddress) {
        alert('Vui lÃ²ng chá»n hoáº·c Ä‘iá»n thÃ´ng tin Ä‘á»‹a chá»‰ giao hÃ ng há»£p lá»‡.');
        return false;
    }

    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.classList.add('opacity-50');
        submitBtn.innerHTML = 'Äang xá»­ lÃ½ Ä‘áº·t Ä‘Æ¡n... <span class="material-symbols-outlined animate-spin text-sm">sync</span>';
    }
    return true;
}

// Prefill default address on load if available
document.addEventListener('DOMContentLoaded', () => {
    initAddressWidget();
});
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
