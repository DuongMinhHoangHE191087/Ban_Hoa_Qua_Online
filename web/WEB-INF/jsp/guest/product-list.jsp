<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Sản Phẩm - MetaFruit"/></jsp:include>

<div class="shop-container" style="max-width: 1200px; margin: 30px auto; padding: 0 15px; font-family: 'Plus Jakarta Sans', sans-serif; display: grid; grid-template-columns: 280px 1fr; gap: 30px;">
    
    <!-- SIDEBAR FILTERS (II.16 - II.20) -->
    <aside style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid rgba(20, 83, 45, 0.08); height: fit-content;">
        <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin-top: 0; margin-bottom: 20px; border-bottom: 2px solid rgba(20, 83, 45, 0.08); padding-bottom: 10px; display: flex; align-items: center; gap: 8px;">
            <i class="fa-solid fa-filter"></i> Bộ lọc tìm kiếm
        </h3>
        
        <form id="filterForm" action="${pageContext.request.contextPath}/products" method="get">
            
            <!-- II.16: Categories Checkboxes -->
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 10px;">Danh mục trái cây</label>
                <div style="max-height: 180px; overflow-y: auto; padding-right: 5px;">
                    <c:forEach var="cat" items="${categories}">
                        <label style="display: flex; align-items: center; cursor: pointer; color: #4b5563; font-size: 14px; margin-bottom: 8px; user-select: none;">
                            <input type="checkbox" name="categoryIds" value="${cat.categoryId}" 
                                   ${categoryIds != null && categoryIds.contains(cat.categoryId) ? 'checked' : ''}
                                   style="margin-right: 10px; accent-color: #14532d; width: 16px; height: 16px;" />
                            <c:out value="${cat.name}"/>
                        </label>
                    </c:forEach>
                </div>
            </div>

            <!-- II.17: Price Range inputs -->
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 10px;">Khoảng giá (VNĐ)</label>
                <div style="display: flex; gap: 10px; align-items: center;">
                    <input type="number" id="minPriceInput" name="minPrice" placeholder="Từ" value="${minPrice}" min="0" step="5000"
                           style="width: 100%; padding: 8px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 13px; text-align: center;" />
                    <span style="color: #9ca3af;">-</span>
                    <input type="number" id="maxPriceInput" name="maxPrice" placeholder="Đến" value="${maxPrice}" min="0" step="5000"
                           style="width: 100%; padding: 8px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 13px; text-align: center;" />
                </div>
            </div>

            <!-- II.18: Average Rating Star Select -->
            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 10px;">Đánh giá chất lượng</label>
                <select name="rating" style="width: 100%; padding: 10px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 14px; background: white; cursor: pointer; color: #4b5563;">
                    <option value="">Tất cả sao</option>
                    <option value="4.0" ${rating == 4.0 ? 'selected' : ''}>4★ trở lên</option>
                    <option value="3.0" ${rating == 3.0 ? 'selected' : ''}>3★ trở lên</option>
                    <option value="2.0" ${rating == 2.0 ? 'selected' : ''}>2★ trở lên</option>
                </select>
            </div>

            <!-- II.19: Availability check -->
            <div style="margin-bottom: 25px;">
                <label style="display: flex; align-items: center; cursor: pointer; color: #374151; font-weight: 600; font-size: 14px; user-select: none;">
                    <input type="checkbox" name="inStockOnly" value="true" ${inStockOnly ? 'checked' : ''}
                           style="margin-right: 10px; accent-color: #14532d; width: 16px; height: 16px;" />
                    Chỉ hiện sản phẩm còn hàng
                </label>
            </div>

            <!-- II.20: Sorting -->
            <div style="margin-bottom: 25px;">
                <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 10px;">Sắp xếp theo</label>
                <select name="sortBy" style="width: 100%; padding: 10px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 14px; background: white; cursor: pointer; color: #4b5563;">
                    <option value="newest" ${'newest'.equals(sortBy) ? 'selected' : ''}>Mới nhất</option>
                    <option value="price_asc" ${'price_asc'.equals(sortBy) ? 'selected' : ''}>Giá: Thấp đến Cao</option>
                    <option value="price_desc" ${'price_desc'.equals(sortBy) ? 'selected' : ''}>Giá: Cao đến Thấp</option>
                    <option value="rating" ${'rating'.equals(sortBy) ? 'selected' : ''}>Đánh giá cao nhất</option>
                </select>
            </div>

            <button type="submit" style="width: 100%; background-color: #14532d; color: white; border: none; padding: 12px; border-radius: 10px; font-weight: 700; font-size: 14px; cursor: pointer; box-shadow: 0 4px 10px rgba(20,83,45,0.15); transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#166534'" onmouseout="this.style.backgroundColor='#14532d'">
                <i class="fa-solid fa-circle-check"></i> Áp dụng bộ lọc
            </button>
        </form>

        <!-- II.22 Best Sellers Sidebar Block -->
        <c:if test="${not empty bestSellers}">
            <div style="margin-top: 35px; border-top: 2px solid rgba(20,83,45,0.08); padding-top: 20px;">
                <h4 style="color: #14532d; font-size: 16px; font-weight: 700; margin-top: 0; margin-bottom: 15px; display: flex; align-items: center; gap: 8px;">
                    <i class="fa-solid fa-fire" style="color: #ef4444;"></i> Bán Chạy Nhất (II.22)
                </h4>
                <div style="display: flex; flex-direction: column; gap: 15px;">
                    <c:forEach var="bs" items="${bestSellers}">
                        <a href="${pageContext.request.contextPath}/products/detail?id=${bs.productId}" style="display: flex; gap: 10px; text-decoration: none; color: inherit; align-items: center;">
                            <img src="${pageContext.request.contextPath}/assets/img/logo-leaf.png" alt="fruit" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover; background-color: #f0fdf4; border: 1px solid #dcfce7;" />
                            <div>
                                <h5 style="margin: 0; font-size: 13px; font-weight: 600; color: #1f2937; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical;"><c:out value="${bs.name}"/></h5>
                                <div style="display: flex; align-items: center; gap: 5px; margin-top: 3px;">
                                    <span style="color: #ef4444; font-size: 12px; font-weight: 700;"><c:out value="${bs.soldQuantity}"/> đã bán</span>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </c:if>
    </aside>

    <!-- PRODUCT DISPLAY GRID AREA -->
    <main>
        <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid rgba(20, 83, 45, 0.08); margin-bottom: 30px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <span style="color: #4b5563; font-size: 15px;">Tìm thấy <strong style="color: #14532d; font-size: 18px;" id="countBadge">${pagedResult.totalItems}</strong> sản phẩm</span>
            </div>

            <!-- GRID ROW -->
            <div id="productGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px;">
                <c:forEach var="p" items="${pagedResult.items}">
                    
                    <div class="product-card" style="background: white; border-radius: 12px; border: 1px solid #f3f4f6; overflow: hidden; position: relative; transition: all 0.3s; box-shadow: 0 2px 10px rgba(0,0,0,0.015); display: flex; flex-direction: column;">
                        
                        <!-- II.9 Badges badging overlay -->
                        <c:if test="${not empty p.labelType}">
                            <span style="position: absolute; top: 12px; left: 12px; z-index: 10; padding: 4px 10px; border-radius: 20px; color: white; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;
                                         background-color: ${'Organic'.equals(p.labelType) ? '#16a34a' : '#2563eb'};">
                                <c:out value="${p.labelType}"/>
                            </span>
                        </c:if>

                        <a href="${pageContext.request.contextPath}/products/detail?id=${p.productId}" style="text-decoration: none; color: inherit; display: flex; flex-direction: column; height: 100%;">
                            <div style="height: 180px; background-color: #f9fafb; display: flex; align-items: center; justify-content: center; position: relative;">
                                <img src="${pageContext.request.contextPath}/assets/img/logo-leaf.png" alt="Fruit" style="max-height: 100px; max-width: 100px; object-fit: contain;" />
                            </div>
                            
                            <div style="padding: 15px; display: flex; flex-direction: column; flex-grow: 1;">
                                <h3 style="font-size: 15px; font-weight: 700; color: #1f2937; margin: 0 0 8px 0; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: 38px;">
                                    <c:out value="${p.name}"/>
                                </h3>
                                
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto;">
                                    <div>
                                        <div style="color: #4b5563; font-size: 11px;">Đánh giá</div>
                                        <ft:stars rating="${p.rating}" />
                                    </div>
                                    <span style="background-color: #f0fdf4; color: #166534; font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 20px;">
                                        Xem chi tiết
                                    </span>
                                </div>
                            </div>
                        </a>
                    </div>
                </c:forEach>
            </div>

            <!-- II.16: If no products found -->
            <div id="noProductsAlert" style="display: ${empty pagedResult.items ? 'block' : 'none'}; text-align: center; padding: 40px 20px; color: #6b7280;">
                <i class="fa-solid fa-basket-shopping" style="font-size: 48px; color: #d1d5db; margin-bottom: 15px;"></i>
                <p style="font-size: 16px; margin: 0;">Không tìm thấy sản phẩm nào khớp với bộ lọc của bạn.</p>
            </div>

            <!-- PAGINATION BLOCK -->
            <div id="paginationBlock" style="margin-top: 30px; border-top: 1px solid #f3f4f6; padding-top: 20px;">
                <ft:pagination current="${pagedResult.currentPage}" total="${pagedResult.totalPages}" baseUrl="${pageContext.request.contextPath}/products" />
            </div>
        </div>

        <!-- II.23 Recently Viewed Block -->
        <c:if test="${not empty recentlyViewed}">
            <div style="background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid rgba(20, 83, 45, 0.08);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1.5px solid rgba(20,83,45,0.08); padding-bottom: 10px;">
                    <h3 style="color: #14532d; font-size: 18px; font-weight: 700; margin: 0; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-clock-rotate-left"></i> Trái Cây Bạn Đã Xem Gần Đây (II.23)
                    </h3>
                    <button id="clearRecentlyViewedBtn" style="background: none; border: none; color: #ef4444; font-size: 12px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 4px;" onclick="clearRecentlyViewedCookie()">
                        <i class="fa-solid fa-trash-can"></i> Xóa lịch sử
                    </button>
                </div>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 15px;">
                    <c:forEach var="rv" items="${recentlyViewed}">
                        <a href="${pageContext.request.contextPath}/products/detail?id=${rv.productId}" style="background: #f9fafb; border-radius: 10px; padding: 10px; border: 1px solid #f3f4f6; text-decoration: none; color: inherit; display: flex; flex-direction: column; align-items: center; transition: all 0.2s;">
                            <div style="height: 100px; display: flex; align-items: center; justify-content: center; margin-bottom: 8px;">
                                <img src="${pageContext.request.contextPath}/assets/img/logo-leaf.png" alt="fruit" style="max-height: 60px;" />
                            </div>
                            <h5 style="margin: 0; font-size: 12px; font-weight: 700; color: #374151; text-align: center; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;"><c:out value="${rv.name}"/></h5>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </c:if>
    </main>
</div>

<!-- AJAX AUTOMATIC LIVE UPDATES LOGIC script -->
<script>
document.addEventListener("DOMContentLoaded", function() {
    const filterForm = document.getElementById("filterForm");
    
    // Automatically trigger AJAX query on input changes
    const inputs = filterForm.querySelectorAll("input[type='checkbox'], select, input[type='number']");
    inputs.forEach(input => {
        input.addEventListener("change", function() {
            triggerFilterAjax();
        });
        
        // Handle input debouncing for range values
        if (input.type === 'number') {
            input.addEventListener("input", debounce(function() {
                triggerFilterAjax();
            }, 500));
        }
    });

    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    function triggerFilterAjax() {
        const formData = new FormData(filterForm);
        const searchParams = new URLSearchParams(formData);
        
        // Add ajax token
        searchParams.set("ajax", "true");
        searchParams.set("page", "1"); // Reset to page 1

        const url = filterForm.action + "?" + searchParams.toString();

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const productGrid = document.getElementById("productGrid");
                const countBadge = document.getElementById("countBadge");
                const noProductsAlert = document.getElementById("noProductsAlert");
                const paginationBlock = document.getElementById("paginationBlock");

                // Update count badge
                countBadge.textContent = data.totalItems;

                if (!data.items || data.items.length === 0) {
                    productGrid.style.display = "none";
                    noProductsAlert.style.display = "block";
                    paginationBlock.innerHTML = "";
                    return;
                }

                noProductsAlert.style.display = "none";
                productGrid.style.display = "grid";

                // Build grid items dynamically
                let html = "";
                data.items.forEach(p => {
                    const badgeColor = p.labelType === 'Organic' ? '#16a34a' : '#2563eb';
                    const badgeHtml = p.labelType ? `
                        <span style="position: absolute; top: 12px; left: 12px; z-index: 10; padding: 4px 10px; border-radius: 20px; color: white; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;
                                     background-color: ` + badgeColor + `;">
                            ` + escapeHtml(p.labelType) + `
                        </span>
                    ` : '';

                    const ctxPath = '${pageContext.request.contextPath}';
                    const starsFull = '★'.repeat(Math.round(p.rating || 0));
                    const starsEmpty = '☆'.repeat(5 - Math.round(p.rating || 0));

                    html += `
                        <div class="product-card" style="background: white; border-radius: 12px; border: 1px solid #f3f4f6; overflow: hidden; position: relative; transition: all 0.3s; box-shadow: 0 2px 10px rgba(0,0,0,0.015); display: flex; flex-direction: column;">
                            ` + badgeHtml + `
                            <a href="` + ctxPath + `/products/detail?id=` + p.productId + `" style="text-decoration: none; color: inherit; display: flex; flex-direction: column; height: 100%;">
                                <div style="height: 180px; background-color: #f9fafb; display: flex; align-items: center; justify-content: center; position: relative;">
                                    <img src="` + ctxPath + `/assets/img/logo-leaf.png" alt="Fruit" style="max-height: 100px; max-width: 100px; object-fit: contain;" />
                                </div>
                                <div style="padding: 15px; display: flex; flex-direction: column; flex-grow: 1;">
                                    <h3 style="font-size: 15px; font-weight: 700; color: #1f2937; margin: 0 0 8px 0; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: 38px;">
                                        ` + escapeHtml(p.name) + `
                                    </h3>
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: auto;">
                                        <div>
                                            <div style="color: #4b5563; font-size: 11px;">Đánh giá</div>
                                            <div style="color: #eab308; font-size: 11px;">` + starsFull + starsEmpty + `</div>
                                        </div>
                                        <span style="background-color: #f0fdf4; color: #166534; font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 20px;">
                                            Xem chi tiết
                                        </span>
                                    </div>
                                </div>
                            </a>
                        </div>
                    `;
                });
                productGrid.innerHTML = html;

                // Build pagination block
                let paginationHtml = "";
                if (data.totalPages > 1) {
                    paginationHtml = `<div class="pagination" style="display: flex; gap: 8px; justify-content: center; align-items: center; list-style: none; padding: 0;">`;
                    for (let i = 1; i <= data.totalPages; i++) {
                        const activeStyle = data.currentPage === i ? `background-color: #14532d; color: white; border-color: #14532d;` : `background-color: white; color: #374151; border-color: #d1d5db;`;
                        paginationHtml += `<a href="` + filterForm.action + `?page=` + i + `&` + searchParams.toString() + `" style="padding: 6px 12px; border: 1px solid; border-radius: 6px; font-size: 13px; font-weight: 600; text-decoration: none; ` + activeStyle + `">` + i + `</a>`;
                    }
                    paginationHtml += `</div>`;
                }
                paginationBlock.innerHTML = paginationHtml;
            })
            .catch(error => console.error("Error executing Ajax search:", error));
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }
});

function clearRecentlyViewedCookie() {
    // II.23 Test case: clearing the cookie must clear the recently viewed block
    document.cookie = "recently_viewed_ids=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    location.reload();
}
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>