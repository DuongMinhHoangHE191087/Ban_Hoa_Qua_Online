<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Hồ sơ cửa hàng"/>
</jsp:include>

<main class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <h2 class="mb-4">Thông tin cửa hàng</h2>
            
            <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

            <div class="card mb-4">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0">Cập nhật hồ sơ cửa hàng</h5>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/shop/profile" method="post">
                        
                        <div class="mb-3">
                            <label for="shopName" class="form-label">Tên cửa hàng <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="shopName" name="shopName" value="${shopProfile.shopName}" required>
                        </div>

                        <div class="mb-3">
                            <label for="shopDescription" class="form-label">Mô tả cửa hàng</label>
                            <textarea class="form-control" id="shopDescription" name="shopDescription" rows="4">${shopProfile.shopDescription}</textarea>
                        </div>

                        <div class="mb-3">
                            <label for="preferredCategories" class="form-label">Loại trái cây kinh doanh chính</label>
                            <input type="text" class="form-control" id="preferredCategories" name="preferredCategories" value="${shopProfile.preferredCategories}" placeholder="VD: Trái cây nhập khẩu, Trái cây hữu cơ...">
                        </div>

                        <div class="mb-3">
                            <label for="deliveryAddress" class="form-label">Địa chỉ lấy hàng mặc định</label>
                            <textarea class="form-control" id="deliveryAddress" name="deliveryAddress" rows="3">${shopProfile.deliveryAddress}</textarea>
                            <div class="form-text">Địa chỉ này sẽ được cung cấp cho nhân viên giao hàng đến lấy hàng.</div>
                        </div>

                        <button type="submit" class="btn btn-primary">Lưu hồ sơ cửa hàng</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
