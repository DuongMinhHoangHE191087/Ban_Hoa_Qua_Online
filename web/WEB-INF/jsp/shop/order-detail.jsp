<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Chi tiết đơn #${order.orderId}"/></jsp:include>

<div class="container" style="padding: var(--space-6) 0;">
    <div style="margin-bottom: var(--space-4);">
        <a href="${pageContext.request.contextPath}/shop/orders" style="color: var(--color-primary); text-decoration: none; display: flex; align-items: center; gap: var(--space-1);">
            <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách đơn hàng
        </a>
    </div>

    <h1 style="margin-bottom: var(--space-4);">Chi tiết đơn #${order.orderId}</h1>

    <c:if test="${not empty sessionScope.flashMsg}">
        <div style="padding: var(--space-3); margin-bottom: var(--space-4); border-radius: var(--radius-md); background: ${sessionScope.flashType == 'success' ? '#dcfce7' : '#fee2e2'}; color: ${sessionScope.flashType == 'success' ? '#166534' : '#991b1b'}; border: 1px solid ${sessionScope.flashType == 'success' ? '#86efac' : '#fca5a5'};">
            <strong>${sessionScope.flashMsg}</strong>
        </div>
        <c:remove var="flashMsg" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>

    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: var(--space-4); margin-bottom: var(--space-6);">
        <!-- Thông tin đơn hàng -->
        <section class="card">
            <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border);">
                <h2 style="margin: 0; font-size: var(--font-size-lg);">Thông tin đơn hàng</h2>
            </div>
            <div style="padding: var(--space-4);">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-3); margin-bottom: var(--space-4);">
                    <div>
                        <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Ngày đặt:</p>
                        <strong><fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/></strong>
                    </div>
                    <div>
                        <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Trạng thái:</p>
                        <span style="background: #e0e7ff; color: #3730a3; padding: 4px 10px; border-radius: var(--radius-full); font-size: 0.85rem; font-weight: 600;">
                            ${order.status}
                        </span>
                    </div>
                </div>

                <div style="border-top: 1px solid var(--color-border); padding-top: var(--space-4); margin-bottom: var(--space-4);">
                    <p style="margin: 0 0 var(--space-2) 0; font-weight: 600; font-size: 0.95rem;">Sản phẩm trong đơn:</p>
                    <div style="display: flex; flex-direction: column; gap: var(--space-2);">
                        <c:choose>
                            <c:when test="${empty orderItems}">
                                <p style="color: var(--color-muted);">Không có sản phẩm nào trong đơn hàng.</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${orderItems}">
                                    <div style="display: flex; justify-content: space-between; padding: var(--space-2); background: var(--color-surface-light); border-radius: var(--radius-md);">
                                        <div>
                                            <strong>${item.variantLabel}</strong>
                                            <br>
                                            <span style="font-size: 0.9rem; color: var(--color-muted);">Số lượng: ${item.quantity}</span>
                                        </div>
                                        <div style="text-align: right;">
                                            <strong style="color: var(--color-danger);">
                                                <fmt:formatNumber value="${item.priceAtOrder}" type="number"/> đ
                                            </strong>
                                            <br>
                                            <span style="font-size: 0.85rem; color: var(--color-muted);">
                                                Subtotal: <fmt:formatNumber value="${item.subtotal}" type="number"/> đ
                                            </span>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div style="border-top: 1px solid var(--color-border); padding-top: var(--space-4);">
                    <div style="display: flex; justify-content: space-between; margin-bottom: var(--space-2);">
                        <span>Subtotal:</span>
                        <strong><fmt:formatNumber value="${order.subtotal}" type="number"/> đ</strong>
                    </div>
                    <c:if test="${order.shippingFee > 0}">
                        <div style="display: flex; justify-content: space-between; margin-bottom: var(--space-2);">
                            <span>Phí giao hàng:</span>
                            <strong><fmt:formatNumber value="${order.shippingFee}" type="number"/> đ</strong>
                        </div>
                    </c:if>
                    <c:if test="${order.discountAmount > 0}">
                        <div style="display: flex; justify-content: space-between; margin-bottom: var(--space-2);">
                            <span>Giảm giá:</span>
                            <strong style="color: var(--color-success);">-<fmt:formatNumber value="${order.discountAmount}" type="number"/> đ</strong>
                        </div>
                    </c:if>
                    <div style="display: flex; justify-content: space-between; border-top: 2px solid var(--color-border); padding-top: var(--space-2); font-size: 1.1rem;">
                        <strong>Tổng cộng:</strong>
                        <strong style="color: var(--color-danger);">
                            <fmt:formatNumber value="${order.finalAmount}" type="number"/> đ
                        </strong>
                    </div>
                </div>
            </div>
        </section>

        <!-- Thông tin giao hàng & khách hàng -->
        <div style="display: flex; flex-direction: column; gap: var(--space-4);">
            <section class="card">
                <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border);">
                    <h2 style="margin: 0; font-size: var(--font-size-lg);">Giao hàng</h2>
                </div>
                <div style="padding: var(--space-4);">
                    <c:choose>
                        <c:when test="${not empty delivery}">
                            <div style="margin-bottom: var(--space-3);">
                                <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Địa chỉ:</p>
                                <p style="margin: 0; font-size: 0.95rem;">${order.deliveryAddress}</p>
                            </div>
                            <div style="margin-bottom: var(--space-3);">
                                <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Người nhận:</p>
                                <p style="margin: 0; font-size: 0.95rem;">${order.recipientName} - ${order.recipientPhone}</p>
                            </div>
                            <div style="margin-bottom: var(--space-3);">
                                <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Nhân viên giao hàng:</p>
                                <p style="margin: 0; font-size: 0.95rem;">${delivery.driverName} - ${delivery.driverPhone}</p>
                            </div>
                            <div>
                                <p style="margin: 0 0 var(--space-1) 0; color: var(--color-muted); font-size: 0.9rem;">Trạng thái giao hàng:</p>
                                <span style="background: #dcfce7; color: #166534; padding: 4px 10px; border-radius: var(--radius-full); font-size: 0.85rem; font-weight: 600;">
                                    ${delivery.status}
                                </span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p style="color: var(--color-muted);">Chưa có thông tin giao hàng.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <section class="card">
                <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border);">
                    <h2 style="margin: 0; font-size: var(--font-size-lg);">Khách hàng</h2>
                </div>
                <div style="padding: var(--space-4);">
                    <div style="display: flex; align-items: center; gap: var(--space-3);">
                        <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; font-size: 1.2rem;">
                            ${order.customerId}
                        </div>
                        <div>
                            <p style="margin: 0; font-weight: 600;">Khách #${order.customerId}</p>
                            <p style="margin: 0; color: var(--color-muted); font-size: 0.9rem;">ID: ${order.customerId}</p>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </div>

    <!-- Thao tác -->
    <c:if test="${order.status != 'CANCELLED' && order.status != 'DELIVERED'}">
        <div style="display: flex; gap: var(--space-3); flex-wrap: wrap;">
            <c:if test="${order.status == 'PENDING'}">
                <form action="${pageContext.request.contextPath}/shop/orders" method="post" style="display: inline;">
                    <input type="hidden" name="action" value="confirm">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-primary" style="background: var(--color-success); border: none;">
                        <i class="fa-solid fa-check"></i> Xác nhận đơn hàng
                    </button>
                </form>
            </c:if>
            <c:if test="${order.status == 'PROCESSING'}">
                <form action="${pageContext.request.contextPath}/shop/orders" method="post" style="display: inline;">
                    <input type="hidden" name="action" value="dispatch">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-truck"></i> Chuẩn bị giao hàng
                    </button>
                </form>
            </c:if>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
