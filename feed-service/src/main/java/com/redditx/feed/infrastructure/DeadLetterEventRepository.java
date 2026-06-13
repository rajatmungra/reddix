package com.redditx.feed.infrastructure;

import com.redditx.feed.domain.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
}