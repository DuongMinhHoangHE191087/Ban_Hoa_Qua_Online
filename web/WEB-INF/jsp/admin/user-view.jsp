<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết người dùng – Admin Verdant Market</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary:      '#4d661c',
                        'primary-dk': '#364e03',
                        surface:      '#ffffff',
                        'surface-2':  '#f8fafc',
                        border:       '#e2ece7',
                        'txt':        '#0f172a',
                        'txt-2':      '#475569',
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Segoe UI',sans-serif; }
        .glass-card {
            background:#fff;
            border:1px solid #e2ece7;
            border-radius:1rem;
            box-shadow:0 1px 3px rgba(0,0,0,.05),0 4px 16px -4px rgba(20,83,45,.06);
        }
        .detail-label {
            font-size: 0.75rem;
            font-weight: 700;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.25rem;
        }
        .detail-value {
            font-size: 1rem;
            font-weight: 500;
            color: #0f172a;
            padding: 0.75rem 1rem;
            background: #f8fafc;
            border: 1px solid #e2ece7;
            border-radius: 0.75rem;
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
        
        <div class="mb-6 flex items-center justify-between">
            <a href="${pageContext.request.contextPath}/admin/users" class="text-primary hover:text-primary-dk font-bold flex items-center gap-2 transition-colors">
                <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách
            </a>
            <h1 class="text-2xl font-extrabold text-txt">Chi Tiết Người Dùng</h1>
        </div>

        <div class="glass-card max-w-3xl mx-auto overflow-hidden">
            <div class="bg-gradient-to-r from-primary to-primary-dk p-6 text-white flex items-center gap-4">
                <div class="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center text-2xl font-bold border-2 border-white/30 shadow-inner">
                    <c:choose>
                        <c:when test="${not empty user.avatarUrl}">
                            <img src="${user.avatarUrl}" alt="Avatar" class="w-full h-full rounded-full object-cover">
                        </c:when>
                        <c:otherwise>
                            <i class="fa-solid fa-user"></i>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <h2 class="text-xl font-bold">${user.fullName}</h2>
                    <p class="text-white/80 text-sm mt-1">ID: #${user.userId} • Đăng ký: <fmt:parseDate value="${user.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/><fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedDate}"/></p>
                </div>
                <div class="ml-auto">
                    <c:choose>
                        <c:when test="${user.status == 'ACTIVE'}">
                            <span class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-emerald-400/20 text-emerald-100 border border-emerald-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-solid fa-check"></i> HOẠT ĐỘNG
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-red-400/20 text-red-100 border border-red-400/30 text-xs font-bold shadow-sm">
                                <i class="fa-solid fa-lock"></i> ĐÃ KHÓA
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="p-8 grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <div class="detail-label"><i class="fa-regular fa-id-card mr-1"></i> Họ và tên</div>
                    <div class="detail-value">${user.fullName}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-regular fa-envelope mr-1"></i> Địa chỉ Email</div>
                    <div class="detail-value">${user.email} <c:if test="${user.emailVerified}"><i class="fa-solid fa-circle-check text-emerald-500 ml-1 text-sm" title="Đã xác thực"></i></c:if></div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-phone mr-1"></i> Số điện thoại</div>
                    <div class="detail-value">${empty user.phone ? '<span class="text-txt-3 italic">Chưa cập nhật</span>' : user.phone}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-user-shield mr-1"></i> Vai trò (Role)</div>
                    <div class="detail-value">
                        <span class="inline-block px-2.5 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-bold uppercase border border-indigo-100">
                            ${user.role}
                        </span>
                    </div>
                </div>
                <div class="md:col-span-2">
                    <div class="detail-label"><i class="fa-solid fa-map-location-dot mr-1"></i> Địa chỉ mặc định</div>
                    <div class="detail-value">${empty user.userAddress ? '<span class="text-txt-3 italic">Chưa có địa chỉ</span>' : user.userAddress}</div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-clock-rotate-left mr-1"></i> Cập nhật lần cuối</div>
                    <div class="detail-value text-sm">
                        <c:choose>
                            <c:when test="${not empty user.updatedAt}">
                                <fmt:parseDate value="${user.updatedAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedUpdDate" type="both"/>
                                <fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${parsedUpdDate}"/>
                            </c:when>
                            <c:otherwise><span class="text-txt-3">N/A</span></c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div>
                    <div class="detail-label"><i class="fa-solid fa-triangle-exclamation mr-1"></i> Đăng nhập sai</div>
                    <div class="detail-value text-sm ${user.failedLoginCount > 0 ? 'text-amber-600 font-bold' : ''}">
                        ${user.failedLoginCount} lần
                    </div>
                </div>
            </div>
            
            <div class="px-8 py-4 bg-slate-50 border-t border-border flex justify-end">
                <a href="${pageContext.request.contextPath}/admin/users" class="px-6 py-2.5 bg-white border border-slate-300 hover:bg-slate-100 text-txt-2 font-bold rounded-xl transition-colors cursor-pointer text-sm">
                    Đóng
                </a>
            </div>
        </div>
    </main>
</div>
</body>
</html>
