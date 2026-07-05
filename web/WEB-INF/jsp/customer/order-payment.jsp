<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quét mã QR Thanh toán - MetaFruit"/></jsp:include>

<!-- Tích hợp Tailwind CSS CDN, Lexend Font và Material Symbols Outlined -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
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

<div class="pt-24 pb-12 px-margin-mobile md:px-margin-desktop max-w-5xl mx-auto font-sans antialiased text-on-background bg-[#eaffea] min-h-screen">
    <input type="hidden" id="js-qr-expire-min" value="${qrExpireMin != null ? qrExpireMin : 10}">
    <input type="hidden" id="js-order-id" value="<c:out value='${order.orderId}'/>">
    <input type="hidden" id="js-reference" value="<c:out value='${reference}'/>">
    <input type="hidden" id="js-amount-formatted" value="<c:out value='${amountFormatted}'/>">
    <input type="hidden" id="js-context-path" value="<c:out value='${pageContext.request.contextPath}'/>">
    
    <div class="mb-8 flex items-baseline justify-between border-b border-[#b1f2be] pb-4">
        <div>
            <h1 class="text-3xl font-extrabold text-primary mb-2">Thanh toán đơn hàng</h1>
            <p class="text-sm text-on-surface-variant">Quét mã VietQR chuyển khoản nhanh bằng ví điện tử hoặc ứng dụng Ngân hàng của bạn.</p>
        </div>
        <a href="${pageContext.request.contextPath}/orders" class="text-xs font-bold text-primary flex items-center gap-1 hover:underline">
            <span class="material-symbols-outlined text-[16px]">arrow_back</span>
            Danh sách đơn hàng
        </a>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
        
        <!-- LEFT: QR Code Scan Container -->
        <div class="lg:col-span-5 flex flex-col gap-6">
            <div class="glass-card rounded-2xl p-6 text-center flex flex-col items-center gap-4 shadow-xl border border-white/60">
                
                <!-- Countdown Timer -->
                <div class="w-full bg-[#ffdad6]/60 border border-[#ffb4ab] text-[#93000a] px-4 py-2.5 rounded-lg flex items-center justify-center gap-2 text-sm font-bold shadow-sm">
                    <span class="material-symbols-outlined text-lg animate-spin">hourglass_empty</span>
                    <span>Mã QR hết hạn trong: <span id="countdown">10:00</span></span>
                </div>

                <!-- VietQR Image Container -->
                <div class="relative w-72 h-72 bg-white rounded-xl border border-emerald-100 p-2 shadow-inner flex items-center justify-center overflow-hidden">
                    <img src="${qrUrl}" alt="VietQR Vietcombank Premium" class="w-full h-full object-contain" id="qr-img">
                    <div id="qr-overlay" class="absolute inset-0 bg-white/95 backdrop-blur-sm flex flex-col items-center justify-center gap-3 hidden text-center p-4">
                        <span class="material-symbols-outlined text-red-600 text-[56px] font-bold">cancel</span>
                        <p class="text-sm font-extrabold text-[#93000a]">Mã QR Đã Hết Hạn</p>
                        <p class="text-xs text-on-surface-variant leading-relaxed">Vui lòng quay lại danh sách đơn hàng để kiểm tra hoặc đặt hàng mới.</p>
                    </div>
                </div>

                <div class="flex items-center gap-2 text-xs text-on-surface-variant font-medium mt-1">
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-ping"></span>
                    <span>Đang chờ hệ thống ghi nhận thanh toán tự động...</span>
                </div>

                <!-- Nút Làm mới QR khi hết hạn -->
                <form id="renew-qr-form" action="${pageContext.request.contextPath}/checkout" method="get" class="hidden w-full mt-2">
                    <input type="hidden" name="action" value="payment"/>
                    <input type="hidden" name="orderId" value="${order.orderId}"/>
                    <button type="submit" class="w-full bg-amber-500 hover:bg-amber-600 text-white font-bold py-2.5 px-4 rounded-xl flex items-center justify-center gap-2 transition-colors cursor-pointer">
                        <span class="material-symbols-outlined text-lg">refresh</span>
                        Làm mới mã QR
                    </button>
                </form>
            </div>
            
            <!-- Dev Helper Tools (Simulated Payment) -->
            <div class="glass-card rounded-2xl p-5 border border-amber-200 bg-amber-50/40 text-xs">
                <span class="font-bold text-amber-800 flex items-center gap-1 mb-2">
                    <span class="material-symbols-outlined text-sm">construction</span>
                    DEVELOPMENT TESTING - Webhook SePay:
                </span>
                <p class="text-on-surface-variant mb-3 leading-relaxed">
                    Dùng nút này để mô phỏng webhook SePay. Trạng thái đơn chỉ được xem là hoàn tất khi polling nhận được trạng thái đơn hàng mới.
                </p>
                <div class="flex gap-2">
                    <button onclick="simulateSuccessRedirect()" class="bg-[#14532D] text-white px-3 py-1.5 rounded font-bold hover:bg-opacity-90 transition-all cursor-pointer">
                        Mô phỏng webhook SePay
                    </button>
                </div>
            </div>
        </div>

        <!-- RIGHT: Bank Account Transfer Details -->
        <div class="lg:col-span-7 flex flex-col gap-6">
            <div class="glass-card rounded-2xl p-6 md:p-8 shadow-xl border border-white/60">
                <h3 class="text-lg font-bold text-[#00210d] mb-4 border-b border-[#b1f2be] pb-3 flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary text-xl">account_balance</span>
                    Thông tin chuyển khoản thủ công
                </h3>
                
                <div class="space-y-4 font-sans text-sm">
                    <!-- Bank Name -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-emerald-100/50 hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Ngân hàng thụ hưởng</span>
                            <span class="font-extrabold text-[#00210d]">Ngân hàng Quân Đội (MB Bank)</span>
                        </div>
                        <button onclick="copyText(this.dataset.text, this)" data-text="Ngân hàng Quân Đội (MB Bank)" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Account No -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-emerald-100/50 hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Số tài khoản</span>
                            <span class="font-extrabold text-[#00210d] text-base select-all tracking-wider"><c:out value="${accountNo}"/></span>
                        </div>
                        <button onclick="copyText(this.dataset.text, this)" data-text="<c:out value='${accountNo}'/>" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Account Holder -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-emerald-100/50 hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Chủ tài khoản thụ hưởng</span>
                            <span class="font-extrabold text-[#00210d] select-all"><c:out value="${accountName}"/></span>
                        </div>
                        <button onclick="copyText(this.dataset.text, this)" data-text="<c:out value='${accountName}'/>" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Amount -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-[#ffdad6] hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Số tiền chính xác</span>
                            <span class="font-black text-[#ba1a1a] text-lg select-all"><ft:currency value="${order.finalAmount}"/></span>
                        </div>
                        <button onclick="copyText(this.dataset.text, this)" data-text="<c:out value='${amountFormatted}'/>" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Transfer Message (Critical) -->
                    <div class="flex justify-between items-center bg-yellow-50/50 p-4 rounded-lg border border-yellow-200/80 hover:bg-yellow-50 transition-colors">
                        <div>
                            <span class="text-xs text-amber-800 font-bold block mb-0.5 flex items-center gap-1">
                                <span class="material-symbols-outlined text-sm">warning</span>
                                Nội dung chuyển khoản bắt buộc
                            </span>
                            <span class="font-black text-amber-900 text-base select-all tracking-wider font-mono"><c:out value="${description}"/></span>
                        </div>
                        <button onclick="copyText(this.dataset.text, this)" data-text="<c:out value='${description}'/>" class="text-amber-800 hover:text-opacity-80 p-1.5 flex items-center justify-center rounded hover:bg-yellow-100 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[20px] font-bold">content_copy</span>
                        </button>
                    </div>
                </div>

                <div class="bg-[#d1ffd8]/40 border border-[#bcfdc9] rounded-xl p-4 mt-6 text-xs text-on-surface-variant leading-relaxed space-y-2">
                    <span class="font-bold text-[#14532D] block">Lưu ý quan trọng:</span>
                    <ul class="list-disc pl-4 space-y-1 text-on-surface-variant">
                        <li>Vui lòng chuyển tiền đúng <strong>Số tài khoản</strong> và chính xác <strong>Số tiền</strong> ở trên.</li>
                        <li><strong>Nội dung chuyển khoản</strong> cần điền chính xác mã <code class="bg-[#bcfdc9] px-1 py-0.5 rounded font-bold">${description}</code> để được khớp lệnh tự động hóa trong 30 giây.</li>
                        <li>Tránh nhập thêm các từ khóa như "mua", "thanh toan" để tránh lỗi xử lý webhook.</li>
                    </ul>
                </div>

                <!-- Phân cách -->
                <div class="border-t border-[#b1f2be] mt-6 pt-6">

                    <!-- Trạng thái đang chờ xác nhận -->
                    <c:if test="${paymentTx != null && paymentTx.status == 'processing'}">
                        <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-800 flex items-start gap-3 mb-4">
                            <span class="material-symbols-outlined text-blue-500 text-xl flex-shrink-0">schedule</span>
                            <div>
                                <strong>Đang xác minh thanh toán</strong><br/>
                                Chúng tôi đang kiểm tra giao dịch của bạn. Vui lòng đợi 1–24 giờ làm việc.
                            </div>
                        </div>
                    </c:if>

                    <!-- Nút chính: Tôi đã thanh toán -->
                    <c:if test="${paymentTx == null || paymentTx.status == 'pending' || paymentTx.status == 'processing'}">
                        <form action="${pageContext.request.contextPath}/checkout" method="post" id="confirm-payment-form">
                            <input type="hidden" name="action" value="confirmPayment"/>
                            <input type="hidden" name="orderId" value="${order.orderId}"/>
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}"/>
                            <button type="submit" id="confirm-btn"
                                class="w-full bg-[#14532D] hover:bg-[#0d3d20] text-white font-bold py-3.5 px-6 rounded-xl flex items-center justify-center gap-2.5 text-base transition-all shadow-lg hover:shadow-xl cursor-pointer"
                                onclick="return confirmPaymentClick(this)">
                                <span class="material-symbols-outlined text-xl">payment</span>
                                Tôi đã thanh toán
                            </button>
                        </form>
                        <p class="text-xs text-on-surface-variant text-center mt-2">
                            Bấm sau khi bạn đã chuyển khoản xong. Admin sẽ xác minh và duyệt trong 1–24 giờ.
                        </p>
                    </c:if>

                </div>
            </div>
        </div>

</div>

<!-- Success Modal (Thanh toán thành công) -->
<div id="payment-success-modal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm hidden opacity-0 transition-all duration-300">
    <div class="glass-card max-w-md w-full p-8 rounded-2xl text-center shadow-2xl border border-white/80 transform scale-95 transition-all duration-300 flex flex-col items-center gap-5 bg-white">
        <div class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-600 animate-bounce">
            <span class="material-symbols-outlined text-[40px] font-bold">check_circle</span>
        </div>
        <h3 class="text-2xl font-black text-[#00210d]">Thanh toán thành công!</h3>
        <p class="text-sm text-on-surface-variant leading-relaxed">
            Hệ thống đã ghi nhận thanh toán của bạn thành công. Đơn hàng đang được chuẩn bị để giao tới bạn sớm nhất!
        </p>
        <div class="text-xs text-emerald-700 bg-emerald-50 py-1.5 px-3 rounded-full font-bold">
            Tự động chuyển hướng sau <span id="success-countdown">3</span> giây...
        </div>
        <a href="${pageContext.request.contextPath}/profile?tab=orders" class="w-full bg-[#14532D] hover:bg-[#0d3d20] text-white font-bold py-3 px-6 rounded-xl flex items-center justify-center gap-2 transition-all">
            <span class="material-symbols-outlined text-lg">list_alt</span>
            Quản lý đơn hàng
        </a>
    </div>
</div>

<!-- JavaScript Controls for Countdown and Real-time Polling -->
<script>
    function handleJSONResponse(response) {
        const contentType = response.headers.get("content-type");
        if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
            if (contentType && contentType.indexOf("application/json") !== -1) {
                return response.json().then(errData => {
                    throw new Error(errData.message || errData.error || 'Lỗi hệ thống (Mã: ' + response.status + ')');
                });
            }
            throw new Error('Lỗi hệ thống (Mã: ' + response.status + ')');
        }
        return response.json();
    }

    // Copy to clipboard utility
    function copyText(text, btn) {
        navigator.clipboard.writeText(text).then(() => {
            const originalIcon = btn.innerHTML;
            btn.innerHTML = '<span class="material-symbols-outlined text-[18px] text-emerald-600 font-bold">check</span>';
            btn.classList.add('bg-emerald-100/50');
            setTimeout(() => {
                btn.innerHTML = originalIcon;
                btn.classList.remove('bg-emerald-100/50');
            }, 1500);
        }).catch(err => {
            console.error('Không thể sao chép văn bản: ', err);
        });
    }

    // Countdown Timer — dùng qrExpireMin từ server (mặc định 10 phút)
    let totalSeconds = parseInt(document.getElementById('js-qr-expire-min').value || '10') * 60;
    const countdownEl = document.getElementById('countdown');
    const qrOverlayEl = document.getElementById('qr-overlay');
    const renewFormEl = document.getElementById('renew-qr-form');

    const timerInterval = setInterval(() => {
        if (totalSeconds <= 0) {
            clearInterval(timerInterval);
            clearInterval(pollingInterval);
            countdownEl.textContent = "Hết hạn";
            qrOverlayEl.classList.remove('hidden');
            if (renewFormEl) renewFormEl.classList.remove('hidden');
            return;
        }

        totalSeconds--;
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        countdownEl.textContent = 
            (minutes < 10 ? '0' : '') + minutes + ':' + 
            (seconds < 10 ? '0' : '') + seconds;
    }, 1000);

    // Read clean values from DOM hidden fields to prevent quote/syntax breaking issues
    const orderId = document.getElementById('js-order-id').value;
    const reference = document.getElementById('js-reference').value;
    const amountFormatted = document.getElementById('js-amount-formatted').value;
    const contextPath = document.getElementById('js-context-path').value;
    const terminalPaidStatuses = new Set(['CONFIRMED', 'APPROVED', 'PREPARING', 'DISPATCHED', 'DELIVERED']);

    // Real-time Polling every 3 seconds to check order payment status
    const pollingUrl = contextPath + '/checkout?action=status&orderId=' + encodeURIComponent(orderId);
    const successUrl = contextPath + '/checkout?action=success&orderId=' + encodeURIComponent(orderId);

    // Hiển thị modal thông báo thành công và đếm ngược tự động chuyển hướng
    function showSuccessModal(redirectUrl) {
        const modal = document.getElementById('payment-success-modal');
        if (!modal) {
            window.location.href = redirectUrl;
            return;
        }
        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            const innerCard = modal.querySelector('.glass-card');
            if (innerCard) {
                innerCard.classList.remove('scale-95');
                innerCard.classList.add('scale-100');
            }
        }, 50);

        let countdown = 3;
        const countdownEl = document.getElementById('success-countdown');
        const interval = setInterval(() => {
            countdown--;
            if (countdownEl) countdownEl.textContent = countdown;
            if (countdown <= 0) {
                clearInterval(interval);
                window.location.href = redirectUrl;
            }
        }, 1000);
    }

    const pollingInterval = setInterval(() => {
        fetch(pollingUrl, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(handleJSONResponse)
            .then(data => {
                const orderStatus = data.data ? data.data.status : null;
                console.log('[FruitMkt] Polling Order Status:', orderStatus);
                if (orderStatus === 'CANCELLED') {
                    clearInterval(timerInterval);
                    clearInterval(pollingInterval);
                    alert('Đơn hàng đã bị hủy. Bạn sẽ được chuyển tới chi tiết đơn hàng.');
                    window.location.href = contextPath + '/profile/order-detail?orderId=' + encodeURIComponent(orderId);
                    return;
                }
                // If order state updated to a paid/fulfilled state, show success modal.
                if (orderStatus && terminalPaidStatuses.has(orderStatus)) {
                    clearInterval(timerInterval);
                    clearInterval(pollingInterval);
                    showSuccessModal(successUrl);
                }
            })
            .catch(error => {
                console.warn('[FruitMkt] Lỗi polling payment status:', error);
            });
    }, 3000);

    // Simulating developer payment success trigger
    function simulateSuccessRedirect() {
        const payload = {
            id: "SIM_TX_" + Date.now(),
            code: reference,
            transferType: "in",
            transferAmount: amountFormatted
        };

        fetch(contextPath + '/api/payment/webhook', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
        .then(handleJSONResponse)
        .then(data => {
            const outcome = data.data ? data.data.outcome : null;
            if (outcome && outcome !== 'invalid_payload') {
                console.log('[Dev Sim] Webhook simulation outcome:', outcome, data);
                alert('Webhook mô phỏng đã được gửi. Trạng thái sẽ được xác nhận qua polling.');
            } else {
                alert('Gửi webhook mô phỏng thất bại: ' + (data.error || data.message || 'Lỗi xử lý nội bộ'));
            }
        })
        .catch(err => {
            console.error('Lỗi webhook:', err);
            alert('Lỗi kết nối khi mô phỏng thanh toán.');
        });
    }

    // Xử lý nút "Tôi đã thanh toán" — vô hiệu hóa sau khi bấm để tránh double-submit
    function confirmPaymentClick(btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="material-symbols-outlined text-xl animate-spin">hourglass_empty</span> Đang gửi...';
        return true; // cho phép form submit
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
