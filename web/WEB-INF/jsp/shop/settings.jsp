<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kênh Người Bán | Cài Đặt Cửa Hàng</title>
    <meta name="description" content="Cài đặt ngưỡng cảnh báo tồn kho thấp và số ngày cảnh báo trước khi lô hàng hết hạn.">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">

    <!-- Google Fonts & Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">

    <!-- Core Tailwind and SweetAlert -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>

    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:      '#4d661c',
                        'primary-hover': '#364e03',
                        'primary-dk': '#364e03',
                        'primary-lt': '#f0f7e6',
                        surface:      '#ffffff',
                        'surface-2':  '#f8fafc',
                        border:       '#e2ece7',
                        'txt':        '#0f172a',
                        'txt-2':      '#475569',
                        'txt-3':      '#94a3b8',
                    },
                    fontFamily: {
                        sans: ['Lexend', 'Plus Jakarta Sans', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
                    },
                    boxShadow: {
                        card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                    }
                }
            }
        }
    </script>

    <style>
        body {
            background-color: #f4fbf7;
            font-family: 'Lexend', 'Plus Jakarta Sans', -apple-system, sans-serif;
        }
        .glass-card {
            background: #ffffff;
            border: 1px solid #e2ece7;
            box-shadow: 0 1px 3px rgba(0,0,0,.05), 0 4px 16px -4px rgba(20,83,45,.06);
        }
        .form-input {
            width: 100%;
            padding: 10px 14px;
            border: 1.5px solid #e2ece7;
            border-radius: 10px;
            font-size: 14px;
            font-family: 'Lexend', sans-serif;
            background: #fff;
            color: #0f172a;
            outline: none;
            transition: all 0.2s;
        }
        .form-input:focus {
            border-color: #4d661c;
            box-shadow: 0 0 0 3px rgba(77,102,28,0.12);
        }
        .range-track {
            -webkit-appearance: none;
            appearance: none;
            height: 6px;
            background: linear-gradient(to right, #4d661c var(--pct,50%), #e2ece7 var(--pct,50%));
            border-radius: 9999px;
            outline: none;
            cursor: pointer;
        }
        .range-track::-webkit-slider-thumb {
            -webkit-appearance: none;
            width: 20px; height: 20px;
            background: #4d661c;
            border-radius: 50%;
            box-shadow: 0 2px 6px rgba(77,102,28,.3);
            transition: transform .15s;
        }
        .range-track::-webkit-slider-thumb:hover { transform: scale(1.15); }
        .toggle-wrapper { position: relative; width: 44px; height: 24px; }
        .toggle-wrapper input { opacity: 0; width: 0; height: 0; }
        .toggle-slider {
            position: absolute; cursor: pointer;
            inset: 0; background: #cbd5e1; border-radius: 9999px;
            transition: background .2s;
        }
        .toggle-slider:before {
            content: ''; position: absolute;
            width: 18px; height: 18px; left: 3px; bottom: 3px;
            background: #fff; border-radius: 50%;
            transition: transform .2s;
            box-shadow: 0 1px 3px rgba(0,0,0,.2);
        }
        .toggle-wrapper input:checked + .toggle-slider { background: #4d661c; }
        .toggle-wrapper input:checked + .toggle-slider:before { transform: translateX(20px); }
    </style>
</head>
<body class="antialiased text-[#0f172a]">
<div class="flex min-h-screen">
    <!-- Shared Sidebar -->
    <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
        <jsp:param name="activePage" value="settings"/>
    </jsp:include>

    <!-- Main Content Area -->
    <main class="flex-1 p-6 md:p-8 overflow-y-auto">

        <!-- Page Header -->
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Cài Đặt Cửa Hàng</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Tuỳ chỉnh ngưỡng cảnh báo tồn kho, lô hàng sắp hết hạn và các tùy chọn thông báo.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-white/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-sliders text-[#84cc16]"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Shop Settings</span>
            </div>
        </div>

        <!-- Flash Messages -->
        <c:if test="${not empty sessionScope.flashMsg}">
            <script>
                window.addEventListener('DOMContentLoaded', function() {
                    Swal.fire({
                        icon: '${sessionScope.flashType == "error" ? "error" : "success"}',
                        title: '${sessionScope.flashType == "error" ? "Thất bại" : "Thành công"}',
                        text: '${sessionScope.flashMsg}',
                        confirmButtonColor: '#4d661c',
                        timer: 3000
                    });
                });
            </script>
            <c:remove var="flashMsg" scope="session"/>
            <c:remove var="flashType" scope="session"/>
        </c:if>

        <form action="${pageContext.request.contextPath}/shop/settings" method="post" class="space-y-6" id="settings-form">

            <!-- Card 1: Low Stock Alert -->
            <div class="glass-card rounded-2xl p-6">
                <div class="flex items-center gap-3 mb-6 pb-4 border-b border-[#e2ece7]">
                    <div class="w-10 h-10 rounded-xl bg-red-50 text-red-500 flex items-center justify-center">
                        <i class="fa-solid fa-triangle-exclamation text-lg"></i>
                    </div>
                    <div>
                        <h2 class="text-base font-bold text-[#0f172a]">Cảnh Báo Tồn Kho Thấp</h2>
                        <p class="text-xs text-[#475569] mt-0.5">Hiển thị cảnh báo trên Dashboard khi tồn kho ≤ ngưỡng này. Sản phẩm bị ẩn/deactivate sẽ không bị báo.</p>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label class="block text-xs font-bold text-[#475569] mb-2" for="lowStockThreshold">
                            Ngưỡng cảnh báo tồn kho thấp <span class="text-red-500">*</span>
                        </label>
                        <div class="flex items-center gap-4">
                            <input type="range" id="lowStockSlider" min="1" max="500" step="1"
                                   value="${shopProfile.lowStockThreshold > 0 ? shopProfile.lowStockThreshold : 10}"
                                   class="range-track flex-1" oninput="syncRange('lowStock')">
                            <div class="flex items-center gap-2">
                                <input type="number" id="lowStockThreshold" name="lowStockThreshold"
                                       class="form-input w-24 text-center font-bold text-lg"
                                       value="${shopProfile.lowStockThreshold > 0 ? shopProfile.lowStockThreshold : 10}"
                                       required min="1" max="1000" oninput="syncInput('lowStock')">
                                <span class="text-xs text-[#94a3b8] whitespace-nowrap">đơn vị</span>
                            </div>
                        </div>
                        <div class="flex justify-between text-[10px] text-[#94a3b8] mt-1 px-1">
                            <span>1</span><span>100</span><span>200</span><span>300</span><span>500</span>
                        </div>
                    </div>

                    <div class="bg-amber-50/60 border border-amber-100 rounded-xl p-4 flex flex-col justify-center">
                        <h3 class="text-xs font-bold text-amber-800 mb-2">📌 Gợi ý ngưỡng cảnh báo</h3>
                        <ul class="text-xs text-amber-700 space-y-1">
                            <li>• <strong>5–10</strong> đơn vị: Hàng nhỏ, quay vòng nhanh</li>
                            <li>• <strong>20–50</strong> đơn vị: Hàng có đặt trước nhiều</li>
                            <li>• <strong>50–100+</strong>: Hàng đặt theo lô/vụ mùa</li>
                        </ul>
                    </div>
                </div>

                <div class="mt-4 p-3 bg-[#f0faf3] rounded-xl border border-[#bbf7d0]/60 text-xs text-[#364e03]">
                    <i class="fa-solid fa-circle-info mr-1.5"></i>
                    Khi bạn <strong>ẩn hoặc deactivate sản phẩm</strong>, biến thể đó sẽ <strong>tự động biến mất</strong> khỏi danh sách cảnh báo.
                    Bạn cũng có thể nhấn <strong>✕</strong> trên Dashboard để tạm ẩn dòng cảnh báo không còn liên quan.
                </div>
            </div>

            <!-- Card 2: Expiry Warning -->
            <div class="glass-card rounded-2xl p-6">
                <div class="flex items-center gap-3 mb-6 pb-4 border-b border-[#e2ece7]">
                    <div class="w-10 h-10 rounded-xl bg-orange-50 text-orange-500 flex items-center justify-center">
                        <i class="fa-solid fa-calendar-xmark text-lg"></i>
                    </div>
                    <div>
                        <h2 class="text-base font-bold text-[#0f172a]">Cảnh Báo Hết Hạn Lô Hàng</h2>
                        <p class="text-xs text-[#475569] mt-0.5">Số ngày trước khi lô hàng hết hạn để hệ thống gửi thông báo cảnh báo sớm.</p>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label class="block text-xs font-bold text-[#475569] mb-2" for="expiryWarningDays">
                            Số ngày cảnh báo trước khi hết hạn <span class="text-red-500">*</span>
                        </label>
                        <div class="flex items-center gap-4">
                            <input type="range" id="expirySlider" min="1" max="90" step="1"
                                   value="${shopProfile.expiryWarningDays > 0 ? shopProfile.expiryWarningDays : 3}"
                                   class="range-track flex-1" oninput="syncRange('expiry')">
                            <div class="flex items-center gap-2">
                                <input type="number" id="expiryWarningDays" name="expiryWarningDays"
                                       class="form-input w-24 text-center font-bold text-lg"
                                       value="${shopProfile.expiryWarningDays > 0 ? shopProfile.expiryWarningDays : 3}"
                                       required min="1" max="90" oninput="syncInput('expiry')">
                                <span class="text-xs text-[#94a3b8] whitespace-nowrap">ngày</span>
                            </div>
                        </div>
                        <div class="flex justify-between text-[10px] text-[#94a3b8] mt-1 px-1">
                            <span>1 ngày</span><span>30 ngày</span><span>60 ngày</span><span>90 ngày</span>
                        </div>
                    </div>

                    <div class="bg-orange-50/60 border border-orange-100 rounded-xl p-4 flex flex-col justify-center">
                        <h3 class="text-xs font-bold text-orange-800 mb-2">📌 Gợi ý số ngày cảnh báo</h3>
                        <ul class="text-xs text-orange-700 space-y-1">
                            <li>• <strong>1–3 ngày</strong>: Hàng tươi, hoa quả ngắn hạn</li>
                            <li>• <strong>7–14 ngày</strong>: Hàng đóng gói, chế biến sẵn</li>
                            <li>• <strong>30+ ngày</strong>: Hàng khô, đông lạnh, bảo quản lâu</li>
                        </ul>
                    </div>
                </div>

                <div class="mt-4 p-3 bg-orange-50/50 rounded-xl border border-orange-100/60 text-xs text-orange-800">
                    <i class="fa-solid fa-bell mr-1.5"></i>
                    Hệ thống sẽ tự động quét hàng đêm và gửi thông báo <strong>INVENTORY_ALERT</strong> cho các lô hàng sắp hết hạn theo số ngày bạn cấu hình.
                </div>
            </div>

            <!-- Card 3: Notification Preferences -->
            <div class="glass-card rounded-2xl p-6">
                <div class="flex items-center gap-3 mb-6 pb-4 border-b border-[#e2ece7]">
                    <div class="w-10 h-10 rounded-xl bg-blue-50 text-blue-500 flex items-center justify-center">
                        <i class="fa-solid fa-bell text-lg"></i>
                    </div>
                    <div>
                        <h2 class="text-base font-bold text-[#0f172a]">Tuỳ Chọn Thông Báo</h2>
                        <p class="text-xs text-[#475569] mt-0.5">Bật/tắt các loại thông báo hiển thị trên chuông và email. Lưu tại trình duyệt.</p>
                    </div>
                </div>

                <div class="space-y-4">
                    <!-- Notification toggle row -->
                    <c:set var="notifItems" value="lowstock,expiry,order,return" />
                    <div class="flex items-center justify-between p-4 bg-[#f8fafc] rounded-xl border border-[#e2ece7] hover:border-[#4d661c]/30 transition-colors">
                        <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg bg-red-50 text-red-500 flex items-center justify-center text-sm"><i class="fa-solid fa-triangle-exclamation"></i></div>
                            <div>
                                <p class="text-sm font-semibold text-[#0f172a]">Cảnh báo tồn kho thấp</p>
                                <p class="text-xs text-[#94a3b8]">Hiện thông báo khi có biến thể dưới ngưỡng</p>
                            </div>
                        </div>
                        <label class="toggle-wrapper" id="toggle-lowstock-wrap">
                            <input type="checkbox" id="notif-lowstock" checked onchange="saveNotifPref('lowstock', this.checked)">
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="flex items-center justify-between p-4 bg-[#f8fafc] rounded-xl border border-[#e2ece7] hover:border-[#4d661c]/30 transition-colors">
                        <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg bg-orange-50 text-orange-500 flex items-center justify-center text-sm"><i class="fa-solid fa-calendar-xmark"></i></div>
                            <div>
                                <p class="text-sm font-semibold text-[#0f172a]">Cảnh báo lô hàng hết hạn</p>
                                <p class="text-xs text-[#94a3b8]">Thông báo khi lô hàng sắp hết hạn theo cài đặt</p>
                            </div>
                        </div>
                        <label class="toggle-wrapper" id="toggle-expiry-wrap">
                            <input type="checkbox" id="notif-expiry" checked onchange="saveNotifPref('expiry', this.checked)">
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="flex items-center justify-between p-4 bg-[#f8fafc] rounded-xl border border-[#e2ece7] hover:border-[#4d661c]/30 transition-colors">
                        <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg bg-blue-50 text-blue-500 flex items-center justify-center text-sm"><i class="fa-solid fa-clipboard-list"></i></div>
                            <div>
                                <p class="text-sm font-semibold text-[#0f172a]">Đơn hàng mới</p>
                                <p class="text-xs text-[#94a3b8]">Thông báo khi có đơn hàng cần xử lý</p>
                            </div>
                        </div>
                        <label class="toggle-wrapper" id="toggle-order-wrap">
                            <input type="checkbox" id="notif-order" checked onchange="saveNotifPref('order', this.checked)">
                            <span class="toggle-slider"></span>
                        </label>
                    </div>

                    <div class="flex items-center justify-between p-4 bg-[#f8fafc] rounded-xl border border-[#e2ece7] hover:border-[#4d661c]/30 transition-colors">
                        <div class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-lg bg-purple-50 text-purple-500 flex items-center justify-center text-sm"><i class="fa-solid fa-rotate-left"></i></div>
                            <div>
                                <p class="text-sm font-semibold text-[#0f172a]">Yêu cầu hoàn trả</p>
                                <p class="text-xs text-[#94a3b8]">Thông báo khi khách yêu cầu đổi/hoàn trả</p>
                            </div>
                        </div>
                        <label class="toggle-wrapper" id="toggle-return-wrap">
                            <input type="checkbox" id="notif-return" checked onchange="saveNotifPref('return', this.checked)">
                            <span class="toggle-slider"></span>
                        </label>
                    </div>
                </div>

                <p class="text-[10px] text-[#94a3b8] mt-4 text-center">
                    <i class="fa-solid fa-info-circle mr-1"></i>
                    Tuỳ chọn thông báo lưu tại trình duyệt này. Thay đổi không ảnh hưởng các trình duyệt khác.
                </p>
            </div>

            <!-- Submit -->
            <div class="flex items-center gap-3">
                <button type="submit" id="save-btn"
                        class="flex items-center gap-2 bg-[#4d661c] hover:bg-[#364e03] text-white font-bold px-8 py-3 rounded-xl shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg">
                    <i class="fa-solid fa-floppy-disk"></i> Lưu cài đặt
                </button>
                <a href="${pageContext.request.contextPath}/shop/dashboard"
                   class="flex items-center gap-2 text-sm font-semibold text-[#475569] hover:text-[#4d661c] transition-colors px-4 py-3">
                    <i class="fa-solid fa-arrow-left text-xs"></i> Quay lại Dashboard
                </a>
            </div>

        </form>
    </main>
</div>

<script>
    // ===== Range Slider Sync =====
    var lowStockSlider = document.getElementById('lowStockSlider');
    var lowStockInput  = document.getElementById('lowStockThreshold');
    var expirySlider   = document.getElementById('expirySlider');
    var expiryInput    = document.getElementById('expiryWarningDays');

    function updateSliderFill(slider) {
        var min = parseFloat(slider.min), max = parseFloat(slider.max), val = parseFloat(slider.value);
        var pct = ((val - min) / (max - min)) * 100;
        slider.style.setProperty('--pct', pct + '%');
    }

    function syncRange(type) {
        if (type === 'lowStock') {
            lowStockInput.value = lowStockSlider.value;
            updateSliderFill(lowStockSlider);
        } else {
            expiryInput.value = expirySlider.value;
            updateSliderFill(expirySlider);
        }
    }

    function syncInput(type) {
        if (type === 'lowStock') {
            lowStockSlider.value = Math.min(500, Math.max(1, lowStockInput.value || 1));
            updateSliderFill(lowStockSlider);
        } else {
            expirySlider.value = Math.min(90, Math.max(1, expiryInput.value || 1));
            updateSliderFill(expirySlider);
        }
    }

    // Init fills
    updateSliderFill(lowStockSlider);
    updateSliderFill(expirySlider);

    // ===== Notification Preferences (localStorage) =====
    var NOTIF_KEY = 'shopNotifPrefs_<c:out value="${sessionScope.currentUser.userId}"/>';

    function loadNotifPrefs() {
        var prefs;
        try { prefs = JSON.parse(localStorage.getItem(NOTIF_KEY) || '{}'); } catch(e) { prefs = {}; }
        ['lowstock', 'expiry', 'order', 'return'].forEach(function(k) {
            var el = document.getElementById('notif-' + k);
            if (el && prefs[k] !== undefined) el.checked = prefs[k];
        });
    }

    function saveNotifPref(key, value) {
        var prefs;
        try { prefs = JSON.parse(localStorage.getItem(NOTIF_KEY) || '{}'); } catch(e) { prefs = {}; }
        prefs[key] = value;
        localStorage.setItem(NOTIF_KEY, JSON.stringify(prefs));
    }

    // ===== Submit feedback =====
    document.getElementById('settings-form').addEventListener('submit', function() {
        var btn = document.getElementById('save-btn');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
    });

    window.addEventListener('DOMContentLoaded', function() {
        loadNotifPrefs();
    });
</script>
</body>
</html>
