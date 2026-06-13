package com.redditx.comment.event;

import java.time.Instant;
import java.util.UUID;

public record CommentDeletedEvent(
        UUID eventId,
        UUID commentId,
        UUID postId,
        UUID deletedByUserId,
        Instant deletedAt
) {
}