<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đối soát Thanh toán - Admin MetaFruit</title>
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
        .status-PENDING { background: #fef3c7; color: #92400e; }
        .status-PAID { background: #dcfce7; color: #166534; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="settlements"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Đối soát Thanh toán</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <div class="admin-panel">
                    <form action="${pageContext.request.contextPath}/admin/settlements" method="get" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;">Danh sách Đối soát</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <select name="status" class="form-select" style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                                <option value="">Tất cả trạng thái</option>
                                <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán (PENDING)</option>
                                <option value="PAID" ${paramStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán (PAID)</option>
                            </select>
                            <button type="submit" class="btn btn-secondary btn-sm"><i class="fa-solid fa-filter"></i> Lọc</button>
                        </div>
                    </form>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã ĐS</th>
                                    <th>Cửa hàng (ID)</th>
                                    <th>Kỳ đối soát</th>
                                    <th>Doanh thu gộp</th>
                                    <th>Phí sàn</th>
                                    <th>Hoàn tiền</th>
                                    <th>Số tiền thực nhận</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty settlementList}">
                                        <tr><td colspan="9" class="text-center text-muted">Không có kỳ đối soát nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="s" items="${settlementList}">
                                            <tr>
                                                <td><strong>#${s.settlementId}</strong></td>
                                                <td>Shop #${s.ownerId}</td>
                                                <td><fmt:formatDate value="${s.periodStartAsDate}" pattern="dd/MM/yyyy"/> - <fmt:formatDate value="${s.periodEndAsDate}" pattern="dd/MM/yyyy"/></td>
                                                <td><fmt:formatNumber value="${s.grossAmount}" type="number"/> đ</td>
                                                <td class="text-danger">-<fmt:formatNumber value="${s.platformFeeAmount}" type="number"/> đ</td>
                                                <td class="text-danger">-<fmt:formatNumber value="${s.refundAmount}" type="number"/> đ</td>
                                                <td class="text-success fw-bold"><fmt:formatNumber value="${s.netAmount}" type="number"/> đ</td>
                                                <td>
                                                    <span class="status-badge status-${s.status}">
                                                        ${s.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:if test="${s.status == 'PENDING'}">
                                                        <form action="${pageContext.request.contextPath}/admin/settlements" method="post" style="display:inline-block;" onsubmit="return confirm('Xác nhận đã chuyển khoản số tiền này cho Shop?');">
                                                            <input type="hidden" name="action" value="markPaid">
                                                            <input type="hidden" name="settlementId" value="${s.settlementId}">
                                                            <button type="submit" class="btn btn-sm btn-success" title="Xác nhận đã thanh toán"><i class="fa-solid fa-check"></i> Thanh toán</button>
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
