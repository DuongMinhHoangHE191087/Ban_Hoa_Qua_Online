<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gửi Thông báo – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="notifications"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-surface border border-border p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Gửi Thông Báo Hệ Thống</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Soạn và gửi thông báo truyền thông, bảo trì, khuyến mãi hàng loạt.</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-primary-lt text-primary px-4 py-2 rounded-xl border border-primary-fixed font-bold">
                <i class="fa-solid fa-bullhorn text-primary"></i>
                <span class="text-xs font-bold uppercase tracking-wider">Mạng Quảng Bá</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Two Columns Layout --%>
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
            <%-- Form Column --%>
            <div class="xl:col-span-1">
                <div class="glass-card p-6">
                    <h3 class="font-black text-txt text-base mb-4 border-b border-border pb-3 flex items-center gap-2">
                        <i class="fa-solid fa-pen-to-square text-primary"></i> Soạn Thông Báo
                    </h3>
                    
                    <form action="${pageContext.request.contextPath}/admin/notifications" method="POST" 
                          onsubmit="return confirmSend(event)">
                        <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                        
                        <div class="mb-4">
                            <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Gửi tới Nhóm đối tượng <span class="text-red-500">*</span></label>
                            <select id="target" name="target" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm bg-white transition-all cursor-pointer" required>
                                <option value="ALL">Tất cả Người dùng</option>
                                <option value="CUSTOMER">Chỉ Khách hàng (CUSTOMER)</option>
                                <option value="SHOP_OWNER">Chỉ Cửa hàng (SHOP_OWNER)</option>
                                <option value="DELIVERY">Chỉ Tài xế (DELIVERY)</option>
                            </select>
                        </div>
                        
                        <div class="mb-4">
                            <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Tiêu đề thông báo <span class="text-red-500">*</span></label>
                            <input type="text" id="title" name="title" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all" 
                                   required placeholder="Nhập tiêu đề ngắn gọn...">
                        </div>
                        
                        <div class="mb-6">
                            <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Nội dung chi tiết <span class="text-red-500">*</span></label>
                            <textarea id="message" name="message" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm resize-none transition-all" 
                                      rows="5" required placeholder="Nhập nội dung thông báo đầy đủ..."></textarea>
                        </div>
                        
                        <button type="submit" class="w-full py-3 bg-primary hover:bg-primary-dk text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                            <i class="fa-solid fa-paper-plane mr-1"></i> Gửi thông báo
                        </button>
                    </form>
                </div>
            </div>

            <%-- History Column --%>
            <div class="xl:col-span-2">
                <div class="glass-card overflow-hidden">
                    <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                        <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-history text-primary mr-1"></i> Lịch Sử Gửi</h3>
                        <span class="inline-flex items-center px-2.5 py-1 rounded-full bg-primary-lt border border-[#d9f99d] text-primary text-xs font-bold">
                            ${notificationList.size()} bản ghi
                        </span>
                    </div>

                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-sm">
                            <thead>
                                <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                                    <th class="px-6 py-3.5 font-bold">ID</th>
                                    <th class="px-6 py-3.5 font-bold">Thời gian gửi</th>
                                    <th class="px-6 py-3.5 font-bold">Tiêu đề</th>
                                    <th class="px-6 py-3.5 font-bold">Nội dung</th>
                                    <th class="px-6 py-3.5 font-bold">Người nhận</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-[#f1f5f9]">
                                <c:choose>
                                    <c:when test="${empty notificationList}">
                                        <tr>
                                            <td colspan="5" class="px-6 py-12 text-center text-txt-3">
                                                <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                                Chưa có thông báo hệ thống nào được gửi.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="n" items="${notificationList}">
                                            <tr>
                                                <td class="px-6 py-4 font-mono font-bold text-primary">#${n.notificationId}</td>
                                                <td class="px-6 py-4 text-xs text-txt-2 whitespace-nowrap">
                                                    <fmt:formatDate value="${n.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                </td>
                                                <td class="px-6 py-4 font-semibold text-txt"><c:out value="${n.title}"/></td>
                                                <td class="px-6 py-4 text-txt-2 text-xs max-w-[250px] truncate" title="<c:out value='${n.message}'/>">
                                                    <c:out value="${n.message}"/>
                                                </td>
                                                <td class="px-6 py-4 text-xs font-mono text-txt-2 bg-[#f8fafc]/40">Người dùng #${n.userId}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

    </main>
</div>

<script>
    function confirmSend(event) {
        event.preventDefault();
        const targetSelect = document.getElementById('target');
        const targetText = targetSelect.options[targetSelect.selectedIndex].text;
        
        Swal.fire({
            title: 'Gửi thông báo?',
            html: 'Hành động này sẽ gửi thông báo hàng loạt tới: <b class="text-primary">' + targetText + '</b>.<br><small class="text-txt-3">Bạn chắc chắn muốn tiếp tục?</small>',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#4d661c',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Đúng, gửi ngay',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }
</script>
</body>
</html>
