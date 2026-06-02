<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tld/fruitmkt.tld" %>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="Quản lý sản phẩm - Kênh người bán"/>
</jsp:include>

<!-- Google Fonts & Tailwind isolated engine to match Premium HomeUI -->
<link href="https://fonts.googleapis.com" rel="preconnect">
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">

<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    "primary": "#4d661c",          // Emerald Green
                    "primary-hover": "#364e03",
                    "primary-light": "#d9f99d",
                    "secondary": "#31694b",        // Deep Forest Green
                    "surface-bright": "#eaffea",   // Warm Mint Light
                    "surface-container-low": "#d1ffd8",
                    "on-surface": "#00210d",
                    "on-surface-variant": "#44483b",
                    "tertiary": "#486554",
                    "tertiary-container": "#d5f5e0"
                },
                fontFamily: {
                    sans: ["Lexend", "sans-serif"]
                }
            }
        }
    }
</script>

<style>
    .glass-card {
        background: rgba(255, 255, 255, 0.75);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.4);
    }
    .ambient-shadow {
        box-shadow: 0 10px 40px rgba(20, 83, 45, 0.05);
    }
    .navbar {
        font-family: 'Lexend', sans-serif !important;
    }
    /* Simple dynamic fadeout row style */
    .row-fadeout {
        transition: all 0.5s ease-out;
        opacity: 0;
        transform: scale(0.95) translateY(10px);
    }
</style>

<!-- Main Wrapper -->
<div class="bg-gradient-to-br from-surface-bright via-white to-surface-container-low min-h-screen text-on-surface antialiased font-sans pt-24 pb-20">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <!-- Breadcrumb & Header Title -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
            <div>
                <div class="flex items-center gap-2 text-xs text-on-surface-variant/70 mb-2">
                    <a href="${pageContext.request.contextPath}/shop/dashboard" class="hover:text-primary transition-colors">Kênh người bán</a>
                    <span class="material-symbols-outlined text-[12px]">chevron_right</span>
                    <span class="font-semibold text-primary">Sản phẩm</span>
                </div>
                <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight">📦 QUẢN LÝ SẢN PHẨM CỬA HÀNG</h1>
                <p class="text-xs text-on-surface-variant font-light mt-1">Xem, thêm mới, sửa thông tin, ẩn/hiện hoặc xóa các sản phẩm hoa quả sạch của bạn.</p>
            </div>
            
            <a href="${pageContext.request.contextPath}/shop/product-create" 
               class="bg-primary hover:bg-primary-hover text-white font-semibold text-sm px-5 py-3 rounded-2xl transition-all shadow-md hover:shadow-lg flex items-center gap-2 active:scale-95 duration-200 cursor-pointer">
                <span class="material-symbols-outlined text-[18px]">add_box</span>
                <span>Thêm sản phẩm mới</span>
            </a>
        </div>

        <!-- Dynamic Flash Message Alert -->
        <c:if test="${not empty sessionScope.flashMsg}">
            <div id="flash-alert-container" class="mb-6 p-4 rounded-2xl glass-card flex items-center gap-3 border-l-4 ${sessionScope.flashType == 'success' ? 'border-primary text-secondary' : 'border-red-500 text-red-700'} shadow-sm animate-pulse">
                <span class="material-symbols-outlined text-[24px]">
                    <c:choose>
                        <c:when test="${sessionScope.flashType == 'success'}">check_circle</c:when>
                        <c:otherwise>error</c:otherwise>
                    </c:choose>
                </span>
                <div class="flex-grow font-semibold text-sm"><c:out value="${sessionScope.flashMsg}"/></div>
                <button onclick="document.getElementById('flash-alert-container').remove();" class="text-on-surface-variant/60 hover:text-on-surface">
                    <span class="material-symbols-outlined text-[18px]">close</span>
                </button>
            </div>
            <%
                // Clear flash messages after displaying
                session.removeAttribute("flashMsg");
                session.removeAttribute("flashType");
            %>
        </c:if>

        <!-- Product Table Grid -->
        <div class="glass-card rounded-3xl overflow-hidden ambient-shadow bg-white/70">
            <c:choose>
                <c:when test="${empty products}">
                    <div class="text-center py-20 px-4">
                        <span class="material-symbols-outlined text-[64px] text-primary/30 animate-bounce mb-4">box_edit</span>
                        <h3 class="text-lg font-bold">Cửa hàng của bạn chưa có sản phẩm nào</h3>
                        <p class="text-xs text-on-surface-variant/80 mt-1 max-w-md mx-auto font-light">Bắt đầu đưa trái cây sạch, hữu cơ tươi ngon của bạn tiếp cận hàng ngàn khách hàng bằng cách bấm nút thêm sản phẩm!</p>
                        <a href="${pageContext.request.contextPath}/shop/product-create" 
                           class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold px-6 py-3 rounded-full mt-6 transition-all shadow-sm">
                            <span class="material-symbols-outlined text-[16px]">add</span>
                            Thêm sản phẩm đầu tiên
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="overflow-x-auto">
                        <table class="w-full text-left border-collapse">
                            <thead>
                                <tr class="bg-primary/5 text-primary text-xs font-bold uppercase tracking-wider border-b border-primary/10">
                                    <th class="py-4 px-6">Sản phẩm</th>
                                    <th class="py-4 px-4">Danh mục</th>
                                    <th class="py-4 px-4">Giá bán</th>
                                    <th class="py-4 px-4 text-center">Tồn kho</th>
                                    <th class="py-4 px-4 text-center">Tương tác</th>
                                    <th class="py-4 px-4 text-center">Trạng thái bán</th>
                                    <th class="py-4 px-6 text-right">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-gray-100 text-sm">
                                <c:forEach var="item" items="${products}">
                                    <tr id="product-row-${item.productId}" class="hover:bg-white/40 transition-colors duration-150">
                                        <!-- Product Detail Thumbnail & Origin -->
                                        <td class="py-4 px-6 flex items-center gap-4">
                                            <div class="w-12 h-12 rounded-xl overflow-hidden bg-emerald-50 border border-gray-150 shrink-0 shadow-sm relative group">
                                                <img src="${item.image}" alt="${item.name}" 
                                                     class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                                                     onerror="this.src='https://images.unsplash.com/photo-1610832958506-ee5633619144?w=100&auto=format&fit=crop&q=80'">
                                            </div>
                                            <div>
                                                <h4 class="font-bold text-on-surface line-clamp-1 hover:text-primary transition-colors cursor-pointer" 
                                                    onclick="window.location.href='${pageContext.request.contextPath}/products/detail?id=${item.productId}'">
                                                    <c:out value="${item.name}"/>
                                                </h4>
                                                <div class="flex items-center gap-1.5 text-xs text-on-surface-variant/60 font-light mt-0.5">
                                                    <span class="material-symbols-outlined text-[14px] text-primary/70">location_on</span>
                                                    <span><c:out value="${item.originRegion}"/>, <c:out value="${item.originCountry}"/></span>
                                                </div>
                                            </div>
                                        </td>
                                        
                                        <!-- Category -->
                                        <td class="py-4 px-4 text-on-surface-variant/80 font-medium">
                                            <c:out value="${item.categoryName}"/>
                                        </td>
                                        
                                        <!-- Price -->
                                        <td class="py-4 px-4 font-bold text-primary">
                                            <ft:currency value="${item.price}"/>
                                            <span class="text-[10px] text-on-surface-variant/50 font-normal"> / <c:out value="${item.unit}"/></span>
                                        </td>
                                        
                                        <!-- Stock Status Badge -->
                                        <td class="py-4 px-4 text-center">
                                            <c:choose>
                                                <c:when test="${item.stock > 10}">
                                                    <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 shadow-sm">
                                                        ${item.stock} ${item.unit}
                                                    </span>
                                                </c:when>
                                                <c:when test="${item.stock > 0}">
                                                    <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 shadow-sm">
                                                        Còn ít: ${item.stock}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800 shadow-sm animate-pulse">
                                                        Hết hàng
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        
                                        <!-- Interactive counts -->
                                        <td class="py-4 px-4 text-center text-xs text-on-surface-variant/70 font-light">
                                            <div class="flex flex-col items-center">
                                                <span class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">visibility</span> Lượt xem: ${item.viewCount}</span>
                                                <span class="flex items-center gap-1 mt-1 font-semibold text-secondary"><span class="material-symbols-outlined text-[14px]">shopping_cart</span> Đã bán: ${item.soldQuantity}</span>
                                            </div>
                                        </td>

                                        <!-- AJAX Toggle Sale Switch -->
                                        <td class="py-4 px-4 text-center">
                                            <div class="flex items-center justify-center">
                                                <label class="relative inline-flex items-center cursor-pointer select-none">
                                                    <input type="checkbox" id="toggle-status-${item.productId}" 
                                                           class="sr-only peer" 
                                                           ${item.status == 'ACTIVE' ? 'checked' : ''} 
                                                           onchange="toggleSaleStatus(${item.productId}, this)">
                                                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary shadow-inner"></div>
                                                </label>
                                            </div>
                                        </td>
                                        
                                        <!-- Actions (Edit/Delete) -->
                                        <td class="py-4 px-6 text-right">
                                            <div class="inline-flex items-center gap-2">
                                                <!-- Edit button -->
                                                <a href="${pageContext.request.contextPath}/shop/product-edit?id=${item.productId}" 
                                                   class="w-9 h-9 rounded-xl border border-primary/20 bg-white text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer" 
                                                   title="Sửa thông tin">
                                                    <span class="material-symbols-outlined text-[18px]">edit</span>
                                                </a>
                                                <!-- Delete button -->
                                                <button onclick="confirmSoftDelete(${item.productId}, '<c:out value="${item.name}"/>')" 
                                                        class="w-9 h-9 rounded-xl border border-red-200 bg-white text-red-600 hover:bg-red-600 hover:text-white transition-all shadow-sm active:scale-90 flex items-center justify-center cursor-pointer" 
                                                        title="Xóa mềm">
                                                    <span class="material-symbols-outlined text-[18px]">delete</span>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </div>
</div>

<!-- AJAX Callouts & SweetAlert style modal -->
<script>
    /**
     * AJAX Toggle switch for sales display status
     */
    function toggleSaleStatus(productId, checkbox) {
        const isChecked = checkbox.checked;
        const newStatus = isChecked ? 'ACTIVE' : 'INACTIVE';
        
        // Prevent clicking while requesting
        checkbox.disabled = true;

        const params = new URLSearchParams();
        params.append('action', 'toggle');
        params.append('productId', productId);
        params.append('status', newStatus);

        fetch('${pageContext.request.contextPath}/shop/product-status', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-Token': '${sessionScope._csrfToken}'
            },
            body: params
        })
        .then(response => response.json())
        .then(data => {
            checkbox.disabled = false;
            if (data.success) {
                // Show dynamic mini toast style alert on header if needed
                console.log(`Product ${productId} toggled to ${newStatus} successfully.`);
            } else {
                checkbox.checked = !isChecked; // Rollback check state
                alert("Lỗi: " + (data.message || "Không thể cập nhật trạng thái."));
            }
        })
        .catch(err => {
            checkbox.disabled = false;
            checkbox.checked = !isChecked; // Rollback
            console.error(err);
            alert("Lỗi kết nối máy chủ khi cập nhật trạng thái.");
        });
    }

    /**
     * Show custom modal to confirm soft delete
     */
    function confirmSoftDelete(productId, productName) {
        const confirmation = confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${productName}"?\nLưu ý: Sản phẩm sẽ bị ẩn khỏi gian hàng nhưng các đơn hàng cũ vẫn hiển thị bình thường.`);
        if (confirmation) {
            executeSoftDelete(productId);
        }
    }

    /**
     * AJAX Soft Delete
     */
    function executeSoftDelete(productId) {
        const row = document.getElementById(`product-row-${productId}`);
        if (!row) return;

        const params = new URLSearchParams();
        params.append('action', 'delete');
        params.append('productId', productId);

        fetch('${pageContext.request.contextPath}/shop/product-status', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-Token': '${sessionScope._csrfToken}'
            },
            body: params
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Fade out row beautifully with micro-animations
                row.classList.add('row-fadeout');
                setTimeout(() => {
                    row.remove();
                    // If no rows left, reload window to show empty state
                    const tbody = document.querySelector('tbody');
                    if (tbody && tbody.children.length === 0) {
                        window.location.reload();
                    }
                }, 500);
            } else {
                alert("Lỗi: " + (data.message || "Không thể xóa sản phẩm."));
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi kết nối máy chủ khi thực hiện xóa.");
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
