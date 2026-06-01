<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm duyệt đánh giá - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .status-VISIBLE { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .status-HIDDEN { background: #f3f4f6; color: #4b5563; border: 1px solid #d1d5db; }
        
        /* Toggle Switch CSS */
        .switch {
            position: relative; display: inline-block; width: 44px; height: 24px;
        }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider {
            position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
            background-color: #ccc; transition: .4s; border-radius: 24px;
        }
        .slider:before {
            position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px;
            background-color: white; transition: .4s; border-radius: 50%;
        }
        input:checked + .slider { background-color: var(--color-success); }
        input:checked + .slider:before { transform: translateX(20px); }
    </style>
</head>
<body>
    <div class="admin-layout">
        <!-- Sidebar -->
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="reviews"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="admin-main">
            <header class="admin-header">
                <div>
                    <h1>Kiểm Duyệt Đánh Giá</h1>
                    <p style="color: var(--color-text-light); font-size: 0.9rem;">Quản lý nội dung phản hồi từ người dùng, ẩn các đánh giá spam, không phù hợp.</p>
                </div>
            </header>

            <div class="admin-content">
                <div class="admin-panel">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;"><i class="fa-solid fa-comments"></i> Danh sách Đánh giá</h2>
                        <div class="search-box" style="display: flex; gap: var(--space-2);">
                            <input type="text" id="reviewSearch" placeholder="Tìm nội dung, user..." style="padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md);">
                            <button class="btn btn-secondary btn-sm"><i class="fa-solid fa-search"></i></button>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 25%">Sản phẩm / Người dùng</th>
                                    <th style="text-align:center;">Rating</th>
                                    <th style="width: 40%">Nội dung</th>
                                    <th style="text-align:center;">Trạng thái</th>
                                    <th style="text-align:center;">Ẩn / Hiện</th>
                                </tr>
                            </thead>
                            <tbody id="reviewTableBody">
                                <c:choose>
                                    <c:when test="${empty reviewList}">
                                        <tr><td colspan="5" class="text-center text-muted">Chưa có đánh giá nào trên hệ thống.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="review" items="${reviewList}">
                                            <tr>
                                                <td>
                                                    <strong class="searchable-text" style="color:var(--color-primary);">${review.productName}</strong><br>
                                                    <small class="searchable-text" style="color:var(--color-text-light);">Bởi: <b>${review.customerName}</b></small>
                                                </td>
                                                <td style="text-align:center; color:#f59e0b;">
                                                    <c:forEach begin="1" end="${review.rating}"><i class="fa-solid fa-star"></i></c:forEach>
                                                    <c:forEach begin="${review.rating + 1}" end="5"><i class="fa-regular fa-star"></i></c:forEach>
                                                </td>
                                                <td>
                                                    <div class="searchable-text" style="max-width:300px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${fn:escapeXml(review.reviewText)}">${review.reviewText}</div>
                                                    <c:if test="${not empty review.reviewImageUrl}">
                                                        <a href="${review.reviewImageUrl}" target="_blank" style="font-size:0.8rem; color:var(--color-primary); display:block; margin-top:4px;">
                                                            <i class="fa-solid fa-image"></i> Xem ảnh
                                                        </a>
                                                    </c:if>
                                                </td>
                                                <td style="text-align:center;" id="status-col-${review.reviewId}">
                                                    <c:choose>
                                                        <c:when test="${review.isHidden}">
                                                            <span class="status-badge status-HIDDEN"><i class="fa-solid fa-eye-slash"></i> Đã Ẩn</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="status-badge status-VISIBLE"><i class="fa-solid fa-eye"></i> Đang Hiện</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align:center;">
                                                    <label class="switch">
                                                        <input type="checkbox" onchange="toggleReviewVisibility(${review.reviewId}, !this.checked)" ${!review.isHidden ? 'checked' : ''}>
                                                        <span class="slider"></span>
                                                    </label>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        document.getElementById('reviewSearch').addEventListener('input', function(e) {
            const term = e.target.value.toLowerCase();
            document.querySelectorAll('#reviewTableBody tr').forEach(row => {
                const searchTexts = row.querySelectorAll('.searchable-text');
                if(searchTexts.length === 0) return; // empty row
                let match = false;
                searchTexts.forEach(el => {
                    if(el.textContent.toLowerCase().includes(term)) match = true;
                });
                row.style.display = match ? '' : 'none';
            });
        });

        function toggleReviewVisibility(reviewId, isHidden) {
            fetch('${pageContext.request.contextPath}/admin/reviews/visibility', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'reviewId=' + reviewId + '&isHidden=' + isHidden + '&_csrf=${sessionScope._csrfToken}'
            })
            .then(response => response.json())
            .then(data => {
                if(data.success) {
                    const statusCol = document.getElementById('status-col-' + reviewId);
                    if(isHidden) {
                        statusCol.innerHTML = '<span class="status-badge status-HIDDEN"><i class="fa-solid fa-eye-slash"></i> Đã Ẩn</span>';
                    } else {
                        statusCol.innerHTML = '<span class="status-badge status-VISIBLE"><i class="fa-solid fa-eye"></i> Đang Hiện</span>';
                    }
                    const Toast = Swal.mixin({ toast: true, position: 'bottom-end', showConfirmButton: false, timer: 2000, timerProgressBar: true });
                    Toast.fire({ icon: 'success', title: data.message });
                } else {
                    Swal.fire('Lỗi', data.message, 'error');
                    setTimeout(() => window.location.reload(), 1500);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire('Lỗi', 'Lỗi kết nối mạng.', 'error');
            });
        }
    </script>
</body>
</html>
