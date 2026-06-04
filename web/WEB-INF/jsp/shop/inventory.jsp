<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tồn kho | Kênh Người Bán</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        body { background-color: var(--color-background); font-family: var(--font-family); margin: 0; }
        .shop-layout { display: flex; min-height: 100vh; }
        
        /* Sidebar */
        .sidebar { width: 250px; background-color: white; border-right: 1px solid var(--color-border); padding: 1.5rem 0; flex-shrink: 0; }
        .sidebar-brand { font-size: 1.25rem; font-weight: 700; color: var(--color-primary); padding: 0 1.5rem 1.5rem; border-bottom: 1px solid var(--color-border); margin-bottom: 1rem; }
        .nav-item { display: flex; align-items: center; padding: 0.75rem 1.5rem; color: var(--color-text-secondary); text-decoration: none; transition: all 0.2s; }
        .nav-item:hover, .nav-item.active { background-color: var(--color-primary-light); color: var(--color-primary); }
        .nav-item i { width: 20px; margin-right: 10px; }
        
        /* Main Content */
        .main-content { flex: 1; padding: 2rem; overflow-y: auto; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
        .page-title { font-size: 1.5rem; font-weight: 700; color: var(--color-text-primary); }
        
        /* Layout Grid */
        .inventory-grid { display: grid; grid-template-columns: 1fr; gap: 2rem; }
        @media (min-width: 992px) {
            .inventory-grid { grid-template-columns: 350px 1fr; }
        }
        
        /* Card & Table */
        .card { background: white; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); border: 1px solid var(--color-border); overflow: hidden; height: fit-content; }
        .card-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--color-border); background-color: rgba(77, 102, 28, 0.02); }
        .card-title { font-size: 1.1rem; font-weight: 700; color: var(--color-primary-dark); margin: 0; }
        .card-body { padding: 1.5rem; }
        
        .table-responsive { width: 100%; overflow-x: auto; }
        .table-responsive-scroll {
            width: 100%;
            overflow-x: auto;
            max-height: 230px;
            overflow-y: auto;
            border-bottom: 1px solid var(--color-border);
        }
        .table { width: 100%; border-collapse: collapse; text-align: left; }
        .table th, .table td { padding: 0.85rem 1.25rem; border-bottom: 1px solid var(--color-border); font-size: 0.9rem; }
        .table th { background-color: #f8fcf9; font-weight: 600; color: var(--color-text-primary); text-transform: uppercase; letter-spacing: 0.05em; font-size: 0.8rem; position: sticky; top: 0; z-index: 10; border-bottom: 1.5px solid var(--color-border); }
        .table tr:last-child td { border-bottom: none; }
        .table tr:hover { background-color: rgba(77, 102, 28, 0.02); }
        
        /* Badge for stock quantity increase */
        .stock-delta { font-weight: 700; padding: 2px 8px; border-radius: var(--radius-sm); font-size: 0.85rem; display: inline-block; }
        .stock-delta-positive { background-color: #E8F5E9; color: var(--color-success); }
        .stock-delta-negative { background-color: #FFEBEE; color: var(--color-danger); }
        
        /* Form Controls overrides for consistency */
        .form-group { margin-bottom: 1.25rem; }
        .form-label { display: block; margin-bottom: 0.5rem; font-weight: 600; color: var(--color-text-secondary); font-size: 0.875rem; }
        .form-control { width: 100%; padding: 0.65rem 0.75rem; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-family: inherit; font-size: 0.925rem; box-sizing: border-box; background-color: #fff; transition: border-color 0.2s; }
        .form-control:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(77, 102, 28, 0.1); }
        
        /* Alert Close button */
        .alert-dismissible { position: relative; display: flex; justify-content: space-between; align-items: center; }
        .btn-close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
        .btn-close:hover { opacity: 1; }
    </style>
</head>
<body>
    <div class="shop-layout">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="sidebar-brand">Kênh Người Bán</div>
            <a href="${pageContext.request.contextPath}/shop/dashboard" class="nav-item"><i class="fa-solid fa-chart-line"></i> Tổng quan</a>
            <a href="${pageContext.request.contextPath}/shop/products" class="nav-item"><i class="fa-solid fa-box"></i> Sản phẩm</a>
            <a href="${pageContext.request.contextPath}/shop/orders" class="nav-item"><i class="fa-solid fa-clipboard-list"></i> Đơn hàng</a>
            <a href="${pageContext.request.contextPath}/shop/promotions" class="nav-item"><i class="fa-solid fa-tags"></i> Khuyến mãi</a>
            <a href="${pageContext.request.contextPath}/shop/inventory" class="nav-item active"><i class="fa-solid fa-warehouse"></i> Tồn kho</a>
            <a href="${pageContext.request.contextPath}/shop/settlement" class="nav-item"><i class="fa-solid fa-wallet"></i> Tài chính</a>
            <a href="${pageContext.request.contextPath}/shop/profile" class="nav-item"><i class="fa-solid fa-store"></i> Hồ sơ Shop</a>
            <a href="${pageContext.request.contextPath}/auth/logout" class="nav-item" style="margin-top: auto; color: var(--color-danger);"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <!-- Flash Alert Messages -->
            <c:if test="${not empty sessionScope.flashMsg}">
                <div class="alert alert-${sessionScope.flashType == 'error' ? 'error' : 'success'} alert-dismissible" role="alert">
                    <span>
                        <i class="fa-solid ${sessionScope.flashType == 'error' ? 'fa-circle-exclamation' : 'fa-circle-check'} me-2"></i>
                        ${sessionScope.flashMsg}
                    </span>
                    <button type="button" class="btn-close" aria-label="Close" onclick="this.parentElement.style.display='none';"></button>
                </div>
                <c:remove var="flashMsg" scope="session"/>
                <c:remove var="flashType" scope="session"/>
            </c:if>

            <div class="page-header">
                <h1 class="page-title">Nhập hàng & Quản lý Tồn kho</h1>
            </div>

            <!-- Layout Grid -->
            <div class="inventory-grid">
                <!-- Form Column -->
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title"><i class="fa-solid fa-plus-minus me-2"></i>Nhập kho sản phẩm</h2>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/shop/inventory" method="POST" id="restockForm">
                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                            
                            <div class="form-group">
                                <label class="form-label" for="variantId">Sản phẩm & Biến thể <span class="text-danger">*</span></label>
                                <select name="variantId" id="variantId" class="form-control" required>
                                    <option value="" disabled selected>-- Chọn biến thể sản phẩm --</option>
                                    <c:forEach var="v" items="${variants}">
                                        <option value="${v.variantId}">
                                            ${v.productName} - ${v.variantLabel} (Hiện tại: ${v.stockQuantity})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="quantity">Số lượng nhập thêm <span class="text-danger">*</span></label>
                                <input type="number" name="quantity" id="quantity" class="form-control" min="1" placeholder="Ví dụ: 10, 50, 100" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="changedAt">Ngày nhập kho <span class="text-danger">*</span></label>
                                <input type="date" name="changedAt" id="changedAt" class="form-control" required>
                            </div>



                            <div class="form-group">
                                <label class="form-label" for="note">Ghi chú</label>
                                <input type="text" name="note" id="note" class="form-control" placeholder="Ghi chú nhập kho (ví dụ: Nhập hàng từ nhà cung cấp A)">
                            </div>

                            <button type="submit" class="btn btn-primary btn-block mt-4">
                                <i class="fa-solid fa-circle-arrow-down me-2"></i>Nhập kho
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Right Columns (Tables Column) -->
                <div style="display: flex; flex-direction: column; gap: 2rem;">
                    
                    <!-- Stock Levels Card -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title"><i class="fa-solid fa-boxes-stacked me-2"></i>Số lượng tồn kho hiện tại</h2>
                        </div>
                        <div class="card-body" style="padding: 0;">
                            <div class="table-responsive-scroll">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Sản phẩm & Biến thể</th>
                                            <th>SKU</th>
                                            <th>Tồn kho hiện tại</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="v" items="${variants}">
                                            <tr>
                                                <td>
                                                    <strong style="color: var(--color-text-primary);">${v.productName}</strong>
                                                    <div class="text-muted" style="font-size: 0.8rem;">${v.variantLabel}</div>
                                                </td>
                                                <td><code>${v.sku}</code></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${v.stockQuantity <= 0}">
                                                            <span class="badge badge-danger">Hết hàng (0)</span>
                                                        </c:when>
                                                        <c:when test="${v.stockQuantity < 10}">
                                                            <span class="badge badge-warning">Sắp hết hàng (${v.stockQuantity})</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-success">Còn hàng (${v.stockQuantity})</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty variants}">
                                            <tr>
                                                <td colspan="3" class="text-center py-4" style="color: var(--color-text-muted); font-style: italic; padding: 2rem;">
                                                    Chưa có sản phẩm nào!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- History Column -->
                    <div class="card">
                        <div class="card-header">
                            <h2 class="card-title"><i class="fa-solid fa-clock-rotate-left me-2"></i>Lịch sử biến động kho</h2>
                        </div>
                        <div class="card-body" style="padding: 0;">
                            <div class="table-responsive-scroll">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Mã</th>
                                            <th>Sản phẩm & Biến thể</th>
                                            <th>Thay đổi</th>
                                            <th>Loại</th>
                                            <th>Ghi chú</th>
                                            <th>Thời gian</th>
                                            <th>Người thực hiện</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${restockLogs}">
                                            <tr>
                                                <td>#${log.logId}</td>
                                                <td>
                                                    <strong style="color: var(--color-text-primary);">${log.productName}</strong>
                                                    <div class="text-muted" style="font-size: 0.8rem;">${log.variantLabel}</div>
                                                </td>
                                                <td>
                                                    <span class="stock-delta ${log.quantityDelta >= 0 ? 'stock-delta-positive' : 'stock-delta-negative'}">
                                                        ${log.quantityDelta >= 0 ? '+' : ''}${log.quantityDelta}
                                                    </span>
                                                </td>
                                                <td>
                                                    <code>${log.changeType}</code>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty log.note}">
                                                            <span style="color: var(--color-text-primary);">${log.note}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">-</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${log.formattedChangedAt}</td>
                                                <td>${log.changedByName}</td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty restockLogs}">
                                            <tr>
                                                <td colspan="7" class="text-center py-4" style="color: var(--color-text-muted); font-style: italic; padding: 2rem;">
                                                    Chưa có lịch sử biến động kho nào!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </main>
    </div>

    <script>
        // Set default restock date to today
        document.getElementById('changedAt').valueAsDate = new Date();
    </script>
</body>
</html>
