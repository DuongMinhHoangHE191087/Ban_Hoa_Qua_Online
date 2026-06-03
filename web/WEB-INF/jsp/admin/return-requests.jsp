<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quản lý yêu cầu đổi trả (Admin)"/></jsp:include>

<style>
    .admin-container {
        max-width: 1200px;
        margin: 40px auto;
        padding: 0 15px;
    }
    .panel-card {
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
        border: 1px solid #eef2f3;
        padding: 30px;
    }
    .panel-title {
        font-family: 'Lexend', sans-serif;
        font-weight: 700;
        color: #1e3a8a;
        margin-bottom: 25px;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .status-badge {
        font-weight: 600;
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.85em;
    }
    .status-requested { background: #fff3e0; color: #e65100; }
    .status-approved { background: #e8f5e9; color: #1b5e20; }
    .status-rejected { background: #ffebee; color: #c62828; }
    
    .table-custom th {
        background-color: #f8fafc;
        color: #475569;
        font-weight: 600;
        border-bottom: 2px solid #e2e8f0;
    }
    .table-custom td {
        vertical-align: middle;
    }
    .modal-custom {
        display: none;
        position: fixed;
        top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0, 0, 0, 0.5);
        z-index: 1000;
        align-items: center;
        justify-content: center;
    }
    .modal-content-custom {
        background: #fff;
        padding: 30px;
        border-radius: 12px;
        width: 100%;
        max-width: 500px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.1);
    }
</style>

<div class="admin-container">
    <div class="panel-card">
        <h2 class="panel-title"><i class="fa-solid fa-list-check"></i> Quản lý Yêu cầu Đổi trả hàng (Admin)</h2>

        <div class="table-responsive">
            <table class="table table-hover table-custom">
                <thead>
                    <tr>
                        <th>Mã YC</th>
                        <th>Đơn hàng</th>
                        <th>Sản phẩm</th>
                        <th>Khách hàng</th>
                        <th>Loại YC</th>
                        <th>Lý do</th>
                        <th>Số lượng</th>
                        <th>Số tiền hoàn</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="req" items="${returnRequests}">
                        <tr>
                            <td><strong>#<c:out value="${req.returnRequestId}"/></strong></td>
                            <td><a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${req.orderId}">#<c:out value="${req.orderId}"/></a></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty req.productName}">
                                        <c:out value="${req.productName}"/>
                                        <c:if test="${not empty req.variantLabel}">
                                            <br/><small style="color:#888;">Phân loại: <c:out value="${req.variantLabel}"/></small>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <small style="color:#999; font-style:italic;">Toàn bộ đơn hàng</small>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>ID: <c:out value="${req.customerId}"/></td>
                            <td>
                                <span class="badge ${req.requestType == 'RETURN' ? 'badge-danger' : 'badge-primary'}">
                                    <c:out value="${req.requestType}"/>
                                </span>
                            </td>
                            <td>
                                <span style="font-weight: 500; font-size: 0.9em;"><c:out value="${req.reasonCode}"/></span>
                                <c:if test="${not empty req.description}">
                                    <br/><small style="color:#777;"><c:out value="${req.description}"/></small>
                                </c:if>
                                <c:if test="${not empty req.evidenceUrl}">
                                    <br/><a href="<c:out value="${req.evidenceUrl}"/>" target="_blank" style="font-size:0.8em;"><i class="fa-solid fa-image"></i> Xem bằng chứng</a>
                                </c:if>
                            </td>
                            <td><c:out value="${req.requestedQuantity}"/></td>
                            <td style="color:#d32f2f; font-weight:700;"><c:out value="${req.refundAmount}"/>đ</td>
                            <td>
                                <span class="status-badge ${req.status == 'REQUESTED' ? 'status-requested' : (req.status == 'APPROVED' ? 'status-approved' : 'status-rejected')}">
                                    <c:out value="${req.status}"/>
                                </span>
                            </td>
                            <td><c:out value="${req.createdAt}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.status == 'REQUESTED'}">
                                        <button onclick="openDecisionModal('${req.returnRequestId}', 'APPROVED')" class="btn btn-sm btn-success mr-1">Duyệt</button>
                                        <button onclick="openDecisionModal('${req.returnRequestId}', 'REJECTED')" class="btn btn-sm btn-danger">Từ chối</button>
                                    </c:when>
                                    <c:otherwise>
                                        <small style="color: #666; font-style:italic;">Đã quyết định bởi Admin #${req.decidedBy}</small>
                                        <c:if test="${not empty req.decisionReason}">
                                            <br/><small style="color:#888;">Lý do: <c:out value="${req.decisionReason}"/></small>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty returnRequests}">
                        <tr>
                            <td colspan="10" class="text-center" style="color: #888; padding: 30px 0;">Không có yêu cầu đổi trả nào.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal Quyết định -->
<div id="decisionModal" class="modal-custom">
    <div class="modal-content-custom">
        <h4 style="font-family:'Lexend',sans-serif; margin-bottom: 20px;" id="modalTitle">Phán quyết yêu cầu</h4>
        <form action="${pageContext.request.contextPath}/returns" method="post">
            <input type="hidden" name="action" value="decide"/>
            <input type="hidden" name="requestId" id="modalReqId"/>
            <input type="hidden" name="status" id="modalStatus"/>
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}"/>

            <div class="form-group">
                <label for="reason" class="form-label">Lý do duyệt/từ chối <span style="color:red;">*</span></label>
                <textarea class="form-control" name="reason" id="reason" rows="4" required placeholder="Nhập lý do chi tiết cho quyết định này để gửi cho khách hàng..."></textarea>
            </div>

            <div class="text-right mt-4">
                <button type="button" onclick="closeDecisionModal()" class="btn btn-secondary mr-2">Đóng</button>
                <button type="submit" class="btn btn-primary" id="btnSubmitModal">Xác nhận</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openDecisionModal(reqId, status) {
        document.getElementById('modalReqId').value = reqId;
        document.getElementById('modalStatus').value = status;
        
        const titleEl = document.getElementById('modalTitle');
        const submitBtn = document.getElementById('btnSubmitModal');
        
        if (status === 'APPROVED') {
            titleEl.innerText = "Duyệt yêu cầu Đổi/Trả hàng #" + reqId;
            submitBtn.className = "btn btn-success";
            submitBtn.innerText = "Duyệt hoàn trả";
        } else {
            titleEl.innerText = "Từ chối yêu cầu Đổi/Trả hàng #" + reqId;
            submitBtn.className = "btn btn-danger";
            submitBtn.innerText = "Từ chối";
        }
        
        document.getElementById('decisionModal').style.display = 'flex';
    }

    function closeDecisionModal() {
        document.getElementById('decisionModal').style.display = 'none';
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
