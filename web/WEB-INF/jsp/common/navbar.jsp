<%-- navbar.jsp — Thanh điều hướng chính.
     Hiển thị khác nhau tuỳ theo login state và role.
     Dùng ft:allow để ẩn/hiện menu theo role.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tags/fruitmkt.tld" %>
<nav class="navbar">
    <div class="container navbar__inner">
        <a href="/" class="navbar__logo">🍊 FruitMkt</a>

        <form action="/products" method="get" class="navbar__search">
            <input type="text" name="q" placeholder="Tìm hoa quả..." value="">
            <button type="submit">Tìm</button>
        </form>

        <ul class="navbar__menu">
            <li><a href="/products">Sản phẩm</a></li>

            <c:choose>
                <c:when test="">
                    <%-- Menu theo role --%>
                    <ft:allow role="SHOP_OWNER">
                        <li><a href="/shop/dashboard">Shop</a></li>
                    </ft:allow>
                    <ft:allow role="DELIVERY">
                        <li><a href="/delivery/dashboard">Giao hàng</a></li>
                    </ft:allow>
                    <ft:allow role="ADMIN">
                        <li><a href="/admin/dashboard">Admin</a></li>
                    </ft:allow>
                    <li><a href="/cart">Giỏ hàng</a></li>
                    <li class="navbar__user">
                        <span></span>
                        <a href="/auth/logout">Đăng xuất</a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li><a href="/auth/login">Đăng nhập</a></li>
                    <li><a href="/auth/register">Đăng ký</a></li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>
