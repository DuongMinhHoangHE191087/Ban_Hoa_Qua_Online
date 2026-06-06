<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Hồ sơ cá nhân" />
</jsp:include>

<!-- Load Tailwind CSS Play Script -->
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    primary:         '#4d661c',
                    'primary-hover': '#364e03',
                    'primary-lt':    '#f0f7e6',
                    border:          '#e2ece7',
                    'txt':           '#0f172a',
                    'txt-2':         '#475569',
                    'txt-3':         '#94a3b8',
                },
                fontFamily: { sans: ['Lexend', 'sans-serif'] }
            }
        }
    }
</script>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 min-h-screen">
    <div class="flex flex-col lg:flex-row gap-8">
        
        <!-- Sidebar Navigation (Desktop Navigation Container) -->
        <aside class="w-full lg:w-64 shrink-0">
            <div class="bg-white/80 backdrop-blur-md border border-white/50 rounded-2xl p-5 shadow-sm sticky top-24">
                <h2 class="text-xs font-bold uppercase tracking-wider text-txt-2 mb-4 px-2">Tài khoản của tôi</h2>
                <nav class="flex flex-col gap-1.5" id="profile-nav">
                    <a class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all duration-200 bg-primary text-white" 
                       href="#profile-section" id="nav-profile">
                        <i class="fa-solid fa-user text-base w-5 text-center"></i>
                        <span>Thông tin cá nhân</span>
                    </a>
                    <a class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all duration-200 text-txt-2 hover:bg-primary-lt hover:text-primary" 
                       href="#address-section" id="nav-address">
                        <i class="fa-solid fa-map-location-dot text-base w-5 text-center"></i>
                        <span>Sổ địa chỉ</span>
                    </a>
                    <a class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all duration-200 text-txt-2 hover:bg-primary-lt hover:text-primary" 
                       href="#security-section" id="nav-security">
                        <i class="fa-solid fa-shield-halved text-base w-5 text-center"></i>
                        <span>Đổi mật khẩu</span>
                    </a>
                </nav>
            </div>
        </aside>

        <!-- Main Content Panel Areas -->
        <main class="flex-1 space-y-8">
            
            <!-- Profile Info Section -->
            <section id="profile-section" class="bg-white/85 backdrop-blur-md border border-white/50 rounded-2xl p-6 md:p-8 shadow-sm scroll-mt-24">
                <h2 class="text-lg font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-user text-primary"></i> Thông tin tài khoản
                </h2>
                
                <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data" class="space-y-6">
                    <input type="hidden" name="action" value="updateProfile">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                    <!-- Avatar Upload Component -->
                    <div class="flex flex-col sm:flex-row items-center gap-6 pb-6 border-b border-gray-100">
                        <div class="relative w-28 h-28 rounded-full overflow-hidden border-2 border-primary/20 shadow-sm cursor-pointer group shrink-0" 
                             onclick="document.getElementById('avatarInput').click();" 
                             title="Click để đổi ảnh đại diện">
                            <c:choose>
                                <c:when test="${not empty user.avatarUrl}">
                                    <img id="avatarPreview" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" 
                                         src="${user.avatarUrl.startsWith('http') ? user.avatarUrl : pageContext.request.contextPath.concat('/').concat(user.avatarUrl)}" alt="Avatar">
                                </c:when>
                                <c:otherwise>
                                    <img id="avatarPreview" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" 
                                         src="${pageContext.request.contextPath}/assets/images/default-avatar.svg" alt="Default Avatar">
                                </c:otherwise>
                            </c:choose>
                            <div class="absolute inset-0 bg-black/40 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                <i class="fa-solid fa-camera text-xl"></i>
                            </div>
                        </div>
                        <div class="text-center sm:text-left space-y-2">
                            <h3 class="text-base font-extrabold text-txt"><c:out value="${user.fullName}"/></h3>
                            <div class="flex items-center justify-center sm:justify-start gap-2">
                                <span class="text-xs text-txt-3">Vai trò:</span>
                                <span class="px-2.5 py-0.5 bg-primary-lt text-primary border border-primary/10 rounded-full text-xs font-bold uppercase"><c:out value="${user.role}"/></span>
                            </div>
                            <input type="file" name="avatar" id="avatarInput" accept="image/jpeg,image/png,image/webp" class="hidden">
                            <button type="button" class="px-4 py-2 border border-border hover:border-primary text-xs font-bold text-txt-2 hover:text-primary rounded-xl transition-colors cursor-pointer" 
                                    onclick="document.getElementById('avatarInput').click();">Chọn ảnh mới</button>
                            <div class="text-[10px] text-txt-3">Chấp nhận JPG, PNG, WEBP tối đa 5MB.</div>
                        </div>
                    </div>

                    <!-- Personal Information Form fields -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="fullName">Họ và tên <span class="text-red-500">*</span></label>
                            <input type="text" id="fullName" name="fullName" value="<c:out value="${user.fullName}"/>" required
                                   class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                        </div>
                        <div class="flex flex-col gap-1.5">
                            <label class="text-xs font-bold text-txt-2" for="phone">Số điện thoại</label>
                            <input type="text" id="phone" name="phone" value="<c:out value="${user.phone}"/>" placeholder="Ví dụ: 0987654321"
                                   class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                            <div class="text-[10px] text-txt-3">Định dạng số điện thoại Việt Nam (10 chữ số).</div>
                        </div>
                    </div>

                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-txt-3" for="email">Địa chỉ Email (Tên đăng nhập)</label>
                        <input type="email" id="email" value="<c:out value="${user.email}"/>" disabled
                               class="w-full px-4 py-2.5 bg-gray-50 border border-border rounded-xl text-sm text-txt-3 cursor-not-allowed">
                        <div class="text-[10px] text-txt-3">Bạn không thể thay đổi địa chỉ email đăng ký tài khoản.</div>
                    </div>

                    <div class="flex justify-end pt-4">
                        <button type="submit" class="flex items-center gap-2 px-6 py-2.5 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                        </button>
                    </div>
                </form>
            </section>

            <!-- Address Section -->
            <section id="address-section" class="bg-white/85 backdrop-blur-md border border-white/50 rounded-2xl p-6 md:p-8 shadow-sm scroll-mt-24">
                <h2 class="text-lg font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-map-location-dot text-emerald-600"></i> Sổ địa chỉ giao hàng
                </h2>
                
                <form action="${pageContext.request.contextPath}/profile" method="post" class="space-y-6">
                    <input type="hidden" name="action" value="updateAddress">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                    <div class="flex flex-col gap-1.5">
                        <label class="text-xs font-bold text-txt-2" for="userAddress">Địa chỉ nhận hàng mặc định</label>
                        <textarea id="userAddress" name="userAddress" rows="3" placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố..."
                                  class="w-full px-4 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all resize-none"><c:out value="${user.userAddress}"/></textarea>
                        <div class="text-[10px] text-txt-3">Địa chỉ này sẽ được điền tự động khi bạn tiến hành thanh toán đơn hàng.</div>
                    </div>

                    <div class="flex justify-end pt-4">
                        <button type="submit" class="flex items-center gap-2 px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                            <i class="fa-solid fa-floppy-disk"></i> Cập nhật địa chỉ
                        </button>
                    </div>
                </form>
            </section>

            <!-- Password / Security Section -->
            <section id="security-section" class="bg-white/85 backdrop-blur-md border border-white/50 rounded-2xl p-6 md:p-8 shadow-sm scroll-mt-24">
                <h2 class="text-lg font-bold text-txt mb-6 pb-3 border-b border-gray-100 flex items-center gap-2">
                    <i class="fa-solid fa-shield-halved text-amber-600"></i> Đổi mật khẩu tài khoản
                </h2>

                <c:choose>
                    <c:when test="${empty user.passwordHash}">
                        <div class="p-4 bg-blue-50 border border-blue-100 rounded-xl text-blue-800 text-xs font-medium flex items-center gap-2.5">
                            <i class="fa-solid fa-circle-info text-base"></i> 
                            <span>Tài khoản này được đăng nhập trực tiếp qua <strong>Google OAuth</strong>. Mật khẩu không áp dụng.</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/profile" method="post" id="passwordForm" class="space-y-6">
                            <input type="hidden" name="action" value="changePassword">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">

                            <!-- Current Password -->
                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-bold text-txt-2" for="currentPassword">Mật khẩu hiện tại <span class="text-red-500">*</span></label>
                                <div class="relative w-full">
                                    <input type="password" id="currentPassword" name="currentPassword" required
                                           class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                    <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                            onclick="togglePasswordVisibility('currentPassword', this)">
                                        <i class="fa-solid fa-eye text-sm"></i>
                                    </button>
                                </div>
                            </div>

                            <!-- New Password Form Grid -->
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <!-- New Password -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-txt-2" for="newPassword">Mật khẩu mới <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="newPassword" name="newPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('newPassword', this)">
                                            <i class="fa-solid fa-eye text-sm"></i>
                                        </button>
                                    </div>
                                    <div class="text-[10px] text-txt-3">Độ dài từ 8 đến 64 ký tự.</div>
                                    
                                    <!-- Password Strength Meter Component -->
                                    <div class="mt-2 space-y-1.5">
                                        <div class="flex items-center justify-between text-[10px] font-semibold">
                                            <span class="text-txt-3">Độ mạnh mật khẩu:</span>
                                            <span id="strengthText" class="text-txt-3 font-bold">Trống</span>
                                        </div>
                                        <div class="w-full bg-gray-200 h-1.5 rounded-full overflow-hidden flex gap-1">
                                            <div id="strengthSegment1" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment2" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                            <div id="strengthSegment3" class="h-full w-1/3 bg-gray-300 rounded-full transition-all"></div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Confirm Password -->
                                <div class="flex flex-col gap-1.5">
                                    <label class="text-xs font-bold text-txt-2" for="confirmPassword">Xác nhận mật khẩu mới <span class="text-red-500">*</span></label>
                                    <div class="relative w-full">
                                        <input type="password" id="confirmPassword" name="confirmPassword" required
                                               class="w-full pl-4 pr-10 py-2.5 border border-border rounded-xl text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition-all">
                                        <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-txt-3 hover:text-txt transition-colors border-0 bg-transparent cursor-pointer py-1" 
                                                onclick="togglePasswordVisibility('confirmPassword', this)">
                                            <i class="fa-solid fa-eye text-sm"></i>
                                        </button>
                                    </div>
                                    <span id="matchText" class="text-[10px] text-red-600 hidden">Mật khẩu xác nhận không khớp!</span>
                                </div>
                            </div>

                            <div class="flex justify-end pt-4">
                                <button type="submit" id="submitPasswordBtn" class="flex items-center gap-2 px-6 py-2.5 bg-amber-600 hover:bg-amber-700 text-white text-sm font-bold rounded-xl transition-all shadow-sm border-0 cursor-pointer">
                                    <i class="fa-solid fa-key"></i> Đặt lại mật khẩu
                                </button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </div>
</div>

<script>
    // Preview avatar image before uploading
    document.getElementById('avatarInput').addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            // Check size (5MB)
            if (file.size > 5 * 1024 * 1024) {
                alert('File ảnh không được vượt quá 5MB!');
                event.target.value = '';
                return;
            }
            
            // Check format
            const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
            if (!allowedTypes.includes(file.type)) {
                alert('Chỉ chấp nhận các định dạng ảnh: JPG, PNG, WEBP');
                event.target.value = '';
                return;
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById('avatarPreview').src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    // Toggle password visibility
    function togglePasswordVisibility(inputId, button) {
        const input = document.getElementById(inputId);
        const icon = button.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }

    // Password strength logic
    const newPasswordInput = document.getElementById('newPassword');
    const strengthText = document.getElementById('strengthText');
    const seg1 = document.getElementById('strengthSegment1');
    const seg2 = document.getElementById('strengthSegment2');
    const seg3 = document.getElementById('strengthSegment3');

    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            const pwd = this.value;
            let score = 0;
            
            if (pwd.length >= 8) score++;
            if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
            if (/\d/.test(pwd)) score++;
            if (/[^a-zA-Z\d]/.test(pwd)) score++;
            
            // Reset segments
            seg1.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            seg2.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            seg3.className = 'h-full w-1/3 bg-gray-300 rounded-full transition-all';
            
            if (pwd.length === 0) {
                strengthText.textContent = 'Trống';
                strengthText.className = 'text-txt-3 font-bold';
                return;
            }
            
            if (pwd.length < 8) {
                strengthText.textContent = 'Rất ngắn (Yếu)';
                strengthText.className = 'text-red-500 font-bold';
                seg1.classList.remove('bg-gray-300');
                seg1.classList.add('bg-red-500');
                return;
            }
            
            if (score <= 1) {
                strengthText.textContent = 'Yếu';
                strengthText.className = 'text-red-500 font-bold';
                seg1.classList.remove('bg-gray-300');
                seg1.classList.add('bg-red-500');
            } else if (score === 2 || score === 3) {
                strengthText.textContent = 'Trung bình';
                strengthText.className = 'text-orange-500 font-bold';
                seg1.classList.remove('bg-gray-300');
                seg1.classList.add('bg-orange-500');
                seg2.classList.remove('bg-gray-300');
                seg2.classList.add('bg-orange-500');
            } else if (score >= 4) {
                strengthText.textContent = 'Mạnh';
                strengthText.className = 'text-primary font-bold';
                seg1.classList.remove('bg-gray-300');
                seg1.classList.add('bg-primary');
                seg2.classList.remove('bg-gray-300');
                seg2.classList.add('bg-primary');
                seg3.classList.remove('bg-gray-300');
                seg3.classList.add('bg-primary');
            }
        });
    }

    // Match password fields
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const matchText = document.getElementById('matchText');
    const passwordForm = document.getElementById('passwordForm');

    function checkPasswordMatch() {
        if (!confirmPasswordInput || !newPasswordInput) return true;
        if (confirmPasswordInput.value !== newPasswordInput.value && confirmPasswordInput.value.length > 0) {
            matchText.classList.remove('hidden');
            return false;
        } else {
            matchText.classList.add('hidden');
            return true;
        }
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', checkPasswordMatch);
    }
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', checkPasswordMatch);
    }

    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            // Check password match
            if (!checkPasswordMatch()) {
                e.preventDefault();
                alert('Mật khẩu xác nhận không khớp!');
                return;
            }
            
            // Block Weak or Very Short passwords
            const pwd = newPasswordInput.value;
            if (pwd.length < 8) {
                e.preventDefault();
                alert('Mật khẩu mới phải có độ dài tối thiểu 8 ký tự!');
                return;
            }
        });
    }

    // Scroll Spy highlight navigation menu
    const sections = document.querySelectorAll('section[id]');
    const navItems = {
        'profile-section': document.getElementById('nav-profile'),
        'address-section': document.getElementById('nav-address'),
        'security-section': document.getElementById('nav-security')
    };

    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.2
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const id = entry.target.getAttribute('id');
                const activeNav = navItems[id];
                if (activeNav) {
                    // Reset all other links
                    Object.values(navItems).forEach(nav => {
                        if (nav) {
                            nav.className = "flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all duration-200 text-txt-2 hover:bg-primary-lt hover:text-primary";
                        }
                    });
                    // Set active link style
                    activeNav.className = "flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all duration-200 bg-primary text-white";
                }
            }
        });
    }, observerOptions);

    sections.forEach(sec => observer.observe(sec));

    // Smooth scroll navigation behavior
    const links = document.querySelectorAll('#profile-nav a');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
