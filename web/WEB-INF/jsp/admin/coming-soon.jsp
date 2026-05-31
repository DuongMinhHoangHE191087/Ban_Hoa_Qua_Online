<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Coming Soon - Admin Dashboard" />
</jsp:include>

<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp" />

    <main class="admin-main">
        <header class="admin-header">
            <h1>Tính năng đang phát triển</h1>
        </header>
        
        <div class="admin-content">
            <div class="admin-panel text-center py-20">
                <i class="fa-solid fa-person-digging text-6xl text-gray-300 mb-6"></i>
                <h2 class="text-2xl font-bold text-gray-800 mb-3">Phase 4: Coming Soon</h2>
                <p class="text-gray-500 max-w-md mx-auto">
                    Tính năng này thuộc về Phase 4 của dự án và hiện đang trong quá trình xây dựng. Vui lòng quay lại sau!
                </p>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
