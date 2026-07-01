
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
<div class="error-page container mx-auto max-w-[600px] px-4 py-16 text-center">
    <div class="mb-4 text-8xl leading-none text-primary">⚠️</div>
    <h1 class="mb-2 text-3xl font-extrabold text-txt">
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404 or requestScope['jakarta.servlet.error.status_code'] == 404}">404 — Không tìm thấy trang</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403 or requestScope['jakarta.servlet.error.status_code'] == 403}">403 — Không có quyền truy cập</c:when>
            <c:otherwise>500 — Lỗi máy chủ</c:otherwise>
        </c:choose>
    </h1>
    <p class="mb-6 text-lg text-txt-2">
        Xin lỗi, hệ thống đã gặp sự cố không mong muốn hoặc trang bạn đang tìm kiếm không tồn tại.
    </p>
    <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Quay về trang chủ</a>
</div>
</body>
</html>
