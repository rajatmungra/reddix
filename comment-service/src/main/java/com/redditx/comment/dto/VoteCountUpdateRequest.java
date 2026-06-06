package com.redditx.comment.dto;

public record VoteCountUpdateRequest(
        int upvoteDelta,
        int downvoteDelta
) {
}