<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giám sát đơn hàng - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        body { background-color: var(--color-background); font-family: var(--font-primary); margin: 0; }
        
        .status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .status-PENDING_PAYMENT { background: #fef3c7; color: #b45309; border: 1px solid #fde68a; }
        .status-CONFIRMED { background: #dbeafe; color: #1e40af; border: 1px solid #bfdbfe; }
        .status-PREPARING { background: #e0e7ff; color: #3730a3; border: 1px solid #c7d2fe; }
        .status-APPROVED { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .status-DISPATCHED { background: #fce7f3; color: #9d174d; border: 1px solid #fbcfe8; }
        .status-DELIVERED { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; }
        .status-CANCELLED { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

        .filter-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 1.5rem; }
        .filter-btn { padding: 6px 14px; border: 1px solid var(--color-border); border-radius: 20px; background: white; color: var(--color-text-secondary); text-decoration: none; font-size: 0.85rem; font-weight: 500; transition: 0.2s; }
        .filter-btn:hover, .filter-btn.active { background: var(--color-primary); color: white; border-color: var(--color-primary); text-decoration: none; }

        .table-actions { display: flex; gap: 6px; justify-content: center; }
        
        .section-title {
            font-size: var(--font-size-md);
            font-weight: 700;
            color: var(--color-primary-dark);
            margin-top: 0;
            margin-bottom: var(--space-4);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .modal {
            display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.5); align-items: center; justify-content: center;
        }
        .modal.show { display: flex; }
        .modal-content {
            background-color: #fff; padding: 20px; border-radius: 12px; width: 400px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
        }
        .modal-content h3 { margin-top: 0; color: #111827; font-size: 1.15rem; }
        .modal-content textarea {
            width: 100%; border: 1px solid #d1d5db; border-radius: 8px; padding: 10px;
            margin-top: 10px; resize: none; font-family: inherit;
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="orders"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header" style="height: auto; padding: var(--space-4) var(--space-6); display: block; border-bottom: 1px solid var(--color-border); background: white;">
                <div>
                    <h1 style="margin: 0 0 4px; font-size: 1.5rem; font-weight: 700; color: var(--color-text-primary);">Giám Sát Đơn Hàng</h1>
                    <p style="margin: 0; color: var(--color-text-light); font-size: 0.9rem;">Xem và kiểm duyệt toàn bộ luồng giao dịch, thanh toán đơn hàng trên toàn hệ thống.</p>
                </div>
            </header>

            <div class="admin-content">
                <!-- Flash Message -->
                <c:if test="${not empty sessionScope.flashMsg}">
                    <script>
                        Swal.fire({
                            icon: '${sessionScope.flashType == "success" ? "success" : "error"}',
                            title: '${sessionScope.flashType == "success" ? "Thành công" : "Lỗi"}',
                            text: '${sessionScope.flashMsg}',
                            timer: 3000,
                            showConfirmButton: false
                        });
                    </script>
                    <c:remove var="flashMsg" scope="session"/>
                    <c:remove var="flashType" scope="session"/>
                </c:if>

                <!-- Section 1: Đơn hàng chuyển khoản cần xác nhận thanh toán -->
                <div class="admin-panel" style="margin-bottom: var(--space-6); border-left: 4px solid #f59e0b;">
                    <h3 class="section-title" style="color: #d97706;">
                        <i class="fa-solid fa-receipt"></i> Đơn Chuyển Khoản Chờ Phê Duyệt (${pendingPayments.size()})
                    </h3>
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã Đơn</th>
                                    <th>Khách Hàng</th>
                                    <th>Gian Hàng (Shop ID)</th>
                                    <th>Thời Gian Đặt</th>
                                    <th>Số Tiền</th>
                                    <th style="text-align:center;">Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pendingPayments}">
                                        <tr>
                                            <td colspan="6" class="text-center text-muted" style="padding: 1.5rem;">Không có đơn hàng nào chờ duyệt thanh toán.</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="order" items="${pendingPayments}">
                                            <tr>
                                                <td><strong>#${order.orderId}</strong></td>
                                                <td>User ID: ${order.customerId}</td>
                                                <td>Shop ID: ${order.ownerId}</td>
                                                <td>${order.createdAt}</td>
                                                <td class="text-danger fw-bold"><ft:currency value="${order.finalAmount}" /></td>
                                                <td style="text-align:center;">
                                                    <div class="table-actions">
                                                        <form action="${pageContext.request.contextPath}/admin/orders" method="POST" style="display:inline;" onsubmit="return confirmApprove(event, '${order.orderId}')">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="confirmPayment">
                                                            <input type="hidden" name="orderId" value="${order.orderId}">
                                                            <button type="submit" class="btn btn-success btn-sm"><i class="fa-solid fa-check"></i> Xác nhận đã nhận tiền</button>
                                                        </form>
                                                        <button class="btn btn-danger btn-sm" onclick="showCancelModal('${order.orderId}')"><i class="fa-solid fa-ban"></i> Hủy đơn</button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Section 2: Bộ lọc và danh sách tất cả các đơn hàng -->
                <div class="admin-panel">
                    <h3 class="section-title">
                        <i class="fa-solid fa-list-check"></i> Lịch Sử Giao Dịch Toàn Sàn
                    </h3>
                    
                    <!-- Status Filter -->
                    <div class="filter-bar">
                        <a href="?status=" class="filter-btn ${empty statusFilter ? 'active' : ''}">Tất cả</a>
                        <a href="?status=PENDING_PAYMENT" class="filter-btn ${statusFilter == 'PENDING_PAYMENT' ? 'active' : ''}">Chờ thanh toán</a>
                        <a href="?status=CONFIRMED" class="filter-btn ${statusFilter == 'CONFIRMED' ? 'active' : ''}">Đã xác nhận</a>
                        <a href="?status=APPROVED" class="filter-btn ${statusFilter == 'APPROVED' ? 'active' : ''}">Đã duyệt (Chờ giao)</a>
                        <a href="?status=DISPATCHED" class="filter-btn ${statusFilter == 'DISPATCHED' ? 'active' : ''}">Đang giao</a>
                        <a href="?status=DELIVERED" class="filter-btn ${statusFilter == 'DELIVERED' ? 'active' : ''}">Đã nhận</a>
                        <a href="?status=CANCELLED" class="filter-btn ${statusFilter == 'CANCELLED' ? 'active' : ''}">Đã hủy</a>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã Đơn</th>
                                    <th>Khách Hàng</th>
                                    <th>Chủ Shop</th>
                                    <th>Phương Thức</th>
                                    <th>Tổng Tiền</th>
                                    <th>Trạng Thái</th>
                                    <th>Thời Gian</th>
                                    <th style="text-align:center;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty orders}">
                                        <tr>
                                            <td colspan="8" class="text-center text-muted" style="padding: 1.5rem;">Không tìm thấy dữ liệu đơn hàng nào.</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="order" items="${orders}">
                                            <tr>
                                                <td><strong>#${order.orderId}</strong></td>
                                                <td>Khách ID: ${order.customerId}</td>
                                                <td>Shop ID: ${order.ownerId}</td>
                                                <td><span class="badge" style="background:#e2e8f0; color:#475569;">${order.paymentMethod}</span></td>
                                                <td class="text-danger fw-bold"><ft:currency value="${order.finalAmount}" /></td>
                                                <td>
                                                    <span class="status-badge status-${order.status}">
                                                        <c:choose>
                                                            <c:when test="${order.status == 'PENDING_PAYMENT'}">Chờ thanh toán</c:when>
                                                            <c:when test="${order.status == 'CONFIRMED'}">Đã xác nhận</c:when>
                                                            <c:when test="${order.status == 'PREPARING'}">Shop đang chuẩn bị</c:when>
                                                            <c:when test="${order.status == 'APPROVED'}">Đã duyệt (Chờ giao)</c:when>
                                                            <c:when test="${order.status == 'DISPATCHED'}">Đang giao</c:when>
                                                            <c:when test="${order.status == 'DELIVERED'}">Đã nhận</c:when>
                                                            <c:when test="${order.status == 'CANCELLED'}">Đã hủy</c:when>
                                                            <c:otherwise>${order.status}</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                </td>
                                                <td style="font-size: 0.85rem; color: var(--color-text-light);">${order.createdAt}</td>
                                                <td style="text-align:center;">
                                                    <div class="table-actions">
                                                        <c:if test="${order.status != 'DELIVERED' && order.status != 'CANCELLED'}">
                                                            <button class="btn btn-danger btn-sm" onclick="showCancelModal('${order.orderId}')"><i class="fa-solid fa-trash-can"></i> Hủy đơn</button>
                                                        </c:if>
                                                        <c:if test="${order.status == 'DELIVERED' || order.status == 'CANCELLED'}">
                                                            <span style="font-size:0.8rem; color:var(--color-text-light); font-style:italic;">Không thể tác động</span>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:1.5rem;">
                        <span style="font-size:0.85rem; color:var(--color-text-light);">Trang ${currentPage}</span>
                        <div style="display:flex; gap:5px;">
                            <c:if test="${currentPage > 1}">
                                <a href="?status=${statusFilter}&page=${currentPage - 1}" class="btn btn-secondary btn-sm"><i class="fa-solid fa-chevron-left"></i> Trước</a>
                            </c:if>
                            <a href="?status=${statusFilter}&page=${currentPage + 1}" class="btn btn-secondary btn-sm">Sau <i class="fa-solid fa-chevron-right"></i></a>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal Hủy Đơn -->
    <div id="cancelModal" class="modal">
        <div class="modal-content">
            <h3>Hủy đơn hàng bởi Admin</h3>
            <p style="font-size:0.85rem; color:#6b7280; margin-bottom: 12px;">Vui lòng nhập lý do hủy đơn hàng này để cập nhật hệ thống và gửi thông báo.</p>
            <form action="${pageContext.request.contextPath}/admin/orders" method="POST" onsubmit="return submitCancel(event)">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <input type="hidden" name="action" value="cancelOrder">
                <input type="hidden" name="orderId" id="cancelOrderId">
                <textarea name="reason" id="cancelReason" rows="4" placeholder="Nhập lý do hủy..." required></textarea>
                <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:15px;">
                    <button type="button" class="btn btn-secondary" onclick="closeCancelModal()">Hủy bỏ</button>
                    <button type="submit" class="btn btn-danger">Xác nhận Hủy Đơn</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function confirmApprove(event, orderId) {
            event.preventDefault();
            Swal.fire({
                title: 'Duyệt thanh toán?',
                text: "Bạn xác nhận đã nhận đủ tiền cho đơn hàng #" + orderId + "?",
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#10b981',
                cancelButtonColor: '#d1d5db',
                confirmButtonText: 'Đúng, xác nhận',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    event.target.submit();
                }
            });
            return false;
        }

        function showCancelModal(orderId) {
            document.getElementById('cancelOrderId').value = orderId;
            document.getElementById('cancelReason').value = '';
            document.getElementById('cancelModal').classList.add('show');
        }

        function closeCancelModal() {
            document.getElementById('cancelModal').classList.remove('show');
        }

        function submitCancel(event) {
            event.preventDefault();
            const reason = document.getElementById('cancelReason').value.trim();
            if(!reason) {
                Swal.fire('Lỗi', 'Vui lòng nhập lý do hủy đơn hàng', 'error');
                return false;
            }
            event.target.submit();
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            const modal = document.getElementById('cancelModal');
            if (event.target === modal) {
                closeCancelModal();
            }
        }
    </script>
</body>
</html>
