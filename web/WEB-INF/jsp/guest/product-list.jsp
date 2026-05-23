<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Sản phẩm"/></jsp:include>

<%-- Servlet set: pagedResult (PagedResultDTO), categories, currentFilter (Map) --%>
<div class="container page-product-list">
    <aside class="filter-sidebar">
        <h3>Lọc sản phẩm</h3>
        <form action="${pageContext.request.contextPath}/products" method="get">
            <div class="form-group">
                <label>Tìm kiếm</label>
                <input type="text" name="q" value="">
            </div>
            <div class="form-group">
                <label>Danh mục</label>
                <select name="categoryId">
                    <option value="">Tất cả</option>
                    <c:forEach var="cat" items="">
                        <option value="" >
                            <c:out value=""/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group form-row">
                <input type="number" name="minPrice" placeholder="Giá min" value="" min="0">
                <input type="number" name="maxPrice" placeholder="Giá max" value="" min="0">
            </div>
            <div class="form-group">
                <label>Sắp xếp</label>
                <select name="sort">
                    <option value="newest">Mới nhất</option>
                    <option value="best_seller">Bán chạy</option>
                    <option value="price_asc">Giá tăng dần</option>
                    <option value="price_desc">Giá giảm dần</option>
                </select>
            </div>
            <button type="submit" class="btn btn-primary btn-block">Áp dụng</button>
        </form>
    </aside>

    <main class="product-results">
        <div class="results-header">
            <span>Tìm thấy <strong></strong> sản phẩm</span>
        </div>
        <div class="product-grid">
            <c:forEach var="p" items="">
                <a href="${pageContext.request.contextPath}/products/detail?id=" class="product-card">
                    <img src="${pageContext.request.contextPath}/" alt="">
                    <h3><c:out value=""/></h3>
                    <div><ft:currency value=""/></div>
                    <div><ft:stars rating=""/></div>
                    <div class="product-card__shop"><c:out value=""/></div>
                </a>
            </c:forEach>
        </div>
        <%-- Phân trang --%>
        <ft:pagination current=""
                       total=""
                       baseUrl="${pageContext.request.contextPath}/products"/>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
