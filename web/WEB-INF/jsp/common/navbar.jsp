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
                    
                    <%-- Chuông thông báo & Dropdown thời gian thực --%>
                    <li class="navbar__notif">
                        <button id="notif-btn" class="navbar__notif-btn" aria-label="Thông báo" title="Thông báo">
                            <span class="cart-icon-wrapper" style="position: relative; display: flex; align-items: center;">
                                <i class="fa-solid fa-bell" style="font-size: 1.25rem;"></i>
                                <span id="notif-badge" class="notif-badge-count hidden">0</span>
                            </span>
                        </button>
                        <div id="notif-dropdown" class="notif-dropdown">
                            <div class="notif-dropdown__header">
                                <span class="notif-dropdown__title">Thông báo mới nhất</span>
                                <button id="notif-mark-all" class="notif-dropdown__mark-read">Đánh dấu đã đọc hết</button>
                            </div>
                            <div id="notif-list" class="notif-list">
                                <div class="notif-empty">
                                    <i class="fa-solid fa-bell-slash"></i>
                                    Đang tải thông báo...
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/notifications" class="notif-dropdown__footer">
                                Xem tất cả thông báo
                            </a>
                        </div>
                    </li>
                    
                    <li class="navbar__user-profile">
                        <div class="user-avatar">
                            <i class="fa-solid fa-user-circle"></i>
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

<c:if test="${not empty sessionScope.currentUser}">
    <script>
        (function() {
            let prevUnreadCount = null;
            let pollingTimer = null;

            function escapeHtml(text) {
                if (!text) return '';
                return text
                    .replace(/&/g, "&amp;")
                    .replace(/</g, "&lt;")
                    .replace(/>/g, "&gt;")
                    .replace(/"/g, "&quot;")
                    .replace(/'/g, "&#039;");
            }

            function getNotificationIcon(type) {
                switch(type) {
                    case 'ORDER_UPDATE': return '<i class="fa-solid fa-box text-emerald-600"></i>';
                    case 'PROMOTION': return '<i class="fa-solid fa-tags text-orange-500"></i>';
                    case 'SYSTEM': return '<i class="fa-solid fa-circle-info text-blue-500"></i>';
                    case 'INVENTORY_ALERT': return '<i class="fa-solid fa-triangle-exclamation text-red-500"></i>';
                    case 'PAYMENT': return '<i class="fa-solid fa-credit-card text-green-600"></i>';
                    default: return '<i class="fa-solid fa-bell text-gray-500"></i>';
                }
            }

            function getNotificationIconClass(type) {
                switch(type) {
                    case 'ORDER_UPDATE': return 'notif-icon--order';
                    case 'PROMOTION': return 'notif-icon--promo';
                    case 'SYSTEM': return 'notif-icon--system';
                    case 'INVENTORY_ALERT': return 'notif-icon--alert';
                    case 'PAYMENT': return 'notif-icon--payment';
                    default: return 'notif-icon--system';
                }
            }

            function formatTime(isoString) {
                try {
                    const date = new Date(isoString);
                    const now = new Date();
                    const diffMs = now - date;
                    const diffMins = Math.floor(diffMs / 60000);
                    if (diffMins < 1) return 'Vừa xong';
                    if (diffMins < 60) return diffMins + ' phút trước';
                    const diffHours = Math.floor(diffMins / 60);
                    if (diffHours < 24) return diffHours + ' giờ trước';
                    const diffDays = Math.floor(diffHours / 24);
                    if (diffDays < 30) return diffDays + ' ngày trước';
                    return date.toLocaleDateString('vi-VN');
                } catch(e) {
                    return '';
                }
            }

            function showToast(notif) {
                // Remove existing notification toast if any
                const oldToast = document.getElementById('notif-toast');
                if (oldToast) oldToast.remove();

                // Create toast element
                let toast = document.createElement('div');
                toast.id = 'notif-toast';
                toast.className = 'premium-toast show';
                toast.innerHTML = `
                    <span class="premium-toast-icon">\${getNotificationIcon(notif.type)}</span>
                    <div style="flex: 1;">
                        <div style="font-weight: 700; font-size: 13px; color: var(--color-primary-dark);">\${escapeHtml(notif.title)}</div>
                        <div style="font-size: 11px; color: var(--color-text-secondary); max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">\${escapeHtml(notif.message)}</div>
                    </div>
                `;
                document.body.appendChild(toast);
                setTimeout(() => {
                    toast.classList.remove('show');
                    setTimeout(() => toast.remove(), 500);
                }, 4500);
            }

            function fetchNotifications() {
                if (pollingTimer) clearTimeout(pollingTimer);

                fetch(window.contextPath + '/api/notifications?limit=5')
                    .then(res => {
                        if (res.status === 401) {
                            console.log('Session expired or logged out. Stopping polling.');
                            return null;
                        }
                        return res.json();
                    })
                    .then(data => {
                        if (!data) return; // Stop scheduling next poll if response was 401
                        
                        if (data.success) {
                            const count = data.unreadCount;
                            const badge = document.getElementById('notif-badge');
                            if (badge) {
                                if (count > 0) {
                                    badge.textContent = count;
                                    badge.classList.remove('hidden');
                                } else {
                                    badge.classList.add('hidden');
                                }
                            }

                            // Check if unread count increased, show toast for the newest
                            if (prevUnreadCount !== null && count > prevUnreadCount) {
                                if (data.notifications && data.notifications.length > 0) {
                                    const newest = data.notifications[0];
                                    if (!newest.isRead) {
                                        showToast(newest);
                                    }
                                }
                            }
                            prevUnreadCount = count;

                            // Render dropdown
                            const listContainer = document.getElementById('notif-list');
                            if (listContainer) {
                                if (!data.notifications || data.notifications.length === 0) {
                                    listContainer.innerHTML = `
                                        <div class="notif-empty">
                                            <i class="fa-solid fa-bell-slash"></i>
                                            Chưa có thông báo nào
                                        </div>
                                    `;
                                } else {
                                    listContainer.innerHTML = data.notifications.map(n => {
                                        const unreadClass = n.isRead ? '' : 'unread';
                                        const link = n.actionUrl ? window.contextPath + n.actionUrl : 'javascript:void(0)';
                                        return `
                                            <a href="\${link}" class="notif-item \${unreadClass}" data-id="\${n.notificationId}">
                                                <div class="notif-item__icon \${getNotificationIconClass(n.type)}">
                                                    \${getNotificationIcon(n.type)}
                                                </div>
                                                <div class="notif-item__content">
                                                    <span class="notif-item__title">\${escapeHtml(n.title)}</span>
                                                    <span class="notif-item__msg">\${escapeHtml(n.message)}</span>
                                                    <span class="notif-item__time">\${formatTime(n.createdAt)}</span>
                                                </div>
                                            </a>
                                        `;
                                    }).join('');

                                    // Click item logic
                                    listContainer.querySelectorAll('.notif-item').forEach(item => {
                                        item.addEventListener('click', function(e) {
                                            const id = this.getAttribute('data-id');
                                            const url = this.getAttribute('href');
                                            if (this.classList.contains('unread')) {
                                                e.preventDefault();
                                                markAsRead(id, url);
                                            }
                                        });
                                    });
                                }
                            }
                        }
                        
                        pollingTimer = setTimeout(fetchNotifications, 15000);
                    })
                    .catch(err => {
                        console.error('Error fetching notifications:', err);
                        pollingTimer = setTimeout(fetchNotifications, 15000);
                    });
            }

            function markAsRead(id, redirectUrl) {
                fetch(window.contextPath + '/api/notifications', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-Token': window.csrfToken
                    },
                    body: 'action=markRead&notificationId=' + id
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        if (redirectUrl && redirectUrl !== 'javascript:void(0)') {
                            window.location.href = redirectUrl;
                        } else {
                            fetchNotifications();
                        }
                    }
                })
                .catch(err => console.error('Error marking read:', err));
            }

            function markAllAsRead() {
                fetch(window.contextPath + '/api/notifications', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-Token': window.csrfToken
                    },
                    body: 'action=markAllRead'
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        fetchNotifications();
                    }
                })
                .catch(err => console.error('Error marking all read:', err));
            }

            // Events init
            const btn = document.getElementById('notif-btn');
            const dropdown = document.getElementById('notif-dropdown');
            const markAllBtn = document.getElementById('notif-mark-all');

            if (btn && dropdown) {
                btn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    dropdown.classList.toggle('show');
                });

                document.addEventListener('click', function() {
                    dropdown.classList.remove('show');
                });

                dropdown.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
            }

            if (markAllBtn) {
                markAllBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    markAllAsRead();
                });
            }

            // Initial fetch
            fetchNotifications();
        })();
    </script>
</c:if>

