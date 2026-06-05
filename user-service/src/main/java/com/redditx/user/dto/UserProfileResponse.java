package com.redditx.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID profileId,
        UUID authUserId,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        long postKarma,
        long commentKarma,
        long totalKarma,
        Instant createdAt
) {
}