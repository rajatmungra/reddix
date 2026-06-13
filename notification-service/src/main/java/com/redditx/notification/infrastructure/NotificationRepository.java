package com.redditx.notification.infrastructure;

import com.redditx.notification.domain.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(
            UUID recipientUserId,
            Pageable pageable
    );

    Page<NotificationEntity> findByRecipientUserIdAndReadFlagFalseOrderByCreatedAtDesc(
            UUID recipientUserId,
            Pageable pageable
    );

    long countByRecipientUserIdAndReadFlagFalse(UUID recipientUserId);
}