package com.redditx.comment.client;

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
public class PostServiceClient {

    private final RestClient restClient;
    private final String internalSecret;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public PostServiceClient(
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
                .create("comment-service-to-post-service-get-post")
                .run(
                        () -> callGetPost(postId),
                        throwable -> fallbackGetPost(postId, throwable)
                );
    }

    public void incrementCommentCount(UUID postId) {
        circuitBreakerFactory
                .create("comment-service-to-post-service-increment-comment-count")
                .run(
                        () -> {
                            callIncrementCommentCount(postId);
                            return null;
                        },
                        throwable -> null
                );
    }

    public void decrementCommentCount(UUID postId) {
        circuitBreakerFactory
                .create("comment-service-to-post-service-decrement-comment-count")
                .run(
                        () -> {
                            callDecrementCommentCount(postId);
                            return null;
                        },
                        throwable -> null
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

    private void callIncrementCommentCount(UUID postId) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/comments/increment", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }

    private void callDecrementCommentCount(UUID postId) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/comments/decrement", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }

    private PostResponse fallbackGetPost(UUID postId, Throwable throwable) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Post service is currently unavailable. Please try again later."
        );
    }
}