/**
 * cart.js — JavaScript quản lý trang Giỏ hàng (Cart)
 * Hỗ trợ: Lưu Local Storage, Ajax mượt mà, Beacon API khi tắt tab, Check Stock khi Checkout.
 */

const CartPage = {
    isLoggedIn: false,
    contextPath: '',
    userCartKey: 'userCart', // Key lưu giỏ hàng của user đã đăng nhập ở Local Storage
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
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: `guestCart=${encodeURIComponent(JSON.stringify(guestItems))}`
            });
            const data = await this.safeParseJSON(response);
            if (data.success) {
                // Xóa giỏ hàng guest vì đã gộp thành công
                GuestCart.clear();
                // Lưu giỏ hàng user đã gộp vào Local Storage
                if (data.cartSummary && data.cartSummary.items) {
                    this.saveUserCartToLocal(data.cartSummary.items);
                    this.renderCart(data.cartSummary);
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
            stockQuantity: item.stockQuantity
        }));
        localStorage.setItem(this.userCartKey, JSON.stringify(mappedItems));
        
        // Cập nhật badge navbar
        const totalQty = mappedItems.reduce((sum, i) => sum + i.quantity, 0);
        const badge = document.getElementById('cart-badge');
        if (badge) badge.textContent = totalQty;
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
            if (data.success && data.cartSummary) {
                // Lưu bản chuẩn vào Local Storage
                this.saveUserCartToLocal(data.cartSummary.items);
                // Render lại UI với dữ liệu chuẩn xác nhất từ DB (nếu có biến động giá/tồn kho)
                this.renderCart(data.cartSummary);
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
                const maxStock = parseInt(input.getAttribute('data-stock')) || 999;
                let val = parseInt(input.value) || 1;
                
                if (val < maxStock) {
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
                const checkboxes = document.querySelectorAll('.chk-item');
                checkboxes.forEach(chk => {
                    chk.checked = chkSelectAll.checked;
                });
                this.recalculateClientSummary(this.getCurrentLocalItems());
            });
        }

        // Bắt sự kiện check từng dòng
        cartContainer.addEventListener('change', (e) => {
            const target = e.target;
            if (target.classList.contains('chk-item')) {
                const checkboxes = Array.from(document.querySelectorAll('.chk-item'));
                const allChecked = checkboxes.every(chk => chk.checked);
                if (chkSelectAll) chkSelectAll.checked = allChecked;
                this.recalculateClientSummary(this.getCurrentLocalItems());
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
                    this.showToast('Không tìm thấy thông tin biến thể mới.', 'error');
                    target.value = oldVariantId;
                    return;
                }

                if (newVariant.stockQuantity <= 0) {
                    this.showToast('Rất tiếc! Biến thể này hiện đã hết hàng.', 'warning');
                    target.value = oldVariantId;
                    return;
                }

                let localItems = this.getCurrentLocalItems();
                const row = target.closest('.cart-item-row');
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
                            headers: { 'X-Requested-With': 'XMLHttpRequest' }
                        });
                        const data = await this.safeParseJSON(response);
                        if (data.success) {
                            this.showToast('Đã đổi biến thể thành công!', 'success');
                            this.loadAndSyncFromServer();
                        } else {
                            this.showToast(data.error || 'Lỗi đổi biến thể.', 'error');
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
                    GuestCart.save(localItems);
                    this.showToast('Đã đổi biến thể thành công!', 'success');
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
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                const data = await this.safeParseJSON(response);
                if (!data.success) {
                    if (data.errorCode === 'out_of_stock') {
                        this.showToast(data.error, 'error');
                        // Khôi phục lại số lượng chuẩn từ server
                        this.loadAndSyncFromServer();
                    } else {
                        this.showToast(data.error || 'Có lỗi xảy ra', 'error');
                    }
                }
            } catch (err) {
                console.warn('[CartPage] Lỗi mạng khi cập nhật số lượng:', err);
            }
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
                        headers: { 'X-Requested-With': 'XMLHttpRequest' }
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

        const checkedVariantIds = Array.from(document.querySelectorAll('.chk-item:checked')).map(chk => parseInt(chk.getAttribute('data-variant-id')));
        
        items.forEach(item => {
            const isChecked = checkedVariantIds.includes(item.variantId);
            if (isChecked) {
                const price = parseFloat(item.price) || 0;
                const weight = parseFloat(item.weightKg) || 1.0;
                
                totalCents += Math.round(price) * item.quantity;
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

        const btnCheckout = document.getElementById('btn-cart-checkout');
        if (btnCheckout) {
            if (checkedCount === 0) {
                btnCheckout.disabled = true;
                btnCheckout.classList.add('opacity-50', 'cursor-not-allowed');
            } else {
                btnCheckout.disabled = false;
                btnCheckout.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }
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

        await Promise.all(Array.from(productIds).map(async (productId) => {
            if (this.productVariantsCache[productId]) return;

            try {
                const response = await fetch(`${this.contextPath}/products/detail?id=${productId}&format=json`);
                const data = await this.safeParseJSON(response);
                if (data.success && data.variants) {
                    this.productVariantsCache[productId] = data.variants;
                }
            } catch (err) {
                console.warn(`[CartPage] Không thể lấy biến thể cho sản phẩm ${productId}:`, err);
            }
        }));

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

        let html = '';
        summary.items.forEach(item => {
            let imgUrl = item.imagePath;
            if (!imgUrl) {
                imgUrl = `${this.contextPath}/assets/images/placeholder.png`;
            } else if (!imgUrl.startsWith('http://') && !imgUrl.startsWith('https://')) {
                if (!imgUrl.startsWith('/')) imgUrl = '/' + imgUrl;
                imgUrl = this.contextPath + imgUrl;
            }

            const itemSubtotal = parseFloat(item.price) * item.quantity;

            // Fallback thông minh cho dữ liệu Local Storage của Guest để tránh lỗi undefined / NaN
            const productName = item.productName || (item.name ? item.name.split(' - ')[0] : 'Sản phẩm');
            const variantLabel = item.variantLabel || (item.name ? item.name.split(' - ')[1] : 'Mặc định');
            const weightKg = parseFloat(item.weightKg) || 1.0;

            html += `
                <article class="bg-white/70 backdrop-blur-[12px] border border-white/40 shadow-[0_4px_12px_rgba(20,83,45,0.05)] rounded-xl p-md flex flex-row gap-md items-center cart-item-row" data-item-id="${item.cartItemId || ''}" data-variant-id="${item.variantId}">
                    <!-- Checkbox từng mặt hàng -->
                    <div class="flex items-center shrink-0 pr-2">
                        <input type="checkbox" class="chk-item rounded text-primary focus:ring-primary w-5 h-5 border-[#BBF7D0] bg-[#eaffea] cursor-pointer" data-variant-id="${item.variantId}" data-item-id="${item.cartItemId || ''}" checked>
                    </div>
                    
                    <img alt="${productName}" class="w-24 h-24 sm:w-32 sm:h-32 rounded-lg object-cover flex-shrink-0 border border-white/30" src="${imgUrl}">
                    <div class="flex-grow flex flex-col gap-xs w-full">
                        <div class="flex justify-between items-start w-full">
                            <div>
                                <h3 class="font-headline-md text-headline-md text-inverse-surface font-bold text-lg text-dark">${productName}</h3>
                                <p class="font-body-md text-body-md text-on-surface-variant text-sm mt-1">
                                    Biến thể: 
                                    ${item.productId && item.productId !== 'undefined' && item.productId !== 'null' && item.productId !== '' ? `
                                    <span class="inline-block relative">
                                        <select class="cart-variant-select bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-xs border border-secondary/20 font-semibold cursor-pointer focus:ring-1 focus:ring-primary outline-none py-0 pr-8" data-item-id="${item.cartItemId || ''}" data-current-variant-id="${item.variantId}" data-product-id="${item.productId}">
                                            <option value="${item.variantId}" selected>${variantLabel} - ${CurrencyFmt.format(item.price)}</option>
                                        </select>
                                    </span>
                                    ` : `<span class="bg-[#f3f4f6] text-[#374151] px-2 py-0.5 rounded text-xs font-semibold border border-slate-200">${variantLabel}</span>`}
                                </p>
                                <p class="font-body-md text-body-md text-on-surface-variant text-xs mt-1 text-muted">Trọng lượng: <span class="fw-semibold text-dark">${weightKg.toFixed(3)} kg</span></p>
                            </div>
                            <span class="font-headline-md text-headline-md text-primary font-bold text-lg text-success">${CurrencyFmt.format(item.price)}</span>
                        </div>
                        
                        <div class="flex justify-between items-center mt-sm w-full">
                            <!-- Bộ Spinner số lượng bo tròn cao cấp -->
                            <div class="flex items-center bg-surface-container-low border border-surface-container-highest rounded-full p-1 quantity-spinner">
                                <button aria-label="Decrease quantity" class="w-8 h-8 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors rounded-full hover:bg-surface-container-high btn-qty-minus">
                                    <span class="material-symbols-outlined text-sm">remove</span>
                                </button>
                                <span class="font-label-md text-label-md w-8 text-center text-inverse-surface fw-bold input-qty-value">${item.quantity}</span>
                                <input type="number" class="input-qty hidden" value="${item.quantity}" data-stock="${item.stockQuantity || 99}" min="1">
                                <button aria-label="Increase quantity" class="w-8 h-8 flex items-center justify-center text-on-surface-variant hover:text-primary transition-colors rounded-full hover:bg-surface-container-high btn-qty-plus">
                                    <span class="material-symbols-outlined text-sm">add</span>
                                </button>
                            </div>
                            
                            <!-- Nút xóa sản phẩm với micro-animation -->
                            <button class="flex items-center gap-xs text-error hover:text-on-error-container transition-colors group btn-remove-item" data-id="${item.cartItemId || ''}" data-variant-id="${item.variantId}">
                                <span class="material-symbols-outlined text-lg group-hover:scale-110 transition-transform">delete</span>
                                <span class="font-label-md text-label-md hidden sm:inline ml-1 font-semibold text-sm">Remove</span>
                            </button>
                        </div>
                    </div>
                </article>
            `;
        });

        container.innerHTML = html;
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

        const checkedItems = Array.from(document.querySelectorAll('.chk-item:checked'));
        if (checkedItems.length === 0) {
            this.showToast('Vui lòng chọn ít nhất một sản phẩm để thanh toán!', 'warning');
            return;
        }

        const variantIds = checkedItems.map(chk => chk.getAttribute('data-variant-id')).join(',');
        const btnCheckout = document.getElementById('btn-cart-checkout');
        const spinner = btnCheckout.querySelector('.checkout-spinner');
        
        btnCheckout.disabled = true;
        if (spinner) spinner.classList.remove('hidden');

        try {
            console.log('[CartPage] Checking real-time stock before checkout...');
            const response = await fetch(`${this.contextPath}/cart?action=checkStock`, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': window.csrfToken || ''
                }
            });
            const data = await this.safeParseJSON(response);
            
            if (data.success) {
                console.log('[CartPage] Stock double-check success. Proceeding to checkout.');
                window.location.href = `${this.contextPath}/checkout?variantIds=${variantIds}`;
            } else {
                let errorHtml = '<ul class="mb-0 text-start text-danger">';
                data.errors.forEach(err => {
                    errorHtml += `<li>${err}</li>`;
                });
                errorHtml += '</ul>';

                this.showStockErrorModal(errorHtml);
                this.loadAndSyncFromServer();
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
