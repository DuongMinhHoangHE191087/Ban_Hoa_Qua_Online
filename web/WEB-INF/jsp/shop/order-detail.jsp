<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Chi tiết đơn hàng #${order.orderId}"/>
</jsp:include>

<div class="container py-6">
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/shop/orders" class="inline-flex items-center gap-1 no-underline text-primary">
            <i class="fa-solid fa-arrow-left"></i> Quay lại danh sách đơn hàng
        </a>
    </div>

    <h1 class="mb-4">Chi tiết đơn hàng #${order.orderId}</h1>



    <div class="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(320px,1fr)] mb-6">
        <section class="card overflow-hidden">
            <div class="px-4 py-4 border-b border-border">
                <h2 class="m-0 text-lg">Thông tin đơn hàng</h2>
            </div>
            <div class="p-4">
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2 mb-4">
                    <div>
                        <p class="m-0 mb-1 text-sm text-muted">Ngày đặt:</p>
                        <strong><fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/></strong>
                    </div>
                    <div>
                        <p class="m-0 mb-1 text-sm text-muted">Trạng thái:</p>
                        <span class="inline-flex items-center rounded-full bg-[#e0e7ff] px-2.5 py-1 text-sm font-semibold text-[#3730a3]">
                            ${order.status}
                        </span>
                    </div>
                </div>

                <div class="border-t border-border pt-4 mb-4">
                    <p class="m-0 mb-2 text-sm font-semibold">Sản phẩm trong đơn:</p>
                    <div class="flex flex-col gap-2">
                        <c:choose>
                            <c:when test="${empty orderItems}">
                                <p class="text-muted">Không có sản phẩm nào trong đơn hàng.</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${orderItems}">
                                    <div class="flex justify-between gap-4 rounded-md bg-surface-light p-3">
                                        <div>
                                            <strong>${item.variantLabel}</strong>
                                            <br>
                                            <span class="text-sm text-muted">Số lượng: ${item.quantity}</span>
                                        </div>
                                        <div class="text-right">
                                            <strong class="text-[#d32f2f]">
                                                <fmt:formatNumber value="${item.priceAtOrder}" type="number"/> đ
                                            </strong>
                                            <br>
                                            <span class="text-sm text-muted">
                                                Tạm tính: <fmt:formatNumber value="${item.subtotal}" type="number"/> đ
                                            </span>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="border-t border-border pt-4">
                    <div class="mb-2 flex items-center justify-between">
                        <span>Tạm tính:</span>
                        <strong><fmt:formatNumber value="${order.subtotal}" type="number"/> đ</strong>
                    </div>
                    <c:if test="${order.shippingFee > 0}">
                        <div class="mb-2 flex items-center justify-between">
                            <span>Phí giao hàng:</span>
                            <strong><fmt:formatNumber value="${order.shippingFee}" type="number"/> đ</strong>
                        </div>
                    </c:if>
                    <c:if test="${order.discountAmount > 0}">
                        <div class="mb-2 flex items-center justify-between">
                            <span>Giảm giá:</span>
                            <strong class="text-success">-<fmt:formatNumber value="${order.discountAmount}" type="number"/> đ</strong>
                        </div>
                    </c:if>
                    <div class="flex items-center justify-between border-t-2 border-border pt-2 text-lg">
                        <strong>Tổng cộng:</strong>
                        <strong class="text-[#d32f2f]">
                            <fmt:formatNumber value="${order.finalAmount}" type="number"/> đ
                        </strong>
                    </div>
                </div>
            </div>
        </section>

        <div class="flex flex-col gap-4">
            <section class="card overflow-hidden">
                <div class="px-4 py-4 border-b border-border">
                    <h2 class="m-0 text-lg">Giao hàng</h2>
                </div>
                <div class="p-4">
                    <c:choose>
                        <c:when test="${not empty delivery}">
                            <div class="mb-3">
                                <p class="m-0 mb-1 text-sm text-muted">Địa chỉ:</p>
                                <p class="m-0 text-[0.95rem]">${order.deliveryAddress}</p>
                            </div>
                            <div class="mb-3">
                                <p class="m-0 mb-1 text-sm text-muted">Người nhận:</p>
                                <p class="m-0 text-[0.95rem]">${order.recipientName} - ${order.recipientPhone}</p>
                            </div>
                            <div class="mb-3">
                                <p class="m-0 mb-1 text-sm text-muted">Nhân viên giao hàng:</p>
                                <p class="m-0 text-[0.95rem]">${delivery.driverName} - ${delivery.driverPhone}</p>
                            </div>
                            <div>
                                <p class="m-0 mb-1 text-sm text-muted">Trạng thái giao hàng:</p>
                                <span class="inline-flex items-center rounded-full bg-[#dcfce7] px-2.5 py-1 text-sm font-semibold text-[#166534]">
                                    ${delivery.status}
                                </span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted">Chưa có thông tin giao hàng.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <section class="card overflow-hidden">
                <div class="px-4 py-4 border-b border-border">
                    <h2 class="m-0 text-lg">Khách hàng</h2>
                </div>
                <div class="p-4">
                    <div class="flex items-center gap-3">
                        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary text-lg font-semibold text-white">
                            ${order.customerId}
                        </div>
                        <div>
                            <p class="m-0 font-semibold">Khách hàng #${order.customerId}</p>
                            <p class="m-0 text-sm text-muted">ID: ${order.customerId}</p>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </div>

    <c:if test="${order.status != 'CANCELLED' && order.status != 'DELIVERED'}">
        <div class="flex flex-wrap gap-3">
            <c:if test="${order.status == 'PENDING'}">
                <form action="${pageContext.request.contextPath}/shop/orders" method="post" class="inline-flex">
                    <input type="hidden" name="action" value="confirm">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="inline-flex items-center gap-2 rounded-full bg-[#10b981] px-4 py-2.5 text-sm font-bold text-white shadow-sm">
                        <i class="fa-solid fa-check"></i> Xác nhận đơn hàng
                    </button>
                </form>
            </c:if>
            <c:if test="${order.status == 'PROCESSING'}">
                <form action="${pageContext.request.contextPath}/shop/orders" method="post" class="inline-flex">
                    <input type="hidden" name="action" value="dispatch">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="inline-flex items-center gap-2 rounded-full bg-primary px-4 py-2.5 text-sm font-bold text-white shadow-sm">
                        <i class="fa-solid fa-truck"></i> Chuẩn bị giao hàng
                    </button>
                </form>
            </c:if>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
