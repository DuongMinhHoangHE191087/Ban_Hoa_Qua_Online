<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Đăng ký tài khoản - MetaFruit</title>
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
        /* Custom scrollbar for beautiful experience */
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
        
        <!-- Registration Card (Glassmorphism) -->
        <div class="w-full max-w-2xl glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">
            
            <!-- Header Title -->
            <div class="text-center mb-8">
                <h1 class="text-2xl md:text-3xl font-bold text-primary mb-2 flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-[32px]">person_add</span>
                    Tạo tài khoản mới
                </h1>
                <p class="text-sm md:text-base text-on-surface-variant font-light">
                    Hành trình trải nghiệm và cung cấp nguồn thực phẩm xanh sạch bắt đầu tại đây!
                </p>
            </div>

            <!-- Error message display if any -->
            <c:if test="${not empty requestScope.errorMsg}">
                <div class="mb-6 p-4 bg-red-50 border-l-4 border-error text-red-800 rounded-r-lg flex items-center gap-3 shadow-sm animate-pulse">
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

            <!-- Unified Form -->
            <form action="${pageContext.request.contextPath}/auth/register" method="post" class="space-y-6" id="registerForm">
                
                <!-- Anti-CSRF Token Placeholder -->
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                
                <!-- Dynamic Account Type Selector (Hidden Input + Tailwind Tabs) -->
                <input type="hidden" id="accountType" name="accountType" value="<c:out value="${not empty param.accountType ? param.accountType : 'CUSTOMER'}"/>">
                
                <div class="flex p-1.5 bg-emerald-100/60 rounded-xl max-w-md mx-auto border border-emerald-200/50 shadow-inner">
                    <button type="button" onclick="switchTab('CUSTOMER')" id="tabCustomer"
                            class="flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 bg-white text-primary shadow-sm">
                        <span class="material-symbols-outlined text-[18px]">shopping_cart</span>
                        Khách hàng
                    </button>
                    <button type="button" onclick="switchTab('SHOP_OWNER')" id="tabShop"
                            class="flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 text-on-surface-variant hover:text-primary">
                        <span class="material-symbols-outlined text-[18px]">storefront</span>
                        Chủ cửa hàng
                    </button>
                </div>

                <!-- Section: Base Fields (Required for both Customer and Shop) -->
                <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                    <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-2 flex items-center gap-1.5">
                        <span class="material-symbols-outlined text-[16px]">assignment_ind</span>
                        Thông tin tài khoản cơ bản
                    </h3>
                    
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <!-- Full Name Field -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="fullName">Họ và tên *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">person</span>
                                <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="fullName" name="fullName" value="<c:out value="${param.fullName}"/>" placeholder="Nhập họ và tên của bạn" type="text" required minlength="3" maxlength="100">
                            </div>
                        </div>

                        <!-- Phone Field -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="phone">Số điện thoại *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">call</span>
                                <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="phone" name="phone" value="<c:out value="${param.phone}"/>" placeholder="Nhập số điện thoại" type="tel" required maxlength="15">
                            </div>
                        </div>
                    </div>

                    <!-- Email Field -->
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="email">Địa chỉ Email đăng nhập *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">mail</span>
                            <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                   id="email" name="email" value="<c:out value="${param.email}"/>" placeholder="ví dụ: tenban@email.com" type="email" required>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <!-- Password Field -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="password">Mật khẩu đăng nhập *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                                <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="password" name="password" placeholder="Mật khẩu từ 8-64 ký tự" type="password" required minlength="8" maxlength="64">
                                <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1" 
                                        type="button" onclick="togglePasswordVisibility('password', this)">
                                    <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                                </button>
                            </div>
                        </div>

                        <!-- Confirm Password Field -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="confirmPassword">Xác nhận mật khẩu *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">lock</span>
                                <input class="w-full pl-9 pr-10 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu" type="password" required>
                                <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors flex items-center justify-center p-1" 
                                        type="button" onclick="togglePasswordVisibility('confirmPassword', this)">
                                    <span class="material-symbols-outlined text-[20px]">visibility_off</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Section: Advanced Shop Fields (Hidden by default, shown for SHOP_OWNER) -->
                <div id="shopFields" class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4 hidden animate-[fadeIn_0.4s_ease-out]">
                    <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-2 flex items-center gap-1.5">
                        <span class="material-symbols-outlined text-[16px]">domain</span>
                        Thông tin chi tiết cửa hàng đối tác
                    </h3>
                    
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <!-- Store Name -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="storeName">Tên cửa hàng / Doanh nghiệp *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">storefront</span>
                                <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="storeName" name="storeName" value="<c:out value="${param.storeName}"/>" placeholder="VD: Nông trại hữu cơ xanh" type="text">
                            </div>
                        </div>
                        
                        <!-- Business Email -->
                        <div class="flex flex-col gap-1">
                            <label class="text-xs font-semibold text-primary" for="businessEmail">Email liên hệ kinh doanh *</label>
                            <div class="relative">
                                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">contact_mail</span>
                                <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                       id="businessEmail" name="businessEmail" value="<c:out value="${param.businessEmail}"/>" placeholder="email@doanhnghiep.com" type="email">
                            </div>
                        </div>
                    </div>

                    <!-- Business Address -->
                    <div class="flex flex-col gap-1">
                        <label class="text-xs font-semibold text-primary" for="address">Địa chỉ kinh doanh / Điểm gom hàng *</label>
                        <div class="relative">
                            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">pin_drop</span>
                            <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                   id="address" name="address" value="<c:out value="${param.address}"/>" placeholder="Nhập số nhà, tên đường, phường/xã, quận/huyện..." type="text">
                        </div>
                    </div>

                    <!-- Categories Selection -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-semibold text-primary">Danh mục sản phẩm mong muốn kinh doanh *</label>
                        <div class="grid grid-cols-2 gap-3 mt-1">
                            <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                <input class="rounded text-primary focus:ring-primary h-4.5 w-4.5 border-outline/30 bg-white" name="categories" value="citrus" type="checkbox">
                                <span class="ml-2.5 text-xs font-medium text-on-surface">Cam, Quýt, Bưởi nội địa</span>
                            </label>
                            <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                <input class="rounded text-primary focus:ring-primary h-4.5 w-4.5 border-outline/30 bg-white" name="categories" value="tropical" type="checkbox">
                                <span class="ml-2.5 text-xs font-medium text-on-surface">Trái cây vùng nhiệt đới</span>
                            </label>
                            <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                <input class="rounded text-primary focus:ring-primary h-4.5 w-4.5 border-outline/30 bg-white" name="categories" value="berries" type="checkbox">
                                <span class="ml-2.5 text-xs font-medium text-on-surface">Quả mọng & Trái cây nhập</span>
                            </label>
                            <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                <input class="rounded text-primary focus:ring-primary h-4.5 w-4.5 border-outline/30 bg-white" name="categories" value="dry" type="checkbox">
                                <span class="ml-2.5 text-xs font-medium text-on-surface">Rau củ quả sấy, đóng hộp</span>
                            </label>
                        </div>
                    </div>

                    <!-- File Upload for Business Documents -->
                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-semibold text-primary">Tải lên tài liệu xác minh (GPKD, Chứng nhận ATTP...) *</label>
                        <div class="border-2 border-dashed border-primary/20 rounded-lg p-5 text-center bg-white/20 hover:bg-emerald-50/50 transition-colors cursor-pointer group relative" id="dropzone">
                            <input class="absolute inset-0 w-full h-full opacity-0 cursor-pointer" id="businessDocs" name="businessDocs" type="file" multiple onchange="handleFileSelect(this)">
                            <div class="flex flex-col items-center gap-2 pointer-events-none">
                                <span class="material-symbols-outlined text-[36px] text-primary/60 group-hover:text-primary transition-colors">cloud_upload</span>
                                <p class="text-xs font-medium text-on-surface-variant" id="uploadLabel">
                                    Kéo và thả tài liệu vào đây hoặc <span class="text-primary font-bold">chọn tệp từ thiết bị</span>
                                </p>
                                <p class="text-[10px] text-outline">
                                    Hỗ trợ định dạng: PDF, JPG, PNG (Tối đa 5MB)
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Terms and Conditions Checkbox -->
                <div class="flex items-start gap-3 mt-4">
                    <div class="flex items-center h-5 mt-0.5">
                        <input class="w-4 h-4 rounded border-outline/30 text-primary focus:ring-primary bg-white cursor-pointer" id="terms" name="terms" type="checkbox" required>
                    </div>
                    <label class="text-xs text-on-surface-variant leading-relaxed cursor-pointer" for="terms">
                        Tôi đồng ý với các <a class="text-primary font-bold hover:underline" href="#">Điều khoản sử dụng dịch vụ</a> và <a class="text-primary font-bold hover:underline" href="#">Chính sách bảo mật dữ liệu khách hàng</a> của MetaFruit.
                    </label>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="w-full mt-4 bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                    <span>Hoàn tất Đăng ký</span>
                    <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">arrow_forward</span>
                </button>
            </form>

            <!-- Divider with Text -->
            <div class="relative flex items-center my-6">
                <div class="flex-grow border-t border-outline-variant/40"></div>
                <span class="flex-shrink mx-4 text-xs font-semibold text-outline tracking-wider uppercase">Hoặc</span>
                <div class="flex-grow border-t border-outline-variant/40"></div>
            </div>

            <!-- Google OAuth UI Registration Button -->
            <div class="flex justify-center">
                <a href="${pageContext.request.contextPath}/auth/google-login"
                   class="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-50 border border-gray-300 text-gray-700 font-semibold text-sm py-3 px-6 rounded-lg shadow-sm hover:shadow transition-all duration-300 active:scale-[0.99]">
                    <svg class="h-5 w-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05" />
                        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                    </svg>
                    <span>Đăng ký nhanh bằng tài khoản Google</span>
                </a>
            </div>

            <!-- Footer Login Link -->
            <div class="mt-8 text-center border-t border-outline-variant/30 pt-6">
                <p class="text-sm text-on-surface-variant font-light">
                    Đã có tài khoản trên sàn? 
                    <a class="text-primary font-bold hover:underline hover:text-primary-hover ml-1 transition-colors" 
                       href="${pageContext.request.contextPath}/auth/login">Đăng nhập ngay</a>
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
         * Switches the registration forms between Customer and Shop Owner
         * @param {string} tabType - 'CUSTOMER' or 'SHOP_OWNER'
         */
        function switchTab(tabType) {
            const hiddenInput = document.getElementById('accountType');
            const tabCustomer = document.getElementById('tabCustomer');
            const tabShop = document.getElementById('tabShop');
            const shopFields = document.getElementById('shopFields');
            
            // Get inputs inside shopFields
            const shopInputs = shopFields.querySelectorAll('input');

            hiddenInput.value = tabType;

            if (tabType === 'CUSTOMER') {
                // Style tabs
                tabCustomer.className = "flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 bg-white text-primary shadow-sm";
                tabShop.className = "flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 text-on-surface-variant hover:text-primary";
                
                // Hide Advanced Fields
                shopFields.classList.add('hidden');
                
                // Remove required flags for hidden fields
                shopInputs.forEach(input => {
                    input.removeAttribute('required');
                });
            } else {
                // Style tabs
                tabShop.className = "flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 bg-white text-primary shadow-sm";
                tabCustomer.className = "flex-1 py-3 px-4 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 text-on-surface-variant hover:text-primary";
                
                // Show Advanced Fields
                shopFields.classList.remove('hidden');
                
                // Add required flags for validation
                document.getElementById('storeName').setAttribute('required', 'required');
                document.getElementById('businessEmail').setAttribute('required', 'required');
                document.getElementById('address').setAttribute('required', 'required');
            }
        }

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

        /**
         * Handles drag and drop or manual file selection visual feedbacks
         * @param {HTMLInputElement} input
         */
        function handleFileSelect(input) {
            const label = document.getElementById('uploadLabel');
            const fileCount = input.files.length;
            if (fileCount > 0) {
                label.innerHTML = `Đã chọn <span class="text-primary font-bold">${fileCount} tệp</span> tài liệu`;
            } else {
                label.innerHTML = `Kéo và thả tài liệu vào đây hoặc <span class="text-primary font-bold">chọn tệp từ thiết bị</span>`;
            }
        }

        // Auto restore correct tab on DOM content load
        document.addEventListener('DOMContentLoaded', () => {
            const currentType = document.getElementById('accountType').value;
            switchTab(currentType);
        });
    </script>
</body>
</html>
