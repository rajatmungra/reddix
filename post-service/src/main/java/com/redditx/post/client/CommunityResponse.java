package com.redditx.post.client;

import java.time.Instant;
import java.util.UUID;

public record CommunityResponse(
        UUID communityId,
        String name,
        String displayName,
        String description,
        UUID ownerUserId,
        long memberCount,
        boolean joinedByCurrentUser,
        String currentUserRole,
        Instant createdAt
) {
}