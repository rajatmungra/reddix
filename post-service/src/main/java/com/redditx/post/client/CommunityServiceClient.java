package com.redditx.post.client;

import com.redditx.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CommunityServiceClient {

    private final RestClient restClient;
    private final String internalSecret;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public CommunityServiceClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public CommunityResponse getCommunityByName(String communityName) {
        return circuitBreakerFactory
                .create("post-service-to-community-service")
                .run(
                        () -> callCommunityService(communityName),
                        throwable -> fallbackCommunityService(communityName, throwable)
                );
    }

    private CommunityResponse callCommunityService(String communityName) {
        ApiResponse<CommunityResponse> response = restClient.get()
                .uri("http://community-service/internal/communities/{name}", communityName)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<CommunityResponse>>() {
                });

        if (response == null || response.data() == null) {
            throw new IllegalStateException("Empty response from community-service");
        }

        return response.data();
    }

    private CommunityResponse fallbackCommunityService(
            String communityName,
            Throwable throwable
    ) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Community service is currently unavailable. Please try again later."
        );
    }
}