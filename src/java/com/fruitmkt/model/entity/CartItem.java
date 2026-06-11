package com.fruitmkt.model.entity;

/**
 * CartItem — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class CartItem {

    private int cartItemId;
    private int cartId;
    private int variantId;
    private int quantity;
    private java.time.LocalDateTime addedAt;
    
    // Transient fields for display
    private String productName;
    private String variantLabel;
    private java.math.BigDecimal price;
    private java.math.BigDecimal basePrice;
    private java.math.BigDecimal weightKg;
    private String imagePath;
    private int stockQuantity;
    private int productId;

    // Packaging option selection properties
    private Integer packagingId;
    private String packagingLabel;
    private java.math.BigDecimal packagingPriceAdd;

    public java.math.BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(java.math.BigDecimal basePrice) { this.basePrice = basePrice; }

    public CartItem() {}

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getVariantId() { return variantId; }
    public void setVariantId(int variantId) { this.variantId = variantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public java.time.LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(java.time.LocalDateTime addedAt) { this.addedAt = addedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }

    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }

    public java.math.BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(java.math.BigDecimal weightKg) { this.weightKg = weightKg; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public Integer getPackagingId() { return packagingId; }
    public void setPackagingId(Integer packagingId) { this.packagingId = packagingId; }

    public String getPackagingLabel() { return packagingLabel; }
    public void setPackagingLabel(String packagingLabel) { this.packagingLabel = packagingLabel; }

    public java.math.BigDecimal getPackagingPriceAdd() { return packagingPriceAdd; }
    public void setPackagingPriceAdd(java.math.BigDecimal packagingPriceAdd) { this.packagingPriceAdd = packagingPriceAdd; }
}
