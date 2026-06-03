<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Gửi Thông báo - Admin MetaFruit</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="notifications"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Gửi Thông báo Khuyến mãi / Hệ thống</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

                <!-- Form Gửi thông báo -->
                <div class="admin-panel" style="margin-bottom: var(--space-6);">
                    <h2 style="font-size: var(--font-size-lg); margin-bottom: var(--space-4);">Soạn Thông báo mới</h2>
                    
                    <form action="${pageContext.request.contextPath}/admin/notifications" method="post" style="max-width: 600px;">
                        <div class="form-group">
                            <label class="form-label" for="target">Gửi tới Nhóm đối tượng <span class="text-danger">*</span></label>
                            <select id="target" name="target" class="form-select" required>
                                <option value="ALL">Tất cả Người dùng</option>
                                <option value="CUSTOMER">Chỉ Khách hàng (CUSTOMER)</option>
                                <option value="SHOP_OWNER">Chỉ Cửa hàng (SHOP_OWNER)</option>
                                <option value="DELIVERY">Chỉ Tài xế (DELIVERY)</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label" for="title">Tiêu đề thông báo <span class="text-danger">*</span></label>
                            <input type="text" id="title" name="title" class="form-control" required placeholder="Nhập tiêu đề (VD: Giảm giá 50% toàn bộ trái cây!)">
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label" for="message">Nội dung chi tiết <span class="text-danger">*</span></label>
                            <textarea id="message" name="message" class="form-control" rows="4" required placeholder="Nội dung khuyến mãi, thông báo bảo trì, ..."></textarea>
                        </div>
                        
                        <div class="form-actions" style="margin-top: var(--space-4);">
                            <button type="submit" class="btn btn-primary" onclick="return confirm('Bạn có chắc muốn gửi thông báo này tới nhóm đã chọn? Hành động này không thể hoàn tác.');">
                                <i class="fa-solid fa-paper-plane"></i> Gửi Thông Báo Hàng Loạt
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Lịch sử Thông báo -->
                <div class="admin-panel">
                    <h2 style="font-size: var(--font-size-lg); margin-bottom: var(--space-4);">Lịch sử các Thông báo Hệ thống</h2>
                    
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Thời gian gửi</th>
                                    <th>Tiêu đề</th>
                                    <th>Nội dung</th>
                                    <th>Người nhận (ID)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty notificationList}">
                                        <tr><td colspan="5" class="text-center text-muted">Chưa có thông báo hệ thống nào được gửi.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="n" items="${notificationList}">
                                            <tr>
                                                <td>#${n.notificationId}</td>
                                                <td><fmt:formatDate value="${n.createdAtAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td><strong>${n.title}</strong></td>
                                                <td style="max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${n.message}">${n.message}</td>
                                                <td>User #${n.userId}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
                
            </div>
        </main>
    </div>
</body>
</html>
