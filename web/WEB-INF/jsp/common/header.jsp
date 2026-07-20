
<%-- header.jsp — Include vào đầu mỗi JSP page.
     Khai báo taglib, set content type, include alert và navbar.

     CÁCH DÙNG:
       <jsp:include page="/WEB-INF/jsp/common/header.jsp">
           <jsp:param name="pageTitle" value="Tên trang"/>
       </jsp:include>
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${param.pageTitle}"/> — MetaFruit Premium</title>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/favicon.png">
    
    <!-- Google Fonts: Lexend -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- FontAwesome Icons & Material Symbols -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/material-symbols-outlined.css">

    <!-- Core CSS (navbar, legacy components) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

    <%-- ── Tailwind CSS — Global shared design system ────────────────────────── --%>
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">

    <!-- SweetAlert2 — premium modals -->
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <script>
        window.alert = function(message) {
            Swal.fire({
                icon: 'info', title: 'Thông báo', text: message,
                confirmButtonText: 'Đồng ý',
                confirmButtonColor: '#4D661C',
                background: '#ffffff',
                customClass: {
                    popup: 'premium-swal-popup',
                    title: 'premium-swal-title',
                    confirmButton: 'premium-swal-button'
                }
            });
        };
        window.appConfirm = function(message, options) {
            return Swal.fire(Object.assign({ icon: 'question', title: 'Xác nhận thao tác', text: String(message == null ? '' : message), showCancelButton: true, confirmButtonText: 'Xác nhận', cancelButtonText: 'Hủy', confirmButtonColor: '#4D661C', cancelButtonColor: '#E5E7EB', reverseButtons: true }, options || {})).then(function(result) { return result.isConfirmed; });
        };
    </script>

    <!-- Global app state for AJAX / cart -->
    <script>
        window.isLoggedIn  = "${sessionScope.currentUser != null}" === "true";
        window.contextPath = '${pageContext.request.contextPath}';
        window.csrfToken   = '${sessionScope._csrfToken}';
        window.getCsrfToken = function() {
            const hidden = document.querySelector('input[name="_csrf"]');
            return (window.csrfToken || (hidden ? hidden.value : '') || '').trim();
        };
    </script>
</head>
<body>
    <%-- Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp"/>

    <%-- Flash alert (PRG pattern) --%>
    <jsp:include page="/WEB-INF/jsp/common/alert.jsp"/>

    <main class="main-content">
