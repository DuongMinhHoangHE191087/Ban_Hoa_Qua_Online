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
                headers: { 'Content-Type': 'application/json' },
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
                const summaryData = await summaryResp.json();
                if (summaryData.success && summaryData.cartSummary) {
                    localStorage.setItem('userCart', JSON.stringify(summaryData.cartSummary.items || []));
                    GuestCart.updateBadge();
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


// ── 8. Unified AJAX Add-to-Cart Helper (Global) ──────────────────────────────
/**
 * Thêm sản phẩm vào giỏ hàng qua AJAX (Dùng chung cho cả Home, Detail, List...)
 * Tự động gộp / cập nhật Local Storage userCart hoặc guestCart tương ứng.
 */
window.addCartItem = async function(variantId, quantity, name, price, imagePath, stockQuantity, productId) {
    try {
        const isLoggedIn = window.isLoggedIn === true;
        const contextPath = window.contextPath || '';
        
        const response = await fetch(`${contextPath}/cart?action=add`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: `variantId=${variantId}&quantity=${quantity}&_csrf=${window.csrfToken || ''}`
        });
        const data = await response.json();
        
        if (data.success) {
            if (isLoggedIn) {
                if (data.cartSummary && data.cartSummary.items) {
                    const mappedItems = data.cartSummary.items.map(item => ({
                        cartItemId: item.cartItemId,
                        variantId: item.variantId,
                        productName: item.productName,
                        variantLabel: item.variantLabel,
                        price: item.price,
                        weightKg: item.weightKg || 1.0,
                        quantity: item.quantity,
                        imagePath: item.imagePath,
                        stockQuantity: item.stockQuantity,
                        productId: item.productId
                    }));
                    localStorage.setItem('userCart', JSON.stringify(mappedItems));
                }
            } else {
                const item = {
                    variantId: parseInt(variantId),
                    name: name,
                    price: parseFloat(price),
                    quantity: parseInt(quantity),
                    imagePath: imagePath || 'assets/img/placeholder.png',
                    stockQuantity: parseInt(stockQuantity) || 99,
                    productId: parseInt(productId) || null
                };
                if (typeof GuestCart !== 'undefined') {
                    GuestCart.add(item);
                } else {
                    let items = JSON.parse(localStorage.getItem('guestCart')) || [];
                    const idx = items.findIndex(i => i.variantId === item.variantId);
                    if (idx >= 0) items[idx].quantity += item.quantity;
                    else items.push(item);
                    localStorage.setItem('guestCart', JSON.stringify(items));
                }
            }
            
            // Cập nhật badge trên navbar
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
            
            // Cập nhật số lượng trên các thẻ sản phẩm tại trang chủ (nếu có)
            if (window.updateCardAddedQuantities) {
                window.updateCardAddedQuantities();
            }
            
            // Hiển thị toast thông báo thành công
            showCartSuccessToast(name, quantity);
            return true;
        } else {
            alert(data.error || 'Có lỗi xảy ra khi thêm vào giỏ hàng.');
            return false;
        }
    } catch (err) {
        console.error('Lỗi thêm giỏ hàng:', err);
        alert('Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.');
        return false;
    }
};

function showCartSuccessToast(productName, quantity) {
    let toast = document.getElementById('cart-added-toast');
    if (!toast) {
        const toastHtml = `
            <div id="cart-added-toast" class="premium-toast font-sans">
                <span class="premium-toast-icon"><i class="fa-solid fa-circle-check"></i></span>
                <div>
                    <strong style="display: block; font-weight: 700;">Thành công!</strong>
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
