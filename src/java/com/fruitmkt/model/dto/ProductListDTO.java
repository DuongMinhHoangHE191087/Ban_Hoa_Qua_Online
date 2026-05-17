package com.fruitmkt.model.dto;

/**
 * /** DTO hiển thị 1 card sản phẩm trong trang listing — chỉ các field cần thiết. */
 * @author fruitmkt-team
 */
public class ProductListDTO {

        private int productId;
        private String name;
        private String primaryImagePath;
        private java.math.BigDecimal minPrice; // Giá thấp nhất trong các variant
        private java.math.BigDecimal maxPrice;
        private java.math.BigDecimal rating;
        private int soldQuantity;
        private String shopName;
        private int shopId;
        private String categoryName;

    // TODO: Thêm constructor, getters, setters theo fields bên trên
    public ProductListDTO() {}
}
