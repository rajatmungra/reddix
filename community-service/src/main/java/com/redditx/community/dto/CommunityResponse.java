package com.redditx.community.dto;

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