package com.redditx.vote.client;

import com.redditx.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class PostTargetClient {

    private final RestClient restClient;
    private final String internalSecret;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public PostTargetClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PostResponse getPost(UUID postId) {
        return circuitBreakerFactory
                .create("vote-service-to-post-service-get-post")
                .run(
                        () -> callGetPost(postId),
                        throwable -> fallbackPostUnavailable()
                );
    }

    public void applyVoteDelta(UUID postId, int upvoteDelta, int downvoteDelta) {
        circuitBreakerFactory
                .create("vote-service-to-post-service-apply-vote-delta")
                .run(
                        () -> {
                            callApplyVoteDelta(postId, upvoteDelta, downvoteDelta);
                            return null;
                        },
                        throwable -> fallbackPostUnavailable()
                );
    }

    private PostResponse callGetPost(UUID postId) {
        ApiResponse<PostResponse> response = restClient.get()
                .uri("http://post-service/internal/posts/{postId}", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<PostResponse>>() {
                });

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Empty response from post-service");
        }

        return response.data();
    }

    private void callApplyVoteDelta(UUID postId, int upvoteDelta, int downvoteDelta) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/votes/apply-delta", postId)
                .header("X-Internal-Secret", internalSecret)
                .body(new VoteCountUpdateRequest(upvoteDelta, downvoteDelta))
                .retrieve()
                .toBodilessEntity();
    }

    private <T> T fallbackPostUnavailable() {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Post service is currently unavailable. Please try again later."
        );
    }
}