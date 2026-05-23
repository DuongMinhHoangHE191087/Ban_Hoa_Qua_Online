<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>${requestScope.forgotMode ? 'Xác minh để đặt lại mật khẩu' : 'Xác minh email'} - MetaFruit</title>
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
                    borderRadius: {
                        "lg": "0.75rem",
                        "xl": "1.25rem",
                        "2xl": "1.5rem"
                    },
                    fontFamily: {
                        sans: ["Lexend", "sans-serif"]
                    }
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
    </style>
</head>
<body class="bg-emerald-50 text-on-surface min-h-screen flex flex-col antialiased relative">
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
        <div class="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-multiply" style="background-image: url('https://lh3.googleusercontent.com/aida-public/AB6AXuDbzTRH5MPfxXQnED9OhayiGIhydHTVZL2CgybXiVn-iGcwBhA-qLCSGyekLQAVcm_RUpEDJtEv1_dACfRuWo4Utwsq8I5P2LdCjSPImoyUi9-ZwkMLix_Tor9bQei6zL2uFzVk6hMIf55qGhWqNDePckWeNBL3FpIcPmUalFvXnu98oImfdEpYZ05NsZqqwDPlzhQWXpUx0A0uTgqMNLhwXCQa8vYL5qKzl33ZDymr54KIJvNsO7tkF4BM8QHEctyj4Mzaizwus24');"></div>
        <div class="absolute inset-0 bg-gradient-to-br from-white/90 via-emerald-50/70 to-emerald-100/90 backdrop-blur-[4px]"></div>
    </div>

    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/40 backdrop-blur-md shadow-[0_2px_15px_rgba(20,83,45,0.03)]">
        <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-3xl font-bold">eco</span>
            <div class="text-2xl font-bold text-primary tracking-wide">MetaFruit</div>
        </div>
        <div class="flex items-center gap-2">
            <a href="${pageContext.request.contextPath}/auth/login" class="text-sm font-semibold text-primary hover:text-primary-hover flex items-center gap-1 transition-colors px-3 py-1.5 rounded-full hover:bg-emerald-100/50">
                <span class="material-symbols-outlined text-[18px]">login</span>
                Đăng nhập
            </a>
        </div>
    </header>

    <main class="flex-1 flex items-center justify-center pt-28 pb-16 px-4 md:px-8 relative z-10 w-full">
        <div class="w-full max-w-xl glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">

            <%-- Step indicator chỉ hiện trong forgot mode --%>
            <c:if test="${requestScope.forgotMode}">
                <div class="flex items-center justify-center gap-2 mb-8">
                    <div class="flex items-center gap-1.5">
                        <div class="w-8 h-8 rounded-full bg-emerald-200 text-primary flex items-center justify-center shadow-sm">
                            <span class="material-symbols-outlined text-[16px]">check</span>
                        </div>
                        <span class="text-xs text-outline hidden sm:block">Nhập email</span>
                    </div>
                    <div class="h-px w-8 bg-primary"></div>
                    <div class="flex items-center gap-1.5">
                        <div class="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center text-xs font-bold shadow-md">2</div>
                        <span class="text-xs font-semibold text-primary hidden sm:block">Xác minh OTP</span>
                    </div>
                    <div class="h-px w-8 bg-outline-variant/50"></div>
                    <div class="flex items-center gap-1.5">
                        <div class="w-8 h-8 rounded-full bg-outline-variant/40 text-outline flex items-center justify-center text-xs font-bold">3</div>
                        <span class="text-xs text-outline hidden sm:block">Đặt lại mật khẩu</span>
                    </div>
                </div>
            </c:if>

            <div class="text-center mb-8">
                <h1 class="text-2xl md:text-3xl font-bold text-primary mb-2 flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-[32px]">${requestScope.forgotMode ? 'lock_reset' : 'mark_email_unread'}</span>
                    ${requestScope.forgotMode ? 'Xác minh để đặt lại mật khẩu' : 'Xác minh email'}
                </h1>
                <p class="text-sm md:text-base text-on-surface-variant font-light">
                    <c:choose>
                        <c:when test="${requestScope.forgotMode}">
                            Nhập mã xác minh đã gửi tới hộp thư của bạn để tiếp tục đặt lại mật khẩu.
                        </c:when>
                        <c:otherwise>
                            Nhập mã đã gửi tới hộp thư của bạn để kích hoạt tài khoản trong 5 phút.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <c:if test="${not empty requestScope.errorMsg}">
                <div class="mb-6 p-4 bg-red-50 border-l-4 border-error text-red-800 rounded-r-lg flex items-center gap-3 shadow-sm">
                    <span class="material-symbols-outlined text-error">error</span>
                    <span class="text-sm font-medium"><c:out value="${requestScope.errorMsg}"/></span>
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.flashMsg}">
                <c:set var="isError" value="${sessionScope.flashType == 'error'}"/>
                <div class="mb-6 p-4 ${isError ? 'bg-red-50 border-error text-red-800' : 'bg-green-50 border-primary text-green-800'} border-l-4 rounded-r-lg flex items-center gap-3 shadow-sm">
                    <span class="material-symbols-outlined ${isError ? 'text-error' : 'text-primary'}">
                        ${isError ? 'error' : 'check_circle'}
                    </span>
                    <span class="text-sm font-medium"><c:out value="${sessionScope.flashMsg}"/></span>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <div class="mb-6 rounded-xl border border-emerald-200 bg-emerald-50/80 p-4 text-sm text-on-surface-variant">
                Mã xác minh chỉ có hiệu lực <strong>5 phút</strong>. Bạn chỉ có thể gửi lại mã sau <strong>1 phút</strong> kể từ lần gửi gần nhất.
            </div>

            <form action="${pageContext.request.contextPath}${requestScope.forgotMode ? '/auth/forgot-verify' : '/auth/verify'}" method="post" class="space-y-5" id="verifyForm">
                <input type="hidden" name="action" value="verify">

                <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="email">Email xác minh</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">mail</span>
                            <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 rounded-lg text-sm transition-all outline-none text-on-surface-variant/90 cursor-not-allowed"
                                   id="email" value="<c:out value="${requestScope.email}"/>" type="email" readonly>
                        </div>
                    </div>

                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="code">Mã xác minh *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">pin</span>
                            <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm tracking-[0.4em] text-center transition-all outline-none placeholder:text-outline-variant/60"
                                   id="code" name="code" placeholder="______" inputmode="numeric" maxlength="6" minlength="6" pattern="[0-9]{6}" required>
                        </div>
                    </div>
                </div>

                <button type="submit" class="w-full bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                    <span>Xác minh ngay</span>
                    <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">verified_user</span>
                </button>
            </form>

            <div class="relative flex items-center my-6">
                <div class="flex-grow border-t border-outline-variant/40"></div>
                <span class="flex-shrink mx-4 text-xs font-semibold text-outline tracking-wider uppercase">Hoặc</span>
                <div class="flex-grow border-t border-outline-variant/40"></div>
            </div>

            <form action="${pageContext.request.contextPath}${requestScope.forgotMode ? '/auth/forgot-verify' : '/auth/verify'}" method="post" class="space-y-3" id="resendForm">
                <input type="hidden" name="action" value="resend">
                <button type="submit" id="resendButton" class="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-50 border border-gray-300 text-gray-700 font-semibold text-sm py-3 px-6 rounded-lg shadow-sm hover:shadow transition-all duration-300 active:scale-[0.99]">
                    <span class="material-symbols-outlined text-[18px]">refresh</span>
                    <span>Gửi lại mã xác minh</span>
                </button>
                <p id="resendHint" class="text-center text-xs text-on-surface-variant"></p>
            </form>

            <div class="mt-8 text-center border-t border-outline-variant/30 pt-6">
                <p class="text-sm text-on-surface-variant font-light">
                    <c:choose>
                        <c:when test="${requestScope.forgotMode}">
                            Quay lại bước nhập email?
                            <a class="text-primary font-bold hover:underline hover:text-primary-hover ml-1 transition-colors" href="${pageContext.request.contextPath}/auth/forgot">Thử email khác</a>
                        </c:when>
                        <c:otherwise>
                            Đã xác minh xong?
                            <a class="text-primary font-bold hover:underline hover:text-primary-hover ml-1 transition-colors" href="${pageContext.request.contextPath}/auth/login">Quay lại đăng nhập</a>
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </main>

    <footer class="w-full py-4 text-center border-t border-white/20 bg-white/40 backdrop-blur-md relative z-10">
        <p class="text-xs text-on-surface-variant/80 font-light">&copy; 2026 MetaFruit. Giải pháp nông sản Việt sạch và bền vững.</p>
    </footer>

    <script>
        const resendButton = document.getElementById('resendButton');
        const resendHint = document.getElementById('resendHint');
        let remainingSeconds = 60;

        function updateResendState() {
            if (remainingSeconds > 0) {
                resendButton.disabled = true;
                resendButton.classList.add('opacity-60', 'cursor-not-allowed');
                resendHint.textContent = 'Bạn có thể gửi lại mã sau ' + remainingSeconds + ' giây.';
                remainingSeconds -= 1;
                window.setTimeout(updateResendState, 1000);
            } else {
                resendButton.disabled = false;
                resendButton.classList.remove('opacity-60', 'cursor-not-allowed');
                resendHint.textContent = 'Nếu chưa nhận được email, hãy kiểm tra spam hoặc nhấn gửi lại mã.';
            }
        }

        updateResendState();
    </script>
</body>
</html>