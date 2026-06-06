package com.redditx.vote.dto;

import com.redditx.vote.domain.VoteTargetType;
import com.redditx.vote.domain.VoteType;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
        UUID voteId,
        UUID userId,
        VoteTargetType targetType,
        UUID targetId,
        VoteType voteType,
        Instant createdAt,
        Instant updatedAt
) {
}