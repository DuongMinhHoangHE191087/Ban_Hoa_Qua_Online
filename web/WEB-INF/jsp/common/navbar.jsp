<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- navbar.jsp — Thanh điều hướng chính.
     Hiển thị khác nhau tuỳ theo login state và role.
     Dùng ft:allow để ẩn/hiện menu theo role.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<nav class="navbar">
    <div class="container navbar__inner">
        <a href="${pageContext.request.contextPath}/home" class="navbar__logo">
            <img src="${pageContext.request.contextPath}/assets/images/logo.png" alt="MetaFruit" style="height: 38px; width: 38px; border-radius: 8px; object-fit: cover;">
            <span class="logo-text">Meta<span class="text-highlight">Fruit</span></span>
        </a>

        <form action="${pageContext.request.contextPath}/home" method="get" class="navbar__search">
            <div class="search-wrapper">
                <input type="text" name="keyword" placeholder="Tìm hoa quả sạch nhập khẩu, hữu cơ..." value="<c:out value="${param.keyword}"/>">
                <button type="submit" aria-label="Tìm kiếm">
                    <i class="fa-solid fa-magnifying-glass"></i>
                </button>
            </div>
        </form>

        <ul class="navbar__menu">
            <li>
                <a href="${pageContext.request.contextPath}/home" class="menu-link">
                    <i class="fa-solid fa-apple-whole"></i> Sản phẩm
                </a>
            </li>
            <li>
                <a href="#about" class="menu-link">
                    <i class="fa-solid fa-info-circle"></i> Giới thiệu
                </a>
            </li>
            <li>
                <a href="#contact" class="menu-link">
                    <i class="fa-solid fa-envelope"></i> Liên hệ
                </a>
            </li>

            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <ft:allow role="SHOP_OWNER">
                        <li>
                            <a href="${pageContext.request.contextPath}/home" class="menu-link">
                                <i class="fa-solid fa-store"></i> Cửa hàng
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/shop/dashboard" class="menu-link highlight-shop">
                                <i class="fa-solid fa-shop"></i> Kênh người bán
                            </a>
                        </li>
                    </ft:allow>
                    <ft:allow role="DELIVERY">
                        <li>
                            <a href="${pageContext.request.contextPath}/delivery/dashboard" class="menu-link highlight-delivery">
                                <i class="fa-solid fa-truck-ramp-box"></i> Tài xế
                            </a>
                        </li>
                    </ft:allow>
                    <ft:allow role="ADMIN">
                        <li>
                            <a href="${pageContext.request.contextPath}/admin/dashboard" class="menu-link highlight-admin">
                                <i class="fa-solid fa-user-shield"></i> Admin
                            </a>
                        </li>
                    </ft:allow>
                    
                    <li>
                        <a href="${pageContext.request.contextPath}/cart" class="navbar__cart-btn">
                            <span class="cart-icon-wrapper">
                                <i class="fa-solid fa-basket-shopping"></i>
                                <span id="cart-badge" class="cart-badge-count">0</span>
                            </span>
                            <span class="cart-text">Giỏ hàng</span>
                        </a>
                    </li>
                    
                    <li class="navbar__user-profile">
                        <div class="user-avatar">
                            <i class="fa-solid fa-user-circle"></i>
                        </div>
                        <span class="user-greeting">
                            Chào, <strong class="user-name"><c:out value="${sessionScope.currentUser.fullName}"/></strong>
                        </span>
                        <a href="${pageContext.request.contextPath}/auth/logout" class="logout-btn" title="Đăng xuất">
                            <i class="fa-solid fa-right-from-bracket"></i>
                        </a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li>
                        <a href="${pageContext.request.contextPath}/cart" class="navbar__cart-btn guest-cart-btn">
                            <span class="cart-icon-wrapper">
                                <i class="fa-solid fa-basket-shopping"></i>
                                <span id="cart-badge" class="cart-badge-count">0</span>
                            </span>
                            <span class="cart-text">Giỏ hàng</span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/auth/login" class="nav-btn nav-btn-secondary">
                            <i class="fa-solid fa-right-to-bracket"></i> Đăng nhập
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/auth/register" class="nav-btn nav-btn-primary">
                            Đăng ký
                        </a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>
