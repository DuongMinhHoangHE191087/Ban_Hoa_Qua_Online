package com.fruitmkt.model.dto;

/**
 * /** DTO tổng kết giỏ hàng để hiển thị ở cart page và checkout. */
 * @author fruitmkt-team
 */
public class CartSummaryDTO {

        private java.util.List<com.fruitmkt.model.entity.CartItem> items;
        private java.math.BigDecimal subtotal;
        private java.math.BigDecimal discountAmount;
        private java.math.BigDecimal deliveryFee;
        private java.math.BigDecimal total;

    // TODO: Thêm constructor, getters, setters theo fields bên trên
    public CartSummaryDTO() {}
}
