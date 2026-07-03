package model.dto.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO quote giá checkout chuẩn dùng cho preview và place-order.
 */
// Touched for IDE re-indexing
public class CheckoutQuoteDTO {

    private boolean valid = true;
    private int shopCount;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal directSaleAmount = BigDecimal.ZERO;
    private BigDecimal shopDiscountAmount = BigDecimal.ZERO;
    private BigDecimal systemDiscountAmount = BigDecimal.ZERO;
    private BigDecimal shippingDiscountAmount = BigDecimal.ZERO;
    private BigDecimal paymentDiscountAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalAmount = BigDecimal.ZERO;
    private List<CheckoutShopSummaryDTO> shopSummaries = new ArrayList<>();
    private List<CheckoutCouponDTO> appliedCoupons = new ArrayList<>();
    private List<CheckoutCouponDTO> invalidCoupons = new ArrayList<>();
    private List<CheckoutCouponDTO> eligibleSystemCoupons = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getShopCount() {
        return shopCount;
    }

    public void setShopCount(int shopCount) {
        this.shopCount = shopCount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getDirectSaleAmount() {
        return directSaleAmount;
    }

    public void setDirectSaleAmount(BigDecimal directSaleAmount) {
        this.directSaleAmount = directSaleAmount;
    }

    public BigDecimal getShopDiscountAmount() {
        return shopDiscountAmount;
    }

    public void setShopDiscountAmount(BigDecimal shopDiscountAmount) {
        this.shopDiscountAmount = shopDiscountAmount;
    }

    public BigDecimal getSystemDiscountAmount() {
        return systemDiscountAmount;
    }

    public void setSystemDiscountAmount(BigDecimal systemDiscountAmount) {
        this.systemDiscountAmount = systemDiscountAmount;
    }

    public BigDecimal getShippingDiscountAmount() {
        return shippingDiscountAmount;
    }

    public void setShippingDiscountAmount(BigDecimal shippingDiscountAmount) {
        this.shippingDiscountAmount = shippingDiscountAmount;
    }

    public BigDecimal getPaymentDiscountAmount() {
        return paymentDiscountAmount;
    }

    public void setPaymentDiscountAmount(BigDecimal paymentDiscountAmount) {
        this.paymentDiscountAmount = paymentDiscountAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public List<CheckoutShopSummaryDTO> getShopSummaries() {
        return shopSummaries;
    }

    public void setShopSummaries(List<CheckoutShopSummaryDTO> shopSummaries) {
        this.shopSummaries = shopSummaries;
    }

    public List<CheckoutCouponDTO> getAppliedCoupons() {
        return appliedCoupons;
    }

    public void setAppliedCoupons(List<CheckoutCouponDTO> appliedCoupons) {
        this.appliedCoupons = appliedCoupons;
    }

    public List<CheckoutCouponDTO> getInvalidCoupons() {
        return invalidCoupons;
    }

    public void setInvalidCoupons(List<CheckoutCouponDTO> invalidCoupons) {
        this.invalidCoupons = invalidCoupons;
    }

    public List<CheckoutCouponDTO> getEligibleSystemCoupons() {
        return eligibleSystemCoupons;
    }

    public void setEligibleSystemCoupons(List<CheckoutCouponDTO> eligibleSystemCoupons) {
        this.eligibleSystemCoupons = eligibleSystemCoupons;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
