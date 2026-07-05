/**
 * cart.js — JavaScript quản lý trang Giỏ hàng (Cart)
 * Hỗ trợ: Lưu Local Storage, Ajax mượt mà, Beacon API khi tắt tab, Check Stock khi Checkout.
 */

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[char]));
}

function readStockQuantity(value, fallback = 0) {
    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? fallback : parsed;
}

function buildCartStockState(stockQuantity, requestedQuantity) {
    const stock = readStockQuantity(stockQuantity, 0);
    const quantity = readStockQuantity(requestedQuantity, 0);
    const outOfStock = stock <= 0;
    const overLimit = stock > 0 && quantity > stock;
    return {
        stock,
        quantity,
        outOfStock,
        overLimit,
        blocked: outOfStock || overLimit
    };
}

function getCartStockDisplayText(stockState) {
    if (stockState.outOfStock) {
        return 'Đã hết hàng';
    }
    if (stockState.overLimit || stockState.stock <= 10) {
        return `Chỉ còn ${stockState.stock} sản phẩm`;
    }
    return `${stockState.stock} sản phẩm`;
}

const CartPage = {
    isLoggedIn: false,
    contextPath: '',
    userCartKey: 'userCart', // Key lưu giỏ hàng của user đã đăng nhập ở Local Storage
    selectedVariantKey: 'cartSelectedVariantIds',
    productVariantsCache: {},

    async safeParseJSON(response) {
        const contentType = response.headers.get("content-type");
        if (!response.ok || !contentType || contentType.indexOf("application/json") === -1) {
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errData = await response.json();
                throw new Error(errData.error || errData.message || `Lỗi hệ thống (Mã: ${response.status})`);
            }
            throw new Error(`Lỗi hệ thống (Mã: ${response.status})`);
        }
        return response.json();
    },

    init(isLoggedIn, contextPath) {
        this.isLoggedIn = isLoggedIn;
        this.contextPath = contextPath;
        
        console.log(`[CartPage] Initialized. LoggedIn: ${this.isLoggedIn}`);

        // 1. Nếu có guestCart khi vừa đăng nhập -> thực hiện đồng bộ gộp vào DB
        if (this.isLoggedIn) {
            this.syncGuestCartOnLogin();
        }

        // 2. Render tức thì từ Local Storage để tạo phản hồi nhanh (0ms)
        this.renderImmediate();

        // 3. Gọi Ajax ngầm lấy dữ liệu giỏ hàng chuẩn từ Server (nếu đã đăng nhập)
        this.loadAndSyncFromServer();

        // 4. Đăng ký sự kiện tắt tab / đóng trình duyệt để đồng bộ DB (Beacon API)
        this.registerUnloadSync();

        // 5. Đăng ký các sự kiện tương tác UI
        this.registerEvents();
    },

    /**
     * Đồng bộ gộp giỏ hàng khách vãng lai lên server khi đăng nhập thành công.
     */
    async syncGuestCartOnLogin() {
        const guestItems = GuestCart.getItems();
        if (guestItems.length === 0) return;

        try {
            console.log('[CartPage] Syncing guest cart to server...');
            const response = await fetch(`${this.contextPath}/cart?action=sync`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': window.csrfToken || ''
                },
                body: `guestCart=${encodeURIComponent(JSON.stringify(guestItems))}`
            });
            const data = await this.safeParseJSON(response);
            if (data.success) {
                // Xóa giỏ hàng guest vì đã gộp thành công
                GuestCart.clear();
                // Lưu giỏ hàng user đã gộp vào Local Storage
                const payload = data.data;
                if (payload && payload.cartSummary && payload.cartSummary.items) {
                    this.saveUserCartToLocal(payload.cartSummary.items);
                    this.renderCart(payload.cartSummary);
                }
            }
        } catch (err) {
            console.warn('[CartPage] Lỗi đồng bộ guest cart:', err);
        }
    },

    /**
     * Lưu giỏ hàng user vào Local Storage
     */
    saveUserCartToLocal(items) {
        const mappedItems = items.map(item => ({
            cartItemId: item.cartItemId,
            variantId: item.variantId,
            productName: item.productName,
            variantLabel: item.variantLabel,
            price: item.price,
            weightKg: item.weightKg || 1.0,
            quantity: item.quantity,
            imagePath: item.imagePath,
            stockQuantity: item.stockQuantity,
            packagingId: item.packagingId,
            packagingLabel: item.packagingLabel,
            packagingPriceAdd: item.packagingPriceAdd,
            shopId: item.shopId,
            shopName: item.shopName
        }));
        localStorage.setItem(this.userCartKey, JSON.stringify(mappedItems));
        
        // Cập nhật badge navbar
        const totalQty = mappedItems.reduce((sum, i) => sum + i.quantity, 0);
        const badge = document.getElementById('cart-badge');
        if (badge) badge.textContent = totalQty;
    },

    /**
     * Lưu danh sách variant đang được chọn để giữ state qua các lần re-render.
     */
    saveSelectedVariantIds(variantIds) {
        try {
            const uniqueIds = Array.from(new Set((variantIds || [])
                .map(id => parseInt(id, 10))
                .filter(id => Number.isInteger(id) && id > 0)));
            localStorage.setItem(this.selectedVariantKey, JSON.stringify(uniqueIds));
        } catch {
            // Không chặn luồng checkout nếu localStorage không ghi được.
        }
    },

    /**
     * Đọc danh sách variant đang được chọn từ localStorage.
     */
    getPersistedSelectedVariantIds() {
        try {
            const raw = localStorage.getItem(this.selectedVariantKey);
            if (!raw) return [];
            const parsed = JSON.parse(raw);
            if (!Array.isArray(parsed)) return [];
            return parsed
                .map(id => parseInt(id, 10))
                .filter(id => Number.isInteger(id) && id > 0);
        } catch {
            return [];
        }
    },

    /**
     * Lấy danh sách variant đang được tick trên DOM hiện tại.
     */
    getCheckedVariantIdsFromDom() {
        return Array.from(document.querySelectorAll('.chk-item:checked'))
            .filter(chk => !chk.disabled && chk.closest('.cart-item-row')?.getAttribute('data-stock-blocked') !== 'true')
            .map(chk => parseInt(chk.getAttribute('data-variant-id'), 10))
            .filter(id => Number.isInteger(id) && id > 0);
    },

    /**
     * Khôi phục checkbox theo state đã lưu trước đó.
     * Nếu chưa có state nào, mặc định chọn toàn bộ như hành vi cũ.
     */
    restoreSelectedVariantState(items) {
        const persistedIds = this.getPersistedSelectedVariantIds();
        const hasPersistedState = persistedIds.length > 0;
        const selectedSet = hasPersistedState ? new Set(persistedIds) : null;
        const selectableIds = new Set(this.getSelectableVariantIds(items));

        document.querySelectorAll('.cart-item-row').forEach(row => {
            const chk = row.querySelector('.chk-item');
            if (!chk) return;
            const variantId = parseInt(chk.getAttribute('data-variant-id'), 10);
            const stockQuantity = readStockQuantity(row.getAttribute('data-stock-quantity'), 0);
            const requestedQuantity = readStockQuantity(row.getAttribute('data-requested-quantity'), 0);
            const stockState = buildCartStockState(stockQuantity, requestedQuantity);

            row.dataset.stockBlocked = stockState.blocked ? 'true' : 'false';
            row.classList.toggle('opacity-50', stockState.outOfStock);
            row.classList.toggle('grayscale', stockState.outOfStock);
            row.classList.toggle('bg-rose-50/60', stockState.outOfStock);
            row.classList.toggle('border-rose-200', stockState.outOfStock);
            row.classList.toggle('bg-amber-50/60', stockState.overLimit);
            row.classList.toggle('border-amber-200', stockState.overLimit);

            if (!Number.isInteger(variantId) || variantId <= 0) {
                chk.checked = false;
                chk.disabled = true;
                return;
            }
            chk.disabled = stockState.blocked;
            chk.checked = stockState.blocked ? false : (selectedSet ? selectedSet.has(variantId) : true);

            const qtyInput = row.querySelector('.input-qty');
            const minusBtn = row.querySelector('.btn-qty-minus');
            const plusBtn = row.querySelector('.btn-qty-plus');
            if (qtyInput) {
                qtyInput.disabled = stockState.outOfStock;
                qtyInput.max = stockState.stock > 0 ? stockState.stock : 1;
            }
            if (minusBtn) {
                minusBtn.disabled = stockState.outOfStock;
                minusBtn.classList.toggle('opacity-40', stockState.outOfStock);
                minusBtn.classList.toggle('cursor-not-allowed', stockState.outOfStock);
            }
            if (plusBtn) {
                plusBtn.disabled = stockState.outOfStock;
                plusBtn.classList.toggle('opacity-40', stockState.outOfStock);
                plusBtn.classList.toggle('cursor-not-allowed', stockState.outOfStock);
            }
        });

        const effectiveSelection = hasPersistedState
            ? persistedIds.filter(id => selectableIds.has(id))
            : Array.from(selectableIds);
        this.saveSelectedVariantIds(effectiveSelection);
        this.syncCartSelectionControls();
    },

    /**
     * Đọc giỏ hàng user từ Local Storage
     */
    getUserCartFromLocal() {
        try {
            return JSON.parse(localStorage.getItem(this.userCartKey)) || [];
        } catch {
            return [];
        }
    },

    /**
     * Lấy toàn bộ items giỏ hàng hiện tại tùy trạng thái đăng nhập
     */
    getCurrentLocalItems() {
        return this.isLoggedIn ? this.getUserCartFromLocal() : GuestCart.getItems();
    },

    getSelectableVariantIds(items) {
        return (items || [])
            .map(item => {
                const variantId = parseInt(item.variantId, 10);
                if (!Number.isInteger(variantId) || variantId <= 0) {
                    return null;
                }
                const stockState = buildCartStockState(item.stockQuantity, item.quantity);
                return stockState.blocked ? null : variantId;
            })
            .filter(id => Number.isInteger(id) && id > 0);
    },

    hasBlockingStockIssues(items) {
        return (items || []).some(item => buildCartStockState(item.stockQuantity, item.quantity).blocked);
    },

    isStaleCartItemError(data) {
        const errorText = String(data?.error || data?.message || '');
        const metaErrorCode = data?.errorCode || data?.meta?.errorCode || '';
        return metaErrorCode === 'cart_item_not_found'
            || errorText.includes('Không tìm thấy sản phẩm này trong giỏ hàng')
            || errorText.includes('Sản phẩm không thuộc giỏ hàng của bạn');
    },

    handleStaleCartItemError(data) {
        if (this.isStaleCartItemError(data)) {
            this.loadAndSyncFromServer();
            return true;
        }
        return false;
    },

    syncCartSelectionControls() {
        const selectableItemCheckboxes = Array.from(document.querySelectorAll('.chk-item'))
            .filter(chk => !chk.disabled);
        const allSelectableChecked = selectableItemCheckboxes.length > 0
            && selectableItemCheckboxes.every(chk => chk.checked);

        const chkSelectAll = document.getElementById('chk-select-all');
        if (chkSelectAll) {
            chkSelectAll.checked = allSelectableChecked;
            chkSelectAll.disabled = selectableItemCheckboxes.length === 0;
            chkSelectAll.classList.toggle('opacity-50', selectableItemCheckboxes.length === 0);
            chkSelectAll.classList.toggle('cursor-not-allowed', selectableItemCheckboxes.length === 0);
        }

        document.querySelectorAll('.shop-group').forEach(group => {
            const shopCheckbox = group.querySelector('.chk-shop');
            if (!shopCheckbox) return;

            const selectableShopCheckboxes = Array.from(group.querySelectorAll('.chk-item'))
                .filter(chk => !chk.disabled);
            const allShopChecked = selectableShopCheckboxes.length > 0
                && selectableShopCheckboxes.every(chk => chk.checked);

            shopCheckbox.checked = allShopChecked;
            shopCheckbox.disabled = selectableShopCheckboxes.length === 0;
            shopCheckbox.classList.toggle('opacity-50', selectableShopCheckboxes.length === 0);
            shopCheckbox.classList.toggle('cursor-not-allowed', selectableShopCheckboxes.length === 0);
        });
    },

    updateCheckoutButtonState(items) {
        const btnCheckout = document.getElementById('btn-cart-checkout');
        if (!btnCheckout) return;

        const checkedVariantIds = this.getCheckedVariantIdsFromDom();
        const checkedVariantSet = new Set(checkedVariantIds);
        const selectedItems = (items || []).filter(item => {
            const variantId = parseInt(item.variantId, 10);
            return Number.isInteger(variantId) && variantId > 0 && checkedVariantSet.has(variantId);
        });
        const hasBlockingStockIssues = selectedItems.some(item => buildCartStockState(item.stockQuantity, item.quantity).blocked);
        const shouldDisable = checkedVariantIds.length === 0 || hasBlockingStockIssues;

        btnCheckout.disabled = shouldDisable;
        btnCheckout.classList.toggle('opacity-50', shouldDisable);
        btnCheckout.classList.toggle('cursor-not-allowed', shouldDisable);
        btnCheckout.title = hasBlockingStockIssues
            ? 'Vui lòng xử lý các sản phẩm hết hàng trước khi thanh toán.'
            : (checkedVariantIds.length === 0
                ? 'Vui lòng chọn ít nhất một sản phẩm để thanh toán.'
                : '');
    },

    /**
     * Render giỏ hàng tức thì từ Local Storage (không chờ server)
     */
    renderImmediate() {
        const localItems = this.getCurrentLocalItems();
        if (localItems.length === 0) {
            this.renderEmptyState();
            return;
        }

        // Tạo cấu trúc CartSummary giả định từ client để render trước
        let totalCents = 0;
        let totalGrams = 0;
        localItems.forEach(item => {
            const price = parseFloat(item.price) || 0;
            const weight = parseFloat(item.weightKg) || 1.0;
            totalCents += Math.round(price) * item.quantity;
            totalGrams += Math.round(weight * 1000) * item.quantity;
        });

        const subtotal = totalCents;
        const totalWeight = totalGrams / 1000;

        const dummySummary = {
            items: localItems,
            subtotal: subtotal,
            discountAmount: 0,
            deliveryFee: 0,
            total: subtotal,
            totalWeight: totalWeight
        };

        this.renderCart(dummySummary);
    },

    /**
     * Gọi Ajax lấy giỏ hàng chuẩn từ DB (nếu đã đăng nhập) và cập nhật
     */
    async loadAndSyncFromServer() {
        if (!this.isLoggedIn) return;

        try {
            const response = await fetch(`${this.contextPath}/cart?format=json`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const data = await this.safeParseJSON(response);
            const payload = data.success ? data.data : null;
            if (payload && payload.cartSummary) {
                // Lưu bản chuẩn vào Local Storage
                this.saveUserCartToLocal(payload.cartSummary.items);
                // Render lại UI với dữ liệu chuẩn xác nhất từ DB (nếu có biến động giá/tồn kho)
                this.renderCart(payload.cartSummary);
            }
        } catch (err) {
            console.warn('[CartPage] Không thể kết nối server để đồng bộ giỏ hàng:', err);
        }
    },

    /**
     * Đăng ký sự kiện tắt tab để đồng bộ database qua Beacon API
     */
    registerUnloadSync() {
        window.addEventListener('beforeunload', () => {
            if (this.isLoggedIn) {
                const localItems = this.getUserCartFromLocal().map(i => ({
                    variantId: i.variantId,
                    quantity: i.quantity
                }));
                const blob = new Blob([JSON.stringify({ items: localItems })], { type: 'application/json' });
                navigator.sendBeacon(`${this.contextPath}/cart?action=syncOnUnload`, blob);
            }
        });
    },

    /**
     * Đăng ký các sự kiện click, nút tăng giảm số lượng, nút xóa, đổi biến thể và checkbox
     */
    registerEvents() {
        const cartContainer = document.getElementById('cart-items-container');
        if (!cartContainer) return;

        // Sự kiện tăng giảm số lượng, xóa
        cartContainer.addEventListener('click', (e) => {
            const target = e.target;
            
            // Nút giảm số lượng (dùng closest để nhận diện chính xác kể cả click vào icon)
            if (target.closest('.btn-qty-minus')) {
                const btn = target.closest('.btn-qty-minus');
                const spinner = btn.closest('.quantity-spinner');
                const input = spinner.querySelector('.input-qty');
                const display = spinner.querySelector('.input-qty-value');
                let val = parseInt(input.value) || 1;
                
                if (val > 1) {
                    val--;
                    input.value = val;
                    if (display) display.textContent = val;
                    this.handleQuantityChange(btn, val);
                }
            }

            // Nút tăng số lượng (dùng closest)
            if (target.closest('.btn-qty-plus')) {
                const btn = target.closest('.btn-qty-plus');
                const spinner = btn.closest('.quantity-spinner');
                const input = spinner.querySelector('.input-qty');
                const display = spinner.querySelector('.input-qty-value');
                const maxStock = readStockQuantity(input.getAttribute('data-stock'), 999);
                let val = parseInt(input.value) || 1;
                
                if (maxStock <= 0) {
                    this.showToast('Sản phẩm này hiện đã hết hàng. Vui lòng xóa khỏi giỏ hàng hoặc chọn phân loại khác.', 'warning');
                } else if (val < maxStock) {
                    val++;
                    input.value = val;
                    if (display) display.textContent = val;
                    this.handleQuantityChange(btn, val);
                } else {
                    this.showToast(`Chỉ còn tối đa ${maxStock} sản phẩm trong kho!`, 'warning');
                }
            }

            // Nút xóa sản phẩm
            if (target.closest('.btn-remove-item')) {
                const btn = target.closest('.btn-remove-item');
                const row = btn.closest('.cart-item-row');
                const cartItemId = btn.getAttribute('data-id');
                const variantId = btn.getAttribute('data-variant-id');
                
                this.handleRemoveItem(row, cartItemId, parseInt(variantId));
            }
        });

        // Bắt sự kiện check/uncheck tất cả
        const chkSelectAll = document.getElementById('chk-select-all');
        if (chkSelectAll) {
            chkSelectAll.addEventListener('change', () => {
                const shopCheckboxes = Array.from(document.querySelectorAll('.chk-shop')).filter(chk => !chk.disabled);
                shopCheckboxes.forEach(chk => {
                    chk.checked = chkSelectAll.checked;
                });
                const checkboxes = Array.from(document.querySelectorAll('.chk-item')).filter(chk => !chk.disabled);
                checkboxes.forEach(chk => {
                    chk.checked = chkSelectAll.checked;
                });
                this.saveSelectedVariantIds(this.getCheckedVariantIdsFromDom());
                this.syncCartSelectionControls();
                this.recalculateClientSummary(this.getCurrentLocalItems());
            });
        }

        // Bắt sự kiện check/uncheck của từng shop (delegated)
        cartContainer.addEventListener('change', (e) => {
            const target = e.target;
            if (target.classList.contains('chk-shop')) {
                const shopId = target.getAttribute('data-shop-id');
                const itemsInShop = Array.from(document.querySelectorAll(`.chk-item[data-shop-id="${shopId}"]`))
                    .filter(chk => !chk.disabled);
                itemsInShop.forEach(chk => {
                    chk.checked = target.checked;
                });
                
                // Cập nhật chkSelectAll
                const allShopCheckboxes = Array.from(document.querySelectorAll('.chk-shop')).filter(chk => !chk.disabled);
                const allChecked = allShopCheckboxes.length > 0 && allShopCheckboxes.every(chk => chk.checked);
                if (chkSelectAll) chkSelectAll.checked = allChecked;
                
                this.saveSelectedVariantIds(this.getCheckedVariantIdsFromDom());
                this.syncCartSelectionControls();
                this.recalculateClientSummary(this.getCurrentLocalItems());
            }
        });

        // Bắt sự kiện check từng dòng
        cartContainer.addEventListener('change', (e) => {
            const target = e.target;
            if (target.classList.contains('chk-item')) {
                const shopId = target.getAttribute('data-shop-id');
                const itemsInShop = Array.from(document.querySelectorAll(`.chk-item[data-shop-id="${shopId}"]`))
                    .filter(chk => !chk.disabled);
                const allShopItemsChecked = itemsInShop.length > 0 && itemsInShop.every(chk => chk.checked);
                
                const shopCheckbox = document.querySelector(`.chk-shop[data-shop-id="${shopId}"]`);
                if (shopCheckbox) shopCheckbox.checked = allShopItemsChecked;
                
                const checkboxes = Array.from(document.querySelectorAll('.chk-item')).filter(chk => !chk.disabled);
                const allChecked = checkboxes.length > 0 && checkboxes.every(chk => chk.checked);
                if (chkSelectAll) chkSelectAll.checked = allChecked;
                
                this.saveSelectedVariantIds(this.getCheckedVariantIdsFromDom());
                this.syncCartSelectionControls();
                this.recalculateClientSummary(this.getCurrentLocalItems());
            }
        });

        // Bắt sự kiện gõ/thay đổi số lượng trực tiếp
        cartContainer.addEventListener('change', (e) => {
            const target = e.target;
            if (target.classList.contains('input-qty')) {
                const input = target;
                const maxStock = readStockQuantity(input.getAttribute('data-stock'), 999);
                let val = parseInt(input.value) || 1;
                
                if (maxStock <= 0) {
                    input.value = 1;
                    this.showToast('Sản phẩm này hiện đã hết hàng. Vui lòng xóa khỏi giỏ hàng hoặc chọn phân loại khác.', 'warning');
                    return;
                }

                if (isNaN(val) || val < 1) {
                    val = 1;
                } else if (val > maxStock) {
                    val = maxStock;
                    this.showToast(`Chỉ còn tối đa ${maxStock} sản phẩm trong kho!`, 'warning');
                }
                input.value = val;
                this.handleQuantityChange(input, val);
            }
        });

        // Bắt sự kiện thay đổi biến thể trực tiếp trong giỏ
        cartContainer.addEventListener('change', async (e) => {
            const target = e.target;
            if (target.classList.contains('cart-variant-select')) {
                const cartItemId = target.getAttribute('data-item-id');
                const oldVariantId = parseInt(target.getAttribute('data-current-variant-id'));
                const newVariantId = parseInt(target.value);
                const productId = target.getAttribute('data-product-id');

                if (oldVariantId === newVariantId) return;

                const variants = this.productVariantsCache[productId] || [];
                const newVariant = variants.find(v => v.variantId === newVariantId);

                if (!newVariant) {
                    this.showToast('Không tìm thấy thông tin phân loại mới.', 'error');
                    target.value = oldVariantId;
                    return;
                }

                if (newVariant.stockQuantity <= 0) {
                    this.showToast('Rất tiếc! Phân loại này hiện đã hết hàng.', 'warning');
                    target.value = oldVariantId;
                    return;
                }

                let localItems = this.getCurrentLocalItems();
                const row = target.closest('.cart-item-row');
                const rowCheckbox = row ? row.querySelector('.chk-item') : null;
                const wasChecked = rowCheckbox ? rowCheckbox.checked : false;
                const quantityInput = row.querySelector('.input-qty');
                let qty = parseInt(quantityInput.value) || 1;

                if (qty > newVariant.stockQuantity) {
                    qty = newVariant.stockQuantity;
                    this.showToast(`Số lượng được tự động giảm xuống ${qty} để phù hợp với kho hàng.`, 'warning');
                }

                if (this.isLoggedIn) {
                    try {
                        const response = await fetch(`${this.contextPath}/cart?action=changeVariant&cartItemId=${cartItemId}&newVariantId=${newVariantId}`, {
                            method: 'POST',
                            headers: {
                                'X-Requested-With': 'XMLHttpRequest',
                                'X-CSRF-Token': window.csrfToken || ''
                            }
                        });
                        const data = await this.safeParseJSON(response);
                        if (data.success) {
                            const selectedVariantIds = this.getPersistedSelectedVariantIds().filter(id => id !== oldVariantId);
                            if (wasChecked) {
                                selectedVariantIds.push(newVariantId);
                            }
                            this.saveSelectedVariantIds(selectedVariantIds);
                            this.showToast('Đã đổi phân loại thành công!', 'success');
                            this.loadAndSyncFromServer();
                        } else {
                            if (!this.handleStaleCartItemError(data)) {
                                this.showToast(data.error || 'Lỗi đổi phân loại.', 'error');
                            }
                            target.value = oldVariantId;
                        }
                    } catch (err) {
                        this.showToast('Lỗi mạng. Vui lòng thử lại.', 'error');
                        target.value = oldVariantId;
                    }
                } else {
                    const existingIdx = localItems.findIndex(i => i.variantId === newVariantId);
                    const currentIdx = localItems.findIndex(i => i.variantId === oldVariantId);

                    if (existingIdx >= 0 && existingIdx !== currentIdx) {
                        let newQty = localItems[currentIdx].quantity + localItems[existingIdx].quantity;
                        if (newQty > newVariant.stockQuantity) newQty = newVariant.stockQuantity;
                        localItems[existingIdx].quantity = newQty;
                        localItems.splice(currentIdx, 1);
                    } else {
                        localItems[currentIdx] = {
                            ...localItems[currentIdx],
                            variantId: newVariantId,
                            variantLabel: newVariant.variantLabel,
                            price: newVariant.price,
                            weightKg: newVariant.weightKg || 1.0,
                            quantity: qty,
                            stockQuantity: newVariant.stockQuantity
                        };
                    }
                    const selectedVariantIds = this.getPersistedSelectedVariantIds().filter(id => id !== oldVariantId);
                    if (wasChecked) {
                        selectedVariantIds.push(newVariantId);
                    }
                    this.saveSelectedVariantIds(selectedVariantIds);
                    GuestCart.save(localItems);
                    this.showToast('Đã đổi phân loại thành công!', 'success');
                    this.renderCart({ items: localItems });
                }
            }
        });

        // Nút tiến hành thanh toán
        const btnCheckout = document.getElementById('btn-cart-checkout');
        if (btnCheckout) {
            btnCheckout.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleCheckout();
            });
        }
    },

    /**
     * Xử lý khi tăng/giảm số lượng sản phẩm
     */
    async handleQuantityChange(element, quantity) {
        const row = element.closest('.cart-item-row');
        const cartItemId = row.getAttribute('data-item-id');
        const variantId = parseInt(row.getAttribute('data-variant-id'));

        // Cập nhật Local Storage tức thì
        let localItems = this.getCurrentLocalItems();
        if (this.isLoggedIn) {
            localItems = localItems.map(i => i.cartItemId == cartItemId ? { ...i, quantity } : i);
            this.saveUserCartToLocal(localItems);
        } else {
            localItems = localItems.map(i => i.variantId == variantId ? { ...i, quantity } : i);
            GuestCart.save(localItems);
        }

        // Tự động tính toán lại Cart Summary ở Client để mượt mà lập tức
        this.recalculateClientSummary(localItems);

        // Gửi Ajax cập nhật lên database (nếu đã đăng nhập)
        if (this.isLoggedIn) {
            try {
                const response = await fetch(`${this.contextPath}/cart?action=update&cartItemId=${cartItemId}&quantity=${quantity}`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-Token': window.csrfToken || ''
                    }
                });
                const data = await this.safeParseJSON(response);
                if (!data.success) {
                    const errorCode = data.errorCode || data.meta?.errorCode;
                    if (errorCode === 'out_of_stock') {
                        this.showToast(data.error || 'Sản phẩm đã thay đổi tồn kho.', 'error');
                        // Khôi phục lại số lượng chuẩn từ server
                        this.loadAndSyncFromServer();
                    } else if (this.handleStaleCartItemError(data)) {
                        // Item đã bị đổi ID hoặc đồng bộ lại từ server, refresh thầm lặng thay vì bắn toast lỗi.
                    } else {
                        this.showToast(data.error || 'Có lỗi xảy ra', 'error');
                    }
                } else {
                    this.loadAndSyncFromServer();
                }
            } catch (err) {
                console.warn('[CartPage] Lỗi mạng khi cập nhật số lượng:', err);
            }
        } else {
            this.renderCart({ items: localItems });
        }
    },

    /**
     * Xử lý xóa sản phẩm khỏi giỏ hàng
     */
    async handleRemoveItem(row, cartItemId, variantId) {
        // Thực hiện micro-animation xóa ở client: mờ dần và co lại trước khi biến mất
        row.classList.add('removing-item');
        
        setTimeout(async () => {
            row.remove();
            
            // Cập nhật Local Storage
            let localItems = this.getCurrentLocalItems();
            if (this.isLoggedIn) {
                localItems = localItems.filter(i => i.cartItemId != cartItemId);
                this.saveUserCartToLocal(localItems);
            } else {
                localItems = localItems.filter(i => i.variantId != variantId);
                GuestCart.save(localItems);
            }

            // Tính lại tổng
            if (localItems.length === 0) {
                this.renderEmptyState();
            } else {
                this.recalculateClientSummary(localItems);
            }

            this.showToast('Đã xóa sản phẩm khỏi giỏ hàng.', 'success');

            // Gửi Ajax xóa lên DB (nếu đã đăng nhập)
            if (this.isLoggedIn) {
                try {
                    const response = await fetch(`${this.contextPath}/cart?action=remove&cartItemId=${cartItemId}`, {
                        method: 'POST',
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest',
                            'X-CSRF-Token': window.csrfToken || ''
                        }
                    });
                    const data = await this.safeParseJSON(response);
                    if (!data.success) {
                        this.showToast(data.error || 'Lỗi khi xóa sản phẩm trên server.', 'error');
                        this.loadAndSyncFromServer();
                    }
                } catch (err) {
                    console.warn('[CartPage] Lỗi kết nối xóa sản phẩm:', err);
                }
            }
        }, 300); // Khớp với thời gian CSS transition
    },

    /**
     * Tính toán lại Summary ở phía client tức thì để tạo cảm giác mượt mà
     */
    recalculateClientSummary(items) {
        let totalCents = 0;
        let totalGrams = 0;
        let checkedCount = 0;

        const checkedVariantIds = this.getCheckedVariantIdsFromDom();
        const checkedVariantSet = new Set(checkedVariantIds);
        this.saveSelectedVariantIds(checkedVariantIds);
        
        items.forEach(item => {
            const itemVariantId = parseInt(item.variantId, 10);
            const isChecked = Number.isInteger(itemVariantId) && checkedVariantSet.has(itemVariantId);
            if (isChecked) {
                const price = parseFloat(item.price) || 0;
                const packagingPriceAdd = parseFloat(item.packagingPriceAdd) || 0;
                const weight = parseFloat(item.weightKg) || 1.0;
                
                totalCents += Math.round(price + packagingPriceAdd) * item.quantity;
                totalGrams += Math.round(weight * 1000) * item.quantity;
                checkedCount++;
            }
        });

        const subtotal = totalCents;
        const totalWeightKg = totalGrams / 1000;

        const txtSubtotal = document.getElementById('summary-subtotal');
        const txtWeight = document.getElementById('summary-weight');
        const txtTotal = document.getElementById('summary-total');
        const txtCount = document.getElementById('summary-count');
        const txtCountTop = document.getElementById('summary-count-top');

        const spanCheckedCount = document.getElementById('checked-count');
        const spanTotalCount = document.getElementById('total-count');

        if (txtSubtotal) txtSubtotal.textContent = CurrencyFmt.format(subtotal);
        if (txtWeight) txtWeight.textContent = `${totalWeightKg.toFixed(3)} kg`;
        if (txtTotal) txtTotal.textContent = CurrencyFmt.format(subtotal);
        if (txtCount) txtCount.textContent = `${checkedCount} mặt hàng`;
        if (txtCountTop) txtCountTop.textContent = `${items.length} mặt hàng`;

        if (spanCheckedCount) spanCheckedCount.textContent = checkedCount;
        if (spanTotalCount) spanTotalCount.textContent = items.length;

        this.updateCheckoutButtonState(items);
    },

    /**
     * Render toàn bộ giỏ hàng lên HTML
     */
    async populateCartVariantDropdowns() {
        const dropdowns = document.querySelectorAll('.cart-variant-select');
        if (dropdowns.length === 0) return;

        const productIds = new Set();
        dropdowns.forEach(select => {
            const pId = select.getAttribute('data-product-id');
            if (pId && pId !== 'undefined' && pId !== 'null' && pId !== '') {
                productIds.add(pId);
            }
        });

        let hasUpdatedLocalCart = false;
        await Promise.all(Array.from(productIds).map(async (productId) => {
            if (this.productVariantsCache[productId]) return;

            try {
                const response = await fetch(`${this.contextPath}/products/detail?id=${productId}&format=json`);
                const data = await this.safeParseJSON(response);
                if (data.success) {
                    const payload = data.data;
                    if (payload && payload.variants) {
                        this.productVariantsCache[productId] = payload.variants;
                        const sId = payload.product?.shopId || null;
                        const sName = payload.product?.shopName || 'Cửa hàng';
                        this.productVariantsCache[productId].shopId = sId;
                        this.productVariantsCache[productId].shopName = sName;

                        // Check and update local items missing shop info
                        let localItems = this.getCurrentLocalItems();
                        let updated = false;
                        localItems.forEach(item => {
                            if (item.productId == productId && (!item.shopId || !item.shopName)) {
                                item.shopId = sId;
                                item.shopName = sName;
                                updated = true;
                            }
                        });

                        if (updated) {
                            if (this.isLoggedIn) {
                                this.saveUserCartToLocal(localItems);
                            } else {
                                GuestCart.save(localItems);
                            }
                            hasUpdatedLocalCart = true;
                        }
                    }
                }
            } catch (err) {
                console.warn(`[CartPage] Không thể lấy biến thể cho sản phẩm ${productId}:`, err);
            }
        }));

        if (hasUpdatedLocalCart) {
            this.renderCart({ items: this.getCurrentLocalItems() });
            return;
        }

        dropdowns.forEach(select => {
            const productId = select.getAttribute('data-product-id');
            if (!productId || productId === 'undefined' || productId === 'null') return;
            
            const currentVariantId = parseInt(select.getAttribute('data-current-variant-id'));
            const variants = this.productVariantsCache[productId];

            if (variants && variants.length > 0) {
                let optionsHtml = '';
                variants.forEach(v => {
                    const selectedAttr = v.variantId === currentVariantId ? 'selected' : '';
                    optionsHtml += `<option value="${v.variantId}" ${selectedAttr}>${v.variantLabel} - ${CurrencyFmt.format(v.price)}</option>`;
                });
                select.innerHTML = optionsHtml;
            }
        });
    },

    renderCart(summary) {
        const container = document.getElementById('cart-items-container');
        if (!container) return;

        if (!summary.items || summary.items.length === 0) {
            this.renderEmptyState();
            return;
        }

        // Group items strictly by shopId (if valid), otherwise group by shopName (no merging for different shopIds)
        const stockIssues = summary.items.reduce((acc, item) => {
            const stockState = buildCartStockState(item.stockQuantity, item.quantity);
            if (stockState.blocked) {
                acc.push({
                    name: item.productName || 'Sản phẩm',
                    variant: item.variantLabel || 'Mặc định',
                    stockState
                });
            }
            return acc;
        }, []);

        const groups = {};
        summary.items.forEach(item => {
            const shopName = (item.shopName || "Cửa hàng").trim();
            const shopId = parseInt(item.shopId, 10) || 0;
            const key = shopId > 0 ? shopId : shopName;
            if (!groups[key]) {
                groups[key] = {
                    shopId: shopId,
                    shopName: shopName,
                    items: []
                };
            }
            groups[key].items.push(item);
        });

        let html = '';
        if (stockIssues.length > 0) {
            const outOfStockCount = stockIssues.filter(issue => issue.stockState.outOfStock).length;
            const overLimitCount = stockIssues.length - outOfStockCount;
            const statusLine = [
                outOfStockCount > 0 ? `${outOfStockCount} sản phẩm đã hết hàng` : null,
                overLimitCount > 0 ? `${overLimitCount} sản phẩm vượt số lượng đặt` : null
            ].filter(Boolean).join(' và ');

            html += `
                <div class="mb-4 rounded-2xl border border-amber-200 bg-amber-50/90 text-amber-900 p-4 shadow-sm">
                    <div class="flex items-start gap-3">
                        <span class="material-symbols-outlined text-xl mt-0.5">warning</span>
                        <div class="text-sm leading-relaxed">
                            <p class="font-bold">Có ${stockIssues.length} sản phẩm chưa thể thanh toán ngay.</p>
                            <p class="mt-1">${statusLine || 'Vui lòng kiểm tra lại số lượng và phân loại còn hàng.'} Hãy đổi sang phân loại còn tồn kho hoặc giảm số lượng trước khi thanh toán.</p>
                        </div>
                    </div>
                </div>
            `;
        }
        Object.values(groups).forEach(group => {
            // Clickable shop link if shopId is valid (>0)
            const shopLinkHtml = group.shopId > 0 
                ? `<a href="${this.contextPath}/shop-view?id=${group.shopId}" class="font-semibold text-inverse-surface text-sm hover:underline hover:text-primary transition-all">${group.shopName}</a>`
                : `<span class="font-semibold text-inverse-surface text-sm">${group.shopName}</span>`;

            html += `
                <section class="bg-white/70 backdrop-blur-[12px] border border-white/40 shadow-[0_4px_12px_rgba(20,83,45,0.05)] rounded-2xl p-4 flex flex-col gap-4 mb-4 shop-group" data-shop-id="${group.shopId}">
                    <!-- Tiêu đề Shop -->
                    <div class="flex items-center gap-3 select-none pb-2 border-b border-surface-container/30">
                        <input type="checkbox" class="chk-shop rounded text-primary focus:ring-primary w-5 h-5 border-[#BBF7D0] bg-[#eaffea] cursor-pointer" data-shop-id="${group.shopId}" checked>
                        <span class="material-symbols-outlined text-primary text-xl">storefront</span>
                        ${shopLinkHtml}
                    </div>
                    
                    <div class="flex flex-col gap-md">
            `;

            group.items.forEach(item => {
                const stockState = buildCartStockState(item.stockQuantity, item.quantity);
                const rowClass = stockState.outOfStock
                    ? 'opacity-50 grayscale bg-rose-50/60 border-rose-200'
                    : stockState.overLimit
                        ? 'bg-amber-50/60 border-amber-200'
                        : '';
                let imgUrl = item.imagePath;
                if (!imgUrl) {
                    imgUrl = `${this.contextPath}/assets/images/placeholder.png`;
                } else if (!imgUrl.startsWith('http://') && !imgUrl.startsWith('https://')) {
                    if (!imgUrl.startsWith('/')) imgUrl = '/' + imgUrl;
                    imgUrl = this.contextPath + imgUrl;
                }

                const price = parseFloat(item.price) || 0;
                const packagingPriceAdd = parseFloat(item.packagingPriceAdd) || 0;
                const itemSubtotal = (price + packagingPriceAdd) * item.quantity;

                const productName = item.productName || (item.name ? item.name.split(' - ')[0] : 'Sản phẩm');
                const variantLabel = item.variantLabel || (item.name ? item.name.split(' - ')[1] : 'Mặc định');
                const weightKg = parseFloat(item.weightKg) || 1.0;
                const stockQuantity = stockState.stock;
                const stockBadgeClass = stockQuantity <= 0
                    ? 'border-red-200 bg-red-50 text-red-700'
                    : stockState.overLimit
                        ? 'border-amber-200 bg-amber-50 text-amber-700'
                        : stockQuantity <= 10
                        ? 'border-amber-200 bg-amber-50 text-amber-700'
                        : 'border-emerald-200 bg-emerald-50 text-emerald-700';
                const stockDisplayText = getCartStockDisplayText(stockState);

                const productLinkHtml = item.productId && item.productId !== 'undefined' && item.productId !== 'null' && item.productId !== ''
                    ? `<a href="${this.contextPath}/products/detail?id=${item.productId}" class="font-headline-md text-headline-md text-inverse-surface font-bold text-lg text-dark hover:underline hover:text-primary transition-all">${productName}</a>`
                    : `${productName}`;

                html += `
                    <article class="flex flex-row gap-md items-center cart-item-row ${rowClass}" data-item-id="${item.cartItemId || ''}" data-variant-id="${item.variantId}" data-shop-id="${group.shopId}" data-stock-quantity="${stockQuantity}" data-requested-quantity="${item.quantity}" data-stock-blocked="${stockState.blocked ? 'true' : 'false'}">
                        <!-- Checkbox từng mặt hàng -->
                        <div class="flex items-center shrink-0 pr-2">
                            <input type="checkbox" class="chk-item rounded text-primary focus:ring-primary w-5 h-5 border-[#BBF7D0] bg-[#eaffea] cursor-pointer ${stockState.blocked ? 'opacity-50 cursor-not-allowed' : ''}" data-variant-id="${item.variantId}" data-item-id="${item.cartItemId || ''}" data-shop-id="${group.shopId}" ${stockState.blocked ? 'disabled' : 'checked'}>
                        </div>
                        
                        <img alt="${productName}" class="w-24 h-24 sm:w-32 sm:h-32 rounded-lg object-cover flex-shrink-0 border border-white/30" src="${imgUrl}">
                        <div class="flex-grow flex flex-col gap-xs w-full">
                            <div class="flex justify-between items-start w-full">
                                <div>
                                    <h3 class="font-headline-md text-headline-md text-inverse-surface font-bold text-lg text-dark">${productLinkHtml}</h3>
                                    <p class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">
                                        Phân loại: 
                                        ${item.productId && item.productId !== 'undefined' && item.productId !== 'null' && item.productId !== '' ? `
                                        <span class="inline-block relative">
                                            <select class="cart-variant-select bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-xs border border-secondary/20 font-semibold cursor-pointer focus:ring-1 focus:ring-primary outline-none py-0 pr-8" data-item-id="${item.cartItemId || ''}" data-current-variant-id="${item.variantId}" data-product-id="${item.productId}">
                                                <option value="${item.variantId}" selected>${variantLabel} - ${CurrencyFmt.format(item.price)}</option>
                                            </select>
                                        </span>
                                        ` : `<span class="bg-[#f3f4f6] text-[#374151] px-2 py-0.5 rounded text-xs font-semibold border border-slate-200">${variantLabel}</span>`}
                                    </p>
                                    <p class="font-body-md text-body-md text-on-surface-variant text-xs mt-1 text-muted">Trọng lượng: <span class="fw-semibold text-dark">${weightKg.toFixed(3)} kg</span></p>
                                    <p class="font-body-md text-body-md text-on-surface-variant text-xs mt-1 text-muted">
                                        Tồn kho:
                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full border font-semibold ${stockBadgeClass}">
                                            ${stockDisplayText}
                                        </span>
                                    </p>
                                    ${item.packagingLabel ? `
                                    <p class="font-body-md text-body-md text-on-surface-variant text-[11px] mt-1 text-muted">
                                        Đóng gói: <span class="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded text-[11px] font-semibold border border-[#BBF7D0]/40">${item.packagingLabel} (+${CurrencyFmt.format(packagingPriceAdd)})</span>
                                    </p>
                                    ` : ''}
                                </div>
                                <div class="flex flex-col items-end">
                                    <span class="font-headline-md text-headline-md text-primary font-bold text-lg text-success">${CurrencyFmt.format(price + packagingPriceAdd)}</span>
                                    ${packagingPriceAdd > 0 ? `<span class="text-[10px] text-gray-400">Gồm đóng gói +${CurrencyFmt.format(packagingPriceAdd)}</span>` : ''}
                                </div>
                            </div>
                            
                            <div class="flex justify-between items-center mt-sm w-full">
                                <!-- Bộ Spinner số lượng bo tròn cao cấp -->
                                <div class="flex items-center bg-surface-container-low border border-surface-container-highest rounded-full p-1 quantity-spinner">
                                    <button aria-label="Decrease quantity" class="w-8 h-8 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors rounded-full hover:bg-surface-container-high btn-qty-minus ${stockQuantity <= 0 ? 'opacity-40 cursor-not-allowed' : ''}" ${stockQuantity <= 0 ? 'disabled' : ''}>
                                        <span class="material-symbols-outlined text-sm">remove</span>
                                    </button>
                                    <input type="number" class="font-label-md text-label-md w-12 text-center text-inverse-surface fw-bold bg-transparent border-0 focus:ring-0 focus:outline-none input-qty [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none ${stockQuantity <= 0 ? 'opacity-60 cursor-not-allowed' : ''}" value="${item.quantity}" data-stock="${stockQuantity}" min="1" max="${stockQuantity > 0 ? stockQuantity : 1}" ${stockQuantity <= 0 ? 'disabled' : ''}>
                                    <button aria-label="Increase quantity" class="w-8 h-8 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors rounded-full hover:bg-surface-container-high btn-qty-plus ${stockQuantity <= 0 ? 'opacity-40 cursor-not-allowed' : ''}" ${stockQuantity <= 0 ? 'disabled' : ''}>
                                        <span class="material-symbols-outlined text-sm">add</span>
                                    </button>
                                </div>
                                
                                <!-- Nút xóa sản phẩm với micro-animation -->
                                <button class="flex items-center gap-xs text-error hover:text-on-error-container transition-colors group btn-remove-item" data-id="${item.cartItemId || ''}" data-variant-id="${item.variantId}">
                                    <span class="material-symbols-outlined text-lg group-hover:scale-110 transition-transform">delete</span>
                                    <span class="font-label-md text-label-md hidden sm:inline ml-1 font-semibold text-sm">Xóa</span>
                                </button>
                            </div>
                        </div>
                    </article>
                `;
            });

            html += `
                    </div>
                </section>
            `;
        });

        container.innerHTML = html;
        this.restoreSelectedVariantState(summary.items);
        this.recalculateClientSummary(summary.items);
        this.populateCartVariantDropdowns();
        
        const checkoutCard = document.getElementById('cart-checkout-card');
        if (checkoutCard) checkoutCard.classList.remove('hidden');
        const headerActions = document.getElementById('cart-header-actions');
        if (headerActions) headerActions.classList.remove('hidden');
    },

    renderEmptyState() {
        const container = document.getElementById('cart-items-container');
        if (container) {
            container.innerHTML = `
                <div class="text-center py-12 premium-glass-card rounded-[1.5rem] p-lg">
                    <img src="${this.contextPath}/assets/images/empty_cart.png" alt="Giỏ hàng trống" class="mx-auto mb-6 opacity-70" style="max-width: 150px;">
                    <h3 class="font-headline-md text-headline-md text-inverse-surface font-bold text-dark text-xl">Giỏ hàng của bạn đang trống!</h3>
                    <p class="text-on-surface-variant mt-2 text-sm text-muted">Hãy quay lại cửa hàng để lựa chọn các loại hoa quả tươi ngon nhất nhé.</p>
                    <a href="${this.contextPath}/home" class="inline-flex items-center gap-2 bg-primary text-on-primary font-label-md text-label-md py-3 px-6 rounded-pill hover:bg-inverse-surface transition-all shadow-md mt-6">
                        <span class="material-symbols-outlined text-lg">shopping_basket</span>
                        <span>Tiếp tục mua sắm</span>
                    </a>
                </div>
            `;
        }

        const checkoutCard = document.getElementById('cart-checkout-card');
        if (checkoutCard) checkoutCard.classList.add('hidden');
        const headerActions = document.getElementById('cart-header-actions');
        if (headerActions) headerActions.classList.add('hidden');
        this.saveSelectedVariantIds([]);
    },

    /**
     * Xử lý nút thanh toán - Kiểm tra tồn kho DB thực tế chống xung đột và gửi danh sách variantId được chọn
     */
    async handleCheckout() {
        if (!this.isLoggedIn) {
            this.showToast('Bạn cần đăng nhập để tiến hành thanh toán!', 'warning');
            setTimeout(() => {
                window.location.href = `${this.contextPath}/auth/login?redirect=${encodeURIComponent(this.contextPath + '/cart')}`;
            }, 1500);
            return;
        }

        const blockedRows = Array.from(document.querySelectorAll('.chk-item:checked'))
            .map(chk => chk.closest('.cart-item-row'))
            .filter(row => row && row.getAttribute('data-stock-blocked') === 'true');
        if (blockedRows.length > 0) {
            this.showToast('Có sản phẩm đã hết hàng hoặc vượt tồn kho. Hãy đổi sang phân loại còn hàng hoặc giảm số lượng trước khi thanh toán.', 'warning');
            return;
        }

        const checkedVariantIds = this.getCheckedVariantIdsFromDom();
        if (checkedVariantIds.length === 0) {
            this.showToast('Vui lòng chọn ít nhất một sản phẩm để thanh toán!', 'warning');
            return;
        }

        const variantIds = checkedVariantIds.join(',');
        const btnCheckout = document.getElementById('btn-cart-checkout');
        const spinner = btnCheckout.querySelector('.checkout-spinner');
        
        btnCheckout.disabled = true;
        if (spinner) spinner.classList.remove('hidden');

        try {
            console.log('[CartPage] Checking real-time stock before checkout...');
            const response = await fetch(`${this.contextPath}/cart?action=checkStock`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': window.csrfToken || ''
                },
                body: new URLSearchParams({ variantIds }).toString()
            });
            const data = await this.safeParseJSON(response);
            
            if (data.success) {
                console.log('[CartPage] Stock double-check success. Proceeding to checkout.');
                window.location.href = `${this.contextPath}/checkout?variantIds=${encodeURIComponent(variantIds)}`;
            } else {
                const errors = Array.isArray(data.meta?.errors)
                    ? data.meta.errors
                    : Array.isArray(data.errors)
                        ? data.errors
                        : null;
                const errorCode = data.errorCode || data.meta?.errorCode;
                if (errors && errors.length > 0) {
                    let errorHtml = '<ul class="mb-0 text-start text-danger">';
                    errors.forEach(err => {
                        errorHtml += `<li>${escapeHtml(err)}</li>`;
                    });
                    errorHtml += '</ul>';
                    const isOutOfSeason = errorCode === 'out_of_season';
                    if (typeof Swal !== 'undefined') {
                        const intro = isOutOfSeason
                            ? 'Một số sản phẩm trong giỏ đã hết mùa nên không thể thanh toán ngay lúc này.'
                            : 'Một số sản phẩm trong giỏ hàng đã thay đổi tồn kho thực tế.';
                        Swal.fire({
                            icon: isOutOfSeason ? 'warning' : 'error',
                            title: isOutOfSeason ? 'Sản phẩm hết mùa' : 'Cảnh báo tồn kho',
                            html: `<div class="text-left">${intro}<div class="mt-3">${errorHtml}</div></div>`,
                            confirmButtonText: 'Đã hiểu',
                            confirmButtonColor: '#14532D',
                            background: '#ffffff',
                            customClass: {
                                popup: 'premium-swal-popup',
                                title: 'premium-swal-title',
                                confirmButton: 'premium-swal-button'
                            }
                        });
                    } else {
                        this.showStockErrorModal(errorHtml);
                    }
                    this.loadAndSyncFromServer();
                } else {
                    if (blockedRows.length > 0) {
                        this.showToast('Có sản phẩm trong giỏ đã hết hàng hoặc vượt tồn kho. Hãy đổi phân loại còn hàng hoặc giảm số lượng.', 'warning');
                    } else {
                        this.showToast(data.error || 'Không thể kiểm tra tồn kho lúc này.', 'error');
                    }
                }
            }
        } catch (err) {
            console.error('[CartPage] Lỗi mạng khi kiểm tra tồn kho:', err);
            this.showToast('Lỗi kết nối mạng. Không thể kiểm tra tồn kho lúc này.', 'error');
        } finally {
            btnCheckout.disabled = false;
            if (spinner) spinner.classList.add('hidden');
        }
    },

    /**
     * Hiển thị popup modal thông báo lỗi tồn kho chi tiết
     */
    showStockErrorModal(errorContent) {
        // Tạo modal dynamically nếu chưa có
        let modalEl = document.getElementById('stock-error-modal');
        if (!modalEl) {
            const html = `
                <div id="stock-error-modal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm hidden opacity-0 transition-opacity duration-300">
                    <div class="bg-white premium-glass-card rounded-[1.5rem] max-w-md w-full overflow-hidden shadow-2xl scale-95 transition-transform duration-300 p-6 relative border border-slate-100">
                        <button type="button" class="absolute top-4 right-4 text-slate-400 hover:text-slate-700 text-2xl font-bold focus:outline-none" onclick="CartPage.hideStockErrorModal()">&times;</button>
                        <div class="flex items-center gap-3 text-red-500 mb-4">
                            <span class="flex items-center justify-center w-10 h-10 rounded-full bg-rose-100 text-rose-600"><i class="fa-solid fa-triangle-exclamation text-lg"></i></span>
                            <h3 class="font-headline-sm text-headline-sm font-bold text-lg text-rose-700">Cảnh báo Tồn kho</h3>
                        </div>
                        <div class="py-2 text-slate-800 text-sm">
                            <p class="font-semibold mb-3">Rất tiếc! Một số sản phẩm trong giỏ hàng đã thay đổi tồn kho thực tế do có khách hàng khác vừa đặt mua:</p>
                            <div id="stock-error-content" class="bg-rose-50 text-rose-700 p-4 rounded-xl text-xs mb-3 font-mono leading-relaxed border border-rose-100"></div>
                            <p class="mb-0 text-xs text-slate-500">Hệ thống đã tự động cập nhật giỏ hàng về số lượng tối đa có thể mua. Vui lòng kiểm tra lại giỏ hàng trước khi nhấn Thanh toán.</p>
                        </div>
                        <div class="flex justify-end gap-3 mt-6">
                            <button type="button" class="bg-primary text-on-primary hover:bg-opacity-90 font-semibold px-6 py-2.5 rounded-full text-sm shadow-md transition-all duration-200" onclick="CartPage.hideStockErrorModal()">Đồng ý</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.insertAdjacentHTML('beforeend', html);
            modalEl = document.getElementById('stock-error-modal');
        }

        document.getElementById('stock-error-content').innerHTML = errorContent;
        modalEl.classList.remove('hidden');
        setTimeout(() => {
            modalEl.classList.add('opacity-100');
            modalEl.querySelector('.premium-glass-card')?.classList.remove('scale-95');
            modalEl.querySelector('.premium-glass-card')?.classList.add('scale-100');
        }, 10);
    },

    hideStockErrorModal() {
        const modalEl = document.getElementById('stock-error-modal');
        if (modalEl) {
            modalEl.classList.remove('opacity-100');
            modalEl.querySelector('.premium-glass-card')?.classList.remove('scale-100');
            modalEl.querySelector('.premium-glass-card')?.classList.add('scale-95');
            setTimeout(() => {
                modalEl.classList.add('hidden');
            }, 300);
        }
    },

    /**
     * Hiển thị Toast thông báo nhanh sử dụng Tailwind CSS thuần không phụ thuộc Bootstrap
     */
    showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'fixed bottom-5 right-5 z-[9999] flex flex-col gap-3 max-w-sm w-full pointer-events-none';
            document.body.appendChild(container);
        }

        // Tạo Toast Element
        const toastEl = document.createElement('div');
        toastEl.className = 'flex items-center gap-3 p-4 bg-white/95 backdrop-blur-md rounded-2xl shadow-2xl border border-slate-100 transform translate-x-full opacity-0 transition-all duration-300 pointer-events-auto';
        
        let iconHtml = '';
        let title = '';
        if (type === 'success') {
            iconHtml = '<span class="flex items-center justify-center w-8 h-8 rounded-full bg-emerald-100 text-emerald-600"><i class="fa-solid fa-circle-check"></i></span>';
            title = 'Thành công';
        } else if (type === 'error') {
            iconHtml = '<span class="flex items-center justify-center w-8 h-8 rounded-full bg-rose-100 text-rose-600"><i class="fa-solid fa-circle-xmark"></i></span>';
            title = 'Lỗi';
        } else if (type === 'warning') {
            iconHtml = '<span class="flex items-center justify-center w-8 h-8 rounded-full bg-amber-100 text-amber-600"><i class="fa-solid fa-triangle-exclamation"></i></span>';
            title = 'Cảnh báo';
        } else {
            iconHtml = '<span class="flex items-center justify-center w-8 h-8 rounded-full bg-sky-100 text-sky-600"><i class="fa-solid fa-circle-info"></i></span>';
            title = 'Thông báo';
        }

        toastEl.innerHTML = `
            ${iconHtml}
            <div class="flex-1 min-w-0">
                <strong class="block text-sm font-bold text-slate-800">${title}</strong>
                <span class="block text-xs text-slate-600 mt-0.5 leading-relaxed">${message}</span>
            </div>
            <button type="button" class="text-slate-400 hover:text-slate-600 text-lg ml-2 focus:outline-none" onclick="this.parentElement.remove()">&times;</button>
        `;

        container.appendChild(toastEl);

        // Animation show
        setTimeout(() => {
            toastEl.classList.remove('translate-x-full', 'opacity-0');
            toastEl.classList.add('translate-x-0', 'opacity-100');
        }, 10);

        // Auto remove
        setTimeout(() => {
            toastEl.classList.remove('translate-x-0', 'opacity-100');
            toastEl.classList.add('translate-x-full', 'opacity-0');
            setTimeout(() => {
                toastEl.remove();
            }, 300);
        }, 3500);
    }
};
