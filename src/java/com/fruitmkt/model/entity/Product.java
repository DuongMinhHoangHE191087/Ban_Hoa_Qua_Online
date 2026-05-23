package com.fruitmkt.model.entity;

/**
 * Product — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class Product {

    private int productId;
    private int ownerId;
    private int categoryId;
    private String name;
    private String description;
    private String originCountry;
    private String originRegion;
    private java.time.LocalDate harvestDate;
    private Integer shelfLifeDays;
    private String storageInstruction;
    private String status;
    private int viewCount;
    private java.math.BigDecimal rating;
    private int soldQuantity;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public Product() {}

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public String getOriginRegion() { return originRegion; }
    public void setOriginRegion(String originRegion) { this.originRegion = originRegion; }

    public java.time.LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(java.time.LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public String getFormattedHarvestDate() {
        if (harvestDate == null) return null;
        return harvestDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }

    public String getStorageInstruction() { return storageInstruction; }
    public void setStorageInstruction(String storageInstruction) { this.storageInstruction = storageInstruction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public java.math.BigDecimal getRating() { return rating; }
    public void setRating(java.math.BigDecimal rating) { this.rating = rating; }

    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}
