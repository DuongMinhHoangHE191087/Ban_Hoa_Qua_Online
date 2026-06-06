package com.fruitmkt.model.entity;

import java.time.LocalDateTime;

/**
 * UserAddress — Ánh xạ bảng [user_addresses] trong SQL Server.
 *
 * Columns: address_id, user_id, recipient_name, recipient_phone, address_detail, is_default, created_at
 *
 * @author fruitmkt-team
 */
public class UserAddress {
    private int addressId;
    private int userId;
    private String recipientName;
    private String recipientPhone;
    private String addressDetail;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public UserAddress() {}

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
