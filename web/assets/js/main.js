/**
 * main.js — JavaScript khởi động cho FruitMkt
 *
 * MODULE PATTERN: Mỗi tính năng là một object độc lập.
 * Import thêm js/components/*.js và js/pages/*.js khi cần.
 *
 * === GUEST CART (localStorage) ===
 * Giỏ hàng guest được lưu trong localStorage key: 'guestCart'
 * Format: [{ variantId, name, price, quantity, imagePath }, ...]
 * Khi login → gọi CartSync.syncToServer() để đồng bộ lên DB.
 *
 * === WEBSOCKET CHAT ===
 * Runtime chat hiện nằm trong các JSP chat page với endpoint /ws/chat/{sessionId}.
 */

// ── 1. Init ──────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    Alert.autoHide();
    
    // Tự động đồng bộ gộp giỏ hàng vãng lai lên database ngay sau khi load trang đăng nhập thành công
    if (window.isLoggedIn === true) {
        CartSync.syncToServer(window.contextPath || '');
    }
    
    GuestCart.updateBadge();
    console.log('[FruitMkt] JS initialized');
});

// ── 2. Alert auto-hide ───────────────────────────────────────────────────────
const Alert = {
    /** Tự động ẩn alert sau 4 giây */
    autoHide() {
        document.querySelectorAll('.alert').forEach(el => {
            setTimeout(() => el.remove(), 4000);
        });
    }
};

function normalizePackagingId(value) {
    const parsed = Number.parseInt(value, 10);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function findCartItemIndexByVariantAndPackaging(items, variantId, packagingId) {
    const normalizedVariantId = Number.parseInt(variantId, 10);
    const normalizedPackagingId = normalizePackagingId(packagingId);
    return (items || []).findIndex(item => {
        const itemVariantId = Number.parseInt(item?.variantId, 10);
        const itemPackagingId = normalizePackagingId(item?.packagingId);
        return itemVariantId === normalizedVariantId && itemPackagingId === normalizedPackagingId;
    });
}

function buildCartItemSelectionKeyFromParts(cartItemId, variantId, packagingId) {
    const normalizedCartItemId = Number.parseInt(cartItemId, 10);
    if (Number.isInteger(normalizedCartItemId) && normalizedCartItemId > 0) {
        return `cart:${normalizedCartItemId}`;
    }
    const normalizedVariantId = Number.parseInt(variantId, 10);
    const normalizedPackagingId = normalizePackagingId(packagingId);
    return `guest:${normalizedVariantId}:${normalizedPackagingId ?? 'none'}`;
}

function buildCartItemSelectionKey(item) {
    return buildCartItemSelectionKeyFromParts(item?.cartItemId, item?.variantId, item?.packagingId);
}

window.normalizePackagingId = normalizePackagingId;
window.findCartItemIndexByVariantAndPackaging = findCartItemIndexByVariantAndPackaging;
window.buildCartItemSelectionKeyFromParts = buildCartItemSelectionKeyFromParts;
window.buildCartItemSelectionKey = buildCartItemSelectionKey;

// ── 3. Guest Cart (localStorage) ─────────────────────────────────────────────
/**
 * GuestCart — Quản lý giỏ hàng khách chưa đăng nhập.
 *
 * CÁCH DÙNG:
 *   GuestCart.add({ variantId: 1, name: 'Xoài Thái', price: 50000, quantity: 2, imagePath: 'uploads/...' });
 *   GuestCart.getItems();  // → Array
 *   GuestCart.remove(variantId);
 *   GuestCart.clear();
 *
 * Khi user đăng nhập xong, CartSync.syncToServer() được gọi tự động.
 */
const GuestCart = {
    KEY: 'guestCart',

    getItems() {
        try { return JSON.parse(localStorage.getItem(this.KEY)) || []; }
        catch { return []; }
    },

    save(items) {
        localStorage.setItem(this.KEY, JSON.stringify(items));
        this.updateBadge();
    },

    /**
     * Thêm sản phẩm vào giỏ hàng guest.
     * @param {{ variantId, name, price, quantity, imagePath }} item
     */
    add(item) {
        const items = this.getItems();
        const normalizedItem = {
            ...item,
            variantId: Number.parseInt(item?.variantId, 10),
            packagingId: normalizePackagingId(item?.packagingId),
            quantity: Number.parseInt(item?.quantity, 10) || 0
        };
        const idx = findCartItemIndexByVariantAndPackaging(items, normalizedItem.variantId, normalizedItem.packagingId);
        if (idx >= 0) {
            items[idx].quantity += normalizedItem.quantity;
            items[idx].packagingId = normalizedItem.packagingId;
        } else {
            items.push(normalizedItem);
        }
        this.save(items);
    },

    remove(variantId, packagingId = null) {
        const normalizedVariantId = Number.parseInt(variantId, 10);
        const normalizedPackagingId = normalizePackagingId(packagingId);
        this.save(this.getItems().filter(i => {
            const itemVariantId = Number.parseInt(i?.variantId, 10);
            const itemPackagingId = normalizePackagingId(i?.packagingId);
            return !(itemVariantId === normalizedVariantId && itemPackagingId === normalizedPackagingId);
        }));
    },

    updateQuantity(variantId, quantity, packagingId = null) {
        const normalizedVariantId = Number.parseInt(variantId, 10);
        const normalizedPackagingId = normalizePackagingId(packagingId);
        const items = this.getItems().map(i => {
            const itemVariantId = Number.parseInt(i?.variantId, 10);
            const itemPackagingId = normalizePackagingId(i?.packagingId);
            return itemVariantId === normalizedVariantId && itemPackagingId === normalizedPackagingId
                ? { ...i, quantity }
                : i;
        });
        this.save(items);
    },

    clear() {
        localStorage.removeItem(this.KEY);
        this.updateBadge();
    },

    getTotal() {
        return this.getItems().reduce((sum, i) => sum + i.price * i.quantity, 0);
    },

    getCount() {
        return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
    },

    /** Cập nhật số badge trên navbar */
    updateBadge() {
        const badge = document.getElementById('cart-badge');
        if (badge) {
            const isLoggedIn = window.isLoggedIn === true;
            const key = isLoggedIn ? 'userCart' : 'guestCart';
            let count = 0;
            try {
                const items = JSON.parse(localStorage.getItem(key)) || [];
                count = items.reduce((sum, i) => sum + (parseInt(i.quantity) || 0), 0);
            } catch (e) {
                count = 0;
            }
            badge.textContent = count;
        }
    }
};

// ── 4. CartSync — Đồng bộ guest cart lên server sau khi login ───────────────
/**
 * CartSync — Gọi sau khi đăng nhập thành công.
 *
 * Servlet nhận: POST /api/cart/sync
 * Body (JSON): { items: [...] }
 * Response: { success: true }
 *
 * CÁCH GỌI TRONG LoginServlet (sau khi setCurrentUser):
 *   // Server trả về response có header X-Cart-Sync: required
 *   // Client JS tự động sync.
 *   // Hoặc redirect về /api/cart/sync?redirect=/
 */
const CartSync = {
    async syncToServer(contextPath = '') {
        const items = GuestCart.getItems();
        if (items.length === 0) return;

        try {
            console.log('[CartSync] Initiating global guest cart sync to server...');
            const resp = await fetch(`${contextPath}/api/cart/sync`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-Token': window.csrfToken || ''
                },
                body: JSON.stringify({ items })
            });
            if (resp.ok) {
                GuestCart.clear();
                console.log('[CartSync] Guest cart synced successfully.');

                // Lấy summary giỏ hàng thành viên mới nhất từ database để update Navbar badge
                const summaryResp = await fetch(`${contextPath}/cart?action=sync`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: `guestCart=[]&_csrf=${window.csrfToken || ''}`
                });
                const contentType = summaryResp.headers.get("content-type");
                if (summaryResp.ok && contentType && contentType.indexOf("application/json") !== -1) {
                    const summaryData = await summaryResp.json();
                    const payload = summaryData.success ? summaryData.data : null;
                    if (payload && payload.cartSummary) {
                        localStorage.setItem('userCart', JSON.stringify(payload.cartSummary.items || []));
                        GuestCart.updateBadge();
                    }
                } else {
                    console.warn('[CartSync] Failed to get valid JSON cart summary or response failed', summaryResp.status);
                }
            }
        } catch (err) {
            console.warn('[CartSync] Global sync failed:', err);
        }
    }
};

// ── 5. AJAX Helper ───────────────────────────────────────────────────────────
/**
 * ApiClient — Wrapper cho fetch với CSRF token tự động.
 *
 * CÁCH DÙNG:
 *   const data = await ApiClient.post('/api/cart/sync', { items: [] });
 */
const ApiClient = {
    getCsrfToken() {
        return document.querySelector('input[name="_csrf"]')?.value || '';
    },

    async post(url, body) {
        const resp = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': this.getCsrfToken()
            },
            body: JSON.stringify(body)
        });
        const contentType = resp.headers.get("content-type");
        if (!resp.ok || !contentType || contentType.indexOf("application/json") === -1) {
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errData = await resp.json();
                throw new Error(errData.message || errData.error || `Lỗi hệ thống (Mã: ${resp.status})`);
            }
            throw new Error(`Lỗi hệ thống (Mã: ${resp.status})`);
        }
        return resp.json();
    }
};

// ── 6. Notification AJAX helper ──────────────────────────────────────────────
const NotificationAjax = {
    getContextPath() {
        return window.contextPath || '';
    },

    getCsrfToken() {
        if (window.csrfToken) {
            return window.csrfToken;
        }
        if (typeof ApiClient !== 'undefined' && ApiClient.getCsrfToken) {
            return ApiClient.getCsrfToken();
        }
        return '';
    },

    buildUrl(path, params = {}) {
        const url = new URL(`${this.getContextPath()}${path}`, window.location.origin);
        Object.entries(params).forEach(([key, value]) => {
            if (value === undefined || value === null || value === '') {
                return;
            }
            url.searchParams.set(key, String(value));
        });
        return url.toString();
    },

    buildHeaders(method = 'GET', extraHeaders = {}) {
        const headers = {
            'X-Requested-With': 'XMLHttpRequest',
            ...extraHeaders
        };
        if (!['GET', 'HEAD'].includes(String(method).toUpperCase())) {
            headers['X-CSRF-Token'] = this.getCsrfToken();
        }
        return headers;
    },

    async requestJson(path, { method = 'GET', params = {}, extraHeaders = {} } = {}) {
        const response = await fetch(this.buildUrl(path, params), {
            method,
            credentials: 'same-origin',
            headers: this.buildHeaders(method, extraHeaders)
        });

        const contentType = response.headers.get('content-type') || '';
        let payload = null;
        if (contentType.includes('application/json')) {
            payload = await response.json();
        } else {
            const text = await response.text();
            payload = text ? { raw: text } : null;
        }

        if (!response.ok) {
            throw this.createError(response, payload);
        }
        if (payload && payload.success === false) {
            throw this.createError(response, payload);
        }
        return payload;
    },

    createError(response, payload) {
        const rawMessage = payload?.error || payload?.message || payload?.raw || `Lỗi hệ thống (Mã: ${response.status})`;
        const error = new Error(rawMessage);
        error.status = response.status;
        error.payload = payload;
        return error;
    },

    normalizeErrorMessage(error) {
        const raw = String(error?.message || '').trim();
        const status = Number(error?.status || 0);
        if (status === 401) {
            return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
        }
        if (status === 403) {
            return 'Không thể xác thực yêu cầu. Vui lòng tải lại trang rồi thử lại.';
        }
        if (status === 404) {
            return 'Không tìm thấy dữ liệu thông báo.';
        }
        if (status >= 500) {
            return 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau ít phút.';
        }
        if (!raw) {
            return 'Không thể xử lý thông báo. Vui lòng thử lại.';
        }

        const lower = raw.toLowerCase();
        if (lower.includes('csrf')) {
            return 'Phiên làm việc đã thay đổi hoặc hết hạn. Vui lòng tải lại trang rồi thử lại.';
        }
        if (lower.includes('chưa đăng nhập') || lower.includes('không đăng nhập')) {
            return 'Bạn cần đăng nhập lại để tiếp tục.';
        }
        if (lower.includes('failed to fetch') || lower.includes('network') || lower.includes('kết nối')) {
            return 'Không thể kết nối tới máy chủ. Kiểm tra mạng rồi thử lại.';
        }
        return raw;
    },

    escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    formatNotifTime(createdAt, createdAtAsDate) {
        let d = null;
        if (createdAtAsDate !== undefined && createdAtAsDate !== null && createdAtAsDate !== '') {
            const numeric = Number(createdAtAsDate);
            if (!Number.isNaN(numeric) && numeric > 0) {
                d = new Date(numeric);
            }
        }
        if (!d && Array.isArray(createdAt)) {
            d = new Date(createdAt[0], createdAt[1] - 1, createdAt[2], createdAt[3] || 0, createdAt[4] || 0, createdAt[5] || 0);
        } else if (!d && createdAt) {
            d = new Date(createdAt);
        }
        return d && !Number.isNaN(d.getTime())
            ? d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })
            : '';
    },

    updateBadgeElement(badge, count) {
        if (!badge) {
            return;
        }

        const safeCount = Math.max(0, Number(count) || 0);
        if (safeCount > 0) {
            badge.textContent = safeCount > 99 ? '99+' : String(safeCount);
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }
    },

    async getUnreadCounts() {
        const payload = await this.requestJson('/api/notifications/unread');
        return payload?.data || { unreadNotifications: 0, unreadChats: 0 };
    },

    async refreshBadges() {
        const counts = await this.getUnreadCounts();
        this.updateBadgeElement(document.getElementById('chat-badge'), counts.unreadChats);
        this.updateBadgeElement(document.getElementById('notif-badge'), counts.unreadNotifications);
        return counts;
    },

    async getRecentNotifications() {
        const payload = await this.requestJson('/api/notifications/recent');
        const notifications = payload?.data?.notifications;
        return Array.isArray(notifications) ? notifications : [];
    },

    async loadRecentNotifications(container) {
        if (!container) {
            return [];
        }

        this.renderLoading(container);
        try {
            const notifications = await this.getRecentNotifications();
            if (!notifications.length) {
                this.renderEmpty(container);
                return [];
            }
            this.renderRecentNotifications(container, notifications);
            return notifications;
        } catch (error) {
            this.renderError(container, error, () => this.loadRecentNotifications(container));
            return [];
        }
    },

    async markRead(notifId) {
        return this.requestJson('/api/notifications/markRead', {
            method: 'POST',
            params: { action: 'markRead', notifId }
        });
    },

    async deleteNotification(notifId) {
        return this.requestJson('/api/notifications/delete', {
            method: 'POST',
            params: { action: 'delete', notifId }
        });
    },

    async markAllRead() {
        return this.requestJson('/api/notifications/markAllRead', {
            method: 'POST',
            params: { action: 'markAllRead' }
        });
    },

    setButtonBusy(button, busy, busyLabel = 'Đang xử lý...') {
        if (!button) {
            return;
        }

        if (busy) {
            if (!button.dataset.originalText) {
                button.dataset.originalText = button.textContent || '';
            }
            button.disabled = true;
            button.textContent = busyLabel;
            button.style.opacity = '0.65';
            button.style.cursor = 'wait';
        } else {
            button.disabled = false;
            button.textContent = button.dataset.originalText || button.textContent || '';
            delete button.dataset.originalText;
            button.style.opacity = '';
            button.style.cursor = '';
        }
    },

    renderStatusCard(container, { tone = 'info', icon, title, message, actionLabel, onAction } = {}) {
        if (!container) {
            return;
        }

        let html = `
            <div class="notification-status-card" data-tone="${tone}">
                <div class="notification-status-card__surface">
                    <div class="notification-status-card__icon">
                        <span class="material-symbols-outlined text-[18px] leading-none">${icon}</span>
                    </div>
                    <div class="notification-status-card__body">
                        <div class="notification-status-card__title">${this.escapeHtml(title)}</div>
                        <div class="notification-status-card__message">${this.escapeHtml(message)}</div>`;

        if (actionLabel) {
            html += `
                        <div class="notification-status-card__actions">
                            <button type="button" data-notification-action class="notification-status-card__button">
                                ${this.escapeHtml(actionLabel)}
                            </button>
                        </div>`;
        }

        html += `
                    </div>
                </div>
            </div>`;

        container.innerHTML = html;
        const actionButton = container.querySelector('[data-notification-action]');
        if (actionButton && typeof onAction === 'function') {
            actionButton.addEventListener('click', function(event) {
                event.preventDefault();
                onAction();
            });
        }
    },

    renderLoading(container, message = 'Đang tải thông báo...') {
        this.renderStatusCard(container, {
            tone: 'info',
            icon: 'progress_activity',
            title: 'Đang tải thông báo',
            message
        });
    },

    renderEmpty(container, message = 'Bạn chưa có thông báo mới nào gần đây.') {
        this.renderStatusCard(container, {
            tone: 'success',
            icon: 'notifications_off',
            title: 'Chưa có thông báo',
            message
        });
    },

    renderError(container, error, retryHandler) {
        this.renderStatusCard(container, {
            tone: 'error',
            icon: 'warning',
            title: 'Không thể tải thông báo',
            message: this.normalizeErrorMessage(error),
            actionLabel: retryHandler ? 'Thử lại' : null,
            onAction: retryHandler
        });
    },

    renderRecentNotifications(container, notifications) {
        if (!container) {
            return;
        }

        const html = notifications.map(notification => this.buildNotificationItem(notification)).join('');
        container.innerHTML = html;
    },

    buildNotificationItem(notification) {
        const isRead = !!notification?.isRead;
        const dot = isRead ? '' : '<span class="mr-1 inline-block h-1.5 w-1.5 rounded-full bg-primary align-middle"></span>';
        const link = notification?.actionUrl ? `${this.getContextPath()}${notification.actionUrl}` : '#';
        const dateStr = this.formatNotifTime(notification?.createdAt, notification?.createdAtAsDate);
        const title = this.escapeHtml(notification?.title);
        const message = this.escapeHtml(notification?.message);

        return `
            <div class="notif-item relative ${isRead ? '' : 'unread'}" onclick='handleNotifClick(event, ${notification.notificationId}, ${JSON.stringify(link)})'>
                <span class="notif-item__icon notif-icon--system">
                    <span class="material-symbols-outlined text-[18px] leading-none">notifications</span>
                </span>
                <div class="notif-item__content">
                    <div class="flex items-center justify-between gap-2">
                        <span class="notif-item__title">
                        ${dot}${title}
                        </span>
                        <span class="text-[10px] text-slate-400 whitespace-nowrap">${dateStr}</span>
                    </div>
                    <p class="notif-item__msg">${message}</p>
                </div>
                <button type="button" onclick="handleNotifDelete(event, ${notification.notificationId})" class="absolute right-3 top-3 flex h-6 w-6 items-center justify-center rounded-md border-0 bg-transparent text-slate-400 transition-colors hover:bg-slate-100 hover:text-red-500" aria-label="Xóa thông báo">
                    &times;
                </button>
            </div>
        `;
    }
};
window.NotificationAjax = NotificationAjax;

// ── 7. Format tiền VND (phía client) ─────────────────────────────────────────
const CurrencyFmt = {
    format(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
};

// ── 8. TODO: WebSocket Chat ───────────────────────────────────────────────────
// Runtime chat được gắn trực tiếp trong các JSP chat page và dùng /ws/chat/{sessionId}.


// ── 9. Unified AJAX Add-to-Cart Helper (Global) ──────────────────────────────
/**
 * Thêm sản phẩm vào giỏ hàng qua AJAX (Dùng chung cho cả Home, Detail, List...)
 * Tự động gộp / cập nhật Local Storage userCart hoặc guestCart tương ứng.
 */
window.addCartItem = async function(variantId, quantity, name, price, imagePath, stockQuantity, productId, packagingId) {
    const isLoggedIn = window.isLoggedIn === true;
    const contextPath = window.contextPath || '';
    const normalizedVariantId = Number.parseInt(variantId, 10);
    const normalizedPackagingId = normalizePackagingId(packagingId);
    
    // 1. Optimistic Update of Local Storage & Badge
    let rollbackItems = null;
    try {
        if (isLoggedIn) {
            let items = JSON.parse(localStorage.getItem('userCart')) || [];
            rollbackItems = JSON.stringify(items);
            const idx = findCartItemIndexByVariantAndPackaging(items, normalizedVariantId, normalizedPackagingId);
            if (idx >= 0) {
                items[idx].quantity += Number.parseInt(quantity, 10);
            } else {
                items.push({
                    cartItemId: -1, // Temporary negative ID to prevent duplication key conflicts
                    variantId: normalizedVariantId,
                    productName: name.split(' - ')[0],
                    variantLabel: name.split(' - ')[1] || 'Mặc định',
                    price: parseFloat(price),
                    weightKg: 1.0,
                    quantity: Number.parseInt(quantity, 10),
                    imagePath: imagePath || 'assets/img/placeholder.png',
                    stockQuantity: Number.parseInt(stockQuantity, 10) || 0,
                    productId: parseInt(productId) || null,
                    packagingId: normalizedPackagingId
                });
            }
            localStorage.setItem('userCart', JSON.stringify(items));
        } else {
            let items = JSON.parse(localStorage.getItem('guestCart')) || [];
            rollbackItems = JSON.stringify(items);
            const idx = findCartItemIndexByVariantAndPackaging(items, normalizedVariantId, normalizedPackagingId);
            if (idx >= 0) {
                items[idx].quantity += Number.parseInt(quantity, 10);
            } else {
                items.push({
                    variantId: normalizedVariantId,
                    name: name,
                    price: parseFloat(price),
                    quantity: Number.parseInt(quantity, 10),
                    imagePath: imagePath || 'assets/img/placeholder.png',
                    stockQuantity: Number.parseInt(stockQuantity, 10) || 0,
                    productId: Number.parseInt(productId, 10) || null,
                    packagingId: normalizedPackagingId
                });
            }
            localStorage.setItem('guestCart', JSON.stringify(items));
        }

        // Update badge and card quantities immediately
        if (typeof GuestCart !== 'undefined') {
            GuestCart.updateBadge();
        } else {
            const badge = document.getElementById('cart-badge');
            if (badge) {
                const key = isLoggedIn ? 'userCart' : 'guestCart';
                const items = JSON.parse(localStorage.getItem(key)) || [];
                badge.textContent = items.reduce((sum, i) => sum + i.quantity, 0);
            }
        }
        if (window.updateCardAddedQuantities) {
            window.updateCardAddedQuantities();
        }

        // Show toast immediately (0ms latency UI feedback)
        showCartSuccessToast(name, quantity);

    } catch (e) {
        console.warn('[Cart] Error performing optimistic UI update:', e);
    }

    // 2. Perform Backend Request Asynchronously in the Background
    let requestBody = `variantId=${normalizedVariantId}&quantity=${quantity}&_csrf=${window.csrfToken || ''}`;
    if (normalizedPackagingId) {
        requestBody += `&packagingId=${normalizedPackagingId}`;
    }

    fetch(`${contextPath}/cart?action=add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: requestBody
    })
    .then(response => {
        const contentType = response.headers.get("content-type");
        if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
            if (contentType && contentType.indexOf("application/json") !== -1) {
                return response.json().then(errData => {
                    throw new Error(errData.error || `Lỗi hệ thống (Mã: ${response.status})`);
                });
            }
            throw new Error(`Lỗi hệ thống (Mã: ${response.status})`);
        }
        return response.json();
    })
    .then(data => {
        if (!data.success) {
            throw new Error(data.error || 'Có lỗi xảy ra khi thêm vào giỏ hàng.');
        }
        
        // Sync with backend final structure (e.g. database-assigned cartItemId)
        const payload = data.success ? data.data : null;
        if (isLoggedIn && payload && payload.cartSummary && payload.cartSummary.items) {
            const mappedItems = payload.cartSummary.items.map(item => ({
                cartItemId: item.cartItemId,
                variantId: item.variantId,
                productName: item.productName,
                variantLabel: item.variantLabel,
                price: item.price,
                weightKg: item.weightKg || 1.0,
                quantity: item.quantity,
                imagePath: item.imagePath,
                stockQuantity: item.stockQuantity,
                productId: item.productId,
                packagingId: normalizePackagingId(item.packagingId),
                packagingLabel: item.packagingLabel ?? null,
                packagingPriceAdd: item.packagingPriceAdd ?? null
            }));
            localStorage.setItem('userCart', JSON.stringify(mappedItems));
            if (window.updateCardAddedQuantities) {
                window.updateCardAddedQuantities();
            }
        }
    })
    .catch(err => {
        console.error('Error syncing add cart with server:', err);
        // Rollback local storage on error
        if (rollbackItems) {
            if (isLoggedIn) {
                localStorage.setItem('userCart', rollbackItems);
            } else {
                localStorage.setItem('guestCart', rollbackItems);
            }
            if (typeof GuestCart !== 'undefined') {
                GuestCart.updateBadge();
            } else {
                const badge = document.getElementById('cart-badge');
                if (badge) {
                    const key = isLoggedIn ? 'userCart' : 'guestCart';
                    const items = JSON.parse(localStorage.getItem(key)) || [];
                    badge.textContent = items.reduce((sum, i) => sum + i.quantity, 0);
                }
            }
            if (window.updateCardAddedQuantities) {
                window.updateCardAddedQuantities();
            }
        }
        window.showCartErrorModal(err.message || 'Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.');
    });

    return true; // Return true immediately to unblock frontend execution without delay
};

window.showCartErrorModal = function(message, title = 'Không thể thêm vào giỏ hàng') {
    const text = message || 'Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.';
    if (typeof Swal !== 'undefined') {
        return Swal.fire({
            icon: 'error',
            title,
            text,
            confirmButtonText: 'Đã hiểu',
            confirmButtonColor: '#14532D',
            background: '#ffffff',
            customClass: {
                popup: 'premium-swal-popup',
                title: 'premium-swal-title',
                confirmButton: 'premium-swal-button'
            }
        });
    }
    alert(text);
};

function showCartSuccessToast(productName, quantity) {
    let toast = document.getElementById('cart-added-toast');
    if (!toast) {
        const toastHtml = `
            <div id="cart-added-toast" class="premium-toast font-sans">
                <span class="premium-toast-icon"><i class="fa-solid fa-circle-check"></i></span>
                <div>
                    <strong class="block font-bold">Thành công!</strong>
                    <span class="text-xs" id="toast-message">Đã thêm sản phẩm vào giỏ hàng.</span>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', toastHtml);
        toast = document.getElementById('cart-added-toast');
    }
    
    const msgEl = document.getElementById('toast-message');
    if (msgEl) {
        msgEl.innerHTML = `Đã thêm <strong>${quantity}</strong> x <strong>${productName}</strong> vào giỏ hàng.`;
    }
    
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3500);
}
