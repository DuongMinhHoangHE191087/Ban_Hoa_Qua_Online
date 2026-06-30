<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- alert.jsp — Hiển thị flash message từ session (PRG pattern).
     Include trong header.jsp để tự động hiển thị mọi trang.
     Tự động xóa flash sau khi render.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:if test="${not empty sessionScope.flashMsg}">
    <div id="flash-alert" class="fixed top-24 left-1/2 transform -translate-x-1/2 z-[9999] w-11/12 max-w-lg flex items-center gap-3 p-4 rounded-xl border shadow-2xl text-sm font-semibold
         ${sessionScope.flashType == 'error' ? 'bg-red-50 border-red-500 text-red-800' : 
           (sessionScope.flashType == 'warning' ? 'bg-amber-50 border-amber-500 text-amber-800' : 'bg-emerald-50 border-emerald-500 text-emerald-800')}" role="alert">
        <i class="fa-solid ${sessionScope.flashType == 'error' ? 'fa-circle-exclamation text-red-600' : 
                             (sessionScope.flashType == 'warning' ? 'fa-triangle-exclamation text-amber-600' : 'fa-circle-check text-emerald-600')}"></i>
        <span class="flex-1"><c:out value="${sessionScope.flashMsg}"/></span>
        <button onclick="document.getElementById('flash-alert').remove()" class="appearance-none border-0 bg-transparent p-0 cursor-pointer opacity-60 hover:opacity-100 transition-opacity">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
    <script>
        setTimeout(() => {
            const el = document.getElementById('flash-alert');
            if(el) el.remove();
        }, 5000);
    </script>
    <c:remove var="flashMsg"  scope="session"/>
    <c:remove var="flashType" scope="session"/>
</c:if>
