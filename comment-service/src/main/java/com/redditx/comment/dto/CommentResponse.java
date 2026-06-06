package com.redditx.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID commentId,
        UUID postId,
        UUID authorUserId,
        UUID parentCommentId,
        String content,
        int depth,
        long upvoteCount,
        long downvoteCount,
        long score,
        Instant createdAt,
        Instant updatedAt
) {
}