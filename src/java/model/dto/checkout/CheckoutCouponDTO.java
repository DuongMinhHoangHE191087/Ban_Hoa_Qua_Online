package model.dto.checkout;

import java.math.BigDecimal;

/**
 * DTO mô tả voucher/coupon trong quote checkout.
 */
// Touched for IDE re-indexing
public class CheckoutCouponDTO {

    private int promoId;
    private String code;
    private String discountScope;
    private String benefitTarget;
    private Integer ownerId;
    private BigDecimal discountAmount;
    private boolean canStack;
    private boolean valid = true;
    private String message;

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountScope() {
        return discountScope;
    }

    public void setDiscountScope(String discountScope) {
        this.discountScope = discountScope;
    }

    public String getBenefitTarget() {
        return benefitTarget;
    }

    public void setBenefitTarget(String benefitTarget) {
        this.benefitTarget = benefitTarget;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public boolean getCanStack() {
        return canStack;
    }

    public void setCanStack(boolean canStack) {
        this.canStack = canStack;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
