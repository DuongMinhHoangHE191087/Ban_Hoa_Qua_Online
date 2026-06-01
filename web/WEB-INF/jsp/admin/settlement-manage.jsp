<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý settlement - Admin MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Core CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp" />

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <h1>Quản lý settlement</h1>
            </header>
            
            <div class="admin-content">
                <div class="admin-panel" style="text-align: center; padding: 60px 20px;">
                    <i class="fa-solid fa-person-digging" style="font-size: 4rem; color: #d1d5db; margin-bottom: 24px;"></i>
                    <h2 style="font-size: 1.5rem; font-weight: bold; color: var(--color-text); margin-bottom: 12px;">Phase 4: Coming Soon</h2>
                    <p style="color: var(--color-text-light); max-width: 400px; margin: 0 auto;">
                        Tính năng này thuộc về Phase 4 của dự án và hiện đang trong quá trình xây dựng. Vui lòng quay lại sau!
                    </p>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
