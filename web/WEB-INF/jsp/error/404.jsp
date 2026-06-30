<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 — Không tìm thấy trang | MetaFruit</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;800&family=Plus+Jakarta+Sans:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/error-404.css">
</head>
<body>
    <div class="particle p1"></div>
    <div class="particle p2"></div>

    <div class="container">
        <div class="error-card">
            <!-- Animated sad lost orange graphics -->
            <div class="fruit-container">
                <div class="orange-leaf"></div>
                <div class="lost-orange">
                    <div class="lost-eyes">
                        <div class="eye"><div class="pupil"></div></div>
                        <div class="eye"><div class="pupil"></div></div>
                    </div>
                    <div class="blush left"></div>
                    <div class="blush right"></div>
                    <div class="sad-mouth"></div>
                </div>
            </div>

            <!-- Custom 404 text -->
            <h1>404 — Không tìm thấy trang</h1>
            <p>Xin lỗi, đường dẫn bạn yêu cầu không tồn tại hoặc đã bị di chuyển. Vui lòng quay về trang chủ.</p>

            <a href="${pageContext.request.contextPath}/" class="btn-home">
                <svg viewBox="0 0 24 24">
                    <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                </svg>
                Quay về trang chủ
            </a>
        </div>
    </div>
</body>
</html>
