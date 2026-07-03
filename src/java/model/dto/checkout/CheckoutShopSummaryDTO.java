package model.dto.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO tổng hợp định giá theo từng shop trong checkout multi-shop.
 */
// Touched for IDE re-indexing
public class CheckoutShopSummaryDTO {

    private int ownerId;
    private String shopName;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal automaticDiscountAmount = BigDecimal.ZERO;
    private BigDecimal shopMerchandiseDiscountAmount = BigDecimal.ZERO;
    private BigDecimal systemMerchandiseDiscountAmount = BigDecimal.ZERO;
    private BigDecimal shopShippingDiscountAmount = BigDecimal.ZERO;
    private BigDecimal systemShippingDiscountAmount = BigDecimal.ZERO;
    private BigDecimal paymentDiscountAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalAmount = BigDecimal.ZERO;
    private List<CheckoutCouponDTO> eligibleCoupons = new ArrayList<>();
    private List<CheckoutCouponDTO> appliedCoupons = new ArrayList<>();

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
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

    public BigDecimal getAutomaticDiscountAmount() {
        return automaticDiscountAmount;
    }

    public void setAutomaticDiscountAmount(BigDecimal automaticDiscountAmount) {
        this.automaticDiscountAmount = automaticDiscountAmount;
    }

    public BigDecimal getShopMerchandiseDiscountAmount() {
        return shopMerchandiseDiscountAmount;
    }

    public void setShopMerchandiseDiscountAmount(BigDecimal shopMerchandiseDiscountAmount) {
        this.shopMerchandiseDiscountAmount = shopMerchandiseDiscountAmount;
    }

    public BigDecimal getSystemMerchandiseDiscountAmount() {
        return systemMerchandiseDiscountAmount;
    }

    public void setSystemMerchandiseDiscountAmount(BigDecimal systemMerchandiseDiscountAmount) {
        this.systemMerchandiseDiscountAmount = systemMerchandiseDiscountAmount;
    }

    public BigDecimal getShopShippingDiscountAmount() {
        return shopShippingDiscountAmount;
    }

    public void setShopShippingDiscountAmount(BigDecimal shopShippingDiscountAmount) {
        this.shopShippingDiscountAmount = shopShippingDiscountAmount;
    }

    public BigDecimal getSystemShippingDiscountAmount() {
        return systemShippingDiscountAmount;
    }

    public void setSystemShippingDiscountAmount(BigDecimal systemShippingDiscountAmount) {
        this.systemShippingDiscountAmount = systemShippingDiscountAmount;
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

    public List<CheckoutCouponDTO> getEligibleCoupons() {
        return eligibleCoupons;
    }

    public void setEligibleCoupons(List<CheckoutCouponDTO> eligibleCoupons) {
        this.eligibleCoupons = eligibleCoupons;
    }

    public List<CheckoutCouponDTO> getAppliedCoupons() {
        return appliedCoupons;
    }

    public void setAppliedCoupons(List<CheckoutCouponDTO> appliedCoupons) {
        this.appliedCoupons = appliedCoupons;
    }
}
