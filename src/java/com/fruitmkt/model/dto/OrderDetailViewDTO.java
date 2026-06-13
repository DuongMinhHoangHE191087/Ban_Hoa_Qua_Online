package com.fruitmkt.model.dto;

import com.fruitmkt.model.entity.Delivery;
import com.fruitmkt.model.entity.Order;
import com.fruitmkt.model.entity.OrderItem;
import com.fruitmkt.model.entity.PaymentTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Du lieu view cho trang chi tiet/invoice don hang.
 */
public class OrderDetailViewDTO {

    private Order order;
    private List<OrderItem> orderItems = new ArrayList<>();
    private PaymentTransaction paymentTransaction;
    private Delivery delivery;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}
