<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tính năng sắp ra mắt – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout flex h-screen overflow-hidden">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp" />

    <%-- Main --%>
    <main class="admin-main flex-1 overflow-y-auto p-6 md:p-8 animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex items-center justify-between bg-surface border border-border p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Tính Năng Đang Phát Triển</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Phần quản trị này hiện đang được hoàn thiện kỹ thuật.</p>
            </div>
        </div>

        <%-- Coming Soon Panel --%>
        <div class="glass-card max-w-xl mx-auto p-12 text-center flex flex-col items-center justify-center shadow-card mt-12">
            <div class="w-20 h-20 rounded-full bg-primary-lt text-primary flex items-center justify-center text-4xl mb-6 shadow-inner animate-pulse">
                <i class="fa-solid fa-person-digging"></i>
            </div>
            
            <h2 class="text-lg md:text-xl font-black text-txt mb-3">Phase 4: Coming Soon</h2>
            
            <p class="text-xs md:text-sm text-txt-2 leading-relaxed max-w-sm mb-8">
                Tính năng này thuộc về lộ trình **Phase 4** của dự án bán hàng nông sản MetaFruit và hiện đang trong quá trình xây dựng. Vui lòng quay lại sau!
            </p>
            
            <a href="${pageContext.request.contextPath}/admin/dashboard" 
               class="px-6 py-3 bg-primary hover:bg-primary-dk text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Quay lại Tổng quan
            </a>
        </div>

    </main>
</div>
</body>
</html>
