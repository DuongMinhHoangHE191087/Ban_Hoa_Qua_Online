<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Yêu cầu Đổi trả - Admin MetaFruit</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
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
        .status-APPROVED { background: #dcfce7; color: #166534; }
        .status-REJECTED { background: #fee2e2; color: #991b1b; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="refunds"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Xử lý Yêu cầu Đổi trả / Hoàn tiền</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <div class="admin-panel">
                    <form action="${pageContext.request.contextPath}/admin/refunds" method="get" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;">Danh sách yêu cầu</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <select name="status" class="form-select" style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                                <option value="">Tất cả</option>
                                <option value="PENDING" ${paramStatus == 'PENDING' ? 'selected' : ''}>Chờ duyệt (PENDING)</option>
                                <option value="APPROVED" ${paramStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt (APPROVED)</option>
                                <option value="REJECTED" ${paramStatus == 'REJECTED' ? 'selected' : ''}>Đã từ chối (REJECTED)</option>
                            </select>
                            <button type="submit" class="btn btn-secondary btn-sm"><i class="fa-solid fa-filter"></i> Lọc</button>
                        </div>
                    </form>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Mã YC</th>
                                    <th>Mã Đơn</th>
                                    <th>Khách hàng</th>
                                    <th>Lý do</th>
                                    <th>Chi tiết</th>
                                    <th>Số tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty requestList}">
                                        <tr><td colspan="8" class="text-center text-muted">Không có yêu cầu đổi trả nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="r" items="${requestList}">
                                            <tr>
                                                <td><strong>#${r.returnRequestId}</strong></td>
                                                <td>Đơn #${r.orderId}</td>
                                                <td>User #${r.customerId}</td>
                                                <td>${r.reasonCode}</td>
                                                <td style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${r.description}">
                                                    ${r.description}
                                                </td>
                                                <td class="text-danger fw-bold"><fmt:formatNumber value="${r.refundAmount}" type="number"/> đ</td>
                                                <td>
                                                    <span class="status-badge status-${r.status}">
                                                        ${r.status}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:if test="${r.status == 'PENDING' || r.status == 'REQUESTED'}">
                                                        <div style="display: flex; gap: 5px;">
                                                            <!-- Approve Form -->
                                                            <form action="${pageContext.request.contextPath}/admin/refunds" method="post" onsubmit="return confirm('Chấp nhận hoàn tiền cho đơn này?');">
                                                                <input type="hidden" name="action" value="approve">
                                                                <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                                <input type="hidden" name="orderId" value="${r.orderId}">
                                                                <input type="hidden" name="decisionReason" value="Hợp lệ">
                                                                <button type="submit" class="btn btn-sm btn-success" title="Chấp nhận"><i class="fa-solid fa-check"></i></button>
                                                            </form>
                                                            <!-- Reject Form -->
                                                            <form action="${pageContext.request.contextPath}/admin/refunds" method="post" onsubmit="return confirm('Từ chối hoàn tiền?');">
                                                                <input type="hidden" name="action" value="reject">
                                                                <input type="hidden" name="requestId" value="${r.returnRequestId}">
                                                                <input type="hidden" name="orderId" value="${r.orderId}">
                                                                <input type="hidden" name="decisionReason" value="Không hợp lệ">
                                                                <button type="submit" class="btn btn-sm btn-danger" title="Từ chối"><i class="fa-solid fa-xmark"></i></button>
                                                            </form>
                                                        </div>
                                                    </c:if>
                                                    <c:if test="${r.status != 'PENDING' && r.status != 'REQUESTED'}">
                                                        <span class="text-muted" style="font-size: 0.8rem;">Đã xử lý: ${r.decisionReason}</span>
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
