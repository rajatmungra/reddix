package com.redditx.vote.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class PostTargetClient {

    private final RestClient restClient;
    private final String internalSecret;

    public PostTargetClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
    }

    public void validatePostExists(UUID postId) {
        restClient.get()
                .uri("http://post-service/internal/posts/{postId}", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }

    public void applyVoteDelta(UUID postId, int upvoteDelta, int downvoteDelta) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/votes/apply-delta", postId)
                .header("X-Internal-Secret", internalSecret)
                .body(new VoteCountUpdateRequest(upvoteDelta, downvoteDelta))
                .retrieve()
                .toBodilessEntity();
    }
}