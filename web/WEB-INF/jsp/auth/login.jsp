<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Đăng nhập - MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Material Symbols Outlined -->
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <!-- Tailwind Configuration -->
    <script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#14532D", // Dark Green for MetaFruit theme
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
        body {
            font-family: 'Lexend', sans-serif;
        }
        .glass-card {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.5);
            box-shadow: 0 20px 50px rgba(20, 83, 45, 0.12);
        }
        /* Custom scrollbar */
        ::-webkit-scrollbar {
            width: 6px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: #14532D;
            border-radius: 9999px;
        }
    </style>
</head>
<body class="bg-emerald-50 text-on-surface min-h-screen flex flex-col antialiased relative">

    <!-- Decorative Organic Background -->
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
        <div class="absolute inset-0 bg-cover bg-center opacity-30 mix-blend-multiply" 
             style="background-image: url('https://lh3.googleusercontent.com/aida-public/AB6AXuDbzTRH5MPfxXQnED9OhayiGIhydHTVZL2CgybXiVn-iGcwBhA-qLCSGyekLQAVcm_RUpEDJtEv1_dACfRuWo4Utwsq8I5P2LdCjSPImoyUi9-ZwkMLix_Tor9bQei6zL2uFzVk6hMIf55qGhWqNDePckWeNBL3FpIcPmUalFvXnu98oImfdEpYZ05NsZqqwDPlzhQWXpUx0A0uTgqMNLhwXCQa8vYL5qKzl33ZDymr54KIJvNsO7tkF4BM8QHEctyj4Mzaizwus24');">
        </div>
        <!-- Gradient Overlay to ensure readability and glass effect -->
        <div class="absolute inset-0 bg-gradient-to-br from-white/90 via-emerald-50/70 to-emerald-100/90 backdrop-blur-[4px]"></div>
    </div>

    <!-- Top AppBar Navigation Header -->
    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/40 backdrop-blur-md shadow-[0_2px_15px_rgba(20,83,45,0.03)]">
        <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-3xl font-bold">eco</span>
            <div class="text-2xl font-bold text-primary tracking-wide">
                MetaFruit
            </div>
        </div>
        <div class="flex items-center gap-2">
            <a href="${pageContext.request.contextPath}/" class="text-sm font-semibold text-primary hover:text-primary-hover flex items-center gap-1 transition-colors px-3 py-1.5 rounded-full hover:bg-emerald-100/50">
                <span class="material-symbols-outlined text-[18px]">home</span>
                Trang chủ
            </a>
        </div>
    </header>

    <!-- Main Content Container -->
    <main class="flex-1 flex items-center justify-center pt-28 pb-16 px-4 md:px-8 relative z-10 w-full">
        
        <!-- Login Card (Glassmorphism) -->
        <div class="w-full max-w-md glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">
            
            <!-- Header Title -->
            <div class="text-center mb-8">
                <h1 class="text-2xl md:text-3xl font-bold text-primary mb-2 flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-[32px]">login</span>
                    Đăng nhập tài khoản
                </h1>
                <p class="text-sm md:text-base text-on-surface-variant font-light">
                    Chào mừng bạn quay lại với sàn MetaFruit!
                </p>
            </div>

            <!-- Error message display if any -->
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

            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/auth/login" method="post" class="space-y-6" id="loginForm">
                
                <!-- Anti-CSRF Token -->
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                
                <!-- If there is a redirect parameter, pass it through -->
                <c:if test="${not empty param.redirect}">
                    <input type="hidden" name="redirect" value="<c:out value="${param.redirect}"/>">
                </c:if>

                <!-- Input Fields Container -->
                <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                    
                    <!-- Email Field -->
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="identifier">Email hoặc Số điện thoại *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">mail</span>
                            <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                   id="identifier" name="identifier" value="<c:out value="${param.identifier}"/>" placeholder="Nhập email hoặc số điện thoại" type="text" required>
                        </div>
                    </div>

                    <!-- Password Field -->
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="password">Mật khẩu *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                            <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                   id="password" name="password" placeholder="Nhập mật khẩu" type="password" required>
                            <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1" 
                                    type="button" onclick="togglePasswordVisibility('password', this)">
                                <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Remember Me & Forgot Password -->
                <div class="flex items-center justify-between">
                    <label class="flex items-center gap-2 cursor-pointer select-none text-xs text-on-surface-variant font-medium">
                        <input class="rounded border-outline/30 text-primary focus:ring-primary h-4.5 w-4.5 bg-white cursor-pointer" 
                               type="checkbox" name="rememberMe" id="rememberMe">
                        <span>Ghi nhớ đăng nhập</span>
                    </label>
                    <a class="text-xs font-bold text-primary hover:underline hover:text-primary-hover transition-colors" 
                       href="${pageContext.request.contextPath}/auth/forgot">Quên mật khẩu?</a>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="w-full mt-4 bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                    <span>Đăng nhập</span>
                    <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">login</span>
                </button>
            </form>

            <!-- Divider with Text -->
            <div class="relative flex items-center my-6">
                <div class="flex-grow border-t border-outline-variant/40"></div>
                <span class="flex-shrink mx-4 text-xs font-semibold text-outline tracking-wider uppercase">Hoặc</span>
                <div class="flex-grow border-t border-outline-variant/40"></div>
            </div>

            <!-- Google OAuth Login Button (Bỏ Facebook theo yêu cầu) -->
            <div class="flex justify-center">
                <a href="${pageContext.request.contextPath}/auth/google-login"
                   class="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-50 border border-gray-300 text-gray-700 font-semibold text-sm py-3 px-6 rounded-lg shadow-sm hover:shadow transition-all duration-300 active:scale-[0.99]">
                    <svg class="h-5 w-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05" />
                        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                    </svg>
                    <span>Đăng nhập nhanh bằng Google</span>
                </a>
            </div>

            <!-- Footer Signup Link -->
            <div class="mt-8 text-center border-t border-outline-variant/30 pt-6">
                <p class="text-sm text-on-surface-variant font-light">
                    Chưa có tài khoản trên sàn? 
                    <a class="text-primary font-bold hover:underline hover:text-primary-hover ml-1 transition-colors" 
                       href="${pageContext.request.contextPath}/auth/register">Đăng ký ngay</a>
                </p>
            </div>
            
        </div>
    </main>

    <!-- Site Footer -->
    <footer class="w-full py-4 text-center border-t border-white/20 bg-white/40 backdrop-blur-md relative z-10">
        <p class="text-xs text-on-surface-variant/80 font-light">&copy; 2026 MetaFruit. Giải pháp nông sản Việt sạch và bền vững.</p>
    </footer>

    <!-- Interactive Client Script -->
    <script>
        /**
         * Toggle Password Fields visibility
         * @param {string} inputId
         * @param {HTMLButtonElement} btn
         */
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
    </script>
</body>
</html>
