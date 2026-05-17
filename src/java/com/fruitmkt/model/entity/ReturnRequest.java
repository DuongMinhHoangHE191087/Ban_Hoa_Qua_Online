package com.fruitmkt.model.entity;

/**
 * ReturnRequest — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ReturnRequest {

    private int returnRequestId;
    private int orderId;
    private Integer orderItemId;
    private int customerId;
    private String requestType;
    private String reasonCode;
    private String description;
    private String evidenceUrl;
    private int requestedQuantity;
    private String resolutionType;
    private Integer replacementVariantId;
    private java.math.BigDecimal refundAmount;
    private String status;
    private Integer decidedBy;
    private String decisionReason;
    private java.time.LocalDateTime resolvedAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public ReturnRequest() {}

    public int getReturnRequestId() { return returnRequestId; }
    public void setReturnRequestId(int returnRequestId) { this.returnRequestId = returnRequestId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public String getResolutionType() { return resolutionType; }
    public void setResolutionType(String resolutionType) { this.resolutionType = resolutionType; }

    public Integer getReplacementVariantId() { return replacementVariantId; }
    public void setReplacementVariantId(Integer replacementVariantId) { this.replacementVariantId = replacementVariantId; }

    public java.math.BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDecidedBy() { return decidedBy; }
    public void setDecidedBy(Integer decidedBy) { this.decidedBy = decidedBy; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public java.time.LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(java.time.LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}
