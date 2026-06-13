package com.redditx.notification.dto;

import com.redditx.notification.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID recipientUserId,
        UUID actorUserId,
        NotificationType type,
        String targetType,
        UUID targetId,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
}