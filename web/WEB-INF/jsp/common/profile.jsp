<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="title" value="Hồ sơ cá nhân" />
</jsp:include>

<main class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <h2 class="mb-4">Thông tin tài khoản</h2>
            
            <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

            <div class="card mb-4">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">Cập nhật hồ sơ</h5>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/profile" method="post">
                        <input type="hidden" name="action" value="updateProfile">
                        
                        <div class="mb-3">
                            <label for="email" class="form-label">Email (Tên đăng nhập)</label>
                            <input type="email" class="form-control" id="email" value="${user.email}" disabled>
                            <div class="form-text">Bạn không thể thay đổi email đã đăng ký.</div>
                        </div>

                        <div class="mb-3">
                            <label for="role" class="form-label">Vai trò</label>
                            <input type="text" class="form-control" id="role" value="${user.role}" disabled>
                        </div>

                        <div class="mb-3">
                            <label for="fullName" class="form-label">Họ và tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="fullName" name="fullName" value="${user.fullName}" required>
                        </div>

                        <div class="mb-3">
                            <label for="phone" class="form-label">Số điện thoại</label>
                            <input type="text" class="form-control" id="phone" name="phone" value="${user.phone}">
                        </div>

                        <button type="submit" class="btn btn-primary">Lưu thông tin cá nhân</button>
                    </form>
                </div>
            </div>

            <div class="card">
                <div class="card-header bg-success text-white">
                    <h5 class="mb-0">Sổ địa chỉ</h5>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/profile" method="post">
                        <input type="hidden" name="action" value="updateAddress">
                        
                        <div class="mb-3">
                            <label for="userAddress" class="form-label">Địa chỉ giao hàng mặc định</label>
                            <textarea class="form-control" id="userAddress" name="userAddress" rows="3" placeholder="Ví dụ: Số 123 Đường ABC, Phường X, Quận Y, TP Z">${user.userAddress}</textarea>
                            <div class="form-text">Địa chỉ này sẽ được dùng mặc định khi bạn đặt hàng hoặc giao nhận.</div>
                        </div>

                        <button type="submit" class="btn btn-success">Cập nhật địa chỉ</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
