<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan - Admin MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- FontAwesome -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <!-- Core CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: var(--space-4);
            margin-bottom: var(--space-6);
        }
        
        .stat-card {
            background: var(--color-surface);
            border-radius: var(--radius-lg);
            padding: var(--space-5);
            box-shadow: var(--shadow-sm);
            display: flex;
            align-items: center;
            transition: transform 0.2s, box-shadow 0.2s;
            border-left: 5px solid var(--color-primary);
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow-md);
        }
        
        .stat-card.blue { border-left-color: #3b82f6; }
        .stat-card.green { border-left-color: #10b981; }
        .stat-card.red { border-left-color: #ef4444; }
        .stat-card.orange { border-left-color: #f59e0b; }

        .stat-icon {
            font-size: 2.5rem;
            margin-right: var(--space-4);
        }
        
        .stat-card.blue .stat-icon { color: #3b82f6; }
        .stat-card.green .stat-icon { color: #10b981; }
        .stat-card.red .stat-icon { color: #ef4444; }
        .stat-card.orange .stat-icon { color: #f59e0b; }

        .stat-content h3 {
            margin: 0;
            font-size: var(--font-size-sm);
            color: var(--color-text-light);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .stat-content .stat-value {
            margin: var(--space-1) 0 0 0;
            font-size: 2rem;
            font-weight: 700;
            color: var(--color-text);
        }
        
        .dashboard-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: var(--space-4);
        }
        
        .action-card {
            background: var(--color-surface);
            border-radius: var(--radius-lg);
            padding: var(--space-5);
            box-shadow: var(--shadow-sm);
            text-align: center;
        }
        
        .action-card i {
            font-size: 3rem;
            color: var(--color-primary);
            margin-bottom: var(--space-3);
        }
        
        .action-card h3 {
            margin-bottom: var(--space-2);
        }
        
        .action-card p {
            color: var(--color-text-light);
            margin-bottom: var(--space-4);
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="dashboard"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <h1>Tổng quan hệ thống</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <!-- Thống kê KPI -->
                <div class="dashboard-grid">
                    <div class="stat-card blue">
                        <div class="stat-icon"><i class="fa-solid fa-users"></i></div>
                        <div class="stat-content">
                            <h3>Tổng người dùng</h3>
                            <div class="stat-value">${totalUsers != null ? totalUsers : 0}</div>
                        </div>
                    </div>
                    
                    <div class="stat-card green">
                        <div class="stat-icon"><i class="fa-solid fa-cart-shopping"></i></div>
                        <div class="stat-content">
                            <h3>Tổng đơn hàng</h3>
                            <div class="stat-value">${totalOrders != null ? totalOrders : 0}</div>
                        </div>
                    </div>
                    
                    <div class="stat-card red">
                        <div class="stat-icon"><i class="fa-solid fa-rotate-left"></i></div>
                        <div class="stat-content">
                            <h3>Yêu cầu Đổi trả (Chờ duyệt)</h3>
                            <div class="stat-value">${pendingRefunds != null ? pendingRefunds : 0}</div>
                        </div>
                    </div>
                    
                    <div class="stat-card orange">
                        <div class="stat-icon"><i class="fa-solid fa-file-invoice-dollar"></i></div>
                        <div class="stat-content">
                            <h3>Đối soát cần thanh toán</h3>
                            <div class="stat-value">${unpaidSettlements != null ? unpaidSettlements : 0}</div>
                        </div>
                    </div>
                </div>

                <!-- Liên kết nhanh -->
                <h2 style="margin-bottom: var(--space-4);">Truy cập nhanh</h2>
                <div class="dashboard-actions">
                    <div class="action-card">
                        <i class="fa-solid fa-user-shield"></i>
                        <h3>Quản lý Người dùng</h3>
                        <p>Tìm kiếm, vô hiệu hóa, và quản lý tất cả các tài khoản trên hệ thống.</p>
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Truy cập</a>
                    </div>
                    
                    <div class="action-card">
                        <i class="fa-solid fa-bullhorn"></i>
                        <h3>Gửi thông báo</h3>
                        <p>Gửi thông báo khuyến mãi, bảo trì hệ thống tới các nhóm người dùng.</p>
                        <a href="${pageContext.request.contextPath}/admin/notifications" class="btn btn-secondary">Truy cập</a>
                    </div>
                </div>

            </div>
        </main>
    </div>
</body>
</html>
