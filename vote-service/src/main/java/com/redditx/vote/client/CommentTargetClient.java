package com.redditx.vote.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CommentTargetClient {

    private final RestClient restClient;
    private final String internalSecret;

    public CommentTargetClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
    }

    public void validateCommentExists(UUID commentId) {
        restClient.get()
                .uri("http://comment-service/internal/comments/{commentId}", commentId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }

    public void applyVoteDelta(UUID commentId, int upvoteDelta, int downvoteDelta) {
        restClient.post()
                .uri("http://comment-service/internal/comments/{commentId}/votes/apply-delta", commentId)
                .header("X-Internal-Secret", internalSecret)
                .body(new VoteCountUpdateRequest(upvoteDelta, downvoteDelta))
                .retrieve()
                .toBodilessEntity();
    }
}