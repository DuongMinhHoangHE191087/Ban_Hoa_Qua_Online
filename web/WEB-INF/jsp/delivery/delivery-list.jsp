<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Danh sách giao hàng"/>
</jsp:include>

<script src="${pageContext.request.contextPath}/assets/js/tailwind.js?plugins=forms"></script>
<script>
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#16A34A', 'primary-hover': '#15803D', 'primary-light': '#DCFCE7',
                'txt': '#0F172A', 'txt-2': '#475569', 'txt-3': '#94A3B8',
                'border-c': '#BBF7D0', 'bg-page': '#F0FDF4'
            },
            fontFamily: { sans: ['Lexend', 'sans-serif'] }
        }
    }
}
</script>

<style>
body { background: #F0FDF4; }
.glass-card { background: rgba(255,255,255,0.88); backdrop-filter: blur(14px); border: 1px solid rgba(187,247,208,0.6); box-shadow: 0 4px 24px -6px rgba(22,163,74,0.08); }
</style>

<main class="max-w-7xl mx-auto px-4 md:px-8 py-10 font-sans text-txt">

    <%-- Back Button + Title --%>
    <div class="mb-7 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-2xl bg-primary flex items-center justify-center shadow-md">
                <i class="fa-solid fa-list-check text-white text-lg"></i>
            </div>
            <div>
                <h1 class="text-2xl font-extrabold text-[#14532D] tracking-tight">Danh sách giao hàng</h1>
                <p class="text-txt-3 text-xs">Tổng hợp tất cả các đơn hàng đang giao nhận</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/delivery/dashboard"
           class="text-primary hover:text-primary-hover text-sm font-bold flex items-center gap-1.5 w-fit">
             Quay lại Dashboard <i class="fa-solid fa-arrow-right"></i>
        </a>
    </div>

    <%-- Placeholder list view --%>
    <div class="glass-card rounded-3xl p-6 text-center py-20 px-6">
        <div class="w-20 h-20 rounded-full bg-primary-light flex items-center justify-center mx-auto mb-5">
            <i class="fa-solid fa-clock-rotate-left text-3xl text-primary"></i>
        </div>
        <h2 class="text-lg font-bold text-[#0C4A6E] mb-2">Tính năng đang phát triển</h2>
        <p class="text-txt-3 text-sm max-w-md mx-auto mb-6">Trang thống kê lịch sử giao nhận chi tiết đang được đồng bộ hóa. Vui lòng sử dụng Dashboard để quản lý trạng thái các đơn hàng hiện tại.</p>
        <a href="${pageContext.request.contextPath}/delivery/dashboard" class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white font-bold px-6 py-3 rounded-2xl transition-all shadow-md active:scale-95">
            <i class="fa-solid fa-truck-fast"></i> Truy cập Dashboard ngay
        </a>
    </div>

</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
