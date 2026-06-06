package com.fruitmkt.model.entity;

/**
 * ShopProfile — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ShopProfile {

    private int profileId;
    private int userId;
    private String shopName;
    private String shopDescription;
    private String approvalStatus;
    private String rejectionReason;
    private java.time.LocalDateTime approvedAt;
    private String deliveryAddress;
    private java.math.BigDecimal rating;
    private String preferredCategories; // JSON array category_id: "[1,3,5]"
    private String docPaths;            // JSON array đượng dẫn tài liệu: ["uploads/shop-docs/1/uuid.pdf"]
    private String businessEmail;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String logoUrl;
    private String coverUrl;

    public ShopProfile() {}

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getShopDescription() { return shopDescription; }
    public void setShopDescription(String shopDescription) { this.shopDescription = shopDescription; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public java.time.LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(java.time.LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public java.math.BigDecimal getRating() { return rating; }
    public void setRating(java.math.BigDecimal rating) { this.rating = rating; }

    public String getPreferredCategories() { return preferredCategories; }
    public void setPreferredCategories(String preferredCategories) { this.preferredCategories = preferredCategories; }

    public String getDocPaths() { return docPaths; }
    public void setDocPaths(String docPaths) { this.docPaths = docPaths; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

}
