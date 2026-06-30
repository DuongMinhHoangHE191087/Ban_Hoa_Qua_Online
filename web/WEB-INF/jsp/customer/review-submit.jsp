<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh giá sản phẩm - MetaFruit</title>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- FontAwesome -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <!-- Core CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">


</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

    <main class="container min-h-[60vh]">
        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <div class="review-container">
            <h1 class="mb-5 text-2xl">Đánh giá Sản phẩm</h1>

            <form action="${pageContext.request.contextPath}/reviews" method="POST" enctype="multipart/form-data">
                <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                <c:if test="${not empty order}">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                </c:if>
                <input type="hidden" name="orderItemId" value="${orderItemId}">
                <c:if test="${not empty action}">
                    <input type="hidden" name="action" value="${action}">
                </c:if>
                <c:if test="${review != null}">
                    <input type="hidden" name="reviewId" value="${review.reviewId}">
                    <input type="hidden" name="reviewImageUrl" value="<c:out value='${review.reviewImageUrl}'/>">
                </c:if>

                <div class="form-group">
                    <label>Chất lượng sản phẩm</label>
                    <div class="rating-stars">
                        <input type="radio" name="rating" id="star5" value="5" ${review != null && review.rating == 5 ? 'checked' : ''} required>
                        <label for="star5"><i class="fa-solid fa-star"></i></label>

                        <input type="radio" name="rating" id="star4" value="4" ${review != null && review.rating == 4 ? 'checked' : ''}>
                        <label for="star4"><i class="fa-solid fa-star"></i></label>

                        <input type="radio" name="rating" id="star3" value="3" ${review != null && review.rating == 3 ? 'checked' : ''}>
                        <label for="star3"><i class="fa-solid fa-star"></i></label>

                        <input type="radio" name="rating" id="star2" value="2" ${review != null && review.rating == 2 ? 'checked' : ''}>
                        <label for="star2"><i class="fa-solid fa-star"></i></label>

                        <input type="radio" name="rating" id="star1" value="1" ${review != null && review.rating == 1 ? 'checked' : ''}>
                        <label for="star1"><i class="fa-solid fa-star"></i></label>
                    </div>
                </div>

                <div class="form-group">
                    <label>Nội dung đánh giá</label>
                    <textarea name="reviewText" class="form-control" placeholder="Hãy chia sẻ nhận xét của bạn về sản phẩm này nhé..." required><c:out value="${review != null ? review.reviewText : ''}" /></textarea>
                </div>

                <div class="form-group">
                    <label>Hình ảnh (Không bắt buộc)</label>
                    <div class="file-upload-wrapper" onclick="document.getElementById('reviewImage').click()">
                        <i class="fa-solid fa-cloud-arrow-up mb-2.5 text-2xl text-txt-3"></i>
                        <p class="m-0 text-txt-3">Nhấn để chọn ảnh tải lên</p>
                        <input type="file" name="reviewImage" id="reviewImage" accept="image/*" class="is-hidden" onchange="previewImage(event)">
                        <c:set var="reviewImagePreviewSrc" value="#" />
                        <c:if test="${review != null && review.reviewImageUrl != null}">
                            <c:url value="/${review.reviewImageUrl}" var="reviewImagePreviewSrc" />
                        </c:if>
                        <img id="imagePreview" src="${reviewImagePreviewSrc}" alt="Preview"
                             class="${review != null && review.reviewImageUrl != null ? 'block' : 'block is-hidden'}">
                    </div>
                </div>

                <button type="submit" class="btn-submit-premium">Gửi đánh giá</button>
            </form>
        </div>
    </main>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />

    <script>
        function previewImage(event) {
            var reader = new FileReader();
            reader.onload = function(){
                var output = document.getElementById('imagePreview');
                output.src = reader.result;
                output.classList.remove('is-hidden');
                output.classList.add('block');
            };
            if(event.target.files[0]) {
                reader.readAsDataURL(event.target.files[0]);
            }
        }
    </script>
</body>
</html>
