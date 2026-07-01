package model.entity.catalog;

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

    private boolean isOrganic;
    private boolean isImported;
    private Integer seasonStartMonth;
    private Integer seasonEndMonth;

    private String approvalStatus;
    private String verificationDocPath;
    private String rejectionReason;

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

    public boolean isExpired() {
        if (harvestDate == null || shelfLifeDays == null || shelfLifeDays <= 0) {
            return false;
        }
        return harvestDate.plusDays(shelfLifeDays).isBefore(java.time.LocalDate.now()) ||
               harvestDate.plusDays(shelfLifeDays).isEqual(java.time.LocalDate.now());
     }
 
    public boolean getIsOrganic() { return isOrganic; }
    public void setIsOrganic(boolean isOrganic) { this.isOrganic = isOrganic; }

    public boolean getIsImported() { return isImported; }
    public void setIsImported(boolean isImported) { this.isImported = isImported; }

    public Integer getSeasonStartMonth() { return seasonStartMonth; }
    public void setSeasonStartMonth(Integer seasonStartMonth) { this.seasonStartMonth = seasonStartMonth; }

    public Integer getSeasonEndMonth() { return seasonEndMonth; }
    public void setSeasonEndMonth(Integer seasonEndMonth) { this.seasonEndMonth = seasonEndMonth; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getVerificationDocPath() { return verificationDocPath; }
    public void setVerificationDocPath(String verificationDocPath) { this.verificationDocPath = verificationDocPath; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    /**
     * Trả về nhãn mùa vụ dạng "Tháng 11 – Tháng 1 (qua năm mới)".
     * Trả về null nếu không cấu hình mùa vụ.
     */
    public String getSeasonLabel() {
        if (seasonStartMonth == null || seasonEndMonth == null) return null;
        String[] months = {"", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
                           "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
                           "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        String label = months[seasonStartMonth] + " – " + months[seasonEndMonth];
        if (seasonStartMonth > seasonEndMonth) {
            label += " (qua năm mới)";
        }
        return label;
    }

    /**
     * Kiểm tra tháng hiện tại có thuộc mùa vụ không.
     * Xử lý đúng mùa xuên năm (VD: start=11, end=1 bao gồm tháng 11, 12, 1).
     * Nếu không cấu hình mùa vụ, mặc định trả về true (quảnh năm).
     */
    public boolean isInSeason() {
        if (seasonStartMonth == null || seasonEndMonth == null) return true;
        int m = java.time.LocalDate.now().getMonthValue();
        if (seasonStartMonth <= seasonEndMonth) {
            return m >= seasonStartMonth && m <= seasonEndMonth;
        } else {
            // Mùa xuên năm: VD start=11, end=1 -> tháng 11, 12, 1
            return m >= seasonStartMonth || m <= seasonEndMonth;
        }
    }
}
