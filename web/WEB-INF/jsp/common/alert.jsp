<%-- alert.jsp — Hiển thị flash message từ session (PRG pattern).
     Include trong header.jsp để tự động hiển thị mọi trang.
     Tự động xóa flash sau khi render.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:if test="">
    <div class="alert alert-" role="alert">
        <c:out value=""/>
        <button class="alert__close" onclick="this.parentElement.remove()">✕</button>
    </div>
    <c:remove var="flashMsg"  scope="session"/>
    <c:remove var="flashType" scope="session"/>
</c:if>
