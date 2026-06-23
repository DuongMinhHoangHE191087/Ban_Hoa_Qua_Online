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

// ── 6. Format tiền VND (phía client) ─────────────────────────────────────────
const CurrencyFmt = {
    format(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
};

// ── 7. TODO: WebSocket Chat ───────────────────────────────────────────────────
// Runtime chat được gắn trực tiếp trong các JSP chat page và dùng /ws/chat/{sessionId}.


// ── 8. Unified AJAX Add-to-Cart Helper (Global) ──────────────────────────────
/**
 * Thêm sản phẩm vào giỏ hàng qua AJAX (Dùng chung cho cả Home, Detail, List...)
 * Tự động gộp / cập nhật Local Storage userCart hoặc guestCart tương ứng.
 */
window.addCartItem = async function(variantId, quantity, name, price, imagePath, stockQuantity, productId, packagingId) {
    const isLoggedIn = window.isLoggedIn === true;
    const contextPath = window.contextPath || '';
    
    // 1. Optimistic Update of Local Storage & Badge
    let rollbackItems = null;
    try {
        if (isLoggedIn) {
            let items = JSON.parse(localStorage.getItem('userCart')) || [];
            rollbackItems = JSON.stringify(items);
            const idx = items.findIndex(i => i.variantId === parseInt(variantId));
            if (idx >= 0) {
                items[idx].quantity += parseInt(quantity);
            } else {
                items.push({
                    cartItemId: -1, // Temporary negative ID to prevent duplication key conflicts
                    variantId: parseInt(variantId),
                    productName: name.split(' - ')[0],
                    variantLabel: name.split(' - ')[1] || 'Mặc định',
                    price: parseFloat(price),
                    weightKg: 1.0,
                    quantity: parseInt(quantity),
                    imagePath: imagePath || 'assets/img/placeholder.png',
                    stockQuantity: parseInt(stockQuantity) || 99,
                    productId: parseInt(productId) || null,
                    packagingId: packagingId ? parseInt(packagingId) : null
                });
            }
            localStorage.setItem('userCart', JSON.stringify(items));
        } else {
            let items = JSON.parse(localStorage.getItem('guestCart')) || [];
            rollbackItems = JSON.stringify(items);
            const idx = items.findIndex(i => i.variantId === parseInt(variantId));
            if (idx >= 0) {
                items[idx].quantity += parseInt(quantity);
            } else {
                items.push({
                    variantId: parseInt(variantId),
                    name: name,
                    price: parseFloat(price),
                    quantity: parseInt(quantity),
                    imagePath: imagePath || 'assets/img/placeholder.png',
                    stockQuantity: parseInt(stockQuantity) || 99,
                    productId: parseInt(productId) || null,
                    packagingId: packagingId ? parseInt(packagingId) : null
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
    let requestBody = `variantId=${variantId}&quantity=${quantity}&_csrf=${window.csrfToken || ''}`;
    if (packagingId) {
        requestBody += `&packagingId=${packagingId}`;
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
                packagingId: item.packagingId ?? null,
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
        alert(err.message || 'Lỗi kết nối mạng. Không thể thêm vào giỏ hàng.');
    });

    return true; // Return true immediately to unblock frontend execution without delay
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
