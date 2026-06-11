<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${promotionTitle} - MetaFruit</title>
    <link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fontawesome.all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/sweetalert2.all.min.js"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#4d661c',
                        'primary-dk': '#364e03',
                        'primary-lt': '#f0f7e6',
                        surface: '#ffffff',
                        'surface-2': '#f8fafc',
                        border: '#e2ece7',
                        txt: '#0f172a',
                        'txt-2': '#475569',
                        'txt-3': '#94a3b8'
                    },
                    fontFamily: {
                        sans: ['Lexend', 'sans-serif']
                    }
                }
            }
        }
    </script>
    <style>
        body { background:#f4fbf7; font-family:'Lexend', sans-serif; }
        .glass-card {
            background:#fff;
            border:1px solid #e2ece7;
            border-radius:1rem;
            box-shadow:0 1px 3px rgba(0,0,0,.05),0 4px 16px -4px rgba(20,83,45,.06);
        }
        tbody tr { transition:background .12s; }
        tbody tr:hover td { background:#f8fafc; }
        
        /* Layout structures to support flex horizontal layout for both admin and shop sidebars */
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-main { flex: 1; display: flex; flex-direction: column; overflow-x: hidden; min-width: 0; }
    </style>
</head>
<body>
<div class="admin-layout">
    <c:choose>
        <c:when test="${promotionMode eq 'GLOBAL'}">
            <jsp:include page="/WEB-INF/jsp/common/admin-sidebar.jsp">
                <jsp:param name="activeMenu" value="promotions"/>
            </jsp:include>
        </c:when>
        <c:otherwise>
            <jsp:include page="/WEB-INF/jsp/common/shop-sidebar.jsp">
                <jsp:param name="activePage" value="promotions"/>
            </jsp:include>
        </c:otherwise>
    </c:choose>

    <main class="admin-main p-6 md:p-8 overflow-y-auto">
        <div class="flex items-center justify-between bg-gradient-to-r from-[#f0faf3] to-[#dcfce7] border border-[#bbf7d0]/60 p-6 rounded-2xl shadow-sm mb-8">
            <div>
                <h1 class="text-xl md:text-2xl font-extrabold text-[#364e03] tracking-tight">${promotionTitle}</h1>
                <p class="text-[#475569] text-xs md:text-sm mt-1">${promotionDescription}</p>
            </div>
            <div class="hidden md:flex items-center gap-2 bg-[#ffffff]/80 border border-[#bbf7d0]/80 px-4 py-2 rounded-xl text-[#364e03] shadow-sm">
                <i class="fa-solid fa-ticket text-amber-500"></i>
                <span class="text-xs font-bold uppercase tracking-wider">${promotionBadge}</span>
            </div>
        </div>

        <jsp:include page="/WEB-INF/jsp/common/alert.jsp" />

        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
            <div class="glass-card xl:col-span-1 p-6">
                <h2 class="text-lg font-extrabold text-txt mb-5">
                    <c:choose>
                        <c:when test="${not empty editPromotion}">Sửa voucher</c:when>
                        <c:otherwise>Tạo voucher mới</c:otherwise>
                    </c:choose>
                </h2>
                <form action="${pageContext.request.contextPath}${promotionBasePath}" method="POST" class="space-y-4">
                    <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                    <input type="hidden" name="action" value="${not empty editPromotion ? 'update' : 'save'}">
                    <c:if test="${not empty editPromotion}">
                        <input type="hidden" name="promoId" value="${editPromotion.promoId}">
                    </c:if>
                    <c:if test="${not empty promotionFixedScope}">
                        <input type="hidden" name="discountScope" value="${promotionFixedScope}">
                    </c:if>

                    <div>
                        <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Mã voucher</label>
                        <input name="code" value="${not empty editPromotion ? editPromotion.code : ''}" required
                               class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white uppercase">
                    </div>

                    <div class="grid grid-cols-2 gap-3">
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Kiểu giảm</label>
                            <select name="discountType" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                                <option value="PERCENT" ${not empty editPromotion && editPromotion.discountType == 'PERCENT' ? 'selected' : ''}>PERCENT</option>
                                <option value="FIXED" ${not empty editPromotion && editPromotion.discountType == 'FIXED' ? 'selected' : ''}>FIXED</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Phạm vi voucher</label>
                            <c:choose>
                                <c:when test="${not empty promotionFixedScope}">
                                    <input value="${promotionFixedScope eq 'SHOP' ? 'Voucher shop' : 'Voucher sàn'}" readonly
                                           class="w-full rounded-xl border border-slate-300 bg-slate-100 outline-none px-4 py-2 text-sm text-txt-2">
                                </c:when>
                                <c:otherwise>
                                    <select name="discountScope" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                                        <option value="SHOP" ${not empty editPromotion && editPromotion.discountScope == 'SHOP' ? 'selected' : ''}>Voucher shop</option>
                                        <option value="ALL" ${not empty editPromotion && editPromotion.discountScope == 'ALL' ? 'selected' : ''}>Voucher sàn</option>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-3">
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Phạm vi áp dụng</label>
                            <select name="scope" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                                <option value="ORDER" ${not empty editPromotion && editPromotion.scope == 'ORDER' ? 'selected' : ''}>Đơn hàng</option>
                                <option value="PRODUCT" ${not empty editPromotion && editPromotion.scope == 'PRODUCT' ? 'selected' : ''}>Sản phẩm</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Sản phẩm áp dụng</label>
                            <select name="productId" class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                                <option value="">Tất cả sản phẩm</option>
                                <c:forEach var="product" items="${products}">
                                    <option value="${product.productId}" ${not empty editPromotion && editPromotion.productId == product.productId ? 'selected' : ''}>
                                        #${product.productId} - <c:out value="${product.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-3">
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Giá trị voucher</label>
                            <input name="discountValue" type="number" min="0" step="0.01"
                                   value="${not empty editPromotion ? editPromotion.discountValue : ''}" required
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Giảm tối đa</label>
                            <input name="discountMax" type="number" min="0" step="0.01"
                                   value="${not empty editPromotion ? editPromotion.discountMax : 0}"
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-3">
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Đơn tối thiểu</label>
                            <input name="minOrderValue" type="number" min="0" step="0.01"
                                   value="${not empty editPromotion ? editPromotion.minOrderValue : 0}"
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Số lượt dùng</label>
                            <input name="maxUses" type="number" min="0" step="1"
                                   value="${not empty editPromotion && not empty editPromotion.maxUses ? editPromotion.maxUses : ''}"
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-3">
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Bắt đầu</label>
                            <input name="validFrom" type="datetime-local" value="${not empty editPromotion ? editValidFrom : ''}" required
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-txt-2 uppercase tracking-wider mb-1">Kết thúc</label>
                            <input name="validUntil" type="datetime-local" value="${not empty editPromotion ? editValidUntil : ''}" required
                                   class="w-full rounded-xl border border-slate-300 focus:border-primary focus:ring-2 focus:ring-primary/15 outline-none px-4 py-2 text-sm bg-white">
                        </div>
                    </div>

                    <div class="flex items-center justify-between gap-3">
                        <label class="inline-flex items-center gap-2 text-sm text-txt-2">
                            <input type="checkbox" name="canStack" ${not empty editPromotion && editPromotion.canStack ? 'checked' : ''} class="rounded border-slate-300 text-primary focus:ring-primary">
                            Có thể cộng dồn
                        </label>
                        <label class="inline-flex items-center gap-2 text-sm text-txt-2">
                            <input type="checkbox" name="isActive" ${empty editPromotion || editPromotion.isActive ? 'checked' : ''} class="rounded border-slate-300 text-primary focus:ring-primary">
                            Đang hoạt động
                        </label>
                    </div>

                    <div class="flex items-center gap-2 pt-2">
                        <button type="submit" class="bg-primary text-white hover:bg-primary-dk font-bold px-5 py-2.5 rounded-xl text-sm transition-all shadow-sm active:scale-95">
                            <c:choose>
                                <c:when test="${not empty editPromotion}">Cập nhật</c:when>
                                <c:otherwise>Tạo mới</c:otherwise>
                            </c:choose>
                        </button>
                        <c:if test="${not empty editPromotion}">
                            <a href="${pageContext.request.contextPath}${promotionBasePath}" class="bg-white border border-border hover:bg-slate-100 text-txt-2 font-bold px-4 py-2.5 rounded-xl text-sm transition-all">
                                Hủy sửa
                            </a>
                        </c:if>
                    </div>
                </form>
            </div>

            <div class="glass-card xl:col-span-2 overflow-hidden">
                <div class="px-6 py-4 border-b border-border bg-slate-50/50 flex items-center justify-between">
                    <div>
                        <h2 class="font-extrabold text-txt">Danh sách voucher</h2>
                        <p class="text-xs text-txt-3 mt-1">Tổng số: ${promotions.size()}</p>
                    </div>
                </div>

                <div class="overflow-x-auto pb-24">
                    <table class="w-full text-left border-collapse text-sm">
                        <thead>
                        <tr class="bg-slate-50/50">
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Mã voucher</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Áp dụng</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Mức giảm</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2">Thời gian</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2 text-center">Trạng thái</th>
                            <th class="p-3 border-b border-border font-semibold text-txt-2 text-center">Hành động</th>
                        </tr>
                        </thead>
                        <tbody class="divide-y divide-border/60">
                        <c:choose>
                            <c:when test="${empty promotions}">
                                <tr>
                                    <td colspan="6" class="p-8 text-center text-txt-3 italic">Chưa có voucher nào.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${promotions}">
                                    <tr class="hover:bg-primary-lt/20">
                                        <td class="p-3">
                                            <div class="font-mono font-bold text-primary">${p.code}</div>
                                            <div class="text-[11px] text-txt-3">#${p.promoId}</div>
                                        </td>
                                        <td class="p-3 text-xs text-txt-2">
                                            <div class="font-semibold text-txt">
                                                <c:choose>
                                                    <c:when test="${p.discountType == 'PERCENT'}">Giảm theo %</c:when>
                                                    <c:otherwise>Giảm theo số tiền</c:otherwise>
                                                </c:choose>
                                                /
                                                <c:choose>
                                                    <c:when test="${p.discountScope == 'SHOP'}">Voucher shop</c:when>
                                                    <c:otherwise>Voucher sàn</c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div>
                                                <c:choose>
                                                    <c:when test="${p.scope == 'ORDER'}">Đơn hàng</c:when>
                                                    <c:otherwise>Sản phẩm</c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="text-[11px] text-txt-3">
                                                <c:choose>
                                                    <c:when test="${p.productId != null}">Product #${p.productId}</c:when>
                                                    <c:otherwise>Áp dụng cho toàn bộ phù hợp</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td class="p-3 text-xs text-txt-2">
                                            <div>Giảm: <span class="font-bold text-txt">${p.discountValue}</span></div>
                                            <div>Max: <span class="font-bold text-txt">${p.discountMax}</span></div>
                                            <div>Min order: <span class="font-bold text-txt">${p.minOrderValue}</span></div>
                                        </td>
                                        <td class="p-3 text-xs text-txt-2 whitespace-nowrap">
                                            <div>Từ: ${p.validFrom}</div>
                                            <div>Đến: ${p.validUntil}</div>
                                        </td>
                                        <td class="p-3 text-center">
                                            <c:choose>
                                                <c:when test="${p.isActive && !p.isDeleted}">
                                                    <span class="px-2.5 py-1 bg-emerald-100 text-emerald-800 rounded-full text-xs font-bold border border-emerald-200">Active</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="px-2.5 py-1 bg-slate-100 text-slate-700 rounded-full text-xs font-bold border border-slate-200">Paused</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="p-3 text-center">
                                            <div class="relative inline-block text-left dropdown">
                                                <button type="button" onclick="toggleDropdown(event, 'dropdown-${p.promoId}')" class="inline-flex justify-center w-8 h-8 rounded-full text-[#475569] hover:bg-slate-100 flex items-center justify-center transition-all border-0 bg-transparent cursor-pointer">
                                                    <i class="fa-solid fa-ellipsis-vertical"></i>
                                                </button>
                                                <div id="dropdown-${p.promoId}" class="origin-top-right absolute right-0 mt-1 w-32 rounded-xl shadow-lg bg-white ring-1 ring-black ring-opacity-5 divide-y divide-gray-100 focus:outline-none hidden z-30 border border-border">
                                                    <div class="py-1">
                                                        <a href="${pageContext.request.contextPath}${promotionBasePath}?editId=${p.promoId}" class="group flex items-center px-4 py-2 text-xs text-txt-2 hover:bg-slate-50 hover:text-primary font-bold transition-all text-decoration-none">
                                                            <i class="fa-solid fa-pen mr-2 text-slate-400 group-hover:text-primary"></i> Sửa
                                                        </a>
                                                        <form action="${pageContext.request.contextPath}${promotionBasePath}" method="POST" class="m-0">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="toggle">
                                                            <input type="hidden" name="promoId" value="${p.promoId}">
                                                            <button type="submit" class="group flex w-full items-center px-4 py-2 text-xs text-txt-2 hover:bg-slate-50 hover:text-amber-600 font-bold text-left transition-all border-0 bg-transparent cursor-pointer">
                                                                <i class="fa-solid fa-power-off mr-2 text-slate-400 group-hover:text-amber-600"></i> ${p.isActive ? 'Tắt' : 'Bật'}
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}${promotionBasePath}" method="POST" class="m-0" onsubmit="return confirmDelete(event, '${p.code}')">
                                                            <input type="hidden" name="_csrf" value="${sessionScope._csrfToken}">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="promoId" value="${p.promoId}">
                                                            <button type="submit" class="group flex w-full items-center px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 font-bold text-left transition-all border-0 bg-transparent cursor-pointer">
                                                                <i class="fa-solid fa-trash mr-2 text-rose-400 group-hover:text-rose-600"></i> Xóa
                                                            </button>
                                                        </form>
                                                    </div>
                                                </div>
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

<script>
    function confirmDelete(event, code) {
        event.preventDefault();
        Swal.fire({
            title: 'Xóa mềm khuyến mãi?',
            text: 'Mã ' + code + ' sẽ bị ẩn khỏi hệ thống nhưng vẫn giữ lịch sử sử dụng.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then(r => {
            if (r.isConfirmed) {
                event.target.submit();
            }
        });
        return false;
    }

    function toggleDropdown(event, id) {
        event.stopPropagation();
        // Close all other dropdowns
        document.querySelectorAll('[id^="dropdown-"]').forEach(el => {
            if (el.id !== id) el.classList.add('hidden');
        });
        const dropdown = document.getElementById(id);
        dropdown.classList.toggle('hidden');
    }

    // Close dropdowns when clicking outside
    document.addEventListener('click', function(event) {
        document.querySelectorAll('[id^="dropdown-"]').forEach(el => {
            el.classList.add('hidden');
        });
    });
</script>
</body>
</html>
