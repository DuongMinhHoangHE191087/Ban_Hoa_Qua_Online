<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout flex h-screen overflow-hidden">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="profile"/>
    </jsp:include>

    <%-- Main --%>
    <main class="flex-1 overflow-y-auto p-6 md:p-8 animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-surface border border-border p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Hồ Sơ Cá Nhân</h1>
                <p class="text-txt-2 text-sm mt-1">Quản lý thông tin cá nhân và bảo mật tài khoản quản trị.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-primary-lt text-primary px-4 py-2 rounded-xl border border-primary-fixed font-bold">
                <i class="fa-solid fa-user-shield"></i> Admin
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <%-- Cập nhật thông tin --%>
            <div class="glass-card p-6">
                <h3 class="font-bold text-txt text-lg mb-6 border-b border-slate-100 pb-3">
                    <i class="fa-solid fa-user-edit text-primary mr-2"></i> Thông Tin Cơ Bản
                </h3>
                <form action="${pageContext.request.contextPath}/admin/profile" method="POST" class="space-y-4">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                    <input type="hidden" name="action" value="updateInfo">
                    
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Email (Tài khoản)</label>
                        <input type="email" value="${adminUser.email}" disabled 
                               class="w-full rounded-xl border border-slate-200 bg-slate-100 px-4 py-2.5 text-sm text-slate-500 cursor-not-allowed">
                        <p class="text-[10px] text-slate-400 mt-1">Không thể thay đổi email đăng nhập.</p>
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Họ và Tên</label>
                        <input type="text" name="fullName" value="${adminUser.fullName}" required 
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2.5 text-sm bg-white transition-all">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Số điện thoại</label>
                        <input type="tel" name="phone" value="${adminUser.phone}" 
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2.5 text-sm bg-white transition-all">
                    </div>

                    <div class="pt-4">
                        <button type="submit" class="w-full bg-primary hover:bg-primary-dk text-white font-bold py-3 rounded-xl shadow-md transition-all active:scale-95">
                            Cập Nhật Thông Tin
                        </button>
                    </div>
                </form>
            </div>

            <%-- Đổi mật khẩu --%>
            <div class="glass-card p-6">
                <h3 class="font-bold text-txt text-lg mb-6 border-b border-slate-100 pb-3">
                    <i class="fa-solid fa-lock text-primary mr-2"></i> Đổi Mật Khẩu
                </h3>
                <form action="${pageContext.request.contextPath}/admin/profile" method="POST" class="space-y-4">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                    <input type="hidden" name="action" value="updatePassword">
                    
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Mật khẩu hiện tại</label>
                        <input type="password" name="oldPassword" required 
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2.5 text-sm bg-white transition-all">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Mật khẩu mới</label>
                        <input type="password" name="newPassword" required minlength="6" 
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2.5 text-sm bg-white transition-all">
                    </div>

                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-1">Xác nhận mật khẩu mới</label>
                        <input type="password" name="confirmPassword" required minlength="6" 
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2.5 text-sm bg-white transition-all">
                    </div>

                    <div class="pt-4">
                        <button type="submit" class="w-full bg-slate-800 hover:bg-slate-900 text-white font-bold py-3 rounded-xl shadow-md transition-all active:scale-95">
                            Đổi Mật Khẩu
                        </button>
                    </div>
                </form>
            </div>
        </div>

    </main>
</div>
</body>
</html>
