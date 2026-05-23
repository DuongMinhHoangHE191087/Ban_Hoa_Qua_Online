<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Danh mục - Admin</title>
    <!-- Import CSS gốc của dự án -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=Lexend:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-sidebar { width: 260px; background: var(--color-surface); border-right: 1px solid var(--color-border); padding: var(--space-4); }
        .admin-main { flex: 1; padding: var(--space-6); background: var(--color-bg); }
        .table-container { background: var(--color-surface); padding: var(--space-5); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
        .data-table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { padding: var(--space-3); text-align: left; border-bottom: 1px solid var(--color-border); }
        .data-table th { background: var(--color-bg); color: var(--color-text-secondary); }
        
        .btn-sm { padding: 6px 12px; font-size: 13px; border-radius: 4px; cursor: pointer; display: inline-block; border: none; transition: 0.2s;}
        .btn-primary { background: #4D661C; color: white; }
        .btn-warning { background: #f59e0b; color: white; }
        .btn-success { background: #10b981; color: white; }
        
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
        .badge-active { background: #d1fae5; color: #065f46; }
        .badge-inactive { background: #fee2e2; color: #991b1b; }

        .msg-alert { padding: 12px 15px; border-radius: 4px; margin-bottom: 20px; font-weight: 500;}
        .msg-success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;}
        .msg-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca;}
        
        /* CSS dành riêng cho phần Form Thêm danh mục */
        .add-form-container { background: var(--color-surface); padding: var(--space-4); border-radius: var(--radius-lg); margin-bottom: var(--space-5); box-shadow: var(--shadow-sm); border: 1px solid var(--color-border); }
        .form-row { display: flex; gap: 15px; align-items: flex-end; }
        .form-group { display: flex; flex-direction: column; flex: 1; }
        .form-group label { margin-bottom: 5px; font-weight: 500; font-size: 14px; }
        .form-group input, .form-group select { padding: 8px 12px; border: 1px solid var(--color-border); border-radius: 4px; }
    </style>
</head>
<body>
<div class="admin-layout">
    <!-- Sidebar Navigation -->
    <aside class="admin-sidebar">
        <div class="navbar__logo mb-4">
            <div class="logo-icon"><i class="fa-solid fa-leaf"></i></div>
            <div class="logo-text">Fruit<span class="text-highlight">Mkt</span> Admin</div>
        </div>
        <ul class="navbar__menu" style="flex-direction: column; align-items: flex-start; padding: 0;">
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/dashboard" class="menu-link" style="width: 100%;"><i class="fa-solid fa-chart-pie"></i> Dashboard</a></li>
            <!-- Highlight menu Danh mục hệ thống -->
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/categories" class="menu-link" style="width: 100%; background: rgba(77,102,28,0.1); color: var(--color-primary);"><i class="fa-solid fa-tags"></i> Danh mục hệ thống</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/products" class="menu-link" style="width: 100%;"><i class="fa-solid fa-box"></i> Sản phẩm vi phạm</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/shops" class="menu-link" style="width: 100%;"><i class="fa-solid fa-store"></i> Duyệt gian hàng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/users" class="menu-link" style="width: 100%;"><i class="fa-solid fa-users"></i> Người dùng</a></li>
            <li style="width: 100%;"><a href="${pageContext.request.contextPath}/admin/settlements" class="menu-link" style="width: 100%;"><i class="fa-solid fa-file-invoice"></i> Đối soát</a></li>
        </ul>
    </aside>

    <main class="admin-main">
        <h1 style="margin-bottom: var(--space-4);">Quản lý Danh mục sản phẩm</h1>
        
        <!-- Khối hiển thị thông báo alert -->
        <c:if test="${param.msg == 'add_success'}"><div class="msg-alert msg-success">Đã thêm danh mục mới thành công!</div></c:if>
        <c:if test="${param.msg == 'toggle_success'}"><div class="msg-alert msg-success">Cập nhật trạng thái hiển thị thành công!</div></c:if>
        <c:if test="${param.msg == 'error'}"><div class="msg-alert msg-error">Lỗi thao tác! Có thể tên danh mục hoặc URL tĩnh (slug) đã bị trùng lặp. Vui lòng thử lại.</div></c:if>

        <!-- Form nhập liệu thêm danh mục mới -->
        <div class="add-form-container">
            <h3><i class="fa-solid fa-plus"></i> Thêm danh mục mới</h3>
            <form action="${pageContext.request.contextPath}/admin/categories/add" method="POST" style="margin-top: 15px;">
                <div class="form-row">
                    <div class="form-group">
                        <label>Tên danh mục</label>
                        <input type="text" name="name" required placeholder="VD: Trái cây nội địa" />
                    </div>
                    <div class="form-group">
                        <label>URL tĩnh (Slug - Dùng SEO)</label>
                        <input type="text" name="slug" required placeholder="VD: trai-cay-noi-dia" />
                    </div>
                    <div class="form-group" style="flex: 0.5;">
                        <label>Thứ tự sắp xếp</label>
                        <input type="number" name="displayOrder" required value="0" />
                    </div>
                    <div class="form-group" style="flex: 0.5;">
                        <label>Trạng thái</label>
                        <select name="isActive">
                            <option value="1">Hiển thị ra Web</option>
                            <option value="0">Tạm ẩn</option>
                        </select>
                    </div>
                    <!-- Submit form về server -->
                    <button type="submit" class="btn-sm btn-primary" style="padding: 10px 20px;"><i class="fa-solid fa-save"></i> Lưu</button>
                </div>
            </form>
        </div>

        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Thứ tự</th>
                        <th>Tên danh mục</th>
                        <th>Slug (Đường dẫn)</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Duyệt qua List danh mục từ DAO -->
                    <c:forEach var="cat" items="${categories}">
                        <tr>
                            <td>#${cat.display_order}</td>
                            <td><strong>${cat.name}</strong></td>
                            <td style="color: var(--color-primary);">${cat.slug}</td>
                            <td>
                                <!-- Nhãn Hiển thị hay Đang Ẩn -->
                                <c:choose>
                                    <c:when test="${cat.is_active}"><span class="badge badge-active">Đang hiển thị</span></c:when>
                                    <c:otherwise><span class="badge badge-inactive">Đang ẩn</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <!-- Nếu đang Bật thì hiện nút Ẩn -->
                                <c:if test="${cat.is_active}">
                                    <form action="${pageContext.request.contextPath}/admin/categories/toggle" method="POST" style="display:inline;">
                                        <input type="hidden" name="categoryId" value="${cat.category_id}" />
                                        <input type="hidden" name="isActive" value="0" />
                                        <button type="submit" class="btn-sm btn-warning" title="Ẩn danh mục"><i class="fa-solid fa-eye-slash"></i> Ẩn</button>
                                    </form>
                                </c:if>
                                
                                <!-- Nếu đang Ẩn thì hiện nút Bật -->
                                <c:if test="${!cat.is_active}">
                                    <form action="${pageContext.request.contextPath}/admin/categories/toggle" method="POST" style="display:inline;">
                                        <input type="hidden" name="categoryId" value="${cat.category_id}" />
                                        <input type="hidden" name="isActive" value="1" />
                                        <button type="submit" class="btn-sm btn-success" title="Hiện danh mục"><i class="fa-solid fa-eye"></i> Hiện lại</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
