package com.fruitmkt.model.dto;

import com.fruitmkt.model.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng kết giỏ hàng để hiển thị ở cart page và checkout.
 * 
 * @author fruitmkt-team
 */
public class CartSummaryDTO {

    private List<CartItem> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private BigDecimal totalWeight;

    public CartSummaryDTO() {}

    public CartSummaryDTO(List<CartItem> items, BigDecimal subtotal, BigDecimal discountAmount, BigDecimal deliveryFee, BigDecimal total) {
        this.items = items;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.totalWeight = BigDecimal.ZERO;
    }

    public CartSummaryDTO(List<CartItem> items, BigDecimal subtotal, BigDecimal discountAmount, BigDecimal deliveryFee, BigDecimal total, BigDecimal totalWeight) {
        this.items = items;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.totalWeight = totalWeight;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }
}
