<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quét mã QR Thanh toán - Verdant Market"/></jsp:include>

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

<div class="pt-24 pb-12 px-margin-mobile md:px-margin-desktop max-w-5xl mx-auto font-sans antialiased text-on-background bg-[#eaffea] min-h-screen">
    
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
                    DEVELOPMENT TESTING:
                </span>
                <p class="text-on-surface-variant mb-3 leading-relaxed">
                    Bạn có thể mô phỏng SePay callback ghi nhận tiền về thành công để kiểm thử khả năng chuyển trang tự động của Polling script.
                </p>
                <div class="flex gap-2">
                    <button onclick="simulateSuccessRedirect()" class="bg-[#14532D] text-white px-3 py-1.5 rounded font-bold hover:bg-opacity-90 transition-all cursor-pointer">
                        Mô phỏng Thanh toán Thành công
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
                        <button onclick="copyText('Ngân hàng Quân Đội (MB Bank)', this)" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Account No -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-emerald-100/50 hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Số tài khoản</span>
                            <span class="font-extrabold text-[#00210d] text-base select-all tracking-wider">${accountNo}</span>
                        </div>
                        <button onclick="copyText('${accountNo}', this)" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Account Holder -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-emerald-100/50 hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Chủ tài khoản thụ hưởng</span>
                            <span class="font-extrabold text-[#00210d] select-all">${accountName}</span>
                        </div>
                        <button onclick="copyText('${accountName}', this)" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
                            <span class="material-symbols-outlined text-[18px]">content_copy</span>
                        </button>
                    </div>

                    <!-- Amount -->
                    <div class="flex justify-between items-center bg-white/40 p-3 rounded-lg border border-[#ffdad6] hover:bg-white/60 transition-colors">
                        <div>
                            <span class="text-xs text-on-surface-variant font-medium block">Số tiền chính xác</span>
                            <span class="font-black text-[#ba1a1a] text-lg select-all"><ft:currency value="${order.finalAmount}"/></span>
                        </div>
                        <button onclick="copyText('${amountFormatted}', this)" class="text-primary hover:text-opacity-80 p-1 flex items-center justify-center rounded hover:bg-emerald-50 transition-colors cursor-pointer" title="Copy">
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
                            <span class="font-black text-amber-900 text-base select-all tracking-wider font-mono">${description}</span>
                        </div>
                        <button onclick="copyText('${description}', this)" class="text-amber-800 hover:text-opacity-80 p-1.5 flex items-center justify-center rounded hover:bg-yellow-100 transition-colors cursor-pointer" title="Copy">
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

    // Countdown Timer — dùng qrExpireMin từ server (mặc định 15 phút)
    let totalSeconds = ${qrExpireMin != null ? qrExpireMin : 15} * 60;
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

    // Real-time Polling every 3 seconds to check order payment status
    const pollingUrl = '${pageContext.request.contextPath}/checkout?action=status&orderId=${order.orderId}';
    const successUrl = '${pageContext.request.contextPath}/checkout?action=success&orderId=${order.orderId}';

    const pollingInterval = setInterval(() => {
        fetch(pollingUrl, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(handleJSONResponse)
            .then(data => {
                console.log('[FruitMkt] Polling Order Status:', data.status);
                // If order state updated to CONFIRMED (or preparing, dispatched etc. indicating payment complete)
                if (data.status && data.status !== 'PENDING_PAYMENT' && data.status !== 'UNKNOWN') {
                    clearInterval(timerInterval);
                    clearInterval(pollingInterval);
                    window.location.href = successUrl;
                }
            })
            .catch(error => {
                console.warn('[FruitMkt] Lỗi polling payment status:', error);
            });
    }, 3000);

    // Simulating developer payment success trigger
    function simulateSuccessRedirect() {
        window.location.href = successUrl;
    }

    // Xử lý nút "Tôi đã thanh toán" — vô hiệu hóa sau khi bấm để tránh double-submit
    function confirmPaymentClick(btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="material-symbols-outlined text-xl animate-spin">hourglass_empty</span> Đang gửi...';
        return true; // cho phép form submit
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
