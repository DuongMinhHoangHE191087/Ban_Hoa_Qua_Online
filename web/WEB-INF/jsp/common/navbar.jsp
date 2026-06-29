<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- navbar.jsp — Thanh điều hướng chính.
     Hiển thị khác nhau tuỳ theo login state và role.
     Dùng ft:allow để ẩn/hiện menu theo role.
--%>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="ft" uri="/WEB-INF/tld/fruitmkt.tld" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<nav class="navbar">
    <div class="container navbar__inner">
        <a href="${pageContext.request.contextPath}/home" class="navbar__logo brand-lockup">
            <img src="${pageContext.request.contextPath}/assets/images/logo_light.png" alt="MetaFruit" class="brand-mark">
            <span class="brand-wordmark">Meta<span class="brand-wordmark__accent">Fruit</span></span>
        </a>

        <c:set var="currentURI" value="${pageContext.request.requestURI}" />
        <c:choose>
            <c:when test="${fn:contains(currentURI, '/products') and not fn:contains(currentURI, '/products/detail')}">
                <!-- Hide header search to avoid duplicate search inputs on product list page -->
                <div class="navbar__search opacity-0 pointer-events-none" style="visibility: hidden;"></div>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/products" method="get" class="navbar__search">
                    <div class="search-wrapper">
                        <input type="text" name="keyword" placeholder="Tìm hoa quả sạch nhập khẩu, hữu cơ..." value="<c:out value="${param.keyword}"/>">
                        <button type="submit" aria-label="Tìm kiếm">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>

        <!-- Hamburger Menu Toggle Button for Mobile -->
        <button type="button" id="navbarToggle" class="navbar__toggle" aria-label="Toggle Navigation">
            <i class="fa-solid fa-bars"></i>
        </button>

        <ul class="navbar__menu" id="navbarMenu">
            <li>
                <a href="${pageContext.request.contextPath}/products" class="menu-link">
                    <i class="fa-solid fa-apple-whole"></i> Sản phẩm
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/about" class="menu-link">
                    <i class="fa-solid fa-info-circle"></i> Giới thiệu
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/about#contact" class="menu-link">
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
                    
                    <!-- Chat support button -->
                    <li>
                        <c:choose>
                            <c:when test="${sessionScope.currentUser.role == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/admin/chat" class="navbar__cart-btn" title="Hộp thư hỗ trợ Admin">
                                    <span class="cart-icon-wrapper">
                                        <i class="fa-solid fa-comments"></i>
                                        <span id="chat-badge" class="cart-badge-count hidden">0</span>
                                    </span>
                                    <span class="cart-text">Hỗ trợ</span>
                                </a>
                            </c:when>
                            <c:when test="${sessionScope.currentUser.role == 'SHOP_OWNER'}">
                                <a href="${pageContext.request.contextPath}/shop/chat" class="navbar__cart-btn" title="Tin nhắn Shop">
                                    <span class="cart-icon-wrapper">
                                        <i class="fa-solid fa-comments"></i>
                                        <span id="chat-badge" class="cart-badge-count hidden">0</span>
                                    </span>
                                    <span class="cart-text">Tin nhắn</span>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/chat" class="navbar__cart-btn" title="Hộp thư chat">
                                    <span class="cart-icon-wrapper">
                                        <i class="fa-solid fa-comments"></i>
                                        <span id="chat-badge" class="cart-badge-count hidden">0</span>
                                    </span>
                                    <span class="cart-text">Tin nhắn</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </li>

                    <!-- Notifications button with dropdown preview -->
                    <li class="relative" style="position: relative;">
                        <a href="javascript:void(0)" id="btnNotifDropdown" class="navbar__cart-btn" title="Thông báo của bạn">
                            <span class="cart-icon-wrapper">
                                <i class="fa-solid fa-bell"></i>
                                <span id="notif-badge" class="cart-badge-count hidden">0</span>
                            </span>
                            <span class="cart-text">Thông báo</span>
                        </a>
                        
                        <!-- Dropdown Menu -->
                        <div id="notifDropdown" class="hidden notif-dropdown">
                            <div class="notif-dropdown__header flex justify-between items-center px-4 py-3 border-b border-slate-100">
                                <span class="font-bold text-slate-800 text-sm">Thông báo mới</span>
                                <button type="button" id="btnMarkAllReadAjax" class="text-xs font-semibold text-primary hover:text-primary-dark border-0 bg-transparent cursor-pointer">Đọc tất cả</button>
                            </div>
                            <div id="notifDropdownList" class="notif-dropdown__list max-h-[280px] overflow-y-auto">
                                <div class="px-5 py-5 text-center text-slate-400 text-sm">Đang tải thông báo...</div>
                            </div>
                            <div class="notif-dropdown__footer flex justify-center gap-2 px-4 py-3 border-t border-slate-100 bg-slate-50">
                                <a href="${pageContext.request.contextPath}/notifications" class="text-xs font-bold text-primary no-underline">Xem chi tiết</a>
                            </div>
                        </div>
                    </li>

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
                            <c:choose>
                                <c:when test="${not empty sessionScope.currentUser.avatarUrl}">
                                    <img src="${fn:startsWith(sessionScope.currentUser.avatarUrl, 'http') ? sessionScope.currentUser.avatarUrl : pageContext.request.contextPath.concat('/').concat(sessionScope.currentUser.avatarUrl)}" 
                                         alt="Avatar" 
                                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                    <div class="avatar-fallback" style="display: none;">
                                        <c:out value="${fn:substring(sessionScope.currentUser.fullName, 0, 1)}"/>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="avatar-fallback" style="display: flex;">
                                        <c:out value="${fn:substring(sessionScope.currentUser.fullName, 0, 1)}"/>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <span class="user-greeting">
                            Chào, <a href="${pageContext.request.contextPath}/profile" class="user-name text-decoration-none" style="color: var(--color-primary-dark);"><c:out value="${sessionScope.currentUser.fullName}"/></a>
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

<script>
    document.addEventListener("DOMContentLoaded", function() {
        if (!window.isLoggedIn) {
            return;
        }

        const btnNotifDropdown = document.getElementById('btnNotifDropdown');
        const notifDropdown = document.getElementById('notifDropdown');
        const notifDropdownList = document.getElementById('notifDropdownList');
        const btnMarkAllReadAjax = document.getElementById('btnMarkAllReadAjax');

        function updateBadges() {
            if (!window.NotificationAjax) {
                return;
            }
            NotificationAjax.refreshBadges().catch(err => console.error("Error loading badges", err));
        }

        function loadRecentNotifications() {
            if (!window.NotificationAjax || !notifDropdownList) {
                return Promise.resolve([]);
            }
            return NotificationAjax.loadRecentNotifications(notifDropdownList);
        }

        function showNotificationError(error) {
            if (!window.NotificationAjax || !notifDropdownList) {
                return;
            }
            NotificationAjax.renderError(notifDropdownList, error, loadRecentNotifications);
        }

        updateBadges();

        if (btnNotifDropdown && notifDropdown) {
            btnNotifDropdown.addEventListener('click', function(e) {
                e.stopPropagation();
                const isHidden = notifDropdown.style.display === 'none' || notifDropdown.style.display === '';
                if (isHidden) {
                    notifDropdown.style.display = 'block';
                    loadRecentNotifications();
                } else {
                    notifDropdown.style.display = 'none';
                }
            });

            document.addEventListener('click', function(e) {
                if (notifDropdown && !notifDropdown.contains(e.target) && e.target !== btnNotifDropdown) {
                    notifDropdown.style.display = 'none';
                }
            });
        }

        window.handleNotifClick = function(e, notifId, link) {
            e.preventDefault();
            if (!window.NotificationAjax) {
                return;
            }

            NotificationAjax.markRead(notifId)
                .then(() => {
                    if (link && link !== '#') {
                        window.location.href = link;
                        return;
                    }
                    if (notifDropdown) {
                        notifDropdown.style.display = 'none';
                    }
                    return NotificationAjax.refreshBadges().then(loadRecentNotifications);
                })
                .catch(error => {
                    console.error(error);
                    showNotificationError(error);
                });
        };

        window.handleNotifDelete = function(e, notifId) {
            e.preventDefault();
            e.stopPropagation();
            if (!window.NotificationAjax) {
                return;
            }
            NotificationAjax.deleteNotification(notifId)
                .then(() => NotificationAjax.refreshBadges())
                .then(loadRecentNotifications)
                .catch(error => {
                    console.error(error);
                    showNotificationError(error);
                });
        };

        if (btnMarkAllReadAjax) {
            btnMarkAllReadAjax.addEventListener('click', function(e) {
                e.stopPropagation();
                if (!window.NotificationAjax) {
                    return;
                }

                NotificationAjax.setButtonBusy(btnMarkAllReadAjax, true, 'Đang đọc...');
                NotificationAjax.markAllRead()
                    .then(() => NotificationAjax.refreshBadges())
                    .then(loadRecentNotifications)
                    .catch(error => {
                        console.error(error);
                        showNotificationError(error);
                    })
                    .finally(() => {
                        NotificationAjax.setButtonBusy(btnMarkAllReadAjax, false);
                    });
            });
        }
    });
</script>
