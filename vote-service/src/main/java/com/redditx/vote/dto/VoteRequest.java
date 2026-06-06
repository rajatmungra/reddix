package com.redditx.vote.dto;

import com.redditx.vote.domain.VoteTargetType;
import com.redditx.vote.domain.VoteType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VoteRequest(

        @NotNull(message = "Target type is required")
        VoteTargetType targetType,

        @NotNull(message = "Target id is required")
        UUID targetId,

        @NotNull(message = "Vote type is required")
        VoteType voteType
) {
}