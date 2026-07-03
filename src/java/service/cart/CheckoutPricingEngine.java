package service.cart;

import dao.catalog.ProductDAO;
import dao.catalog.ProductVariantDAO;
import dao.shop.PromotionDAO;
import dao.shop.ShopProfileDAO;
import model.dto.checkout.CheckoutCouponDTO;
import model.dto.checkout.CheckoutQuoteDTO;
import model.dto.checkout.CheckoutQuoteRequestDTO;
import model.dto.checkout.CheckoutShopSummaryDTO;
import model.dto.product.CartSummaryDTO;
import model.entity.Promotion;
import model.entity.auth.User;
import model.entity.cart.CartItem;
import model.entity.catalog.Product;
import model.entity.catalog.ProductVariant;
import model.entity.shop.ShopProfile;
import service.shop.PromotionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CheckoutPricingEngine gom toàn bộ logic quote/pricing của checkout.
 */
// Touched for IDE re-indexing
public class CheckoutPricingEngine {

    public static final BigDecimal DELIVERY_FEE_PER_SHOP = new BigDecimal("15000");
    private static final String COUPON_SLOT_SELLER_MERCHANDISE = "SELLER_MERCHANDISE";
    private static final String COUPON_SLOT_PLATFORM_MERCHANDISE = "PLATFORM_MERCHANDISE";
    private static final String COUPON_SLOT_FREE_SHIPPING = "FREE_SHIPPING";
    private static final String COUPON_SLOT_PAYMENT_METHOD = "PAYMENT_METHOD";

    private final CartService cartService = new CartService();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private final ShopProfileDAO shopProfileDAO = new ShopProfileDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final PromotionService promotionService = new PromotionService();

    public CheckoutPricingSnapshot buildSelectionQuote(User user, List<Integer> requestedVariantIds) throws SQLException {
        CheckoutQuoteRequestDTO request = new CheckoutQuoteRequestDTO();
        request.setVariantIds(requestedVariantIds);
        return buildQuote(user, request, false);
    }

    public CheckoutPricingSnapshot buildQuote(User user, CheckoutQuoteRequestDTO request, boolean validateStock)
            throws SQLException {
        CartSummaryDTO cartSummary = cartService.getCart(user.getUserId());
        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
        }
        if (request == null || request.getVariantIds() == null || request.getVariantIds().isEmpty()) {
            throw new IllegalStateException("Danh sách sản phẩm chọn thanh toán đang trống.");
        }

        List<CartItem> checkoutItems = filterCheckoutItems(cartSummary.getItems(), request.getVariantIds());
        Map<Integer, ProductVariant> variantMap = loadVariantMap(checkoutItems);
        Map<Integer, List<CartItem>> itemsByOwner = groupItemsByOwnerId(checkoutItems, variantMap);
        LinkedHashMap<Integer, CheckoutShopSummaryDTO> summariesByOwner = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<CartItem>> entry : itemsByOwner.entrySet()) {
            Integer ownerId = entry.getKey();
            CheckoutShopSummaryDTO summary = new CheckoutShopSummaryDTO();
            summary.setOwnerId(ownerId);
            summary.setShopName(resolveShopName(entry.getValue()));
            summary.setSubtotal(calculateSubtotal(entry.getValue(), variantMap));
            summary.setDeliveryFee(DELIVERY_FEE_PER_SHOP);
            summary.setAutomaticDiscountAmount(calculateDirectSaleAmount(entry.getValue()));
            summary.setEligibleCoupons(loadEligibleShopCoupons(ownerId, summary.getSubtotal()));
            summariesByOwner.put(ownerId, summary);
        }

        CartSummaryDTO selectedCartSummary = new CartSummaryDTO();
        selectedCartSummary.setItems(checkoutItems);
        selectedCartSummary.setSubtotal(calculateSubtotal(checkoutItems, variantMap));
        selectedCartSummary.setDeliveryFee(DELIVERY_FEE_PER_SHOP.multiply(new BigDecimal(itemsByOwner.size())));
        selectedCartSummary.setTotal(selectedCartSummary.getSubtotal().add(selectedCartSummary.getDeliveryFee()));
        selectedCartSummary.setTotalWeight(calculateTotalWeight(checkoutItems));

        CheckoutQuoteDTO quote = new CheckoutQuoteDTO();
        quote.setShopCount(itemsByOwner.size());
        quote.setSubtotal(selectedCartSummary.getSubtotal());
        quote.setDeliveryFee(selectedCartSummary.getDeliveryFee());
        quote.setDirectSaleAmount(calculateDirectSaleAmount(checkoutItems));
        quote.setEligibleSystemCoupons(loadEligibleSystemCoupons(selectedCartSummary.getSubtotal()));
        quote.setShopSummaries(new ArrayList<>(summariesByOwner.values()));

        List<String> validationErrors = collectValidationErrors(checkoutItems, variantMap, itemsByOwner, validateStock);
        if (!validationErrors.isEmpty()) {
            quote.getErrors().addAll(validationErrors);
            quote.setValid(false);
        }

        List<PromotionAllocation> promotionAllocations = new ArrayList<>();
        if (quote.getErrors().isEmpty()) {
            applyRequestedCoupons(request, itemsByOwner, summariesByOwner, quote, promotionAllocations);
        }

        finalizeQuote(summariesByOwner, quote);

        CheckoutPricingSnapshot snapshot = new CheckoutPricingSnapshot();
        snapshot.setCartSummary(selectedCartSummary);
        snapshot.setCheckoutItems(checkoutItems);
        snapshot.setVariantMap(variantMap);
        snapshot.setItemsByOwner(itemsByOwner);
        snapshot.setQuote(quote);
        snapshot.setPromotionAllocations(promotionAllocations);
        if (itemsByOwner.size() == 1) {
            snapshot.setSingleOwnerId(itemsByOwner.keySet().iterator().next());
        }
        return snapshot;
    }

    private void applyRequestedCoupons(CheckoutQuoteRequestDTO request,
                                       Map<Integer, List<CartItem>> itemsByOwner,
                                       Map<Integer, CheckoutShopSummaryDTO> summariesByOwner,
                                       CheckoutQuoteDTO quote,
                                       List<PromotionAllocation> promotionAllocations) throws SQLException {
        Map<Integer, BigDecimal> remainingMerchandiseByOwner = new LinkedHashMap<>();
        Map<Integer, BigDecimal> remainingShippingByOwner = new LinkedHashMap<>();
        for (Map.Entry<Integer, CheckoutShopSummaryDTO> entry : summariesByOwner.entrySet()) {
            remainingMerchandiseByOwner.put(entry.getKey(), entry.getValue().getSubtotal());
            remainingShippingByOwner.put(entry.getKey(), entry.getValue().getDeliveryFee());
        }

        ResolvedCoupons resolvedCoupons = resolveCoupons(request, itemsByOwner, summariesByOwner, quote);
        if (!quote.getErrors().isEmpty()) {
            quote.setValid(false);
            return;
        }

        try {
            promotionService.validateCouponStack(resolvedCoupons.shopPromotions, resolvedCoupons.systemPromotions);
        } catch (RuntimeException ex) {
            quote.getErrors().add(ex.getMessage());
            quote.setValid(false);
            return;
        }

        for (Map.Entry<Integer, Promotion> entry : resolvedCoupons.shopMerchandisePromos.entrySet()) {
            Integer ownerId = entry.getKey();
            Promotion promo = entry.getValue();
            BigDecimal base = remainingMerchandiseByOwner.get(ownerId);
            BigDecimal discount = promotionService.calculateDiscount(promo, base);
            applyOwnerPromotion(quote, summariesByOwner.get(ownerId), promo, ownerId, discount,
                    true, false, remainingMerchandiseByOwner, remainingShippingByOwner, promotionAllocations);
        }

        for (Promotion promo : resolvedCoupons.systemMerchandisePromos) {
            BigDecimal base = sumValues(remainingMerchandiseByOwner);
            BigDecimal discount = promotionService.calculateDiscount(promo, base);
            Map<Integer, BigDecimal> allocated = allocateDiscount(discount, remainingMerchandiseByOwner);

            BigDecimal totalAllocated = BigDecimal.ZERO;
            Map<Integer, BigDecimal> actualAllocations = new LinkedHashMap<>();
            for (Map.Entry<Integer, BigDecimal> entry : allocated.entrySet()) {
                BigDecimal ownerDiscount = entry.getValue();
                if (ownerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Integer ownerId = entry.getKey();
                BigDecimal remainingMerchandise = remainingMerchandiseByOwner.get(ownerId);
                if (remainingMerchandise == null) {
                    remainingMerchandise = BigDecimal.ZERO;
                }
                BigDecimal actualOwnerDiscount = ownerDiscount.min(remainingMerchandise);
                if (actualOwnerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                actualAllocations.put(ownerId, actualOwnerDiscount);
                totalAllocated = totalAllocated.add(actualOwnerDiscount);
            }

            appendTopLevelAppliedCoupon(quote, promo, null, totalAllocated);
            for (Map.Entry<Integer, BigDecimal> entry : actualAllocations.entrySet()) {
                Integer ownerId = entry.getKey();
                BigDecimal ownerDiscount = entry.getValue();
                CheckoutShopSummaryDTO summary = summariesByOwner.get(ownerId);
                summary.setSystemMerchandiseDiscountAmount(
                        summary.getSystemMerchandiseDiscountAmount().add(ownerDiscount));
                summary.setDiscountAmount(summary.getDiscountAmount().add(ownerDiscount));
                summary.getAppliedCoupons().add(buildCouponDTO(promo, ownerId, ownerDiscount, true, null));
                remainingMerchandiseByOwner.put(ownerId,
                        remainingMerchandiseByOwner.get(ownerId).subtract(ownerDiscount).max(BigDecimal.ZERO));
                promotionAllocations.add(new PromotionAllocation(promo, ownerId, ownerDiscount));
            }
        }

        for (Map.Entry<Integer, Promotion> entry : resolvedCoupons.shopShippingPromos.entrySet()) {
            Integer ownerId = entry.getKey();
            Promotion promo = entry.getValue();
            BigDecimal base = remainingShippingByOwner.get(ownerId);
            BigDecimal discount = promotionService.calculateDiscount(promo, base);
            applyOwnerPromotion(quote, summariesByOwner.get(ownerId), promo, ownerId, discount,
                    true, true, remainingMerchandiseByOwner, remainingShippingByOwner, promotionAllocations);
        }

        for (Promotion promo : resolvedCoupons.systemShippingPromos) {
            BigDecimal base = sumValues(remainingShippingByOwner);
            BigDecimal discount = promotionService.calculateDiscount(promo, base);
            Map<Integer, BigDecimal> allocated = allocateDiscount(discount, remainingShippingByOwner);

            BigDecimal totalAllocated = BigDecimal.ZERO;
            Map<Integer, BigDecimal> actualAllocations = new LinkedHashMap<>();
            for (Map.Entry<Integer, BigDecimal> entry : allocated.entrySet()) {
                BigDecimal ownerDiscount = entry.getValue();
                if (ownerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Integer ownerId = entry.getKey();
                BigDecimal remainingShipping = remainingShippingByOwner.get(ownerId);
                if (remainingShipping == null) {
                    remainingShipping = BigDecimal.ZERO;
                }
                BigDecimal actualOwnerDiscount = ownerDiscount.min(remainingShipping);
                if (actualOwnerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                actualAllocations.put(ownerId, actualOwnerDiscount);
                totalAllocated = totalAllocated.add(actualOwnerDiscount);
            }

            appendTopLevelAppliedCoupon(quote, promo, null, totalAllocated);
            for (Map.Entry<Integer, BigDecimal> entry : actualAllocations.entrySet()) {
                Integer ownerId = entry.getKey();
                BigDecimal ownerDiscount = entry.getValue();
                CheckoutShopSummaryDTO summary = summariesByOwner.get(ownerId);
                summary.setSystemShippingDiscountAmount(
                        summary.getSystemShippingDiscountAmount().add(ownerDiscount));
                summary.setDiscountAmount(summary.getDiscountAmount().add(ownerDiscount));
                summary.getAppliedCoupons().add(buildCouponDTO(promo, ownerId, ownerDiscount, true, null));
                remainingShippingByOwner.put(ownerId,
                        remainingShippingByOwner.get(ownerId).subtract(ownerDiscount).max(BigDecimal.ZERO));
                promotionAllocations.add(new PromotionAllocation(promo, ownerId, ownerDiscount));
            }
        }

        Map<Integer, BigDecimal> remainingPaymentByOwner = new LinkedHashMap<>();
        for (Map.Entry<Integer, CheckoutShopSummaryDTO> entry : summariesByOwner.entrySet()) {
            Integer ownerId = entry.getKey();
            CheckoutShopSummaryDTO summary = entry.getValue();
            BigDecimal netMerchandise = summary.getSubtotal()
                    .subtract(summary.getShopMerchandiseDiscountAmount()
                            .add(summary.getSystemMerchandiseDiscountAmount()))
                    .max(BigDecimal.ZERO);
            BigDecimal netShipping = summary.getDeliveryFee()
                    .subtract(summary.getShopShippingDiscountAmount()
                            .add(summary.getSystemShippingDiscountAmount()))
                    .max(BigDecimal.ZERO);
            remainingPaymentByOwner.put(ownerId, netMerchandise.add(netShipping));
        }

        for (Promotion promo : resolvedCoupons.paymentPromos) {
            BigDecimal base = sumValues(remainingPaymentByOwner);
            BigDecimal discount = promotionService.calculateDiscount(promo, base);
            Map<Integer, BigDecimal> allocated = allocateDiscount(discount, remainingPaymentByOwner);

            BigDecimal totalAllocated = BigDecimal.ZERO;
            Map<Integer, BigDecimal> actualAllocations = new LinkedHashMap<>();
            for (Map.Entry<Integer, BigDecimal> entry : allocated.entrySet()) {
                BigDecimal ownerDiscount = entry.getValue();
                if (ownerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Integer ownerId = entry.getKey();
                BigDecimal remainingPayment = remainingPaymentByOwner.get(ownerId);
                if (remainingPayment == null) {
                    remainingPayment = BigDecimal.ZERO;
                }
                BigDecimal actualOwnerDiscount = ownerDiscount.min(remainingPayment);
                if (actualOwnerDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                actualAllocations.put(ownerId, actualOwnerDiscount);
                totalAllocated = totalAllocated.add(actualOwnerDiscount);
            }

            appendTopLevelAppliedCoupon(quote, promo, null, totalAllocated);
            for (Map.Entry<Integer, BigDecimal> entry : actualAllocations.entrySet()) {
                Integer ownerId = entry.getKey();
                BigDecimal ownerDiscount = entry.getValue();
                CheckoutShopSummaryDTO summary = summariesByOwner.get(ownerId);
                summary.setPaymentDiscountAmount(summary.getPaymentDiscountAmount().add(ownerDiscount));
                summary.setDiscountAmount(summary.getDiscountAmount().add(ownerDiscount));
                summary.getAppliedCoupons().add(buildCouponDTO(promo, ownerId, ownerDiscount, true, null));
                remainingPaymentByOwner.put(ownerId,
                        remainingPaymentByOwner.get(ownerId).subtract(ownerDiscount).max(BigDecimal.ZERO));
                promotionAllocations.add(new PromotionAllocation(promo, ownerId, ownerDiscount));
            }
        }
    }

    private List<String> collectValidationErrors(List<CartItem> checkoutItems,
                                                 Map<Integer, ProductVariant> variantMap,
                                                 Map<Integer, List<CartItem>> itemsByOwner,
                                                 boolean validateStockFlag) throws SQLException {
        List<String> errors = new ArrayList<>();
        if (validateStockFlag) {
            errors.addAll(validateStock(checkoutItems, variantMap));
        }
        errors.addAll(validateProductAvailability(checkoutItems, variantMap));
        errors.addAll(validateShopStatus(itemsByOwner));
        return errors;
    }

    private List<String> validateProductAvailability(List<CartItem> checkoutItems,
                                                     Map<Integer, ProductVariant> variantMap) throws SQLException {
        List<String> errors = new ArrayList<>();
        for (CartItem item : checkoutItems) {
            ProductVariant variant = variantMap.get(item.getVariantId());
            if (variant == null) {
                errors.add(resolveProductName(null, item.getProductName()) + " hiện không còn tồn tại.");
                continue;
            }
            Product product = productDAO.findOneById(variant.getProductId());
            String productError = validatePurchasableProduct(product, item.getProductName());
            if (productError != null) {
                errors.add(productError + " (" + item.getVariantLabel() + ")");
            }
        }
        return errors;
    }

    private String resolveProductName(Product product, String fallbackName) {
        if (product != null && product.getName() != null && !product.getName().trim().isEmpty()) {
            return product.getName().trim();
        }
        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            return fallbackName.trim();
        }
        return "Sản phẩm này";
    }

    private String validatePurchasableProduct(Product product, String fallbackName) {
        String productName = resolveProductName(product, fallbackName);
        if (product == null) {
            return productName + " hiện không còn tồn tại.";
        }

        String status = product.getStatus();
        if ("DELETED".equals(status)) {
            return productName + " đã bị gỡ khỏi gian hàng.";
        }
        if ("INACTIVE".equals(status)) {
            return productName + " đã ngừng kinh doanh.";
        }
        if ("OUT_OF_SEASON".equals(status) || !product.isInSeason()) {
            return productName + " đã hết mùa. Vui lòng quay lại khi có vụ mới.";
        }
        return null;
    }

    private List<String> collectValidationErrors(List<CartItem> checkoutItems,
                                                 Map<Integer, ProductVariant> variantMap,
                                                 Map<Integer, List<CartItem>> itemsByOwner,
                                                 boolean validateStockFlag) throws SQLException {
        List<String> errors = new ArrayList<>();
        if (validateStockFlag) {
            errors.addAll(validateStock(checkoutItems, variantMap));
        }
        errors.addAll(validateProductAvailability(checkoutItems, variantMap));
        errors.addAll(validateShopStatus(itemsByOwner));
        return errors;
    }

    private List<String> validateProductAvailability(List<CartItem> checkoutItems,
                                                     Map<Integer, ProductVariant> variantMap) throws SQLException {
        List<String> errors = new ArrayList<>();
        for (CartItem item : checkoutItems) {
            ProductVariant variant = variantMap.get(item.getVariantId());
            if (variant == null) {
                errors.add(resolveProductName(null, item.getProductName()) + " hiện không còn tồn tại.");
                continue;
            }
            Product product = productDAO.findOneById(variant.getProductId());
            String productError = validatePurchasableProduct(product, item.getProductName());
            if (productError != null) {
                errors.add(productError + " (" + item.getVariantLabel() + ")");
            }
        }
        return errors;
    }

    private String resolveProductName(Product product, String fallbackName) {
        if (product != null && product.getName() != null && !product.getName().trim().isEmpty()) {
            return product.getName().trim();
        }
        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            return fallbackName.trim();
        }
        return "Sản phẩm này";
    }

    private String validatePurchasableProduct(Product product, String fallbackName) {
        String productName = resolveProductName(product, fallbackName);
        if (product == null) {
            return productName + " hiện không còn tồn tại.";
        }

        String status = product.getStatus();
        if ("DELETED".equals(status)) {
            return productName + " đã bị gỡ khỏi gian hàng.";
        }
        if ("INACTIVE".equals(status)) {
            return productName + " đã ngừng kinh doanh.";
        }
        if ("OUT_OF_SEASON".equals(status) || !product.isInSeason()) {
            return productName + " đã hết mùa. Vui lòng quay lại khi có vụ mới.";
        }
        return null;
    }

    private void applyOwnerPromotion(CheckoutQuoteDTO quote,
                                     CheckoutShopSummaryDTO summary,
                                     Promotion promo,
                                     Integer ownerId,
                                     BigDecimal discount,
                                     boolean topLevel,
                                     boolean shippingCoupon,
                                     Map<Integer, BigDecimal> remainingMerchandiseByOwner,
                                     Map<Integer, BigDecimal> remainingShippingByOwner,
                                     List<PromotionAllocation> promotionAllocations) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal actualDiscount;
        if (shippingCoupon) {
            BigDecimal remainingShipping = remainingShippingByOwner.get(ownerId);
            if (remainingShipping == null) {
                remainingShipping = BigDecimal.ZERO;
            }
            actualDiscount = discount.min(remainingShipping);
            if (actualDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            summary.setShopShippingDiscountAmount(summary.getShopShippingDiscountAmount().add(actualDiscount));
            remainingShippingByOwner.put(ownerId,
                    remainingShippingByOwner.get(ownerId).subtract(actualDiscount).max(BigDecimal.ZERO));
        } else {
            BigDecimal remainingMerchandise = remainingMerchandiseByOwner.get(ownerId);
            if (remainingMerchandise == null) {
                remainingMerchandise = BigDecimal.ZERO;
            }
            actualDiscount = discount.min(remainingMerchandise);
            if (actualDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            summary.setShopMerchandiseDiscountAmount(summary.getShopMerchandiseDiscountAmount().add(actualDiscount));
            remainingMerchandiseByOwner.put(ownerId,
                    remainingMerchandiseByOwner.get(ownerId).subtract(actualDiscount).max(BigDecimal.ZERO));
        }
        summary.setDiscountAmount(summary.getDiscountAmount().add(actualDiscount));
        summary.getAppliedCoupons().add(buildCouponDTO(promo, ownerId, actualDiscount, true, null));
        if (topLevel) {
            appendTopLevelAppliedCoupon(quote, promo, ownerId, actualDiscount);
        }
        promotionAllocations.add(new PromotionAllocation(promo, ownerId, actualDiscount));
    }

    private ResolvedCoupons resolveCoupons(CheckoutQuoteRequestDTO request,
                                           Map<Integer, List<CartItem>> itemsByOwner,
                                           Map<Integer, CheckoutShopSummaryDTO> summariesByOwner,
                                           CheckoutQuoteDTO quote) throws SQLException {
        ResolvedCoupons resolved = new ResolvedCoupons();
        Promotion sellerMerchandisePromo = null;
        Promotion platformMerchandisePromo = null;
        Promotion freeShippingPromo = null;
        Promotion paymentMethodPromo = null;
        for (String code : normalizeCouponCodes(request.getShopCouponCodes())) {
            Promotion matchedPromo = null;
            Integer matchedOwnerId = null;
            for (Integer ownerId : itemsByOwner.keySet()) {
                BigDecimal subtotal = summariesByOwner.get(ownerId).getSubtotal();
                Promotion candidate = promotionService.validateShopCoupon(code, ownerId, subtotal);
                if (candidate != null) {
                    matchedPromo = candidate;
                    matchedOwnerId = ownerId;
                    break;
                }
            }
            if (matchedPromo == null || matchedOwnerId == null) {
                Promotion existingPromo = promotionDAO.findAnyByCode(code);
                String invalidMessage = existingPromo != null
                        ? "Mã voucher shop chưa đạt giá trị đơn tối thiểu hoặc không thuộc shop nào trong giỏ hàng."
                        : "Mã voucher shop không hợp lệ, chưa đạt giá trị đơn tối thiểu, hoặc không thuộc shop nào trong giỏ hàng.";
                appendInvalidCoupon(quote, code, "SHOP",
                        invalidMessage);
                continue;
            }
            String benefitTarget = promotionService.resolveBenefitTarget(matchedPromo);
            if (PromotionService.BENEFIT_TARGET_PRODUCT.equalsIgnoreCase(benefitTarget)) {
                appendInvalidCoupon(quote, code, "SHOP",
                        "Mã này là voucher theo sản phẩm và không thể áp dụng ở bước thanh toán.");
                continue;
            }
            String slot = resolveCouponSlot(matchedPromo);
            if (COUPON_SLOT_FREE_SHIPPING.equals(slot)) {
                if (freeShippingPromo != null) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            describeSlotConflict(code, slot, freeShippingPromo.getCode()));
                    continue;
                }
                freeShippingPromo = matchedPromo;
                resolved.shopPromotions.add(matchedPromo);
                resolved.shopShippingPromos.put(matchedOwnerId, matchedPromo);
            } else if (COUPON_SLOT_PAYMENT_METHOD.equals(slot)) {
                if (paymentMethodPromo != null) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            describeSlotConflict(code, slot, paymentMethodPromo.getCode()));
                    continue;
                }
                if (platformMerchandisePromo != null
                        && (!matchedPromo.getCanStack() || !platformMerchandisePromo.getCanStack())) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            "Voucher sàn " + platformMerchandisePromo.getCode()
                                    + " và voucher phương thức thanh toán " + code
                                    + " không thể cộng dồn.");
                    continue;
                }
                paymentMethodPromo = matchedPromo;
                resolved.shopPromotions.add(matchedPromo);
                resolved.paymentPromos.add(matchedPromo);
            } else if (COUPON_SLOT_PLATFORM_MERCHANDISE.equals(slot)) {
                if (platformMerchandisePromo != null) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            describeSlotConflict(code, slot, platformMerchandisePromo.getCode()));
                    continue;
                }
                if (paymentMethodPromo != null
                        && (!matchedPromo.getCanStack() || !paymentMethodPromo.getCanStack())) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            "Voucher sàn " + code
                                    + " và voucher phương thức thanh toán " + paymentMethodPromo.getCode()
                                    + " không thể cộng dồn.");
                    continue;
                }
                platformMerchandisePromo = matchedPromo;
                resolved.shopPromotions.add(matchedPromo);
                resolved.shopMerchandisePromos.put(matchedOwnerId, matchedPromo);
            } else {
                if (sellerMerchandisePromo != null) {
                    appendInvalidCoupon(quote, code, "SHOP",
                            describeSlotConflict(code, slot, sellerMerchandisePromo.getCode()));
                    continue;
                }
                sellerMerchandisePromo = matchedPromo;
                resolved.shopPromotions.add(matchedPromo);
                resolved.shopMerchandisePromos.put(matchedOwnerId, matchedPromo);
            }
        }

        BigDecimal totalMerchandiseBase = sumSummarySubtotals(summariesByOwner.values());
        for (String code : normalizeCouponCodes(request.getSystemCouponCodes())) {
            Promotion promo = promotionService.validateSystemCoupon(code, totalMerchandiseBase);
            if (promo == null) {
                appendInvalidCoupon(quote, code, "ALL",
                        "Mã voucher sàn không hợp lệ, đã hết hạn hoặc chưa đủ điều kiện áp dụng.");
                continue;
            }
            String benefitTarget = promotionService.resolveBenefitTarget(promo);
            if (PromotionService.BENEFIT_TARGET_PRODUCT.equalsIgnoreCase(benefitTarget)) {
                appendInvalidCoupon(quote, code, "ALL",
                        "Mã này là voucher theo sản phẩm và không thể áp dụng ở bước thanh toán.");
                continue;
            }
            String slot = resolveCouponSlot(promo);
            if (COUPON_SLOT_FREE_SHIPPING.equals(slot)) {
                if (freeShippingPromo != null) {
                    appendInvalidCoupon(quote, code, "ALL",
                            describeSlotConflict(code, slot, freeShippingPromo.getCode()));
                    continue;
                }
                freeShippingPromo = promo;
                resolved.systemPromotions.add(promo);
                resolved.systemShippingPromos.add(promo);
            } else if (COUPON_SLOT_PAYMENT_METHOD.equals(slot)) {
                if (paymentMethodPromo != null) {
                    appendInvalidCoupon(quote, code, "ALL",
                            describeSlotConflict(code, slot, paymentMethodPromo.getCode()));
                    continue;
                }
                if (platformMerchandisePromo != null
                        && (!promo.getCanStack() || !platformMerchandisePromo.getCanStack())) {
                    appendInvalidCoupon(quote, code, "ALL",
                            "Voucher sàn " + platformMerchandisePromo.getCode()
                                    + " và voucher phương thức thanh toán " + code
                                    + " không thể cộng dồn.");
                    continue;
                }
                paymentMethodPromo = promo;
                resolved.systemPromotions.add(promo);
                resolved.paymentPromos.add(promo);
            } else if (COUPON_SLOT_PLATFORM_MERCHANDISE.equals(slot)) {
                if (platformMerchandisePromo != null) {
                    appendInvalidCoupon(quote, code, "ALL",
                            describeSlotConflict(code, slot, platformMerchandisePromo.getCode()));
                    continue;
                }
                if (paymentMethodPromo != null
                        && (!promo.getCanStack() || !paymentMethodPromo.getCanStack())) {
                    appendInvalidCoupon(quote, code, "ALL",
                            "Voucher sàn " + code
                                    + " và voucher phương thức thanh toán " + paymentMethodPromo.getCode()
                                    + " không thể cộng dồn.");
                    continue;
                }
                platformMerchandisePromo = promo;
                resolved.systemPromotions.add(promo);
                resolved.systemMerchandisePromos.add(promo);
            } else {
                if (sellerMerchandisePromo != null) {
                    appendInvalidCoupon(quote, code, "ALL",
                            describeSlotConflict(code, slot, sellerMerchandisePromo.getCode()));
                    continue;
                }
                sellerMerchandisePromo = promo;
                resolved.systemPromotions.add(promo);
                resolved.systemMerchandisePromos.add(promo);
            }
        }
        return resolved;
    }

    private void appendInvalidCoupon(CheckoutQuoteDTO quote, String code, String discountScope, String message) {
        CheckoutCouponDTO invalidCoupon = new CheckoutCouponDTO();
        invalidCoupon.setCode(code);
        invalidCoupon.setDiscountScope(discountScope);
        invalidCoupon.setValid(false);
        invalidCoupon.setMessage(message);
        quote.getInvalidCoupons().add(invalidCoupon);
        quote.getErrors().add(message + " [" + code + "]");
        quote.setValid(false);
    }

    private void appendTopLevelAppliedCoupon(CheckoutQuoteDTO quote, Promotion promo, Integer ownerId, BigDecimal discountAmount) {
        quote.getAppliedCoupons().add(buildCouponDTO(promo, ownerId, discountAmount, true, null));
    }

    private String resolveCouponSlot(Promotion promo) {
        String benefitTarget = promotionService.resolveBenefitTarget(promo);
        if (PromotionService.BENEFIT_TARGET_SHIPPING.equalsIgnoreCase(benefitTarget)) {
            return COUPON_SLOT_FREE_SHIPPING;
        }
        if (PromotionService.BENEFIT_TARGET_PAYMENT_METHOD.equalsIgnoreCase(benefitTarget)) {
            return COUPON_SLOT_PAYMENT_METHOD;
        }
        String discountScope = promo != null && promo.getDiscountScope() != null
                ? promo.getDiscountScope().trim().toUpperCase()
                : "";
        if ("ALL".equals(discountScope)) {
            return COUPON_SLOT_PLATFORM_MERCHANDISE;
        }
        return COUPON_SLOT_SELLER_MERCHANDISE;
    }

    private String humanizeCouponSlot(String slot) {
        if (COUPON_SLOT_PLATFORM_MERCHANDISE.equals(slot)) {
            return "voucher sàn";
        }
        if (COUPON_SLOT_FREE_SHIPPING.equals(slot)) {
            return "voucher miễn phí vận chuyển";
        }
        if (COUPON_SLOT_PAYMENT_METHOD.equals(slot)) {
            return "voucher phương thức thanh toán";
        }
        return "voucher shop";
    }

    private String describeSlotConflict(String code, String slot, String existingCode) {
        String slotLabel = humanizeCouponSlot(slot);
        if (existingCode != null && !existingCode.trim().isEmpty()) {
            return "Mỗi checkout chỉ được tối đa 1 " + slotLabel + ". Mã ["
                    + existingCode + "] đã chiếm slot này, nên không thể áp dụng [" + code + "].";
        }
        return "Mỗi checkout chỉ được tối đa 1 " + slotLabel + ".";
    }

    private CheckoutCouponDTO buildCouponDTO(Promotion promo,
                                             Integer ownerId,
                                             BigDecimal discountAmount,
                                             boolean valid,
                                             String message) {
        CheckoutCouponDTO dto = new CheckoutCouponDTO();
        dto.setPromoId(promo.getPromoId());
        dto.setCode(promo.getCode());
        dto.setDiscountScope(promo.getDiscountScope());
        dto.setBenefitTarget(promotionService.resolveBenefitTarget(promo));
        dto.setOwnerId(ownerId);
        dto.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        dto.setCanStack(promo.getCanStack());
        dto.setValid(valid);
        dto.setMessage(message);
        return dto;
    }

    private List<CheckoutCouponDTO> loadEligibleShopCoupons(int ownerId, BigDecimal subtotal) throws SQLException {
        List<CheckoutCouponDTO> coupons = new ArrayList<>();
        for (Promotion promo : promotionDAO.findShopActivePromotions(ownerId)) {
            if (!"ORDER".equalsIgnoreCase(promo.getScope())) {
                continue;
            }
            if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
                continue;
            }
            if (promo.getMinOrderValue() != null && promo.getMinOrderValue().compareTo(subtotal) > 0) {
                continue;
            }
            coupons.add(buildCouponDTO(promo, ownerId, BigDecimal.ZERO, true, null));
        }
        return coupons;
    }

    private List<CheckoutCouponDTO> loadEligibleSystemCoupons(BigDecimal subtotal) throws SQLException {
        List<CheckoutCouponDTO> coupons = new ArrayList<>();
        for (Promotion promo : promotionDAO.findActiveSystemPromotions()) {
            if (!"ORDER".equalsIgnoreCase(promo.getScope())) {
                continue;
            }
            if (promo.getMaxUses() != null && promo.getUsedCount() >= promo.getMaxUses()) {
                continue;
            }
            if (promo.getMinOrderValue() != null && promo.getMinOrderValue().compareTo(subtotal) > 0) {
                continue;
            }
            coupons.add(buildCouponDTO(promo, null, BigDecimal.ZERO, true, null));
        }
        return coupons;
    }

    private void finalizeQuote(Map<Integer, CheckoutShopSummaryDTO> summariesByOwner, CheckoutQuoteDTO quote) {
        BigDecimal shopDiscountAmount = BigDecimal.ZERO;
        BigDecimal systemDiscountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscountAmount = BigDecimal.ZERO;
        BigDecimal paymentDiscountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = BigDecimal.ZERO;
        for (CheckoutShopSummaryDTO summary : summariesByOwner.values()) {
            BigDecimal shippingDiscount = summary.getShopShippingDiscountAmount()
                    .add(summary.getSystemShippingDiscountAmount());
            BigDecimal merchandiseDiscount = summary.getShopMerchandiseDiscountAmount()
                    .add(summary.getSystemMerchandiseDiscountAmount());
            BigDecimal paymentDiscount = summary.getPaymentDiscountAmount();
            BigDecimal netMerchandise = summary.getSubtotal().subtract(merchandiseDiscount).max(BigDecimal.ZERO);
            BigDecimal netShipping = summary.getDeliveryFee().subtract(shippingDiscount).max(BigDecimal.ZERO);
            summary.setFinalAmount(netMerchandise.add(netShipping).subtract(paymentDiscount).max(BigDecimal.ZERO));
            finalAmount = finalAmount.add(summary.getFinalAmount());

            shopDiscountAmount = shopDiscountAmount
                    .add(summary.getShopMerchandiseDiscountAmount())
                    .add(summary.getShopShippingDiscountAmount());
            systemDiscountAmount = systemDiscountAmount
                    .add(summary.getSystemMerchandiseDiscountAmount())
                    .add(summary.getSystemShippingDiscountAmount());
            paymentDiscountAmount = paymentDiscountAmount.add(paymentDiscount);
            shippingDiscountAmount = shippingDiscountAmount.add(shippingDiscount);
        }

        quote.setShopDiscountAmount(shopDiscountAmount);
        quote.setSystemDiscountAmount(systemDiscountAmount.add(paymentDiscountAmount));
        quote.setShippingDiscountAmount(shippingDiscountAmount);
        quote.setPaymentDiscountAmount(paymentDiscountAmount);
        quote.setDiscountAmount(shopDiscountAmount.add(systemDiscountAmount).add(paymentDiscountAmount));
        quote.setFinalAmount(finalAmount.max(BigDecimal.ZERO));
        quote.setValid(quote.getErrors().isEmpty());
    }

    private List<CartItem> filterCheckoutItems(List<CartItem> items, List<Integer> variantIds) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống hoặc đơn hàng đang được xử lý.");
        }
        Set<Integer> selectedIds = new HashSet<>(variantIds);
        List<CartItem> checkoutItems = new ArrayList<>();
        for (CartItem item : items) {
            if (selectedIds.contains(item.getVariantId())) {
                checkoutItems.add(item);
            }
        }
        if (checkoutItems.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm nào để thanh toán.");
        }
        return checkoutItems;
    }

    private Map<Integer, ProductVariant> loadVariantMap(List<CartItem> checkoutItems) throws SQLException {
        List<Integer> variantIds = new ArrayList<>();
        for (CartItem item : checkoutItems) {
            variantIds.add(item.getVariantId());
        }
        return productVariantDAO.findByIds(variantIds);
    }

    private List<String> validateStock(List<CartItem> checkoutItems, Map<Integer, ProductVariant> variantMap) {
        List<String> stockErrors = new ArrayList<>();
        for (CartItem item : checkoutItems) {
            if (item.getQuantity() <= 0) {
                stockErrors.add("Số lượng sản phẩm " + item.getProductName() + " không hợp lệ.");
                continue;
            }
            ProductVariant variant = variantMap.get(item.getVariantId());
            if (variant == null || !variant.getIsActive()) {
                stockErrors.add(item.getProductName() + " đã ngừng kinh doanh hoặc hết hàng.");
            } else if (item.getQuantity() > variant.getStockQuantity()) {
                stockErrors.add(item.getProductName() + " (" + item.getVariantLabel() + ") đã hết số lượng bạn cần mua, hiện chỉ còn "
                        + variant.getStockQuantity() + " sản phẩm.");
            }
        }
        return stockErrors;
    }

    private Map<Integer, List<CartItem>> groupItemsByOwnerId(List<CartItem> checkoutItems,
                                                             Map<Integer, ProductVariant> variantMap) throws SQLException {
        Map<Integer, List<CartItem>> grouped = new LinkedHashMap<>();
        for (CartItem item : checkoutItems) {
            int ownerId = resolveOwnerId(item, variantMap);
            grouped.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(item);
        }
        return grouped;
    }

    private int resolveOwnerId(CartItem item, Map<Integer, ProductVariant> variantMap) throws SQLException {
        if (item.getShopId() > 0) {
            return item.getShopId();
        }
        ProductVariant variant = variantMap.get(item.getVariantId());
        if (variant == null) {
            throw new IllegalStateException("Không thể xác định shop của sản phẩm trong giỏ hàng.");
        }
        return productVariantDAO.getProductOwnerId(item.getVariantId());
    }

    private List<String> validateShopStatus(Map<Integer, List<CartItem>> itemsByOwner) {
        List<String> errors = new ArrayList<>();
        for (Integer ownerId : itemsByOwner.keySet()) {
            List<ShopProfile> profiles;
            try {
                profiles = shopProfileDAO.findByUserId(ownerId);
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi truy vấn ShopProfile", e);
            }
            ShopProfile profile = (profiles == null || profiles.isEmpty()) ? null : profiles.get(0);
            if (profile == null || !"APPROVED".equals(profile.getApprovalStatus())) {
                String shopName = profile != null && profile.getShopName() != null && !profile.getShopName().trim().isEmpty()
                        ? profile.getShopName().trim()
                        : "cửa hàng này";
                errors.add("Cửa hàng \"" + shopName + "\" đã bị đình chỉ hoạt động. Vui lòng xóa các sản phẩm của cửa hàng này khỏi giỏ hàng.");
            }
        }
        return errors;
    }

    private BigDecimal calculateSubtotal(List<CartItem> items, Map<Integer, ProductVariant> variantMap) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            ProductVariant variant = variantMap != null ? variantMap.get(item.getVariantId()) : null;
            BigDecimal price = variant != null ? variant.getActivePrice()
                    : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            BigDecimal packagingPriceAdd = item.getPackagingPriceAdd() != null
                    ? item.getPackagingPriceAdd() : BigDecimal.ZERO;
            subtotal = subtotal.add(price.add(packagingPriceAdd).multiply(new BigDecimal(item.getQuantity())));
        }
        return subtotal.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDirectSaleAmount(List<CartItem> items) {
        long accumulativeDiscount = 0;
        for (CartItem item : items) {
            BigDecimal basePrice = item.getBasePrice() != null ? item.getBasePrice()
                    : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            BigDecimal activePrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal unitDiscount = basePrice.subtract(activePrice).max(BigDecimal.ZERO);
            long unitDiscountValue = unitDiscount.setScale(0, RoundingMode.HALF_UP).longValue();
            accumulativeDiscount += unitDiscountValue * item.getQuantity();
        }
        return new BigDecimal(accumulativeDiscount);
    }

    private BigDecimal calculateTotalWeight(List<CartItem> items) {
        long accumulativeGrams = 0;
        for (CartItem item : items) {
            BigDecimal weight = item.getWeightKg() != null ? item.getWeightKg() : new BigDecimal("1.000");
            long weightGrams = weight.multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP).longValue();
            accumulativeGrams += weightGrams * item.getQuantity();
        }
        return new BigDecimal(accumulativeGrams).divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal sumSummarySubtotals(Collection<CheckoutShopSummaryDTO> summaries) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CheckoutShopSummaryDTO summary : summaries) {
            sum = sum.add(summary.getSubtotal());
        }
        return sum;
    }

    private BigDecimal sumValues(Map<Integer, BigDecimal> values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values.values()) {
            if (value != null) {
                sum = sum.add(value);
            }
        }
        return sum;
    }

    private Map<Integer, BigDecimal> allocateDiscount(BigDecimal discount, Map<Integer, BigDecimal> allocationBase) {
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal baseTotal = sumValues(allocationBase);
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0 || baseTotal.compareTo(BigDecimal.ZERO) <= 0) {
            for (Integer ownerId : allocationBase.keySet()) {
                result.put(ownerId, BigDecimal.ZERO);
            }
            return result;
        }

        BigDecimal remaining = discount;
        int index = 0;
        int size = allocationBase.size();
        for (Map.Entry<Integer, BigDecimal> entry : allocationBase.entrySet()) {
            index++;
            BigDecimal base = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;
            BigDecimal allocated;
            if (index == size) {
                allocated = remaining.min(base).max(BigDecimal.ZERO);
            } else {
                allocated = discount.multiply(base)
                        .divide(baseTotal, 0, RoundingMode.HALF_UP)
                        .min(base)
                        .max(BigDecimal.ZERO);
                remaining = remaining.subtract(allocated);
            }
            result.put(entry.getKey(), allocated.max(BigDecimal.ZERO));
        }
        return result;
    }

    private List<String> normalizeCouponCodes(List<String> rawCodes) {
        List<String> normalizedCodes = new ArrayList<>();
        if (rawCodes == null) {
            return normalizedCodes;
        }
        for (String rawCode : rawCodes) {
            if (rawCode == null) {
                continue;
            }
            String code = rawCode.trim().toUpperCase();
            if (!code.isEmpty() && !normalizedCodes.contains(code)) {
                normalizedCodes.add(code);
            }
        }
        return normalizedCodes;
    }

    private String resolveShopName(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        for (CartItem item : items) {
            if (item.getShopName() != null && !item.getShopName().trim().isEmpty()) {
                return item.getShopName().trim();
            }
        }
        return null;
    }

    private static final class ResolvedCoupons {
        private final Map<Integer, Promotion> shopMerchandisePromos = new LinkedHashMap<>();
        private final Map<Integer, Promotion> shopShippingPromos = new LinkedHashMap<>();
        private final List<Promotion> systemMerchandisePromos = new ArrayList<>();
        private final List<Promotion> systemShippingPromos = new ArrayList<>();
        private final List<Promotion> paymentPromos = new ArrayList<>();
        private final List<Promotion> shopPromotions = new ArrayList<>();
        private final List<Promotion> systemPromotions = new ArrayList<>();
    }

    public static final class PromotionAllocation {
        private final Promotion promo;
        private final Integer ownerId;
        private final BigDecimal discountAmount;

        public PromotionAllocation(Promotion promo, Integer ownerId, BigDecimal discountAmount) {
            this.promo = promo;
            this.ownerId = ownerId;
            this.discountAmount = discountAmount;
        }

        public Promotion getPromo() {
            return promo;
        }

        public Integer getOwnerId() {
            return ownerId;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }
    }

    public static final class CheckoutPricingSnapshot {
        private CartSummaryDTO cartSummary;
        private List<CartItem> checkoutItems = new ArrayList<>();
        private Map<Integer, ProductVariant> variantMap = new HashMap<>();
        private Map<Integer, List<CartItem>> itemsByOwner = new LinkedHashMap<>();
        private CheckoutQuoteDTO quote;
        private List<PromotionAllocation> promotionAllocations = new ArrayList<>();
        private Integer singleOwnerId;

        public CartSummaryDTO getCartSummary() {
            return cartSummary;
        }

        public void setCartSummary(CartSummaryDTO cartSummary) {
            this.cartSummary = cartSummary;
        }

        public List<CartItem> getCheckoutItems() {
            return checkoutItems;
        }

        public void setCheckoutItems(List<CartItem> checkoutItems) {
            this.checkoutItems = checkoutItems;
        }

        public Map<Integer, ProductVariant> getVariantMap() {
            return variantMap;
        }

        public void setVariantMap(Map<Integer, ProductVariant> variantMap) {
            this.variantMap = variantMap;
        }

        public Map<Integer, List<CartItem>> getItemsByOwner() {
            return itemsByOwner;
        }

        public void setItemsByOwner(Map<Integer, List<CartItem>> itemsByOwner) {
            this.itemsByOwner = itemsByOwner;
        }

        public CheckoutQuoteDTO getQuote() {
            return quote;
        }

        public void setQuote(CheckoutQuoteDTO quote) {
            this.quote = quote;
        }

        public List<PromotionAllocation> getPromotionAllocations() {
            return promotionAllocations;
        }

        public void setPromotionAllocations(List<PromotionAllocation> promotionAllocations) {
            this.promotionAllocations = promotionAllocations;
        }

        public Integer getSingleOwnerId() {
            return singleOwnerId;
        }

        public void setSingleOwnerId(Integer singleOwnerId) {
            this.singleOwnerId = singleOwnerId;
        }
    }
}
