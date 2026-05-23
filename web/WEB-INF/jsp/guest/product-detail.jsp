<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>
<%@ taglib prefix="ft"  uri="/WEB-INF/tld/fruitmkt.tld" %>

<%-- 
    product-detail.jsp — Giao diện xem chi tiết sản phẩm cao cấp (Premium Organic Glassmorphism).
    Cấu trúc trang:
    [1] 2-column grid: Gallery | Thông tin sản phẩm + Giá + Biến thể + Nút mua
    [2] Panel: Thông tin cửa hàng (tên, mô tả, rating) + Xem thêm từ shop + Vận chuyển + Voucher
    [3] Panel: Thông số kỹ thuật sản phẩm
    [4] Panel: Đánh giá (rating summary, filter tabs, phân trang)
    [5] Section: Sản phẩm tương tự (bên dưới review)
--%>

<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="pageTitle" value="${product.name}"/>
</jsp:include>

<!-- Custom Premium CSS for Product Detail Page -->
<style>
    /* ============================================================
       PREMIUM PAGE DESIGN TOKENS
    ============================================================ */
    :root {
        --color-accent: #84CC16;
        --color-glass-bg: rgba(255, 255, 255, 0.72);
        --color-glass-border: rgba(226, 236, 231, 0.6);
        --transition-premium: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        --shop-green: rgba(77, 102, 28, 0.06);
        --shop-green-border: rgba(77, 102, 28, 0.12);
    }

    /* ============================================================
       PAGE CONTAINER & LAYOUT
    ============================================================ */
    .detail-container {
        padding-top: var(--space-8);
        padding-bottom: var(--space-12);
        background:
            radial-gradient(ellipse at 0% 0%, rgba(187,247,208,0.35) 0%, transparent 55%),
            radial-gradient(ellipse at 100% 10%, rgba(134,239,172,0.2) 0%, transparent 50%),
            linear-gradient(160deg, #f0fdf4 0%, #ffffff 60%, #f7fee7 100%);
        min-height: 100vh;
    }

    /* Breadcrumbs */
    .breadcrumbs {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        margin-bottom: var(--space-6);
    }
    .breadcrumbs a {
        color: var(--color-text-muted);
        transition: var(--transition-premium);
    }
    .breadcrumbs a:hover {
        color: var(--color-primary);
        text-decoration: none;
    }
    .breadcrumbs .separator {
        color: var(--color-text-muted);
        opacity: 0.5;
    }

    /* Two-Column Product Grid */
    .detail-grid {
        display: grid;
        grid-template-columns: 1.1fr 1fr;
        gap: var(--space-10);
        margin-bottom: var(--space-8);
    }
    @media (max-width: 992px) {
        .detail-grid { grid-template-columns: 1fr; gap: var(--space-8); }
    }

    /* Glassmorphic Panel */
    .premium-panel {
        background: rgba(255,255,255,0.88);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1.5px solid rgba(134,239,172,0.3);
        border-radius: var(--radius-xl);
        padding: var(--space-6);
        box-shadow:
            0 8px 32px rgba(34,197,94,0.08),
            0 1px 4px rgba(20,83,45,0.06),
            inset 0 1px 0 rgba(255,255,255,0.9);
        transition: var(--transition-premium);
    }
    .premium-panel:hover {
        box-shadow:
            0 16px 48px rgba(34,197,94,0.12),
            0 2px 8px rgba(20,83,45,0.08),
            inset 0 1px 0 rgba(255,255,255,1);
        border-color: rgba(134,239,172,0.5);
    }

    /* GALLERY */
    .gallery-wrapper {
        display: flex;
        flex-direction: column;
        gap: var(--space-4);
    }
    .main-image-box {
        position: relative;
        width: 100%;
        aspect-ratio: 4/3;
        border-radius: var(--radius-xl);
        overflow: hidden;
        background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
        border: 2px solid rgba(134,239,172,0.35);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 8px 32px rgba(34,197,94,0.1), inset 0 1px 0 rgba(255,255,255,0.8);
    }
    .main-image-box img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.6s cubic-bezier(0.4,0,0.2,1);
    }
    .main-image-box:hover img { transform: scale(1.05); }
    .thumbnail-list {
        display: flex;
        gap: var(--space-3);
        overflow-x: auto;
        padding-bottom: var(--space-2);
    }
    .thumbnail-item {
        width: 80px;
        height: 60px;
        border-radius: var(--radius-md);
        overflow: hidden;
        border: 2px solid transparent;
        cursor: pointer;
        opacity: 0.65;
        transition: var(--transition-premium);
        background: #fff;
        flex-shrink: 0;
        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    }
    .thumbnail-item img { width: 100%; height: 100%; object-fit: cover; }
    .thumbnail-item.active, .thumbnail-item:hover {
        border-color: #22c55e;
        opacity: 1;
        transform: translateY(-3px);
        box-shadow: 0 6px 16px rgba(34,197,94,0.2);
    }

    /* ============================================================
       BADGES & TAGS
    ============================================================ */
    .badge-stock {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        padding: 5px var(--space-3);
        border-radius: var(--radius-full);
        font-size: var(--font-size-xs);
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }
    .badge-instock {
        background: linear-gradient(120deg, #dcfce7, #bbf7d0);
        color: #14532d;
        border: 1px solid rgba(34,197,94,0.3);
        box-shadow: 0 2px 8px rgba(34,197,94,0.15);
    }
    .badge-outstock {
        background: linear-gradient(120deg, #fee2e2, #fecaca);
        color: #7f1d1d;
        border: 1px solid rgba(239,68,68,0.3);
    }
    .badge-rating-top {
        background: linear-gradient(120deg, #fef9c3, #fde68a);
        color: #713f12;
        border: 1px solid rgba(245,158,11,0.3);
        font-weight: 700;
        font-size: var(--font-size-xs);
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 5px var(--space-3);
        border-radius: var(--radius-full);
        box-shadow: 0 2px 8px rgba(245,158,11,0.12);
    }

    /* ============================================================
       VARIANT SELECTOR
    ============================================================ */
    .variant-section { margin: var(--space-5) 0; }
    .section-sub-title {
        font-size: var(--font-size-xs);
        font-weight: 800;
        color: #14532d;
        margin-bottom: var(--space-3);
        text-transform: uppercase;
        letter-spacing: 1px;
        display: flex;
        align-items: center;
        gap: 6px;
    }
    .section-sub-title::before {
        content: '';
        display: inline-block;
        width: 3px; height: 14px;
        background: linear-gradient(180deg, #22c55e, #16a34a);
        border-radius: 2px;
    }
    .variant-chips { display: flex; flex-wrap: wrap; gap: var(--space-2); }
    .variant-chip-input { display: none; }
    .variant-chip-label {
        display: inline-flex;
        align-items: center;
        padding: var(--space-2) var(--space-5);
        border: 2px solid #e2e8f0;
        border-radius: var(--radius-full);
        font-weight: 600;
        font-size: var(--font-size-sm);
        color: #475569;
        cursor: pointer;
        transition: var(--transition-premium);
        background: #fff;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
    }
    .variant-chip-input:checked + .variant-chip-label {
        border-color: #16a34a;
        background: linear-gradient(120deg, #f0fdf4, #dcfce7);
        color: #14532d;
        box-shadow: 0 0 0 3px rgba(34,197,94,0.15), 0 2px 8px rgba(34,197,94,0.1);
        font-weight: 700;
    }
    .variant-chip-label:hover {
        border-color: #22c55e;
        color: #14532d;
        background: #f0fdf4;
        transform: translateY(-1px);
    }

    /* ============================================================
       CART ACTIONS
    ============================================================ */
    .cart-action-row {
        display: flex;
        align-items: center;
        gap: var(--space-4);
        margin-top: var(--space-6);
        margin-bottom: var(--space-2);
    }
    .qty-selector {
        display: flex;
        align-items: center;
        border: 2px solid #e2e8f0;
        border-radius: var(--radius-full);
        padding: 2px;
        background: #fff;
        box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        transition: var(--transition-premium);
    }
    .qty-selector:focus-within {
        border-color: #22c55e;
        box-shadow: 0 0 0 3px rgba(34,197,94,0.12);
    }
    .qty-btn {
        width: 36px; height: 36px;
        border-radius: var(--radius-full);
        border: none;
        background: none;
        color: #16a34a;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        font-size: 1rem;
        transition: var(--transition-premium);
        font-weight: 700;
    }
    .qty-btn:hover {
        background: linear-gradient(135deg, #dcfce7, #bbf7d0);
        color: #14532d;
        transform: scale(1.1);
    }
    .qty-input {
        width: 44px;
        text-align: center;
        border: none;
        font-weight: 800;
        font-size: var(--font-size-base);
        color: #14532d;
        outline: none;
        background: transparent;
    }
    .qty-input::-webkit-outer-spin-button,
    .qty-input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
    .btn-add-to-cart-large {
        flex: 1;
        padding: 14px var(--space-6);
        background: linear-gradient(135deg, #16a34a 0%, #22c55e 60%, #4ade80 100%);
        color: #fff;
        font-weight: 800;
        font-size: var(--font-size-base);
        border-radius: var(--radius-full);
        border: none;
        cursor: pointer;
        display: inline-flex; align-items: center; justify-content: center;
        gap: var(--space-2);
        box-shadow: 0 4px 20px rgba(34,197,94,0.35), 0 2px 8px rgba(20,83,45,0.2);
        transition: var(--transition-premium);
        letter-spacing: 0.2px;
        position: relative;
        overflow: hidden;
    }
    .btn-add-to-cart-large::before {
        content: '';
        position: absolute;
        top: 0; left: -100%;
        width: 100%; height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
        transition: left 0.5s ease;
    }
    .btn-add-to-cart-large:hover::before { left: 100%; }
    .btn-add-to-cart-large:hover {
        background: linear-gradient(135deg, #14532d 0%, #16a34a 60%, #22c55e 100%);
        transform: translateY(-2px);
        box-shadow: 0 8px 28px rgba(34,197,94,0.45), 0 4px 12px rgba(20,83,45,0.25);
    }
    .btn-add-to-cart-large:active { transform: translateY(0); }

    /* Product Description Box */
    .product-desc-box {
        background: linear-gradient(135deg, rgba(240,253,244,0.8) 0%, rgba(220,252,231,0.5) 100%);
        border: 1px solid rgba(34,197,94,0.2);
        border-left: 3px solid #22c55e;
        border-radius: 0 var(--radius-md) var(--radius-md) 0;
        padding: var(--space-4) var(--space-5);
        margin-bottom: var(--space-5);
        position: relative;
    }
    .product-desc-box::before {
        content: '\201C';
        position: absolute;
        top: -4px; left: 12px;
        font-size: 2.5rem;
        color: #22c55e;
        opacity: 0.3;
        line-height: 1;
        font-family: Georgia, serif;
    }
    .product-desc-text {
        font-size: var(--font-size-sm);
        color: #374151;
        line-height: 1.75;
        font-style: italic;
        padding-left: var(--space-3);
    }

    /* Price Area */
    .price-area {
        background: linear-gradient(120deg, rgba(240,253,244,0.95) 0%, rgba(220,252,231,0.7) 100%);
        border: 1.5px solid rgba(34,197,94,0.25);
        border-radius: var(--radius-lg);
        padding: var(--space-4) var(--space-5);
        margin-bottom: var(--space-4);
        display: flex;
        align-items: baseline;
        gap: var(--space-2);
        box-shadow: 0 2px 12px rgba(34,197,94,0.08), inset 0 1px 0 rgba(255,255,255,0.8);
    }
    .price-main {
        font-size: 2.2rem;
        font-weight: 900;
        background: linear-gradient(135deg, #14532d, #16a34a);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        line-height: 1;
        letter-spacing: -1px;
    }
    .price-unit {
        font-size: var(--font-size-sm);
        color: #6b7280;
        font-weight: 500;
    }

    /* Flash Sale Inline Badge */
    .flash-sale-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        background: linear-gradient(120deg, #fff1f2, #ffe4e6);
        border: 1.5px solid rgba(239,68,68,0.3);
        border-radius: var(--radius-full);
        padding: 5px var(--space-4);
        font-size: var(--font-size-xs);
        font-weight: 700;
        color: #991B1B;
        margin-bottom: var(--space-3);
        box-shadow: 0 2px 8px rgba(239,68,68,0.1);
        animation: pulse-red 2s infinite;
    }
    @keyframes pulse-red {
        0%, 100% { box-shadow: 0 2px 8px rgba(239,68,68,0.1); }
        50% { box-shadow: 0 2px 16px rgba(239,68,68,0.25); }
    }

    /* Stock indicator */
    .stock-indicator {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        font-size: var(--font-size-xs);
        color: #6b7280;
        margin-bottom: var(--space-5);
    }
    .stock-bar-bg {
        flex: 1;
        height: 5px;
        background: #e5e7eb;
        border-radius: var(--radius-full);
        overflow: hidden;
        max-width: 120px;
    }
    .stock-bar-fill {
        height: 100%;
        background: linear-gradient(90deg, #22c55e, #4ade80);
        border-radius: var(--radius-full);
        transition: width 0.8s ease;
    }

    /* ============================================================
       SHOP PROFILE CARD (below product info)
    ============================================================ */
    .shop-profile-panel {
        background: linear-gradient(160deg, rgba(255,255,255,0.96) 0%, rgba(240,253,244,0.92) 100%);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid rgba(74,187,74,0.22);
        border-radius: var(--radius-xl);
        overflow: hidden;
        margin-bottom: var(--space-6);
        box-shadow: 0 8px 32px rgba(34,197,94,0.10), 0 2px 8px rgba(20,83,45,0.06);
    }
    .shop-header-band {
        background: linear-gradient(120deg, #16a34a 0%, #22c55e 45%, #4ade80 80%, #86efac 100%);
        padding: var(--space-5) var(--space-6);
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--space-4);
    }
    .shop-header-left {
        display: flex;
        align-items: center;
        gap: var(--space-4);
    }
    .shop-avatar-lg {
        width: 56px; height: 56px;
        background: rgba(255,255,255,0.2);
        border-radius: var(--radius-lg);
        display: flex; align-items: center; justify-content: center;
        font-size: 1.6rem;
        color: #fff;
        border: 2px solid rgba(255,255,255,0.3);
        flex-shrink: 0;
    }
    .shop-name-hero {
        font-weight: 800;
        font-size: 1.15rem;
        color: #fff;
        margin-bottom: 2px;
        letter-spacing: -0.3px;
    }
    .shop-hero-sub {
        font-size: var(--font-size-xs);
        color: rgba(255,255,255,0.75);
        display: flex;
        align-items: center;
        gap: var(--space-2);
    }
    .shop-rating-stars-sm {
        display: inline-flex;
        align-items: center;
        gap: 2px;
        color: #FCD34D;
        font-size: var(--font-size-xs);
    }
    .btn-visit-shop-hero {
        padding: var(--space-2) var(--space-5);
        background: rgba(255,255,255,0.15);
        color: #fff;
        font-weight: 700;
        font-size: var(--font-size-sm);
        border-radius: var(--radius-full);
        border: 1.5px solid rgba(255,255,255,0.4);
        white-space: nowrap;
        transition: var(--transition-premium);
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: var(--space-2);
    }
    .btn-visit-shop-hero:hover {
        background: rgba(255,255,255,0.28);
        border-color: rgba(255,255,255,0.7);
        text-decoration: none;
    }
    .shop-body-section {
        padding: var(--space-4) var(--space-6);
        border-bottom: 1px dashed var(--color-glass-border);
    }
    .shop-body-section:last-child { border-bottom: none; }
    .shop-section-label {
        font-size: 10px;
        font-weight: 800;
        color: var(--color-primary-dark);
        text-transform: uppercase;
        letter-spacing: 1px;
        margin-bottom: var(--space-3);
        display: flex;
        align-items: center;
        gap: var(--space-2);
    }
    .shop-section-label i { color: var(--color-primary); }

    /* ============================================================
       SHIPPING INFO PILLS
    ============================================================ */
    .shipping-info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
        gap: var(--space-3);
    }
    .shipping-pill {
        background: linear-gradient(135deg, rgba(240,253,244,0.95) 0%, rgba(220,252,231,0.8) 100%);
        border: 1px solid rgba(34,197,94,0.2);
        border-radius: var(--radius-lg);
        padding: var(--space-3) var(--space-4);
        display: flex;
        align-items: flex-start;
        gap: var(--space-3);
        transition: var(--transition-premium);
    }
    .shipping-pill:hover {
        background: linear-gradient(135deg, rgba(220,252,231,0.98) 0%, rgba(187,247,208,0.7) 100%);
        border-color: rgba(34,197,94,0.4);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(34,197,94,0.12);
    }
    .shipping-pill-icon {
        width: 36px; height: 36px;
        border-radius: var(--radius-md);
        background: linear-gradient(135deg, #22c55e, #16a34a);
        display: flex; align-items: center; justify-content: center;
        color: #fff;
        font-size: 1rem;
        flex-shrink: 0;
        box-shadow: 0 2px 8px rgba(34,197,94,0.3);
    }
    .shipping-pill-title {
        font-weight: 700;
        font-size: var(--font-size-sm);
        color: #14532d;
        margin-bottom: 2px;
    }
    .shipping-pill-sub {
        font-size: var(--font-size-xs);
        color: #166534;
        opacity: 0.8;
        line-height: 1.4;
    }

    /* ============================================================
       VOUCHER / PROMOTION SECTION — Horizontal Slider
    ============================================================ */
    .voucher-slider-wrapper {
        position: relative;
    }
    .voucher-slider-nav {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: var(--space-2);
    }
    .voucher-nav-btn {
        width: 30px; height: 30px;
        border-radius: var(--radius-full);
        border: 1.5px solid #22c55e;
        background: #fff;
        color: #16a34a;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        font-size: 12px;
        transition: var(--transition-premium);
        flex-shrink: 0;
        box-shadow: 0 2px 8px rgba(34,197,94,0.12);
    }
    .voucher-nav-btn:hover { background: #22c55e; color: #fff; box-shadow: 0 4px 12px rgba(34,197,94,0.3); }
    .voucher-nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }
    .voucher-dots {
        display: flex; gap: 5px; align-items: center;
    }
    .voucher-dot {
        width: 6px; height: 6px;
        border-radius: 50%;
        background: #bbf7d0;
        transition: all 0.3s ease;
        cursor: pointer;
    }
    .voucher-dot.active { background: #16a34a; width: 18px; border-radius: 3px; }
    .voucher-track-container {
        overflow: hidden;
        border-radius: var(--radius-lg);
    }
    .voucher-track {
        display: flex;
        gap: var(--space-3);
        transition: transform 0.42s cubic-bezier(0.4, 0, 0.2, 1);
        will-change: transform;
    }
    .voucher-item {
        flex: 0 0 calc(50% - 6px);
        min-width: 220px;
        display: flex;
        align-items: stretch;
        border-radius: var(--radius-lg);
        overflow: hidden;
        border: 1.5px solid;
        transition: var(--transition-premium);
        position: relative;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }
    @media (max-width: 600px) {
        .voucher-item { flex: 0 0 calc(100% - 0px); }
    }
    .voucher-item:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
    .voucher-item.type-shop {
        border-color: #F59E0B;
        background: linear-gradient(120deg, #FFFBEB 0%, #FEF3C7 60%, #FDE68A22 100%);
    }
    .voucher-item.type-system {
        border-color: #22c55e;
        background: linear-gradient(120deg, #f0fdf4 0%, #dcfce7 60%, #bbf7d022 100%);
    }
    .voucher-item.type-product {
        border-color: #EF4444;
        background: linear-gradient(120deg, #FEF2F2 0%, #FEE2E2 60%, #FECACA22 100%);
    }
    .voucher-ribbon {
        width: 54px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: var(--space-3) var(--space-2);
        font-size: 9px;
        font-weight: 800;
        color: #fff;
        flex-shrink: 0;
        text-align: center;
        line-height: 1.2;
        position: relative;
        min-height: 64px;
        letter-spacing: 0.5px;
    }
    .voucher-ribbon::after {
        content: '';
        position: absolute;
        right: -9px;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 0;
        border-top: 9px solid transparent;
        border-bottom: 9px solid transparent;
        border-left: 9px solid;
    }
    .voucher-item.type-shop .voucher-ribbon { background: linear-gradient(180deg, #F59E0B, #D97706); }
    .voucher-item.type-shop .voucher-ribbon::after { border-left-color: #D97706; }
    .voucher-item.type-system .voucher-ribbon { background: linear-gradient(180deg, #22c55e, #16a34a); }
    .voucher-item.type-system .voucher-ribbon::after { border-left-color: #16a34a; }
    .voucher-item.type-product .voucher-ribbon { background: linear-gradient(180deg, #EF4444, #DC2626); }
    .voucher-item.type-product .voucher-ribbon::after { border-left-color: #DC2626; }
    .voucher-ribbon-icon { font-size: 1.2rem; margin-bottom: 3px; }
    .voucher-body {
        flex: 1;
        padding: var(--space-2) var(--space-3) var(--space-2) var(--space-5);
        display: flex;
        flex-direction: column;
        justify-content: center;
    }
    .voucher-code {
        font-weight: 800;
        font-size: 13px;
        letter-spacing: 0.5px;
        margin-bottom: 2px;
    }
    .voucher-item.type-shop .voucher-code { color: #92400E; }
    .voucher-item.type-system .voucher-code { color: #14532d; }
    .voucher-item.type-product .voucher-code { color: #991B1B; }
    .voucher-desc {
        font-size: 11px;
        color: var(--color-text-secondary);
        line-height: 1.4;
    }
    .voucher-expire {
        font-size: 9px;
        color: var(--color-text-muted);
        margin-top: 3px;
        font-style: italic;
    }
    .voucher-copy-btn {
        padding: 5px 10px;
        font-size: 10px;
        font-weight: 700;
        border-radius: var(--radius-sm);
        border: 1.5px solid;
        cursor: pointer;
        transition: var(--transition-premium);
        background: transparent;
        white-space: nowrap;
        margin: auto var(--space-3) auto 0;
        flex-shrink: 0;
        align-self: center;
    }
    .voucher-item.type-shop .voucher-copy-btn { border-color: #F59E0B; color: #92400E; }
    .voucher-item.type-shop .voucher-copy-btn:hover { background: #F59E0B; color: #fff; }
    .voucher-item.type-system .voucher-copy-btn { border-color: #22c55e; color: #14532d; }
    .voucher-item.type-system .voucher-copy-btn:hover { background: #22c55e; color: #fff; }
    .voucher-item.type-product .voucher-copy-btn { border-color: #EF4444; color: #991B1B; }
    .voucher-item.type-product .voucher-copy-btn:hover { background: #EF4444; color: #fff; }

    /* ============================================================
       SHOP MORE PRODUCTS SLIDER
    ============================================================ */
    .shop-products-slider {
        display: flex;
        gap: var(--space-3);
        overflow-x: auto;
        scroll-behavior: smooth;
        padding-bottom: var(--space-2);
        snap-type: x mandatory;
    }
    .shop-products-slider::-webkit-scrollbar { display: none; }
    .shop-product-mini {
        flex: 0 0 130px;
        snap-align: start;
        background: #fff;
        border-radius: var(--radius-lg);
        border: 1px solid var(--color-glass-border);
        overflow: hidden;
        transition: var(--transition-premium);
    }
    .shop-product-mini:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(20,83,45,0.1);
        border-color: var(--color-primary-light);
    }
    .shop-product-mini-img {
        width: 100%;
        height: 90px;
        object-fit: cover;
    }
    .shop-product-mini-info { padding: var(--space-2) var(--space-2) var(--space-3); }
    .shop-product-mini-name {
        font-size: 11px;
        font-weight: 600;
        color: var(--color-text-primary);
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 3px;
    }
    .shop-product-mini-price {
        font-size: 10px;
        font-weight: 800;
        color: var(--color-primary-dark);
    }

    /* ============================================================
       SPECIFICATIONS TABLE
    ============================================================ */
    .spec-table {
        width: 100%;
        border-collapse: collapse;
        border: 1px solid var(--color-glass-border);
        border-radius: var(--radius-lg);
        overflow: hidden;
        margin-top: var(--space-4);
    }
    .spec-table th, .spec-table td { padding: var(--space-3) var(--space-4); }
    .spec-table th {
        background: rgba(77, 102, 28, 0.05);
        color: var(--color-primary-dark);
        font-weight: 700;
        width: 30%;
    }
    .spec-table tr:not(:last-child) { border-bottom: 1px solid var(--color-glass-border); }
    .spec-table tr:hover td { background: rgba(132, 204, 22, 0.03); }

    /* ============================================================
       REVIEW SECTION
    ============================================================ */
    .review-grid {
        display: grid;
        grid-template-columns: 1fr 2fr;
        gap: var(--space-8);
        margin-top: var(--space-6);
    }
    @media (max-width: 768px) { .review-grid { grid-template-columns: 1fr; gap: var(--space-6); } }
    .rating-summary-box {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: rgba(77, 102, 28, 0.03);
        border: 1px solid var(--color-glass-border);
        border-radius: var(--radius-xl);
        padding: var(--space-6);
        text-align: center;
    }
    .big-score {
        font-size: 3.5rem;
        font-weight: 800;
        color: var(--color-primary-dark);
        line-height: 1;
    }
    .rating-bar-row {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        width: 100%;
        margin-bottom: var(--space-2);
        font-size: var(--font-size-xs);
    }
    .bar-stars { width: 55px; text-align: right; font-weight: 600; }
    .progress-bar-bg {
        flex: 1;
        height: 8px;
        background: var(--color-border);
        border-radius: var(--radius-full);
        overflow: hidden;
    }
    .progress-bar-fill {
        height: 100%;
        background: #F59E0B;
        border-radius: var(--radius-full);
        transition: width 0.6s ease;
    }
    .bar-count-percent { width: 45px; text-align: left; color: var(--color-text-secondary); font-weight: 600; }

    /* Review Filter Tabs */
    .review-filters-row {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-2);
        border-bottom: 1px solid var(--color-border);
        padding-bottom: var(--space-4);
        margin-bottom: var(--space-6);
    }
    .filter-tab-btn {
        padding: var(--space-2) var(--space-4);
        border-radius: var(--radius-full);
        font-size: var(--font-size-xs);
        font-weight: 700;
        background: #fff;
        border: 1.5px solid var(--color-border);
        color: var(--color-text-secondary);
        cursor: pointer;
        display: flex; align-items: center; gap: 4px;
        transition: var(--transition-premium);
        text-decoration: none;
    }
    .filter-tab-btn:hover, .filter-tab-btn.active {
        border-color: var(--color-primary);
        color: var(--color-primary-dark);
        background: rgba(77, 102, 28, 0.05);
        text-decoration: none;
    }

    /* Review Card */
    .review-card-item {
        padding: var(--space-4) 0;
        border-bottom: 1px dashed var(--color-border);
    }
    .review-card-item:last-child { border-bottom: none; }
    .review-card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: var(--space-2);
    }
    .reviewer-meta { display: flex; align-items: center; gap: var(--space-3); }
    .reviewer-avatar {
        width: 38px; height: 38px;
        border-radius: var(--radius-full);
        background: var(--color-primary-light);
        color: var(--color-primary-dark);
        display: flex; align-items: center; justify-content: center;
        font-weight: 800;
        font-size: var(--font-size-base);
    }
    .reviewer-name { font-weight: 700; font-size: var(--font-size-sm); color: var(--color-text-primary); }
    .review-date { font-size: var(--font-size-xs); color: var(--color-text-muted); }
    .review-body-text {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        line-height: 1.6;
        margin-bottom: var(--space-3);
    }
    .review-attachment-box { display: flex; gap: var(--space-2); flex-wrap: wrap; }
    .review-thumb-image {
        width: 90px; height: 90px;
        border-radius: var(--radius-md);
        overflow: hidden;
        border: 1.5px solid var(--color-border);
        cursor: zoom-in;
        transition: var(--transition-premium);
        background: #fff;
    }
    .review-thumb-image img { width: 100%; height: 100%; object-fit: cover; }
    .review-thumb-image:hover {
        transform: scale(1.04);
        border-color: var(--color-primary);
        box-shadow: var(--shadow-sm);
    }

    /* ============================================================
       SIMILAR PRODUCTS SECTION (below reviews)
    ============================================================ */
    .similar-section {
        margin-top: var(--space-10);
        margin-bottom: var(--space-12);
        position: relative;
    }
    .carousel-header-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--space-5);
    }
    .carousel-arrows { display: flex; gap: var(--space-2); }
    .arrow-btn {
        width: 36px; height: 36px;
        border-radius: var(--radius-full);
        border: 1.5px solid var(--color-primary);
        color: var(--color-primary);
        background: #fff;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        transition: var(--transition-premium);
    }
    .arrow-btn:hover { background: var(--color-primary); color: #fff; }
    .slider-track {
        display: flex;
        gap: var(--space-5);
        overflow-x: auto;
        scroll-behavior: smooth;
        padding-bottom: var(--space-4);
        snap-type: x mandatory;
    }
    .slider-track::-webkit-scrollbar { display: none; }
    .slider-item {
        flex: 0 0 260px;
        snap-align: start;
    }

    /* ============================================================
       MODAL (photo zoom)
    ============================================================ */
    .premium-modal {
        display: none;
        position: fixed;
        top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0, 33, 13, 0.7);
        backdrop-filter: blur(8px);
        -webkit-backdrop-filter: blur(8px);
        z-index: 2000;
        justify-content: center;
        align-items: center;
        opacity: 0;
        transition: opacity 0.3s ease;
    }
    .premium-modal.show { display: flex; opacity: 1; }
    .modal-content-box {
        position: relative;
        max-width: 90%; max-height: 85%;
        border-radius: var(--radius-xl);
        overflow: hidden;
        box-shadow: var(--shadow-lg);
        background: #fff;
        border: 3px solid #fff;
        transform: scale(0.9);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .premium-modal.show .modal-content-box { transform: scale(1); }
    .modal-content-box img { max-width: 100%; max-height: 75vh; display: block; object-fit: contain; }
    .modal-close-btn {
        position: absolute;
        top: 10px; right: 10px;
        width: 36px; height: 36px;
        border-radius: var(--radius-full);
        background: rgba(0, 0, 0, 0.5);
        color: #fff;
        border: none;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        font-size: 1.2rem;
        transition: var(--transition-premium);
    }
    .modal-close-btn:hover { background: rgba(0, 0, 0, 0.8); }

    /* Toast */
    .premium-toast {
        position: fixed;
        bottom: 30px; right: 30px;
        background: var(--color-primary-dark);
        color: #fff;
        border-left: 5px solid var(--color-accent);
        border-radius: var(--radius-md);
        padding: var(--space-4) var(--space-6);
        box-shadow: var(--shadow-lg);
        display: flex; align-items: center; gap: var(--space-3);
        z-index: 3000;
        transform: translateY(100px);
        opacity: 0;
        transition: var(--transition-premium);
    }
    .premium-toast.show { transform: translateY(0); opacity: 1; }
    .premium-toast-icon { color: var(--color-accent); font-size: 1.4rem; }

    /* Voucher copy toast */
    .copy-toast {
        position: fixed;
        bottom: 80px; right: 30px;
        background: #1e293b;
        color: #fff;
        font-size: var(--font-size-sm);
        font-weight: 700;
        border-radius: var(--radius-md);
        padding: var(--space-3) var(--space-4);
        box-shadow: var(--shadow-lg);
        z-index: 3100;
        transform: translateY(20px);
        opacity: 0;
        transition: var(--transition-premium);
        pointer-events: none;
    }
    .copy-toast.show { transform: translateY(0); opacity: 1; }
</style>

<div class="detail-container">
    <div class="container">
        
        <!-- ① Breadcrumbs Navigation -->
        <div class="breadcrumbs">
            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
            <span class="separator"><i class="fa-solid fa-chevron-right"></i></span>
            <a href="${pageContext.request.contextPath}/products">Trái cây sạch</a>
            <span class="separator"><i class="fa-solid fa-chevron-right"></i></span>
            <span class="text-muted"><c:out value="${product.name}"/></span>
        </div>

        <!-- ② 2-Column Product Layout -->
        <div class="detail-grid">
            
            <!-- LEFT: Interactive Image Gallery -->
            <div class="gallery-wrapper">
                <div class="main-image-box premium-panel">
                    <c:choose>
                        <c:when test="${not empty images}">
                            <c:set var="firstImg" value="${images[0].filePath}"/>
                            <c:choose>
                                <c:when test="${fn:startsWith(firstImg, 'http://') || fn:startsWith(firstImg, 'https://')}">
                                    <img id="main-product-img" src="${firstImg}" alt="<c:out value='${product.name}'/>">
                                </c:when>
                                <c:otherwise>
                                    <c:set var="resolvedPath" value="${firstImg}"/>
                                    <c:if test="${!fn:startsWith(resolvedPath, '/')}">
                                        <c:set var="resolvedPath" value="/${resolvedPath}"/>
                                    </c:if>
                                    <img id="main-product-img" src="${pageContext.request.contextPath}${resolvedPath}" alt="<c:out value='${product.name}'/>" onerror="handleImageError(this)">
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <img id="main-product-img" src="${pageContext.request.contextPath}/assets/img/placeholder.png" alt="Placeholder">
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- Thumbnails -->
                <c:if test="${fn:length(images) > 1}">
                    <div class="thumbnail-list">
                        <c:forEach var="img" items="${images}" varStatus="status">
                            <c:set var="thumbImg" value="${img.filePath}"/>
                            <c:choose>
                                <c:when test="${fn:startsWith(thumbImg, 'http://') || fn:startsWith(thumbImg, 'https://')}">
                                    <div class="thumbnail-item ${status.index == 0 ? 'active' : ''}" onclick="switchProductImage(this, '${thumbImg}')">
                                        <img src="${thumbImg}" alt="Thumbnail">
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="resolvedThumb" value="${thumbImg}"/>
                                    <c:if test="${!fn:startsWith(resolvedThumb, '/')}">
                                        <c:set var="resolvedThumb" value="/${resolvedThumb}"/>
                                    </c:if>
                                    <div class="thumbnail-item ${status.index == 0 ? 'active' : ''}" onclick="switchProductImage(this, '${pageContext.request.contextPath}${resolvedThumb}')">
                                        <img src="${pageContext.request.contextPath}${resolvedThumb}" alt="Thumbnail">
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <!-- RIGHT: Product Specifications & Cart -->
            <div class="premium-panel flex-grow">
                <!-- Badges Row -->
                <div class="flex items-center space-x-2 mb-3 gap-2">
                    <c:choose>
                        <c:when test="${product.status == 'ACTIVE'}">
                            <span class="badge-stock badge-instock"><i class="fa-solid fa-circle-check mr-1"></i> Còn hàng</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge-stock badge-outstock"><i class="fa-solid fa-circle-xmark mr-1"></i> Hết hàng</span>
                        </c:otherwise>
                    </c:choose>
                    <span class="badge-rating-top">
                        <i class="fa-solid fa-star text-[#F59E0B]"></i>
                        <c:out value="${product.rating}"/> (<c:out value="${totalReviewsCount}"/> Đánh giá)
                    </span>
                    <span class="text-xs text-muted"><i class="fa-solid fa-eye mr-1"></i> <c:out value="${product.viewCount}"/> lượt xem</span>
                </div>

                <!-- Product Name -->
                <h1 class="font-bold text-3xl mb-2 text-[#00210D] font-headline-lg" style="letter-spacing: -0.5px;">
                    <c:out value="${product.name}"/>
                </h1>

                <!-- Origin -->
                <p class="text-sm text-[#44483B] mb-4 flex items-center">
                    <i class="fa-solid fa-location-dot mr-2" style="color: var(--color-primary);"></i>
                    Xuất xứ: <strong><c:out value="${product.originRegion}"/>, <c:out value="${product.originCountry}"/></strong>
                </p>

                <!-- Flash Sale badge if product promotion exists -->
                <c:if test="${not empty productPromotions}">
                    <c:set var="fp" value="${productPromotions[0]}"/>
                    <div class="flash-sale-badge">
                        <i class="fa-solid fa-bolt"></i>
                        Flash Sale:
                        <c:choose>
                            <c:when test="${fp.discountType == 'PERCENT'}">Giảm <c:out value="${fp.discountValue}"/>%</c:when>
                            <c:otherwise>Giảm <ft:currency value="${fp.discountValue}"/></c:otherwise>
                        </c:choose>
                        <span style="opacity:0.5">|</span> Còn lại <c:out value="${fp.maxUses - fp.usedCount}"/> lượt
                    </div>
                </c:if>

                <!-- Price Area -->
                <div class="price-area">
                    <span class="price-main" id="displayed-price">
                        <c:choose>
                            <c:when test="${not empty variants}"><ft:currency value="${variants[0].price}"/></c:when>
                            <c:otherwise>0 ₫</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="price-unit" id="displayed-unit">/ đơn vị</span>
                </div>

                <!-- Product Description -->
                <div class="product-desc-box">
                    <p class="product-desc-text"><c:out value="${product.description}"/></p>
                </div>

                <hr class="border-t border-[#E2ECE7] my-4">

                <!-- Variant Selection -->
                <c:choose>
                    <c:when test="${not empty variants}">
                        <div class="variant-section">
                            <div class="section-sub-title">Chọn phân loại / đóng gói:</div>
                            <div class="variant-chips">
                                <c:forEach var="v" items="${variants}" varStatus="status">
                                    <input type="radio" name="product_variant"
                                           id="v_${v.variantId}"
                                           class="variant-chip-input"
                                           value="${v.variantId}"
                                           data-price="${v.price}"
                                           data-label="${v.variantLabel}"
                                           data-stock="${v.stockQuantity}"
                                           ${status.index == 0 ? 'checked' : ''}
                                           onchange="onVariantChange(this)">
                                    <label for="v_${v.variantId}" class="variant-chip-label">
                                        <c:out value="${v.variantLabel}"/>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-sm text-danger font-semibold my-4">Không có biến thể khả dụng cho sản phẩm này.</div>
                    </c:otherwise>
                </c:choose>

                <!-- Stock Indicator -->
                <div class="stock-indicator" id="variant-stock-hint">
                    <c:if test="${not empty variants}">
                        <i class="fa-solid fa-boxes-stacked" style="color:#22c55e;"></i>
                        Tồn kho: <strong style="color:#14532d;"><c:out value="${variants[0].stockQuantity}"/></strong> sản phẩm
                        <div class="stock-bar-bg">
                            <div class="stock-bar-fill" style="width: min(100%, calc(${variants[0].stockQuantity} * 100% / 200));"></div>
                        </div>
                    </c:if>
                </div>

                <!-- Cart Action Row -->
                <c:if test="${not empty variants && product.status == 'ACTIVE'}">
                    <div class="cart-action-row">
                        <div class="qty-selector">
                            <button type="button" class="qty-btn" onclick="adjustQuantity(-1)"><i class="fa-solid fa-minus"></i></button>
                            <input type="number" id="purchase-qty" class="qty-input" value="1" min="1" readonly>
                            <button type="button" class="qty-btn" onclick="adjustQuantity(1)"><i class="fa-solid fa-plus"></i></button>
                        </div>
                        <button type="button" class="btn-add-to-cart-large" onclick="handleAddToCart()">
                            <i class="fa-solid fa-cart-arrow-down"></i> Thêm Vào Giỏ Hàng
                        </button>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- ③ SHOP PROFILE PANEL (below 2-column grid) -->
        <c:if test="${not empty shopProfile}">
            <div class="shop-profile-panel mb-6">
                
                <!-- Shop Header Band (green) -->
                <div class="shop-header-band">
                    <div class="shop-header-left">
                        <div class="shop-avatar-lg">
                            <i class="fa-solid fa-store"></i>
                        </div>
                        <div>
                            <div class="shop-name-hero"><c:out value="${shopProfile.shopName}"/></div>
                            <div class="shop-hero-sub">
                                <span class="shop-rating-stars-sm">
                                    <i class="fa-solid fa-star"></i>
                                    <c:out value="${shopProfile.rating}"/>/5
                                </span>
                                <span>•</span>
                                <span>Cửa hàng Đảm Bảo</span>
                                <c:if test="${not empty shopProfile.shopDescription}">
                                    <span>•</span>
                                    <span><c:out value="${shopProfile.shopDescription}"/></span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/products?ownerId=${product.ownerId}" class="btn-visit-shop-hero">
                        <i class="fa-solid fa-store"></i> Ghé Thăm Shop
                    </a>
                </div>

                <!-- Shipping Info Section -->
                <div class="shop-body-section">
                    <div class="shop-section-label">
                        <i class="fa-solid fa-truck-fast"></i> Thông tin vận chuyển
                    </div>
                    <div class="shipping-info-grid">
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-bolt"></i></div>
                            <div>
                                <div class="shipping-pill-title">Giao Siêu Tốc</div>
                                <div class="shipping-pill-sub">Nhận hàng trong 2–3 tiếng</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-box-open"></i></div>
                            <div>
                                <div class="shipping-pill-title">Đóng Gói Cẩn Thận</div>
                                <div class="shipping-pill-sub">Hộp lạnh bảo quản tươi ngon</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-money-bill-wave"></i></div>
                            <div>
                                <div class="shipping-pill-title">Thanh Toán Khi Nhận</div>
                                <div class="shipping-pill-sub">Hỗ trợ COD toàn quốc</div>
                            </div>
                        </div>
                        <div class="shipping-pill">
                            <div class="shipping-pill-icon"><i class="fa-solid fa-shield-halved"></i></div>
                            <div>
                                <div class="shipping-pill-title">Đảm Bảo Hoàn Tiền</div>
                                <div class="shipping-pill-sub">Đổi trả trong 24h nếu lỗi</div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Voucher Section — Horizontal Slider --%>
                <c:if test="${not empty productPromotions || not empty shopVouchers || not empty systemVouchers}">
                    <div class="shop-body-section">
                        <div class="shop-section-label">
                            <i class="fa-solid fa-ticket"></i> Mã giảm giá &amp; Voucher
                        </div>
                        <div class="voucher-slider-wrapper">
                            <div class="voucher-slider-nav">
                                <button class="voucher-nav-btn" id="voucher-prev" onclick="slideVoucher(-1)" aria-label="Trước">
                                    <i class="fa-solid fa-chevron-left"></i>
                                </button>
                                <div class="voucher-dots" id="voucher-dots"></div>
                                <button class="voucher-nav-btn" id="voucher-next" onclick="slideVoucher(1)" aria-label="Tiếp">
                                    <i class="fa-solid fa-chevron-right"></i>
                                </button>
                            </div>
                            <div class="voucher-track-container">
                                <div class="voucher-track" id="voucher-track">

                                    <%-- Voucher sản phẩm (Flash Sale) --%>
                                    <c:forEach var="pv" items="${productPromotions}">
                                        <div class="voucher-item type-product">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-bolt"></i></span>
                                                FLASH
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${pv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${pv.discountType == 'PERCENT'}">Giảm <c:out value="${pv.discountValue}"/>% sản phẩm này</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${pv.discountValue}"/> cho sản phẩm</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${pv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${pv.minOrderValue}"/></c:if>
                                                </div>
                                                <div class="voucher-expire">Còn <c:out value="${pv.maxUses - pv.usedCount}"/> lượt dùng</div>
                                            </div>
                                            <button class="voucher-copy-btn" onclick="copyVoucher(this, '<c:out value="${pv.code}"/>')">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                    <%-- Voucher của Shop --%>
                                    <c:forEach var="sv" items="${shopVouchers}">
                                        <div class="voucher-item type-shop">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-store"></i></span>
                                                SHOP
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${sv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${sv.discountType == 'PERCENT'}">Giảm <c:out value="${sv.discountValue}"/>% đơn hàng</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${sv.discountValue}"/> cho đơn hàng</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${sv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${sv.minOrderValue}"/></c:if>
                                                </div>
                                            </div>
                                            <button class="voucher-copy-btn" onclick="copyVoucher(this, '<c:out value="${sv.code}"/>')">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                    <%-- Voucher hệ thống --%>
                                    <c:forEach var="syv" items="${systemVouchers}">
                                        <div class="voucher-item type-system">
                                            <div class="voucher-ribbon">
                                                <span class="voucher-ribbon-icon"><i class="fa-solid fa-leaf"></i></span>
                                                SÀN
                                            </div>
                                            <div class="voucher-body">
                                                <div class="voucher-code"><c:out value="${syv.code}"/></div>
                                                <div class="voucher-desc">
                                                    <c:choose>
                                                        <c:when test="${syv.discountType == 'PERCENT'}">Giảm <c:out value="${syv.discountValue}"/>% toàn sàn</c:when>
                                                        <c:otherwise>Giảm <ft:currency value="${syv.discountValue}"/> đơn hàng</c:otherwise>
                                                    </c:choose>
                                                    <c:if test="${syv.minOrderValue > 0}"> • Đơn tối thiểu <ft:currency value="${syv.minOrderValue}"/></c:if>
                                                </div>
                                            </div>
                                            <button class="voucher-copy-btn" onclick="copyVoucher(this, '<c:out value="${syv.code}"/>')">SAO CHÉP</button>
                                        </div>
                                    </c:forEach>

                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- More from this shop -->
                <c:if test="${not empty shopOtherProducts}">
                    <div class="shop-body-section">
                        <div class="shop-section-label" style="justify-content: space-between;">
                            <span><i class="fa-solid fa-layer-group"></i> Xem thêm sản phẩm từ cửa hàng này</span>
                            <a href="${pageContext.request.contextPath}/products?ownerId=${product.ownerId}" 
                               style="font-size:10px; color:var(--color-primary); font-weight:700; text-transform:none; letter-spacing:0;">
                                Xem tất cả <i class="fa-solid fa-arrow-right ml-1"></i>
                            </a>
                        </div>
                        <div class="shop-products-slider" id="shop-slider">
                            <c:forEach var="sp" items="${shopOtherProducts}">
                                <a href="${pageContext.request.contextPath}/products/detail?id=${sp.productId}" 
                                   class="shop-product-mini" style="text-decoration:none; color:inherit;">
                                    <img src="${sp.image}" class="shop-product-mini-img" alt="<c:out value='${sp.name}'/>" onerror="handleImageError(this)">
                                    <div class="shop-product-mini-info">
                                        <div class="shop-product-mini-name"><c:out value="${sp.name}"/></div>
                                        <div class="shop-product-mini-price"><ft:currency value="${sp.price}"/></div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

            </div>
        </c:if>

        <!-- ④ Product Specifications Panel -->
        <div class="premium-panel mb-6">
            <h2 class="font-headline-lg text-[#00210D] font-bold text-xl mb-2 flex items-center">
                <i class="fa-solid fa-circle-info mr-2" style="color: var(--color-primary);"></i>
                Thông số kỹ thuật &amp; Bảo quản
            </h2>
            <table class="spec-table">
                <tbody>
                    <tr>
                        <th>Hạn dùng (Kể từ ngày thu hoạch)</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty product.shelfLifeDays}"><c:out value="${product.shelfLifeDays}"/> ngày</c:when>
                                <c:otherwise>Xem trên bao bì</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Ngày thu hoạch</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty product.formattedHarvestDate}"><c:out value="${product.formattedHarvestDate}"/></c:when>
                                <c:otherwise>Xem trên bao bì</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Quốc gia xuất khẩu</th>
                        <td><c:out value="${product.originCountry}"/></td>
                    </tr>
                    <tr>
                        <th>Hướng dẫn bảo quản</th>
                        <td><c:out value="${product.storageInstruction}"/></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- ⑤ Reviews Section -->
        <div class="premium-panel mb-6" id="reviews">
            <h2 class="font-headline-lg text-[#00210D] font-bold text-2xl mb-6">
                Đánh giá từ khách hàng
            </h2>

            <div class="review-grid mb-8">
                <!-- Rating Score Summary -->
                <div class="rating-summary-box">
                    <div class="big-score"><c:out value="${product.rating}"/></div>
                    <div class="my-2"><ft:stars rating="${product.rating}"/></div>
                    <div class="text-sm text-muted"><c:out value="${totalReviewsCount}"/> đánh giá thực tế</div>
                </div>

                <!-- Distribution Bars -->
                <div class="flex flex-col justify-center">
                    <c:forEach var="star" begin="1" end="5">
                        <c:set var="starIndex" value="${6 - star}"/>
                        <c:set var="starCount" value="${ratingDistribution[starIndex] != null ? ratingDistribution[starIndex] : 0}"/>
                        <c:set var="starPercent" value="${totalReviewsCount > 0 ? (starCount * 100 / totalReviewsCount) : 0}"/>
                        <div class="rating-bar-row">
                            <div class="bar-stars">${starIndex} <i class="fa-solid fa-star text-[#F59E0B]"></i></div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" style="width: ${starPercent}%"></div>
                            </div>
                            <div class="bar-count-percent">
                                <fmt:formatNumber value="${starPercent}" maxFractionDigits="0"/>% (${starCount})
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- Star Filter Tabs -->
            <div class="review-filters-row">
                <a href="${pageContext.request.contextPath}/products/detail?id=${product.productId}#reviews"
                   class="filter-tab-btn ${ratingFilter == null ? 'active' : ''}">
                    Tất cả (${totalReviewsCount})
                </a>
                <c:forEach var="starIndex" begin="1" end="5">
                    <c:set var="sVal" value="${6 - starIndex}"/>
                    <c:set var="sCount" value="${ratingDistribution[sVal] != null ? ratingDistribution[sVal] : 0}"/>
                    <a href="${pageContext.request.contextPath}/products/detail?id=${product.productId}&amp;rating=${sVal}#reviews"
                       class="filter-tab-btn ${ratingFilter == sVal ? 'active' : ''}">
                        ${sVal} Sao (${sCount})
                    </a>
                </c:forEach>
            </div>

            <!-- Paginated Review List -->
            <div class="reviews-list-wrapper">
                <c:choose>
                    <c:when test="${not empty reviewPagedResult.items}">
                        <c:forEach var="r" items="${reviewPagedResult.items}">
                            <div class="review-card-item">
                                <div class="review-card-header">
                                    <div class="reviewer-meta">
                                        <div class="reviewer-avatar">
                                            ${fn:substring(r.customerName, 0, 1)}
                                        </div>
                                        <div>
                                            <div class="reviewer-name"><c:out value="${r.customerName}"/></div>
                                            <div class="flex items-center gap-2">
                                                <ft:stars rating="${r.rating}"/>
                                                <span class="review-date"><c:out value="${r.createdAt}"/></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="review-body-text">
                                    <c:out value="${r.reviewText}"/>
                                </div>
                                <c:if test="${not empty r.reviewImageUrl}">
                                    <div class="review-attachment-box">
                                        <div class="review-thumb-image" onclick="openPhotoModal(this)">
                                            <img src="${r.reviewImageUrl}" alt="Ảnh review khách hàng">
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>

                        <!-- Pagination -->
                        <div class="mt-6">
                            <c:set var="ratingQueryParam" value="${ratingFilter != null ? '&amp;rating=' : ''}${ratingFilter != null ? ratingFilter : ''}"/>
                            <ft:pagination current="${reviewPagedResult.currentPage}"
                                           total="${reviewPagedResult.totalPages}"
                                           baseUrl="${pageContext.request.contextPath}/products/detail?id=${product.productId}${ratingQueryParam}"/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center text-[#8E9285] py-8 italic font-semibold">
                            Chưa có lượt đánh giá nào phù hợp với bộ lọc đã chọn.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- ⑥ Similar Products Slider (below reviews) -->
        <c:if test="${not empty similarProducts}">
            <div class="similar-section">
                <div class="carousel-header-row">
                    <h2 class="font-headline-lg text-[#00210D] font-bold text-2xl">
                        Sản Phẩm Tương Tự
                    </h2>
                    <div class="carousel-arrows">
                        <button class="arrow-btn" onclick="slideCarousel(-1)"><i class="fa-solid fa-chevron-left"></i></button>
                        <button class="arrow-btn" onclick="slideCarousel(1)"><i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                </div>
                
                <div class="slider-track" id="similar-slider">
                    <c:forEach var="p" items="${similarProducts}">
                        <div class="slider-item">
                            <div class="product-card" style="margin-bottom: var(--space-1);">
                                <a href="${pageContext.request.contextPath}/products/detail?id=${p.productId}" class="product-card-link" style="text-decoration: none; color: inherit;">
                                    <img src="${p.image}"
                                         id="similar_img_${p.productId}" onerror="handleImageError(this)"
                                         class="object-cover w-full h-[180px] rounded-t-xl" alt="<c:out value='${p.name}'/>">
                                    <h3 class="font-bold text-sm px-3 pt-3 line-clamp-1"><c:out value="${p.name}"/></h3>
                                    <div class="product-card__price px-3 py-1 font-bold" style="color: var(--color-primary);">
                                        Giá chỉ từ <ft:currency value="${p.price}"/>
                                    </div>
                                    <div class="product-card__rating px-3 pb-1">
                                        <ft:stars rating="${p.rating}"/>
                                    </div>
                                    <div class="product-card__shop px-3 pb-3 text-xs text-muted flex items-center justify-between">
                                        <span>Nguồn: <c:out value="${p.originRegion}"/></span>
                                        <span class="bg-emerald-100 text-primary text-[9px] font-semibold px-2 py-0.5 rounded-full">/ <c:out value="${p.unit}"/></span>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

    </div>
</div>

<!-- Photo Zoom Modal -->
<div id="photo-viewer-modal" class="premium-modal" onclick="closePhotoModal()">
    <div class="modal-content-box" onclick="event.stopPropagation()">
        <button class="modal-close-btn" onclick="closePhotoModal()"><i class="fa-solid fa-xmark"></i></button>
        <img id="modal-expanded-img" src="" alt="Expanded review photo">
    </div>
</div>

<!-- Cart Success Toast -->
<div id="cart-added-toast" class="premium-toast">
    <span class="premium-toast-icon"><i class="fa-solid fa-circle-check"></i></span>
    <div>
        <strong style="display: block;">Thành công!</strong>
        <span class="text-xs">Sản phẩm đã được thêm vào giỏ hàng.</span>
    </div>
</div>

<!-- Voucher Copy Toast -->
<div id="copy-toast" class="copy-toast">
    <i class="fa-solid fa-copy mr-2"></i> Đã sao chép mã: <strong id="copy-toast-code"></strong>
</div>

<script>
    // Image fallback
    window.handleImageError = function(img) {
        if (!img.dataset.errorStage) {
            img.dataset.errorStage = "1";
            img.src = "https://images.unsplash.com/photo-1610832958506-ee5633619144?w=600&auto=format&fit=crop&q=80";
        } else if (img.dataset.errorStage === "1") {
            img.dataset.errorStage = "2";
            img.src = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MDAgMzAwIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjRmYmY3IiByeD0iMTYiLz48dGV4dCB4PSIyMDAiIHk9IjE2MCIgZm9udC1mYW1pbHk9InN5c3RlbS11aSxzYW5zLXNlcmlmIiBmb250LXNpemU9IjE2IiBmaWxsPSIjNGQ2NjFjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj5IxINuaCDhuqNuaDwvdGV4dD48L3N2Zz4=";
        }
    };

    // 1. Switch main product image from thumbnail
    function switchProductImage(element, src) {
        document.querySelectorAll('.thumbnail-item').forEach(el => el.classList.remove('active'));
        const mainImg = document.getElementById('main-product-img');
        if (mainImg) mainImg.src = src;
        element.classList.add('active');
    }

    // 2. Variant change — update price, unit, stock
    function onVariantChange(radioElement) {
        const price = parseFloat(radioElement.getAttribute('data-price'));
        const label = radioElement.getAttribute('data-label');
        const stock = parseInt(radioElement.getAttribute('data-stock'));

        const priceDisplay = document.getElementById('displayed-price');
        if (priceDisplay) {
            priceDisplay.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
        }
        const unitDisplay = document.getElementById('displayed-unit');
        if (unitDisplay) unitDisplay.textContent = ' / ' + label;

        const stockHint = document.getElementById('variant-stock-hint');
        if (stockHint) stockHint.innerHTML = 'Tồn kho: <strong>' + stock + '</strong> sản phẩm khả dụng';

        const qtyInput = document.getElementById('purchase-qty');
        if (qtyInput) qtyInput.value = 1;
    }

    // 3. Quantity adjustment
    function adjustQuantity(delta) {
        const qtyInput = document.getElementById('purchase-qty');
        if (!qtyInput) return;
        let currentQty = parseInt(qtyInput.value) || 1;
        currentQty += delta;
        let maxStock = 99;
        const checkedVariant = document.querySelector('input[name="product_variant"]:checked');
        if (checkedVariant) maxStock = parseInt(checkedVariant.getAttribute('data-stock')) || 99;
        if (currentQty < 1) currentQty = 1;
        if (currentQty > maxStock) {
            currentQty = maxStock;
            alert('Số lượng yêu cầu vượt quá tồn kho khả dụng (' + maxStock + ')!');
        }
        qtyInput.value = currentQty;
    }

    // 4. Add to cart (Guest Cart via localStorage)
    function handleAddToCart() {
        const checkedVariant = document.querySelector('input[name="product_variant"]:checked');
        if (!checkedVariant) { alert('Vui lòng chọn một phân loại sản phẩm.'); return; }

        const variantId = parseInt(checkedVariant.value);
        const qtyInput = document.getElementById('purchase-qty');
        const quantity = parseInt(qtyInput ? qtyInput.value : 1);
        const name = "<c:out value='${product.name}'/> - " + checkedVariant.getAttribute('data-label');
        const price = parseFloat(checkedVariant.getAttribute('data-price'));

        let imagePath = 'assets/img/placeholder.png';
        const mainImg = document.getElementById('main-product-img');
        if (mainImg) {
            const srcUrl = mainImg.src;
            const contextIdx = srcUrl.indexOf('/Ban_Hoa_Qua_Online/');
            if (contextIdx >= 0) imagePath = srcUrl.substring(contextIdx + '/Ban_Hoa_Qua_Online/'.length);
        }

        if (typeof GuestCart !== 'undefined') {
            GuestCart.add({ variantId, name, price, quantity, imagePath });
            showSuccessToast();
        } else {
            console.warn('GuestCart not defined');
            alert('Thêm vào giỏ hàng thành công!');
        }
    }

    // 5. Show cart success toast
    function showSuccessToast() {
        const toast = document.getElementById('cart-added-toast');
        if (toast) {
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 3000);
        }
    }

    // 6. Similar products carousel scroll
    function slideCarousel(direction) {
        const track = document.getElementById('similar-slider');
        if (track) track.scrollLeft += direction * 280;
    }

    // 6b. Voucher horizontal slider
    (function() {
        let voucherPage = 0;
        const ITEMS_PER_PAGE = 2;

        function initVoucherSlider() {
            const track = document.getElementById('voucher-track');
            const dotsEl = document.getElementById('voucher-dots');
            if (!track || !dotsEl) return;

            const items = track.querySelectorAll('.voucher-item');
            if (items.length === 0) return;

            const totalPages = Math.ceil(items.length / ITEMS_PER_PAGE);

            // Build dots
            dotsEl.innerHTML = '';
            for (let i = 0; i < totalPages; i++) {
                const dot = document.createElement('span');
                dot.className = 'voucher-dot' + (i === 0 ? ' active' : '');
                dot.addEventListener('click', () => goToVoucherPage(i));
                dotsEl.appendChild(dot);
            }

            updateVoucherSlider(totalPages);
        }

        function updateVoucherSlider(totalPages) {
            const track = document.getElementById('voucher-track');
            const dotsEl = document.getElementById('voucher-dots');
            const prevBtn = document.getElementById('voucher-prev');
            const nextBtn = document.getElementById('voucher-next');
            if (!track) return;

            const items = track.querySelectorAll('.voucher-item');
            const tp = totalPages || Math.ceil(items.length / ITEMS_PER_PAGE);

            // Calculate item width dynamically
            const containerWidth = track.parentElement.offsetWidth;
            const gap = 12;
            const itemW = (containerWidth - gap) / ITEMS_PER_PAGE;
            const offset = voucherPage * (itemW + gap) * ITEMS_PER_PAGE;
            track.style.transform = 'translateX(-' + offset + 'px)';

            // Update dots
            if (dotsEl) {
                dotsEl.querySelectorAll('.voucher-dot').forEach((d, i) => {
                    d.classList.toggle('active', i === voucherPage);
                });
            }

            if (prevBtn) prevBtn.disabled = voucherPage === 0;
            if (nextBtn) nextBtn.disabled = voucherPage >= tp - 1;
        }

        function goToVoucherPage(page) {
            const track = document.getElementById('voucher-track');
            if (!track) return;
            const totalPages = Math.ceil(track.querySelectorAll('.voucher-item').length / ITEMS_PER_PAGE);
            voucherPage = Math.max(0, Math.min(page, totalPages - 1));
            updateVoucherSlider(totalPages);
        }

        window.slideVoucher = function(direction) {
            const track = document.getElementById('voucher-track');
            if (!track) return;
            const totalPages = Math.ceil(track.querySelectorAll('.voucher-item').length / ITEMS_PER_PAGE);
            goToVoucherPage(voucherPage + direction);
        };

        document.addEventListener('DOMContentLoaded', initVoucherSlider);
    })();

    // 7. Open photo zoom modal
    function openPhotoModal(thumbElement) {
        const img = thumbElement.querySelector('img');
        if (!img) return;
        const modal = document.getElementById('photo-viewer-modal');
        const modalImg = document.getElementById('modal-expanded-img');
        if (modal && modalImg) {
            modalImg.src = img.src;
            modal.classList.add('show');
        }
    }

    // 8. Close photo zoom modal
    function closePhotoModal() {
        const modal = document.getElementById('photo-viewer-modal');
        if (modal) modal.classList.remove('show');
    }

    // 9. Copy voucher code
    function copyVoucher(btn, code) {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(code).catch(() => {});
        } else {
            const tmp = document.createElement('input');
            tmp.value = code;
            document.body.appendChild(tmp);
            tmp.select();
            document.execCommand('copy');
            document.body.removeChild(tmp);
        }
        const toast = document.getElementById('copy-toast');
        const codeEl = document.getElementById('copy-toast-code');
        if (toast && codeEl) {
            codeEl.textContent = code;
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 2500);
        }
        const origText = btn.textContent;
        btn.textContent = '✓ ĐÃ SAO';
        setTimeout(() => btn.textContent = origText, 2000);
    }

    // 10. Auto-update similar product images from seeded Unsplash fallback map
    document.addEventListener('DOMContentLoaded', () => {
        const fruitImages = {
            1: 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600&auto=format&fit=crop&q=80',
            2: 'https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&auto=format&fit=crop&q=80',
            3: 'https://images.unsplash.com/photo-1571772996211-2f02c9727629?w=600&auto=format&fit=crop&q=80',
            4: 'https://images.unsplash.com/photo-1553279768-865429fa0078?w=600&auto=format&fit=crop&q=80',
            5: 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600&auto=format&fit=crop&q=80',
            6: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80',
            7: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80',
            8: 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600&auto=format&fit=crop&q=80',
            9: 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=600&auto=format&fit=crop&q=80',
            10: 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=600&auto=format&fit=crop&q=80'
        };
        Object.keys(fruitImages).forEach(id => {
            const imgEl = document.getElementById('similar_img_' + id);
            if (imgEl) imgEl.src = fruitImages[id];
        });

        // Initialize displayed-unit from first checked variant
        const firstVariant = document.querySelector('input[name="product_variant"]:checked');
        if (firstVariant) {
            const label = firstVariant.getAttribute('data-label');
            const unitEl = document.getElementById('displayed-unit');
            if (unitEl && label) unitEl.textContent = ' / ' + label;
        }
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
