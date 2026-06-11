<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- SweetAlert2: offline local asset --%>
<script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
<script>
    /* Global native-alert override ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ SweetAlert2 premium */
    window.alert = function(msg) {
        Swal.fire({ icon:'info', title:'ThÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡o', text:msg,
            confirmButtonText:'Ãƒâ€žÃ‚ÂÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng ÃƒÆ’Ã‚Â½', confirmButtonColor:'#4D661C',
            background:'#fff', borderRadius:'12px' });
    };
</script>

<%-- ============================================================
     ADMIN SIDEBAR ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Light Premium + Tailwind
     SÃƒÂ¡Ã‚Â»Ã‚Â­ dÃƒÂ¡Ã‚Â»Ã‚Â¥ng inline <style> vÃƒÆ’Ã‚Â¬ Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¢y lÃƒÆ’Ã‚Â  fragment Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c include, khÃƒÆ’Ã‚Â´ng phÃƒÂ¡Ã‚ÂºÃ‚Â£i full page
     Tailwind CDN Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c load bÃƒÂ¡Ã‚Â»Ã…Â¸i trang cha (page-level)
============================================================ --%>
<style>
    /* Root layout */
    .admin-layout { display:flex; min-height:100vh; }

    /* Sidebar base */
    #admin-sidebar {
        width:256px; min-width:256px;
        background:linear-gradient(180deg, #ffffff 65%, #f4fbf7 100%);
        display:flex; flex-direction:column;
        position:sticky; top:0; height:100vh;
        overflow-y:auto; overflow-x:hidden;
        border-right:1px solid #e2ece7;
        box-shadow:2px 0 12px rgba(20,83,45,.04);
        z-index:100;
        scrollbar-width:thin;
        scrollbar-color:#cbd5e1 transparent;
    }
    #admin-sidebar::-webkit-scrollbar { width:4px; }
    #admin-sidebar::-webkit-scrollbar-track { background:transparent; }
    #admin-sidebar::-webkit-scrollbar-thumb { background:#cbd5e1; border-radius:4px; }

    /* Logo */
    .sb-logo {
        padding:1.25rem 1.25rem 1rem;
        border-bottom:1px solid #e2ece7;
        flex-shrink:0;
    }
    .sb-logo a {
        display:flex; align-items:center; gap:.625rem;
        text-decoration:none;
        transition: opacity 0.15s ease;
    }
    .sb-logo a:hover {
        opacity: 0.9;
        text-decoration:none;
    }
    .sb-logo-text {
        font-family:'Lexend', 'Segoe UI', -apple-system, sans-serif;
        font-size:1.2rem;
        font-weight:800;
        color:#4d661c;
        letter-spacing:-0.5px;
    }
    .sb-logo-text span { color:#84cc16; }

    /* Nav section label */
    .sb-section-label {
        font-size:.65rem; font-weight:700; letter-spacing:.1em;
        text-transform:uppercase; color:#94a3b8;
        padding:.75rem 1.25rem .3rem;
        flex-shrink:0;
    }

    /* Nav */
    .sb-nav { flex:1; padding:.5rem 0; overflow-y:auto; }
    .sb-nav-list { list-style:none; margin:0; padding:0; }
    .sb-nav-item { margin:.1rem .625rem; }
    .sb-nav-link {
        display:flex; align-items:center; gap:.65rem;
        padding:.6rem .875rem;
        color:#475569;
        font-size:.84rem; font-weight:500;
        text-decoration:none;
        border-radius:.625rem;
        transition:background .15s,color .15s;
        position:relative;
        cursor:pointer;
    }
    .sb-nav-link .sb-icon {
        width:18px; display:flex; align-items:center; justify-content:center;
        flex-shrink:0; font-size:.9rem; color:#64748b;
    }
    .sb-nav-link:hover {
        background:#f4fbf7;
        color:#4d661c;
        text-decoration:none;
    }
    .sb-nav-link.active {
        background:#edf7f2;
        color:#364e03;
        font-weight:700;
        box-shadow:inset 3px 0 0 #4d661c;
    }
    .sb-nav-link.active .sb-icon { color:#4d661c; }

    /* Sidebar footer */
    .sb-footer {
        padding:.875rem 1rem 1rem;
        border-top:1px solid #e2ece7;
        display:flex; flex-direction:column; gap:.5rem;
        flex-shrink:0;
    }
    .sb-footer-btn {
        display:flex; align-items:center; justify-content:center; gap:.5rem;
        padding:.5rem .75rem; border-radius:.625rem;
        font-size:.8rem; font-weight:600; cursor:pointer;
        text-decoration:none; transition:background .15s;
        border:none; width:100%;
    }
    .sb-btn-home {
        background:#f1f5f9;
        color:#475569;
    }
    .sb-btn-home:hover { background:#e2e8f0; color:#0f172a; text-decoration:none; }
    .sb-btn-logout {
        background:#fee2e2;
        color:#ef4444;
        border:1px solid #fecaca;
    }
    .sb-btn-logout:hover { background:#fecaca; color:#991b1b; text-decoration:none; }

    /* Main content area ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â set by sidebar */
    .admin-main { flex:1; display:flex; flex-direction:column; overflow-x:hidden; min-width:0; }
</style>

<aside id="admin-sidebar">
    <%-- Logo --%>
    <div class="sb-logo">
        <a href="${pageContext.request.contextPath}/admin/dashboard">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" style="height: 38px; width: 38px; border-radius: 8px; object-fit: cover; box-shadow: 0 2px 8px rgba(77,102,28,.15);">
            <div class="sb-logo-text">Meta<span>Fruit</span></div>
        </a>
    </div>

    <%-- Navigation --%>
    <nav class="sb-nav">
        <div class="sb-section-label">QuÃƒÂ¡Ã‚ÂºÃ‚Â£n trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ hÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng</div>
        <ul class="sb-nav-list">
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/dashboard"
                   class="sb-nav-link ${param.activeMenu == 'dashboard' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-chart-pie"></i></span>
                    <span>TÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¢ng quan</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/users"
                   class="sb-nav-link ${param.activeMenu == 'users' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-users"></i></span>
                    <span>QuÃƒÂ¡Ã‚ÂºÃ‚Â£n lÃƒÆ’Ã‚Â½ ngÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/chat"
                   class="sb-nav-link ${param.activeMenu == 'chat' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-comments"></i></span>
                    <span>Chat HÃƒÂ¡Ã‚Â»Ã¢â‚¬â€ trÃƒÂ¡Ã‚Â»Ã‚Â£</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/reports"
                   class="sb-nav-link ${param.activeMenu == 'reports' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-chart-column"></i></span>
                    <span>Báo cáo thống kê</span>
                </a>
            </li>
        </ul>

        <div class="sb-section-label">ThÃƒâ€ Ã‚Â°Ãƒâ€ Ã‚Â¡ng mÃƒÂ¡Ã‚ÂºÃ‚Â¡i</div>
        <ul class="sb-nav-list">
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/shops"
                   class="sb-nav-link ${param.activeMenu == 'shops' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-store-slash"></i></span>
                    <span>PhÃƒÆ’Ã‚Âª duyÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t CÃƒÂ¡Ã‚Â»Ã‚Â­a hÃƒÆ’Ã‚Â ng</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/shops/manage"
                   class="sb-nav-link ${param.activeMenu == 'manage-shops' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-store"></i></span>
                    <span>QuÃƒÂ¡Ã‚ÂºÃ‚Â£n lÃƒÆ’Ã‚Â½ CÃƒÂ¡Ã‚Â»Ã‚Â­a hÃƒÆ’Ã‚Â ng</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/products"
                   class="sb-nav-link ${param.activeMenu == 'admin-products' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-clipboard-check"></i></span>
                    <span>PhÃƒÆ’Ã‚Âª duyÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡t SÃƒÂ¡Ã‚ÂºÃ‚Â£n phÃƒÂ¡Ã‚ÂºÃ‚Â©m</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/categories"
                   class="sb-nav-link ${param.activeMenu == 'categories' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-tags"></i></span>
                    <span>Danh mÃƒÂ¡Ã‚Â»Ã‚Â¥c SÃƒÂ¡Ã‚ÂºÃ‚Â£n phÃƒÂ¡Ã‚ÂºÃ‚Â©m</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/orders"
                   class="sb-nav-link ${param.activeMenu == 'orders' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-box-open"></i></span>
                    <span>Giám sát Đơn hàng</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/promotions"
                   class="sb-nav-link ${param.activeMenu == 'promotions' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-tags"></i></span>
                    <span>Voucher sàn</span>
                </a>
            </li>

        </ul>
        <div class="sb-section-label">TÃƒÆ’Ã‚Â i chÃƒÆ’Ã‚Â­nh</div>
        <ul class="sb-nav-list">
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/settlements"
                   class="sb-nav-link ${param.activeMenu == 'settlements' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-file-invoice-dollar"></i></span>
                    <span>Đối soát Thanh toán</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/payments"
                   class="sb-nav-link ${param.activeMenu == 'payments' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-credit-card"></i></span>
                    <span>Giám sát Thanh toán</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/returns"
                   class="sb-nav-link ${param.activeMenu == 'returns' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-rotate-left"></i></span>
                    <span>Yêu cầu Đổi trả</span>
                </a>
            </li>
        </ul>

        <div class="sb-section-label">NÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢i dung</div>
        <ul class="sb-nav-list">
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/reviews"
                   class="sb-nav-link ${param.activeMenu == 'reviews' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-star"></i></span>
                    <span>Kiểm duyệt Đánh giá</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/notifications"
                   class="sb-nav-link ${param.activeMenu == 'notifications' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-bell"></i></span>
                    <span>GÃƒÂ¡Ã‚Â»Ã‚Â­i ThÃƒÆ’Ã‚Â´ng bÃƒÆ’Ã‚Â¡o</span>
                </a>
            </li>
        </ul>
        <div class="sb-section-label">HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng</div>
        <ul class="sb-nav-list">
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/config"
                   class="sb-nav-link ${param.activeMenu == 'config' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-cogs"></i></span>
                    <span>CÃƒÂ¡Ã‚ÂºÃ‚Â¥u hÃƒÆ’Ã‚Â¬nh HÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡ thÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœng</span>
                </a>
            </li>
            <li class="sb-nav-item">
                <a href="${pageContext.request.contextPath}/admin/profile"
                   class="sb-nav-link ${param.activeMenu == 'profile' ? 'active' : ''}">
                    <span class="sb-icon"><i class="fa-solid fa-user-shield"></i></span>
                    <span>HÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ sÃƒâ€ Ã‚Â¡ CÃƒÆ’Ã‚Â¡ nhÃƒÆ’Ã‚Â¢n</span>
                </a>
            </li>
        </ul>
    </nav>

    <%-- Footer --%>
    <div class="sb-footer">
        <div class="flex items-center gap-2 px-2 py-1 mb-1">
            <div class="w-7 h-7 rounded-full bg-[#edf7f2] text-[#4d661c] flex items-center justify-center text-xs font-bold shrink-0">
                <i class="fa-solid fa-user-shield"></i>
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-xs font-bold text-[#0f172a] truncate" style="margin: 0;"><c:out value="${sessionScope.currentUser.fullName}"/></p>
                <p class="text-[10px] text-gray-400 truncate" style="margin: 0;">QuÃƒÂ¡Ã‚ÂºÃ‚Â£n trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ viÃƒÆ’Ã‚Âªn</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/" class="sb-footer-btn sb-btn-home">
            <i class="fa-solid fa-house"></i> VÃƒÂ¡Ã‚Â»Ã‚Â trang chÃƒÂ¡Ã‚Â»Ã‚Â§
        </a>
        <a href="${pageContext.request.contextPath}/auth/logout" class="sb-footer-btn sb-btn-logout">
            <i class="fa-solid fa-right-from-bracket"></i> Ãƒâ€žÃ‚ÂÃƒâ€žÃ†â€™ng xuÃƒÂ¡Ã‚ÂºÃ‚Â¥t
        </a>
    </div>
</aside>
