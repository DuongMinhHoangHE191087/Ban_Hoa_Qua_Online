package model.dto.checkout;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO request cho API quote checkout.
 */
// Touched for IDE re-indexing
public class CheckoutQuoteRequestDTO {

    private List<Integer> cartItemIds = new ArrayList<>();
    private List<Integer> variantIds = new ArrayList<>();
    private String deliveryAddress;
    private String deliveryTimeSlot;
    private String paymentMethod;
    private List<String> shopCouponCodes = new ArrayList<>();
    private List<String> systemCouponCodes = new ArrayList<>();

    public List<Integer> getCartItemIds() {
        return cartItemIds;
    }

    public void setCartItemIds(List<Integer> cartItemIds) {
        this.cartItemIds = cartItemIds;
    }

    public List<Integer> getVariantIds() {
        return variantIds;
    }

    public void setVariantIds(List<Integer> variantIds) {
        this.variantIds = variantIds;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryTimeSlot() {
        return deliveryTimeSlot;
    }

    public void setDeliveryTimeSlot(String deliveryTimeSlot) {
        this.deliveryTimeSlot = deliveryTimeSlot;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<String> getShopCouponCodes() {
        return shopCouponCodes;
    }

    public void setShopCouponCodes(List<String> shopCouponCodes) {
        this.shopCouponCodes = shopCouponCodes;
    }

    public List<String> getSystemCouponCodes() {
        return systemCouponCodes;
    }

    public void setSystemCouponCodes(List<String> systemCouponCodes) {
        this.systemCouponCodes = systemCouponCodes;
    }
}
