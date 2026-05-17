package com.fruitmkt.model.entity;

/**
 * Promotion — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class Promotion {

    private int promoId;
    private String code;
    private String discountType;
    private String discountScope;
    private java.math.BigDecimal discountMax;
    private java.math.BigDecimal discountValue;
    private java.math.BigDecimal minOrderValue;
    private String scope;
    private Integer productId;
    private Integer maxUses;
    private int usedCount;
    private boolean canStack;
    private java.time.LocalDateTime validFrom;
    private java.time.LocalDateTime validUntil;
    private int createdBy;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private boolean isDeleted;
    private boolean isActive;

    public Promotion() {}

    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public String getDiscountScope() { return discountScope; }
    public void setDiscountScope(String discountScope) { this.discountScope = discountScope; }

    public java.math.BigDecimal getDiscountMax() { return discountMax; }
    public void setDiscountMax(java.math.BigDecimal discountMax) { this.discountMax = discountMax; }

    public java.math.BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(java.math.BigDecimal discountValue) { this.discountValue = discountValue; }

    public java.math.BigDecimal getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(java.math.BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public boolean getCanStack() { return canStack; }
    public void setCanStack(boolean canStack) { this.canStack = canStack; }

    public java.time.LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(java.time.LocalDateTime validFrom) { this.validFrom = validFrom; }

    public java.time.LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(java.time.LocalDateTime validUntil) { this.validUntil = validUntil; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

}
