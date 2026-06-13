package com.redditx.comment.event;

import java.time.Instant;
import java.util.UUID;

public record CommentCreatedEvent(
        UUID eventId,
        UUID commentId,
        UUID postId,
        UUID postAuthorUserId,
        String postTitle,
        UUID authorUserId,
        UUID parentCommentId,
        UUID parentCommentAuthorUserId,
        Instant createdAt
) {
}