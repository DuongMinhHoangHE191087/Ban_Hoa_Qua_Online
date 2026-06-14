package model.dto.order;

import model.entity.order.Delivery;
import model.entity.order.Order;

/**
 * DeliveryWithOrderDTO — Gộp thông tin Delivery + Order để hiển thị trên dashboard của Delivery Staff.
 * Tránh việc JSP phải gọi thêm DAO, tuân thủ kiến trúc MVC.
 *
 * @author fruitmkt-team
 */
public class DeliveryWithOrderDTO {

    private Delivery delivery;
    private Order order;

    public DeliveryWithOrderDTO(Delivery delivery, Order order) {
        this.delivery = delivery;
        this.order = order;
    }

    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    // Convenience helpers for JSP
    public int getDeliveryId() { return delivery != null ? delivery.getDeliveryId() : 0; }
    public int getOrderId() { return delivery != null ? delivery.getOrderId() : 0; }
    public String getDeliveryStatus() { return delivery != null ? delivery.getStatus() : ""; }
    public String getRecipientName() { return order != null ? order.getRecipientName() : ""; }
    public String getRecipientPhone() { return order != null ? order.getRecipientPhone() : ""; }
    public String getDeliveryAddress() { return order != null ? order.getDeliveryAddress() : ""; }
    public java.time.LocalDateTime getEstimatedDeliveryTime() {
        return delivery != null ? delivery.getEstimatedDeliveryTime() : null;
    }
    public String getFailureReason() { return delivery != null ? delivery.getFailureReason() : null; }
}
