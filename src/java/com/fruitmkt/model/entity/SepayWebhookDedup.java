package com.fruitmkt.model.entity;

/**
 * SepayWebhookDedup — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class SepayWebhookDedup {

    private int dedupId;
    private String sepayTransactionId;
    private String orderCode;
    private String processResult;
    private java.time.LocalDateTime createdAt;

    public SepayWebhookDedup() {}

    public int getDedupId() { return dedupId; }
    public void setDedupId(int dedupId) { this.dedupId = dedupId; }

    public String getSepayTransactionId() { return sepayTransactionId; }
    public void setSepayTransactionId(String sepayTransactionId) { this.sepayTransactionId = sepayTransactionId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getProcessResult() { return processResult; }
    public void setProcessResult(String processResult) { this.processResult = processResult; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

}
