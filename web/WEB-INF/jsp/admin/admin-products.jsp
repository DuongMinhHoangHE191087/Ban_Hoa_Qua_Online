<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phê duyệt Sản phẩm – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <!-- Tailwind & SweetAlert -->
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <%-- Sidebar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="admin-products"/>
    </jsp:include>

    <%-- Main --%>
    <main class="admin-main p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">

        <%-- Page header --%>
        <div class="flex flex-col md:flex-row md:items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8 gap-4">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Phê Duyệt Sản Phẩm</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Duyệt sản phẩm mới đăng, kiểm tra giấy tờ chứng nhận, gắn nhãn Hữu cơ/Nhập khẩu và lọc sản phẩm vi phạm.</p>
            </div>
            
            <%-- Filter Tabs --%>
            <div class="flex items-center gap-2 bg-surface/80 p-1.5 rounded-xl border border-border self-start md:self-auto">
                <a href="${pageContext.request.contextPath}/admin/products" 
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${empty paramApprovalStatus ? 'bg-primary text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Tất cả
                </a>
                <a href="${pageContext.request.contextPath}/admin/products?approvalStatus=PENDING" 
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'PENDING' ? 'bg-[#eab308] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Chờ duyệt
                </a>
                <a href="${pageContext.request.contextPath}/admin/products?approvalStatus=APPROVED" 
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'APPROVED' ? 'bg-[#22c55e] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Đã duyệt
                </a>
                <a href="${pageContext.request.contextPath}/admin/products?approvalStatus=REJECTED" 
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'REJECTED' ? 'bg-[#ef4444] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Từ chối
                </a>
            </div>
        </div>
        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <%-- Products List Card --%>
        <div class="glass-card overflow-hidden">
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-boxes-stacked text-primary mr-1"></i> Danh Sách Sản Phẩm</h3>
                <span class="inline-flex items-center px-2.5 py-1 rounded-full bg-primary-lt border border-[#d9f99d] text-primary text-xs font-bold">
                    ${products.size()} sản phẩm
                </span>
            </div>

            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm">
                    <thead>
                        <tr class="bg-surface-2 border-b border-border text-txt-2 text-xs uppercase tracking-wider">
                            <th class="px-6 py-3.5 font-bold">Mã sản phẩm</th>
                            <th class="px-6 py-3.5 font-bold">Sản phẩm</th>
                            <th class="px-6 py-3.5 font-bold">Nhóm hàng / Danh mục</th>
                            <th class="px-6 py-3.5 font-bold">Chủ cửa hàng</th>
                            <th class="px-6 py-3.5 font-bold">Giấy tờ kiểm định</th>
                            <th class="px-6 py-3.5 font-bold text-center">Nhãn nhãn</th>
                            <th class="px-6 py-3.5 font-bold text-center">Trạng thái duyệt</th>
                            <th class="px-6 py-3.5 font-bold text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[#f1f5f9]">
                        <c:choose>
                            <c:when test="${empty products}">
                                <tr>
                                    <td colspan="8" class="px-6 py-12 text-center text-txt-3">
                                        <i class="fa-solid fa-box-open text-3xl mb-2 block text-slate-300"></i>
                                        Không tìm thấy sản phẩm nào phù hợp với bộ lọc.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${products}">
                                    <tr>
                                        <%-- ID --%>
                                        <td class="px-6 py-4 font-mono font-bold text-primary">#${p.productId}</td>
                                        
                                        <%-- Product detail info --%>
                                        <td class="px-6 py-4">
                                            <div class="flex flex-col">
                                                <span class="font-bold text-txt">${p.name}</span>
                                                <span class="text-xs text-txt-2 mt-0.5">Xuất xứ: ${p.originCountry} (${p.originRegion})</span>
                                                <span class="text-[11px] text-txt-3 mt-0.5">Status bán: 
                                                    <c:choose>
                                                        <c:when test="${p.status == 'ACTIVE'}">
                                                            <span class="text-emerald-600 font-semibold">Active</span>
                                                        </c:when>
                                                        <c:when test="${p.status == 'INACTIVE'}">
                                                            <span class="text-amber-500 font-semibold">Inactive</span>
                                                        </c:when>
                                                        <c:when test="${p.status == 'OUT_OF_SEASON'}">
                                                            <span class="text-slate-500">Hết mùa</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-red-500 font-semibold">${p.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </td>
                                        
                                        <%-- Category --%>
                                        <td class="px-6 py-4 font-semibold text-txt-2">
                                            <c:forEach var="cat" items="${categories}">
                                                <c:if test="${cat.categoryId == p.categoryId}">
                                                    ${cat.name}
                                                </c:if>
                                            </c:forEach>
                                        </td>
                                        
                                        <%-- Owner --%>
                                        <td class="px-6 py-4 text-xs text-txt-2">
                                            <span class="font-mono">Owner ID: ${p.ownerId}</span>
                                        </td>
                                        
                                        <%-- Verification documents --%>
                                        <td class="px-6 py-4">
                                            <c:choose>
                                                <c:when test="${not empty p.verificationDocPath}">
                                                    <a href="${pageContext.request.contextPath}/${p.verificationDocPath}" 
                                                       target="_blank" 
                                                       class="inline-flex items-center gap-1 text-primary hover:text-primary-dk font-bold transition-all text-xs bg-primary-lt border border-[#bbf7d0] px-2.5 py-1.5 rounded-lg shadow-sm">
                                                        <i class="fa-solid fa-file-pdf"></i> Xem giấy tờ
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-red-500 text-xs font-semibold flex items-center gap-1">
                                                        <i class="fa-solid fa-triangle-exclamation"></i> Không có giấy tờ
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        
                                        <%-- Organic/Imported Label --%>
                                        <td class="px-6 py-4 text-center">
                                            <div class="flex flex-col gap-1 items-center justify-center">
                                                <c:choose>
                                                    <c:when test="${p.isOrganic}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-emerald-100 border border-emerald-200 text-emerald-800 text-[10px] font-bold">
                                                            <i class="fa-solid fa-leaf mr-0.5"></i> Hữu cơ
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-[10px] text-txt-3">Thường</span>
                                                    </c:otherwise>
                                                </c:choose>
                                                
                                                <c:choose>
                                                    <c:when test="${p.isImported}">
                                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full bg-blue-100 border border-blue-200 text-blue-800 text-[10px] font-bold">
                                                            <i class="fa-solid fa-globe mr-0.5"></i> Nhập khẩu
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-[10px] text-txt-3">Nội địa</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        
                                        <%-- Approval Status & Rejection Reason --%>
                                        <td class="px-6 py-4 text-center">
                                            <div class="flex flex-col items-center justify-center">
                                                <c:choose>
                                                    <c:when test="${p.approvalStatus == 'APPROVED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold">
                                                            <i class="fa-solid fa-circle-check"></i> Đã Duyệt
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${p.approvalStatus == 'REJECTED'}">
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-rose-50 border border-rose-100 text-rose-800 text-xs font-bold cursor-help" 
                                                              title="Lý do: ${p.rejectionReason}">
                                                            <i class="fa-solid fa-circle-xmark"></i> Bị từ chối
                                                        </span>
                                                        <span class="text-[10px] text-rose-600 font-semibold mt-1 max-w-[150px] truncate" title="${p.rejectionReason}">
                                                            Lý do: ${p.rejectionReason}
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-yellow-50 border border-yellow-100 text-yellow-800 text-xs font-bold animate-pulse">
                                                            <i class="fa-solid fa-clock"></i> Chờ duyệt
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        
                                        <%-- Action buttons --%>
                                        <td class="px-6 py-4">
                                            <div class="flex items-center justify-center gap-2">
                                                <c:choose>
                                                    <c:when test="${p.approvalStatus == 'PENDING'}">
                                                        <%-- Duyệt button — dùng data-* attribute để tránh lỗi khi tên sản phẩm có dấu nháy đơn --%>
                                                        <button type="button"
                                                                class="btn-approve bg-[#22c55e] hover:bg-[#16a34a] text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all shadow-sm cursor-pointer"
                                                                data-product-id="${p.productId}"
                                                                data-product-name="<c:out value='${p.name}'/>"
                                                                data-category-id="${p.categoryId}"
                                                                data-is-organic="${p.isOrganic}"
                                                                data-is-imported="${p.isImported}">
                                                            <i class="fa-solid fa-check mr-0.5"></i> Duyệt
                                                        </button>
                                                        
                                                        <%-- Từ chối button — dùng data-* attribute để tránh lỗi khi tên sản phẩm có dấu nháy đơn --%>
                                                        <button type="button"
                                                                class="btn-reject bg-[#ef4444] hover:bg-[#dc2626] text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all shadow-sm cursor-pointer"
                                                                data-product-id="${p.productId}"
                                                                data-product-name="<c:out value='${p.name}'/>">  
                                                            <i class="fa-solid fa-xmark mr-0.5"></i> Từ chối
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <%-- Ban / Soft delete button if approved/rejected but violates rules later --%>
                                                        <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="inline"
                                                              onsubmit="return confirmBan(event)"
                                                              data-product-name="${fn:escapeXml(p.name)}">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="ban">
                                                            <input type="hidden" name="productId" value="${p.productId}">
                                                            <button type="submit" 
                                                                    class="bg-white hover:bg-red-50 border border-red-200 text-red-600 hover:text-red-700 font-bold px-2.5 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
                                                                <i class="fa-solid fa-ban mr-0.5"></i> Gỡ bỏ
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            
            <%-- Pagination --%>
            <c:if test="${totalPages > 1}">
                <div class="px-6 py-4 border-t border-border flex items-center justify-between bg-slate-50/50">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="${pageContext.request.contextPath}/admin/products?${not empty paramApprovalStatus ? 'approvalStatus=' : ''}${fn:escapeXml(paramApprovalStatus)}" />
                </div>
            </c:if>
        </div>
    </main>
</div>

<%-- Approve Confirmation / Config Modal --%>
<div id="approveModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Cấu hình & Phê duyệt sản phẩm</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('approveModal')">&times;</button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="p-6">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
            <input type="hidden" name="action" value="approve">
            <input type="hidden" name="productId" id="approveProductId">
            
            <div class="mb-4">
                <p class="text-sm text-txt-2 mb-2">Tên sản phẩm: <b class="text-txt" id="approveProductName"></b></p>
            </div>
            
            <%-- Category selection override --%>
            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Nhóm danh mục sản phẩm <span class="text-red-500">*</span></label>
                <select name="categoryId" id="approveCategoryId" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all" required>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.categoryId}">${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
            
            <%-- Labels assignment --%>
            <div class="mb-5 flex flex-col gap-3">
                <div class="flex items-center gap-2">
                    <input type="checkbox" name="isOrganic" id="approveOrganic" class="w-4 h-4 rounded text-primary focus:ring-primary border-slate-300">
                    <label for="approveOrganic" class="text-sm font-semibold text-txt-2 cursor-pointer select-none">
                        <i class="fa-solid fa-leaf text-emerald-600 mr-1"></i> Gắn nhãn <b>Hữu cơ (Organic)</b>
                    </label>
                </div>
                
                <div class="flex items-center gap-2">
                    <input type="checkbox" name="isImported" id="approveImported" class="w-4 h-4 rounded text-primary focus:ring-primary border-slate-300">
                    <label for="approveImported" class="text-sm font-semibold text-txt-2 cursor-pointer select-none">
                        <i class="fa-solid fa-globe text-blue-600 mr-1"></i> Gắn nhãn <b>Nhập khẩu (Imported)</b>
                    </label>
                </div>
            </div>
            
            <button type="submit" class="w-full py-3 bg-[#22c55e] hover:bg-[#16a34a] text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Đồng ý Duyệt & Đưa lên sàn
            </button>
        </form>
    </div>
</div>

<%-- Reject Modal --%>
<div id="rejectModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Từ chối duyệt sản phẩm</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('rejectModal')">&times;</button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="p-6">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
            <input type="hidden" name="action" value="reject">
            <input type="hidden" name="productId" id="rejectProductId">
            
            <div class="mb-4">
                <p class="text-sm text-txt-2 mb-2">Tên sản phẩm: <b class="text-txt" id="rejectProductName"></b></p>
            </div>
            
            <div class="mb-5">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Lý do từ chối <span class="text-red-500">*</span></label>
                <textarea name="reason" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all" 
                          rows="4" required placeholder="Nhập lý do từ chối (ví dụ: Giấy tờ xác nhận hết hạn, thông tin không chính xác...)"></textarea>
            </div>
            
            <button type="submit" class="w-full py-3 bg-[#ef4444] hover:bg-[#dc2626] text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Từ chối phê duyệt
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
    
    // Đọc data-* attributes từ nút Duyệt, điền vào modal — an toàn với mọi ký tự đặc biệt
    document.querySelectorAll('.btn-approve').forEach(function(btn) {
        btn.addEventListener('click', function() {
            openApproveModal(
                this.dataset.productId,
                this.dataset.productName,
                this.dataset.categoryId,
                this.dataset.isOrganic === 'true',
                this.dataset.isImported === 'true'
            );
        });
    });

    // Đọc data-* attributes từ nút Từ chối, điền vào modal
    document.querySelectorAll('.btn-reject').forEach(function(btn) {
        btn.addEventListener('click', function() {
            openRejectModal(this.dataset.productId, this.dataset.productName);
        });
    });

    function openApproveModal(id, name, categoryId, isOrganic, isImported) {
        document.getElementById('approveProductId').value = id;
        document.getElementById('approveProductName').innerText = name;
        document.getElementById('approveCategoryId').value = categoryId;
        document.getElementById('approveOrganic').checked = isOrganic;
        document.getElementById('approveImported').checked = isImported;
        openModal('approveModal');
    }
    
    function openRejectModal(id, name) {
        document.getElementById('rejectProductId').value = id;
        document.getElementById('rejectProductName').innerText = name;
        openModal('rejectModal');
    }

    function confirmBan(event) {
        event.preventDefault();
        var form = event.target.closest ? event.target.closest('form') : event.target;
        var rawName = form ? (form.dataset.productName || '') : '';
        var safeName = rawName.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
        Swal.fire({
            title: 'Gỡ bỏ sản phẩm?',
            html: 'Bạn có chắc chắn muốn gỡ bỏ sản phẩm <b>' + safeName + '</b> khỏi website?<br><small class="text-red-500 font-semibold">Lưu ý: Sản phẩm sẽ bị ẩn hoàn toàn và chuyển thành trạng thái xóa mềm để đảm bảo dữ liệu đơn hàng cũ.</small>',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#e5e7eb',
            confirmButtonText: 'Có, gỡ bỏ ngay',
            cancelButtonText: 'Hủy'
        }).then(r => { if (r.isConfirmed) event.target.submit(); });
        return false;
    }

    window.onclick = e => {
        if (e.target === document.getElementById('approveModal')) closeModal('approveModal');
        if (e.target === document.getElementById('rejectModal')) closeModal('rejectModal');
    };
</script>
</body>
</html>
