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
 * Xem js/components/chat.js (TODO)
 */

// ── 1. Init ──────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    Alert.autoHide();
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
        const idx   = items.findIndex(i => i.variantId === item.variantId);
        if (idx >= 0) { items[idx].quantity += item.quantity; }
        else          { items.push(item); }
        this.save(items);
    },

    remove(variantId) {
        this.save(this.getItems().filter(i => i.variantId !== variantId));
    },

    updateQuantity(variantId, quantity) {
        const items = this.getItems().map(i => i.variantId === variantId ? { ...i, quantity } : i);
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
        if (badge) badge.textContent = this.getCount();
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
            const resp = await fetch(`${contextPath}/api/cart/sync`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ items })
            });
            if (resp.ok) {
                GuestCart.clear();
                console.log('[CartSync] Guest cart synced successfully.');
            }
        } catch (err) {
            console.warn('[CartSync] Sync failed:', err);
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
        return resp.json();
    }
};

// ── 6. Format tiền VND (phía client) ─────────────────────────────────────────
const CurrencyFmt = {
    format(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
};

// ── 7. TODO: WebSocket Chat ───────────────────────────────────────────────────
// Xem js/components/chat.js
// Endpoint: ws://host/ctx/chat-ws?sessionId=xxx
// Tạo ChatWebSocketServlet.java trong package servlet.api
