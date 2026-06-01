<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Bảng Điều Khiển Giao Hàng" />
</jsp:include>

<style>
    .shipper-container {
        max-width: 1200px;
        margin: 40px auto;
        padding: 20px;
        background-color: #fff;
        border-radius: var(--radius-lg);
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        min-height: 70vh;
    }
    .shipper-header {
        display: flex; justify-content: space-between; align-items: center;
        border-bottom: 2px solid var(--color-border); padding-bottom: 20px; margin-bottom: 30px;
    }
    .shipper-header h1 { margin: 0; color: var(--color-text); font-size: 1.8rem; }
    .shipper-header p { margin: 5px 0 0; color: var(--color-text-light); }
    
    .empty-state { text-align: center; padding: 50px 20px; color: var(--color-text-light); }
    .empty-state i { font-size: 3rem; margin-bottom: 20px; color: #cbd5e1; }
    
    .order-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 20px;
    }
    
    .order-card {
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: var(--radius-md);
        overflow: hidden;
        display: flex; flex-direction: column;
    }
    .order-card-header {
        padding: 15px; border-bottom: 1px solid #e2e8f0; background: #fff;
        display: flex; justify-content: space-between; align-items: center;
    }
    .order-card-header h3 { margin: 0; font-size: 1.1rem; color: #1e293b; }
    
    .badge { padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: bold; }
    .badge-ASSIGNED { background: #dbeafe; color: #1e40af; }
    .badge-PICKED_UP { background: #fef3c7; color: #92400e; }
    .badge-IN_TRANSIT { background: #f3e8ff; color: #6b21a8; }
    .badge-DELIVERED { background: #dcfce7; color: #166534; }
    .badge-FAILED { background: #fee2e2; color: #991b1b; }
    
    .order-card-body { padding: 15px; flex-grow: 1; }
    .info-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 10px; font-size: 0.9rem; }
    .info-row i { color: #94a3b8; margin-top: 3px; }
    .fail-reason { background: #fee2e2; color: #991b1b; padding: 10px; border-radius: 6px; font-size: 0.85rem; margin-top: 15px; }
    
    .order-card-footer { padding: 15px; background: #fff; border-top: 1px solid #e2e8f0; }
    .action-btn { width: 100%; display: block; text-align: center; margin-bottom: 5px; }
    .action-group { display: flex; gap: 10px; }
    .action-group .btn { flex: 1; }
    
    /* Modals */
    .modal-overlay {
        display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(0,0,0,0.5); z-index: 1000;
        align-items: center; justify-content: center;
    }
    .modal-overlay.show { display: flex; }
    .modal-content {
        background: #fff; width: 400px; padding: 25px; border-radius: var(--radius-lg);
        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
    }
    .modal-content h3 { margin-top: 0; margin-bottom: 15px; color: #1e293b; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; font-weight: 500; margin-bottom: 5px; font-size: 0.9rem; }
    .form-group input, .form-group textarea {
        width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px;
    }
    .modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
</style>

<div class="shipper-container">
    <div class="shipper-header">
        <div>
            <h1><i class="fa-solid fa-truck-fast"></i> Nhiệm Vụ Giao Hàng</h1>
            <p>Quản lý các đơn hàng được phân công cho bạn</p>
        </div>
        <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary btn-sm">
            <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
        </a>
    </div>

    <c:if test="${empty deliveryList}">
        <div class="empty-state">
            <i class="fa-solid fa-box-open"></i>
            <h2>Chưa có đơn hàng nào</h2>
            <p>Hiện tại bạn chưa được phân công giao đơn hàng nào. Hãy chờ đợi hoặc liên hệ Quản lý.</p>
        </div>
    </c:if>

    <div class="order-grid">
        <c:forEach var="deliv" items="${deliveryList}">
            <div class="order-card">
                <div class="order-card-header">
                    <h3>Đơn hàng #${deliv.orderId}</h3>
                    <span class="badge badge-${deliv.status}">
                        <c:choose>
                            <c:when test="${deliv.status == 'ASSIGNED'}">Mới nhận</c:when>
                            <c:when test="${deliv.status == 'PICKED_UP'}">Đã lấy hàng</c:when>
                            <c:when test="${deliv.status == 'IN_TRANSIT'}">Đang giao</c:when>
                            <c:when test="${deliv.status == 'DELIVERED'}">Thành công</c:when>
                            <c:when test="${deliv.status == 'FAILED'}">Thất bại</c:when>
                        </c:choose>
                    </span>
                </div>
                
                <div class="order-card-body">
                    <div class="info-row">
                        <i class="fa-solid fa-clock"></i>
                        <div>
                            <strong style="display:block; color:#475569;">Dự kiến giao:</strong>
                            <c:choose>
                                <c:when test="${not empty deliv.estimatedDeliveryTime}">
                                    <fmt:parseDate value="${deliv.estimatedDeliveryTime}" pattern="yyyy-MM-dd'T'HH:mm" var="estDate" type="both" />
                                    <b style="color:var(--color-primary);"><fmt:formatDate value="${estDate}" pattern="dd/MM/yyyy HH:mm" /></b>
                                </c:when>
                                <c:otherwise><span style="color:#94a3b8; font-style:italic;">Chưa thiết lập</span></c:otherwise>
                            </c:choose>
                            <button type="button" onclick="openEstimateModal(${deliv.deliveryId})" style="background:none; border:none; color:var(--color-primary); text-decoration:underline; cursor:pointer; font-size:0.8rem; margin-left:10px;">Cập nhật</button>
                        </div>
                    </div>
                    
                    <c:if test="${deliv.status == 'FAILED'}">
                        <div class="fail-reason">
                            <strong>Lý do thất bại:</strong> ${deliv.failureReason}
                        </div>
                    </c:if>
                </div>
                
                <div class="order-card-footer">
                    <c:choose>
                        <c:when test="${deliv.status == 'ASSIGNED'}">
                            <button onclick="updateStatus(${deliv.deliveryId}, 'PICKED_UP')" class="btn btn-primary action-btn">Xác nhận Đã Lấy Hàng</button>
                        </c:when>
                        <c:when test="${deliv.status == 'PICKED_UP'}">
                            <button onclick="updateStatus(${deliv.deliveryId}, 'IN_TRANSIT')" class="btn btn-secondary action-btn" style="background:#8b5cf6; color:#fff; border:none;">Bắt đầu Đi Giao</button>
                        </c:when>
                        <c:when test="${deliv.status == 'IN_TRANSIT'}">
                            <div class="action-group">
                                <button onclick="openProofModal(${deliv.deliveryId})" class="btn btn-success"><i class="fa-solid fa-check"></i> Thành công</button>
                                <button onclick="openFailModal(${deliv.deliveryId})" class="btn btn-danger"><i class="fa-solid fa-xmark"></i> Thất bại</button>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <button disabled class="btn btn-secondary action-btn" style="opacity:0.5; cursor:not-allowed;">Đã hoàn tất xử lý</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<!-- Modal Bằng Chứng Giao Hàng -->
<div id="proofModal" class="modal-overlay">
    <div class="modal-content">
        <h3 style="color:var(--color-success);"><i class="fa-solid fa-check-circle"></i> Giao Thành Công</h3>
        <p style="font-size:0.9rem; color:#64748b; margin-bottom:15px;">Cung cấp hình ảnh bằng chứng giao hàng thành công.</p>
        <input type="hidden" id="proofDeliveryId">
        <div class="form-group">
            <label>URL Ảnh Bằng Chứng *</label>
            <input type="text" id="proofImageUrl" placeholder="https://example.com/image.jpg">
        </div>
        <div class="modal-actions">
            <button onclick="closeModal('proofModal')" class="btn btn-secondary">Hủy</button>
            <button onclick="submitSuccess()" class="btn btn-success">Xác nhận</button>
        </div>
    </div>
</div>

<!-- Modal Giao Thất Bại -->
<div id="failModal" class="modal-overlay">
    <div class="modal-content">
        <h3 style="color:var(--color-danger);"><i class="fa-solid fa-times-circle"></i> Báo Cáo Thất Bại</h3>
        <input type="hidden" id="failDeliveryId">
        <div class="form-group">
            <label>Lý do thất bại *</label>
            <textarea id="failureReason" rows="3" placeholder="Ví dụ: Khách không nghe máy, Sai địa chỉ..."></textarea>
        </div>
        <div class="modal-actions">
            <button onclick="closeModal('failModal')" class="btn btn-secondary">Hủy</button>
            <button onclick="submitFail()" class="btn btn-danger">Xác nhận Thất bại</button>
        </div>
    </div>
</div>

<!-- Modal Thời gian dự kiến -->
<div id="estimateModal" class="modal-overlay">
    <div class="modal-content">
        <h3><i class="fa-solid fa-clock"></i> Thời Gian Dự Kiến</h3>
        <input type="hidden" id="estDeliveryId">
        <div class="form-group">
            <label>Thời gian giao hàng dự kiến *</label>
            <input type="datetime-local" id="estimatedTime">
        </div>
        <div class="modal-actions">
            <button onclick="closeModal('estimateModal')" class="btn btn-secondary">Hủy</button>
            <button onclick="submitEstimate()" class="btn btn-primary">Lưu</button>
        </div>
    </div>
</div>

<script>
function closeModal(id) { document.getElementById(id).classList.remove('show'); }
function openProofModal(id) { document.getElementById('proofDeliveryId').value = id; document.getElementById('proofImageUrl').value = ''; document.getElementById('proofModal').classList.add('show'); }
function openFailModal(id) { document.getElementById('failDeliveryId').value = id; document.getElementById('failureReason').value = ''; document.getElementById('failModal').classList.add('show'); }
function openEstimateModal(id) { document.getElementById('estDeliveryId').value = id; document.getElementById('estimateModal').classList.add('show'); }

async function apiCall(data) {
    try {
        const res = await fetch('${pageContext.request.contextPath}/delivery/api/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if (result.success) {
            location.reload();
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (e) { alert("Lỗi mạng!"); }
}

function updateStatus(id, status) {
    if(confirm("Xác nhận chuyển trạng thái?")) {
        apiCall({ action: "STATUS", deliveryId: id, status: status });
    }
}
function submitSuccess() {
    const id = document.getElementById('proofDeliveryId').value;
    const url = document.getElementById('proofImageUrl').value;
    if(!url) { alert("Vui lòng nhập link ảnh!"); return; }
    apiCall({ action: "STATUS", deliveryId: id, status: "DELIVERED", proofImageUrl: url });
}
function submitFail() {
    const id = document.getElementById('failDeliveryId').value;
    const reason = document.getElementById('failureReason').value;
    if(!reason) { alert("Vui lòng nhập lý do!"); return; }
    apiCall({ action: "STATUS", deliveryId: id, status: "FAILED", failureReason: reason });
}
function submitEstimate() {
    const id = document.getElementById('estDeliveryId').value;
    const time = document.getElementById('estimatedTime').value;
    if(!time) { alert("Vui lòng chọn thời gian!"); return; }
    apiCall({ action: "ESTIMATE", deliveryId: id, estimatedTime: time });
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
