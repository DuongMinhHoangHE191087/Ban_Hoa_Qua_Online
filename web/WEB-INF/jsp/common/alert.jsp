<%-- alert.jsp — Hiển thị flash message từ session (PRG pattern).
     Include trong header.jsp để tự động hiển thị mọi trang.
     Tự động xóa flash sau khi render.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:if test="${not empty sessionScope.flashMsg}">
    <div class="alert alert-${sessionScope.flashType}" role="alert">
        <c:out value="${sessionScope.flashMsg}"/>
        <button class="alert__close" onclick="this.parentElement.remove()">✕</button>
    </div>
    <c:remove var="flashMsg"  scope="session"/>
    <c:remove var="flashType" scope="session"/>
</c:if>
