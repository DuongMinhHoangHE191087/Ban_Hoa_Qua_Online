<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Giám sát Đơn hàng - Admin MetaFruit</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-block;
        }
        .status-PENDING_PAYMENT, .status-PENDING { background: #fef3c7; color: #92400e; }
        .status-PAID, .status-PROCESSING { background: #e0e7ff; color: #3730a3; }
        .status-SHIPPED { background: #dcfce7; color: #166534; }
        .status-DELIVERED { background: #dcfce7; color: #166534; }
        .status-CANCELLED { background: #fee2e2; color: #991b1b; }
        
        .action-form { display: inline-block; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="orders"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Giám sát Đơn hàng toàn sàn</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <div class="admin-panel">
                    <form action="${pageContext.request.contextPath}/admin/orders" method="get" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;">Tất cả đơn hàng</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <select name="status" class="form-select" style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                                <option value="">Tất cả trạng thái</option>
                                <option value="PENDING_PAYMENT" ${paramStatus == 'PENDING_PAYMENT' ? 'selected' : ''}>Chờ thanh toán</option>
                                <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                                <option value="PROCESSING" ${paramStatus == 'PROCESSING' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="SHIPPED" ${paramStatus == 'SHIPPED' ? 'selected' : ''}>Đang giao</option>
                                <option value="DELIVERED" ${paramStatus == 'DELIVERED' ? 'selected' : ''}>Đã giao</option>
                                <option value="CANCELLED" ${paramStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
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
                                    <th>Khách hàng (ID)</th>
                                    <th>Cửa hàng (ID)</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty orderList}">
                                        <tr><td colspan="7" class="text-center text-muted">Không có đơn hàng nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="order" items="${orderList}">
                                            <tr>
                                                <td><strong>#${order.orderId}</strong></td>
                                                <td><fmt:formatDate value="${order.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td>User #${order.customerId}</td>
                                                <td>Shop #${order.ownerId}</td>
                                                <td class="text-danger fw-bold"><fmt:formatNumber value="${order.finalAmount}" type="number"/> đ</td>
                                                <td>
                                                    <span class="status-badge status-${order.status}">
                                                        ${order.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:if test="${order.status != 'CANCELLED' && order.status != 'DELIVERED'}">
                                                        <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="action-form" onsubmit="return confirm('Bạn có chắc chắn muốn HỦY đơn hàng này?');">
                                                            <input type="hidden" name="action" value="cancel">
                                                            <input type="hidden" name="orderId" value="${order.orderId}">
                                                            <input type="hidden" name="reason" value="Vi phạm chính sách">
                                                            <button type="submit" class="btn btn-sm btn-danger" title="Hủy đơn hàng"><i class="fa-solid fa-ban"></i> Hủy</button>
                                                        </form>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div style="display: flex; justify-content: center; margin-top: var(--space-4); gap: 5px;">
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <a href="?page=${i}&status=${paramStatus}" 
                                   class="btn btn-sm ${i == currentPage ? 'btn-primary' : 'btn-secondary'}">${i}</a>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
