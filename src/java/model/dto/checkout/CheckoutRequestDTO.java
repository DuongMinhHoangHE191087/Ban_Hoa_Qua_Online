package model.dto.checkout;

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
    private List<String> shopCouponCodes = new ArrayList<>();
    private List<String> systemCouponCodes = new ArrayList<>();
    private List<Integer> cartItemIds = new ArrayList<>();
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
        if (shopCouponCodes == null || shopCouponCodes.isEmpty()) {
            this.shopCouponCodes = normalizeCouponCodes(parseCouponCodes(shopCouponCode));
        }
    }

    public String getSystemCouponCode() {
        return systemCouponCode;
    }

    public void setSystemCouponCode(String systemCouponCode) {
        this.systemCouponCode = systemCouponCode;
        if (systemCouponCodes == null || systemCouponCodes.isEmpty()) {
            this.systemCouponCodes = normalizeCouponCodes(parseCouponCodes(systemCouponCode));
        }
    }

    public List<String> getShopCouponCodes() {
        if ((shopCouponCodes == null || shopCouponCodes.isEmpty()) && shopCouponCode != null) {
            shopCouponCodes = normalizeCouponCodes(parseCouponCodes(shopCouponCode));
        }
        return shopCouponCodes;
    }

    public void setShopCouponCodes(List<String> shopCouponCodes) {
        this.shopCouponCodes = normalizeCouponCodes(shopCouponCodes);
    }

    public List<String> getSystemCouponCodes() {
        if ((systemCouponCodes == null || systemCouponCodes.isEmpty()) && systemCouponCode != null) {
            systemCouponCodes = normalizeCouponCodes(parseCouponCodes(systemCouponCode));
        }
        return systemCouponCodes;
    }

    public void setSystemCouponCodes(List<String> systemCouponCodes) {
        this.systemCouponCodes = normalizeCouponCodes(systemCouponCodes);
    }

    public List<Integer> getVariantIds() {
        return variantIds;
    }

    public void setVariantIds(List<Integer> variantIds) {
        this.variantIds = variantIds;
    }

    public List<Integer> getCartItemIds() {
        return cartItemIds;
    }

    public void setCartItemIds(List<Integer> cartItemIds) {
        this.cartItemIds = cartItemIds;
    }

    public boolean isSaveAddressToBook() {
        return saveAddressToBook;
    }

    public void setSaveAddressToBook(boolean saveAddressToBook) {
        this.saveAddressToBook = saveAddressToBook;
    }

    private List<String> parseCouponCodes(String rawCodes) {
        List<String> result = new ArrayList<>();
        if (rawCodes == null || rawCodes.trim().isEmpty()) {
            return result;
        }
        String[] parts = rawCodes.split(",");
        for (String part : parts) {
            String normalized = normalizeCouponCode(part);
            if (normalized != null && !result.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private List<String> normalizeCouponCodes(List<String> codes) {
        List<String> normalized = new ArrayList<>();
        if (codes == null) {
            return normalized;
        }
        for (String code : codes) {
            String normalizedCode = normalizeCouponCode(code);
            if (normalizedCode != null && !normalized.contains(normalizedCode)) {
                normalized.add(normalizedCode);
            }
        }
        return normalized;
    }

    private String normalizeCouponCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }
}
