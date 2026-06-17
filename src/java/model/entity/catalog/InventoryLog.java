package model.entity.catalog;

/**
 * InventoryLog — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class InventoryLog {

    private int logId;
    private int variantId;
    private int changedBy;
    private String changeType;
    private int quantityDelta;
    private int quantityAfter;
    private String note;
    private java.time.LocalDateTime changedAt;
    private java.time.LocalDate expiresAt;
    private boolean expired;

    // Helper fields for UI display
    private String productName;
    private String variantLabel;
    private String changedByName;

    public InventoryLog() {}

    public java.time.LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(java.time.LocalDate expiresAt) { this.expiresAt = expiresAt; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public int getQuantityDelta() { return quantityDelta; }
    public void setQuantityDelta(int quantityDelta) { this.quantityDelta = quantityDelta; }

    public int getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(int quantityAfter) { this.quantityAfter = quantityAfter; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public java.time.LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(java.time.LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }

    public String getFormattedChangedAt() {
        if (changedAt == null) return "";
        return changedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

}
