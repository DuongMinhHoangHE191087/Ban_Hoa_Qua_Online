package model.entity.catalog;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryBatch {
    private int batchId;
    private int variantId;
    private int initialQuantity;
    private int remainingQuantity;
    private LocalDate expiresAt;
    private boolean isExpired;
    private LocalDateTime createdAt;

    // Constructors
    public InventoryBatch() {
    }

    public InventoryBatch(int batchId, int variantId, int initialQuantity, int remainingQuantity, 
                          LocalDate expiresAt, boolean isExpired, LocalDateTime createdAt) {
        this.batchId = batchId;
        this.variantId = variantId;
        this.initialQuantity = initialQuantity;
        this.remainingQuantity = remainingQuantity;
        this.expiresAt = expiresAt;
        this.isExpired = isExpired;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedExpiresAt() {
        if (expiresAt == null) return "";
        return expiresAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public boolean isExpiringSoon() {
        if (expiresAt == null || isExpired) return false;
        java.time.LocalDate now = java.time.LocalDate.now();
        return !expiresAt.isBefore(now) && expiresAt.isBefore(now.plusDays(7));
    }
}
