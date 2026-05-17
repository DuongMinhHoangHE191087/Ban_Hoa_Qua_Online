package com.fruitmkt.model.entity;

/**
 * ShopSettlement — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ShopSettlement {

    private int settlementId;
    private int ownerId;
    private java.time.LocalDate periodStart;
    private java.time.LocalDate periodEnd;
    private java.math.BigDecimal grossAmount;
    private java.math.BigDecimal platformFeeAmount;
    private java.math.BigDecimal refundAmount;
    private java.math.BigDecimal adjustmentAmount;
    private java.math.BigDecimal netAmount;
    private String status;
    private java.time.LocalDateTime calculatedAt;
    private java.time.LocalDateTime confirmedAt;
    private java.time.LocalDateTime paidAt;
    private int createdBy;
    private String note;

    public ShopSettlement() {}

    public int getSettlementId() { return settlementId; }
    public void setSettlementId(int settlementId) { this.settlementId = settlementId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public java.time.LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(java.time.LocalDate periodStart) { this.periodStart = periodStart; }

    public java.time.LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(java.time.LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public java.math.BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(java.math.BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public java.math.BigDecimal getPlatformFeeAmount() { return platformFeeAmount; }
    public void setPlatformFeeAmount(java.math.BigDecimal platformFeeAmount) { this.platformFeeAmount = platformFeeAmount; }

    public java.math.BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public java.math.BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
    public void setAdjustmentAmount(java.math.BigDecimal adjustmentAmount) { this.adjustmentAmount = adjustmentAmount; }

    public java.math.BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(java.math.BigDecimal netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(java.time.LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public java.time.LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(java.time.LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public java.time.LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(java.time.LocalDateTime paidAt) { this.paidAt = paidAt; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

}
