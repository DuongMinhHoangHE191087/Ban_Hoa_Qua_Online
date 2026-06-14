package model.entity.chat;
import dao.chat.ChatDAO;

/**
 * ChatSession — Ánh xạ bảng chat_sessions + các trường JOIN phục vụ UI.
 * @author fruitmkt-team
 */
public class ChatSession {

    private int sessionId;
    private int customerId;
    private int ownerId;
    private String status;
    private String sessionType;     // 'SHOP' | 'ADMIN' — [NEW]
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private java.time.LocalDateTime closedAt;

    // Transient fields — populated by JOIN in ChatDAO, not persisted
    private String partnerName;     // Tên đối tác hiển thị trên sidebar [NEW]
    private String partnerAvatar;   // Avatar URL của đối tác [NEW]
    private int unreadCount;        // Số tin chưa đọc (computed) [NEW]

    public ChatSession() {}

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public java.time.LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(java.time.LocalDateTime closedAt) { this.closedAt = closedAt; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerAvatar() { return partnerAvatar; }
    public void setPartnerAvatar(String partnerAvatar) { this.partnerAvatar = partnerAvatar; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
