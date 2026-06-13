package com.fruitmkt.model.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO cho luong checkout thuc te.
 */
public class CheckoutRequestDTO {

    private String fullName;
    private String phone;
    private String deliveryAddress;
    private String deliveryTimeSlot;
    private String notes;
    private String paymentMethod;
    private String shopCouponCode;
    private String systemCouponCode;
    private List<Integer> variantIds = new ArrayList<>();
    private boolean saveAddressToBook;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getShopCouponCode() {
        return shopCouponCode;
    }

    public void setShopCouponCode(String shopCouponCode) {
        this.shopCouponCode = shopCouponCode;
    }

    public String getSystemCouponCode() {
        return systemCouponCode;
    }

    public void setSystemCouponCode(String systemCouponCode) {
        this.systemCouponCode = systemCouponCode;
    }

    public List<Integer> getVariantIds() {
        return variantIds;
    }

    public void setVariantIds(List<Integer> variantIds) {
        this.variantIds = variantIds;
    }

    public boolean isSaveAddressToBook() {
        return saveAddressToBook;
    }

    public void setSaveAddressToBook(boolean saveAddressToBook) {
        this.saveAddressToBook = saveAddressToBook;
    }
}
