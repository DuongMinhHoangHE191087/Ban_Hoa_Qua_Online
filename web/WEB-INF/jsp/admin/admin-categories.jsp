<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh mục sản phẩm – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="categories"/>
    </jsp:include>

    <main class="admin-main p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">
        <div class="flex items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Danh Mục Sản Phẩm</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Cấu hình các phân nhóm trái cây hiển thị trên website bán hàng.</p>
            </div>
            <button onclick="openModal('addModal')"
                    class="bg-surface hover:bg-primary-lt text-primary border border-primary-fixed font-bold px-4 py-2.5 rounded-xl text-xs flex items-center gap-1.5 shadow-sm transition-all active:scale-95 cursor-pointer">
                <i class="fa-solid fa-plus text-sm text-primary"></i> Thêm danh mục mới
            </button>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />
        <div class="glass-card overflow-hidden">
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-tags text-primary mr-1"></i> Danh Sách Danh Mục</h3>
                <span class="inline-flex items-center px-2.5 py-1 rounded-full bg-primary-lt border border-[#d9f99d] text-primary text-xs font-bold">
                    ${totalItems} nhóm hàng
                </span>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">Tên Danh Mục</th>
                            <th class="px-6 py-3.5 font-bold">Slug (URL)</th>
                            <th class="px-6 py-3.5 font-bold text-center">Thứ tự hiển thị</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty categories}">
                                <tr>
                                    <td colspan="5" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-inbox text-3xl mb-2 block text-slate-300"></i>
                                        Chưa có danh mục nào trên hệ thống.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="c" items="${categories}">
                                    <tr>
                                        <td class="px-6 py-4 font-bold text-txt">
                                            <a href="${pageContext.request.contextPath}/admin/products?categoryId=${c.categoryId}"
                                               class="hover:text-primary transition-colors"
                                               title="Xem sản phẩm trong danh mục này">
                                                <c:out value="${c.name}"/>
                                            </a>
                                        </td>
                                        <td class="px-6 py-4 font-mono text-xs text-txt-2 bg-[#f8fafc]/40"><c:out value="${c.slug}"/></td>
                                        <td class="px-6 py-4 text-center font-semibold text-txt-2"><c:out value="${c.displayOrder}"/></td>
                                        <td class="px-6 py-4 text-center">
                                            <c:choose>
                                                <c:when test="${c.getIsActive()}">
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                        <i class="fa-solid fa-eye text-[10px]"></i> Đang Hiện
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-500 text-xs font-semibold">
                                                        <i class="fa-solid fa-eye-slash text-[10px]"></i> Đã Ẩn
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4">
                                            <div class="flex items-center justify-center gap-2">
                                                <button type="button"
                                                        data-category-id="${c.categoryId}"
                                                        data-category-name="<c:out value='${c.name}'/>"
                                                        data-category-slug="<c:out value='${c.slug}'/>"
                                                        data-category-order="${c.displayOrder}"
                                                        data-category-active="${c.getIsActive()}"
                                                        onclick="openEditModal(this)"
                                                        class="bg-white hover:bg-slate-50 border border-slate-200 text-txt-2 hover:text-primary font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
                                                    <i class="fa-solid fa-pen mr-0.5"></i> Sửa
                                                </button>

                                                <form method="POST" action="${pageContext.request.contextPath}/admin/categories" class="inline">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="toggle">
                                                    <input type="hidden" name="categoryId" value="${c.categoryId}">
                                                    <button type="submit"
                                                            class="bg-white hover:bg-slate-50 border border-slate-200 ${c.getIsActive() ? 'text-red-500' : 'text-emerald-600'} font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
                                                        <i class="fa-solid ${c.getIsActive() ? 'fa-eye-slash' : 'fa-eye'}"></i>
                                                    </button>
                                                </form>

                                                <form method="POST" action="${pageContext.request.contextPath}/admin/categories" class="inline"
                                                      data-category-name="<c:out value='${c.name}'/>"
                                                      onsubmit="return confirmDelete(event, this)">
                                                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="categoryId" value="${c.categoryId}">
                                                    <button type="submit"
                                                            class="bg-white hover:bg-red-50 border border-red-100 text-red-600 hover:text-red-700 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
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

            <c:if test="${totalPages > 1}">
                <div class="px-6 py-4 border-t border-border flex items-center justify-between bg-slate-50/50">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="?" />
                </div>
            </c:if>
        </div>
    </main>
</div>

<div id="addModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Thêm danh mục mới</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('addModal')">&times;</button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/admin/categories" class="p-6">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
            <input type="hidden" name="action" value="create">

            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Tên danh mục <span class="text-red-500">*</span></label>
                <input type="text" name="name" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all"
                       required placeholder="Nhập tên (ví dụ: Trái Cây Nhập Khẩu)" onkeyup="generateSlug(this.value, 'addSlug')">
            </div>

            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Slug (URL thân thiện)</label>
                <input type="text" name="slug" id="addSlug" class="w-full rounded-xl border border-slate-300 bg-slate-50 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm font-mono transition-all"
                       required placeholder="slug-danh-muc">
            </div>

            <div class="mb-5">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Thứ tự hiển thị</label>
                <input type="number" name="displayOrder" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all"
                       value="0" required>
            </div>

            <div class="mb-6 flex items-center gap-2">
                <input type="checkbox" name="isActive" id="addActive" checked class="w-4 h-4 rounded text-primary focus:ring-primary border-slate-300">
                <label for="addActive" class="text-sm font-semibold text-txt-2 cursor-pointer select-none">Hiển thị ngay trên website (Active)</label>
            </div>

            <button type="submit" class="w-full py-3 bg-primary hover:bg-primary-dk text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Lưu danh mục
            </button>
        </form>
    </div>
</div>

<div id="editModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Chỉnh sửa danh mục</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('editModal')">&times;</button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/admin/categories" class="p-6">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="categoryId" id="editCategoryId">

            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Tên danh mục <span class="text-red-500">*</span></label>
                <input type="text" name="name" id="editName" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all"
                       required onkeyup="generateSlug(this.value, 'editSlug')">
            </div>

            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Slug (URL)</label>
                <input type="text" name="slug" id="editSlug" class="w-full rounded-xl border border-slate-300 bg-slate-50 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm font-mono transition-all"
                       required>
            </div>

            <div class="mb-5">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Thứ tự hiển thị</label>
                <input type="number" name="displayOrder" id="editOrder" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all"
                       required>
            </div>

            <div class="mb-6 flex items-center gap-2">
                <input type="checkbox" name="isActive" id="editActive" class="w-4 h-4 rounded text-primary focus:ring-primary border-slate-300">
                <label for="editActive" class="text-sm font-semibold text-txt-2 cursor-pointer select-none">Hiển thị trên website (Active)</label>
            </div>

            <button type="submit" class="w-full py-3 bg-primary hover:bg-primary-dk text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Cập nhật thay đổi
            </button>
        </form>
    </div>
</div>

<script>
    function openModal(id) {
        document.getElementById(id).classList.remove('hidden');
    }

    function closeModal(id) {
        document.getElementById(id).classList.add('hidden');
    }

    function generateSlug(text, targetId) {
        var slug = (text || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .replace(/[^a-z0-9\s-]/g, '')
            .trim()
            .replace(/\s+/g, '-')
            .replace(/-+/g, '-');
        document.getElementById(targetId).value = slug;
    }

    function openEditModal(button) {
        document.getElementById('editCategoryId').value = button.dataset.categoryId;
        document.getElementById('editName').value = button.dataset.categoryName;
        document.getElementById('editSlug').value = button.dataset.categorySlug;
        document.getElementById('editOrder').value = button.dataset.categoryOrder;
        document.getElementById('editActive').checked = button.dataset.categoryActive === 'true';
        openModal('editModal');
    }

    function confirmDelete(event, form) {
        event.preventDefault();
        const categoryName = form.dataset.categoryName || 'danh mục này';
        Swal.fire({
            title: 'Xóa danh mục?',
            text: 'Bạn có chắc muốn xóa ' + categoryName + '?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) form.submit(); });
        return false;
    }
</script>
</body>
</html>
