<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Đăng nhập"/></jsp:include>
<div class="container auth-form">
    <h1>Đăng nhập</h1>
    <%-- TODO: Servlet đặt errorMsg vào request attribute khi login sai --%>
    <c:if test="">
        <div class="alert alert-error"><c:out value=""/></div>
    </c:if>
    <form action="${pageContext.request.contextPath}/auth/login" method="post">
        <input type="hidden" name="_csrf" value="">
        <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required placeholder="email@example.com">
        </div>
        <div class="form-group">
            <label for="password">Mật khẩu</label>
            <input type="password" id="password" name="password" required>
        </div>
        <label><input type="checkbox" name="rememberMe"> Ghi nhớ đăng nhập</label>
        <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/auth/forgot">Quên mật khẩu?</a> | <a href="${pageContext.request.contextPath}/auth/register">Đăng ký tài khoản</a></p>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
