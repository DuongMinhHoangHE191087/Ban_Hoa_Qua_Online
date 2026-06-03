<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đơn hàng | Kênh Người Bán</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        body { background-color: var(--color-background); font-family: var(--font-primary); margin: 0; }
        .shop-layout { display: flex; min-height: 100vh; }
        
        /* Sidebar */
        .sidebar { width: 250px; background-color: white; border-right: 1px solid var(--color-gray-200); padding: 1.5rem 0; flex-shrink: 0; }
        .sidebar-brand { font-size: 1.25rem; font-weight: 700; color: var(--color-primary); padding: 0 1.5rem 1.5rem; border-bottom: 1px solid var(--color-gray-200); margin-bottom: 1rem; }
        .nav-item { display: flex; align-items: center; padding: 0.75rem 1.5rem; color: var(--color-gray-600); text-decoration: none; transition: all 0.2s; }
        .nav-item:hover, .nav-item.active { background-color: var(--color-primary-light); color: var(--color-primary); }
        .nav-item i { width: 20px; margin-right: 10px; }
        
        /* Main Content */
        .main-content { flex: 1; padding: 2rem; overflow-y: auto; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; color: var(--color-gray-900); }
        
        /* Filters */
        .filter-bar { display: flex; gap: 10px; margin-bottom: 1.5rem; }
        .filter-btn { padding: 8px 16px; border: 1px solid var(--color-gray-300); border-radius: 20px; background: white; color: var(--color-gray-700); text-decoration: none; font-size: 0.875rem; transition: 0.2s; }
        .filter-btn:hover, .filter-btn.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }
        
        /* Table */
        .card { background: white; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; }
        .table-responsive { width: 100%; overflow-x: auto; }
        .table { width: 100%; border-collapse: collapse; text-align: left; }
        .table th, .table td { padding: 1rem 1.5rem; border-bottom: 1px solid var(--color-gray-200); }
        .table th { background-color: var(--color-gray-50); font-weight: 600; color: var(--color-gray-700); font-size: 0.875rem; text-transform: uppercase; letter-spacing: 0.05em; }
        .table tr:last-child td { border-bottom: none; }
        .table tr:hover { background-color: var(--color-gray-50); }
        
        /* Badges */
        .badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
        .badge-pending { background-color: #fef3c7; color: #d97706; }
        .badge-confirmed { background-color: #dbeafe; color: #2563eb; }
        .badge-preparing { background-color: #e0e7ff; color: #4f46e5; }
        .badge-approved { background-color: #e0e7ff; color: #4f46e5; }
        .badge-dispatched { background-color: #fce7f3; color: #db2777; }
        .badge-delivered { background-color: #dcfce3; color: #16a34a; }
        .badge-cancelled { background-color: #fee2e2; color: #dc2626; }
        
        /* Modal */
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .modal-content { background-color: white; padding: 2rem; border-radius: var(--radius-lg); width: 100%; max-width: 500px; box-shadow: var(--shadow-lg); }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
        .modal-title { font-size: 1.25rem; font-weight: 700; margin: 0; }
        .close-btn { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: var(--color-gray-500); }
        
        /* Form Controls */
        .form-group { margin-bottom: 1.5rem; }
        .form-label { display: block; margin-bottom: 0.5rem; font-weight: 500; color: var(--color-gray-700); }
        .form-control { width: 100%; padding: 0.75rem 1rem; border: 1px solid var(--color-gray-300); border-radius: var(--radius-md); font-family: inherit; font-size: 1rem; box-sizing: border-box; }
        .form-control:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1); }
    </style>
</head>
<body>
    <div class="shop-layout">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="sidebar-brand">Kênh Người Bán</div>
            <a href="${pageContext.request.contextPath}/shop/dashboard" class="nav-item"><i class="fa-solid fa-chart-line"></i> Tổng quan</a>
            <a href="${pageContext.request.contextPath}/shop/products" class="nav-item"><i class="fa-solid fa-box"></i> Sản phẩm</a>
            <a href="${pageContext.request.contextPath}/shop/orders" class="nav-item active"><i class="fa-solid fa-clipboard-list"></i> Đơn hàng</a>
            <a href="${pageContext.request.contextPath}/shop/promotions" class="nav-item"><i class="fa-solid fa-tags"></i> Khuyến mãi</a>
            <a href="${pageContext.request.contextPath}/shop/inventory" class="nav-item"><i class="fa-solid fa-warehouse"></i> Tồn kho</a>
            <a href="${pageContext.request.contextPath}/shop/settlement" class="nav-item"><i class="fa-solid fa-wallet"></i> Tài chính</a>
            <a href="${pageContext.request.contextPath}/shop/profile" class="nav-item"><i class="fa-solid fa-store"></i> Hồ sơ Shop</a>
            <a href="${pageContext.request.contextPath}/auth/logout" class="nav-item" style="margin-top: auto; color: var(--color-danger);"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashMsg}">
                <div class="alert alert-${sessionScope.flashType} alert-dismissible" role="alert">
                    ${sessionScope.flashMsg}
                    <button type="button" class="btn-close" aria-label="Close" onclick="this.parentElement.style.display='none';"></button>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <div class="page-header">
                <h1 class="page-title">Quản lý Đơn hàng</h1>
            </div>

            <!-- Filters -->
            <div class="filter-bar">
                <a href="?status=" class="filter-btn ${empty status ? 'active' : ''}">Tất cả</a>
                <a href="?status=PENDING_PAYMENT" class="filter-btn ${status == 'PENDING_PAYMENT' ? 'active' : ''}">Chờ thanh toán</a>
                <a href="?status=CONFIRMED" class="filter-btn ${status == 'CONFIRMED' ? 'active' : ''}">Chờ duyệt</a>
                <a href="?status=APPROVED" class="filter-btn ${status == 'APPROVED' ? 'active' : ''}">Đã duyệt (Chờ đóng gói)</a>
                <a href="?status=DISPATCHED" class="filter-btn ${status == 'DISPATCHED' ? 'active' : ''}">Đang giao</a>
                <a href="?status=DELIVERED" class="filter-btn ${status == 'DELIVERED' ? 'active' : ''}">Hoàn thành</a>
                <a href="?status=CANCELLED" class="filter-btn ${status == 'CANCELLED' ? 'active' : ''}">Đã hủy</a>
            </div>

            <!-- Orders Table -->
            <div class="card">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Mã ĐH</th>
                                <th>Khách hàng</th>
                                <th>Ngày tạo</th>
                                <th>Tổng tiền</th>
                                <th>Thanh toán</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="order" items="${orders}">
                                <tr>
                                    <td>#${order.orderId}</td>
                                    <td>Khách ID: ${order.customerId}</td>
                                    <td>${order.createdAt}</td>
                                    <td class="text-danger fw-bold"><ft:currency value="${order.finalAmount}" /></td>
                                    <td>${order.paymentMethod}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.status == 'PENDING_PAYMENT'}"><span class="badge badge-pending">Chờ TT</span></c:when>
                                            <c:when test="${order.status == 'CONFIRMED'}"><span class="badge badge-confirmed">Chờ Duyệt</span></c:when>
                                            <c:when test="${order.status == 'PREPARING'}"><span class="badge badge-preparing">Chuẩn bị</span></c:when>
                                            <c:when test="${order.status == 'APPROVED'}"><span class="badge badge-approved">Đã Duyệt</span></c:when>
                                            <c:when test="${order.status == 'DISPATCHED'}"><span class="badge badge-dispatched">Đang giao</span></c:when>
                                            <c:when test="${order.status == 'DELIVERED'}"><span class="badge badge-delivered">Đã giao</span></c:when>
                                            <c:when test="${order.status == 'CANCELLED'}"><span class="badge badge-cancelled">Đã hủy</span></c:when>
                                            <c:otherwise><span class="badge">${order.status}</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${order.status == 'CONFIRMED' || order.status == 'PENDING_PAYMENT'}">
                                            <form action="${pageContext.request.contextPath}/shop/orders" method="POST" style="display:inline;">
                                                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                <input type="hidden" name="action" value="approve">
                                                <input type="hidden" name="orderId" value="${order.orderId}">
                                                <button type="submit" class="btn btn-primary btn-sm">Duyệt</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${order.status == 'APPROVED' || order.status == 'PREPARING'}">
                                            <button type="button" class="btn btn-success btn-sm" onclick="openDispatchModal('${order.orderId}')">Giao hàng</button>
                                        </c:if>
                                        <c:if test="${order.status != 'DELIVERED' && order.status != 'CANCELLED' && order.status != 'DISPATCHED'}">
                                            <button type="button" class="btn btn-danger btn-sm" onclick="openRejectModal('${order.orderId}')">Hủy</button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty orders}">
                                <tr>
                                    <td colspan="7" class="text-center py-4" style="text-align: center; color: var(--color-gray-500);">Không có đơn hàng nào!</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal Hủy Đơn -->
    <div id="rejectModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Lý do hủy đơn hàng</h2>
                <button class="close-btn" onclick="closeModal('rejectModal')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/shop/orders" method="POST">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="action" value="reject">
                <input type="hidden" name="orderId" id="rejectOrderId">
                <div class="form-group">
                    <label class="form-label">Vui lòng cho biết lý do hủy đơn này:</label>
                    <textarea name="reason" class="form-control" rows="4" required placeholder="Hết hàng, khách đổi ý..."></textarea>
                </div>
                <div style="display: flex; gap: 10px; justify-content: flex-end;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('rejectModal')">Đóng</button>
                    <button type="submit" class="btn btn-danger">Xác nhận Hủy</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal Giao Hàng -->
    <div id="dispatchModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">Giao cho đơn vị vận chuyển</h2>
                <button class="close-btn" onclick="closeModal('dispatchModal')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/shop/orders" method="POST">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="action" value="dispatch">
                <input type="hidden" name="orderId" id="dispatchOrderId">
                <div class="form-group">
                    <label class="form-label">Dự kiến thời gian giao đến khách (Tùy chọn):</label>
                    <input type="datetime-local" name="estimatedDeliveryTime" class="form-control">
                </div>
                <div style="display: flex; gap: 10px; justify-content: flex-end;">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('dispatchModal')">Đóng</button>
                    <button type="submit" class="btn btn-success">Bàn giao</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openRejectModal(orderId) {
            document.getElementById('rejectOrderId').value = orderId;
            document.getElementById('rejectModal').classList.add('active');
        }
        function openDispatchModal(orderId) {
            document.getElementById('dispatchOrderId').value = orderId;
            document.getElementById('dispatchModal').classList.add('active');
        }
        function closeModal(modalId) {
            document.getElementById(modalId).classList.remove('active');
        }
        
        // Close modal when clicking outside
        window.onclick = function(event) {
            if (event.target.classList.contains('modal')) {
                event.target.classList.remove('active');
            }
        }
    </script>
</body>
</html>
