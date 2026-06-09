<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- error.jsp — Trang lỗi chung (403, 404, 500).
     Nhận thông tin từ Servlet container qua request attributes.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lỗi — MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
<div class="error-page container" style="text-align: center; padding: var(--space-16) var(--space-4); max-width: 600px; margin: 0 auto;">
    <div style="font-size: 5rem; line-weight: 1; color: var(--color-primary); margin-bottom: var(--space-4);">⚠️</div>
    <h1 style="font-size: var(--font-size-3xl); font-weight: 800; color: var(--color-text-primary); margin-bottom: var(--space-2);">
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404 or requestScope['jakarta.servlet.error.status_code'] == 404}">404 — Không tìm thấy trang</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403 or requestScope['jakarta.servlet.error.status_code'] == 403}">403 — Không có quyền truy cập</c:when>
            <c:otherwise>500 — Lỗi máy chủ</c:otherwise>
        </c:choose>
    </h1>
    <p style="color: var(--color-text-secondary); margin-bottom: var(--space-6); font-size: var(--font-size-lg);">
        Xin lỗi, hệ thống đã gặp sự cố không mong muốn hoặc trang bạn đang tìm kiếm không tồn tại.
    </p>
    <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Quay về trang chủ</a>
</div>
</body>
</html>
