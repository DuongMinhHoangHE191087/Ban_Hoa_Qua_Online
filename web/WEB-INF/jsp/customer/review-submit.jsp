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
    
    <style>
        .review-container {
            max-width: 600px;
            margin: 40px auto;
            background: rgba(255, 255, 255, 0.88);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            padding: 40px 30px;
            border-radius: var(--radius-xl);
            border: 1.5px solid rgba(134, 239, 172, 0.35);
            box-shadow: 
                0 20px 40px -15px rgba(34, 197, 94, 0.08),
                0 1px 3px rgba(20, 83, 45, 0.05),
                inset 0 1px 0 rgba(255, 255, 255, 0.95);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .review-container:hover {
            transform: translateY(-2px);
            box-shadow: 
                0 30px 60px -20px rgba(34, 197, 94, 0.15),
                0 2px 8px rgba(20, 83, 45, 0.08);
            border-color: rgba(134, 239, 172, 0.55);
        }
        
        .rating-stars {
            display: flex;
            flex-direction: row-reverse;
            justify-content: flex-end;
            gap: 10px;
            margin-bottom: 20px;
        }
        .rating-stars input {
            display: none;
        }
        .rating-stars label {
            font-size: 2.5rem;
            color: #e2e8f0;
            cursor: pointer;
            transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
            text-shadow: 0 2px 4px rgba(0,0,0,0.03);
        }
        .rating-stars input:checked ~ label,
        .rating-stars label:hover,
        .rating-stars label:hover ~ label {
            color: #fbbf24;
            transform: scale(1.15) rotate(3deg);
            filter: drop-shadow(0 0 8px rgba(251, 191, 36, 0.35));
        }
        .rating-stars label:active {
            transform: scale(0.9) rotate(-3deg);
        }
        
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 700; color: var(--color-text-primary); }
        .form-control { 
            width: 100%; 
            padding: 14px; 
            border: 1px solid rgba(197, 200, 183, 0.6); 
            border-radius: var(--radius-md); 
            font-family: inherit; 
            background: rgba(255, 255, 255, 0.7);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .form-control:focus {
            border-color: #4d661c;
            background: #ffffff;
            box-shadow: 0 0 0 4px rgba(77, 102, 28, 0.12);
            outline: none;
        }
        textarea.form-control { resize: vertical; min-height: 120px; }
        
        .file-upload-wrapper {
            border: 2px dashed rgba(197, 200, 183, 0.6);
            background: rgba(255, 255, 255, 0.6);
            padding: 24px;
            text-align: center;
            border-radius: var(--radius-md);
            cursor: pointer;
            margin-bottom: 20px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .file-upload-wrapper:hover {
            border-color: #4d661c;
            background: rgba(240, 253, 244, 0.6);
            box-shadow: 0 0 15px rgba(77, 102, 28, 0.08);
        }
        #imagePreview {
            max-width: 100%;
            max-height: 200px;
            margin-top: 15px;
            display: none;
            border-radius: var(--radius-md);
            box-shadow: var(--shadow-sm);
        }
        
        .btn-submit-premium {
            background: linear-gradient(135deg, #4d661c 0%, #31694b 100%);
            box-shadow: 0 4px 15px rgba(77, 102, 28, 0.3);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            color: #fff;
            font-weight: 700;
            border: none;
            cursor: pointer;
            width: 100%;
            padding: 14px;
            border-radius: var(--radius-md);
        }
        .btn-submit-premium:hover {
            background: linear-gradient(135deg, #364e03 0%, #1e3f2d 100%);
            box-shadow: 0 8px 25px rgba(77, 102, 28, 0.45);
            transform: translateY(-1.5px);
        }
        .btn-submit-premium:active {
            transform: translateY(0);
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

    <main class="container" style="min-height: 60vh;">
        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
        
        <div class="review-container">
            <h1 style="font-size: 1.5rem; margin-bottom: 20px;">Đánh giá Sản phẩm</h1>
            
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
                        <i class="fa-solid fa-cloud-arrow-up" style="font-size: 2rem; color: var(--color-text-light); margin-bottom: 10px;"></i>
                        <p style="color: var(--color-text-light); margin: 0;">Nhấn để chọn ảnh tải lên</p>
                        <input type="file" name="reviewImage" id="reviewImage" accept="image/*" style="display: none;" onchange="previewImage(event)">
                        <c:set var="reviewImagePreviewSrc" value="#" />
                        <c:if test="${review != null && review.reviewImageUrl != null}">
                            <c:url value="/${review.reviewImageUrl}" var="reviewImagePreviewSrc" />
                        </c:if>
                        <img id="imagePreview" src="${reviewImagePreviewSrc}" alt="Preview" 
                             <c:choose>
                                 <c:when test="${review != null && review.reviewImageUrl != null}">style="display:block;"</c:when>
                                 <c:otherwise>style="display:none;"</c:otherwise>
                             </c:choose>>
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
                output.style.display = 'block';
            };
            if(event.target.files[0]) {
                reader.readAsDataURL(event.target.files[0]);
            }
        }
    </script>
</body>
</html>
