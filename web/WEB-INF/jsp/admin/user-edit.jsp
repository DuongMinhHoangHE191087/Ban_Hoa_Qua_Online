<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sửa người dùng - Admin MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .admin-panel-form {
            background: #ffffff;
            border-radius: var(--radius-lg, 12px);
            box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
            padding: 2rem;
            border: 1px solid var(--color-border, #e5e7eb);
        }
        
        .form-group {
            margin-bottom: 1.5rem;
        }
        
        .form-label {
            display: block;
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: var(--color-text, #1f2937);
        }
        
        .form-control {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid var(--color-border, #d1d5db);
            border-radius: var(--radius-md, 8px);
            font-size: 1rem;
            font-family: 'Plus Jakarta Sans', sans-serif;
            transition: all 0.2s ease;
            box-sizing: border-box;
        }
        
        .form-control:focus {
            outline: none;
            border-color: var(--color-primary, #22c55e);
            box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2);
        }
        
        .form-control:disabled {
            background-color: #f3f4f6;
            color: #6b7280;
            cursor: not-allowed;
            border-color: #e5e7eb;
        }

        .form-actions {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            padding-top: 1.5rem;
            border-top: 1px solid var(--color-border, #e5e7eb);
        }
        
        .text-danger {
            color: #ef4444;
        }
    </style>
</head>
<body>
    <div class="admin-layout">
        <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
            <jsp:param name="activeMenu" value="users"/>
        </jsp:include>

        <main class="admin-main">
            <header class="admin-header">
                <h1>Sửa người dùng: <span style="color: var(--color-primary);">${user.email}</span></h1>
            </header>
            
            <div class="admin-content">
                <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
                
                <div class="admin-panel admin-panel-form" style="max-width: 700px; margin: 0 auto;">
                    <form action="${pageContext.request.contextPath}/admin/users/edit" method="post">
                        <input type="hidden" name="userId" value="${user.userId}">
                        
                        <div class="form-group">
                            <label class="form-label">Email (Tài khoản)</label>
                            <input type="text" class="form-control" value="${user.email}" disabled>
                            <small style="color: #6b7280; margin-top: 0.25rem; display: block;"><i class="fa-solid fa-circle-info"></i> Không thể thay đổi email đã đăng ký.</small>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Họ và tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="fullName" value="${user.fullName}" required placeholder="Nhập họ và tên đầy đủ">
                        </div>

                        <div class="form-group" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                            <div>
                                <label class="form-label">Số điện thoại</label>
                                <input type="text" class="form-control" name="phone" value="${user.phone}" placeholder="Ví dụ: 0912345678">
                            </div>

                            <div>
                                <label class="form-label">Vai trò hệ thống</label>
                                <select class="form-control" name="role" required>
                                    <option value="CUSTOMER" ${user.role == 'CUSTOMER' ? 'selected' : ''}>Khách hàng (CUSTOMER)</option>
                                    <option value="SHOP_OWNER" ${user.role == 'SHOP_OWNER' ? 'selected' : ''}>Chủ cửa hàng (SHOP_OWNER)</option>
                                    <option value="DELIVERY" ${user.role == 'DELIVERY' ? 'selected' : ''}>Nhân viên giao hàng (DELIVERY)</option>
                                    <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>Quản trị viên (ADMIN)</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label class="form-label">Địa chỉ</label>
                            <textarea class="form-control" name="userAddress" rows="3" placeholder="Nhập địa chỉ cụ thể">${user.userAddress}</textarea>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem; font-weight: 600;"><i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi</button>
                            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary" style="padding: 0.75rem 2rem; font-weight: 600;">Quay lại</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
