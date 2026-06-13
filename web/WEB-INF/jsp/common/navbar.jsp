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
        <a href="${pageContext.request.contextPath}/home" class="navbar__logo flex items-center gap-2" style="text-decoration: none; display: inline-flex; align-items: center;">
            <img src="${pageContext.request.contextPath}/assets/images/logo_light.png" alt="MetaFruit" style="height: 38px; width: 38px; border-radius: 10px; object-fit: cover; box-shadow: 0 4px 12px rgba(77, 102, 28, 0.15); border: 2px solid rgba(255, 255, 255, 0.8);">
            <span class="logo-text" style="font-size: 1.25rem; font-weight: 800; color: #4d661c; letter-spacing: -0.025em; font-family: 'Lexend', sans-serif; margin-left: 8px;">Meta<span style="color: #31694b; font-weight: 600;">Fruit</span></span>
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

        <ul class="navbar__menu">
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
                        <div id="notifDropdown" class="hidden" style="display: none; position: absolute; right: 0; top: 100%; margin-top: 10px; width: 340px; background: white; border: 1px solid rgba(0,0,0,0.08); border-radius: 12px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1); z-index: 999; overflow: hidden;">
                            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f1f5f9; background: #f8fafc;">
                                <span style="font-weight: 700; color: #1e293b; font-size: 14px;">Thông báo mới</span>
                                <button type="button" id="btnMarkAllReadAjax" style="font-size: 12px; font-weight: 600; color: #4d661c; border: none; background: none; cursor: pointer;">Đọc tất cả</button>
                            </div>
                            <div id="notifDropdownList" style="max-height: 280px; overflow-y: auto;">
                                <div style="padding: 20px; text-align: center; color: #94a3b8; font-size: 13px;">Đang tải thông báo...</div>
                            </div>
                            <div style="padding: 10px; text-align: center; background: #f8fafc; border-top: 1px solid #f1f5f9; display: flex; justify-content: center; gap: 10px;">
                                <a href="${pageContext.request.contextPath}/notifications" style="font-size: 12px; font-weight: 700; color: #4d661c; text-decoration: none;">Xem chi tiết</a>
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
                        <div class="user-avatar" style="overflow: hidden; width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 1.5px solid rgba(77, 102, 28, 0.15);">
                            <c:choose>
                                <c:when test="${not empty sessionScope.currentUser.avatarUrl}">
                                    <img src="${fn:startsWith(sessionScope.currentUser.avatarUrl, 'http') ? sessionScope.currentUser.avatarUrl : pageContext.request.contextPath.concat('/').concat(sessionScope.currentUser.avatarUrl)}" 
                                         alt="Avatar" 
                                         style="width: 100%; height: 100%; object-fit: cover;"
                                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                    <div class="avatar-fallback" style="display: none; width: 100%; height: 100%; background: linear-gradient(135deg, #14532d, #166534); color: white; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; text-transform: uppercase; border-radius: 50%;">
                                        <c:out value="${fn:substring(sessionScope.currentUser.fullName, 0, 1)}"/>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="avatar-fallback" style="display: flex; width: 100%; height: 100%; background: linear-gradient(135deg, #14532d, #166534); color: white; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; text-transform: uppercase; border-radius: 50%;">
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
        if (window.isLoggedIn) {
            // Function to update badges
            function updateBadges() {
                fetch(window.contextPath + '/api/notifications/unread')
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            const chatBadge = document.getElementById('chat-badge');
                            const notifBadge = document.getElementById('notif-badge');
                            
                            if (chatBadge) {
                                if (data.unreadChats > 0) {
                                    chatBadge.textContent = data.unreadChats;
                                    chatBadge.classList.remove('hidden');
                                } else {
                                    chatBadge.classList.add('hidden');
                                }
                            }
                            if (notifBadge) {
                                if (data.unreadNotifications > 0) {
                                    notifBadge.textContent = data.unreadNotifications;
                                    notifBadge.classList.remove('hidden');
                                } else {
                                    notifBadge.classList.add('hidden');
                                }
                            }
                        }
                    }).catch(err => console.error("Error loading badges", err));
            }
            updateBadges();

            // Toggle dropdown
            const btnNotifDropdown = document.getElementById('btnNotifDropdown');
            const notifDropdown = document.getElementById('notifDropdown');
            const notifDropdownList = document.getElementById('notifDropdownList');

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

            function loadRecentNotifications() {
                if (!notifDropdownList) return;
                notifDropdownList.innerHTML = '<div style="padding: 20px; text-align: center; color: #94a3b8; font-size: 13px;">Đang tải thông báo...</div>';
                fetch(window.contextPath + '/api/notifications/recent')
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            if (!data.notifications || data.notifications.length === 0) {
                                notifDropdownList.innerHTML = '<div style="padding: 20px; text-align: center; color: #94a3b8; font-size: 13px;">Không có thông báo nào gần đây.</div>';
                                return;
                            }
                            
                            let html = '';
                            function formatNotifTime(val) {
                                if (!val) return '';
                                let d = Array.isArray(val)
                                    ? new Date(val[0], val[1]-1, val[2], val[3]||0, val[4]||0, val[5]||0)
                                    : new Date(val);
                                return isNaN(d) ? '' : d.toLocaleString('vi-VN', {hour: '2-digit', minute:'2-digit', day:'2-digit', month:'2-digit'});
                            }

                            data.notifications.forEach(n => {
                                const bg = n.isRead ? 'transparent' : '#f8fafc';
                                const dot = n.isRead ? '' : '<span style="display:inline-block; width:6px; height:6px; background:#4d661c; border-radius:50%; margin-right:4px;"></span>';
                                const dateStr = formatNotifTime(n.createdAt);
                                const link = n.actionUrl ? window.contextPath + n.actionUrl : '#';
                                
                                html += `
                                    <div class="notif-dropdown-item" style="padding:12px 16px; background:\${bg}; border-bottom:1px solid #f1f5f9; cursor:pointer;" onclick="handleNotifClick(event, \${n.notificationId}, '\${link}')">
                                        <div style="display:flex; justify-content:space-between; margin-bottom:4px; align-items:center;">
                                            <span style="font-weight:600; font-size:13px; color:#334155; display:flex; align-items:center;">
                                                \${dot}\${n.title}
                                            </span>
                                            <span style="font-size:10px; color:#94a3b8; white-space:nowrap; margin-left:8px;">\${dateStr}</span>
                                        </div>
                                        <p style="margin:0; font-size:12px; color:#64748b; line-height:1.4; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
                                            \${n.message}
                                        </p>
                                    </div>
                                `;
                            });
                            notifDropdownList.innerHTML = html;
                        } else {
                            notifDropdownList.innerHTML = '<div style="padding: 20px; text-align: center; color: #ef4444; font-size: 13px;">Không thể tải thông báo.</div>';
                        }
                    }).catch(err => {
                        console.error(err);
                        notifDropdownList.innerHTML = '<div style="padding: 20px; text-align: center; color: #ef4444; font-size: 13px;">Lỗi tải thông báo.</div>';
                    });
            }

            window.handleNotifClick = function(e, notifId, link) {
                e.preventDefault();
                // Mark as read via AJAX, then redirect
                fetch(window.contextPath + '/api/notifications/markAllRead?action=markRead&notifId=' + notifId, { method: 'POST' })
                    .finally(() => {
                        if (link && link !== '#') {
                            window.location.href = link;
                        } else {
                            if (notifDropdown) notifDropdown.style.display = 'none';
                            updateBadges();
                        }
                    });
            };

            const btnMarkAllReadAjax = document.getElementById('btnMarkAllReadAjax');
            if (btnMarkAllReadAjax) {
                btnMarkAllReadAjax.addEventListener('click', function(e) {
                    e.stopPropagation();
                    fetch(window.contextPath + '/api/notifications/markAllRead', { method: 'POST' })
                        .then(r => r.json())
                        .then(data => {
                            if (data.success) {
                                updateBadges();
                                loadRecentNotifications();
                            }
                        }).catch(err => console.error(err));
                });
            }
        }
    });
</script>
