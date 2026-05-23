<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Đổi mật khẩu - MetaFruit</title>
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#14532D",
                        "primary-hover": "#166534",
                        "primary-light": "#d1ffd8",
                        "surface": "#eaffea",
                        "on-surface": "#00210d",
                        "on-surface-variant": "#44483b",
                        "outline": "#75796a",
                        "outline-variant": "#c5c8b7",
                        "error": "#ba1a1a"
                    },
                    borderRadius: { "lg": "0.75rem", "xl": "1.25rem", "2xl": "1.5rem" },
                    fontFamily: { sans: ["Lexend", "sans-serif"] }
                }
            }
        }
    </script>
    <style>
        body { font-family: 'Lexend', sans-serif; }
        .glass-card {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.5);
            box-shadow: 0 20px 50px rgba(20, 83, 45, 0.12);
        }
        ::-webkit-scrollbar { width: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #14532D; border-radius: 9999px; }
        .strength-bar-fill {
            height: 4px;
            border-radius: 9999px;
            transition: width 0.4s ease, background-color 0.4s ease;
        }
    </style>
</head>
<body class="bg-emerald-50 text-on-surface min-h-screen flex flex-col antialiased relative">

    <%-- Decorative Background --%>
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
        <div class="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-multiply"
             style="background-image: url('https://lh3.googleusercontent.com/aida-public/AB6AXuDbzTRH5MPfxXQnED9OhayiGIhydHTVZL2CgybXiVn-iGcwBhA-qLCSGyekLQAVcm_RUpEDJtEv1_dACfRuWo4Utwsq8I5P2LdCjSPImoyUi9-ZwkMLix_Tor9bQei6zL2uFzVk6hMIf55qGhWqNDePckWeNBL3FpIcPmUalFvXnu98oImfdEpYZ05NsZqqwDPlzhQWXpUx0A0uTgqMNLhwXCQa8vYL5qKzl33ZDymr54KIJvNsO7tkF4BM8QHEctyj4Mzaizwus24');"></div>
        <div class="absolute inset-0 bg-gradient-to-br from-white/90 via-emerald-50/70 to-emerald-100/90 backdrop-blur-[4px]"></div>
    </div>

    <%-- Header --%>
    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/40 backdrop-blur-md shadow-[0_2px_15px_rgba(20,83,45,0.03)]">
        <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-3xl font-bold">eco</span>
            <div class="text-2xl font-bold text-primary tracking-wide">MetaFruit</div>
        </div>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/"
               class="text-sm font-semibold text-primary hover:text-primary-hover flex items-center gap-1 transition-colors px-3 py-1.5 rounded-full hover:bg-emerald-100/50">
                <span class="material-symbols-outlined text-[18px]">home</span>
                <span class="hidden sm:block">Trang chủ</span>
            </a>
        </div>
    </header>

    <main class="flex-1 flex items-center justify-center pt-28 pb-16 px-4 md:px-8 relative z-10 w-full">
        <div class="w-full max-w-md glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">

            <%-- Title --%>
            <div class="text-center mb-8">
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-emerald-100 mb-4 shadow-inner">
                    <span class="material-symbols-outlined text-primary text-4xl">manage_accounts</span>
                </div>
                <h1 class="text-2xl md:text-3xl font-bold text-primary mb-2">Đổi mật khẩu</h1>
                <p class="text-sm md:text-base text-on-surface-variant font-light leading-relaxed">
                    Xác nhận mật khẩu hiện tại và tạo mật khẩu mới an toàn hơn.
                </p>
            </div>

            <%-- Flash Message --%>
            <c:if test="${not empty sessionScope.flashMsg}">
                <c:set var="isError" value="${sessionScope.flashType == 'error'}"/>
                <div class="mb-6 p-4 ${isError ? 'bg-red-50 border-error text-red-800' : 'bg-green-50 border-primary text-green-800'} border-l-4 rounded-r-lg flex items-center gap-3 shadow-sm">
                    <span class="material-symbols-outlined ${isError ? 'text-error' : 'text-primary'}">${isError ? 'error' : 'check_circle'}</span>
                    <span class="text-sm font-medium"><c:out value="${sessionScope.flashMsg}"/></span>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <%-- Error Message --%>
            <c:if test="${not empty requestScope.errorMsg}">
                <div class="mb-6 p-4 bg-red-50 border-l-4 border-error text-red-800 rounded-r-lg flex items-center gap-3 shadow-sm">
                    <span class="material-symbols-outlined text-error">error</span>
                    <span class="text-sm font-medium"><c:out value="${requestScope.errorMsg}"/></span>
                </div>
            </c:if>

            <%-- Form --%>
            <form action="${pageContext.request.contextPath}/auth/change-password" method="post" class="space-y-6" id="changeForm">

                <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-5">

                    <%-- Current Password --%>
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="currentPassword">Mật khẩu hiện tại *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock_open</span>
                            <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60"
                                   id="currentPassword" name="currentPassword"
                                   placeholder="Nhập mật khẩu đang dùng"
                                   type="password" required autocomplete="current-password">
                            <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1"
                                    type="button" onclick="togglePasswordVisibility('currentPassword', this)">
                                <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                            </button>
                        </div>
                    </div>

                    <%-- Divider --%>
                    <div class="flex items-center gap-3">
                        <div class="flex-grow border-t border-outline-variant/30"></div>
                        <span class="text-xs text-outline">Mật khẩu mới</span>
                        <div class="flex-grow border-t border-outline-variant/30"></div>
                    </div>

                    <%-- New Password --%>
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="newPassword">Mật khẩu mới *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                            <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60"
                                   id="newPassword" name="newPassword"
                                   placeholder="Nhập mật khẩu mới"
                                   type="password" required minlength="8" maxlength="64"
                                   autocomplete="new-password"
                                   oninput="checkStrength(this.value); checkMatch()">
                            <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1"
                                    type="button" onclick="togglePasswordVisibility('newPassword', this)">
                                <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                            </button>
                        </div>
                        <div class="mt-1.5">
                            <div class="w-full h-1 bg-outline-variant/30 rounded-full overflow-hidden">
                                <div id="strengthBar" class="strength-bar-fill bg-outline-variant/30 w-0"></div>
                            </div>
                            <p id="strengthLabel" class="text-[11px] text-outline mt-1 h-4"></p>
                        </div>
                    </div>

                    <%-- Confirm Password --%>
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="confirmPassword">Xác nhận mật khẩu mới *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock_clock</span>
                            <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60"
                                   id="confirmPassword" name="confirmPassword"
                                   placeholder="Nhập lại mật khẩu mới"
                                   type="password" required minlength="8" maxlength="64"
                                   autocomplete="new-password"
                                   oninput="checkMatch()">
                            <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1"
                                    type="button" onclick="togglePasswordVisibility('confirmPassword', this)">
                                <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                            </button>
                        </div>
                        <p id="matchMsg" class="text-[11px] h-4"></p>
                    </div>
                </div>

                <%-- Security Tips --%>
                <div class="rounded-xl border border-emerald-200 bg-emerald-50/80 p-4 text-on-surface-variant space-y-1.5">
                    <p class="font-semibold text-primary text-xs uppercase tracking-wider mb-2">Yêu cầu mật khẩu</p>
                    <div class="flex items-center gap-2 text-xs" id="req-len">
                        <span class="material-symbols-outlined text-[15px] text-outline-variant">radio_button_unchecked</span>
                        <span>Tối thiểu 8 ký tự</span>
                    </div>
                    <div class="flex items-center gap-2 text-xs" id="req-upper">
                        <span class="material-symbols-outlined text-[15px] text-outline-variant">radio_button_unchecked</span>
                        <span>Có chữ hoa</span>
                    </div>
                    <div class="flex items-center gap-2 text-xs" id="req-num">
                        <span class="material-symbols-outlined text-[15px] text-outline-variant">radio_button_unchecked</span>
                        <span>Có chữ số</span>
                    </div>
                </div>

                <%-- Submit --%>
                <button type="submit" id="submitBtn"
                        class="w-full bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                    <span>Đổi mật khẩu</span>
                    <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">shield_check</span>
                </button>
            </form>

            <%-- Footer --%>
            <div class="mt-8 text-center border-t border-outline-variant/30 pt-6 space-y-2">
                <p class="text-sm text-on-surface-variant font-light">
                    Quên mật khẩu hiện tại?
                    <a class="text-primary font-bold hover:underline hover:text-primary-hover ml-1 transition-colors"
                       href="${pageContext.request.contextPath}/auth/forgot">Đặt lại mật khẩu</a>
                </p>
                <p class="text-sm text-on-surface-variant font-light">
                    <a class="text-on-surface-variant hover:text-primary hover:underline flex items-center justify-center gap-1 transition-colors"
                       href="${pageContext.request.contextPath}/">
                        <span class="material-symbols-outlined text-[16px]">arrow_back</span>
                        Quay về trang chủ
                    </a>
                </p>
            </div>

        </div>
    </main>

    <footer class="w-full py-4 text-center border-t border-white/20 bg-white/40 backdrop-blur-md relative z-10">
        <p class="text-xs text-on-surface-variant/80 font-light">&copy; 2026 MetaFruit. Giải pháp nông sản Việt sạch và bền vững.</p>
    </footer>

    <script>
        function togglePasswordVisibility(inputId, btn) {
            const input = document.getElementById(inputId);
            const icon = btn.querySelector('.material-symbols-outlined');
            if (input.type === 'password') {
                input.type = 'text';
                icon.textContent = 'visibility';
            } else {
                input.type = 'password';
                icon.textContent = 'visibility_off';
            }
        }

        function checkStrength(val) {
            const bar = document.getElementById('strengthBar');
            const label = document.getElementById('strengthLabel');
            let score = 0;
            if (val.length >= 8) score++;
            if (val.length >= 12) score++;
            if (/[A-Z]/.test(val)) score++;
            if (/[0-9]/.test(val)) score++;
            if (/[^A-Za-z0-9]/.test(val)) score++;

            const levels = [
                { pct: '0%',   color: 'bg-outline-variant/30', text: '' },
                { pct: '25%',  color: 'bg-red-400',            text: 'Rất yếu' },
                { pct: '50%',  color: 'bg-orange-400',         text: 'Yếu' },
                { pct: '75%',  color: 'bg-yellow-400',         text: 'Trung bình' },
                { pct: '90%',  color: 'bg-emerald-400',        text: 'Mạnh' },
                { pct: '100%', color: 'bg-primary',            text: 'Rất mạnh' },
            ];
            const lv = levels[Math.min(score, 5)];
            bar.className = 'strength-bar-fill ' + lv.color;
            bar.style.width = val.length === 0 ? '0%' : lv.pct;
            label.textContent = val.length === 0 ? '' : lv.text;

            toggleReq('req-len',   val.length >= 8);
            toggleReq('req-upper', /[A-Z]/.test(val));
            toggleReq('req-num',   /[0-9]/.test(val));
        }

        function toggleReq(id, ok) {
            const el = document.getElementById(id);
            const icon = el.querySelector('.material-symbols-outlined');
            const span = el.querySelector('span:last-child');
            if (ok) {
                icon.textContent = 'check_circle';
                icon.className = 'material-symbols-outlined text-[15px] text-primary';
                span.className = 'text-primary font-medium';
            } else {
                icon.textContent = 'radio_button_unchecked';
                icon.className = 'material-symbols-outlined text-[15px] text-outline-variant';
                span.className = '';
            }
        }

        function checkMatch() {
            const pw = document.getElementById('newPassword').value;
            const cpw = document.getElementById('confirmPassword').value;
            const msg = document.getElementById('matchMsg');
            const confirmInput = document.getElementById('confirmPassword');
            if (!cpw) { msg.textContent = ''; return; }
            if (pw === cpw) {
                msg.textContent = '✓ Mật khẩu khớp';
                msg.className = 'text-[11px] h-4 text-primary font-semibold';
                confirmInput.classList.remove('border-red-400');
                confirmInput.classList.add('border-primary');
            } else {
                msg.textContent = '✗ Mật khẩu chưa khớp';
                msg.className = 'text-[11px] h-4 text-error font-semibold';
                confirmInput.classList.remove('border-primary');
                confirmInput.classList.add('border-red-400');
            }
        }

        document.getElementById('changeForm').addEventListener('submit', function (e) {
            const pw = document.getElementById('newPassword').value;
            const cpw = document.getElementById('confirmPassword').value;
            if (pw !== cpw) {
                e.preventDefault();
                document.getElementById('matchMsg').textContent = '✗ Mật khẩu chưa khớp';
                document.getElementById('matchMsg').className = 'text-[11px] h-4 text-error font-semibold';
                return;
            }
            const btn = document.getElementById('submitBtn');
            btn.disabled = true;
            btn.innerHTML = '<span class="material-symbols-outlined text-[18px] animate-spin">progress_activity</span><span>Đang xử lý...</span>';
        });
    </script>
</body>
</html>
