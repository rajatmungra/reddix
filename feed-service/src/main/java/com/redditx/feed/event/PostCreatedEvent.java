package com.redditx.feed.event;

import java.time.Instant;
import java.util.UUID;

public record PostCreatedEvent(
        UUID eventId,
        UUID postId,
        String communityName,
        UUID authorUserId,
        String title,
        String postType,
        Instant createdAt
) {
}