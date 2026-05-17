<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Đăng ký"/></jsp:include>
<div class="container auth-form">
    <h1>Đăng ký tài khoản</h1>
    <form action="${pageContext.request.contextPath}/auth/register" method="post">
        <input type="hidden" name="_csrf" value="">
        <%-- fullName, email, phone, password, confirmPassword, accountType (CUSTOMER|SHOP_OWNER) --%>
        <%-- TODO: Thêm validation error display cho từng field --%>
        <div class="form-group">
            <label for="fullName">Họ và tên *</label>
            <input type="text" id="fullName" name="fullName" required minlength="3" maxlength="100">
        </div>
        <div class="form-group">
            <label for="regEmail">Email *</label>
            <input type="email" id="regEmail" name="email" required>
        </div>
        <div class="form-group">
            <label for="phone">Số điện thoại</label>
            <input type="tel" id="phone" name="phone" maxlength="15">
        </div>
        <div class="form-group">
            <label for="regPwd">Mật khẩu * (8-64 ký tự)</label>
            <input type="password" id="regPwd" name="password" required minlength="8" maxlength="64">
        </div>
        <div class="form-group">
            <label for="confirmPwd">Xác nhận mật khẩu *</label>
            <input type="password" id="confirmPwd" name="confirmPassword" required>
        </div>
        <div class="form-group">
            <label>Loại tài khoản *</label>
            <label><input type="radio" name="accountType" value="CUSTOMER" checked> Khách hàng</label>
            <label><input type="radio" name="accountType" value="SHOP_OWNER"> Chủ shop</label>
        </div>
        <button type="submit" class="btn btn-primary btn-block">Đăng ký</button>
    </form>
    <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth/login">Đăng nhập</a></p>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
