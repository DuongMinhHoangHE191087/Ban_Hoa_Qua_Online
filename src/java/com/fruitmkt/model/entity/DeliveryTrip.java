package com.fruitmkt.model.entity;

public class DeliveryTrip {

    private int tripId;
    private int parentOrderId;
    private Integer shipperId;
    private String status;
    private java.time.LocalDateTime estimatedStartTime;
    private java.time.LocalDateTime estimatedEndTime;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getParentOrderId() { return parentOrderId; }
    public void setParentOrderId(int parentOrderId) { this.parentOrderId = parentOrderId; }

    public Integer getShipperId() { return shipperId; }
    public void setShipperId(Integer shipperId) { this.shipperId = shipperId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getEstimatedStartTime() { return estimatedStartTime; }
    public void setEstimatedStartTime(java.time.LocalDateTime estimatedStartTime) { this.estimatedStartTime = estimatedStartTime; }

    public java.time.LocalDateTime getEstimatedEndTime() { return estimatedEndTime; }
    public void setEstimatedEndTime(java.time.LocalDateTime estimatedEndTime) { this.estimatedEndTime = estimatedEndTime; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
