package com.redditx.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedItemResponse(
        UUID feedItemId,
        UUID postId,
        String communityName,
        UUID authorUserId,
        String title,
        String postType,
        long score,
        long commentCount,
        Instant postCreatedAt
) {
}