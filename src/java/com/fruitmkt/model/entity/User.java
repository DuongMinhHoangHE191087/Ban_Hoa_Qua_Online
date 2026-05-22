package com.fruitmkt.model.entity;

import java.time.LocalDateTime;

/**
 * User — Ánh xạ bảng [users] trong SQL Server.
 *
 * Columns: user_id, full_name, email, password_hash, phone, role,
 *          status, user_address, is_email_verified, failed_login_count,
 *          locked_until, created_at, updated_at
 *
 * Roles hợp lệ: CUSTOMER | SHOP_OWNER | DELIVERY | ADMIN  (xem AppConfig)
 * Status hợp lệ: ACTIVE | INACTIVE
 *
 * LƯU Ý:
 *   - password_hash: Lưu chuỗi BCrypt, KHÔNG phải plain text
 *   - locked_until: null = không bị khóa; != null = còn trong thời gian khóa
 *
 * @author fruitmkt-team
 */
public class User {

    private int            userId;
    private String         fullName;
    private String         email;
    private String         passwordHash;
    private String         phone;
    private String         role;           // CUSTOMER | SHOP_OWNER | DELIVERY | ADMIN
    private String         status;         // ACTIVE | INACTIVE
    private String         userAddress;
    private boolean        isEmailVerified;
    private int            failedLoginCount;
    private LocalDateTime  lockedUntil;
    private LocalDateTime  createdAt;
    private LocalDateTime  updatedAt;
    private String         emailVerificationCodeHash;
    private LocalDateTime  emailVerificationExpiresAt;
    private LocalDateTime  emailVerificationResendAt;
    private LocalDateTime  emailVerificationSentAt;

    public User() {}

    // ---- Getters & Setters -------------------------------------------

    public int getUserId()                        { return userId; }
    public void setUserId(int userId)             { this.userId = userId; }

    public String getFullName()                   { return fullName; }
    public void setFullName(String fullName)      { this.fullName = fullName; }

    public String getEmail()                      { return email; }
    public void setEmail(String email)            { this.email = email; }

    public String getPasswordHash()               { return passwordHash; }
    public void setPasswordHash(String hash)      { this.passwordHash = hash; }

    public String getPhone()                      { return phone; }
    public void setPhone(String phone)            { this.phone = phone; }

    public String getRole()                       { return role; }
    public void setRole(String role)              { this.role = role; }

    public String getStatus()                     { return status; }
    public void setStatus(String status)          { this.status = status; }

    public String getUserAddress()                { return userAddress; }
    public void setUserAddress(String addr)       { this.userAddress = addr; }

    public boolean isEmailVerified()              { return isEmailVerified; }
    public void setEmailVerified(boolean v)       { this.isEmailVerified = v; }

    public int getFailedLoginCount()              { return failedLoginCount; }
    public void setFailedLoginCount(int count)    { this.failedLoginCount = count; }

    public LocalDateTime getLockedUntil()         { return lockedUntil; }
    public void setLockedUntil(LocalDateTime t)   { this.lockedUntil = t; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)     { this.updatedAt = t; }

    public String getEmailVerificationCodeHash()        { return emailVerificationCodeHash; }
    public void setEmailVerificationCodeHash(String h)   { this.emailVerificationCodeHash = h; }

    public LocalDateTime getEmailVerificationExpiresAt() { return emailVerificationExpiresAt; }
    public void setEmailVerificationExpiresAt(LocalDateTime t) { this.emailVerificationExpiresAt = t; }

    public LocalDateTime getEmailVerificationResendAt()  { return emailVerificationResendAt; }
    public void setEmailVerificationResendAt(LocalDateTime t) { this.emailVerificationResendAt = t; }

    public LocalDateTime getEmailVerificationSentAt()    { return emailVerificationSentAt; }
    public void setEmailVerificationSentAt(LocalDateTime t) { this.emailVerificationSentAt = t; }

    // ---- Helper Methods ---------------------------------------------

    /** Kiểm tra tài khoản có đang bị khóa không */
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    /** Kiểm tra tài khoản có hoạt động không */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', role='" + role + "'}";
    }

    public String getPassword() {
        return passwordHash;
    }
}
