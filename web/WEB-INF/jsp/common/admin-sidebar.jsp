<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="admin-sidebar">
    <div class="admin-sidebar__logo">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="logo-link">
            <div class="logo-icon"><i class="fa-solid fa-leaf"></i></div>
            <div class="logo-text">Meta<span class="text-highlight">Fruit</span></div>
        </a>
    </div>
    
    <div class="admin-sidebar__user">
        <div class="user-avatar"><i class="fa-solid fa-user-shield"></i></div>
        <div class="user-info">
            <div class="user-greeting">Xin chào,</div>
            <div class="user-name"><c:out value="${sessionScope.currentUser.fullName}" default="Admin"/></div>
        </div>
    </div>

    <nav class="admin-sidebar__nav">
        <ul class="nav-list">
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link ${param.activeMenu == 'dashboard' ? 'active' : ''}">
                    <i class="fa-solid fa-chart-pie"></i> <span>Tổng quan</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/users" class="nav-link ${param.activeMenu == 'users' ? 'active' : ''}">
                    <i class="fa-solid fa-users"></i> <span>Quản lý người dùng</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/shops" class="nav-link ${param.activeMenu == 'shops' ? 'active' : ''}">
                    <i class="fa-solid fa-store"></i> <span>Phê duyệt Cửa hàng</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/categories" class="nav-link ${param.activeMenu == 'categories' ? 'active' : ''}">
                    <i class="fa-solid fa-tags"></i> <span>Danh mục Sản phẩm</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/orders" class="nav-link ${param.activeMenu == 'orders' ? 'active' : ''}">
                    <i class="fa-solid fa-box-open"></i> <span>Giám sát Đơn hàng</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/settlements" class="nav-link ${param.activeMenu == 'settlements' ? 'active' : ''}">
                    <i class="fa-solid fa-file-invoice-dollar"></i> <span>Đối soát Thanh toán</span>
                </a>
            </li>
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/admin/reviews" class="nav-link ${param.activeMenu == 'reviews' ? 'active' : ''}">
                    <i class="fa-solid fa-star"></i> <span>Kiểm duyệt Đánh giá</span>
                </a>
            </li>
        </ul>
    </nav>
    
    <div class="admin-sidebar__footer">
        <a href="${pageContext.request.contextPath}/" class="btn btn-secondary btn-sm btn-block">
            <i class="fa-solid fa-home"></i> Về trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger btn-sm btn-block mt-4">
            <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
        </a>
    </div>
</aside>

<style>
/* Admin Sidebar Styles - Reusing variables from main.css */
.admin-layout {
    display: flex;
    min-height: 100vh;
    background-color: var(--color-bg);
}

.admin-sidebar {
    width: 260px;
    background: var(--color-surface);
    border-right: 1px solid var(--color-border);
    display: flex;
    flex-direction: column;
    position: sticky;
    top: 0;
    height: 100vh;
    box-shadow: var(--shadow-sm);
    z-index: 100;
}

.admin-sidebar__logo {
    padding: var(--space-5) var(--space-4);
    border-bottom: 1px solid var(--color-border);
}

.admin-sidebar__logo .logo-link {
    display: flex; align-items: center; gap: var(--space-2);
    font-size: var(--font-size-xl); font-weight: 800;
    color: var(--color-primary);
    text-decoration: none;
}

.admin-sidebar__logo .logo-icon {
    background: var(--color-primary-light);
    color: var(--color-primary);
    width: 32px; height: 32px;
    display: flex; align-items: center; justify-content: center;
    border-radius: var(--radius-md);
    font-size: 1.1rem;
}
.admin-sidebar__logo .logo-text { font-family: 'Lexend', sans-serif; }
.admin-sidebar__logo .text-highlight { color: #84cc16; }

.admin-sidebar__user {
    padding: var(--space-4);
    display: flex;
    align-items: center;
    gap: var(--space-3);
    border-bottom: 1px solid var(--color-border);
    background: rgba(217, 249, 157, 0.1); /* light primary tint */
}

.admin-sidebar__user .user-avatar {
    width: 40px; height: 40px;
    border-radius: var(--radius-full);
    background: var(--color-primary);
    color: white;
    display: flex; align-items: center; justify-content: center;
    font-size: 1.2rem;
}
.admin-sidebar__user .user-greeting { font-size: var(--font-size-xs); color: var(--color-text-secondary); }
.admin-sidebar__user .user-name { font-weight: 600; font-size: var(--font-size-sm); color: var(--color-primary-dark); }

.admin-sidebar__nav {
    flex: 1;
    padding: var(--space-4) 0;
    overflow-y: auto;
}

.nav-list { list-style: none; padding: 0; margin: 0; }
.nav-item { margin-bottom: 2px; }
.nav-link {
    display: flex; align-items: center; gap: var(--space-3);
    padding: var(--space-3) var(--space-4);
    color: var(--color-text-secondary);
    font-weight: 500;
    text-decoration: none;
    transition: all 0.2s ease;
    border-left: 3px solid transparent;
}

.nav-link i { width: 20px; text-align: center; font-size: 1.1rem; opacity: 0.8; }
.nav-link:hover {
    background: rgba(77, 102, 28, 0.05);
    color: var(--color-primary);
    text-decoration: none;
}
.nav-link.active {
    background: rgba(77, 102, 28, 0.1);
    color: var(--color-primary-dark);
    border-left-color: var(--color-primary);
    font-weight: 600;
}
.nav-link.active i { color: var(--color-primary); opacity: 1; }

.admin-sidebar__footer {
    padding: var(--space-4);
    border-top: 1px solid var(--color-border);
}

.admin-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
}

.admin-header {
    height: 60px;
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
    display: flex;
    align-items: center;
    padding: 0 var(--space-6);
    box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.admin-header h1 {
    font-size: var(--font-size-lg);
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0;
}

.admin-content {
    padding: var(--space-6);
    flex: 1;
}

/* Card for Admin panels */
.admin-panel {
    background: var(--color-surface);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    padding: var(--space-5);
    border: 1px solid var(--color-border);
}
</style>
