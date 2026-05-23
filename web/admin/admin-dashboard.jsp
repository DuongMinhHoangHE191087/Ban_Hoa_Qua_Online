<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- Khai báo thư viện JSTL Core để dùng vòng lặp, câu lệnh điều kiện (if/else) --%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- Khai báo thư viện JSTL Format để format tiền tệ, ngày tháng --%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - FruitMkt</title>
    
    <!-- Import CSS gốc của dự án để tái sử dụng Design System (màu sắc, font chữ) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <!-- Import Font chữ Plus Jakarta Sans và Lexend từ Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Import FontAwesome để hiển thị các icon -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Import thư viện Chart.js từ CDN để vẽ biểu đồ doanh thu -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <!-- Các style CSS tùy chỉnh riêng cho Layout của trang Admin -->
    <style>
        /* Bố cục chính chia làm 2 cột: Sidebar trái, Main Content phải */
        .admin-layout { display: flex; min-height: 100vh; }
        
        /* Cột Sidebar bên trái: cố định chiều rộng, màu nền sáng */
        .admin-sidebar { width: 260px; background: var(--color-surface); border-right: 1px solid var(--color-border); padding: var(--space-4); }
        
        /* Cột nội dung bên phải: chiếm toàn bộ không gian còn lại */
        .admin-main { flex: 1; padding: var(--space-6); background: var(--color-bg); }
        
        /* Lưới hiển thị 4 thẻ thống kê trên cùng một hàng ngang */
        .stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-4); margin-bottom: var(--space-6); }
        
        /* Thiết kế thẻ Card cho từng chỉ số thống kê (Doanh thu, đơn hàng...) */
        .stat-card { background: var(--color-surface); padding: var(--space-4); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border-left: 4px solid var(--color-primary); }
        .stat-card h3 { color: var(--color-text-secondary); font-size: var(--font-size-sm); margin-bottom: var(--space-2); }
        .stat-card .value { font-size: var(--font-size-2xl); font-weight: 700; color: var(--color-text-primary); }
        
        /* The Card màu cam/đỏ dùng để cảnh báo (ví dụ: Shop chờ duyệt) */
        .stat-card.warning { border-left-color: var(--color-warning); }

        /* Lưới chia đôi màn hình: Biểu đồ chiếm 2 phần, Danh sách top sản phẩm chiếm 1 phần */
        .dashboard-row { display: grid; grid-template-columns: 2fr 1fr; gap: var(--space-6); }
        
        /* Thiết kế Panel trắng chứa biểu đồ và danh sách */
        .dashboard-panel { background: var(--color-surface); padding: var(--space-5); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
        .dashboard-panel h2 { font-size: var(--font-size-lg); margin-bottom: var(--space-4); font-family: 'Lexend', sans-serif; }
        
        /* Style cho bảng Top 5 sản phẩm */
        .top-products-table { width: 100%; border-collapse: collapse; }
        .top-products-table th, .top-products-table td { padding: var(--space-2); text-align: left; border-bottom: 1px solid var(--color-border); }
        .top-products-table th { color: var(--color-text-secondary); font-weight: 600; }
    </style>
</head>
<body>

<div class="admin-layout">
    <!-- Khu vực Sidebar Navigation (Menu trái) -->
    <aside class="admin-sidebar">
        <!-- Logo và tên hệ thống -->
        <div class="navbar__logo mb-4">
            <div class="logo-icon"><i class="fa-solid fa-leaf"></i></div>
            <div class="logo-text">Fruit<span class="text-highlight">Mkt</span> Admin</div>
        </div>
        
        <!-- Danh sách các link chuyển trang trong Admin -->
        <ul class="navbar__menu" style="flex-direction: column; align-items: flex-start; padding: 0;">
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/dashboard" class="menu-link" style="width: 100%; background: rgba(77,102,28,0.1); color: var(--color-primary);"><i class="fa-solid fa-chart-pie"></i> Dashboard</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/products" class="menu-link" style="width: 100%;"><i class="fa-solid fa-box"></i> Sản phẩm vi phạm</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/shops" class="menu-link" style="width: 100%;"><i class="fa-solid fa-store"></i> Duyệt gian hàng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/users" class="menu-link" style="width: 100%;"><i class="fa-solid fa-users"></i> Người dùng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/settlements" class="menu-link" style="width: 100%;"><i class="fa-solid fa-file-invoice"></i> Đối soát</a></li>
        </ul>
    </aside>

    <!-- Khu vực Main Content (Nội dung chính hiển thị bên phải) -->
    <main class="admin-main">
        <h1 style="margin-bottom: var(--space-6);">Tổng quan hệ thống</h1>

        <!-- Dãy 4 thẻ hiển thị chỉ số thống kê (Lấy từ Controller) -->
        <div class="stat-grid">
            <!-- Thẻ Tổng doanh thu: Dùng JSTL fmt để định dạng số có dấu phẩy ngăn cách hàng nghìn -->
            <div class="stat-card">
                <h3>Tổng Doanh Thu</h3>
                <div class="value"><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> đ</div>
            </div>
            <!-- Thẻ Đơn hàng mới -->
            <div class="stat-card">
                <h3>Đơn Hàng Mới (30 ngày)</h3>
                <div class="value">${newOrdersCount}</div>
            </div>
            <!-- Thẻ Shop chờ duyệt: Style màu cam cảnh báo để Admin chú ý -->
            <div class="stat-card warning">
                <h3>Shop Chờ Duyệt</h3>
                <div class="value">${pendingShopsCount}</div>
            </div>
            <!-- Thẻ Người dùng mới -->
            <div class="stat-card">
                <h3>User Mới (30 ngày)</h3>
                <div class="value">${newUsersCount}</div>
            </div>
        </div>

        <div class="dashboard-row">
            <!-- Khu vực vẽ biểu đồ doanh thu (Sử dụng thẻ canvas của HTML5 cho Chart.js) -->
            <div class="dashboard-panel">
                <h2>Biểu đồ doanh thu 4 tuần qua</h2>
                <canvas id="revenueChart" height="120"></canvas>
            </div>
            
            <!-- Khu vực danh sách Top 5 sản phẩm bán chạy nhất -->
            <div class="dashboard-panel">
                <h2>Top 5 Sản phẩm bán chạy</h2>
                
                <!-- Hiển thị câu thông báo nếu List topProducts rỗng -->
                <c:if test="${empty topProducts}">
                    <p style="color: var(--color-text-secondary);">Chưa có dữ liệu sản phẩm.</p>
                </c:if>
                
                <!-- Bảng hiển thị danh sách sản phẩm nếu có dữ liệu -->
                <c:if test="${not empty topProducts}">
                    <table class="top-products-table">
                        <thead>
                            <tr>
                                <th>Tên sản phẩm</th>
                                <th>Đã bán</th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- Vòng lặp forEach duyệt qua từng sản phẩm lấy từ Database -->
                            <c:forEach var="product" items="${topProducts}">
                                <tr>
                                    <td>${product.name}</td>
                                    <td>${product.sold_quantity}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
                
                <!-- Nút thao tác nhanh: Chỉ hiện ra nếu có Shop đang chờ duyệt -->
                <c:if test="${pendingShopsCount > 0}">
                    <div style="margin-top: var(--space-4);">
                        <a href="${pageContext.request.contextPath}/admin/shops" class="btn btn-primary btn-block">
                            Duyệt ${pendingShopsCount} Shop ngay
                        </a>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<!-- Khối Script: Đổ dữ liệu mảng từ Java/JSP sang Javascript để vẽ biểu đồ -->
<script>
    // Nhận mảng dữ liệu doanh thu từ Controller.
    // Dùng JSTL forEach lặp qua List Java, in từng giá trị ra file JS, cách nhau bằng dấu phẩy
    const revenueData = [
        <c:forEach var="rev" items="${revenueLast4Weeks}" varStatus="loop">
            ${rev}${!loop.last ? ',' : ''} 
        </c:forEach>
    ];

    // Khởi tạo đối tượng Chart.js trên thẻ canvas
    const ctx = document.getElementById('revenueChart').getContext('2d');
    new Chart(ctx, {
        type: 'line', // Biểu đồ dạng đường
        data: {
            labels: ['Tuần 1 (Cũ)', 'Tuần 2', 'Tuần 3', 'Tuần 4 (Gần nhất)'], // Nhãn dán trục X
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: revenueData, // Nạp mảng số liệu vừa lấy ở trên vào trục Y
                borderColor: '#4D661C', // Màu đường biểu đồ (Xanh lá đậm)
                backgroundColor: 'rgba(77, 102, 28, 0.1)', // Màu nền dưới đường biểu đồ (Mờ)
                borderWidth: 2, 
                fill: true, // Lấp đầy màu nền
                tension: 0.4 // Độ uốn cong của đường (0 = thẳng, 0.4 = cong mềm mại)
            }]
        },
        // Tắt hiển thị chú giải (legend) vì chỉ có 1 đường
        options: { responsive: true, plugins: { legend: { display: false } } } 
    });
</script>
</body>
</html>
