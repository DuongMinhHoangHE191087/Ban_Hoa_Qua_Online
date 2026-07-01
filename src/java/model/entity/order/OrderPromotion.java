package model.entity.order;

/**
 * OrderPromotion — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class OrderPromotion {

    private int usageId;
    private int orderId;
    private int promoId;
    private int customerId;
    private java.math.BigDecimal discountApplied;
    private String couponCode;
    private String discountScope;
    private String benefitTarget;
    private java.time.LocalDateTime usedAt;

    public OrderPromotion() {}

    public int getUsageId() { return usageId; }
    public void setUsageId(int usageId) { this.usageId = usageId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public java.math.BigDecimal getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(java.math.BigDecimal discountApplied) { this.discountApplied = discountApplied; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getDiscountScope() { return discountScope; }
    public void setDiscountScope(String discountScope) { this.discountScope = discountScope; }

    public String getBenefitTarget() { return benefitTarget; }
    public void setBenefitTarget(String benefitTarget) { this.benefitTarget = benefitTarget; }

    public java.time.LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(java.time.LocalDateTime usedAt) { this.usedAt = usedAt; }

}
