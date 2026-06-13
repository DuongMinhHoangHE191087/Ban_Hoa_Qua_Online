package com.fruitmkt.model.dto;

/**
 * Ket qua sau khi dat don checkout.
 */
public class CheckoutResultDTO {

    private int orderId;
    private boolean paymentRequired;
    private String purgedVariantIds;
    private String successMessage;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public boolean isPaymentRequired() {
        return paymentRequired;
    }

    public void setPaymentRequired(boolean paymentRequired) {
        this.paymentRequired = paymentRequired;
    }

    public String getPurgedVariantIds() {
        return purgedVariantIds;
    }

    public void setPurgedVariantIds(String purgedVariantIds) {
        this.purgedVariantIds = purgedVariantIds;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
