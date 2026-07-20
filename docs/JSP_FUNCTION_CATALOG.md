# JSP/JSPF Function Catalog

Nguồn: `web/WEB-INF/jsp/**/*.jsp` và `*.jspf`. Các occurrence cùng function key là cùng chức năng; không tính lặp theo từng product/order item.

- JSP/JSPF files scanned: 77
- Unique function/form keys: 181

| Function key | Type | Occurrences | Source files | Sample |
|---|---|---:|---|---|
| `${pageContext.request.contextPath}/admin/categories` | form-action | 4 | web/WEB-INF/jsp/admin/admin-categories.jsp | <form method="POST" action="${pageContext.request.contextPath}/admin/categories" class="inline"> |
| `${pageContext.request.contextPath}/admin/config` | form-action | 2 | web/WEB-INF/jsp/admin/admin-config.jsp | <form method="POST" action="${pageContext.request.contextPath}/admin/config" onsubmit="return confirmClearSessions(event)"> |
| `${pageContext.request.contextPath}/admin/notifications` | form-action | 1 | web/WEB-INF/jsp/admin/admin-notifications.jsp | <form action="${pageContext.request.contextPath}/admin/notifications" method="POST" onsubmit="return confirmSend(event)"> |
| `${pageContext.request.contextPath}/admin/order-monitor` | form-action | 1 | web/WEB-INF/jsp/admin/order-monitor.jsp | <form action="${pageContext.request.contextPath}/admin/order-monitor" method="get" class="flex flex-wrap items-center justify-between gap-2 mb-4"> |
| `${pageContext.request.contextPath}/admin/orders` | form-action | 3 | web/WEB-INF/jsp/admin/orders.jsp | <form action="${pageContext.request.contextPath}/admin/orders" method="POST" class="inline" onsubmit="return confirmApprove(event, '${order.orderId}')"> |
| `${pageContext.request.contextPath}/admin/products` | form-action | 3 | web/WEB-INF/jsp/admin/admin-products.jsp | <form method="POST" action="${pageContext.request.contextPath}/admin/products" class="inline" onsubmit="return confirmBan(event)" data-product-name="${fn:escapeXml(p.name)}"> |
| `${pageContext.request.contextPath}/admin/profile` | form-action | 2 | web/WEB-INF/jsp/admin/admin-profile.jsp | <form action="${pageContext.request.contextPath}/admin/profile" method="POST" class="space-y-4"> |
| `${pageContext.request.contextPath}/admin/refunds` | form-action | 4 | web/WEB-INF/jsp/admin/admin-refunds.jsp | <form action="${pageContext.request.contextPath}/admin/refunds" method="POST" class="w-full"> |
| `${pageContext.request.contextPath}/admin/reports` | form-action | 1 | web/WEB-INF/jsp/admin/report.jsp | <form method="GET" action="${pageContext.request.contextPath}/admin/reports" class="glass-card p-5 mb-8"> |
| `${pageContext.request.contextPath}/admin/settlements` | form-action | 4 | web/WEB-INF/jsp/admin/admin-settlements.jsp | <form action="${pageContext.request.contextPath}/admin/settlements" method="POST" class="inline"> |
| `${pageContext.request.contextPath}/admin/shops/manage` | form-action | 2 | web/WEB-INF/jsp/admin/admin-shops.jsp | <form action="${pageContext.request.contextPath}/admin/shops/manage" method="POST" class="inline" onsubmit="return confirmSuspend(event, '${fn:escapeXml(shop.shopName)}')"> |
| `${pageContext.request.contextPath}/auth/change-password` | form-action | 1 | web/WEB-INF/jsp/auth/change-password.jsp | <form action="${pageContext.request.contextPath}/auth/change-password" method="post" class="space-y-6" id="changeForm"> |
| `${pageContext.request.contextPath}/auth/forgot` | form-action | 1 | web/WEB-INF/jsp/auth/forgot-password.jsp | <form action="${pageContext.request.contextPath}/auth/forgot" method="post" class="space-y-6" id="forgotForm"> |
| `${pageContext.request.contextPath}/auth/login` | form-action | 1 | web/WEB-INF/jsp/auth/login.jsp | <form action="${pageContext.request.contextPath}/auth/login" method="post" class="space-y-6" id="loginForm"> |
| `${pageContext.request.contextPath}/auth/register` | form-action | 1 | web/WEB-INF/jsp/auth/register.jsp | <form action="${pageContext.request.contextPath}/auth/register" method="post" enctype="multipart/form-data" class="space-y-6" id="registerForm"> |
| `${pageContext.request.contextPath}/auth/reset-password` | form-action | 1 | web/WEB-INF/jsp/auth/reset-password.jsp | <form action="${pageContext.request.contextPath}/auth/reset-password" method="post" class="space-y-6" id="resetForm"> |
| `${pageContext.request.contextPath}/checkout` | form-action | 3 | web/WEB-INF/jsp/customer/checkout.jsp<br>web/WEB-INF/jsp/customer/order-payment.jsp | <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="post" onsubmit="return validateCheckoutForm()" class="grid grid-cols-1 lg:grid-cols-12 gap-gutt |
| `${pageContext.request.contextPath}/contact` | form-action | 1 | web/WEB-INF/jsp/guest/about.jsp | <form id="contactForm" action="${pageContext.request.contextPath}/contact" method="post" class="space-y-5"> |
| `${pageContext.request.contextPath}/customer/shop-apply` | form-action | 1 | web/WEB-INF/jsp/customer/shop-apply.jsp | <form action="${pageContext.request.contextPath}/customer/shop-apply" method="post" enctype="multipart/form-data" class="space-y-6" id="shopApplyForm"> |
| `${pageContext.request.contextPath}/delivery/confirm-success` | form-action | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | <form action="${pageContext.request.contextPath}/delivery/confirm-success" method="POST" enctype="multipart/form-data"> |
| `${pageContext.request.contextPath}/home` | form-action | 1 | web/WEB-INF/jsp/guest/home.jsp | <form action="${pageContext.request.contextPath}/home" method="get" class="flex items-center w-full" id="heroSearchForm" onsubmit="handleHeroSearch(event)"> |
| `${pageContext.request.contextPath}/notifications` | form-action | 3 | web/WEB-INF/jsp/customer/notification.jsp | <form action="${pageContext.request.contextPath}/notifications" method="POST" class="w-full sm:w-auto shrink-0"> |
| `${pageContext.request.contextPath}/orders` | form-action | 6 | web/WEB-INF/jsp/customer/order-detail.jsp<br>web/WEB-INF/jsp/customer/orders.jsp | <form action="${pageContext.request.contextPath}/orders" method="POST" class="flex flex-col gap-3" onsubmit="return confirmCancel(event)"> |
| `${pageContext.request.contextPath}/products` | form-action | 2 | web/WEB-INF/jsp/common/navbar.jsp<br>web/WEB-INF/jsp/guest/product-list.jsp | <form action="${pageContext.request.contextPath}/products" method="get" class="navbar__search"> |
| `${pageContext.request.contextPath}/profile` | form-action | 6 | web/WEB-INF/jsp/common/profile.jsp | <form action="${pageContext.request.contextPath}/profile" method="post" enctype="multipart/form-data" class="space-y-6"> |
| `${pageContext.request.contextPath}/returns` | form-action | 2 | web/WEB-INF/jsp/admin/return-requests.jsp<br>web/WEB-INF/jsp/customer/return-request.jsp | <form action="${pageContext.request.contextPath}/returns" method="post"> |
| `${pageContext.request.contextPath}/reviews` | form-action | 4 | web/WEB-INF/jsp/customer/order-reviews.jsp<br>web/WEB-INF/jsp/customer/review-submit.jsp<br>web/WEB-INF/jsp/customer/review.jsp | <form action="${pageContext.request.contextPath}/reviews" method="POST" class="d-inline" onsubmit="return confirmDeleteReview(event)"> |
| `${pageContext.request.contextPath}/shop/inventory` | form-action | 1 | web/WEB-INF/jsp/shop/inventory.jsp | <form action="${pageContext.request.contextPath}/shop/inventory" method="POST" id="restockForm"> |
| `${pageContext.request.contextPath}/shop/orders` | form-action | 5 | web/WEB-INF/jsp/shop/order-detail.jsp<br>web/WEB-INF/jsp/shop/orders.jsp | <form action="${pageContext.request.contextPath}/shop/orders" method="post" class="inline-flex"> |
| `${pageContext.request.contextPath}/shop/products` | form-action | 1 | web/WEB-INF/jsp/shop/product-list.jsp | <form action="${pageContext.request.contextPath}/shop/products" method="GET" class="flex flex-col gap-4"> |
| `${pageContext.request.contextPath}/shop/profile` | form-action | 1 | web/WEB-INF/jsp/shop/profile.jsp | <form action="${pageContext.request.contextPath}/shop/profile" method="post" class="space-y-4" id="shop-info-form"> |
| `${pageContext.request.contextPath}/shop/reports` | form-action | 1 | web/WEB-INF/jsp/shop/report.jsp | <form method="GET" action="${pageContext.request.contextPath}/shop/reports" class="glass-card p-5 mb-8 rounded-2xl"> |
| `${pageContext.request.contextPath}/shop/settings` | form-action | 1 | web/WEB-INF/jsp/shop/settings.jsp | <form action="${pageContext.request.contextPath}/shop/settings" method="post" class="space-y-6" id="settings-form"> |
| `${pageContext.request.contextPath}/shop/settlement` | form-action | 3 | web/WEB-INF/jsp/shop/settlement.jsp | <form action="${pageContext.request.contextPath}/shop/settlement" method="POST" onsubmit="return confirmShopSettlement(event, this, '${s.settlementId}', '${s.netAmount}')" class="i |
| `${pageContext.request.contextPath}/shop/status` | form-action | 1 | web/WEB-INF/jsp/shop/status.jsp | <form action="${pageContext.request.contextPath}/shop/status" method="post" enctype="multipart/form-data" class="space-y-6" id="resubmitForm"> |
| `${pageContext.request.contextPath}${promotionBasePath}` | form-action | 3 | web/WEB-INF/jsp/shop/promotion.jsp | <form action="${pageContext.request.contextPath}${promotionBasePath}" method="POST" class="space-y-4"> |
| `${pageContext.request.contextPath}${requestScope.forgotMode ? ` | form-action | 2 | web/WEB-INF/jsp/auth/verify.jsp | <form action="${pageContext.request.contextPath}${requestScope.forgotMode ? '/auth/forgot-verify' : '/auth/verify'}" method="post" class="space-y-5" id="verifyForm"> |
| `addNewImagePreviews` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | addNewImagePreviews(this) |
| `addPackagingRow` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | addPackagingRow() |
| `addToCart` | client-handler | 1 | web/WEB-INF/jsp/shop/view.jsp | addToCart( |
| `addVariantRow` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | addVariantRow() |
| `adjustQuantity` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | adjustQuantity(-1) |
| `adjustQuickAddQty` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | adjustQuickAddQty(-1) |
| `applyAiPrompt` | client-handler | 3 | web/WEB-INF/jsp/guest/home.jsp | applyAiPrompt( |
| `applyCoupon` | client-handler | 1 | web/WEB-INF/jsp/customer/checkout.jsp | applyCoupon() |
| `approveShop` | client-handler | 1 | web/WEB-INF/jsp/admin/shop-approvals.jsp | approveShop( |
| `calculateSubtotal` | client-handler | 1 | web/WEB-INF/jsp/guest/product-detail.jsp | calculateSubtotal() |
| `changeReviewPage` | client-handler | 3 | web/WEB-INF/jsp/guest/product-detail.jsp | changeReviewPage( |
| `claimOrder` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | claimOrder( |
| `clearAiChatHistory` | client-handler | 1 | web/WEB-INF/jsp/common/footer.jsp | clearAiChatHistory() |
| `clearDirty` | client-handler | 1 | web/WEB-INF/jsp/shop/profile.jsp | clearDirty() |
| `closeAddressModal` | client-handler | 2 | web/WEB-INF/jsp/common/profile.jsp | closeAddressModal() |
| `closeAssignModal` | client-handler | 1 | web/WEB-INF/jsp/admin/orders.jsp | closeAssignModal() |
| `closeCancelModal` | client-handler | 1 | web/WEB-INF/jsp/admin/orders.jsp | closeCancelModal() |
| `closeDecisionModal` | client-handler | 1 | web/WEB-INF/jsp/admin/return-requests.jsp | closeDecisionModal() |
| `closeDetailModal` | client-handler | 4 | web/WEB-INF/jsp/admin/admin-shops.jsp<br>web/WEB-INF/jsp/admin/shop-approvals.jsp | closeDetailModal() |
| `closeModal` | client-handler | 20 | web/WEB-INF/jsp/admin/admin-categories.jsp<br>web/WEB-INF/jsp/admin/admin-config.jsp<br>web/WEB-INF/jsp/admin/admin-products.jsp<br>web/WEB-INF/jsp/delivery/dashboard.jsp | closeModal( |
| `closeOrderDetailModal` | client-handler | 2 | web/WEB-INF/jsp/common/profile.jsp | closeOrderDetailModal() |
| `closePhotoModal` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | closePhotoModal() |
| `closePremiumToast` | client-handler | 1 | web/WEB-INF/jsp/common/alert.jsp | closePremiumToast() |
| `closeProductModal` | client-handler | 3 | web/WEB-INF/jsp/shop/product-list.jsp | closeProductModal() |
| `closeQuickAddModal` | client-handler | 1 | web/WEB-INF/jsp/guest/home.jsp | closeQuickAddModal() |
| `closeRejectModal` | client-handler | 2 | web/WEB-INF/jsp/admin/shop-approvals.jsp | closeRejectModal() |
| `confirmActivate` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-shops.jsp | return confirmActivate(event,  |
| `confirmApprove` | client-handler | 2 | web/WEB-INF/jsp/admin/admin-refunds.jsp<br>web/WEB-INF/jsp/admin/orders.jsp | return confirmApprove(event,  |
| `confirmBan` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-products.jsp | return confirmBan(event) |
| `confirmCancel` | client-handler | 2 | web/WEB-INF/jsp/customer/order-detail.jsp<br>web/WEB-INF/jsp/customer/orders.jsp | return confirmCancel(event) |
| `confirmCancelOrder` | client-handler | 1 | web/WEB-INF/jsp/common/profile.jsp | return confirmCancelOrder(event,  |
| `confirmClearSessions` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-config.jsp | return confirmClearSessions(event) |
| `confirmComplete` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-refunds.jsp | return confirmComplete(event,  |
| `confirmDelete` | client-handler | 2 | web/WEB-INF/jsp/admin/admin-categories.jsp<br>web/WEB-INF/jsp/shop/promotion.jsp | return confirmDelete(event, this) |
| `confirmDeleteAddress` | client-handler | 1 | web/WEB-INF/jsp/common/profile.jsp | return confirmDeleteAddress(event) |
| `confirmDeleteReview` | client-handler | 2 | web/WEB-INF/jsp/customer/order-reviews.jsp<br>web/WEB-INF/jsp/customer/review.jsp | return confirmDeleteReview(event) |
| `confirmNotReceived` | client-handler | 1 | web/WEB-INF/jsp/customer/order-detail.jsp | return confirmNotReceived(event) |
| `confirmPayment` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-settlements.jsp | return confirmPayment(event, this,  |
| `confirmPaymentClick` | client-handler | 1 | web/WEB-INF/jsp/customer/order-payment.jsp | return confirmPaymentClick(this) |
| `confirmQuickAdd` | client-handler | 1 | web/WEB-INF/jsp/guest/home.jsp | confirmQuickAdd() |
| `confirmReceived` | client-handler | 1 | web/WEB-INF/jsp/customer/order-detail.jsp | return confirmReceived(event) |
| `confirmReject` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-refunds.jsp | return confirmReject(event,  |
| `confirmSend` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-notifications.jsp | return confirmSend(event) |
| `confirmShopSettlement` | client-handler | 1 | web/WEB-INF/jsp/shop/settlement.jsp | return confirmShopSettlement(event, this,  |
| `confirmSoftDelete` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | confirmSoftDelete(this) |
| `confirmSuspend` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-shops.jsp | return confirmSuspend(event,  |
| `copyPromoCode` | client-handler | 1 | web/WEB-INF/jsp/shop/view.jsp | copyPromoCode( |
| `copyText` | client-handler | 5 | web/WEB-INF/jsp/customer/order-payment.jsp | copyText(this.dataset.text, this) |
| `copyVoucher` | client-handler | 3 | web/WEB-INF/jsp/guest/product-detail.jsp | copyVoucher(this) |
| `deleteExistingImageModal` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | deleteExistingImageModal( |
| `dismissAllLowStockAlerts` | client-handler | 1 | web/WEB-INF/jsp/shop/dashboard.jsp | dismissAllLowStockAlerts() |
| `disputeShopSettlement` | client-handler | 1 | web/WEB-INF/jsp/shop/settlement.jsp | return disputeShopSettlement(event, this,  |
| `exportToCSV` | client-handler | 2 | web/WEB-INF/jsp/admin/report.jsp<br>web/WEB-INF/jsp/shop/report.jsp | exportToCSV() |
| `fetchProductsAjax` | client-handler | 3 | web/WEB-INF/jsp/guest/product-list.jsp | fetchProductsAjax(\${page - 1}) |
| `filterCategoryAjax` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | filterCategoryAjax(event, null) |
| `getElementById` | client-handler | 7 | web/WEB-INF/jsp/common/profile.jsp<br>web/WEB-INF/jsp/customer/order-detail.jsp<br>web/WEB-INF/jsp/customer/review-submit.jsp<br>web/WEB-INF/jsp/customer/review.jsp | document.getElementById( |
| `goToStep` | client-handler | 5 | web/WEB-INF/jsp/customer/checkout.jsp | goToStep(1) |
| `handleAddToCart` | client-handler | 1 | web/WEB-INF/jsp/guest/product-detail.jsp | handleAddToCart() |
| `handleDocSelect` | client-handler | 1 | web/WEB-INF/jsp/customer/shop-apply.jsp | handleDocSelect(this) |
| `handleFileSelect` | client-handler | 2 | web/WEB-INF/jsp/auth/register.jsp<br>web/WEB-INF/jsp/shop/status.jsp | handleFileSelect(this) |
| `handleHeroSearch` | client-handler | 1 | web/WEB-INF/jsp/guest/home.jsp | handleHeroSearch(event) |
| `handleInlineAddressSubmit` | client-handler | 1 | web/WEB-INF/jsp/customer/checkout.jsp | handleInlineAddressSubmit() |
| `hideInlineAddressForm` | client-handler | 1 | web/WEB-INF/jsp/customer/checkout.jsp | hideInlineAddressForm() |
| `if` | client-handler | 1 | web/WEB-INF/jsp/guest/home.jsp | if(event.target===this)closeQuickAddModal() |
| `markDirty` | client-handler | 1 | web/WEB-INF/jsp/shop/profile.jsp | markDirty() |
| `moderateReview` | client-handler | 2 | web/WEB-INF/jsp/admin/review-management.jsp | moderateReview( |
| `onVariantChange` | client-handler | 1 | web/WEB-INF/jsp/guest/product-detail.jsp | onVariantChange(this) |
| `openAddressModal` | client-handler | 1 | web/WEB-INF/jsp/common/profile.jsp | openAddressModal() |
| `openAddressModalFromBtn` | client-handler | 1 | web/WEB-INF/jsp/common/profile.jsp | openAddressModalFromBtn(this) |
| `openAssignModal` | client-handler | 1 | web/WEB-INF/jsp/admin/orders.jsp | openAssignModal( |
| `openCreateModal` | client-handler | 2 | web/WEB-INF/jsp/shop/product-list.jsp | openCreateModal() |
| `openDecisionModal` | client-handler | 2 | web/WEB-INF/jsp/admin/return-requests.jsp | openDecisionModal( |
| `openDetails` | client-handler | 1 | web/WEB-INF/jsp/shop/settlement.jsp | openDetails( |
| `openDispatchModal` | client-handler | 1 | web/WEB-INF/jsp/shop/orders.jsp | openDispatchModal( |
| `openEditModal` | client-handler | 3 | web/WEB-INF/jsp/admin/admin-categories.jsp<br>web/WEB-INF/jsp/admin/admin-config.jsp<br>web/WEB-INF/jsp/shop/product-list.jsp | openEditModal(this) |
| `openEstimateModal` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | openEstimateModal( |
| `openFailModal` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | openFailModal( |
| `openModal` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-categories.jsp | openModal( |
| `openPhotoModal` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | openPhotoModal(this) |
| `openProofModal` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | openProofModal( |
| `openRejectModal` | client-handler | 1 | web/WEB-INF/jsp/shop/orders.jsp | openRejectModal( |
| `previewImage` | client-handler | 1 | web/WEB-INF/jsp/customer/review-submit.jsp | previewImage(event) |
| `previewReviewImage` | client-handler | 1 | web/WEB-INF/jsp/customer/review.jsp | previewReviewImage(event,  |
| `print` | client-handler | 1 | web/WEB-INF/jsp/customer/invoice.jsp | window.print(); |
| `promptPageJumpForCallback` | client-handler | 1 | web/WEB-INF/jsp/shop/view.jsp | promptPageJumpForCallback(${totalPages},  |
| `promptPageJumpForList` | client-handler | 1 | web/WEB-INF/jsp/guest/product-list.jsp | promptPageJumpForList(\${total}) |
| `promptReviewPageJump` | client-handler | 1 | web/WEB-INF/jsp/guest/product-detail.jsp | promptReviewPageJump( |
| `quickAddProduct` | client-handler | 9 | web/WEB-INF/jsp/guest/home.jsp<br>web/WEB-INF/jsp/guest/product-list.jsp | quickAddProduct(event,  |
| `removeCoupon` | client-handler | 3 | web/WEB-INF/jsp/customer/checkout.jsp | removeCoupon(\ |
| `removeNewImage` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | removeNewImage( |
| `removePackagingRow` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | removePackagingRow(this) |
| `removeVariantRow` | client-handler | 2 | web/WEB-INF/jsp/shop/product-list.jsp | removeVariantRow(this) |
| `reportUnreceivedSettlement` | client-handler | 1 | web/WEB-INF/jsp/shop/settlement.jsp | return reportUnreceivedSettlement(event, this,  |
| `requestRestock` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | requestRestock() |
| `resetAiProductFilter` | client-handler | 3 | web/WEB-INF/jsp/guest/product-list.jsp | return resetAiProductFilter(event) |
| `reviewPaymentIssue` | client-handler | 2 | web/WEB-INF/jsp/admin/admin-settlements.jsp | return reviewPaymentIssue(event, this,  |
| `saveNotifPref` | client-handler | 4 | web/WEB-INF/jsp/shop/settings.jsp | saveNotifPref( |
| `scrollBestSellers` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | scrollBestSellers(-1) |
| `scrollFlashSale` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | scrollFlashSale(-1) |
| `scrollImported` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | scrollImported(-1) |
| `scrollOrganic` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | scrollOrganic(-1) |
| `scrollSeasonal` | client-handler | 2 | web/WEB-INF/jsp/guest/home.jsp | scrollSeasonal(-1) |
| `searchProductsAjax` | client-handler | 3 | web/WEB-INF/jsp/guest/home.jsp | searchProductsAjax(currentKeyword, currentCategoryId, \${currentPage - 1}) |
| `sendAiChatMessage` | client-handler | 1 | web/WEB-INF/jsp/common/footer.jsp | sendAiChatMessage() |
| `sendQuickAiQuery` | client-handler | 4 | web/WEB-INF/jsp/common/footer.jsp | sendQuickAiQuery( |
| `setExistingImagePrimary` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | setExistingImagePrimary( |
| `setQuickDateRange` | client-handler | 4 | web/WEB-INF/jsp/admin/report.jsp<br>web/WEB-INF/jsp/shop/report.jsp | setQuickDateRange(30) |
| `setSlide` | client-handler | 4 | web/WEB-INF/jsp/guest/home.jsp | setSlide(0) |
| `showCancelModal` | client-handler | 2 | web/WEB-INF/jsp/admin/orders.jsp | showCancelModal( |
| `showDetailModal` | client-handler | 2 | web/WEB-INF/jsp/admin/admin-shops.jsp<br>web/WEB-INF/jsp/admin/shop-approvals.jsp | showDetailModal( |
| `showInlineAddressForm` | client-handler | 1 | web/WEB-INF/jsp/customer/checkout.jsp | showInlineAddressForm( |
| `showRejectModal` | client-handler | 1 | web/WEB-INF/jsp/admin/shop-approvals.jsp | showRejectModal( |
| `showStatusDetails` | client-handler | 1 | web/WEB-INF/jsp/shop/status.jsp | showStatusDetails() |
| `simulateSuccessRedirect` | client-handler | 1 | web/WEB-INF/jsp/customer/order-payment.jsp | simulateSuccessRedirect() |
| `slideCarousel` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | slideCarousel(-1) |
| `slideVoucher` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | slideVoucher(-1) |
| `stopPropagation` | client-handler | 4 | web/WEB-INF/jsp/customer/checkout.jsp<br>web/WEB-INF/jsp/customer/order-detail.jsp<br>web/WEB-INF/jsp/guest/product-detail.jsp | event.stopPropagation(); showInlineAddressForm( |
| `submit` | client-handler | 4 | web/WEB-INF/jsp/shop/product-list.jsp | this.form.submit() |
| `submitAssign` | client-handler | 1 | web/WEB-INF/jsp/admin/orders.jsp | return submitAssign(event) |
| `submitCancel` | client-handler | 1 | web/WEB-INF/jsp/admin/orders.jsp | return submitCancel(event) |
| `submitEstimate` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | submitEstimate() |
| `submitFail` | client-handler | 2 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | submitFail() |
| `submitProductForm` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | submitProductForm(event) |
| `submitReject` | client-handler | 1 | web/WEB-INF/jsp/admin/shop-approvals.jsp | submitReject() |
| `switchProductImage` | client-handler | 2 | web/WEB-INF/jsp/guest/product-detail.jsp | switchProductImage(this,  |
| `switchTab` | client-handler | 6 | web/WEB-INF/jsp/auth/register.jsp<br>web/WEB-INF/jsp/shop/view.jsp | switchTab( |
| `syncPromotionTargetFields` | client-handler | 1 | web/WEB-INF/jsp/shop/promotion.jsp | syncPromotionTargetFields() |
| `toggleAddressSelectionList` | client-handler | 2 | web/WEB-INF/jsp/customer/checkout.jsp | toggleAddressSelectionList() |
| `toggleAiChat` | client-handler | 2 | web/WEB-INF/jsp/common/footer.jsp | toggleAiChat() |
| `toggleDangerButton` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-config.jsp | toggleDangerButton(this) |
| `toggleDebug` | client-handler | 1 | web/WEB-INF/jsp/error/500.jsp | toggleDebug() |
| `toggleDropdown` | client-handler | 1 | web/WEB-INF/jsp/shop/promotion.jsp | toggleDropdown(event,  |
| `togglePasswordVisibility` | client-handler | 11 | web/WEB-INF/jsp/auth/change-password.jsp<br>web/WEB-INF/jsp/auth/login.jsp<br>web/WEB-INF/jsp/auth/register.jsp<br>web/WEB-INF/jsp/auth/reset-password.jsp | togglePasswordVisibility( |
| `toggleSaleStatus` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | toggleSaleStatus( |
| `toggleSubOrder` | client-handler | 1 | web/WEB-INF/jsp/customer/order-detail.jsp | toggleSubOrder(this.dataset.orderId) |
| `updateMaxQuantity` | client-handler | 1 | web/WEB-INF/jsp/customer/return-request.jsp | updateMaxQuantity() |
| `updateRatingLabel` | client-handler | 5 | web/WEB-INF/jsp/customer/review.jsp | updateRatingLabel( |
| `updateStatus` | client-handler | 4 | web/WEB-INF/jsp/delivery/dashboard.jsp<br>web/WEB-INF/jsp/delivery/delivery-detail.jsp | updateStatus( |
| `uploadImage` | client-handler | 2 | web/WEB-INF/jsp/shop/profile.jsp | uploadImage(this,  |
| `validateCheckoutForm` | client-handler | 1 | web/WEB-INF/jsp/customer/checkout.jsp | return validateCheckoutForm() |
| `validateDiscountDateInput` | client-handler | 2 | web/WEB-INF/jsp/shop/product-list.jsp | validateDiscountDateInput(this) |
| `viewDetails` | client-handler | 1 | web/WEB-INF/jsp/admin/admin-refunds.jsp | viewDetails( |
| `window.location.href=` | client-handler | 1 | web/WEB-INF/jsp/shop/product-list.jsp | window.location.href= |
| `window[\` | client-handler | 3 | web/WEB-INF/jsp/shop/view.jsp | window[\ |

Quy tắc: function key dùng để đánh số representative trong `uiEvidence`; `duplicateCount` chỉ mô tả số instance render, không cộng thêm LOC.
