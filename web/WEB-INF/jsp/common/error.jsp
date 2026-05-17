<%-- error.jsp — Trang lỗi chung (403, 404, 500).
     Nhận thông tin từ Servlet container qua request attributes.
--%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lỗi — FruitMkt</title>
    <link rel="stylesheet" href="/assets/css/main.css">
</head>
<body>
<div class="error-page">
    <h1>
        <c:choose>
            <c:when test="">404 — Không tìm thấy trang</c:when>
            <c:when test="">403 — Không có quyền truy cập</c:when>
            <c:otherwise>500 — Lỗi máy chủ</c:otherwise>
        </c:choose>
    </h1>
    <p>Xin lỗi, đã có lỗi xảy ra. Vui lòng <a href="/">quay về trang chủ</a>.</p>
    <%-- Chỉ hiện stacktrace trong môi trường dev --%>
    <%-- <pre></pre> --%>
</div>
</body>
</html>
