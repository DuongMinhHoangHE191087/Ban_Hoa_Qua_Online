<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Tổng quan khách hàng"/></jsp:include>

<div class="container" style="padding: var(--space-6) 0;">
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); margin-bottom: var(--space-6);">
        <!-- Đơn hàng gần đây -->
        <section class="card">
            <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border);">
                <h2 style="margin: 0; font-size: var(--font-size-lg);">
                    <i class="fa-solid fa-shopping-bag"></i> Đơn hàng gần đây
                </h2>
            </div>
            <div style="padding: var(--space-4);">
                <c:choose>
                    <c:when test="${empty recentOrders}">
                        <p style="color: var(--color-muted); text-align: center; padding: var(--space-4);">
                            Bạn chưa có đơn hàng nào.
                        </p>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-sm" style="display: inline-block;">
                            <i class="fa-solid fa-shop"></i> Mua sắm ngay
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div style="display: flex; flex-direction: column; gap: var(--space-3);">
                            <c:forEach var="order" items="${recentOrders}" begin="0" end="4">
                                <div style="padding: var(--space-2); border: 1px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-surface-light);">
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2);">
                                        <strong>
                                            <a href="${pageContext.request.contextPath}/orders?action=detail&orderId=${order.orderId}" style="color: inherit; text-decoration: none;">
                                                Đơn #${order.orderId}
                                            </a>
                                        </strong>
                                        <span style="font-size: 0.85rem; color: var(--color-muted);">
                                            <fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </div>
                                    <div style="display: flex; justify-content: space-between; align-items: center;">
                                        <div>
                                            <span style="background: #e0e7ff; color: #3730a3; padding: 2px 8px; border-radius: var(--radius-full); font-size: 0.8rem; font-weight: 600;">
                                                ${order.status}
                                            </span>
                                        </div>
                                        <strong style="color: var(--color-danger);">
                                            <fmt:formatNumber value="${order.finalAmount}" type="number"/> đ
                                        </strong>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <a href="${pageContext.request.contextPath}/orders" class="btn btn-secondary btn-sm" style="display: inline-block; margin-top: var(--space-4);">
                            <i class="fa-solid fa-list"></i> Xem tất cả đơn hàng
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <!-- Mã giảm giá khả dụng -->
        <section class="card">
            <div style="padding: var(--space-4); border-bottom: 1px solid var(--color-border);">
                <h2 style="margin: 0; font-size: var(--font-size-lg);">
                    <i class="fa-solid fa-ticket"></i> Mã giảm giá
                </h2>
            </div>
            <div style="padding: var(--space-4);">
                <c:choose>
                    <c:when test="${empty activeVouchers}">
                        <p style="color: var(--color-muted); text-align: center; padding: var(--space-4);">
                            Hiện không có mã giảm giá nào.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div style="display: flex; flex-direction: column; gap: var(--space-3);">
                            <c:forEach var="voucher" items="${activeVouchers}" begin="0" end="4">
                                <div style="padding: var(--space-3); border: 2px solid var(--color-primary); border-radius: var(--radius-md); background: linear-gradient(135deg, #ede9fe 0%, #fce7f3 100%);">
                                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-2);">
                                        <div>
                                            <strong style="font-size: 1.1rem; color: var(--color-primary);">
                                                ${voucher.code}
                                            </strong>
                                            <br>
                                            <span style="font-size: 0.9rem; color: var(--color-muted);">
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
                                            <span style="font-size: 0.8rem; background: #fee2e2; color: #991b1b; padding: 4px 8px; border-radius: var(--radius-full);">
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
    <section style="display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-3); margin-top: var(--space-6);">
        <a href="${pageContext.request.contextPath}/products" class="card" style="text-align: center; padding: var(--space-4); text-decoration: none; color: inherit; transition: transform 0.2s;">
            <i class="fa-solid fa-shop" style="font-size: 2rem; color: var(--color-primary); margin-bottom: var(--space-2);"></i>
            <h3 style="margin: 0; font-size: 0.95rem;">Mua sắm</h3>
        </a>
        <a href="${pageContext.request.contextPath}/orders" class="card" style="text-align: center; padding: var(--space-4); text-decoration: none; color: inherit; transition: transform 0.2s;">
            <i class="fa-solid fa-box" style="font-size: 2rem; color: #8b5cf6; margin-bottom: var(--space-2);"></i>
            <h3 style="margin: 0; font-size: 0.95rem;">Đơn hàng</h3>
        </a>
        <a href="${pageContext.request.contextPath}/profile" class="card" style="text-align: center; padding: var(--space-4); text-decoration: none; color: inherit; transition: transform 0.2s;">
            <i class="fa-solid fa-user" style="font-size: 2rem; color: #06b6d4; margin-bottom: var(--space-2);"></i>
            <h3 style="margin: 0; font-size: 0.95rem;">Hồ sơ</h3>
        </a>
        <a href="${pageContext.request.contextPath}/customer/shop-apply" class="card" style="text-align: center; padding: var(--space-4); text-decoration: none; color: inherit; transition: transform 0.2s;">
            <i class="fa-solid fa-store" style="font-size: 2rem; color: #10b981; margin-bottom: var(--space-2);"></i>
            <h3 style="margin: 0; font-size: 0.95rem;">Mở cửa hàng</h3>
        </a>
    </section>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
