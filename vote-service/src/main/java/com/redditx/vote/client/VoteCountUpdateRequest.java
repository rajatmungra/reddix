package com.redditx.vote.client;

public record VoteCountUpdateRequest(
        int upvoteDelta,
        int downvoteDelta
) {
}