package com.redditx.post.dto;

public record VoteCountUpdateRequest(
        int upvoteDelta,
        int downvoteDelta
) {
}