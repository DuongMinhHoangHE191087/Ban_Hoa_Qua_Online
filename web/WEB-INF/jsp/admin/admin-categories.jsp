<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh mục sản phẩm - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    
    <style>
        .category-status-badge {
            padding: 4px 10px;
            border-radius: var(--radius-full);
            font-size: 0.75rem;
            font-weight: 700;
        }
        .status-ACTIVE { background: #dcfce7; color: #166534; }
        .status-INACTIVE { background: #f3f4f6; color: #4b5563; }
        
        .table-actions {
            display: flex; gap: var(--space-2);
        }
        
        /* Modal Styles */
        .modal {
            display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.5); align-items: center; justify-content: center;
        }
        .modal-content {
            background-color: white; padding: 24px; border-radius: var(--radius-lg); width: 400px;
            box-shadow: var(--shadow-lg);
        }
        .modal-header {
            display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
        }
        .modal-header h3 { margin: 0; font-size: 1.25rem; }
        .close-btn { cursor: pointer; font-size: 1.5rem; color: #6b7280; border: none; background: none; }
        
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: 500; }
        .form-control { width: 100%; padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-md); }
    </style>
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="categories"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Quản lý Danh mục</h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
                
                <div class="admin-panel">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4);">
                        <h2 style="font-size: var(--font-size-lg); margin:0;">Danh sách Danh mục</h2>
                        <button class="btn btn-primary btn-sm" onclick="openModal('addModal')"><i class="fa-solid fa-plus"></i> Thêm mới</button>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Tên Danh mục</th>
                                    <th>Slug</th>
                                    <th>Thứ tự</th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty categories}">
                                        <tr><td colspan="6" class="text-center text-muted">Không có danh mục nào.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${categories}">
                                            <tr>
                                                <td>#${c.categoryId}</td>
                                                <td style="font-weight: 500;">${c.name}</td>
                                                <td>${c.slug}</td>
                                                <td>${c.displayOrder}</td>
                                                <td>
                                                    <span class="category-status-badge ${c.getIsActive() ? 'status-ACTIVE' : 'status-INACTIVE'}">
                                                        ${c.getIsActive() ? 'Hiện' : 'Ẩn'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="table-actions">
                                                        <button class="btn btn-sm btn-secondary" onclick="openEditModal(${c.categoryId}, '${c.name}', '${c.slug}', ${c.displayOrder}, ${c.getIsActive()})">
                                                            <i class="fa-solid fa-pen"></i> Sửa
                                                        </button>
                                                        
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/categories" style="display:inline;">
                                                            <input type="hidden" name="action" value="toggle">
                                                            <input type="hidden" name="categoryId" value="${c.categoryId}">
                                                            <button type="submit" class="btn btn-sm ${c.getIsActive() ? 'btn-danger' : 'btn-success'}">
                                                                <i class="fa-solid ${c.getIsActive() ? 'fa-eye-slash' : 'fa-eye'}"></i>
                                                            </button>
                                                        </form>
                                                        
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/categories" style="display:inline;" onsubmit="return confirm('Bạn có chắc chắn muốn xóa danh mục này? Chỉ có thể xóa nếu không có sản phẩm nào đang dùng.');">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="categoryId" value="${c.categoryId}">
                                                            <button type="submit" class="btn btn-sm btn-danger">
                                                                <i class="fa-solid fa-trash"></i>
                                                            </button>
                                                        </form>
                                                    </div>
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

    <!-- Add Modal -->
    <div id="addModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Thêm danh mục mới</h3>
                <button class="close-btn" onclick="closeModal('addModal')">&times;</button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/categories">
                <input type="hidden" name="action" value="create">
                <div class="form-group">
                    <label>Tên danh mục</label>
                    <input type="text" name="name" class="form-control" required onkeyup="generateSlug(this.value, 'addSlug')">
                </div>
                <div class="form-group">
                    <label>Slug (URL thân thiện)</label>
                    <input type="text" name="slug" id="addSlug" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Thứ tự hiển thị</label>
                    <input type="number" name="displayOrder" class="form-control" value="0" required>
                </div>
                <div class="form-group" style="display:flex; align-items:center; gap: 8px;">
                    <input type="checkbox" name="isActive" id="addActive" checked>
                    <label for="addActive" style="margin:0;">Hiển thị (Active)</label>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">Lưu danh mục</button>
            </form>
        </div>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Chỉnh sửa danh mục</h3>
                <button class="close-btn" onclick="closeModal('editModal')">&times;</button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/categories">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="categoryId" id="editCategoryId">
                
                <div class="form-group">
                    <label>Tên danh mục</label>
                    <input type="text" name="name" id="editName" class="form-control" required onkeyup="generateSlug(this.value, 'editSlug')">
                </div>
                <div class="form-group">
                    <label>Slug</label>
                    <input type="text" name="slug" id="editSlug" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Thứ tự hiển thị</label>
                    <input type="number" name="displayOrder" id="editOrder" class="form-control" required>
                </div>
                <div class="form-group" style="display:flex; align-items:center; gap: 8px;">
                    <input type="checkbox" name="isActive" id="editActive">
                    <label for="editActive" style="margin:0;">Hiển thị (Active)</label>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">Cập nhật</button>
            </form>
        </div>
    </div>

    <script>
        function openModal(id) {
            document.getElementById(id).style.display = 'flex';
        }
        function closeModal(id) {
            document.getElementById(id).style.display = 'none';
        }
        function openEditModal(id, name, slug, order, isActive) {
            document.getElementById('editCategoryId').value = id;
            document.getElementById('editName').value = name;
            document.getElementById('editSlug').value = slug;
            document.getElementById('editOrder').value = order;
            document.getElementById('editActive').checked = isActive;
            openModal('editModal');
        }

        // Tạo slug tự động từ tiếng việt
        function generateSlug(text, targetId) {
            let slug = text.toLowerCase();
            slug = slug.replace(/á|à|ả|ạ|ã|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ/gi, 'a');
            slug = slug.replace(/é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ/gi, 'e');
            slug = slug.replace(/i|í|ì|ỉ|ĩ|ị/gi, 'i');
            slug = slug.replace(/ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ/gi, 'o');
            slug = slug.replace(/ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự/gi, 'u');
            slug = slug.replace(/ý|ỳ|ỷ|ỹ|ỵ/gi, 'y');
            slug = slug.replace(/đ/gi, 'd');
            slug = slug.replace(/[^a-z0-9 -]/g, ''); 
            slug = slug.replace(/\s+/g, '-'); 
            slug = slug.replace(/-+/g, '-');
            document.getElementById(targetId).value = slug;
        }

        // Close modal when clicking outside
        window.onclick = function(event) {
            if (event.target == document.getElementById('addModal')) {
                closeModal('addModal');
            }
            if (event.target == document.getElementById('editModal')) {
                closeModal('editModal');
            }
        }
    </script>
</body>
</html>
