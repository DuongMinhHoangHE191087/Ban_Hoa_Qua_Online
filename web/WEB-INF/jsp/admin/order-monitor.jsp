<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Monitor Đơn hàng - Admin MetaFruit</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="orders"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Monitor đơn hàng</h1>
            </header>

            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <div class="admin-panel">
                    <form action="${pageContext.request.contextPath}/admin/order-monitor" method="get" class="flex flex-wrap items-center justify-between gap-2 mb-4">
                        <h2 class="m-0 text-lg">Giám sát đơn hàng</h2>
                        <div class="search-box flex flex-wrap gap-2">
                            <select name="status" class="form-select px-3 py-1.5 border border-border rounded-md">
                                <option value="">Tất cả trạng thái</option>
                                <option value="PENDING_PAYMENT" ${statusFilter == 'PENDING_PAYMENT' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="PENDING" ${statusFilter == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                                <option value="PROCESSING" ${statusFilter == 'PROCESSING' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="SHIPPED" ${statusFilter == 'SHIPPED' ? 'selected' : ''}>Đang giao</option>
                                <option value="DELIVERED" ${statusFilter == 'DELIVERED' ? 'selected' : ''}>Đã giao</option>
                                <option value="CANCELLED" ${statusFilter == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                            </select>
                            <select name="paymentMethod" class="form-select px-3 py-1.5 border border-border rounded-md">
                                <option value="">Tất cả phương thức TT</option>
                                <option value="COD" ${paymentMethod == 'COD' ? 'selected' : ''}>COD</option>
                                <option value="BANK_TRANSFER" ${paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Chuyển khoản</option>
                            </select>
                            <select name="paymentStatus" class="form-select px-3 py-1.5 border border-border rounded-md">
                                <option value="">Tất cả trạng thái TT</option>
                                <option value="PENDING" ${paymentStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="PAID" ${paymentStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
                                <option value="FAILED" ${paymentStatus == 'FAILED' ? 'selected' : ''}>Thất bại</option>
                            </select>
                            <button type="submit" class="btn btn-secondary btn-sm"><i class="fa-solid fa-filter"></i> Lọc</button>
                        </div>
                    </form>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã Đơn</th>
                                    <th>Ngày đặt</th>
                                    <th>Khách hàng</th>
                                    <th>Cửa hàng</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Phương thức TT</th>
                                    <th>TT Thanh toán</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty pagedResult.items}">
                                        <tr><td colspan="8" class="text-center text-muted">Không có đơn hàng nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="order" items="${pagedResult.items}">
                                            <tr>
                                                <td><strong><a href="${pageContext.request.contextPath}/admin/orders?orderId=${order.orderId}" title="Xem chi tiết">#${order.orderId}</a></strong></td>
                                                <td><fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td>
                                                    <div class="font-semibold">${order.customerName}</div>
                                                </td>
                                                <td>
                                                    <div class="font-semibold">${order.shopName}</div>
                                                    <div class="text-[11px] text-txt-3">Chủ: ${order.ownerName}</div>
                                                </td>
                                                <td class="text-danger fw-bold"><fmt:formatNumber value="${order.finalAmount}" type="number"/> đ</td>
                                                <td>
                                                    <span class="status-badge status-${order.status}">
                                                        ${order.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${order.paymentMethod == 'COD'}">
                                                            <span class="status-badge payment-COD">COD</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="status-badge payment-BANK_TRANSFER">Chuyển khoản</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${order.paymentStatus == 'PAID'}">
                                                            <span class="status-badge status-badge-approved">Đã TT</span>
                                                        </c:when>
                                                        <c:when test="${order.paymentStatus == 'PENDING'}">
                                                            <span class="status-badge status-badge-pending">Chờ TT</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="status-badge status-badge-rejected">Thất bại</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${pagedResult.totalPages > 1}">
                        <div class="flex justify-center mt-4">
                            <ft:pagination current="${pagedResult.currentPage}" total="${pagedResult.totalPages}" baseUrl="?status=${fn:escapeXml(statusFilter)}&paymentMethod=${fn:escapeXml(paymentMethod)}&paymentStatus=${fn:escapeXml(paymentStatus)}" />
                        </div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
