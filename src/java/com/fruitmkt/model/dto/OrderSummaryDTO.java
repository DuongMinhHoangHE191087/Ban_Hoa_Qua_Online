package com.fruitmkt.model.dto;

/**
 * /** DTO hiển thị 1 card đơn hàng trong order history. */
 * @author fruitmkt-team
 */
public class OrderSummaryDTO {

        private int orderId;
        private String orderCode;   // Dùng để hiển thị và tra cứu
        private String status;
        private java.math.BigDecimal finalAmount;
        private java.time.LocalDateTime createdAt;
        private int itemCount;
        private String shopName;

    // TODO: Thêm constructor, getters, setters theo fields bên trên
    public OrderSummaryDTO() {}
}
