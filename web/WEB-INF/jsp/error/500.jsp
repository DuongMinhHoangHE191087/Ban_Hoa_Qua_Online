<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Lỗi máy chủ | MetaFruit</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;800&family=Plus+Jakarta+Sans:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/error-500.css">
</head>
<body>
    <div class="particle p1"></div>
    <div class="particle p2"></div>

    <div class="container">
        <div class="error-card">
            <!-- Animated sad cracked fruit graphics -->
            <div class="fruit-container">
                <div class="orange-leaf"></div>
                <div class="sad-orange">
                    <div class="sad-eyes">
                        <div class="eye"></div>
                        <div class="eye"></div>
                    </div>
                    <div class="sad-mouth"></div>
                </div>
            </div>

            <!-- Custom User-requested Text -->
            <h1>500 — Lỗi máy chủ</h1>
            <p>Xin lỗi, đã có lỗi xảy ra. Vui lòng quay về trang chủ.</p>

            <c:choose>
                <c:when test="${not empty requestScope.logFilePath}">
                    <c:set var="resolvedLogFilePath" value="${requestScope.logFilePath}" />
                </c:when>
                <c:otherwise>
                    <c:set var="resolvedLogFilePath" value="${applicationScope.appLogFilePath}" />
                </c:otherwise>
            </c:choose>
            <div class="error-meta">
                <c:if test="${not empty requestScope.errorId}">
                    <div><strong>Mã tham chiếu:</strong> ${requestScope.errorId}</div>
                </c:if>
                <c:if test="${not empty resolvedLogFilePath}">
                    <div><strong>Log chi tiết:</strong> ${resolvedLogFilePath}</div>
                </c:if>
            </div>

            <a href="${pageContext.request.contextPath}/" class="btn-home">
                <svg viewBox="0 0 24 24">
                    <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                </svg>
                Quay về trang chủ
            </a>

            <!-- Gỡ lỗi chi tiết (chỉ hiển thị khi có Exception) -->
            <c:if test="${not empty applicationScope.appEnv and applicationScope.appEnv ne 'production' and (not empty pageContext.exception or not empty requestScope['jakarta.servlet.error.exception'])}">
                <div class="debug-panel">
                    <div class="debug-title" onclick="toggleDebug()">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 12 15 18 9"></polyline>
                        </svg>
                        Thông tin kỹ thuật (Developer Only)
                    </div>
                    <div id="debug-content" class="debug-content">
                        <strong>Thông điệp:</strong> ${requestScope['jakarta.servlet.error.message']}<br>
                        <strong>Mã tham chiếu:</strong> ${requestScope.errorId}<br>
                        <strong>Log file:</strong> ${resolvedLogFilePath}<br>
                        <strong>Exception:</strong> ${requestScope['jakarta.servlet.error.exception']}<br>
                        <strong>URI yêu cầu:</strong> ${requestScope['jakarta.servlet.error.request_uri']}<br>
                    </div>
                </div>
                <script>
                    function toggleDebug() {
                        var panel = document.getElementById('debug-content');
                        panel.classList.toggle('show');
                    }
                </script>
            </c:if>
        </div>
    </div>
</body>
</html>
