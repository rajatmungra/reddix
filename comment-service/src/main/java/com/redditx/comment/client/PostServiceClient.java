package com.redditx.comment.client;

import com.redditx.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class PostServiceClient {

    private final RestClient restClient;
    private final String internalSecret;

    public PostServiceClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
    }

    public PostResponse getPost(UUID postId) {
        ApiResponse<PostResponse> response = restClient.get()
                .uri("http://post-service/internal/posts/{postId}", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<PostResponse>>() {
                });

        if (response == null || response.data() == null) {
            throw new RestClientException("Empty response from post-service");
        }

        return response.data();
    }

    public void incrementCommentCount(UUID postId) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/comments/increment", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }

    public void decrementCommentCount(UUID postId) {
        restClient.post()
                .uri("http://post-service/internal/posts/{postId}/comments/decrement", postId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
    }
}