<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sản phẩm – Admin MetaFruit</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-overrides.css">
    <jsp:include page="/WEB-INF/jsp/common/tailwind-config.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
</head>
<body class="antialiased text-txt bg-background">
<div class="admin-layout">
    <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
        <jsp:param name="activeMenu" value="admin-products"/>
    </jsp:include>

    <main class="admin-main p-6 md:p-8 overflow-y-auto animate-fade-in-up opacity-0">
        <c:url var="allProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="ALL" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="pendingProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="PENDING" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="approvedProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="APPROVED" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="rejectedProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="REJECTED" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="adminProductsPaginationUrl" value="/admin/products">
            <c:if test="${not empty paramApprovalStatus}">
                <c:param name="approvalStatus" value="${paramApprovalStatus}" />
            </c:if>
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <div class="flex flex-col md:flex-row md:items-center justify-between bg-gradient-to-r from-primary-lt to-secondary-container/20 border border-primary-fixed/60 p-6 rounded-2xl shadow-sm mb-8 gap-4">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-primary-dark tracking-tight">Quản lý sản phẩm</h1>
                <p class="text-txt-2 text-xs md:text-sm mt-1">Duyệt sản phẩm mới đăng, kiểm tra giấy tờ chứng nhận và xử lý các sản phẩm vi phạm.</p>
            </div>
            <div class="flex items-center gap-2 bg-surface/80 p-1.5 rounded-xl border border-border self-start md:self-auto">
                <a href="${allProductsUrl}"
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'ALL' ? 'bg-primary text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Tất cả
                </a>
                <a href="${pendingProductsUrl}"
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'PENDING' ? 'bg-[#eab308] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Chờ duyệt
                </a>
                <a href="${approvedProductsUrl}"
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'APPROVED' ? 'bg-[#22c55e] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Đã duyệt
                </a>
                <a href="${rejectedProductsUrl}"
                   class="px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${paramApprovalStatus == 'REJECTED' ? 'bg-[#ef4444] text-white shadow-sm' : 'text-txt-2 hover:bg-slate-100'}">
                    Từ chối
                </a>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <c:url var="allProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="ALL" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="pendingProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="PENDING" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="approvedProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="APPROVED" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="rejectedProductsUrl" value="/admin/products">
            <c:param name="approvalStatus" value="REJECTED" />
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>
        <c:url var="adminProductsPaginationUrl" value="/admin/products">
            <c:if test="${not empty paramApprovalStatus}">
                <c:param name="approvalStatus" value="${paramApprovalStatus}" />
            </c:if>
            <c:if test="${not empty paramCategoryId}">
                <c:param name="categoryId" value="${paramCategoryId}" />
            </c:if>
        </c:url>

        <div class="glass-card overflow-hidden">
            <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                <h3 class="font-bold text-txt text-sm"><i class="fa-solid fa-boxes-stacked text-primary mr-1"></i> Danh Sách Sản Phẩm</h3>
                <span class="inline-flex items-center px-2.5 py-1 rounded-full bg-primary-lt border border-[#d9f99d] text-primary text-xs font-bold">
                    ${totalItems} sản phẩm
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
                                        <td class="px-6 py-4 font-mono font-bold text-primary">#${p.productId}</td>
                                        <td class="px-6 py-4">
                                            <div class="flex flex-col">
                                                <a href="${pageContext.request.contextPath}/products/detail?id=${p.productId}"
                                                   class="font-bold text-txt hover:text-primary transition-colors">
                                                    <c:out value="${p.name}"/>
                                                </a>
                                                <span class="text-xs text-txt-2 mt-0.5">Xuất xứ: ${p.originCountry} (${p.originRegion})</span>
                                                <span class="text-[11px] text-txt-3 mt-0.5">Status bán:
                                                    <c:choose>
                                                        <c:when test="${p.status == 'ACTIVE'}"><span class="text-emerald-600 font-semibold">Active</span></c:when>
                                                        <c:when test="${p.status == 'INACTIVE'}"><span class="text-amber-500 font-semibold">Inactive</span></c:when>
                                                        <c:when test="${p.status == 'OUT_OF_SEASON'}"><span class="text-slate-500">Hết mùa</span></c:when>
                                                        <c:otherwise><span class="text-red-500 font-semibold">${p.status}</span></c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </td>
                                        <td class="px-6 py-4 font-semibold text-txt-2">
                                            <c:choose>
                                                <c:when test="${not empty p.categoryName}">
                                                    <a href="${pageContext.request.contextPath}/admin/products?categoryId=${p.categoryId}"
                                                       class="hover:text-primary transition-colors">
                                                        <c:out value="${p.categoryName}"/>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>Chưa phân loại</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-txt-2">
                                            <div class="font-semibold text-txt">
                                                <a href="${pageContext.request.contextPath}/shop-view?id=${p.ownerId}&idType=owner"
                                                   class="hover:text-primary transition-colors">
                                                    <c:out value="${p.shopName}"/>
                                                </a>
                                            </div>
                                            <div class="text-[11px] text-txt-3 mt-0.5">
                                                Chủ:
                                                <a href="${pageContext.request.contextPath}/shop-view?id=${p.ownerId}&idType=owner"
                                                   class="text-primary hover:text-primary-dk font-semibold transition-colors">
                                                    <c:out value="${p.ownerName}"/>
                                                </a>
                                            </div>
                                        </td>
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
                                        <td class="px-6 py-4">
                                            <div class="flex items-center justify-center gap-2">
                                                <button type="button"
                                                        class="btn-product-detail bg-blue-600 hover:bg-blue-700 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all shadow-sm cursor-pointer"
                                                        data-product-id="${p.productId}">
                                                    <i class="fa-solid fa-eye mr-0.5"></i> Xem
                                                </button>
                                                <c:choose>
                                                    <c:when test="${p.approvalStatus == 'PENDING'}">
                                                        <button type="button"
                                                                class="btn-approve bg-[#22c55e] hover:bg-[#16a34a] text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all shadow-sm cursor-pointer"
                                                                data-product-id="${p.productId}"
                                                                data-product-name="<c:out value='${p.name}'/>"
                                                                data-category-id="${p.categoryId}"
                                                                data-is-organic="${p.isOrganic}"
                                                                data-is-imported="${p.isImported}">
                                                            <i class="fa-solid fa-check mr-0.5"></i> Duyệt
                                                        </button>
                                                        <button type="button"
                                                                class="btn-reject bg-[#ef4444] hover:bg-[#dc2626] text-white font-bold px-3 py-1.5 rounded-lg text-xs transition-all shadow-sm cursor-pointer"
                                                                data-product-id="${p.productId}"
                                                                data-product-name="<c:out value='${p.name}'/>">
                                                            <i class="fa-solid fa-xmark mr-0.5"></i> Từ chối
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="inline"
                                                              onsubmit="return confirmBan(event)"
                                                              data-product-name="${fn:escapeXml(p.name)}">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="ban">
                                                            <input type="hidden" name="productId" value="${p.productId}">
                                                            <c:if test="${not empty paramApprovalStatus}">
                                                                <input type="hidden" name="approvalStatus" value="${paramApprovalStatus}">
                                                            </c:if>
                                                            <c:if test="${not empty paramCategoryId}">
                                                                <input type="hidden" name="categoryId" value="${paramCategoryId}">
                                                            </c:if>
                                                            <input type="hidden" name="page" value="${currentPage}">
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

            <c:if test="${totalPages > 1}">
                <div class="px-6 py-4 border-t border-border flex items-center justify-between bg-slate-50/50">
                    <span class="text-xs text-txt-2 font-medium">Trang ${currentPage} / ${totalPages}</span>
                    <ft:pagination current="${currentPage}" total="${totalPages}" baseUrl="${adminProductsPaginationUrl}" />
                </div>
            </c:if>
        </div>
    </main>
</div>

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
            <c:if test="${not empty paramApprovalStatus}">
                <input type="hidden" name="approvalStatus" value="${paramApprovalStatus}">
            </c:if>
            <c:if test="${not empty paramCategoryId}">
                <input type="hidden" name="categoryId" value="${paramCategoryId}">
            </c:if>
            <input type="hidden" name="page" value="${currentPage}">

            <div class="mb-4">
                <p class="text-sm text-txt-2 mb-2">Tên sản phẩm: <b class="text-txt" id="approveProductName"></b></p>
            </div>

            <div class="mb-4">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Nhóm danh mục sản phẩm <span class="text-red-500">*</span></label>
                <select name="categoryId" id="approveCategoryId" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm transition-all" required>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.categoryId}">${cat.name}</option>
                    </c:forEach>
                </select>
            </div>

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

<div id="rejectModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl border border-border">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="font-black text-txt text-base">Từ chối sản phẩm</h3>
            <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('rejectModal')">&times;</button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="p-6">
            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
            <input type="hidden" name="action" value="reject">
            <input type="hidden" name="productId" id="rejectProductId">
            <c:if test="${not empty paramApprovalStatus}">
                <input type="hidden" name="approvalStatus" value="${paramApprovalStatus}">
            </c:if>
            <c:if test="${not empty paramCategoryId}">
                <input type="hidden" name="categoryId" value="${paramCategoryId}">
            </c:if>
            <input type="hidden" name="page" value="${currentPage}">

            <div class="mb-4">
                <p class="text-sm text-txt-2 mb-2">Tên sản phẩm: <b class="text-txt" id="rejectProductName"></b></p>
            </div>

            <div class="mb-5">
                <label class="block text-xs font-bold text-txt-2 mb-1.5 uppercase tracking-wide">Lý do từ chối <span class="text-red-500">*</span></label>
                <textarea name="reason" rows="4" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none p-3 text-sm resize-none" required placeholder="Nhập lý do từ chối..."></textarea>
            </div>

            <button type="submit" class="w-full py-3 bg-[#ef4444] hover:bg-[#dc2626] text-white font-bold rounded-xl text-xs tracking-wider uppercase transition-all shadow-md active:scale-95 cursor-pointer">
                Xác nhận Từ chối
            </button>
        </form>
    </div>
</div>

<div id="productDetailModal" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
    <div class="bg-white rounded-2xl w-full max-w-4xl shadow-2xl border border-border flex flex-col max-h-[90vh]">
        <div class="flex items-center justify-between gap-4 px-6 py-4 border-b border-border">
            <div>
                <h3 class="font-black text-txt text-base flex items-center gap-2">
                    <i class="fa-solid fa-box text-primary"></i>
                    Chi tiết sản phẩm
                </h3>
                <p class="text-xs text-txt-2 mt-1">Xem đầy đủ dữ liệu kiểm duyệt và trạng thái hiển thị công khai.</p>
            </div>
            <div class="flex items-center gap-2">
                <a id="detailProductLink" href="#" target="_blank"
                   class="inline-flex items-center gap-1 bg-primary-lt text-primary hover:text-primary-dk border border-[#bbf7d0] px-3 py-2 rounded-xl text-xs font-bold transition-all">
                    <i class="fa-solid fa-arrow-up-right-from-square"></i> Mở trang chi tiết
                </a>
                <button class="text-txt-3 hover:text-txt text-xl focus:outline-none cursor-pointer" onclick="closeModal('productDetailModal')">&times;</button>
            </div>
        </div>
        <div class="p-6 overflow-y-auto flex-1 space-y-4 text-sm">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div class="bg-slate-50 p-4 rounded-xl border border-border space-y-2">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Thông tin cơ bản</h4>
                    <p><span class="font-semibold text-txt-2">Tên sản phẩm:</span> <span id="detailProductName" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Shop:</span> <span id="detailShopName" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Chủ shop:</span> <span id="detailOwnerName" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Danh mục:</span> <span id="detailCategoryName" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Trạng thái duyệt:</span> <span id="detailApprovalStatus" class="font-bold"></span></p>
                    <p><span class="font-semibold text-txt-2">Trạng thái bán:</span> <span id="detailSaleStatus" class="font-bold"></span></p>
                    <p><span class="font-semibold text-txt-2">Nhãn:</span> <span id="detailLabels" class="inline-flex flex-wrap gap-2"></span></p>
                </div>
                <div class="bg-slate-50 p-4 rounded-xl border border-border space-y-2">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Thống kê và lịch sử</h4>
                    <p><span class="font-semibold text-txt-2">Xuất xứ:</span> <span id="detailOrigin" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Ngày thu hoạch:</span> <span id="detailHarvestDate" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Hạn bảo quản:</span> <span id="detailShelfLife" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Mùa vụ:</span> <span id="detailSeason" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Lượt xem:</span> <span id="detailViewCount" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Đã bán:</span> <span id="detailSoldQuantity" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Đánh giá:</span> <span id="detailRating" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Tạo lúc:</span> <span id="detailCreatedAt" class="text-txt font-medium"></span></p>
                    <p><span class="font-semibold text-txt-2">Cập nhật:</span> <span id="detailUpdatedAt" class="text-txt font-medium"></span></p>
                </div>
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Mô tả</h4>
                    <p id="detailDescription" class="text-txt-2 text-xs leading-relaxed whitespace-pre-line"></p>
                    <div class="mt-4">
                        <h5 class="font-semibold text-txt-2 mb-1">Hướng dẫn bảo quản</h5>
                        <p id="detailStorageInstruction" class="text-xs text-txt-2 whitespace-pre-line"></p>
                    </div>
                </div>
                <div class="bg-slate-50 p-4 rounded-xl border border-border">
                    <h4 class="font-bold text-primary mb-2 border-b border-border pb-1">Giấy tờ và ghi chú</h4>
                    <div id="detailDocs" class="space-y-2"></div>
                    <div class="mt-4">
                        <h5 class="font-semibold text-txt-2 mb-1">Lý do từ chối</h5>
                        <p id="detailRejectionReason" class="text-xs text-rose-600 whitespace-pre-line"></p>
                    </div>
                </div>
            </div>
        </div>
        <div class="px-6 py-4 border-t border-border flex justify-end">
            <button class="bg-white border border-slate-200 text-txt-2 hover:bg-slate-50 font-bold px-4 py-2 rounded-xl text-xs transition-all cursor-pointer"
                    onclick="closeModal('productDetailModal')">
                Đóng
            </button>
        </div>
    </div>
</div>

<script>
    function closeModal(id) {
        document.getElementById(id).classList.add('hidden');
    }

    function openModal(id) {
        document.getElementById(id).classList.remove('hidden');
    }

    function handleJSONResponse(response) {
        const contentType = response.headers.get('content-type');
        if (!response.ok || !contentType || contentType.indexOf('application/json') === -1) {
            if (contentType && contentType.indexOf('application/json') !== -1) {
                return response.json().then(errData => {
                    throw new Error(errData.message || errData.error || ('Lỗi hệ thống (Mã: ' + response.status + ')'));
                });
            }
            throw new Error('Lỗi hệ thống (Mã: ' + response.status + ')');
        }
        return response.json();
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function formatText(value, fallback) {
        const text = value == null ? '' : String(value).trim();
        return text ? text : (fallback || 'Không có');
    }

    function statusBadge(status) {
        const normalized = (status || '').toString().toUpperCase();
        if (normalized === 'APPROVED') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold"><i class="fa-solid fa-circle-check"></i> Đã duyệt</span>';
        if (normalized === 'REJECTED') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-rose-50 border border-rose-100 text-rose-800 text-xs font-bold"><i class="fa-solid fa-circle-xmark"></i> Bị từ chối</span>';
        if (normalized === 'PENDING') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-yellow-50 border border-yellow-100 text-yellow-800 text-xs font-bold"><i class="fa-solid fa-clock"></i> Chờ duyệt</span>';
        return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold">' + escapeHtml(formatText(status, 'Không rõ')) + '</span>';
    }

    function saleStatusBadge(status) {
        const normalized = (status || '').toString().toUpperCase();
        if (normalized === 'ACTIVE') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 border border-emerald-100 text-emerald-800 text-xs font-bold"><i class="fa-solid fa-check"></i> Đang bán</span>';
        if (normalized === 'INACTIVE') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-100 text-amber-800 text-xs font-bold"><i class="fa-solid fa-pause"></i> Tạm dừng</span>';
        if (normalized === 'OUT_OF_SEASON') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold"><i class="fa-solid fa-seedling"></i> Hết mùa</span>';
        if (normalized === 'DELETED') return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-rose-50 border border-rose-100 text-rose-800 text-xs font-bold"><i class="fa-solid fa-trash-can"></i> Đã xóa mềm</span>';
        return '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold">' + escapeHtml(formatText(status, 'Không rõ')) + '</span>';
    }

    function renderProductDetail(product) {
        document.getElementById('detailProductLink').href = '${pageContext.request.contextPath}/products/detail?id=' + product.productId;
        document.getElementById('detailProductName').textContent = formatText(product.name, 'Không có');
        document.getElementById('detailShopName').textContent = formatText(product.shopName, 'Không có');
        document.getElementById('detailOwnerName').textContent = formatText(product.ownerName, 'Không có');
        document.getElementById('detailCategoryName').textContent = formatText(product.categoryName, 'Chưa phân loại');
        document.getElementById('detailApprovalStatus').innerHTML = statusBadge(product.approvalStatus);
        document.getElementById('detailSaleStatus').innerHTML = saleStatusBadge(product.status);
        document.getElementById('detailOrigin').textContent = formatText([product.originCountry, product.originRegion].filter(Boolean).join(' - '), 'Không có');
        document.getElementById('detailHarvestDate').textContent = formatText(product.harvestDate, 'Không có');
        document.getElementById('detailShelfLife').textContent = product.shelfLifeDays == null ? 'Không có' : (product.shelfLifeDays + ' ngày');
        document.getElementById('detailSeason').textContent = formatText(product.seasonLabel, 'Không có');
        document.getElementById('detailViewCount').textContent = (product.viewCount == null ? 0 : product.viewCount) + ' lượt';
        document.getElementById('detailSoldQuantity').textContent = (product.soldQuantity == null ? 0 : product.soldQuantity) + ' sản phẩm';
        document.getElementById('detailRating').textContent = product.rating == null ? 'Chưa có đánh giá' : product.rating;
        document.getElementById('detailCreatedAt').textContent = formatText(product.createdAt, 'Không có');
        document.getElementById('detailUpdatedAt').textContent = formatText(product.updatedAt, 'Không có');
        document.getElementById('detailDescription').textContent = formatText(product.description, 'Không có mô tả.');
        document.getElementById('detailStorageInstruction').textContent = formatText(product.storageInstruction, 'Không có hướng dẫn bảo quản.');
        document.getElementById('detailRejectionReason').textContent = formatText(product.rejectionReason, 'Không có');

        const labels = [];
        if (product.isOrganic) {
            labels.push('<span class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-emerald-100 border border-emerald-200 text-emerald-800 text-[11px] font-bold"><i class="fa-solid fa-leaf"></i> Hữu cơ</span>');
        }
        if (product.isImported) {
            labels.push('<span class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-blue-100 border border-blue-200 text-blue-800 text-[11px] font-bold"><i class="fa-solid fa-globe"></i> Nhập khẩu</span>');
        }
        if (product.inSeason === true) {
            labels.push('<span class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-primary-lt border border-[#bbf7d0] text-primary text-[11px] font-bold"><i class="fa-solid fa-seedling"></i> Đang vào mùa</span>');
        }
        if (labels.length === 0) {
            labels.push('<span class="text-xs text-txt-3">Không có nhãn đặc biệt</span>');
        }
        document.getElementById('detailLabels').innerHTML = labels.join('');

        const docsContainer = document.getElementById('detailDocs');
        if (product.verificationDocPath) {
            const normalizedDocPath = String(product.verificationDocPath).replace(/\\/g, '/').replace(/^\/+/, '');
            const docUrl = '${pageContext.request.contextPath}/' + normalizedDocPath;
            const fileName = normalizedDocPath.split('/').pop();
            docsContainer.innerHTML = '<a href="' + docUrl + '" target="_blank" class="inline-flex items-center gap-2 text-primary hover:text-primary-dk font-bold transition-all text-xs bg-primary-lt border border-[#bbf7d0] px-3 py-2 rounded-lg shadow-sm"><i class="fa-solid fa-file-pdf"></i>' + escapeHtml(fileName) + '</a>';
        } else {
            docsContainer.innerHTML = '<span class="text-xs text-txt-3">Không có giấy tờ kiểm định.</span>';
        }

        document.getElementById('productDetailModal').classList.remove('hidden');
    }

    function showProductDetail(productId) {
        Swal.fire({ title: 'Đang tải...', allowOutsideClick: false, didOpen: () => { Swal.showLoading(); } });
        fetch('${pageContext.request.contextPath}/api/admin/products/detail?id=' + productId)
            .then(handleJSONResponse)
            .then(data => {
                Swal.close();
                if (data.success && data.data) {
                    renderProductDetail(data.data);
                    openModal('productDetailModal');
                } else {
                    Swal.fire('Lỗi', data.message || 'Không thể lấy thông tin sản phẩm.', 'error');
                }
            })
            .catch(error => {
                Swal.close();
                Swal.fire('Lỗi', error.message || 'Lỗi kết nối mạng.', 'error');
            });
    }

    document.querySelectorAll('.btn-product-detail').forEach(function(btn) {
        btn.addEventListener('click', function() {
            showProductDetail(this.dataset.productId);
        });
    });

    document.querySelectorAll('.btn-approve').forEach(function(btn) {
        btn.addEventListener('click', function() {
            document.getElementById('approveProductId').value = this.dataset.productId;
            document.getElementById('approveProductName').innerText = this.dataset.productName;
            document.getElementById('approveCategoryId').value = this.dataset.categoryId;
            document.getElementById('approveOrganic').checked = this.dataset.isOrganic === 'true';
            document.getElementById('approveImported').checked = this.dataset.isImported === 'true';
            openModal('approveModal');
        });
    });

    document.querySelectorAll('.btn-reject').forEach(function(btn) {
        btn.addEventListener('click', function() {
            document.getElementById('rejectProductId').value = this.dataset.productId;
            document.getElementById('rejectProductName').innerText = this.dataset.productName;
            openModal('rejectModal');
        });
    });

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
        if (e.target === document.getElementById('productDetailModal')) closeModal('productDetailModal');
    };
</script>
</body>
</html>
