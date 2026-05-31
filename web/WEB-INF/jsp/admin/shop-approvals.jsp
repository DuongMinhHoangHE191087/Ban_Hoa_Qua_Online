<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phê duyệt cửa hàng - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .status-PENDING { background: #fef3c7; color: #b45309; border: 1px solid #fde68a; }
        .status-APPROVED { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .status-REJECTED { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .table-actions { display: flex; gap: var(--space-2); justify-content: center; }
        
        .modal {
            display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.5); align-items: center; justify-content: center;
        }
        .modal.show { display: flex; }
        .modal-content {
            background-color: #fff; padding: 20px; border-radius: 12px; width: 400px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .modal-content h3 { margin-top: 0; color: #111827; }
        .modal-content textarea {
            width: 100%; border: 1px solid #d1d5db; border-radius: 8px; padding: 10px;
            margin-top: 10px; resize: none;
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="shops"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <div>
                    <h1>Phê Duyệt Cửa Hàng</h1>
                    <p style="color: var(--color-text-light); font-size: 0.9rem;">Quản lý và duyệt các yêu cầu mở cửa hàng mới của User.</p>
                </div>
            </header>

            <div class="admin-content">
                <div class="admin-panel">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;"><i class="fa-solid fa-store"></i> Danh sách yêu cầu</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <input type="text" id="shopSearch" placeholder="Tìm tên shop..." style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                            <button class="btn btn-secondary btn-sm"><i class="fa-solid fa-search"></i></button>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Tên Cửa Hàng</th>
                                    <th>Mô tả</th>
                                    <th>Địa chỉ</th>
                                    <th style="text-align:center;">Trạng thái</th>
                                    <th style="text-align:center;">Hành động</th>
                                </tr>
                            </thead>
                            <tbody id="shopTableBody">
                                <c:choose>
                                    <c:when test="${empty shopList}">
                                        <tr><td colspan="5" class="text-center text-muted">Không có yêu cầu mở shop nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="shop" items="${shopList}">
                                            <tr>
                                                <td>
                                                    <strong class="shop-name-col">${shop.shopName}</strong><br>
                                                    <small style="color:var(--color-text-light);">User ID: ${shop.userId}</small>
                                                </td>
                                                <td><div style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${fn:escapeXml(shop.shopDescription)}">${shop.shopDescription}</div></td>
                                                <td><div style="max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${fn:escapeXml(shop.deliveryAddress)}">${shop.deliveryAddress}</div></td>
                                                <td style="text-align:center;">
                                                    <span class="status-badge status-${shop.approvalStatus}" id="status-badge-${shop.profileId}">
                                                        <c:choose>
                                                            <c:when test="${shop.approvalStatus == 'PENDING'}"><i class="fa-solid fa-clock"></i> Chờ Duyệt</c:when>
                                                            <c:when test="${shop.approvalStatus == 'APPROVED'}"><i class="fa-solid fa-check-circle"></i> Đã Duyệt</c:when>
                                                            <c:when test="${shop.approvalStatus == 'REJECTED'}"><i class="fa-solid fa-times-circle"></i> Từ Chối</c:when>
                                                        </c:choose>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="table-actions" id="action-btns-${shop.profileId}">
                                                        <c:if test="${shop.approvalStatus == 'PENDING'}">
                                                            <button class="btn btn-success btn-sm" onclick="approveShop(${shop.profileId})" title="Duyệt"><i class="fa-solid fa-check"></i></button>
                                                            <button class="btn btn-danger btn-sm" onclick="showRejectModal(${shop.profileId})" title="Từ chối"><i class="fa-solid fa-times"></i></button>
                                                        </c:if>
                                                        <c:if test="${shop.approvalStatus != 'PENDING'}">
                                                            <span style="font-size:0.8rem; color:var(--color-text-light); font-style:italic;">Đã xử lý</span>
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
                </div>
            </div>
        </main>
    </div>

    <!-- Reject Modal -->
    <div id="rejectModal" class="modal">
        <div class="modal-content">
            <h3>Từ chối mở cửa hàng</h3>
            <p style="font-size:0.85rem; color:#6b7280;">Nhập lý do từ chối để thông báo cho người dùng.</p>
            <input type="hidden" id="rejectProfileId">
            <textarea id="rejectionReason" rows="4" placeholder="Giấy tờ không hợp lệ..."></textarea>
            <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:15px;">
                <button class="btn btn-secondary" onclick="closeRejectModal()">Hủy</button>
                <button class="btn btn-danger" onclick="submitReject()">Xác nhận Từ chối</button>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('shopSearch').addEventListener('input', function(e) {
            const term = e.target.value.toLowerCase();
            document.querySelectorAll('#shopTableBody tr').forEach(row => {
                const nameEl = row.querySelector('.shop-name-col');
                if(!nameEl) return;
                const name = nameEl.textContent.toLowerCase();
                row.style.display = name.includes(term) ? '' : 'none';
            });
        });

        function approveShop(profileId) {
            Swal.fire({
                title: 'Duyệt cửa hàng này?',
                text: "Cửa hàng sẽ được cấp quyền hoạt động ngay lập tức.",
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#10b981',
                cancelButtonColor: '#d1d5db',
                confirmButtonText: 'Có, Duyệt ngay',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    updateShopStatus(profileId, 'APPROVED', '');
                }
            });
        }

        function showRejectModal(profileId) {
            document.getElementById('rejectProfileId').value = profileId;
            document.getElementById('rejectionReason').value = '';
            document.getElementById('rejectModal').classList.add('show');
        }

        function closeRejectModal() {
            document.getElementById('rejectModal').classList.remove('show');
        }

        function submitReject() {
            const profileId = document.getElementById('rejectProfileId').value;
            const reason = document.getElementById('rejectionReason').value.trim();
            if(!reason) {
                Swal.fire('Lỗi', 'Vui lòng nhập lý do từ chối', 'error');
                return;
            }
            closeRejectModal();
            updateShopStatus(profileId, 'REJECTED', reason);
        }

        function updateShopStatus(profileId, status, reason) {
            fetch('${pageContext.request.contextPath}/admin/shops/approve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'profileId=' + profileId + '&status=' + status + '&rejectionReason=' + encodeURIComponent(reason) + '&_csrf=${sessionScope._csrfToken}'
            })
            .then(response => response.json())
            .then(data => {
                if(data.success) {
                    Swal.fire({ icon: 'success', title: 'Thành công', text: data.message, timer: 1500, showConfirmButton: false });
                    const badgeContainer = document.getElementById('status-badge-' + profileId);
                    const actionContainer = document.getElementById('action-btns-' + profileId);
                    if(status === 'APPROVED') {
                        badgeContainer.className = 'status-badge status-APPROVED';
                        badgeContainer.innerHTML = '<i class="fa-solid fa-check-circle"></i> Đã Duyệt';
                    } else if(status === 'REJECTED') {
                        badgeContainer.className = 'status-badge status-REJECTED';
                        badgeContainer.innerHTML = '<i class="fa-solid fa-times-circle"></i> Từ Chối';
                        badgeContainer.title = reason;
                    }
                    actionContainer.innerHTML = '<span style="font-size:0.8rem; color:var(--color-text-light); font-style:italic;">Đã xử lý</span>';
                } else {
                    Swal.fire('Lỗi', data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire('Lỗi', 'Lỗi kết nối mạng.', 'error');
            });
        }
    </script>
</body>
</html>
