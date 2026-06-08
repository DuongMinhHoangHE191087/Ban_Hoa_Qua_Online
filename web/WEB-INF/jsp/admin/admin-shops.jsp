<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý toàn bộ cửa hàng – Admin Verdant Market</title>
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
                        surface:      '#ffffff',
                        'surface-2':  '#f8fafc',
                        border:       '#e2ece7',
                        'txt':        '#0f172a',
                        'txt-2':      '#475569',
                        'txt-3':      '#94a3b8',
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Segoe UI',-apple-system,sans-serif; }
        .glass-card { background:#fff; border:1px solid #e2ece7; border-radius:1rem; box-shadow:0 1px 3px rgba(0,0,0,.05),0 4px 16px -4px rgba(20,83,45,.06); }
    </style>
</head>
<body>
<div class="admin-layout flex h-screen overflow-hidden">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="manage-shops"/>
    </jsp:include>

    <%-- Main --%>
    <main class="flex-1 overflow-y-auto p-6 md:p-8">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-white border border-slate-200 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-slate-800 tracking-tight">Tổng Quản Lý Cửa Hàng</h1>
                <p class="text-slate-500 text-sm mt-1">Giám sát và kiểm soát hoạt động của tất cả các cửa hàng trên hệ thống.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-emerald-50 text-emerald-700 px-4 py-2 rounded-xl border border-emerald-100 font-bold">
                <i class="fa-solid fa-store"></i> ${fn:length(shopList)} Gian Hàng
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Shops list panel --%>
        <div class="glass-card overflow-hidden">
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-list-check text-primary mr-1"></i> Danh Sách Cửa Hàng</h3>
                <div class="relative w-full sm:w-64">
                    <input type="text" id="shopSearch" placeholder="Tìm tên cửa hàng..." 
                           class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none pl-9 pr-4 py-2 text-xs bg-white transition-all">
                    <i class="fa-solid fa-search text-txt-3 absolute left-3 top-2.5 text-xs"></i>
                </div>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">Gian Hàng</th>
                            <th class="px-6 py-3.5 font-bold">UID Chủ Shop</th>
                            <th class="px-6 py-3.5 font-bold">Địa Chỉ</th>
                            <th class="px-6 py-3.5 font-bold text-center">Đánh Giá</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng Thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody id="shopTableBody" class="divide-y divide-slate-100">
                        <c:choose>
                            <c:when test="${empty shopList}">
                                <tr><td colspan="6" class="px-6 py-12 text-center text-slate-400">Không có cửa hàng nào trên hệ thống.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="shop" items="${shopList}">
                                    <tr class="hover:bg-slate-50 transition-colors">
                                        <td class="px-6 py-4">
                                            <strong class="shop-name-col block text-slate-800">${shop.shopName}</strong>
                                            <span class="text-xs text-slate-500 line-clamp-1 max-w-[200px]" title="${fn:escapeXml(shop.shopDescription)}">${shop.shopDescription}</span>
                                        </td>
                                        <td class="px-6 py-4 font-mono text-xs text-primary font-bold">#${shop.userId}</td>
                                        <td class="px-6 py-4 text-xs text-slate-600 line-clamp-2 max-w-[250px]">${shop.deliveryAddress}</td>
                                        <td class="px-6 py-4 text-center">
                                            <span class="inline-flex items-center gap-1 bg-amber-50 text-amber-600 px-2 py-0.5 rounded font-bold text-xs border border-amber-200">
                                                ${shop.rating} <i class="fa-solid fa-star text-[10px]"></i>
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${shop.approvalStatus == 'APPROVED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-bold">
                                                        <i class="fa-solid fa-check-circle"></i> Đang Hoạt Động
                                                    </span>
                                                </c:when>
                                                <c:when test="${shop.approvalStatus == 'SUSPENDED'}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-red-50 border border-red-200 text-red-700 text-xs font-bold">
                                                        <i class="fa-solid fa-ban"></i> Đã Đình Chỉ
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-600 text-xs font-bold">
                                                        ${shop.approvalStatus}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 text-center">
                                            <c:if test="${shop.approvalStatus == 'APPROVED'}">
                                                <form action="${pageContext.request.contextPath}/admin/shops/manage" method="POST" class="inline" onsubmit="return confirm('Bạn có chắc muốn đình chỉ cửa hàng này? Họ sẽ không thể bán hàng nữa.');">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="suspend">
                                                    <input type="hidden" name="profileId" value="${shop.profileId}">
                                                    <button type="submit" class="bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm">
                                                        Đình Chỉ
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${shop.approvalStatus == 'SUSPENDED'}">
                                                <form action="${pageContext.request.contextPath}/admin/shops/manage" method="POST" class="inline" onsubmit="return confirm('Mở lại hoạt động cho cửa hàng này?');">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="activate">
                                                    <input type="hidden" name="profileId" value="${shop.profileId}">
                                                    <button type="submit" class="bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shadow-sm">
                                                        Mở Khóa
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </main>
</div>

<script>
    document.getElementById('shopSearch').addEventListener('input', function(e) {
        const term = e.target.value.toLowerCase();
        document.querySelectorAll('#shopTableBody tr').forEach(row => {
            const nameEl = row.querySelector('.shop-name-col');
            if(!nameEl) return;
            const name = nameEl.textContent.toLowerCase();
            row.style.display = name.includes(term) ? '' : 'none';
        });
    });
</script>
</body>
</html>
