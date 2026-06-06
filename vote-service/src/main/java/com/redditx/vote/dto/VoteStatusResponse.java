package com.redditx.vote.dto;

import com.redditx.vote.domain.VoteTargetType;
import com.redditx.vote.domain.VoteType;

import java.util.UUID;

public record VoteStatusResponse(
        VoteTargetType targetType,
        UUID targetId,
        boolean voted,
        VoteType voteType
) {
}