package com.fruitmkt.model.entity;

/**
 * Delivery — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class Delivery {

    private int deliveryId;
    private int orderId;
    private Integer staffId;
    private String status;
    private java.time.LocalDateTime pickedUpAt;
    private java.time.LocalDateTime deliveredAt;
    private String failureReason;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String proofImageUrl;
    private java.time.LocalDateTime estimatedDeliveryTime;

    public Delivery() {}

    public int getDeliveryId() { return deliveryId; }
    public void setDeliveryId(int deliveryId) { this.deliveryId = deliveryId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(java.time.LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public java.time.LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(java.time.LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }

    public java.time.LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(java.time.LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

}
