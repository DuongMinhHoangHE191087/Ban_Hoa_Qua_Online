<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Thanh toán - Verdant Market"/></jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined cho phong cách Verdant Clarity -->
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
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
        <%-- MÀN HÌNH ĐẶT HÀNG THÀNH CÔNG --%>
        <c:when test="${not empty isSuccess}">
            <div class="max-w-2xl mx-auto text-center py-16 glass-card rounded-xl border border-white/50 p-8 flex flex-col items-center gap-6 mt-12 shadow-xl">
                <!-- Checkmark Icon Animated -->
                <div class="w-20 h-20 rounded-full bg-emerald-100 flex items-center justify-center text-primary border border-emerald-200 shadow-md">
                    <span class="material-symbols-outlined text-[48px] font-bold">verified</span>
                </div>
                
                <div>
                    <h1 class="text-3xl font-bold text-inverse-surface tracking-tight">Đặt Hàng Thành Công!</h1>
                    <p class="text-on-surface-variant mt-3 text-sm leading-relaxed max-w-md mx-auto">
                        Cảm ơn bạn đã mua nông sản sạch tại <strong>Verdant Market</strong>. Đơn hàng của bạn đã được chuyển tới nhà vườn đóng gói lạnh giao hỏa tốc!
                    </p>
                </div>

                <div class="w-full bg-[#d1ffd8]/60 border border-[#bcfdc9] rounded-2xl p-5 text-start text-xs text-on-surface-variant space-y-2">
                    <div class="flex justify-between border-b border-emerald-100/30 pb-2">
                        <span>Trạng thái thanh toán</span>
                        <span class="text-primary font-bold">Thành công (COD / Chờ xử lý)</span>
                    </div>
                    <div class="flex justify-between border-b border-emerald-100/30 pb-2">
                        <span>Mã đơn hàng</span>
                        <span class="text-inverse-surface font-bold">#<c:out value="${param.orderId}"/></span>
                    </div>
                    <div class="flex justify-between">
                        <span>Dự kiến giao hàng</span>
                        <span class="text-inverse-surface font-bold">Trong 2 - 3 tiếng (Giao Hỏa Tốc)</span>
                    </div>
                </div>

                <div class="flex flex-col sm:flex-row gap-4 w-full justify-center">
                    <a href="${pageContext.request.contextPath}/home" class="bg-[#14532D] text-white font-bold py-3.5 px-8 rounded-lg hover:bg-opacity-90 transition-all shadow-md active:scale-95 text-sm flex items-center justify-center gap-2 cursor-pointer">
                        <span class="material-symbols-outlined text-lg">shopping_basket</span>
                        <span>Tiếp tục mua sắm</span>
                    </a>
                </div>
            </div>

            <%-- CRITICAL SCRIPT: RESET GIỎ HÀNG LOCAL STORAGE CHỌN LỌC --%>
            <%-- [FIX] Đọc purgedVariantIds từ session attribute (không lộ trên URL) --%>
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
                    } else {
                        // Fallback: xóa toàn bộ
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

        <%-- FORM THANH TOÁN chuẩn mẫu Check_Out_UI.html --%>
        <c:otherwise>
            <div class="mb-8 flex items-baseline justify-between border-b border-[#b1f2be] pb-4">
                <div>
                    <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-primary mb-2">Hoàn tất đơn hàng</h1>
                    <p class="font-body-md text-body-md text-on-surface-variant">Vui lòng kiểm tra lại thông tin và xác nhận thanh toán.</p>
                </div>
                <a href="${pageContext.request.contextPath}/cart" class="text-xs font-bold text-primary flex items-center gap-1 hover:underline">
                    <span class="material-symbols-outlined text-[16px]">arrow_back</span>
                    Quay lại giỏ hàng
                </a>
            </div>

            <form action="${pageContext.request.contextPath}/checkout" method="post" class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="variantIds" value="<c:out value="${param.variantIds}"/>">

                <!-- LEFT COLUMN: Forms -->
                <div class="lg:col-span-8 flex flex-col gap-8">
                    <!-- Section 1: Delivery Info -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center gap-3 mb-6 text-primary border-b border-[#b1f2be] pb-3">
                            <span class="material-symbols-outlined text-2xl">local_shipping</span>
                            <h2 class="font-headline-md text-headline-md font-bold">Thông tin giao hàng</h2>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div class="flex flex-col gap-2">
                                <label class="font-label-md text-label-md text-[#14532D] font-bold" for="fullName">Họ và tên</label>
                                <input class="form-input rounded-lg px-4 py-3 font-body-md text-body-md w-full" id="fullName" name="fullName"
                                       value="<c:out value="${sessionScope.currentUser.fullName}"/>" placeholder="Nhập họ và tên" required type="text">
                            </div>
                            <div class="flex flex-col gap-2">
                                <label class="font-label-md text-label-md text-[#14532D] font-bold" for="phone">Số điện thoại</label>
                                <input class="form-input rounded-lg px-4 py-3 font-body-md text-body-md w-full" id="phone" name="phone"
                                       value="<c:out value="${sessionScope.currentUser.phone}"/>" placeholder="Nhập số điện thoại" required type="tel">
                            </div>
                            <div class="flex flex-col gap-2 md:col-span-2">
                                <label class="font-label-md text-label-md text-[#14532D] font-bold" for="deliveryAddress">Địa chỉ giao hàng chi tiết</label>
                                <input class="form-input rounded-lg px-4 py-3 font-body-md text-body-md w-full" id="deliveryAddress" name="deliveryAddress"
                                       value="<c:out value="${sessionScope.currentUser.userAddress}"/>" placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố" required type="text">
                            </div>
                            <div class="flex flex-col gap-2 md:col-span-2">
                                <label class="font-label-md text-label-md text-[#14532D]" for="notes">Ghi chú (Tuỳ chọn)</label>
                                <textarea class="form-input rounded-lg px-4 py-3 font-body-md text-body-md w-full" id="notes" name="notes" placeholder="Ghi chú thêm cho người giao hàng..." rows="3"></textarea>
                            </div>
                        </div>
                    </section>

                    <!-- Section 2: Payment Methods -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center gap-3 mb-6 text-primary border-b border-[#b1f2be] pb-3">
                            <span class="material-symbols-outlined text-2xl">payments</span>
                            <h2 class="font-headline-md text-headline-md font-bold">Phương thức thanh toán</h2>
                        </div>
                        <div class="flex flex-col gap-4">
                            <!-- Option 1: COD -->
                            <label class="flex items-start gap-4 p-4 rounded-lg border border-outline-variant hover:border-primary bg-white/40 cursor-pointer transition-colors">
                                <div class="flex items-center h-6">
                                    <input checked="" class="custom-radio w-5 h-5 text-primary focus:ring-primary border-[#14532D]" name="paymentMethod" type="radio" value="COD">
                                </div>
                                <div class="flex flex-col">
                                    <span class="font-label-md text-label-md text-on-surface font-bold">Thanh toán khi nhận hàng (COD)</span>
                                    <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">Thanh toán bằng tiền mặt khi giao hàng tận nơi.</span>
                                </div>
                                <span class="material-symbols-outlined ml-auto text-primary opacity-70">local_atm</span>
                            </label>
                            
                            <!-- Option 2: Bank QR -->
                            <label class="flex items-start gap-4 p-4 rounded-lg border border-outline-variant hover:border-primary bg-white/40 cursor-pointer transition-colors">
                                <div class="flex items-center h-6">
                                    <input class="custom-radio w-5 h-5 text-primary focus:ring-primary border-[#14532D]" name="paymentMethod" type="radio" value="CK">
                                </div>
                                <div class="flex flex-col">
                                    <span class="font-label-md text-label-md text-on-surface font-bold">Chuyển khoản QR ngân hàng</span>
                                    <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">Thông tin và mã QR chuyển khoản sẽ hiển thị sau khi đặt hàng.</span>
                                </div>
                                <span class="material-symbols-outlined ml-auto text-primary opacity-70">account_balance</span>
                            </label>
                        </div>
                    </section>

                    <!-- Section 3: Order Items List -->
                    <section class="glass-card rounded-xl p-6 md:p-8">
                        <div class="flex items-center justify-between mb-6 border-b border-[#b1f2be] pb-3">
                            <div class="flex items-center gap-3 text-primary">
                                <span class="material-symbols-outlined text-2xl">shopping_basket</span>
                                <h2 class="font-headline-md text-headline-md font-bold">Sản phẩm trong đơn</h2>
                            </div>
                            <a class="font-label-md text-label-md text-primary hover:underline flex items-center gap-1 font-semibold text-sm" href="${pageContext.request.contextPath}/cart">
                                Sửa giỏ hàng <span class="material-symbols-outlined text-sm">edit</span>
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
                                        <span class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">Biến thể: <strong class="text-primary"><c:out value="${item.variantLabel}"/></strong></span>
                                        <span class="font-body-md text-body-md text-on-surface-variant text-xs mt-0.5">Trọng lượng: <c:out value="${item.weightKg}"/> kg</span>
                                    </div>
                                    <div class="text-right">
                                        <span class="font-label-md text-label-md text-primary font-bold text-lg block"><ft:currency value="${item.price}"/></span>
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
                        <h2 class="font-headline-md text-headline-md text-primary mb-6 font-bold border-b border-[#b1f2be] pb-3">Tổng kết đơn hàng</h2>
                        <div class="flex flex-col gap-4 font-body-md text-body-md text-on-surface mb-6">
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Tạm tính (<c:out value="${fn:length(cartSummary.items)}"/> sản phẩm)</span>
                                <span class="font-bold text-inverse-surface"><ft:currency value="${cartSummary.subtotal}"/></span>
                            </div>
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Tổng trọng lượng</span>
                                <span class="font-bold text-inverse-surface"><c:out value="${cartSummary.totalWeight}"/> kg</span>
                            </div>
                            <div class="flex justify-between items-center">
                                <span class="text-on-surface-variant">Phí vận chuyển (Giao hỏa tốc)</span>
                                <span class="font-bold text-inverse-surface"><ft:currency value="${cartSummary.deliveryFee}"/></span>
                            </div>
                        </div>
                        <div class="border-t border-[#BBF7D0] pt-4 mb-8">
                            <div class="flex justify-between items-end">
                                <span class="font-label-md text-label-md text-on-surface font-bold">Tổng cộng</span>
                                <span class="font-headline-md text-headline-md text-primary font-black text-2xl"><ft:currency value="${cartSummary.total}"/></span>
                            </div>
                            <span class="block text-right text-xs text-on-surface-variant mt-1">(Đã bao gồm VAT & Cước bảo ôn cold-chain)</span>
                        </div>
                        <button type="submit" class="w-full py-4 rounded-lg bg-[#14532D] text-white font-label-md text-label-md flex justify-center items-center gap-2 hover:bg-opacity-90 transition-all font-bold cursor-pointer active:scale-95 shadow-md">
                            Đặt hàng ngay
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </button>
                        <p class="text-center text-xs text-on-surface-variant mt-4 font-body-md flex items-center justify-center gap-1 opacity-80">
                            <span class="material-symbols-outlined text-sm">shield</span>
                            Thông tin của bạn được bảo mật tuyệt đối
                        </p>
                    </div>
                </div>
            </form>
        </c:otherwise>
    </c:choose>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
