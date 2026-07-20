<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quản lý yêu cầu đổi trả (Admin)"/></jsp:include>

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
                                            <br/><small class="text-xs text-gray-500">Phân loại: <c:out value="${req.variantLabel}"/></small>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <small class="text-xs italic text-gray-400">Toàn bộ đơn hàng</small>
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
                                <span class="text-sm font-medium"><c:out value="${req.reasonCode}"/></span>
                                <c:if test="${not empty req.description}">
                                    <br/><small class="text-xs text-gray-500"><c:out value="${req.description}"/></small>
                                </c:if>
                                <c:if test="${not empty req.evidenceUrl}">
                                    <br/><a href="<c:out value="${req.evidenceUrl}"/>" target="_blank" class="text-[0.8em]"><i class="fa-solid fa-image"></i> Xem bằng chứng</a>
                                </c:if>
                            </td>
                            <td><c:out value="${req.requestedQuantity}"/></td>
                            <td class="text-[#d32f2f] font-bold"><c:out value="${req.refundAmount}"/>đ</td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.status == 'REQUESTED'}">
                                        <span class="status-badge status-requested">Chưa duyệt</span>
                                    </c:when>
                                    <c:when test="${req.status == 'PROCESSING'}">
                                        <span class="status-badge status-badge-pending">Đang xử lý</span>
                                    </c:when>
                                    <c:when test="${req.status == 'APPROVED'}">
                                        <span class="status-badge status-approved">Đã duyệt</span>
                                    </c:when>
                                    <c:when test="${req.status == 'COMPLETED'}">
                                        <span class="status-badge status-badge-approved">Đã hoàn tiền</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-rejected">Từ chối</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${req.createdAt}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.status == 'REQUESTED'}">
                                        <button onclick="openDecisionModal('${req.returnRequestId}', 'APPROVED')" class="btn btn-sm btn-success mr-1">Duyệt</button>
                                        <button onclick="openDecisionModal('${req.returnRequestId}', 'REJECTED')" class="btn btn-sm btn-danger">Từ chối</button>
                                    </c:when>
                                    <c:when test="${req.status == 'PROCESSING'}">
                                        <button class="btn btn-sm btn-info" disabled>Đang xử lý</button>
                                    </c:when>
                                    <c:otherwise>
                                        <small class="text-xs italic text-gray-500">Đã quyết định bởi Admin #${req.decidedBy}</small>
                                        <c:if test="${not empty req.decisionReason}">
                                            <br/><small class="text-xs text-gray-500">Lý do: <c:out value="${req.decisionReason}"/></small>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty returnRequests}">
                        <tr>
                            <td colspan="10" class="px-6 py-8 text-center text-gray-400">Không có yêu cầu đổi trả nào.</td>
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
        <h4 class="mb-5 font-bold text-lg" id="modalTitle">Phán quyết yêu cầu</h4>
        <form action="${pageContext.request.contextPath}/returns" method="post">
            <input type="hidden" name="action" value="decide"/>
            <input type="hidden" name="requestId" id="modalReqId"/>
            <input type="hidden" name="status" id="modalStatus"/>
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}"/>

            <div class="form-group">
                <label for="reason" class="form-label">Lý do duyệt/từ chối <span class="text-red-600">*</span></label>
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
