<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Yêu cầu đổi trả hàng đơn hàng #${order.orderId}" />
</jsp:include>

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

<main class="max-w-3xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-on-background">
    <!-- Header -->
    <div class="mb-lg border-b border-surface-container-high pb-4">
        <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" class="text-on-surface-variant hover:text-primary transition-all text-sm font-semibold flex items-center gap-1 mb-2">
            <span class="material-symbols-outlined text-base">arrow_back</span> Quay lại chi tiết đơn hàng
        </a>
        <h1 class="font-display-lg text-2xl md:text-3xl text-inverse-surface font-bold">Yêu cầu Đổi / Trả / Hoàn tiền</h1>
        <p class="text-on-surface-variant text-sm mt-1">Chúng tôi cam kết bảo vệ quyền lợi người tiêu dùng tối đa. Hồ sơ sẽ được giải quyết nhanh trong vòng 24h.</p>
    </div>



    <!-- Order summary card -->
    <div class="premium-glass-card rounded-[1.5rem] p-6 mb-6">
        <h3 class="font-headline-md text-base text-inverse-surface font-bold mb-3">Tóm tắt thông tin đơn hàng</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
            <div>
                <p class="text-on-surface-variant"><strong class="text-inverse-surface">Mã đơn hàng:</strong> #${order.orderId}</p>
                <p class="text-on-surface-variant mt-1"><strong class="text-inverse-surface">Ngày đặt:</strong> ${order.createdAt}</p>
            </div>
            <div class="sm:text-end">
                <p class="text-on-surface-variant"><strong class="text-inverse-surface">Tổng tiền thanh toán:</strong> <span class="text-[#d32f2f] font-bold"><ft:currency value="${order.finalAmount}"/></span></p>
                <p class="text-on-surface-variant mt-1"><strong class="text-inverse-surface">Phương thức:</strong> <span class="bg-[#dcfce7] text-[#15803d] px-2 py-0.5 rounded-full text-xs font-bold">${order.paymentMethod}</span></p>
            </div>
        </div>
    </div>

    <!-- Products list reference -->
    <div class="premium-glass-card rounded-[1.5rem] overflow-hidden mb-6">
        <div class="px-6 py-4 border-b border-outline-variant/30">
            <h3 class="font-headline-md text-base text-inverse-surface font-bold">Danh sách sản phẩm trong đơn</h3>
        </div>
        <div class="divide-y divide-outline-variant/20">
            <c:forEach var="item" items="${orderItems}">
                <div class="p-4 flex items-center justify-between gap-4">
                    <div>
                        <h4 class="font-semibold text-inverse-surface text-sm mb-1">${item.productNameSnapshot}</h4>
                        <span class="text-on-surface-variant text-[10px] font-medium bg-surface-container-high px-2 py-0.5 rounded">Phân loại: ${item.variantLabelSnapshot}</span>
                    </div>
                    <div class="text-end text-sm">
                        <span class="text-on-surface-variant text-xs block">Số lượng đã mua: <strong class="text-inverse-surface">${item.quantity}</strong></span>
                        <span class="text-on-surface-variant text-xs">Đơn giá: <ft:currency value="${item.unitPrice}"/></span>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Form container -->
    <div class="premium-glass-card rounded-[1.5rem] p-6">
        <form action="${pageContext.request.contextPath}/returns" method="POST" enctype="multipart/form-data" class="flex flex-col gap-4">
            <input type="hidden" name="orderId" value="${order.orderId}"/>
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}"/>

            <!-- Product select -->
            <div>
                <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="orderItemId">Chọn sản phẩm muốn đổi trả <span class="text-error">*</span></label>
                <select class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm font-semibold" id="orderItemId" name="orderItemId" required onchange="updateMaxQuantity()">
                    <option value="" disabled selected>-- Chọn sản phẩm --</option>
                    <c:forEach var="item" items="${orderItems}">
                        <option value="${item.orderItemId}" data-qty="${item.quantity}">
                            <c:out value="${item.productNameSnapshot}"/> (<c:out value="${item.variantLabelSnapshot}"/>) - Số lượng: <c:out value="${item.quantity}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <!-- Request Type -->
                <div>
                    <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="requestType">Loại yêu cầu <span class="text-error">*</span></label>
                    <select class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm font-semibold" id="requestType" name="requestType" required>
                        <option value="RETURN">Trả hàng & Hoàn tiền (Refund)</option>
                        <option value="EXCHANGE">Yêu cầu đổi sản phẩm (Exchange)</option>
                    </select>
                </div>

                <!-- Reason Code -->
                <div>
                    <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="reasonCode">Lý do đổi trả <span class="text-error">*</span></label>
                    <select class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm font-semibold" id="reasonCode" name="reasonCode" required>
                        <option value="WRONG_ITEM">Giao sai sản phẩm / loại quả</option>
                        <option value="DAMAGED">Sản phẩm bị dập nát / hư hỏng do giao nhận</option>
                        <option value="MISSING_ITEM">Giao thiếu số lượng / quà tặng kèm</option>
                        <option value="LATE_DELIVERY">Giao hàng quá trễ so với cam kết hỏa tốc</option>
                        <option value="NOT_AS_DESCRIBED">Trái cây không giống hình ảnh/mô tả</option>
                        <option value="OTHER">Lý do cá nhân khác</option>
                    </select>
                </div>
            </div>

            <!-- Requested Quantity -->
            <div>
                <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="requestedQuantity">
                    Số lượng đổi trả <span class="text-error">*</span> <span id="maxQtyLabel" class="font-normal text-error text-xs ml-1"></span>
                </label>
                <input type="number" class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm" id="requestedQuantity" name="requestedQuantity" value="1" min="1" required/>
                <p class="text-[10px] text-on-surface-variant mt-1">Không được vượt quá số lượng sản phẩm bạn đã mua thực tế.</p>
            </div>

            <!-- Evidence upload -->
            <div>
                <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="evidence">Hình ảnh/Video bằng chứng lỗi <span class="text-error">*</span></label>
                <input type="file" class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm" id="evidence" name="evidence" accept="image/*,video/*" multiple required/>
                <p class="text-[10px] text-on-surface-variant mt-1">Tải lên ít nhất 1 video hoặc ít nhất 2 ảnh để hệ thống đối soát phê duyệt. Có thể chọn nhiều tệp cùng lúc.</p>
            </div>

            <!-- Description -->
            <div>
                <label class="block text-sm font-bold text-inverse-surface mb-1.5" for="description">Mô tả chi tiết sự việc <span class="text-error">*</span></label>
                <textarea class="w-full rounded-xl border border-outline-variant/40 p-3 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm" id="description" name="description" rows="4" placeholder="Vui lòng cung cấp thêm thông tin chi tiết về tình trạng trái cây lúc nhận hàng để chúng tôi xử lý..." required></textarea>
            </div>

            <div class="text-end border-t border-outline-variant/20 pt-4 mt-2">
                <button type="submit" class="bg-primary text-on-primary hover:bg-inverse-surface px-8 py-3.5 rounded-xl font-bold transition-all shadow-md active:scale-95 transform flex items-center justify-center gap-1 ml-auto">
                    <span class="material-symbols-outlined text-lg">check_circle</span> Gửi yêu cầu đổi trả
                </button>
            </div>
        </form>
    </div>
</main>

<script>
function updateMaxQuantity() {
    var select = document.getElementById("orderItemId");
    var selectedOption = select.options[select.selectedIndex];
    var maxQty = selectedOption.getAttribute("data-qty");
    
    var qtyInput = document.getElementById("requestedQuantity");
    qtyInput.max = maxQty;
    if (parseInt(qtyInput.value) > parseInt(maxQty)) {
        qtyInput.value = maxQty;
    }
    
    var maxLabel = document.getElementById("maxQtyLabel");
    if (maxLabel) {
        maxLabel.innerText = "(Số lượng mua tối đa: " + maxQty + ")";
    }
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
