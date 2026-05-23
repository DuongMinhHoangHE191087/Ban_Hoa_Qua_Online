package com.fruitmkt.model.entity;

/**
 * Review — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class Review {

    private int reviewId;
    private int orderItemId;
    private int customerId;
    private int rating;
    private String reviewText;
    private boolean isHidden;
    private java.time.LocalDateTime createdAt;

    private String reviewImageUrl;
    private String customerName;

    public Review() {}

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public boolean getIsHidden() { return isHidden; }
    public void setIsHidden(boolean isHidden) { this.isHidden = isHidden; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Lấy đường dẫn ảnh đánh giá thực tế từ khách hàng.
     * @return đường dẫn ảnh (có thể null nếu đánh giá không kèm ảnh)
     */
    public String getReviewImageUrl() {
        return reviewImageUrl;
    }

    /**
     * Thiết lập đường dẫn ảnh đánh giá thực tế.
     * @param reviewImageUrl đường dẫn ảnh
     */
    public void setReviewImageUrl(String reviewImageUrl) {
        this.reviewImageUrl = reviewImageUrl;
    }

    /**
     * Lấy tên đầy đủ của khách hàng viết đánh giá này.
     * Thuộc tính này được truy vấn từ bảng users qua câu JOIN SQL.
     * @return tên đầy đủ của khách hàng
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Thiết lập tên đầy đủ của khách hàng viết đánh giá.
     * @param customerName tên khách hàng
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

}

