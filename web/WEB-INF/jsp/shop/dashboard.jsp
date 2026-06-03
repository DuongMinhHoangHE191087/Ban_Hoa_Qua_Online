<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kênh Người Bán | Tổng Quan Cửa Hàng</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        body { background-color: var(--color-background); font-family: var(--font-family); margin: 0; }
        .shop-layout { display: flex; min-height: 100vh; }
        
        /* Sidebar */
        .sidebar { width: 250px; background-color: white; border-right: 1px solid var(--color-border); padding: 1.5rem 0; flex-shrink: 0; }
        .sidebar-brand { font-size: 1.25rem; font-weight: 700; color: var(--color-primary); padding: 0 1.5rem 1.5rem; border-bottom: 1px solid var(--color-border); margin-bottom: 1rem; }
        .nav-item { display: flex; align-items: center; padding: 0.75rem 1.5rem; color: var(--color-text-secondary); text-decoration: none; transition: all 0.2s; }
        .nav-item:hover, .nav-item.active { background-color: var(--color-primary-light); color: var(--color-primary); }
        .nav-item i { width: 20px; margin-right: 10px; }
        
        /* Main Content */
        .main-content { flex: 1; padding: 2rem; overflow-y: auto; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; color: var(--color-text-primary); }
        
        /* Welcome Banner */
        .welcome-card { background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)); color: white; border-radius: var(--radius-lg); padding: 2rem; margin-bottom: 2rem; position: relative; overflow: hidden; }
        .welcome-card::after { content: ''; position: absolute; right: -50px; bottom: -50px; width: 200px; height: 200px; border-radius: var(--radius-full); background: rgba(255, 255, 255, 0.05); }
        .welcome-card__text h2 { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.5rem; margin-top: 0; }
        .welcome-card__text p { font-size: 0.95rem; opacity: 0.85; margin: 0; }
        
        /* Stats Grid */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem; }
        .stat-card { background: white; border-radius: var(--radius-lg); padding: 1.5rem; border: 1px solid var(--color-border); box-shadow: var(--shadow-sm); display: flex; align-items: center; justify-content: space-between; transition: all 0.25s ease; }
        .stat-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }
        .stat-card__info { display: flex; flex-direction: column; }
        .stat-card__title { font-size: 0.85rem; font-weight: 600; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; }
        .stat-card__value { font-size: 1.6rem; font-weight: 800; color: var(--color-text-primary); }
        .stat-card__icon { width: 48px; height: 48px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
        .stat-card__icon--revenue { background-color: #E8F5E9; color: var(--color-success); }
        .stat-card__icon--orders { background-color: #E3F2FD; color: var(--color-info); }
        .stat-card__icon--stock { background-color: #FFF3E0; color: var(--color-warning); }

        /* Quick Actions Card */
        .actions-card { background: white; border-radius: var(--radius-lg); border: 1px solid var(--color-border); box-shadow: var(--shadow-sm); padding: 2rem; }
        .actions-card__title { font-size: 1.15rem; font-weight: 700; color: var(--color-primary-dark); margin-bottom: 1.25rem; margin-top: 0; }
        .actions-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
        .action-btn { display: flex; align-items: center; gap: 1rem; padding: 1.25rem; border-radius: var(--radius-md); border: 1.5px solid var(--color-border); background: white; color: var(--color-text-primary); font-weight: 600; text-decoration: none; transition: all 0.2s; }
        .action-btn:hover { border-color: var(--color-primary); background-color: rgba(77, 102, 28, 0.02); text-decoration: none; color: var(--color-primary); }
        .action-btn i { font-size: 1.35rem; color: var(--color-primary); }
    </style>
</head>
<body>
    <div class="shop-layout">
        <!-- Sidebar Navigation -->
        <aside class="sidebar">
            <div class="sidebar-brand">Kênh Người Bán</div>
            <a href="${pageContext.request.contextPath}/shop/dashboard" class="nav-item active"><i class="fa-solid fa-chart-line"></i> Tổng quan</a>
            <a href="${pageContext.request.contextPath}/shop/products" class="nav-item"><i class="fa-solid fa-box"></i> Sản phẩm</a>
            <a href="${pageContext.request.contextPath}/shop/orders" class="nav-item"><i class="fa-solid fa-clipboard-list"></i> Đơn hàng</a>
            <a href="${pageContext.request.contextPath}/shop/promotions" class="nav-item"><i class="fa-solid fa-tags"></i> Khuyến mãi</a>
            <a href="${pageContext.request.contextPath}/shop/inventory" class="nav-item"><i class="fa-solid fa-warehouse"></i> Tồn kho</a>
            <a href="${pageContext.request.contextPath}/shop/settlement" class="nav-item"><i class="fa-solid fa-wallet"></i> Tài chính</a>
            <a href="${pageContext.request.contextPath}/shop/profile" class="nav-item"><i class="fa-solid fa-store"></i> Hồ sơ Shop</a>
            <a href="${pageContext.request.contextPath}/auth/logout" class="nav-item" style="margin-top: auto; color: var(--color-danger);"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <!-- Header Section -->
            <div class="page-header">
                <h1 class="page-title">Tổng quan vận hành</h1>
            </div>

            <!-- Welcome Banner -->
            <div class="welcome-card">
                <div class="welcome-card__text">
                    <h2>Xin chào, <c:out value="${sessionScope.currentUser.fullName}"/>!</h2>
                    <p>Chào mừng bạn quay trở lại Kênh Người Bán. Dưới đây là các chỉ số vận hành cơ bản hôm nay của cửa hàng bạn.</p>
                </div>
            </div>

            <!-- Stats Grid Dashboard -->
            <div class="stats-grid">
                <!-- Revenue Card -->
                <div class="stat-card">
                    <div class="stat-card__info">
                        <span class="stat-card__title">Tổng doanh thu</span>
                        <span class="stat-card__value">
                            <ft:currency value="${revenue}" />
                        </span>
                    </div>
                    <div class="stat-card__icon stat-card__icon--revenue">
                        <i class="fa-solid fa-money-bill-wave"></i>
                    </div>
                </div>

                <!-- Orders Card -->
                <div class="stat-card">
                    <div class="stat-card__info">
                        <span class="stat-card__title">Tổng đơn hàng</span>
                        <span class="stat-card__value">${orderCount}</span>
                    </div>
                    <div class="stat-card__icon stat-card__icon--orders">
                        <i class="fa-solid fa-clipboard-list"></i>
                    </div>
                </div>

                <!-- Low Stock Alert Card -->
                <div class="stat-card">
                    <div class="stat-card__info">
                        <span class="stat-card__title">Sắp hết hàng</span>
                        <span class="stat-card__value">${lowStock}</span>
                    </div>
                    <div class="stat-card__icon stat-card__icon--stock">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                    </div>
                </div>
            </div>

            <!-- Quick Actions Section -->
            <div class="actions-card">
                <h2 class="actions-card__title"><i class="fa-solid fa-bolt me-2"></i>Thao tác nhanh</h2>
                <div class="actions-grid">
                    <a href="${pageContext.request.contextPath}/shop/products" class="action-btn">
                        <i class="fa-solid fa-box"></i>
                        <span>Quản lý sản phẩm</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/shop/inventory" class="action-btn">
                        <i class="fa-solid fa-warehouse"></i>
                        <span>Quản lý nhập kho</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/shop/orders" class="action-btn">
                        <i class="fa-solid fa-file-invoice"></i>
                        <span>Quản lý đơn hàng</span>
                    </a>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
