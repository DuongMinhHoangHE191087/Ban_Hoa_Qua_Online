<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Tổng quan khách hàng"/></jsp:include>

<div class="container py-6">
    <div class="grid grid-cols-1 gap-4 mb-6 lg:grid-cols-2">
        <!-- Đơn hàng gần đây -->
        <section class="card overflow-hidden">
            <div class="px-4 py-4 border-b border-border">
                <h2 class="m-0 text-lg">
                    <i class="fa-solid fa-shopping-bag"></i> Đơn hàng gần đây
                </h2>
            </div>
            <div class="p-4">
                <c:choose>
                    <c:when test="${empty recentOrders}">
                        <p class="py-4 text-center text-muted">
                            Bạn chưa có đơn hàng nào.
                        </p>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm inline-flex items-center gap-2">
                            <i class="fa-solid fa-shop"></i> Mua sắm ngay
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div class="flex flex-col gap-3">
                            <c:forEach var="order" items="${recentOrders}" begin="0" end="4">
                                <div class="rounded-md border border-border bg-surface-light p-2">
                                    <div class="mb-2 flex items-center justify-between">
                                        <strong>
                                            <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" class="no-underline text-inherit">
                                                Đơn #${order.orderId}
                                            </a>
                                        </strong>
                                        <span class="text-sm text-muted">
                                            <fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </div>
                                    <div class="flex items-center justify-between">
                                        <div>
                                            <span class="inline-flex items-center rounded-full bg-[#e0e7ff] px-2 py-0.5 text-[0.8rem] font-semibold text-[#3730a3]">
                                                ${order.status}
                                            </span>
                                        </div>
                                        <strong class="text-danger">
                                            <fmt:formatNumber value="${order.finalAmount}" type="number"/> đ
                                        </strong>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/orders" class="btn btn-secondary btn-sm inline-flex items-center gap-2 mt-4">
                            <i class="fa-solid fa-list"></i> Xem tất cả đơn hàng
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <!-- Mã giảm giá khả dụng -->
        <section class="card overflow-hidden">
            <div class="px-4 py-4 border-b border-border">
                <h2 class="m-0 text-lg">
                    <i class="fa-solid fa-ticket"></i> Mã giảm giá
                </h2>
            </div>
            <div class="p-4">
                <c:choose>
                    <c:when test="${empty activeVouchers}">
                        <p class="py-4 text-center text-muted">
                            Hiện không có mã giảm giá nào.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="flex flex-col gap-3">
                            <c:forEach var="voucher" items="${activeVouchers}" begin="0" end="4">
                                <div class="rounded-md border-2 border-primary bg-gradient-to-br from-[#ede9fe] to-[#fce7f3] p-3">
                                    <div class="mb-2 flex items-start justify-between">
                                        <div>
                                            <strong class="text-[1.1rem] text-primary">
                                                ${voucher.code}
                                            </strong>
                                            <br>
                                            <span class="text-sm text-muted">
                                                <c:choose>
                                                    <c:when test="${voucher.discountType == 'PERCENT'}">
                                                        Giảm ${voucher.discountValue}%
                                                    </c:when>
                                                    <c:otherwise>
                                                        Giảm <fmt:formatNumber value="${voucher.discountValue}" type="number"/> đ
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <c:if test="${not empty voucher.validUntil}">
                                            <span class="inline-flex items-center rounded-full bg-[#fee2e2] px-2 py-1 text-[0.8rem] font-semibold text-[#991b1b]">
                                                HSD: <fmt:formatDate value="${voucher.validUntil}" pattern="dd/MM"/>
                                            </span>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>

    <!-- Quick links -->
    <section class="mt-6 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
        <a href="${pageContext.request.contextPath}/products" class="card text-inherit no-underline p-4 text-center transition-transform duration-200 hover:-translate-y-0.5">
            <i class="fa-solid fa-shop mb-2 text-2xl text-primary"></i>
            <h3 class="m-0 text-[0.95rem]">Mua sắm</h3>
        </a>
        <a href="${pageContext.request.contextPath}/orders" class="card text-inherit no-underline p-4 text-center transition-transform duration-200 hover:-translate-y-0.5">
            <i class="fa-solid fa-box mb-2 text-2xl text-[#8b5cf6]"></i>
            <h3 class="m-0 text-[0.95rem]">Đơn hàng</h3>
        </a>
        <a href="${pageContext.request.contextPath}/profile" class="card text-inherit no-underline p-4 text-center transition-transform duration-200 hover:-translate-y-0.5">
            <i class="fa-solid fa-user mb-2 text-2xl text-[#06b6d4]"></i>
            <h3 class="m-0 text-[0.95rem]">Hồ sơ</h3>
        </a>
        <a href="${pageContext.request.contextPath}/customer/shop-apply" class="card text-inherit no-underline p-4 text-center transition-transform duration-200 hover:-translate-y-0.5">
            <i class="fa-solid fa-store mb-2 text-2xl text-[#10b981]"></i>
            <h3 class="m-0 text-[0.95rem]">Mở cửa hàng</h3>
        </a>
    </section>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
