package com.redditx.notification.infrastructure;

import com.redditx.notification.domain.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
}