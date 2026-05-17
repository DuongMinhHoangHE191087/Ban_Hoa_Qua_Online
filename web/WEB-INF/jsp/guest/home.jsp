<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tags/fruitmkt.tld" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"><jsp:param name="pageTitle" value="Trang chủ"/></jsp:include>

<%-- Servlet set vào request:
     - categories   : List<Category>
     - featuredProducts : List<ProductListDTO>
     - promotionBanner  : Promotion (có thể null)
--%>
<section class="hero">
    <div class="container">
        <h1>Hoa quả tươi, giao tận cửa 🍊</h1>
        <form action="${pageContext.request.contextPath}/products" method="get" class="hero-search">
            <input type="text" name="q" placeholder="Tìm xoài, táo, cam...">
            <button type="submit" class="btn btn-primary">Tìm kiếm</button>
        </form>
    </div>
</section>

<section class="categories container">
    <h2>Danh mục</h2>
    <div class="category-grid">
        <c:forEach var="cat" items="">
            <a href="${pageContext.request.contextPath}/products?categoryId=" class="category-card">
                <span></span>
            </a>
        </c:forEach>
    </div>
</section>

<section class="featured-products container">
    <h2>Sản phẩm nổi bật</h2>
    <div class="product-grid">
        <c:forEach var="p" items="">
            <a href="${pageContext.request.contextPath}/products/detail?id=" class="product-card">
                <img src="${pageContext.request.contextPath}/" alt="">
                <h3><c:out value=""/></h3>
                <div class="product-card__price"><ft:currency value=""/></div>
                <div class="product-card__rating"><ft:stars rating="" showValue="true"/></div>
                <div class="product-card__shop"><c:out value=""/></div>
            </a>
        </c:forEach>
    </div>
</section>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
