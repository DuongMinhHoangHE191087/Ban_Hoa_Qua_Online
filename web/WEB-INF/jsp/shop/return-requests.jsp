<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Theo dõi yêu cầu đổi trả (Cửa hàng)"/></jsp:include>

<style>
    .shop-container {
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
        color: #2e7d32;
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
</style>

<div class="shop-container">
    <div class="panel-card">
        <h2 class="panel-title"><i class="fa-solid fa-clock-rotate-left"></i> Theo dõi Yêu cầu Đổi trả hàng (Cửa hàng)</h2>
        <p style="color: #666; font-size:0.95em; margin-bottom: 20px;">Danh sách các yêu cầu đổi/trả/hoàn tiền từ khách hàng đối với các đơn hàng của gian hàng bạn. Admin sẽ là bên trung gian phân xử và đưa ra phán quyết.</p>

        <div class="table-responsive">
            <table class="table table-hover table-custom">
                <thead>
                    <tr>
                        <th>Mã YC</th>
                        <th>Đơn hàng</th>
                        <th>Sản phẩm</th>
                        <th>Khách hàng</th>
                        <th>Loại YC</th>
                        <th>Lý do & Mô tả</th>
                        <th>Số lượng</th>
                        <th>Số tiền hoàn</th>
                        <th>Trạng thái YC</th>
                        <th>Ngày tạo</th>
                        <th>Quyết định của Sàn</th>
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
                                        <span class="text-warning" style="font-style:italic; font-size:0.9em;"><i class="fa-solid fa-spinner fa-spin"></i> Chờ sàn duyệt...</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-dark" style="font-size:0.9em;">
                                            <strong>${req.status == 'APPROVED' ? 'Đã chấp nhận hoàn tiền' : 'Đã từ chối'}</strong>
                                        </span>
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
                            <td colspan="10" class="text-center" style="color: #888; padding: 30px 0;">Chưa có yêu cầu đổi trả nào từ khách hàng.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
