package com.fruitmkt.model.entity;

/**
 * Order — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class Order {

    private int orderId;
    private int customerId;
    private int ownerId;
    private String deliveryAddress;
    private String userAddress;
    private String deliveryTimeSlot;
    private String notes;
    private java.time.LocalDateTime cancelledAt;
    private Integer cancelledBy;
    private String cancellationReason;
    private String status;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal deliveryFee;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal systemDiscountAmount;
    private java.math.BigDecimal shopDiscountAmount;
    private java.math.BigDecimal platformFee;
    private java.math.BigDecimal finalAmount;
    private String paymentMethod;
    private String refundStatus;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public Order() {}

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public String getDeliveryTimeSlot() { return deliveryTimeSlot; }
    public void setDeliveryTimeSlot(String deliveryTimeSlot) { this.deliveryTimeSlot = deliveryTimeSlot; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public java.time.LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(java.time.LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public Integer getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Integer cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public java.math.BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(java.math.BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public java.math.BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(java.math.BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public java.math.BigDecimal getSystemDiscountAmount() { return systemDiscountAmount; }
    public void setSystemDiscountAmount(java.math.BigDecimal systemDiscountAmount) { this.systemDiscountAmount = systemDiscountAmount; }

    public java.math.BigDecimal getShopDiscountAmount() { return shopDiscountAmount; }
    public void setShopDiscountAmount(java.math.BigDecimal shopDiscountAmount) { this.shopDiscountAmount = shopDiscountAmount; }

    public java.math.BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(java.math.BigDecimal platformFee) { this.platformFee = platformFee; }

    public java.math.BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(java.math.BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public java.util.Date getCreatedAtAsDate() {
        return createdAt != null ? java.util.Date.from(createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

}
