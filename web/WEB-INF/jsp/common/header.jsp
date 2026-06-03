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
    
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- FontAwesome Icons & Material Symbols -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

    <!-- Core CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    
    <!-- Global application state injected for AJAX cart operations -->
    <script>
        window.isLoggedIn = ${not empty sessionScope.currentUser ? 'true' : 'false'};
        window.contextPath = '${pageContext.request.contextPath}';
        window.csrfToken = '${sessionScope._csrfToken}';
    </script>
</head>
<body>
    <%-- Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp"/>

    <%-- Flash alert (PRG pattern) --%>
    <jsp:include page="/WEB-INF/jsp/common/alert.jsp"/>

    <main class="main-content">
