package com.redditx.post.dto;

import com.redditx.post.domain.PostType;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID postId,
        String communityName,
        UUID authorUserId,
        String title,
        String content,
        String url,
        PostType postType,
        long upvoteCount,
        long downvoteCount,
        long score,
        long commentCount,
        Instant createdAt,
        Instant updatedAt
) {
}