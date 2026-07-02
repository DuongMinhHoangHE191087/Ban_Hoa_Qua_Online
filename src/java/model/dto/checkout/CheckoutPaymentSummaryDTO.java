package model.dto.checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO tóm tắt payment/order tree cho checkout payment page và poll trạng thái.
 */
// Touched for IDE re-indexing
public class CheckoutPaymentSummaryDTO {

    private int requestedOrderId;
    private int orderId;
    private String orderType;
    private String paymentMethod;
    private String orderStatus;
    private String paymentStatus;
    private String reference;
    private BigDecimal finalAmount = BigDecimal.ZERO;
    private boolean paymentRequired;
    private boolean paid;
    private boolean pendingPayment;
    private boolean cancelled;
    private boolean canConfirmPayment;
    private List<Integer> childOrderIds = new ArrayList<>();

    public int getRequestedOrderId() {
        return requestedOrderId;
    }

    public void setRequestedOrderId(int requestedOrderId) {
        this.requestedOrderId = requestedOrderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public boolean getPaymentRequired() {
        return paymentRequired;
    }

    public void setPaymentRequired(boolean paymentRequired) {
        this.paymentRequired = paymentRequired;
    }

    public boolean getPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean getPendingPayment() {
        return pendingPayment;
    }

    public void setPendingPayment(boolean pendingPayment) {
        this.pendingPayment = pendingPayment;
    }

    public boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean getCanConfirmPayment() {
        return canConfirmPayment;
    }

    public void setCanConfirmPayment(boolean canConfirmPayment) {
        this.canConfirmPayment = canConfirmPayment;
    }

    public List<Integer> getChildOrderIds() {
        return childOrderIds;
    }

    public void setChildOrderIds(List<Integer> childOrderIds) {
        this.childOrderIds = childOrderIds;
    }
}
