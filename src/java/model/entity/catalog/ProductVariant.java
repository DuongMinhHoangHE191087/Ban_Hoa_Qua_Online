package model.entity.catalog;

/**
 * ProductVariant — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ProductVariant {

    private int variantId;
    private int productId;
    private String sku;
    private String variantLabel;
    private java.math.BigDecimal price;
    private int stockQuantity;
    private boolean isActive;
    private java.math.BigDecimal weightKg;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    private java.math.BigDecimal discountPrice;
    private java.time.LocalDateTime discountStart;
    private java.time.LocalDateTime discountEnd;

    public ProductVariant() {}

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }

    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public java.math.BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(java.math.BigDecimal weightKg) { this.weightKg = weightKg; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public java.math.BigDecimal getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(java.math.BigDecimal discountPrice) { this.discountPrice = discountPrice; }

    public java.time.LocalDateTime getDiscountStart() { return discountStart; }
    public void setDiscountStart(java.time.LocalDateTime discountStart) { this.discountStart = discountStart; }

    public java.time.LocalDateTime getDiscountEnd() { return discountEnd; }
    public void setDiscountEnd(java.time.LocalDateTime discountEnd) { this.discountEnd = discountEnd; }

    public java.math.BigDecimal getActivePrice() {
        if (discountPrice != null && discountStart != null && discountEnd != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if ((now.isAfter(discountStart) || now.isEqual(discountStart)) && 
                (now.isBefore(discountEnd) || now.isEqual(discountEnd))) {
                return discountPrice;
            }
        }
        return price;
    }

    public boolean getIsDiscounted() {
        if (discountPrice != null && discountStart != null && discountEnd != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            return (now.isAfter(discountStart) || now.isEqual(discountStart)) && 
                   (now.isBefore(discountEnd) || now.isEqual(discountEnd));
        }
        return false;
    }
}
