<%-- header.jsp — Include vào đầu mỗi JSP page.
     Khai báo taglib, set content type, include alert và navbar.

     CÁCH DÙNG:
       <jsp:include page="/WEB-INF/jsp/common/header.jsp">
           <jsp:param name="pageTitle" value="Tên trang"/>
       </jsp:include>
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tags/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value=""/> — FruitMkt</title>

    <!-- CSS -->
    <link rel="stylesheet" href="/assets/css/main.css">
    <%-- Thêm CSS trang cụ thể nếu cần: --%>
    <%-- <link rel="stylesheet" href="/assets/css/pages/home.css"> --%>
</head>
<body>
    <%-- Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/navbar.jsp"/>

    <%-- Flash alert (PRG pattern) --%>
    <jsp:include page="/WEB-INF/jsp/common/alert.jsp"/>

    <main class="main-content">
