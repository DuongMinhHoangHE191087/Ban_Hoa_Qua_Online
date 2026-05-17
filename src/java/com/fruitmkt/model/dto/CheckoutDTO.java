package com.fruitmkt.model.dto;

/**
 * /** DTO từ form checkout — nhận từ request POST. */
 * @author fruitmkt-team
 */
public class CheckoutDTO {

        private String deliveryAddress;
        private String deliveryTimeSlot;
        private String notes;
        private String paymentMethod;   // CK | COD
        private String voucherCode;     // Có thể null
        private java.util.List<Integer> cartItemIds; // Items được checkout

    // TODO: Thêm constructor, getters, setters theo fields bên trên
    public CheckoutDTO() {}
}
