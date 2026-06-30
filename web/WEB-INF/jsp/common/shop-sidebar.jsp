<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Shared Sidebar for Shop Owner pages.
  Usage: <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
             <jsp:param name="activePage" value="dashboard|orders|products|inventory|profile|settlement|promotions|reports|settings"/>
         </jsp:include>
--%>
<aside class="w-64 bg-gradient-to-b from-white to-[#f4fbf7] border-r border-[#e2ece7] flex flex-col h-screen sticky top-0 overflow-y-auto z-40 shrink-0 shadow-sm">
    <!-- Logo Brand -->
    <div class="p-5 border-b border-[#e2ece7] flex items-center gap-3">
        <img src="${pageContext.request.contextPath}/assets/images/logo_light.png" alt="MetaFruit"
             class="brand-mark"
             onerror="this.src='https://images.unsplash.com/photo-1610832958506-ee5633619144?w=100'">
        <div>
            <div class="text-base font-extrabold text-[#4d661c] leading-tight">Meta<span class="text-[#84cc16]">Fruit</span></div>
            <div class="text-[10px] text-gray-500 font-bold uppercase tracking-wider">Kênh Người Bán</div>
        </div>
    </div>

    <!-- Navigation Links -->
    <nav class="flex-1 py-6 px-4 space-y-1.5">
        <a href="${pageContext.request.contextPath}/shop/dashboard"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'dashboard' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-chart-line w-5 text-center ${param.activePage == 'dashboard' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Tổng quan</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/products"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'products' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-box w-5 text-center ${param.activePage == 'products' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Sản phẩm</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/orders"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'orders' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-clipboard-list w-5 text-center ${param.activePage == 'orders' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Đơn hàng</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/promotions"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'promotions' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-tags w-5 text-center ${param.activePage == 'promotions' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Voucher shop</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/inventory"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'inventory' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-warehouse w-5 text-center ${param.activePage == 'inventory' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Tồn kho</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/settlement"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'settlement' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-wallet w-5 text-center ${param.activePage == 'settlement' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Tài chính</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/reports"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'reports' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-chart-column w-5 text-center ${param.activePage == 'reports' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Báo cáo</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/profile"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'profile' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-store w-5 text-center ${param.activePage == 'profile' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Hồ sơ Shop</span>
        </a>
        <a href="${pageContext.request.contextPath}/shop/settings"
           class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all duration-200
                  ${param.activePage == 'settings' ? 'font-bold bg-[#edf7f2] text-[#4d661c] shadow-inner border-l-4 border-[#4d661c]' : 'font-medium text-[#475569] hover:bg-[#edf7f2] hover:text-[#4d661c]'}">
            <i class="fa-solid fa-sliders w-5 text-center ${param.activePage == 'settings' ? 'text-[#4d661c]' : 'text-gray-400'}"></i>
            <span>Cài đặt</span>
        </a>
    </nav>

    <!-- User Info & Logout -->
    <div class="p-4 border-t border-[#e2ece7] space-y-3">
        <div class="flex items-center gap-3 px-2">
            <div class="w-8 h-8 rounded-full bg-[#edf7f2] text-[#4d661c] flex items-center justify-center text-sm font-bold shrink-0">
                <i class="fa-solid fa-user"></i>
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-xs font-bold text-[#0f172a] truncate"><c:out value="${sessionScope.currentUser.fullName}"/></p>
                <p class="text-[10px] text-gray-400 truncate">Người bán</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/"
           class="flex items-center justify-center gap-2 w-full py-2.5 bg-[#f0f7e6] text-[#4d661c] hover:bg-[#4d661c] hover:text-white rounded-xl text-xs font-bold transition-all duration-200 mb-2">
            <i class="fa-solid fa-house"></i>
            <span>Về trang chủ</span>
        </a>
        <a href="${pageContext.request.contextPath}/auth/logout"
           class="flex items-center justify-center gap-2 w-full py-2.5 bg-red-50 text-red-600 hover:bg-red-600 hover:text-white rounded-xl text-xs font-bold transition-all duration-200">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span>Đăng xuất</span>
        </a>
    </div>
</aside>

<script>
document.addEventListener("DOMContentLoaded", function() {
    if (window.innerWidth <= 1024) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.id = 'sidebarMobileToggle';
        btn.className = 'sidebar-mobile-toggle';
        btn.innerHTML = '<i class="fa-solid fa-angles-right"></i>';
        
        const sidebar = document.querySelector('aside.w-64');
        if (sidebar) {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                sidebar.classList.toggle('active');
                const icon = btn.querySelector('i');
                if (sidebar.classList.contains('active')) {
                    icon.className = 'fa-solid fa-angles-left';
                    btn.style.left = '270px';
                } else {
                    icon.className = 'fa-solid fa-angles-right';
                    btn.style.left = '20px';
                }
            });
            
            document.addEventListener('click', function(ev) {
                if (sidebar.classList.contains('active') && !sidebar.contains(ev.target) && ev.target !== btn) {
                    sidebar.classList.remove('active');
                    const icon = btn.querySelector('i');
                    if (icon) icon.className = 'fa-solid fa-angles-right';
                    btn.style.left = '20px';
                }
            });
            
            document.body.appendChild(btn);
        }
    }
});
</script>
