package model.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị 1 card đơn hàng trong order history.
 * 
 * @author fruitmkt-team
 */
public class OrderSummaryDTO {

    private int orderId;
    private String orderCode;   // Dùng để hiển thị và tra cứu
    private String status;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private int itemCount;
    private String shopName;

    public OrderSummaryDTO() {}

    public OrderSummaryDTO(int orderId, String orderCode, String status, BigDecimal finalAmount, LocalDateTime createdAt, int itemCount, String shopName) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.status = status;
        this.finalAmount = finalAmount;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
        this.shopName = shopName;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
