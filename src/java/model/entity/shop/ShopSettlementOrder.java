package model.entity.shop;

/**
 * ShopSettlementOrder — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class ShopSettlementOrder {

    private int settlementOrderId;
    private int settlementId;
    private int orderId;
    private java.math.BigDecimal orderAmount;
    private java.math.BigDecimal platformFeeAmount;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal refundAmount;
    private java.math.BigDecimal netAmount;

    public ShopSettlementOrder() {}

    public int getSettlementOrderId() { return settlementOrderId; }
    public void setSettlementOrderId(int settlementOrderId) { this.settlementOrderId = settlementOrderId; }

    public int getSettlementId() { return settlementId; }
    public void setSettlementId(int settlementId) { this.settlementId = settlementId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public java.math.BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(java.math.BigDecimal orderAmount) { this.orderAmount = orderAmount; }

    public java.math.BigDecimal getPlatformFeeAmount() { return platformFeeAmount; }
    public void setPlatformFeeAmount(java.math.BigDecimal platformFeeAmount) { this.platformFeeAmount = platformFeeAmount; }

    public java.math.BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(java.math.BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public java.math.BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public java.math.BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(java.math.BigDecimal netAmount) { this.netAmount = netAmount; }

}
