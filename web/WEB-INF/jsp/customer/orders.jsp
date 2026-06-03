<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Lịch sử đơn hàng" />
</jsp:include>

<div class="container my-5">
    <h2 class="mb-4">Lịch sử đơn hàng của bạn</h2>

    <c:if test="${not empty sessionScope.flashMsg}">
        <div class="alert alert-${sessionScope.flashType} alert-dismissible fade show" role="alert">
            ${sessionScope.flashMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <div class="row">
        <c:forEach var="order" items="${orders}">
            <div class="col-12 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                        <div>
                            <span class="fw-bold text-primary">Đơn hàng #${order.orderId}</span>
                            <span class="text-muted ms-2" style="font-size: 0.9rem;">Đặt ngày: ${order.createdAt}</span>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${order.status == 'PENDING_PAYMENT'}"><span class="badge bg-warning text-dark px-3 py-2">Chờ thanh toán</span></c:when>
                                <c:when test="${order.status == 'CONFIRMED'}"><span class="badge bg-info text-dark px-3 py-2">Đã xác nhận</span></c:when>
                                <c:when test="${order.status == 'PREPARING'}"><span class="badge bg-secondary px-3 py-2">Shop đang chuẩn bị</span></c:when>
                                <c:when test="${order.status == 'APPROVED'}"><span class="badge bg-primary px-3 py-2">Đã duyệt (Chờ giao)</span></c:when>
                                <c:when test="${order.status == 'DISPATCHED'}"><span class="badge bg-info text-dark px-3 py-2">Đang giao hàng</span></c:when>
                                <c:when test="${order.status == 'SHIPPED'}"><span class="badge bg-info text-dark px-3 py-2">Đang giao hàng</span></c:when>
                                <c:when test="${order.status == 'DELIVERED'}"><span class="badge bg-success px-3 py-2">Đã nhận hàng</span></c:when>
                                <c:when test="${order.status == 'CANCELLED'}"><span class="badge bg-danger px-3 py-2">Đã hủy</span></c:when>
                                <c:otherwise><span class="badge bg-dark px-3 py-2">${order.status}</span></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="card-body row">
                        <div class="col-md-8 border-end">
                            <!-- Order details -->
                            <p class="mb-1"><strong>Giao đến:</strong> ${order.deliveryAddress}</p>
                            <p class="mb-1"><strong>Ghi chú:</strong> ${order.notes != null ? order.notes : 'Không có'}</p>
                            <c:if test="${not empty deliveryMap[order.orderId].estimatedDeliveryTime}">
                                <p class="mb-1 text-primary"><strong>Dự kiến nhận hàng:</strong> 
                                    <fmt:parseDate value="${deliveryMap[order.orderId].estimatedDeliveryTime}" pattern="yyyy-MM-dd'T'HH:mm" var="estDate" type="both" />
                                    <fmt:formatDate value="${estDate}" pattern="dd/MM/yyyy HH:mm" />
                                </p>
                            </c:if>
                            <p class="mb-1 text-danger fw-bold">Tổng tiền: <ft:formatCurrency value="${order.finalAmount}" /></p>
                        </div>
                        <div class="col-md-4 d-flex flex-column justify-content-center align-items-center text-center">
                            <c:if test="${order.status == 'PENDING_PAYMENT' || order.status == 'CONFIRMED'}">
                                <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-100">
                                    <input type="hidden" name="action" value="cancel">
                                    <input type="hidden" name="orderId" value="${order.orderId}">
                                    <input type="hidden" name="reason" value="Khách hàng tự hủy trên web">
                                    <button type="submit" class="btn btn-outline-danger w-75 mb-2" onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng này?');">Hủy đơn hàng</button>
                                </form>
                            </c:if>

                            <c:if test="${order.status == 'DISPATCHED' || order.status == 'SHIPPED'}">
                                <form action="${pageContext.request.contextPath}/orders" method="POST" class="w-100">
                                    <input type="hidden" name="action" value="confirmDelivery">
                                    <input type="hidden" name="orderId" value="${order.orderId}">
                                    <button type="submit" class="btn btn-success w-75 mb-2">Đã nhận hàng</button>
                                </form>
                                <small class="text-muted d-block mt-2">Vui lòng xác nhận sau khi bạn đã kiểm tra và nhận đủ hàng.</small>
                            </c:if>

                            <c:if test="${order.status == 'DELIVERED'}">
                                <a href="${pageContext.request.contextPath}/reviews?orderId=${order.orderId}" class="btn btn-outline-primary w-75 mb-2">Đánh giá sản phẩm</a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty orders}">
            <div class="col-12 text-center py-5">
                <i class="fa-solid fa-box-open text-muted" style="font-size: 4rem; margin-bottom: 1rem;"></i>
                <h4 class="text-muted">Bạn chưa có đơn hàng nào</h4>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary mt-3">Mua sắm ngay</a>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
