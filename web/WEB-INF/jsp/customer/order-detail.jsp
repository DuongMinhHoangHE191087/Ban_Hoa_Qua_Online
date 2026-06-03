<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Chi tiết đơn hàng #${order.orderId}" />
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
</style>

<main class="max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop py-xl font-body-md text-on-background">
    
    <!-- Flash Notifications -->
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

    <!-- Top Navigation and Actions -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-lg pb-4 border-b border-surface-container-high gap-4">
        <div>
            <a href="${pageContext.request.contextPath}/orders" class="text-on-surface-variant hover:text-primary transition-all text-sm font-semibold flex items-center gap-1 mb-2">
                <span class="material-symbols-outlined text-base">arrow_back</span> Quay lại danh sách đơn hàng
            </a>
            <h1 class="font-display-lg text-2xl md:text-3xl text-inverse-surface font-bold">Chi tiết đơn hàng #${order.orderId}</h1>
            <p class="text-on-surface-variant text-sm mt-1">Mã tham chiếu thanh toán & giao vận</p>
        </div>
        <div class="flex flex-wrap gap-2">
            <c:if test="${order.status == 'DELIVERED'}">
                <a href="${pageContext.request.contextPath}/orders?action=invoice&orderId=${order.orderId}" class="bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary px-5 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-sm flex items-center gap-2">
                    <span class="material-symbols-outlined text-lg">description</span> Xem Hóa Đơn Điện Tử (PDF)
                </a>
            </c:if>
        </div>
    </div>

    <!-- Timeline Progress Board -->
    <div class="premium-glass-card rounded-[1.5rem] p-6 mb-8">
        <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">analytics</span> Trạng thái hành trình đơn hàng
        </h3>
        
        <c:set var="stepNum" value="1" />
        <c:choose>
            <c:when test="${order.status == 'CANCELLED'}">
                <c:set var="stepNum" value="-1" />
            </c:when>
            <c:when test="${order.status == 'PENDING_PAYMENT' || order.status == 'CONFIRMED'}">
                <c:set var="stepNum" value="1" />
            </c:when>
            <c:when test="${order.status == 'APPROVED' || order.status == 'PREPARING'}">
                <c:set var="stepNum" value="2" />
            </c:when>
            <c:when test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
                <c:set var="stepNum" value="3" />
            </c:when>
            <c:when test="${order.status == 'DELIVERED'}">
                <c:set var="stepNum" value="4" />
            </c:when>
        </c:choose>

        <div class="relative py-8">
            <!-- Timeline connectors background -->
            <div class="absolute top-[50px] left-0 right-0 h-1.5 bg-outline-variant/30 rounded-full z-0"></div>
            <!-- Timeline active progress bar -->
            <div class="absolute top-[50px] left-0 h-1.5 bg-primary rounded-full z-0 transition-all duration-500" style="width: ${stepNum == -1 ? '100%' : (stepNum - 1) * 33.33}%"></div>
            
            <div class="flex justify-between items-center z-10 relative">
                <c:choose>
                    <c:when test="${stepNum == -1}">
                        <div class="flex flex-col items-center flex-1">
                            <div class="w-12 h-12 rounded-full bg-error text-on-error flex items-center justify-center border-4 border-white shadow-md">
                                <span class="material-symbols-outlined">cancel</span>
                            </div>
                            <span class="text-sm font-bold text-error mt-2">Đã Hủy Đơn Hàng</span>
                            <span class="text-xs text-on-surface-variant mt-1">Cập nhật: ${order.updatedAt}</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- Step 1 -->
                        <div class="flex flex-col items-center flex-1">
                            <div class="w-12 h-12 rounded-full flex items-center justify-center border-4 border-white shadow-md transition-all ${stepNum >= 1 ? (stepNum > 1 ? 'bg-primary text-on-primary' : 'bg-primary-container text-on-primary-container ring-4 ring-primary/20') : 'bg-outline-variant/30 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-lg">receipt_long</span>
                            </div>
                            <span class="text-xs md:text-sm font-bold mt-2 ${stepNum >= 1 ? 'text-primary' : 'text-on-surface-variant'}">Đặt hàng</span>
                            <span class="text-[10px] text-on-surface-variant">Thành công</span>
                        </div>
                        
                        <!-- Step 2 -->
                        <div class="flex flex-col items-center flex-1">
                            <div class="w-12 h-12 rounded-full flex items-center justify-center border-4 border-white shadow-md transition-all ${stepNum >= 2 ? (stepNum > 2 ? 'bg-primary text-on-primary' : 'bg-primary-container text-on-primary-container ring-4 ring-primary/20') : 'bg-outline-variant/30 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-lg">box_unpacking</span>
                            </div>
                            <span class="text-xs md:text-sm font-bold mt-2 ${stepNum >= 2 ? 'text-primary' : 'text-on-surface-variant'}">Chuẩn bị hàng</span>
                            <span class="text-[10px] text-on-surface-variant">Shop chuẩn bị lạnh</span>
                        </div>

                        <!-- Step 3 -->
                        <div class="flex flex-col items-center flex-1">
                            <div class="w-12 h-12 rounded-full flex items-center justify-center border-4 border-white shadow-md transition-all ${stepNum >= 3 ? (stepNum > 3 ? 'bg-primary text-on-primary' : 'bg-primary-container text-on-primary-container ring-4 ring-primary/20') : 'bg-outline-variant/30 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-lg">local_shipping</span>
                            </div>
                            <span class="text-xs md:text-sm font-bold mt-2 ${stepNum >= 3 ? 'text-primary' : 'text-on-surface-variant'}">Đang giao hàng</span>
                            <span class="text-[10px] text-on-surface-variant">Shipper hỏa tốc</span>
                        </div>

                        <!-- Step 4 -->
                        <div class="flex flex-col items-center flex-1">
                            <div class="w-12 h-12 rounded-full flex items-center justify-center border-4 border-white shadow-md transition-all ${stepNum >= 4 ? 'bg-primary text-on-primary' : 'bg-outline-variant/30 text-on-surface-variant'}">
                                <span class="material-symbols-outlined text-lg">task_alt</span>
                            </div>
                            <span class="text-xs md:text-sm font-bold mt-2 ${stepNum >= 4 ? 'text-primary' : 'text-on-surface-variant'}">Hoàn thành</span>
                            <span class="text-[10px] text-on-surface-variant">Đã ký nhận</span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <c:if test="${order.status == 'CANCELLED'}">
            <div class="mt-4 p-4 rounded-xl border border-error/20 bg-error-container/20 text-[#93000a] text-sm">
                <p class="font-bold flex items-center gap-1 mb-1">
                    <span class="material-symbols-outlined text-lg">warning</span> Thông tin hủy đơn hàng
                </p>
                <p><strong>Người thực hiện:</strong> ${order.cancelledBy == order.customerId ? 'Khách hàng' : 'Cửa hàng / Quản trị viên'}</p>
                <p><strong>Lý do hủy đơn:</strong> ${order.cancellationReason != null ? order.cancellationReason : 'Không có lý do chi tiết.'}</p>
            </div>
        </c:if>
    </div>

    <!-- Main Grid Content -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
        
        <!-- Left: Items list & pricing -->
        <div class="lg:col-span-8 flex flex-col gap-6">
            <!-- Items Card -->
            <div class="premium-glass-card rounded-[1.5rem] overflow-hidden">
                <div class="px-6 py-4 border-b border-outline-variant/30">
                    <h3 class="font-headline-md text-lg text-inverse-surface font-bold">Danh sách sản phẩm đã mua</h3>
                </div>
                <div class="divide-y divide-outline-variant/20">
                    <c:forEach var="item" items="${orderItems}">
                        <div class="p-6 flex items-center justify-between gap-4">
                            <div>
                                <h4 class="font-semibold text-inverse-surface text-base mb-1">${item.productNameSnapshot}</h4>
                                <span class="text-on-surface-variant text-xs font-medium bg-surface-container-high px-2.5 py-1 rounded-md">Phân loại: ${item.variantLabelSnapshot}</span>
                            </div>
                            <div class="text-end">
                                <span class="text-on-surface-variant text-xs block mb-1">Đơn giá: <ft:currency value="${item.unitPrice}"/></span>
                                <span class="text-inverse-surface font-bold"><ft:currency value="${item.subtotal}"/> <span class="text-on-surface-variant text-xs font-normal">x ${item.quantity}</span></span>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- Pricing summary card -->
            <div class="premium-glass-card rounded-[1.5rem] p-6">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4">Chi tiết thanh toán hóa đơn</h3>
                <div class="flex flex-col gap-3 font-body-md">
                    <div class="flex justify-between items-center text-on-surface-variant">
                        <span>Tiền hàng tạm tính</span>
                        <span class="text-inverse-surface font-semibold"><ft:currency value="${order.totalAmount}"/></span>
                    </div>
                    <div class="flex justify-between items-center text-on-surface-variant">
                        <span>Phí giao hàng lạnh</span>
                        <span class="text-inverse-surface font-semibold"><ft:currency value="${order.deliveryFee}"/></span>
                    </div>
                    <c:if test="${order.shopDiscountAmount > 0}">
                        <div class="flex justify-between items-center text-primary font-semibold">
                            <span>Mã giảm giá từ Shop</span>
                            <span>-<ft:currency value="${order.shopDiscountAmount}"/></span>
                        </div>
                    </c:if>
                    <c:if test="${order.systemDiscountAmount > 0}">
                        <div class="flex justify-between items-center text-primary font-semibold">
                            <span>Mã giảm giá từ Sàn (MetaFruit)</span>
                            <span>-<ft:currency value="${order.systemDiscountAmount}"/></span>
                        </div>
                    </c:if>
                    <hr class="border-outline-variant/30 my-2">
                    <div class="flex justify-between items-center font-bold text-lg">
                        <span class="text-inverse-surface">Khách hàng thực thanh toán</span>
                        <span class="text-[#ba1a1a] text-xl font-extrabold"><ft:currency value="${order.finalAmount}"/></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Shipping info & Action controls -->
        <div class="lg:col-span-4 flex flex-col gap-6">
            
            <!-- Shipping details card -->
            <div class="premium-glass-card rounded-[1.5rem] p-6">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold mb-4 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">local_shipping</span> Thông tin nhận hàng
                </h3>
                <div class="flex flex-col gap-3 text-sm">
                    <div>
                        <span class="text-on-surface-variant block text-xs font-semibold mb-1 uppercase tracking-wider">Địa chỉ giao hàng</span>
                        <p class="text-inverse-surface font-medium">${order.deliveryAddress}</p>
                    </div>
                    <div class="border-t border-outline-variant/20 pt-3">
                        <span class="text-on-surface-variant block text-xs font-semibold mb-1 uppercase tracking-wider">Phương thức thanh toán</span>
                        <p class="text-inverse-surface font-semibold">
                            <c:choose>
                                <c:when test="${order.paymentMethod == 'COD'}">Thanh toán khi nhận hàng (COD)</c:when>
                                <c:when test="${order.paymentMethod == 'CK'}">Chuyển khoản online / VietQR</c:when>
                                <c:otherwise>${order.paymentMethod}</c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                    <c:if test="${not empty order.notes}">
                        <div class="border-t border-outline-variant/20 pt-3">
                            <span class="text-on-surface-variant block text-xs font-semibold mb-1 uppercase tracking-wider">Ghi chú vận chuyển</span>
                            <p class="text-on-surface-variant small italic bg-slate-50 p-2.5 rounded-lg border border-outline-variant/10">${order.notes}</p>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Customer actions control card (DEL-03) -->
            <div class="premium-glass-card rounded-[1.5rem] p-6 flex flex-col gap-4">
                <h3 class="font-headline-md text-lg text-inverse-surface font-bold border-b border-outline-variant/20 pb-3 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">construction</span> Tương tác nghiệp vụ
                </h3>

                <!-- Nút tự hủy đơn hàng (Chỉ cho phép khi ở trạng thái PENDING hoặc CONFIRMED) -->
                <c:if test="${order.status == 'PENDING_PAYMENT' || order.status == 'CONFIRMED'}">
                    <button type="button" onclick="const f=document.getElementById('cancelFormContainer'); f.classList.toggle('hidden');" class="w-full bg-[#fef2f2] text-error hover:bg-[#ba1a1a] hover:text-white py-3.5 rounded-xl font-bold transition-all border border-error/20 flex items-center justify-center gap-2 text-sm shadow-sm active:scale-95 transform">
                        <span class="material-symbols-outlined">cancel</span> Yêu cầu hủy đơn hàng
                    </button>
                    
                    <div id="cancelFormContainer" class="hidden mt-2 p-4 border border-outline-variant/40 rounded-2xl bg-slate-50 flex flex-col gap-3">
                        <form action="${pageContext.request.contextPath}/orders" method="POST" class="flex flex-col gap-3">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <div>
                                <label class="block text-xs font-bold text-on-surface-variant mb-1">Vui lòng chọn lý do hủy đơn:</label>
                                <select class="w-full rounded-lg border border-outline-variant/40 p-2.5 bg-white focus:outline-none focus:ring-2 focus:ring-primary text-sm font-medium" name="reason" required>
                                    <option value="Tôi muốn thay đổi mặt hàng / số lượng mua">Tôi muốn thay đổi mặt hàng / số lượng mua</option>
                                    <option value="Tôi điền sai địa chỉ / thông tin nhận hàng">Tôi điền sai địa chỉ / thông tin nhận hàng</option>
                                    <option value="Tôi không muốn mua hàng nữa">Tôi không muốn mua hàng nữa</option>
                                    <option value="Khác">Lý do khác</option>
                                </select>
                            </div>
                            <button type="submit" class="w-full bg-error text-on-error py-2.5 rounded-xl font-bold text-sm shadow-md hover:bg-inverse-surface transition-all active:scale-95 transform" onclick="return confirm('Hành động này sẽ giải phóng toàn bộ số lượng sản phẩm đang giữ trong kho. Bạn chắc chắn muốn hủy?');">
                                Xác nhận hủy đơn
                            </button>
                        </form>
                    </div>
                </c:if>

                <!-- Nút xác nhận nhận hàng hỏa tốc -->
                <c:if test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
                    <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-full">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        <input type="hidden" name="action" value="confirmDelivery">
                        <input type="hidden" name="orderId" value="${order.orderId}">
                        <button type="submit" class="w-full bg-primary text-on-primary hover:bg-inverse-surface py-4 rounded-xl font-bold transition-all shadow-md flex items-center justify-center gap-2 active:scale-95 transform" onclick="return confirm('Nhấn xác nhận khi bạn đã nhận được gói hàng tươi ngon và kiểm tra đúng số lượng.');">
                            <span class="material-symbols-outlined">verified</span> Đã nhận được hàng
                        </button>
                    </form>
                    <p class="text-[10px] text-on-surface-variant text-center opacity-85 mt-1">Vui lòng chỉ bấm khi đã kiểm tra chất lượng trái cây.</p>
                </c:if>

                <!-- Đã giao hàng thành công (DELIVERED) -> Khách hàng được quyền đánh giá hoặc yêu cầu đổi trả -->
                <c:if test="${order.status == 'DELIVERED'}">
                    <div class="flex flex-col gap-3 w-full">
                        <a href="${pageContext.request.contextPath}/reviews?orderId=${order.orderId}" class="w-full bg-primary text-on-primary hover:bg-inverse-surface py-3.5 rounded-xl font-bold transition-all shadow-md text-center flex items-center justify-center gap-2 text-sm">
                            <span class="material-symbols-outlined">star</span> Viết đánh giá sản phẩm
                        </a>
                        
                        <a href="${pageContext.request.contextPath}/returns?orderId=${order.orderId}" class="w-full bg-[#fff5f5] text-error hover:bg-[#ba1a1a] hover:text-white py-3.5 rounded-xl font-bold border border-error/20 text-center flex items-center justify-center gap-2 text-sm">
                            <span class="material-symbols-outlined">rotate_left</span> Yêu cầu Đổi / Trả hàng
                        </a>
                    </div>
                </c:if>

                <c:if test="${order.status == 'CANCELLED'}">
                    <div class="text-center p-4 border border-outline-variant/20 rounded-xl bg-slate-50 text-on-surface-variant text-xs font-semibold">
                        <span class="material-symbols-outlined text-lg mb-1 block">info</span> Đơn hàng đã dừng hoạt động. Không có thao tác khả dụng.
                    </div>
                </c:if>
            </div>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
