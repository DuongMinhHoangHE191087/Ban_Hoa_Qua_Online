package model.entity.shop;

/**
 * ShopSettlement — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ShopSettlement {

    private int settlementId;
    private int ownerId;
    private String shopName;
    private String ownerName;
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
    private Integer confirmedBy;
    private String confirmNote;
    private java.time.LocalDateTime cancelledAt;
    private Integer cancelledBy;
    private String cancelReason;
    private java.time.LocalDateTime paidAt;
    private Integer paidBy;
    private String paidReference;
    private String paidNote;
    private String paymentIssueStatus;
    private java.time.LocalDateTime paymentIssueAt;
    private Integer paymentIssueBy;
    private String paymentIssueNote;
    private java.time.LocalDateTime paymentIssueResolvedAt;
    private Integer paymentIssueResolvedBy;
    private String paymentIssueResolutionNote;
    private int createdBy;
    private String note;

    public ShopSettlement() {}

    public int getSettlementId() { return settlementId; }
    public void setSettlementId(int settlementId) { this.settlementId = settlementId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

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

    public Integer getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(Integer confirmedBy) { this.confirmedBy = confirmedBy; }

    public String getConfirmNote() { return confirmNote; }
    public void setConfirmNote(String confirmNote) { this.confirmNote = confirmNote; }

    public java.time.LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(java.time.LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public Integer getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Integer cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public java.time.LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(java.time.LocalDateTime paidAt) { this.paidAt = paidAt; }

    public Integer getPaidBy() { return paidBy; }
    public void setPaidBy(Integer paidBy) { this.paidBy = paidBy; }

    public String getPaidReference() { return paidReference; }
    public void setPaidReference(String paidReference) { this.paidReference = paidReference; }

    public String getPaidNote() { return paidNote; }
    public void setPaidNote(String paidNote) { this.paidNote = paidNote; }

    public String getPaymentIssueStatus() { return paymentIssueStatus; }
    public void setPaymentIssueStatus(String paymentIssueStatus) { this.paymentIssueStatus = paymentIssueStatus; }

    public java.time.LocalDateTime getPaymentIssueAt() { return paymentIssueAt; }
    public void setPaymentIssueAt(java.time.LocalDateTime paymentIssueAt) { this.paymentIssueAt = paymentIssueAt; }

    public Integer getPaymentIssueBy() { return paymentIssueBy; }
    public void setPaymentIssueBy(Integer paymentIssueBy) { this.paymentIssueBy = paymentIssueBy; }

    public String getPaymentIssueNote() { return paymentIssueNote; }
    public void setPaymentIssueNote(String paymentIssueNote) { this.paymentIssueNote = paymentIssueNote; }

    public java.time.LocalDateTime getPaymentIssueResolvedAt() { return paymentIssueResolvedAt; }
    public void setPaymentIssueResolvedAt(java.time.LocalDateTime paymentIssueResolvedAt) { this.paymentIssueResolvedAt = paymentIssueResolvedAt; }

    public Integer getPaymentIssueResolvedBy() { return paymentIssueResolvedBy; }
    public void setPaymentIssueResolvedBy(Integer paymentIssueResolvedBy) { this.paymentIssueResolvedBy = paymentIssueResolvedBy; }

    public String getPaymentIssueResolutionNote() { return paymentIssueResolutionNote; }
    public void setPaymentIssueResolutionNote(String paymentIssueResolutionNote) { this.paymentIssueResolutionNote = paymentIssueResolutionNote; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public java.util.Date getPeriodStartAsDate() {
        return periodStart != null ? java.util.Date.from(periodStart.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getPeriodEndAsDate() {
        return periodEnd != null ? java.util.Date.from(periodEnd.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getSettledAtAsDate() {
        return paidAt != null ? java.util.Date.from(paidAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getConfirmedAtAsDate() {
        return confirmedAt != null ? java.util.Date.from(confirmedAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getCancelledAtAsDate() {
        return cancelledAt != null ? java.util.Date.from(cancelledAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getPaymentIssueAtAsDate() {
        return paymentIssueAt != null ? java.util.Date.from(paymentIssueAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public java.util.Date getPaymentIssueResolvedAtAsDate() {
        return paymentIssueResolvedAt != null ? java.util.Date.from(paymentIssueResolvedAt.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

}
