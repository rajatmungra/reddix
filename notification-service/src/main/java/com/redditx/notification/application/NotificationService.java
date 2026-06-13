package com.redditx.notification.application;

import com.redditx.common.dto.PageResponse;
import com.redditx.notification.domain.NotificationEntity;
import com.redditx.notification.domain.NotificationType;
import com.redditx.notification.domain.ProcessedEvent;
import com.redditx.notification.dto.NotificationResponse;
import com.redditx.notification.dto.UnreadCountResponse;
import com.redditx.notification.event.CommentCreatedEvent;
import com.redditx.notification.event.VoteChangedEvent;
import com.redditx.notification.infrastructure.NotificationRepository;
import com.redditx.notification.infrastructure.ProcessedEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            ProcessedEventRepository processedEventRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handleCommentCreated(CommentCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        if (event.parentCommentId() != null) {
            createReplyNotification(event);
        } else {
            createPostCommentNotification(event);
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "CommentCreatedEvent")
        );
    }

    @Transactional
    public void handleVoteChanged(VoteChangedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        if (event.newVoteType() == null) {
            processedEventRepository.save(
                    new ProcessedEvent(event.eventId(), "VoteChangedEvent")
            );
            return;
        }

        if (event.targetOwnerUserId() == null) {
            processedEventRepository.save(
                    new ProcessedEvent(event.eventId(), "VoteChangedEvent")
            );
            return;
        }

        if ("POST".equalsIgnoreCase(event.targetType())) {
            createPostVoteNotification(event);
        }

        if ("COMMENT".equalsIgnoreCase(event.targetType())) {
            createCommentVoteNotification(event);
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "VoteChangedEvent")
        );
    }

    private void createPostCommentNotification(CommentCreatedEvent event) {
        UUID recipientUserId = event.postAuthorUserId();

        if (recipientUserId == null || recipientUserId.equals(event.authorUserId())) {
            return;
        }

        NotificationEntity notification = new NotificationEntity(
                recipientUserId,
                event.authorUserId(),
                NotificationType.POST_COMMENT,
                "POST",
                event.postId(),
                "New comment on your post",
                "Someone commented on your post: " + safeTitle(event.postTitle())
        );

        notificationRepository.save(notification);
    }

    private void createReplyNotification(CommentCreatedEvent event) {
        UUID recipientUserId = event.parentCommentAuthorUserId();

        if (recipientUserId == null || recipientUserId.equals(event.authorUserId())) {
            return;
        }

        NotificationEntity notification = new NotificationEntity(
                recipientUserId,
                event.authorUserId(),
                NotificationType.COMMENT_REPLY,
                "COMMENT",
                event.parentCommentId(),
                "New reply to your comment",
                "Someone replied to your comment on post: " + safeTitle(event.postTitle())
        );

        notificationRepository.save(notification);
    }

    private void createPostVoteNotification(VoteChangedEvent event) {
        UUID recipientUserId = event.targetOwnerUserId();

        if (recipientUserId.equals(event.userId())) {
            return;
        }

        NotificationType type = "UPVOTE".equalsIgnoreCase(event.newVoteType())
                ? NotificationType.POST_UPVOTE
                : NotificationType.POST_DOWNVOTE;

        String action = "UPVOTE".equalsIgnoreCase(event.newVoteType())
                ? "upvoted"
                : "downvoted";

        NotificationEntity notification = new NotificationEntity(
                recipientUserId,
                event.userId(),
                type,
                "POST",
                event.targetId(),
                "New vote on your post",
                "Someone " + action + " your post: " + safeTitle(event.targetTitle())
        );

        notificationRepository.save(notification);
    }

    private void createCommentVoteNotification(VoteChangedEvent event) {
        UUID recipientUserId = event.targetOwnerUserId();

        if (recipientUserId.equals(event.userId())) {
            return;
        }

        NotificationType type = "UPVOTE".equalsIgnoreCase(event.newVoteType())
                ? NotificationType.COMMENT_UPVOTE
                : NotificationType.COMMENT_DOWNVOTE;

        String action = "UPVOTE".equalsIgnoreCase(event.newVoteType())
                ? "upvoted"
                : "downvoted";

        NotificationEntity notification = new NotificationEntity(
                recipientUserId,
                event.userId(),
                type,
                "COMMENT",
                event.targetId(),
                "New vote on your comment",
                "Someone " + action + " your comment: " + safeTitle(event.targetTitle())
        );

        notificationRepository.save(notification);
    }

    public PageResponse<NotificationResponse> getNotifications(
            UUID currentUserId,
            boolean unreadOnly,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<NotificationEntity> notifications = unreadOnly
                ? notificationRepository.findByRecipientUserIdAndReadFlagFalseOrderByCreatedAtDesc(
                currentUserId,
                PageRequest.of(safePage, safeSize)
        )
                : notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(
                currentUserId,
                PageRequest.of(safePage, safeSize)
        );

        return new PageResponse<>(
                notifications.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isLast()
        );
    }

    public UnreadCountResponse getUnreadCount(UUID currentUserId) {
        long count = notificationRepository.countByRecipientUserIdAndReadFlagFalse(
                currentUserId
        );

        return new UnreadCountResponse(count);
    }

    @Transactional
    public NotificationResponse markRead(UUID currentUserId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        if (!notification.belongsTo(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this notification"
            );
        }

        notification.markRead();

        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(UUID currentUserId) {
        Page<NotificationEntity> unreadNotifications =
                notificationRepository.findByRecipientUserIdAndReadFlagFalseOrderByCreatedAtDesc(
                        currentUserId,
                        PageRequest.of(0, 500)
                );

        unreadNotifications.getContent().forEach(NotificationEntity::markRead);

        notificationRepository.saveAll(unreadNotifications.getContent());
    }

    private NotificationResponse toResponse(NotificationEntity notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getActorUserId(),
                notification.getType(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isReadFlag(),
                notification.getCreatedAt()
        );
    }

    private String safeTitle(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim();
    }
}