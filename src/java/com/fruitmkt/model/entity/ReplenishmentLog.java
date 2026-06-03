package com.fruitmkt.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReplenishmentLog — Entity class mapping to the replenishment_logs table.
 * Used for Restock Management by Shop Owners.
 */
public class ReplenishmentLog {

    private int logId;
    private int variantId;
    private int replenishedBy;
    private int quantity;
    private String supplierDetails;
    private LocalDate replenishmentDate;
    private LocalDateTime createdAt;

    // Additional helper fields for UI display (not columns in replenishment_logs)
    private String productName;
    private String variantLabel;
    private String replenishedByName;

    public ReplenishmentLog() {}

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getReplenishedBy() {
        return replenishedBy;
    }

    public void setReplenishedBy(int replenishedBy) {
        this.replenishedBy = replenishedBy;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplierDetails() {
        return supplierDetails;
    }

    public void setSupplierDetails(String supplierDetails) {
        this.supplierDetails = supplierDetails;
    }

    public LocalDate getReplenishmentDate() {
        return replenishmentDate;
    }

    public void setReplenishmentDate(LocalDate replenishmentDate) {
        this.replenishmentDate = replenishmentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantLabel() {
        return variantLabel;
    }

    public void setVariantLabel(String variantLabel) {
        this.variantLabel = variantLabel;
    }

    public String getReplenishedByName() {
        return replenishedByName;
    }

    public void setReplenishedByName(String replenishedByName) {
        this.replenishedByName = replenishedByName;
    }
}
