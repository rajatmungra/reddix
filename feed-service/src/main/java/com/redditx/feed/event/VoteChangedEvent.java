package com.redditx.feed.event;

import java.time.Instant;
import java.util.UUID;

public record VoteChangedEvent(
        UUID eventId,
        UUID userId,
        String targetType,
        UUID targetId,
        UUID targetOwnerUserId,
        String targetTitle,
        String oldVoteType,
        String newVoteType,
        int upvoteDelta,
        int downvoteDelta,
        Instant changedAt
) {
}