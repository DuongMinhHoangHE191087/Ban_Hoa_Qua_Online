package model.dto.checkout;

import java.util.List;

/**
 * DTO từ form checkout — nhận từ request POST.
 * 
 * @author fruitmkt-team
 */
public class CheckoutDTO {

    private String deliveryAddress;
    private String deliveryTimeSlot;
    private String notes;
    private String paymentMethod;   // CK | COD
    private String voucherCode;     // Có thể null
    private List<Integer> cartItemIds; // Items được checkout

    public CheckoutDTO() {}

    public CheckoutDTO(String deliveryAddress, String deliveryTimeSlot, String notes, String paymentMethod, String voucherCode, List<Integer> cartItemIds) {
        this.deliveryAddress = deliveryAddress;
        this.deliveryTimeSlot = deliveryTimeSlot;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.voucherCode = voucherCode;
        this.cartItemIds = cartItemIds;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public List<Integer> getCartItemIds() {
        return cartItemIds;
    }

    public void setCartItemIds(List<Integer> cartItemIds) {
        this.cartItemIds = cartItemIds;
    }
}
