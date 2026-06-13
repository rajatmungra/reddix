package com.redditx.notification.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_created", columnList = "recipient_user_id,created_at"),
                @Index(name = "idx_notifications_recipient_read", columnList = "recipient_user_id,read_flag")
        }
)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "read_flag", nullable = false)
    private boolean readFlag;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            String targetType,
            UUID targetId,
            String title,
            String message
    ) {
        this.recipientUserId = recipientUserId;
        this.actorUserId = actorUserId;
        this.type = type;
        this.targetType = targetType;
        this.targetId = targetId;
        this.title = title;
        this.message = message;
        this.readFlag = false;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void markRead() {
        this.readFlag = true;
    }

    public boolean belongsTo(UUID userId) {
        return this.recipientUserId.equals(userId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isReadFlag() {
        return readFlag;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}