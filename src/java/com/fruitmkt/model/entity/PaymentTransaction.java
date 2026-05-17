package com.fruitmkt.model.entity;

/**
 * PaymentTransaction — Ánh xạ bảng DB tương ứng.
 * TODO: Tham khảo Schema.sql và SRS để hiểu ràng buộc của từng field.
 * @author fruitmkt-team
 */
public class PaymentTransaction {

    private int transactionId;
    private int orderId;
    private String paymentMethod;
    private String sepayTransactionId;
    private String sepayReference;
    private String sepayQrCode;
    private java.math.BigDecimal amount;
    private String currency;
    private String status;
    private java.time.LocalDateTime initiatedAt;
    private java.time.LocalDateTime completedAt;
    private java.time.LocalDateTime expiresAt;
    private String providerResponse;
    private String errorCode;
    private String errorMessage;
    private String ipAddress;

    public PaymentTransaction() {}

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSepayTransactionId() { return sepayTransactionId; }
    public void setSepayTransactionId(String sepayTransactionId) { this.sepayTransactionId = sepayTransactionId; }

    public String getSepayReference() { return sepayReference; }
    public void setSepayReference(String sepayReference) { this.sepayReference = sepayReference; }

    public String getSepayQrCode() { return sepayQrCode; }
    public void setSepayQrCode(String sepayQrCode) { this.sepayQrCode = sepayQrCode; }

    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.time.LocalDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(java.time.LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }

    public java.time.LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(java.time.LocalDateTime completedAt) { this.completedAt = completedAt; }

    public java.time.LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(java.time.LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

}
