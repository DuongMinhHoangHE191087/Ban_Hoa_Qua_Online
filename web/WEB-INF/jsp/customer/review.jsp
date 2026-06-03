<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Đánh giá sản phẩm đơn hàng #${order.orderId}" />
</jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
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
                "fontFamily": {
                    "label-md": ["Lexend"],
                    "headline-lg-mobile": ["Lexend"],
                    "headline-md": ["Lexend"],
                    "label-sm": ["Lexend"],
                    "display-lg": ["Lexend"],
                    "body-lg": ["Lexend"],
                    "headline-lg": ["Lexend"],
                    "body-md": ["Lexend"]
                }
            }
        }
    }
</script>

<style>
    .premium-glass-card {
        background: rgba(255, 255, 255, 0.85);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.5);
        box-shadow: 0 10px 30px -10px rgba(20, 83, 45, 0.05);
    }
    .star-rating {
        display: flex;
        flex-direction: row-reverse;
        justify-content: flex-end;
        gap: 6px;
    }
    .star-rating input {
        display: none;
    }
    .star-rating label {
        font-size: 2.25rem;
        color: #cbd5e1;
        cursor: pointer;
        transition: all 0.15s ease-in-out;
    }
    .star-rating input:checked ~ label,
    .star-rating label:hover,
    .star-rating label:hover ~ label {
        color: #fbbf24;
    }
    .star-rating label:active {
        transform: scale(0.9);
    }
</style>

<main class="max-w-3xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-on-background">
    <!-- Header -->
    <div class="mb-lg border-b border-surface-container-high pb-4">
        <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" class="text-on-surface-variant hover:text-primary transition-all text-sm font-semibold flex items-center gap-1 mb-2">
            <span class="material-symbols-outlined text-base">arrow_back</span> Quay lại chi tiết đơn hàng
        </a>
        <h1 class="font-display-lg text-2xl md:text-3xl text-inverse-surface font-bold">Đánh giá sản phẩm đơn hàng #${order.orderId}</h1>
        <p class="text-on-surface-variant text-sm mt-1">Cảm ơn bạn đã lựa chọn MetaFruit. Phản hồi của bạn giúp chúng tôi cải thiện tốt hơn.</p>
    </div>

    <!-- Alert Flash message -->
    <c:if test="${not empty sessionScope.flashMsg}">
        <div class="mb-6 p-4 rounded-xl flex items-center justify-between shadow-sm border ${sessionScope.flashType == 'success' ? 'bg-[#dcfce7] border-[#bbf7d0] text-emerald-800' : 'bg-error-container border-[#ffdad6] text-[#93000a]'}">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined">${sessionScope.flashType == 'success' ? 'check_circle' : 'error'}</span>
                <span class="font-semibold">${sessionScope.flashMsg}</span>
            </div>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <!-- List of unreviewed items -->
    <c:if test="${not empty unreviewedItems}">
        <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4">Sản phẩm chờ bạn đánh giá</h3>
        <div class="flex flex-col gap-6">
            <c:forEach var="item" items="${unreviewedItems}">
                <div class="premium-glass-card rounded-[1.5rem] p-6 flex flex-col gap-6">
                    
                    <!-- Item Header -->
                    <div class="flex justify-between items-start border-b border-outline-variant/20 pb-4 gap-4">
                        <div class="flex items-start gap-3">
                            <div class="w-12 h-12 rounded-xl bg-primary-container/40 flex items-center justify-center text-primary border border-primary-container">
                                <span class="material-symbols-outlined">shopping_basket</span>
                            </div>
                            <div>
                                <h4 class="font-bold text-inverse-surface text-base mb-1">${item.productNameSnapshot}</h4>
                                <span class="text-on-surface-variant text-xs font-semibold bg-surface-container-high px-2.5 py-1 rounded-md">Phân loại: ${item.variantLabelSnapshot}</span>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="text-on-surface-variant text-xs block mb-1">Đơn giá: <ft:currency value="${item.unitPrice}"/></span>
                            <span class="text-inverse-surface font-bold text-sm">Số lượng: ${item.quantity}</span>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/reviews" method="POST" class="flex flex-col gap-4">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        <input type="hidden" name="orderId" value="${order.orderId}">
                        <input type="hidden" name="orderItemId" value="${item.orderItemId}">

                        <!-- Star Rating with Real-time Text Label -->
                        <div>
                            <label class="block text-sm font-bold text-inverse-surface mb-2">Đánh giá chất lượng (Sao):</label>
                            <div class="flex items-center gap-4">
                                <div class="star-rating">
                                    <input type="radio" id="star5-${item.orderItemId}" name="rating" value="5" required onclick="updateRatingLabel(${item.orderItemId}, 5)" />
                                    <label for="star5-${item.orderItemId}" class="fa-solid fa-star hover:scale-110"></label>
                                    
                                    <input type="radio" id="star4-${item.orderItemId}" name="rating" value="4" onclick="updateRatingLabel(${item.orderItemId}, 4)" />
                                    <label for="star4-${item.orderItemId}" class="fa-solid fa-star hover:scale-110"></label>
                                    
                                    <input type="radio" id="star3-${item.orderItemId}" name="rating" value="3" onclick="updateRatingLabel(${item.orderItemId}, 3)" />
                                    <label for="star3-${item.orderItemId}" class="fa-solid fa-star hover:scale-110"></label>
                                    
                                    <input type="radio" id="star2-${item.orderItemId}" name="rating" value="2" onclick="updateRatingLabel(${item.orderItemId}, 2)" />
                                    <label for="star2-${item.orderItemId}" class="fa-solid fa-star hover:scale-110"></label>
                                    
                                    <input type="radio" id="star1-${item.orderItemId}" name="rating" value="1" onclick="updateRatingLabel(${item.orderItemId}, 1)" />
                                    <label for="star1-${item.orderItemId}" class="fa-solid fa-star hover:scale-110"></label>
                                </div>
                                <span id="rating-label-${item.orderItemId}" class="text-sm font-extrabold text-amber-500 transition-all duration-200"></span>
                            </div>
                        </div>

                        <!-- Image URL -->
                        <div>
                            <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="reviewImageUrl-${item.orderItemId}">Hình ảnh sản phẩm thực tế (URL):</label>
                            <input type="url" class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm transition-all" id="reviewImageUrl-${item.orderItemId}" name="reviewImageUrl" placeholder="https://example.com/fruit_image.jpg">
                            <p class="text-[10px] text-on-surface-variant mt-1">Dán liên kết hình ảnh trái cây tươi ngon bạn nhận được để minh họa thực tế.</p>
                        </div>

                        <!-- Review Text -->
                        <div>
                            <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="reviewText-${item.orderItemId}">Nhận xét chi tiết sản phẩm:</label>
                            <textarea class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm transition-all" id="reviewText-${item.orderItemId}" name="reviewText" rows="4" placeholder="Nhập cảm nhận của bạn về độ tươi, mùi vị, quy cách đóng gói hộp lạnh..." required></textarea>
                        </div>

                        <div class="text-end border-t border-outline-variant/20 pt-4">
                            <button type="submit" class="bg-primary text-on-primary hover:bg-inverse-surface px-6 py-3 rounded-xl font-bold transition-all shadow-md active:scale-95 transform flex items-center justify-center gap-1.5 ml-auto cursor-pointer">
                                <span class="material-symbols-outlined text-lg">send</span> Gửi đánh giá
                            </button>
                        </div>
                    </form>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <!-- List of already reviewed items -->
    <c:if test="${not empty reviewedItems}">
        <h3 class="font-headline-md text-lg text-on-surface-variant font-bold mb-4 mt-8">Sản phẩm đã đánh giá</h3>
        <div class="flex flex-col gap-4">
            <c:forEach var="item" items="${reviewedItems}">
                <div class="premium-glass-card rounded-[1.5rem] p-5 opacity-75 bg-slate-50/50 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400">
                            <span class="material-symbols-outlined">shopping_basket</span>
                        </div>
                        <div>
                            <h4 class="font-semibold text-on-surface-variant text-base mb-1 line-through">${item.productNameSnapshot}</h4>
                            <p class="text-on-surface-variant text-xs">Phân loại: ${item.variantLabelSnapshot}</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-1.5 text-primary bg-[#e6f7f0] px-3.5 py-1.5 rounded-full text-xs font-bold shadow-sm self-start sm:self-center">
                        <span class="material-symbols-outlined text-base">check_circle</span> Đã hoàn thành đánh giá
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${empty unreviewedItems && empty reviewedItems}">
        <div class="text-center py-12 premium-glass-card rounded-[1.5rem]">
            <span class="material-symbols-outlined text-4xl text-on-surface-variant opacity-60 mb-2">box_open</span>
            <p class="text-on-surface-variant">Không tìm thấy sản phẩm hợp lệ nào trong đơn hàng để đánh giá.</p>
        </div>
    </c:if>
</main>

<script>
function updateRatingLabel(orderItemId, val) {
    const labels = {
        1: 'Rất tệ 😡',
        2: 'Tệ 😞',
        3: 'Bình thường 🙂',
        4: 'Tốt 😍',
        5: 'Tuyệt vời! 🌟'
    };
    const el = document.getElementById('rating-label-' + orderItemId);
    if (el) {
        el.textContent = labels[val] || '';
        el.className = 'text-sm font-extrabold text-amber-500 animate-pulse';
        setTimeout(() => el.classList.remove('animate-pulse'), 300);
    }
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
