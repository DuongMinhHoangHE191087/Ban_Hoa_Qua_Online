<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa người dùng – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:      '#4d661c',
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
                        sans: ['Segoe UI','-apple-system','BlinkMacSystemFont','Helvetica Neue','Arial','sans-serif'],
                    },
                    boxShadow: {
                        card: '0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)',
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Segoe UI',-apple-system,sans-serif; }
        .glass-card {
            background:#fff;
            border:1px solid #e2ece7;
            border-radius:1rem;
            box-shadow:0 1px 3px rgba(0,0,0,.05),0 4px 16px -4px rgba(20,83,45,.06);
        }
        select {
            appearance: none;
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%234d661c' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            background-size: 12px 8px;
            padding-right: 2.25rem;
        }
    </style>
</head>
<body>
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="users"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">Sửa Thông Tin Thành Viên</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">Cập nhật họ tên, vai trò và địa chỉ của tài khoản thành viên.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-user-pen text-[#84cc16]"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Cập nhật</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Card form --%>
        <div class="glass-card max-w-2xl mx-auto p-6 md:p-8">
            <h3 class="font-black text-txt text-base mb-6 border-b border-border pb-4 flex items-center gap-2">
                <i class="fa-solid fa-id-card text-primary"></i> Tài khoản: <span class="text-primary font-mono">${user.email}</span>
            </h3>
            
            <form action="${pageContext.request.contextPath}/admin/users/edit" method="POST">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="userId" value="${user.userId}">
                
                <div class="mb-4">
                    <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Địa chỉ Email (Tên đăng nhập)</label>
                    <input type="text" class="w-full rounded-xl border border-slate-300 bg-slate-50 text-txt-3 p-3 text-sm font-mono cursor-not-allowed outline-none select-none" value="${user.email}" disabled>
                    <small class="text-txt-3 text-[10px] mt-1.5 block leading-relaxed">
                        <i class="fa-solid fa-circle-info mr-0.5"></i> Email là định danh duy nhất của tài khoản và không được phép thay đổi.
                    </small>
                </div>

                <div class="mb-4">
                    <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Họ và tên thành viên <span class="text-red-500">*</span></label>
                    <input type="text" name="fullName" value="${user.fullName}" required placeholder="Nhập họ và tên..."
                           class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all">
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                    <div>
                        <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Số điện thoại</label>
                        <input type="text" name="phone" value="${user.phone}" placeholder="Ví dụ: 0912345678"
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm font-mono transition-all">
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Vai trò hệ thống</label>
                        <select name="role" required 
                                class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm bg-white transition-all cursor-pointer">
                            <option value="CUSTOMER" ${user.role == 'CUSTOMER' ? 'selected' : ''}>Khách hàng (CUSTOMER)</option>
                            <option value="SHOP_OWNER" ${user.role == 'SHOP_OWNER' ? 'selected' : ''}>Chủ cửa hàng (SHOP_OWNER)</option>
                            <option value="DELIVERY" ${user.role == 'DELIVERY' ? 'selected' : ''}>Nhân viên giao hàng (DELIVERY)</option>
                            <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>Quản trị viên (ADMIN)</option>
                        </select>
                    </div>
                </div>
                
                <div class="mb-6">
                    <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Địa chỉ giao hàng mặc định</label>
                    <textarea name="userAddress" rows="3" placeholder="Nhập địa chỉ nhà, tên đường, tỉnh/thành phố..."
                              class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm resize-none transition-all">${user.userAddress}</textarea>
                </div>

                <div class="flex items-center gap-3 pt-4 border-t border-border">
                    <button type="submit" 
                            class="flex-1 py-3 bg-primary hover:bg-primary-dk text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                        <i class="fa-solid fa-floppy-disk mr-1"></i> Lưu thay đổi
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/users" 
                       class="py-3 px-6 bg-white hover:bg-slate-50 border border-slate-200 text-txt-2 hover:text-txt font-bold rounded-xl text-xs transition-all active:scale-95 cursor-pointer text-center">
                        Quay lại
                    </a>
                </div>
            </form>
        </div>

    </main>
</div>
</body>
</html>
