package model.dto.product;

import java.math.BigDecimal;

/**
 * DTO hiển thị 1 card sản phẩm trong trang listing — chỉ các field cần thiết.
 * 
 * @author fruitmkt-team
 */
public class ProductListDTO {

    private int productId;
    private String name;
    private String primaryImagePath;
    private BigDecimal minPrice; // Giá thấp nhất trong các variant
    private BigDecimal maxPrice;
    private BigDecimal rating;
    private int soldQuantity;
    private String shopName;
    private int shopId;
    private String categoryName;

    public ProductListDTO() {}

    public ProductListDTO(int productId, String name, String primaryImagePath, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal rating, int soldQuantity, String shopName, int shopId, String categoryName) {
        this.productId = productId;
        this.name = name;
        this.primaryImagePath = primaryImagePath;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.rating = rating;
        this.soldQuantity = soldQuantity;
        this.shopName = shopName;
        this.shopId = shopId;
        this.categoryName = categoryName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryImagePath() {
        return primaryImagePath;
    }

    public void setPrimaryImagePath(String primaryImagePath) {
        this.primaryImagePath = primaryImagePath;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
