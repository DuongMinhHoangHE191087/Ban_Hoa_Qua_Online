<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta content="width=device-width, initial-scale=1.0" name="viewport">
    <title>Trạng thái gian hàng - MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Material Symbols Outlined -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">
    <!-- Tailwind CSS -->
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms,container-queries"></script>
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
        <div class="absolute inset-0 bg-gradient-to-br from-white/90 via-emerald-50/70 to-emerald-100/90 backdrop-blur-[4px]"></div>
    </div>

    <!-- Top AppBar Navigation Header -->
    <header class="flex justify-between items-center w-full px-6 md:px-12 py-4 z-50 fixed top-0 left-0 right-0 border-b border-white/30 bg-white/40 backdrop-blur-md shadow-[0_2px_15px_rgba(20,83,45,0.03)]">
        <div class="flex items-center gap-2">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" class="h-8 w-8 rounded-lg object-cover">
            <div class="text-2xl font-bold text-primary tracking-wide">
                MetaFruit
            </div>
        </div>
        <div class="flex items-center gap-4">
            <span class="text-xs font-medium bg-emerald-100 text-primary px-3 py-1 rounded-full border border-primary/20">
                <c:out value="${sessionScope.user.fullName}"/>
            </span>
            <a href="${pageContext.request.contextPath}/auth/logout" class="text-xs font-semibold text-red-600 hover:underline flex items-center gap-0.5">
                <span class="material-symbols-outlined text-[16px]">logout</span>
                Đăng xuất
            </a>
        </div>
    </header>

    <!-- Main Content Container -->
    <main class="flex-1 flex items-center justify-center pt-28 pb-16 px-4 md:px-8 relative z-10 w-full">
        
        <div class="w-full max-w-2xl glass-card rounded-2xl p-6 md:p-10 transition-all duration-300">
            
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

            <c:choose>
                <%-- Trạng thái CHỜ DUYỆT (PENDING) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'PENDING'}">
                    <div class="text-center mb-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-amber-50 border border-amber-200 text-amber-500 mb-4 animate-pulse">
                            <span class="material-symbols-outlined text-[36px]">hourglass_empty</span>
                        </div>
                        <h1 class="text-2xl font-bold text-amber-700 mb-2">Hồ sơ đang được xét duyệt</h1>
                        <p class="text-sm text-on-surface-variant font-light">
                            Hệ thống đã nhận đơn đăng ký của bạn. Ban quản trị đang tiến hành xác minh thông tin.
                        </p>
                    </div>

                    <!-- Timeline Stepper -->
                    <div class="my-10 bg-white/40 p-6 rounded-xl border border-white/60">
                        <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-6 flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">timeline</span>
                            Tiến trình xử lý hồ sơ
                        </h3>
                        
                        <div class="relative pl-6 space-y-8 border-l-2 border-primary/20">
                            <!-- Step 1 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-primary border-4 border-emerald-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-primary">Nộp đơn đăng ký mở gian hàng</h4>
                                <p class="text-xs text-on-surface-variant mt-1">Đã hoàn thành gửi thông tin cửa hàng lên hệ thống.</p>
                            </div>
                            
                            <!-- Step 2 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-amber-500 border-4 border-amber-100 flex items-center justify-center animate-ping"></div>
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-amber-500 border-4 border-amber-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-amber-600">Ban quản trị thẩm định hồ sơ</h4>
                                <p class="text-xs text-on-surface-variant mt-1">Đang đối chiếu giấy tờ pháp lý và điều kiện kinh doanh. Thời gian duyệt từ 1-3 ngày làm việc.</p>
                            </div>
                            
                            <!-- Step 3 -->
                            <div class="relative">
                                <div class="absolute -left-[31px] top-0.5 w-4 h-4 rounded-full bg-gray-300 border-4 border-gray-100 flex items-center justify-center"></div>
                                <h4 class="text-sm font-bold text-gray-400">Kích hoạt gian hàng chính thức</h4>
                                <p class="text-xs text-on-surface-variant mt-1">Cửa hàng đi vào hoạt động và bắt đầu đăng bán sản phẩm.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Shop Details Card -->
                    <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-3 text-sm">
                        <h4 class="text-xs font-bold text-primary uppercase tracking-wider mb-2">Thông tin gian hàng đăng ký</h4>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Tên cửa hàng:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.shopName}"/></span>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Email kinh doanh:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.businessEmail}"/></span>
                        </div>
                        <div class="grid grid-cols-3 gap-2">
                            <span class="text-on-surface-variant">Địa điểm gom hàng:</span>
                            <span class="col-span-2 font-semibold text-on-surface"><c:out value="${profile.deliveryAddress}"/></span>
                        </div>
                    </div>
                </c:when>

                <%-- Trạng thái BỊ TỪ CHỐI (REJECTED) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'REJECTED'}">
                    <div class="text-center mb-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 border border-red-200 text-red-500 mb-4">
                            <span class="material-symbols-outlined text-[36px]">cancel</span>
                        </div>
                        <h1 class="text-2xl font-bold text-red-700 mb-2">Đơn đăng ký bị từ chối</h1>
                        <p class="text-sm text-on-surface-variant font-light">
                            Hồ sơ của bạn không đủ điều kiện phê duyệt hoặc thông tin không chính xác.
                        </p>
                    </div>

                    <!-- Rejection Reason Card -->
                    <div class="mb-8 p-5 bg-red-50/70 border border-red-200 rounded-xl space-y-2">
                        <h4 class="text-xs font-bold text-red-700 uppercase tracking-wider flex items-center gap-1.5">
                            <span class="material-symbols-outlined text-[16px]">info</span>
                            Lý do từ chối phê duyệt
                        </h4>
                        <p class="text-sm text-red-900 font-medium">
                            <c:out value="${profile.rejectionReason != null ? profile.rejectionReason : 'Tài liệu cung cấp không rõ ràng hoặc không hợp lệ. Vui lòng kiểm tra lại giấy tờ đăng ký kinh doanh.'}"/>
                        </p>
                    </div>

                    <!-- Resubmit Form -->
                    <form action="${pageContext.request.contextPath}/shop/status" method="post" enctype="multipart/form-data" class="space-y-6" id="resubmitForm">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        
                        <div class="bg-white/40 p-5 rounded-xl border border-white/60 space-y-4">
                            <h3 class="text-xs font-bold text-primary uppercase tracking-wider mb-2 flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">edit_note</span>
                                Điều chỉnh & nộp lại thông tin hồ sơ
                            </h3>
                            
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <!-- Store Name -->
                                <div class="flex flex-col gap-1">
                                    <label class="text-xs font-semibold text-primary" for="storeName">Tên cửa hàng *</label>
                                    <div class="relative">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">storefront</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                               id="storeName" name="storeName" value="<c:out value="${profile.shopName}"/>" type="text" required minlength="3">
                                    </div>
                                </div>
                                
                                <!-- Business Email -->
                                <div class="flex flex-col gap-1">
                                    <label class="text-xs font-semibold text-primary" for="businessEmail">Email liên hệ kinh doanh *</label>
                                    <div class="relative">
                                        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">contact_mail</span>
                                        <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                               id="businessEmail" name="businessEmail" value="<c:out value="${profile.businessEmail}"/>" type="email" required>
                                    </div>
                                </div>
                            </div>

                            <!-- Business Address -->
                            <div class="flex flex-col gap-1">
                                <label class="text-xs font-semibold text-primary" for="address">Địa chỉ kinh doanh *</label>
                                <div class="relative">
                                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">pin_drop</span>
                                    <input class="w-full pl-9 pr-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                           id="address" name="address" value="<c:out value="${profile.deliveryAddress}"/>" type="text" required minlength="5">
                                </div>
                            </div>

                            <!-- Shop Description -->
                            <div class="flex flex-col gap-1">
                                <label class="text-xs font-semibold text-primary" for="shopDescription">Mô tả ngắn về cửa hàng</label>
                                <textarea class="w-full px-4 py-2.5 bg-white/70 border border-outline/30 focus:border-primary focus:ring-1 focus:ring-primary rounded-lg text-sm transition-all outline-none placeholder:text-outline-variant/60" 
                                          id="shopDescription" name="shopDescription" rows="3" placeholder="Giới thiệu về sản phẩm đặc trưng hoặc nguồn gốc nông sản của bạn..."><c:out value="${profile.shopDescription}"/></textarea>
                            </div>

                            <!-- Categories Selection -->
                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-semibold text-primary">Danh mục sản phẩm kinh doanh *</label>
                                <div class="grid grid-cols-2 gap-2 mt-1" id="categoryGrid">
                                    <c:choose>
                                        <c:when test="${not empty categories}">
                                            <c:forEach var="cat" items="${categories}">
                                                <c:set var="isChecked" value="false"/>
                                                <%-- Check if category is preferred --%>
                                                <c:if test="${not empty profile.preferredCategories}">
                                                    <c:set var="prefCatContains" value="[${cat.categoryId},"/>
                                                    <c:set var="prefCatContainsMiddle" value=",${cat.categoryId},"/>
                                                    <c:set var="prefCatContainsEnd" value=",${cat.categoryId}]"/>
                                                    <c:set var="prefCatSingle" value="[${cat.categoryId}]"/>
                                                    <c:if test="${profile.preferredCategories == prefCatSingle || 
                                                                  fn:contains(profile.preferredCategories, prefCatContains) || 
                                                                  fn:contains(profile.preferredCategories, prefCatContainsMiddle) || 
                                                                  fn:contains(profile.preferredCategories, prefCatContainsEnd)}">
                                                        <c:set var="isChecked" value="true"/>
                                                    </c:if>
                                                </c:if>
                                                <label class="flex items-center p-3 rounded-lg border border-primary/10 bg-white/40 hover:bg-emerald-50 cursor-pointer transition-all duration-200">
                                                    <input class="rounded text-primary focus:ring-primary border-outline/30 bg-white" 
                                                           name="categoryIds" 
                                                           value="<c:out value="${cat.categoryId}"/>" 
                                                           type="checkbox"
                                                           ${isChecked ? 'checked' : ''}>
                                                    <span class="ml-2.5 text-xs font-medium text-on-surface"><c:out value="${cat.name}"/></span>
                                                </label>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="col-span-2 text-xs text-outline italic">Không tải được danh mục.</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Document Upload -->
                            <div class="flex flex-col gap-1.5">
                                <label class="text-xs font-semibold text-primary">Tải lại tài liệu xác minh (GPKD, chứng nhận...) *</label>
                                <p class="text-[10px] text-outline">Tải lên file mới sẽ ghi đè tài liệu cũ của bạn</p>
                                <div class="border-2 border-dashed border-primary/20 rounded-lg p-5 text-center bg-white/20 hover:bg-emerald-50/50 transition-colors cursor-pointer group relative" id="dropzone">
                                    <input class="absolute inset-0 w-full h-full opacity-0 cursor-pointer" 
                                           id="businessDocs" name="businessDocs" type="file" 
                                           multiple 
                                           accept=".pdf,.jpg,.jpeg,.png,.docx"
                                           onchange="handleFileSelect(this)">
                                    <div class="flex flex-col items-center gap-2 pointer-events-none">
                                        <span class="material-symbols-outlined text-[36px] text-primary/60 group-hover:text-primary transition-colors">cloud_upload</span>
                                        <p class="text-xs font-medium text-on-surface-variant" id="uploadLabel">
                                            Kéo thả tài liệu vào đây hoặc <span class="text-primary font-bold">chọn tệp từ thiết bị</span>
                                        </p>
                                    </div>
                                </div>
                                <ul id="fileList" class="mt-2 space-y-1 hidden"></ul>
                                <p id="fileError" class="text-xs text-red-600 hidden"></p>
                            </div>
                        </div>

                        <!-- Resubmit Button -->
                        <button type="submit" class="w-full mt-4 bg-primary text-white text-sm font-semibold py-3.5 px-6 rounded-lg shadow-md hover:bg-primary-hover hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center gap-2 group cursor-pointer">
                            <span>Gửi lại hồ sơ xét duyệt</span>
                            <span class="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">send</span>
                        </button>
                    </form>
                </c:when>

                <%-- Trạng thái ĐÃ PHÊ DUYỆT (APPROVED) --%>
                <c:when test="${not empty profile and profile.approvalStatus == 'APPROVED'}">
                    <div class="text-center py-6">
                        <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-50 border border-green-200 text-primary mb-6">
                            <span class="material-symbols-outlined text-[48px]">check_circle</span>
                        </div>
                        <h1 class="text-2xl font-bold text-primary mb-2">Gian hàng đã kích hoạt hoạt động</h1>
                        <p class="text-sm text-on-surface-variant font-light mb-8 max-w-md mx-auto">
                            Chúc mừng! Đơn đăng ký của bạn đã được phê duyệt thành công. Bạn đã có toàn quyền truy cập trang quản trị cửa hàng.
                        </p>
                        
                        <div class="flex flex-col gap-3 max-w-sm mx-auto">
                            <a href="${pageContext.request.contextPath}/shop/dashboard" 
                               class="w-full bg-primary text-white text-sm font-semibold py-3 px-6 rounded-lg shadow-md hover:bg-primary-hover transition-all flex items-center justify-center gap-2">
                                <span class="material-symbols-outlined text-[18px]">dashboard</span>
                                Vào trang quản trị của tôi
                            </a>
                            <a href="${pageContext.request.contextPath}/" class="text-xs text-primary font-bold hover:underline">
                                Trở về trang chủ
                            </a>
                        </div>
                    </div>
                </c:when>

                <%-- Chưa có thông tin đơn đăng ký (Chưa lưu) --%>
                <c:otherwise>
                    <div class="text-center py-8">
                        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-50 border border-gray-200 text-outline mb-4">
                            <span class="material-symbols-outlined text-[36px]">contact_support</span>
                        </div>
                        <h1 class="text-xl font-bold text-on-surface mb-2">Chưa tìm thấy đơn đăng ký</h1>
                        <p class="text-sm text-on-surface-variant font-light mb-6">
                            Tài khoản của bạn đã được nâng cấp lên Chủ cửa hàng, nhưng chưa có thông tin hồ sơ được lưu.
                        </p>
                        <a href="${pageContext.request.contextPath}/auth/register?accountType=SHOP_OWNER" 
                           class="inline-flex items-center gap-2 bg-primary text-white text-sm font-semibold py-2.5 px-5 rounded-lg shadow hover:bg-primary-hover transition-colors">
                            <span class="material-symbols-outlined text-[18px]">app_registration</span>
                            Bổ sung thông tin hồ sơ ngay
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
            
        </div>
    </main>

    <!-- Site Footer -->
    <footer class="w-full py-4 text-center border-t border-white/20 bg-white/40 backdrop-blur-md relative z-10">
        <p class="text-xs text-on-surface-variant/80 font-light">&copy; 2026 MetaFruit. Giải pháp nông sản Việt sạch và bền vững.</p>
    </footer>

    <script>
        const MAX_DOC_COUNT = 10;
        const MAX_DOC_SIZE_MB = 25;
        const MAX_DOC_SIZE_BYTES = MAX_DOC_SIZE_MB * 1024 * 1024;
        const ALLOWED_EXTS = ['pdf', 'jpg', 'jpeg', 'png', 'docx'];

        function handleFileSelect(input) {
            const fileListEl = document.getElementById('fileList');
            const fileErrorEl = document.getElementById('fileError');
            const uploadLabel = document.getElementById('uploadLabel');
            const files = Array.from(input.files);

            fileListEl.innerHTML = '';
            fileListEl.classList.add('hidden');
            fileErrorEl.classList.add('hidden');
            fileErrorEl.textContent = '';

            let errors = [];
            if (files.length > MAX_DOC_COUNT) {
                errors.push('Chỉ được chọn tối đa ' + MAX_DOC_COUNT + ' tài liệu.');
            }

            const validFiles = files.slice(0, MAX_DOC_COUNT);
            validFiles.forEach((file) => {
                const ext = file.name.split('.').pop().toLowerCase();
                const li = document.createElement('li');
                li.className = 'flex items-center gap-2 text-xs py-1 px-2 bg-white/50 rounded-lg border border-outline/20';

                if (!ALLOWED_EXTS.includes(ext)) {
                    errors.push('File "' + file.name + '" không được hỗ trợ. Chỉ chấp nhận PDF, JPG, PNG, DOCX.');
                    li.classList.add('border-red-300', 'bg-red-50/50');
                    li.innerHTML = '<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">' + file.name + ' — Sai định dạng</span>';
                } else if (file.size > MAX_DOC_SIZE_BYTES) {
                    errors.push('File "' + file.name + '" vượt quá 25MB.');
                    li.classList.add('border-red-300', 'bg-red-50/50');
                    li.innerHTML = '<span class="material-symbols-outlined text-red-500 text-[16px]">error</span><span class="text-red-600">' + file.name + ' — Quá 25MB</span>';
                } else {
                    li.innerHTML = '<span class="material-symbols-outlined text-primary text-[16px]">description</span><span class="text-on-surface">' + file.name + '</span><span class="ml-auto text-outline">' + (file.size/1024/1024).toFixed(1) + 'MB</span>';
                }
                fileListEl.appendChild(li);
            });

            if (validFiles.length > 0) {
                fileListEl.classList.remove('hidden');
                uploadLabel.innerHTML = 'Đã chọn <span class="text-primary font-bold">' + validFiles.length + ' tệp</span> tài liệu';
            } else {
                uploadLabel.innerHTML = 'Kéo thả tài liệu vào đây hoặc <span class="text-primary font-bold">chọn tệp từ thiết bị</span>';
            }

            if (errors.length > 0) {
                fileErrorEl.textContent = errors[0];
                fileErrorEl.classList.remove('hidden');
            }
        }

        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('resubmitForm');
            if (form) {
                form.addEventListener('submit', function(event) {
                    const existingErrors = form.querySelectorAll('.client-error');
                    existingErrors.forEach(el => el.remove());

                    let hasError = false;

                    function showError(inputEl, message) {
                        hasError = true;
                        const errorDiv = document.createElement('p');
                        errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                        errorDiv.textContent = message;
                        inputEl.closest('.flex-col').appendChild(errorDiv);
                    }

                    const storeNameInput = document.getElementById('storeName');
                    if (storeNameInput.value.trim().length < 3) {
                        showError(storeNameInput, 'Tên cửa hàng phải từ 3 ký tự trở lên.');
                    }

                    const businessEmailInput = document.getElementById('businessEmail');
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(businessEmailInput.value.trim())) {
                        showError(businessEmailInput, 'Email liên hệ kinh doanh không đúng định dạng.');
                    }

                    const addressInput = document.getElementById('address');
                    if (addressInput.value.trim().length < 5) {
                        showError(addressInput, 'Địa chỉ kinh doanh phải từ 5 ký tự trở lên.');
                    }

                    const categoryChecked = form.querySelectorAll('input[name="categoryIds"]:checked');
                    if (categoryChecked.length === 0) {
                        hasError = true;
                        const errorDiv = document.createElement('p');
                        errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                        errorDiv.textContent = 'Vui lòng chọn ít nhất một danh mục sản phẩm.';
                        document.getElementById('categoryGrid').parentNode.appendChild(errorDiv);
                    }

                    const fileInput = document.getElementById('businessDocs');
                    if (fileInput.files.length > 0) {
                        const files = Array.from(fileInput.files);
                        if (files.length > MAX_DOC_COUNT) {
                            hasError = true;
                            const errorDiv = document.createElement('p');
                            errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                            errorDiv.textContent = 'Chỉ được chọn tối đa ' + MAX_DOC_COUNT + ' tài liệu.';
                            document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                        }

                        for (let file of files) {
                            const ext = file.name.split('.').pop().toLowerCase();
                            if (!ALLOWED_EXTS.includes(ext)) {
                                hasError = true;
                                const errorDiv = document.createElement('p');
                                errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                                errorDiv.textContent = 'Tệp "' + file.name + '" không hợp lệ. Chỉ chấp nhận các định dạng PDF, JPG, PNG, DOCX.';
                                document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                                break;
                            }
                            if (file.size > MAX_DOC_SIZE_BYTES) {
                                hasError = true;
                                const errorDiv = document.createElement('p');
                                errorDiv.className = 'client-error text-xs text-red-600 mt-1';
                                errorDiv.textContent = 'Tệp "' + file.name + '" vượt quá giới hạn 25MB.';
                                document.getElementById('dropzone').parentNode.appendChild(errorDiv);
                                break;
                            }
                        }
                    }

                    if (hasError) {
                        event.preventDefault();
                        const firstError = form.querySelector('.client-error');
                        if (firstError) {
                            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }
                    }
                });
            }
        });
    </script>
</body>
</html>
