<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Quên mật khẩu"/></jsp:include>
<div class="container auth-form">
    <h1>Quên mật khẩu</h1>
    <p>Nhập email để nhận mã đặt lại mật khẩu.</p>
    <form action="${pageContext.request.contextPath}/auth/forgot" method="post">
        <input type="hidden" name="_csrf" value="">
        <%-- Bước 1: nhập email → nhận code; Bước 2: nhập code + mật khẩu mới --%>
        <div class="form-group">
            <label for="forgotEmail">Email *</label>
            <input type="email" id="forgotEmail" name="email" required>
        </div>
        <button type="submit" class="btn btn-primary">Gửi mã xác thực</button>
    </form>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
