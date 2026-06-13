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
public class CommentTargetClient {

    private final RestClient restClient;
    private final String internalSecret;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public CommentTargetClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public CommentResponse getComment(UUID commentId) {
        return circuitBreakerFactory
                .create("vote-service-to-comment-service-get-comment")
                .run(
                        () -> callGetComment(commentId),
                        throwable -> fallbackCommentUnavailable()
                );
    }

    public void applyVoteDelta(UUID commentId, int upvoteDelta, int downvoteDelta) {
        circuitBreakerFactory
                .create("vote-service-to-comment-service-apply-vote-delta")
                .run(
                        () -> {
                            callApplyVoteDelta(commentId, upvoteDelta, downvoteDelta);
                            return null;
                        },
                        throwable -> fallbackCommentUnavailable()
                );
    }

    private CommentResponse callGetComment(UUID commentId) {
        ApiResponse<CommentResponse> response = restClient.get()
                .uri("http://comment-service/internal/comments/{commentId}", commentId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<CommentResponse>>() {
                });

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Empty response from comment-service");
        }

        return response.data();
    }

    private void callApplyVoteDelta(UUID commentId, int upvoteDelta, int downvoteDelta) {
        restClient.post()
                .uri("http://comment-service/internal/comments/{commentId}/votes/apply-delta", commentId)
                .header("X-Internal-Secret", internalSecret)
                .body(new VoteCountUpdateRequest(upvoteDelta, downvoteDelta))
                .retrieve()
                .toBodilessEntity();
    }

    private <T> T fallbackCommentUnavailable() {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Comment service is currently unavailable. Please try again later."
        );
    }
}